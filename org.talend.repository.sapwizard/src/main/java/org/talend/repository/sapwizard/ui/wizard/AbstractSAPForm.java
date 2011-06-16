package org.talend.repository.sapwizard.ui.wizard;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.core.model.metadata.builder.connection.SAPConnection;
import org.talend.repository.sap.i18n.Messages;
import org.talend.repository.ui.swt.utils.AbstractForm;

public abstract class AbstractSAPForm extends AbstractForm {
	protected AbstractSAPForm(Composite paramComposite, int paramInt, String[] paramArrayOfString) {
		super(paramComposite, paramInt, paramArrayOfString);
	}

	protected SAPConnection getConnection() {
		if (this.connectionItem == null)
			return null;
		return (SAPConnection) this.connectionItem.getConnection();
	}

}