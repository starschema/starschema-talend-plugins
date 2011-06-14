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
package org.talend.repository.ui.actions.folder;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.ui.images.ECoreImage;
import org.talend.core.ui.images.OverlayImageProvider;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.ui.actions.AContextualAction;
import org.talend.repository.ui.wizards.folder.FolderWizard;

/**
 * Action used to create a new folder in repository.<br/>
 * 
 * $Id: CreateFolderAction.java 46952 2010-08-18 08:41:09Z nrousseau $
 * 
 */
public class CreateFolderAction extends AContextualAction {

    public CreateFolderAction() {
        super();
        this.setText(Messages.getString("CreateFolderAction.action.title")); //$NON-NLS-1$
        this.setToolTipText(Messages.getString("CreateFolderAction.action.toolTipText")); //$NON-NLS-1$

        Image folderImg = ImageProvider.getImage(ECoreImage.FOLDER_CLOSE_ICON);
        this.setImageDescriptor(OverlayImageProvider.getImageWithNew(folderImg));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    protected void doRun() {
        ISelection selection = getSelection();
        Object obj = ((IStructuredSelection) selection).getFirstElement();
        RepositoryNode node = (RepositoryNode) obj;

        ERepositoryObjectType objectType = null;
        IPath path = null;

        path = RepositoryNodeUtilities.getPath(node);
        if (RepositoryConstants.isSystemFolder(path.toString())) {
            return;
        }
        objectType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);

        if (objectType != null) {
            FolderWizard processWizard = new FolderWizard(path, objectType, null);
            Shell activeShell = Display.getCurrent().getActiveShell();
            WizardDialog dialog = new WizardDialog(activeShell, processWizard);
            dialog.setPageSize(400, 60);
            dialog.create();
            if (dialog.open() == Window.OK) {
                RepositoryManager.refreshCreatedNode(objectType);
            }
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
            Object property = node.getProperties(EProperties.CONTENT_TYPE);
            switch (node.getType()) {
            case REPOSITORY_ELEMENT:
            case STABLE_SYSTEM_FOLDER:
                canWork = false;
                break;
            case SYSTEM_FOLDER:
                if (property == ERepositoryObjectType.GENERATED || property == ERepositoryObjectType.JOBS
                        || property == ERepositoryObjectType.JOBLETS || property == ERepositoryObjectType.SQLPATTERNS
                        || property == ERepositoryObjectType.REFERENCED_PROJECTS || property == ERepositoryObjectType.SVN_ROOT) {
                    canWork = false;
                }
                break;
            case SIMPLE_FOLDER:
                if (property == ERepositoryObjectType.JOB_DOC || property == ERepositoryObjectType.JOBLET_DOC
                        || (property == ERepositoryObjectType.SQLPATTERNS && !isUnderUserDefined(node))) {
                    canWork = false;
                }
                if (node.getObject().getProperty().getItem().getState().isDeleted()) {
                    canWork = false;
                }
                break;
            default:
                // Nothing to do
            }

            if (canWork && !ProjectManager.getInstance().isInCurrentMainProject(node)) {
                canWork = false;
            }
        }
        setEnabled(canWork);
    }

}
