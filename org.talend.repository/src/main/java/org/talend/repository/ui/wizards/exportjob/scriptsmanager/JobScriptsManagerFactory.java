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
package org.talend.repository.ui.wizards.exportjob.scriptsmanager;

import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.JobJavaScriptESBManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.JobJavaScriptOSGIForESBManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.petals.PetalsJobJavaScriptsManager;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 */
public class JobScriptsManagerFactory {

    private static JobScriptsManagerFactory instance;

    public static JobScriptsManagerFactory getInstance() {
        if (instance == null) {
            instance = new JobScriptsManagerFactory();
        }
        return instance;
    }

    public JobScriptsManager createDefaultManagerInstance() {

        JobScriptsManager manager = null;
        ECodeLanguage language = LanguageManager.getCurrentLanguage();
        if (language == ECodeLanguage.JAVA) {
            manager = new JobJavaScriptsManager();
        } else if (language == ECodeLanguage.PERL) {
            manager = new JobPerlScriptsManager();
        }
        return manager;
    }

    public JobScriptsManager createManagerInstance(ECodeLanguage language, JobExportType jobExportType) {

        JobScriptsManager manager = null;
        if (language == ECodeLanguage.JAVA) {
            switch (jobExportType) {
            case POJO:
                manager = new JobJavaScriptsManager();
                break;
            case WSWAR:
                manager = new JobJavaScriptsWSManager();
                break;
            case WSZIP:
                manager = new JobJavaScriptsWSManager();
                break;
            case JBOSSESB:
                manager = new JobJavaScriptESBManager();
                break;
            case PETALSESB:
                manager = new PetalsJobJavaScriptsManager();
                break;
            case OSGI:
                manager = new JobJavaScriptOSGIForESBManager();
                break;
            default:
                throw new RuntimeException("Export type [" + jobExportType + "] not handled."); //$NON-NLS-1$ //$NON-NLS-2$
            }

        } else if (language == ECodeLanguage.PERL) {
            manager = new JobPerlScriptsManager();
        }
        return manager;
    }
}
