package org.talend.repository.sapwizard.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.ui.images.ECoreImage;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.sap.i18n.Messages;
import org.talend.repository.ui.utils.ConnectionContextHelper;
import org.talend.repository.ui.wizards.PropertiesWizardPage;
import org.talend.repository.ui.wizards.RepositoryWizard;

public class SapWizard extends RepositoryWizard implements INewWizard {

	private PropertiesWizardPage propertiesWizardPage;

	private MDMConnection connection;

	private Property connectionProperty;

	private ConnectionItem connectionItem;

	private boolean isToolBar;

	private RepositoryNode node;

	private SapWizardPage sapWizardPage;

	/**
	 * @param workbench
	 * @param creation
	 * @param node
	 * @param existingNames
	 */
	public SapWizard(IWorkbench workbench, boolean creation,
			RepositoryNode node, String[] existingNames) {
		super(workbench, creation);
		this.existingNames = existingNames;
		this.node = node;
		setNeedsProgressMonitor(true);
		switch (node.getType()) {
		case SIMPLE_FOLDER:
		case REPOSITORY_ELEMENT:
			pathToSave = RepositoryNodeUtilities.getPath(node);
			break;
		case SYSTEM_FOLDER:
			pathToSave = new Path(""); //$NON-NLS-1$
			break;
		}

		switch (node.getType()) {
		case SIMPLE_FOLDER:
		case SYSTEM_FOLDER:
			connection = ConnectionFactory.eINSTANCE.createMDMConnection();
			connectionProperty = PropertiesFactory.eINSTANCE.createProperty();
			connectionProperty.setAuthor(((RepositoryContext) CorePlugin
					.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
					.getUser());
			connectionProperty.setVersion(VersionUtils.DEFAULT_VERSION);
			connectionProperty.setStatusCode(""); //$NON-NLS-1$

			connectionItem = PropertiesFactory.eINSTANCE
					.createMDMConnectionItem();
			connectionItem.setProperty(connectionProperty);
			connectionItem.setConnection(connection);
			break;

		case REPOSITORY_ELEMENT:
			connection = (MDMConnection) ((ConnectionItem) node.getObject()
					.getProperty().getItem()).getConnection();
			connectionProperty = node.getObject().getProperty();
			connectionItem = (ConnectionItem) node.getObject().getProperty()
					.getItem();
			// set the repositoryObject, lock and set isRepositoryObjectEditable
			setRepositoryObject(node.getObject());
			isRepositoryObjectEditable();
			initLockStrategy();
			break;
		}
		// initialize the context mode
		ConnectionContextHelper.checkContextMode(connectionItem);
	}

	public SapWizard(IWorkbench workbench, boolean creation,
			ISelection selection, String[] existingNames) {
		super(workbench, creation);
		this.selection = selection;
		this.existingNames = existingNames;
		setNeedsProgressMonitor(true);

		Object obj = ((IStructuredSelection) selection).getFirstElement();
		node = (RepositoryNode) obj;
		switch (node.getType()) {
		case SIMPLE_FOLDER:
		case REPOSITORY_ELEMENT:
			pathToSave = RepositoryNodeUtilities.getPath(node);
			break;
		case SYSTEM_FOLDER:
			pathToSave = new Path(""); //$NON-NLS-1$
			break;
		}

		switch (node.getType()) {
		case SIMPLE_FOLDER:
		case SYSTEM_FOLDER:
			connection = ConnectionFactory.eINSTANCE.createMDMConnection();
			connectionProperty = PropertiesFactory.eINSTANCE.createProperty();
			connectionProperty.setAuthor(((RepositoryContext) CorePlugin
					.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
					.getUser());
			connectionProperty.setVersion(VersionUtils.DEFAULT_VERSION);
			connectionProperty.setStatusCode(""); //$NON-NLS-1$

			connectionItem = PropertiesFactory.eINSTANCE
					.createMDMConnectionItem();
			connectionItem.setProperty(connectionProperty);
			connectionItem.setConnection(connection);
			break;

		case REPOSITORY_ELEMENT:
			connection = (MDMConnection) ((ConnectionItem) node.getObject()
					.getProperty().getItem()).getConnection();
			connectionProperty = node.getObject().getProperty();
			connectionItem = (ConnectionItem) node.getObject().getProperty()
					.getItem();
			// set the repositoryObject, lock and set isRepositoryObjectEditable
			setRepositoryObject(node.getObject());
			isRepositoryObjectEditable();
			initLockStrategy();
			break;
		}
		// initialize the context mode
		ConnectionContextHelper.checkContextMode(connectionItem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		// if (mdmWizardPage.isPageComplete()) {
		IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
		try {
			if (creation) {
				String nextId = factory.getNextId();
				connectionProperty.setId(nextId);
				factory.create(connectionItem, propertiesWizardPage
						.getDestinationPath());
			} else {
				RepositoryUpdateManager.updateFileConnection(connectionItem);
				factory.save(connectionItem);
				closeLockStrategy();
			}
		} catch (PersistenceException e) {
			String detailError = e.toString();
			new ErrorDialogWidthDetailArea(getShell(), PID, "", //$NON-NLS-1$
					detailError);
			//                log.error(Messages.getString("CommonWizard.persistenceException") + "\n" + detailError); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		List<IRepositoryViewObject> list = new ArrayList<IRepositoryViewObject>();
		list.add(repositoryObject);
		// RepositoryPlugin.getDefault().getRepositoryService()
		// .notifySQLBuilder(list);
		return true;
		// } else {
		// return false;
		// }
	}

	@Override
	public void createPageControls(Composite pageContainer) {
		// do nothing to create pages lazily
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addPages() {
		setWindowTitle(Messages.getString("SapWizard.Window.Title"));//$NON-NLS-1$
        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.METADATA_CONNECTION_WIZ));

        if (isToolBar) {
			pathToSave = null;
		}
		propertiesWizardPage = new SapStep0WizardPage(connectionProperty,pathToSave, ERepositoryObjectType.METADATA_MDMCONNECTION,
				!isRepositoryObjectEditable(), creation);
		sapWizardPage = new SapWizardPage(connectionItem,isRepositoryObjectEditable(), existingNames);
		if(creation){
			propertiesWizardPage.setTitle(Messages.getString("SapWizardPage.TitleCreate.Step1")); //$NON-NLS-1$
			propertiesWizardPage.setDescription(Messages.getString("SapWizardPage.descriptionCreate.Step1")); //$NON-NLS-1$
			propertiesWizardPage.setPageComplete(false);

			sapWizardPage.setTitle(Messages.getString("SapWizardPage.TitleCreate.Step2")); //$NON-NLS-1$
			sapWizardPage.setDescription(Messages.getString("SapWizardPage.descriptionCreate.Step2")); //$NON-NLS-1$
			sapWizardPage.setPageComplete(false);

		}else{
			propertiesWizardPage.setTitle(Messages.getString("SapWizardPage.TitleUpdate.Step1")); //$NON-NLS-1$
			propertiesWizardPage.setDescription(Messages.getString("SapWizardPage.descriptionUpdate.Step1")); //$NON-NLS-1$
			propertiesWizardPage.setPageComplete(isRepositoryObjectEditable());
			
			sapWizardPage.setTitle(Messages.getString("SapWizardPage.TitleUpdate.Step2")); //$NON-NLS-1$
			sapWizardPage.setDescription(Messages.getString("SapWizardPage.descriptionUpdate.Step2")); //$NON-NLS-1$
			sapWizardPage.setPageComplete(isRepositoryObjectEditable());
		}
		addPage(propertiesWizardPage);
		addPage(sapWizardPage);
	}

	public void setToolBar(boolean isToolbar) {
		this.isToolBar = isToolbar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.talend.repository.ui.wizards.RepositoryWizard#getConnectionItem()
	 */
	@Override
	public ConnectionItem getConnectionItem() {
		return this.connectionItem;
	}

	@Override
	public boolean canFinish() {
		return sapWizardPage.isPageComplete();
	}

}
