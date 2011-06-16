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

public class SAPProviderService implements ISAPProviderService {

    @Override
    public SAPConnectionItem getRepositoryItem(INode node) {
	if (node != null) {
	    if (isSAPNode(node)) {
		IElementParameter param = node.getElementParameter(ISAPConstant.PROPERTY);
		IElementParameter typeParam = param.getChildParameters().get(ISAPConstant.PROPERTY_TYPE);
		if (typeParam != null&& ISAPConstant.REF_ATTR_REPOSITORY.equals(typeParam.getValue())) {
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

    @Override
    public boolean isRepositorySchemaLine(INode node,
	    Map<String, Object> lineValue) {
        if (lineValue != null && node != null) {
            Object type = lineValue.get(ISAPConstant.FIELD_SCHEMA + ISAPConstant.REF_TYPE);
            if (type != null && ISAPConstant.REF_ATTR_REPOSITORY.equals(type)) {
                String value = (String) lineValue.get(ISAPConstant.FIELD_SCHEMA);
                if (value != null && !"".equals(value)) { //$NON-NLS-1$
                    if (MetadataTool.getMetadataTableFromNode(node, value) != null) {
                        return true;

                    }
                }
            }
        }
        return false;
    }

    @Override
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

    @Override
    public IWizard newSAPWizard(IWorkbench workbench, boolean creation,
	    RepositoryNode node, String[] existingNames) {
	if (node == null) {
	    return null;
	}
	if (workbench == null) {
	    workbench = PlatformUI.getWorkbench();
	}
	return new SapWizard(workbench, creation, node, existingNames);
    }

}
