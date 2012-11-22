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
package org.talend.repository.ui.wizards.exportjob;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.repository.ui.utils.ZipToFile;

/**
 * This class use to reAchieve the export job script file after the jobscript zip file generated, put all the jar files
 * in 'classpath.jar' to fix bug 0006108
 */
public class JavaJobExportReArchieveCreator {

    private String zipFile; // created zip filename

    private String jobFolerName;

    private File jobFolder;

    private File libFolder;

    private File drlFolder, xlsFolder; // hywang add for 6484

    private File batFile;

    private File shFile;

    private static final String CLASSPATH_JAR = "classpath.jar"; // the output new jar filename //$NON-NLS-1$

    private static final String LIB = "lib"; // lib folder //$NON-NLS-1$

    private static final String DRL = "drl"; //drl folder hywang add //$NON-NLS-N$ //$NON-NLS-1$

    private static final String XLS = "xls"; //xls folder hywang add //$NON-NLS-N$ //$NON-NLS-1$

    private static final String RULES_ROOT = "Rules"; //ruleRoot folder hywang add //$NON-NLS-N$ //$NON-NLS-1$

    String needModuleJarStrs = "";

    public JavaJobExportReArchieveCreator(String zipFile, String jobFolderName) {
        this.zipFile = zipFile;
        this.jobFolerName = jobFolderName;
    }

