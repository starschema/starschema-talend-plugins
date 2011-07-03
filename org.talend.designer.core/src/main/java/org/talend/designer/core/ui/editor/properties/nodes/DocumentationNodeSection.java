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
package org.talend.designer.core.ui.editor.properties.nodes;

import org.talend.core.model.process.EComponentCategory;
import org.talend.designer.core.ui.editor.properties.DynamicTabbedPropertySection;

/**
 * Section that will be used for the documentation of a component.
 * 
 * $Id: DocumentationNodeSection.java 54939 2011-02-11 01:34:57Z mhirt $
 * 
 */
public class DocumentationNodeSection extends DynamicTabbedPropertySection {

    public DocumentationNodeSection() {
        super(EComponentCategory.DOC);
    }
}
