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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
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
import org.talend.repository.ui.utils.ConnectionContextHelper;
import org.talend.repository.ui.wizards.CheckLastVersionRepositoryWizard;
import org.talend.repository.ui.wizards.metadata.connection.Step0WizardPage;

/**
 * @author Ammu
 * 
 */
public class SapTableWizard extends CheckLastVersionRepositoryWizard implements INewWizard {
	/**
	 * functionUnit
	 */
	private SAPFunctionUnit functionUnit;
	/**
	 * propertiesWizardPage
	 */
	private Step0WizardPage propertiesWizardPage;
	/**
	 * connectionProperty
	 */
	private Property connectionProperty;

	/**
	 * sapTablePage
	 */
	private SapTableWizardPage sapTablePage;

	/**
	 * @param workbench
	 * @param repositoryNode
	 * @param sapConnectioItem
	 * @param sapFunctionUnit
	 * @param metadataTable
	 * @param existingNames
	 * @param creation
	 */
	public SapTableWizard(IWorkbench workbench, RepositoryNode repositoryNode, SAPConnectionItem sapConnectioItem,
			SAPFunctionUnit sapFunctionUnit, MetadataTable metadataTable, String[] existingNames, boolean creation) {
		super(workbench, creation);
		this.connectionItem = sapConnectioItem;
		this.functionUnit = sapFunctionUnit;
		this.existingNames = existingNames;
		this.metadataTable = metadataTable;
		this.creation = creation;
		setRepositoryObject(repositoryNode.getObject());

		pathToSave = RepositoryNodeUtilities.getPath(repositoryNode);
		this.connectionProperty = repositoryNode.getObject().getProperty();
		this.connectionItem.setProperty(connectionProperty);

		setNeedsProgressMonitor(true);
		isRepositoryObjectEditable();
		initLockStrategy();
		ConnectionContextHelper.checkContextMode(connectionItem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {
		setWindowTitle(Messages.getString("SapTableWizard.WindowTitle"));
		setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.METADATA_TABLE_WIZ));
		this.propertiesWizardPage = new Step0WizardPage(connectionProperty, pathToSave,
				ERepositoryObjectType.METADATA_MDMCONNECTION, true, creation);
		this.sapTablePage = new SapTableWizardPage(this.connectionItem, functionUnit, this.metadataTable,
				isRepositoryObjectEditable());

		if (creation) {
			propertiesWizardPage.setTitle(Messages.getString("SapTableWizardPage.TitleCreate.Step1")); //$NON-NLS-1$
			propertiesWizardPage.setDescription(Messages.getString("SapTableWizardPage.DescriptionCreate.Step1")); //$NON-NLS-1$
			propertiesWizardPage.setPageComplete(false);

			sapTablePage.setTitle(Messages.getString("SapTableWizardPage.TitleCreate.Step2")); //$NON-NLS-1$
			sapTablePage.setDescription(Messages.getString("SapTableWizardPage.DescriptionCreate.Step2")); //$NON-NLS-1$
			sapTablePage.setPageComplete(false);

		} else {
			propertiesWizardPage.setTitle(Messages.getString("SapTableWizardPage.TitleUpdate.Step1")); //$NON-NLS-1$
			propertiesWizardPage.setDescription(Messages.getString("SapTableWizardPage.DescriptionUpdate.Step1")); //$NON-NLS-1$
			propertiesWizardPage.setPageComplete(isRepositoryObjectEditable());

			sapTablePage.setTitle(Messages.getString("SapTableWizardPage.TitleUpdate.Step2")); //$NON-NLS-1$
			sapTablePage.setDescription(Messages.getString("SapTableWizardPage.DescriptionUpdate.Step2")); //$NON-NLS-1$
			sapTablePage.setPageComplete(isRepositoryObjectEditable());

		}
		addPage(this.propertiesWizardPage);
		addPage(this.sapTablePage);
	}

	/* (non-Javadoc)
	 * @see org.talend.repository.ui.wizards.CheckLastVersionRepositoryWizard#performFinish()
	 */
	public boolean performFinish() {
		if (this.sapTablePage.isPageComplete()) {
			RepositoryUpdateManager.updateSAPFunction(sapTablePage.getFunctionUnit());
			ProxyRepositoryFactory localProxyRepositoryFactory = ProxyRepositoryFactory.getInstance();
			try {
				localProxyRepositoryFactory.save(this.repositoryObject.getProperty().getItem());
				RepositoryManager.refresh(ERepositoryObjectType.METADATA_SAP_FUNCTION);
				closeLockStrategy();
			} catch (PersistenceException persistenceException) {
				String message = persistenceException.toString();
				new ErrorDialogWidthDetailArea(getShell(), "org.talend.repository", Messages
						.getString("CommonWizard.persistenceException"), message);
				log.error(Messages.getString("CommonWizard.persistenceException") + "\n" + message);
			}
		}
		return true;
	}

}
