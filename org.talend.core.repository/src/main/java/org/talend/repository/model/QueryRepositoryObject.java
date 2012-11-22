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
package org.talend.repository.model;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.talend.core.model.metadata.builder.connection.AbstractMetadataObject;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.QueriesConnection;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.User;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ISubRepositoryObject;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class QueryRepositoryObject extends org.talend.core.model.metadata.Query implements ISubRepositoryObject {

    private final IRepositoryViewObject repObj;

    private Query query;

    public QueryRepositoryObject(IRepositoryViewObject repObj, Query table) {
        this.repObj = repObj;
        this.query = table;
    }

    public Property getProperty() {
        Property property = repObj.getProperty();
        updateQuery(property);
        return property;
    }

    public String getVersion() {
        return repObj.getVersion();
    }

    public String getLabel() {
        return query.getLabel();
    }

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

    private void updateQuery(Property property) {
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

    public User getAuthor() {
        return repObj.getAuthor();
    }

    public List<IRepositoryViewObject> getChildren() {
        return repObj.getChildren();
    }

    public Date getCreationDate() {
        return repObj.getCreationDate();
    }

    public String getDescription() {
        return repObj.getDescription();
    }

    public ERepositoryStatus getInformationStatus() {
        return repObj.getInformationStatus();
    }

    public Date getModificationDate() {
        return repObj.getModificationDate();
    }

    public String getPath() {
        return repObj.getPath();
    }

    public String getProjectLabel() {
        return repObj.getProjectLabel();
    }

    public String getPurpose() {
        return repObj.getPurpose();
    }

    public IRepositoryNode getRepositoryNode() {
        return repObj.getRepositoryNode();
    }

    public ERepositoryStatus getRepositoryStatus() {
        return repObj.getRepositoryStatus();
    }

    public String getStatusCode() {
        return repObj.getStatusCode();
    }

    public ERepositoryObjectType getRepositoryObjectType() {
        return ERepositoryObjectType.METADATA_CON_QUERY;
    }

    public boolean isDeleted() {
        return repObj.isDeleted();
    }

    public void setRepositoryNode(IRepositoryNode node) {
        repObj.setRepositoryNode(node);
    }

    public ModelElement getModelElement() {
        return this.query;
    }

}
