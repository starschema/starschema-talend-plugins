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
package org.talend.core.model.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.utils.NodeUtil;
import org.talend.core.model.utils.ParameterValueUtil;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 * 
 * $Id: AbstractNode.java 51166 2010-11-11 06:09:01Z wchen $
 * 
 */
public abstract class AbstractNode implements INode {

    private String componentName;

    List<? extends IElementParameter> elementParameters;

    private List<? extends IConnection> outgoingConnections = new ArrayList<IConnection>();

    private List<? extends IConnection> incomingConnections = new ArrayList<IConnection>();

    private List<IMetadataTable> metadataList;

    private String uniqueName;

    private boolean activate;

    private boolean start;

    private boolean subProcessStart;

    private IProcess process;

    private IComponent component;

    private boolean readOnly;

    private IExternalNode externalNode;

    private boolean hasConditionalOutputs = Boolean.FALSE;

    private boolean isMultiplyingOutputs = Boolean.FALSE;

    private List<BlockCode> blocksCodeToClose;

    private boolean isThereLinkWithHash;

    private boolean isThereLinkWithMerge;

    private Map<INode, Integer> mergeInfo;

    private String label;

    protected List<? extends INodeConnector> listConnector;

    private INode designSubjobStartNode;

    private boolean isVirtualGenerateNode;
    
    private EConnectionType virtualLinkTo;

    private String uniqueShortName;

    private boolean subProcessContainBreakpoint;

    private List<ModuleNeeded> modulesNeeded = new ArrayList<ModuleNeeded>();

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public List<? extends IElementParameter> getElementParameters() {
        return elementParameters;
    }

    public void setElementParameters(List<? extends IElementParameter> elementParameters) {
        this.elementParameters = elementParameters;
    }

    public <T extends IElementParameter> void addElementParameter(T elementParameter) {
        ((List<T>) elementParameters).add(elementParameter);
    }

    public List<? extends IConnection> getIncomingConnections() {
        return incomingConnections;
    }

    public void setIncomingConnections(List<? extends IConnection> incomingConnections) {
        this.incomingConnections = incomingConnections;
    }

    public List<? extends IConnection> getOutgoingConnections() {
        return outgoingConnections;
    }

    public void setOutgoingConnections(List<? extends IConnection> outgoingConnections) {
        this.outgoingConnections = outgoingConnections;
    }

    public List<IMetadataTable> getMetadataList() {
        return metadataList;
    }

    public void setMetadataList(List<IMetadataTable> metadataList) {
        this.metadataList = metadataList;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        for (IElementParameter param : elementParameters) {
            if (param.getName().equals("UNIQUE_NAME")) { //$NON-NLS-1$
                param.setValue(uniqueName);
            }
        }
        this.uniqueName = uniqueName;
    }

    public boolean isActivate() {
        return activate;
    }

