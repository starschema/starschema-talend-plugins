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

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.talend.core.repository.i18n.Messages;
import org.talend.repository.model.ItemReferenceBean;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class ItemReferenceDialog extends Dialog {

    private List<ItemReferenceBean> referenceList;

    public ItemReferenceDialog(Shell parentShell, List<ItemReferenceBean> referenceList) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.MIN | SWT.APPLICATION_MODAL);
        this.referenceList = referenceList;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("ItemReferenceDialog.title")); //$NON-NLS-1$
        newShell.setSize(600, 400);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        Label label = new Label(composite, SWT.NONE);
        label.setText(Messages.getString("ItemReferenceDialog.messages")); //$NON-NLS-1$

        TreeViewer viewer = new TreeViewer(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);

        final Tree tree = viewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));

        TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
        column1.setText(Messages.getString("ItemReferenceDialog.item")); //$NON-NLS-1$
        column1.setWidth(150);
        column1.setResizable(true);

        TreeColumn column2 = new TreeColumn(tree, SWT.LEFT);
        column2.setText(Messages.getString("ItemReferenceDialog.referenceItem")); //$NON-NLS-1$
        column2.setWidth(200);
        column2.setResizable(true);

        TreeColumn column3 = new TreeColumn(tree, SWT.LEFT);
        column3.setText(Messages.getString("ItemReferenceDialog.nodeTotals")); //$NON-NLS-1$
        column3.setWidth(50);
        column3.setResizable(true);
        column3.setToolTipText(Messages.getString("ItemReferenceDialog.nodeTotalsTip")); //$NON-NLS-1$

        TreeColumn column4 = new TreeColumn(tree, SWT.LEFT);
        column4.setText(Messages.getString("ItemReferenceDialog.project")); //$NON-NLS-1$
        column4.setWidth(150);
        column4.setResizable(true);

        ItemReferenceViewProvider provider = new ItemReferenceViewProvider();
        viewer.setContentProvider(provider);
        viewer.setLabelProvider(provider);
        viewer.setInput(referenceList);
        viewer.expandAll();

        return composite;
    }

    protected void initializeBounds() {
        super.initializeBounds();

        Point size = getShell().getSize();
        Point location = getInitialLocation(size);
        getShell().setBounds(getConstrainedShellBounds(new Rectangle(location.x, location.y, size.x, size.y)));
    }
}
