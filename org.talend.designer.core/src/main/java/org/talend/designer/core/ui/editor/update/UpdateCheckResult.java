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
package org.talend.designer.core.ui.editor.update;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.List;

import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.update.EUpdateResult;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.model.update.UpdatesConstants;
import org.talend.core.ui.IEBCDICProviderService;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.views.jobsettings.JobSettingsView;

/**
 * ggu class global comment. Detailled comment
 */
public class UpdateCheckResult extends UpdateResult {

    /**
     * ggu UpdateCheckResult constructor comment.
     * 
     * @param item
     */
    public UpdateCheckResult(Object item) {
        super(item);
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public String getName() {
        String displayName = getUpdateType().getDisplayName();
        switch (getUpdateType()) {
        case NODE_SCHEMA:
            if (getResultType() == EUpdateResult.RENAME) {
                List<Object> param = (List<Object>) getParameter();
                String[] oldSourceIdAndChildName = UpdateManagerUtils.getSourceIdAndChildName((String) param.get(1));
                String[] newSourceIdAndChildName = UpdateManagerUtils.getSourceIdAndChildName((String) param.get(2));

                String display = getRenamedDisplay(oldSourceIdAndChildName[1], newSourceIdAndChildName[1]);
                if (display != null) {
                    displayName = display;
                }
            } else if (getResultType() == EUpdateResult.DELETE) { // table delete by deselect
                List<Object> param = (List<Object>) getParameter();
                String tableDeleted = (String) param.get(0);
                EUpdateResult status = (EUpdateResult) param.get(1);
                String display = getDeleteOrReloadDisplay(tableDeleted, status);
                if (display != null) {
                    displayName = display;
                }
            } else if (getResultType() == EUpdateResult.RELOAD) { // table reload by deselect and reselect
                List<Object> param = (List<Object>) getParameter();
                String tableReload = (String) param.get(0);
                EUpdateResult status = (EUpdateResult) param.get(1);
                String display = getDeleteOrReloadDisplay(tableReload, status);
                if (display != null) {
                    displayName = display;
                }
            } else {
                if (getUpdateObject() instanceof INode && getParameter() instanceof List && PluginChecker.isEBCDICPluginLoaded()) {
                    IEBCDICProviderService service = (IEBCDICProviderService) GlobalServiceRegister.getDefault().getService(
                            IEBCDICProviderService.class);
                    if (service != null && service.isEbcdicNode((INode) getUpdateObject())) {
                        List<Object> paramObjs = (List<Object>) getParameter();
                        if (paramObjs.size() >= 2) {
                            Object schemaName = paramObjs.get(1);
                            if (schemaName instanceof String) {
                                displayName = displayName + UpdateManagerUtils.addBrackets((String) schemaName);
                            }
                        }
                    }
                }
            }
            break;
        case NODE_PROPERTY:
        case NODE_QUERY:
        case JOBLET_SCHEMA:
        case NODE_VALIDATION_RULE:
            break;
        case JOB_PROPERTY_EXTRA:
            displayName = displayName + UpdateManagerUtils.addBrackets(EComponentCategory.EXTRA.getTitle());
            break;
        case JOB_PROPERTY_STATS_LOGS:
            displayName = displayName + UpdateManagerUtils.addBrackets(EComponentCategory.STATSANDLOGS.getTitle());
            break;
        case JOB_PROPERTY_HEADERFOOTER:
            displayName = displayName + UpdateManagerUtils.addBrackets(EComponentCategory.HEADERFOOTER.getTitle());
            break;
        case CONTEXT_GROUP:
            if (getUpdateObject() != null && getUpdateObject() instanceof IContext) {
                displayName = ((IContext) getUpdateObject()).getName();
            }
            break;
        case CONTEXT:
        case JOBLET_CONTEXT: {
            String display = null;
            switch (getResultType()) {
            case RENAME:
                List<Object> param = (List<Object>) getParameter();
                display = getRenamedDisplay((String) param.get(1), (String) param.get(2));
                break;
            default:
                if (getUpdateObject() instanceof Collection) {
                    display = getCollectionsDisplay(getUpdateObject(), false);
                }
                break;
            }

            if (display != null) {
                displayName = display;
            }
            break;
        }
        case JOBLET_RENAMED: {
            List<Object> param = (List<Object>) getParameter();
            String display = getRenamedDisplay((String) param.get(1), (String) param.get(2));
            if (display != null) {
                displayName = display;
            }
            break;
        }
        case RELOAD:
            if (getParameter() != null && getParameter() instanceof PropertyChangeEvent) {
                PropertyChangeEvent event = (PropertyChangeEvent) getParameter();
                // reload all compoennts.
                if (event.getSource() != null && !(event.getSource() instanceof IProcess)) {
                    displayName = getUpdateType().getDisplayName();
                    break;
                }
            }
            break;
        default:
        }

        return displayName;
    }

    private String getRenamedDisplay(final String oldName, final String newName) {
        if (oldName == null || newName == null) {
            return null;
        }
        return oldName + UpdatesConstants.RENAME_SIGN + newName;
    }

    private String getDeleteOrReloadDisplay(final String tableName, EUpdateResult status) {
        if (tableName == null) {
            return null;
        }
        return tableName + UpdatesConstants.RENAME_SIGN + status.toString();
    }

    @SuppressWarnings("unchecked")
    private String getCollectionsDisplay(Object object, boolean all) {
        if (object == null) {
            return null;
        }
        String displayName = UpdatesConstants.EMPTY;
        if (object instanceof Collection) {
            for (Object obj : (Collection) object) {
                String tmp = null;
                if (obj instanceof String) {
                    tmp = (String) obj;
                } else if (obj instanceof INode) {
                    INode node = (INode) obj;
                    if (all && !node.getLabel().equals(node.getUniqueName())) {
                        tmp = node.getLabel() + UpdateManagerUtils.addBrackets(node.getUniqueName());
                    } else {
                        tmp = node.getLabel();
                    }
                }
                if (tmp != null) {
                    displayName = displayName + UpdatesConstants.SEGMENT + tmp;
                }
            }
            if (displayName.startsWith(UpdatesConstants.SEGMENT)) {
                displayName = displayName.substring(1);
            }
        }
        return displayName;
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public String getCategory() {
        String category = null;
        switch (getUpdateType()) {
        case NODE_PROPERTY:
        case NODE_SCHEMA:
        case NODE_QUERY:
        case NODE_VALIDATION_RULE:
        case JOBLET_SCHEMA:
            if (getUpdateObject() != null) {
                if (getUpdateObject() instanceof Node) {
                    Node node = (Node) getUpdateObject();
                    if (node.getUniqueName().equals(node.getLabel())) {
                        category = node.getUniqueName();
                    } else {
                        category = node.getLabel() + UpdateManagerUtils.addBrackets(node.getUniqueName());
                    }
                }
                if (getUpdateObject() instanceof NodeType) {
                    NodeType node = (NodeType) getUpdateObject();
                    String uniqueName = null;
                    for (ElementParameterType param : (List<ElementParameterType>) node.getElementParameter()) {
                        if (EParameterName.UNIQUE_NAME.getName().equals(param.getName())) {
                            uniqueName = param.getValue();
                            break;
                        }

                    }
                    if (uniqueName != null) {
                        category = uniqueName;
                    }
                }
            }
            break;
        case JOB_PROPERTY_EXTRA:
        case JOB_PROPERTY_STATS_LOGS:
        case JOB_PROPERTY_HEADERFOOTER:
            boolean isJoblet = false;
            if (getUpdateObject() != null) {
                if (getUpdateObject() instanceof org.talend.designer.core.ui.editor.process.Process) {
                    if (AbstractProcessProvider.isExtensionProcessForJoblet((IProcess) getUpdateObject())) {
                        isJoblet = true;
                    }
                }
            }
            if (isJoblet) {
                category = JobSettingsView.VIEW_NAME_JOBLET; // joblet
            } else {
                category = JobSettingsView.getViewNameLable();
            }
            break;
        case CONTEXT:
        case JOBLET_CONTEXT:
            category = UpdatesConstants.CONTEXT;
            break;
        case CONTEXT_GROUP:
            category = UpdatesConstants.CONTEXT_GROUP;
            break;
        case JOBLET_RENAMED:
        case RELOAD:
            if (getUpdateObject() != null && getUpdateObject() instanceof List) {
                String display = getCollectionsDisplay(getUpdateObject(), true);
                if (display != null) {
                    category = display;
                }
            } else if (getParameter() != null && getParameter() instanceof PropertyChangeEvent) {
                PropertyChangeEvent event = (PropertyChangeEvent) getParameter();
                // reload all compoennts.
                if (event.getSource() != null && !(event.getSource() instanceof IProcess)) {
                    category = UpdatesConstants.COMPONENT;
                    break;
                }
            } else {
                category = UpdatesConstants.JOBLET;
            }
            break;
        default:
        }

        return category == null ? UpdatesConstants.EMPTY : category;
    }

    @Override
    public String getJobInfor() {
        return this.jobInfor;
    }

    protected void updateJobInfor() {
        if (getJob() != null) {
            String jobInfor = null;
            if (getJob() instanceof IProcess2) {
                Property property = ((IProcess2) getJob()).getProperty();
                jobInfor = RepositoryUpdateManager.getUpdateJobInfor(property);
                org.talend.core.model.properties.Item item = property.getItem();
                if (item instanceof JobletProcessItem) {
                    isJoblet = true;
                }
            }
            if (getJob() instanceof org.talend.core.model.properties.Item) {
                jobInfor = RepositoryUpdateManager.getUpdateJobInfor(((org.talend.core.model.properties.Item) getJob())
                        .getProperty());
                if (getJob() instanceof JobletProcessItem) {
                    isJoblet = true;
                }
            }
            String others = null;
            if (isFromItem()) { // update item
                others = UpdatesConstants.START;
            }
            if (jobInfor != null) {
                this.jobInfor = jobInfor + UpdatesConstants.SPACE + UpdateManagerUtils.addBrackets(others);
                return;
            }
        }

    }

}
