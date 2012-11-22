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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.image.ImageUtils;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ICoreService;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.QueriesConnection;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.DocumentationItem;
import org.talend.core.model.properties.InformationLevel;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.LinkDocumentationItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.User;
import org.talend.core.ui.images.RepositoryImageProvider;
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
 * DOC Administrator class global comment. Detailled comment
 */
public class RepositoryViewObject implements IRepositoryViewObject {

    private IRepositoryNode repositoryNode;

    private String id;

    private User author;

    private Date creationDate;

    private ERepositoryObjectType type;

    private String description;

    private Date modificationDate;

    private String label;

    private String purpose;

    private String statusCode;

    private String version;

    private boolean deleted;

    private String projectLabel;

    private String path;

    private ERepositoryStatus repositoryStatus;

    private ERepositoryStatus informationStatus;

    private Image customImage;

    private static ICoreService coreSerivce = (ICoreService) GlobalServiceRegister.getDefault().getService(ICoreService.class);

    private PersistenceException exception;

    public RepositoryViewObject(Property property, boolean avoidGuiInfos) {
        this.id = property.getId();
        this.author = property.getAuthor();
        this.creationDate = property.getCreationDate();
        this.description = property.getDescription();
        this.modificationDate = property.getModificationDate();
        this.label = property.getDisplayName();
        this.purpose = property.getPurpose();
        this.statusCode = property.getStatusCode();
        this.version = property.getVersion();
        this.type = ERepositoryObjectType.getItemType(property.getItem());
        this.deleted = property.getItem().getState().isDeleted();
        org.talend.core.model.properties.Project emfproject = ProjectManager.getInstance().getProject(property.getItem());
        this.projectLabel = emfproject.getLabel();
        this.path = property.getItem().getState().getPath();

        if (GlobalServiceRegister.getDefault().isServiceRegistered(IProxyRepositoryService.class)) {
            IProxyRepositoryService service = (IProxyRepositoryService) GlobalServiceRegister.getDefault().getService(
                    IProxyRepositoryService.class);
            IProxyRepositoryFactory factory = service.getProxyRepositoryFactory();
            repositoryStatus = factory.getStatus(property.getItem());
            InformationLevel informationLevel = property.getMaxInformationLevel();
            informationStatus = factory.getStatus(informationLevel);
        }
        if (avoidGuiInfos) {
            if (type == ERepositoryObjectType.JOBLET) {
                JobletProcessItem item = (JobletProcessItem) property.getItem();
                if (item.getIcon() != null && item.getIcon().getInnerContent() != null
                        && item.getIcon().getInnerContent().length == 0) {
                    customImage = getJobletCustomIcon(property);
                    customImage = ImageUtils.propertyLabelScale(property.getId(), customImage, ICON_SIZE.ICON_16);
                }
            } else if (type == ERepositoryObjectType.DOCUMENTATION) {
                Item item = property.getItem();
                if (item instanceof DocumentationItem) {
                    customImage = coreSerivce.getImageWithDocExt(((DocumentationItem) item).getExtension());
                } else if (item instanceof LinkDocumentationItem) {
                    customImage = coreSerivce.getImageWithSpecial(
                            coreSerivce.getImageWithDocExt(((LinkDocumentationItem) item).getExtension())).createImage();
                }
            }
        }
    }

    public RepositoryViewObject(Property property) {
        this(property, false);
    }

    /**
     * DOC bqian Comment method "getJobletCustomIcon".
     * 
     * @param property
     * @return
     */
    public static Image getJobletCustomIcon(Property property) {
        JobletProcessItem item = (JobletProcessItem) property.getItem();
        Image image = null;
        if (item.getIcon() == null || item.getIcon().getInnerContent() == null || item.getIcon().getInnerContent().length == 0) {
            // File image = RepositoryLabelProvider.getDefaultJobletImage();
            // try {
            // item.getIcon().setInnerContentFromFile(image);
            // } catch (Exception e) {
            // ExceptionHandler.process(e);
            // }

            image = getDefaultJobletImage();
        } else {

            ImageDescriptor imageDesc = ImageUtils.createImageFromData(item.getIcon().getInnerContent());
            imageDesc = ImageUtils.scale(imageDesc, ICON_SIZE.ICON_32);

            image = cachedImages.get(item.getIcon().getInnerContent());
            if (image == null || image.isDisposed()) {
                image = imageDesc.createImage();
                cachedImages.put(item.getIcon().getInnerContent(), image);
            } else {
                // image = imageDesc.createImage();
            }
        }
        return image;
    }

    public static Image getDefaultJobletImage() {
        return ImageProvider.getImage(ECoreImage.JOBLET_COMPONENT_ICON);
    }

