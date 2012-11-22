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
package org.talend.repository.ui.actions;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.ActionFactory;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.i18n.Messages;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.actions.CopyObjectAction;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: CopyAction.java 77219 2012-01-24 01:14:15Z mhirt $
 * 
 */
public class CopyAction extends AContextualAction {

    private static CopyAction singleton;

    public CopyAction() {
        super();
        setId(ActionFactory.COPY.getId());
        this.setText(Messages.getString("CopyAction.thisText.copy")); //$NON-NLS-1$
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.COPY_ICON));
        //        this.setActionDefinitionId("copyItem"); //$NON-NLS-1$
        singleton = this;
    }

    public static CopyAction getInstance() {
        return singleton;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return super.isEnabled();
    }

    @Override
    protected void doRun() {
        IStructuredSelection selection = (IStructuredSelection) getSelection();

        // see feature 0001563: Display "Save job" prompt when "copy" action for a job is requested.
        promptForSavingIfNecessary((RepositoryNode) selection.getFirstElement());

        LocalSelectionTransfer.getTransfer().setSelection(selection);
        LocalSelectionTransfer.getTransfer().setSelectionSetTime(System.currentTimeMillis());
        // refresh();
    }

    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = true;
        RepositoryNode node = (RepositoryNode) selection.getFirstElement();
        if (selection.isEmpty()) {
            setEnabled(false);
            return;
        }
        if (ProxyRepositoryFactory.getInstance().isUserReadOnlyOnCurrentProject()) {
            canWork = false;
        }
        // cannot copy for refProject
        // if (node.getObject() != null
        // && ProxyRepositoryFactory.getInstance().getStatus(node.getObject()) == ERepositoryStatus.READ_ONLY) {
        // canWork = false;
        // }
        ERepositoryObjectType objectType = null;
        for (Object obj : ((StructuredSelection) selection).toArray()) {
            if (canWork) {
                RepositoryNode sourceNode = (RepositoryNode) obj;
                ERepositoryObjectType type = sourceNode.getObjectType();
                if (objectType != null && objectType != type) {
                    canWork = false; // different objects was copyed
                    break;
                } else {
                    objectType = type;
                }
                if (!CopyObjectAction.getInstance().validateAction(sourceNode, null)) {
                    canWork = false;
                } else if (node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.JOB_DOC
                        || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.JOBLET_DOC
                        || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_SALESFORCE_MODULE
                        || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.SERVICESOPERATION
                        || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.SERVICESPORT) {
                    canWork = false;
                }
            } else {
                break;
            }
        }
        setEnabled(canWork);
    }
}
