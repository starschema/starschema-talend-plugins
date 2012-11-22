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
package org.talend.repository.ui.actions.importexport;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.metadata.builder.connection.CDCConnection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.actions.AContextualAction;
import org.talend.repository.ui.views.IRepositoryView;

/**
 */
/**
 * DOC Administrator class global comment. Detailed comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
public final class ExportItemAction extends AContextualAction implements IWorkbenchWindowActionDelegate {

    private static final String EXPORT_ITEM = Messages.getString("ExportItemAction.Label"); //$NON-NLS-1$

    private boolean toolbarAction = false;

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.ui.swt.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        toolbarAction = false;
        boolean visible = false;
        if (selection.isEmpty()) {
            visible = false;
        } else if (ProxyRepositoryFactory.getInstance().isUserReadOnlyOnCurrentProject()) {
            visible = false;
        } else {
            for (Object object : (selection).toArray()) {

                // Avoid to show this action on Node "Generated"/"Jobs" and Node JOB_DOC, JOBLET_DOC.
                RepositoryNode node = (RepositoryNode) object;

                Object nodProperty = node.getProperties(EProperties.CONTENT_TYPE);
                ERepositoryObjectType contentType = node.getContentType();

                if (!ERepositoryObjectType.JOB_DOC.equals(nodProperty) && !ERepositoryObjectType.JOBLET_DOC.equals(nodProperty)
                        && !ERepositoryObjectType.GENERATED.equals(nodProperty)
                        && !ERepositoryObjectType.JOBS.equals(nodProperty) && !ERepositoryObjectType.JOBLETS.equals(nodProperty)
                        && !ERepositoryObjectType.SQLPATTERNS.equals(nodProperty)
                        && !ERepositoryObjectType.METADATA_CON_CDC.equals(nodProperty)
                        && !ERepositoryObjectType.METADATA_CON_TABLE.equals(nodProperty)
                        && !ERepositoryObjectType.METADATA_CON_QUERY.equals(nodProperty)
                        && !ERepositoryObjectType.SVN_ROOT.equals(nodProperty)
                        && !ERepositoryObjectType.SERVICESOPERATION.equals(nodProperty)
                        && !ERepositoryObjectType.SERVICESPORT.equals(nodProperty)) {
                    visible = true;
                }
                // for cdc
                RepositoryNode parent = node.getParent();
                if (ENodeType.STABLE_SYSTEM_FOLDER.equals(node.getType())) {
                    if (parent != null) {
                        RepositoryNode pNode = parent;
                        if (ENodeType.STABLE_SYSTEM_FOLDER.equals(parent.getType())) {
                            pNode = parent.getParent();
                            if (pNode != null && ENodeType.REPOSITORY_ELEMENT.equals(pNode.getType())) {
                                ERepositoryObjectType nodeType = (ERepositoryObjectType) pNode
                                        .getProperties(EProperties.CONTENT_TYPE);
                                if (ERepositoryObjectType.METADATA_CONNECTIONS.equals(nodeType) && pNode.getObject() != null) {
                                    DatabaseConnection connection = (DatabaseConnection) ((DatabaseConnectionItem) pNode
                                            .getObject().getProperty().getItem()).getConnection();
                                    if (connection != null) {
                                        CDCConnection cdcConns = connection.getCdcConns();
                                        if (cdcConns != null) {
                                            visible = false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // if (visible && parent != null && parent instanceof BinRepositoryNode) {
                // visible = false;
                // }
            }
        }
        setEnabled(visible);
    }

    public ExportItemAction() {
        super();
        this.setText(EXPORT_ITEM);
        this.setToolTipText(EXPORT_ITEM);
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.EXPORT_ICON));
    }

    @Override
    protected void doRun() {
        IRepositoryView repositoryView = getViewPart();
        if (repositoryView != null && repositoryView.getViewer() instanceof TreeViewer) {
            ((TreeViewer) repositoryView.getViewer()).getTree().setFocus();
        }
        ExportItemWizard wizard = new ExportItemWizard();
        IWorkbench workbench = getWorkbench();
        wizard.setWindowTitle(EXPORT_ITEM);
        if (!toolbarAction) {
            wizard.init(workbench, (IStructuredSelection) this.getSelection());
        } else {
            if (repositoryView != null) {
                IStructuredSelection selection = (IStructuredSelection) repositoryView.getViewer().getSelection();
                wizard.init(workbench, selection);
            }
        }

        Shell activeShell = Display.getCurrent().getActiveShell();
        WizardDialog dialog = new WizardDialog(activeShell, wizard);
        dialog.open();
    }

    public void dispose() {
    }

    public void init(IWorkbenchWindow window) {
    }

    public void run(IAction action) {
        toolbarAction = true;
        run();
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }
}
