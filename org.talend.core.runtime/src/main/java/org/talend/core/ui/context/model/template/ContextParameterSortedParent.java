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
package org.talend.core.ui.context.model.template;

/**
 * ggu class global comment. Detailled comment
 */
public class ContextParameterSortedParent extends ContextParameterParent {

    private String sourceId;

    private String projectLabel;

    public ContextParameterSortedParent() {
        super();
    }

    public String getSourceId() {
        return this.sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * Getter for projectLabel.
     * 
     * @return the projectLabel
     */
    public String getProjectLabel() {
        return this.projectLabel;
    }

    /**
     * Sets the projectLabel.
     * 
     * @param projectLabel the projectLabel to set
     */
    public void setProjectLabel(String projectLabel) {
        this.projectLabel = projectLabel;
    }

}
