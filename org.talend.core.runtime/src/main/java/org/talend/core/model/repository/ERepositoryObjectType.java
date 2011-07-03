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
package org.talend.core.model.repository;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EObject;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.AbstractDQModelService;
import org.talend.core.ICamelItemService;
import org.talend.core.PluginChecker;
import org.talend.core.model.properties.BRMSConnectionItem;
import org.talend.core.model.properties.BusinessProcessItem;
import org.talend.core.model.properties.CSVFileConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.DelimitedFileConnectionItem;
import org.talend.core.model.properties.DocumentationItem;
import org.talend.core.model.properties.EDIFACTConnectionItem;
import org.talend.core.model.properties.EbcdicConnectionItem;
import org.talend.core.model.properties.ExcelFileConnectionItem;
import org.talend.core.model.properties.FTPConnectionItem;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.GenericSchemaConnectionItem;
import org.talend.core.model.properties.HL7ConnectionItem;
import org.talend.core.model.properties.HeaderFooterConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobDocumentationItem;
import org.talend.core.model.properties.JobScriptItem;
import org.talend.core.model.properties.JobletDocumentationItem;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.LDAPSchemaConnectionItem;
import org.talend.core.model.properties.LdifFileConnectionItem;
import org.talend.core.model.properties.LinkDocumentationItem;
import org.talend.core.model.properties.LinkRulesItem;
import org.talend.core.model.properties.MDMConnectionItem;
import org.talend.core.model.properties.PositionalFileConnectionItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.RegExFileConnectionItem;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.RulesItem;
import org.talend.core.model.properties.SAPConnectionItem;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.properties.SVGBusinessProcessItem;
import org.talend.core.model.properties.SalesforceSchemaConnectionItem;
import org.talend.core.model.properties.SnippetItem;
import org.talend.core.model.properties.SnippetVariable;
import org.talend.core.model.properties.TDQItem;
import org.talend.core.model.properties.ValidationRulesConnectionItem;
import org.talend.core.model.properties.WSDLSchemaConnectionItem;
import org.talend.core.model.properties.XmlFileConnectionItem;
import org.talend.core.model.properties.util.PropertiesSwitch;
import org.talend.core.repository.IExtendRepositoryNode;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.runtime.i18n.Messages;

/**
 * DOC hywang class global comment. Detailled comment
 */
public class ERepositoryObjectType extends DynaEnum<ERepositoryObjectType> {

    public final static ERepositoryObjectType SVN_ROOT = new ERepositoryObjectType("repository.svnroot", "", "SVN_ROOT", true, 1,
            new String[] { "DI", "CAMEL" }, false);

    public final static ERepositoryObjectType BUSINESS_PROCESS = new ERepositoryObjectType("repository.businessProcess",
            "businessProcess", "BUSINESS_PROCESS", true, 2, new String[] { "DI" });

    public final static ERepositoryObjectType SVG_BUSINESS_PROCESS = new ERepositoryObjectType("repository.svgBusinessProcess",
            "businessProcessSVG", "SVG_BUSINESS_PROCESS", true, 3, new String[] { "DI" });

    public final static ERepositoryObjectType PROCESS = new ERepositoryObjectType("repository.process", "process", "PROCESS",
            true, 4, new String[] { "DI" });

    // public final static ERepositoryObjectType ROUTES = new ERepositoryObjectType("repository.routes", "routes",
    // "ROUTES", true,
    // 5, new String[] { "DI" });

    public final static ERepositoryObjectType CONTEXT = new ERepositoryObjectType("repository.context", "context", "CONTEXT",
            true, 6, new String[] { "DI", "CAMEL" });

    public final static ERepositoryObjectType CODE = new ERepositoryObjectType("repository.code", "code", "CODE", true, 7,
            new String[] { "DI", "CAMEL" });

    public final static ERepositoryObjectType ROUTINES = new ERepositoryObjectType("repository.routines", "code/routines",
            "ROUTINES", true, 7, new String[] { "DI" });

    public final static ERepositoryObjectType JOB_SCRIPT = new ERepositoryObjectType("repository.jobscript", "code/jobscripts",
            "JOB_SCRIPT", true, 9, new String[] { "DI" });

    public final static ERepositoryObjectType SNIPPETS = new ERepositoryObjectType("repository.snippets", "code/snippets",
            "SNIPPETS", true, 10, new String[] { "DI" }, false);

    public final static ERepositoryObjectType DOCUMENTATION = new ERepositoryObjectType("repository.documentation",
            "documentations", "DOCUMENTATION", true, 11, new String[] { "DI" });

    public final static ERepositoryObjectType METADATA = new ERepositoryObjectType("repository.metadata", "metadata", "METADATA",
            true, 12, new String[] { "DI", "DQ" }, false);

    public final static ERepositoryObjectType METADATA_CON_TABLE = new ERepositoryObjectType("repository.metadataTable",
            "METADATA_CON_TABLE", 13, true, true, new String[] { "DI" }, false);

    public final static ERepositoryObjectType METADATA_CON_COLUMN = new ERepositoryObjectType("repository.metadataColumn",
            "METADATA_CON_COLUMN", 14, true, true, new String[] { "DI" }, false);

    public final static ERepositoryObjectType METADATA_CON_VIEW = new ERepositoryObjectType("repository.metadataView",
            "METADATA_CON_VIEW", 15, true, true, new String[] { "DI" }, false);

    public final static ERepositoryObjectType METADATA_CON_CATALOG = new ERepositoryObjectType("repository.metadataCatalog",
            "METADATA_CON_CATALOG", 16, true, true, new String[] { "DI" }, false);

    public final static ERepositoryObjectType METADATA_CON_SCHEMA = new ERepositoryObjectType("repository.metadataSchema",
            "METADATA_CON_SCHEMA", 17, true, true, new String[] { "DI" }, false);

    public final static ERepositoryObjectType METADATA_CON_SYNONYM = new ERepositoryObjectType("repository.synonym",
            "METADATA_CON_SYNONYM", 18, true, true, new String[] { "DI" }, false);

