// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.model.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.INode;

/**
 * DOC xtan class global comment. Detailled comment <br/>
 * 
 */
public class NodeUtil {

    /**
     * DOC sort the outgoingconnections to make sure the first connection is EConnectionType.FLOW_MAIN or
     * EConnectionType.FLOW_REF<br/>
     * <p>
     * bug:9363, if a component have 2 output links,
     * <li>"EConnectionType.FLOW_MAIN(FLOW), EConnectionType.FLOW_REF(REJECT)"</li>
     * <li>"EConnectionType.FLOW_MAIN(REJECT), EConnectionType.FLOW_REF(FLOW)"</li>, make FLOW before "REJECT"
     * </p>
     * 
     * @param node
     * @return List<? extends IConnection>
     */

    public static List<? extends IConnection> getOutgoingCamelSortedConnections(INode node) {
        List<IConnection> conns = null;

        List<? extends IConnection> outgoingConnections = node.getOutgoingConnections();
        if (outgoingConnections != null) {
            conns = new ArrayList<IConnection>(outgoingConnections);
            Collections.sort(conns, new Comparator<IConnection>() {

                public int compare(IConnection o1, IConnection o2) {
                    if (EConnectionType.ROUTE_WHEN == o1.getLineStyle())
                        return -1;
                    if (EConnectionType.ROUTE_OTHER == o1.getLineStyle())
                        if (EConnectionType.ROUTE_WHEN == o2.getLineStyle())
                            return 1;
                    if (EConnectionType.ROUTE_ENDBLOCK == o1.getLineStyle()) {
                        if (EConnectionType.ROUTE_WHEN == o2.getLineStyle() || EConnectionType.ROUTE_OTHER == o2.getLineStyle())
                            return 2;
                        if (EConnectionType.ROUTE_TRY == o2.getLineStyle() || EConnectionType.ROUTE_CATCH == o2.getLineStyle()
                                || EConnectionType.ROUTE_FINALLY == o2.getLineStyle())
                            return 3;
                        if (EConnectionType.ROUTE == o2.getLineStyle()) {
                            return 4;
                        }
                    }
                    if (EConnectionType.ROUTE_TRY == o1.getLineStyle())
                        return -1;
                    if (EConnectionType.ROUTE_CATCH == o1.getLineStyle())
                        if (EConnectionType.ROUTE_TRY == o2.getLineStyle())
                            return 1;
                    if (EConnectionType.ROUTE_FINALLY == o1.getLineStyle())
                        if (EConnectionType.ROUTE_TRY == o2.getLineStyle() || EConnectionType.ROUTE_CATCH == o2.getLineStyle())
                            return 2;

                    return 0;
                }

            });
        }

        return conns;
    }

    public static List<? extends IConnection> getOutgoingSortedConnections(INode node) {

        List<IConnection> conns = null;

        List<? extends IConnection> outgoingConnections = node.getOutgoingConnections();
        if (outgoingConnections != null) {
            conns = new ArrayList<IConnection>(outgoingConnections);
            Collections.sort(conns, new Comparator<IConnection>() {

                public int compare(IConnection connection1, IConnection connection2) {

                    EConnectionType lineStyle = connection1.getLineStyle();
                    EConnectionType lineStyle2 = connection2.getLineStyle();
                    // "FLOW" only for the default Main connection, if user define his connection like: "FILTER",
                    // "REJECT", "FLOW", he should use this API in JET directly: List<? extends IConnection> connsFilter
                    // = node.getOutgoingConnections("FILTER");
                    // 1. FLOW first
                    if ("FLOW".equals(connection1.getConnectorName())) { //$NON-NLS-1$
                        return -1;
                    }

                    if ("FLOW".equals(connection2.getConnectorName())) { //$NON-NLS-1$
                        return 1;
                    }

                    // 2. FLOW_MAIN, FLOW_MERGE second
                    if (lineStyle == EConnectionType.FLOW_MAIN || lineStyle == EConnectionType.FLOW_MERGE) {
                        return -1;
                    }
                    if (lineStyle2 == EConnectionType.FLOW_MAIN || lineStyle2 == EConnectionType.FLOW_MERGE) {
                        return 1;
                    }

                    // 3. others cases, the last
                    return 0;

                }
            });
        }

        return conns;
    }

