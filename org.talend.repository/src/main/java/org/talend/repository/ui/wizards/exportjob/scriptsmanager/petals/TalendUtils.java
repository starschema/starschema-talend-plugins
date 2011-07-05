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

package org.talend.repository.ui.wizards.exportjob.scriptsmanager.petals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.talend.core.model.metadata.types.ContextParameterJavaTypeManager;
import org.talend.core.model.metadata.types.JavaType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.properties.ProcessItem;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ProcessTypeImpl;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.RepositoryNode;

/**
 * A set of utility methods related to Talend.
 * 
 * @author Vincent Zurczak - EBM WebSourcing
 */
public class TalendUtils {

    /**
     * Checks into the selected job and gets all the components whose name is componentName.
     * 
     * @param selection the current selection
     * @param componentName the component name
     * @return a list (potentially empty) of components whose name is componentName
     */
    public static List<NodeType> getTalendComponentsFromSelectedJob(IStructuredSelection selection, String componentName) {

        List<NodeType> components = new ArrayList<NodeType>();
        for (Iterator<?> it = selection.iterator(); it.hasNext();) {
            Object current = it.next();
            if (!(current instanceof RepositoryNode))
                continue;

            RepositoryNode node = (RepositoryNode) current;
            if (!(node.getObject().getProperty().getItem() instanceof ProcessItem))
                continue;

            ProcessItem item = (ProcessItem) node.getObject().getProperty().getItem();
            for (Object o : item.getProcess().getNode()) {
                if (o instanceof NodeType && componentName.equals(((NodeType) o).getComponentName()))
                    components.add((NodeType) o);
            }
        }

        return components;
    }

    /**
     * Checks whether these components have the same schema.
     * 
     * @param components a list of components (should be of the same type)
     * @return true if they have the same schema, false otherwise
     */
    public static boolean haveSameSchema(List<NodeType> components) {

        boolean result = true;
        EList<?> compararisonReference = null;
        for (int i = 0; result && i < components.size(); i++) {
            NodeType nodeType = components.get(i);

            for (int j = 0; result && j < nodeType.getMetadata().size(); j++) {
                Object o = nodeType.getMetadata().get(j);
                if (!(o instanceof MetadataType))
                    continue;

                MetadataType metaDataType = (MetadataType) o;
                if (compararisonReference == null)
                    compararisonReference = metaDataType.getColumn();
                else
                    result = columnsAreEquals(compararisonReference, metaDataType.getColumn());
            }
        }

        return result;
    }

    /**
     * Compares two lists of column types.
     * 
     * @param compararisonReference the first list
     * @param columns the second list
     * @return true if the list have the same columns with the same properties, false otherwise
     */
    private static boolean columnsAreEquals(EList<?> compararisonReference, EList<?> columns) {

        if (compararisonReference == null)
            return columns == null;

        if (columns == null)
            return false;

        if (compararisonReference.size() != columns.size())
            return false;

        boolean result = true;
        for (int i = 0; result && i < columns.size(); i++) {
            Object refCol = compararisonReference.get(i);
            Object col = columns.get(i);

            if (col instanceof ColumnType && refCol instanceof ColumnType)
                result = columnsAreEquals((ColumnType) col, (ColumnType) refCol);
            else
                result = false;
        }

        return result;
    }

