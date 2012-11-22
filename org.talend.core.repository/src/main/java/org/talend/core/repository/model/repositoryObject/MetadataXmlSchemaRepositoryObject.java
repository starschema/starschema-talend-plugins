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

import org.talend.core.model.metadata.MetadataXmlSchema;
import org.talend.core.model.metadata.builder.connection.AbstractMetadataObject;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.User;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ISubRepositoryObject;
import org.talend.cwm.xml.TdXmlSchema;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IRepositoryNode;
import orgomg.cwm.objectmodel.core.ModelElement;


/**
 * DOC klliu  class global comment. Detailled comment
 */
public class MetadataXmlSchemaRepositoryObject extends MetadataXmlSchema implements ISubRepositoryObject {

    private final IRepositoryViewObject viewObject;

    private IRepositoryNode repositoryNode;

    private final TdXmlSchema tdXmlSchema;

    public IRepositoryViewObject getViewObject() {
        return this.viewObject;
    }

    public IRepositoryNode getRepositoryNode() {
        return this.repositoryNode;
    }
    
    public void setRepositoryNode(IRepositoryNode repositoryNode) {
        this.repositoryNode = repositoryNode;
    }

    public TdXmlSchema getTdXmlSchema() {
        return this.tdXmlSchema;
    }

    public List<IRepositoryViewObject> getChildren() {
        return this.viewObject.getChildren();
    }

    /**
     * DOC klliu MetadataXmlSchemaRepositoryObject constructor comment.
     * 
     * @param repositoryViewObject
     * @param tdXmlSchema
     */
    public MetadataXmlSchemaRepositoryObject(IRepositoryViewObject repositoryViewObject, TdXmlSchema tdXmlSchema) {
        this.viewObject = repositoryViewObject;
        this.tdXmlSchema = tdXmlSchema;
    }

    public String getLabel() {
        return getTdXmlSchema().getName();
    }

    public String getId() {
        return getTdXmlSchema().getName();
    }

    public AbstractMetadataObject getAbstractMetadataObject() {
        return null;
    }

    public ERepositoryObjectType getRepositoryObjectType() {
        return ERepositoryObjectType.MDM_SCHEMA;
    }

    public boolean isDeleted() {
        return false;
    }

    public void removeFromParent() {
    }

    private void updateCatalog(Property property) {
    }

    // @Override
    public Property getProperty() {
        Property property = viewObject.getProperty();
        updateCatalog(property);
        return property;
    }

    public String getVersion() {
        return viewObject.getVersion();
    }

    public User getAuthor() {
        return viewObject.getAuthor();
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

    public ERepositoryStatus getRepositoryStatus() {
        return viewObject.getRepositoryStatus();
    }

    public String getStatusCode() {
        return viewObject.getStatusCode();
    }

    public ModelElement getModelElement() {
        return this.tdXmlSchema;
    }

}
