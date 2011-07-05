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
package org.talend.repository.ui.views;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.util.StringMatcher;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.TextActionHandler;
import org.eclipse.ui.commands.ActionHandler;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.dialogs.EventLoopProgressMonitor;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.actions.ITreeContextualAction;
import org.talend.commons.ui.swt.dialogs.ProgressDialog;
import org.talend.commons.ui.swt.tooltip.AbstractTreeTooltip;
import org.talend.commons.utils.Timer;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.IMigrationToolService;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.BusinessProcessItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobDocumentationItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.properties.User;
import org.talend.core.model.properties.impl.JobletDocumentationItemImpl;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryPrefConstants;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.LockInfo;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.model.update.EUpdateItemType;
import org.talend.core.model.update.IUpdateManager;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.IJobletProviderService;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.IRepositoryChangedListener;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryChangedEvent;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.ProjectRepositoryNode;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.actions.MoveObjectAction;
import org.talend.repository.plugin.integration.SwitchProjectAction;
import org.talend.repository.ui.actions.ActionsHelper;
import org.talend.repository.ui.actions.CopyAction;
import org.talend.repository.ui.actions.DeleteAction;
import org.talend.repository.ui.actions.PasteAction;
import org.talend.repository.ui.actions.RefreshAction;
import org.talend.repository.ui.actions.RepositoryDoubleClickAction;
import org.talend.repository.ui.actions.RepositoryFilterAction;

/**
 * 
 * View that presents all the content of the repository.<br/>
 * 
 * $Id: RepositoryView.java 62050 2011-06-10 03:17:43Z wchen $
 * 
 */
