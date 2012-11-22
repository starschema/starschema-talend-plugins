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
package org.talend.designer.codegen;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProjectReference;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;

/***/
public abstract class AbstractRoutineSynchronizer implements ITalendSynchronizer {

    private static Map<String, Date> id2date = new HashMap<String, Date>();

    protected List<IRepositoryViewObject> getRoutines() throws SystemException {
        List<IRepositoryViewObject> routineList = getMainProjectRoutine();
        // list.addAll(getReferencedProjectRoutine());

        // remove routine with same name in reference project
        Set<String> routineNames = new HashSet<String>();
        for (IRepositoryViewObject obj : routineList) {
            routineNames.add(obj.getProperty().getLabel());
        }

        List<IRepositoryViewObject> refRoutines = new ArrayList<IRepositoryViewObject>();
        getReferencedProjectRoutine(refRoutines, ProjectManager.getInstance().getReferencedProjects());
        for (IRepositoryViewObject obj : refRoutines) {
            String name = obj.getProperty().getLabel();
            // it does not have a routine with same name
            if (!routineNames.contains(name)) {
                routineNames.add(name);
                routineList.add(obj);
            }
        }
        return routineList;
    }

    protected List<IRepositoryViewObject> getBeans() throws SystemException {
        List<IRepositoryViewObject> beansList = getMainProjectBean();
        // list.addAll(getReferencedProjectRoutine());

        // remove routine with same name in reference project
        Set<String> beanNames = new HashSet<String>();
        for (IRepositoryViewObject obj : beansList) {
            beanNames.add(obj.getProperty().getLabel());
        }

        List<IRepositoryViewObject> refBeans = new ArrayList<IRepositoryViewObject>();
        getReferencedProjectBean(refBeans, ProjectManager.getInstance().getReferencedProjects());
        for (IRepositoryViewObject obj : refBeans) {
            String name = obj.getProperty().getLabel();
            // it does not have a routine with same name
            if (!beanNames.contains(name)) {
                beanNames.add(name);
                beansList.add(obj);
            }
        }
        return beansList;
    }

    private List<IRepositoryViewObject> getMainProjectRoutine() throws SystemException {
        IProxyRepositoryFactory repositoryFactory = getRepositoryService().getProxyRepositoryFactory();

        List<IRepositoryViewObject> routines;
        try {
            routines = repositoryFactory.getAll(ERepositoryObjectType.ROUTINES);
        } catch (PersistenceException e) {
            throw new SystemException(e);
        }
        return routines;
    }

    public IRepositoryService getRepositoryService() {
        IService service = GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
        return (IRepositoryService) service;
    }

    private List<IRepositoryViewObject> getMainProjectBean() throws SystemException {
        IProxyRepositoryFactory repositoryFactory = getRepositoryService().getProxyRepositoryFactory();

        List<IRepositoryViewObject> beans;
        try {
            beans = repositoryFactory.getAll(ERepositoryObjectType.getTypeFromKey("Beans"));
        } catch (PersistenceException e) {
            throw new SystemException(e);
        }
        return beans;
    }

    private void getReferencedProjectRoutine(List<IRepositoryViewObject> routines, List projects) throws SystemException {
        if (projects == null || projects.isEmpty()) {
            return;
        }
        IProxyRepositoryFactory repositoryFactory = getRepositoryService().getProxyRepositoryFactory();
        for (Object obj : projects) {
            Project project = null;
            if (obj instanceof Project) {
                project = (Project) obj;
            } else if (obj instanceof ProjectReference) {
                project = new Project(((ProjectReference) obj).getReferencedProject());
            }
            if (project != null) {
                try {
                    routines.addAll(repositoryFactory.getAll(project, ERepositoryObjectType.ROUTINES));
                } catch (PersistenceException e) {
                    throw new SystemException(e);
                }
                getReferencedProjectRoutine(routines, project.getEmfProject().getReferencedProjects());
            }
        }

    }

    private void getReferencedProjectBean(List<IRepositoryViewObject> beans, List projects) throws SystemException {
        if (projects == null || projects.isEmpty()) {
            return;
        }
        IProxyRepositoryFactory repositoryFactory = getRepositoryService().getProxyRepositoryFactory();
        for (Object obj : projects) {
            Project project = null;
            if (obj instanceof Project) {
                project = (Project) obj;
            } else if (obj instanceof ProjectReference) {
                project = new Project(((ProjectReference) obj).getReferencedProject());
            }
            if (project != null) {
                try {
                    beans.addAll(repositoryFactory.getAll(project, ERepositoryObjectType.getTypeFromKey("Beans")));
                } catch (PersistenceException e) {
                    throw new SystemException(e);
                }
                getReferencedProjectBean(beans, project.getEmfProject().getReferencedProjects());
            }
        }

    }

