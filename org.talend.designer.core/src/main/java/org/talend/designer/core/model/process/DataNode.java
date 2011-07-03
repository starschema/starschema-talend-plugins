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
package org.talend.designer.core.model.process;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.AbstractNode;

/**
 * Virtual node that will be used for the generated code.
 * 
 * $Id: DataNode.java 54939 2011-02-11 01:34:57Z mhirt $
 * 
 */
public class DataNode extends AbstractNode {

    public DataNode(IComponent component, String uniqueName) {
        setComponentName(component.getName());
        setPluginFullName(component.getPluginFullName());
        List<IMetadataTable> metaList = new ArrayList<IMetadataTable>();
        IMetadataTable metaTable = new MetadataTable();
        metaTable.setTableName(uniqueName);
        metaList.add(metaTable);
        setMetadataList(metaList);
        setComponent(component);
        setElementParameters(component.createElementParameters(this));
        setListConnector(component.createConnectors(this));
        setUniqueName(uniqueName);
        setHasConditionalOutputs(component.hasConditionalOutputs());
        setIsMultiplyingOutputs(component.isMultiplyingOutputs());
    }

    public DataNode() {
        // nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#renameMetadataColumnName(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    public void metadataInputChanged(IODataComponent dataComponent, String connectionToApply) {
        // TODO Auto-generated method stub

    }

    public void metadataOutputChanged(IODataComponent dataComponent, String connectionToApply) {
        // TODO Auto-generated method stub

    }
}
