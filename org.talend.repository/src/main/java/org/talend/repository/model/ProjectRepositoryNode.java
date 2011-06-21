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
package org.talend.repository.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.RuntimeExceptionHandler;
import org.talend.commons.utils.data.container.Container;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.genhtml.IHTMLDocConstants;
import org.talend.core.model.metadata.builder.connection.BRMSConnection;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.DelimitedFileConnection;
import org.talend.core.model.metadata.builder.connection.EbcdicConnection;
import org.talend.core.model.metadata.builder.connection.FTPConnection;
import org.talend.core.model.metadata.builder.connection.FileExcelConnection;
import org.talend.core.model.metadata.builder.connection.GenericSchemaConnection;
import org.talend.core.model.metadata.builder.connection.HL7Connection;
import org.talend.core.model.metadata.builder.connection.LDAPSchemaConnection;
import org.talend.core.model.metadata.builder.connection.LdifFileConnection;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.PositionalFileConnection;
import org.talend.core.model.metadata.builder.connection.QueriesConnection;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.metadata.builder.connection.RegexpFileConnection;
import org.talend.core.model.metadata.builder.connection.SAPConnection;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.metadata.builder.connection.SAPIDocUnit;
import org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection;
import org.talend.core.model.metadata.builder.connection.WSDLSchemaConnection;
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.FolderType;
import org.talend.core.model.properties.Information;
import org.talend.core.model.properties.InformationLevel;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobDocumentationItem;
import org.talend.core.model.properties.JobletDocumentationItem;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.ProjectReference;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.Folder;
import org.talend.core.model.repository.IRepositoryObject;
import org.talend.core.model.repository.IRepositoryPrefConstants;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.model.repository.SVNConstant;
import org.talend.core.ui.ICDCProviderService;
import org.talend.core.ui.IHeaderFooterProviderService;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.images.ECoreImage;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.SubItemHelper;
import org.talend.cwm.helper.TableHelper;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.nodes.IProjectRepositoryNode;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class ProjectRepositoryNode extends RepositoryNode implements IProjectRepositoryNode {

    private RepositoryNode businessProcessNode, recBinNode, codeNode, routineNode, snippetsNode, processNode, contextNode,
            docNode, metadataConNode, sqlPatternNode, metadataFileNode, metadataFilePositionalNode, metadataFileRegexpNode,
            metadataFileXmlNode, metadataFileLdifNode, metadataGenericSchemaNode, metadataLDAPSchemaNode, metadataWSDLSchemaNode,
            metadataFileExcelNode, metadataSalesforceSchemaNode, metadataSAPConnectionNode, metadataFTPConnectionNode,
            metadataEbcdicConnectionNode, metadataHL7ConnectionNode, metadataMDMConnectionNode, metadataRulesNode,
            metadataHeaderFooterConnectionNode, jobscriptsNode, metadataBRMSConnectionNode;

    private RepositoryNode jobletNode;

    private RepositoryNode svnRootNode;

    private RepositoryNode metadataNode;

    private RepositoryNode refProject;

    private boolean mergeRefProject;

    public static boolean refProjectBool = false;

    public static boolean refreshBool = false;

    private final IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    private org.talend.core.model.general.Project project;

    private Map<Object, List<Project>> nodeAndProject;

    /**
     * DOC nrousseau ProjectRepositoryNode constructor comment.
     * 
     * @param object
     * @param parent
     * @param type
     */
    public ProjectRepositoryNode(org.talend.core.model.general.Project project, IRepositoryViewObject object,
            RepositoryNode parent, RepositoryNode root, ENodeType type) {
        super(object, parent, type);
        // for referenced project
        this.project = project;
        setRoot(this);
    }

    public ProjectRepositoryNode(ProjectRepositoryNode projectNode) {
        this(projectNode.getProject(), projectNode.getObject(), projectNode.getParent(), (RepositoryNode) projectNode.getRoot(),
                projectNode.getType());

        this.setProperties(EProperties.LABEL, projectNode.getProperties(EProperties.LABEL));
        this.setProperties(EProperties.CONTENT_TYPE, projectNode.getProperties(EProperties.CONTENT_TYPE));
    }

    public ProjectRepositoryNode(IRepositoryObject object, RepositoryNode parent, ENodeType type) {
        super(object, parent, type);
        // base project
        this.project = ProjectManager.getInstance().getCurrentProject();
        setRoot(this);
    }

    private void hideHiddenNodes() {
        IBrandingService service = (IBrandingService) GlobalServiceRegister.getDefault().getService(IBrandingService.class);
        List<IRepositoryNode> hiddens = service.getBrandingConfiguration().getHiddenRepositoryCategory(this);
        // this.getChildren().removeAll(hiddens);
        for (IRepositoryNode node : hiddens) {
            removeNode(this, node);
            switch (node.getContentType()) {
            case BUSINESS_PROCESS:
                this.businessProcessNode = null;
                break;
            case PROCESS:
                this.processNode = null;
                break;
            case CONTEXT:
                this.contextNode = null;
                break;
            case ROUTINES:
                this.routineNode = null;
                break;
            case SNIPPETS:
                this.snippetsNode = null;
                break;
            case GENERATED:
            case JOBS:
            case JOB_DOC:
            case JOBLETS:
            case JOBLET_DOC:
            case DOCUMENTATION:
                this.docNode = null;
                break;
            // case METADATA_CON_TABLE:
            case METADATA:
                this.metadataNode = null;
                break;
            case METADATA_CON_VIEW:
            case METADATA_CON_SYNONYM:
            case METADATA_CON_QUERY:
            case METADATA_CON_CDC:
            case METADATA_CONNECTIONS:
                this.metadataConNode = null;
                break;
            case METADATA_SAPCONNECTIONS:
                this.metadataSAPConnectionNode = null;
                break;
            case METADATA_HEADER_FOOTER:
                this.metadataHeaderFooterConnectionNode = null;
                break;
            // case METADATA_SAP_FUNCTION:
            // this.metadataSAPFunctionNode = null;
            // break;
            case SQLPATTERNS:
                this.sqlPatternNode = null;
                break;
            case METADATA_FILE_DELIMITED:
                this.metadataFileNode = null;
                break;
            case METADATA_FILE_POSITIONAL:
                this.metadataFilePositionalNode = null;
                break;
            case METADATA_FILE_REGEXP:
                this.metadataFileRegexpNode = null;
                break;
            case METADATA_FILE_XML:
                this.metadataFileXmlNode = null;
                break;
            case METADATA_FILE_HL7:
                this.metadataHL7ConnectionNode = null;
                break;
            case METADATA_FILE_FTP:
                this.metadataFTPConnectionNode = null;
                break;
            case METADATA_FILE_BRMS:
                this.metadataBRMSConnectionNode = null;
                break;
            case METADATA_FILE_LDIF:
                this.metadataFileLdifNode = null;
                break;
            case METADATA_FILE_EXCEL:
                this.metadataFileExcelNode = null;
                break;
            case METADATA_FILE_EBCDIC:
                this.metadataEbcdicConnectionNode = null;
                break;
            case METADATA_MDMCONNECTION:
                this.metadataMDMConnectionNode = null;
                break;
            case METADATA_SALESFORCE_SCHEMA:
                this.metadataSalesforceSchemaNode = null;
                break;
            case METADATA_GENERIC_SCHEMA:
                this.metadataGenericSchemaNode = null;
                break;
            case METADATA_LDAP_SCHEMA:
                this.metadataLDAPSchemaNode = null;
                break;
            case METADATA_WSDL_SCHEMA:
                this.metadataWSDLSchemaNode = null;
                break;
            case METADATA_FILE_RULES:
                this.metadataRulesNode = null;
                break;
            case REFERENCED_PROJECTS:
                this.refProject = null;
                break;
            case JOBLET:
                this.jobletNode = null;
                break;
            default:
            }
        }
    }

    private void removeNode(IRepositoryNode container, IRepositoryNode node) {
        List<IRepositoryNode> nodes = container.getChildren();

        if (nodes.contains(node)) {
            nodes.remove(node);
        } else {
            for (IRepositoryNode n : nodes) {
                removeNode(n, node);
            }
        }
    }

    private String showSVNRoot() {
        String urlPath = ProjectManager.getInstance().getCurrentBranchURL(project);
        if (!urlPath.contains(SVNConstant.NAME_TRUNK) && !urlPath.contains(SVNConstant.NAME_BRANCHES)
                && !urlPath.contains(SVNConstant.NAME_TAGS)) {
            return null;
        }
        if ("".equals(urlPath) || urlPath == null) { //$NON-NLS-1$
            return null;
        }
        String[] urlEles = urlPath.split("/"); //$NON-NLS-1$
        return urlEles[urlEles.length - 1];
    }

    public void initialize() {
        nodeAndProject = new HashMap<Object, List<Project>>();

        List<IRepositoryNode> nodes = null;
        String urlBranch = null;
        if (ProjectManager.getInstance().getCurrentBranchURL(project) != null) {
            urlBranch = showSVNRoot();
        }
        if ("".equals(urlBranch) || urlBranch == null) { //$NON-NLS-1$
            nodes = getChildren();
        } else {
            List<IRepositoryNode> root = getChildren();

            svnRootNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
            svnRootNode.setProperties(EProperties.LABEL, ERepositoryObjectType.SVN_ROOT + "(" + urlBranch + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            svnRootNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.SVN_ROOT);
            root.add(svnRootNode);

            nodes = svnRootNode.getChildren();
        }

        // 0. Recycle bin
        recBinNode = new BinRepositoryNode(this);
        nodes.add(recBinNode);

        // 1. Business process
        businessProcessNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
        businessProcessNode.setProperties(EProperties.LABEL, ERepositoryObjectType.BUSINESS_PROCESS);
        businessProcessNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.BUSINESS_PROCESS);
        nodes.add(businessProcessNode);

        // 2. Process
        processNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
        processNode.setProperties(EProperties.LABEL, ERepositoryObjectType.PROCESS);
        processNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.PROCESS);
        nodes.add(processNode);

        if (PluginChecker.isJobLetPluginLoaded()) {
            // 2.1 Joblet
            jobletNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
            jobletNode.setProperties(EProperties.LABEL, ERepositoryObjectType.JOBLET);
            jobletNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.JOBLET);
            nodes.add(jobletNode);
        }

        // 3. Context
        contextNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
        contextNode.setProperties(EProperties.LABEL, ERepositoryObjectType.CONTEXT);
        contextNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.CONTEXT);
        nodes.add(contextNode);

        // 4. Code
        codeNode = new StableRepositoryNode(this,
                Messages.getString("RepositoryContentProvider.repositoryLabel.code"), ECoreImage.CODE_ICON); //$NON-NLS-1$
        codeNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.ROUTINES);
        nodes.add(codeNode);

        // 4.1. Routines
        routineNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
        routineNode.setProperties(EProperties.LABEL, ERepositoryObjectType.ROUTINES);
        routineNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.ROUTINES);
        codeNode.getChildren().add(routineNode);

        // 4.2 jobscripts
        if (PluginChecker.isMetalanguagePluginLoaded()) {
            jobscriptsNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
            jobscriptsNode.setProperties(EProperties.LABEL, ERepositoryObjectType.JOB_SCRIPT);
            jobscriptsNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.JOB_SCRIPT);
            codeNode.getChildren().add(jobscriptsNode);
        }

        if (GlobalServiceRegister.getDefault().isServiceRegistered(IHeaderFooterProviderService.class)) {
            IHeaderFooterProviderService service = (IHeaderFooterProviderService) GlobalServiceRegister.getDefault().getService(
                    IHeaderFooterProviderService.class);
            if (service.isVisible()) {
                // Metadata Datacert connections
                metadataHeaderFooterConnectionNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
                metadataHeaderFooterConnectionNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_HEADER_FOOTER);
                metadataHeaderFooterConnectionNode.setProperties(EProperties.CONTENT_TYPE,
                        ERepositoryObjectType.METADATA_HEADER_FOOTER);
                codeNode.getChildren().add(metadataHeaderFooterConnectionNode);
            }
        }

        // 4.2. Snippets
        // if (PluginChecker.isSnippetsPluginLoaded()) {
        // snippetsNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
        // snippetsNode.setProperties(EProperties.LABEL, ERepositoryObjectType.SNIPPETS);
        // snippetsNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.SNIPPETS);
        // codeNode.getChildren().add(snippetsNode);
        // }

        // 5. Sql patterns
        sqlPatternNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
        sqlPatternNode.setProperties(EProperties.LABEL, ERepositoryObjectType.SQLPATTERNS);
        sqlPatternNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.SQLPATTERNS);
        nodes.add(sqlPatternNode);

        // 6. Documentation
        docNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
        docNode.setProperties(EProperties.LABEL, ERepositoryObjectType.DOCUMENTATION);
        docNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.DOCUMENTATION);
        nodes.add(docNode);

        // 7. Metadata
        metadataNode = new RepositoryNode(null, this, ENodeType.STABLE_SYSTEM_FOLDER);
        metadataNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA);
        metadataNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA);
        nodes.add(metadataNode);

        // 7.1. Metadata connections
        metadataConNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
        metadataConNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_CONNECTIONS);
        metadataConNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_CONNECTIONS);
        metadataNode.getChildren().add(metadataConNode);

        // 7.2. Metadata file delimited
        metadataFileNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
        metadataFileNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_FILE_DELIMITED);
        metadataFileNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_FILE_DELIMITED);
        metadataNode.getChildren().add(metadataFileNode);

        // 7.3. Metadata file positional
        metadataFilePositionalNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
        metadataFilePositionalNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_FILE_POSITIONAL);
        metadataFilePositionalNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_FILE_POSITIONAL);
        metadataNode.getChildren().add(metadataFilePositionalNode);

        // 7.4. Metadata file regexp
        metadataFileRegexpNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
        metadataFileRegexpNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_FILE_REGEXP);
        metadataFileRegexpNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_FILE_REGEXP);
        metadataNode.getChildren().add(metadataFileRegexpNode);

        // 7.5. Metadata file xml
        metadataFileXmlNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
        metadataFileXmlNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_FILE_XML);
        metadataFileXmlNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_FILE_XML);
        metadataNode.getChildren().add(metadataFileXmlNode);

        // 7.6. Metadata file ldif
        metadataFileLdifNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
        metadataFileLdifNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_FILE_LDIF);
        metadataFileLdifNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_FILE_LDIF);
        metadataNode.getChildren().add(metadataFileLdifNode);

        // 7.7. Metadata file Excel
        metadataFileExcelNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
        metadataFileExcelNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_FILE_EXCEL);
        metadataFileExcelNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_FILE_EXCEL);
        metadataNode.getChildren().add(metadataFileExcelNode);

        ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();
        if (codeLanguage != ECodeLanguage.PERL) {
            // 7.8. LDAP schemas
            metadataLDAPSchemaNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
            metadataLDAPSchemaNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_LDAP_SCHEMA);
            metadataLDAPSchemaNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_LDAP_SCHEMA);
            metadataNode.getChildren().add(metadataLDAPSchemaNode);
        }
        // 7.9. Generic schemas
        metadataGenericSchemaNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
        metadataGenericSchemaNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_GENERIC_SCHEMA);
        metadataGenericSchemaNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_GENERIC_SCHEMA);
        metadataNode.getChildren().add(metadataGenericSchemaNode);
        // 7.10 WSDL
        metadataWSDLSchemaNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
        metadataWSDLSchemaNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_WSDL_SCHEMA);
        metadataWSDLSchemaNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_WSDL_SCHEMA);
        metadataNode.getChildren().add(metadataWSDLSchemaNode);

        // 7.11 Salesforce

        if (codeLanguage != ECodeLanguage.PERL) {
            metadataSalesforceSchemaNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
            metadataSalesforceSchemaNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA);
            metadataSalesforceSchemaNode
                    .setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA);
            metadataNode.getChildren().add(metadataSalesforceSchemaNode);
        }

        // 7.12 SAP
        if (PluginChecker.isSAPWizardPluginLoaded() && LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
            metadataSAPConnectionNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
            metadataSAPConnectionNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_SAPCONNECTIONS);
            metadataSAPConnectionNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_SAPCONNECTIONS);
            metadataNode.getChildren().add(metadataSAPConnectionNode);
        }

        if (PluginChecker.isHL7PluginLoaded()) {
            metadataHL7ConnectionNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
            metadataHL7ConnectionNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_FILE_HL7);
            metadataHL7ConnectionNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_FILE_HL7);
            metadataNode.getChildren().add(metadataHL7ConnectionNode);
        }

        if (PluginChecker.isFTPPluginLoaded()) {
            metadataFTPConnectionNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
            metadataFTPConnectionNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_FILE_FTP);
            metadataFTPConnectionNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_FILE_FTP);
            metadataNode.getChildren().add(metadataFTPConnectionNode);
        }

        // 7.13 EBCDIC
        if (PluginChecker.isEBCDICPluginLoaded()) {
            metadataEbcdicConnectionNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
            metadataEbcdicConnectionNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_FILE_EBCDIC);
            metadataEbcdicConnectionNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_FILE_EBCDIC);
            metadataNode.getChildren().add(metadataEbcdicConnectionNode);
        }

        if (PluginChecker.isMDMPluginLoaded()) {
            metadataMDMConnectionNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
            metadataMDMConnectionNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_MDMCONNECTION);
            metadataMDMConnectionNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_MDMCONNECTION);
            metadataNode.getChildren().add(metadataMDMConnectionNode);
        }
        if (PluginChecker.isRulesPluginLoaded() || PluginChecker.isBRMSPluginLoaded()) {
            StableRepositoryNode baseRulesNode = new StableRepositoryNode(this, Messages
                    .getString("ProjectRepositoryNode.rulesManagement"), //$NON-NLS-1$
                    ECoreImage.METADATA_RULES_ICON);
            baseRulesNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_RULES_MANAGEMENT);
            metadataNode.getChildren().add(baseRulesNode);

            if (PluginChecker.isBRMSPluginLoaded()) {
                metadataBRMSConnectionNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
                metadataBRMSConnectionNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_FILE_BRMS);
                metadataBRMSConnectionNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_FILE_BRMS);
                baseRulesNode.getChildren().add(metadataBRMSConnectionNode);
            }
            // 7.14 RULES
            if (PluginChecker.isRulesPluginLoaded() && codeLanguage != ECodeLanguage.PERL) {
                metadataRulesNode = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
                metadataRulesNode.setProperties(EProperties.LABEL, ERepositoryObjectType.METADATA_FILE_RULES);
                metadataRulesNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_FILE_RULES);
                baseRulesNode.getChildren().add(metadataRulesNode);
            }
        }

        // Reference Projects
        if (PluginChecker.isRefProjectLoaded() && getParent() != this && !getMergeRefProject()
                && project.getEmfProject().getReferencedProjects().size() > 0) {
            refProject = new RepositoryNode(null, this, ENodeType.SYSTEM_FOLDER);
            refProject.setProperties(EProperties.LABEL, ERepositoryObjectType.REFERENCED_PROJECTS);
            refProject.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.REFERENCED_PROJECTS);
            nodes.add(refProject);
        }
        // hide hidden nodes;
        hideHiddenNodes();
    }

    /**
     * DOC nrousseau Comment method "initializeChildren".
     * 
     * @param parent
     */
    public void initializeChildren(Object parent) {
        initializeChildren(project, parent);
        if (PluginChecker.isRefProjectLoaded() && getMergeRefProject()) {
            getRefProject(project.getEmfProject(), parent);
        }
    }

    private void getRefProject(Project project, Object parent) {
        for (ProjectReference refProject : (List<ProjectReference>) (List<ProjectReference>) project.getReferencedProjects()) {
            String parentBranch = ProxyRepositoryFactory.getInstance().getRepositoryContext().getFields().get(
                    IProxyRepositoryFactory.BRANCH_SELECTION + "_" + project.getTechnicalLabel()); //$NON-NLS-1$
            if (refProject.getBranch() == null || parentBranch.equals(refProject.getBranch())) {
                Project p = refProject.getReferencedProject();
                List<Project> list = nodeAndProject.get(parent);
                if (list == null) {
                    list = new ArrayList<Project>();
                    nodeAndProject.put(parent, list);
                }
                if (list.contains(p)) {
                    return;
                } else {
                    list.add(p);
                }
                initializeChildren(new org.talend.core.model.general.Project(p), parent);
                getRefProject(p, parent);
            }
        }

    }

    public void clearNodeAndProjectCash() {
        nodeAndProject.clear();
    }

    public void initializeChildren(org.talend.core.model.general.Project newProject, Object parent) {
        try {
            if (parent == businessProcessNode) {
                convert(newProject, factory.getBusinessProcess(newProject, true), businessProcessNode,
                        ERepositoryObjectType.BUSINESS_PROCESS, recBinNode);
            } else if (parent == processNode) {
                convert(newProject, factory.getProcess(newProject, true), processNode, ERepositoryObjectType.PROCESS, recBinNode);
            } else if (parent == jobletNode) {
                convert(newProject, factory.getJoblets(newProject, true), jobletNode, ERepositoryObjectType.JOBLET, recBinNode);
            } else if (parent == routineNode) {
                convert(newProject, factory.getRoutine(newProject, true), routineNode, ERepositoryObjectType.ROUTINES, recBinNode);
            } else if (parent == jobscriptsNode) {
                convert(newProject, factory.getJobScripts(newProject, true), jobscriptsNode, ERepositoryObjectType.JOB_SCRIPT,
                        recBinNode);
            } else if (parent == snippetsNode) {
                convert(newProject, factory.getSnippets(newProject, true), snippetsNode, ERepositoryObjectType.SNIPPETS,
                        recBinNode);
            } else if (parent == contextNode) {
                convert(newProject, factory.getContext(newProject, true), contextNode, ERepositoryObjectType.CONTEXT, recBinNode);
            } else if (parent == docNode) {
                // convertDocumentation(factory.getDocumentation(), docNode, ERepositoryObjectType.DOCUMENTATION,
                // recBinNode);

                convert(newProject, factory.getDocumentation(newProject, true), docNode, ERepositoryObjectType.DOCUMENTATION,
                        recBinNode);
            } else if (parent == metadataConNode) {
                convert(newProject, factory.getMetadataConnection(newProject, true), metadataConNode,
                        ERepositoryObjectType.METADATA_CONNECTIONS, recBinNode);
            } else if (parent == metadataSAPConnectionNode) {
                convert(newProject, factory.getMetadataSAPConnection(newProject, true), metadataSAPConnectionNode,
                        ERepositoryObjectType.METADATA_SAPCONNECTIONS, recBinNode);
            } else if (parent == metadataMDMConnectionNode) {
                convert(newProject, factory.getMetadataMDM(newProject, true), metadataMDMConnectionNode,
                        ERepositoryObjectType.METADATA_MDMCONNECTION, recBinNode);
            } else if (parent == metadataEbcdicConnectionNode) {
                convert(newProject, factory.getMetadataEBCDIC(newProject, true), metadataEbcdicConnectionNode,
                        ERepositoryObjectType.METADATA_FILE_EBCDIC, recBinNode);
            } else if (parent == metadataHL7ConnectionNode) {
                convert(newProject, factory.getMetadataHL7(newProject, true), metadataHL7ConnectionNode,
                        ERepositoryObjectType.METADATA_FILE_HL7, recBinNode);
            } else if (parent == metadataFTPConnectionNode) {
                convert(newProject, factory.getMetadataFTP(newProject, true), metadataFTPConnectionNode,
                        ERepositoryObjectType.METADATA_FILE_FTP, recBinNode);
            } else if (parent == metadataBRMSConnectionNode) {
                convert(newProject, factory.getMetadataBRMS(newProject, true), metadataBRMSConnectionNode,
                        ERepositoryObjectType.METADATA_FILE_BRMS, recBinNode);
            } else if (parent == sqlPatternNode) {
                convert(newProject, factory.getMetadataSQLPattern(newProject, true), sqlPatternNode,
                        ERepositoryObjectType.SQLPATTERNS, recBinNode);
            } else if (parent == metadataFileNode) {
                convert(newProject, factory.getMetadataFileDelimited(newProject, true), metadataFileNode,
                        ERepositoryObjectType.METADATA_FILE_DELIMITED, recBinNode);
            } else if (parent == metadataFilePositionalNode) {
                convert(newProject, factory.getMetadataFilePositional(newProject, true), metadataFilePositionalNode,
                        ERepositoryObjectType.METADATA_FILE_POSITIONAL, recBinNode);
            } else if (parent == metadataFileRegexpNode) {
                convert(newProject, factory.getMetadataFileRegexp(newProject, true), metadataFileRegexpNode,
                        ERepositoryObjectType.METADATA_FILE_REGEXP, recBinNode);
            } else if (parent == metadataFileXmlNode) {
                convert(newProject, factory.getMetadataFileXml(newProject, true), metadataFileXmlNode,
                        ERepositoryObjectType.METADATA_FILE_XML, recBinNode);
            } else if (parent == metadataFileLdifNode) {
                convert(newProject, factory.getMetadataFileLdif(newProject, true), metadataFileLdifNode,
                        ERepositoryObjectType.METADATA_FILE_LDIF, recBinNode);
            } else if (parent == metadataFileExcelNode) {
                convert(newProject, factory.getMetadataFileExcel(newProject, true), metadataFileExcelNode,
                        ERepositoryObjectType.METADATA_FILE_EXCEL, recBinNode);
            } else if (parent == metadataSalesforceSchemaNode) {
                convert(newProject, factory.getMetadataSalesforceSchema(newProject, true), metadataSalesforceSchemaNode,
                        ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA, recBinNode);
            } else if (parent == metadataLDAPSchemaNode) {
                convert(newProject, factory.getMetadataLDAPSchema(newProject, true), metadataLDAPSchemaNode,
                        ERepositoryObjectType.METADATA_LDAP_SCHEMA, recBinNode);
            } else if (parent == metadataGenericSchemaNode) {
                convert(newProject, factory.getMetadataGenericSchema(newProject, true), metadataGenericSchemaNode,
                        ERepositoryObjectType.METADATA_GENERIC_SCHEMA, recBinNode);
            } else if (parent == metadataWSDLSchemaNode) {
                convert(newProject, factory.getMetadataWSDLSchema(newProject, true), metadataWSDLSchemaNode,
                        ERepositoryObjectType.METADATA_WSDL_SCHEMA, recBinNode);
            } else if (parent == metadataRulesNode) { // feature 6484 added
                convert(newProject, factory.getMetadataRules(newProject, true), metadataRulesNode,
                        ERepositoryObjectType.METADATA_FILE_RULES, recBinNode);
            } else if (parent == metadataHeaderFooterConnectionNode) { // feature 6484 added
                convert(newProject, factory.getMetadataHeaderFooter(newProject, true), metadataHeaderFooterConnectionNode,
                        ERepositoryObjectType.METADATA_HEADER_FOOTER, recBinNode);
            } else if (parent == refProject) {
                if (!getMergeRefProject()) {
                    handleReferenced(refProject);
                }
            } else if (parent == recBinNode) {
                List<RepositoryNode> foldersList = new ArrayList<RepositoryNode>();
                List<FolderItem> folderItems = ProjectManager.getInstance().getFolders(newProject.getEmfProject());
                for (FolderItem folder : new ArrayList<FolderItem>(folderItems)) {
                    addItemToRecycleBin(recBinNode, folder, foldersList);
                }
            }
        } catch (PersistenceException e) {
            RuntimeExceptionHandler.process(e);
        }
    }

    private ERepositoryObjectType getFolderContentType(FolderItem folderItem) {
        if (!folderItem.getType().equals(FolderType.SYSTEM_FOLDER_LITERAL)) {
            if (!(folderItem.getParent() instanceof FolderItem)) {
                return null; // appears only for a folder for expression builder !
            }
            return getFolderContentType((FolderItem) folderItem.getParent());
        }
        for (ERepositoryObjectType objectType : ERepositoryObjectType.values()) {
            String folderName;
            try {
                folderName = ERepositoryObjectType.getFolderName(objectType);
            } catch (Exception e) {
                // just catch exception to avoid the types who don't have folders
                continue;
            }
            if (folderName.contains("/")) { //$NON-NLS-1$
                String[] folders = folderName.split("/"); //$NON-NLS-1$
                FolderItem currentFolderItem = folderItem;
                boolean found = true;
                for (int i = folders.length - 1; i >= 0; i--) {
                    if (!currentFolderItem.getProperty().getLabel().equals(folders[i])) {
                        found = false;
                        break;
                    }
                    if (i > 0) {
                        if (!(currentFolderItem.getParent() instanceof FolderItem)) {
                            found = false;
                            break;
                        }
                        currentFolderItem = (FolderItem) currentFolderItem.getParent();
                    }
                }
                if (found) {
                    return objectType;
                }
            } else {
                if (folderName.equals(folderItem.getProperty().getLabel())) {
                    return objectType;
                }
            }
        }
        if (folderItem.getParent() instanceof FolderItem) {
            return getFolderContentType((FolderItem) folderItem.getParent());
        }
        return null;
    }

    private void addItemToRecycleBin(RepositoryNode parentNode, Item item, List<RepositoryNode> foldersList) {
        if (isGeneratedJobItem(item)) {
            return;
        }
        ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(item);
        RepositoryNode currentParentNode = parentNode;
        if (item instanceof FolderItem) {
            itemType = getFolderContentType((FolderItem) item);
            // MOD qiongli 2011-1-21 filter TDQ root folder.
            if (itemType != null && itemType.isDQItemType()) {
                return;
            }// ~
            if (item.getState().isDeleted()) {
                // need to display this folder in the recycle bin.
                Folder folder = new Folder(item.getProperty(), itemType);
                RepositoryNode folderNode = null;
                for (RepositoryNode existingFolder : foldersList) {
                    if (existingFolder.getContentType() == null) {
                        // this can appear temporary when another user has deleted a folder
                        break;
                    }
                    if (existingFolder.getContentType().equals(folder.getType())
                            && existingFolder.getProperties(EProperties.LABEL).equals(folder.getLabel())) {
                        folderNode = existingFolder;
                        break;
                    }
                }
                if (folderNode == null) {
                    folderNode = new RepositoryNode(folder, parentNode, ENodeType.SIMPLE_FOLDER);
                    folderNode.setProperties(EProperties.CONTENT_TYPE, itemType);
                    folderNode.setProperties(EProperties.LABEL, folder.getLabel());
                    foldersList.add(folderNode);
                    parentNode.getChildren().add(folderNode);
                    folderNode.setParent(parentNode);
                }

                for (Item curItem : (List<Item>) new ArrayList(((FolderItem) item).getChildren())) {
                    addItemToRecycleBin(folderNode, curItem, foldersList);
                }
                currentParentNode = folderNode;
            } else {
                for (Item curItem : (List<Item>) new ArrayList(((FolderItem) item).getChildren())) {
                    addItemToRecycleBin(parentNode, curItem, foldersList);
                }
            }
        } else if (item.getState() != null && item.getState().isDeleted()) {
            try {
                if (item.getProperty().getVersion().equals(
                        ProxyRepositoryFactory.getInstance().getLastVersion(item.getProperty().getId()).getVersion())) {
                    RepositoryNode repNode = new RepositoryNode(new RepositoryViewObject(item.getProperty()), currentParentNode,
                            ENodeType.REPOSITORY_ELEMENT);
                    repNode.setProperties(EProperties.CONTENT_TYPE, itemType);
                    repNode.setProperties(EProperties.LABEL, item.getProperty().getLabel());
                    currentParentNode.getChildren().add(repNode);
                    repNode.setParent(currentParentNode);
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }

        } else {// bug 17768
            if (item instanceof ConnectionItem) {
                Connection connection = ((ConnectionItem) item).getConnection();

                // for sap
                if (connection instanceof SAPConnection) {
                    SAPConnection sapConnection = (SAPConnection) connection;
                    EList<SAPFunctionUnit> funtions = sapConnection.getFuntions();
                    if (funtions != null) {
                        for (int i = 0; i < funtions.size(); i++) {
                            SAPFunctionUnit unit = (SAPFunctionUnit) funtions.get(i);
                            if (SubItemHelper.isDeleted(unit)) {
                                RepositoryNode tableNode = createSAPNode(new RepositoryViewObject(item.getProperty()),
                                        currentParentNode, unit);
                                currentParentNode.getChildren().add(tableNode);
                                tableNode.setParent(currentParentNode);
                            } else {
                                for (MetadataTable table : ConnectionHelper.getTables(connection, unit)) {
                                    if (SubItemHelper.isDeleted(table)) {
                                        RepositoryNode tableNode = createMetatableNode(currentParentNode,
                                                new RepositoryViewObject(item.getProperty()), table,
                                                ERepositoryObjectType.METADATA_CON_TABLE);
                                        currentParentNode.getChildren().add(tableNode);
                                        tableNode.setParent(currentParentNode);
                                    }
                                }
                            }
                        }
                    }
                    EList<SAPIDocUnit> iDocs = sapConnection.getIDocs();
                    if (iDocs != null) {
                        for (int i = 0; i < iDocs.size(); i++) {
                            SAPIDocUnit unit = (SAPIDocUnit) iDocs.get(i);
                            if (SubItemHelper.isDeleted(unit)) {
                                RepositoryNode tableNode = createSAPNode(new RepositoryViewObject(item.getProperty()),
                                        currentParentNode, unit);
                                currentParentNode.getChildren().add(tableNode);
                                tableNode.setParent(currentParentNode);
                            }
                        }
                    }
                    return;
                }

                for (MetadataTable table : ConnectionHelper.getTables(connection)) {
                    if (SubItemHelper.isDeleted(table)) {
                        RepositoryNode tableNode = createMetatableNode(currentParentNode, new RepositoryViewObject(item
                                .getProperty()), table, ERepositoryObjectType.METADATA_CON_TABLE);
                        currentParentNode.getChildren().add(tableNode);
                        tableNode.setParent(currentParentNode);
                    }
                }

                QueriesConnection queriesConnection = connection.getQueries();
                if (queriesConnection != null) {
                    for (Query query : queriesConnection.getQuery()) {
                        if (SubItemHelper.isDeleted(query)) {
                            RepositoryNode queryNode = createQueryNode(currentParentNode, new RepositoryViewObject(item
                                    .getProperty()), query);
                            currentParentNode.getChildren().add(queryNode);
                            queryNode.setParent(currentParentNode);
                        }
                    }
                }

            }
        }
    }

    /**
     * 
     * ggu Comment method "isGeneratedJobItem".
     * 
     * feature 4393
     */
    private boolean isGeneratedJobItem(Item item) {
        if (item != null) {
            if (item instanceof JobDocumentationItem || item instanceof JobletDocumentationItem) {
                return true;
            }
        }
        return false;
    }

    private RepositoryNode getDocumentationNode(ERepositoryObjectType type) {
        if (getMergeRefProject()) {
            RepositoryNode docNode = getRootRepositoryNode(ERepositoryObjectType.DOCUMENTATION);
            for (IRepositoryNode child : docNode.getChildren()) {

                if (type == child.getContentType()) {
                    return (RepositoryNode) child;
                }

                for (IRepositoryNode c : child.getChildren()) {
                    if (type == c.getContentType()) {
                        return (RepositoryNode) c;
                    }
                }
            }
        }
        return null;
    }

    /**
     * ftang Comment method "convertDocumentation".
     * 
     * @param fromModel
     * @param parent
     * @param type
     * @param recBinNode
     */
    private void convertDocumentation(org.talend.core.model.general.Project newProject, Container fromModel,
            RepositoryNode parent, ERepositoryObjectType type, RepositoryNode recBinNode) {
        // for folder Documentation/generated
        RepositoryNode generatedFolder = getDocumentationNode(ERepositoryObjectType.GENERATED);
        String generatedFolderName = ERepositoryObjectType.GENERATED.name().toLowerCase();
        if (generatedFolder == null) {
            generatedFolder = new StableRepositoryNode(parent, generatedFolderName, ECoreImage.FOLDER_CLOSE_ICON);
            parent.getChildren().add(generatedFolder);
        }
        // for folder Documentation/generated/jobs
        RepositoryNode jobsFolder = getDocumentationNode(ERepositoryObjectType.JOBS);
        String jobsFolderName = ERepositoryObjectType.JOBS.name().toLowerCase();
        if (jobsFolder == null) {
            jobsFolder = new StableRepositoryNode(generatedFolder, jobsFolderName, ECoreImage.FOLDER_CLOSE_ICON);
            generatedFolder.getChildren().add(jobsFolder);
        }
        // for folder Documentation/generated/joblets
        RepositoryNode jobletsFolder = getDocumentationNode(ERepositoryObjectType.JOBLETS);
        String jobletsFolderName = ERepositoryObjectType.JOBLETS.name().toLowerCase();
        if (jobletsFolder == null) {
            jobletsFolder = new StableRepositoryNode(generatedFolder, jobletsFolderName, ECoreImage.FOLDER_CLOSE_ICON);
            generatedFolder.getChildren().add(jobletsFolder);
        }

        jobsFolder.setProperties(EProperties.LABEL, ERepositoryObjectType.JOBS);
        jobsFolder.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.JOBS);

        jobletsFolder.setProperties(EProperties.LABEL, ERepositoryObjectType.JOBLETS);
        jobletsFolder.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.JOBLETS);

        Container generatedContainer = null;
        for (Object object : fromModel.getSubContainer()) {
            if (((Container) object).getLabel().equalsIgnoreCase(generatedFolderName)) {
                generatedContainer = (Container) object;
                break;
            }
        }

        Container jobsNode = null;
        Container jobletsNode = null;
        for (Object object : generatedContainer.getSubContainer()) {
            if (((Container) object).getLabel().equalsIgnoreCase(jobsFolderName)) {
                jobsNode = (Container) object;
                break;
            }
        }

        for (Object object : generatedContainer.getSubContainer()) {
            if (((Container) object).getLabel().equalsIgnoreCase(jobletsFolderName)) {
                jobletsNode = (Container) object;
                break;
            }
        }

        // get the files under generated/nodes.
        if (jobsNode != null) {
            convert(newProject, jobsNode, jobsFolder, ERepositoryObjectType.JOB_DOC, recBinNode);
        }

        if (jobletsNode != null) {
            convert(newProject, jobletsNode, jobletsFolder, ERepositoryObjectType.JOBLET_DOC, recBinNode);
        }

        generatedFolder.setProperties(EProperties.LABEL, ERepositoryObjectType.GENERATED);
        generatedFolder.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.GENERATED); // ERepositoryObjectType
        // .FOLDER);

    }

    private RepositoryNode getSQLPatternNode(String parentLabel, String label) {
        if (getMergeRefProject()) {
            List<IRepositoryNode> sqlChildren = getRootRepositoryNode(ERepositoryObjectType.SQLPATTERNS).getChildren();
            // List<RepositoryNode> sqlChildren = parent.getChildren();

            for (IRepositoryNode sqlChild : sqlChildren) {
                if (label.equalsIgnoreCase(sqlChild.toString())) {
                    return (RepositoryNode) sqlChild;
                }
                for (IRepositoryNode userDefined : sqlChild.getChildren()) {
                    if (label.equalsIgnoreCase(((RepositoryNode) userDefined).getProperties(EProperties.LABEL).toString())) {
                        if (sqlChild.toString().equalsIgnoreCase(parentLabel))
                            return (RepositoryNode) userDefined;
                    }

                }
            }
        }
        return null;
    }

    private void convert(org.talend.core.model.general.Project newProject, Container fromModel, RepositoryNode parent,
            ERepositoryObjectType type, RepositoryNode recBinNode) {

        if (parent == null) {
            return;
        }

        for (Object obj : fromModel.getSubContainer()) {
            Container container = (Container) obj;
            Folder oFolder = new Folder((Property) container.getProperty(), type);
            if (oFolder.getProperty() == null) {
                continue;
            }

            RepositoryNode folder = null;

            String label = container.getLabel();
            if (label.equals("bin") || label.startsWith(".")) {
                continue;
            }

            boolean isJobDocRootFolder = ((label.indexOf("_") != -1) && (label.indexOf(".") != -1)); //$NON-NLS-1$ //$NON-NLS-2$
            boolean isPicFolderName = label.equals(IHTMLDocConstants.PIC_FOLDER_NAME);

            // Do not show job documentation root folder and Foder "pictures" on the repository view.
            if (isJobDocRootFolder || isPicFolderName) {
                continue;
            }
            // for system folder
            if (RepositoryConstants.SYSTEM_DIRECTORY.equals(label)) {
                if (getMergeRefProject()) {
                    List list = parent.getChildren();
                    boolean existSystemFolder = false;
                    for (IRepositoryNode node : parent.getChildren()) {
                        if (RepositoryConstants.SYSTEM_DIRECTORY.equalsIgnoreCase(node.getLabel())) { //$NON-NLS-1$
                            existSystemFolder = true;
                            break;
                        }
                    }
                    if (!existSystemFolder) {
                        folder = new StableRepositoryNode(parent, RepositoryConstants.SYSTEM_DIRECTORY,
                                ECoreImage.FOLDER_CLOSE_ICON);
                        parent.getChildren().add(folder);
                    } else {
                        continue;
                    }
                } else {

                    folder = new StableRepositoryNode(parent, RepositoryConstants.SYSTEM_DIRECTORY, ECoreImage.FOLDER_CLOSE_ICON);
                    parent.getChildren().add(folder);
                }

            } else if (ERepositoryObjectType.GENERATED.name().equalsIgnoreCase(label)) {
                convertDocumentation(newProject, fromModel, parent, type, recBinNode);
                continue;
            } else {
                if (getMergeRefProject()) {
                    String a = parent.getProperties(EProperties.LABEL).toString();
                    if (type == ERepositoryObjectType.SQLPATTERNS) {
                        folder = getSQLPatternNode(a, label);
                    }
                    if (folder == null) {
                        if (newProject != this.project && !hasTalendItems(container)) {
                            continue;
                        }
                        folder = new RepositoryNode(oFolder, parent, ENodeType.SIMPLE_FOLDER);
                        if (factory.getStatus(oFolder) != ERepositoryStatus.DELETED) {
                            parent.getChildren().add(folder);
                        }
                    }
                } else {
                    folder = new RepositoryNode(oFolder, parent, ENodeType.SIMPLE_FOLDER);
                    if (factory.getStatus(oFolder) != ERepositoryStatus.DELETED) {
                        parent.getChildren().add(folder);
                    }
                }

            }
            folder.setProperties(EProperties.LABEL, label);
            folder.setProperties(EProperties.CONTENT_TYPE, type);
            convert(newProject, container, folder, type, recBinNode);
        }

        // not folder or folders have no subFolder
        for (Object obj : fromModel.getMembers()) {
            IRepositoryViewObject repositoryObject = (IRepositoryViewObject) obj;
            try {
                if (!repositoryObject.isDeleted()) {
                    addNode(parent, type, recBinNode, repositoryObject);
                }
            } catch (Exception e) {
                ExceptionHandler.log("Item not valid: [" + repositoryObject.getType() + "] " + repositoryObject.getLabel());

                if (repositoryObject.getProperty().getInformations().isEmpty()) {
                    Information info = PropertiesFactory.eINSTANCE.createInformation();
                    info.setLevel(InformationLevel.ERROR_LITERAL);
                    info.setText("Invalid item");
                    Property property = repositoryObject.getProperty();
                    property.getInformations().add(info);
                    try {
                        factory.save(project, property);
                    } catch (PersistenceException e1) {
                        ExceptionHandler.process(e1);
                    }
                    repositoryObject.getProperty(); // call getProperty to update since it's a RepositoryViewObject.
                }
            }
        }
    }

    private boolean hasTalendItems(Container container) {
        if (!container.getMembers().isEmpty()) {
            return true;
        }
        for (Object obj : container.getSubContainer()) {
            Container subContainer = (Container) obj;
            boolean hasTalendItems = hasTalendItems(subContainer);
            if (!hasTalendItems) {
                continue;
            }
            return hasTalendItems;
        }

        return false;

    }

    private void handleReferenced(RepositoryNode parent) {
        if (parent.getType().equals(ENodeType.SYSTEM_FOLDER)) {
            for (ProjectReference refProject : (List<ProjectReference>) (List<ProjectReference>) project.getEmfProject()
                    .getReferencedProjects()) {
                String parentBranch = ProxyRepositoryFactory.getInstance().getRepositoryContext().getFields().get(
                        IProxyRepositoryFactory.BRANCH_SELECTION + "_" + project.getTechnicalLabel()); //$NON-NLS-1$
                // if not a DB ref project, modified by nma, order 12519
                if (refProject.getReferencedProject().getUrl() != null
                        && refProject.getReferencedProject().getUrl().startsWith("teneo") //$NON-NLS-1$
                        || (refProject.getBranch() != null && refProject.getBranch().equals(parentBranch))) {
                    Project emfProject = refProject.getReferencedProject();
                    ProjectRepositoryNode referencedProjectNode = new ProjectRepositoryNode(
                            new org.talend.core.model.general.Project(emfProject), null, parent, this,
                            ENodeType.REFERENCED_PROJECT);
                    referencedProjectNode.setProperties(EProperties.LABEL, emfProject.getLabel()); //$NON-NLS-1$
                    referencedProjectNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.REFERENCED_PROJECTS);
                    parent.getChildren().add(referencedProjectNode);
                    referencedProjectNode.initialize();
                }
            }
        }

    }

    private void addNode(RepositoryNode parent, ERepositoryObjectType type, RepositoryNode recBinNode,
            IRepositoryViewObject repositoryObject) {

        boolean isAvaliableInTOS = true; // this flag filter the databaseconnections which didn't supported by TOS but
        // TOP
        if (type == ERepositoryObjectType.METADATA_CONNECTIONS) {
            DatabaseConnection metadataConnection = (DatabaseConnection) ((ConnectionItem) repositoryObject.getProperty()
                    .getItem()).getConnection();
            isAvaliableInTOS = EDatabaseTypeName.getTypeFromDbType(metadataConnection.getDatabaseType(), false) == null ? false
                    : true;

        }

        RepositoryNode node = new RepositoryNode(repositoryObject, parent, ENodeType.REPOSITORY_ELEMENT);

        node.setProperties(EProperties.CONTENT_TYPE, type);
        node.setProperties(EProperties.LABEL, repositoryObject.getLabel());
        if ((repositoryObject.isDeleted() && parent.getObject() == null)
                || (repositoryObject.isDeleted() && !parent.getObject().isDeleted())) {
            // recBinNode.getChildren().add(node);
            // node.setParent(recBinNode);
        } else {
            for (IRepositoryNode repositoryNode : parent.getChildren()) {
                if (repositoryNode.getObject() != null) {
                    if (repositoryNode.getObject().getId().equals(repositoryObject.getId())) {
                        Problem problem = new Problem();
                        problem.setDescription(type.name() + " " + repositoryNode.getObject().getLabel() + " " //$NON-NLS-1$ //$NON-NLS-2$
                                + repositoryNode.getObject().getVersion() + " and " + repositoryObject.getLabel() + " " //$NON-NLS-1$ //$NON-NLS-2$
                                + repositoryObject.getVersion() + " have the same ID."); //$NON-NLS-1$
                        problem.setStatus(ProblemStatus.WARNING);
                        CorePlugin.getDefault().getDesignerCoreService().addProblems(problem);
                    }
                }
            }
            if (isAvaliableInTOS) {
                parent.getChildren().add(node);
            }
        }

        if (type == ERepositoryObjectType.METADATA_CONNECTIONS && isAvaliableInTOS) {
            DatabaseConnection metadataConnection = (DatabaseConnection) ((ConnectionItem) repositoryObject.getProperty()
                    .getItem()).getConnection();
            createTables(recBinNode, node, repositoryObject, metadataConnection);
        }
        if (type == ERepositoryObjectType.METADATA_SAPCONNECTIONS) {
            SAPConnection metadataConnection = (SAPConnection) ((ConnectionItem) repositoryObject.getProperty().getItem())
                    .getConnection();
            createTables(recBinNode, node, repositoryObject, metadataConnection);
        }
        if (type == ERepositoryObjectType.METADATA_FILE_DELIMITED) {
            DelimitedFileConnection metadataConnection = (DelimitedFileConnection) ((ConnectionItem) repositoryObject
                    .getProperty().getItem()).getConnection();
            createTables(recBinNode, node, repositoryObject, metadataConnection);
        }
        if (type == ERepositoryObjectType.METADATA_FILE_POSITIONAL) {
            PositionalFileConnection metadataConnection = (PositionalFileConnection) ((ConnectionItem) repositoryObject
                    .getProperty().getItem()).getConnection();
            createTables(recBinNode, node, repositoryObject, metadataConnection);
        }
        if (type == ERepositoryObjectType.METADATA_FILE_REGEXP) {
            RegexpFileConnection metadataConnection = (RegexpFileConnection) ((ConnectionItem) repositoryObject.getProperty()
                    .getItem()).getConnection();
            createTables(recBinNode, node, repositoryObject, metadataConnection);
        }
        if (type == ERepositoryObjectType.METADATA_FILE_XML) {
            XmlFileConnection metadataConnection = (XmlFileConnection) ((ConnectionItem) repositoryObject.getProperty().getItem())
                    .getConnection();
            createTables(recBinNode, node, repositoryObject, metadataConnection);
        }

        if (type == ERepositoryObjectType.METADATA_FILE_EXCEL) {
            FileExcelConnection metadataConnection = (FileExcelConnection) ((ConnectionItem) repositoryObject.getProperty()
                    .getItem()).getConnection();
            createTables(recBinNode, node, repositoryObject, metadataConnection);
        }

        if (type == ERepositoryObjectType.METADATA_FILE_LDIF) {
            LdifFileConnection metadataConnection = (LdifFileConnection) ((ConnectionItem) repositoryObject.getProperty()
                    .getItem()).getConnection();
            createTables(recBinNode, node, repositoryObject, metadataConnection);
        }

        if (type == ERepositoryObjectType.METADATA_LDAP_SCHEMA) {
            LDAPSchemaConnection metadataConnection = (LDAPSchemaConnection) ((ConnectionItem) repositoryObject.getProperty()
                    .getItem()).getConnection();
            createTables(recBinNode, node, repositoryObject, metadataConnection);
        }

        if (type == ERepositoryObjectType.METADATA_GENERIC_SCHEMA) {
            GenericSchemaConnection genericSchemaConnection = (GenericSchemaConnection) ((ConnectionItem) repositoryObject
                    .getProperty().getItem()).getConnection();
            createTables(recBinNode, node, repositoryObject, genericSchemaConnection);
        }
        if (type == ERepositoryObjectType.METADATA_WSDL_SCHEMA) {
            WSDLSchemaConnection genericSchemaConnection = (WSDLSchemaConnection) ((ConnectionItem) repositoryObject
                    .getProperty().getItem()).getConnection();
            createTables(recBinNode, node, repositoryObject, genericSchemaConnection);
        }
        if (type == ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA) {
            SalesforceSchemaConnection genericSchemaConnection = (SalesforceSchemaConnection) ((ConnectionItem) repositoryObject
                    .getProperty().getItem()).getConnection();
            createTables(recBinNode, node, repositoryObject, genericSchemaConnection);
        }
        if (type == ERepositoryObjectType.METADATA_FILE_EBCDIC) {
            EbcdicConnection ebcdicConnection = (EbcdicConnection) ((ConnectionItem) repositoryObject.getProperty().getItem())
                    .getConnection();
            createTables(recBinNode, node, repositoryObject, ebcdicConnection);
        }
        if (type == ERepositoryObjectType.METADATA_FILE_HL7) {
            HL7Connection hl7Connection = (HL7Connection) ((ConnectionItem) repositoryObject.getProperty().getItem())
                    .getConnection();
            createTables(recBinNode, node, repositoryObject, hl7Connection);
        }
        if (type == ERepositoryObjectType.METADATA_FILE_FTP) {
            FTPConnection ftpConnection = (FTPConnection) ((ConnectionItem) repositoryObject.getProperty().getItem())
                    .getConnection();
            createTables(recBinNode, node, repositoryObject, ftpConnection);
        }
        if (type == ERepositoryObjectType.METADATA_FILE_BRMS) {
            BRMSConnection brmsConnection = (BRMSConnection) ((ConnectionItem) repositoryObject.getProperty().getItem())
                    .getConnection();
            createTables(recBinNode, node, repositoryObject, brmsConnection);
        }
        if (type == ERepositoryObjectType.METADATA_MDMCONNECTION) {
            MDMConnection mdmConnection = (MDMConnection) ((ConnectionItem) repositoryObject.getProperty().getItem())
                    .getConnection();
            createTables(recBinNode, node, repositoryObject, mdmConnection);
        }
    }

    /**
     * DOC tguiu Comment method "createTables".
     * 
     * @param node
     * @param repositoryObjectType TODO
     * @param iMetadataConnection
     * @param metadataConnection
     */
    private void createTables(RepositoryNode recBinNode, RepositoryNode node, final IRepositoryViewObject repObj, EList list,
            ERepositoryObjectType repositoryObjectType) {
        for (Object currentTable : list) {
            if (currentTable == null) {
                continue;
            }
            if (currentTable instanceof org.talend.core.model.metadata.builder.connection.MetadataTable) {
                org.talend.core.model.metadata.builder.connection.MetadataTable metadataTable = (org.talend.core.model.metadata.builder.connection.MetadataTable) currentTable;

                // fix for 22381 , deleted metadata will be added in addItemToRecycleBin()
                if (SubItemHelper.isDeleted(metadataTable)) {
                    continue;
                }

                RepositoryNode tableNode = createMetatableNode(node, repObj, metadataTable, repositoryObjectType);
                // if (SubItemHelper.isDeleted(metadataTable)) {
                // recBinNode.getChildren().add(tableNode);
                // } else {
                node.getChildren().add(tableNode);
                // }
            } else if (currentTable instanceof Query) {
                Query query = (Query) currentTable;

                // fix for 22381
                if (SubItemHelper.isDeleted(query)) {
                    continue;
                }

                RepositoryNode queryNode = createQueryNode(node, repObj, query);
                // if (SubItemHelper.isDeleted(query)) {
                // recBinNode.getChildren().add(queryNode);
                // } else {
                node.getChildren().add(queryNode);
                // }

            }
        }
    }

    /**
     * DOC cantoine Comment method "createTable".
     * 
     * @param node
     * @param metadataTable
     * @param repositoryObjectType TODO
     * @param iMetadataConnection
     */
    private void createTable(RepositoryNode recBinNode, RepositoryNode node, final IRepositoryViewObject repObj,
            org.talend.core.model.metadata.builder.connection.MetadataTable metadataTable,
            ERepositoryObjectType repositoryObjectType) {
        if (metadataTable == null) {
            return;
        }
        RepositoryNode tableNode = createMetatableNode(node, repObj, metadataTable, repositoryObjectType);
        if (TableHelper.isDeleted(metadataTable)) {
            recBinNode.getChildren().add(tableNode);
        } else {
            node.getChildren().add(tableNode);
        }
    }

    private void createTables(RepositoryNode recBinNode, RepositoryNode node, final IRepositoryViewObject repObj,
            Connection metadataConnection) {

        // // 5.GENERIC SCHEMAS
        // RepositoryNode genericSchemaNode = new StableRepositoryNode(node, Messages
        // .getString("RepositoryContentProvider.repositoryLabel.GenericSchema"), ECoreImage.FOLDER_CLOSE_ICON);
        // node.getChildren().add(genericSchemaNode);

        if (metadataConnection instanceof DatabaseConnection) {

            // 1.Tables:
            RepositoryNode tablesNode = new StableRepositoryNode(node, Messages
                    .getString("RepositoryContentProvider.repositoryLabel.TableSchemas"), ECoreImage.FOLDER_CLOSE_ICON); //$NON-NLS-1$
            node.getChildren().add(tablesNode);

            // 2.VIEWS:
            RepositoryNode viewsNode = new StableRepositoryNode(node, Messages
                    .getString("RepositoryContentProvider.repositoryLabel.ViewSchemas"), ECoreImage.FOLDER_CLOSE_ICON); //$NON-NLS-1$
            node.getChildren().add(viewsNode);

            // 3.SYNONYMS:
            RepositoryNode synonymsNode = new StableRepositoryNode(node, Messages
                    .getString("RepositoryContentProvider.repositoryLabel.SynonymSchemas"), ECoreImage.FOLDER_CLOSE_ICON); //$NON-NLS-1$
            node.getChildren().add(synonymsNode);

            DatabaseConnection dbconn = (DatabaseConnection) metadataConnection;
            List<MetadataTable> allTables = ConnectionHelper.getTablesWithOrders(dbconn);
            /* only refresh and show tables in current schema or catalog,see bug 0015769 */
            // Set<org.talend.core.model.metadata.builder.connection.MetadataTable> allTables = ProjectNodeHelper
            // .getTablesFromSpecifiedDataPackage(dbconn);
            // /* for bug18514,hywang,need to retrieve again if tables is imported from a sas file */
            // if (allTables.isEmpty() && dbconn.getDatabaseType().equals(EDatabaseTypeName.SAS.getDisplayName())) {
            // allTables = ConnectionHelper.getTables(dbconn);
            // }
            Iterator metadataTables = allTables.iterator();

            // Iterator metadataTables = ConnectionHelper.getTables(metadataConnection).iterator();
            while (metadataTables.hasNext()) {
                org.talend.core.model.metadata.builder.connection.MetadataTable metadataTable = (org.talend.core.model.metadata.builder.connection.MetadataTable) metadataTables
                        .next();

                // bug 17768
                if (SubItemHelper.isDeleted(metadataTable)) {
                    continue;
                }

                String typeTable = null;
                if (metadataTable != null && metadataTable.getTableType() != null) {
                    typeTable = metadataTable.getTableType();
                    if (typeTable.equals("TABLE")) { //$NON-NLS-1$
                        createTable(recBinNode, tablesNode, repObj, metadataTable, ERepositoryObjectType.METADATA_CON_TABLE);

                    } else if (typeTable.equals("VIEW")) { //$NON-NLS-1$
                        createTable(recBinNode, viewsNode, repObj, metadataTable, ERepositoryObjectType.METADATA_CON_TABLE);

                    } else if (typeTable.equals("SYNONYM")) { //$NON-NLS-1$
                        createTable(recBinNode, synonymsNode, repObj, metadataTable, ERepositoryObjectType.METADATA_CON_TABLE);
                    }
                    // bug 0017782 ,db2's SYNONYM need to convert to ALIAS;
                    else if (typeTable.equals("ALIAS")) { //$NON-NLS-1$
                        createTable(recBinNode, synonymsNode, repObj, metadataTable, ERepositoryObjectType.METADATA_CON_TABLE);
                    }
                    // else if (typeTable.equals("GENERIC_SCHEMA")) {
                    // //TODO not finished.
                    // createTable(recBinNode, tablesNode, repObj, metadataTable,
                    // ERepositoryObjectType.METADATA_CON_TABLE);
                    // }
                } else {
                    createTable(recBinNode, tablesNode, repObj, metadataTable, ERepositoryObjectType.METADATA_CON_TABLE);
                }
            }

            // if (!node.getChildren().contains(tablesNode)) {
            // node.getChildren().add(tablesNode);
            // }

            // createTables(recBinNode, node, repObj, metadataConnection.getTables());

            // 4.Queries:
            RepositoryNode queriesNode = new StableRepositoryNode(node, Messages
                    .getString("RepositoryContentProvider.repositoryLabel.Queries"), ECoreImage.FOLDER_CLOSE_ICON); //$NON-NLS-1$
            node.getChildren().add(queriesNode);
            QueriesConnection queriesConnection = (metadataConnection).getQueries();
            if (queriesConnection != null) {
                createTables(recBinNode, queriesNode, repObj, queriesConnection.getQuery(),
                        ERepositoryObjectType.METADATA_CON_TABLE);
            }

            // 5. Change Data Capture
            Item item = node.getObject().getProperty().getItem();
            if (item instanceof DatabaseConnectionItem) {
                DatabaseConnectionItem connectionItem = (DatabaseConnectionItem) item;
                DatabaseConnection connection = (DatabaseConnection) connectionItem.getConnection();
                if (PluginChecker.isCDCPluginLoaded()) {
                    ICDCProviderService service = (ICDCProviderService) GlobalServiceRegister.getDefault().getService(
                            ICDCProviderService.class);
                    if (service != null && service.canCreateCDCConnection(connection)) {
                        RepositoryNode cdcNode = new StableRepositoryNode(node, Messages
                                .getString("RepositoryContentProvider.repositoryLabel.CDCFoundation"), //$NON-NLS-1$
                                ECoreImage.FOLDER_CLOSE_ICON);
                        node.getChildren().add(cdcNode);
                        service.createCDCTypes(recBinNode, cdcNode, connection.getCdcConns());
                    }
                }
            }
        } else if (metadataConnection instanceof SAPConnection) {
            // The sap wizard plugin is loaded
            // 1.Tables:
            RepositoryNode functionNode = new StableRepositoryNode(node, Messages
                    .getString("RepositoryContentProvider.repositoryLabel.sapFunction"), ECoreImage.FOLDER_CLOSE_ICON); //$NON-NLS-1$
            node.getChildren().add(functionNode);

            // add functions
            createSAPFunctionNodes(recBinNode, repObj, metadataConnection, functionNode);

            RepositoryNode iDocNode = new StableRepositoryNode(node, Messages
                    .getString("RepositoryContentProvider.repositoryLabel.sapIDoc"), ECoreImage.FOLDER_CLOSE_ICON); //$NON-NLS-1$
            node.getChildren().add(iDocNode);

            // add functions
            createSAPIDocNodes(recBinNode, repObj, metadataConnection, iDocNode);

        } else {
            Set<org.talend.core.model.metadata.builder.connection.MetadataTable> tableset = ConnectionHelper
                    .getTables(metadataConnection);
            EList tables = new BasicEList();
            tables.addAll(tableset);
            createTables(recBinNode, node, repObj, tables, ERepositoryObjectType.METADATA_CON_TABLE);
        }
    }

    /**
     * DOC YeXiaowei Comment method "createSAPFunctionNodes".
     * 
     * @param metadataConnection
     * @param functionNode
     */
    private void createSAPFunctionNodes(final RepositoryNode recBin, IRepositoryViewObject rebObj, Connection metadataConnection,
            RepositoryNode functionNode) {
        EList functions = ((SAPConnection) metadataConnection).getFuntions();
        if (functions == null || functions.isEmpty()) {
            return;
        }
        for (int i = 0; i < functions.size(); i++) {
            SAPFunctionUnit unit = (SAPFunctionUnit) functions.get(i);
            RepositoryNode tableNode = createSAPNode(rebObj, functionNode, unit);

            createTables(recBin, tableNode, rebObj, unit.getTables(), ERepositoryObjectType.METADATA_CON_TABLE);
            if (SubItemHelper.isDeleted(unit)) {
                // recBin.getChildren().add(tableNode);
            } else {
                functionNode.getChildren().add(tableNode);
            }

        }
    }

    /**
     * DOC zli Comment method "createSAPIDocNodes".
     * 
     * @param recBin
     * @param rebObj
     * @param metadataConnection
     * @param iDocNode
     */
    private void createSAPIDocNodes(final RepositoryNode recBin, IRepositoryViewObject rebObj, Connection metadataConnection,
            RepositoryNode iDocNode) {
        EList iDocs = ((SAPConnection) metadataConnection).getIDocs();
        if (iDocs == null || iDocs.isEmpty()) {
            return;
        }
        for (int i = 0; i < iDocs.size(); i++) {
            SAPIDocUnit unit = (SAPIDocUnit) iDocs.get(i);
            RepositoryNode tableNode = createSAPNode(rebObj, iDocNode, unit);

            // createTables(recBin, tableNode, rebObj, unit.getTables(), ERepositoryObjectType.METADATA_CON_TABLE);
            if (SubItemHelper.isDeleted(unit)) {
                // recBin.getChildren().add(tableNode);
            } else {
                iDocNode.getChildren().add(tableNode);
            }

        }
    }

    /**
     * DOC YeXiaowei Comment method "createSAPNode".
     * 
     * @param rebObj
     * @param functionNode
     * @param unit
     * @return
     */
    private RepositoryNode createSAPNode(IRepositoryViewObject rebObj, RepositoryNode functionNode, SAPFunctionUnit unit) {
        SAPFunctionRepositoryObject modelObj = new SAPFunctionRepositoryObject(rebObj, functionNode, unit);
        modelObj.setLabel(unit.getName());
        RepositoryNode tableNode = new RepositoryNode(modelObj, functionNode, ENodeType.REPOSITORY_ELEMENT);
        tableNode.setProperties(EProperties.LABEL, modelObj.getLabel());
        tableNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_SAP_FUNCTION);
        return tableNode;
    }

    private RepositoryNode createSAPNode(IRepositoryViewObject rebObj, RepositoryNode functionNode, SAPIDocUnit unit) {
        SAPIDocRepositoryObject modelObj = new SAPIDocRepositoryObject(rebObj, functionNode, unit);
        modelObj.setLabel(unit.getName());
        RepositoryNode tableNode = new RepositoryNode(modelObj, functionNode, ENodeType.REPOSITORY_ELEMENT);
        tableNode.setProperties(EProperties.LABEL, modelObj.getLabel());
        tableNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_SAP_IDOC);
        return tableNode;
    }

    /**
     * DOC tguiu Comment method "createMetatable".
     * 
     * @param node
     * @param table
     * @param repositoryObjectType TODO
     * @param iMetadataFileDelimited
     * @return
     */
    private RepositoryNode createMetatableNode(RepositoryNode node, IRepositoryViewObject repObj,
            final org.talend.core.model.metadata.builder.connection.MetadataTable table,
            ERepositoryObjectType repositoryObjectType) {
        MetadataTableRepositoryObject modelObj = new MetadataTableRepositoryObject(repObj, table);
        RepositoryNode tableNode = new RepositoryNode(modelObj, node, ENodeType.REPOSITORY_ELEMENT);
        tableNode.setProperties(EProperties.LABEL, modelObj.getLabel());
        tableNode.setProperties(EProperties.CONTENT_TYPE, repositoryObjectType);
        return tableNode;

    }

    /**
     * DOC cantoine Comment method "createQueryNode".
     * 
     * @param node
     * @param repObj
     * @param query
     * @return
     */
    private RepositoryNode createQueryNode(RepositoryNode node, IRepositoryViewObject repObj, Query query) {
        QueryRepositoryObject modelObj = new QueryRepositoryObject(repObj, query);
        modelObj.setLabel(query.getLabel());
        RepositoryNode tableNode = new RepositoryNode(modelObj, node, ENodeType.REPOSITORY_ELEMENT);
        tableNode.setProperties(EProperties.LABEL, query.getLabel());
        tableNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.METADATA_CON_QUERY);
        return tableNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getCodeNode()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getCodeNode()
     */
    public RepositoryNode getCodeNode() {
        return this.codeNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getProcessNode()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getProcessNode()
     */
    public RepositoryNode getProcessNode() {
        return this.processNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataConNode()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataConNode()
     */
    public RepositoryNode getMetadataConNode() {
        return this.metadataConNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataConNode()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataConNode()
     */
    public RepositoryNode getMetadataSAPConnectionNode() {
        return this.metadataSAPConnectionNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataFileNode()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataFileNode()
     */
    public RepositoryNode getMetadataFileNode() {
        return this.metadataFileNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataFilePositionalNode()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataFilePositionalNode()
     */
    public RepositoryNode getMetadataFilePositionalNode() {
        return this.metadataFilePositionalNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataFileRegexpNode()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataFileRegexpNode()
     */
    public RepositoryNode getMetadataFileRegexpNode() {
        return this.metadataFileRegexpNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataFileXmlNode()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataFileXmlNode()
     */
    public RepositoryNode getMetadataFileXmlNode() {
        return this.metadataFileXmlNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataFileLdifNode()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataFileLdifNode()
     */
    public RepositoryNode getMetadataFileLdifNode() {
        return this.metadataFileLdifNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataLDAPSchemaNode()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataLDAPSchemaNode()
     */
    public RepositoryNode getMetadataLDAPSchemaNode() {
        return this.metadataLDAPSchemaNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataWSDLSchemaNode()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataWSDLSchemaNode()
     */
    public RepositoryNode getMetadataWSDLSchemaNode() {
        return this.metadataWSDLSchemaNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataFileExcelNode()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataFileExcelNode()
     */
    public RepositoryNode getMetadataFileExcelNode() {
        return this.metadataFileExcelNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataSalesforceSchemaNode()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataSalesforceSchemaNode()
     */
    public RepositoryNode getMetadataSalesforceSchemaNode() {
        return this.metadataSalesforceSchemaNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getJobletNode()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getJobletNode()
     */
    public RepositoryNode getJobletNode() {
        return this.jobletNode;
    }

    public RepositoryNode getReferenceProjectNode() {
        return this.refProject;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataNode()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataNode()
     */
    public RepositoryNode getMetadataNode() {
        return this.metadataNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataGenericSchemaNode()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getMetadataGenericSchemaNode()
     */
    public RepositoryNode getMetadataGenericSchemaNode() {
        return this.metadataGenericSchemaNode;
    }

    public RepositoryNode getMetadataEbcdicConnectionNode() {
        return this.metadataEbcdicConnectionNode;
    }

    public RepositoryNode getMetadataHL7ConnectionNode() {
        return this.metadataHL7ConnectionNode;
    }

    public RepositoryNode getMetadataFTPConnectionNode() {
        return this.metadataFTPConnectionNode;
    }

    public RepositoryNode getMetadataBRMSConnectionNode() {
        return this.metadataBRMSConnectionNode;
    }

    public RepositoryNode getMetadataMDMConnectionNode() {
        return this.metadataMDMConnectionNode;
    }

    public RepositoryNode getContextNode() {
        return this.contextNode;
    }

    public RepositoryNode getBusinessProcessNode() {
        return this.businessProcessNode;
    }

    public RepositoryNode getRoutineNode() {
        return this.routineNode;
    }

    public RepositoryNode getSQLPatternNode() {
        return this.sqlPatternNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getProject()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IProjectRepositoryNode#getProject()
     */
    public org.talend.core.model.general.Project getProject() {
        return this.project;
    }

    public RepositoryNode getRootRepositoryNode(ERepositoryObjectType type) {
        if (type == null) {
            return null;
        }

        switch (type) {
        case BUSINESS_PROCESS:
            return this.businessProcessNode;
        case PROCESS:
            return this.processNode;
        case CONTEXT:
            return this.contextNode;
        case ROUTINES:
            return this.routineNode;
        case JOB_SCRIPT:
            return this.jobscriptsNode;
        case SNIPPETS:
            return this.snippetsNode;
        case GENERATED:
        case JOBS:
        case JOB_DOC:
        case JOBLETS:
        case JOBLET_DOC:
        case DOCUMENTATION:
            return this.docNode;
            // case METADATA_CON_TABLE:
        case METADATA:
            return this.metadataNode; // maybe, there are some problems to process some fuctions.
        case METADATA_CON_VIEW:
        case METADATA_CON_SYNONYM:
        case METADATA_CON_QUERY:
        case METADATA_CON_CDC:
        case METADATA_CONNECTIONS:
            return this.metadataConNode;
        case METADATA_SAPCONNECTIONS:
            return this.metadataSAPConnectionNode;
        case METADATA_SAP_FUNCTION:
            return this.metadataSAPConnectionNode;
        case METADATA_SAP_IDOC:
            return this.metadataSAPConnectionNode;
        case METADATA_HEADER_FOOTER:
            return this.metadataHeaderFooterConnectionNode;
        case SQLPATTERNS:
            return this.sqlPatternNode;
        case METADATA_FILE_DELIMITED:
            return this.metadataFileNode;
        case METADATA_FILE_POSITIONAL:
            return this.metadataFilePositionalNode;
        case METADATA_FILE_REGEXP:
            return this.metadataFileRegexpNode;
        case METADATA_FILE_XML:
            return this.metadataFileXmlNode;
        case METADATA_FILE_LDIF:
            return this.metadataFileLdifNode;
        case METADATA_FILE_EXCEL:
            return this.metadataFileExcelNode;
        case METADATA_FILE_EBCDIC:
            return this.metadataEbcdicConnectionNode;
        case METADATA_FILE_HL7:
            return this.metadataHL7ConnectionNode;
        case METADATA_FILE_FTP:
            return this.metadataFTPConnectionNode;
        case METADATA_FILE_BRMS:
            return this.metadataBRMSConnectionNode;
        case METADATA_MDMCONNECTION:
        case MDM_CONCEPT:
            return this.metadataMDMConnectionNode;
        case METADATA_SALESFORCE_SCHEMA:
            return this.metadataSalesforceSchemaNode;
        case METADATA_GENERIC_SCHEMA:
            return this.metadataGenericSchemaNode;
        case METADATA_LDAP_SCHEMA:
            return this.metadataLDAPSchemaNode;
        case METADATA_WSDL_SCHEMA:
            return this.metadataWSDLSchemaNode;
        case METADATA_FILE_RULES:
        case METADATA_FILE_LINKRULES:
            return this.metadataRulesNode;
        case REFERENCED_PROJECTS:
            return this.refProject;
        case JOBLET:
            return this.jobletNode;
        case SVN_ROOT:
            return this.svnRootNode;
        default:
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.nodes.IProjectRepositoryNode#isMainProject()
     */
    public boolean isMainProject() {
        return getParent() == null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.nodes.IProjectRepositoryNode#getRecBinNode()
     */
    public RepositoryNode getRecBinNode() {
        return this.recBinNode;
    }

    public boolean getMergeRefProject() {
        IPreferenceStore preferenceStore = RepositoryManager.getPreferenceStore();
        this.mergeRefProject = preferenceStore.getBoolean(IRepositoryPrefConstants.MERGE_REFERENCE_PROJECT);
        return this.mergeRefProject;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.nodes.IProjectRepositoryNode#getMetadataRulesNode()
     */
    public RepositoryNode getMetadataRulesNode() {
        return this.metadataRulesNode;
    }

    public RepositoryNode getDocNode() {
        return this.docNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.nodes.IProjectRepositoryNode#getMetadataHeaderFooterConnectionNode()
     */
    public RepositoryNode getMetadataHeaderFooterConnectionNode() {
        // TODO Auto-generated method stub
        return this.metadataHeaderFooterConnectionNode;
    }

}
