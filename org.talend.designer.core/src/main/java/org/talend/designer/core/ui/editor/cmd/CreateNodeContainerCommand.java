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

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.process.IGraphicalNode;
import org.talend.core.model.update.EUpdateItemType;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.views.problems.Problems;

/**
 * Command that create a new node. <br/>
 * 
 * $Id: CreateNodeContainerCommand.java 54939 2011-02-11 01:34:57Z mhirt $
 * 
 */
public class CreateNodeContainerCommand extends CreateCommand {

    private NodeContainer nodeContainer;

    /**
     * Create the node on the given diagram.
     * 
     * @param diagram
     * @param node
     * @param location
     */
    public CreateNodeContainerCommand(Process process, NodeContainer nodeContainer, Point location) {
        super(Messages.getString("CreateNodeCommand.Label"), process, location); //$NON-NLS-1$
        this.nodeContainer = nodeContainer;
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    @Override
    public boolean canExecute() {
        for (IGraphicalNode currentNode : (List<IGraphicalNode>) process.getGraphicalNodes()) {
            if ((currentNode.getLocation().x == location.x) && (currentNode.getLocation().y == location.y)) {
                return false;
            }
            // check if the component is sigleton
            // see bug 3903
            if (currentNode.getComponent() == nodeContainer.getNode().getComponent()
                    && nodeContainer.getNode().getComponent().isSingleton()) {
                return false;
            }
        }

        AbstractProcessProvider provider = AbstractProcessProvider.findProcessProviderFromPID(nodeContainer.getNode()
                .getComponent().getPluginFullName());
        if (provider != null) {
            if (!provider.canCreateNode(nodeContainer.getNode())) {
                return false;
            }

        }

        return true;
    }

    public void execute() {
        if (this.location != null) {
            this.nodeContainer.getNode().setLocation(this.location);
        }
        AbstractProcessProvider provider = AbstractProcessProvider.findProcessProviderFromPID(nodeContainer.getNode()
                .getComponent().getPluginFullName());
        if (provider == null || (provider != null && provider.containNodeInMemoryNotProcess())) {
            this.process.addNodeContainer(this.nodeContainer);
            process.checkStartNodes();

            nodeContainer.getNode().checkAndRefreshNode();
            // update joblet context.
            // AbstractProcessProvider provider =
            // AbstractProcessProvider.findProcessProviderFromPID(nodeContainer.getNode()
            // .getComponent().getPluginFullName());
            // if (provider != null) {
            // provider.updateJobletContext(nodeContainer.getNode());
            // }
            if (nodeContainer.getNode().getComponent().getComponentType() == EComponentType.JOBLET) {
                process.getUpdateManager().update(EUpdateItemType.JOBLET_CONTEXT);
            }
        } else {
            String name = provider.getComponentProcess().getName() + " " + provider.getComponentProcess().getVersion();
            MessageDialog warningMessageDialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell(), "Can't create node", null, "Joblet process " + name + " is not saved. Please save it first",
                    MessageDialog.OK, new String[] { "OK" }, 0);
            warningMessageDialog.open();

        }
    }

    public void undo() {
        this.process.removeNodeContainer(this.nodeContainer);
        // process.checkProcess();
        process.checkStartNodes();
        Problems.clearAll(nodeContainer.getNode());
        Problems.refreshProblemTreeView();
    }

    public void redo() {
        this.execute();
    }
}
