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

import org.eclipse.gef.commands.Command;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalNode;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.properties.controllers.ConnectionListController;

/**
 * Command that change the label of a connection. <br/>
 * 
 * $Id: ChangeConnTextCommand.java 77219 2012-01-24 01:14:15Z mhirt $
 * 
 */
public class ChangeConnTextCommand extends Command {

    private String newName;

    private String oldName;

    // for elt component
    private String oldMetaName;

    private IConnection connection;

    /**
     * Initialisation of the command with the label of the connection and the new text.
     * 
     * @param connectionLabel Gef object that contains the label of the connection.
     * @param newName new name of the connection label
     */
    public ChangeConnTextCommand(IConnection connection, String newName) {
        this.connection = connection;
        if (newName != null) {
            this.newName = newName;
        } else {
            this.newName = ""; //$NON-NLS-1$
        }
        setLabel(Messages.getString("ChangeConnTextCommand.Label")); //$NON-NLS-1$
    }

    public void execute() {
        oldName = connection.getName();
        connection.setName(newName);
        IElementParameter elementParameter = connection.getElementParameter(EParameterName.UNIQUE_NAME.getName());
        if (elementParameter != null) {
            if ("TABLE".equals(connection.getConnectorName())) {
                oldMetaName = connection.getMetaName();
                connection.setPropertyValue(EParameterName.UNIQUE_NAME.getName(), connection.getMetaName());
            } else {
                connection.setPropertyValue(EParameterName.UNIQUE_NAME.getName(), newName);
            }
        }
        // connection.setPropertyValue(EParameterName.LABEL.getName(), newName);

        if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.UNIQUE_NAME)) {
            connection.getSource().getProcess().removeUniqueConnectionName(oldName);
            connection.getSource().getProcess().addUniqueConnectionName(newName);
        }
        ConnectionListController.renameConnectionInElement(oldName, newName, connection.getSource());

        IExternalNode externalNode = connection.getTarget().getExternalNode();
        if (externalNode != null) {
            externalNode.renameInputConnection(oldName, newName);
        }
        externalNode = connection.getSource().getExternalNode();
        if (externalNode != null) {
            externalNode.renameOutputConnection(oldName, newName);
        }
        ((Process) connection.getSource().getProcess()).checkProcess();
    }

    public void redo() {
        execute();
    }

    public void undo() {
        connection.setName(oldName);
        IElementParameter elementParameter = connection.getElementParameter(EParameterName.UNIQUE_NAME.getName());
        if (elementParameter != null) {
            if ("TABLE".equals(connection.getConnectorName())) {
                connection.setPropertyValue(EParameterName.UNIQUE_NAME.getName(), oldMetaName);
            } else {
                connection.setPropertyValue(EParameterName.UNIQUE_NAME.getName(), oldName);
            }
        }
        if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.UNIQUE_NAME)) {
            connection.getSource().getProcess().removeUniqueConnectionName(newName);
            connection.getSource().getProcess().addUniqueConnectionName(oldName);
        }

        IExternalNode externalNode = connection.getTarget().getExternalNode();
        if (externalNode != null) {
            externalNode.renameInputConnection(newName, oldName);
        }
        externalNode = connection.getSource().getExternalNode();
        if (externalNode != null) {
            externalNode.renameOutputConnection(newName, oldName);
        }
        ((Process) connection.getSource().getProcess()).checkProcess();
    }
}
