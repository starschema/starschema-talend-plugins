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
package org.talend.designer.core.ui.editor.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.components.IODataComponentContainer;
import org.talend.core.model.metadata.ColumnNameChanged;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalData;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.service.IXmlMapService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.properties.controllers.ColumnListController;
import org.talend.designer.core.ui.views.CodeView;
import org.talend.designer.core.ui.views.properties.ComponentSettings;

/**
 * Command that will change the datas stored for an external node.
 * 
 * $Id: ExternalNodeChangeCommand.java 86388 2012-06-27 10:08:36Z hwang $
 * 
 */
public class ExternalNodeChangeCommand extends Command {

    private Node node;

    private IExternalData oldExternalData;

    private List<IMetadataTable> oldMetaDataList;

    private IExternalData newExternalData;

    private List<IMetadataTable> newMetaDataList;

    private Map<Connection, IODataComponent> connectionsToDelete;

    private List<ChangeMetadataCommand> metadataOutputChanges = new ArrayList<ChangeMetadataCommand>();

    private Map<Connection, IMetadataTable> metadataInputChanges = new HashMap<Connection, IMetadataTable>();

    private Map<Connection, Boolean> metadataInputWasRepository = new HashMap<Connection, Boolean>();

    private IODataComponentContainer inAndOut;

    private Boolean propagate;

    private boolean forTemlate;

    private boolean isMetaLanguage = false;

    @SuppressWarnings("unchecked")
    public ExternalNodeChangeCommand(Node node, IExternalNode externalNode, boolean... bs) {
        if (bs.length > 0) {
            this.isMetaLanguage = bs[0];
        }

        this.node = node;

        this.inAndOut = externalNode.getIODataComponents();

        this.oldExternalData = node.getExternalData();
        oldMetaDataList = node.getMetadataList();

        newExternalData = externalNode.getExternalData();
        newMetaDataList = externalNode.getMetadataList();
        init();
    }

    @SuppressWarnings("unchecked")
    public ExternalNodeChangeCommand(Node node, IExternalNode externalNode, IExternalData oldExternalData, boolean... bs) {
        if (bs.length > 0) {
            this.isMetaLanguage = bs[0];
        }

        this.node = node;

        this.inAndOut = externalNode.getIODataComponents();

        this.oldExternalData = oldExternalData;
        oldMetaDataList = node.getMetadataList();

        newExternalData = externalNode.getExternalData();
        newMetaDataList = externalNode.getMetadataList();
        init();
    }

