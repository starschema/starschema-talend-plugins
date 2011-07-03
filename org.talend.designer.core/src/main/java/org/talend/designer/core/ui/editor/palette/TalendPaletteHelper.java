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
package org.talend.designer.core.ui.editor.palette;

import org.eclipse.gef.ui.views.palette.PaletteView;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.talend.designer.core.TalendPartAdapter2;
import org.talend.repository.ui.actions.ShowFavoriteAction;
import org.talend.repository.ui.actions.ShowStandardAction;

/**
 * ggu class global comment. Detailled comment
 */
public final class TalendPaletteHelper {

    private static TalendPartAdapter2 talendPartListener;

    public static void checkAndInitToolBar() {
        if (talendPartListener == null) {
            talendPartListener = new TalendPartAdapter2() {

                public void partActivated(IWorkbenchPartReference partRef) {
                    checkAndInitToolBar();
                }

                @Override
                public void partOpened(IWorkbenchPartReference partRef) {
                    checkAndInitToolBar();
                }
            };
            IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (activeWorkbenchWindow != null) {
                IPartService partService = activeWorkbenchWindow.getPartService();
                partService.addPartListener(talendPartListener);
            }
        }
        // check every time
        checkAndInitToolBar2();
    }

    private static void checkAndInitToolBar2() {
        //
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow == null || activeWorkbenchWindow.getActivePage() == null) {
            return;
        }
        IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();

        IViewPart paletteViewPart = activePage.findView(PaletteView.ID);
        if (paletteViewPart != null) {
            // find existing or not, and add it.
            IActionBars actionBars = paletteViewPart.getViewSite().getActionBars();
            IToolBarManager toolBarManager = actionBars.getToolBarManager();

            ShowStandardAction showStandardAction = ShowStandardAction.getInstance();
            // id have set in class
            IContributionItem cItem = toolBarManager.find(showStandardAction.getClass().getCanonicalName());
            if (cItem == null) {
                toolBarManager.add(showStandardAction);
            }

            ShowFavoriteAction showFavoriteAction = ShowFavoriteAction.getInstance();
            // id have set in class
            cItem = toolBarManager.find(showFavoriteAction.getClass().getCanonicalName());
            if (cItem == null) {
                toolBarManager.add(showFavoriteAction);
            }
            if (ShowFavoriteAction.state) {
                showStandardAction.doSetEnable();
            }
            actionBars.updateActionBars();
        }
    }
}
