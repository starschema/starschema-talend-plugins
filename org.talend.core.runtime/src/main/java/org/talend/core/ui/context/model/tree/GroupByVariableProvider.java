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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.ui.context.model.ContextProviderProxy;

/**
 * cli class global comment. Detailled comment
 * 
 * A label and content provider for the treeviewer which groups the Contexts by variable.
 */
public class GroupByVariableProvider extends ContextProviderProxy {

    /**
     * Temporary model for provider. <br/>
     * 
     */
    class Parent {

        String name;

        List<Son> son = new ArrayList<Son>();
    }

    /**
     * Temporary model for provider. <br/>
     * 
     */
    class Son {

        String context;

        Parent parent;

        IContextParameter parameter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof Parent) {
            if (columnIndex == 0) {
                // Variable name column
                return ((Parent) element).name;
            }
        } else {
            Son son = (Son) element;
            switch (columnIndex) {
            case 1:
                // context column
                return son.context;
            case 3:
                // prompt column
                return son.parameter.getPrompt();
            case 4:
                // value column
                return ContextParameterUtils.checkAndHideValue(son.parameter);
            case 5:
                // comment column
                return son.parameter.getComment();
            default:
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
        if (columnIndex == 2 && (element instanceof Son)) {
            Son son = (Son) element;
            if (son.parameter == null) {
                son.getClass();
            }
            if (son.parameter != null && son.parameter.isPromptNeeded()) {
                return ImageProvider.getImage(EImage.CHECKED_ICON);
            } else {
                return ImageProvider.getImage(EImage.UNCHECKED_ICON);
            }

        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
        List<String> containers = new ArrayList<String>();

        List<IContext> contexts = (List<IContext>) inputElement;
        if (!contexts.isEmpty()) {
            // because all the contexts have the save templ
            for (IContextParameter para : contexts.get(0).getContextParameterList()) {
                containers.add(para.getName());
            }
        }

        List<Parent> root = new ArrayList<Parent>();

        for (String paraName : containers) {
            Parent parent = new Parent();
            parent.name = paraName;
            for (IContext context : contexts) {
                IContextParameter contextPara = context.getContextParameter(paraName);
                Son son = new Son();
                son.context = context.getName();
                if (contextPara == null) {
                    son.getClass();
                }
                son.parameter = contextPara;
                son.parent = parent;
                parent.son.add(son);
            }
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
        if (parentElement instanceof Parent) {
            return ((Parent) parentElement).son.toArray();
        }
        return new Object[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
        if (element instanceof Son) {
            return ((Son) element).parent;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        if (element instanceof Parent) {
            return !((Parent) element).son.isEmpty();
        }
        return false;
    }
}
