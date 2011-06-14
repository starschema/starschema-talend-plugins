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
package org.talend.repository.ui.wizards.metadata.connection;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.CorePlugin;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.types.JavaDataTypeHelper;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.metadata.types.PerlDataTypeHelper;
import org.talend.core.model.metadata.types.PerlTypesManager;
import org.talend.core.prefs.ui.MetadataTypeLengthConstants;
import org.talend.core.ui.metadata.editor.MetadataEmfTableEditorView;
import org.talend.core.utils.CsvArray;
import org.talend.repository.ui.swt.preview.ShadowProcessPreview;
import org.talend.repository.ui.utils.ColumnNameValidator;

/**
 * DOC hyWang class global comment. Detailled comment
 */
public class GuessSchemaUtil {

    private static final String DEFAULT_LABEL = "Column"; //$NON-NLS-1$

    public static List<MetadataColumn> guessSchemaFromArray(final CsvArray csvArray, boolean isFirstLineCaption,
            MetadataEmfTableEditorView tableEditorView, int header) {
        List<MetadataColumn> columns = new ArrayList<MetadataColumn>();

        if (csvArray == null) {
            return columns;
        } else {

            List<String[]> csvRows = csvArray.getRows();

            if (csvRows.isEmpty()) {
                return columns;
            }
            String[] fields = csvRows.get(0);
            // int numberOfCol = fields.size();

            Integer numberOfCol = getRightFirstRow(csvRows);

            // define the label to the metadata width the content of the first
            // row
            int firstRowToExtractMetadata = header;

            // the first rows is used to define the label of any metadata
            String[] label = new String[numberOfCol.intValue()];
            for (int i = 0; i < numberOfCol; i++) {
                label[i] = DEFAULT_LABEL + i;
                if (isFirstLineCaption) {
                    if (numberOfCol <= fields.length) {// if current field size
                        // is greater than or
                        // equals bigest column
                        // size
                        if (fields[i] != null && !("").equals(fields[i])) { //$NON-NLS-1$
                            label[i] = fields[i].trim().replaceAll(" ", "_"); //$NON-NLS-1$ //$NON-NLS-2$
                            label[i] = ColumnNameValidator.validateColumnNameFormat(label[i], i);
                        } else {
                            label[i] = DEFAULT_LABEL + i;
                        }
                    } else {// current field size is less than bigest column
                        // size
                        if (i < fields.length) {
                            if (fields[i] != null && !("").equals(fields[i])) { //$NON-NLS-1$
                                label[i] = fields[i].trim().replaceAll(" ", "_"); //$NON-NLS-1$ //$NON-NLS-2$
                            } else {
                                label[i] = DEFAULT_LABEL + " " + i; //$NON-NLS-1$ 
                            }
                        } else {
                            label[i] = DEFAULT_LABEL + " " + i; //$NON-NLS-1$ 
                        }
                    }
                }
            }
            // fix bug 5694: column names check in FileDelimited wizard fails to
            // rename duplicate column name
            ShadowProcessPreview.fixDuplicateNames(label);

            for (int i = 0; i < numberOfCol.intValue(); i++) {
                // define the first currentType and assimile it to globalType
                String globalType = null;
                int lengthValue = 0;
                int precisionValue = 0;

                int current = firstRowToExtractMetadata;
                while (globalType == null) {
                    if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                        // see the feature 6296,qli comment
                        if (current == csvRows.size()) {
                            globalType = "id_String";//$NON-NLS-1$
                            continue;
                        } else if (i >= csvRows.get(current).length) {
                            globalType = "id_String"; //$NON-NLS-1$
                        } else {
                            globalType = JavaDataTypeHelper.getTalendTypeOfValue(csvRows.get(current)[i]);
                            current++;
                        }
                    } else {
                        if (current == csvRows.size()) {
                            globalType = "String"; //$NON-NLS-1$
                            continue;
                        }
                        if (i >= csvRows.get(current).length) {
                            globalType = "String"; //$NON-NLS-1$
                        } else {
                            globalType = PerlDataTypeHelper.getTalendTypeOfValue(csvRows.get(current)[i]);
                            current++;
                        }
                    }
                }

                // for another lines
                for (int f = firstRowToExtractMetadata; f < csvRows.size(); f++) {
                    fields = csvRows.get(f);
                    if (fields.length > i) {
                        String value = fields[i];
                        if (!value.equals("")) { //$NON-NLS-1$
                            if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                                if (!JavaDataTypeHelper.getTalendTypeOfValue(value).equals(globalType)) {
                                    globalType = JavaDataTypeHelper.getCommonType(globalType, JavaDataTypeHelper
                                            .getTalendTypeOfValue(value));
                                }
                            } else {
                                if (!PerlDataTypeHelper.getTalendTypeOfValue(value).equals(globalType)) {
                                    globalType = PerlDataTypeHelper.getCommonType(globalType, PerlDataTypeHelper
                                            .getTalendTypeOfValue(value));
                                }
                            }
                            if (lengthValue < value.length()) {
                                lengthValue = value.length();
                            }
                            int positionDecimal = 0;
                            if (value.indexOf(',') > -1) {
                                positionDecimal = value.lastIndexOf(',');
                                precisionValue = lengthValue - positionDecimal;
                            } else if (value.indexOf('.') > -1) {
                                positionDecimal = value.lastIndexOf('.');
                                precisionValue = lengthValue - positionDecimal;
                            }
                        } else {
                            if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                                if (CorePlugin.getDefault().getPreferenceStore().getString(
                                        MetadataTypeLengthConstants.VALUE_DEFAULT_TYPE) != null
                                        && !CorePlugin.getDefault().getPreferenceStore().getString(
                                                MetadataTypeLengthConstants.VALUE_DEFAULT_TYPE).equals("")) { //$NON-NLS-1$
                                    globalType = CorePlugin.getDefault().getPreferenceStore().getString(
                                            MetadataTypeLengthConstants.VALUE_DEFAULT_TYPE);
                                    if (CorePlugin.getDefault().getPreferenceStore().getString(
                                            MetadataTypeLengthConstants.VALUE_DEFAULT_LENGTH) != null
                                            && !CorePlugin.getDefault().getPreferenceStore().getString(
                                                    MetadataTypeLengthConstants.VALUE_DEFAULT_LENGTH).equals("")) { //$NON-NLS-1$
                                        lengthValue = Integer.parseInt(CorePlugin.getDefault().getPreferenceStore().getString(
                                                MetadataTypeLengthConstants.VALUE_DEFAULT_LENGTH));
                                    }
                                }
                            } else {
                                if (CorePlugin.getDefault().getPreferenceStore().getString(
                                        MetadataTypeLengthConstants.PERL_VALUE_DEFAULT_TYPE) != null
                                        && !CorePlugin.getDefault().getPreferenceStore().getString(
                                                MetadataTypeLengthConstants.PERL_VALUE_DEFAULT_TYPE).equals("")) { //$NON-NLS-1$
                                    globalType = CorePlugin.getDefault().getPreferenceStore().getString(
                                            MetadataTypeLengthConstants.PERL_VALUE_DEFAULT_TYPE);
                                    if (CorePlugin.getDefault().getPreferenceStore().getString(
                                            MetadataTypeLengthConstants.PERL_VALUE_DEFAULT_LENGTH) != null
                                            && !CorePlugin.getDefault().getPreferenceStore().getString(
                                                    MetadataTypeLengthConstants.PERL_VALUE_DEFAULT_LENGTH).equals("")) { //$NON-NLS-1$
                                        lengthValue = Integer.parseInt(CorePlugin.getDefault().getPreferenceStore().getString(
                                                MetadataTypeLengthConstants.PERL_VALUE_DEFAULT_LENGTH));
                                    }
                                }
                            }

                        }
                    }
                }
                // see the feature 6296,qli comment
                if (csvRows.size() <= 1 && firstRowToExtractMetadata == 1) {
                    lengthValue = 255;
                }

