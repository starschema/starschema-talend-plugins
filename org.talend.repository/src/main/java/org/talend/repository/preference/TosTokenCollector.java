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
package org.talend.repository.preference;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.DynaEnum;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.token.AbstractTokenCollector;
import org.talend.core.token.TokenInforUtil;
import org.talend.core.token.TokenKey;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.model.IProxyRepositoryFactory;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * ggu class global comment. Detailled comment
 */
public class TosTokenCollector extends AbstractTokenCollector {

    private static final String PREF_TOS_JOBS_RECORDS = "TOS_Jobs_Records"; //$NON-NLS-1$

    private static final TokenKey TOS_COUNT_LOCALPROJECTS = new TokenKey("tos.count.localProjects"); //$NON-NLS-1$

    private static final TokenKey TOS_COUNT_JOBS = new TokenKey("tos.count.jobs"); //$NON-NLS-1$

    private static final TokenKey TOS_COUNT_JOBS_PER_PROJECT = new TokenKey("tos.count.jobsPerProject"); //$NON-NLS-1$

    private static final TokenKey TOS_COUNT_COMPONENTS = new TokenKey("tos.count.components"); //$NON-NLS-1$

    private static final TokenKey TOS_COUNT_COMPONENTS_PER_JOB = new TokenKey("tos.count.componentsPerJob"); //$NON-NLS-1$

    private static final TokenKey TOS_COUNT_CONTEXT_VARIABLES = new TokenKey("tos.count.contextVariables"); //$NON-NLS-1$

    private static final TokenKey TOS_COUNT_CONTEXT_VARIABLES_PER_JOB = new TokenKey("tos.count.contextVariablesPerJob"); //$NON-NLS-1$

    private static final TokenKey TOS_JOB_TOP20_COMPONENTS = new TokenKey("tos.job.frequentComponents"); //$NON-NLS-1$

    private static final TokenKey TOS_COUNT_GENERATED_JOB_DOCS = new TokenKey("tos.count.generatedJobDocs"); //$NON-NLS-1$

    private static final TokenKey TOS_COUNT_BUSINESS_MODELS = new TokenKey("tos.count.businessModels"); //$NON-NLS-1$

    private static final TokenKey TOS_COUNT_METADATAS = new TokenKey("tos.count.metadatas"); //$NON-NLS-1$

    public static final int TOP_USED_COMPONENTS_MAX = 20;

    /**
     * ggu JobTokenCollector constructor comment.
     */
    public TosTokenCollector() {
    }

    @Override
    public void priorCollect() throws Exception {
        // for all projects
        JSONObject allProjectRecords = null;

        IPreferenceStore preferenceStore = RepositoryPlugin.getDefault().getPreferenceStore();
        String records = preferenceStore.getString(PREF_TOS_JOBS_RECORDS);
        try {
            // reset
            allProjectRecords = new JSONObject(records);
        } catch (Exception e) {
            // the value is not set, or is empty
            allProjectRecords = new JSONObject();
        }

        JSONObject currentProjectObject = collectJobDetails();

        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        allProjectRecords.put(currentProject.getTechnicalLabel(), currentProjectObject);

        //
        preferenceStore.setValue(PREF_TOS_JOBS_RECORDS, allProjectRecords.toString());
    }

    private JSONObject collectJobDetails() throws PersistenceException, JSONException {
        JSONObject jObject = new JSONObject();

        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        final IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();

        int jobsNum = 0;
        int componentsNum = 0;
        int contextVarsNum = 0;
        int businessModelsNum = 0;
        int generatedJobDocsNum = 0;
        int metadatasNum = 0;

        Map<String, Integer> numComponentMap = new HashMap<String, Integer>();

        // jobs
        List<IRepositoryViewObject> all = factory.getAll(currentProject, ERepositoryObjectType.PROCESS);
        jobsNum += all.size();

        for (IRepositoryViewObject rvo : all) {
            Item item = rvo.getProperty().getItem();
            if (item instanceof ProcessItem) {
                ProcessType processType = ((ProcessItem) item).getProcess();

                componentsNum += processType.getNode().size();
                // components name
                for (NodeType node : (List<NodeType>) processType.getNode()) {
                    String componentName = node.getComponentName();
                    Integer integer = numComponentMap.get(componentName);
                    if (integer == null) {
                        numComponentMap.put(componentName, 1);
                    } else {
                        numComponentMap.put(componentName, integer + 1);
                    }
                }
                // context variable per job
                EList contexts = processType.getContext();
                if (contexts.size() > 0) {
                    ContextType contextType = (ContextType) contexts.get(0);
                    contextVarsNum += contextType.getContextParameter().size();
                }

            }

        }
        // business model
        businessModelsNum += factory.getAll(currentProject, ERepositoryObjectType.BUSINESS_PROCESS).size();
        // generated job docs
        generatedJobDocsNum += factory.getAll(currentProject, ERepositoryObjectType.JOB_DOC).size();
        // metadata
        for (DynaEnum type : ERepositoryObjectType.values()) {
            if (type instanceof ERepositoryObjectType) {
                ERepositoryObjectType repType = (ERepositoryObjectType) type;
                String folder = repType.getFolder();
                if (repType.isDIItemType() && repType.isResouce() && folder != null
                        && folder.startsWith(ERepositoryObjectType.METADATA.getFolder())) {
                    metadatasNum += factory.getAll(currentProject, repType).size();
                }
            }
        }

        jObject.put(TOS_COUNT_JOBS.getKey(), jobsNum);
        jObject.put(TOS_COUNT_BUSINESS_MODELS.getKey(), businessModelsNum);
        jObject.put(TOS_COUNT_GENERATED_JOB_DOCS.getKey(), generatedJobDocsNum);
        jObject.put(TOS_COUNT_METADATAS.getKey(), metadatasNum);
        jObject.put(TOS_COUNT_CONTEXT_VARIABLES.getKey(), contextVarsNum);
        jObject.put(TOS_COUNT_COMPONENTS.getKey(), componentsNum);

        // top 20 component
        jObject.put(TOS_JOB_TOP20_COMPONENTS.getKey(),
                TokenInforUtil.convertTopComponents(numComponentMap, TOP_USED_COMPONENTS_MAX));

        return jObject;
    }

