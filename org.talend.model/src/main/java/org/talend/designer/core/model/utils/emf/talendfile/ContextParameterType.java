/**
 * <copyright>
 * </copyright>
 *
 * $Id: ContextParameterType.java 20455 2008-11-28 05:25:06Z chuang $
 */
package org.talend.designer.core.model.utils.emf.talendfile;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Context Parameter Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType#getComment <em>Comment</em>}</li>
 *   <li>{@link org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType#getName <em>Name</em>}</li>
 *   <li>{@link org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType#getPrompt <em>Prompt</em>}</li>
 *   <li>{@link org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType#isPromptNeeded <em>Prompt Needed</em>}</li>
 *   <li>{@link org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType#getRepositoryContextId <em>Repository Context Id</em>}</li>
 *   <li>{@link org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType#getType <em>Type</em>}</li>
 *   <li>{@link org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType#getValue <em>Value</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.talend.designer.core.model.utils.emf.talendfile.TalendFilePackage#getContextParameterType()
 * @model extendedMetaData="name='ContextParameter_._type' kind='empty'"
 * @generated
 */
public interface ContextParameterType extends EObject {
    /**
     * Returns the value of the '<em><b>Comment</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Comment</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Comment</em>' attribute.
     * @see #setComment(String)
     * @see org.talend.designer.core.model.utils.emf.talendfile.TalendFilePackage#getContextParameterType_Comment()
     * @model dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='attribute' name='comment' namespace='##targetNamespace'"
     * @generated
     */
    String getComment();

    /**
     * Sets the value of the '{@link org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType#getComment <em>Comment</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Comment</em>' attribute.
     * @see #getComment()
     * @generated
     */
    void setComment(String value);

    /**
     * Returns the value of the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Name</em>' attribute.
     * @see #setName(String)
     * @see org.talend.designer.core.model.utils.emf.talendfile.TalendFilePackage#getContextParameterType_Name()
     * @model dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='attribute' name='name' namespace='##targetNamespace'"
     * @generated
     */
    String getName();

    /**
     * Sets the value of the '{@link org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType#getName <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Name</em>' attribute.
     * @see #getName()
     * @generated
     */
    void setName(String value);

    /**
     * Returns the value of the '<em><b>Prompt</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Prompt</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Prompt</em>' attribute.
     * @see #setPrompt(String)
     * @see org.talend.designer.core.model.utils.emf.talendfile.TalendFilePackage#getContextParameterType_Prompt()
     * @model dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='attribute' name='prompt' namespace='##targetNamespace'"
     * @generated
     */
    String getPrompt();

    /**
     * Sets the value of the '{@link org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType#getPrompt <em>Prompt</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Prompt</em>' attribute.
     * @see #getPrompt()
     * @generated
     */
    void setPrompt(String value);

    /**
     * Returns the value of the '<em><b>Prompt Needed</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Prompt Needed</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Prompt Needed</em>' attribute.
     * @see #isSetPromptNeeded()
     * @see #unsetPromptNeeded()
     * @see #setPromptNeeded(boolean)
     * @see org.talend.designer.core.model.utils.emf.talendfile.TalendFilePackage#getContextParameterType_PromptNeeded()
     * @model unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Boolean"
     *        extendedMetaData="kind='attribute' name='promptNeeded' namespace='##targetNamespace'"
     * @generated
     */
    boolean isPromptNeeded();

    /**
     * Sets the value of the '{@link org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType#isPromptNeeded <em>Prompt Needed</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Prompt Needed</em>' attribute.
     * @see #isSetPromptNeeded()
     * @see #unsetPromptNeeded()
     * @see #isPromptNeeded()
     * @generated
     */
    void setPromptNeeded(boolean value);

    /**
     * Unsets the value of the '{@link org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType#isPromptNeeded <em>Prompt Needed</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isSetPromptNeeded()
     * @see #isPromptNeeded()
     * @see #setPromptNeeded(boolean)
     * @generated
     */
    void unsetPromptNeeded();

    /**
     * Returns whether the value of the '{@link org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType#isPromptNeeded <em>Prompt Needed</em>}' attribute is set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return whether the value of the '<em>Prompt Needed</em>' attribute is set.
     * @see #unsetPromptNeeded()
     * @see #isPromptNeeded()
     * @see #setPromptNeeded(boolean)
     * @generated
     */
    boolean isSetPromptNeeded();

    /**
     * Returns the value of the '<em><b>Repository Context Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Repository Context Id</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Repository Context Id</em>' attribute.
     * @see #setRepositoryContextId(String)
     * @see org.talend.designer.core.model.utils.emf.talendfile.TalendFilePackage#getContextParameterType_RepositoryContextId()
     * @model dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='attribute' name='repositoryContextId' namespace='##targetNamespace'"
     * @generated
     */
    String getRepositoryContextId();

    /**
     * Sets the value of the '{@link org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType#getRepositoryContextId <em>Repository Context Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Repository Context Id</em>' attribute.
     * @see #getRepositoryContextId()
     * @generated
     */
    void setRepositoryContextId(String value);

    /**
     * Returns the value of the '<em><b>Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Type</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Type</em>' attribute.
     * @see #setType(String)
     * @see org.talend.designer.core.model.utils.emf.talendfile.TalendFilePackage#getContextParameterType_Type()
     * @model dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='attribute' name='type' namespace='##targetNamespace'"
     * @generated
     */
    String getType();

    /**
     * Sets the value of the '{@link org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType#getType <em>Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Type</em>' attribute.
     * @see #getType()
     * @generated
     */
    void setType(String value);

    /**
     * Returns the value of the '<em><b>Value</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Value</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Value</em>' attribute.
     * @see #setValue(String)
     * @see org.talend.designer.core.model.utils.emf.talendfile.TalendFilePackage#getContextParameterType_Value()
     * @model dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='attribute' name='value' namespace='##targetNamespace'"
     * @generated
     */
    String getValue();
    
    String getRawValue();

    /**
     * Sets the value of the '{@link org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType#getValue <em>Value</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Value</em>' attribute.
     * @see #getValue()
     * @generated
     */
    void setValue(String value);

} // ContextParameterType