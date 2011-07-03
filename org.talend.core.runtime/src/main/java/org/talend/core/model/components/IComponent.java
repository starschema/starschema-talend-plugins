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
package org.talend.core.model.components;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.INodeReturn;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.temp.ECodePart;

/**
 * Interface that describes the functions that a must implements a component manager. <br/>
 * 
 * $Id: IComponent.java 40568 2010-04-19 02:10:00Z nrousseau $
 */
public interface IComponent {

    String JOBLET_PID = "org.talend.designer.joblet"; //$NON-NLS-1$

    String PROP_NAME = "NAME"; //$NON-NLS-1$

    String PROP_LONG_NAME = "LONG_NAME"; //$NON-NLS-1$

    String PROP_FAMILY = "FAMILY"; //$NON-NLS-1$

    String PROP_MENU = "MENU"; //$NON-NLS-1$

    String PROP_LINK = "LINK"; //$NON-NLS-1$

    String PROP_HELP = "HELP"; //$NON-NLS-1$

    String FAMILY = "Joblets";//$NON-NLS-1$

    public String getName();

    public String getLongName();

    public String getOriginalFamilyName();

    public String getTranslatedFamilyName();

    public ImageDescriptor getIcon32();

    public void setIcon32(ImageDescriptor icon32);

    public ImageDescriptor getIcon24();

    public void setIcon24(ImageDescriptor icon24);

    public ImageDescriptor getIcon16();

    public void setIcon16(ImageDescriptor icon16);

    public List<? extends IElementParameter> createElementParameters(INode node);

    public List<? extends INodeReturn> createReturns();

    public List<? extends INodeConnector> createConnectors(INode node);

    public boolean hasConditionalOutputs();

    public boolean isMultiplyingOutputs();

    public String getPluginFullName();

    public boolean isSchemaAutoPropagated();

    public boolean isDataAutoPropagated();

    public boolean isHashComponent();

    public boolean useMerge();

    public boolean useLookup();

    public String getVersion();

    public List<IMultipleComponentManager> getMultipleComponentManagers();

    public boolean isLoaded();

    public boolean isVisible();

    public boolean isVisible(String family);

    public List<ModuleNeeded> getModulesNeeded();

    public String getPathSource();

    public List<ECodePart> getAvailableCodeParts();

    public List<String> getPluginDependencies();

    public boolean isMultipleOutput();

    public boolean useImport();

    public EComponentType getComponentType();

    /**
     * Return true if this component is technical, means should not be displayed in the palette but must be generated
     * 
     * @return
     */
    public boolean isTechnical();

    public boolean isVisibleInComponentDefinition();

    public boolean isSingleton();

    public boolean isMainCodeCalled();

    public boolean canParallelize();

    public String getShortName();

    // see 17353
    public String getCombine();

    public IProcess getProcess();

    public String getPaletteType();

    public void setPaletteType(String paletteType);
}