    public void syncRoutine(RoutineItem routineItem, boolean copyToTemp) throws SystemException {
        if (!isRoutineUptodate(routineItem) || !getFile(routineItem).exists()) {
            doSyncRoutine(routineItem, copyToTemp);
            setRoutineAsUptodate(routineItem);
        }
    }

    public void syncRoutine(RoutineItem routineItem) throws SystemException {
        if (routineItem != null) {
            doSyncRoutine(routineItem, true);
            setRoutineAsUptodate(routineItem);
        }
    }

    public void syncBean(Item beanItem, boolean copyToTemp) throws SystemException {
        if (!isBeanUptodate(beanItem) || !getFile(beanItem).exists()) {
            doSyncBean(beanItem, copyToTemp);
            setBeanAsUptodate(beanItem);
        }
    }

    protected boolean isBeanUptodate(Item beanItem) {
        Date refDate = getRefDate(beanItem);
        if (refDate == null) {
            return false;
        }
        Date date = id2date.get(beanItem.getProperty().getId());
        return refDate.equals(date);
    }

    protected void setBeanAsUptodate(Item beanItem) {
        Date refDate = getRefDate(beanItem);
        if (refDate == null) {
            return;
        }
        id2date.put(beanItem.getProperty().getId(), refDate);
    }

    private Date getRefDate(Item beanItem) {
        return beanItem.getProperty().getModificationDate();
    }

    protected abstract void doSyncRoutine(RoutineItem routineItem, boolean copyToTemp) throws SystemException;

    protected abstract void doSyncBean(Item beanItem, boolean copyToTemp) throws SystemException;

    public abstract void deleteRoutinefile(IRepositoryViewObject objToDelete);

    protected boolean isRoutineUptodate(RoutineItem routineItem) {
        Date refDate = getRefDate(routineItem);
        if (refDate == null) {
            return false;
        }
        Date date = id2date.get(routineItem.getProperty().getId());
        return refDate.equals(date);
    }

    protected void setRoutineAsUptodate(RoutineItem routineItem) {
        Date refDate = getRefDate(routineItem);
        if (refDate == null) {
            return;
        }
        id2date.put(routineItem.getProperty().getId(), refDate);
    }

    private Date getRefDate(RoutineItem routineItem) {
        if (routineItem.isBuiltIn()) {
            // FIXME mhelleboid for now, routines are deleted and recreated on
            // project logon
            // change this code, if one day routines are updated
            return routineItem.getProperty().getCreationDate();
        } else {
            return routineItem.getProperty().getModificationDate();
        }

    }

    public void forceSyncRoutine(RoutineItem routineItem) {
        id2date.remove(routineItem.getProperty().getId());
        try {
            getFile(routineItem).delete(true, new NullProgressMonitor());
        } catch (Exception e) {
            // ignore me
        }
    }

    // qli modified to fix the bug 5400 and 6185.
    public abstract void renameRoutineClass(RoutineItem routineItem);

    /**
     * bug 12582 by ggu.
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<URI>> getUserRoutineModules() {
        Map<String, List<URI>> modules = new HashMap<String, List<URI>>();

        try {
            for (IRepositoryViewObject ro : getRoutines()) {
                Item item = ro.getProperty().getItem();
                if (item instanceof RoutineItem) {
                    EList imports = ((RoutineItem) item).getImports();
                    for (Object o : imports) {
                        if (o instanceof IMPORTType) {
                            String urlPath = ((IMPORTType) o).getUrlPath();
                            if (urlPath != null && !"".equals(urlPath)) { //$NON-NLS-1$
                                File file = new File(urlPath);
                                if (file.exists()) {
                                    URI uri = file.toURI();
                                    List<URI> list = modules.get(ro.getLabel());
                                    if (list == null) {
                                        list = new ArrayList<URI>();
                                        modules.put(ro.getLabel(), list);
                                    }
                                    if (!list.contains(uri)) {
                                        list.add(uri);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (SystemException e) {
            ExceptionHandler.process(e);
        }
        return modules;
    }

    /**
     * DOC Administrator Comment method "renameBeanClass".
     * 
     * @param beanItem
     */
    public abstract void renameBeanClass(Item beanItem);
}
