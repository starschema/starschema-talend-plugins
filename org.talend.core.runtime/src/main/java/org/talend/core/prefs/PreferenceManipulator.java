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
package org.talend.core.prefs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.general.ConnectionBean;

/**
 * Used to store connections / users for the login dialog <br/>
 * 
 * $Id: PreferenceManipulator.java 44184 2010-06-17 04:01:35Z cli $
 * 
 */
public final class PreferenceManipulator implements ITalendCorePrefConstants {

    private static final String PREF_DELIMITER = "|"; //$NON-NLS-1$

    /** The preference store manipulated. */
    private IPreferenceStore store;

    /**
     * Constructs a new PreferenceManipulator.
     * 
     * @param store The preference store manipulated.
     * @deprecated The preferences are only used to store specific connections, so will be forced to a specific
     * preference store, no matter the parameter
     */
    public PreferenceManipulator(IPreferenceStore store) {
        super();

        this.store = PlatformUI.getPreferenceStore();
    }

    /**
     * Constructs a new PreferenceManipulator.
     * 
     * @param store The preference store manipulated.
     */
    public PreferenceManipulator() {
        super();

        this.store = PlatformUI.getPreferenceStore();
    }

    /**
     * Read a string array in the preference store.
     * 
     * @param prefName Name of the preference to be read.
     * @return an array of strings.
     */
    private String[] readStringArray(final String prefName) {
        List<String> strings = readStringList(prefName);

        String[] array = new String[strings.size()];
        array = strings.toArray(array);
        return array;
    }

    private List<String> readStringList(final String prefName) {
        String prefs = store.getString(prefName);
        List<String> strings = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(prefs, PREF_DELIMITER);
        while (st.hasMoreTokens()) {
            strings.add(st.nextToken());
        }

        return strings;
    }

    /**
     * Save a string array in the preference store.
     * 
     * @param prefArray Preferences to be saved.
     * @param prefName Name of the preference to be saved.
     */
    private void saveStringArray(final String[] prefArray, String prefName) {
        StringBuffer prefs = new StringBuffer(256);
        for (int i = 0; i < prefArray.length; i++) {
            prefs.append(prefArray[i]);
            if (i < prefArray.length - 1) {
                prefs.append(PREF_DELIMITER);
            }
        }
        store.setValue(prefName, prefs.toString());
        save();
    }

    /**
     * Add a string to an array preference.
     * 
     * @param s String to be added.
     * @param prefName Name of the preference to add.
     */
    private void addStringToArray(String s, String prefName) {
        String[] array = readStringArray(prefName);
        List<String> l = new ArrayList<String>();
        l.addAll(Arrays.asList(array));

        if (!l.contains(s)) {
            l.add(s);

            array = new String[l.size()];
            array = l.toArray(array);
            saveStringArray(array, prefName);
        }
    }

    public List<ConnectionBean> readConnections() {
        List<ConnectionBean> toReturn = new ArrayList<ConnectionBean>();
        for (String currentConnectionToLoad : readStringArray(CONNECTIONS)) {
            toReturn.add(ConnectionBean.writeFromString(currentConnectionToLoad));
        }
        return toReturn;
    }

    public void saveConnections(List<ConnectionBean> cons) {
        String[] prefArray = new String[cons.size()];
        int i = 0;
        for (ConnectionBean currentConnection : cons) {
            prefArray[i++] = currentConnection.readToString();
        }
        saveStringArray(prefArray, CONNECTIONS);
    }

    public void addConnection(ConnectionBean con) {
        addStringToArray(con.readToString(), CONNECTIONS);
    }

    /**
     * Read all known users in the preference store.
     * 
     * @return all known users.
     */
    public String[] readUsers() {
        return readStringArray(USERS);
    }

    /**
     * Save all known users in the preference store.
     * 
     * @param users all known users.
     */
    public void saveUsers(String[] users) {
        saveStringArray(users, USERS);
    }

    public void addUser(String user) {
        addStringToArray(user, USERS);
    }

    public String getLastConnection() {
        return store.getString(LAST_USED_CONNECTION);
    }

    public void setLastConnection(String connection) {
        store.setValue(LAST_USED_CONNECTION, connection);
        save();
    }

    public String getLastProject() {
        return store.getString(LAST_USED_PROJECT);
    }

    public void setLastProject(String project) {
        store.setValue(LAST_USED_PROJECT, project);
        save();
    }

    public String getLastSVNBranch() {
        String string = store.getString(LAST_USED_SVN_BRANCH);
        // just unified for null
        if (string != null && "".equals(string.trim())) { //$NON-NLS-1$
            return null;
        }
        return string;
    }

    public void setLastSVNBranch(String branch) {
        store.setValue(LAST_USED_SVN_BRANCH, branch);
        save();
    }

    public String getLastUser() {
        return store.getString(LAST_USED_USER);
    }

    public void setLastUser(String user) {
        store.setValue(LAST_USED_USER, user);
        save();
    }

    public List<String> readWorkspaceTasksDone() {
        return readStringList(WORKSPACE_TASKS_DONE);
    }

    public void addWorkspaceTaskDone(String task) {
        addStringToArray(task, WORKSPACE_TASKS_DONE);
    }

    public boolean getBoolean(String name) {
        return this.store.getBoolean(name);
    }

    public void setValue(String name, boolean value) {
        this.store.setValue(name, value);
    }

    /**
     * 
     * DOC ggu Comment method "save".
     */
    public void save() {
        if (store != null && store instanceof IPersistentPreferenceStore && store.needsSaving()) {
            try {
                ((IPersistentPreferenceStore) store).save();
            } catch (IOException e) {
                //
            }
        }
    }
}
