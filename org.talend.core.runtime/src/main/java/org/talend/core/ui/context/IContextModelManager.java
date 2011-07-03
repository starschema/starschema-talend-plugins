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
package org.talend.core.ui.context;

import java.util.Set;

import org.eclipse.gef.commands.CommandStack;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IProcess2;

/**
 * DOC bqian class global comment. Detailled comment <br/>
 * 
 */
public interface IContextModelManager {

    public IContextManager getContextManager();

    public IProcess2 getProcess();

    /**
     * refresh the contextComposite.
     */
    public void refresh();

    /*
     * bug 6828 by cli
     */
    public void refreshTemplateTab();

    public void refreshTableTab();

    public void refreshTreeTab();

    public CommandStack getCommandStack();

    public void onContextChangeDefault(IContextManager contextManager, IContext newDefault);

    public void onContextRenameParameter(IContextManager contextManager, String oldName, String newName);

    public void onContextModify(IContextManager contextManager, IContextParameter parameter);

    public void onContextAddParameter(IContextManager contextManager, IContextParameter parameter);

    public void onContextRemoveParameter(IContextManager contextManager, String paramName);

    public void onContextRemoveParameter(IContextManager contextManager, Set<String> paramNames);

    public boolean isReadOnly();

    public boolean isRepositoryContext();
}
