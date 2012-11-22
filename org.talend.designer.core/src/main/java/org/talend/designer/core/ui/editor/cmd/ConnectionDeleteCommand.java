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
import org.talend.core.model.process.AbstractNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * Command that will delete a given connection. <br/>
 * 
 * $Id: ConnectionDeleteCommand.java 77219 2012-01-24 01:14:15Z mhirt $
 * 
 */
public class ConnectionDeleteCommand extends Command {

    private List<Connection> connectionList;

    /**
     * Initialisation of the command that will delete the given connection.
     * 
     * @param connection connection to delete
     */
    public ConnectionDeleteCommand(List<Connection> connectionList) {
        setLabel(Messages.getString("ConnectionDeleteCommand.Label")); //$NON-NLS-1$
        this.connectionList = connectionList;
    }

    public void execute() {
        Process process = (Process) connectionList.get(0).getSource().getProcess();
        for (Connection connection : connectionList) {
            final INode target = connection.getTarget();
            if (target.getExternalNode() instanceof AbstractNode) {
                ((AbstractNode) target.getExternalNode()).removeInput(connection);
            }
            connection.disconnect();
            INodeConnector nodeConnectorSource, nodeConnectorTarget;
            nodeConnectorSource = connection.getSourceNodeConnector();
            if (nodeConnectorSource != null) {
                nodeConnectorSource.setCurLinkNbOutput(nodeConnectorSource.getCurLinkNbOutput() - 1);
            }
            nodeConnectorTarget = connection.getTargetNodeConnector();
            nodeConnectorTarget.setCurLinkNbInput(nodeConnectorTarget.getCurLinkNbInput() - 1);
        }
        process.checkStartNodes();
        process.checkProcess();
    }

    public void undo() {
        Process process = (Process) connectionList.get(0).getSource().getProcess();
        for (Connection connection : connectionList) {
            connection.reconnect();
            INodeConnector nodeConnectorSource, nodeConnectorTarget;
            nodeConnectorSource = connection.getSourceNodeConnector();
            if (nodeConnectorSource != null) {
                nodeConnectorSource.setCurLinkNbOutput(nodeConnectorSource.getCurLinkNbOutput() + 1);
            }

            nodeConnectorTarget = connection.getTargetNodeConnector();
            if (nodeConnectorTarget != null) {
                nodeConnectorTarget.setCurLinkNbInput(nodeConnectorTarget.getCurLinkNbInput() + 1);
            }

        }
        process.checkStartNodes();
        process.checkProcess();
    }
}
