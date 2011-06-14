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

package org.talend.repository.ui.wizards.exportjob.scriptsmanager.petals;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.talend.repository.i18n.Messages;

/**
 * A dialog to define the way contexts will be exported in the WSDL.
 * 
 * @author Vincent Zurczak - EBM WebSourcing
 */
public class ContextExportDialog extends TitleAreaDialog {

    private static final String NAME_PROPERTY = Messages.getString("ContextExportDialog_Name"); //$NON-NLS-1$

    private static final String XSD_TYPE_PROPERTY = Messages.getString("ContextExportDialog_XsdType"); //$NON-NLS-1$

    private static final String EXPORT_MODE_PROPERTY = Messages.getString("ContextExportDialog_ExportMode"); //$NON-NLS-1$

    private final List<ContextTypeDefinition> contexts;

    /**
     * Constructor.
     * 
     * @param parentShell
     */
    public ContextExportDialog(Shell parentShell, List<ContextTypeDefinition> contexts) {
        super(parentShell);
        this.contexts = contexts;

        setShellStyle(SWT.PRIMARY_MODAL | SWT.TITLE | SWT.BORDER | SWT.RESIZE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.TitleAreaDialog #createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {

        Composite composite = (Composite) super.createDialogArea(parent);
        getShell().setText(Messages.getString("ContextExportDialog_ContextsExport")); //$NON-NLS-1$
        setTitle(Messages.getString("ContextExportDialog_ContextsExport")); //$NON-NLS-1$
        setMessage(Messages.getString("ContextExportDialog_ContextsExportDesc")); //$NON-NLS-1$

        Table table = new Table(composite, SWT.FULL_SELECTION | SWT.BORDER);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableLayout layout = new TableLayout();
        layout.addColumnData(new ColumnWeightData(30, 75, true));
        layout.addColumnData(new ColumnWeightData(25, 75, true));
        layout.addColumnData(new ColumnWeightData(45, 75, true));
        table.setLayout(layout);

        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText(NAME_PROPERTY);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(XSD_TYPE_PROPERTY);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(EXPORT_MODE_PROPERTY);

        final TableViewer viewer = new TableViewer(table);
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new ITableLabelProvider() {

            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }

            public String getColumnText(Object element, int columnIndex) {
                String result = ""; //$NON-NLS-1$
                ContextTypeDefinition def = (ContextTypeDefinition) element;

                switch (columnIndex) {
                case 0:
                    result = def.getDefinition().getName();
                    break;
                case 1:
                    result = def.getDefinition().getType();
                    break;
                case 2:
                    result = def.getExportType().toString();
                    break;
                }
                return result;
            }

            public void addListener(ILabelProviderListener listener) {
                // nothing
            }

            public void dispose() {
                // nothing
            }

            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            public void removeListener(ILabelProviderListener listener) {
                // nothing
            }
        });

        final String[] choices = new String[] { ContextExportType.NOT_EXPORTED.toString(),
                ContextExportType.PARAMETER.toString(), ContextExportType.IN_ATTACHMENT.toString(),
                ContextExportType.OUT_ATTACHMENT.toString(), ContextExportType.PARAMETER_AND_OUT_ATTACHMENT.toString() };

        viewer.setCellModifier(new ICellModifier() {

            public void modify(Object element, String property, Object value) {

                TableItem tableItem = (TableItem) element;
                ContextTypeDefinition data = (ContextTypeDefinition) tableItem.getData();
                if (EXPORT_MODE_PROPERTY.equals(property)) {
                    Integer pos = (Integer) value;
                    data.setExportType(ContextExportType.resolve(choices[pos]));
                    viewer.refresh(data);
                    validate();
                }
            }

            public Object getValue(Object element, String property) {

                Object value = null;
                ContextTypeDefinition def = (ContextTypeDefinition) element;
                if (NAME_PROPERTY.equals(property))
                    value = def.getDefinition().getName();
                else if (XSD_TYPE_PROPERTY.equals(property))
                    value = def.getDefinition().getType();
                else {
                    int pos = Arrays.asList(choices).indexOf(def.getExportType().toString());
                    value = Integer.valueOf(pos);
                }

                return value;
            }

            public boolean canModify(Object element, String property) {
                return EXPORT_MODE_PROPERTY.equals(property);
            }
        });

        viewer.setCellEditors(new CellEditor[] { new TextCellEditor(table), new TextCellEditor(table),
                new ComboBoxCellEditor(table, choices, SWT.READ_ONLY) });

        viewer.setColumnProperties(new String[] { NAME_PROPERTY, XSD_TYPE_PROPERTY, EXPORT_MODE_PROPERTY });
        viewer.setInput(this.contexts);
        return composite;
    }

    /**
     * @return the contexts
     */
    public List<ContextTypeDefinition> getContexts() {
        return this.contexts;
    }

    /**
     * Validates the user choices.
     */
    private void validate() {

        // Error message to show
        for (ContextTypeDefinition def : this.contexts) {
            if (!def.attachmentSupported) {
                if (def.exportType != ContextExportType.NOT_EXPORTED && def.exportType != ContextExportType.PARAMETER)
                    updateStatus(def.definition.getName() + Messages.getString("ContextExportDialog_AttachmentsNotSupported")); //$NON-NLS-1$
                else
                    updateStatus(null);
            }
        }
    }

    /**
     * Updates the error message and enables or disables the OK button.
     * 
     * @param msg the error message
     */
    private void updateStatus(String msg) {

        setErrorMessage(msg);
        if (getButton(IDialogConstants.OK_ID) != null)
            getButton(IDialogConstants.OK_ID).setEnabled(msg == null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog #createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {

        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        validate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#getInitialSize()
     */
    @Override
    protected Point getInitialSize() {
        return new Point(440, 260);
    }
}
