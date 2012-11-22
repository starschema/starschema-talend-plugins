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
package org.talend.designer.core.ui.views.jobsettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.business.BusinessType;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.EmptyRepositoryObject;
import org.talend.core.model.repository.IRepositoryEditorInput;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.properties.tab.HorizontalTabFactory;
import org.talend.core.properties.tab.IDynamicProperty;
import org.talend.core.properties.tab.TalendPropertyTabDescriptor;
import org.talend.core.ui.IHeaderFooterProviderService;
import org.talend.core.ui.ISVNProviderService;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.designer.business.diagram.custom.IDiagramModelService;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.views.jobsettings.tabs.HeaderFooterComposite;
import org.talend.designer.core.ui.views.jobsettings.tabs.MainComposite;
import org.talend.designer.core.ui.views.jobsettings.tabs.ProcessVersionComposite;
import org.talend.designer.core.ui.views.properties.MultipleThreadDynamicComposite;
import org.talend.designer.core.ui.views.statsandlogs.StatsAndLogsComposite;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.views.IJobSettingsView;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class JobSettingsView extends ViewPart implements IJobSettingsView, ISelectionChangedListener {

    /**
     * 
     */
    private static final String SEPARATOR = "->"; //$NON-NLS-1$

    public static final String VIEW_NAME_JOBLET = "Joblet"; //$NON-NLS-1$

    public static final String MODEL = "Model"; //$NON-NLS-1$

    private HorizontalTabFactory tabFactory = null;

    private TalendPropertyTabDescriptor currentSelectedTab;

    private Element element;

    private boolean cleaned;

    private boolean selectedPrimary;

    private boolean allowVerchange = true;

    private Process process;

    private Composite parent;

    private ISelection selectedModel;

    public JobSettingsView() {
        tabFactory = new HorizontalTabFactory();
        CorePlugin.getDefault().getRepositoryService().addRepositoryTreeViewListener(this);
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        allowVerchange = brandingService.getBrandingConfiguration().isAllowChengeVersion();
    }

    public static String getViewNameLable() {
        return Messages.getString("JobSettingsView.JobSettings"); //$NON-NLS-1$
    }

    @Override
    public void createPartControl(Composite parent) {
        // tabFactory = new HorizontalTabFactory();
        this.parent = parent;
        tabFactory.initComposite(parent, false);
        tabFactory.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                TalendPropertyTabDescriptor descriptor = (TalendPropertyTabDescriptor) selection.getFirstElement();

                if (descriptor == null) {
                    return;
                }

                if (currentSelectedTab != null) {
                    if ((!currentSelectedTab.getData().equals(descriptor.getData())
                            || currentSelectedTab.getData() != descriptor.getData() || currentSelectedTab.getCategory() != descriptor
                            .getCategory())) {
                        for (Control curControl : tabFactory.getTabComposite().getChildren()) {
                            curControl.dispose();
                        }
                    }
                }

                if (element == null || !element.equals(descriptor.getData()) || currentSelectedTab == null
                        || currentSelectedTab.getCategory() != descriptor.getCategory() || selectedPrimary) {
                    Object data = descriptor.getData();
                    if (data instanceof Element) {
                        element = (Element) data;
                        currentSelectedTab = descriptor;
                        IDynamicProperty propertyComposite = createTabComposite(tabFactory.getTabComposite(), element,
                                descriptor.getCategory());

                    } else if (data instanceof IRepositoryViewObject) {

                        currentSelectedTab = descriptor;
                        IDynamicProperty propertyComposite = createTabComposite(tabFactory.getTabComposite(), data,
                                descriptor.getCategory());

                    } else if (data instanceof IEditorPart) {
                        currentSelectedTab = descriptor;
                        IRepositoryViewObject repObj = retrieveBusiness((IEditorPart) data);
                        if (repObj != null) {
                            IDynamicProperty propertyComposite = createTabComposite(tabFactory.getTabComposite(), repObj,
                                    descriptor.getCategory());
                        }

                    } else {
                        currentSelectedTab = descriptor;
                        IDynamicProperty propertyComposite = createTabComposite(tabFactory.getTabComposite(), null,
                                descriptor.getCategory());
                    }
                    selectedPrimary = false;
                }

            }
        });

    }

    private IRepositoryViewObject retrieveBusiness(IEditorPart businessPart) {
        if (CorePlugin.getDefault().getDiagramModelService().isBusinessDiagramEditor(businessPart)) {
            IRepositoryViewObject lastVersion = null;
            selectedModel = CorePlugin
                    .getDefault()
                    .getDiagramModelService()
                    .getBusinessEditorSelection(
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor());

            try {

                IRepositoryEditorInput input = CorePlugin.getDefault().getDiagramModelService()
                        .getBusinessDiagramEditorInput(businessPart);

                if (input != null) {
                    RepositoryNode node = input.getRepositoryNode();
                    if (node != null) {
                        lastVersion = node.getObject();
                    } else {
                        lastVersion = CorePlugin.getDefault().getProxyRepositoryFactory()
                                .getLastVersion(input.getItem().getProperty().getId());
                    }
                }
                return lastVersion;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        return null;
    }

    private IDynamicProperty createTabComposite(Composite parent, Object data, EComponentCategory category) {
        final int style = SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS;
        IDynamicProperty dynamicComposite = null;

        ISVNProviderService service = null;
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            service = (ISVNProviderService) GlobalServiceRegister.getDefault().getService(ISVNProviderService.class);
        }

        if (EComponentCategory.EXTRA.equals(category)) {
            // achen modify to fix bug 0006241
            Process process = getElement();
            boolean isJoblet = AbstractProcessProvider.isExtensionProcessForJoblet(process);
            if (isJoblet) {
                dynamicComposite = new MultipleThreadDynamicComposite(parent, style, category, (Element) data, true);
            } else {
                dynamicComposite = new ExtraComposite(parent, style, category, (Element) data, true);
            }

        } else if (EComponentCategory.STATSANDLOGS.equals(category)) {
            dynamicComposite = new StatsAndLogsComposite(parent, style, category, (Element) data);

        } else if (EComponentCategory.CONTEXT.equals(category)) {
            // TODO
            // dynamicComposite = new ContextDynamicComposite(parent, style, category, element);

        } else if (EComponentCategory.MAIN.equals(category)) {
            dynamicComposite = new MainComposite(parent, SWT.NONE, tabFactory.getWidgetFactory(), (IRepositoryViewObject) data);
        } else if (EComponentCategory.VERSIONS.equals(category)) {
            if (allowVerchange) {
                dynamicComposite = new ProcessVersionComposite(parent, SWT.NONE, tabFactory.getWidgetFactory(),
                        (IRepositoryViewObject) data);
            }
        } else if (EComponentCategory.HEADERFOOTER.equals(category)) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IHeaderFooterProviderService.class)) {
                IHeaderFooterProviderService headerFooterService = (IHeaderFooterProviderService) GlobalServiceRegister
                        .getDefault().getService(IHeaderFooterProviderService.class);
                if (headerFooterService.isVisible()) {
                    dynamicComposite = new HeaderFooterComposite(parent, SWT.NONE, tabFactory.getWidgetFactory(),
                            (IRepositoryViewObject) data);
                }
            }
        } else if (EComponentCategory.SVNHISTORY.equals(category) && service != null) {
            dynamicComposite = service.createProcessSVNHistoryComposite(parent, tabFactory.getWidgetFactory(),
                    (IRepositoryViewObject) data);
        } else if (EComponentCategory.APPEARANCE.equals(category)) {
            dynamicComposite = (IDynamicProperty) CorePlugin.getDefault().getDiagramModelService()
                    .getBusinessAppearanceComposite(parent, SWT.NONE, tabFactory.getWidgetFactory(), selectedModel);
        } else if (EComponentCategory.RULERS_AND_GRID.equals(category)) {
            dynamicComposite = (IDynamicProperty) CorePlugin.getDefault().getDiagramModelService()
                    .getBusinessRulersAndGridComposite(parent, SWT.NONE, tabFactory.getWidgetFactory(), null);
        } else if (EComponentCategory.ASSIGNMENT.equals(category)) {
            dynamicComposite = (IDynamicProperty) CorePlugin.getDefault().getDiagramModelService()
                    .getBusinessAssignmentComposite(parent, SWT.NONE, tabFactory.getWidgetFactory(), selectedModel);
        }

        if (dynamicComposite != null) {
            dynamicComposite.refresh();
        }
        currentSelectedTab.setPropertyComposite(dynamicComposite);
        return dynamicComposite;
    }

    /**
     * 
     * DOC ggu Comment method "setElement".
     * 
     * @param obj
     */

    private void setElement(Object obj, final String title, Image image) {
        EComponentCategory[] categories = null;

        if (obj != null && obj instanceof Process) {
            process = (Process) obj;
            if (currentSelectedTab != null && currentSelectedTab.getData().equals(process) && !cleaned) {
                return;
            }

            categories = getCategories(process);
        } else if (obj != null && obj instanceof IRepositoryViewObject) {
            categories = getCategories(obj);
        } else if (obj instanceof IEditorPart) {
            if (CorePlugin.getDefault().getDiagramModelService().isBusinessDiagramEditor((IEditorPart) obj)) {
                categories = getCategories(obj);
            }

        } else {
            BusinessType type = CorePlugin.getDefault().getDiagramModelService().getBusinessModelType(obj);
            if (BusinessType.NOTE.equals(type) || BusinessType.SHAP.equals(type) || BusinessType.CONNECTION.equals(type)) {
                categories = getCategories(obj);
            } else {
                cleanDisplay();
                return;
            }
        }

        final List<TalendPropertyTabDescriptor> descriptors = new ArrayList<TalendPropertyTabDescriptor>();
        for (EComponentCategory category : categories) {
            TalendPropertyTabDescriptor d = new TalendPropertyTabDescriptor(category);
            d.setData(obj);
            descriptors.add(d);
        }

        tabFactory.setInput(descriptors);
        setPartName(title, image);
        cleaned = false;
        tabFactory.setSelection(new IStructuredSelection() {

            public Object getFirstElement() {
                return null;
            }

            public Iterator iterator() {
                return null;
            }

            public int size() {
                return 0;
            }

            public Object[] toArray() {
                return null;
            }

            public List toList() {
                List<TalendPropertyTabDescriptor> d = new ArrayList<TalendPropertyTabDescriptor>();

                if (descriptors.size() > 0) {
                    if (currentSelectedTab != null) {
                        for (TalendPropertyTabDescriptor ds : descriptors) {
                            if (ds.getCategory() == currentSelectedTab.getCategory()) {
                                d.add(ds);
                                return d;
                            }
                        }
                    }
                    d.add(descriptors.get(0));
                }
                return d;
            }

            public boolean isEmpty() {
                return false;
            }

        });

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.ViewPart#setPartName(java.lang.String)
     */
    @Override
    protected void setPartName(String partName) {
        setPartName(partName, null);
    }

    /**
     * 
     * DOC ggu Comment method "setPartName".
     * 
     * set title
     */
    public void setPartName(String typeTitle, Image icon) {
        String title = null;
        String type = null;

        if (typeTitle != null && typeTitle.contains(SEPARATOR)) {
            String[] tt = typeTitle.split(SEPARATOR);
            type = tt[0];
            title = tt[1];
        } else {
            title = typeTitle;
        }

        String viewName = getViewNameLable();
        if (element instanceof IProcess && AbstractProcessProvider.isExtensionProcessForJoblet((IProcess) element)) {
            viewName = VIEW_NAME_JOBLET;
        }

        if (type != null) {
            viewName = type;
        }

        if (title == null) {
            title = ""; //$NON-NLS-1$
        }
        if (!title.equals("")) { //$NON-NLS-1$
            viewName = viewName + "(" + title + ")"; //$NON-NLS-1$ //$NON-NLS-2$            
            super.setTitleToolTip(title);
        }
        if (tabFactory != null && icon == null) {
            Image image = ImageProvider.getImage(ECoreImage.PROCESS_ICON);
            if (this.element != null && this.element instanceof IProcess) {
                if (((IProcess2) this.element).disableRunJobView()) { // ?? joblet
                    image = ImageProvider.getImage(ECoreImage.JOBLET_ICON);
                }
            }
            tabFactory.setTitle(title, image);
        } else {
            tabFactory.setTitle(title, icon);
        }

        super.setPartName(viewName);
    }

    /**
     * set the category.
     */
    private EComponentCategory[] getCategories(Object obj) {
        List<EComponentCategory> category = new ArrayList<EComponentCategory>();

        ISVNProviderService service = null;
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            service = (ISVNProviderService) GlobalServiceRegister.getDefault().getService(ISVNProviderService.class);
        }

        if (obj instanceof Process) {
            Process process = (Process) obj;
            boolean route = false;

            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault()
                        .getService(ICamelDesignerCoreService.class);
                if (camelService.isInstanceofCamelRoutes(process.getProperty().getItem())) {
                    route = true;
                }
            }

            category.add(EComponentCategory.MAIN);
            if (!route) {
                category.add(EComponentCategory.EXTRA);
            }
            boolean isJoblet = AbstractProcessProvider.isExtensionProcessForJoblet(process);
            if (!isJoblet && !route) {
                category.add(EComponentCategory.STATSANDLOGS);
            }

            if (allowVerchange) {
                category.add(EComponentCategory.VERSIONS);
            }

            if (GlobalServiceRegister.getDefault().isServiceRegistered(IHeaderFooterProviderService.class)) {
                IHeaderFooterProviderService headerFooterService = (IHeaderFooterProviderService) GlobalServiceRegister
                        .getDefault().getService(IHeaderFooterProviderService.class);
                if (headerFooterService.isVisible()) {
                    category.add(EComponentCategory.HEADERFOOTER);
                }
            }

            // if svn remote connection, added by nma
            if (service != null && service.isProjectInSvnMode()) {
                category.add(EComponentCategory.SVNHISTORY);
            }
            // category.add(EComponentCategory.CONTEXT);

        } else if (obj instanceof IRepositoryViewObject) {
            category.add(EComponentCategory.MAIN);
            if (allowVerchange) {
                category.add(EComponentCategory.VERSIONS);
            }

            if (service != null
                    && service.isProjectInSvnMode()
                    && (((IRepositoryViewObject) obj).getRepositoryObjectType() == ERepositoryObjectType.PROCESS || ((IRepositoryViewObject) obj)
                            .getRepositoryObjectType() == ERepositoryObjectType.JOBLET))
                category.add(EComponentCategory.SVNHISTORY);
        } else if (obj instanceof IEditorPart) {
            if (CorePlugin.getDefault().getDiagramModelService().isBusinessDiagramEditor((IEditorPart) obj)) {
                category.add(EComponentCategory.MAIN);
                category.add(EComponentCategory.APPEARANCE);
                category.add(EComponentCategory.RULERS_AND_GRID);
                if (allowVerchange) {
                    category.add(EComponentCategory.VERSIONS);
                }
            }
        } else {
            BusinessType type = CorePlugin.getDefault().getDiagramModelService().getBusinessModelType(obj);
            if (BusinessType.SHAP.equals(type) || BusinessType.CONNECTION.equals(type)) {
                category.add(EComponentCategory.APPEARANCE);
                category.add(EComponentCategory.ASSIGNMENT);
            } else if (BusinessType.NOTE.equals(type)) {
                category.add(EComponentCategory.APPEARANCE);
            }
        }

        return category.toArray(new EComponentCategory[0]);
    }

    public Process getElement() {
        return (Process) element;
    }

    public boolean isCleaned() {
        return this.cleaned;
    }

    public void cleanDisplay() {
        setPartName(null);
        tabFactory.setInput(null);
        tabFactory.setTitle(null, null);
        if (tabFactory.getTabComposite() != null) {
            for (Control curControl : tabFactory.getTabComposite().getChildren()) {
                curControl.dispose();
            }
        }
        this.currentSelectedTab = null;
        this.element = null;
        this.cleaned = true;
        this.selectedPrimary = true;
        process = null;
    }

    public void refresh() {
        refresh(false, null);
    }

    public void refresh(boolean force, Object obj) {
        if (force) {
            cleanDisplay();
        }
        final IEditorPart activeEditor = getSite().getPage().getActiveEditor();

        if (obj == null) {
            if (activeEditor != null && activeEditor instanceof AbstractMultiPageTalendEditor) {
                AbstractTalendEditor talendEditor = ((AbstractMultiPageTalendEditor) activeEditor).getTalendEditor();
                IProcess process = talendEditor.getProcess();
                if (process != null && process instanceof Element) {
                    this.selectedPrimary = true;
                    this.cleaned = force;
                    this.element = (Element) process;

                    // remove "Job" or "Joblet" from title
                    String title = activeEditor.getTitle();
                    if (title.startsWith(VIEW_NAME_JOBLET)) {
                        title = title.substring(VIEW_NAME_JOBLET.length() + 1);
                    } else if (title.startsWith(getViewNameLable())) {
                        title = title.substring(getViewNameLable().length() + 1);

                    }

                    setElement(element, title, null);
                    return;
                }
            } else {
                IDiagramModelService diagramModelService = CorePlugin.getDefault().getDiagramModelService();
                if (diagramModelService != null && diagramModelService.isBusinessDiagramEditor(activeEditor)) {
                    this.selectedPrimary = true;
                    this.cleaned = force;
                    IRepositoryViewObject object = retrieveBusiness(activeEditor);
                    if (object != null) {
                        String title = object.getLabel() + " " + object.getVersion(); //$NON-NLS-1$
                        Object type = object.getRepositoryObjectType();
                        setElement(activeEditor, type + SEPARATOR + title, null);
                    }
                    return;
                }
            }

        } else {

            this.selectedPrimary = true;
            this.cleaned = force;
            IRepositoryViewObject object = retrieveBusiness(activeEditor);
            if (object != null) {
                String title = object.getLabel() + " " + object.getVersion(); //$NON-NLS-1$
                Object type = object.getRepositoryObjectType();
                setElement(obj, type + SEPARATOR + title, null);
            }
            return;

        }

        cleanDisplay();

    }

    @Override
    public void setFocus() {
        this.parent.setFocus();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        CorePlugin.getDefault().getRepositoryService().removeRepositoryTreeViewListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent
     * )
     */
    public void selectionChanged(SelectionChangedEvent event) {
        ISelection selection = event.getSelection();
        if (selection instanceof StructuredSelection) {
            Object input = ((IStructuredSelection) selection).getFirstElement();

            if (!(input instanceof RepositoryNode)) {
                if (input instanceof IAdaptable) {
                    // see ProcessPart.getAdapter()
                    IAdaptable adaptable = (IAdaptable) input;
                    input = adaptable.getAdapter(RepositoryNode.class);
                }
            }

            if (input instanceof RepositoryNode) {
                RepositoryNode repositoryNode = (RepositoryNode) input;
                Object obj = repositoryNode.getProperties(EProperties.CONTENT_TYPE);

                String type = null;
                if (obj != null) {
                    type = obj.toString();
                    if (obj instanceof ERepositoryObjectType) {
                        ERepositoryObjectType objType = (ERepositoryObjectType) obj;
                        if (objType == ERepositoryObjectType.PROCESS) {
                            type = getViewNameLable();
                        } else if (objType == ERepositoryObjectType.JOBLET) {
                            type = VIEW_NAME_JOBLET;
                        }
                    }

                } else {
                    return;
                }

                IRepositoryViewObject repositoryObject = repositoryNode.getObject();
                if (repositoryObject == null) {
                    repositoryObject = new EmptyRepositoryObject();
                    return;
                }
                String title = repositoryObject.getLabel(); //$NON-NLS-1$
                if (allowVerchange) {
                    title = repositoryObject.getLabel() + " " + repositoryObject.getVersion(); //$NON-NLS-1$
                }

                setElement(repositoryObject, type + SEPARATOR + title, ImageProvider.getImage(repositoryNode.getIcon()));
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.ui.views.properties.IJobSettingsView#getSelection()
     */
    public ISelection getSelection() {
        ISVNProviderService service = null;
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            service = (ISVNProviderService) GlobalServiceRegister.getDefault().getService(ISVNProviderService.class);
        }

        IDynamicProperty dc = currentSelectedTab.getPropertyComposite();
        if (dc instanceof ProcessVersionComposite) {
            return ((ProcessVersionComposite) dc).getSelection();

        } else if (service != null && service.isSVNHistoryComposite(dc)) {
            return service.getSVNHistorySelection(dc);
        } else if (CorePlugin.getDefault().getDiagramModelService().isInstanceOfBusinessAssignmentComposite(dc)) {
            IRepositoryView repositoryView = RepositoryManagerHelper.findRepositoryView();
            if (repositoryView != null) {
                return repositoryView.getViewer().getSelection();
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.ui.views.properties.IJobSettingsView#refreshCurrentViewTab()
     */
    public void refreshCurrentViewTab() {
        if (currentSelectedTab == null) {
            return;
        }
        IDynamicProperty dc = currentSelectedTab.getPropertyComposite();
        if (dc != null) {
            dc.refresh();
        }
    }

    public void setISelection(ISelection selection) {
        this.selectedModel = selection;
    }

}
