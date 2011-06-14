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
package org.talend.repository.ui.wizards.exportjob;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.ProcessItem;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobJavaScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;

/**
 * Page of the Job Scripts Export Wizard. <br/>
 * 
 * @referto WizardArchiveFileResourceExportPage1 $Id: JobScriptsExportWizardPage.java 1 2006-12-13 下午03:09:07 bqian
 * 
 */
public class JavaJobScriptsExportWizardPage extends JobScriptsExportWizardPage {

    // dialog store id constants
    public static final String STORE_SHELL_LAUNCHER_ID = "JavaJobScriptsExportWizardPage.STORE_SHELL_LAUNCHER_ID"; //$NON-NLS-1$

    public static final String STORE_SYSTEM_ROUTINE_ID = "JavaJobScriptsExportWizardPage.STORE_SYSTEM_ROUTINE_ID"; //$NON-NLS-1$

    public static final String STORE_USER_ROUTINE_ID = "JavaJobScriptsExportWizardPage.STORE_USER_ROUTINE_ID"; //$NON-NLS-1$

    public static final String STORE_MODEL_ID = "JavaJobScriptsExportWizardPage.STORE_MODEL_ID"; //$NON-NLS-1$

    public static final String STORE_JOB_ID = "JavaJobScriptsExportWizardPage.STORE_JOB_ID"; //$NON-NLS-1$

    public static final String STORE_CONTEXT_ID = "JavaJobScriptsExportWizardPage.STORE_CONTEXT_ID"; //$NON-NLS-1$

    public static final String APPLY_TO_CHILDREN_ID = "JavaJobScriptsExportWizardPage.APPLY_TO_CHILDREN_ID"; //$NON-NLS-1$

    public static final String STORE_DEPENDENCIES_ID = "JavaJobScriptsExportWizardPage.STORE_DEPENDENCIES_ID"; //$NON-NLS-1$

    // public static final String STORE_GENERATECODE_ID = "JavaJobScriptsExportWizardPage.STORE_GENERATECODE_ID";
    // //$NON-NLS-1$

    public static final String STORE_SOURCE_ID = "JavaJobScriptsExportWizardPage.STORE_SOURCE_ID"; //$NON-NLS-1$

    public static final String STORE_DESTINATION_NAMES_ID = "JavaJobScriptsExportWizardPage.STORE_DESTINATION_NAMES_ID"; //$NON-NLS-1$

    public static final String EXTRACT_ZIP_FILE = "JavaJobScriptsExportWizardPage.EXTRACT_ZIP_FILE"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.wizards.exportjob.JobScriptsExportWizardPage#createJobScriptsManager()
     */
    @Override
    public JobScriptsManager createJobScriptsManager() {
        return new JobJavaScriptsManager();
    }

    /**
     * Create an instance of this class.
     * 
     * @param selection the selection
     */
    public JavaJobScriptsExportWizardPage(IStructuredSelection selection) {
        super("JavaJobscriptsExportPage1", selection); //$NON-NLS-1$
    }

    /**
     * Returns resources to be exported. This returns file - for just the files use getSelectedResources.
     * 
     * @return a collection of resources currently selected for export (element type: <code>IResource</code>)
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.wizards.exportjob.JobScriptsExportWizardPage#getExportResources()
     */
    @Override
    public List<ExportFileResource> getExportResources() throws ProcessorException {
        final List<ExportFileResource>[] resourcesToExportxx = new List[1];

        BusyIndicator.showWhile(this.getShell().getDisplay(), new Runnable() {

            public void run() {
                try {
                    resourcesToExportxx[0] = JavaJobScriptsExportWizardPage.super.getExportResources();
                } catch (ProcessorException e) {
                    ExceptionHandler.process(e);
                }
            }
        });
        return resourcesToExportxx[0];
    }

    /**
     * Hook method for saving widget values for restoration by the next instance of this class.
     */
    @Override
    protected void internalSaveWidgetValues() {
        // update directory names history
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames == null) {
                directoryNames = new String[0];
            }

            directoryNames = addToHistory(directoryNames, getDestinationValue());
            settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);
            settings.put(STORE_SHELL_LAUNCHER_ID, shellLauncherButton.getSelection());
            settings.put(STORE_SYSTEM_ROUTINE_ID, systemRoutineButton.getSelection());
            settings.put(STORE_USER_ROUTINE_ID, userRoutineButton.getSelection());
            settings.put(STORE_MODEL_ID, modelButton.getSelection());
            settings.put(STORE_JOB_ID, jobItemButton.getSelection());
            settings.put(STORE_SOURCE_ID, jobScriptButton.getSelection());
            settings.put(STORE_CONTEXT_ID, contextButton.getSelection());
            settings.put(APPLY_TO_CHILDREN_ID, applyToChildrenButton.getSelection());
            settings.put(EXTRACT_ZIP_FILE, chkButton.getSelection());
            // settings.put(STORE_GENERATECODE_ID, genCodeButton.getSelection());
        }
    }

    /**
     * Hook method for restoring widget values to the values that they held last time this wizard was used to
     * completion.
     */
    @Override
    protected void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames != null && directoryNames.length > 0) {
                for (int i = 0; i < directoryNames.length; i++) {
                    addDestinationItem(directoryNames[i]);
                }
            }
            setDefaultDestination();

            shellLauncherButton.setSelection(settings.getBoolean(STORE_SHELL_LAUNCHER_ID));
            systemRoutineButton.setSelection(settings.getBoolean(STORE_SYSTEM_ROUTINE_ID));
            userRoutineButton.setSelection(settings.getBoolean(STORE_USER_ROUTINE_ID));
            modelButton.setSelection(settings.getBoolean(STORE_MODEL_ID));
            jobItemButton.setSelection(settings.getBoolean(STORE_JOB_ID));
            jobScriptButton.setSelection(settings.getBoolean(STORE_SOURCE_ID));
            contextButton.setSelection(settings.getBoolean(STORE_CONTEXT_ID));
            applyToChildrenButton.setSelection(settings.getBoolean(APPLY_TO_CHILDREN_ID));
            chkButton.setSelection(settings.getBoolean(EXTRACT_ZIP_FILE));
            // genCodeButton.setSelection(settings.getBoolean(STORE_GENERATECODE_ID));
        }

        launcherCombo.setItems(manager.getLauncher());
        if (manager.getLauncher().length > 0) {
            launcherCombo.select(0);
        }
        if (process.length > 0) {
            try {
                process[0].setProcess((ProcessItem) ProxyRepositoryFactory.getInstance().getUptodateProperty(
                        process[0].getItem().getProperty()).getItem());
            } catch (PersistenceException e) {
                e.printStackTrace();
            }
            List<String> contextNames = manager.getJobContexts((ProcessItem) process[0].getItem());
            contextCombo.setItems(contextNames.toArray(new String[contextNames.size()]));
            if (contextNames.size() > 0) {
                contextCombo.select(0);
            }
        }
    }

}
