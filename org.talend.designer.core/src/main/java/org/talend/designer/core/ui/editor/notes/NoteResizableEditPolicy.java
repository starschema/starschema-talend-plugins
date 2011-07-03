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
package org.talend.designer.core.ui.editor.notes;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.talend.designer.core.ui.editor.cmd.ResizeNoteCommand;
import org.talend.designer.core.ui.editor.process.Process;

/**
 */
public class NoteResizableEditPolicy extends ResizableEditPolicy {

    @Override
    protected Command getResizeCommand(ChangeBoundsRequest request) {
        Object parent = getHost().getParent().getModel();
        if (!(parent instanceof Process)) {
            return null;
        }

        Note note = (Note) getHost().getModel();
        if (note.isReadOnly()) {
            return null;
        }

        return new ResizeNoteCommand(note, new Dimension(note.getSize().width + request.getSizeDelta().width,
                note.getSize().height + request.getSizeDelta().height));
    }

}
