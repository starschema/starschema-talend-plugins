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
package org.talend.designer.core.utils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.core.CorePlugin;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.librariesmanager.model.ModulesNeededProvider;

/**
 * DOC xye class global comment. Detailled comment
 */
public class JavaProcessUtil {

    public static Set<String> getNeededLibraries(final IProcess process, boolean withChildrens) {
        // see bug 4939: making tRunjobs work loop will cause a error of "out of memory"
        Set<ProcessItem> searchItems = new HashSet<ProcessItem>();
        if (withChildrens) {
            ProcessItem processItem = null;
            if (process.getVersion() != null) {
                processItem = ItemCacheManager.getProcessItem(process.getId(), process.getVersion());
            } else {
                processItem = ItemCacheManager.getProcessItem(process.getId());
            }
            if (processItem != null) {
                searchItems.add(processItem);
            }
        }
        return getNeededLibraries(process, withChildrens, searchItems);
    }

    private static Set<String> getNeededLibraries(final IProcess process, boolean withChildrens, Set<ProcessItem> searchItems) {
        Set<String> neededLibraries = new HashSet<String>();
        IElementParameter headerParameter = process.getElementParameter(EParameterName.HEADER_LIBRARY.getName());
        if (headerParameter != null) {
            Object value = headerParameter.getValue();
            if (value != null) {
                String headerLibraries = (String) value;
                if (headerLibraries.indexOf(File.separatorChar) > 0
                        && headerLibraries.length() > headerLibraries.lastIndexOf(File.separatorChar) + 2) {
                    String substring = headerLibraries.substring(headerLibraries.lastIndexOf(File.separatorChar) + 2);
                    if (!"".equals(substring)) {//$NON-NLS-1$
                        neededLibraries.add(getModuleValue(process, substring));
                    }
                }
            }
        }
        IElementParameter footerParameter = process.getElementParameter(EParameterName.FOOTER_LIBRARY.getName());
        if (footerParameter != null) {
            Object value = footerParameter.getValue();
            if (value != null) {
                String footerLibraries = (String) value;
                if (footerLibraries.indexOf(File.separatorChar) > 0
                        && footerLibraries.length() > footerLibraries.lastIndexOf(File.separatorChar) + 2) {
                    String substring = footerLibraries.substring(footerLibraries.lastIndexOf(File.separatorChar) + 2);
                    if (!"".equals(substring)) {//$NON-NLS-1$
                        neededLibraries.add(getModuleValue(process, substring));
                    }
                }
            }
        }

        IElementParameter elementParameter = process.getElementParameter(EParameterName.DRIVER_JAR.getName());
        if (elementParameter != null && elementParameter.getFieldType() == EParameterFieldType.TABLE) {
            getModulsInTable(process, elementParameter, neededLibraries);
        }

        if (process instanceof IProcess2) {
            Item item = ((IProcess2) process).getProperty().getItem();
            if (item instanceof ProcessItem) {
                List<ModuleNeeded> modulesNeededForRoutines = ModulesNeededProvider
                        .getModulesNeededForRoutines((ProcessItem) item);
                if (modulesNeededForRoutines != null) {
                    for (ModuleNeeded moduleNeeded : modulesNeededForRoutines) {
                        neededLibraries.add(moduleNeeded.getModuleName());
                    }
                }
            }
        }

        List<? extends INode> nodeList = process.getGeneratingNodes();
        for (INode node : nodeList) {
            List<ModuleNeeded> moduleList = node.getComponent().getModulesNeeded();
            for (ModuleNeeded needed : moduleList) {
                if (needed.isRequired()) {
                    neededLibraries.add(needed.getModuleName());
                }
            }
            for (IElementParameter curParam : node.getElementParameters()) {
                if (curParam.getFieldType() == null) {
                    continue; // field can be null in some really specific cases, like for example when preview from
                    // wizard.
                }
                if (curParam.getFieldType().equals(EParameterFieldType.MODULE_LIST)) {
                    if (curParam.getValue() != null && !"".equals(curParam.getValue())) { // if the parameter //$NON-NLS-1$
                        // is not empty.
                        neededLibraries.add(getModuleValue(process, (String) curParam.getValue()));
                    }
                } else if (curParam.getFieldType() == EParameterFieldType.TABLE) {
                    getModulsInTable(process, curParam, neededLibraries);
                }

                // see feature 4720 Add libraries for different version DB components and tMomInput components
                // IElementParameter elementParameter = node.getElementParameter("USE_EXISTING_CONNECTION");
                // if (elementParameter != null && elementParameter.isShow(node.getElementParameters())
                // && Boolean.TRUE.equals(elementParameter.getValue())) {
                if (curParam.isShow(node.getElementParameters())) {
                    findMoreLibraries(neededLibraries, curParam, true);
                } else {
                    findMoreLibraries(neededLibraries, curParam, false);
                }
            }

            if (withChildrens) {
                if (node.getComponent().getName().equals("tRunJob")) { //$NON-NLS-1$
                    IElementParameter processIdparam = node.getElementParameter("PROCESS_TYPE_PROCESS"); //$NON-NLS-1$
                    IElementParameter processVersionParam = node.getElementParameter(EParameterName.PROCESS_TYPE_VERSION
                            .getName());

                    ProcessItem processItem = null;
                    if (processVersionParam != null) {
                        processItem = ItemCacheManager.getProcessItem((String) processIdparam.getValue(),
                                (String) processVersionParam.getValue());
                    } else {
                        processItem = ItemCacheManager.getProcessItem((String) processIdparam.getValue());
                    }

                    String context = (String) node.getElementParameter("PROCESS_TYPE_CONTEXT").getValue(); //$NON-NLS-1$
                    if (processItem != null && !searchItems.contains(processItem)) {
                        // avoid dead loop of method call
                        searchItems.add(processItem);
                        JobInfo subJobInfo = new JobInfo(processItem, context);
                        // achen modify to fix 0006107
                        IDesignerCoreService service = CorePlugin.getDefault().getDesignerCoreService();
                        IProcess child = service.getProcessFromItem(subJobInfo.getProcessItem());
                        // Process child = new Process(subJobInfo.getProcessItem().getProperty());
                        // child.loadXmlFile();
                        neededLibraries.addAll(JavaProcessUtil.getNeededLibraries(child, true, searchItems));
                    }
                }
            }
        }
        // return the jars needed for the job.
        return neededLibraries;

    }

