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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.MessageBoxExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.ui.images.ECoreImage;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.BinRepositoryNode;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.ISubRepositoryObject;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.model.IRepositoryNode.ENodeType;

/**
 * Action used to empty the recycle bin.<br/>
 * 
 * $Id: EmptyRecycleBinAction.java 53253 2010-12-29 04:28:05Z nrousseau $
 * 
 */
public class EmptyRecycleBinAction extends AContextualAction {

    public EmptyRecycleBinAction() {
        super();
        this.setText(Messages.getString("EmptyRecycleBinAction.action.title")); //$NON-NLS-1$
        this.setToolTipText(Messages.getString("EmptyRecycleBinAction.action.toolTipText")); //$NON-NLS-1$
        this.setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.RECYCLE_BIN_EMPTY_ICON));
    }

    @SuppressWarnings("restriction")
    protected void doRun() {
        ISelection selection = getSelection();
        Object obj = ((IStructuredSelection) selection).getFirstElement();
        final RepositoryNode node = (RepositoryNode) obj;

        final String title = Messages.getString("EmptyRecycleBinAction.dialog.title"); //$NON-NLS-1$
        String message = null;

        if (node.getChildren().size() == 0) {
            return;
        } else if (node.getChildren().size() > 1) {
            message = Messages.getString("DeleteAction.dialog.messageAllElements") + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
                    Messages.getString("DeleteAction.dialog.message2"); //$NON-NLS-1$;
        } else {
            message = Messages.getString("DeleteAction.dialog.message1") + "\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + Messages.getString("DeleteAction.dialog.message2"); //$NON-NLS-1$
        }
        final Shell shell = getShell();
        if (!(MessageDialog.openQuestion(shell, title, message))) {
            return;
        }

        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        for (IRepositoryNode child : node.getChildren()) {
            try {
                deleteElements(factory, (RepositoryNode) child);
            } catch (Exception e) {
                MessageBoxExceptionHandler.process(e);
            }
        }
        try {
            factory.saveProject(ProjectManager.getInstance().getCurrentProject());
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        RepositoryManager.refresh(ERepositoryObjectType.JOB_SCRIPT);
        RepositoryManager.getRepositoryView().refresh();
    }

    private Shell getShell() {
        Shell shell = null;

        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow != null) {
            shell = activeWorkbenchWindow.getShell();
        }
        if (shell == null) {
            Display dis = Display.getCurrent();
            if (dis == null) {
                dis = Display.getDefault();
            }
            if (dis != null) {
                shell = dis.getActiveShell();
            }
        }
        if (shell == null) {
            shell = new Shell();
        }
        return shell;
    }

    /**
     * 
     * ggu Comment method "refreshRelations".
     * 
     * bug 12883
     */
    private void refreshRelations() {
        // refresh
        if (!DeleteActionCache.getInstance().isDocRefresh()) { // not refresh in JobDeleteListener
            RepositoryManager.refreshCreatedNode(ERepositoryObjectType.DOCUMENTATION);
        }
        RepositoryManager.refreshDeletedNode(null);
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow != null) {
            IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
            if (page != null) {
                for (IEditorReference editors : page.getEditorReferences()) {
                    CorePlugin.getDefault().getDiagramModelService().refreshBusinessModel(editors);
                }
            }
        }
    }

    private void deleteElements(IProxyRepositoryFactory factory, RepositoryNode currentNode) throws PersistenceException,
            BusinessException {
        IRepositoryViewObject objToDelete = currentNode.getObject();
        if (objToDelete == null) {
            return;
        }
        if (objToDelete instanceof ISubRepositoryObject) {
            ISubRepositoryObject subRepositoryObject = (ISubRepositoryObject) objToDelete;
            if (!isRootNodeDeleted(currentNode)) {
                Item item = subRepositoryObject.getProperty().getItem();
                subRepositoryObject.removeFromParent();
                factory.save(item);
            }
        } else {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            for (IEditorReference editors : page.getEditorReferences()) {
                String nameInEditor = editors.getName();
                if (objToDelete.getLabel().equals(nameInEditor.substring(nameInEditor.indexOf(" ") + 1))) { //$NON-NLS-1$
                    page.closeEditor(editors.getEditor(false), false);
                }
            }
            if (objToDelete.getType() != ERepositoryObjectType.JOB_DOC
                    && objToDelete.getType() != ERepositoryObjectType.JOBLET_DOC) {
                if (currentNode.getType() == ENodeType.SIMPLE_FOLDER) {
                    for (IRepositoryNode curNode : currentNode.getChildren()) {
                        deleteElements(factory, (RepositoryNode) curNode);
                    }
                    factory.deleteFolder(ProjectManager.getInstance().getCurrentProject(), currentNode.getContentType(),
                            RepositoryNodeUtilities.getFolderPath(currentNode.getObject().getProperty().getItem()), true);
                } else {
                    factory.deleteObjectPhysical(ProjectManager.getInstance().getCurrentProject(), objToDelete, null, true);
                }
            }
        }
    }

    /**
     * DOC qzhang Comment method "getRootNode".
     * 
     * @param child
     * @return
     */
    private boolean isRootNodeDeleted(RepositoryNode child) {
        boolean isDeleted = false;
        if (child != null) {
            RepositoryNode parent = child.getParent();
            if (parent != null) {
                IRepositoryViewObject object = parent.getObject();
                if (object != null) {
                    ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

                    isDeleted = factory.getStatus(object) == ERepositoryStatus.DELETED;
                }

                if (!isDeleted) {
                    isDeleted = isRootNodeDeleted(parent);
                }
            }
        }
        return isDeleted;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            canWork = false;
        }
        if (canWork) {
            Object o = selection.getFirstElement();
            RepositoryNode node = (RepositoryNode) o;
            switch (node.getType()) {
            case STABLE_SYSTEM_FOLDER:
                if (!(node instanceof BinRepositoryNode) || !node.hasChildren()) {
                    canWork = false;
                }
                break;
            default:
                canWork = false;
                break;
            }
            if (canWork && !ProjectManager.getInstance().isInCurrentMainProject(node)) {
                canWork = false;
            }
        }
        setEnabled(canWork);
    }

}
