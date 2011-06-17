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
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.commons.ui.swt.formtools.UtilsButton;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.repository.sap.i18n.Messages;

import com.sap.mw.jco.JCO;

public class SapForm extends AbstractSAPForm {
	private LabelledText sapClient;
	private LabelledText sapSystemNumberText;
	private LabelledText sapLanguageText;
	private LabelledText sapUsernameText;
	private LabelledText sapPasswordText;
	private LabelledText sapHostnameText;
	private UtilsButton checkButton;
	private boolean readOnly;

	public SapForm(Composite paramComposite, ConnectionItem paramConnectionItem, String[] paramArrayOfString) {
		super(paramComposite, 0, paramArrayOfString);
		this.connectionItem = paramConnectionItem;
		setConnectionItem(paramConnectionItem);
		setupForm(false);
		layoutForm();
	}

	private void layoutForm() {
		GridLayout localGridLayout = (GridLayout) getLayout();
		localGridLayout.marginHeight = 0;
		setLayout(localGridLayout);
	}

	protected void adaptFormToReadOnly() {
		this.readOnly = isReadOnly();
		this.sapHostnameText.setReadOnly(this.readOnly);
		this.sapUsernameText.setReadOnly(this.readOnly);
		this.sapPasswordText.setReadOnly(this.readOnly);
		this.sapClient.setReadOnly(this.readOnly);
		this.sapSystemNumberText.setReadOnly(this.readOnly);
		this.sapLanguageText.setReadOnly(this.readOnly);
	}

	protected void addFields() {
		Group localGroup = new Group(this, 0);
		GridLayout localGridLayout = new GridLayout();
		localGridLayout.numColumns = 2;
		localGroup.setLayout(localGridLayout);
		GridData localGridData = new GridData(768);
		localGroup.setLayoutData(localGridData);
		this.sapClient = new LabelledText(localGroup, Messages.getString("SapForm.Client"), true);
		this.sapClient.setText("000");
		this.sapHostnameText = new LabelledText(localGroup, Messages.getString("SapForm.Host"), true);
		this.sapUsernameText = new LabelledText(localGroup, Messages.getString("SapForm.User"), true);
		this.sapPasswordText = new LabelledText(localGroup, Messages.getString("SapForm.Password"), 1, 4196352);
		this.sapSystemNumberText = new LabelledText(localGroup, Messages.getString("SapForm.SysNumber"), true);
		this.sapSystemNumberText.setText("00");
		this.sapLanguageText = new LabelledText(localGroup, Messages.getString("SapForm.Language"), true);
		this.sapLanguageText.setText("EN");
		addCheckButton(localGroup);
	}

	private void addCheckButton(Composite paramComposite) {
		Composite localComposite = Form.startNewGridLayout(paramComposite, 1, false, 16777216, 128);
		GridData localGridData = new GridData(768);
		localGridData.horizontalSpan = 2;
		localGridData.horizontalAlignment = 16777216;
		localComposite.setLayoutData(localGridData);
		GridLayout localGridLayout = (GridLayout) localComposite.getLayout();
		localGridLayout.marginHeight = 0;
		localGridLayout.marginTop = 0;
		localGridLayout.marginBottom = 0;
		this.checkButton = new UtilsButton(localComposite, Messages.getString("SapForm.Check"), 100, 30);
		this.checkButton.setEnabled(false);
	}

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

	protected void addUtilsButtonListeners() {
		this.checkButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent paramSelectionEvent) {
				checkSAPConnection();
			}
		});
	}

	private void checkSAPConnection() {

		if (isContextMode()) {
			return;
		}
		ProgressMonitorDialog localProgressMonitorDialog = new ProgressMonitorDialog(getShell());
		try {
			localProgressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor paramIProgressMonitor) throws InvocationTargetException,
						InterruptedException {
					try {
						testSAPConnection();
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								if (!isReadOnly())
									updateStatus(0, null);
								MessageDialog.openInformation(getShell(), Messages
										.getString("SapForm.CheckConnectionTitle"), "\""
										+ connectionItem.getProperty().getLabel() + "\" "
										+ Messages.getString("SapForm.CheckIsDone"));
								if ((isReadOnly()) && (isContextMode()))
									adaptFormToEditable();
							}
						});
					} catch (Throwable localThrowable) {
						openErrorDialogWithDetail(localThrowable);
					}
				}

			});
		} catch (InvocationTargetException localInvocationTargetException) {
			ExceptionHandler.process(localInvocationTargetException);
		} catch (InterruptedException localInterruptedException) {
			ExceptionHandler.process(localInterruptedException);
		}
	}

	private void testSAPConnection() throws Throwable {
		JCO.Client localClient = null;
		try {
			localClient = JCO.createClient(getConnection().getClient(), getConnection().getUsername(), getConnection()
					.getPassword(), getConnection().getLanguage(), getConnection().getHost(), getConnection()
					.getSystemNumber());
			localClient.connect();
		} catch (Exception localException) {
			throw localException;
		} finally {
			if (localClient != null)
				localClient.disconnect();
		}
	}

	protected boolean checkFieldsValue() {
		if (isContextMode())
			return true;
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
			updateStatus(2, Messages.getString("SapForm.Alert",
					new Object[] { this.sapSystemNumberText.getLabelText() }));
			return false;
		}
		updateCheckButton();
		updateStatus(0, null);
		return true;
	}

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

	protected void initialize() {
		this.sapHostnameText.setText(getConnection().getHost());
		this.sapUsernameText.setText(getConnection().getUsername());
		this.sapPasswordText.setText(getConnection().getPassword());
		this.sapClient.setText(getConnection().getClient());
		this.sapSystemNumberText.setText(getConnection().getSystemNumber());
		this.sapLanguageText.setText(getConnection().getLanguage());
		updateStatus(0, "");
	}

	public void setVisible(boolean paramBoolean) {
		super.setVisible(paramBoolean);
		updateCheckButton();
		if (isContextMode())
			adaptFormToEditable();
		if (isReadOnly() != this.readOnly)
			adaptFormToReadOnly();
	}
}