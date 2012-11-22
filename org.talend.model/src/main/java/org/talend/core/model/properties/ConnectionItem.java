/**
 * <copyright> </copyright>
 * 
 * $Id: ConnectionItem.java 19828 2008-11-05 10:07:53Z ggu $
 */
package org.talend.core.model.properties;

import org.talend.core.model.metadata.builder.connection.Connection;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Connection Item</b></em>'. <!--
 * end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.talend.core.model.properties.ConnectionItem#getConnection <em>Connection</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.talend.core.model.properties.PropertiesPackage#getConnectionItem()
 * @model
 * @generated
 */
public interface ConnectionItem extends Item {

    /**
     * Returns the value of the '<em><b>Connection</b></em>' reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Connection</em>' reference isn't clear, there really should be more of a
     * description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Connection</em>' reference.
     * @see #setConnection(Connection)
     * @see org.talend.core.model.properties.PropertiesPackage#getConnectionItem_Connection()
     * @model
     * @generated
     */
    Connection getConnection();

    /**
     * Sets the value of the '{@link org.talend.core.model.properties.ConnectionItem#getConnection <em>Connection</em>}' reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Connection</em>' reference.
     * @see #getConnection()
     * @generated
     */
    void setConnection(Connection value);

} // ConnectionItem
