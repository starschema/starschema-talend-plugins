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
package org.talend.designer.core.ui.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.core.model.components.IComponent;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainer;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainerFigure;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainerPart;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.notes.NoteEditPart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;

/**
 * Modification of the delete action to enhance the speed of the action from GEF. <br/>
 * 
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 * 
 */
public class GEFDeleteAction extends DeleteAction {

    public GEFDeleteAction(IWorkbenchPart part) {
        super(part);
    }

    @Override
    protected boolean calculateEnabled() {
        List objects = getSelectedObjects();
        if (objects.isEmpty()) {
            return false;
        }

        if (!(objects.get(0) instanceof EditPart)) {
            return false;
        }
        AbstractProcessProvider pProvider = AbstractProcessProvider.findProcessProviderFromPID(IComponent.JOBLET_PID);
        if (pProvider != null) {
            boolean allJobletnode = true;
            boolean hasNode = false;
            for (Object o : objects) {
                if (o instanceof NodePart) {
                    hasNode = true;
                    Node no = (Node) ((NodePart) o).getModel();
                    if (no.getJobletNode() == null) {
                        allJobletnode = false;
                    }
                    if (!pProvider.canDeleteNode(no)) {
                        return false;
                    }
                }
            }
            if (allJobletnode && hasNode) {
                return false;
            }
        }
        return true;
    }

    public static List filterSameObject(List list) {
        List newList = new ArrayList();
        for (Object object : list) {
            if (!newList.contains(object)) {
                newList.add(object);
            }
        }

        return newList;

    }

    @Override
    public Command createDeleteCommand(List objects) {
        objects = filterSameObject(objects);
        if (objects.isEmpty()) {
            return null;
        }
        if (!(objects.get(0) instanceof EditPart)) {
            return null;
        }

        EditPart object = (EditPart) objects.get(0);

        List nodeParts = new ArrayList();
        List noteParts = new ArrayList();
        List others = new ArrayList(objects);

        for (Object o : objects) {
            if (o instanceof NodePart) {
                others.remove(o);
                Node model = (Node) ((NodePart) o).getModel();
                if (model.getJobletNode() != null) {
                    continue;
                }

                nodeParts.add(o);
            } else if (o instanceof NoteEditPart) {
                noteParts.add(o);
                others.remove(o);
            } else if (o instanceof SubjobContainerPart) {
                others.remove(o);
                SubjobContainerPart subjob = (SubjobContainerPart) o;

                for (Iterator iterator = subjob.getChildren().iterator(); iterator.hasNext();) {
                    NodeContainerPart nodeContainerPart = (NodeContainerPart) iterator.next();
                    if (nodeContainerPart instanceof JobletContainerPart) {
                        JobletContainer jobletCon = (JobletContainer) ((JobletContainerPart) nodeContainerPart).getModel();
                        JobletContainerFigure jobletFigure = (JobletContainerFigure) ((JobletContainerPart) nodeContainerPart)
                                .getFigure();
                        if (!jobletCon.isCollapsed()) {
                            jobletFigure.doCollapse();
                        }
                    }
                    NodePart nodePart = nodeContainerPart.getNodePart();
                    if (nodePart != null) {
                        nodeParts.add(nodePart);
                    }
                }
            }
        }

        if (others.size() == 0) { // so notes & nodes only
            CompoundCommand cpdCmd = new CompoundCommand();
            cpdCmd.setLabel(Messages.getString("GEFDeleteAction.DeleteItems")); //$NON-NLS-1$
            if (nodeParts.size() != 0) {
                GroupRequest deleteReq = new GroupRequest(RequestConstants.REQ_DELETE);
                deleteReq.setEditParts(nodeParts);

                cpdCmd.add(((NodePart) nodeParts.get(0)).getCommand(deleteReq));
            }
            if (noteParts.size() != 0) {
                GroupRequest deleteReq = new GroupRequest(RequestConstants.REQ_DELETE);
                deleteReq.setEditParts(noteParts);
                cpdCmd.add(((NoteEditPart) noteParts.get(0)).getCommand(deleteReq));
            }

            return cpdCmd;
        } else {
            GroupRequest deleteReq = new GroupRequest(RequestConstants.REQ_DELETE);
            deleteReq.setEditParts(objects);

            Command cmd = object.getCommand(deleteReq);
            return cmd;
        }
    }
}
