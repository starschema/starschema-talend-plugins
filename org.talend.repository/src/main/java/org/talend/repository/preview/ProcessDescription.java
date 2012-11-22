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
package org.talend.repository.preview;

/**
 * Describes a process for shadow execution. <br/>
 * 
 * $Id: ProcessDescription.java 77219 2012-01-24 01:14:15Z mhirt $
 * 
 */
public class ProcessDescription extends AbstractProcessDescription {

    private LDAPSchemaBean ldapSchemaBean;

    private WSDLSchemaBean wsdlSchemaBean;

    private ExcelSchemaBean excelSchemaBean;

    private SalesforceSchemaBean salesforceSchemaBean;

    /**
     * Getter for wsdlSchemaBean.
     * 
     * @return the wsdlSchemaBean
     */
    public WSDLSchemaBean getWsdlSchemaBean() {
        return this.wsdlSchemaBean;
    }

    /**
     * Sets the wsdlSchemaBean.
     * 
     * @param wsdlSchemaBean the wsdlSchemaBean to set
     */
    public void setWsdlSchemaBean(WSDLSchemaBean wsdlSchemaBean) {
        this.wsdlSchemaBean = wsdlSchemaBean;
    }

    /**
     * Constructs a new ProcessDescription.
     */
    public ProcessDescription() {
        super();
    }

    public LDAPSchemaBean getLdapSchemaBean() {
        return this.ldapSchemaBean;
    }

    public void setLdapSchemaBean(LDAPSchemaBean ldapSchemaBean) {
        this.ldapSchemaBean = ldapSchemaBean;
    }

    /**
     * Getter for excelSchemaBean.
     * 
     * @return the excelSchemaBean
     */
    public ExcelSchemaBean getExcelSchemaBean() {
        return this.excelSchemaBean;
    }

    /**
     * Sets the excelSchemaBean.
     * 
     * @param excelSchemaBean the excelSchemaBean to set
     */
    public void setExcelSchemaBean(ExcelSchemaBean excelSchemaBean) {
        this.excelSchemaBean = excelSchemaBean;
    }

    /**
     * Getter for salesforceSchemaBean.
     * 
     * @return the salesforceSchemaBean
     */
    public SalesforceSchemaBean getSalesforceSchemaBean() {
        return this.salesforceSchemaBean;
    }

    /**
     * Sets the salesforceSchemaBean.
     * 
     * @param salesforceSchemaBean the salesforceSchemaBean to set
     */
    public void setSalesforceSchemaBean(SalesforceSchemaBean salesforceSchemaBean) {
        this.salesforceSchemaBean = salesforceSchemaBean;
    }

}
