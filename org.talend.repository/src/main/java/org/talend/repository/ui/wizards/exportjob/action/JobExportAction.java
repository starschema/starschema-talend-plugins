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
package org.talend.repository.ui.wizards.exportjob.action;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.EventLoopProgressMonitor;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.documentation.ArchiveFileExportOperationFullPath;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.i18n.Messages;
import org.talend.repository.job.deletion.JobResource;
import org.talend.repository.job.deletion.JobResourceManager;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.utils.ZipToFile;
import org.talend.repository.ui.wizards.exportjob.JavaJobExportReArchieveCreator;
import org.talend.repository.ui.wizards.exportjob.JobScriptsExportWizardPage;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.utils.JobVersionUtils;

/**
 * 
 * class global comment. Detailled comment
 */
public class JobExportAction implements IRunnableWithProgress {

    private List<RepositoryNode> nodes;

    private String jobVersion;

    private JobScriptsManager manager;

    private String directoryName;

    private String bundleVersion;

    private String type = "Job";

    public JobExportAction(List<RepositoryNode> nodes, String jobVersion, String bundleVersion, JobScriptsManager manager,
            String directoryName, String type) {
        this(nodes, jobVersion, jobVersion, manager, directoryName);
        this.type = type;
    }

    public JobExportAction(List<RepositoryNode> nodes, String jobVersion, String bundleVersion, JobScriptsManager manager,
            String directoryName) {
        super();
        this.nodes = nodes;
        this.jobVersion = jobVersion;
        this.bundleVersion = bundleVersion;
        this.manager = manager;
        this.directoryName = directoryName;
    }

    public JobExportAction(List<RepositoryNode> nodes, String jobVersion, JobScriptsManager manager, String directoryName,
            String type) {
        this(nodes, jobVersion, jobVersion, manager, directoryName, type);
    }

    public JobExportAction(List<RepositoryNode> nodes, String jobVersion, JobScriptsManager manager, String directoryName) {
        this(nodes, jobVersion, jobVersion, manager, directoryName);
    }

    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        final EventLoopProgressMonitor progressMonitor = new EventLoopProgressMonitor(monitor);

        progressMonitor.beginTask(
                Messages.getString("JobScriptsExportWizardPage.newExportJobScript", type), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
        // int i = 0;
        // int nodeSize = nodes.size();
        // manager.setTmpFleDeleted(false);
        // Changed by Marvin Wang on Jun.1, 2012 for bug TDI-21244, the original codes have some logical problems.
        if (nodes != null && nodes.size() > 0) {
            int size = nodes.size();
            if (size == 1) {
                if (jobVersion != null && jobVersion.equals(JobScriptsExportWizardPage.ALL_VERSIONS)) {
                    String[] allVersions = JobVersionUtils.getAllVersions(nodes.get(0));
                    for (String version : allVersions) {
                        monitor.subTask(Messages.getString(
                                "JobScriptsExportWizardPage.newExportJob0", type, nodes.get(0).getLabel(), version)); //$NON-NLS-1$
                        // Commentted by Marvin Wang on Jun.1, 2012.
                        // FIXME Here will export all nodes for each loop. Maybe only the lastest version can be saved,
                        // caz the privious versions are deleted.
                        if (!exportJobScript(nodes, version, bundleVersion, progressMonitor)) {
                            return;
                        }
                    }
                } else {
                    if (!exportJobScript(nodes, jobVersion, bundleVersion, progressMonitor)) {
                        return;
                    }
                    monitor.subTask(Messages.getString("JobScriptsExportWizardPage.newExportSuccess", type, nodes.get(0)
                            .getLabel(), jobVersion));
                }
            } else if (size > 1) {
                if (!exportJobScript(nodes, jobVersion, bundleVersion, progressMonitor)) {
                    return;
                }
                monitor.subTask(Messages.getString("JobScriptsExportWizardPage.newExportSuccess", "all"));
            }
        }
        // for (RepositoryNode node : nodes) {
        // // if (i == nodeSize - 1)
        // // manager.setTmpFleDeleted(true);
        // List<RepositoryNode> exportedNode = new ArrayList<RepositoryNode>();
        // exportedNode.add(node);
        // if (jobVersion != null && jobVersion.equals(JobScriptsExportWizardPage.ALL_VERSIONS)) {
        // String[] allVersions = JobVersionUtils.getAllVersions(node);
        // for (String version : allVersions) {
        // monitor.subTask(Messages
        //                            .getString("JobScriptsExportWizardPage.newExportJob0", type, node.getLabel(), version)); //$NON-NLS-1$
        // if (!exportJobScript(nodes, version, bundleVersion, progressMonitor)) {
        // return;
        // }
        // }
        // } else {
        //                monitor.subTask(Messages.getString("JobScriptsExportWizardPage.newExportJob1", type, node.getLabel(), jobVersion)); //$NON-NLS-1$
        // if (!exportJobScript(nodes, jobVersion, bundleVersion, progressMonitor)) {
        // return;
        // }
        // }
        // // i++;
        // monitor.subTask(Messages.getString(
        //                    "JobScriptsExportWizardPage.newExportJobSucessful", type, node.getLabel(), jobVersion)); //$NON-NLS-1$
        // }
        progressMonitor.done();
    }

