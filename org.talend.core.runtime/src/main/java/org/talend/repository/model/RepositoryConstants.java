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
package org.talend.repository.model;

import org.talend.core.model.repository.ERepositoryObjectType;

/**
 * Defines some constants relative to repository such as file patterns.<br/>
 * 
 * $Id: RepositoryConstants.java 8 2006-10-02 09:09:23 +0000 (鏄熸湡涓� 02 �?鏈�2006) mhirt $
 * 
 */
public class RepositoryConstants {

    public static final String TABLE = "TABLE"; //$NON-NLS-1$

    public static final String TEMP_DIRECTORY = "temp"; //$NON-NLS-1$

    public static final String IMG_DIRECTORY = "images"; //$NON-NLS-1$

    public static final String IMG_DIRECTORY_OF_JOB_OUTLINE = "images/job_outlines"; //$NON-NLS-1$

    public static final String IMG_DIRECTORY_OF_JOBLET_OUTLINE = "images/joblet_outlines"; //$NON-NLS-1$

    public static final String JOB_DOCUMENTATION_PATH = "documentations/generated/jobs"; //$NON-NLS-1$

    public static final String JOBLET_DOCUMENTATION_PATH = "documentations/generated/joblets"; //$NON-NLS-1$

    public static final String SYSTEM_DIRECTORY = "system"; //$NON-NLS-1$

    public static final String USER_DEFINED = "UserDefined"; //$NON-NLS-1$

    public static final String CONTEXT_AND_VARIABLE_PATTERN = "^[a-zA-Z]+[a-zA-Z0-9\\_]*$"; //$NON-NLS-1$

    public static final String PROJECT_PATTERN = "^[a-zA-Z]+[a-zA-Z0-9 \\-_]*$"; //$NON-NLS-1$

    public static final String CODE_ITEM_PATTERN = "^[a-zA-Z]+[a-zA-Z0-9\\_]*$"; //$NON-NLS-1$

    public static final String ROUTINES_ITEM_PATTERN = "^[a-zA-Z\\_]+[a-zA-Z0-9\\_]*$"; //$NON-NLS-1$

    //    public static final String SCHEMA_NAME_PATTERN = "^[a-zA-Z0-9\\_]*$";; //$NON-NLS-1$

    public static final String COLUMN_NAME_PATTERN = "^[a-zA-Z_][a-zA-Z_0-9]*$"; //$NON-NLS-1$

    //    public static final String REPOSITORY_SCHEMA_PATTERN = "^[a-zA-Z0-9\\_]+$"; //$NON-NLS-1$

    public static final String FOLDER_PATTERN = "^[a-zA-Z]+[a-zA-Z0-9\\_]*$"; //$NON-NLS-1$

    public static final String REPOSITORY_ITEM_PATTERN_INTERN = "a-zA-Z0-9\\.\\-\\_\\ \\(\\)\\[\\]="; //$NON-NLS-1$

    public static final String REPOSITORY_ITEM_PATTERN = "^[" + REPOSITORY_ITEM_PATTERN_INTERN + "]+$"; //$NON-NLS-1$ //$NON-NLS-2$

    public static final String REPOSITORY_ITEM_WILDCARD = "^[" + REPOSITORY_ITEM_PATTERN_INTERN + "\\*\\?]+$"; //$NON-NLS-1$ //$NON-NLS-2$

    public static final String PATH_WILDCARD = "^[a-zA-Z\\*\\?/]+[a-zA-Z0-9\\_\\*\\?/]*$"; //$NON-NLS-1$

    public static final String PORT_ITEM_PATTERN = "^[0-9]"; //$NON-NLS-1$

    public static final String MAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*(\\.[_A-Za-z0-9-]+)"; //$NON-NLS-1$

    public static final String NOT_SET_STATUS = "not_set_status";

    public static final String REPOSITORY_LOCAL_ID = "local"; //$NON-NLS-1$

    public static final String REPOSITORY_REMOTE_ID = "remote"; //$NON-NLS-1$

    public static final String REPOSITORY_URL = "url"; //$NON-NLS-1$

    public static final String TDQ_PAT_ITEM_PATTERN = ".*"; //$NON-NLS-1$

    public static String getPattern(ERepositoryObjectType type) {
        if (type == ERepositoryObjectType.FOLDER) {
            return FOLDER_PATTERN;
        } else if (type == ERepositoryObjectType.PROCESS || type == ERepositoryObjectType.JOBLET) {
            return CODE_ITEM_PATTERN;
        } else if (type == ERepositoryObjectType.ROUTINES) {
            // for bug 10356
            return ROUTINES_ITEM_PATTERN;
        } else if (type == ERepositoryObjectType.TDQ_JRAXML_ELEMENT || type == ERepositoryObjectType.TDQ_DATA_PROFILING
                || type == ERepositoryObjectType.TDQ_ANALYSIS_ELEMENT || type == ERepositoryObjectType.TDQ_REPORT_ELEMENT
                || type == ERepositoryObjectType.TDQ_LIBRARIES || type == ERepositoryObjectType.TDQ_PATTERN_ELEMENT
                || type == ERepositoryObjectType.TDQ_PATTERN_REGEX || type == ERepositoryObjectType.TDQ_PATTERN_SQL
                || type == ERepositoryObjectType.TDQ_SOURCE_FILE_ELEMENT || type == ERepositoryObjectType.TDQ_RULES
                || type == ERepositoryObjectType.TDQ_RULES_SQL || type == ERepositoryObjectType.TDQ_INDICATOR_ELEMENT) {
            return TDQ_PAT_ITEM_PATTERN;
        } else {
            return TDQ_PAT_ITEM_PATTERN;
        }
    }

    public static boolean isSystemFolder(String label) {
        if (label == null) {
            return false;
        }
        return label.equalsIgnoreCase(SYSTEM_DIRECTORY);
    }

    public static boolean isGeneratedFolder(String label) {
        if (label == null) {
            return false;
        }
        return label.equalsIgnoreCase(ERepositoryObjectType.GENERATED.toString());
    }

    public static boolean isJobsFolder(String label) {
        if (label == null) {
            return false;
        }
        return label.equalsIgnoreCase(ERepositoryObjectType.JOBS.toString());
    }

    /**
     * DOC tang Comment method "isJobletsFolder".
     * 
     * @param label
     * @return
     */
    public static boolean isJobletsFolder(String label) {
        if (label == null) {
            return false;
        }
        return label.equalsIgnoreCase(ERepositoryObjectType.JOBLETS.toString());
    }
}
