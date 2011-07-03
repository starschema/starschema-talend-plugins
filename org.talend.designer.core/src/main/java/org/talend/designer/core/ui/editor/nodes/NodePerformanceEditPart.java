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
package org.talend.designer.core.ui.editor.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.talend.commons.ui.utils.workbench.gef.SimpleHtmlFigure;
import org.talend.commons.utils.workbench.preferences.GlobalConstant;

/**
 * EditPart of the NodePerformance.
 * 
 * $Id: NodePerformanceEditPart.java 54939 2011-02-11 01:34:57Z mhirt $
 * 
 */
public class NodePerformanceEditPart extends AbstractGraphicalEditPart implements PropertyChangeListener {

    @Override
    public boolean isSelectable() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
     */
    public void activate() {
        if (!isActive()) {
            super.activate();
            ((NodePerformance) getModel()).getNodeContainer().getNode().addPropertyChangeListener(this);
            ((NodePerformance) getModel()).getNodeContainer().getNodeLabel().addPropertyChangeListener(this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
     */
    public void deactivate() {
        if (isActive()) {
            super.deactivate();
            ((NodePerformance) getModel()).getNodeContainer().getNode().removePropertyChangeListener(this);
            ((NodePerformance) getModel()).getNodeContainer().getNodeLabel().removePropertyChangeListener(this);
        }
    }

    public NodePerformanceEditPart() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    @Override
    protected IFigure createFigure() {
        SimpleHtmlFigure label = new SimpleHtmlFigure() {

            /*
             * (non-Javadoc)
             * 
             * @see org.talend.commons.utils.workbench.gef.SimpleHtmlFigure#paint(org.eclipse.draw2d.Graphics)
             */
            @Override
            public void paint(Graphics graphics) {
                // see bug 2074
                if (GlobalConstant.generatingScreenShoot) {
                    return;
                }

                super.paint(graphics);

            }

        };
        return label;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
     */
    @Override
    protected void refreshVisuals() {
        SimpleHtmlFigure label = (SimpleHtmlFigure) getFigure();

        NodePerformance nodePerf = (NodePerformance) getModel();
        label.setText(nodePerf.getLabel());
        Dimension size = label.getPreferredSize();
        nodePerf.setSize(size);
        Point loc = nodePerf.getLocation();
        Rectangle rectangle = new Rectangle(loc, size);
        ((GraphicalEditPart) getParent()).setLayoutConstraint(this, label, rectangle);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (propName.equals(Node.PERFORMANCE_DATA)) {
            refreshVisuals();
        } else if (propName.equals(Node.LOCATION)) {
            refreshVisuals();
        } else if (propName.equals(NodeLabel.OFFSET_CHANGE)) {
            refreshVisuals();
        }
    }
}
