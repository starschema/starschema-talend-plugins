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

package org.talend.repository.sapwizard;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.metadata.ISAPConstant;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.LinkRulesItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RulesItem;
import org.talend.core.model.properties.SAPConnectionItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.ui.ISAPProviderService;
import org.talend.core.ui.metadata.celleditor.EProcessTypeForRule;
import org.talend.core.ui.rule.AbstractRlueOperationChoice;
import org.talend.repository.UpdateRepositoryUtils;
import org.talend.repository.model.IMetadataService;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.sapwizard.actions.CreateSAPTableAction;
import org.talend.repository.sapwizard.ui.wizard.SapWizard;
import org.talend.repository.ui.actions.metadata.AbstractCreateTableAction;

/**
 * @author Ammu
 * 
 */
public class SAPProviderService implements ISAPProviderService, IMetadataService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.talend.core.ui.ISAPProviderService#getRepositoryItem(org.talend.core .model.process.INode)
	 */

	public SAPConnectionItem getRepositoryItem(INode node) {
		if (node != null) {
			if (isSAPNode(node)) {
				IElementParameter param = node.getElementParameter(ISAPConstant.PROPERTY);
				IElementParameter typeParam = param.getChildParameters().get(ISAPConstant.PROPERTY_TYPE);
				if (typeParam != null && ISAPConstant.REF_ATTR_REPOSITORY.equals(typeParam.getValue())) {
					IElementParameter repositoryParam = param.getChildParameters().get(ISAPConstant.REPOSITORY_PROPERTY_TYPE);
					final String value = (String) repositoryParam.getValue();
					Item item = UpdateRepositoryUtils.getConnectionItemByItemId(value);
					if (item != null && item instanceof SAPConnectionItem) {
						return (SAPConnectionItem) item;
					}
				}
			}
		}
		return null;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.talend.core.ui.ISAPProviderService#isRepositorySchemaLine(org.talend .core.model.process.INode, java.util.Map)
	 */
	public boolean isRepositorySchemaLine(INode node, Map<String, Object> lineValue) {
		if (lineValue != null && node != null) {
			Object type = lineValue.get(ISAPConstant.FIELD_SCHEMA + ISAPConstant.REF_TYPE);
			if (type != null && ISAPConstant.REF_ATTR_REPOSITORY.equals(type)) {
				String value = (String) lineValue.get(ISAPConstant.FIELD_SCHEMA);
				if (value != null && !value.isEmpty()) { //$NON-NLS-1$
					if (MetadataToolHelper.getMetadataTableFromNodeLabel(node, value) != null) {
						return true;

					}
				}
			}
		}
		return false;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.talend.core.ui.ISAPProviderService#isSAPNode(org.talend.core.model .process.INode)
	 */

	public boolean isSAPNode(INode node) {
		if (node != null) {
			IElementParameter param = node.getElementParameter(ISAPConstant.PROPERTY);
			if (param != null && param.getFieldType() == EParameterFieldType.PROPERTY_TYPE && ISAPConstant.REPOSITORY_VALUE.equals(param.getRepositoryValue())) {
				return true;
			}
		}
		return false;
	}


	@Override
	public IWizard newWizard(IWorkbench workbench, boolean creation, RepositoryNode node, String[] existingNames) {
		if (node == null) {
			return null;
		}
		if (workbench == null) {
			workbench = PlatformUI.getWorkbench();
		}
		return new SapWizard(workbench, creation, node, existingNames);
	}


	@Override
	public WizardDialog getGenericSchemaWizardDialog(Shell shell, IWorkbench workbench, boolean creation, ISelection selection, String[] existingNames, boolean isSinglePageOnly) {
		return null;
	}


	@Override
	public Property getPropertyFromWizardDialog() {
		return null;
	}


	@Override
	public IPath getPathForSaveAsGenericSchema() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ConnectionItem openMetadataConnection(boolean creation, IRepositoryNode repoNode, INode node) {
		return null;
	}


	@Override
	public void openMetadataConnection(IRepositoryViewObject o, INode node) {
		
	}


	@Override
	public void openEditSchemaWizard(String value) {
		
	}


	@Override
	public void runCreateTableAction(RepositoryNode metadataNode) {
        AbstractCreateTableAction action = new CreateSAPTableAction(metadataNode);
        action.setAvoidUnloadResources(true);
        action.run();
	}


	@Override
	public AbstractRlueOperationChoice getOperationChoice(Shell shell, INode node, RulesItem[] repositoryRuleItems, LinkRulesItem[] linkRuleItems, EProcessTypeForRule rule, String ruleToEdit, boolean readOnly) {
		return null;
	}


	@Override
	public DatabaseMetaData findCustomizedJTDSDBMetadata(Connection conn) {
		return null;
	}

}
