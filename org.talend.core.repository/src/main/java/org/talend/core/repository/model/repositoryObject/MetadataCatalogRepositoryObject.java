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
package org.talend.core.repository.model.repositoryObject;

import java.util.Date;
import java.util.List;

import org.talend.core.model.metadata.MetadataCatalog;
import org.talend.core.model.metadata.builder.connection.AbstractMetadataObject;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.User;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ISubRepositoryObject;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IRepositoryNode;
import orgomg.cwm.objectmodel.core.ModelElement;
import orgomg.cwm.resource.relational.Catalog;


/**
 * DOC klliu  class global comment. Detailled comment
 */
public class MetadataCatalogRepositoryObject extends MetadataCatalog implements ISubRepositoryObject {

    private final IRepositoryViewObject viewObject;

    public IRepositoryViewObject getViewObject() {
        return this.viewObject;
    }

    private final Catalog catalog;

    private IRepositoryNode repositoryNode;

    public Catalog getCatalog() {
        return catalog;
    }

    /**
     * DOC klliu MetadataCatalogRepositoryObject constructor comment.
     * 
     * Arguments must not be null.
     * 
     * @param repositoryViewObject
     * @param catalog
     */
    public MetadataCatalogRepositoryObject(IRepositoryViewObject repositoryViewObject, Catalog catalog) {
        assert catalog != null;
        assert repositoryViewObject != null;

        this.viewObject = repositoryViewObject;
        this.catalog = catalog;
    }

    public Property getProperty() {
        Property property = viewObject.getProperty();
        // update table
        updateCatalog(property);
        return property;
    }

    // @Override
    public String getVersion() {
        return viewObject.getVersion();
    }

    public String getLabel() {
        return getCatalog().getName();
    }

    public String getId() {
        return getCatalog().getName();
    }

    public AbstractMetadataObject getAbstractMetadataObject() {
        return null;
    }

    public void removeFromParent() {
    }

    private void updateCatalog(Property property) {
        // FIXME empty method ?
    }

    public User getAuthor() {
        return viewObject.getAuthor();
    }

    public List<IRepositoryViewObject> getChildren() {
        return this.viewObject.getChildren();
    }

    public Date getCreationDate() {
        return viewObject.getCreationDate();
    }

    public String getDescription() {
        return viewObject.getDescription();
    }

    public ERepositoryStatus getInformationStatus() {
        return viewObject.getInformationStatus();
    }

    public Date getModificationDate() {
        return viewObject.getModificationDate();
    }

    public String getPath() {
        return viewObject.getPath();
    }

    public String getProjectLabel() {
        return viewObject.getProjectLabel();
    }

    public String getPurpose() {
        return viewObject.getPurpose();
    }

    public IRepositoryNode getRepositoryNode() {
        return this.repositoryNode;
    }

    public ERepositoryStatus getRepositoryStatus() {
        return viewObject.getRepositoryStatus();
    }

    public String getStatusCode() {
        return viewObject.getStatusCode();
    }

    public ERepositoryObjectType getRepositoryObjectType() {
        return ERepositoryObjectType.METADATA_CON_CATALOG;
    }

    public boolean isDeleted() {
        return false;
    }

    public void setRepositoryNode(IRepositoryNode node) {
        this.repositoryNode = node;
    }

    public ModelElement getModelElement() {
        return this.catalog;
    }

}
