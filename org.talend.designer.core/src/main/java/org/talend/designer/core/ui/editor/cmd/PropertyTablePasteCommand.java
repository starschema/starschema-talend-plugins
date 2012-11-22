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
package org.talend.designer.core.ui.editor.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.commons.ui.swt.advanced.dataeditor.commands.ExtendedTablePasteCommand;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 * @param <B> bean
 */
public class PropertyTablePasteCommand<B> extends ExtendedTablePasteCommand {

    /**
     * DOC amaumont MetadataPasteCommand constructor comment.
     * 
     * @param extendedTable
     * @param validAssignableType
     * @param indexStartAdd
     */
    public PropertyTablePasteCommand(ExtendedTableModel extendedTable, Integer indexStartAdd) {
        super(extendedTable, indexStartAdd);
    }

    /**
     * DOC amaumont MetadataPasteCommand constructor comment.
     * 
     * @param extendedTable
     * @param instanceOfType
     */
    public PropertyTablePasteCommand(ExtendedTableModel extendedTable) {
        super(extendedTable);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.ui.swt.advanced.dataeditor.commands.ExtendedTablePasteCommand#createPastableBeansList(java.util.List)
     */
    @Override
    public List createPastableBeansList(ExtendedTableModel extendedTable, List copiedObjectsList) {
        ArrayList list = new ArrayList();

        for (Object current : copiedObjectsList) {
            if (current instanceof HashMap) {
                // create a new column as a copy of this column
                Map<String, Object> clonedRow = (Map<String, Object>) ((HashMap) current).clone();
                list.add(clonedRow);
            }
        }
        return list;
    }

}
