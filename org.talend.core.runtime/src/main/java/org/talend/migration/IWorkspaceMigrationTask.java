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
package org.talend.migration;

import java.util.Date;

/**
 * Define an atomic migration task to run on a workspace to assure comptibility trough Talend versions. See
 * org.talend.core.migrationTask extension point.<br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 * 
 */
public interface IWorkspaceMigrationTask {

    public String getId();

    public void setId(String id);

    public String getName();

    public void setName(String name);

    public boolean execute();

    // Use to manage task order. Tasks are sorted (ASC) by this date, then executed following this order.
    public Date getOrder();
}
