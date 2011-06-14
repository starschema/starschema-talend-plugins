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
package org.talend.repository.ui.actions.importproject;

import org.talend.core.language.ECodeLanguage;

/**
 * DOC Administrator class global comment. Detailled comment <br/>
 * 
 */
public class DemoProjectBean {

    private String projectName;

    private ECodeLanguage language;

    private String demoProjectFilePath;

    private String descriptionFilePath;

    private EDemoProjectFileType demoProjectFileType;

    /**
     * Getter for archiveFilePath.
     * 
     * @return the archiveFilePath
     */
    public String getDemoProjectFilePath() {
        return demoProjectFilePath;
    }

    /**
     * Sets the archiveFilePath.
     * 
     * @param archiveFilePath the archiveFilePath to set
     */
    public void setDemoProjectFilePath(String archiveFilePath) {
        this.demoProjectFilePath = archiveFilePath;
    }

    /**
     * Getter for language.
     * 
     * @return the language
     */
    public ECodeLanguage getLanguage() {
        return language;
    }

    /**
     * Sets the language.
     * 
     * @param language the language to set
     */
    public void setLanguage(ECodeLanguage language) {
        this.language = language;
    }

    /**
     * Getter for projectName.
     * 
     * @return the projectName
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Sets the projectName.
     * 
     * @param projectName the projectName to set
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescriptionFilePath() {
        return this.descriptionFilePath;
    }

    public void setDescriptionFilePath(String descriptionFilePath) {
        this.descriptionFilePath = descriptionFilePath;
    }

    /**
     * Getter for demoProjectFileType.
     * 
     * @return the demoProjectFileType
     */
    public EDemoProjectFileType getDemoProjectFileType() {
        return demoProjectFileType;
    }

    /**
     * Sets the demoProjectFileType.
     * 
     * @param demoProjectFileType the demoProjectFileType to set
     */
    public void setDemoProjectFileType(EDemoProjectFileType demoProjectFileType) {
        this.demoProjectFileType = demoProjectFileType;
    }
}
