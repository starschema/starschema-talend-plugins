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
package org.talend.designer.core.ui.editor.nodecontainer;

import java.util.Iterator;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodeFigure;

/**
 * This class describes the figures that will be used for drag and drop each objects on the diagram. <br/>
 * 
 * $Id: NodeContainerResizableEditPolicy.java 77219 2012-01-24 01:14:15Z mhirt $
 * 
 */
public class NodeContainerResizableEditPolicy extends ResizableEditPolicy {

    /**
     * Creates the figure used for feedback.
     * 
     * @return the new feedback figure
     */
    protected IFigure createDragSourceFeedbackFigure() {
        IFigure figure = createFigure((GraphicalEditPart) getHost(), null);

        figure.setBounds(getInitialFeedbackBounds());
        addFeedback(figure);
        return figure;
    }

    protected IFigure createFigure(GraphicalEditPart part, IFigure parent) {
        IFigure child = getCustomFeedbackFigure(part.getModel());

        if (parent != null) {
            parent.add(child);
        }

        Rectangle childBounds = part.getFigure().getBounds().getCopy();

        IFigure walker = part.getFigure().getParent();

        while (walker != ((GraphicalEditPart) part.getParent()).getFigure()) {
            walker.translateToParent(childBounds);
            walker = walker.getParent();
        }

        child.setBounds(childBounds);

        Iterator i = part.getChildren().iterator();

        while (i.hasNext()) {
            createFigure((GraphicalEditPart) i.next(), child);
        }

        return child;
    }

    /**
     * This will take the figure of the node and set it as feedback figure.
     * 
     * @param modelPart
     * @return
     */
    protected IFigure getCustomFeedbackFigure(Object modelPart) {
        IFigure figure;

        if (modelPart instanceof Node) {
            Node node = (Node) modelPart;
            figure = new Figure();
            figure.setOpaque(false);
            NodeFigure nodeFigure = new NodeFigure(node);
            figure.add(nodeFigure);
            nodeFigure.setLocation(new Point(32, 32));
            if (node.isStart()) {
                nodeFigure.setBackgroundColor(NodeFigure.START_COLOR);
            } else {
                nodeFigure.setOpaque(false);
            }
        } else {
            figure = new RectangleFigure();
            ((RectangleFigure) figure).setXOR(true);
            ((RectangleFigure) figure).setFill(true);
            figure.setBackgroundColor(ColorConstants.darkGreen);
            figure.setForegroundColor(ColorConstants.white);
        }

        return figure;
    }

    /**
     * Returns the layer used for displaying feedback.
     * 
     * @return the feedback layer
     */
    protected IFigure getFeedbackLayer() {
        return getLayer(LayerConstants.SCALED_FEEDBACK_LAYER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editpolicies.NonResizableEditPolicy#getInitialFeedbackBounds()
     */
    protected Rectangle getInitialFeedbackBounds() {
        return getHostFigure().getBounds().getExpanded(32, 32);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editpolicies.NonResizableEditPolicy#getOrphanCommand(org.eclipse.gef.Request)
     */
    @Override
    protected Command getOrphanCommand(Request req) {
        // if (req instanceof ChangeBoundsRequest) {
        // ChangeBoundsRequest cbr = (ChangeBoundsRequest) req;
        // EditPart editPart = (EditPart) cbr.getEditParts().get(0);
        // if (editPart instanceof NodePart) {
        // MoveNodeCommand locationCommand = new MoveNodeCommand((Node) editPart.getModel(), cbr.getLocation());
        // return locationCommand;
        // }
        // }
        return super.getOrphanCommand(req);
    }
}
