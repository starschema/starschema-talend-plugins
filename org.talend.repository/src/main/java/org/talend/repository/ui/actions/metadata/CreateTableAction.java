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

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.connection.CDCConnection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.SubscriberTable;
import org.talend.core.model.properties.BRMSConnectionItem;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.EbcdicConnectionItem;
import org.talend.core.model.properties.HL7ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.MDMConnectionItem;
import org.talend.core.model.properties.SAPConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.ui.images.ECoreImage;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.MetadataTableRepositoryObject;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;

/**
 * Action used to create table on metadata.<br/>
 * 
 * $Id: CreateTableAction.java 59927 2011-05-06 07:37:21Z ldong $
 * 
 */
public class CreateTableAction extends AbstractCreateTableAction {

    protected static Logger log = Logger.getLogger(CreateConnectionAction.class);

    protected static final String PID = RepositoryPlugin.PLUGIN_ID;

    protected static final String CREATE_LABEL = Messages.getString("CreateTableAction.action.createTitle"); //$NON-NLS-1$

    protected static final String EDIT_LABEL = Messages.getString("CreateTableAction.action.editTitle"); //$NON-NLS-1$

    private RepositoryNode node;

    public CreateTableAction() {
        super();

        this.setText(CREATE_LABEL);
        this.setToolTipText(CREATE_LABEL);
        this.setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.METADATA_TABLE_ICON));
    }

    /**
     * yzhang CreateTableAction constructor comment.
     * 
     * @param node
     */
    public CreateTableAction(RepositoryNode node) {
        this();
        this.node = node;
    }

    protected void doRun() {
        RepositoryNode metadataNode = null;
        if (node == null && repositoryNode != null) {
            node = repositoryNode;
        }
        if (node == null) {
            // RepositoryNode metadataNode = getViewPart().getRoot().getChildren().get(6);
            metadataNode = getMetadataNode(getCurrentRepositoryNode());
            IStructuredSelection selection = (IStructuredSelection) getSelection();
            node = (RepositoryNode) selection.getFirstElement();
            // Force focus to the repositoryView and open Metadata and DbConnection nodes
            getViewPart().setFocus();
            getViewPart().expand(metadataNode, true);
        } else {
            metadataNode = getMetadataNode(node);
        }

        // Init the content of the Wizard
        init(node);

        ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);

        if (ERepositoryObjectType.METADATA_CON_TABLE.equals(nodeType)) {
            final IRepositoryViewObject object = node.getObject();
            if (object instanceof MetadataTableRepositoryObject) {
                MetadataTable table = ((MetadataTableRepositoryObject) object).getTable();
                if (table instanceof SubscriberTable) {
                    this.node = null;
                    return;
                }
            }
            ConnectionItem connectionItem = (ConnectionItem) object.getProperty().getItem();
            nodeType = ERepositoryObjectType.getItemType(connectionItem);

        }

        if (ERepositoryObjectType.METADATA_FILE_POSITIONAL.equals(nodeType)) {
            createFilePositionalTableWizard(node, false);
        } else if (ERepositoryObjectType.METADATA_FILE_DELIMITED.equals(nodeType)) {
            createFileDelimitedTableWizard(node, false);
        } else if (ERepositoryObjectType.METADATA_CONNECTIONS.equals(nodeType)) {
            createDatabaseTableWizard(node, false);
        } else if (ERepositoryObjectType.METADATA_FILE_REGEXP.equals(nodeType)) {
            createFileRegexpTableWizard(node, false);
        } else if (ERepositoryObjectType.METADATA_FILE_XML.equals(nodeType)) {
            createFileXmlTableWizard(node, false);
        } else if (ERepositoryObjectType.METADATA_FILE_LDIF.equals(nodeType)) {
            createFileLdifTableWizard(node, false);
        } else if (ERepositoryObjectType.METADATA_FILE_EXCEL.equals(nodeType)) {
            createFileExcelTableWizard(node, false);
        } else if (ERepositoryObjectType.METADATA_GENERIC_SCHEMA.equals(nodeType)) {
            createGenericSchemaWizard(node, false);
        } else if (ERepositoryObjectType.METADATA_LDAP_SCHEMA.equals(nodeType)) {
            createLDAPSchemaWizard(node, false);
        } else if (ERepositoryObjectType.METADATA_WSDL_SCHEMA.equals(nodeType)) {
            createWSDLSchemaWizard(node, false);
        } else if (ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA.equals(nodeType)) {
            createSalesforceSchemaWizard(node, false);
        }
        this.node = null;
    }

    @Override
    public Class getClassForDoubleClick() {
        return IMetadataTable.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.repository.ui.actions.metadata.AbstractCreateAction#init(org.talend.repository.model.RepositoryNode)
     */
    @Override
    protected void init(RepositoryNode node) {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject() || !ProjectManager.getInstance().isInCurrentMainProject(node)) {
            setEnabled(false);
        } else {
            if (ENodeType.REPOSITORY_ELEMENT.equals(node.getType())) {
                ERepositoryStatus status = node.getObject().getRepositoryStatus();
                boolean isDeleted = status == ERepositoryStatus.DELETED;
                if (node.getObject().getRepositoryStatus() == ERepositoryStatus.DELETED
                        || node.getObject().getRepositoryStatus() == ERepositoryStatus.LOCK_BY_OTHER) {
                    setEnabled(false);
                    return;
                }
                if (factory.getStatus(node.getObject()) == ERepositoryStatus.DELETED
                        || factory.getStatus(node.getObject()) == ERepositoryStatus.LOCK_BY_OTHER) {
                    setEnabled(false);
                    return;
                }

                ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
                if (ERepositoryObjectType.METADATA_CON_TABLE.equals(nodeType)) {
                    setText(EDIT_LABEL);
                    collectSiblingNames(node);
                    IRepositoryViewObject repositoryObject = node.getObject();
                    if (repositoryObject != null) {
                        Item item2 = repositoryObject.getProperty().getItem();
                        if (item2 instanceof DatabaseConnectionItem) {
                            DatabaseConnectionItem item = (DatabaseConnectionItem) repositoryObject.getProperty().getItem();
                            DatabaseConnection connection = (DatabaseConnection) item.getConnection();
                            CDCConnection cdcConns = connection.getCdcConns();
                            if (cdcConns != null) {
                                if (repositoryObject instanceof MetadataTableRepositoryObject) {
                                    MetadataTable table = ((MetadataTableRepositoryObject) repositoryObject).getTable();
                                    String tableType = table.getTableType();
                                    setEnabled(RepositoryConstants.TABLE.equals(tableType));
                                    return;
                                }
                            }
                        }

                        if (item2 instanceof SAPConnectionItem) {
                            setEnabled(false);
                            return;
                        }

                        if (item2 instanceof EbcdicConnectionItem) {
                            setEnabled(false);
                            return;
                        }

                        if (item2 instanceof HL7ConnectionItem) {
                            setEnabled(false);
                            return;
                        }
                        if (item2 instanceof BRMSConnectionItem) {
                            setEnabled(false);
                            return;
                        }

                        if (item2 instanceof MDMConnectionItem) {
                            setEnabled(false);
                            return;
                        }
                    }
                    if (isLastVersion(node)) {
                        setEnabled(true);
                    }
                    return;
                }

                if (ERepositoryObjectType.METADATA_CONNECTIONS.equals(nodeType)
                        || ERepositoryObjectType.METADATA_FILE_DELIMITED.equals(nodeType)
                        || ERepositoryObjectType.METADATA_FILE_POSITIONAL.equals(nodeType)
                        || ERepositoryObjectType.METADATA_FILE_REGEXP.equals(nodeType)
                        || ERepositoryObjectType.METADATA_FILE_XML.equals(nodeType)
                        || ERepositoryObjectType.METADATA_FILE_LDIF.equals(nodeType)
                        || ERepositoryObjectType.METADATA_FILE_EXCEL.equals(nodeType)
                        || ERepositoryObjectType.METADATA_GENERIC_SCHEMA.equals(nodeType)
                        || ERepositoryObjectType.METADATA_LDAP_SCHEMA.equals(nodeType)
                        || ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA.equals(nodeType)) {
                    setText(CREATE_LABEL);
                    collectChildNames(node);
                    if (isLastVersion(node)) {
                        setEnabled(true);
                    }
                    return;
                }
                // if (ERepositoryObjectType.METADATA_CON_QUERY.equals(nodeType)) {
                // setEnabled(false);
                // }
            }
        }
    }
}
