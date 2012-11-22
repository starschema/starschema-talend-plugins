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

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * Command that will set or remove the start status on a node. <br/>
 * 
 * $Id: ChangeActivateStatusNodeCommand.java 3351 2007-05-04 12:14:00 +0000 (ven., 04 mai 2007) plegall $
 * 
 */
public class ChangeActivateStatusElementCommand extends Command {

    private final List<Node> nodeList;

    private List<Connection> connectionList;

    private boolean value;

    private int connIndex = 0;

    private List<IConnection> listNm = null;

    private Object object = null;

    private Connection curConn = null;

    private int deactiveNum = 0;

    private List<Connection> outputs = null;

    /**
     * Gives the node where the status will be set or removed.
     * 
     * @param connectionList
     * 
     * @param newStartNode
     */
    public ChangeActivateStatusElementCommand(boolean activate, List<Node> nodeList, List<Connection> connectionList) {
        this.nodeList = nodeList;
        this.connectionList = connectionList;
        value = activate;
        if (activate) {
            setLabel(Messages.getString("ChangeActivateStatusNodeCommand.Label.Active")); //$NON-NLS-1$
        } else {
            setLabel(Messages.getString("ChangeActivateStatusNodeCommand.Label.Deactive")); //$NON-NLS-1$
        }
    }

    private void refreshPropertyView() {
        // IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        // IViewPart view = page.findView("org.eclipse.ui.views.PropertySheet"); //$NON-NLS-1$
        // PropertySheet sheet = (PropertySheet) view;
        // if (sheet.getCurrentPage() instanceof TabbedPropertySheetPage) {
        // TabbedPropertySheetPage tabbedPropertySheetPage = (TabbedPropertySheetPage) sheet.getCurrentPage();
        // if (tabbedPropertySheetPage.getCurrentTab() != null) {
        // tabbedPropertySheetPage.refresh();
        // }
        // }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() {

        if (connectionList != null && connectionList.size() != 0) {
            curConn = connectionList.get(0);
            listNm = (List<IConnection>) curConn.getSource().getOutgoingConnections(curConn.getLineStyle());
            outputs = (List<Connection>) curConn.getSource().getOutgoingConnections();
            connIndex = outputs.indexOf(curConn);
            deactiveNum = 0;
            object = outputs.get(connIndex);
            if (listNm.size() > 1) {
                for (int i = 0; i < listNm.size(); i++) {
                    if (!listNm.get(i).isActivate()) {
                        deactiveNum = deactiveNum + 1;
                    }
                }
                outputs.remove(curConn);
                if (value) {
                    outputs.add(outputs.size() - deactiveNum, (Connection) object);
                } else {
                    outputs.add(curConn);
                }
                curConn.updateAllId();
            }
        }
        if (nodeList != null && nodeList.size() > 0) {
            List<Connection> connIn = null;
            List<Connection> allConn = null;
            connIn = (List<Connection>) nodeList.get(0).getIncomingConnections();
            if (nodeList.get(0).getIncomingConnections() != null && nodeList.get(0).getIncomingConnections().size() > 0) {
                allConn = (List<Connection>) nodeList.get(0).getIncomingConnections().get(0).getSource().getOutgoingConnections();
                if (allConn.containsAll(connIn)) {
                    if (connIn != null && connIn.size() >= 1) {
                        for (int i = 0; i < connIn.size(); i++) {
                            Connection con = connIn.get(i);
                            allConn.remove(con);
                            allConn.add(con);
                        }
                    }
                }
                allConn.get(0).updateAllId();
            }
        }

        Process process;
        if (nodeList.size() > 0) {
            process = (Process) nodeList.get(0).getProcess();
        } else {
            process = (Process) curConn.getSource().getProcess();
        }
        process.setActivate(false);
        for (Connection connection : connectionList) {
            connection.setPropertyValue(EParameterName.ACTIVATE.getName(), value);
        }
        for (Node node : nodeList) {
            if (isSameSchemaInputOutput(node)) {
                node.setPropertyValue(EParameterName.DUMMY.getName(), !value);
            }
            node.setPropertyValue(EParameterName.ACTIVATE.getName(), value);
        }
        process.setActivate(true);
        process.checkStartNodes();
        process.checkProcess();

        refreshPropertyView();
    }

    private boolean isSameSchemaInputOutput(Node node) {
        boolean hasInput = false;
        IMetadataTable inputMetadata = null;
        for (Connection connection : (List<Connection>) node.getIncomingConnections()) {
            if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                if (hasInput) {
                    // if the flag is already set, that means that there is more than one input link
                    // so don't accept the dummy
                    return false;
                }
                hasInput = true;
                inputMetadata = connection.getMetadataTable();
            }
        }

        if (!hasInput) {
            return false;
        }

        for (Connection outputConnection : (List<Connection>) node.getOutgoingConnections()) {
            if (outputConnection.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                IMetadataTable outputMeta = outputConnection.getMetadataTable();
                if (!inputMetadata.sameMetadataAs(outputMeta, IMetadataColumn.OPTIONS_IGNORE_KEY
                        | IMetadataColumn.OPTIONS_IGNORE_NULLABLE | IMetadataColumn.OPTIONS_IGNORE_COMMENT
                        | IMetadataColumn.OPTIONS_IGNORE_PATTERN | IMetadataColumn.OPTIONS_IGNORE_DBCOLUMNNAME
                        | IMetadataColumn.OPTIONS_IGNORE_DBTYPE)) {
                    // input and output schema is different, so don't accept dummy
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void undo() {
        Process process;
        if (nodeList.size() > 0) {
            process = (Process) nodeList.get(0).getProcess();
        } else {
            process = (Process) connectionList.get(0).getSource().getProcess();
        }
        process.setActivate(false);
        for (Node node : nodeList) {
            if (isSameSchemaInputOutput(node)) {
                node.setPropertyValue(EParameterName.DUMMY.getName(), value);
            }
            node.setPropertyValue(EParameterName.ACTIVATE.getName(), !value);
        }
        for (Connection connection : connectionList) {
            connection.setPropertyValue(EParameterName.ACTIVATE.getName(), !value);
        }
        if (connectionList != null && connectionList.size() != 0) {
            if (connIndex < outputs.size()) {
                outputs.remove(this.curConn);
                outputs.add(outputs.size() - deactiveNum, (Connection) object);
            }
            if (connIndex >= outputs.size()) {
                outputs.add(curConn);
            }
            curConn.updateAllId();
        }
        process.setActivate(true);
        process.checkStartNodes();
        process.checkProcess();
        refreshPropertyView();
    }

    @Override
    public void redo() {
        this.execute();
    }
}
