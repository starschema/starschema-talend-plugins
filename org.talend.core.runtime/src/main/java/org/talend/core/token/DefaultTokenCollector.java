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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.talend.commons.utils.VersionUtils;
import org.talend.commons.utils.network.NetworkUtil;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.utils.security.CryptoHelper;

import us.monoid.json.JSONObject;

/**
 * ggu class global comment. Detailled comment
 */
public class DefaultTokenCollector extends AbstractTokenCollector {

    private static final TokenKey VERSION = new TokenKey("version"); //$NON-NLS-1$

    private static final TokenKey UNIQUE_ID = new TokenKey("uniqueId"); //$NON-NLS-1$

    private static final TokenKey TYPE_STUDIO = new TokenKey("typeStudio"); //$NON-NLS-1$

    public DefaultTokenCollector() {
        super();
    }

    @Override
    protected void collectTokenStudio(JSONObject tokenStudioObject) throws Exception {
        // version
        tokenStudioObject.put(VERSION.getKey(), VersionUtils.getVersion());
        // uniqueId
        tokenStudioObject.put(UNIQUE_ID.getKey(), calcUniqueId());
        // typeStudio
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IBrandingService.class)) {
            IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                    IBrandingService.class);
            tokenStudioObject.put(TYPE_STUDIO.getKey(), brandingService.getAcronym());
            // tokenStudioObject.put(TYPE_STUDIO.getKey(), brandingService.getShortProductName());
        }

    }

    public static String calcUniqueId() {
        StringBuffer sb = new StringBuffer();
        String macAddress = NetworkUtil.getMacAddress();
        if (macAddress != null) {
            sb.append(macAddress);
            sb.append('-');
        }
        String osName = System.getProperty("os.name"); //$NON-NLS-1$
        String osVersion = System.getProperty("os.version"); //$NON-NLS-1$
        sb.append(osName);
        sb.append(osVersion);
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            sb.append('-');
            sb.append(hostName);
        } catch (UnknownHostException e) {
            //
        }
        CryptoHelper cryptoHelper = new CryptoHelper(ConnectionHelper.PASSPHRASE);
        return cryptoHelper.encrypt(sb.toString());
    }
}
