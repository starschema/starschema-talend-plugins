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
package org.talend.designer.core.ui.editor.cmd;

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.talend.core.model.process.INodeConnector;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * Command that will delete a given connection. <br/>
 * 
 * $Id: ConnectionDeleteCommand.java 54939 2011-02-11 01:34:57Z mhirt $
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
