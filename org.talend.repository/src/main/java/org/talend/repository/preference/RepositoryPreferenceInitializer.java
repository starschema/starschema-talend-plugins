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
package org.talend.repository.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.core.model.repository.IRepositoryPrefConstants;
import org.talend.core.model.repository.RepositoryManager;

/**
 * ggu class global comment. Detailled comment
 */
public class RepositoryPreferenceInitializer extends AbstractPreferenceInitializer {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        final IPreferenceStore preferenceStore = RepositoryManager.getPreferenceStore();
        // preferenceStore.setDefault(IRepositoryPrefConstants.MANUALLY_REFRESH, false);
        // preferenceStore.setDefault(IRepositoryPrefConstants.CREATING_REFRESH, true);
        // preferenceStore.setDefault(IRepositoryPrefConstants.SAVING_REFRESH, true);
        // preferenceStore.setDefault(IRepositoryPrefConstants.DELETING_REFRESH, true);
        preferenceStore.setDefault(IRepositoryPrefConstants.MERGE_REFERENCE_PROJECT, true);
    }

}