    /**
     * DOC get the EConnectionType.FLOW_MAIN or EConnectionType.FLOW_REF out goning Connections<br/>
     * 
     * @param node
     * @return List<? extends IConnection>
     */
    public static List<? extends IConnection> getMainOutgoingConnections(INode node) {
        List<IConnection> conns = null;

        List<? extends IConnection> outgoingConnections = node.getOutgoingConnections();
        if (outgoingConnections != null) {
            conns = new ArrayList<IConnection>();

            for (int i = 0; i < outgoingConnections.size(); i++) {

                IConnection connection = outgoingConnections.get(i);
                if (connection.getLineStyle() == EConnectionType.FLOW_MAIN
                        || connection.getLineStyle() == EConnectionType.FLOW_REF) {

                    conns.add(connection);

                }
            }
        }
        return conns;
    }

    public static List<? extends IConnection> getOutgoingConnections(INode node, EConnectionType connectionType) {
        List<IConnection> conns = null;

        List<? extends IConnection> outgoingConnections = node.getOutgoingConnections();
        if (outgoingConnections != null) {
            conns = new ArrayList<IConnection>();

            for (int i = 0; i < outgoingConnections.size(); i++) {
                IConnection connection = outgoingConnections.get(i);
                if (connection.getLineStyle() == connectionType) {
                    conns.add(connection);
                }
            }
        }
        return conns;
    }

    public static List<? extends IConnection> getIncomingConnections(INode node, EConnectionType connectionType) {
        List<IConnection> conns = null;

        List<? extends IConnection> incomingConnections = node.getIncomingConnections();
        if (incomingConnections != null) {
            conns = new ArrayList<IConnection>();

            for (int i = 0; i < incomingConnections.size(); i++) {
                IConnection connection = incomingConnections.get(i);
                if (connection.getLineStyle() == connectionType) {
                    conns.add(connection);
                }
            }
        }
        return conns;
    }

    public static List<? extends IConnection> getOutgoingConnections(INode node, String connectorName) {
        List<IConnection> conns = null;

        List<? extends IConnection> outgoingConnections = node.getOutgoingConnections();
        if (outgoingConnections != null) {
            conns = new ArrayList<IConnection>();

            for (int i = 0; i < outgoingConnections.size(); i++) {

                IConnection connection = outgoingConnections.get(i);
                if (connectorName.equals(connection.getConnectorName())) {
                    conns.add(connection);
                }
            }
        }
        return conns;
    }

    /**
     * 
     * wzhang Comment method "getIncomingConnections".
     * 
     * @param node
     * @param category
     * @return
     */
    public static List<? extends IConnection> getIncomingConnections(INode node, int category) {
        List<IConnection> conns = null;

        List<? extends IConnection> incomingConnections = node.getIncomingConnections();
        if (incomingConnections != null) {
            conns = new ArrayList<IConnection>();

            for (int i = 0; i < incomingConnections.size(); i++) {

                IConnection connection = incomingConnections.get(i);
                if (connection.getLineStyle().hasConnectionCategory(category)) {
                    conns.add(connection);
                }
            }
        }
        return conns;
    }

    public static List<? extends IConnection> getOutgoingConnections(INode node, int category) {
        List<IConnection> conns = null;

        List<? extends IConnection> outgoingConnections = node.getOutgoingConnections();
        if (outgoingConnections != null) {
            conns = new ArrayList<IConnection>();

            for (int i = 0; i < outgoingConnections.size(); i++) {

                IConnection connection = outgoingConnections.get(i);
                if (connection.getLineStyle().hasConnectionCategory(category)) {
                    conns.add(connection);
                }
            }
        }
        return conns;
    }

    public static List<IMetadataTable> getIncomingMetadataTable(INode node, int category) {
        List<IMetadataTable> metadatas = new ArrayList<IMetadataTable>();

        List<? extends IConnection> incomingConnections = node.getIncomingConnections();
        if (incomingConnections != null) {
            for (int i = 0; i < incomingConnections.size(); i++) {

                IConnection connection = incomingConnections.get(i);
                if (connection.getLineStyle().hasConnectionCategory(category)) {
                    metadatas.add(connection.getMetadataTable());
                }
            }
        }
        return metadatas;
    }

