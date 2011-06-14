// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.model.actions;

import org.eclipse.core.runtime.IPath;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: RestoreObjectAction.java 58859 2011-04-20 07:10:27Z wzhang $
 * 
 */
public class RestoreObjectAction {

    private static RestoreObjectAction singleton = new RestoreObjectAction();

    public static RestoreObjectAction getInstance() {
        return singleton;
    }

    public boolean validateAction(RepositoryNode sourceNode, RepositoryNode targetNode) {
        MoveObjectAction moveObjectAction = MoveObjectAction.getInstance();
        if (!moveObjectAction.validateAction(sourceNode, targetNode, false)) {
            return false;
        }

        if (sourceNode.getType() == ENodeType.REPOSITORY_ELEMENT || sourceNode.getType() == ENodeType.SIMPLE_FOLDER) {
            IRepositoryViewObject objectToRestore = sourceNode.getObject();
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            return factory.getStatus(objectToRestore) == ERepositoryStatus.DELETED;
        } else {
            return false;
        }
    }

    public void execute(RepositoryNode sourceNode, RepositoryNode targetNode, IPath folderPath) throws Exception {
        if (!validateAction(sourceNode, targetNode)) {
            return;
        }
        MoveObjectAction moveObjectAction = MoveObjectAction.getInstance();
        moveObjectAction.execute(sourceNode, targetNode, folderPath, false);
    }
}
