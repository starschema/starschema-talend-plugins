package org.talend.designer.core.ui.editor.jobletcontainer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.ui.IJobletProviderService;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.JobletConnectionReconnectCommand;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;

public class JobletContainer extends NodeContainer {

    public static final String UPDATE_JOBLET_CONTENT = "UPDATE_JOBLET_CONTENT"; //$NON-NLS-1$

    public static final String UPDATE_JOBLET_DATA = "UPDATE_JOBLET_DATA"; //$NON-NLS-1$

    public static final String UPDATE_JOBLET_CONNECTIONS = "UPDATE_JOBLET_CONNECTIONS"; //$NON-NLS-1$

    public static final String UPDATE_JOBLET_TITLE_COLOR = "UPDATE_JOBLET_TITLE_COLOR"; //$NON-NLS-1$

    public static final String UPDATE_JOBLET_DISPLAY = "UPDATE_JOBLET_DISPLAY"; //$NON-NLS-1$

    protected List<Node> nodes = new ArrayList<Node>();

    private List<NodeContainer> nodeContainers = new ArrayList<NodeContainer>();

    private IProcess2 process;

    private Node node;

    private Rectangle jobletRectangle;

    private int changeWidth;

    private int changeHeight;

    private boolean hasChange;

    private boolean needchangeLock = true;

    protected List<IElement> jobletElements = new ArrayList<IElement>();

    public JobletContainer(Node node) {
        super(node);
        this.node = node;
    }

    /**
     * Getter for process.
     * 
     * @return the process
     */
    public IProcess2 getProcess() {
        if (process == null) {
            IProcess iPro = node.getComponent().getProcess();
            if (iPro instanceof IProcess2) {
                return (IProcess2) iPro;
            }
        }
        return this.process;
    }

    public Rectangle getJobletUnion(Rectangle jobletNodeRec, Rectangle rect) {
        // if (rect == null || rect.isEmpty())
        // return new Rectangle(this);
        Rectangle union = new Rectangle(jobletNodeRec.x, jobletNodeRec.y, 0, 0);
        union.width = Math.max(jobletNodeRec.x + jobletNodeRec.width, rect.x + rect.width) - union.x;
        union.height = Math.max(jobletNodeRec.y + jobletNodeRec.height, rect.y + rect.height) - union.y;
        return union;
    }

    /**
     * DOC hwang Comment method "getJobletContainerRectangle".
     * 
     * @return
     */
    public Rectangle getJobletContainerRectangle() {
        Rectangle totalRectangle = null;
        boolean collapsed = isCollapsed();
        if (!collapsed && nodeContainers.size() > 0) {
            Rectangle jobletNodeRec = this.node.getNodeContainer().getNodeContainerRectangle();
            for (NodeContainer container : nodeContainers) {
                Rectangle curRect = container.getNodeContainerRectangle();
                // if (container.getNode().isDesignSubjobStartNode()) {
                // totalRectangle = curRect.getCopy();
                // } else {
                if (totalRectangle == null) {
                    totalRectangle = curRect.getCopy();
                } else {
                    totalRectangle = totalRectangle.getUnion(curRect);
                }
                // }
            }
            // totalRectangle.setLocation(jobletNodeRec.getLocation());
        } else if (node != null) {
            NodeContainer container = node.getNodeContainer();
            Rectangle curRect = container.getNodeContainerRectangle();
            if (collapsed) {
                totalRectangle = curRect.getCopy();
            } else {
                if (totalRectangle == null) {
                    totalRectangle = curRect.getCopy();
                } else {
                    totalRectangle = totalRectangle.getUnion(curRect);
                }
            }
        }

        if (totalRectangle == null) {
            return null;
        }
        if (jobletRectangle != null) {
            if ((Math.abs(jobletRectangle.width - totalRectangle.width) != 0) || this.nodeContainers.size() == 1) {
                changeWidth = Math.abs(jobletRectangle.width - totalRectangle.width);
            }
            if ((Math.abs(jobletRectangle.height - totalRectangle.height) != 0) || this.nodeContainers.size() == 1) {
                changeHeight = Math.abs(jobletRectangle.height - totalRectangle.height);
            }

        }
        jobletRectangle = totalRectangle.getCopy();
        return totalRectangle;
    }

    public int getChangeWidth() {
        return this.changeWidth;

    }

    public int getChangeHeight() {
        return this.changeHeight;
    }

    public boolean isReadonly() {
        return true;
    }

