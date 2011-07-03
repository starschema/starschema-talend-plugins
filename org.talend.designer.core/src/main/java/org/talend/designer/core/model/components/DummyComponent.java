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
package org.talend.designer.core.model.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.INodeReturn;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.temp.ECodePart;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

/**
 * DOC talend class global comment. Detailled comment
 */
public class DummyComponent extends AbstractComponent {

    protected ImageDescriptor icon32;

    private NodeType nType;

    public DummyComponent(NodeType nType) {
        icon32 = ImageProvider.getImageDesc(EImage.COMPONENT_MISSING);
        this.nType = nType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#canParallelize()
     */
    public boolean canParallelize() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#createConnectors(org.talend.core.model.process.INode)
     */
    public List<? extends INodeConnector> createConnectors(INode node) {
        List<INodeConnector> listConnector = new ArrayList<INodeConnector>();
        INodeConnector nodeConnector;
        int nbInput = 0;
        int nbOutput = 0;
        for (int i = 0; i < EConnectionType.values().length; i++) {
            EConnectionType currentType = EConnectionType.values()[i];

            if ((currentType == EConnectionType.FLOW_REF) || (currentType == EConnectionType.FLOW_MERGE)) {
                continue;
            }
            boolean exists = false;
            for (INodeConnector curNodeConn : listConnector) {
                if (curNodeConn.getDefaultConnectionType().equals(currentType)) {
                    exists = true;
                }
            }
            if (!exists) { // will add by default all connectors not defined in
                // the xml files
                nodeConnector = new NodeConnector(node);
                nodeConnector.setDefaultConnectionType(currentType);
                nodeConnector.setName(currentType.getName());
                nodeConnector.setBaseSchema(currentType.getName());
                nodeConnector.addConnectionProperty(currentType, currentType.getRGB(), currentType.getDefaultLineStyle());
                nodeConnector.setLinkName(currentType.getDefaultLinkName());
                nodeConnector.setMenuName(currentType.getDefaultMenuName());
                nodeConnector.setMaxLinkInput(0);
                nodeConnector.setMinLinkInput(0);
                nodeConnector.setMaxLinkOutput(0);
                nodeConnector.setMinLinkOutput(0);
                if (currentType == EConnectionType.FLOW_MAIN) {
                    nodeConnector.addConnectionProperty(EConnectionType.FLOW_REF, EConnectionType.FLOW_REF.getRGB(),
                            EConnectionType.FLOW_REF.getDefaultLineStyle());
                    nodeConnector.addConnectionProperty(EConnectionType.FLOW_MERGE, EConnectionType.FLOW_MERGE.getRGB(),
                            EConnectionType.FLOW_MERGE.getDefaultLineStyle());
                }
                listConnector.add(nodeConnector);
            }
        }
        INodeConnector mainConnector = null;
        for (INodeConnector connector : listConnector) {
            if (connector.getDefaultConnectionType().equals(EConnectionType.FLOW_MAIN)) {
                mainConnector = connector;
            }
        }

        return listConnector;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#createElementParameters(org.talend.core.model.process.INode)
     */
    public List<? extends IElementParameter> createElementParameters(INode node) {
        List<IElementParameter> listParam = new ArrayList<IElementParameter>();
        ElementParameter param = new ElementParameter(node);
        param.setName(EParameterName.UNIQUE_NAME.getName());
        param.setValue(""); //$NON-NLS-1$
        param.setDisplayName(EParameterName.UNIQUE_NAME.getDisplayName());
        param.setFieldType(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.MAIN);
        param.setNumRow(1);
        param.setReadOnly(true);
        param.setShow(true);
        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.STARTABLE.getName());
        param.setValue(true);
        param.setDisplayName("STARTABLE"); //$NON-NLS-1$
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.MAIN);
        param.setNumRow(1);
        param.setReadOnly(true);
        param.setShow(false);
        listParam.add(param);

        param = new ElementParameter(node);
        param.setName(EParameterName.UPDATE_COMPONENTS.getName());
        param.setValue(Boolean.TRUE);
        param.setDisplayName(EParameterName.UPDATE_COMPONENTS.getDisplayName());
        param.setFieldType(EParameterFieldType.CLOSED_LIST);
        param.setCategory(EComponentCategory.BASIC);
        param.setNumRow(5);
        param.setReadOnly(true);
        param.setShow(false);
        listParam.add(param);

        return listParam;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#createReturns()
     */
    public List<? extends INodeReturn> createReturns() {
        return new ArrayList<NodeReturn>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getAvailableCodeParts()
     */
    public List<ECodePart> getAvailableCodeParts() {
        return new ArrayList<ECodePart>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getComponentType()
     */
    public EComponentType getComponentType() {
        return EComponentType.DUMMY;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getIcon16()
     */
    public ImageDescriptor getIcon16() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getIcon24()
     */
    public ImageDescriptor getIcon24() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getIcon32()
     */
    public ImageDescriptor getIcon32() {
        return icon32;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getLongName()
     */
    public String getLongName() {
        return nType.getComponentName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getModulesNeeded()
     */
    public List<ModuleNeeded> getModulesNeeded() {
        return new ArrayList<ModuleNeeded>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getMultipleComponentManagers()
     */
    public List<IMultipleComponentManager> getMultipleComponentManagers() {
        return Collections.EMPTY_LIST;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getName()
     */
    public String getName() {
        return nType.getComponentName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getOriginalFamilyName()
     */
    public String getOriginalFamilyName() {
        // TODO Auto-generated method stub
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getPathSource()
     */
    public String getPathSource() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getPluginDependencies()
     */
    public List<String> getPluginDependencies() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getPluginFullName()
     */
    public String getPluginFullName() {
        return IComponentsFactory.COMPONENTS_LOCATION;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getTranslatedFamilyName()
     */
    public String getTranslatedFamilyName() {
        // TODO Auto-generated method stub
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getVersion()
     */
    public String getVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#hasConditionalOutputs()
     */
    public boolean hasConditionalOutputs() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isDataAutoPropagated()
     */
    public boolean isDataAutoPropagated() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isHashComponent()
     */
    public boolean isHashComponent() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isLoaded()
     */
    public boolean isLoaded() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isMainCodeCalled()
     */
    public boolean isMainCodeCalled() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isMultipleOutput()
     */
    public boolean isMultipleOutput() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isMultiplyingOutputs()
     */
    public boolean isMultiplyingOutputs() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isSchemaAutoPropagated()
     */
    public boolean isSchemaAutoPropagated() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isSingleton()
     */
    public boolean isSingleton() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isTechnical()
     */
    public boolean isTechnical() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isVisible()
     */
    public boolean isVisible() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isVisible(java.lang.String)
     */
    public boolean isVisible(String family) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isVisibleInComponentDefinition()
     */
    public boolean isVisibleInComponentDefinition() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#setIcon16(org.eclipse.jface.resource.ImageDescriptor)
     */
    public void setIcon16(ImageDescriptor icon16) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#setIcon24(org.eclipse.jface.resource.ImageDescriptor)
     */
    public void setIcon24(ImageDescriptor icon24) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#setIcon32(org.eclipse.jface.resource.ImageDescriptor)
     */
    public void setIcon32(ImageDescriptor icon32) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#useImport()
     */
    public boolean useImport() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#useLookup()
     */
    public boolean useLookup() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#useMerge()
     */
    public boolean useMerge() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getShortName()
     */
    public String getShortName() {
        return "dc";
    }

    public String getCombine() {
        // TODO Auto-generated method stub
        return null;
    }

    public IProcess getProcess() {
        // TODO Auto-generated method stub
        return null;
    }

}
