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
package org.talend.repository.imports;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

/**
 */
public class FilesManager extends ResourcesManager {

    public InputStream getStream(IPath path) throws IOException {
        return new BufferedInputStream(new FileInputStream((File) path2Object.get(path)));
    }

    public boolean collectPath2Object(Object root) {
        return doCollectItemFiles((File) root);
    }

    private boolean doCollectItemFiles(File directory) {
        File[] contents = directory.listFiles();

        if (contents != null) {
            for (int i = 0; i < contents.length; i++) {
                File file = contents[i];

                if (file.isFile()) {
                    add(file.getAbsolutePath(), file);
                }
                if (file.isDirectory()) {
                    if (!file.getName().equals(WizardProjectsImportPage.METADATA_FOLDER) && (!file.getName().equals(".svn"))) {
                        collectPath2Object(contents[i]);
                    }
                }
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.localprovider.imports.ResourcesManager#getProvider()
     */
    @Override
    public IImportStructureProvider getProvider() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.localprovider.imports.ResourcesManager#closeResource()
     */
    @Override
    public void closeResource() {
        // TODO Auto-generated method stub

    }
}
