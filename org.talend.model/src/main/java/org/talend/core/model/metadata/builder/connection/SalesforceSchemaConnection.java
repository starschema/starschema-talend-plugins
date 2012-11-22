/**
 * <copyright> </copyright>
 * 
 * $Id$
 */
package org.talend.core.model.metadata.builder.connection;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Salesforce Schema Connection</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getWebServiceUrl <em>Web Service Url</em>}</li>
 *   <li>{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getUserName <em>User Name</em>}</li>
 *   <li>{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getPassword <em>Password</em>}</li>
 *   <li>{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getModuleName <em>Module Name</em>}</li>
 *   <li>{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getQueryCondition <em>Query Condition</em>}</li>
 *   <li>{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#isUseCustomModuleName <em>Use Custom Module Name</em>}</li>
 *   <li>{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#isUseProxy <em>Use Proxy</em>}</li>
 *   <li>{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getProxyHost <em>Proxy Host</em>}</li>
 *   <li>{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getProxyPort <em>Proxy Port</em>}</li>
 *   <li>{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getProxyUsername <em>Proxy Username</em>}</li>
 *   <li>{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getProxyPassword <em>Proxy Password</em>}</li>
 *   <li>{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getBatchSize <em>Batch Size</em>}</li>
 *   <li>{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#isUseHttpProxy <em>Use Http Proxy</em>}</li>
 *   <li>{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#isUseAlphbet <em>Use Alphbet</em>}</li>
 *   <li>{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getTimeOut <em>Time Out</em>}</li>
 *   <li>{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getModules <em>Modules</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getSalesforceSchemaConnection()
 * @model
 * @generated
 */
public interface SalesforceSchemaConnection extends Connection {

    /**
     * Returns the value of the '<em><b>Web Service Url</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Web Service Url</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Web Service Url</em>' attribute.
     * @see #setWebServiceUrl(String)
     * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getSalesforceSchemaConnection_WebServiceUrl()
     * @model
     * @generated
     */
    String getWebServiceUrl();

    /**
     * Sets the value of the '{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getWebServiceUrl <em>Web Service Url</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Web Service Url</em>' attribute.
     * @see #getWebServiceUrl()
     * @generated
     */
    void setWebServiceUrl(String value);

    /**
     * Returns the value of the '<em><b>User Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>User Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>User Name</em>' attribute.
     * @see #setUserName(String)
     * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getSalesforceSchemaConnection_UserName()
     * @model
     * @generated
     */
    String getUserName();

    /**
     * Sets the value of the '{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getUserName <em>User Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>User Name</em>' attribute.
     * @see #getUserName()
     * @generated
     */
    void setUserName(String value);

    /**
     * Returns the value of the '<em><b>Password</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Password</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Password</em>' attribute.
     * @see #setPassword(String)
     * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getSalesforceSchemaConnection_Password()
     * @model
     * @generated
     */
    String getPassword();

    /**
     * Sets the value of the '{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getPassword <em>Password</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Password</em>' attribute.
     * @see #getPassword()
     * @generated
     */
    void setPassword(String value);

    /**
     * Returns the value of the '<em><b>Module Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Module Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Module Name</em>' attribute.
     * @see #setModuleName(String)
     * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getSalesforceSchemaConnection_ModuleName()
     * @model
     * @generated
     */
    String getModuleName();

    /**
     * Sets the value of the '{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getModuleName <em>Module Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Module Name</em>' attribute.
     * @see #getModuleName()
     * @generated
     */
    void setModuleName(String value);

    /**
     * Returns the value of the '<em><b>Query Condition</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Query Condition</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Query Condition</em>' attribute.
     * @see #setQueryCondition(String)
     * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getSalesforceSchemaConnection_QueryCondition()
     * @model
     * @generated
     */
    String getQueryCondition();

    /**
     * Sets the value of the '{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getQueryCondition <em>Query Condition</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Query Condition</em>' attribute.
     * @see #getQueryCondition()
     * @generated
     */
    void setQueryCondition(String value);

    /**
     * Returns the value of the '<em><b>Use Custom Module Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Use Custom Module Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Use Custom Module Name</em>' attribute.
     * @see #setUseCustomModuleName(boolean)
     * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getSalesforceSchemaConnection_UseCustomModuleName()
     * @model
     * @generated
     */
    boolean isUseCustomModuleName();

    /**
     * Sets the value of the '{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#isUseCustomModuleName <em>Use Custom Module Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Use Custom Module Name</em>' attribute.
     * @see #isUseCustomModuleName()
     * @generated
     */
    void setUseCustomModuleName(boolean value);

    /**
     * Returns the value of the '<em><b>Use Proxy</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Use Proxy</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Use Proxy</em>' attribute.
     * @see #setUseProxy(boolean)
     * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getSalesforceSchemaConnection_UseProxy()
     * @model
     * @generated
     */
    boolean isUseProxy();

    /**
     * Sets the value of the '{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#isUseProxy <em>Use Proxy</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Use Proxy</em>' attribute.
     * @see #isUseProxy()
     * @generated
     */
    void setUseProxy(boolean value);

    /**
     * Returns the value of the '<em><b>Proxy Host</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Proxy Host</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Proxy Host</em>' attribute.
     * @see #setProxyHost(String)
     * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getSalesforceSchemaConnection_ProxyHost()
     * @model
     * @generated
     */
    String getProxyHost();

    /**
     * Sets the value of the '{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getProxyHost <em>Proxy Host</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Proxy Host</em>' attribute.
     * @see #getProxyHost()
     * @generated
     */
    void setProxyHost(String value);

    /**
     * Returns the value of the '<em><b>Proxy Port</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Proxy Port</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Proxy Port</em>' attribute.
     * @see #setProxyPort(String)
     * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getSalesforceSchemaConnection_ProxyPort()
     * @model
     * @generated
     */
    String getProxyPort();

    /**
     * Sets the value of the '{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getProxyPort <em>Proxy Port</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Proxy Port</em>' attribute.
     * @see #getProxyPort()
     * @generated
     */
    void setProxyPort(String value);

    /**
     * Returns the value of the '<em><b>Proxy Username</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Proxy Username</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Proxy Username</em>' attribute.
     * @see #setProxyUsername(String)
     * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getSalesforceSchemaConnection_ProxyUsername()
     * @model
     * @generated
     */
    String getProxyUsername();

    /**
     * Sets the value of the '{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getProxyUsername <em>Proxy Username</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Proxy Username</em>' attribute.
     * @see #getProxyUsername()
     * @generated
     */
    void setProxyUsername(String value);

    /**
     * Returns the value of the '<em><b>Proxy Password</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Proxy Password</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Proxy Password</em>' attribute.
     * @see #setProxyPassword(String)
     * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getSalesforceSchemaConnection_ProxyPassword()
     * @model
     * @generated
     */
    String getProxyPassword();

    /**
     * Sets the value of the '{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getProxyPassword <em>Proxy Password</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Proxy Password</em>' attribute.
     * @see #getProxyPassword()
     * @generated
     */
    void setProxyPassword(String value);

    /**
     * Returns the value of the '<em><b>Batch Size</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Batch Size</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Batch Size</em>' attribute.
     * @see #setBatchSize(String)
     * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getSalesforceSchemaConnection_BatchSize()
     * @model
     * @generated
     */
    String getBatchSize();

    /**
     * Sets the value of the '{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getBatchSize <em>Batch Size</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Batch Size</em>' attribute.
     * @see #getBatchSize()
     * @generated
     */
    void setBatchSize(String value);

    /**
     * Returns the value of the '<em><b>Use Http Proxy</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Use Http Proxy</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Use Http Proxy</em>' attribute.
     * @see #setUseHttpProxy(boolean)
     * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getSalesforceSchemaConnection_UseHttpProxy()
     * @model
     * @generated
     */
    boolean isUseHttpProxy();

    /**
     * Sets the value of the '{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#isUseHttpProxy <em>Use Http Proxy</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Use Http Proxy</em>' attribute.
     * @see #isUseHttpProxy()
     * @generated
     */
    void setUseHttpProxy(boolean value);

    /**
     * Returns the value of the '<em><b>Use Alphbet</b></em>' attribute.
     * The default value is <code>"true"</code>.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Use Alphbet</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Use Alphbet</em>' attribute.
     * @see #setUseAlphbet(boolean)
     * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getSalesforceSchemaConnection_UseAlphbet()
     * @model default="true"
     * @generated
     */
    boolean isUseAlphbet();

    /**
     * Sets the value of the '{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#isUseAlphbet <em>Use Alphbet</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Use Alphbet</em>' attribute.
     * @see #isUseAlphbet()
     * @generated
     */
    void setUseAlphbet(boolean value);

    /**
     * Returns the value of the '<em><b>Time Out</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Time Out</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Time Out</em>' attribute.
     * @see #setTimeOut(String)
     * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getSalesforceSchemaConnection_TimeOut()
     * @model
     * @generated
     */
    String getTimeOut();

    /**
     * Sets the value of the '{@link org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection#getTimeOut <em>Time Out</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Time Out</em>' attribute.
     * @see #getTimeOut()
     * @generated
     */
    void setTimeOut(String value);

    /**
     * Returns the value of the '<em><b>Modules</b></em>' containment reference list.
     * The list contents are of type {@link org.talend.core.model.metadata.builder.connection.SalesforceModuleUnit}.
     * It is bidirectional and its opposite is '{@link org.talend.core.model.metadata.builder.connection.SalesforceModuleUnit#getConnection <em>Connection</em>}'.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Modules</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Modules</em>' containment reference list.
     * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getSalesforceSchemaConnection_Modules()
     * @see org.talend.core.model.metadata.builder.connection.SalesforceModuleUnit#getConnection
     * @model opposite="connection" containment="true" resolveProxies="true"
     * @generated
     */
    EList<SalesforceModuleUnit> getModules();

} // SalesforceSchemaConnection
