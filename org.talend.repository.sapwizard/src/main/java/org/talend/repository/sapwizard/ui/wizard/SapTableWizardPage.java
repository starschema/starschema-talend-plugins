package org.talend.repository.sapwizard.ui.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.designer.business.model.business.SAPFunction;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.swt.utils.AbstractForm;
import org.talend.repository.ui.swt.utils.AbstractForm.ICheckListener;

public class SapTableWizardPage extends WizardPage {

	private SapTableForm sapTableForm;
	private ConnectionItem connectionItem;
	private MetadataTable metadataTable;
	private RepositoryNode node;
	private boolean isRepositoryObjectEditable;
	private SAPFunctionUnit functionUnit;


	protected SapTableWizardPage(RepositoryNode paramRepositoryNode, ConnectionItem paramConnectionItem,SAPFunctionUnit functionUnit,
			MetadataTable paramMetadataTable, boolean paramBoolean) {
		super("");
		this.node = paramRepositoryNode;
		this.connectionItem = paramConnectionItem;
		this.metadataTable = paramMetadataTable;
		this.functionUnit=functionUnit;
		this.isRepositoryObjectEditable = paramBoolean;
	}

	public void createControl(Composite paramComposite) {
		this.sapTableForm = new SapTableForm(paramComposite, this.connectionItem,this.functionUnit, this.metadataTable);
		this.sapTableForm.setReadOnly(!this.isRepositoryObjectEditable);
		if (this.isRepositoryObjectEditable)
			setPageComplete(true);
		ICheckListener local1 = new AbstractForm.ICheckListener() {
			public void checkPerformed(AbstractForm paramAbstractForm) {
				if (paramAbstractForm.isStatusOnError()) {
					SapTableWizardPage.this.setPageComplete(false);
					SapTableWizardPage.this.setErrorMessage(paramAbstractForm.getStatus());
				} else {
					SapTableWizardPage.this.setPageComplete(SapTableWizardPage.this.isRepositoryObjectEditable);
					SapTableWizardPage.this.setErrorMessage(null);
					SapTableWizardPage.this.setMessage(paramAbstractForm.getStatus(), paramAbstractForm
							.getStatusLevel());
				}
			}
		};
		this.sapTableForm.setListener(local1);
		setControl(this.sapTableForm);
	}

	public SAPFunctionUnit getFunctionUnit() {
		return sapTableForm.getFunctionUnit();
	}
	public void performCancel() {
		if (this.sapTableForm != null)
			this.sapTableForm.performCancel();
	}
}