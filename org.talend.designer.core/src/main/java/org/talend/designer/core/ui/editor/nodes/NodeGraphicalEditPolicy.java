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
package org.talend.designer.core.ui.editor.nodes;

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.talend.designer.core.ui.editor.cmd.ConnectionCreateCommand;
import org.talend.designer.core.ui.editor.cmd.ConnectionReconnectCommand;
import org.talend.designer.core.ui.editor.connections.Connection;

/**
 * Edit policy that will allow connections to connect to the node. <br/>
 * 
 * $Id: NodeGraphicalEditPolicy.java 77219 2012-01-24 01:14:15Z mhirt $
 * 
 */
public class NodeGraphicalEditPolicy extends GraphicalNodeEditPolicy {

    protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
        ConnectionCreateCommand cmd = (ConnectionCreateCommand) request.getStartCommand();
        cmd.setTarget((Node) getHost().getModel());
        return cmd;
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
        Node source = (Node) getHost().getModel();
        if (source.isReadOnly()) {
            return null;
        }
        String style = (String) request.getNewObjectType();
        ConnectionCreateCommand cmd = new ConnectionCreateCommand(source, style, (List<Object>) request.getNewObject());
        request.setStartCommand(cmd);
        return cmd;
    }

    protected Command getReconnectSourceCommand(ReconnectRequest request) {
        Connection conn = (Connection) request.getConnectionEditPart().getModel();
        Node newSource = (Node) getHost().getModel();
        if (newSource.isReadOnly()) {
            return null;
        }
        ConnectionReconnectCommand cmd = new ConnectionReconnectCommand(conn);
        cmd.setNewSource(newSource);
        return cmd;
    }

    protected Command getReconnectTargetCommand(ReconnectRequest request) {
        Connection conn = (Connection) request.getConnectionEditPart().getModel();
        Node newTarget = (Node) getHost().getModel();
        if (newTarget.isReadOnly()) {
            return null;
        }

        ConnectionReconnectCommand cmd = new ConnectionReconnectCommand(conn);
        cmd.setNewTarget(newTarget);
        return cmd;
    }
}
