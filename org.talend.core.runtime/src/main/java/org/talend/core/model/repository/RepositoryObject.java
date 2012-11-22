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
package org.talend.core.model.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.QueriesConnection;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.InformationLevel;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.User;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.PackageHelper;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IProxyRepositoryService;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryService;
import orgomg.cwm.objectmodel.core.Package;
import orgomg.cwm.resource.relational.Catalog;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class RepositoryObject implements IRepositoryObject {

    protected Property property = PropertiesFactory.eINSTANCE.createProperty();

    private IRepositoryNode repositoryNode;

    public RepositoryObject() {
    }

    public RepositoryObject(Property property) {
        this.property = property;
    }

    public Property getProperty() {
        return this.property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public RepositoryObject(String id, String label) {
        this.setId(id);
        this.setLabel(label);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RepositoryObject)) {
            return false;
        }
        if (property == null) {
            return super.equals(obj);
        }
        RepositoryObject another = (RepositoryObject) obj;

        return getProperty().equals(another.getProperty());
    }

    public User getAuthor() {
        return this.property.getAuthor();
    }

    public Date getCreationDate() {
        return this.property.getCreationDate();
    }

    public String getDescription() {
        return this.property.getDescription();
    }

    public String getId() {
        return this.property.getId();
    }

    public Date getModificationDate() {
        return this.property.getModificationDate();
    }

    public String getLabel() {
        return this.property.getLabel();
    }

    public String getDisplayName() {
        return this.property.getDisplayName();
    }

    public String getPurpose() {
        return this.property.getPurpose();
    }

    public String getStatusCode() {
        return this.property.getStatusCode();
    }

    public String getVersion() {
        return this.property.getVersion();
    }

    public void setAuthor(User value) {
        this.property.setAuthor(value);
    }

    public void setCreationDate(Date value) {
        this.property.setCreationDate(value);
    }

    public void setDescription(String value) {
        this.property.setDescription(value);
    }

    public void setId(String value) {
        this.property.setId(value);
    }

    public void setModificationDate(Date value) {
        this.property.setModificationDate(value);
    }

    public void setLabel(String value) {
        this.property.setLabel(value);
    }

    public void setPurpose(String value) {
        this.property.setPurpose(value);
    }

    public void setStatusCode(String value) {
        this.property.setStatusCode(value);
    }

    public void setVersion(String value) {
        this.property.setVersion(value);
    }

    public ERepositoryObjectType getRepositoryObjectType() {
        return ERepositoryObjectType.getItemType(this.property.getItem());
    }

    public List<IRepositoryViewObject> getChildren() {
        List<IRepositoryViewObject> toReturn = new ArrayList<IRepositoryViewObject>();
        return toReturn;
    }

    @SuppressWarnings("unchecked")
    public RepositoryObject cloneNewObject() {
        RepositoryObject object = new RepositoryObject();
        try {
            Property connectionProperty = PropertiesFactory.eINSTANCE.createProperty();
            connectionProperty.setAuthor(getAuthor());
            connectionProperty.setCreationDate(getCreationDate());
            connectionProperty.setDescription(getDescription());
            connectionProperty.setId(getId());
            connectionProperty.setLabel(getLabel());
            connectionProperty.setDisplayName(getDisplayName());
            connectionProperty.setModificationDate(getModificationDate());
            connectionProperty.setPurpose(getPurpose());
            connectionProperty.setStatusCode(getStatusCode());
            connectionProperty.setVersion(getVersion());
            final Item oldItem = getProperty().getItem();
            DatabaseConnectionItem newItem = null;
            if (oldItem instanceof DatabaseConnectionItem) {
                DatabaseConnectionItem item = (DatabaseConnectionItem) oldItem;
                newItem = PropertiesFactory.eINSTANCE.createDatabaseConnectionItem();
                newItem.setProperty(connectionProperty);
                ItemState state = PropertiesFactory.eINSTANCE.createItemState();
                // state.setCommitDate(oldItem.getState().getCommitDate());
                // state.setDeleted(oldItem.getState().isDeleted());
                // state.setLockDate(oldItem.getState().getLockDate());
                // state.setLocked(oldItem.getState().isLocked());
                // state.setLocker(oldItem.getState().getLocker());
                state.setPath(oldItem.getState().getPath());
                newItem.setState(state);

                final DatabaseConnection connection = (DatabaseConnection) item.getConnection();

                DatabaseConnection conn = null;
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IRepositoryService.class)) {
                    IRepositoryService service = (IRepositoryService) GlobalServiceRegister.getDefault().getService(
                            IRepositoryService.class);
                    conn = service.cloneOriginalValueConnection(connection);
                }

                final QueriesConnection queries = connection.getQueries();
                QueriesConnection newQ = null;
                if (queries != null) {
                    newQ = ConnectionFactory.eINSTANCE.createQueriesConnection();
                    newQ.setConnection(conn);
                    final List<Query> query = queries.getQuery();
                    List<Query> queries2 = new ArrayList<Query>();
                    for (Query query2 : query) {
                        Query newQuery = ConnectionFactory.eINSTANCE.createQuery();
                        newQuery.setProperties(query2.getProperties());
                        newQuery.setComment(query2.getComment());
                        newQuery.setDivergency(query2.isDivergency());
                        newQuery.setId(query2.getId());
                        newQuery.setLabel(query2.getLabel());
                        newQuery.setQueries(newQ);
                        // newQuery.setReadOnly(query2.isReadOnly());
                        newQuery.setSynchronised(query2.isSynchronised());
                        newQuery.setValue(query2.getValue());
                        newQuery.setContextMode(query2.isContextMode());
                        queries2.add(newQuery);
                    }
                    newQ.getQuery().addAll(queries2);
                }

                final Set<MetadataTable> tables = ConnectionHelper.getTables(connection);
                List<MetadataTable> newTs = null;
                if (tables != null) {
                    newTs = new ArrayList<MetadataTable>();
                    for (MetadataTable table : tables) {
                        MetadataTable table2 = ConnectionFactory.eINSTANCE.createMetadataTable();
                        table2.setProperties(table.getProperties());
                        table2.setComment(table.getComment());
                        if (table2.getNamespace() instanceof Package) { // hywang
                            // remove
                            // setconn
                            Package pkg = (Package) table2.getNamespace();
                            pkg.getDataManager().add(conn);
                        }
                        // table2.setConnection(conn);
                        table2.setDivergency(table.isDivergency());
                        table2.setId(table.getId());
                        table2.setLabel(table.getLabel());
                        // table2.setReadOnly(table.isReadOnly());
                        table2.setSourceName(table.getSourceName());
                        table2.setSynchronised(table.isSynchronised());
                        table2.setTableType(table.getTableType());
                        List<MetadataColumn> list = new ArrayList<MetadataColumn>();
                        for (MetadataColumn column : (List<MetadataColumn>) table.getColumns()) {
                            MetadataColumn column2 = ConnectionFactory.eINSTANCE.createMetadataColumn();
                            column2.setProperties(column.getProperties());
                            column2.setComment(column.getComment());
                            column2.setDefaultValue(column.getDefaultValue());
                            column2.setDivergency(column.isDivergency());
                            column2.setId(column.getId());
                            column2.setKey(column.isKey());
                            column2.setLabel(column.getLabel());
                            column2.setLength(column.getLength());
                            column2.setNullable(column.isNullable());
                            column2.setOriginalField(column.getOriginalField());
                            column2.setPattern(column.getPattern());
                            column2.setPrecision(column.getPrecision());
                            // column2.setReadOnly(column.isReadOnly());
                            column2.setSourceType(column.getSourceType());
                            column2.setSynchronised(column.isSynchronised());
                            column2.setTable(table2);
                            column2.setTalendType(column.getTalendType());
                            list.add(column2);
                        }
                        table2.getColumns().addAll(list);
                        newTs.add(table2);
                    }
                }
                Catalog c = (Catalog) ConnectionHelper.getPackage(conn.getSID(), (Connection) conn, Catalog.class);
                if (c != null) {
                    PackageHelper.addMetadataTable(newTs, c);
                    c.getOwnedElement().addAll(newTs);
                }
                conn.setQueries(newQ);
                // conn.getTables().addAll(newTs);
                newItem.setConnection(conn);
            }
            connectionProperty.setItem(newItem);
            object.setProperty(connectionProperty);
        } catch (Exception e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
            // do notbing.
        }
        return object;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.repository.IRepositoryObject#getRepositoryNode()
     */
    public IRepositoryNode getRepositoryNode() {
        return this.repositoryNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.core.model.repository.IRepositoryObject#setRepositoryNode(org.talend.repository.model.RepositoryNode)
     */
    public void setRepositoryNode(IRepositoryNode node) {
        this.repositoryNode = node;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.repository.IRepositoryViewObject#isDeleted()
     */
    public boolean isDeleted() {
        return property.getItem().getState().isDeleted();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.repository.IRepositoryViewObject#getProjectLabel()
     */
    public String getProjectLabel() {
        org.talend.core.model.properties.Project emfproject = ProjectManager.getInstance().getProject(property.getItem());
        return emfproject.getLabel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.repository.IRepositoryViewObject#getPath()
     */
    public String getPath() {
        return property.getItem().getState().getPath();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.repository.IRepositoryViewObject#getInformationStatus()
     */
    public ERepositoryStatus getInformationStatus() {

        IService service = GlobalServiceRegister.getDefault().getService(IProxyRepositoryService.class);
        IProxyRepositoryFactory factory = ((IProxyRepositoryService) service).getProxyRepositoryFactory();
        InformationLevel informationLevel = property.getMaxInformationLevel();
        return factory.getStatus(informationLevel);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.repository.IRepositoryViewObject#getRepositoryStatus()
     */
    public ERepositoryStatus getRepositoryStatus() {
        IService service = GlobalServiceRegister.getDefault().getService(IProxyRepositoryService.class);
        IProxyRepositoryFactory factory = ((IProxyRepositoryService) service).getProxyRepositoryFactory();
        return factory.getStatus(property.getItem());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.repository.IRepositoryViewObject#throwPersistenceExceptionIfAny()
     */
    public void throwPersistenceExceptionIfAny() throws PersistenceException {
        // can't have any exception here...
    }

    @Override
    public String toString() {
        if (property != null) {
            String label = "";
            if (property.getItem() != null) {
                ERepositoryObjectType type = ERepositoryObjectType.getItemType(property.getItem());
                label = "[" + type.getKey() + "] ";
            }
            label += property.getLabel();
            return label;
        }
        return super.toString();
    }

    public void unload() {
        Item item = property.getItem();
        if (item.getParent() != null && item.getParent() instanceof FolderItem) {
            ((FolderItem) item.getParent()).getChildren().remove(item);
            item.setParent(null);
        }
        property = null;
    }
}
