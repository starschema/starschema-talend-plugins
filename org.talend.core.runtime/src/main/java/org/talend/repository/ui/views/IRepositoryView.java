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
package org.talend.repository.ui.views;

import java.util.List;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IViewPart;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;

/**
 * Defines all methods a repository view must provides. Actually the only view implementing this interface is
 * RepositoryView.<br/>
 * 
 * $Id: IRepositoryView.java 797 2006-11-30 16:16:52 +0000 (星期四, 30 十一月 2006) amaumont $
 * 
 */
public interface IRepositoryView extends IViewPart {

    public static final String VIEW_ID = "org.talend.repository.views.repository"; //$NON-NLS-1$

    public StructuredViewer getViewer();

    public void refresh();

    public void refresh(Object object);

    public void refreshView();

    /**
     * refresh the type of root node for creating and deleting items.
     * 
     */
    public void refresh(ERepositoryObjectType type);

    public void expand(Object object);

    public void expand(Object object, boolean state);

    public boolean getExpandedState(Object object);

    public IRepositoryNode getRoot();

    public List<IRepositoryViewObject> getAll(ERepositoryObjectType type);

    /** only for repository manager. */
    public void refreshAllChildNodes(RepositoryNode rootNode);

    public boolean containsRepositoryType(ERepositoryObjectType type);

    public String[] gatherMetadataChildenLabels();
}
