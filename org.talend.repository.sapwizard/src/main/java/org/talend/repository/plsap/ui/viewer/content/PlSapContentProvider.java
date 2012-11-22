
package org.talend.repository.plsap.ui.viewer.content;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.metadata.content.AbstractMetadataContentProvider;
import org.talend.repository.model.ProjectRepositoryNode;
import org.talend.repository.model.RepositoryNode;

public class PlSapContentProvider extends AbstractMetadataContentProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.talend.repository.viewer.content.ProjectRepoAbstractContentProvider#getTopLevelNodeFromProjectRepositoryNode
	 * (org.talend.repository.model.ProjectRepositoryNode)
	 */
	@Override
	protected RepositoryNode getTopLevelNodeFromProjectRepositoryNode(ProjectRepositoryNode projectNode) {
		return projectNode.getRootRepositoryNode(ERepositoryObjectType.METADATA_SAPCONNECTIONS);
	}
}
