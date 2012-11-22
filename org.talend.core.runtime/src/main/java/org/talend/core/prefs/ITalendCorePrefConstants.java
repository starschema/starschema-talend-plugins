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
package org.talend.core.prefs;

/**
 * Core preferences. Detailled comment <br/>
 * 
 * $Id: ITalendCorePrefConstants.java 46402 2010-08-06 08:19:31Z wzhang $
 * 
 */
public interface ITalendCorePrefConstants {

    /** Path to the directory of temporary files. */
    String FILE_PATH_TEMP = "filePathTemp"; //$NON-NLS-1$

    /** Perl interpreter. */
    String PERL_INTERPRETER = "perlInterpreter"; //$NON-NLS-1$

    /** Java interpreter. */
    String JAVA_INTERPRETER = "javaInterpreter"; //$NON-NLS-1$

    /** Lanugage selector. */
    String LANGUAGE_SELECTOR = "languageSelector"; //$NON-NLS-1$

    /** Perl library directory. */
    // String PERL_LIB = "perlLib";
    /** Known users list. */
    String USERS = "users"; //$NON-NLS-1$

    String CONNECTIONS = "connections"; //$NON-NLS-1$

    /** Last used connection. */
    String LAST_USED_CONNECTION = "lastUSedConnection"; //$NON-NLS-1$

    /** Last used project. */
    String LAST_USED_PROJECT = "lastUSedProject"; //$NON-NLS-1$

    String LAST_USED_SVN_BRANCH = "lastUSedSVNBranch"; //$NON-NLS-1$

    /** Last used user. */
    String LAST_USED_USER = "lastUSedUser"; //$NON-NLS-1$

    /** Preview Limit. */
    String PREVIEW_LIMIT = "previewLimit"; //$NON-NLS-1$

    String WORKSPACE_TASKS_DONE = "workspaceTasksDone"; //$NON-NLS-1$

    String RUN_IN_MULTI_THREAD = "runInMultiThread"; //$NON-NLS-1$

    String ALWAYS_WELCOME = "alwaysWelcome"; //$NON-NLS-1$

    String CONTEXT_GROUP_BY_SOURCE = "groupBySource"; //$NON-NLS-1$

    String SQL_ADD_QUOTE = "addSqlQuote"; //$NON-NLS-1$

    String SQL_ADD_WARNING = "addSqlWarning"; //$NON-NLS-1$

    String AS400_SQL_SEG = "AS400Sqlseg"; //$NON-NLS-1$

    String DOC_GENERATION = "doc_generation"; //$NON-NLS-1$

    String IREPORT_PATH = "ireport_path"; //$NON-NLS-1$

    String DOC_HIDEPASSWORDS = "doc_hidepasswords"; //$NON-NLS-1$

    String DOC_GENERATESOURCECODE = "doc_generatesourcecode"; //$NON-NLS-1$

    String COMMAND_STR = "CommandStr"; //$NON-NLS-1$

    String LINE_SEPERATOR_STR = "lineSeperator"; //$NON-NLS-1$

    String DEFAULT_COMMAND_STR = "%GENERATED_TOS_CALL%"; //$NON-NLS-1$

    public static final String DEACTIVE_REPOSITORY_UPDATE = "DEACTIVE_REPOSITORY_UPDATE"; //$NON-NLS-1$

    String DB_CONNECTION_TIMEOUT_ACTIVED = "db_conn_timeout_actived"; //$NON-NLS-1$

    String DB_CONNECTION_TIMEOUT = "db_conn_timeout"; //$NON-NLS-1$

    String DOC_USER_LOGO = "doc_user_logo";

    String DOC_COMPANY_NAME = "company_name";

    String ADD_SYSTEM_ROUTINES = "add_system_routines";

    String ADD_USER_ROUTINES = "add_user_routines";

    String USE_CSS_TEMPLATE = "use_css_template";

    String CSS_FILE_PATH = "css_file_path";

    String FORBIDDEN_MAPPING_LENGTH_PREC_LOGIC = "forbidden_mapping_length_prec_logic";

    String DATA_COLLECTOR_ENABLED = "active_data_collector";

    String DATA_COLLECTOR_UPLOAD_PERIOD = "active_data_collector_times";

    String DATA_COLLECTOR_LAST_TIME = "data_collector_last_time";

    String DATA_COLLECTOR_PREVIEW = "data_collector_preview";

    String DATA_COLLECTOR = "data_collector";

    String EXCHANGE_CHECK_TIS_VERSION = "exchange_check_tis_version";

    String EXCHANGE_DOWNLOADED_CHECK_UPDATES = "exchange_downloaded_check_updates";

    String MAXIMUM_AMOUNT_OF_COLUMNS_FOR_XML = "maximum_amount_of_columns_for_xml";

    String OOZIE_SCHEDULER_PATH = "oozie_scheduler_path";

    String OOZIE_SHCEDULER_NAME_NODE_ENDPOINT = "oozie_scheduler_name_node_endpoint";

    String OOZIE_SHCEDULER_JOB_TRACKER_ENDPOINT = "oozie_scheduler_job_tracker_endpoint";

    String OOZIE_SHCEDULER_OOZIE_ENDPOINT = "oozie_scheduler_oozie_endpoint";

    String OOZIE_SCHEDULER_USER_NAME = "oozie_scheduler_user_name";
}
