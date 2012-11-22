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
package org.talend.designer.core.ui.editor.connections;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.swt.graphics.Color;
import org.talend.commons.ui.utils.image.ColorUtils;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IConnectionProperty;
import org.talend.core.model.process.INode;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.utils.ResourceDisposeUtil;

/**
 * Figure corresponding the the connection. <br/>
 * 
 * $Id: ConnectionFigure.java 77219 2012-01-24 01:14:15Z mhirt $
 * 
 */
public class ConnectionFigure extends PolylineConnection {

    private IConnectionProperty connectionProperty;

    private INode linkedNode;

    private IConnection connection;

    /**
     * Used for standard connections.
     * 
     * @param connection
     * @param connectionProperty
     * @param node
     */
    public ConnectionFigure(IConnection connection, IConnectionProperty connectionProperty, INode node) {
        linkedNode = node;
        this.connection = connection;
        setTargetDecoration(new PolygonDecoration());
        setConnectionProperty(connectionProperty);
    }

    /**
     * Only used for partial connections used for dummy state.
     * 
     * @param connectionProperty
     * @param node
     */
    public ConnectionFigure(IConnectionProperty connectionProperty, Node node) {
        this(null, connectionProperty, node);
    }

    @Override
    public void paint(Graphics graphics) {
        if (((Node) linkedNode).getJobletNode() != null) {
            Node jnode = (Node) ((Node) linkedNode).getJobletNode();
            SubjobContainer subjobCon = jnode.getNodeContainer().getSubjobContainer();
            if (subjobCon != null && subjobCon.isCollapsed() && connection != null && !connection.isSubjobConnection()) {

                Node subjobStartNode = jnode.getNodeContainer().getSubjobContainer().getSubjobStartNode();
                if (subjobStartNode.equals(jnode) && ((Node) connection.getTarget()).getJobletNode() != null) {
                    // do nothing
                } else {
                    // only dependency links will be drawn
                    if (!connection.getLineStyle().hasConnectionCategory(IConnectionCategory.DEPENDENCY)) {
                        return;
                    }
                    if (!connection.getSource().equals(subjobStartNode) && !connection.getTarget().equals(subjobStartNode)) {
                        return;
                    }
                    if (connection.getTarget().getDesignSubjobStartNode().equals(subjobStartNode)) {
                        return;
                    }
                }

            }
        } else {
            if (((Node) linkedNode).getNodeContainer().getSubjobContainer() != null
                    && ((Node) linkedNode).getNodeContainer().getSubjobContainer().isCollapsed() && connection != null
                    && !connection.isSubjobConnection()) {

                Node subjobStartNode = ((Node) linkedNode).getNodeContainer().getSubjobContainer().getSubjobStartNode();
                // only dependency links will be drawn
                if (!connection.getLineStyle().hasConnectionCategory(IConnectionCategory.DEPENDENCY)) {
                    return;
                }
                if (!connection.getSource().equals(subjobStartNode) && !connection.getTarget().equals(subjobStartNode)) {
                    return;
                }
                if (connection.getTarget().getDesignSubjobStartNode().equals(subjobStartNode)) {
                    return;
                }
            }
        }

        if (getAlpha() != null && getAlpha() != -1) {
            graphics.setAlpha(getAlpha());
        }
        super.paint(graphics);
    }

    protected void setConnectionProperty(IConnectionProperty connectionProperty) {
        this.connectionProperty = connectionProperty;
        setLineStyle(connectionProperty.getLineStyle());
        Color color = ColorUtils.getCacheColor(connectionProperty.getRGB());
        ResourceDisposeUtil.setAndCheckColor(this, color, true);
    }

    public void disposeColors() {
        // ResourceDisposeUtil.disposeColor(getForegroundColor());
    }

    /**
     * Getter for connectionProperty.
     * 
     * @return the connectionProperty
     */
    public IConnectionProperty getConnectionProperty() {
        return this.connectionProperty;
    }

    /**
     * Getter for connection.
     * 
     * @return the connection
     */
    public IConnection getConnection() {
        return this.connection;
    }

}
