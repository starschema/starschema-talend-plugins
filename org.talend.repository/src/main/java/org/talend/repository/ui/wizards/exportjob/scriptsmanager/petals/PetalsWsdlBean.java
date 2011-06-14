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

import java.util.Collections;
import java.util.List;

/**
 * A bean used to pass information to the WSDL's JET template.
 * 
 * @author Vincent Zurczak - EBM WebSourcing
 */
public class PetalsWsdlBean {

    /**
     * The schema of the tPetalsInput.
     */
    List<ElementTypeDefinition> tPetalsInputSchema = Collections.emptyList();

    /**
     * The schema of the tPetalsInput.
     */
    List<ElementTypeDefinition> tPetalsOutputSchema = Collections.emptyList();

    /**
     * The job contexts.
     */
    List<ContextTypeDefinition> contextDefinitions;

    /**
     * The job name.
     */
    String jobName;

    // VZ
    /**
     * The job version.
     */
    String jobVersion;

    // VZ

    /**
     * Generate the end-point?
     */
    boolean autogenerate;

    /**
     * @param tPetalsInputSchema the tPetalsInputSchema to set
     */
    public void settPetalsInputSchema(List<ElementTypeDefinition> tPetalsInputSchema) {
        this.tPetalsInputSchema = tPetalsInputSchema;
    }

    /**
     * @param tPetalsOutputSchema the tPetalsOutputSchema to set
     */
    public void settPetalsOutputSchema(List<ElementTypeDefinition> tPetalsOutputSchema) {
        this.tPetalsOutputSchema = tPetalsOutputSchema;
    }

    /**
     * @param contextDefinitions the contextDefinitions to set
     */
    public void setContextDefinitions(List<ContextTypeDefinition> contextDefinitions) {
        this.contextDefinitions = contextDefinitions;
    }

    /**
     * @param jobName the jobName to set
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * @param autogenerate the autogenerate to set
     */
    public void setAutogenerate(boolean autogenerate) {
        this.autogenerate = autogenerate;
    }

    // VZ
    /**
     * @param jobVersion the jobVersion to set
     */
    public void setJobVersion(String jobVersion) {
        this.jobVersion = jobVersion;
    }
    // VZ
}
