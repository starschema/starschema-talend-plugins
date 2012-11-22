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
package org.talend.core.repository.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.ICheckDeleteItemReference;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.ItemReferenceBean;
import org.talend.repository.ui.actions.DeleteActionCache;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public abstract class AbstractCheckDeleteItemReference implements ICheckDeleteItemReference {

    protected IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    protected List<? extends IRepositoryNode> deleteNodes;

    public Set<ItemReferenceBean> getItemReferenceBeans(List<? extends IRepositoryNode> deleteNodes,
            DeleteActionCache deleteActionCache) {
        this.deleteNodes = deleteNodes;
        Set<ItemReferenceBean> refBeans = new HashSet<ItemReferenceBean>();

        if (deleteActionCache == null) {
            deleteActionCache = DeleteActionCache.getInstance();
            deleteActionCache.createRecords();
        }

        for (IRepositoryNode repositoryNode : deleteNodes) {
            refBeans.addAll(checkItemReferenceBeans(factory, deleteActionCache, repositoryNode));
        }

        return refBeans;
    }

    protected abstract Set<ItemReferenceBean> checkItemReferenceBeans(IProxyRepositoryFactory factory,
            DeleteActionCache deleteActionCache, IRepositoryNode currentJobNode);

    protected boolean isItemInDeleteList(ItemReferenceBean bean, boolean isRefer) {
        if (deleteNodes == null) {
            return false;
        }

        String itemName;
        String itemVersion;
        ERepositoryObjectType itemType;
        for (IRepositoryNode node : deleteNodes) {
            IRepositoryViewObject repObj = node.getObject();
            Property property = repObj.getProperty();
            String label = property.getLabel();
            String version = property.getVersion();
            ERepositoryObjectType type = repObj.getRepositoryObjectType();
            if (isRefer) {
                itemName = bean.getReferenceItemName();
                itemVersion = bean.getReferenceItemVersion();
                itemType = bean.getReferenceItemType();
            } else {
                itemName = bean.getItemName();
                itemVersion = bean.getItemVersion();
                itemType = bean.getItemType();
            }
            if (label.equals(itemName) && version.equals(itemVersion) && type == itemType) {
                return true;
            }
        }

        return false;
    }

}
