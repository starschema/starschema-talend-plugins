/**
 * <copyright> </copyright>
 * 
 * $Id: ElementParameterTypeImpl.java 65339 2011-08-02 07:41:04Z dlin $
 */
package org.talend.designer.core.model.utils.emf.talendfile.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFilePackage;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Element Parameter Type</b></em>'. <!--
 * end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.talend.designer.core.model.utils.emf.talendfile.impl.ElementParameterTypeImpl#getElementValue <em>Element Value</em>}</li>
 *   <li>{@link org.talend.designer.core.model.utils.emf.talendfile.impl.ElementParameterTypeImpl#getField <em>Field</em>}</li>
 *   <li>{@link org.talend.designer.core.model.utils.emf.talendfile.impl.ElementParameterTypeImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.talend.designer.core.model.utils.emf.talendfile.impl.ElementParameterTypeImpl#getValue <em>Value</em>}</li>
 *   <li>{@link org.talend.designer.core.model.utils.emf.talendfile.impl.ElementParameterTypeImpl#isContextMode <em>Context Mode</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ElementParameterTypeImpl extends EObjectImpl implements ElementParameterType {

    /**
     * The cached value of the '{@link #getElementValue() <em>Element Value</em>}' containment reference list. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getElementValue()
     * @generated
     * @ordered
     */
    protected EList elementValue;

    /**
     * The default value of the '{@link #getField() <em>Field</em>}' attribute.
     * <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * @see #getField()
     * @generated
     * @ordered
     */
    protected static final String FIELD_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getField() <em>Field</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @see #getField()
     * @generated
     * @ordered
     */
    protected String field = FIELD_EDEFAULT;

    /**
     * The default value of the '{@link #getName() <em>Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @see #getName()
     * @generated
     * @ordered
     */
    protected static final String NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getName() <em>Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @see #getName()
     * @generated
     * @ordered
     */
    protected String name = NAME_EDEFAULT;

    /**
     * The default value of the '{@link #getValue() <em>Value</em>}' attribute.
     * <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * @see #getValue()
     * @generated
     * @ordered
     */
    protected static final String VALUE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getValue() <em>Value</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @see #getValue()
     * @generated
     * @ordered
     */
    protected String value = VALUE_EDEFAULT;

    /**
     * The default value of the '{@link #isContextMode() <em>Context Mode</em>}' attribute.
     * <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * @see #isContextMode()
     * @generated
     * @ordered
     */
    protected static final boolean CONTEXT_MODE_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isContextMode() <em>Context Mode</em>}' attribute.
     * <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * @see #isContextMode()
     * @generated
     * @ordered
     */
    protected boolean contextMode = CONTEXT_MODE_EDEFAULT;

    /**
     * This is true if the Context Mode attribute has been set.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    protected boolean contextModeESet;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    protected ElementParameterTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return TalendFilePackage.Literals.ELEMENT_PARAMETER_TYPE;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public EList getElementValue() {
        if (elementValue == null) {
            elementValue = new EObjectContainmentEList(ElementValueType.class, this, TalendFilePackage.ELEMENT_PARAMETER_TYPE__ELEMENT_VALUE);
        }
        return elementValue;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public boolean isContextMode() {
        return contextMode;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public void setContextMode(boolean newContextMode) {
        boolean oldContextMode = contextMode;
        contextMode = newContextMode;
        boolean oldContextModeESet = contextModeESet;
        contextModeESet = true;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, TalendFilePackage.ELEMENT_PARAMETER_TYPE__CONTEXT_MODE, oldContextMode, contextMode, !oldContextModeESet));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public void unsetContextMode() {
        boolean oldContextMode = contextMode;
        boolean oldContextModeESet = contextModeESet;
        contextMode = CONTEXT_MODE_EDEFAULT;
        contextModeESet = false;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.UNSET, TalendFilePackage.ELEMENT_PARAMETER_TYPE__CONTEXT_MODE, oldContextMode, CONTEXT_MODE_EDEFAULT, oldContextModeESet));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public boolean isSetContextMode() {
        return contextModeESet;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public String getField() {
        return field;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public void setField(String newField) {
        String oldField = field;
        field = newField;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, TalendFilePackage.ELEMENT_PARAMETER_TYPE__FIELD, oldField, field));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public String getName() {
        return name;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, TalendFilePackage.ELEMENT_PARAMETER_TYPE__NAME, oldName, name));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public String getValue() {
        return value;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public void setValue(String newValue) {
        String oldValue = value;
        value = newValue;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, TalendFilePackage.ELEMENT_PARAMETER_TYPE__VALUE, oldValue, value));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__ELEMENT_VALUE:
                return ((InternalEList)getElementValue()).basicRemove(otherEnd, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__ELEMENT_VALUE:
                return getElementValue();
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__FIELD:
                return getField();
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__NAME:
                return getName();
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__VALUE:
                return getValue();
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__CONTEXT_MODE:
                return isContextMode() ? Boolean.TRUE : Boolean.FALSE;
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__ELEMENT_VALUE:
                getElementValue().clear();
                getElementValue().addAll((Collection)newValue);
                return;
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__FIELD:
                setField((String)newValue);
                return;
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__NAME:
                setName((String)newValue);
                return;
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__VALUE:
                setValue((String)newValue);
                return;
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__CONTEXT_MODE:
                setContextMode(((Boolean)newValue).booleanValue());
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public void eUnset(int featureID) {
        switch (featureID) {
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__ELEMENT_VALUE:
                getElementValue().clear();
                return;
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__FIELD:
                setField(FIELD_EDEFAULT);
                return;
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__NAME:
                setName(NAME_EDEFAULT);
                return;
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__VALUE:
                setValue(VALUE_EDEFAULT);
                return;
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__CONTEXT_MODE:
                unsetContextMode();
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__ELEMENT_VALUE:
                return elementValue != null && !elementValue.isEmpty();
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__FIELD:
                return FIELD_EDEFAULT == null ? field != null : !FIELD_EDEFAULT.equals(field);
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__NAME:
                return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__VALUE:
                return VALUE_EDEFAULT == null ? value != null : !VALUE_EDEFAULT.equals(value);
            case TalendFilePackage.ELEMENT_PARAMETER_TYPE__CONTEXT_MODE:
                return isSetContextMode();
        }
        return super.eIsSet(featureID);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public String toString() {
        if (eIsProxy()) return super.toString();

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (field: ");
        result.append(field);
        result.append(", name: ");
        result.append(name);
        result.append(", value: ");
        result.append(value);
        result.append(", contextMode: ");
        if (contextModeESet) result.append(contextMode); else result.append("<unset>");
        result.append(')');
        return result.toString();
    }

} // ElementParameterTypeImpl
