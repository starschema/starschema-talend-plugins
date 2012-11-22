/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.core.model.utils.emf.component;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>SQLPATTERNS Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.talend.designer.core.model.utils.emf.component.SQLPATTERNSType#getSQLPATTERN <em>SQLPATTERN</em>}</li>
 *   <li>{@link org.talend.designer.core.model.utils.emf.component.SQLPATTERNSType#getDB <em>DB</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.talend.designer.core.model.utils.emf.component.ComponentPackage#getSQLPATTERNSType()
 * @model extendedMetaData="name='SQLPATTERNS_._type' kind='elementOnly'"
 * @generated
 */
public interface SQLPATTERNSType extends EObject {
    /**
     * Returns the value of the '<em><b>SQLPATTERN</b></em>' containment reference list.
     * The list contents are of type {@link org.talend.designer.core.model.utils.emf.component.SQLPATTERNType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>SQLPATTERN</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>SQLPATTERN</em>' containment reference list.
     * @see org.talend.designer.core.model.utils.emf.component.ComponentPackage#getSQLPATTERNSType_SQLPATTERN()
     * @model type="org.talend.designer.core.model.utils.emf.component.SQLPATTERNType" containment="true"
     *        extendedMetaData="kind='element' name='SQLPATTERN' namespace='##targetNamespace'"
     * @generated
     */
    EList getSQLPATTERN();

    /**
     * Returns the value of the '<em><b>DB</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>DB</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>DB</em>' attribute.
     * @see #setDB(String)
     * @see org.talend.designer.core.model.utils.emf.component.ComponentPackage#getSQLPATTERNSType_DB()
     * @model dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='attribute' name='DB' namespace='##targetNamespace'"
     * @generated
     */
    String getDB();

    /**
     * Sets the value of the '{@link org.talend.designer.core.model.utils.emf.component.SQLPATTERNSType#getDB <em>DB</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>DB</em>' attribute.
     * @see #getDB()
     * @generated
     */
    void setDB(String value);

} // SQLPATTERNSType
