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
package org.talend.designer.core.ui.editor.process;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.talend.core.model.process.IGraphicalNode;
import org.talend.designer.core.ui.editor.cmd.CreateNodeContainerCommand;
import org.talend.designer.core.ui.editor.cmd.CreateNoteCommand;
import org.talend.designer.core.ui.editor.cmd.MoveNodeCommand;
import org.talend.designer.core.ui.editor.cmd.MoveNoteCommand;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.notes.Note;
import org.talend.designer.core.ui.editor.notes.NoteEditPart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;

/**
 * Edit policy of the Diagram that will allow to move the objects on it and create nodes. <br/>
 * 
 * $Id: ProcessLayoutEditPolicy.java 79831 2012-03-15 05:18:56Z fwang $
 * 
 */
public class ProcessLayoutEditPolicy extends XYLayoutEditPolicy {

    // ------------------------------------------------------------------------
    // Overridden from ConstrainedLayoutEditPolicy

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChildEditPolicy(org.eclipse.gef.EditPart)
     */
    protected EditPolicy createChildEditPolicy(final EditPart child) {
        ProcessResizableEditPolicy policy = new ProcessResizableEditPolicy();
        policy.setResizeDirections(0);
        return policy;
    }

    // ------------------------------------------------------------------------
    // Abstract methods from ConstrainedLayoutEditPolicy

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createAddCommand(org.eclipse.gef.EditPart,
     * java.lang.Object)
     */
    protected Command createAddCommand(final EditPart child, final Object constraint) {
        if (child instanceof NodePart) {
            if (((Node) child.getModel()).isReadOnly()) {
                return null;
            }
            MoveNodeCommand locationCommand = new MoveNodeCommand((Node) child.getModel(), ((Rectangle) constraint).getLocation());
            return locationCommand;
        }
        // add for bug TDI-7706,when moving Node,the note can't move
        if (child instanceof NoteEditPart) {
            if (((Note) child.getModel()).isReadOnly()) {
                return null;
            }
            MoveNoteCommand locationCommand = new MoveNoteCommand((Note) child.getModel(), ((Rectangle) constraint).getLocation());
            return locationCommand;
        }

        return null;

        // Command moveContainer = new Command("test");
        // return moveContainer; // no support for adding
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChangeConstraintCommand(org.eclipse.gef.EditPart,
     * java.lang.Object)
     */
    public Command createChangeConstraintCommand(final EditPart child, final Object constraint) {
        // return a command to move the part to the location given by the constraint
        if (child instanceof NodePart) {
            if (((Node) child.getModel()).isReadOnly()) {
                return null;
            }
            MoveNodeCommand locationCommand = new MoveNodeCommand((Node) child.getModel(), ((Rectangle) constraint).getLocation());
            return locationCommand;
        }
        if (child instanceof NoteEditPart) {
            if (((Note) child.getModel()).isReadOnly()) {
                return null;
            }
            MoveNoteCommand locationCommand = new MoveNoteCommand((Note) child.getModel(), ((Rectangle) constraint).getLocation());
            return locationCommand;
        }
        if (child instanceof SubjobContainerPart) {
            SubjobContainer sjc = (SubjobContainer) child.getModel();
            Point sjcLocation = sjc.getSubjobContainerRectangle().getLocation();
            Point translationNeeded = new Point(((Rectangle) constraint).getLocation().x - sjcLocation.x,
                    ((Rectangle) constraint).getLocation().y - sjcLocation.y);
            CompoundCommand cc = new CompoundCommand();
            for (NodeContainer nc : sjc.getNodeContainers()) {
                if (nc.isReadOnly()) {
                    return null;
                }
                IGraphicalNode node = nc.getNode();
                Point nodeLocation = node.getLocation();
                MoveNodeCommand locationCommand = new MoveNodeCommand(nc.getNode(), nodeLocation.getTranslated(translationNeeded));
                cc.add(locationCommand);
            }
            return cc;
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // Abstract methods from LayoutEditPolicy

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
     */
    protected Command getCreateCommand(final CreateRequest request) {
        if (((Process) getHost().getModel()).isReadOnly()) {
            return null;
        }
        Rectangle constraint = (Rectangle) getConstraintFor(request);

        Command command = null;
        if (Note.class.equals(request.getNewObjectType())) {
            command = new CreateNoteCommand((Process) getHost().getModel(), (Note) request.getNewObject(),
                    constraint.getLocation());
        } else if (request.getNewObject() instanceof Node) {
            NodeContainer nodeContainer = null;
            if (((Node) request.getNewObject()).isJoblet()) {
                nodeContainer = new JobletContainer((Node) request.getNewObject());
            } else {
                nodeContainer = new NodeContainer((Node) request.getNewObject());
            }
            command = new CreateNodeContainerCommand((Process) getHost().getModel(), nodeContainer, constraint.getLocation());
        }

        return command;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getDeleteDependantCommand(org.eclipse.gef.Request)
     */
    protected Command getDeleteDependantCommand(final Request request) {
        return null; // no support for deleting a dependant
    }
}