    /**
     * Compares two column types.
     * 
     * @param col the first column
     * @param refCol the second column
     * @return true if the columns have the same properties, false otherwise
     */
    private static boolean columnsAreEquals(ColumnType col, ColumnType refCol) {

        // Simple fields
        if (col.isNullable() != refCol.isNullable())
            return false;

        if (col.isKey() != refCol.isKey())
            return false;

        if (col.getLength() != refCol.getLength())
            return false;

        if (col.getPrecision() != refCol.getPrecision())
            return false;

        // Default value
        if (col.getDefaultValue() == null) {
            if (refCol.getDefaultValue() != null)
                return false;
        } else if (!col.getDefaultValue().equals(refCol.getDefaultValue()))
            return false;

        // Name
        if (col.getName() == null) {
            if (refCol.getName() != null)
                return false;
        } else if (!col.getName().equals(refCol.getName()))
            return false;

        // Pattern
        if (col.getPattern() == null) {
            if (refCol.getPattern() != null)
                return false;
        } else if (!col.getPattern().equals(refCol.getPattern()))
            return false;

        // Type
        if (col.getType() == null) {
            if (refCol.getType() != null)
                return false;
        } else if (!col.getType().equals(refCol.getType()))
            return false;

        // Original column name
        if (col.getOriginalDbColumnName() == null) {
            if (refCol.getOriginalDbColumnName() != null)
                return false;
        } else if (!col.getOriginalDbColumnName().equals(refCol.getOriginalDbColumnName()))
            return false;

        // Source type
        if (col.getSourceType() == null) {
            if (refCol.getSourceType() != null)
                return false;
        } else if (!col.getSourceType().equals(refCol.getSourceType()))
            return false;

        return true;
    }

    /**
     * Builds the schema that will be written in the WSDL for this component.
     * 
     * @param component the component whose schema must be written in the WSDL
     * @return a list of writable objects describing an XML schema
     * @throws PetalsExportException
     */
    public static List<ElementTypeDefinition> getWsdlSchemaForComponent(NodeType component) throws PetalsExportException {

        List<ElementTypeDefinition> result = new ArrayList<ElementTypeDefinition>();
        if (component.getMetadata().size() > 0 && component.getMetadata().get(0) instanceof MetadataType) {

            MetadataType metaDataType = (MetadataType) component.getMetadata().get(0);
            for (Object o : metaDataType.getColumn()) {

                // Name
                ColumnType col = (ColumnType) o;
                ElementTypeDefinition def = new ElementTypeDefinition();
                def.setName(col.getName());
                def.setDefaultValue(col.getDefaultValue());

                JavaType javaType = JavaTypesManager.getJavaTypeFromId(col.getType());
                boolean primitive = JavaTypesManager.isJavaPrimitiveType(col.getType(), col.isNullable());

                // Nillable and minOccurs
                if (col.isNullable()) {
                    if (primitive)
                        def.setMinOccurs(0);
                    else
                        def.setNillable(true);
                }

                // Type and maxOccurs
                String type = getXsdType(javaType);
                def.setType(type);
                result.add(def);
            }
        }

        return result;
    }

    /**
     * Checks whether the component schema contains unsupported types by the Petals export.
     * 
     * @param component the component whose schema must be written in the WSDL
     * @return null if everything is fine, an error message otherwise
     */
    public static String componentSchemaHasUnsupportedTypes(NodeType component) {

        String result = null;
        if (component.getMetadata().size() > 0 && component.getMetadata().get(0) instanceof MetadataType) {

            MetadataType metaDataType = (MetadataType) component.getMetadata().get(0);
            for (int i = 0; result == null && i < metaDataType.getColumn().size(); i++) {
                Object o = metaDataType.getColumn().get(i);

                // Name
                ColumnType col = (ColumnType) o;
                JavaType javaType = JavaTypesManager.getJavaTypeFromId(col.getType());
                try {
                    getXsdType(javaType);

                } catch (PetalsExportException e) {
                    result = e.getMessage();
                }
            }
        }

        return result;
    }

