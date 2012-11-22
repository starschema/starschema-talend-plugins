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
package org.talend.designer.core.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

    public static Set<ModuleNeeded> getNeededModules(final IProcess process, boolean withChildrens) {
        List<ModuleNeeded> modulesNeeded = new ArrayList<ModuleNeeded>();
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

        // call recursive function to get all dependencies from job & subjobs
        getNeededModules(process, withChildrens, searchItems, modulesNeeded);

        // remove duplicates, and keep by priority the one got bundle dependency setup
        Collections.sort(modulesNeeded, new Comparator<ModuleNeeded>(){

            @Override
            public int compare(ModuleNeeded arg0, ModuleNeeded arg1) {
                if (arg0.getBundleName() == null && arg1.getBundleName() != null) {
                    return 1;
                }
                if (arg0.getBundleName() != null && arg1.getBundleName() != null
                        && "".equals(arg0.getBundleName()) && !"".equals(arg1.getBundleName())) {
                    return 1;
                }
                return 0;
            }
            
        });
        Set<String> dedupModulesList = new HashSet<String>();
        Iterator<ModuleNeeded> it = modulesNeeded.iterator();
        while (it.hasNext()) {
            ModuleNeeded module = it.next();
            if (dedupModulesList.contains(module.getModuleName())) {
                it.remove();
            } else {
                dedupModulesList.add(module.getModuleName());
            }
        }
        
        return new HashSet<ModuleNeeded>(modulesNeeded);
    }

    public static Set<String> getNeededLibraries(final IProcess process, boolean withChildrens) {
        Set<String> libsNeeded = new HashSet<String>();
        for (ModuleNeeded module : getNeededModules(process, withChildrens)) {
            libsNeeded.add(module.getModuleName());
        }

        return libsNeeded;
    }

    private static void getNeededModules(final IProcess process, boolean withChildrens, Set<ProcessItem> searchItems,
            List<ModuleNeeded> modulesNeeded) {
        IElementParameter headerParameter = process.getElementParameter(EParameterName.HEADER_LIBRARY.getName());
        if (headerParameter != null) {
            Object value = headerParameter.getValue();
            if (value != null) {
                String headerLibraries = (String) value;
                if (headerLibraries.indexOf(File.separatorChar) > 0
                        && headerLibraries.length() > headerLibraries.lastIndexOf(File.separatorChar) + 2) {
                    String substring = headerLibraries.substring(headerLibraries.lastIndexOf(File.separatorChar) + 2);
                    if (!"".equals(substring)) {//$NON-NLS-1$
                        modulesNeeded.add(getModuleValue(process, substring));
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
                        modulesNeeded.add(getModuleValue(process, substring));
                    }
                }
            }
        }

        IElementParameter elementParameter = process.getElementParameter(EParameterName.DRIVER_JAR.getName());
        if (elementParameter != null && elementParameter.getFieldType() == EParameterFieldType.TABLE) {
            getModulesInTable(process, elementParameter, modulesNeeded);
        }

        if (process instanceof IProcess2) {
            Item item = ((IProcess2) process).getProperty().getItem();
            if (item instanceof ProcessItem) {
                List<ModuleNeeded> modulesNeededForRoutines = ModulesNeededProvider
                        .getModulesNeededForRoutines((ProcessItem) item);
                if (modulesNeededForRoutines != null) {
                    modulesNeeded.addAll(modulesNeededForRoutines);
                }
            }
        }

        List<? extends INode> nodeList = process.getGeneratingNodes();
        for (INode node : nodeList) {
            List<ModuleNeeded> moduleList = node.getModulesNeeded();
            for (ModuleNeeded needed : moduleList) {
                if (needed.isRequired(node.getElementParameters())) {
                    modulesNeeded.add(needed);
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
                        modulesNeeded.add(getModuleValue(process, (String) curParam.getValue()));
                    }
                } else if (curParam.getFieldType() == EParameterFieldType.TABLE) {
                    getModulesInTable(process, curParam, modulesNeeded);
                }

                // see feature 4720 Add libraries for different version DB components and tMomInput components
                // IElementParameter elementParameter = node.getElementParameter("USE_EXISTING_CONNECTION");
                // if (elementParameter != null && elementParameter.isShow(node.getElementParameters())
                // && Boolean.TRUE.equals(elementParameter.getValue())) {
                if (curParam.isShow(node.getElementParameters())) {
                    findMoreLibraries(process, modulesNeeded, curParam, true);
                } else {
                    findMoreLibraries(process, modulesNeeded, curParam, false);
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
                        JavaProcessUtil.getNeededModules(child, true, searchItems, modulesNeeded);
                    }
                }
            }
        }
    }

    private static void getModulesInTable(final IProcess process, IElementParameter curParam, List<ModuleNeeded> modulesNeeded) {

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

                                                if (curParam.getName().equals(EParameterName.DRIVER_JAR.getName())
                                                        && value.contains(";")) {
                                                    String[] jars = value.split(";");
                                                    for (int i = 0; i < jars.length; i++) {
                                                        String jar = jars[i];
                                                        jar = jar.substring(jar.lastIndexOf("\\") + 1);
                                                        ModuleNeeded module = new ModuleNeeded(null, jar, null, true);
                                                        modulesNeeded.add(module);
                                                    }
                                                } else {
                                                    value = value.substring(value.lastIndexOf("\\") + 1);
                                                    ModuleNeeded module = new ModuleNeeded(null, value, null, true);
                                                    modulesNeeded.add(module);
                                                }
                                            }
                                        }
                                    }

                                } else {
                                    modulesNeeded.add(getModuleValue(process, moduleName));
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private static ModuleNeeded getModuleValue(final IProcess process, String moduleValue) {
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
                    return new ModuleNeeded(null, filename, null, true);
                }
            }
        }
        return new ModuleNeeded(null, TalendTextUtils.removeQuotes(moduleValue), null, true);
    }

    /**
     * DOC YeXiaowei Comment method "findMoreLibraries".
     * 
     * @param neededLibraries
     * @param curParam
     */
    public static void findMoreLibraries(final IProcess process, List<ModuleNeeded> modulesNeeded, IElementParameter curParam,
            boolean flag) {

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
                            if (driverName != null && !"".equals(driverName)) {
                                boolean isContextMode = ContextParameterUtils.containContextVariables(driverName);
                                if (isContextMode) {
                                    getModulesInTable(process, curParam, modulesNeeded);
                                } else {
                                    ModuleNeeded module = new ModuleNeeded(null, (driverName).replaceAll(
                                            TalendTextUtils.QUOTATION_MARK, "").replaceAll( //$NON-NLS-1$
                                            TalendTextUtils.SINGLE_QUOTE, ""), null, true);//$NON-NLS-1$
                                    modulesNeeded.add(module);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (curParam.getName().equals("DB_VERSION")) { //$NON-NLS-1$
            String jdbcName = (String) value;
            //
            if (jdbcName != null && !jdbcName.equals("Access_2003") && !jdbcName.equals("Access_2007")) {
                if (jdbcName.contains("11g")) { //$NON-NLS-1$
                    if (System.getProperty("java.version").startsWith("1.6")) { //$NON-NLS-1$ //$NON-NLS-2$
                        jdbcName = jdbcName.replace('5', '6');
                    } else {
                        jdbcName = jdbcName.replace('6', '5');
                    }
                }

                if (flag == true) {
                    String jars = (jdbcName).replaceAll(TalendTextUtils.QUOTATION_MARK, "").replaceAll( //$NON-NLS-1$
                            TalendTextUtils.SINGLE_QUOTE, "");
                    String separator = ";"; //$NON-NLS-1$
                    if (jars.contains(separator)) {
                        for (String jar : jars.split(separator)) {
                            ModuleNeeded module = new ModuleNeeded(null, jar, null, true);
                            modulesNeeded.add(module);
                        }
                    } else {
                        ModuleNeeded module = new ModuleNeeded(null, jars, null, true);
                        modulesNeeded.add(module);
                    }
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
                ModuleNeeded module = new ModuleNeeded(null, jar, null, true);
                modulesNeeded.add(module);
            }
        }
        if (curParam.getName().equals("HOTLIBS")) { //$NON-NLS-1$
            List<Map<String, Object>> tableValues = (List<Map<String, Object>>) value;

            for (Map<String, Object> line : tableValues) {
                if (line.containsKey("LIBPATH") && !StringUtils.isEmpty((String) line.get("LIBPATH"))) {
                    String path = (String) line.get("LIBPATH");
                    ModuleNeeded module = new ModuleNeeded(null, TalendTextUtils.removeQuotes(path), null, true);
                    modulesNeeded.add(module);
                }
            }
        }
    }

    /**
     * 
     * DOC hcyi Comment method "getContextOriginalValue".
     * 
     * @param process
     * @param contextStr
     */
    public static String getContextOriginalValue(final IProcess process, String contextStr) {
        String originalValue = null;
        IContext context = process.getContextManager().getDefaultContext();
        if (context != null) {
            List<IContextParameter> contextParameterList = context.getContextParameterList();
            for (IContextParameter contextPara : contextParameterList) {
                String var = ContextParameterUtils.getVariableFromCode(contextStr);
                if (var.equals(contextPara.getName())) {
                    originalValue = context.getContextParameter(contextPara.getName()).getValue();
                    return originalValue;
                }
            }
        }
        return null;
    }
}
