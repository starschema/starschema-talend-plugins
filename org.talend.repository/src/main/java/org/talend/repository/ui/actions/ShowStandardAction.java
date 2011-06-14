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
public class ShowStandardAction extends Action {

    public static ShowStandardAction showStandard = null;

    private ShowStandardAction() {
        super(PaletteMessages.STANDARD_LABEL);
        setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.STANDARD_DISICON));
    }

    public static ShowStandardAction getInstance() {
        if (showStandard == null) {
            showStandard = new ShowStandardAction();
        }
        return showStandard;
    }

    private ShowFavoriteAction showF = null;

    public ShowFavoriteAction getShowF() {
        return this.showF;
    }

    public void setShowF(ShowFavoriteAction showF) {
        this.showF = showF;
    }

    public void run() {
        ComponentUtilities.updatePalette(false);
        ShowFavoriteAction.state = true;
        setEnabled(false);
        getShowF().setEnabled(true);
    }

    public void doRun() {
        ComponentUtilities.updatePalette(false);
        ShowFavoriteAction.state = true;
        doSetEnable();
    }

    public void doSetEnable() {
        setEnabled(false);
        if (getShowF() != null)
            getShowF().setEnabled(true);
        if (!this.isEnabled()) {
            setDisabledImageDescriptor(ImageProvider.getImageDesc(ECoreImage.STANDARD_ICON));
        }
    }

}
