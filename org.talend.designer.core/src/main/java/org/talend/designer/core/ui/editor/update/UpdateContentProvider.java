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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.model.update.UpdatesConstants;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * ggu class global comment. Detailled comment
 */
public class UpdateContentProvider implements ITreeContentProvider {

    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof Job) {
            return ((Job) parentElement).getCategories().toArray();
        } else if (parentElement instanceof Category) {
            return ((Category) parentElement).getItems().toArray();
        }
        return null;
    }

    public Object getParent(Object element) {
        if (element instanceof Job) {
            return null;
        } else if (element instanceof Category) {
            return ((Category) element).getParent();
        } else if (element instanceof Item) {
            return ((Item) element).getParent();
        }
        return null;
    }

    public boolean hasChildren(Object element) {
        if (element instanceof Job) {
            return !((Job) element).getCategories().isEmpty();
        } else if (element instanceof Category) {
            return !((Category) element).getItems().isEmpty();
        } else if (element instanceof Item) {
            return false;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public Object[] getElements(Object inputElement) {
        List<Job> jobs = new ArrayList<Job>();
        if (inputElement instanceof Collection) {
            for (UpdateResult result : (List<UpdateResult>) inputElement) {
                String jobName = result.getJobInfor();
                if (jobName == null) {
                    jobName = UpdatesConstants.EMPTY;
                }
                Job job = getJob(jobs, jobName);
                if (job == null) {
                    job = new Job(jobName);
                    Object job2 = result.getJob();
                    if (job2 != null) {
                        if (job2 instanceof IProcess2) {
                            job.setJoblet(((IProcess2) job2).disableRunJobView()); // ?? joblet
                            job.setReadOnlyProcess(result.isReadOnlyProcess());
                            IProcess2 process = (IProcess2) job2;
                            org.talend.core.model.properties.Item processItem = process.getProperty().getItem();
                            if (processItem instanceof ProcessItem) {
                                job.setJoblet(false);
                            } else if (processItem instanceof JobletProcessItem) {
                                job.setJoblet(true);
                            }
                        }

                    } else {
                        job.setJoblet(result.isJoblet());
                    }
                    jobs.add(job);
                }

                Category category = job.getCategory(result.getCategory());
                if (category == null) {
                    category = new Category(job, result.getCategory());
                    category.setType(result.getUpdateType()); // for icon
                    if (result.getUpdateObject() instanceof Node) { // for node icon
                        category.setNode((Node) result.getUpdateObject());
                    }
                    if (result.getUpdateObject() instanceof NodeType) { // for node icon
                        category.setNode((NodeType) result.getUpdateObject());
                    }
                    if (result.getUpdateObject() instanceof List) { // for node icon
                        List list = (List) result.getUpdateObject();
                        if (list.size() > 0) {
                            Object object = list.get(0);
                            if (object instanceof Node) {
                                category.setNode((Node) object);
                            }
                        }
                    }
                    job.addCategory(category);
                }
                Item item = new Item(category, result);
                category.addItem(item);
            }
        }
        return jobs.toArray();
    }

    private Job getJob(List<Job> jobs, String name) {
        if (jobs == null || name == null) {
            return null;
        }
        for (Job job : jobs) {
            if (name.equals(job.getName())) {
                return job;
            }
        }
        return null;
    }

    public void dispose() {

    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

    }
}
