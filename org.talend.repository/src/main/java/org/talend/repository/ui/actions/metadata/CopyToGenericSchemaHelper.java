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
package org.talend.repository.ui.actions.metadata;

import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.GenericPackage;
import org.talend.core.model.metadata.builder.connection.GenericSchemaConnection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.impl.MetadataColumnImpl;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.GenericSchemaConnectionItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.PackageHelper;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.MetadataTableRepositoryObject;
import orgomg.cwm.objectmodel.core.Package;

/**
 * Administrator class global comment. Detailed comment <br/>
 * 
 */
public class CopyToGenericSchemaHelper {

    private static final String DEFAULT_TABLE_NAME = "metadata"; //$NON-NLS-1$

    private static IProxyRepositoryFactory repositoryFactory;

    /**
     * Administrator Comment method "moveToGenericSchema".
     * 
     * @param factory
     * @param targetPath
     * @param ve
     * @param isConnectionTableSchema
     * @throws PersistenceException
     */
    public static void copyToGenericSchema(IProxyRepositoryFactory factory, IRepositoryViewObject tableToMove, IPath targetPath)
            throws PersistenceException {

        repositoryFactory = factory;
        String connectionLabel;

        String dbmsId = null;

        if (tableToMove.getProperty().getItem() instanceof DatabaseConnectionItem) {
            Connection connection = ((DatabaseConnectionItem) tableToMove.getProperty().getItem()).getConnection();
            DatabaseConnection dbConnection = (DatabaseConnection) connection;
            dbmsId = dbConnection.getDbmsId();
            connectionLabel = tableToMove.getLabel();
        } else if (tableToMove.getProperty().getItem() instanceof GenericSchemaConnectionItem) {
            Connection connection = ((GenericSchemaConnectionItem) tableToMove.getProperty().getItem()).getConnection();
            GenericSchemaConnection dbConnection = (GenericSchemaConnection) connection;
            dbmsId = dbConnection.getMappingTypeId();
            connectionLabel = tableToMove.getProperty().getItem().getProperty().getLabel();
        } else {
            connectionLabel = tableToMove.getProperty().getItem().getProperty().getLabel();
        }
        connectionLabel = connectionLabel.replace(" ", "_"); //$NON-NLS-1$ //$NON-NLS-2$

        GenericSchemaConnectionItem connectionItem = PropertiesFactory.eINSTANCE.createGenericSchemaConnectionItem();
        GenericSchemaConnection connection = ConnectionFactory.eINSTANCE.createGenericSchemaConnection();

        if (dbmsId != null && dbmsId.length() > 0) {
            connection.setMappingTypeId(dbmsId);
            connection.setMappingTypeUsed(true);
        }

        MetadataTable metadataTable = ConnectionFactory.eINSTANCE.createMetadataTable();
        if (metadataTable.getNamespace() instanceof Package) {
            Package pkg = (Package) metadataTable.getNamespace();
            pkg.getDataManager().add(connection);
        }
        EList listColumns;
        if (tableToMove instanceof MetadataTableRepositoryObject) {
            listColumns = ((MetadataTableRepositoryObject) tableToMove).getTable().getColumns();
        } else {
            Connection sourceConnection = ((GenericSchemaConnectionItem) tableToMove.getProperty().getItem()).getConnection();
            GenericSchemaConnection dbConnection = (GenericSchemaConnection) sourceConnection;
            listColumns = (ConnectionHelper.getTables(dbConnection).toArray(new MetadataTable[0])[0]).getColumns();
        }

        boolean isConnectionTableSchema = checkIsConnectionTableSchema(tableToMove);
        if (isConnectionTableSchema) {
            metadataTable.setLabel(DEFAULT_TABLE_NAME);
        } else {
            metadataTable.setLabel(tableToMove.getLabel() == null ? DEFAULT_TABLE_NAME : tableToMove.getLabel());
        }
        for (Object temp : listColumns) {
            MetadataColumn column = (MetadataColumnImpl) temp;
            MetadataColumn metadataColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
            metadataColumn.setComment(column.getComment());
            metadataColumn.setLabel(column.getLabel());
            metadataColumn.setDefaultValue(column.getDefaultValue());
            metadataColumn.setKey(column.isKey());

            metadataColumn.setLength(column.getLength());

            metadataColumn.setPrecision(column.getPrecision());

            metadataColumn.setPattern(column.getPattern());
            metadataColumn.setNullable(column.isNullable());
            String originalField = column.getOriginalField();
            if (originalField == null || "".equals(originalField)) { //$NON-NLS-1$
                originalField = column.getLabel();
            }
            metadataColumn.setOriginalField(originalField);
            metadataColumn.setTalendType(column.getTalendType());
            metadataColumn.setSourceType(column.getSourceType());

            metadataTable.getColumns().add(metadataColumn);
        }

        Property connectionProperty = PropertiesFactory.eINSTANCE.createProperty();
        connectionProperty.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                .getUser());
        connectionProperty.setVersion(VersionUtils.DEFAULT_VERSION);
        connectionProperty.setStatusCode(""); //$NON-NLS-1$
        connectionProperty.setLabel(connectionLabel);
        metadataTable.setId(factory.getNextId());

