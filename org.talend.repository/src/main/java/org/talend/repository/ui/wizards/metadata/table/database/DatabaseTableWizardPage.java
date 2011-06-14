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
package org.talend.repository.ui.wizards.metadata.table.database;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.repository.ui.swt.utils.AbstractForm;
import org.talend.repository.ui.utils.ManagerConnection;

/**
 * TableWizard present the TableForm width the MetaDataTable. Use to create a new table (need a connection to a DB).
 * Page allows setting a table.
 */
public class DatabaseTableWizardPage extends WizardPage {

    private DatabaseTableForm tableForm;

    private ConnectionItem connectionItem;

    private boolean isRepositoryObjectEditable;

    private ManagerConnection managerConnection;

    private IMetadataConnection metadataConnection;

    private DatabaseConnection temConnection;

    private MetadataTable metadataTable;

    /**
     * DatabaseWizardPage constructor (to instance IMetadataConnection OR MetaDataTableType). If MetaDataTableType
     * exist, it's an update of existing metadata else it's a new metadata.
     * 
     * @param managerConnection
     * 
     * @param ISelection
     */
    public DatabaseTableWizardPage(MetadataTable metadataTable, ManagerConnection managerConnection,
            ConnectionItem connectionItem, boolean isRepositoryObjectEditable, IMetadataConnection metadataConnection,
            DatabaseConnection temConnection) {
        super("wizardPage"); //$NON-NLS-1$
        this.metadataTable = metadataTable;
        this.managerConnection = managerConnection;
        this.connectionItem = connectionItem;
        this.isRepositoryObjectEditable = isRepositoryObjectEditable;
        this.metadataConnection = metadataConnection;
        this.temConnection = temConnection;
    }

    public DatabaseTableWizardPage(ManagerConnection managerConnection, ConnectionItem connectionItem,
            boolean isRepositoryObjectEditable, IMetadataConnection metadataConnection, DatabaseConnection temConnection) {
        super("wizardPage"); //$NON-NLS-1$
        this.metadataTable = null;
        this.managerConnection = managerConnection;
        this.connectionItem = connectionItem;
        this.isRepositoryObjectEditable = isRepositoryObjectEditable;
        this.metadataConnection = metadataConnection;
        this.temConnection = temConnection;
    }

    /**
     * Create the first composite, addComponentsAndControls and initialize TableWizardPage.
     * 
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(final Composite parent) {
        boolean needCheck = true;
        tableForm = new DatabaseTableForm(parent, connectionItem, metadataTable, managerConnection, this, temConnection);
        tableForm.setIMetadataConnection(metadataConnection);
        tableForm.setReadOnly(!isRepositoryObjectEditable);

        AbstractForm.ICheckListener listener = new AbstractForm.ICheckListener() {

            public void checkPerformed(final AbstractForm source) {
                if (source.isStatusOnError()) {
                    DatabaseTableWizardPage.this.setPageComplete(false);
                    setErrorMessage(source.getStatus());
                } else {
                    DatabaseTableWizardPage.this.setPageComplete(isRepositoryObjectEditable);
                    setErrorMessage(null);
                    setMessage(source.getStatus(), source.getStatusLevel());
                }
            }
        };
        tableForm.setListener(listener);
        setControl(tableForm);
    }

    public IWizardPage getPreviousPage() {
        IWizardPage perviousPage = super.getPreviousPage();
        if (perviousPage instanceof SelectorTableWizardPage) {
            List<String> nameList = ((SelectorTableWizardPage) perviousPage).getItemListName();
            if (nameList != null && nameList.isEmpty()) {
                ((SelectorTableWizardPage) perviousPage).initControlData();
            }
        }
        return perviousPage;
    }
}