    /**
     * Gets the context parameters for a given job and context.
     * 
     * @param item the process item
     * @return a list of elements that will be completed in the export dialog and then used in the WSDL generation
     * @throws PetalsExportException
     */
    public static List<ContextTypeDefinition> getWsdlSchemaForContexts(ProcessItem item, String contextName)
            throws PetalsExportException {

        List<ContextTypeDefinition> result = new ArrayList<ContextTypeDefinition>();
        for (Object o : ((ProcessTypeImpl) item.getProcess()).getContext()) {
            if (!(o instanceof ContextType))
                continue;

            if (!contextName.equals(((ContextType) o).getName()))
                continue;

            for (Object p : ((ContextType) o).getContextParameter()) {
                if (!(p instanceof ContextParameterType))
                    continue;

                ContextParameterType paramType = (ContextParameterType) p;
                ContextTypeDefinition def = new ContextTypeDefinition();
                def.setExportType(ContextExportType.NOT_EXPORTED);

                ElementTypeDefinition typeDef = new ElementTypeDefinition();
                typeDef.setName(paramType.getName());
                JavaType javaType = ContextParameterJavaTypeManager.getJavaTypeFromId(paramType.getType());
                String type = getXsdType(javaType);
                typeDef.setType(type);

                typeDef.setDefaultValue(paramType.getValue());
                def.setDefinition(typeDef);

                if (javaType == JavaTypesManager.STRING)
                    def.setAttachmentSupported(true);

                result.add(def);
            }
        }

        return result;
    }

    /**
     * Gets the XSD type from a Talend Java type.
     * 
     * @param javaType
     * @return xs:xsdType
     * @throws PetalsExportException
     */
    private static String getXsdType(JavaType javaType) throws PetalsExportException {

        String type = null;
        if (javaType == JavaTypesManager.BIGDECIMAL) {
            type = "xs:decimal"; //$NON-NLS-1$
        } else if (javaType == JavaTypesManager.BOOLEAN) {
            type = "xs:boolean"; //$NON-NLS-1$
        } else if (javaType == JavaTypesManager.BYTE) {
            type = "xs:byte"; //$NON-NLS-1$
        } else if (javaType == JavaTypesManager.BYTE_ARRAY) {
            type = "xs:string"; //$NON-NLS-1$
        } else if (javaType == JavaTypesManager.CHARACTER) {
            type = "xs:string"; //$NON-NLS-1$
        } else if (javaType == JavaTypesManager.DATE) {
            type = "xs:long"; //$NON-NLS-1$
        } else if (javaType == JavaTypesManager.DIRECTORY) {
            type = "xs:string"; //$NON-NLS-1$
        } else if (javaType == JavaTypesManager.DOUBLE) {
            type = "xs:double"; //$NON-NLS-1$
        } else if (javaType == JavaTypesManager.FILE) {
            type = "xs:string"; //$NON-NLS-1$
        } else if (javaType == JavaTypesManager.FLOAT) {
            type = "xs:float"; //$NON-NLS-1$
        } else if (javaType == JavaTypesManager.INTEGER) {
            type = "xs:integer"; //$NON-NLS-1$
        } else if (javaType == JavaTypesManager.LIST) {
            throw new PetalsExportException(Messages.getString("TalendUtils.4")); //$NON-NLS-1$
        } else if (javaType == JavaTypesManager.LONG) {
            type = "xs:long"; //$NON-NLS-1$
        } else if (javaType == JavaTypesManager.OBJECT) {
            throw new PetalsExportException(Messages.getString("TalendUtils.0")); //$NON-NLS-1$
        } else if (javaType == JavaTypesManager.PASSWORD) {
            throw new PetalsExportException(Messages.getString("TalendUtils.1")); //$NON-NLS-1$
        } else if (javaType == JavaTypesManager.SHORT) {
            type = "xs:short"; //$NON-NLS-1$
        } else if (javaType == JavaTypesManager.STRING) {
            type = "xs:string"; //$NON-NLS-1$
        } else if (javaType == JavaTypesManager.VALUE_LIST) {
            throw new PetalsExportException(Messages.getString("TalendUtils.2")); //$NON-NLS-1$
        } else {
            throw new PetalsExportException(javaType.getId() + Messages.getString("TalendUtils.3")); //$NON-NLS-1$
        }

        return type;
    }
}
