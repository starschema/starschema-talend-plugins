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
package org.talend.core.model.relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsService;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ItemRelation;
import org.talend.core.model.properties.ItemRelations;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.runtime.i18n.Messages;
import org.talend.core.service.IDesignerMapperService;
import org.talend.core.ui.IJobletProviderService;
import org.talend.designer.core.model.utils.emf.talendfile.AbstractExternalData;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;

/**
 * This class store all relationships between jobs/joblets and other items from the repository. Be sure to update the
 * INDEX_VERSION when change this class. This will force to run the migration task on the next logon on this project.
 * 
 * This actually can deal with: <br>
 * - Context (id = context id) <br>
 * - Property (id = connection id) <br>
 * - Schema (id = connection id + " - " + schema name)<br>
 * - Job (id = job id)<br>
 * - Joblet (id = joblet name)<br>
 * - Query (id = connection id + " - " + query name) <br>
 * - SQL Templates (id = SQLPattern id + "--" + SQLPattern name) <br>
 */
public class RelationshipItemBuilder {

    // upgrade of version must be done each time there is any change in this class.
    // this will force next time the project to upgrade to be sure to be up to date when logon.
    public static final String INDEX_VERSION = "1.2"; //$NON-NLS-1$

    public static final String LATEST_VERSION = "Latest"; //$NON-NLS-1$

    private static Logger log = Logger.getLogger(RelationshipItemBuilder.class);

    public static final String JOB_RELATION = "job"; //$NON-NLS-1$

    public static final String JOBLET_RELATION = "joblet"; //$NON-NLS-1$

    public static final String SERVICES_RELATION = "services"; //$NON-NLS-1$

    public static final String PROPERTY_RELATION = "property"; //$NON-NLS-1$

    public static final String SCHEMA_RELATION = "schema"; //$NON-NLS-1$

    public static final String VALIDATION_RULE_RELATION = "validationRuleRelation"; //$NON-NLS-1$

    public static final String QUERY_RELATION = "query"; //$NON-NLS-1$

    public static final String CONTEXT_RELATION = "context"; //$NON-NLS-1$

    public static final String SQLPATTERN_RELATION = "sqlPattern"; //$NON-NLS-1$

    public static final String ROUTINE_RELATION = "routine"; //$NON-NLS-1$

    public static final String SURVIVOR_RELATION = "survivorshipRuleRelation"; //$NON-NLS-1$

    public static RelationshipItemBuilder instance;

    private Map<Relation, Set<Relation>> currentProjectItemsRelations;

    private Map<Relation, Set<Relation>> referencesItemsRelations;

    private boolean loaded = false;

    private boolean loading = false;

    private boolean modified = false;

    private static final String COMMA = ";";

    private RelationshipItemBuilder() {

    }

    public static RelationshipItemBuilder getInstance() {
        if (instance == null) {
            instance = new RelationshipItemBuilder();
        }
        return instance;
    }

    public List<Relation> getItemsRelatedTo(String itemId, String version, String relationType) {
        if (!loaded) {
            loadRelations();
        }
        Set<Relation> relations = new HashSet<Relation>();
        Set<Relation> itemsRelations = getItemsRelatedTo(currentProjectItemsRelations, itemId, version, relationType);
        if (itemsRelations != null) {
            relations.addAll(itemsRelations);
        }
        itemsRelations = getItemsRelatedTo(referencesItemsRelations, itemId, version, relationType);
        if (itemsRelations != null) {
            relations.addAll(itemsRelations);
        }
        return new ArrayList<Relation>(relations);
    }

