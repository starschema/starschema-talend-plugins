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
package org.talend.designer.core.ui.views.contexts;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.ViewPart;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ContextParameterTypeImpl;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ContextTypeImpl;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.editor.JobEditorInput;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.utils.ConnectionContextHelper;

/**
 * qzhang class global comment. Detailled comment <br/>
 * 
 */
public class ContextsView extends ViewPart {

    public static final String ID = "org.talend.designer.core.ui.views.ContextsView"; //$NON-NLS-1$

    private AbstractMultiPageTalendEditor part;

    public ContextsView() {
    }

    private ContextViewComposite contextComposite;

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout());
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.verticalAlignment = SWT.FILL;
        gridData.horizontalAlignment = SWT.FILL;
        parent.setLayoutData(gridData);
        getPart();

        contextComposite = new ContextViewComposite(parent, this);
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.verticalAlignment = SWT.FILL;
        gd.horizontalAlignment = SWT.FILL;
        contextComposite.setLayoutData(gd);

        initialDrop();

        refresh();

    }

    public void initialDrop() {
        int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT;
        DropTarget target = new DropTarget(contextComposite, operations);
        final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
        target.setTransfer(new Transfer[] { transfer });
        target.addDropListener(new DropTargetListener() {

            public void dropAccept(DropTargetEvent event) {
            }

            public void drop(DropTargetEvent event) {
                if (transfer.isSupportedType(event.currentDataType)) {
                    ISelection selection = transfer.getSelection();
                    if (selection instanceof TreeSelection) {
                        Object[] objects = ((TreeSelection) selection).toArray();
                        List<Object> asList = Arrays.asList(objects);
                        boolean created = false;
                        for (Object obj : objects) {
                            if (obj instanceof RepositoryNode) {
                                RepositoryNode sourceNode = (RepositoryNode) obj;
                                Item item = sourceNode.getObject().getProperty().getItem();
                                if (item instanceof ContextItem) {
                                    ContextItem contextItem = (ContextItem) item;
                                    EList context = contextItem.getContext();
                                    Set<String> contextSet = new HashSet<String>();
                                    Iterator iterator = context.iterator();
                                    while (iterator.hasNext()) {
                                        Object repositoryObject = iterator.next();
                                        if (repositoryObject instanceof ContextTypeImpl) {
                                            EList contextParameters = ((ContextTypeImpl) repositoryObject).getContextParameter();
                                            Iterator contextParas = contextParameters.iterator();
                                            while (contextParas.hasNext()) {
                                                ContextParameterTypeImpl contextParameterType = (ContextParameterTypeImpl) contextParas
                                                        .next();
                                                String name = contextParameterType.getName();
                                                contextSet.add(name);
                                            }
                                        }
                                    }
                                    IEditorInput editorInput = part.getEditorInput();
                                    if (editorInput instanceof JobEditorInput) {
                                        JobEditorInput jobInput = (JobEditorInput) editorInput;
                                        IProcess2 process = jobInput.getLoadedProcess();
                                        IContextManager contextManager = process.getContextManager();
                                        List<IContext> listContext = contextManager.getListContext();
                                        // context group will reflect absolutely if no context variable in contextViewer
                                        if (!ConnectionContextHelper.containsVariable(contextManager)) {
                                            // for bug 15608
                                            ConnectionContextHelper.addContextVarForJob(process, contextItem, contextManager);
                                            // ConnectionContextHelper.checkAndAddContextsVarDND(contextItem,
                                            // contextManager);
                                            created = true;
                                        } else {
                                            Set<String> addedContext = ConnectionContextHelper.checkAndAddContextVariables(
                                                    contextItem, contextSet, contextManager, false);
                                            if (addedContext != null && addedContext.size() > 0) {
                                                ConnectionContextHelper.addContextVarForJob(process, contextItem, contextSet);
                                                created = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (created) {
                            RepositoryPlugin.getDefault().getDesignerCoreService().switchToCurContextsView();
                        }
                    }
                }
            }

            public void dragOver(DropTargetEvent event) {
                if (transfer.isSupportedType(event.currentDataType)) {
                    IEditorInput editorInput = part.getEditorInput();
                    if (!(editorInput instanceof JobEditorInput)) {
                        event.detail = DND.DROP_NONE;
                    } else {
                        ISelection selection = transfer.getSelection();
                        if (selection instanceof TreeSelection) {
                            Object[] objects = ((TreeSelection) selection).toArray();
                            for (Object obj : objects) {
                                if (obj instanceof RepositoryNode) {
                                    RepositoryNode sourceNode = (RepositoryNode) obj;
                                    Item item = sourceNode.getObject().getProperty().getItem();
                                    if (!(item instanceof ContextItem)) {
                                        event.detail = DND.DROP_NONE;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            public void dragOperationChanged(DropTargetEvent event) {
            }

            public void dragLeave(DropTargetEvent event) {
            }

            public void dragEnter(DropTargetEvent event) {
                if (event.detail == DND.DROP_DEFAULT) {
                    if ((event.operations & DND.DROP_COPY) != 0) {
                        event.detail = DND.DROP_COPY;
                    } else {
                        event.detail = DND.DROP_NONE;
                    }
                }
                for (int i = 0; i < event.dataTypes.length; i++) {
                    if (transfer.isSupportedType(event.dataTypes[i])) {
                        event.currentDataType = event.dataTypes[i];
                        if (event.detail != DND.DROP_COPY) {
                            event.detail = DND.DROP_NONE;
                        }
                        break;
                    }
                }

            }
        });
    }

    public boolean updateContextFromRepository() {
        // String repositoryId = process.getRepositoryId();
        // IProxyRepositoryFactory factory = DesignerPlugin.getDefault().getProxyRepositoryFactory();
        // List<ContextItem> contextItemList = null;
        // try {
        // contextItemList = factory.getContextItem();
        // } catch (PersistenceException e) {
        // throw new RuntimeException(e);
        // }
        // if (contextItemList != null) {
        // for (ContextItem item : contextItemList) {
        // if (factory.getStatus(item) != ERepositoryStatus.DELETED) {
        // String id = item.getProperty().getId();
        // if (id.equals(repositoryId)) {
        // IContextManager tmpManager = new JobContextManager(item.getContext(), item.getDefaultContext());
        // if (tmpManager.sameAs(getContextManager())) {
        // return false;
        // }
        // getContextManager().setListContext(tmpManager.getListContext());
        // getContextManager().setDefaultContext(tmpManager.getDefaultContext());
        // return true;
        // }
        // }
        // }
        // }
        return false;
    }

    /**
     * bqian Comment method "getContextManager".
     * 
     * @return
     */
    private IContextManager getContextManager() {
        return part.getProcess().getContextManager();
    }

    public void updateContextView() {
        updateContextView(true, false, true);
    }

    public void updateContextView(boolean isBuildIn) {
        updateContextView(isBuildIn, false, true);
    }

    public void updateAllContextView(boolean isBuildIn) {
        updateAllContextView(isBuildIn, false, true);
    }

    public void updateAllContextView(boolean isBuildIn, boolean isDisposeAll, boolean refreshView) {
        getPart();
        if (part != null) {
            boolean modified = updateContextFromRepository();
            if (modified) {
                part.getTalendEditor().setDirty(true);
            }
        }
        contextComposite.setReadOnly(!isBuildIn);

        if (refreshView) {
            contextComposite.refreshTemplateTab();
            contextComposite.refreshTableTab();
            contextComposite.refreshTreeTab();
        }

    }

    public void updateContextView(boolean isBuildIn, boolean isDisposeAll, boolean refreshView) {
        getPart();
        if (part != null) {
            boolean modified = updateContextFromRepository();
            if (modified) {
                part.getTalendEditor().setDirty(true);
            }
        }
        contextComposite.setReadOnly(!isBuildIn);

        if (refreshView) {
            contextComposite.refreshTemplateTab();
        }

    }

    public void updateContextView(boolean isBuildIn, boolean isDisposeAll) {
        updateContextView(isBuildIn, isDisposeAll, true);
    }

    public void refresh() {
        getPart();
        if (part != null) {
            boolean modified = updateContextFromRepository();
            if (modified) {
                part.getTalendEditor().setDirty(true);
            }
            IEditorInput editorInput = part.getEditorInput();
            if (editorInput != null && editorInput instanceof JobEditorInput) {
                JobEditorInput jobInput = (JobEditorInput) editorInput;
                IProcess2 process = jobInput.getLoadedProcess();
                if (process != null) {
                    contextComposite.setReadOnly(process.isReadOnly());
                }
            }
        }
        contextComposite.setPart(part);
    }

    @Override
    public void setFocus() {
        contextComposite.setFocus();
    }

    public void setPartName(String title) {
        String viewName = "Contexts"; //$NON-NLS-1$

        if (!title.equals("")) { //$NON-NLS-1$
            viewName = viewName + "(" + title + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            super.setTitleToolTip(title);
        }
        super.setPartName(viewName);
    }

    private void getPart() {
        final IEditorPart activeEditor = getSite().getPage().getActiveEditor();
        if (activeEditor instanceof AbstractMultiPageTalendEditor) {
            part = (AbstractMultiPageTalendEditor) activeEditor;
        } else {
            part = null;
        }
    }

    /**
     * DOC nrousseau Comment method "reset".
     */
    public void reset() {
        part = null;
        contextComposite.setPart(null);
    }

    public ContextViewComposite getContextViewComposite() {
        return this.contextComposite;
    }
}
