// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.dialog;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.model.IRepositoryNode.ENodeType;

/**
 * DOC chuang class global comment. Detailled comment
 */
public class JobSearchResultProcessor extends JobTypeProcessor {

    private Set<String> jobIds;

    private Set<String> folders;

    public JobSearchResultProcessor(List<IRepositoryViewObject> jobs) {
        super(null);
        jobIds = new HashSet<String>();
        folders = new HashSet<String>();
        folders.contains(new Path("").toString()); //$NON-NLS-1$
        for (IRepositoryViewObject obj : jobs) {
            String path = obj.getProperty().getItem().getState().getPath();
            if (path != null && path.length() > 0) {
                addFolder(new Path(path));
            }
            jobIds.add(obj.getId());
        }
    }

    /**
     * DOC chuang Comment method "addFolder".
     * 
     * @param path
     */
    private void addFolder(IPath path) {
        String lastSegment = path.lastSegment();
        if (lastSegment != null) {
            folders.add(path.toString());
            addFolder(path.removeLastSegments(1));
        }
    }

    @Override
    public ViewerFilter makeFilter() {
        return new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                RepositoryNode node = (RepositoryNode) element;
                if (node.getType() == ENodeType.SYSTEM_FOLDER || node.getType() == ENodeType.STABLE_SYSTEM_FOLDER) {

                    try {
                        if (node.getContentType().equals(ERepositoryObjectType.PROCESS)) {
                            return true;
                        }
                    } catch (Exception e) { // ignore
                    }
                    return false;
                } else if (node.getType() == ENodeType.SIMPLE_FOLDER) {
                    try {

                        String path = RepositoryNodeUtilities.getPath(node).toString();
                        return folders.contains(path);
                    } catch (Exception e) {
                        return false;
                    }
                } else if (node.getObject() != null) {
                    String id = node.getObject().getId();
                    return jobIds.contains(id);
                }
                return false;
            }
        };
    }

}
