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
package org.talend.designer.core.ui.editor.properties.nodes;

import org.talend.core.model.process.EComponentCategory;
import org.talend.designer.core.ui.editor.properties.DynamicTabbedPropertySection;

/**
 * Section for the "view part" of the component.
 * 
 * $Id: ViewNodeSection.java 77219 2012-01-24 01:14:15Z mhirt $
 * 
 */
public class ViewNodeSection extends DynamicTabbedPropertySection {

    public ViewNodeSection() {
        super(EComponentCategory.VIEW);
    }

}
