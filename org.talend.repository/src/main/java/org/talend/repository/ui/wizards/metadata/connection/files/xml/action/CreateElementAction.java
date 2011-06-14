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
package org.talend.repository.ui.wizards.metadata.connection.files.xml.action;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.repository.ui.swt.utils.AbstractXmlStepForm;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.Attribute;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.Element;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.FOXTreeNode;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.NameSpaceNode;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.util.StringUtil;

/**
 * wzhang class global comment. Detailled comment
 */
public class CreateElementAction extends SelectionProviderAction {

    private TreeViewer xmlViewer;

    private AbstractXmlStepForm form;

    public CreateElementAction(TreeViewer xmlViewer, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
    }

    public CreateElementAction(TreeViewer xmlViewer, AbstractXmlStepForm form, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
        this.form = form;
    }

    @Override
    public void run() {
        FOXTreeNode node = (FOXTreeNode) this.getStructuredSelection().getFirstElement();
        if (createChildNode(node)) {
            form.redrawLinkers();
        }
        form.updateConnection();
    }

    private boolean createChildNode(FOXTreeNode node) {
        if (node.getColumn() != null) {
            if (!MessageDialog.openConfirm(xmlViewer.getControl().getShell(), "Warning",
                    "Do you want to disconnect the existing linker and then add an sub element for the selected element "
                            + node.getLabel() + " \"?")) {
                return false;
            }
            node.setColumn(null);
        }
        String label = "";
        while (!StringUtil.validateLabelForXML(label)) {
            InputDialog dialog = new InputDialog(null, "Input element's label", "nput the new element's valid label", "", null);
            int status = dialog.open();
            if (status == InputDialog.OK) {
                label = dialog.getValue().trim();
            }
            if (status == InputDialog.CANCEL) {
                return false;
            }
        }
        FOXTreeNode child = new Element(label);
        // child.setRow(node.getRow());
        node.addChild(child);
        this.xmlViewer.refresh();
        this.xmlViewer.expandAll();
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IStructuredSelection selection) {
        FOXTreeNode node = (FOXTreeNode) this.getStructuredSelection().getFirstElement();
        if (node == null) {
            this.setEnabled(false);
            return;
        }
        if (node instanceof Attribute) {
            this.setEnabled(false);
            return;
        }

        if (node instanceof NameSpaceNode) {
            this.setEnabled(false);
            return;
        }
        this.setEnabled(true);
    }
}
