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
package org.talend.designer.core.ui.editor.process;

import org.eclipse.gef.commands.CompoundCommand;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.utils.CsvArray;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.designer.core.ui.editor.cmd.QueryGuessCommand;
import org.talend.designer.core.ui.editor.cmd.RepositoryChangeMetadataCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.DbInfo;
import org.talend.designer.core.ui.editor.properties.controllers.GuessSchemaProcess;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.repository.UpdateRepositoryUtils;
import org.talend.repository.model.ComponentsFactoryProvider;

/**
 * DOC hyWang class global comment. Detailled comment
 */
public class ConvertRepositoryNodeToProcessNode {

    private INode node;

    private String memoSQL;

    private DbInfo info;

    private IContext selectedContext;

    DatabaseConnection databaseConnection;

    public ConvertRepositoryNodeToProcessNode(ConnectionItem connectionItem, String tableName) {
        databaseConnection = (DatabaseConnection) connectionItem.getConnection();
        IMetadataConnection iMetadataConnection = null;
        iMetadataConnection = ConvertionHelper.convert(databaseConnection);
        String dbType = iMetadataConnection.getDbType();
        String username = iMetadataConnection.getUsername();
        String pwd = iMetadataConnection.getPassword();
        String dbVersion = iMetadataConnection.getDbVersionString();
        String url = iMetadataConnection.getUrl();
        String additionalParams = iMetadataConnection.getAdditionalParams();

        if (EDatabaseTypeName.GENERAL_JDBC.getDisplayName().equals(dbType)) { // hywang for 9594
            info = new DbInfo(dbType, username, pwd, dbVersion, url, iMetadataConnection.getDriverClass(),
                    iMetadataConnection.getDriverJarPath(), additionalParams);
        } else {
            info = new DbInfo(dbType, username, pwd, dbVersion, url, additionalParams);
        }
        try {
            convertToProcessNode(connectionItem, tableName);
        } catch (ProcessorException e) {
            ExceptionHandler.process(e);
        }
    }

    public void convertToProcessNode(ConnectionItem connectionItem, String tableName) throws ProcessorException {

        EDatabaseComponentName name = EDatabaseComponentName.getCorrespondingComponentName(connectionItem,
                ERepositoryObjectType.METADATA_CONNECTIONS);
        String componentName = null;
        componentName = name.getDefaultComponentName();
        IComponent dbInputComponent = ComponentsFactoryProvider.getInstance().get(componentName);
        Process process = new Process(GuessSchemaProcess.getNewmockProperty());
        node = new Node(dbInputComponent, process);
        selectedContext = node.getProcess().getContextManager().getDefaultContext();

        // TDI-20011
        IMetadataTable table = UpdateRepositoryUtils.getTableByName(connectionItem, tableName);
        if (table == null) {
            table = new MetadataTable();
            table.setTableName(tableName);
            table.setLabel(tableName);
        }
        IElementParameter propertyParam = ((Node) node).getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
        IElementParameter schemaParam = ((Node) node).getElementParameterFromField(EParameterFieldType.SCHEMA_TYPE);
        String propertyId = connectionItem.getProperty().getId();
        String schema = databaseConnection.getUiSchema();
        String dbType = databaseConnection.getDatabaseType();
        String value = connectionItem.getProperty().getId() + " - " + table.getLabel(); //$NON-NLS-1$

        CompoundCommand cc = new CompoundCommand();
        // inital parameters command
        ChangeValuesFromRepository changeValueCommand = new ChangeValuesFromRepository((Node) node, databaseConnection,
                propertyParam.getName() + ":" + EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), propertyId); //$NON-NLS-1$
        cc.add(changeValueCommand);

        // change metadata command
        RepositoryChangeMetadataCommand changeMetadataCommand = new RepositoryChangeMetadataCommand((Node) node,
                schemaParam.getName() + ":" //$NON-NLS-1$
                        + EParameterName.REPOSITORY_SCHEMA_TYPE.getName(), value, table, null, null);
        cc.add(changeMetadataCommand);

        // guess query command
        QueryGuessCommand queryGuessCommand = new QueryGuessCommand((Node) node, node.getMetadataList().get(0), schema, dbType,
                databaseConnection);
        cc.add(queryGuessCommand);

        // execute the commands
        cc.execute();

        IElementParameter query = node.getElementParameter("QUERY"); //$NON-NLS-N$ //$NON-NLS-1$
        //
        memoSQL = query.getValue().toString();
        String memoSQLTemp = TalendTextUtils.removeQuotesIfExist(memoSQL);
        if ((memoSQLTemp == null || memoSQLTemp.equals("")) && tableName != null && !tableName.equals("")) {
            memoSQL = "select * from " + tableName;
            memoSQL = TalendTextUtils.addSQLQuotes(memoSQL);
        }
    }

    public CsvArray runMockProcess() throws ProcessorException {
        GuessSchemaProcess gsp = new GuessSchemaProcess(GuessSchemaProcess.getNewmockProperty(), node, selectedContext, memoSQL,
                info);
        return gsp.run();
    }
}
