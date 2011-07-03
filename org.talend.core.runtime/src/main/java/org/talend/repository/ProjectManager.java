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
package org.talend.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.ecore.EObject;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ICoreService;
import org.talend.core.PluginChecker;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProjectReference;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.ui.IReferencedProjectService;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IProxyRepositoryService;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.nodes.IProjectRepositoryNode;

/**
 * ggu class global comment. Detailled comment
 */
public final class ProjectManager {

    private static ProjectManager singleton;

    private Project currentProject;

    private List<Project> referencedprojects = new ArrayList<Project>();

    private List<Project> allReferencedprojects = new ArrayList<Project>();

    private Map<String, String> mapProjectUrlToBranchUrl = new HashMap<String, String>();

    private static ICoreService coreSerivce = (ICoreService) GlobalServiceRegister.getDefault().getService(ICoreService.class);

    private static Map<String, List<FolderItem>> foldersMap = new HashMap<String, List<FolderItem>>();

    private ProjectManager() {
        initCurrentProject();
    }

    public static synchronized ProjectManager getInstance() {
        if (singleton == null) {
            singleton = new ProjectManager();
        }
        return singleton;
    }

    private void initCurrentProject() {
        Context ctx = CoreRuntimePlugin.getInstance().getContext();
        if (ctx != null) {
            RepositoryContext repositoryContext = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
            if (repositoryContext != null) {
                currentProject = repositoryContext.getProject();
                if (currentProject != null) {
                    resolveRefProject(currentProject.getEmfProject());
                }
                return;
            }
        }
        currentProject = null;
    }

    @SuppressWarnings("unchecked")
    private void resolveRefProject(org.talend.core.model.properties.Project p) {
        Context ctx = CoreRuntimePlugin.getInstance().getContext();
        if (p != null && ctx != null) {
            RepositoryContext repositoryContext = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
            String parentBranch = repositoryContext.getFields().get(
                    IProxyRepositoryFactory.BRANCH_SELECTION + "_" + p.getTechnicalLabel());
            for (ProjectReference pr : (List<ProjectReference>) p.getReferencedProjects()) {
                if (pr.getBranch() == null || parentBranch.equals(pr.getBranch())) {
                    resolveRefProject(pr.getReferencedProject()); // only to resolve all
                }
            }
        }
    }

    private void resolveSubRefProject(org.talend.core.model.properties.Project p) {
        Context ctx = CoreRuntimePlugin.getInstance().getContext();
        if (ctx != null && p != null) {
            RepositoryContext repositoryContext = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
            String parentBranch = repositoryContext.getFields().get(
                    IProxyRepositoryFactory.BRANCH_SELECTION + "_" + p.getTechnicalLabel());
            for (ProjectReference pr : (List<ProjectReference>) p.getReferencedProjects()) {
                if (pr.getBranch() == null || parentBranch.equals(pr.getBranch())) {
                    Project project = new Project(pr.getReferencedProject());
                    allReferencedprojects.add(project);
                    resolveSubRefProject(pr.getReferencedProject()); // only to resolve all
                }
            }
        }
    }

