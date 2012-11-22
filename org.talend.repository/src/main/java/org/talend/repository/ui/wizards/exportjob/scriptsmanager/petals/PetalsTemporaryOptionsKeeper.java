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
package org.talend.repository.ui.wizards.exportjob.scriptsmanager.petals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * A singleton class in charge of storing additional Petals options.
 * <p>
 * Some of these options should be in fact part of the ExportChoice enumeration.<br />
 * OK, and by the time, this class somehow became a storage facility for global variables...
 * </p>
 * 
 * @author Vincent Zurczak - EBM WebSourcing
 */
public class PetalsTemporaryOptionsKeeper {

    private boolean singleton = true;

    private boolean generateEndpoint = false;

    private boolean validateByWsdl = false;

    private List<ContextTypeDefinition> contexts;

    private IStructuredSelection selection;

    public static final PetalsTemporaryOptionsKeeper INSTANCE = new PetalsTemporaryOptionsKeeper();

    /**
     * Private constructor (singleton pattern).
     */
    private PetalsTemporaryOptionsKeeper() {
        // nothing
    }

    /**
     * @return the singleton
     */
    public synchronized Boolean isSingleton() {
        return this.singleton;
    }

    /**
     * @param singleton the singleton to set
     */
    public synchronized void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    /**
     * @return the generateEndpoint
     */
    public synchronized Boolean isGenerateEndpoint() {
        return this.generateEndpoint;
    }

    /**
     * @param generateEndpoint the generateEndpoint to set
     */
    public synchronized void setGenerateEndpoint(boolean generateEndpoint) {
        this.generateEndpoint = generateEndpoint;
    }

    /**
     * @return the validateByWsdl
     */
    public boolean isValidateByWsdl() {
        return this.validateByWsdl;
    }

    /**
     * @param validateByWsdl the validateByWsdl to set
     */
    public void setValidateByWsdl(boolean validateByWsdl) {
        this.validateByWsdl = validateByWsdl;
    }

    /**
     * @return the contexts
     */
    public List<ContextTypeDefinition> getContexts() {
        return this.contexts != null ? this.contexts : new ArrayList<ContextTypeDefinition>(0);
    }

    /**
     * @param contexts the contexts to set
     */
    public void setContexts(List<ContextTypeDefinition> contexts) {
        this.contexts = contexts;
    }

    /**
     * @return the selection
     */
    public IStructuredSelection getSelection() {
        return this.selection;
    }

    /**
     * @param selection the selection to set
     */
    public void setSelection(IStructuredSelection selection) {
        this.selection = selection;
    }
}
