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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DelimitedFileConnection;
import org.talend.core.model.metadata.builder.connection.EbcdicConnection;
import org.talend.core.model.metadata.builder.connection.FieldSeparator;
import org.talend.core.model.metadata.builder.connection.FileConnection;
import org.talend.core.model.metadata.builder.connection.FileExcelConnection;
import org.talend.core.model.metadata.builder.connection.HL7Connection;
import org.talend.core.model.metadata.builder.connection.PositionalFileConnection;
import org.talend.core.model.metadata.builder.connection.RegexpFileConnection;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.repository.model.IConnParamName;

/**
 * ggu class global comment. Detailled comment
 */
public final class FileConnectionContextUtils {

    private static final ECodeLanguage LANGUAGE = LanguageManager.getCurrentLanguage();

    /**
     * 
     */
    public enum EFileParamName implements IConnParamName {
        // Server,
        File,
        RowSeparator,
        Encoding,
        Limit,
        Header,
        Footer,
        //
        FieldSeparator,
        RegExpression, // regular
        // excel
        ThousandSeparator,
        DecimalSeparator,
        LastColumn,
        FirstColumn,
    }

    static List<IContextParameter> getFileVariables(String prefixName, FileConnection conn, Set<IConnParamName> paramSet) {
        if (conn == null || prefixName == null || paramSet == null || paramSet.isEmpty()) {
            return Collections.emptyList();
        }
        List<IContextParameter> varList = new ArrayList<IContextParameter>();
        prefixName = prefixName + ConnectionContextHelper.LINE;
        String paramName = null;
        for (IConnParamName param : paramSet) {
            if (param instanceof EFileParamName) {
                EFileParamName fileParam = (EFileParamName) param;
                paramName = prefixName + fileParam;
                switch (fileParam) {
                case File:
                    String filePath = conn.getFilePath();
                    if (LANGUAGE == ECodeLanguage.PERL) {
                        filePath = TalendTextUtils.addQuotes(filePath);
                    }
                    ConnectionContextHelper.createParameters(varList, paramName, filePath, JavaTypesManager.FILE);
                    break;
                case Encoding:
                    // qli modified to fix the bug 6995.
                    String encoding = conn.getEncoding();
                    if (LANGUAGE.equals(ECodeLanguage.PERL)) {
                        encoding = TalendTextUtils.addQuotes(encoding);
                    }
                    ConnectionContextHelper.createParameters(varList, paramName, encoding);
                    break;
                case Limit:
                    ConnectionContextHelper.createParameters(varList, paramName, conn.getLimitValue(), JavaTypesManager.INTEGER);
                    break;
                case Header:
                    ConnectionContextHelper.createParameters(varList, paramName, conn.getHeaderValue(), JavaTypesManager.INTEGER);
                    break;
                case Footer:
                    ConnectionContextHelper.createParameters(varList, paramName, conn.getFooterValue(), JavaTypesManager.INTEGER);
                    break;
                case RowSeparator:
                    if (conn instanceof DelimitedFileConnection || conn instanceof PositionalFileConnection
                            || conn instanceof RegexpFileConnection) {
                        ConnectionContextHelper.createParameters(varList, paramName, conn.getRowSeparatorValue());
                    }
                    break;
                case FieldSeparator:
                    if (conn instanceof DelimitedFileConnection || conn instanceof PositionalFileConnection) {
                        ConnectionContextHelper.createParameters(varList, paramName, conn.getFieldSeparatorValue());
                    }
                    break;
                case RegExpression:
                    if (conn instanceof RegexpFileConnection) {
                        ConnectionContextHelper.createParameters(varList, paramName, conn.getFieldSeparatorValue());
                    }
                    break;
                default:
                    if (conn instanceof FileExcelConnection) {
                        FileExcelConnection excelConn = (FileExcelConnection) conn;
                        switch (fileParam) {
                        case FirstColumn:
                            ConnectionContextHelper.createParameters(varList, paramName, excelConn.getFirstColumn(),
                                    JavaTypesManager.INTEGER);
                            break;
                        case LastColumn:
                            ConnectionContextHelper.createParameters(varList, paramName, excelConn.getLastColumn(),
                                    JavaTypesManager.INTEGER);
                            break;
                        case ThousandSeparator:
                            if (LANGUAGE == ECodeLanguage.JAVA) {
                                ConnectionContextHelper.createParameters(varList, paramName, excelConn.getThousandSeparator());
                            }
                            break;
                        case DecimalSeparator:
                            if (LANGUAGE == ECodeLanguage.JAVA) {
                                ConnectionContextHelper.createParameters(varList, paramName, excelConn.getDecimalSeparator());
                            }
                            break;
                        default:
                        }
                    }
                    break;
                }
            }
        }

        return varList;
    }

