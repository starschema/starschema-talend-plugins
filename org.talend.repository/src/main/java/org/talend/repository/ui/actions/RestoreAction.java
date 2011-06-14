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
package org.talend.repository.ui.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.image.EImage;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.metadata.builder.connection.AbstractMetadataObject;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.cwm.helper.SubItemHelper;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.ISubRepositoryObject;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.actions.RestoreObjectAction;
import org.talend.repository.ui.views.IRepositoryView;
import org.talend.repository.utils.AbstractResourceChangesService;
import org.talend.repository.utils.ResourceChangesServiceRegister;

/**
 * Action used to restore obects that had been logically deleted.<br/>
 * 
 * $Id: RestoreAction.java 56110 2011-03-03 05:03:54Z hcyi $
 * 
 */
public class RestoreAction extends AContextualAction {

    boolean needToUpdatePalette;

    Map<String, Item> procItems;

    List<IRepositoryViewObject> connections;

    public RestoreAction() {
        super();
        this.setText(Messages.getString("RestoreAction.action.title")); //$NON-NLS-1$
        this.setToolTipText(Messages.getString("RestoreAction.action.toolTipText")); //$NON-NLS-1$
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.RESTORE_ICON));
        this.setActionDefinitionId("restoreItem"); //$NON-NLS-1$
    }

    protected void restoreNode(RepositoryNode node) {
        try {
            RestoreFolderUtil restoreFolder = new RestoreFolderUtil();
            ERepositoryObjectType nodeType = (ERepositoryObjectType) (node).getProperties(EProperties.CONTENT_TYPE);
            if (nodeType == null) {
                return;
            }
            if (nodeType.isSubItem()) {
                ConnectionItem item = (ConnectionItem) node.getObject().getProperty().getItem();
                AbstractMetadataObject abstractMetadataObject = ((ISubRepositoryObject) node.getObject())
                        .getAbstractMetadataObject();
                SubItemHelper.setDeleted(abstractMetadataObject, false);

                final String id = item.getProperty().getId();
                Item tmpItem = procItems.get(id);
                if (tmpItem == null) {
                    procItems.put(id, item);
                }
                connections.add(node.getObject());
            } else {
                IPath path = restoreFolder.restoreFolderIfNotExists(nodeType, node);
                if (node.getObject().getProperty().getItem() instanceof FolderItem) {
                    node.getObject().getProperty().getItem().getState().setDeleted(false);
                } else {
                    RestoreObjectAction restoreObjectAction = RestoreObjectAction.getInstance();
                    restoreObjectAction.execute(node, null, path);
                }
                ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                // for bug 17079
                if (nodeType.equals(ERepositoryObjectType.PROCESS) || nodeType.equals(ERepositoryObjectType.JOBLET)
                        && node.getObject() != null) {
                    IRepositoryViewObject docObject = factory.getLastVersion(node.getObject().getId() + "doc"); //$NON-NLS-1$
                    if (docObject != null) {
                        factory.restoreObject(docObject, path);
                    }
                }
                // disable SVG actions

                // if (PluginChecker.isTIS()) {
                // if (node.getObject() != null) {
                // if (ERepositoryObjectType.BUSINESS_PROCESS.equals(node.getObject().getType())) {
                //                            IRepositoryViewObject svgObjectToMove = factory.getLastVersion("svg_" + node.getObject().getId()); //$NON-NLS-1$
                // if (svgObjectToMove != null) {
                // factory.restoreObject(svgObjectToMove, path);
                // }
                // }
                // }
                // }

                // MOD qiongli 2010-10-11,bug 15674
                AbstractResourceChangesService resChangeService = ResourceChangesServiceRegister.getInstance()
                        .getResourceChangeService(AbstractResourceChangesService.class);
                Item item = node.getObject().getProperty().getItem();
                if (item instanceof ConnectionItem && resChangeService != null) {
                    resChangeService.handleRestore(item.getProperty());
                }
                // ~

            }
            if (nodeType == ERepositoryObjectType.JOBLET) {
                needToUpdatePalette = true;
            }
            if (nodeType.isSubItem()) {
                RepositoryNode parent = node.getParent();
                if (parent.getObject() == null) { // db
                    parent = parent.getParent();
                }
                nodeType = parent.getObjectType();
            }
            if (node.hasChildren()) {
                for (IRepositoryNode childNode : node.getChildren()) {
                    restoreNode((RepositoryNode) childNode);
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    @Override
    protected void doRun() {
        // used to store the database connection object that are used to notify the sqlBuilder.
        ISelection selection = getSelection();

        connections = new ArrayList<IRepositoryViewObject>();
        procItems = new HashMap<String, Item>();
        needToUpdatePalette = false;
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

        for (Object obj : ((IStructuredSelection) selection).toArray()) {
            if (obj instanceof RepositoryNode) {
                RepositoryNode node = (RepositoryNode) obj;
                restoreNode(node);

                // restore parents folder if deleted also

                while (node.getParent().getObject() != null
                        && factory.getStatus(node.getParent().getObject()) == ERepositoryStatus.DELETED) {
                    node = node.getParent();
                    if (node.getObject().getProperty().getItem() instanceof FolderItem) {
                        node.getObject().getProperty().getItem().getState().setDeleted(false);
                    }

                }
            }
        }
        try {
            factory.saveProject(ProjectManager.getInstance().getCurrentProject());
            for (String id : procItems.keySet()) {
                Item item = procItems.get(id);
                factory.save(item);
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        procItems = null;

        final boolean updatePalette = needToUpdatePalette;
        Display.getCurrent().syncExec(new Runnable() {

            public void run() {
                // bug 16594
                IRepositoryView repositoryView = RepositoryManager.getRepositoryView();
                if (repositoryView != null) {
                    repositoryView.refresh();
                }
                if (updatePalette) {
                    ComponentUtilities.updatePalette();
                }
            }
        });
        notifySQLBuilder(connections);
        connections = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty();
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            canWork = false;
        }
        RestoreObjectAction restoreObjectAction = RestoreObjectAction.getInstance();
        for (Object o : (selection).toArray()) {
            if (canWork) {
                if (o instanceof RepositoryNode) {
                    RepositoryNode node = (RepositoryNode) o;
                    canWork = restoreObjectAction.validateAction(node, null);
                    if (canWork && !ProjectManager.getInstance().isInCurrentMainProject(node)) {
                        canWork = false;
                    }
                    if (!canWork) {
                        break;
                    }
                }
            }
        }
        setEnabled(canWork);
    }
}
