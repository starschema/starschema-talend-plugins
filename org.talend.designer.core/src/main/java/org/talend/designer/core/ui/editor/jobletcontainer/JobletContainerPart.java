package org.talend.designer.core.ui.editor.jobletcontainer;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.CompoundSnapToHelper;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.rulers.RulerProvider;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerLayoutEditPolicy;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.process.NodeSnapToGeometry;
import org.talend.designer.core.ui.editor.process.ProcessPart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;

public class JobletContainerPart extends NodeContainerPart {

    @Override
    protected IFigure createFigure() {
        JobletContainerFigure JobletContainerFigure = new JobletContainerFigure((JobletContainer) this.getModel());
        // Node node = ((NodeContainer) getModel()).getNode();
        // if (node.isActivate()) {
        // JobletContainerFigure.setAlpha(-1);
        // } else {
        // JobletContainerFigure.setAlpha(Node.ALPHA_VALUE);
        // }
        // IElementParameter param = node.getElementParameter(EParameterName.INFORMATION.getName());
        // if (param != null) {
        // boolean showInfoFlag = Boolean.TRUE.equals(param.getValue());
        // JobletContainerFigure.updateStatus(node.getStatus(), showInfoFlag);
        // JobletContainerFigure.setInfoHint(node.getShowHintText());
        // }
        Node node = ((NodeContainer) getModel()).getNode();
        JobletContainerFigure.updateStatus(node.getStatus());

        return JobletContainerFigure;
    }

    @Override
    protected void unregisterVisuals() {
        super.unregisterVisuals();
        ((JobletContainerFigure) getFigure()).disposeColors();
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    public void activate() {
        super.activate();
        ((JobletContainer) getModel()).addPropertyChangeListener(this);
        Node node = ((JobletContainer) getModel()).getNode();
        node.addPropertyChangeListener(this);
    }

    public void deactivate() {
        super.deactivate();
        ((JobletContainer) getModel()).removePropertyChangeListener(this);
    }

    @Override
    public void setSelected(int value) {
        super.setSelected(SELECTED_NONE);
    }

    public NodePart getNodePart() {
        Object o = this.getChildren().get(0);
        if (o instanceof NodePart) {
            return (NodePart) o;
        }
        return null;
    }

    // /*
    // * (non-Javadoc)
    // *
    // * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
    // */
    // @Override
    // protected IFigure createFigure() {
    // JobletContainerFigure JobletContainerFigure = new JobletContainerFigure((NodeContainer) this.getModel());
    // Node node = ((NodeContainer) getModel()).getNode();
    // if (node.isActivate()) {
    // JobletContainerFigure.setAlpha(-1);
    // } else {
    // JobletContainerFigure.setAlpha(Node.ALPHA_VALUE);
    // }
    // IElementParameter param = node.getElementParameter(EParameterName.INFORMATION.getName());
    // if (param != null) {
    // boolean showInfoFlag = Boolean.TRUE.equals(param.getValue());
    // JobletContainerFigure.updateStatus(node.getStatus(), showInfoFlag);
    // JobletContainerFigure.setInfoHint(node.getShowHintText());
    // }
    // return JobletContainerFigure;
    // }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new NodeContainerLayoutEditPolicy());
    }

