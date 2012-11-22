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
package org.talend.core.model.general;

import java.util.List;

import org.talend.core.model.process.IElementParameter;
import org.talend.core.runtime.CoreRuntimePlugin;

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

    private String requiredIf;

    // bundleName and bundleVersion for osgi system,feature 0023460
    private String bundleName;

    private String bundleVersion;

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

    public ModuleNeeded(String context, String moduleName, String informationMsg, boolean required, List<String> installURL,
            String requiredIf) {
        super();
        this.context = context;
        this.moduleName = moduleName;
        this.informationMsg = informationMsg;
        this.required = required;
        this.installURL = installURL;
        this.requiredIf = requiredIf;
    }

    public String getRequiredIf() {
        return requiredIf;
    }

    /**
     * Check if the library is required depends the condition of "required if". Note that if the flag "required=true" in
     * the xml of component, it will never check in the required_if.
     * 
     * In some cases where we only want to check the basic "required=true" and not the required_if (module view for
     * example), it's possible to simply give null parameter.
     * 
     * @param listParam
     * @return
     */
    public boolean isRequired(List<? extends IElementParameter> listParam) {
        if (required) { // if flag required is set, then forget the "required if" test.
            return required;
        }
        boolean isRequired = false;

        if (requiredIf != null && listParam != null) {
            isRequired = CoreRuntimePlugin.getInstance().getDesignerCoreService().evaluate(requiredIf, listParam);
        }
        return isRequired;
    }

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

    public String getBundleName() {
        return bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public String getBundleVersion() {
        return bundleVersion;
    }

    public void setBundleVersion(String bundleVersion) {
        this.bundleVersion = bundleVersion;
    }

    @Override
    public String toString() {
        if (bundleName == null) {
            return moduleName;
        } else if (bundleVersion == null) {
            return moduleName + "[" + bundleName + "]";
        } else {
            return moduleName + "[" + bundleName + ":" + bundleVersion + "]";
        }
    }
}
