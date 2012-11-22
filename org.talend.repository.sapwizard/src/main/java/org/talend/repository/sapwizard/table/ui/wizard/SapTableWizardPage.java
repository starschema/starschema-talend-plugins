/** 
 *    Copyright (C) 2011, Starschema Ltd. <info at starschema.net>
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 2 of the License, or
 *    any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package org.talend.repository.sapwizard.table.ui.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.repository.ui.swt.utils.AbstractForm;
import org.talend.repository.ui.swt.utils.AbstractForm.ICheckListener;

/**
 * @author Ammu
 * 
 */
public class SapTableWizardPage extends WizardPage {

	/**
	 * sapTableForm
	 */
	private SapTableForm sapTableForm;
	/**
	 * connectionItem
	 */
	private ConnectionItem connectionItem;
	/**
	 * metadataTable
	 */
	private MetadataTable metadataTable;
	/**
	 * isRepositoryObjectEditable
	 */
	private boolean isRepositoryObjectEditable;
	/**
	 * functionUnit
	 */
	private SAPFunctionUnit functionUnit;

	/**
	 * @param connectionItem
	 * @param functionUnit
	 * @param metadataTable
	 * @param creation
	 */
	protected SapTableWizardPage(ConnectionItem connectionItem, SAPFunctionUnit functionUnit,
			MetadataTable metadataTable, boolean creation) {
		super("wizardPage");//$NON-NLS-1$
		this.connectionItem = connectionItem;
		this.metadataTable = metadataTable;
		this.functionUnit = functionUnit;
		this.isRepositoryObjectEditable = creation;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite composite) {
		this.sapTableForm = new SapTableForm(composite, this.connectionItem, this.functionUnit, this.metadataTable);
		this.sapTableForm.setReadOnly(!this.isRepositoryObjectEditable);
		ICheckListener listener = new AbstractForm.ICheckListener() {
			public void checkPerformed(AbstractForm abstractForm) {
				if (abstractForm.isStatusOnError() || sapTableForm.getFunctionUnit() == null) {
					SapTableWizardPage.this.setPageComplete(false);
					SapTableWizardPage.this.setErrorMessage(abstractForm.getStatus());
				} else {
					SapTableWizardPage.this.setPageComplete(isRepositoryObjectEditable);
					SapTableWizardPage.this.setErrorMessage(null);
					SapTableWizardPage.this.setMessage(abstractForm.getStatus(), abstractForm
							.getStatusLevel());
				}
			}
		};
		this.sapTableForm.setListener(listener);
		setControl(this.sapTableForm);
		if (connectionItem.getProperty().getLabel() != null && !connectionItem.getProperty().getLabel().equals("")) { //$NON-NLS-1$
			sapTableForm.checkFieldsValue();
		}

	}

	/**
	 * @return
	 */
	public SAPFunctionUnit getFunctionUnit() {
		return sapTableForm.getFunctionUnit();
	}

	/**
	 * 
	 */
	public void performCancel() {
		if (this.sapTableForm != null)
			this.sapTableForm.performCancel();
	}
}