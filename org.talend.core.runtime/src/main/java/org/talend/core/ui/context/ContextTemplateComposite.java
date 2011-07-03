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
package org.talend.core.ui.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.context.JobContextParameter;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.types.ContextParameterJavaTypeManager;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.runtime.i18n.Messages;
import org.talend.core.ui.context.action.ContextBuiltinToRepositoryAction;
import org.talend.core.ui.context.model.ContextViewerProvider;
import org.talend.core.ui.context.model.template.ContextCellModifier;
import org.talend.core.ui.context.model.template.ContextColumnSorterListener;
import org.talend.core.ui.context.model.template.ContextConstant;
import org.talend.core.ui.context.model.template.ContextParameterParent;
import org.talend.core.ui.context.model.template.ContextParameterSon;
import org.talend.core.ui.context.model.template.ContextParameterSortedParent;
import org.talend.core.ui.context.model.template.ContextParameterSortedSon;
import org.talend.core.ui.context.model.template.ContextViewerSorter;
import org.talend.core.ui.context.model.template.GroupByNothingProvider;
import org.talend.core.ui.context.model.template.GroupBySourceProvider;
import org.talend.core.utils.TalendQuoteUtils;

/**
 * zwang class global comment. Detailled comment <br/>
 * 
 */
public class ContextTemplateComposite extends AbstractContextTabEditComposite {

    public static final String NEW_PARAM_NAME = "new"; //$NON-NLS-1$

    private boolean readOnly;

    private TreeViewer viewer;

    private ContextViewerProvider provider;

    private ContextCellModifier cellModifier;

    private static final int CNUM_DEFAULT = 4;

    private IContextModelManager modelManager = null;

    private ContextManagerHelper helper;

    private List<Button> buttonList;

    private Listener sortListener;

    private Button moveDownButton;

    private Button moveUpButton;

    private Button orderButton;

    private Button removeButton;

    /**
     * bqian ConextTemplateComposite constructor comment.
     * 
     * @param parent
     * @param style
     */
    public ContextTemplateComposite(Composite parent, IContextModelManager manager) {
        super(parent, SWT.NONE);
        buttonList = new ArrayList<Button>();
        modelManager = manager;
        this.helper = new ContextManagerHelper(manager.getContextManager());
        this.setBackground(parent.getBackground());
        this.setLayout(new GridLayout());
        initializeUI();
    }

    public IContextManager getContextManager() {
        return modelManager.getContextManager();
    }

    public IContextModelManager getContextModelManager() {
        return modelManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled) {
        // update the state of buttons
        for (Button button : buttonList) {
            if (modelManager.isReadOnly()) {
                button.setEnabled(false);
            } else {
                button.setEnabled(enabled);
            }
        }
    }

    /**
     * zwang Comment method "initializeUI".
     */
    private void initializeUI() {
        ComboBoxCellEditor comboBoxCellEditor = null;
        viewer = new TreeViewer(this, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
        final Tree tree = viewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));

        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        column.setText(Messages.getString("ContextTemplateComposite.nameLabel")); //$NON-NLS-1$
        column.setWidth(120);

        if ((modelManager instanceof ContextComposite) && !((ContextComposite) modelManager).isRepositoryContext()) {
            column = new TreeColumn(tree, SWT.NONE);
            column.setText(Messages.getString("ContextTemplateComposite.sourceLabel")); //$NON-NLS-1$
            column.setWidth(80);
        }

        column = new TreeColumn(tree, SWT.NONE);
        column.setText(Messages.getString("ContextTemplateComposite.typeLabel")); //$NON-NLS-1$
        column.setWidth(80);

        column = new TreeColumn(tree, SWT.NONE);
        column.setText(Messages.getString("ContextTemplateComposite.scriptCodeLabel")); //$NON-NLS-1$
        column.setWidth(250);

        column = new TreeColumn(tree, SWT.NONE);
        column.setText(Messages.getString("ContextTemplateComposite.CommentLabel")); //$NON-NLS-1$
        column.setWidth(300);

        final ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();
        if (codeLanguage == ECodeLanguage.JAVA) {
            comboBoxCellEditor = new ComboBoxCellEditor(tree, ContextParameterJavaTypeManager.getJavaTypesLabels());
        } else {
            String[] values = ContextParameterJavaTypeManager.getPerlTypesLabels();
            comboBoxCellEditor = new ComboBoxCellEditor(tree, values);
        }
        boolean isRepositoryContext = (modelManager instanceof ContextComposite)
                && ((ContextComposite) modelManager).isRepositoryContext();