    private Set<Relation> getItemsRelatedTo(Map<Relation, Set<Relation>> itemsRelations, String itemId, String version,
            String relationType) {

        Relation itemToTest = new Relation();

        itemToTest.setId(itemId);
        itemToTest.setType(relationType);
        itemToTest.setVersion(version);
        if (itemsRelations.containsKey(itemToTest)) {
            return itemsRelations.get(itemToTest);
        }

        Set<Relation> relations = new HashSet<Relation>();

        for (Relation baseItem : itemsRelations.keySet()) {
            for (Relation relatedItem : itemsRelations.get(baseItem)) {
                String id = relatedItem.getId();
                if (id != null) {
                    Relation tmpRelatedItem = null;
                    if (id.indexOf(" - ") != -1) { //$NON-NLS-1$
                        try {
                            tmpRelatedItem = (Relation) relatedItem.clone();
                            tmpRelatedItem.setId(id.split(" - ")[0]); //$NON-NLS-1$
                            tmpRelatedItem.setType(relationType);
                        } catch (CloneNotSupportedException e) {
                            log.error(e);
                        }
                    } else {
                        tmpRelatedItem = relatedItem;
                    }
                    if (tmpRelatedItem != null && tmpRelatedItem.equals(itemToTest)) {
                        relations.add(baseItem);
                        break;
                    }
                }
            }
        }

        return relations;
    }

    public List<Relation> getItemsJobRelatedTo(String itemId, String version, String relationType) {
        if (!loaded) {
            loadRelations();
        }
        Set<Relation> relations = new HashSet<Relation>();
        Set<Relation> itemsRelations = getItemsJobRelatedTo(currentProjectItemsRelations, itemId, version, relationType);
        if (itemsRelations != null) {
            relations.addAll(itemsRelations);
        }
        itemsRelations = getItemsJobRelatedTo(referencesItemsRelations, itemId, version, relationType);
        if (itemsRelations != null) {
            relations.addAll(itemsRelations);
        }
        return new ArrayList<Relation>(relations);
    }

    private Set<Relation> getItemsJobRelatedTo(Map<Relation, Set<Relation>> itemsRelations, String itemId, String version,
            String relationType) {

        Relation itemToTest = new Relation();
        Set<Relation> jobRelations = new HashSet<Relation>();

        itemToTest.setId(itemId);
        itemToTest.setType(relationType);
        itemToTest.setVersion(version);
        if (itemsRelations.containsKey(itemToTest)) {
            Set<Relation> relations = itemsRelations.get(itemToTest);
            for (Relation relatedItem : relations) {
                if (relatedItem.getType().equals(relationType)) {
                    jobRelations.add(relatedItem);
                }
            }
            return jobRelations;
        }

        return jobRelations;
    }

    public void unloadRelations() {
        loaded = false;
        currentProjectItemsRelations = new HashMap<Relation, Set<Relation>>();
        referencesItemsRelations = new HashMap<Relation, Set<Relation>>();
    }

    private void loadRelations() {
        if (loading) {
            return;
        }
        loading = true;
        currentProjectItemsRelations = new HashMap<Relation, Set<Relation>>();
        referencesItemsRelations = new HashMap<Relation, Set<Relation>>();

        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        loadRelations(currentProjectItemsRelations, currentProject);

        List<Project> referencedProjects = ProjectManager.getInstance().getReferencedProjects();
        for (Project p : referencedProjects) {
            loadRelations(referencesItemsRelations, p);
        }

        loading = false;
        loaded = true;

    }

    private void loadRelations(Map<Relation, Set<Relation>> itemRelations, Project project) {

        for (Object o : project.getEmfProject().getItemsRelations()) {
            ItemRelations relations = (ItemRelations) o;
            Relation baseItem = new Relation();

            if (relations.getBaseItem() == null) {
                log.log(Level.WARN, "Error when load the relationship, so rebuild all..."); //$NON-NLS-1$
                loaded = false;
                project.getEmfProject().getItemsRelations().clear();
                buildIndex(itemRelations, project, new NullProgressMonitor());
                return;
            }

            baseItem.setId(relations.getBaseItem().getId());
            baseItem.setType(relations.getBaseItem().getType());
            baseItem.setVersion(relations.getBaseItem().getVersion());

            itemRelations.put(baseItem, new HashSet<Relation>());
            for (Object o2 : relations.getRelatedItems()) {
                ItemRelation emfRelatedItem = (ItemRelation) o2;

                Relation relatedItem = new Relation();
                relatedItem.setId(emfRelatedItem.getId());
                relatedItem.setType(emfRelatedItem.getType());
                relatedItem.setVersion(emfRelatedItem.getVersion());

                itemRelations.get(baseItem).add(relatedItem);
            }
        }

    }