    public final static ERepositoryObjectType METADATA_CON_QUERY = new ERepositoryObjectType("repository.query",
            "METADATA_CON_QUERY", 19, true, true, new String[] { "DI" }, false);

    public final static ERepositoryObjectType METADATA_CON_CDC = new ERepositoryObjectType("repository.CDC", "METADATA_CON_CDC",
            20, true, true, new String[] { "DI" }, false);

    public final static ERepositoryObjectType METADATA_SAP_FUNCTION = new ERepositoryObjectType(
            "repository.SAPFunction", "METADATA_SAP_FUNCTION", 21, true, true, new String[] { "DI" }, false); //$NON-NLS-1$

    public final static ERepositoryObjectType METADATA_SAP_IDOC = new ERepositoryObjectType("repository.SAPIDoc",
            "METADATA_SAP_IDOC", 22, true, true, new String[] { "DI" }, false);

    public final static ERepositoryObjectType MDM_CONCEPT = new ERepositoryObjectType(
            "repository.concept", "MDM_CONCEPT", 23, true, true, new String[] { "DI" }, false); //$NON-NLS-1$

    public final static ERepositoryObjectType MDM_SCHEMA = new ERepositoryObjectType(
            "repository.xmlSchema", "MDM_SCHEMA", 24, true, true, new String[] { "DI" }, false); //$NON-NLS-1$

    public final static ERepositoryObjectType MDM_ELEMENT_TYPE = new ERepositoryObjectType(
            "repository.xmlElementType", "MDM_ELEMENT_TYPE", 25, true, true, new String[] { "DI" }, false);//$NON-NLS-1$

    public final static ERepositoryObjectType RECYCLE_BIN = new ERepositoryObjectType("repository.recyclebin", "", "RECYCLE_BIN",
            true, 26, new String[] { "DI", "CAMEL" }, false);

    public final static ERepositoryObjectType METADATA_COLUMN = new ERepositoryObjectType("repository.column", "",
            "METADATA_COLUMN", true, 27, new String[] { "DI" }, false);

