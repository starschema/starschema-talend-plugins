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

import org.eclipse.ui.IViewPart;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.repository.model.IRepositoryNode;

/**
 * DOC bZhou class global comment. Detailled comment
 */
public interface ITDQRepositoryService extends IService {

    public IViewPart getTDQRespositoryView();

    public void openEditor(Item item);

    public void notifySQLExplorer(Item... items);

    public void fillMetadata(ConnectionItem connItem);

    public void refresh();

    public void initProxyRepository();

    public void addPartListener();
    //MOD klliu 2011-04-28 bug 20204 removing connection is synced to the connection view of SQL explore 
    public boolean removeAliasInSQLExplorer(IRepositoryNode children);

}