    private static Map<byte[], Image> cachedImages = new HashMap<byte[], Image>();

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RepositoryObject) && !(obj instanceof RepositoryViewObject)) {
            return false;
        }
        if (id == null) {
            return super.equals(obj);
        }
        if (obj instanceof RepositoryObject) {
            RepositoryObject another = (RepositoryObject) obj;
            return id.equals(another.getId());
        } else {
            RepositoryViewObject another = (RepositoryViewObject) obj;
            return id.equals(another.getId());
        }

    }

    public User getAuthor() {
        return this.author;
    }

    public Date getCreationDate() {
        return this.creationDate;
    }

    public String getDescription() {
        return this.description;
    }

    public String getId() {
        return this.id;
    }

    public Date getModificationDate() {
        return this.modificationDate;
    }

    public String getLabel() {
        return this.label;
    }

    public String getPurpose() {
        return this.purpose;
    }

    public String getStatusCode() {
        return this.statusCode;
    }

    public String getVersion() {
        return this.version;
    }

    public Property getProperty() {
        exception = null;
        try {
            IProxyRepositoryFactory factory = null;
            if (!GlobalServiceRegister.getDefault().isServiceRegistered(IProxyRepositoryService.class)) {
                return null;
            }
            factory = ((IProxyRepositoryService) GlobalServiceRegister.getDefault().getService(IProxyRepositoryService.class))
                    .getProxyRepositoryFactory();

            IRepositoryViewObject object = factory.getLastVersion(id);
            if (object == null) {
                throw new PersistenceException("item with name:" + label + " / id:" + id
                        + " not found !\n\nPlease Refresh the repository.");
            }
            this.customImage = null;
            Property property = object.getProperty();
            this.id = property.getId();
            this.author = property.getAuthor();
            this.creationDate = property.getCreationDate();
            this.description = property.getDescription();
            this.modificationDate = property.getModificationDate();
            this.label = property.getDisplayName();
            this.purpose = property.getPurpose();
            this.statusCode = property.getStatusCode();
            this.version = property.getVersion();
            this.type = ERepositoryObjectType.getItemType(property.getItem());
            this.deleted = property.getItem().getState().isDeleted();
            this.path = property.getItem().getState().getPath();
            repositoryStatus = factory.getStatus(property.getItem());
            InformationLevel informationLevel = property.getMaxInformationLevel();
            informationStatus = factory.getStatus(informationLevel);
            if (type == ERepositoryObjectType.JOBLET) {
                JobletProcessItem item = (JobletProcessItem) property.getItem();
                if (item.getIcon() != null && item.getIcon().getInnerContent() != null
                        && item.getIcon().getInnerContent().length == 0) {
                    customImage = getJobletCustomIcon(property);
                    customImage = ImageUtils.propertyLabelScale(property.getId(), customImage, ICON_SIZE.ICON_16);
                }
            } else if (type == ERepositoryObjectType.DOCUMENTATION) {
                this.customImage = ImageProvider.getImage(RepositoryImageProvider.getIcon(type));
                Item item = property.getItem();
                if (item instanceof DocumentationItem) {
                    customImage = coreSerivce.getImageWithDocExt(((DocumentationItem) item).getExtension());
                } else if (item instanceof LinkDocumentationItem) {
                    customImage = coreSerivce.getImageWithSpecial(customImage).createImage();
                }
            }
            return property;
        } catch (PersistenceException e) {
            exception = e;
            ExceptionHandler.process(e);
            // if (!CommonsPlugin.isHeadless() && PlatformUI.isWorkbenchRunning()) {
            // MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", exception.getMessage());
            // }
        }
        return null;
    }

    public ERepositoryObjectType getRepositoryObjectType() {
        return type;
    }

    public List<IRepositoryViewObject> getChildren() {
        List<IRepositoryViewObject> toReturn = new ArrayList<IRepositoryViewObject>();
        return toReturn;
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

    public RepositoryViewObject cloneNewObject() {
        RepositoryViewObject object = null;
        try {
            Property connectionProperty = PropertiesFactory.eINSTANCE.createProperty();
            connectionProperty.setAuthor(getAuthor());
            connectionProperty.setCreationDate(getCreationDate());
            connectionProperty.setDescription(getDescription());
            connectionProperty.setId(getId());
            connectionProperty.setLabel(getLabel());
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
            object = new RepositoryViewObject(connectionProperty);
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
     * @see org.talend.core.model.repository.IRepositoryViewObject#isDeleted()
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Getter for projectLabel.
     * 
     * @return the projectLabel
     */
    public String getProjectLabel() {
        return this.projectLabel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.repository.IRepositoryViewObject#getPath()
     */
    public String getPath() {
        return path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.repository.IRepositoryViewObject#getInformationStatus()
     */
    public ERepositoryStatus getInformationStatus() {
        return informationStatus;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.repository.IRepositoryViewObject#getRepositoryStatus()
     */
    public ERepositoryStatus getRepositoryStatus() {
        return repositoryStatus;
    }

    /**
     * Getter for customImage.
     * 
     * @return the customImage
     */
    public Image getCustomImage() {
        return this.customImage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.repository.IRepositoryViewObject#throwPersistenceExceptionIfAny()
     */
    public void throwPersistenceExceptionIfAny() throws PersistenceException {
        if (exception != null) {
            throw exception;
        }
    }
}
