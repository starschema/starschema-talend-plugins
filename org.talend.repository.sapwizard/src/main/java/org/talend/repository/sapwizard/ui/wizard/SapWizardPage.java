package org.talend.repository.sapwizard.ui.wizard;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.repository.ui.swt.utils.AbstractForm;

/**
 * DatabaseWizard present the DatabaseForm. Use to Use to manage the metadata
 * connection. Page allows setting a database.
 * 
 * @author Ammu
 */
public class SapWizardPage extends WizardPage {

	private ConnectionItem connectionItem;
	private SapForm sapForm;
	private final String[] existingNames;
	private final boolean isRepositoryObjectEditable;

	/**
	 * DatabaseWizardPage constructor.
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
//		boolean isCreation = false;
//		if (this.getWizard() instanceof RepositoryWizard) {
//			isCreation = ((RepositoryWizard) getWizard()).isCreation();
//		}
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
