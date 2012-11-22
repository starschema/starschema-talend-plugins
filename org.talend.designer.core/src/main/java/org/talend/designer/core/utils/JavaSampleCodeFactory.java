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
package org.talend.designer.core.utils;

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC YeXiaowei class global comment. Detailled comment
 */
public final class JavaSampleCodeFactory implements ISampleCodeFactory {

    private JavaSampleCodeFactory() {

    }

    private static JavaSampleCodeFactory instance = null;

    public static JavaSampleCodeFactory getInstance() {
        if (instance == null) {
            instance = new JavaSampleCodeFactory();
        }
        return instance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.utils.ISampleCodeFactory#generateCode(org.talend.designer.core.ui.editor.nodes.Node)
     */
    public Command generateCodeForParameters(final Node node) {

        String uniqueName = node.getUniqueName();

        // see feature 4131
        if (uniqueName.startsWith("tJavaRow_")) { //$NON-NLS-1$
            // Generate code for tJavaRow component
            String codeString = generateJavaRowCode(node);

            if (codeString != null) {
                return new PropertyChangeCommand(node, "CODE", codeString); //$NON-NLS-1$
            }
        }

        return null;
    }

    /**
     * 
     * DOC YeXiaowei Comment method "generateJavaRowCode".
     * 
     * @param node
     * @return
     */
    private String generateJavaRowCode(final Node node) {
        String primeVlue = "// code sample:\r\n" + "//\r\n" + "// multiply by 2 the row identifier\r\n"
                + "// output_row.id = input_row.id * 2;\r\n" + "//\r\n" + "// lowercase the name\r\n"
                + "// output_row.name = input_row.name.toLowerCase();";
        if (node.getMetadataList() == null || node.getMetadataList().get(0) == null) {
            return primeVlue;
        }

        if (node.getIncomingConnections() == null || node.getIncomingConnections().isEmpty()
                || node.getIncomingConnections().get(0).getMetadataTable() == null) {
            return primeVlue;
        }

        IMetadataTable inputTable = node.getIncomingConnections().get(0).getMetadataTable();
        List<IMetadataColumn> inputColumns = inputTable.getListColumns();

        IMetadataTable outputTable = node.getMetadataList().get(0);
        List<IMetadataColumn> outputColumns = outputTable.getListColumns();

        if (inputColumns == null || inputColumns.isEmpty() || outputColumns == null || outputColumns.isEmpty()) {
            return primeVlue;
        }

        String javaEnding = ";"; //$NON-NLS-1$
        String lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$

        StringBuilder builder = new StringBuilder();
        MessageDialog mgd = new MessageDialog(null, null, null, null, 0, null, 0);
        boolean isSelect = mgd.openConfirm(null, null, "Are you sure to replace use'code to generate code ?");
        if (isSelect) {
            // Add simple comment
            builder.append(Messages.getString("JavaSampleCodeFactory.schema")).append(lineSeparator); //$NON-NLS-1$

            int inputRowsLength = inputColumns.size();
            int ouputRowsLength = outputColumns.size();

            if (inputRowsLength == 0 || ouputRowsLength == 0) {
                return null;
            }

            if (inputRowsLength >= ouputRowsLength) {
                for (int i = 0; i < inputRowsLength; i++) {

                    String inputLabel = inputColumns.get(i).getLabel();

                    String outputLabel = null;

                    if (i > ouputRowsLength - 1) {
                        outputLabel = outputColumns.get(ouputRowsLength - 1).getLabel();
                    } else {
                        outputLabel = outputColumns.get(i).getLabel();
                    }

                    builder
                            .append("output_row.").append(outputLabel).append(" = ").append("input_row.").append(inputLabel).append( //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    javaEnding);

                    builder.append(lineSeparator);
                }
            } else {
                for (int i = 0; i < ouputRowsLength; i++) {

                    String outputLabel = outputColumns.get(i).getLabel();
                    String inputLabel = null;

                    if (i > inputRowsLength - 1) {
                        inputLabel = inputColumns.get(inputRowsLength - 1).getLabel();
                    } else {
                        inputLabel = inputColumns.get(i).getLabel();
                    }

                    builder
                            .append("output_row.").append(outputLabel).append(" = ").append("input_row.").append(inputLabel).append( //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    javaEnding);
                    builder.append(lineSeparator);
                }
            }

            return builder.toString();
        } else {
            return null;
        }

    }

}
