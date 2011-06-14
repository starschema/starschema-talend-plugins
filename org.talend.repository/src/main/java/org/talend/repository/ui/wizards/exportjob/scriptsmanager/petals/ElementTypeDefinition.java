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
public class ElementTypeDefinition {

    public static final int UNBOUNDED = -1;

    String name, type, defaultValue;

    boolean nillable = false;

    int minOccurs = 0, maxOccurs = 1;

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @param nillable the nillable to set
     */
    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    /**
     * @param minOccurs the minOccurs to set
     */
    public void setMinOccurs(int minOccurs) {
        this.minOccurs = minOccurs;
    }

    /**
     * @param maxOccurs the maxOccurs to set
     */
    public void setMaxOccurs(int maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;

        if (this.defaultValue != null && this.defaultValue.length() > 0) {
            char firstChar = this.defaultValue.charAt(0);
            if (firstChar == '\'' || firstChar == '\"')
                this.defaultValue = this.defaultValue.substring(1);

            if (this.defaultValue.length() > 1) {
                char lastChar = this.defaultValue.charAt(this.defaultValue.length() - 1);
                if (lastChar == '\'' || lastChar == '\"')
                    this.defaultValue = this.defaultValue.substring(0, this.defaultValue.length() - 1);
            } else if (this.defaultValue.equals("'") || this.defaultValue == "\"") //$NON-NLS-1$ //$NON-NLS-2$
                this.defaultValue = ""; //$NON-NLS-1$
        }

        if (this.defaultValue != null && this.defaultValue.trim().length() == 0)
            this.defaultValue = null;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the type
     */
    public String getType() {
        return this.type;
    }
}
