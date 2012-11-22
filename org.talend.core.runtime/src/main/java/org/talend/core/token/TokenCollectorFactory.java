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
package org.talend.core.token;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.commons.utils.network.NetworkUtil;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.ui.branding.IBrandingService;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.AbstractContent;
import us.monoid.web.FormData;
import us.monoid.web.Resty;
import us.monoid.web.TextResource;
import us.monoid.web.mime.MultipartContent;

/**
 * ggu class global comment. Detailled comment
 */
public final class TokenCollectorFactory {

    private static Logger log = Logger.getLogger(TokenCollectorFactory.class);

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$

    private static TokenCollectorFactory factory;

    private static final Map<String, TokenInforProvider> providers;
    static {
        providers = new HashMap<String, TokenInforProvider>();

        Map<String, String> idWithPluginMap = new HashMap<String, String>();
        //
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IConfigurationElement[] configurationElements = registry
                .getConfigurationElementsFor("org.talend.core.runtime.tokenInfo_provider"); //$NON-NLS-1$
        for (IConfigurationElement ce : configurationElements) {
            String pluginName = ce.getContributor().getName();

            String id = ce.getAttribute("id"); //$NON-NLS-1$
            String name = ce.getAttribute("name"); //$NON-NLS-1$
            String description = ce.getAttribute("description"); //$NON-NLS-1$
            String productType = ce.getAttribute("productType"); //$NON-NLS-1$
            ITokenCollector collector = null;
            try {
                collector = (ITokenCollector) ce.createExecutableExtension("collector"); //$NON-NLS-1$
            } catch (CoreException e) {
                log.log(Priority.ERROR, "Can't create the collector for id: " + id + " in plugin:" + pluginName, e); //$NON-NLS-1$ //$NON-NLS-2$
            }

            TokenInforProvider provider = new TokenInforProvider(id, name, description, productType, collector);
            if (!providers.containsKey(id)) {
                providers.put(id, provider);
                idWithPluginMap.put(id, pluginName);
            } else {
                log.log(Priority.WARN, "there is  id: " + id + " to have been existed in plugin:" + idWithPluginMap.get(id) //$NON-NLS-1$ //$NON-NLS-2$
                        + " （current plugin is:" + pluginName + "）， will ignore this extension."); //$NON-NLS-1$ //$NON-NLS-2$
            }

        }
    }

    public static TokenCollectorFactory getFactory() {
        if (factory == null) {
            factory = new TokenCollectorFactory();
        }
        return factory;
    }

    public TokenInforProvider[] getProviders() {
        return providers.values().toArray(new TokenInforProvider[0]);
    }

    public void priorCollect() throws Exception {
        if (isActiveAndValid(false)) { //
            for (TokenInforProvider tip : getProviders()) {
                ITokenCollector collector = tip.getCollector();
                if (collector != null) {
                    collector.priorCollect();
                }
            }
        }
    }

    public JSONObject collectTokenInfors() throws Exception {
        JSONObject result = new JSONObject();

        for (TokenInforProvider tip : getProviders()) {
            ITokenCollector collector = tip.getCollector();
            if (collector != null) {
                JSONObject collectionObject = collector.collect();
                if (collectionObject != null) {
                    TokenInforUtil.integrateJSONObject(result, collectionObject);
                }
            }
        }

        return result;
    }

    private boolean isActiveAndValid(boolean timeExpired) {
        final IPreferenceStore preferenceStore = CoreRuntimePlugin.getInstance().getPreferenceStore();
        if (preferenceStore.getBoolean(ITalendCorePrefConstants.DATA_COLLECTOR_ENABLED)) {
            String last = preferenceStore.getString(ITalendCorePrefConstants.DATA_COLLECTOR_LAST_TIME);
            int days = preferenceStore.getInt(ITalendCorePrefConstants.DATA_COLLECTOR_UPLOAD_PERIOD);
            Date lastDate = null;
            if (last != null && !"".equals(last.trim())) { //$NON-NLS-1$
                // parse the last date;
                try {
                    lastDate = DATE_FORMAT.parse(last);
                } catch (ParseException ee) {
                    //
                }
            }
            Date curDate = new Date();
            Date addedDate = curDate;
            if (days > 0 && lastDate != null) {
                addedDate = TokenInforUtil.getDateAfter(lastDate, days);
            }
            //
            if (timeExpired) {
                if (addedDate.compareTo(curDate) <= 0) {
                    return true;
                }
            } else {
                return true; // only check active
            }
        }
        return false;

    }

    public boolean process() {
        boolean result = false;

        if (isActiveAndValid(true)) {
            send();
            // set new days
            final IPreferenceStore preferenceStore = CoreRuntimePlugin.getInstance().getPreferenceStore();
            preferenceStore.setValue(ITalendCorePrefConstants.DATA_COLLECTOR_LAST_TIME, DATE_FORMAT.format(new Date()));
            result = true;
        }
        return result;
    }

    public boolean send() {
        try {
            boolean isPoweredbyTalend = false;

            if (GlobalServiceRegister.getDefault().isServiceRegistered(IBrandingService.class)) {
                IBrandingService service = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                        IBrandingService.class);
                isPoweredbyTalend = service.isPoweredbyTalend();
            }
            if (isPoweredbyTalend && NetworkUtil.isNetworkValid()) {
                JSONObject tokenInfors = collectTokenInfors();

                Resty r = new Resty();

                AbstractContent ac = Resty.content(tokenInfors);
                MultipartContent mpc = Resty.form(new FormData("data", ac)); //$NON-NLS-1$

                TextResource result = r.text("http://www.talend.com/TalendRegisterWS/tokenstudio.php", mpc); //$NON-NLS-1$
                try {
                    String resultStr = new JSONObject(result.toString()).getString("result"); //$NON-NLS-1$
                    return (resultStr != null && resultStr.endsWith("OK")); //$NON-NLS-1$
                } catch (JSONException je) {
                    //
                }
            }
        } catch (Exception e) {
            // if want to test, enable this
            // ExceptionHandler.process(e);
        }
        return false;
    }
}
