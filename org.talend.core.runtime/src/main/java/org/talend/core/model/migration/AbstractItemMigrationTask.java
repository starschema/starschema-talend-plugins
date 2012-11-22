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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.runtime.i18n.Messages;
import org.talend.migration.AbstractMigrationTask;
import org.talend.migration.IProjectMigrationTask;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;

/**
 */
public abstract class AbstractItemMigrationTask extends AbstractMigrationTask implements IProjectMigrationTask {

    private static Logger log = Logger.getLogger(AbstractItemMigrationTask.class);

    private Project project;

    private ExecutionResult status;

    public ExecutionResult execute(Project project) {
        setProject(project);
        IRepositoryService service = (IRepositoryService) GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
        IProxyRepositoryFactory factory = service.getProxyRepositoryFactory();
        ExecutionResult executeFinal = null;
        List<IRepositoryViewObject> list = new ArrayList<IRepositoryViewObject>();
        try {
            for (ERepositoryObjectType curTyp : getTypes()) {
                if (curTyp.isResourceItem()) {
                    /* specific project so that on svn model it will migrate all ref projects,bug 17295 */
                    list.addAll(factory.getAll(project, curTyp, true, true));
                }
            }

            if (list.isEmpty()) {
                return ExecutionResult.NOTHING_TO_DO;
            }

            for (IRepositoryViewObject object : list) {
                ExecutionResult execute = null;
                // in case the resource has been modified (see MergeTosMetadataMigrationTask for example)
                if ((object.getProperty().eResource() == null || object.getProperty().getItem().eResource() == null)
                        && (object instanceof RepositoryObject)) {
                    Property updatedProperty = factory.reload(object.getProperty());
                    ((RepositoryObject) object).setProperty(updatedProperty);
                }

                Item item = object.getProperty().getItem();
                execute = execute(item);
                unloadObject(object);
                // if (item.getProperty().eResource().isModified()) {
                // factory.save(item, true);
                // item.getProperty().eResource().setModified(false);
                // }
                if (execute == ExecutionResult.FAILURE) {
                    log.warn(Messages.getString(
                            "AbstractItemMigrationTask.taskFailed", this.getName(), item.getProperty().getLabel())); //$NON-NLS-1$
                    executeFinal = ExecutionResult.FAILURE;
                }
                if (executeFinal != ExecutionResult.FAILURE) {
                    executeFinal = execute;
                }
            }

            return executeFinal;
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    public ExecutionResult execute(Project project, Item item) {
        if (!getTypes().contains(ERepositoryObjectType.getItemType(item))) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        setProject(project);
        return execute(item);
    }

    public abstract ExecutionResult execute(Item item);

    // if need to unload the object ,overide this method,see bug 21587
    protected void unloadObject(IRepositoryViewObject object) {

    };

    public List<ERepositoryObjectType> getTypes() {
        return Arrays.asList((ERepositoryObjectType[]) ERepositoryObjectType.values());
    }

    public final boolean isApplicableOnItems() {
        return true;
    }

    /**
     * Getter for project.
     * 
     * @return the project
     */
    public Project getProject() {
        return this.project;
    }

    /**
     * Sets the project.
     * 
     * @param project the project to set
     */
    public void setProject(Project project) {
        this.project = project;
    }

    public boolean isDeprecated() {
        return false;
    }

    public void setStatus(ExecutionResult status) {
        this.status = status;
    }

    public ExecutionResult getStatus() {
        return status;
    }

}
