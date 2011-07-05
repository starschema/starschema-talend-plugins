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
package org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.EList;
import org.osgi.framework.Bundle;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.ILibrariesService;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.designer.runprocess.LastGenerationInfo;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobJavaScriptsManager;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class JobJavaScriptOSGIForESBManager extends JobJavaScriptsManager {

    private static final String PACKAGE_SEPARATOR = ".";

    private static final String JAVA = "java";

    private static final String ROUTE = "route";

    private static final String JOB = "job";

    private static Logger logger = Logger.getLogger(JobJavaScriptOSGIForESBManager.class);

    private static final String BLUEPRINT = "blueprint"; //$NON-NLS-1$

    private static final String OSGI_INF = "OSGI-INF"; //$NON-NLS-1$

    private String packageName;

    private String jobName;

    private String jobClassName;

    private String jobVersion;

    private String itemType = null;

    public List<ExportFileResource> getExportResources(ExportFileResource[] process, Map<ExportChoice, Object> exportChoice,
            String contextName, String launcher, int statisticPort, int tracePort, String... codeOptions)
            throws ProcessorException {
        List<ExportFileResource> list = new ArrayList<ExportFileResource>();

        boolean needJob = true;
        ExportFileResource libResource = new ExportFileResource(null, LIBRARY_FOLDER_NAME); //$NON-NLS-1$
        ExportFileResource osgiResource = new ExportFileResource(null, ""); //$NON-NLS-1$
        ExportFileResource jobScriptResource = new ExportFileResource(null, ""); //$NON-NLS-1$

        if (needJob) {
            list.add(libResource);
        }
        list.add(osgiResource);
        list.add(jobScriptResource);

        // set export config mode now only to be sure that the libraries will be setup for an export mode, and not
        // editor mode.
        ProcessorUtilities.setExportConfig(JAVA, "", ""); //$NON-NLS-1$

        // Gets talend libraries

        Set<String> neededLibraries = null;
        for (int i = 0; i < process.length; i++) {
            ProcessItem processItem = (ProcessItem) process[i].getItem();
            jobName = processItem.getProperty().getLabel();
            packageName = JavaResourcesHelper.getProjectFolderName(processItem)
                    + PACKAGE_SEPARATOR
                    + JavaResourcesHelper.getJobFolderName(processItem.getProperty().getLabel(), processItem.getProperty()
                            .getVersion());
            jobClassName = packageName + PACKAGE_SEPARATOR + jobName;

            jobVersion = processItem.getProperty().getVersion();
            if (!isMultiNodes() && this.getSelectedJobVersion() != null) {
                jobVersion = this.getSelectedJobVersion();
            }
            ERepositoryObjectType type = ERepositoryObjectType.getItemType(processItem);
            if (type.equals(ERepositoryObjectType.PROCESS)) {
                itemType = JOB;
            } else {
                itemType = ROUTE;
            }

            // generate the source files
            String libPath = calculateLibraryPathFromDirectory(process[i].getDirectoryName());
            // use character @ as temporary classpath separator, this one will be replaced during the export.
            String standardJars = libPath + PATH_SEPARATOR + SYSTEMROUTINE_JAR + ProcessorUtilities.TEMP_JAVA_CLASSPATH_SEPARATOR
                    + libPath + PATH_SEPARATOR + USERROUTINE_JAR + ProcessorUtilities.TEMP_JAVA_CLASSPATH_SEPARATOR
                    + PACKAGE_SEPARATOR; //$NON-NLS-1$
            ProcessorUtilities.setExportConfig(JAVA, standardJars, libPath); //$NON-NLS-1$

            if (!isOptionChoosed(exportChoice, ExportChoice.doNotCompileCode)) {
                if (neededLibraries == null) {
                    neededLibraries = new HashSet<String>();
                }
                generateJobFiles(processItem, contextName, jobVersion, statisticPort != IProcessor.NO_STATISTICS,
                        tracePort != IProcessor.NO_TRACES, isOptionChoosed(exportChoice, ExportChoice.applyToChildren),
                        true /* isExportAsOSGI */, progressMonitor);
                neededLibraries.addAll(LastGenerationInfo.getInstance().getModulesNeededWithSubjobPerJob(
                        processItem.getProperty().getId(), jobVersion));
            } else {
                LastGenerationInfo.getInstance().setModulesNeededWithSubjobPerJob(processItem.getProperty().getId(),
                        processItem.getProperty().getVersion(), neededLibraries);
                LastGenerationInfo.getInstance().setLastMainJob(null);
            }

            // generate jar file for job
            getJobScriptsUncompressed(jobScriptResource, processItem);

            // dynamic db xml mapping
            addXmlMapping(process[i], isOptionChoosed(exportChoice, ExportChoice.needSourceCode));

        }

        // Gets talend libraries
        List<URL> talendLibraries = getExternalLibraries(true, process, neededLibraries);
        libResource.addResources(talendLibraries);

        List<String> esbFiles = generateESBFiles(process[0].getItem(), contextName);

        List<URL> urlList = new ArrayList<URL>();
        try {
            for (String file : esbFiles) {
                urlList.add(new File(file).toURL());
            }
        } catch (MalformedURLException e) {
            ExceptionHandler.process(e);
            logger.error(e);
        }
        osgiResource.addResources(getOSGIInfFolder(), urlList);

        // Gets system routines
        List<URL> systemRoutineList = getSystemRoutine(process, true);
        libResource.addResources(systemRoutineList);
        // Gets user routines
        List<URL> userRoutineList = getUserRoutine(process, true);
        libResource.addResources(userRoutineList);

        // generate the META-INFO folder
        ExportFileResource metaInfoFolder = genMetaInfoFolder(libResource);
        list.add(metaInfoFolder);

        return list;
    }

    protected List<String> generateESBFiles(Item processItem, String contextName) {
        List<String> files = new ArrayList<String>();
        final Bundle b = Platform.getBundle(RepositoryPlugin.PLUGIN_ID);
        try {
            if (itemType == null)
                itemType = JOB;
            String inputFile = FileLocator.toFileURL(
                    FileLocator.find(b, new Path("resources/" + itemType + "-template.xml"), null)) //$NON-NLS-1$
                    .getFile();
            String targetFile = getTmpFolder() + PATH_SEPARATOR + "job.xml"; //$NON-NLS-1$
            readAndReplaceInXmlTemplate(inputFile, targetFile, jobName, jobClassName, itemType);
            files.add(targetFile);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
        return files;
    }

    private String getOSGIInfFolder() {
        return OSGI_INF.concat(PATH_SEPARATOR).concat(BLUEPRINT);
    }

    protected void readAndReplaceInXmlTemplate(String inputFile, String outputFile, String jobName, String jobClassName,
            String itemType) {
        FileReader fr = null;
        try {
            fr = new FileReader(inputFile);
            BufferedReader br = new BufferedReader(fr);

            FileWriter fw = new FileWriter(outputFile);
            BufferedWriter bw = new BufferedWriter(fw);

            String line = br.readLine();
            while (line != null) {
                line = line.replace("@JOBNAME@", jobName).replace("@TYPE@", itemType).replace("@JOBCLASSNAME@", jobClassName); //$NON-NLS-1$ //$NON-NLS-2$
                bw.write(line + "\n"); //$NON-NLS-1$
                line = br.readLine();
            }
            bw.flush();
            fr.close();
            fw.close();
        } catch (FileNotFoundException e) {
            ExceptionHandler.process(e);
            logger.error(e);
        } catch (IOException e) {
            ExceptionHandler.process(e);
            logger.error(e);
        }
    }

    protected void getContextScripts(ProcessItem processItem, Boolean needContext, ExportFileResource contextResource,
            String version) {
        String jobName = processItem.getProperty().getLabel();
        addContextScripts(processItem, jobName, version, contextResource, needContext);
    }

    protected ExportFileResource genMetaInfoFolder(ExportFileResource libResource) {
        ExportFileResource metaInfoResource = new ExportFileResource(null, "META-INF"); //$NON-NLS-1$

        // generate the MANIFEST.MF file in the temp folder
        String manifestPath = getTmpFolder() + PATH_SEPARATOR + "MANIFEST.MF"; //$NON-NLS-1$

        FileOutputStream fos = null;
        try {
            Manifest manifest = getManifest(libResource);
            fos = new FileOutputStream(manifestPath);
            manifest.write(fos);
        } catch (FileNotFoundException e1) {
            ExceptionHandler.process(e1);
            logger.error(e1);
        } catch (IOException e1) {
            ExceptionHandler.process(e1);
            logger.error(e1);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    ExceptionHandler.process(e);
                    logger.error(e);
                }
            }
        }

        List<URL> urlList = new ArrayList<URL>();
        try {
            urlList.add(new File(manifestPath).toURL());
        } catch (MalformedURLException e) {
            ExceptionHandler.process(e);
        }
        metaInfoResource.addResources(urlList);

        return metaInfoResource;
    }

    protected Manifest getManifest(ExportFileResource libResource) throws IOException {
        Manifest manifest = new Manifest();
        Attributes a = manifest.getMainAttributes();
        a.put(Attributes.Name.MANIFEST_VERSION, "1.0"); //$NON-NLS-1$
        a.put(new Attributes.Name("Bundle-Name"), jobName); //$NON-NLS-1$
        a.put(new Attributes.Name("Bundle-SymbolicName"), jobName); //$NON-NLS-1$
        a.put(new Attributes.Name("Bundle-Version"), jobVersion); //$NON-NLS-1$
        a.put(new Attributes.Name("Bundle-ManifestVersion"), "2"); //$NON-NLS-1$ //$NON-NLS-2$
        a.put(new Attributes.Name("Export-Package"), packageName); //$NON-NLS-1$
        if (ROUTE.equals(itemType)) {
            a.put(new Attributes.Name("Require-Bundle"), "org.apache.camel.camel-core");
            a.put(new Attributes.Name("Import-Package"), "javax.xml.bind,org.apache.camel;version=\"[2.7,3)\",org.apache.camel.builder;" + //$NON-NLS-1$
                            "version=\"[2.7,3)\",org.apache.camel.impl;version=\"[2.7,3)\",org.apache.camel.management;version=\"[2.7,3)\","
                            + //$NON-NLS-1$
                            "org.apache.camel.model;version=\"[2.7,3)\",org.apache.camel.osgi;version=\"[2.7,3)\"," + //$NON-NLS-1$
                            "org.apache.camel.spi;version=\"[2.7,3)\",org.apache.camel.view;version=\"[2.7,3)\"," + //$NON-NLS-1$
                            "org.osgi.framework;version=\"[1.5,2)\"," + //$NON-NLS-1$
                            "org.osgi.service.blueprint;version=\"[1.0.0,2.0.0)\",routines.system.api"); //$NON-NLS-1$
        } else {
            a.put(new Attributes.Name("Import-Package"), //$NON-NLS-1$
                    "routines.system.api;resolution:=optional" + //$NON-NLS-1$
                            ",org.dom4j;resolution:=optional" + //$NON-NLS-1$
                            ",org.dom4j.io;resolution:=optional" + //$NON-NLS-1$
                            ",org.dom4j.tree;resolution:=optional" + //$NON-NLS-1$
                            ",org.jaxen;resolution:=optional" + //$NON-NLS-1$
                            ",javax.xml.soap;resolution:=optional" + //$NON-NLS-1$
                            ",javax.xml.ws.soap;resolution:=optional"); //$NON-NLS-1$
        }

        a.put(new Attributes.Name("Bundle-ClassPath"), getClassPath(libResource)); //$NON-NLS-1$
        a.put(new Attributes.Name("Export-Service"), "routines.system.api.TalendJob;name=" + jobName + ";type=" + itemType); //$NON-NLS-1$

        return manifest;
    }

    private String getClassPath(ExportFileResource libResource) {
        StringBuffer libBuffer = new StringBuffer();
        libBuffer.append(PACKAGE_SEPARATOR).append(","); //$NON-NLS-1$ //$NON-NLS-2$
        Set<String> relativePathList = libResource.getRelativePathList();
        for (String path : relativePathList) {
            Set<URL> resources = libResource.getResourcesByRelativePath(path);
            for (URL url : resources) {
                File currentResource = new File(url.getPath());
                libBuffer.append(libResource.getDirectoryName() + PATH_SEPARATOR + currentResource.getName()).append(","); //$NON-NLS-1$
            }
        }
        libBuffer.deleteCharAt(libBuffer.length() - 1);
        return libBuffer.toString();
    }

    @Override
    protected List<URL> getExternalLibraries(boolean needLibraries, ExportFileResource[] process, Set<String> neededLibraries) {
        List<URL> list = new ArrayList<URL>();
        if (!needLibraries) {
            return list;
        }
        // jar from routines
        List<IRepositoryViewObject> collectRoutines = new ArrayList<IRepositoryViewObject>();
        boolean useBeans = false;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
            ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                    ICamelDesignerCoreService.class);
            if (camelService.isInstanceofCamel(process[0].getItem())) {
                useBeans = true;
            }
        }
        // Lists all the needed jar files
        Set<String> listModulesReallyNeeded = new HashSet<String>();
        ILibrariesService librariesService = CorePlugin.getDefault().getLibrariesService();
        String path = librariesService.getLibrariesPath();
        File file = new File(path);
        File[] files = file.listFiles(FilesUtils.getAcceptModuleFilesFilter());

        if (!useBeans) {
            // Gets all the jar files
            if (neededLibraries == null) {
                // in case export as been done with option "not recompile", then libraires can't be retrieved when
                // build.
                IDesignerCoreService designerService = RepositoryPlugin.getDefault().getDesignerCoreService();
                for (int i = 0; i < process.length; i++) {
                    ExportFileResource resource = process[i];
                    ProcessItem item = (ProcessItem) resource.getItem();
                    String version = item.getProperty().getVersion();
                    if (!isMultiNodes() && this.getSelectedJobVersion() != null) {
                        version = this.getSelectedJobVersion();
                    }
                    ProcessItem selectedProcessItem;
                    if (resource.getNode() != null) {
                        selectedProcessItem = ItemCacheManager.getProcessItem(resource.getNode().getRoot().getProject(), item
                                .getProperty().getId(), version);
                    } else {
                        // if no node given, take in the current project only
                        selectedProcessItem = ItemCacheManager.getProcessItem(item.getProperty().getId(), version);
                    }
                    IProcess iProcess = designerService.getProcessFromProcessItem(selectedProcessItem);
                    neededLibraries = iProcess.getNeededLibraries(true);
                    if (neededLibraries != null) {
                        listModulesReallyNeeded.addAll(neededLibraries);
                    }
                }
            } else {
                listModulesReallyNeeded.addAll(neededLibraries);
            }
        }

        collectRoutines.addAll(collectRoutines(process, useBeans));

        for (IRepositoryViewObject object : collectRoutines) {
            Item item = object.getProperty().getItem();
            if (item instanceof RoutineItem) {
                RoutineItem routine = (RoutineItem) item;
                EList imports = routine.getImports();
                for (Object o : imports) {
                    IMPORTType type = (IMPORTType) o;
                    listModulesReallyNeeded.add(type.getMODULE());
                }
            }
        }

        for (int i = 0; i < files.length; i++) {
            File tempFile = files[i];
            try {
                if (listModulesReallyNeeded.contains(tempFile.getName())) {
                    list.add(tempFile.toURL());
                }
            } catch (MalformedURLException e) {
                ExceptionHandler.process(e);
            }
        }

        return list;
    }
}
