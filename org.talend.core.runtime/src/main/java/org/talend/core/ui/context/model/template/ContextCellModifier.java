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
package org.talend.core.ui.context.model.template;

import org.eclipse.swt.widgets.TreeItem;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.types.ContextParameterJavaTypeManager;
import org.talend.core.model.metadata.types.JavaType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.ui.context.ContextManagerHelper;
import org.talend.core.ui.context.ContextTemplateComposite;
import org.talend.core.ui.context.model.AbstractContextCellModifier;

/**
 * ggu class global comment. Detailled comment
 */
public class ContextCellModifier extends AbstractContextCellModifier {

    public ContextCellModifier(ContextTemplateComposite parentComposite) {
        this(parentComposite, false);
    }

    public ContextCellModifier(ContextTemplateComposite parentComposite, boolean reposFlag) {
        super(parentComposite, reposFlag);
    }

    protected ContextTemplateComposite getParentMode() {
        return (ContextTemplateComposite) super.getParentMode();
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

        if (!para.isBuiltIn() && !isRepositoryMode()) {
            // not built-in, not update
            return false;

        }

        if (property.equals(ContextConstant.SOURCE_COLUMN_NAME) || property.equals(ContextConstant.SCRIPTCODE_COLUMN_NAME)) {
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
            if (property.equals(ContextConstant.NAME_COLUMN_NAME)) {
                return para.getName();
            } else if (property.equals(ContextConstant.TYPE_COLUMN_NAME)) {
                String s = ContextManagerHelper.convertFormat(para.getType());
                final ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();
                if (codeLanguage == ECodeLanguage.JAVA) {
                    for (int i = 0; i < ContextParameterJavaTypeManager.getJavaTypesLabels().length; i++) {
                        if (s.equals(ContextParameterJavaTypeManager.getJavaTypesLabels()[i])) {
                            return i;
                        }
                    }
                    return -1;
                } else {
                    for (int i = 0; i < ContextParameterJavaTypeManager.getPerlTypesLabels().length; i++) {
                        if (s.equals(ContextParameterJavaTypeManager.getPerlTypesLabels()[i])) {
                            return i;
                        }
                    }
                    return -1;
                }

            } else if (property.equals(ContextConstant.COMMENT_COLUMN_NAME)) {
                return para.getComment();
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
    private IContextParameter getRealParameter(Object element) {
        IContextParameter para = null;
        IContext context = getContextManager().getDefaultContext();

        if (getParentMode().isGroupBySource()) {
            if (element instanceof ContextParameterSortedParent) {
                if (IContextParameter.BUILT_IN.equals(((ContextParameterSortedParent) element).getSourceId())) {
                    para = context.getContextParameter(((ContextParameterSortedParent) element).getParameter().getName());
                }
            } else if (element instanceof ContextParameterSortedSon) {
                para = context.getContextParameter((((ContextParameterSortedSon) element).getParameter()).getName());
            }
        } else {
            if (element instanceof ContextParameterParent) {
                para = context.getContextParameter(((ContextParameterParent) element).getParameter().getName());
            }
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

        if (property.equals(ContextConstant.NAME_COLUMN_NAME)) {
            if (para.getName().equals(value)) {
                return;
            }
            String name = para.getName();
            getParentMode().renameParameter(name, (String) value, isRepositoryMode());
        } else if (property.equals(ContextConstant.TYPE_COLUMN_NAME)) {
            int index = -1;
            String s = ContextManagerHelper.convertFormat(para.getType());
            final ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();
            if (codeLanguage == ECodeLanguage.JAVA) {
                for (int i = 0; i < ContextParameterJavaTypeManager.getJavaTypesLabels().length; i++) {
                    if (s.equals(ContextParameterJavaTypeManager.getJavaTypesLabels()[i])) {
                        index = i;
                    }
                }
                if (index == ((Integer) value)) {
                    return;
                }
                String newType = getRealType(ContextParameterJavaTypeManager.getJavaTypesLabels()[(Integer) value]);
                String name = para.getName();
                for (IContext context : getContextManager().getListContext()) {
                    for (IContextParameter contextParameter : context.getContextParameterList()) {
                        if (name.equals(contextParameter.getName())) {
                            contextParameter.setType(newType);
                        }
                    }
                }
            } else {
                for (int i = 0; i < ContextParameterJavaTypeManager.getPerlTypesLabels().length; i++) {
                    if (s.equals(ContextParameterJavaTypeManager.getPerlTypesLabels()[i])) {
                        index = i;
                    }
                }
                if (index == ((Integer) value)) {
                    return;
                }
                String newType = getRealType(ContextParameterJavaTypeManager.getPerlTypesLabels()[(Integer) value]);
                String name = para.getName();
                for (IContext context : getContextManager().getListContext()) {
                    for (IContextParameter contextParameter : context.getContextParameterList()) {
                        if (name.equals(contextParameter.getName())) {
                            contextParameter.setType(newType);
                            ContextManagerHelper.checkAndSetDefaultValue(contextParameter);
                        }
                    }
                }
            }
        } else if (property.equals(ContextConstant.COMMENT_COLUMN_NAME)) {
            if (para.getComment().equals(value)) {
                return;
            }
            String name = para.getName();
            for (IContext context : getContextManager().getListContext()) {
                for (IContextParameter contextParameter : context.getContextParameterList()) {
                    if (name.equals(contextParameter.getName())) {
                        contextParameter.setComment((String) value);
                    }
                }
            }
        }
        // set updated flag.
        setAndRefreshFlags(object, para);
    }

    private String getRealType(String type) {
        final ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();
        if (codeLanguage == ECodeLanguage.JAVA) {
            StringBuffer sb = new StringBuffer("id_"); //$NON-NLS-1$
            JavaType javaType = JavaTypesManager.getJavaTypeFromLabel(type);
            if (type.indexOf(ContextConstant.DOWNWARDS_STRING) != -1) {
                return javaType.getId();
            } else {
                if (javaType != null) {
                    return javaType.getId();
                } else {
                    return sb.append(type.trim()).toString();
                }

            }
        } else {
            return type;
        }
    }
}