    public void setActivate(boolean activate) {
        this.activate = activate;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public boolean isSubProcessStart() {
        return subProcessStart;
    }

    public void setSubProcessStart(boolean subProcessStart) {
        this.subProcessStart = subProcessStart;
    }

    public void setPerformanceData(String perfData) {
        // null
    }

    public void setTraceData(String traceData) {
        // null
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#getReturns()
     */
    public List<? extends INodeReturn> getReturns() {
        return new ArrayList<INodeReturn>();
    }

    public IProcess getProcess() {
        return process;
    }

    public void setProcess(IProcess process) {
        this.process = process;
    }

    public void setComponent(IComponent component) {
        this.component = component;
    }

    public IComponent getComponent() {
        return component;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isOnMainMergeBranch() {
        // return true even if got no branch.
        Map<INode, Integer> mapMerge = NodeUtil.getLinkedMergeInfo(this);
        if (mapMerge == null) {
            return true;
        } else {
            for (Integer i : mapMerge.values()) {
                if (i != 1) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Will return the first item of the subprocess. If "withCondition" is true, if there is links from type RunIf /
     * RunAfter / RunBefore, it will return the first element found. If "withCondition" is false, it will return the
     * first element with no active link from type Main/Ref/Iterate.<br>
     * <i><b>Note:</b></i> This function doesn't work if the node has several start points (will return a random start
     * node).
     * 
     * @param withCondition
     * @return Start Node found.
     */
    public INode getSubProcessStartNode(boolean withConditions) {
        if (!withConditions) {
            Map<INode, Integer> mapMerge = NodeUtil.getLinkedMergeInfo(this);
            if (mapMerge == null) { // no merge after, so must be sub process start.
                if ((getCurrentActiveLinksNbInput(EConnectionType.MAIN) == 0)) {
                    return this;
                }
            } else {
                for (Integer i : mapMerge.values()) {
                    if (i != 1) {
                        // not first merge, so will take the last merge from the tree, and retrieve the main sub
                        // process start.
                        return mapMerge.keySet().iterator().next().getSubProcessStartNode(withConditions);
                    }
                }
                if ((getCurrentActiveLinksNbInput(EConnectionType.MAIN) == 0)) {
                    return this; // main branch here, so we got the correct sub process start.
                }
            }
        } else {
            int nb = 0;
            for (IConnection connection : getIncomingConnections()) {
                if (connection.isActivate()) {
                    nb++;
                }
            }
            if (nb == 0) {
                return this;
            }
        }
        IConnection connec;

        for (int j = 0; j < getIncomingConnections().size(); j++) {
            connec = getIncomingConnections().get(j);
            if (((AbstractNode) connec.getSource()).isOnMainMergeBranch()) {
                if (!connec.getLineStyle().equals(EConnectionType.FLOW_REF)) {
                    return connec.getSource().getSubProcessStartNode(withConditions);
                }
            }
        }
        return null;
    }

    private int getCurrentActiveLinksNbInput(int connectionCategory) {
        int nb = 0;
        for (IConnection connection : getIncomingConnections()) {
            if (connection.isActivate() && connection.getLineStyle().hasConnectionCategory(connectionCategory)) {
                nb++;
            }
        }
        return nb;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#hasConditionnalOutputs()
     */
    public boolean hasConditionalOutputs() {
        return this.hasConditionalOutputs;
    }

    /**
     * Sets the hasConditionnalOutputs.
     * 
     * @param hasConditionalOutputs the hasConditionnalOutputs to set
     */
    public void setHasConditionalOutputs(boolean hasConditionalOutputs) {
        this.hasConditionalOutputs = hasConditionalOutputs;
    }

    /**
     * Getter for isMultiplyingOutputs.
     * 
     * @return the isMultiplyingOutputs
     */
    public boolean isMultiplyingOutputs() {
        return isMultiplyingOutputs;
    }

    /**
     * Sets the isMultiplyingOutputs.
     * 
     * @param isMultiplyingOutputs the isMultiplyingOutputs to set
     */
    public void setIsMultiplyingOutputs(Boolean isMultiplyingOutputs) {
        this.isMultiplyingOutputs = isMultiplyingOutputs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(uniqueName + " - "); //$NON-NLS-1$
        buff.append("inputs:("); //$NON-NLS-1$
        for (int i = 0; i < incomingConnections.size(); i++) {
            buff.append(incomingConnections.get(i).getName());
            if (i < (incomingConnections.size() - 1)) {
                buff.append(","); //$NON-NLS-1$
            }
        }
        buff.append(") "); //$NON-NLS-1$
        buff.append("outputs:("); //$NON-NLS-1$
        for (int i = 0; i < outgoingConnections.size(); i++) {
            buff.append(outgoingConnections.get(i).getName());
            if (i < (outgoingConnections.size() - 1)) {
                buff.append(","); //$NON-NLS-1$
            }
        }
        buff.append(")"); //$NON-NLS-1$
        return buff.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#getLocation()
     */
    public Object getLocation() {
        return null;
    }

    public IExternalNode getExternalNode() {
        return externalNode;
    }

    public void setExternalNode(IExternalNode externalNode) {
        this.externalNode = externalNode;
    }

    /**
     * Getter for isThereLinkWithHash.
     * 
     * @return the isThereLinkWithHash
     */
    public boolean isThereLinkWithHash() {
        return isThereLinkWithHash;
    }

    /**
     * Sets the isThereLinkWithHash.
     * 
     * @param isThereLinkWithHash the isThereLinkWithHash to set
     */
    public void setThereLinkWithHash(boolean isThereLinkWithHash) {
        this.isThereLinkWithHash = isThereLinkWithHash;
    }

    public IElementParameter getElementParameter(String name) {
        if (name.contains(":")) { // look for the parent first, then will //$NON-NLS-1$
            // retrieve the children
            StringTokenizer token = new StringTokenizer(name, ":"); //$NON-NLS-1$
            String parentId = token.nextToken();
            String childId = token.nextToken();
            for (int i = 0; i < elementParameters.size(); i++) {
                if (elementParameters.get(i).getName().equals(parentId)) {
                    IElementParameter parent = elementParameters.get(i);
                    return parent.getChildParameters().get(childId);
                }
            }
        } else {
            for (IElementParameter elementParam : elementParameters) {
                if (elementParam.getName().equals(name)) {
                    return elementParam;
                }
            }
        }

        // if not found, look for the name if it's the name of a children
        // this code is added only for compatibility and will be executed only
        // one time
        // to initialize the child.
        // The parameters name are unique, so we just take the first one.
        for (IElementParameter elementParam : elementParameters) {
            for (String key : elementParam.getChildParameters().keySet()) {
                IElementParameter param = elementParam.getChildParameters().get(key);
                if (param.getName().equals(name)) {
                    return param;
                }
            }
        }
        return null;
    }

    public List<? extends IConnection> getOutgoingSortedConnections() {
        return org.talend.core.model.utils.NodeUtil.getOutgoingSortedConnections(this);
    }

    public List<? extends IConnection> getOutgoingCamelSortedConnections() {
        return org.talend.core.model.utils.NodeUtil.getOutgoingCamelSortedConnections(this);
    }

    public List<? extends IConnection> getMainOutgoingConnections() {
        return org.talend.core.model.utils.NodeUtil.getMainOutgoingConnections(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#getOutgoingConnections(org.talend .core.model.process.EConnectionType)
     */
    public List<? extends IConnection> getOutgoingConnections(EConnectionType connectionType) {
        return org.talend.core.model.utils.NodeUtil.getOutgoingConnections(this, connectionType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#getOutgoingConnections(java.lang. String)
     */
    public List<? extends IConnection> getOutgoingConnections(String connectorName) {
        return org.talend.core.model.utils.NodeUtil.getOutgoingConnections(this, connectorName);
    }

    public List<BlockCode> getBlocksCodeToClose() {
        return this.blocksCodeToClose;
    }

    public void setBlocksCodeToClose(List<BlockCode> blockCodesToClose) {
        this.blocksCodeToClose = blockCodesToClose;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#renameData(java.lang.String, java.lang.String)
     */
    public void renameData(String oldName, String newName) {
        if (oldName.equals(newName)) {
            return;
        }

        for (IElementParameter param : this.getElementParameters()) {
            if (param.getName().equals("UNIQUE_NAME") || isSQLQueryParameter(param)) { //$NON-NLS-1$
                continue;
            }
            ParameterValueUtil.renameValues(param, oldName, newName);
        }
    }

    /**
     * see bug 4733
     * <p>
     * DOC YeXiaowei Comment method "isSQLQueryParameter".
     * 
     * @param parameter
     * @return
     */
    private boolean isSQLQueryParameter(final IElementParameter parameter) {
        return parameter.getFieldType().equals(EParameterFieldType.MEMO_SQL) && parameter.getName().equals("QUERY"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#useData(java.lang.String)
     */
    public boolean useData(String name) {

        for (IElementParameter param : this.getElementParameters()) {
            if (param.getFieldType() == EParameterFieldType.IMAGE) {
                continue;
            }
            if (param.getName().equals("UNIQUE_NAME")) { //$NON-NLS-1$
                continue;
            }
            if (ParameterValueUtil.isUseData(param, name)) {
                return true;
            }
        }
        return false;
    }

    public Map<INode, Integer> getLinkedMergeInfo() {
        return mergeInfo;
    }

    public void setLinkedMergeInfo(Map<INode, Integer> mergeInfo) {
        this.mergeInfo = mergeInfo;
    }

    public boolean isThereLinkWithMerge() {
        return isThereLinkWithMerge;
    }

    public void setThereLinkWithMerge(boolean isThereLinkWithHash) {
        this.isThereLinkWithMerge = isThereLinkWithHash;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#getIncomingConnections(org.talend .core.model.process.EConnectionType)
     */
    public List<? extends IConnection> getIncomingConnections(EConnectionType connectionType) {
        return org.talend.core.model.utils.NodeUtil.getIncomingConnections(this, connectionType);
    }

    public List<? extends IConnection> getIncomingConnections(String connectorName) {
        return org.talend.core.model.utils.NodeUtil.getIncomingConnections(this, connectorName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#getLabel()
     */
    public String getLabel() {
        return label;
    }

    public IMetadataTable getMetadataFromConnector(String connector) {
        if (metadataList == null) {
            return null;
        }
        for (IMetadataTable table : metadataList) {
            if (table.getAttachedConnector().equals(connector)) {
                return table;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#getConnectorFromName(java.lang.String )
     */
    public INodeConnector getConnectorFromName(String connName) {
        INodeConnector nodeConnector = null;
        int nbConn = 0;

        while ((nodeConnector == null) && (nbConn < listConnector.size())) {
            if (listConnector.get(nbConn).getName().equals(connName)) {
                nodeConnector = listConnector.get(nbConn);
            }
            nbConn++;
        }
        return nodeConnector;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#reloadComponent(org.talend.core.model .components.IComponent,
     * java.util.Map)
     */
    public void reloadComponent(IComponent component, Map<String, Object> parameters) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#getDesignSubjobStart()
     */
    public INode getDesignSubjobStartNode() {
        if (designSubjobStartNode == null) {
            // used for process without design.
            return getSubProcessStartNode(false);
        }
        return designSubjobStartNode;
    }

    /**
     * Sets the designSubjobStart.
     * 
     * @param designSubjobStart the designSubjobStart to set
     */
    public void setDesignSubjobStartNode(INode designSubjobStartNode) {
        this.designSubjobStartNode = designSubjobStartNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#isDesignSubjobStartNode()
     */
    public boolean isDesignSubjobStartNode() {
        if (designSubjobStartNode == null) {
            return this.equals(getSubProcessStartNode(false));
        }
        return this.equals(designSubjobStartNode);
    }

    public boolean isVirtualGenerateNode() {
        return this.isVirtualGenerateNode;
    }

    public boolean setVirtualGenerateNode(boolean isVirtualGenerateNode) {
        return this.isVirtualGenerateNode = isVirtualGenerateNode;
    }

    /**
     * Sets the listConnector.
     * 
     * @param listConnector the listConnector to set
     */
    public void setListConnector(List<? extends INodeConnector> listConnector) {
        this.listConnector = listConnector;
    }

    public List<? extends IElementParameter> getElementParametersWithChildrens() {
        List<IElementParameter> fullListParam = new ArrayList<IElementParameter>(this.elementParameters);

        for (IElementParameter curParam : elementParameters) {
            if (curParam.getChildParameters() != null) {
                for (String key : curParam.getChildParameters().keySet()) {
                    IElementParameter childParam = curParam.getChildParameters().get(key);
                    fullListParam.add(childParam);
                }
            }
        }
        return fullListParam;
    }

    public boolean isGeneratedAsVirtualComponent() {
        IElementParameter param = getElementParameter("IS_VIRTUAL_COMPONENT"); //$NON-NLS-1$
        if (param != null) { // now only available for tUniqRow.
            return (Boolean) param.getValue();
        }
        List<IMultipleComponentManager> multipleComponentManagers = getComponent().getMultipleComponentManagers();
        for (IMultipleComponentManager mcm : multipleComponentManagers) {
            if (!mcm.isLookupMode()) {
                return true;
            }
        }
        return false;
    }
    
    public EConnectionType getVirtualLinkTo() {
        return this.virtualLinkTo;
    }

    public void setVirtualLinkTo(EConnectionType virtualLinkTo) {
        this.virtualLinkTo=virtualLinkTo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#isELTComponent()
     */
    public boolean isELTComponent() {
        if (getComponent() != null) {
            return getComponent().getOriginalFamilyName().startsWith("ELT"); //$NON-NLS-1$
        }
        return false;
    }

    public boolean isFileScaleComponent() {
        return getComponent().getOriginalFamilyName().equals("FileScale"); //$NON-NLS-1$
    }

    public boolean isUseLoopOnConditionalOutput(String outputName) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#getUniqueShortName()
     */
    public String getUniqueShortName() {
        return this.uniqueShortName;
    }

    /**
     * Sets the uniqueShortName.
     * 
     * @param uniqueShortName the uniqueShortName to set
     */
    public void setUniqueShortName(String uniqueShortName) {
        this.uniqueShortName = uniqueShortName;
    }

    public void setSubProcessContainTraceBreakpoint(boolean subProcessContainBreakpoint) {
        this.subProcessContainBreakpoint = subProcessContainBreakpoint;
    }

    public boolean isSubProcessContainTraceBreakpoint() {
        return subProcessContainBreakpoint;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElement#getElementName()
     */
    public String getElementName() {
        return this.getUniqueName();
    }

    public void addInput(IConnection connection) {
        // TODO Auto-generated method stub

    }

    public void addOutput(IConnection connection) {
        // TODO Auto-generated method stub

    }

    public boolean checkIfCanBeStart() {
        // TODO Auto-generated method stub
        return false;
    }

    public void checkNode() {
        // TODO Auto-generated method stub

    }

    public Set<INode> fsComponentsInProgressBar() {
        // TODO Auto-generated method stub
        return null;
    }

    public INodeConnector getConnectorFromType(EConnectionType lineStyle) {
        // TODO Auto-generated method stub
        return null;
    }

    public IExternalData getExternalData() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<? extends INodeConnector> getListConnector() {
        return listConnector;
    }

    public IMetadataTable getMetadataTable(String metaName) {
        // TODO Auto-generated method stub
        return null;
    }

    public INode getProcessStartNode(boolean processStartNode) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getSchemaParameterFromConnector(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getSize() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean hasRunIfLink() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isDummy() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isExternalNode() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isGeneratedByJobscriptBool() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isTemplate() {
        // TODO Auto-generated method stub
        return false;
    }

    public void metadataInputChanged(IODataComponent dataComponent, String connectionToApply) {
        // TODO Auto-generated method stub

    }

    public void metadataOutputChanged(IODataComponent dataComponent, String connectionToApply) {
        // TODO Auto-generated method stub

    }

    public void removeInput(IConnection connection) {
        // TODO Auto-generated method stub

    }

    public void removeOutput(IConnection connection) {
        // TODO Auto-generated method stub

    }

    public boolean sameProcessAs(INode target, boolean withConditions) {
        // TODO Auto-generated method stub
        return false;
    }

    public void setLocation(Object location) {
        // TODO Auto-generated method stub

    }

    public void setSize(Object size) {
        // TODO Auto-generated method stub

    }

    public Object getPropertyValue(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setPropertyValue(String name, Object value) {
        // TODO Auto-generated method stub

    }

    public IElementParameter getElementParameterFromField(EParameterFieldType propertyType, EComponentCategory category) {
        // TODO Auto-generated method stub
        return null;
    }

    public IElementParameter getElementParameterFromField(EParameterFieldType dbtable) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getPropertyValue(String name, String paramName) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setLabel(String label) {
        // TODO Auto-generated method stub

    }

    public int getPosX() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getPosY() {
        // TODO Auto-generated method stub
        return 0;
    }

    public INode getJobletNode() {
        return null;
    }

    public List<ModuleNeeded> getModulesNeeded() {
        if (modulesNeeded.isEmpty() && component != null && component.getModulesNeeded() != null) {
            // if the list is empty, initialize from the original component
            // this avoids complex refactor to initialize this list all the time, and add the possibility to add more
            // modules needed to one original component

            modulesNeeded.addAll(component.getModulesNeeded());
        }
        return modulesNeeded;
    }
}
