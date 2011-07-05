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
package org.talend.repository.local;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.ExternalCrossReferencer;
import org.talend.commons.emf.EmfHelper;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.CorePlugin;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.MetadataManager;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.PropertiesPackage;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.User;
import org.talend.core.model.properties.helper.ByteArrayResource;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.component.impl.IMPORTTypeImpl;
import org.talend.repository.ProjectManager;
import org.talend.repository.constants.FileConstants;
import org.talend.repository.documentation.IFileExporterFullPath;
import org.talend.repository.documentation.TarFileExporterFullPath;
import org.talend.repository.documentation.ZipFileExporterFullPath;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.ERepositoryStatus;

/***/
public class ExportItemUtil {

    private static final String EXPORTUSER_TALEND_COM = "exportuser@talend.com"; //$NON-NLS-1$

    private Resource projectResource;

    private Resource propertyResource;

    private Resource itemResource;

    private File projectFile;

    private File propertyFile;

    private File itemFile;

    private IPath projectPath;

    private IPath propertyPath;

    private IPath itemPath;

    private Project project;

    private Map<String, User> login2user = new HashMap<String, User>();

    private ProjectManager pManager = ProjectManager.getInstance();

    public ExportItemUtil() {
        project = pManager.getCurrentProject().getEmfProject();
    }

    public ExportItemUtil(Project project) {
        this.project = project;
    }

    private IFileExporterFullPath exporter = null;

