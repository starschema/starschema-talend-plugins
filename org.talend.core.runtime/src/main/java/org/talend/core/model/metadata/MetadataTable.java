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

import java.util.ArrayList;
import java.util.List;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.components.EReadOnlyComlumnPosition;

/**
 * Meta Data Table. Contains all the columns. <br/>
 * $Id: MetadataTable.java 46622 2010-08-11 10:04:57Z wliu $
 */
public class MetadataTable implements IMetadataTable, Cloneable {

    private String tableName;

    private List<IMetadataColumn> listColumns = new ArrayList<IMetadataColumn>();

    private IMetadataConnection parent;

    private boolean readOnly = false;

    private String dbms;

    private String attachedConnector;

    private String readOnlyColumnPosition;

    private String label;

    private String id;

    private String comment;

    public String getReadOnlyColumnPosition() {
        return this.readOnlyColumnPosition;
    }

    public void setReadOnlyColumnPosition(String readOnlyColumnPosition) {
        this.readOnlyColumnPosition = readOnlyColumnPosition;
    }

    @Override
    public String toString() {
        StringBuffer toReturn = new StringBuffer(getTableName() + ":"); //$NON-NLS-1$
        for (IMetadataColumn cur : listColumns) {
            toReturn.append(cur.toString() + " "); //$NON-NLS-1$
        }
        return toReturn.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataTable#getTableName()
     */
    public String getTableName() {
        return this.tableName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataTable#setTableName(java.lang.String)
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataTable#getListColumns()
     */
    public List<IMetadataColumn> getListColumns() {
        return this.listColumns;
    }

    public boolean isDynamicSchema() {

        int sizeListColumns = listColumns.size();
        boolean hasDynamic = false;
        for (int i = 0; i < sizeListColumns; i++) {
            if (listColumns.get(i).getTalendType().equals("id_Dynamic")) {
                hasDynamic = true;
                break;
            }
        }
        return hasDynamic;

    }

    public IMetadataColumn getDynamicColumn() {
        if (isDynamicSchema()) {
            for (int i = 0; i < listColumns.size(); i++) {
                if (listColumns.get(i).getTalendType().equals("id_Dynamic")) {
                    return listColumns.get(i);
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataTable#setListColumns(Hashtable)
     */
    public void setListColumns(List<IMetadataColumn> listColumns) {
        this.listColumns = listColumns;
    }

    public IMetadataTable clone(boolean withCustoms) {
        IMetadataTable clonedMetadata = null;
        try {
            clonedMetadata = (IMetadataTable) super.clone();

            List<IMetadataColumn> clonedMetaColumns = new ArrayList<IMetadataColumn>();
            clonedMetadata.setListColumns(clonedMetaColumns);
            for (int i = 0; i < listColumns.size(); i++) {
                clonedMetaColumns.add(listColumns.get(i).clone(withCustoms));
            }
            clonedMetadata.setTableName(this.getTableName());
            clonedMetadata.setLabel(this.getLabel());
        } catch (CloneNotSupportedException e) {
            // nothing
        } catch (Exception e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
        return clonedMetadata;
    }

    /**
     * cloned without custom columns by default.
     */
    @Override
    public IMetadataTable clone() {
        return clone(false);
    }

    /**
     * Note: for a table with custom columns, the order for the test is really important. It should be
     * outputMetadata.sameMetadataAs (inputMetadata).
     */
    public boolean sameMetadataAs(IMetadataTable input, int options) {
        if (this == input) {
            return true;
        }
        if (input == null) {
            return false;
        }
        if (!(input instanceof IMetadataTable)) {
            return false;
        }

        if (this.listColumns == null) {
            if (input.getListColumns() != null) {
                return false;
            }
        } else {
            if (listColumns.size() == input.getListColumns().size()) { // test if standard columns (no custom, or same
                // input / output)
                for (int i = 0; i < input.getListColumns().size(); i++) {
                    IMetadataColumn otherColumn = input.getListColumns().get(i);
                    IMetadataColumn myColumn = this.listColumns.get(i);
                    if (!otherColumn.sameMetacolumnAs(myColumn, options)) {
                        return false;
                    }
                }
            } else {
                // output should have at least all columns from input, but can add some others (custom)
                // custom columns are considered as columns who can be added to output

                List<IMetadataColumn> outputColumnsNotTested = new ArrayList<IMetadataColumn>(this.getListColumns());

                // test that input columns are correctly propagated to output first
                // no matter if this one is custom or not (all custom must be propagated too)
                for (int i = 0; i < input.getListColumns().size(); i++) {
                    IMetadataColumn inputColumn = input.getListColumns().get(i);
                    IMetadataColumn myColumn = this.getColumn(inputColumn.getLabel());
                    outputColumnsNotTested.remove(myColumn);
                    if (!inputColumn.sameMetacolumnAs(myColumn, options)) {
                        return false;
                    }
                }

                // we tested already all input columns propagated to output
                // means normally there should be only possible custom columns remaining in the schema.
                // if one of the column remaining in output is not custom, then there is a problem of propagation
                for (IMetadataColumn column : outputColumnsNotTested) {
                    if (!column.isCustom()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean sameMetadataAs(IMetadataTable other) {
        return sameMetadataAs(other, IMetadataColumn.OPTIONS_IGNORE_DBCOLUMNNAME | IMetadataColumn.OPTIONS_IGNORE_DEFAULT
                | IMetadataColumn.OPTIONS_IGNORE_COMMENT);
    }

    /**
     * Getter for parent.
     * 
     * @return the parent
     */
    public IMetadataConnection getParent() {
        return this.parent;
    }

    /**
     * Sets the parent.
     * 
     * @param parent the parent to set
     */
    public void setParent(IMetadataConnection parent) {
        this.parent = parent;
    }

    public IMetadataColumn getColumn(String columnName) {
        for (int i = 0; i < listColumns.size(); i++) {
            IMetadataColumn column = listColumns.get(i);
            if (column.getLabel().equals(columnName)) {
                return column;
            }
        }
        return null;
    }

    public void sortCustomColumns() {
        List<IMetadataColumn> customColumns = new ArrayList<IMetadataColumn>();
        List<IMetadataColumn> tempCustomColumns = new ArrayList<IMetadataColumn>();
        List<IMetadataColumn> noCustomColumns = new ArrayList<IMetadataColumn>();

        for (int i = 0; i < listColumns.size(); i++) {
            IMetadataColumn column = listColumns.get(i);
            if (column.isCustom()) {
                tempCustomColumns.add(column);
            }
        }

        listColumns.removeAll(tempCustomColumns);

        noCustomColumns.addAll(listColumns);

        int nbDone = 0;
        while (nbDone < tempCustomColumns.size()) {
            boolean found = false;
            for (int i = 0; i < tempCustomColumns.size() && !found; i++) {
                IMetadataColumn column = tempCustomColumns.get(i);
                if (column.getCustomId() == nbDone) {
                    customColumns.add(column);
                    found = true;
                }
            }
            nbDone++;
        }

        listColumns.clear();
        if (this.readOnlyColumnPosition != null && this.readOnlyColumnPosition.equals(EReadOnlyComlumnPosition.TOP.getName())) {
            listColumns.addAll(customColumns);
            listColumns.addAll(noCustomColumns);
        } else {
            listColumns.addAll(noCustomColumns);
            listColumns.addAll(customColumns);
        }
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getDbms() {
        return dbms;
    }

    public void setDbms(String dbms) {
        this.dbms = dbms;
    }

    /**
     * Getter for attachedConnector.
     * 
     * @return the attachedConnector
     */
    public String getAttachedConnector() {
        return attachedConnector;
    }

    /**
     * Sets the attachedConnector.
     * 
     * @param attachedConnector the attachedConnector to set
     */
    public void setAttachedConnector(String attachedConnector) {
        this.attachedConnector = attachedConnector;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getComment() {
        return comment;
    }

    public String getId() {
        return id;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setId(String id) {
        this.id = id;
    }
}
