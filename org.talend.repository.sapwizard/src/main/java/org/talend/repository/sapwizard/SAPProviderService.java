package org.talend.repository.sapwizard;

import java.util.Map;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.SAPConnectionItem;
import org.talend.core.ui.ISAPProviderService;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.sapwizard.ui.wizard.SapWizard;

public class SAPProviderService implements ISAPProviderService{

	@Override
	public SAPConnectionItem getRepositoryItem(INode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRepositorySchemaLine(INode node,
			Map<String, Object> lineValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSAPNode(INode node) {
		// TODO Auto-generated method stub
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
