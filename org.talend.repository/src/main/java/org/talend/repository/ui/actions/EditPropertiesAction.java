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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.corext.refactoring.rename.JavaRenameProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameCompilationUnitProcessor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IESBService;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobScriptItem;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.IUIRefresher;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.editor.RepositoryEditorInput;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.views.IJobSettingsView;
import org.talend.repository.ui.views.IRepositoryView;
import org.talend.repository.ui.wizards.PropertiesWizard;

/**
 * smallet class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 * 
 */
public class EditPropertiesAction extends AContextualAction {

    public EditPropertiesAction() {
        super();
        setText(Messages.getString("EditPropertiesAction.action.title")); //$NON-NLS-1$
        setToolTipText(Messages.getString("EditPropertiesAction.action.toolTipText")); //$NON-NLS-1$
        setImageDescriptor(ImageProvider.getImageDesc(EImage.EDIT_ICON));
    }

    protected void doRun() {
        ISelection selection = getSelection();
        Object obj = ((IStructuredSelection) selection).getFirstElement();
        RepositoryNode node = (RepositoryNode) obj;
        // try {
        // ProxyRepositoryFactory.getInstance().initialize();
        // } catch (PersistenceException e1) {
        // ExceptionHandler.process(e1);
        // }

        IRepositoryViewObject object = node.getObject();
        if (getNeededVersion() != null && !object.getVersion().equals(getNeededVersion())) {
            try {
                object = ProxyRepositoryFactory.getInstance().getSpecificVersion(
                        new Project(ProjectManager.getInstance().getProject(object.getProperty().getItem())),
                        object.getProperty().getId(), getNeededVersion(), false);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        IPath path = RepositoryNodeUtilities.getPath(node);
        String originalName = object.getLabel();
        PropertiesWizard wizard = new PropertiesWizard(object, path, getNeededVersion() == null);
        WizardDialog dlg = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
        if (dlg.open() == Window.OK) {
            refresh(node);
            // refresh the corresponding editor's name
            IEditorPart part = getCorrespondingEditor(node);
            if (part != null && part instanceof IUIRefresher) {
                ((IUIRefresher) part).refreshName();
            } else {
                processRoutineRenameOperation(originalName, node, path);
            }
            // rename the job launch, for bug 8878
            IDesignerCoreService designerCoreService = RepositoryPlugin.getDefault().getDesignerCoreService();
            if (designerCoreService != null) {
                designerCoreService.renameJobLaunch(node.getObject(), originalName);
            }
            // refresh ...
            IViewPart jobSettingView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .findView(IJobSettingsView.ID);
            if (jobSettingView != null && jobSettingView instanceof IJobSettingsView) {
                ((IJobSettingsView) jobSettingView).refreshCurrentViewTab();
            }

            if (node.getObjectType() == ERepositoryObjectType.ROUTINES) {
                RepositoryManager.syncRoutineAndJoblet(ERepositoryObjectType.ROUTINES);
            }
            if (node.getObjectType().getType().equals("SERVICES")) {
                ConnectionItem connectionItem = (ConnectionItem) node.getObject().getProperty().getItem();
                RepositoryUpdateManager.updateServices(connectionItem);
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class)) {
                    IESBService service = (IESBService) GlobalServiceRegister.getDefault().getService(IESBService.class);
                    if (service != null) {
                        service.refreshComponentView(connectionItem);
                    }
                }
            }
        }
    }

