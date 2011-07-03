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
package org.talend.core.language;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ICoreService;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.runtime.CoreRuntimePlugin;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public class LanguageManager {

    private static ICoreService coreService = (ICoreService) GlobalServiceRegister.getDefault().getService(ICoreService.class);

    private static ECodeLanguage currentLanguage;

    public static void reset() {
        currentLanguage = null;
    }

    public static ECodeLanguage getCurrentLanguage() {
        if (CoreRuntimePlugin.getInstance().getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY) == null) {
            if (coreService != null) {

                coreService.initializeForTalendStartupJob();
                String lanType = coreService.getLanTypeString();

                for (ECodeLanguage language : ECodeLanguage.values()) {
                    if (language.getName().equals(lanType)) {
                        return language;
                    }
                }
            }

            // the first time run talend in eclipse
            // TODO
            return ECodeLanguage.JAVA;
        }

        try {

            if (((RepositoryContext) CoreRuntimePlugin.getInstance().getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                    .getProject() != null) {
                currentLanguage = ((RepositoryContext) CoreRuntimePlugin.getInstance().getContext().getProperty(
                        Context.REPOSITORY_CONTEXT_KEY)).getProject().getLanguage();
            } else {
                currentLanguage = ECodeLanguage.PERL;
            }
        } catch (RuntimeException e) {
            // should be run only when testing
            // e.printStackTrace();
            ExceptionHandler.process(e);
            currentLanguage = ECodeLanguage.PERL;
        }
        return currentLanguage;
    }
}
