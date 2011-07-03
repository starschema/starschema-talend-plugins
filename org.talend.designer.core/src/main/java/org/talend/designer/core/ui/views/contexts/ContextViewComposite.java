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
package org.talend.designer.core.ui.views.contexts;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.BidiMap;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.context.UpdateContextVariablesHelper;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.ui.context.ContextComposite;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.ContextAddParameterCommand;
import org.talend.designer.core.ui.editor.cmd.ContextChangeDefaultCommand;
import org.talend.designer.core.ui.editor.cmd.ContextRemoveParameterCommand;
import org.talend.designer.core.ui.editor.cmd.ContextRenameParameterCommand;
import org.talend.designer.core.ui.editor.cmd.ContextTemplateModifyCommand;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.views.jobsettings.JobSettings;
import org.talend.designer.core.ui.views.properties.ComponentSettings;

/**
 * A concrete class of ContextComposite for the context view. <br/>
 * 
 */
public class ContextViewComposite extends ContextComposite {

    AbstractMultiPageTalendEditor part;

    // private CCombo typeCombo;
    //
    // private CCombo repositoryCombo;

    private String currentRepositoryContext = null;

    private Map<String, ContextItem> repositoryContextItemMap = null;

    private BidiMap repositoryContextValueMap = null;

    /**
     * bqian ContextComposite constructor comment.
     * 
     * @param parent
     * @param style
     */
    public ContextViewComposite(Composite parent, ContextsView contextView) {
        super(parent, false);
    }

    public void setPart(AbstractMultiPageTalendEditor part) {
        this.part = part;
        super.refreshTemplateTab();
        super.refreshTableTab();
        super.refreshTreeTab();
        refreshRelationship();
        // for bug 13730
    }

    private void refreshRelationship() {
        IProcess process = getProcess();
        if (process != null) {
            setReadOnly(process.isReadOnly());
        }

        DesignerPlugin.getDefault().getRunProcessService().refreshView();
    }

    public void refresh() {
        refreshRelationship();
        super.refresh();
    }

    @Override
    public void refreshTableTab() {
        refreshRelationship();
        super.refreshTableTab();
    }

    @Override
    public void refreshTemplateTab() {
        refreshRelationship();
        super.refreshTemplateTab();
    }

    @Override
    public void refreshTreeTab() {
        refreshRelationship();
        super.refreshTreeTab();
    }

    public CommandStack getCommandStack() {
        return part == null ? null : (CommandStack) (part.getTalendEditor().getAdapter(CommandStack.class));
    }

    public IContextManager getContextManager() {
        return getProcess() == null ? null : getProcess().getContextManager();
    }

    public IProcess2 getProcess() {
        return part == null ? null : part.getTalendEditor().getProcess();
    }

    private Process getJob() {
        return (Process) getProcess();
    }

    public void onContextChangeDefault(IContextManager contextManager, IContext newDefault) {
        getCommandStack().execute(new ContextChangeDefaultCommand(contextManager, newDefault));
    }

    public void onContextRenameParameter(IContextManager contextManager, String oldName, String newName) {
        if (contextManager instanceof JobContextManager) {
            JobContextManager manager = (JobContextManager) contextManager;
            manager.addNewName(newName, oldName);
            // record the modified operation.
            setModifiedFlag(contextManager);
        }
        getCommandStack().execute(new ContextRenameParameterCommand(contextManager, oldName, newName));
        // update variable reference for current job, for 2608
        if (UpdateContextVariablesHelper.updateProcessForRenamed(getProcess(), oldName, newName)) {
            JobSettings.switchToCurJobSettingsView();
            ComponentSettings.switchToCurComponentSettingsView();
        }
    }

    public void onContextModify(IContextManager contextManager, IContextParameter parameter) {
        // record the modified operation.
        setModifiedFlag(contextManager);
        getCommandStack().execute(new ContextTemplateModifyCommand(getProcess(), contextManager, parameter));
    }

    public void onContextAddParameter(IContextManager contextManager, IContextParameter parameter) {
        getCommandStack().execute(new ContextAddParameterCommand(getContextManager(), parameter));
    }

    public void onContextRemoveParameter(IContextManager contextManager, String paramName) {
        Set<String> names = new HashSet<String>();
        names.add(paramName);
        onContextRemoveParameter(contextManager, names);
    }

    private void setModifiedFlag(IContextManager contextManager) {
        if (contextManager != null && contextManager instanceof JobContextManager) {
            JobContextManager manager = (JobContextManager) contextManager;
            // record the modified operation.
            manager.setModified(true);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.talend.core.ui.context.IContextModelManager#onContextRemoveParameter(org.talend.core.model.process.
     * IContextManager, java.util.List)
     */
    public void onContextRemoveParameter(IContextManager contextManager, Set<String> paramNames) {
        // record the modified operation.
        setModifiedFlag(contextManager);
        getCommandStack().execute(new ContextRemoveParameterCommand(getContextManager(), paramNames));
    }
}
