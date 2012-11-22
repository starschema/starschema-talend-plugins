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
package org.talend.repository.model.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.ICDCProviderService;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.dialog.PastSelectorDialog;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: CopyObjectAction.java 82489 2012-04-25 06:15:54Z plv $
 * 
 */
public class CopyObjectAction {

    IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    private static CopyObjectAction singleton = new CopyObjectAction();

    public static CopyObjectAction getInstance() {
        return singleton;
    }

    public boolean validateAction(RepositoryNode sourceNode, RepositoryNode targetNode) {
        if (sourceNode == null) {
            return false;
        }

        // Cannot copy folder or system folder :
        if (sourceNode.getType() != ENodeType.REPOSITORY_ELEMENT) {
            return false;
        }
        IRepositoryViewObject objectToCopy = sourceNode.getObject();
        if (objectToCopy.getId() == null) {
            return false;
        }
        // TDI-18273:if the paste item in clipboard has been deleted physically from recycle bin,can not copy again.
        try {
            if (ProxyRepositoryFactory.getInstance().getLastVersion(objectToCopy.getId()) == null) {
                return false;
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }

        // Cannot move logically deleted objects :
        // try {
        if (objectToCopy != null && objectToCopy.getId() == null) {
            return false;
        }
        // Cannot copy for refProject
        // if (objectToCopy != null && factory.getStatus(objectToCopy) == ERepositoryStatus.READ_ONLY) {
        // return false;
        // }

        // if copied item has been deleted
        if (objectToCopy == null || objectToCopy.getRepositoryStatus() == ERepositoryStatus.DELETED
                || ProxyRepositoryFactory.getInstance().getStatus(sourceNode.getObject()) == ERepositoryStatus.DELETED) {
            return false;
        }

        // } catch (PersistenceException e) {
        // ExceptionHandler.process(e);
        // return false;
        // }

        // Cannot copy system routines:
        if (objectToCopy.getRepositoryObjectType() == ERepositoryObjectType.ROUTINES) {
            Property property = objectToCopy.getProperty();
            RoutineItem item = (RoutineItem) property.getItem();
            return !item.isBuiltIn();
        }
        // Cannot copy system sql pattern:
        if (objectToCopy.getRepositoryObjectType() == ERepositoryObjectType.SQLPATTERNS) {
            Property property = objectToCopy.getProperty();
            SQLPatternItem item = (SQLPatternItem) property.getItem();
            return !item.isSystem();
        }
        // for cdc
        if (PluginChecker.isCDCPluginLoaded()) {
            ICDCProviderService cdcService = (ICDCProviderService) GlobalServiceRegister.getDefault().getService(
                    ICDCProviderService.class);
            if (cdcService != null
                    && (cdcService.isSubscriberTableNode(sourceNode) || cdcService.isSystemSubscriberTable(sourceNode))) {
                return false;
            }
        }
        // Special rule : temp ?
        if (targetNode == null) {
            return true;
        }

        // for bug 0005454: Copy paste with keyboard in the repository view doesn't work.
        if (targetNode.getType() == ENodeType.REPOSITORY_ELEMENT || targetNode.getType() == ENodeType.SIMPLE_FOLDER
                || targetNode.getType() == ENodeType.SYSTEM_FOLDER) {
            return ((ERepositoryObjectType) targetNode.getProperties(EProperties.CONTENT_TYPE)) == objectToCopy
                    .getRepositoryObjectType();
        }
        return false;
    }

    public void execute(RepositoryNode sourceNode, RepositoryNode targetNode) throws Exception {
        if (!validateAction(sourceNode, targetNode)) {
            return;
        }

        // for bug 0005454: Copy paste with keyboard in the repository view doesn't work.
        if (targetNode.getType() == ENodeType.REPOSITORY_ELEMENT) {
            targetNode = targetNode.getParent();
        }

        final IPath path = RepositoryNodeUtilities.getPath(targetNode);

        if (sourceNode.getType().equals(ENodeType.REPOSITORY_ELEMENT)) {
            // Source is an repository element :
            // wzhang modified to fix bug 12349 and 11535
            final Item originalItem = sourceNode.getObject().getProperty().getItem();
            List<IRepositoryViewObject> allVersion = factory.getAllVersion(originalItem.getProperty().getId());

            if (allVersion.size() == 1) {
                copySingleVersionItem(originalItem, path);

            } else if (allVersion.size() > 1) {
                PastSelectorDialog dialog = new PastSelectorDialog(Display.getCurrent().getActiveShell(), allVersion, sourceNode);
                if (dialog.open() == Window.OK) {
                    final Set<IRepositoryViewObject> selectedVersionItems = dialog.getSelectedVersionItems();

                    //                        RepositoryWorkUnit<Object> workUnit = new RepositoryWorkUnit<Object>("", this) {//$NON-NLS-1$
                    //
                    // @Override
                    // protected void run() throws LoginException, PersistenceException {
                    // if (copy instanceof ProcessItem) {
                    // RelationshipItemBuilder.getInstance().addOrUpdateItem((ProcessItem) copy);
                    // }
                    // factory.save(copy);
                    // }
                    // };
                    // workUnit.setAvoidUnloadResources(true);
                    // factory.executeRepositoryWorkUnit(workUnit);

                    final IWorkspaceRunnable op = new IWorkspaceRunnable() {

                        public void run(IProgressMonitor monitor) throws CoreException {
                            String id = null;
                            String label = null;
                            boolean isfirst = true;
                            boolean needSys = true;
                            for (IRepositoryViewObject object : selectedVersionItems) {
                                Item selectedItem = object.getProperty().getItem();
                                try {
                                    final Item copy = factory.copy(selectedItem, path);
                                    if (isfirst) {
                                        id = copy.getProperty().getId();
                                        label = copy.getProperty().getLabel();
                                        isfirst = false;
                                    }
                                    copy.getProperty().setId(id);
                                    copy.getProperty().setLabel(label);
                                    // changed by hqzhang for TDI-19965
                                    copy.getProperty().setDisplayName(label);
                                    if (needSys && originalItem instanceof RoutineItem) {
                                        String lastestVersion = getLastestVersion(selectedVersionItems);
                                        if (lastestVersion.equals(copy.getProperty().getVersion())) {
                                            synDuplicatedRoutine((RoutineItem) copy);
                                            needSys = false;
                                        }
                                    }
                                    if (copy instanceof ProcessItem) {
                                        RelationshipItemBuilder.getInstance().addOrUpdateItem((ProcessItem) copy);
                                    }

                                    factory.save(copy);
                                } catch (PersistenceException e) {
                                    ExceptionHandler.process(e);
                                } catch (BusinessException e) {
                                    ExceptionHandler.process(e);
                                }
                            }
                        }

                    };

                    IRunnableWithProgress iRunnableWithProgress = new IRunnableWithProgress() {

                        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                            IWorkspace workspace = ResourcesPlugin.getWorkspace();
                            try {
                                ISchedulingRule schedulingRule = workspace.getRoot();
                                // the update the project files need to be done in the workspace runnable to avoid
                                // all
                                // notification
                                // of changes before the end of the modifications.
                                workspace.run(op, schedulingRule, IWorkspace.AVOID_UPDATE, monitor);
                            } catch (CoreException e) {
                                throw new InvocationTargetException(e);
                            }

                        }
                    };

                    try {
                        new ProgressMonitorDialog(null).run(true, true, iRunnableWithProgress);
                    } catch (InvocationTargetException e) {
                        ExceptionHandler.process(e);
                    } catch (InterruptedException e) {
                        //
                    }

                    // }

                }
            }
        }

    }

