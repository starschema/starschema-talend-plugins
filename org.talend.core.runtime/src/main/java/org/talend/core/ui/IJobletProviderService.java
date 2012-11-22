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
package org.talend.core.ui;

import java.util.List;
import java.util.Map;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.talend.core.IService;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * cli class global comment. Detailled comment
 */
public interface IJobletProviderService extends IService {

    public boolean isJobletComponent(INode node);

    public boolean isJobletInOutComponent(INode node);

    public Property getJobletComponentItem(INode node);

    public Property getJobletComponentItem(IComponent component);

    public void reloadJobletProcess(INode node);

    public boolean isTriggerNode(INode node);

    public boolean isTriggerInputNode(INode node);

    public boolean canConnectTriggerNode(INode node, EConnectionType connType);

    public List<EConnectionType> getTriggerNodeSupportConnTypes(INode triggerNode);

    public List<INode> getTriggerNodes(INode jobletNode);

    public EConnectionType getTriggerNodeConnType(INode triggerNode);

    public boolean isBuiltTriggerConnector(INode jobletNode, INodeConnector connector);

    public List<INodeConnector> getTriggerBuiltConnectors(INode jobletNode, EConnectionType lineStyle, boolean input);

    public List<INodeConnector> getFreeTriggerBuiltConnectors(INode jobletNode, EConnectionType lineStyle, boolean input);

    public List<INode> getConnNodesForInputTrigger(INode jobletNode, IElementParameter param);

    public void upateJobletComonentList(INode jobletNode);

    public void loadComponentsFromProviders();

    public IComponent setPropertyForJobletComponent(String id, String version);

    public void updateParametersFromJoblet(INode node, IComponent newComponent);

    public ProcessType getJobletProcess(NodeType node);

    public IEditorPart openJobletItem(JobletProcessItem item);

    public SelectionAction getMoveToJobletAction(IWorkbenchPart part, INode jobletNode, Map<INode, IConnection> nodeMap);

    public SelectionAction getMoveToJobAction(IWorkbenchPart part);

    public MultiPageEditorPart openJobletEditor(INode jobletNode, IWorkbenchPage page);

    public void saveJobletNode(JobletProcessItem jobletItem, IElement jobletContainer);

    public boolean isLock(INode in);

    public boolean isReadOnly(INode in);

    public void lockJoblet(INode node);

    public void updateJobletCom(INode node);

    public void unlockJoblet(INode node, boolean needAsk);

    public List<INode> checkAddNodes(IElement jobletContainer);

    public List<INode> checkDeleteNodes(IElement jobletContainer);

    public boolean checkModify(IElement jobletContainer);
}
