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
package org.talend.repository.ui.actions.importexport;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.talend.commons.ui.image.EImage;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.imports.ImportItemWizard;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.ui.actions.AContextualAction;
import org.talend.repository.ui.views.IRepositoryView;
import org.talend.repository.ui.views.RepositoryView;

/**
 */
public final class ImportItemAction extends AContextualAction implements IWorkbenchWindowActionDelegate {

    private static final String IMPORT_ITEM = Messages.getString("ImportItemAction.Label"); //$NON-NLS-1$

    private boolean toolbarAction = true;

    public void init(TreeViewer viewer, IStructuredSelection selection) {
        toolbarAction = false;
        boolean canWork = false;
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (selection.size() == 1) {
            if (selection.getFirstElement() instanceof RepositoryNode) {
                RepositoryNode repositoryNode = (RepositoryNode) selection.getFirstElement();
                if (factory.isUserReadOnlyOnCurrentProject()
                        || !ProjectManager.getInstance().isInCurrentMainProject(repositoryNode)) {
                    setEnabled(false);
                    return;
                }
                if (repositoryNode.getType().equals(ENodeType.SYSTEM_FOLDER)
                        || repositoryNode.getType().equals(ENodeType.SIMPLE_FOLDER)) {
                    canWork = true;
                }
                if (repositoryNode.getContentType() == ERepositoryObjectType.REFERENCED_PROJECTS) {
                    canWork = false;
                }
                if (repositoryNode.getContentType() == ERepositoryObjectType.SVN_ROOT) {
                    canWork = false;
                }
                if (repositoryNode.getObject() != null
                        && repositoryNode.getObject().getRepositoryStatus() == ERepositoryStatus.DELETED) {
                    canWork = false;
                }
            }
        }
        setEnabled(canWork);
    }

    @Override
    public boolean isVisible() {
        return isEnabled();
    }

    public ImportItemAction() {
        super();
        this.setText(IMPORT_ITEM);
        this.setToolTipText(IMPORT_ITEM);
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.IMPORT_ICON));
    }

    @Override
    protected void doRun() {
        // qli modified to fix the bug "6999".
        final TreeViewer repositoryTreeView = CorePlugin.getDefault().getRepositoryService().getRepositoryTreeView();
        if (repositoryTreeView != null) {
            repositoryTreeView.getTree().setFocus();
        }
        ISelection selection = this.getSelection();
        if (toolbarAction == true) {
            IRepositoryView repositoryView = RepositoryView.show();
            selection = (IStructuredSelection) repositoryView.getViewer().getSelection();
        }
        if (selection instanceof IStructuredSelection) {
            RepositoryNode rNode = null;
            if (((IStructuredSelection) selection).getFirstElement() instanceof RepositoryNode) {
                rNode = (RepositoryNode) ((IStructuredSelection) selection).getFirstElement();
            }

            ImportItemWizard wizard = new ImportItemWizard(rNode);
            IWorkbench workbench = this.getViewPart().getViewSite().getWorkbenchWindow().getWorkbench();
            wizard.setWindowTitle(IMPORT_ITEM);
            wizard.init(workbench, (IStructuredSelection) selection);

            Shell activeShell = Display.getCurrent().getActiveShell();
            WizardDialog dialog = new WizardDialog(activeShell, wizard);
            if (dialog.open() == Window.OK) {
                refresh();
                if (wizard.isNeedToRefreshPalette()) {
                    ComponentUtilities.updatePalette();
                }
            }
        }
    }

    public void dispose() {
    }

    public void init(IWorkbenchWindow window) {
    }

    public void run(IAction action) {
        doRun();
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }
}
