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
package org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTool;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.properties.controllers.TableController;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 * $Id: MetadataTableEditor.java 801 2006-11-30 16:28:36Z amaumont $
 * 
 * @param <B>
 */
public class PropertiesTableEditorModel<B> extends ExtendedTableModel<B> {

    private IElement element;

    private IElementParameter elemParameter;

    private IProcess process;

    private boolean dynamicData;

    /**
     * DOC amaumont PropertiesTableEditorModel constructor comment.
     */
    public PropertiesTableEditorModel() {
        super();
    }

    public PropertiesTableEditorModel(String titleName) {
        super(titleName);
    }

    public void setData(IElement element, IElementParameter elemParameter, IProcess process) {
        this.element = element;
        this.process = process;
        this.elemParameter = elemParameter;
        registerDataList((List<B>) elemParameter.getValue());
    }

    public String getTitleName() {
        return super.getName();
    }

    public B createNewEntry() {
        return (B) TableController.createNewLine(elemParameter);
    }

    /**
     * Getter for dynamicData.
     * 
     * @return the dynamicData
     */
    public boolean isDynamicData() {
        return dynamicData;
    }

    /**
     * Getter for element.
     * 
     * @return the element
     */
    public IElement getElement() {
        return this.element;
    }

    /**
     * Getter for elemParameter.
     * 
     * @return the elemParameter
     */
    public IElementParameter getElemParameter() {
        return this.elemParameter;
    }

    /**
     * Getter for items.
     * 
     * @return the items
     */
    public String[] getItems() {
        return (String[]) elemParameter.getListItemsDisplayCodeName();
    }

    /**
     * Getter for itemsNotShowIf.
     * 
     * @return the itemsNotShowIf
     */
    public String[] getItemsNotShowIf() {
        return (String[]) elemParameter.getListItemsNotShowIf();
    }

    /**
     * Getter for itemsShowIf.
     * 
     * @return the itemsShowIf
     */
    public String[] getItemsShowIf() {
        return (String[]) elemParameter.getListItemsShowIf();
    }

    /**
     * Getter for itemsValue.
     * 
     * @return the itemsValue
     */
    public Object[] getItemsValue() {
        return (Object[]) elemParameter.getListItemsValue();
    }

    /**
     * Getter for process.
     * 
     * @return the process
     */
    public IProcess getProcess() {
        return this.process;
    }

    /**
     * Getter for titles.
     * 
     * @return the titles
     */
    public String[] getTitles() {
        return elemParameter.getListItemsDisplayName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.ui.swt.extended.table.ExtendedTableModel#remove(java.lang.Object)
     */
    @Override
    public boolean remove(B bean) {
        return super.remove(bean);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.ui.swt.extended.table.ExtendedTableModel#removeAll(java.util.Collection)
     */
    @Override
    public boolean removeAll(Collection<B> c) {
        boolean cancel = false;
        String schemaType = null;
        for (Object object : elemParameter.getListItemsValue()) {
            if (object instanceof ElementParameter) {
                ElementParameter param = (ElementParameter) object;
                if (param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)) {
                    schemaType = param.getName();
                    cancel = !MessageDialog.openQuestion(this.getTableViewer().getTable().getShell(), Messages
                            .getString("PropertiesTableEditorModel.removeSchema"), //$NON-NLS-1$
                            Messages.getString("PropertiesTableEditorModel.popWindow")); //$NON-NLS-1$
                }
            }
        }

        if (cancel) {
            return false;
        }
        if (schemaType != null) {
            INode node = ((INode) elemParameter.getElement());
            List<IMetadataTable> metadatasToRemove = new ArrayList<IMetadataTable>();
            for (Map<String, Object> line : (List<Map<String, Object>>) c) {
                String schemaName = (String) line.get(schemaType);
                IMetadataTable metadata = MetadataTool.getMetadataTableFromNode(node, schemaName);
                if (metadata != null) {
                    metadatasToRemove.add(metadata);
                    IDesignerCoreService service = (IDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                            IDesignerCoreService.class);
                    if (service != null) {
                        service.removeConnection(node, metadata.getTableName());
                    }
                }
            }
            node.getMetadataList().removeAll(metadatasToRemove);
        }
        return super.removeAll(c);
    }

}
