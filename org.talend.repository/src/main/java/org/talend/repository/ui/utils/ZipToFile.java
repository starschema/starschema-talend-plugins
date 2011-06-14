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
package org.talend.repository.ui.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.talend.commons.exception.ExceptionHandler;

/**
 * DOC aiming class global comment. Detailled comment
 */
public class ZipToFile {

    public static final int BUFFER = 1024;// buf size

    public static void deleteDirectory(File dir) {
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
     * DOC wzhang Comment method "copyFile".
     * 
     * @param src
     * @param dest
     */
    public static void copyFile(String src, String dest) {
        File srcFile = new File(src);
        if (srcFile.exists()) {
            FileInputStream in = null;
            FileOutputStream out = null;
            try {
                in = new FileInputStream(src);
                out = new FileOutputStream(dest);
                byte[] buffer = new byte[1024];
                int i = 0;
                while ((i = in.read(buffer)) != -1) {
                    out.write(buffer, 0, i);
                }
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (in != null)
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                if (out != null)
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
    }

    /**
     * 
     * DOC aiming Comment method "zipFile".
     * 
     * @param baseDir
     * @param zipFile
     * @throws Exception
     */
    public static void zipFile(String baseDir, String zipFile) throws Exception {
        List fileList = getSubFiles(new File(baseDir));
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
        ZipEntry ze = null;
        byte[] buf = new byte[BUFFER];
        int readLen = 0;
        for (int i = 0; i < fileList.size(); i++) {
            File f = (File) fileList.get(i);
            ze = new ZipEntry(getAbsFileName(baseDir, f));
            ze.setSize(f.length());
            ze.setTime(f.lastModified());
            zos.putNextEntry(ze);
            InputStream is = new BufferedInputStream(new FileInputStream(f));
            while ((readLen = is.read(buf, 0, BUFFER)) != -1) {
                zos.write(buf, 0, readLen);
            }
            is.close();
        }
        zos.close();
    }

    /**
     * 
     * DOC aiming Comment method "getAbsFileName".
     * 
     * @param baseDir
     * @param realFileName
     * @return
     */
    private static String getAbsFileName(String baseDir, File realFileName) {
        File real = realFileName;
        File base = new File(baseDir);
        String ret = real.getName();
        while (true) {
            real = real.getParentFile();
            if (real == null)
                break;
            if (real.equals(base))
                break;
            else
                ret = real.getName() + "/" + ret; //$NON-NLS-1$
        }
        return ret;
    }

    /**
     * 
     * DOC aiming Comment method "getSubFiles".
     * 
     * @param baseDir
     * @return
     */
    private static List getSubFiles(File baseDir) {
        List ret = new ArrayList();
        File[] tmp = baseDir.listFiles();
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i].isFile())
                ret.add(tmp[i]);
            if (tmp[i].isDirectory())
                ret.addAll(getSubFiles(tmp[i]));
        }
        return ret;
    }

    /**
     * 
     * DOC aiming Comment method "unZipFile".
     * 
     * @param zipfile
     * @param unzipdir
     * @throws Exception
     */
    public static void unZipFile(String zipfile, String unzipdir) throws Exception {
        File unzipF = new File(unzipdir);
        if (!unzipF.exists()) {
            unzipF.mkdirs();
        }
        ZipFile zfile = new ZipFile(zipfile);
        Enumeration zList = zfile.entries();
        ZipEntry ze = null;
        byte[] buf = new byte[1024];
        while (zList.hasMoreElements()) {
            ze = (ZipEntry) zList.nextElement();
            if (ze.isDirectory()) {
                File f = new File(unzipdir + ze.getName());
                f.mkdirs();
                continue;
            }
            // OutputStream os = new BufferedOutputStream(new FileOutputStream(getRealFileName(unzipdir,
            // ze.getName())));
            unzipdir = unzipdir.replace('\\', '/');
            if (!unzipdir.endsWith("/")) { //$NON-NLS-1$
                unzipdir = unzipdir + "/"; //$NON-NLS-1$
            }
            String filename = unzipdir + ze.getName();
            File zeF = new File(filename);
            if (!zeF.getParentFile().exists()) {
                zeF.getParentFile().mkdirs();
            }

            OutputStream os = new BufferedOutputStream(new FileOutputStream(zeF));
            InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
            int readLen = 0;
            while ((readLen = is.read(buf, 0, 1024)) != -1) {
                os.write(buf, 0, readLen);
            }
            is.close();
            os.close();
        }
        zfile.close();
    }

    public static void main(String[] args) {
        try {
            zipFile("C:\\zipfile\\", "C:\\new.jar"); //$NON-NLS-1$ //$NON-NLS-2$
            unZipFile("C:\\new.jar", "c:/unzipf/"); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
    }
}
