// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.FTPConnection;
import org.talend.core.model.metadata.builder.connection.LDAPSchemaConnection;
import org.talend.core.model.metadata.builder.connection.LdifFileConnection;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection;
import org.talend.core.model.metadata.builder.connection.SchemaTarget;
import org.talend.core.model.metadata.builder.connection.WSDLSchemaConnection;
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.metadata.builder.connection.XmlXPathLoopDescriptor;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.repository.model.IConnParamName;

/**
 * ggu class global comment. Detailled comment
 */
public final class OtherConnectionContextUtils {

    private static final ECodeLanguage LANGUAGE = LanguageManager.getCurrentLanguage();

    /**
     * 
     */
    public enum EParamName implements IConnParamName {
        FilePath,

        // xml
        // Encoding,
        XmlFilePath,
        XPathQuery,
        OutputFilePath,

        // Salesforce
        WebServiceUrl,
        UserName,
        Password,
        BatchSize,
        TimeOut,
        QueryCondition,

        // LDAP
        Host,
        Port,
        BindPassword,
        BindPrincipal,
        Filter,
        CountLimit,
        TimeOutLimit,

        // WSDL
        WSDL,
        ProxyHost,
        ProxyPort,
        ProxyUser,
        ProxyPassword,
        MethodName,
        EndpointURI,
        Encoding,
        // MDM
        MDMURL,
        UNIVERSE,
        DATACLUSTER,
        DATAMODEL,

        // FTP
        FTPHOST,
        FTPPORT,
        FTPUAERNAME,
        FTPPASSWORD,

        // DATACERT CONNECTION
        URL,
        Directory,
    }

    /*
     * LdifFile
     */
    static List<IContextParameter> getLdifFileVariables(String prefixName, LdifFileConnection conn) {
        if (conn == null || prefixName == null) {
            return Collections.emptyList();
        }
        List<IContextParameter> varList = new ArrayList<IContextParameter>();
        prefixName = prefixName + ConnectionContextHelper.LINE;
        String paramName = null;
        // TODO there is some problem for the DND
        paramName = prefixName + EParamName.FilePath;
        ConnectionContextHelper.createParameters(varList, paramName, conn.getFilePath(), JavaTypesManager.FILE);

        return varList;
    }

