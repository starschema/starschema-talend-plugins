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

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IProcess2;
import org.talend.core.runtime.i18n.Messages;

/**
 * This class must be extended for implementing the specific context composite. <br/>
 * 
 */
public abstract class ContextComposite extends Composite implements IContextModelManager {

    private boolean readOnly;

    private ContextTemplateComposite template;

    private ContextTreeValuesComposite treeValues;

    private ContextTableValuesComposite tableValues;

    private CTabFolder tab;

    private boolean isRepositoryContext;

    private IContextManager contextManager;

    /**
     * bqian ContextComposite constructor comment.
     * 
     * @param parent
     * @param style
     */
    public ContextComposite(Composite parent, IContextManager contextManager) {
        this(parent, contextManager, true);
    }

    public ContextComposite(Composite parent, boolean isRepositoryContext) {
        this(parent, null, isRepositoryContext);
    }

    public ContextComposite(Composite parent, IContextManager contextManager, boolean isRepositoryContext) {
        super(parent, SWT.NONE);
        this.contextManager = contextManager;
        this.isRepositoryContext = isRepositoryContext;
        this.setBackground(parent.getBackground());
        this.setLayout(new GridLayout());
        initializeUI();
    }

    private void setTabEnable(boolean enable) {

        // no need to set the ConextTreeValuesComposite and ConextTableValuesComposite. They can take care of
        // themselvies.
        template.setEnabled(enable);
        tableValues.setEnabled(enable);
        treeValues.setEnabled(enable);

    }

    public void refresh() {
        refreshTemplateTab();
        refreshTableTab();
        refreshTreeTab();
    }

    public void refreshTemplateTab() {
        if (getContextManager() == null) {
            this.setEnabled(false);
            template.clear();
        } else {
            this.setEnabled(true);
            setTabEnable(!isReadOnly());
            toolgeRefreshContextRelitiveComposite(template);
        }

        if (getContextManager() != null) {
            getContextManager().fireContextsChangedEvent();
        }
    }

    public void refreshTableTab() {
        if (getContextManager() != null && tableValues != null) {
            tableValues.refresh();
        }
        if (getContextManager() == null) {
            this.setEnabled(false);
            tableValues.clear();
        } else {
            this.setEnabled(true);
            setTabEnable(!isReadOnly());
            toolgeRefreshContextRelitiveComposite(tableValues);
        }

        if (getContextManager() != null) {
            getContextManager().fireContextsChangedEvent();
        }
    }

    public void refreshTreeTab() {
        if (getContextManager() == null) {
            this.setEnabled(false);
            treeValues.clear();
        } else {
            this.setEnabled(true);
            setTabEnable(!isReadOnly());
            toolgeRefreshContextRelitiveComposite(treeValues);
        }

        if (getContextManager() != null) {
            getContextManager().fireContextsChangedEvent();
        }
    }

    /**
     * 
     * DOC YeXiaowei Comment method "refreshContextEditComposite".
     * 
     * @param composite
     */
    private void toolgeRefreshContextRelitiveComposite(AbstractContextTabEditComposite composite) {
        if (composite == null) {
            return;
        }
        if (composite.isNeedRefresh()) {
            composite.refresh();
        }

        // set need refresh back to true
        composite.setNeedRefresh(true);
    }

    public IContextManager getContextManager() {
        return this.contextManager;
    }

    /**
     * bqian Comment method "initializeUI".
     */
    protected void initializeUI() {

        tab = new CTabFolder(this, SWT.FLAT | SWT.BORDER);
        tab.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                CTabItem cTabItem = (CTabItem) e.item;
                Control control = cTabItem.getControl();
                if (control == treeValues) {
                    refreshTreeTab();
                } else if (control == tableValues) {
                    refreshTableTab();
                } else if (control == template) {
                    refreshTemplateTab();
                } else {
                    refresh();
                }
            }

        });
        tab.setLayoutData(new GridData(GridData.FILL_BOTH));
        CTabItem templateItem = new CTabItem(tab, SWT.NONE);
        templateItem.setText(Messages.getString("ContextComposite.variable")); //$NON-NLS-1$
        creatTemplate(tab, templateItem);

        CTabItem treeValuesItem = new CTabItem(tab, SWT.NONE);
        treeValuesItem.setText(Messages.getString("ContextComposite.treeValue")); //$NON-NLS-1$
        creatTreeValues(tab, treeValuesItem);

        CTabItem tableValuesItem = new CTabItem(tab, SWT.NONE);
        tableValuesItem.setText(Messages.getString("ContextComposite.tableValue")); //$NON-NLS-1$
        creatTableValues(tab, tableValuesItem);
        tab.setSelection(templateItem);
    }

    public CTabFolder getTableFolder() {
        return this.tab;
    }

    protected void layoutButtonBar() {
        this.layout();
    }

    private void creatTemplate(CTabFolder tab, CTabItem templateItem) {
        template = new ContextTemplateComposite(tab, this);
        templateItem.setControl(template);

    }

    private void creatTreeValues(CTabFolder tab, CTabItem treeValuesItem) {
        treeValues = new ContextTreeValuesComposite(tab, this);
        treeValuesItem.setControl(treeValues);
    }

    private void creatTableValues(CTabFolder tab, CTabItem tableValuesItem) {
        tableValues = new ContextTableValuesComposite(tab, this);
        tableValuesItem.setControl(tableValues);
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.context.IContextModelManager#getProcess()
     */
    public IProcess2 getProcess() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.context.IContextModelManager#getCommandStack()
     */
    public CommandStack getCommandStack() {
        return null;
    }

    /**
     * Getter for isRepositoryContext.
     * 
     * @return the isRepositoryContext
     */
    public boolean isRepositoryContext() {
        return this.isRepositoryContext;
    }

}
