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
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.INode;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * yzhang class global comment. Detailled comment
 */
public class ChangeOutputConnectionOrderCommand extends Command {

    private INode multipleOutputNode;

    private List<IConnection> connectionInNewOrder;

    private List<IConnection> connectionInOldOrder;

    /**
     * yzhang ChangeOutputConnectionOrderCommand constructor comment.
     */
    public ChangeOutputConnectionOrderCommand(INode multipleOutputNode, List<IConnection> outputConnections) {

        this.multipleOutputNode = multipleOutputNode;
        this.connectionInNewOrder = outputConnections;
        this.connectionInOldOrder = (List<IConnection>) multipleOutputNode.getOutgoingConnections();

        if (connectionInNewOrder.size() != connectionInOldOrder.size()) {
            throw new IllegalArgumentException("new connection list should have the same size as the old one"); //$NON-NLS-1$
        }
        this.setLabel(Messages.getString("ChangeMergeOrderCommand.label")); //$NON-NLS-1$

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();
        for (IConnection connection : connectionInNewOrder) {
            if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
                if (!metadataList.contains(connection.getMetadataTable())) {
                    metadataList.add(connection.getMetadataTable());
                }
            }
        }
        multipleOutputNode.setMetadataList(metadataList);
        multipleOutputNode.setOutgoingConnections(connectionInNewOrder);
        connectionInNewOrder.get(0).updateAllId();
        ((Process) multipleOutputNode.getProcess()).checkStartNodes();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();
        // if (hasBuiltInConnector) {
        for (IConnection connection : connectionInOldOrder) {
            if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
                if (!metadataList.contains(connection.getMetadataTable())) {
                    metadataList.add(connection.getMetadataTable());
                }
            }
        }
        // }

        multipleOutputNode.setMetadataList(metadataList);

        multipleOutputNode.setOutgoingConnections(connectionInOldOrder);
        connectionInOldOrder.get(0).updateAllId();
        ((Process) multipleOutputNode.getProcess()).checkStartNodes();
    }
}
