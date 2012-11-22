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
package org.talend.core.model.metadata.designerproperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.PasswordEncryptUtil;
import org.talend.core.database.EDatabase4DriverClassName;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.DatabaseConnStrUtil;
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.builder.connection.BRMSConnection;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.DelimitedFileConnection;
import org.talend.core.model.metadata.builder.connection.EbcdicConnection;
import org.talend.core.model.metadata.builder.connection.Escape;
import org.talend.core.model.metadata.builder.connection.FileExcelConnection;
import org.talend.core.model.metadata.builder.connection.HL7Connection;
import org.talend.core.model.metadata.builder.connection.LDAPSchemaConnection;
import org.talend.core.model.metadata.builder.connection.LdifFileConnection;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.connection.PositionalFileConnection;
import org.talend.core.model.metadata.builder.connection.RegexpFileConnection;
import org.talend.core.model.metadata.builder.connection.SAPConnection;
import org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection;
import org.talend.core.model.metadata.builder.connection.SchemaTarget;
import org.talend.core.model.metadata.builder.connection.WSDLSchemaConnection;
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.metadata.builder.connection.XmlXPathLoopDescriptor;
import org.talend.core.model.metadata.designerproperties.PropertyConstants.CDCTypeMode;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.repository.DragAndDropManager;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.IDragAndDropServiceHandler;
import org.talend.core.runtime.i18n.Messages;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.cwm.helper.ConnectionHelper;

/**
 * DOC wzhang class global comment. Detailled comment
 */
public class ComponentToRepositoryProperty {

    /**
     * DOC wzhang Comment method "setValue".
     * 
     * @param connection
     * @param node
     */
    public static boolean setValue(Connection connection, INode node) {

        if (connection == null || node == null) {
            return false;
        }
        if (connection instanceof DatabaseConnection) {
            setDatabaseType((DatabaseConnection) connection, node);
        }
        // impossible to use OCI in oracle
        IElementParameter elementParameter = node.getElementParameter("CONNECTION_TYPE"); //$NON-NLS-1$
        if (elementParameter != null) {
            if ("ORACLE_OCI".equals(elementParameter.getValue())) { //$NON-NLS-1$
                Shell shell = Display.getCurrent().getActiveShell();
                String title = Messages.getString("ComponentToRepositoryProperty.error"); //$NON-NLS-1$
                String message = Messages.getString("ComponentToRepositoryProperty.ImpossibleUseOCI"); //$NON-NLS-1$
                MessageDialog.openError(shell, title, message);
                return false;
            }
        }
        for (IElementParameter param : node.getElementParameters()) {
            param.getName();
            String repositoryValue = param.getRepositoryValue();
            setValue(connection, node, repositoryValue);
        }
        if (connection instanceof DatabaseConnection) {
            // add url instance ------DataStringConnection
            DatabaseConnection conn = (DatabaseConnection) connection;
            // see bug in 18011, set url and driver_jar.
            conn.setURL(DatabaseConnStrUtil.getURLString(conn));

            // see bug in feature 5998, set dbmsId.
            String repositoryType = node.getElementParameter("PROPERTY_TYPE").getRepositoryValue(); //$NON-NLS-1$
            if (repositoryType.startsWith("DATABASE") && repositoryType.contains(":")) { //$NON-NLS-1$ //$NON-NLS-2$
                String product = repositoryType.substring(repositoryType.indexOf(":") + 1); //$NON-NLS-1$
                // see bug in feature 17761.
                if (product.equals(EDatabaseTypeName.GENERAL_JDBC.getProduct())) {
                    String driverClass = getParameterValue(connection, node, "DRIVER_CLASS"); //$NON-NLS-1$
                    List<EDatabase4DriverClassName> driverClasses = EDatabase4DriverClassName.indexOfByDriverClass(driverClass);
                    if (driverClasses.size() > 0) { // use the first one
                        product = driverClasses.get(0).getDbType().getProduct();
                    } else {
                        product = EDatabaseTypeName.MYSQL.getProduct();
                    }
                }
                String mapping = MetadataTalendType.getDefaultDbmsFromProduct(product).getId();
                conn.setDbmsId(mapping);
            }
        }
        return true;
    }

    /**
     * 
     * DOC wzhang Comment method "setValue".
     * 
     * @param connection
     * @param node
     * @param repositoryValue
     */
    public static void setValue(Connection connection, INode node, String repositoryValue) {
        if (connection == null || node == null || repositoryValue == null) {
            return;
        } else if (connection instanceof XmlFileConnection) {
            setXmlFileValue((XmlFileConnection) connection, node, repositoryValue);
        } else if (connection instanceof DatabaseConnection) {
            setDatabaseValue((DatabaseConnection) connection, node, repositoryValue);
        } else if (connection instanceof EbcdicConnection) {
            setEbcdicValue((EbcdicConnection) connection, node, repositoryValue);
        } else if (connection instanceof DelimitedFileConnection) {
            setDelimitedFileValue((DelimitedFileConnection) connection, node, repositoryValue);
        } else if (connection instanceof LDAPSchemaConnection) {
            setLDAPSchemaValue((LDAPSchemaConnection) connection, node, repositoryValue);
        } else if (connection instanceof WSDLSchemaConnection) {
            setWSDLSchemaValue((WSDLSchemaConnection) connection, node, repositoryValue);
        } else if (connection instanceof LdifFileConnection) {
            setLdifFileValue((LdifFileConnection) connection, node, repositoryValue);
        } else if (connection instanceof RegexpFileConnection) {
            setRegexpFileValue((RegexpFileConnection) connection, node, repositoryValue);
        } else if (connection instanceof PositionalFileConnection) {
            setPositionalFileValue((PositionalFileConnection) connection, node, repositoryValue);
        } else if (connection instanceof FileExcelConnection) {
            setFileExcelValue((FileExcelConnection) connection, node, repositoryValue);
        } else if (connection instanceof SAPConnection) {
            setSAPValue((SAPConnection) connection, node, repositoryValue);
        } else if (connection instanceof SalesforceSchemaConnection) {
            setSalesforceSchema((SalesforceSchemaConnection) connection, node, repositoryValue);
        } else if (connection instanceof MDMConnection) {
            setMDMValue((MDMConnection) connection, node, repositoryValue);
        } else if (connection instanceof BRMSConnection) {
            setBRMSValue((BRMSConnection) connection, node, repositoryValue);
        } else if (connection instanceof HL7Connection) {
            setHL7Value((HL7Connection) connection, node, repositoryValue);
        }
        for (IDragAndDropServiceHandler handler : DragAndDropManager.getHandlers()) {
            if (handler.canHandle(connection)) {
                handler.setComponentValue(connection, node, repositoryValue);
            }
        }
    }

    /**
     * 
     * DOC wzhang Comment method "getParameterValue".
     * 
     * @param node
     * @param paramName
     * @return
     */
    private static String getParameterValue(Connection connection, INode node, String paramName) {
        String originalValue = getParameterOriginalValue(connection, node, paramName);
        if (originalValue != null) {
            return TalendQuoteUtils.removeQuotes(originalValue);
        }
        return null;
    }

