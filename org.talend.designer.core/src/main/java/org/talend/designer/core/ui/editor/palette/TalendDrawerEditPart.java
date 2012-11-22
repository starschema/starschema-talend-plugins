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
package org.talend.designer.core.ui.editor.palette;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.FocusEvent;
import org.eclipse.draw2d.FocusListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.internal.ui.palette.editparts.DrawerEditPart;
import org.eclipse.gef.palette.PaletteDrawer;

/**
 * 
 */
/**
 * DOC Administrator class global comment. Detailled comment
 */
public class TalendDrawerEditPart extends DrawerEditPart {

    @Override
    protected void unregister() {
        super.unregister();
    }

    @Override
    protected void unregisterVisuals() {
        if (getFigure() instanceof TalendDrawerFigure) {
            ((TalendDrawerFigure) getFigure()).disposeColors();
        }
        super.unregisterVisuals();

    }

    @Override
    public void deactivate() {
        if (getFigure() instanceof TalendDrawerFigure) {
            ((TalendDrawerFigure) getFigure()).disposeColors();
        }
        super.deactivate();

    }

    private int childLevel = 0;

    public TalendDrawerEditPart(PaletteDrawer drawer) {
        super(drawer);
    }

    @SuppressWarnings("restriction")
    public IFigure createFigure() {
        if (getParent() instanceof TalendDrawerEditPart) {

            TalendDrawerEditPart parent = (TalendDrawerEditPart) getParent();
            childLevel = parent.childLevel + 1;
            getViewer().getControl().setData("ANIMATE", Boolean.FALSE); //$NON-NLS-1$

            TalendDrawerFigure fig = new TalendDrawerFigure(getViewer().getControl(), childLevel) {

                IFigure buildTooltip() {
                    return createToolTip();
                }
            };
            getViewer().getControl().setData("ANIMATE", Boolean.TRUE); //$NON-NLS-1$
            fig.setExpanded(getDrawer().isInitiallyOpen());
            fig.setPinned(getDrawer().isInitiallyPinned());

            fig.getCollapseToggle().addFocusListener(new FocusListener.Stub() {

                public void focusGained(FocusEvent fe) {
                    getViewer().select(TalendDrawerEditPart.this);
                }
            });

            return fig;
        }
        return super.createFigure();
    }

    @SuppressWarnings("restriction")
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        String property = evt.getPropertyName();
        if (property.equals(PaletteDrawer.PROPERTY_INITIAL_STATUS)) {
            boolean isExpaned = getDrawerFigure().isExpanded();
            if (isExpaned == getDrawer().isInitiallyOpen()) {
                return;
            }
            getDrawerFigure().setExpanded(getDrawer().isInitiallyOpen());
            refreshVisuals();
        }

    }
}
