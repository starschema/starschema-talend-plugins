// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.wizards.newproject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.ui.images.ECoreImage;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.actions.importproject.ImportProjectsUtilities;
import org.talend.repository.ui.wizards.newproject.copyfromeclipse.TalendWizardProjectsImportPage;

/**
 * Wizard for the creation of a new project. <br/>
 * 
 * $Id: NewProjectWizard.java 1877 2007-02-06 17:16:43Z amaumont $
 * 
 */
public class ImportProjectAsWizard extends Wizard {

    /** Main page. */
    private ImportProjectAsWizardPage mainPage;

    private String name;

    private WizardProjectsImportPage manyProjectsPage;

    /**
     * Constructs a new NewProjectWizard.
     * 
     * @param author Project author.
     * @param server
     * @param password
     * @param port2
     */
    public ImportProjectAsWizard() {
        super();
        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.PROJECT_WIZ));
        setNeedsProgressMonitor(true);
        setDialogSettings(RepositoryPlugin.getDefault().getDialogSettings());
        getDialogSettings().put("WizardProjectsImportPage.STORE_COPY_PROJECT_ID", true); //$NON-NLS-1$
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        mainPage = new ImportProjectAsWizardPage();
        addPage(mainPage);

        manyProjectsPage = new TalendWizardProjectsImportPage();
        addPage(manyProjectsPage);

        setWindowTitle(Messages.getString("ImportProjectAsWizard.windowTitle")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/importproj_wiz.png")); //$NON-NLS-1$
    }

    public String getProjectName() {
        return name;
    }

    @Override
    public boolean canFinish() {
        return getContainer().getCurrentPage().isPageComplete();
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        if (getContainer().getCurrentPage().equals(manyProjectsPage)) {
            return manyProjectsPage.createProjects();
        } else {
            name = mainPage.getName();
            final String technicalName = mainPage.getTechnicalName();
            final String sourcePath = mainPage.getSourcePath();
            final boolean isArchive = mainPage.isArchive();

            // see bug 4600, update the external lib path, make it possible to
            // copy external jar files into tos
            updateExternalLibPath();

            WorkspaceModifyOperation op = new WorkspaceModifyOperation() {

                @Override
                protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        monitor.beginTask("", 1); //$NON-NLS-1$
                        if (monitor.isCanceled()) {
                            throw new OperationCanceledException();
                        }

                        if (!isArchive) {
                            ImportProjectsUtilities.importProjectAs(getShell(), name, technicalName, sourcePath,
                                    new SubProgressMonitor(monitor, 1));
                        } else {
                            try {
                                ImportProjectsUtilities.importArchiveProjectAs(getShell(), name, technicalName, sourcePath,
                                        new SubProgressMonitor(monitor, 1));
                            } catch (TarException e) {
                                throw new InvocationTargetException(e, Messages
                                        .getString("ImportProjectAsWizard.encouteringProblem")); //$NON-NLS-1$
                            } catch (IOException e) {
                                throw new InvocationTargetException(e, Messages
                                        .getString("ImportProjectAsWizard.encouteringProblem")); //$NON-NLS-1$
                            }
                        }

                    } finally {
                        monitor.done();
                    }
                }
            };

            // run the new project creation operation
            try {
                getContainer().run(false, true, op);
            } catch (InterruptedException e) {
                return false;
            } catch (InvocationTargetException e) {
                // one of the steps resulted in a core exception
                Throwable t = e.getTargetException();
                String message = Messages.getString("ImportProjectAsWizardPage.error.message"); //$NON-NLS-1$
                IStatus status;
                if (t instanceof CoreException) {
                    status = ((CoreException) t).getStatus();
                } else {
                    status = new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, 1, message, t);
                }
                ErrorDialog.openError(getShell(), message, null, status);
                // e.printStackTrace();
                ExceptionHandler.process(e);
                return false;
            }
            return true;

            // MessageBoxExceptionHandler.process(e, shell);
        }
    }

    /**
     * DOC hcw Comment method "updateExternalLibPath".
     */
    private void updateExternalLibPath() {
        String destinationJavaPath = CorePlugin.getDefault().getLibrariesService().getJavaLibrariesPath();
        String destinationPerlPath = CorePlugin.getDefault().getLibrariesService().getPerlLibrariesPath();

        IPathVariableManager pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
        try {
            pathVariableManager.setValue(EXTERNAL_LIB_JAVA_PATH, new Path(destinationJavaPath));
            pathVariableManager.setValue(EXTERNAL_LIB_PERL_PATH, new Path(destinationPerlPath));
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }

    }

    public final static String EXTERNAL_LIB_JAVA_PATH = "external_lib_java_path"; //$NON-NLS-1$

    public final static String EXTERNAL_LIB_PERL_PATH = "external_lib_perl_path"; //$NON-NLS-1$
}
