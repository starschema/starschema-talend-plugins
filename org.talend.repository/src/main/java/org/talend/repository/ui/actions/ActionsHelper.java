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
package org.talend.repository.ui.actions;

import java.util.List;

import org.talend.commons.ui.swt.actions.ITreeContextualAction;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: ActionsHelper.java 54939 2011-02-11 01:34:57Z mhirt $
 * 
 */
public class ActionsHelper {

    public static List<ITreeContextualAction> getRepositoryContextualsActions() {
        return org.talend.core.ui.actions.ActionsHelper.getRepositoryContextualsActions();
    }

}
