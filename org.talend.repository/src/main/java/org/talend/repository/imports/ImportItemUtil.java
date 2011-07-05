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
package org.talend.repository.imports;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.ConnectionPackage;
import org.talend.core.model.properties.BusinessProcessItem;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.FileItem;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.FolderType;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.LinkDocumentationItem;
import org.talend.core.model.properties.LinkType;
import org.talend.core.model.properties.NotationHolder;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.PropertiesPackage;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.properties.SnippetItem;
import org.talend.core.model.properties.TDQItem;
import org.talend.core.model.properties.User;
import org.talend.core.model.properties.helper.ByteArrayResource;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.repository.model.PropertiesProjectResourceImpl;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.utils.RoutineUtils;
import org.talend.core.repository.utils.XmiResourceManager;
import org.talend.core.ui.IJobletProviderService;
import org.talend.designer.business.model.business.BusinessPackage;
import org.talend.designer.business.model.business.BusinessProcess;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFilePackage;
import org.talend.migration.IProjectMigrationTask;
import org.talend.migration.IProjectMigrationTask.ExecutionResult;
import org.talend.migrationtool.model.GetTasksHelper;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.constants.FileConstants;
import org.talend.repository.i18n.Messages;
import org.talend.repository.imports.ItemRecord.State;
import org.talend.repository.imports.TreeBuilder.ProjectNode;
import org.talend.repository.model.ComponentsFactoryProvider;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.actions.RestoreFolderUtil;
import org.talend.repository.utils.FileCopyUtils;

/**
 */
public class ImportItemUtil {

    private static Logger log = Logger.getLogger(ImportItemUtil.class);

    private XmiResourceManager xmiResourceManager = new XmiResourceManager();

    private boolean hasErrors = false;

    private RepositoryObjectCache cache = new RepositoryObjectCache();

    private TreeBuilder treeBuilder = new TreeBuilder();

    private Set<String> deletedItems = new HashSet<String>();

    private Map<IPath, Project> projects = new HashMap<IPath, Project>();

    private Map<String, Set<String>> routineExtModulesMap = new HashMap<String, Set<String>>();

    private boolean statAndLogsSettingsReloaded = false;

    private boolean implicitSettingsReloaded = false;

    private static boolean hasJoblets = false;

    private RestoreFolderUtil restoreFolder;

    public void clear() {
        deletedItems.clear();
    }

