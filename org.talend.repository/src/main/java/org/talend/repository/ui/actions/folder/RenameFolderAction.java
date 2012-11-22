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
package org.talend.repository.ui.actions.folder;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.editor.RepositoryEditorInput;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.actions.AContextualAction;
import org.talend.repository.ui.wizards.folder.FolderWizard;

/**
 * Action used to create a new folder in repository.<br/>
 * 
 * $Id: CreateFolderAction.java 1100 2006-12-19 14:27:01Z amaumont $
 * 
 */
public class RenameFolderAction extends AContextualAction {

    public RenameFolderAction() {
        super();
        this.setText(Messages.getString("RenameFolderAction.action.title")); //$NON-NLS-1$
        this.setToolTipText(Messages.getString("RenameFolderAction.action.toolTipText")); //$NON-NLS-1$
        this.setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.FOLDER_CLOSE_ICON));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    protected void doRun() {
        ISelection selection = getSelection();
        Object obj = ((IStructuredSelection) selection).getFirstElement();
        RepositoryNode node = (RepositoryNode) obj;

        // Check if some jobs in the folder are currently opened:
        String firstChildOpen = getFirstOpenedChild(node);
        if (firstChildOpen != null) {
            MessageDialog.openWarning(new Shell(), Messages.getString("RenameFolderAction.warning.editorOpen.title"), Messages //$NON-NLS-1$
                    .getString("RenameFolderAction.warning.editorOpen.message", firstChildOpen, node //$NON-NLS-1$
                            .getProperties(EProperties.LABEL)));
            return;
        }

        ERepositoryObjectType objectType = null;
        IPath path = null;

        path = RepositoryNodeUtilities.getPath(node);
        objectType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);

        if (objectType != null) {
            FolderWizard processWizard = new FolderWizard(path, objectType, node.getObject().getLabel());
            Shell activeShell = Display.getCurrent().getActiveShell();
            WizardDialog dialog = new WizardDialog(activeShell, processWizard);
            dialog.setPageSize(400, 60);
            dialog.create();
            dialog.open();
        }
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
            Object obj = node.getProperties(EProperties.LABEL);
            switch (node.getType()) {
            case SIMPLE_FOLDER:
                String label = null;
                if (obj instanceof String) {
                    label = (String) obj;
                }
                if (node.getContentType() == ERepositoryObjectType.JOB_DOC
                        || node.getContentType() == ERepositoryObjectType.JOBLET_DOC
                        || (node.getContentType() == ERepositoryObjectType.SQLPATTERNS && !isUnderUserDefined(node))
                        || RepositoryConstants.USER_DEFINED.equals(label)) {
                    canWork = false;
                }
                if (node.getObject() != null && node.getObject().getProperty().getItem().getState().isDeleted()) {
                    canWork = false;
                }
                break;
            default:
                canWork = false;
            }
            if (canWork && !ProjectManager.getInstance().isInCurrentMainProject(node)) {
                canWork = false;
            }
        }
        setEnabled(canWork);
    }

    private String getFirstOpenedChild(IRepositoryNode node) {
        if (node.hasChildren()) {
            IWorkbenchPage page = getActivePage();
            IEditorReference[] editorReferences = page.getEditorReferences();
            List<String> openEditor = new ArrayList<String>();
            for (IEditorReference tmp : editorReferences) {
                try {
                    IEditorInput editorInput = tmp.getEditorInput();
                    if (editorInput instanceof RepositoryEditorInput) {
                        RepositoryEditorInput rei = (RepositoryEditorInput) editorInput;
                        openEditor.add(rei.getItem().getProperty().getId());
                    }
                } catch (PartInitException e) {
                    ExceptionHandler.process(e, Level.WARN);
                }
            }

            List<IRepositoryNode> children = node.getChildren();
            for (IRepositoryNode currentNode : children) {
                if (currentNode.getType() == ENodeType.REPOSITORY_ELEMENT) {
                    if (openEditor.contains(currentNode.getObject().getId())) {
                        return currentNode.getObject().getLabel();
                    }
                } else if (currentNode.getType() == ENodeType.SIMPLE_FOLDER) {
                    String childOpen = getFirstOpenedChild(currentNode);
                    if (childOpen != null) {
                        return childOpen;
                    }
                }
            }
        }
        return null;
    }
}