    private static String getParameterOriginalValue(Connection connection, INode node, String paramName) {
        if (node != null || paramName != null) {
            IElementParameter param = node.getElementParameter(paramName);
            if (param != null) {
                Object o = param.getValue();
                if (o instanceof String || o instanceof Boolean || o instanceof Integer || o instanceof Long
                        || o instanceof Character) {
                    String value = String.valueOf(o);
                    if (isConetxtParaMode(connection, value)) {
                        value = getContextOriginalValue(connection, node, value);
                    }
                    return value;
                } else if (o instanceof List && paramName.equals("DRIVER_JAR")) {
                    List<Map<String, Object>> list = (List<Map<String, Object>>) o;
                    String userDir = System.getProperty("user.dir"); //$NON-NLS-1$
                    String pathSeparator = System.getProperty("file.separator"); //$NON-NLS-1$
                    String defaultPath = userDir + pathSeparator + "lib" + pathSeparator + "java"; //$NON-NLS-1$ //$NON-NLS-2$
                    Character comma = ';';
                    String symbol = "\\";
                    String jarspath = "";
                    for (int i = 0; i < list.size(); i++) {
                        jarspath = jarspath + defaultPath + symbol + list.get(i).get("JAR_NAME");
                        if (i < list.size() - 1) {
                            jarspath = jarspath + comma.toString();
                        }
                    }
                    return jarspath;
                }
            }
        }
        return null;
    }

    protected static String getValueFromRepositoryName(Connection connection, INode node, String repositoryName) {
        for (IElementParameter param : (List<IElementParameter>) node.getElementParameters()) {
            if (param.getRepositoryValue() != null) {
                if (param.getRepositoryValue().equals(repositoryName)) {
                    if (param.getFieldType().equals(EParameterFieldType.CLOSED_LIST)) {
                        String repositoryItem = getRepositoryItemFromRepositoryName(param, repositoryName);
                        if (isConetxtParaMode(connection, repositoryItem)) {
                            return getContextOriginalValue(connection, node, repositoryItem);
                        }
                        return repositoryItem;
                    } else {
                        String value = (String) param.getValue();
                        if (isConetxtParaMode(connection, value)) {
                            return getContextOriginalValue(connection, node, value);
                        }
                        return value;
                    }
                }
            }
        }
        return ""; //$NON-NLS-1$
    }

    protected static String getRepositoryItemFromRepositoryName(IElementParameter param, String repositoryName) {
        String value = (String) param.getValue();
        Object[] valuesList = param.getListItemsValue();
        String[] originalList = param.getListItemsDisplayName();
        for (int i = 0; i < valuesList.length; i++) {
            if (valuesList[i].equals(value)) {
                return originalList[i];
            }
        }

        return ""; //$NON-NLS-1$
    }

    private static Object getParameterObjectValue(INode node, String paramName) {
        if (node != null || paramName != null) {
            IElementParameter param = node.getElementParameter(paramName);
            if (param != null) {
                Object o = param.getValue();
                return o;
            }
        }
        return null;
    }

