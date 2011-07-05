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
package org.talend.repository.ui.actions.importproject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.dialogs.EventLoopProgressMonitor;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.osgi.framework.Bundle;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.dialogs.ProgressDialog;
import org.talend.core.CorePlugin;
import org.talend.core.model.general.Project;
import org.talend.core.prefs.PreferenceManipulator;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.ERepositoryImages;
import org.talend.resources.ResourcesPlugin;

/**
 * Action used to refresh a repository view.<br/>
 * 
 * $Id: RefreshAction.java 824 2006-12-01 15:49:55 +0000 (ven., 01 déc. 2006) smallet $
 * 
 */
public final class ImportDemoProjectAction extends Action {

    public static final String DEMO_ALREADY_IMPORTED = "demoProjectAlreadyImported"; //$NON-NLS-1$

    private static final String ACTION_TITLE = Messages.getString("ImportDemoProjectAction.actionTitle"); //$NON-NLS-1$

    private static final String ACTION_TOOLTIP = Messages.getString("ImportDemoProjectAction.actionTooltip"); //$NON-NLS-1$

    private String lastImportedName;

    private static ImportDemoProjectAction singleton;

    public static ImportDemoProjectAction getInstance() {
        if (singleton == null) {
            singleton = new ImportDemoProjectAction();
        }
        return singleton;
    }

    private Shell shell;

    private Project[] projects;

    private ImportDemoProjectAction() {
        super();
        this.setText(ACTION_TITLE);
        this.setToolTipText(ACTION_TOOLTIP);
        this.setImageDescriptor(ImageProvider.getImageDesc(ERepositoryImages.IMPORT_PROJECTS_ACTION));
    }

    @Override
    public void run() {

        final List<DemoProjectBean> demoProjectList = ImportProjectsUtilities.getAllDemoProjects();

        ImportDemoProjectWizard demoProjectWizard = new ImportDemoProjectWizard(demoProjectList);

        WizardDialog dialog = new WizardDialog(shell, demoProjectWizard);
        if (dialog.open() != 1 && demoProjectWizard.getSelectedDemoProjectIndex() != Integer.MAX_VALUE) {
            final int selectedDemoProjectIndex = demoProjectWizard.getSelectedDemoProjectIndex();

            ProgressDialog progressDialog = new ProgressDialog(shell) {

                private IProgressMonitor monitorWrap;

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitorWrap = new EventLoopProgressMonitor(monitor);

                    try {
                        DemoProjectBean demoProjectBean = demoProjectList.get(selectedDemoProjectIndex);
                        String techName = demoProjectBean.getProjectName();

                        if (checkProjectIsExisting(techName)) {
                            boolean reImportFlag = MessageDialog.openQuestion(shell,
                                    Messages.getString("ImportDemoProjectAction.alertDialog.messageTitle"), Messages //$NON-NLS-1$
                                            .getString("ImportDemoProjectAction.alertDialog.message")); //$NON-NLS-1$
                            if (!reImportFlag) {
                                return;
                            }
                        }

                        String demoFilePath = demoProjectBean.getDemoProjectFilePath();
                        EDemoProjectFileType demoProjectFileType = demoProjectBean.getDemoProjectFileType();
                        String pluginID = ResourcesPlugin.PLUGIN_ID;
                        if (techName.equals("TALENDDEMOSPERL")) { //$NON-NLS-1$
                            pluginID = "org.talend.resources.perl"; //$NON-NLS-1$
                        } else if (techName.equals("TDQEEDEMOJAVA")) { //$NON-NLS-1$
                            pluginID = "org.talend.datacleansing.core.ui"; //$NON-NLS-1$
                        }
                        Bundle bundle = Platform.getBundle(pluginID);

                        URL url = FileLocator.resolve(bundle.getEntry(demoFilePath));

                        String filePath = new Path(url.getFile()).toOSString();

                        if (demoProjectFileType.getName().equalsIgnoreCase("folder")) { //$NON-NLS-1$
                            ImportProjectsUtilities.importProjectAs(shell, techName, techName, filePath, monitorWrap);
                        } else {// type.equalsIgnoreCase("archive")
                            ImportProjectsUtilities.importArchiveProject(shell, techName, filePath, monitorWrap);

                        }
                        lastImportedName = techName;

                    } catch (IOException e) {
                        throw new InvocationTargetException(e);
                    } catch (TarException e) {
                        throw new InvocationTargetException(e);
                    }

                    monitorWrap.done();
                    MessageDialog.openInformation(shell,
                            Messages.getString("ImportDemoProjectAction.messageDialogTitle.demoProject"), //$NON-NLS-1$
                            Messages.getString("ImportDemoProjectAction.messageDialogContent.demoProjectImportedSuccessfully")); //$NON-NLS-1$
                    PreferenceManipulator prefManipulator = new PreferenceManipulator(CorePlugin.getDefault()
                            .getPreferenceStore());
                    prefManipulator.setValue(DEMO_ALREADY_IMPORTED, true);
                }
            };

            try {
                progressDialog.executeProcess();
            } catch (InvocationTargetException e) {
                MessageBoxExceptionHandler.process(e.getTargetException(), shell);
            } catch (InterruptedException e) {
                // Nothing to do
            }
        }
    }

    public String getProjectName() {
        return lastImportedName;
    }

    public void setShell(Shell shell) {
        this.shell = shell;
    }

    public void setExistingProjects(Project[] projects) {
        this.projects = projects;
    }

    private boolean checkProjectIsExisting(String techName) {
        if (this.projects == null || this.projects.length == 0) {
            return false;
        }
        for (Project project : projects) {
            if (project.getTechnicalLabel().equalsIgnoreCase(techName)) {
                return true;
            }
        }
        return false;
    }
}
