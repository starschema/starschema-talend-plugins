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
package org.talend.repository.ui.actions.importproject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipFile;

import org.apache.commons.lang.ArrayUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.wizards.datatransfer.ArchiveFileManipulations;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.eclipse.ui.internal.wizards.datatransfer.TarFile;
import org.eclipse.ui.internal.wizards.datatransfer.TarLeveledStructureProvider;
import org.eclipse.ui.internal.wizards.datatransfer.ZipLeveledStructureProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.osgi.framework.Bundle;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.commons.utils.platform.PluginChecker;
import org.talend.commons.xml.XmlUtil;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.metadata.builder.database.PluginConstant;
import org.talend.core.model.properties.Project;
import org.talend.core.repository.utils.XmiResourceManager;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.utils.AfterImportProjectUtil;
import org.talend.resources.ResourcesPlugin;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * TODO SML Order methods
 * 
 * $Id$
 * 
 */
public class ImportProjectsUtilities {

    public static final String TALEND_PROJECT_FILE_NAME = "talend.project"; //$NON-NLS-1$

    public static final String OLD_TALEND_PROJECT_FILE_NAME = "talendProject"; //$NON-NLS-1$

    private static final String XML_FILE_PATH = "resources/demoprojects/"; //$NON-NLS-1$

    public static void importProjectAs(Shell shell, String newName, String technicalName, String sourcePath,
            IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        IImportStructureProvider provider = FilterFileSystemStructureProvider.INSTANCE;

        importProject(shell, provider, new File(sourcePath), new Path(technicalName), true, false, monitor);

        Project project=afterImportAs(newName, technicalName);
        
        //do additional actions after importing projects
        AfterImportProjectUtil.runAfterImportProjectActions(new org.talend.core.model.general.Project(project));
    }

