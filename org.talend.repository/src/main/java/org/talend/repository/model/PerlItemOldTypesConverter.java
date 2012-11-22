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
package org.talend.repository.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.talend.commons.exception.PersistenceException;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.types.PerlTypesManager;
import org.talend.core.model.properties.BusinessProcessItem;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.DocumentationItem;
import org.talend.core.model.properties.FileItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.RoutineItem;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.rowgenerator.data.PerlFunctionParser;

/**
 * DOC ggu class global comment. Detailled comment <br/>
 * 
 */
public final class PerlItemOldTypesConverter {

    private boolean isJava = true;

    private boolean isModified = false;

    private Item item;

    public PerlItemOldTypesConverter(Item item) throws PersistenceException {
        this.item = item;
        init();
        convert();
    }

    private void init() {
        switch (LanguageManager.getCurrentLanguage()) {
        case JAVA:
            isJava = true;
            break;
        case PERL:
        default:
            isJava = false;
        }
    }

    /**
     * 
     * DOC ggu Comment method "getItem".
     * 
     * @return the converted Item
     */
    public Item getItem() {
        return item;
    }

    public boolean isModified() {
        return isModified;
    }

    private void convert() throws PersistenceException {
        if (!isJava) {// Only Perl
            if (item instanceof BusinessProcessItem) {
                // Do nothing.
            } else if (item instanceof ProcessItem) {

                changeJobNodeMetadata((ProcessItem) item);
                changeJobContext((ProcessItem) item);

            } else if (item instanceof ContextItem) {

                changeContext((ContextItem) item);

            } else if (item instanceof FileItem) {
                if (item instanceof RoutineItem) {
                    changeRoutines((RoutineItem) item);
                } else if (item instanceof DocumentationItem) {
                    // Do nothing.
                }
            } else if (item instanceof ConnectionItem) {
                changeConnections((ConnectionItem) item);
            }
            setItemProperty(item);
        }
    }

    /*
     * Convert the metadata of nodes in Job Process.
     */
    private void changeJobNodeMetadata(ProcessItem processItem) throws PersistenceException {
        String newPerlType = null;
        String oldPerlType = null;
        // Get the nodes in ProcessItem
        for (Object nodeObject : processItem.getProcess().getNode()) {
            NodeType nodeType = (NodeType) nodeObject;
            // Get the metadata in the node
            for (Object metadataObject : nodeType.getMetadata()) {
                MetadataType metadataType = (MetadataType) metadataObject;
                for (Object columnObject : metadataType.getColumn()) {
                    ColumnType columnType = (ColumnType) columnObject;
                    oldPerlType = columnType.getType();
                    newPerlType = PerlTypesManager.getNewTypeName(oldPerlType);
                    if (newPerlType != null && !oldPerlType.equals(newPerlType)) {
                        columnType.setType(newPerlType);
                        isModified = true;
                    }

                }
            }
        }

    }

    /*
     * Convert the context in Job Process.
     */
    private void changeJobContext(ProcessItem processItem) throws PersistenceException {
        String newPerlType = null;
        String oldPerlType = null;
        // Get the Context
        for (Object contextObject : processItem.getProcess().getContext()) {
            ContextType contextType = (ContextType) contextObject;
            for (Object parameterObject : contextType.getContextParameter()) {
                ContextParameterType parameterType = (ContextParameterType) parameterObject;
                oldPerlType = parameterType.getType();
                newPerlType = PerlTypesManager.getNewTypeName(oldPerlType);
                if (newPerlType != null && !oldPerlType.equals(newPerlType)) {
                    parameterType.setType(newPerlType);
                    isModified = true;
                }
            }
        }

    }

    /*
     * Convert the Context
     */
    private void changeContext(ContextItem contextItem) throws PersistenceException {
        String newPerlType = null;
        String oldPerlType = null;

        for (Object context : contextItem.getContext()) {
            ContextType contextType = (ContextType) context;
            for (Object parameterObject : contextType.getContextParameter()) {
                ContextParameterType parameterType = (ContextParameterType) parameterObject;
                oldPerlType = parameterType.getType();
                newPerlType = PerlTypesManager.getNewTypeName(oldPerlType);
                if (newPerlType != null && !oldPerlType.equals(newPerlType)) {
                    parameterType.setType(newPerlType);
                    isModified = true;
                }
            }
        }

    }

    /*
     * Convert the Context
     */
    private void changeRoutines(RoutineItem routineItem) throws PersistenceException {
        // Ignored the system routines
        if (!RepositoryConstants.SYSTEM_DIRECTORY.equals(routineItem.getState().getPath())) {
            ByteArray byteArray = routineItem.getContent();
            // Get the replaced routines content.
            String newContent = getRoutineNewTypesFileStr(new String(byteArray.getInnerContent()));
            byteArray.setInnerContent(newContent.getBytes());
            routineItem.setContent(byteArray);

        }

    }

    /*
     * Convert the Connections
     */
    private void changeConnections(ConnectionItem connectionItem) throws PersistenceException {
        String newPerlType = null;
        String oldPerlType = null;

        for (Object tableObject : ConnectionHelper.getTables(connectionItem.getConnection())) {
            MetadataTable metadataTable = (MetadataTable) tableObject;
            for (Object columnObject : metadataTable.getColumns()) {
                MetadataColumn metadataColumn = (MetadataColumn) columnObject;
                oldPerlType = metadataColumn.getTalendType();
                newPerlType = PerlTypesManager.getNewTypeName(oldPerlType);
                if (newPerlType != null && !oldPerlType.equals(newPerlType)) {
                    metadataColumn.setTalendType(newPerlType);
                    isModified = true;
                }
            }
        }

    }

    /*******************************************************************************************************************
     * 
     * DOC ggu Comment method "changedRoutineString".
     * 
     * @param oldStr
     * @return the replace new Perl type string.
     */
    private String getRoutineNewTypesFileStr(String oldFileStr) {
        StringBuffer buffer = new StringBuffer();

        Pattern regex = Pattern.compile(PerlFunctionParser.FUNCTION_REGEX, Pattern.CANON_EQ);
        Pattern typeRegex = Pattern.compile(PerlFunctionParser.FUNCTION_TYPE_REGEX, Pattern.CANON_EQ);

        Matcher matcher = regex.matcher(oldFileStr);
        Matcher typeMatcher = null;

        // all matched String
        int index = 0;
        String newPerlType = null;
        String oldPerlType = null;

        while (matcher.find()) {
            String group = matcher.group();
            buffer.append(oldFileStr.substring(index, oldFileStr.indexOf(group)));
            index = oldFileStr.indexOf(group) + group.length();
            // match the TalendType
            typeMatcher = typeRegex.matcher(group);
            if (typeMatcher.find()) {
                String tmpGroup = typeMatcher.group(0);
                oldPerlType = typeMatcher.group(2);
                newPerlType = PerlTypesManager.getNewTypeName(oldPerlType);
                if (newPerlType != null && !oldPerlType.equals(newPerlType)) {
                    // replaced by the new type
                    group = group.replace(tmpGroup, tmpGroup.replace(oldPerlType, newPerlType));
                    isModified = true;
                }
            }

            buffer.append(group);

        }

        buffer.append(oldFileStr.substring(index));

        return buffer.toString();
    }

    /**
     * 
     * DOC ggu Comment method "setItemProperty".<br>
     * 
     * set some item properties
     * 
     * @param item
     */
    private void setItemProperty(Item item) {
        // item.getProperty().setVersion("");
        // item.getProperty().setModificationDate(new Date());
    }
}
