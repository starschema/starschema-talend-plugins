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
package org.talend.designer.core.ui.action;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.ui.routine.RoutineItemRecord;
import org.talend.designer.core.ui.routine.SetupProcessDependenciesRoutinesDialog;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * ggu class global comment. Detailled comment
 */
public class SetupProcessDependenciesRoutinesAction extends AContextualAction {

    public SetupProcessDependenciesRoutinesAction() {
        super();

        String text2 = Messages.getString("SetupProcessDependenciesRoutinesAction.title"); //$NON-NLS-1$
        this.setText(text2);
        this.setToolTipText(text2);
        this.setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.ROUTINE_ICON));
    }

    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            canWork = false;
        }
        if (canWork) {
            Object o = selection.getFirstElement();
            RepositoryNode node = (RepositoryNode) o;
            switch (node.getType()) {
            case REPOSITORY_ELEMENT:
                if (node.getObjectType() != ERepositoryObjectType.PROCESS) {
                    canWork = false;
                } else {
                    // canWork = false;
                    // IRepositoryService service = DesignerPlugin.getDefault().getRepositoryService();
                    // IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();
                    // if (repFactory.isPotentiallyEditable(node.getObject())) {
                    // canWork = true;
                    // }
                }
                break;
            default:
                canWork = false;
            }
            if (canWork && node.getObject() != null
                    && ProxyRepositoryFactory.getInstance().getStatus(node.getObject()) == ERepositoryStatus.DELETED) {
                canWork = false;
            }
            if (canWork && !ProjectManager.getInstance().isInCurrentMainProject(node)) {
                canWork = false;
            }

            // If the editProcess action canwork is true, then detect that the job version is the latest verison or not.
            if (canWork) {
                canWork = isLastVersion(node);
            }

        }
        setEnabled(canWork);
    }

    @Override
    protected void doRun() {
        ISelection selection = getSelection();
        Object obj = ((IStructuredSelection) selection).getFirstElement();
        if (obj == null) {
            return;
        }
        RepositoryNode node = (RepositoryNode) obj;
        boolean readonly = false;
        IRepositoryService service = DesignerPlugin.getDefault().getRepositoryService();
        IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();
        ERepositoryStatus status = repFactory.getStatus(node.getObject());
        if (!repFactory.isPotentiallyEditable(node.getObject()) || status == ERepositoryStatus.LOCK_BY_OTHER
                || status == ERepositoryStatus.LOCK_BY_USER) {
            readonly = true;
        }
        ProcessItem processItem = (ProcessItem) node.getObject().getProperty().getItem();
        // try {
        // IRepositoryViewObject lastVersion =
        // repFactory.getLastVersion(ProjectManager.getInstance().getCurrentProject(),
        // processItem.getProperty().getId());
        // if (lastVersion != null) {
        // processItem = (ProcessItem) lastVersion.getProperty().getItem();
        // }
        // } catch (PersistenceException e1) {
        // ExceptionHandler.process(e1);
        // }

        ProcessType process = processItem.getProcess();

        SetupProcessDependenciesRoutinesDialog dialog = new SetupProcessDependenciesRoutinesDialog(PlatformUI.getWorkbench()
                .getDisplay().getActiveShell(), process, readonly);
        if (dialog.open() == Window.OK && !readonly) {
            process.getParameters().getRoutinesParameter().clear();

            createRoutinesDependencies(process, dialog.getSystemRoutines());
            createRoutinesDependencies(process, dialog.getUserRoutines());
            try {
                CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory().save(processItem);
                RelationshipItemBuilder.getInstance().addOrUpdateItem(processItem);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    private void createRoutinesDependencies(ProcessType process, List<RoutineItemRecord> routineRecords) {
        if (routineRecords == null) {
            return;
        }
        for (RoutineItemRecord r : routineRecords) {
            List<RoutinesParameterType> routinesDependencies = (List<RoutinesParameterType>) process.getParameters()
                    .getRoutinesParameter();
            boolean found = false;
            for (RoutinesParameterType type : routinesDependencies) {
                // if (r.isSystem() == type.isSystem()) {
                if (type.getId().equals(r.getId()) || type.getName().equals(r.getName())) {
                    found = true;
                    break;
                }
                // }
            }
            if (!found) {
                RoutinesParameterType itemRecordType = TalendFileFactory.eINSTANCE.createRoutinesParameterType();
                itemRecordType.setName(r.getName());
                itemRecordType.setId(r.getId());
                process.getParameters().getRoutinesParameter().add(itemRecordType);
            }
        }
    }
}
