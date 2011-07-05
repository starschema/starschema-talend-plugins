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
package org.talend.repository.ui.wizards.exportjob.scriptsmanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.BooleanUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.PerlResourcesHelper;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.runprocess.LastGenerationInfo;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.constants.FileConstants;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.i18n.Messages;
import org.talend.repository.local.ExportItemUtil;
import org.talend.repository.model.ResourceModelUtils;

/**
 * Manages the job scripts to be exported. <br/>
 * 
 * $Id: JobScriptsManager.java 1 2006-12-14 下�?�05:06:49 bqian
 * 
 */
public abstract class JobScriptsManager {

    protected static final String UNIX_LAUNCHER = "run.sh"; //$NON-NLS-1$

    protected static final String WINDOWS_LAUNCHER = "run.bat"; //$NON-NLS-1$

    protected static final String LIBRARY_FOLDER_NAME = "lib"; //$NON-NLS-1$

    protected static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$

    public static final String ALL_ENVIRONMENTS = Messages.getString("JobPerlScriptsManager.allInterpreter"); //$NON-NLS-1$

    public static final String UNIX_ENVIRONMENT = "Unix"; //$NON-NLS-1$

    public static final String WINDOWS_ENVIRONMENT = "Windows"; //$NON-NLS-1$

    protected static final String JOB_SOURCE_FOLDER_NAME = "src"; //$NON-NLS-1$

    protected static final String JOB_ITEMS_FOLDER_NAME = "items"; //$NON-NLS-1$

    public static final String JOB_CONTEXT_FOLDER = "contexts"; //$NON-NLS-1$

    public static final String CTX_PARAMETER_ARG = "--context_param"; //$NON-NLS-1$

    public static final String CMDFORWIN = "%*"; //$NON-NLS-1$

    public static final String CMDFORUNIX = "$*"; //$NON-NLS-1$

    private String selectedJobVersion; //$NON-NLS-1$

    protected IProgressMonitor progressMonitor; // achen added to fix bug 0006222

    protected List<ContextParameterType> contextEditableResultValuesList;

    public List<ContextParameterType> getContextEditableResultValuesList() {
        return this.contextEditableResultValuesList;
    }

    public void setContextEditableResultValuesList(List<ContextParameterType> contextEditableResultValuesList) {
        this.contextEditableResultValuesList = contextEditableResultValuesList;
    }

    public Map<ExportChoice, Object> getDefaultExportChoiseMap() {
        Map<ExportChoice, Object> exportChoiceMap = new EnumMap<ExportChoice, Object>(ExportChoice.class);
        exportChoiceMap.put(ExportChoice.needLauncher, true);
        exportChoiceMap.put(ExportChoice.needSystemRoutine, true);
        exportChoiceMap.put(ExportChoice.needUserRoutine, true);
        exportChoiceMap.put(ExportChoice.needTalendLibraries, true);
        exportChoiceMap.put(ExportChoice.needJobItem, true);
        exportChoiceMap.put(ExportChoice.needJobScript, true);
        exportChoiceMap.put(ExportChoice.needContext, true);
        exportChoiceMap.put(ExportChoice.needSourceCode, true);
        exportChoiceMap.put(ExportChoice.applyToChildren, false);
        exportChoiceMap.put(ExportChoice.doNotCompileCode, false);
        return exportChoiceMap;
    }

    public void setProgressMonitor(IProgressMonitor progressMonitor) {
        this.progressMonitor = progressMonitor;
    }

    // bug 8720
    protected boolean isOptionChoosed(Map<ExportChoice, Object> exportChoice, Object key) {
        if (key != null) {
            final Object object = exportChoice.get(key);
            if (object instanceof Boolean) {
                return BooleanUtils.isTrue((Boolean) object);
            }
        }
        return false;
    }

    /**
     * 
     * DOC Represent exportchoice <br/>
     * .
     * 
     * $Id: JobScriptsExportWizardPage.java 1 2007-1-31下�?�06:14:19 +0000 ylv $
     * 
     */
    public enum ExportChoice {
        needMetaInfo,
        needWEBXML,
        needCONFIGFILE,
        needAXISLIB,
        needWSDD,
        needWSDL,
        needLauncher,
        needSystemRoutine,
        needUserRoutine,
        needTalendLibraries,
        needJobItem,
        needJobScript,
        needSourceCode, // only usefull for Java, as source code is job script in Perl. Activated when needJobItem is
        // selected
        needContext,
        applyToChildren,
        addStatistics, // for feature 11031
        doNotCompileCode,
        needDependencies,
        setParameterValues,
        esbQueueMessageName,
        esbServiceName,
        esbCategory,
        esbExportType
    }

