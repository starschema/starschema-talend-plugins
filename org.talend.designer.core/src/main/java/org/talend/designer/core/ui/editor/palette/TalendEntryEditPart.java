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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.gef.internal.ui.palette.editparts.ToolEntryEditPart;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * DOC talend class global comment. Detailled comment
 */
public class TalendEntryEditPart extends ToolEntryEditPart {

    private static ImageCache globalImageCache;

    private ImageDescriptor imgDescriptor;

    /**
     * DOC talend TalendEntryEditPart constructor comment.
     * 
     * @param paletteEntry
     */
    public TalendEntryEditPart(PaletteEntry paletteEntry) {
        super(paletteEntry);
    }

    public static void resetImageCache() {
        Display display = Display.getDefault();
        if (display == null) {
            display = Display.getCurrent();
        }
        if (display != null) {
            display.syncExec(new Runnable() {

                public void run() {
                    if (globalImageCache != null) {
                        globalImageCache.dispose();
                        globalImageCache = null;
                    }
                }
            });

        }
    }

    @Override
    protected void setImageDescriptor(ImageDescriptor desc) {
        if (desc == imgDescriptor)
            return;
        imgDescriptor = desc;
        setImageInFigure(getImageCache2().getImage(imgDescriptor));
    }

    protected static ImageCache getImageCache2() {
        ImageCache cache = globalImageCache;
        if (cache == null) {
            globalImageCache = cache = new ImageCache();
            Display display = Display.getDefault();
            if (display != null) {
                display.disposeExec(new Runnable() {

                    public void run() {
                        if (globalImageCache != null) {
                            globalImageCache.dispose();
                            globalImageCache = null;
                        }
                    }
                });
            }
        }
        return cache;
    }

    protected static class ImageCache {

        /** Map from ImageDescriptor to Image */
        private Map images = new HashMap(11);

        Image getImage(ImageDescriptor desc) {
            if (desc == null) {
                return null;
            }
            Image img = null;
            Object obj = images.get(desc);
            if (obj != null) {
                img = (Image) obj;
            } else {
                img = desc.createImage();
                images.put(desc, img);
            }
            return img;
        }

        Image getMissingImage() {
            return getImage(ImageDescriptor.getMissingImageDescriptor());
        }

        void dispose() {
            for (Iterator i = images.values().iterator(); i.hasNext();) {
                Image img = (Image) i.next();
                img.dispose();
            }
            images.clear();
        }
    }

}
