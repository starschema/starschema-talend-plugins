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
package org.talend.core.repository.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.ICheckDeleteItemReference;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.ItemReferenceBean;
import org.talend.repository.ui.actions.DeleteActionCache;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class RepositoryNodeDeleteManager {

    private static RepositoryNodeDeleteManager instance;

    public static final String EXTENTION_ID = "org.talend.core.repository.checkDeleteItemReference"; //$NON-NLS-1$

    private static final String ATT_CLASS = "class"; //$NON-NLS-1$

    private static final String ATT_PRIORITY = "priority"; //$NON-NLS-1$

    private static IConfigurationElement[] configurationElements;

    private RepositoryNodeDeleteManager() {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        configurationElements = registry.getConfigurationElementsFor(EXTENTION_ID);
    }

    public static synchronized RepositoryNodeDeleteManager getInstance() {
        if (instance == null) {
            instance = new RepositoryNodeDeleteManager();
        }

        return instance;
    }

    static class DeleteCheck {

        public final ICheckDeleteItemReference checkDeleteItemReference;

        public final Priority priority;

        enum Priority {
            LOWEST,
            LOW,
            NORMAL,
            HIGH,
            HIGHEST
        }

        public DeleteCheck(ICheckDeleteItemReference checkDeleteItemReference, Priority priority) {
            this.checkDeleteItemReference = checkDeleteItemReference;
            this.priority = priority;
        }

    }

    public List<ItemReferenceBean> getUnDeleteItems(List<? extends IRepositoryNode> deleteObjs,
            DeleteActionCache deleteActionCache) {
        return getUnDeleteItems(deleteObjs, deleteActionCache, true);
    }

    @SuppressWarnings("unchecked")
    public List<ItemReferenceBean> getUnDeleteItems(List<? extends IRepositoryNode> deleteNodes,
            DeleteActionCache deleteActionCache, boolean updateDelList) {
        List<ItemReferenceBean> beans = new ArrayList<ItemReferenceBean>();
        Set<ItemReferenceBean> refBeans = new HashSet<ItemReferenceBean>();
        List<DeleteCheck> deleteChecks = new ArrayList<DeleteCheck>();

        if (deleteNodes == null || deleteNodes.size() == 0) {
            return beans;
        }

        try {
            for (IConfigurationElement element : configurationElements) {
                ICheckDeleteItemReference checkDeleteItemReference = (ICheckDeleteItemReference) element
                        .createExecutableExtension(ATT_CLASS);
                String priorityStr = element.getAttribute(ATT_PRIORITY);
                DeleteCheck.Priority priority = (priorityStr != null && priorityStr.length() > 0) ? DeleteCheck.Priority
                        .valueOf(priorityStr.toUpperCase()) : DeleteCheck.Priority.NORMAL;
                DeleteCheck deleteCheck = new DeleteCheck(checkDeleteItemReference, priority);
                if (!deleteChecks.contains(deleteCheck)) {
                    deleteChecks.add(deleteCheck);
                }
            }
            sortDeleteChecks(deleteChecks);
            for (DeleteCheck deleteCheck : deleteChecks) {
                refBeans.addAll(deleteCheck.checkDeleteItemReference.getItemReferenceBeans(deleteNodes, deleteActionCache));
            }

            MultiKeyMap item2beansMap = new MultiKeyMap();
            for (ItemReferenceBean refBean : refBeans) {
                List<ItemReferenceBean> beansList = (List<ItemReferenceBean>) item2beansMap.get(refBean.getItemName(),
                        refBean.getItemVersion(), refBean.getItemType());
                if (beansList == null) {
                    beansList = new ArrayList<ItemReferenceBean>();
                    item2beansMap.put(refBean.getItemName(), refBean.getItemVersion(), refBean.getItemType(), beansList);
                }
                if (!beansList.contains(refBean)) {
                    beansList.add(refBean);
                }
            }

            Iterator it = item2beansMap.keySet().iterator();
            while (it.hasNext()) {
                Object obj = it.next();
                if (obj instanceof MultiKey) {
                    Object[] keys = ((MultiKey) obj).getKeys();
                    if (keys.length >= 3) {
                        String itemName = String.valueOf(keys[0]);
                        String itemVersion = String.valueOf(keys[1]);
                        ERepositoryObjectType itemType = (ERepositoryObjectType) keys[2];
                        ItemReferenceBean parentBean = new ItemReferenceBean();
                        parentBean.setItemName(itemName);
                        parentBean.setItemVersion(itemVersion);
                        parentBean.setItemType(itemType);
                        parentBean.setHost(true);
                        parentBean.addChildren((List<ItemReferenceBean>) item2beansMap.get(obj));
                        beans.add(parentBean);
                    }
                }
            }
            sortReferenceBeans(beans);

            if (updateDelList) {
                List<String> unDeleteItemNames = new ArrayList<String>();
                for (ItemReferenceBean bean : beans) {
                    unDeleteItemNames.add(bean.getItemName());
                }
                Iterator<? extends IRepositoryNode> nodeIter = deleteNodes.iterator();
                while (nodeIter.hasNext()) {
                    IRepositoryNode node = nodeIter.next();
                    Object label = node.getProperties(EProperties.LABEL);
                    if (unDeleteItemNames.contains(label)) {
                        nodeIter.remove();
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        return beans;
    }

    private void sortDeleteChecks(List<DeleteCheck> deleteChecks) {
        Collections.sort(deleteChecks, new Comparator<DeleteCheck>() {

            @Override
            public int compare(DeleteCheck check1, DeleteCheck check2) {
                return check1.priority.compareTo(check2.priority);
            }
        });
    }

    private void sortReferenceBeans(List<ItemReferenceBean> beans) {
        Collections.sort(beans, new Comparator<ItemReferenceBean>() {

            @Override
            public int compare(ItemReferenceBean bean1, ItemReferenceBean bean2) {
                int ordinal1 = bean1.getItemType().ordinal();
                int ordinal2 = bean2.getItemType().ordinal();
                if (ordinal1 > ordinal2) {
                    return 1;
                } else if (ordinal1 < ordinal2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }

}
