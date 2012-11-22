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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
 * $Id: ContextRemoveParameterCommand.java 80136 2012-03-20 09:19:35Z mwang $
 * 
 */
public class ContextRemoveParameterCommand extends Command {

    private IContextManager contextManager;

    private String paraSourceId;

    private Set<String> contextParamNames = new HashSet<String>();

    private Map<String, List<IContextParameter>> mapParams = new HashMap<String, List<IContextParameter>>();

    public ContextRemoveParameterCommand(IContextManager contextManager, Set<String> contextParamNames) {
        init(contextManager, contextParamNames);
    }

    /**
     * Added by Marvin Wang on Mar.5, 2012 for bug TDI 8574, should use contextParaName and paraSourceId to identify
     * which ContextParameter can be removed.
     * 
     * @param contextManager
     * @param contextParamNames
     * @param paraSourceId
     */
    public ContextRemoveParameterCommand(IContextManager contextManager, Set<String> contextParamNames, String paraSourceId) {
        init(contextManager, contextParamNames);
        this.paraSourceId = paraSourceId;
    }

    /**
     * Added by Marvin Wang on Mar.5, 2012 for bug TDI 8574, should use contextParaName and paraSourceId to identify
     * which ContextParameter can be removed.
     * 
     * @param contextManager
     * @param contextParamName
     * @param paraSourceId
     */
    public ContextRemoveParameterCommand(IContextManager contextManager, String contextParamName, String paraSourceId) {
        this(contextManager, contextParamName);
        this.paraSourceId = paraSourceId;
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

    /**
     * This method is used to remove the <code>JobContextParameter</code> in <code>JobContext</code>, using the
     * combination of <code>sourceId</code> and <code>name</code> can identify the unique
     * <code>JobContextParameter</code>.
     * 
     * @param sourceId
     * @param name
     */
    private void removeParameterFromContext(String sourceId, String name) {
        List<IContext> list = contextManager.getListContext();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                IContext context = list.get(i);
                List<IContextParameter> contextParameters = context.getContextParameterList();
                List<IContextParameter> movedList = new ArrayList<IContextParameter>();
                if (contextParameters != null && contextParameters.size() > 0) {
                    for (int j = 0; j < contextParameters.size(); j++) {
                        IContextParameter contextPara = contextParameters.get(j);
                        String tempSourceId = contextPara.getSource();
                        String tempParaName = contextPara.getName();
                        if (tempSourceId.equals(sourceId) && tempParaName.equals(name)) {
                            movedList.add(contextPara);
                            contextParameters.remove(j);
                            mapParams.put(context.getName(), movedList);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Changed by Marvin Wang on Mar.6, 2012 for bug TDI-8574, for each JobContext, only has one JobContextParameter
     * with the unique combination of source and name. The original algorithm will cause some JobContextParamters can
     * not be removed.
     */
    @Override
    public void execute() {
        mapParams.clear();
        if (contextParamNames != null && contextParamNames.size() > 0) {
            Iterator<String> it = contextParamNames.iterator();
            while (it.hasNext()) {
                String contextParaName = it.next();
                removeParameterFromContext(paraSourceId, contextParaName);
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
