
package org.talend.repository.plsap.ui.viewer.tester;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.tester.SubNodeTester;

public class PlSapSchemaNodeTester extends SubNodeTester {

	private static final String IS_SCHEMA = "isSchema"; //$NON-NLS-1$


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.talend.repository.tester.AbstractNodeTester#testProperty(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	protected Boolean testProperty(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof RepositoryNode) {
			RepositoryNode repositoryNode = (RepositoryNode) receiver;
			if (IS_SCHEMA.equals(property)) {
				return isSchema(repositoryNode);
			}
		}
		return null;
	}


	public boolean isSchema(RepositoryNode repositoryNode) {
		return isTypeNode(repositoryNode, ERepositoryObjectType.METADATA_SAP_FUNCTION);
	}


	@Override
	public ERepositoryObjectType findParentItemType(RepositoryNode repositoryNode) {
		final ERepositoryObjectType objectType = repositoryNode.getObjectType();
		if (objectType == ERepositoryObjectType.METADATA_SAP_FUNCTION) {
			if (repositoryNode.getObject() != null) {
				ERepositoryObjectType parentType = ERepositoryObjectType.getItemType(repositoryNode.getObject().getProperty().getItem());
				return parentType;
			}
		}
		return null;
	}

}
