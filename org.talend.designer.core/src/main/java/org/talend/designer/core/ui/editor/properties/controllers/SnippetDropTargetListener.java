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
package org.talend.designer.core.ui.editor.properties.controllers;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.model.process.IElement;
import org.talend.core.model.properties.SnippetItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.snippets.SnippetManager;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;

/**
 * DOC bqian class global comment. Detailled comment <br/>
 * 
 */
public class SnippetDropTargetListener implements TransferDropTargetListener {

    TextViewer viewer;

    private String propertyName;

    private IElement elem;

    private CommandStack commandStack;

    public SnippetDropTargetListener(TextViewer viewer, String propertyName, IElement elem, CommandStack commandStack) {
        this.propertyName = propertyName;
        this.viewer = viewer;
        this.elem = elem;
        this.commandStack = commandStack;
    }

    public void dragEnter(DropTargetEvent event) {
        // TODO Auto-generated method stub

    }

    public void dragLeave(DropTargetEvent event) {
        // TODO Auto-generated method stub

    }

    public void dragOperationChanged(DropTargetEvent event) {
        // TODO Auto-generated method stub

    }

    public void dragOver(DropTargetEvent event) {
        RepositoryNode sourceNode = getSelection();
        if (sourceNode != null) {
            ENodeType type = sourceNode.getType();
            if (type.equals(ENodeType.SIMPLE_FOLDER)) {
                event.detail = DND.DROP_NONE;
            }
        }
    }

    private RepositoryNode getSelection() {
        LocalSelectionTransfer transfer = (LocalSelectionTransfer) getTransfer();
        IStructuredSelection selection = (IStructuredSelection) transfer.getSelection();
        if (selection != null && selection.getFirstElement() instanceof RepositoryNode) {
            RepositoryNode node = (RepositoryNode) selection.getFirstElement();
            return node;
        }
        return null;
    }

    public void drop(DropTargetEvent event) {

        RepositoryNode node = getSelection();
        if (node != null && node.getProperties(EProperties.CONTENT_TYPE).equals(ERepositoryObjectType.SNIPPETS)) {
            SnippetItem snippetItem = (SnippetItem) node.getObject().getProperty().getItem();
            Shell shell = viewer.getTextWidget().getShell();
            SnippetManager manager = new SnippetManager();

            String content = manager.applySnippet(snippetItem, shell);

            // String content = SnippetManager.SNIPPET_PREFFIX + snippetItem.getProperty().getLabel()
            // + SnippetManager.SNIPPET_SUFFIX;

            Point sel = viewer.getSelectedRange();
            IDocument document = viewer.getDocument();
            try {
                document.replace(sel.x, 0, content);
            } catch (BadLocationException ex) {
                MessageBoxExceptionHandler.process(ex);
            }

            if (commandStack != null) {
                String text = viewer.getTextWidget().getText();
                Command cmd = new PropertyChangeCommand(elem, propertyName, text);
                commandStack.execute(cmd);
            }
        }
    }

    public void dropAccept(DropTargetEvent event) {
        // TODO Auto-generated method stub

    }

    public Transfer getTransfer() {
        return LocalSelectionTransfer.getTransfer();
    }

    public boolean isEnabled(DropTargetEvent event) {
        return false;
    }
}
