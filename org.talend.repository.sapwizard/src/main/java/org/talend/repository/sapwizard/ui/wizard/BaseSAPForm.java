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
public abstract class BaseSAPForm extends AbstractForm {

	/**
	 * @param parent
	 * @param style
	 * @param existingNames
	 */
	protected BaseSAPForm(Composite parent, int style, String[] existingNames) {
		super(parent, style, existingNames);
	}

	/**
	 * @return
	 */
	protected SAPConnection getConnection() {
		if (this.connectionItem == null)
			return null;
		return (SAPConnection) this.connectionItem.getConnection();
	}

	/**
	 * @param exception
	 */
	protected void openErrorDialogWithDetail(Throwable exception) {
		String errorMsg = exception.getMessage();
		if ((exception instanceof Error))
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