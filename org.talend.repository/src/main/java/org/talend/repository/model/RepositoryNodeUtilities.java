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
package org.talend.repository.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.FolderType;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.SAPConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.ui.views.IRepositoryView;
import org.talend.repository.ui.views.RepositoryView;

/**
 * Utility class to manage RepositoryNode.<br/>
 * 
 * $Id: RepositoryNodeUtilities.java 1368 2007-01-10 09:50:53Z smallet $
 * 
 */
public class RepositoryNodeUtilities {

    private final static String[] METADATA_LABELS = new String[] {};

    public static IPath getPath(RepositoryNode node) {
        if (node == null) {
            return null;
        }
        if (node.isBin()) {
            return new Path(""); //$NON-NLS-1$
        }
        if ((node.getType() == ENodeType.STABLE_SYSTEM_FOLDER && node.getContentType() != ERepositoryObjectType.JOBS && node
                .getContentType() != ERepositoryObjectType.JOBLETS) || node.getType() == ENodeType.SYSTEM_FOLDER) {
            return new Path(""); //$NON-NLS-1$
        }
        if (node.getType() == ENodeType.SIMPLE_FOLDER) {
            String label = node.getObject().getProperty().getLabel();
            return getPath(node.getParent()).append(label);
        }

        String label = node.getLabel();
        // checks if node is under Documentations/Generatated/Jobs
        if (node.getType() == ENodeType.STABLE_SYSTEM_FOLDER
                && (node.getContentType() == ERepositoryObjectType.JOBS || node.getContentType() == ERepositoryObjectType.JOBLETS)) {

            Object nodeLabel = node.getProperties(EProperties.LABEL);

            if (nodeLabel == ERepositoryObjectType.JOBS) {
                return new Path(""); //$NON-NLS-1$
            } else {
                return getPath(node.getParent()).append(label);
            }
        } else {
            if (!isMetadataLabel(label) && !label.equals(ERepositoryObjectType.PROCESS.toString())
                    && !label.equals(ERepositoryObjectType.JOBLET.toString())
                    && !label.equals(ERepositoryObjectType.CONTEXT.toString())
                    && !label.equals(ERepositoryObjectType.ROUTINES.toString())
                    && !label.equals(ERepositoryObjectType.JOB_SCRIPT.toString())
                    && !label.equals(ERepositoryObjectType.SQLPATTERNS.toString())
                    && !label.equals(ERepositoryObjectType.DOCUMENTATION.toString())
                    && !label.equals(ERepositoryObjectType.BUSINESS_PROCESS.toString())
                    && !label.equals(ERepositoryObjectType.METADATA_HEADER_FOOTER.toString())) {
                return getPath(node.getParent()).append(label);
            } else {
                return getPath(node.getParent());
            }
        }

    }

    public static IPath getFolderPath(EObject obj) {
        if (obj == null) {
            return null;
        }
        if (!(obj instanceof FolderItem)) {
            return new Path("");
        }
        FolderItem folderItem = (FolderItem) obj;

        if (folderItem.getType().getValue() == FolderType.FOLDER) {
            String label = folderItem.getProperty().getLabel();
            return getFolderPath(folderItem.getParent()).append(label);
        }
        return new Path("");

    }

    /**
     * Gather all view's metadata children nodes dynamic and get their label.
     * <p>
     * DOC YeXiaowei Comment method "isMetadataLabel".
     * 
     * @param label
     * @return
     */
    private static boolean isMetadataLabel(final String label) {

        RepositoryView view = (RepositoryView) RepositoryView.show();
        if (view == null) {
            return false;
        }

        String[] metadataLabels = view.gatherMetadataChildenLabels();
        if (metadataLabels == null || metadataLabels.length <= 0) {
            return false;
        }

        for (String mlabel : metadataLabels) {
            if (mlabel.equals(label)) {
                return true;
            }
        }

        return false;

    }

    /**
     * 
     * ggu Comment method "getPath".
     * 
     * get path by repository item id. can't get the folders.
     */
    public static IPath getPath(final String id) {
        if (id == null || "".equals(id) || RepositoryNode.NO_ID.equals(id)) { //$NON-NLS-1$
            return null;
        }
        IProxyRepositoryFactory factory = RepositoryPlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
        try {
            final IRepositoryViewObject lastVersion = factory.getLastVersion(id);
            return getPath(lastVersion);
        } catch (PersistenceException e) {
            //
        }
        return null;
    }

    public static IPath getPath(IRepositoryViewObject curNode) {
        if (curNode == null) {
            return null;
        }
        final RepositoryNode repositoryNode = getRepositoryNode(curNode);
        if (repositoryNode != null) {
            return getPath(repositoryNode);
        }
        return null;
    }

