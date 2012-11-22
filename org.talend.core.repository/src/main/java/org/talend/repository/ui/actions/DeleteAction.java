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
package org.talend.repository.ui.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.map.MultiKeyMap;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ICoreService;
import org.talend.core.IESBService;
import org.talend.core.ITDQRepositoryService;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.SubscriberTable;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.FolderType;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.ProjectReference;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.TDQItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.Folder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.repository.i18n.Messages;
import org.talend.core.repository.model.ISubRepositoryObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.core.repository.utils.AbstractResourceChangesService;
import org.talend.core.repository.utils.RepositoryNodeDeleteManager;
import org.talend.core.repository.utils.TDQServiceRegister;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.cwm.helper.SubItemHelper;
import org.talend.designer.business.diagram.custom.IDiagramModelService;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.expressionbuilder.ExpressionPersistance;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.ContextReferenceBean;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.ItemReferenceBean;
import org.talend.repository.model.JobletReferenceBean;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.dialog.ContextReferenceDialog;
import org.talend.repository.ui.dialog.ItemReferenceDialog;

/**
 * Action used to delete object from repository. This action manages logical and physical deletions.<br/>
 * 
 * $Id: DeleteAction.java 85660 2012-06-15 08:09:39Z ycbai $
 * 
 */
public class DeleteAction extends AContextualAction {

    private static DeleteAction singleton;

    private static final String DELETE_LOGICAL_TITLE = Messages.getString("DeleteAction.action.logicalTitle"); //$NON-NLS-1$

    private static final String DELETE_FOREVER_TITLE = Messages.getString("DeleteAction.action.foreverTitle"); //$NON-NLS-1$

    private static final String DELETE_LOGICAL_TOOLTIP = Messages.getString("DeleteAction.action.logicalToolTipText"); //$NON-NLS-1$

    private static final String DELETE_FOREVER_TOOLTIP = Messages.getString("DeleteAction.action.logicalToolTipText"); //$NON-NLS-1$

