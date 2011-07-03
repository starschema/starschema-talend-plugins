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
package org.talend.designer.core.ui.editor.connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.core.CorePlugin;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.designer.core.model.components.EParameterName;

/**
 * cLi class global comment. Detailled comment
 */
public class TracesConnectionUtils {

    private static final String EMPTY = ""; //$NON-NLS-1$

    public static List<String> getEnabledTraceColumns(final IConnection conn) {
        List<String> enabledColumns = new ArrayList<String>();

        List<Map<String, Object>> values = getTraceConnectionFilterValues(conn);
        IMetadataTable table = conn.getMetadataTable();

        if (table != null && values != null && conn != null) {

            for (Map<String, Object> line : values) {
                Object column = line.get(IConnection.TRACE_SCHEMA_COLUMN);
                if (isTraceColumnEnabled(conn, line, column)) {
                    enabledColumns.add((String) column);
                }
            }
        }
        return enabledColumns;
    }

    private static boolean isTraceColumnEnabled(final IConnection conn, final Map<String, Object> line, final Object columnName) {
        if (conn == null || columnName == null || line == null || line.isEmpty()) {
            return false;
        }
        IMetadataTable table = conn.getMetadataTable();

        if (columnName instanceof String && isContainColumn(table, (String) columnName)) {
            Object checked = line.get(IConnection.TRACE_SCHEMA_COLUMN_CHECKED);
            if (checked != null) {
                if (checked instanceof Boolean && (Boolean) checked) {
                    return true;
                } else if (checked instanceof String && Boolean.TRUE.toString().equals(checked)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isTracesColumnEnabled(final IConnection conn, final String columnName) {
        if (conn == null || columnName == null) {
            return false;
        }
        List<Map<String, Object>> values = getTraceConnectionFilterValues(conn);
        if (values != null) {
            for (Map<String, Object> line : values) {
                Object column = line.get(IConnection.TRACE_SCHEMA_COLUMN);
                if (columnName.equals(column)) {
                    return isTraceColumnEnabled(conn, line, columnName);
                }
            }
        }
        return false;
    }

    public static String getTracesConditionSet(final IConnection conn) {
        IMetadataTable table = conn.getMetadataTable();
        List<Map<String, Object>> values = getTraceConnectionFilterValues(conn);
        if (table != null && values != null) {
            StringBuffer conditions = new StringBuffer();

            boolean first = true;
            for (Map<String, Object> line : values) {
                Object column = line.get(IConnection.TRACE_SCHEMA_COLUMN);
                if (!isTraceColumnEnabled(conn, line, column)) {
                    continue;
                }
                String str = getTracesColumnCondition(line, conn, column);
                if (!EMPTY.equals(str)) {
                    if (first) {
                        conditions.append(str);
                        first = false;
                    } else {
                        conditions.append(" && " + str); //$NON-NLS-1$
                    }
                }
            }
            if (!first) { // contain one at least.
                String string = conditions.toString();
                if (!string.trim().equals(EMPTY)) {
                    return string;
                }
            }
        }
        return null;
    }

    private static String getTracesColumnCondition(Map<String, Object> line, IConnection conn, Object columnName) {
        if (line == null || line.isEmpty() || conn == null || columnName == null) {
            return EMPTY;
        }
        IMetadataTable table = conn.getMetadataTable();

        if (columnName instanceof String && isContainColumn(table, (String) columnName)) {
            Object condition = line.get(IConnection.TRACE_SCHEMA_COLUMN_CONDITION);
            if (condition != null && condition instanceof String) {
                String str = (String) condition;
                str = str.trim();
                if (!EMPTY.equals(str)) {
                    return str;
                }
            }
        }
        return EMPTY;
    }

    public static String getTracesColumnCondition(final IConnection conn, final String columnName) {
        if (conn == null || columnName == null) {
            return EMPTY;
        }
        IMetadataTable table = conn.getMetadataTable();
        List<Map<String, Object>> values = getTraceConnectionFilterValues(conn);
        if (table != null && values != null) {

            for (Map<String, Object> line : values) {
                Object column = line.get(IConnection.TRACE_SCHEMA_COLUMN);
                if (columnName.equals(column)) {
                    return getTracesColumnCondition(line, conn, columnName);
                }
            }
        }
        return EMPTY;
    }

    public static void setTraceColumnValues(final IConnection conn, final String columnName, String condition, boolean enable) {
        if (conn == null || columnName == null || EMPTY.equals(columnName)) {
            return;
        }
        if (condition == null) {
            condition = EMPTY;
        }
        IMetadataTable table = conn.getMetadataTable();
        List<Map<String, Object>> values = getTraceConnectionFilterValues(conn);
        if (table != null && values != null) {
            Map<String, Object> foundLine = null;
            for (Map<String, Object> line : values) {
                Object column = line.get(IConnection.TRACE_SCHEMA_COLUMN);
                if (columnName.equals(column)) {// found
                    foundLine = line;
                    break;
                }
            }
            if (foundLine == null) { // not found
                foundLine = new HashMap<String, Object>();
                foundLine.put(IConnection.TRACE_SCHEMA_COLUMN, columnName);
                values.add(foundLine);

            }
            foundLine.put(IConnection.TRACE_SCHEMA_COLUMN_CHECKED, new Boolean(enable));
            foundLine.put(IConnection.TRACE_SCHEMA_COLUMN_CONDITION, condition);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getTraceConnectionFilterValues(IConnection conn) {
        IElementParameter elementParameter = conn.getElementParameter(EParameterName.TRACES_CONNECTION_FILTER.getName());
        if (elementParameter != null) {
            Object value = elementParameter.getValue();
            if (value != null) {
                List<Map<String, Object>> values = (List<Map<String, Object>>) value;
                return values;
            }
        }
        return null;
    }

    private static boolean isContainColumn(IMetadataTable table, String column) {
        if (table != null && column != null) {
            for (IMetadataColumn col : table.getListColumns()) {
                if (col.getLabel().equals(column)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * DOC wzhang Comment method "setTraceFilterParameters".
     */
    public static void setTraceFilterParameters(INode node, IMetadataTable table, Set<String> preColumnSet,
            Map<String, String> changedNameColumns) {
        if (node != null && table != null && preColumnSet != null) {
            IConnection curConnection = getConnection(node.getOutgoingConnections(), table);
            if (curConnection != null) {
                Set<String> addedColumns = new HashSet<String>();
                for (IMetadataColumn curColumn : table.getListColumns()) {
                    if (!(preColumnSet.contains(curColumn.getLabel()))) {
                        addedColumns.add(curColumn.getLabel());
                    }
                }
                CorePlugin.getDefault().getDesignerCoreService().updateTraceColumnValues(curConnection, changedNameColumns,
                        addedColumns);
            }
        }
    }

    /**
     * DOC wzhang Comment method "getConnection".
     */
    public static IConnection getConnection(List<? extends IConnection> connections, IMetadataTable table) {
        if (table != null && connections != null) {
            for (IConnection conn : connections) {
                IMetadataTable metaTable = conn.getMetadataTable();
                if (metaTable != null) {
                    String tabName = metaTable.getTableName();
                    if (tabName.equals(table.getTableName())) {
                        return conn;
                    }
                }
            }
        }
        return null;
    }
}