    static void setLdifFilePropertiesForContextMode(String prefixName, LdifFileConnection conn) {
        if (conn == null || prefixName == null) {
            return;
        }
        prefixName = prefixName + ConnectionContextHelper.LINE;
        String paramName = null;

        paramName = prefixName + EParamName.FilePath;
        conn.setFilePath(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
    }

    static void revertLdifFilePropertiesForContextMode(LdifFileConnection conn, ContextType contextType) {
        if (conn == null || contextType == null) {
            return;
        }
        String filePath = ConnectionContextHelper.getOriginalValue(contextType, conn.getFilePath());
        filePath = TalendTextUtils.removeQuotes(filePath);
        conn.setFilePath(filePath);
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public static LdifFileConnection cloneOriginalValueLdifFileConnection(LdifFileConnection fileConn, ContextType contextType) {
        if (fileConn == null) {
            return null;
        }

        LdifFileConnection cloneConn = ConnectionFactory.eINSTANCE.createLdifFileConnection();

        String filePath = ConnectionContextHelper.getOriginalValue(contextType, fileConn.getFilePath());
        filePath = TalendTextUtils.removeQuotes(filePath);
        cloneConn.setFilePath(filePath);

        cloneConn.setLimitEntry(fileConn.getLimitEntry());
        cloneConn.setServer(fileConn.getServer());
        cloneConn.setUseLimit(fileConn.isUseLimit());

        ConnectionContextHelper.cloneConnectionProperties(fileConn, cloneConn);

        EList values = fileConn.getValue();
        if (values != null && values instanceof BasicEList) {
            cloneConn.getValue().addAll((EList) ((BasicEList) values).clone());
        }
        return cloneConn;
    }

    /*
     * get variables for context .So need to add Quotes.
     */
    static List<IContextParameter> getXmlFileVariables(String prefixName, XmlFileConnection conn) {

        if (conn == null || prefixName == null) {
            return Collections.emptyList();
        }
        List<IContextParameter> varList = new ArrayList<IContextParameter>();
        prefixName = prefixName + ConnectionContextHelper.LINE;
        String paramName = null;
        if (!conn.isInputModel()) {
            String outputFilePath = conn.getOutputFilePath();
            paramName = prefixName + EParamName.OutputFilePath;
            ConnectionContextHelper.createParameters(varList, paramName, outputFilePath, JavaTypesManager.FILE);
        } else {

            String xmlFilePath = conn.getXmlFilePath();
            String encoding = conn.getEncoding();

            if (LANGUAGE.equals(ECodeLanguage.PERL)) {
                xmlFilePath = TalendTextUtils.addQuotes(xmlFilePath);
                encoding = TalendTextUtils.addQuotes(encoding);
            }
            paramName = prefixName + EParamName.XmlFilePath;
            ConnectionContextHelper.createParameters(varList, paramName, xmlFilePath, JavaTypesManager.FILE);

            paramName = prefixName + EParamName.Encoding;
            ConnectionContextHelper.createParameters(varList, paramName, encoding);

            EList schema = conn.getSchema();
            if (schema != null) {
                Object object = schema.get(0);
                if (object instanceof XmlXPathLoopDescriptor) {
                    XmlXPathLoopDescriptor loopDesc = (XmlXPathLoopDescriptor) object;
                    paramName = prefixName + EParamName.XPathQuery;
                    String absoluteXPathQuery = loopDesc.getAbsoluteXPathQuery();
                    if (LANGUAGE.equals(ECodeLanguage.PERL)) {
                        absoluteXPathQuery = TalendTextUtils.addQuotes(absoluteXPathQuery);
                    }
                    ConnectionContextHelper.createParameters(varList, paramName, absoluteXPathQuery);
                }
            }
        }

        return varList;
    }

    static void setXmlFilePropertiesForContextMode(String prefixName, XmlFileConnection conn) {
        if (conn == null || prefixName == null) {
            return;
        }
        prefixName = prefixName + ConnectionContextHelper.LINE;
        String paramName = null;

        if (conn.isInputModel()) {
            paramName = prefixName + EParamName.XmlFilePath;
            conn.setXmlFilePath(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
            paramName = prefixName + EParamName.Encoding;
            conn.setEncoding(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
            EList schema = conn.getSchema();
            if (schema != null) {
                if (schema.get(0) instanceof XmlXPathLoopDescriptor) {
                    XmlXPathLoopDescriptor descriptor = (XmlXPathLoopDescriptor) schema.get(0);
                    paramName = prefixName + EParamName.XPathQuery;
                    descriptor.setAbsoluteXPathQuery(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
                }
            }
        } else {
            paramName = prefixName + EParamName.OutputFilePath;
            conn.setOutputFilePath(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
        }

    }

    static void revertXmlFilePropertiesForContextMode(XmlFileConnection conn, ContextType contextType) {
        if (conn == null || contextType == null) {
            return;
        }
        if (!conn.isInputModel()) {
            String outputFilePath = ConnectionContextHelper.getOriginalValue(contextType, conn.getOutputFilePath());
            outputFilePath = TalendTextUtils.removeQuotes(outputFilePath);
            conn.setOutputFilePath(outputFilePath);
        } else {
            String filePath = ConnectionContextHelper.getOriginalValue(contextType, conn.getXmlFilePath());
            String encoding = ConnectionContextHelper.getOriginalValue(contextType, conn.getEncoding());

            filePath = TalendTextUtils.removeQuotes(filePath);
            conn.setXmlFilePath(filePath);
            encoding = TalendTextUtils.removeQuotes(encoding);
            conn.setEncoding(encoding);
            EList schema = conn.getSchema();
            if (schema != null) {
                if (schema.get(0) instanceof XmlXPathLoopDescriptor) {
                    XmlXPathLoopDescriptor descriptor = (XmlXPathLoopDescriptor) schema.get(0);
                    String xPahtQuery = ConnectionContextHelper.getOriginalValue(contextType, descriptor.getAbsoluteXPathQuery());
                    xPahtQuery = TalendTextUtils.removeQuotes(xPahtQuery);
                    descriptor.setAbsoluteXPathQuery(xPahtQuery);
                }
            }

        }
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public static XmlFileConnection cloneOriginalValueXmlFileConnection(XmlFileConnection fileConn, ContextType contextType) {
        if (fileConn == null) {
            return null;
        }

        XmlFileConnection cloneConn = ConnectionFactory.eINSTANCE.createXmlFileConnection();

        String filePath = ConnectionContextHelper.getOriginalValue(contextType, fileConn.getXmlFilePath());
        String encoding = ConnectionContextHelper.getOriginalValue(contextType, fileConn.getEncoding());

        filePath = TalendTextUtils.removeQuotes(filePath);
        cloneConn.setXmlFilePath(filePath);
        encoding = TalendTextUtils.removeQuotes(encoding);
        cloneConn.setEncoding(encoding);
        //
        cloneConn.setMaskXPattern(fileConn.getMaskXPattern());
        cloneConn.setXsdFilePath(fileConn.getXsdFilePath());
        cloneConn.setGuess(fileConn.isGuess());

        ConnectionContextHelper.cloneConnectionProperties(fileConn, cloneConn);

        cloneConn.getSchema().clear();

        List<XmlXPathLoopDescriptor> descs = (List<XmlXPathLoopDescriptor>) fileConn.getSchema();
        for (XmlXPathLoopDescriptor desc : descs) {
            XmlXPathLoopDescriptor cloneDesc = ConnectionFactory.eINSTANCE.createXmlXPathLoopDescriptor();
            cloneDesc.setLimitBoucle(desc.getLimitBoucle().intValue());
            String xPathQuery = ConnectionContextHelper.getOriginalValue(contextType, desc.getAbsoluteXPathQuery());
            xPathQuery = TalendTextUtils.removeQuotes(xPathQuery);
            cloneDesc.setAbsoluteXPathQuery(xPathQuery);

            cloneDesc.getSchemaTargets().clear();
            List<SchemaTarget> schemaTargets = (List<SchemaTarget>) desc.getSchemaTargets();
            for (SchemaTarget schemaTarget : schemaTargets) {
                SchemaTarget cloneSchemaTarget = ConnectionFactory.eINSTANCE.createSchemaTarget();
                cloneSchemaTarget.setRelativeXPathQuery(schemaTarget.getRelativeXPathQuery());
                cloneSchemaTarget.setTagName(schemaTarget.getTagName());

                cloneSchemaTarget.setSchema(cloneDesc);
                cloneDesc.getSchemaTargets().add(cloneSchemaTarget);
            }

            cloneDesc.setConnection(cloneConn);
            cloneConn.getSchema().add(cloneDesc);
        }

        return cloneConn;
    }

    /*
     * Salesforce schema
     */
    static List<IContextParameter> getSalesforceVariables(String prefixName, SalesforceSchemaConnection ssConn) {
        if (ssConn == null || prefixName == null) {
            return Collections.emptyList();
        }
        List<IContextParameter> varList = new ArrayList<IContextParameter>();
        prefixName = prefixName + ConnectionContextHelper.LINE;
        String paramName = null;

        paramName = prefixName + EParamName.WebServiceUrl;
        ConnectionContextHelper.createParameters(varList, paramName, ssConn.getWebServiceUrl());

        paramName = prefixName + EParamName.UserName;
        ConnectionContextHelper.createParameters(varList, paramName, ssConn.getUserName());

        paramName = prefixName + EParamName.Password;
        ConnectionContextHelper.createParameters(varList, paramName, ssConn.getPassword(), JavaTypesManager.PASSWORD);

        paramName = prefixName + EParamName.BatchSize;
        ConnectionContextHelper.createParameters(varList, paramName, ssConn.getBatchSize());

        paramName = prefixName + EParamName.TimeOut;
        ConnectionContextHelper.createParameters(varList, paramName, ssConn.getTimeOut());

        paramName = prefixName + EParamName.QueryCondition;
        ConnectionContextHelper.createParameters(varList, paramName, ssConn.getQueryCondition());

        return varList;
    }

    static void setMDMPropertiesForContextMode(String prefixName, MDMConnection conn) {
        // if (conn == null || prefixName == null) {
        // return;
        // }
        // prefixName = prefixName + ConnectionContextHelper.LINE;
        // String paramName = null;
        //
        // paramName = prefixName + EParamName.XmlFilePath;
        // conn.setXmlFilePath(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
        //
        // EList schema = conn.getSchema();
        // if (schema != null) {
        // if (schema.get(0) instanceof XmlXPathLoopDescriptor) {
        // XmlXPathLoopDescriptor descriptor = (XmlXPathLoopDescriptor) schema.get(0);
        // paramName = prefixName + EParamName.XPathQuery;
        // descriptor.setAbsoluteXPathQuery(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
        // }
        // }
        //
        // paramName = prefixName + EParamName.Encoding;
        // conn.setEncoding(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));

    }

    static List<IContextParameter> getMDMSchemaVariables(String prefixName, MDMConnection conn) {
        if (conn == null || prefixName == null) {
            return Collections.emptyList();
        }
        List<IContextParameter> varList = new ArrayList<IContextParameter>();
        prefixName = prefixName + ConnectionContextHelper.LINE;
        String paramName = null;

        paramName = prefixName + EParamName.UNIVERSE;
        ConnectionContextHelper.createParameters(varList, paramName, conn.getUniverse());

        paramName = prefixName + EParamName.DATACLUSTER;
        ConnectionContextHelper.createParameters(varList, paramName, conn.getDatacluster());

        paramName = prefixName + EParamName.DATAMODEL;
        ConnectionContextHelper.createParameters(varList, paramName, conn.getDatamodel());

        paramName = prefixName + EParamName.MDMURL;
        String url = "http://" + conn.getServer() + ":" + conn.getPort() + "/talend/TalendPort"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ConnectionContextHelper.createParameters(varList, paramName, url);

        return varList;
    }

    static List<IContextParameter> getFTPSChemaVariables(String prefixName, FTPConnection conn) {
        if (conn == null || prefixName == null) {
            return Collections.emptyList();
        }
        List<IContextParameter> varList = new ArrayList<IContextParameter>();
        prefixName = prefixName + ConnectionContextHelper.LINE;
        String paramName = null;

        paramName = prefixName + EParamName.FTPHOST;
        ConnectionContextHelper.createParameters(varList, paramName, conn.getHost());

        paramName = prefixName + EParamName.FTPPORT;
        ConnectionContextHelper.createParameters(varList, paramName, conn.getPort());

        paramName = prefixName + EParamName.FTPUAERNAME;
        ConnectionContextHelper.createParameters(varList, paramName, conn.getUsername());

        paramName = prefixName + EParamName.FTPPASSWORD;
        ConnectionContextHelper.createParameters(varList, paramName, conn.getPassword());

        return varList;
    }

    static void setSalesforcePropertiesForContextMode(String prefixName, SalesforceSchemaConnection ssConn) {
        if (ssConn == null || prefixName == null) {
            return;
        }
        prefixName = prefixName + ConnectionContextHelper.LINE;
        String paramName = null;

        paramName = prefixName + EParamName.WebServiceUrl;
        ssConn.setWebServiceUrl(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));

        paramName = prefixName + EParamName.UserName;
        ssConn.setUserName(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));

        paramName = prefixName + EParamName.Password;
        ssConn.setPassword(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));

        paramName = prefixName + EParamName.BatchSize;
        ssConn.setBatchSize(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));

        paramName = prefixName + EParamName.TimeOut;
        ssConn.setTimeOut(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));

        paramName = prefixName + EParamName.QueryCondition;
        ssConn.setQueryCondition(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
    }

    static void revertSalesforcePropertiesForContextMode(SalesforceSchemaConnection ssConn, ContextType contextType) {
        if (ssConn == null || contextType == null) {
            return;
        }
        String url = ConnectionContextHelper.getOriginalValue(contextType, ssConn.getWebServiceUrl());
        String userName = ConnectionContextHelper.getOriginalValue(contextType, ssConn.getUserName());
        String password = ConnectionContextHelper.getOriginalValue(contextType, ssConn.getPassword());
        String batchSize = ConnectionContextHelper.getOriginalValue(contextType, ssConn.getBatchSize());
        String queryCondition = ConnectionContextHelper.getOriginalValue(contextType, ssConn.getQueryCondition());

        ssConn.setWebServiceUrl(url);
        ssConn.setUserName(userName);
        ssConn.setPassword(password);
        ssConn.setBatchSize(batchSize);
        ssConn.setQueryCondition(queryCondition);
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public static SalesforceSchemaConnection cloneOriginalValueSalesforceConnection(SalesforceSchemaConnection ssConn,
            ContextType contextType) {
        if (ssConn == null) {
            return null;
        }

        SalesforceSchemaConnection cloneConn = ConnectionFactory.eINSTANCE.createSalesforceSchemaConnection();

        String url = ConnectionContextHelper.getOriginalValue(contextType, ssConn.getWebServiceUrl());
        String userName = ConnectionContextHelper.getOriginalValue(contextType, ssConn.getUserName());
        String password = ConnectionContextHelper.getOriginalValue(contextType, ssConn.getPassword());
        String batchSize = ConnectionContextHelper.getOriginalValue(contextType, ssConn.getBatchSize());
        String timeOut = ConnectionContextHelper.getOriginalValue(contextType, ssConn.getTimeOut());
        String queryCondition = ConnectionContextHelper.getOriginalValue(contextType, ssConn.getQueryCondition());

        cloneConn.setWebServiceUrl(url);
        cloneConn.setUserName(userName);
        cloneConn.setPassword(password);
        cloneConn.setBatchSize(batchSize);
        cloneConn.setTimeOut(timeOut);
        cloneConn.setQueryCondition(queryCondition);

        ConnectionContextHelper.cloneConnectionProperties(ssConn, cloneConn);

        cloneConn.setModuleName(ssConn.getModuleName());

        return cloneConn;
    }

    /*
     * LDAP schema
     */
    static List<IContextParameter> getLDAPSchemaVariables(String prefixName, LDAPSchemaConnection ldapConn) {
        if (ldapConn == null || prefixName == null) {
            return Collections.emptyList();
        }
        List<IContextParameter> varList = new ArrayList<IContextParameter>();
        prefixName = prefixName + ConnectionContextHelper.LINE;
        String paramName = null;

        paramName = prefixName + EParamName.Host;
        ConnectionContextHelper.createParameters(varList, paramName, ldapConn.getHost());

        paramName = prefixName + EParamName.Port;
        ConnectionContextHelper.createParameters(varList, paramName, ldapConn.getPort(), JavaTypesManager.INTEGER);

        paramName = prefixName + EParamName.BindPassword;
        ConnectionContextHelper.createParameters(varList, paramName, ldapConn.getBindPassword(), JavaTypesManager.PASSWORD);

        paramName = prefixName + EParamName.BindPrincipal;
        ConnectionContextHelper.createParameters(varList, paramName, ldapConn.getBindPrincipal());

        paramName = prefixName + EParamName.Filter;
        ConnectionContextHelper.createParameters(varList, paramName, ldapConn.getFilter());

        paramName = prefixName + EParamName.TimeOutLimit;
        ConnectionContextHelper.createParameters(varList, paramName, ldapConn.getTimeOutLimit(), JavaTypesManager.INTEGER);

        paramName = prefixName + EParamName.CountLimit;
        ConnectionContextHelper.createParameters(varList, paramName, ldapConn.getCountLimit(), JavaTypesManager.INTEGER);
        return varList;
    }

    static void setLDAPSchemaPropertiesForContextMode(String prefixName, LDAPSchemaConnection ldapConn) {
        if (ldapConn == null || prefixName == null) {
            return;
        }
        prefixName = prefixName + ConnectionContextHelper.LINE;
        String paramName = null;

        paramName = prefixName + EParamName.Host;
        ldapConn.setHost(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));

        paramName = prefixName + EParamName.Port;
        ldapConn.setPort(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));

        paramName = prefixName + EParamName.BindPrincipal;
        ldapConn.setBindPrincipal(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));

        paramName = prefixName + EParamName.BindPassword;
        ldapConn.setBindPassword(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));

        paramName = prefixName + EParamName.CountLimit;
        ldapConn.setCountLimit(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));