    public DeleteAction() {
        super();
        setId(ActionFactory.DELETE.getId());
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.DELETE_ICON));
        //        this.setActionDefinitionId("deleteItem"); //$NON-NLS-1$
        singleton = this;

        // for restore, unload after, not before, since the state will change (item was normal, and change to "deleted")
        this.setUnloadResourcesAfter(true);
        this.setAvoidUnloadResources(true);
    }

    public static DeleteAction getInstance() {
        return singleton;
    }

    boolean needToUpdataPalette = false;

    boolean confirmAssignDialog = false;

    @Override
    protected void doRun() {
        final ISelection selection = getSelection();
        final IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        final DeleteActionCache deleteActionCache = DeleteActionCache.getInstance();
        deleteActionCache.setGetAlways(false);
        deleteActionCache.setDocRefresh(false);
        deleteActionCache.createRecords();

        final Set<ERepositoryObjectType> types = new HashSet<ERepositoryObjectType>();
        final List<RepositoryNode> deletedFolder = new ArrayList<RepositoryNode>();
        final IWorkspaceRunnable op = new IWorkspaceRunnable() {

            public void run(IProgressMonitor monitor) {
                monitor.beginTask("Delete Running", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                Object[] selections = ((IStructuredSelection) selection).toArray();
                List<RepositoryNode> selectNodes = new ArrayList<RepositoryNode>();
                for (Object obj : selections) {
                    if (obj instanceof RepositoryNode) {
                        selectNodes.add((RepositoryNode) obj);
                    }
                }
                final List<ItemReferenceBean> unDeleteItems = RepositoryNodeDeleteManager.getInstance().getUnDeleteItems(
                        selectNodes, deleteActionCache);
                for (RepositoryNode node : selectNodes) {
                    try {
                        // ADD xqliu 2012-05-24 TDQ-4831
                        if (sourceFileOpening(node)) {
                            continue;
                        }
                        // ~ TDQ-4831
                        if (containParent(node, (IStructuredSelection) selection)) {
                            continue;
                        }

                        if (isForbidNode(node)) {
                            continue;
                        }
                        if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
                            if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class)) {
                                IESBService service = (IESBService) GlobalServiceRegister.getDefault().getService(
                                        IESBService.class);
                                Item repoItem = node.getObject().getProperty().getItem();
                                if (service != null && !repoItem.getState().isDeleted()) {
                                    final StringBuffer jobNames = service.getAllTheJObNames(node);
                                    if (jobNames != null) {
                                        Display.getDefault().syncExec(new Runnable() {

                                            public void run() {
                                                String message = jobNames.toString()
                                                        + Messages.getString("DeleteAction.deleteJobAssignedToOneService"); //$NON-NLS-1$
                                                final Shell shell = getShell();
                                                confirmAssignDialog = MessageDialog.openQuestion(shell, "", message); //$NON-NLS-1$

                                            }
                                        });
                                        if (!confirmAssignDialog) {
                                            continue;
                                        }
                                    }
                                }
                            }

                            if (isInDeletedFolder(deletedFolder, node.getParent())) {
                                continue;
                            }

                            boolean needReturn = deleteElements(factory, deleteActionCache, node);
                            if (node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.JOBLET) {
                                needToUpdataPalette = true;
                            }
                            if (needReturn) {
                                return;
                            }
                            types.add(node.getObjectType());
                        } else if (node.getType() == ENodeType.SIMPLE_FOLDER) {
                            FolderItem folderItem = (FolderItem) node.getObject().getProperty().getItem();
                            if (node.getChildren().size() > 0 && !folderItem.getState().isDeleted()) {
                                if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class)) {
                                    IESBService service = (IESBService) GlobalServiceRegister.getDefault().getService(
                                            IESBService.class);
                                    if (service != null) {
                                        final StringBuffer jobNames = service.getAllTheJObNames(node);
                                        if (jobNames != null) {
                                            Display.getDefault().syncExec(new Runnable() {

                                                public void run() {
                                                    String message = null;
                                                    if (jobNames.toString().contains(",")) { //$NON-NLS-1$
                                                        message = jobNames.toString()
                                                                + Messages
                                                                        .getString("DeleteAction.deleteSomeJobsAssignedToServices"); //$NON-NLS-1$
                                                    } else {
                                                        message = jobNames.toString()
                                                                + Messages
                                                                        .getString("DeleteAction.deleteJobAssignedToOneService"); //$NON-NLS-1$
                                                    }
                                                    final Shell shell = getShell();
                                                    confirmAssignDialog = MessageDialog.openQuestion(shell, "", message); //$NON-NLS-1$

                                                }
                                            });
                                            if (!confirmAssignDialog) {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            // bug 18158
                            boolean isSqlTemplate = false;
                            if (node.getObject() instanceof Folder) {
                                // isSqlTemplate = ((Folder) node.getObject()).getContentType().equals(
                                // ERepositoryObjectType.SQLPATTERNS);

                                Object label = node.getProperties(EProperties.LABEL);
                                if (ENodeType.SIMPLE_FOLDER.equals(node.getType())
                                        && ERepositoryObjectType.SQLPATTERNS.equals(node.getContentType())
                                        && (label.equals("Generic") || label.equals("UserDefined") || label.equals("MySQL") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                                || label.equals("Netezza") || label.equals("Oracle") //$NON-NLS-1$ //$NON-NLS-2$
                                                || label.equals("ParAccel") || label.equals("Teradata")) //$NON-NLS-1$ //$NON-NLS-2$
                                        || label.equals("Hive")) { //$NON-NLS-1$
                                    isSqlTemplate = true;

                                }
                            }
                            if (!isSqlTemplate) {
                                types.add(node.getContentType());
                                // fixed for the documentation deleted
                                if (node.getContentType() == ERepositoryObjectType.PROCESS
                                        || node.getContentType() == ERepositoryObjectType.JOBLET) {
                                    types.add(ERepositoryObjectType.DOCUMENTATION);
                                }
                                deletedFolder.add(node);
                                deleteFolder(node, factory, deleteActionCache);
                            }
                        }
                    } catch (PersistenceException e) {
                        MessageBoxExceptionHandler.process(e);
                    } catch (BusinessException e) {
                        MessageBoxExceptionHandler.process(e);
                    }
                }
                if (unDeleteItems.size() > 0) {
                    Display.getDefault().syncExec(new Runnable() {

                        public void run() {
                            ItemReferenceDialog dialog = new ItemReferenceDialog(PlatformUI.getWorkbench()
                                    .getActiveWorkbenchWindow().getShell(), unDeleteItems);
                            dialog.open();
                        }
                    });
                }
                try {
                    factory.saveProject(ProjectManager.getInstance().getCurrentProject());
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }

            /**
             * DOC xqliu Comment method "sourceFileOpening".
             * 
             * @param node
             * @return
             */
            private boolean sourceFileOpening(RepositoryNode node) {
                boolean result = false;
                if (node != null) {
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(ITDQRepositoryService.class)) {
                        ITDQRepositoryService service = (ITDQRepositoryService) GlobalServiceRegister.getDefault().getService(
                                ITDQRepositoryService.class);
                        if (service != null) {
                            result = service.sourceFileOpening(node);
                        }
                    }
                }
                return result;
            }
        };

        IRunnableWithProgress iRunnableWithProgress = new IRunnableWithProgress() {

            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                try {
                    ISchedulingRule schedulingRule = workspace.getRoot();
                    // the update the project files need to be done in the workspace runnable to avoid all
                    // notification
                    // of changes before the end of the modifications.
                    workspace.run(op, schedulingRule, IWorkspace.AVOID_UPDATE, monitor);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                }

            }
        };

        try {
            PlatformUI.getWorkbench().getProgressService().run(false, false, iRunnableWithProgress);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        final boolean updatePalette = needToUpdataPalette;
        Display.getCurrent().syncExec(new Runnable() {

            public void run() {
                // MOD qiongli 2011-1-24,avoid to refresh repositoryView for top
                if (!org.talend.commons.utils.platform.PluginChecker.isOnlyTopLoaded()) {
                    if (updatePalette && GlobalServiceRegister.getDefault().isServiceRegistered(ICoreService.class)) {
                        ICoreService service = (ICoreService) GlobalServiceRegister.getDefault().getService(ICoreService.class);
                        service.updatePalette();
                    }

                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    for (IEditorReference editors : page.getEditorReferences()) {
                        if (GlobalServiceRegister.getDefault().isServiceRegistered(IDiagramModelService.class)) {
                            IDiagramModelService service = (IDiagramModelService) GlobalServiceRegister.getDefault().getService(
                                    IDiagramModelService.class);
                            service.refreshBusinessModel(editors);
                        }
                    }
                }
                deleteActionCache.revertParameters();
            }
        });

    }

    private boolean isInDeletedFolder(List<RepositoryNode> folderList, RepositoryNode node) {
        for (RepositoryNode folder : folderList) {
            if (node == folder) {
                return true;
            }
        }

        return false;
    }

    /**
     * DOC qwei Comment method "deleteFolder".
     * 
     * @param deleteActionCache
     */
    private void deleteFolder(final RepositoryNode node, final IProxyRepositoryFactory factory,
            final DeleteActionCache deleteActionCache) {
        FolderItem folderItem = (FolderItem) node.getObject().getProperty().getItem();
        if (folderItem.getState().isDeleted()) {
            // if folder has been deleted already
            try {
                deleteElements(factory, deleteActionCache, node);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
            return;
        }
        IPath path = RepositoryNodeUtilities.getPath(node);
        ERepositoryObjectType objectType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
        List<IRepositoryNode> repositoryList = node.getChildren();
        boolean success = true;
        for (IRepositoryNode repositoryNode : repositoryList) {
            try {
                deleteRepositoryNode(repositoryNode, factory);
            } catch (Exception e) {
                ExceptionHandler.process(e);
                success = false;
            }
        }
        if (!success) {
            return;
        }

        folderItem = factory.getFolderItem(ProjectManager.getInstance().getCurrentProject(), objectType, path);
        folderItem.getState().setDeleted(true);

        String fullPath = ""; //$NON-NLS-1$
        FolderItem curItem = folderItem;

        while (curItem.getParent() instanceof FolderItem && ((Item) curItem.getParent()).getParent() instanceof FolderItem
                && ((FolderItem) ((Item) curItem.getParent()).getParent()).getType().getValue() == FolderType.FOLDER) {
            FolderItem parentFolder = (FolderItem) curItem.getParent();
            if ("".equals(fullPath)) { //$NON-NLS-1$
                fullPath = parentFolder.getProperty().getLabel() + fullPath;
            } else {
                fullPath = parentFolder.getProperty().getLabel() + "/" + fullPath; //$NON-NLS-1$
            }
            curItem = parentFolder;
        }
        if (!objectType.getKey().toString().startsWith("repository.metadata") && objectType != ERepositoryObjectType.SQLPATTERNS //$NON-NLS-1$
                && objectType != ERepositoryObjectType.ROUTINES && objectType != ERepositoryObjectType.JOB_SCRIPT
                && curItem.getParent() instanceof FolderItem && ((Item) curItem.getParent()).getParent() instanceof FolderItem
                && !objectType.isDQItemType()) {// MOD qiongli 2011-1-20 except DQItem.
            FolderItem parentFolder = (FolderItem) curItem.getParent();
            if ("".equals(fullPath)) { //$NON-NLS-1$
                fullPath = parentFolder.getProperty().getLabel() + fullPath;
            } else {
                fullPath = parentFolder.getProperty().getLabel() + "/" + fullPath; //$NON-NLS-1$
            }
            curItem = parentFolder;
        }
        if (objectType.getKey().toString().startsWith("repository.metadata")) { //$NON-NLS-1$
            while (((FolderItem) curItem.getParent()).getType().getValue() != FolderType.SYSTEM_FOLDER) {
                if ("".equals(fullPath)) { //$NON-NLS-1$
                    fullPath = ((FolderItem) curItem.getParent()).getProperty().getLabel() + fullPath;
                } else {
                    fullPath = ((FolderItem) curItem.getParent()).getProperty().getLabel() + "/" + fullPath; //$NON-NLS-1$
                }
                curItem = (FolderItem) curItem.getParent();
            }
        }
        if (objectType == ERepositoryObjectType.ROUTINES) {
            while (((FolderItem) curItem.getParent()).getType().getValue() != FolderType.SYSTEM_FOLDER) {
                if ("".equals(fullPath)) { //$NON-NLS-1$
                    fullPath = ((FolderItem) curItem.getParent()).getProperty().getLabel() + fullPath;
                } else {
                    fullPath = ((FolderItem) curItem.getParent()).getProperty().getLabel() + "/" + fullPath; //$NON-NLS-1$
                }
                curItem = (FolderItem) curItem.getParent();
            }
        }

        if (objectType == ERepositoryObjectType.JOB_SCRIPT) {
            while (((FolderItem) curItem.getParent()).getType().getValue() != FolderType.SYSTEM_FOLDER) {
                if ("".equals(fullPath)) { //$NON-NLS-1$
                    fullPath = ((FolderItem) curItem.getParent()).getProperty().getLabel() + fullPath;
                } else {
                    fullPath = ((FolderItem) curItem.getParent()).getProperty().getLabel() + "/" + fullPath; //$NON-NLS-1$
                }
                curItem = (FolderItem) curItem.getParent();
            }
        }

        if (objectType == ERepositoryObjectType.SQLPATTERNS) {
            while (((FolderItem) curItem.getParent()).getType().getValue() != FolderType.SYSTEM_FOLDER) {
                if ("".equals(fullPath)) { //$NON-NLS-1$
                    fullPath = ((FolderItem) curItem.getParent()).getProperty().getLabel() + fullPath;
                } else {
                    fullPath = ((FolderItem) curItem.getParent()).getProperty().getLabel() + "/" + fullPath; //$NON-NLS-1$
                }
                curItem = (FolderItem) curItem.getParent();
            }
            while (!((FolderItem) curItem.getParent()).getProperty().getLabel().equals("sqlPatterns")) { //$NON-NLS-1$
                fullPath = ((FolderItem) curItem.getParent()).getProperty().getLabel() + "/" + fullPath; //$NON-NLS-1$
                curItem = (FolderItem) curItem.getParent();
            }
        }
        // Add this 'if' by qiongli 2011-1-19,handle DQItem
        if (objectType.isDQItemType()) {

            while (((FolderItem) curItem.getParent()).getType().getValue() != FolderType.SYSTEM_FOLDER) {
                if ("".equals(fullPath)) { //$NON-NLS-1$
                    fullPath = ((FolderItem) curItem.getParent()).getProperty().getLabel() + fullPath;
                } else {
                    fullPath = ((FolderItem) curItem.getParent()).getProperty().getLabel() + "/" + fullPath; //$NON-NLS-1$
                }
                curItem = (FolderItem) curItem.getParent();
            }

        }
        folderItem.getState().setPath(fullPath);
        this.setChildFolderPath(folderItem);
    }

    private void setChildFolderPath(FolderItem folderItem) {
        EList childFoderList = folderItem.getChildren();
        for (Object o : childFoderList) {
            if (o instanceof FolderItem) {
                String parentPath = ((FolderItem) ((FolderItem) o).getParent()).getState().getPath();
                String parentName = ((FolderItem) ((FolderItem) o).getParent()).getProperty().getLabel();
                ((FolderItem) o).getState().setPath(parentPath + File.separator + parentName);
                setChildFolderPath((FolderItem) o);
            }
        }
    }

    private void deleteRepositoryNode(IRepositoryNode repositoryNode, IProxyRepositoryFactory factory)
            throws PersistenceException, BusinessException {
        if (repositoryNode.getType() == ENodeType.SIMPLE_FOLDER) {
            IPath path = RepositoryNodeUtilities.getPath((RepositoryNode) repositoryNode);
            ERepositoryObjectType objectType = (ERepositoryObjectType) repositoryNode.getProperties(EProperties.CONTENT_TYPE);
            List<IRepositoryNode> repositoryList = repositoryNode.getChildren();
            PersistenceException pex = null;
            BusinessException bex = null;
            for (IRepositoryNode repositoryNode2 : repositoryList) {
                try {
                    deleteRepositoryNode(repositoryNode2, factory);
                } catch (PersistenceException e) {
                    pex = e;
                } catch (BusinessException e) {
                    bex = e;
                }
            }
            if (pex != null) {
                throw pex;
            }
            if (bex != null) {
                throw bex;
            }

            FolderItem folderItem = factory.getFolderItem(ProjectManager.getInstance().getCurrentProject(), objectType, path);
            folderItem.getState().setDeleted(true);

            String fullPath = ""; //$NON-NLS-1$
            FolderItem curItem = folderItem;
            while (curItem.getParent() instanceof FolderItem && ((Item) curItem.getParent()).getParent() instanceof FolderItem) {
                FolderItem parentFolder = (FolderItem) curItem.getParent();
                if ("".equals(fullPath)) { //$NON-NLS-1$
                    fullPath = parentFolder.getProperty().getLabel() + fullPath;
                } else {
                    fullPath = parentFolder.getProperty().getLabel() + "/" + fullPath; //$NON-NLS-1$
                }
                curItem = parentFolder;
            }
            folderItem.getState().setPath(fullPath);

        } else {
            IRepositoryViewObject objToDelete = repositoryNode.getObject();
            factory.deleteObjectLogical(objToDelete);
        }
    }

    /**
     * DOC qzhang Comment method "checkRepository".
     * 
     * @param factory
     * @param currentJobNode
     * @return
     */

    public static IEditorReference[] getEditors() {
        final List<IEditorReference> list = new ArrayList<IEditorReference>();
        Display.getDefault().syncExec(new Runnable() {

            public void run() {
                IEditorReference[] reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .getEditorReferences();
                list.addAll(Arrays.asList(reference));
            }
        });
        return list.toArray(new IEditorReference[0]);
    }

    private static boolean isOpenedItem(Item openedItem, MultiKeyMap openProcessMap) {
        if (openedItem == null) {
            return false;
        }
        Property property = openedItem.getProperty();
        return (openProcessMap.get(property.getId(), property.getLabel(), property.getVersion()) != null);
    }

    /**
     * 
     * wzhang Comment method "calcParentProjects".
     * 
     * @param curProject
     * @param parentProject
     * @param refParentProjects
     * @return
     */
    private static boolean calcParentProjects(Project curProject, Project parentProject, Set<Project> refParentProjects) {
        boolean found = false;
        if (curProject != null && parentProject != null) {
            Context ctx = CoreRuntimePlugin.getInstance().getContext();
            if (ctx == null) {
                return false;
            }
            RepositoryContext repositoryContext = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
            String parentBranch = repositoryContext.getFields().get(
                    IProxyRepositoryFactory.BRANCH_SELECTION + "_" + parentProject.getTechnicalLabel()); //$NON-NLS-1$

            EList referencedProjects = parentProject.getEmfProject().getReferencedProjects();
            for (ProjectReference pRef : (List<ProjectReference>) referencedProjects) {
                if (pRef.getBranch() != null && !parentBranch.equals(pRef.getBranch())) {
                    continue;
                }
                final String technicalLabel = pRef.getReferencedProject().getTechnicalLabel();
                if (technicalLabel != null) {
                    final Project project = new Project(pRef.getReferencedProject());
                    final Project paProject = new Project(pRef.getProject());
                    if (technicalLabel.equals(curProject.getTechnicalLabel())
                            || calcParentProjects(curProject, project, refParentProjects)) {
                        found = true;
                        if (!refParentProjects.contains(project)) {
                            refParentProjects.add(project);
                        }
                        if (!refParentProjects.contains(paProject)) {
                            refParentProjects.add(paProject);
                        }
                    }
                }
            }
        }
        return found;
    }

    public static List<ContextReferenceBean> checkContextFromProcess(IProxyRepositoryFactory factory,
            DeleteActionCache deleteActionCache, RepositoryNode currentJobNode) {
        IRepositoryViewObject object = currentJobNode.getObject();
        Item nodeItem = object.getProperty().getItem();
        boolean contextIsUsed = false;
        if (nodeItem instanceof ContextItem) {
            contextIsUsed = true;
        }
        // List<RelationshipItemBuilder.Relation> relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(
        // nodeItem.getProperty().getId(), RelationshipItemBuilder.LATEST_VERSION,
        // RelationshipItemBuilder.CONTEXT_RELATION);

        List<ContextReferenceBean> list = new ArrayList<ContextReferenceBean>();

        if (deleteActionCache == null) {
            deleteActionCache = DeleteActionCache.getInstance();
            deleteActionCache.createRecords();
        }
        if (object != null && contextIsUsed) {
            Property property = object.getProperty();
            if (property != null) {
                String label = property.getLabel();
                String version = property.getVersion();
                Item item = property.getItem();
                if (!(item instanceof ContextItem)) {
                    return list;
                }

                Set<Project> refParentProjects = new HashSet<Project>();
                try {
                    refParentProjects.add(ProjectManager.getInstance().getCurrentProject());
                    refParentProjects.addAll(ProjectManager.getInstance().getReferencedProjects());
                    for (Project refP : refParentProjects) {
                        List<IRepositoryViewObject> objList = new ArrayList<IRepositoryViewObject>();
                        List<IRepositoryViewObject> processes = factory.getAll(refP, ERepositoryObjectType.PROCESS);
                        List<IRepositoryViewObject> jobletes = factory.getAll(refP, ERepositoryObjectType.JOBLET);
                        processes.addAll(jobletes);
                        deleteActionCache.setProcessList(processes);
                        objList.addAll(processes);

                        List<IRepositoryViewObject> connectionc = factory
                                .getAll(refP, ERepositoryObjectType.METADATA_CONNECTIONS);
                        objList.addAll(connectionc);
                        List<IRepositoryViewObject> edifact = factory.getAll(refP, ERepositoryObjectType.METADATA_EDIFACT);
                        objList.addAll(edifact);
                        List<IRepositoryViewObject> brms = factory.getAll(refP, ERepositoryObjectType.METADATA_FILE_BRMS);
                        objList.addAll(brms);
                        List<IRepositoryViewObject> delis = factory.getAll(refP, ERepositoryObjectType.METADATA_FILE_DELIMITED);
                        objList.addAll(delis);
                        List<IRepositoryViewObject> ebcdic = factory.getAll(refP, ERepositoryObjectType.METADATA_FILE_EBCDIC);
                        objList.addAll(ebcdic);
                        List<IRepositoryViewObject> excel = factory.getAll(refP, ERepositoryObjectType.METADATA_FILE_EXCEL);
                        objList.addAll(excel);
                        List<IRepositoryViewObject> ftp = factory.getAll(refP, ERepositoryObjectType.METADATA_FILE_FTP);
                        objList.addAll(ftp);
                        List<IRepositoryViewObject> hl7 = factory.getAll(refP, ERepositoryObjectType.METADATA_FILE_HL7);
                        objList.addAll(hl7);
                        List<IRepositoryViewObject> ldif = factory.getAll(refP, ERepositoryObjectType.METADATA_FILE_LDIF);
                        objList.addAll(ldif);
                        List<IRepositoryViewObject> positional = factory.getAll(refP,
                                ERepositoryObjectType.METADATA_FILE_POSITIONAL);
                        objList.addAll(positional);
                        List<IRepositoryViewObject> regexp = factory.getAll(refP, ERepositoryObjectType.METADATA_FILE_REGEXP);
                        objList.addAll(regexp);
                        List<IRepositoryViewObject> xmls = factory.getAll(refP, ERepositoryObjectType.METADATA_FILE_XML);
                        objList.addAll(xmls);
                        List<IRepositoryViewObject> mdms = factory.getAll(refP, ERepositoryObjectType.METADATA_MDMCONNECTION);
                        objList.addAll(mdms);
                        List<IRepositoryViewObject> wsdl = factory.getAll(refP, ERepositoryObjectType.METADATA_WSDL_SCHEMA);
                        objList.addAll(wsdl);
                        List<IRepositoryViewObject> saleForces = factory.getAll(refP,
                                ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA);
                        objList.addAll(saleForces);

                        for (IRepositoryViewObject process : objList) {
                            Property property2 = process.getProperty();

                            boolean isDelete = factory.getStatus(process) == ERepositoryStatus.DELETED;
                            boolean isJob = true;

                            Item item2 = property2.getItem();
                            if (item == item2) {
                                continue;
                            }
                            List<IContext> contextList = null;
                            String contextID = null;
                            if (!isOpenedItem(item2, deleteActionCache.getOpenProcessMap())) {
                                IDesignerCoreService service = (IDesignerCoreService) GlobalServiceRegister.getDefault()
                                        .getService(IDesignerCoreService.class);
                                if (item2 instanceof ProcessItem) {
                                    contextList = service.getProcessFromProcessItem((ProcessItem) item2).getContextManager()
                                            .getListContext();
                                } else if (item2 instanceof JobletProcessItem) {
                                    contextList = service.getProcessFromJobletProcessItem((JobletProcessItem) item2)
                                            .getContextManager().getListContext();
                                } else if (item2 instanceof ConnectionItem) {
                                    contextID = ((ConnectionItem) item2).getConnection().getContextId();
                                }
                            }
                            if (contextList != null) {
                                // isExtensionComponent(node);
                                for (IContext context : contextList) {
                                    if (context.getContextParameterList().size() <= 0) {
                                        continue;
                                    }
                                    String source = context.getContextParameterList().get(0).getSource();
                                    if (source.equals(item.getProperty().getId())) {
                                        String path = item2.getState().getPath();
                                        String type = process.getRepositoryObjectType().getType();
                                        ContextReferenceBean bean = new ContextReferenceBean(property2.getLabel(), type,
                                                property2.getVersion(), path, refP.getLabel());
                                        bean.setJobFlag(isJob, isDelete);
                                        list.add(bean);
                                        break;
                                    }
                                }
                            } else if (contextID != null) {
                                if (contextID.equals(item.getProperty().getId())) {
                                    String path = item2.getState().getPath();
                                    String type = process.getRepositoryObjectType().getType();
                                    ContextReferenceBean bean = new ContextReferenceBean(property2.getLabel(), type,
                                            property2.getVersion(), path, refP.getLabel());
                                    bean.setJobFlag(isJob, isDelete);
                                    list.add(bean);
                                    break;
                                }
                            }
                        }
                        for (IProcess2 openedProcess : deleteActionCache.getOpenedProcessList()) {
                            List<IContext> contextList = openedProcess.getContextManager().getListContext();
                            for (IContext context : contextList) {
                                if (context.getContextParameterList().size() <= 0) {
                                    continue;
                                }
                                String source = context.getContextParameterList().get(0).getSource();
                                if (source.equals(item.getProperty().getId())) {
                                    boolean isDelete = factory.getStatus(openedProcess) == ERepositoryStatus.DELETED;
                                    boolean isJob = true;
                                    Property property2 = openedProcess.getProperty();
                                    Item item2 = property2.getItem();
                                    String path = item2.getState().getPath();

                                    ContextReferenceBean bean = new ContextReferenceBean(property2.getLabel(), openedProcess
                                            .getRepositoryObjectType().getType(), property2.getVersion(), path, refP.getLabel());
                                    bean.setJobFlag(isJob, isDelete);
                                    list.add(bean);
                                    break;
                                }
                            }
                        }

                    }

                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }

            }

        }

        return list;
    }

    @Deprecated
    public static List<JobletReferenceBean> checkRepositoryNodeFromProcess(IProxyRepositoryFactory factory,
            DeleteActionCache deleteActionCache, RepositoryNode currentJobNode) {
        IRepositoryViewObject object = currentJobNode.getObject();
        Item nodeItem = object.getProperty().getItem(); // hywang add
        boolean needCheckJobletIfUsedInProcess = false;
        if (nodeItem instanceof JobletProcessItem) {
            needCheckJobletIfUsedInProcess = true;
        }
        List<JobletReferenceBean> list = new ArrayList<JobletReferenceBean>();

        if (deleteActionCache == null) {
            deleteActionCache = DeleteActionCache.getInstance();
            deleteActionCache.createRecords();
        }
        if (object != null && needCheckJobletIfUsedInProcess) {
            Property property = object.getProperty();
            if (property != null) {
                String label = property.getLabel();
                String version = property.getVersion();
                Item item = property.getItem();
                if (!(item instanceof JobletProcessItem)) {
                    return list;
                }
                EList nodesList = null;
                // wzhang added to fix bug 10050
                Set<Project> refParentProjects = new HashSet<Project>();
                try {
                    refParentProjects.add(ProjectManager.getInstance().getCurrentProject());
                    refParentProjects.addAll(ProjectManager.getInstance().getReferencedProjects());
                    // if (currentProject != null) {
                    // final Project[] readProject = factory.readProject();
                    // for (Project p : readProject) {
                    // if (p.equals(currentProject)) {
                    // continue;
                    // }
                    // calcParentProjects(currentProject, p, refParentProjects);
                    // }
                    // refParentProjects.add(currentProject); // contain current project
                    // }
                    for (Project refP : refParentProjects) {
                        List<IRepositoryViewObject> processes = factory.getAll(refP, ERepositoryObjectType.PROCESS);
                        List<IRepositoryViewObject> jobletes = factory.getAll(refP, ERepositoryObjectType.JOBLET);
                        processes.addAll(jobletes);
                        deleteActionCache.setProcessList(processes);
                        for (IRepositoryViewObject process : deleteActionCache.getProcessList()) {
                            // node = (EList) process.getGraphicalNodes();item

                            Property property2 = process.getProperty();

                            boolean isDelete = factory.getStatus(process) == ERepositoryStatus.DELETED;
                            boolean isJob = true;

                            Item item2 = property2.getItem();
                            if (item == item2) {
                                continue;
                            }
                            if (!isOpenedItem(item2, deleteActionCache.getOpenProcessMap())) {
                                if (item2 instanceof ProcessItem) {
                                    nodesList = ((ProcessItem) item2).getProcess().getNode();
                                } else if (item2 instanceof JobletProcessItem) {
                                    nodesList = ((JobletProcessItem) item2).getJobletProcess().getNode();
                                }
                            }
                            if (nodesList != null) {
                                // isExtensionComponent(node);
                                for (Object object2 : nodesList) {
                                    if (object2 instanceof NodeType) {
                                        NodeType nodeType = (NodeType) object2;
                                        nodeType.getElementParameter();
                                        boolean equals = nodeType.getComponentName().equals(label);
                                        // && nodeType.getComponentVersion().equals(version);for bug 14212
                                        if (equals) {
                                            String path = item2.getState().getPath();

                                            boolean found = false;
                                            JobletReferenceBean bean = new JobletReferenceBean(property2.getLabel(),
                                                    property2.getVersion(), path, refP.getLabel());
                                            bean.setJobFlag(isJob, isDelete);

                                            for (JobletReferenceBean b : list) {
                                                if (b.toString().equals(bean.toString())) {
                                                    found = true;
                                                    b.addNodeNum();
                                                    break;
                                                }
                                            }
                                            if (!found) {
                                                list.add(bean);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        for (IProcess2 openedProcess : deleteActionCache.getOpenedProcessList()) {
                            for (INode node : openedProcess.getGraphicalNodes()) {
                                boolean equals = node.getComponent().getName().equals(label);
                                // && node.getComponent().getVersion().equals(version);for bug 14212
                                boolean isDelete = factory.getStatus(openedProcess) == ERepositoryStatus.DELETED;
                                boolean isJob = true;
                                Property property2 = openedProcess.getProperty();
                                Item item2 = property2.getItem();
                                String path = item2.getState().getPath();

                                if (equals) {

                                    boolean found = false;
                                    JobletReferenceBean bean = new JobletReferenceBean(property2.getLabel(),
                                            property2.getVersion(), path, refP.getLabel());
                                    bean.setJobFlag(isJob, isDelete);

                                    for (JobletReferenceBean b : list) {
                                        if (b.toString().equals(bean.toString())) {
                                            found = true;
                                            b.addNodeNum();
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        list.add(bean);
                                    }
                                }

                            }
                        }

                    }

                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }

            }

        }

        return list;
    }

    /**
     * ftang Comment method "isForbbidNode".
     * 
     * @param node
     * @return
     */
    private boolean isForbidNode(RepositoryNode node) {

        IRepositoryViewObject nodeObject = node.getObject();
        // Avoid to delete node which is locked.
        if (nodeObject != null
                && nodeObject.getProperty() != null
                && nodeObject.getProperty().getItem() != null
                && (nodeObject.getRepositoryStatus() == ERepositoryStatus.LOCK_BY_OTHER
                        || nodeObject.getRepositoryStatus() == ERepositoryStatus.LOCK_BY_USER || RepositoryManager
                        .isOpenedItemInEditor(nodeObject)) && !(DELETE_FOREVER_TITLE.equals(getText()))) {

            final String title = Messages.getString("DeleteAction.error.title"); //$NON-NLS-1$
            String nodeName = ERepositoryObjectType.getDeleteFolderName(nodeObject.getRepositoryObjectType());
            final String message = Messages.getString("DeleteAction.error.lockedOrOpenedObject.newMessage", nodeName);//$NON-NLS-1$
            Display.getDefault().syncExec(new Runnable() {

                public void run() {
                    MessageDialog dialog = new MessageDialog(new Shell(), title, null, message, MessageDialog.ERROR,
                            new String[] { IDialogConstants.OK_LABEL }, 0);//$NON-NLS-1$
                    dialog.open();
                }
            });
            return true;
        }

        // Avoid to delete all related documentation node by click Key "Delete" from keyboard.
        if (node.getContentType() == ERepositoryObjectType.JOB_DOC) {
            return true;
        }

        if (node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.JOB_DOC) {
            return true;
        }

        if (node.getContentType() == ERepositoryObjectType.JOBLET_DOC) {
            return true;
        }

        if (node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.JOBLET_DOC) {
            return true;
        }

        if (node.getContentType() == ERepositoryObjectType.JOBS) {
            return true;
        }
        if (node.getContentType() == ERepositoryObjectType.GENERATED) {
            return true;
        }
        if (node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_CDC) {
            return true;
        }
        if (node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_TABLE) {
            final IRepositoryViewObject object = nodeObject;
            if (object != null && object instanceof MetadataTableRepositoryObject) {
                final MetadataTable table = ((MetadataTableRepositoryObject) object).getTable();
                if (table != null && table instanceof SubscriberTable) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean deleteElements(IProxyRepositoryFactory factory, DeleteActionCache deleteActionCache,
            RepositoryNode currentJobNode) throws PersistenceException, BusinessException {
        return deleteElements(factory, deleteActionCache, currentJobNode, null);
    }

    protected boolean confirmFromDialog = false;

    private boolean deleteElements(IProxyRepositoryFactory factory, DeleteActionCache deleteActionCache,
            final RepositoryNode currentJobNode, Boolean confirm) throws PersistenceException, BusinessException {
        boolean needReturn = false;
        final IRepositoryViewObject objToDelete = currentJobNode.getObject();

        final List<ContextReferenceBean> checkContext = checkContextFromProcess(factory, deleteActionCache, currentJobNode);
        if (checkContext.size() > 0) {
            Display.getDefault().syncExec(new Runnable() {

                public void run() {
                    ContextReferenceDialog dialog = new ContextReferenceDialog(PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow().getShell(), objToDelete, checkContext);
                    dialog.open();
                }
            });
            return true;
        }

        Item item = objToDelete.getProperty().getItem();
        AbstractResourceChangesService resChangeService = TDQServiceRegister.getInstance().getResourceChangeService(
                AbstractResourceChangesService.class);
        if (resChangeService != null && item instanceof ConnectionItem && item.getState().isDeleted()) {
            if (!resChangeService.handleResourceChange(((ConnectionItem) item).getConnection())) {
                return true;
            }
        }

        // To manage case of we have a subitem. This is possible using 'DEL' shortcut:
        ERepositoryObjectType nodeType = (ERepositoryObjectType) currentJobNode.getProperties(EProperties.CONTENT_TYPE);
        if (nodeType != null && nodeType.isSubItem()) {
            Display.getDefault().syncExec(new Runnable() {

                public void run() {
                    final DeleteTableAction deleteTableAction = new DeleteTableAction();
                    deleteTableAction.setWorkbenchPart(getWorkbenchPart());
                    deleteTableAction.run();
                }
            });
            needReturn = true;
        } else {
            if (factory.getStatus(objToDelete) == ERepositoryStatus.DELETED) {
                if (confirm == null) {
                    Display.getDefault().syncExec(new Runnable() {

                        public void run() {
                            String title = Messages.getString("DeleteAction.dialog.title"); //$NON-NLS-1$

                            String message = currentJobNode.getProperties(EProperties.LABEL)
                                    + " " + Messages.getString("DeleteAction.dialog.message0") + "\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    + Messages.getString("DeleteAction.dialog.message2"); //$NON-NLS-1$

                            confirmFromDialog = MessageDialog.openQuestion(new Shell(), title, message);
                        }
                    });
                    confirm = confirmFromDialog;
                }
                if (confirm) {

                    deleteActionCache.closeOpenedEditor(objToDelete);
                    if (currentJobNode.getType() == ENodeType.SIMPLE_FOLDER) {
                        boolean success = true;
                        for (IRepositoryNode curNode : currentJobNode.getChildren()) {
                            try {
                                deleteElements(factory, deleteActionCache, (RepositoryNode) curNode, confirm);
                            } catch (Exception e) {
                                ExceptionHandler.process(e);
                                success = false;
                            }
                        }
                        if (success) {
                            if (currentJobNode.getObject() != null && currentJobNode.getObject().getProperty() != null
                                    && currentJobNode.getObject().getProperty().getItem() != null) {
                                Item fitem = currentJobNode.getObject().getProperty().getItem();
                                if ((fitem instanceof FolderItem)
                                        && (((FolderItem) fitem).getType().getValue() == FolderType.FOLDER)) {
                                    factory.deleteFolder(
                                            currentJobNode.getContentType(),
                                            RepositoryNodeUtilities.getFolderPath(currentJobNode.getObject().getProperty()
                                                    .getItem()));
                                } else {
                                    factory.deleteFolder(currentJobNode.getContentType(),
                                            RepositoryNodeUtilities.getPath(currentJobNode));
                                }
                            } else {
                                factory.deleteFolder(currentJobNode.getContentType(),
                                        RepositoryNodeUtilities.getPath(currentJobNode));
                            }
                        }
                    } else {
                        // MOD qiongli 2011-5-10,bug 21189.should remove dependency after showing the question dialog of
                        // physical delete.
                        if (resChangeService != null && (item instanceof TDQItem || item instanceof ConnectionItem)) {
                            resChangeService.removeAllDependecies(item);
                        }
                        // MOD qiongli 2012-3-30 remove SQL Explore only when it is confirmed to delete.
                        if (item instanceof ConnectionItem
                                && GlobalServiceRegister.getDefault().isServiceRegistered(ITDQRepositoryService.class)) {
                            ITDQRepositoryService tdqRepService = (ITDQRepositoryService) GlobalServiceRegister.getDefault()
                                    .getService(ITDQRepositoryService.class);
                            tdqRepService.removeAliasInSQLExplorer(currentJobNode);
                        }
                        factory.deleteObjectPhysical(objToDelete);
                        ExpressionPersistance.getInstance().jobDeleted(objToDelete.getLabel());
                    }
                }
            } else {
                factory.deleteObjectLogical(objToDelete);
            }
        }

        return needReturn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        visible = !selection.isEmpty();
        if (selection.isEmpty()) {
            setEnabled(false);
            return;
        }

        boolean enabled = true;
        this.setText(null);
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            visible = false;
        }
        for (Object o : (selection).toArray()) {
            if (visible) {
                RepositoryNode node = (RepositoryNode) o;
                if (!ProjectManager.getInstance().isInCurrentMainProject(node)) {
                    visible = false;
                    break;
                }
                switch (node.getType()) {
                case STABLE_SYSTEM_FOLDER:
                    visible = false;
                case SYSTEM_FOLDER:
                    visible = false;
                    break;
                case SIMPLE_FOLDER:
                    Object obj = node.getProperties(EProperties.LABEL);
                    String label = null;
                    IRepositoryViewObject folderObj = node.getObject();
                    ERepositoryStatus statusFolder = folderObj.getRepositoryStatus();
                    boolean isDeletedFolder = statusFolder == ERepositoryStatus.DELETED;
                    if (obj instanceof String) {
                        label = (String) obj;
                    }
                    if (node.getContentType() == ERepositoryObjectType.JOB_DOC
                            || node.getContentType() == ERepositoryObjectType.JOBLET_DOC
                            || RepositoryConstants.USER_DEFINED.equals(label)) {
                        visible = false;
                    } else {
                        if (isDeletedFolder) {
                            this.setText(DELETE_FOREVER_TITLE);
                            this.setToolTipText(DELETE_FOREVER_TOOLTIP);
                        } else {
                            this.setText(DELETE_LOGICAL_TITLE);
                            this.setToolTipText(DELETE_LOGICAL_TOOLTIP);
                        }

                        if (node.hasChildren()) {
                            visible = true;
                            enabled = true;
                        }
                    }

                    // 1. the select node should belong to the SQL Patterns
                    // 2. the select node is the father node of the SQL Patterns
                    // 3. the select node do not has father node(means do not contain "/")
                    String selectName = selection.getFirstElement().toString();
                    if (node.getContentType() == ERepositoryObjectType.SQLPATTERNS && selectName.equals(label)
                            && !selectName.contains("/")) { //$NON-NLS-1$
                        visible = false;
                    }
                    break;
                case REPOSITORY_ELEMENT:
                    Object contentType = node.getProperties(EProperties.CONTENT_TYPE);
                    if (contentType == ERepositoryObjectType.JOB_DOC || contentType == ERepositoryObjectType.JOBLET_DOC) {
                        visible = false;
                        break;
                    }
                    if (contentType == ERepositoryObjectType.METADATA_CON_CDC) {
                        enabled = false;
                        visible = false;
                        break;
                    }

                    if (contentType == ERepositoryObjectType.SERVICESOPERATION) {
                        enabled = false;
                        visible = false;
                        break;
                    }

                    if (contentType == ERepositoryObjectType.SERVICESPORT) {
                        enabled = false;
                        visible = false;
                        break;
                    }

                    IRepositoryViewObject repObj = node.getObject();

                    ERepositoryStatus status = repObj.getRepositoryStatus();
                    boolean isEditable = status.isPotentiallyEditable() || status.isEditable();
                    boolean isDeleted = status == ERepositoryStatus.DELETED;
                    ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);

                    if (nodeType.isSubItem() && repObj instanceof ISubRepositoryObject) {
                        ISubRepositoryObject subRepositoryObject = (ISubRepositoryObject) repObj;
                        isDeleted = SubItemHelper.isDeleted(subRepositoryObject.getAbstractMetadataObject());
                    }

                    if (isDeleted) {
                        if (ERepositoryObjectType.METADATA_CON_TABLE.equals(nodeType)) {
                            visible = false;
                            break;
                        }

                        if (ERepositoryObjectType.METADATA_CON_COLUMN.equals(nodeType)) {
                            visible = false;
                            break;
                        }

                        if (ERepositoryObjectType.METADATA_CON_QUERY.equals(nodeType)) {
                            visible = false;
                            break;
                        }

                        if (getText() == null || DELETE_FOREVER_TITLE.equals(getText())) {
                            this.setText(DELETE_FOREVER_TITLE);
                            this.setToolTipText(DELETE_FOREVER_TOOLTIP);
                        } else {
                            visible = false;
                        }
                    } else {
                        ERepositoryObjectType repositoryObjectType = repObj.getRepositoryObjectType();
                        if (repositoryObjectType == ERepositoryObjectType.METADATA_CON_TABLE
                                || repositoryObjectType == ERepositoryObjectType.METADATA_CON_QUERY
                                || repositoryObjectType == ERepositoryObjectType.METADATA_CON_COLUMN) {
                            visible = false;
                        } else {
                            if (getText() == null || DELETE_LOGICAL_TITLE.equals(getText())) {
                                this.setText(DELETE_LOGICAL_TITLE);
                                this.setToolTipText(DELETE_LOGICAL_TOOLTIP);

                                if (!isEditable) {
                                    visible = true;
                                    enabled = false;
                                }
                            } else {
                                visible = false;
                            }
                        }
                    }
                    break;
                default:
                    // Nothing to do
                    break;
                }
            }
        }
        setEnabled(enabled);
    }

    private boolean visible;

    /**
     * Getter for visible.
     * 
     * @return the visible
     */
    @Override
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * Sets the visible.
     * 
     * @param visible the visible to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    private boolean containParent(RepositoryNode node, IStructuredSelection selection) {
        for (Object o : (selection).toArray()) {
            RepositoryNode parent = (RepositoryNode) o;
            if (node.getParent() != null && node.getParent().equals(parent)) {
                return true;
            }
        }
        return false;
    }

    protected Shell getShell() {
        Shell shell = null;

        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow != null) {
            shell = activeWorkbenchWindow.getShell();
        }
        if (shell == null) {
            Display dis = Display.getCurrent();
            if (dis == null) {
                dis = Display.getDefault();
            }
            if (dis != null) {
                shell = dis.getShells()[0];
            }
        }
        if (shell == null) {
            shell = new Shell();
        }
        return shell;
    }

}