    public boolean isNeedSaveRelations() {
        if (loaded && modified) {
            return true;
        }
        return false;
    }

    public void saveRelations() {
        if (!loaded && !modified) {
            return;
        }
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        currentProject.getEmfProject().getItemsRelations().clear();

        for (Relation relation : currentProjectItemsRelations.keySet()) {
            ItemRelations itemRelations = PropertiesFactory.eINSTANCE.createItemRelations();

            ItemRelation baseItem = PropertiesFactory.eINSTANCE.createItemRelation();
            itemRelations.setBaseItem(baseItem);

            baseItem.setId(relation.getId());
            baseItem.setType(relation.getType());
            baseItem.setVersion(relation.getVersion());
            for (Relation relatedItem : currentProjectItemsRelations.get(relation)) {
                ItemRelation emfRelatedItem = PropertiesFactory.eINSTANCE.createItemRelation();
                emfRelatedItem.setId(relatedItem.getId());
                emfRelatedItem.setType(relatedItem.getType());
                emfRelatedItem.setVersion(relatedItem.getVersion());
                itemRelations.getRelatedItems().add(emfRelatedItem);
            }
            currentProject.getEmfProject().getItemsRelations().add(itemRelations);
        }
        try {
            IRepositoryService service = (IRepositoryService) GlobalServiceRegister.getDefault().getService(
                    IRepositoryService.class);
            IProxyRepositoryFactory factory = service.getProxyRepositoryFactory();
            factory.saveProject(currentProject);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        modified = false;
    }

    private String getTypeFromItem(Item item) {
        String type = null;

        if (item instanceof ProcessItem) {
            type = JOB_RELATION;
        }
        if (item instanceof JobletProcessItem) {
            type = JOBLET_RELATION;
        }

        return type;
    }

    private void clearItemsRelations(Item baseItem) {
        Relation relation = new Relation();
        relation.setId(baseItem.getProperty().getId());
        relation.setType(getTypeFromItem(baseItem));
        relation.setVersion(baseItem.getProperty().getVersion());

        if (currentProjectItemsRelations.containsKey(relation)) {
            currentProjectItemsRelations.get(relation).clear();
        }
        if (referencesItemsRelations.containsKey(relation)) {
            referencesItemsRelations.get(relation).clear();
        }
    }

    private void addRelationShip(Item baseItem, String relatedId, String relatedVersion, String type) {
        Relation relation = new Relation();
        relation.setId(baseItem.getProperty().getId());
        relation.setType(getTypeFromItem(baseItem));
        relation.setVersion(baseItem.getProperty().getVersion());

        Relation addedRelation = new Relation();
        addedRelation.setId(relatedId);
        addedRelation.setType(type);
        addedRelation.setVersion(relatedVersion);
        Map<Relation, Set<Relation>> itemRelations = getRelatedRelations(baseItem);

        if (!itemRelations.containsKey(relation)) {
            itemRelations.put(relation, new HashSet<Relation>());
        }
        itemRelations.get(relation).add(addedRelation);
    }

    private Map<Relation, Set<Relation>> getRelatedRelations(Item baseItem) {
        Map<Relation, Set<Relation>> itemRelations = currentProjectItemsRelations;
        if (!ProjectManager.getInstance().isInCurrentMainProject(baseItem)) {
            itemRelations = referencesItemsRelations;
        }
        return itemRelations;

    }

    public boolean isAlreadyBuilt(Project project) {
        return !project.getEmfProject().getItemsRelations().isEmpty();
    }

    private void buildIndex(Map<Relation, Set<Relation>> itemRelations, Project project, IProgressMonitor monitor) {
        modified = true;

        if (!project.getEmfProject().getItemsRelations().isEmpty()) {
            loadRelations(itemRelations, project);
            if (loaded) { // check if already loaded successfully
                return;
            }
        }

        IRepositoryService service = (IRepositoryService) GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
        IProxyRepositoryFactory factory = service.getProxyRepositoryFactory();
        List<IRepositoryViewObject> list = new ArrayList<IRepositoryViewObject>();
        try {
            for (ERepositoryObjectType curTyp : getTypes()) {
                list.addAll(factory.getAll(curTyp, true, true));
            }
            monitor.beginTask(Messages.getString("RelationshipItemBuilder.buildingIndex"), list.size()); //$NON-NLS-1$

            if (list.isEmpty()) {
                return;
            }

            for (IRepositoryViewObject object : list) {
                Item item = object.getProperty().getItem();
                monitor.subTask(Messages.getString("RelationshipItemBuilder.forItem") + item.getProperty().getLabel()); //$NON-NLS-1$
                addOrUpdateItem(item, true);
                monitor.worked(1);
                if (monitor.isCanceled()) {
                    return;
                }
            }
            saveRelations();
            monitor.done();
            loaded = true;

        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
    }

    private List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.PROCESS);
        toReturn.add(ERepositoryObjectType.JOBLET);
        return toReturn;
    }

