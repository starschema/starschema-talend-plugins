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

import org.eclipse.gef.ui.palette.PaletteMessages;
import org.eclipse.jface.action.Action;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.ui.images.ECoreImage;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class ShowFavoriteAction extends Action {

    private static ShowFavoriteAction showFavorite = null;

    public static boolean state = true;

    private ShowFavoriteAction() {
        super(PaletteMessages.FAVORITE_LABEL);
        setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.FAVORITE_DISICON));
    }

    public static ShowFavoriteAction getInstance() {
        if (showFavorite == null) {
            showFavorite = new ShowFavoriteAction();
        }
        return showFavorite;
    }

    private ShowStandardAction showS = null;

    public ShowStandardAction getShowS() {
        return this.showS;
    }

    public void setShowS(ShowStandardAction showS) {
        this.showS = showS;
    }

    public void run() {
        ComponentUtilities.updatePalette(true);
        state = false;
        setEnabled(false);
        getShowS().setEnabled(true);
        if (!this.isEnabled()) {
            setDisabledImageDescriptor(ImageProvider.getImageDesc(ECoreImage.FAVORITE_ICON));
        }
    }

}
