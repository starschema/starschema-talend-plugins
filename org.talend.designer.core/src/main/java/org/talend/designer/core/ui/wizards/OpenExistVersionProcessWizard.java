// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.properties.BusinessProcessItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.codegen.ISQLPatternSynchronizer;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.MultiPageTalendEditor;
import org.talend.designer.core.ui.editor.ProcessEditorInput;
import org.talend.expressionbuilder.ExpressionPersistance;
import org.talend.repository.ProjectManager;
import org.talend.repository.editor.RepositoryEditorInput;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.actions.routines.RoutineEditorInput;

/**
 * DOC xye class global comment. Detailled comment
 */
public class OpenExistVersionProcessWizard extends Wizard {

    OpenExistVersionProcessPage mainPage = null;

    private final IRepositoryViewObject processObject;

    private boolean alreadyEditedByUser = false;

    private String originaleObjectLabel = null;

    private String originalVersion = null;

    public OpenExistVersionProcessWizard(IRepositoryViewObject processObject) {
        this.processObject = processObject;
        originaleObjectLabel = processObject.getProperty().getLabel();
        originalVersion = processObject.getProperty().getVersion();

        ERepositoryStatus status = processObject.getRepositoryStatus();
        if (status == ERepositoryStatus.LOCK_BY_OTHER || status.equals(ERepositoryStatus.LOCK_BY_USER)
                && RepositoryManager.isOpenedItemInEditor(processObject)) {
            alreadyEditedByUser = true;
        }
    }

    @Override
    public void addPages() {
        mainPage = new OpenExistVersionProcessPage(alreadyEditedByUser, processObject);
        addPage(mainPage);
        setWindowTitle(Messages.getString("NewProcessWizard.windowTitle")); //$NON-NLS-1$
    }