                // define the metadataColumn to field i
                MetadataColumn metadataColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
                metadataColumn.setPattern("\"dd-MM-yyyy\""); //$NON-NLS-1$
                // Convert javaType to TalendType
                String talendType = null;
                if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                    talendType = globalType;
                    if (globalType.equals(JavaTypesManager.FLOAT.getId()) || globalType.equals(JavaTypesManager.DOUBLE.getId())) {
                        metadataColumn.setPrecision(precisionValue);
                    } else {
                        metadataColumn.setPrecision(0);
                    }
                } else {
                    talendType = PerlTypesManager.getNewTypeName(MetadataTalendType.loadTalendType(globalType,
                            "TALENDDEFAULT", false)); //$NON-NLS-1$
                    if (globalType.equals("FLOAT") || globalType.equals("DOUBLE")) { //$NON-NLS-1$ //$NON-NLS-2$
                        metadataColumn.setPrecision(precisionValue);
                    } else {
                        metadataColumn.setPrecision(0);
                    }
                }
                metadataColumn.setTalendType(talendType);
                metadataColumn.setLength(lengthValue);
                // Check the label and add it to the table
                metadataColumn.setLabel(tableEditorView.getMetadataEditor().getNextGeneratedColumnName(label[i]));
                columns.add(i, metadataColumn);
            }
        }
        return columns;
    }

    // CALCULATE THE NULBER OF COLUMNS IN THE PREVIEW
    private static Integer getRightFirstRow(List<String[]> csvRows) {

        Integer numbersOfColumns = null;
        int parserLine = csvRows.size();
        if (parserLine > 50) {
            parserLine = 50;
        }
        for (int i = 0; i < parserLine; i++) {
            if (csvRows.get(i) != null) {
                String[] nbRow = csvRows.get(i);
                // List<XmlField> nbRowFields = nbRow.getFields();
                if (numbersOfColumns == null || nbRow.length >= numbersOfColumns) {
                    numbersOfColumns = nbRow.length;
                }
            }
        }
        return numbersOfColumns;
    }

}
