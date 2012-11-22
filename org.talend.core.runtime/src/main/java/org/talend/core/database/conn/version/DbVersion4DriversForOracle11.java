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
package org.talend.core.database.conn.version;

import java.util.HashSet;
import java.util.Set;

import org.talend.core.database.EDatabaseTypeName;

/**
 * cli class global comment. Detailled comment
 */
public class DbVersion4DriversForOracle11 extends DbVersion4Drivers {

    static final String DRIVER_1_6 = "ojdbc6-11g.jar"; //$NON-NLS-1$

    static final String DRIVER_1_5 = "ojdbc5-11g.jar"; //$NON-NLS-1$

    DbVersion4DriversForOracle11(EDatabaseTypeName[] dbTypes, String versionDisplayName, String versionValue, String[] drivers) {
        super(dbTypes, versionDisplayName, versionValue, drivers);
    }

    @Override
    Set<String> getDrivers() {
        Set<String> drivers = new HashSet<String>();
        if (System.getProperty("java.version").startsWith("1.6")) { //$NON-NLS-1$ //$NON-NLS-2$
            drivers.add(DRIVER_1_6);
        } else {
            drivers.add(DRIVER_1_5);
        }
        return drivers;
    }

}
