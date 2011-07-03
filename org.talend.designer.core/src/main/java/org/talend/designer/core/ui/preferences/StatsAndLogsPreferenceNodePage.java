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
package org.talend.designer.core.ui.preferences;

import org.talend.core.language.LanguageManager;

/**
 * DOC Administrator class global comment. Detailled comment <br/>
 * 
 * @deprecated see StatLogsProjectSettingPage
 */
public class StatsAndLogsPreferenceNodePage extends StatsAndLogsPreferencePage {

    public StatsAndLogsPreferenceNodePage() {
        super(LanguageManager.getCurrentLanguage());
        this.setTitle(getTitle() + " (" + LanguageManager.getCurrentLanguage().getCaseName() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