    /**
     * qian Gets the export resources.
     * 
     * @param process
     * @param needLauncher
     * @param needSystemRoutine
     * @param needUserRoutine
     * @param needModel
     * @param needJob
     * @param needContext
     * @return
     */

    public abstract List<ExportFileResource> getExportResources(ExportFileResource[] process,
            Map<ExportChoice, Object> exportChoiceMap, IContext context, String launcher, int statisticPort, int tracePort,
            String... codeOptions) throws ProcessorException;

    public abstract List<ExportFileResource> getExportResources(ExportFileResource[] process,
            Map<ExportChoice, Object> exportChoiceMap, String contextName, String launcher, int statisticPort, int tracePort,
            String... codeOptions) throws ProcessorException;

    protected String getTmpFolder() {
        String tmpFold = getTmpFolderPath();
        File f = new File(tmpFold);
        if (!f.exists()) {
            f.mkdir();
        }
        return tmpFold;
    }

    private String getTmpFolderPath() {
        Project project = ProjectManager.getInstance().getCurrentProject();
        IProject physProject;
        String tmpFolder = System.getProperty("user.dir"); //$NON-NLS-1$
        try {
            physProject = ResourceModelUtils.getProject(project);
            tmpFolder = physProject.getFolder("temp").getLocation().toPortableString(); //$NON-NLS-1$
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        tmpFolder = tmpFolder + "/talendExporter"; //$NON-NLS-1$
        return tmpFolder;
    }

    /**
     * Gets the perl launcher location.
     * 
     * @return
     */
    public String[] getLauncher() {
        String[] launchers = { ALL_ENVIRONMENTS, UNIX_ENVIRONMENT, WINDOWS_ENVIRONMENT };
        return launchers;
    }

    /**
     * 
     * Create launcher(s) and get url(s).
     * 
     * @param needLauncher
     * @param process
     * @param contextName
     * @param environment use JobScriptsManager.ALL_ENVIRONMENTS, JobScriptsManager.UNIX_ENVIRONMENT or
     * JobScriptsManager.WINDOWS_ENVIRONMENT
     * @param statisticPort TODO
     * @param tracePort TODO
     * @param codeOptions TODO
     * @return
     */
    protected List<URL> getLauncher(boolean needLauncher, boolean setParameterValues, boolean needContext, IProcess process,
            ProcessItem processItem, String contextName, String environment, int statisticPort, int tracePort,
            String... codeOptions) {

        List<URL> list = new ArrayList<URL>();
        if (!needLauncher) {
            return list;
        }
        String windowsCmd;
        String unixCmd;
        if (process == null) {
            windowsCmd = getCommandByTalendJob(Platform.OS_WIN32, processItem, contextName, needContext, statisticPort,
                    tracePort, codeOptions);
            unixCmd = getCommandByTalendJob(Platform.OS_LINUX, processItem, contextName, needContext, statisticPort, tracePort,
                    codeOptions);
        } else {
            windowsCmd = getCommandByTalendJob(Platform.OS_WIN32, process, contextName, needContext, statisticPort, tracePort,
                    codeOptions);
            unixCmd = getCommandByTalendJob(Platform.OS_LINUX, process, contextName, needContext, statisticPort, tracePort,
                    codeOptions);
        }
        if (setParameterValues) {
            String contextParameterValues = getSettingContextParametersValue();
            if (windowsCmd.contains(CMDFORWIN) && windowsCmd.indexOf(CMDFORWIN) > 2) {
                windowsCmd = windowsCmd.substring(0, windowsCmd.indexOf(CMDFORWIN) - 1) + contextParameterValues + CMDFORWIN;
            }
            if (unixCmd.contains(CMDFORUNIX) && unixCmd.indexOf(CMDFORUNIX) > 2) {
                unixCmd = unixCmd.substring(0, unixCmd.indexOf(CMDFORUNIX) - 1) + contextParameterValues + CMDFORUNIX;
            }
        }

        String tmpFold = getTmpFolder();

        if (environment.equals(ALL_ENVIRONMENTS)) {
            createLauncherFile(processItem, list, unixCmd, UNIX_LAUNCHER, tmpFold);
            createLauncherFile(processItem, list, windowsCmd, WINDOWS_LAUNCHER, tmpFold);
        } else if (environment.equals(UNIX_ENVIRONMENT)) {
            createLauncherFile(processItem, list, unixCmd, UNIX_LAUNCHER, tmpFold);
        } else if (environment.equals(WINDOWS_ENVIRONMENT)) {
            createLauncherFile(processItem, list, windowsCmd, WINDOWS_LAUNCHER, tmpFold);
        }

        return list;
    }

    private String getSettingContextParametersValue() {
        String contextParameterValues = "";//$NON-NLS-1$
        List<ContextParameterType> jobContextValues = getContextEditableResultValuesList();
        for (int i = 0; i < jobContextValues.size(); i++) {
            ContextParameterType contextParameterType = jobContextValues.get(i);
            String name = contextParameterType.getName();
            String value = contextParameterType.getValue();
            // name = TalendTextUtils.removeQuotes(name);
            // value = TalendTextUtils.removeQuotes(value);
            if (value == null) {
                contextParameterValues += " " + CTX_PARAMETER_ARG + " " + name + "=" + null;//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ 
            } else if (value != null && !"".equals(value)) {//$NON-NLS-1$
                contextParameterValues += " " + CTX_PARAMETER_ARG + " " + name + "=" + value;//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            }
        }
        contextParameterValues = contextParameterValues + " ";//$NON-NLS-1$

        return contextParameterValues;
    }

    /**
     * DOC zli Comment method "getLauncher".
     * 
     * @param needLauncher
     * @param setParameterValues used for context_param to export
     * @param process
     * @param contextName
     * @param environment
     * @param statisticPort
     * @param tracePort
     * @param codeOptions
     * @return
     */
    protected List<URL> getLauncher(boolean needLauncher, boolean setParameterValues, ProcessItem process, String contextName,
            String environment, int statisticPort, int tracePort, String... codeOptions) {

        List<URL> list = new ArrayList<URL>();
        if (!needLauncher) {
            return list;
        }
        String processId = process.getProperty().getId();
        String windowsCmd = getCommandByTalendJob(Platform.OS_WIN32, processId, contextName, process.getProperty().getVersion(),
                statisticPort, tracePort, codeOptions);
        String unixCmd = getCommandByTalendJob(Platform.OS_LINUX, processId, contextName, process.getProperty().getVersion(),
                statisticPort, tracePort, codeOptions);
        if (setParameterValues) {
            String contextParameterValues = getSettingContextParametersValue();
            if (windowsCmd.contains(CMDFORWIN) && windowsCmd.indexOf(CMDFORWIN) > 2) {
                windowsCmd = windowsCmd.substring(0, windowsCmd.indexOf(CMDFORWIN) - 1) + contextParameterValues + CMDFORWIN;
            }
            if (unixCmd.contains(CMDFORUNIX) && unixCmd.indexOf(CMDFORUNIX) > 2) {
                unixCmd = unixCmd.substring(0, unixCmd.indexOf(CMDFORUNIX) - 1) + contextParameterValues + CMDFORUNIX;
            }
        }

        String tmpFold = getTmpFolder();

        if (environment.equals(ALL_ENVIRONMENTS)) {
            createLauncherFile(process, list, unixCmd, UNIX_LAUNCHER, tmpFold);
            createLauncherFile(process, list, windowsCmd, WINDOWS_LAUNCHER, tmpFold);
        } else if (environment.equals(UNIX_ENVIRONMENT)) {
            createLauncherFile(process, list, unixCmd, UNIX_LAUNCHER, tmpFold);
        } else if (environment.equals(WINDOWS_ENVIRONMENT)) {
            createLauncherFile(process, list, windowsCmd, WINDOWS_LAUNCHER, tmpFold);
        }

        return list;
    }

    /**
     * @deprecated <br>
     * Call instead the function with IProcess.<br>
     * This avoids to reload the ProcessItem another time.
     * 
     * @param targetPlatform
     * @param processId
     * @param context
     * @param processVersion
     * @param statisticPort
     * @param tracePort
     * @param codeOptions
     * @return
     */
    protected String getCommandByTalendJob(String targetPlatform, String processId, String context, String processVersion,
            int statisticPort, int tracePort, String... codeOptions) {
        String[] cmd = new String[] {};
        try {
            cmd = ProcessorUtilities.getCommandLine(targetPlatform, true, processId, context, processVersion, statisticPort,
                    tracePort, codeOptions);
        } catch (ProcessorException e) {
            ExceptionHandler.process(e);
        }
        return ProcessorUtilities.generateCmdByTalendJob(cmd);
    }

    /**
     * @deprecated <br>
     * Call instead the function with IProcess.<br>
     * This avoids to reload the ProcessItem another time.
     * 
     * @param targetPlatform
     * @param processItem
     * @param context
     * @param statisticPort
     * @param tracePort
     * @param codeOptions
     * @return
     */
    protected String getCommandByTalendJob(String targetPlatform, ProcessItem processItem, String context, boolean needContext,
            int statisticPort, int tracePort, String... codeOptions) {
        String[] cmd = new String[] {};
        try {
            cmd = ProcessorUtilities.getCommandLine(targetPlatform, true, processItem, context, needContext, statisticPort,
                    tracePort, codeOptions);
        } catch (ProcessorException e) {
            ExceptionHandler.process(e);
        }
        return ProcessorUtilities.generateCmdByTalendJob(cmd);
    }

    protected String getCommandByTalendJob(String targetPlatform, IProcess process, String context, boolean needContext,
            int statisticPort, int tracePort, String... codeOptions) {
        String[] cmd = new String[] {};
        try {
            cmd = ProcessorUtilities.getCommandLine(targetPlatform, true, process, context, needContext, statisticPort,
                    tracePort, codeOptions);
        } catch (ProcessorException e) {
            ExceptionHandler.process(e);
        }
        return ProcessorUtilities.generateCmdByTalendJob(cmd);
    }

    /**
     * DOC Administrator Comment method "createLauncherFile".
     * 
     * @param process
     * @param list
     * @param cmdPrimary
     * @param cmdSecondary
     * @param tmpFold
     */
    private void createLauncherFile(ProcessItem process, List<URL> list, String cmdPrimary, String fileName, String tmpFold) {
        PrintWriter pw = null;
        try {

            File file = new File(tmpFold, process.getProperty().getLabel() + "_" + fileName); //$NON-NLS-1$
            file.createNewFile();
            pw = new PrintWriter(new FileOutputStream(file));
            pw.print(cmdPrimary);
            pw.flush();
            list.add(file.toURL());
            pw.close();
        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally {
            try {
                if (pw != null) {
                    pw.close();
                }
            } catch (Exception e) {
                // do nothing here
            }
        }
    }

    /**
     * Deletes the temporary files.
     */
    public void deleteTempFiles() {
        String tmpFold = getTmpFolderPath();
        File file = new File(tmpFold);
        if (!file.exists() && !file.isDirectory()) {
            return;
        }
        deleteDirectory(file);
    }

    public void deleteDirectory(File dir) {
        File[] entries = dir.listFiles();
        int sz = entries.length;
        for (int i = 0; i < sz; i++) {
            if (entries[i].isDirectory()) {
                deleteDirectory(entries[i]);
            } else {
                entries[i].delete();
            }
        }
        dir.delete();
    }

    /**
     * 
     * Gets the set of current job's context.
     * 
     * @return a List of context names.
     * 
     */
    public abstract List<String> getJobContexts(ProcessItem processItem);

    /**
     * ftang Comment method "escapeFileNameSpace".
     * 
     * @param processItem
     * @return
     */
    protected String escapeFileNameSpace(ProcessItem processItem) {
        String jobName = processItem.getProperty().getLabel();
        return escapeSpace(jobName);
    }

    /**
     * ftang Comment method "escapeSpace".
     * 
     * @param name
     * @return
     */
    public String escapeSpace(String name) {
        return PerlResourcesHelper.escapeSpace(name);
    }

    /**
     * Generates the perl files.
     * 
     * @param needGenerateCode
     * @param contextName
     * @param process
     * @throws ProcessorException
     */
    protected IProcess generateJobFiles(ProcessItem process, String contextName, boolean statistics, boolean trace,
            boolean applyContextToChildren) throws ProcessorException {
        LastGenerationInfo.getInstance().getUseDynamicMap().clear();
        return ProcessorUtilities.generateCode(process, contextName, statistics, trace, applyContextToChildren).getProcess();
    }

    /**
     * Generates the job files.
     * 
     * @param needGenerateCode
     * @param context
     * @param process
     * @throws ProcessorException
     */
    protected IProcess generateJobFiles(ProcessItem process, IContext context, String version, boolean statistics, boolean trace,
            boolean applyContextToChildren, IProgressMonitor monitor) throws ProcessorException {
        LastGenerationInfo.getInstance().getUseDynamicMap().clear();
        return ProcessorUtilities.generateCode(process, context, version, statistics, trace, applyContextToChildren, monitor)
                .getProcess();
    }

    /**
     * Generates the job files.
     * 
     * @param needGenerateCode
     * @param contextName
     * @param process
     * @throws ProcessorException
     */
    protected IProcess generateJobFiles(ProcessItem process, String contextName, String version, boolean statistics,
            boolean trace, boolean applyContextToChildren, IProgressMonitor monitor) throws ProcessorException {
        return generateJobFiles(process, contextName, version, statistics, trace, applyContextToChildren, false, monitor);
    }

    protected IProcess generateJobFiles(ProcessItem process, String contextName, String version, boolean statistics,
            boolean trace, boolean applyContextToChildren, boolean isExportAsOSGI, IProgressMonitor monitor)
            throws ProcessorException {
        LastGenerationInfo.getInstance().getUseDynamicMap().clear();
        return ProcessorUtilities.generateCode(process, contextName, version, statistics, trace, applyContextToChildren,
                isExportAsOSGI, monitor).getProcess();
    }

    protected IResource[] sourceResouces = null;

    private boolean isMultiNodes;

    protected void addNodeToResource(IResource[] resources, List<IResource> sourceFile) throws CoreException {

        for (IResource resource : resources) {
            if (resource instanceof IFolder) {
                IFolder folder = (IFolder) resource;
                addNodeToResource(folder.members(), sourceFile);
            }
            if (resource instanceof IFile) {
                sourceFile.add(resource);
            }

        }
    }

    protected void addJobItem(ExportFileResource[] resources, ProcessItem processItem, boolean needChoice,
            ExportFileResource curResource, String... selectedJobVersion) {
        List<URL> list = new ArrayList<URL>();
        if (needChoice) {
            try {
                Set<File> files = new ExportItemUtil().createLocalResources(new File(getTmpFolder()), processItem);
                for (File file : files) {
                    list.add(file.toURI().toURL());
                }
                curResource.addResources(JOB_SOURCE_FOLDER_NAME, list);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
    }

    /**
     * Gets resources' URL.
     * 
     * @param resources
     * @param fileNames
     * @return
     */
    protected List<URL> getResourcesURL(IResource[] resources, List<String> fileNames) {
        List<URL> list = new ArrayList<URL>();

        for (Iterator<String> iter = fileNames.iterator(); iter.hasNext();) {
            String name = iter.next();
            for (int i = 0; i < resources.length; i++) {
                IResource resource = resources[i];

                if (resource.getName().equals(name)) {
                    try {
                        URL url = resource.getLocation().toFile().toURL();
                        list.add(FileLocator.toFileURL(url));
                        break;
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        }
        return list;
    }

    protected void addToList(List<String> list, String o) {
        if (!list.contains(o)) {
            list.add(o);
        }
    }

    /**
     * ftang Comment method "setJobVersion".
     * 
     * @param selectedJobVersion
     */
    public void setJobVersion(String selectedJobVersion) {
        this.selectedJobVersion = selectedJobVersion;
    }

    /**
     * ftang Comment method "getSelectedJobVersion".
     * 
     * @return
     */
    public String getSelectedJobVersion() {
        return this.selectedJobVersion;
    }

    /**
     * ftang Comment method "setMultiNodes".
     * 
     * @param b
     */
    public void setMultiNodes(boolean isMultiNodes) {
        this.isMultiNodes = isMultiNodes;
    }

    /**
     * ftang Comment method "setMultiNodes".
     * 
     * @param b
     */
    public boolean isMultiNodes() {
        return this.isMultiNodes;
    }

    protected IPath getEmfFileRootPath(Item item) throws Exception {
        IPath root = getCorrespondingProjectRootPath(item).append(
                ERepositoryObjectType.getFolderName(ERepositoryObjectType.getItemType(item)));
        return root;
    }

    /**
     * ggu Comment method "getCorrespondingProjectRootPath".
     * 
     * if item is null, will return currrent probject path.
     */
    protected IPath getCorrespondingProjectRootPath(Item item) throws Exception {
        // for bug 17685
        org.talend.core.model.properties.Project p = ProjectManager.getInstance().getProject(item);
        IProject project = null;
        if (p != null) {
            project = ResourcesPlugin.getWorkspace().getRoot().getProject(p.getTechnicalLabel().toUpperCase());
            if (project != null) {
                return project.getLocation();
            }
        }
        // maybe, not used
        project = RepositoryPlugin.getDefault().getRunProcessService().getProject(LanguageManager.getCurrentLanguage());
        IPath root = project.getParent().getLocation().append(getCorrespondingProjectName(item).toUpperCase());
        return root;
    }

    /**
     * Gets current project name.
     * 
     * @param item TODO
     * 
     * @return
     */
    protected abstract String getCorrespondingProjectName(Item item);

    /**
     * DOC qwei Comment method "addDepencies".
     */
    protected void addDependencies(ExportFileResource[] allResources, ProcessItem processItem, Boolean needDependencies,
            ExportFileResource resource) {
        if (!needDependencies) {
            return;
        }
        Collection<IRepositoryViewObject> allDependencies = ProcessUtils.getAllProcessDependencies(
                Arrays.asList(new Item[] { processItem }), false);

        for (IRepositoryViewObject object : allDependencies) {
            Item item = object.getProperty().getItem();

            try {
                ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(item);
                IPath typeFolderPath = new Path(ERepositoryObjectType.getFolderName(itemType));
                String itemName = item.getProperty().getLabel();
                String itemVersion = item.getProperty().getVersion();
                String itemPath = item.getState().getPath();

                itemPath = (itemPath == null || itemPath.equals("")) ? "" : itemPath; //$NON-NLS-1$ //$NON-NLS-2$
                IPath projectRootPath = getCorrespondingProjectRootPath(item);
                String projectName = getCorrespondingProjectName(item);
                // project file
                IPath projectFilePath = getCorrespondingProjectRootPath(item).append(FileConstants.LOCAL_PROJECT_FILENAME);
                checkAndAddProjectResource(allResources, resource, JOB_ITEMS_FOLDER_NAME + PATH_SEPARATOR + projectName,
                        FileLocator.toFileURL(projectFilePath.toFile().toURL()));

                IPath itemFilePath = projectRootPath.append(typeFolderPath).append(itemPath)
                        .append(itemName + "_" + itemVersion + "." + FileConstants.ITEM_EXTENSION); //$NON-NLS-1$ //$NON-NLS-2$
                IPath propertiesFilePath = projectRootPath.append(typeFolderPath).append(itemPath)
                        .append(itemName + "_" + itemVersion + "." //$NON-NLS-1$ //$NON-NLS-2$
                                + FileConstants.PROPERTIES_EXTENSION);

                List<URL> metadataNameFileUrls = new ArrayList<URL>();

                metadataNameFileUrls.add(FileLocator.toFileURL(itemFilePath.toFile().toURL()));
                metadataNameFileUrls.add(FileLocator.toFileURL(propertiesFilePath.toFile().toURL()));

                String basePath = JOB_ITEMS_FOLDER_NAME + PATH_SEPARATOR + projectName + PATH_SEPARATOR
                        + typeFolderPath.toString();
                if (itemPath != null && !"".equals(itemPath)) { //$NON-NLS-1$
                    basePath = basePath + PATH_SEPARATOR + itemPath;
                }
                resource.addResources(basePath, metadataNameFileUrls);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }

    }

    protected void checkAndAddProjectResource(ExportFileResource[] allResources, ExportFileResource curResource,
            String relativePath, URL projectURL) {
        if (allResources == null || curResource == null || projectURL == null) {
            return;
        }
        if (relativePath == null) {
            relativePath = ""; //$NON-NLS-1$
        }
        /*
         * boolean found = false; for (ExportFileResource res : allResources) { Set<URL> urls =
         * res.getResourcesByRelativePath(relativePath); if (urls != null && urls.contains(projectURL)) { found = true;
         * break; } }
         */
        // for bug 13256
        List<URL> projectUrls = new ArrayList<URL>();
        projectUrls.add(projectURL);
        curResource.addResources(relativePath, projectUrls);
    }

    /**
     * DOC Administrator Comment method "getJobContextsComboValue".
     * 
     * @param item
     * @return
     */
    public List<String> getJobContextsComboValue(ProcessItem item) {
        // TODO Auto-generated method stub
        return null;
    }
}