public class RepositoryView extends ViewPart implements IRepositoryView, ITabbedPropertySheetPageContributor,
        IRepositoryChangedListener, ISelectionListener {

    public final static String ID = "org.talend.repository.views.repository"; //$NON-NLS-1$

    private static final String PERSPECTIVE_DI_ID = "org.talend.rcp.perspective"; //$NON-NLS-1$

    private static final String SEPARATOR = ":";

    private static Logger log = Logger.getLogger(RepositoryView.class);

    protected TreeViewer viewer;

    private RepositoryContentProvider contentProvider = null;

    private static List<ISelectionChangedListener> listenersNeedTobeAddedIntoTreeviewer = new ArrayList<ISelectionChangedListener>();

    private static ProjectRepositoryNode root = new ProjectRepositoryNode(null, null, ENodeType.STABLE_SYSTEM_FOLDER);

    private IPreferenceStore preferenceStore = RepositoryManager.getPreferenceStore();

    private List<ITreeContextualAction> contextualsActions;

    private static boolean codeGenerationEngineInitialised;

    private Action doubleClickAction;

    private Action refreshAction;

    private RepositoryFilterAction repositoryFilterAction;

    private Listener dragDetectListener;

    private MenuManager rootMenu = null;

    private Label refreshBtn;

    private Label filterBtn;

    protected boolean isFromFake = true;

    // private boolean useFilter = false;

    public RepositoryView() {
    }

    /**
     * yzhang Comment method "addPreparedListeners".
     * 
     * @param listeners
     */
    public static void addPreparedListeners(ISelectionChangedListener listeners) {
        if (listeners != null) {
            listenersNeedTobeAddedIntoTreeviewer.add(listeners);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
     */
    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
        CorePlugin.getDefault().getRepositoryService().initializePluginMode();
        if (!codeGenerationEngineInitialised && !CorePlugin.getDefault().getRepositoryService().isRCPMode()) {

            if (!CorePlugin.getDefault().getLibrariesService().isLibSynchronized()) {
                CorePlugin.getDefault().getLibrariesService().syncLibraries();
            }
            codeGenerationEngineInitialised = true;
        }
        getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
    }

    public static IRepositoryView show() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page != null) {
            IViewPart part = page.findView(IRepositoryView.VIEW_ID);
            if (part == null) {
                try {
                    // MOD by zshen for 15750 todo 39 if the Perspective is DataProfilingPerspective refuse the
                    // RepositoryView be display by automatic
                    if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective().getId()
                            .equalsIgnoreCase("org.talend.dataprofiler.DataProfilingPerspective")) {//$NON-NLS-1$
                        part = ((WorkbenchPage) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage())
                                .getViewFactory().createView(IRepositoryView.VIEW_ID).getView(true);
                    } else {
                        // bug 16594
                        String perId = page.getPerspective().getId();
                        if ((!"".equals(perId) || null != perId) && perId.equalsIgnoreCase(PERSPECTIVE_DI_ID)) {
                            part = page.showView(IRepositoryView.VIEW_ID);
                        }
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
            return (IRepositoryView) part;
        }
        return null;
    }

    protected TreeViewer createTreeViewer(Composite parent) {
        return new RepositoryTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    }

    @Override
    public void createPartControl(Composite parent) {

        Composite comp = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.horizontalSpacing = 0;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        // layout.verticalSpacing = 0;
        comp.setLayout(layout);
        comp.setLayoutData(new GridData(GridData.FILL_BOTH));
        makeActions();
        createActionComposite(comp);

        viewer = createTreeViewer(comp);
        viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
        if (viewer instanceof ITreeViewerListener) {
            viewer.addTreeListener((ITreeViewerListener) viewer);
        }
        viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
        setContentProviderForView();
        viewer.setLabelProvider(new RepositoryLabelProvider(this));
        viewer.setSorter(new RepositoryNameSorter());
        IViewSite viewSite = getViewSite();
        viewer.setInput(viewSite);
        getSite().setSelectionProvider(viewer);
        addFilters();
        /* need to expand so that all folderItem will be created */
        viewer.expandAll();
        viewer.collapseAll();
        if (isFromFake) {
            refresh();
        }
        // This only tree listener aim is to change open/close icons on folders :
        viewer.addTreeListener(new ITreeViewerListener() {

            public void treeCollapsed(TreeExpansionEvent event) {
                RepositoryNode node = (RepositoryNode) event.getElement();
                if (node.getType().equals(ENodeType.SIMPLE_FOLDER)) {
                    TreeItem item = getObject(viewer.getTree(), event.getElement());
                    if (item != null) {
                        item.setImage(ImageProvider.getImage(ECoreImage.FOLDER_CLOSE_ICON));
                    }
                }
            }

            public void treeExpanded(TreeExpansionEvent event) {
                RepositoryNode node = (RepositoryNode) event.getElement();
                if (node.getType().equals(ENodeType.SIMPLE_FOLDER)) {
                    TreeItem item = getObject(viewer.getTree(), event.getElement());
                    if (item != null) {
                        item.setImage(ImageProvider.getImage(ECoreImage.FOLDER_OPEN_ICON));
                    }
                }
            }
        });
        createTreeTooltip(viewer.getTree());

        hookContextMenu();
        // contributeToActionBars();
        initDragAndDrop();
        hookDoubleClickAction();

        setPartName(Messages.getString("repository.title", ((RepositoryContext) CorePlugin.getContext().getProperty( //$NON-NLS-1$
                Context.REPOSITORY_CONTEXT_KEY)).getProject()));

        viewer.getTree().addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                log.trace("Repository gain focus"); //$NON-NLS-1$
                IContextService contextService = (IContextService) RepositoryPlugin.getDefault().getWorkbench()
                        .getAdapter(IContextService.class);
                ca = contextService.activateContext("talend.repository"); //$NON-NLS-1$
            }

            public void focusLost(FocusEvent e) {
                log.trace("Repository lost focus"); //$NON-NLS-1$
                if (ca != null) {
                    IContextService contextService = (IContextService) RepositoryPlugin.getDefault().getWorkbench()
                            .getAdapter(IContextService.class);
                    contextService.deactivateContext(ca);
                }
            }
        });

        if (listenersNeedTobeAddedIntoTreeviewer.size() > 0) {
            for (ISelectionChangedListener listener : listenersNeedTobeAddedIntoTreeviewer) {
                viewer.addSelectionChangedListener(listener);
            }
            listenersNeedTobeAddedIntoTreeviewer.clear();
        }

        CorePlugin.getDefault().getRepositoryService().registerRepositoryChangedListenerAsFirst(this);

        if (!CorePlugin.getDefault().getRepositoryService().isRCPMode()) {
            IMigrationToolService toolService = CorePlugin.getDefault().getMigrationToolService();
            toolService.executeMigration(SwitchProjectAction.PLUGIN_MODEL);

            IRunProcessService runService = CorePlugin.getDefault().getRunProcessService();
            runService.deleteAllJobs(SwitchProjectAction.PLUGIN_MODEL);

            final RepositoryContext repositoryContext = (RepositoryContext) CorePlugin.getContext().getProperty(
                    Context.REPOSITORY_CONTEXT_KEY);
            final Project project = repositoryContext.getProject();

            final IWorkbenchWindow activedWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            activedWorkbenchWindow.getPartService().addPartListener(new IPartListener() {

                public void partActivated(IWorkbenchPart part) {
                    if (part instanceof RepositoryView) {
                        String title = activedWorkbenchWindow.getShell().getText();
                        if (!title.contains("|")) { //$NON-NLS-1$
                            title += " | " + repositoryContext.getUser() + " | " + project.getLabel(); //$NON-NLS-1$ //$NON-NLS-2$
                            activedWorkbenchWindow.getShell().setText(title);
                        }
                    }
                }

                public void partBroughtToTop(IWorkbenchPart part) {

                }

                public void partClosed(IWorkbenchPart part) {

                }

                public void partDeactivated(IWorkbenchPart part) {

                }

                public void partOpened(IWorkbenchPart part) {
                    if (part instanceof RepositoryView) {
                        String title = activedWorkbenchWindow.getShell().getText();
                        if (!title.contains("|")) { //$NON-NLS-1$
                            title += " | " + repositoryContext.getUser() + " | " + project.getLabel(); //$NON-NLS-1$ //$NON-NLS-2$
                            activedWorkbenchWindow.getShell().setText(title);
                        }
                    }
                }

            });
        }
        if (root.getChildren().size() == 1) {
            viewer.setExpandedState(root.getChildren().get(0), true);
        }
    }

    protected void setContentProviderForView() {
        contentProvider = new RepositoryContentProvider(this);
        viewer.setContentProvider(contentProvider);
    }

    public void createActionComposite(Composite parent) {
        Composite toolbar = new Composite(parent, SWT.NONE);
        toolbar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolbar.setLayout(new FormLayout());
        refreshBtn = new Label(toolbar, SWT.NONE);
        refreshBtn.setImage(ImageProvider.getImage(EImage.REFRESH_ICON));
        refreshBtn.setToolTipText("refresh");

        filterBtn = new Label(toolbar, SWT.NONE);
        filterBtn.setToolTipText("Filters..." + "\n" + "Right click to set up");
        updateFilterImage();

        FormData thisFormData = new FormData();
        thisFormData.left = new FormAttachment(100, -20);
        // thisFormData.right = new FormAttachment(10, 30);
        thisFormData.top = new FormAttachment(0, 4);
        thisFormData.bottom = new FormAttachment(100, -2);

        filterBtn.setLayoutData(thisFormData);

        thisFormData = new FormData();
        thisFormData.right = new FormAttachment(filterBtn, -2);
        thisFormData.top = new FormAttachment(0, 4);

        refreshBtn.setLayoutData(thisFormData);
        addListeners();
    }

    private void addListeners() {
        refreshBtn.addMouseTrackListener(new RepositoryMouseTrackListener());
        refreshBtn.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseUp(MouseEvent e) {
                if (refreshAction != null) {
                    refreshAction.run();
                }
            }
        });

        filterBtn.addMouseTrackListener(new RepositoryMouseTrackListener());
        filterBtn.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseUp(MouseEvent e) {
                // right click
                if (repositoryFilterAction != null) {
                    if (e.button == 3) {
                        repositoryFilterAction.openSetupDialog();
                    } else {
                        boolean useFilter = preferenceStore.getBoolean(IRepositoryPrefConstants.USE_FILTER);
                        preferenceStore.setValue(IRepositoryPrefConstants.USE_FILTER, !useFilter);
                        repositoryFilterAction.run();
                        updateFilterImage();
                    }
                }

            }

        });
    }

    private void updateFilterImage() {
        if (preferenceStore.getBoolean(IRepositoryPrefConstants.USE_FILTER)) {
            filterBtn.setImage(ImageProvider.getImage(EImage.FILTER_ACTIVED_ICON));
        } else {
            filterBtn.setImage(ImageProvider.getImage(EImage.FILTER_DEACTIVED_ICON));
        }
    }

    public void addFilters() {
        // filter by node : filter stable talend elements
        viewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (!preferenceStore.getBoolean(IRepositoryPrefConstants.USE_FILTER)) {
                    return true;
                }
                String[] uncheckedNodesFromFilter = RepositoryManager
                        .getFiltersByPreferenceKey(IRepositoryPrefConstants.FILTER_BY_NODE);

                RepositoryNode node = (RepositoryNode) element;
                ERepositoryObjectType contentType = node.getContentType();
                if (uncheckedNodesFromFilter == null || contentType == null || node.isBin()) {
                    return true;
                }
                String technicalLabel = node.getRoot().getProject().getEmfProject().getTechnicalLabel();
                String uniqueSymbol = technicalLabel + SEPARATOR;
                // sql patterns like Generic ,Mysql
                if (contentType != null && ERepositoryObjectType.SQLPATTERNS.equals(contentType) && node.getId() != "-1") {
                    uniqueSymbol = uniqueSymbol + contentType.name() + SEPARATOR + node.getProperties(EProperties.LABEL);
                } else {
                    uniqueSymbol = uniqueSymbol + contentType.name();
                    if (node instanceof ProjectRepositoryNode) {
                        uniqueSymbol = uniqueSymbol + SEPARATOR + "ROOT";//$NON-NLS-1$
                    }

                }
                List<String> filters = Arrays.asList(uncheckedNodesFromFilter);
                if (filters.contains(uniqueSymbol)) {
                    return false;
                }
                return true;
            }

        });

        // filter by status and users: filter user created nodes REPOSITORY_ELEMENT
        viewer.addFilter(new ViewerFilter() {

            private StringMatcher[] matchers;

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {

                if (!preferenceStore.getBoolean(IRepositoryPrefConstants.USE_FILTER)) {
                    return true;
                }

                boolean visible = true;
                RepositoryNode node = (RepositoryNode) element;
                if (ENodeType.REPOSITORY_ELEMENT.equals(node.getType()) || ENodeType.SIMPLE_FOLDER.equals(node.getType())) {
                    visible = filterByUserStatusName(node);

                }

                return visible;
            }

            private boolean filterByUserStatusName(RepositoryNode node) {
                String[] statusFilter = RepositoryManager.getFiltersByPreferenceKey(IRepositoryPrefConstants.FILTER_BY_STATUS);
                String[] userFilter = RepositoryManager.getFiltersByPreferenceKey(IRepositoryPrefConstants.FILTER_BY_USER);
                boolean enableNameFilter = RepositoryManager.getPreferenceStore().getBoolean(
                        IRepositoryPrefConstants.TAG_USER_DEFINED_PATTERNS_ENABLED);
                if (statusFilter == null && userFilter == null && !enableNameFilter) {
                    return true;
                }

                boolean visible = true;

                if (ENodeType.SIMPLE_FOLDER.equals(node.getType())) {
                    visible = isStableItem(node);
                    if (visible) {
                        return true;
                    }
                    for (IRepositoryNode childNode : node.getChildren()) {
                        visible = visible || filterByUserStatusName((RepositoryNode) childNode);
                        if (visible) {
                            return true;
                        }
                    }
                    return visible;
                }

                List items = new ArrayList();
                if (statusFilter != null && statusFilter.length > 0) {
                    items.addAll(Arrays.asList(statusFilter));
                }
                if (userFilter != null && userFilter.length > 0) {
                    items.addAll(Arrays.asList(userFilter));
                }
                if (node.getObject() != null) {
                    Property property = node.getObject().getProperty();
                    String statusCode = "";
                    if (property != null) {
                        statusCode = property.getStatusCode();
                    }
                    User author = node.getObject().getAuthor();
                    String user = "";
                    if (author != null) {
                        user = author.getLogin();
                    }
                    if ((items.contains(statusCode) || items.contains(user)) && !isStableItem(node)) {
                        visible = false;
                    } else if (items.contains(RepositoryConstants.NOT_SET_STATUS)
                            && (statusCode == null || "".equals(statusCode))) {
                        visible = false;
                        if (property != null) {
                            Item item = property.getItem();
                            if (item instanceof RoutineItem && ((RoutineItem) item).isBuiltIn() || item instanceof SQLPatternItem
                                    && ((SQLPatternItem) item).isSystem() || item instanceof BusinessProcessItem
                                    || item instanceof JobDocumentationItem || item instanceof JobletDocumentationItemImpl) {
                                visible = true;
                            }
                        }

                    }

                }

                // filter by name
                String label = (String) node.getProperties(EProperties.LABEL);
                if (visible && isMatchNameFilterPattern(label)) {
                    visible = true;
                } else {
                    if (enableNameFilter && !isStableItem(node)) {
                        visible = false;
                    }
                }

                return visible;
            }

            private boolean isMatchNameFilterPattern(String label) {
                boolean enable = RepositoryManager.getPreferenceStore().getBoolean(
                        IRepositoryPrefConstants.TAG_USER_DEFINED_PATTERNS_ENABLED);
                if (!enable) {
                    return false;
                }
                if (label != null && label.length() > 0) {
                    StringMatcher[] testMatchers = getMatchers();
                    if (testMatchers != null) {
                        for (int i = 0; i < testMatchers.length; i++) {
                            if (testMatchers[i].match(label))
                                return true;
                        }
                    }
                }
                return false;
            }

            private StringMatcher[] getMatchers() {
                String userFilterPattern = RepositoryManager.getPreferenceStore().getString(
                        IRepositoryPrefConstants.FILTER_BY_NAME);
                String[] newPatterns = null;
                if (userFilterPattern != null && !"".equals(userFilterPattern)) {
                    newPatterns = RepositoryManager.convertFromString(userFilterPattern, RepositoryManager.PATTERNS_SEPARATOR);
                }
                if (newPatterns != null) {
                    matchers = new StringMatcher[newPatterns.length];
                    for (int i = 0; i < newPatterns.length; i++) {
                        matchers[i] = new StringMatcher(newPatterns[i], true, false);
                    }
                }
                return matchers;
            }

            private boolean isStableItem(RepositoryNode node) {
                Object label = node.getProperties(EProperties.LABEL);
                if (ENodeType.SIMPLE_FOLDER.equals(node.getType())
                        && ERepositoryObjectType.SQLPATTERNS.equals(node.getContentType())
                        && (label.equals("Generic") || label.equals("UserDefined") || label.equals("MySQL")
                                || label.equals("Netezza") || label.equals("Oracle") || label.equals("ParAccel") || label
                                .equals("Teradata")) || label.equals("Hive")) {
                    return true;

                } else if (ENodeType.REPOSITORY_ELEMENT.equals(node.getType()) && node.getObject() != null) {
                    Item item = node.getObject().getProperty().getItem();
                    if (item instanceof SQLPatternItem) {
                        if (((SQLPatternItem) item).isSystem()) {
                            return true;
                        }
                    } else if (item instanceof RoutineItem) {
                        if (((RoutineItem) item).isBuiltIn()) {
                            return true;
                        }
                    }
                }
                return false;

            }

            private boolean hasVisibleChildren(RepositoryNode node) {
                boolean hasVisibleChildren = false;
                List<IRepositoryNode> children = node.getChildren();
                for (IRepositoryNode childNode : children) {
                    hasVisibleChildren = hasVisibleChildren || filterByUserStatusName((RepositoryNode) childNode)
                            || hasVisibleChildren((RepositoryNode) childNode);
                    if (hasVisibleChildren) {
                        return hasVisibleChildren;
                    }
                }
                return hasVisibleChildren;
            }
        });

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
        super.dispose();
    }

    /**
     * DOC bqian Comment method "createTreeTooltip".
     * 
     * @param tree
     */
    private void createTreeTooltip(Tree tree) {
        AbstractTreeTooltip tooltip = new AbstractTreeTooltip(tree) {

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.talend.commons.ui.swt.tooltip.AbstractTreeTooltip#getTooltipContent(org.eclipse.swt.widgets.TreeItem)
             */
            @Override
            public String getTooltipContent(TreeItem item) {

                RepositoryNode node = (RepositoryNode) item.getData();
                IRepositoryViewObject object = node.getObject();
                if (object == null) {
                    return null;
                }
                // add for feature 10281
                String content = null;
                User currentLoginUser = ((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                        .getUser();
                String currentLogin = null;
                if (currentLoginUser != null) {
                    currentLogin = currentLoginUser.getLogin();
                }
                String login = null;
                String application = "studio";//$NON-NLS-1$
                if (object.getRepositoryStatus() == ERepositoryStatus.LOCK_BY_OTHER) {
                    Property property = object.getProperty();
                    if (property != null) {
                        Item item2 = property.getItem();
                        if (item2 != null) {
                            LockInfo lockInfo = ProxyRepositoryFactory.getInstance().getLockInfo(item2);
                            if (!lockInfo.getUser().equals(currentLogin)) {
                                login = lockInfo.getUser();
                                application = lockInfo.getApplication();
                            }
                        }
                    }
                    if (login != null && !"".equals(login)) {//$NON-NLS-1$
                        content = "  locked by " + login + " on " + application;
                    }
                }
                String description = object.getDescription();
                if (content == null || "".equals(content)) { //$NON-NLS-1$
                    if (description == null || "".equals(description)) {//$NON-NLS-1$
                        return null;
                    }
                    return description;
                } else {
                    if (description == null || "".equals(description)) {//$NON-NLS-1$
                        return content;
                    }
                    return content + "\n" + "  Description: " + description;//$NON-NLS-1$//$NON-NLS-1$
                }
            }
        };

    }

    IContextActivation ca;

    private TreeItem getObject(Tree tree, Object objectToFind) {
        for (TreeItem item : tree.getItems()) {
            TreeItem toReturn = getObject(item, objectToFind);
            if (toReturn != null) {
                return toReturn;
            }
        }
        return null;
    }

    private TreeItem getObject(TreeItem parent, Object objectToFind) {
        for (TreeItem currentChild : parent.getItems()) {
            if (objectToFind.equals(currentChild.getData())) {
                return currentChild;
            }
            TreeItem toReturn = getObject(currentChild, objectToFind);
            if (toReturn != null) {
                return toReturn;
            }
        }
        return null;
    }

    protected void initDragAndDrop() {
        int ops = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
        Transfer[] transfers = new Transfer[] { LocalSelectionTransfer.getTransfer() };

        viewer.addDragSupport(ops, transfers, new DragSourceAdapter() {

            private static final long FFFFFFFFL = 0xFFFFFFFFL;

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.dnd.DragSourceAdapter#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
             */
            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = LocalSelectionTransfer.getTransfer().getSelection();
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.dnd.DragSourceAdapter#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
             */
            @Override
            public void dragStart(DragSourceEvent event) {
                ISelection selection = viewer.getSelection();

                for (Object obj : ((StructuredSelection) selection).toArray()) {
                    RepositoryNode sourceNode = (RepositoryNode) obj;

                    // As i don't know how to get event operation i test on MoveOperation
                    event.doit = MoveObjectAction.getInstance().validateAction(sourceNode, null, true);
                }

                LocalSelectionTransfer.getTransfer().setSelection(selection);
                LocalSelectionTransfer.getTransfer().setSelectionSetTime(event.time & FFFFFFFFL);
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.dnd.DragSourceAdapter#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
             */
            @Override
            public void dragFinished(DragSourceEvent event) {
                RepositoryView.this.dragFinished();
            }
        });
        RepositoryDropAdapter adapter = new RepositoryDropAdapter(viewer);
        adapter.setFeedbackEnabled(false);
        viewer.addDropSupport(ops | DND.DROP_DEFAULT, transfers, adapter);
        dragDetectListener = new Listener() {

            public void handleEvent(Event event) {
                // dragDetected = true;
            }
        };
        viewer.getControl().addListener(SWT.DragDetect, dragDetectListener);
    }

    public void dragFinished() {
        refresh();
        LocalSelectionTransfer.getTransfer().setSelection(null);
        LocalSelectionTransfer.getTransfer().setSelectionSetTime(0);
    }

    protected void makeActions() {
        IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);

        refreshAction = new RefreshAction(this);
        IHandler handler1 = new ActionHandler(refreshAction);
        handlerService.activateHandler(refreshAction.getActionDefinitionId(), handler1);

        repositoryFilterAction = new RepositoryFilterAction(this);

        contextualsActions = ActionsHelper.getRepositoryContextualsActions();
        for (ITreeContextualAction action : contextualsActions) {
            action.setWorkbenchPart(this);
            if (action.getActionDefinitionId() != null) {
                handler1 = new ActionHandler(action);
                handlerService.activateHandler(action.getActionDefinitionId(), handler1);
            }
        }
        doubleClickAction = new RepositoryDoubleClickAction(this, contextualsActions);

        TextActionHandler textActionHandler = new TextActionHandler(getViewSite().getActionBars());
        textActionHandler.setCopyAction(CopyAction.getInstance());
        textActionHandler.setPasteAction(PasteAction.getInstance());
        textActionHandler.setDeleteAction(DeleteAction.getInstance());

        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), CopyAction.getInstance());
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.PASTE.getId(), PasteAction.getInstance());
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.DELETE.getId(), DeleteAction.getInstance());
    }

    protected void hookDoubleClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
    }

    protected void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);

        menuMgr.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                RepositoryView.this.fillContextMenu(manager);
            }
        });

        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    protected void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {
        // Project project = ProjectManager.getInstance().getCurrentProject();
        //
        // IConfigurationElement[] elems = Platform.getExtensionRegistry().getConfigurationElementsFor(
        // "org.talend.repository.repository_menu_provider");
        // for (IConfigurationElement elem : elems) {
        // RepositoryMenuAction createExecutableExtension;
        // try {
        // createExecutableExtension = (RepositoryMenuAction) elem.createExecutableExtension("class");
        // if (project.isLocal() && !(createExecutableExtension instanceof RepositoryFilterAction)) {
        // continue;
        // }
        // createExecutableExtension.initialize(this);
        // manager.add(createExecutableExtension);
        // } catch (CoreException e) {
        // ExceptionHandler.process(e);
        // }
        // }
    }

    private void fillContextMenu(IMenuManager manager) {
        IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();

        final MenuManager[] menuManagerGroups = org.talend.core.ui.actions.ActionsHelper.getRepositoryContextualsActionGroups();
        // find group
        Set<String> processedGroupIds = new HashSet<String>();
        for (ITreeContextualAction action : contextualsActions) {
            action.init(getViewer(), sel);
            if (action.isVisible() && action.isEnabled()) {
                IMenuManager groupMenu = findMenuManager(menuManagerGroups, action.getGroupId(), true); // find root
                if (groupMenu != null) { // existed
                    final String rootId = groupMenu.getId();
                    if (!processedGroupIds.contains(rootId)) {
                        manager.add(groupMenu);
                        processedGroupIds.add(rootId);
                    }
                }
                groupMenu = findMenuManager(menuManagerGroups, action.getGroupId(), false); // find last child
                if (groupMenu != null) { // existed
                    groupMenu.add(action);
                } else { // child
                    manager.add(action);
                }
            }
        }
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private MenuManager findMenuManager(final MenuManager[] menuManagerGroups, String groupId, boolean findParent) {
        for (MenuManager groupMenu : menuManagerGroups) {
            if (groupMenu.getId().equals(groupId)) {
                if (findParent) {
                    final MenuManager parent = (MenuManager) groupMenu.getParent();
                    if (parent == null) {
                        return groupMenu;
                    } else {
                        return findMenuManager(menuManagerGroups, parent.getId(), findParent);
                    }
                } else {
                    return groupMenu;
                }
            }
        }
        return null;
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(refreshAction);
        manager.add(repositoryFilterAction);
    }

    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.repository.views.IRepositoryView#getViewer()
     */
    public TreeViewer getViewer() {
        return viewer;
    }

    public void refresh() {
        this.refresh(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.repository.views.IRepositoryView#refresh()
     */
    public void refresh(final boolean needInitialize) {
        /*
         * fix bug 4040. Sometimes Display.getCurrent.getActiveShell() get null result we not expect.
         */
        // Shell shell = Display.getCurrent().getActiveShell();
        Shell shell = getSite().getShell();

        if (shell == null) {
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(shell, 1000) {

            private IProgressMonitor monitorWrap;

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                Timer timer = Timer.getTimer("repositoryView"); //$NON-NLS-1$
                timer.start();
                if (needInitialize) {
                    monitorWrap = new EventLoopProgressMonitor(monitor);
                    try {
                        final ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                        factory.initialize();
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
                root = new ProjectRepositoryNode(null, null, ENodeType.STABLE_SYSTEM_FOLDER);
                viewer.refresh();

                // unsetting the selection will prevent the propertyView from displaying dirty data
                viewer.setSelection(new TreeSelection());

                if (root.getChildren().size() == 1) {
                    viewer.setExpandedState(root.getChildren().get(0), true);
                }
                timer.stop();
                // timer.print();
            }
        };

        try {
            progressDialog.executeProcess();
        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
            return;
        } catch (Exception e) {
            MessageBoxExceptionHandler.process(e);
            return;
        }
    }

    public void refreshView() {
        /*
         * fix bug 4040. Sometimes Display.getCurrent.getActiveShell() get null result we not expect.
         */
        // Shell shell = Display.getCurrent().getActiveShell();
        Shell shell = getSite().getShell();

        if (shell == null) {
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(shell, 1000) {

            private IProgressMonitor monitorWrap;

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                Timer timer = Timer.getTimer("repositoryView"); //$NON-NLS-1$
                timer.start();
                monitorWrap = new EventLoopProgressMonitor(monitor);
                try {
                    final ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                    factory.initialize();
                    final Project currentProject = ProjectManager.getInstance().getCurrentProject();
                    // factory.reloadProject(currentProject);
                } catch (Exception e) {
                    throw new InvocationTargetException(e);
                }

                root = new ProjectRepositoryNode(null, null, ENodeType.STABLE_SYSTEM_FOLDER);
                viewer.refresh();

                // unsetting the selection will prevent the propertyView from displaying dirty data
                viewer.setSelection(new TreeSelection());

                if (root.getChildren().size() == 1) {
                    viewer.setExpandedState(root.getChildren().get(0), true);
                }
                // PTODO
                if (PluginChecker.isJobLetPluginLoaded()) {
                    IJobletProviderService jobletService = (IJobletProviderService) GlobalServiceRegister.getDefault()
                            .getService(IJobletProviderService.class);
                    if (jobletService != null) {
                        jobletService.loadComponentsFromProviders();
                    }
                }
                timer.stop();
                // timer.print();
            }
        };

        try {
            progressDialog.executeProcess();
        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
            return;
        } catch (Exception e) {
            MessageBoxExceptionHandler.process(e);
            return;
        }
        List<IProcess2> openedProcessList = CorePlugin.getDefault().getDesignerCoreService()
                .getOpenedProcess(RepositoryUpdateManager.getEditors());
        List<UpdateResult> updateAllResults = new ArrayList<UpdateResult>();
        IUpdateManager manager = null;
        for (IProcess2 proc : openedProcessList) {
            // UpdateJobletUtils.updateRelationship();
            manager = proc.getUpdateManager();
            List<UpdateResult> updateResults = manager.getUpdatesNeeded(EUpdateItemType.RELOAD);
            updateAllResults.addAll(updateResults);
        }
        if (manager != null)
            manager.executeUpdates(updateAllResults);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.views.IRepositoryView#refresh(java.lang.Object)
     */
    public void refresh(Object object) {

        if (object != null) {
            // maybe, no effect.
            viewer.refresh(object);
            viewer.setExpandedState(object, true);
            if (object instanceof RepositoryNode) {
                RepositoryNode node = (RepositoryNode) object;
                ERepositoryObjectType type = node.getObjectType();
                if (type == null) {
                    type = node.getContentType();
                }
                refresh(type);
            }
        } else {
            // refresh();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.repository.ui.views.IRepositoryView#refresh(org.talend.core.model.repository.ERepositoryObjectType)
     */
    public void refresh(ERepositoryObjectType type) {
        if (type != null && !type.isSubItem()) {
            RepositoryNode rootNode = researchRootRepositoryNode(type);
            refreshAllChildNodes(rootNode);
        } else if (type != null
                && (type == ERepositoryObjectType.METADATA_SAP_FUNCTION || type == ERepositoryObjectType.METADATA_SAP_IDOC)) {
            RepositoryNode rootNode = researchRootRepositoryNode(type);
            refreshAllChildNodes(rootNode);
        }
    }

    private RepositoryNode researchRootRepositoryNode(ERepositoryObjectType type) {

        if (type != null && getRoot() instanceof ProjectRepositoryNode) {
            ProjectRepositoryNode pRoot = (ProjectRepositoryNode) getRoot();
            return pRoot.getRootRepositoryNode(type);

        }
        return null;
    }

    /**
     * only refresh the child of root node.
     */
    public void refreshAllChildNodes(RepositoryNode rootNode) {
        if (rootNode != null) {
            rootNode.setInitialized(false);
            if (!rootNode.getContentType().equals(ERepositoryObjectType.METADATA))
                rootNode.getChildren().clear();
            // for bug 11786
            if (rootNode.getParent() instanceof ProjectRepositoryNode) {
                ((ProjectRepositoryNode) rootNode.getParent()).clearNodeAndProjectCash();
            }
            // refresh content of recyclebin
            contentProvider.getChildren(rootNode); // retrieve child
            viewer.refresh(rootNode);
            // user A and B in svn,if user A delete some jobs,B create job,the recyle bin can't refresh,
            // so need refresh recyle bin. if empty recyle bin,must delete job documents,don't need refresh recyle bin.
            // if refresh throw exception.
            if (!rootNode.getContentType().equals(ERepositoryObjectType.DOCUMENTATION)) {
                root.getRecBinNode().setInitialized(false);
                root.getRecBinNode().getChildren().clear();
                contentProvider.getChildren(root.getRecBinNode());
                viewer.refresh(root.getRecBinNode());
            }
        }
    }

    public void expand(Object object) {
        if (object == null) {
            return;
        }
        boolean state = getExpandedState(object);
        expand(object, !state);
    }

    public boolean getExpandedState(Object object) {
        if (object == null) {
            return false;
        }
        boolean state = viewer.getExpandedState(object);
        return state;
    }

    public void expand(Object object, boolean state) {
        if (object == null) {
            return;
        }
        viewer.setExpandedState(object, state);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.repository.views.IRepositoryView#getSystemFolders()
     */
    public RepositoryNode getRoot() {
        return root;
    }

    public List<IRepositoryViewObject> getAll(ERepositoryObjectType type) {
        // find the system folder
        RepositoryNode container = findContainer(root, type);

        if (container == null) {
            throw new IllegalArgumentException(type + Messages.getString("RepositoryView.notfound")); //$NON-NLS-1$
        }

        List<IRepositoryViewObject> result = new ArrayList<IRepositoryViewObject>();
        addElement(result, type, container);
        addElement(result, type, root.getRecBinNode());
        return result;
    }

    // see RepositoryContentProvider.initialize();
    private RepositoryNode findContainer(RepositoryNode node, ERepositoryObjectType type) {
        if (node.getType() == ENodeType.SYSTEM_FOLDER || node.getType() == ENodeType.STABLE_SYSTEM_FOLDER) {
            if (type == node.getProperties(EProperties.CONTENT_TYPE)) {
                return node;
            }
            for (IRepositoryNode repositoryNode : node.getChildren()) {

                RepositoryNode result = findContainer((RepositoryNode) repositoryNode, type);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;

    }

    private void addElement(List<IRepositoryViewObject> result, ERepositoryObjectType type, RepositoryNode node) {
        if (node.getType() == ENodeType.REPOSITORY_ELEMENT && node.getProperties(EProperties.CONTENT_TYPE) == type) {
            result.add(node.getObject());
        } else {
            for (IRepositoryNode child : node.getChildren()) {
                addElement(result, type, (RepositoryNode) child);
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor#getContributorId()
     */
    public String getContributorId() {
        return getSite().getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySheetPage.class) {
            return new TabbedPropertySheetPage(this);
        }
        return super.getAdapter(adapter);
    }

    public void repositoryChanged(RepositoryChangedEvent event) {
        refresh();
    }

    public String[] gatherMetadataChildenLabels() {
        return contentProvider.gatherMetdataChildrens();
    }

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (part == this && selection instanceof TreeSelection) {
            CopyAction.getInstance().init(getViewer(), (IStructuredSelection) selection);
            PasteAction.getInstance().init(getViewer(), (IStructuredSelection) selection);
            DeleteAction.getInstance().init(getViewer(), (IStructuredSelection) selection);
        }
    }

    public Action getDoubleClickAction() {
        return this.doubleClickAction;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.talend.repository.ui.views.IRepositoryView#containsRepositoryType(org.talend.core.model.repository.
     * ERepositoryObjectType)
     */
    public boolean containsRepositoryType(ERepositoryObjectType type) {
        return researchRootRepositoryNode(type) != null;
    }

    /**
     * 
     * 
     * 
     * $Id: RepositoryView.java 62050 2011-06-10 03:17:43Z wchen $
     * 
     */
    class RepositoryMouseTrackListener implements MouseTrackListener {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseEnter(MouseEvent e) {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseExit(MouseEvent e) {
            if (e.getSource() instanceof Label) {
                Label label = (Label) e.getSource();
                if (label == refreshBtn) {
                    refreshBtn.setImage(ImageProvider.getImage(EImage.REFRESH_ICON));
                    refreshBtn.pack();
                } else if (label == filterBtn) {
                    updateFilterImage();
                    filterBtn.pack();
                }
            }

        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseHover(MouseEvent e) {
            if (e.getSource() instanceof Label) {
                Label label = (Label) e.getSource();
                if (label == refreshBtn) {
                    refreshBtn.setImage(ImageProvider.getImage(EImage.REFRESH_WITH_BGCOLOR_ICON));
                    refreshBtn.setSize(new Point(19, 19));
                } else if (label == filterBtn) {
                    filterBtn.setSize(new Point(19, 19));
                    updateFilterImage();
                }
            }

        }

    }

    public boolean isFromFake() {
        return this.isFromFake;
    }

    public void setFromFake(boolean isFromFake) {
        this.isFromFake = isFromFake;
    }
}
