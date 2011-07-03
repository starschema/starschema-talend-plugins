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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.ui.palette.PaletteEditPartFactory;

/**
 * 
 */
public class TalendPaletteEditPartFactory extends PaletteEditPartFactory {

    protected EditPart createDrawerEditPart(EditPart parentEditPart, Object model) {
        return new TalendDrawerEditPart((PaletteDrawer) model);
    }

}