    private static void getModulsInTable(final IProcess process, IElementParameter curParam, Set<String> neededLibraries) {

        if (!(curParam.getValue() instanceof List)) {
            return;
        }
        List<Map<String, Object>> values = (List<Map<String, Object>>) curParam.getValue();
        if (values != null && !values.isEmpty()) {
            Object[] listItemsValue = curParam.getListItemsValue();
            if (listItemsValue != null && listItemsValue.length > 0 && listItemsValue[0] instanceof IElementParameter) {
                for (Object o : listItemsValue) {
                    IElementParameter param = (IElementParameter) o;
                    if (param.getFieldType() == EParameterFieldType.MODULE_LIST) {
                        for (Map<String, Object> line : values) {
                            String moduleName = (String) line.get(param.getName());
                            if (moduleName != null && !"".equals(moduleName)) {

                                boolean isContextMode = ContextParameterUtils.containContextVariables(moduleName);
                                if (isContextMode) {
                                    List<IContext> listContext = process.getContextManager().getListContext();
                                    for (IContext context : listContext) {
                                        List<IContextParameter> contextParameterList = context.getContextParameterList();
                                        for (IContextParameter contextPara : contextParameterList) {
                                            String var = ContextParameterUtils.getVariableFromCode(moduleName);
                                            if (var.equals(contextPara.getName())) {
                                                String value = context.getContextParameter(contextPara.getName()).getValue();
                                                value = value.substring(value.lastIndexOf("\\") + 1);
                                                if (!neededLibraries.contains(value)) {
                                                    neededLibraries.add(value);
                                                }
                                            }
                                        }
                                    }

                                } else {
                                    moduleName = getModuleValue(process, moduleName);
                                    if (!neededLibraries.contains(moduleName)) {
                                        neededLibraries.add(moduleName);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private static String getModuleValue(final IProcess process, String moduleValue) {
        if (ContextParameterUtils.isContainContextParam(moduleValue)) {
            String var = ContextParameterUtils.getVariableFromCode(moduleValue);
            if (var != null) {
                IContext selectedContext = CorePlugin.getDefault().getRunProcessService().getSelectedContext();
                if (selectedContext == null) {
                    selectedContext = process.getContextManager().getDefaultContext();
                }
                IContextParameter param = selectedContext.getContextParameter(var);
                if (param != null) {
                    // add only the file name without path
                    String paramvalue = param.getValue();
                    int a = paramvalue.lastIndexOf("\\"); //$NON-NLS-1$
                    String filename = paramvalue.substring(a + 1, paramvalue.length());
                    return filename;
                }
            }
        }
        return TalendTextUtils.removeQuotes(moduleValue);
    }

    /**
     * DOC YeXiaowei Comment method "findMoreLibraries".
     * 
     * @param neededLibraries
     * @param curParam
     */
    public static void findMoreLibraries(Set<String> neededLibraries, IElementParameter curParam, boolean flag) {

        Object value = curParam.getValue();
        if (curParam.getName().equals("DRIVER_JAR")) {//$NON-NLS-N$
            // added for bug 13592. new parameter DRIVER_JAR was used for jdbc connection
            if (value != null && value instanceof List) {
                List list = (List) value;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i) instanceof HashMap) {
                        HashMap map = (HashMap) list.get(i);// JAR_NAME
                        Object object = map.get("JAR_NAME");//$NON-NLS-N$
                        if (object != null && object instanceof String) {
                            String driverName = (String) object;
                            neededLibraries.add((driverName).replaceAll(TalendTextUtils.QUOTATION_MARK, "").replaceAll( //$NON-NLS-1$
                                    TalendTextUtils.SINGLE_QUOTE, ""));//$NON-NLS-1$
                        }
                    }
                }
            }
        }

        if (curParam.getName().equals("DB_VERSION")) { //$NON-NLS-1$
            String jdbcName = (String) value;
            if (jdbcName != null) {
                if (jdbcName.contains("11g")) { //$NON-NLS-1$
                    if (System.getProperty("java.version").startsWith("1.6")) { //$NON-NLS-1$ //$NON-NLS-2$
                        jdbcName = jdbcName.replace('5', '6');
                    } else {
                        jdbcName = jdbcName.replace('6', '5');
                    }
                }

                if (flag == true) {
                    neededLibraries.add((jdbcName).replaceAll(TalendTextUtils.QUOTATION_MARK, "").replaceAll( //$NON-NLS-1$
                            TalendTextUtils.SINGLE_QUOTE, ""));//$NON-NLS-1$
                }
            }
        }

        String separator = ";"; //$NON-NLS-1$
        if (curParam.getName().equals("MQ_DERVIERS")) { //$NON-NLS-1$
            String path = (String) value;

            if (path == null || path.equals("")) { //$NON-NLS-1$
                return;
            }

            for (String jar : path.split(separator)) {
                neededLibraries.add(jar);
            }
        }
        if (curParam.getName().equals("HOTLIBS")) { //$NON-NLS-1$
            List<Map<String, Object>> tableValues = (List<Map<String, Object>>) value;

            for (Map<String, Object> line : tableValues) {
                if (line.containsKey("LIBPATH") && !StringUtils.isEmpty((String) line.get("LIBPATH"))) {
                    String path = (String) line.get("LIBPATH");
                    neededLibraries.add(TalendTextUtils.removeQuotes(path));
                }
            }
        }
    }
}
