/**
 * <copyright> </copyright>
 * 
 * $Id$
 */
package org.talend.core.model.metadata.builder.connection;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Ebcdic Connection</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.talend.core.model.metadata.builder.connection.EbcdicConnection#getMidFile <em>Mid File</em>}</li>
 *   <li>{@link org.talend.core.model.metadata.builder.connection.EbcdicConnection#getDataFile <em>Data File</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getEbcdicConnection()
 * @model
 * @generated
 */
public interface EbcdicConnection extends FileConnection {

    /**
     * Returns the value of the '<em><b>Mid File</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Mid File</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Mid File</em>' attribute.
     * @see #setMidFile(String)
     * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getEbcdicConnection_MidFile()
     * @model
     * @generated
     */
    String getMidFile();

    /**
     * Sets the value of the '{@link org.talend.core.model.metadata.builder.connection.EbcdicConnection#getMidFile <em>Mid File</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Mid File</em>' attribute.
     * @see #getMidFile()
     * @generated
     */
    void setMidFile(String value);

    /**
     * Returns the value of the '<em><b>Data File</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Data File</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Data File</em>' attribute.
     * @see #setDataFile(String)
     * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getEbcdicConnection_DataFile()
     * @model
     * @generated
     */
    String getDataFile();

    /**
     * Sets the value of the '{@link org.talend.core.model.metadata.builder.connection.EbcdicConnection#getDataFile <em>Data File</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Data File</em>' attribute.
     * @see #getDataFile()
     * @generated
     */
    void setDataFile(String value);
} // EbcdicConnection
