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

package org.talend.repository.ui.wizards.exportjob.scriptsmanager.petals;

import org.talend.repository.i18n.Messages;

/**
 * The different ways a context can be exported for Petals.
 * 
 * @author Vincent Zurczak - EBM WebSourcing
 */
public enum ContextExportType {

    NOT_EXPORTED,
    PARAMETER,
    IN_ATTACHMENT,
    OUT_ATTACHMENT,
    PARAMETER_AND_OUT_ATTACHMENT;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {

        String result;
        switch (this) {
        case NOT_EXPORTED:
            result = "Not exported"; //$NON-NLS-1$
            break;
        case IN_ATTACHMENT:
            result = "In-Attachment"; //$NON-NLS-1$
            break;
        case PARAMETER:
            result = "Parameter"; //$NON-NLS-1$
            break;
        case OUT_ATTACHMENT:
            result = "Out-Attachment"; //$NON-NLS-1$
            break;
        case PARAMETER_AND_OUT_ATTACHMENT:
            result = "Parameter and Out-Attachment"; //$NON-NLS-1$
            break;
        default:
            result = ""; //$NON-NLS-1$
        }

        return result;
    }

    /**
     * Gets a context export type from a string.
     * 
     * @param value
     * @return the matching export type
     * @throws IllegalArgumentException if the value is null or if it could not be resolved
     */
    public static ContextExportType resolve(String value) {

        if (value == null)
            throw new IllegalArgumentException(Messages.getString("ContextExportType_NullCannotBeMapped")); //$NON-NLS-1$

        for (ContextExportType type : ContextExportType.values()) {
            if (value.equals(type.toString()))
                return type;
        }

        throw new IllegalArgumentException("'" + value + Messages.getString("ContextExportType_CannotBeMapped")); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
