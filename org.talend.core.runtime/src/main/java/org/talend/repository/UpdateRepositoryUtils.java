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
package org.talend.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.QueriesConnection;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.metadata.builder.connection.SAPConnection;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.UpdateRepositoryHelper;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.service.IMetadataManagmentService;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;

/**
 * ggu class global comment. Detailled comment
 */
public final class UpdateRepositoryUtils {

    /**
     * ggu Comment method "getQueryFromItem".
     * 
     * get Query
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public static Query getQueryById(Item item, final String queryId) {
        if (item == null || queryId == null) {
            return null;
        }
        if (item instanceof ConnectionItem) {
            ConnectionItem connItem = (ConnectionItem) item;
            final Connection connection = connItem.getConnection();
            if (connection != null) {
                final QueriesConnection queryConn = connection.getQueries();
                if (queryConn != null) {
                    final EList queries = queryConn.getQuery();
                    if (queries != null) {
                        for (Query query : (List<Query>) queries) {
                            Object object = query.getProperties().get("deleted"); //$NON-NLS-1$
                            if (object == null || (object != null && !object.equals(Boolean.TRUE.toString()))) {
                                if (query.getId().equals(queryId)) {
                                    return query;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static Query getQueryById(final String queryId) {
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        List<ConnectionItem> metadataConnectionsItem = null;
        try {
            metadataConnectionsItem = factory.getMetadataConnectionsItem();
        } catch (PersistenceException e) {
            //
        }
        if (metadataConnectionsItem != null) {
            for (ConnectionItem connectionItem : metadataConnectionsItem) {
                if (factory.getStatus(connectionItem) != ERepositoryStatus.DELETED) {
                    Query query = getQueryById(connectionItem, queryId);
                    if (query != null) {
                        return query;
                    }
                }
            }
        }
        return null;
    }

    public static Query getQueryById(Map<String, ConnectionItem> itemsMap, final String queryId) {
        if (itemsMap == null || itemsMap.isEmpty() || queryId == null) {
            return null;
        }
        for (String connId : itemsMap.keySet()) {
            ConnectionItem item = itemsMap.get(connId);
            final Query query = getQueryById(item, queryId);
            if (query != null) {
                return query;
            }

        }
        return null;
    }

    /**
     * 
     * ggu Comment method "getTableFromItem".
     * 
     * get MetadataTable
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public static MetadataTable getTableById(Item item, final String tableId) {
        if (item == null || tableId == null) {
            return null;
        }
        if (item instanceof ConnectionItem) {
            ConnectionItem connItem = (ConnectionItem) item;
            final Connection connection = connItem.getConnection();
            if (connection != null) {
                final Set tables = ConnectionHelper.getTables(connection);
                if (tables != null) {
                    for (MetadataTable table : (Set<MetadataTable>) tables) {
                        Object object = table.getProperties().get("deleted"); //$NON-NLS-1$
                        if (object == null || (object != null && !object.equals(Boolean.TRUE.toString()))) {
                            if (table.getId().equals(tableId)) {
                                return table;
                            }
                        }

                    }
                }
            }
        }
        return null;
    }

    public static MetadataTable getTableById(final String tableId) {
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        List<ConnectionItem> metadataConnectionsItem = null;
        try {
            metadataConnectionsItem = factory.getMetadataConnectionsItem();
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
        if (metadataConnectionsItem != null) {
            for (ConnectionItem connectionItem : metadataConnectionsItem) {
                if (factory.getStatus(connectionItem) != ERepositoryStatus.DELETED) {
                    MetadataTable table = getTableById(connectionItem, tableId);
                    if (table != null) {
                        return table;
                    }
                }
            }
        }
        return null;
    }

    public static MetadataTable getTableById(Map<String, ConnectionItem> itemsMap, final String tableId) {
        if (itemsMap == null || itemsMap.isEmpty() || tableId == null) {
            return null;
        }
        for (String connId : itemsMap.keySet()) {
            ConnectionItem item = itemsMap.get(connId);
            final MetadataTable table = getTableById(item, tableId);
            if (table != null) {
                return table;
            }
        }
        return null;
    }

    /**
     * 
     * ggu Comment method "getConnectionItemByChildId".
     * 
     * @param itemsMap
     * @param childId for table and query
     * @return
     */
    public static ConnectionItem getConnectionItemByChildId(Map<String, ConnectionItem> itemsMap, final String childId) {
        if (itemsMap == null || itemsMap.isEmpty() || childId == null) {
            return null;
        }
        for (String connId : itemsMap.keySet()) {
            ConnectionItem item = itemsMap.get(connId);
            // check query
            if (getQueryById(item, childId) != null) {
                return item;
            }
            // check table
            if (getTableById(item, childId) != null) {
                return item;
            }
        }
        return null;
    }

