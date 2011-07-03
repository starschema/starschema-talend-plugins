// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.model.process.jobsettings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IMultipleComponentItem;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.INodeReturn;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.temp.ECodePart;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.MultipleComponentConnection;
import org.talend.designer.core.model.components.MultipleComponentManager;
import org.talend.designer.core.model.process.jobsettings.JobSettingsConstants.ContextLoadInfo;

/**
 * DOC ggu class global comment. Detailled comment <br/>
 * 
 * This class will create a virtual component that will create the logs for the job. <br/>
 * 
 * It's not used at all in the designer, only during the code generation. <br/>
 */
public class JobContextLoadComponent implements IComponent {

    private static final String FILE_INPUT_DELIMITED = "Delimited"; //$NON-NLS-1$

    private static final String FILE_INPUT_COMPONENT = "tFileInputDelimited"; //$NON-NLS-1$

    private static final String CONTEXT_LOAD = "Context"; //$NON-NLS-1$

    public static final String CONTEXTLOAD_COMPONENT = "tContextLoad"; //$NON-NLS-1$

    private static final String DB_INPUT = "Database"; //$NON-NLS-1$

    private final String dbComponent;

    private final List<IMultipleComponentManager> multipleComponentManagers = new ArrayList<IMultipleComponentManager>();

    private final boolean isFile;

    public JobContextLoadComponent(boolean isFile, String dbComponent) {
        this.isFile = isFile;
        this.dbComponent = dbComponent;
        loadMultipleComponentManager();
    }

    protected void loadMultipleComponentManager() {
        IMultipleComponentManager multipleComponentManager = null;
        // create base items
        if (isFile) {
            multipleComponentManager = new MultipleComponentManager(FILE_INPUT_DELIMITED, CONTEXT_LOAD);

            IMultipleComponentItem currentItem = multipleComponentManager.addItem(FILE_INPUT_DELIMITED, FILE_INPUT_COMPONENT);
            currentItem.getOutputConnections().add(
                    new MultipleComponentConnection(EConnectionType.FLOW_MAIN.getName(), CONTEXT_LOAD));

            currentItem = multipleComponentManager.addItem(CONTEXT_LOAD, CONTEXTLOAD_COMPONENT);

        } else {
            if (dbComponent == null) {
                return;
            }
            multipleComponentManager = new MultipleComponentManager(DB_INPUT, CONTEXT_LOAD);

            IMultipleComponentItem currentItem = multipleComponentManager.addItem(DB_INPUT, dbComponent);
            currentItem.getOutputConnections().add(
                    new MultipleComponentConnection(EConnectionType.FLOW_MAIN.getName(), CONTEXT_LOAD));

            currentItem = multipleComponentManager.addItem(CONTEXT_LOAD, CONTEXTLOAD_COMPONENT);

        }
        multipleComponentManager.validateItems();
        multipleComponentManagers.add(multipleComponentManager);
        createMultipleComponentsParameters();
    }

    // no use for virtual component
    public List<? extends INodeConnector> createConnectors(INode node) {
        return null;
    }

    public List<? extends INodeReturn> createReturns() {
        return null;
    }

    public List<ECodePart> getAvailableCodeParts() {
        return null;
    }

    public ImageDescriptor getIcon16() {
        return null;
    }

    public ImageDescriptor getIcon24() {
        return null;
    }

    public ImageDescriptor getIcon32() {
        return null;
    }

    public String getLongName() {
        return null;
    }

    public List<ModuleNeeded> getModulesNeeded() {
        return null;
    }

    public String getPathSource() {
        return null;
    }

    public List<String> getPluginDependencies() {
        return null;
    }

    public String getPluginFullName() {
        return null;
    }

    public String getTranslatedName() {
        return null;
    }

    public boolean hasConditionalOutputs() {
        return false;
    }

    public boolean isMultiplyingOutputs() {
        return false;
    }

    public boolean isDataAutoPropagated() {
        return false;
    }

