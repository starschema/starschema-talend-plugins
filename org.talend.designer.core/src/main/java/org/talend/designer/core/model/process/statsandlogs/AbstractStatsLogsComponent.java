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
package org.talend.designer.core.model.process.statsandlogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IMultipleComponentItem;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.INodeReturn;
import org.talend.core.model.temp.ECodePart;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.MultipleComponentConnection;
import org.talend.designer.core.model.components.MultipleComponentManager;

/**
 * These components won't be used in the designer part, only for the generation. <br/>
 * 
 */
public abstract class AbstractStatsLogsComponent implements IComponent {

    protected boolean useDb = false;

    protected boolean useConsole = false;

    protected String dbComponent;

    protected boolean useFile = false;

    protected String componentId;

    protected String subComponent;

    protected List<IMultipleComponentManager> multipleComponentManagers = new ArrayList<IMultipleComponentManager>();

    // no use for virtual component
    public List<? extends INodeConnector> createConnectors(INode node) {
        return null;
    }

    // no use for virtual component
    public List<? extends INodeReturn> createReturns() {
        return null;
    }

    // no use for virtual component
    public List<ECodePart> getAvailableCodeParts() {
        return null;
    }

    // no use for virtual component
    public String getOriginalFamilyName() {
        return "Virtual"; //$NON-NLS-1$
    }

    public String getTranslatedFamilyName() {
        return "Virtual"; //$NON-NLS-1$
    }

    // no use for virtual component
    public ImageDescriptor getIcon16() {
        return null;
    }

    // no use for virtual component
    public ImageDescriptor getIcon24() {
        return null;
    }

    // no use for virtual component
    public ImageDescriptor getIcon32() {
        return null;
    }

    // no use for virtual component
    public String getLongName() {
        return null;
    }

    // no use for virtual component ?
    public List<ModuleNeeded> getModulesNeeded() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getName()
     */
    public String getName() {
        return this.getClass().getName();
    }

    // no use for virtual component
    public String getPathSource() {
        return null;
    }

    // no use for virtual component
    public List<String> getPluginDependencies() {
        return null;
    }

    // no use for virtual component
    public String getPluginFullName() {
        return null;
    }

    // no use for virtual component
    public String getTranslatedName() {
        return null;
    }

    // no use for virtual component
    public boolean hasConditionalOutputs() {
        return false;
    }

    // no use for virtual component
    public boolean isMultiplyingOutputs() {
        return false;
    }

    // no use for virtual component
    public boolean isDataAutoPropagated() {
        return false;
    }

    // no use for virtual component
    public boolean isLoaded() {
        return true;
    }

    // no use for virtual component
    public boolean isSchemaAutoPropagated() {
        return false;
    }

