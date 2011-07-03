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
package org.talend.designer.core.ui.editor.cmd;

import org.eclipse.gef.commands.Command;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.views.contexts.ContextsView;

/**
 * Command that will change the default context.
 * 
 * $Id: ContextChangeDefaultCommand.java 54939 2011-02-11 01:34:57Z mhirt $
 * 
 */
public class ContextChangeDefaultCommand extends Command {

    IContext newDefault;

    IContext oldDefault;

    IContextManager contextManager;

    public ContextChangeDefaultCommand(IContextManager contextManager, IContext newDefault) {
        this.newDefault = newDefault;
        this.contextManager = contextManager;
        this.oldDefault = contextManager.getDefaultContext();
        this.setLabel(Messages.getString("ContextChangeDefaultCommand.label")); //$NON-NLS-1$
    }

    /**
     * qzhang Comment method "refreshContextView".
     */
    private void refreshContextView() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IViewPart view2 = page.findView(ContextsView.ID);
        if (view2 instanceof ContextsView) {
            ((ContextsView) view2).updateContextView(true, false);
        }
    }

    @Override
    public void execute() {
        contextManager.setDefaultContext(newDefault);
        contextManager.fireContextsChangedEvent();
        refreshContextView();
    }

    @Override
    public void redo() {
        execute();
    }

    @Override
    public void undo() {
        contextManager.setDefaultContext(oldDefault);
        contextManager.fireContextsChangedEvent();
        // refreshContextView();
    }
}