    private void copySingleVersionItem(final Item item, final IPath path) {
        final RepositoryWorkUnit<Object> workUnit = new RepositoryWorkUnit<Object>("", this) {//$NON-NLS-1$

            @Override
            protected void run() throws LoginException, PersistenceException {
                try {
                    Item newItem = factory.copy(item, path, true);
                    // qli modified to fix the bug 5400 and 6185.
                    if (newItem instanceof RoutineItem) {
                        synDuplicatedRoutine((RoutineItem) newItem);
                    }
                    ICamelDesignerCoreService service = null;
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                        service = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                                ICamelDesignerCoreService.class);
                    }
                    if (service != null && service.isInstanceofCamelBeans(item)) {
                        // for camel
                        synDuplicatedBean(newItem);
                    }
                    if (newItem instanceof ProcessItem || newItem instanceof JobletProcessItem) {
                        RelationshipItemBuilder.getInstance().addOrUpdateItem(newItem);
                    }
                    if (newItem instanceof ConnectionItem) {
                        ConnectionItem connectionItem = (ConnectionItem) newItem;
                        connectionItem.getConnection().getSupplierDependency().clear();
                    }
                    factory.save(newItem);
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        };

        workUnit.setAvoidUnloadResources(true);
        factory.executeRepositoryWorkUnit(workUnit);
    }

    private String getLastestVersion(Set<IRepositoryViewObject> set) {
        if (set.isEmpty()) {
            return null;
        }
        String version = null;
        for (IRepositoryViewObject obj : set) {
            String curVersion = obj.getProperty().getVersion();
            if (version == null) {
                version = curVersion;
            } else {
                Double dVersion = Double.valueOf(version);
                Double dCurVersion = Double.valueOf(curVersion);
                if (dCurVersion > dVersion) {
                    version = curVersion;
                }
            }
        }
        return version;
    }

    private void synDuplicatedRoutine(RoutineItem item) {
        ICodeGeneratorService codeGenService = (ICodeGeneratorService) GlobalServiceRegister.getDefault().getService(
                ICodeGeneratorService.class);
        if (codeGenService != null) {
            codeGenService.createRoutineSynchronizer().renameRoutineClass((RoutineItem) item);
            try {
                codeGenService.createRoutineSynchronizer().syncRoutine((RoutineItem) item, true);
            } catch (SystemException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    private void synDuplicatedBean(Item item) {
        ICodeGeneratorService codeGenService = (ICodeGeneratorService) GlobalServiceRegister.getDefault().getService(
                ICodeGeneratorService.class);
        if (codeGenService != null) {
            codeGenService.createCamelBeanSynchronizer().renameBeanClass(item);
            try {
                codeGenService.createCamelBeanSynchronizer().syncBean(item, true);
            } catch (SystemException e) {
                ExceptionHandler.process(e);
            }
        }
    }

}
