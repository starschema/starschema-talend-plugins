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
package org.talend.designer.core.ui.editor.cmd;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.views.contexts.ContextsView;

/**
 * Command that will add a new parameter in all contexts. <br/>
 * 
 * $Id: ContextAddParameterCommand.java 77219 2012-01-24 01:14:15Z mhirt $
 * 
 */
public class ContextAddParameterCommand extends Command {

    IContextManager contextManager;

    IContextParameter contextParam;

    Map<IContext, IContextParameter> addedParameters;

    public ContextAddParameterCommand(IContextManager contextManager, IContextParameter contextParam) {
        this.contextManager = contextManager;
        this.contextParam = contextParam;
        this.setLabel(Messages.getString("ContextAddParameterCommand.label")); //$NON-NLS-1$
        addedParameters = new HashMap<IContext, IContextParameter>();
    }

    private void refreshPropertyView() {
        // IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        // IViewPart view = page.findView("org.eclipse.ui.views.PropertySheet"); //$NON-NLS-1$
        // PropertySheet sheet = (PropertySheet) view;
        // final IPage currentPage = sheet.getCurrentPage();
        // if (currentPage instanceof TabbedPropertySheetPage) {
        // TabbedPropertySheetPage tabbedPropertySheetPage = (TabbedPropertySheetPage) currentPage;
        // tabbedPropertySheetPage.refresh();
        // }
    }

    /**
     * qzhang Comment method "refreshContextView".
     */
    private void refreshContextView() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IViewPart view2 = page.findView(ContextsView.ID);
        if (view2 instanceof ContextsView) {
            ((ContextsView) view2).updateContextView(true);
        }
    }

    @Override
    public void execute() {
        for (int i = 0; i < contextManager.getListContext().size(); i++) {
            IContext context = contextManager.getListContext().get(i);
            IContextParameter toAdd = contextParam.clone();
            addedParameters.put(context, toAdd);
            toAdd.setContext(context);
            context.getContextParameterList().add(toAdd);
        }
        contextManager.fireContextsChangedEvent();
        refreshContextView();
    }

    @Override
    public void redo() {
        execute();
    }

    @Override
    public void undo() {
        for (int i = 0; i < contextManager.getListContext().size(); i++) {
            IContext context = contextManager.getListContext().get(i);
            context.getContextParameterList().remove(addedParameters.get(context));
        }
        contextManager.fireContextsChangedEvent();
        refreshContextView();
    }
}
