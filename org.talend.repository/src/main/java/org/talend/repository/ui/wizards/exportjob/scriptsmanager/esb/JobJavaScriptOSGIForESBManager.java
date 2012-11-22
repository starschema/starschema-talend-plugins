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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IOsgiDependenciesService;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.designer.runprocess.LastGenerationInfo;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobJavaScriptsManager;
import org.talend.repository.utils.EmfModelUtils;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class JobJavaScriptOSGIForESBManager extends JobJavaScriptsManager {

    public JobJavaScriptOSGIForESBManager(Map<ExportChoice, Object> exportChoiceMap, String contextName, String launcher,
            int statisticPort, int tracePort) {
        super(exportChoiceMap, contextName, launcher, statisticPort, tracePort);
    }

    private static final String PACKAGE_SEPARATOR = ".";

    private static final String JAVA = "java";

    private static final String ROUTE = "route";

    private static final String JOB = "job";

    private static Logger logger = Logger.getLogger(JobJavaScriptOSGIForESBManager.class);

    private static final String OSGI_INF = "OSGI-INF"; //$NON-NLS-1$

    private static final String BLUEPRINT = "blueprint"; //$NON-NLS-1$

    private static final String META_INF = "META-INF"; //$NON-NLS-1$

    private static final String SPRING = "spring"; //$NON-NLS-1$

	private static final String ROUTE_RESOURCES = "route_resources";

    private String jobName;

    private String jobClassName;

    private String jobVersion;

    private String itemType = null;

    public List<ExportFileResource> getExportResources(ExportFileResource[] process, String... codeOptions)
            throws ProcessorException {
        List<ExportFileResource> list = new ArrayList<ExportFileResource>();

        boolean needJob = true;
        ExportFileResource libResource = new ExportFileResource(null, LIBRARY_FOLDER_NAME); //$NON-NLS-1$
        ExportFileResource osgiResource = getOsgiResource();
        ExportFileResource jobScriptResource = new ExportFileResource(null, ""); //$NON-NLS-1$

        List<ProcessItem> itemToBeExport = new ArrayList<ProcessItem>();

        if (needJob) {
            list.add(libResource);
        }
        list.add(osgiResource);
        list.add(jobScriptResource);

        // set export config mode now only to be sure that the libraries will be
        // setup for an export mode, and not
        // editor mode.
        ProcessorUtilities.setExportConfig(JAVA, "", ""); //$NON-NLS-1$

        // Gets talend libraries

        Set<String> neededLibraries = null;
        Set<ModuleNeeded> neededModules = null;
        for (int i = 0; i < process.length; i++) {
            ProcessItem processItem = (ProcessItem) process[i].getItem();
            itemToBeExport.add(processItem);
            jobName = processItem.getProperty().getLabel();
            jobClassName = getPackageName(processItem) + PACKAGE_SEPARATOR + jobName;

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
            boolean esbJob = JOB.equals(itemType) && isESBJob(processItem);
            boolean restJob = JOB.equals(itemType) && isRESTProviderJob(processItem);

            // generate the source files
            String libPath = calculateLibraryPathFromDirectory(process[i].getDirectoryName());
            // use character @ as temporary classpath separator, this one will
            // be replaced during the export.
            String standardJars = libPath + PATH_SEPARATOR + SYSTEMROUTINE_JAR + ProcessorUtilities.TEMP_JAVA_CLASSPATH_SEPARATOR
                    + libPath + PATH_SEPARATOR + USERROUTINE_JAR + ProcessorUtilities.TEMP_JAVA_CLASSPATH_SEPARATOR
                    + PACKAGE_SEPARATOR; //$NON-NLS-1$

            ProcessorUtilities.setExportConfig(JAVA, standardJars, libPath); //$NON-NLS-1$

            if (!isOptionChoosed(ExportChoice.doNotCompileCode)) {
                if (neededLibraries == null) {
                    neededLibraries = new HashSet<String>();
                }
                if (neededModules == null) {
                    neededModules = new HashSet<ModuleNeeded>();
                }
                generateJobFiles(processItem, contextName, jobVersion, statisticPort != IProcessor.NO_STATISTICS,
                        tracePort != IProcessor.NO_TRACES, isOptionChoosed(ExportChoice.applyToChildren),
                        true /* isExportAsOSGI */, progressMonitor);
                neededModules = LastGenerationInfo.getInstance().getModulesNeededWithSubjobPerJob(
                        processItem.getProperty().getId(), jobVersion);
                for (ModuleNeeded module : neededModules) {
                    if (module.getBundleName() == null) { // if no bundle defined for this, add to the jars to export
                        neededLibraries.add(module.getModuleName());
                    }
                }
            } else {
                LastGenerationInfo.getInstance().setModulesNeededWithSubjobPerJob(processItem.getProperty().getId(),
                        processItem.getProperty().getVersion(), neededModules);
                LastGenerationInfo.getInstance().setLastMainJob(null);
            }

            // generate jar file for job
            getJobScriptsUncompressed(jobScriptResource, processItem);

            // dynamic db xml mapping
            addXmlMapping(process[i], isOptionChoosed(ExportChoice.needSourceCode));

            if (restJob) {
                List<String> restSpringFiles = generateRestJobSpringFiles(processItem);
                osgiResource.addResources(getMetaInfSpringFolder(), buildUrlList(restSpringFiles));
            } else {
                List<String> esbFiles = generateESBFiles(processItem, esbJob);
                osgiResource.addResources(getOSGIInfFolder(), buildUrlList(esbFiles));
            }

			// Add Route Resource http://jira.talendforge.org/browse/TESB-6227
			if (ROUTE.equals(itemType)) {
				addOSGIRouteResources(osgiResource, processItem);
			}

        }

        // Gets talend libraries
        List<URL> talendLibraries = getExternalLibraries(true, process, neededLibraries);
        libResource.addResources(talendLibraries);

        // Gets system routines
        List<URL> systemRoutineList = getSystemRoutine(process, true);
        libResource.addResources(systemRoutineList);
        // Gets user routines
        List<URL> userRoutineList = getUserRoutine(process, true);
        libResource.addResources(userRoutineList);

        // generate the META-INFO folder
        ExportFileResource metaInfoFolder = genMetaInfoFolder(libResource, itemToBeExport);
        list.add(0, metaInfoFolder);

        return list;
    }

	/**
	 * Get all route resource needed.
	 * 
	 * @param osgiResource
	 * @param processItem
	 */
	private void addOSGIRouteResources(ExportFileResource osgiResource,
			ProcessItem processItem) {

		ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister
				.getDefault().getService(ICamelDesignerCoreService.class);
		List<IPath> paths = camelService.synchronizeRouteResource(processItem);

		for (IPath path : paths) {
			IPath relativePath = path.removeLastSegments(1);
			String pathStr = relativePath.toString();
			int index = pathStr.indexOf(ROUTE_RESOURCES);
			if (index > -1) {
				pathStr = pathStr.substring(index, pathStr.length());
				try {
					URL url = path.toFile().toURI().toURL();
					osgiResource.addResource(pathStr, url);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private List<URL> buildUrlList(List<String> files) {
        List<URL> urlList = new ArrayList<URL>();
        try {
            for (String file : files) {
                urlList.add(new File(file).toURL());
            }
        } catch (MalformedURLException e) {
            ExceptionHandler.process(e);
            logger.error(e);
        }
        return urlList;
    }

    /**
     * This method will return <code>true</code> if given job contains tESBProviderRequest or tESBConsumer component
     *
     * @param processItem
     * @author rzubairov
     * @return
     */
    private boolean isESBJob(ProcessItem processItem) {
    	return null != EmfModelUtils.getComponentByName(processItem, "tESBProviderRequest", "tESBConsumer");
    }

    private boolean isESBProviderJob(ProcessItem processItem) {
    	return null != EmfModelUtils.getComponentByName(processItem, "tESBProviderRequest");
    }

    private boolean isRESTProviderJob(ProcessItem processItem) {
        return null != getRESTRequestComponent(processItem);
    }
    
    private boolean isRESTClientJob(ProcessItem processItem) {
        return null != EmfModelUtils.getComponentByName(processItem, "tRESTClient");
    }    
    
    private NodeType getRESTRequestComponent(ProcessItem processItem) {
    	return EmfModelUtils.getComponentByName(processItem, "tRESTRequest");
    }

    protected ExportFileResource getOsgiResource() {
        return new ExportFileResource(null, ""); //$NON-NLS-1$;
    }

    private String getPackageName(ProcessItem processItem) {
        return JavaResourcesHelper.getProjectFolderName(processItem)
                + PACKAGE_SEPARATOR
                + JavaResourcesHelper.getJobFolderName(processItem.getProperty().getLabel(), processItem.getProperty()
                        .getVersion());
    }

    protected List<String> generateESBFiles(ProcessItem processItem, boolean isESBJob) {
        List<String> files = new ArrayList<String>();
        try {
            if (itemType == null) {
                itemType = JOB;
            }

            String inputFile = getPluginResourceUri("resources/" + itemType + "-template.xml"); //$NON-NLS-1$ //$NON-NLS-2$
            String targetFile = getTmpFolder() + PATH_SEPARATOR + "job.xml"; //$NON-NLS-1$

            createJobBundleBlueprintConfig(processItem, inputFile, targetFile, jobName, jobClassName, itemType, isESBJob);

            files.add(targetFile);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
        return files;
    }

    protected List<String> generateRestJobSpringFiles(ProcessItem processItem) {
        List<String> files = new ArrayList<String>();
        try {
            String inputFile = getPluginResourceUri("resources/job-rest-beans-template.xml"); //$NON-NLS-1$
            String targetFile = getTmpFolder() + PATH_SEPARATOR + "beans.xml"; //$NON-NLS-1$

            createRestJobBundleSpringConfig(processItem, inputFile, targetFile, jobName, jobClassName);

            files.add(targetFile);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
        return files;
    }

    private String getPluginResourceUri(String resourcePath) throws IOException {
        final Bundle b = Platform.getBundle(RepositoryPlugin.PLUGIN_ID);
        return FileLocator.toFileURL(FileLocator.find(b, new Path(resourcePath), null)).getFile();
    }

    private void createRestJobBundleSpringConfig(ProcessItem processItem, String inputFile,
            String targetFile, String jobName, String jobClassName) {

        NodeType restRequestComponent = getRESTRequestComponent(processItem);

        String endpointUri = EmfModelUtils.computeTextElementValue("REST_ENDPOINT", restRequestComponent);
        if (!endpointUri.isEmpty() && !endpointUri.contains("://") && !endpointUri.startsWith("/")) {
            endpointUri = "/" + endpointUri;
        }
        if (endpointUri.contains("://")) {
            try {
                endpointUri = new URL(endpointUri).getPath();
            } catch (MalformedURLException e) {
                logger.error("Endpoint URI invalid: " + e);
            }
        }
        if (endpointUri.equals("/services/") || endpointUri.equals("/services")) {
            // pass as is
        } else if (endpointUri.startsWith("/services/")) {
            // remove forwarding "/services/" context as required by runtime
            endpointUri = endpointUri.substring("/services/".length() - 1); // leave forwarding slash
        }


        String jaxrsServiceProviders = "";
        String additionalBeansConfig = "";
        String additionalJobBeanParams = "";
        boolean useHttpBasicAuth = EmfModelUtils.computeCheckElementValue("HTTP_BASIC_AUTH", restRequestComponent);
        if (useHttpBasicAuth) {
            jaxrsServiceProviders = "<ref bean=\"authenticationFilter\"/>";
            additionalBeansConfig = "\t<bean id=\"authenticationFilter\" class=\"org.apache.cxf.jaxrs.security.JAASAuthenticationFilter\">"
                    + "\n\t\t<property name=\"contextName\" value=\"karaf\"/>\n\t</bean>";
        }
        // OSGi DataSource
        DataSourceConfig dataSourceConfig = new DataSourceConfig(processItem);
        additionalJobBeanParams += dataSourceConfig.getAdditionalJobBeanParams();
        additionalBeansConfig += dataSourceConfig.getAdditionalJobBundleConfig();

        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new FileReader(inputFile));
            bw = new BufferedWriter(new FileWriter(targetFile));

            String line = br.readLine();
            while (line != null) {
                line = line.replace("@ENDPOINT_URI@", endpointUri) //$NON-NLS-1$
                        .replace("@JOBNAME@", jobName) //$NON-NLS-1$
                        .replace("@JOBCLASSNAME@", jobClassName) //$NON-NLS-1$
                        .replace("@JAXRS_SERVICE_PROVIDERS@", jaxrsServiceProviders) //$NON-NLS-1$
                        .replace("@ADDITIONAL_BEANS_CONFIG@", additionalBeansConfig) //$NON-NLS-1$
                        .replace("@ADDITIONAL_JOB_BEAN_PARAMS@", additionalJobBeanParams); //$NON-NLS-1$

                bw.write(line);
                bw.newLine();
                line = br.readLine();
            }
            bw.flush();
        } catch (IOException e) {
            ExceptionHandler.process(e);
            logger.error(e);
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
            if (null != bw) {
                try {
                    bw.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Created OSGi Blueprint configuration for job bundle.
     *
     * @param processItem
     * @param inputFile
     * @param targetFile
     * @param jobName
     * @param jobClassName
     * @param itemType
     * @param isESBJob
     */
    private void createJobBundleBlueprintConfig(ProcessItem processItem, String inputFile, String targetFile, String jobName,
            String jobClassName, String itemType, boolean isESBJob) {

        // http://jira.talendforge.org/browse/TESB-3677
        boolean hasSAM = false;

        if (ROUTE.equals(itemType)) {
        	for (NodeType node : EmfModelUtils.getComponentsByName(processItem, "cCXF")) {
                // http://jira.talendforge.org/browse/TESB-3850
                String format = EmfModelUtils.computeTextElementValue("DATAFORMAT", node); //$NON-NLS-1$
                if (!"MESSAGE".equals(format)) { //$NON-NLS-1$
                    hasSAM = EmfModelUtils.computeCheckElementValue("ENABLE_SAM", node); //$NON-NLS-1$
                    if (hasSAM) {
                    	break;
                    }
                }
			}
        }

        String additionalJobInterfaces = "";
        String additionalServiceProps = "";
        String additionalJobBundleConfig = "";
        String additionalJobBeanParams = "";
        if (isESBJob) {
            additionalJobInterfaces = "<value>routines.system.api.TalendESBJob</value>"; //$NON-NLS-1$
            if (isESBProviderJob(processItem)) {
                additionalJobInterfaces += "\n\t\t\t<value>routines.system.api.TalendESBJobFactory</value>"; //$NON-NLS-1$
                additionalServiceProps = "<entry key=\"multithreading\" value=\"true\" />"; //$NON-NLS-1$
            }
        }
        // OSGi DataSource
        DataSourceConfig dataSourceConfig = new DataSourceConfig(processItem);
        additionalJobBeanParams += dataSourceConfig.getAdditionalJobBeanParams();
        additionalJobBundleConfig += dataSourceConfig.getAdditionalJobBundleConfig();

        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new FileReader(inputFile));
            bw = new BufferedWriter(new FileWriter(targetFile));

            String line = br.readLine();
            while (line != null) {
                line = line.replace("@JOBNAME@", jobName) //$NON-NLS-1$
                        .replace("@TYPE@", itemType) //$NON-NLS-1$
                        .replace("@JOBCLASSNAME@", jobClassName) //$NON-NLS-1$
                        .replace("@ADDITIONAL_JOB_INTERFACE@", additionalJobInterfaces) //$NON-NLS-1$
                        .replace("@ADDITIONAL_JOB_BEAN_PARAMS@", additionalJobBeanParams) //$NON-NLS-1$
                        .replace("@ADDITIONAL_JOB_BUNDLE_CONFIG@", additionalJobBundleConfig) //$NON-NLS-1$
                        .replace("@ADDITIONAL_SERVICE_PROPERTIES@", additionalServiceProps); //$NON-NLS-1$

                // SAM
                line = line.replace("@ESBSAMFeatureProperty@", //$NON-NLS-1$
                        (hasSAM) ? "<property name=\"eventFeature\" ref=\"eventFeature\"/>" : "");
                line = line.replace("@ESBSAMFeaturePropertyRef@", //$NON-NLS-1$
                        (hasSAM) ? "<reference id=\"eventFeature\" interface=\"org.talend.esb.sam.agent.feature.EventFeature\"/>"
                                : "");

                bw.write(line);
                bw.newLine();
                line = br.readLine();
            }
            bw.flush();
        } catch (IOException e) {
            ExceptionHandler.process(e);
            logger.error(e);
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
            if (null != bw) {
                try {
                    bw.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private String getOSGIInfFolder() {
        return OSGI_INF.concat(PATH_SEPARATOR).concat(BLUEPRINT);
    }

    private String getMetaInfSpringFolder() {
        return META_INF.concat(PATH_SEPARATOR).concat(SPRING);
    }

    protected void readAndReplaceInXmlTemplate(String inputFile, String outputFile, String jobName, String jobClassName,
            String itemType, boolean isESBJob) {
        FileReader fr = null;
        String additionalServiceProps = "";
        String additionalJobInterfaces = "<value>routines.system.api.TalendESBJob</value>"; //$NON-NLS-1$
        if (!isESBJob) {
            additionalJobInterfaces = "";
        }
        try {
            fr = new FileReader(inputFile);
            BufferedReader br = new BufferedReader(fr);

            FileWriter fw = new FileWriter(outputFile);
            BufferedWriter bw = new BufferedWriter(fw);

            String line = br.readLine();
            while (line != null) {
                line = line.replace("@JOBNAME@", jobName) //$NON-NLS-1$
                        .replace("@TYPE@", itemType) //$NON-NLS-1$
                        .replace("@JOBCLASSNAME@", jobClassName) //$NON-NLS-1$
                        .replace("@ADDITIONAL_JOB_INTERFACE@", additionalJobInterfaces) //$NON-NLS-1$
                        .replace("@ADDITIONAL_SERVICE_PROPERTIES@", additionalServiceProps); //$NON-NLS-1$
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

    protected ExportFileResource genMetaInfoFolder(ExportFileResource libResource, List<ProcessItem> itemToBeExport) {
        ExportFileResource metaInfoResource = new ExportFileResource(null, META_INF);

        // generate the MANIFEST.MF file in the temp folder
        String manifestPath = getTmpFolder() + PATH_SEPARATOR + "MANIFEST.MF"; //$NON-NLS-1$

        FileOutputStream fos = null;
        try {
            Manifest manifest = getManifest(libResource, itemToBeExport, jobName);
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

    protected Manifest getManifest(ExportFileResource libResource, List<ProcessItem> itemToBeExport, String bundleName)
            throws IOException {
        Manifest manifest = new Manifest();
        Attributes a = manifest.getMainAttributes();

        // http://jira.talendforge.org/browse/TESB-5382 LiXiaopeng
        String symbolicName = bundleName;
        Project project = ProjectManager.getInstance().getCurrentProject();
        if (project != null) {
            String proName = project.getLabel();
            if (proName != null) {
                symbolicName = proName.toLowerCase() + "." + symbolicName;
            }
        }
        a.put(Attributes.Name.MANIFEST_VERSION, "1.0"); //$NON-NLS-1$
        a.put(new Attributes.Name("Bundle-Name"), bundleName); //$NON-NLS-1$
        a.put(new Attributes.Name("Bundle-SymbolicName"), symbolicName); //$NON-NLS-1$
        a.put(new Attributes.Name("Bundle-Version"), getBundleVersion()); //$NON-NLS-1$
        a.put(new Attributes.Name("Bundle-ManifestVersion"), "2"); //$NON-NLS-1$ //$NON-NLS-2$
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        a.put(new Attributes.Name("Created-By"), brandingService.getFullProductName() + " (" + brandingService.getAcronym() + "_"
                + RepositoryPlugin.getDefault().getBundle().getVersion().toString() + ")");
        StringBuilder sb = new StringBuilder();
        String delim = "";
        for (ProcessItem pi : itemToBeExport) {
            sb.append(delim).append(getPackageName(pi));
            delim = ",";
			// Add Route Resource Export packages
			// http://jira.talendforge.org/browse/TESB-6227
			if (ROUTE.equals(itemType)) {
				String routeResourcePackages = addRouteResourcePackages(pi);
				if (!routeResourcePackages.isEmpty()) {
					sb.append(delim).append(routeResourcePackages);
				}
			}
        }

        a.put(new Attributes.Name("Export-Package"), sb.toString()); //$NON-NLS-1$

        if (ROUTE.equals(itemType)) {
        	addRouterOsgiDependencies(libResource, itemToBeExport, a);
        } else {
        	String additionalImports = ""; //$NON-NLS-1$
        	for (ProcessItem processItem : itemToBeExport) {
				if (DataSourceConfig.isDBConnectionJob(processItem)) {
					additionalImports = ",org.apache.commons.dbcp.datasources";
				}
			}
            a.put(new Attributes.Name("Import-Package"), //$NON-NLS-1$
                    "routines.system.api;resolution:=optional" //$NON-NLS-1$
                            + ",javax.xml.soap;resolution:=optional" //$NON-NLS-1$
                            + ",javax.xml.ws.soap;resolution:=optional" //$NON-NLS-1$
                            + ",org.apache.cxf.management.counters" //$NON-NLS-1$
                            + additionalImports
            );
            if (itemToBeExport != null && !itemToBeExport.isEmpty()) {
            	for (ProcessItem pi : itemToBeExport) {
            		/*
            		 * need to fill bundle depedence informations for every component,feature 0023460
            		 */
            		String requiredBundles = caculateDependenciesBundles(pi);
            		requiredBundles = addAdditionalRequiredBundles(pi, requiredBundles);
            		if (requiredBundles != null && !"".equals(requiredBundles)) {
            			a.put(new Attributes.Name("Require-Bundle"), requiredBundles);
            		}
            	}
            }
            if (!libResource.getAllResources().isEmpty()) {
            	a.put(new Attributes.Name("Bundle-ClassPath"), getClassPath(libResource)); //$NON-NLS-1$
            }
        }
        a.put(new Attributes.Name("Export-Service"), "routines.system.api.TalendJob;name=" + bundleName + ";type=" + itemType); //$NON-NLS-1$

        return manifest;
    }
    
	/**
	 * Add route resource packages.
	 */
	private String addRouteResourcePackages(ProcessItem item) {
		Set<String> pkgs = new HashSet<String>();
		EMap additionalProperties = item.getProperty()
				.getAdditionalProperties();
		if (additionalProperties == null) {
			return "";
		}
		Object resourcesObj = additionalProperties.get("ROUTE_RESOURCES_PROP");
		if (resourcesObj == null) {
			return "";
		}

		String[] resourceIds = resourcesObj.toString().split(",");
		String exportPkg = "";
		for (String id : resourceIds) {
			try {
				IRepositoryViewObject rvo = ProxyRepositoryFactory
						.getInstance().getLastVersion(id);
				if (rvo != null) {

					Item it = rvo.getProperty().getItem();
					String path = it.getState().getPath();
					if (path != null && !path.isEmpty()) {
						exportPkg = "route_resources." + path.replace("/", ".");
					} else {
						exportPkg = "route_resources";
					}
					pkgs.add(exportPkg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return StringUtils.join(pkgs.toArray(), ",");
	}

    private String addAdditionalRequiredBundles(ProcessItem pi, String requiredBundles) {
    	if (isRESTClientJob(pi) || isRESTProviderJob(pi)) {
    		String bundlesToAdd = "org.apache.cxf.cxf-rt-frontend-jaxrs" 
    							+ ",org.apache.cxf.cxf-rt-rs-extension-providers";
    		// check if we need add ',' after already existing bundles
    		requiredBundles = (requiredBundles != null && !"".equals(requiredBundles)) ? requiredBundles + "," : "";
   			requiredBundles = requiredBundles + bundlesToAdd;
    	}
    	
    	return requiredBundles;
    }

    private void addRouterOsgiDependencies(ExportFileResource libResource,
    		List<ProcessItem> itemToBeExport, Attributes a) {
    	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    	IProject project = root.getProject(JavaUtils.JAVA_PROJECT_NAME);
    	IPath libPath = project.getLocation().append(JavaUtils.JAVA_LIB_DIRECTORY);
    	for(ProcessItem pi: itemToBeExport){
    		IOsgiDependenciesService dependenciesService = (IOsgiDependenciesService) GlobalServiceRegister.getDefault().getService(
    				IOsgiDependenciesService.class);
    		if(dependenciesService!=null){
    			Map<String, String> bundleDependences = dependenciesService.getBundleDependences(pi, pi.getProperty().getAdditionalProperties());
    			//process external libs
    			String externalLibs = bundleDependences.get(IOsgiDependenciesService.BUNDLE_CLASSPATH);
    			String[] libs = externalLibs.split(IOsgiDependenciesService.ITEM_SEPARATOR);
    			Set<URL> list = new HashSet<URL>();
    			for(String s: libs){
    				try {
    					if(s.isEmpty()){
    						continue;
    					}
    					IPath path = libPath.append(s);
						URL url = path.toFile().toURL();
						list.add(url);
					} catch (MalformedURLException e) {
						e.printStackTrace();
						continue;
					}
    			}
    			libResource.addResources(new ArrayList<URL>(list));
    			
    			// add manifest items
    			String requireBundles = bundleDependences.get(IOsgiDependenciesService.REQUIRE_BUNDLE);
    			if(requireBundles!=null && !"".equals(requireBundles)){
    				a.put(new Attributes.Name("Require-Bundle"), requireBundles);
    			}
    			String importPackages = bundleDependences.get(IOsgiDependenciesService.IMPORT_PACKAGE);
    			if(importPackages!=null && !"".equals(importPackages)){
    				a.put(new Attributes.Name("Import-Package"), importPackages);
    			}
    			if (!libResource.getAllResources().isEmpty()) {
    				a.put(new Attributes.Name("Bundle-ClassPath"), getClassPath(libResource)); //$NON-NLS-1$
    			}
    		}

    	}
    }

    /**
     * DOC hywang Comment method "caculateDependenciesBundles".
     *
     * @return
     */
    private String caculateDependenciesBundles(ProcessItem processItem) {
        StringBuffer requiredBundles = new StringBuffer();
        // this list is used to avoid add dumplicated bundle
        List<String> alreadyAddedBundles = new ArrayList<String>();

        List<String> segments = new ArrayList<String>();
        Set<ModuleNeeded> neededModules = LastGenerationInfo.getInstance().getModulesNeededWithSubjobPerJob(
                processItem.getProperty().getId(), jobVersion);

        generateBundleSegments(neededModules, alreadyAddedBundles, segments);
        int index = 0;
        for (String segment : segments) {
            if (index != segments.size() - 1) {
                segment = segment + ",";
            }
            requiredBundles.append(segment);
            index++;
        }
        segments = null;
        alreadyAddedBundles = null;
        return requiredBundles.toString();
    }

    protected void generateBundleSegments(Set<ModuleNeeded> neededModules, List<String> alreadyAddedBundles, List<String> segments) {
        for (ModuleNeeded module : neededModules) {
            String bundleName = module.getBundleName();
            String bundleVersion = module.getBundleVersion();
            // the last dependence should not contain "," and "\n"
            String bundleToAdd = bundleName;
            if (bundleVersion != null && !"".equals(bundleVersion)) {
                bundleToAdd = bundleName + ";bundle-version=" + TalendTextUtils.addQuotes(bundleVersion);
            }

            if (bundleToAdd != null && !"".equals(bundleToAdd)) {
                if (!alreadyAddedBundles.contains(bundleToAdd)) {
                    segments.add(bundleToAdd);
                    alreadyAddedBundles.add(bundleToAdd);
                }
            }
        }
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
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject prj = root.getProject(JavaUtils.JAVA_PROJECT_NAME);
        IJavaProject project = JavaCore.create(prj);
        IPath libPath = project.getResource().getLocation().append(JavaUtils.JAVA_LIB_DIRECTORY);
        File file = libPath.toFile();
        File[] files = file.listFiles(FilesUtils.getAcceptModuleFilesFilter());

        if (!useBeans) {
            // Gets all the jar files
            if (neededLibraries == null) {
                // in case export as been done with option "not recompile", then
                // libraires can't be retrieved when
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

    @Override
    public void setTopFolder(List<ExportFileResource> resourcesToExport) {
        return;
    }

    @Override
    public String getOutputSuffix() {
        return ".jar";
    }

}
