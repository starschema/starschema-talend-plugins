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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.beans.PropertyChangeEvent;
import java.io.File;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.properties.tab.IDynamicProperty;
import org.talend.designer.core.DesignerPlugin;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryConstants;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 * 
 * $Id: ImageController.java 1 2006-12-12 下午01:49:18 +0000 (下午01:49:18) yzhang $
 * 
 */
public class ImageController extends AbstractElementPropertySectionController {

    /**
     * DOC yzhang ImageController constructor comment.
     * 
     * @param parameterBean
     */
    public ImageController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createControl()
     */
    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {

        // String fileName = (String) param.getValue();
        // IRepositoryService service = DesignerPlugin.getDefault().getRepositoryService();
        // String filePath = service.getPathFileName(RepositoryConstants.IMG_DIRECTORY, fileName).toPortableString();

        if (param.getElement() instanceof INode && ((INode) param.getElement()).getExternalNode() != null
                && ((INode) param.getElement()).getExternalNode().getScreenshot() != null) {
            ImageDescriptor imageDesc = ((INode) param.getElement()).getExternalNode().getScreenshot();

            // File fileOrFolder = new java.io.File(filePath);
            // if (!fileOrFolder.isFile() || !fileOrFolder.canRead()) {
            // return lastControl;
            // }
            final Composite compositeImage = new Composite(subComposite, SWT.BORDER);

            final Image image = imageDesc.createImage();
            addResourceDisposeListener(compositeImage, image);

            CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName() + " :"); //$NON-NLS-1$
            FormData formDataLabel = new FormData();
            labelLabel.setVisible(true);
            if (numInRow != 1) {
                labelLabel.setAlignment(SWT.RIGHT);
            }
            if (lastControl != null) {
                formDataLabel.left = new FormAttachment(lastControl, 0);
            } else {
                formDataLabel.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
                formDataLabel.top = new FormAttachment(0, top);
            }
            labelLabel.setLayoutData(formDataLabel);

            compositeImage.setToolTipText(param.getDisplayName());
            Point size = new Point(image.getImageData().width, image.getImageData().height);
            FormData formData = new FormData(size.x, size.y);
            formData.top = new FormAttachment(0, top);
            formData.left = new FormAttachment(labelLabel);
            compositeImage.setBackgroundImage(image);
            compositeImage.setLayoutData(formData);
            return compositeImage;
        } else {
            return lastControl;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        IRepositoryService service = DesignerPlugin.getDefault().getRepositoryService();
        String filePath = service.getPathFileName(RepositoryConstants.IMG_DIRECTORY, (String) param.getValue())
                .toPortableString();

        if (filePath != null) {
            File fileOrFolder = new java.io.File(filePath);
            if (!fileOrFolder.isFile() || !fileOrFolder.canRead()) {
                return 0;
            }
            final Image image = new Image(subComposite.getDisplay(), filePath);
            int height = image.getImageData().height;
            image.dispose();
            return height;
        }

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub

    }

    @Override
    public void refresh(IElementParameter param, boolean check) {
    }

}