    public static ConnectionItem getConnectionItemByChildId(final String childId) {
        final IProxyRepositoryFactory proxyRepositoryFactory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        try {
            List<ConnectionItem> metadataConnectionsItem = proxyRepositoryFactory.getMetadataConnectionsItem();
            for (ConnectionItem item : metadataConnectionsItem) {
                // check query
                if (getQueryById(item, childId) != null) {
                    return item;
                }
                // check table
                if (getTableById(item, childId) != null) {
                    return item;
                }
            }
        } catch (PersistenceException e) {
            //
        }
        return null;
    }

    /**
     * 
     * ggu Comment method "getConnectionItemByItemId".
     * 
     * get item by id
     */
    public static ConnectionItem getConnectionItemByItemId(String itemId) {
        return getConnectionItemByItemId(itemId, false);
    }

    public static ConnectionItem getConnectionItemByItemId(String itemId, boolean deleted) {
        return MetadataToolHelper.getConnectionItemByItemId(itemId, deleted);
    }

    /**
     * 
     * ggu Comment method "getQueriesFromItemId".
     * 
     * get queries from the item.
     */
    public static List<Query> getQueriesFromItemId(final String itemId) {
        ConnectionItem item = getConnectionItemByItemId(itemId);
        if (item != null) {
            final Connection connection = item.getConnection();
            if (connection != null) {
                final QueriesConnection queryConn = connection.getQueries();
                if (queryConn != null) {
                    final EList query = queryConn.getQuery();
                    if (query != null) {
                        return (List<Query>) query;
                    }
                }
            }
        }
        return null;
    }

