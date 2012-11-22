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
package org.talend.designer.core.model.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.RGB;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IElementParameterDefaultValue;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;

/**
 * Each parameter of the components are read and written in this class. <br/>
 * 
 * $Id: ElementParameter.java 78755 2012-02-23 08:31:15Z hqzhang $
 * 
 */
public class ElementParameter implements IElementParameter {

    private static final int NB_LINES_DEFAULT = 3;

    private String name, displayName;

    private EParameterFieldType field;

    private EComponentCategory category;

    private boolean show = true, required = false, readOnly = false;

    private Object value;

    private IElement element;

    // used for CLOSED_LIST / TABLE
    private String[] itemsDisplayName;

    // used for CLOSED_LIST / TABLE
    private String[] itemsDisplayCodeName;

    // used for CLOSED_LIST / TABLE
    private String[] itemsShowIf;

    // used for CLOSED_LIST / TABLE
    private String[] itemsNotShowIf;

    // used for CLOSED_LIST
    private Object[] itemsValue;

    // used for CLOSED_LIST / TABLE
    private String[] itemsRepository;

    // used for CLOSED_LIST
    private Object defaultClosedListValue;

    // hshen 6930
    private String[] itemsNotReadOnlyIf;

    private String[] itemsReadOnlyIf;

    private boolean basedOnSchema = false;

    private boolean basedOnSubjobStarts = false;

    private boolean columnsBasedOnSchema = false;

    // achen add
    private boolean basedOnInputSchema = false;

    private int nbLines = NB_LINES_DEFAULT, numRow = 0; // Default values

    private String repositoryValue;

    private boolean repositoryValueUsed = false;

    private String showIf = null;

    private String notShowIf = null;

    // hshen 6930
    private String readonlyIf = null;

    private String notReadonlyIf = null;

    private List<IElementParameterDefaultValue> defaultValues = new ArrayList<IElementParameterDefaultValue>();

    private String filter = null;

    private boolean noCheck = false;

    private String context, groupName, groupDisplayName;

    private Map<String, IElementParameter> childParameters;

    private IElementParameter parentParameter;

    private int currentRow; // for Table only

    private Item linkedRepositoryItem;

    private boolean contextMode;

    private String labelFromRepository;

    private RGB color;

    private RGB backgroundColor;

    private boolean dynamicSettings = false;

    private boolean noContextAssist;

    private String javaClass; // for JAVA_COMMAND

    private String javaFunction; // for JAVA_COMMAND

    private String jar; // for JAVA_COMMAND

    private String[] args;

    private int maxlength;

    public ElementParameter(final IElement element) {
        this.element = element;
    }

    public boolean isBasedOnInputSchema() {
        return this.basedOnInputSchema;
    }

