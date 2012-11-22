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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.ConnectionCreationTool;

/**
 * This tool is used to create a connection in the context menu. After the constructor, the function
 * "performConnectionStartWith" must be called, it will define the first point for the connection. <br/>
 * 
 * $Id: TalendConnectionCreationTool.java 7038 2007-11-15 14:05:48Z plegall $
 * 
 */
public class TalendConnectionCreationTool extends ConnectionCreationTool {

    private boolean fromMenu;

    public TalendConnectionCreationTool(CreationFactory factory, boolean fromMenu) {
        super(factory);
        this.fromMenu = fromMenu;
        setUnloadWhenFinished(true);
    }

    /**
     * The node part of the source must be given in argument. This will define the fist point of the connection.
     * 
     * @param sourcePart the edit part that will be the source of the connection
     */
    public void performConnectionStartWith(EditPart sourcePart) {
        setConnectionSource(sourcePart);
        updateTargetRequest();
        Command cmd = ((AbstractEditPart) sourcePart).getCommand(getTargetRequest());
        setCurrentCommand(cmd);
        setState(STATE_CONNECTION_STARTED);
    }

    /*
     * override this method for issue 1253.
     * 
     * @see org.eclipse.gef.tools.AbstractConnectionCreationTool#handleCreateConnection()
     */
    protected boolean handleCreateConnection() {
        Command endCommand = getCommand();
        if (endCommand != null) {
            return super.handleCreateConnection();
        }
        if (isInState(STATE_TERMINAL)) {
            // Fake a drag to cause feedback to be displayed immediately on mouse down.
            setState(STATE_CONNECTION_STARTED);
            handleDrag();
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.tools.ConnectionCreationTool#handleButtonDown(int)
     */
    @Override
    protected boolean handleButtonDown(int button) {
        if (!fromMenu && button == 3) {
            return super.handleButtonDown(1);
        }
        return super.handleButtonDown(button);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.tools.AbstractConnectionCreationTool#handleButtonUp(int)
     */
    @Override
    protected boolean handleButtonUp(int button) {
        if (!fromMenu && button == 3) {
            super.handleButtonDown(1);
        }
        return super.handleButtonUp(button);
    }

}
