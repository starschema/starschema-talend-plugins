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
package org.talend.core.model.context;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextParameter;

/**
 * Context of a process.
 * 
 * $Id: JobContext.java 38013 2010-03-05 14:21:59Z mhirt $
 * 
 */
public class JobContext implements IContext, Cloneable {

    String name;

    boolean confirmationNeeded;

    public JobContext(String name) {
        this.name = name;
    }

    List<IContextParameter> contextParameterList = new ArrayList<IContextParameter>();

    public List<IContextParameter> getContextParameterList() {
        return this.contextParameterList;
    }

    public void setContextParameterList(final List<IContextParameter> contextParameterList) {
        this.contextParameterList = contextParameterList;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isConfirmationNeeded() {
        return this.confirmationNeeded;
    }

    public void setConfirmationNeeded(boolean confirmationNeeded) {
        this.confirmationNeeded = confirmationNeeded;
    }

    public IContext clone() {
        IContext clonedContext = null;
        try {
            clonedContext = (IContext) super.clone();
            List<IContextParameter> clonedContextParametersList = new ArrayList<IContextParameter>();
            clonedContext.setContextParameterList(clonedContextParametersList);
            for (int i = 0; i < contextParameterList.size(); i++) {
                clonedContextParametersList.add(contextParameterList.get(i).clone());
            }
        } catch (CloneNotSupportedException e) {
            // nothing
        }
        return clonedContext;
    }

    public boolean sameAs(IContext context) {
        if (!context.getName().equals(name)) {
            return false;
        }
        if (contextParameterList.size() != context.getContextParameterList().size()) {
            return false;
        }
        for (int i = 0; i < contextParameterList.size(); i++) {
            IContextParameter curContextParam = contextParameterList.get(i);
            IContextParameter testContextParam = context.getContextParameterList().get(i);
            if (!curContextParam.sameAs(testContextParam)) {
                return false;
            }
        }
        return true;
    }

    public IContextParameter getContextParameter(String parameterName) {
        for (IContextParameter contextParam : contextParameterList) {
            if (contextParam.getName() != null && contextParam.getName().equals(parameterName)) {
                return contextParam;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer(name + ":"); //$NON-NLS-1$
        buff.append("("); //$NON-NLS-1$
        for (int i = 0; i < contextParameterList.size(); i++) {
            IContextParameter param = contextParameterList.get(i);
            buff.append(param.getName() + "="); //$NON-NLS-1$
            buff.append(param.getValue());
            if (i < (contextParameterList.size() - 1)) {
                buff.append(","); //$NON-NLS-1$
            }
        }
        buff.append(")"); //$NON-NLS-1$
        return buff.toString();
    }
}