    public boolean isLoaded() {
        return true;
    }

    public boolean isSchemaAutoPropagated() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isVisible(java.lang.String)
     */
    public boolean isVisible(String family) {
        return false;
    }

    public boolean isVisible() {
        return false;
    }

    public void setIcon16(ImageDescriptor icon16) {
    }

    public void setIcon24(ImageDescriptor icon24) {
    }

    public void setIcon32(ImageDescriptor icon32) {
    }

    public boolean useMerge() {
        return false;
    }

    // set name
    public String getName() {
        return this.getClass().getName();
    }

    public String getOriginalFamilyName() {
        return "Virtual"; //$NON-NLS-1$
    }

    public String getTranslatedFamilyName() {
        return "Virtual"; //$NON-NLS-1$
    }

    public String getVersion() {
        return VersionUtils.DEFAULT_VERSION;
    }

    public List<IMultipleComponentManager> getMultipleComponentManagers() {
        return multipleComponentManagers;
    }

    protected void createMultipleComponentsParameters() {
        final String self = "self."; //$NON-NLS-1$
        // create parameters
        if ((multipleComponentManagers != null) && (multipleComponentManagers.size() > 0)) {
            IMultipleComponentManager multipleComponentManager = multipleComponentManagers.get(0);
            if (isFile) {
                // delimited
                String source = self + EParameterName.IMPLICIT_TCONTEXTLOAD_FILE.getName();
                multipleComponentManager.addParam(source, FILE_INPUT_DELIMITED + ".FILENAME"); //$NON-NLS-1$ 

                source = self + EParameterName.FIELDSEPARATOR.getName();
                multipleComponentManager.addParam(source, FILE_INPUT_DELIMITED + ".FIELDSEPARATOR"); //$NON-NLS-1$ 

            } else {
                String source = self + JobSettingsConstants.getExtraParameterName(EParameterName.HOST.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".HOST"); //$NON-NLS-1$ 
                multipleComponentManager.addParam(source, DB_INPUT + ".SERVER"); //$NON-NLS-1$
                multipleComponentManager.addParam(source, DB_INPUT + ".DSN"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.PORT.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".PORT"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.DBNAME.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".DBNAME"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.DB_VERSION.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".DB_VERSION"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTIES.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".PROPERTIES"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.SCHEMA_DB.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".SCHEMA_DB"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.USER.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".USER"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.PASS.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".PASS"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.DBTABLE.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".DBTABLE"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.DB_TYPE.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".DB_TYPE"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.getExtraParameterName(EParameterName.CONNECTION_TYPE.getName());
                multipleComponentManager.addParam(source, DB_INPUT + ".CONNECTION_TYPE"); //$NON-NLS-1$ 

                source = self + JobSettingsConstants.QUERY;
                multipleComponentManager.addParam(source, DB_INPUT + "." + JobSettingsConstants.QUERY); //$NON-NLS-1$ //$NON-NLS-2$

            }
            // context parameter
            final String context = CONTEXT_LOAD + "."; //$NON-NLS-1$  

            String source = self + EParameterName.LOAD_NEW_VARIABLE.getName();
            String target = context + EParameterName.LOAD_NEW_VARIABLE.getName();
            multipleComponentManager.addParam(source, target);

            source = self + EParameterName.NOT_LOAD_OLD_VARIABLE.getName();
            target = context + EParameterName.NOT_LOAD_OLD_VARIABLE.getName();
            multipleComponentManager.addParam(source, target);

            source = self + EParameterName.PRINT_OPERATIONS.getName();
            target = context + EParameterName.PRINT_OPERATIONS.getName();
            multipleComponentManager.addParam(source, target);

            source = self + EParameterName.DISABLE_ERROR.getName();
            target = context + EParameterName.DISABLE_ERROR.getName();
            multipleComponentManager.addParam(source, target);

            source = self + EParameterName.DISABLE_INFO.getName();
            target = context + EParameterName.DISABLE_INFO.getName();
            multipleComponentManager.addParam(source, target);

            source = self + EParameterName.DISABLE_WARNINGS.getName();
            target = context + EParameterName.DISABLE_WARNINGS.getName();
            multipleComponentManager.addParam(source, target);
        }

    }

    public List<? extends IElementParameter> createElementParameters(INode node) {

        List<IElementParameter> elemParamList = new ArrayList<IElementParameter>();
        if (isFile) {
            // from file
            addFileInputDelimitedParameters(elemParamList, node);
        } else {
            // from database
            addDatabaseParameter(elemParamList, node);
        }
        // contextload
        addtContextLoadParameter(elemParamList, node);

        return elemParamList;
    }

    private void addFileInputDelimitedParameters(List<IElementParameter> elemParamList, INode node) {

        IElementParameter newParam = new ElementParameter(node);
        newParam.setName(EParameterName.IMPLICIT_TCONTEXTLOAD_FILE.getName());
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setValue(""); //$NON-NLS-1$
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(EParameterName.FIELDSEPARATOR.getName());
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setValue(""); //$NON-NLS-1$
        elemParamList.add(newParam);

    }

    private void addDatabaseParameter(List<IElementParameter> elemParamList, INode node) {
        //
        IElementParameter newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.HOST.getName()));
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.CONNECTION_TYPE.getName())); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("SERVER"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("DSN"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.PORT.getName())); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DBNAME.getName()));
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DB_VERSION.getName()));
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTIES.getName()));
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.SCHEMA_DB.getName()));
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.USER.getName()));
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.PASS.getName()));
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DBTABLE.getName())); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(JobSettingsConstants.QUERY);
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

    }

    private void addtContextLoadParameter(List<IElementParameter> elemParamList, INode node) {

        IElementParameter newParam = new ElementParameter(node);
        newParam.setName(EParameterName.LOAD_NEW_VARIABLE.getName());
        newParam.setFieldType(EParameterFieldType.CLOSED_LIST);
        newParam.setValue(ContextLoadInfo.WARNING);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(EParameterName.NOT_LOAD_OLD_VARIABLE.getName());
        newParam.setFieldType(EParameterFieldType.CLOSED_LIST);
        newParam.setValue(ContextLoadInfo.WARNING);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(EParameterName.PRINT_OPERATIONS.getName());
        newParam.setFieldType(EParameterFieldType.CHECK);
        newParam.setValue(false);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(EParameterName.DISABLE_ERROR.getName());
        newParam.setFieldType(EParameterFieldType.CHECK);
        newParam.setValue(false);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(EParameterName.DISABLE_INFO.getName());
        newParam.setFieldType(EParameterFieldType.CHECK);
        newParam.setValue(true);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName(EParameterName.DISABLE_WARNINGS.getName());
        newParam.setFieldType(EParameterFieldType.CHECK);
        newParam.setValue(true);
        elemParamList.add(newParam);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getComponentType()
     */
    public boolean isMultipleOutput() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#useLookup()
     */
    public boolean useLookup() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#useImport()
     */
    public boolean useImport() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getComponentType()
     */
    public EComponentType getComponentType() {
        return EComponentType.JOB_CONTEXT_LOAD;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isHashComponent()
     */
    public boolean isHashComponent() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isTechnical()
     */
    public boolean isTechnical() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isVisibleInComponentDefinition()
     */
    public boolean isVisibleInComponentDefinition() {
        return false;
    }

    public boolean isSingleton() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isMainCodeCalled()
     */
    public boolean isMainCodeCalled() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#canParallelize()
     */
    public boolean canParallelize() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getShortName()
     */
    public String getShortName() {
        return "icl";
    }

    public String getCombine() {
        return null;
    }

    public IProcess getProcess() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getPaletteType()
     */
    public String getPaletteType() {
        return "HIDDEN";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#setPaletteType(java.lang.String)
     */
    public void setPaletteType(String paletteType) {
        // TODO Auto-generated method stub

    }
}
