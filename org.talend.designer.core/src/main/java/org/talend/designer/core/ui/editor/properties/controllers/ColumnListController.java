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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.core.model.metadata.ColumnNameChanged;
import org.talend.core.model.metadata.ColumnNameChangedExt;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.MetadataTool;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.EDIFACTColumn;
import org.talend.core.model.metadata.builder.connection.EDIFACTConnection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.properties.tab.IDynamicProperty;
import org.talend.core.utils.KeywordsValidator;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 * 
 * $Id: ColumnListController.java 1 2006-12-12 下午02:04:32 +0000 (下午02:04:32) yzhang $
 * 
 */
public class ColumnListController extends AbstractElementPropertySectionController {

    private boolean updateColumnListFlag;

    /**
     * DOC dev ColumnListController constructor comment.
     * 
     * @param parameterBean
     */
    public ColumnListController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createCommand()
     */
    public Command createCommand(SelectionEvent selectionEvent) {

        Set<String> elementsName;
        Control ctrl;
        IMetadataTable repositoryMetadata = new MetadataTable();
        Connection repositoryConnection = null;

        elementsName = hashCurControls.keySet();
        for (String name : elementsName) {
            Object o = hashCurControls.get(name);
            if (o instanceof Control) {
                ctrl = (Control) o;
                if (ctrl == null) {
                    hashCurControls.remove(name);
                    return null;
                }

                if (ctrl.equals(selectionEvent.getSource()) && ctrl instanceof CCombo) {
                    boolean isDisposed = ((CCombo) ctrl).isDisposed();
                    if (!isDisposed && (!elem.getPropertyValue(name).equals(((CCombo) ctrl).getText()))) {

                        String value = new String(""); //$NON-NLS-1$
                        for (int i = 0; i < elem.getElementParameters().size(); i++) {
                            IElementParameter param = elem.getElementParameters().get(i);
                            if (param.getName().equals(name)) {
                                for (int j = 0; j < param.getListItemsValue().length; j++) {
                                    if (((CCombo) ctrl).getText().equals(param.getListItemsDisplayName()[j])) {
                                        value = (String) param.getListItemsValue()[j];
                                    }
                                }
                            }
                        }
                        if (value.equals(elem.getPropertyValue(name))) { // same value so no need to do anything
                            return null;
                        }

                        return new PropertyChangeCommand(elem, name, value);
                    }
                }

            }
        }
        return null;
    }

