// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.model.migration;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * Support 3 version of tJavaFlex, see bug:11754.
 * 
 */
public class ThreeVersionstJavaFlexForBug11754 extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     * 
     * @seeorg.talend.core.model.migration.AbstractJobMigrationTask#executeOnProcess(org.talend.core.model.properties.
     * ProcessItem)
     */
    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        String[] componentsName = new String[] { "tJavaFlex" };

        try {

            for (int i = 0; i < componentsName.length; i++) {
                IComponentFilter filter = new NameComponentFilter(componentsName[i]);
                ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays
                        .<IComponentConversion> asList(new IComponentConversion() {

                            public void transform(NodeType node) {
                                ElementParameterType dataAuto = ComponentUtilities.getNodeProperty(node, "DATA_AUTO_PROPAGATE");
                                // it is TOS2.0, row1 = row2, they are the same referece to one Object
                                if (dataAuto == null) {

                                    ComponentUtilities.addNodeProperty(node, "DATA_AUTO_PROPAGATE", "CHECK");
                                    ComponentUtilities.getNodeProperty(node, "DATA_AUTO_PROPAGATE").setValue("true");

                                    // //////
                                    ComponentUtilities.addNodeProperty(node, "Version_V2_0", "CHECK");
                                    ComponentUtilities.getNodeProperty(node, "Version_V2_0").setValue("true");

                                    ComponentUtilities.addNodeProperty(node, "Version_V3_2", "CHECK");
                                    ComponentUtilities.getNodeProperty(node, "Version_V3_2").setValue("false");

                                    ComponentUtilities.addNodeProperty(node, "Version_V4.0", "CHECK");
                                    ComponentUtilities.getNodeProperty(node, "Version_V4.0").setValue("false");
                                } else {
                                    // it is TOS3.2, copy action AFTER code
                                    ComponentUtilities.addNodeProperty(node, "Version_V2_0", "CHECK");
                                    ComponentUtilities.getNodeProperty(node, "Version_V2_0").setValue("false");

                                    ComponentUtilities.addNodeProperty(node, "Version_V3_2", "CHECK");
                                    ComponentUtilities.getNodeProperty(node, "Version_V3_2").setValue("true");

                                    ComponentUtilities.addNodeProperty(node, "Version_V4.0", "CHECK");
                                    ComponentUtilities.getNodeProperty(node, "Version_V4.0").setValue("false");

                                }
                            }

                        }));
            }

            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2010, 3, 9, 12, 0, 0);
        return gc.getTime();
    }
}