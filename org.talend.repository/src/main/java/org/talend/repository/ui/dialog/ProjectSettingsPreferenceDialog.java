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
package org.talend.repository.ui.dialog;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.PluginChecker;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.actions.ExportProjectSettings;
import org.talend.repository.ui.actions.ImportProjectSettings;

/**
 * wchen class global comment. Detailled comment
 */
public class ProjectSettingsPreferenceDialog extends PreferenceDialog {

    private Button importButton;

    private Button exportButton;

    public static final int IMPORT = 97;

    public static final int EXPORT = 98;

    /**
     * wchen ProjectSettingsPreferenceDialog constructor comment.
     * 
     * @param parentShell
     * @param manager
     */
    public ProjectSettingsPreferenceDialog(Shell parentShell, PreferenceManager manager) {
        super(parentShell, manager);
        unloadProject();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        importButton = createButton(parent, IMPORT, "Import", false); //$NON-NLS-1$
        exportButton = createButton(parent, EXPORT, "Export", false); //$NON-NLS-1$
        super.createButtonsForButtonBar(parent);

    }

    @Override
    protected void buttonPressed(int buttonId) {
        switch (buttonId) {
        case IDialogConstants.OK_ID: {
            okPressed();
            commiteProjectSettings();
            return;
        }
        case IDialogConstants.CANCEL_ID: {
            cancelPressed();
            return;
        }
        case IDialogConstants.HELP_ID: {
            helpPressed();
            return;
        }
        case IMPORT: {
            importPressed();
            commiteProjectSettings();
            return;
        }
        case EXPORT: {
            exportPressed();
            return;
        }
        }
    }

    private void commiteProjectSettings() {
        // excute a workUnit to do svn commite
        RepositoryWorkUnit repositoryWorkUnit = new RepositoryWorkUnit(ProjectManager.getInstance().getCurrentProject(), "") {

            public void run() throws PersistenceException {
                // do nothing
            }
        };
        repositoryWorkUnit.setAvoidUnloadResources(true);
        CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory().executeRepositoryWorkUnit(repositoryWorkUnit);

    }

    private void importPressed() {

        FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
        String[] files = new String[] { "*.xml" }; //$NON-NLS-1$
        fileDialog.setFilterExtensions(files);

        String path = fileDialog.open();
        ImportProjectSettings settings = new ImportProjectSettings(path);

        boolean error = false;
        try {
            settings.updateProjectSettings();
        } catch (Exception e) {
            error = true;
            showErrorMessage();
        }

        // close the projec settings and open it again to get new settings
        if (!error) {
            close();
            ProjectSettingDialog dialog = new ProjectSettingDialog();
            dialog.open();
        }
    }

    private void exportPressed() {
        saveCurrentSettings();
        FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
        fileDialog.setFileName("ProjectSettings.xml"); //$NON-NLS-1$
        String[] files = new String[] { "*.xml" }; //$NON-NLS-1$
        fileDialog.setFilterExtensions(files);

        String path = fileDialog.open();
        ExportProjectSettings settings = new ExportProjectSettings(path);
        settings.saveProjectSettings();

    }

    private void showErrorMessage() {
        MessageBox message = new MessageBox(new Shell(getShell()), SWT.ICON_ERROR | SWT.OK);
        message.setMessage(Messages.getString("ImportProjectSettings.Error")); //$NON-NLS-1$
        message.open();
    }

    protected void saveCurrentSettings() {
        SafeRunnable.run(new SafeRunnable() {

            private boolean errorOccurred;

            public void run() {
                errorOccurred = false;
                boolean hasFailedOK = false;
                try {
                    Iterator nodes = getPreferenceManager().getElements(PreferenceManager.PRE_ORDER).iterator();
                    while (nodes.hasNext()) {
                        IPreferenceNode node = (IPreferenceNode) nodes.next();
                        IPreferencePage page = node.getPage();
                        if (page != null) {
                            if (!page.performOk()) {
                                hasFailedOK = true;
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    handleException(e);
                } finally {

                    if (hasFailedOK) {
                        setReturnCode(FAILED);
                        return;
                    }

                    if (!errorOccurred) {

                        handleSave();
                    }
                    setReturnCode(OK);
                }
            }

            public void handleException(Throwable e) {
                errorOccurred = true;

                Policy.getLog().log(new Status(IStatus.ERROR, Policy.JFACE, 0, e.toString(), e));

                clearSelectedNode();
                String message = JFaceResources.getString("SafeRunnable.errorMessage"); //$NON-NLS-1$

                Policy.getStatusHandler().show(new Status(IStatus.ERROR, Policy.JFACE, message, e),
                        JFaceResources.getString("Error")); //$NON-NLS-1$ 

            }
        });
    }

    void clearSelectedNode() {
        setSelectedNodePreference(null);
    }

    private void unloadProject() {
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        final ProxyRepositoryFactory instance = ProxyRepositoryFactory.getInstance();
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            try {
                if (!instance.isLocalConnectionProvider()) {
                    // instance.getRepositoryFactoryFromProvider().reloadProject(currentProject);
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }

    }

    private void fillParentsFolderInfos(FolderItem folderItem) {
        for (Item curItem : (List<Item>) folderItem.getChildren()) {
            if (curItem instanceof FolderItem) {
                fillParentsFolderInfos((FolderItem) curItem);
            }
            curItem.setParent(folderItem);
        }
    }

}
