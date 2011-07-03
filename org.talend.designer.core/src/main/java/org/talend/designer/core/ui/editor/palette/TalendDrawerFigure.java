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

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.internal.ui.palette.PaletteColorUtil;
import org.eclipse.gef.internal.ui.palette.editparts.DrawerFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * 
 */
public class TalendDrawerFigure extends DrawerFigure {

    private static final int COLOR_INCREMENT = 15;

    private static final int X_OFFSET = 17;

    private static Color baseColor = FigureUtilities.mixColors(PaletteColorUtil.WIDGET_BACKGROUND,
            PaletteColorUtil.WIDGET_LIST_BACKGROUND, 0.1);

    public TalendDrawerFigure(Control control, int childLevel) {
        super(control);
        Color backgroundColor = new Color(Display.getCurrent(), getNewValue(baseColor.getRed(), childLevel), getNewValue(
                baseColor.getGreen(), childLevel), getNewValue(baseColor.getBlue(), childLevel));
        getContentPane().setBackgroundColor(backgroundColor);
    }

    public void disposeColors() {
        if (getContentPane().getBackgroundColor() != null && !getContentPane().getBackgroundColor().isDisposed()) {
            getContentPane().getBackgroundColor().dispose();
        }
    }

    private int getNewValue(int oldValue, int childLevel) {
        int result = oldValue - childLevel * COLOR_INCREMENT;
        return (result > 0 ? result : 0);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(bounds.x + X_OFFSET, bounds.y, bounds.width, bounds.height);
    }
}
