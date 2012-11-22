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
package org.talend.core.model.repository;

import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.IComponentsService;
import org.talend.core.model.properties.BusinessProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.ui.IJobletProviderService;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.nodes.IProjectRepositoryNode;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * ggu class global comment. Detailled comment
 */
public final class RepositoryManager {

    public static final String PATTERNS_SEPARATOR = ","; //$NON-NLS-1$

    public static IPreferenceStore getPreferenceStore() {
        return CoreRuntimePlugin.getInstance().getDesignerCoreService().getDesignerCorePreferenceStore();
    }

    public static boolean isRefreshManually() {
        return getPreferenceStore().getBoolean(IRepositoryPrefConstants.MANUALLY_REFRESH);
    }

    public static boolean isRefreshCreated() {
        return getPreferenceStore().getBoolean(IRepositoryPrefConstants.CREATING_REFRESH);
    }

    public static boolean isRefreshSaved() {
        return getPreferenceStore().getBoolean(IRepositoryPrefConstants.SAVING_REFRESH);
    }

    public static boolean isRefreshDeleted() {
        return getPreferenceStore().getBoolean(IRepositoryPrefConstants.DELETING_REFRESH);
    }

    public static IRepositoryView getRepositoryView() {
        return RepositoryManagerHelper.getRepositoryView();
    }

    /**
     * 
     * for create
     */
    public static void refreshCreatedNode(ERepositoryObjectType type) {
        // if (isRefreshManually() || !isRefreshCreated()) {
        refresh(type);
        // } else {
        // IRepositoryView repositoryView = getRepositoryView();
        // if (repositoryView != null) {
        // repositoryView.refresh();
        // }
        // }
        // qli modified to fix the bug 6659.
        if (type != null) {
            syncRoutineAndJoblet(type);
        }
    }

    /**
     * 
     * for editorProperties
     */
    public static void refreshEditorPropertiesNode(ERepositoryObjectType type) {
        if (isRefreshManually() || !isRefreshCreated()) {
            refresh(type);
        } else {
            IRepositoryView repositoryView = getRepositoryView();
            if (repositoryView != null) {
                repositoryView.refresh();
            }
        }
        if (type != null) {
            syncRoutineAndJoblet(type);
        }
    }

    public static void refreshCreatedNode(IProjectRepositoryNode projectNode, ERepositoryObjectType type) {
        IRepositoryView repositoryView = getRepositoryView();
        if (repositoryView != null) {
            if ((isRefreshManually() || !isRefreshCreated()) && !type.isSubItem()) {
                if (projectNode != null) {
                    RepositoryNode rootNode = (RepositoryNode) projectNode.getRootRepositoryNode(type);
                    repositoryView.refreshAllChildNodes(rootNode);
                } else {
                    // main project
                    refresh(type);
                }
            } else {
                repositoryView.refresh();
            }
            // qli modified to fix the bug 6659.
            if (type != null) {
                syncRoutineAndJoblet(type);
            }
        }

    }

    /**
     * 
     * for delete
     */
    public static void refreshDeletedNode(Set<ERepositoryObjectType> types) {
        IRepositoryView repositoryView = getRepositoryView();
        if (repositoryView != null) {
            // if (isRefreshManually() || !isRefreshDeleted()) {
            //
            // RepositoryNode root = repositoryView.getRoot();
            // if (root != null && root instanceof IProjectRepositoryNode) {
            // RepositoryNode recBinNode = ((IProjectRepositoryNode) root).getRecBinNode();
            //
            // Set<ERepositoryObjectType> existedTypes = new HashSet<ERepositoryObjectType>();
            // for (RepositoryNode child : recBinNode.getChildren()) {
            // ERepositoryObjectType objectType = child.getObjectType();
            // if (objectType.isSubItem()) {
            // RepositoryNode parent = child.getParent();
            // if (parent.getObject() == null) { // for db connection
            // parent = parent.getParent();
            // }
            // existedTypes.add(parent.getObjectType());
            // }
            // }
            // repositoryView.refreshAllChildNodes(recBinNode);
            //
            // if (types != null) {
            // refresh(types);
            // existedTypes.removeAll(types);
            // }
            // refresh(existedTypes);
            //
            // repositoryView.refresh(recBinNode);
            // }
            //
            // } else {
            repositoryView.refresh();
            // }
        }
    }

    /**
     * 
     * for save
     */
    public static void refreshSavedNode(RepositoryNode node) {
        if (node == null) {
            return;
        }
        IRepositoryView repositoryView = getRepositoryView();
        if (repositoryView != null) {
            if (isRefreshManually() || !isRefreshSaved()) {
                repositoryView.refresh(node);
            } else {
                repositoryView.refresh();
            }
        }
    }

    /**
     * 
     * 
     */
    public static void refresh(ERepositoryObjectType type) {
        if (type != null) {
            IRepositoryView repositoryView = getRepositoryView();
            if (repositoryView != null) {
                repositoryView.refresh(type);
                repositoryView.refresh();
            }
        }
    }

    public static void refresh(Set<ERepositoryObjectType> types) {
        IRepositoryView repositoryView = getRepositoryView();
        if (types != null && repositoryView != null) {
            for (ERepositoryObjectType type : types) {
                repositoryView.refresh(type);
            }
        }
    }

