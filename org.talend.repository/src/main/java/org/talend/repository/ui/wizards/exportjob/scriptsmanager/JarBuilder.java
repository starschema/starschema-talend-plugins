// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.repository.utils.ZipFileUtils;

/**
 * This is a jar file builder. <br/>
 * 
 * $Id: MakeJarRunnable.java Mar 30, 200712:49:05 PM bqian $
 * 
 */
public class JarBuilder {

    String dir = null;

    String jarFile = null;

    String jarname = null;

    List<String> includeDirs = null;

    List<String> excludeDirs = null;

    List<File> excludeFiles = null;

    List<File> includeRoutines = null;

    private static final String SYSTEM = "system"; //$NON-NLS-1$

    private static final String CONTEXT = "context"; //$NON-NLS-1$

    private static final String TEMP = "temp"; //$NON-NLS-1$

    /**
     * Constructure.
     * 
     * @param root
     * @param jarFile
     * @param jarName
     * @param includeDirs
     */
    JarBuilder(String root, String jarFile) {
        this.dir = root;
        this.jarFile = jarFile;
        File file = new File(jarFile);
        this.jarname = file.getName();
    }

    public void setIncludeDir(List<String> includeDirs) {
        this.includeDirs = includeDirs;
    }

    public void setExcludeDir(List<String> excludeDirs) {
        this.excludeDirs = excludeDirs;
    }

    public void setExcludeFiles(List<File> excludeFiles) {
        this.excludeFiles = excludeFiles;
    }

    public void setIncludeRoutines(List<File> includeRoutines) {
        this.includeRoutines = includeRoutines;
    }

    /**
     * Gets the files to zip in jar.
     * 
     * @return
     */
    private List<File> getExportedFiles() {

        if (includeDirs == null) {
            includeDirs = new ArrayList<String>();
            includeDirs.add(""); //$NON-NLS-1$
        }
        List<File> includeFiles = getAllFiles(includeDirs);

        if (excludeDirs != null) {
            List<File> excludeFiles = getAllFiles(excludeDirs);
            includeFiles.removeAll(excludeFiles);
        }
        if (excludeFiles != null) {
            includeFiles.removeAll(excludeFiles);
        }
        if (includeRoutines != null) {
            for (File f : includeRoutines) {
                if (!includeFiles.contains(f)) {
                    includeFiles.add(f);
                }
            }
        }

        return includeFiles;
    }

    private List<File> getAllFiles(List<String> subDirs) {
        final List<File> list = new ArrayList<File>();

        for (int i = 0; i < subDirs.size(); i++) {

            File subFile = new File(dir, subDirs.get(i));
            subFile.listFiles(new java.io.FilenameFilter() {

                public boolean accept(java.io.File dir, String name) {
                    File file = new java.io.File(dir, name);
                    if (file.isFile()) {
                        list.add(file);
                        return true;
                    } else {
                        file.listFiles(this);
                    }
                    return false;
                }
            });
        }
        return list;
    }

    private Manifest getManifest() throws IOException {

        Manifest manifest = new Manifest();
        Map<String, Attributes> m = manifest.getEntries();
        Attributes a = new Attributes();
        a.put(Attributes.Name.IMPLEMENTATION_VERSION, "1.0"); //$NON-NLS-1$
        a.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Talend Open Studio"); //$NON-NLS-1$
        m.put(jarname, a);
        return manifest;
    }

    /**
     * Builds the jar file.
     * 
     * @throws Exception
     */
    public void buildJar() throws Exception {
        File root = new File(dir);
        final List<File> list = getExportedFiles();
        Manifest manifest = getManifest();
        exportJar(root, list, manifest);
    }

    /**
     * Create temp folder for zip files to jar file. Add by nma, order 12346
     * 
     * @throws Exception
     */
    private void createTempSubFolder(String tempFolderPath, File srcFile) {
        if (srcFile.isDirectory() && !srcFile.getName().equals(CONTEXT)) {
            File projectFolder = new File(tempFolderPath + File.separator + srcFile.getName());
            if (!projectFolder.exists()) {
                projectFolder.mkdir();
                File[] folderFiles = srcFile.listFiles();
                for (File f : folderFiles) {
                    createTempSubFolder(projectFolder.getAbsolutePath(), f);
                }
            }
        }
    }

    /**
     * exports the jar to specific location.
     * 
     * @param root
     * @param list
     * @param manifest
     */
    private void exportJar(File root, List<File> list, Manifest manifest) throws Exception {
        File file = new File(jarFile);
        if (file.exists()) {
            String tempFolderPath = file.getParent() + File.separator + TEMP;
            ZipFileUtils.unZip(jarFile, tempFolderPath);
            for (File subf : list) {
                String desFileName = subf.getAbsolutePath().substring(root.getAbsolutePath().length() + 1);
                File srcFile = subf;
                while (!srcFile.getParentFile().getAbsolutePath().equals(root.getAbsolutePath())) {
                    srcFile = srcFile.getParentFile();
                }
                createTempSubFolder(tempFolderPath, srcFile);
                try {
                    FileChannel srcChannel = new FileInputStream(subf.getAbsoluteFile()).getChannel();
                    FileChannel dstChannel = new FileOutputStream(tempFolderPath + File.separator + desFileName).getChannel();
                    dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
                    srcChannel.close();
                    dstChannel.close();
                } catch (IOException e) {
                    ExceptionHandler.process(e);
                }
            }
            ZipFileUtils.zip(tempFolderPath, jarFile, false);
        } else {
            JarOutputStream jarOut = null;
            try {
                jarOut = new JarOutputStream(new FileOutputStream(jarFile), manifest);

                for (int i = 0; i < list.size(); i++) {
                    String filename = list.get(i).getAbsolutePath();
                    filename = filename.substring(root.getAbsolutePath().length() + 1);
                    JarEntry entry = new JarEntry(filename.replace('\\', '/'));
                    jarOut.putNextEntry(entry);

                    FileInputStream fin = new FileInputStream(list.get(i));
                    byte[] buf = new byte[4096];
                    int read;
                    while ((read = fin.read(buf)) != -1) {
                        jarOut.write(buf, 0, read);
                    }
                    fin.close();

                    jarOut.closeEntry();
                    jarOut.flush();
                }
            } finally {

                if (jarOut != null) {
                    try {
                        jarOut.close();
                    } catch (Exception e) {
                        // do nothing
                    }
                }
            }
        }
    }
}