    /**
     * export the sected TOS model elements to the destination
     * 
     * @param destination zip file or folder to store the exported elements
     * @param items, the items to be exported
     * @param exportAllVersions whether all the versions are export or only the selected once
     * @param progressMonitor, to show the progress during export
     * @throws Exception in case of problem
     */
    public void exportItems(File destination, Collection<Item> items, boolean exportAllVersions, IProgressMonitor progressMonitor)
            throws Exception {
        // bug 11301 :export 0 items
        if (items == null || items.size() == 0) {
            return;
        }
        Collection<Item> otherVersions = new ArrayList<Item>();
        // get all versions of the exported items if wanted
        if (exportAllVersions) {
            otherVersions = getOtherVersions(items);
            items.addAll(otherVersions);
            otherVersions.clear();
        }// else keep current items version only
        try {

            File tmpDirectory = null;
            Map<File, IPath> toExport;

            if (destination.getName().endsWith(".tar")) { //$NON-NLS-1$
                createFolder(destination.getParentFile());
                exporter = new TarFileExporterFullPath(destination.getAbsolutePath(), false);
            } else if (destination.getName().endsWith(".tar.gz")) { //$NON-NLS-1$
                createFolder(destination.getParentFile());
                exporter = new TarFileExporterFullPath(destination.getAbsolutePath(), true);
            } else if (destination.getName().endsWith(".zip")) { //$NON-NLS-1$
                createFolder(destination.getParentFile());
                exporter = new ZipFileExporterFullPath(destination.getAbsolutePath(), true);
            } else {
                createFolder(destination);
            }

            if (exporter != null) {
                tmpDirectory = createTmpDirectory();
            }

            try {
                if (exporter != null) {
                    toExport = exportItems(items, tmpDirectory, true, progressMonitor);

                    // in case of .tar.gz we remove extension twice
                    // IPath rootPath = new Path(destination.getName()).removeFileExtension().removeFileExtension();
                    for (File file : toExport.keySet()) {
                        IPath path = toExport.get(file);
                        // exporter.write(file.getAbsolutePath(), rootPath.append(path).toString());
                        exporter.write(file.getAbsolutePath(), path.toString());
                    }
                } else {
                    toExport = exportItems(items, destination, true, progressMonitor);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                if (exporter != null) {
                    deleteTmpDirectory(tmpDirectory);
                }
            }
        } finally {
            if (exporter != null) {
                try {
                    exporter.finished();
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        }
    }

    /**
     * return a collection of items that have the same id as the input items params and a different version. <br>
     * WARNING : when calling this method the global TOS model will be updated with all the items versions
     * 
     * @param items all the items to get the different version of.
     * @return list of all the items with same id as input items and different versions
     * @throws PersistenceException if could not load some items //MOD sgandon 31/03/2010 bug 12229: changed
     * getAllVersions into getOtherVersions.
     */
    private Collection<Item> getOtherVersions(Collection<Item> items) throws PersistenceException {
        Collection<Item> itemsVersions = new ArrayList<Item>();
        for (Item item : items) {
            org.talend.core.model.general.Project itemProject = new org.talend.core.model.general.Project(
                    pManager.getProject(item));
            List<IRepositoryViewObject> allVersion = ProxyRepositoryFactory.getInstance().getAllVersion(itemProject,
                    item.getProperty().getId(), false);
            for (IRepositoryViewObject repositoryObject : allVersion) {
                Item anyVersionItem = repositoryObject.getProperty().getItem();
                if (!anyVersionItem.equals(item)) {
                    itemsVersions.add(anyVersionItem);
                }// else same item so ignor it
            }
        }
        return itemsVersions;
    }

    public Set<File> createLocalResources(File destinationDirectory, Item item) throws Exception {
        List<Item> items = new ArrayList<Item>();
        items.add(item);

        Map<File, IPath> exportItems = exportItems(items, destinationDirectory, false, new NullProgressMonitor());

        return exportItems.keySet();
    }

    /**
     * DOC chuang Comment method "sortItemsByProject".
     * 
     * @param items
     * @param itemProjectMap
     * @return
     */
    private Collection<Item> sortItemsByProject(Collection<Item> items, Map<Item, Project> itemProjectMap) {
        Map<Project, List<Item>> projectItems = new HashMap<Project, List<Item>>();
        for (Item item : items) {
            // get project corresponding to item
            Project p = pManager.getProject(item);
            // store for further lookup
            itemProjectMap.put(item, p);

            // items in the same list belongs to same project
            List<Item> list = projectItems.get(p);
            if (list == null) {
                list = new ArrayList<Item>();
                projectItems.put(p, list);
            }
            list.add(item);
        }
        // merge items from different projects
        items = new ArrayList<Item>(items.size());
        for (List<Item> list : projectItems.values()) {
            items.addAll(list);
        }
        return items;
    }

    private Map<File, IPath> exportItems(Collection<Item> items, File destinationDirectory, boolean projectFolderStructure,
            IProgressMonitor progressMonitor) throws Exception {
        Map<File, IPath> toExport = new HashMap<File, IPath>();

        progressMonitor.beginTask("Export Items", items.size() + 1); //$NON-NLS-1$
        ResourceSetImpl resourceSet = new ResourceSetImpl();

        try {
            // store item and its corresponding project
            Map<Item, Project> itemProjectMap = new HashMap<Item, Project>();

            Collection<Item> allItems = new ArrayList<Item>(items);

            items.clear();

            allItems = sortItemsByProject(allItems, itemProjectMap);

            itemProjectMap.clear();

            Iterator<Item> iterator = allItems.iterator();
            while (iterator.hasNext()) {
                Item item = iterator.next();
                project = pManager.getProject(item);

                computeProjectFileAndPath(destinationDirectory);
                if (!toExport.containsKey(projectFile)) {
                    createProjectResource(resourceSet, allItems);
                    toExport.put(projectFile, projectPath);

                }
                if (ERepositoryObjectType.getItemType(item).isResourceItem()) {
                    Collection<EObject> copiedObjects = copyObjects(item);

                    Item copiedItem = (Item) EcoreUtil.getObjectByType(copiedObjects, PropertiesPackage.eINSTANCE.getItem());
                    fixItem(copiedItem);
                    computeItemFilesAndPaths(destinationDirectory, copiedItem, projectFolderStructure);
                    createItemResources(copiedItem, copiedObjects, resourceSet);
                    fixItemUserReferences(copiedItem);
                    fixItemLockState();
                    toExport.put(propertyFile, propertyPath);
                    toExport.put(itemFile, itemPath);

                    // ProxyRepositoryFactory.getInstance().unloadResources(copiedItem.getProperty());
                    // copiedItem.setParent(null);
                }

                if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {
                    List jarNameList = new ArrayList();

                    if (item instanceof RoutineItem) {
                        List list = ((RoutineItem) item).getImports();
                        for (int i = 0; i < list.size(); i++) {
                            String jarName = ((IMPORTTypeImpl) list.get(i)).getMODULE();
                            jarNameList.add(jarName.toString());
                        }

                    }

                    String path = CorePlugin.getDefault().getLibrariesService().getJavaLibrariesPath();

                    for (int j = 0; j < jarNameList.size(); j++) {
                        String jarName = (String) jarNameList.get(j);
                        IPath jarPath = new Path(getNeedProjectPath()).append("lib").append(jarName);//$NON-NLS-1$ 
                        String filePath = new Path(destinationDirectory.toString()).append(jarPath.toString()).toString();
                        copyJarToDestination(new Path(path).append(jarName).toString(), filePath);
                        toExport.put(new File(filePath), jarPath);
                    }
                }
                progressMonitor.worked(1);

                dereferenceNotContainedObjects(resourceSet);
                saveResources(resourceSet);

                if (!ERepositoryStatus.LOCK_BY_USER.equals(ProxyRepositoryFactory.getInstance().getStatus(item))) {
                    ProxyRepositoryFactory.getInstance().unloadResources(item.getProperty());
                    if (item.getParent() != null && item.getParent() instanceof FolderItem) {
                        ((FolderItem) item.getParent()).getChildren().remove(item);
                        item.setParent(null);
                    }
                }
                iterator.remove();

                cleanResources(resourceSet);
            }

            progressMonitor.worked(1);

        } catch (Exception e) {
            throw e;
        } finally {
            cleanResources(resourceSet);
        }

        return toExport;
    }

    private File createTmpDirectory() throws IOException {
        File tmpDirectory = null;
        int suffix = 0;
        while (tmpDirectory == null || tmpDirectory.exists()) {
            tmpDirectory = new File(SystemUtils.getJavaIoTmpDir(), "talendExportItems" + suffix); //$NON-NLS-1$
            suffix++;
        }

        if (!tmpDirectory.mkdir()) {
            throw new IOException(Messages.getString("ExportItemUtil.cannotCreate", tmpDirectory)); //$NON-NLS-1$
        }

        return tmpDirectory;
    }

    private void deleteTmpDirectory(File tmpDirectory) {
        if (tmpDirectory.isFile()) {
            tmpDirectory.delete();
        } else {
            for (File file : tmpDirectory.listFiles()) {
                deleteTmpDirectory(file);
            }
            tmpDirectory.delete();
        }
    }

    private void computeProjectFileAndPath(File destinationFile) {
        projectPath = getProjectPath();
        projectPath = projectPath.append(FileConstants.LOCAL_PROJECT_FILENAME);
        projectFile = new File(destinationFile, projectPath.toOSString());
    }

    private IPath getProjectPath() {
        return new Path(project.getTechnicalLabel());
    }

    private void computeItemFilesAndPaths(File destinationFile, Item item, boolean projectFolderStructure) {
        IPath fileNamePath = getProjectPath();

        if (projectFolderStructure) {
            ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(item);
            IPath typeFolderPath = new Path(ERepositoryObjectType.getFolderName(itemType));
            if (item.getProperty().getItem().getState().getPath() == null) {
                item.getProperty().getItem().getState().setPath(""); //$NON-NLS-1$
            }
            IPath itemDestinationPath = typeFolderPath.append(item.getProperty().getItem().getState().getPath());
            fileNamePath = fileNamePath.append(itemDestinationPath);
        }
        String itemFileName = ResourceFilenameHelper.getExpectedFileName(item.getProperty().getLabel(), item.getProperty()
                .getVersion());

        fileNamePath = fileNamePath.append(itemFileName);
        propertyPath = fileNamePath.addFileExtension(FileConstants.PROPERTIES_EXTENSION);
        propertyFile = new File(destinationFile, propertyPath.toOSString());

        itemPath = fileNamePath.addFileExtension(FileConstants.ITEM_EXTENSION);

        itemFile = new File(destinationFile, itemPath.toOSString());

    }

    // private void init() {
    // resourceSet = new ResourceSetImpl();
    // }

    private void createProjectResource(ResourceSetImpl resourceSet, Collection<Item> items) {
        projectResource = createResource(projectFile, false, resourceSet);

        EObject projectCopy = EcoreUtil.copy(project);
        projectResource.getContents().add(projectCopy);

        Set<String> logins = new HashSet<String>();
        logins.add(EXPORTUSER_TALEND_COM);
        for (Item item : items) {
            User author = item.getProperty().getAuthor();
            if (author != null) {
                logins.add(author.getLogin());
            }
        }

        for (String login : logins) {
            User user = PropertiesFactory.eINSTANCE.createUser();
            user.setLogin(login);
            projectResource.getContents().add(user);
            login2user.put(login, user);
        }
    }

    private void createItemResources(Item item, Collection<EObject> copiedObjects, ResourceSetImpl resourceSet) {

        propertyResource = createResource(propertyFile, false, resourceSet);
        moveObjectsToResource(propertyResource, copiedObjects, PropertiesPackage.eINSTANCE.getProperty());
        moveObjectsToResource(propertyResource, copiedObjects, PropertiesPackage.eINSTANCE.getItemState());
        moveObjectsToResource(propertyResource, copiedObjects, PropertiesPackage.eINSTANCE.getItem());

        boolean isFileItem = PropertiesPackage.eINSTANCE.getFileItem().isSuperTypeOf(item.eClass());
        itemResource = createResource(itemFile, isFileItem, resourceSet);
        moveObjectsToResource(itemResource, copiedObjects, null);
    }

    private void fixItem(Item item) {
        item.getProperty().setLabel(item.getProperty().getLabel().replace(' ', '_'));
    }

    private Resource createResource(File file, boolean byteArrayResource, ResourceSet resourceSet) {
        URI uri = URI.createFileURI(file.getAbsolutePath());
        if (byteArrayResource) {
            Resource resource = new ByteArrayResource(uri);
            resourceSet.getResources().add(resource);
            return resource;
        } else {
            return resourceSet.createResource(uri);
        }
    }

    private void saveResources(ResourceSet resourceSet) throws IOException, PersistenceException {
        for (Resource resource : resourceSet.getResources()) {
            if (resource.getURI().isFile())
                EmfHelper.saveResource(resource);
        }
    }

    private void cleanResources(ResourceSet resourceSet) {
        for (Resource resource : resourceSet.getResources()) {
            resource.unload();
        }
        resourceSet.getResources().clear();
        resourceSet = new ResourceSetImpl();
    }

    @SuppressWarnings("unchecked")
    private Collection<EObject> copyObjects(Item item) {
        List<EObject> objects = new ArrayList<EObject>();

        objects.add(item);
        EList references = item.eClass().getEAllReferences();
        for (Iterator iter = references.iterator(); iter.hasNext();) {
            EReference reference = (EReference) iter.next();
            if (!reference.isTransient()) {
                if (reference.isMany()) {
                    EList referencedEList = (EList) item.eGet(reference);
                    for (Iterator iterator = referencedEList.iterator(); iterator.hasNext();) {
                        EObject referenceEObject = (EObject) iterator.next();
                        if (referenceEObject != null && !objects.contains(referenceEObject)) {
                            objects.add(referenceEObject);
                        }
                    }
                } else {
                    EObject referenceEObject = (EObject) item.eGet(reference);
                    if (referenceEObject != null && !objects.contains(referenceEObject)) {
                        objects.add(referenceEObject);
                    }
                }
            }
        }

        MetadataManager.addPackges(item, objects); // hywang 13221

        return EcoreUtil.copyAll(objects);
    }

    private void moveObjectsToResource(Resource resource, Collection<EObject> objects, EClass type) {
        Collection<EObject> objectsToTransfer;
        if (type != null) {
            objectsToTransfer = EcoreUtil.getObjectsByType(objects, type);
        } else {
            objectsToTransfer = objects;
        }
        resource.getContents().addAll(objectsToTransfer);
        objects.removeAll(objectsToTransfer);
    }

    private void fixItemUserReferences(Item item) {
        Item newItem = (Item) EcoreUtil.getObjectByType(propertyResource.getContents(), PropertiesPackage.eINSTANCE.getItem());
        User author = item.getProperty().getAuthor();
        String login = EXPORTUSER_TALEND_COM;
        if (author != null) {
            login = author.getLogin();
        }
        newItem.getProperty().setAuthor(login2user.get(login));
    }

    private void fixItemLockState() {
        Item item = (Item) EcoreUtil.getObjectByType(propertyResource.getContents(), PropertiesPackage.eINSTANCE.getItem());
        item.getState().setLocker(null);
        item.getState().setLockDate(null);
        item.getState().setLocked(false);
    }

    @SuppressWarnings("unchecked")
    private void dereferenceNotContainedObjects(ResourceSet resourceSet) {
        Map<EObject, Collection<Setting>> externalObjects = ExternalCrossReferencer.find(resourceSet);

        for (EObject object : externalObjects.keySet()) {
            Collection<Setting> collection = externalObjects.get(object);
            for (Setting setting : collection) {
                if (setting.getEStructuralFeature().isMany()) {
                    EList referencedEList = (EList) setting.getEObject().eGet(setting.getEStructuralFeature());
                    referencedEList.clear();
                } else {
                    setting.getEObject().eSet(setting.getEStructuralFeature(), null);
                }
            }
        }
    }

    private void createFolder(File folder) throws IOException {
        folder.mkdirs();
        if (!folder.exists()) {
            throw new IOException(Messages.getString("ExportItemUtil.cannotCreateDir", folder)); //$NON-NLS-1$
        }
    }

    private void copyJarToDestination(String sourcePath, String destinationPath) {
        // String path = CorePlugin.getDefault().getLibrariesService().getJavaLibrariesPath();
        File sourceFile = new File(sourcePath);
        File destinationFile = new File(destinationPath);
        if (sourceFile.exists()) {
            try {
                FilesUtils.copyFile(sourceFile, destinationFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public String getNeedProjectPath() {
        IPath needPath = getProjectPath();
        return needPath.toString();
    }

    public IFileExporterFullPath getExporter() {
        return exporter;
    }
}
