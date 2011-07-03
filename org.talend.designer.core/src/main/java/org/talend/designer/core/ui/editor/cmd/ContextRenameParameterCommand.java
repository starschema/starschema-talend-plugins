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
 * $Id: ContextRenameParameterCommand.java 54939 2011-02-11 01:34:57Z mhirt $
 * 
 */
public class ContextRenameParameterCommand extends Command {

    String oldName, newName;

    IContextManager contextManager;

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
                if (listParams.get(j).getName().equals(oldName)) {
                    listParams.get(j).setName(newName);
                    // see 0003889: Context script code not refreshed.
                    String scriptCode = listParams.get(j).getScriptCode().replaceAll(oldName, newName);
                    listParams.get(j).setScriptCode(scriptCode);
                    // if the user haven't modified prompt, change it
                    if (listParams.get(j).getPrompt().equals(oldName + "?")) { //$NON-NLS-1$
                        listParams.get(j).setPrompt(newName + "?"); //$NON-NLS-1$
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
                if (listParams.get(j).getName().equals(newName)) {
                    listParams.get(j).setName(oldName);
                    // see 0003889: Context script code not refreshed.
                    String scriptCode = listParams.get(j).getScriptCode().replaceAll(newName, oldName);
                    listParams.get(j).setScriptCode(scriptCode);
                    // if the user haven't modified prompt, change it
                    if (listParams.get(j).getPrompt().equals(newName + "?")) { //$NON-NLS-1$
                        listParams.get(j).setPrompt(oldName + "?"); //$NON-NLS-1$
                    }
                    found = true;
                }
            }
        }
        refreshContextView();
    }
}
