// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC hcw class global comment. Detailled comment
 */
public class CheckboxRepositoryTreeViewer extends ContainerCheckedTreeViewer implements ITreeViewerListener {

    private Map<String, Boolean> expanded = new HashMap<String, Boolean>();

    private TreeItem lastClickedItem = null;

    public CheckboxRepositoryTreeViewer(Composite parent, int style) {
        super(parent, style);
    }

    private RepositoryNode getRepositoryNode(Item node) {
        Object data = node.getData();
        RepositoryNode repositoryNode = null;
        if (data instanceof RepositoryNode) {
            repositoryNode = (RepositoryNode) data;
        }
        return repositoryNode;
    }

    @Override
    protected boolean getExpanded(Item item) {
        RepositoryNode repositoryNode = getRepositoryNode(item);
        if (repositoryNode != null && repositoryNode.getId() != null) {
            Boolean result = expanded.get(repositoryNode.getId());
            if (result != null) {
                if (item instanceof TreeItem) {
                    TreeItem treeItem = (TreeItem) item;
                    treeItem.setExpanded(result);
                }
            }
        }
        return super.getExpanded(item);
    }

    @Override
    public void setExpandedState(Object elementOrTreePath, boolean expanded) {
        if (expanded) {
            internalExpand(elementOrTreePath);
        } else {
            internalCollapse(elementOrTreePath);
        }
        super.setExpandedState(elementOrTreePath, expanded);
    }

    public void treeCollapsed(TreeExpansionEvent event) {
        Object element = event.getElement();
        internalCollapse(element);
    }

    public void treeExpanded(TreeExpansionEvent event) {
        Object element = event.getElement();
        internalExpand(element);
    }

    private void internalCollapse(Object element) {
        if (element instanceof RepositoryNode) {
            RepositoryNode repositoryNode = (RepositoryNode) element;
            if (idIsValid(repositoryNode)) {
                expanded.put(repositoryNode.getId(), false);
            }
            emptyExpandedChildren(repositoryNode);
        }
    }

    private void internalExpand(Object element) {
        if (element instanceof RepositoryNode) {
            RepositoryNode repositoryNode = (RepositoryNode) element;
            if (idIsValid(repositoryNode)) {
                expanded.put(repositoryNode.getId(), true);
            }
        }
    }

    private void emptyExpandedChildren(RepositoryNode repositoryNode) {
        for (IRepositoryNode children : repositoryNode.getChildren()) {
            if (idIsValid(children)) {
                expanded.remove(children.getId());
            }
            emptyExpandedChildren((RepositoryNode) children);
        }
    }

    private boolean idIsValid(IRepositoryNode repositoryNode) {
        String id = repositoryNode.getId();
        return id != null && !RepositoryNode.NO_ID.equals(id);
    }

    @Override
    protected void handleSelect(SelectionEvent event) {
        lastClickedItem = null;
        if (event.detail == SWT.CHECK) {
            TreeItem item = (TreeItem) event.item;
            lastClickedItem = item;
        }
        super.handleSelect(event);
    }

    public TreeItem getLastClickedItem() {
        return this.lastClickedItem;
    }

}
