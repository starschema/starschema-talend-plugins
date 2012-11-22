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
package org.talend.core.repository.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.talend.commons.emf.EmfHelper;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.DelimitedFileConnectionItem;
import org.talend.core.model.properties.EbcdicConnectionItem;
import org.talend.core.model.properties.FileItem;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobDocumentationItem;
import org.talend.core.model.properties.JobletDocumentationItem;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.PositionalFileConnectionItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.PropertiesPackage;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.ReferenceFileItem;
import org.talend.core.model.properties.TDQItem;
import org.talend.core.model.properties.ValidationRulesConnectionItem;
import org.talend.core.model.properties.helper.ByteArrayResource;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.utils.ResourceFilenameHelper.FileName;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.ProjectManager;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 * 
 * $Id: XmiResourceManager.java 44692 2010-06-30 04:29:32Z nrousseau $
 * 
 */
public class XmiResourceManager {

    private static final String OLD_PROJECT_FILENAME = "talendProject"; //$NON-NLS-1$

    // PTODO mhelleboid should use a custom ResourceFactory
    // PTODO mhelleboid test duplicate resourcesUri in resourceSet !

    public ResourceSet resourceSet = new ResourceSetImpl();

    private boolean useOldProjectFile;

    private boolean avoidUnloadResource;

    public XmiResourceManager() {
        setUseOldProjectFile(false);
        resourceSet.getLoadOptions().put("OPTION_DEFER_IDREF_RESOLUTION", Boolean.TRUE);
        resourceSet.getLoadOptions().put("OPTION_USE_PARSER_POOL", Boolean.TRUE);
    }

    public void resetResourceSet() {
        resourceSet = new ResourceSetImpl();
        resourceSet.getLoadOptions().put("OPTION_DEFER_IDREF_RESOLUTION", Boolean.TRUE);
        resourceSet.getLoadOptions().put("OPTION_USE_PARSER_POOL", Boolean.TRUE);
    }

    public Project loadProject(IProject project) throws PersistenceException {
        URI uri = getProjectResourceUri(project);
        if (!isAvoidUnloadResource()) {
            unloadResource(uri.toString());
        }
        // unloadResources();
        Resource resource = resourceSet.getResource(uri, true);
        Project emfProject = (Project) EcoreUtil
                .getObjectByType(resource.getContents(), PropertiesPackage.eINSTANCE.getProject());
        return emfProject;
    }

