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

import java.util.List;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.talend.core.model.update.EUpdateResult;
import org.talend.core.model.update.UpdateResult;

/**
 * ggu class global comment. Detailled comment
 */
public class UpdateViewerHelper {

    /**
     * 
     */
    enum StateType {
        ALL,
        SOME,
        NONE;
    }

    private UpdateDetectionDialog updateDialog;

    public UpdateViewerHelper(UpdateDetectionDialog updateDialog) {
        if (updateDialog == null) {
            throw new RuntimeException("The argument can't be null"); //$NON-NLS-1$
        }
        this.updateDialog = updateDialog;
    }

    private CheckboxTreeViewer getViewer() {
        return this.updateDialog.getViewer();
    }

    /**
     * 
     * init view state.
     */
    public void initViewerState() {
        if (getViewer() == null) {
            return;
        }

        Tree tree = getViewer().getTree();
        for (TreeItem item : tree.getItems()) {
            Object data = item.getData();
            if (data != null) {
                if (data instanceof Job) {
                    updateJobState((Job) data);
                }
            }
        }
        if (tree.getItems().length == 1) {
            getViewer().expandAll();
        }
        refreshSelectButton();
        checkUpdateStatus();
    }

    /**
     * 
     * select all.
     * 
     */
    public void selectAll(boolean state) {
        if (getViewer() == null) {
            return;
        }
        getViewer().getTree().setRedraw(false);
        TreeItem[] items = getViewer().getTree().getItems();
        for (TreeItem item : items) {
            Object data = item.getData();
            if (data != null) {
                if (data instanceof Job) {
                    updateCheckedState(data, state);
                }
            }

        }
        getViewer().getTree().setRedraw(true);
        refreshSelectButton();
        checkUpdateStatus();
    }

    public void updateCheckedState(Object obj, boolean state) {
        if (obj == null || getViewer() == null) {
            return;
        }
        getViewer().getTree().setRedraw(false);
        if (obj instanceof Item) {
            checkItemResultState((Item) obj, state);
        }
        // must check child first.
        checkChildSate(obj, state);
        checkParentState(obj);

        getViewer().getTree().setRedraw(true);
        refreshSelectButton();
        checkUpdateStatus();
    }

    private void checkChildSate(Object obj, boolean state) {
        if (getViewer().getGrayed(obj)) {
            // inverse the checked child
            checkGrayedState(obj);
        } else {
            checkNormalState(obj, state);
        }
    }

    private void checkNormalState(Object obj, boolean state) {
        if (obj instanceof Job) {
            for (Category category : ((Job) obj).getCategories()) {
                checkNormalState(category, state);
            }
        } else if (obj instanceof Category) {
            checkItemsState((Category) obj, state);
        } else if (obj instanceof Item) {
            // noting to do
        }
    }

    private void checkGrayedState(Object obj) {
        // inverse the checked child
        if (obj instanceof Job) {
            Job job = (Job) obj;
            boolean checked = false;
            switch (updateCategoriesState(job)) {
            case ALL:
                checked = false; // inversion
                break;
            case SOME:
            case NONE:
                checked = true;
                break;
            default:
            }
            for (Category category : job.getCategories()) {
                checkItemsState(category, checked);
            }
        } else if (obj instanceof Category) {
            Category category = (Category) obj;
            switch (updateItemsState(category)) {
            case ALL:
                // all will be unchecked
                checkItemsState(category, false);
                break;
            case SOME:
                // all will be checked
                checkItemsState(category, true);
                break;
            case NONE:
                break;
            default:
            }
        }
    }

    private void checkItemsState(Category category, boolean state) {
        if (category == null) {
            return;
        }
        for (Item item : category.getItems()) {
            checkItemResultState(item, state);
        }
    }

    private void checkItemResultState(Item item, boolean state) {
        if (item == null) {
            return;
        }
        // not checked
        if (!state && !item.getResultObject().isReadOnly()) {
            EUpdateResult resultType = item.getResultObject().getResultType();
            if (resultType == EUpdateResult.UPDATE || resultType == EUpdateResult.ADD || resultType == EUpdateResult.DELETE) {
                item.setChecked(false);
            }
        } else {
            // keep the checked
            item.setChecked(true);
        }
    }

    private void checkParentState(Object obj) {
        if (obj instanceof Job) {
            updateJobState((Job) obj);
        } else if (obj instanceof Category) {
            updateJobState(((Category) obj).getParent());
        } else if (obj instanceof Item) {
            updateJobState(((Item) obj).getParent().getParent());
        }
    }

    private void updateJobState(Job job) {
        getViewer().refresh(job, true);

        switch (updateCategoriesState(job)) {
        case ALL:
            getViewer().setChecked(job, true);
            getViewer().setGrayed(job, false);
            break;
        case SOME:
            getViewer().setGrayChecked(job, true);
            break;
        case NONE:
            getViewer().setGrayChecked(job, false);
            break;
        default:
        }
    }

    private StateType updateCategoriesState(Job job) {
        if (job == null) {
            return StateType.NONE;
        }
        int all = 0;
        int none = 0;
        for (Category category : job.getCategories()) {
            switch (updateItemsState(category)) {
            case ALL:
                getViewer().setChecked(category, true);
                getViewer().setGrayed(category, false);
                all++;
                break;
            case SOME:
                getViewer().setGrayChecked(category, true);
                break;
            case NONE:
                getViewer().setGrayChecked(category, false);
                none++;
                break;
            default:
            }
        }

        int num = job.getCategories().size();
        if (all == num) {
            return StateType.ALL;
        }
        if (none == num) {
            return StateType.NONE;
        }

        return StateType.SOME;
    }

    private StateType updateItemsState(Category category) {
        if (category == null) {
            return StateType.NONE;
        }
        int num = 0;
        for (Item item : category.getItems()) {
            if (item.isChecked()) {
                num++;
            }
            getViewer().refresh(item, true);
            getViewer().setChecked(item, item.isChecked());
        }
        if (num == 0) {
            return StateType.NONE;
        } else if (num == category.getItems().size()) {
            return StateType.ALL;
        }
        return StateType.SOME;
    }

    /**
     * 
     * ggu Comment method "refreshSelectButton".
     * 
     * refresh the label of select button.
     */
    @SuppressWarnings("restriction")
    public void refreshSelectButton() {
        List<UpdateResult> inputElements = updateDialog.getInputElements();
        if (inputElements == null) {
            return;
        }
        int num = 0;
        for (UpdateResult result : inputElements) {
            if (result.isChecked()) {
                num++;
            }
        }
        if (num == inputElements.size()) {
            updateDialog.setSelectButtonLabel(WorkbenchMessages.SelectionDialog_deselectLabel);
        } else {
            updateDialog.setSelectButtonLabel(WorkbenchMessages.SelectionDialog_selectLabel);
        }

    }

    public void checkUpdateStatus() {
        List<UpdateResult> inputElements = updateDialog.getInputElements();
        if (inputElements != null) {
            for (UpdateResult result : inputElements) {

                if (result.isReadOnlyProcess()) {
                    updateDialog.updateReadOnlyJobWarnMessage();
                    return;
                }

                if (!result.isChecked()
                        && (result.getResultType() == EUpdateResult.UPDATE || result.getResultType() == EUpdateResult.DELETE)) {
                    updateDialog.updateWarnMessage();
                    return;
                }
            }
        }
        updateDialog.updateNomarlMessage();
    }
}
