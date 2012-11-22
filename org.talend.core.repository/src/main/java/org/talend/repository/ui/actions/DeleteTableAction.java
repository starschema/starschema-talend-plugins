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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.metadata.builder.connection.AbstractMetadataObject;
import org.talend.core.model.metadata.builder.connection.CDCConnection;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.SubscriberTable;
import org.talend.core.model.metadata.builder.connection.impl.SalesforceModuleUnitImpl;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.SalesforceSchemaConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.repository.i18n.Messages;
import org.talend.core.repository.model.ISubRepositoryObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.core.repository.utils.AbstractResourceChangesService;
import org.talend.core.repository.utils.TDQServiceRegister;
import org.talend.cwm.helper.SubItemHelper;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC tguiu class global comment. Detailled comment <br/>
 * 
 * $Id: DeleteTableAction.java 77219 2012-01-24 01:14:15Z mhirt $
 * 
 */
public class DeleteTableAction extends AContextualAction {

    private static final String DELETE_LOGICAL_TITLE = Messages.getString("DeleteAction.action.logicalTitle"); //$NON-NLS-1$

    private static final String DELETE_FOREVER_TITLE = Messages.getString("DeleteAction.action.foreverTitle"); //$NON-NLS-1$

    private static final String DELETE_LOGICAL_TOOLTIP = Messages.getString("DeleteAction.action.logicalToolTipText"); //$NON-NLS-1$

    private static final String DELETE_FOREVER_TOOLTIP = Messages.getString("DeleteAction.action.logicalToolTipText"); //$NON-NLS-1$

