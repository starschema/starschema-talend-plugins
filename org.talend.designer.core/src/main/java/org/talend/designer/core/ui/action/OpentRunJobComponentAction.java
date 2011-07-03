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
package org.talend.designer.core.ui.action;

import java.util.List;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.model.properties.ProcessItem;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.MultiPageTalendEditor;
import org.talend.designer.core.ui.editor.ProcessEditorInput;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.views.properties.ComponentSettingsView;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC zli class global comment. Detailled comment add for featrue 13361
 */
public class OpentRunJobComponentAction extends SelectionAction {

    /**
     * DOC zli OpentRunJobComponentAction constructor comment.
     * 
     * @param part
     */
    public OpentRunJobComponentAction(IWorkbenchPart part) {
        super(part);
        setText(Messages.getString("OpentRunJobComponent.Title")); //$NON-NLS-1$
        setToolTipText(Messages.getString("OpentRunJobComponent.Tooltip")); //$NON-NLS-1$
        setDescription(Messages.getString("OpentRunJobComponent.Tooltip")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
     */
    @Override
    protected boolean calculateEnabled() {
        List parts = getSelectedObjects();
        if (parts.size() != 1) {
            return false;
        }
        Object o = parts.get(0);
        if (o instanceof NodePart) {
            Object model = ((NodePart) o).getModel();
            if (model instanceof Node) {
                String name = ((Node) model).getComponent().getName();
                if ("tRunJob".equals(name)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void run() {

        List selection = getSelectedObjects();

        Object input = selection.get(0);
        if (input instanceof NodePart) {
            NodePart part = (NodePart) input;
            Node node = (Node) part.getModel();
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            String processName = (String) node.getPropertyValue(EParameterName.PROCESS_TYPE_PROCESS.getName());
            String version = (String) node.getPropertyValue(EParameterName.PROCESS_TYPE_VERSION.getName());

            if (processName != null && !"".equals(processName)) { //$NON-NLS-1$
                try {
                    ItemCacheManager.clearCache();
                    ProcessItem processItem = ItemCacheManager.getProcessItem(processName, version);
                    if (processItem != null) {
                        ProcessEditorInput fileEditorInput = new ProcessEditorInput(processItem, true);

                        IEditorPart editorPart = page.findEditor(fileEditorInput);

                        if (editorPart == null) {
                            IViewPart viewPart = page.findView(IRepositoryView.VIEW_ID);
                            if (viewPart != null) {
                                fileEditorInput.setView((IRepositoryView) viewPart);
                                fileEditorInput.setRepositoryNode(null);
                                page.openEditor(fileEditorInput, MultiPageTalendEditor.ID, true);
                            }
                        } else {
                            page.activate(editorPart);
                        }
                    }
                } catch (PartInitException e) {
                    MessageBoxExceptionHandler.process(e);
                } catch (PersistenceException e) {
                    MessageBoxExceptionHandler.process(e);
                }
            } else {
                try {
                    // modified for feature 2454.
                    page.showView(ComponentSettingsView.ID);
                } catch (PartInitException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
    }

}
