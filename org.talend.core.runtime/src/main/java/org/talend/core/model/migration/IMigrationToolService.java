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
package org.talend.core.model.migration;

import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.core.IService;
import org.talend.core.model.general.Project;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 * 
 */
public interface IMigrationToolService extends IService {

    public void executeWorspaceTasks();

    public void initNewProjectTasks(Project project);

    public boolean needExecutemigration();

    public void executeMigration(boolean pluginModel);

    public void executeProjectTasks(Project project, boolean beforeLogon, IProgressMonitor monitorWrap);

}
