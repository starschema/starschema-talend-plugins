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
package org.talend.core;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;

/**
 * This class can check whether some specific plugins are loaded or not. <br/>
 * 
 */
public class PluginChecker {

    private static final String DOCUMENTATION_PLUGIN_ID = "org.talend.repository.documentation"; //$NON-NLS-1$

    private static final String SNIPPET_PLUGIN_ID = "org.talend.snippets"; //$NON-NLS-1$

    private static final String JOBLET_PLUGIN_ID = "org.talend.designer.joblet"; //$NON-NLS-1$

    private static final String WSDL_PLUGIN_ID = "org.talend.wsdl.advancement"; //$NON-NLS-1$

    private static final String CDC_PLUGIN_ID = "org.talend.designer.cdc"; //$NON-NLS-1$

    private static final String SAP_WZIARD_PLUGIN_ID = "org.talend.repository.sapwizard"; //$NON-NLS-1$

    private static final String EBCDIC_PLUGIN_ID = "org.talend.repository.ebcdic"; //$NON-NLS-1$

    private static final String FTP_PLUGIN_ID = "org.talend.repository.ftp"; //$NON-NLS-1$

    private static final String MDM_PLUGIN_ID = "org.talend.repository.mdm"; //$NON-NLS-1$

    private static final String REF_PROJECT_PLUGIN_ID = "org.talend.repository.refproject"; //$NON-NLS-1$

    private static final String PERFORMANCE_PLUGIN_ID = "org.talend.designer.core.ui.preferences.PerformancePreferencePage"; //$NON-NLS-1$

    private static final String EXCHANGE_SYSTEM_PLUGIN_ID = "org.talend.designer.components.ecosystem"; //$NON-NLS-1$

    // added by hyWang
    private static final String RULES_PLUGIN_ID = "org.talend.repository.rules"; //$NON-NLS-1$   

    private static final String PREVIEW_PLUGIN_ID = "org.talend.designer.component.preview"; //$NON-NLS-1$   

    private static final String SVN_PROVIDER_PLUGIN_ID = "org.talend.repository.svnprovider"; //$NON-NLS-1$   

    private static final String CORE_TIS_PLUGIN_ID = "org.talend.core.tis"; //$NON-NLS-1$   

    private static final String HL7_PLUGIN_ID = "org.talend.repository.hl7"; //$NON-NLS-1$

    private static final String BRMS_ID = "org.talend.repository.brms"; //$NON-NLS-1$

    private static final String DEBUG_PLUGIN_ID = "org.talend.designer.runprocess.debug";

    private static final String WEBSERVICE_PLUGIN_ID = "org.talend.designer.webservice";

    private static final String METALANGUAGE_PLUGIN_ID = "org.talend.metalanguage.jobscript";

    private static final String VALIDATIONRULES_PLUGIN_ID = "org.talend.validationrules"; //$NON-NLS-1$

    private static final String EDIFACT_PLUGIN_ID = "org.talend.repository.edifact";

    private static final String DATACERT_PLUGIN_ID = "com.datacert.rest.core"; //$NON-NLS-1$

    private static final String RUNREMOTE_PLUGIN_ID = "org.talend.designer.runprocess.remote"; //$NON-NLS-1$ 

    private static final String TRACEDEBUG_PLUGIN_ID = "org.talend.designer.runprocess.debug"; //$NON-NLS-1$ 

    /**
     * Check if specific plug-in is loaded.
     * 
     * @return isLoaded
     */
    public static boolean isPluginLoaded(String pluginID) {
        boolean isLoaded = true;
        Bundle bundle = Platform.getBundle(pluginID);
        if (bundle == null || (bundle != null && bundle.getState() == Bundle.UNINSTALLED)) {
            isLoaded = false;
        }
        return isLoaded;
    }

    public static boolean isTIS() {
        return isJobLetPluginLoaded();
    }

    public static Plugin getWSDLPlugin() {
        return Platform.getPlugin(WSDL_PLUGIN_ID);
    }

    public static boolean isRefProjectLoaded() {
        return isPluginLoaded(REF_PROJECT_PLUGIN_ID);
    }