    /**
     * 
     * retrieve the referenced projects of current project.
     */
    public void retrieveReferencedProjects() {
        referencedprojects.clear();
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IProxyRepositoryService.class)) {
            IProxyRepositoryService service = (IProxyRepositoryService) GlobalServiceRegister.getDefault().getService(
                    IProxyRepositoryService.class);
            IProxyRepositoryFactory factory = service.getProxyRepositoryFactory();
            if (factory != null) {
                List<org.talend.core.model.properties.Project> rProjects = factory
                        .getReferencedProjects(this.getCurrentProject());
                if (rProjects != null) {
                    for (org.talend.core.model.properties.Project p : rProjects) {
                        Project project = new Project(p);
                        resolveRefProject(p);
                        referencedprojects.add(project);
                    }
                }
            }
        }
    }

    /**
     * return current project.
     * 
     */
    public Project getCurrentProject() {
        initCurrentProject();
        return this.currentProject;
    }

    /**
     * 
     * return the referenced projects of current project.
     */
    public List<Project> getReferencedProjects() {
        // if (this.referencedprojects.isEmpty() || CommonsPlugin.isHeadless()) {
        retrieveReferencedProjects();
        // }
        return this.referencedprojects;
    }

    /**
     * 
     * return all the referenced projects of current project.
     */
    public List<Project> getAllReferencedProjects() {
        // if (this.allReferencedprojects.isEmpty() || CommonsPlugin.isHeadless()) {
        allReferencedprojects.clear();
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IProxyRepositoryService.class)) {
            IProxyRepositoryService service = (IProxyRepositoryService) GlobalServiceRegister.getDefault().getService(
                    IProxyRepositoryService.class);
            IProxyRepositoryFactory factory = service.getProxyRepositoryFactory();
            if (factory != null) {
                List<org.talend.core.model.properties.Project> rProjects = factory
                        .getReferencedProjects(this.getCurrentProject());
                if (rProjects != null) {
                    for (org.talend.core.model.properties.Project p : rProjects) {
                        Project project = new Project(p);
                        allReferencedprojects.add(project);
                        resolveSubRefProject(p);
                    }
                }
            }
        }
        // }
        return this.allReferencedprojects;
    }

    /**
     * 
     * return the referenced projects of the project.
     */
    @SuppressWarnings("unchecked")
    public List<Project> getReferencedProjects(Project project) {
        Context ctx = CoreRuntimePlugin.getInstance().getContext();
        if (project != null && ctx != null) {
            if (project.equals(this.currentProject)) {
                return getReferencedProjects();
            }
            RepositoryContext repositoryContext = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
            String parentBranch = repositoryContext.getFields().get(
                    IProxyRepositoryFactory.BRANCH_SELECTION + "_" + project.getTechnicalLabel());

            List<Project> refProjects = new ArrayList<Project>();
            for (ProjectReference refProject : (List<ProjectReference>) project.getEmfProject().getReferencedProjects()) {
                if (refProject.getBranch() == null || parentBranch.equals(refProject.getBranch())) {
                    refProjects.add(new Project(refProject.getReferencedProject()));
                }
            }
            return refProjects;
        }
        return Collections.emptyList();
    }

    /**
     * 
     * return the project by object.
     */
    public org.talend.core.model.properties.Project getProject(EObject object) {
        if (object != null) {
            if (object instanceof org.talend.core.model.properties.Project) {
                return (org.talend.core.model.properties.Project) object;
            }
            if (object instanceof Property) {
                return getProject(((Property) object).getItem());
            }
            if (object instanceof Item) {
                return getProject(((Item) object).getParent());
            }
        }

        // default
        Project p = getCurrentProject();
        if (p != null) {
            return p.getEmfProject();
        }
        return null;
    }

    public IProject getResourceProject(org.talend.core.model.properties.Project project) {
        if (project != null) {
            try {
                return ResourceUtils.getProject(project.getTechnicalLabel());
            } catch (PersistenceException e) {
                //
            }
        }
        Project p = getCurrentProject();
        if (p != null) {
            return ResourcesPlugin.getWorkspace().getRoot().getProject(p.getEmfProject().getTechnicalLabel());
        }
        return null;
    }

    public IProject getResourceProject(EObject object) {
        return getResourceProject(getProject(object));
    }

    /**
     * 
     * ggu Comment method "isInCurrentMainProject".
     * 
     * check the EObject in current main project.
     */
    public boolean isInCurrentMainProject(EObject object) {
        if (object != null) {
            org.talend.core.model.properties.Project project = getProject(object);
            Project p = getCurrentProject();
            if (project != null && p != null) {
                return project.getTechnicalLabel().equals(p.getEmfProject().getTechnicalLabel());
            }
        }
        return false;
    }

    /**
     * 
     * ggu Comment method "isInCurrentMainProject".
     * 
     * check the node in current main project.
     */
    public boolean isInCurrentMainProject(IRepositoryNode node) {
        if (node != null) {
            Project curP = getCurrentProject();
            if (PluginChecker.isRefProjectLoaded()) {
                IReferencedProjectService service = (IReferencedProjectService) GlobalServiceRegister.getDefault().getService(
                        IReferencedProjectService.class);
                if (service != null && service.isMergeRefProject() && curP != null) {
                    IRepositoryViewObject object = node.getObject();
                    if (object == null) {
                        return true;
                    }
                    org.talend.core.model.properties.Project emfProject = getProject(object.getProperty().getItem());
                    org.talend.core.model.properties.Project curProject = curP.getEmfProject();
                    return emfProject.equals(curProject);

                } else {
                    IProjectRepositoryNode root = node.getRoot();
                    if (root != null) {
                        Project project = root.getProject();
                        if (project != null) {
                            return project.equals(curP);
                        } else {
                            return true;
                        }
                    }
                }
                // refplugin is not loaded
            } else {
                IProjectRepositoryNode root = node.getRoot();
                if (root != null) {
                    Project project = root.getProject();
                    if (project != null && curP != null) {
                        return project.getTechnicalLabel().equals(curP.getTechnicalLabel());
                    } else {
                        return true;
                    }
                }

            }
        }
        return false;
    }

    public static IProjectRepositoryNode researchProjectNode(Project project) {
        IProjectRepositoryNode root = (IProjectRepositoryNode) coreSerivce.getRoot();
        if (project == null || root.getProject().equals(project)) {
            return root;
        }
        IRepositoryNode refRoot = root.getRootRepositoryNode(ERepositoryObjectType.REFERENCED_PROJECTS);
        if (refRoot != null) {
            for (IRepositoryNode node : refRoot.getChildren()) {
                if (node instanceof IProjectRepositoryNode) {
                    IProjectRepositoryNode pNode = (IProjectRepositoryNode) node;
                    if (pNode.getProject().getTechnicalLabel().equals(project.getTechnicalLabel())) {
                        return pNode;
                    }
                }
            }
        }
        return null;
    }

    public String getCurrentBranchURL(Project project) {
        if (mapProjectUrlToBranchUrl != null && project != null && project.getEmfProject() != null) {
            return mapProjectUrlToBranchUrl.get(project.getEmfProject().getUrl());
        }
        return null;
    }

    public String getCurrentBranchURL(String projectUrl) {
        return mapProjectUrlToBranchUrl.get(projectUrl);
    }

    public void setCurrentBranchURL(Project project, String currentBranch) {
        mapProjectUrlToBranchUrl.put(project.getEmfProject().getUrl(), currentBranch);
    }

    public List<FolderItem> getFolders(org.talend.core.model.properties.Project project) {
        if (!foldersMap.containsKey(project.getTechnicalLabel())) {
            foldersMap.put(project.getTechnicalLabel(), new ArrayList<FolderItem>());
        }
        return foldersMap.get(project.getTechnicalLabel());
    }
}