    private void init() {

        connectionsToDelete = new HashMap<Connection, IODataComponent>();

        for (IODataComponent dataComponent : inAndOut.getOuputs()) {
            IConnection connection = dataComponent.getConnection();
            boolean metadataExists = false;
            for (IMetadataTable metadata : newMetaDataList) {
                if (connection.getMetadataTable().getTableName().equals(metadata.getTableName())) {
                    metadataExists = true;
                }
            }
            if (!metadataExists && (connection instanceof Connection)) {
                connectionsToDelete.put((Connection) connection, dataComponent);
            }
        }

        for (Connection connection : (List<Connection>) node.getIncomingConnections()) {
            String schemaType = (String) connection.getSource().getPropertyValue(EParameterName.SCHEMA_TYPE.getName());
            if (schemaType != null) {
                if (schemaType.equals(EmfComponent.REPOSITORY)) {
                    String metaRepositoryName = (String) connection.getSource().getPropertyValue(
                            EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
                    IMetadataTable repositoryMetadata = MetadataToolHelper.getMetadataFromRepository(metaRepositoryName);
                    if (repositoryMetadata == null) {
                        connection.getSource().setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), EmfComponent.BUILTIN);
                    } else {
                        repositoryMetadata = repositoryMetadata.clone();
                        repositoryMetadata.setTableName(connection.getSource().getUniqueName());
                        if (!repositoryMetadata.sameMetadataAs(connection.getMetadataTable())) {
                            connection.getSource().setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), EmfComponent.BUILTIN);
                        }
                    }
                }
            }
        }

        setLabel(Messages.getString("ExternalNodeChangeCommand.modifaicationFrom") + node.getUniqueName()); //$NON-NLS-1$
    }

    private void refreshCodeView() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IViewPart view = page.findView(CodeView.ID);
        if (view != null) {
            CodeView codeView = (CodeView) view;
            codeView.refresh();
        }
    }

    private boolean getPropagate() {
        if (propagate == null) {
            propagate = MessageDialog
                    .openQuestion(
                            new Shell(),
                            Messages.getString("ExternalNodeChangeCommand.propagate"), Messages.getString("ExternalNodeChangeCommand.propagateMessage")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return propagate;
    }

    private void propagateInput() {
        for (Connection connection : (List<Connection>) node.getIncomingConnections()) {
            if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
                IODataComponent currentIO = inAndOut.getDataComponent(connection);
                currentIO.setColumnOption(IMetadataColumn.OPTIONS_NONE);
                if (currentIO.hasChanged()) {
                    IMetadataTable metadata = inAndOut.getTable(connection);
                    INode sourceNode = currentIO.getSource();
                    sourceNode.metadataOutputChanged(currentIO, currentIO.getName());
                    // It's better to clone, because will change that, if apply, bug 13325
                    // IMetadataTable oldMetadata = connection.getMetadataTable().clone();
                    IMetadataTable newMetadata = metadata.clone();
                    currentIO.setTable(newMetadata);
                    String schemaType = (String) connection.getSource().getPropertyValue(EParameterName.SCHEMA_TYPE.getName());
                    if (schemaType != null) {
                        // if there is a SCHEMA_TYPE, then switch it to BUILT_IN if REPOSITORY is set.
                        if (schemaType.equals(EmfComponent.REPOSITORY)) {
                            connection.getSource().setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), EmfComponent.BUILTIN);
                            metadataInputWasRepository.put(connection, Boolean.TRUE);
                        }
                    }
                    // for bug 9849
                    List<IMetadataColumn> listColumns = connection.getMetadataTable().getListColumns();
                    boolean empty = listColumns.isEmpty(); // before is empty
                    List<IMetadataColumn> newListColumns = newMetadata.getListColumns();
                    List<ColumnNameChanged> columnNameChangeds = new ArrayList<ColumnNameChanged>();
                    int size = listColumns.size();
                    int newSize = newListColumns.size();
                    if (newSize < size) {
                        size = newSize;
                    }

                    for (int i = 0; i < size; i++) {
                        IMetadataColumn metadataColumn = listColumns.get(i);
                        IMetadataColumn newMetadataColumn = newListColumns.get(i);
                        if (metadataColumn != null && newMetadataColumn != null) {
                            String oldLabel = metadataColumn.getLabel();
                            String newLabel = newMetadataColumn.getLabel();
                            if (oldLabel != null && !oldLabel.equals(newLabel)) {
                                columnNameChangeds.add(new ColumnNameChanged(oldLabel, newLabel));
                            }
                        }
                    }

                    connection.getMetadataTable().setListColumns(newListColumns);
                    ColumnListController.updateColumnList(sourceNode, columnNameChangeds, false);

                    if (empty) { // trace init
                        connection.initTraceParamters();
                    }
                }
            }
        }
    }

    @Override
    public void execute() {
        propagateInput();
        // bug 0020749
        if (!oldMetaDataList.isEmpty() && !newMetaDataList.isEmpty()
                && !oldMetaDataList.get(0).sameMetadataAs(newMetaDataList.get(0))) {
            node.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), EmfComponent.BUILTIN);
        }
        metadataOutputChanges.clear();
        List<IConnection> initTraceList = new ArrayList<IConnection>();
        for (IConnection connection : node.getOutgoingConnections()) {
            if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
                IODataComponent dataComponent = inAndOut.getDataComponent(connection);
                if (!connection.getMetadataTable().sameMetadataAs(dataComponent.getTable())) {
                    IMetadataTable table = connection.getMetadataTable();
                    if (table == null || table.getListColumns().isEmpty()) {
                        initTraceList.add(connection);
                    }
                    boolean openDialog = false;
                    if (isForTemlate()) {
                        openDialog = true;
                    } else {
                        openDialog = getPropagate();
                    }

                    if (openDialog) {
                        IElementParameter schemaParam = null;

                        if (connection != null) {
                            IMetadataTable connTable = connection.getMetadataTable();
                            IMetadataTable dataTable = dataComponent.getTable();

                            for (IElementParameter param : ((Node) connection.getTarget()).getElementParameters()) {
                                if (param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                                        && param.getContext().equals(connection.getConnectorName())) {
                                    schemaParam = param;
                                    break;
                                }
                            }
                            if (schemaParam != null) {
                                ChangeMetadataCommand cmd = new ChangeMetadataCommand(connection.getTarget(), schemaParam,
                                        connTable, dataTable);
                                cmd.execute(true);
                                metadataOutputChanges.add(cmd);
                            }

                            for (IElementParameter param : ((Node) connection.getSource()).getElementParameters()) {
                                if (param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                                        && param.getContext().equals(connection.getConnectorName())) {
                                    schemaParam = param;
                                    break;
                                }
                            }
                            if (schemaParam != null) {
                                ChangeMetadataCommand cmd = new ChangeMetadataCommand(connection.getSource(), schemaParam,
                                        connTable, dataTable);
                                cmd.execute(true);
                                metadataOutputChanges.add(cmd);
                            }
                        }

                    } else {
                        // no matter propagate or not the metadata change will be propagate to xmlmap emf data
                        final Node target = (Node) connection.getTarget();
                        if (target != null && target.getExternalNode() != null) {
                            if (GlobalServiceRegister.getDefault().isServiceRegistered(IXmlMapService.class)) {
                                final IXmlMapService service = (IXmlMapService) GlobalServiceRegister.getDefault().getService(
                                        IXmlMapService.class);
                                if (service.isXmlMapComponent(target.getExternalNode())) {
                                    IODataComponent output = new IODataComponent(connection, dataComponent.getTable());
                                    target.metadataInputChanged(output, connection.getUniqueName());
                                }
                            }
                        }
                    }
                }
                if (connection instanceof Connection) {
                    ((Connection) connection).updateName();

                }
            }
        }
        node.setExternalData(newExternalData);
        /*
         * It's better to clone, because will change that, if apply, bug 13325
         */
        // node.setExternalData(newExternalData.clone()); //should test later.
        List<IMetadataTable> cloneNewMetadata = new ArrayList<IMetadataTable>();
        if (newMetaDataList != null) {
            for (IMetadataTable t : newMetaDataList) {
                cloneNewMetadata.add(t.clone(true));
            }
        }
        node.setMetadataList(cloneNewMetadata);
        // init trace
        for (IConnection conn : initTraceList) {
            if (conn instanceof Connection) {
                ((Connection) conn).initTraceParamters();
            }
        }

        for (Connection connection : connectionsToDelete.keySet()) {
            connection.disconnect();
            INode prevNode = connection.getSource();
            INodeConnector nodeConnectorSource, nodeConnectorTarget;
            nodeConnectorSource = prevNode.getConnectorFromType(connection.getLineStyle());
            nodeConnectorSource.setCurLinkNbOutput(nodeConnectorSource.getCurLinkNbOutput() - 1);

            INode nextNode = connection.getTarget();
            nodeConnectorTarget = nextNode.getConnectorFromType(connection.getLineStyle());
            nodeConnectorTarget.setCurLinkNbInput(nodeConnectorTarget.getCurLinkNbInput() - 1);

            inAndOut.getOuputs().remove(connectionsToDelete.get(connection));
        }
        ((Process) node.getProcess()).checkProcess();
        if (!isMetaLanguage) {
            refreshCodeView();
            ComponentSettings.switchToCurComponentSettingsView();
        }
    }

    @Override
    public void undo() {
        for (Connection connection : (List<Connection>) node.getIncomingConnections()) {
            if (connection.getLineStyle().equals(EConnectionType.FLOW_MAIN)
                    || connection.getLineStyle().equals(EConnectionType.FLOW_REF)
                    || connection.getLineStyle().equals(EConnectionType.TABLE)) {
                IODataComponent currentIO = inAndOut.getDataComponent(connection);
                if (currentIO.hasChanged()) {
                    // IMetadataTable metadata = inAndOut.getTable(connection);
                    INode sourceNode = currentIO.getSource();
                    sourceNode.metadataOutputChanged(currentIO, currentIO.getName());
                    // IMetadataTable oldMetadata = connection.getMetadataTable().clone();
                    // currentIO.setTable(oldMetadata);
                    // connection.getMetadataTable().setListColumns(metadata.getListColumns());

                    IMetadataTable oldMetadata = currentIO.getConnMetadataTable();
                    // currentIO.setTable(oldMetadata);
                    connection.getMetadataTable().setListColumns(oldMetadata.getListColumns());
                    if (metadataInputWasRepository.get(connection) != null) {
                        connection.getSource().setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), EmfComponent.REPOSITORY);
                    }
                }
            }
        }
        metadataInputWasRepository.clear();
        node.setExternalData(oldExternalData);
        node.setMetadataList(oldMetaDataList);
        for (Connection connection : connectionsToDelete.keySet()) {
            connection.reconnect();
            Node prevNode = (Node) connection.getSource();
            INodeConnector nodeConnectorSource, nodeConnectorTarget;
            nodeConnectorSource = prevNode.getConnectorFromType(connection.getLineStyle());
            nodeConnectorSource.setCurLinkNbOutput(nodeConnectorSource.getCurLinkNbOutput() + 1);

            Node nextNode = (Node) connection.getTarget();
            nodeConnectorTarget = nextNode.getConnectorFromType(connection.getLineStyle());
            nodeConnectorTarget.setCurLinkNbInput(nodeConnectorTarget.getCurLinkNbInput() + 1);

            if (connectionsToDelete.get(connection) != null) {
                inAndOut.getOuputs().add(connectionsToDelete.get(connection));
            }
        }
        for (ChangeMetadataCommand cmd : metadataOutputChanges) {
            cmd.undo();
        }
        ((Process) node.getProcess()).checkProcess();
        if (!isMetaLanguage) {
            refreshCodeView();
        }
    }

    public boolean isForTemlate() {
        return this.forTemlate;
    }

    public void setForTemlate(boolean forTemlate) {
        this.forTemlate = forTemlate;
    }
}
