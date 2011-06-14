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
package org.talend.repository.ui.swt.utils;

import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.repository.ui.utils.FileConnectionContextUtils.EFileParamName;
import org.talend.repository.ui.utils.OtherConnectionContextUtils.EParamName;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.FOXTreeNode;

/**
 * DOC cantoine class global comment. Detailled comment <br/>
 * 
 * $Id: AbstractXmlFileStepForm.java 48226 2010-09-14 10:04:12Z hywang $
 * 
 */
public abstract class AbstractXmlFileStepForm extends AbstractXmlStepForm {

    protected XmlFileConnection connection;

    /**
     * DOC cantoine AbstractXmlFileStepForm constructor comment. Use to step1
     */
    public AbstractXmlFileStepForm(Composite parent, ConnectionItem connectionItem, String[] existingNames) {
        super(parent, SWT.NONE, existingNames);
        setConnectionItem(connectionItem);
    }

    /**
     * DOC cantoine AbstractXmlFileStepForm constructor comment. Use to step2
     * 
     * @param parent
     * @param connection2
     */
    public AbstractXmlFileStepForm(Composite parent, ConnectionItem connectionItem) {
        this(parent, connectionItem, null);
    }

    /**
     * DOC cantoine AbstractDelimitedFileStepForm constructor comment. Use to step1
     */
    public AbstractXmlFileStepForm(Composite parent, ConnectionItem connectionItem, MetadataTable metadataTable,
            String[] existingNames) {
        super(parent, SWT.NONE, existingNames);
        setConnectionItem(connectionItem);
    }

    protected XmlFileConnection getConnection() {
        return (XmlFileConnection) connectionItem.getConnection();
    }

    @Override
    protected void exportAsContext() {
        collectConnParams();
        super.exportAsContext();
    }

    protected void collectConnParams() {
        if (getConnection().isInputModel()) {
            addContextParams(EFileParamName.File, true);
            addContextParams(EFileParamName.Encoding, true);
            addContextParams(EParamName.XPathQuery, true);
        } else {
            addContextParams(EParamName.OutputFilePath, true);
        }

    }

    public void redrawLinkers() {
    }

    public void updateConnection() {
    }

    public void updateStatus() {
    }

    public List<FOXTreeNode> getTreeData() {
        return null;
    }

    public void setSelectedText(String label) {
    }

    public MetadataTable getMetadataTable() {
        return null;
    }

    public TableViewer getSchemaViewer() {
        return null;
    }

    @Override
    public MetadataTable getMetadataOutputTable() {
        return null;
    }

}
