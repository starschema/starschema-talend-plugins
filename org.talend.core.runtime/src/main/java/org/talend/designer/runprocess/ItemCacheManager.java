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
package org.talend.designer.runprocess;

import java.util.List;

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC nrousseau class global comment. Detailled comment
 *
 * @deprecated
 *
 * Do not cache anymore. This class actually is only used for compatibility, and keep the system of LASTEST_VERSION
 */
public class ItemCacheManager {

    public static final String LATEST_VERSION = "Latest"; //$NON-NLS-1$

    public static void clearCache() {
    }

    public static ProcessItem getProcessItem(Project project, String processId) {
        if (processId == null || "".equals(processId)) { //$NON-NLS-1$
            return null;
        }
        ProcessItem lastVersionOfProcess = null;

        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        try {
            IRepositoryViewObject object = factory.getLastVersion(project, processId);
            if (object == null || !(object.getProperty().getItem() instanceof ProcessItem)) {
                return null;
            }
            lastVersionOfProcess = (ProcessItem) object.getProperty().getItem();
            return lastVersionOfProcess;
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    public static ProcessItem getProcessItem(String processId) {
        ProjectManager instance = ProjectManager.getInstance();
        ProcessItem processItem = getRefProcessItem(instance.getCurrentProject(), processId);
        return processItem;
    }

    public static ProcessItem getRefProcessItem(Project project, String processId) {
        ProjectManager instance = ProjectManager.getInstance();
        ProcessItem processItem = getProcessItem(project, processId);
        if (processItem == null) {
            for (Project p : instance.getReferencedProjects(project)) {
                processItem = getRefProcessItem(p, processId);
                if (processItem != null) {
                    break;
                }
            }
        }

        return processItem;
    }

    public static ProcessItem getProcessItem(String processId, String version) {
        ProcessItem refProcessItem = getRefProcessItem(ProjectManager.getInstance().getCurrentProject(), processId, version);
        return refProcessItem;
    }

    public static ProcessItem getRefProcessItem(Project project, String processId, String version) {
        ProjectManager projectManager = ProjectManager.getInstance();
        ProcessItem processItem = getProcessItem(project, processId, version);
        if (processItem == null) {
            for (Project p : projectManager.getReferencedProjects(project)) {
                processItem = getRefProcessItem(p, processId, version);
                if (processItem != null) {
                    break;
                }
            }
        }
        return processItem;
    }

    public static ProcessItem getProcessItem(Project project, String processId, String version) {
        if (processId == null || "".equals(processId)) { //$NON-NLS-1$
            return null;
        }
        // feature 19312
        if (version == null || version.equals("") || LATEST_VERSION.equals(version)) {
            return getProcessItem(project, processId);
        }
        ProcessItem selectedProcessItem = null;

        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        try {

            List<IRepositoryViewObject> allVersions = factory.getAllVersion(project, processId, false);
            for (IRepositoryViewObject ro : allVersions) {
                if (ro.getVersion().equals(version) && ro.getProperty().getItem() instanceof ProcessItem) {
                    selectedProcessItem = (ProcessItem) ro.getProperty().getItem();
                    break;
                }
            }
            return selectedProcessItem;
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    public static String getProcessNameByProcessId(String processId) {
        ProcessItem item = getProcessItem(processId);
        if (item != null) {
            return item.getProperty().getLabel();
        }
        return null;
    }

    public static JobletProcessItem getJobletProcessItem(Project project, String jobletId) {
        if (jobletId == null || "".equals(jobletId)) { //$NON-NLS-1$
            return null;
        }
        JobletProcessItem lastVersionOfJoblet = null;

        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        try {
            IRepositoryViewObject object = factory.getLastVersion(project, jobletId);
            if (object == null || object.getRepositoryObjectType() != ERepositoryObjectType.JOBLET) {
                return null;
            }
            lastVersionOfJoblet = (JobletProcessItem) object.getProperty().getItem();
            return lastVersionOfJoblet;
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    public static JobletProcessItem getJobletProcessItem(String jobletId) {
        ProjectManager projectManager = ProjectManager.getInstance();
        JobletProcessItem jobletProcessItem = getJobletProcessItem(projectManager.getCurrentProject(), jobletId);

        if (jobletProcessItem == null) {
            for (Project p : projectManager.getReferencedProjects()) {
                jobletProcessItem = getJobletProcessItem(p, jobletId);
                if (jobletProcessItem != null) {
                    break;
                }
            }
        }
        return jobletProcessItem;
    }

    public static JobletProcessItem getJobletProcessItem(Project project, String jobletId, String version) {
        if (jobletId == null || "".equals(jobletId)) { //$NON-NLS-1$
            return null;
        }
        if (version == null || LATEST_VERSION.equals(version)) {
            return getJobletProcessItem(jobletId);
        }
        JobletProcessItem selectedProcessItem = null;

        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        try {

            List<IRepositoryViewObject> allVersions = factory.getAllVersion(project, jobletId, false);
            for (IRepositoryViewObject ro : allVersions) {
                if (ro.getRepositoryObjectType() == ERepositoryObjectType.JOBLET) {
                    if (ro.getVersion().equals(version)) {
                        selectedProcessItem = (JobletProcessItem) ro.getProperty().getItem();
                    }
                }
            }
            return selectedProcessItem;
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    public static JobletProcessItem getJobletProcessItem(String jobletId, String version) {
        ProjectManager projectManager = ProjectManager.getInstance();
        JobletProcessItem jobletProcessItem = getJobletProcessItem(projectManager.getCurrentProject(), jobletId, version);

        if (jobletProcessItem == null) {
            for (Project p : projectManager.getReferencedProjects()) {
                jobletProcessItem = getJobletProcessItem(p, jobletId, version);
                if (jobletProcessItem != null) {
                    break;
                }
            }
        }
        return jobletProcessItem;
    }
}
