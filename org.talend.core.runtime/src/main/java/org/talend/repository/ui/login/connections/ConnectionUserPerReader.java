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
package org.talend.repository.ui.login.connections;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.adaptor.LocationManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.general.ConnectionBean;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class ConnectionUserPerReader {

    private static ConnectionUserPerReader con = new ConnectionUserPerReader();

    private String perfileName = "connection_user.properties"; //$NON-NLS-1$

    public static final String CONNECTION_REGISTFAILTIMES = "connection.registFailTimes"; //$NON-NLS-1$ 

    private String path = null;

    private File perfile = null;

    private Properties proper = null;

    private boolean isRead;

    private ConnectionUserPerReader() {
        proper = new EncryptedProperties();
        isRead = false;
        // String tmp = LocationManager.getConfigurationLocation().getURL().getPath();
        // String s = new Path(LocationManager.getConfigurationLocation().getURL().getPath()).toFile().getPath();
        //        path = tmp.substring(tmp.indexOf("/") + 1, tmp.length());//$NON-NLS-1$
        path = new Path(LocationManager.getConfigurationLocation().getURL().getPath()).toFile().getAbsolutePath();
        String tmp = String.valueOf(path.charAt(path.length() - 1));
        if (!tmp.equals(File.separator)) {
            perfile = new File(path + File.separator + perfileName);
        } else {
            perfile = new File(path + perfileName);
        }
    }

    public static ConnectionUserPerReader getInstance() {
        synchronized (con) {
            if (con == null)
                con = new ConnectionUserPerReader();
            return con;
        }
    }

    public List<ConnectionBean> forceReadConnections() {
        isRead = false;
        return readConnections();
    }

    /**
     * connetion.users=user1|user2|user3|user4 user1=local#Local#Default connection####false
     * 
     * 
     * DOC teileizeget Comment method "readConnections".
     * 
     * @return
     * @throws DocumentException
     */
    public List<ConnectionBean> readConnections() {
        if (!isRead)
            this.readProperties();
        String userString = proper.getProperty("connection.users"); //$NON-NLS-1$
        if (userString == null) {
            userString = "";//$NON-NLS-1$
            return new ArrayList<ConnectionBean>(0);
        } else {
            String[] users = userString.split("\\|");//$NON-NLS-1$
            List<ConnectionBean> toReturn = new ArrayList<ConnectionBean>(users.length);
            for (String usr : users) {
                ConnectionBean conBean = ConnectionBean.writeFromString(proper.getProperty(usr));
                toReturn.add(conBean);
            }
            return toReturn;
        }

    }

    private ConnectionUserPerReader readProperties() {
        if (!isHaveUserPer())
            createPropertyFile();
        try {
            proper.load(new FileInputStream(perfile));
            isRead = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String readLastConncetion() {
        if (!isRead)
            this.readProperties();
        String tmp = proper.getProperty("connection.lastConnection"); //$NON-NLS-1$
        if (tmp == null) {
            tmp = "";//$NON-NLS-1$
        }
        return tmp;
    }

    public String readLastWorkSpace() {
        if (!isRead)
            this.readProperties();
        String workSpace = proper.getProperty("connection.lastWorkSpace"); //$NON-NLS-1$

        if (workSpace == null) {
            return "";//$NON-NLS-1$
        }
        return workSpace;
    }

    // public boolean haveWorkSpace() {
    // return proper.containsKey("connection.lastWorkSpace");
    // }

    public void saveConnections(List<ConnectionBean> cons) {
        if (!isHaveUserPer())
            createPropertyFile();
        if (!isRead)
            this.readProperties();
        StringBuilder sb = new StringBuilder();
        int i = 0;
        if (cons == null || cons.size() == 0) {
            proper.remove("connection.users");//$NON-NLS-1$
        } else {
            for (ConnectionBean currentConnection : cons) {
                String userName = currentConnection.getName();

                if (i != 0 && userName != null) {
                    sb.append("|");//$NON-NLS-1$
                }
                if (userName != null)
                    sb.append(userName);
                proper.setProperty(userName, currentConnection.readToString());
                i++;
            }
            proper.setProperty("connection.users", sb.toString());//$NON-NLS-1$
        }
        try {

            FileOutputStream out = new FileOutputStream(perfile);
            proper.store(out, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveLastConnectionBean(ConnectionBean bean) {
        if (!isHaveUserPer())
            createPropertyFile();
        if (!isRead)
            this.readProperties();
        if (bean == null) {
            proper.remove("connection.lastConnection"); //$NON-NLS-1$
            proper.remove("connection.lastWorkSpace"); //$NON-NLS-1$
        } else {
            String userName = bean.getName();
            String workSpace = bean.getWorkSpace();
            if (!"".equals(userName) && userName != null) { //$NON-NLS-1$
                proper.setProperty("connection.lastConnection", userName);//$NON-NLS-1$
            } else {
                proper.remove("connection.lastConnection"); //$NON-NLS-1$
            }
            if (!"".equals(workSpace) && workSpace != null) { //$NON-NLS-1$
                proper.setProperty("connection.lastWorkSpace", workSpace);//$NON-NLS-1$
            } else {
                proper.remove("connection.lastWorkSpace"); //$NON-NLS-1$
            }

        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(perfile);
            proper.store(out, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void createPropertyFile() {
        File fatherFloder = new File(path);
        if (!fatherFloder.exists()) {
            fatherFloder.mkdirs();
        }
        try {
            if (!perfile.exists()) {
                perfile.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean isHaveUserPer() {
        return perfile.exists();
    }

    public String readRegistration() {
        if (!isRead)
            this.readProperties();
        String tmp = proper.getProperty("connection.readRegistration"); //$NON-NLS-1$
        if (tmp == null) {
            tmp = "";//$NON-NLS-1$
        }
        return tmp;
    }

    public String readRegistrationDone() {
        if (!isRead)
            this.readProperties();
        String tmp = proper.getProperty("connection.readRegistrationDone"); //$NON-NLS-1$
        if (tmp == null) {
            tmp = "";//$NON-NLS-1$
        }
        return tmp;
    }

    public String readLicenseManagement() {
        if (!isRead)
            this.readProperties();
        String tmp = proper.getProperty("connection.licenseManagement"); //$NON-NLS-1$
        if (tmp == null) {
            tmp = "";//$NON-NLS-1$
        }
        return tmp;
    }

    /**
     * DOC ycbai Comment method "readRegistFailTimes".
     * 
     * @return
     */
    public String readRegistFailTimes() {
        if (!isRead)
            this.readProperties();
        return StringUtils.trimToEmpty(proper.getProperty(CONNECTION_REGISTFAILTIMES));
    }

    public void saveRegistoryBean() {
        if (!isHaveUserPer())
            createPropertyFile();
        if (!isRead)
            this.readProperties();
        IPreferenceStore prefStore = PlatformUI.getPreferenceStore();
        proper.setProperty("connection.readRegistration", Integer.toString(prefStore.getInt("REGISTRATION_TRIES"))); //$NON-NLS-1$ //$NON-NLS-2$
        proper.setProperty("connection.readRegistrationDone", Integer.toString(prefStore.getInt("REGISTRATION_DONE"))); //$NON-NLS-1$ //$NON-NLS-2$
        try {

            FileOutputStream out = new FileOutputStream(perfile);
            proper.store(out, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * DOC ycbai Comment method "saveRegistoryBean".
     * 
     * @param propertyMap
     */
    public void saveRegistoryBean(Map<String, String> propertyMap) {
        if (propertyMap == null)
            return;
        if (!isHaveUserPer())
            createPropertyFile();
        if (!isRead)
            this.readProperties();
        Iterator<Entry<String, String>> iter = propertyMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, String> entry = iter.next();
            String key = entry.getKey();
            String val = entry.getValue();
            proper.setProperty(key, val);
        }
        try {
            FileOutputStream out = new FileOutputStream(perfile);
            proper.store(out, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveLiscenseManagement() {
        if (!isHaveUserPer())
            createPropertyFile();
        if (!isRead)
            this.readProperties();
        IPreferenceStore prefStore = PlatformUI.getPreferenceStore();
        proper.setProperty("connection.licenseManagement", Integer.toString(prefStore.getInt("LICENSE_VALIDATION_DONE")));
        try {

            FileOutputStream out = new FileOutputStream(perfile);
            proper.store(out, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
