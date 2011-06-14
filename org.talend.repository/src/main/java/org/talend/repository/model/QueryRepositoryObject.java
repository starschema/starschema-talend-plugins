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
package org.talend.repository.model;

import java.util.Iterator;

import org.talend.core.model.metadata.builder.connection.AbstractMetadataObject;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.QueriesConnection;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class QueryRepositoryObject extends org.talend.core.model.metadata.Query implements ISubRepositoryObject {

    private final IRepositoryViewObject repObj;

    private Query query;

    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == Query.class) {
            return query;
        }
        return null;
    }

    public QueryRepositoryObject(IRepositoryViewObject repObj, Query table) {
        this.repObj = repObj;
        this.query = table;
    }

    @Override
    public Property getProperty() {
        Property property = repObj.getProperty();
        updataQuery(property);
        return property;
    }

    @Override
    public String getVersion() {
        return repObj.getVersion();
    }

    @Override
    public String getLabel() {
        return query.getLabel();
    }

    @Override
    public String getId() {
        return query.getId();
    }

    public Query getQuery() {
        return query;
    }

    public AbstractMetadataObject getAbstractMetadataObject() {
        return getQuery();
    }

    public void removeFromParent() {
        query.getQueries().getQuery().remove(query);
    }

    private void updataQuery(Property property) {
        if (property == null) {
            return;
        }
        Connection connection = null;
        Item item = property.getItem();
        if (item instanceof ConnectionItem) {
            ConnectionItem cItem = (ConnectionItem) item;
            connection = cItem.getConnection();
        }
        if (connection instanceof DatabaseConnection) {
            DatabaseConnection dbConn = (DatabaseConnection) connection;
            QueriesConnection queries = dbConn.getQueries();
            Iterator iterator = queries.getQuery().iterator();
            while (iterator.hasNext()) {
                Object next = iterator.next();
                if (next instanceof Query && query.getLabel() != null && query.getLabel().equals(((Query) next).getLabel())) {
                    query = (Query) next;
                }
            }
        }

    }

}