    // feature 0006484 add
    public final static ERepositoryObjectType METADATA_FILE_RULES = new ERepositoryObjectType(
            "repository.metadataFileRules", "metadata/rules", "METADATA_FILE_RULES", 28, true, "repository.metadataFileRules.alias", new String[] { "DI" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_FILE_LINKRULES = new ERepositoryObjectType(
            "repository.metadataLinkFileRules", "metadata/rules", "METADATA_FILE_LINKRULES", 29, true, "repository.metadataLinkFileRules.alias", new String[] { "DI" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_RULES_MANAGEMENT = new ERepositoryObjectType(
            "repository.metadataRulesManagement", "", "METADATA_RULES_MANAGEMENT", 30, true, "repository.metadataRulesManagement.alias", new String[] { "DI" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_CONNECTIONS = new ERepositoryObjectType(
            "repository.metadataConnections", "metadata/connections", "METADATA_CONNECTIONS", 31, true, "repository.metadataConnections.alias", new String[] { "DI", "DQ" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_SAPCONNECTIONS = new ERepositoryObjectType(
            "repository.metadataSAPConnections", "metadata/sapconnections", "METADATA_SAPCONNECTIONS", 32, true, "repository.metadataSAPConnections.alias", new String[] { "DI" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_EDIFACT = new ERepositoryObjectType("repositorymetadataEDIFact",
            "metadata/EDISchema", "METADATA_EDIFACT", 50, true, "repositorymetadataEDIFact.alias", new String[] { "DI" });

    public final static ERepositoryObjectType SQLPATTERNS = new ERepositoryObjectType(
            "repository.metadataSQLPatterns", "sqlPatterns", "SQLPATTERNS", 33, true, "repository.metadataSQLPatterns.alias", new String[] { "DI" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_FILE_EBCDIC = new ERepositoryObjectType(
            "repository.metadataFileEDCDIC", "metadata/fileEBCDIC", "METADATA_FILE_EBCDIC", 34, true, "repository.metadataFileEDCDIC.alias", new String[] { "DI" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_FILE_HL7 = new ERepositoryObjectType(
            "repository.metadataFileHL7", "metadata/fileHL7", "METADATA_FILE_HL7", 35, true, "repository.metadataFileHL7.alias", new String[] { "DI" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_FILE_FTP = new ERepositoryObjectType(
            "repository.metadataFileFTP", "metadata/FTPconnections", "METADATA_FILE_FTP", 36, true, "repository.metadataFileFTP.alias", new String[] { "DI" }); //$NON-NLS-1$ //$NON-NLS-2$

    // 0015169 added
    public final static ERepositoryObjectType METADATA_FILE_BRMS = new ERepositoryObjectType(
            "repository.metadataFileBRMS", "metadata/BRMSconnections", "METADATA_FILE_BRMS", 37, true, "repository.metadataFileBRMS.alias", new String[] { "DI" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_MDMCONNECTION = new ERepositoryObjectType(
            "repository.metadataMDMConnections", "metadata/MDMconnections", "METADATA_MDMCONNECTION", 38, true, "repository.metadataMDMConnections.alias", new String[] { "DI", "DQ" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_FILE_DELIMITED = new ERepositoryObjectType(
            "repository.metadataFileDelimited", "metadata/fileDelimited", "METADATA_FILE_DELIMITED", 39, true, "repository.metadataFileDelimited.alias", new String[] { "DI", "DQ" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_FILE_POSITIONAL = new ERepositoryObjectType(
            "repository.metadataFilePositional", "metadata/filePositional", "METADATA_FILE_POSITIONAL", 40, true, "repository.metadataFilePositional.alias", new String[] { "DI" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_FILE_REGEXP = new ERepositoryObjectType(
            "repository.metadataFileRegexp", "metadata/fileRegex", "METADATA_FILE_REGEXP", 41, true, "repository.metadataFileRegexp.alias", new String[] { "DI" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_FILE_XML = new ERepositoryObjectType(
            "repository.metadataFileXml", "metadata/fileXml", "METADATA_FILE_XML", 42, true, "repository.metadataFileXml.alias", new String[] { "DI" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_FILE_LDIF = new ERepositoryObjectType(
            "repository.metadataFileLdif", "metadata/fileLdif", "METADATA_FILE_LDIF", 43, true, "repository.metadataFileLdif.alias", new String[] { "DI" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_FILE_EXCEL = new ERepositoryObjectType(
            "repository.metadataFileExcel", "metadata/fileExcel", "METADATA_FILE_EXCEL", 44, true, "repository.metadataFileExcel.alias", new String[] { "DI" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_SALESFORCE_SCHEMA = new ERepositoryObjectType(
            "repository.metadataSalesforceSchema", "metadata/SalesforceSchema", "METADATA_SALESFORCE_SCHEMA", 45, true, "repository.metadataSalesforceSchema.alias", new String[] { "DI" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_GENERIC_SCHEMA = new ERepositoryObjectType(
            "repository.metadataGenericSchema", "metadata/genericSchema", "METADATA_GENERIC_SCHEMA", 46, true, "repository.metadataGenericSchema.alias", new String[] { "DI" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_LDAP_SCHEMA = new ERepositoryObjectType(
            "repository.metadataLDAPSchema", "metadata/LDAPSchema", "METADATA_LDAP_SCHEMA", 47, true, "repository.metadataLDAPSchema.alias", new String[] { "DI" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_VALIDATION_RULES = new ERepositoryObjectType(
            "repository.metadataValidationRules", "metadata/validationRules", "METADATA_VALIDATION_RULES", 48, true, "repository.metadataValidationRules.alias", new String[] { "DI" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_VALIDATIONS_RULES_FOLDER = new ERepositoryObjectType(
            "repository.metadataValidationRulesFolder", "", "METADATA_VALIDATIONS_RULES_FOLDER", 49, true, "repository.metadataValidationRulesFolder.alias", new String[] { "DI" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType FOLDER = new ERepositoryObjectType(
            "repository.folder", "", "FOLDER", true, 50, new String[] { "DI" }, false); //$NON-NLS-1$

    public final static ERepositoryObjectType REFERENCED_PROJECTS = new ERepositoryObjectType(
            "repository.referencedProjects", "", "REFERENCED_PROJECTS", 51, true, "repository.referencedProjects.alias", new String[] { "DI" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType GENERATED = new ERepositoryObjectType(
            "repository.generated", "documentations/generated", "GENERATED", true, 52, new String[] { "DI" }, false); //$NON-NLS-1$

    public final static ERepositoryObjectType JOBS = new ERepositoryObjectType(
            "repository.jobs", "documentations/generated/jobs", "JOBS", true, 53, new String[] { "DI" }, false); //$NON-NLS-1$

    public final static ERepositoryObjectType JOB_DOC = new ERepositoryObjectType(
            "repository.jobdoc", "documentations/generated/jobs", "JOB_DOC", true, 54, new String[] { "DI" }); //$NON-NLS-1$

    public final static ERepositoryObjectType JOBLET = new ERepositoryObjectType(
            "repository.joblet", "joblets", "JOBLET", true, 55, new String[] { "DI" }); //$NON-NLS-1$

    public final static ERepositoryObjectType LIBS = new ERepositoryObjectType(
            "repository.libs", "libs", "LIBS", true, 56, new String[] { "DI" }, false); //$NON-NLS-1$

    public final static ERepositoryObjectType METADATA_WSDL_SCHEMA = new ERepositoryObjectType(
            "repository.metadataWSDLSchema", "metadata/WSDLSchema", "METADATA_WSDL_SCHEMA", 57, true, "repository.metadataWSDLSchema.alias", new String[] { "DI" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType JOBLETS = new ERepositoryObjectType(
            "repository.joblets", "documentations/generated/joblets", "JOBLETS", true, 58, new String[] { "DI" }, false); //$NON-NLS-1$

    public final static ERepositoryObjectType JOBLET_DOC = new ERepositoryObjectType(
            "repository.jobletdoc", "documentations/generated/joblets", "JOBLET_DOC", true, 59, new String[] { "DI" }); //$NON-NLS-1$

    public final static ERepositoryObjectType METADATA_HEADER_FOOTER = new ERepositoryObjectType(
            "repository.headerFooterConnections", "metadata/header_footer", "METADATA_HEADER_FOOTER", 60, true, "repository.headerFooterConnections.alias", new String[] { "DI" }); //$NON-NLS-1$

    public final static ERepositoryObjectType COMPONENTS = new ERepositoryObjectType(
            "repository.components", "components", "COMPONENTS", true, 61, new String[] { "DI" }, false); //$NON-NLS-1$

    // MOD mzhao feature 9207
    public final static ERepositoryObjectType TDQ_ELEMENT = new ERepositoryObjectType(
            "repository.tdqelement", "", "TDQ_ELEMENT", 62, true, "repository.tdqelement.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    // MOD mzhao feature 13114, 2010-05-19
    public final static ERepositoryObjectType TDQ_ANALYSIS_ELEMENT = new ERepositoryObjectType(
            "repository.tdqelement.analysis", "TDQ_Data Profiling/Analyses", "TDQ_ANALYSIS_ELEMENT", 63, true, "repository.tdqelement.analysis.alias", new String[] { "DQ" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType TDQ_REPORT_ELEMENT = new ERepositoryObjectType(
            "repository.tdqelement.report", "TDQ_Data Profiling/Reports", "TDQ_REPORT_ELEMENT", 64, true, "repository.tdqelement.report.alias", new String[] { "DQ" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType TDQ_INDICATOR_ELEMENT = new ERepositoryObjectType(
            "repository.tdqelement.indicator", "TDQ_Libraries/Indicators", "TDQ_INDICATOR_ELEMENT", 66, true, "repository.tdqelement.indicator.alias", new String[] { "DQ" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType TDQ_PATTERN_ELEMENT = new ERepositoryObjectType(
            "repository.tdqelement.pattern", "TDQ_Libraries/Patterns", "TDQ_PATTERN_ELEMENT", 67, true, "repository.tdqelement.pattern.alias", new String[] { "DQ" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType TDQ_SOURCE_FILE_ELEMENT = new ERepositoryObjectType(
            "repository.tdqelement.sourceFile", "TDQ_Libraries/Source Files", "TDQ_SOURCE_FILE_ELEMENT", 68, true, "repository.tdqelement.sourceFile.alias", new String[] { "DQ" }); //$NON-NLS-1$ //$NON-NLS-2$

    // MOD zqin feature 14507
    public final static ERepositoryObjectType TDQ_JRAXML_ELEMENT = new ERepositoryObjectType(
            "repository.tdqelement.jrxml", "TDQ_Libraries/JRXML Template", "TDQ_JRAXML_ELEMENT", 69, true, "repository.tdqelement.jrxml.alias", new String[] { "DQ" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType TDQ_FOLDER_NODE = new ERepositoryObjectType(
            "repository.tdqelement.folderNode", "", "TDQ_FOLDER_NODE", 70, true, "repository.tdqelement.folderNode.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    // MOD klliu feature 15750
    public final static ERepositoryObjectType TDQ_DATA_PROFILING = new ERepositoryObjectType(
            "repository.dataprofiling", "TDQ_Data Profiling", "TDQ_DATA_PROFILING", 71, true, "repository.dataprofiling.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType TDQ_LIBRARIES = new ERepositoryObjectType(
            "repository.libraries", "TDQ_Libraries", "TDQ_LIBRARIES", 74, true, "repository.libraries.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType TDQ_PATTERN_REGEX = new ERepositoryObjectType(
            "repository.patternRegex", "TDQ_Libraries/Patterns/Regex", "TDQ_PATTERN_REGEX", 76, true, "repository.patternRegex.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType TDQ_PATTERN_SQL = new ERepositoryObjectType(
            "repository.patternSql", "TDQ_Libraries/Patterns/SQL", "TDQ_PATTERN_SQL", 77, true, "repository.patternSql.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType TDQ_RULES = new ERepositoryObjectType(
            "repository.rules", "TDQ_Libraries/Rules", "TDQ_RULES", 79, true, "repository.rules.alias", new String[] { "DQ" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType TDQ_RULES_SQL = new ERepositoryObjectType(
            "repository.rulesSql", "TDQ_Libraries/Rules/SQL", "TDQ_RULES_SQL", 80, true, "repository.rulesSql.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    // MOD klliu 2010-11-26 definition type
    public final static ERepositoryObjectType TDQ_SYSTEM_INDICATORS = new ERepositoryObjectType(
            "repository.systemIndicators", "TDQ_Libraries/Indicators/System Indicators", "TDQ_SYSTEM_INDICATORS", 83, true, "repository.systemIndicators.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType TDQ_USERDEFINE_INDICATORS = new ERepositoryObjectType(
            "repository.userDefineIndicators", "TDQ_Libraries/Indicators/User Defined Indicators", "TDQ_USERDEFINE_INDICATORS", 84, true, "repository.userDefineIndicators.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType TDQ_USERDEFINE_INDICATORS_LIB = new ERepositoryObjectType(
            "repository.userDefineIndicators.lib", "", "TDQ_USERDEFINE_INDICATORS_LIB", 85, true, "repository.userDefineIndicators.lib.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType SYSTEM_INDICATORS_ADVANCED_STATISTICS = new ERepositoryObjectType(
            "repository.systemIndicators.advancedStatistics", "TDQ_Libraries/Indicators/System Indicators/Advanced Statistics", "SYSTEM_INDICATORS_ADVANCED_STATISTICS", 86, true, "repository.systemIndicators.advancedStatistics.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType SYSTEM_INDICATORS_BUSINESS_RULES = new ERepositoryObjectType(
            "repository.systemIndicators.businessRules", "TDQ_Libraries/Indicators/System Indicators/Business Rules", "SYSTEM_INDICATORS_BUSINESS_RULES", 87, true, "repository.systemIndicators.businessRules.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType SYSTEM_INDICATORS_CORRELATION = new ERepositoryObjectType(
            "repository.systemIndicators.correlation", "TDQ_Libraries/Indicators/System Indicators/Correlation", "SYSTEM_INDICATORS_CORRELATION", 88, true, "repository.systemIndicators.correlation.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType SYSTEM_INDICATORS_FUNCTIONAL_DEPENDENCY = new ERepositoryObjectType(
            "repository.systemIndicators.functionalDependency", "TDQ_Libraries/Indicators/System Indicators/Functional Dependency", "SYSTEM_INDICATORS_FUNCTIONAL_DEPENDENCY", 89, true, "repository.systemIndicators.functionalDependency.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType SYSTEM_INDICATORS_OVERVIEW = new ERepositoryObjectType(
            "repository.systemIndicators.overview, repository.systemIndicators.overview.alias", "TDQ_Libraries/Indicators/System Indicators/Overview", "SYSTEM_INDICATORS_OVERVIEW", true, 90, new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType SYSTEM_INDICATORS_PATTERN_FINDER = new ERepositoryObjectType(
            "repository.systemIndicators.patternFinder", "TDQ_Libraries/Indicators/System Indicators/Pattern Finder", "SYSTEM_INDICATORS_PATTERN_FINDER", 91, true, "repository.systemIndicators.patternFinder.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType SYSTEM_INDICATORS_PATTERN_MATCHING = new ERepositoryObjectType(
            "repository.systemIndicators.patternMatching", "TDQ_Libraries/Indicators/System Indicators/Pattern Matching", "SYSTEM_INDICATORS_PATTERN_MATCHING", 92, true, "repository.systemIndicators.patternMatching.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType SYSTEM_INDICATORS_ROW_COMPARISON = new ERepositoryObjectType(
            "repository.systemIndicators.rowComparison", "TDQ_Libraries/Indicators/System Indicators/Row Comparison", "SYSTEM_INDICATORS_ROW_COMPARISON", 93, true, "repository.systemIndicators.rowComparison.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType SYSTEM_INDICATORS_SIMPLE_STATISTICS = new ERepositoryObjectType(
            "repository.systemIndicators.simpleStatistics", "TDQ_Libraries/Indicators/System Indicators/Simple Statistics", "SYSTEM_INDICATORS_SIMPLE_STATISTICS", 94, true, "repository.systemIndicators.simpleStatistics.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType SYSTEM_INDICATORS_SOUNDEX = new ERepositoryObjectType(
            "repository.systemIndicators.soundex", "TDQ_Libraries/Indicators/System Indicators/Soundex", "SYSTEM_INDICATORS_SOUNDEX", 95, true, "repository.systemIndicators.soundex.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType SYSTEM_INDICATORS_SUMMARY_STATISTICS = new ERepositoryObjectType(
            "repository.systemIndicators.summaryStatistics", "TDQ_Libraries/Indicators/System Indicators/Summary Statistics", "SYSTEM_INDICATORS_SUMMARY_STATISTICS", 96, true, "repository.systemIndicators.summaryStatistics.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType SYSTEM_INDICATORS_TEXT_STATISTICS = new ERepositoryObjectType(
            "repository.systemIndicators.textStatistics", "TDQ_Libraries/Indicators/System Indicators/Text Statistics", "SYSTEM_INDICATORS_TEXT_STATISTICS", 97, true, "repository.systemIndicators.textStatistics.alias", new String[] { "DQ" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType TDQ_EXCHANGE = new ERepositoryObjectType(
            "repository.tdqExchange", "TDQ_Libraries/Exchange", "TDQ_EXCHANGE", 98, true, "repository.tdqExchange.alias", new String[] { "DQ" }); //$NON-NLS-1$ //$NON-NLS-2$

    public final static ERepositoryObjectType METADATA_SALESFORCE_MODULE = new ERepositoryObjectType(
            "repository.metadataSalesforceModule", "METADATA_SALESFORCE_MODULE", 99, true, true, new String[] { "DI" }, false); //$NON-NLS-1$ //$NON-NLS-2$

    private String alias;

    private String folder = ""; //$NON-NLS-N$

    private String[] products;

    private boolean subItem;

    private boolean isResouce = true;

    static {

        try {
            initDynamicNodes(ERepositoryObjectType.class);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    /* dynamic nodes should use this constructor ,type is required */
    ERepositoryObjectType(String key, String folder, String type, boolean isStaticNode, int ordinal, String[] products,
            boolean... isResouce) {
        super(key, type, isStaticNode, ordinal);
        this.folder = folder;
        this.products = products;
        if (isResouce != null && isResouce.length == 1) {
            this.isResouce = isResouce[0];
        }
    }

    ERepositoryObjectType(String key, String type, int ordinal, boolean isStaticNode, boolean subItem, String[] products,
            boolean... isResouce) {
        super(key, type, isStaticNode, ordinal);
        this.subItem = subItem;
        this.products = products;
        if (isResouce != null && isResouce.length == 1) {
            this.isResouce = isResouce[0];
        }
    }

    ERepositoryObjectType(String key, String folder, String type, int ordinal, boolean isStaticNode, String alias,
            String[] products, boolean... isResouce) {
        this(key, folder, type, isStaticNode, ordinal, products, isResouce);
        this.alias = alias;
    }

    public static <E> DynaEnum<? extends DynaEnum<?>>[] values() {
        return values(ERepositoryObjectType.class);
    }

    /* if node from extension point,add the new RepositoryObjectType dynamiclly by using the java reflection */
    private static <E> void initDynamicNodes(Class<E> clazz) throws Exception {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IConfigurationElement[] configurationElements = registry
                .getConfigurationElementsFor("org.talend.core.repository.repository_node_provider"); //$NON-NLS-1$
        try {
            for (int i = 0; i < configurationElements.length; i++) {
                IConfigurationElement element = configurationElements[i];
                Object extensionNode = element.createExecutableExtension("class");//$NON-NLS-N$
                if (extensionNode instanceof IExtendRepositoryNode) {
                    IExtendRepositoryNode diyNode = (IExtendRepositoryNode) extensionNode;
                    String label = element.getAttribute("label");//$NON-NLS-N$
                    String type = element.getAttribute("type");//$NON-NLS-N$
                    String folder = element.getAttribute("folder");//$NON-NLS-N$
                    String isResouce = element.getAttribute("isResouce");//$NON-NLS-N$

                    String productsAttribute = element.getAttribute("products");//$NON-NLS-N$
                    String[] products = productsAttribute.split("\\|");//$NON-NLS-N$
                    boolean isResource = false;
                    if (isResouce != null) {
                        isResource = Boolean.parseBoolean(isResouce);
                    }
                    boolean[] resource = new boolean[] { isResource };
                    if (products == null) {
                        products = new String[] { productsAttribute };
                    }
                    int ordinal = diyNode.getOrdinal();
                    Constructor<E> dynamicConstructor = getConstructor(clazz, new Class[] { String.class, String.class,
                            String.class, boolean.class, int.class, String[].class, boolean[].class });
                    dynamicConstructor.newInstance(label, folder, type, false, ordinal, products, resource);

                }
            }
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E> Constructor<E> getConstructor(Class<E> clazz, Class<?>[] argTypes) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            try {
                return (Constructor<E>) c.getDeclaredConstructor(argTypes);
            } catch (Exception e) {
                continue;
            }
        }
        return null;
    }

    public String getAlias() {
        if (alias == null) {
            return null;
        }
        return Messages.getString(alias);
    }

    public static ERepositoryObjectType getTypeFromKey(String key) {
        for (ERepositoryObjectType type : (ERepositoryObjectType[]) values()) {
            if (type.getKey().equals(key)) {
                return type;
            }
        }
        return null;
    }

    public boolean hasFolder() {
        try {
            String folderName = getFolderName(this);
            if (folderName != null && !"".equals(folderName)) { //$NON-NLS-1$
                return true;
            }
        } catch (IllegalArgumentException e) { // not found
            // nothing to do
        }
        return false;
    }

    public static String getFolderName(ERepositoryObjectType type) {

        if (type == GENERATED || type == JOBS || type == JOB_DOC) {
            if ((PluginChecker.isDocumentationPluginLoaded())) {
                return type.getFolder(); //$NON-NLS-1$
            }
        }
        if (type == JOBLETS || type == JOBLET_DOC) {
            if (PluginChecker.isJobLetPluginLoaded()) {
                return type.getFolder(); //$NON-NLS-1$
            }
        } else {

            return type.getFolder();
        }
        throw new IllegalArgumentException(Messages.getString("ERepositoryObjectType.FolderNotFound", type)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static String getDeleteFolderName(ERepositoryObjectType type) {
        if (type == BUSINESS_PROCESS) {
            return "businessProcess"; //$NON-NLS-1$
        } else if (type == SVG_BUSINESS_PROCESS) {
            return "businessProcessSVG"; //$NON-NLS-1$
        } else if (type == PROCESS) {
            return "job"; //$NON-NLS-1$
        } else if (type == JOBLET) {
            return "joblet";
        } else if (type == CONTEXT) {
            return "context"; //$NON-NLS-1$
        } else if (type == ROUTINES) {
            return "routine"; //$NON-NLS-1$
        } else if (type == JOB_SCRIPT) {
            return "jobscript"; //$NON-NLS-1$
        } else if (type == SNIPPETS) {
            return "snippet"; //$NON-NLS-1$
        } else if (type == DOCUMENTATION) {
            return "documentation"; //$NON-NLS-1$
        } else if (type == SQLPATTERNS) {
            return "sqlPattern"; //$NON-NLS-1$
        } else if (type == METADATA) {
            return "metadata"; //$NON-NLS-1$
        } else if (type == METADATA_CONNECTIONS) {
            return "DB connection"; //$NON-NLS-1$
        } else if (type == METADATA_SAPCONNECTIONS) {
            return "SAPconnection"; //$NON-NLS-1$
        } else if (type == METADATA_FILE_EBCDIC) {
            return "fileEBCDIC"; //$NON-NLS-1$
        } else if (type == METADATA_FILE_HL7) {
            return "fileHL7"; //$NON-NLS-1$
        } else if (type == METADATA_FILE_FTP) {
            return "FTPconnection"; //$NON-NLS-1$
        } else if (type == METADATA_FILE_BRMS) {
            return "BRMSconnection"; //$NON-NLS-1$
        } else if (type == METADATA_MDMCONNECTION) {
            return "MDMconnection"; //$NON-NLS-1$
        } else if (type == METADATA_FILE_DELIMITED) {
            return "fileDelimited"; //$NON-NLS-1$
        } else if (type == METADATA_FILE_POSITIONAL) {
            return "filePositional"; //$NON-NLS-1$
        } else if (type == METADATA_FILE_REGEXP) {
            return "fileRegex"; //$NON-NLS-1$
        } else if (type == METADATA_FILE_XML) {
            return "fileXml"; //$NON-NLS-1$
        } else if (type == METADATA_FILE_EXCEL) {
            return "fileExcel"; //$NON-NLS-1$
        } else if (type == METADATA_FILE_LDIF) {
            return "fileLdif"; //$NON-NLS-1$
        } else if (type == METADATA_LDAP_SCHEMA) {
            return "LDAPSchema"; //$NON-NLS-1$
        } else if (type == METADATA_GENERIC_SCHEMA) {
            return "genericSchema"; //$NON-NLS-1$
        } else if (type == METADATA_WSDL_SCHEMA) {
            return "WSDLSchema"; //$NON-NLS-1$
        } else if (type == METADATA_SALESFORCE_SCHEMA) {
            return "SalesforceSchema"; //$NON-NLS-1$
        } else if (type == METADATA_FILE_RULES) {
            return "rules"; //$NON-NLS-1$
        } else if (type == METADATA_FILE_LINKRULES) {
            return "rules"; //$NON-NLS-1$
        } else if (type == METADATA_HEADER_FOOTER) {
            return "header_footer";
        } else if (type == TDQ_ANALYSIS_ELEMENT) {
            return "Analyses"; //$NON-NLS-1$
        } else if (type == TDQ_RULES_SQL) {
            return "Rules"; //$NON-NLS-1$
        } else if (type == TDQ_INDICATOR_ELEMENT) {
            return "Indicators"; //$NON-NLS-1$
        } else if (type == TDQ_PATTERN_ELEMENT) {
            return "Patterns"; //$NON-NLS-1$ 
        } else if (type == TDQ_JRAXML_ELEMENT) {
            return "JRXML Template";
        } else if (type == TDQ_REPORT_ELEMENT) {
            return "Reports"; //$NON-NLS-1$
        } else if (type == TDQ_ELEMENT) {
            return "";//$NON-NLS-1$
        } else if (type == COMPONENTS) {
            return "components";
        } else {
            return "job";
        }
        // switch (type) {
        // case BUSINESS_PROCESS:
        //            return "businessModel"; //$NON-NLS-1$
        // case SVG_BUSINESS_PROCESS:
        //            return "businessProcessSVG"; //$NON-NLS-1$
        // case PROCESS:
        //            return "job"; //$NON-NLS-1$
        // case JOBLET:
        // return "joblet";
        // case CONTEXT:
        //            return "context"; //$NON-NLS-1$
        // case ROUTINES:
        //            return "routine"; //$NON-NLS-1$
        // case JOB_SCRIPT:
        //            return "jobscript"; //$NON-NLS-1$
        // case SNIPPETS:
        //            return "snippet"; //$NON-NLS-1$
        // case DOCUMENTATION:
        //            return "documentation"; //$NON-NLS-1$
        // case SQLPATTERNS:
        //            return "sqlPattern"; //$NON-NLS-1$
        // case METADATA:
        //            return "metadata"; //$NON-NLS-1$
        // case METADATA_CONNECTIONS:
        //            return "DB connection"; //$NON-NLS-1$
        // case METADATA_SAPCONNECTIONS:
        //            return "SAPconnection"; //$NON-NLS-1$
        // case METADATA_FILE_EBCDIC:
        //            return "fileEBCDIC"; //$NON-NLS-1$
        // case METADATA_FILE_HL7:
        //            return "fileHL7"; //$NON-NLS-1$
        // case METADATA_FILE_FTP:
        //            return "FTPconnection"; //$NON-NLS-1$
        // case METADATA_FILE_BRMS:
        //            return "BRMSconnection"; //$NON-NLS-1$
        // case METADATA_MDMCONNECTION:
        //            return "MDMconnection"; //$NON-NLS-1$
        // case METADATA_FILE_DELIMITED:
        //            return "fileDelimited"; //$NON-NLS-1$
        // case METADATA_FILE_POSITIONAL:
        //            return "filePositional"; //$NON-NLS-1$
        // case METADATA_FILE_REGEXP:
        //            return "fileRegex"; //$NON-NLS-1$
        // case METADATA_FILE_XML:
        //            return "fileXml"; //$NON-NLS-1$
        // case METADATA_FILE_EXCEL:
        //            return "fileExcel"; //$NON-NLS-1$
        // case METADATA_FILE_LDIF:
        //            return "fileLdif"; //$NON-NLS-1$
        // case METADATA_LDAP_SCHEMA:
        //            return "LDAPSchema"; //$NON-NLS-1$
        // case METADATA_GENERIC_SCHEMA:
        //            return "genericSchema"; //$NON-NLS-1$
        // case METADATA_WSDL_SCHEMA:
        //            return "WSDLSchema"; //$NON-NLS-1$
        // case METADATA_SALESFORCE_SCHEMA:
        //            return "SalesforceSchema"; //$NON-NLS-1$
        // case METADATA_FILE_RULES:
        //            return "rules"; //$NON-NLS-1$
        // case METADATA_FILE_LINKRULES:
        //            return "rules"; //$NON-NLS-1$
        // // MOD mzhao feature 13114, 2010-05-19
        // case METADATA_HEADER_FOOTER:
        // return "header_footer";
        // case TDQ_ANALYSIS_ELEMENT:
        //            return "Analyses"; //$NON-NLS-1$
        // case TDQ_BUSINESSRULE_ELEMENT:
        //            return "Rules"; //$NON-NLS-1$
        // case TDQ_INDICATOR_ELEMENT:
        //            return "Indicators"; //$NON-NLS-1$
        // case TDQ_PATTERN_ELEMENT:
        //            return "Patterns"; //$NON-NLS-1$ 
        // case TDQ_JRAXML_ELEMENT:
        // return "JRXML Template";
        // case TDQ_REPORT_ELEMENT:
        //            return "Reports"; //$NON-NLS-1$
        // // MOD mzhao feature 9207
        // case TDQ_ELEMENT:
        //            return "";//$NON-NLS-1$
        // case COMPONENTS:
        // return "components";
        // default:
        // return "job";
        // }
    }

    public static ERepositoryObjectType getItemType(Item item) {

        ERepositoryObjectType repObjType = getTDQRepObjType(item);
        if (repObjType != null) {
            return repObjType;
        }
        repObjType = getCamelRepObjectType(item);
        if (repObjType != null) {
            return repObjType;
        }
        return (ERepositoryObjectType) new PropertiesSwitch() {

            @Override
            public Object caseFolderItem(FolderItem object) {
                return FOLDER;
            }

            public Object caseDocumentationItem(DocumentationItem object) {
                return DOCUMENTATION;
            }

            @Override
            public Object caseLinkDocumentationItem(LinkDocumentationItem object) {
                return DOCUMENTATION;
            }

            @Override
            public Object caseRulesItem(RulesItem object) {
                return METADATA_FILE_RULES;
            }

            @Override
            public Object caseLinkRulesItem(LinkRulesItem object) {
                return METADATA_FILE_LINKRULES;
            }

            /*
             * (non-Javadoc)
             * 
             * @seeorg.talend.core.model.properties.util.PropertiesSwitch# caseJobDocumentationItem
             * (org.talend.core.model.properties.JobDocumentationItem)
             */
            public Object caseJobDocumentationItem(JobDocumentationItem object) {
                return JOB_DOC;
            }

            /*
             * (non-Javadoc)
             * 
             * @seeorg.talend.core.model.properties.util.PropertiesSwitch# caseJobletDocumentationItem
             * (org.talend.core.model.properties.JobletDocumentationItem)
             */
            public Object caseJobletDocumentationItem(JobletDocumentationItem object) {
                return JOBLET_DOC;
            }

            public Object caseRoutineItem(RoutineItem object) {
                return ROUTINES;
            }

            // public Object caseBeanItem(BeanItem object) {
            // return BEANS;
            // }

            public Object caseJobScriptItem(JobScriptItem object) {
                return JOB_SCRIPT;
            }

            /*
             * (non-Javadoc)
             * 
             * @seeorg.talend.core.model.properties.util.PropertiesSwitch# caseSQLPatternItem
             * (org.talend.core.model.properties.SQLPatternItem)
             */
            @Override
            public Object caseSQLPatternItem(SQLPatternItem object) {
                return SQLPATTERNS;
            }

            public Object caseProcessItem(ProcessItem object) {

                return PROCESS;
            }

            /*
             * (non-Javadoc)
             * 
             * @seeorg.talend.core.model.properties.util.PropertiesSwitch# caseJobletProcessItem
             * (org.talend.core.model.properties.JobletProcessItem)
             */
            @Override
            public Object caseJobletProcessItem(JobletProcessItem object) {
                return JOBLET;
            }

            public Object caseContextItem(ContextItem object) {
                return CONTEXT;
            }

            public Object caseSnippetItem(SnippetItem object) {
                return SNIPPETS;
            }

            public Object caseSnippetVariable(SnippetVariable object) {
                return SNIPPETS;
            }

            public Object caseBusinessProcessItem(BusinessProcessItem object) {
                return BUSINESS_PROCESS;
            }

            public Object caseCSVFileConnectionItem(CSVFileConnectionItem object) {
                throw new IllegalStateException(Messages.getString("ERepositoryObjectType.NotImplemented")); //$NON-NLS-1$
            }

            public Object caseDatabaseConnectionItem(DatabaseConnectionItem object) {
                return METADATA_CONNECTIONS;
            }

            @Override
            public Object caseSAPConnectionItem(SAPConnectionItem object) {
                return METADATA_SAPCONNECTIONS;
            }

            public Object caseDelimitedFileConnectionItem(DelimitedFileConnectionItem object) {
                return METADATA_FILE_DELIMITED;
            }

            public Object casePositionalFileConnectionItem(PositionalFileConnectionItem object) {
                return METADATA_FILE_POSITIONAL;
            }

            public Object caseRegExFileConnectionItem(RegExFileConnectionItem object) {
                return METADATA_FILE_REGEXP;
            }

            public Object caseXmlFileConnectionItem(XmlFileConnectionItem object) {
                return METADATA_FILE_XML;
            }

            public Object caseExcelFileConnectionItem(ExcelFileConnectionItem object) {
                return METADATA_FILE_EXCEL;
            }

            public Object caseLdifFileConnectionItem(LdifFileConnectionItem object) {
                return METADATA_FILE_LDIF;
            }

            public Object caseLDAPSchemaConnectionItem(LDAPSchemaConnectionItem object) {
                return METADATA_LDAP_SCHEMA;
            }

            public Object caseGenericSchemaConnectionItem(GenericSchemaConnectionItem object) {
                return METADATA_GENERIC_SCHEMA;
            }

            public Object caseSalesforceSchemaConnectionItem(SalesforceSchemaConnectionItem object) {
                return METADATA_SALESFORCE_SCHEMA;
            }

            public Object caseWSDLSchemaConnectionItem(WSDLSchemaConnectionItem object) {
                return METADATA_WSDL_SCHEMA;
            }

            public Object caseEDIFACTConnectionItem(EDIFACTConnectionItem object) {
                return METADATA_EDIFACT;
            }

            @Override
            public Object caseEbcdicConnectionItem(EbcdicConnectionItem object) {
                return METADATA_FILE_EBCDIC;
            }

            public Object caseHL7ConnectionItem(HL7ConnectionItem object) {
                return METADATA_FILE_HL7;
            }

            public Object caseFTPConnectionItem(FTPConnectionItem object) {
                return METADATA_FILE_FTP;
            }

            @Override
            public Object caseBRMSConnectionItem(BRMSConnectionItem object) {
                return METADATA_FILE_BRMS;
            }

            public Object caseMDMConnectionItem(MDMConnectionItem object) {
                return METADATA_MDMCONNECTION;
            }

            @Override
            public Object caseSVGBusinessProcessItem(SVGBusinessProcessItem object) {
                return SVG_BUSINESS_PROCESS;
            }

            public Object caseHeaderFooterConnectionItem(HeaderFooterConnectionItem object) {
                return METADATA_HEADER_FOOTER;
            }

            // MOD mzhao feature 9207
            @Override
            public Object caseTDQItem(TDQItem object) {
                return TDQ_ELEMENT;
            }

            public Object caseValidationRulesConnectionItem(ValidationRulesConnectionItem object) {
                return METADATA_VALIDATION_RULES;
            }

            public Object defaultCase(EObject object) {
                throw new IllegalStateException();
            }
        }.doSwitch(item);

    }

    private static ERepositoryObjectType getTDQRepObjType(Item item) {
        AbstractDQModelService dqModelService = CoreRuntimePlugin.getInstance().getDQModelService();
        if (dqModelService != null) {
            return dqModelService.getTDQRepObjType(item);
        }
        return null;
    }

    private static ERepositoryObjectType getCamelRepObjectType(Item item) {
        ICamelItemService camelService = CoreRuntimePlugin.getInstance().getCamelService();
        if (camelService != null) {
            return camelService.getCamelRepObjType(item);
        }
        return null;
    }

    public boolean isSubItem() {
        return this.subItem;
    }

    public boolean isResourceItem() {
        return this.isResouce();
    }

    /**
     * DOC bZhou Comment method "isDQItemType".
     * 
     * This method is to estimat a type is a TDQ item or not.
     * 
     * @return
     */
    public boolean isDQItemType() {
        return Arrays.asList(this.getProducts()).contains("DQ"); //$NON-NLS-N$
    }

    /**
     * DOC bZhou Comment method "isDIType".
     * 
     * This method is to estimat a type belongs to Data Intergration.
     * 
     * @param type
     * @return
     */
    public boolean isDIItemType() {
        return Arrays.asList(this.getProducts()).contains("DI"); //$NON-NLS-N$
    }

    /**
     * DOC bZhou Comment method "isSharedType".
     * 
     * This method is to estimat a type belongs to both DQ and DI product.
     * 
     * @param type
     * @return
     */
    public boolean isSharedType() {
        return isDQItemType() && isDIItemType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.repository.DynaEnum#name()
     */
    public String name() {
        if (isStaticNode()) {
            Field[] allFields = ERepositoryObjectType.class.getDeclaredFields();
            for (Field f : allFields) {
                try {
                    Object object = f.get(null);
                    if (object == this) {
                        return f.getName();
                    }
                } catch (IllegalArgumentException e) {
                    //
                } catch (IllegalAccessException e) {
                    //
                }
            }
        }
        return super.name();
    }

    public String getFolder() {
        return this.folder;
    }

    public String[] getProducts() {
        return this.products;
    }

    public boolean isResouce() {
        return this.isResouce;
    }
}
