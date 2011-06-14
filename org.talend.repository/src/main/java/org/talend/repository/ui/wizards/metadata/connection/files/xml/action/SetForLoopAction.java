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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.repository.ui.swt.utils.AbstractXmlStepForm;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.Attribute;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.FOXTreeNode;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.NameSpaceNode;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.util.TreeUtil;

/**
 * wzhang class global comment. Detailled comment
 */
public class SetForLoopAction extends SelectionProviderAction {

    private TreeViewer xmlViewer;

    private AbstractXmlStepForm form;

    public SetForLoopAction(TreeViewer xmlViewer, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
    }

    public SetForLoopAction(TreeViewer xmlViewer, AbstractXmlStepForm form, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
        this.form = form;
    }

    @Override
    public void run() {
        FOXTreeNode node = (FOXTreeNode) this.getStructuredSelection().getFirstElement();
        if (node.isLoop()) {
            return;
        }
        FOXTreeNode rootTreeData = TreeUtil.getRootFOXTreeNode(node);
        TreeUtil.clearSubGroupNode(node);
        // make sure group element is a ancestor of loop, or no group element.
        if (TreeUtil.findUpGroupNode(node) == null) {
            TreeUtil.clearSubGroupNode(rootTreeData);
        }
        TreeUtil.clearLoopNode(rootTreeData);
        TreeUtil.clearMainNode(rootTreeData);

        if (node.isGroup()) {
            node.setGroup(false);
        }
        node.setLoop(true);

        if (form != null) {
            form.updateStatus();
        }
        TreeUtil.upsetMainNode(node);
        xmlViewer.refresh();

        form.updateStatus();
        form.updateConnection();
    }

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
        this.setEnabled(TreeUtil.checkTreeLoopNode(node));
    }
}
