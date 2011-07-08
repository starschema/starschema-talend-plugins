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

package org.talend.repository.sapwizard.ui.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.commons.ui.swt.formtools.UtilsButton;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.repository.sap.i18n.Messages;
import org.talend.repository.sapwizard.service.SapUtil;

/**
 * @author Ammu
 * 
 */
public class SapForm extends BaseSAPForm {

	/**
	 * sapClient
	 */
	private LabelledText sapClient;
	/**
	 * sapSystemNumberText
	 */
	private LabelledText sapSystemNumberText;
	/**
	 * sapLanguageText
	 */
	private LabelledText sapLanguageText;
	/**
	 * sapUsernameText
	 */
	private LabelledText sapUsernameText;
	/**
	 * sapPasswordText
	 */
	private LabelledText sapPasswordText;
	/**
	 * sapHostnameText
	 */
	private LabelledText sapHostnameText;
	/**
	 * checkButton
	 */
	private UtilsButton checkButton;
	/**
	 * readOnly
	 */
	private boolean readOnly;

	/**
	 * @param composite
	 * @param connectionItem
	 * @param existingNames
	 */
	public SapForm(Composite composite, ConnectionItem connectionItem, String[] existingNames) {
		super(composite, 0, existingNames);
		this.connectionItem = connectionItem;
		setConnectionItem(connectionItem);
		setupForm(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.talend.repository.ui.swt.utils.AbstractForm#adaptFormToReadOnly()
	 */
	protected void adaptFormToReadOnly() {
		this.readOnly = isReadOnly();
		this.sapHostnameText.setReadOnly(this.readOnly);
		this.sapUsernameText.setReadOnly(this.readOnly);
		this.sapPasswordText.setReadOnly(this.readOnly);
		this.sapClient.setReadOnly(this.readOnly);
		this.sapSystemNumberText.setReadOnly(this.readOnly);
		this.sapLanguageText.setReadOnly(this.readOnly);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.talend.repository.ui.swt.utils.AbstractForm#addFields()
	 */
	protected void addFields() {
		Group group = new Group(this, 0);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		group.setLayout(gridLayout);
		GridData gridData = new GridData(768);
		group.setLayoutData(gridData);

		this.sapClient = new LabelledText(group, Messages.getString("SapForm.Client"), true);
		this.sapClient.setText("000");
		this.sapHostnameText = new LabelledText(group, Messages.getString("SapForm.Host"), true);
		this.sapUsernameText = new LabelledText(group, Messages.getString("SapForm.User"), true);
		this.sapPasswordText = new LabelledText(group, Messages.getString("SapForm.Password"), 1, 4196352);
		this.sapSystemNumberText = new LabelledText(group, Messages.getString("SapForm.SysNumber"), true);
		this.sapSystemNumberText.setText("00");
		this.sapLanguageText = new LabelledText(group, Messages.getString("SapForm.Language"), true);
		this.sapLanguageText.setText("EN");
		addCheckButton(group);
	}

	/**
	 * @param parent
	 */
	private void addCheckButton(Composite parent) {
		Composite composite = Form.startNewGridLayout(parent, 1, false, 16777216, 128);
		GridData gridData = new GridData(768);
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = 16777216;
		composite.setLayoutData(gridData);
		GridLayout gridLayout = (GridLayout) composite.getLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginTop = 0;
		gridLayout.marginBottom = 0;
		this.checkButton = new UtilsButton(composite, Messages.getString("SapForm.Check"), 100, 30);
		this.checkButton.setEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.talend.repository.ui.swt.utils.AbstractForm#addFieldsListeners()
	 */
	protected void addFieldsListeners() {
		this.sapHostnameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent paramModifyEvent) {
				if ((!isContextMode()) && (sapHostnameText.getEditable())) {
					getConnection().setHost(sapHostnameText.getText());
					checkFieldsValue();
				}
			}
		});
		this.sapUsernameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent paramModifyEvent) {
				if ((!isContextMode()) && (sapUsernameText.getEditable())) {
					getConnection().setUsername(sapUsernameText.getText());
					checkFieldsValue();
				}
			}
		});
		this.sapPasswordText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent paramModifyEvent) {
				if ((!isContextMode()) && (sapPasswordText.getEditable())) {
					getConnection().setPassword(sapPasswordText.getText());
					checkFieldsValue();
				}
			}
		});
		this.sapClient.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent paramModifyEvent) {
				if ((!isContextMode()) && (sapClient.getEditable())) {
					getConnection().setClient(sapClient.getText());
					checkFieldsValue();
				}
			}
		});
		this.sapLanguageText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent paramModifyEvent) {
				if ((!isContextMode()) && (sapLanguageText.getEditable())) {
					getConnection().setLanguage(sapLanguageText.getText());
					checkFieldsValue();
				}
			}
		});
		this.sapSystemNumberText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent paramModifyEvent) {
				if ((!isContextMode()) && (sapSystemNumberText.getEditable())) {
					getConnection().setSystemNumber(sapSystemNumberText.getText());
					checkFieldsValue();
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.talend.repository.ui.swt.utils.AbstractForm#addUtilsButtonListeners()
	 */
	protected void addUtilsButtonListeners() {
		this.checkButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent paramSelectionEvent) {
				checkSAPConnection();
			}
		});
	}

	/**
	 * 
	 */
	private void checkSAPConnection() {

		if (isContextMode()) {
			return;
		}
		ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(getShell());
		try {
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
					try {
						SapUtil.connectSAPserver(getConnection().getClient(), getConnection().getLanguage(), getConnection().getSystemNumber(),
								getConnection().getHost(), getConnection().getUsername(), getConnection().getPassword());
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								if (!isReadOnly()) {
									updateStatus(0, null);
								}
								MessageDialog.openInformation(getShell(), Messages.getString("SapForm.CheckConnectionTitle"), "\""
										+ connectionItem.getProperty().getLabel() + "\" " + Messages.getString("SapForm.CheckIsDone"));
								if ((isReadOnly()) && (isContextMode())) {
									adaptFormToEditable();
								}
							}
						});
					} catch (Throwable throwable) {
						openErrorDialogWithDetail(throwable);
					}
				}

			});
		} catch (InvocationTargetException invocationTargetException) {
			ExceptionHandler.process(invocationTargetException);
		} catch (InterruptedException interruptedException) {
			ExceptionHandler.process(interruptedException);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.talend.repository.ui.swt.utils.AbstractForm#checkFieldsValue()
	 */
	protected boolean checkFieldsValue() {
		if (isContextMode()) {
			return true;
		}
		if (this.sapHostnameText.getCharCount() == 0) {
			updateStatus(2, Messages.getString("SapForm.Alert", new Object[] { this.sapHostnameText.getLabelText() }));
			return false;
		}
		if (this.sapUsernameText.getCharCount() == 0) {
			updateStatus(2, Messages.getString("SapForm.Alert", new Object[] { this.sapUsernameText.getLabelText() }));
			return false;
		}
		if (this.sapPasswordText.getCharCount() == 0) {
			updateStatus(2, Messages.getString("SapForm.Alert", new Object[] { this.sapPasswordText.getLabelText() }));
			return false;
		}
		if (this.sapClient.getCharCount() == 0) {
			updateStatus(2, Messages.getString("SapForm.Alert", new Object[] { this.sapClient.getLabelText() }));
			return false;
		}
		if (this.sapLanguageText.getCharCount() == 0) {
			updateStatus(2, Messages.getString("SapForm.Alert", new Object[] { this.sapLanguageText.getLabelText() }));
			return false;
		}
		if (this.sapSystemNumberText.getCharCount() == 0) {
			updateStatus(2, Messages.getString("SapForm.Alert", new Object[] { this.sapSystemNumberText.getLabelText() }));
			return false;
		}
		updateCheckButton();
		updateStatus(0, null);
		return true;
	}

	/**
	 * 
	 */
	private void updateCheckButton() {
		if (isContextMode()) {
			this.checkButton.setEnabled(true);
		} else {
			boolean bool = (this.sapHostnameText.getCharCount() != 0) && (this.sapUsernameText.getCharCount() != 0)
					&& (this.sapPasswordText.getCharCount() != 0) && (this.sapClient.getCharCount() != 0)
					&& (this.sapSystemNumberText.getCharCount() != 0) && (this.sapLanguageText.getCharCount() != 0);
			this.checkButton.setEnabled(bool);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.talend.repository.ui.swt.utils.AbstractForm#initialize()
	 */
	protected void initialize() {
		this.sapHostnameText.setText(getConnection().getHost());
		this.sapUsernameText.setText(getConnection().getUsername());
		this.sapPasswordText.setText(getConnection().getPassword());
		this.sapClient.setText(getConnection().getClient());
		this.sapSystemNumberText.setText(getConnection().getSystemNumber());
		this.sapLanguageText.setText(getConnection().getLanguage());
		updateStatus(0, "");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Control#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		updateCheckButton();
		if (isContextMode()) {
			adaptFormToEditable();
		}
		if (isReadOnly() != this.readOnly) {
			adaptFormToReadOnly();
		}
	}
}