    public static List<? extends IConnection> getIncomingConnections(INode node, String connectorName) {
        List<IConnection> conns = null;

        List<? extends IConnection> incomingConnections = node.getIncomingConnections();
        if (incomingConnections != null) {
            conns = new ArrayList<IConnection>();

            for (int i = 0; i < incomingConnections.size(); i++) {

                IConnection connection = incomingConnections.get(i);
                if (connectorName.equals(connection.getConnectorName())) {
                    conns.add(connection);
                }
            }
        }
        return conns;
    }

    public static boolean checkConnectionAfterNode(INode node, EConnectionType connectionType, List<INode> checkedNodes) {
        // fix bug 0004935: Error on job save
        if (checkedNodes.contains(node)) {
            return false;
        } else {
            checkedNodes.add(node);
        }

        boolean result = false;
        List<? extends IConnection> onErrorConns = getOutgoingConnections(node, EConnectionType.ON_COMPONENT_ERROR);
        if (onErrorConns == null || onErrorConns.size() == 0) {
            List<? extends IConnection> conns = getOutgoingSortedConnections(node);
            if (conns != null && conns.size() > 0) {
                for (IConnection conn : conns) {
                    result = checkConnectionAfterNode(conn.getTarget(), EConnectionType.ON_COMPONENT_ERROR, checkedNodes);
                    if (result) {
                        break;
                    }
                }
            } else {
                result = false;
            }
        } else {
            result = true;
        }
        return result;
    }

    public static boolean checkComponentErrorConnectionAfterNode(INode node) {
        List<INode> checkedNodes = new ArrayList<INode>();
        return checkConnectionAfterNode(node, EConnectionType.ON_COMPONENT_ERROR, checkedNodes);
    }

    /**
     * DOC xtan
     * <p>
     * InLineJob means all nodes after a iterate link(The nodes will execute many times on every iterate).
     * </p>
     * Notice: The search method don't consider the second branch of the tUnite, but it is ok.
     * 
     * @param node
     * @return
     */
    public static Set<? extends IConnection> getAllInLineJobConnections(INode node) {
        Set<String> uniqueNamesDone = new HashSet<String>();
        uniqueNamesDone.add(node.getUniqueName());
        return getAllInLineJobConnections(node, uniqueNamesDone);
    }

    private static Set<? extends IConnection> getAllInLineJobConnections(INode node, Set<String> uniqueNamesDone) {
        Set<IConnection> conns = new HashSet<IConnection>();

        List<? extends IConnection> outgoingConnections = node.getOutgoingConnections();
        if (outgoingConnections != null) {

            conns.addAll(outgoingConnections); // add all

            for (int i = 0; i < outgoingConnections.size(); i++) {

                IConnection connection = outgoingConnections.get(i);
                INode nextNode = connection.getTarget();

                if (!uniqueNamesDone.contains(nextNode.getUniqueName())) {
                    uniqueNamesDone.add(nextNode.getUniqueName());
                    conns.addAll(getAllInLineJobConnections(nextNode, uniqueNamesDone)); // follow this way
                }
            }
        }
        return conns;
    }

