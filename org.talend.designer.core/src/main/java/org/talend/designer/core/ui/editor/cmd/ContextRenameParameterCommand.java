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

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.views.contexts.ContextsView;

/**
 * Command that will rename the parameter in all contexts. <br/>
 * 
 * $Id: ContextRenameParameterCommand.java 80103 2012-03-20 07:30:16Z mwang $
 * 
 */
public class ContextRenameParameterCommand extends Command {

    String oldName, newName;

    IContextManager contextManager;

    private String sourceId;

    public ContextRenameParameterCommand(IContextManager contextManager, String sourceId, String oldName, String newName) {
        this(contextManager, oldName, newName);
        this.sourceId = sourceId;
    }

    public ContextRenameParameterCommand(IContextManager contextManager, String oldName, String newName) {
        this.contextManager = contextManager;
        this.oldName = oldName;
        this.newName = newName;
        setLabel(Messages.getString("ContextRenameParameterCommand.renameParameter")); //$NON-NLS-1$
    }

    /**
     * qzhang Comment method "refreshContextView".
     */
    private void refreshContextView() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IViewPart view2 = page.findView("org.talend.designer.core.ui.views.ContextsView"); //$NON-NLS-1$
        if (view2 instanceof ContextsView) {
            ((ContextsView) view2).updateContextView(true, false, false);
        }
    }

    @Override
    public void execute() {
        boolean found;
        List<IContextParameter> listParams;

        for (int i = 0; i < contextManager.getListContext().size(); i++) {
            listParams = contextManager.getListContext().get(i).getContextParameterList();
            found = false;
            for (int j = 0; j < listParams.size() && !found; j++) {
                IContextParameter contextParameter = listParams.get(j);
                String tempName = contextParameter.getName();
                String tempSourceId = contextParameter.getSource();
                if (tempName.equals(oldName) && tempSourceId.equals(sourceId)) {
                    contextParameter.setName(newName);
                    // see 0003889: Context script code not refreshed.
                    String scriptCode = contextParameter.getScriptCode().replaceAll(oldName, newName);
                    contextParameter.setScriptCode(scriptCode);
                    // if the user haven't modified prompt, change it
                    if (contextParameter.getPrompt().equals(oldName + "?")) { //$NON-NLS-1$
                        contextParameter.setPrompt(newName + "?"); //$NON-NLS-1$
                    }

                    found = true;
                }
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
        boolean found;
        List<IContextParameter> listParams;

        for (int i = 0; i < contextManager.getListContext().size(); i++) {
            listParams = contextManager.getListContext().get(i).getContextParameterList();
            found = false;
            for (int j = 0; j < listParams.size() && !found; j++) {
                IContextParameter contextParameter = listParams.get(j);
                String name = contextParameter.getName();
                String tempSourceId = contextParameter.getSource();
                if (name.equals(newName) && tempSourceId.equals(sourceId)) {
                    contextParameter.setName(oldName);
                    // see 0003889: Context script code not refreshed.
                    String scriptCode = contextParameter.getScriptCode().replaceAll(newName, oldName);
                    contextParameter.setScriptCode(scriptCode);
                    // if the user haven't modified prompt, change it
                    if (contextParameter.getPrompt().equals(newName + "?")) { //$NON-NLS-1$
                        contextParameter.setPrompt(oldName + "?"); //$NON-NLS-1$
                    }
                    found = true;
                }
            }
        }
        refreshContextView();
    }
}
