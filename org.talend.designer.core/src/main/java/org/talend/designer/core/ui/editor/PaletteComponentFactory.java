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
package org.talend.designer.core.ui.editor;

import org.eclipse.gef.requests.CreationFactory;
import org.talend.core.model.components.IComponent;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * Factory used to create a new Node. <br/>
 * 
 * $Id: PaletteComponentFactory.java 77219 2012-01-24 01:14:15Z mhirt $
 * 
 */
public class PaletteComponentFactory implements CreationFactory {

    private IComponent component;

    public PaletteComponentFactory(IComponent c) {
        component = c;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.requests.CreationFactory#getNewObject()
     */
    public Object getNewObject() {
        return new Node(component);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.requests.CreationFactory#getObjectType()
     */
    public Object getObjectType() {
        return null;
    }

    public String getCombinedFamilyName() {
        return component.getOriginalFamilyName();
    }

}