    // no use for virtual component
    public boolean isVisible() {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#isVisibleInComponentDefinition()
     */
    public boolean isVisibleInComponentDefinition() {
        return false;
    }

    // no use for virtual component
    public void setIcon16(ImageDescriptor icon16) {
    }

    // no use for virtual component
    public void setIcon24(ImageDescriptor icon24) {
    }

    // no use for virtual component
    public void setIcon32(ImageDescriptor icon32) {
    }

    // no use for virtual component
    public boolean useMerge() {
        return false;
    }

    public List<? extends IElementParameter> createElementParameters(INode node) {
        List<IElementParameter> elemParamList = new ArrayList<IElementParameter>();

        addFileOutputParameters(elemParamList, node);
        addDbParameters(elemParamList, node);
        return elemParamList;
    }

    protected void addDbParameters(List<IElementParameter> elemParamList, INode node) {
        // parameters for db output.
        IElementParameter newParam = new ElementParameter(node);
        newParam.setName("HOST"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        // This parameter is use for Connection Component
        newParam = new ElementParameter(node);
        newParam.setName("USE_EXISTING_CONNECTION"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.CHECK);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("CONNECTION"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        // This parameter is use for Oracle component
        newParam = new ElementParameter(node);
        newParam.setName("CONNECTION_TYPE"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setShow(false);
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
        newParam.setName("PORT"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("DBNAME"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("DB_VERSION"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("PROPERTIES"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("SCHEMA_DB"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("USER"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("PASS"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("TABLE"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("TABLE_ACTION"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setValue("CREATE_IF_NOT_EXISTS"); //$NON-NLS-1$
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("DATA_ACTION"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setValue("INSERT"); //$NON-NLS-1$
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("COMMIT_EVERY"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setValue("1"); //$NON-NLS-1$
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("USE_SHARED_CONNECTION"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.CHECK);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("SHARED_CONNECTION_NAME"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("USE_TRANSACTION"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.CHECK);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("LOCAL_SERVICE_NAME"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

    }

    protected void addFileOutputParameters(List<IElementParameter> elemParamList, INode node) {
        // parameters for file output.
        IElementParameter newParam = new ElementParameter(node);
        newParam.setName("FILENAME"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("ROWSEPARATOR"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setValue(TalendTextUtils.addQuotes("\\n", TalendTextUtils.QUOTATION_MARK)); //$NON-NLS-1$
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("FIELDSEPARATOR"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setValue(TalendTextUtils.addQuotes(";")); //$NON-NLS-1$
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("APPEND"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.CHECK);
        newParam.setValue(Boolean.TRUE);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("INCLUDEHEADER"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.CHECK);
        newParam.setValue(Boolean.FALSE);
        elemParamList.add(newParam);

        newParam = new ElementParameter(node);
        newParam.setName("ENCODING"); //$NON-NLS-1$
        newParam.setFieldType(EParameterFieldType.TEXT);
        newParam.setValue(TalendTextUtils.addQuotes("ISO-8859-15")); //$NON-NLS-1$
        elemParamList.add(newParam);

    }

    protected void loadMultipleComponentManager() {
        String lastComponent = null;
        if (useConsole) {
            lastComponent = "CONSOLE"; //$NON-NLS-1$
        } else if (useDb) {
            lastComponent = "DB"; //$NON-NLS-1$
        } else {
            lastComponent = "FILE"; //$NON-NLS-1$
        }
        // create base items
        IMultipleComponentManager multipleComponentManager = new MultipleComponentManager(componentId, lastComponent);
        IMultipleComponentItem currentItem = multipleComponentManager.addItem(componentId, subComponent);
        if (useFile) {
            currentItem.getOutputConnections().add(new MultipleComponentConnection("FLOW", "FILE")); //$NON-NLS-1$ //$NON-NLS-2$
            currentItem = multipleComponentManager.addItem("FILE", "tFileOutputDelimited"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (useDb) {
            currentItem.getOutputConnections().add(new MultipleComponentConnection("FLOW", "DB")); //$NON-NLS-1$ //$NON-NLS-2$
            currentItem = multipleComponentManager.addItem("DB", dbComponent); //$NON-NLS-1$
        }
        if (useConsole) {
            currentItem.getOutputConnections().add(new MultipleComponentConnection("FLOW", "CONSOLE")); //$NON-NLS-1$ //$NON-NLS-2$
            currentItem = multipleComponentManager.addItem("CONSOLE", "tLogRow"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        multipleComponentManager.validateItems();
        multipleComponentManagers.add(multipleComponentManager);
        createMultipleComponentsParameters();
    }

    protected void createMultipleComponentsParameters() {
        // create parameters
        if ((multipleComponentManagers != null) && (multipleComponentManagers.size() > 0)) {
            IMultipleComponentManager multipleComponentManager = multipleComponentManagers.get(0);
            if (useFile) {
                multipleComponentManager.addParam("self.FILENAME", "FILE.FILENAME"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.ROWSEPARATOR", "FILE.ROWSEPARATOR"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.FIELDSEPARATOR", "FILE.FIELDSEPARATOR"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.APPEND", "FILE.APPEND"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.INCLUDEHEADER", "FILE.INCLUDEHEADER"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.ENCODING", "FILE.ENCODING"); //$NON-NLS-1$ //$NON-NLS-2$
            }

            if (useDb) {
                multipleComponentManager.addParam("self.USE_EXISTING_CONNECTION", "DB.USE_EXISTING_CONNECTION"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.CONNECTION", "DB.CONNECTION"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.COMMIT_EVERY", "DB.COMMIT_EVERY"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.HOST", "DB.HOST"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.HOST", "DB.SERVER"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.HOST", "DB.DSN"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.PORT", "DB.PORT"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.DBNAME", "DB.DBNAME"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.DB_VERSION", "DB.DB_VERSION"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.PROPERTIES", "DB.PROPERTIES"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.USER", "DB.USER"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.PASS", "DB.PASS"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.TABLE", "DB.TABLE"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.TABLE_ACTION", "DB.TABLE_ACTION"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.DATA_ACTION", "DB.DATA_ACTION"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.COMMIT_EVERY", "DB.COMMIT_EVERY"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.SCHEMA_DB", "DB.SCHEMA_DB"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.DB_TYPE", "DB.DB_TYPE"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.CONNECTION_TYPE", "DB.CONNECTION_TYPE"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.USE_TRANSACTION", "DB.USE_TRANSACTION"); //$NON-NLS-1$ //$NON-NLS-2$
                multipleComponentManager.addParam("self.LOCAL_SERVICE_NAME", "DB.LOCAL_SERVICE_NAME"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getMultipleComponentManager()
     */
    public List<IMultipleComponentManager> getMultipleComponentManagers() {
        return multipleComponentManagers;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#getComponentType()
     */
    public boolean isMultipleOutput() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#useLookup()
     */
    public boolean useLookup() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IComponent#useImport()
     */
    public boolean useImport() {
        // TODO Auto-generated method stub
        return false;
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
        return "slog";
    }

    public String getCombine() {
        return null;
    }

    public String getPaletteType() {
        return "HIDDEN";
    }

    public void setPaletteType(String paletteType) {
        // TODO Auto-generated method stub

    }
}
