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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.ui.context.ContextTableValuesComposite;
import org.talend.core.ui.context.model.ContextProviderProxy;

/**
 * cli class global comment. Detailled comment
 */
public class GroupByNothingTableProvider extends ContextProviderProxy {

    private ContextTableValuesComposite parentModel;

    public GroupByNothingTableProvider(ContextTableValuesComposite parentModel) {
        super();
        this.parentModel = parentModel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(Object element, int columnIndex) {
        int count = 0;
        List<IContext> contextList = parentModel.getContexts();
        count = contextList.size();

        if (element instanceof GroupByNothingTableParent) {
            if (columnIndex == 0) {
                if (((GroupByNothingTableParent) element).getContextParameter() != null) {
                    IContext defaultContext = parentModel.getContextModelManager().getContextManager().getDefaultContext();
                    if (defaultContext.getContextParameter(((GroupByNothingTableParent) element).getContextParameter().getName()) != null) {
                        return defaultContext.getContextParameter(
                                ((GroupByNothingTableParent) element).getContextParameter().getName()).getName();
                    }
                }
            }

            for (int j = 1; j <= count; j++) {
                if (j == 1) {
                    if (columnIndex == j) {
                        if (((GroupByNothingTableParent) element).getContextParameter() != null) {
                            if (contextList.get(columnIndex - 1).getContextParameter(
                                    ((GroupByNothingTableParent) element).getContextParameter().getName()) != null) {
                                return ContextParameterUtils.checkAndHideValue(contextList.get(columnIndex - 1)
                                        .getContextParameter(
                                                ((GroupByNothingTableParent) element).getContextParameter().getName()));
                            }
                        }
                    }
                } else {
                    if (columnIndex == j) {
                        if (((GroupByNothingTableParent) element).getContextParameter() != null) {
                            if (contextList.get(columnIndex - 1).getContextParameter(
                                    ((GroupByNothingTableParent) element).getContextParameter().getName()) != null) {
                                return ContextParameterUtils.checkAndHideValue(contextList.get(columnIndex - 1)
                                        .getContextParameter(
                                                ((GroupByNothingTableParent) element).getContextParameter().getName()));
                            }
                        }
                    }
                }
            }
        }
        return ""; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.context.ConextTreeValuesComposite.ProviderProxy#getColumnImage(java.lang.Object, int)
     */
    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
        List<IContextParameter> contexts = (List<IContextParameter>) inputElement;
        List<GroupByNothingTableParent> root = new ArrayList<GroupByNothingTableParent>();

        for (IContextParameter contextParameter : contexts) {
            GroupByNothingTableParent parent = new GroupByNothingTableParent();
            parent.setContextParameter(contextParameter);
            root.add(parent);
        }
        return root.toArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof GroupByNothingTableParent) {
            return ((GroupByNothingTableParent) parentElement).getSon().toArray();
        }
        return new Object[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
        if (element instanceof GroupByNothingTableSon) {
            return ((GroupByNothingTableSon) element).getParent();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        if (element instanceof GroupByNothingTableParent) {
            return !((GroupByNothingTableParent) element).getSon().isEmpty();
        }
        return false;
    }
}
