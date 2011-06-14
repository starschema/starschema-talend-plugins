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

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.SystemException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.ui.ICDCProviderService;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.ui.dialog.PastSelectorDialog;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: CopyObjectAction.java 58947 2011-04-21 05:54:23Z wchen $
 * 
 */
public class CopyObjectAction {

    IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    private static CopyObjectAction singleton = new CopyObjectAction();

    public static CopyObjectAction getInstance() {
        return singleton;
    }

    public boolean validateAction(RepositoryNode sourceNode, RepositoryNode targetNode) {
        if (sourceNode == null) {
            return false;
        }

        // Cannot copy folder or system folder :
        if (sourceNode.getType() != ENodeType.REPOSITORY_ELEMENT) {
            return false;
        }

        IRepositoryViewObject objectToCopy = sourceNode.getObject();

        // Cannot move logically deleted objects :
        // try {
        if (objectToCopy != null && objectToCopy.getId() == null) {
            return false;
        }
        // Cannot copy for refProject
        // if (objectToCopy != null && factory.getStatus(objectToCopy) == ERepositoryStatus.READ_ONLY) {
        // return false;
        // }

        // if copied item has been deleted
        if (objectToCopy == null || objectToCopy.getRepositoryStatus() == ERepositoryStatus.DELETED) {
            return false;
        }

        // } catch (PersistenceException e) {
        // ExceptionHandler.process(e);
        // return false;
        // }

        // Cannot copy system routines:
        if (objectToCopy.getType() == ERepositoryObjectType.ROUTINES) {
            Property property = objectToCopy.getProperty();
            RoutineItem item = (RoutineItem) property.getItem();
            return !item.isBuiltIn();
        }
        // Cannot copy system sql pattern:
        if (objectToCopy.getType() == ERepositoryObjectType.SQLPATTERNS) {
            Property property = objectToCopy.getProperty();
            SQLPatternItem item = (SQLPatternItem) property.getItem();
            return !item.isSystem();
        }
        // for cdc
        if (PluginChecker.isCDCPluginLoaded()) {
            ICDCProviderService cdcService = (ICDCProviderService) GlobalServiceRegister.getDefault().getService(
                    ICDCProviderService.class);
            if (cdcService != null
                    && (cdcService.isSubscriberTableNode(sourceNode) || cdcService.isSystemSubscriberTable(sourceNode))) {
                return false;
            }
        }
        // Special rule : temp ?
        if (targetNode == null) {
            return true;
        }

        // for bug 0005454: Copy paste with keyboard in the repository view doesn't work.
        if (targetNode.getType() == ENodeType.REPOSITORY_ELEMENT || targetNode.getType() == ENodeType.SIMPLE_FOLDER
                || targetNode.getType() == ENodeType.SYSTEM_FOLDER) {
            return ((ERepositoryObjectType) targetNode.getProperties(EProperties.CONTENT_TYPE)) == objectToCopy.getType();
        }
        return false;
    }

