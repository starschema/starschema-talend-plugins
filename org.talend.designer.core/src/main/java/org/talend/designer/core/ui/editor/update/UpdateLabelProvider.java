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
package org.talend.designer.core.ui.editor.update;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.commons.ui.runtime.image.OverlayImage.EPosition;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.update.EUpdateItemType;
import org.talend.core.model.update.EUpdateResult;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.model.update.UpdatesConstants;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.core.ui.images.OverlayImageProvider;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.repository.model.ComponentsFactoryProvider;

/**
 * ggu class global comment. Detailled comment
 */
public class UpdateLabelProvider implements ITableLabelProvider {

    public Image getColumnImage(Object element, int columnIndex) {
        if (columnIndex == 0) {
            ECoreImage warnOverlay = ECoreImage.WARN_OVERLAY;
            EPosition position = EPosition.BOTTOM_RIGHT;
            if (element instanceof Job) {
                IImage image;
                Job job = (Job) element;
                if (job.isJoblet()) {
                    image = ECoreImage.JOBLET_ICON;
                } else {
                    image = ECoreImage.PROCESS_ICON;
                }
                if (checkElementForUpdate(element)) {
                    return OverlayImageProvider.getImageForOverlay(image, warnOverlay, position);
                }
                return ImageProvider.getImage(image);
            } else if (element instanceof Category) {
                Category category = (Category) element;
                EUpdateItemType type = category.getType();
                if (type != null) {
                    Image image = null;
                    switch (type) {
                    case JOB_PROPERTY_EXTRA:
                    case JOB_PROPERTY_STATS_LOGS:
                    case JOB_PROPERTY_HEADERFOOTER:

                        image = ImageProvider.getImage(ECoreImage.PROCESS_ICON);
                        break;
                    case CONTEXT:
                    case CONTEXT_GROUP:
                    case JOBLET_CONTEXT:
                        image = ImageProvider.getImage(ECoreImage.CONTEXT_ICON);
                        break;
                    case NODE_PROPERTY:
                    case NODE_SCHEMA:
                    case NODE_QUERY:
                    case NODE_SAP_IDOC:
                    case NODE_SAP_FUNCTION:
                    case NODE_VALIDATION_RULE:
                    case JOBLET_SCHEMA:
                    case JOBLET_RENAMED:
                    case RELOAD:
                        image = getImageFromNode(category.getNode());
                        break;
                    default:
                        // return ImageProvider.getImage(ECoreImage.TALEND_PICTO);
                    }
                    if (image != null && checkElementForUpdate(element)) {
                        return OverlayImageProvider.getImageForOverlay(image, warnOverlay, position);
                    }
                    return image;
                }
            } else if (element instanceof Item) {
                Item item = (Item) element;
                IImage image = EImage.EMPTY;
                switch (item.getResultObject().getUpdateType()) {
                case NODE_QUERY:
                    image = ECoreImage.METADATA_QUERY_ICON;
                    break;
                case NODE_SCHEMA:
                case JOBLET_SCHEMA:
                    image = ECoreImage.METADATA_TABLE_ICON;
                    break;
                case NODE_PROPERTY:
                case NODE_VALIDATION_RULE:
                case JOB_PROPERTY_EXTRA:
                case JOB_PROPERTY_STATS_LOGS:
                    ERepositoryObjectType type = RepositoryUpdateManager.getTypeFromSource(item.getRemark());
                    if (type != null) {
                        image = CoreImageProvider.getIcon(type);
                    }
                case JOBLET_RENAMED:
                case RELOAD:
                    // return ImageProvider.getImage(ECoreImage.TALEND_PICTO);
                case CONTEXT:
                case CONTEXT_GROUP:
                case JOBLET_CONTEXT:
                    // return ImageProvider.getImage(ECoreImage.CONTEXT_ICON);
                default:
                }
                if (checkElementForUpdate(element)) {
                    return OverlayImageProvider.getImageForOverlay(image, warnOverlay, position);
                }
                return ImageProvider.getImage(image);
            }

        }
        return null;
    }

    public String getColumnText(Object element, int columnIndex) {
        boolean isJob = element instanceof Job;
        boolean isCategory = element instanceof Category;
        switch (columnIndex) {
        case 0:
            if (isJob) {
                String name = ((Job) element).getName();
                return name == null ? UpdatesConstants.EMPTY : name;

            } else if (isCategory) {
                return ((Category) element).getName();
            } else if (element instanceof Item) {
                return ((Item) element).getProperty();
            }
            break;
        case 1:
            if (isJob || isCategory) {
                return UpdatesConstants.SEGMENT_LINE;
            } else if (element instanceof Item) {
                Item item = (Item) element;
                if (isBuilInMode(item) && !item.getParent().getParent().isReadOnlyProcess()) {
                    return EUpdateResult.BUIL_IN.getDisplayName();
                }
                return item.getOperation().getDisplayName();
            }
            break;
        case 2:
            if (isJob || isCategory) {
                if (isJob && ((Job) element).isReadOnlyProcess()) {
                    return Messages.getString("ProcessUpdateManager.ReadOnlyProcessUpdateWarningMessages"); //$NON-NLS-1$
                }
                return UpdatesConstants.SEGMENT_LINE;

            } else if (element instanceof Item) {
                Item item = (Item) element;
                if (isBuilInMode(item)) {
                    return UpdatesConstants.EMPTY;
                }
                return item.getRemark();
            }
            break;
        default:

        }

        return UpdatesConstants.EMPTY;
    }

    public void addListener(ILabelProviderListener listener) {

    }

    public void dispose() {

    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    public void removeListener(ILabelProviderListener listener) {

    }

    private boolean isBuilInMode(Item item) {
        if (item != null && !item.isChecked()
                && (item.getOperation() == EUpdateResult.UPDATE || item.getOperation() == EUpdateResult.DELETE)) {
            return true;
        }
        return false;
    }

    private Image getImageFromNode(Object node) {
        if (node != null) {
            if (node instanceof Node) {
                return CoreImageProvider.getComponentIcon(((Node) node).getComponent(), ICON_SIZE.ICON_16);
            } else if (node instanceof NodeType) {
                NodeType nodeType = (NodeType) node;
                nodeType.getComponentName();
                String componentName = nodeType.getComponentName();
                IComponentsFactory instance = ComponentsFactoryProvider.getInstance();
                if (instance != null && componentName != null) {
                    IComponent component = instance.get(componentName);
                    if (component != null) {
                        return CoreImageProvider.getComponentIcon(component, ICON_SIZE.ICON_16);
                    }
                }
            }
        }
        return ImageProvider.getImage(ECoreImage.TALEND_PICTO);
    }

    /**
     * 
     * ggu Comment method "checkElementForUpdate".
     * 
     * if there is a update item which have changed to built-in mode, return true.
     */
    private boolean checkElementForUpdate(Object element) {

        if (element == null) {
            return false;
        }
        if (element instanceof Item) {
            return isBuilInMode((Item) element);
        } else if (element instanceof Category) {
            Category category = (Category) element;
            for (Item item : category.getItems()) {
                boolean flag = checkElementForUpdate(item);
                if (flag) {
                    return true;
                }
            }
        } else if (element instanceof Job) {
            Job job = (Job) element;
            for (Category category : job.getCategories()) {
                boolean flag = checkElementForUpdate(category);
                if (flag) {
                    return true;
                }
            }
        }
        return false;
    }
}