    public static List<Query> getQueriesFromItem(Item item) {
        if (item == null) {
            return null;
        }
        if (item instanceof ConnectionItem) {
            final Connection connection = ((ConnectionItem) item).getConnection();
            if (connection != null) {
                final QueriesConnection queryConn = connection.getQueries();
                if (queryConn != null) {
                    final EList query = queryConn.getQuery();
                    if (query != null) {
                        return (List<Query>) query;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 
     * ggu Comment method "getMetadataTablesFromItemId".
     * 
     * get tables from item
     */
    public static List<MetadataTable> getMetadataTablesFromItemId(final String itemId) {
        ConnectionItem item = getConnectionItemByItemId(itemId);
        if (item != null) {
            final Connection connection = item.getConnection();
            if (connection != null) {
                final Set<MetadataTable> tableset = ConnectionHelper.getTables(connection);
                if (tableset != null) {
                    EList<MetadataTable> tables = new BasicEList<MetadataTable>();
                    tables.addAll(tableset);
                    return tables;
                }
            }
        }
        return null;
    }

    public static List<MetadataTable> getMetadataTablesFromItem(Item item) {
        if (item == null) {
            return null;
        }
        if (item instanceof ConnectionItem) {
            final Connection connection = ((ConnectionItem) item).getConnection();
            if (connection != null) {
                if (connection instanceof SAPConnection) {
                    final SAPConnection sapConnection = (SAPConnection) connection;
                    final EList functions = sapConnection.getFuntions();
                    if (functions != null && !functions.isEmpty()) {
                        final EList tables = new BasicEList();
                        for (int i = 0; i < functions.size(); i++) {
                            tables.add(((SAPFunctionUnit) functions.get(i)).getMetadataTable());
                        }
                        return tables;
                    }
                }
                final Set<MetadataTable> tableset = ConnectionHelper.getTables(connection);
                if (tableset != null) {
                    EList<MetadataTable> tables = new BasicEList<MetadataTable>();
                    tables.addAll(tableset);
                    return tables;
                }
            }
        }
        return null;
    }

    /**
     * 
     * ggu Comment method "getRepositoryObjectById".
     * 
     * @param id
     * @return
     */
    public static IRepositoryViewObject getRepositoryObjectById(final String id) {
        if (id == null || "".equals(id) || RepositoryNode.NO_ID.equals(id)) { //$NON-NLS-1$
            return null;
        }
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        try {
            IRepositoryViewObject lastVersion = factory.getLastVersion(id);
            if (lastVersion != null && factory.getStatus(lastVersion) != ERepositoryStatus.DELETED) {
                return lastVersion;
            }
        } catch (PersistenceException e) {
            //
        }
        return null;
    }

    /**
     * 
     * ggu Comment method "getRepositoryAliasName".
     * 
     * @param item
     * @return
     */
    public static String getRepositorySourceName(Item item) {
        return UpdateRepositoryHelper.getRepositorySourceName(item);
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public static IMetadataTable getTableByName(ConnectionItem item, String name) {
        if (item == null || name == null) {
            return null;
        }
        final Connection connection = item.getConnection();
        if (connection != null) {
            final EList tables = MetadataToolHelper.getMetadataTableFromConnection(connection);
            // for bug 12543
            if (tables != null && tables.size() > 0) {
                Object tableObject = tables.get(0);
                if (tableObject instanceof MetadataTable) {
                    for (MetadataTable table : (List<MetadataTable>) tables) {
                        Object object = table.getProperties().get("deleted"); //$NON-NLS-1$
                        if (object == null || (object != null && !object.equals(Boolean.TRUE.toString()))) {
                            if (table.getLabel().equals(name)) {
                                if (GlobalServiceRegister.getDefault().isServiceRegistered(IMetadataManagmentService.class)) {
                                    IMetadataManagmentService mmService = (IMetadataManagmentService) GlobalServiceRegister
                                            .getDefault().getService(IMetadataManagmentService.class);
                                    return mmService.convertMetadataTable(table);
                                }
                            }
                        }
                    }

                } else if (tableObject instanceof EObjectContainmentEList) {
                    EObjectContainmentEList eObjectContainmentEList = (EObjectContainmentEList) tableObject;
                    for (MetadataTable table : (List<MetadataTable>) eObjectContainmentEList) {
                        Object object = table.getProperties().get("deleted"); //$NON-NLS-1$
                        if (object == null || (object != null && !object.equals(Boolean.TRUE.toString()))) {
                            if (table.getLabel().equals(name)) {
                                if (GlobalServiceRegister.getDefault().isServiceRegistered(IMetadataManagmentService.class)) {
                                    IMetadataManagmentService mmService = (IMetadataManagmentService) GlobalServiceRegister
                                            .getDefault().getService(IMetadataManagmentService.class);
                                    return mmService.convertMetadataTable(table);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public static Query getQueryByName(ConnectionItem item, String name) {
        if (item == null || name == null) {
            return null;
        }
        final Connection connection = item.getConnection();
        if (connection != null) {
            final QueriesConnection queryConn = connection.getQueries();
            if (queryConn != null) {
                final EList queries = queryConn.getQuery();
                if (queries != null) {
                    for (Query query : (List<Query>) queries) {
                        Object object = query.getProperties().get("deleted"); //$NON-NLS-1$
                        if (object == null || (object != null && !object.equals(Boolean.TRUE.toString()))) {
                            if (query.getLabel().equals(name)) {
                                return query;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
