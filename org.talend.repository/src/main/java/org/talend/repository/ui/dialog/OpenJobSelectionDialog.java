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

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.views.RepositoryView;

/**
 * A job selection dialog used for opening jobs.
 */
public class OpenJobSelectionDialog extends RepositoryReviewDialog {

    private static final int SELECTINREPOSITORY = 99;

    public OpenJobSelectionDialog(Shell parentShell) {
        super(parentShell, ERepositoryObjectType.PROCESS, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.dialog.RepositoryReviewDialog#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.getString("OpenJobSelectionDialog.findJob")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.repository.ui.dialog.RepositoryReviewDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite
     * )
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, SELECTINREPOSITORY, "Link Repository", false); //$NON-NLS-1$
        super.createButtonsForButtonBar(parent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    @Override
    protected void buttonPressed(int buttonId) {
        if (SELECTINREPOSITORY == buttonId) {
            IStructuredSelection selection = (IStructuredSelection) getRepositoryView().getViewer().getSelection();
            // RepositoryNode node = (RepositoryNode) selection.getFirstElement();
            //
            // RepositoryView.show().expand(node);
            RepositoryView.show().getViewer().setSelection(selection, true);
        } else {
            super.buttonPressed(buttonId);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.dialog.RepositoryReviewDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Control control = super.createDialogArea(parent);
        getRepositoryView().getViewer().addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                boolean highlightOKButton = isSelectionValid(event);
                getButton(SELECTINREPOSITORY).setEnabled(highlightOKButton);
            }

        });

        return control;
    }

}
