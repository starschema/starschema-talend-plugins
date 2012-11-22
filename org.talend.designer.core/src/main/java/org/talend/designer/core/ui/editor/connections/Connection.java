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
package org.talend.designer.core.ui.editor.connections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.talend.core.PluginChecker;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.IPerformance;
import org.talend.core.model.process.IProcess;
import org.talend.core.repository.model.ILocalRepositoryFactory;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.properties.controllers.ColumnListController;
import org.talend.designer.core.ui.editor.properties.controllers.TableController;
import org.talend.repository.model.ComponentsFactoryProvider;

/**
 * Class that define the connection. It's the model part of the Gef element <br/>
 * 
 * $Id: Connection.java 84520 2012-06-01 10:33:18Z hcyi $
 * 
 */
public class Connection extends Element implements IConnection, IPerformance {

    public static final String NAME = "name"; //$NON-NLS-1$

    public static final String LINESTYLE_PROP = "LineStyle"; //$NON-NLS-1$

    public static final String ENABLE_PARALLEL = "ENABLE_PARALLEL"; //$NON-NLS-1$

    public static final String NUMBER_PARALLEL = "NUMBER_PARALLEL"; //$NON-NLS-1$

    private static final String RESUMING_CHECKPOINT = "RESUMING_CHECKPOINT_GROUP"; //$NON-NLS-1$

    private EConnectionType lineStyle = EConnectionType.FLOW_MAIN;

    private boolean isConnected;

    private INode target;

    private INode source;

    protected String name;

    private ConnectionLabel label;

    private ConnectionTrace trace;

    private ConnectionResuming resuming;

    private MonitorConnectionLabel monitorLabel;

    private String metaName;

    private String uniqueName;

    // true if this connection is activated.
    private boolean activate = true;

    private boolean readOnly = false;

    private Map<String, String> traceData;

    private String connectorName;

    private ConnectionPerformance performance;

    private boolean monitorConnection = false;

    public boolean setNullColumn = false;

    /**
     * Tells if this connection has a subjob source or not instead of a node.
     */
    private boolean isSubjobConnection;

    public ArrayList<Integer> traceColumn = new ArrayList<Integer>();

    // used only for copy / paste (will generate the name) && connection
    // creation
    public Connection(INode source, INode target, EConnectionType lineStyle, String connectorName, String metaName,
            String linkName, final boolean monitorConnection) {
        init(source, target, lineStyle, connectorName, metaName, linkName, monitorConnection);
    }

    // used only when loading a process && connection creation
    public Connection(INode source, INode target, EConnectionType lineStyle, String connectorName, String metaName,
            String linkName, String uniqueName, final boolean monitorConnection) {
        this.uniqueName = uniqueName;
        init(source, target, lineStyle, connectorName, metaName, linkName, monitorConnection);
    }

    // used only in ConnectionManager to test if we can connect or not.
    public Connection(INode source, INode target, EConnectionType lineStyle, final boolean monitorConnection) {
        this.source = source;
        this.target = target;
        this.lineStyle = lineStyle;
        this.monitorConnection = monitorConnection;

        // add activate parameter
        IElementParameter param = new ElementParameter(this);
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setValue(Boolean.TRUE);
        param.setName(EParameterName.ACTIVATE.getName());
        param.setDisplayName(EParameterName.ACTIVATE.getDisplayName());
        param.setShow(false);
        param.setNumRow(1);
        addElementParameter(param);
    }

    public void resetStatus() {
        performance.resetStatus();
    }

    /**
     * 
     * Return true if link matches one of types.
     * 
     * @param link
     * @param types
     * @return
     */
    private boolean isInTypes(EConnectionType link, EConnectionType... types) {
        for (EConnectionType type : types) {
            if (link.getId() == type.getId()) {
                return true;
            }
        }
        return false;
    }

