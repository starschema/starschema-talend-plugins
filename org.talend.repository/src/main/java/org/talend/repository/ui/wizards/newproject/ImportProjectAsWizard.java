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
package org.talend.repository.ui.wizards.newproject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.dialogs.EventLoopProgressMonitor;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage;
import org.osgi.framework.Bundle;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.dialogs.ProgressDialog;
import org.talend.core.CorePlugin;
import org.talend.core.prefs.PreferenceManipulator;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.actions.importproject.DemoProjectBean;
import org.talend.repository.ui.actions.importproject.EDemoProjectFileType;
import org.talend.repository.ui.actions.importproject.ImportProjectsUtilities;
import org.talend.repository.ui.wizards.newproject.copyfromeclipse.TalendWizardProjectsImportPage;
import org.talend.resources.ResourcesPlugin;

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
            name = mainPage.getName().trim().replace(' ', '_');
            final String technicalName = mainPage.getTechnicalName();
            final String sourcePath = mainPage.getSourcePath();
            final boolean isArchive = mainPage.isArchive();

            // see bug 4600, update the external lib path, make it possible to
            // copy external jar files into tos
            updateExternalLibPath();
            final Shell shell=getShell();
            ProgressDialog progressDialog = new ProgressDialog(shell) {

                private IProgressMonitor monitorWrap;

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitorWrap = new EventLoopProgressMonitor(monitor);

                    try {

                        if (!isArchive) {
                            ImportProjectsUtilities.importProjectAs(shell, name, technicalName, sourcePath, monitorWrap);
                        } else {// type.equalsIgnoreCase("archive")
                            ImportProjectsUtilities.importArchiveProjectAs(shell, name, technicalName, sourcePath, monitorWrap);

                        }


                    } catch (IOException e) {
                        throw new InvocationTargetException(e);
                    } catch (TarException e) {
                        throw new InvocationTargetException(e);
                    }
                    monitorWrap.done();
                    MessageDialog.openInformation(shell,
                            Messages.getString("ImportDemoProjectAction.messageDialogTitle.demoProject"), //$NON-NLS-1$
                            Messages.getString("ImportDemoProjectAction.messageDialogContent.demoProjectImportedSuccessfully")); //$NON-NLS-1$
                }
            };

            try {
                progressDialog.executeProcess();
            } catch (InvocationTargetException e) {
                MessageBoxExceptionHandler.process(e.getTargetException(), shell);
                return false;
            } catch (InterruptedException e) {
                // Nothing to do
                return false;
            }
            
            return true;
        }
    }

    /**
     * DOC hcw Comment method "updateExternalLibPath".
     */
    private void updateExternalLibPath() {
        // String destinationJavaPath = CorePlugin.getDefault().getLibrariesService().getJavaLibrariesPath();
        // String destinationPerlPath = CorePlugin.getDefault().getLibrariesService().getPerlLibrariesPath();

        // IPathVariableManager pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
        // try {
        // pathVariableManager.setValue(EXTERNAL_LIB_JAVA_PATH, new Path(destinationJavaPath));
        // pathVariableManager.setValue(EXTERNAL_LIB_PERL_PATH, new Path(destinationPerlPath));
        // } catch (CoreException e) {
        // ExceptionHandler.process(e);
        // }

    }

    public final static String EXTERNAL_LIB_JAVA_PATH = "external_lib_java_path"; //$NON-NLS-1$

    public final static String EXTERNAL_LIB_PERL_PATH = "external_lib_perl_path"; //$NON-NLS-1$
}
