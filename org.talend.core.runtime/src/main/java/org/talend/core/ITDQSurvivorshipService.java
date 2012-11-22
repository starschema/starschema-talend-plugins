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
package org.talend.core;

import org.talend.core.model.process.INode;

/**
 * Interface of TDQSurvivorshipService
 */
public interface ITDQSurvivorshipService extends IService {

    /**
     * Create rule file items in repository folder named "metadata/survivorship" according to the configuration of
     * tRuleSurvivorship Component.
     * 
     * @param node
     */
    public void createSurvivorshipItems(INode node);
}