    /**
     * delete the used routine java file if the routine is renamed. This method is added for solving bug 1321, only
     * supply to talend java version.
     * 
     * @param path
     * @param node
     * @param originalName
     */
    protected void processRoutineRenameOperation(String originalName, RepositoryNode node, IPath path) {
        if (LanguageManager.getCurrentLanguage() != ECodeLanguage.JAVA) {
            return;
        }

        if (node.getObjectType() != ERepositoryObjectType.ROUTINES) {
            return;
        }
        if (originalName.equals(node.getObject().getProperty().getLabel())) {
            return;
        }

        try {
            IJavaProject javaProject = CorePlugin.getDefault().getRunProcessService().getJavaProject();
            if (javaProject == null) {
                return;
            }

            IProject project = javaProject.getProject();
            IFolder srcFolder = project.getFolder(JavaUtils.JAVA_SRC_DIRECTORY);
            IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(srcFolder);

            // qli modified to fix the bug 5400 and 6185.
            IPackageFragment routinesPkg = root.getPackageFragment(JavaUtils.JAVA_ROUTINES_DIRECTORY); //$NON-NLS-1$

            ICompilationUnit unit = routinesPkg.getCompilationUnit(originalName + SuffixConstants.SUFFIX_STRING_java);
            if (unit == null) {
                return;
            }
            String newName = node.getObject().getProperty().getLabel();

            JavaRenameProcessor processor = new RenameCompilationUnitProcessor(unit);
            processor.setNewElementName(newName + SuffixConstants.SUFFIX_STRING_java);
            RenameRefactoring ref = new RenameRefactoring(processor);
            final PerformRefactoringOperation operation = new PerformRefactoringOperation(ref,
                    CheckConditionsOperation.ALL_CONDITIONS);

            IRunnableWithProgress r = new IRunnableWithProgress() {

                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    Display.getDefault().asyncExec(new Runnable() {

                        public void run() {
                            try {
                                operation.run(monitor);
                            } catch (CoreException e) {
                                ExceptionHandler.process(e);
                            }
                        }
                    });
                }
            };
            PlatformUI.getWorkbench().getProgressService().run(true, true, r);
            RefactoringStatus conditionStatus = operation.getConditionStatus();
            if (conditionStatus.hasError()) {
                String errorMessage = Messages.getString("EditPropertiesAction.renameError", unit.getElementName(), newName); //$NON-NLS-1$
                RefactoringStatusEntry[] entries = conditionStatus.getEntries();
                for (int i = 0; i < entries.length; i++) {
                    RefactoringStatusEntry entry = entries[i];
                    errorMessage += "\n>>>" + entry.getMessage(); //$NON-NLS-1$
                }
                Shell shell = null;
                IRepositoryView viewPart = getViewPart();
                if (viewPart != null) {
                    shell = viewPart.getViewSite().getShell();
                } else {
                    shell = Display.getCurrent().getActiveShell();
                }
                MessageDialog.openError(shell, Messages.getString("EditPropertiesAction.warning"), errorMessage); //$NON-NLS-1$
                return;
            }

            ICompilationUnit newUnit = routinesPkg.getCompilationUnit(newName + SuffixConstants.SUFFIX_STRING_java);
            if (newUnit == null) {
                return;
            }
            RoutineItem item = (RoutineItem) node.getObject().getProperty().getItem();
            IFile javaFile = (IFile) newUnit.getAdapter(IResource.class);
            try {
                ByteArray byteArray = item.getContent();
                byteArray.setInnerContentFromFile(javaFile);
                IRepositoryService service = CorePlugin.getDefault().getRepositoryService();
                IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();
                repFactory.save(item);
            } catch (Exception e) {
                // e.printStackTrace();
                ExceptionHandler.process(e);
            }

        } catch (Exception e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
    }

    /**
     * Find the editor that is related to the node.
     * 
     * @param node
     * @return
     */
    protected IEditorPart getCorrespondingEditor(RepositoryNode node) {
        IEditorReference[] eidtors = getActivePage().getEditorReferences();

        for (int i = 0; i < eidtors.length; i++) {
            try {
                IEditorInput input = eidtors[i].getEditorInput();
                if (!(input instanceof RepositoryEditorInput)) {
                    continue;
                }

                RepositoryEditorInput repositoryInput = (RepositoryEditorInput) input;
                if (repositoryInput.getItem().equals(node.getObject().getProperty().getItem())) {

                    IPath path = repositoryInput.getFile().getLocation();

                    return eidtors[i].getEditor(false);
                }
            } catch (PartInitException e) {
                continue;
            }
        }
        return null;
    }

    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = selection.size() == 1;
        if (canWork) {
            Object o = ((IStructuredSelection) selection).getFirstElement();
            if (o instanceof RepositoryNode) {
                RepositoryNode node = (RepositoryNode) o;
                switch (node.getType()) {
                case REPOSITORY_ELEMENT:
                    if (node.getObjectType() == ERepositoryObjectType.BUSINESS_PROCESS
                            || node.getObjectType() == ERepositoryObjectType.PROCESS) {
                        canWork = true;
                    } else if (node.getObjectType() == ERepositoryObjectType.ROUTINES) {
                        Item item = node.getObject().getProperty().getItem();
                        if (item instanceof RoutineItem) {
                            canWork = !((RoutineItem) item).isBuiltIn();
                        } else {
                            canWork = false;
                        }
                    } else if (node.getObjectType() == ERepositoryObjectType.SQLPATTERNS) {
                        Item item = node.getObject().getProperty().getItem();
                        if (item instanceof SQLPatternItem) {
                            canWork = !((SQLPatternItem) item).isSystem();
                        } else {
                            canWork = false;
                        }
                    } else if (node.getObjectType() == ERepositoryObjectType.JOB_SCRIPT) {
                        Item item = node.getObject().getProperty().getItem();
                        if (item instanceof JobScriptItem) {
                            canWork = true;
                        } else {
                            canWork = false;
                        }
                    } else {
                        canWork = false;
                    }
                    break;
                default:
                    canWork = false;
                    break;
                }
                if (canWork) {
                    canWork = (node.getObject().getRepositoryStatus() != ERepositoryStatus.DELETED);
                }
                if (canWork) {
                    canWork = isLastVersion(node);
                }
            }
        }
        setEnabled(canWork);
    }

}
