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
package org.talend.repository.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryObject;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.ProjectRepositoryNode;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;

/**
 * Content provider for the repository view.<br/>
 * 
 * $Id: RepositoryContentProvider.java 50916 2010-11-04 07:59:46Z wchen $
 * 
 */
public class RepositoryContentProvider implements IStructuredContentProvider, ITreeContentProvider {

    private final IRepositoryView view;

    private ProjectRepositoryNode root;

    private TreeViewer viewer;

    private final List<IRepositoryObject> joblets = new ArrayList<IRepositoryObject>();

    private final IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    public RepositoryContentProvider(IRepositoryView view) {
        super();
        this.view = view;
    }

    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        viewer = (TreeViewer) v;
    }

    public void dispose() {
    }

    public Object[] getElements(Object parent) {
        if (parent.equals(view.getViewSite())) {
            IRepositoryNode systemFolders = view.getRoot();
            if (systemFolders.getChildren().isEmpty()) {
                initialize();
            }

            return systemFolders.getChildren().toArray();
        }
        return getChildren(parent);
    }

    public Object getParent(Object child) {

        return ((RepositoryNode) child).getParent();
    }

    public Object[] getChildren(Object parent) {
        RepositoryNode repositoryNode = ((RepositoryNode) parent);
        if (!repositoryNode.isInitialized()) {
            if (repositoryNode.getParent() instanceof ProjectRepositoryNode) {
                // initialize repository from main project
                ((ProjectRepositoryNode) repositoryNode.getParent()).initializeChildren(parent);
            }
            repositoryNode.setInitialized(true);
        }

        return repositoryNode.getChildren().toArray();
    }

    public boolean hasChildren(Object parent) {
        Boolean boolean1 = factory.hasChildren(parent);
        if (boolean1 != null) {
            return boolean1;
        } else {
            if (parent instanceof RepositoryNode) {
                RepositoryNode repositoryNode = (RepositoryNode) parent;
                if (repositoryNode.isInitialized()) {
                    return repositoryNode.getChildren().size() > 0;
                } else {
                    return getChildren(parent).length > 0;
                }
            }
            return true;
        }
    }

    private void initialize() {
        root = (ProjectRepositoryNode) view.getRoot();

        root.initialize();
    }

    /**
     * Getter for processNode.
     * 
     * @return the processNode
     */
    public RepositoryNode getProcessNode() {
        return root.getProcessNode();
    }

    public RepositoryNode getJobletNode() {
        return root.getJobletNode();
    }

    public RepositoryNode getReferenceProjectNode() {
        return root.getReferenceProjectNode();
    }

    /**
     * Getter for metadataConNode.
     * 
     * @return the metadataConNode
     */
    public RepositoryNode getMetadataConNode() {
        if (root.getMetadataConNode() == null) {
            getChildren(root.getMetadataConNode());
        }
        return root.getMetadataConNode();
    }

    /**
     * Getter for codeNode.
     * 
     * @return the codeNode
     */
    public RepositoryNode getCodeNode() {
        return root.getCodeNode();
    }

    /**
     * Getter for metadataFileNode.
     * 
     * @return the metadataFileNode
     */
    public RepositoryNode getMetadataFileNode() {
        return root.getMetadataFileNode();
    }

    /**
     * Getter for metadataFilePositionalNode.
     * 
     * @return the metadataFilePositionalNode
     */
    public RepositoryNode getMetadataFilePositionalNode() {
        return root.getMetadataFilePositionalNode();
    }

    /**
     * Getter for metadataFileRegexpNode.
     * 
     * @return the metadataFileRegexpNode
     */
    public RepositoryNode getMetadataFileRegexpNode() {
        return root.getMetadataFileRegexpNode();
    }

    /**
     * Getter for metadataFileXmlNode.
     * 
     * @return the metadataFileXmlNode
     */
    public RepositoryNode getMetadataFileXmlNode() {
        return root.getMetadataFileXmlNode();
    }

    /**
     * Getter for metadataFileLdifNode.
     * 
     * @return the metadataFileLdifNode
     */
    public RepositoryNode getMetadataFileLdifNode() {
        return root.getMetadataFileLdifNode();
    }

    /**
     * Getter for metadataGenericSchemaNode.
     * 
     * @return the metadataGenericSchemaNode
     */
    public RepositoryNode getMetadataGenericSchemaNode() {
        return root.getMetadataGenericSchemaNode();
    }

    /**
     * Getter for metadataLDAPSchemaNode.
     * 
     * @return the metadataLDAPSchemaNode
     */
    public RepositoryNode getMetadataLDAPSchemaNode() {
        return root.getMetadataLDAPSchemaNode();
    }

    /**
     * Getter for metadataWSDLSchemaNode.
     * 
     * @return the metadataWSDLSchemaNode
     */
    public RepositoryNode getMetadataWSDLSchemaNode() {
        return root.getMetadataWSDLSchemaNode();
    }

    /**
     * Getter for metadataNode.
     * 
     * @return the metadataNode
     */
    public RepositoryNode getMetadataNode() {
        return root.getMetadataNode();
    }

    /**
     * Getter for metadataFileExcelNode.
     * 
     * @return the metadataFileExcelNode
     */
    public RepositoryNode getMetadataFileExcelNode() {
        return root.getMetadataFileExcelNode();
    }

    /**
     * Getter for metadataSalesforceSchemaNode.
     * 
     * @return the metadataSalesforceSchemaNode
     */
    public RepositoryNode getMetadataSalesforceSchemaNode() {
        return root.getMetadataSalesforceSchemaNode();
    }

    public RepositoryNode getMetadataBRMSConnectionNode() {
        return root.getMetadataBRMSConnectionNode();
    }

    public String[] gatherMetdataChildrens() {
        if (getMetadataNode() == null) {
            return null;
        }

        List<IRepositoryNode> nodes = getMetadataNode().getChildren();
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }

        String[] res = new String[nodes.size()];
        for (int i = 0, n = nodes.size(); i < n; i++) {
            res[i] = nodes.get(i).getLabel();
        }

        return res;
    }

    public RepositoryNode getMetadataSAPConnectionNode() {
        return root.getMetadataSAPConnectionNode();
    }

    public RepositoryNode getMetadataHeaderFooterConnectionNode() {
        return root.getMetadataHeaderFooterConnectionNode();
    }

    public RepositoryNode getRootRepositoryNode(ERepositoryObjectType type) {
        return root.getRootRepositoryNode(type);
    }

    public RepositoryNode getRoot() {
        return root;
    }
}
