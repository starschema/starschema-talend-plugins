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
package org.talend.repository.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.advanced.composite.FilteredCheckboxTree;
import org.talend.core.CorePlugin;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.views.CheckboxRepositoryTreeViewer;
import org.talend.repository.ui.views.IRepositoryView;
import org.talend.repository.ui.views.RepositoryView;

/**
 * DOC yhch class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
public class UseDynamicJobSelectionDialog extends Dialog {

    private FilteredCheckboxTree filteredCheckboxTree;

    private CheckboxRepositoryView exportItemsTreeViewer;

    private IRepositoryView repositoryView = RepositoryView.show();

    List<RepositoryNode> repositoryNodes = new ArrayList<RepositoryNode>();

    ERepositoryObjectType type;

    String repositoryType;

    ITypeProcessor typeProcessor;

    public UseDynamicJobSelectionDialog(IShellProvider parentShell) {
        super(parentShell);
    }

    public UseDynamicJobSelectionDialog(Shell parentShell, ERepositoryObjectType type, String repositoryType,
            boolean isSelectUseDynamic) {
        super(parentShell);
        setShellStyle(SWT.SHELL_TRIM | SWT.APPLICATION_MODAL | getDefaultOrientation());
        this.type = type;
        /*
         * avoid select self repository node for Process Type.
         * 
         * borrow the repositoryType to set the current process id here.
         */
        this.repositoryType = repositoryType;
    }

    /**
     * Configures the shell
     * 
     * @param shell the shell
     */
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.getString("UseDynamicJobSelectionDialog.selectJobShellText"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(container);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(500, 400).applyTo(container);

        Label label = new Label(container, SWT.NONE);
        label.setText(Messages.getString("UseDynamicJobSelectionDialog.selectJob")); //$NON-NLS-1$
        GridDataFactory.swtDefaults().span(2, 1).applyTo(label);

        createTreeViewer(container);
        createSelectionButton(container);
        exportItemsTreeViewer.refresh();
        // force loading all nodes
        TreeViewer viewer = exportItemsTreeViewer.getViewer();
        viewer.expandAll();
        viewer.collapseAll();
        // expand to level of metadata connection
        viewer.expandToLevel(4);
        setCheckedNodes();
        return container;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {

        RepositoryNode[] repositoryObjects = getCheckNodes();
        repositoryNodes.clear();
        for (RepositoryNode repositoryObject : repositoryObjects) {
            repositoryNodes.add(repositoryObject);
        }
        filteredCheckboxTree.dispose();
        exportItemsTreeViewer.dispose();
        super.okPressed();
    }

    public List<RepositoryNode> getRepositoryNodes() {
        return this.repositoryNodes;
    }

    public void setRepositoryNodes(List<RepositoryNode> repositoryNodes) {
        this.repositoryNodes = repositoryNodes;
    }

    public RepositoryNode[] getCheckNodes() {
        CheckboxTreeViewer viewer = (CheckboxTreeViewer) exportItemsTreeViewer.getViewer();
        List<RepositoryNode> ret = new ArrayList<RepositoryNode>();
        for (int i = 0; i < viewer.getCheckedElements().length; i++) {
            RepositoryNode node = (RepositoryNode) viewer.getCheckedElements()[i];
            if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
                ret.add(node);
            }
        }
        return (RepositoryNode[]) ret.toArray(new RepositoryNode[0]);
    }

    public void setCheckedNodes() {
        CheckboxTreeViewer viewer = (CheckboxTreeViewer) exportItemsTreeViewer.getViewer();
        if (repositoryNodes != null || repositoryNodes.size() != 0) {
            viewer.setCheckedElements(repositoryNodes.toArray());
        }
        viewer.collapseAll();
    }

    private void createTreeViewer(Composite itemComposite) {
        filteredCheckboxTree = new FilteredCheckboxTree(itemComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI) {

            @Override
            protected CheckboxTreeViewer doCreateTreeViewer(Composite parent, int style) {
                exportItemsTreeViewer = new CheckboxRepositoryView();
                try {
                    exportItemsTreeViewer.init(repositoryView.getViewSite());
                } catch (PartInitException e) {
                    ExceptionHandler.process(e);
                }
                exportItemsTreeViewer.createPartControl(parent);

                return (CheckboxTreeViewer) exportItemsTreeViewer.getViewer();
            }

            @Override
            protected void refreshCompleted() {
                getViewer().expandToLevel(3);
                restoreCheckedElements();
            }

            @Override
            protected boolean isNodeCollectable(TreeItem item) {
                Object obj = item.getData();
                if (obj instanceof RepositoryNode) {
                    RepositoryNode node = (RepositoryNode) obj;
                    if (node.getObjectType() == ERepositoryObjectType.METADATA_CONNECTIONS) {
                        return true;
                    }
                }
                return false;
            }
        };
        exportItemsTreeViewer.getViewer().addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                RepositoryNode node = (RepositoryNode) element;
                return filterRepositoryNode(node);
            }
        });
    }

    public void addCheckStateListener(ICheckStateListener listener) {
        ((CheckboxTreeViewer) exportItemsTreeViewer.getViewer()).addCheckStateListener(listener);
    }

    public void removeCheckStateListener(ICheckStateListener listener) {
        ((CheckboxTreeViewer) exportItemsTreeViewer.getViewer()).removeCheckStateListener(listener);
    }

    private boolean filterRepositoryNode(RepositoryNode node) {
        if (node == null) {
            return false;
        }
        if (node.isBin()) {
            return false;
        }

        ERepositoryObjectType contentType = node.getContentType();
        if (contentType != null) {
            if (contentType == ERepositoryObjectType.PROCESS) {
                String id = node.getId();
                if (repositoryType != null && !repositoryType.equals("") && id != null && !id.equals("")) {
                    if (id.equals(repositoryType)) {
                        return false;
                    }
                }
                return true;
            } else if (contentType == ERepositoryObjectType.SVN_ROOT) {
                return true;
            } else if (contentType == ERepositoryObjectType.REFERENCED_PROJECTS) {
                return true;
            } else {
                return false;
            }
        } else {
            if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
                return true;
            }
        }

        return false;
    }

    /**
     * DOC hcw Comment method "createSelectionButton".
     * 
     * @param itemComposite
     */
    private void createSelectionButton(Composite itemComposite) {
        Composite buttonComposite = new Composite(itemComposite, SWT.NONE);
        GridLayoutFactory.swtDefaults().margins(0, 25).applyTo(buttonComposite);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(buttonComposite);

        Button selectAll = new Button(buttonComposite, SWT.PUSH);
        selectAll.setText(DataTransferMessages.DataTransfer_selectAll);
        selectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ((CheckboxTreeViewer) exportItemsTreeViewer.getViewer()).setAllChecked(true);
            }
        });

        setButtonLayoutData(selectAll);

        Button deselectAll = new Button(buttonComposite, SWT.PUSH);
        deselectAll.setText(DataTransferMessages.DataTransfer_deselectAll);
        deselectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ((CheckboxTreeViewer) exportItemsTreeViewer.getViewer()).setAllChecked(false);
            }
        });

        setButtonLayoutData(deselectAll);

        Button expandBtn = new Button(buttonComposite, SWT.PUSH);
        expandBtn.setText(Messages.getString("UseDynamicJobSelectionDialog.expandBtnText")); //$NON-NLS-1$
        expandBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                exportItemsTreeViewer.getViewer().expandAll();
            }
        });
        setButtonLayoutData(expandBtn);

        Button collapseBtn = new Button(buttonComposite, SWT.PUSH);
        collapseBtn.setText(Messages.getString("UseDynamicJobSelectionDialog.collapseBtnText")); //$NON-NLS-1$
        collapseBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                exportItemsTreeViewer.getViewer().collapseAll();
            }
        });
        setButtonLayoutData(collapseBtn);
    }

    /**
     * Repository view with checkbox
     * 
     * DOC yhch UseDynamicJobSelectionDialog class global comment. Detailled comment <br/>
     * 
     * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
     * 
     */
    class CheckboxRepositoryView extends RepositoryView {

        @Override
        protected TreeViewer createTreeViewer(Composite parent) {
            return new CheckboxRepositoryTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.talend.repository.ui.views.RepositoryView#createPartControl(org.eclipse.swt.widgets.Composite)
         */
        @Override
        public void createPartControl(Composite parent) {
            super.createPartControl(parent);
            CorePlugin.getDefault().getRepositoryService().removeRepositoryChangedListener(this);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.talend.repository.ui.views.RepositoryView#refresh(java.lang.Object)
         */
        @Override
        public void refresh(Object object) {
            refresh();
            if (object != null) {
                getViewer().expandToLevel(object, AbstractTreeViewer.ALL_LEVELS);
            }
        }

        @Override
        protected void makeActions() {
        }

        @Override
        protected void hookContextMenu() {
        }

        @Override
        protected void contributeToActionBars() {
        }

        @Override
        protected void initDragAndDrop() {
        }

        @Override
        protected void hookDoubleClickAction() {
        }

        @Override
        public void addFilters() {
        }

        @Override
        public void createActionComposite(Composite parent) {
        }

    }

    /**
     * DOC nrousseau Comment method "dispose".
     */
    public void dispose() {
        ((CheckboxTreeViewer) exportItemsTreeViewer.getViewer()).setCheckedElements(ArrayUtils.EMPTY_OBJECT_ARRAY);
        exportItemsTreeViewer.dispose();
        repositoryView = null;
        exportItemsTreeViewer = null;
        repositoryNodes.clear();
        repositoryNodes = null;
        filteredCheckboxTree = null;
    }
}