    public void updateItemVersion(Item baseItem, String oldVersion, String id, Map<String, String> versions)
            throws PersistenceException {
        updateItemVersion(baseItem, oldVersion, id, versions, false);
    }

    public void updateItemVersion(Item baseItem, String oldVersion, String id, Map<String, String> versions,
            boolean avoidSaveProject) throws PersistenceException {
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        IRepositoryViewObject obj = factory.getSpecificVersion(id, oldVersion, avoidSaveProject);
        Item item = obj.getProperty().getItem();
        // String itemVersion = item.getProperty().getVersion();
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        Project project = new Project(ProjectManager.getInstance().getProject(item));
        if (!loaded) {
            loadRelations();
        }
        if (!currentProject.equals(project)) { // only current project
            return;
        }
        ProcessType processType = null;
        if (item instanceof ProcessItem) {
            processType = ((ProcessItem) item).getProcess();
        }
        if (item instanceof JobletProcessItem) {
            processType = ((JobletProcessItem) item).getJobletProcess();
        }
        for (Object o : processType.getNode()) {
            if (o instanceof NodeType) {
                NodeType currentNode = (NodeType) o;
                if ("tRunJob".equals(currentNode.getComponentName())) { //$NON-NLS-1$
                    // in case of tRunJob
                    String jobIdStr = null;
                    String jobVersion = LATEST_VERSION;
                    String nowVersion = "";
                    Set<String> jobIdSet = new HashSet<String>();
                    for (Object o2 : currentNode.getElementParameter()) {
                        if (o2 instanceof ElementParameterType) {
                            ElementParameterType param = (ElementParameterType) o2;
                            if (param.getName().equals("PROCESS:PROCESS_TYPE_PROCESS") //$NON-NLS-1$
                                    || param.getName().equals("PROCESS_TYPE_PROCESS")) { //$NON-NLS-1$
                                // feature 19312
                                String jobIds = param.getValue();
                                String[] jobsArr = jobIds.split(RelationshipItemBuilder.COMMA);
                                for (String jobId : jobsArr) {
                                    if (StringUtils.isNotEmpty(jobId)) {
                                        jobIdSet.add(jobId);
                                    }
                                    jobIdStr = jobId;
                                }
                            }
                            if (param.getName().equals("PROCESS:PROCESS_TYPE_VERSION") //$NON-NLS-1$
                                    || param.getName().equals("PROCESS_TYPE_VERSION")) { //$NON-NLS-1$
                                jobVersion = param.getValue();
                                if (jobVersion.equals(LATEST_VERSION)) {
                                    if (!versions.isEmpty()) {
                                        nowVersion = versions.get(jobIdStr);
                                        param.setValue(nowVersion);
                                    }
                                }
                            }
                        }
                    }
                    for (String jobId : jobIdSet) {
                        addRelationShip(item, jobId, nowVersion, JOB_RELATION);
                        factory.save(project, item);
                    }
                }
            }
        }
        if (!avoidSaveProject) {
            saveRelations();
        }
    }

    public void addOrUpdateItem(Item item) {
        addOrUpdateItem(item, false);
    }

