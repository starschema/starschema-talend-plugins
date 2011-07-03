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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.database.EDatabase4DriverClassName;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.model.metadata.builder.database.DriverShim;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;

/**
 * DOC hyWang class global comment. Detailled comment
 */
public class DbInfo {

    private String username = null;

    private String pwd = null;

    private String url = null;

    private String driverClassName = null;

    private String driverJarPath = null;

    private String dbType = null;

    private String dbVersion = null;

    private String trueDBTypeForJDBC = null;

    private String additionalParams = null;

    private Connection conn = null;

    public DbInfo(String dbType, String username, String pwd, String dbVersion, String url, String additionalParams) {
        this.dbType = dbType;
        this.username = username;
        this.pwd = pwd;
        this.dbVersion = dbVersion;
        this.url = url;
        this.additionalParams = additionalParams;
        generateDriverName();
        genarateDriverJarPath();
        getConnFromNode();
        this.trueDBTypeForJDBC = getTrueDBType(driverClassName, driverJarPath);
    }

    public DbInfo(String dbType, String username, String pwd, String dbVersion, String url, String driverJarPath,
            String additionalParams) {
        this.dbType = dbType;
        this.username = username;
        this.pwd = pwd;
        this.dbVersion = dbVersion;
        this.url = url;
        this.driverJarPath = driverJarPath;
        generateDriverName();
        getConnFromNode();
        genarateDriverJarPath();
        this.trueDBTypeForJDBC = getTrueDBType(driverClassName, driverJarPath);
    }

    public String getTrueDBTypeForJDBC() {
        return this.trueDBTypeForJDBC;
    }

    // hywang add constructor for bug 9594
    public DbInfo(String dbType, String username, String pwd, String dbVersion, String url, String driverClassName,
            String driverJarPath, String additionalParams) {
        this.dbType = dbType;
        this.username = username;
        this.pwd = pwd;
        this.dbVersion = dbVersion;
        this.url = url;
        this.driverClassName = driverClassName;
        this.driverJarPath = driverJarPath;
        this.trueDBTypeForJDBC = dbType;
        this.additionalParams = additionalParams;
        getConnFromNode();
    }

    public String getUrl() {
        return this.url;
    }

    public String getUsername() {
        return this.username;
    }

    public String getDriverClassName() {
        return this.driverClassName;
    }

    public String getDriverJarPath() {
        return this.driverJarPath;
    }

    public String getDbType() {
        return this.dbType;
    }

    public String getDbVersion() {
        return this.dbVersion;
    }

    public Connection getConn() {
        return this.conn;
    }

    public String getPwd() {
        return this.pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public void setDriverJarPath(String driverJarPath) {
        this.driverJarPath = driverJarPath;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public void setDbVersion(String dbVersion) {
        this.dbVersion = dbVersion;
    }

    private void getConnFromNode() {
        DriverShim wapperDriver = null;
        try {
            List list = null;
            if (dbType.equals(EDatabaseTypeName.GENERAL_JDBC.getDisplayName())) {
                list = ExtractMetaDataUtils.connect(trueDBTypeForJDBC, url, username, pwd, driverClassName, driverJarPath,
                        dbVersion, additionalParams);
            } else {
                // driverJarPath set to null,to reget driverJarPath
                driverJarPath = ""; //$NON-NLS-N$
                list = ExtractMetaDataUtils.connect(dbType, url, username, pwd, driverClassName, driverJarPath, dbVersion,
                        additionalParams); //$NON-NLS-N$
            }
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i) instanceof Connection) {
                        conn = (Connection) list.get(i);
                    }
                    if (list.get(i) instanceof DriverShim) {
                        wapperDriver = (DriverShim) list.get(i);
                    }
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        } finally {
            // bug 9162
            if ((driverClassName.equals(EDatabase4DriverClassName.JAVADB_EMBEDED.getDriverClass())
                    || dbType.equals(EDatabaseTypeName.JAVADB_EMBEDED.getDisplayName())
                    || dbType.equals(EDatabaseTypeName.JAVADB_DERBYCLIENT.getDisplayName())
                    || dbType.equals(EDatabaseTypeName.JAVADB_JCCJDBC.getDisplayName())
                    || dbType.equals(EDatabaseTypeName.HSQLDB_IN_PROGRESS.getDisplayName()) || dbType
                    .equals(EDatabaseTypeName.GENERAL_JDBC.getDisplayName())) && wapperDriver != null) {
                try {
                    wapperDriver.connect("jdbc:derby:;shutdown=true", null); //$NON-NLS-1$
                } catch (SQLException e) {
                    // exception of shutdown success. no need to catch.
                }
            }
        }
    }

    private void generateDriverName() {
        driverClassName = ExtractMetaDataUtils.getDriverClassByDbType(dbType);
    }

    private void genarateDriverJarPath() {
        // modified by wzhang. when driver has more than one jar.
        List<String> drivers = EDatabaseVersion4Drivers.getDrivers(dbType, dbVersion);
        if (drivers.size() == 1) {
            String driverName = EDatabaseVersion4Drivers.getDriversStr(dbType, dbVersion);
            if (driverName != null) {
                driverJarPath = ExtractMetaDataUtils.getJavaLibPath() + driverName;
            }
        } else {
            driverJarPath = null;
        }
    }

    private String getTrueDBType(String className, String driverJar) {
        return ExtractMetaDataUtils.getDbTypeByClassNameAndDriverJar(className, driverJar);
    }

    private String getTrueDBType(String className) {
        return ExtractMetaDataUtils.getDbTypeByClassName(className);
    }

}
