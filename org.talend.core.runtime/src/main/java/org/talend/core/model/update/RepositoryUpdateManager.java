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
package org.talend.core.model.update;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.MultiKeyMap;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ICoreService;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.context.JobContext;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.DelimitedFileConnection;
import org.talend.core.model.metadata.builder.connection.FileExcelConnection;
import org.talend.core.model.metadata.builder.connection.LdifFileConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.PositionalFileConnection;
import org.talend.core.model.metadata.builder.connection.QueriesConnection;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.metadata.builder.connection.RegexpFileConnection;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.metadata.builder.connection.SAPIDocUnit;
import org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection;
import org.talend.core.model.metadata.builder.connection.WSDLSchemaConnection;
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.metadata.builder.connection.XmlXPathLoopDescriptor;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.GenericSchemaConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.model.utils.UpdateRepositoryHelper;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.runtime.i18n.Messages;
import org.talend.core.service.IMetadataManagmentService;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryNode;

/**
 * ggu class global comment. Detailled comment
 */
public abstract class RepositoryUpdateManager {

    /**
     * for repository context rename.
     */
    private Map<ContextItem, Map<String, String>> repositoryRenamedMap = new HashMap<ContextItem, Map<String, String>>();

    private Map<String, String> schemaRenamedMap = new HashMap<String, String>();

    /**
     * for context group
     */
    private Map<ContextItem, List<IContext>> repositoryContextGroupMap = new HashMap<ContextItem, List<IContext>>();

    private Map<ContextItem, List<IContext>> removeRepositoryContextGroupMap = new HashMap<ContextItem, List<IContext>>();

    private Map<ContextItem, List<IContext>> renameRepositoryContextGroupMap = new HashMap<ContextItem, List<IContext>>();

    private Map<IContext, String> renameContextGroup = new HashMap<IContext, String>();

    /* for table deleted and reselect on database wizard table */
    private Map<String, EUpdateResult> deletedOrReselectTablesMap = new HashMap<String, EUpdateResult>();

    /**
     * used for filter result.
     */
    protected Object parameter;

    private Map<ContextItem, Set<String>> newParametersMap = new HashMap<ContextItem, Set<String>>();

    private boolean onlyOpeningJob = false;

    private List<RelationshipItemBuilder.Relation> relations;

    private static IRepositoryService repistoryService = null;

    private static ICoreService coreService = null;

    private boolean isDetectAndUpdate = false;