    @SuppressWarnings("unchecked")
    public Node getJobletStartNode() {
        if (getProcess() != null) {
            if (getProcess().getSubjobContainers().size() > 0) {
                String subjobStartUniqueName = (String) getProcess().getSubjobContainers().get(0)
                        .getPropertyValue(EParameterName.UNIQUE_NAME.getName());
                if (getProcess() != null && (List<Node>) getProcess().getGraphicalNodes() != null) {
                    for (Node node : (List<Node>) getProcess().getGraphicalNodes()) {
                        if (node.getUniqueName() != null && node.getUniqueName().equals(subjobStartUniqueName)) {
                            return node;
                        }
                    }
                }
            }
        } else if (node != null) {
            return node;
        }

        return null;
    }

    @Override
    public void setPropertyValue(String id, Object value) {

        if (id.equals(EParameterName.COLLAPSED.getName())) {
            // outputs.clear();
            for (IConnection conn : node.getOutgoingConnections()) {
                outputs.add(conn);
            }
            // inputs.clear();
            for (IConnection conn : node.getIncomingConnections()) {
                inputs.add(conn);
            }
            if (needchangeLock) {
                if (!((Boolean) value)) {
                    IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                            IJobletProviderService.class);
                    if (service != null) {
                        service.lockJoblet(this.getNode());
                    }
                } else {
                    IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                            IJobletProviderService.class);
                    if (service != null) {
                        service.unlockJoblet(node, true);
                    }
                }
            }
            needchangeLock = true;
            refreshJobletNodes(false, (Boolean) value);
            if (!canCollapse()) {
                Shell shell = Display.getCurrent().getActiveShell();

                MessageDialog dlg = new MessageDialog(new Shell(shell), "ERROR", null, "Please attach connection!",
                        MessageDialog.QUESTION, new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
                dlg.open();
                return;
            }
            super.setPropertyValue(id, value);
            updateSubjobContainer();
            transferLocation(false);
            refreshJobletConnections();
            fireStructureChange(EParameterName.COLLAPSED.getName(), this);
        } else {
            super.setPropertyValue(id, value);
        }
    }

    public void updateJobletNodes(boolean update) {
        if (!isCollapsed()) {
            refreshJobletNodes(update, isCollapsed());
            updateSubjobContainer();
            transferLocation(update);
            refreshJobletConnections();
        }
    }

    public void refreshJobletNodes(boolean update, boolean coll) {
        if (!coll) {
            JobletUtil util = new JobletUtil();
            IProcess jobletProcess = this.getNode().getComponent().getProcess();
            Set<IConnection> conns = new HashSet<IConnection>();
            List<? extends INode> jobletNodes = jobletProcess.getGraphicalNodes();
            boolean lockByOther = false;
            if (jobletProcess instanceof IProcess2) {
                lockByOther = util.lockByOthers(((IProcess2) jobletProcess).getProperty().getItem());
            }

            // List<NodeContainer> temList = new ArrayList<NodeContainer>(nodeContainers);
            for (NodeContainer nc : nodeContainers) {
                if (this.node.getProcess() instanceof IProcess2) {
                    ((IProcess2) this.node.getProcess()).removeUniqueNodeName(nc.getNode().getUniqueName());
                }
            }
            nodeContainers.clear();
            jobletElements.clear();

            // boolean canAdd = false;
            // boolean canRemove = false;
            for (INode inode : jobletNodes) {
                // canAdd = util.canAdd(temList, inode);
                if ((inode instanceof Node)) {
                    Node temNode = (Node) inode;
                    // if (canAdd) {
                    conns.addAll(temNode.getIncomingConnections());
                    conns.addAll(temNode.getOutgoingConnections());
                    Node jnode = util.cloneNode(temNode, this.node.getProcess(), lockByOther);
                    NodeContainer nodeContainer = util.cloneNodeContainer(temNode.getNodeContainer(), jnode);
                    jnode.setJobletnode(this.node);
                    jnode.setJoblet_unique_name(temNode.getUniqueName());
                    this.nodeContainers.add(nodeContainer);
                    this.jobletElements.add(jnode);
                    this.jobletElements.add(jnode.getNodeLabel());
                    this.jobletElements.add(jnode.getNodeError());
                    this.jobletElements.add(jnode.getNodeProgressBar());
                    // } else if (update) {
                    // for (NodeContainer nodeC : nodeContainers) {
                    // if (nodeC.getNode().getJoblet_unique_name().equals(temNode.getUniqueName())) {
                    // util.updateNode(nodeC.getNode(), temNode);
                    // break;
                    // }
                    // }
                    // }

                }
            }
            // temList = new ArrayList<NodeContainer>(nodeContainers);
            // for (NodeContainer nodeCon : temList) {
            // Node temNode = nodeCon.getNode();
            // canRemove = util.canDelete(jobletNodes, temNode);
            // if (canRemove) {
            // this.nodeContainers.remove(nodeCon);
            // this.jobletElements.remove(temNode);
            // this.jobletElements.remove(temNode.getNodeError());
            // this.jobletElements.remove(temNode.getNodeLabel());
            // this.jobletElements.remove(temNode.getNodeProgressBar());
            // List<? extends IConnection> inCons = new ArrayList<IConnection>(temNode.getIncomingConnections());
            // for (IConnection con : inCons) {
            // con.getTarget().removeInput(con);
            // }
            // List<? extends IConnection> outCons = new ArrayList<IConnection>(temNode.getOutgoingConnections());
            // for (IConnection con : outCons) {
            // con.getTarget().removeOutput(con);
            // }
            // }
            // }
            for (Iterator<IConnection> iter = conns.iterator(); iter.hasNext();) {
                IConnection con = iter.next();
                String sourceName = con.getSource().getUniqueName();
                String targetName = con.getTarget().getUniqueName();
                Node sourceNode = null;
                Node targetNode = null;
                for (NodeContainer nodeC : nodeContainers) {
                    Node connNode = nodeC.getNode();
                    if (connNode.getJoblet_unique_name().equals(sourceName)) {
                        sourceNode = connNode;
                    }
                    if (connNode.getJoblet_unique_name().equals(targetName)) {
                        targetNode = connNode;
                    }
                    if (sourceNode != null && targetNode != null) {
                        util.cloneConnection(con, sourceNode, targetNode);
                        break;
                    }
                }
            }

        }
    }

    private void transferLocation(boolean update) {
        if (update) {
            // do nothing
        }
        if (this.isCollapsed() == true) {
            return;
        }
        if (this.nodeContainers.size() <= 0) {
            return;
        }
        Point oragPoint = this.getNode().getLocation();
        Node startNode = getJobletStartNode();
        if (startNode == null) {
            return;
        }
        Point stratPoint = startNode.getLocation();
        int withe_x = oragPoint.x - stratPoint.x;
        int hight_y = oragPoint.y - stratPoint.y;
        for (NodeContainer nodeCon : this.nodeContainers) {
            Node jobNode = nodeCon.getNode();
            if (jobNode.getJoblet_unique_name().equals(startNode.getUniqueName())) {
                jobNode.setLocation(oragPoint);
            } else {
                Point nodePoint = jobNode.getLocation();
                jobNode.setLocation(new Point(nodePoint.x + withe_x, nodePoint.y + hight_y));
                hasChange = true;
            }
        }

    }

    public void transferLocation(Point oldPos) {
        if (this.isCollapsed() == true) {
            return;
        }
        if (this.nodeContainers.size() <= 0) {
            return;
        }
        Point oragPoint = this.getNode().getLocation();
        Node startNode = getJobletStartNode();
        if (startNode == null) {
            return;
        }
        // Point stratPoint = startNode.getLocation();
        int withe_x = oragPoint.x - oldPos.x;
        int hight_y = oragPoint.y - oldPos.y;
        for (NodeContainer nodeCon : this.nodeContainers) {
            Node jobNode = nodeCon.getNode();
            if (jobNode.getJoblet_unique_name().equals(startNode.getUniqueName())) {
                jobNode.setLocation(oragPoint);
            } else {
                Point nodePoint = jobNode.getLocation();
                jobNode.setLocation(new Point(nodePoint.x + withe_x, nodePoint.y + hight_y));
            }
        }
    }

    private void refreshJobletConnections() {
        Iterator<IConnection> inIterator = inputs.iterator();
        while (inIterator.hasNext()) {
            IConnection conn = inIterator.next();
            if (isCollapsed()) {
                ((Connection) conn).reconnect(conn.getSource(), this.node, conn.getLineStyle());
            } else {
                for (NodeContainer nodeContainer : this.nodeContainers) {
                    Node connNode = nodeContainer.getNode();
                    IElementParameter elePa = this.node.getElementParameter(connNode.getJoblet_unique_name());

                    if (elePa != null) {
                        IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                                IJobletProviderService.class);
                        IElementParameter elechild = null;
                        String uniqueName = null;
                        boolean isTri = service.isTriggerNode(connNode);
                        if (service != null && isTri) {
                            elechild = elePa.getChildParameters().get("COMPONENT_LIST");
                            uniqueName = conn.getMetaName();
                        } else {
                            elechild = elePa.getChildParameters().get("CONNECTION");
                            uniqueName = conn.getUniqueName();
                        }

                        if (elechild != null && elechild.getValue().equals(uniqueName)) {
                            List<? extends INodeConnector> connList = new JobletUtil().createConnectors(connNode,
                                    this.getProcess());
                            // modify///////////////////////////////////////////////////////////////////////////////////////////////////////
                            List<INodeConnector> inodeConnList = new ArrayList<INodeConnector>();
                            inodeConnList.addAll(connList);
                            inodeConnList.addAll(connNode.getListConnector());
                            connNode.setListConnector(inodeConnList);
                            // connNode.getListConnector().addAll(connList);
                            IMetadataTable iTable = this.node.getMetadataTable(connNode.getUniqueName());
                            if (iTable != null && !connNode.getMetadataList().contains(iTable)) {
                                // connNode.getMetadataList().add(iTable);
                            }
                            JobletConnectionReconnectCommand reConnectCommand = new JobletConnectionReconnectCommand(conn);
                            reConnectCommand.setNewTarget(connNode);
                            reConnectCommand.execute();
                            break;
                        } else if (getFlowInput(inputs).size() == 1 && !isTri
                                && new JobletUtil().isJobletInput(connNode, this.getProcess())) {
                            JobletConnectionReconnectCommand reConnectCommand = new JobletConnectionReconnectCommand(conn);
                            reConnectCommand.setNewTarget(connNode);
                            reConnectCommand.execute();
                            break;
                        }
                    }
                }
            }
        }

        Iterator<IConnection> outIterator = outputs.iterator();
        while (outIterator.hasNext()) {
            IConnection conn = outIterator.next();
            if (isCollapsed()) {
                ((Connection) conn).reconnect(this.node, conn.getTarget(), conn.getLineStyle());
            } else {
                for (NodeContainer nodeContainer : this.nodeContainers) {
                    Node connNode = nodeContainer.getNode();
                    if (conn.getConnectorName().equals(connNode.getJoblet_unique_name())) {
                        List<? extends INodeConnector> connList = new JobletUtil().createConnectors(connNode, this.getProcess());
                        List<INodeConnector> inodeConnList = new ArrayList<INodeConnector>();
                        inodeConnList.addAll(connList);
                        inodeConnList.addAll(connNode.getListConnector());
                        connNode.setListConnector(inodeConnList);
                        // connNode.setListConnector(connList);
                        IMetadataTable iTable = this.node.getMetadataTable(connNode.getUniqueName());
                        if (iTable != null && !connNode.getMetadataList().contains(iTable)) {
                            // connNode.getMetadataList().add(iTable);
                        }

                        JobletConnectionReconnectCommand reConnectCommand = new JobletConnectionReconnectCommand(conn);
                        reConnectCommand.setNewSource(connNode);
                        reConnectCommand.execute();
                        // return;
                    }
                }
            }
        }
    }

    public void updateSubjobContainer() {
        fireStructureChange(UPDATE_JOBLET_CONTENT, this);
    }

    public void updateSubjobDisplay() {
        if (!isDisplayed() && isCollapsed()) {
            // if the subjob hidden and collapsed, remove the collapse status first.
            setCollapsed(false);
        }
        fireStructureChange(UPDATE_JOBLET_DISPLAY, this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.ISubjobContainer#isDisplayed()
     */
    public boolean isDisplayed() {
        if (!DesignerPlugin.getDefault().getPreferenceStore().getBoolean(TalendDesignerPrefConstants.DISPLAY_SUBJOBS)) {
            return false;
        }
        return true;// (Boolean) getPropertyValue(EParameterName.SUBJOB_DISPLAYED.getName());
    }

    /**
     * Sets the collapsed.
     * 
     * @param collapsed the collapsed to set
     */
    public void setCollapsed(boolean collapsed) {
        setPropertyValue(EParameterName.COLLAPSED.getName(), new Boolean(collapsed));
    }

    @Override
    public List getElements() {
        if (isCollapsed() || this.jobletElements.size() <= 0) {
            return super.getElements();
        } else {
            return this.jobletElements;
        }

    }

    public List<NodeContainer> getNodeContainers() {
        return this.nodeContainers;
    }

    public boolean canCollapse() {
        if (node.getIncomingConnections().size() > 1) {
            for (NodeContainer nodeContainer : this.nodeContainers) {
                Node connNode = nodeContainer.getNode();
                IElementParameter elePa = this.node.getElementParameter(connNode.getJoblet_unique_name());
                if (elePa != null) {
                    IElementParameter elechild = elePa.getChildParameters().get("CONNECTION");
                    if (elechild != null && (elechild.getValue() == null || "".equals(elechild.getValue()))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private Set<IConnection> getFlowInput(Set<IConnection> inputs) {
        Set<IConnection> finputs = new HashSet<IConnection>();
        Iterator<IConnection> ite = inputs.iterator();
        while (ite.hasNext()) {
            IConnection conn = ite.next();
            if (!conn.getConnectorName().equals("SUBJOB_OK") && !conn.getConnectorName().equals("SUBJOB_ERROR")
                    && !conn.getConnectorName().equals("COMPONENT_OK") && !conn.getConnectorName().equals("COMPONENT_ERROR")) {
                finputs.add(conn);
            }
        }
        return finputs;
    }

    // public Boolean isNeedLock() {
    // return needLock;
    // }

    public void setNeedchangeLock(boolean needchangeLock) {
        this.needchangeLock = needchangeLock;
    }
}
