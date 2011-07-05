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
package org.talend.repository.ui.wizards.newproject.copyfromeclipse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.internal.wizards.datatransfer.ArchiveFileManipulations;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.internal.wizards.datatransfer.ILeveledImportStructureProvider;
import org.eclipse.ui.internal.wizards.datatransfer.TarEntry;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.eclipse.ui.internal.wizards.datatransfer.TarFile;
import org.eclipse.ui.internal.wizards.datatransfer.TarLeveledStructureProvider;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage;
import org.eclipse.ui.internal.wizards.datatransfer.ZipLeveledStructureProvider;
import org.eclipse.ui.statushandlers.StatusManager;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;

/**
 * DOC zhangchao.wang class global comment. Detailled comment
 */
public class TalendWizardProjectsImportPage extends WizardProjectsImportPage {

    public TalendWizardProjectsImportPage() {

    }

    private String sourcePath;

    private ILeveledImportStructureProvider structureProvider;

    /**
     * 
     * DOC guanglong.du TalendWizardProjectsImportPage class global comment. Detailled comment
     */
    public class TalendProjectRecord {

        File projectSystemFile;

        Object projectArchiveFile;

        String projectName;

        Object parent;

        int level;

        boolean hasConflicts;

        IProjectDescription description;

        /**
         * Create a record for a project based on the info in the file.
         * 
         * @param file
         */
        TalendProjectRecord(File file) {
            projectSystemFile = file;
            setProjectName();
        }

        /**
         * @param file The Object representing the .project file
         * @param parent The parent folder of the .project file
         * @param level The number of levels deep in the provider the file is
         */
        TalendProjectRecord(Object file, Object parent, int level) {
            this.projectArchiveFile = file;
            this.parent = parent;
            this.level = level;
            setProjectName();
        }

        /**
         * Set the name of the project based on the projectFile.
         */
        private void setProjectName() {
            try {
                if (projectArchiveFile != null) {
                    InputStream stream = structureProvider.getContents(projectArchiveFile);

                    // If we can get a description pull the name from there
                    if (stream == null) {
                        if (projectArchiveFile instanceof ZipEntry) {
                            IPath path = new Path(((ZipEntry) projectArchiveFile).getName());
                            projectName = path.segment(path.segmentCount() - 2);
                        } else if (projectArchiveFile instanceof TarEntry) {
                            IPath path = new Path(((TarEntry) projectArchiveFile).getName());
                            projectName = path.segment(path.segmentCount() - 2);
                        }
                    } else {
                        description = IDEWorkbenchPlugin.getPluginWorkspace().loadProjectDescription(stream);
                        stream.close();
                        projectName = description.getName();
                    }

                }

                // If we don't have the project name try again
                if (projectName == null) {
                    IPath path = new Path(projectSystemFile.getPath());
                    // if the file is in the default location, use the directory
                    // name as the project name
                    if (isDefaultLocation(path)) {
                        projectName = path.segment(path.segmentCount() - 2);
                        description = IDEWorkbenchPlugin.getPluginWorkspace().newProjectDescription(projectName);
                    } else {
                        description = IDEWorkbenchPlugin.getPluginWorkspace().loadProjectDescription(path);
                        projectName = description.getName();
                    }

                }
            } catch (CoreException e) {
                // no good couldn't get the name
            } catch (IOException e) {
                // no good couldn't get the name
            }
        }

        /**
         * Returns whether the given project description file path is in the default location for a project
         * 
         * @param path The path to examine
         * @return Whether the given path is the default location for a project
         */
        private boolean isDefaultLocation(IPath path) {
            // The project description file must at least be within the project,
            // which is within the workspace location
            if (path.segmentCount() < 2)
                return false;
            return path.removeLastSegments(2).toFile().equals(Platform.getLocation().toFile());
        }

        /**
         * Get the name of the project
         * 
         * @return String
         */
        public String getProjectName() {
            return projectName;
        }

        /**
         * Gets the label to be used when rendering this project record in the UI.
         * 
         * @return String the label
         * @since 3.4
         */
        public String getProjectLabel() {
            if (description == null)
                return projectName;

            String path = projectSystemFile == null ? structureProvider.getLabel(parent) : projectSystemFile.getParent();

            return NLS.bind(DataTransferMessages.WizardProjectsImportPage_projectLabel, projectName, path);
        }