    /**
     * 
     * DOC wzhang Comment method "setDatabaseType".
     * 
     * @param connection
     * @param node
     */
    private static void setDatabaseType(DatabaseConnection connection, INode node) {
        IElementParameter parameter = node.getElementParameter("TYPE"); //$NON-NLS-1$
        if (parameter == null) {
            // GreePlum
            IElementParameter para = node.getElementParameter("PROPERTY"); //$NON-NLS-1$
            if (para.getRepositoryValue().endsWith(EDatabaseTypeName.GREENPLUM.getProduct())) {
                connection.setDatabaseType(EDatabaseTypeName.GREENPLUM.getDisplayName());
                connection.setProductId(EDatabaseTypeName.GREENPLUM.getProduct());
            }
            // PostgresPlus
            if (para.getRepositoryValue().endsWith(EDatabaseTypeName.PLUSPSQL.getProduct())) {
                connection.setDatabaseType(EDatabaseTypeName.PLUSPSQL.getDisplayName());
                connection.setProductId(EDatabaseTypeName.PLUSPSQL.getProduct());
            }
            // jdbc
            if (para.getRepositoryValue().endsWith(EDatabaseTypeName.GENERAL_JDBC.getProduct())) {
                connection.setDatabaseType(EDatabaseTypeName.GENERAL_JDBC.getDisplayName());
                connection.setProductId(EDatabaseTypeName.GENERAL_JDBC.getProduct());
            }
            return;
        }
        // mysql
        else if (EDatabaseTypeName.MYSQL.getProduct().equalsIgnoreCase((String) parameter.getValue())) {
            connection.setDatabaseType(EDatabaseTypeName.MYSQL.getDisplayName());
            connection.setProductId(EDatabaseTypeName.MYSQL.getProduct());
        }
        // mssql
        else if (EDatabaseTypeName.MSSQL.getXmlName().equalsIgnoreCase((String) parameter.getValue())) {
            connection.setDatabaseType(EDatabaseTypeName.MSSQL.getDisplayName());
            connection.setProductId(EDatabaseTypeName.MSSQL.getProduct());
        }
        // Exasolution
        else if (EDatabaseTypeName.EXASOL.getProduct().equalsIgnoreCase((String) parameter.getValue())) {
            connection.setDatabaseType(EDatabaseTypeName.EXASOL.getDisplayName());
            connection.setProductId(EDatabaseTypeName.EXASOL.getProduct());
        }
        // Psql
        else if (EDatabaseTypeName.PSQL.getProduct().equalsIgnoreCase((String) parameter.getValue())) {
            connection.setDatabaseType(EDatabaseTypeName.PSQL.getDisplayName());
            connection.setProductId(EDatabaseTypeName.PSQL.getProduct());
        }
        // PlusSql
        else if (EDatabaseTypeName.PLUSPSQL.getProduct().equalsIgnoreCase((String) parameter.getValue())) {
            connection.setDatabaseType(EDatabaseTypeName.PLUSPSQL.getDisplayName());
            connection.setProductId(EDatabaseTypeName.PLUSPSQL.getProduct());
        }
        // DB2
        else if (EDatabaseTypeName.IBMDB2.getProduct().equalsIgnoreCase(((String) parameter.getValue()).replace(' ', '_'))) {
            connection.setDatabaseType(EDatabaseTypeName.IBMDB2.getDisplayName());
            connection.setProductId(EDatabaseTypeName.IBMDB2.getProduct());
        }
        // Ingres
        else if (EDatabaseTypeName.INGRES.getProduct().equalsIgnoreCase((String) parameter.getValue())) {
            connection.setDatabaseType(EDatabaseTypeName.INGRES.getDisplayName());
            connection.setProductId(EDatabaseTypeName.INGRES.getProduct());
        }
        // Interbase
        else if (EDatabaseTypeName.INTERBASE.getProduct().equalsIgnoreCase((String) parameter.getValue())) {
            connection.setDatabaseType(EDatabaseTypeName.INTERBASE.getDisplayName());
            connection.setProductId(EDatabaseTypeName.INTERBASE.getProduct());
        }
        // Sqlite
        else if (EDatabaseTypeName.SQLITE.getProduct().equalsIgnoreCase((String) parameter.getValue())) {
            connection.setDatabaseType(EDatabaseTypeName.SQLITE.getDisplayName());
            connection.setProductId(EDatabaseTypeName.SQLITE.getProduct());
        }
        // Firebird
        else if (EDatabaseTypeName.FIREBIRD.getProduct().equalsIgnoreCase((String) parameter.getValue())) {
            connection.setDatabaseType(EDatabaseTypeName.FIREBIRD.getDisplayName());
            connection.setProductId(EDatabaseTypeName.FIREBIRD.getProduct());
        }
        // Informix
        else if (EDatabaseTypeName.INFORMIX.getProduct().equalsIgnoreCase((String) parameter.getValue())) {
            connection.setDatabaseType(EDatabaseTypeName.INFORMIX.getDisplayName());
            connection.setProductId(EDatabaseTypeName.INFORMIX.getProduct());
        }
        // Access
        else if (EDatabaseTypeName.ACCESS.getProduct().equalsIgnoreCase((String) parameter.getValue())) {
            connection.setDatabaseType(EDatabaseTypeName.ACCESS.getDisplayName());
            connection.setProductId(EDatabaseTypeName.ACCESS.getProduct());
        }
        // Teradata
        else if (EDatabaseTypeName.TERADATA.getProduct().equalsIgnoreCase((String) parameter.getValue())) {
            connection.setDatabaseType(EDatabaseTypeName.TERADATA.getDisplayName());
            connection.setProductId(EDatabaseTypeName.TERADATA.getProduct());
        }
        // AS400
        else if (EDatabaseTypeName.AS400.getProduct().equalsIgnoreCase((String) parameter.getValue())) {
            connection.setDatabaseType(EDatabaseTypeName.AS400.getDisplayName());
            connection.setProductId(EDatabaseTypeName.AS400.getProduct());
        }
        // Vertica
        // not exist in "DB Type" in Database Connection page.
        // else if (EDatabaseTypeName.VERTICA.getProduct().equalsIgnoreCase((String) parameter.getValue())) {
        // connection.setDatabaseType(EDatabaseTypeName.VERTICA.getDisplayName());
        // connection.setProductId(EDatabaseTypeName.VERTICA.getProduct());
        // }

        // MaxDB
        else if (EDatabaseTypeName.MAXDB.getProduct().equalsIgnoreCase((String) parameter.getValue())) {
            connection.setDatabaseType(EDatabaseTypeName.MAXDB.getDisplayName());
            connection.setProductId(EDatabaseTypeName.MAXDB.getProduct());
        }
        // Paraccel
        else if (EDatabaseTypeName.PARACCEL.getProduct().equalsIgnoreCase((String) parameter.getValue())) {
            connection.setDatabaseType(EDatabaseTypeName.PARACCEL.getDisplayName());
            connection.setProductId(EDatabaseTypeName.PARACCEL.getProduct());
        }
        // NeTezza
        else if (EDatabaseTypeName.NETEZZA.getProduct().equalsIgnoreCase((String) parameter.getValue())) {
            connection.setDatabaseType(EDatabaseTypeName.NETEZZA.getDisplayName());
            connection.setProductId(EDatabaseTypeName.NETEZZA.getProduct());
        }
        // Sybase
        else if (((String) parameter.getValue()).toLowerCase().startsWith(EDatabaseTypeName.SYBASEASE.getProduct().toLowerCase())) {
            parameter = node.getElementParameter("TYPE"); //$NON-NLS-1$
            if ("SybaseASE".equals(parameter.getValue())) { //$NON-NLS-1$
                connection.setDatabaseType(EDatabaseTypeName.SYBASEASE.getDisplayName());
                connection.setProductId(EDatabaseTypeName.SYBASEASE.getProduct());
            }
            // not exist in "DB Type" in Database Connection page.
            // else if ("SybaseIQ".equals(parameter.getValue())) {
            // connection.setDatabaseType(EDatabaseTypeName.SYBASEIQ.getDisplayName());
            // }
            return;
        }

        // oracle
        else if (EDatabaseTypeName.ORACLEFORSID.getProduct().equalsIgnoreCase((String) parameter.getValue())
                || EDatabaseTypeName.ORACLEFORSID.getXmlName().equalsIgnoreCase((String) parameter.getValue())
                || EDatabaseTypeName.ORACLESN.getXmlName().equalsIgnoreCase((String) parameter.getValue())
                || EDatabaseTypeName.ORACLE_OCI.getXmlName().equalsIgnoreCase((String) parameter.getValue())
                || EDatabaseTypeName.ORACLE_RAC.getXmlName().equalsIgnoreCase((String) parameter.getValue())) {
            parameter = node.getElementParameter("CONNECTION_TYPE"); //$NON-NLS-1$
            // if ("ORACLE_OCI".equals(parameter.getValue())) {
            // }

            if ("ORACLE_SERVICE_NAME".equals(parameter.getValue()) || "service_name".equals(parameter.getValue())) { //$NON-NLS-1$ //$NON-NLS-2$
                connection.setDatabaseType(EDatabaseTypeName.ORACLESN.getDisplayName());
                connection.setProductId(EDatabaseTypeName.ORACLESN.getProduct());
            } else if ("ORACLE_SID".equals(parameter.getValue()) || "sid".equals(parameter.getValue())) { //$NON-NLS-1$  //$NON-NLS-2$
                connection.setDatabaseType(EDatabaseTypeName.ORACLEFORSID.getDisplayName());
                connection.setProductId(EDatabaseTypeName.ORACLESN.getProduct());
            } else if ("ORACLE_RAC".equals(parameter.getValue()) || "rac".equals(parameter.getValue())) { //$NON-NLS-1$  //$NON-NLS-2$
                connection.setDatabaseType(EDatabaseTypeName.ORACLE_RAC.getDisplayName());
                connection.setProductId(EDatabaseTypeName.ORACLESN.getProduct());
            }
            return;
        }
        // HSql
        else if (EDatabaseTypeName.HSQLDB_SERVER.getProduct().equalsIgnoreCase((String) parameter.getValue())) {
            parameter = node.getElementParameter("RUNNING_MODE"); //$NON-NLS-1$
            if ("HSQLDB_SERVER".equals(parameter.getValue())) { //$NON-NLS-1$
                connection.setDatabaseType(EDatabaseTypeName.HSQLDB_SERVER.getDisplayName());
                connection.setProductId(EDatabaseTypeName.HSQLDB_SERVER.getProduct());
            } else if ("HSQLDB_WEBSERVER".equals(parameter.getValue())) { //$NON-NLS-1$
                connection.setDatabaseType(EDatabaseTypeName.HSQLDB_WEBSERVER.getDisplayName());
                connection.setProductId(EDatabaseTypeName.HSQLDB_WEBSERVER.getProduct());
            } else if ("HSQLDB_INPROGRESS_PERSISTENT".equals(parameter.getValue())) { //$NON-NLS-1$
                connection.setDatabaseType(EDatabaseTypeName.HSQLDB_IN_PROGRESS.getDisplayName());
                connection.setProductId(EDatabaseTypeName.HSQLDB_IN_PROGRESS.getProduct());
            }
            return;
        }

        // JavaDB
        else if (EDatabaseTypeName.JAVADB_EMBEDED.getProduct().equalsIgnoreCase((String) parameter.getValue())) {
            parameter = node.getElementParameter("FRAMEWORK_TYPE"); //$NON-NLS-1$
            if ("EMBEDED".equals(parameter.getValue())) { //$NON-NLS-1$
                connection.setDatabaseType(EDatabaseTypeName.JAVADB_EMBEDED.getDisplayName());
                connection.setProductId(EDatabaseTypeName.JAVADB_EMBEDED.getProduct());
            } else if ("JCCJDBC".equals(parameter.getValue())) { //$NON-NLS-1$
                connection.setDatabaseType(EDatabaseTypeName.JAVADB_JCCJDBC.getDisplayName());
                connection.setProductId(EDatabaseTypeName.JAVADB_JCCJDBC.getProduct());
            } else if ("DERBYCLIENT".equals(parameter.getValue())) { //$NON-NLS-1$
                connection.setDatabaseType(EDatabaseTypeName.JAVADB_DERBYCLIENT.getDisplayName());
                connection.setProductId(EDatabaseTypeName.JAVADB_DERBYCLIENT.getProduct());
            }
            return;
        }

        // DB
        else if (node.getComponent().getName().startsWith("tDBInput") || node.getComponent().getName().startsWith("tDBOutput")) { //$NON-NLS-1$ //$NON-NLS-2$
            parameter = node.getElementParameter("PROPERTY"); //$NON-NLS-1$
            if (parameter.getRepositoryValue().endsWith(EDatabaseTypeName.GODBC.getProduct())) {
                connection.setDatabaseType(EDatabaseTypeName.GODBC.getDisplayName());
                connection.setProductId(EDatabaseTypeName.GODBC.getProduct());
            }
        }

        // SAX
        // can not find corresponding component. also not exist in EDatabaseType.java.
    }