    /**
     * 
     * qli Comment method "syncRoutineAndJoblet".
     * 
     * synchronize the routines and the joblets created by other users.
     */
    public static void syncRoutineAndJoblet(ERepositoryObjectType type) {
        if (type == null) {
            return;
        }
        if (type.equals(ERepositoryObjectType.ROUTINES)) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICodeGeneratorService.class)) {
                ICodeGeneratorService codeGenService = (ICodeGeneratorService) GlobalServiceRegister.getDefault().getService(
                        ICodeGeneratorService.class);
                try {
                    codeGenService.createRoutineSynchronizer().syncAllRoutines();
                } catch (SystemException e) {
                    ExceptionHandler.process(e);
                }
            }
        } else if (type.equals(ERepositoryObjectType.JOBLET)) {
            if (PluginChecker.isJobLetPluginLoaded()) {
                IJobletProviderService jobletService = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                        IJobletProviderService.class);
                if (jobletService != null) {
                    jobletService.loadComponentsFromProviders();
                }
            }
        } else {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                ICamelDesignerCoreService service = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                        ICamelDesignerCoreService.class);
                if (type.equals(service.getBeansType())) {
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(ICodeGeneratorService.class)) {
                        ICodeGeneratorService codeGenService = (ICodeGeneratorService) GlobalServiceRegister.getDefault()
                                .getService(ICodeGeneratorService.class);

                        try {
                            codeGenService.createCamelBeanSynchronizer().syncAllBeans();
                        } catch (SystemException e) {
                            ExceptionHandler.process(e);
                        }
                    }
                }
            }
        }
    }

    public static void syncUserComponents() {
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            IComponentsService codeGenService = (IComponentsService) GlobalServiceRegister.getDefault().getService(
                    IComponentsService.class);
            if (codeGenService != null) {
                codeGenService.getComponentsFactory().loadUserComponentsFromComponentsProviderExtension();
            }
        }

    }

    /**
     * 
     * ggu Comment method "isOpenedItemInEditor".
     * 
     * for jobs/joblets/business diagrams/routines/sql patterns
     */
    public static boolean isOpenedItemInEditor(IRepositoryViewObject objectToMove) {
        try {
            if (objectToMove != null) {
                IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (activeWorkbenchWindow != null) {
                    IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
                    if (activePage != null) {
                        IEditorReference[] editorReferences = activePage.getEditorReferences();
                        if (editorReferences != null) {
                            for (IEditorReference editorReference : editorReferences) {
                                IEditorInput editorInput = editorReference.getEditorInput();
                                if ((editorInput != null && editorInput instanceof IRepositoryEditorInput)) {
                                    IRepositoryEditorInput rInput = (IRepositoryEditorInput) editorInput;
                                    Property openedProperty = rInput.getItem().getProperty();
                                    if (openedProperty.getId().equals(objectToMove.getId())
                                            && VersionUtils.compareTo(openedProperty.getVersion(), objectToMove.getVersion()) == 0) {
                                        return true;
                                    }
                                } else if (objectToMove.getProperty().getItem() instanceof BusinessProcessItem) {
                                    Object obj = editorInput.getAdapter(IRepositoryEditorInput.class);
                                    if (obj instanceof IRepositoryEditorInput) {
                                        IRepositoryEditorInput rInput = (IRepositoryEditorInput) obj;
                                        Property openedProperty = rInput.getItem().getProperty();
                                        if (openedProperty.getId().equals(objectToMove.getId())
                                                && VersionUtils.compareTo(openedProperty.getVersion(), objectToMove.getVersion()) == 0) {
                                            return true;
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        } catch (PartInitException e) {
            ExceptionHandler.process(e);
        }
        return false;
    }

    /**
     * 
     * ggu Comment method "isEditableItemInEditor".
     * 
     * it's editable also.
     * 
     * for jobs/joblets/business diagrams/routines/sql patterns
     */
    public static boolean isEditableItemInEditor(IRepositoryViewObject objectToMove) {
        try {
            if (objectToMove != null) {
                IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (activeWorkbenchWindow != null) {
                    IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
                    if (activePage != null) {
                        IEditorReference[] editorReferences = activePage.getEditorReferences();
                        if (editorReferences != null) {
                            Property property = objectToMove.getProperty().getItem().getProperty();

                            for (IEditorReference editorReference : editorReferences) {
                                IEditorInput editorInput = editorReference.getEditorInput();
                                if (editorInput != null) {
                                    IRepositoryEditorInput rInput = null;
                                    // for business/routine/sql pattern
                                    IPersistableElement persistableElement = editorInput.getPersistable();
                                    if (persistableElement != null && persistableElement instanceof IRepositoryEditorInput) {
                                        rInput = (IRepositoryEditorInput) persistableElement;
                                    }
                                    // for job/joblet/routine/sql pattern
                                    if (editorInput instanceof IRepositoryEditorInput) {
                                        rInput = (IRepositoryEditorInput) editorInput;
                                    }
                                    if (rInput != null) {
                                        Property openedProperty = rInput.getItem().getProperty();
                                        if (openedProperty.getId().equals(property.getId())
                                                && VersionUtils.compareTo(openedProperty.getVersion(), property.getVersion()) == 0
                                                && !rInput.isReadOnly()) {

                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (PartInitException e) {
            ExceptionHandler.process(e);
        }
        return false;
    }

}
