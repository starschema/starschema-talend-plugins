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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.metadata.types.ContextParameterJavaTypeManager;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.ui.context.ContextComposite;
import org.talend.core.ui.context.ContextManagerHelper;
import org.talend.core.ui.context.IContextModelManager;
import org.talend.core.ui.context.model.ContextProviderProxy;

/**
 * A label and content provider for the treeviewer which groups the Contexts by nothing.
 * 
 */
public class GroupByNothingProvider extends ContextProviderProxy {

    private IContextModelManager modelManager = null;

    public GroupByNothingProvider(IContextModelManager modelManager) {
        super();
        this.modelManager = modelManager;
        if (modelManager == null) {
            throw new NullPointerException();
        }
    }

    private IContextManager getContextManager() {
        if (modelManager != null) {
            return modelManager.getContextManager();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof ContextParameterParent) {
            switch (columnIndex) {
            case 0:
                if (((ContextParameterParent) element).getParameter() != null) {
                    if (getContextManager().getDefaultContext().getContextParameter(
                            ((ContextParameterParent) element).getParameter().getName()) != null) {
                        return getContextManager().getDefaultContext().getContextParameter(
                                ((ContextParameterParent) element).getParameter().getName()).getName();
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            case 1:
                if ((modelManager instanceof ContextComposite) && !((ContextComposite) modelManager).isRepositoryContext()) {
                    if (((ContextParameterParent) element).getParameter() != null) {
                        if (getContextManager().getDefaultContext().getContextParameter(
                                ((ContextParameterParent) element).getParameter().getName()) != null) {
                            final String source = getContextManager().getDefaultContext().getContextParameter(
                                    ((ContextParameterParent) element).getParameter().getName()).getSource();
                            ContextItem contextItem = ContextUtils.getContextItemById2(source);
                            if (contextItem != null) {
                                return contextItem.getProperty().getLabel();
                            }
                            return source;
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    if (((ContextParameterParent) element).getParameter() != null) {
                        if (getContextManager().getDefaultContext().getContextParameter(
                                ((ContextParameterParent) element).getParameter().getName()) != null) {
                            String contextParameterType = getContextManager().getDefaultContext().getContextParameter(
                                    ((ContextParameterParent) element).getParameter().getName()).getType();
                            String s = ContextManagerHelper.convertFormat(contextParameterType);
                            String[] string = null;
                            if (s.indexOf(ContextConstant.DOWNWARDS_STRING) != -1) {
                                string = s.split(ContextConstant.SPLIT_CHAR);
                                if ("".equals(getContextManager().getDefaultContext().getContextParameter( //$NON-NLS-1$
                                        ((ContextParameterParent) element).getParameter().getName()).getValue())
                                        || ContextConstant.NULL_STRING.equals(getContextManager().getDefaultContext()
                                                .getContextParameter(((ContextParameterParent) element).getParameter().getName())
                                                .getValue())) {
                                    if (ContextParameterJavaTypeManager.getJavaTypesLabels()[0].equals(s)) {
                                        getContextManager().getDefaultContext().getContextParameter(
                                                ((ContextParameterParent) element).getParameter().getName()).setValue(
                                                Boolean.FALSE.toString());
                                    }
                                    return string[1];
                                } else {
                                    if (ContextParameterJavaTypeManager.getJavaTypesLabels()[0].equals(s)) {
                                        if (!(Boolean.TRUE.toString().equals(getContextManager().getDefaultContext()
                                                .getContextParameter(((ContextParameterParent) element).getParameter().getName())
                                                .getValue()))
                                                && !(Boolean.FALSE.toString().equals(getContextManager().getDefaultContext()
                                                        .getContextParameter(
                                                                ((ContextParameterParent) element).getParameter().getName())
                                                        .getValue()))) {
                                            getContextManager().getDefaultContext().getContextParameter(
                                                    ((ContextParameterParent) element).getParameter().getName()).setValue(
                                                    Boolean.FALSE.toString());
                                        }
                                    }
                                    return string[0];
                                }
                            } else {
                                return ContextManagerHelper.convertFormat(contextParameterType);
                            }
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            case 2:
                if ((modelManager instanceof ContextComposite) && !((ContextComposite) modelManager).isRepositoryContext()) {
                    if (((ContextParameterParent) element).getParameter() != null) {
                        if (getContextManager().getDefaultContext().getContextParameter(
                                ((ContextParameterParent) element).getParameter().getName()) != null) {
                            String contextParameterType = getContextManager().getDefaultContext().getContextParameter(
                                    ((ContextParameterParent) element).getParameter().getName()).getType();
                            String s = ContextManagerHelper.convertFormat(contextParameterType);
                            String[] string = null;
                            if (s.indexOf(ContextConstant.DOWNWARDS_STRING) != -1) {
                                string = s.split(ContextConstant.SPLIT_CHAR);
                                if ("".equals(getContextManager().getDefaultContext().getContextParameter( //$NON-NLS-1$
                                        ((ContextParameterParent) element).getParameter().getName()).getValue())
                                        || ContextConstant.NULL_STRING.equals(getContextManager().getDefaultContext()
                                                .getContextParameter(((ContextParameterParent) element).getParameter().getName())
                                                .getValue())) {
                                    if (ContextParameterJavaTypeManager.getJavaTypesLabels()[0].equals(s)) {
                                        getContextManager().getDefaultContext().getContextParameter(
                                                ((ContextParameterParent) element).getParameter().getName()).setValue(
                                                Boolean.FALSE.toString());
                                    }
                                    return string[1];
                                } else {
                                    if (ContextParameterJavaTypeManager.getJavaTypesLabels()[0].equals(s)) {
                                        if (!(Boolean.TRUE.toString().equals(getContextManager().getDefaultContext()
                                                .getContextParameter(((ContextParameterParent) element).getParameter().getName())
                                                .getValue()))
                                                && !(Boolean.FALSE.toString().equals(getContextManager().getDefaultContext()
                                                        .getContextParameter(
                                                                ((ContextParameterParent) element).getParameter().getName())
                                                        .getValue()))) {
                                            getContextManager().getDefaultContext().getContextParameter(
                                                    ((ContextParameterParent) element).getParameter().getName()).setValue(
                                                    Boolean.FALSE.toString());
                                        }
                                    }
                                    return string[0];
                                }
                            } else {
                                return ContextManagerHelper.convertFormat(contextParameterType);
                            }
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    final ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();
                    if (codeLanguage == ECodeLanguage.JAVA) {
                        if (((ContextParameterParent) element).getParameter() != null) {
                            if (getContextManager().getDefaultContext().getContextParameter(
                                    ((ContextParameterParent) element).getParameter().getName()) != null) {
                                return ContextParameterUtils.getNewScriptCode(getContextManager().getDefaultContext()
                                        .getContextParameter(((ContextParameterParent) element).getParameter().getName())
                                        .getName(), codeLanguage);
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        if (((ContextParameterParent) element).getParameter() != null) {
                            if (getContextManager().getDefaultContext().getContextParameter(
                                    ((ContextParameterParent) element).getParameter().getName()) != null) {
                                return getContextManager().getDefaultContext().getContextParameter(
                                        ((ContextParameterParent) element).getParameter().getName()).getScriptCode();
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            case 3:
                if ((modelManager instanceof ContextComposite) && !((ContextComposite) modelManager).isRepositoryContext()) {
                    final ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();
                    if (codeLanguage == ECodeLanguage.JAVA) {
                        if (((ContextParameterParent) element).getParameter() != null) {
                            if (getContextManager().getDefaultContext().getContextParameter(
                                    ((ContextParameterParent) element).getParameter().getName()) != null) {
                                return ContextParameterUtils.getNewScriptCode(getContextManager().getDefaultContext()
                                        .getContextParameter(((ContextParameterParent) element).getParameter().getName())
                                        .getName(), codeLanguage);
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        if (((ContextParameterParent) element).getParameter() != null) {
                            if (getContextManager().getDefaultContext().getContextParameter(
                                    ((ContextParameterParent) element).getParameter().getName()) != null) {
                                return getContextManager().getDefaultContext().getContextParameter(
                                        ((ContextParameterParent) element).getParameter().getName()).getScriptCode();
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                } else {
                    if (((ContextParameterParent) element).getParameter() != null) {
                        if (getContextManager().getDefaultContext().getContextParameter(
                                ((ContextParameterParent) element).getParameter().getName()) != null) {
                            return getContextManager().getDefaultContext().getContextParameter(
                                    ((ContextParameterParent) element).getParameter().getName()).getComment();
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            case 4:
                if (((ContextParameterParent) element).getParameter() != null) {
                    if (getContextManager().getDefaultContext().getContextParameter(
                            ((ContextParameterParent) element).getParameter().getName()) != null) {
                        return getContextManager().getDefaultContext().getContextParameter(
                                ((ContextParameterParent) element).getParameter().getName()).getComment();
                    } else {
                        break;
                    }
                } else {
                    break;
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
        List<IContext> contexts = (List<IContext>) inputElement;
        List<ContextParameterParent> root = new ArrayList<ContextParameterParent>();

        if (!contexts.isEmpty()) {
            IContext context = contexts.get(0);
            for (IContextParameter contextParameter : context.getContextParameterList()) {
                ContextParameterParent parent = new ContextParameterParent();
                parent.setParameter(contextParameter);

                root.add(parent);
            }
        }
        return root.toArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof ContextParameterParent) {
            return ((ContextParameterParent) parentElement).getSon().toArray();
        }
        return new Object[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
        if (element instanceof ContextParameterSon) {
            return ((ContextParameterSon) element).getParent();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        if (element instanceof ContextParameterParent) {
            return !((ContextParameterParent) element).getSon().isEmpty();
        }
        return false;
    }
}
