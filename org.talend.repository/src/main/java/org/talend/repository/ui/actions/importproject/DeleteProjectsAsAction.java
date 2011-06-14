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
package org.talend.repository.ui.actions.importproject;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.repository.ui.ERepositoryImages;

/**
 * DOC qwei class global comment. Detailled comment
 */
public class DeleteProjectsAsAction extends Action implements IWorkbenchWindowActionDelegate {

    private boolean login;

    public DeleteProjectsAsAction() {
        super();
        this.setImageDescriptor(ImageProvider.getImageDesc(ERepositoryImages.DELETE_PROJECT_ACTION));
        this.login = false;
    }

    public DeleteProjectsAsAction(boolean login) {
        this();
        this.login = login;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        run();
    }

    public void run() {
        // TODO Auto-generated method stub
        Shell activeShell = Display.getCurrent().getActiveShell();
        SelectDeleteProjectDialog dialog = new SelectDeleteProjectDialog(activeShell, this.login);
        if (dialog.open() == Dialog.OK) {
            CorePlugin.getDefault().getRepositoryLocalProviderService().resetXmiResourceSet();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     * org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        // TODO Auto-generated method stub

    }
}