    public boolean hasTalendProjectFile(IProject project) {
        URI uri = getProjectResourceUri(project);
        try {
            project.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
        IPath path = URIHelper.convert(uri);
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
        return file.exists();
    }

    public void loadScreenshots(Property property, ProcessType processType) {
        Resource screenshotResource = getScreenshotResource(property.getItem());
        processType.getScreenshots().addAll(screenshotResource.getContents());

    }

    public Property loadProperty(IResource iResource) {
        // force unload old version, or the UI won't be synchronized all the time to the current file.
        // this is only if a user update itself a .item or .properties, or for SVN repository.
        //
        URI propertyUri = URIHelper.convert(iResource.getFullPath());
        URI itemResourceURI = getItemResourceURI(propertyUri);
        List<Resource> resources = new ArrayList<Resource>(resourceSet.getResources());
        for (Resource res : resources) {
            if (res != null) {
                if (propertyUri.toString().equals(res.getURI().toString())) {
                    res.unload();
                    resourceSet.getResources().remove(res);
                }
                if (itemResourceURI.toString().equals(res.getURI().toString())) {
                    res.unload();
                    resourceSet.getResources().remove(res);
                }
            }
        }

        Resource propertyResource = resourceSet.getResource(propertyUri, true);

        Property property = (Property) EcoreUtil.getObjectByType(propertyResource.getContents(),
                PropertiesPackage.eINSTANCE.getProperty());
        return property;
    }

    public Property forceReloadProperty(Property property) {
        URI propertyURI = property.eResource().getURI();
        unloadResources(property);
        Resource propertyResource = resourceSet.getResource(propertyURI, true);
        return (Property) EcoreUtil.getObjectByType(propertyResource.getContents(), PropertiesPackage.eINSTANCE.getProperty());
    }

    /**
     * 
     * DOC klliu Comment method "getFolderPath".
     * 
     * @param project
     * @param repositoryObjectType
     * @param relativePath
     * @return
     * @throws PersistenceException
     */
    private IPath getFolderPath(IProject project, ERepositoryObjectType repositoryObjectType, IPath relativePath)
            throws PersistenceException {
        ERepositoryObjectType type = repositoryObjectType;
        if (ERepositoryObjectType.TDQ_SYSTEM_INDICATORS.equals(repositoryObjectType)
                || ERepositoryObjectType.TDQ_USERDEFINE_INDICATORS.equals(repositoryObjectType)) {
            type = ERepositoryObjectType.TDQ_INDICATOR_ELEMENT;
        } else if (ERepositoryObjectType.TDQ_PATTERN_REGEX.equals(repositoryObjectType)
                || ERepositoryObjectType.TDQ_PATTERN_SQL.equals(repositoryObjectType)) {
            type = ERepositoryObjectType.TDQ_PATTERN_ELEMENT;
        }
        IFolder folder = project.getFolder(ERepositoryObjectType.getFolderName(type)).getFolder(relativePath);
        return folder.getFullPath();
    }

    public Resource createProjectResource(IProject project) {
        URI uri = getProjectResourceUri(project);
        return resourceSet.createResource(uri);
    }

    public Resource createTempProjectResource() {
        URI uri = null;
        try {
            uri = URI.createPlatformResourceURI(Platform.getInstallLocation().getURL().toURI().toString());
        } catch (URISyntaxException e) {
            ExceptionHandler.process(e);
        }
        return resourceSet.createResource(uri);
    }

    private URI getProjectResourceUri(IProject project) {
        URI uri = URIHelper.convert(project.getFullPath().append(getProjectFilename()));
        return uri;
    }

    public Resource createPropertyResource(Resource itemResource) {
        URI propertyResourceURI = getPropertyResourceURI(itemResource.getURI());
        Resource propResource = resourceSet.getResource(propertyResourceURI, false);
        if (propResource != null) {
            propResource.unload();
            resourceSet.getResources().remove(propResource);
        }
        return resourceSet.createResource(propertyResourceURI);
    }

    public Resource getReferenceFileResource(Resource itemResource, String extension, boolean needLoad) {
        URI referenceFileURI = getReferenceFileURI(itemResource.getURI(), extension);
        URIConverter converter = resourceSet.getURIConverter();
        Resource referenceResource = new ByteArrayResource(referenceFileURI);
        InputStream inputStream = null;

        List<Resource> resources = new ArrayList<Resource>(resourceSet.getResources());
        for (Resource res : resources) {
            if (res != null && referenceFileURI.toString().equals(res.getURI().toString())) {
                res.unload();
                resourceSet.getResources().remove(res);
            }
        }

        resourceSet.getResources().add(referenceResource);
        try {
            if (needLoad) {
                inputStream = converter.createInputStream(referenceFileURI);
                referenceResource.load(inputStream, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                ExceptionHandler.process(e);
            }
        }
        return referenceResource;
    }

    private URI getReferenceFileURI(URI itemResourceURI, String extension) {
        return itemResourceURI.trimFileExtension().appendFileExtension(extension);
    }

    public Resource createItemResource(IProject project, Item item, IPath path, ERepositoryObjectType repositoryObjectType,
            boolean byteArrayResource) throws PersistenceException {
        URI itemResourceURI = getItemResourceURI(project, repositoryObjectType, path, item);

        Resource itemResource = createItemResource(byteArrayResource, itemResourceURI);

        return itemResource;
    }

    public Resource createScreenshotResource(IProject project, Item item, IPath path, ERepositoryObjectType repositoryObjectType,
            boolean byteArrayResource) throws PersistenceException {
        URI itemResourceURI = getScreenshotResourceURI(project, repositoryObjectType, path, item);

        Resource itemResource = createItemResource(byteArrayResource, itemResourceURI);

        return itemResource;
    }

    // MOD mzhao 2010-11-22, suppport TDQ item file extensions.(.ana, .rep, etc)
    /**
     * Please use item.setFileExtension directly. When create the extension, it will add automatically the extension
     * point needed.
     */
    @Deprecated
    public Resource createItemResourceWithExtension(IProject project, Item item, IPath path,
            ERepositoryObjectType repositoryObjectType, boolean byteArrayResource, String fileExtension)
            throws PersistenceException {
        URI itemResourceURI = getItemResourceURI(project, repositoryObjectType, path, item, fileExtension);

        Resource itemResource = createItemResource(byteArrayResource, itemResourceURI);

        return itemResource;
    }

    private Resource createItemResource(boolean byteArrayResource, URI itemResourceURI) {
        Resource itemResource;
        itemResource = resourceSet.getResource(itemResourceURI, false);
        if (itemResource != null) {
            itemResource.unload();
            resourceSet.getResources().remove(itemResource);
        }
        if (byteArrayResource) {
            itemResource = new ByteArrayResource(itemResourceURI);
            resourceSet.getResources().add(itemResource);
        } else {
            itemResource = resourceSet.createResource(itemResourceURI);
        }
        return itemResource;
    }

    public void deleteResource(Resource resource) throws PersistenceException {
        ResourceUtils.deleteFile(URIHelper.getFile(resource.getURI()));
        resourceSet.getResources().remove(resource);
    }

    public void deleteLogiclResource(Resource resource) throws PersistenceException {
        ResourceUtils.deleteRevisionFile(URIHelper.getFile(resource.getURI()));
        resourceSet.getResources().remove(resource);
    }

    public Resource getItemResource(Item item) {
        URI itemResourceURI = null;
        if (item.getFileExtension() != null) {
            itemResourceURI = getItemResourceURI(getItemURI(item), item.getFileExtension());
        } else if (item instanceof TDQItem) {
            IPath fileName = new Path(((TDQItem) item).getFilename());
            itemResourceURI = getItemResourceURI(getItemURI(item), fileName.getFileExtension());
        } else {
            itemResourceURI = getItemResourceURI(getItemURI(item));
        }
        Resource itemResource = resourceSet.getResource(itemResourceURI, false);

        if (itemResource == null) {
            if (item instanceof FileItem) {
                itemResource = new ByteArrayResource(itemResourceURI);
                resourceSet.getResources().add(itemResource);
            }
            itemResource = resourceSet.getResource(itemResourceURI, true);
        }

        return itemResource;
    }

    /*
     * Get a resource obj from Item resource file. if the resouce file does not exist ,will create it first.
     */
    public Resource getScreenshotResource(Item item) {
        URI itemResourceURI = null;
        itemResourceURI = getScreenshotResourceURI(getItemURI(item));
        Resource itemResource;
        try {
            // judge whether the physical file exists or not
            itemResource = resourceSet.getResource(itemResourceURI, true);
        } catch (Exception e) {
            itemResource = resourceSet.createResource(itemResourceURI);
        }
        return itemResource;
    }

    private URI getItemURI(Item item) {
        ProjectManager pManager = ProjectManager.getInstance();
        org.talend.core.model.general.Project project = new org.talend.core.model.general.Project(pManager.getProject(item));
        // referenced item
        if (project != null && !project.equals(pManager.getCurrentProject())) {
            String folder = null;
            if (item instanceof JobDocumentationItem) {
                folder = ERepositoryObjectType.getFolderName(ERepositoryObjectType.JOB_DOC);
            } else if (item instanceof JobletDocumentationItem) {
                folder = ERepositoryObjectType.getFolderName(ERepositoryObjectType.JOBLET_DOC);
            } else if (item instanceof DatabaseConnectionItem) {
                folder = ERepositoryObjectType.getFolderName(ERepositoryObjectType.METADATA_CONNECTIONS);
            } else if (item instanceof DelimitedFileConnectionItem) {
                folder = ERepositoryObjectType.getFolderName(ERepositoryObjectType.METADATA_FILE_DELIMITED);
            } else if (item instanceof EbcdicConnectionItem) {
                folder = ERepositoryObjectType.getFolderName(ERepositoryObjectType.METADATA_FILE_EBCDIC);
            } else if (item instanceof PositionalFileConnectionItem) {
                folder = ERepositoryObjectType.getFolderName(ERepositoryObjectType.METADATA_FILE_POSITIONAL);
            } else if (item instanceof ValidationRulesConnectionItem) {
                folder = ERepositoryObjectType.getFolderName(ERepositoryObjectType.METADATA_VALIDATION_RULES);
            }

            if (folder != null) {
                IPath path = new Path(project.getTechnicalLabel());
                path = path.append(folder);
                path = path.append(item.getState().getPath());
                Property property = item.getProperty();
                String itemStr = property.getLabel() + "_" + property.getVersion() + "." + FileConstants.PROPERTIES_EXTENSION; //$NON-NLS-1$ //$NON-NLS-2$
                path = path.append(itemStr);
                return URIHelper.convert(path);
            }
        }
        return item.getProperty().eResource().getURI();
    }

    // public List<Resource> getAffectedResources(Property property, boolean needScreenshot) {
    // List<Resource> resources = getAffectedResources(property);
    // if (needScreenshot) {
    // Resource screenshotResource = getScreenshotResource(property.getItem());
    // if (screenshotResource != null) {
    // resources.add(screenshotResource);
    // }
    // }
    // return resources;
    // }

    public List<Resource> getAffectedResources(Property property) {
        List<Resource> resources = new ArrayList<Resource>();
        Iterator<EObject> i = property.getItem().eCrossReferences().iterator();
        while (i.hasNext()) {
            EObject object = i.next();
            Resource currentResource = object.eResource();
            if (currentResource == null) {
                // only for invalid items !!
                continue;
            }
            if (!resources.contains(currentResource)) {

                // ignore the business model linking *.business_diagram file...(for update version of item...)
                if (object instanceof org.eclipse.gmf.runtime.notation.impl.DiagramImpl) {
                    continue;
                }

                if (!currentResource.getURI().lastSegment().equals(getProjectFilename())) {
                    resources.add(currentResource);
                }
                if (!resourceSet.getResources().contains(currentResource)) {
                    resourceSet.getResources().add(currentResource);
                }
            }
            if (object instanceof ReferenceFileItem) {
                ReferenceFileItem fi = (ReferenceFileItem) object;
                ByteArray ba = fi.getContent();
                if (ba != null) {
                    Resource fiResource = ba.eResource();
                    if (fiResource != null) {
                        resources.add(fiResource);
                    }
                }
            }
        }
        i = property.getItem().eAllContents();
        while (i.hasNext()) {
            EObject object = i.next();
            Iterator<EObject> j = object.eCrossReferences().iterator();
            while (j.hasNext()) {
                EObject childEObject = j.next();
                Resource currentResource = childEObject.eResource();
                if (currentResource == null) {
                    // only for invalid items !!
                    continue;
                }
                if (!resources.contains(currentResource)) {

                    // ignore the business model linking *.business_diagram file...(for update version of item...)
                    if (object instanceof org.eclipse.gmf.runtime.notation.impl.DiagramImpl) {
                        continue;
                    }

                    if (!currentResource.getURI().lastSegment().equals(getProjectFilename())) {
                        resources.add(currentResource);
                    }
                }
                if (!resourceSet.getResources().contains(currentResource)) {
                    resourceSet.getResources().add(currentResource);
                }
            }
        }
        if (property.getItem() instanceof ProcessItem || property.getItem() instanceof JobletProcessItem) {
            if (property.eResource() != null) {
                Resource screenshotResource = getScreenshotResource(property.getItem());
                if (screenshotResource != null) {
                    resources.add(screenshotResource);
                }
            }
        }

        return resources;
    }

    public void moveResource(Resource resource, IPath path) throws PersistenceException {
        ResourceUtils.moveResource(URIHelper.getFile(resource.getURI()), path);
        resource.setURI(URIHelper.convert(path));
    }

    public void saveResource(Resource resource) throws PersistenceException {
        EmfHelper.saveResource(resource);
    }

    // MOD mzhao 2010-11-22, suppport TDQ item file extensions.(.ana, .rep, etc)
    public URI getItemResourceURI(URI propertyResourceURI, String... fileExtension) {
        if (fileExtension == null || fileExtension.length == 0) {
            return propertyResourceURI.trimFileExtension().appendFileExtension(FileConstants.ITEM_EXTENSION);
        } else {
            return propertyResourceURI.trimFileExtension().appendFileExtension(fileExtension[0]);
        }
    }

    private URI getPropertyResourceURI(URI itemResourceURI) {
        return itemResourceURI.trimFileExtension().appendFileExtension(FileConstants.PROPERTIES_EXTENSION);
    }

    private URI getScreenshotResourceURI(URI itemResourceURI) {
        // return itemResourceURI.trimFileExtension().appendFileExtension(FileConstants.PROPERTIES_EXTENSION);
        return itemResourceURI.trimFileExtension().appendFileExtension(FileConstants.SCREENSHOT_EXTENSION);
    }

    // MOD mzhao 2010-11-22, suppport TDQ item file extensions.(.ana, .rep, etc)
    private URI getItemResourceURI(IProject project, ERepositoryObjectType repositoryObjectType, IPath path, Item item)
            throws PersistenceException {
        IPath folderPath = getFolderPath(project, repositoryObjectType, path);
        FileName fileName = ResourceFilenameHelper.create(item.getProperty());
        IPath resourcePath = null;
        if (item.getFileExtension() == null) {
            resourcePath = ResourceFilenameHelper.getExpectedFilePath(fileName, folderPath, FileConstants.ITEM_EXTENSION);
        } else {
            resourcePath = ResourceFilenameHelper.getExpectedFilePath(fileName, folderPath, item.getFileExtension());
        }
        return URIHelper.convert(resourcePath);
    }

    @Deprecated
    private URI getItemResourceURI(IProject project, ERepositoryObjectType repositoryObjectType, IPath path, Item item,
            String fileExtension) throws PersistenceException {
        IPath folderPath = getFolderPath(project, repositoryObjectType, path);
        FileName fileName = ResourceFilenameHelper.create(item.getProperty());
        IPath resourcePath = ResourceFilenameHelper.getExpectedFilePath(fileName, folderPath, fileExtension);
        return URIHelper.convert(resourcePath);
    }

    // added by dlin 2011-7-14 to create the uri of file of .screenshot
    private URI getScreenshotResourceURI(IProject project, ERepositoryObjectType repositoryObjectType, IPath path, Item item,
            String... fileExtension) throws PersistenceException {
        IPath folderPath = getFolderPath(project, repositoryObjectType, path);
        FileName fileName = ResourceFilenameHelper.create(item.getProperty());
        IPath resourcePath = ResourceFilenameHelper.getExpectedFilePath(fileName, folderPath, FileConstants.SCREENSHOT_EXTENSION);
        if (fileExtension != null && fileExtension.length > 0) {
            resourcePath = ResourceFilenameHelper.getExpectedFilePath(fileName, folderPath, fileExtension[0]);
        }
        return URIHelper.convert(resourcePath);
    }

    public boolean isPropertyFile(IFile file) {
        return FileConstants.PROPERTIES_EXTENSION.equals(file.getFileExtension());
    }

    public boolean isPropertyFile(String filename) {
        return filename.endsWith(FileConstants.PROPERTIES_EXTENSION);
    }

    public boolean isProjectFile(IFile file) {
        return getProjectFilename().equals(file.getName());
    }

    public void propagateFileName(Property lastVersionProperty, Property resourceProperty) throws PersistenceException {
        // test first from the resource of the property
        // this test will avoid to load all other resources linked, since by default it will load all resources, big or
        // not.
        // this should save quite lots of memory when call this function.
        ResourceFilenameHelper.FileName fileNameTest = ResourceFilenameHelper.create(resourceProperty.eResource(),
                resourceProperty, lastVersionProperty);

        if (!ResourceFilenameHelper.mustChangeVersion(fileNameTest) && !ResourceFilenameHelper.mustChangeLabel(fileNameTest)) {
            return;
        }

        // now we now we need all the resources to change the file, we can load them, and execute all code of this
        // function.
        // note: at the end of the function, it will try to unload everything but not the .properties

        List<Resource> affectedResources = getAffectedResources(resourceProperty);
        List<Resource> resourcesToSave = new ArrayList<Resource>();
        Property previousVersionProperty = null;
        IFile propertyFile = null;

        for (Resource resource : affectedResources) {
            ResourceFilenameHelper.FileName fileName = ResourceFilenameHelper.create(resource, resourceProperty,
                    lastVersionProperty);

            if (ResourceFilenameHelper.mustChangeVersion(fileName)) {
                IPath path = URIHelper.convert(resource.getURI());
                IPath bakPath = path.addFileExtension("bak"); //$NON-NLS-1$

                // Create copy
                copyResource(resource, bakPath);
                IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(bakPath);

                // move actual to new version
                moveResource(resource, ResourceFilenameHelper.getExpectedFilePath(fileName, false));
                resourcesToSave.add(resource);

                // restore copy as previous version
                ResourceUtils.moveResource(file, path);
                file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);

                if (isPropertyFile(file)) {
                    propertyFile = file;
                }
            } else if (ResourceFilenameHelper.mustChangeLabel(fileName)) {
                resourceProperty.setLabel(lastVersionProperty.getLabel());
                if (!ResourceFilenameHelper.hasSameNameButDifferentCase(fileName)) {
                    moveResource(resource, ResourceFilenameHelper.getExpectedFilePath(fileName, false));
                }
                resourcesToSave.add(resource);
            }
        }

        if (propertyFile != null) {
            previousVersionProperty = loadProperty(propertyFile);
        }

        if (previousVersionProperty != null) {
            List<Resource> previousVersionResources = getAffectedResources(previousVersionProperty);
            for (Resource resource : previousVersionResources) {
                FileName fileName = ResourceFilenameHelper.create(resource, previousVersionProperty, lastVersionProperty);

                if (ResourceFilenameHelper.mustChangeLabel(fileName)) {
                    IPath expectedFilePath = ResourceFilenameHelper.getExpectedFilePath(fileName, true);
                    previousVersionProperty.setLabel(lastVersionProperty.getLabel());
                    if (!ResourceFilenameHelper.hasSameNameButDifferentCase(fileName)) {
                        moveResource(resource, expectedFilePath);
                    }
                }
                resourcesToSave.add(resource);
            }
            if (lastVersionProperty.getItem().getParent() instanceof FolderItem) {
                FolderItem folderItem = (FolderItem) lastVersionProperty.getItem().getParent();
                folderItem.getChildren().add(previousVersionProperty.getItem());
            }
        }

        for (Resource resource : resourcesToSave) {
            // add for bug TDI-20844
            if (ResourceFilenameHelper.mustChangeLabel(fileNameTest) && resource.getURI() != null
                    && resource.getURI().toString().endsWith(".screenshot")) {
                continue;
            }
            saveResource(resource);
        }
        if (!resourceProperty.equals(lastVersionProperty)) {
            // this version was only used to rename the file.
            // we can directly unload the resource to free the memory.
            Item item = resourceProperty.getItem();
            if (item.getParent() != null && item.getParent() instanceof FolderItem) {
                ((FolderItem) item.getParent()).getChildren().remove(item);
                item.setParent(null);
            }
        }
    }

    private void copyResource(Resource resource, IPath path) throws PersistenceException {
        IFile file = URIHelper.getFile(resource.getURI());
        try {
            file.copy(path, true, null);
        } catch (CoreException e) {
            throw new PersistenceException(e);
        }
    }

    public String getProjectFilename() {
        if (useOldProjectFile) {
            return OLD_PROJECT_FILENAME;
        } else {
            return FileConstants.LOCAL_PROJECT_FILENAME;
        }
    }

    public void setUseOldProjectFile(boolean useOldProjectFile) {
        this.useOldProjectFile = useOldProjectFile;
    }

    public void unloadResources() {
        List<Resource> resources = new ArrayList<Resource>(resourceSet.getResources());
        for (Resource resource : resources) {
            if (resource != null) {
                resource.unload();
                resourceSet.getResources().remove(resource);
            }
        }
    }

    public void unloadResources(Property property) {
        for (Resource resource : getAffectedResources(property)) {
            if (resource != null) {
                resource.unload();
                resourceSet.getResources().remove(resource);
            }
        }
    }

    /**
     * Method "unloadResource" unload and remove the specification resource from the resource set. MOD mzhao
     * 
     * @param uriString the uri sting of resource.
     */
    public synchronized void unloadResource(String uriString) {
        List<Resource> resources = new ArrayList<Resource>(resourceSet.getResources());
        for (Resource res : resources) {
            if (res != null && uriString.equals(res.getURI().toString())) {
                res.unload();
                resourceSet.getResources().remove(res);
            }
        }
    }

    public boolean isAvoidUnloadResource() {
        return this.avoidUnloadResource;
    }

    public void setAvoidUnloadResource(boolean avoidUnloadResource) {
        this.avoidUnloadResource = avoidUnloadResource;
    }
}
