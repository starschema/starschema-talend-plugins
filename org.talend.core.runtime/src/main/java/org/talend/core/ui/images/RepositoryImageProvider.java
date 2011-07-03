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
package org.talend.core.ui.images;

import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.ICamelDesignerCoreService;

/**
 * ggu class global comment. Detailled comment
 */
public class RepositoryImageProvider {

    public static IImage getIcon(ERepositoryObjectType type) {

        if (type == ERepositoryObjectType.SVN_ROOT) {
            return ECoreImage.SVN_ROOT_ICON;
        } else if (type == ERepositoryObjectType.BUSINESS_PROCESS) {
            return ECoreImage.BUSINESS_PROCESS_ICON;
        } else if (type == ERepositoryObjectType.PROCESS) {
            return ECoreImage.PROCESS_ICON;
        } else if (type == ERepositoryObjectType.JOBLET) {
            return ECoreImage.JOBLET_ICON;
        } else if (type == ERepositoryObjectType.CONTEXT) {
            return ECoreImage.CONTEXT_ICON;
        } else if (type == ERepositoryObjectType.ROUTINES) {
            return ECoreImage.ROUTINE_ICON;
        } else if (type == ERepositoryObjectType.JOB_SCRIPT) {
            return ECoreImage.JOB_SCRIPTS_ICON;
        } else if (type == ERepositoryObjectType.SNIPPETS) {
            return ECoreImage.SNIPPETS_ICON;
        } else if (type == ERepositoryObjectType.DOCUMENTATION || type == ERepositoryObjectType.JOB_DOC
                || type == ERepositoryObjectType.JOBLET_DOC) {
            return ECoreImage.DOCUMENTATION_ICON;
        } else if (type == ERepositoryObjectType.METADATA) {
            return ECoreImage.METADATA_ICON;
        } else if (type == ERepositoryObjectType.METADATA_CONNECTIONS) {
            return ECoreImage.METADATA_CONNECTION_ICON;
        } else if (type == ERepositoryObjectType.METADATA_SAPCONNECTIONS || type == ERepositoryObjectType.METADATA_SAP_FUNCTION) {
            return ECoreImage.METADATA_SAPCONNECTION_ICON;
        } else if (type == ERepositoryObjectType.SQLPATTERNS) {
            return ECoreImage.METADATA_SQLPATTERN_ICON;
        } else if (type == ERepositoryObjectType.METADATA_CON_TABLE) {
            return ECoreImage.METADATA_TABLE_ICON;
        } else if (type == ERepositoryObjectType.METADATA_CON_COLUMN) {
            return ECoreImage.METADATA_COLUMN_ICON;
        } else if (type == ERepositoryObjectType.METADATA_CON_QUERY) {
            return ECoreImage.METADATA_QUERY_ICON;
        } else if (type == ERepositoryObjectType.METADATA_CON_VIEW) {
            return ECoreImage.METADATA_VIEW_ICON;
        } else if (type == ERepositoryObjectType.METADATA_CON_SYNONYM) {
            return ECoreImage.METADATA_SYNONYM_ICON;
        } else if (type == ERepositoryObjectType.METADATA_FILE_DELIMITED) {
            return ECoreImage.METADATA_FILE_DELIMITED_ICON;
        } else if (type == ERepositoryObjectType.METADATA_FILE_POSITIONAL) {
            return ECoreImage.METADATA_FILE_POSITIONAL_ICON;
        } else if (type == ERepositoryObjectType.METADATA_FILE_REGEXP) {
            return ECoreImage.METADATA_FILE_REGEXP_ICON;
        } else if (type == ERepositoryObjectType.METADATA_FILE_XML) {
            return ECoreImage.METADATA_FILE_XML_ICON;
        } else if (type == ERepositoryObjectType.METADATA_FILE_EXCEL) {
            return ECoreImage.METADATA_FILE_EXCEL_ICON;
        } else if (type == ERepositoryObjectType.METADATA_FILE_LDIF) {
            return ECoreImage.METADATA_FILE_LDIF_ICON;
        } else if (type == ERepositoryObjectType.FOLDER) {
            return ECoreImage.FOLDER_OPEN_ICON;
        } else if (type == ERepositoryObjectType.REFERENCED_PROJECTS) {
            return ECoreImage.REFERENCED_ICON;
        } else if (type == ERepositoryObjectType.METADATA_GENERIC_SCHEMA) {
            return ECoreImage.METADATA_GENERIC_ICON;
        } else if (type == ERepositoryObjectType.METADATA_FILE_FTP) {
            return ECoreImage.FTP_ICON;
        } else if (type == ERepositoryObjectType.METADATA_LDAP_SCHEMA) {
            return ECoreImage.METADATA_LDAP_SCHEMA_ICON;
        } else if (type == ERepositoryObjectType.METADATA_WSDL_SCHEMA) {
            return ECoreImage.METADATA_WSDL_SCHEMA_ICON;
        } else if (type == ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA) {
            return ECoreImage.METADATA_SALESFORCE_SCHEMA_ICON;
        } else if (type == ERepositoryObjectType.METADATA_SALESFORCE_MODULE) {
            return ECoreImage.METADATA_SALESFORCE_SCHEMA_ICON;
        } else if (type == ERepositoryObjectType.METADATA_FILE_EBCDIC) {
            return ECoreImage.METADATA_EBCDIC_CONNECTION_ICON;
        } else if (type == ERepositoryObjectType.METADATA_FILE_HL7) {
            return ECoreImage.METADATA_HL7_CONNECTION_ICON;
        } else if (type == ERepositoryObjectType.METADATA_FILE_BRMS) {
            return ECoreImage.METADATA_BRMS_CONNECTION_ICON;
        } else if (type == ERepositoryObjectType.METADATA_MDMCONNECTION) {
            return ECoreImage.METADATA_MDM_CONNECTION_ICON;
        } else if (type == ERepositoryObjectType.METADATA_FILE_RULES) {
            return ECoreImage.METADATA_RULES_ICON;
        } else if (type == ERepositoryObjectType.METADATA_FILE_LINKRULES) {
            return ECoreImage.METADATA_RULES_ICON;
        } else if (type == ERepositoryObjectType.METADATA_VALIDATION_RULES) {
            return ECoreImage.METADATA_ICON;
        } else if (type == ERepositoryObjectType.RECYCLE_BIN) {
            return ECoreImage.RECYCLE_BIN_EMPTY_ICON;
        } else if (type == ERepositoryObjectType.METADATA_EDIFACT) {
            return ECoreImage.METADATA_EDIFACT_ICON;
        } else {
            IImage image = null;
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                ICamelDesignerCoreService service = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                        ICamelDesignerCoreService.class);
                image = service.getCamelIcon(type);
                if (image != null) {
                    return image;
                } else {
                    return EImage.DEFAULT_IMAGE;
                }
            }
            return EImage.DEFAULT_IMAGE;
        }
    }
}
