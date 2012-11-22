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
package org.talend.core;

import java.net.URI;
import java.util.Collection;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public interface ILibraryManagerService extends IService {

    /**
     * DOC ycbai Comment method "isInitialized".
     * 
     * Check whether the OBR has been initialized.
     * 
     * @param monitorWrap
     * @return
     */
    public boolean isInitialized();

    public void setInitialized();

    /**
     * DOC ycbai Comment method "deploy".
     * 
     * Deploy jar file to OBR.
     * 
     * @param jarFileUri
     * @param monitorWrap
     */
    public void deploy(URI jarFileUri, IProgressMonitor... monitorWrap);

    public void deploy(Collection<URI> jarFileUris, IProgressMonitor... monitorWrap);

    /**
     * DOC ycbai Comment method "retrieve".
     * 
     * Retrieve jar file from OBR.
     * 
     * @param jarNeeded
     * @param pathToStore
     * @param monitorWrap
     * @return
     */
    public boolean retrieve(String jarNeeded, String pathToStore, IProgressMonitor... monitorWrap);

    public boolean retrieve(Collection<String> jarsNeeded, String pathToStore, IProgressMonitor... monitorWrap);

    /**
     * List all the jars (or other files) available.
     * 
     * @param monitorWrap
     * @return
     */
    public Set<String> list(IProgressMonitor... monitorWrap);

    public boolean delete(String jarName);

    public boolean contains(String jarName);

    public void clearCache();
}
