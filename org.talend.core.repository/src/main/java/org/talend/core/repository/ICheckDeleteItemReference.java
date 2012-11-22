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
package org.talend.core.repository;

import java.util.List;
import java.util.Set;

import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.ItemReferenceBean;
import org.talend.repository.ui.actions.DeleteActionCache;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public interface ICheckDeleteItemReference {

    public Set<ItemReferenceBean> getItemReferenceBeans(List<? extends IRepositoryNode> deleteNodes,
            DeleteActionCache deleteActionCache);

}
