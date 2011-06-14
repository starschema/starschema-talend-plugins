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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.FOXTreeNode;

/**
 * wchen class global comment. Detailled comment
 */
public abstract class AbstractXmlStepForm extends AbstractForm {

    /**
     * wchen AbstractXmlStepForm constructor comment.
     * 
     * @param parent
     * @param style
     */
    public AbstractXmlStepForm(Composite parent, int style, String[] existingNames) {
        super(parent, style, existingNames);
    }

    public TreeViewer getTreeViewer() {
        return null;
    }

    public abstract void redrawLinkers();

    public abstract void updateConnection();

    public abstract void updateStatus();

    public abstract List<FOXTreeNode> getTreeData();

    public abstract void setSelectedText(String label);

    public abstract MetadataTable getMetadataTable();

    public abstract MetadataTable getMetadataOutputTable();

    public abstract TableViewer getSchemaViewer();

}
