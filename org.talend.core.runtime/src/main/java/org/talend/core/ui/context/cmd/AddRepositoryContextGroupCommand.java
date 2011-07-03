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
package org.talend.core.ui.context.cmd;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.gef.commands.Command;
import org.talend.core.model.context.JobContext;
import org.talend.core.model.context.JobContextParameter;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.ui.context.IContextModelManager;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class AddRepositoryContextGroupCommand extends Command {

    private IProgressMonitor monitor;

    private IContextManager manager;

    private IContextModelManager modelManager;

    private final Set<String> nameSet;

    private List<ContextItem> selectedItems;

    public AddRepositoryContextGroupCommand(IProgressMonitor monitor, IContextModelManager modelManager,
            final List<ContextItem> selectedItems, final Set<String> nameSet) {
        super();
        this.monitor = monitor;
        this.modelManager = modelManager;
        if (modelManager != null) {
            this.manager = modelManager.getContextManager();
        }
        this.selectedItems = selectedItems;
        this.nameSet = nameSet;
    }

    @Override
    public void execute() {
        if (modelManager == null || manager == null || selectedItems == null || selectedItems.isEmpty()
                || nameSet == null) {
            return;
        }
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        IContext defaultContext = manager.getDefaultContext();

        // specially process the context group(bug 4025)
        boolean special = false;
        ContextItem item = null;
        if (defaultContext.getContextParameterList().isEmpty() && selectedItems.size() == 1) {
            special = true;
            item = selectedItems.get(0);
            List<String> contextNames = new ArrayList<String>();
            for (ContextType context : (List<ContextType>) item.getContext()) {
                contextNames.add(context.getName());
            }
            Iterator<IContext> iterator = manager.getListContext().iterator();

            while (iterator.hasNext()) {
                String name = iterator.next().getName();
                // if not select new default context, will not delete the default context for job.
                if (contextNames.contains(name)
                        || (!nameSet.contains(item.getDefaultContext()) && name.equals(defaultContext.getName()))) {
                    continue;
                }
                iterator.remove();
            }
            monitor.worked(1);
        }
        //
        IContext newDefaultContext = null;
        for (String name : nameSet) {
            JobContext newContext = new JobContext(name);

            List<IContextParameter> newParamList = new ArrayList<IContextParameter>();
            newContext.setContextParameterList(newParamList);
            JobContextParameter param;
            for (IContextParameter parameter : defaultContext.getContextParameterList()) {
                param = (JobContextParameter) parameter.clone();
                param.setContext(newContext);
                newParamList.add(param);
            }
            manager.getListContext().add(newContext);
            if (special && item != null) {
                if (name.equals(item.getDefaultContext())) {
                    newDefaultContext = newContext;
                }
            }
            monitor.worked(1);
        }
        if (special) {
            if (newDefaultContext != null) { // have selected new default context
                modelManager.onContextChangeDefault(manager, newDefaultContext);
            } else { // nameSet is empty
                IContext context = manager.getContext(item.getDefaultContext());
                if (context != null) {
                    modelManager.onContextChangeDefault(manager, context);
                }
            }
            monitor.worked(1);
        }
    }

}
