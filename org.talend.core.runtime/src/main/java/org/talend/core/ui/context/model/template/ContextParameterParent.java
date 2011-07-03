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

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.process.IContextParameter;

/**
 * ggu class global comment. Detailled comment
 */
public class ContextParameterParent {

    private IContextParameter parameter;

    private List<ContextParameterSon> son = new ArrayList<ContextParameterSon>();

    public ContextParameterParent() {
        super();
    }

    public IContextParameter getParameter() {
        return this.parameter;
    }

    public void setParameter(IContextParameter parameter) {
        this.parameter = parameter;
    }

    public List<ContextParameterSon> getSon() {
        return this.son;
    }

}
