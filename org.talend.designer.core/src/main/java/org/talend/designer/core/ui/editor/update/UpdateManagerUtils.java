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
package org.talend.designer.core.ui.editor.update;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.dialogs.ProgressDialog;
import org.talend.core.CorePlugin;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.context.JobContext;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.model.update.UpdatesConstants;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.update.cmd.UpdateContextParameterCommand;
import org.talend.designer.core.ui.editor.update.cmd.UpdateJobletNodeCommand;
import org.talend.designer.core.ui.editor.update.cmd.UpdateMainParameterCommand;
import org.talend.designer.core.ui.editor.update.cmd.UpdateNodeParameterCommand;
import org.talend.designer.core.ui.views.contexts.Contexts;
import org.talend.designer.core.ui.views.jobsettings.JobSettings;
import org.talend.designer.core.ui.views.properties.ComponentSettings;
import org.talend.designer.joblet.model.JobletProcess;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * ggu class global comment. Detailled comment
 */
public final class UpdateManagerUtils {

    /**
     * 
     * used for get repository id and child name, such as "xxxxxxxxxxx - metadata".
     */
    public static String[] getSourceIdAndChildName(final String idAndName) {
        if (idAndName == null) {
            return null;
        }
        String[] result = idAndName.split(UpdatesConstants.SEGMENT_LINE);
        if (result.length == 2) {
            return result;
        }
        return null;

    }

    public static IComponent getComponent(Process process, final String name) {
        if (name != null) {
            AbstractMultiPageTalendEditor editor = (AbstractMultiPageTalendEditor) process.getEditor();
            if (editor != null) {
                AbstractTalendEditor talendEditor = editor.getTalendEditor();
                if (talendEditor != null) {
                    return talendEditor.getComponent(name);
                }
            }
        }
        return null;
    }

    /**
     * qzhang Comment method "getNewOutputTableForConnection".
     * 
     * @param newOutputTableList
     * @param attachedConnector
     * @return
     */
    public static IMetadataTable getNewOutputTableForConnection(List<IMetadataTable> newOutputTableList, String tableName) {
        for (IMetadataTable metadataTable : newOutputTableList) {
            if (tableName != null && tableName.equals(metadataTable.getTableName())
                    || tableName.equals(metadataTable.getTableName())) {
                return metadataTable;
            }
        }
        return null;
    }

    /**
     * qzhang Comment method "getNewInputTableForConnection".
     * 
     * @param newInputTableList
     * @param connector
     * 
     * @return
     */
    public static IMetadataTable getNewInputTableForConnection(List<IMetadataTable> newInputTableList, String connector) {
        for (IMetadataTable metadataTable : newInputTableList) {
            if (connector != null
                    && (connector.equals(metadataTable.getAttachedConnector()) || connector.equals(metadataTable.getTableName()))) {
                return metadataTable;
            }
        }
        return null;
    }

    /**
     * qzhang Comment method "getElemParam".
     * 
     * @param elemParams
     * @param string
     * 
     * @return
     */
    public static IElementParameter getElemParam(List<IElementParameter> elemParams, String string) {
        for (IElementParameter elementParameter : elemParams) {
            if (string != null && string.equals(elementParameter.getContext())) {
                return elementParameter;
            }
        }
        return null;
    }

    public static String addBrackets(String value) {
        if (value == null || UpdatesConstants.EMPTY.equals(value.trim())) {
            return UpdatesConstants.EMPTY;
        }
        return UpdatesConstants.SPACE + UpdatesConstants.LEFT_BRACKETS + value + UpdatesConstants.RIGHT_BRACKETS;
    }

    public static List<IProcess2> getOpenedProcess() {
        IEditorReference[] reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
        if (reference != null) {
            List<IProcess2> processes = RepositoryPlugin.getDefault().getDesignerCoreService().getOpenedProcess(reference);
            if (processes != null) {
                return processes;
            }
        }
        return Collections.emptyList();
    }

