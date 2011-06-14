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
package org.talend.repository.ui.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.context.JobContext;
import org.talend.core.model.context.JobContextParameter;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.builder.connection.AbstractMetadataObject;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.FTPConnection;
import org.talend.core.model.metadata.builder.connection.FileConnection;
import org.talend.core.model.metadata.builder.connection.GenericSchemaConnection;
import org.talend.core.model.metadata.builder.connection.LDAPSchemaConnection;
import org.talend.core.model.metadata.builder.connection.LdifFileConnection;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.QueriesConnection;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection;
import org.talend.core.model.metadata.builder.connection.WSDLSchemaConnection;
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
import org.talend.core.model.metadata.types.ContextParameterJavaTypeManager;
import org.talend.core.model.metadata.types.JavaType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ContextParameterTypeImpl;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ContextTypeImpl;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.UpdateRepositoryUtils;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IConnParamName;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.wizards.context.ContextWizard;
import org.talend.repository.ui.wizards.metadata.ContextSetsSelectionDialog;
import org.talend.repository.ui.wizards.metadata.ShowAddedContextdialog;
import orgomg.cwm.objectmodel.core.Package;

/**
 * ggu class global comment. Detailled comment
 */
public final class ConnectionContextHelper {

    public static final String LINE = "_"; //$NON-NLS-1$

    public static final String EMPTY = ""; //$NON-NLS-1$

    /**
     * 
     * ggu Comment method "checkContextMode".
     * 
     * initialize and check context mode for the ConnectionItem.
     */
    public static ContextItem checkContextMode(ConnectionItem connItem) {
        if (connItem == null) {
            return null;
        }
        Connection connection = connItem.getConnection();
        if (connection == null) {
            return null;
        }
        String contextId = connection.getContextId();
        if (contextId == null || EMPTY.equals(contextId.trim()) || RepositoryNode.NO_ID.equals(contextId.trim())) { //$NON-NLS-1$
            return null;
        }
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        try {
            IRepositoryViewObject context = factory.getLastVersion(contextId);
            if (context != null && factory.getStatus(context) != ERepositoryStatus.DELETED) {
                if (context.getProperty().getItem() instanceof ContextItem) {
                    connection.setContextMode(true);
                    return (ContextItem) context.getProperty().getItem();
                }
            }
        } catch (PersistenceException e) {
            //
        }
        connection.setContextMode(false);
        connection.setContextId(null);
        return null;
    }

    public static void openInConetxtModeDialog() {
        MessageDialog.openWarning(PlatformUI.getWorkbench().getDisplay().getActiveShell(), Messages
                .getString("ConnectionContextHelper.ContextTitle"), //$NON-NLS-1$
                Messages.getString("ConnectionContextHelper.InConextMessages")); //$NON-NLS-1$
    }

    public static void openOutConetxtModeDialog() {
        MessageDialog.openWarning(PlatformUI.getWorkbench().getDisplay().getActiveShell(), Messages
                .getString("ConnectionContextHelper.ContextTitle"), //$NON-NLS-1$
                Messages.getString("ConnectionContextHelper.OutConextMessages")); //$NON-NLS-1$
    }

    /**
     * 
     * ggu Comment method "exportAsContext".
     * 
     */
    public static ContextItem exportAsContext(ConnectionItem connItem, Set<IConnParamName> paramSet) {
        if (connItem == null) {
            return null;
        }
        List<IContextParameter> varList = createContextParameters(connItem, paramSet);
        if (varList == null || varList.isEmpty()) {
            return null;
        }

        String contextName = convertContextLabel(connItem.getProperty().getLabel());

        ISelection selection = getRepositoryContext(contextName, false);

        if (selection == null) {
            return null;
        }

        ContextWizard contextWizard = new ContextWizard(contextName, selection.isEmpty(), selection, varList);
        WizardDialog dlg = new WizardDialog(Display.getCurrent().getActiveShell(), contextWizard);
        if (dlg.open() == Window.OK) {
            ContextItem contextItem = contextWizard.getContextItem();
            if (contextItem != null) {
                contextItem.getProperty().setLabel(contextName);
            }
            return contextItem;
        }
        return null;
    }

