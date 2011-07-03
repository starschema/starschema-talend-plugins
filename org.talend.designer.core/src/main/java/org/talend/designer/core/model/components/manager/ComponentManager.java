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
package org.talend.designer.core.model.components.manager;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.talend.commons.emf.EmfHelper;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.component_cache.ComponentCacheFactory;
import org.talend.core.model.component_cache.ComponentsCache;
import org.talend.core.model.component_cache.util.ComponentCacheResourceFactoryImpl;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
public class ComponentManager {

    private static ComponentsCache cache;

    private static final String TALEND_COMPONENT_CACHE = "ComponentsCache.";

    private static final String TALEND_FILE_NAME = "cache";

    public static ComponentsCache getInstance() {
        if (cache == null) {
            cache = ComponentCacheFactory.eINSTANCE.createComponentsCache();
        }
        return cache;
    }

    public static void saveResource() {
        String installLocation = new Path(Platform.getConfigurationLocation().getURL().getPath()).toFile().getAbsolutePath();
        try {
            Resource resource = createComponentCacheResource(installLocation);
            resource.getContents().add(cache);
            EmfHelper.saveResource(cache.eResource());
        } catch (PersistenceException e1) {
            // TODO Auto-generated catch block
            ExceptionHandler.process(e1);
        }
    }

    public static Resource createComponentCacheResource(String installLocation) {
        String filePath = ComponentManager.TALEND_COMPONENT_CACHE + LanguageManager.getCurrentLanguage().toString().toLowerCase()
                + ComponentManager.TALEND_FILE_NAME;
        URI uri = URI.createFileURI(installLocation).appendSegment(filePath);
        ComponentCacheResourceFactoryImpl compFact = new ComponentCacheResourceFactoryImpl();
        return compFact.createResource(uri);
    }
}