    public DeleteTableAction() {
        super();
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.DELETE_ICON));
    }

    @Override
    protected void doRun() {
        ISelection selection = getSelection();
        Boolean confirm = null;

        // used to store the database connection object that are used to notify the sqlBuilder.
        final List<IRepositoryViewObject> connections = new ArrayList<IRepositoryViewObject>();
        final Set<ERepositoryObjectType> types = new HashSet<ERepositoryObjectType>();
        Map<String, Item> procItems = new HashMap<String, Item>();

        for (Object obj : ((IStructuredSelection) selection).toArray()) {
            if (obj instanceof RepositoryNode) {
                RepositoryNode node = (RepositoryNode) obj;
                ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
                if (node.getType() == ENodeType.REPOSITORY_ELEMENT && nodeType.isSubItem()) {
                    Connection connection = null;
                    ERepositoryObjectType parentNodeType = (ERepositoryObjectType) node.getParent().getProperties(
                            EProperties.CONTENT_TYPE);
                    if (parentNodeType == null) {
                        parentNodeType = node.getParent().getParent().getObjectType(); // for db connection
                    }
                    if (parentNodeType != null) {
                        types.add(parentNodeType);
                    }
                    ConnectionItem item = (ConnectionItem) node.getObject().getProperty().getItem();
                    connection = (item).getConnection();
                    ISubRepositoryObject subRepositoryObject = (ISubRepositoryObject) node.getObject();
                    // this one is the old metadataObject
                    AbstractMetadataObject abstractMetadataObject = subRepositoryObject.getAbstractMetadataObject();
                    if (abstractMetadataObject instanceof SubscriberTable) {
                        return;
                    }

                    // for (Object table : connection.getTables()) {
                    // if (table instanceof AbstractMetadataObject) {
                    // AbstractMetadataObject metadataTable = (AbstractMetadataObject) table;
                    // if (metadataTable.getLabel() != null
                    // && metadataTable.getLabel().equals(abstractMetadataObject.getLabel())) {
                    // abstractMetadataObject = metadataTable;
                    // }
                    // }
                    // }
                    if (abstractMetadataObject == null) {
                        return;
                    }
                    boolean isSave = true;
                    if (item instanceof ConnectionItem) {
                        AbstractResourceChangesService resChangeService = TDQServiceRegister.getInstance()
                                .getResourceChangeService(AbstractResourceChangesService.class);
                        if (resChangeService != null) {
                            isSave = resChangeService.handleResourceChange(((ConnectionItem) item).getConnection());
                        }
                    }
                    if (isSave) {
                        //
                        String sfm = null;
                        String sf = null;
                        EObject eContainer = abstractMetadataObject.eContainer();
                        if (eContainer != null && eContainer instanceof SalesforceModuleUnitImpl) {
                            sfm = ((SalesforceModuleUnitImpl) eContainer).getModuleName();
                            sf = abstractMetadataObject.getLabel();
                        }
                        if (SubItemHelper.isDeleted(abstractMetadataObject) && isSave) {
                            if (confirm == null) {
                                String title = Messages.getString("DeleteAction.dialog.title"); //$NON-NLS-1$
                                String message = Messages.getString("DeleteAction.dialog.message1") + "\n" //$NON-NLS-1$ //$NON-NLS-2$
                                        + Messages.getString("DeleteAction.dialog.message2"); //$NON-NLS-1$
                                confirm = (MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), title, message));
                            }
                            if (confirm) {
                                subRepositoryObject.removeFromParent();
                            }
                        }
                        // bug 20963
                        else if (item instanceof SalesforceSchemaConnectionItem && parentNodeType.getType() != null
                                && parentNodeType.getType().equals("METADATA_SALESFORCE_MODULE") && sfm != null && sf != null
                                && sfm.equals(sf)) {
                            // Nothing to do
                        } else {
                            SubItemHelper.setDeleted(abstractMetadataObject, true);
                        }
                        final String id = item.getProperty().getId();
                        Item tmpItem = procItems.get(id);
                        if (tmpItem == null) {
                            procItems.put(id, item);
                        }
                        connections.add(node.getObject());
                    }
                }
            }
        }
        try {
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            for (String id : procItems.keySet()) {
                Item item = procItems.get(id);
                factory.save(item);
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        Display.getCurrent().syncExec(new Runnable() {

            public void run() {
                RepositoryManager.refreshDeletedNode(types);
            }
        });
        notifySQLBuilder(connections);
        // IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(
        // RepositoryView.VIEW_ID);
        // IRepositoryView repositoryView = (IRepositoryView) viewPart;

        // // Find Metadata node
        // RepositoryNode recycleBinNode = repositoryView.getRoot().getChildren().get(8);
        //
        // // Force focus to the repository View ans erase the current user selection
        // viewPart.setFocus();
        // repositoryView.getViewSite().getSelectionProvider().setSelection(null);
        // repositoryView.expand(recycleBinNode, true);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = false;
        setText(null);
        for (Object o : (selection).toArray()) {
            RepositoryNode node = (RepositoryNode) o;
            switch (node.getType()) {
            case STABLE_SYSTEM_FOLDER:
            case SYSTEM_FOLDER:
            case SIMPLE_FOLDER:
                canWork = false;
                break;
            case REPOSITORY_ELEMENT:
                IRepositoryViewObject repObj = node.getObject();

                ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
                if (!nodeType.isSubItem()) {
                    canWork = false;
                    break;
                }
                if (node.getObjectType() == ERepositoryObjectType.METADATA_CON_TABLE) {
                    canWork = true;
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
                                    boolean is = RepositoryConstants.TABLE.equals(tableType);
                                    canWork = is && !table.isAttachedCDC();
                                }
                            }
                        } else if (item2 instanceof SalesforceSchemaConnectionItem) {
                            IRepositoryViewObject parent = node.getParent().getObject();
                            if (parent != null && parent.getLabel().equals(repositoryObject.getLabel())) {
                                canWork = false;
                            }
                        }
                    }
                } else if (node.getObjectType() == ERepositoryObjectType.METADATA_CON_CDC) {
                    canWork = false;
                } else if (node.getObjectType() == ERepositoryObjectType.METADATA_CON_QUERY) {
                    canWork = true;
                }

                if (!canWork) {
                    break;
                }
                IProxyRepositoryFactory repFactory = ProxyRepositoryFactory.getInstance();
                boolean isLocked = false;
                boolean isDeleted = false;
                isLocked = !repFactory.isPotentiallyEditable(repObj);
                isDeleted = (repFactory.getStatus(node.getObject()) == ERepositoryStatus.DELETED);
                if (isLocked) {
                    canWork = false;
                } else if (isDeleted) {
                    if (getText() == null || DELETE_FOREVER_TITLE.equals(getText())) {
                        this.setText(DELETE_FOREVER_TITLE);
                        this.setToolTipText(DELETE_FOREVER_TOOLTIP);
                    } else {
                        canWork = false;
                    }
                } else {
                    setText(DELETE_LOGICAL_TITLE);
                    setToolTipText(DELETE_LOGICAL_TOOLTIP);
                }
                break;
            default:
                // Nothing to do
                break;
            }
            if (!canWork) {
                break;
            }
        }
        setEnabled(canWork);
    }

}