        GenericPackage g = (GenericPackage) ConnectionHelper.getPackage(connection.getName(), (Connection) connection,
                GenericPackage.class);
        if (g != null) { // hywang
            g.getOwnedElement().add(metadataTable);
        } else {
            GenericPackage gpkg = ConnectionFactory.eINSTANCE.createGenericPackage();
            PackageHelper.addMetadataTable(metadataTable, gpkg);
            ConnectionHelper.addPackage(gpkg, connection);

        }
        connectionItem.setConnection(connection);
        connectionProperty.setItem(connectionItem);
        connectionProperty.setId(factory.getNextId());

        if (!repositoryFactory.isNameAvailable(connectionItem, connectionLabel))// name is existing in generic
            // schema.
            try {
                setPropNewName(connectionProperty);
            } catch (BusinessException e) {
                // e.printStackTrace();
                ExceptionHandler.process(e);
            }

        repositoryFactory.create(connectionItem, targetPath);
    }

    /**
     * Comment method "checkIsConnectionTableSchema".
     * 
     * @param objectToM m isConnectionTableSchema
     * @return
     */
    private static boolean checkIsConnectionTableSchema(IRepositoryViewObject objectToMove) {
        if (objectToMove != null && objectToMove.getType() != null) {
            switch (objectToMove.getType()) {
            case METADATA_CON_TABLE:
            case METADATA_CON_SYNONYM:
            case METADATA_CON_VIEW:
                return true;
            default:
                return false;
            }
        }
        return false;
    }

    /**
     * Comment method "getCopiedLabel".
     * 
     * @param copiedProperty
     * @return
     * @throws PersistenceException
     * @throws BusinessException
     */
    private static void setPropNewName(Property copiedProperty) throws PersistenceException, BusinessException {
        String originalLabel = copiedProperty.getLabel();
        String add1 = "Copy_of_"; //$NON-NLS-1$
        String initialTry = add1 + originalLabel;
        copiedProperty.setLabel(initialTry);
        if (repositoryFactory.isNameAvailable(copiedProperty.getItem(), null)) {
            return;
        } else {
            char j = 'a';
            while (!repositoryFactory.isNameAvailable(copiedProperty.getItem(), null)) {
                if (j > 'z') {
                    throw new BusinessException(Messages.getString("CopyToGenericSchemaHelper.cannotGenarateItem")); //$NON-NLS-1$
                }
                String nextTry = initialTry + "_" + (j++) + ""; //$NON-NLS-1$ //$NON-NLS-2$
                copiedProperty.setLabel(nextTry);
            }
        }
    }
}