    static void setPropertiesForContextMode(String prefixName, FileConnection conn, Set<IConnParamName> paramSet) {
        if (conn == null || prefixName == null || paramSet == null || paramSet.isEmpty()) {
            return;
        }
        prefixName = prefixName + ConnectionContextHelper.LINE;
        String paramName = null;
        for (IConnParamName param : paramSet) {
            if (param instanceof EFileParamName) {
                EFileParamName fileParam = (EFileParamName) param;
                paramName = prefixName + fileParam;
                switch (fileParam) {
                case File:
                    conn.setFilePath(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
                    break;
                case Encoding:
                    conn.setEncoding(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
                    break;
                case Limit:
                    conn.setLimitValue(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
                    break;
                case Header:
                    conn.setHeaderValue(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
                    break;
                case Footer:
                    conn.setFooterValue(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
                    break;
                case RowSeparator:
                    if (conn instanceof DelimitedFileConnection || conn instanceof PositionalFileConnection
                            || conn instanceof RegexpFileConnection) {
                        conn.setRowSeparatorValue(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
                    }
                    break;
                case FieldSeparator:
                case RegExpression:
                    if (conn instanceof DelimitedFileConnection || conn instanceof PositionalFileConnection
                            || conn instanceof RegexpFileConnection) {
                        conn.setFieldSeparatorValue(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
                        if (conn instanceof DelimitedFileConnection) {
                            ((DelimitedFileConnection) conn).setFieldSeparatorType(FieldSeparator.CUSTOM_UTF8_LITERAL);
                        }
                    }
                    break;
                default:
                    if (conn instanceof FileExcelConnection) {
                        FileExcelConnection excelConn = (FileExcelConnection) conn;
                        switch (fileParam) {
                        case FirstColumn:
                            excelConn.setFirstColumn(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
                            break;
                        case LastColumn:
                            excelConn.setLastColumn(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
                            break;
                        case ThousandSeparator:
                            if (LANGUAGE == ECodeLanguage.JAVA) {
                                excelConn.setThousandSeparator(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
                            }
                            break;
                        case DecimalSeparator:
                            if (LANGUAGE == ECodeLanguage.JAVA) {
                                excelConn.setDecimalSeparator(ContextParameterUtils.getNewScriptCode(paramName, LANGUAGE));
                            }
                            break;
                        default:
                        }
                    }
                    break;
                }
            }
        }

    }

    /**
     * 
     * ggu Comment method "cloneOriginalValueConnection".
     * 
     * perhaps, if connection is in context mode, will open dialog to choose context sets.
     */
    public static FileConnection cloneOriginalValueConnection(FileConnection fileConn) {
        return cloneOriginalValueConnection(null, fileConn, false);
    }

    public static FileConnection cloneOriginalValueConnection(Shell shell, FileConnection fileConn, boolean defaultContext) {
        if (fileConn == null) {
            return null;
        }
        ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(shell, fileConn, null, defaultContext);
        return cloneOriginalValueConnection(fileConn, contextType);
    }

    /**
     * 
     * ggu Comment method "cloneOriginalValueConnection".
     * 
     * 
     */
    @SuppressWarnings("unchecked")
    public static FileConnection cloneOriginalValueConnection(FileConnection fileConn, ContextType contextType) {
        if (fileConn == null) {
            return null;
        }

        FileConnection cloneConn = null;
        if (fileConn instanceof DelimitedFileConnection) {
            cloneConn = ConnectionFactory.eINSTANCE.createDelimitedFileConnection();
        } else if (fileConn instanceof PositionalFileConnection) {
            cloneConn = ConnectionFactory.eINSTANCE.createPositionalFileConnection();
        } else if (fileConn instanceof RegexpFileConnection) {
            cloneConn = ConnectionFactory.eINSTANCE.createRegexpFileConnection();
        } else if (fileConn instanceof FileExcelConnection) {
            cloneConn = ConnectionFactory.eINSTANCE.createFileExcelConnection();
        } else if (fileConn instanceof HL7Connection) {
            cloneConn = ConnectionFactory.eINSTANCE.createHL7Connection();
        } else if (fileConn instanceof EbcdicConnection) {
            cloneConn = ConnectionFactory.eINSTANCE.createEbcdicConnection();
        }

        if (cloneConn != null) {
            String filePath = ConnectionContextHelper.getOriginalValue(contextType, fileConn.getFilePath());
            String encoding = ConnectionContextHelper.getOriginalValue(contextType, fileConn.getEncoding());
            String headValue = ConnectionContextHelper.getOriginalValue(contextType, fileConn.getHeaderValue());
            String footerValue = ConnectionContextHelper.getOriginalValue(contextType, fileConn.getFooterValue());
            String limitValue = ConnectionContextHelper.getOriginalValue(contextType, fileConn.getLimitValue());

            filePath = TalendTextUtils.removeQuotes(filePath);
            // qli modified to fix the bug 6995.
            encoding = TalendTextUtils.removeQuotes(encoding);
            cloneConn.setFilePath(filePath);
            cloneConn.setEncoding(encoding);
            cloneConn.setHeaderValue(headValue);
            cloneConn.setFooterValue(footerValue);
            cloneConn.setLimitValue(limitValue);

            //
            if (fileConn instanceof DelimitedFileConnection || fileConn instanceof PositionalFileConnection
                    || fileConn instanceof RegexpFileConnection) {
                String fieldSeparatorValue = ConnectionContextHelper.getOriginalValue(contextType, fileConn
                        .getFieldSeparatorValue());
                String rowSeparatorValue = ConnectionContextHelper.getOriginalValue(contextType, fileConn.getRowSeparatorValue());

                cloneConn.setFieldSeparatorValue(fieldSeparatorValue);
                cloneConn.setRowSeparatorValue(rowSeparatorValue);

                if (fileConn instanceof DelimitedFileConnection) {
                    ((DelimitedFileConnection) cloneConn).setFieldSeparatorType(((DelimitedFileConnection) fileConn)
                            .getFieldSeparatorType());
                }
            }
            // excel
            if (fileConn instanceof FileExcelConnection) {
                FileExcelConnection excelConnection = (FileExcelConnection) fileConn;
                FileExcelConnection cloneExcelConnection = (FileExcelConnection) cloneConn;

                String thousandSeparator = ConnectionContextHelper.getOriginalValue(contextType, excelConnection
                        .getThousandSeparator());
                String decimalSeparator = ConnectionContextHelper.getOriginalValue(contextType, excelConnection
                        .getDecimalSeparator());
                String firstColumn = ConnectionContextHelper.getOriginalValue(contextType, excelConnection.getFirstColumn());
                String lastColumn = ConnectionContextHelper.getOriginalValue(contextType, excelConnection.getLastColumn());

                cloneExcelConnection.setThousandSeparator(thousandSeparator);
                cloneExcelConnection.setDecimalSeparator(decimalSeparator);
                cloneExcelConnection.setFirstColumn(firstColumn);
                cloneExcelConnection.setLastColumn(lastColumn);

                cloneExcelConnection.setSelectAllSheets(excelConnection.isSelectAllSheets());
                cloneExcelConnection.setSheetName(excelConnection.getSheetName());

                ArrayList sheetList = excelConnection.getSheetList();
                cloneExcelConnection.setSheetList((ArrayList) sheetList.clone());

                EList sheetColumns = excelConnection.getSheetColumns();
                if (sheetColumns != null && sheetColumns instanceof BasicEList) {
                    cloneExcelConnection.getSheetColumns().addAll((EList) ((BasicEList) sheetColumns).clone());
                }

                cloneExcelConnection.setAdvancedSpearator(excelConnection.isAdvancedSpearator());

                cloneConn.setFieldSeparatorValue(fileConn.getFieldSeparatorValue());
                cloneConn.setRowSeparatorType(fileConn.getRowSeparatorType());
                cloneConn.setRowSeparatorValue(fileConn.getRowSeparatorValue());
            }

            cloneConn.setRowSeparatorType(fileConn.getRowSeparatorType());

            cloneConn.setCsvOption(fileConn.isCsvOption());
            cloneConn.setEscapeChar(fileConn.getEscapeChar());
            cloneConn.setEscapeType(fileConn.getEscapeType());
            cloneConn.setFirstLineCaption(fileConn.isFirstLineCaption());
            cloneConn.setFormat(fileConn.getFormat());
            cloneConn.setRemoveEmptyRow(fileConn.isRemoveEmptyRow());
            cloneConn.setServer(fileConn.getServer());
            cloneConn.setTextEnclosure(fileConn.getTextEnclosure());
            cloneConn.setTextIdentifier(fileConn.getTextIdentifier());
            cloneConn.setUseFooter(fileConn.isUseFooter());
            cloneConn.setUseHeader(fileConn.isUseHeader());
            cloneConn.setUseLimit(fileConn.isUseLimit());

            ConnectionContextHelper.cloneConnectionProperties(fileConn, cloneConn);
        }
        return cloneConn;
    }

    static void revertPropertiesForContextMode(FileConnection fileConn, ContextType contextType) {
        if (fileConn == null || contextType == null) {
            return;
        }
        String filePath = ConnectionContextHelper.getOriginalValue(contextType, fileConn.getFilePath());
        String encoding = ConnectionContextHelper.getOriginalValue(contextType, fileConn.getEncoding());
        String headValue = ConnectionContextHelper.getOriginalValue(contextType, fileConn.getHeaderValue());
        String footerValue = ConnectionContextHelper.getOriginalValue(contextType, fileConn.getFooterValue());
        String limitValue = ConnectionContextHelper.getOriginalValue(contextType, fileConn.getLimitValue());

        filePath = TalendTextUtils.removeQuotes(filePath);
        // qli modified to fix the bug 6995.
        encoding = TalendTextUtils.removeQuotes(encoding);
        fileConn.setFilePath(filePath);
        fileConn.setEncoding(encoding);
        fileConn.setHeaderValue(headValue);
        fileConn.setFooterValue(footerValue);
        fileConn.setLimitValue(limitValue);

        //
        if (fileConn instanceof DelimitedFileConnection || fileConn instanceof PositionalFileConnection
                || fileConn instanceof RegexpFileConnection) {
            String fieldSeparatorValue = ConnectionContextHelper.getOriginalValue(contextType, fileConn.getFieldSeparatorValue());
            String rowSeparatorValue = ConnectionContextHelper.getOriginalValue(contextType, fileConn.getRowSeparatorValue());

            fileConn.setFieldSeparatorValue(fieldSeparatorValue);
            fileConn.setRowSeparatorValue(rowSeparatorValue);

        }
        // excel
        if (fileConn instanceof FileExcelConnection) {
            FileExcelConnection excelConnection = (FileExcelConnection) fileConn;

            String thousandSeparator = ConnectionContextHelper.getOriginalValue(contextType, excelConnection
                    .getThousandSeparator());
            String decimalSeparator = ConnectionContextHelper
                    .getOriginalValue(contextType, excelConnection.getDecimalSeparator());
            String firstColumn = ConnectionContextHelper.getOriginalValue(contextType, excelConnection.getFirstColumn());
            String lastColumn = ConnectionContextHelper.getOriginalValue(contextType, excelConnection.getLastColumn());

            excelConnection.setThousandSeparator(thousandSeparator);
            excelConnection.setDecimalSeparator(decimalSeparator);
            excelConnection.setFirstColumn(firstColumn);
            excelConnection.setLastColumn(lastColumn);
        }
    }

    public static void checkAndInitRowsToSkip() {

    }
}