    public void buildNewJar() {
        try {
            String tmpFoler = getTmpFolder();
            // ZipToFile.unZipFile(zipFile, tmpFoler);
            // init jobFolder File
            initJobFolder();
            // bug 21473
            // change the .bat file & .sh file
            if (batFile != null) {
                changeScriptFile(batFile);
            }
            if (shFile != null) {
                changeScriptFile(shFile);
            }
            if (jobFolder == null) {
                return;
            }
            String newJarPath = jobFolder.getAbsolutePath() + "/" + CLASSPATH_JAR; //$NON-NLS-1$
            NewJarBuilder jarBuilder = new NewJarBuilder(tmpFoler, newJarPath);
            jarBuilder.buildJar();

            // delete non used jar files
            // deleteNonUsedJar();

        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * 
     * DOC aiming Comment method "getJobFolerName".
     * 
     * @return
     */
    public String getJobFolerName() {
        return this.jobFolerName;
    }

    /**
     * 
     * DOC aiming Comment method "setJobFolerName".
     * 
     * @param jobFolerName
     */
    public void setJobFolerName(String jobFolerName) {
        this.jobFolerName = jobFolerName;
    }

    /**
     * 
     * change .sh & .bat file's contents
     * 
     * @throws FileNotFoundException
     */
    private void changeScriptFile(File file) {
        // change batFile
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new FileReader(file));
            StringBuffer changedContent = new StringBuffer();

            // write all the lines before the java command
            String line = br.readLine();
            while (line != null && !line.contains("java")) { //$NON-NLS-N$  hywang modify for 6484 //$NON-NLS-1$
                changedContent.append(line + "\n"); //$NON-NLS-1$
                line = br.readLine();
            }
            if (line == null)
                line = "";
            // get java command line
            String line1 = line.trim();
            String[] strs = line1.split("\\s"); //$NON-NLS-1$
            int pos = -1;
            for (int i = 0; i < strs.length; i++) {
                if ("-cp".equalsIgnoreCase(strs[i]) || "-classpath".equalsIgnoreCase(strs[i])) { //$NON-NLS-1$ //$NON-NLS-2$
                    pos = i;
                }
            }
            if (pos != -1) {
                if (file.getName().endsWith(".sh")) { //$NON-NLS-1$
                    strs[pos + 1] = CLASSPATH_JAR + ":"; //$NON-NLS-1$
                }
                if (file.getName().endsWith(".bat")) { //$NON-NLS-1$
                    // bug 21473
                    needModuleJarStrs = strs[pos + 1];
                    strs[pos + 1] = CLASSPATH_JAR + ";"; //$NON-NLS-1$
                }
            }

            for (String s : strs) {
                changedContent.append(s).append(" "); //$NON-NLS-1$
            }
            // see bug 7181. add addition lines
            String line2 = br.readLine();
            while (line2 != null) {
                changedContent.append(('\n')).append(line2); //$NON-NLS-1$
                line2 = br.readLine();
            }
            bw = new BufferedWriter(new FileWriter(file));
            // rewrite the changed content to file
            bw.write(changedContent.toString());
            bw.flush();
            // br.close();
            // bw.close();
        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    ExceptionHandler.process(e);
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
    }

    private String[] getLibJarFilenames() {
        if (libFolder != null) {
            File[] files = libFolder.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".jar") //$NON-NLS-1$ //$NON-NLS-2$
                            || name.toLowerCase().endsWith(".zip") ? true : false; //$NON-NLS-1$
                }
            });
            String[] filenames = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                filenames[i] = files[i].getName();
            }
            return filenames;
        }
        return null;
    }

    // hywang add for 6484
    private String[] getDrlFilenames() {
        String[] filenames = null;
        if (drlFolder != null) {
            File[] files = drlFolder.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".drl") //$NON-NLS-1$ 
                    ? true
                            : false; //$NON-NLS-1$
                }
            });
            filenames = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                filenames[i] = files[i].getName();
            }
        }
        return filenames;
    }

    private String[] getXLSFilenames() {
        String[] filenames = null;
        if (xlsFolder != null) {
            File[] files = xlsFolder.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".xls") //$NON-NLS-1$ 
                    ? true
                            : false; //$NON-NLS-1$
                }
            });
            filenames = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                filenames[i] = files[i].getName();
            }
        }
        return filenames;
    }

    private String[] getJobFolderJarFilenames() {
        File[] files = jobFolder.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jar") //$NON-NLS-1$ //$NON-NLS-2$
                        || name.toLowerCase().endsWith(".zip") ? true : false; //$NON-NLS-1$
            }
        });
        String[] filenames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            filenames[i] = files[i].getName();
        }
        return filenames;
    }

    /**
     * 
     * get jobFolder, libFolder, batFile, shFile
     */
    private void initJobFolder() {
        String tmpFoler = getTmpFolder();
        File tmpFile = new File(tmpFoler);
        File[] files = tmpFile.listFiles();
        File[] fs = files[0].listFiles();
        for (int i = 0; fs != null && i < fs.length; i++) {
            if (fs[i].getName().equals(jobFolerName)) {
                jobFolder = fs[i];
                File[] fs1 = jobFolder.listFiles();
                for (File f : fs1) {
                    if (f.getName().toLowerCase().endsWith(".bat")) { //$NON-NLS-1$
                        batFile = f;
                    }
                    if (f.getName().toLowerCase().endsWith(".sh")) { //$NON-NLS-1$
                        shFile = f;
                    }
                }
            }
            if (fs[i].getName().equals(LIB)) {
                libFolder = fs[i];
            }
            // hywang add for 6484
            if (fs[i].getName().equals(DRL)) {
                drlFolder = fs[i];
            }
            if (fs[i].getName().equals(XLS)) {
                xlsFolder = fs[i];
            }
        }
    }

    private String getClasspath() {
        StringBuffer sb = new StringBuffer();
        String[] fs = getJobFolderJarFilenames();
        for (int i = 0; i < fs.length; i++) {
            sb.append(fs[i] + " "); //$NON-NLS-1$
        }
        // bug 21473
        if (needModuleJarStrs != null && !needModuleJarStrs.equals("")) {
            String[] needjars = needModuleJarStrs.split(";");
            if (needjars != null) {
                for (int i = 0; i < needjars.length; i++) {
                    if (".".equals(needjars[i])) {
                        break;
                    }
                    sb.append(needjars[i] + " "); //$NON-NLS-3$
                }
            }
        } else { // TDI-17346:Exported Job as Unix script can't work when you export as Unix script only
            if (shFile != null) {
                String[] fn = getLibJarFilenames();
                if (fn != null) {
                    for (int i = 0; i < fn.length; i++) {
                        sb.append("../" + LIB + "/" + fn[i] + " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                }
            }
        }
        // hywang add for set drl path in classpass.jar
        // String[] drls = getDrlFilenames();
        // String[] xlss = getXLSFilenames();
        // if (drls != null || xlss != null) {
        sb.append("../" + RULES_ROOT + "/"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // }

        //            sb.append("../" + XLS + "/"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // sb.append("\n");
        return sb.toString();
    }

    public static String getTmpFolder() {
        String tmp = System.getProperty("user.dir") + "/newjarFolder"; //$NON-NLS-1$ //$NON-NLS-2$
        tmp = tmp.replace('\\', '/');
        File f = new File(tmp);
        if (!f.exists()) {
            f.mkdir();
        }
        return tmp;
    }

    /**
     * Deletes the temporary files.
     */
    public static void deleteTempFiles() {
        String tmpFold = getTmpFolder();
        File file = new File(tmpFold);
        if (!file.exists() && !file.isDirectory()) {
            return;
        }
        ZipToFile.deleteDirectory(file);
    }

    /**
     * DOC zli Comment method "getTmpDestinationFolder".
     * 
     * @return
     */
    public static String getTmpDestinationFolder() {
        String tmp = System.getProperty("user.dir") + "/newExportFolder";//$NON-NLS-1$ //$NON-NLS-2$
        tmp = tmp.replace('\\', '/');
        File f = new File(tmp);
        if (!f.exists()) {
            f.mkdir();
        }
        return tmp;
    }

    /**
     * DOC zli Comment method "deleteTempDestinationFiles".
     */
    public static void deleteTempDestinationFiles() {
        String tmpFold = getTmpDestinationFolder();
        File file = new File(tmpFold);
        if (!file.exists() && !file.isDirectory()) {
            return;
        }
        ZipToFile.deleteDirectory(file);
    }

    /**
     * 
     * only build the 'manifest.mf' to the classpath.jar
     */
    private class NewJarBuilder {

        String dir = null;

        String jarFile = null;

        String jarname = null;

        /**
         * Constructure.
         * 
         * @param root
         * @param jarFile
         * @param jarName
         * @param includeDirs
         */
        NewJarBuilder(String root, String jarFile) {
            this.dir = root;
            this.jarFile = jarFile;
            File file = new File(jarFile);
            this.jarname = file.getName();
        }

        private Manifest getManifest() throws IOException {
            Manifest manifest = new Manifest();
            Attributes a = manifest.getMainAttributes();
            a.put(Attributes.Name.MANIFEST_VERSION, "1.0"); //$NON-NLS-1$
            a.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Talend Open Studio"); //$NON-NLS-1$
            a.put(Attributes.Name.CLASS_PATH, getClasspath());
            return manifest;
        }

        /**
         * Builds the jar file.
         * 
         * @throws Exception
         */
        public void buildJar() throws Exception {
            Manifest manifest = getManifest();
            // File root = new File(dir);
            // final List<File> list = getExportedFiles();
            // exportJar(root, list, manifest);
            exportJar(manifest);
        }

        private void exportJar(Manifest manifest) throws FileNotFoundException, IOException {
            JarOutputStream jarOut = null;
            try {
                jarOut = new JarOutputStream(new FileOutputStream(jarFile), manifest);
                jarOut.flush();
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
