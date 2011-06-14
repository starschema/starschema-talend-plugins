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
package org.talend.repository.ui.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.commons.utils.image.ImageUtils;
import org.talend.commons.utils.image.ImageUtils.ICON_SIZE;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.Folder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.ui.ICDCProviderService;
import org.talend.core.ui.IReferencedProjectService;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.core.ui.images.ECoreImage;
import org.talend.core.ui.images.OverlayImageProvider;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.model.ECDCStatus;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.MetadataTableRepositoryObject;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;

/**
 * Label provider for the repository view. <code>DEBUG</code> boolean field specify if details (such as objects ids)
 * must appears on display or not.<br/>
 * 
 * $Id: RepositoryLabelProvider.java 46952 2010-08-18 08:41:09Z nrousseau $
 * 
 */
public class RepositoryLabelProvider extends LabelProvider implements IColorProvider, IFontProvider {

    private static final Color STABLE_SECONDARY_ENTRY_COLOR = new Color(null, 100, 100, 100);

    private static final Color STABLE_PRIMARY_ENTRY_COLOR = new Color(null, 0, 0, 0);

    private static final Color INACTIVE_ENTRY_COLOR = new Color(null, 200, 200, 200);

    private static final Color LOCKED_ENTRY = new Color(null, 200, 0, 0);

    private static final Color MERGED_REFERENCED_ITEMS_COLOR = new Color(null, 120, 120, 120);

    private IRepositoryView view;

    public RepositoryLabelProvider(IRepositoryView view) {
        super();
        this.view = view;
    }