        if (!isRepositoryContext) {
            viewer.setColumnProperties(ContextConstant.GROUP_BY_SOURCE_COLUMN_PROPERTIES);
            viewer.setCellEditors(new CellEditor[] { new TextCellEditor(tree), null, comboBoxCellEditor,
                    new TextCellEditor(tree), new TextCellEditor(tree) });
            ((CCombo) comboBoxCellEditor.getControl()).setEditable(false);
        } else {
            viewer.setColumnProperties(ContextConstant.GROUP_BY_REPOSITORYSOURCE_COLUMN_PROPERTIES);
            viewer.setCellEditors(new CellEditor[] { new TextCellEditor(tree), comboBoxCellEditor, new TextCellEditor(tree),
                    new TextCellEditor(tree) });
            ((CCombo) comboBoxCellEditor.getControl()).setEditable(false);
        }

        cellModifier = new ContextCellModifier(this, isRepositoryContext);
        viewer.setCellModifier(cellModifier);

        provider = new ContextViewerProvider();

        if (isGroupBySource()) {
            provider.setProvider(new GroupBySourceProvider(modelManager));
        } else {
            provider.setProvider(new GroupByNothingProvider(modelManager));
        }

        viewer.setLabelProvider(provider);
        viewer.setContentProvider(provider);

        addSorter(viewer);