    private void init(INode source, INode target, EConnectionType lineStyle, String connectorName, String metaName,
            String linkName, final boolean monitorConnection) {
        if (lineStyle.equals(EConnectionType.ITERATE)) {
            performance = new IterateConnectionPerformance(this);
        } else if (lineStyle.hasConnectionCategory(IConnectionCategory.DEPENDENCY)) {
            // "OnComponentOK/OnComponentError/OnSubJobOK/OnSubJobError/If"
            performance = new LiteralConnectionPerformance(this);
        } else {
            // if no parallel execution existed, just delegate to super class.
            performance = new ParallelConnectionPerformance(this);
        }

        this.connectorName = connectorName;
        this.lineStyle = lineStyle;
        this.metaName = metaName;
        this.monitorConnection = monitorConnection;
        resuming = new ConnectionResuming(this);
        if (lineStyle.hasConnectionCategory(IConnectionCategory.FLOW)) {
            trace = new ConnectionTrace(this);
            createTraceParamters();
            IComponent component = ComponentsFactoryProvider.getInstance().get("tFlowMeter"); //$NON-NLS-1$
            if (component != null) { // only if tFlowMeter is available
                createMeterParameters((Process) source.getProcess());
            }
        }
        setName(linkName);
        if (trace != null) {
            trace.setOffset(label.getOffset());
        }
        if (resuming != null) {
            resuming.setOffset(label.getOffset());
        }

        reconnect(source, target, lineStyle);
        updateName();
        if (lineStyle.equals(EConnectionType.RUN_IF)) {
            IElementParameter param = new ElementParameter(this);
            switch (LanguageManager.getCurrentLanguage()) {
            case JAVA:
                param.setFieldType(EParameterFieldType.MEMO_JAVA);
                break;
            default:
                param.setFieldType(EParameterFieldType.MEMO_PERL);
            }
            param.setCategory(EComponentCategory.BASIC);
            param.setValue(""); //$NON-NLS-1$
            param.setNbLines(5);
            param.setName(EParameterName.CONDITION.getName());
            param.setDisplayName(EParameterName.CONDITION.getDisplayName());
            param.setShow(true);
            param.setNumRow(1);
            addElementParameter(param);
        }
        if (lineStyle.equals(EConnectionType.ROUTE_WHEN)) {

            IElementParameter param = new ElementParameter(this);
            param.setCategory(EComponentCategory.BASIC);
            param.setName(EParameterName.ROUTETYPE.getName());
            param.setDisplayName(EParameterName.ROUTETYPE.getDisplayName());
            String[] strList = { "constant", "el", "groovy", "header", "javaScript", "jxpath", "mvel", "ognl", "php", "property",
                    "python", "ruby", "simple", "spel", "sql", "xpath", "xquery" };
            param.setListItemsValue(strList); //$NON-NLS-1$
            param.setListItemsDisplayName(strList);
            param.setListItemsDisplayCodeName(strList);
            param.setNbLines(1);
            param.setFieldType(EParameterFieldType.CLOSED_LIST);
            param.setShow(true);
            param.setNumRow(1);
            addElementParameter(param);

            param = new ElementParameter(this);
            switch (LanguageManager.getCurrentLanguage()) {
            case JAVA:
                param.setFieldType(EParameterFieldType.MEMO_JAVA);
                break;
            default:
                param.setFieldType(EParameterFieldType.MEMO_PERL);
            }
            param.setCategory(EComponentCategory.BASIC);
            param.setValue(""); //$NON-NLS-1$
            param.setNbLines(5);
            param.setName(EParameterName.CONDITION.getName());
            param.setDisplayName(EParameterName.CONDITION.getDisplayName());
            param.setShow(true);
            param.setNumRow(2);
            addElementParameter(param);
        }

        if (lineStyle.equals(EConnectionType.ROUTE_CATCH)) {

            IElementParameter param = new ElementParameter(this);
            param.setCategory(EComponentCategory.BASIC);
            param.setName(EParameterName.EXCEPTIONLIST.getName());
            param.setDisplayName(EParameterName.EXCEPTIONLIST.getDisplayName());
            param.setNbLines(5);
            param.setFieldType(EParameterFieldType.TEXT);
            param.setShow(true);
            param.setNumRow(1);
            addElementParameter(param);
        }

        if (lineStyle.equals(EConnectionType.ITERATE)) {
            IElementParameter param = new ElementParameter(this);
            param.setFieldType(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.BASIC);
            param.setValue(Boolean.FALSE);
            param.setName(ENABLE_PARALLEL);
            param.setDisplayName(Messages.getString("Connection.enableParallel")); //$NON-NLS-1$
            param.setShow(true);
            param.setNumRow(1);
            addElementParameter(param);

            param = new ElementParameter(this);
            param.setFieldType(EParameterFieldType.TEXT);
            param.setCategory(EComponentCategory.BASIC);
            // param.setListItemsDisplayName(new String[] { "2", "3", "4" });
            // param.setListItemsDisplayCodeName(new String[] { "2", "3", "4" });
            // param.setListItemsValue(new String[] { "2", "3", "4" });
            param.setValue("2"); //$NON-NLS-1$
            param.setName(NUMBER_PARALLEL);
            param.setDisplayName(Messages.getString("Connection.numberParallel")); //$NON-NLS-1$
            param.setShow(true);
            param.setShowIf("ENABLE_PARALLEL == 'true'"); //$NON-NLS-1$
            param.setNumRow(1);
            param.setRequired(true);
            addElementParameter(param);
        }

        // add activate parameter
        IElementParameter param = new ElementParameter(this);
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setValue(Boolean.TRUE);
        param.setName(EParameterName.ACTIVATE.getName());
        param.setDisplayName(EParameterName.ACTIVATE.getDisplayName());
        param.setShow(false);
        param.setNumRow(1);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.UNIQUE_NAME.getName());
        param.setValue(this.getUniqueName()); //$NON-NLS-1$
        param.setDisplayName(EParameterName.UNIQUE_NAME.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.ADVANCED);
        param.setNumRow(1);
        param.setReadOnly(true);
        param.setShow(false);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.UPDATE_COMPONENTS.getName());
        param.setValue(Boolean.FALSE);
        param.setDisplayName(EParameterName.UPDATE_COMPONENTS.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(5);
        param.setReadOnly(true);
        param.setRequired(false);
        param.setShow(false);
        addElementParameter(param);

        // param = new ElementParameter(this);
        // param.setName(EParameterName.LABEL.getName());
        // param.setValue(name);
        // param.setDisplayName(EParameterName.LABEL.getDisplayName());
        // param.setField(EParameterFieldType.TEXT);
        // param.setCategory(EComponentCategory.BASIC);
        // param.setNumRow(6);
        // param.setRequired(false);
        // param.setShow(false);
        // addElementParameter(param);
        if (lineStyle.hasConnectionCategory(IConnectionCategory.FLOW)) {
            initTraceParamters();
        }

        if (PluginChecker.isTeamEdition()) {
            boolean isLocalRepository = (ProxyRepositoryFactory.getInstance().getRepositoryFactoryFromProvider() instanceof ILocalRepositoryFactory);
            param = new ElementParameter(this);
            param.setName(EParameterName.RESUMING_CHECKPOINT.getName());
            param.setValue(Boolean.FALSE);
            param.setGroupDisplayName(EParameterName.RESUMING_CHECKPOINT.getDisplayName());
            param.setDisplayName(EParameterName.RESUMING_CHECKPOINT.getDisplayName());
            param.setFieldType(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.RESUMING);
            param.setGroup(RESUMING_CHECKPOINT);
            param.setNumRow(2);
            param.setShow(true);
            param.setReadOnly(isLocalRepository);
            addElementParameter(param);

            param = new ElementParameter(this);
            param.setName(EParameterName.RESUMLABEL.getName());
            param.setDisplayName(EParameterName.RESUMLABEL.getDisplayName());
            param.setFieldType(EParameterFieldType.TEXT);
            param.setCategory(EComponentCategory.RESUMING);
            param.setGroup(RESUMING_CHECKPOINT);
            param.setValue("");
            param.setNumRow(3);
            param.setShow(true);
            param.setReadOnly(isLocalRepository);
            addElementParameter(param);

            param = new ElementParameter(this);
            param.setName(EParameterName.FAILURE_INSTRUCTIONS.getName());
            param.setDisplayName(EParameterName.FAILURE_INSTRUCTIONS.getDisplayName());
            param.setFieldType(EParameterFieldType.MEMO);
            param.setCategory(EComponentCategory.RESUMING);
            param.setGroup(RESUMING_CHECKPOINT);
            param.setNbLines(5);
            param.setNumRow(4);
            param.setShow(true);
            param.setReadOnly(isLocalRepository);
            addElementParameter(param);
            // breakpoint
            if (lineStyle.hasConnectionCategory(IConnectionCategory.FLOW)
                    || lineStyle.hasConnectionCategory(IConnectionCategory.MERGE)) {
                param = new ElementParameter(this);
                param.setName(EParameterName.ACTIVEBREAKPOINT.getName());
                param.setDisplayName(EParameterName.ACTIVEBREAKPOINT.getDisplayName());
                param.setFieldType(EParameterFieldType.CHECK);
                param.setCategory(EComponentCategory.BREAKPOINT);
                param.setNumRow(13);
                param.setValue(false);
                param.setContextMode(false);
                param.setShow(true);

                addElementParameter(param);
                IComponent component = ComponentsFactoryProvider.getInstance().get("tFilterRow");
                Node tmpNode = new Node(component, (Process) source.getProcess());
                tmpNode.setTemplate(source.isTemplate());
                tmpNode.setGeneratedByJobscriptBool(source.isGeneratedByJobscriptBool());
                tmpNode.addInput(this);
                IElementParameter tmpParam = tmpNode.getElementParameter("LOGICAL_OP");
                if (tmpParam != null) {
                    tmpParam.setElement(this);
                    tmpParam.setCategory(EComponentCategory.BREAKPOINT);
                    tmpParam.setNumRow(14);
                    addElementParameter(tmpParam);
                }
                tmpParam = tmpNode.getElementParameter("CONDITIONS");
                if (tmpParam != null) {
                    tmpParam.setElement(this);
                    tmpParam.setCategory(EComponentCategory.BREAKPOINT);
                    tmpParam.setNumRow(15);
                    ColumnListController.updateColumnList(tmpNode, null, true);
                    addElementParameter(tmpParam);
                }

                tmpParam = tmpNode.getElementParameter("USE_ADVANCED");
                if (tmpParam != null) {
                    tmpParam.setElement(this);
                    tmpParam.setCategory(EComponentCategory.BREAKPOINT);
                    tmpParam.setNumRow(16);
                    addElementParameter(tmpParam);
                }
                tmpParam = tmpNode.getElementParameter("ADVANCED_COND");
                if (tmpParam != null) {
                    tmpParam.setElement(this);
                    tmpParam.setCategory(EComponentCategory.BREAKPOINT);
                    tmpParam.setNumRow(17);
                    addElementParameter(tmpParam);
                }
            }
        }
    }

    /**
     * 
     * cLi Comment method "createTraceParamters".
     * 
     * feature 6355
     */
    private void createTraceParamters() {
        IElementParameter param = new ElementParameter(this);
        param.setName(EParameterName.TRACES_CONNECTION_ENABLE.getName());
        param.setDisplayName(EParameterName.TRACES_CONNECTION_ENABLE.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setValue(Boolean.TRUE);
        param.setCategory(EComponentCategory.ADVANCED);
        param.setShow(false);
        param.setNumRow(1);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.TRACES_CONNECTION_FILTER.getName());
        param.setDisplayName(EParameterName.TRACES_CONNECTION_FILTER.getDisplayName());
        param.setFieldType(EParameterFieldType.TABLE);
        String[] columns = new String[] { IConnection.TRACE_SCHEMA_COLUMN, IConnection.TRACE_SCHEMA_COLUMN_CHECKED,
                IConnection.TRACE_SCHEMA_COLUMN_CONDITION };
        param.setListItemsDisplayCodeName(columns);
        param.setListItemsDisplayName(columns);
        param.setListItemsValue(new ElementParameter[0]);
        param.setValue(new ArrayList<Map<String, Object>>());
        param.setCategory(EComponentCategory.ADVANCED);
        param.setShow(false);
        param.setNumRow(2);

        addElementParameter(param);
    }

    public void initTraceParamters() {
        List<Map<String, Object>> values = new ArrayList<Map<String, Object>>();
        IMetadataTable metadataTable = this.getMetadataTable();
        if (metadataTable != null) {
            for (IMetadataColumn metaColumn : metadataTable.getListColumns()) {
                Map<String, Object> line = new HashMap<String, Object>();

                line.put(IConnection.TRACE_SCHEMA_COLUMN, metaColumn.getLabel());
                line.put(IConnection.TRACE_SCHEMA_COLUMN_CHECKED, true);
                line.put(IConnection.TRACE_SCHEMA_COLUMN_CONDITION, null);

                values.add(line);
            }
        }
        if (getElementParameter(EParameterName.TRACES_CONNECTION_FILTER.getName()) != null && values != null)
            getElementParameter(EParameterName.TRACES_CONNECTION_FILTER.getName()).setValue(values);
        if (trace != null) {
            this.trace.setPropertyValue(EParameterName.TRACES_SHOW_ENABLE.getName(), checkTraceShowEnable());
        }
        // if (resuming != null) {
        // this.resuming.setPropertyValue(EParameterName.RESUMING_CHECKPOINT.getName(), checkResumingShowEnable());
        // }
    }

    public boolean checkTraceShowEnable() {
        // enable
        IProcess process = DesignerPlugin.getDefault().getRunProcessService().getActiveProcess();
        if (process == null || this.getSource() == null) {
            return false;
        }
        if (!process.getId().equals(this.getSource().getProcess().getId())) {
            return false;
        }
        boolean enabled = DesignerPlugin.getDefault().getRunProcessService().enableTraceForActiveRunProcess();
        return enabled;
    }

    // public boolean checkResumingShowEnable() {
    // // enable
    // boolean enabled = DesignerPlugin.getDefault().getRunProcessService().enableResumingForActiveRunProcess();
    // return enabled;
    // }

    private void createMeterParameters(Process process) {

        IElementParameter param = new ElementParameter(this);
        param.setName(EParameterName.MONITOR_CONNECTION.getName());
        param.setDisplayName(EParameterName.MONITOR_CONNECTION.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setValue(monitorConnection);
        param.setCategory(EComponentCategory.ADVANCED);
        param.setShow(true);
        param.setNumRow(10);
        addElementParameter(param);

        Node meterAttached = new Node(ComponentsFactoryProvider.getInstance().get("tFlowMeter"), process); //$NON-NLS-1$
        for (IElementParameter curParam : meterAttached.getElementParameters()) {
            if (curParam.getCategory() == EComponentCategory.BASIC
                    && !curParam.getName().equals(EParameterName.NOT_SYNCHRONIZED_SCHEMA.getName())) {
                curParam.setCategory(EComponentCategory.ADVANCED);
                curParam.setNumRow(curParam.getNumRow() + 1);
                if (curParam.getShowIf() == null || curParam.getShowIf().equals("")) { //$NON-NLS-1$
                    curParam.setShowIf("MONITOR_CONNECTION == 'true'"); //$NON-NLS-1$
                } else {
                    curParam.setShowIf("(" + curParam.getShowIf() + " and MONITOR_CONNECTION == 'true')"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                curParam.setElement(this);
                addElementParameter(curParam);
            }
        }
        meterAttached = null;

        setMonitorLabel(new MonitorConnectionLabel(this));

        updateMonitorLabel(param);
    }

    /**
     * YeXiaowei Comment method "updateMonitorLabel".
     * 
     * @param param
     */
    private void updateMonitorLabel(IElementParameter param) {
        firePropertyChange(EParameterName.MONITOR_CONNECTION.getName(), null, (param.getValue()));
    }

    @Override
    public String toString() {
        return "Name=" + getName() + ", Table=" + getMetadataTable(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Get the name/label of the connection.
     * 
     * @return Connection Name
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * Only works for FLOW_MAIN, FLOW_REF or TABLE link.
     * 
     * @return
     */
    public String getUniqueName() {
        // if (source != null) {
        // if (source.getConnectorFromType(lineStyle).isBuiltIn()) {
        // return metaName;
        // }
        // }
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    /**
     * Set the name/label of the connection.
     * 
     * @param name
     */
    public void setName(String name) {
        boolean canModify = true;
        List connections;
        if (target != null) {
            connections = target.getIncomingConnections();
            for (int i = 0; i < connections.size(); i++) {
                if (((Connection) connections.get(i)).getName().equals(name)) {
                    canModify = false;
                }
            }
        }

        if (canModify) {

            this.name = name;

            if (!lineStyle.equals(EConnectionType.TABLE) && !lineStyle.equals(EConnectionType.ITERATE)) {
                if (isInTypes(lineStyle, EConnectionType.ON_COMPONENT_OK, EConnectionType.ON_COMPONENT_ERROR,
                        EConnectionType.ON_SUBJOB_OK, EConnectionType.ON_SUBJOB_ERROR, EConnectionType.RUN_IF,
                        EConnectionType.ROUTE_WHEN, EConnectionType.ROUTE_CATCH)
                        && source != null && source.getComponent().getName().equals(source.getLabel())) {
                    uniqueName = connectorName;
                } else if (!isInTypes(lineStyle, EConnectionType.ON_COMPONENT_OK, EConnectionType.ON_COMPONENT_ERROR,
                        EConnectionType.ON_SUBJOB_OK, EConnectionType.ON_SUBJOB_ERROR, EConnectionType.RUN_IF,
                        EConnectionType.ROUTE_WHEN, EConnectionType.ROUTE_CATCH)
                        || uniqueName == null || !uniqueName.startsWith(lineStyle.getDefaultLinkName())) {
                    uniqueName = name;
                }
            }

            if (source != null && lineStyle.hasConnectionCategory(IConnectionCategory.FLOW)) {
                // see the bug "6397",the different NodeConnector's instances produce the link's missing.
                if (getSourceNodeConnector().isMultiSchema()) {
                    IMetadataTable table = getMetadataTable();
                    table.setTableName(name);
                    metaName = name;
                }
            }

            if (source != null && (lineStyle == EConnectionType.TABLE)) {
                IMetadataTable table = getMetadataTable();
                table.setLabel(name);
            }
            if (label == null) {
                label = new ConnectionLabel(name, this);
            }
            updateName();
        }
    }

    public void updateName() {
        if (source == null) {
            return;
        }
        // IElementParameter labelParam = getElementParameter(EParameterName.LABEL.getName());
        String labelText = name;
        // if (labelParam != null) {
        // String value = (String) labelParam.getValue();
        // if (!"".equals(value)) {
        // labelText = value;
        // }
        // }

        int outputId = getOutputId();

        boolean updateName = false;
        INodeConnector sourceNodeConnector = getSourceNodeConnector();
        if (sourceNodeConnector == null) {
            return;
        }
        if (getLineStyle().equals(EConnectionType.TABLE)) {
            if (outputId >= 0) {
                labelText += " (" + metaName + ", order:" + outputId + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else {
                labelText += " (" + sourceNodeConnector.getLinkName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            updateName = true;
        } else if (getLineStyle().equals(EConnectionType.FLOW_MAIN) || getLineStyle().equals(EConnectionType.FLOW_REF)) {
            if (sourceNodeConnector.getDefaultConnectionType().equals(getLineStyle())) { // if it's the standard
                // link
                if (outputId >= 0) {
                    labelText += " (" + sourceNodeConnector.getLinkName() + " order:" + outputId + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                } else {
                    labelText += " (" + sourceNodeConnector.getLinkName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
            } else if (sourceNodeConnector.getName().equals(EConnectionType.FLOW_MAIN.getName())) {
                // link
                if (outputId >= 0) {
                    labelText += " (" + getLineStyle().getDefaultLinkName() + " order:" + outputId + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                } else {
                    labelText += " (" + getLineStyle().getDefaultLinkName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
            } else {
                if (outputId >= 0) {
                    labelText += " (" + getLineStyle().getDefaultLinkName() + ", " + sourceNodeConnector.getLinkName() //$NON-NLS-1$ //$NON-NLS-2$
                            + " order:" + outputId + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    labelText += " (" + getLineStyle().getDefaultLinkName() + ", " + sourceNodeConnector.getLinkName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            }
            updateName = true;
        } else if (getLineStyle().equals(EConnectionType.FLOW_MERGE)) {
            int inputId = getInputId();
            if (outputId >= 0) {
                labelText += " (Main order:" + outputId + ", " + getLineStyle().getDefaultLinkName() + " order:" + inputId + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            } else {
                labelText += " (" + getLineStyle().getDefaultLinkName() + " order:" + inputId + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            updateName = true;
        } else if (getLineStyle().equals(EConnectionType.RUN_IF) && (!sourceNodeConnector.getLinkName().equals(name))) {
            // if "RunIf" got a custom name
            labelText = sourceNodeConnector.getLinkName() + " (" + name + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            // bug 8087
            if (outputId >= 0) {
                labelText += " (order:" + outputId + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            updateName = true;
        } else if (getLineStyle().equals(EConnectionType.ROUTE_WHEN)/*
                                                                     * &&
                                                                     * (!sourceNodeConnector.getLinkName().equals(name))
                                                                     */) {
            if (getUniqueName() != null) {
                String linkName = getUniqueName();
                labelText = linkName;
                this.setName(linkName);
            }
            //labelText = labelText + " (" + name + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            if (outputId >= 0) {
                labelText += " (order:" + outputId + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            updateName = true;
        } else if (getLineStyle().equals(EConnectionType.ROUTE_CATCH)/*
                                                                      * &&
                                                                      * (!sourceNodeConnector.getLinkName().equals(name
                                                                      * ))
                                                                      */) {
            if (getUniqueName() != null) {
                String linkName = getUniqueName();
                labelText = linkName;
                this.setName(linkName);
            }
            //labelText = labelText + " (" + name + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            if (outputId >= 0) {
                labelText += " (order:" + outputId + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            updateName = true;
        } else if (getLineStyle().equals(EConnectionType.ROUTE)/*
                                                                * if there are more than one ROUTE connections exist
                                                                * then show the orders of them &&
                                                                * (!sourceNodeConnector.getLinkName().equals(name ))
                                                                */) {
            if (getUniqueName() != null) {
                String linkName = getUniqueName();
                labelText = linkName;
                this.setName(linkName);
            }
            //labelText = labelText + " (" + name + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            if (outputId >= 0) {
                labelText += " (order:" + outputId + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            updateName = true;
        } else if (getLineStyle().equals(EConnectionType.ITERATE)) {
            IElementParameter enableParam = this.getElementParameter(ENABLE_PARALLEL);
            IElementParameter numberParam = this.getElementParameter(NUMBER_PARALLEL);
            // for feature 4505
            boolean special = (outputId >= 0);
            String linkName = sourceNodeConnector.getLinkName();
            if (getUniqueName() != null && special) {
                linkName = getUniqueName();
            }
            if (enableParam != null && (Boolean) enableParam.getValue()) {
                labelText = linkName + " (x " + (String) numberParam.getValue(); //$NON-NLS-1$
                if (special) {
                    labelText += " order:" + outputId; //$NON-NLS-1$
                }
                labelText += ")"; //$NON-NLS-1$
            } else if (special) {
                labelText = linkName + " (order:" + outputId + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            updateName = true;
        } else if (getLineStyle().equals(EConnectionType.ROUTE_ENDBLOCK) || getLineStyle().equals(EConnectionType.ROUTE_TRY)
                || getLineStyle().equals(EConnectionType.ROUTE_FINALLY) || getLineStyle().equals(EConnectionType.ROUTE_OTHER)) {
            String linkName = sourceNodeConnector.getLinkName();
            if (getUniqueName() != null) {
                linkName = getUniqueName();
                labelText = linkName;
                this.setName(linkName);
            }
            updateName = true;
        } else if (getLineStyle().equals(EConnectionType.SYNCHRONIZE)) {
            IElementParameter synchroType = this.getSource().getElementParameter("WAIT_FOR"); //$NON-NLS-1$
            if (synchroType != null) {
                if ("All".equals(synchroType.getValue())) { //$NON-NLS-1$
                    labelText += " (Wait for all)"; //$NON-NLS-1$
                } else if ("First".equals(synchroType.getValue())) { //$NON-NLS-1$
                    labelText += " (Wait for first)"; //$NON-NLS-1$
                }
            }
            updateName = true;
        } else {
            if (outputId >= 0 && !getLineStyle().equals(EConnectionType.PARALLELIZE)) {
                labelText += " (" + "order:" + outputId + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            updateName = true;
        } /*
           * else if (getLineStyle().equals(EConnectionType.LOOKUP)) { labelText += " (" + nodeConnector.getLinkName() +
           * ")"; updateName = true; }
           */

        if (updateName) {

            if (!label.getLabelText().equals(labelText)) {
                label.setLabelText(labelText);
            }

            firePropertyChange(NAME, null, name);
        }
    }

    public ConnectionTrace getConnectionTrace() {
        return trace;
    }

    public void setTraceData(Map<String, String> traceData) {
        Map<String, String> oldData = this.traceData;
        if (!ObjectUtils.equals(oldData, traceData)) {
            this.traceData = traceData;
            if (traceData != null) {
                String traceValue = traceData.get(getName());
                trace.setTrace(traceValue);
            } else {
                trace.setTrace(null);
            }
        }
    }

    public Map<String, String> getTraceData() {
        return this.traceData;
    }

    public INode getTarget() {
        // if (this.target.getJobletNode() != null) {
        // return this.target.getJobletNode();
        // }
        return this.target;
    }

    public INode getSource() {
        // if (this.source.getJobletNode() != null) {
        // return this.source.getJobletNode();
        // }
        return this.source;
    }

    public ConnectionLabel getConnectionLabel() {
        return label;
    }

    /**
     * Reconnect the connection Used after delete a connection, for the undo command and when the connection is
     * connected to new source or target.
     */
    public void reconnect() {
        if (!isConnected) {
            if (lineStyle.equals(EConnectionType.TABLE)) {
                if (uniqueName == null) {
                    uniqueName = source.getProcess().generateUniqueConnectionName(Process.DEFAULT_TABLE_CONNECTION_NAME);
                }
                // if (source.getConnectorFromType(lineStyle).isBuiltIn()) {
                IMetadataTable table = getMetadataTable();
                if (table != null) {
                    table.setTableName(uniqueName);
                    if (table.getLabel() == null) {
                        table.setLabel(name);
                    }
                }
                metaName = uniqueName;
                // }
            } else if (lineStyle.equals(EConnectionType.ITERATE)) {
                // see 3680, the iterate link must have a unique name.
                if (uniqueName == null || !uniqueName.startsWith(Process.DEFAULT_ITERATE_CONNECTION_NAME)) {
                    uniqueName = source.getProcess().generateUniqueConnectionName(Process.DEFAULT_ITERATE_CONNECTION_NAME);
                }
            } else if (lineStyle.equals(EConnectionType.ROUTE) || lineStyle.equals(EConnectionType.ROUTE_ENDBLOCK)) {
                // see 3680, the iterate link must have a unique name.
                if (uniqueName == null || !uniqueName.startsWith(Process.DEFAULT_ROUTE_CONNECTION_NAME)
                        || !source.getProcess().checkValidConnectionName(uniqueName)) {
                    uniqueName = source.getProcess().generateUniqueConnectionName(Process.DEFAULT_ROUTE_CONNECTION_NAME);
                }
            } else if (lineStyle.equals(EConnectionType.ROUTE_WHEN)) {
                // see 3680, the iterate link must have a unique name.
                if (uniqueName == null || !uniqueName.startsWith(Process.DEFAULT_WHEN_CONNECTION_NAME)
                        || !source.getProcess().checkValidConnectionName(uniqueName)) {
                    uniqueName = source.getProcess().generateUniqueConnectionName(Process.DEFAULT_WHEN_CONNECTION_NAME);
                }
            } else if (lineStyle.equals(EConnectionType.ROUTE_OTHER)) {
                // see 3680, the iterate link must have a unique name.
                if (uniqueName == null || !uniqueName.startsWith(Process.DEFAULT_OTHER_CONNECTION_NAME)
                        || !source.getProcess().checkValidConnectionName(uniqueName)) {
                    uniqueName = source.getProcess().generateUniqueConnectionName(Process.DEFAULT_OTHER_CONNECTION_NAME);
                }
            } else if (lineStyle.equals(EConnectionType.ROUTE_CATCH)) {
                // see 3680, the iterate link must have a unique name.
                if (uniqueName == null || !uniqueName.startsWith(Process.DEFAULT_CATCH_CONNECTION_NAME)
                        || !source.getProcess().checkValidConnectionName(uniqueName)) {
                    uniqueName = source.getProcess().generateUniqueConnectionName(Process.DEFAULT_CATCH_CONNECTION_NAME);
                }
            } else if (lineStyle.equals(EConnectionType.ROUTE_FINALLY)) {
                // see 3680, the iterate link must have a unique name.
                if (uniqueName == null || !uniqueName.startsWith(Process.DEFAULT_FINALLY_CONNECTION_NAME)
                        || !source.getProcess().checkValidConnectionName(uniqueName)) {
                    uniqueName = source.getProcess().generateUniqueConnectionName(Process.DEFAULT_FINALLY_CONNECTION_NAME);
                }
            } else if (lineStyle.equals(EConnectionType.ROUTE_TRY)) {
                // see 3680, the iterate link must have a unique name.
                if (uniqueName == null || !uniqueName.startsWith(Process.DEFAULT_TRY_CONNECTION_NAME)
                        || !source.getProcess().checkValidConnectionName(uniqueName)) {
                    uniqueName = source.getProcess().generateUniqueConnectionName(Process.DEFAULT_TRY_CONNECTION_NAME);
                }
            } else if (isInTypes(lineStyle, EConnectionType.ON_COMPONENT_OK, EConnectionType.ON_COMPONENT_ERROR,
                    EConnectionType.ON_SUBJOB_OK, EConnectionType.ON_SUBJOB_ERROR, EConnectionType.RUN_IF)) {
                // see 3443, these links should have unique name
                if (uniqueName == null || uniqueName.equals(lineStyle.getDefaultLinkName())) {
                    uniqueName = source.getProcess().generateUniqueConnectionName(lineStyle.getDefaultLinkName());
                }
            }
            if ((lineStyle.equals(EConnectionType.TABLE) && getSourceNodeConnector().isMultiSchema())
                    || lineStyle.hasConnectionCategory(IConnectionCategory.UNIQUE_NAME)) {
                if (target.getProcess().checkValidConnectionName(uniqueName)) {
                    target.getProcess().addUniqueConnectionName(uniqueName);
                } else if (source.getProcess().checkValidConnectionName(uniqueName)) {
                    source.getProcess().addUniqueConnectionName(uniqueName);
                }
            }
            source.addOutput(this);
            target.addInput(this);
            updateAllId();
            isConnected = true;

            if (lineStyle.equals(EConnectionType.SYNCHRONIZE)) {
                for (IElementParameter param : target.getElementParameters()) {
                    if (param.isBasedOnSubjobStarts()) {
                        TableController.updateSubjobStarts(target, param);
                    }
                }
            }
            if (lineStyle.hasConnectionCategory(IConnectionCategory.FLOW)) {
                initTraceParamters();
            }
        }

    }

    int order = -1;

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Disconnect the connection This function is used before delete or reconnect a connection.
     */
    public void disconnect() {
        if (isConnected) {

            INodeConnector sourceNodeConnector = getSourceNodeConnector();
            if (sourceNodeConnector != null && !sourceNodeConnector.isMultiSchema()) {
                if (lineStyle.hasConnectionCategory(IConnectionCategory.CUSTOM_NAME)
                        || isInTypes(lineStyle, EConnectionType.ITERATE, EConnectionType.ON_COMPONENT_OK,
                                EConnectionType.ON_COMPONENT_ERROR, EConnectionType.ON_SUBJOB_OK,
                                EConnectionType.ON_SUBJOB_ERROR, EConnectionType.RUN_IF, EConnectionType.ROUTE,
                                EConnectionType.ROUTE_TRY, EConnectionType.ROUTE_CATCH, EConnectionType.ROUTE_FINALLY,
                                EConnectionType.ROUTE_ENDBLOCK, EConnectionType.ROUTE_WHEN, EConnectionType.ROUTE_OTHER)) {
                    source.getProcess().removeUniqueConnectionName(uniqueName);
                }
            }
            source.removeOutput(this);
            target.removeInput(this);
            updateAllId();
            isConnected = false;

            if (lineStyle.equals(EConnectionType.SYNCHRONIZE)) {
                for (IElementParameter param : target.getElementParameters()) {
                    if (param.isBasedOnSubjobStarts()) {
                        TableController.updateSubjobStarts(target, param);
                    }
                }
            }
        }
    }

    /**
     * Sets the isConnected.
     * 
     * @param isConnected the isConnected to set
     */
    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    /**
     * Reconnect a connection to a new source and target.
     * 
     * @param newSource
     * @param newTarget
     */
    public void reconnect(INode newSource, INode newTarget, EConnectionType newLineStyle) {
        disconnect();
        this.source = newSource;
        this.target = newTarget;
        this.lineStyle = newLineStyle;

        if ((lineStyle == EConnectionType.SYNCHRONIZE) || (lineStyle == EConnectionType.PARALLELIZE)) {
            ((Process) source.getProcess()).setPropertyValue(EParameterName.MULTI_THREAD_EXECATION.getName(), Boolean.TRUE);
        }
        reconnect();
    }

    /**
     * Set a new style for a given line.
     * 
     * @see org.talend.designer.core.ui.editor.connections.IConnectionType
     * @param lineStyle
     */
    public void setLineStyle(EConnectionType lineStyle) {
        this.lineStyle = lineStyle;
        updateName();
        firePropertyChange(LINESTYLE_PROP, null, connectorName);
    }

    /**
     * Return the given style of the connection.
     * 
     * @see org.talend.designer.core.ui.editor.connections.EConnectionType
     * @return int value of the style
     */
    public int getLineStyleId() {
        return lineStyle.getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.ui.editor.connections.IDesignerConnection#getLineStyle()
     */
    public EConnectionType getLineStyle() {
        return lineStyle;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.ui.editor.Element#setPropertyValue(java.lang.Object, java.lang.Object)
     */
    @Override
    public void setPropertyValue(String id, Object value) {
        if (id.equals(EParameterName.ACTIVATE.getName())) {
            setActivate((Boolean) value);
        }

        if (id.equals(EParameterName.RESUMING_CHECKPOINT.getName())) {
            setResumingConnection((Boolean) value);
        }

        if (id.equals(EParameterName.RESUMLABEL.getName())) {
            setResumingLabelConnection(value.toString());
        }

        if (id.equals(EParameterName.UNIQUE_NAME.getName())) {
            // parameter.setValue(value);
            setUniqueName(value.toString());
        }

        // feature 6355
        if (EParameterName.TRACES_CONNECTION_ENABLE.getName().equals(id) && value instanceof Boolean) {
            setTraceConnection((Boolean) value);
        }
        if ((EParameterName.TRACES_SHOW_ENABLE.getName().equals(id) || EParameterName.ACTIVEBREAKPOINT.getName().equals(id))
                && value instanceof Boolean) {
            if (PluginChecker.isTraceDebugPluginLoaded()
                    && (lineStyle.hasConnectionCategory(IConnectionCategory.FLOW) || lineStyle
                            .hasConnectionCategory(IConnectionCategory.MERGE))) {
                if (EParameterName.ACTIVEBREAKPOINT.getName().equals(id))
                    this.getElementParameter(EParameterName.ACTIVEBREAKPOINT.getName()).setValue(value);
                if (this.trace != null) {
                    this.trace.setPropertyValue(EParameterName.ACTIVEBREAKPOINT.getName(), value);
                }
                firePropertyChange(EParameterName.ACTIVEBREAKPOINT.getName(), null, value);
                setProcessStates();
            }
        }

        if (EParameterName.TRACES_CONNECTION_FILTER.getName().equals(id)) {
            if (this.trace != null) {
                this.trace.setPropertyValue(id, value);
            }
            firePropertyChange(id, null, value);
            setProcessStates();
        }
        if (id.equals(LINESTYLE_PROP)) {
            // setLineStyle((EConnectionType) value);
            setConnectorName((String) value);
        } else {
            if (id.equals(NAME)) {
                setName((String) value);
            } else {
                super.setPropertyValue(id, value);
            }
        }
        if (id.equals(NUMBER_PARALLEL) || id.equals(ENABLE_PARALLEL) || id.equals(EParameterName.LABEL.getName())) {
            updateName();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.ui.editor.Element#getPropertyValue(java.lang.Object)
     */
    @Override
    public Object getPropertyValue(String id) {
        if (id.equals(LINESTYLE_PROP)) {
            return getLineStyle();
        } else if (NAME.equals(id)) {
            return getName();
        }
        return super.getPropertyValue(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.ui.editor.connections.IDesignerConnection#getMetadataTable()
     */
    public IMetadataTable getMetadataTable() {
        if (source != null && this.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
            INodeConnector sourceNodeConnector = getSourceNodeConnector();
            if (sourceNodeConnector != null) {
                if (sourceNodeConnector.isMultiSchema()) {
                    IMetadataTable table = source.getMetadataTable(metaName);
                    if (table == null && source.getJobletNode() != null) {
                        if (source.getMetadataList().size() > 0) {
                            table = source.getMetadataList().get(0);
                        }
                        // MOD by zshen when call the TGenkeyViewAction from tMatchGroup node which have tMap node
                        // before it there only case about one output then need get metadata as follow:
                    } else if (table == null && source.getMetadataList() != null && source.getMetadataList().size() == 1) {
                        table = source.getMetadataFromConnector(sourceNodeConnector.getName());
                    }
                    return table;
                } else {
                    IMetadataTable table = source.getMetadataFromConnector(sourceNodeConnector.getName());
                    if (table == null && source.getJobletNode() != null) {
                        if (source.getMetadataList().size() > 0) {
                            table = source.getMetadataList().get(0);
                        }
                    }
                    return table;
                }
            }
        }
        return null;
    }

    public void setMetaName(String metaName) {
        this.metaName = metaName;
    }

    public String getMetaName() {
        return metaName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.ui.editor.Element#getElementName()
     */
    @Override
    public String getElementName() {
        return getUniqueName();
    }

    public boolean isActivate() {
        return this.activate;
    }

    private void setActivate(boolean activate) {
        this.activate = activate;
        firePropertyChange(EParameterName.ACTIVATE.getName(), null, null);
    }

    public String getCondition() {
        if (lineStyle.equals(EConnectionType.RUN_IF) || lineStyle.equals(EConnectionType.ROUTE_WHEN)) {
            return (String) getPropertyValue(EParameterName.CONDITION.getName());
        } else {
            return null;
        }

    }

    public String getRouteConnectionType() {
        if (lineStyle.equals(EConnectionType.ROUTE_WHEN)) {
            return (String) getPropertyValue(EParameterName.ROUTETYPE.getName());
        } else {
            return null;
        }
    }

    public String getExceptionList() {
        if (lineStyle.equals(EConnectionType.ROUTE_CATCH)) {
            return (String) getPropertyValue(EParameterName.EXCEPTIONLIST.getName());
        } else {
            return null;
        }
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    private void orderConnectionsByMetadata() {
        List<IMetadataTable> tableList = source.getMetadataList();
        List<IConnection> connectionList = (List<IConnection>) source.getOutgoingConnections();
        List<IConnection> tmpList = new ArrayList<IConnection>(connectionList);
        connectionList.clear();
        for (IMetadataTable table : tableList) {
            String tableName = table.getTableName();
            for (IConnection connection : tmpList) {
                if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)
                        && connection.getMetadataTable() != null && connection.getMetadataTable().getTableName() != null
                        && connection.getMetadataTable().getTableName().equals(tableName)
                        && connection.getConnectorName().equals(table.getAttachedConnector())) {
                    connectionList.add(connection);
                }
            }
        }
        // add connections without metadata
        for (IConnection connection : tmpList) {
            if (!connectionList.contains(connection)) {
                connectionList.add(connection);
            }
        }
    }

    public void updateAllId() {
        if (source != null) {
            orderConnectionsByMetadata();
            for (int i = 0; i < source.getOutgoingConnections().size(); i++) {
                Connection connection = (Connection) source.getOutgoingConnections().get(i);
                connection.updateName();
            }
        }
        if (target != null) {
            for (int i = 0; i < target.getIncomingConnections().size(); i++) {
                Connection connection = (Connection) target.getIncomingConnections().get(i);
                connection.updateName();
            }
        }
    }

    public int getOutputId() {
        if (source != null) {
            switch (lineStyle) {
            case FLOW_MAIN:
            case FLOW_REF:
            case FLOW_MERGE:
                int total = 0,
                currentId = -1;
                for (Connection connection : (List<Connection>) source.getOutgoingConnections()) {
                    if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                        total++;
                        if (connection.equals(this)) {
                            currentId = total;
                        }
                    }
                }
                if (total > 1) {
                    return currentId;
                }
                break;
            default:
                List<Connection> connList = (List<Connection>) source.getOutgoingConnections(lineStyle);
                if (connList.size() <= 1) {
                    return -1;
                }
                for (int i = 0; i < connList.size(); i++) {
                    IConnection connection = connList.get(i);
                    if (connection.equals(this)) {

                        return i + 1;
                    }
                }
            }
        }
        return -1;

    }

    public int getInputId() {
        if (target != null) {
            for (int i = 0; i < target.getIncomingConnections().size(); i++) {
                if (target.getIncomingConnections().get(i).equals(this)) {
                    return i + 1;
                }
            }
        }
        return -1;
    }

    /**
     * This function will change the merge order of the connection.
     * 
     * @param id
     */
    public void setInputId(int id) {
        int newId = id - 1;
        int curId = 0;
        if (target != null) {
            if (target.getIncomingConnections().size() < newId) {
                throw new IllegalArgumentException(Messages.getString("Connection.inputInvalid")); //$NON-NLS-1$
            }
            if (target.getIncomingConnections().get(newId).equals(this)) {
                return; // id is already set
            }
            for (int i = 0; i < target.getIncomingConnections().size(); i++) {
                if (target.getIncomingConnections().get(i).equals(this)) {
                    curId = i;
                    break;
                }
            }
            Collections.swap(target.getIncomingConnections(), curId, newId);
        }
    }

    /**
     * Getter for nodeConnector.
     * 
     * @return the nodeConnector
     */
    public INodeConnector getSourceNodeConnector() {
        return source.getConnectorFromName(connectorName);
    }

    public INodeConnector getTargetNodeConnector() {
        // INodeConnector targetNodeConnector =
        // target.getConnectorFromName(connectorName);
        // if (targetNodeConnector != null) {
        // return targetNodeConnector;
        // }
        return target.getConnectorFromType(this.getLineStyle());
    }

    /**
     * Getter for connectionType.
     * 
     * @return the connectionType
     */
    public String getConnectorName() {
        return connectorName;
    }

    /**
     * Sets the connectionType.
     * 
     * @param connectionType the connectionType to set
     */
    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
        updateName();
        firePropertyChange(LINESTYLE_PROP, null, connectorName);
    }

    /**
     * Getter for performance.
     * 
     * @return the performance
     */
    public ConnectionPerformance getPerformance() {
        return this.performance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IPerformance#setPerformanceData(java.lang.String)
     */
    public void setPerformanceData(String pefData) {
        performance.setLabel(pefData);

    }

    public void clearPerformanceDataOnUI() {
        this.performance.clearPerformanceDataOnUI();
    }

    public boolean isUseByMetter() {
        INode sourceNode = this.getSource();
        List<INode> metterNodes = (List<INode>) sourceNode.getProcess().getNodesOfType("tFlowMeter"); //$NON-NLS-1$
        if (metterNodes.size() > 0) {

            Iterator<INode> it = metterNodes.iterator();
            while (it.hasNext()) {
                INode node = it.next();

                String absolute = (String) node.getElementParameter("ABSOLUTE").getValue(); //$NON-NLS-1$
                String reference = (String) node.getElementParameter("CONNECTIONS").getValue(); //$NON-NLS-1$

                if (absolute.equals(Boolean.FALSE.toString()) && reference.equals(this.getUniqueName())) { //$NON-NLS-1$
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Getter for isSubjobConnection.
     * 
     * @return the isSubjobConnection
     */
    public boolean isSubjobConnection() {
        return this.isSubjobConnection;
    }

    /**
     * Sets the isSubjobConnection.
     * 
     * @param isSubjobConnection the isSubjobConnection to set
     */
    public void setSubjobConnection(boolean isSubjobConnection) {
        this.isSubjobConnection = isSubjobConnection;
    }

    /**
     * Getter for monitorConnection.
     * 
     * @return the monitorConnection
     */
    public boolean isMonitorConnection() {
        return this.monitorConnection;
    }

    /**
     * Sets the monitorConnection.
     * 
     * @param monitorConnection the monitorConnection to set
     */
    public void setMonitorConnection(boolean monitorConnection) {
        this.monitorConnection = monitorConnection;
        firePropertyChange(EParameterName.MONITOR_CONNECTION.getName(), null, name);
    }

    /**
     * feature 6355
     */
    public boolean isTraceConnection() {
        Object propertyValue = this.getPropertyValue(EParameterName.TRACES_CONNECTION_ENABLE.getName());
        if (propertyValue != null && propertyValue instanceof Boolean) {
            return (Boolean) propertyValue;
        }
        return false;
    }

    public void setTraceConnection(boolean traceConnection) {
        final String parameterName = EParameterName.TRACES_CONNECTION_ENABLE.getName();
        Object propertyValue = this.getPropertyValue(parameterName);

        if (propertyValue == null || !propertyValue.equals(new Boolean(traceConnection))) {
            super.setPropertyValue(parameterName, traceConnection);
            if (this.trace != null) {
                this.trace.setPropertyValue(parameterName, traceConnection);
            }
            if (propertyValue != null) {
                setProcessStates();
            }

            firePropertyChange(parameterName, null, traceConnection);
        }
    }

    public void setResumingConnection(boolean resumingConnBool) {
        final String parameterName = EParameterName.RESUMING_CHECKPOINT.getName();
        Object propertyValue = this.getPropertyValue(parameterName);

        if (propertyValue == null || !propertyValue.equals(new Boolean(resumingConnBool))) {
            super.setPropertyValue(parameterName, resumingConnBool);
            if (this.resuming != null) {
                this.resuming.setPropertyValue(parameterName, resumingConnBool);
            }
            if (propertyValue != null) {
                setProcessStates();
            }
            firePropertyChange(parameterName, null, resumingConnBool);
        }
    }

    public void setResumingLabelConnection(String resumingLabelValue) {
        final String parameterName = EParameterName.RESUMLABEL.getName();
        Object propertyValue = this.getPropertyValue(parameterName);

        if (propertyValue == null || !propertyValue.equals(resumingLabelValue)) {
            super.setPropertyValue(parameterName, resumingLabelValue);
            if (this.resuming != null) {
                this.resuming.setPropertyValue(parameterName, resumingLabelValue);
            }
            setProcessStates();
            firePropertyChange(parameterName, null, resumingLabelValue);
        }
    }

    private void setProcessStates() {
        IProcess process = this.getSource().getProcess();
        process.setNeedRegenerateCode(true); // generate code again.
        if (process instanceof Process) {
            Process process2 = (Process) process;
            if (!process2.isDuplicate()) {
                process2.setProcessModified(true); // generate data node again.
            }
        }
    }

    public boolean enableTraces() {
        IElementParameter element = this.getElementParameter(EParameterName.TRACES_CONNECTION_ENABLE.getName());
        return element != null;
    }

    public List<String> getEnabledTraceColumns() {
        return TracesConnectionUtils.getEnabledTraceColumns(this);
    }

    public String getTracesCondition() {
        return TracesConnectionUtils.getTracesConditionSet(this);
    }

    /**
     * Getter for monitorLabel.
     * 
     * @return the monitorLabel
     */
    public MonitorConnectionLabel getMonitorLabel() {
        return this.monitorLabel;
    }

    /**
     * Sets the monitorLabel.
     * 
     * @param monitorLabel the monitorLabel to set
     */
    public void setMonitorLabel(MonitorConnectionLabel monitorLabel) {
        this.monitorLabel = monitorLabel;
    }

    public ConnectionResuming getResuming() {
        return this.resuming;
    }

}