    public void setBasedOnInputSchema(boolean basedOnInputSchema) {
        this.basedOnInputSchema = basedOnInputSchema;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.components.IDesignerElementParameter#setName(java.lang.String)
     */
    public void setName(final String s) {
        name = s;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.components.IDesignerElementParameter#getVariableName()
     */
    public String getVariableName() {
        return "__" + name + "__"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void setCategory(final EComponentCategory cat) {
        category = cat;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.components.IDesignerElementParameter#getCategory()
     */
    public EComponentCategory getCategory() {
        return this.category;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.components.IDesignerElementParameter#setDisplayName(java.lang.String)
     */
    public void setDisplayName(final String s) {
        displayName = s;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.talend.designer.core.model.components.IDesignerElementParameter#setField(org.talend.core.model.designer.
     * EParameterFieldType)
     */
    public void setFieldType(final EParameterFieldType type) {
        field = type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.components.IDesignerElementParameter#setValue(java.lang.Object)
     */
    public void setValue(final Object o) {
        value = o;
    }

    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.components.IDesignerElementParameter#getDisplayName()
     */
    public String getDisplayName() {
        return displayName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.components.IDesignerElementParameter#getField()
     */
    public EParameterFieldType getFieldType() {
        return field;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.components.IDesignerElementParameter#getValue()
     */
    public Object getValue() {
        return value;
    }

    public void setListItemsDisplayName(final String[] list) {
        itemsDisplayName = list;
    }

    public String[] getListItemsDisplayName() {
        return itemsDisplayName;
    }

    public void setListItemsDisplayCodeName(final String[] list) {
        itemsDisplayCodeName = list;
    }

    public String[] getListItemsDisplayCodeName() {
        return itemsDisplayCodeName;
    }

    public void setListItemsValue(final Object[] list) {
        if (this.getFieldType() == EParameterFieldType.TABLE) {
            EParameterFieldType.AS400_CHECK.getClass();
        }
        itemsValue = list;
    }

    public Object[] getListItemsValue() {
        return itemsValue;
    }

    public void setDefaultClosedListValue(Object o) {
        defaultClosedListValue = o;
    }

    public Object getDefaultClosedListValue() {
        return defaultClosedListValue;
    }

    public void setListRepositoryItems(final String[] list) {
        itemsRepository = list;
    }

    public String[] getListRepositoryItems() {
        return itemsRepository;
    }

    public void setListItemsShowIf(String[] list) {
        itemsShowIf = list;
    }

    public String[] getListItemsShowIf() {
        return itemsShowIf;
    }

    public void setListItemsNotShowIf(String[] list) {
        itemsNotShowIf = list;
    }

    public String[] getListItemsNotShowIf() {
        return itemsNotShowIf;
    }

    public int getIndexOfItemFromList(String item) {
        int index = -1;
        boolean found = false;
        if (itemsDisplayCodeName != null) {
            for (int i = 0; i < itemsDisplayCodeName.length && !found; i++) {
                String string = itemsDisplayCodeName[i];
                if (string.equals(item)) {
                    found = true;
                    index = i;
                }
            }
        }
        for (int i = 0; i < itemsValue.length && !found; i++) {
            String string = (String) itemsValue[i];
            if (string.equals(item)) {
                found = true;
                index = i;
            }
        }
        return index;
    }

    public int getNbLines() {
        return this.nbLines;
    }

    public void setNbLines(final int nbLines) {
        this.nbLines = nbLines;
    }

    public int getNumRow() {
        return this.numRow;
    }

    public void setNumRow(final int numRow) {
        this.numRow = numRow;
    }

    public boolean isReadOnly() {
        if (element != null) {
            return (this.readOnly || element.isReadOnly());
        }
        return this.readOnly;
    }

    public boolean getOriginalityReadOnly() {
        return this.readOnly;
    }

    public void setReadOnly(final boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isRequired() {
        return this.required;
    }

    public void setRequired(final boolean required) {
        this.required = required;
    }

    public void setShow(final boolean show) {
        this.show = show;
    }

    public String getRepositoryValue() {
        return this.repositoryValue;
    }

    public void setRepositoryValue(String repositoryValue) {
        this.repositoryValue = repositoryValue;
    }

    public boolean isRepositoryValueUsed() {
        return this.repositoryValueUsed;
    }

    public void setRepositoryValueUsed(boolean repositoryUsed) {
        this.repositoryValueUsed = repositoryUsed;
    }

    public String getShowIf() {
        return showIf;
    }

    public void setShowIf(String showIf) {
        this.showIf = showIf;
    }

    public String getNotShowIf() {
        return notShowIf;
    }

    public void setNotShowIf(String notShowIf) {
        this.notShowIf = notShowIf;
    }

    public boolean isShow(List<? extends IElementParameter> listParam) {
        boolean showParameter = false;

        if (((showIf != null) || (notShowIf != null)) && show) {
            if (showIf != null) {
                showParameter = Expression.evaluate(showIf, listParam, this);
            } else {
                showParameter = !Expression.evaluate(notShowIf, listParam, this);
            }
        } else {
            showParameter = show;
        }
        return showParameter;
    }

    public boolean isShow(String conditionShowIf, String conditionNotShowIf, List<? extends IElementParameter> listParam) {
        boolean showParameter = false;

        if (((conditionShowIf != null) || (conditionNotShowIf != null)) && show) {
            if (conditionShowIf != null) {
                showParameter = Expression.evaluate(conditionShowIf, listParam, this);
            } else {
                showParameter = !Expression.evaluate(conditionNotShowIf, listParam, this);
            }
        } else {
            showParameter = show;
        }
        return showParameter;
    }

    public boolean isCondition(String conditionShowIf, String conditionNotShowIf, List<? extends IElementParameter> listParam) {

        boolean showParameter = false;

        if ((conditionShowIf != null) || (conditionNotShowIf != null)) {
            if (conditionShowIf != null) {
                showParameter = Expression.evaluate(conditionShowIf, listParam, this);
            } else {
                showParameter = !Expression.evaluate(conditionNotShowIf, listParam, this);
            }
        }
        return showParameter;
    }

    public List<IElementParameterDefaultValue> getDefaultValues() {
        return this.defaultValues;
    }

    public void setDefaultValues(List<IElementParameterDefaultValue> defaultValues) {
        this.defaultValues = defaultValues;
    }

    public void setValueToDefault(List<? extends IElementParameter> listParam) {
        for (IElementParameterDefaultValue defaultValue : defaultValues) {
            boolean setDefaultValue = false;
            String conditionIf = defaultValue.getIfCondition();
            String conditionNotIf = defaultValue.getNotIfCondition();

            if ((conditionIf != null) || (conditionNotIf != null)) {
                if (conditionIf != null) {
                    setDefaultValue = Expression.evaluate(conditionIf, listParam);
                } else {
                    setDefaultValue = !Expression.evaluate(conditionNotIf, listParam);
                }
            }
            if (setDefaultValue) {
                if (this.field.equals(EParameterFieldType.CHECK) || this.field.equals(EParameterFieldType.RADIO)) {
                    setValue(new Boolean(defaultValue.getDefaultValue().toString()));
                } else {
                    setValue(defaultValue.getDefaultValue());
                }
            }
        }
    }

    public IElement getElement() {
        return element;
    }

    public void setElement(IElement element) {
        this.element = element;
    }

    public boolean isBasedOnSchema() {
        return basedOnSchema;
    }

    public void setBasedOnSchema(boolean basedOnSchema) {
        this.basedOnSchema = basedOnSchema;
    }

    @Override
    public String toString() {
        if (value == null) {
            return name + ": null"; //$NON-NLS-1$
        }
        return name + ": " + value.toString(); //$NON-NLS-1$
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * Getter for noCheck.
     * 
     * @return the noCheck
     */
    public boolean isNoCheck() {
        IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();

        if (!preferenceStore.getBoolean(TalendDesignerPrefConstants.PROPERTY_CODE_CHECK)) {
            // if the check has been completely disabled then no check.
            // if not disabled in the preferences, then it will depends on the next conditions.
            return true;
        }

        if (!(element instanceof INode)) {
            return true;
        }
        return noCheck;
    }

    /**
     * Sets the noCheck.
     * 
     * @param noCheck the noCheck to set
     */
    public void setNoCheck(boolean noCheck) {
        this.noCheck = noCheck;
    }

    /**
     * Getter for context.
     * 
     * @return the context
     */
    public String getContext() {
        return context;
    }

    /**
     * Sets the context.
     * 
     * @param context the context to set
     */
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * Getter for childParameters.
     * 
     * @return the childParameters
     */
    public Map<String, IElementParameter> getChildParameters() {
        if (childParameters == null) {
            childParameters = new HashMap<String, IElementParameter>();
        }
        return childParameters;
    }

    public IElementParameter getParentParameter() {
        return parentParameter;
    }

    public void setParentParameter(IElementParameter parentParameter) {
        this.parentParameter = parentParameter;
        // keep the same category with parent.
        this.setCategory(parentParameter.getCategory());
        parentParameter.getChildParameters().put(this.getName(), this);
    }

    public boolean isDisplayedByDefault() {
        return this.show;
    }

    /**
     * Getter for currentRow.
     * 
     * @return the currentRow
     */
    public int getCurrentRow() {
        return this.currentRow;
    }

    /**
     * Sets the currentRow.
     * 
     * @param currentRow the currentRow to set
     */
    public void setCurrentRow(int currentRow) {
        this.currentRow = currentRow;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#getGroup()
     */
    public String getGroup() {
        return this.groupName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#setGroup(java.lang.String)
     */
    public void setGroup(String groupName) {
        this.groupName = groupName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#getGroupDisplayName()
     */
    public String getGroupDisplayName() {
        return this.groupDisplayName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#setGroupDisplayName(java.lang.String)
     */
    public void setGroupDisplayName(String groupDisplayName) {
        this.groupDisplayName = groupDisplayName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#getContextMode()
     */
    public boolean isContextMode() {
        return this.contextMode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#setContextMode(java.lang.String)
     */
    public void setContextMode(boolean mode) {
        this.contextMode = mode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#getLabelFromRepository()
     */
    public String getLabelFromRepository() {
        return this.labelFromRepository;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#setLabelFromRepository(java.lang.String)
     */
    public void setLabelFromRepository(String label) {
        this.labelFromRepository = label;

    }

    /**
     * Getter for color.
     * 
     * @return the color
     */
    public RGB getColor() {
        return this.color;
    }

    /**
     * Sets the color.
     * 
     * @param color the color to set
     */
    public void setColor(RGB color) {
        this.color = color;
    }

    /**
     * Getter for backgroundColor.
     * 
     * @return the backgroundColor
     */
    public RGB getBackgroundColor() {
        return this.backgroundColor;
    }

    /**
     * Sets the backgroundColor.
     * 
     * @param backgroundColor the backgroundColor to set
     */
    public void setBackgroundColor(RGB backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#isBasedOnSubjobStarts()
     */
    public boolean isBasedOnSubjobStarts() {
        return basedOnSubjobStarts;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#setBasedOnSubjobStarts(boolean)
     */
    public void setBasedOnSubjobStarts(boolean basedOnSubjobStarts) {
        this.basedOnSubjobStarts = basedOnSubjobStarts;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#isDynamicSettings()
     */
    public boolean isDynamicSettings() {
        return this.dynamicSettings;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#setDynamicSettings(java.lang.Boolean)
     */
    public void setDynamicSettings(boolean dynamicSettings) {
        this.dynamicSettings = dynamicSettings;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#getListItemsNotReadOnlyIf()
     */
    public String[] getListItemsNotReadOnlyIf() {
        return this.itemsNotReadOnlyIf;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#getListItemsReadOnlyIf()
     */
    public String[] getListItemsReadOnlyIf() {
        return this.itemsReadOnlyIf;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#getNotReadOnlyIf()
     */
    public String getNotReadOnlyIf() {
        // TODO Auto-generated method stub
        return this.notReadonlyIf;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#getReadOnlyIf()
     */
    public String getReadOnlyIf() {
        // TODO Auto-generated method stub
        return this.readonlyIf;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#isReadOnly(java.util.List)
     */
    public boolean isReadOnly(List<? extends IElementParameter> listParam) {
        boolean readonlyParameter = false;

        if (((readonlyIf != null) || (this.notReadonlyIf != null)) && !readOnly) {
            if (readonlyIf != null) {
                readonlyParameter = Expression.evaluate(readonlyIf, listParam, this);
            } else {
                readonlyParameter = !Expression.evaluate(this.notReadonlyIf, listParam, this);
            }
        } else {
            readonlyParameter = readOnly;
        }
        return readonlyParameter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#isReadOnly(java.lang.String, java.lang.String,
     * java.util.Map)
     */
    public boolean isReadOnly(String conditionReadOnlyIf, String conditionNotReadOnlyIf,
            List<? extends IElementParameter> listParam) {
        boolean readonlyParameter = false;

        if (((conditionReadOnlyIf != null) || (conditionNotReadOnlyIf != null)) && !readOnly) {
            if (conditionReadOnlyIf != null) {
                readonlyParameter = Expression.evaluate(conditionReadOnlyIf, listParam, this);
            } else {
                readonlyParameter = !Expression.evaluate(conditionNotReadOnlyIf, listParam, this);
            }
        } else {
            readonlyParameter = readOnly;
        }
        return readonlyParameter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#setListItemsNotReadOnlyIf(java.lang.String[])
     */
    public void setListItemsNotReadOnlyIf(String[] list) {
        itemsNotReadOnlyIf = list;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#setListItemsReadOnlyIf(java.lang.String[])
     */
    public void setListItemsReadOnlyIf(String[] list) {
        itemsReadOnlyIf = list;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#setNotReadOnlyIf(java.lang.String)
     */
    public void setNotReadOnlyIf(String notReadOnly) {
        this.notReadonlyIf = notReadOnly;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.IElementParameter#setReadOnlyIf(java.lang.String)
     */
    public void setReadOnlyIf(String readOnly) {
        this.readonlyIf = readOnly;
    }

    public boolean isColumnsBasedOnSchema() {
        return this.columnsBasedOnSchema;
    }

    public void setColumnsBasedOnSchema(boolean columnsBasedOnSchema) {
        this.columnsBasedOnSchema = columnsBasedOnSchema;
    }

    public boolean isNoContextAssist() {
        return this.noContextAssist;
    }

    public void setNoContextAssist(boolean enable) {
        this.noContextAssist = enable;
    }

    public IElementParameter getClone() {
        IElementParameter clone = new ElementParameter(this.element);

        final RGB bgc = getBackgroundColor();
        if (bgc != null) {
            clone.setBackgroundColor(new RGB(bgc.red, bgc.green, bgc.blue));
        }
        clone.setBasedOnSchema(isBasedOnSchema());
        clone.setBasedOnSubjobStarts(isBasedOnSubjobStarts());
        clone.setCategory(getCategory());
        final RGB c = getColor();
        if (c != null) {
            clone.setColor(new RGB(c.red, c.green, c.blue));
        }
        clone.setContext(getContext());
        clone.setContextMode(isContextMode());
        clone.setDefaultClosedListValue(getDefaultClosedListValue()); // ?
        clone.setDefaultValues(getDefaultValues()); // ?
        clone.setDisplayName(getDisplayName());
        clone.setDynamicSettings(isDynamicSettings());
        clone.setElement(getElement()); // maybe, need reset it after.
        clone.setFieldType(getFieldType());
        clone.setFilter(getFilter());
        clone.setGroupDisplayName(getGroupDisplayName());
        clone.setLabelFromRepository(getLabelFromRepository());
        clone.setListItemsDisplayCodeName(getListItemsDisplayCodeName());
        clone.setListItemsDisplayName(getListItemsDisplayName());
        clone.setListItemsNotReadOnlyIf(getListItemsNotReadOnlyIf());
        clone.setListItemsNotShowIf(getListItemsNotShowIf());
        clone.setListItemsReadOnlyIf(getListItemsReadOnlyIf());
        clone.setListItemsShowIf(getListItemsShowIf());
        clone.setListItemsValue(getListItemsValue()); // ?
        clone.setListRepositoryItems(getListRepositoryItems());
        clone.setName(getName());
        clone.setNbLines(getNbLines());
        clone.setNoCheck(isNoCheck());
        clone.setNotReadOnlyIf(getNotReadOnlyIf());
        clone.setNotShowIf(getNotShowIf());
        clone.setNumRow(getNumRow());
       //changed by hqzhang for TDI 19754 start
//        final IElementParameter pParameter = getParentParameter();
//        if (pParameter != null) {
//            clone.setParentParameter(pParameter.getClone());
//        }
        clone.setReadOnly(isReadOnly());
        clone.setReadOnlyIf(getReadOnlyIf());
        clone.setRepositoryValue(getRepositoryValue());
        clone.setRepositoryValueUsed(isRepositoryValueUsed());
        clone.setRequired(isRequired());
        clone.setShow(isDisplayedByDefault());
        clone.setShowIf(getShowIf());
        clone.setValue(getValue()); // ?
        // clone.setValueToDefault(null)
        clone.setNoContextAssist(isNoContextAssist());
        if(this.getChildParameters().size()>0){
            for(String name : this.getChildParameters().keySet()){
                IElementParameter childParamClone = this.getChildParameters().get(name).getClone();
                clone.getChildParameters().put(name, childParamClone);
                childParamClone.setParentParameter(clone);
            }
        }//TDI 19754 end
        return clone;
    }

    /**
     * Getter for javaMethod.
     * 
     * @return the javaMethod
     */
    public String getJavaClass() {
        return this.javaClass;
    }

    /**
     * Sets the javaMethod.
     * 
     * @param javaMethod the javaMethod to set
     */
    public void setJavaClass(String javaClass) {
        this.javaClass = javaClass;
    }

    /**
     * Getter for jar.
     * 
     * @return the jar
     */
    public String getJar() {
        return this.jar;
    }

    /**
     * Sets the jar.
     * 
     * @param jar the jar to set
     */
    public void setJar(String jar) {
        this.jar = jar;
    }

    /**
     * Getter for args.
     * 
     * @return the args
     */
    public String[] getArgs() {
        return this.args;
    }

    /**
     * Sets the args.
     * 
     * @param args the args to set
     */
    public void setArgs(String[] args) {
        this.args = args;
    }

    /**
     * Getter for javaFunction.
     * 
     * @return the javaFunction
     */
    public String getJavaFunction() {
        return this.javaFunction;
    }

    /**
     * Sets the javaFunction.
     * 
     * @param javaFunction the javaFunction to set
     */
    public void setJavaFunction(String javaFunction) {
        this.javaFunction = javaFunction;
    }

    /**
     * DOC Administrator Comment method "setMaxLength".
     * 
     * @param maxlength
     */
    public void setMaxLength(int maxlength) {
        this.maxlength = maxlength;
    }

    /**
     * Getter for maxlength.
     * 
     * @return the maxlength
     */
    public int getMaxlength() {
        return this.maxlength;
    }
}
