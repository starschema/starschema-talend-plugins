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
package org.talend.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.runtime.i18n.Messages;

/**
 * DOC qian class global comment. A global service register provides the service registration and acquirement. <br/>
 * 
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (鏄熸湡浜� 29 涔?鏈�2006) nrousseau $
 * 
 */
// TODO remove this class and use OSGI dynamic services
public class GlobalServiceRegister {

    // The shared instance
    private static GlobalServiceRegister instance = new GlobalServiceRegister();

    private static IConfigurationElement[] configurationElements;

    private static IConfigurationElement[] configurationDQModelElements;

    public static GlobalServiceRegister getDefault() {
        return instance;
    }

    private Map<Class, IService> services = new HashMap<Class, IService>();

    private Map<Class<?>, AbstractDQModelService> dqModelServices = new HashMap<Class<?>, AbstractDQModelService>();

    static {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        configurationElements = registry.getConfigurationElementsFor("org.talend.core.runtime.service"); //$NON-NLS-1$
        configurationDQModelElements = registry.getConfigurationElementsFor("org.talend.core.runtime.dq_EMFModel_provider"); //$NON-NLS-1$
    }

    public AbstractDQModelService getDQModelService(Class<?> klass) {
        AbstractDQModelService dqModelserviceInst = dqModelServices.get(klass);
        if (dqModelserviceInst == null) {
            dqModelserviceInst = findDQModelService(klass);
            if (dqModelserviceInst != null) {
                dqModelServices.put(klass, dqModelserviceInst);
            }
        }
        return dqModelserviceInst;
    }

    public boolean isDQModelServiceRegistered(Class klass) {
        AbstractDQModelService service = dqModelServices.get(klass);
        if (service == null) {
            service = findDQModelService(klass);
            if (service == null) {
                return false;
            }
            dqModelServices.put(klass, service);
        }
        return true;
    }

    public boolean isServiceRegistered(Class klass) {
        IService service = services.get(klass);
        if (service == null) {
            service = findService(klass);
            if (service == null) {
                return false;
            }
            services.put(klass, service);
        }
        return true;
    }

    /**
     * DOC qian Comment method "getService".Gets the specific IService.
     * 
     * @param klass the Service type you want to get
     * @return IService IService
     */
    public IService getService(Class klass) {
        IService service = services.get(klass);
        if (service == null) {
            service = findService(klass);
            if (service == null) {
                throw new RuntimeException(Messages.getString("GlobalServiceRegister.ServiceNotRegistered", klass.getName())); //$NON-NLS-1$ //$NON-NLS-2$
            }
            services.put(klass, service);
        }
        return service;
    }

    /**
     * DOC qian Comment method "findService".Finds the specific service from the list.
     * 
     * @param klass the interface type want to find.
     * @return IService
     */
    private IService findService(Class klass) {
        String key = klass.getName();
        for (int i = 0; i < configurationElements.length; i++) {
            IConfigurationElement element = configurationElements[i];
            if (element.isValid()) {
                String id = element.getAttribute("serviceId"); //$NON-NLS-1$
                if (!key.endsWith(id)) {
                    continue;
                }
                try {
                    Object service = element.createExecutableExtension("class"); //$NON-NLS-1$
                    if (klass.isInstance(service)) {
                        return (IService) service;
                    }
                } catch (CoreException e) {
                    ExceptionHandler.process(e);
                }
            }// else element is not valid because the bundle may have been stoped or uninstalled and the extension point
             // registry is still holding values
             // has mentionned in the class TODO, this class should be removed and OSGI dynamic services used.
        }
        return null;
    }

    private AbstractDQModelService findDQModelService(Class<?> klass) {
        for (int i = 0; i < configurationDQModelElements.length; i++) {
            IConfigurationElement element = configurationDQModelElements[i];
            try {
                Object service = element.createExecutableExtension("class"); //$NON-NLS-1$
                if (klass.isInstance(service)) {
                    return (AbstractDQModelService) service;
                }
            } catch (CoreException e) {
                ExceptionHandler.process(e);
            }
        }
        return null;
    }
}
