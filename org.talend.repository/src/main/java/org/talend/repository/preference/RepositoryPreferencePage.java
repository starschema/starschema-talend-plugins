// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.preference;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.commons.ui.swt.preferences.CheckBoxFieldEditor;
import org.talend.core.PluginChecker;
import org.talend.core.model.repository.IRepositoryPrefConstants;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * ggu class global comment. Detailled comment
 * 
 * @deprecated
 */
public class RepositoryPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private CheckBoxFieldEditor manuallyRefreshEditor;

    private CheckBoxFieldEditor creatingRefreshEditor;

    private CheckBoxFieldEditor savingRefreshEditor;

    private CheckBoxFieldEditor deletingRefreshEditor;

    private CheckBoxFieldEditor mergingReferenceProject;

    public RepositoryPreferencePage() {
        super(GRID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        Composite comp = new Composite(getFieldEditorParent(), SWT.NULL);
        comp.setLayout(new GridLayout());

        Group refreshGroup = new Group(comp, SWT.NULL);
        refreshGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        refreshGroup.setText(Messages.getString("RepositoryPreferencePage.RefreshTitle")); //$NON-NLS-1$

        manuallyRefreshEditor = new CheckBoxFieldEditor(IRepositoryPrefConstants.MANUALLY_REFRESH,
                Messages.getString("RepositoryPreferencePage.RefreshManually"), //$NON-NLS-1$
                refreshGroup);
        GridDataFactory.swtDefaults().indent(5, 5).applyTo(manuallyRefreshEditor.getButton());

        Composite childGroup = new Composite(refreshGroup, SWT.NULL);
        GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.horizontalIndent = 5;
        childGroup.setLayoutData(layoutData);

        creatingRefreshEditor = new CheckBoxFieldEditor(IRepositoryPrefConstants.CREATING_REFRESH,
                Messages.getString("RepositoryPreferencePage.RefreshCreated"), childGroup); //$NON-NLS-1$
        GridDataFactory.swtDefaults().indent(10, 0).applyTo(creatingRefreshEditor.getButton());

        savingRefreshEditor = new CheckBoxFieldEditor(IRepositoryPrefConstants.SAVING_REFRESH,
                Messages.getString("RepositoryPreferencePage.RefreshSaved"), //$NON-NLS-1$
                childGroup);
        GridDataFactory.swtDefaults().indent(10, 0).applyTo(savingRefreshEditor.getButton());

        deletingRefreshEditor = new CheckBoxFieldEditor(IRepositoryPrefConstants.DELETING_REFRESH,
                Messages.getString("RepositoryPreferencePage.RefreshDeleted"), //$NON-NLS-1$
                childGroup);
        GridDataFactory.swtDefaults().indent(10, 0).applyTo(deletingRefreshEditor.getButton());

        if (PluginChecker.isRefProjectLoaded()) {
            mergingReferenceProject = new CheckBoxFieldEditor(IRepositoryPrefConstants.MERGE_REFERENCE_PROJECT,
                    Messages.getString("RepositoryPreferencePage.ReferenceProjectMerged"), //$NON-NLS-1$
                    comp);
            GridDataFactory.swtDefaults().indent(10, 0).applyTo(mergingReferenceProject.getButton());
            addField(mergingReferenceProject);
        }
        addField(manuallyRefreshEditor);
        addField(creatingRefreshEditor);
        addField(savingRefreshEditor);
        addField(deletingRefreshEditor);

        addListeners();
    }

    private void addListeners() {

        manuallyRefreshEditor.getButton().addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                boolean value = manuallyRefreshEditor.getBooleanValue();
                creatingRefreshEditor.setChecked(!value);
                savingRefreshEditor.setChecked(!value);
                deletingRefreshEditor.setChecked(!value);
            }
        });
        final SelectionAdapter listener = new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                boolean value = creatingRefreshEditor.getBooleanValue() || savingRefreshEditor.getBooleanValue()
                        || deletingRefreshEditor.getBooleanValue();
                manuallyRefreshEditor.setChecked(!value);
            }
        };
        creatingRefreshEditor.getButton().addSelectionListener(listener);
        savingRefreshEditor.getButton().addSelectionListener(listener);
        deletingRefreshEditor.getButton().addSelectionListener(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
        setPreferenceStore(RepositoryManager.getPreferenceStore());
    }

    @Override
    public void dispose() {
        super.dispose();
        IRepositoryView view = RepositoryManagerHelper.findRepositoryView();
        if (view != null) {
            view.refresh();
        }
    }
}
