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

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.SAPConnection;
import org.talend.core.model.metadata.designerproperties.SapJcoVersion;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.sap.i18n.Messages;
import org.talend.repository.ui.utils.ConnectionContextHelper;
import org.talend.repository.ui.wizards.CheckLastVersionRepositoryWizard;
import org.talend.repository.ui.wizards.metadata.connection.Step0WizardPage;

/**
 * @author Ammu
 * 
 */
public class SapWizard extends CheckLastVersionRepositoryWizard implements INewWizard {

	/**
	 * propertiesWizardPage
	 */
	private Step0WizardPage propertiesWizardPage;
	/**
	 * connection
	 */
	private SAPConnection connection;
	/**
	 * connectionProperty
	 */
	private Property connectionProperty;

	/**
	 * sapWizardPage
	 */
	private SapWizardPage sapWizardPage;
	/**
	 * isToolBar
	 */
	private boolean isToolBar;

	/**
	 * @param workbench
	 * @param creation
	 * @param node
	 * @param existingNames
	 */
	public SapWizard(IWorkbench workbench, boolean creation, RepositoryNode node, String[] existingNames) {
		super(workbench, creation);
		this.existingNames = existingNames;
		setNeedsProgressMonitor(true);
		init(node);
	}

	private void init(RepositoryNode node) {
		switch (node.getType()) {
		case SIMPLE_FOLDER:
		case REPOSITORY_ELEMENT:
			pathToSave = RepositoryNodeUtilities.getPath(node);
			connection = (SAPConnection) ((ConnectionItem) node.getObject().getProperty().getItem()).getConnection();
			connectionProperty = node.getObject().getProperty();
			connectionItem = (ConnectionItem) node.getObject().getProperty().getItem();
			setRepositoryObject(node.getObject());
			isRepositoryObjectEditable();
			initLockStrategy();
			break;
		case SYSTEM_FOLDER:
			pathToSave = new Path(""); //$NON-NLS-1$
			connection = ConnectionFactory.eINSTANCE.createSAPConnection();
			connection.setJcoVersion(SapJcoVersion.SAP3.getModulName());
			connectionProperty = PropertiesFactory.eINSTANCE.createProperty();
			connectionProperty.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(
					Context.REPOSITORY_CONTEXT_KEY)).getUser());
			connectionProperty.setVersion(VersionUtils.DEFAULT_VERSION);
			connectionProperty.setStatusCode(""); //$NON-NLS-1$

			connectionItem = PropertiesFactory.eINSTANCE.createSAPConnectionItem();
			connectionItem.setProperty(connectionProperty);
			connectionItem.setConnection(connection);
			break;
		}
		ConnectionContextHelper.checkContextMode(connectionItem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		if (this.sapWizardPage.isPageComplete()) {
			ProxyRepositoryFactory proxyRepositoryFactory = ProxyRepositoryFactory.getInstance();
			try {
				if (this.creation) {
					String nextId = proxyRepositoryFactory.getNextId();
					this.connectionProperty.setId(nextId);
					proxyRepositoryFactory.create(this.connectionItem, this.propertiesWizardPage.getDestinationPath(),
							new boolean[0]);
				} else {
					RepositoryUpdateManager.updateFileConnection(this.connectionItem);
					proxyRepositoryFactory.save(this.connectionItem, new boolean[0]);
					closeLockStrategy();
				}
			} catch (PersistenceException persistenceException) {
				String detailError = persistenceException.toString();
				new ErrorDialogWidthDetailArea(getShell(), "org.talend.repository", Messages
						.getString("CommonWizard.persistenceException"), detailError);
				log.error(Messages.getString("CommonWizard.persistenceException") + "\n" + detailError);
				return false;
			}
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.setWorkbench(workbench);
		this.selection = selection;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		setWindowTitle(Messages.getString("SapWizard.Window.Title"));//$NON-NLS-1$
		setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.METADATA_CONNECTION_WIZ));

		if (isToolBar) {
			pathToSave = null;
		}
		propertiesWizardPage = new Step0WizardPage(connectionProperty, pathToSave,
				ERepositoryObjectType.METADATA_CONNECTIONS, !isRepositoryObjectEditable(), creation);
		sapWizardPage = new SapWizardPage(connectionItem, isRepositoryObjectEditable(), existingNames);
		if (creation) {
			propertiesWizardPage.setTitle(Messages.getString("SapWizardPage.TitleCreate.Step1")); //$NON-NLS-1$
			propertiesWizardPage.setDescription(Messages.getString("SapWizardPage.DescriptionCreate.Step1")); //$NON-NLS-1$
			propertiesWizardPage.setPageComplete(false);

			sapWizardPage.setTitle(Messages.getString("SapWizardPage.TitleCreate.Step2")); //$NON-NLS-1$
			sapWizardPage.setDescription(Messages.getString("SapWizardPage.DescriptionCreate.Step2")); //$NON-NLS-1$
			sapWizardPage.setPageComplete(false);

		} else {
			propertiesWizardPage.setTitle(Messages.getString("SapWizardPage.TitleUpdate.Step1")); //$NON-NLS-1$
			propertiesWizardPage.setDescription(Messages.getString("SapWizardPage.DescriptionUpdate.Step1")); //$NON-NLS-1$
			propertiesWizardPage.setPageComplete(isRepositoryObjectEditable());

			sapWizardPage.setTitle(Messages.getString("SapWizardPage.TitleUpdate.Step2")); //$NON-NLS-1$
			sapWizardPage.setDescription(Messages.getString("SapWizardPage.DescriptionUpdate.Step2")); //$NON-NLS-1$
			sapWizardPage.setPageComplete(isRepositoryObjectEditable());
		}
		addPage(propertiesWizardPage);
		addPage(sapWizardPage);
	}

	/**
	 * @param isToolbar
	 */
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

}
