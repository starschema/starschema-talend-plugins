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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.ui.IViewPart;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.utils.sugars.ReturnCode;

/**
 * DOC bZhou class global comment. Detailled comment
 */
public interface ITDQRepositoryService extends IService {

    public static final String RULE_VALUE = "RULE_VALUE"; //$NON-NLS-1$

    public static final String RULE_TYPE = "RULE_TYPE"; //$NON-NLS-1$

    public static final String RULE_NAME = "RULE_NAME"; //$NON-NLS-1$

    public static final String RULE_TABLE = "RULE_TABLE";//$NON-NLS-1$

    public IViewPart getTDQRespositoryView();

    public void openEditor(Item item);

    public void notifySQLExplorer(Item... items);

    public void fillMetadata(ConnectionItem connItem);

    public void refresh();

    // Added 20120503 yyin
    public void refresh(Object refreshObject);

    public void initProxyRepository();

    public void addPartListener();
    //MOD klliu 2011-04-28 bug 20204 removing connection is synced to the connection view of SQL explore 
    public boolean removeAliasInSQLExplorer(IRepositoryNode children);
    public void createParserRuleItem(ArrayList<HashMap<String, Object>> values, String parserRuleName);

    public List<Map<String, String>> getPaserRulesFromResources(Object[] rules);

    public List<Map<String, String>> getPaserRulesFromRules(Object parser);

    // ADD qiongli 2011-9-13 TDQ-3317
    public void reloadDatabase(ContextItem contextItem);

    // ADD zshen 2012-1-6 TDQ-4384
    public ReturnCode reloadDatabase(ConnectionItem connectionItem);

    // ADD qiongli 2011-9-13 TDQ-3797
    public void updateImpactOnAnalysis(ConnectionItem connectionItem);

    // ADD qiongli 2011-9-13 TDQ-3930
    public boolean confirmUpdateAnalysis(ConnectionItem connectionItem);

    // ADD qiongli 2012-4-19 TDQ-5130
    public boolean hasClientDependences(ConnectionItem connectionItem);

    // ADD zshen 2012-5-9 TDQ-4738
    public int confimDelete(IRepositoryViewObject deleteObject);

    // ADD zshen 2012-5-9 TDQ-4738
    // public boolean deleteObjectPhysical(IRepositoryViewObject deleteObject, Item newItem);

    // ADD zshen 2012-5-9 TDQ-4738
    public InputDialog getInputDialog(final Item newItem);

    // ADD zshen 2012-5-9 TDQ-4738
    public void changeElementName(Item newItem, String newName);

    // ADD xqliu 2012-05-25 TDQ-4831
    public boolean sourceFileOpening(RepositoryNode node);
}