    /**
     * 
     * DOC wzhang Comment method "setDatabaseValue".
     * 
     * @param connection
     * @param node
     * @param repositoryValue
     */
    private static void setDatabaseValue(DatabaseConnection connection, INode node, String repositoryValue) {
        if ("USERNAME".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getValueFromRepositoryName(connection, node, "USERNAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setUsername(TalendQuoteUtils.removeQuotes(value));
            }
        }
        if ("PASSWORD".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getValueFromRepositoryName(connection, node, "PASSWORD"); //$NON-NLS-1$
            if (value != null) {
                // see bug in feature 5998,encrypt the password.
                try {
                    connection.setPassword(PasswordEncryptUtil.encryptPassword(TalendQuoteUtils.removeQuotes(value)));
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        if ("SERVER_NAME".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getValueFromRepositoryName(connection, node, "SERVER_NAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setServerName(TalendQuoteUtils.removeQuotes(value));
            }
        }
        if ("PORT".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getValueFromRepositoryName(connection, node, "PORT"); //$NON-NLS-1$
            if (value != null) {
                connection.setPort(TalendQuoteUtils.removeQuotes(value));
            }
        }
        if ("SID".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getValueFromRepositoryName(connection, node, "SID"); //$NON-NLS-1$
            if (value != null) {
                connection.setSID(TalendQuoteUtils.removeQuotes(value));
            }
        }
        if ("SCHEMA".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getValueFromRepositoryName(connection, node, "SCHEMA"); //$NON-NLS-1$
            if (value != null) {
                if (connection.getDatabaseType().equals(EDatabaseTypeName.ORACLEFORSID.getDisplayName())) {
                    connection.setUiSchema(TalendQuoteUtils.removeQuotes(value.toUpperCase()));
                } else {
                    connection.setUiSchema(TalendQuoteUtils.removeQuotes(value));
                }
            }
        }
        if ("CDC_TYPE_MODE".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "CDC_TYPE_MODE"); //$NON-NLS-1$
            if (value != null && Boolean.valueOf(value).booleanValue()) {
                connection.setCdcTypeMode(CDCTypeMode.LOG_MODE.getName());
            }
        }
        // for feature 11674
        if ("DBPATH".equals(repositoryValue)) {//$NON-NLS-1$
            String value = getParameterValue(connection, node, "DBPATH"); //$NON-NLS-1$
            if (value != null) {
                connection.setDBRootPath(value);
            }
        }
        if (connection.getDatabaseType().equals(EDatabaseTypeName.ORACLEFORSID.getDisplayName())) {
            setDatabaseValueForOracleSid(connection, node, repositoryValue);
        }
        if (connection.getDatabaseType().equals(EDatabaseTypeName.ORACLESN.getDisplayName())) {
            setDatabaseValueForOracleSeverName(connection, node, repositoryValue);
        }
        if (connection.getDatabaseType().equals(EDatabaseTypeName.MYSQL.getDisplayName())) {
            setDatabaseValueForMysql(connection, node, repositoryValue);
        }
        if (connection.getDatabaseType().equals(EDatabaseTypeName.EXASOL.getDisplayName())) {
            setDatabaseValueForEXASolution(connection, node, repositoryValue);
        }
        if (connection.getDatabaseType().equals(EDatabaseTypeName.SYBASEASE.getDisplayName())
                || connection.getDatabaseType().equals(EDatabaseTypeName.SYBASEIQ.getDisplayName())) {
            setDatabaseValueForSysbase(connection, node, repositoryValue);
        }
        if (connection.getDatabaseType().equals(EDatabaseTypeName.ACCESS.getDisplayName())) {
            setDatabaseValueForAccess(connection, node, repositoryValue);
        }
        if (connection.getDatabaseType().equals(EDatabaseTypeName.SQLITE.getDisplayName())) {
            setDatabaseValueForSqlite(connection, node, repositoryValue);
        }
        if (connection.getDatabaseType().equals(EDatabaseTypeName.FIREBIRD.getDisplayName())) {
            setDatabaseValueForFileBird(connection, node, repositoryValue);
        }
        if (connection.getDatabaseType().equals(EDatabaseTypeName.AS400.getDisplayName())) {
            setDatabaseValueForAs400(connection, node, repositoryValue);
        }
        if (connection.getDatabaseType().equals(EDatabaseTypeName.MSSQL.getDisplayName())) {
            setDatabaseValueForMSSql(connection, node, repositoryValue);
        }
        if (connection.getDatabaseType().equals(EDatabaseTypeName.GODBC.getDisplayName())) {
            setDatabaseValueForDB(connection, node, repositoryValue);
        }
        if (connection.getDatabaseType().equals(EDatabaseTypeName.INGRES.getDisplayName())) {
            setDatabaseValueForIngres(connection, node, repositoryValue);
        }
        if (connection.getDatabaseType().equals(EDatabaseTypeName.INFORMIX.getDisplayName())) {
            setDatabaseValueForInformix(connection, node, repositoryValue);
        }
        if (connection.getDatabaseType().equals(EDatabaseTypeName.JAVADB.getDisplayName())
                || connection.getDatabaseType().equals(EDatabaseTypeName.JAVADB_EMBEDED.getDisplayName())
                || connection.getDatabaseType().equals(EDatabaseTypeName.JAVADB_JCCJDBC.getDisplayName())
                || connection.getDatabaseType().equals(EDatabaseTypeName.JAVADB_DERBYCLIENT.getDisplayName())) {
            setDatabaseValueForjavadb(connection, node, repositoryValue);
        }
        if (connection.getDatabaseType().equals(EDatabaseTypeName.NETEZZA.getDisplayName())) {
            setDatabaseValueForNetezza(connection, node, repositoryValue);
        }
        if (connection.getDatabaseType().equals(EDatabaseTypeName.GENERAL_JDBC.getDisplayName())) {
            setDatabaseValueForJdbc(connection, node, repositoryValue);
        }
    }

    /**
     * 
     * DOC wzhang Comment method "setDatabaseValueForOracleSid".
     * 
     * @param connection
     * @param node
     * @param repositoryValue
     */
    private static void setDatabaseValueForOracleSid(DatabaseConnection connection, INode node, String repositoryValue) {

        if ("DB_VERSION".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "DB_VERSION"); //$NON-NLS-1$
            String dbVersionName = EDatabaseVersion4Drivers.getDbVersionName(EDatabaseTypeName.ORACLEFORSID, value);
            if (value != null) {
                connection.setDbVersionString(dbVersionName);
            }
        }
        if ("SID".equals(repositoryValue)) { //$NON-NLS-1$
            IElementParameter param = node.getElementParameter("CONNECTION_TYPE"); //$NON-NLS-1$
            if (param != null && "ORACLE_OCI".equals(param.getValue())) { //$NON-NLS-1$
                String value = getParameterValue(connection, node, "LOCAL_SERVICE_NAME"); //$NON-NLS-1$
                if (value != null) {
                    connection.setSID(value);
                }
            } else {
                String value = getParameterValue(connection, node, "DBNAME"); //$NON-NLS-1$
                if (value != null) {
                    connection.setSID(value);
                }
            }
        }
    }

    private static void setDatabaseValueForOracleSeverName(DatabaseConnection connection, INode node, String repositoryValue) {
        if ("DB_VERSION".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "DB_VERSION"); //$NON-NLS-1$
            String dbVersionName = EDatabaseVersion4Drivers.getDbVersionName(EDatabaseTypeName.ORACLESN, value);
            if (value != null) {
                connection.setDbVersionString(dbVersionName);
            }
        }
        if ("SID".equals(repositoryValue)) { //$NON-NLS-1$
            IElementParameter param = node.getElementParameter("CONNECTION_TYPE"); //$NON-NLS-1$
            if (param != null && "ORACLE_OCI".equals(param.getValue())) { //$NON-NLS-1$
                String value = getParameterValue(connection, node, "LOCAL_SERVICE_NAME"); //$NON-NLS-1$
                if (value != null) {
                    connection.setSID(value);
                }
            } else {
                String value = getParameterValue(connection, node, "DBNAME"); //$NON-NLS-1$
                if (value != null) {
                    connection.setSID(value);
                }
            }
        }
    }

