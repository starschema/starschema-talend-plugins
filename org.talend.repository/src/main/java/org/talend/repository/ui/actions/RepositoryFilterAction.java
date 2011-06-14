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
package org.talend.repository.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.talend.commons.ui.image.EImage;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.core.model.repository.IRepositoryPrefConstants;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.repository.ui.dialog.RepositoryFilterDialog;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class RepositoryFilterAction extends Action {

    private IPreferenceStore preferenceStore = RepositoryManager.getPreferenceStore();

    private final IRepositoryView view;

    public RepositoryFilterAction(IRepositoryView repositoryView) {
        // this.setText("&Filters...");
        setImageDescriptor(ImageProvider.getImageDesc(EImage.FILTER_DEACTIVED_ICON));
        setId("filter_action");
        this.view = repositoryView;

    }

    @Override
    public void run() {
        view.getViewer().refresh();

    }

    public void openSetupDialog() {
        RepositoryFilterDialog dialog = new RepositoryFilterDialog(view);
        if (dialog.open() == Window.OK && preferenceStore.getBoolean(IRepositoryPrefConstants.USE_FILTER)) {
            view.getViewer().refresh();
        }
    }

}
