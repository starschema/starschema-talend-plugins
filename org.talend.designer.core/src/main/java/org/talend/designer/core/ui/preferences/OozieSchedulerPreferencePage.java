package org.talend.designer.core.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.core.CorePlugin;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.designer.core.i18n.Messages;

public class OozieSchedulerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public OozieSchedulerPreferencePage() {
        super(GRID);
        setPreferenceStore(CorePlugin.getDefault().getPreferenceStore());
    }

    protected void createFieldEditors() {
        // UserName for acessing hadoop
        StringFieldEditor userNameFE = new StringFieldEditor(ITalendCorePrefConstants.OOZIE_SCHEDULER_USER_NAME,
                Messages.getString("OoziePreferencePage.userName"), getFieldEditorParent());
        userNameFE.setEmptyStringAllowed(true);
        addField(userNameFE);

        // Name Node End Point
        StringFieldEditor nameNodeFE = new StringFieldEditor(ITalendCorePrefConstants.OOZIE_SHCEDULER_NAME_NODE_ENDPOINT,
                Messages.getString("OoziePreferencePage.nameNodeEndPoint"), getFieldEditorParent());
        nameNodeFE.setEmptyStringAllowed(true);
        addField(nameNodeFE);

        // Job Tracker End Point
        StringFieldEditor jobTrackerFE = new StringFieldEditor(ITalendCorePrefConstants.OOZIE_SHCEDULER_JOB_TRACKER_ENDPOINT,
                Messages.getString("OoziePreferencePage.jobTrackerEndPoint"), getFieldEditorParent());
        jobTrackerFE.setEmptyStringAllowed(true);
        addField(jobTrackerFE);

        // Oozie End Point
        StringFieldEditor oozieFE = new StringFieldEditor(ITalendCorePrefConstants.OOZIE_SHCEDULER_OOZIE_ENDPOINT,
                Messages.getString("OoziePreferencePage.oozieEndPoint"), getFieldEditorParent());
        oozieFE.setEmptyStringAllowed(true);
        addField(oozieFE);

        // The path of last job
        // StringFieldEditor pathFE = new StringFieldEditor(ITalendCorePrefConstants.OOZIE_SCHEDULER_PATH,
        // Messages.getString("OoziePreferencePage.path"), getFieldEditorParent());
        // pathFE.setEmptyStringAllowed(true);
        // addField(pathFE);

        IPreferenceStore ps = getPreferenceStore();
        String nameNodeEPValue = ps.getString(ITalendCorePrefConstants.OOZIE_SHCEDULER_NAME_NODE_ENDPOINT);
        nameNodeFE.setStringValue(nameNodeEPValue);

        String jobTrackerEPValue = ps.getString(ITalendCorePrefConstants.OOZIE_SHCEDULER_JOB_TRACKER_ENDPOINT);
        jobTrackerFE.setStringValue(jobTrackerEPValue);

        String oozieEPValue = ps.getString(ITalendCorePrefConstants.OOZIE_SHCEDULER_OOZIE_ENDPOINT);
        oozieFE.setStringValue(oozieEPValue);

        // String pathValue = ps.getString(ITalendCorePrefConstants.OOZIE_SCHEDULER_PATH);
        // pathFE.setStringValue(pathValue);

        String userNameValue = ps.getString(ITalendCorePrefConstants.OOZIE_SCHEDULER_USER_NAME);
        userNameFE.setStringValue(userNameValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
    }
}