        paramName = prefixName + EParamName.TimeOutLimit;
        ldapConn.setTimeOutLimit(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));

        paramName = prefixName + EParamName.Filter;
        ldapConn.setFilter(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
    }

    static void revertLDAPSchemaPropertiesForContextMode(LDAPSchemaConnection ldapConn, ContextType contextType) {
        if (ldapConn == null || contextType == null) {
            return;
        }
        String host = ConnectionContextHelper.getOriginalValue(contextType, ldapConn.getHost());
        String port = ConnectionContextHelper.getOriginalValue(contextType, ldapConn.getPort());
        String bindPrincipal = ConnectionContextHelper.getOriginalValue(contextType, ldapConn.getBindPrincipal());
        String bindPassword = ConnectionContextHelper.getOriginalValue(contextType, ldapConn.getBindPassword());
        String filter = ConnectionContextHelper.getOriginalValue(contextType, ldapConn.getFilter());
        String countLimit = ConnectionContextHelper.getOriginalValue(contextType, ldapConn.getCountLimit());
        String timeOutLimit = ConnectionContextHelper.getOriginalValue(contextType, ldapConn.getTimeOutLimit());

        ldapConn.setHost(host);
        ldapConn.setPort(port);
        ldapConn.setBindPrincipal(bindPrincipal);
        ldapConn.setBindPassword(bindPassword);
        ldapConn.setFilter(filter);
        ldapConn.setCountLimit(countLimit);
        ldapConn.setTimeOutLimit(timeOutLimit);
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public static LDAPSchemaConnection cloneOriginalValueLDAPSchemaConnection(LDAPSchemaConnection ldapConn,
            ContextType contextType) {
        if (ldapConn == null) {
            return null;
        }

        LDAPSchemaConnection cloneConn = ConnectionFactory.eINSTANCE.createLDAPSchemaConnection();

        String host = ConnectionContextHelper.getOriginalValue(contextType, ldapConn.getHost());
        String port = ConnectionContextHelper.getOriginalValue(contextType, ldapConn.getPort());
        String bindPrincipal = ConnectionContextHelper.getOriginalValue(contextType, ldapConn.getBindPrincipal());
        String bindPassword = ConnectionContextHelper.getOriginalValue(contextType, ldapConn.getBindPassword());
        String filter = ConnectionContextHelper.getOriginalValue(contextType, ldapConn.getFilter());
        String countLimit = ConnectionContextHelper.getOriginalValue(contextType, ldapConn.getCountLimit());
        String timeOutLimit = ConnectionContextHelper.getOriginalValue(contextType, ldapConn.getTimeOutLimit());

        cloneConn.setHost(host);
        cloneConn.setPort(port);
        cloneConn.setBindPrincipal(bindPrincipal);
        cloneConn.setBindPassword(bindPassword);
        cloneConn.setFilter(filter);
        cloneConn.setCountLimit(countLimit);
        cloneConn.setTimeOutLimit(timeOutLimit);

        cloneConn.setSeparator(ldapConn.getSeparator());
        cloneConn.setProtocol(ldapConn.getProtocol());
        cloneConn.setSelectedDN(ldapConn.getSelectedDN());
        cloneConn.setStorePath(ldapConn.getStorePath());

        cloneConn.setAliases(ldapConn.getAliases());
        cloneConn.setReferrals(ldapConn.getReferrals());
        cloneConn.setEncryptionMethodName(ldapConn.getEncryptionMethodName());
        cloneConn.setGetBaseDNsFromRoot(ldapConn.isGetBaseDNsFromRoot());
        cloneConn.setLimitValue(ldapConn.getLimitValue());
        cloneConn.setSavePassword(ldapConn.isSavePassword());
        cloneConn.setUseAdvanced(ldapConn.isUseAdvanced());
        cloneConn.setUseAuthen(ldapConn.isUseAuthen());
        cloneConn.setUseLimit(ldapConn.isUseLimit());

        ConnectionContextHelper.cloneConnectionProperties(ldapConn, cloneConn);

        EList baseDNs = ldapConn.getBaseDNs();
        if (baseDNs != null && baseDNs instanceof BasicEList) {
            cloneConn.getBaseDNs().addAll((EList) ((BasicEList) baseDNs).clone());
        }

        EList values = ldapConn.getValue();
        if (values != null && values instanceof BasicEList) {
            cloneConn.getValue().addAll((EList) ((BasicEList) values).clone());
        }
        EList returnAttributes = ldapConn.getReturnAttributes();
        if (returnAttributes != null && returnAttributes instanceof BasicEList) {
            cloneConn.getReturnAttributes().addAll((EList) ((BasicEList) returnAttributes).clone());
        }
        return cloneConn;
    }

    public static FTPConnection cloneOriginalValueFTPConnection(FTPConnection ftpConn, ContextType contextType) {
        if (ftpConn == null) {
            return null;
        }
        FTPConnection cloneConn = ConnectionFactory.eINSTANCE.createFTPConnection();
        String host = ConnectionContextHelper.getOriginalValue(contextType, ftpConn.getHost());
        String port = ConnectionContextHelper.getOriginalValue(contextType, ftpConn.getPort());
        String userName = ConnectionContextHelper.getOriginalValue(contextType, ftpConn.getUsername());
        String password = ConnectionContextHelper.getOriginalValue(contextType, ftpConn.getPassword());

        cloneConn.setHost(host);
        cloneConn.setPort(port);
        cloneConn.setUsername(userName);
        cloneConn.setPassword(password);

        return cloneConn;
    }

    /*
     * WSDL Schema
     */
    static List<IContextParameter> getWSDLSchemaVariables(String prefixName, WSDLSchemaConnection wsdlConn) {
        if (wsdlConn == null || prefixName == null) {
            return Collections.emptyList();
        }
        List<IContextParameter> varList = new ArrayList<IContextParameter>();
        prefixName = prefixName + ConnectionContextHelper.LINE;
        String paramName = null;

        paramName = prefixName + EParamName.WSDL;
        ConnectionContextHelper.createParameters(varList, paramName, wsdlConn.getWSDL());
        if (wsdlConn.isIsInputModel()) {
            paramName = prefixName + EParamName.MethodName;
            ConnectionContextHelper.createParameters(varList, paramName, wsdlConn.getMethodName());
        }
        switch (LanguageManager.getCurrentLanguage()) {
        case JAVA:
            if (wsdlConn.isIsInputModel()) {
                paramName = prefixName + EParamName.UserName;
                ConnectionContextHelper.createParameters(varList, paramName, wsdlConn.getUserName());

                paramName = prefixName + EParamName.Password;
                ConnectionContextHelper.createParameters(varList, paramName, wsdlConn.getPassword(), JavaTypesManager.PASSWORD);

                paramName = prefixName + EParamName.ProxyHost;
                ConnectionContextHelper.createParameters(varList, paramName, wsdlConn.getProxyHost());

                paramName = prefixName + EParamName.ProxyPort;
                ConnectionContextHelper.createParameters(varList, paramName, wsdlConn.getProxyPort(), JavaTypesManager.INTEGER);

                paramName = prefixName + EParamName.ProxyUser;
                ConnectionContextHelper.createParameters(varList, paramName, wsdlConn.getProxyUser());

                paramName = prefixName + EParamName.ProxyPassword;
                ConnectionContextHelper.createParameters(varList, paramName, wsdlConn.getProxyPassword(),
                        JavaTypesManager.PASSWORD);
            }
            break;
        case PERL:
        default:
            paramName = prefixName + EParamName.EndpointURI;
            ConnectionContextHelper.createParameters(varList, paramName, wsdlConn.getEndpointURI());

            paramName = prefixName + EParamName.Encoding;
            ConnectionContextHelper.createParameters(varList, paramName, wsdlConn.getEncoding());
            break;
        }

        return varList;
    }

    static void setWSDLSchemaPropertiesForContextMode(String prefixName, WSDLSchemaConnection wsdlConn) {
        if (wsdlConn == null || prefixName == null) {
            return;
        }
        prefixName = prefixName + ConnectionContextHelper.LINE;
        String paramName = null;

        paramName = prefixName + EParamName.WSDL;
        wsdlConn.setWSDL(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
        if (wsdlConn.isIsInputModel()) {
            paramName = prefixName + EParamName.MethodName;
            wsdlConn.setMethodName(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
        }
        switch (LanguageManager.getCurrentLanguage()) {
        case JAVA:
            if (wsdlConn.isIsInputModel()) {
                paramName = prefixName + EParamName.UserName;
                wsdlConn.setUserName(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));

                paramName = prefixName + EParamName.Password;
                wsdlConn.setPassword(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));

                paramName = prefixName + EParamName.ProxyHost;
                wsdlConn.setProxyHost(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));

                paramName = prefixName + EParamName.ProxyPort;
                wsdlConn.setProxyPort(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));

                paramName = prefixName + EParamName.ProxyUser;
                wsdlConn.setProxyUser(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));

                paramName = prefixName + EParamName.ProxyPassword;
                wsdlConn.setProxyPassword(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
            }
            break;
        case PERL:
        default:
            paramName = prefixName + EParamName.EndpointURI;
            wsdlConn.setEndpointURI(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));

            paramName = prefixName + EParamName.Encoding;
            wsdlConn.setEncoding(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
            break;
        }

    }

    static void revertWSDLSchemaPropertiesForContextMode(WSDLSchemaConnection wsdlConn, ContextType contextType) {
        if (wsdlConn == null || contextType == null) {
            return;
        }
        String wsdl = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getWSDL());
        if (wsdlConn.isIsInputModel()) {
            String username = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getUserName());
            String password = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getPassword());
            String proxyHost = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getProxyHost());
            String proxyPort = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getProxyPort());
            String proxyUser = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getProxyUser());
            String proxyPassword = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getProxyPassword());
            String methodName = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getMethodName());
            String encoding = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getEncoding());
            String endpointURL = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getEndpointURI());

            wsdlConn.setUserName(username);
            wsdlConn.setPassword(password);
            wsdlConn.setProxyHost(proxyHost);
            wsdlConn.setProxyPort(proxyPort);
            wsdlConn.setProxyUser(proxyUser);
            wsdlConn.setProxyPassword(proxyPassword);
            wsdlConn.setMethodName(methodName);
            wsdlConn.setEncoding(encoding);
            wsdlConn.setEndpointURI(endpointURL);
        }
        wsdlConn.setWSDL(wsdl);
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public static WSDLSchemaConnection cloneOriginalValueWSDLSchemaConnection(WSDLSchemaConnection wsdlConn,
            ContextType contextType) {
        if (wsdlConn == null) {
            return null;
        }

        WSDLSchemaConnection cloneConn = ConnectionFactory.eINSTANCE.createWSDLSchemaConnection();

        String wsdl = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getWSDL());
        if (wsdlConn.isIsInputModel()) {
            String username = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getUserName());
            String password = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getPassword());
            String proxyHost = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getProxyHost());
            String proxyPort = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getProxyPort());
            String proxyUser = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getProxyUser());
            String proxyPassword = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getProxyPassword());
            String methodName = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getMethodName());
            String encoding = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getEncoding());
            String endpointURL = ConnectionContextHelper.getOriginalValue(contextType, wsdlConn.getEndpointURI());

            cloneConn.setUserName(username);
            cloneConn.setPassword(password);
            cloneConn.setProxyHost(proxyHost);
            cloneConn.setProxyPort(proxyPort);
            cloneConn.setProxyUser(proxyUser);
            cloneConn.setProxyPassword(proxyPassword);
            cloneConn.setMethodName(methodName);
            cloneConn.setEncoding(encoding);
            cloneConn.setEndpointURI(endpointURL);

            cloneConn.setNeedAuth(wsdlConn.isNeedAuth());
            cloneConn.setUseProxy(wsdlConn.isUseProxy());
        }
        cloneConn.setWSDL(wsdl);
        ConnectionContextHelper.cloneConnectionProperties(wsdlConn, cloneConn);

        cloneConn.setParameters((ArrayList) wsdlConn.getParameters().clone());
        EList values = wsdlConn.getValue();
        if (values != null && values instanceof BasicEList) {
            cloneConn.getValue().addAll((EList) ((BasicEList) values).clone());
        }
        return cloneConn;
    }

    public static XmlFileConnection getOriginalValueConnection(XmlFileConnection connection, ConnectionItem connectionItem,
            boolean isContextMode, boolean defaultContext) {
        if (isContextMode) {
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection(),
                    defaultContext);
            return (XmlFileConnection) OtherConnectionContextUtils.cloneOriginalValueXmlFileConnection(connection, contextType);
        }
        return connection;

    }
}
