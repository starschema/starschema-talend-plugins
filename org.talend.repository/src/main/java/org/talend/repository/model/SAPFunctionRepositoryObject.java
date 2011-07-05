// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
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
import org.talend.core.model.metadata.builder.connection.SAPConnection;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.repository.model.ISubRepositoryObject;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class SAPFunctionRepositoryObject extends RepositoryObject implements ISubRepositoryObject {

    private SAPFunctionUnit functionUnit;

    private final IRepositoryViewObject repObj;

    public SAPFunctionRepositoryObject(IRepositoryViewObject repObj, RepositoryNode functionNode,
            final SAPFunctionUnit functionUnit) {
        this.repObj = repObj;
        this.functionUnit = functionUnit;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.repository.RepositoryObject#getAdapter(java.lang .Class)
     */
    public Object getAdapter(Class adapter) {
        if (adapter == org.talend.core.model.metadata.builder.connection.MetadataTable.class) {
            return functionUnit.getMetadataTable();
        } else if (adapter == org.talend.core.model.metadata.builder.connection.InputSAPFunctionParameterTable.class) {
            return functionUnit.getInputParameterTable();
        } else if (adapter == org.talend.core.model.metadata.builder.connection.OutputSAPFunctionParameterTable.class) {
            return functionUnit.getOutputParameterTable();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.repository.IRepositoryObject#getType()
     */
    @Override
    public ERepositoryObjectType getRepositoryObjectType() {
        return ERepositoryObjectType.METADATA_SAP_FUNCTION;
    }

    public void setLabel(String value) {
        if (functionUnit.getLabel() == null) {
            functionUnit.setLabel(value);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.repository.RepositoryObject#getLabel()
     */
    @Override
    public String getLabel() {
        return functionUnit.getLabel();
    }

    @Override
    public Property getProperty() {
        Property property = repObj.getProperty();
        updateFunctionUnit(property);
        return property;
    }

    @Override
    public String getVersion() {
        return repObj.getVersion();
    }

    @Override
    public String getId() {
        return functionUnit.getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.ISubRepositoryObject#getAbstractMetadataObject ()
     */
    public AbstractMetadataObject getAbstractMetadataObject() {
        return this.functionUnit;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.ISubRepositoryObject#removeFromParent()
     */
    public void removeFromParent() {
        functionUnit.getConnection().getFuntions().remove(functionUnit);
    }
    
    
    public ERepositoryStatus getRepositoryStatus() {
        return repObj.getRepositoryStatus();
    }

    private void updateFunctionUnit(Property property) {
        if (property == null) {
            return;
        }
        Connection connection = null;
        Item item = property.getItem();
        if (item instanceof ConnectionItem) {
            ConnectionItem cItem = (ConnectionItem) item;
            connection = cItem.getConnection();
        }
        if (connection instanceof SAPConnection) {
            SAPConnection sapConnection = (SAPConnection) connection;
            if (sapConnection.getFuntions() != null) {
                Iterator iterator = sapConnection.getFuntions().iterator();
                while (iterator.hasNext()) {
                    Object fObj = iterator.next();
                    if (fObj instanceof SAPFunctionUnit) {
                        SAPFunctionUnit unit = (SAPFunctionUnit) fObj;
                        if (functionUnit.getLabel() != null && functionUnit.getLabel().equals(unit.getLabel())) {
                            functionUnit = unit;
                        }
                    }
                }
            }
        }

    }

    public ModelElement getModelElement() {
        return this.functionUnit;
    }

}