        /**
         * @return Returns the hasConflicts.
         */
        public boolean hasConflicts() {
            return hasConflicts;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage#updateProjectsList(java.lang.String)
     */
    @Override
    public void updateProjectsList(String sourcePath) {
        if (sourcePath == null || sourcePath.length() == 0) {
            return;
        }
        String destinationJavaPath = null;
        String destinationPerlPath = null;
        this.sourcePath = sourcePath;
        try {
            if (!("".equals(sourcePath))) { //$NON-NLS-1$
                destinationJavaPath = CorePlugin.getDefault().getLibrariesService().getJavaLibrariesPath();
                destinationPerlPath = CorePlugin.getDefault().getLibrariesService().getPerlLibrariesPath();

                IPathVariableManager pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
                pathVariableManager.setValue(EXTERNAL_LIB_JAVA_PATH, new Path(destinationJavaPath));
                if (destinationPerlPath != null) {
                    pathVariableManager.setValue(EXTERNAL_LIB_PERL_PATH, new Path(destinationPerlPath));
                }

                super.updateProjectsList(sourcePath);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    @Override
    public ProjectRecord[] getProjectRecords() {
        if (sourcePath == null || sourcePath.length() == 0) {
            return new ProjectRecord[0];
        }
        ProjectRecord[] selectedProjects = super.getProjectRecords();
        Collection files = new ArrayList();
        List projects = new ArrayList();
        ProjectRecord[] selected = null;
        for (int i = 0; i < selectedProjects.length; i++) {
            projects.add(selectedProjects[i]);
        }
        final File directory = new File(sourcePath);
        if (ArchiveFileManipulations.isTarFile(sourcePath)) {
            try {
                TarFile sourceTarFile = new TarFile(sourcePath);
                if (sourceTarFile == null) {
                    return new ProjectRecord[0];
                }
                structureProvider = new TarLeveledStructureProvider(sourceTarFile);
                Object child = structureProvider.getRoot();
                collectProjectFilesFromProvider(files, child, 0);
                selected = new ProjectRecord[files.size()];
                Iterator filesIterator = files.iterator();
                int j = 0;
                while (filesIterator.hasNext()) {
                    TalendProjectRecord file = (TalendProjectRecord) filesIterator.next();
                    for (int i = 0; i < projects.size(); i++) {
                        if (file.getProjectName().equals(((ProjectRecord) projects.get(i)).getProjectName())) {
                            selected[j] = (ProjectRecord) projects.get(i);
                            j++;
                        }
                    }

                }
            } catch (TarException e) {
                displayErrorDialog(DataTransferMessages.TarImport_badFormat);
            } catch (IOException e) {
                displayErrorDialog(DataTransferMessages.ZipImport_couldNotRead);
            }
        } else if (ArchiveFileManipulations.isZipFile(sourcePath)) {
            try {
                ZipFile sourceFile = new ZipFile(sourcePath);
                if (sourceFile == null) {
                    return new ProjectRecord[0];
                }
                structureProvider = new ZipLeveledStructureProvider(sourceFile);
                Object child = structureProvider.getRoot();
                collectProjectFilesFromProvider(files, child, 0);
                selected = new ProjectRecord[files.size()];
                Iterator filesIterator = files.iterator();
                int j = 0;
                while (filesIterator.hasNext()) {
                    TalendProjectRecord file = (TalendProjectRecord) filesIterator.next();
                    for (int i = 0; i < projects.size(); i++) {
                        if (file.getProjectName().equals(((ProjectRecord) projects.get(i)).getProjectName())) {
                            selected[j] = (ProjectRecord) projects.get(i);
                            j++;
                        }
                    }
                }
            } catch (IOException e) {
                displayErrorDialog(DataTransferMessages.ZipImport_badFormat);
            }
        } else if (directory.isDirectory()) {
            collectProjectFilesFromDirectory(files, directory, null);
            selected = new ProjectRecord[files.size()];
            Iterator filesIterator = files.iterator();
            int j = 0;
            while (filesIterator.hasNext()) {
                File file = (File) filesIterator.next();
                for (int i = 0; i < projects.size(); i++) {
                    if (file.getParentFile().getName().equals(((ProjectRecord) projects.get(i)).getProjectName())) {
                        selected[j] = (ProjectRecord) projects.get(i);
                        j++;
                    }
                }
            }
        }

        return selected;
    }

    private boolean collectProjectFilesFromProvider(Collection files, Object entry, int level) {
        List children = structureProvider.getChildren(entry);
        if (children == null) {
            children = new ArrayList(1);
        }
        boolean isContainsFile = false;
        Iterator childrenEnum = children.iterator();
        for (int i = 0; i < children.size(); i++) {
            Object child = children.get(i);
            if (!structureProvider.isFolder(child)) {
                String elementLabel = structureProvider.getLabel(child);
                if (elementLabel.equals("talend.project")) {
                    isContainsFile = true;
                }
            }
        }
        while (childrenEnum.hasNext()) {
            Object child = childrenEnum.next();
            if (structureProvider.isFolder(child)) {
                collectProjectFilesFromProvider(files, child, level + 1);
            }
            String elementLabel = structureProvider.getLabel(child);
            if (elementLabel.equals(".project") && isContainsFile) {
                files.add(new TalendProjectRecord(child, entry, level));
            }
        }
        return true;
    }

    private boolean collectProjectFilesFromDirectory(Collection files, File directory, Set directoriesVisited) {

        File[] contents = directory.listFiles();
        if (contents == null)
            return false;

        // Initialize recursion guard for recursive symbolic links
        if (directoriesVisited == null) {
            directoriesVisited = new HashSet();
            try {
                directoriesVisited.add(directory.getCanonicalPath());
            } catch (IOException exception) {
                StatusManager.getManager()
                        .handle(StatusUtil.newStatus(IStatus.ERROR, exception.getLocalizedMessage(), exception));
            }
        }

        // first look for project description files
        final String dotProject = "talend.project";
        for (int i = 0; i < contents.length; i++) {
            File file = contents[i];
            if (file.isFile() && file.getName().equals(dotProject)) {
                files.add(file);
                // don't search sub-directories since we can't have nested
                // projects
                return true;
            }
        }
        // no project description found, so recurse into sub-directories
        for (int i = 0; i < contents.length; i++) {
            if (contents[i].isDirectory()) {
                if (!contents[i].getName().equals(METADATA_FOLDER)) {
                    try {
                        String canonicalPath = contents[i].getCanonicalPath();
                        if (!directoriesVisited.add(canonicalPath)) {
                            // already been here --> do not recurse
                            continue;
                        }
                    } catch (IOException exception) {
                        StatusManager.getManager().handle(
                                StatusUtil.newStatus(IStatus.ERROR, exception.getLocalizedMessage(), exception));
                    }
                    collectProjectFilesFromDirectory(files, contents[i], directoriesVisited);
                }
            }
        }
        return true;
    }

    public final static String EXTERNAL_LIB_JAVA_PATH = "external_lib_java_path"; //$NON-NLS-1$

    public final static String EXTERNAL_LIB_PERL_PATH = "external_lib_perl_path"; //$NON-NLS-1$

}
