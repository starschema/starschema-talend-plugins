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

package org.talend.repository.ui.wizards.exportjob.scriptsmanager.petals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * A set of utility methods used to create a Petals service assembly for the Talend SE.
 * 
 * @author Vincent Zurczak - EBM WebSourcing
 */
public class SaUtils {

    /**
     * Creates a service assembly file for the Petals Talend SE.
     * <p>
     * The SU name and file name will be computed from the SA file path.
     * </p>
     * 
     * @param suFile the SU file
     * @param saFilePath the path of the target SA
     * @param jobDescription the job description (not null)
     * @return the created SA file, or null if the creation failed
     */
    public static File createSaForTalend(File suFile, String saFilePath, String jobDescription) {

        IPath saPath = new Path(saFilePath);
        String saName = saPath.lastSegment();
        if (saName.endsWith(".zip")) //$NON-NLS-1$
            saName = saName.substring(0, saName.length() - 4);

        String suName = saName.replace("sa-", "su-"); //$NON-NLS-1$ //$NON-NLS-2$
        String jbiXmlContent = generateJbiXmlForTalendSA(saName, jobDescription, suName);
        Map<String, File> entries = new HashMap<String, File>(1);
        entries.put(suName + ".zip", suFile); //$NON-NLS-1$

        File saFile = null;
        try {
            saFile = createJbiArchive(saFilePath, jbiXmlContent, entries);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return saFile;
    }

    /**
     * Creates the jbi.xml content for a Petals SA.
     * 
     * @param saName the SA name (not the service assembly file name).
     * <p>
     * Service assembly names look like <i>sa-Jsr181-&lt;serviceName&gt;-provide</i>.
     * </p>
     * 
     * @param jobDescription the job description (not null)
     * 
     * @param suNames the SU names (not the service-unit file names).
     * <p>
     * Service unit names look like <i>su-Jsr181-&lt;serviceName&gt;-provide</i>.
     * </p>
     * 
     * @return the jbi.xml content
     */
    public static String generateJbiXmlForTalendSA(String saName, String jobDescription, String... suNames) {

        // Content of the jbi.xml file
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
        sb.append("<jbi:jbi version=\"1.0\"\n"); //$NON-NLS-1$
        sb.append("\txmlns=\"http://java.sun.com/xml/ns/jbi\"\n"); //$NON-NLS-1$
        sb.append("\txmlns:jbi=\"http://java.sun.com/xml/ns/jbi\">\n\n"); //$NON-NLS-1$

        sb.append("\t<jbi:service-assembly>\n\t\t<jbi:identification>\n"); //$NON-NLS-1$
        sb.append("\t\t\t<jbi:name>" + saName + "</jbi:name>\n"); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append("\t\t\t<jbi:description>" + jobDescription + "</jbi:description>\n"); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append("\t\t</jbi:identification>\n"); //$NON-NLS-1$

        for (String suName : suNames) {

            sb.append("\n\t\t<!-- New service-unit -->\n"); //$NON-NLS-1$
            sb.append("\t\t<jbi:service-unit>\n\t\t\t<jbi:identification>\n"); //$NON-NLS-1$
            sb.append("\t\t\t\t<jbi:name>" + suName + "</jbi:name>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("\t\t\t\t<jbi:description>" + jobDescription + "</jbi:description>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("\t\t\t</jbi:identification>\n\n"); //$NON-NLS-1$

            sb.append("\t\t\t<jbi:target>\n"); //$NON-NLS-1$
            sb.append("\t\t\t\t<jbi:artifacts-zip>" + suName + ".zip</jbi:artifacts-zip>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("\t\t\t\t<jbi:component-name>petals-se-talend</jbi:component-name>\n"); //$NON-NLS-1$
            sb.append("\t\t\t</jbi:target>\n\t\t</jbi:service-unit>\n"); //$NON-NLS-1$
        }

        sb.append("\t</jbi:service-assembly>\n"); //$NON-NLS-1$
        sb.append("</jbi:jbi>"); //$NON-NLS-1$

        return sb.toString();
    }

    /**
     * Creates a service-unit or a service assembly file.
     * 
     * @param targetZipFile the path of the target ZIP file
     * @param jbiXmlContent the jbi.xml file content, or null to not add a jbi.xml
     * @param entries key = zip entry, value = file to zip (not null)
     * @return the created archive in case of success, null otherwise
     * @throws IOException
     */
    public static File createJbiArchive(String targetZipFile, String jbiXmlContent, Map<String, File> entries) throws IOException {

        File zipFile = new File(targetZipFile);
        File temporaryFile = File.createTempFile("petalsTempFile-", null, null); //$NON-NLS-1$
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(temporaryFile));

        // Files
        for (Map.Entry<String, File> entry : entries.entrySet()) {
            if (entry.getValue().exists()) {
                InputStream in = new FileInputStream(entry.getValue());
                addFileToZip(zos, in, entry.getKey());
            }
        }

        // JBI descriptor
        if (jbiXmlContent != null) {
            ByteArrayInputStream in = new ByteArrayInputStream(jbiXmlContent.getBytes());
            addFileToZip(zos, in, "META-INF/jbi.xml"); //$NON-NLS-1$
        }

        zos.close();
        if (zipFile.exists() && !zipFile.delete())
            zipFile.deleteOnExit();

        if (!temporaryFile.renameTo(zipFile))
            throw new IOException("Could not move temporary archive to target destination."); //$NON-NLS-1$

        return zipFile;
    }

    /**
     * Adds a file into a zip archive.
     * 
     * @param out the zip file
     * @param in the input stream to add into the zip
     * @param entry the entry for the file to add into the zip (not null)
     */
    private static void addFileToZip(ZipOutputStream out, InputStream in, String entry) {

        byte[] buf = new byte[1024]; // Create a buffer for reading the files
        ZipEntry ze = new ZipEntry(entry);
        try {
            out.putNextEntry(ze);
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.closeEntry();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
