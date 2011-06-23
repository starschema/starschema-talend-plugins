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

import java.util.Map;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.metadata.ISAPConstant;
import org.talend.core.model.metadata.MetadataTool;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.SAPConnectionItem;
import org.talend.core.ui.ISAPProviderService;
import org.talend.repository.UpdateRepositoryUtils;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.sapwizard.ui.wizard.SapWizard;

/**
 * @author Ammu
 * 
 */
public class SAPProviderService implements ISAPProviderService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.talend.core.ui.ISAPProviderService#getRepositoryItem(org.talend.core
	 * .model.process.INode)
	 */

	public SAPConnectionItem getRepositoryItem(INode node) {
		if (node != null) {
			if (isSAPNode(node)) {
				IElementParameter param = node.getElementParameter(ISAPConstant.PROPERTY);
				IElementParameter typeParam = param.getChildParameters().get(ISAPConstant.PROPERTY_TYPE);
				if (typeParam != null && ISAPConstant.REF_ATTR_REPOSITORY.equals(typeParam.getValue())) {
					IElementParameter repositoryParam = param.getChildParameters().get(
							ISAPConstant.REPOSITORY_PROPERTY_TYPE);
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
	 * @see
	 * org.talend.core.ui.ISAPProviderService#isRepositorySchemaLine(org.talend
	 * .core.model.process.INode, java.util.Map)
	 */
	@SuppressWarnings("deprecation")
	public boolean isRepositorySchemaLine(INode node, Map<String, Object> lineValue) {
		if (lineValue != null && node != null) {
			Object type = lineValue.get(ISAPConstant.FIELD_SCHEMA + ISAPConstant.REF_TYPE);
			if (type != null && ISAPConstant.REF_ATTR_REPOSITORY.equals(type)) {
				String value = (String) lineValue.get(ISAPConstant.FIELD_SCHEMA);
				if (value != null && !value.isEmpty()) { //$NON-NLS-1$
					if (MetadataTool.getMetadataTableFromNode(node, value) != null) {
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
	 * @see
	 * org.talend.core.ui.ISAPProviderService#isSAPNode(org.talend.core.model
	 * .process.INode)
	 */

	public boolean isSAPNode(INode node) {
		if (node != null) {
			IElementParameter param = node.getElementParameter(ISAPConstant.PROPERTY);
			if (param != null && param.getField() == EParameterFieldType.PROPERTY_TYPE
					&& ISAPConstant.REPOSITORY_VALUE.equals(param.getRepositoryValue())) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.talend.core.ui.ISAPProviderService#newSAPWizard(org.eclipse.ui.IWorkbench
	 * , boolean, org.talend.repository.model.RepositoryNode,
	 * java.lang.String[])
	 */

	public IWizard newSAPWizard(IWorkbench workbench, boolean creation, RepositoryNode node, String[] existingNames) {
		if (node == null) {
			return null;
		}
		if (workbench == null) {
			workbench = PlatformUI.getWorkbench();
		}
		return new SapWizard(workbench, creation, node, existingNames);
	}

}
