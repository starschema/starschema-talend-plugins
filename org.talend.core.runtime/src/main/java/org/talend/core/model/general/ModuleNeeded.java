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
package org.talend.core.model.general;

import java.util.List;

/**
 * This bean is use to manage needed moduless (perl) and libraries (java).<br/>
 * 
 * $Id: ModuleNeeded.java 38013 2010-03-05 14:21:59Z mhirt $
 * 
 */
public class ModuleNeeded {

    private String context;

    private String moduleName;

    private String informationMsg;

    private boolean required;

    private ELibraryInstallStatus status = ELibraryInstallStatus.UNKNOWN;

    private boolean isShow = true;

    List<InstallModule> installModule;

    List<String> installURL;

    public static final String SINGLE_QUOTE = "'"; //$NON-NLS-1$

    public static final String QUOTATION_MARK = "\""; //$NON-NLS-1$

    /**
     * DOC smallet ModuleNeeded class global comment. Detailled comment <br/>
     * 
     * $Id: ModuleNeeded.java 38013 2010-03-05 14:21:59Z mhirt $
     * 
     */
    public enum ELibraryInstallStatus {
        UNKNOWN,
        INSTALLED,
        UNUSED,
        NOT_INSTALLED;
    }

    /**
     * DOC smallet ModuleNeeded constructor comment.
     * 
     * @param context
     * @param moduleName
     * @param informationMsg
     * @param required
     * @param status
     */
    public ModuleNeeded(String context, String moduleName, String informationMsg, boolean required) {
        super();
        this.context = context;
        this.moduleName = moduleName;
        this.informationMsg = informationMsg;
        this.required = required;
    }

    public ModuleNeeded(String context, String moduleName, String informationMsg, boolean required, List<String> installURL) {
        super();
        this.context = context;
        this.moduleName = moduleName;
        this.informationMsg = informationMsg;
        this.required = required;
        this.installURL = installURL;
    }

    // public ModuleNeeded(String context, String moduleName, String informationMsg, boolean required,
    // List<InstallModule> installModule) {
    // super();
    // this.context = context;
    // this.moduleName = moduleName;
    // this.informationMsg = informationMsg;
    // this.required = required;
    // this.installModule = installModule;
    // }

    /**
     * Getter for installURL.
     * 
     * @return the installURL
     */
    public List<String> getInstallURL() {
        return this.installURL;
    }

    /**
     * Sets the installURL.
     * 
     * @param installURL the installURL to set
     */
    public void setInstallURL(List<String> installURL) {
        this.installURL = installURL;
    }

    /**
     * Getter for component.
     * 
     * @return the component
     */
    public String getContext() {
        return this.context;
    }

    /**
     * Sets the component.
     * 
     * @param component the component to set
     */
    public void setContext(String component) {
        this.context = component;
    }

    public List<InstallModule> getInstallModule() {
        return this.installModule;
    }

    public void setInstallModule(List<InstallModule> installModule) {
        this.installModule = installModule;
    }

    public String getInformationMsg() {
        return this.informationMsg;
    }

    public void setInformationMsg(String informationMsg) {
        this.informationMsg = informationMsg;
    }

    public String getModuleName() {
        if (moduleName != null) {
            return moduleName.replaceAll(QUOTATION_MARK, "").replaceAll(SINGLE_QUOTE, //$NON-NLS-1$
                    ""); //$NON-NLS-1$
        }
        return this.moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public boolean isRequired() {
        return this.required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public ELibraryInstallStatus getStatus() {
        return this.status;
    }

    public void setStatus(ELibraryInstallStatus status) {
        this.status = status;
    }

    /**
     * Getter for isShow.
     * 
     * @return the isShow
     */
    public boolean isShow() {
        return this.isShow;
    }

    /**
     * Sets the isShow.
     * 
     * @param isShow the isShow to set
     */
    public void setShow(boolean isShow) {
        this.isShow = isShow;
    }
}