    public static INode getFirstMergeNode(INode node) {
        INode mergeNode = null;
        for (IConnection connection : node.getOutgoingConnections()) {
            if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.MERGE)) {
                mergeNode = connection.getTarget();
            } else if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                mergeNode = getFirstMergeNode(connection.getTarget());
            }
            if (mergeNode != null) {
                break;
            }
        }
        return mergeNode;
    }

    public static boolean isLastFromMergeTree(INode node) {
        INode firstMergeNode = getFirstMergeNode(node);
        int totMerge = NodeUtil.getIncomingConnections(firstMergeNode, IConnectionCategory.MERGE).size();
        Integer posMerge = node.getLinkedMergeInfo().get(firstMergeNode);
        return totMerge == posMerge;
    }

    public static IConnection getRealConnectionTypeBased(IConnection connection) {
        IConnection realConnection = connection;

        boolean connectionAvailable = true;

        //        while (connectionAvailable && realConnection.getSource().getComponent().getName().equals("tReplace")) { //$NON-NLS-1$

        while (connectionAvailable && !realConnection.getSource().isSubProcessStart()
                && realConnection.getSource().getComponent().isDataAutoPropagated()) {

            List<IConnection> inConnections = (List<IConnection>) getIncomingConnections(realConnection.getSource(),
                    IConnectionCategory.FLOW);
            if (inConnections.size() > 0) {
                realConnection = inConnections.get(0);
            } else {
                connectionAvailable = false;
            }
        }

        return realConnection;
    }

    /**
     * DOC wliu
     * <p>
     * judge if the current connection is the last output connection of the component
     * </p>
     * Notice: It is used in subtree_end.javajet. And the aim is for feature5996
     * 
     * @param connection
     * @return
     */
    public static boolean isLastMultiplyingOutputComponents(IConnection connection) {

        List<? extends IConnection> conns = connection.getSource().getOutgoingConnections();
        int last = 0;
        if (conns != null && conns.size() > 0) {
            for (int i = 0; i < conns.size(); i++) {
                if (conns.get(i).getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
                    last = i;
                }
            }
        }

        if (connection.getName().equals(conns.get(last).getName())) {
            return true;
        }

        return false;
    }

    public static Map<INode, Integer> getLinkedMergeInfo(final INode node) {
        Map<INode, Integer> map = new HashMap<INode, Integer>();

        getLinkedMergeInfo(node, map);

        if (map.isEmpty()) {
            // in case the component is not linked directly, it should take the status of previous component, since it
            // will be in the same branch.
            getLinkedMergeInfoFromMainLink(node, map);
        }

        return map;
    }

    private static void getLinkedMergeInfoFromMainLink(final INode node, final Map<INode, Integer> map) {
        if (node.getComponent().useMerge()) {
            return;
        }
        List<IConnection> inputConnections = (List<IConnection>) getIncomingConnections(node, IConnectionCategory.MAIN);
        if (inputConnections.size() > 0) {
            IConnection input = inputConnections.get(0);
            INode sourceNode = input.getSource();
            if (sourceNode.getJobletNode() != null) {
                sourceNode = sourceNode.getJobletNode();
            }
            getLinkedMergeInfo(sourceNode, map);
            if (map.isEmpty()) {
                getLinkedMergeInfoFromMainLink(sourceNode, map);
            }
        }

    }

    private static void getLinkedMergeInfo(final INode node, final Map<INode, Integer> map) {
        List<? extends IConnection> outgoingConnections = node.getOutgoingConnections();
        for (int i = 0; (i < outgoingConnections.size()); i++) {
            IConnection connec = outgoingConnections.get(i);
            if (connec.isActivate()) {
                if (connec.getLineStyle().hasConnectionCategory(EConnectionType.MERGE)) {
                    INode jNode = connec.getTarget();
                    if (jNode.getJobletNode() != null) {
                        jNode = jNode.getJobletNode();
                    }
                    map.put(jNode, connec.getInputId());
                    getLinkedMergeInfo(jNode, map);
                } else if (connec.getLineStyle().hasConnectionCategory(EConnectionType.MAIN) && connec.getTarget() != null) {
                    INode jNode = connec.getTarget();
                    if (jNode.getJobletNode() != null) {
                        jNode = jNode.getJobletNode();
                    }
                    getLinkedMergeInfo(jNode, map);
                }
            }
        }
    }

    // this is only for bug:11754, and only be used in generating code.
    public static boolean isDataAutoPropagated(final INode node) {
        IComponent component = node.getComponent();
        // if it is tJavaFlex, use the property Version_V2_0 to instead the DATA_AUTO_PROPAGATE="false" config
        // in tJavaFlex_java.xml
        if (component.getName().compareTo("tJavaFlex") == 0) {
            boolean isVersionV20 = "true".equals(ElementParameterParser.getValue(node, "__Version_V2_0__"));
            return isVersionV20;
        } else {
            return component.isDataAutoPropagated();
        }
    }
}