    static {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRepositoryService.class)) {
            repistoryService = (IRepositoryService) GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
        }
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICoreService.class)) {
            coreService = (ICoreService) GlobalServiceRegister.getDefault().getService(ICoreService.class);
        }
    }

    public RepositoryUpdateManager(Object parameter) {
        super();
        this.parameter = parameter;
    }

    public RepositoryUpdateManager(Object parameter, boolean isDetectAndUpdate) {
        super();
        this.parameter = parameter;
        this.isDetectAndUpdate = isDetectAndUpdate;
    }

    public RepositoryUpdateManager(Object parameter, List<RelationshipItemBuilder.Relation> relations) {
        super();
        this.parameter = parameter;
        this.relations = relations;
    }

    // fwang fixed bug TDI-17155
    public RepositoryUpdateManager(Object parameter, List<RelationshipItemBuilder.Relation> relations, boolean isDetectAndUpdate) {
        super();
        this.parameter = parameter;
        this.relations = relations;
        this.isDetectAndUpdate = isDetectAndUpdate;
    }

    public void setOnlyOpeningJob(boolean onlyOpeningJob) {
        this.onlyOpeningJob = onlyOpeningJob;
    }

    /*
     * context
     */
    public Map<ContextItem, Map<String, String>> getContextRenamedMap() {
        return this.repositoryRenamedMap;
    }

    public void setContextRenamedMap(Map<ContextItem, Map<String, String>> repositoryRenamedMap) {
        this.repositoryRenamedMap = repositoryRenamedMap;
    }

    public Map<ContextItem, List<IContext>> getRepositoryAddGroupContext() {
        return this.repositoryContextGroupMap;
    }

    public void setRepositoryAddGroupContext(Map<ContextItem, List<IContext>> repositoryContextGroupMap) {
        this.repositoryContextGroupMap = repositoryContextGroupMap;
    }

    public Map<ContextItem, List<IContext>> getRepositoryRemoveGroupContext() {
        return this.removeRepositoryContextGroupMap;
    }

    public void setRepositoryRemoveGroupContext(Map<ContextItem, List<IContext>> removeRepositoryContextGroupMap) {
        this.removeRepositoryContextGroupMap = removeRepositoryContextGroupMap;
    }

    public Map<ContextItem, List<IContext>> getRepositoryRenameGroupContext() {
        return this.renameRepositoryContextGroupMap;
    }

    public void setRepositoryRenameGroupContext(Map<ContextItem, List<IContext>> renameRepositoryContextGroupMap) {
        this.renameRepositoryContextGroupMap = renameRepositoryContextGroupMap;
    }

    public Map<IContext, String> getRenameContextGroup() {
        return renameContextGroup;
    }

    public void setRenameContextGroup(Map<IContext, String> renameContextGroup) {
        this.renameContextGroup = renameContextGroup;
    }

    /*
     * Schema old name to new one
     */

    public Map<String, String> getSchemaRenamedMap() {
        return this.schemaRenamedMap;
    }

    public void setSchemaRenamedMap(Map<String, String> schemaRenamedMap) {
        this.schemaRenamedMap = schemaRenamedMap;
    }

    public abstract Set<EUpdateItemType> getTypes();

    private boolean openPropagationDialog() {
        return MessageDialog.openQuestion(Display.getCurrent().getActiveShell(),
                Messages.getString("RepositoryUpdateManager.Title"), //$NON-NLS-1$
                Messages.getString("RepositoryUpdateManager.Messages")); //$NON-NLS-1$
    }

    /**
     * 
     * ggu Comment method "openNoModificationDialog".
     * 
     * @param onlyImpactAnalysis for 9543
     * @return
     */
    private void openNoModificationDialog(boolean onlyImpactAnalysis) {
        String title = Messages.getString("RepositoryUpdateManager.NoModificationTitle"); //$NON-NLS-1$
        String messages = Messages.getString("RepositoryUpdateManager.NoModificationMessages"); ////$NON-NLS-1$
        if (onlyImpactAnalysis) {
            title = Messages.getString("RepositoryUpdateManager.NotFoundTitle"); //$NON-NLS-1$
            messages = Messages.getString("RepositoryUpdateManager.NotFoundMessages"); //$NON-NLS-1$
        }
        MessageDialog.openInformation(Display.getCurrent().getActiveShell(), title, messages);
    }

    private boolean openRenameCheckedDialog() {
        return MessageDialog.openQuestion(Display.getCurrent().getActiveShell(),
                Messages.getString("RepositoryUpdateManager.RenameContextTitle"), //$NON-NLS-1$
                Messages.getString("RepositoryUpdateManager.RenameContextMessages")); //$NON-NLS-1$

    }

    public boolean doWork() {
        return doWork(true, false);
    }

    public boolean needForcePropagation() {
        return needForcePropagationForContext() || (getSchemaRenamedMap() != null && !getSchemaRenamedMap().isEmpty());
    }

    private boolean needForcePropagationForContext() {
        return getContextRenamedMap() != null && !getContextRenamedMap().isEmpty();
    }

    @SuppressWarnings("restriction")
    public boolean doWork(boolean show, final boolean onlyImpactAnalysis) {
        // check the dialog.
        boolean checked = true;
        boolean showed = false;
        if (show) {
            if (needForcePropagationForContext()) {
                checked = openRenameCheckedDialog(); // bug 4988
                showed = true;
            } else if (parameter != null && !needForcePropagation()) {
                // see feature 4786
                IDesignerCoreService designerCoreService = CoreRuntimePlugin.getInstance().getDesignerCoreService();
                boolean deactive = designerCoreService != null ? Boolean.parseBoolean(designerCoreService
                        .getPreferenceStore(ITalendCorePrefConstants.DEACTIVE_REPOSITORY_UPDATE)) : true;
                if (deactive) {
                    return false;
                }

                checked = openPropagationDialog();
                showed = true;
            }
        } else {
            showed = true;
        }
        if (checked) {
            final List<UpdateResult> results = new ArrayList<UpdateResult>();
            boolean cancelable = !needForcePropagation();
            IRunnableWithProgress runnable = new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    List<UpdateResult> returnResult = checkJobItemsForUpdate(monitor, getTypes(), onlyImpactAnalysis);
                    if (returnResult != null) {
                        results.addAll(returnResult);
                    }
                }
            };

            try {
                final ProgressMonitorJobsDialog dialog = new ProgressMonitorJobsDialog(null);
                dialog.run(true, cancelable, runnable);

                // PlatformUI.getWorkbench().getProgressService().run(true, true, runnable);
            } catch (InvocationTargetException e) {
                ExceptionHandler.process(e);
            } catch (InterruptedException e) {
                if (e.getMessage().equals(UpdatesConstants.MONITOR_IS_CANCELED)) {
                    return false;
                }
                ExceptionHandler.process(e);
            }
            List<UpdateResult> checkedResults = null;

            if (parameter == null) { // update all job
                checkedResults = filterSpecialCheckedResult(results);
            } else { // filter
                checkedResults = filterCheckedResult(results);
            }
            if (checkedResults != null && !checkedResults.isEmpty()) {
                if (showed || parameter == null || unShowDialog(checkedResults) || openPropagationDialog()) {
                    IDesignerCoreService designerCoreService = CoreRuntimePlugin.getInstance().getDesignerCoreService();
                    return designerCoreService.executeUpdatesManager(checkedResults, onlyImpactAnalysis);
                }
                return false;
            }
            openNoModificationDialog(onlyImpactAnalysis);
        }
        return false;
    }

    private List<UpdateResult> filterSpecialCheckedResult(List<UpdateResult> results) {
        if (results == null) {
            return null;
        }
        List<IProcess2> openedProcessList = CoreRuntimePlugin.getInstance().getDesignerCoreService()
                .getOpenedProcess(getEditors());

        List<UpdateResult> checkedResults = new ArrayList<UpdateResult>();
        for (UpdateResult result : results) {
            if (result.getParameter() instanceof JobletProcessItem) {
                if (result.getJob() instanceof IProcess2) { // only opening job
                    if (openedProcessList.contains(result.getJob())) {
                        checkedResults.add(result);
                    }
                }
            } else {
                checkedResults.add(result); // ignore others
            }
        }
        return checkedResults;
    }

    private List<UpdateResult> filterCheckedResult(List<UpdateResult> results) {
        if (results == null) {
            return null;
        }
        List<UpdateResult> checkedResults = new ArrayList<UpdateResult>();
        for (UpdateResult result : results) {
            if (filterForType(result)) {
                checkedResults.add(result);
            } else {
                // for context
                if (result.getUpdateType() == EUpdateItemType.CONTEXT && result.getResultType() == EUpdateResult.BUIL_IN) {
                    checkedResults.add(result);
                }
                // for context group
                if (result.getUpdateType() == EUpdateItemType.CONTEXT_GROUP && result.getResultType() == EUpdateResult.ADD) {
                    Object job = result.getJob();
                    if (parameter instanceof ContextItem && job instanceof IProcess2) {
                        ContextItem contextItem = (ContextItem) parameter;
                        String sourceId = contextItem.getProperty().getId();
                        IProcess2 relatedJob = (IProcess2) job;
                        if (relatedJob != null) {
                            List<IContext> listContext = relatedJob.getContextManager().getListContext();
                            List<String> existSource = new ArrayList<String>();
                            for (IContext context : listContext) {
                                for (IContextParameter param : context.getContextParameterList()) {
                                    String source = param.getSource();
                                    if (source != null && !existSource.contains(source)) {
                                        existSource.add(source);
                                    }
                                }
                            }
                            if (existSource.contains(sourceId)) {
                                checkedResults.add(result);
                            }
                        }

                    }
                } else if (result.getUpdateType() == EUpdateItemType.CONTEXT && result.getResultType() == EUpdateResult.ADD) {
                    ConnectionItem contextModeConnectionItem = result.getContextModeConnectionItem();
                    // for context mode
                    if (contextModeConnectionItem != null && contextModeConnectionItem.getConnection() == this.parameter) {
                        checkedResults.add(result);
                    }
                }

            }

        }
        return checkedResults;
    }

    private boolean unShowDialog(List<UpdateResult> checkedResults) {
        if (checkedResults == null) {
            return false;
        }
        for (UpdateResult result : checkedResults) {
            if (result.getResultType() != EUpdateResult.UPDATE) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean filterForType(UpdateResult result) {
        if (result == null || parameter == null) {
            return false;
        }
        Object object = result.getParameter();
        if (object == null) {
            return false;
        }
        if (object == parameter) {
            return true;
        }
        if (object instanceof List) {
            List list = ((List) object);
            if (!list.isEmpty()) {
                Object firstObj = list.get(0);
                if (parameter == firstObj) { // for context rename
                    return true;
                }

                // schema
                if (checkResultSchema(result, firstObj, parameter)) {
                    return true;
                }

            }

        }
        // schema
        if (checkResultSchema(result, object, parameter)) {
            return true;
        }
        // query for wizard
        if (parameter instanceof QueriesConnection && object instanceof Query) {
            for (Query query : (List<Query>) ((QueriesConnection) parameter).getQuery()) {
                if (query.getId().equals(((Query) object).getId())) {
                    return true;
                }
            }
        }
        // for bug 17573
        if ((object instanceof Query) && (parameter instanceof Query)) {
            if (((Query) object).getId().equals(((Query) parameter).getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkResultSchema(UpdateResult result, Object object, Object parameter) {
        if (object == null || parameter == null) {
            return false;
        }
        // schema
        if (object instanceof IMetadataTable) { //
            if (parameter instanceof ConnectionItem) { //
                ConnectionItem connection = (ConnectionItem) parameter;
                String source = UpdateRepositoryHelper.getRepositorySourceName(connection);
                if (result.getRemark() != null) {
                    if (result.getRemark().startsWith(source)) {
                        return true;
                    } else if (result.isReadOnlyProcess()) {
                        return true;
                    } else {
                        // for bug 10365
                        String[] split = result.getRemark().split(UpdatesConstants.SEGMENT_LINE);
                        if (connection.getProperty() != null && split[0].equals(connection.getProperty().getId())) {
                            return true;
                        }
                    }
                }
            } else if (parameter instanceof org.talend.core.model.metadata.builder.connection.MetadataTable) {
                IMetadataTable table1 = ((IMetadataTable) object);
                MetadataTable table2 = (org.talend.core.model.metadata.builder.connection.MetadataTable) parameter;
                if (table1.getId() == null || table2.getId() == null) {
                    return table1.getLabel().equals(table2.getLabel());
                } else {
                    return table1.getId().equals(table2.getId());
                }
            } else if (parameter instanceof SAPFunctionUnit) {
                // check sap function and schema
                IMetadataTable table1 = ((IMetadataTable) object);
                return table1.getId().equals(((SAPFunctionUnit) parameter).getMetadataTable().getId());
            } else if (parameter instanceof Connection) {
                Set<MetadataTable> tables = ConnectionHelper.getTables((Connection) parameter);
                if (tables.size() == 1) {
                    IMetadataTable table1 = ((IMetadataTable) object);
                    MetadataTable table2 = (MetadataTable) tables.toArray(new MetadataTable[0])[0];
                    return table1.getId().equals(table2.getId());
                }
                if (parameter instanceof XmlFileConnection) {
                    boolean isResult = false;
                    for (MetadataTable table : tables) {
                        if (table.getId() != null && table.getId().equals(((IMetadataTable) object).getId())) {
                            isResult = true;
                            break;
                        }
                    }
                    return isResult;
                }
            }

        }
        /* table delete or reload */
        Object parameter2 = result.getParameter();
        if (object instanceof String && parameter2 instanceof List) {
            List listParameter = (List) parameter2;
            if (listParameter.get(1) instanceof EUpdateResult) {
                return true;
            }
        }
        return false;
    }

    private void updateConnection(ContextItem citem) throws PersistenceException {
        Map<ContextItem, Map<String, String>> renameMap = getContextRenamedMap();
        if (renameMap == null) {
            return;
        }
        Map<String, String> valueMap = renameMap.get(citem);
        if (valueMap == null) {
            return;
        }
        Set<String> set = valueMap.keySet();
        List<String> list = new ArrayList<String>(set);
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        for (String newValue : list) {
            String oldValue = valueMap.get(newValue);
            oldValue = "context." + oldValue;
            newValue = "context." + newValue;
            List<IRepositoryViewObject> dbConnList = factory.getAll(ERepositoryObjectType.METADATA_CONNECTIONS, true);
            for (IRepositoryViewObject obj : dbConnList) {
                Item item = obj.getProperty().getItem();
                if (item instanceof ConnectionItem) {
                    Connection conn = ((ConnectionItem) item).getConnection();
                    if (conn.isContextMode()) {
                        ContextItem contextItem = ContextUtils.getContextItemById2(conn.getContextId());
                        if (contextItem == null) {
                            continue;
                        }
                        if (citem == contextItem) {
                            if (conn instanceof DatabaseConnection) {
                                DatabaseConnection dbConn = (DatabaseConnection) conn;
                                if (dbConn.getAdditionalParams() != null && dbConn.getAdditionalParams().equals(oldValue)) {
                                    dbConn.setAdditionalParams(newValue);
                                } else if (dbConn.getUsername() != null && dbConn.getUsername().equals(oldValue)) {
                                    dbConn.setUsername(newValue);
                                } else if (dbConn.getPassword() != null && dbConn.getPassword().equals(oldValue)) {
                                    dbConn.setPassword(newValue);
                                } else if (dbConn.getServerName() != null && dbConn.getServerName().equals(oldValue)) {
                                    dbConn.setServerName(newValue);
                                } else if (dbConn.getPort() != null && dbConn.getPort().equals(oldValue)) {
                                    dbConn.setPort(newValue);
                                } else if (dbConn.getSID() != null && dbConn.getSID().equals(oldValue)) {
                                    dbConn.setSID(newValue);
                                } else if (dbConn.getDbmsId() != null && dbConn.getDbmsId().equals(oldValue)) {
                                    dbConn.setDbmsId(newValue);
                                } else if (dbConn.getDriverClass() != null && dbConn.getDriverClass().equals(oldValue)) {
                                    dbConn.setDriverClass(newValue);
                                } else if (dbConn.getDriverJarPath() != null && dbConn.getDriverJarPath().equals(oldValue)) {
                                    dbConn.setDriverJarPath(newValue);
                                } else if (dbConn.getURL() != null && dbConn.getURL().equals(oldValue)) {
                                    dbConn.setURL(newValue);
                                }
                                factory.save(item);
                            }
                        }

                    }
                }
            }
            List<IRepositoryViewObject> excelConnList = factory.getAll(ERepositoryObjectType.METADATA_FILE_EXCEL, true);
            for (IRepositoryViewObject obj : excelConnList) {
                Item item = obj.getProperty().getItem();
                if (item instanceof ConnectionItem) {
                    Connection conn = ((ConnectionItem) item).getConnection();
                    if (conn.isContextMode()) {
                        ContextItem contextItem = ContextUtils.getContextItemById2(conn.getContextId());
                        if (contextItem == null) {
                            continue;
                        }
                        if (citem == contextItem) {
                            if (conn instanceof FileExcelConnection) {
                                if (((FileExcelConnection) conn).getFirstColumn() != null
                                        && ((FileExcelConnection) conn).getFirstColumn().equals(oldValue)) {
                                    ((FileExcelConnection) conn).setFirstColumn(newValue);
                                } else if (((FileExcelConnection) conn).getLastColumn() != null
                                        && ((FileExcelConnection) conn).getLastColumn().equals(oldValue)) {
                                    ((FileExcelConnection) conn).setLastColumn(newValue);
                                } else if (((FileExcelConnection) conn).getThousandSeparator() != null
                                        && ((FileExcelConnection) conn).getThousandSeparator().equals(oldValue)) {
                                    ((FileExcelConnection) conn).setThousandSeparator(newValue);
                                } else if (((FileExcelConnection) conn).getDecimalSeparator() != null
                                        && ((FileExcelConnection) conn).getDecimalSeparator().equals(oldValue)) {
                                    ((FileExcelConnection) conn).setDecimalSeparator(newValue);
                                } else if (((FileExcelConnection) conn).getFilePath() != null
                                        && ((FileExcelConnection) conn).getFilePath().equals(oldValue)) {
                                    ((FileExcelConnection) conn).setFilePath(newValue);
                                } else if (((FileExcelConnection) conn).getEncoding() != null
                                        && ((FileExcelConnection) conn).getEncoding().equals(oldValue)) {
                                    ((FileExcelConnection) conn).setEncoding(newValue);
                                } else if (((FileExcelConnection) conn).getLimitValue() != null
                                        && ((FileExcelConnection) conn).getLimitValue().equals(oldValue)) {
                                    ((FileExcelConnection) conn).setLimitValue(newValue);
                                } else if (((FileExcelConnection) conn).getHeaderValue() != null
                                        && ((FileExcelConnection) conn).getHeaderValue().equals(oldValue)) {
                                    ((FileExcelConnection) conn).setHeaderValue(newValue);
                                } else if (((FileExcelConnection) conn).getFooterValue() != null
                                        && ((FileExcelConnection) conn).getFooterValue().equals(oldValue)) {
                                    ((FileExcelConnection) conn).setFooterValue(newValue);
                                }
                                factory.save(item);
                            }
                        }
                    }
                }
            }

            List<IRepositoryViewObject> deliConnList = factory.getAll(ERepositoryObjectType.METADATA_FILE_DELIMITED, true);
            for (IRepositoryViewObject obj : deliConnList) {
                Item item = obj.getProperty().getItem();
                if (item instanceof ConnectionItem) {
                    Connection conn = ((ConnectionItem) item).getConnection();
                    if (conn.isContextMode()) {
                        ContextItem contextItem = ContextUtils.getContextItemById2(conn.getContextId());
                        if (contextItem == null) {
                            continue;
                        }
                        if (citem == contextItem) {
                            if (conn instanceof DelimitedFileConnection) {
                                if (((DelimitedFileConnection) conn).getFilePath() != null
                                        && ((DelimitedFileConnection) conn).getFilePath().equals(oldValue)) {
                                    ((DelimitedFileConnection) conn).setFilePath(newValue);
                                } else if (((DelimitedFileConnection) conn).getEncoding() != null
                                        && ((DelimitedFileConnection) conn).getEncoding().equals(oldValue)) {
                                    ((DelimitedFileConnection) conn).setEncoding(newValue);
                                } else if (((DelimitedFileConnection) conn).getLimitValue() != null
                                        && ((DelimitedFileConnection) conn).getLimitValue().equals(oldValue)) {
                                    ((DelimitedFileConnection) conn).setLimitValue(newValue);
                                } else if (((DelimitedFileConnection) conn).getHeaderValue() != null
                                        && ((DelimitedFileConnection) conn).getHeaderValue().equals(oldValue)) {
                                    ((DelimitedFileConnection) conn).setHeaderValue(newValue);
                                } else if (((DelimitedFileConnection) conn).getFooterValue() != null
                                        && ((DelimitedFileConnection) conn).getFooterValue().equals(oldValue)) {
                                    ((DelimitedFileConnection) conn).setFooterValue(newValue);
                                } else if (((DelimitedFileConnection) conn).getRowSeparatorValue() != null
                                        && ((DelimitedFileConnection) conn).getRowSeparatorValue().equals(oldValue)) {
                                    ((DelimitedFileConnection) conn).setRowSeparatorValue(newValue);
                                } else if (((DelimitedFileConnection) conn).getFieldSeparatorValue() != null
                                        && ((DelimitedFileConnection) conn).getFieldSeparatorValue().equals(oldValue)) {
                                    ((DelimitedFileConnection) conn).setFieldSeparatorValue(newValue);
                                }
                                factory.save(item);
                            }

                        }
                    }
                }
            }
            List<IRepositoryViewObject> regConnList = factory.getAll(ERepositoryObjectType.METADATA_FILE_REGEXP, true);
            for (IRepositoryViewObject obj : regConnList) {
                Item item = obj.getProperty().getItem();
                if (item instanceof ConnectionItem) {
                    Connection conn = ((ConnectionItem) item).getConnection();
                    if (conn.isContextMode()) {
                        ContextItem contextItem = ContextUtils.getContextItemById2(conn.getContextId());
                        if (contextItem == null) {
                            continue;
                        }
                        if (citem == contextItem) {
                            if (conn instanceof RegexpFileConnection) {
                                if (((RegexpFileConnection) conn).getFilePath() != null
                                        && ((RegexpFileConnection) conn).getFilePath().equals(oldValue)) {
                                    ((RegexpFileConnection) conn).setFilePath(newValue);
                                } else if (((RegexpFileConnection) conn).getEncoding() != null
                                        && ((RegexpFileConnection) conn).getEncoding().equals(oldValue)) {
                                    ((RegexpFileConnection) conn).setEncoding(newValue);
                                } else if (((RegexpFileConnection) conn).getLimitValue() != null
                                        && ((RegexpFileConnection) conn).getLimitValue().equals(oldValue)) {
                                    ((RegexpFileConnection) conn).setLimitValue(newValue);
                                } else if (((RegexpFileConnection) conn).getHeaderValue() != null
                                        && ((RegexpFileConnection) conn).getHeaderValue().equals(oldValue)) {
                                    ((RegexpFileConnection) conn).setHeaderValue(newValue);
                                } else if (((RegexpFileConnection) conn).getFooterValue() != null
                                        && ((RegexpFileConnection) conn).getFooterValue().equals(oldValue)) {
                                    ((RegexpFileConnection) conn).setFooterValue(newValue);
                                } else if (((RegexpFileConnection) conn).getRowSeparatorValue() != null
                                        && ((RegexpFileConnection) conn).getRowSeparatorValue().equals(oldValue)) {
                                    ((RegexpFileConnection) conn).setRowSeparatorValue(newValue);
                                } else if (((RegexpFileConnection) conn).getFieldSeparatorValue() != null
                                        && ((RegexpFileConnection) conn).getFieldSeparatorValue().equals(oldValue)) {
                                    ((RegexpFileConnection) conn).setFieldSeparatorValue(newValue);
                                }
                                factory.save(item);
                            }

                        }
                    }
                }
            }
            List<IRepositoryViewObject> ldifConnList = factory.getAll(ERepositoryObjectType.METADATA_FILE_LDIF, true);
            for (IRepositoryViewObject obj : ldifConnList) {
                Item item = obj.getProperty().getItem();
                if (item instanceof ConnectionItem) {
                    Connection conn = ((ConnectionItem) item).getConnection();
                    if (conn.isContextMode()) {
                        ContextItem contextItem = ContextUtils.getContextItemById2(conn.getContextId());
                        if (contextItem == null) {
                            continue;
                        }
                        if (citem == contextItem) {
                            if (conn instanceof LdifFileConnection) {
                                LdifFileConnection dbConn = (LdifFileConnection) conn;
                                if (dbConn.getFilePath() != null && dbConn.getFilePath().equals(oldValue)) {
                                    dbConn.setFilePath(newValue);
                                }
                                factory.save(item);
                            }
                        }
                    }
                }
            }
            List<IRepositoryViewObject> posiConnList = factory.getAll(ERepositoryObjectType.METADATA_FILE_POSITIONAL, true);
            for (IRepositoryViewObject obj : posiConnList) {
                Item item = obj.getProperty().getItem();
                if (item instanceof ConnectionItem) {
                    Connection conn = ((ConnectionItem) item).getConnection();
                    if (conn.isContextMode()) {
                        ContextItem contextItem = ContextUtils.getContextItemById2(conn.getContextId());
                        if (contextItem == null) {
                            continue;
                        }
                        if (citem == contextItem) {
                            if (conn instanceof PositionalFileConnection) {
                                PositionalFileConnection dbConn = (PositionalFileConnection) conn;
                                if (dbConn.getFilePath() != null && dbConn.getFilePath().equals(oldValue)) {
                                    dbConn.setFilePath(newValue);
                                } else if (dbConn.getEncoding() != null && dbConn.getEncoding().equals(oldValue)) {
                                    dbConn.setEncoding(newValue);
                                } else if (dbConn.getLimitValue() != null && dbConn.getLimitValue().equals(oldValue)) {
                                    dbConn.setLimitValue(newValue);
                                } else if (dbConn.getHeaderValue() != null && dbConn.getHeaderValue().equals(oldValue)) {
                                    dbConn.setHeaderValue(newValue);
                                } else if (dbConn.getFooterValue() != null && dbConn.getFooterValue().equals(oldValue)) {
                                    dbConn.setFooterValue(newValue);
                                } else if (dbConn.getRowSeparatorValue() != null
                                        && dbConn.getRowSeparatorValue().equals(oldValue)) {
                                    dbConn.setRowSeparatorValue(newValue);
                                } else if (dbConn.getFieldSeparatorValue() != null
                                        && dbConn.getFieldSeparatorValue().equals(oldValue)) {
                                    dbConn.setFieldSeparatorValue(newValue);
                                }
                                factory.save(item);
                            }

                        }
                    }
                }
            }
            List<IRepositoryViewObject> xmlConnList = factory.getAll(ERepositoryObjectType.METADATA_FILE_XML, true);
            for (IRepositoryViewObject obj : xmlConnList) {
                Item item = obj.getProperty().getItem();
                if (item instanceof ConnectionItem) {
                    Connection conn = ((ConnectionItem) item).getConnection();
                    if (conn.isContextMode()) {
                        ContextItem contextItem = ContextUtils.getContextItemById2(conn.getContextId());
                        if (contextItem == null) {
                            continue;
                        }
                        if (citem == contextItem) {
                            if (conn instanceof XmlFileConnection) {
                                XmlFileConnection dbConn = (XmlFileConnection) conn;
                                if (dbConn.getXmlFilePath() != null && dbConn.getXmlFilePath().equals(oldValue)) {
                                    dbConn.setXmlFilePath(newValue);
                                } else if (dbConn.getEncoding() != null && dbConn.getEncoding().equals(oldValue)) {
                                    dbConn.setEncoding(newValue);
                                } else if (dbConn.getOutputFilePath() != null && dbConn.getOutputFilePath().equals(oldValue)) {
                                    dbConn.setOutputFilePath(newValue);
                                }
                                EList schema = dbConn.getSchema();
                                if (schema != null && schema.size() > 0) {
                                    if (schema.get(0) instanceof XmlXPathLoopDescriptor) {
                                        XmlXPathLoopDescriptor descriptor = (XmlXPathLoopDescriptor) schema.get(0);
                                        if (descriptor.getAbsoluteXPathQuery() != null
                                                && descriptor.getAbsoluteXPathQuery().equals(oldValue)) {
                                            descriptor.setAbsoluteXPathQuery(newValue);
                                        }
                                    }
                                }
                                factory.save(item);
                            }
                        }
                    }
                }
            }
            List<IRepositoryViewObject> saleConnList = factory.getAll(ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA, true);
            for (IRepositoryViewObject obj : saleConnList) {
                Item item = obj.getProperty().getItem();
                if (item instanceof ConnectionItem) {
                    Connection conn = ((ConnectionItem) item).getConnection();
                    if (conn.isContextMode()) {
                        ContextItem contextItem = ContextUtils.getContextItemById2(conn.getContextId());
                        if (contextItem == null) {
                            continue;
                        }
                        if (citem == contextItem) {
                            if (conn instanceof SalesforceSchemaConnection) {
                                SalesforceSchemaConnection dbConn = (SalesforceSchemaConnection) conn;
                                if (dbConn.getWebServiceUrl() != null && dbConn.getWebServiceUrl().equals(oldValue)) {
                                    dbConn.setWebServiceUrl(newValue);
                                } else if (dbConn.getPassword() != null && dbConn.getPassword().equals(oldValue)) {
                                    dbConn.setPassword(newValue);
                                } else if (dbConn.getUserName() != null && dbConn.getUserName().equals(oldValue)) {
                                    dbConn.setUserName(newValue);
                                } else if (dbConn.getTimeOut() != null && dbConn.getTimeOut().equals(oldValue)) {
                                    dbConn.setTimeOut(newValue);
                                } else if (dbConn.getBatchSize() != null && dbConn.getBatchSize().equals(oldValue)) {
                                    dbConn.setBatchSize(newValue);
                                } else if (dbConn.getQueryCondition() != null && dbConn.getQueryCondition().equals(oldValue)) {
                                    dbConn.setQueryCondition(newValue);
                                }
                                factory.save(item);
                            }
                        }
                    }
                }
            }
            List<IRepositoryViewObject> wsdlConnList = factory.getAll(ERepositoryObjectType.METADATA_WSDL_SCHEMA, true);
            for (IRepositoryViewObject obj : wsdlConnList) {
                Item item = obj.getProperty().getItem();
                if (item instanceof ConnectionItem) {
                    Connection conn = ((ConnectionItem) item).getConnection();
                    if (conn.isContextMode()) {
                        ContextItem contextItem = ContextUtils.getContextItemById2(conn.getContextId());
                        if (contextItem == null) {
                            continue;
                        }
                        if (citem == contextItem) {
                            if (conn instanceof WSDLSchemaConnection) {
                                WSDLSchemaConnection dbConn = (WSDLSchemaConnection) conn;
                                if (dbConn.getUserName() != null && dbConn.getUserName().equals(oldValue)) {
                                    dbConn.setUserName(newValue);
                                } else if (dbConn.getPassword() != null && dbConn.getPassword().equals(oldValue)) {
                                    dbConn.setPassword(newValue);
                                } else if (dbConn.getProxyHost() != null && dbConn.getProxyHost().equals(oldValue)) {
                                    dbConn.setProxyHost(newValue);
                                } else if (dbConn.getProxyPassword() != null && dbConn.getProxyPassword().equals(oldValue)) {
                                    dbConn.setProxyPassword(newValue);
                                } else if (dbConn.getProxyUser() != null && dbConn.getProxyUser().equals(oldValue)) {
                                    dbConn.setProxyUser(newValue);
                                } else if (dbConn.getProxyPort() != null && dbConn.getProxyPort().equals(oldValue)) {
                                    dbConn.setProxyPort(newValue);
                                }
                                factory.save(item);
                            }
                        }
                    }
                }
            }
        }

    }

    public static IEditorReference[] getEditors() {
        final List<IEditorReference> list = new ArrayList<IEditorReference>();
        Display.getDefault().syncExec(new Runnable() {

            public void run() {
                IEditorReference[] reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .getEditorReferences();
                list.addAll(Arrays.asList(reference));
            }
        });
        return list.toArray(new IEditorReference[0]);
    }

    /**
     * 
     * ggu Comment method "checkJobItemsForUpdate".
     * 
     * @param types - need update types of jobs.
     * @param sourceIdMap - map old source id to new one.
     * @param sourceItem - modified repository item.
     * @return
     */
    private List<UpdateResult> checkJobItemsForUpdate(IProgressMonitor parentMonitor, final Set<EUpdateItemType> types,
            final boolean onlySimpleShow) throws InterruptedException {
        if (types == null || types.isEmpty()) {
            return null;
        }

        final List<IEditorReference> list = new ArrayList<IEditorReference>();
        Display.getDefault().syncExec(new Runnable() {

            public void run() {
                IEditorReference[] reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .getEditorReferences();
                list.addAll(Arrays.asList(reference));
            }
        });

        List<IProcess2> openedProcessList = CoreRuntimePlugin.getInstance().getDesignerCoreService()
                .getOpenedProcess(getEditors());

        try {

            List<UpdateResult> resultList = new ArrayList<UpdateResult>();
            int size = openedProcessList.size();
            IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();

            MultiKeyMap openProcessMap = createOpenProcessMap(openedProcessList);
            if (isItemIndexChecked() && parameter != null && relations != null) {
                size = size + relations.size();
                parentMonitor.beginTask(Messages.getString("RepositoryUpdateManager.Check"), size); //$NON-NLS-1$
                parentMonitor.setTaskName(Messages.getString("RepositoryUpdateManager.ItemsToUpdate")); //$NON-NLS-1$

                for (int i = 0; i < relations.size(); i++) {
                    RelationshipItemBuilder.Relation relation = relations.get(i);
                    IRepositoryViewObject relatedObj = factory.getLastVersion(relation.getId());

                    if (relatedObj == null) {
                        continue;
                    }
                    List<IRepositoryViewObject> allVersionList = new ArrayList<IRepositoryViewObject>();
                    //
                    if (!onlyOpeningJob) {

                        // must match TalendDesignerPrefConstants.CHECK_ONLY_LAST_VERSION
                        boolean checkOnlyLastVersion = Boolean.parseBoolean(CoreRuntimePlugin.getInstance()
                                .getDesignerCoreService().getPreferenceStore("checkOnlyLastVersion")); //$NON-NLS-1$
                        // get all version
                        allVersionList = new ArrayList<IRepositoryViewObject>();
                        if (!checkOnlyLastVersion) {
                            List<IRepositoryViewObject> allVersion = factory.getAllVersion(relatedObj.getId());
                            for (IRepositoryViewObject object : allVersion) {
                                if (factory.getStatus(object) != ERepositoryStatus.LOCK_BY_OTHER) {
                                    allVersionList.add(object);
                                }
                            }
                        } else {
                            // assume that repositoryObj is the last version, otherwise we should call
                            // factory.getLastVersion(repositoryObj.getId());
                            IRepositoryViewObject lastVersion = relatedObj; // factory.getLastVersion(repositoryObj.getId());
                            ERepositoryStatus status = factory.getStatus(lastVersion);
                            if (status != ERepositoryStatus.LOCK_BY_OTHER) {
                                allVersionList.add(lastVersion);
                            }
                        }

                        //

                        checkMonitorCanceled(parentMonitor);

                        int index = 0;
                        for (IRepositoryViewObject repositoryObj : allVersionList) {
                            checkMonitorCanceled(parentMonitor);
                            Item item = repositoryObj.getProperty().getItem();
                            // avoid the opened job
                            if (isOpenedItem(item, openProcessMap)) {
                                continue;
                            }
                            parentMonitor.subTask(getUpdateJobInfor(repositoryObj.getProperty()));
                            List<UpdateResult> updatesNeededFromItems = getUpdatesNeededFromItems(parentMonitor, item, types,
                                    onlySimpleShow);
                            if (updatesNeededFromItems != null && !updatesNeededFromItems.isEmpty()) {
                                resultList.addAll(updatesNeededFromItems);
                            }
                            index++;

                            if (!ERepositoryStatus.LOCK_BY_USER.equals(factory.getStatus(item))) {
                                if (repositoryObj instanceof RepositoryObject) {
                                    ((RepositoryObject) repositoryObj).unload();
                                }
                            }

                        }
                        parentMonitor.worked(1);
                    }

                }
            } else {
                parentMonitor.beginTask(Messages.getString("RepositoryUpdateManager.Check"), size); //$NON-NLS-1$
                parentMonitor.setTaskName(Messages.getString("RepositoryUpdateManager.ItemsToUpdate")); //$NON-NLS-1$

            }
            // opened job
            for (IProcess2 process : openedProcessList) {
                checkMonitorCanceled(parentMonitor);
                parentMonitor.subTask(getUpdateJobInfor(process.getProperty()));

                List<UpdateResult> resultFromProcess = getResultFromProcess(process, types, onlySimpleShow);
                if (resultFromProcess != null) {
                    resultList.addAll(resultFromProcess);
                }
                parentMonitor.worked(1);
            }

            if (isDetectAndUpdate) {
                resultList = updateAllProcess(parentMonitor, resultList, openedProcessList, types, onlySimpleShow);
            }

            if (!onlyOpeningJob) {
                // Ok, you also need to update the job setting in "create job with template"
                List<UpdateResult> templateSetUpdate = checkSettingInJobTemplateWizard();
                if (templateSetUpdate != null) {
                    resultList.addAll(templateSetUpdate);
                }
            }
            parentMonitor.done();
            return resultList;
        } catch (PersistenceException e) {
            //
        }

        return null;
    }

    private List<UpdateResult> updateAllProcess(IProgressMonitor parentMonitor, List<UpdateResult> resultList,
            List<IProcess2> openedProcessList, final Set<EUpdateItemType> types, final boolean onlySimpleShow) {
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        if (factory == null) {
            return resultList;
        }
        IDesignerCoreService designerCoreService = CoreRuntimePlugin.getInstance().getDesignerCoreService();
        if (designerCoreService == null) {
            return resultList;
        }
        List<IRepositoryViewObject> processRep = new ArrayList<IRepositoryViewObject>();
        List<IProcess> processList = new ArrayList<IProcess>();
        try {
            processRep.addAll(factory.getAll(ERepositoryObjectType.PROCESS, true));
            processRep.addAll(factory.getAll(ERepositoryObjectType.JOBLET, true));
            for (IRepositoryViewObject obj : processRep) {
                Item item = obj.getProperty().getItem();
                IProcess process = null;
                if (item instanceof ProcessItem) {
                    process = designerCoreService.getProcessFromProcessItem((ProcessItem) item);
                } else if (item instanceof JobletProcessItem) {
                    process = designerCoreService.getProcessFromJobletProcessItem((JobletProcessItem) item);
                }
                processList.add(process);
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }

        // all the jobs
        for (IProcess process : processList) {
            if (process instanceof IProcess2) {
                IProcess2 ip2 = (IProcess2) process;
                boolean found = false;
                for (IProcess2 open : openedProcessList) {
                    if (open.getId().equals(ip2.getId())) {
                        found = true;
                    }
                }
                if (found) {
                    continue;
                }
                checkMonitorCanceled(parentMonitor);
                parentMonitor.subTask(getUpdateJobInfor(ip2.getProperty()));

                // List<UpdateResult> resultFromProcess = getResultFromProcess(process, types, onlySimpleShow);

                List<UpdateResult> resultFromProcess = getUpdatesNeededFromItems(parentMonitor, ((IProcess2) process)
                        .getProperty().getItem(), types, onlySimpleShow);
                if (resultFromProcess != null) {
                    resultList.addAll(resultFromProcess);
                }
                parentMonitor.worked(1);
            }

        }
        return resultList;
    }

    private void checkMonitorCanceled(IProgressMonitor monitor) {
        if (monitor.isCanceled()) {
            throw new OperationCanceledException(UpdatesConstants.MONITOR_IS_CANCELED);
        }
    }

    /**
     * YeXiaowei Comment method "checkSettingInJobTemplateWizard".
     */
    private List<UpdateResult> checkSettingInJobTemplateWizard() {
        List<IProcess> processes = CoreRuntimePlugin.getInstance().getDesignerCoreService().getProcessForJobTemplate();

        if (processes == null || processes.isEmpty()) {
            return null;
        }

        List<UpdateResult> result = new ArrayList<UpdateResult>();

        for (IProcess process : processes) {
            if (process instanceof IProcess2) {
                IProcess2 nowProcess = (IProcess2) process;
                nowProcess.getUpdateManager().checkAllModification();
                List<UpdateResult> results = nowProcess.getUpdateManager().getUpdatesNeeded();
                if (results != null) {
                    result.addAll(results);
                }
            }
        }

        return result;
    }

    /**
     * Create a hashmap for fash lookup of the specified IProcess.
     * 
     * @param openedProcessList
     * @return
     */
    private MultiKeyMap createOpenProcessMap(List<IProcess2> openedProcessList) {
        MultiKeyMap map = new MultiKeyMap();
        if (openedProcessList != null) {
            for (IProcess2 process : openedProcessList) {
                map.put(process.getId(), process.getName(), process.getVersion(), process);
            }
        }
        return map;
    }

    private boolean isOpenedItem(Item openedItem, MultiKeyMap openProcessMap) {
        if (openedItem == null) {
            return false;
        }
        Property property = openedItem.getProperty();
        return (openProcessMap.get(property.getId(), property.getLabel(), property.getVersion()) != null);
    }

    protected List<UpdateResult> getResultFromProcess(IProcess process, final Set<EUpdateItemType> types,
            final boolean onlySimpleShow) {
        if (process == null || types == null) {
            return null;
        }

        List<UpdateResult> resultList = new ArrayList<UpdateResult>();
        if (process instanceof IProcess2) {
            IProcess2 process2 = (IProcess2) process;
            // context rename and context group
            IContextManager contextManager = process2.getContextManager();
            if (contextManager instanceof JobContextManager) {
                JobContextManager jobContextManager = (JobContextManager) contextManager;
                jobContextManager.setRepositoryRenamedMap(getContextRenamedMap());
                jobContextManager.setNewParametersMap(getNewParametersMap());
                Map<ContextItem, List<IContext>> repositoryAddGroupContext = getRepositoryAddGroupContext();

                List<IContext> listIContext = new ArrayList<IContext>();
                for (ContextItem item : repositoryAddGroupContext.keySet()) {
                    List<IContext> list = repositoryAddGroupContext.get(item);
                    ListIterator<IContext> listIterator = list.listIterator();
                    while (listIterator.hasNext()) {
                        IContext context = listIterator.next();
                        JobContext newJobContext = new JobContext(context.getName());
                        List<IContextParameter> existedParameters = new ArrayList<IContextParameter>();

                        for (int j = 0; j < context.getContextParameterList().size(); j++) {
                            IContextParameter param = context.getContextParameterList().get(j);
                            IContextParameter contextParameter = jobContextManager.getDefaultContext().getContextParameter(
                                    param.getName());
                            if (contextParameter != null && param.getName().equals(contextParameter.getName())
                                    && item.getProperty().getId().equals(contextParameter.getSource())) { // found
                                IContextParameter clone = param.clone();
                                clone.setContext(newJobContext);
                                existedParameters.add(clone);
                            }
                        }
                        if (!existedParameters.isEmpty()) {
                            newJobContext.setContextParameterList(existedParameters);
                            listIContext.add(newJobContext);
                        }
                    }

                }
                jobContextManager.setAddGroupContext(listIContext);
                jobContextManager.setAddContextGroupMap(repositoryAddGroupContext);

                Map<ContextItem, List<IContext>> repositoryRemoveGroupContext = getRepositoryRemoveGroupContext();

                List<IContext> removeListIContext = new ArrayList<IContext>();
                for (ContextItem item : repositoryRemoveGroupContext.keySet()) {
                    List<IContext> list = repositoryRemoveGroupContext.get(item);
                    ListIterator<IContext> listIterator = list.listIterator();
                    while (listIterator.hasNext()) {
                        IContext context = listIterator.next();

                        if (!removeListIContext.contains(context)) {
                            removeListIContext.add(context);
                        }

                    }

                }
                jobContextManager.setRemoveGroupContext(removeListIContext);
                jobContextManager.setRemoveContextGroupMap(repositoryRemoveGroupContext);

                Map<ContextItem, List<IContext>> repositoryRenameGroupContext = getRepositoryRenameGroupContext();

                jobContextManager.setRenameGroupContext(getRenameContextGroup());
                jobContextManager.setRenameContextGroupMap(repositoryRenameGroupContext);
            }
            // schema
            IUpdateManager updateManager = process2.getUpdateManager();
            if (updateManager instanceof AbstractUpdateManager) {
                AbstractUpdateManager manager = (AbstractUpdateManager) updateManager;
                if (getSchemaRenamedMap() != null && !getSchemaRenamedMap().isEmpty()) {
                    manager.setSchemaRenamedMap(getSchemaRenamedMap());
                }
                if (getDeletedOrReselectTablesMap() != null && !getDeletedOrReselectTablesMap().isEmpty()) {
                    manager.setDeletedOrReselectTablesMap(getDeletedOrReselectTablesMap());
                }
                manager.setFromRepository(true);
            }
            //
            for (EUpdateItemType type : types) {
                List<UpdateResult> updatesNeeded = updateManager.getUpdatesNeeded(type, onlySimpleShow);
                if (updatesNeeded != null) {
                    resultList.addAll(updatesNeeded);
                }
            }
            if (updateManager instanceof AbstractUpdateManager) {
                AbstractUpdateManager manager = (AbstractUpdateManager) updateManager;
                manager.setFromRepository(false);
            }
        }
        return resultList;
    }

    protected List<UpdateResult> getUpdatesNeededFromItems(IProgressMonitor parentMonitor, Item item,
            final Set<EUpdateItemType> types, final boolean onlySimpleShow) {
        if (item == null || types == null) {
            return null;
        }
        IDesignerCoreService designerCoreService = CoreRuntimePlugin.getInstance().getDesignerCoreService();
        if (designerCoreService == null) {
            return null;
        }
        //
        IProcess process = null;
        if (item instanceof ProcessItem) {
            process = designerCoreService.getProcessFromProcessItem((ProcessItem) item);
        } else if (item instanceof JobletProcessItem) {
            process = designerCoreService.getProcessFromJobletProcessItem((JobletProcessItem) item);
        }
        //
        if (process != null && process instanceof IProcess2) {
            IProcess2 process2 = (IProcess2) process;
            // for save item
            List<UpdateResult> resultFromProcess = getResultFromProcess(process2, types, onlySimpleShow);

            for (UpdateResult result : resultFromProcess) {
                if (result.getJob() != null) {
                    result.setJob(null);
                }
                result.setFromItem(true);
                result.setObjectId(item.getProperty().getId());
            }

            process2.dispose();
            return resultFromProcess;

        }
        return null;
    }

    public static ERepositoryObjectType getTypeFromSource(String source) {
        if (source == null) {
            return null;
        }
        for (ERepositoryObjectType type : (ERepositoryObjectType[]) ERepositoryObjectType.values()) {
            String alias = type.getAlias();
            if (alias != null && source.startsWith(alias)) {
                return type;
            }
        }
        return null;
    }

    public static String getUpdateJobInfor(Property property) {
        StringBuffer infor = new StringBuffer();
        String prefix = UpdatesConstants.JOB;
        String label = null;
        String version = null;
        if (property.getItem() instanceof JobletProcessItem) { // for joblet
            prefix = UpdatesConstants.JOBLET;
        }
        label = property.getLabel();
        version = property.getVersion();
        infor.append(prefix);
        if (label != null) {
            infor.append(UpdatesConstants.SPACE);
            infor.append(label);
            infor.append(UpdatesConstants.SPACE);
            infor.append(version);
        }
        return infor.toString();

    }

    /**
     * 
     * ggu Comment method "updateSchema".
     * 
     * for repository wizard.
     */
    public static boolean updateDBConnection(ConnectionItem connection) {
        return updateDBConnection(connection, true, false);
    }

    /**
     * 
     * hwang Comment method "updateServices".
     * 
     * for repository wizard.
     */
    public static boolean updateServices(ConnectionItem connection) {
        return updateServices(connection, true, false);
    }

    public static boolean updateDBConnection(ConnectionItem connection, boolean show) {
        return updateDBConnection(connection, show, false);
    }

    /**
     * 
     * ggu Comment method "updateQuery".
     * 
     * if show is false, will work for context menu action.
     */
    public static boolean updateDBConnection(ConnectionItem connectionItem, boolean show, final boolean onlySimpleShow) {
        List<IRepositoryViewObject> updateList = new ArrayList<IRepositoryViewObject>();
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        List<RelationshipItemBuilder.Relation> relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(
                connectionItem.getProperty().getId(), RelationshipItemBuilder.LATEST_VERSION,
                RelationshipItemBuilder.PROPERTY_RELATION);

        RepositoryUpdateManager repositoryUpdateManager = new RepositoryUpdateManager(connectionItem.getConnection(), relations) {

            @Override
            public Set<EUpdateItemType> getTypes() {
                Set<EUpdateItemType> types = new HashSet<EUpdateItemType>();
                types.add(EUpdateItemType.NODE_PROPERTY);
                types.add(EUpdateItemType.JOB_PROPERTY_EXTRA);
                types.add(EUpdateItemType.JOB_PROPERTY_STATS_LOGS);
                types.add(EUpdateItemType.JOB_PROPERTY_HEADERFOOTER);

                return types;
            }

        };
        return repositoryUpdateManager.doWork(true, false);
    }

    /**
     * 
     * hwang Comment method "updateServices".
     * 
     * if show is false, will work for context menu action.
     */
    public static boolean updateServices(ConnectionItem connectionItem, boolean show, final boolean onlySimpleShow) {
        List<IRepositoryViewObject> updateList = new ArrayList<IRepositoryViewObject>();
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        List<RelationshipItemBuilder.Relation> relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(
                connectionItem.getProperty().getId(), RelationshipItemBuilder.LATEST_VERSION,
                RelationshipItemBuilder.SERVICES_RELATION);

        RepositoryUpdateManager repositoryUpdateManager = new RepositoryUpdateManager(connectionItem.getConnection(), relations) {

            @Override
            public Set<EUpdateItemType> getTypes() {
                Set<EUpdateItemType> types = new HashSet<EUpdateItemType>();
                types.add(EUpdateItemType.NODE_PROPERTY);
                types.add(EUpdateItemType.JOB_PROPERTY_EXTRA);
                types.add(EUpdateItemType.JOB_PROPERTY_STATS_LOGS);
                types.add(EUpdateItemType.JOB_PROPERTY_HEADERFOOTER);

                return types;
            }

        };
        return repositoryUpdateManager.doWork(true, false);
    }

    /**
     * 
     * ggu Comment method "updateSchema".
     * 
     * for repository wizard.
     */
    public static boolean updateFileConnection(ConnectionItem connection) {
        return updateFileConnection(connection, true, false);
    }

    /**
     * 
     * ggu Comment method "updateQuery".
     * 
     * if show is false, will work for context menu action.
     */
    public static boolean updateFileConnection(ConnectionItem connectionItem, boolean show, boolean onlySimpleShow) {
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        List<IRepositoryViewObject> updateList = new ArrayList<IRepositoryViewObject>();
        List<RelationshipItemBuilder.Relation> relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(
                connectionItem.getProperty().getId(), RelationshipItemBuilder.LATEST_VERSION,
                RelationshipItemBuilder.PROPERTY_RELATION);

        RepositoryUpdateManager repositoryUpdateManager = new RepositoryUpdateManager(connectionItem.getConnection(), relations) {

            @Override
            public Set<EUpdateItemType> getTypes() {
                Set<EUpdateItemType> types = new HashSet<EUpdateItemType>();
                types.add(EUpdateItemType.NODE_PROPERTY);
                types.add(EUpdateItemType.NODE_SCHEMA);
                types.add(EUpdateItemType.JOB_PROPERTY_HEADERFOOTER);
                types.add(EUpdateItemType.NODE_SAP_IDOC);
                return types;
            }

        };
        return repositoryUpdateManager.doWork(show, onlySimpleShow);
    }

    /**
     * DOC ycbai Comment method "updateValidationRuleConnection".
     * 
     * @param connection
     * @return
     */
    public static boolean updateValidationRuleConnection(ConnectionItem connection) {
        return updateValidationRuleConnection(connection, true, false);
    }

    /**
     * DOC ycbai Comment method "updateValidationRuleConnection".
     * 
     * @param connectionItem
     * @param show
     * @param onlySimpleShow
     * @return
     */
    public static boolean updateValidationRuleConnection(ConnectionItem connectionItem, boolean show, boolean onlySimpleShow) {
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        List<IRepositoryViewObject> updateList = new ArrayList<IRepositoryViewObject>();
        List<RelationshipItemBuilder.Relation> relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(
                connectionItem.getProperty().getId(), RelationshipItemBuilder.LATEST_VERSION,
                RelationshipItemBuilder.VALIDATION_RULE_RELATION);

        RepositoryUpdateManager repositoryUpdateManager = new RepositoryUpdateManager(connectionItem.getConnection(), relations) {

            @Override
            public Set<EUpdateItemType> getTypes() {
                Set<EUpdateItemType> types = new HashSet<EUpdateItemType>();
                types.add(EUpdateItemType.NODE_VALIDATION_RULE);
                return types;
            }

        };
        return repositoryUpdateManager.doWork(show, onlySimpleShow);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getTableIdAndNameMap(ConnectionItem connItem) {
        if (connItem == null) {
            return Collections.emptyMap();
        }
        Map<String, String> idAndNameMap = new HashMap<String, String>();
        Set<MetadataTable> tables = ConnectionHelper.getTables(connItem.getConnection());
        if (tables != null) {
            for (MetadataTable table : (Set<MetadataTable>) tables) {
                idAndNameMap.put(table.getId(), table.getLabel());
            }
        }
        return idAndNameMap;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getTableIdAndNameMap(SAPFunctionUnit functionUnit) {
        if (functionUnit == null) {
            return Collections.emptyMap();
        }
        Map<String, String> idAndNameMap = new HashMap<String, String>();
        EList tables = functionUnit.getTables();
        if (tables != null) {
            for (MetadataTable table : (List<MetadataTable>) tables) {
                idAndNameMap.put(table.getId(), table.getLabel());
            }
        }
        return idAndNameMap;
    }

    public static Map<String, String> getOldTableIdAndNameMap(ConnectionItem connItem, MetadataTable metadataTable,
            boolean creation) {
        Map<String, String> oldTableMap = getTableIdAndNameMap(connItem);
        if (creation && metadataTable != null) {
            oldTableMap.remove(metadataTable.getId());
        }
        return oldTableMap;
    }

    public static Map<String, String> getOldTableIdAndNameMap(SAPFunctionUnit functionUnit, MetadataTable metadataTable,
            boolean creation) {
        Map<String, String> oldTableMap = getTableIdAndNameMap(functionUnit);
        if (creation && metadataTable != null) {
            oldTableMap.remove(metadataTable.getId());
        }
        return oldTableMap;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getSchemaRenamedMap(ConnectionItem connItem, Map<String, String> oldTableMap) {
        if (connItem == null || oldTableMap == null) {
            return Collections.emptyMap();
        }

        Map<String, String> schemaRenamedMap = new HashMap<String, String>();

        final String prefix = connItem.getProperty().getId() + UpdatesConstants.SEGMENT_LINE;
        Set<MetadataTable> tables = ConnectionHelper.getTables(connItem.getConnection());
        if (tables != null) {
            for (MetadataTable table : (Set<MetadataTable>) tables) {
                String oldName = oldTableMap.get(table.getId());
                String newName = table.getLabel();
                if (oldName != null && !oldName.equals(newName)) {
                    schemaRenamedMap.put(prefix + oldName, prefix + newName);
                }
            }
        }
        return schemaRenamedMap;
    }

    /**
     * 
     * overload the method for TDQ-3930.
     * 
     * @param connection
     * @param property
     * @param oldTableMap
     * @return
     */
    public static Map<String, String> getSchemaRenamedMap(Connection connection, Property property,
            Map<String, String> oldTableMap) {
        if (connection == null || oldTableMap == null) {
            return Collections.emptyMap();
        }

        Map<String, String> schemaRenamedMap = new HashMap<String, String>();

        final String prefix = property.getId() + UpdatesConstants.SEGMENT_LINE;
        Set<MetadataTable> tables = ConnectionHelper.getTables(connection);
        if (tables != null) {
            for (MetadataTable table : (Set<MetadataTable>) tables) {
                String oldName = oldTableMap.get(table.getId());
                String newName = table.getLabel();
                if (oldName != null && !oldName.equals(newName)) {
                    schemaRenamedMap.put(prefix + oldName, prefix + newName);
                }
            }
        }
        return schemaRenamedMap;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getSchemaRenamedMap(SAPFunctionUnit functionUnit, ConnectionItem connItem,
            Map<String, String> oldTableMap) {
        if (functionUnit == null || oldTableMap == null) {
            return Collections.emptyMap();
        }

        Map<String, String> schemaRenamedMap = new HashMap<String, String>();

        final String prefix = connItem.getProperty().getId() + UpdatesConstants.SEGMENT_LINE;
        EList tables = functionUnit.getTables();
        if (tables != null) {
            for (MetadataTable table : (List<MetadataTable>) tables) {
                String oldName = oldTableMap.get(table.getId());
                String newName = table.getLabel();
                if (oldName != null && !oldName.equals(newName)) {
                    schemaRenamedMap.put(prefix + oldName, prefix + newName);
                }
            }
        }
        return schemaRenamedMap;
    }

    /**
     * 
     * ggu Comment method "updateSchema".
     * 
     * for repository wizard.
     */
    public static boolean updateSingleSchema(ConnectionItem connItem, final MetadataTable newTable,
            final IMetadataTable oldMetadataTable, Map<String, String> oldTableMap) {
        if (connItem == null) {
            return false;
        }
        Map<String, String> schemaRenamedMap = RepositoryUpdateManager.getSchemaRenamedMap(connItem, oldTableMap);
        boolean update = !schemaRenamedMap.isEmpty();

        if (!update) {
            if (newTable != null && oldMetadataTable != null && oldTableMap.containsKey(newTable.getId())) {
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IMetadataManagmentService.class)) {
                    IMetadataManagmentService service = (IMetadataManagmentService) GlobalServiceRegister.getDefault()
                            .getService(IMetadataManagmentService.class);
                    IMetadataTable newMetadataTable = service.convertMetadataTable(newTable);
                    update = !oldMetadataTable.sameMetadataAs(newMetadataTable, IMetadataColumn.OPTIONS_NONE);
                }
            }
        }
        if (update) {
            // update
            return updateSchema(newTable, connItem, schemaRenamedMap, true, false);
        }
        return false;
    }

    public static boolean updateMultiSchema(ConnectionItem connItem, List<IMetadataTable> oldMetadataTable,
            Map<String, String> oldTableMap) {
        if (connItem == null) {
            return false;
        }
        Map<String, String> schemaRenamedMap = RepositoryUpdateManager.getSchemaRenamedMap(connItem, oldTableMap);
        Map<String, EUpdateResult> deletedOrReselectTablesMap = null;
        boolean update = !schemaRenamedMap.isEmpty();
        boolean isDeleteOrReselect = false;
        Connection connection = connItem.getConnection();

        if (!update) {
            if (oldMetadataTable != null) {
                List<IMetadataTable> newMetadataTable = RepositoryUpdateManager.getConversionMetadataTables(connItem
                        .getConnection());
                update = !RepositoryUpdateManager.sameAsMetadatTable(newMetadataTable, oldMetadataTable, oldTableMap);
            }
        }
        /* if table has been deselect and select again,should propgate the update dialog */
        if (!update) {
            deletedOrReselectTablesMap = new HashMap<String, EUpdateResult>();

            List<IMetadataTable> newMetadataTable = new ArrayList<IMetadataTable>();
            if (coreService != null && connection instanceof DatabaseConnection) {
                Set<org.talend.core.model.metadata.builder.connection.MetadataTable> newTables = ConnectionHelper
                        .getTables(connection);
                if (newTables != null) {
                    for (org.talend.core.model.metadata.builder.connection.MetadataTable originalTable : newTables) {
                        IMetadataTable conversionTable = coreService.convert(originalTable);
                        newMetadataTable.add(conversionTable);
                    }
                }
            }
            isDeleteOrReselect = isDeleteOrReselectMap(connItem, newMetadataTable, oldMetadataTable, deletedOrReselectTablesMap);
        }
        // update
        if (update) {
            return updateSchema(connItem, connItem, schemaRenamedMap, true, false);
        } else if (isDeleteOrReselect) {
            return updateDeleteOrReselectSchema(connItem, connItem, deletedOrReselectTablesMap, true, false);
        }
        return false;

    }

    /**
     * DOC hywang Comment method "updateDeleteOrReselectSchema".
     */
    private static boolean updateDeleteOrReselectSchema(Object table, ConnectionItem connItem,
            Map<String, EUpdateResult> deletedOrReselectTablesMap, boolean show, boolean onlySimpleShow) {

        List<IRepositoryViewObject> updateList = new ArrayList<IRepositoryViewObject>();
        List<RelationshipItemBuilder.Relation> relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(
                ((ConnectionItem) connItem).getProperty().getId(), ItemCacheManager.LATEST_VERSION,
                RelationshipItemBuilder.PROPERTY_RELATION);

        /*
         * the id for schema which stored in .project file is like "_dlkjfhjkdfioi - metadata",not only indicate by a
         * single id but also table name,so if only find the relations by id and
         * RelationshipItemBuilder.PROPERTY_RELATION,it can't find
         */
        if (connItem instanceof GenericSchemaConnectionItem) {
            String id = ((ConnectionItem) connItem).getProperty().getId();
            if (table instanceof MetadataTable) {
                id = id + " - " + ((MetadataTable) table).getLabel(); //$NON-NLS-N$ 
            }
            List<RelationshipItemBuilder.Relation> schemaRelations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(id,
                    ItemCacheManager.LATEST_VERSION, RelationshipItemBuilder.SCHEMA_RELATION);
            if (!schemaRelations.isEmpty()) {
                relations.addAll(schemaRelations);
            }
        }

        RepositoryUpdateManager repositoryUpdateManager = new RepositoryUpdateManager(table, relations) {

            @Override
            public Set<EUpdateItemType> getTypes() {
                Set<EUpdateItemType> types = new HashSet<EUpdateItemType>();
                types.add(EUpdateItemType.NODE_SCHEMA);
                return types;
            }

        };

        // set renamed schema
        repositoryUpdateManager.setDeletedOrReselectTablesMap(deletedOrReselectTablesMap);

        final boolean doWork = repositoryUpdateManager.doWork(show, onlySimpleShow);
        repositoryUpdateManager.deletedOrReselectTablesMap.clear();
        return doWork;

    }

    /* hywang for bug 20024 */
    public static boolean isDeleteOrReselectMap(ConnectionItem connItem, List<IMetadataTable> newTables,
            List<IMetadataTable> oldTables, Map<String, EUpdateResult> deletedOrReselectTables) {
        for (IMetadataTable oldTable : oldTables) {
            String prefix;
            boolean isDeleted = true;
            String oldtableLabel = oldTable.getLabel();
            String oldtableId = oldTable.getId();
            for (IMetadataTable newTable : newTables) {
                String tableLabel = newTable.getLabel();
                String tableId = newTable.getId();
                if (tableLabel.equals(oldtableLabel)) {
                    isDeleted = false;
                    /* if table name is same but tableId is not same,means table has been deselect and reselect */
                    if (!tableId.equals(oldtableId)) {
                        prefix = connItem.getProperty().getId() + UpdatesConstants.SEGMENT_LINE;
                        deletedOrReselectTables.put(prefix + tableLabel, EUpdateResult.RELOAD);
                    }
                }
            }
            /* if can't find the name when looping the new tables,means the table has been removed */
            if (isDeleted) {
                prefix = connItem.getProperty().getId() + UpdatesConstants.SEGMENT_LINE;
                deletedOrReselectTables.put(prefix + oldtableLabel, EUpdateResult.DELETE);
            }
        }
        return !deletedOrReselectTables.isEmpty();
    }

    public static boolean updateWSDLConnection(ConnectionItem connectionItem, boolean show, final boolean onlySimpleShow) {
        List<IRepositoryViewObject> updateList = new ArrayList<IRepositoryViewObject>();
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        List<RelationshipItemBuilder.Relation> relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(
                connectionItem.getProperty().getId(), RelationshipItemBuilder.LATEST_VERSION,
                RelationshipItemBuilder.PROPERTY_RELATION);

        RepositoryUpdateManager repositoryUpdateManager = new RepositoryUpdateManager(connectionItem.getConnection(), relations) {

            @Override
            public Set<EUpdateItemType> getTypes() {
                Set<EUpdateItemType> types = new HashSet<EUpdateItemType>();
                types.add(EUpdateItemType.NODE_PROPERTY);
                types.add(EUpdateItemType.NODE_SCHEMA);
                return types;
            }

        };
        return repositoryUpdateManager.doWork(true, false);
    }

    public static boolean updateMultiSchema(SAPFunctionUnit functionUnit, ConnectionItem connItem,
            List<IMetadataTable> oldMetadataTable, Map<String, String> oldTableMap) {
        if (functionUnit == null) {
            return false;
        }
        Map<String, String> schemaRenamedMap = RepositoryUpdateManager.getSchemaRenamedMap(functionUnit, connItem, oldTableMap);
        boolean update = !schemaRenamedMap.isEmpty();

        if (!update) {
            if (oldMetadataTable != null) {
                List<IMetadataTable> newMetadataTable = RepositoryUpdateManager.getConversionMetadataTables(functionUnit);
                update = !RepositoryUpdateManager.sameAsMetadatTable(newMetadataTable, oldMetadataTable, oldTableMap);
            }
        }
        // update
        if (update) {
            return updateSchema(functionUnit, connItem, schemaRenamedMap, true, false);
        }
        return false;

    }

    /**
     * MOD qiongli 2011-11-28 change this method 'private' into 'public'.it is used to judge if need to update DQ
     * analyses.
     */
    public static boolean sameAsMetadatTable(List<IMetadataTable> newTables, List<IMetadataTable> oldTables,
            Map<String, String> oldTableMap) {
        if (newTables == null || oldTables == null) {
            return false;
        }

        Map<String, IMetadataTable> id2TableMap = new HashMap<String, IMetadataTable>();
        for (IMetadataTable oldTable : oldTables) {
            id2TableMap.put(oldTable.getId(), oldTable);
        }

        for (IMetadataTable newTable : newTables) {
            IMetadataTable oldTable = id2TableMap.get(newTable.getId());
            if (oldTableMap.containsKey(newTable.getId())) { // not a new created table.
                if (oldTable == null) {
                    return false;
                } else {
                    if (!newTable.sameMetadataAs(oldTable, IMetadataColumn.OPTIONS_NONE)) {
                        return false;
                    }
                }
            }
        }
        return true;

    }

    /**
     * 
     * xye Comment method "updateSAPFunction".
     * 
     * @param sapFunction
     * @param show
     * @return
     */
    public static boolean updateSAPFunction(final SAPFunctionUnit sapFunction, boolean show, boolean onlySimpleShow) {
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        List<IRepositoryViewObject> updateList = new ArrayList<IRepositoryViewObject>();
        List<RelationshipItemBuilder.Relation> relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(
                sapFunction.getId(), RelationshipItemBuilder.LATEST_VERSION, RelationshipItemBuilder.PROPERTY_RELATION);

        RepositoryUpdateManager repositoryUpdateManager = new RepositoryUpdateManager(sapFunction, relations) {

            @Override
            public Set<EUpdateItemType> getTypes() {
                Set<EUpdateItemType> types = new HashSet<EUpdateItemType>();
                types.add(EUpdateItemType.NODE_SAP_FUNCTION);
                types.add(EUpdateItemType.NODE_SCHEMA);
                return types;
            }

        };

        return repositoryUpdateManager.doWork(show, onlySimpleShow);
    }

    /**
     * DOC zli Comment method "updateSAPIDoc".
     * 
     * @param sapIDoc
     * @param show
     * @param onlySimpleShow
     * @return
     */
    public static boolean updateSAPIDoc(final SAPIDocUnit sapIDoc, boolean show, boolean onlySimpleShow) {
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        List<IRepositoryViewObject> updateList = new ArrayList<IRepositoryViewObject>();
        List<RelationshipItemBuilder.Relation> relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(
                sapIDoc.getId(), RelationshipItemBuilder.LATEST_VERSION, RelationshipItemBuilder.PROPERTY_RELATION);

        RepositoryUpdateManager repositoryUpdateManager = new RepositoryUpdateManager(sapIDoc, relations) {

            @Override
            public Set<EUpdateItemType> getTypes() {
                Set<EUpdateItemType> types = new HashSet<EUpdateItemType>();
                types.add(EUpdateItemType.NODE_SAP_IDOC);
                types.add(EUpdateItemType.NODE_SCHEMA);
                return types;
            }

        };

        return repositoryUpdateManager.doWork(show, onlySimpleShow);
    }

    /**
     * 
     * xye Comment method "updateSAPFunction".
     * 
     * @param sapFunction
     * @return
     */
    public static boolean updateSAPFunction(final SAPFunctionUnit sapFunction) {
        return updateSAPFunction(sapFunction, true, false);
    }

    /**
     * DOC zli Comment method "updateSAPIDoc".
     * 
     * @param sapIDoc
     * @return
     */
    public static boolean updateSAPIDoc(final SAPIDocUnit sapIDoc) {
        return updateSAPIDoc(sapIDoc, true, false);
    }

    /**
     * 
     * ggu Comment method "updateSchema".
     * 
     * if show is false, will work for context menu action.
     */
    public static boolean updateSchema(final MetadataTable metadataTable, boolean show) {

        return updateSchema(metadataTable, null, null, show, false);
    }

    public static boolean updateSchema(final MetadataTable metadataTable, RepositoryNode node, boolean show,
            boolean onlySimpleShow) {
        ConnectionItem connItem = (ConnectionItem) node.getObject().getProperty().getItem();
        return updateSchema(metadataTable, connItem, null, show, onlySimpleShow);
    }

    private static boolean updateSchema(final Object table, ConnectionItem connItem, Map<String, String> schemaRenamedMap,
            boolean show, boolean onlySimpleShow) {
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        List<IRepositoryViewObject> updateList = new ArrayList<IRepositoryViewObject>();
        List<RelationshipItemBuilder.Relation> relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(
                ((ConnectionItem) connItem).getProperty().getId(), RelationshipItemBuilder.LATEST_VERSION,
                RelationshipItemBuilder.PROPERTY_RELATION);

        RepositoryUpdateManager repositoryUpdateManager = new RepositoryUpdateManager(table, relations) {

            @Override
            public Set<EUpdateItemType> getTypes() {
                Set<EUpdateItemType> types = new HashSet<EUpdateItemType>();
                types.add(EUpdateItemType.NODE_SCHEMA);
                return types;
            }

        };

        // set renamed schema
        repositoryUpdateManager.setSchemaRenamedMap(schemaRenamedMap);

        return repositoryUpdateManager.doWork(show, onlySimpleShow);
    }

    /**
     * 
     * ggu Comment method "updateQuery".
     * 
     * for repository wizard.
     */
    public static boolean updateQuery(Query query) {

        return updateQueryObject(query, true, false);
    }

    public static boolean updateQuery(Query query, RepositoryNode node) {
        return updateQueryObject(query, true, false, node);
    }

    /**
     * 
     * ggu Comment method "updateQuery".
     * 
     * if show is false, will work for context menu action.
     */
    public static boolean updateQuery(Query query, boolean show) {
        return updateQueryObject(query, show, false);
    }

    public static boolean updateQuery(Query query, RepositoryNode node, boolean show, boolean onlySimpleShow) {
        return updateQueryObject(query, show, onlySimpleShow, node);
    }

    private static boolean updateQueryObject(Object parameter, boolean show, boolean onlySimpleShow) {
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        List<IRepositoryViewObject> updateList = new ArrayList<IRepositoryViewObject>();
        List<RelationshipItemBuilder.Relation> relations = null;
        if (parameter instanceof Query) {
            relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(((Query) parameter).getId(),
                    RelationshipItemBuilder.LATEST_VERSION, RelationshipItemBuilder.QUERY_RELATION);
        } else if (parameter instanceof QueriesConnection) {
            relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(
                    ((QueriesConnection) parameter).getConnection().getId(), RelationshipItemBuilder.LATEST_VERSION,
                    RelationshipItemBuilder.QUERY_RELATION);
        }

        RepositoryUpdateManager repositoryUpdateManager = new RepositoryUpdateManager(parameter, relations) {

            @Override
            public Set<EUpdateItemType> getTypes() {
                Set<EUpdateItemType> types = new HashSet<EUpdateItemType>();
                types.add(EUpdateItemType.NODE_QUERY);
                return types;
            }

        };
        return repositoryUpdateManager.doWork(show, onlySimpleShow);
    }

    private static boolean updateQueryObject(Object parameter, boolean show, boolean onlySimpleShow, RepositoryNode node) {
        Item item = node.getObject().getProperty().getItem();
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        List<IRepositoryViewObject> updateList = new ArrayList<IRepositoryViewObject>();
        List<RelationshipItemBuilder.Relation> relations = null;
        if (parameter instanceof Query) {
            String id = item.getProperty().getId() + " - " + ((Query) parameter).getLabel(); //$NON-NLS-1$
            relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(id, RelationshipItemBuilder.LATEST_VERSION,
                    RelationshipItemBuilder.QUERY_RELATION);
        }

        RepositoryUpdateManager repositoryUpdateManager = new RepositoryUpdateManager(parameter, relations, true) {

            @Override
            public Set<EUpdateItemType> getTypes() {
                Set<EUpdateItemType> types = new HashSet<EUpdateItemType>();
                types.add(EUpdateItemType.NODE_QUERY);
                return types;
            }

        };
        return repositoryUpdateManager.doWork(show, onlySimpleShow);
    }

    /**
     * 
     * ggu Comment method "updateContext".
     * 
     * if show is false, will work for context menu action.
     */
    public static boolean updateContext(ContextItem item, boolean show) {
        return updateContext(null, item, show, false);
    }

    public static boolean updateContext(ContextItem item, boolean show, boolean onlySimpleShow) {
        return updateContext(null, item, show, onlySimpleShow);
    }

    /**
     * 
     * ggu Comment method "updateContext".
     * 
     * for repository wizard.
     */
    public static boolean updateContext(JobContextManager repositoryContextManager, ContextItem item) {

        return updateContext(repositoryContextManager, item, true, false);
    }

    private static boolean updateContext(JobContextManager repositoryContextManager, ContextItem item, boolean show,
            boolean onlySimpleShow) {
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        List<IRepositoryViewObject> updateList = new ArrayList<IRepositoryViewObject>();
        List<RelationshipItemBuilder.Relation> relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(
                item.getProperty().getId(), RelationshipItemBuilder.LATEST_VERSION, RelationshipItemBuilder.CONTEXT_RELATION);

        RepositoryUpdateManager repositoryUpdateManager = new RepositoryUpdateManager(item, relations) {

            @Override
            public Set<EUpdateItemType> getTypes() {
                Set<EUpdateItemType> types = new HashSet<EUpdateItemType>();
                types.add(EUpdateItemType.CONTEXT);
                types.add(EUpdateItemType.CONTEXT_GROUP);
                return types;
            }

        };
        if (repositoryContextManager != null) {
            // add for bug 9119 context group
            Map<ContextItem, List<IContext>> repositoryContextGroupMap = new HashMap<ContextItem, List<IContext>>();
            List<IContext> addGroupContext = repositoryContextManager.getAddGroupContext();
            if (!addGroupContext.isEmpty()) {
                repositoryContextGroupMap.put(item, addGroupContext);
            }
            repositoryUpdateManager.setRepositoryAddGroupContext(repositoryContextGroupMap);

            Map<ContextItem, List<IContext>> removeRepositoryContextGroupMap = new HashMap<ContextItem, List<IContext>>();
            List<IContext> removeGroupContext = repositoryContextManager.getRemoveGroupContext();
            if (!removeGroupContext.isEmpty()) {
                removeRepositoryContextGroupMap.put(item, removeGroupContext);
            }
            repositoryUpdateManager.setRepositoryRemoveGroupContext(removeRepositoryContextGroupMap);

            Map<ContextItem, List<IContext>> renameRepositoryContextGroupMap = new HashMap<ContextItem, List<IContext>>();
            Map<IContext, String> renameContextGroup = new HashMap<IContext, String>();
            Map<IContext, String> renameGroupContext = repositoryContextManager.getRenameGroupContext();
            List<IContext> renameGroupList = new ArrayList<IContext>();
            for (IContext renameGroup : renameGroupContext.keySet()) {
                renameGroupList.add(renameGroup);
                renameContextGroup.put(renameGroup, renameGroupContext.get(renameGroup));
            }
            if (!renameGroupContext.isEmpty()) {
                renameRepositoryContextGroupMap.put(item, renameGroupList);
            }
            repositoryUpdateManager.setRepositoryRenameGroupContext(renameRepositoryContextGroupMap);
            repositoryUpdateManager.setRenameContextGroup(renameContextGroup);

            Map<ContextItem, Map<String, String>> repositoryRenamedMap = new HashMap<ContextItem, Map<String, String>>();
            if (!repositoryContextManager.getNameMap().isEmpty()) {
                repositoryRenamedMap.put(item, repositoryContextManager.getNameMap());
            }
            repositoryUpdateManager.setContextRenamedMap(repositoryRenamedMap);

            // newly added parameters
            Map<ContextItem, Set<String>> newParametersMap = new HashMap<ContextItem, Set<String>>();
            if (!repositoryContextManager.getNewParameters().isEmpty()) {
                newParametersMap.put(item, repositoryContextManager.getNewParameters());
            }
            repositoryUpdateManager.setNewParametersMap(newParametersMap);
        }
        try {
            repositoryUpdateManager.updateConnection(item);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return repositoryUpdateManager.doWork(show, onlySimpleShow);
    }

    public Map<ContextItem, Set<String>> getNewParametersMap() {
        return newParametersMap;
    }

    public void setNewParametersMap(Map<ContextItem, Set<String>> newParametersMap) {
        this.newParametersMap = newParametersMap;
    }

    public static boolean updateAllJob() {
        RepositoryUpdateManager repositoryUpdateManager = new RepositoryUpdateManager(null) {

            @Override
            public Set<EUpdateItemType> getTypes() {
                Set<EUpdateItemType> types = new HashSet<EUpdateItemType>();
                for (EUpdateItemType type : EUpdateItemType.values()) {
                    types.add(type);
                }
                return types;
            }

        };
        return repositoryUpdateManager.doWork();
    }

    public static boolean updateAllJob(boolean isDetectAndUpdate) {
        RepositoryUpdateManager repositoryUpdateManager = new RepositoryUpdateManager(null, isDetectAndUpdate) {

            @Override
            public Set<EUpdateItemType> getTypes() {
                Set<EUpdateItemType> types = new HashSet<EUpdateItemType>();
                for (EUpdateItemType type : EUpdateItemType.values()) {
                    types.add(type);
                }
                return types;
            }

        };
        return repositoryUpdateManager.doWork();
    }

    @SuppressWarnings("unchecked")
    public static List<IMetadataTable> getConversionMetadataTables(Connection conn) {
        if (conn == null) {
            return Collections.emptyList();
        }
        List<IMetadataTable> tables = new ArrayList<IMetadataTable>();

        Set tables2 = ConnectionHelper.getTables(conn);
        if (tables2 != null) {
            for (org.talend.core.model.metadata.builder.connection.MetadataTable originalTable : (Set<org.talend.core.model.metadata.builder.connection.MetadataTable>) tables2) {
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IMetadataManagmentService.class)) {
                    IMetadataManagmentService service = (IMetadataManagmentService) GlobalServiceRegister.getDefault()
                            .getService(IMetadataManagmentService.class);
                    IMetadataTable conversionTable = service.convertMetadataTable(originalTable);
                    tables.add(conversionTable);
                }
            }
        }

        return tables;
    }

    @SuppressWarnings("unchecked")
    public static List<IMetadataTable> getConversionMetadataTables(SAPFunctionUnit functionUnit) {
        if (functionUnit == null) {
            return Collections.emptyList();
        }
        List<IMetadataTable> tables = new ArrayList<IMetadataTable>();

        EList tables2 = functionUnit.getTables();
        if (tables2 != null) {
            for (org.talend.core.model.metadata.builder.connection.MetadataTable originalTable : (List<org.talend.core.model.metadata.builder.connection.MetadataTable>) tables2) {
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IMetadataManagmentService.class)) {
                    IMetadataManagmentService service = (IMetadataManagmentService) GlobalServiceRegister.getDefault()
                            .getService(IMetadataManagmentService.class);
                    IMetadataTable conversionTable = service.convertMetadataTable(originalTable);
                    tables.add(conversionTable);
                }
            }
        }

        return tables;
    }

    public static boolean updateJoblet(JobletProcessItem item, boolean show, boolean onlySimpleShow) {
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        List<IRepositoryViewObject> updateList = new ArrayList<IRepositoryViewObject>();
        List<RelationshipItemBuilder.Relation> relations = RelationshipItemBuilder.getInstance().getItemsRelatedTo(
                item.getProperty().getId(), RelationshipItemBuilder.LATEST_VERSION, RelationshipItemBuilder.JOBLET_RELATION);

        RepositoryUpdateManager repositoryUpdateManager = new RepositoryUpdateManager(item, relations) {

            @Override
            public Set<EUpdateItemType> getTypes() {
                Set<EUpdateItemType> types = new HashSet<EUpdateItemType>();
                types.add(EUpdateItemType.RELOAD);
                return types;
            }

        };
        repositoryUpdateManager.setOnlyOpeningJob(true);

        return repositoryUpdateManager.doWork(show, onlySimpleShow);
    }

    public boolean isItemIndexChecked() {
        // IDesignerCoreService designerCoreService = CoreRuntimePlugin.getInstance().getDesignerCoreService();
        // IPreferenceStore preferenceStore = designerCoreService.getDesignerCorePreferenceStore();
        // return preferenceStore.getBoolean(ITalendCorePrefConstants.ITEM_INDEX);
        return true;
    }

    public Map<String, EUpdateResult> getDeletedOrReselectTablesMap() {
        return deletedOrReselectTablesMap;
    }

    public void setDeletedOrReselectTablesMap(Map<String, EUpdateResult> deletedOrReselectTablesMap) {
        this.deletedOrReselectTablesMap = deletedOrReselectTablesMap;
    }

}