    private static void setDatabaseValueForAs400(DatabaseConnection connection, INode node, String repositoryValue) {
        if ("DB_VERSION".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "DB_VERSION"); //$NON-NLS-1$
            String dbVersionName = EDatabaseVersion4Drivers.getDbVersionName(EDatabaseTypeName.AS400, value);
            if (value != null) {
                connection.setDbVersionString(dbVersionName);
            }
        }

        if ("PROPERTIES_STRING".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "PROPERTIES"); //$NON-NLS-1$
            if (value != null) {
                connection.setAdditionalParams(value);
            }
        }
    }

    private static void setDatabaseValueForMysql(DatabaseConnection connection, INode node, String repositoryValue) {
        if ("PROPERTIES_STRING".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "PROPERTIES"); //$NON-NLS-1$
            if (value != null) {
                connection.setAdditionalParams(value);
            }
        }
    }

    private static void setDatabaseValueForEXASolution(DatabaseConnection connection, INode node, String repositoryValue) {
        if ("PROPERTIES_STRING".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "PROPERTIES"); //$NON-NLS-1$
            if (value != null) {
                connection.setAdditionalParams(value);
            }
        }
    }

    private static void setDatabaseValueForMSSql(DatabaseConnection connection, INode node, String repositoryValue) {
        if ("PROPERTIES_STRING".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "PROPERTIES"); //$NON-NLS-1$
            if (value != null) {
                connection.setAdditionalParams(value);
            }
        }
    }

    private static void setDatabaseValueForDB(DatabaseConnection connection, INode node, String repositoryValue) {
        if ("DATASOURCE".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "DBNAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setDatasourceName(value);
            }
        }
    }

    private static void setDatabaseValueForSysbase(DatabaseConnection connection, INode node, String repositoryValue) {
        if ("SERVER_NAME".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "SERVER"); //$NON-NLS-1$
            if (value != null) {
                connection.setServerName(value);
            }
        }
    }

    private static void setDatabaseValueForAccess(DatabaseConnection connection, INode node, String repositoryValue) {
        if ("DB_VERSION".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "DB_VERSION"); //$NON-NLS-1$
            if (value != null) {
                EDatabaseVersion4Drivers version = EDatabaseVersion4Drivers.indexOfByVersion(value);
                if (version != null) {
                    connection.setDbVersionString(version.getVersionValue());
                }
            }
        }
        if ("FILE".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "DBNAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setFileFieldName(value);
            }
        }
    }

    private static void setDatabaseValueForFileBird(DatabaseConnection connection, INode node, String repositoryValue) {
        if ("FILE".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "DB"); //$NON-NLS-1$
            if (value != null) {
                connection.setFileFieldName(value);
            }
        }
    }

    private static void setDatabaseValueForSqlite(DatabaseConnection connection, INode node, String repositoryValue) {
        if ("FILE".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "DBNAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setFileFieldName(value);
            }
        }
    }

    private static void setDatabaseValueForIngres(DatabaseConnection connection, INode node, String repositoryValue) {
        if ("SERVER_NAME".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "SERVER"); //$NON-NLS-1$
            if (value != null) {
                connection.setServerName(value);
            }
        }
    }

    private static void setDatabaseValueForInformix(DatabaseConnection connection, INode node, String repositoryValue) {
        if ("PROPERTIES_STRING".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "PROPERTIES"); //$NON-NLS-1$
            if (value != null) {
                connection.setAdditionalParams(value);
            }
        }
        if ("DATASOURCE".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "DBSERVER"); //$NON-NLS-1$
            if (value != null) {
                connection.setDatasourceName(value);
            }
        }

    }

    private static void setDatabaseValueForjavadb(DatabaseConnection connection, INode node, String repositoryValue) {

        if ("SID".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "DB"); //$NON-NLS-1$
            if (value != null) {
                connection.setSID(value);
            }
        }
        if ("DIRECTORY".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "DBPATH"); //$NON-NLS-1$
            if (value != null) {
                connection.setDBRootPath(value);
            }
        }
    }

    private static void setDatabaseValueForNetezza(DatabaseConnection connection, INode node, String repositoryValue) {
        if ("DBNAME".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "DBNAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setDatasourceName(value);
            }
        }
    }

    private static void setDatabaseValueForJdbc(DatabaseConnection connection, INode node, String repositoryValue) {
        if ("URL".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "URL"); //$NON-NLS-1$
            if (value != null) {
                connection.setURL(value);
            }
        }
        if ("DRIVER_JAR".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "DRIVER_JAR"); //$NON-NLS-1$
            if (value != null) {
                connection.setDriverJarPath(value);
            }
        }
        if ("DRIVER_CLASS".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "DRIVER_CLASS"); //$NON-NLS-1$
            if (value != null) {
                connection.setDriverClass(value);
            }
        }
    }

    /**
     * 
     * DOC wzhang Comment method "setXmlFileValue".
     * 
     * @param connection
     * @param node
     * @param repositoryValue
     */
    private static void setXmlFileValue(XmlFileConnection connection, INode node, String repositoryValue) {

        if ("FILE_PATH".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "FILENAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setXmlFilePath(value);
            }
        }
        if ("ENCODING".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "ENCODING"); //$NON-NLS-1$
            if (value != null) {
                connection.setEncoding(value);
            }
        }
        EList emfSchemaList = connection.getSchema();
        if (emfSchemaList.size() < 1) {
            emfSchemaList.add(ConnectionFactory.eINSTANCE.createXmlXPathLoopDescriptor());
        }

        XmlXPathLoopDescriptor xmlDesc = (XmlXPathLoopDescriptor) emfSchemaList.get(0);
        if ("XPATH_QUERY".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "LOOP_QUERY"); //$NON-NLS-1$
            if (value != null) {
                xmlDesc.setAbsoluteXPathQuery(value);
            }
        }
        if ("LIMIT".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "LIMIT"); //$NON-NLS-1$
            if (value != null && value.trim().length() > 0) {
                xmlDesc.setLimitBoucle(Integer.valueOf(value));
            }
        }
        if ("XML_MAPPING".equals(repositoryValue)) { //$NON-NLS-1$
            IElementParameter param = node.getElementParameter("MAPPING"); //$NON-NLS-1$
            if (param != null) {
                EList schemaTargets = xmlDesc.getSchemaTargets();
                List<Map<String, Object>> tableInfo = (List<Map<String, Object>>) param.getValue();
                for (Map<String, Object> mapObject : tableInfo) {
                    String schema = (String) mapObject.get("SCHEMA_COLUMN"); //$NON-NLS-1$
                    if (schema != null) {
                        String query = (String) mapObject.get("QUERY"); //$NON-NLS-1$
                        SchemaTarget schemaTarget = ConnectionFactory.eINSTANCE.createSchemaTarget();
                        schemaTargets.add(schemaTarget);
                        schemaTarget.setTagName(schema);
                        schemaTarget.setRelativeXPathQuery(TalendQuoteUtils.removeQuotes(query));
                    }
                }
            }
        }

    }

    /**
     * 
     * DOC wzhang Comment method "setLDAPSchemaValue".
     * 
     * @param connection
     * @param node
     * @param repositoryValue
     */
    private static void setLDAPSchemaValue(LDAPSchemaConnection connection, INode node, String repositoryValue) {
        if ("HOST".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "HOST").replaceAll("\\\\\\\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (value != null) {
                connection.setHost(value);
            }
        }
        if ("PORT".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "PORT"); //$NON-NLS-1$
            if (value != null) {
                connection.setPort(value);
            }
        }
        if ("BASEDN".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "BASEDN").replaceAll("\\\\\\\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (value != null) {
                connection.setSelectedDN(value);
            }
        }
        if ("PROTOCOL".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "PROTOCOL"); //$NON-NLS-1$
            if (value != null) {
                connection.setEncryptionMethodName(value);
            }
        }

        if ("AUTHENTIFICATION".equals(repositoryValue)) { //$NON-NLS-1$
            IElementParameter param = node.getElementParameter("AUTHENTIFICATION"); //$NON-NLS-1$
            if (param != null) {
                Object o = param.getValue();
                if (o != null && o instanceof Boolean) {
                    connection.setUseAuthen((Boolean) o);
                }
            }
        }

        if ("USER".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "USER").replaceAll("\\\\\\\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (value != null) {
                connection.setBindPrincipal(value);
            }
        }
        if ("PASSWORD".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "PASS").replaceAll("\\\\\\\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (value != null) {
                connection.setBindPassword(value);
            }
        }
        if ("FILTER".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "FILTER"); //$NON-NLS-1$
            if (value != null) {
                connection.setFilter(value);
            }
        }
        if ("ALIASES".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "ALIASES"); //$NON-NLS-1$
            if (value != null) {
                connection.setAliases(value);
            }
        }
        if ("REFERRALS".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "REFERRALS"); //$NON-NLS-1$
            if (value != null) {
                connection.setReferrals(value);
            }
        }
        if ("COLUMN_COUNT_LIMIT".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "LIMIT"); //$NON-NLS-1$
            if (value != null) {
                connection.setCountLimit(value);
            }
        }
        if ("TIME_OUT_LIMIT".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "TIMEOUT"); //$NON-NLS-1$
            if (value != null) {
                connection.setTimeOutLimit(value);
            }
        }
    }

    private static void setWSDLSchemaValue(WSDLSchemaConnection connection, INode node, String repositoryValue) {
        if ("ENDPOINT".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "ENDPOINT"); //$NON-NLS-1$
            if (value != null) {
                connection.setWSDL(value);
            }
        }
        if ("WSDLURL".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "WSDL"); //$NON-NLS-1$
            if (value != null) {
                connection.setEndpointURI(value);
            }
        }
        if ("NEED_AUTH".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "NEED_AUTH"); //$NON-NLS-1$
            if (value != null) {
                connection.setNeedAuth(Boolean.valueOf(value));
            }
        }
        if ("AUTH_USERNAME".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "AUTH_USERNAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setUserName(value);
            }
        }
        if ("AUTH_PASSWORD".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "AUTH_PASSWORD"); //$NON-NLS-1$
            if (value != null) {
                connection.setPassword(value);
            }
        }
        if ("UES_PROXY".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "UES_PROXY"); //$NON-NLS-1$
            if (value != null) {
                connection.setUseProxy(Boolean.valueOf(value));
            }
        }
        if ("PROXY_HOST".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "PROXY_HOST"); //$NON-NLS-1$
            if (value != null) {
                connection.setProxyHost(value);
            }
        }
        if ("PROXY_PORT".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "PROXY_PORT"); //$NON-NLS-1$
            if (value != null) {
                connection.setProxyPort(value);
            }
        }
        if ("PROXY_USERNAME".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "PROXY_USERNAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setProxyUser(value);
            }
        }
        if ("PROXY_PASSWORD".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "PROXY_PASSWORD"); //$NON-NLS-1$
            if (value != null) {
                connection.setProxyPassword(value);
            }
        }
        if ("METHOD".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "METHOD"); //$NON-NLS-1$
            if (value != null) {
                connection.setMethodName(value);
            }
        }
        if ("TIMEOUT".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "TIMEOUT"); //$NON-NLS-1$
            if (value != null) {
                connection.setTimeOut(Integer.valueOf(value));
            }
        }
        if ("WSDL_PARAMS".equals(repositoryValue)) { //$NON-NLS-1$
            Object value = getParameterObjectValue(node, "PARAMS"); //$NON-NLS-1$
            if (value != null && value instanceof ArrayList) {
                ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) value;
                ArrayList<String> result = new ArrayList<String>();
                for (HashMap<String, String> m : list) {
                    Iterator<Map.Entry<String, String>> it = m.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, String> entry = it.next();
                        result.add(entry.getValue());
                    }
                }
                connection.setParameters((ArrayList) result);
            }
        }
    }

    private static void setEbcdicValue(EbcdicConnection connection, INode node, String repositoryValue) {
        if ("DATA_FILE".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "FILENAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setDataFile(value);
            }
        }
        if ("XC2J_FILE".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "COPYBOOK"); //$NON-NLS-1$
            if (value != null) {
                connection.setMidFile(value);
            }
        }
        if ("ENCODING".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "ENCODING"); //$NON-NLS-1$
            if (value != null) {
                connection.setEncoding(value);
            }
        }
    }

    private static void setMDMValue(MDMConnection connection, INode node, String repositoryValue) {
        if ("USERNAME".equals(repositoryValue)) {//$NON-NLS-1$
            String value = getParameterValue(connection, node, "USERNAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setUsername(value);
            }
        }

        if ("PASSWORD".equals(repositoryValue)) {//$NON-NLS-1$
            String value = getParameterValue(connection, node, "PASSWORD"); //$NON-NLS-1$
            if (value != null) {
                ConnectionHelper.setPassword(connection, value);
            }
        }

        if ("MDMURL".equals(repositoryValue)) {//$NON-NLS-1$
            String value = getParameterValue(connection, node, "MDMURL"); //$NON-NLS-1$
            if (value != null) {
                String[] values = value.split(":"); //$NON-NLS-1$
                String server = values[1].substring(values[1].indexOf("//") + 2); //$NON-NLS-1$
                String port = values[2].substring(0, values[2].indexOf("/")); //$NON-NLS-1$

                connection.setServer(server);
                connection.setPort(port);
            }
        }

        if ("UNIVERSE".equals(repositoryValue)) {//$NON-NLS-1$
            String value = getParameterValue(connection, node, "UNIVERSE"); //$NON-NLS-1$
            if (value != null) {
                connection.setUniverse(value);
            }
        }

        if ("DATAMODEL".equals(repositoryValue)) {//$NON-NLS-1$
            String value = getParameterValue(connection, node, "DATAMODEL"); //$NON-NLS-1$
            if (value != null) {
                connection.setDatamodel(value);
            }
        }

        if ("DATACLUSTER".equals(repositoryValue)) {//$NON-NLS-1$
            String value = getParameterValue(connection, node, "DATACLUSTER"); //$NON-NLS-1$
            if (value != null) {
                connection.setDatacluster(value);
            }
        }
    }

    private static void setBRMSValue(BRMSConnection connection, INode node, String repositoryValue) {
        if ("XML_FIELD".equals(repositoryValue)) {//$NON-NLS-1$
            String value = getParameterValue(connection, node, "XMLFIELD"); //$NON-NLS-1$
            if (value != null) {
                connection.setXmlField(value);
            }
        }

        if ("GUVNOR_URL".equals(repositoryValue)) {//$NON-NLS-1$
            String value = getParameterValue(connection, node, "GUVNOR_URL"); //$NON-NLS-1$
            if (value != null) {
                connection.setUrlName(value);
            }
        }

        if ("CLASS_NAME".equals(repositoryValue)) {//$NON-NLS-1$
            String value = getParameterValue(connection, node, "CLASS_NAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setClassName(value);
            }
        }

        if ("GUVNOR_PACKAGE".equals(repositoryValue)) {//$NON-NLS-1$
            String value = getParameterValue(connection, node, "GUVNOR_PACKAGE"); //$NON-NLS-1$
            if (value != null) {
                connection.setPackage(value);
            }
        }

        if ("MODULE_USED".equals(repositoryValue)) {//$NON-NLS-1$
            String value = getParameterValue(connection, node, "LIBRARY"); //$NON-NLS-1$
            if (value != null) {
                connection.setModuleUsed(value);
            }
        }
    }

    private static void setHL7Value(HL7Connection connection, INode node, String repositoryValue) {
        if ("FILE_PATH".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "FILENAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setFilePath(value);
            }
        }
        if ("START_MSG".equals(repositoryValue)) { //$NON-NLS-1$
            final String value = getParameterValue(connection, node, "START_MSG"); //$NON-NLS-1$
            if (value != null) {
                connection.setStartChar(value);
            }
        }
        if ("END_MSG".equals(repositoryValue)) { //$NON-NLS-1$
            final String value = getParameterValue(connection, node, "END_MSG"); //$NON-NLS-1$
            if (value != null) {
                connection.setEndChar(value);
            }
        }

    }

    private static void setLdifFileValue(LdifFileConnection connection, INode node, String repositoryValue) {
        if ("FILE_PATH".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "FILENAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setFilePath(value);
            }
        }
    }

    private static void setFileExcelValue(FileExcelConnection connection, INode node, String repositoryValue) {
        if ("FILE_PATH".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "FILENAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setFilePath(value);
            }
        }
        if ("SELECT_ALL_SHEETS".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "ALL_SHEETS"); //$NON-NLS-1$
            if (value != null) {
                connection.setSelectAllSheets(Boolean.valueOf(value).booleanValue());
            }
        }

        // if ("SHEET_LIST".equals(repositoryValue)) {
        // Object value = getParameterObjectValue(node, "SHEETLIST");
        // if (value != null && value instanceof ArrayList) {
        // ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) value;
        // ArrayList<String> result = new ArrayList<String>();
        // for (HashMap<String, String> m : list) {
        // Iterator<Map.Entry<String, String>> it = m.entrySet().iterator();
        // while (it.hasNext()) {
        // Map.Entry<String, String> entry = it.next();
        // result.add(entry.getValue());
        // }
        // }
        // connection.setSheetList((ArrayList) result);
        // }
        // }

        if ("ADVANCED_SEPARATOR".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "ADVANCED_SEPARATOR"); //$NON-NLS-1$
            if (value != null) {
                connection.setAdvancedSpearator(Boolean.valueOf(value).booleanValue());
            }
        }
        if ("HEADER".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "HEADER"); //$NON-NLS-1$
            if (value != null) {
                connection.setHeaderValue(value);
            }
        }
        if ("FOOTER".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "FOOTER"); //$NON-NLS-1$
            if (value != null) {
                connection.setFooterValue(value);
            }
        }
        if ("LIMIT".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "LIMIT"); //$NON-NLS-1$
            if (value != null) {
                connection.setLimitValue(value);
            }
        }
        if ("FIRST_COLUMN".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "FIRST_COLUMN"); //$NON-NLS-1$
            if (value != null) {
                connection.setFirstColumn(value);
            }
        }
        if ("LAST_COLUMN".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "LAST_COLUMN"); //$NON-NLS-1$
            if (value != null) {
                connection.setLastColumn(value);
            }
        }

        if ("THOUSANDS_SEPARATOR".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "THOUSANDS_SEPARATOR"); //$NON-NLS-1$
            if (value != null) {
                connection.setThousandSeparator(value);
            }
        }
        if ("DECIMAL_SEPARATOR".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "DECIMAL_SEPARATOR"); //$NON-NLS-1$
            if (value != null) {
                connection.setDecimalSeparator(value);
            }
        }
        if ("ENCODING".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "ENCODING"); //$NON-NLS-1$
            if (value != null) {
                connection.setEncoding(value);
            }
        }

    }

    /**
     * 
     * DOC wzhang Comment method "setDelimitedFileValue".
     * 
     * @param connection
     * @param node
     * @param repositoryValue
     */
    private static void setDelimitedFileValue(DelimitedFileConnection connection, INode node, String repositoryValue) {
        if ("FILE_PATH".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "FILENAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setFilePath(value);
            }
        }
        if ("ROW_SEPARATOR".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "ROWSEPARATOR"); //$NON-NLS-1$
            if (value != null) {
                connection.setRowSeparatorValue(value);
            }
        }
        if ("FIELD_SEPARATOR".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "FIELDSEPARATOR"); //$NON-NLS-1$
            if (value != null) {
                connection.setFieldSeparatorValue(value);
            }
        }
        if ("CSV_OPTION".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "CSV_OPTION"); //$NON-NLS-1$
            if (value != null) {
                connection.setCsvOption(Boolean.valueOf(value).booleanValue());
            }
            if (connection.isCsvOption()) {
                connection.setEscapeType(Escape.CSV);
            } else {
                connection.setEscapeType(Escape.DELIMITED);
            }
        }
        if ("ESCAPE_CHAR".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterOriginalValue(connection, node, "ESCAPE_CHAR"); //$NON-NLS-1$
            if (value != null) {
                connection.setEscapeChar(value);
            }
        }
        if ("TEXT_ENCLOSURE".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterOriginalValue(connection, node, "TEXT_ENCLOSURE"); //$NON-NLS-1$
            if (value != null) {
                connection.setTextEnclosure(value);
            }
        }
        if ("HEADER".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "HEADER"); //$NON-NLS-1$
            if (value != null) {
                connection.setHeaderValue(value);
            }
        }

        if ("FOOTER".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "FOOTER"); //$NON-NLS-1$
            if (value != null) {
                connection.setFooterValue(value);
            }
        }
        if ("LIMIT".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "LIMIT"); //$NON-NLS-1$
            if (value != null) {
                connection.setLimitValue(value);
            }
        }
        if ("REMOVE_EMPTY_ROW".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "REMOVE_EMPTY_ROW"); //$NON-NLS-1$
            if (value != null) {
                connection.setRemoveEmptyRow(Boolean.valueOf(value).booleanValue());
            }
        }
        if ("ENCODING".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "ENCODING"); //$NON-NLS-1$
            if (value != null) {
                connection.setEncoding(value);
            }
        }
        if ("SPLITRECORD".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "SPLITRECORD"); //$NON-NLS-1$
            if (value != null) {
                connection.setSplitRecord(Boolean.valueOf(value).booleanValue());
            }
        }
    }

    /**
     * 
     * DOC wzhang Comment method "setPositionalFileValue".
     * 
     * @param connection
     * @param node
     * @param repositoryValue
     */

    private static void setPositionalFileValue(PositionalFileConnection connection, INode node, String repositoryValue) {
        if ("FILE_PATH".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "FILENAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setFilePath(value);
            }
        }
        if ("ROW_SEPARATOR".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "ROWSEPARATOR"); //$NON-NLS-1$
            if (value != null) {
                connection.setRowSeparatorValue(value);
            }
        }
        if ("PATTERN".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "PATTERN"); //$NON-NLS-1$
            if (value != null) {
                connection.setFieldSeparatorValue(value);
            }
        }
        if ("REMOVE_EMPTY_ROW".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "REMOVE_EMPTY_ROW"); //$NON-NLS-1$
            if (value != null) {
                connection.setRemoveEmptyRow(Boolean.valueOf(value).booleanValue());
            }
        }
        if ("HEADER".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "HEADER"); //$NON-NLS-1$
            if (value != null) {
                connection.setHeaderValue(value);
            }
        }
        if ("FOOTER".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "FOOTER"); //$NON-NLS-1$
            if (value != null) {
                connection.setFooterValue(value);
            }
        }
        if ("LIMIT".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "LIMIT"); //$NON-NLS-1$
            if (value != null) {
                connection.setLimitValue(value);
            }
        }
    }

    /**
     * 
     * DOC wzhang Comment method "setRegexpFileValue".
     * 
     * @param connection
     * @param node
     * @param repositoryValue
     */
    private static void setRegexpFileValue(RegexpFileConnection connection, INode node, String repositoryValue) {
        if ("FILE_PATH".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "FILENAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setFilePath(value);
            }
        }
        if ("ROW_SEPARATOR".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "ROWSEPARATOR"); //$NON-NLS-1$
            if (value != null) {
                connection.setRowSeparatorValue(value);
            }
        }
        if ("REGEXP".equals(repositoryValue)) { //$NON-NLS-1$
            IElementParameter param = node.getElementParameter("REGEX"); //$NON-NLS-1$
            if (param != null) {
                String value = (String) param.getValue();
                if (value != null) {
                    connection.setFieldSeparatorValue(value);
                }
            }
        }
        if ("HEADER".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "HEADER"); //$NON-NLS-1$
            if (value != null) {
                connection.setHeaderValue(value);
            }
        }
        if ("FOOTER".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "FOOTER"); //$NON-NLS-1$
            if (value != null) {
                connection.setFooterValue(value);
            }
        }
        if ("LIMIT".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "LIMIT"); //$NON-NLS-1$
            if (value != null) {
                connection.setLimitValue(value);
            }
        }
        if ("REMOVE_EMPTY_ROW".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "REMOVE_EMPTY_ROW"); //$NON-NLS-1$
            if (value != null) {
                connection.setRemoveEmptyRow(Boolean.valueOf(value).booleanValue());
            }
        }
        if ("ENCODING".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "ENCODING"); //$NON-NLS-1$
            if (value != null) {
                connection.setEncoding(value);
            }
        }
    }

    /*
     * SAP
     */
    private static void setSAPValue(SAPConnection connection, INode node, String repositoryValue) {
        if ("CLIENT".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "CLIENT"); //$NON-NLS-1$
            if (value != null) {
                connection.setClient(value);
            }
        }
        if ("PASSWORD".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "PASSWORD"); //$NON-NLS-1$
            if (value != null) {
                connection.setPassword(value);
            }
        }
        if ("LANGUAGE".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "LANGUAGE"); //$NON-NLS-1$
            if (value != null) {
                connection.setLanguage(value);
            }
        }
        if ("HOSTNAME".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "HOSTNAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setHost(value);
            }
        }
        if ("USERID".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "USERID"); //$NON-NLS-1$
            if (value != null) {
                connection.setUsername(value);
            }
        }
        if ("SYSTEMNUMBER".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "SYSTEMNUMBER"); //$NON-NLS-1$
            if (value != null) {
                connection.setSystemNumber(value);
            }
        }
    }

    /**
     * 
     * DOC wzhang Comment method "setSalesforceSchema".
     * 
     * @param connection
     * @param node
     * @param repositoryValue
     */
    private static void setSalesforceSchema(SalesforceSchemaConnection connection, INode node, String repositoryValue) {

        if ("ENDPOINT".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "ENDPOINT"); //$NON-NLS-1$
            if (value != null) {
                connection.setWebServiceUrl(value);
            }
        }
        if ("USER_NAME".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "USER"); //$NON-NLS-1$
            if (value != null) {
                connection.setUserName(value);
            }
        }
        if ("PASSWORD".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "PASS"); //$NON-NLS-1$
            if (value != null) {
                connection.setPassword(value);
            }
        }
        if ("MODULENAME".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "MODULENAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setModuleName(value);
            }
        }
        if ("QUERY_CONDITION".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "CONDITION"); //$NON-NLS-1$
            if (value != null) {
                connection.setQueryCondition(value);
            }

        }
        if ("BATCH_SIZE".equals(repositoryValue)) { //$NON-NLS-1$
            connection.setBatchSize(getParameterValue(connection, node, "BATCH_SIZE")); //$NON-NLS-1$
            // add for feature 7507
        }
        if ("UES_PROXY".equals(repositoryValue)) { //$NON-NLS-1$
            String parameterValue = getParameterValue(connection, node, "UES_PROXY"); //$NON-NLS-1$
            connection.setUseProxy(Boolean.valueOf(parameterValue));
        }
        if ("PROXY_HOST".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "PROXY_HOST"); //$NON-NLS-1$
            if (value != null) {
                connection.setProxyHost(value);
            }

        }
        if ("PROXY_PORT".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "PROXY_PORT"); //$NON-NLS-1$
            if (value != null) {
                connection.setProxyPort(value);
            }

        }
        if ("PROXY_USERNAME".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "PROXY_USERNAME"); //$NON-NLS-1$
            if (value != null) {
                connection.setProxyUsername(value);
            }

        }
        if ("PROXY_PASSWORD".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "PROXY_PASSWORD"); //$NON-NLS-1$
            if (value != null) {
                connection.setProxyPassword(value);
            }

        }
        if ("TIMEOUT".equals(repositoryValue)) { //$NON-NLS-1$
            String value = getParameterValue(connection, node, "TIMEOUT"); //$NON-NLS-1$
            if (value != null) {
                connection.setTimeOut(value);
            }

        }
    }

    /**
     * wzhang Comment method "isConetxtParaMode".
     */
    private static boolean isConetxtParaMode(Connection connection, String value) {
        if (value == null) {
            return false;
        }
        if (connection.isContextMode() && ContextParameterUtils.isContainContextParam(value)) {
            return true;
        }
        return false;
    }

    /**
     * wzhang Comment method "getContextOriginalValue".
     */
    private static String getContextOriginalValue(Connection connection, INode node, String value) {
        if (!isConetxtParaMode(connection, value)) {
            String variable = ContextParameterUtils.getVariableFromCode(value);
            IContextManager contextManager = node.getProcess().getContextManager();
            IContext context = contextManager.getContext(value);
            List<IContextParameter> contextParameterList = context.getContextParameterList();
            for (IContextParameter contextPara : contextParameterList) {
                String contextName = contextPara.getName();
                if (contextName != null && contextName.equals(variable)) {
                    value = contextPara.getValue();
                }
            }
        }
        return value;
    }
}
