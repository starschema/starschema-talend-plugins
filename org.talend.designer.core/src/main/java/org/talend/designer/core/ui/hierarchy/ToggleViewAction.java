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
package org.talend.designer.core.ui.hierarchy;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.typehierarchy.TypeHierarchyMessages;
import org.eclipse.jface.action.Action;
import org.talend.designer.core.ui.IJobHierarchyViewPart;

/**
 * Action to switch between the different hierarchy views.
 */
public class ToggleViewAction extends Action {

    private IJobHierarchyViewPart fViewPart;

    private int fViewerIndex;

    public ToggleViewAction(IJobHierarchyViewPart v, int viewerIndex) {
        super("", AS_RADIO_BUTTON); //$NON-NLS-1$
        if (viewerIndex == IJobHierarchyViewPart.HIERARCHY_MODE_SUPERTYPES) {
            setText(TypeHierarchyMessages.ToggleViewAction_supertypes_label);
            setDescription(TypeHierarchyMessages.ToggleViewAction_supertypes_description);
            setToolTipText(TypeHierarchyMessages.ToggleViewAction_supertypes_tooltip);
            JavaPluginImages.setLocalImageDescriptors(this, "super_co.gif"); //$NON-NLS-1$
        } else if (viewerIndex == IJobHierarchyViewPart.HIERARCHY_MODE_SUBTYPES) {
            setText(TypeHierarchyMessages.ToggleViewAction_subtypes_label);
            setDescription(TypeHierarchyMessages.ToggleViewAction_subtypes_description);
            setToolTipText(TypeHierarchyMessages.ToggleViewAction_subtypes_tooltip);
            JavaPluginImages.setLocalImageDescriptors(this, "sub_co.gif"); //$NON-NLS-1$
        } else if (viewerIndex == IJobHierarchyViewPart.HIERARCHY_MODE_CLASSIC) {
            setText(TypeHierarchyMessages.ToggleViewAction_vajhierarchy_label);
            setDescription(TypeHierarchyMessages.ToggleViewAction_vajhierarchy_description);
            setToolTipText(TypeHierarchyMessages.ToggleViewAction_vajhierarchy_tooltip);
            JavaPluginImages.setLocalImageDescriptors(this, "hierarchy_co.gif"); //$NON-NLS-1$
        } else {
            Assert.isTrue(false);
        }

        fViewPart = v;
        fViewerIndex = viewerIndex;
    }

    public int getViewerIndex() {
        return fViewerIndex;
    }

    /*
     * @see Action#actionPerformed
     */
    public void run() {
        fViewPart.setHierarchyMode(fViewerIndex);
    }
}