    /**
     * Returns the currently active page for this workbench window.
     * 
     * @return the active page, or <code>null</code> if none
     */
    public IWorkbenchPage getActivePage() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    }

    @Override
    public boolean performCancel() {
        if (!getProperty().getVersion().equals(getOriginVersion()))
            restoreVersion();
        return super.performCancel();
    }

    private void lockObject(IRepositoryViewObject object) {
        IProxyRepositoryFactory repositoryFactory = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
        try {
            repositoryFactory.lock(object);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        } catch (BusinessException e) {
            ExceptionHandler.process(e);
        }
    }

    private void unlockObject() {
        IProxyRepositoryFactory repositoryFactory = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
        try {
            if (repositoryFactory.getStatus(processObject).equals(ERepositoryStatus.LOCK_BY_USER)) {
                repositoryFactory.unlock(processObject);
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        } catch (LoginException e) {
            ExceptionHandler.process(e);
        }
        RepositoryManager.refreshCreatedNode(ERepositoryObjectType.PROCESS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        if (mainPage.isCreateNewVersionJob()) {
            if (!alreadyEditedByUser) {
                refreshNewJob();
                try {
                    ProxyRepositoryFactory.getInstance().saveProject(ProjectManager.getInstance().getCurrentProject());
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }

            try {
                Item newCreated = null;
                if (processObject.getProperty() != null && processObject.getProperty().getItem() != null) {
                    newCreated = processObject.getProperty().getItem();
                }
                if (!(newCreated instanceof BusinessProcessItem)) {
                    ProxyRepositoryFactory.getInstance().lock(processObject);
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            } catch (LoginException e) {
                ExceptionHandler.process(e);
            }

            boolean locked = processObject.getRepositoryStatus().equals(ERepositoryStatus.LOCK_BY_USER);
            openAnotherVersion((RepositoryNode) processObject.getRepositoryNode(), !locked);
            try {
                ProxyRepositoryFactory.getInstance().saveProject(ProjectManager.getInstance().getCurrentProject());
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        } else {
            StructuredSelection selection = (StructuredSelection) mainPage.getSelection();
            RepositoryNode node = (RepositoryNode) selection.getFirstElement();
            boolean lastVersion = node.getObject().getVersion().equals(processObject.getVersion());
            processObject.getProperty().setVersion(originalVersion);
            if (lastVersion) {
                lockObject(processObject);
            }
            ERepositoryStatus status = node.getObject().getRepositoryStatus();
            boolean isLocked = false;
            if (status == ERepositoryStatus.LOCK_BY_USER) {
                isLocked = true;
            }

            // Only latest version can be editted
            openAnotherVersion(node, !lastVersion || !isLocked);
        }
        return true;
    }

    private boolean refreshNewJob() {
        if (alreadyEditedByUser) {
            return false;
        }
        IProxyRepositoryFactory repositoryFactory = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
        try {
            repositoryFactory.save(getProperty(), this.originaleObjectLabel, this.originalVersion);
            ExpressionPersistance.getInstance().jobNameChanged(originaleObjectLabel, processObject.getLabel());
            ProxyRepositoryFactory.getInstance().saveProject(ProjectManager.getInstance().getCurrentProject());
            return true;
        } catch (PersistenceException e) {
            MessageBoxExceptionHandler.process(e);
            return false;
        }
    }

    private void openAnotherVersion(final RepositoryNode node, final boolean readonly) {
        try {
            if (node.getObject() != null) {
                Item item = node.getObject().getProperty().getItem();
                IWorkbenchPage page = getActivePage();
                IEditorPart editorPart = null;
                RepositoryEditorInput fileEditorInput = null;
                ICodeGeneratorService codeGenService = (ICodeGeneratorService) GlobalServiceRegister.getDefault().getService(
                        ICodeGeneratorService.class);

                if (item instanceof ProcessItem) {
                    ProcessItem processItem = (ProcessItem) item;
                    fileEditorInput = new ProcessEditorInput(processItem, true, false, readonly);

                } else if (item instanceof BusinessProcessItem) {
                    BusinessProcessItem businessProcessItem = (BusinessProcessItem) item;
                    IFile file = CorePlugin.getDefault().getDiagramModelService()
                            .getDiagramFileAndUpdateResource(page, businessProcessItem);
                    fileEditorInput = new RepositoryEditorInput(file, businessProcessItem);
                } else if (item instanceof RoutineItem) {
                    RoutineItem routineItem = (RoutineItem) item;
                    ITalendSynchronizer routineSynchronizer = codeGenService.createRoutineSynchronizer();
                    IFile file = null;
                    ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                    String lastVersion = factory.getLastVersion(routineItem.getProperty().getId()).getVersion();
                    String curVersion = routineItem.getProperty().getVersion();
                    if (curVersion != null && curVersion.equals(lastVersion)) {
                        file = routineSynchronizer.getFile(routineItem);
                    } else {
                        file = routineSynchronizer.getRoutinesFile(routineItem);
                    }
                    fileEditorInput = new RoutineEditorInput(file, routineItem);
                } else if (item instanceof SQLPatternItem) {
                    SQLPatternItem patternItem = (SQLPatternItem) item;
                    ISQLPatternSynchronizer SQLPatternSynchronizer = codeGenService.getSQLPatternSynchronizer();
                    SQLPatternSynchronizer.syncSQLPattern(patternItem, true);
                    IFile file = SQLPatternSynchronizer.getSQLPatternFile(patternItem);
                    fileEditorInput = new RepositoryEditorInput(file, patternItem);
                }

                editorPart = page.findEditor(fileEditorInput);
                if (editorPart == null) {
                    // fileEditorInput.setView(getViewPart());
                    fileEditorInput.setRepositoryNode(node);
                    if (item instanceof ProcessItem) {
                        page.openEditor(fileEditorInput, MultiPageTalendEditor.ID, readonly);

                    } else if (item instanceof BusinessProcessItem) {
                        CorePlugin.getDefault().getDiagramModelService().openBusinessDiagramEditor(page, fileEditorInput);
                    } else {
                        ECodeLanguage lang = ((RepositoryContext) CorePlugin.getContext().getProperty(
                                Context.REPOSITORY_CONTEXT_KEY)).getProject().getLanguage();
                        String talendEditorID = "org.talend.designer.core.ui.editor.StandAloneTalend" + lang.getCaseName() + "Editor"; //$NON-NLS-1$ //$NON-NLS-2$
                        page.openEditor(fileEditorInput, talendEditorID);
                    }
                } else {
                    page.activate(editorPart);
                }
            }
        } catch (PartInitException e) {
            MessageBoxExceptionHandler.process(e);
        } catch (PersistenceException e) {
            MessageBoxExceptionHandler.process(e);
        } catch (SystemException e) {
            MessageBoxExceptionHandler.process(e);
        }
    }

    private Property getProperty() {
        return processObject.getProperty();
    }

    public String getOriginVersion() {
        return this.originalVersion;
    }

    public void restoreVersion() {
        getProperty().setVersion(getOriginVersion());
    }

}