    /**
     * 
     * ggu Comment method "getRepositoryNode".
     * 
     * @param id
     * @return the repository node by id
     */
    public static RepositoryNode getRepositoryNode(final String id) {
        return getRepositoryNode(id, true);
    }

    public static RepositoryNode getRepositoryNode(final String id, boolean expanded) {
        if (id == null || "".equals(id) || RepositoryNode.NO_ID.equals(id)) { //$NON-NLS-1$
            return null;
        }
        IProxyRepositoryFactory factory = RepositoryPlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
        try {
            final IRepositoryViewObject lastVersion = factory.getLastVersion(id);
            if (lastVersion != null) {
                return getRepositoryNode(lastVersion, expanded);
            }
        } catch (PersistenceException e) {
            //
        }
        return null;
    }

    /**
     * 
     * ggu Comment method "getRepositoryNode".
     * 
     * get the repository node by a IRepositoryObject.
     */
    public static RepositoryNode getRepositoryNode(IRepositoryViewObject curNode) {
        return getRepositoryNode(curNode, true);
    }

    public static RepositoryNode getRepositoryNode(IRepositoryViewObject curNode, boolean expanded) {
        if (curNode == null) {
            return null;
        }
        IRepositoryView view = RepositoryView.show();
        if (view == null) {
            return null;
        }
        return getRepositoryNode(view.getRoot(), curNode, view, expanded);
    }

    private static RepositoryNode getRepositoryNode(IRepositoryNode rootNode, IRepositoryViewObject curNode,
            IRepositoryView view, boolean expanded) {
        if (rootNode == null || curNode == null || view == null) {
            return null;
        }
        if (expanded) {
            // expande the unvisible node
            expandNode((RepositoryNode) rootNode, curNode, view);
        }

        final List<IRepositoryNode> children = rootNode.getChildren();

        if (children != null) {
            // in the first, search the current folder
            List<IRepositoryNode> folderChild = new ArrayList<IRepositoryNode>();

            for (IRepositoryNode childNode : children) {
                RepositoryNode node = (RepositoryNode) childNode;
                if (isRepositoryFolder(node) || node.getType() == ENodeType.REFERENCED_PROJECT) {
                    folderChild.add(node);
                } else if (node.getId().equals(curNode.getId()) && node.getObjectType() == curNode.getType()) {
                    return node;
                }

            }
            for (IRepositoryNode folderNode : folderChild) {
                final RepositoryNode repositoryNode = getRepositoryNode((RepositoryNode) folderNode, curNode, view, expanded);
                if (repositoryNode != null) {
                    return repositoryNode;
                }
            }
        }

        return null;
    }

    public static void expandNode(IRepositoryView view, RepositoryNode curNode, Set<RepositoryNode> nodes) {
        getRepositoryCheckedNode(view.getRoot(), curNode.getObject(), view, true, nodes);
    }

    private static RepositoryNode getRepositoryCheckedNode(IRepositoryNode rootNode, IRepositoryViewObject curNode,
            IRepositoryView view, boolean expanded, Set<RepositoryNode> nodes) {
        if (rootNode == null || curNode == null || view == null) {
            return null;
        }
        if (expanded) {
            // expande the unvisible node
            expandNode((RepositoryNode) rootNode, curNode, view);
        }
        final List<IRepositoryNode> children = rootNode.getChildren();

        if (children != null) {
            // in the first, search the current folder
            List<IRepositoryNode> folderChild = new ArrayList<IRepositoryNode>();

            for (IRepositoryNode childNode : children) {
                if (isRepositoryFolder(childNode) || childNode.getType() == ENodeType.REFERENCED_PROJECT) {
                    if (hasCheckedChild(childNode, nodes)) {
                        folderChild.add(childNode);
                    }

                } else if (childNode.getId().equals(curNode.getId()) && childNode.getObjectType() == curNode.getType()) {
                    return (RepositoryNode) childNode;
                }

            }
            for (IRepositoryNode folderNode : folderChild) {
                final RepositoryNode repositoryNode = getRepositoryCheckedNode(folderNode, curNode, view, expanded, nodes);
                if (repositoryNode != null) {
                    return repositoryNode;
                }
            }
        }

        return null;
    }

