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

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.repository.ui.swt.utils.AbstractForm;

/**
 * SapWizard present the SapForm. Use to Use to manage the metadata connection.
 * Page allows setting a sap server.
 * 
 * @author Ammu
 */
public class SapWizardPage extends WizardPage {

	/**
	 * connectionItem
	 */
	private ConnectionItem connectionItem;
	/**
	 * sapForm
	 */
	private SapForm sapForm;
	/**
	 * existingNames
	 */
	private final String[] existingNames;
	/**
	 * isRepositoryObjectEditable
	 */
	private final boolean isRepositoryObjectEditable;

	/**
	 * SapWizardPage constructor.
	 * 
	 * @param connection
	 * @param isRepositoryObjectEditable
	 * @param existingNames
	 */
	public SapWizardPage(ConnectionItem connectionItem, boolean isRepositoryObjectEditable, String[] existingNames) {
		super("wizardPage"); //$NON-NLS-1$
		this.connectionItem = connectionItem;
		this.existingNames = existingNames;
		this.isRepositoryObjectEditable = isRepositoryObjectEditable;
	}

	/**
	 * Create the composites, initialize it and add controls.
	 * 
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(final Composite parent) {
		sapForm = new SapForm(parent, connectionItem, existingNames);
		sapForm.setReadOnly(!isRepositoryObjectEditable);

		AbstractForm.ICheckListener listener = new AbstractForm.ICheckListener() {

			public void checkPerformed(final AbstractForm source) {
				if (source.isStatusOnError()) {
					SapWizardPage.this.setPageComplete(false);
					setErrorMessage(source.getStatus());
				} else {
					SapWizardPage.this.setPageComplete(isRepositoryObjectEditable);
					setErrorMessage(null);
					setMessage(source.getStatus(), source.getStatusLevel());
				}
			}
		};
		sapForm.setListener(listener);
		setControl(sapForm);
		if (connectionItem.getProperty().getLabel() != null && !connectionItem.getProperty().getLabel().equals("")) { //$NON-NLS-1$
			sapForm.checkFieldsValue();
		}
	}
}
