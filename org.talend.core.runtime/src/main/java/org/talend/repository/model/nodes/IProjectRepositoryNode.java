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
package org.talend.repository.model.nodes;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.IRepositoryNode;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public interface IProjectRepositoryNode {

    /**
     * Getter for codeNode.
     * 
     * @return the codeNode
     */
    public IRepositoryNode getCodeNode();

    /**
     * Getter for processNode.
     * 
     * @return the processNode
     */
    public IRepositoryNode getProcessNode();

    /**
     * Getter for metadataConNode.
     * 
     * @return the metadataConNode
     */
    public IRepositoryNode getMetadataConNode();

    /**
     * Getter for metadataFileNode.
     * 
     * @return the metadataFileNode
     */
    public IRepositoryNode getMetadataFileNode();

    /**
     * Getter for metadataFilePositionalNode.
     * 
     * @return the metadataFilePositionalNode
     */
    public IRepositoryNode getMetadataFilePositionalNode();

    /**
     * Getter for metadataFileRegexpNode.
     * 
     * @return the metadataFileRegexpNode
     */
    public IRepositoryNode getMetadataFileRegexpNode();

    /**
     * Getter for metadataFileXmlNode.
     * 
     * @return the metadataFileXmlNode
     */
    public IRepositoryNode getMetadataFileXmlNode();

    /**
     * Getter for metadataFileLdifNode.
     * 
     * @return the metadataFileLdifNode
     */
    public IRepositoryNode getMetadataFileLdifNode();

    /**
     * Getter for metadataLDAPSchemaNode.
     * 
     * @return the metadataLDAPSchemaNode
     */
    public IRepositoryNode getMetadataLDAPSchemaNode();

    /**
     * Getter for metadataWSDLSchemaNode.
     * 
     * @return the metadataWSDLSchemaNode
     */
    public IRepositoryNode getMetadataWSDLSchemaNode();

    /**
     * Getter for metadataFileExcelNode.
     * 
     * @return the metadataFileExcelNode
     */
    public IRepositoryNode getMetadataFileExcelNode();

    /**
     * Getter for metadataSalesforceSchemaNode.
     * 
     * @return the metadataSalesforceSchemaNode
     */
    public IRepositoryNode getMetadataSalesforceSchemaNode();

    /**
     * Getter for jobletNode.
     * 
     * @return the jobletNode
     */
    public IRepositoryNode getJobletNode();

    /**
     * Getter for metadataNode.
     * 
     * @return the metadataNode
     */
    public IRepositoryNode getMetadataNode();

    /**
     * Getter for metadataGenericSchemaNode.
     * 
     * @return the metadataGenericSchemaNode
     */
    public IRepositoryNode getMetadataGenericSchemaNode();

    public IRepositoryNode getMetadataSAPConnectionNode();

    public IRepositoryNode getMetadataHeaderFooterConnectionNode();

    public IRepositoryNode getMetadataEbcdicConnectionNode();

    public IRepositoryNode getMetadataHL7ConnectionNode();

    public IRepositoryNode getMetadataFTPConnectionNode();

    public IRepositoryNode getMetadataBRMSConnectionNode();

    public IRepositoryNode getMetadataMDMConnectionNode();

    public IRepositoryNode getMetadataRulesNode();

    public IRepositoryNode getMetadataValidationRulesNode();

    /**
     * Getter for project.
     * 
     * @return the project
     */
    public org.talend.core.model.general.Project getProject();

    public boolean isMainProject();

    public IRepositoryNode getRootRepositoryNode(ERepositoryObjectType type);

    public IRepositoryNode getRecBinNode();

    public IRepositoryNode getBusinessProcessNode();

    public IRepositoryNode getRoutineNode();

    public IRepositoryNode getSQLPatternNode();

}
