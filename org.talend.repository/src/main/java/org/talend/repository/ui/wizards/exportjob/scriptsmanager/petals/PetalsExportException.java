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
 * An exception specific to the Petals export.
 * 
 * @author Vincent Zurczak - EBM WebSourcing
 */
public class PetalsExportException extends Exception {

    /**
     * The serial ID.
     */
    private static final long serialVersionUID = -6416783862881196911L;

    /**
     * Constructor.
     * 
     * @param message
     * @param cause
     */
    public PetalsExportException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     */
    public PetalsExportException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     */
    public PetalsExportException(Throwable cause) {
        super(cause);
    }
}
