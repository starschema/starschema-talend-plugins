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
package org.talend.core.model.metadata;

import java.util.List;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 * 
 * $Id: IMetadataTable.java 38013 2010-03-05 14:21:59Z mhirt $
 * 
 */
public interface IMetadataTable {

    public String getId();

    public void setId(String id);

    public void setComment(String comment);

    public String getComment();

    public void setLabel(String label);

    public String getLabel();

    public String getDbms();

    public void setDbms(String dbms);

    public String getTableName();

    public void setTableName(String tableName);

    public IMetadataColumn getColumn(String columnName);

    public List<IMetadataColumn> getListColumns();

    public void setListColumns(List<IMetadataColumn> listColumns);

    public IMetadataTable clone(boolean withCustoms);

    public IMetadataTable clone();

    public IMetadataConnection getParent();

    public void setParent(IMetadataConnection metadataConnection);

    public boolean sameMetadataAs(IMetadataTable meta);

    public boolean sameMetadataAs(IMetadataTable other, int options);

    public void sortCustomColumns();

    public boolean isReadOnly();

    public void setReadOnly(boolean readOnly);

    public String getAttachedConnector();

    public void setAttachedConnector(String attachedConnector);

    public String getReadOnlyColumnPosition();

    public void setReadOnlyColumnPosition(String readOnlyColumnPosition);

    /**
     * used in component team; it is used to judge if the List<IMetadataColumn> has the dynamic columns.
     * 
     * @author wliu
     * @return true: the List<IMetadataColumn> has dynamic column; false,not
     */
    public boolean isDynamicSchema();

    public IMetadataColumn getDynamicColumn();

}
