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
package org.talend.repository.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
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
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.talend.commons.ui.swt.advanced.composite.FilteredCheckboxTree;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.viewer.ui.provider.RepositoryViewerProvider;

/**
 * DOC yhch class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
public class UseDynamicJobSelectionDialog extends Dialog {

    private FilteredCheckboxTree filteredCheckboxTree;

    List<RepositoryNode> repositoryNodes = new ArrayList<RepositoryNode>();

    // ERepositoryObjectType type;

    String repositoryType;

    public UseDynamicJobSelectionDialog(IShellProvider parentShell) {
        super(parentShell);
    }

    public UseDynamicJobSelectionDialog(Shell parentShell, ERepositoryObjectType type, String repositoryType,
            boolean isSelectUseDynamic) {
        super(parentShell);
        setShellStyle(SWT.SHELL_TRIM | SWT.APPLICATION_MODAL | getDefaultOrientation());
        // this.type = type;
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

    private CheckboxTreeViewer getItemsTreeViewer() {
        return filteredCheckboxTree.getViewer();
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

        CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
        exportItemsTreeViewer.refresh();
        // force loading all nodes
        exportItemsTreeViewer.expandAll();
        exportItemsTreeViewer.collapseAll();
        // expand to level of metadata connection
        exportItemsTreeViewer.expandToLevel(4);
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
        super.okPressed();
    }

    public List<RepositoryNode> getRepositoryNodes() {
        return this.repositoryNodes;
    }

    public void setRepositoryNodes(List<RepositoryNode> repositoryNodes) {
        this.repositoryNodes = repositoryNodes;
    }

    public RepositoryNode[] getCheckNodes() {
        CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
        List<RepositoryNode> ret = new ArrayList<RepositoryNode>();
        for (int i = 0; i < exportItemsTreeViewer.getCheckedElements().length; i++) {
            RepositoryNode node = (RepositoryNode) exportItemsTreeViewer.getCheckedElements()[i];
            if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
                ret.add(node);
            }
        }
        return (RepositoryNode[]) ret.toArray(new RepositoryNode[0]);
    }

    public void setCheckedNodes() {
        CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
        if (repositoryNodes != null || repositoryNodes.size() != 0) {
            exportItemsTreeViewer.setCheckedElements(repositoryNodes.toArray());
        }
        exportItemsTreeViewer.collapseAll();
    }

    private void createTreeViewer(Composite itemComposite) {
        filteredCheckboxTree = new FilteredCheckboxTree(itemComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI) {

            @Override
            protected CheckboxTreeViewer doCreateTreeViewer(Composite parent, int style) {
                RepositoryViewerProvider provider = new RepositoryViewerProvider() {

                    @Override
                    protected ERepositoryObjectType getCheckingType() {
                        return ERepositoryObjectType.PROCESS;
                    }

                };
                return (CheckboxTreeViewer) provider.createViewer(parent);
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
        CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
        exportItemsTreeViewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                RepositoryNode node = (RepositoryNode) element;
                return filterRepositoryNode(node);
            }
        });
    }

    public void addCheckStateListener(ICheckStateListener listener) {
        CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
        exportItemsTreeViewer.addCheckStateListener(listener);
    }

    public void removeCheckStateListener(ICheckStateListener listener) {
        CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
        exportItemsTreeViewer.removeCheckStateListener(listener);
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
                CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
                exportItemsTreeViewer.setAllChecked(true);
            }
        });

        setButtonLayoutData(selectAll);

        Button deselectAll = new Button(buttonComposite, SWT.PUSH);
        deselectAll.setText(DataTransferMessages.DataTransfer_deselectAll);
        deselectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
                exportItemsTreeViewer.setAllChecked(false);
            }
        });

        setButtonLayoutData(deselectAll);

        Button expandBtn = new Button(buttonComposite, SWT.PUSH);
        expandBtn.setText(Messages.getString("UseDynamicJobSelectionDialog.expandBtnText")); //$NON-NLS-1$
        expandBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
                exportItemsTreeViewer.expandAll();
            }
        });
        setButtonLayoutData(expandBtn);

        Button collapseBtn = new Button(buttonComposite, SWT.PUSH);
        collapseBtn.setText(Messages.getString("UseDynamicJobSelectionDialog.collapseBtnText")); //$NON-NLS-1$
        collapseBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
                exportItemsTreeViewer.collapseAll();
            }
        });
        setButtonLayoutData(collapseBtn);
    }

    /**
     * DOC nrousseau Comment method "dispose".
     */
    public void dispose() {
        CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
        exportItemsTreeViewer.setCheckedElements(ArrayUtils.EMPTY_OBJECT_ARRAY);
        exportItemsTreeViewer = null;
        repositoryNodes.clear();
        repositoryNodes = null;
        filteredCheckboxTree = null;
    }
}
