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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobJavaScriptsManager;

/**
 * DOC x class global comment. Detailled comment <br/>
 * 
 */
public class JobJavaScriptESBManager extends JobJavaScriptsManager {

    private static Logger log = Logger.getLogger(ExceptionHandler.class);

    public static final String EXPORT_METHOD = "runJob"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobJavaScriptsManager#getExportResources
     * (org.talend.repository.ui.wizards.exportjob.ExportFileResource[], java.util.Map, java.lang.String,
     * java.lang.String, int, int, java.lang.String[])
     */
    @Override
    public List<ExportFileResource> getExportResources(ExportFileResource[] process, Map<ExportChoice, Object> exportChoice,
            String contextName, String launcher, int statisticPort, int tracePort, String... codeOptions)
            throws ProcessorException {

        List<ExportFileResource> list = new ArrayList<ExportFileResource>();
        HashMap<String, String> jobMap = new HashMap<String, String>();

        boolean needJob = true;
        boolean needContext = isOptionChoosed(exportChoice, ExportChoice.needContext);
        ExportFileResource libResource = new ExportFileResource(null, ""); //$NON-NLS-1$
        ExportFileResource contextResource = new ExportFileResource(null, ""); //$NON-NLS-1$

        ExportFileResource itemsResource = new ExportFileResource(null, ""); //$NON-NLS-1$
        if (needJob) {
            list.add(libResource);
        }
        if (needContext) {
            list.add(contextResource);
        }
        list.add(itemsResource);

        // set export config mode now only to be sure that the libraries will be setup for an export mode, and not
        // editor mode.
        ProcessorUtilities.setExportConfig("java", "", ""); //$NON-NLS-1$

        // Gets talend libraries
        List<URL> talendLibraries = getExternalLibraries(true, process);
        libResource.addResources(talendLibraries);

        for (int i = 0; i < process.length; i++) {
            ProcessItem processItem = (ProcessItem) process[i].getItem();
            String jobName = processItem.getProperty().getLabel();
            String packageName = JavaResourcesHelper.getProjectFolderName(processItem) + "." //$NON-NLS-1$
                    + JavaResourcesHelper.getJobFolderName(processItem.getProperty().getLabel(), processItem.getProperty()
                            .getVersion());
            jobMap.put(jobName, packageName);
            // processItem.
            String selectedJobVersion = processItem.getProperty().getVersion();
            if (!isMultiNodes() && this.getSelectedJobVersion() != null) {
                selectedJobVersion = this.getSelectedJobVersion();
            }

            // generate the source files
            String libPath = calculateLibraryPathFromDirectory(process[i].getDirectoryName());
            // use character @ as temporary classpath separator, this one will be replaced during the export.
            String standardJars = libPath + PATH_SEPARATOR + SYSTEMROUTINE_JAR + ProcessorUtilities.TEMP_JAVA_CLASSPATH_SEPARATOR
                    + libPath + PATH_SEPARATOR + USERROUTINE_JAR + ProcessorUtilities.TEMP_JAVA_CLASSPATH_SEPARATOR + "."; //$NON-NLS-1$
            ProcessorUtilities.setExportConfig("java", standardJars, libPath); //$NON-NLS-1$

            if (!isOptionChoosed(exportChoice, ExportChoice.doNotCompileCode)) {
                generateJobFiles(processItem, contextName, selectedJobVersion, statisticPort != IProcessor.NO_STATISTICS,
                        tracePort != IProcessor.NO_TRACES, isOptionChoosed(exportChoice, ExportChoice.applyToChildren),
                        progressMonitor);
                generateESBActionFile(processItem, contextName);
            }

            addJobItem(process, processItem, isOptionChoosed(exportChoice, ExportChoice.needJobItem), itemsResource,
                    selectedJobVersion);

            addDependencies(process, processItem, isOptionChoosed(exportChoice, ExportChoice.needDependencies), itemsResource);

            // add children jobs
            boolean needChildren = true;
            addSubJobResources(process, processItem, needChildren, exportChoice, libResource, contextResource, itemsResource,
                    selectedJobVersion);

            // generate the context file
            getContextScripts(processItem, needContext, contextResource, selectedJobVersion);

            // generate jar file for job
            libResource.addResources(getJobScripts(processItem, selectedJobVersion, needJob));

            // dynamic db xml mapping
            addXmlMapping(process[i], isOptionChoosed(exportChoice, ExportChoice.needSourceCode));

        }

        prepareESBFiles(jobMap, exportChoice);

        // generate the META-INFO folder
        ExportFileResource metaInfoFolder = genMetaInfoFolder();
        list.add(metaInfoFolder);

        // Gets system routines
        List<URL> systemRoutineList = getSystemRoutine(process, true);
        libResource.addResources(systemRoutineList);
        // Gets user routines
        List<URL> userRoutineList = getUserRoutine(process, true);
        libResource.addResources(userRoutineList);

        // copy jbm-queue-service.xml or jbmq-queue-service.xml
        String serverConfigFile;
        if (JavaJobScriptsExportWSWizardPage.ESBTYPE_JBOSS_MESSAGING.equals(exportChoice.get(ExportChoice.esbExportType))) {
            serverConfigFile = getTmpFolder() + PATH_SEPARATOR + "jbm-queue-service.xml"; //$NON-NLS-1$
        } else {
            serverConfigFile = getTmpFolder() + PATH_SEPARATOR + "jbmq-queue-service.xml"; //$NON-NLS-1$
        }
        // String ESBT
        ArrayList<URL> urlList = new ArrayList<URL>();
        try {
            urlList.add(new File(serverConfigFile).toURL());
        } catch (MalformedURLException e) {
            ExceptionHandler.process(e);
        }
        libResource.addResources(urlList);

        return list;
    }

