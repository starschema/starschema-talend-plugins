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
package org.talend.designer.core.ui.views.contexts;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.ui.branding.IBrandingConfiguration;

/**
 * qzhang class global comment. Detailled comment <br/>
 * 
 */
public class Contexts {

    private static String currentTitle = ""; //$NON-NLS-1$

    private static String newTitle = ""; //$NON-NLS-1$

    private static final String PERSPECTIVE_DI_ID = "org.talend.rcp.perspective"; //$NON-NLS-1$ 

    public static void switchToCurContextsView() {
        ContextsView cxtView = getView();
        if (cxtView == null) {
            return;
        }
        if (!newTitle.equals(currentTitle)) {
            cxtView.setPartName(newTitle);
            currentTitle = newTitle;
        }
        refreshView(cxtView);
    }

    /**
     * qzhang Comment method "refreshView".
     */
    private static ContextsView getView() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        // seem 16594
        if (page != null) {
            String perId = page.getPerspective().getId();
            if ((!"".equals(perId) || null != perId)
                    && (perId.equalsIgnoreCase(PERSPECTIVE_DI_ID) || perId
                            .equalsIgnoreCase(IBrandingConfiguration.PERSPECTIVE_CAMEL_ID))) {
                IViewPart view = page.findView("org.talend.designer.core.ui.views.ContextsView"); //$NON-NLS-1$
                if (view == null) {
                    try {
                        view = page.showView("org.talend.designer.core.ui.views.ContextsView"); //$NON-NLS-1$
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }
                if (view instanceof ContextsView) {
                    return (ContextsView) view;
                }
            }
        }
        return null;

    }

    /**
     * qzhang Comment method "refreshView".
     * 
     * @param view
     */
    private static void refreshView(ContextsView view) {
        if (view != null) {
            if (!newTitle.equals(currentTitle)) {
                view.setPartName(newTitle);
                currentTitle = newTitle;
            }
            view.refresh();
        }
    }

    public static void setTitle(String title) {
        newTitle = title;
    }

    /**
     * qzhang Comment method "clearAll".
     */
    public static void clearAll() {
        ContextsView cxtView = getView();
        refreshView(cxtView);
        if (cxtView != null) {
            cxtView.reset();
        }
    }

}