    public void setErrors(boolean errors) {
        this.hasErrors = errors;
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    private static IPath getPath(RepositoryNode node) {

        if (node.getType() == ENodeType.STABLE_SYSTEM_FOLDER || node.getType() == ENodeType.SYSTEM_FOLDER) {
            String prefix = ""; //$NON-NLS-1$
            ERepositoryObjectType type = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
            if (type == ERepositoryObjectType.METADATA_FILE_DELIMITED || type == ERepositoryObjectType.METADATA_FILE_POSITIONAL
                    || type == ERepositoryObjectType.METADATA_FILE_REGEXP || type == ERepositoryObjectType.METADATA_FILE_XML
                    || type == ERepositoryObjectType.METADATA_FILE_LDIF || type == ERepositoryObjectType.METADATA_FILE_EXCEL
                    || type == ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA
                    || type == ERepositoryObjectType.METADATA_GENERIC_SCHEMA
                    || type == ERepositoryObjectType.METADATA_LDAP_SCHEMA || type == ERepositoryObjectType.METADATA_CONNECTIONS
                    || type == ERepositoryObjectType.METADATA_SAPCONNECTIONS
                    || type == ERepositoryObjectType.METADATA_HEADER_FOOTER) {
                prefix = ERepositoryObjectType.METADATA.toString();
            }
            return new Path(prefix).append(node.getLabel());
        }

        String label = node.getObject().getProperty().getLabel();
        return getPath(node.getParent()).append(label);
    }

    private boolean checkItem(ItemRecord itemRecord, boolean overwrite) {

        boolean result = false;
        try {
            Item item = itemRecord.getItem();
            if (item instanceof TDQItem) {
                return false; // hide tdq first
            }
            ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(item);

            cache.initialize(itemType);

            boolean isSqlPattern = (itemType == ERepositoryObjectType.SQLPATTERNS);
            String itemPath = null;
            if (item.getState() != null) {
                itemPath = item.getState().getPath();
            }

            boolean nameAvailable = true;
            IRepositoryViewObject itemWithSameId = null;
            IRepositoryViewObject itemWithSameName = null;

            // take care, in cache it's RepositoryViewObject, not RepositoryObject
            for (IRepositoryViewObject current : cache.getItemsFromRepository().get(itemType)) {
                if (itemRecord.getProperty().getLabel().equalsIgnoreCase(current.getLabel())
                        && itemRecord.getProperty().getId() != current.getId()) {
                    // To check SQLPattern in same path. see bug 0005038: unable to add a SQLPattern into repository.
                    if (!isSqlPattern || current.getPath().equals(itemPath)) {
                        nameAvailable = false;
                    }
                    // overwrite the item with same label but diff id: 15787: import items does not overwrite some
                    // elements
                    if (!nameAvailable) {
                        itemWithSameName = current;
                    }
                }
                if (itemRecord.getProperty().getId().equalsIgnoreCase(current.getId())) {
                    itemWithSameId = current;
                }
            }
            itemRecord.setExistingItemWithSameId(itemWithSameId);
            boolean idAvailable = itemWithSameId == null;

            boolean isSystem = false;
            // we do not import built in routines
            if (item.eClass().equals(PropertiesPackage.eINSTANCE.getRoutineItem())) {
                RoutineItem routineItem = (RoutineItem) item;
                if (item instanceof RoutineItem) {
                    RoutineItem rItem = (RoutineItem) item;
                    Set<String> set = routineExtModulesMap.get(rItem.getProperty().getId());
                    if (set == null) {
                        set = new HashSet<String>();
                        routineExtModulesMap.put(rItem.getProperty().getId(), set);
                    }
                    for (IMPORTType type : (List<IMPORTType>) rItem.getImports()) {
                        set.add(type.getMODULE());
                    }
                }

                if (routineItem.isBuiltIn()) {
                    isSystem = true;
                }
            }

            // we do not import system sql patterns
            if (item.eClass().equals(PropertiesPackage.eINSTANCE.getSQLPatternItem())) {
                SQLPatternItem sqlPatternItem = (SQLPatternItem) item;
                if (sqlPatternItem.isSystem()) {
                    isSystem = true;
                }
            }

            if (isSystem) {
                itemRecord.addError(Messages.getString("RepositoryUtil.isSystem"));
                return result;
            }

            if (nameAvailable) {
                if (idAvailable) {
                    if (!isSystem) {
                        result = true;
                    } /*
                       * else { itemRecord.addError(Messages.getString("RepositoryUtil.isSystemRoutine")); //$NON-NLS-1$
                       * }
                       */
                } else {
                    // same id but different name,no need to care overwrite cause the item will be considered as a
                    // different one,see bug 20445
                    itemRecord.setState(State.ID_EXISTED);
                    // if (overwrite) {
                    // result = true;
                    // } else {
                    // see bug 0005222: [Import items] [Errors and Warnings]
                    // id is already in use
                    result = true;
                    // RepositoryNode nodeWithSameId = RepositoryNodeUtilities.getRepositoryNode(itemWithSameId);
                    // IPath path = getPath(nodeWithSameId);
                    // itemRecord.addError(Messages.getString(
                    //                                "RepositoryUtil.idUsed", itemWithSameId.getLabel(), path.toOSString())); //$NON-NLS-1$
                    // }
                }
            } else {
                if (idAvailable) {
                    // same name but different id
                    itemRecord.setState(State.NAME_EXISTED);

                    if (!isSystem && overwrite) {
                        // if anything system, don't replace the source item if same name.
                        // if not from system, can overwrite.
                        itemRecord.setExistingItemWithSameId(itemWithSameName);
                        result = true;
                    }

                } else {
                    // same name and same id
                    itemRecord.setState(State.NAME_EXISTED);
                    if (overwrite) {
                        result = true;
                    }
                    if (!isSystem && overwrite
                            && !itemWithSameName.getProperty().getLabel().equals(itemWithSameId.getProperty().getLabel())) {
                        // if anything system, don't replace the source item if same name.
                        // if not from system, can overwrite.
                        itemRecord.setExistingItemWithSameId(itemWithSameName);
                        itemRecord.setState(State.NAME_AND_ID_EXISTED);
                        result = true;
                    }
                }
                if (!result && !isSystem) {
                    itemRecord.addError(Messages.getString("RepositoryUtil.nameUsed")); //$NON-NLS-1$
                }
            }

            if (result && overwrite && itemRecord.getState() == State.ID_EXISTED) {
                // if item is locked, cannot overwrite
                if (checkIfLocked(itemRecord)) {
                    itemRecord.addError(Messages.getString("RepositoryUtil.itemLocked")); //$NON-NLS-1$
                    result = false;
                }
            }
        } catch (Exception e) {
            log.error("Error when checking item :" + itemRecord.getPath(), e);
        }
        return result;
    }

    /**
     * DOC hcw Comment method "checkIfLocked".
     * 
     * @param itemRecord
     * @return
     * @throws PersistenceException
     */
    private boolean checkIfLocked(ItemRecord itemRecord) throws PersistenceException {
        Boolean lockState = cache.getItemLockState(itemRecord);
        if (lockState != null) {
            return lockState.booleanValue();
        }

        List<IRepositoryViewObject> list = cache.findObjectsByItem(itemRecord);

        for (IRepositoryViewObject obj : list) {
            ERepositoryStatus status = obj.getRepositoryStatus();
            if (status == ERepositoryStatus.LOCK_BY_OTHER || status == ERepositoryStatus.LOCK_BY_USER) {
                itemRecord.setLocked(true);
                cache.setItemLockState(itemRecord, true);
                return true;
            }
        }

        cache.setItemLockState(itemRecord, false);
        return false;
    }

    @SuppressWarnings("unchecked")
    public List<ItemRecord> importItemRecords(final ResourcesManager manager, final List<ItemRecord> itemRecords,
            final IProgressMonitor monitor, final boolean overwrite, final IPath destinationPath, final String contentType) {
        hasJoblets = false;
        statAndLogsSettingsReloaded = false;
        implicitSettingsReloaded = false;
        restoreFolder = new RestoreFolderUtil();

        Collections.sort(itemRecords, new Comparator<ItemRecord>() {

            public int compare(ItemRecord o1, ItemRecord o2) {
                if (o1.getProperty().getItem() instanceof RoutineItem) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        monitor.beginTask(Messages.getString("ImportItemWizardPage.ImportSelectedItems"), itemRecords.size() * 2 + 1); //$NON-NLS-1$

        RepositoryWorkUnit repositoryWorkUnit = new RepositoryWorkUnit("Import Items") { //$NON-NLS-1$

            @Override
            public void run() throws PersistenceException {
                final IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();
                // bug 10520
                final Set<String> overwriteDeletedItems = new HashSet<String>();

                Map<String, String> nameToIdMap = new HashMap<String, String>();

                for (ItemRecord itemRecord : itemRecords) {
                    if (!monitor.isCanceled()) {
                        if (itemRecord.isValid()) {
                            if (itemRecord.getState() == State.ID_EXISTED || itemRecord.getState() == State.NAME_AND_ID_EXISTED) {
                                String id = nameToIdMap.get(itemRecord.getProperty().getLabel()
                                        + ERepositoryObjectType.getItemType(itemRecord.getProperty().getItem()).toString());
                                if (id == null) {
                                    /*
                                     * if id exsist then need to genrate new id for this job,in this case the job won't
                                     * override the old one
                                     */
                                    id = EcoreUtil.generateUUID();
                                    nameToIdMap.put(
                                            itemRecord.getProperty().getLabel()
                                                    + ERepositoryObjectType.getItemType(itemRecord.getProperty().getItem())
                                                            .toString(), id);
                                }
                                itemRecord.getProperty().setId(id);
                            }
                        }
                    }
                }

                for (ItemRecord itemRecord : itemRecords) {
                    if (!monitor.isCanceled()) {
                        if (itemRecord.isValid()) {
                            importItemRecord(manager, itemRecord, overwrite, destinationPath, overwriteDeletedItems, contentType,
                                    monitor);

                            monitor.worked(1);
                        }
                    }
                }
                // deploy routines Jar
                if (!getRoutineExtModulesMap().isEmpty()) {
                    Set<String> extRoutines = new HashSet<String>();
                    for (String id : getRoutineExtModulesMap().keySet()) {
                        Set<String> set = getRoutineExtModulesMap().get(id);
                        if (set != null) {
                            extRoutines.addAll(set);
                        }
                    }
                    if (manager instanceof ProviderManager || manager instanceof ZipFileManager) {
                        deployJarToDesForArchive(manager, extRoutines);
                    } else {
                        deployJarToDes(manager, extRoutines);
                    }
                }
                if (PluginChecker.isJobLetPluginLoaded()) {
                    IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                            IJobletProviderService.class);
                    if (service != null) {
                        service.loadComponentsFromProviders();
                    }
                }
                // cannot cancel this part
                //                monitor.beginTask(Messages.getString("ImportItemWizardPage.ApplyMigrationTasks"), itemRecords.size() + 1); //$NON-NLS-1$
                // for (ItemRecord itemRecord : itemRecords) {
                // if (itemRecord.isImported()) {
                // applyMigrationTasks(itemRecord, monitor);
                // }
                // monitor.worked(1);
                // }
                checkDeletedFolders();
                monitor.done();
                if (RelationshipItemBuilder.getInstance().isNeedSaveRelations()) {
                    RelationshipItemBuilder.getInstance().saveRelations();
                } else {
                    // only save the project here if no relation need to be saved, since project will already be saved
                    // with relations
                    factory.saveProject(ProjectManager.getInstance().getCurrentProject());
                }
            }
        };
        repositoryWorkUnit.setAvoidUnloadResources(true);
        repositoryWorkUnit.setUnloadResourcesAfterRun(true);
        ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(repositoryWorkUnit);

        monitor.done();

        // for (ItemRecord itemRecord : itemRecords) {
        // itemRecord.clear();
        // }

        clearAllData();
        if (hasJoblets) {
            ComponentsFactoryProvider.getInstance().resetSpecificComponents();
        }

        return itemRecords;
    }

    private void checkDeletedFolders() {
        List<FolderItem> foldersList = (List<FolderItem>) ProjectManager.getInstance().getFolders(
                ProjectManager.getInstance().getCurrentProject().getEmfProject());
        for (FolderItem folderItem : foldersList) {
            setPathToDeleteIfNeed(folderItem);
        }
    }

    private boolean setPathToDeleteIfNeed(FolderItem folderItem) {
        if (folderItem.getState().isDeleted()) {
            return true;
        }
        boolean allDeleted = folderItem.getType().getValue() == FolderType.FOLDER && folderItem.getChildren().size() != 0;
        for (Item item : (List<Item>) folderItem.getChildren()) {
            if (item instanceof FolderItem) {
                if (!setPathToDeleteIfNeed((FolderItem) item)) {
                    allDeleted = false;
                }
            }
            if (!item.getState().isDeleted()) {
                allDeleted = false;
            }
        }
        if (allDeleted) {
            folderItem.getState().setDeleted(true);
            String fullPath = "";
            FolderItem curItem = folderItem;
            while (curItem.getParent() instanceof FolderItem && ((Item) curItem.getParent()).getParent() instanceof FolderItem
                    && ((FolderItem) ((Item) curItem.getParent()).getParent()).getType().getValue() == FolderType.FOLDER) {
                FolderItem parentFolder = (FolderItem) curItem.getParent();
                if ("".equals(fullPath)) {
                    fullPath = parentFolder.getProperty().getLabel() + fullPath;
                } else {
                    fullPath = parentFolder.getProperty().getLabel() + "/" + fullPath;
                }
                curItem = parentFolder;
            }
            folderItem.getState().setPath(fullPath);
        }
        return allDeleted;
    }

    public void clearAllData() {
        deletedItems.clear();
        cache.clear();
        treeBuilder.clear();
        xmiResourceManager.unloadResources();
        xmiResourceManager.resetResourceSet();
        projects.clear();
    }

    private void importItemRecord(ResourcesManager manager, ItemRecord itemRecord, boolean overwrite, IPath destinationPath,
            final Set<String> overwriteDeletedItems, String contentType, final IProgressMonitor monitor) {

        monitor.subTask(Messages.getString("ImportItemWizardPage.Importing") + itemRecord.getItemName()); //$NON-NLS-1$

        resolveItem(manager, itemRecord);

        int num = 0;
        for (Object obj : itemRecord.getResourceSet().getResources()) {
            if (!(obj instanceof PropertiesProjectResourceImpl)) {
                if (obj instanceof XMIResourceImpl) {
                    num++;
                    if (num > 2) {// The is no explanation for this value and what is this loop for to I increased
                        // it to
                        // 2 so that metadata migration for 4.1 works
                        try {
                            throw new InvocationTargetException(new PersistenceException("The source file of "
                                    + itemRecord.getLabel() + " has error,Please check it!"));
                        } catch (InvocationTargetException e) {
                            ExceptionHandler.process(e);
                        }
                        return;
                    }
                }
            }
        }

        final Item item = itemRecord.getItem();
        if (item != null) {
            ProxyRepositoryFactory repFactory = ProxyRepositoryFactory.getInstance();
            ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(item);

            IPath path = new Path(item.getState().getPath());
            if (destinationPath != null && itemType.name().equals(contentType)) {
                path = destinationPath.append(path);
            }

            try {
                repFactory.createParentFoldersRecursively(ProjectManager.getInstance().getCurrentProject(), itemType, path, true);
            } catch (Exception e) {
                logError(e);
                path = new Path(""); //$NON-NLS-1$
            }

            try {
                Item tmpItem = item;

                // delete existing items before importing, this should be done
                // once for a different id
                String id = itemRecord.getProperty().getId();

                IRepositoryViewObject lastVersion = itemRecord.getExistingItemWithSameId();
                if (lastVersion != null
                        && overwrite
                        && !itemRecord.isLocked()
                        && (itemRecord.getState() == State.ID_EXISTED || itemRecord.getState() == State.NAME_EXISTED || itemRecord
                                .getState() == State.NAME_AND_ID_EXISTED) && !deletedItems.contains(id)) {
                    if (!overwriteDeletedItems.contains(id)) { // bug 10520.
                        ERepositoryStatus status = repFactory.getStatus(lastVersion);
                        if (status == ERepositoryStatus.DELETED) {
                            repFactory.restoreObject(lastVersion, path); // restore first.
                        }
                        overwriteDeletedItems.add(id);
                    }
                    /* only delete when name exsit rather than id exist */
                    if (itemRecord.getState().equals(ItemRecord.State.NAME_EXISTED)
                            || itemRecord.getState().equals(ItemRecord.State.NAME_AND_ID_EXISTED)) {
                        repFactory.forceDeleteObjectPhysical(lastVersion, itemRecord.getProperty().getVersion());
                    }
                    lastVersion = null;

                    // List<IRepositoryObject> list = cache.findObjectsByItem(itemRecord);
                    // if (!list.isEmpty()) {
                    // // this code will delete all version of item with same
                    // // id
                    // repFactory.forceDeleteObjectPhysical(list.get(0));
                    // deletedItems.add(id);
                    // }
                }

                User author = itemRecord.getProperty().getAuthor();
                if (author != null) {
                    if (!repFactory.setAuthorByLogin(tmpItem, author.getLogin())) {
                        tmpItem.getProperty().setAuthor(null); // author will be
                        // the logged
                        // user in
                        // create method
                    }
                }

                if (item instanceof JobletProcessItem) {
                    hasJoblets = true;
                }

                if (tmpItem instanceof ProcessItem && !statAndLogsSettingsReloaded && !implicitSettingsReloaded) {
                    ProcessItem processItem = (ProcessItem) tmpItem;
                    ParametersType paType = processItem.getProcess().getParameters();
                    boolean statsPSettingRemoved = false;

                    // for commanline import project setting
                    if (itemRecord.isRemoveProjectStatslog()) {
                        if (paType != null) {
                            String paramName = "STATANDLOG_USE_PROJECT_SETTINGS";
                            EList listParamType = paType.getElementParameter();
                            for (int j = 0; j < listParamType.size(); j++) {
                                ElementParameterType pType = (ElementParameterType) listParamType.get(j);
                                if (pType != null && paramName.equals(pType.getName())) {
                                    pType.setValue(Boolean.FALSE.toString());
                                    statsPSettingRemoved = true;
                                    break;
                                }
                            }
                        }
                    }

                    // 14446: item apply project setting param if use project setting
                    String statslogUsePSetting = null;
                    String implicitUsePSetting = null;
                    if (paType != null) {
                        EList listParamType = paType.getElementParameter();
                        for (int j = 0; j < listParamType.size(); j++) {
                            ElementParameterType pType = (ElementParameterType) listParamType.get(j);
                            if (pType != null) {
                                if (!statsPSettingRemoved && "STATANDLOG_USE_PROJECT_SETTINGS".equals(pType.getName())) {
                                    statslogUsePSetting = pType.getValue();
                                }
                                if ("IMPLICITCONTEXT_USE_PROJECT_SETTINGS".equals(pType.getName())) {
                                    implicitUsePSetting = pType.getValue();
                                }
                                if (statsPSettingRemoved && implicitUsePSetting != null || !statsPSettingRemoved
                                        && implicitUsePSetting != null && statslogUsePSetting != null) {
                                    break;
                                }
                            }
                        }
                    }
                    if (statslogUsePSetting != null && Boolean.parseBoolean(statslogUsePSetting) && !statAndLogsSettingsReloaded) {
                        CorePlugin.getDefault().getDesignerCoreService()
                                .reloadParamFromProjectSettings(paType, "STATANDLOG_USE_PROJECT_SETTINGS");
                        statAndLogsSettingsReloaded = true;
                    }
                    if (implicitUsePSetting != null && Boolean.parseBoolean(implicitUsePSetting) && !implicitSettingsReloaded) {
                        CorePlugin.getDefault().getDesignerCoreService()
                                .reloadParamFromProjectSettings(paType, "IMPLICITCONTEXT_USE_PROJECT_SETTINGS");
                        implicitSettingsReloaded = true;
                    }

                }
                boolean isCamel = false;
                if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                    ICamelDesignerCoreService service = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault()
                            .getService(ICamelDesignerCoreService.class);
                    if (service.isInstanceofCamel(tmpItem)) {
                        isCamel = true;
                    }
                }
                if (lastVersion == null || itemRecord.getState().equals(ItemRecord.State.ID_EXISTED)) {
                    // import has not been developed to cope with migration in mind
                    // so some model may not be able to load like the ConnectionItems
                    // in that case items needs to be copied before migration
                    // here we check that the loading of the item failed before calling the create method
                    boolean isConnectionEmptyBeforeMigration = tmpItem instanceof ConnectionItem
                            && ((ConnectionItem) tmpItem).getConnection().eResource() == null
                            && !itemRecord.getMigrationTasksToApply().isEmpty();

                    if (isCamel) {
                        repFactory.createCamel(tmpItem, path, true);
                    } else {
                        repFactory.create(tmpItem, path, true);
                    }
                    if (isConnectionEmptyBeforeMigration) {// copy the file before migration, this is bad because it
                        // should not refer to Filesytem
                        // but this is a quick hack and anyway the migration task only works on files
                        // IPath itemPath = itemRecord.getPath().removeFileExtension().addFileExtension(
                        // FileConstants.ITEM_EXTENSION);

                        InputStream is = manager.getStream(itemRecord.getPath().removeFileExtension()
                                .addFileExtension(FileConstants.ITEM_EXTENSION));
                        try {
                            URI propertyResourceURI = EcoreUtil.getURI(((ConnectionItem) tmpItem).getProperty());
                            URI relativePlateformDestUri = propertyResourceURI.trimFileExtension().appendFileExtension(
                                    FileConstants.ITEM_EXTENSION);
                            URL fileURL = FileLocator.toFileURL(new java.net.URL(
                                    "platform:/resource" + relativePlateformDestUri.toPlatformString(true))); //$NON-NLS-1$
                            OutputStream os = new FileOutputStream(fileURL.getFile());
                            try {
                                FileCopyUtils.copyStreams(is, os);
                            } finally {
                                os.close();
                            }
                        } finally {
                            is.close();
                        }
                    }

                    itemRecord.setImportPath(path.toPortableString());
                    itemRecord.setRepositoryType(itemType);
                    itemRecord.setItemId(itemRecord.getProperty().getId());
                    itemRecord.setItemVersion(itemRecord.getProperty().getVersion());
                    itemRecord.setImported(true);
                } else if (VersionUtils.compareTo(lastVersion.getProperty().getVersion(), tmpItem.getProperty().getVersion()) < 0) {
                    if (isCamel) {
                        repFactory.createCamel(tmpItem, path);
                    } else {
                        repFactory.forceCreate(tmpItem, path);
                    }
                    itemRecord.setImportPath(path.toPortableString());
                    itemRecord.setItemId(itemRecord.getProperty().getId());
                    itemRecord.setRepositoryType(itemType);
                    itemRecord.setItemVersion(itemRecord.getProperty().getVersion());
                    itemRecord.setImported(true);
                } else {
                    PersistenceException e = new PersistenceException(Messages.getString(
                            "ImportItemUtil.persistenceException", tmpItem.getProperty())); //$NON-NLS-1$
                    itemRecord.addError(e.getMessage());
                    logError(e);
                }

                if (tmpItem != null) {
                    RelationshipItemBuilder.getInstance().addOrUpdateItem(tmpItem);
                    if (tmpItem.getState() != null) {
                        if (itemType != null) {
                            final Set<String> folders = restoreFolder.getFolders(itemType);
                            if (folders != null) {
                                for (String folderPath : folders) {
                                    if (folderPath != null && folderPath.equals(path.toString())) {
                                        FolderItem folderItem = repFactory.getFolderItem(ProjectManager.getInstance()
                                                .getCurrentProject(), itemType, path);
                                        if (folderItem != null) {
                                            folderItem.getState().setDeleted(false);

                                            while (!(folderItem.getParent() instanceof Project)) {
                                                folderItem = (FolderItem) folderItem.getParent();
                                                if (folderItem.getType() == FolderType.SYSTEM_FOLDER_LITERAL) {
                                                    break;
                                                }
                                                folderItem.getState().setDeleted(false);
                                            }

                                        }
                                        break;
                                    }
                                }
                            }

                        }
                    }
                }

            } catch (Exception e) {
                itemRecord.addError(e.getMessage());
                logError(e);
            }

        }
        for (Resource resource : itemRecord.getResourceSet().getResources()) {
            // Due to the system of lazy loading for db repository of ByteArray,
            // it can't be unloaded just after create the item.
            if (!(resource instanceof ByteArrayResource)) {
                resource.unload();
            }
        }

        applyMigrationTasks(itemRecord, monitor);

    }

    private void applyMigrationTasks(ItemRecord itemRecord, IProgressMonitor monitor) {
        Context ctx = CorePlugin.getContext();
        RepositoryContext repositoryContext = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
        ITalendSynchronizer routineSynchronizer = getRoutineSynchronizer();

        ERepositoryObjectType repositoryType = itemRecord.getRepositoryType();

        Item item = null;
        try {
            List<IRepositoryViewObject> allVersion = ProxyRepositoryFactory.getInstance().getAllVersion(
                    ProjectManager.getInstance().getCurrentProject(), itemRecord.getItemId(), itemRecord.getImportPath(),
                    repositoryType);
            for (IRepositoryViewObject repositoryObject : allVersion) {
                if (repositoryObject.getProperty().getVersion().equals(itemRecord.getItemVersion())) {
                    item = repositoryObject.getProperty().getItem();
                }
            }
        } catch (Exception e) {
            logError(e);
        }

        if (item == null) {
            return;
        }

        List<IProjectMigrationTask> toExecute = new ArrayList<IProjectMigrationTask>();
        for (String taskId : itemRecord.getMigrationTasksToApply()) {
            IProjectMigrationTask task = GetTasksHelper.getInstance().getProjectTask(taskId);
            if (task == null) {
                log.warn(Messages.getString("ImportItemUtil.taskLogWarn", taskId)); //$NON-NLS-1$
            } else if (!task.isDeprecated()) {
                toExecute.add(task);
            }

        }
        Collections.sort(toExecute, new Comparator<IProjectMigrationTask>() {

            public int compare(IProjectMigrationTask o1, IProjectMigrationTask o2) {
                return o1.getOrder().compareTo(o2.getOrder());
            }
        });

        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

        for (IProjectMigrationTask task : toExecute) {
            monitor.subTask(Messages.getString("ImportItemUtil.taskMonitor", task.getName(), itemRecord.getItemName())); //$NON-NLS-1$
            try {
                // in case the resource has been modified (see MergeTosMetadataMigrationTask for example)
                if ((item.getProperty().eResource() == null || item.eResource() == null)) {
                    Property updatedProperty = factory.reload(item.getProperty());
                    item = updatedProperty.getItem();
                }

                if (item != null) {
                    ExecutionResult executionResult = task.execute(repositoryContext.getProject(), item);
                    if (executionResult == ExecutionResult.FAILURE) {
                        log.warn(Messages.getString("ImportItemUtil.itemLogWarn", itemRecord.getItemName(), task.getName())); //$NON-NLS-1$
                        // TODO smallet add a warning/error to the job using
                        // model
                    }
                }
            } catch (Exception e) {
                log.warn(Messages.getString("ImportItemUtil.itemLogException", itemRecord.getItemName(), task.getName()), e); //$NON-NLS-1$
                try {
                    factory.deleteObjectPhysical(new RepositoryObject(item.getProperty()));
                    break;// stop migrating the object it has be deleted
                } catch (PersistenceException e1) {
                    log.error("Could not delete physical item(" + item.getProperty().getLabel() + "), Project may be corrupted.",
                            e);
                }
            }
        }

        try {
            if (item != null && item instanceof RoutineItem) {
                RoutineUtils.changeRoutinesPackage(item);
                RoutineItem routineItem = (RoutineItem) item;
                routineSynchronizer.forceSyncRoutine(routineItem);
                routineSynchronizer.syncRoutine(routineItem, true);
                routineSynchronizer.getFile(routineItem);
            }
            // if (item.getProperty().eResource().isModified()) {
            // ProxyRepositoryFactory.getInstance().save(item, true);
            // item.getProperty().eResource().setModified(false);
            // }
            if (item.getProperty().eResource() != null) {
                ProxyRepositoryFactory.getInstance().unloadResources(item.getProperty());
                if (item.getParent() != null && item.getParent() instanceof FolderItem) {
                    ((FolderItem) item.getParent()).getChildren().remove(item);
                    item.setParent(null);
                }
            }

            itemRecord.setExistingItemWithSameId(null);
            itemRecord.clear();

        } catch (Exception e) {
            logError(e);
        }
    }

    private ITalendSynchronizer getRoutineSynchronizer() {

        ICodeGeneratorService service = (ICodeGeneratorService) GlobalServiceRegister.getDefault().getService(
                ICodeGeneratorService.class);

        ECodeLanguage lang = ((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                .getProject().getLanguage();
        ITalendSynchronizer routineSynchronizer = null;
        switch (lang) {
        case JAVA:
            routineSynchronizer = service.createJavaRoutineSynchronizer();
            break;
        case PERL:
            routineSynchronizer = service.createPerlRoutineSynchronizer();
            break;
        default:
            throw new UnsupportedOperationException(Messages.getString("ImportItemUtil.unknowException", lang)); //$NON-NLS-1$
        }

        return routineSynchronizer;

    }

    private void logError(Exception e) {
        hasErrors = true;
        // IStatus status;
        //        String messageStatus = e.getMessage() != null ? e.getMessage() : ""; //$NON-NLS-1$
        ExceptionHandler.process(e);

        // status = new Status(IStatus.ERROR, RepositoryLocalProviderPlugin.PLUGIN_ID, IStatus.OK, messageStatus, e);
        // RepositoryLocalProviderPlugin.getDefault().getLog().log(status);
    }

    public List<ProjectNode> getTreeViewInput() {
        return treeBuilder.getInput();
    }

    /**
     * need to returns sorted items by version to correctly import them later.
     */
    public List<ItemRecord> populateItems(ResourcesManager collector, boolean overwrite, IProgressMonitor progressMonitor) {
        treeBuilder.clear();
        cache.clear();
        projects.clear();
        routineExtModulesMap.clear();
        List<ItemRecord> items = new ArrayList<ItemRecord>();

        int nbItems = 0;

        for (IPath path : collector.getPaths()) {
            if (isPropertyPath(path)) {
                nbItems++;
            }
        }

        progressMonitor.beginTask("Populate items to import", nbItems); //$NON-NLS-1$

        for (IPath path : collector.getPaths()) {
            if (!progressMonitor.isCanceled()) {
                if (isPropertyPath(path)) {
                    // IPath itemPath = getItemPath(path);
                    // if (collector.getPaths().contains(itemPath)) { //commet by tdq import
                    ItemRecord itemRecord = computeItemRecord(collector, path);
                    if (itemRecord.getProperty() != null) {
                        items.add(itemRecord);

                        if (checkItem(itemRecord, overwrite)) {
                            InternalEObject author = (InternalEObject) itemRecord.getProperty().getAuthor();
                            URI uri = null;
                            if (author != null) {
                                uri = author.eProxyURI();
                            }

                            IPath projectFilePath = getValidProjectFilePath(collector, path, uri);
                            if (projectFilePath != null) {
                                Project project = computeProject(collector, itemRecord, projectFilePath);
                                if (checkProject(project, itemRecord)) {
                                    treeBuilder.addItem(project, itemRecord);

                                    // set item project into record.
                                    itemRecord.setItemProject(project);
                                    // we can try to import item
                                    // and we will try to resolve user
                                    if (uri != null) {
                                        User user = (User) project.eResource().getEObject(uri.fragment());
                                        itemRecord.getProperty().setAuthor(user);
                                    }
                                }
                            } else {
                                ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(itemRecord.getItem());
                                if (itemType.isDIItemType()) {
                                    itemRecord.addError(Messages.getString("RepositoryUtil.ProjectNotFound")); //$NON-NLS-1$
                                }
                            }
                        }
                    }
                    // }
                    progressMonitor.worked(1);
                }
            } else {
                break;
            }
        }

        Collections.sort(items, new Comparator<ItemRecord>() {

            public int compare(ItemRecord o1, ItemRecord o2) {
                return VersionUtils.compareTo(o1.getProperty().getVersion(), o2.getProperty().getVersion());
            }
        });

        for (List<IRepositoryViewObject> list : this.cache.getItemsFromRepository().values()) {
            list.clear();
        }
        this.cache.getItemsFromRepository().clear();

        return items;
    }

    private boolean checkProject(Project project, ItemRecord itemRecord) {
        boolean checkProject = false;

        // Context ctx = CorePlugin.getContext();
        // RepositoryContext repositoryContext = (RepositoryContext)
        // ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
        // Project currentProject =
        // repositoryContext.getProject().getEmfProject();
        Project currentProject = ProjectManager.getInstance().getCurrentProject().getEmfProject();

        if (project != null) {
            if (project.getLanguage().equals(currentProject.getLanguage())) {
                if (checkMigrationTasks(project, itemRecord, currentProject)) {
                    checkProject = true;
                }
            } else {
                itemRecord.addError(Messages.getString("RepositoryUtil.DifferentLanguage", project.getLanguage(), currentProject //$NON-NLS-1$
                        .getLanguage()));
            }
        } else {
            itemRecord.addError(Messages.getString("RepositoryUtil.ProjectNotFound")); //$NON-NLS-1$
        }

        return checkProject;
    }

    private List<String> getOptionnalMigrationTasks() {
        List<String> toReturn = new ArrayList<String>();

        toReturn.add("org.talend.repository.documentation.migrationtask.generatejobdocmigrationtask"); //$NON-NLS-1$
        // old task, added for an old version of TOS, not used anymore.
        toReturn.add("org.talend.repository.migration.ReplaceOldContextScriptCodeMigrationTask"); //$NON-NLS-1$
        toReturn.add("org.talend.designer.core.model.process.migration.SynchronizeSchemaOnlyForPerlDemo"); //$NON-NLS-1$
        toReturn.add("org.talend.repository.model.migration.RenametFSFilterRow"); //$NON-NLS-1$        
        return toReturn;
    }

    @SuppressWarnings("unchecked")
    private boolean checkMigrationTasks(Project project, ItemRecord itemRecord, Project currentProject) {
        List<String> itemMigrationTasks = new ArrayList(project.getMigrationTasks());
        List<String> projectMigrationTasks = new ArrayList(currentProject.getMigrationTasks());

        itemMigrationTasks.removeAll(getOptionnalMigrationTasks());

        // 1. Check if all the migration tasks of the items are done in the
        // project:
        // if not, the item use a more recent version of TOS: impossible to
        // import (forward compatibility)
        if (!projectMigrationTasks.containsAll(itemMigrationTasks)) {
            itemMigrationTasks.removeAll(projectMigrationTasks);

            String message = Messages.getString("ImportItemUtil.message", itemRecord.getItemName(), itemMigrationTasks); //$NON-NLS-1$
            itemRecord.addError(message);
            log.info(message);

            return false;
        }
        // force to redo this migration task, even if already did before.
        itemMigrationTasks.remove("org.talend.repository.model.migration.AutoUpdateRelationsMigrationTask");

        // 2. Get all the migration tasks to apply on this item on import
        // (backwards compatibility)
        // (those that are in the project but not in the item)
        projectMigrationTasks.removeAll(itemMigrationTasks);
        itemRecord.setMigrationTasksToApply(projectMigrationTasks);

        return true;
    }

    private IPath getValidProjectFilePath(ResourcesManager collector, IPath path, URI uri) {
        IPath projectFilePath = path.removeLastSegments(1);

        while (projectFilePath.lastSegment() != null
                && !collector.getPaths().contains(projectFilePath.append(FileConstants.LOCAL_PROJECT_FILENAME))) {
            projectFilePath = projectFilePath.removeLastSegments(1);
        }
        if (collector.getPaths().contains(projectFilePath.append(FileConstants.LOCAL_PROJECT_FILENAME))) {
            return projectFilePath.append(FileConstants.LOCAL_PROJECT_FILENAME);
        }
        return null;
    }

    private ItemRecord computeItemRecord(ResourcesManager collector, IPath path) {
        ItemRecord itemRecord = new ItemRecord(path);
        computeProperty(collector, itemRecord);
        return itemRecord;
    }

    private void computeProperty(ResourcesManager manager, ItemRecord itemRecord) {
        InputStream stream = null;
        try {
            stream = manager.getStream(itemRecord.getPath());
            Resource resource = createResource(itemRecord.getResourceSet(), itemRecord.getPath(), false);
            resource.load(stream, null);
            itemRecord.setProperty((Property) EcoreUtil.getObjectByType(resource.getContents(),
                    PropertiesPackage.eINSTANCE.getProperty()));
        } catch (IOException e) {
            // ignore
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public void resolveItem(ResourcesManager manager, ItemRecord itemRecord) {
        if (itemRecord.isResolved()) {
            return;
        }

        InputStream stream = null;

        try {
            final Item item = itemRecord.getItem();
            boolean byteArray = (item instanceof FileItem);
            IPath itemPath = getItemPath(itemRecord.getPath());

            stream = manager.getStream(itemPath);
            Resource resource = createResource(itemRecord.getResourceSet(), itemPath, byteArray);

            resource.load(stream, null);
            resetItemReference(itemRecord, resource);
            // EcoreUtil.resolveAll(itemRecord.getResourceSet());
        } catch (IOException e) {
            // ignore
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        itemRecord.setResolved(true);
    }

    /**
     * 
     * cLi Comment method "resetItemReference".
     * 
     * resolve the encode some special character(bug 6252), maybe, It's not better to resolve this by manually.
     * 
     * such as, "[" is "%5B", "]" is "%5D", etc.
     */
    @SuppressWarnings("unchecked")
    private void resetItemReference(ItemRecord itemRecord, Resource resource) {
        Item item = itemRecord.getItem();
        EList<EObject> contents = resource.getContents();
        /*
         * ignore job. no need, because it can't be allowed input special char for name.
         */
        if (item instanceof ProcessItem) {
            // ((ProcessItem) item).setProcess((ProcessType) EcoreUtil.getObjectByType(contents,
            // TalendFilePackage.eINSTANCE
            // .getProcessType()));
        } else
        /*
         * ignore joblet. no need, because it can't be allowed input special char for name.
         */
        if (item instanceof JobletProcessItem) {
            // JobletProcessItem jobletProcessItem = (JobletProcessItem) item;
            //
            // jobletProcessItem.setJobletProcess((JobletProcess) EcoreUtil.getObjectByType(contents,
            // JobletPackage.eINSTANCE
            // .getJobletProcess()));
            // jobletProcessItem
            // .setIcon((ByteArray) EcoreUtil.getObjectByType(contents, PropertiesPackage.eINSTANCE.getByteArray()));
        } else
        // connectionItem
        if (item instanceof ConnectionItem) {
            ((ConnectionItem) item).setConnection((Connection) EcoreUtil.getObjectByType(contents,
                    ConnectionPackage.eINSTANCE.getConnection()));
        } else
        // context
        if (item instanceof ContextItem) {
            EList contexts = ((ContextItem) item).getContext();
            contexts.clear();
            contexts.addAll(EcoreUtil.getObjectsByType(contents, TalendFilePackage.eINSTANCE.getContextType()));
        } else
        // file
        if (item instanceof FileItem) {
            /*
             * ignore routine, no need, because it can't be allowed input special char for name.
             */
            if (item instanceof RoutineItem) {
                return;
            }
            FileItem fileItem = (FileItem) item;
            fileItem.setContent((ByteArray) EcoreUtil.getObjectByType(contents, PropertiesPackage.eINSTANCE.getByteArray()));
        } else
        // snippet
        if (item instanceof SnippetItem) {
            EList variables = ((SnippetItem) item).getVariables();
            variables.clear();
            variables.addAll(EcoreUtil.getObjectsByType(contents, PropertiesPackage.eINSTANCE.getSnippetVariable()));
        } else
        // link doc
        if (item instanceof LinkDocumentationItem) {
            ((LinkDocumentationItem) item).setLink((LinkType) EcoreUtil.getObjectByType(contents,
                    PropertiesPackage.eINSTANCE.getLinkType()));
        } else
        // business
        if (item instanceof BusinessProcessItem) {
            BusinessProcessItem businessProcessItem = (BusinessProcessItem) item;

            businessProcessItem.setSemantic((BusinessProcess) EcoreUtil.getObjectByType(contents,
                    BusinessPackage.eINSTANCE.getBusinessProcess()));

            businessProcessItem.setNotationHolder((NotationHolder) EcoreUtil.getObjectByType(contents,
                    PropertiesPackage.eINSTANCE.getNotationHolder()));
        }

    }

    private Project computeProject(ResourcesManager manager, ItemRecord itemRecord, IPath path) {
        InputStream stream = null;

        try {
            if (!projects.containsKey(path)) {
                stream = manager.getStream(path);
                Resource resource = createResource(itemRecord.getResourceSet(), path, false);
                resource.load(stream, null);
                projects.put(path,
                        (Project) EcoreUtil.getObjectByType(resource.getContents(), PropertiesPackage.eINSTANCE.getProject()));
            }
            return projects.get(path);
        } catch (IOException e) {
            // ignore
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return null;
    }

    private Resource createResource(ResourceSet resourceSet, IPath path, boolean byteArrayResource) throws FileNotFoundException {
        Resource resource;
        if (byteArrayResource) {
            resource = new ByteArrayResource(getURI(path));
            resourceSet.getResources().add(resource);
        } else {
            resource = resourceSet.createResource(getURI(path));
        }
        return resource;
    }

    private URI getURI(IPath path) {
        return URI.createURI(path.lastSegment());
    }

    private boolean isPropertyPath(IPath path) {
        return xmiResourceManager.isPropertyFile(path.lastSegment());
    }

    private IPath getItemPath(IPath path) {
        return path.removeFileExtension().addFileExtension(FileConstants.ITEM_EXTENSION);
    }

    /**
     * 
     * DOC hcw ImportItemUtil class global comment. Detailled comment
     */
    static class RepositoryObjectCache {

        static ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

        private Set<ERepositoryObjectType> types = new HashSet<ERepositoryObjectType>();

        private Map<String, Boolean> lockState = new HashMap<String, Boolean>();

        // key is id of IRepositoryObject, value is a list of IRepositoryObject
        // with same id
        private Map<String, List<IRepositoryViewObject>> cache = new HashMap<String, List<IRepositoryViewObject>>();

        private Map<ERepositoryObjectType, List<IRepositoryViewObject>> itemsFromRepository = new HashMap<ERepositoryObjectType, List<IRepositoryViewObject>>();

        public List<IRepositoryViewObject> findObjectsByItem(ItemRecord itemRecord) throws PersistenceException {
            Item item = itemRecord.getItem();
            ERepositoryObjectType type = ERepositoryObjectType.getItemType(item);
            initialize(type);
            List<IRepositoryViewObject> result = cache.get(itemRecord.getProperty().getId());
            if (result == null) {
                result = Collections.EMPTY_LIST;
            }
            return result;
        }

        public void initialize(ERepositoryObjectType itemType) throws PersistenceException {
            if (!types.contains(itemType)) {
                types.add(itemType);
                // load object by type
                List<IRepositoryViewObject> list = factory.getAll(itemType, true, false);
                // change to RepositoryViewObject to save memory
                // (could be enhanced directly in repository for future versions)
                List<IRepositoryViewObject> newList = new ArrayList<IRepositoryViewObject>();
                for (IRepositoryViewObject obj : list) {
                    IRepositoryViewObject newObject = new RepositoryViewObject(obj.getProperty(), true);
                    // items with same id
                    List<IRepositoryViewObject> items = cache.get(newObject.getId());
                    if (items == null) {
                        items = new ArrayList<IRepositoryViewObject>();
                        cache.put(newObject.getId(), items);
                    }
                    items.add(newObject);
                    newList.add(newObject);
                }
                itemsFromRepository.put(itemType, newList);
            }
        }

        public void setItemLockState(ItemRecord itemRecord, boolean state) {
            lockState.put(itemRecord.getProperty().getId(), state);
        }

        public Boolean getItemLockState(ItemRecord itemRecord) {
            return lockState.get(itemRecord.getProperty().getId());
        }

        public void clear() {
            types.clear();
            cache.clear();
            lockState.clear();
            itemsFromRepository.clear();
        }

        public Map<ERepositoryObjectType, List<IRepositoryViewObject>> getItemsFromRepository() {
            return itemsFromRepository;
        }
    }

    @SuppressWarnings("unchecked")
    private void deployJarToDes(final ResourcesManager manager, Set<String> extRoutines) {
        File file = null;
        if (extRoutines.isEmpty()) {
            return;
        }

        for (Iterator iterator = manager.getPaths().iterator(); iterator.hasNext();) {
            String value = iterator.next().toString();
            file = new File(value);
            if (extRoutines.contains(file.getName())) {
                try {
                    CorePlugin.getDefault().getLibrariesService().deployLibrary(file.toURL());
                } catch (MalformedURLException e) {
                    ExceptionHandler.process(e);
                } catch (IOException e) {
                    ExceptionHandler.process(e);
                }
            }

        }

    }

    private void deployJarToDesForArchive(final ResourcesManager manager, Set<String> extRoutines) {
        if (extRoutines.isEmpty()) {
            return;
        }
        IPath tmpDir = new Path(System.getProperty("user.dir") + File.separatorChar + "tmpJar"); //$NON-NLS-1$  //$NON-NLS-1$

        File dirFile = tmpDir.toFile();
        for (Iterator<IPath> iterator = manager.getPaths().iterator(); iterator.hasNext();) {
            IPath path = iterator.next();
            String fileName = path.lastSegment();
            if (extRoutines.contains(fileName)) {
                try {
                    InputStream is = manager.getStream(path);
                    if (!dirFile.exists()) {
                        dirFile.mkdirs();
                    }
                    File temFile = tmpDir.append(fileName).toFile();
                    if (temFile.exists()) {
                        temFile.delete();
                    }
                    byte[] b = new byte[1024];
                    int length = 0;
                    BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(temFile, true));
                    while ((length = is.read(b)) != -1) {
                        fos.write(b, 0, length);
                    }
                    fos.close();
                    //
                    CorePlugin.getDefault().getLibrariesService().deployLibrary(temFile.toURI().toURL());

                    temFile.delete();

                } catch (MalformedURLException e) {
                    ExceptionHandler.process(e);
                } catch (IOException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        dirFile.delete();
    }

    public Map<String, Set<String>> getRoutineExtModulesMap() {
        return this.routineExtModulesMap;
    }

}
