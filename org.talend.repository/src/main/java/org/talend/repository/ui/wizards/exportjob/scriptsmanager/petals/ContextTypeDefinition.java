// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.wizards.exportjob.scriptsmanager.petals;

/**
 * @author Vincent Zurczak - EBM WebSourcing
 */
public class ContextTypeDefinition {

    ElementTypeDefinition definition;

    ContextExportType exportType;

    boolean attachmentSupported = false;

    /**
     * @param definition the definition to set
     */
    public void setDefinition(ElementTypeDefinition definition) {
        this.definition = definition;
    }

    /**
     * @param exportType the exportType to set
     */
    public void setExportType(ContextExportType exportType) {
        this.exportType = exportType;
    }

    /**
     * @return the definition
     */
    public ElementTypeDefinition getDefinition() {
        return this.definition;
    }

    /**
     * @return the exportType
     */
    public ContextExportType getExportType() {
        return this.exportType;
    }

    /**
     * @param attachmentSupported the attachmentSupported to set
     */
    public void setAttachmentSupported(boolean attachmentSupported) {
        this.attachmentSupported = attachmentSupported;
    }
}
