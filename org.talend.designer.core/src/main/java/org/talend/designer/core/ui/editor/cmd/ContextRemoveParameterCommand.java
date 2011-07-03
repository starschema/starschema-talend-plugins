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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * Command that will remove a parameter in all contexts. <br/>
 * 
 * $Id: ContextRemoveParameterCommand.java 54939 2011-02-11 01:34:57Z mhirt $
 * 
 */
public class ContextRemoveParameterCommand extends Command {

    private IContextManager contextManager;

    private Set<String> contextParamNames = new HashSet<String>();

    private Map<String, List<IContextParameter>> mapParams = new HashMap<String, List<IContextParameter>>();

    public ContextRemoveParameterCommand(IContextManager contextManager, Set<String> contextParamNames) {
        init(contextManager, contextParamNames);
    }

    public ContextRemoveParameterCommand(IContextManager contextManager, String contextParamName) {
        Set<String> names = new HashSet<String>();
        if (contextParamName != null) {
            names.add(contextParamName);
        }
        init(contextManager, names);
    }

    private void init(IContextManager contextManager, Set<String> contextParamNames) {
        this.contextManager = contextManager;
        if (contextParamNames != null) {
            this.contextParamNames.addAll(contextParamNames);
        }
        this.setLabel(Messages.getString("ContextRemoveParameterCommand.label")); //$NON-NLS-1$

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
            ((ContextsView) view2).updateContextView(true, false, false);
        }
    }

    @Override
    public void execute() {
        mapParams.clear();
        for (int i = 0; i < contextManager.getListContext().size(); i++) {
            IContext context = contextManager.getListContext().get(i);
            List<IContextParameter> listParams = context.getContextParameterList();

            boolean found = false;
            List<IContextParameter> movedList = new ArrayList<IContextParameter>();

            for (int j = 0; j < listParams.size(); j++) {
                IContextParameter contextParameter = listParams.get(j);
                if (contextParamNames.contains(contextParameter.getName())) {
                    movedList.add(contextParameter);
                    found = true;
                }
                if (movedList.size() == contextParamNames.size()) { // has finished search
                    break;
                }
            }
            if (found) {
                mapParams.put(context.getName(), movedList);
                listParams.removeAll(movedList);
            } else { // not find anything in first
                return;
            }
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
            List<IContextParameter> listParams = context.getContextParameterList();
            List<IContextParameter> contextParamList = mapParams.get(context.getName());
            if (contextParamList != null) {
                listParams.addAll(contextParamList);
            }
        }
        contextManager.fireContextsChangedEvent();
        refreshContextView();
    }
}