    public static boolean isWSDLPluginLoaded() {
        return isPluginLoaded(WSDL_PLUGIN_ID);
    }

    public static boolean isFTPPluginLoaded() {
        return isPluginLoaded(FTP_PLUGIN_ID);
    }

    /**
     * Checks if documentation plug-in is loaded.
     * 
     * @return isLoaded
     */
    public static boolean isDocumentationPluginLoaded() {
        return isPluginLoaded(DOCUMENTATION_PLUGIN_ID);
    }

    public static boolean isPerformancePluginLoaded() {
        return isPluginLoaded(PERFORMANCE_PLUGIN_ID);
    }

    /**
     * Check if snippets plug-in is loaded.
     * 
     * @return isLoaded
     */
    public static boolean isSnippetsPluginLoaded() {
        return isPluginLoaded(SNIPPET_PLUGIN_ID);
    }

    /**
     * DOC tang Comment method "isJobLetPluginLoaded".
     * 
     * @return
     */
    public static boolean isJobLetPluginLoaded() {
        return isPluginLoaded(JOBLET_PLUGIN_ID);
    }

    /**
     * DOC qzhang Comment method "containJoblet".
     * 
     * @return
     */
    public static boolean isCDCPluginLoaded() {
        return Platform.getBundle(CDC_PLUGIN_ID) != null;
    }

    public static boolean isSAPWizardPluginLoaded() {
        return isPluginLoaded(SAP_WZIARD_PLUGIN_ID);
    }

    public static boolean isEBCDICPluginLoaded() {
        return isPluginLoaded(EBCDIC_PLUGIN_ID);
    }

    public static boolean isHL7PluginLoaded() {
        return isPluginLoaded(HL7_PLUGIN_ID);
    }

    public static boolean isBRMSPluginLoaded() {
        return isPluginLoaded(BRMS_ID);
    }

    public static boolean isMDMPluginLoaded() {
        return isPluginLoaded(MDM_PLUGIN_ID);
    }

    public static boolean isExchangeSystemLoaded() {
        return isPluginLoaded(EXCHANGE_SYSTEM_PLUGIN_ID);
    }

    public static boolean isRulesPluginLoaded() { // added by hyWang
        return isPluginLoaded(RULES_PLUGIN_ID);
    }

    public static boolean isPreviewPluginLoaded() {
        return isPluginLoaded(PREVIEW_PLUGIN_ID);
    }

    public static boolean isSVNProviderPluginLoaded() {
        return isPluginLoaded(SVN_PROVIDER_PLUGIN_ID);
    }

    public static boolean isCoreTISPluginLoaded() {
        return isPluginLoaded(CORE_TIS_PLUGIN_ID);
    }

    public static boolean isDebugTisPluginLocaed() {
        return isPluginLoaded(DEBUG_PLUGIN_ID);
    }

    public static boolean isWebServicePluginLocaed() {
        return isPluginLoaded(WEBSERVICE_PLUGIN_ID);
    }

    public static boolean isMetalanguagePluginLoaded() {
        return isPluginLoaded(METALANGUAGE_PLUGIN_ID);
    }

    public static boolean isValidationrulesPluginLoaded() {
        return isPluginLoaded(VALIDATIONRULES_PLUGIN_ID);
    }

    public static boolean isRunRemotePluginLoaded() {
        return isPluginLoaded(RUNREMOTE_PLUGIN_ID);
    }

    public static boolean isTraceDebugPluginLoaded() {
        return isPluginLoaded(TRACEDEBUG_PLUGIN_ID);
    }

    public static boolean isTeamEdition() {
        return isPluginLoaded(REF_PROJECT_PLUGIN_ID);
    }

    public static String getSapWizardPluginId() {
        return SAP_WZIARD_PLUGIN_ID;
    }

    public static boolean isEDIFACTPluginLoaded() {
        return isPluginLoaded(EDIFACT_PLUGIN_ID);
    }

    public static boolean isDatacertPluginLoaded() {
        return isPluginLoaded(DATACERT_PLUGIN_ID);
    }
}
