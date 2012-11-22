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

package org.talend.repository.sapwizard.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.properties.SAPConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.actions.metadata.AbstractCreateAction;
import org.talend.core.ui.images.OverlayImageProvider;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.sap.i18n.Messages;
import org.talend.repository.sapwizard.ui.wizard.SapWizard;

/**
 * @author Ammu
 * 
 */
public class CreateSAPConnectionAction extends AbstractCreateAction {

	private static final String EDIT_LABEL = Messages.getString("CreateSAPConnectionAction.Action.EditTitle"); //$NON-NLS-1$

	private static final String OPEN_LABEL = Messages.getString("CreateSAPConnectionAction.Action.OpenTitle"); //$NON-NLS-1$

	private static final String CREATE_LABEL = Messages.getString("CreateSAPConnectionAction.Action.CreateTitle"); //$NON-NLS-1$

	private ImageDescriptor defaultImage = ImageProvider.getImageDesc(ECoreImage.METADATA_CONNECTION_ICON);

	private ImageDescriptor createImage = OverlayImageProvider.getImageWithNew(ImageProvider.getImage(ECoreImage.METADATA_CONNECTION_ICON));

	/**
	 * 
	 */
	public CreateSAPConnectionAction() {
		super();

		this.setText(CREATE_LABEL);
		this.setToolTipText(CREATE_LABEL);
		this.setImageDescriptor(defaultImage);
	}

	/**
	 * @param isToolbar
	 */
	public CreateSAPConnectionAction(boolean isToolbar) {
		super();
		this.setToolbar(isToolbar);
		this.setText(CREATE_LABEL);
		this.setToolTipText(CREATE_LABEL);
		this.setImageDescriptor(defaultImage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void doRun() {
		if (this.repositoryNode == null) {
			repositoryNode = getCurrentRepositoryNode();
		}
		if (isToolbar()) {
			if (repositoryNode != null && repositoryNode.getContentType() != ERepositoryObjectType.METADATA_CONNECTIONS) {
				repositoryNode = null;
			}
			if (repositoryNode == null) {
				repositoryNode = getRepositoryNodeForDefault(ERepositoryObjectType.METADATA_CONNECTIONS);
			}
		}
		RepositoryManager.getRepositoryView().refresh();
		RepositoryNode metadataNode = repositoryNode.getParent();
		if (metadataNode != null) {
			getViewPart().setFocus();
			getViewPart().expand(metadataNode, true);
			getViewPart().expand(repositoryNode, true);
		}

		if (repositoryNode.getObject() != null && repositoryNode.getObject().getClass().equals(RepositoryObject.class)) {
			try {
				((RepositoryObject) repositoryNode.getObject()).setProperty(ProxyRepositoryFactory.getInstance().getUptodateProperty(
						repositoryNode.getObject().getProperty()));
			} catch (PersistenceException e) {
				ExceptionHandler.process(e);
			}
		}

		RepositoryNode node = repositoryNode;
		if (!isToolbar()) {
			Object userSelection = ((IStructuredSelection) getSelection()).getFirstElement();
			if (userSelection instanceof RepositoryNode) {
				switch (((RepositoryNode) userSelection).getType()) {
				case REPOSITORY_ELEMENT:
				case SIMPLE_FOLDER:
				case SYSTEM_FOLDER:
					node = (RepositoryNode) userSelection;
					break;
				}
			}
		}
		boolean creation = false;
		switch (node.getType()) {
		case REPOSITORY_ELEMENT:
			creation = false;
			break;
		case SIMPLE_FOLDER:
			creation = true;
			break;
		case SYSTEM_FOLDER:
			creation = true;
			break;
		}

		SapWizard repositoryWizard = new SapWizard(PlatformUI.getWorkbench(), creation, node, getExistingNames());
		WizardDialog wizardDialog = new WizardDialog(Display.getCurrent().getActiveShell(), repositoryWizard);
		wizardDialog.setPageSize(600, 500);
		wizardDialog.create();
		wizardDialog.open();
		RepositoryManager.refreshSavedNode(node);
	}

	@Override
	protected void init(RepositoryNode node) {
		ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
		if (!ERepositoryObjectType.METADATA_SAPCONNECTIONS.equals(nodeType)) {
			setEnabled(false);
			return;
		}
		IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
		switch (node.getType()) {
		case SIMPLE_FOLDER:
		case SYSTEM_FOLDER:
			if (factory.isUserReadOnlyOnCurrentProject() || !ProjectManager.getInstance().isInCurrentMainProject(node)) {
				setEnabled(false);
				return;
			}
			if (node.getObject() != null && node.getObject().getProperty().getItem().getState().isDeleted()) {
				setEnabled(false);
				return;
			}
			this.setText(CREATE_LABEL);
			this.setImageDescriptor(createImage);
			collectChildNames(node);
			break;
		case REPOSITORY_ELEMENT:
			if (factory.isPotentiallyEditable(node.getObject()) && isLastVersion(node)) {
				this.setText(EDIT_LABEL);
				this.setImageDescriptor(defaultImage);
				collectSiblingNames(node);
			} else {
				this.setText(OPEN_LABEL);
				this.setImageDescriptor(defaultImage);
			}
			break;
		default:
			return;
		}
		setEnabled(true);
	}

	@Override
	public Class<SAPConnectionItem> getClassForDoubleClick() {
		return SAPConnectionItem.class;
	}

}
