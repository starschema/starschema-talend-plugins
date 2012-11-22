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

import us.monoid.json.JSONObject;

/**
 * ggu class global comment. Detailled comment
 */
public abstract class AbstractTokenCollector implements ITokenCollector {

    protected static final TokenKey TOKEN_STUDIO = new TokenKey("tokenStudio"); //$NON-NLS-1$

    protected static final TokenKey PROPERTIES = new TokenKey("properties"); //$NON-NLS-1$

    public AbstractTokenCollector() {
        //
    }

    public void priorCollect() throws Exception {
        // default, nothing to do
    }

    public JSONObject collect() throws Exception {
        JSONObject result = new JSONObject();

        // tokenStudio
        JSONObject tokenStudioObject = new JSONObject();
        result.put(TOKEN_STUDIO.getKey(), tokenStudioObject);
        collectTokenStudio(tokenStudioObject);

        // properties
        JSONObject propertiesObject = new JSONObject();
        tokenStudioObject.put(PROPERTIES.getKey(), propertiesObject);
        collectProperties(propertiesObject);

        return result;
    }

    protected void collectTokenStudio(JSONObject tokenStudioObject) throws Exception {
        // will do something
    }

    protected void collectProperties(JSONObject propertiesObject) throws Exception {
        // will do something
    }
}
