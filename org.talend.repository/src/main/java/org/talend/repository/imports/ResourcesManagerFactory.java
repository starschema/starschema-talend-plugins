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
package org.talend.repository.imports;

import java.util.zip.ZipFile;

/**
 */
public class ResourcesManagerFactory {

    private static ResourcesManagerFactory instance = new ResourcesManagerFactory();

    public ProviderManager createResourcesManager(Object provider) {
        return new ProviderManager(provider);
    }

    public ZipFileManager createResourcesManager(ZipFile zipFile) {
        return new ZipFileManager(zipFile);
    }

    public FilesManager createResourcesManager() {
        return new FilesManager();
    }

    public static ResourcesManagerFactory getInstance() {
        return instance;
    }
}
