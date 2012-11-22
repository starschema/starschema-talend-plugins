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

package org.talend.repository.plsap.extractor.wizard;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.repository.plsap.extractor.SAPExtractorPlugin;

public class PLSAPConnectionWizardPage extends WizardPage {
	private Label sapClientLabel;
	private Text clientField;
	private Label sapHostNameLabel;
	private Text hostField;
	private Group group;
	private Text userField;
	private Text pwdField;
	private Text systemNbrField;
	private Text languageField;
	private Button connectionCheckBtn;

	protected String client;
	protected String host;
	protected String user;
	protected String password;
	protected String systemNumber;
	protected String language;


	/**
	 * SapWizardPage constructor.
	 * 
	 * @param connection
	 * @param isRepositoryObjectEditable
	 * @param existingNames
	 */
	public PLSAPConnectionWizardPage() {
		super("ConnectionWizardPage"); //$NON-NLS-1$

	}


	/**
	 * Create the composites, initialize it and add controls.
	 * 
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(final Composite parent) {
		final Composite pageComposite = new Composite(parent, SWT.NULL);
		pageComposite.setFont(parent.getFont());
		initializeDialogUnits(parent);
		pageComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		pageComposite.setLayout(new FillLayout(SWT.HORIZONTAL));

		group = new Group(pageComposite, SWT.NONE);
		group.setLayout(new GridLayout(2, false));

		sapClientLabel = new Label(group, SWT.NONE);
		sapClientLabel.setFont(parent.getFont());
		sapClientLabel.setText("Client:");

		clientField = new Text(group, SWT.BORDER);
		clientField.setText(client != null ? client : "");
		GridData gd_clientField = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_clientField.widthHint = 402;
		clientField.setLayoutData(gd_clientField);
		clientField.setFont(parent.getFont());

		sapHostNameLabel = new Label(group, SWT.NONE);
		sapHostNameLabel.setFont(parent.getFont());
		sapHostNameLabel.setText("Host:");

		hostField = new Text(group, SWT.BORDER);
		hostField.setText(host != null ? host : "");
		GridData gd_hostField = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_hostField.widthHint = 394;
		hostField.setLayoutData(gd_hostField);
		hostField.setFont(parent.getFont());

		Label sapUserLabel = new Label(group, SWT.NONE);
		sapUserLabel.setText("User:");
		sapUserLabel.setFont(parent.getFont());
		sapUserLabel.setLocation(0, 0);

		userField = new Text(group, SWT.BORDER);
		userField.setText(user != null ? user : "");
		userField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		userField.setFont(parent.getFont());
		userField.setBounds(0, 0, 78, 26);

		Label sapPwdLabel = new Label(group, SWT.NONE);
		sapPwdLabel.setText("Password:");
		sapPwdLabel.setFont(parent.getFont());
		sapPwdLabel.setLocation(0, 0);

		pwdField = new Text(group, SWT.BORDER | SWT.PASSWORD);
		pwdField.setText(password != null ? password : "");
		pwdField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		pwdField.setFont(parent.getFont());
		pwdField.setBounds(0, 0, 78, 26);

		Label sapSystemNbrLabel = new Label(group, SWT.NONE);
		sapSystemNbrLabel.setText("System Number:");
		sapSystemNbrLabel.setFont(parent.getFont());
		sapSystemNbrLabel.setLocation(0, 0);

		systemNbrField = new Text(group, SWT.BORDER);
		systemNbrField.setText(systemNumber != null ? systemNumber : "");
		systemNbrField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		systemNbrField.setFont(parent.getFont());
		systemNbrField.setBounds(0, 0, 78, 26);

		Label sapLanguageLabel = new Label(group, SWT.NONE);
		sapLanguageLabel.setText("Language:");
		sapLanguageLabel.setFont(parent.getFont());
		sapLanguageLabel.setLocation(0, 0);

		languageField = new Text(group, SWT.BORDER);
		languageField.setText(language != null ? language : "");
		languageField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		languageField.setFont(parent.getFont());
		languageField.setBounds(0, 0, 78, 26);

		connectionCheckBtn = new Button(group, SWT.NONE);
		GridData gd_connectionCheckBtn = new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1);
		gd_connectionCheckBtn.widthHint = 98;
		connectionCheckBtn.setLayoutData(gd_connectionCheckBtn);
		connectionCheckBtn.setText("&Check");
		new Label(group, SWT.NONE);
		new Label(group, SWT.NONE);

		hostField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				host = hostField.getText();
				updateStatus(validatePage());
			}
		});

		clientField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				client = clientField.getText();
				updateStatus(validatePage());
			}
		});

		userField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				user = userField.getText();
				updateStatus(validatePage());
			}
		});

		pwdField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				password = pwdField.getText();
				updateStatus(validatePage());
			}
		});

		systemNbrField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				systemNumber = systemNbrField.getText();
				updateStatus(validatePage());
			}
		});

		languageField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				language = languageField.getText();
				updateStatus(validatePage());
			}
		});

		connectionCheckBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				SAPExtractorPlugin.getDefault().checkSAPConnection(client, language, systemNumber, host, user, password, getShell(), false);
			}


			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(pageComposite);
		updateStatus(validatePage());
	}


	private void updateStatus(boolean isValid) {
		setPageComplete(isValid);
		connectionCheckBtn.setEnabled(isValid);
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

		if (clientField == null || clientField.getText().isEmpty()) {
			setErrorMessage("The Client field should not be empty !");
			return false;
		} else if (hostField == null || hostField.getText().isEmpty()) {
			setErrorMessage("The Host field should not be empty !");
			return false;
		} else if (userField == null || userField.getText().isEmpty()) {
			setErrorMessage("The User field should not be empty !");
			return false;
		} else if (pwdField == null || pwdField.getText().isEmpty()) {
			setErrorMessage("The Password field should not be empty !");
			return false;
		} else if (systemNbrField == null || systemNbrField.getText().isEmpty()) {
			setErrorMessage("The System Number field should not be empty !");
			return false;
		} else if (languageField == null || languageField.getText().isEmpty()) {
			setErrorMessage("The Language field should not be empty !");
			return false;
		}
		return true;
	}
}
