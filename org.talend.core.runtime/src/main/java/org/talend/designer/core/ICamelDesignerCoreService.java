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
package org.talend.designer.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorPart;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.core.IService;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
public interface ICamelDesignerCoreService extends IService {

    public IAction getCreateProcessAction(boolean isToolbar);

    public IAction getCreateBeanAction(boolean isToolbar);

    public ERepositoryObjectType createCamelResource(Item item);

    public Resource createCamel(IProject project, Item item, IPath path, ERepositoryObjectType type) throws PersistenceException;

    public Resource saveCamel(Item item) throws PersistenceException;

    public IImage getCamelIcon(ERepositoryObjectType type);

    public boolean isInstanceofCamelRoutes(Item item);

    public ERepositoryObjectType getRoutes();

    public ProcessType getCamelProcessType(Item item);

    public Item createNewCamelItem(ERepositoryObjectType type);

    public ITalendSynchronizer createCamelJavaSynchronizer();

    public ITalendSynchronizer createCamelPerlSynchronizer();

    public ERepositoryObjectType getBeansType();

    public boolean isInstanceofCamel(Item item);

    public boolean isInstanceofCamelBeans(Item item);

    public boolean isCamelRepObjType(ERepositoryObjectType type);

    public boolean isCamelMulitPageEditor(IEditorPart editor);
}
