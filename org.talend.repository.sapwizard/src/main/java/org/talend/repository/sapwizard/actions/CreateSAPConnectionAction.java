// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.sapwizard.actions;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.SAPConnection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.SAPConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.ui.images.ECoreImage;
import org.talend.core.ui.images.OverlayImageProvider;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.sap.i18n.Messages;
import org.talend.repository.sapwizard.ui.wizard.SapWizard;
import org.talend.repository.ui.actions.metadata.AbstractCreateAction;

/**
 * @author Ammu
 * 
 */
public class CreateSAPConnectionAction extends AbstractCreateAction {

	protected static Logger log = Logger.getLogger(CreateSAPConnectionAction.class);

	protected static final String PID = RepositoryPlugin.PLUGIN_ID;

	private static final String EDIT_LABEL = Messages.getString("CreateSAPConnectionAction.action.editTitle"); //$NON-NLS-1$

	private static final String OPEN_LABEL = Messages.getString("CreateSAPConnectionAction.action.openTitle"); //$NON-NLS-1$

	private static final String CREATE_LABEL = Messages.getString("CreateSAPConnectionAction.action.createTitle"); //$NON-NLS-1$

	private ImageDescriptor defaultImage = ImageProvider.getImageDesc(ECoreImage.METADATA_CONNECTION_ICON);

	private ImageDescriptor createImage = OverlayImageProvider.getImageWithNew(ImageProvider
			.getImage(ECoreImage.METADATA_CONNECTION_ICON));

	private ConnectionItem connItem;

	public CreateSAPConnectionAction() {
		super();

		this.setText(CREATE_LABEL);
		this.setToolTipText(CREATE_LABEL);
		this.setImageDescriptor(defaultImage);
	}

	public CreateSAPConnectionAction(boolean isToolbar) {
		super();
		setToolbar(isToolbar);
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
			// Force focus to the repositoryView and open Metadata and
			// DbConnection nodes
			getViewPart().setFocus();
			getViewPart().expand(metadataNode, true);
			getViewPart().expand(repositoryNode, true);
		}

		SAPConnection connection = null;
		IPath pathToSave = null;

		// 16670
		if (repositoryNode.getObject() != null && repositoryNode.getObject().getClass().equals(RepositoryObject.class)) {
			try {
				((RepositoryObject) repositoryNode.getObject()).setProperty(ProxyRepositoryFactory.getInstance()
						.getUptodateProperty(repositoryNode.getObject().getProperty()));
			} catch (PersistenceException e) {
				ExceptionHandler.process(e);
			}
		}

		// Define the RepositoryNode, by default Metadata/DbConnection
		RepositoryNode node = repositoryNode;
		// When the userSelection is an element of metadataNode, use it !
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
		// Define the repositoryObject SAP Connection and his pathToSave
		switch (node.getType()) {
		case REPOSITORY_ELEMENT:
			// pathToSave = null;
			connection = (SAPConnection) ((ConnectionItem) node.getObject().getProperty().getItem()).getConnection();
			creation = false;
			break;
		case SIMPLE_FOLDER:
			pathToSave = RepositoryNodeUtilities.getPath(node);
			connection = ConnectionFactory.eINSTANCE.createSAPConnection();
			creation = true;
			break;
		case SYSTEM_FOLDER:
			pathToSave = new Path(""); //$NON-NLS-1$
			connection = ConnectionFactory.eINSTANCE.createSAPConnection();
			creation = true;
			break;
		}

		if (!creation) {
			Property property = node.getObject().getProperty();
			Property updatedProperty = null;
			if (getNeededVersion() == null) {
				// try {
				// updatedProperty =
				// ProxyRepositoryFactory.getInstance().getLastVersion(
				// new
				// Project(ProjectManager.getInstance().getProject(property.getItem())),
				// property.getId())
				// .getProperty();
				//
				// node.getObject().setProperty(updatedProperty);
				// } catch (PersistenceException e) {
				// ExceptionHandler.process(e);
				// }
			} else {
				try {
					updatedProperty = ProxyRepositoryFactory.getInstance().getUptodateProperty(
							new Project(ProjectManager.getInstance().getProject(property.getItem())), property);
				} catch (PersistenceException e) {
					ExceptionHandler.process(e);
				}
			}

		}

		SapWizard repositoryWizard = new SapWizard(PlatformUI.getWorkbench(), creation, node, getExistingNames());
		// Open the Wizard
		WizardDialog wizardDialog = new WizardDialog(Display.getCurrent().getActiveShell(), repositoryWizard);
		wizardDialog.setPageSize(600, 510);
		wizardDialog.create();
		wizardDialog.open();
		connItem = repositoryWizard.getConnectionItem();
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

	public ConnectionItem getConnectionItem() {
		return connItem;
	}

}