    /**
     * DOC smallet Comment method "afterImportAs".
     * 
     * @param newName
     * @param technicalName
     * @throws InvocationTargetException
     */
    private static Project afterImportAs(String newName, String technicalName) throws InvocationTargetException {
        // Rename in ".project" and "talendProject" or "talend.project"
        // TODO SML Optimize
        final IWorkspace workspace = org.eclipse.core.resources.ResourcesPlugin.getWorkspace();
        IContainer containers = (IProject) workspace.getRoot().findMember(new Path(technicalName));
        IResource file2 = containers.findMember(IProjectDescription.DESCRIPTION_FILE_NAME);
        try {
            FilesUtils.replaceInFile("<name>.*</name>", file2.getLocation().toOSString(), "<name>" + technicalName + "</name>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            // TDI-19269
            final IProject project = workspace.getRoot().getProject(technicalName);
            XmiResourceManager xmiManager = new XmiResourceManager();
            try {
                final Project loadProject = xmiManager.loadProject(project);
                loadProject.setTechnicalLabel(technicalName);
                loadProject.setLabel(newName);
                loadProject.setLocal(true);
                loadProject.setId(0);
                loadProject.setUrl(null);
                loadProject.setCreationDate(null);
                loadProject.setDescription("");
                loadProject.setType(null);
                // ADD xqliu 2012-03-12 TDQ-4771 clear the list of Folders
                if (loadProject.getFolders() != null) {
                    loadProject.getFolders().clear();
                }
                // ~ TDQ-4771
                xmiManager.saveResource(loadProject.eResource());
                return loadProject;
            } catch (PersistenceException e) {
                //
            }
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        }
        return null;
    }

    private static void replaceInFile(String regex, String fileName, String replacement) throws IOException {
        InputStream in = new FileInputStream(fileName);
        StringBuffer buffer = new StringBuffer();
        try {
            InputStreamReader inR = new InputStreamReader(in);
            BufferedReader buf = new BufferedReader(inR);
            String line;
            while ((line = buf.readLine()) != null) {
                if (line.contains("<TalendProperties:Project")) { //$NON-NLS-1$
                    line = line.replaceAll(regex, replacement);
                }
                buffer.append(line).append("\n"); //$NON-NLS-1$
            }
        } finally {
            in.close();
        }

        OutputStream os = new FileOutputStream(fileName);
        os.write(buffer.toString().getBytes());
        os.close();
    }

    public static void importArchiveProjectAs(Shell shell, String newName, String technicalName, String sourcePath,
            IProgressMonitor monitor) throws InvocationTargetException, InterruptedException, TarException, IOException {
        importArchiveProject(shell, technicalName, sourcePath, monitor);

        Project project=afterImportAs(newName, technicalName);
        
        //do additional actions after importing projects
        AfterImportProjectUtil.runAfterImportProjectActions(new org.talend.core.model.general.Project(project));
    }

    public static void importArchiveProject(Shell shell, String technicalName, String sourcePath, IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException, TarException, IOException {

        IImportStructureProvider provider;
        Object source;

        if (ArchiveFileManipulations.isZipFile(sourcePath)) {
            ZipLeveledStructureProvider zipProvider = new ZipLeveledStructureProvider(new ZipFile(sourcePath));
            source = zipProvider.getRoot();
            boolean ok = true;
            for (Object o : zipProvider.getChildren(source)) {
                String label = zipProvider.getLabel(o);
                if (!label.equals(IProjectDescription.DESCRIPTION_FILE_NAME) && ok) {
                    source = o;
                } else {
                    ok = false;
                }
            }
            if (!ok) {
                source = zipProvider.getRoot();
            }

            provider = zipProvider;
        } else if (ArchiveFileManipulations.isTarFile(sourcePath)) {
            TarLeveledStructureProvider tarProvider = new TarLeveledStructureProvider(new TarFile(sourcePath));
            source = tarProvider.getRoot();
            boolean ok = true;
            for (Object o : tarProvider.getChildren(source)) {
                String label = tarProvider.getLabel(o);
                if (!label.equals(IProjectDescription.DESCRIPTION_FILE_NAME) && ok) {
                    source = o;
                } else {
                    ok = false;
                }
            }
            if (!ok) {
                source = tarProvider.getRoot();
            }

            provider = tarProvider;
        } else {
            throw new IllegalArgumentException(Messages.getString("ImportProjectsUtilities.fileFormatError", sourcePath)); //$NON-NLS-1$
        }

        importProject(shell, provider, source, new Path(technicalName), false, false, monitor);
    }

    private static void importProject(Shell shell, IImportStructureProvider provider, Object source, IPath path,
            boolean overwriteResources, boolean createContainerStructure, IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException {
        monitor.beginTask(Messages.getString("ImportProjectsUtilities.task.importingProject"), 1); //$NON-NLS-1$

        ArrayList fileSystemObjects = new ArrayList();
        ImportProjectsUtilities.getFilesForProject(fileSystemObjects, provider, source);

        ImportOperation operation = new ImportOperation(path, source, provider, new MyOverwriteQuery(), fileSystemObjects);
        operation.setContext(shell);
        operation.setOverwriteResources(overwriteResources);
        operation.setCreateContainerStructure(createContainerStructure);
        operation.run(new SubProgressMonitor(monitor, 1));
        monitor.done();
    }

    /**
     * Return a list of all files in the project
     * 
     * Method as taken in org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage.
     * 
     * @param provider The provider for the parent file
     * @param entry The root directory of the project
     * @return A list of all files in the project
     */
    public static void getFilesForProject(Collection files, IImportStructureProvider provider, Object entry) {
        List children = provider.getChildren(entry);
        Iterator childrenEnum = children.iterator();

        while (childrenEnum.hasNext()) {

            Object child = childrenEnum.next();
            // Add the child, this way we get every files except the project
            // folder itself which we don't want
            files.add(child);
            // We don't have isDirectory for tar so must check for children
            // instead
            if (provider.isFolder(child)) {
                getFilesForProject(files, provider, child);
            }
        }
    }

    /**
     * Collect the list of .project files that are under directory into files.
     * 
     * <br/>
     * Method almost as taken in org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage. Modifications
     * are:
     * <ol>
     * <li>no recusrive search</li>
     * <li>add searchFileName as parameter</li>
     * <li>checks if monitor is null</li>
     * </ol>
     * 
     * @param files
     * @param directory
     * @param monitor The monitor to report to
     * @param searchFileName
     * @return boolean <code>true</code> if the operation was completed.
     */
    public static boolean collectProjectFilesFromDirectory(Collection files, File directory, IProgressMonitor monitor,
            String searchFileName) {
        if (monitor != null && monitor.isCanceled()) {
            return false;
        }
        if (monitor != null) {
            monitor.subTask(Messages.getString("ImportProjectsUtilities.task.checkingFolder", directory.getPath())); //$NON-NLS-1$
        }
        File[] contents = directory.listFiles();
        // first look for project description files
        for (int i = 0; i < contents.length; i++) {
            File file = contents[i];
            if (file.isFile() && file.getName().equals(searchFileName)) {
                files.add(file);
                // don't search sub-directories since we can't have nested
                // projects
                return true;
            }
        }
        return true;
    }

    /**
     * Collect the list of .project files that are under directory into files.
     * 
     * <br/>
     * Method almost as taken in org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage. Modifications
     * are:
     * <ol>
     * <li>no recusrive search</li>
     * <li>add searchFileName as parameter</li>
     * <li>checks if monitor is null</li>
     * </ol>
     * 
     * @param files
     * @param monitor The monitor to report to
     * @return boolean <code>true</code> if the operation was completed.
     */
    public static boolean collectProjectFilesFromProvider(Collection files, IImportStructureProvider provider, Object entry,
            int level, IProgressMonitor monitor, String searchFileName) {

        if (monitor != null && monitor.isCanceled()) {
            return false;
        }
        if (monitor != null) {
            monitor.subTask(Messages.getString("ImportProjectsUtilities.task.checkingFolder", provider.getLabel(entry))); //$NON-NLS-1$
        }
        List children = provider.getChildren(entry);
        if (children == null) {
            children = new ArrayList(1);
        }
        Iterator childrenEnum = children.iterator();
        while (childrenEnum.hasNext()) {
            Object child = childrenEnum.next();
            if (level < 1) {
                if (provider.isFolder(child)) {
                    collectProjectFilesFromProvider(files, provider, child, level + 1, monitor, searchFileName);
                }
            }
            String elementLabel = provider.getLabel(child);
            if (elementLabel.equals(searchFileName)) {
                files.add(elementLabel);
            }
        }
        return true;
    }

    /**
     * 
     * DOC smallet ImportDemoProjectAction class global comment. Detailled comment <br/>
     * 
     * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
     * 
     */
    private static class MyOverwriteQuery implements IOverwriteQuery {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.dialogs.IOverwriteQuery#queryOverwrite(java.lang.String )
         */
        public String queryOverwrite(String pathString) {
            return pathString;
        }

    }

    /**
     * Gets all demo projects information.
     * 
     * @return a list of <code>DemoProjectBean</code>
     */
    public static List<DemoProjectBean> getAllDemoProjects() {
        SAXReader reader = new SAXReader();
        Document doc = null;
        List<DemoProjectBean> demoProjectList = new ArrayList<DemoProjectBean>();
        DemoProjectBean demoProject = null;
        List<File> xmlFilePath = getXMLFilePath();
        for (int t = 0; t < xmlFilePath.size(); t++) {
            try {
                doc = reader.read(xmlFilePath.get(t));
            } catch (DocumentException e) {
                ExceptionHandler.process(e);
                return null;
            }

            Element demoProjectsInfo = doc.getRootElement();

            IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                    IBrandingService.class);
            String[] availableLanguages = brandingService.getBrandingConfiguration().getAvailableLanguages();

            for (Iterator<DemoProjectBean> i = demoProjectsInfo.elementIterator("project"); i.hasNext();) { //$NON-NLS-1$
                Element demoProjectElement = (Element) i.next();
                demoProject = new DemoProjectBean();
                demoProject.setProjectName(demoProjectElement.attributeValue("name")); //$NON-NLS-1$
                String language = demoProjectElement.attributeValue("language"); //$NON-NLS-1$

                if (!ArrayUtils.contains(availableLanguages, language)) {
                    // if the language is not available in current branding, don't display this demo project
                    continue;
                }

                demoProject.setLanguage(ECodeLanguage.getCodeLanguage(language));
                String demoProjectFileType = demoProjectElement.attributeValue("demoProjectFileType"); //$NON-NLS-1$
                demoProject.setDemoProjectFileType(EDemoProjectFileType.getDemoProjectFileTypeName(demoProjectFileType));
                demoProject.setDemoProjectFilePath(demoProjectElement.attributeValue("demoFilePath")); //$NON-NLS-1$
                demoProject.setDescriptionFilePath(demoProjectElement.attributeValue("descriptionFilePath")); //$NON-NLS-1$
                //get the demo plugin Id                
                demoProject.setPluginId(demoProjectElement.attributeValue("pluginId")); //$NON-NLS-1$                
                if (demoProject.getProjectName().equals("ESBDEMOS")) {
                    if (!PluginChecker.isPluginLoaded("org.talend.repository.services")) {
                        continue;
                    }
                }
                demoProjectList.add(demoProject);
            }
        }
        return demoProjectList;
    }

    private static String getMDMDemoPluginId(){
        if (!PluginChecker.isPluginLoaded("org.talend.mdm.workbench.enterprise")) {//CE //$NON-NLS-1$ 
            return "org.talend.mdm.repository"; //$NON-NLS-1$
        }else{//EE
            return "org.talend.mdm.repository.enterprise"; //$NON-NLS-1$
        }
    }
    /**
     * Gets the path of demo projects xml file.
     * 
     * @return String
     */
    private static List<File> getXMLFilePath() {
        List<File> xmlListFile = new ArrayList<File>();
        String[] pluginIDs = new String[] { ResourcesPlugin.PLUGIN_ID, "org.talend.resources.perl", //$NON-NLS-1$
                ResourcesPlugin.TDQ_PLUGIN_ID , getMDMDemoPluginId()}; //$NON-NLS-1$

        for (int i = 0; i < pluginIDs.length; i++) {
            Bundle bundle = Platform.getBundle(pluginIDs[i]);
            if (bundle != null) {
                URL url = null;

                String fullPath = XML_FILE_PATH;
                if (ResourcesPlugin.TDQ_PLUGIN_ID.equals(pluginIDs[i])) {
                    fullPath = PluginConstant.EMPTY_STRING;
                }
                URL fileUrl=FileLocator.find(bundle, new Path(fullPath), null);
                try {
                    if(fileUrl!=null){
                        url = FileLocator.toFileURL(fileUrl);
                    }
                } catch (IOException e) {
                    ExceptionHandler.process(e);
                }
                if(url==null)
                    continue;
                File xmlFilePath = new File(url.getPath());
                if (xmlFilePath.exists()) {
                    String files[] = xmlFilePath.list(new FilenameFilter() {

                        public boolean accept(File arg0, String arg1) {
                            return XmlUtil.isXMLFile(arg1);
                        }
                    });
                    for (String file : files) {
                        File xml = new File(url.getPath() + "/" + file); //$NON-NLS-1$
                        xmlListFile.add(xml);
                    }
                }
            }
        }
        return xmlListFile;
    }
}
