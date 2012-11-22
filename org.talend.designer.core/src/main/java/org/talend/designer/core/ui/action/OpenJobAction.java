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
package org.talend.designer.core.ui.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.talend.designer.core.i18n.Messages;
import org.talend.repository.ui.dialog.OpenJobSelectionDialog;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;

public class OpenJobAction extends Action implements IWorkbenchWindowActionDelegate, IActionDelegate2 {

    IWorkbenchWindow window = null;

    public OpenJobAction() {
        super();
        setDescription(Messages.getString("OpenJobAction.description")); //$NON-NLS-1$
        setToolTipText(Messages.getString("OpenJobAction.tipText")); //$NON-NLS-1$
    }

    public void run() {
        final OpenJobSelectionDialog dialog = new OpenJobSelectionDialog(window.getShell());

        if (dialog.open() == RepositoryReviewDialog.OK) {
            EditProcess editProcess = new EditProcess() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see org.talend.repository.ui.actions.AContextualAction#getSelection()
                 */
                @Override
                public ISelection getSelection() {
                    return new StructuredSelection(dialog.getResult());
                }

            };
            editProcess.run();
        }
    }

    public void run(IAction action) {
        run();
    }

    public void dispose() {
        // do nothing.
    }

    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing. Action doesn't depend on selection.
    }

    // ---- IActionDelegate2
    // ------------------------------------------------

    public void runWithEvent(IAction action, Event event) {
        runWithEvent(event);
    }

    public void init(IAction action) {
        // do nothing.
    }
}
