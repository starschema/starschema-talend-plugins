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
package org.talend.repository.ui.actions.metadata;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.image.EImage;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.MetadataTableRepositoryObject;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * Administrator class global comment. Detailed comment <br/>
 * 
 */
public class CopyToGenericSchemaAction extends AContextualAction {

    protected static final String LABEL = "Copy to Generic schema"; //$NON-NLS-1$

    private boolean isAllowedRepositoryElement = false;

    private RepositoryNode sourceNode;

    private TreeViewer viewer;

    public CopyToGenericSchemaAction() {
        super();
        this.setText(LABEL);
        this.setToolTipText(LABEL);
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.COPY_ICON));
    }

    public void init(final TreeViewer viewer, final IStructuredSelection selection) {
        this.viewer = viewer;
        setEnabled(false);
        Object o = selection.getFirstElement();
        if (selection.isEmpty() || selection.size() != 1 || o == null || !(o instanceof RepositoryNode)) {
            return;
        }
        init((RepositoryNode) o);
        if (ProxyRepositoryFactory.getInstance().isUserReadOnlyOnCurrentProject()) {
            // setEnabled(false);
        }
        if (!ProjectManager.getInstance().isInCurrentMainProject((RepositoryNode) o)) {
            setEnabled(false);
        }
    }

    /**
     * Administrator Comment method "init".
     * 
     * @param node
     */
    protected void init(RepositoryNode node) {
        ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);

        if (nodeType == null || !(node.getObject() instanceof MetadataTableRepositoryObject)) {
            return;
        }

        switch (nodeType) {
        case METADATA_CON_TABLE:
        case METADATA_CON_VIEW:
        case METADATA_CON_SYNONYM:
        case METADATA_FILE_DELIMITED:
        case METADATA_FILE_POSITIONAL:
        case METADATA_FILE_REGEXP:
        case METADATA_FILE_XML:
        case METADATA_FILE_EXCEL:
        case METADATA_LDAP_SCHEMA:
        case METADATA_SALESFORCE_SCHEMA:
        case METADATA_FILE_LDIF:
            break;
        default:
            return;
        }

        RepositoryNode genericNode = (RepositoryNode) CorePlugin.getDefault().getRepositoryService().getRootRepositoryNode(
                ERepositoryObjectType.METADATA_GENERIC_SCHEMA);
        if (genericNode == null) {
            return;
        }

        switch (node.getType()) {
        case REPOSITORY_ELEMENT:
            isAllowedRepositoryElement = true;
            this.sourceNode = node;
            break;
        default:
            return;
        }
        setEnabled(true);
    }

    @Override
    protected void doRun() {
        if (isAllowedRepositoryElement) {
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            try {
                CopyToGenericSchemaHelper.copyToGenericSchema(factory, (MetadataTableRepositoryObject) this.sourceNode
                        .getObject(), new Path("")); //$NON-NLS-1$
                RepositoryManager.refreshCreatedNode(ERepositoryObjectType.METADATA_GENERIC_SCHEMA);
            } catch (PersistenceException e) {
                // e.printStackTrace();
                ExceptionHandler.process(e);
            }
        }
    }
}