    public void execute(RepositoryNode sourceNode, RepositoryNode targetNode) throws Exception {
        if (!validateAction(sourceNode, targetNode)) {
            return;
        }

        // for bug 0005454: Copy paste with keyboard in the repository view doesn't work.
        if (targetNode.getType() == ENodeType.REPOSITORY_ELEMENT) {
            targetNode = targetNode.getParent();
        }

        IPath path = RepositoryNodeUtilities.getPath(targetNode);

        if (sourceNode.getType().equals(ENodeType.REPOSITORY_ELEMENT)) {
            // Source is an repository element :
            // wzhang modified to fix bug 12349 and 11535
            Item originalItem = sourceNode.getObject().getProperty().getItem();
            List<IRepositoryViewObject> allVersion = factory.getAllVersion(originalItem.getProperty().getId());

            if (allVersion.size() == 1) {
                copySingleVersionItem(originalItem, path);

            } else if (allVersion.size() > 1) {
                PastSelectorDialog dialog = new PastSelectorDialog(Display.getCurrent().getActiveShell(), allVersion, sourceNode);
                if (dialog.open() == Window.OK) {
                    Set<IRepositoryViewObject> selectedVersionItems = dialog.getSelectedVersionItems();
                    String id = null;
                    String label = null;
                    boolean isfirst = true;
                    boolean needSys = true;
                    for (IRepositoryViewObject object : selectedVersionItems) {
                        Item selectedItem = object.getProperty().getItem();
                        final Item copy = factory.copy(selectedItem, path);
                        if (isfirst) {
                            id = copy.getProperty().getId();
                            label = copy.getProperty().getLabel();
                            isfirst = false;
                        }
                        copy.getProperty().setId(id);
                        copy.getProperty().setLabel(label);
                        if (needSys && originalItem instanceof RoutineItem) {
                            String lastestVersion = getLastestVersion(selectedVersionItems);
                            if (lastestVersion.equals(copy.getProperty().getVersion())) {
                                synDuplicatedRoutine((RoutineItem) copy);
                                needSys = false;
                            }
                        }
                        RepositoryWorkUnit<Object> workUnit = new RepositoryWorkUnit<Object>("", this) {//$NON-NLS-1$

                            @Override
                            protected void run() throws LoginException, PersistenceException {
                                if (copy instanceof ProcessItem) {
                                    RelationshipItemBuilder.getInstance().addOrUpdateItem((ProcessItem) copy);
                                }
                                factory.save(copy);
                            }
                        };
                        workUnit.setAvoidUnloadResources(true);
                        factory.executeRepositoryWorkUnit(workUnit);
                    }
                }
            }
        }

    }

    private void copySingleVersionItem(final Item item, final IPath path) {
        final RepositoryWorkUnit<Object> workUnit = new RepositoryWorkUnit<Object>("", this) {//$NON-NLS-1$

            @Override
            protected void run() throws LoginException, PersistenceException {
                try {
                    Item newItem = factory.copy(item, path, true);
                    // qli modified to fix the bug 5400 and 6185.
                    if (newItem instanceof RoutineItem) {
                        synDuplicatedRoutine((RoutineItem) newItem);
                    }
                    if (newItem instanceof ProcessItem || newItem instanceof JobletProcessItem) {
                        RelationshipItemBuilder.getInstance().addOrUpdateItem(newItem);
                    }
                    if (newItem instanceof ConnectionItem) {
                        ConnectionItem connectionItem = (ConnectionItem) newItem;
                        connectionItem.getConnection().getSupplierDependency().clear();
                    }
                    factory.save(newItem);
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        };

        workUnit.setAvoidUnloadResources(true);
        factory.executeRepositoryWorkUnit(workUnit);
    }

    private String getLastestVersion(Set<IRepositoryViewObject> set) {
        if (set.isEmpty()) {
            return null;
        }
        String version = null;
        for (IRepositoryViewObject obj : set) {
            String curVersion = obj.getProperty().getVersion();
            if (version == null) {
                version = curVersion;
            } else {
                Double dVersion = Double.valueOf(version);
                Double dCurVersion = Double.valueOf(curVersion);
                if (dCurVersion > dVersion) {
                    version = curVersion;
                }
            }
        }
        return version;
    }

    private void synDuplicatedRoutine(RoutineItem item) {
        ICodeGeneratorService codeGenService = (ICodeGeneratorService) GlobalServiceRegister.getDefault().getService(
                ICodeGeneratorService.class);
        if (codeGenService != null) {
            codeGenService.createRoutineSynchronizer().renameRoutineClass((RoutineItem) item);
            try {
                codeGenService.createRoutineSynchronizer().syncRoutine((RoutineItem) item, true);
            } catch (SystemException e) {
                ExceptionHandler.process(e);
            }
        }
    }

}
