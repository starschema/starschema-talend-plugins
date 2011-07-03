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
package org.talend.designer.core.ui.preferences;

import org.eclipse.gmf.runtime.common.ui.preferences.CheckBoxFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.core.model.repository.IRepositoryPrefConstants;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;

public class ImportExportPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private boolean isSaveUsed = true;

    private CheckBoxFieldEditor isUsedCheckButton;

    public ImportExportPreferencePage() {

        super(GRID);

        IPreferenceStore store = getPreferenceStore();
        setPreferenceStore(store);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        // TODO Auto-generated method stub
        String text = "ImportExportPreferencePage.exportJobScript";
        isUsedCheckButton = new CheckBoxFieldEditor(IRepositoryPrefConstants.USE_EXPORT_SAVE, Messages.getString(text),
                getFieldEditorParent());
        addField(isUsedCheckButton);
        IPreferenceStore store = DesignerPlugin.getDefault().getPreferenceStore();

        isSaveUsed = store.getBoolean(IRepositoryPrefConstants.USE_EXPORT_SAVE);

        isUsedCheckButton.getCheckbox().setSelection(isSaveUsed);
        SelectionListener listener = new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }

            public void widgetSelected(SelectionEvent e) {
                // TODO Auto-generated method stub
                IPreferenceStore store = DesignerPlugin.getDefault().getPreferenceStore();
                store.setValue(IRepositoryPrefConstants.USE_EXPORT_SAVE, ((Button) e.getSource()).getSelection());
            }

        };
        isUsedCheckButton.getCheckbox().addSelectionListener(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
        // TODO Auto-generated method stub

    }

}