    /**
     * DOC nrousseau Comment method "generateESBActionFile".
     * 
     * @param processItem
     * @param contextName
     */
    protected void generateESBActionFile(ProcessItem processItem, String contextName) {
        String packageName = JavaResourcesHelper.getProjectFolderName(processItem) + "." //$NON-NLS-1$
                + JavaResourcesHelper.getJobFolderName(processItem.getProperty().getLabel(), processItem.getProperty()
                        .getVersion());
        String jobName = processItem.getProperty().getLabel();
        final Bundle b = Platform.getBundle(RepositoryPlugin.PLUGIN_ID);
        try {
            String file;

            if (PluginChecker.isPluginLoaded("org.talend.designer.components.thalesprovider")) { //$NON-NLS-1$
                file = FileLocator.toFileURL(
                        FileLocator.find(b, new Path("resources/ESBListenerActionWithMessages.javatemplate"), null)) //$NON-NLS-1$
                        .getFile();
            } else {
                file = FileLocator.toFileURL(FileLocator.find(b, new Path("resources/ESBListenerAction.javatemplate"), null)) //$NON-NLS-1$
                        .getFile();
            }

            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IProject rootProject = root.getProject(JavaUtils.JAVA_PROJECT_NAME);

            IFile outputFile = rootProject
                    .getFile("src/" + packageName.replace(".", "/") + "/" + jobName + "ListenerAction.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

            String line = br.readLine();
            StringBuffer stringBuff = new StringBuffer();
            while (line != null) {
                line = line.replace("<JobPackage>", packageName).replace("<JobName>", jobName); //$NON-NLS-1$ //$NON-NLS-2$
                stringBuff.append(line + "\n"); //$NON-NLS-1$
                line = br.readLine();
            }
            InputStream outputStream = new ByteArrayInputStream(stringBuff.toString().getBytes());
            if (!outputFile.exists()) {
                File systemFile = outputFile.getLocation().toFile();
                if (systemFile.exists()) {
                    systemFile.delete();
                    outputFile.getParent().refreshLocal(IResource.DEPTH_INFINITE, null);
                }

                outputFile.create(outputStream, true, null);
            } else {
                outputFile.setContents(outputStream, true, false, null);
            }

            fr.close();

        } catch (IOException e) {
            ExceptionHandler.process(e);
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
        try {
            CorePlugin.getDefault().getRunProcessService().getJavaProject().getProject()
                    .build(IncrementalProjectBuilder.AUTO_BUILD, null);
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
    }

    protected void addSubJobResources(ExportFileResource[] allResources, ProcessItem process, boolean needChildren,
            Map<ExportChoice, Object> exportChoice, ExportFileResource libResource, ExportFileResource contextResource,
            ExportFileResource srcResource, String selectedJobVersion) {

        List<JobInfo> list = new ArrayList<JobInfo>();
        String projectName = getCorrespondingProjectName(process);
        if (needChildren) {

            try {
                List<ProcessItem> processedJob = new ArrayList<ProcessItem>();
                getChildrenJobAndContextName(allResources, process.getProperty().getLabel(), list, process, projectName,
                        processedJob, srcResource, exportChoice, selectedJobVersion);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }

        for (Iterator<JobInfo> iter = list.iterator(); iter.hasNext();) {
            JobInfo jobInfo = iter.next();
            libResource.addResources(getJobScripts(projectName, jobInfo.getJobName(), jobInfo.getJobVersion(), true));
            addContextScripts(jobInfo.getProcessItem(), jobInfo.getJobName(), jobInfo.getJobVersion(), contextResource,
                    isOptionChoosed(exportChoice, ExportChoice.needContext));
            addDependencies(allResources, jobInfo.getProcessItem(), isOptionChoosed(exportChoice, ExportChoice.needDependencies),
                    srcResource);
        }

    }

    protected void prepareESBFiles(HashMap<String, String> jobMap, Map<ExportChoice, Object> exportChoice) {
        String jobName = jobMap.keySet().iterator().next();
        String jobWithPackageName = jobMap.get(jobName);
        String jobAlias = jobWithPackageName.replace(".", "_"); //$NON-NLS-1$ //$NON-NLS-2$

        final Bundle b = Platform.getBundle(RepositoryPlugin.PLUGIN_ID);

        try {
            String inputFile = FileLocator.toFileURL(FileLocator.find(b, new Path("resources/jboss-esb-template.xml"), null)) //$NON-NLS-1$
                    .getFile();
            String targetFile = getTmpFolder() + PATH_SEPARATOR + "jboss-esb.xml"; //$NON-NLS-1$
            readAndReplaceInXmlTemplate(inputFile, targetFile, jobName, jobAlias, jobWithPackageName, exportChoice);

            inputFile = FileLocator.toFileURL(FileLocator.find(b, new Path("resources/deployment-template.xml"), null)).getFile(); //$NON-NLS-1$
            targetFile = getTmpFolder() + PATH_SEPARATOR + "deployment.xml"; //$NON-NLS-1$
            readAndReplaceInXmlTemplate(inputFile, targetFile, jobName, jobAlias, jobWithPackageName, exportChoice);

            if (JavaJobScriptsExportWSWizardPage.ESBTYPE_JBOSS_MESSAGING.equals(exportChoice.get(ExportChoice.esbExportType))) {
                inputFile = FileLocator
                        .toFileURL(FileLocator.find(b, new Path("resources/jbm-queue-service-template.xml"), null)) //$NON-NLS-1$
                        .getFile();
                targetFile = getTmpFolder() + PATH_SEPARATOR + "jbm-queue-service.xml"; //$NON-NLS-1$
            } else {
                inputFile = FileLocator.toFileURL(
                        FileLocator.find(b, new Path("resources/jbmq-queue-service-template.xml"), null)) //$NON-NLS-1$
                        .getFile();
                targetFile = getTmpFolder() + PATH_SEPARATOR + "jbmq-queue-service.xml"; //$NON-NLS-1$
            }
            readAndReplaceInXmlTemplate(inputFile, targetFile, jobName, jobAlias, jobWithPackageName, exportChoice);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }

    }

    protected void readAndReplaceInXmlTemplate(String inputFile, String outputFile, String jobName, String jobAlias,
            String jobPackage, Map<ExportChoice, Object> exportChoice) {
        FileReader fr;
        try {
            fr = new FileReader(inputFile);
            BufferedReader br = new BufferedReader(fr);

            FileWriter fw = new FileWriter(outputFile);
            BufferedWriter bw = new BufferedWriter(fw);

            String line = br.readLine();
            while (line != null) {
                line = line.replace("#JobName#", jobName).replace("#JobAlias#", jobAlias).replace("#JobPackage#", jobPackage) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        .replace("#QueueName#", (String) exportChoice.get(ExportChoice.esbQueueMessageName)) //$NON-NLS-1$
                        .replace("#ServiceName#", (String) exportChoice.get(ExportChoice.esbServiceName)) //$NON-NLS-1$
                        .replace("#Category#", (String) exportChoice.get(ExportChoice.esbCategory)); //$NON-NLS-1$
                bw.write(line + "\n"); //$NON-NLS-1$
                line = br.readLine();
            }
            bw.flush();
            fr.close();
            fw.close();
        } catch (FileNotFoundException e) {
            ExceptionHandler.process(e);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
    }

    protected void getContextScripts(ProcessItem processItem, Boolean needContext, ExportFileResource contextResource,
            String version) {
        String jobName = processItem.getProperty().getLabel();
        addContextScripts(processItem, jobName, version, contextResource, needContext);
    }

    /**
     * DOC x Comment method "genMetaInfoForder".
     * 
     * @param list
     * @return
     */
    protected ExportFileResource genMetaInfoFolder() {
        ExportFileResource metaInfoResource = new ExportFileResource(null, "META-INF"); //$NON-NLS-1$

        // generate the MANIFEST.MF file in the temp folder
        String manifestPath = getTmpFolder() + PATH_SEPARATOR + "MANIFEST.MF"; //$NON-NLS-1$

        FileOutputStream fos = null;
        try {
            Manifest manifest = getManifest();
            fos = new FileOutputStream(manifestPath);
            manifest.write(fos);
        } catch (FileNotFoundException e1) {
            ExceptionHandler.process(e1);
        } catch (IOException e1) {
            ExceptionHandler.process(e1);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    ExceptionHandler.process(e);
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

        // copy jboss-esb.xml
        String serverConfigFile = getTmpFolder() + PATH_SEPARATOR + "jboss-esb.xml"; //$NON-NLS-1$
        urlList = new ArrayList<URL>();
        try {
            urlList.add(new File(serverConfigFile).toURL());
        } catch (MalformedURLException e) {
            ExceptionHandler.process(e);
        }
        metaInfoResource.addResources(urlList);

        // copy deployment.xml
        serverConfigFile = getTmpFolder() + PATH_SEPARATOR + "deployment.xml"; //$NON-NLS-1$
        urlList = new ArrayList<URL>();
        try {
            urlList.add(new File(serverConfigFile).toURL());
        } catch (MalformedURLException e) {
            ExceptionHandler.process(e);
        }
        metaInfoResource.addResources(urlList);

        return metaInfoResource;
    }

    protected Manifest getManifest() throws IOException {
        Manifest manifest = new Manifest();
        Attributes a = manifest.getMainAttributes();
        a.put(Attributes.Name.MANIFEST_VERSION, "1.0"); //$NON-NLS-1$
        IBrandingService service = (IBrandingService) GlobalServiceRegister.getDefault().getService(IBrandingService.class);
        a.put(Attributes.Name.IMPLEMENTATION_VENDOR, service.getFullProductName());
        return manifest;
    }
}