    public String getText(IRepositoryViewObject object) {
        StringBuffer string = new StringBuffer();
        string.append(object.getLabel());
        if (!(object instanceof Folder)) {
            string.append(" " + object.getVersion()); //$NON-NLS-1$
        }
        // nodes in the recycle bin
        if (object.isDeleted()) {
            String oldPath = object.getPath();
            string.append(" (" + oldPath + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return string.toString();
    }

    public String getText(Property property) {
        StringBuffer string = new StringBuffer();
        string.append(property.getLabel());
        // PTODO SML [FOLDERS++] temp code
        if (ERepositoryObjectType.getItemType(property.getItem()) != ERepositoryObjectType.FOLDER) {
            string.append(" " + property.getVersion()); //$NON-NLS-1$
        }
        // nodes in the recycle bin
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.getStatus(property.getItem()) == ERepositoryStatus.DELETED) {
            String oldPath = property.getItem().getState().getPath();
            string.append(" (" + oldPath + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return string.toString();
    }

    public String getText(Object obj) {
        if (obj instanceof IRepositoryViewObject) {
            return getText((IRepositoryViewObject) obj);
        }
        if (obj instanceof Property) {
            return getText((Property) obj);
        }
        RepositoryNode node = (RepositoryNode) obj;

        if (node.getType() == ENodeType.REPOSITORY_ELEMENT || node.getType() == ENodeType.SIMPLE_FOLDER) {
            IRepositoryViewObject object = node.getObject();
            if (object == null) {
                return node.getLabel();
            }
            org.talend.core.model.properties.Project mainProject = ProjectManager.getInstance().getCurrentProject()
                    .getEmfProject();
            String projectLabel = object.getProjectLabel();

            switch (object.getType()) {
            case METADATA_CON_QUERY:
            case SNIPPETS:
            case METADATA_CON_SYNONYM:
            case METADATA_CON_TABLE:
            case METADATA_CON_VIEW:
            case METADATA_CON_CDC:
            case METADATA_SAP_IDOC:
            case METADATA_SAP_FUNCTION:
                String label = object.getLabel();
                if (!mainProject.getLabel().equals(projectLabel) && PluginChecker.isRefProjectLoaded()) {

                    IReferencedProjectService service = (IReferencedProjectService) GlobalServiceRegister.getDefault()
                            .getService(IReferencedProjectService.class);
                    if (service != null && service.isMergeRefProject()) {
                        label = label + " (@" + projectLabel + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                    }

                }
                return label;
            default:
                break;
            }
            String label = getText(object);
            if (!mainProject.getLabel().equals(projectLabel) && PluginChecker.isRefProjectLoaded()) {
                IReferencedProjectService service = (IReferencedProjectService) GlobalServiceRegister.getDefault().getService(
                        IReferencedProjectService.class);
                if (service != null && service.isMergeRefProject()) {
                    label = label + " (@" + projectLabel + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }

            }

            return label;
        } else {
            String label = node.getLabel();
            return label;
        }
    }

    public Image getImage(IRepositoryViewObject object) {
        // Item item = property.getItem();
        ERepositoryObjectType itemType = object.getType();
        Image img = null;
        if (object instanceof RepositoryViewObject && ((RepositoryViewObject) object).getCustomImage() != null) {
            img = ((RepositoryViewObject) object).getCustomImage();
        } else {
            img = CoreImageProvider.getImage(itemType);
        }

        ERepositoryStatus repositoryStatus = object.getRepositoryStatus();

        Image image = OverlayImageProvider.getImageWithStatus(img, repositoryStatus);

        ERepositoryStatus informationStatus = object.getInformationStatus();

        return OverlayImageProvider.getImageWithStatus(image, informationStatus);
    }

    public static Image getDefaultJobletImage() {
        return ImageProvider.getImage(ECoreImage.JOBLET_COMPONENT_ICON);
    }

    /**
     * DOC bqian Comment method "getJobletCustomIcon".
     * 
     * @param property
     * @return
     */
    public static Image getJobletCustomIcon(Property property) {
        JobletProcessItem item = (JobletProcessItem) property.getItem();
        Image image = null;
        if (item.getIcon() == null || item.getIcon().getInnerContent() == null || item.getIcon().getInnerContent().length == 0) {
            // File image = RepositoryLabelProvider.getDefaultJobletImage();
            // try {
            // item.getIcon().setInnerContentFromFile(image);
            // } catch (Exception e) {
            // ExceptionHandler.process(e);
            // }

            image = getDefaultJobletImage();
        } else {

            ImageDescriptor imageDesc = ImageUtils.createImageFromData(item.getIcon().getInnerContent());
            imageDesc = ImageUtils.scale(imageDesc, ICON_SIZE.ICON_32);

            image = cachedImages.get(item.getIcon().getInnerContent());
            if (image == null || image.isDisposed()) {
                image = imageDesc.createImage();
                cachedImages.put(item.getIcon().getInnerContent(), image);
            } else {
                // image = imageDesc.createImage();
            }
        }
        return image;
    }

    private static Map<byte[], Image> cachedImages = new HashMap<byte[], Image>();

    public Image getImage(Object obj) {
        if (obj instanceof IRepositoryViewObject) {
            return getImage((IRepositoryViewObject) obj);
        }

        RepositoryNode node = (RepositoryNode) obj;

        switch (node.getType()) {
        case STABLE_SYSTEM_FOLDER:
        case SYSTEM_FOLDER:
            return ImageProvider.getImage(node.getIcon());
        case SIMPLE_FOLDER:
            // FIXME SML Move in repository node
            ECoreImage image = (view.getExpandedState(obj) ? ECoreImage.FOLDER_OPEN_ICON : ECoreImage.FOLDER_CLOSE_ICON);
            return ImageProvider.getImage(image);
        default:
            if (node.getObject() == null) {
                return ImageProvider.getImage(node.getIcon());
            }

            switch (node.getObject().getType()) {
            case METADATA_CON_QUERY:
            case SNIPPETS:
            case METADATA_CON_SYNONYM:
            case METADATA_CON_VIEW:
            case JOB_DOC:
            case JOBLET_DOC:
                return ImageProvider.getImage(node.getIcon());
            case METADATA_CON_TABLE:
                Image tableImage = ImageProvider.getImage(node.getIcon());
                Item item = node.getObject().getProperty().getItem();
                if (item != null && item instanceof DatabaseConnectionItem) {
                    if (PluginChecker.isCDCPluginLoaded()) {
                        ICDCProviderService service = (ICDCProviderService) GlobalServiceRegister.getDefault().getService(
                                ICDCProviderService.class);
                        if (service != null) {
                            String cdcLinkId = service.getCDCConnectionLinkId((DatabaseConnectionItem) item);
                            if (cdcLinkId != null) { // cdc connection exist.
                                if (node.getObject() instanceof MetadataTableRepositoryObject) {
                                    MetadataTable table = ((MetadataTableRepositoryObject) node.getObject()).getTable();
                                    String tableType = table.getTableType();
                                    if (tableType != null && "TABLE".equals(tableType)) { //$NON-NLS-1$
                                        ECDCStatus status = ECDCStatus.NONE;
                                        if (table.isActivatedCDC()) {
                                            status = ECDCStatus.ACTIVATED;
                                        } else if (table.isAttachedCDC()) {
                                            status = ECDCStatus.ADDED;
                                        }
                                        return OverlayImageProvider.getImageWithCDCStatus(tableImage, status).createImage();
                                    }
                                }
                            }
                        }
                    }
                }
                return tableImage;
            case METADATA_CON_CDC:
                ImageDescriptor idf = RepositoryPlugin.imageDescriptorFromPlugin(RepositoryPlugin.PLUGIN_ID,
                        "icons/subscriber.jpg"); //$NON-NLS-1$
                return idf.createImage();
            default:
                break;
            }

            return getImage(node.getObject());
        }
    }

    public Color getBackground(Object element) {
        return null;
    }

    public Color getForeground(Object element) {
        RepositoryNode node = (RepositoryNode) element;
        switch (node.getType()) {
        case REFERENCED_PROJECT:
            return STABLE_PRIMARY_ENTRY_COLOR;
        case STABLE_SYSTEM_FOLDER:
            if (node.getLabel().equals(ERepositoryObjectType.SNIPPETS.toString())) {
                return INACTIVE_ENTRY_COLOR;
            }
            if (node.getContentType() == ERepositoryObjectType.METADATA) {
                return STABLE_PRIMARY_ENTRY_COLOR;
            }
        case SYSTEM_FOLDER:
            if (node.getContentType() == ERepositoryObjectType.BUSINESS_PROCESS) {
                return STABLE_PRIMARY_ENTRY_COLOR;
            }
            if (node.getContentType() == ERepositoryObjectType.PROCESS) {
                return STABLE_PRIMARY_ENTRY_COLOR;
            }
            return STABLE_SECONDARY_ENTRY_COLOR;
        default:
            ERepositoryStatus repositoryStatus = node.getObject().getRepositoryStatus();
            if (repositoryStatus == ERepositoryStatus.LOCK_BY_OTHER) {
                return LOCKED_ENTRY;
            } else {
                if (PluginChecker.isRefProjectLoaded()) {
                    IReferencedProjectService service = (IReferencedProjectService) GlobalServiceRegister.getDefault()
                            .getService(IReferencedProjectService.class);
                    if (service != null && service.isMergeRefProject()) {
                        IRepositoryViewObject object = node.getObject();
                        if (object != null) {
                            org.talend.core.model.properties.Project mainProject = ProjectManager.getInstance()
                                    .getCurrentProject().getEmfProject();
                            String projectLabel = object.getProjectLabel();
                            if (!mainProject.getLabel().equals(projectLabel)) {
                                return MERGED_REFERENCED_ITEMS_COLOR;
                            }
                        }
                    }
                }
                return null;
            }
        }
    }

    public Font getFont(Object element) {
        RepositoryNode node = (RepositoryNode) element;
        switch (node.getType()) {
        case STABLE_SYSTEM_FOLDER:
            if (node.getLabel().equals(ERepositoryObjectType.SNIPPETS.toString())) {
                return JFaceResources.getFontRegistry().defaultFont();
            }
        case SYSTEM_FOLDER:
            return JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
        default:
            return JFaceResources.getFontRegistry().defaultFont();
        }
    }

}
