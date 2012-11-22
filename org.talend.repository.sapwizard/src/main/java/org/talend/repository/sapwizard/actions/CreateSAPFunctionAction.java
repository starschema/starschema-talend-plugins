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

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.SAPConnection;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.metadata.builder.connection.SubscriberTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.SAPConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryObject;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.cwm.helper.TableHelper;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.SAPFunctionRepositoryObject;
import org.talend.repository.sap.i18n.Messages;
import org.talend.repository.sapwizard.ui.wizard.SapTableWizard;
import org.talend.repository.ui.actions.metadata.AbstractCreateTableAction;

/**
 * @author Ammu
 * 
 */
public class CreateSAPFunctionAction extends AbstractCreateTableAction {

	private static final String CREATE_LABEL = Messages.getString("CreateSAPFunctionAction.Action.CreateTitle");
	private RepositoryNode node = null;


	public CreateSAPFunctionAction() {
		setText(CREATE_LABEL);
		setToolTipText(CREATE_LABEL);
		setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.METADATA_TABLE_ICON));
	}


	public CreateSAPFunctionAction(RepositoryNode paramRepositoryNode) {
		this();
		this.node = paramRepositoryNode;
	}


	protected void doRun() {
		Object selection = null;
		if (this.node == null) {
			RepositoryNode repositoryNode = getMetadataNode(getCurrentRepositoryNode());
			getViewPart().setFocus();
			getViewPart().expand(repositoryNode, true);
			selection = (IStructuredSelection) getSelection();
			this.node = ((RepositoryNode) ((IStructuredSelection) selection).getFirstElement());
		}
		init(this.node);
		ERepositoryObjectType repositoryNodeType = (ERepositoryObjectType) this.node.getProperties(RepositoryNode.EProperties.CONTENT_TYPE);
		if (ERepositoryObjectType.METADATA_CON_TABLE.equals(repositoryNodeType)) {
			selection = this.node.getObject();
			if ((selection instanceof MetadataTableRepositoryObject)) {
				MetadataTable localObject3 = ((MetadataTableRepositoryObject) selection).getTable();
				if ((localObject3 instanceof SubscriberTable)) {
					this.node = null;
					return;
				}
			}
			ConnectionItem connectionItem = (ConnectionItem) ((IRepositoryObject) selection).getProperty().getItem();
			repositoryNodeType = ERepositoryObjectType.getItemType(connectionItem);
		}
		if ((ERepositoryObjectType.METADATA_SAPCONNECTIONS.equals(repositoryNodeType)) || (ERepositoryObjectType.METADATA_SAP_FUNCTION.equals(repositoryNodeType))) {
			getViewPart().expand(this.node, true);
		}
		createSAPTableWizard(this.node, true);
	}


	@SuppressWarnings("deprecation")
	private void createSAPTableWizard(RepositoryNode paramRepositoryNode, boolean creation) {
		SAPConnection localSAPConnection = null;
		SAPFunctionUnit localSAPFunctionUnit = null;
		if (paramRepositoryNode.getType() == RepositoryNode.ENodeType.REPOSITORY_ELEMENT) {
			ERepositoryObjectType localERepositoryObjectType = (ERepositoryObjectType) paramRepositoryNode.getProperties(RepositoryNode.EProperties.CONTENT_TYPE);
			SAPConnectionItem localSAPConnectionItem = null;
			MetadataTable metadataTable = null;
			String label = null;
			if (localERepositoryObjectType == ERepositoryObjectType.METADATA_CON_TABLE) {
				localSAPConnectionItem = (SAPConnectionItem) paramRepositoryNode.getObject().getProperty().getItem();
				localSAPConnection = (SAPConnection) localSAPConnectionItem.getConnection();
			} else if (localERepositoryObjectType == ERepositoryObjectType.METADATA_SAPCONNECTIONS) {
				localSAPConnectionItem = (SAPConnectionItem) paramRepositoryNode.getObject().getProperty().getItem();
				localSAPConnection = (SAPConnection) localSAPConnectionItem.getConnection();
				label = (String) paramRepositoryNode.getProperties(RepositoryNode.EProperties.LABEL);
				IRepositoryViewObject localIRepositoryObject = paramRepositoryNode.getObject();
				if ((localIRepositoryObject instanceof MetadataTableRepositoryObject)) {
					metadataTable = ((MetadataTableRepositoryObject) localIRepositoryObject).getTable();
				} else {
					metadataTable = TableHelper.findByLabel(localSAPConnection, label);
				}
				creation = true;
			} else if (localERepositoryObjectType == ERepositoryObjectType.METADATA_SAP_FUNCTION) {
				localSAPConnectionItem = (SAPConnectionItem) paramRepositoryNode.getObject().getProperty().getItem();
				label = (String) paramRepositoryNode.getProperties(RepositoryNode.EProperties.LABEL);
				localSAPConnection = (SAPConnection) localSAPConnectionItem.getConnection();
				if ((paramRepositoryNode.getObject() instanceof SAPFunctionRepositoryObject)) {
					localSAPFunctionUnit = getFuntionUnitByLabel(localSAPConnection, label);
				}
				creation = false;
			}
			initContextMode(localSAPConnectionItem);
			openSAPFunctionWizard(localSAPConnectionItem, localSAPFunctionUnit, creation, paramRepositoryNode, metadataTable);
		}
	}


	private SAPFunctionUnit getFuntionUnitByLabel(SAPConnection sapConnection, String label) {
		EList<SAPFunctionUnit> localEList = sapConnection.getFuntions();
		for (SAPFunctionUnit localSAPFunctionUnit : localEList) {
			if (localSAPFunctionUnit.getName().equals(label)) {
				return localSAPFunctionUnit;
			}
		}
		return null;
	}


	private void openSAPFunctionWizard(SAPConnectionItem sapConnectionItem, SAPFunctionUnit sapFunctionUnit, boolean creation, RepositoryNode repositoryNode, MetadataTable metadataTable) {
		SapTableWizard localSAPFunctionWizard = new SapTableWizard(PlatformUI.getWorkbench(), repositoryNode, sapConnectionItem, sapFunctionUnit, metadataTable, getExistingNames(), creation);
		WizardDialog localWizardDialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), localSAPFunctionWizard);
		localWizardDialog.setPageSize(400, 400);
		localWizardDialog.setBlockOnOpen(true);
		RepositoryNode localRepositoryNode = this.node;
		if ((this.node.getObject() instanceof SAPFunctionRepositoryObject)) {
			localRepositoryNode = this.node.getParent().getParent();
		}
		handleWizard(localRepositoryNode, localWizardDialog, true);
		this.node = null;
	}


	protected void init(RepositoryNode paramRepositoryNode) {
		ProxyRepositoryFactory localProxyRepositoryFactory = ProxyRepositoryFactory.getInstance();
		if ((localProxyRepositoryFactory.isUserReadOnlyOnCurrentProject()) || (!ProjectManager.getInstance().isInCurrentMainProject(paramRepositoryNode))) {
			setEnabled(false);
		} else if (RepositoryNode.ENodeType.REPOSITORY_ELEMENT.equals(paramRepositoryNode.getType())) {
			if (localProxyRepositoryFactory.getStatus(paramRepositoryNode.getObject()) == ERepositoryStatus.DELETED) {
				setEnabled(false);
				return;
			}
			ERepositoryObjectType localERepositoryObjectType = (ERepositoryObjectType) paramRepositoryNode.getProperties(RepositoryNode.EProperties.CONTENT_TYPE);
			if (ERepositoryObjectType.METADATA_SAPCONNECTIONS.equals(localERepositoryObjectType)) {
				setText(CREATE_LABEL);
				collectChildNames(paramRepositoryNode);
				setEnabled(true);
				this.node = paramRepositoryNode;
				return;
			}
			if (ERepositoryObjectType.METADATA_SAP_FUNCTION.equals(localERepositoryObjectType)) {
				setText(CREATE_LABEL);
				collectChildNames(paramRepositoryNode);
				setEnabled(true);
				this.node = paramRepositoryNode;
				return;
			}
		}
	}


	public Class<SAPFunctionRepositoryObject> getClassForDoubleClick() {
		return SAPFunctionRepositoryObject.class;
	}
}
