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
package org.talend.repository.ui.actions;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.talend.commons.ui.image.EImage;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.ui.wizards.exportjob.JobScriptsExportWizard;

/**
 * Action used to export job scripts. <br/>
 * 
 * $Id: ExportJobScriptAction.java 1 2006-12-13 下午03:12:05 bqian
 * 
 */
public class ExportJobScriptAction extends AContextualAction {

    protected static final String EXPORTJOBSCRIPTS = Messages.getString("ExportJobScriptAction.actionLabel"); //$NON-NLS-1$

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = true;
        if (selection.isEmpty()) {
            setEnabled(false);
            return;
        }
        List<RepositoryNode> nodes = (List<RepositoryNode>) selection.toList();
        for (RepositoryNode node : nodes) {
            if (node.getType() != ENodeType.REPOSITORY_ELEMENT
                    || node.getProperties(EProperties.CONTENT_TYPE) != ERepositoryObjectType.PROCESS) {
                canWork = false;
                break;
            }
            if (canWork && node.getObject() != null
                    && ProxyRepositoryFactory.getInstance().getStatus(node.getObject()) == ERepositoryStatus.DELETED) {
                canWork = false;
                break;
            }
        }
        setEnabled(canWork);
    }

    public boolean isVisible() {
        return isEnabled();
    }

    public ExportJobScriptAction() {
        super();
        this.setText(EXPORTJOBSCRIPTS);
        this.setToolTipText(EXPORTJOBSCRIPTS);
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.EXPORT_JOB_ICON));
    }

    protected void doRun() {
        JobScriptsExportWizard processWizard = new JobScriptsExportWizard();
        IWorkbench workbench = this.getViewPart().getViewSite().getWorkbenchWindow().getWorkbench();
        processWizard.setWindowTitle(EXPORTJOBSCRIPTS);
        processWizard.init(workbench, (IStructuredSelection) this.getSelection());

        Shell activeShell = Display.getCurrent().getActiveShell();
        WizardDialog dialog = new WizardDialog(activeShell, processWizard);
        workbench.saveAllEditors(true);
        dialog.setPageSize(830, 450);
        dialog.open();
    }
}
