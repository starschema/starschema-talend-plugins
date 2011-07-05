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

package org.talend.repository.ui.wizards.exportjob.scriptsmanager.petals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobJavaScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.JobJavaScriptESBManager;

/**
 * The manager in charge of selecting the resources to export for PEtALS.
 * 
 * @author Vincent Zurczak - EBM WebSourcing
 */
public class PetalsJobJavaScriptsManager extends JobJavaScriptsManager {

    public static final String SU_NAMESPACE_URI_PREFIX = "http://petals.ow2.org/talend/"; //$NON-NLS-1$

    public static final boolean NEED_CONTEXT = true;

    public static final boolean APPLY_TO_CHILDREN = true;

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobJavaScriptsManager #getExportResources(
     * org.talend.repository.documentation.ExportFileResource[], java.util.Map, java.lang.String, java.lang.String, int,
     * int, java.lang.String[])
     */
    @Override
    public List<ExportFileResource> getExportResources(ExportFileResource[] process, Map<ExportChoice, Object> exportChoice,
            String contextName, String launcher, int statisticPort, int tracePort, String... codeOptions) {

        // Get options
        boolean needSource = (Boolean) exportChoice.get(ExportChoice.needSourceCode);
        boolean needDependencies = (Boolean) exportChoice.get(ExportChoice.needDependencies);
        boolean needUserRoutines = (Boolean) exportChoice.get(ExportChoice.needUserRoutine);

        boolean generateEndpoint = PetalsTemporaryOptionsKeeper.INSTANCE.isGenerateEndpoint();
        boolean singleton = PetalsTemporaryOptionsKeeper.INSTANCE.isSingleton();
        boolean validateByWSdl = PetalsTemporaryOptionsKeeper.INSTANCE.isValidateByWsdl();

        // For each exported job, define the resources to export...
        // ... and associate them with the item (the item can't be null)
        List<ExportFileResource> resources = new ArrayList<ExportFileResource>();
        for (ExportFileResource proces : process) {
            ProcessItem processItem = (ProcessItem) proces.getItem();
            String selectedJobVersion = processItem.getProperty().getVersion();

            // Generate job files
            String libPath = calculateLibraryPathFromDirectory(proces.getDirectoryName());
            String standardJars = libPath + PATH_SEPARATOR + SYSTEMROUTINE_JAR + ProcessorUtilities.TEMP_JAVA_CLASSPATH_SEPARATOR
                    + libPath + PATH_SEPARATOR + USERROUTINE_JAR + ProcessorUtilities.TEMP_JAVA_CLASSPATH_SEPARATOR + "."; //$NON-NLS-1$

            ProcessorUtilities.setExportConfig("java", standardJars, libPath); //$NON-NLS-1$
            try {
                generateJobFiles(processItem, contextName, selectedJobVersion, statisticPort != IProcessor.NO_STATISTICS,
                        tracePort != IProcessor.NO_TRACES, APPLY_TO_CHILDREN, this.progressMonitor);

            } catch (ProcessorException e) {
                e.printStackTrace();
            }

            // Job libraries - routines, libraries, etc...
            ExportFileResource libResource = new ExportFileResource(processItem, null);
            resources.add(libResource);

            List<URL> talendLibraries = getExternalLibraries(true, process);
            libResource.addResources(talendLibraries);

            // dynamic db xml mapping
            addXmlMapping(proces, isOptionChoosed(exportChoice, ExportChoice.needSourceCode));

            List<URL> systemRoutineList = getSystemRoutine(process, true);
            libResource.addResources(systemRoutineList);

            addDependencies(process, processItem, needDependencies, libResource);
            if (needUserRoutines) {
                List<URL> userRoutineList = getUserRoutine(process, true);
                libResource.addResources(userRoutineList);
            }
            // Generate jar file for the job
            libResource.addResources(getJobScripts(processItem, selectedJobVersion, true));

            // Job sources
            ExportFileResource srcResource = new ExportFileResource(processItem, null);
            addSourceCode(process, processItem, needSource, srcResource, selectedJobVersion);
            resources.add(srcResource);

            // Contexts
            ExportFileResource contextResource = new ExportFileResource(processItem, null);
            addContextScripts(contextResource, selectedJobVersion, NEED_CONTEXT);
            resources.add(contextResource);

            // Job children: order elements in the same "categories"
            // All the contexts together, all the libraries together...
            addSubJobResources(process, processItem, APPLY_TO_CHILDREN, exportChoice, libResource, contextResource, srcResource,
                    selectedJobVersion);

            // jbi.xml file
            ExportFileResource jbiDescriptorFile = generateJbiDescriptorFile(processItem, generateEndpoint, singleton,
                    validateByWSdl, contextName);
            resources.add(jbiDescriptorFile);

            // The WSDL file
            ExportFileResource wsdlFile = generateWsdlFile(processItem);
            resources.add(wsdlFile);
        }
        // routines src
        ExportFileResource routinesSrcResource = new ExportFileResource();
        addDependenciesSourceCode(process, routinesSrcResource, needSource);
        resources.add(routinesSrcResource);

        return resources;
    }

    /**
     * Copied from {@link JobJavaScriptESBManager}.
     * 
     * @param allResources
     * @param process
     * @param needChildren
     * @param exportChoice
     * @param libResource
     * @param contextResource
     * @param srcResource
     * @param selectedJobVersion
     * @see JobJavaScriptESBManager
     */
    private void addSubJobResources(ExportFileResource[] allResources, ProcessItem process, boolean needChildren,
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

        for (JobInfo jobInfo : list) {
            libResource.addResources(getJobScripts(projectName, jobInfo.getJobName(), jobInfo.getJobVersion(), true));
            addContextScripts(jobInfo.getProcessItem(), jobInfo.getJobName(), jobInfo.getJobVersion(), contextResource, true);
        }

    }

    /**
     * Generates and creates an {@link ExportFileResource} for the jbi.xml of a SU.
     * 
     * @param item the item to export
     * @param genEndpoint true to let PEtALS generate the end-point
     * @param singleton
     * @param validateWithWsdl
     * @param contextName the context name
     * @return the {@link ExportFileResource} for the jbi.xml of the service-unit to create.
     */
    protected ExportFileResource generateJbiDescriptorFile(ProcessItem item, boolean genEndpoint, boolean singleton,
            boolean validateWithWsdl, String contextName) {

        // Prepare the parameters
        String jobName = item.getProperty().getLabel();
        String jobVersion = item.getProperty().getVersion();
        Map<String, String> elementNameToValue = new LinkedHashMap<String, String>(7);

        // VZ: reversed the order of the two next lines
        elementNameToValue.put("petalsCDK:validate-wsdl", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        elementNameToValue.put("petalsCDK:wsdl", jobName + ".wsdl"); //$NON-NLS-1$ //$NON-NLS-2$
        // VZ

        elementNameToValue.put("talend:name", jobName); //$NON-NLS-1$

        String projectName = getCorrespondingProjectName(item);
        String folderName = JavaResourcesHelper.getJobFolderName(jobName, jobVersion);
        String className = projectName + "." + folderName + "." + jobName; //$NON-NLS-1$ //$NON-NLS-2$
        elementNameToValue.put("talend:class-name", className); //$NON-NLS-1$

        elementNameToValue.put("talend:context", contextName); //$NON-NLS-1$
        elementNameToValue.put("talend:singleton", String.valueOf(singleton)); //$NON-NLS-1$
        elementNameToValue.put("talend:validate-exchange-by-wsdl", String.valueOf(validateWithWsdl)); //$NON-NLS-1$

        // Content of the jbi.xml file
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
        sb.append("<jbi:jbi version=\"1.0\"\n"); //$NON-NLS-1$
        sb.append("\txmlns:jbi=\"http://java.sun.com/xml/ns/jbi\"\n"); //$NON-NLS-1$
        sb.append("\txmlns:petalsCDK=\"http://petals.ow2.org/components/extensions/version-5\"\n"); //$NON-NLS-1$
        sb.append("\txmlns:talend=\"http://petals.ow2.org/components/talend/version-1\"\n"); //$NON-NLS-1$
        sb.append("\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n\n"); //$NON-NLS-1$
        sb.append("\t<jbi:services binding-component=\"false\">\n"); //$NON-NLS-1$

        sb.append("\t\t<jbi:provides\n"); //$NON-NLS-1$
        sb.append("\t\t\tinterface-name=\"generatedNs:" + jobName + "ServicePortType\"\n"); //$NON-NLS-1$ //$NON-NLS-2$

        // VZ: append the job version to the service's name
        sb.append("\t\t\tservice-name=\"generatedNs:" + jobName + "Service_" + jobVersion + "\"\n"); //$NON-NLS-1$ //$NON-NLS-2$
        // VZ

        sb.append("\t\t\tendpoint-name=\""); //$NON-NLS-1$

        // VZ: add the job's version in the end-point name
        String endpoint = genEndpoint ? "autogenerate" : (jobName + "_" + jobVersion + "_Endpoint"); //$NON-NLS-1$ //$NON-NLS-2$
        // VZ

        sb.append(endpoint + "\"\n"); //$NON-NLS-1$

        // VZ: append the job's name to the TNS
        sb.append("\t\t\txmlns:generatedNs=\"" //$NON-NLS-1$
                + SU_NAMESPACE_URI_PREFIX + jobName + "/\">\n\n"); //$NON-NLS-1$
        // VZ

        // SU parameters
        for (Map.Entry<String, String> entry : elementNameToValue.entrySet()) {
            sb.append("\t\t\t<" + entry.getKey() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append(entry.getValue());
            sb.append("</" + entry.getKey() + ">\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Extra SU parameters: the output attachments the SE will query
        for (ContextTypeDefinition def : PetalsTemporaryOptionsKeeper.INSTANCE.getContexts()) {
            if (def.getExportType() != ContextExportType.OUT_ATTACHMENT
                    && def.getExportType() != ContextExportType.PARAMETER_AND_OUT_ATTACHMENT)
                continue;

            sb.append("\t\t\t<talend:output-attachment>"); //$NON-NLS-1$
            sb.append(def.getDefinition().getName());
            sb.append("</talend:output-attachment>\n"); //$NON-NLS-1$
        }

        sb.append("\t\t</jbi:provides>\n"); //$NON-NLS-1$
        sb.append("\t</jbi:services>\n"); //$NON-NLS-1$
        sb.append("</jbi:jbi>"); //$NON-NLS-1$

        // Create the jbi.xml file
        File f = null;
        try {
            File jobFolder = new File(getTmpFolder() + PATH_SEPARATOR + jobName);
            if (!jobFolder.exists() && !jobFolder.mkdir())
                throw new IOException("Could not create the directory to store the job resources."); //$NON-NLS-1$

            f = new File(jobFolder, "jbi.xml"); //$NON-NLS-1$
            if (!f.exists() && !f.createNewFile())
                throw new IOException("Could not create temporary jbi.xml file during an export operation for PEtALS."); //$NON-NLS-1$

            ByteArrayInputStream in = new ByteArrayInputStream(sb.toString().getBytes());
            FileOutputStream out = new FileOutputStream(f);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);
            out.close();

        } catch (IOException e) {
            ExceptionHandler.process(e);
        }

        // Create the ExportFileResource
        ExportFileResource metaInfResource = new ExportFileResource(item, "META-INF"); //$NON-NLS-1$
        List<URL> urlList = new ArrayList<URL>();
        try {
            if (f != null)
                urlList.add(f.toURL());

        } catch (MalformedURLException e) {
            ExceptionHandler.process(e);
        }

        metaInfResource.addResources(urlList);
        return metaInfResource;
    }

    /**
     * Generates the WSDL file for the exported job.
     * 
     * @param processItem
     * @return
     */
    private ExportFileResource generateWsdlFile(ProcessItem item) {

        // Prepare the parameters
        String jobName = item.getProperty().getLabel();

        // VZ
        String jobVersion = item.getProperty().getVersion();
        // VZ

        // Prepare the parameters for the generation
        PetalsWsdlBean wsdlBean = new PetalsWsdlBean();
        wsdlBean.setJobName(jobName);

        // VZ
        wsdlBean.setJobVersion(jobVersion);
        // VZ

        wsdlBean.setContextDefinitions(PetalsTemporaryOptionsKeeper.INSTANCE.getContexts());
        wsdlBean.setAutogenerate(PetalsTemporaryOptionsKeeper.INSTANCE.isGenerateEndpoint());

        IStructuredSelection selection = PetalsTemporaryOptionsKeeper.INSTANCE.getSelection();
        List<NodeType> petalsComponents = TalendUtils.getTalendComponentsFromSelectedJob(selection, "tPetalsInput"); //$NON-NLS-1$
        if (petalsComponents.size() > 0) {
            List<ElementTypeDefinition> defs;
            try {
                defs = TalendUtils.getWsdlSchemaForComponent(petalsComponents.get(0));

            } catch (PetalsExportException e) {
                ExceptionHandler.process(e);
                defs = Collections.emptyList();
            }
            wsdlBean.settPetalsInputSchema(defs);
        }

        petalsComponents = TalendUtils.getTalendComponentsFromSelectedJob(selection, "tPetalsOutput"); //$NON-NLS-1$
        if (petalsComponents.size() > 0) {
            List<ElementTypeDefinition> defs;
            try {
                defs = TalendUtils.getWsdlSchemaForComponent(petalsComponents.get(0));

            } catch (PetalsExportException e) {
                ExceptionHandler.process(e);
                defs = Collections.emptyList();
            }
            wsdlBean.settPetalsOutputSchema(defs);
        }

        // Go on...
        File f = null;
        try {
            File jobFolder = new File(getTmpFolder() + PATH_SEPARATOR + jobName);
            if (!jobFolder.exists() && !jobFolder.mkdir())
                throw new IOException("Could not create the directory to store the job resources."); //$NON-NLS-1$

            f = new File(jobFolder, jobName + ".wsdl"); //$NON-NLS-1$
            if (!f.exists() && !f.createNewFile())
                throw new IOException("Could not create temporary WSDL file during an export operation for PEtALS."); //$NON-NLS-1$

            String content = new WsdlGenerator().generate(wsdlBean);
            ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
            FileOutputStream out = new FileOutputStream(f);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);
            out.close();

        } catch (IOException e) {
            ExceptionHandler.process(e);
        }

        // Create the ExportFileResource
        ExportFileResource metaInfResource = new ExportFileResource(item, null);
        List<URL> urlList = new ArrayList<URL>();
        try {
            if (f != null)
                urlList.add(f.toURL());

        } catch (MalformedURLException e) {
            ExceptionHandler.process(e);
        }

        metaInfResource.addResources(urlList);
        return metaInfResource;
    }

    /**
     * Puts the job contexts into a JAR.
     * <p>
     * Without this, the contexts are not found by the job when running into Petals.
     * </p>
     * 
     * @param item
     * @param selectedJobVersion
     * @return
     * @deprecated
     */
    @Deprecated
    private ExportFileResource jarContexts(ProcessItem item, String selectedJobVersion) {

        // Prepare the parameters
        String jobName = item.getProperty().getLabel();
        ExportFileResource contextResource = new ExportFileResource(item, null);
        addContextScripts(contextResource, selectedJobVersion, NEED_CONTEXT);

        // Prepare the files
        File jarFile = null;
        try {
            File jobFolder = new File(getTmpFolder() + PATH_SEPARATOR + jobName);
            if (!jobFolder.exists() && !jobFolder.mkdir())
                throw new IOException("Could not create the directory to store the job resources."); //$NON-NLS-1$

            jarFile = new File(jobFolder, "contexts.jar"); //$NON-NLS-1$
            FileOutputStream stream = new FileOutputStream(jarFile);
            JarOutputStream out = new JarOutputStream(stream, new Manifest());
            byte buffer[] = new byte[10240];

            Set<String> paths = contextResource.getRelativePathList();
            for (String relativePath : paths) {

                Set<URL> resource = contextResource.getResourcesByRelativePath(relativePath);
                for (URL url : resource) {
                    String filePath = url.getPath();
                    File toBeJared = new File(filePath);

                    JarEntry jarAdd = new JarEntry(relativePath + "/" + toBeJared.getName()); //$NON-NLS-1$
                    jarAdd.setTime(toBeJared.lastModified());
                    out.putNextEntry(jarAdd);

                    FileInputStream in = new FileInputStream(toBeJared);
                    int nRead;
                    while ((nRead = in.read(buffer, 0, buffer.length)) > 0)
                        out.write(buffer, 0, nRead);
                    in.close();
                }
            }

            out.close();
            stream.close();

        } catch (FileNotFoundException e) {
            ExceptionHandler.process(e);

        } catch (IOException e) {
            ExceptionHandler.process(e);
        }

        // Create the ExportFileResource
        ExportFileResource jaredContextResource = new ExportFileResource(item, null);
        List<URL> urlList = new ArrayList<URL>();
        try {
            if (jarFile != null)
                urlList.add(jarFile.toURL());

        } catch (MalformedURLException e) {
            ExceptionHandler.process(e);
        }

        jaredContextResource.addResources(urlList);
        return jaredContextResource;
    }
}