    private List<ExportFileResource> getProcesses(List<RepositoryNode> nodes, String path) {
        List<ExportFileResource> value = new ArrayList<ExportFileResource>();
        for (final RepositoryNode node : nodes) {
            if (node.getType() == ENodeType.SYSTEM_FOLDER || node.getType() == ENodeType.SIMPLE_FOLDER) {
                @SuppressWarnings({ "unchecked", "serial" })
                List<RepositoryNode> children = new ArrayList<RepositoryNode>() {

                    {
                        addAll((Collection<? extends RepositoryNode>) node.getChildren());
                    }
                };
                value.addAll(getProcesses(children, node.getProperties(EProperties.LABEL).toString() + "/"));
            }
            if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
                IRepositoryViewObject repositoryObject = node.getObject();
                if (repositoryObject.getProperty().getItem() instanceof ProcessItem) {
                    ProcessItem processItem = (ProcessItem) repositoryObject.getProperty().getItem();
                    ExportFileResource resource = new ExportFileResource(processItem, path + processItem.getProperty().getLabel());
                    processItem.getProcess().getNode();
                    resource.setNode((RepositoryNode) node);
                    value.add(resource);
                }
            }
        }
        return value;
    }

    private boolean exportJobScript(List<RepositoryNode> nodes, String version, String bundleVersion, IProgressMonitor monitor) {
        manager.setJobVersion(version);
        manager.setBundleVersion(bundleVersion);

        List<ExportFileResource> processes = getProcesses(nodes, "");

        boolean isNotFirstTime = directoryName != null;
        if (isNotFirstTime && processes != null) {
            for (ExportFileResource process : processes) {
                process.setDirectoryName(directoryName);
            }
        }

        try {
            ProxyRepositoryFactory.getInstance().initialize();
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        ItemCacheManager.clearCache();

        if (!isMultiNodes()) { // TODO : bug with export?
            for (ExportFileResource process : processes) {
                process.removeAllMap();
                ProcessItem processItem = (ProcessItem) process.getItem();
                if (!processItem.getProperty().getVersion().equals(version)) {
                    // update with the correct version.
                    process.setProcess(ItemCacheManager.getProcessItem(processItem.getProperty().getId(), version));
                }
            }

        }

        manager.setProgressMonitor(monitor);
        List<ExportFileResource> resourcesToExport = null;
        try {
            resourcesToExport = manager.getExportResources(processes.toArray(new ExportFileResource[] {}));
            IStructuredSelection selection = new StructuredSelection(nodes);
            // if job has compile error, will not export to avoid problem if run jobscript
            boolean hasErrors = CorePlugin.getDefault().getRunProcessService().checkExportProcess(selection, true);
            if (hasErrors) {
                manager.deleteTempFiles();
                return false;
            }
        } catch (ProcessorException e) {
            MessageBoxExceptionHandler.process(e);
            return false;
        }
        manager.setTopFolder(resourcesToExport);

        // boolean ok =executeExportOperation(new ArchiveFileExportOperationFullPath(process));
        ArchiveFileExportOperationFullPath exporterOperation = new ArchiveFileExportOperationFullPath(resourcesToExport,
                getTempDestinationValue());

        executeExportOperation(exporterOperation, monitor);

        // path can like name/name
        manager.deleteTempFiles();
        ProcessorUtilities.resetExportConfig();

        String projectName = ((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                .getProject().getLabel();

        List<JobResource> jobResources = new ArrayList<JobResource>();

        for (ExportFileResource process : processes) {
            ProcessItem processItem = (ProcessItem) process.getItem();
            JobInfo jobInfo = new JobInfo(processItem, processItem.getProcess().getDefaultContext(), version);
            jobResources.add(new JobResource(projectName, jobInfo));

            Set<JobInfo> jobInfos = ProcessorUtilities.getChildrenJobInfo(processItem);
            for (JobInfo subjobInfo : jobInfos) {
                jobResources.add(new JobResource(projectName, subjobInfo));
            }
        }

        JobResourceManager reManager = JobResourceManager.getInstance();
        for (JobResource r : jobResources) {
            if (reManager.isProtected(r)) {
                try {
                    ProcessorUtilities.generateCode(r.getJobInfo().getJobId(), r.getJobInfo().getContextName(), r.getJobInfo()
                            .getJobVersion(), false, false, monitor);
                } catch (ProcessorException e) {
                    MessageBoxExceptionHandler.process(e);
                    return false;
                }
            }
        }
        monitor.subTask(Messages.getString("JobScriptsExportWizardPage.newExportSuccess", type)); //$NON-NLS-1$
        reBuildJobZipFile(processes);
        return true;
    }

    /**
     * DOC zli Comment method "getTempDestinationValue".
     * 
     * @return
     */
    protected String getTempDestinationValue() {
        String idealSuffix = manager.getOutputSuffix();
        String destinationText = manager.getDestinationPath();
        String tempdestination = JavaJobExportReArchieveCreator.getTmpDestinationFolder();
        if (destinationText.indexOf("\\") != -1) {
            int lastIndexOf = destinationText.lastIndexOf("\\");
            String substring = destinationText.substring(lastIndexOf + 1, destinationText.length());
            tempdestination = tempdestination + "/" + substring;
        }
        if (tempdestination.length() != 0 && !tempdestination.endsWith(File.separator)) {
            int dotIndex = tempdestination.lastIndexOf('.');
            if (dotIndex != -1) {
                // the last path seperator index
                int pathSepIndex = tempdestination.lastIndexOf(File.separator);
                if (pathSepIndex != -1 && dotIndex < pathSepIndex) {
                    tempdestination += idealSuffix;
                }
            } else {
                tempdestination += idealSuffix;
            }
        }
        if (tempdestination.endsWith(jobVersion + manager.getOutputSuffix())) {
            return tempdestination;
        }
        return tempdestination;

    }

    /**
     * 
     * DOC aiming Comment method "reBuildJobZipFile".
     * 
     * @param processes
     */
    private void reBuildJobZipFile(List<ExportFileResource> processes) {
        JavaJobExportReArchieveCreator creator = null;
        String zipFile = getTempDestinationValue();
        String destinationZipFile = manager.getDestinationPath();

        String tmpFolder = JavaJobExportReArchieveCreator.getTmpFolder();
        try {
            // unzip to tmpFolder
            ZipToFile.unZipFile(zipFile, tmpFolder);
            // build new jar
            for (ExportFileResource process : processes) {
                if (process != null) {
                    String jobFolderName = process.getDirectoryName();
                    int pos = jobFolderName.indexOf("/"); //$NON-NLS-1$
                    if (pos != -1) {
                        jobFolderName = jobFolderName.substring(pos + 1);
                    }
                    if (creator == null) {
                        creator = new JavaJobExportReArchieveCreator(zipFile, jobFolderName);
                    } else {
                        creator.setJobFolerName(jobFolderName);
                    }
                    creator.buildNewJar();
                }
            }
            // Modified by Marvin Wang on Feb.1, 2012 for bug
            if (canCreateNewFile(destinationZipFile))
                // rezip the tmpFolder to zipFile
                ZipToFile.zipFile(tmpFolder, destinationZipFile);
            else {
                MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Can not create a file",
                        "Can not create a file or have not the permission to create a file!");
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally {
            JavaJobExportReArchieveCreator.deleteTempFiles();
            JavaJobExportReArchieveCreator.deleteTempDestinationFiles();
            new File(zipFile).delete(); // delete the temp zip file
        }
    }

    /**
     * Added by Marvin Wang on Feb.1, 2012 for estimating if the file can be created. In win7 or other systems, have not
     * the permission to create a file directly under system disk(like C:\).
     * 
     * @param disZipFileStr
     * @return
     */
    private boolean canCreateNewFile(String disZipFileStr) {
        boolean flag = true;
        File disZipFile = new File(disZipFileStr);
        if (!disZipFile.exists()) {
            try {
                disZipFile.createNewFile();
            } catch (IOException e) {
                flag = false;
                ExceptionHandler.process(e);
            }
        }
        return flag;
    }

    /**
     * Export the passed resource and recursively export all of its child resources (iff it's a container). Answer a
     * boolean indicating success.
     */
    private boolean executeExportOperation(ArchiveFileExportOperationFullPath op, IProgressMonitor monitor) {
        op.setCreateLeadupStructure(true);
        op.setUseCompression(true);

        try {
            op.run(monitor);
        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
        } catch (InterruptedException e) {
            return false;
        }

        IStatus status = op.getStatus();
        if (!status.isOK()) {
            ErrorDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "", null, // no //$NON-NLS-1$
                    // special
                    // message
                    status);
            return false;
        }

        return true;
    }

    private boolean isMultiNodes() {
        return nodes.size() > 1;
    }

}
