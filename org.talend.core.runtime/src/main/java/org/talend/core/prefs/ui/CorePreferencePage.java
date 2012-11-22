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
package org.talend.core.prefs.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ICoreService;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.xml.XmlArray;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.runtime.i18n.Messages;
import org.talend.core.service.ICorePerlService;

/**
 * DOC msjian class global comment. Detailled comment <br/>
 * 
 * $Id: CorePreferencePage.java TDQ-3990 move from org.talend.core $
 * 
 */
public class CorePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

//    private BooleanFieldEditor groupBySource =null;

    private List<FieldEditor> fields = new ArrayList<FieldEditor>();

    /**
     * Construct a new CorePreferencePage.
     */
    public CorePreferencePage() {
        super(GRID);
        
        // MOD msjian 2011-11-17 TDQ-3990: use the service to get the coreplugin(the coreplugin is differert between top and tdq)
        IPreferenceStore store =null;
        // Set the preference store for the preference page.
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICoreService.class)) {
            ICoreService service = (ICoreService) GlobalServiceRegister.getDefault().getService(ICoreService.class);
            store=service.getPreferenceStore();
        }
        // TDQ-3990 ~
        setPreferenceStore(store);
    }

    /**
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        DirectoryFieldEditor filePathTemp = new DirectoryFieldEditor(ITalendCorePrefConstants.FILE_PATH_TEMP, Messages
                .getString("CorePreferencePage.temporaryFiles"), getFieldEditorParent()); //$NON-NLS-1$
        addField(filePathTemp);
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICorePerlService.class)) {
            FileFieldEditor perlInterpreter = new FileFieldEditor(ITalendCorePrefConstants.PERL_INTERPRETER, Messages
                .getString("CorePreferencePage.perlInterpreter"), true, getFieldEditorParent()) {//$NON-NLS-1$

                protected boolean checkState() {
                    if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {
                        return true;
                    }

                    return super.checkState();
                }

            };
        addField(perlInterpreter);
        }
        FileFieldEditor javaInterpreter = new FileFieldEditor(ITalendCorePrefConstants.JAVA_INTERPRETER, Messages
                .getString("CorePreferencePage.javaInterpreter"), true, getFieldEditorParent()){; //$NON-NLS-1$
                protected boolean checkState() {
                    if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.PERL)) {
                        return true;
                    }

                    return super.checkState();
                }
        };
            
        addField(javaInterpreter);

        IntegerFieldEditor previewLimit = new IntegerFieldEditor(ITalendCorePrefConstants.PREVIEW_LIMIT, Messages
                .getString("CorePreferencePage.previewLimit"), getFieldEditorParent(), 9); //$NON-NLS-1$
        previewLimit.setEmptyStringAllowed(false);
        previewLimit.setValidRange(1, 999999999);
        addField(previewLimit);

        BooleanFieldEditor runInMultiThread = new BooleanFieldEditor(
            ITalendCorePrefConstants.RUN_IN_MULTI_THREAD,
            Messages.getString("CorePreferencePage.runInMultiThread"), //$NON-NLS-1$
            getFieldEditorParent()
        );
        addField(runInMultiThread);
        
        DirectoryFieldEditor ireportPath = new DirectoryFieldEditor(ITalendCorePrefConstants.IREPORT_PATH, Messages
                .getString("CorePreferencePage.iReportPath"), getFieldEditorParent()); //$NON-NLS-1$
        addField(ireportPath);
        
        
        BooleanFieldEditor alwaysWelcome = new BooleanFieldEditor(
                ITalendCorePrefConstants.ALWAYS_WELCOME,
                Messages.getString("CorePreferencePage.alwaysWelcome"), //$NON-NLS-1$
                getFieldEditorParent()
            );
         addField(alwaysWelcome);
      
        
        
//            groupBySource = new BooleanFieldEditor(ITalendCorePrefConstants.CONTEXT_GROUP_BY_SOURCE, Messages
//                    .getString("CorePreferencePage.groupBySource"),  getFieldEditorParent());//$NON-NLS-1$
//          addField(groupBySource);

        // ends
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        boolean ok = super.performOk();
        if (ok) {
            if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.PERL)) {
                String perlInterpreter = getPreferenceStore().getString(ITalendCorePrefConstants.PERL_INTERPRETER);
                if (GlobalServiceRegister.getDefault().isServiceRegistered(ICorePerlService.class)) {
                    ICorePerlService service= (ICorePerlService) GlobalServiceRegister.getDefault().getService(ICorePerlService.class);
                    service.setExecutablePreference(perlInterpreter);
                }
            }
            XmlArray.setLimitToDefault();

//          CorePlugin.getDefault().getDesignerCoreService().switchToCurContextsView();
        }
        return ok;
    }

    private void log(String s) {
        log.log(Level.INFO, s);
    }

    private static Logger log = Logger.getLogger(CorePreferencePage.class);

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
        // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#addField(org.eclipse.jface.preference.FieldEditor)
     */
    @Override
    protected void addField(FieldEditor editor) {
        super.addField(editor);
        fields.add(editor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#checkState()
     */
    @Override
    protected void checkState() {
        super.checkState();
        int size = fields.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                FieldEditor editor = fields.get(i);
                if (!editor.isValid()) {
                    editor.setFocus();
                }
            }
        }
    }

}
