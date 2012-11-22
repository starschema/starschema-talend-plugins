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
package org.talend.core.model.repository;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.core.model.properties.Item;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
public interface IRepositoryContentHandler {

    public Resource create(IProject project, Item item, int classifierID, IPath path) throws PersistenceException;

    public Resource save(Item item) throws PersistenceException;

    public IImage getIcon(ERepositoryObjectType type);

    public Item createNewItem(ERepositoryObjectType type);

    public boolean isProcess(Item item);

    public boolean isRepObjType(ERepositoryObjectType type);

    public ERepositoryObjectType getProcessType();

    public ERepositoryObjectType getCodeType();

    public ERepositoryObjectType getRepositoryObjectType(Item item);

    public void addNode(ERepositoryObjectType type, RepositoryNode recBinNode, IRepositoryViewObject repositoryObject,
            RepositoryNode node);

    public void addContents(Collection<EObject> collection, Resource resource);

    public IImage getIcon(Item item);

}
