/**
 * <copyright>
 * </copyright>
 *
 * $Id: IMPORTSType.java 17131 2008-08-20 05:46:08Z nrousseau $
 */
package org.talend.designer.core.model.utils.emf.component;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IMPORTS Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.talend.designer.core.model.utils.emf.component.IMPORTSType#getIMPORT <em>IMPORT</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.talend.designer.core.model.utils.emf.component.ComponentPackage#getIMPORTSType()
 * @model extendedMetaData="name='IMPORTS_._type' kind='elementOnly'"
 * @generated
 */
public interface IMPORTSType extends EObject {
    /**
     * Returns the value of the '<em><b>IMPORT</b></em>' containment reference list.
     * The list contents are of type {@link org.talend.designer.core.model.utils.emf.component.IMPORTType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>IMPORT</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>IMPORT</em>' containment reference list.
     * @see org.talend.designer.core.model.utils.emf.component.ComponentPackage#getIMPORTSType_IMPORT()
     * @model type="org.talend.designer.core.model.utils.emf.component.IMPORTType" containment="true" required="true"
     *        extendedMetaData="kind='element' name='IMPORT' namespace='##targetNamespace'"
     * @generated
     */
    EList getIMPORT();

} // IMPORTSType