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
package org.talend.repository.utils;

import java.util.ArrayList;
import java.util.List;

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;

/**
 * ftang class global comment. Detailled comment <br/>
 * 
 */
public class JobVersionUtils {

    /**
     * ftang Comment method "getCurrentJobVersion".
     * 
     * @return
     */
    public static String getCurrentVersion(RepositoryNode repositoryNode) {
        try {
            return ProxyRepositoryFactory.getInstance().getLastVersion(repositoryNode.getId()).getVersion();
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * ftang Comment method "getAllVersions".
     * 
     * @return
     */
    public static String[] getAllVersions(RepositoryNode repositoryNode) {
        List<String> versionList = new ArrayList<String>();
        try {
            List<IRepositoryViewObject> allVersion = ProxyRepositoryFactory.getInstance().getAllVersion(
                    repositoryNode.getRoot().getProject(), repositoryNode.getId(), false);
            for (IRepositoryViewObject repositoryObject : allVersion) {
                versionList.add(repositoryObject.getVersion());
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return versionList.toArray(new String[versionList.size()]);
    }

}
