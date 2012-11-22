
package org.talend.repository.plsap.ui.viewer.tester;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.metadata.tester.SchemaNodeTester;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.tester.AbstractNodeTester;
import org.talend.repository.tester.SubNodeTester;

public class PlSapMetadataNodeTester extends AbstractNodeTester {

	private PlSapSchemaNodeTester schemaTester = new PlSapSchemaNodeTester();


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.talend.repository.tester.AbstractNodeTester#testProperty(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	protected Boolean testProperty(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof RepositoryNode) {
			RepositoryNode repositoryNode = (RepositoryNode) receiver;

			final ERepositoryObjectType propertyType = ERepositoryObjectType.METADATA_SAPCONNECTIONS;

			if (propertyType != null) {
				if (repositoryNode.getType() == ENodeType.STABLE_SYSTEM_FOLDER) {
					// stable folder(Queries,Table schemas, View schemas, Synonym schemas, Columns)
					Boolean parentTest = testProperty(repositoryNode.getParent(), property, args, expectedValue);
					if (parentTest != null) { // only do for the checked parent.
						return parentTest;
					}
				}

				boolean currentType = isTypeNode(repositoryNode, propertyType);
				/*
				 * check the implication, such as query, schema, column,[CDC] etc
				 */
				boolean schemaTest = checkImplicatedTeser(schemaTester, repositoryNode, ERepositoryObjectType.METADATA_SAP_FUNCTION, propertyType);

				return currentType || schemaTest;
			}
		}
		return null;
	}


	private boolean checkImplicatedTeser(SubNodeTester subTester, RepositoryNode repositoryNode, ERepositoryObjectType testerType, ERepositoryObjectType propertyType) {
		return subTester.isTypeNode(repositoryNode, testerType) && (subTester.findParentItemType(repositoryNode) == propertyType);
	}

}
