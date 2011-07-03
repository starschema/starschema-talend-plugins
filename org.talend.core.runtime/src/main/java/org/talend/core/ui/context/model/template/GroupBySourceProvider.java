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
import org.talend.core.model.properties.Project;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.ui.context.ContextComposite;
import org.talend.core.ui.context.ContextManagerHelper;
import org.talend.core.ui.context.IContextModelManager;
import org.talend.core.ui.context.model.ContextProviderProxy;
import org.talend.repository.ProjectManager;

/**
 * A label and content provider for the treeviewer which groups the Contexts by source.
 * 
 */
public class GroupBySourceProvider extends ContextProviderProxy {

    private IContextModelManager modelManager = null;

    public GroupBySourceProvider(IContextModelManager modelManager) {
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
        // int index = -1;
        // String name=getContextManager().getDefaultContext().getName();
        if (element instanceof ContextParameterSortedParent) {
            ContextParameterSortedParent cpsp = ((ContextParameterSortedParent) element);
            String sourceId = cpsp.getSourceId();
            String sourceLabel = sourceId;
            if (sourceId != null && !IContextParameter.BUILT_IN.equals(sourceId)) {
                sourceLabel = sourceId;
                if (cpsp.getProjectLabel() != null) {
                    sourceLabel += ContextConstant.SPACE_CHAR + ContextConstant.REF_CHAR + cpsp.getProjectLabel();
                }
            }
            switch (columnIndex) {
            case 0:
                if (IContextParameter.BUILT_IN.equals(sourceId)) {
                    if (((ContextParameterSortedParent) element).getParameter() != null) {
                        if (getContextManager().getDefaultContext().getContextParameter(
                                ((ContextParameterSortedParent) element).getParameter().getName()) != null) {
                            return getContextManager().getDefaultContext()
                                    .getContextParameter(((ContextParameterSortedParent) element).getParameter().getName())
                                    .getName();
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    if (sourceId != null) {
                        return sourceLabel;
                    } else {
                        break;
                    }
                }
            case 1:
                if ((modelManager instanceof ContextComposite) && !((ContextComposite) modelManager).isRepositoryContext()) {
                    return sourceLabel;
                } else {
                    if (IContextParameter.BUILT_IN.equals(sourceId)) {
                        if (((ContextParameterSortedParent) element).getParameter() != null) {
                            if (getContextManager().getDefaultContext().getContextParameter(
                                    ((ContextParameterSortedParent) element).getParameter().getName()) != null) {
                                String contextParameterType = getContextManager().getDefaultContext()
                                        .getContextParameter(((ContextParameterSortedParent) element).getParameter().getName())
                                        .getType();
                                String s = ContextManagerHelper.convertFormat(contextParameterType);
                                String[] string = null;
                                if (s.indexOf(ContextConstant.DOWNWARDS_STRING) != -1) {
                                    string = s.split(ContextConstant.SPLIT_CHAR);
                                    if ("".equals(getContextManager().getDefaultContext().getContextParameter( //$NON-NLS-1$
                                            ((ContextParameterSortedParent) element).getParameter().getName()).getValue())
                                            || ContextConstant.NULL_STRING.equals(getContextManager()
                                                    .getDefaultContext()
                                                    .getContextParameter(
                                                            ((ContextParameterSortedParent) element).getParameter().getName())
                                                    .getValue())) {
                                        if (ContextParameterJavaTypeManager.getJavaTypesLabels()[0].equals(s)) {
                                            getContextManager()
                                                    .getDefaultContext()
                                                    .getContextParameter(
                                                            ((ContextParameterSortedParent) element).getParameter().getName())
                                                    .setValue(Boolean.FALSE.toString());
                                        }
                                        return string[1];
                                    } else {
                                        if (ContextParameterJavaTypeManager.getJavaTypesLabels()[0].equals(s)) {
                                            if (!(Boolean.TRUE.toString().equals(getContextManager()
                                                    .getDefaultContext()
                                                    .getContextParameter(
                                                            ((ContextParameterSortedParent) element).getParameter().getName())
                                                    .getValue()))
                                                    && !(Boolean.FALSE.toString().equals(getContextManager()
                                                            .getDefaultContext()
                                                            .getContextParameter(
                                                                    ((ContextParameterSortedParent) element).getParameter()
                                                                            .getName()).getValue()))) {
                                                getContextManager()
                                                        .getDefaultContext()
                                                        .getContextParameter(
                                                                ((ContextParameterSortedParent) element).getParameter().getName())
                                                        .setValue(Boolean.FALSE.toString());
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
                        if (sourceId != null) {
                            return ContextConstant.LINE_STRING;
                        } else {
                            break;
                        }
                    }
                }
            case 2:
                if ((modelManager instanceof ContextComposite) && !((ContextComposite) modelManager).isRepositoryContext()) {
                    if (IContextParameter.BUILT_IN.equals(sourceId)) {
                        if (((ContextParameterSortedParent) element).getParameter() != null) {
                            if (getContextManager().getDefaultContext().getContextParameter(
                                    ((ContextParameterSortedParent) element).getParameter().getName()) != null) {
                                String contextParameterType = getContextManager().getDefaultContext()
                                        .getContextParameter(((ContextParameterSortedParent) element).getParameter().getName())
                                        .getType();
                                String s = ContextManagerHelper.convertFormat(contextParameterType);
                                String[] string = null;
                                if (s.indexOf(ContextConstant.DOWNWARDS_STRING) != -1) {
                                    string = s.split(ContextConstant.SPLIT_CHAR);
                                    if ("".equals(getContextManager().getDefaultContext().getContextParameter( //$NON-NLS-1$
                                            ((ContextParameterSortedParent) element).getParameter().getName()).getValue())
                                            || ContextConstant.NULL_STRING.equals(getContextManager()
                                                    .getDefaultContext()
                                                    .getContextParameter(
                                                            ((ContextParameterSortedParent) element).getParameter().getName())
                                                    .getValue())) {
                                        if (ContextParameterJavaTypeManager.getJavaTypesLabels()[0].equals(s)) {
                                            getContextManager()
                                                    .getDefaultContext()
                                                    .getContextParameter(
                                                            ((ContextParameterSortedParent) element).getParameter().getName())
                                                    .setValue(Boolean.FALSE.toString());
                                        }
                                        return string[1];
                                    } else {
                                        if (ContextParameterJavaTypeManager.getJavaTypesLabels()[0].equals(s)) {
                                            if (!(Boolean.TRUE.toString().equals(getContextManager()
                                                    .getDefaultContext()
                                                    .getContextParameter(
                                                            ((ContextParameterSortedParent) element).getParameter().getName())
                                                    .getValue()))
                                                    && !(Boolean.FALSE.toString().equals(getContextManager()
                                                            .getDefaultContext()
                                                            .getContextParameter(
                                                                    ((ContextParameterSortedParent) element).getParameter()
                                                                            .getName()).getValue()))) {
                                                getContextManager()
                                                        .getDefaultContext()
                                                        .getContextParameter(
                                                                ((ContextParameterSortedParent) element).getParameter().getName())
                                                        .setValue(Boolean.FALSE.toString());
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
                        if (sourceId != null) {
                            return ContextConstant.LINE_STRING;
                        } else {
                            break;
                        }
                    }
                } else {
                    final ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();
                    if (codeLanguage == ECodeLanguage.JAVA) {
                        if (IContextParameter.BUILT_IN.equals(sourceId)) {
                            if (((ContextParameterSortedParent) element).getParameter() != null) {
                                if (getContextManager().getDefaultContext().getContextParameter(
                                        ((ContextParameterSortedParent) element).getParameter().getName()) != null) {
                                    return ContextParameterUtils.getNewScriptCode(
                                            getContextManager()
                                                    .getDefaultContext()
                                                    .getContextParameter(
                                                            ((ContextParameterSortedParent) element).getParameter().getName())
                                                    .getName(), codeLanguage);
                                } else {
                                    break;
                                }
                            } else {
                                break;
                            }
                        } else {
                            if (sourceId != null) {
                                return ContextConstant.LINE_STRING;
                            } else {
                                break;
                            }
                        }
                    } else {
                        if (IContextParameter.BUILT_IN.equals(sourceId)) {
                            if (((ContextParameterSortedParent) element).getParameter() != null) {
                                if (getContextManager().getDefaultContext().getContextParameter(
                                        ((ContextParameterSortedParent) element).getParameter().getName()) != null) {
                                    return getContextManager()
                                            .getDefaultContext()
                                            .getContextParameter(
                                                    ((ContextParameterSortedParent) element).getParameter().getName())
                                            .getScriptCode();
                                } else {
                                    break;
                                }
                            } else {
                                break;
                            }
                        } else {
                            if (sourceId != null) {
                                return ContextConstant.LINE_STRING;
                            } else {
                                break;
                            }
                        }
                    }
                }
            case 3:
                if ((modelManager instanceof ContextComposite) && !((ContextComposite) modelManager).isRepositoryContext()) {
                    final ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();
                    if (codeLanguage == ECodeLanguage.JAVA) {
                        if (IContextParameter.BUILT_IN.equals(sourceId)) {
                            if (((ContextParameterSortedParent) element).getParameter() != null) {
                                if (getContextManager().getDefaultContext().getContextParameter(
                                        ((ContextParameterSortedParent) element).getParameter().getName()) != null) {
                                    return ContextParameterUtils.getNewScriptCode(
                                            getContextManager()
                                                    .getDefaultContext()
                                                    .getContextParameter(
                                                            ((ContextParameterSortedParent) element).getParameter().getName())
                                                    .getName(), codeLanguage);
                                } else {
                                    break;
                                }
                            } else {
                                break;
                            }
                        } else {
                            if (sourceId != null) {
                                return ContextConstant.LINE_STRING;
                            } else {
                                break;
                            }
                        }
                    } else {
                        if (IContextParameter.BUILT_IN.equals(sourceId)) {
                            if (((ContextParameterSortedParent) element).getParameter() != null) {
                                if (getContextManager().getDefaultContext().getContextParameter(
                                        ((ContextParameterSortedParent) element).getParameter().getName()) != null) {
                                    return getContextManager()
                                            .getDefaultContext()
                                            .getContextParameter(
                                                    ((ContextParameterSortedParent) element).getParameter().getName())
                                            .getScriptCode();
                                } else {
                                    break;
                                }
                            } else {
                                break;
                            }
                        } else {
                            if (sourceId != null) {
                                return ContextConstant.LINE_STRING;
                            } else {
                                break;
                            }
                        }
                    }
                } else {
                    if (IContextParameter.BUILT_IN.equals(sourceId)) {
                        if (((ContextParameterSortedParent) element).getParameter() != null) {
                            if (getContextManager().getDefaultContext().getContextParameter(
                                    ((ContextParameterSortedParent) element).getParameter().getName()) != null) {
                                return getContextManager().getDefaultContext()
                                        .getContextParameter(((ContextParameterSortedParent) element).getParameter().getName())
                                        .getComment();
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        if (sourceId != null) {
                            return ContextConstant.LINE_STRING;
                        } else {
                            break;
                        }
                    }
                }
            case 4:
                if (IContextParameter.BUILT_IN.equals(sourceId)) {
                    if (((ContextParameterSortedParent) element).getParameter() != null) {
                        if (getContextManager().getDefaultContext().getContextParameter(
                                ((ContextParameterSortedParent) element).getParameter().getName()) != null) {
                            return getContextManager().getDefaultContext()
                                    .getContextParameter(((ContextParameterSortedParent) element).getParameter().getName())
                                    .getComment();
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    if (sourceId != null) {
                        return ContextConstant.LINE_STRING;
                    } else {
                        break;
                    }
                }
            }
        } else {
            ContextParameterSortedSon son = (ContextParameterSortedSon) element;
            switch (columnIndex) {
            case 0:
                // name column
                if (son.getParameter() != null) {
                    if (getContextManager().getDefaultContext().getContextParameter(son.getParameter().getName()) != null) {
                        return getContextManager().getDefaultContext().getContextParameter(son.getParameter().getName())
                                .getName();
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            case 1:
                // source column
                if (son.getParameter() != null) {
                    if (getContextManager().getDefaultContext().getContextParameter(son.getParameter().getName()) != null) {
                        final String source = getContextManager().getDefaultContext()
                                .getContextParameter(son.getParameter().getName()).getSource();
                        ContextItem contextItem = ContextUtils.getContextItemById2(source);
                        if (contextItem != null) {
                            String sourceLabel = contextItem.getProperty().getLabel();
                            final ProjectManager pm = ProjectManager.getInstance();
                            if (!pm.isInCurrentMainProject(contextItem)) {
                                final Project project = pm.getProject(contextItem);
                                if (project != null) {
                                    sourceLabel += ContextConstant.SPACE_CHAR + ContextConstant.REF_CHAR + project.getLabel();
                                }
                            }
                            return sourceLabel;
                        }
                        return source;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            case 2:
                // type column
                if (son.getParameter() != null) {
                    if (getContextManager().getDefaultContext().getContextParameter(son.getParameter().getName()) != null) {
                        String sonType = getContextManager().getDefaultContext()
                                .getContextParameter(son.getParameter().getName()).getType();
                        String s = ContextManagerHelper.convertFormat(sonType);
                        String[] string = null;
                        if (s.indexOf(ContextConstant.DOWNWARDS_STRING) != -1) {
                            string = s.split(ContextConstant.SPLIT_CHAR);
                            if ("".equals(getContextManager().getDefaultContext() //$NON-NLS-1$
                                    .getContextParameter(son.getParameter().getName()).getValue())
                                    || ContextConstant.NULL_STRING.equals(getContextManager().getDefaultContext()
                                            .getContextParameter(son.getParameter().getName()).getValue())) {
                                if (ContextParameterJavaTypeManager.getJavaTypesLabels()[0].equals(s)) {
                                    getContextManager().getDefaultContext().getContextParameter(son.getParameter().getName())
                                            .setValue(Boolean.FALSE.toString());
                                }
                                return string[1];
                            } else {
                                if (ContextParameterJavaTypeManager.getJavaTypesLabels()[0].equals(s)) {
                                    if (!(Boolean.TRUE.toString().equals(getContextManager().getDefaultContext()
                                            .getContextParameter(son.getParameter().getName()).getValue()))
                                            && !(Boolean.FALSE.toString().equals(getContextManager().getDefaultContext()
                                                    .getContextParameter(son.getParameter().getName()).getValue()))) {
                                        getContextManager().getDefaultContext().getContextParameter(son.getParameter().getName())
                                                .setValue(Boolean.FALSE.toString());
                                    }
                                }
                                return string[0];
                            }
                        } else {
                            return ContextManagerHelper.convertFormat(sonType);
                        }
                        // return convertFormat(sonType);
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            case 3:
                // script code column
                final ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();
                if (codeLanguage == ECodeLanguage.JAVA) {
                    if (son.getParameter() != null) {
                        if (getContextManager().getDefaultContext().getContextParameter(son.getParameter().getName()) != null) {
                            return ContextParameterUtils.getNewScriptCode(getContextManager().getDefaultContext()
                                    .getContextParameter(son.getParameter().getName()).getName(), codeLanguage);
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    if (son.getParameter() != null) {
                        if (getContextManager().getDefaultContext().getContextParameter(son.getParameter().getName()) != null) {
                            return getContextManager().getDefaultContext().getContextParameter(son.getParameter().getName())
                                    .getScriptCode();
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            case 4:
                // comment column
                if (son.getParameter() != null) {
                    if (getContextManager().getDefaultContext().getContextParameter(son.getParameter().getName()) != null) {
                        return getContextManager().getDefaultContext().getContextParameter(son.getParameter().getName())
                                .getComment();
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
        boolean flag = false;
        boolean b = false;
        boolean builtin = false;
        List<String> containers = new ArrayList<String>();
        List<String> nameContainers = new ArrayList<String>();
        List<String> oldBuiltinName = new ArrayList<String>();

        List<IContext> contexts = (List<IContext>) inputElement;
        if (!contexts.isEmpty()) {
            // because all the contexts have the save templ
            for (IContextParameter para : contexts.get(0).getContextParameterList()) {
                flag = false;
                if (!containers.isEmpty()) {
                    for (String source : containers) {
                        if (source.equals(para.getSource())) {
                            if (!(IContextParameter.BUILT_IN.equals(para.getSource()))) {
                                flag = true;
                            }
                        }
                    }
                    if (!flag) {
                        containers.add(para.getSource());
                    }
                } else {
                    containers.add(para.getSource());
                }
            }
        }

        if (!contexts.isEmpty()) {
            for (IContextParameter para : contexts.get(0).getContextParameterList()) {
                nameContainers.add(para.getName());
            }
        }

        List<ContextParameterSortedParent> root = new ArrayList<ContextParameterSortedParent>();

        if (!contexts.isEmpty()) {
            IContext context = contexts.get(0);
            oldBuiltinName.clear();
            for (String sourceId : containers) {
                builtin = false;
                ContextParameterSortedParent parent = new ContextParameterSortedParent();
                String sourceLabel = sourceId;
                ContextItem contextItem = ContextUtils.getContextItemById2(sourceId);
                if (contextItem != null) {
                    sourceLabel = contextItem.getProperty().getLabel();
                    final ProjectManager pm = ProjectManager.getInstance();
                    if (!pm.isInCurrentMainProject(contextItem)) {
                        final Project project = pm.getProject(contextItem);
                        if (project != null) {
                            parent.setProjectLabel(project.getLabel());
                        }
                    }
                }
                parent.setSourceId(sourceLabel);
                for (String paraName : nameContainers) {
                    IContextParameter contextPara = context.getContextParameter(paraName);
                    if (contextPara != null && !(IContextParameter.BUILT_IN.equals(contextPara.getSource()))) {
                        if (sourceId.equals(contextPara.getSource())) {
                            ContextParameterSortedSon son = new ContextParameterSortedSon();
                            son.setParameter(contextPara);
                            son.setParent(parent);
                            parent.getSon().add(son);
                        }
                    } else if (contextPara != null) {
                        if (IContextParameter.BUILT_IN.equals(sourceId)) {
                            if (!builtin) {
                                b = false;
                                for (String name : oldBuiltinName) {
                                    if (name.equals(contextPara.getName())) {
                                        b = true;
                                    }
                                }
                                if (!b) {
                                    parent.setParameter(contextPara);
                                    oldBuiltinName.add(contextPara.getName());
                                    builtin = true;
                                }
                            }
                        }
                    }
                }
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
        if (parentElement instanceof ContextParameterSortedParent) {
            return ((ContextParameterSortedParent) parentElement).getSon().toArray();
        }
        return new Object[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
        if (element instanceof ContextParameterSortedSon) {
            return ((ContextParameterSortedSon) element).getParent();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        if (element instanceof ContextParameterSortedParent) {
            return !((ContextParameterSortedParent) element).getSon().isEmpty();
        }
        return false;
    }
}
