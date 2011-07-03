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
package org.talend.repository.documentation;

/**
 * ftang class global comment. Detailled comment.
 */
public enum ERepositoryActionName {

    JOB_RESTORE("job restore"), //$NON-NLS-1$
    JOB_MOVE("job move"), //$NON-NLS-1$
    JOB_COPY("job copy"), //$NON-NLS-1$

    JOB_CREATE("job create"), //$NON-NLS-1$
    JOB_SAVE("job save"), //$NON-NLS-1$
    JOB_PROPERTIES_CHANGE("job properties change"), //$NON-NLS-1$
    JOB_DELETE_TO_RECYCLE_BIN("job delete to recycle bin"), //$NON-NLS-1$
    JOB_DELETE_FOREVER("job delete forever"), //$NON-NLS-1$
    BUSINESS_DELETE_TO_RECYCLE_BIN("business delete to recycle bin"), //$NON-NLS-1$
    BUSINESS_DELETE_FOREVER("business delete forever"), //$NON-NLS-1$

    // for normal job
    FOLDER_CREATE("folder create"), //$NON-NLS-1$
    FOLDER_DELETE("folder delete"), //$NON-NLS-1$
    FOLDER_RENAME("folder rename"), //$NON-NLS-1$
    FOLDER_MOVE("folder move"), //$NON-NLS-1$

    // for joblet
    JOBLET_MOVE("joblet.move"), //$NON-NLS-1$
    JOBLET_FOLDER_CREATE("joblet.folder.create"), //$NON-NLS-1$
    JOBLET_FOLDER_DELETE("joblet.folder.delete"), //$NON-NLS-1$
    JOBLET_FOLDER_RENAME("joblet.folder.rename"), //$NON-NLS-1$
    JOBLET_FOLDER_MOVE("joblet.folder.move"); //$NON-NLS-1$

    private String name;

    /**
     * ftang ERepositoryActionName constructor comment.
     * 
     * @param name
     */
    private ERepositoryActionName(String name) {
        this.name = name;
    }

    /**
     * ftang Comment method "getName".
     * 
     * @return
     */
    public String getName() {
        return this.name;
    }
}
