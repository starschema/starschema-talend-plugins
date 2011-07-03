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
package org.talend.core.model.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.ProjectManager;

/**
 * An util tools for java version resources. Detailled comment <br/>
 * 
 */
/**
 * bqian An util tools for java version resources. <br/>
 * 
 */
public class JavaResourcesHelper {

    /**
     * Gets the specific resource in the java project.
     * 
     * @param name
     * @param projectName
     * 
     * @return
     */
    public static IResource getSpecificResourceInJavaProject(IPath path) throws CoreException {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            IRunProcessService service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
                    IRunProcessService.class);
            IProject project = service.getProject(ECodeLanguage.JAVA);
            IResource resource = project.findMember(path);
            return resource;
        }
        return null;
    }

    /**
     * Gets current project name.
     * 
     * @return
     */
    public static String getCurrentProjectName() {
        ProjectManager pManager = ProjectManager.getInstance();
        Project project = pManager.getCurrentProject();
        String name = project.getTechnicalLabel().toLowerCase();
        return name;
    }

    public static String getJobFolderName(String jobName, String version) {
        if (version != null) {
            return jobName.replaceAll(" ", "_").toLowerCase() + "_" + version.replace(".", "_"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        } else {
            return jobName.replaceAll(" ", "_").toLowerCase(); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static String getProjectFolderName(Property property) {
        if (property == null) {
            return ProjectManager.getInstance().getCurrentProject().getTechnicalLabel().toLowerCase();
        }
        return getProjectFolderName(property.getItem());
    }

    public static String getProjectFolderName(Item item) {
        ProjectManager pManager = ProjectManager.getInstance();
        org.talend.core.model.properties.Project p = pManager.getProject(item);
        String projectFolderName = p.getTechnicalLabel();
        projectFolderName = projectFolderName.toLowerCase();
        return projectFolderName;
    }

}
