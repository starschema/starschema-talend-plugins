package org.talend.repository.sapwizard.ui.wizard;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.core.model.metadata.builder.connection.SAPConnection;
import org.talend.repository.sap.i18n.Messages;
import org.talend.repository.sapwizard.SAPPlugin;
import org.talend.repository.ui.swt.utils.AbstractForm;

/**
 * @author Ammu
 *
 */
public abstract class AbstractSAPForm extends AbstractForm {
	protected AbstractSAPForm(Composite paramComposite, int paramInt, String[] paramArrayOfString) {
		super(paramComposite, paramInt, paramArrayOfString);
	}

	protected SAPConnection getConnection() {
		if (this.connectionItem == null)
			return null;
		return (SAPConnection) this.connectionItem.getConnection();
	}

	protected void openErrorDialogWithDetail(Throwable paramThrowable) {
		String errorMsg = paramThrowable.getMessage();
		if ((paramThrowable instanceof Error))
			errorMsg = errorMsg + System.getProperty("line.separator")
					+ Messages.getString("SapForm.Check.Miss.NativeLibrary");
		final String errorDisplay = errorMsg;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				String statusLabelText = Messages.getString("SapForm.CheckFailure") + " "
						+ Messages.getString("SapForm.CheckFailureTip");
				if (!isReadOnly()) {
					updateStatus(2, statusLabelText);
				}
				new ErrorDialogWidthDetailArea(getShell(), SAPPlugin.PLUGIN_ID, statusLabelText, errorDisplay);
			}
		});
	}
}