    private static String convertContextLabel(String label) {
        if (label != null) {
            String newLabel = label.replaceAll("[\\.\\-\\ \\(\\)\\[\\]=]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
            Pattern pattern = Pattern.compile("^[0-9]+.*$"); //$NON-NLS-1$
            Matcher m = pattern.matcher(newLabel);
            if (m.matches()) {
                newLabel = "_" + newLabel; //$NON-NLS-1$
            }
            //
            try {
                ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                List<IRepositoryViewObject> contextObjectList = factory.getAll(ERepositoryObjectType.CONTEXT, true);

                int i = 1;
                String tmpLabel = newLabel;
                while (!isValidContextName(contextObjectList, tmpLabel)) {
                    tmpLabel = newLabel + i;
                    i++;
                }
                return tmpLabel;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
            return newLabel;
        }
        return null;
    }

    private static boolean isValidContextName(List<IRepositoryViewObject> contextObjectList, String name) {
        if (contextObjectList != null) {
            for (IRepositoryViewObject object : contextObjectList) {
                Item item = object.getProperty().getItem();
                if (item.getProperty().getLabel().equalsIgnoreCase(name)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static List<IContextParameter> createContextParameters(ConnectionItem connectionItem, Set<IConnParamName> paramSet) {
        if (connectionItem == null) {
            return null;
        }
        final String label = convertContextLabel(connectionItem.getProperty().getLabel());
        Connection conn = connectionItem.getConnection();

        List<IContextParameter> varList = null;
        if (conn instanceof DatabaseConnection) {
            varList = DBConnectionContextUtils.getDBVariables(label, (DatabaseConnection) conn, paramSet);
        } else if (conn instanceof FileConnection) {
            varList = FileConnectionContextUtils.getFileVariables(label, (FileConnection) conn, paramSet);
        } else if (conn instanceof LdifFileConnection) {
            varList = OtherConnectionContextUtils.getLdifFileVariables(label, (LdifFileConnection) conn);
        } else if (conn instanceof XmlFileConnection) {
            varList = OtherConnectionContextUtils.getXmlFileVariables(label, (XmlFileConnection) conn);
        } else if (conn instanceof LDAPSchemaConnection) {
            varList = OtherConnectionContextUtils.getLDAPSchemaVariables(label, (LDAPSchemaConnection) conn);
        } else if (conn instanceof WSDLSchemaConnection) {
            varList = OtherConnectionContextUtils.getWSDLSchemaVariables(label, (WSDLSchemaConnection) conn);
        } else if (conn instanceof SalesforceSchemaConnection) {
            varList = OtherConnectionContextUtils.getSalesforceVariables(label, (SalesforceSchemaConnection) conn);
        } else if (conn instanceof GenericSchemaConnection) {
            //
        } else if (conn instanceof MDMConnection) {
            varList = OtherConnectionContextUtils.getMDMSchemaVariables(label, (MDMConnection) conn);
        } else if (conn instanceof FTPConnection) {
            varList = OtherConnectionContextUtils.getFTPSChemaVariables(label, (FTPConnection) conn);
        }

        return varList;
    }

    public static void setPropertiesForContextMode(ConnectionItem connectionItem, ContextItem contextItem,
            Set<IConnParamName> paramSet) {
        if (connectionItem == null || contextItem == null) {
            return;
        }

        final String label = contextItem.getProperty().getLabel();
        Connection conn = connectionItem.getConnection();

        if (conn instanceof DatabaseConnection) {
            DBConnectionContextUtils.setPropertiesForContextMode(label, (DatabaseConnection) conn, paramSet);
        } else if (conn instanceof FileConnection) {
            FileConnectionContextUtils.setPropertiesForContextMode(label, (FileConnection) conn, paramSet);
        } else if (conn instanceof LdifFileConnection) {
            OtherConnectionContextUtils.setLdifFilePropertiesForContextMode(label, (LdifFileConnection) conn);
        } else if (conn instanceof XmlFileConnection) {
            OtherConnectionContextUtils.setXmlFilePropertiesForContextMode(label, (XmlFileConnection) conn);
        } else if (conn instanceof LDAPSchemaConnection) {
            OtherConnectionContextUtils.setLDAPSchemaPropertiesForContextMode(label, (LDAPSchemaConnection) conn);
        } else if (conn instanceof WSDLSchemaConnection) {
            OtherConnectionContextUtils.setWSDLSchemaPropertiesForContextMode(label, (WSDLSchemaConnection) conn);
        } else if (conn instanceof SalesforceSchemaConnection) {
            OtherConnectionContextUtils.setSalesforcePropertiesForContextMode(label, (SalesforceSchemaConnection) conn);
        } else if (conn instanceof GenericSchemaConnection) {
            //
        }
        // set connection for context mode
        connectionItem.getConnection().setContextMode(true);
        connectionItem.getConnection().setContextId(contextItem.getProperty().getId());

    }

    static void createParameters(List<IContextParameter> varList, String paramName, String value) {
        createParameters(varList, paramName, value, null);
    }

    static void createParameters(List<IContextParameter> varList, String paramName, String value, JavaType type) {
        if (varList == null || paramName == null) {
            return;
        }

        if (value == null) {
            value = EMPTY;
        }

        JobContextParameter contextParam = new JobContextParameter();
        contextParam.setName(paramName);

        switch (LanguageManager.getCurrentLanguage()) {
        case JAVA:
            if (type == null) {
                contextParam.setType(MetadataTalendType.getDefaultTalendType());
            } else {
                contextParam.setType(type.getId());
            }
            break;
        case PERL:
        default:
            if (type != null) {
                if (type == JavaTypesManager.DIRECTORY) {
                    contextParam.setType(ContextParameterJavaTypeManager.PERL_DIRECTORY);
                    break;
                } else if (type == JavaTypesManager.FILE) {
                    contextParam.setType(ContextParameterJavaTypeManager.PERL_FILE);
                    break;
                } else if (type == JavaTypesManager.INTEGER) {
                    contextParam.setType(MetadataTalendType.getPerlTypes()[3]); // int
                    break;
                } else if (type == JavaTypesManager.PASSWORD) {
                    contextParam.setType(ContextParameterJavaTypeManager.PERL_PASSWORD);
                    break;
                }
            }
            contextParam.setType(MetadataTalendType.getPerlTypes()[5]); // string
            break;
        }

        contextParam.setPrompt(paramName + "?"); //$NON-NLS-1$
        if (value != null) {
            contextParam.setValue(value);
        }
        contextParam.setComment(EMPTY);
        varList.add(contextParam);
    }

    private static ISelection getRepositoryContext(final String contextNameOrId, boolean isId) {
        if (contextNameOrId == null || "".equals(contextNameOrId.trim())) { //$NON-NLS-1$
            return null;
        }
        if (isId && RepositoryNode.NO_ID.equals(contextNameOrId.trim())) {
            return null;
        }
        IRepositoryViewObject contextObject = null;
        try {
            ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            List<IRepositoryViewObject> contextObjectList = factory.getAll(ERepositoryObjectType.CONTEXT, true);
            if (contextObjectList != null) {
                for (IRepositoryViewObject object : contextObjectList) {
                    Item item = object.getProperty().getItem();
                    if (item != null && item instanceof ContextItem) {
                        String itemNameOrId = null;
                        if (isId) {
                            itemNameOrId = item.getProperty().getId();
                        } else {
                            itemNameOrId = item.getProperty().getLabel();
                        }
                        if (contextNameOrId.equals(itemNameOrId)) {
                            contextObject = object;
                            break;
                        }
                    }
                }
            }
        } catch (PersistenceException e) {
            //
        }

        StructuredSelection selection = new StructuredSelection();
        if (contextObject != null) {
            RepositoryNode repositoryNode = RepositoryNodeUtilities.getRepositoryNode(contextObject);
            if (repositoryNode != null) {
                selection = new StructuredSelection(repositoryNode);
            }
        }
        return selection;
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    static String getContextValue(ContextType contextType, final String value, final String paramName) {
        if (value == null) {
            return EMPTY;
        }

        if (contextType != null && ContextParameterUtils.isContainContextParam(value)) {
            ContextParameterType param = null;
            for (ContextParameterType paramType : (List<ContextParameterType>) contextType.getContextParameter()) {
                if (paramType.getName().equals(paramName)) {
                    param = paramType;
                    break;
                }
            }
            if (param != null && param.getValue() != null) {
                return param.getValue();
            }
            return EMPTY;
        }
        return value;

    }

    /**
     * 
     * ggu Comment method "upateContext".
     * 
     * open the context wizard to update context parameters.
     */
    public static boolean upateContext(ConnectionItem connItem) {
        if (connItem == null) {
            return false;
        }

        Shell activeShell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        boolean checked = MessageDialog.openConfirm(activeShell, Messages.getString("ConnectionContextHelper.UpdateTitle"), //$NON-NLS-1$
                Messages.getString("ConnectionContextHelper.UpdateMessages")); //$NON-NLS-1$
        if (!checked) {
            return false;
        }
        ISelection selection = getRepositoryContext(connItem.getConnection().getContextId(), true);
        if (selection == null || selection.isEmpty()) {
            return false;
        }
        Object firstElement = ((IStructuredSelection) selection).getFirstElement();
        if (firstElement instanceof RepositoryNode) {
            ContextWizard contextWizard = new ContextWizard(PlatformUI.getWorkbench(), false, (RepositoryNode) firstElement,
                    false);
            WizardDialog dlg = new WizardDialog(activeShell, contextWizard);
            if (dlg.open() == Window.OK) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * ggu Comment method "processContextForJob".
     * 
     * @ignoreContextMode if true, only work for jobtemplate plugin(so far).
     */
    public static void addContextForNodeParameter(final INode node, final ConnectionItem connItem, final boolean ignoreContextMode) {
        if (node == null || connItem == null) {
            return;
        }
        IProcess process = node.getProcess();
        if (process instanceof IProcess2) {
            addContextForElementParameters((IProcess2) process, connItem, node.getElementParameters(), null, ignoreContextMode);
        }
    }

    /**
     * 
     * ggu Comment method "addContextForProcessParameter".
     * 
     * @param process
     * @param connItem
     * @param section for EXTRA and STATSANDLOGS
     * @ignoreContextMode if true, only work for jobtemplate plugin(so far).
     */
    public static void addContextForProcessParameter(final IProcess2 process, final ConnectionItem connItem,
            final EComponentCategory category, final boolean ignoreContextMode) {
        if (process == null || connItem == null) {
            return;
        }
        addContextForElementParameters(process, connItem, process.getElementParameters(), category, ignoreContextMode);
    }

    /**
     * 
     * ggu Comment method "addContextForElementParameters".
     * 
     * @param process
     * @param connItem
     * @param elementParameters
     * @param category
     * @param checked
     */
    private static void addContextForElementParameters(final IProcess2 process, final ConnectionItem connItem,
            List<? extends IElementParameter> elementParameters, final EComponentCategory category,
            final boolean ignoreContextMode) {
        if (connItem == null || elementParameters == null || process == null) {
            return;
        }

        Connection connection = connItem.getConnection();
        if (connection != null && connection.isContextMode()) {
            // get the context variables from the node parameters.
            Set<String> neededVars = retrieveContextVar(elementParameters, connection, category);
            if (neededVars != null && !neededVars.isEmpty()) {
                ContextItem contextItem = ContextUtils.getContextItemById2(connection.getContextId());
                if (contextItem != null) {
                    // find added variables
                    Set<String> addedVars = checkAndAddContextVariables(contextItem, neededVars, process.getContextManager(),
                            false);
                    if (addedVars != null && !addedVars.isEmpty()) {
                        boolean added = false;
                        if (ignoreContextMode) {
                            addContextVarForJob(process, contextItem, addedVars);
                            added = true;
                        } else {
                            // show
                            Map<String, Set<String>> addedVarsMap = new HashMap<String, Set<String>>();
                            addedVarsMap.put(connItem.getProperty().getId(), addedVars);
                            ShowAddedContextdialog showDialog = new ShowAddedContextdialog(addedVarsMap, true);
                            if (showDialog.open() == Window.OK) {
                                addContextVarForJob(process, contextItem, addedVars);
                                added = true;
                            }
                        }
                        // refresh context view
                        if (added) {
                            RepositoryPlugin.getDefault().getDesignerCoreService().switchToCurContextsView();
                        }
                    }
                }
            }

        }
    }

    /**
     * 
     * ggu Comment method "checkNodesPropertiesForAddedContextMode".
     * 
     * @param process
     */
    public static void checkNodesPropertiesForAddedContextMode(final IProcess2 process) {
        if (process == null) {
            return;
        }

        Map<String, Set<String>> varsMap = new HashMap<String, Set<String>>();
        // main
        checkProcessMainProperties(varsMap, process, EComponentCategory.EXTRA);
        checkProcessMainProperties(varsMap, process, EComponentCategory.STATSANDLOGS);
        // nodes
        for (INode node : process.getGraphicalNodes()) {
            checkProcessNodesProperties(varsMap, node);
        }
        //
        if (!varsMap.isEmpty()) {
            Map<String, Set<String>> addedVarsMap = new HashMap<String, Set<String>>();
            for (String id : varsMap.keySet()) {
                ConnectionItem connItem = UpdateRepositoryUtils.getConnectionItemByItemId(id);
                if (connItem != null) {
                    ContextItem contextItem = ContextUtils.getContextItemById2(connItem.getConnection().getContextId());
                    if (contextItem != null) {
                        // add needed vars into job
                        Set<String> addedVars = checkAndAddContextVariables(contextItem, varsMap.get(id), process
                                .getContextManager(), false);
                        if (addedVars != null && !addedVars.isEmpty()) {
                            String source = UpdateRepositoryUtils.getRepositorySourceName(connItem);
                            addedVarsMap.put(source, addedVars);
                        }
                    }
                }
            }
            if (!addedVarsMap.isEmpty()) {
                // show
                ShowAddedContextdialog showDialog = new ShowAddedContextdialog(addedVarsMap);
                if (showDialog.open() == Window.OK) {
                    boolean added = false;
                    for (String id : varsMap.keySet()) {
                        ConnectionItem connItem = UpdateRepositoryUtils.getConnectionItemByItemId(id);
                        if (connItem != null) {
                            String source = UpdateRepositoryUtils.getRepositorySourceName(connItem);
                            if (addedVarsMap.containsKey(source)) {
                                ContextItem contextItem = ContextUtils.getContextItemById2(connItem.getConnection()
                                        .getContextId());
                                if (contextItem != null) {
                                    addContextVarForJob(process, contextItem, addedVarsMap.get(source));
                                    added = true;
                                }
                            }
                        }
                    }

                    // refresh context view
                    if (added) {
                        RepositoryPlugin.getDefault().getDesignerCoreService().switchToCurContextsView();
                    }
                }
            }
        }
    }

    private static void checkProcessMainProperties(Map<String, Set<String>> varsMap, final IProcess2 process,
            final EComponentCategory category) {
        if (process != null && (category == EComponentCategory.EXTRA || EComponentCategory.STATSANDLOGS == category)) {
            calcContextVariablesFromParameters(varsMap, process.getElementParameters(), category);
        }
    }

    private static void checkProcessNodesProperties(Map<String, Set<String>> varsMap, final INode node) {
        if (node == null) {
            return;
        }
        calcContextVariablesFromParameters(varsMap, node.getElementParameters(), null);
    }

    private static void calcContextVariablesFromParameters(Map<String, Set<String>> varsMap,
            List<? extends IElementParameter> parameters, final EComponentCategory category) {
        if (parameters != null) {
            IElementParameter propertyParam = null;
            for (IElementParameter param : parameters) {
                if ((category == null || category == param.getCategory())
                        && param.getField() == EParameterFieldType.PROPERTY_TYPE && param.isShow(parameters)) {
                    propertyParam = param;
                    break;
                }
            }
            if (propertyParam != null) {
                IElementParameter param = propertyParam.getChildParameters().get("REPOSITORY_PROPERTY_TYPE"); //$NON-NLS-1$
                if (param != null) {
                    String id = (String) param.getValue();
                    ConnectionItem connItem = UpdateRepositoryUtils.getConnectionItemByItemId(id);
                    if (connItem != null) {
                        checkContextMode(connItem);
                        Connection connection = connItem.getConnection();
                        if (connection.isContextMode()) {
                            Set<String> neededVars = retrieveContextVar(parameters, connection, category);
                            if (neededVars != null && !neededVars.isEmpty()) {
                                Set<String> varsSet = varsMap.get(id);
                                if (varsSet == null) {
                                    varsMap.put(id, neededVars);
                                } else {
                                    varsSet.addAll(neededVars);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static Set<String> retrieveContextVar(List<? extends IElementParameter> elementParameters, Connection connection,
            EComponentCategory category) {
        if (elementParameters == null || connection == null) {
            return null;
        }
        Set<String> addedVars = new HashSet<String>();
        String var = null;
        for (IElementParameter param : elementParameters) {
            if (category == null || category == param.getCategory()) {
                String repositoryValue = param.getRepositoryValue();
                if (repositoryValue != null) {
                    Object objectValue = RepositoryToComponentProperty.getValue(connection, repositoryValue, null);

                    if (objectValue != null) {
                        if (objectValue instanceof List) {
                            List list = (List) objectValue;
                            for (int i = 0; i < list.size(); i++) {
                                Object object = list.get(i);
                                if (object instanceof HashMap) {
                                    Map map = (HashMap) object;
                                    if (!map.isEmpty()) {
                                        Set keySet = map.keySet();
                                        Iterator iterator = keySet.iterator();
                                        while (iterator.hasNext()) {
                                            String key = (String) iterator.next();
                                            Object object2 = map.get(key);
                                            if (object2 instanceof String) {
                                                var = ContextParameterUtils.getVariableFromCode((String) object2);
                                                if (var != null) {
                                                    addedVars.add(var);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (objectValue instanceof String) {
                            var = ContextParameterUtils.getVariableFromCode((String) objectValue);
                            if (var != null) {
                                addedVars.add(var);
                            }
                        }
                    }
                }
            }
        }

        return addedVars;
    }

    /**
     * DOC zli Comment method "addContextVarForJob".
     * 
     * @param process
     * @param contextItem
     * @param contextManager added for bug 15608
     */
    public static void addContextVarForJob(IProcess2 process, final ContextItem contextItem, final IContextManager contextManager) {
        if (process == null || contextItem == null || contextManager == null) {
            return;
        }
        CommandStack commandStack = process.getCommandStack();

        Command cmd = new Command() {

            @Override
            public void execute() {
                checkAndAddContextsVarDND(contextItem, contextManager);
            }
        };
        if (commandStack == null) {
            cmd.execute();
        } else {
            commandStack.execute(cmd);
        }

    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public static void addContextVarForJob(IProcess2 process, final ContextItem contextItem, final Set<String> addedVars) {
        if (process == null || contextItem == null || addedVars == null || addedVars.isEmpty()) {
            return;
        }
        final IContextManager contextManager = process.getContextManager();
        if (contextManager != null) {
            CommandStack commandStack = process.getCommandStack();

            Command cmd = new Command() {

                @Override
                public void execute() {
                    checkAndAddContextVariables(contextItem, addedVars, contextManager, true);
                }
            };

            if (commandStack == null) {
                cmd.execute();
            } else {
                commandStack.execute(cmd);
            }
        }
    }

    /**
     * ggu Comment method "checkAndAddContextVariables".
     */
    public static Set<String> checkAndAddContextVariables(final ContextItem contextItem, final Set<String> neededVars,
            final IContextManager contextManager, boolean added) {
        Set<String> addedVars = new HashSet<String>();
        for (IContext context : contextManager.getListContext()) {

            ContextType type = ContextUtils.getContextTypeByName(contextItem.getContext(), context.getName(), contextItem
                    .getDefaultContext());
            if (type != null) {
                for (String var : neededVars) {
                    if (context.getContextParameter(var) != null) {
                        continue;
                    }
                    ContextParameterType param = ContextUtils.getContextParameterTypeByName(type, var);
                    if (param != null) {
                        //
                        if (added) {
                            JobContextParameter contextParam = new JobContextParameter();

                            ContextUtils.updateParameter(param, contextParam);

                            contextParam.setSource(contextItem.getProperty().getId());
                            contextParam.setContext(context);

                            context.getContextParameterList().add(contextParam);
                        }
                        addedVars.add(var);
                    }
                }
            }
        }
        return addedVars;
    }

    /*
     * maybe will open dialog to confirm the context set.
     */
    public static ContextType getContextTypeForContextMode(Connection connection) {
        return getContextTypeForContextMode(connection, false);
    }

    /**
     * 
     * wzhang Comment method "containsVariable".
     * 
     * @param contextManager
     * @return
     */
    public static boolean containsVariable(IContextManager contextManager) {
        List<IContext> listContext = contextManager.getListContext();
        for (IContext context : listContext) {
            List<IContextParameter> paraList = context.getContextParameterList();
            if (!paraList.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * wzhang Comment method "checkAndAddContextsVarDND".
     * 
     * @param item
     * @param contextManager
     */
    public static void checkAndAddContextsVarDND(ContextItem item, IContextManager contextManager) {
        EList context = item.getContext();
        Map<String, List<ContextParameterTypeImpl>> map = new HashMap<String, List<ContextParameterTypeImpl>>();
        Iterator iterator = context.iterator();
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof ContextTypeImpl) {
                ContextTypeImpl contextTypeImpl = (ContextTypeImpl) obj;
                String name = contextTypeImpl.getName();
                EList contextParameters = contextTypeImpl.getContextParameter();
                Iterator contextParas = contextParameters.iterator();
                List<ContextParameterTypeImpl> list = new ArrayList<ContextParameterTypeImpl>();
                while (contextParas.hasNext()) {
                    ContextParameterTypeImpl contextParameterType = (ContextParameterTypeImpl) contextParas.next();
                    list.add(contextParameterType);
                }
                map.put(name, list);
            }
        }
        if (map.isEmpty()) {
            return;
        }
        contextManager.getListContext().clear();
        String defaultContextName = item.getDefaultContext();

        for (String key : map.keySet()) {
            List<ContextParameterTypeImpl> list = map.get(key);
            JobContext jobContext = new JobContext(key);
            for (ContextParameterTypeImpl contextImpl : list) {
                JobContextParameter contextParam = new JobContextParameter();
                ContextUtils.updateParameter(contextImpl, contextParam);
                contextParam.setSource(item.getProperty().getId());
                contextParam.setContext(jobContext);
                jobContext.getContextParameterList().add(contextParam);
            }
            contextManager.getListContext().add(jobContext);
            if (key.equals(defaultContextName)) {
                contextManager.setDefaultContext(jobContext);
            }
        }

    }

    /*
     * if defaultContext is false, maybe will open dialog to confirm the context set. same as
     * getContextTypeForContextMode(connection).
     * 
     * if defaultContext is true, will use the default context.
     */
    public static ContextType getContextTypeForContextMode(Connection connection, boolean defaultContext) {
        return getContextTypeForContextMode(null, connection, null, defaultContext);
    }

    public static ContextType getContextTypeForContextMode(Shell shell, Connection connection) {
        return getContextTypeForContextMode(shell, connection, null, false);
    }

    public static ContextType getContextTypeForContextMode(Shell shell, Connection connection, String selectedContext,
            boolean defaultContext) {
        return getContextTypeForContextMode(shell, connection, selectedContext, defaultContext, false);
    }

    public static ContextType getContextTypeForContextMode(Shell shell, Connection connection, boolean canCancel) {
        return getContextTypeForContextMode(shell, connection, null, false, canCancel);
    }

    /**
     * 
     * ggu Comment method "getContextTypeForContextMode".
     * 
     * if connection is in context mode,choose the context. if return null, the connection is not in context mode.
     * 
     * if canCancel is true, the selecting cotnext sets dialog will can be cancel.
     */
    private static ContextType getContextTypeForContextMode(Shell shell, Connection connection, String selectedContext,
            boolean defaultContext, boolean canCancel) {
        if (connection == null) {
            return null;
        }
        ContextItem contextItem = ContextUtils.getContextItemById2(connection.getContextId());
        if (contextItem != null && connection.isContextMode()) {
            if (defaultContext) {
                selectedContext = contextItem.getDefaultContext();
            } else if (selectedContext == null) {
                if (contextItem.getContext().size() > 1) {
                    final ContextSetsSelectionDialog setsDialog = new ContextSetsSelectionDialog(shell, contextItem, canCancel);
                    Display.getDefault().syncExec(new Runnable() {// launch the dialog box in the UI thread beacause the

                                // method may be called from other threads.

                                public void run() {
                                    setsDialog.open();
                                }
                            });
                    selectedContext = setsDialog.getSelectedContext();
                } else {
                    selectedContext = contextItem.getDefaultContext();
                }
            }
            // if can cancel, can't return the default contex by auto.
            return ContextUtils.getContextTypeByName(contextItem, selectedContext, !canCancel);
        }
        return null;
    }

    /**
     * DOC gcui Comment method "getContextTypeForJob".
     * 
     * @param shell
     * @param contextManager
     * @param canCancel
     * @return
     */
    public static String getContextTypeForJob(Shell shell, IContextManager contextManager, boolean canCancel) {
        ContextSetsSelectionDialog setsDialog = new ContextSetsSelectionDialog(shell, contextManager, canCancel);
        setsDialog.open();
        String currentDefaultName = setsDialog.getSelectedContext();
        return currentDefaultName;
    }

    /**
     * 
     * ggu Comment method "getOriginalValue".
     * 
     * if value is context mode, return original value.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public static String getOriginalValue(ContextType contextType, final String value) {
        if (value == null) {
            return EMPTY;
        }
        if (contextType != null && ContextParameterUtils.isContainContextParam(value)) {
            String var = ContextParameterUtils.getVariableFromCode(value);
            if (var != null) {
                ContextParameterType param = null;
                for (ContextParameterType paramType : (List<ContextParameterType>) contextType.getContextParameter()) {
                    if (paramType.getName().equals(var)) {
                        param = paramType;
                        break;
                    }
                }
                if (param != null) {
                    String value2 = param.getValue();
                    if (value2 != null) {
                        // return TalendTextUtils.removeQuotes(value2); //some value can't be removed for quote
                        return value2;
                    }
                }
                return EMPTY;
            }
        }
        return value;
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public static void cloneConnectionProperties(Connection sourceConn, Connection targetConn) {
        if (sourceConn == null || targetConn == null) {
            return;
        }
        cloneConnectionProperties((AbstractMetadataObject) sourceConn, (AbstractMetadataObject) targetConn);

        // not clone
        // targetConn.setContextId(sourceConn.getContextId());
        // targetConn.setContextMode(sourceConn.isContextMode());
        targetConn.setVersion(sourceConn.getVersion());
        QueriesConnection queryConnection = sourceConn.getQueries();
        if (queryConnection != null) {
            QueriesConnection cloneQueriesConnection = ConnectionFactory.eINSTANCE.createQueriesConnection();

            cloneQueriesConnection.getQuery().clear();
            List<Query> queries = (List<Query>) queryConnection.getQuery();
            for (Query query : queries) {
                Query cloneQuery = ConnectionFactory.eINSTANCE.createQuery();
                cloneConnectionProperties(query, cloneQuery);
                cloneQuery.setValue(query.getValue());

                cloneQuery.setQueries(cloneQueriesConnection);
                cloneQueriesConnection.getQuery().add(cloneQuery);
            }

            cloneQueriesConnection.setConnection(targetConn);
            targetConn.setQueries(cloneQueriesConnection);

        }
        // no need to clean targetconn's datapackage because when clone the connection,the datapacage won't be cloned.

        // Package pkg = (Package) ConnectionHelper.getPackage(targetConn.getName(), targetConn, Package.class);

        Set<MetadataTable> tables = (Set<MetadataTable>) ConnectionHelper.getTables(sourceConn);
        for (MetadataTable table : tables) {
            MetadataTable cloneTable = ConnectionFactory.eINSTANCE.createMetadataTable();

            cloneConnectionProperties(table, cloneTable);

            cloneTable.setActivatedCDC(table.isActivatedCDC());
            cloneTable.setAttachedCDC(table.isAttachedCDC());
            cloneTable.setTableType(table.getTableType());
            cloneTable.setSourceName(table.getSourceName());

            cloneTable.getColumns().clear();

            List<MetadataColumn> columns = (List<MetadataColumn>) table.getColumns();
            for (MetadataColumn column : columns) {
                MetadataColumn cloneColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();

                cloneConnectionProperties(column, cloneColumn);

                cloneColumn.setDefaultValue(column.getDefaultValue());
                cloneColumn.setDisplayField(column.getDisplayField());
                cloneColumn.setKey(column.isKey());
                cloneColumn.setLength(column.getLength());
                cloneColumn.setNullable(column.isNullable());
                cloneColumn.setOriginalField(column.getOriginalField());
                cloneColumn.setPattern(column.getPattern());
                cloneColumn.setPrecision(column.getPrecision());
                cloneColumn.setSourceType(column.getSourceType());
                cloneColumn.setTalendType(column.getTalendType());

                cloneColumn.setTable(cloneTable);
                cloneTable.getColumns().add(cloneColumn);
            }
            if (cloneTable.getNamespace() instanceof Package) {
                Package pkg = (Package) cloneTable.getNamespace();
                pkg.getDataManager().add(targetConn);
            }
            // if (targetConn instanceof DatabaseConnection) { // hywang,need to see if schema or catalog
            // // Catalog c = (Catalog) ConnectionHelper.getPackage(((DatabaseConnection) targetConn).getSID(),
            // // ((DatabaseConnection) targetConn), Catalog.class);
            // // Schema s = (Schema) ConnectionHelper.getPackage(((DatabaseConnection) targetConn).getUiSchema(),
            // // ((DatabaseConnection) targetConn), Schema.class);
            // // if (c != null) {
            // // PackageHelper.addMetadataTable(cloneTable, c);
            // // } else if (s != null) {
            // // PackageHelper.addMetadataTable(cloneTable, s);
            // // }
            // Catalog c = (Catalog) ConnectionHelper.getPackage(((DatabaseConnection) targetConn).getSID(), targetConn,
            // Catalog.class);
            // if (c != null) {
            // PackageHelper.addMetadataTable(cloneTable, c);
            // }
            // } else {
            // Package pkg = (Package) ConnectionHelper.getPackage(targetConn.getName(), targetConn, Package.class);
            // if (pkg != null) {
            // PackageHelper.addMetadataTable(cloneTable, pkg);
            // }
            // }
            // cloneTable.setConnection(targetConn);
            // targetConn.getTables().add(cloneTable);
        }

    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    private static void cloneConnectionProperties(AbstractMetadataObject sourceObj, AbstractMetadataObject targetObj) {
        if (sourceObj == null || targetObj == null) {
            return;
        }
        targetObj.setComment(sourceObj.getComment());
        targetObj.setDivergency(sourceObj.isDivergency());
        targetObj.setId(sourceObj.getId());
        targetObj.setLabel(sourceObj.getLabel());
        // targetObj.setReadOnly(sourceObj.isReadOnly()); //can't set
        targetObj.setSynchronised(sourceObj.isSynchronised());
        HashMap properties = sourceObj.getProperties();
        if (properties != null) {
            targetObj.setProperties((HashMap) properties.clone());
        }
    }

    /*
     * 
     */
    public static int convertValue(String value) {
        if (value == null || value.equals(EMPTY)) {
            return -1;
        }
        int i = -1;
        try {
            i = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            //
        }
        return i;
    }

    public static void revertPropertiesForContextMode(ConnectionItem connItem, ContextType contextType) {
        if (connItem == null || contextType == null) {
            return;
        }
        Connection conn = connItem.getConnection();
        if (conn instanceof DatabaseConnection) {
            DBConnectionContextUtils.revertPropertiesForContextMode((DatabaseConnection) conn, contextType);
        } else if (conn instanceof FileConnection) {
            FileConnectionContextUtils.revertPropertiesForContextMode((FileConnection) conn, contextType);
        } else if (conn instanceof LdifFileConnection) {
            OtherConnectionContextUtils.revertLdifFilePropertiesForContextMode((LdifFileConnection) conn, contextType);
        } else if (conn instanceof XmlFileConnection) {
            OtherConnectionContextUtils.revertXmlFilePropertiesForContextMode((XmlFileConnection) conn, contextType);
        } else if (conn instanceof LDAPSchemaConnection) {
            OtherConnectionContextUtils.revertLDAPSchemaPropertiesForContextMode((LDAPSchemaConnection) conn, contextType);
        } else if (conn instanceof WSDLSchemaConnection) {
            OtherConnectionContextUtils.revertWSDLSchemaPropertiesForContextMode((WSDLSchemaConnection) conn, contextType);
        } else if (conn instanceof SalesforceSchemaConnection) {
            OtherConnectionContextUtils.revertSalesforcePropertiesForContextMode((SalesforceSchemaConnection) conn, contextType);
        } else if (conn instanceof GenericSchemaConnection) {
            //
        }
        // set connection for context mode
        conn.setContextMode(false);
        conn.setContextId(EMPTY);
    }
}
