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
package org.talend.designer.core.ui.editor.outline;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * This class gets all node to add them in the tree in the Outline It doesn't give any detail, it just adds the roots
 * objects. <br/>
 * 
 * $Id: ProcessTreeEditPart.java 54939 2011-02-11 01:34:57Z mhirt $
 * 
 */
public class ProcessTreeEditPart extends AbstractTreeEditPart implements PropertyChangeListener {

    private NodeTransferDragSourceListener nodeTransferDragSourceListener = NodeTransferDragSourceListener.getInstance();

    public ProcessTreeEditPart(Object model) {
        super(model);
    }

    @Override
    public void setSelected(int value) {
        nodeTransferDragSourceListener.setEditPart(this);
        super.setSelected(value);
    }

    // TransferDragSourceListener dragDropListener = new TransferDragSourceListener() {
    //
    // TextTransfer transfer;
    //
    // public Transfer getTransfer() {
    // transfer = TextTransfer.getInstance();
    // return transfer;
    // }
    //
    // public void dragFinished(final DragSourceEvent event) {
    // }
    //
    // public void dragSetData(final DragSourceEvent event) {
    // INode node = (INode) currentEditPart.getParent().getModel();
    // String value = ElementParameterParser.parse(node, ((INodeReturn) currentEditPart.getModel()).getVarName());
    // event.data = value;
    // }
    //
    // public void dragStart(final DragSourceEvent event) {
    // event.doit = true;
    // }
    //
    // };

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editparts.AbstractEditPart#activate()
     */
    public void activate() {
        super.activate();
        ((Process) getModel()).addPropertyChangeListener(this);
        nodeTransferDragSourceListener.setEditPart(this);
        getViewer().addDragSourceListener(nodeTransferDragSourceListener.getNodeTransferDragSourceListener());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editparts.AbstractEditPart#deactivate()
     */
    public void deactivate() {
        ((Process) getModel()).removePropertyChangeListener(this);
        nodeTransferDragSourceListener.setEditPart(this);
        getViewer().removeDragSourceListener(nodeTransferDragSourceListener.getNodeTransferDragSourceListener());
        super.deactivate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
     */
    protected List getModelChildren() {
        return ((Process) getModel()).getGraphicalNodes();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(final PropertyChangeEvent change) {
        refreshChildren();

    }
}
