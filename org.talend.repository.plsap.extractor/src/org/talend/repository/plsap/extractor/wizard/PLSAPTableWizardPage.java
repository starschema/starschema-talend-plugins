
package org.talend.repository.plsap.extractor.wizard;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PLSAPTableWizardPage extends WizardPage {

	private static final int TEXT_WIDTH = 250;
	private Label nameLabel;
	private Text nameField;
	private Label outputFolderLabel;
	private Text outputFolderField;

	protected String tableName;
	protected String outputFolder;
	private Button btnNewButton;


	public PLSAPTableWizardPage() {
		super("TableWizard");
		setPageComplete(false);
	}


	@Override
	public void createControl(Composite parent) {
		final Composite pageComposite = new Composite(parent, SWT.NULL);
		pageComposite.setFont(parent.getFont());
		initializeDialogUnits(parent);

		pageComposite.setLayout(new GridLayout());
		pageComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Composite dataComposite = new Composite(pageComposite, SWT.NONE);
		dataComposite.setLayout(new GridLayout(3, false));
		dataComposite.setLayoutData(new GridData(SWT.FILL, GridData.CENTER, true, false));

		nameLabel = new Label(dataComposite, SWT.NONE);
		nameLabel.setFont(parent.getFont());
		nameLabel.setText("Table Name:");

		nameField = new Text(dataComposite, SWT.BORDER);
		GridData gd_nameField = new GridData(TEXT_WIDTH, SWT.DEFAULT);
		gd_nameField.horizontalSpan = 2;
		gd_nameField.horizontalAlignment = SWT.FILL;
		gd_nameField.grabExcessHorizontalSpace = true;
		nameField.setLayoutData(gd_nameField);
		nameField.setFont(parent.getFont());
		nameField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				tableName = nameField.getText();
				setPageComplete(validatePage());
			}
		});

		outputFolderLabel = new Label(dataComposite, SWT.NONE);
		outputFolderLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		outputFolderLabel.setFont(parent.getFont());
		outputFolderLabel.setText("Table Output Folder:");

		outputFolderField = new Text(dataComposite, SWT.BORDER);
		outputFolderField.setText(outputFolder != null ? outputFolder : "");
		outputFolderField.setFont(parent.getFont());
		outputFolderField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		outputFolderField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				outputFolder = outputFolderField.getText();
				setPageComplete(validatePage());
			}
		});

		btnNewButton = new Button(dataComposite, SWT.NONE);
		btnNewButton.setText("...");
		btnNewButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(PLSAPTableWizardPage.this.getShell());
				String folder = dialog.open();
				if (folder != null) {
					outputFolderField.setText(folder);
				}

			}


			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(pageComposite);

		setPageComplete(validatePage());
	}


	/**
	 * Check if the informations contained in the page are OK
	 * 
	 * @return true is OK
	 */
	boolean validatePage() {
		// erase all previous messages
		setMessage(null);
		setErrorMessage(null);

		if (nameField == null || nameField.getText().isEmpty()) {
			setErrorMessage("The Table name field should not be empty !");
			return false;
		} else if (outputFolderField == null || outputFolderField.getText().isEmpty()) {
			setErrorMessage("The output folder field should not be empty !");
			return false;
		} else {
			File outputFile = new File(outputFolder + File.separator + tableName + ".xml");
			if (outputFile.exists()) {
				setErrorMessage(tableName + ".xml" + " already exists in folder " + outputFolder + " !");
				return false;
			}
		}
		return true;
	}

}