    private static boolean hasCheckedChild(IRepositoryNode fatherNode, Set<RepositoryNode> nodes) {
        if (!fatherNode.getChildren().isEmpty()) {
            for (IRepositoryNode node : fatherNode.getChildren()) {
                for (IRepositoryNode pnode : nodes) {
                    if (node.equals(pnode)) {
                        return true;
                    }
                }
                boolean flag = hasCheckedChild(node, nodes);
                if (flag) {
                    return true;
                }
            }
        } else {
            for (IRepositoryNode node : nodes) {
                if (node.equals(fatherNode)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void expandNode(RepositoryNode rootNode, IRepositoryViewObject curNode, IRepositoryView view) {
        if (rootNode == null || curNode == null || view == null) {
            return;
        }
        final ERepositoryObjectType rootContextType = rootNode.getContentType();
        final ERepositoryObjectType curType = curNode.getType();

        // for referenced project
        if (rootContextType == ERepositoryObjectType.REFERENCED_PROJECTS || rootNode.getType() == ENodeType.REFERENCED_PROJECT) {
            expandParentNode(view, rootNode);
        }
        if (rootContextType != null) {

            ERepositoryObjectType tmpType = null;
            switch (curType) {
            case METADATA_CON_TABLE:
            case METADATA_CON_VIEW:
            case METADATA_CON_SYNONYM:
            case METADATA_CON_QUERY:
            case METADATA_CONNECTIONS:
            case METADATA_FILE_DELIMITED:
            case METADATA_FILE_POSITIONAL:
            case METADATA_FILE_REGEXP:
            case METADATA_FILE_XML:
            case METADATA_FILE_LDIF:
            case METADATA_FILE_EXCEL:
            case METADATA_GENERIC_SCHEMA:
            case METADATA_LDAP_SCHEMA:
            case METADATA_SALESFORCE_SCHEMA:
            case METADATA_WSDL_SCHEMA:
            case METADATA_FILE_EBCDIC:
            case METADATA_FILE_HL7:
            case METADATA_FILE_FTP:
            case METADATA_FILE_BRMS:
            case METADATA_MDMCONNECTION:
            case METADATA_FILE_RULES:// added by hyWang
            case METADATA_FILE_LINKRULES:
            case METADATA_SAPCONNECTIONS:
            case METADATA_HEADER_FOOTER:
                tmpType = ERepositoryObjectType.METADATA;
                break;
            case ROUTINES:
            case SNIPPETS:
                tmpType = ERepositoryObjectType.ROUTINES;
                break;
            case DOCUMENTATION:
            case JOB_DOC:
            case JOBLET_DOC:
                tmpType = ERepositoryObjectType.DOCUMENTATION;
                //
                break;
            default:
            }

            if (tmpType != null && tmpType == rootContextType) {
                expandParentNode(view, rootNode);
            }
            // expand the parent node

            if (curType == rootContextType && isRepositoryFolder(rootNode)) {
                if (rootContextType == ERepositoryObjectType.SQLPATTERNS
                        && !(rootNode.getParent() instanceof ProjectRepositoryNode)) {
                    // sql pattern
                } else {
                    expandParentNode(view, rootNode);
                    view.getViewer().refresh();
                }
            }

        }
    }

    public static void expandParentNode(IRepositoryView view, RepositoryNode node) {
        if (view == null || node == null) {
            return;
        }
        expandParentNode(view, node.getParent());
        view.expand(node, true);
        // for db
        StructuredViewer viewer = view.getViewer();
        if (viewer instanceof TreeViewer) {
            TreeViewer treeViewer = (TreeViewer) viewer;
            ERepositoryObjectType objectType = node.getObjectType();
            if (objectType != null) {
                switch (objectType) {
                case METADATA_CONNECTIONS:
                    treeViewer.expandToLevel(node, TreeViewer.ALL_LEVELS);
                    break;
                case ROUTINES:
                    treeViewer.expandToLevel(node, 2);
                    break;
                }
            }
        }

    }

    private static boolean isRepositoryFolder(IRepositoryNode node) {
        if (node == null) {
            return false;
        }
        final ENodeType type = node.getType();
        if (type == ENodeType.SIMPLE_FOLDER || type == ENodeType.STABLE_SYSTEM_FOLDER || type == ENodeType.SYSTEM_FOLDER) {
            return true;
        }
        return false;
    }

    public static RepositoryNode getMetadataTableFromConnection(String schemaValue) {
        String[] values = schemaValue.split(" - "); //$NON-NLS-1$
        String repositoryID = values[0];
        String tableName = values[1];

        try {
            final RepositoryNode realNode = getRepositoryNode(repositoryID);
            if (realNode.getObject() != null && realNode.getObject().getProperty() != null) {
                Item item = realNode.getObject().getProperty().getItem();
                if (item instanceof SAPConnectionItem) {
                    return getSAPSchemaFromConnection(realNode, schemaValue);
                } else {
                    return getSchemeFromConnection(realNode, tableName, ERepositoryObjectType.METADATA_CON_TABLE);
                }
            }

        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    private static RepositoryNode getSAPSchemaFromConnection(RepositoryNode realNode, String name) {
        String[] values = name.split(" - "); //$NON-NLS-1$
        if (values.length != 3) {
            return null;
        }
        String metadataName = values[2];
        String repositoryId = name.substring(0, name.lastIndexOf(" - ")); //$NON-NLS-1$
        RepositoryNode functionNode = getSAPFunctionFromConnection(repositoryId);
        for (IRepositoryNode node : functionNode.getChildren()) {
            if (metadataName.equals(node.getProperties(EProperties.LABEL))) {
                return (RepositoryNode) node;
            }
        }
        return null;
    }

    public static RepositoryNode getQueryFromConnection(String schemaValue) {
        String[] values = schemaValue.split(" - "); //$NON-NLS-1$
        String repositoryID = values[0];
        String tableName = values[1];

        try {
            final RepositoryNode realNode = getRepositoryNode(repositoryID);
            return getSchemeFromConnection(realNode, tableName, ERepositoryObjectType.METADATA_CON_QUERY);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    public static RepositoryNode getSAPFunctionFromConnection(String id) {
        String[] values = id.split(" - "); //$NON-NLS-1$
        String repositoryID = values[0];
        String functionName = values[1];

        try {
            final RepositoryNode realNode = getRepositoryNode(repositoryID);
            if (realNode.getObject() != null) {
                if (ERepositoryObjectType.METADATA_SAPCONNECTIONS.equals(realNode.getObject().getType())) {
                    for (IRepositoryNode node : realNode.getChildren()) {
                        if (Messages.getString("RepositoryContentProvider.repositoryLabel.sapFunction").equals(node.getLabel())) { //$NON-NLS-1$
                            for (IRepositoryNode function : node.getChildren()) {
                                if (functionName.equals(function.getProperties(EProperties.LABEL))) {
                                    return (RepositoryNode) function;
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    private static RepositoryNode getSchemeFromConnection(RepositoryNode connection, String tableName,
            ERepositoryObjectType repType) {
        ERepositoryObjectType type = connection.getObject().getType();
        if (repType == ERepositoryObjectType.METADATA_CON_QUERY) {
            for (IRepositoryNode node : connection.getChildren()) {
                if (Messages.getString("RepositoryContentProvider.repositoryLabel.Queries").equals(node.getLabel())) { //$NON-NLS-1$
                    for (IRepositoryNode query : node.getChildren()) {
                        if (tableName.equals(query.getProperties(EProperties.LABEL))) {
                            return (RepositoryNode) query;
                        }
                    }
                }
            }
        } else {
            if (type == ERepositoryObjectType.METADATA_CONNECTIONS) {
                for (IRepositoryNode child : connection.getChildren()) {
                    if (Messages.getString("RepositoryContentProvider.repositoryLabel.Queries").equals(child.getLabel())) { //$NON-NLS-1$
                        continue;
                    }
                    for (IRepositoryNode node : child.getChildren()) {
                        if (tableName.equals(node.getProperties(EProperties.LABEL))) {
                            return (RepositoryNode) node;
                        }
                    }
                }
            } else {
                for (IRepositoryNode child : connection.getChildren()) {
                    if (tableName.equals(child.getProperties(EProperties.LABEL))) {
                        return (RepositoryNode) child;
                    }
                }

            }

        }
        return null;
    }

    /**
     * 
     * ggu Comment method "getParentRepositoryNodeFromSelection".
     * 
     */
    public static RepositoryNode getParentRepositoryNodeFromSelection(IRepositoryViewObject object) {
        if (object.getRepositoryNode() != null && ((RepositoryNode) object.getRepositoryNode()).getParent() != null) {
            return ((RepositoryNode) object.getRepositoryNode()).getParent();
        }

        // "old" code bellow should never be called, unless the repository object is a new created and not from the
        // repository.

        IRepositoryView viewPart = RepositoryView.show();
        ISelection repositoryViewSelection = viewPart.getViewer().getSelection();

        if (repositoryViewSelection instanceof IStructuredSelection) {
            RepositoryNode selectedRepositoryNode = (RepositoryNode) ((IStructuredSelection) repositoryViewSelection)
                    .getFirstElement();
            // fixed for the opened job and lost the selected node.
            if (object != null) {

                selectedRepositoryNode = getRepositoryNode(object, false);

            }
            if (selectedRepositoryNode != null) {
                return selectedRepositoryNode.getParent();
            }
        }
        return null;

    }
}
