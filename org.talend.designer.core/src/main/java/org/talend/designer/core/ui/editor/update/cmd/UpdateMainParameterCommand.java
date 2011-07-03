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
package org.talend.designer.core.ui.editor.update.cmd;

import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.talend.core.model.metadata.builder.connection.HeaderFooterConnection;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.HeaderFooterConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.update.EUpdateResult;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.model.update.UpdatesConstants;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.preferences.StatsAndLogsConstants;
import org.talend.repository.UpdateRepositoryUtils;

/**
 * ggu class global comment. Detailled comment
 */
public class UpdateMainParameterCommand extends Command {

    private UpdateResult result;

    public UpdateMainParameterCommand(UpdateResult result) {
        super();
        this.result = result;
    }

    @Override
    public void execute() {
        if (result == null) {
            return;
        }
        Object job = result.getJob();
        if (job == null) {
            return;
        }
        if (result.getUpdateObject() != job) {
            return;
        }
        if (job instanceof IProcess2) {
            Process process = (Process) job;

            EComponentCategory category = null;
            switch (result.getUpdateType()) {
            case JOB_PROPERTY_EXTRA:
                category = EComponentCategory.EXTRA;
                break;
            case JOB_PROPERTY_STATS_LOGS:
                category = EComponentCategory.STATSANDLOGS;
                break;
            case JOB_PROPERTY_HEADERFOOTER:
                category = EComponentCategory.HEADERFOOTER;
                break;
            default:
            }
            if (category == EComponentCategory.HEADERFOOTER) {
                if (result.getResultType() == EUpdateResult.UPDATE) {

                    if (result.isChecked()) {
                        for (IElementParameter param : process.getElementParameters()) {
                            if (param.getCategory() == EComponentCategory.HEADERFOOTER) {
                                IElementParameter headerIDParameter = process
                                        .getElementParameter(EParameterName.HEADERFOOTER_HEADERID.getName());
                                if (headerIDParameter != null) {
                                    IRepositoryViewObject lastVersion = UpdateRepositoryUtils
                                            .getRepositoryObjectById((String) headerIDParameter.getValue());
                                    HeaderFooterConnection repositoryConnection = null;
                                    if (lastVersion != null) {
                                        final Item item = lastVersion.getProperty().getItem();
                                        if (item != null && item instanceof ConnectionItem) {
                                            repositoryConnection = (HeaderFooterConnection) ((HeaderFooterConnectionItem) item)
                                                    .getConnection();
                                            if (repositoryConnection != null) {
                                                Boolean isHeader = repositoryConnection.isIsHeader();
                                                String libraries = repositoryConnection.getLibraries();
                                                String mainCode = repositoryConnection.getMainCode();
                                                String imports = repositoryConnection.getImports();
                                                process.getElementParameter(EParameterName.HEADER_ENABLED.getName()).setValue(
                                                        isHeader);
                                                process.getElementParameter(EParameterName.HEADER_LIBRARY.getName()).setValue(
                                                        libraries);
                                                process.getElementParameter(EParameterName.HEADER_CODE.getName()).setValue(
                                                        mainCode);
                                                process.getElementParameter(EParameterName.HEADER_IMPORT.getName()).setValue(
                                                        imports);
                                            }
                                        }
                                    }

                                }

                                IElementParameter footerIDParameter = process
                                        .getElementParameter(EParameterName.HEADERFOOTER_FOOTERID.getName());
                                if (footerIDParameter != null) {
                                    IRepositoryViewObject lastVersion = UpdateRepositoryUtils
                                            .getRepositoryObjectById((String) footerIDParameter.getValue());
                                    HeaderFooterConnection repositoryConnection = null;
                                    if (lastVersion != null) {
                                        final Item item = lastVersion.getProperty().getItem();
                                        if (item != null && item instanceof ConnectionItem) {
                                            repositoryConnection = (HeaderFooterConnection) ((HeaderFooterConnectionItem) item)
                                                    .getConnection();
                                            if (repositoryConnection != null) {
                                                Boolean isHeader = repositoryConnection.isIsHeader();
                                                String libraries = repositoryConnection.getLibraries();
                                                String mainCode = repositoryConnection.getMainCode();
                                                String imports = repositoryConnection.getImports();
                                                process.getElementParameter(EParameterName.FOOTER_ENABLED.getName()).setValue(
                                                        !isHeader);
                                                process.getElementParameter(EParameterName.FOOTER_LIBRARY.getName()).setValue(
                                                        libraries);
                                                process.getElementParameter(EParameterName.FOOTER_CODE.getName()).setValue(
                                                        mainCode);
                                                process.getElementParameter(EParameterName.FOOTER_IMPORT.getName()).setValue(
                                                        imports);
                                            }
                                        }
                                    }

                                }

                            }
                        }
                    }
                }
            } else if (category != null) {
                boolean repository = false;

                if (result.getResultType() == EUpdateResult.UPDATE) {
                    // upgrade from repository
                    if (result.isChecked()) {
                        IElementParameter property = process.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE,
                                category);
                        if (property != null) {
                            Map<String, IElementParameter> childParameters = property.getChildParameters();
                            if (childParameters != null) {
                                IElementParameter elementParameter = childParameters.get(EParameterName.PROPERTY_TYPE.getName());
                                // is repository
                                if (elementParameter != null && EmfComponent.REPOSITORY.equals(elementParameter.getValue())) {
                                    for (IElementParameter param : process.getElementParameters()) {
                                        if (param.getCategory() != category) {
                                            continue;
                                        }
                                        String repositoryValue = param.getRepositoryValue();
                                        if (param.isShow(process.getElementParameters()) && (repositoryValue != null)
                                                && (!param.getName().equals(EParameterName.PROPERTY_TYPE.getName()))) {
                                            Object objectValue = RepositoryToComponentProperty.getValue(
                                                    (org.talend.core.model.metadata.builder.connection.Connection) result
                                                            .getParameter(), repositoryValue, null);
                                            if (objectValue != null) {
                                                if (param.getFieldType().equals(EParameterFieldType.CLOSED_LIST)
                                                        && repositoryValue.equals(UpdatesConstants.TYPE)) {
                                                    boolean found = false;
                                                    String[] items = param.getListRepositoryItems();
                                                    for (int i = 0; (i < items.length) && (!found); i++) {
                                                        if (objectValue.equals(items[i])) {
                                                            found = true;
                                                            process.setPropertyValue(param.getName(),
                                                                    param.getListItemsValue()[i]);
                                                        }
                                                    }
                                                } else {
                                                    process.setPropertyValue(param.getName(), objectValue);
                                                }
                                                param.setRepositoryValueUsed(true);
                                                param.setReadOnly(true);
                                                repository = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
                if (!repository) {
                    IElementParameter property = process
                            .getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE, category);
                    if (property != null) {
                        Map<String, IElementParameter> childParameters = property.getChildParameters();
                        if (childParameters != null) {
                            IElementParameter elementParameter = childParameters.get(EParameterName.PROPERTY_TYPE.getName());
                            elementParameter.setValue(EmfComponent.BUILTIN);
                        }
                    }

                    // built-in
                    IElementParameter dbTypeParam = null;
                    IElementParameter impliciteDbType = null;
                    for (IElementParameter param : process.getElementParameters()) {
                        if (param.getCategory() != category) {
                            continue;
                        }
                        String repositoryValue = param.getRepositoryValue();
                        if (param.isShow(process.getElementParameters()) && (repositoryValue != null)) {
                            // for mysql db verion
                            if (EParameterName.DB_TYPE.getName().equals(param.getName()) && "TYPE".equals(repositoryValue)) {
                                dbTypeParam = param;
                            }
                            if ("DB_TYPE_IMPLICIT_CONTEXT".equals(param.getName()) && "TYPE".equals(repositoryValue)) {
                                impliciteDbType = param;
                            }
                            if (EParameterName.DB_VERSION.getName().equals(repositoryValue) && dbTypeParam != null
                                    && dbTypeParam.getValue() != null) {
                                final int indexOfItem = dbTypeParam.getIndexOfItemFromList(dbTypeParam.getValue().toString());
                                String dbType = dbTypeParam.getListItemsDisplayCodeName()[indexOfItem];
                                setDBVersionForMysql(param, dbType);
                            } else if (EParameterName.DB_VERSION.getName().equals(repositoryValue) && impliciteDbType != null
                                    && impliciteDbType.getValue() != null) {
                                final int indexOfItem = impliciteDbType.getIndexOfItemFromList(impliciteDbType.getValue()
                                        .toString());
                                String dbType = impliciteDbType.getListItemsDisplayCodeName()[indexOfItem];
                                setDBVersionForMysql(param, dbType);
                            }
                            param.setRepositoryValueUsed(false);
                            param.setReadOnly(false);
                        }
                    }
                }
            }
        }

    }

    private void setDBVersionForMysql(IElementParameter dbVersionParam, String dbType) {
        if ("MYSQL".equals(dbType)) {
            String[] drivers = StatsAndLogsConstants.MYSQL_VERSION_DRIVER;
            // if driver is not set , set to the default
            boolean found = false;
            for (int i = 0; i < drivers.length; i++) {
                if (drivers[i].equals(dbVersionParam.getValue())) {
                    found = true;
                }
            }
            if (!found) {
                dbVersionParam.setValue(drivers[0]);
            }
        }
    }

}