    public static boolean executeUpdates(final List<UpdateResult> results) {
        return executeUpdates(results, false);
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public static boolean executeUpdates(final List<UpdateResult> results, final IProcess2 currentProcess) {
        RepositoryWorkUnit<Boolean> repositoryWorkUnit = new RepositoryWorkUnit<Boolean>(
                Messages.getString("UpdateManagerUtils.updateMOfification")) { //$NON-NLS-1$

            @Override
            protected void run() throws LoginException, PersistenceException {
                result = doExecuteUpdates(results, currentProcess);
            }

        };
        repositoryWorkUnit.setAvoidUnloadResources(true);
        ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(repositoryWorkUnit);
        return repositoryWorkUnit.getResult();
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public static boolean executeUpdates(final List<UpdateResult> results, final boolean onlySimpleShow) {
        RepositoryWorkUnit<Boolean> repositoryWorkUnit = new RepositoryWorkUnit<Boolean>(
                Messages.getString("UpdateManagerUtils.updateMOfification")) { //$NON-NLS-1$

            @Override
            protected void run() throws LoginException, PersistenceException {
                result = doExecuteUpdates(results, onlySimpleShow);
            }

        };
        repositoryWorkUnit.setAvoidUnloadResources(true);
        ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(repositoryWorkUnit);
        return repositoryWorkUnit.getResult();
    }

    private static boolean doExecuteUpdates(final List<UpdateResult> results, boolean onlySimpleShow) {
        if (results == null || results.isEmpty()) {
            return false;
        }
        try {
            // changed by hqzhang, Display.getCurrent().getActiveShell() may cause studio freeze
            UpdateDetectionDialog checkDialog = new UpdateDetectionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell(), results, onlySimpleShow);

            if (checkDialog.open() == IDialogConstants.OK_ID) {
                // final List<Object> selectResult = Arrays.asList(checkDialog.getResult());
                ProgressDialog progress = new ProgressDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()) {

                    @Override
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        monitor.setCanceled(false);
                        int size = (results.size() * 2 + 1) * UpdatesConstants.SCALE;
                        monitor.beginTask(Messages.getString("UpdateManagerUtils.Update"), size); //$NON-NLS-1$

                        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

                        IRepositoryViewObject previousObj = null;
                        for (UpdateResult result : results) {
                            // several results may keep the same objectId , they are from the same process . So no need
                            // unload each time.
                            IRepositoryViewObject currentObj = null;
                            String currentId = result.getObjectId();
                            if (result.getJob() == null && previousObj != null && previousObj.getId().equals(currentId)) {
                                currentObj = previousObj;
                            } else if (previousObj instanceof RepositoryObject) {
                                if (!ERepositoryStatus.LOCK_BY_USER
                                        .equals(factory.getStatus(previousObj.getProperty().getItem()))) {
                                    ((RepositoryObject) previousObj).unload();
                                    previousObj = null;
                                }
                            }

                            if (result.getJob() == null) {
                                if (currentObj == null && currentId != null) {
                                    boolean checkOnlyLastVersion = Boolean.parseBoolean(DesignerPlugin.getDefault()
                                            .getPreferenceStore().getString("checkOnlyLastVersion")); //$NON-NLS-1$
                                    try {
                                        if (checkOnlyLastVersion || result.getObjectVersion() == null) {
                                            currentObj = factory.getLastVersion(currentId);
                                        } else {
                                            List<IRepositoryViewObject> allVersion = factory.getAllVersion(currentId);
                                            for (IRepositoryViewObject obj : allVersion) {
                                                if (obj.getVersion().equals(result.getObjectVersion())) {
                                                    currentObj = obj;
                                                }
                                            }

                                        }
                                    } catch (PersistenceException e) {
                                        ExceptionHandler.process(e);
                                    }
                                }
                                if (currentObj != null) {
                                    previousObj = currentObj;
                                    Item item = currentObj.getProperty().getItem();
                                    IProcess process = null;
                                    IDesignerCoreService designerCoreService = CorePlugin.getDefault().getDesignerCoreService();
                                    if (item instanceof ProcessItem) {
                                        process = designerCoreService.getProcessFromProcessItem((ProcessItem) item);
                                    } else if (item instanceof JobletProcessItem) {
                                        process = designerCoreService.getProcessFromJobletProcessItem((JobletProcessItem) item);
                                    }
                                    result.setJob(process);

                                    // node stored in result set is not the same instance as in process after we
                                    // load again the closed process, so need to find the corresponding node in process
                                    if (process != null && result.getUpdateObject() instanceof INode) {
                                        INode toUpdate = (INode) result.getUpdateObject();
                                        for (INode node : process.getGraphicalNodes()) {
                                            if (node.getLabel().equals(toUpdate.getLabel())) {
                                                result.setUpdateObject(node);
                                            }
                                        }
                                    }
                                }
                            }

                            // execute
                            executeUpdate(result, monitor);

                            if (result.getObjectId() != null) {
                                result.setJob(null);
                            }

                        }

                        // update joblet reference
                        upadateJobletReferenceInfor();

                        // refresh
                        refreshRelatedViewers(results);

                        // hyWang add method checkandRefreshProcess for bug7248
                        checkandRefreshProcess(results);

                        monitor.worked(1 * UpdatesConstants.SCALE);
                        monitor.done();
                    }

                };
                try {
                    progress.executeProcess();
                } catch (InvocationTargetException e) {
                    ExceptionHandler.process(e);
                    //
                } catch (InterruptedException e) {
                    ExceptionHandler.process(e);
                    //
                }
                return !results.isEmpty();
            }
        } finally {
            results.clear();
        }
        return false;
    }

    private static boolean doExecuteUpdates(final List<UpdateResult> results, final IProcess2 currentProcess) {
        if (results == null || results.isEmpty()) {
            return false;
        }
        try {
            // UpdateDetectionDialog checkDialog = new UpdateDetectionDialog(Display.getCurrent().getActiveShell(),
            // results,
            // onlySimpleShow);

            // if (checkDialog.open() == IDialogConstants.OK_ID) {
            // final List<Object> selectResult = Arrays.asList(checkDialog.getResult());
            ProgressDialog progress = new ProgressDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()) {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.setCanceled(false);
                    int size = (results.size() * 2 + 1) * UpdatesConstants.SCALE;
                    monitor.beginTask(Messages.getString("UpdateManagerUtils.Update"), size); //$NON-NLS-1$

                    ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

                    IRepositoryViewObject previousObj = null;
                    for (UpdateResult result : results) {
                        // several results may keep the same objectId , they are from the same process . So no need
                        // unload each time.
                        IRepositoryViewObject currentObj = null;
                        String currentId = result.getObjectId();
                        if (result.getJob() == null && previousObj != null && previousObj.getId().equals(currentId)) {
                            currentObj = previousObj;
                        } else if (previousObj instanceof RepositoryObject) {
                            if (!ERepositoryStatus.LOCK_BY_USER.equals(factory.getStatus(previousObj.getProperty().getItem()))) {
                                ((RepositoryObject) previousObj).unload();
                                previousObj = null;
                            }
                        }

                        if (result.getJob() == null) {
                            if (currentObj == null && currentId != null) {
                                boolean checkOnlyLastVersion = Boolean.parseBoolean(DesignerPlugin.getDefault()
                                        .getPreferenceStore().getString("checkOnlyLastVersion")); //$NON-NLS-1$
                                try {
                                    if (checkOnlyLastVersion || result.getObjectVersion() == null) {
                                        currentObj = factory.getLastVersion(currentId);
                                    } else {
                                        List<IRepositoryViewObject> allVersion = factory.getAllVersion(currentId);
                                        for (IRepositoryViewObject obj : allVersion) {
                                            if (obj.getVersion().equals(result.getObjectVersion())) {
                                                currentObj = obj;
                                            }
                                        }

                                    }
                                } catch (PersistenceException e) {
                                    ExceptionHandler.process(e);
                                }
                            }
                            if (currentObj != null) {
                                previousObj = currentObj;
                                Item item = currentObj.getProperty().getItem();
                                IProcess process = null;
                                IDesignerCoreService designerCoreService = CorePlugin.getDefault().getDesignerCoreService();
                                if (item instanceof ProcessItem) {
                                    process = designerCoreService.getProcessFromProcessItem((ProcessItem) item);
                                } else if (item instanceof JobletProcessItem) {
                                    process = designerCoreService.getProcessFromJobletProcessItem((JobletProcessItem) item);
                                }
                                result.setJob(process);

                                // node stored in result set is not the same instance as in process after we
                                // load again the closed process, so need to find the corresponding node in process
                                if (process != null && result.getUpdateObject() instanceof INode) {
                                    INode toUpdate = (INode) result.getUpdateObject();
                                    for (INode node : process.getGraphicalNodes()) {
                                        if (node.getLabel().equals(toUpdate.getLabel())) {
                                            result.setUpdateObject(node);
                                        }
                                    }
                                }
                            }
                        }

                        // execute
                        executeUpdate(result, monitor);

                        if (result.getObjectId() != null) {
                            result.setJob(null);
                        }

                    }

                    // update joblet reference
                    upadateJobletReferenceInfor();

                    // refresh
                    refreshRelatedViewers(results);

                    // hyWang add method checkandRefreshProcess for bug7248
                    checkandRefreshProcess(results);

                    monitor.worked(1 * UpdatesConstants.SCALE);
                    monitor.done();
                }

            };
            try {
                progress.executeProcess();
            } catch (InvocationTargetException e) {
                ExceptionHandler.process(e);
                //
            } catch (InterruptedException e) {
                ExceptionHandler.process(e);
                //
            }
            return !results.isEmpty();
            // }
        } finally {
            results.clear();
        }
    }

    /**
     * 
     * ggu Comment method "refreshViewers".
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    private static void refreshRelatedViewers(List results) {
        boolean context = false;
        boolean jobSetting = false;
        boolean componentSettings = false;
        boolean palette = false;

        for (UpdateResult result : (List<UpdateResult>) results) {
            switch (result.getUpdateType()) {
            case CONTEXT:
            case JOBLET_CONTEXT:
                context = true;
                break;
            case JOB_PROPERTY_EXTRA:
            case JOB_PROPERTY_STATS_LOGS:
            case JOB_PROPERTY_HEADERFOOTER:
                jobSetting = true;
                break;
            case NODE_PROPERTY:
            case NODE_QUERY:
            case NODE_SCHEMA:
                componentSettings = true;
                break;
            case RELOAD:
            case JOBLET_RENAMED:
            case JOBLET_SCHEMA:
                palette = true;
                break;
            default:
                break;
            }
        }
        if (context) {
            Contexts.switchToCurContextsView();
        }
        if (jobSetting) {
            JobSettings.switchToCurJobSettingsView();
        }
        if (componentSettings) {
            ComponentSettings.switchToCurComponentSettingsView();
        }
        if (palette) {
            ComponentUtilities.updatePalette();
        }
    }

    private static void saveModifiedItem(UpdateResult updateResult) {

        // save
        IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();

        try {
            if (updateResult.getJob() instanceof IProcess2) {
                IProcess2 process2 = (IProcess2) updateResult.getJob();
                Property property = factory.getUptodateProperty(process2.getProperty());
                if (property != null) {
                    process2.setProperty(property);
                }

                ProcessType processType = process2.saveXmlFile();

                Item item = process2.getProperty().getItem();
                if (item instanceof JobletProcessItem) {
                    ((JobletProcessItem) item).setJobletProcess((JobletProcess) processType);
                } else {
                    ((ProcessItem) item).setProcess(processType);
                }
                factory.save(item);
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }

    }

    private static void executeUpdate(UpdateResult result, IProgressMonitor monitor) {
        if (result.isReadOnlyProcess()) {
            return;
        }
        // update
        Command command = null;
        switch (result.getUpdateType()) {
        case NODE_PROPERTY:
        case NODE_SCHEMA:
        case NODE_QUERY:
        case NODE_SAP_IDOC:
        case NODE_SAP_FUNCTION:
        case NODE_VALIDATION_RULE:
            command = new UpdateNodeParameterCommand(result);
            break;
        case JOB_PROPERTY_EXTRA:
        case JOB_PROPERTY_STATS_LOGS:
        case JOB_PROPERTY_HEADERFOOTER:
            command = new UpdateMainParameterCommand(result);
            break;
        case CONTEXT:
            command = executeContextUpdates(result);
            break;
        case CONTEXT_GROUP:
            command = executeContextGroupUpdates(result);
            break;
        case JOBLET_RENAMED:
        case JOBLET_SCHEMA:
        case RELOAD:
            command = executeJobletNodesUpdates(result);
            break;
        case JOBLET_CONTEXT:
            command = new Command() { // have update in checking.
            };
            break;
        default:
            break;
        }
        if (command != null) {
            SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1 * UpdatesConstants.SCALE,
                    SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
            subMonitor.beginTask(UpdatesConstants.EMPTY, 1);
            subMonitor.subTask(getResultTaskInfor(result));
            //
            Object job = result.getJob();
            boolean executed = false;
            boolean isSaveModifiedItem = false;
            if (job != null) {
                if (job instanceof IProcess2) {
                    IProcess2 process = (IProcess2) job;
                    process.getCommandStack().execute(command);
                    if (process.getEditor() != null) {
                        isSaveModifiedItem = true;
                    }
                    executed = true;
                }
            }
            if (!executed) {
                command.execute();
            }
            if (result.isFromItem() || !isSaveModifiedItem) {
                // save updated process
                saveModifiedItem(result);
            }
            subMonitor.worked(1);

        }

    }

    private static String getResultTaskInfor(UpdateResult result) {
        if (result == null) {
            return UpdatesConstants.EMPTY;
        }
        StringBuffer infor = new StringBuffer();
        infor.append(result.getName());
        infor.append(UpdatesConstants.LEFT_BRACKETS);
        infor.append(result.getCategory());
        infor.append(UpdatesConstants.SEGMENT);
        infor.append(result.getJobInfor());
        infor.append(UpdatesConstants.RIGHT_BRACKETS);
        return infor.toString();
    }

    /*
     * context
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    private static Command executeContextUpdates(UpdateResult result) {
        if (result == null) {
            return null;
        }
        Object object = result.getUpdateObject();
        if (object instanceof Set) {
            return new UpdateContextParameterCommand(result);
        }
        return null;
    }

    /*
     * contextGroup
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    private static Command executeContextGroupUpdates(UpdateResult result) {
        if (result == null) {
            return null;
        }
        Object object = result.getUpdateObject();
        if (object instanceof JobContext) {
            return new UpdateContextParameterCommand(result);
        }
        return null;
    }

    /*
     * joblet
     */
    private static Command executeJobletNodesUpdates(UpdateResult result) {
        if (result == null) {
            return null;
        }
        Object parameter = result.getParameter();
        if (parameter != null) {
            return new UpdateJobletNodeCommand(result);
        }
        return null;
    }

    private static void upadateJobletReferenceInfor() {
        List<IProcess2> openedProcessList = CorePlugin.getDefault().getDesignerCoreService()
                .getOpenedProcess(RepositoryUpdateManager.getEditors());

        for (IProcess2 proc : openedProcessList) {
            proc.getUpdateManager().retrieveRefInformation();
        }
    }

    /**
     * DOC hyWang Comment method "checkandRefreshProcess".
     * 
     * @param results
     */
    private static void checkandRefreshProcess(final List<UpdateResult> results) {
        for (UpdateResult tempResult : results) {
            if (tempResult.getJob() instanceof IProcess2) {
                IProcess2 process = (IProcess2) tempResult.getJob();
                process.checkProcess();
            }
        }
    }
}
