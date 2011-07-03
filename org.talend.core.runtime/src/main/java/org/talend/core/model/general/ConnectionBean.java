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
package org.talend.core.model.general;

import java.util.HashMap;
import java.util.Map;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.runtime.i18n.Messages;
import org.talend.repository.model.RepositoryConstants;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 * 
 */
public class ConnectionBean implements Cloneable {

    private static final String DYN_FIELDS_SEPARATOR = "="; //$NON-NLS-1$

    private static final String FIELDS_SEPARATOR = "#"; //$NON-NLS-1$

    private String repositoryId;

    private String name;

    private String description;

    private String user;

    private String password;

    private Map<String, String> dynamicFields = new HashMap<String, String>();

    private boolean complete;

    private String workSpace;

    /**
     * DOC smallet ConnectionBean constructor comment.
     */
    public ConnectionBean() {
        super();
    }

    public static ConnectionBean getDefaultConnectionBean() {
        ConnectionBean newConnection = new ConnectionBean();
        newConnection.setName(Messages.getString("ConnectionBean.Local")); //$NON-NLS-1$
        newConnection.setDescription(Messages.getString("ConnectionBean.DefaultConnection")); //$NON-NLS-1$
        newConnection.setRepositoryId(RepositoryConstants.REPOSITORY_LOCAL_ID);
        newConnection.setPassword(""); //$NON-NLS-1$
        // newConnection.setUser("your@userName.here"); //$NON-NLS-1$
        return newConnection;
    }

    public String getRepositoryId() {
        return this.repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    /**
     * Getter for description.
     * 
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the description.
     * 
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for name.
     * 
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for password.
     * 
     * @return the password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets the password.
     * 
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Getter for user.
     * 
     * @return the user
     */
    public String getUser() {
        return this.user;
    }

    /**
     * Sets the user.
     * 
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Getter for workSpace.
     * 
     * @return the workSpace
     */
    public String getWorkSpace() {
        if (this.workSpace == null) {
            return ""; //$NON-NLS-1$
        }
        return this.workSpace;
    }

    /**
     * Sets the workSpace.
     * 
     * @param workSpace the workSpace to set
     */
    public void setWorkSpace(String workSpace) {
        this.workSpace = workSpace;
    }

    public Map<String, String> getDynamicFields() {
        return this.dynamicFields;
    }

    public void setDynamicFields(Map<String, String> dynamicFields) {
        this.dynamicFields = dynamicFields;
    }

    public boolean isComplete() {
        return this.complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    @Override
    public ConnectionBean clone() throws CloneNotSupportedException {
        ConnectionBean object = (ConnectionBean) super.clone();
        object.setDynamicFields(new HashMap<String, String>(this.dynamicFields));
        return object;
    }

    @Override
    public String toString() {
        StringBuffer string = new StringBuffer("Repository:" + getRepositoryId() + ", Name:" + getName() //$NON-NLS-1$ //$NON-NLS-2$
                + ", Desription:" + getDescription() + ", User:" + getUser() + ", Password:" + getPassword() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + ", WorkSpace:" + getWorkSpace() + ", Complete:" + isComplete()); //$NON-NLS-1$//$NON-NLS-1$ //$NON-NLS-2$
        string.append(", Dyn:").append(dynamicFields); //$NON-NLS-1$
        return string.toString();
    }

    public String readToString() {
        StringBuffer fields = new StringBuffer(256);
        fields.append(assertValue(getRepositoryId()));
        fields.append(FIELDS_SEPARATOR);
        fields.append(assertValue(getName()));
        fields.append(FIELDS_SEPARATOR);
        fields.append(assertValue(getDescription()));
        fields.append(FIELDS_SEPARATOR);
        fields.append(assertValue(getUser()));
        fields.append(FIELDS_SEPARATOR);
        fields.append(assertValue(getPassword()));
        fields.append(FIELDS_SEPARATOR);
        fields.append(assertValue(getWorkSpace()));
        fields.append(FIELDS_SEPARATOR);
        fields.append(isComplete());

        if (dynamicFields.size() > 0) {
            for (String current : dynamicFields.keySet()) {
                fields.append(FIELDS_SEPARATOR);
                fields.append(current + DYN_FIELDS_SEPARATOR + dynamicFields.get(current));
            }
        }

        return fields.toString();
    }

    public static ConnectionBean writeFromString(String s) {
        ConnectionBean toReturn = new ConnectionBean();
        try {
            String[] st = s.split(FIELDS_SEPARATOR, -1);
            int i = 0;
            toReturn.setRepositoryId(st[i++]);
            toReturn.setName(st[i++]);
            toReturn.setDescription(st[i++]);
            toReturn.setUser(st[i++]);
            toReturn.setPassword(st[i++]);
            toReturn.setWorkSpace(st[i++]);
            toReturn.setComplete(new Boolean(st[i++]));
            while (i < st.length) {
                String[] st2 = st[i++].split(DYN_FIELDS_SEPARATOR, -1);
                toReturn.getDynamicFields().put(st2[0], st2[1]);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
        return toReturn;
    }

    // public static void main(String[] args) {
    // ConnectionBean tt = new ConnectionBean();
    // tt.setName("tagada");
    // System.out.println(tt);
    // tt.setDescription("MyDesc");
    // tt.setUser("smallet@talend.com");
    // tt.setPassword("");
    // tt.getDynamicFields().put("DbLogin", "root");
    // tt.getDynamicFields().put("DbPassword", "toor");
    // String test2 = tt.readToString();
    // ConnectionBean bean2 = writeFromString(test2);
    // System.out.println(bean2 + " (" + test2 + ")");
    // }

    /**
     * 
     * DOC ggu Comment method "assertValue".
     */
    private String assertValue(final String value) {
        if (value == null) {
            return ""; //$NON-NLS-1$
        }
        return value;

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ConnectionBean)) {
            return false;
        }

        return this.readToString().equals(((ConnectionBean) obj).readToString());
    }

}