        viewer.addPostSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                checkButtonEnableState();
            }
        });
        if (!isRepositoryContext) {
            createPopupMenu();
        }

        final Composite buttonsComposite = new Composite(this, SWT.NONE);
        buttonsComposite.setLayout(GridLayoutFactory.swtDefaults().spacing(0, 0).margins(0, 0).numColumns(7).create());
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.DOWN).grab(true, false).applyTo(buttonsComposite);
        buttonList.clear();
        Button addButton = createAddPushButton(buttonsComposite);
        buttonList.add(addButton);
        removeButton = createRemovePushButton(buttonsComposite);
        buttonList.add(removeButton);

        if (!isRepositoryContext) {// for bug 7393
            moveUpButton = createMoveUpPushButton(buttonsComposite);
            buttonList.add(moveUpButton);
            moveDownButton = createMoveDownPushButton(buttonsComposite);
            buttonList.add(moveDownButton);
        }

        if ((modelManager instanceof ContextComposite) && !((ContextComposite) modelManager).isRepositoryContext()) {
            Button selectContextVariablesButton = createSelectContextVariablesPushButton(buttonsComposite);
            buttonList.add(selectContextVariablesButton);
        }
        if (!isRepositoryContext) {// for bug 7393
            orderButton = createOrderCheckButton(buttonsComposite);
            buttonList.add(orderButton);
        }
    }

    /**
     * 
     * DOC xye Comment method "createPopupMenu".
     */
    private void createPopupMenu() {
        final MenuManager menuMgr = new MenuManager("#PopUp"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager mgr) {
                ContextBuiltinToRepositoryAction action = new ContextBuiltinToRepositoryAction(modelManager);
                action.init(viewer, (IStructuredSelection) viewer.getSelection());
                menuMgr.add(action);
            }
        });

        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
    }

    private void addSorter(final TreeViewer viewer2) {
        final Tree table = viewer2.getTree();
        if (sortListener == null) {
            sortListener = new ContextColumnSorterListener(viewer2);
        }
        table.getColumn(0).addListener(SWT.Selection, sortListener);
        if ((modelManager instanceof ContextComposite) && !((ContextComposite) modelManager).isRepositoryContext()) {
            table.getColumn(1).addListener(SWT.Selection, sortListener);
        }

        table.setSortColumn(table.getColumn(0));
        table.setSortDirection(SWT.UP);
        viewer2.setSorter(new ContextViewerSorter(viewer2, table.getColumn(0), 1));

    }

    private void removeSorter(final TreeViewer viewer2) {
        final Tree table = viewer2.getTree();
        if (sortListener != null) {
            table.getColumn(0).removeListener(SWT.Selection, sortListener);
            if ((modelManager instanceof ContextComposite) && !((ContextComposite) modelManager).isRepositoryContext()) {
                table.getColumn(1).removeListener(SWT.Selection, sortListener);
            }
        }
        // re-order to original orders
        table.setSortDirection(SWT.NONE);
        viewer2.setSorter(new ContextViewerSorter(getContextManager()));

    }

    public boolean renameParameter(final String oldParamName, final String newParamName, boolean reposFlag) {
        IContextParameter param = getContextManager().getDefaultContext().getContextParameter(oldParamName);
        if (param != null && !param.isBuiltIn() && !reposFlag) {
            // not built-in, not update
            return false;
        }

        if (!getContextManager().checkValidParameterName(newParamName)) {
            MessageDialog
                    .openError(
                            this.getShell(),
                            Messages.getString("ContextProcessSection.errorTitle"), Messages.getString("ContextProcessSection.ParameterNameIsNotValid")); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
        // fix 0017942: It is unlimited for total characters of context variable name
        if (null != newParamName && !"".equals(newParamName)) { //$NON-NLS-1$
            if (newParamName.length() > 255) {
                MessageDialog
                        .openError(
                                this.getShell(),
                                Messages.getString("ContextProcessSection.errorTitle"), Messages.getString("ContextTemplateComposite.ParamterLengthInvilid")); //$NON-NLS-1$ //$NON-NLS-2$
                return false;
            }
        }

        onContextRenameParameter(getContextManager(), oldParamName, newParamName);
        return true;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isGroupBySource() {
        boolean isRepositoryContext = false;
        if (modelManager != null) {
            isRepositoryContext = (modelManager instanceof ContextComposite)
                    && ((ContextComposite) modelManager).isRepositoryContext();
        }
        boolean value = getPreferenceStore().getBoolean(ITalendCorePrefConstants.CONTEXT_GROUP_BY_SOURCE);
        boolean order = false;
        if (this.orderButton != null) {
            order = orderButton.getSelection();
        }
        return value && !isRepositoryContext && !order;

    }

    /**
     * zwang Comment method "setContexts".
     * 
     * @param jobContextManager2
     */
    @Override
    public void refresh() {

        if (isGroupBySource()) {
            provider.setProvider(new GroupBySourceProvider(modelManager));
        } else {
            provider.setProvider(new GroupByNothingProvider(modelManager));
        }

        viewer.setLabelProvider(provider);
        viewer.setContentProvider(provider);

        List<IContext> contexts = getContextManager().getListContext();

        refreshVariableSource(contexts);

        viewer.setInput(contexts);

        checkButtonEnableState();

        viewer.refresh();
        viewer.expandAll();
    }

    private void refreshVariableSource(List<IContext> contexts) {
        if (helper == null) {
            return;
        }
        helper.initHelper(getContextManager());
        for (IContext context : contexts) {
            for (IContextParameter param : context.getContextParameterList()) {
                if (!param.isBuiltIn()) {
                    ContextItem item = helper.getContextItemById(param.getSource());
                    if (item == null) { // source not found
                        param.setSource(IContextParameter.BUILT_IN);
                        continue;
                    }
                    // // the variable not exist in the ContextItem
                    // if (!helper.hasExistedVariableFromContextItem(item, param.getName())) {
                    // param.setSource(IContextParameter.BUILT_IN);
                    // }

                }
            }
        }
    }

    /**
     * Clear the data in this viewer.
     * 
     * @param jobContextManager2
     */
    public void clear() {
        viewer.setInput(Collections.EMPTY_LIST);
    }

    /**
     * bqian Comment method "computeContextTemplate".
     * 
     * @param contexts
     * 
     * @return
     */
    public static List<IContextParameter> computeContextTemplate(List<IContext> contexts) {
        List<IContextParameter> contextTemplate = new ArrayList<IContextParameter>();

        if (!contexts.isEmpty()) {
            // All the context has the same template
            List<IContextParameter> paras = contexts.get(0).getContextParameterList();
            for (IContextParameter contextParameter : paras) {

                contextTemplate.add(contextParameter.clone());
            }

        }
        return contextTemplate;
    }

    private Button createAddPushButton(final Composite parent) {
        Button addPushButton = new Button(parent, SWT.PUSH);
        addPushButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IContextParameter parameter = (IContextParameter) createNewEntry();
                if (parameter != null) {
                    // set the source to built-in
                    parameter.setSource(IContextParameter.BUILT_IN);
                    modelManager.onContextAddParameter(getContextManager(), parameter);
                    ContextManagerHelper.revertTreeSelection(getViewer(), parameter);
                    checkButtonEnableState();

                    // see feature 4661: Add an option to propagate when add or remove a variable in a repository
                    // context to jobs/joblets.
                    if (ContextUtils.isPropagateContextVariable() && getContextManager() != null) {
                        IContextManager manager = getContextManager();
                        if (manager != null && manager instanceof JobContextManager) {
                            JobContextManager jobManger = (JobContextManager) manager;
                            // set updated flag.
                            jobManger.setModified(true);
                            jobManger.addNewParameters(parameter.getName());
                        }
                    }

                }
            }

        });
        Image image = ImageProvider.getImage(EImage.ADD_ICON);
        addPushButton.setImage(image);
        return addPushButton;
    }

    public Object createNewEntry() {
        List<IContextParameter> listParams = getContextManager().getDefaultContext().getContextParameterList();
        Integer numParam = new Integer(1);
        boolean paramNameFound;
        String paramName = null;
        do { // look for a new name
            paramNameFound = true;
            paramName = NEW_PARAM_NAME + numParam;
            for (int i = 0; i < listParams.size(); i++) {
                if (paramName.equals(listParams.get(i).getName())) {
                    paramNameFound = false;
                }
            }
            if (!paramNameFound) {
                numParam++;
            }
        } while (!paramNameFound);

        JobContextParameter contextParam = new JobContextParameter();
        contextParam.setName(paramName);
        ECodeLanguage curLanguage = LanguageManager.getCurrentLanguage();
        if (curLanguage == ECodeLanguage.JAVA) {
            contextParam.setType(ContextParameterJavaTypeManager.getDefaultJavaType().getId());
        } else {
            contextParam.setType(MetadataTalendType.getDefaultTalendType());
        }
        contextParam.setPrompt(paramName + "?"); //$NON-NLS-1$
        String defaultValue;
        if (curLanguage == ECodeLanguage.JAVA) {
            defaultValue = ContextParameterJavaTypeManager.getDefaultValueFromJavaIdType(ContextParameterJavaTypeManager
                    .getDefaultJavaType().getId(), false);
        } else {
            defaultValue = TalendQuoteUtils.addQuotes(""); //$NON-NLS-1$
        }
        contextParam.setValue(defaultValue);
        contextParam.setComment(""); //$NON-NLS-1$
        contextParam.setSource(""); //$NON-NLS-1$
        return contextParam;
    }

    private Button createRemovePushButton(final Composite parent) {
        Button removePushButton = new Button(parent, SWT.PUSH);
        removePushButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                IContextParameter parameter = null;
                TreeItem[] items = viewer.getTree().getSelection();

                Object[] obj = new Object[items.length];

                for (int i = 0; i < obj.length; i++) {
                    obj[i] = items[i].getData();
                }

                if (items.length > 0) {
                    for (Object object : obj) { // multi delete
                        IContextParameter beforeParam = ContextManagerHelper.getNearParameterBySelectionItem(getViewer());
                        if (object == null) {
                            return;
                        }

                        if (object instanceof ContextParameterSortedParent) {
                            if (IContextParameter.BUILT_IN.equals(((ContextParameterSortedParent) object).getSourceId())) {
                                /*
                                 * make the view just refresh one time when delete more than one context in designer
                                 * added by hyWang
                                 */
                                Set<String> paramNamesFromBuiltIn = new HashSet<String>();
                                for (Object o : obj) {
                                    if (o instanceof ContextParameterSortedParent) {
                                        parameter = ((ContextParameterSortedParent) o).getParameter();
                                    } else if (o instanceof ContextParameterSortedSon) {
                                        parameter = ((ContextParameterSortedSon) o).getParameter();
                                    }
                                    if (parameter != null) {
                                        paramNamesFromBuiltIn.add(parameter.getName());
                                    }
                                }
                                modelManager.onContextRemoveParameter(getContextManager(), paramNamesFromBuiltIn);
                            } else {
                                Set<String> paramNamesFromRepository = new HashSet<String>();
                                for (ContextParameterSon son : ((ContextParameterSortedParent) object).getSon()) {
                                    parameter = ((ContextParameterSortedSon) son).getParameter();
                                    paramNamesFromRepository.add(parameter.getName());
                                }
                                modelManager.onContextRemoveParameter(getContextManager(), paramNamesFromRepository);
                            }
                        } else if (object instanceof ContextParameterSortedSon) {
                            parameter = ((ContextParameterSortedSon) object).getParameter();
                            modelManager.onContextRemoveParameter(getContextManager(), parameter.getName());
                        } else if (object instanceof ContextParameterParent) {
                            parameter = ((ContextParameterParent) object).getParameter();
                            modelManager.onContextRemoveParameter(getContextManager(), parameter.getName());
                        }
                        modelManager.refreshTemplateTab();
                        ContextManagerHelper.revertTreeSelection(getViewer(), beforeParam);
                        checkButtonEnableState();
                    }
                }
            }
        });

        Image image = ImageProvider.getImage(EImage.DELETE_ICON);
        removePushButton.setImage(image);
        return removePushButton;
    }

    private Button createSelectContextVariablesPushButton(final Composite parent) {
        Button selectContextVariablesPushButton = new Button(parent, SWT.PUSH);
        Image image = ImageProvider.getImage(ECoreImage.CONTEXT_ICON);
        selectContextVariablesPushButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                SelectRepositoryContextDialog dialog = new SelectRepositoryContextDialog(getContextModelManager(), parent
                        .getShell(), helper);
                dialog.open();
                refresh();
            }

        });
        selectContextVariablesPushButton.setImage(image);
        return selectContextVariablesPushButton;
    }

    private Button createMoveUpPushButton(final Composite parent) {
        Button moveUpPushButton = new Button(parent, SWT.PUSH);
        Image image = ImageProvider.getImage(EImage.UP_ICON);
        moveUpPushButton.setImage(image);
        moveUpPushButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (ContextManagerHelper.changeContextOrder(viewer, modelManager, true)) {
                    checkButtonEnableState();
                    viewer.getTree().setFocus();
                }
            }

        });
        return moveUpPushButton;
    }

    private Button createMoveDownPushButton(final Composite parent) {
        Button moveDownPushButton = new Button(parent, SWT.PUSH);
        Image image = ImageProvider.getImage(EImage.DOWN_ICON);
        moveDownPushButton.setImage(image);
        moveDownPushButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (ContextManagerHelper.changeContextOrder(viewer, modelManager, false)) {
                    checkButtonEnableState();
                    viewer.getTree().setFocus();
                }
            }

        });
        return moveDownPushButton;
    }

    private Button createOrderCheckButton(final Composite parent) {
        Composite orderComposite = new Composite(parent, SWT.NONE);
        orderComposite.setBackground(parent.getBackground());

        orderComposite.setLayout(GridLayoutFactory.swtDefaults().spacing(10, 0).margins(5, 0).create());
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(orderComposite);

        final Button orderBtn = new Button(orderComposite, SWT.CHECK);
        orderBtn.setText(Messages.getString("ConextTemplateComposite.OrderText")); //$NON-NLS-1$
        orderBtn.setToolTipText(Messages.getString("ConextTemplateComposite.OrderMessages")); //$NON-NLS-1$
        orderBtn.setAlignment(SWT.LEFT);
        GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(orderBtn);

        orderBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (orderBtn.getSelection()) {
                    removeSorter(viewer);
                } else {
                    addSorter(viewer);
                }
                refresh();
            }

        });
        return orderBtn;
    }

    private void checkButtonEnableState() {
        boolean orderEnable = false;
        boolean selectionEnable = false;
        boolean removeEnable = false;
        if (this.orderButton != null) {
            orderEnable = this.orderButton.getSelection();
        }
        if (this.viewer != null) {
            ISelection selection = this.viewer.getSelection();
            selectionEnable = !selection.isEmpty();
            removeEnable = !selection.isEmpty();
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection sel = (IStructuredSelection) selection;
                if (sel.size() > 1) {
                    // Multi selection, not support sort
                    selectionEnable = false;
                }
            }
        }
        selectionEnable = selectionEnable && !modelManager.isReadOnly();
        removeEnable = removeEnable && !modelManager.isReadOnly();
        boolean moveState = orderEnable && selectionEnable;
        if (this.moveUpButton != null) {
            this.moveUpButton.setEnabled(moveState);
        }
        if (this.moveDownButton != null) {
            this.moveDownButton.setEnabled(moveState);
        }
        if (this.removeButton != null) {
            this.removeButton.setEnabled(removeEnable);
        }
    }

    protected void onContextRenameParameter(IContextManager contextManager, String oldName, String newName) {
        modelManager.onContextRenameParameter(contextManager, oldName, newName);
    }

    protected void onContextModify(IContextManager contextManager, IContextParameter parameter) {
        modelManager.onContextModify(contextManager, parameter);
    }

    /**
     * bqian Comment method "getProcess".
     * 
     * @return
     */
    private IProcess getProcess() {
        return modelManager.getProcess();
    }

    /**
     * bqian Comment method "getCommandStack".
     * 
     * @return
     */

    public IContext getSelectedContext() {
        return this.getContextManager().getDefaultContext();
    }

    public TreeViewer getViewer() {
        return this.viewer;
    }

}
