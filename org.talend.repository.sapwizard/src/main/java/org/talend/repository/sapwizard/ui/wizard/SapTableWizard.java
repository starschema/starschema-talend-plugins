/**
 * 
 */
package org.talend.repository.sapwizard.ui.wizard;

import java.util.Map;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.SAPConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.ui.images.ECoreImage;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.sap.i18n.Messages;
import org.talend.repository.ui.wizards.CheckLastVersionRepositoryWizard;

/**
 * @author Ammu
 * 
 */
public class SapTableWizard extends CheckLastVersionRepositoryWizard implements INewWizard {

	private final ConnectionItem connectionItem;
	private final SAPFunctionUnit functionUnit;
	private SapStep0WizardPage propertiesWizardPage;
	private Property connectionProperty;
	private boolean creation;
	private SapTableWizardPage sapTablePage;
	private RepositoryNode node;

	public SapTableWizard(IWorkbench workbench, RepositoryNode repositoryNode, SAPConnectionItem sapConnectioItem,
			SAPFunctionUnit sapFunctionUnit, MetadataTable metadataTable, String[] existingNames, boolean creation) {
		super(workbench, creation);
		this.connectionItem = sapConnectioItem;
		this.functionUnit = sapFunctionUnit;
		this.existingNames = existingNames;
		this.node = repositoryNode;
		this.metadataTable = metadataTable;
		this.creation = creation;
		setRepositoryObject(repositoryNode.getObject());

		if (creation) {
			this.pathToSave = new Path("");
			this.connectionProperty = PropertiesFactory.eINSTANCE.createProperty();
			this.connectionProperty.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(
					Context.REPOSITORY_CONTEXT_KEY)).getUser());
			this.connectionProperty.setVersion(VersionUtils.DEFAULT_VERSION);
			this.connectionProperty.setStatusCode(""); //$NON-NLS-1$
			this.connectionItem.setProperty(connectionProperty);
		} else {
			pathToSave = RepositoryNodeUtilities.getPath(repositoryNode);
			this.connectionProperty = repositoryNode.getObject().getProperty();
			// this.connectionItem.setProperty(connectionProperty);

		}
		setNeedsProgressMonitor(true);
		isRepositoryObjectEditable();
		initLockStrategy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	public void addPages() {
		setWindowTitle(Messages.getString("SapTableWizard.WindowTitle"));
		setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.METADATA_TABLE_WIZ));
		propertiesWizardPage = new SapStep0WizardPage(connectionProperty, pathToSave,
				ERepositoryObjectType.METADATA_MDMCONNECTION, !isRepositoryObjectEditable(), creation);
		this.sapTablePage = new SapTableWizardPage(this.node, this.connectionItem, functionUnit, this.metadataTable,
				isRepositoryObjectEditable());

		if (creation) {
			propertiesWizardPage.setTitle(Messages.getString("SapTableWizardPage.TitleCreate.Step1")); //$NON-NLS-1$
			propertiesWizardPage.setDescription(Messages.getString("SapTableWizardPage.DescriptionCreate.Step1")); //$NON-NLS-1$
			propertiesWizardPage.setPageComplete(false);
		}
		addPage(this.propertiesWizardPage);
		addPage(this.sapTablePage);
	}

	public boolean performFinish() {
		Map<?, ?> map = sapTablePage.getFunctionUnit().getProperties();
		RepositoryUpdateManager.updateSAPFunction(sapTablePage.getFunctionUnit());

		saveMetaData();
		closeLockStrategy();
		return true;
	}

	private void saveMetaData() {
		ProxyRepositoryFactory localProxyRepositoryFactory = ProxyRepositoryFactory.getInstance();
		try {
			localProxyRepositoryFactory.save(this.repositoryObject.getProperty().getItem());
			RepositoryManager.refresh(ERepositoryObjectType.METADATA_SAP_FUNCTION);

		} catch (PersistenceException localPersistenceException) {
			String str = localPersistenceException.toString();
			new ErrorDialogWidthDetailArea(getShell(), "org.talend.repository", Messages
					.getString("CommonWizard.persistenceException"), str);
			log.error(Messages.getString("CommonWizard.persistenceException") + "\n" + str);
		}
	}
}