    IControlCreator cbCtrl = new IControlCreator() {

        public Control createControl(final Composite parent, final int style) {
            CCombo cb = new CCombo(parent, style);
            return cb;
        }
    };

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createControl()
     */
    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {
        if (param.getDisplayName().startsWith("!!")) { //$NON-NLS-1$
            if (param.getFieldType() == EParameterFieldType.COLUMN_LIST) {
                param.setDisplayName(EParameterName.COLUMN_LIST.getDisplayName());
            } else if (param.getFieldType() == EParameterFieldType.PREV_COLUMN_LIST) {
                param.setDisplayName(EParameterName.PREV_COLUMN_LIST.getDisplayName());
            } else if (param.getFieldType() == EParameterFieldType.LOOKUP_COLUMN_LIST) {
                param.setDisplayName(EParameterName.LOOKUP_COLUMN_LIST.getDisplayName());
            }
        }

        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, cbCtrl);
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }

        Control cLayout = dField.getLayoutControl();
        CCombo combo = (CCombo) dField.getControl();
        FormData data;
        combo.setEditable(false);
        cLayout.setBackground(subComposite.getBackground());
        combo.setEnabled(!param.isReadOnly());
        combo.addSelectionListener(listenerSelection);
        if (elem instanceof Node) {
            combo.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // *********************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);

        // data = new FormData();
        // data.left = new FormAttachment(cLayout, 0, SWT.RIGHT);
        // data.top = new FormAttachment(0, top);
        // data.height = initialSize.y;
        //
        // refreshBtn.setLayoutData(data);

        // **********************
        hashCurControls.put(param.getName(), combo);
        updateData();
        // this.dynamicTabbedPropertySection.updateColumnList(null);

        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return cLayout;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, cbCtrl);
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();

        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    private void updateData() {
        if (elem instanceof Node) {
            updateColumnList((Node) elem, null);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub
    }

    SelectionListener listenerSelection = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent event) {
            // dynamicProperty.updateRepositoryList();
            Command cmd = createCommand(event);
            if (cmd != null) {
                executeCommand(cmd);
                if (updateColumnListFlag) {
                    updateData();
                }
            }
        }
    };

    @Override
    public void refresh(IElementParameter param, boolean check) {
        CCombo combo = (CCombo) hashCurControls.get(param.getName());
        if (combo == null || combo.isDisposed()) {
            return;
        }

        updateData();

        String[] curColumnNameList = param.getListItemsDisplayName();
        String[] curColumnValueList = (String[]) param.getListItemsValue();

        Object value = param.getValue();
        boolean listContainValue = false;
        int numValue = 0;
        if (curColumnNameList != null && curColumnValueList != null) {
            for (int i = 0; i < curColumnValueList.length && !listContainValue; i++) {
                if (curColumnValueList[i].equals(value)) {
                    listContainValue = true;
                    numValue = i;
                }
            }
            combo.setItems(curColumnNameList);
            if (!listContainValue) {
                if (curColumnNameList.length > 0) {
                    elem.setPropertyValue(param.getName(), curColumnValueList[0]);
                    combo.setText(curColumnNameList[0]);
                }
            } else {
                combo.setText(curColumnNameList[numValue]);
            }
        }
    }

    // only set synWidthWithMhaoetadataColumn =true, when use the metadataDialog to set the metadata.
    // see issue 0001676
    public static void updateColumnList(INode node, List<ColumnNameChanged> columnsChanged, boolean synWidthWithMetadataColumn) {
        List<String> columnList = null;
        List<String> prevColumnList = getPrevColumnList(node);
        Map<IConnection, List<String>> refColumnLists = getRefColumnLists(node);

        String[] columnNameList = null;
        String[] prevColumnNameList = prevColumnList.toArray(new String[0]);
        String[] curColumnNameList = null;
        String[] curColumnValueList = null;

        List<String> refColumnListNamesTmp = new ArrayList<String>();
        List<String> refColumnListNamesTmpWithSourceName = new ArrayList<String>();
        List<String> refColumnListValuesTmp = new ArrayList<String>();

        for (IConnection connection : refColumnLists.keySet()) {
            String oldSourceName = connection.getSource().getUniqueName() + ".";
            String name = connection.getName() + "."; //$NON-NLS-1$
            for (String column : refColumnLists.get(connection)) {
                refColumnListNamesTmpWithSourceName.add(oldSourceName + column);
                refColumnListNamesTmp.add(name + column);
                refColumnListValuesTmp.add(column);
            }
        }
        String[] refColumnListNames = refColumnListNamesTmp.toArray(new String[0]);
        String[] refColumnListValues = refColumnListValuesTmp.toArray(new String[0]);

        boolean isSCDComponent = node.getComponent().getName().endsWith("SCD");//$NON-NLS-1$
        boolean isEdifactComponent = node.getComponent().getName().equals("tExtractEDIField");
        updateColumnsOnElement(node, columnsChanged, synWidthWithMetadataColumn, prevColumnNameList, curColumnNameList,
                curColumnValueList, refColumnListNamesTmpWithSourceName, refColumnListValuesTmp, refColumnListNames,
                refColumnListValues, isSCDComponent, isEdifactComponent);

        for (IConnection connection : node.getOutgoingConnections()) {
            updateColumnsOnElement(connection, columnsChanged, synWidthWithMetadataColumn, prevColumnNameList, curColumnNameList,
                    curColumnValueList, refColumnListNamesTmpWithSourceName, refColumnListValuesTmp, refColumnListNames,
                    refColumnListValues, isSCDComponent, isEdifactComponent);
        }
        synLengthTipFlag = null;
    }

    private static void updateColumnsOnElement(IElement element, List<ColumnNameChanged> columnsChanged,
            boolean synWidthWithMetadataColumn, String[] prevColumnNameList, String[] curColumnNameList,
            String[] curColumnValueList, List<String> refColumnListNamesTmpWithSourceName, List<String> refColumnListValuesTmp,
            String[] refColumnListNames, String[] refColumnListValues, boolean isSCDComponent, boolean isEdifactComponent) {
        List<String> columnList;
        String[] columnNameList;
        String edifactId = null;
        if (isEdifactComponent) {
            for (IElementParameter par : element.getElementParametersWithChildrens()) {
                if (par.getName().equals("REPOSITORY_PROPERTY_TYPE")) {
                    edifactId = par.getValue().toString();
                    break;
                }
            }
        }
        for (int i = 0; i < element.getElementParameters().size(); i++) {
            IElementParameter param = element.getElementParameters().get(i);
            columnList = getColumnList(element, param.getContext());
            columnNameList = columnList.toArray(new String[0]);
            if (param.getFieldType() == EParameterFieldType.COLUMN_LIST && !isSCDComponent) {
                curColumnNameList = columnNameList;
                curColumnValueList = columnNameList;
            }
            if (param.getFieldType() == EParameterFieldType.PREV_COLUMN_LIST) {
                curColumnNameList = prevColumnNameList;
                curColumnValueList = prevColumnNameList;
            }
            if (param.getFieldType() == EParameterFieldType.LOOKUP_COLUMN_LIST) {
                curColumnNameList = refColumnListNames;
                curColumnValueList = refColumnListValues;
            }
            if ((param.getFieldType() == EParameterFieldType.COLUMN_LIST && !isSCDComponent)
                    || param.getFieldType() == EParameterFieldType.PREV_COLUMN_LIST
                    || param.getFieldType() == EParameterFieldType.LOOKUP_COLUMN_LIST) {
                param.setListItemsDisplayName(curColumnNameList);
                param.setListItemsValue(curColumnValueList);

                // for bug 10945
                boolean currentColumnStillExist = ArrayUtils.contains(curColumnValueList, param.getValue()); // 10155
                if (!currentColumnStillExist) {
                    for (int j = 0; j < refColumnListNamesTmpWithSourceName.size(); j++) {
                        if (param.getValue().equals(refColumnListNamesTmpWithSourceName.get(j))) {
                            param.setValue(refColumnListValuesTmp.get(j));
                            break;
                        }
                    }
                    currentColumnStillExist = ArrayUtils.contains(curColumnValueList, param.getValue());
                }

                if (curColumnNameList.length > 0 && !currentColumnStillExist) {
                    param.setValue(curColumnValueList[0]);
                } else if (!currentColumnStillExist) {
                    param.setValue(""); //$NON-NLS-1$
                }
                syncNodePropertiesColumns(param, columnsChanged, curColumnNameList);

            }
            if (param.getFieldType() == EParameterFieldType.TABLE) {
                Object[] itemsValue = param.getListItemsValue();
                for (int j = 0; j < itemsValue.length; j++) {
                    if (itemsValue[j] instanceof IElementParameter) {
                        IElementParameter tmpParam = (IElementParameter) itemsValue[j];
                        columnList = getColumnList(element, tmpParam.getContext());
                        String[] tableColumnNameList = columnList.toArray(new String[0]);
                        if (tmpParam.getFieldType() == EParameterFieldType.COLUMN_LIST) {
                            curColumnNameList = tableColumnNameList;
                            curColumnValueList = tableColumnNameList;
                        }
                        if (tmpParam.getFieldType() == EParameterFieldType.PREV_COLUMN_LIST && element instanceof INode) {
                            curColumnNameList = prevColumnNameList;
                            curColumnValueList = prevColumnNameList;
                        }
                        // previous columns from a connection is in fact the same as the current columns.
                        // needed for traces with breakpoint, feature:
                        if (tmpParam.getFieldType() == EParameterFieldType.PREV_COLUMN_LIST && element instanceof IConnection) {
                            curColumnNameList = tableColumnNameList;
                            curColumnValueList = tableColumnNameList;
                        }
                        if (tmpParam.getFieldType() == EParameterFieldType.LOOKUP_COLUMN_LIST) {
                            curColumnNameList = refColumnListNames;
                            curColumnValueList = refColumnListValues;
                        }
                        if (tmpParam.getFieldType() == EParameterFieldType.COLUMN_LIST
                                || tmpParam.getFieldType() == EParameterFieldType.PREV_COLUMN_LIST
                                || tmpParam.getFieldType() == EParameterFieldType.LOOKUP_COLUMN_LIST) {
                            tmpParam.setListItemsDisplayCodeName(curColumnNameList);
                            tmpParam.setListItemsDisplayName(curColumnNameList);
                            tmpParam.setListItemsValue(curColumnValueList);
                            if (curColumnValueList.length > 0) {
                                tmpParam.setDefaultClosedListValue(curColumnValueList[0]);
                            } else {
                                tmpParam.setDefaultClosedListValue(""); //$NON-NLS-1$
                            }
                            syncNodePropertiesTableColumns(param, columnsChanged, curColumnNameList, tmpParam);

                        }
                    }
                }
            }
            if (param.isBasedOnSchema()) {
                List<Map<String, Object>> paramValues = (List<Map<String, Object>>) param.getValue();
                List<Map<String, Object>> newParamValues = new ArrayList<Map<String, Object>>();
                for (int j = 0; j < columnNameList.length; j++) {
                    String columnName = columnNameList[j];
                    String[] codes = param.getListItemsDisplayCodeName();
                    Map<String, Object> newLine = null;
                    boolean found = false;
                    ColumnNameChanged colChanged = null;
                    if (columnsChanged != null) {
                        for (int k = 0; k < columnsChanged.size() && !found; k++) {
                            colChanged = columnsChanged.get(k);
                            if (colChanged.getNewName().equals(columnName)) {
                                found = true;
                            }
                        }
                    }
                    if (found) {
                        found = false;
                        for (int k = 0; k < paramValues.size() && !found; k++) {
                            Map<String, Object> currentLine = paramValues.get(k);
                            if (currentLine.get(codes[0]).equals(colChanged.getOldName())) {
                                currentLine.put(codes[0], colChanged.getNewName());
                                found = true;
                            }
                        }
                    }
                    found = false;
                    for (int k = 0; k < paramValues.size() && !found; k++) {
                        Map<String, Object> currentLine = paramValues.get(k);
                        if (currentLine.get(codes[0]) == null) {
                            currentLine.put(codes[0], columnName);
                        }
                        if (currentLine.get(codes[0]).equals(columnName)) {
                            found = true;
                            newLine = currentLine;
                        }
                    }
                    if (!found) {
                        newLine = TableController.createNewLine(param);
                        newLine.put(codes[0], columnName);
                        if (!StringUtils.isEmpty(edifactId)) {
                            org.talend.core.model.metadata.builder.connection.Connection connection = MetadataTool
                                    .getConnectionFromRepository(edifactId);
                            if (connection != null && connection instanceof EDIFACTConnection) {
                                List<org.talend.core.model.metadata.builder.connection.MetadataTable> tables = ConnectionHelper
                                        .getTablesWithOrders(connection);
                                for (MetadataColumn col : tables.get(0).getColumns()) {
                                    if (col.getLabel().equals(columnName)) {
                                        if (col instanceof EDIFACTColumn) {
                                            EDIFACTColumn edicolumn = (EDIFACTColumn) col;
                                            String ediXpath = edicolumn.getEDIXpath();
                                            newLine.put(codes[0], columnName);
                                            newLine.put(codes[1], ediXpath);
                                            break;
                                        }
                                    }
                                }
                            }

                        }
                        /* should put other attribute for edi mapping */
                    }
                    if (synWidthWithMetadataColumn) {
                        setColumnSize(newLine, element, codes, param);
                    }
                    newParamValues.add(j, newLine);
                }
                paramValues.clear();
                paramValues.addAll(newParamValues);
            } else if (param.isColumnsBasedOnSchema()) {
                List<Map<String, Object>> paramValues = (List<Map<String, Object>>) param.getValue();
                List<Map<String, Object>> newParamValues = new ArrayList<Map<String, Object>>();
                String[] listRepositoryItem = new String[columnNameList.length];
                String[] listItemsDisplayValue = new String[columnNameList.length];
                String[] listItemsDisplayCodeValue = new String[columnNameList.length];
                Object[] listItemsValue = new Object[columnNameList.length];
                String[] listItemsShowIf = new String[columnNameList.length];
                String[] listItemsNotShowIf = new String[columnNameList.length];
                ElementParameter newParam;
                for (int j = 0; j < columnNameList.length; j++) {

                    String columnName = columnNameList[j];
                    listItemsDisplayCodeValue[j] = columnName;
                    listItemsDisplayValue[j] = columnName;
                    listRepositoryItem[j] = ""; //$NON-NLS-1$
                    listItemsShowIf[j] = null;
                    listItemsNotShowIf[j] = null;
                    newParam = new ElementParameter(element);
                    newParam.setName(columnName); //$NON-NLS-1$
                    newParam.setDisplayName(""); //$NON-NLS-1$
                    newParam.setFieldType(EParameterFieldType.TEXT);
                    newParam.setValue(""); //$NON-NLS-1$
                    listItemsValue[j] = newParam;

                    boolean found = false;
                    ColumnNameChanged colChanged = null;
                    if (columnsChanged != null) {
                        for (int k = 0; k < columnsChanged.size() && !found; k++) {
                            colChanged = columnsChanged.get(k);
                            if (colChanged.getNewName().equals(columnName)) {
                                found = true;
                            }
                        }
                    }

                    for (int k = 0; k < paramValues.size(); k++) {
                        Map<String, Object> line = paramValues.get(k);
                        Map<String, Object> newline = new HashMap<String, Object>();
                        if (found) {
                            Object object = line.get(colChanged.getOldName());
                            if (object != null) {
                                newline.put(colChanged.getNewName(), object);
                                line.remove(colChanged.getOldName());
                                line.putAll(newline);
                            }
                        }
                    }

                }
                param.setListItemsDisplayName(listItemsDisplayValue);
                param.setListItemsDisplayCodeName(listItemsDisplayCodeValue);
                param.setListItemsValue(listItemsValue);
                param.setListRepositoryItems(listRepositoryItem);
                param.setListItemsShowIf(listItemsShowIf);
                param.setListItemsNotShowIf(listItemsNotShowIf);

            }
        }
    }

    /**
     * FOR ISSUE 1678 <br>
     * null: need tip<br>
     * true: no need to tip, and confirmation is ok. <br>
     * false: no need to tip and confirmation is canceled.
     */
    private static Boolean synLengthTipFlag = null;

    public static void updateColumnList(INode node, List<ColumnNameChanged> columnsChanged) {
        updateColumnList(node, columnsChanged, false);
    }

    /**
     * bqian Comment method "setSize".
     * 
     * @param newLine
     * @param node
     * @param codes
     * @param param
     */
    private static void setColumnSize(Map<String, Object> newLine, IElement element, String[] codes, IElementParameter param) {
        if (element instanceof INode && ((INode) element).getMetadataList().size() > 0) {
            /* FORMATS should be syn whether this parameter is invisible or not */
            // if (!param.isShow(((INode) element).getElementParameters()) && param.getName().equals("FORMATS")) {
            // return;
            // }
            IMetadataTable table = ((INode) element).getMetadataList().get(0);
            String lineName = (String) newLine.get("SCHEMA_COLUMN"); //$NON-NLS-1$
            for (IMetadataColumn column : table.getListColumns()) {

                if (lineName.equals(column.getLabel())) {
                    final Object size = newLine.get("SIZE"); //$NON-NLS-1$
                    final Integer length = column.getLength();
                    // wzhang modified to fix 12131.
                    if (size != null && length != null && length.intValue() > 0
                            && Integer.parseInt((String) size) == column.getLength()) {
                        break;
                    }

                    if (!needSynchronizeSize(param)) {
                        break;
                    }
                    if (length != null && length.intValue() > 0) {

                        // codes[1] is "SIZE"
                        newLine.put("SIZE", length.toString()); //$NON-NLS-1$
                    } else {
                        newLine.put("SIZE", null); //$NON-NLS-1$
                    }
                    break;
                }
            }
        }
    }

    /**
     * DOC bqian Comment method "needSynchronizeSize".
     * 
     * @param param
     * 
     * @return
     */
    public static boolean needSynchronizeSize(IElementParameter param) {
        boolean find = false;
        Object[] paras = param.getListItemsValue();
        for (Object object : paras) {
            IElementParameter pamameter = (IElementParameter) object;
            if ("LENGTH".equals(pamameter.getContext())) { //$NON-NLS-1$
                // if (pamameter.getName().equals("SIZE")) {
                find = true;
                // }
            }
        }

        if (!find) {
            return false;
        }

        if (synLengthTipFlag == null) {
            Node node = (Node) param.getElement();

            boolean ok = MessageDialog
                    .openConfirm(
                            null,
                            Messages.getString("ColumnListController.confirm"), Messages.getString("ColumnListController.saveChange", node.getLabel()) //$NON-NLS-1$ //$NON-NLS-2$
                    );
            synLengthTipFlag = new Boolean(ok);
        }
        return synLengthTipFlag.booleanValue();
    }

    private static List<String> getColumnList(IElement element, String context) {
        List<String> columnList = new ArrayList<String>();

        IMetadataTable table = null;
        if (element instanceof INode) {
            table = ((INode) element).getMetadataFromConnector(context);
            if (table == null) {
                if (((INode) element).getMetadataList().size() > 0) {
                    table = ((INode) element).getMetadataList().get(0);
                }
            }
        } else if (element instanceof IConnection) {
            table = ((IConnection) element).getMetadataTable();
        }

        if (table != null) {
            for (IMetadataColumn column : table.getListColumns()) {
                // add for bug 12034
                String label = column.getLabel();
                if (element instanceof INode && ((INode) element).getComponent().getName().endsWith("tFileInputXML")) {//$NON-NLS-1$
                    if (label.length() > 1) {
                        String labelSub = label.substring(1);
                        if (labelSub != null && KeywordsValidator.isKeyword(labelSub)) {
                            label = labelSub;
                        }
                    }
                }
                columnList.add(label);
            }
        }

        return columnList;
    }

    private static List<String> getPrevColumnList(INode node) {
        List<String> columnList = new ArrayList<String>();

        IConnection connection = null;
        boolean found = false;
        for (int i = 0; i < node.getIncomingConnections().size() && !found; i++) {
            IConnection curConnec = node.getIncomingConnections().get(i);
            if (curConnec.getLineStyle() == EConnectionType.FLOW_MAIN) {
                connection = curConnec;
                found = true;
            }
        }
        if (connection != null) {
            IMetadataTable table = connection.getMetadataTable();
            if (table != null) {
                for (IMetadataColumn column : table.getListColumns()) {
                    columnList.add(column.getLabel());
                }
            }
        }

        return columnList;
    }

    private static Map<IConnection, List<String>> getRefColumnLists(INode node) {
        Map<IConnection, List<String>> refColumnLists = new HashMap<IConnection, List<String>>();

        List<IConnection> refConnections = new ArrayList<IConnection>();
        for (int i = 0; i < node.getIncomingConnections().size(); i++) {
            IConnection curConnec = node.getIncomingConnections().get(i);
            if (curConnec.getLineStyle() == EConnectionType.FLOW_REF) {
                refConnections.add(curConnec);
            }
        }
        for (IConnection connection : refConnections) {
            List<String> columnList = new ArrayList<String>();
            IMetadataTable table = connection.getMetadataTable();
            if (table == null) {
                continue;
            }
            for (IMetadataColumn column : table.getListColumns()) {
                columnList.add(column.getLabel());
            }
            refColumnLists.put(connection, columnList);
        }
        return refColumnLists;
    }

    /**
     * 
     * DOC ggu Comment method "syncNodePropertiesTableColumns".<BR/>
     * 
     * synchronize COLUMN_LIST, PREV_COLUMN_LIST, LOOKUP_COLUMN_LIST in table. <br/>
     * when modified column name of schema .
     * 
     * @param param
     * @param columnsChanged
     * @param columnNameList
     * @param tmpParam
     */
    private static void syncNodePropertiesTableColumns(IElementParameter param, List<ColumnNameChanged> columnsChanged,
            String[] columnNameList, IElementParameter tmpParam) {
        if (columnsChanged == null || columnsChanged.isEmpty()) {
            return;
        }
        if (columnNameList == null || columnNameList.length == 0) {
            return;
        }
        if (!isUpdateColumnEnable(param, columnsChanged, tmpParam)) {
            return;
        }
        String componentUniqueName = ""; //$NON-NLS-1$
        String preRowLookup = ""; //$NON-NLS-1$
        if (tmpParam.getFieldType() == EParameterFieldType.LOOKUP_COLUMN_LIST && columnNameList[0].indexOf(".") > 0) { //$NON-NLS-1$
            ColumnNameChanged tmpChanged = columnsChanged.get(0);
            if (tmpChanged instanceof ColumnNameChangedExt) {
                componentUniqueName = ((ColumnNameChangedExt) tmpChanged).getChangedNode().getUniqueName() + "."; //$NON-NLS-1$
                preRowLookup = columnNameList[0].substring(0, columnNameList[0].indexOf(".") + 1); //$NON-NLS-1$
            }
        }
        for (ColumnNameChanged colChanged : columnsChanged) {
            String newName = preRowLookup + colChanged.getNewName();
            ColumnNameChanged theChanged = null;
            for (int j = 0; j < columnNameList.length; j++) {
                if (newName.equals(columnNameList[j])) {
                    theChanged = colChanged;
                    break;
                }
            }
            // found
            if (theChanged != null) {
                for (Map<String, Object> currentLine : (List<Map<String, Object>>) param.getValue()) {
                    if (currentLine.get(tmpParam.getName()).equals(componentUniqueName + theChanged.getOldName())) {
                        currentLine.put(tmpParam.getName(), componentUniqueName + theChanged.getNewName());
                    }
                }
            }
        }

    }

    /**
     * 
     * DOC ggu Comment method "syncNodePropertiesColumns".<BR/>
     * 
     * synchronize COLUMN_LIST, PREV_COLUMN_LIST, LOOKUP_COLUMN_LIST
     * 
     * @param param
     * @param columnsChanged
     * @param columnNameList
     */
    private static void syncNodePropertiesColumns(IElementParameter param, List<ColumnNameChanged> columnsChanged,
            String[] columnNameList) {
        if (columnsChanged == null || columnsChanged.isEmpty()) {
            return;
        }
        if (columnNameList == null || columnNameList.length == 0) {
            return;
        }
        if (!isUpdateColumnEnable(param, columnsChanged, param)) {
            return;
        }
        String componentUniqueName = ""; //$NON-NLS-1$
        String preRowLookup = ""; //$NON-NLS-1$
        if (param.getFieldType() == EParameterFieldType.LOOKUP_COLUMN_LIST && columnNameList[0].indexOf(".") > 0) { //$NON-NLS-1$
            ColumnNameChanged tmpChanged = columnsChanged.get(0);
            if (tmpChanged instanceof ColumnNameChangedExt) {
                componentUniqueName = ((ColumnNameChangedExt) tmpChanged).getChangedNode().getUniqueName() + "."; //$NON-NLS-1$
                preRowLookup = columnNameList[0].substring(0, columnNameList[0].indexOf(".") + 1); //$NON-NLS-1$
            }
        }

        for (ColumnNameChanged colChanged : columnsChanged) {
            boolean found = false;
            String newName = preRowLookup + colChanged.getNewName();
            for (int j = 0; j < columnNameList.length; j++) {
                if (newName.equals(columnNameList[j])) {
                    found = true;
                    break;
                }
            }
            if (found) {
                if (param.getValue().equals(componentUniqueName + colChanged.getOldName())) {
                    param.setValue(componentUniqueName + colChanged.getNewName());
                }
            }
        }

    }

    private static boolean isUpdateColumnEnable(IElementParameter param, List<ColumnNameChanged> columnsChanged,
            IElementParameter tmpParam) {
        ColumnNameChanged tmpChanged = columnsChanged.get(0);
        if (tmpChanged instanceof ColumnNameChangedExt && param.getElement() instanceof Node) {
            INode curNode = (Node) param.getElement();
            INode changedNode = ((ColumnNameChangedExt) tmpChanged).getChangedNode();

            if (changedNode == null) {
                return false;
            }

            if (changedNode != curNode) {
                // if not update current node, only update the prev/lookup column list
                if (tmpParam.getFieldType() == EParameterFieldType.PREV_COLUMN_LIST
                        || tmpParam.getFieldType() == EParameterFieldType.LOOKUP_COLUMN_LIST) {
                    return true;
                }

            } else {
                // if update current node, only update the self column list
                if (tmpParam.getFieldType() == EParameterFieldType.COLUMN_LIST) {
                    return true;
                }
            }
        } else {
            // only update self column list
            if (tmpParam.getFieldType() == EParameterFieldType.COLUMN_LIST) {
                return true;
            }
        }
        return false;
    }

}
