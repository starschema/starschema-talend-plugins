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
package org.talend.core.model.metadata;

import java.util.Date;
import java.util.List;

import org.talend.core.model.metadata.builder.connection.AbstractMetadataObject;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.User;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ISubRepositoryObject;
import org.talend.cwm.helper.SubItemHelper;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IRepositoryNode;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * DOC klliu class global comment. Detailled comment
 */
public class MetadataColumnRepositoryObject extends MetadataColumn implements ISubRepositoryObject {

    private final IRepositoryViewObject viewObject;

    private IRepositoryNode repositoryNode;

    public IRepositoryViewObject getViewObject() {
        return this.viewObject;
    }

    private org.talend.core.model.metadata.builder.connection.MetadataColumn tdColumn;

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.metadata.ITDQMetadataColumn#getTdColumn()
     */
    public org.talend.core.model.metadata.builder.connection.MetadataColumn getTdColumn() {
        return this.tdColumn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.metadata.ITDQMetadataColumn#setTdColumn()
     */
    public void setTdColumn(org.talend.core.model.metadata.builder.connection.MetadataColumn tdColumn) {
        this.tdColumn = tdColumn;
    }

    /**
     * DOC klliu TDQMetadataColumnRepositoryObject constructor comment.
     */
    public MetadataColumnRepositoryObject(IRepositoryViewObject repositoryViewObject,
            org.talend.core.model.metadata.builder.connection.MetadataColumn column) {
        this.viewObject = repositoryViewObject;
        setTdColumn(column);
    }

    public Property getProperty() {
        Property property = viewObject.getProperty();
        // update column
        updateColumn(property);
        return property;
    }

    // @Override
    public String getVersion() {
        return viewObject.getVersion();
    }

    public String getLabel() {
        return getTdColumn().getLabel();
    }

    public void setLabel(String label) {
        this.getTdColumn().setLabel(label);
    }

    public String getId() {
        return getTdColumn().getId();
    }

    public void setId(String id) {
        getTdColumn().setId(id);
    }

    public AbstractMetadataObject getAbstractMetadataObject() {
        return getTdColumn();
    }

    public void removeFromParent() {
    }

    private void updateColumn(Property property) {
    }

    public User getAuthor() {
        return viewObject.getAuthor();
    }

    public List<IRepositoryViewObject> getChildren() {
        return viewObject.getChildren();
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
        return ERepositoryObjectType.METADATA_CON_COLUMN;
    }

    public boolean isDeleted() {
        return SubItemHelper.isDeleted(getTdColumn());
    }

    public void setRepositoryNode(IRepositoryNode node) {
        this.repositoryNode = node;
    }

    public ModelElement getModelElement() {
        return this.tdColumn;
    }

}
