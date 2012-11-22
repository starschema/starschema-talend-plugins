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
package org.talend.core.database.conn.version;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.DatabaseConnConstants;

/**
 * cli class global comment. Detailled comment
 */
public enum EDatabaseVersion4Drivers {
    // access
    ACCESS_2003(new DbVersion4Drivers(EDatabaseTypeName.ACCESS, "Access 2003", "Access_2003")), //$NON-NLS-1$ //$NON-NLS-2$
    ACCESS_2007(new DbVersion4Drivers(EDatabaseTypeName.ACCESS, "Access 2007", "Access_2007")), //$NON-NLS-1$ //$NON-NLS-2$
    // oracle
    ORACLE_11(new DbVersion4DriversForOracle11(new EDatabaseTypeName[] { EDatabaseTypeName.ORACLEFORSID,
            EDatabaseTypeName.ORACLESN, EDatabaseTypeName.ORACLE_OCI, EDatabaseTypeName.ORACLE_RAC },
            "Oracle 11", "ORACLE_11", new String[] { DbVersion4DriversForOracle11.DRIVER_1_5, //$NON-NLS-1$ //$NON-NLS-2$
                    DbVersion4DriversForOracle11.DRIVER_1_6 })),
    ORACLE_10(new DbVersion4Drivers(new EDatabaseTypeName[] { EDatabaseTypeName.ORACLEFORSID, EDatabaseTypeName.ORACLESN,
            EDatabaseTypeName.ORACLE_OCI, EDatabaseTypeName.ORACLE_RAC }, "Oracle 10", "ORACLE_10", "ojdbc14-10g.jar")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    ORACLE_9(new DbVersion4Drivers(new EDatabaseTypeName[] { EDatabaseTypeName.ORACLEFORSID, EDatabaseTypeName.ORACLESN,
            EDatabaseTypeName.ORACLE_OCI, EDatabaseTypeName.ORACLE_RAC }, "Oracle 9", "ORACLE_9", "ojdbc14-9i.jar")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    ORACLE_8(new DbVersion4Drivers(new EDatabaseTypeName[] { EDatabaseTypeName.ORACLEFORSID, EDatabaseTypeName.ORACLESN,
            EDatabaseTypeName.ORACLE_OCI, EDatabaseTypeName.ORACLE_RAC }, "Oracle 8", "ORACLE_8", "ojdbc12-8i.jar")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    // AS400
    AS400_V5R4_V6R1(new DbVersion4Drivers(EDatabaseTypeName.AS400, "V5R4 to V6R1", "V5R4 to V6R1", "jt400_V5R3.jar")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    AS400_V5R2_V5R4(new DbVersion4Drivers(EDatabaseTypeName.AS400, "V5R2 to V5R4", "V5R2 to V5R4", "jt400_V5R2.jar")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    //
    INTERBASE(new DbVersion4Drivers(EDatabaseTypeName.INTERBASE, "interclient.jar")), //$NON-NLS-1$

    //
    HSQLDB(new DbVersion4Drivers(EDatabaseTypeName.HSQLDB, "hsqldb.jar")), //$NON-NLS-1$
    HSQLDB_IN_PROGRESS(new DbVersion4Drivers(EDatabaseTypeName.HSQLDB_IN_PROGRESS, "hsqldb.jar")), //$NON-NLS-1$
    HSQLDB_SERVER(new DbVersion4Drivers(EDatabaseTypeName.HSQLDB_SERVER, "hsqldb.jar")), //$NON-NLS-1$
    HSQLDB_WEBSERVER(new DbVersion4Drivers(EDatabaseTypeName.HSQLDB_WEBSERVER, "hsqldb.jar")), //$NON-NLS-1$

    //
    JAVADB_EMBEDED(new DbVersion4Drivers(EDatabaseTypeName.JAVADB_EMBEDED, "derby.jar")), //$NON-NLS-1$
    SQLITE(new DbVersion4Drivers(EDatabaseTypeName.SQLITE, "sqlitejdbc-v056.jar")), //$NON-NLS-1$
    FIREBIRD(new DbVersion4Drivers(EDatabaseTypeName.FIREBIRD, "jaybird-full-2.1.1.jar")), //$NON-NLS-1$
    TERADATA(new DbVersion4Drivers(EDatabaseTypeName.TERADATA,
            new String[] { "terajdbc4.jar", "tdgssconfig.jar", "tdgssjava.jar" })), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    JAVADB_DERBYCLIENT(new DbVersion4Drivers(EDatabaseTypeName.JAVADB_DERBYCLIENT, "derbyclient.jar")), //$NON-NLS-1$
    NETEZZA(new DbVersion4Drivers(EDatabaseTypeName.NETEZZA, "nzjdbc.jar")), //$NON-NLS-1$
    INFORMIX(new DbVersion4Drivers(EDatabaseTypeName.INFORMIX, "ifxjdbc.jar")), //$NON-NLS-1$

    SAS_9_1(new DbVersion4Drivers(EDatabaseTypeName.SAS, "SAS 9.1", "SAS_9.1", new String[] { "sas.core.jar", //$NON-NLS-1$
            "sas.intrnet.javatools.jar", "sas.svc.connection.jar" })), //$NON-NLS-1$ //$NON-NLS-2$
    SAS_9_2(new DbVersion4Drivers(EDatabaseTypeName.SAS,
            "SAS 9.2", "SAS_9.2", new String[] { "sas.core.jar", "sas.security.sspi.jar", "sas.svc.connection.jar" })), //$NON-NLS-1$ //$NON-NLS-2$
    // MYSQL, add for 9594
    MYSQL_5(new DbVersion4Drivers(EDatabaseTypeName.MYSQL, "MySQL 5", "MYSQL_5", "mysql-connector-java-5.1.0-bin.jar")), //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$
    MYSQL_4(new DbVersion4Drivers(EDatabaseTypeName.MYSQL, "MySQL 4", "MYSQL_4", "mysql-connector-java-3.1.14-bin.jar")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    // add for 9594
    MSSQL(new DbVersion4Drivers(EDatabaseTypeName.MSSQL, "jtds-1.2.5.jar")), //$NON-NLS-1$
    MSSQL2005(new DbVersion4Drivers(EDatabaseTypeName.MSSQL05_08,
            "MSSQL2005", "MSSQL_2005", new String[] { "sqljdbc.jar", "sqljdbc4.jar" })), //$NON-NLS-1$
    MSSQL2008(new DbVersion4Drivers(EDatabaseTypeName.MSSQL05_08,
            "MSSQL2008", "MSSQL_2008", new String[] { "sqljdbc.jar", "sqljdbc4.jar" })), //$NON-NLS-1$

    VERTICA_5(new DbVersion4Drivers(EDatabaseTypeName.VERTICA, "VERTICA 5", "VERTICA_5", "vertica_4.1.14_jdk_5.jar")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    VERTICA_4_1(new DbVersion4Drivers(EDatabaseTypeName.VERTICA, "VERTICA 4.1", "VERTICA_4_1", "vertica_4.1.7_jdk_5.jar")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    VERTICA_4(new DbVersion4Drivers(EDatabaseTypeName.VERTICA, "VERTICA 4", "VERTICA_4", "vertica_4.0_jdk_5.jar")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    VERTICA_3(new DbVersion4Drivers(EDatabaseTypeName.VERTICA, "VERTICA 3", "VERTICA_3", "vertica_3.0_jdk_5.jar")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    VERTICA_3_5(new DbVersion4Drivers(EDatabaseTypeName.VERTICA, "VERTICA 3.5", "VERTICA_3.5", "vertica_3.5_jdk_5.jar")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    // for bug 0017930
    GREENPLUM(new DbVersion4Drivers(EDatabaseTypeName.GREENPLUM, "postgresql-8.3-603.jdbc3.jar")), //$NON-NLS-1$ 
    PSQL(new DbVersion4Drivers(EDatabaseTypeName.PSQL, "postgresql-8.3-603.jdbc3.jar")), //$NON-NLS-1$ 
    PLUSPSQL(new DbVersion4Drivers(EDatabaseTypeName.PLUSPSQL, "postgresql-8.3-603.jdbc3.jar")), //$NON-NLS-1$
    IBMDB2(new DbVersion4Drivers(EDatabaseTypeName.IBMDB2, new String[] { "db2jcc.jar", "db2jcc_license_cu.jar", //$NON-NLS-1$ //$NON-NLS-2$
            "db2jcc_license_cisuz.jar" })), //$NON-NLS-1$ 
    IBMDB2ZOS(new DbVersion4Drivers(EDatabaseTypeName.IBMDB2ZOS, new String[] { "db2jcc.jar", "db2jcc_license_cu.jar", //$NON-NLS-1$ //$NON-NLS-2$
            "db2jcc_license_cisuz.jar" })), //$NON-NLS-1$ 
    SYBASEASE(new DbVersion4Drivers(EDatabaseTypeName.SYBASEASE, "jconn3.jar")), //$NON-NLS-1$ 
    SYBASEIQ(new DbVersion4Drivers(EDatabaseTypeName.SYBASEIQ, "jconn3.jar")), //$NON-NLS-1$ 
    // for bug 0013127
    PARACCEL(new DbVersion4Drivers(EDatabaseTypeName.PARACCEL, "paraccel-jdbc.jar")), //$NON-NLS-1$
    VECTORWISE(new DbVersion4Drivers(EDatabaseTypeName.VECTORWISE, "iijdbc.jar")), //$NON-NLS-1$

    EXASOL(new DbVersion4Drivers(EDatabaseTypeName.EXASOL, "jdbc14.jar")), //$NON-NLS-1$ 
    MAXDB(new DbVersion4Drivers(EDatabaseTypeName.MAXDB, "sapdbc.jar")), //$NON-NLS-1$

    INGRES(new DbVersion4Drivers(EDatabaseTypeName.INGRES, "iijdbc.jar")), //$NON-NLS-1$

    HIVE(new DbVersion4Drivers(EDatabaseTypeName.HIVE, new String[] { "hive-jdbc-0.7.1.jar", "hive-metastore-0.7.1.jar",
            "hive-exec-0.7.1.jar", "hive-service-0.7.1.jar", "libfb303.jar", "hadoop-core-0.20.203.0.jar",
            "commons-logging-1.0.4.jar", "log4j-1.2.15.jar", "slf4j-api-1.6.1.jar", "slf4j-log4j12-1.6.1.jar" })),

    HBASE(new DbVersion4Drivers(EDatabaseTypeName.HBASE, new String[] { "hadoop-core-0.20.2-cdh3u0.jar",
            "hbase-0.90.1-cdh3u0.jar", "zookeeper-3.3.3-cdh3u0.jar", "commons-lang3-3.0.jar", "commons-logging-1.1.1.jar",
            "log4j-1.2.16.jar" }));

    private DbVersion4Drivers dbVersionBean;

    EDatabaseVersion4Drivers(DbVersion4Drivers dbVersionBean) {
        this.dbVersionBean = dbVersionBean;
    }

    public String getVersionDisplay() {
        return this.dbVersionBean.getVersionDisplayName();
    }

    public String getVersionValue() {
        return this.dbVersionBean.getVersionValue();
    }

    public Set<String> getProviderDrivers() {
        return this.dbVersionBean.getDrivers();
    }

    private List<EDatabaseTypeName> getSupportDbTypes() {
        return this.dbVersionBean.getDbTypes();
    }

    public boolean supportDatabase(EDatabaseTypeName dbType) {
        if (dbType != null) {
            return getSupportDbTypes().contains(dbType);
        }
        return false;
    }

    public boolean supportDatabase(String dbType) {
        if (dbType != null) {
            for (EDatabaseTypeName type : getSupportDbTypes()) {
                if (type.getXmlName().equalsIgnoreCase(dbType) || type.getDisplayName().equalsIgnoreCase(dbType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static EDatabaseVersion4Drivers indexOfByVersionDisplay(String displayName) {
        return indexOf(displayName, true);
    }

    public static EDatabaseVersion4Drivers indexOfByVersion(String value) {
        return indexOf(value, false);
    }

    private static EDatabaseVersion4Drivers indexOf(String name, boolean display) {
        if (name != null) {
            for (EDatabaseVersion4Drivers version : EDatabaseVersion4Drivers.values()) {
                if (display) {
                    if (name.equalsIgnoreCase(version.getVersionDisplay())) {
                        return version;
                    }
                } else {
                    if (name.equalsIgnoreCase(version.getVersionValue())) {
                        return version;
                    }
                }
            }
        }
        return null;
    }

    public static List<EDatabaseVersion4Drivers> indexOfByDbType(String dbType) {
        List<EDatabaseVersion4Drivers> v4dList = new ArrayList<EDatabaseVersion4Drivers>();
        if (dbType != null) {
            for (EDatabaseVersion4Drivers v4d : EDatabaseVersion4Drivers.values()) {
                if (v4d.supportDatabase(dbType)) {
                    v4dList.add(v4d);
                }
            }
        }
        return v4dList;
    }

    public static String getDbVersionName(final EDatabaseTypeName dbType, final String driverName) {
        if (dbType != null) {
            return getDbVersionName(dbType.getXmlName(), driverName);
        } else {
            return getDbVersionName((String) null, driverName);
        }
    }

    public static String getDbVersionName(final String dbType, final String driverName) {
        for (EDatabaseVersion4Drivers v4d : EDatabaseVersion4Drivers.values()) {
            if (driverName != null && dbType != null) {
                if (v4d.getProviderDrivers().contains(driverName) && v4d.supportDatabase(dbType)) {
                    return v4d.getVersionValue();
                }
            } else if (driverName != null && dbType == null) {
                if (v4d.getProviderDrivers().contains(driverName)) {
                    return v4d.getVersionValue();
                }
            } else if (driverName == null && dbType != null) {
                if (v4d.supportDatabase(dbType)) {
                    return v4d.getVersionValue();
                }
            }
        }
        return null;
    }

    public static String getDriversStr(final String dbType, final String version) {
        List<String> drivers = getDrivers(dbType, version);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < drivers.size(); i++) {
            String str = drivers.get(i);
            if (str != null && !DatabaseConnConstants.EMPTY.equals(str.trim())) {
                if (i < drivers.size() - 1) {
                    sb.append(str);
                    sb.append(';');
                } else {
                    sb.append(str);
                }
            }
        }
        return sb.toString();
    }

    public static List<String> getDrivers(final String dbType, final String version) {
        List<String> drivers = new ArrayList<String>();
        for (EDatabaseVersion4Drivers v4d : EDatabaseVersion4Drivers.values()) {
            if (dbType != null) {
                if (v4d.supportDatabase(dbType)) {
                    if (version == null || v4d.getVersionValue() == null || version.equals("")) { // add all for this db
                                                                                                  // type
                        drivers.addAll(v4d.getProviderDrivers());
                    } else
                    // check both db type and version value.
                    if (version.equalsIgnoreCase(v4d.getVersionValue())) {
                        drivers.addAll(v4d.getProviderDrivers());
                    }
                }
            } else {
                // only check the version value
                if (version != null && version.equalsIgnoreCase(v4d.getVersionValue())) {
                    drivers.addAll(v4d.getProviderDrivers());
                }
            }
        }
        return drivers;
    }

    public static boolean containTypeAndVersion(final String dbType, final String version) {
        if (version == null) {
            return false;
        }
        for (EDatabaseVersion4Drivers v4d : EDatabaseVersion4Drivers.values()) {
            if (v4d.supportDatabase(dbType)) {
                if (version.equalsIgnoreCase(v4d.getVersionValue())) {
                    return true;
                }
            }
        }
        return false;
    }
}