    protected void refreshVisuals() {
        Boolean isDisplayJoblet = ((JobletContainer) this.getModel()).isDisplayed();
        if (getParent() == null) {// || !isDisplayJoblet
            return;
        }
        // Rectangle rectangle = ((JobletContainer) this.getModel()).getSubjobContainerRectangle();
        // if (rectangle == null) {
        // return;
        // }
        // ((SubjobContainerFigure) getFigure()).initializeSubjobContainer(rectangle);
        // // added for bug 4005
        // if (getFigure().getParent() != null) {
        // ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), rectangle);
        // }

        Rectangle rectangle = ((JobletContainer) this.getModel()).getJobletContainerRectangle();
        if (rectangle == null) {
            return;
        }
        Rectangle cleanRectangle = ((NodeContainer) this.getModel()).getNodeMarkRectangle();
        if (this.getParent() != null) {
            if (this.getParent().getModel() instanceof SubjobContainer) {
                Rectangle subjobRec = ((SubjobContainer) this.getParent().getModel()).getSubjobContainerRectangle();
                ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), rectangle);
            }
        }

        ((JobletContainerFigure) getFigure()).initializejobletContainer(rectangle);

    }

    protected List getModelChildren() {
        return ((JobletContainer) this.getModel()).getElements();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent changeEvent) {
        String prop = changeEvent.getPropertyName();
        boolean needUpdateSubjob = false;
        if (prop.equals(EParameterName.HINT.getName())) {
            Node node = ((NodeContainer) getModel()).getNode();
            // ((JobletContainerFigure) figure).setInfoHint(node.getShowHintText());
        } else if (JobletContainer.UPDATE_JOBLET_CONTENT.equals(prop)) {
            refresh();
            List<AbstractGraphicalEditPart> childrens = getChildren();
            for (AbstractGraphicalEditPart part : childrens) {
                part.refresh();
            }
            needUpdateSubjob = true;
        } else if (JobletContainer.UPDATE_JOBLET_CONNECTIONS.equals(prop)) {
            refreshSourceConnections();
        } else if (JobletContainer.UPDATE_JOBLET_TITLE_COLOR.equals(prop)) {
            if (getFigure() instanceof JobletContainerFigure) {
                ((JobletContainerFigure) getFigure()).updateSubJobTitleColor();
                refreshVisuals();
            }
        } else if (JobletContainer.UPDATE_JOBLET_DISPLAY.equals(prop)) {
            // List<NodeContainer> tmpList = new ArrayList<NodeContainer>(((JobletContainer)
            // getModel()).getNodeContainers());
            // ((SubjobContainer) getModel()).getNodeContainers().clear();
            // refreshChildren();
            // List elems = ((Process) getParent().getModel()).getElements();
            // elems.remove(getModel());
            // EditPart parent = getParent();
            // parent.refresh();
            // ((JobletContainer) getModel()).getNodeContainers().addAll(tmpList);
            // elems.add(getModel());
            // parent.refresh();
            needUpdateSubjob = true;
        } else { // can only be UPDATE_SUBJOB_DATA, need to modify if some others are added
            if (getFigure() instanceof JobletContainerFigure) {
                ((JobletContainerFigure) getFigure()).updateData();
                refreshVisuals();
            }
        }

        // if (changeEvent.getPropertyName().equals(Node.UPDATE_STATUS)) {
        // Node node = ((NodeContainer) getModel()).getNode();
        // ((JobletContainerFigure) this.getFigure()).updateErrorFlag(node.isErrorFlag());
        // ((JobletContainerFigure) this.getFigure()).setShowCompareMark(node.isCompareFlag() && !node.isErrorFlag());

        // IElementParameter param = node.getElementParameter(EParameterName.INFORMATION.getName());
        // if (param != null) {
        // boolean showInfoFlag = Boolean.TRUE.equals(param.getValue());
        // if (changeEvent.getNewValue() instanceof Integer) {
        // Integer status = (Integer) changeEvent.getNewValue();
        // if (status != null) {
        // ((JobletContainerFigure) this.getFigure()).updateStatus(status, showInfoFlag);
        // }
        // ((JobletContainerFigure) this.getFigure()).setInfoHint(node.getShowHintText());
        // }
        // refreshVisuals();
        // }
        // }
        if (changeEvent.getPropertyName().equals(EParameterName.ACTIVATE.getName())) {
            Node node = ((NodeContainer) getModel()).getNode();
            if (node.isActivate()) {
                // ((JobletContainerFigure) figure).setAlpha(-1);
                ((JobletContainerFigure) figure).repaint();
                refreshVisuals();
            } else {
                // ((JobletContainerFigure) figure).setAlpha(Node.ALPHA_VALUE);
                ((JobletContainerFigure) figure).repaint();
                refreshVisuals();
            }
        }
        if (changeEvent.getPropertyName().equals(Node.PERFORMANCE_DATA)) {
            refreshVisuals();
        }

        if (needUpdateSubjob) {
            EditPart editPart = getParent();
            if (editPart != null) {
                while ((!(editPart instanceof ProcessPart)) && (!(editPart instanceof SubjobContainerPart))) {
                    editPart = editPart.getParent();
                }
                if (editPart instanceof SubjobContainerPart) {
                    // Node node = ((NodeContainer) getModel()).getNode();
                    NodeContainer nc = (NodeContainer) getModel();
                    // Rectangle rec = new Rectangle(node.getLocation(), node.getSize());
                    boolean isCollapse = ((JobletContainer) this.getModel()).isCollapsed();
                    int changewidth = ((JobletContainer) this.getModel()).getChangeWidth();
                    int changeheight = ((JobletContainer) this.getModel()).getChangeHeight();
                    ((SubjobContainer) editPart.getModel()).refreshNodesLocation(isCollapse, nc, changewidth, changeheight);
                    editPart.refresh();
                }
            }
        }
        if (changeEvent.getPropertyName().equals(Node.UPDATE_STATUS)) {
            Node node = ((NodeContainer) getModel()).getNode();
            ((JobletContainerFigure) getFigure()).updateStatus(node.getStatus());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class key) {
        if (key == SnapToHelper.class) {
            List<Object> snapStrategies = new ArrayList<Object>();
            Boolean val = (Boolean) getViewer().getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY);

            val = (Boolean) getViewer().getProperty(NodeSnapToGeometry.PROPERTY_SNAP_ENABLED);
            if (val != null && val.booleanValue()) {
                snapStrategies.add(new NodeSnapToGeometry(this));
            }

            val = (Boolean) getViewer().getProperty(SnapToGrid.PROPERTY_GRID_ENABLED);
            if (val != null && val.booleanValue()) {
                snapStrategies.add(new SnapToGrid(this));
            }

            if (snapStrategies.size() == 0) {
                return null;
            }
            if (snapStrategies.size() == 1) {
                return snapStrategies.get(0);
            }

            SnapToHelper[] ss = new SnapToHelper[snapStrategies.size()];
            for (int i = 0; i < snapStrategies.size(); i++) {
                ss[i] = (SnapToHelper) snapStrategies.get(i);
            }
            return new CompoundSnapToHelper(ss);
        }

        return super.getAdapter(key);
    }

}
