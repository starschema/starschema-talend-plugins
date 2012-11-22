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
package org.talend.designer.core.ui.editor.cmd;

import org.eclipse.gef.commands.Command;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * Command that will set or remove the start status on a node. <br/>
 * 
 * $Id: ChangeActivateStatusNodeCommand.java 3351 2007-05-04 12:14:00 +0000 (ven., 04 mai 2007) plegall $
 * 
 */
public class ChangeActivateStatusSubjobCommand extends Command {

    private Node node;

    private boolean value;

    private boolean oneComponent;

    /**
     * Gives the node where the status will be set or removed.
     * 
     * @param newStartNode
     */
    public ChangeActivateStatusSubjobCommand(Node node, boolean oneComponent) {
        this.node = node;
        this.oneComponent = oneComponent;
        if (node.isStart()) {
            if (!oneComponent) {
                if (node.isActivate()) {
                    value = false;
                    setLabel(Messages.getString("ChangeActivateStatusSubjobCommand.Label.DeactiveComplete")); //$NON-NLS-1$
                } else {
                    value = true;
                    setLabel(Messages.getString("ChangeActivateStatusSubjobCommand.Label.ActiveComplete")); //$NON-NLS-1$
                }
            } else {
                if (node.isActivate()) {
                    value = false;
                    setLabel(Messages.getString("ChangeActivateStatusSubjobCommand.Label.DeactivePart")); //$NON-NLS-1$
                } else {
                    value = true;
                    setLabel(Messages.getString("ChangeActivateStatusSubjobCommand.Label.ActivePart")); //$NON-NLS-1$
                }
            }

        } else {
            if (node.isActivate()) {
                value = false;
                setLabel(Messages.getString("ChangeActivateStatusSubjobCommand.Label.DeactivePart")); //$NON-NLS-1$
            } else {
                value = true;
                setLabel(Messages.getString("ChangeActivateStatusSubjobCommand.Label.ActivePart")); //$NON-NLS-1$
            }
        }
    }

    private void refreshPropertyView() {
        // IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        // IViewPart view = page.findView("org.eclipse.ui.views.PropertySheet"); //$NON-NLS-1$
        // PropertySheet sheet = (PropertySheet) view;
        // if (sheet.getCurrentPage() instanceof TabbedPropertySheetPage) {
        // TabbedPropertySheetPage tabbedPropertySheetPage = (TabbedPropertySheetPage) sheet.getCurrentPage();
        // if (tabbedPropertySheetPage.getCurrentTab() != null) {
        // tabbedPropertySheetPage.refresh();
        // }
        // }
    }

    public void execute() {
        Process process = (Process) node.getProcess();
        process.setActivateSubjob(node, value, oneComponent);
        process.checkStartNodes();
        process.checkProcess();
        refreshPropertyView();
    }

    public void undo() {
        Process process = (Process) node.getProcess();
        process.setActivateSubjob(node, !value, oneComponent);
        process.checkStartNodes();
        process.checkProcess();
        refreshPropertyView();
    }

    public void redo() {
        this.execute();
    }
}
