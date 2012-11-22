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
package org.talend.designer.core.ui.editor.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Translatable;
import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.gef.ui.palette.editparts.PaletteEditPart;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IEbcdicConstant;
import org.talend.core.model.metadata.IHL7Constant;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.ISAPConstant;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.CDCConnection;
import org.talend.core.model.metadata.builder.connection.CDCType;
import org.talend.core.model.metadata.builder.connection.Concept;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.HL7FileNode;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.connection.MdmConceptType;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.metadata.builder.connection.SAPIDocUnit;
import org.talend.core.model.metadata.builder.connection.SalesforceModuleUnit;
import org.talend.core.model.metadata.builder.connection.XMLFileNode;
import org.talend.core.model.metadata.builder.connection.impl.BRMSConnectionImpl;
import org.talend.core.model.metadata.builder.connection.impl.HL7ConnectionImpl;
import org.talend.core.model.metadata.designerproperties.PropertyConstants.CDCTypeMode;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.EbcdicConnectionItem;
import org.talend.core.model.properties.FileItem;
import org.talend.core.model.properties.HL7ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.LinkRulesItem;
import org.talend.core.model.properties.MDMConnectionItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RulesItem;
import org.talend.core.model.properties.SAPConnectionItem;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.properties.SalesforceSchemaConnectionItem;
import org.talend.core.model.properties.ValidationRulesConnectionItem;
import org.talend.core.model.repository.DragAndDropManager;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.IComponentName;
import org.talend.core.model.utils.IDragAndDropServiceHandler;
import org.talend.core.model.utils.SQLPatternUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.RepositoryComponentManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.core.ui.ICDCProviderService;
import org.talend.core.ui.IJobletProviderService;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.core.ui.metadata.command.RepositoryChangeMetadataForEBCDICCommand;
import org.talend.core.ui.metadata.command.RepositoryChangeMetadataForHL7Command;
import org.talend.core.ui.metadata.command.RepositoryChangeMetadataForSAPCommand;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.components.ExternalUtilities;
import org.talend.designer.core.model.process.ConnectionManager;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ContextParameterTypeImpl;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ContextTypeImpl;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.dialog.mergeorder.ChooseJobletDialog;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.TalendEditor;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.designer.core.ui.editor.cmd.ConnectionCreateCommand;
import org.talend.designer.core.ui.editor.cmd.ConnectionReconnectCommand;
import org.talend.designer.core.ui.editor.cmd.CreateNodeContainerCommand;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.cmd.QueryGuessCommand;
import org.talend.designer.core.ui.editor.cmd.RepositoryChangeMetadataCommand;
import org.talend.designer.core.ui.editor.cmd.RepositoryChangeQueryCommand;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainer;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainerPart;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.designer.core.utils.DesignerUtilities;
import org.talend.designer.core.utils.ValidationRulesUtil;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.editor.JobEditorInput;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.QueryRepositoryObject;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.SAPFunctionRepositoryObject;
import org.talend.repository.model.SAPIDocRepositoryObject;
import org.talend.repository.model.SalesforceModuleRepositoryObject;
import org.talend.repository.ui.utils.ConnectionContextHelper;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * Performs a native Drop for the talendEditor. see feature
 * 
 * $Id: TalendEditorDropTargetListener.java 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
public class TalendEditorDropTargetListener extends TemplateTransferDropTargetListener {

    private AbstractTalendEditor editor;

    private boolean fromPalette; // only for palette dnd, feature 6457

    /**
     * TalendEditorDropTargetListener constructor comment.
     * 
     * @param editor
     */
    public TalendEditorDropTargetListener(AbstractTalendEditor editor) {
        super(editor.getViewer());
        this.editor = editor;
        setTransfer(LocalSelectionTransfer.getTransfer());
    }

    public boolean isEnabled(DropTargetEvent e) {
        if (PluginChecker.isCDCPluginLoaded()) {
            ICDCProviderService service = (ICDCProviderService) GlobalServiceRegister.getDefault().getService(
                    ICDCProviderService.class);
            Object obj = getSelection().getFirstElement();
            if (obj instanceof RepositoryNode) {
                RepositoryNode sourceNode = (RepositoryNode) obj;
                if (service != null && (service.isSubscriberTableNode(sourceNode) || service.isSystemSubscriberTable(sourceNode))) {
                    return false;
                }
            }

        }
        return !this.editor.getProcess().isReadOnly();
    }

    public void dragEnter(DropTargetEvent event) {

    }

    public void dragLeave(DropTargetEvent event) {

    }

    public void dragOperationChanged(DropTargetEvent event) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.dnd.TemplateTransferDropTargetListener#handleDragOver()
     */
    @Override
    protected void handleDragOver() {
        super.handleDragOver();
        // when the job that selected is the same one in the current editor, the drag event should be disabled.
        IStructuredSelection selection = getSelection();
        if (selection.size() != 1) {
            getCurrentEvent().detail = DND.DROP_NONE;
            return;
        }

        if (selection.getFirstElement() instanceof RepositoryNode) {
            RepositoryNode sourceNode = (RepositoryNode) selection.getFirstElement();
            if (equalsJobInCurrentEditor(sourceNode)) {
                getCurrentEvent().detail = DND.DROP_NONE;
            }
        }

    }