    @Override
    protected void collectProperties(JSONObject propertiesObject) throws Exception {
        // number of projects
        int projectsNum = 0, jobsNum = 0, businessModelsNum = 0, generatedJobDocsNum = 0;
        int metadatasNum = 0, contextVarsNum = 0, componentsNum = 0;
        Map<String, Integer> numComponentMap = new HashMap<String, Integer>();

        IPreferenceStore preferenceStore = RepositoryPlugin.getDefault().getPreferenceStore();
        String records = preferenceStore.getString(PREF_TOS_JOBS_RECORDS);
        try {
            JSONObject allProjectRecords = new JSONObject(records);
            Iterator<String> keys = allProjectRecords.keys();
            while (keys.hasNext()) {
                String projectName = keys.next();
                projectsNum++;

                JSONObject object = (JSONObject) allProjectRecords.get(projectName);
                if (object != null) {
                    jobsNum += object.getInt(TOS_COUNT_JOBS.getKey());
                    businessModelsNum += object.getInt(TOS_COUNT_BUSINESS_MODELS.getKey());
                    generatedJobDocsNum += object.getInt(TOS_COUNT_GENERATED_JOB_DOCS.getKey());
                    metadatasNum += object.getInt(TOS_COUNT_METADATAS.getKey());
                    contextVarsNum += object.getInt(TOS_COUNT_CONTEXT_VARIABLES.getKey());
                    componentsNum += object.getInt(TOS_COUNT_COMPONENTS.getKey());

                    JSONObject top20Components = (JSONObject) object.get(TOS_JOB_TOP20_COMPONENTS.getKey());
                    if (top20Components != null && top20Components.length() > 0) {
                        Iterator<String> componentKeys = top20Components.keys();
                        while (componentKeys.hasNext()) {
                            String componentName = componentKeys.next();
                            int num = top20Components.getInt(componentName);
                            if (numComponentMap.containsKey(componentName)) {
                                Integer old = numComponentMap.get(componentName);
                                if (old != null) {
                                    num += old.intValue();
                                }
                            }
                            numComponentMap.put(componentName, num);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // the value is not set, or is empty
        }

        propertiesObject.put(TOS_COUNT_LOCALPROJECTS.getKey(), projectsNum);
        propertiesObject.put(TOS_COUNT_JOBS.getKey(), jobsNum);
        propertiesObject.put(TOS_COUNT_BUSINESS_MODELS.getKey(), businessModelsNum);
        propertiesObject.put(TOS_COUNT_GENERATED_JOB_DOCS.getKey(), generatedJobDocsNum);
        propertiesObject.put(TOS_COUNT_METADATAS.getKey(), metadatasNum);
        propertiesObject.put(TOS_COUNT_JOBS_PER_PROJECT.getKey(), TokenInforUtil.calcAverageToStr(jobsNum, projectsNum));
        propertiesObject.put(TOS_COUNT_COMPONENTS_PER_JOB.getKey(), TokenInforUtil.calcAverageToStr(componentsNum, jobsNum));
        propertiesObject.put(TOS_COUNT_CONTEXT_VARIABLES_PER_JOB.getKey(),
                TokenInforUtil.calcAverageToStr(contextVarsNum, jobsNum));

        // top20
        propertiesObject.put(TOS_JOB_TOP20_COMPONENTS.getKey(),
                TokenInforUtil.convertTopComponentsArray(numComponentMap, TOP_USED_COMPONENTS_MAX));
    }

}
