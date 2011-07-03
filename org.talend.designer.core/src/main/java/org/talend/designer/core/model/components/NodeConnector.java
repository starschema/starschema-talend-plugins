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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.RGB;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnectionProperty;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.utils.DesignerColorUtils;

/**
 * Defines connector type and name for each component. <br/>
 * 
 * $Id: NodeConnector.java 54939 2011-02-11 01:34:57Z mhirt $
 * 
 */
public class NodeConnector implements INodeConnector {

    private EConnectionType defaultConnectionType;

    private int maxLinkOutput = -1;

    private int minLinkOutput = 0;

    private int maxLinkInput = -1;

    private int minLinkInput = 0;

    private int curLinkNbOutput = 0;

    private int curLinkNbInput = 0;

    private boolean builtIn = false;

    private String name;

    private String menuName;

    private String linkName;

    private String baseSchema;

    private Map<EConnectionType, IConnectionProperty> propertyMap = new HashMap<EConnectionType, IConnectionProperty>();

    private boolean multiSchema = false;

    private INode parentNode;

    private String notShowIf = null;

    private String showIf = null;

    private boolean show = true;

    private boolean mergeAllowDifferentSchema;

    public NodeConnector(INode parentNode) {
        super();
        this.parentNode = parentNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.components.INodeConnector#getConnectionType()
     */
    public EConnectionType getDefaultConnectionType() {
        return this.defaultConnectionType;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.talend.designer.core.model.components.INodeConnector#setConnectionType(org.talend.core.model.designer.
     * EConnectionType)
     */
    public void setDefaultConnectionType(final EConnectionType defaultConnectionType) {
        this.defaultConnectionType = defaultConnectionType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.components.INodeConnector#isBuiltIn()
     */
    public boolean isBuiltIn() {
        return this.builtIn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.components.INodeConnector#setBuiltIn(boolean)
     */
    public void setBuiltIn(final boolean builtIn) {
        this.builtIn = builtIn;
    }

    public int getCurLinkNbInput() {
        return this.curLinkNbInput;
    }

    public void setCurLinkNbInput(int curLinkNbInput) {
        this.curLinkNbInput = curLinkNbInput;
    }

    public int getCurLinkNbOutput() {
        return this.curLinkNbOutput;
    }

    public void setCurLinkNbOutput(int curLinkNbOutput) {
        this.curLinkNbOutput = curLinkNbOutput;
    }

    public int getMaxLinkInput() {
        return this.maxLinkInput;
    }

    public void setMaxLinkInput(int maxLinkInput) {
        this.maxLinkInput = maxLinkInput;
    }

    public int getMaxLinkOutput() {
        if (parentNode != null) {
            // && defaultConnectionType.equals(EConnectionType.FLOW_MAIN)) {
            IElementParameter param = parentNode.getElementParameter(EParameterName.PARALLELIZE.getName());
            if (param != null && ((Boolean) param.getValue())) {
                return 0;
            }
        }
        return this.maxLinkOutput;
    }

    public void setMaxLinkOutput(int maxLinkOutput) {
        this.maxLinkOutput = maxLinkOutput;
    }

    public int getMinLinkInput() {
        return this.minLinkInput;
    }

    public void setMinLinkInput(int minLinkInput) {
        this.minLinkInput = minLinkInput;
    }

    public int getMinLinkOutput() {
        return this.minLinkOutput;
    }

    public void setMinLinkOutput(int minLinkOutput) {
        this.minLinkOutput = minLinkOutput;
    }

    /**
     * Getter for linkName.
     * 
     * @return the linkName
     */
    public String getLinkName() {
        return linkName;
    }

    /**
     * Sets the linkName.
     * 
     * @param linkName the linkName to set
     */
    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    /**
     * Getter for menuName.
     * 
     * @return the menuName
     */
    public String getMenuName() {
        return menuName;
    }

    /**
     * Sets the menuName.
     * 
     * @param menuName the menuName to set
     */
    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    /**
     * Getter for name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public void addConnectionProperty(EConnectionType type, RGB rgb, Integer lineStyle) {
        rgb = DesignerColorUtils.getPreferenceConnectionColor(type, rgb);
        propertyMap.put(type, new ConnectionProperty(rgb, lineStyle));
    }

    public IConnectionProperty getConnectionProperty(EConnectionType type) {
        return propertyMap.get(type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (name.equals(defaultConnectionType.getName())) {
            return name + ": inputs(" + curLinkNbInput + "/" + maxLinkInput + "), outputs(" + curLinkNbOutput + "/" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    + maxLinkOutput + ")"; //$NON-NLS-1$
        }
        return name + "(" + defaultConnectionType.getName() + ")" + ": inputs(" + curLinkNbInput + "/" + maxLinkInput //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + "), outputs(" + curLinkNbOutput + "/" + maxLinkOutput + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * DOC nrousseau NodeConnector class global comment. Detailled comment <br/>
     * 
     */
    private class ConnectionProperty implements IConnectionProperty {

        private Integer lineStyle;

        private RGB rgb;

        private String linkedComponent;

        public ConnectionProperty(RGB rgb, Integer lineStyle) {
            super();
            this.lineStyle = lineStyle;
            this.rgb = rgb;
        }

        /**
         * Getter for color.
         * 
         * @return the color
         */
        public RGB getRGB() {
            return rgb;
        }

        /**
         * Sets the color.
         * 
         * @param color the color to set
         */
        public void setRGB(RGB rgb) {
            this.rgb = rgb;
        }

        /**
         * Getter for lineStyle.
         * 
         * @return the lineStyle
         */
        public Integer getLineStyle() {
            return lineStyle;
        }

        /**
         * Sets the lineStyle.
         * 
         * @param lineStyle the lineStyle to set
         */
        public void setLineStyle(Integer lineStyle) {
            this.lineStyle = lineStyle;
        }

        /**
         * Getter for linkedComponent.
         * 
         * @return the linkedComponent
         */
        public String getLinkedComponent() {
            return this.linkedComponent;
        }

        /**
         * Sets the linkedComponent.
         * 
         * @param linkedComponent the linkedComponent to set
         */
        public void setLinkedComponent(String linkedComponent) {
            this.linkedComponent = linkedComponent;
        }
    }

    /**
     * Getter for baseSchema.
     * 
     * @return the baseSchema
     */
    public String getBaseSchema() {
        return baseSchema;
    }

    /**
     * Sets the baseSchema.
     * 
     * @param baseSchema the baseSchema to set
     */
    public void setBaseSchema(String baseSchema) {
        this.baseSchema = baseSchema;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INodeConnector#isMultiSchema()
     */
    public boolean isMultiSchema() {
        return this.multiSchema;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INodeConnector#setMultiSchema(boolean)
     */
    public void setMultiSchema(boolean multiSchema) {
        this.multiSchema = multiSchema;
    }

    public String getNotShowIf() {
        return this.notShowIf;
    }

    public void setNotShowIf(String notShowIf) {
        this.notShowIf = notShowIf;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INodeConnector#isMergeAllowDifferentSchema()
     */
    public boolean isMergeAllowDifferentSchema() {
        return mergeAllowDifferentSchema;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INodeConnector#setMergeAllowDifferentSchema(boolean)
     */
    public void setMergeAllowDifferentSchema(boolean mergeOption) {
        this.mergeAllowDifferentSchema = mergeOption;
    }

    public boolean isShow() {
        String notShowIf = getNotShowIf();
        String showIf = getShowIf();
        if (notShowIf != null && !("".equals(notShowIf))) {
            List<? extends IElementParameter> listParam = parentNode.getElementParameters();
            show = !Expression.evaluate(notShowIf, listParam, null);
        } else if (showIf != null && !("".equals(showIf))) {
            List<? extends IElementParameter> listParam = parentNode.getElementParameters();
            show = Expression.evaluate(showIf, listParam, null);
        }
        return this.show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public String getShowIf() {
        return this.showIf;
    }

    public void setShowIf(String showIf) {
        this.showIf = showIf;
    }
}
