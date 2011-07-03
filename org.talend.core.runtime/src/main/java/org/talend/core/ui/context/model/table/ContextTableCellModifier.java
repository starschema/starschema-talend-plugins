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
package org.talend.core.ui.context.model.table;

import java.util.List;

import org.eclipse.swt.widgets.TreeItem;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.ui.context.ContextTableValuesComposite;
import org.talend.core.ui.context.model.AbstractContextCellModifier;

/**
 * cli class global comment. Detailled comment
 */
public class ContextTableCellModifier extends AbstractContextCellModifier {

    public ContextTableCellModifier(ContextTableValuesComposite parentModel, boolean reposFlag) {
        super(parentModel, reposFlag);
    }

    protected ContextTableValuesComposite getParentMode() {
        return (ContextTableValuesComposite) super.getParentMode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
     */
    public boolean canModify(Object element, String property) {
        IContextParameter para = null;
        if (getModelManager().isReadOnly()) {
            return false;
        }
        List<IContext> contextList = getParentMode().getContexts();
        for (int i = 0; i < (contextList.size() + 1); i++) {
            String colProperty = getParentMode().getColumnProperties()[i];
            if (property.equals(colProperty)) {
                para = getRealParameter(colProperty, element);
            }
        }

        if (para == null) {
            return false;
        }

        if (!para.isBuiltIn() && !isRepositoryMode()) {
            // not built-in, not update
            return false;
        }

        if (property.equals(ContextTableConstants.COLUMN_NAME_PROPERTY)) {
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
        IContextParameter para = null;
        List<IContext> contextList = getParentMode().getContexts();
        for (int i = 0; i < (contextList.size() + 1); i++) {
            if (property.equals(getParentMode().getColumnProperties()[i])) {
                para = getRealParameter(getParentMode().getColumnProperties()[i], element);
            }
        }

        if (para != null) {
            for (IContext context : contextList) {
                if (property.equals(context.getName())) {
                    return para.getValue();
                }
            }
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * zwang Comment method "getRealParameter".
     * 
     * @param property
     * @param templatePara
     * @return
     */
    public IContextParameter getRealParameter(String property, Object element) {
        IContextParameter para = null;
        IContext context = null;

        if (!(property.equals(ContextTableConstants.COLUMN_NAME_PROPERTY))) {
            context = getContextManager().getContext(property);
        }
        if (context == null) {
            return null;
        }

        if (element instanceof GroupBySourceTableParent) {
            if (IContextParameter.BUILT_IN.equals(((GroupBySourceTableParent) element).getSourceId())) {
                IContextParameter builtContextParameter = ((GroupBySourceTableParent) element).getContextParameter();
                if (builtContextParameter != null) {
                    para = context.getContextParameter(builtContextParameter.getName());
                }
            }
        } else if (element instanceof GroupBySourceTableSon) {
            para = context.getContextParameter(((GroupBySourceTableSon) element).getParameter().getName());
        } else if (element instanceof GroupByNothingTableParent) {
            para = context.getContextParameter(((GroupByNothingTableParent) element).getContextParameter().getName());
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

        IContextParameter para = null;
        List<IContext> contextList = getParentMode().getContexts();
        for (int i = 0; i < (contextList.size() + 1); i++) {
            if (property.equals(getParentMode().getColumnProperties()[i])) {
                para = getRealParameter(getParentMode().getColumnProperties()[i], object);
                getParentMode().getValueChecker().checkErrors(item, i, para);
            }
        }

        if (para == null) {
            return;
        }

        for (IContext context : contextList) {
            if (property.equals(context.getName())) {
                if (para.getValue().equals(value)) {
                    return;
                }
                para.setValue((String) value);
            }
        }
        // set updated flag.
        setAndRefreshFlags(object, para);
    }
}
