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
package org.talend.migration;

import java.util.Date;

import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;

/**
 * Define an atomic migration task to run on a project to assure comptibility trough Talend versions. See
 * org.talend.core.migrationTask extension point.<br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 * 
 */
public interface IProjectMigrationTask {

    public String getId();

    public void setId(String id);

    public String getName();

    public void setName(String name);

    public String getDescription();

    public void setDescription(String desc);

    public boolean isApplicableOnItems();

    public ExecutionResult execute(Project project);

    public ExecutionResult execute(Project project, Item item);

    // Use to manage task order. Tasks are sorted (ASC) by this date, then executed following this order.
    public Date getOrder();

    // In case the migration is not used anymore, it won't execute the migration at all, but it will keep in list for
    // already migrated
    public boolean isDeprecated();

    public void setStatus(ExecutionResult status);

    public ExecutionResult getStatus();

    /**
     * Represents the execution status of this task.
     */
    public enum ExecutionResult {
        SUCCESS_WITH_ALERT, // task successfully done, will be show to user in summary pop-up
        SUCCESS_NO_ALERT, // task successfully done, will NOT be show to user in summary pop-up (only in error log)
        NOTHING_TO_DO, // nothing to do in the current context, will NOT be show to user in summary pop-up neither in
        // error log
        FAILURE, // task failed (stacktrace in error log). will be retry on next login
        SKIPPED; // task not failed, not show to user in summary pop-up. will be retry on next login
    }
}