    public void addOrUpdateItem(Item item, boolean fromMigration) {
        if (!loaded) {
            loadRelations();
        }

        ProcessType processType = null;
        if (item instanceof ProcessItem) {
            processType = ((ProcessItem) item).getProcess();
        }
        if (item instanceof JobletProcessItem) {
            processType = ((JobletProcessItem) item).getJobletProcess();
        }
        if (processType != null) {
            boolean relationsModified = true;
            Relation relation = new Relation();
            relation.setId(item.getProperty().getId());
            relation.setType(getTypeFromItem(item));
            relation.setVersion(item.getProperty().getVersion());

            Set<Relation> oldProjectRelations = null;
            if (currentProjectItemsRelations.containsKey(relation)) {
                oldProjectRelations = new HashSet<Relation>(currentProjectItemsRelations.get(relation));
                currentProjectItemsRelations.get(relation).clear();
            }

            clearItemsRelations(item);

            Boolean builtIn = null;
            String currentValue = null;
            String relationType = null;
            // use a system of null value and relationType as the information repository / builtin can be stored
            // either before or after the informations of the repository value.

            for (Object o : processType.getContext()) {
                ContextType context = (ContextType) o;
                for (Object o2 : context.getContextParameter()) {
                    ContextParameterType contextParam = (ContextParameterType) o2;
                    if (!StringUtils.isEmpty(contextParam.getRepositoryContextId())) {
                        addRelationShip(item, contextParam.getRepositoryContextId(), LATEST_VERSION, CONTEXT_RELATION);
                    }
                }
            }

            // jobsetting parameters
            if (processType.getParameters() != null) {
                if (processType.getParameters().getRoutinesParameter() != null) {
                    for (Object o : processType.getParameters().getRoutinesParameter()) {
                        RoutinesParameterType itemInfor = (RoutinesParameterType) o;
                        addRelationShip(item, itemInfor.getName(), LATEST_VERSION, ROUTINE_RELATION);
                    }
                }
                for (Object o : processType.getParameters().getElementParameter()) {
                    if (o instanceof ElementParameterType) {
                        ElementParameterType param = (ElementParameterType) o;
                        if (param.getName().startsWith("SCHEMA:")) { //$NON-NLS-1$ 
                            relationType = SCHEMA_RELATION;
                        } else if (param.getName().startsWith("PROPERTY:")) { //$NON-NLS-1$ 
                            relationType = PROPERTY_RELATION;
                        } else if (param.getName().startsWith("VALIDATION_RULE_TYPE:")) {
                            relationType = VALIDATION_RULE_RELATION;
                        } else { // if no relation parameter, reset variables in case.
                            builtIn = null;
                            currentValue = null;
                        }
                        if (param.getName().endsWith(":PROPERTY_TYPE") || param.getName().endsWith(":SCHEMA_TYPE") || param.getName().endsWith(":VALIDATION_RULE_TYPE")) {//$NON-NLS-1$  //$NON-NLS-2$
                            builtIn = true;
                            if (param.getValue().equals("REPOSITORY")) { //$NON-NLS-1$
                                builtIn = false;
                            }
                        }
                        if (param.getName().endsWith(":REPOSITORY_PROPERTY_TYPE") || //$NON-NLS-1$
                                param.getName().endsWith(":REPOSITORY_SCHEMA_TYPE") || //$NON-NLS-1$
                                param.getName().endsWith(":REPOSITORY_VALIDATION_RULE_TYPE")) { //$NON-NLS-1$ 
                            currentValue = param.getValue();
                        }

                        if (builtIn != null && currentValue != null) { //$NON-NLS-1$
                            if (!builtIn) {
                                addRelationShip(item, currentValue, LATEST_VERSION, relationType);
                            }
                            builtIn = null;
                            currentValue = null;
                        }
                    }
                }
            }

            List<String> jobletsComponentsList = new ArrayList<String>();
            IComponentsService compService = (IComponentsService) GlobalServiceRegister.getDefault().getService(
                    IComponentsService.class);
            for (IComponent component : compService.getComponentsFactory().getComponents()) {
                if (component.getComponentType() == EComponentType.JOBLET) {
                    jobletsComponentsList.add(component.getName());
                }
            }
            builtIn = null;
            currentValue = null;
            for (Object o : processType.getNode()) {
                if (o instanceof NodeType) {
                    NodeType currentNode = (NodeType) o;
                    for (Object o2 : currentNode.getElementParameter()) {
                        if (o2 instanceof ElementParameterType) {
                            ElementParameterType param = (ElementParameterType) o2;

                            if (param.getName().startsWith("QUERYSTORE:")) { //$NON-NLS-1$ 
                                relationType = QUERY_RELATION;
                            } else if (param.getName().startsWith("SCHEMA:") || param.getName().startsWith("SCHEMA_OTHER:")) { //$NON-NLS-1$ 
                                relationType = SCHEMA_RELATION;
                            } else if (param.getName().startsWith("PROPERTY:")) { //$NON-NLS-1$ 
                                relationType = PROPERTY_RELATION;
                            } else if (param.getName().startsWith("VALIDATION_RULE_TYPE:")) {
                                relationType = VALIDATION_RULE_RELATION;
                            } else { // if no relation parameter, reset variables in case.
                                builtIn = null;
                                currentValue = null;
                            }
                            if (param.getName().endsWith(":PROPERTY_TYPE") || param.getName().endsWith(":SCHEMA_TYPE") //$NON-NLS-1$  //$NON-NLS-2$
                                    || param.getName().endsWith(":QUERYSTORE_TYPE") || param.getName().endsWith(":VALIDATION_RULE_TYPE")) { //$NON-NLS-1$
                                builtIn = true;
                                if (param.getValue().equals("REPOSITORY")) { //$NON-NLS-1$
                                    builtIn = false;
                                }
                            }
                            if (param.getName().endsWith(":REPOSITORY_PROPERTY_TYPE") || //$NON-NLS-1$
                                    param.getName().endsWith(":REPOSITORY_SCHEMA_TYPE") || //$NON-NLS-1$
                                    param.getName().endsWith(":REPOSITORY_QUERYSTORE_TYPE") ////$NON-NLS-1$
                                    || param.getName().endsWith(":REPOSITORY_VALIDATION_RULE_TYPE")) { //$NON-NLS-1$
                                currentValue = param.getValue();
                            }

                            if (builtIn != null && currentValue != null) { //$NON-NLS-1$
                                if (!builtIn) {
                                    addRelationShip(item, currentValue, LATEST_VERSION, relationType);
                                }
                                builtIn = null;
                                currentValue = null;
                            }

                            // only for SQL Patterns
                            if (param.getName().equals("SQLPATTERN_VALUE")) { //$NON-NLS-1$
                                for (Object o3 : param.getElementValue()) {
                                    if (o3 instanceof ElementValueType
                                            && "SQLPATTERNLIST".equals(((ElementValueType) o3).getElementRef())) { //$NON-NLS-1$
                                        addRelationShip(item, ((ElementValueType) o3).getValue(), LATEST_VERSION,
                                                SQLPATTERN_RELATION);
                                    }
                                }
                            }

                            // only for SurvivorshipFileItem
                            if (param.getField() != null
                                    && param.getField().equals(EParameterFieldType.SURVIVOR_RELATION.getName())) { //$NON-NLS-1$
                                String relatedID = param.getValue();
                                addRelationShip(item, relatedID, LATEST_VERSION, SURVIVOR_RELATION);
                            }

                        }
                    }
                    // handle tMap schema relations...
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerMapperService.class)) {
                        IDesignerMapperService service = (IDesignerMapperService) GlobalServiceRegister.getDefault().getService(
                                IDesignerMapperService.class);
                        AbstractExternalData nodeData = currentNode.getNodeData();
                        List<String> schemaIds = service.getRepositorySchemaIds(nodeData);
                        if (schemaIds.size() > 0) {
                            for (String schemaId : schemaIds) {
                                addRelationShip(item, schemaId, LATEST_VERSION, SCHEMA_RELATION);
                            }
                        }
                    }

                    if (jobletsComponentsList.contains(currentNode.getComponentName())) {
                        // in case of joblet
                        String version = LATEST_VERSION;
                        for (Object o2 : currentNode.getElementParameter()) {
                            if (o2 instanceof ElementParameterType) {
                                ElementParameterType param = (ElementParameterType) o2;
                                if (param.getName().equals("PROCESS_TYPE_VERSION")) { //$NON-NLS-1$
                                    version = param.getValue();
                                }
                            }
                        }
                        IComponent cc = compService.getComponentsFactory().get(currentNode.getComponentName());
                        IJobletProviderService service = null;
                        if (PluginChecker.isJobLetPluginLoaded()) {
                            service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                                    IJobletProviderService.class);
                        }

                        Property property = service.getJobletComponentItem(cc);
                        if (property != null)
                            addRelationShip(item, property.getId(), version, JOBLET_RELATION);
                    }
                    if ("tRunJob".equals(currentNode.getComponentName())) { //$NON-NLS-1$
                        // in case of tRunJob
                        String jobVersion = LATEST_VERSION;
                        Set<String> jobIdSet = new HashSet<String>();
                        for (Object o2 : currentNode.getElementParameter()) {
                            if (o2 instanceof ElementParameterType) {
                                ElementParameterType param = (ElementParameterType) o2;
                                if (param.getName().equals("PROCESS:PROCESS_TYPE_PROCESS") //$NON-NLS-1$
                                        || param.getName().equals("PROCESS_TYPE_PROCESS")) { //$NON-NLS-1$
                                    // feature 19312
                                    String jobIds = param.getValue();
                                    String[] jobsArr = jobIds.split(RelationshipItemBuilder.COMMA);
                                    for (String jobId : jobsArr) {
                                        if (StringUtils.isNotEmpty(jobId)) {
                                            jobIdSet.add(jobId);
                                            // addRelationShip(item, jobId, jobVersion, JOB_RELATION);
                                        }
                                    }
                                }
                                if (param.getName().equals("PROCESS:PROCESS_TYPE_VERSION") //$NON-NLS-1$
                                        || param.getName().equals("PROCESS_TYPE_VERSION")) { //$NON-NLS-1$
                                    jobVersion = param.getValue();
                                }
                            }
                        }
                        for (String jobId : jobIdSet) {
                            addRelationShip(item, jobId, jobVersion, JOB_RELATION);
                        }
                    }
                }
            }
            if (oldProjectRelations != null) {
                // check if there is any changes on the relations.
                Set<Relation> newProjectRelations = currentProjectItemsRelations.get(relation);
                if (oldProjectRelations.size() == newProjectRelations.size()) {
                    relationsModified = false;
                    for (Relation newRelation : newProjectRelations) {
                        if (!oldProjectRelations.contains(newRelation)) {
                            relationsModified = true;
                            break;
                        }
                    }
                }
                if (!relationsModified) {
                    currentProjectItemsRelations.get(relation).addAll(oldProjectRelations);
                }
            }
            if (relationsModified && !modified) {
                modified = true;
            }
            if (!fromMigration && modified) {
                saveRelations();
            }
        }
    }

    public void removeItemRelations(Item item) {
        if (!loaded) {
            loadRelations();
        }
        modified = true;

        ProcessType processType = null;
        if (item instanceof ProcessItem) {
            processType = ((ProcessItem) item).getProcess();
        }
        if (item instanceof JobletProcessItem) {
            processType = ((JobletProcessItem) item).getJobletProcess();
        }

        if (processType != null) {
            Relation relation = new Relation();
            relation.setId(item.getProperty().getId());
            relation.setType(getTypeFromItem(item));
            relation.setVersion(item.getProperty().getVersion());

            Map<Relation, Set<Relation>> itemRelations = getRelatedRelations(item);

            if (itemRelations.containsKey(relation)) {
                itemRelations.get(relation).clear();
                itemRelations.remove(relation);
                saveRelations();
            }
        }
    }

    /**
     * 
     * Relation class global comment. Detailled comment.
     */
    public class Relation implements Cloneable {

        private String type;

        private String id;

        private String version;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            result = prime * result + ((version == null) ? 0 : version.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Relation other = (Relation) obj;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            if (type == null) {
                if (other.type != null)
                    return false;
            } else if (!type.equals(other.type))
                return false;
            if (version == null) {
                if (other.version != null)
                    return false;
            } else if (!version.equals(other.version))
                return false;
            // if (name == null) {
            // if (other.name != null)
            // return false;
            // } else if (!name.equals(other.name)) {
            // return false;
            // }
            return true;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

}
