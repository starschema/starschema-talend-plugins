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
package org.talend.core.ui.context.model.tree;

import org.eclipse.swt.widgets.TreeItem;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.ui.context.ContextTreeValuesComposite;
import org.talend.core.ui.context.model.AbstractContextCellModifier;
import org.talend.core.ui.context.model.tree.GroupByVariableProvider.Son;

/**
 * cli class global comment. Detailled comment
 */
public class ContextTreeCellModifier extends AbstractContextCellModifier {

    public ContextTreeCellModifier(ContextTreeValuesComposite parentModel, boolean repositoryFlag) {
        super(parentModel, repositoryFlag);

    }

    protected ContextTreeValuesComposite getParentMode() {
        return (ContextTreeValuesComposite) super.getParentMode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
     */
    public boolean canModify(Object element, String property) {
        if (getModelManager().isReadOnly()) {
            return false;
        }
        IContextParameter para = getRealParameter(element);
        if (para == null) {
            return false;
        }

        if (!para.isBuiltIn() && !repositoryFlag) {
            // not built-in, not update
            return false;

        }

        if (property.equals(ContextTreeConstants.VARIABLE_COLUMN_PROPERTY)
                || property.equals(ContextTreeConstants.CONTEXT_COLUMN_PROPERTY)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
     */
    public Object getValue(Object element, String property) {
        IContextParameter para = getRealParameter(element);

        if (para != null) {
            if (property.equals(ContextTreeConstants.VALUE_COLUMN_PROPERTY)) {
                return para.getDisplayValue();
            } else if (property.equals(ContextTreeConstants.PROMPT_COLUMN_PROPERTY)) {
                return para.getPrompt();
            } else if (property.equals(ContextTreeConstants.PROMPTNEEDED_COLUMN_PROPERTY)) {
                return para.isPromptNeeded();
            }
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * bqian Comment method "getRealParameter".
     * 
     * @param property
     * @param templatePara
     * @return
     */
    public IContextParameter getRealParameter(Object element) {
        IContextParameter para = null;

        if (element instanceof IContextParameter) {
            para = (IContextParameter) element;
        } else if (element instanceof Son) {
            para = ((Son) element).parameter;
        }
        return para;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
     */
    public void modify(Object element, final String property, final Object value) {
        TreeItem item = (TreeItem) element;
        final Object object = item.getData();

        final IContextParameter para = getRealParameter(object);
        if (para == null) {
            return;
        }
        if (property.equals(ContextTreeConstants.VALUE_COLUMN_PROPERTY)) {
            if (para.getDisplayValue().equals(value)) {
                getParentMode().getViewer().getTree().update();
                return;
            }
            para.setValue((String) value);
            getParentMode().getValueChecker().checkErrors(item, ContextTreeConstants.VARIABLE_COLUMN_INDEX, para);
            // viewer.getTree().update();
        } else if (property.equals(ContextTreeConstants.PROMPT_COLUMN_PROPERTY)) {
            if (para.getPrompt().equals(value)) {
                return;
            }
            para.setPrompt((String) value);
        } else if (property.equals(ContextTreeConstants.PROMPTNEEDED_COLUMN_PROPERTY)) {
            if (para.isPromptNeeded() == ((Boolean) value).booleanValue()) {
                return;
            }
            para.setPromptNeeded((Boolean) value);
        }

        // set updated flag.
        setAndRefreshFlags(object, para);

    }

}