    public void dragOver(DropTargetEvent event) {
        // multi-drag for job,context, sqlPattern.
        IStructuredSelection selection = getSelection();
        if (selection.size() > 1) {
            boolean allowed = true;
            Item temItem = null;
            Iterator iter = selection.iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                if (next instanceof RepositoryNode) {
                    RepositoryNode sourceNode = (RepositoryNode) next;
                    IRepositoryViewObject object = sourceNode.getObject();
                    if (object != null) {
                        Item selectItem = object.getProperty().getItem();
                        if (temItem == null) {
                            temItem = selectItem;
                            continue;
                        }
                        if (selectItem instanceof ProcessItem && !(temItem instanceof ProcessItem)) {
                            allowed = false;
                        } else if (selectItem instanceof ContextItem && !(temItem instanceof ContextItem)) {
                            allowed = false;
                        } else if (selectItem instanceof SQLPatternItem && !(temItem instanceof SQLPatternItem)) {
                            allowed = false;
                        }
                    }
                }
            }
            if (!allowed) {
                event.detail = DND.DROP_NONE;
            }
        } else {
            CreateRequest req = ((CreateRequest) getTargetRequest());
            Object o = null;
            try {
                o = req.getNewObject();
            } catch (Exception e) {
                return;
            }
            if (!(o instanceof Node)) {
                return;
            }

            RootEditPart rep = editor.getViewer().getRootEditPart().getRoot();

            Point viewOriginalPosition = new Point();
            if (rep instanceof ScalableFreeformRootEditPart) {
                ScalableFreeformRootEditPart root = (ScalableFreeformRootEditPart) rep;
                Viewport viewport = (Viewport) root.getFigure();
                viewOriginalPosition = viewport.getViewLocation();
            }

            org.eclipse.swt.graphics.Point swtLocation = new org.eclipse.swt.graphics.Point(event.x + viewOriginalPosition.x,
                    event.y + viewOriginalPosition.y);
            Canvas canvas = (Canvas) editor.getViewer().getControl();
            swtLocation = canvas.toControl(swtLocation);
            // System.out.println("topLeft:" + topLeftpoint + " / event:" + swtLocation);
            org.eclipse.draw2d.geometry.Point draw2dPosition = new org.eclipse.draw2d.geometry.Point(swtLocation.x, swtLocation.y);
            SubjobContainerPart containerPart = (SubjobContainerPart) getTargetEditPart();

            for (Object child : editor.getProcessPart().getChildren()) {
                if (child instanceof SubjobContainerPart) {
                    SubjobContainer container = (SubjobContainer) ((SubjobContainerPart) child).getModel();
                    if (container.getSubjobContainerRectangle().contains(draw2dPosition)) {
                        containerPart = (SubjobContainerPart) child;
                    }
                }
            }

            if (containerPart != null) {
                List<org.talend.designer.core.ui.editor.connections.Connection> connections = CreateComponentOnLinkHelper
                        .getConnection(containerPart, draw2dPosition, (Node) o);
                for (org.talend.designer.core.ui.editor.connections.Connection connection : connections) {
                    CreateComponentOnLinkHelper.selectConnection(connection, containerPart);
                }

                if (connections.isEmpty()) {
                    CreateComponentOnLinkHelper.unselectAllConnections(containerPart);
                }
            } else {
                for (Object child : editor.getProcessPart().getChildren()) {
                    if (child instanceof SubjobContainerPart) {
                        CreateComponentOnLinkHelper.unselectAllConnections((SubjobContainerPart) child);
                    }
                }
            }
        }
    }

    @Override
    protected Request createTargetRequest() {
        fromPalette = false;
        CreateRequest request = new CreateRequest();
        CreationFactory factory = getFactory(LocalSelectionTransfer.getTransfer().getSelection());
        if (factory != null) {
            fromPalette = true;
            request.setFactory(factory);
            return request;
        }
        return super.createTargetRequest();
    }

    @Override
    protected CreationFactory getFactory(Object template) {
        CreationFactory factory = super.getFactory(template);
        if (factory == null) { // for palette dnd, feature 6457
            if (template != null && template instanceof IStructuredSelection) {
                Object element = ((IStructuredSelection) template).getFirstElement();
                if (element != null && element instanceof PaletteEditPart) {
                    Object model = ((PaletteEditPart) element).getModel();
                    if (model != null && model instanceof ToolEntry) {
                        return (CreationFactory) ((ToolEntry) model).getToolProperty(CreationTool.PROPERTY_CREATION_FACTORY);
                    }

                }
            }
        }
        return factory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.dnd.TemplateTransferDropTargetListener#handleDrop()
     */
    @Override
    protected void handleDrop() {
        updateTargetRequest();
        updateTargetEditPart();
        // getCurrentEvent()
        // if drop a node on the job, create new component,
        // else just update the schema or something of the target component.

        // if (getTargetEditPart() instanceof NodeContainerPart) {

        // IEditorPart iep = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        // IEditorInput iei = iep.getEditorInput();
        // iei
        // EditPart ep = getTargetEditPart();

        if (fromPalette && getTargetRequest() instanceof CreateRequest) {
            if (getTargetEditPart() instanceof ProcessPart) {
                // for palette dnd, feature 6457
                Object newObject = ((CreateRequest) getTargetRequest()).getNewObject();
                if (newObject != null) {
                    Command command = getCommand();
                    if (command != null) {
                        execCommandStack(command);
                    }
                }
                return;
            } else if (getTargetEditPart() instanceof SubjobContainerPart) {
                CreateRequest req = ((CreateRequest) getTargetRequest());
                Object o = req.getNewObject();
                Point location = req.getLocation();
                if (o instanceof Node) {
                    createComponentOnLink((Node) o, location);
                }
                return;
            } else if (getTargetEditPart() instanceof JobletContainerPart) {
                JobletContainerPart jobletPart = (JobletContainerPart) getTargetEditPart();
                if (isLock(jobletPart)) {
                    Shell shell = Display.getCurrent().getActiveShell();
                    ChooseJobletDialog dialog = new ChooseJobletDialog(new Shell(shell), getDropLocation());
                    if (dialog.open() == dialog.OK) {
                        EditPart part = getTargetEditPart();
                        if (dialog.addToJoblet()) {

                            AbstractMultiPageTalendEditor openEditor = getJobletPart((JobletContainerPart) part);
                            part = openEditor.getDesignerEditor().getProcessPart();
                            // editor = openEditor.getTalendEditor();
                            setTargetEditPart(part);
                            Object newObject = ((CreateRequest) getTargetRequest()).getNewObject();
                            if (newObject != null) {
                                Command command = getCommand();
                                if (command != null) {
                                    CommandStack commandStack = (CommandStack) openEditor.getAdapter(CommandStack.class);
                                    if (commandStack != null) {
                                        commandStack.execute(command);
                                    } else {
                                        command.execute();
                                    }
                                }
                            }
                            return;
                        } else {
                            part = getParentPart(part);
                            setTargetEditPart(part);
                            Object newObject = ((CreateRequest) getTargetRequest()).getNewObject();
                            if (newObject != null) {
                                Command command = getCommand();
                                if (command != null) {
                                    execCommandStack(command);
                                }
                            }
                            return;
                        }

                    }
                }
            }

        }

        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
            ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                    ICamelDesignerCoreService.class);
            if (getTargetEditPart().getModel() != null && (getTargetEditPart().getModel() instanceof Process)
                    && camelService.isCamelMulitPageEditor(((Process) getTargetEditPart().getModel()).getEditor())) {
                return;
            }
        }
        List<Object> sources = getSelectSource();
        if (containsContextSource(sources)) {
            createContext(sources);
        } else {
            if (!(getTargetEditPart() instanceof NodeContainerPart)) {

                try {
                    createNewComponent(getCurrentEvent());
                } catch (OperationCanceledException e) {
                    return;
                }

            } else {
                if (containsSQLPatternSource(sources)) {
                    createSQLPattern(sources);
                } else {
                    createSchema(getSelection().getFirstElement(), getTargetEditPart());
                    createQuery(getSelection().getFirstElement(), getTargetEditPart());
                    createProperty(getSelection().getFirstElement(), getTargetEditPart());
                    createChildJob(getSelection().getFirstElement(), getTargetEditPart());
                    createValidationRule(getSelection().getFirstElement(), getTargetEditPart());
                }
            }
        }
        // in case after drag/drop the editor is dirty but can not get focus
        if (editor.isDirty()) {
            editor.setFocus();
        }
        this.eraseTargetFeedback();
    }

    private void createSQLPattern(List<Object> sourceList) {
        if (sourceList.size() == 0) {
            return;
        }
        NodeContainerPart nodePart = (NodeContainerPart) getTargetEditPart();
        Object model = nodePart.getModel();
        if (model instanceof NodeContainer) {
            Node node = ((NodeContainer) model).getNode();
            IElementParameter sqlPatternValue = node.getElementParameter(EParameterName.SQLPATTERN_VALUE.getName());
            if (sqlPatternValue != null) {
                boolean created = false;
                for (Object source : sourceList) {
                    if (source instanceof RepositoryNode) {
                        RepositoryNode sourceNode = (RepositoryNode) source;
                        Item item = sourceNode.getObject().getProperty().getItem();
                        if (item instanceof SQLPatternItem) {
                            SQLPatternItem pattern = (SQLPatternItem) item;
                            Property property = pattern.getProperty();
                            String propertyId = property.getId();
                            String propertyLabel = property.getLabel();
                            List<Map> values = (List<Map>) sqlPatternValue.getValue();
                            Map<String, String> patternMap = new HashMap<String, String>();
                            boolean contains = false;
                            for (Map map : values) {
                                String compoundId = (String) map.get(SQLPatternUtils.SQLPATTERNLIST);
                                String id = compoundId.split(SQLPatternUtils.ID_SEPARATOR)[0];
                                String name = compoundId.split(SQLPatternUtils.ID_SEPARATOR)[1];
                                if (id.equals(propertyId) && name.equals(propertyLabel)) {
                                    contains = true;
                                    break;
                                }
                            }
                            if (!contains) {
                                patternMap.put(SQLPatternUtils.SQLPATTERNLIST, propertyId + SQLPatternUtils.ID_SEPARATOR
                                        + propertyLabel);
                                values.add(patternMap);
                                sqlPatternValue.setValue(values);
                                created = true;
                            }
                        }
                    }
                }
                if (created) {
                    RepositoryPlugin.getDefault().getDesignerCoreService().switchToCurComponentSettingsView();
                }
            }
        }
    }

    private void createContext(List<Object> sourceList) {
        if (sourceList.size() == 0) {
            return;
        }
        boolean created = false;
        for (Object source : sourceList) {
            if (source instanceof RepositoryNode) {
                RepositoryNode sourceNode = (RepositoryNode) source;
                Item item = sourceNode.getObject().getProperty().getItem();
                if (item instanceof ContextItem) {
                    ContextItem contextItem = (ContextItem) item;
                    EList context = contextItem.getContext();
                    Set<String> contextSet = new HashSet<String>();
                    Iterator iterator = context.iterator();
                    while (iterator.hasNext()) {
                        Object obj = iterator.next();
                        if (obj instanceof ContextTypeImpl) {
                            EList contextParameters = ((ContextTypeImpl) obj).getContextParameter();
                            Iterator contextParas = contextParameters.iterator();
                            while (contextParas.hasNext()) {
                                ContextParameterTypeImpl contextParameterType = (ContextParameterTypeImpl) contextParas.next();
                                String name = contextParameterType.getName();
                                contextSet.add(name);
                            }
                        }
                    }
                    IEditorInput editorInput = editor.getEditorInput();
                    if (editorInput instanceof JobEditorInput) {
                        JobEditorInput jobInput = (JobEditorInput) editorInput;
                        IProcess2 process = jobInput.getLoadedProcess();
                        IContextManager contextManager = process.getContextManager();
                        List<IContext> listContext = contextManager.getListContext();
                        // context group will reflect absolutely if no context variable in contextViewer
                        if (!ConnectionContextHelper.containsVariable(contextManager)) {
                            // for bug 15608
                            ConnectionContextHelper.addContextVarForJob(process, contextItem, contextManager);
                            // ConnectionContextHelper.checkAndAddContextsVarDND(contextItem, contextManager);
                            created = true;
                        } else {
                            Set<String> addedContext = ConnectionContextHelper.checkAndAddContextVariables(contextItem,
                                    contextSet, contextManager, false);
                            if (addedContext != null && addedContext.size() > 0) {
                                ConnectionContextHelper.addContextVarForJob(process, contextItem, contextSet);
                                created = true;
                            }
                        }
                    }
                }
            }
        }
        if (created) {
            RepositoryPlugin.getDefault().getDesignerCoreService().switchToCurContextsView();
        }
    }

    private List<Object> getSelectSource() {
        List<Object> sourceList = new ArrayList<Object>();
        Iterator iterator = getSelection().iterator();
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            sourceList.add(obj);
        }
        return sourceList;
    }

    private boolean containsContextSource(List<Object> source) {
        if (source.size() == 0) {
            return false;
        }
        for (Object object : source) {
            if (object instanceof RepositoryNode) {
                RepositoryNode sourceNode = (RepositoryNode) object;
                Item item = sourceNode.getObject().getProperty().getItem();
                if (item instanceof ContextItem) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsSQLPatternSource(List<Object> source) {
        if (source.size() == 0) {
            return false;
        }
        for (Object object : source) {
            if (object instanceof RepositoryNode) {
                RepositoryNode sourceNode = (RepositoryNode) object;
                Item item = sourceNode.getObject().getProperty().getItem();
                if (item instanceof SQLPatternItem) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * DOC ycbai Comment method "createValidationRule".
     * 
     * @param dragModel
     * @param targetEditPart
     */
    private void createValidationRule(Object dragModel, EditPart targetEditPart) {
        if (!(dragModel instanceof RepositoryNode && targetEditPart instanceof NodeContainerPart)) {
            return;
        }
        RepositoryNode dragNode = (RepositoryNode) dragModel;
        NodeContainerPart nodePart = (NodeContainerPart) targetEditPart;
        if (dragNode.getObject().getProperty().getItem() instanceof ValidationRulesConnectionItem) {
            Node node = (Node) nodePart.getNodePart().getModel();
            List<IRepositoryViewObject> valRuleObjs = ValidationRulesUtil.getRelatedValidationRuleObjs(node);
            IRepositoryViewObject valRuleObj = dragNode.getObject();
            String schemaType = (String) node.getPropertyValue(EParameterName.SCHEMA_TYPE.getName());
            if (EmfComponent.BUILTIN.equals(schemaType)
                    || !ValidationRulesUtil.isCurrentValRuleObjInList(valRuleObjs, valRuleObj)) {
                MessageDialog.openWarning(editor.getSite().getShell(),
                        Messages.getString("SchemaTypeController.validationrule.title.warn"), //$NON-NLS-1$
                        Messages.getString("SchemaTypeController.validationrule.cannotApplyValMsg")); //$NON-NLS-1$
                return;
            }
            CompoundCommand cc = new CompoundCommand();
            cc.add(new PropertyChangeCommand(node, EParameterName.VALIDATION_RULES.getName(), true));
            cc.add(new ChangeValuesFromRepository(node, null, "VALIDATION_RULE_TYPE:VALIDATION_RULE_TYPE", //$NON-NLS-1$
                    EmfComponent.REPOSITORY));
            cc.add(new PropertyChangeCommand(node, EParameterName.REPOSITORY_VALIDATION_RULE_TYPE.getName(), valRuleObj
                    .getProperty().getId()));
            execCommandStack(cc);
        }
    }

    /**
     * DOC qwei Comment method "createChildJob".
     */
    private void createChildJob(Object dragModel, EditPart targetEditPart) {
        if (!(dragModel instanceof RepositoryNode && targetEditPart instanceof NodeContainerPart)) {
            return;
        }
        RepositoryNode dragNode = (RepositoryNode) dragModel;
        NodeContainerPart nodePart = (NodeContainerPart) targetEditPart;

        if (dragNode.getObject().getProperty().getItem() instanceof ProcessItem) {
            ProcessItem processItem = (ProcessItem) dragNode.getObject().getProperty().getItem();
            Command command = getChangeChildProcessCommand((Node) nodePart.getNodePart().getModel(), processItem);
            if (command != null) {
                execCommandStack(command);
            }
        }
    }

    /**
     * DOC bqian Comment method "createSchema".
     * 
     * @param firstElement
     * @param targetEditPart
     */
    private void createSchema(Object dragModel, EditPart targetEditPart) {
        if (!(dragModel instanceof RepositoryNode && targetEditPart instanceof NodeContainerPart)) {
            return;
        }
        RepositoryNode dragNode = (RepositoryNode) dragModel;
        NodeContainerPart nodePart = (NodeContainerPart) targetEditPart;
        Node node = (Node) nodePart.getNodePart().getModel();
        IRepositoryViewObject object = dragNode.getObject();

        if (dragNode.getObject().getProperty().getItem() instanceof ConnectionItem) {
            CompoundCommand cc = new CompoundCommand();
            boolean isValRulesLost = false;
            if (dragNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_TABLE) {
                IRepositoryViewObject currentValRuleObj = ValidationRulesUtil.getCurrentValidationRuleObjs(node);
                if (currentValRuleObj != null) {
                    String schema = object.getProperty().getId() + " - " + object.getLabel(); //$NON-NLS-1$
                    List<IRepositoryViewObject> valRuleObjs = ValidationRulesUtil.getRelatedValidationRuleObjs(schema);
                    if (!ValidationRulesUtil.isCurrentValRuleObjInList(valRuleObjs, currentValRuleObj)) {
                        if (!MessageDialog.openConfirm(editor.getSite().getShell(),
                                Messages.getString("SchemaTypeController.validationrule.title.confirm"), //$NON-NLS-1$
                                Messages.getString("SchemaTypeController.validationrule.selectMetadataMsg"))) { //$NON-NLS-1$
                            isValRulesLost = false;
                            return;
                        } else {
                            isValRulesLost = true;
                        }
                    }
                }
            }
            ConnectionItem connectionItem = (ConnectionItem) dragNode.getObject().getProperty().getItem();
            Command command = getChangeMetadataCommand(dragNode, node, connectionItem);
            if (command != null) {
                cc.add(command);
            }
            if (isValRulesLost) {
                ValidationRulesUtil.appendRemoveValidationRuleCommands(cc, node);
            }
            if (cc.getCommands().size() > 0) {
                execCommandStack(cc);
            }
        }
    }

    private void createQuery(Object dragModel, EditPart targetEditPart) {
        if (!(dragModel instanceof RepositoryNode && targetEditPart instanceof NodeContainerPart)) {
            return;
        }
        RepositoryNode dragNode = (RepositoryNode) dragModel;
        NodeContainerPart nodePart = (NodeContainerPart) targetEditPart;
        if (dragNode.getObject().getProperty().getItem() instanceof ConnectionItem) {
            ConnectionItem connectionItem = (ConnectionItem) dragNode.getObject().getProperty().getItem();
            Command command = getChangeQueryCommand(dragNode, (Node) nodePart.getNodePart().getModel(), connectionItem);
            if (command != null) {
                execCommandStack(command);
            }
        }
    }

    private void createProperty(Object dragModel, EditPart targetEditPart) {
        if (!(dragModel instanceof RepositoryNode && targetEditPart instanceof NodeContainerPart)) {
            return;
        }
        RepositoryNode dragNode = (RepositoryNode) dragModel;
        NodeContainerPart nodePart = (NodeContainerPart) targetEditPart;
        if (dragNode.getObject().getProperty().getItem() instanceof ConnectionItem) {
            ConnectionItem connectionItem = (ConnectionItem) dragNode.getObject().getProperty().getItem();
            Command command = getChangePropertyCommand(dragNode, (Node) nodePart.getNodePart().getModel(), connectionItem);
            if (command != null) {
                execCommandStack(command);
            }
        }
    }

    private boolean equalsJobInCurrentEditor(RepositoryNode sourceNode) {
        Item item = sourceNode.getObject().getProperty().getItem();
        if (item instanceof ProcessItem) {
            return editor.getProcess().getProperty().getItem().equals(item);
        }
        return false;
    }

    private IStructuredSelection getSelection() {
        LocalSelectionTransfer transfer = (LocalSelectionTransfer) getTransfer();
        IStructuredSelection selection = (IStructuredSelection) transfer.getSelection();
        return selection;
    }

    /**
     * Used to store data temporarily. <br/>
     * 
     * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
     * 
     */
    class TempStore {

        // This is the element that user select in the repositoryView.
        RepositoryNode seletetedNode = null;

        IComponentName componentName = null;

        IComponent component;

    }

    public void createNewComponent(DropTargetEvent event1) {
        boolean quickCreateInput = event1.detail == DND.DROP_LINK;
        boolean quickCreateOutput = event1.detail == DND.DROP_COPY;
        Iterator iterator = getSelection().iterator();
        List<TempStore> list = new ArrayList<TempStore>();
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof RepositoryNode) {
                RepositoryNode sourceNode = (RepositoryNode) obj;
                if (equalsJobInCurrentEditor(sourceNode)) {
                    continue;
                }

                Item item = sourceNode.getObject().getProperty().getItem();
                ERepositoryObjectType type = sourceNode.getObjectType();
                if (!(item instanceof ConnectionItem) && !(item instanceof ProcessItem) && !(item instanceof JobletProcessItem)
                        && !(item instanceof RulesItem) && !(item instanceof LinkRulesItem)) { // hywang modified for
                    // feature 6484,for
                    // RulesItem
                    return;
                }

                if (item instanceof SalesforceSchemaConnectionItem && type == ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA) {
                    return;
                }

                TempStore store = new TempStore();

                store.seletetedNode = sourceNode;
                getAppropriateComponent(item, quickCreateInput, quickCreateOutput, store, type);
                if (store.component != null) {
                    list.add(store);
                } else {
                    MessageDialog.openInformation(editor.getEditorSite().getShell(),
                            Messages.getString("TalendEditorDropTargetListener.dngsupportdialog.title"), //$NON-NLS-1$
                            Messages.getString("TalendEditorDropTargetListener.dngsupportdialog.content")); //$NON-NLS-1$
                }
            }

            org.eclipse.swt.graphics.Point swtLocation = new org.eclipse.swt.graphics.Point(event1.x, event1.y);
            Canvas canvas = (Canvas) editor.getViewer().getControl();

            /*
             * translate to Canvas coordinate
             */
            swtLocation = canvas.toControl(swtLocation);
            org.eclipse.swt.graphics.Point size = canvas.getSize();
            /*
             * translate to Viewport coordinate with zoom
             */
            org.eclipse.draw2d.geometry.Point draw2dPosition = new org.eclipse.draw2d.geometry.Point(swtLocation.x, swtLocation.y);

            /*
             * calcule the view port position. Take into acounte the scroll position
             */
            ProcessPart part = (ProcessPart) editor.getViewer().getRootEditPart().getRoot().getChildren().get(0);

            IFigure targetFigure = part.getFigure();
            translateAbsolateToRelative(targetFigure, draw2dPosition);
            String lastUniqname = "";
            // creates every node
            for (Iterator<TempStore> iter = list.iterator(); iter.hasNext();) {
                TempStore store = iter.next();

                RepositoryNode selectedNode = store.seletetedNode;
                IComponent element = store.component;
                Node node = new Node(element);
                // for bug4564(metadata label format)
                // IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();
                // if (preferenceStore.getBoolean(TalendDesignerPrefConstants.USE_REPOSITORY_NAME)) {
                // node.setPropertyValue(EParameterName.LABEL.getName(), selectedNode.getObject().getLabel());
                // }
                IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();
                if (preferenceStore.getBoolean(TalendDesignerPrefConstants.USE_REPOSITORY_NAME)) {
                    String LabelValue = null;
                    RepositoryNode repositoryNode = null;
                    repositoryNode = (RepositoryNode) getSelection().getFirstElement();
                    // dnd a table
                    IElementParameter dbTableParam = node.getElementParameterFromField(EParameterFieldType.DBTABLE);
                    boolean hasDbTableField = dbTableParam != null;

                    if (repositoryNode.getObjectType() == ERepositoryObjectType.METADATA_CON_TABLE
                            && repositoryNode.getObject() != null
                            && repositoryNode.getObject().getProperty().getItem() instanceof DatabaseConnectionItem
                            && hasDbTableField) {
                        LabelValue = DesignerUtilities.getParameterVar(dbTableParam.getName());
                    } else if (repositoryNode.getObjectType() == ERepositoryObjectType.PROCESS) { // dnd a job
                        LabelValue = DesignerUtilities.getParameterVar(EParameterName.PROCESS);
                    } else if (CorePlugin.getDefault().getDesignerCoreService()
                            .getPreferenceStore(TalendDesignerPrefConstants.DEFAULT_LABEL).equals( //$NON-NLS-1$
                                    node.getPropertyValue(EParameterName.LABEL.getName()))) {// dnd a default
                        LabelValue = selectedNode.getObject().getLabel();
                    }
                    if (LabelValue != null) {
                        node.setPropertyValue(EParameterName.LABEL.getName(), LabelValue);
                    }
                }
                processSpecificDBTypeIfSameProduct(store.componentName, node);
                NodeContainer nc = null;
                if (node.isJoblet()) {
                    nc = new JobletContainer(node);
                } else {
                    nc = new NodeContainer(node);
                }

                // create component on link
                boolean executed = false;
                if (getSelection().size() == 1 && getTargetEditPart() instanceof SubjobContainerPart) {
                    executed = createComponentOnLink(node, draw2dPosition);
                }

                if (!executed) {
                    // create the node on the design sheet
                    execCommandStack(new CreateNodeContainerCommand((Process) editor.getProcess(), nc, draw2dPosition));
                }
                // initialize the propertiesView

                List<Command> commands = createRefreshingPropertiesCommand(selectedNode, node);
                for (Command command : commands) {
                    execCommandStack(command);
                }
                draw2dPosition = draw2dPosition.getCopy();
                draw2dPosition.x += TalendEditor.GRID_SIZE;
                draw2dPosition.y += TalendEditor.GRID_SIZE;

                node.checkNode();
                lastUniqname = node.getUniqueName();
            }
            // setselecte(part, lastUniqname);
        }

    }

    private void setselecte(AbstractEditPart part, String lastUniqname) {
        List<NodePart> parts = getAllNodePart(part);
        for (NodePart nodePart : parts) {
            if (nodePart.getModel() instanceof Node) {
                Node node = (Node) nodePart.getModel();
                // for (String uniqname : uniquteNames) {
                if (node.getUniqueName().equals(lastUniqname)) {
                    nodePart.setDrop(true);
                    nodePart.setSelected(EditPart.SELECTED);
                    nodePart.setDrop(false);
                } else {
                    nodePart.setSelected(EditPart.SELECTED_NONE);
                }

                // }
            }
        }
    }

    private List<NodePart> getAllNodePart(AbstractEditPart part) {
        List<NodePart> partList = new ArrayList<NodePart>();
        if (part.getChildren() != null && part.getChildren().size() > 0) {
            for (int i = 0; i < part.getChildren().size(); i++) {
                if (part.getChildren().get(i) instanceof AbstractEditPart) {
                    AbstractEditPart child = (AbstractEditPart) part.getChildren().get(i);
                    if (child.getChildren() != null && child.getChildren().size() > 0) {
                        partList.addAll(getAllNodePart(child));
                    } else {
                        if (child instanceof NodePart) {
                            partList.add((NodePart) child);
                        }
                    }
                }
            }
            // partList.add
        } else {
            if (part instanceof NodePart) {
                partList.add((NodePart) part);
            }
        }
        return partList;
    }

    /**
     * DOC bqian Comment method "createRefreshingPropertiesCommand".
     * 
     * @param selectedNode
     * @param node
     */
    private List<Command> createRefreshingPropertiesCommand(RepositoryNode selectedNode, Node node) {
        List<Command> list = new ArrayList<Command>();
        if (selectedNode.getObject().getProperty().getItem() instanceof ConnectionItem) {
            String propertyId = selectedNode.getObject().getProperty().getId();
            ConnectionItem originalConnectionItem = (ConnectionItem) selectedNode.getObject().getProperty().getItem();
            ConnectionItem connectionItem = originalConnectionItem;
            Connection originalConnection = connectionItem.getConnection();
            Connection connection = connectionItem.getConnection();
            // if component is CDC, replace by the CDC connection.
            if (node.getComponent().getName().contains("CDC")) { // to replace by a flag CDC in component? //$NON-NLS-1$
                if (selectedNode.getObject().getProperty().getItem() instanceof DatabaseConnectionItem) {
                    final DatabaseConnection databaseConnection = (DatabaseConnection) connection;
                    CDCConnection cdcConn = databaseConnection.getCdcConns();
                    if (cdcConn != null) {
                        EList cdcTypes = cdcConn.getCdcTypes();
                        if (cdcTypes != null && !cdcTypes.isEmpty()) {
                            CDCType cdcType = (CDCType) cdcTypes.get(0);
                            // replace property by CDC property.
                            propertyId = cdcType.getLinkDB();
                            try {
                                IRepositoryViewObject object = ProxyRepositoryFactory.getInstance().getLastVersion(propertyId);
                                if (object != null) {
                                    if (object.getProperty().getItem() instanceof DatabaseConnectionItem) {
                                        DatabaseConnectionItem dbConnItem = (DatabaseConnectionItem) object.getProperty()
                                                .getItem();
                                        // replace connection by CDC connection
                                        connectionItem = dbConnItem;
                                        connection = dbConnItem.getConnection();
                                    }
                                }
                            } catch (PersistenceException e) {
                                ExceptionHandler.process(e);
                            }
                            // set cdc type mode.
                            IElementParameter logModeParam = node.getElementParameter(EParameterName.CDC_TYPE_MODE.getName());
                            if (logModeParam != null) {
                                String cdcTypeMode = ((DatabaseConnection) originalConnection).getCdcTypeMode();
                                Command logModeCmd = new PropertyChangeCommand(node, EParameterName.CDC_TYPE_MODE.getName(),
                                        CDCTypeMode.LOG_MODE.getName().equals(cdcTypeMode));
                                list.add(logModeCmd);
                            }
                            // set lib for as400 so far.
                            final String name = "SOURCE_LIB"; //$NON-NLS-1$
                            IElementParameter libParam = node.getElementParameter(name);
                            if (libParam != null) {
                                Command libSettingCmd = new PropertyChangeCommand(node, name,
                                        TalendTextUtils.addQuotes(databaseConnection.getSID()));
                                list.add(libSettingCmd);
                            }

                        }
                    }
                }

            }

            // for SAP
            if (selectedNode.getObjectType() == ERepositoryObjectType.METADATA_SAP_FUNCTION
                    && PluginChecker.isSAPWizardPluginLoaded()) {
                SAPFunctionUnit functionUnit = (SAPFunctionUnit) ((SAPFunctionRepositoryObject) selectedNode.getObject())
                        .getAbstractMetadataObject();
                for (MetadataTable table : (List<MetadataTable>) functionUnit.getTables()) {
                    Command sapCmd = new RepositoryChangeMetadataForSAPCommand(node, ISAPConstant.TABLE_SCHEMAS,
                            table.getLabel(), ConvertionHelper.convert(table), functionUnit);
                    list.add(sapCmd);
                }
            }

            // fore EBCDIC, by cli
            if ((connectionItem instanceof EbcdicConnectionItem) && PluginChecker.isEBCDICPluginLoaded()) {
                // TDI-20505:integration the drag/drop for EBCDIC connection and EBCDIC metadataTable
                IRepositoryViewObject object = selectedNode.getObject();
                if (selectedNode.getObjectType() == ERepositoryObjectType.METADATA_FILE_EBCDIC) {
                    for (MetadataTable table : (Set<MetadataTable>) ConnectionHelper.getTables(originalConnection)) {
                        Command ebcdicCmd = new RepositoryChangeMetadataForEBCDICCommand(node, IEbcdicConstant.TABLE_SCHEMAS,
                                table.getLabel(), ConvertionHelper.convert(table));
                        list.add(ebcdicCmd);
                    }
                }
                if (selectedNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_TABLE) {
                    MetadataTable table = null;
                    if (object instanceof MetadataTableRepositoryObject) {
                        table = ((MetadataTableRepositoryObject) object).getTable();
                    }

                    Command ebcdicCmd = new RepositoryChangeMetadataForEBCDICCommand(node, IEbcdicConstant.TABLE_SCHEMAS,
                            table.getLabel(), ConvertionHelper.convert(table));
                    list.add(ebcdicCmd);
                }
            }
            // fore HL7, by gcui
            if ((selectedNode.getObjectType() == ERepositoryObjectType.METADATA_FILE_HL7 && PluginChecker.isHL7PluginLoaded())
                    || (selectedNode.getParent() != null
                            && selectedNode.getParent().getObjectType() == ERepositoryObjectType.METADATA_FILE_HL7 && PluginChecker
                            .isHL7PluginLoaded())) {
                if (originalConnection instanceof HL7ConnectionImpl) {
                    if (((HL7ConnectionImpl) originalConnection).getRoot() != null) {
                        List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
                        for (Object obj : ((HL7ConnectionImpl) originalConnection).getRoot()) {
                            if (obj instanceof HL7FileNode) {
                                Map<String, String> newMap = new HashMap<String, String>();
                                newMap.put(IHL7Constant.ATTRIBUTE, ((HL7FileNode) obj).getAttribute());
                                newMap.put(IHL7Constant.PATH, ((HL7FileNode) obj).getFilePath());
                                newMap.put(IHL7Constant.COLUMN, ((HL7FileNode) obj).getRelatedColumn());
                                newMap.put(IHL7Constant.ORDER, String.valueOf(((HL7FileNode) obj).getOrder()));
                                newMap.put(IHL7Constant.VALUE, ((HL7FileNode) obj).getDefaultValue());
                                newMap.put(IHL7Constant.REPEATABLE, String.valueOf(((HL7FileNode) obj).isRepeatable()));
                                mapList.add(newMap);
                            }
                        }
                        IExternalNode externalNode = ExternalUtilities.getExternalNodeReadyToOpen(node);
                        if (externalNode != null && externalNode.getElementParameter("ROOT") != null) {
                            externalNode.getElementParameter("ROOT").setValue(mapList);
                        }

                        String fileName = ((HL7ConnectionImpl) originalConnection).getOutputFilePath();
                        if (externalNode != null && externalNode.getElementParameter("FILENAME") != null && fileName != null) {
                            externalNode.getElementParameter("FILENAME").setValue(TalendTextUtils.addQuotes(fileName));
                        }

                    }

                }

                // fore HL7, by gcui
                if (selectedNode.getObjectType() == ERepositoryObjectType.METADATA_FILE_HL7 && PluginChecker.isHL7PluginLoaded()) {
                    for (MetadataTable table : (Set<MetadataTable>) ConnectionHelper.getTables(originalConnection)) {
                        Command hl7Cmd = new RepositoryChangeMetadataForHL7Command(node, IHL7Constant.TABLE_SCHEMAS,
                                table.getLabel(), ConvertionHelper.convert(table));
                        list.add(hl7Cmd);
                    }
                }
            }

            // for brms
            if ((selectedNode.getObjectType() == ERepositoryObjectType.METADATA_FILE_BRMS && PluginChecker.isBRMSPluginLoaded())
                    || (selectedNode.getParent() != null
                            && selectedNode.getParent().getObjectType() == ERepositoryObjectType.METADATA_FILE_BRMS && PluginChecker
                            .isBRMSPluginLoaded())) {
                if (originalConnection instanceof BRMSConnectionImpl) {
                    if (((BRMSConnectionImpl) originalConnection).getRoot() != null) {
                        List<Map<String, String>> rootList = new ArrayList<Map<String, String>>();
                        List<Map<String, String>> loopList = new ArrayList<Map<String, String>>();
                        List<Map<String, String>> groupList = new ArrayList<Map<String, String>>();
                        for (Object obj : ((BRMSConnectionImpl) originalConnection).getRoot()) {
                            if (obj instanceof XMLFileNode) {
                                Map<String, String> rootMap = new HashMap<String, String>();
                                rootMap.put("ATTRIBUTE", ((XMLFileNode) obj).getAttribute());
                                rootMap.put("PATH", ((XMLFileNode) obj).getXMLPath());
                                rootMap.put("COLUMN", ((XMLFileNode) obj).getRelatedColumn());
                                rootMap.put("ORDER", String.valueOf(((XMLFileNode) obj).getOrder()));
                                rootMap.put("VALUE", ((XMLFileNode) obj).getDefaultValue());
                                rootList.add(rootMap);

                            }
                        }
                        for (Object obj : ((BRMSConnectionImpl) originalConnection).getLoop()) {
                            if (obj instanceof XMLFileNode) {
                                Map<String, String> loopMap = new HashMap<String, String>();
                                loopMap.put("ATTRIBUTE", ((XMLFileNode) obj).getAttribute());
                                loopMap.put("PATH", ((XMLFileNode) obj).getXMLPath());
                                loopMap.put("COLUMN", ((XMLFileNode) obj).getRelatedColumn());
                                loopMap.put("ORDER", String.valueOf(((XMLFileNode) obj).getOrder()));
                                loopMap.put("VALUE", ((XMLFileNode) obj).getDefaultValue());
                                loopList.add(loopMap);
                            }
                        }
                        for (Object obj : ((BRMSConnectionImpl) originalConnection).getGroup()) {
                            if (obj instanceof XMLFileNode) {
                                Map<String, String> groupMap = new HashMap<String, String>();
                                groupMap.put("ATTRIBUTE", ((XMLFileNode) obj).getAttribute());
                                groupMap.put("PATH", ((XMLFileNode) obj).getXMLPath());
                                groupMap.put("COLUMN", ((XMLFileNode) obj).getRelatedColumn());
                                groupMap.put("ORDER", String.valueOf(((XMLFileNode) obj).getOrder()));
                                groupMap.put("VALUE", ((XMLFileNode) obj).getDefaultValue());
                                groupList.add(groupMap);
                            }
                        }
                        IExternalNode externalNode = ExternalUtilities.getExternalNodeReadyToOpen(node);
                        if (externalNode != null && externalNode.getElementParameter("ROOT") != null) {
                            externalNode.getElementParameter("ROOT").setValue(rootList);
                        }
                        if (externalNode != null && externalNode.getElementParameter("LOOP") != null) {
                            externalNode.getElementParameter("LOOP").setValue(loopList);
                        }
                        if (externalNode != null && externalNode.getElementParameter("GROUP") != null) {
                            externalNode.getElementParameter("GROUP").setValue(groupList);
                        }

                    }

                }
            }

            IElementParameter propertyParam = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
            if (propertyParam != null) {
                propertyParam.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName()).setValue(EmfComponent.REPOSITORY);
                propertyParam.getChildParameters().get(EParameterName.REPOSITORY_PROPERTY_TYPE.getName()).setValue(propertyId);
            }
            IProxyRepositoryFactory factory = DesignerPlugin.getDefault().getProxyRepositoryFactory();

            Map<String, IMetadataTable> repositoryTableMap = new HashMap<String, IMetadataTable>();

            if (!originalConnection.isReadOnly()) {
                for (Object tableObj : ConnectionHelper.getTables(originalConnection)) {
                    org.talend.core.model.metadata.builder.connection.MetadataTable table;

                    table = (org.talend.core.model.metadata.builder.connection.MetadataTable) tableObj;

                    if (factory.getStatus(originalConnectionItem) != ERepositoryStatus.DELETED) {
                        if (!factory.isDeleted(table)) {
                            String value = table.getId();
                            IMetadataTable newTable = ConvertionHelper.convert(table);
                            repositoryTableMap.put(value, newTable);
                        }
                    }
                }
            }
            // DesignerPlugin.getDefault().getProxyRepositoryFactory().getLastVersion("")
            if (propertyParam != null) {
                // command used to set property type
                IMetadataTable metadataTable = null;
                if (selectedNode.getContentType() == ERepositoryObjectType.METADATA_MDMCONNECTION
                        && selectedNode.getObjectType() == ERepositoryObjectType.METADATA_CON_TABLE) {
                    if (selectedNode.getObject() instanceof IMetadataTable) {
                        metadataTable = (IMetadataTable) selectedNode.getObject();
                    }
                }
                ChangeValuesFromRepository command1 = new ChangeValuesFromRepository(node, connection, metadataTable,
                        propertyParam.getName() + ":" + EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), propertyId, true); //$NON-NLS-1$
                command1.setMaps(repositoryTableMap);
                if (selectedNode.getProperties(EProperties.CONTENT_TYPE) != ERepositoryObjectType.METADATA_CON_QUERY) {
                    command1.setGuessQuery(true);
                }

                if (selectedNode.getParent() != null
                        && selectedNode.getParent().getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_SAP_FUNCTION) {
                    IRepositoryViewObject functionObject = selectedNode.getParent().getObject();
                    if (functionObject instanceof SAPFunctionRepositoryObject) {
                        SAPFunctionRepositoryObject sapObj = (SAPFunctionRepositoryObject) functionObject;

                        command1.setSapFunctionLabel(((SAPFunctionUnit) sapObj.getAbstractMetadataObject()).getLabel());
                    }
                }

                if (selectedNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_SAP_FUNCTION) {
                    IRepositoryViewObject selectedObj = selectedNode.getObject();
                    if (selectedObj instanceof SAPFunctionRepositoryObject) {
                        SAPFunctionRepositoryObject sapObj = (SAPFunctionRepositoryObject) selectedObj;

                        command1.setSapFunctionLabel(((SAPFunctionUnit) sapObj.getAbstractMetadataObject()).getLabel());
                    }
                }
                if (selectedNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_SAP_IDOC) {
                    IRepositoryViewObject selectedObj = selectedNode.getObject();
                    if (selectedObj instanceof SAPIDocRepositoryObject) {
                        SAPIDocRepositoryObject sapObj = (SAPIDocRepositoryObject) selectedObj;
                        command1.setSapIDocLabel(((SAPIDocUnit) sapObj.getAbstractMetadataObject()).getLabel());

                    }
                }
                // for salesForce module
                SalesforceModuleRepositoryObject sfObject = null;
                if (selectedNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_SALESFORCE_MODULE) {
                    sfObject = (SalesforceModuleRepositoryObject) selectedNode.getObject();
                } else if (selectedNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_TABLE) {
                    IRepositoryViewObject object = selectedNode.getParent().getObject();
                    if (object instanceof SalesforceModuleRepositoryObject) {
                        sfObject = (SalesforceModuleRepositoryObject) object;
                    }
                }
                if (sfObject != null) {
                    ModelElement modelElement = sfObject.getModelElement();
                    if (modelElement instanceof SalesforceModuleUnit) {
                        command1.setSalesForceModuleUnit((SalesforceModuleUnit) modelElement);
                    }
                }
                list.add(command1);
            }

            // command used to set metadata
            Command command = getChangeMetadataCommand(selectedNode, node, originalConnectionItem);
            if (command != null) {
                list.add(command);
            }

            // command used to set query
            if (selectedNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_QUERY) {
                IElementParameter queryParam = node.getElementParameterFromField(EParameterFieldType.QUERYSTORE_TYPE);

                QueryRepositoryObject object = (QueryRepositoryObject) selectedNode.getObject();
                Query query = object.getQuery();
                String value = originalConnectionItem.getProperty().getId() + " - " + query.getLabel(); //$NON-NLS-1$
                if (queryParam != null) {
                    RepositoryChangeQueryCommand command3 = new RepositoryChangeQueryCommand(node, query, queryParam.getName()
                            + ":" + EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName(), value); //$NON-NLS-1$
                    list.add(command3);
                }
            } else {
                if (connection instanceof DatabaseConnection && hasQuery(node)) {
                    DatabaseConnection connection2 = (DatabaseConnection) connection;
                    String schema = connection2.getUiSchema();
                    String dbType = connection2.getDatabaseType();
                    QueryGuessCommand queryGuessCommand = null;
                    if (node.getMetadataList().size() == 0) {
                        queryGuessCommand = new QueryGuessCommand(node, null, schema, dbType);
                    } else {
                        // modified by hyWang for bug 7190
                        queryGuessCommand = new QueryGuessCommand(node, node.getMetadataList().get(0), schema, dbType, connection);
                    }
                    if (queryGuessCommand != null) {
                        list.add(queryGuessCommand);
                    }
                }
            }
            // context, moved to ChangeValuesFromRepository(bug 5198)
            // ConnectionContextHelper.addContextForNodeParameter(node, connectionItem);
        } else if (selectedNode.getObject().getProperty().getItem() instanceof ProcessItem) {
            ProcessItem processItem = (ProcessItem) selectedNode.getObject().getProperty().getItem();
            // command used to set job
            String value = processItem.getProperty().getId();
            PropertyChangeCommand command4 = new PropertyChangeCommand(node, EParameterName.PROCESS_TYPE_PROCESS.getName(), value);
            list.add(command4);
            PropertyChangeCommand command5 = new PropertyChangeCommand(node, EParameterName.PROCESS_TYPE_CONTEXT.getName(),
                    processItem.getProcess().getDefaultContext());
            list.add(command5);
        } else if (selectedNode.getObject().getProperty().getItem() instanceof FileItem) { // hywang add for 6484
            if (selectedNode.getObject().getProperty().getItem() instanceof RulesItem) {
                RulesItem rulesItem = (RulesItem) selectedNode.getObject().getProperty().getItem();
                //                String displayName = "Rules:" + rulesItem.getProperty().getLabel(); //$NON-NLS-N$
                IElementParameter propertyParam = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
                if (propertyParam != null) {
                    propertyParam.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName())
                            .setValue(EmfComponent.REPOSITORY);
                    // propertyParam.getChildParameters().get(EParameterName.REPOSITORY_PROPERTY_TYPE.getName())
                    // .setListItemsDisplayName(new String[] { displayName });
                    final String showId = rulesItem.getProperty().getId();
                    PropertyChangeCommand command6 = new PropertyChangeCommand(node,
                            EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), showId);
                    list.add(command6);
                }
            }
        } else if (selectedNode.getObject().getProperty().getItem() instanceof LinkRulesItem) {
            LinkRulesItem linkItem = (LinkRulesItem) selectedNode.getObject().getProperty().getItem();
            IElementParameter propertyParam = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
            if (propertyParam != null) {
                propertyParam.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName()).setValue(EmfComponent.REPOSITORY);
                // propertyParam.getChildParameters().get(EParameterName.REPOSITORY_PROPERTY_TYPE.getName())
                // .setListItemsDisplayName(new String[] { displayName });
                final String showId = linkItem.getProperty().getId();
                PropertyChangeCommand command7 = new PropertyChangeCommand(node,
                        EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), showId);
                list.add(command7);
            }
        }
        return list;
    }

    public boolean hasQuery(Node node) {
        IElementParameter elementParameter = node.getElementParameterFromField(EParameterFieldType.MEMO_SQL);
        if (elementParameter == null) {
            return false;
        }
        return true;
    }

    /**
     * DOC bqian Comment method "getChangeMetadataCommand".
     * 
     * @param selectedNode
     * @param node
     * @param list
     * @param connectionItem
     */
    private Command getChangeMetadataCommand(RepositoryNode selectedNode, Node node, ConnectionItem connectionItem) {
        if (selectedNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_TABLE
                || selectedNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_SAP_FUNCTION
                || selectedNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_SALESFORCE_MODULE) {
            String etlSchema = null;
            if (connectionItem.getConnection() instanceof DatabaseConnection) {
                DatabaseConnection connection = (DatabaseConnection) connectionItem.getConnection();
                if (connection instanceof DatabaseConnection) {
                    etlSchema = connection.getUiSchema();
                }
                if (!"".equals(etlSchema)) {
                    IElementParameter e = node.getElementParameter("ELT_SCHEMA_NAME");
                    if (e != null) {
                        if (connection.isContextMode() && ContextParameterUtils.isContainContextParam(etlSchema)) {
                            e.setValue(etlSchema);
                        } else {
                            e.setValue(TalendTextUtils.addQuotes(etlSchema));
                        }
                    }
                    // node.getElementParameter("ELT_SCHEMA_NAME").setValue("\"" + etlSchema + "\"");
                }
            }

            IRepositoryViewObject object = selectedNode.getObject();
            MetadataTable table = null;
            if (object instanceof MetadataTableRepositoryObject) {
                table = ((MetadataTableRepositoryObject) object).getTable();
            } else if (object instanceof SAPFunctionRepositoryObject) {
                table = (MetadataTable) ((SAPFunctionRepositoryObject) object).getAdapter(MetadataTable.class);
            } else if (object instanceof SalesforceModuleRepositoryObject) {
                table = ((SalesforceModuleRepositoryObject) object).getDefaultTable();
            }
            if (table != null) {
                String value = connectionItem.getProperty().getId() + " - " + table.getLabel(); //$NON-NLS-1$
                IElementParameter schemaParam = node.getElementParameterFromField(EParameterFieldType.SCHEMA_TYPE);
                IElementParameter queryParam = node.getElementParameterFromField(EParameterFieldType.QUERYSTORE_TYPE);
                if (queryParam != null) {
                    queryParam = queryParam.getChildParameters().get(EParameterName.QUERYSTORE_TYPE.getName());
                    if (queryParam != null) {
                        queryParam.setValue(EmfComponent.BUILTIN);
                    }
                }
                // for SAP
                if (PluginChecker.isSAPWizardPluginLoaded() && connectionItem instanceof SAPConnectionItem
                        && object instanceof MetadataTableRepositoryObject) {
                    if (table.eContainer() instanceof SAPFunctionUnit) {
                        SAPFunctionUnit functionUnit = (SAPFunctionUnit) table.eContainer();
                        Command sapCmd = new RepositoryChangeMetadataForSAPCommand(node, ISAPConstant.TABLE_SCHEMAS,
                                table.getLabel(), ConvertionHelper.convert(table), functionUnit);
                        return sapCmd;
                    } else {
                        Command sapCmd = new RepositoryChangeMetadataForSAPCommand(node, ISAPConstant.TABLE_SCHEMAS,
                                table.getLabel(), ConvertionHelper.convert(table));
                        return sapCmd;
                    }
                }

                // for EBCDIC (bug 5860)
                // if (PluginChecker.isEBCDICPluginLoaded() && connectionItem instanceof EbcdicConnectionItem) {
                // Command ebcdicCmd = new RepositoryChangeMetadataForEBCDICCommand(node, IEbcdicConstant.TABLE_SCHEMAS,
                // table.getLabel(), ConvertionHelper.convert(table));
                // return ebcdicCmd;
                // }
                if (PluginChecker.isHL7PluginLoaded() && connectionItem instanceof HL7ConnectionItem) {
                    Command hl7Cmd = new RepositoryChangeMetadataForHL7Command(node, IEbcdicConstant.TABLE_SCHEMAS,
                            table.getLabel(), ConvertionHelper.convert(table));
                    return hl7Cmd;
                }
                if (schemaParam == null) {
                    return null;
                }
                if (node.isELTComponent()) {
                    node.setPropertyValue(EParameterName.LABEL.getName(), "__ELT_TABLE_NAME__");
                }
                schemaParam.getChildParameters().get(EParameterName.SCHEMA_TYPE.getName()).setValue(EmfComponent.REPOSITORY);
                RepositoryChangeMetadataCommand command2 = new RepositoryChangeMetadataCommand(node, schemaParam.getName() + ":" //$NON-NLS-1$
                        + EParameterName.REPOSITORY_SCHEMA_TYPE.getName(), value, ConvertionHelper.convert(table), null,
                        connectionItem.getConnection());
                return command2;
            }
        }
        return null;
    }

    private boolean isMdmOutput(RepositoryNode selectedNode, ConnectionItem connectionItem) {
        boolean isMdmOutput = false;
        if (connectionItem instanceof MDMConnectionItem) {
            MDMConnectionItem mdmItem = (MDMConnectionItem) connectionItem;
            final MDMConnection connection = (MDMConnection) mdmItem.getConnection();
            final EList<Concept> schemas = connection.getSchemas();
            final Object properties = selectedNode.getProperties(EProperties.LABEL);
            Concept concept = null;
            for (int i = 0; i < schemas.size(); i++) {
                final String label = schemas.get(i).getLabel();
                if (label != null && label.equals(properties)) {
                    concept = schemas.get(i);
                    break;
                }
            }
            if (concept != null && MdmConceptType.OUTPUT.equals(concept.getConceptType())) {
                isMdmOutput = true;
            }
        }
        return isMdmOutput;
    }

    private Command getChangeQueryCommand(RepositoryNode selectedNode, Node node, ConnectionItem connectionItem) {
        if (selectedNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_QUERY) {
            QueryRepositoryObject object = (QueryRepositoryObject) selectedNode.getObject();
            Query query = object.getQuery();
            String value = connectionItem.getProperty().getId() + " - " + query.getLabel(); //$NON-NLS-1$
            IElementParameter queryParam = node.getElementParameterFromField(EParameterFieldType.QUERYSTORE_TYPE);
            if (queryParam != null) {
                queryParam.getChildParameters().get(EParameterName.QUERYSTORE_TYPE.getName()).setValue(EmfComponent.REPOSITORY);
                RepositoryChangeQueryCommand command2 = new RepositoryChangeQueryCommand(node, query, queryParam.getName() + ":" //$NON-NLS-1$
                        + EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName(), value);
                return command2;
            }

        }
        return null;
    }

    private Command getChangePropertyCommand(RepositoryNode selectedNode, Node node, ConnectionItem connectionItem) {
        ERepositoryObjectType selectedNodetype = selectedNode.getObjectType();
        EDatabaseComponentName name = EDatabaseComponentName.getCorrespondingComponentName(connectionItem, selectedNodetype);
        if (name != null) {
            List<String> componentNameList = new ArrayList<String>();
            componentNameList.add(name.getInputComponentName());
            componentNameList.add(name.getOutPutComponentName());
            String nodeComponentName = node.getComponent().getName();
            if (componentNameList.contains(nodeComponentName)) {
                IElementParameter param = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
                if (param != null) {
                    return getPropertyPublicPart(selectedNode, param, node, connectionItem);
                }
            }
        }

        return null;
    }

    private Command getPropertyPublicPart(RepositoryNode selectedNode, IElementParameter param, Node node,
            ConnectionItem connectionItem) {
        param.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName()).setValue(EmfComponent.REPOSITORY);
        ChangeValuesFromRepository command2 = new ChangeValuesFromRepository(
                node,
                connectionItem.getConnection(),
                param.getName() + ":" + EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), selectedNode.getObject().getProperty().getId()); //$NON-NLS-1$
        if (selectedNode.getObject() instanceof IMetadataTable) {
            IMetadataTable metadataTable = (IMetadataTable) selectedNode.getObject();
            command2.setTable(metadataTable);
        }
        return command2;

    }

    /**
     * DOC qwei Comment method "getChangeChildProcessCommand".
     */
    private Command getChangeChildProcessCommand(Node node, ProcessItem processItem) {
        // command used to set job
        String value = processItem.getProperty().getId();
        IElementParameter processParam = node.getElementParameterFromField(EParameterFieldType.PROCESS_TYPE);
        if (processParam != null) {
            PropertyChangeCommand command2 = new PropertyChangeCommand(node, EParameterName.PROCESS_TYPE_PROCESS.getName(), value);
            return command2;
        }
        return null;
    }

    public void dropAccept(DropTargetEvent event) {
    }

    private void getAppropriateComponent(Item item, boolean quickCreateInput, boolean quickCreateOutput, TempStore store,
            ERepositoryObjectType type) {
        IComponentName rcSetting = RepositoryComponentManager.getSetting(item, type);

        // IComponentName name = EDatabaseComponentName.getCorrespondingComponentName(item, type);
        // For handler, need check for esb
        if (rcSetting == null) {
            for (IDragAndDropServiceHandler handler : DragAndDropManager.getHandlers()) {
                rcSetting = handler.getCorrespondingComponentName(item, type);
                if (rcSetting != null) {
                    break;
                }
            }
            if (rcSetting == null) {
                return;
            }
        }

        List<IComponent> neededComponents = RepositoryComponentManager.filterNeededComponents(item, store.seletetedNode, type);

        // for esb
        for (IDragAndDropServiceHandler handler : DragAndDropManager.getHandlers()) {
            List<IComponent> comList = handler.filterNeededComponents(item, store.seletetedNode, type);
            if (comList != null) {
                for (IComponent handlerComp : comList) {
                    if (!neededComponents.contains(handlerComp)) {
                        neededComponents.add(handlerComp);
                    }
                }
            }
        }
        IComponent component = chooseOneComponent(neededComponents, rcSetting, quickCreateInput, quickCreateOutput);
        store.component = component;
        store.componentName = rcSetting;
    }

    /**
     * Let the user choose which component he would like to create.
     * 
     * @param neededComponents
     * @param name
     * @param quickCreateInput
     * @param quickCreateOutput
     */
    private IComponent chooseOneComponent(List<IComponent> neededComponents, IComponentName name, boolean quickCreateInput,
            boolean quickCreateOutput) {
        if (neededComponents.isEmpty()) {
            return null;
        }
        if (neededComponents.size() == 1) {
            return neededComponents.get(0);
        }

        IComponent inputComponent = getComponentByName(name.getInputComponentName(), quickCreateInput, neededComponents);
        if (inputComponent != null) {
            return inputComponent;
        }
        IComponent outputComponent = getComponentByName(name.getOutPutComponentName(), quickCreateOutput, neededComponents);
        if (outputComponent != null) {
            return outputComponent;
        }

        ComponentChooseDialog dialog = new ComponentChooseDialog(editor.getSite().getShell(), neededComponents);
        IComponent defaultComponent = getComponentByName(name.getDefaultComponentName(), true, neededComponents);
        if (defaultComponent != null) {
            dialog.setInitialSelections(new Object[] { defaultComponent });
        } else {
            dialog.setInitialSelections(new Object[] { neededComponents.get(0) });
        }
        if (dialog.open() == IDialogConstants.OK_ID) {
            return dialog.getResultComponent();
        }

        throw new OperationCanceledException(Messages.getString("TalendEditorDropTargetListener.cancelOperation")); //$NON-NLS-1$
    }

    private IComponent getComponentByName(String name, boolean loop, List<IComponent> neededComponents) {
        if (loop) {
            for (IComponent component : neededComponents) {
                if (component.getName().equals(name)) {
                    return component;
                }
            }
        }
        return null;
    }

    private void execCommandStack(Command command) {
        CommandStack cs = editor.getCommandStack();
        if (cs != null) {
            cs.execute(command);
        } else {
            command.execute();
        }
    }

    private boolean createComponentOnLink(Node node, Point originalPoint) {
        boolean executed = false;

        RootEditPart rep = editor.getViewer().getRootEditPart().getRoot();

        Point viewOriginalPosition = new Point();
        if (rep instanceof ScalableFreeformRootEditPart) {
            ScalableFreeformRootEditPart root = (ScalableFreeformRootEditPart) rep;
            Viewport viewport = (Viewport) root.getFigure();
            viewOriginalPosition = viewport.getViewLocation();
        }
        Point point = new Point(originalPoint.x + viewOriginalPosition.x, originalPoint.y + viewOriginalPosition.y);

        org.talend.designer.core.ui.editor.connections.Connection targetConnection = CreateComponentOnLinkHelper
                .getSelectedConnection();
        for (Object child : editor.getProcessPart().getChildren()) {
            if (child instanceof SubjobContainerPart) {
                CreateComponentOnLinkHelper.unselectAllConnections((SubjobContainerPart) child);
            }
        }
        if (targetConnection != null) {
            NodeContainer nodeContainer = new NodeContainer(node);
            IProcess2 p = editor.getProcess();
            // TDI-21099
            if (p instanceof Process) {
                CreateNodeContainerCommand createCmd = new CreateNodeContainerCommand((Process) p, nodeContainer, point);
                execCommandStack(createCmd);
                // reconnect the node
                Node originalTarget = (Node) targetConnection.getTarget();
                INodeConnector targetConnector = node.getConnectorFromType(EConnectionType.FLOW_MAIN);
                for (INodeConnector connector : node.getConnectorsFromType(EConnectionType.FLOW_MAIN)) {
                    if (connector.getMaxLinkOutput() != 0) {
                        targetConnector = connector;
                        break;
                    }
                }
                ConnectionCreateCommand.setCreatingConnection(true);
                // FIXME perhaps, this is not good fix, need check it later
                // bug 21411
                if (PluginChecker.isJobLetPluginLoaded()) {
                    IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                            IJobletProviderService.class);
                    if (service != null && service.isJobletComponent(targetConnection.getTarget())) {
                        if (targetConnection.getTarget() instanceof Node) {
                            NodeContainer jobletContainer = ((Node) targetConnection.getTarget()).getNodeContainer();
                            // remove the old connection in the container
                            jobletContainer.getInputs().remove(targetConnection);
                        }
                    }
                }
                ConnectionReconnectCommand cmd2 = new ConnectionReconnectCommand(targetConnection);
                cmd2.setNewTarget(node);
                execCommandStack(cmd2);

                List<Object> nodeArgs = CreateComponentOnLinkHelper.getTargetArgs(targetConnection, node);
                ConnectionCreateCommand nodeCmd = new ConnectionCreateCommand(node, targetConnector.getName(), nodeArgs, false);
                nodeCmd.setTarget(originalTarget);
                execCommandStack(nodeCmd);
                if (node.getComponent().getName().equals("tMap")) {
                    CreateComponentOnLinkHelper.setupTMap(node);
                }
                if (originalTarget.getComponent().getName().equals("tMap")) {
                    CreateComponentOnLinkHelper
                            .updateTMap(originalTarget, targetConnection, node.getOutgoingConnections().get(0));
                }
                originalTarget.renameData(targetConnection.getName(), node.getOutgoingConnections().get(0).getName());
                executed = true;
            }
        }

        return executed;
    }

    private void updateConnectionCommand(org.talend.designer.core.ui.editor.connections.Connection connection, Node node,
            CompoundCommand command) {
        if (connection != null || node != null) {
            Node originalTarget = (Node) connection.getTarget();
            INodeConnector targetConnector = node.getConnectorFromType(EConnectionType.FLOW_MAIN);
            for (INodeConnector connector : node.getConnectorsFromType(EConnectionType.FLOW_MAIN)) {
                if (connector.getMaxLinkOutput() != 0) {
                    targetConnector = connector;
                    break;
                }
            }
            ConnectionCreateCommand.setCreatingConnection(true);
            EConnectionType reconnectNewInputStyle = connection.getLineStyle();
            if (ConnectionManager.canConnectToTarget(connection.getSource(), originalTarget, node, connection.getLineStyle(),
                    connection.getName(), targetConnector.getName())) {
                reconnectNewInputStyle = ConnectionManager.getNewConnectionType();
            }
            if (reconnectNewInputStyle.equals(EConnectionType.FLOW_MAIN)) {
                connection.reconnect(connection.getSource(), node, EConnectionType.FLOW_MAIN);
            } else if (reconnectNewInputStyle.equals(EConnectionType.FLOW_MERGE)) {
                connection.reconnect(connection.getSource(), node, EConnectionType.FLOW_MERGE);
            } else if (reconnectNewInputStyle.equals(EConnectionType.FLOW_REF)) {
                connection.reconnect(connection.getSource(), node, EConnectionType.FLOW_REF);
            }
            INodeConnector nodeConnector = node.getConnectorFromName(targetConnector.getName());
            nodeConnector.setCurLinkNbInput(nodeConnector.getCurLinkNbInput() + 1);
            List<Object> nodeArgs = CreateComponentOnLinkHelper.getTargetArgs(connection, node);
            ConnectionCreateCommand nodeCmd = new ConnectionCreateCommand(node, targetConnector.getName(), nodeArgs, false);
            nodeCmd.setTarget(originalTarget);
            INodeConnector originalNodeConnector = originalTarget.getConnectorFromName(connection.getTargetNodeConnector()
                    .getName());
            originalNodeConnector.setCurLinkNbInput(originalNodeConnector.getCurLinkNbInput() - 1);
            command.add(nodeCmd);
        }
    }

    /**
     * see issue 0002439.<br>
     * There are two types of Oracle.
     * 
     * @param name
     * @param node
     */
    private void processSpecificDBTypeIfSameProduct(IComponentName name, Node node) {
        // process "Oracle with service name"
        if (name == EDatabaseComponentName.DBORACLESN) {
            IElementParameter p = node.getElementParameter("CONNECTION_TYPE"); //$NON-NLS-1$
            // set value to "ORACLE_SERVICE_NAME"
            if (p != null) {
                // p.setValue(p.getListItemsValue()[1]);
                node.setPropertyValue("CONNECTION_TYPE", p.getListItemsValue()[1]);
            } else {
                node.setPropertyValue("CONNECTION_TYPE", "ORACLE_SERVICE_NAME");
            }
        } else if (name == EDatabaseComponentName.DBORACLEOCI) {
            IElementParameter p = node.getElementParameter("CONNECTION_TYPE"); //$NON-NLS-1$
            if (p != null) {
                Object[] obj = p.getListItemsValue();
                if (obj.length >= 2) {
                    p.setValue(p.getListItemsValue()[1]);
                }
            }
        }
    }

    public void translateAbsolateToRelative(IFigure owner, Translatable t) {
        owner.translateToRelative(t);

        Rectangle bounds = owner.getBounds();
        t.performTranslate(-bounds.x, -bounds.y);

    }

    /**
     * Sets the editor.
     * 
     * @param editor the editor to set
     */
    public void setEditor(AbstractTalendEditor editor) {
        this.editor = editor;
    }

    public EditPart getParentPart(EditPart part) {
        EditPart parent = part.getParent();
        if (!(parent instanceof ProcessPart)) {
            parent = getParentPart(parent);
        }
        return parent;
    }

    private boolean isLock(JobletContainerPart part) {
        INode jobletNode = ((JobletContainer) part.getModel()).getNode();
        if (PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            if (service != null) {
                return service.isLock(jobletNode);
            }
        }

        return false;
    }

    public AbstractMultiPageTalendEditor getJobletPart(JobletContainerPart part) {
        AbstractMultiPageTalendEditor openEditor = null;
        Node jobletNode = ((JobletContainer) part.getModel()).getNode();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            if (service != null) {
                openEditor = (AbstractMultiPageTalendEditor) service.openJobletEditor(jobletNode, page);
            }
        }
        return openEditor;
    }
}

/**
 * A dialog used to choose one component.
 */
class ComponentChooseDialog extends ListDialog {

    /**
     * bqian ComponentChooseDialog constructor comment.
     * 
     * @param parentShell
     */
    public ComponentChooseDialog(Shell parentShell, List<IComponent> input) {
        super(parentShell);
        setTitle(Messages.getString("TalendEditorDropTargetListener.title")); //$NON-NLS-1$
        setMessage(Messages.getString("TalendEditorDropTargetListener.chooseComponent")); //$NON-NLS-1$
        setInput(input);
        setContentProvider(new ArrayContentProvider());
        setLabelProvider(new LabelProvider() {

            @Override
            public Image getImage(Object element) {
                IComponent component = (IComponent) element;
                return CoreImageProvider.getComponentIcon(component, ICON_SIZE.ICON_16);
            }

            @Override
            public String getText(Object element) {
                IComponent component = (IComponent) element;
                return component.getName();
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
             */
            @Override
            public void dispose() {
                super.dispose();
            }
        });

    }

    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        //
        getTableViewer().getTable().showSelection();
        return control;
    }

    public IComponent getResultComponent() {
        return (IComponent) getResult()[0];
    }

}
