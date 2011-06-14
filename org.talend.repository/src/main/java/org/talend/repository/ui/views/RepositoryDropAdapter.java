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
package org.talend.repository.ui.views;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.part.PluginDropAdapter;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.CorePlugin;
import org.talend.core.model.business.BusinessType;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.actions.CopyObjectAction;
import org.talend.repository.model.actions.MoveObjectAction;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: RepositoryDropAdapter.java 61248 2011-05-27 07:36:06Z hwang $
 * 
 */
public class RepositoryDropAdapter extends PluginDropAdapter {

    public RepositoryDropAdapter(StructuredViewer viewer) {
        super(viewer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.PluginDropAdapter#performDrop(java.lang.Object)
     */
    @Override
    public boolean performDrop(final Object data) {
        int operation = getCurrentOperation();
        final RepositoryNode targetNode = (RepositoryNode) getCurrentTarget();
        boolean toReturn = true;

        try {
            RepositoryWorkUnit<Object> repositoryWorkUnit = null;
            switch (operation) {
            case DND.DROP_COPY:
                String copyName = "User action : Copy Object"; //$NON-NLS-1$
                repositoryWorkUnit = new RepositoryWorkUnit<Object>(copyName, CopyObjectAction.getInstance()) {

                    @Override
                    protected void run() throws LoginException, PersistenceException {
                        try {
                            for (Object obj : ((StructuredSelection) data).toArray()) {
                                final RepositoryNode sourceNode = (RepositoryNode) obj;
                                CopyObjectAction.getInstance().execute(sourceNode, targetNode);
                            }
                        } catch (Exception e) {
                            throw new PersistenceException(e);
                        }
                    }
                };
                ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(repositoryWorkUnit);
                break;
            case DND.DROP_MOVE:
                String moveName = "User action : Move Object"; //$NON-NLS-1$
                repositoryWorkUnit = new RepositoryWorkUnit<Object>(moveName, MoveObjectAction.getInstance()) {

                    @Override
                    protected void run() throws LoginException, PersistenceException {
                        try {
                            for (Object obj : ((StructuredSelection) data).toArray()) {
                                final RepositoryNode sourceNode = (RepositoryNode) obj;
                                MoveObjectAction.getInstance().execute(sourceNode, targetNode, true);
                            }
                        } catch (Exception e) {
                            throw new PersistenceException(e);
                        }
                    }
                };
                ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(repositoryWorkUnit);
                break;
            default:
                // Nothing to do
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        return toReturn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.PluginDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
        super.validateDrop(target, operation, transferType);

        boolean isValid = true;
        for (Object obj : ((StructuredSelection) getViewer().getSelection()).toArray()) {
            RepositoryNode sourceNode = (RepositoryNode) obj;

            if (sourceNode != null) {
                IRepositoryViewObject object = sourceNode.getObject();
                if (object == null) {
                    return false;
                }
                if (object.getType() == ERepositoryObjectType.JOB_DOC || object.getType() == ERepositoryObjectType.JOBLET_DOC) {
                    if (BusinessType.SHAP == CorePlugin.getDefault().getDiagramModelService().getBusinessModelType(target)) {
                        return true;
                    }
                    return false;
                } else if (object.getType() == ERepositoryObjectType.ROUTINES) {
                    Property property = object.getProperty();
                    RoutineItem item = (RoutineItem) property.getItem();
                    if (item.isBuiltIn() && target instanceof RepositoryNode) {
                        return false;
                    }
                } else if (object.getType() == ERepositoryObjectType.SQLPATTERNS) {
                    Property property = object.getProperty();
                    SQLPatternItem item = (SQLPatternItem) property.getItem();
                    if (item.isSystem() && target instanceof RepositoryNode) {
                        return false;
                    }
                }

            }

            switch (operation) {
            case DND.DROP_COPY:
                isValid = CopyObjectAction.getInstance().validateAction(sourceNode, (RepositoryNode) target);
                break;
            case DND.DROP_NONE:
            case DND.DROP_MOVE:
                isValid = MoveObjectAction.getInstance().validateAction(sourceNode, (RepositoryNode) target, true);
                break;
            case DND.DROP_DEFAULT:
            case DND.Drop: // hywang
                isValid = MoveObjectAction.getInstance().validateAction(sourceNode, (RepositoryNode) target, true);
                boolean isLock = false;
                isLock = MoveObjectAction.getInstance().isLock(sourceNode);
                if (isLock) {
                    isValid = false;
                }
                break;
            default:
                isValid = false;
            }

            if (!isValid) {
                return isValid;
            }
        }

        return isValid;
    }
}
