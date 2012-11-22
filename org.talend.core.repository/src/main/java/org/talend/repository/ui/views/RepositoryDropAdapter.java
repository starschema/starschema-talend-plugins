// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.views;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.part.PluginDropAdapter;
import org.osgi.framework.FrameworkUtil;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.business.BusinessType;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.i18n.Messages;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.utils.AbstractResourceChangesService;
import org.talend.core.repository.utils.TDQServiceRegister;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.designer.business.diagram.custom.IDiagramModelService;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.actions.CopyObjectAction;
import org.talend.repository.model.actions.MoveObjectAction;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: RepositoryDropAdapter.java 84099 2012-05-24 07:59:33Z cli $
 * 
 */
public class RepositoryDropAdapter extends PluginDropAdapter {

    public RepositoryDropAdapter(StructuredViewer viewer) {
        super(viewer);
    }

    private DropTargetEvent event;

    @Override
    public void drop(DropTargetEvent event) {
        this.event = event;
        super.drop(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.PluginDropAdapter#performDrop(java.lang.Object)
     */
    @Override
    public boolean performDrop(final Object data) {
        int operation = getCurrentOperation();
        final RepositoryNode targetNode = (RepositoryNode) getCurrentTarget();
        boolean toReturn = true;

        try {
            switch (operation) {
            case DND.DROP_COPY:
                RunnableWithReturnValue runnable = new CopyRunnable(
                        Messages.getString("RepositoryDropAdapter_copyingItems"), data, targetNode); //$NON-NLS-1$
                runInProgressDialog(runnable);
                toReturn = (Boolean) runnable.getReturnValue();
                break;
            case DND.DROP_MOVE:
                runnable = new MoveRunnable(Messages.getString("RepositoryDropAdapter_movingItems"), data, targetNode); //$NON-NLS-1$
                runInProgressDialog(runnable);
                toReturn = (Boolean) runnable.getReturnValue();
                break;
            // TDI-20080 hqzhang, operation value(actually is event.detail) on drag enter state maybe different from
            // value on drop
            // state, this could be a bug of ViewerDropAdapter
            case DND.DROP_DEFAULT:
                if (event.detail == DND.DROP_COPY) {
                    runnable = new CopyRunnable(Messages.getString("RepositoryDropAdapter_copyingItems"), data, targetNode); //$NON-NLS-1$
                    runInProgressDialog(runnable);
                    toReturn = (Boolean) runnable.getReturnValue();
                    break;
                }
                if (event.detail == DND.DROP_MOVE) {
                    runnable = new MoveRunnable(Messages.getString("RepositoryDropAdapter_movingItems"), data, targetNode); //$NON-NLS-1$
                    runInProgressDialog(runnable);
                    toReturn = (Boolean) runnable.getReturnValue();
                    break;
                }
            default:
                // Nothing to do
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        return toReturn;
    }

    /**
     * ADD gdbu 2011-9-29 TDQ-3546
     * 
     * DOC gdbu(TDQ) Comment method "judgeCanMoveOrNotByDependency". If current connecton have dependencies in TDQ ,
     * then return false and that means can not move it.
     * 
     * @param sourceNode
     * @param resourceChangeService
     * @return
     */
    private boolean judgeCanMoveOrNotByDependency(RepositoryNode sourceNode, AbstractResourceChangesService resourceChangeService) {
        for (IRepositoryNode iRepositoryNode : sourceNode.getChildren()) {
            RepositoryNode repositoryNode = (RepositoryNode) iRepositoryNode;
            if (repositoryNode.getType().equals(ENodeType.SIMPLE_FOLDER)) {
                judgeCanMoveOrNotByDependency(repositoryNode, resourceChangeService);
            } else {
                // iRepositoryNode.getObject().getProperty().getItem().eResource().unload();
                Item item = repositoryNode.getObject() == null ? null : repositoryNode.getObject().getProperty().getItem();
                if (resourceChangeService != null && null != item) {
                    boolean handleResourceChange = resourceChangeService.handleResourceChange(((ConnectionItem) item)
                            .getConnection());
                    if (!handleResourceChange) {
                        return handleResourceChange;
                    }
                }
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.PluginDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
        if (target == null)
            return false;
        super.validateDrop(target, operation, transferType);
        boolean isValid = true;
        for (Object obj : ((StructuredSelection) getViewer().getSelection()).toArray()) {
            RepositoryNode sourceNode = (RepositoryNode) obj;

            if (sourceNode != null) {
                IRepositoryViewObject object = sourceNode.getObject();
                if (object == null) {
                    return false;
                }
                if (object.getRepositoryObjectType() == ERepositoryObjectType.JOB_DOC
                        || object.getRepositoryObjectType() == ERepositoryObjectType.JOBLET_DOC) {
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IDiagramModelService.class)) {
                        IDiagramModelService diagramModelService = (IDiagramModelService) GlobalServiceRegister.getDefault()
                                .getService(IDiagramModelService.class);
                        if (diagramModelService != null && BusinessType.SHAP == diagramModelService.getBusinessModelType(target)) {
                            return true;
                        }
                    }

                    return false;
                } else if (object.getRepositoryObjectType() == ERepositoryObjectType.ROUTINES) {
                    Property property = object.getProperty();
                    RoutineItem item = (RoutineItem) property.getItem();
                    if (item.isBuiltIn() && target instanceof RepositoryNode) {
                        return false;
                    }
                } else if (object.getRepositoryObjectType() == ERepositoryObjectType.SQLPATTERNS) {
                    Property property = object.getProperty();
                    SQLPatternItem item = (SQLPatternItem) property.getItem();
                    if (item.isSystem() && target instanceof RepositoryNode) {
                        return false;
                    }
                }

            }

            switch (operation) {
            case DND.DROP_COPY:
                isValid = CopyObjectAction.getInstance().validateAction(sourceNode, (RepositoryNode) target);
                break;
            // case DND.DROP_NONE:
            case DND.DROP_MOVE:
                isValid = MoveObjectAction.getInstance().validateAction(sourceNode, (RepositoryNode) target, true);
                break;
            case DND.DROP_DEFAULT:
            case DND.Drop: // hywang
                isValid = MoveObjectAction.getInstance().validateAction(sourceNode, (RepositoryNode) target, true);
                break;
            default:
                isValid = false;
            }
            // if (!isValid) {
            // return isValid;
            // }
        }

        return isValid;
    }

    private void runInProgressDialog(final IWorkspaceRunnable op) {
        final IRunnableWithProgress iRunnableWithProgress = new IRunnableWithProgress() {

            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                try {
                    ISchedulingRule schedulingRule = workspace.getRoot();
                    // the update the project files need to be done in the workspace runnable to avoid all
                    // notifications.
                    // of changes before the end of the modifications.
                    workspace.run(op, schedulingRule, IWorkspace.AVOID_UPDATE, monitor);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                }
            }
        };

        RepositoryWorkUnit<Object> repositoryWorkUnit = new RepositoryWorkUnit<Object>("Move or Copy", this) {

            @Override
            protected void run() throws LoginException, PersistenceException {
                try {
                    ProgressMonitorDialog progress = new ProgressMonitorDialog(getViewer().getControl().getShell());
                    progress.run(false, false, iRunnableWithProgress);
                } catch (InvocationTargetException e) {
                    ExceptionHandler.process(e);
                } catch (InterruptedException e) {
                    ExceptionHandler.process(e);
                }
            }
        };
        repositoryWorkUnit.setAvoidUnloadResources(true);
        CoreRuntimePlugin.getInstance().getProxyRepositoryFactory().executeRepositoryWorkUnit(repositoryWorkUnit);
    }

    /**
     * hqzhang.
     */
    class CopyRunnable extends RunnableWithReturnValue {

        private Object data;

        private RepositoryNode targetNode;

        public CopyRunnable(String taskName, Object data, RepositoryNode targetNode) {
            super(taskName);
            this.data = data;
            this.targetNode = targetNode;
        }

        public void run(IProgressMonitor monitor) throws CoreException {
            monitor.beginTask(getTaskName(), IProgressMonitor.UNKNOWN);
            String copyName = "User action : Copy Object"; //$NON-NLS-1$
            RepositoryWorkUnit<Object> repositoryWorkUnit = new RepositoryWorkUnit<Object>(copyName,
                    CopyObjectAction.getInstance()) {

                @Override
                protected void run() throws LoginException, PersistenceException {
                    try {
                        for (Object obj : ((StructuredSelection) data).toArray()) {
                            final RepositoryNode sourceNode = (RepositoryNode) obj;
                            CopyObjectAction.getInstance().execute(sourceNode, targetNode);
                        }
                    } catch (Exception e) {
                        throw new PersistenceException(e);
                    }
                }
            };
            ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(repositoryWorkUnit);
            setReturnValue(true);
            monitor.done();
        }

    }

    /**
     * hqzhang.
     */
    class MoveRunnable extends RunnableWithReturnValue {

        private Object data;

        private RepositoryNode targetNode;

        public MoveRunnable(String taskName, Object data, RepositoryNode targetNode) {
            super(taskName);
            this.data = data;
            this.targetNode = targetNode;
        }

        public void run(final IProgressMonitor monitor) throws CoreException {
            monitor.beginTask(getTaskName(), IProgressMonitor.UNKNOWN);
            // MOD gdbu 2011-10-9 TDQ-3545
            List<?> selectItems = ((org.eclipse.jface.viewers.TreeSelection) data).toList();
            for (Object object : selectItems) {
                if (object instanceof RepositoryNode) {
                    RepositoryNode repositoryNode = (RepositoryNode) object;
                    boolean isLock = false;
                    monitor.subTask(Messages.getString("RepositoryDropAdapter.checkingLockStatus") + repositoryNode.getObject().getLabel()); //$NON-NLS-1$
                    isLock = MoveObjectAction.getInstance().isLock(repositoryNode);
                    if (isLock) {
                        String errorMsg = null;
                        IRepositoryViewObject objectToCopy = repositoryNode.getObject();
                        if (ProxyRepositoryFactory.getInstance().getStatus(objectToCopy) == ERepositoryStatus.LOCK_BY_USER) {
                            errorMsg = Messages.getString("RepositoryDropAdapter_lockedByYou"); //$NON-NLS-1$
                        }
                        if (ProxyRepositoryFactory.getInstance().getStatus(objectToCopy) == ERepositoryStatus.LOCK_BY_OTHER) {
                            errorMsg = Messages.getString("RepositoryDropAdapter_lockedByOthers"); //$NON-NLS-1$
                        }
                        MessageDialog.openInformation(getViewer().getControl().getShell(),
                                Messages.getString("RepositoryDropAdapter_moveTitle"), //$NON-NLS-1$
                                errorMsg); //$NON-NLS-1$ 
                        setReturnValue(false);
                        return;
                    }
                    ERepositoryObjectType sourceType = (ERepositoryObjectType) repositoryNode
                            .getProperties(EProperties.CONTENT_TYPE);
                    if (sourceType == ERepositoryObjectType.METADATA_CONNECTIONS
                            || sourceType == ERepositoryObjectType.METADATA_FILE_DELIMITED
                            || sourceType == ERepositoryObjectType.METADATA_MDMCONNECTION) {

                        AbstractResourceChangesService resourceChangeService = TDQServiceRegister.getInstance()
                                .getResourceChangeService(AbstractResourceChangesService.class);
                        boolean judgeCanMoveOrNotByDependency = judgeCanMoveOrNotByDependency(repositoryNode,
                                resourceChangeService);
                        if (!judgeCanMoveOrNotByDependency) {
                            setReturnValue(false);
                            return;
                        }
                    }
                }
            }
            // ~TDQ-3545

            String moveName = "User action : Move Object"; //$NON-NLS-1$
            RepositoryWorkUnit<Object> repositoryWorkUnit = new RepositoryWorkUnit<Object>(moveName,
                    MoveObjectAction.getInstance()) {

                @Override
                protected void run() throws LoginException, PersistenceException {
                    final IWorkspaceRunnable op = new IWorkspaceRunnable() {

                        public void run(IProgressMonitor monitor) throws CoreException {
                            try {
                                for (Object obj : ((StructuredSelection) data).toArray()) {
                                    final RepositoryNode sourceNode = (RepositoryNode) obj;
                                    monitor.subTask(Messages.getString("RepositoryDropAdapter.moving") + sourceNode.getObject().getLabel()); //$NON-NLS-1$
                                    MoveObjectAction.getInstance().execute(sourceNode, targetNode, true);
                                }
                            } catch (Exception e) {
                                throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR, FrameworkUtil
                                        .getBundle(this.getClass()).getSymbolicName(), "Error", e));
                            }
                        };

                    };
                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
                    try {
                        ISchedulingRule schedulingRule = workspace.getRoot();
                        // the update the project files need to be done in the workspace runnable to avoid all
                        // notification
                        // of changes before the end of the modifications.
                        workspace.run(op, schedulingRule, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
                    } catch (CoreException e) {
                        throw new PersistenceException(e.getCause());
                    }

                }
            };
            ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(repositoryWorkUnit);
            setReturnValue(true);
            monitor.done();

            setReturnValue(true);
            monitor.done();
        }
    }

    /**
     * hqzhang.
     */
    abstract class RunnableWithReturnValue implements IWorkspaceRunnable {

        public RunnableWithReturnValue(String taskName) {
            this.taskName = taskName;
        }

        private String taskName;

        private Object returnValue;

        protected Object getReturnValue() {
            return this.returnValue;
        }

        protected void setReturnValue(Object returnValue) {
            this.returnValue = returnValue;
        }

        protected String getTaskName() {
            return this.taskName;
        }

        protected void setTaskName(String taskName) {
            this.taskName = taskName;
        }

    }
}
