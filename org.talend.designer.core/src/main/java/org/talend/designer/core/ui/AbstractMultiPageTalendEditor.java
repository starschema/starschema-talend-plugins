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
package org.talend.designer.core.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.editparts.INodeEditPart;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.EditorReference;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.rulers.IColumnSupport;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.context.UpdateRunJobComponentContextHelper;
import org.talend.core.model.metadata.builder.connection.Properties;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesPackage;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.IRepositoryWorkUnitListener;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.ResourceModelUtils;
import org.talend.core.service.IDesignerPerlService;
import org.talend.core.ui.ICreateXtextProcessService;
import org.talend.core.ui.ILastVersionChecker;
import org.talend.core.ui.IUIRefresher;
import org.talend.core.ui.branding.IBrandingConfiguration;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.utils.AccessingEmfJob;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.IMultiPageTalendEditor;
import org.talend.designer.core.ISyntaxCheckableEditor;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.CodeEditorFactory;
import org.talend.designer.core.ui.editor.TalendJavaEditor;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletUtil;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodeLabel;
import org.talend.designer.core.ui.editor.nodes.NodeLabelEditPart;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.outline.NodeTreeEditPart;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.process.ProcessPart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.designer.core.ui.views.problems.Problems;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.editor.JobEditorInput;
import org.talend.repository.editor.RepositoryEditorInput;
import org.talend.repository.job.deletion.JobResourceManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;

/**
 * DOC qzhang class global comment. Detailled comment
 */
public abstract class AbstractMultiPageTalendEditor extends MultiPageEditorPart implements IResourceChangeListener,
        ISelectionListener, IUIRefresher, IMultiPageTalendEditor {

    protected AdapterImpl dirtyListener = new AdapterImpl() {

        @Override
        public void notifyChanged(Notification notification) {
            if (notification.getEventType() != Notification.REMOVING_ADAPTER) {
                int featureID = notification.getFeatureID(Properties.class);
                if (featureID == PropertiesPackage.PROPERTY__INFORMATIONS) {
                    // || featureID == PropertiesPackage.PROPERTY__MODIFICATION_DATE) {
                    // ignore
                    return;
                } else if (featureID == PropertiesPackage.PROPERTY__MAX_INFORMATION_LEVEL) {
                    updateTitleImage();
                    return;
                }
                // propertyIsDirty = true;
                // process = designerEditor.getProcess();
                // process.getProperty().eAdapters().remove(dirtyListener);
                // process.updateProperties();
                // process.getProperty().eAdapters().add(dirtyListener);

                if (Display.getCurrent() != null) {
                    propertyIsDirty = true;
                    firePropertyChange(IEditorPart.PROP_DIRTY);
                }
            }
        }
    };

    protected IRepositoryWorkUnitListener repositoryWorkListener = new IRepositoryWorkUnitListener() {

        public void workUnitFinished() {
            revisionChanged = true;
            if (display != null) {
                display.asyncExec(new Runnable() {

                    public void run() {
                        setName();
                    }
                });
            }
        }
    };

    protected boolean propertyIsDirty = false;

    protected AbstractDecoratedTextEditor codeEditor;

    // protected IProcess2 process;

    protected IProcessor processor;

    protected String oldJobName;

    protected boolean keepPropertyLocked; // used only if the user try to open more than one editor at a time.

    protected JobEditorInput processEditorInput;

    protected AbstractTalendEditor designerEditor;

    protected List propertyInformation;

    protected boolean useCodeView = true;

    public boolean revisionChanged = false;

    public String revisionNumStr = null;

    protected Display display;

    private IPartListener partListener = new IPartListener() {

        public void partOpened(IWorkbenchPart part) {

        }

        public void partClosed(IWorkbenchPart part) {
            if (part == AbstractMultiPageTalendEditor.this) {
                restorePropertyInformation();
            }
        }

        public void partActivated(IWorkbenchPart part) {
        }

        public void partBroughtToTop(IWorkbenchPart part) {
        }

        public void partDeactivated(IWorkbenchPart part) {
        }

    };

    /**
     * DOC hcw Comment method "restorePropertyInformation".
     */
    protected void restorePropertyInformation() {
        if (getEditor(0).isDirty()) {
            // if user discard change, restore property information, make it possible to remove error status
            Property property = processEditorInput.getItem().getProperty();
            property.getInformations().clear();
            property.getInformations().addAll(propertyInformation);
            Problems.computePropertyMaxInformationLevel(property);
        }
    }

    public AbstractMultiPageTalendEditor() {
        super();

        ActiveProcessTracker.initialize();

        // Display.getDefault().asyncExec(new Runnable() {
        //
        // public void run() {
        // try {
        // CorePlugin.getDefault().getCodeGeneratorService().createRoutineSynchronizer().syncAllRoutines();
        // } catch (Exception e) {
        // ExceptionHandler.process(e);
        // }
        // }
        // });

        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        Map<String, Object> settings = brandingService.getBrandingConfiguration().getJobEditorSettings();
        if (settings.containsKey(IBrandingConfiguration.DISPLAY_CODE_VIEW)) {
            useCodeView = (Boolean) settings.get(IBrandingConfiguration.DISPLAY_CODE_VIEW);
        }
    }

    @Override
    public boolean isDirty() {
        return propertyIsDirty || super.isDirty();
    }

    public void setReadOnly(boolean readonly) {
        designerEditor.setReadOnly(readonly);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.MultiPageEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
        setSite(site);
        setInput(editorInput);
        site.setSelectionProvider(new MultiPageTalendSelectionProvider(this));
        getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);

        // Lock the process :
        IRepositoryService service = CorePlugin.getDefault().getRepositoryService();
        IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();
        processEditorInput = (JobEditorInput) editorInput;
        IProcess2 currentProcess = processEditorInput.getLoadedProcess();
        if (!currentProcess.isReadOnly()) {
            try {
                Property property = processEditorInput.getItem().getProperty();
                propertyInformation = new ArrayList(property.getInformations());
                property.eAdapters().add(dirtyListener);
                display = site.getShell().getDisplay();
                repFactory.addRepositoryWorkUnitListener(repositoryWorkListener);
                repFactory.lock(currentProcess);
                boolean locked = currentProcess.getProperty().getItem().getState().isLocked();
                if (!locked) {
                    setReadOnly(true);
                }
                revisionChanged = true;
            } catch (PersistenceException e) {
                // e.printStackTrace();
                ExceptionHandler.process(e);
            } catch (BusinessException e) {
                // Nothing to do
                ExceptionHandler.process(e);
            }
        } else {
            setReadOnly(true);
            revisionChanged = true;
        }
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
            ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                    ICamelDesignerCoreService.class);
            boolean isCamel = camelService.isInstanceofCamelRoutes(processEditorInput.getItem());
            ERepositoryObjectType repositoryNodeType = camelService.getRoutes();
            if (isCamel) {
                RepositoryManager.refresh(repositoryNodeType);
            }
        }
        if (processEditorInput.getItem() instanceof ProcessItem) {
            RepositoryManager.refresh(ERepositoryObjectType.PROCESS);
        } else {
            RepositoryManager.refresh(ERepositoryObjectType.JOBLET);
        }
        getSite().getWorkbenchWindow().getPartService().addPartListener(partListener);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.editor.INameRefresher#refreshName()
     */
    public void refreshName() {
        try {
            JobResourceManager jobResourceManager = JobResourceManager.getInstance();
            jobResourceManager.removeProtection(designerEditor);
            for (String id : designerEditor.getProtectedIds()) {
                if (designerEditor.getJobResource(id).getJobInfo().getJobName().equalsIgnoreCase(oldJobName)) {
                    // delete only the job renamed
                    jobResourceManager.deleteResource(designerEditor.getJobResource(id));
                }
            }
            designerEditor.resetJobResources();

            setName();
            JobInfo jobInfo = designerEditor.getCurrentJobResource().getJobInfo();
            if (jobInfo != null) {
                jobInfo.setJobName(getEditorInput().getName());
            }
            jobResourceManager.addProtection(designerEditor);

            processor.initPath();
            processor.setProcessorStates(IProcessor.STATES_EDIT);

            // modified by wzhang to fix bug 8180 in thales branding.
            if (useCodeView) {
                FileEditorInput input = createFileEditorInput();
                codeEditor.setInput(input);
            }

            IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (activeWorkbenchWindow != null) {
                if (activeWorkbenchWindow.getActivePage().isPartVisible(this)) {
                    new ActiveProcessTracker().partBroughtToTop(this);
                    DesignerPlugin.getDefault().getRunProcessService().refreshView();
                }
            }

        } catch (Exception e) {
            MessageBoxExceptionHandler.process(e);
        }
    }

    /**
     * DOC bqian Comment method "selectNode".
     * 
     * @param node
     */
    @SuppressWarnings("unchecked")
    public void selectNode(Node node) {
        GraphicalViewer viewer = designerEditor.getViewer();
        Object object = viewer.getRootEditPart().getChildren().get(0);
        if (object instanceof ProcessPart) {
            // the structure in memory is:
            // ProcessPart < SubjobContainerPart < NodeContainerPart < NodePart
            for (EditPart editPart : (List<EditPart>) ((ProcessPart) object).getChildren()) {
                if (editPart instanceof SubjobContainerPart) {
                    SubjobContainerPart subjobPart = (SubjobContainerPart) editPart;
                    for (EditPart part : (List<EditPart>) subjobPart.getChildren()) {
                        if (part instanceof NodeContainerPart) {
                            EditPart nodePart = (EditPart) part.getChildren().get(0);
                            if (nodePart instanceof NodePart) {
                                if (((NodePart) nodePart).getModel().equals(node)) {
                                    viewer.select(nodePart);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * DOC bqian Comment method "selectNode".
     * 
     * @param node
     */
    @SuppressWarnings("unchecked")
    public void selectNode(String nodeName) {
        GraphicalViewer viewer = designerEditor.getViewer();
        Object object = viewer.getRootEditPart().getChildren().get(0);
        if (object instanceof ProcessPart) {
            // the structure in memory is:
            // ProcessPart < SubjobContainerPart < NodeContainerPart < NodePart
            for (EditPart editPart : (List<EditPart>) ((ProcessPart) object).getChildren()) {
                if (editPart instanceof SubjobContainerPart) {
                    SubjobContainerPart subjobPart = (SubjobContainerPart) editPart;
                    for (EditPart part : (List<EditPart>) subjobPart.getChildren()) {
                        if (part instanceof NodeContainerPart) {
                            EditPart nodePart = (EditPart) part.getChildren().get(0);
                            if (nodePart instanceof NodePart) {
                                if (((Node) ((NodePart) nodePart).getModel()).getLabel().equals(nodeName)) {
                                    viewer.select(nodePart);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // only convert process and jobscript when select between designer and jboscript page.
    int oldPageIndex = -1;

    /**
     * Calculates the contents of page 2 when the it is activated.
     */
    @Override
    protected void pageChange(final int newPageIndex) {
        super.pageChange(newPageIndex);
        setName();
        if (newPageIndex == 1) {
            if (codeEditor instanceof ISyntaxCheckableEditor) {
                moveCursorToSelectedComponent();

                /*
                 * Belowing method had been called at line 331 within the generateCode method, as soon as code
                 * generated.
                 */
                // ((ISyntaxCheckableEditor) codeEditor).validateSyntax();
            }

            codeSync();
            // for bug 5033
            if (codeEditor instanceof ISyntaxCheckableEditor && LanguageManager.getCurrentLanguage() == ECodeLanguage.PERL) {
                ((ISyntaxCheckableEditor) codeEditor).validateSyntax();
            }
        } else if (newPageIndex == 0 && oldPageIndex == 2) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICreateXtextProcessService.class)) {
                try {
                    boolean isDirty = getEditor(2).isDirty();
                    getEditor(2).doSave(null);
                    IProcess2 oldProcess = getProcess();

                    ICreateXtextProcessService n = CorePlugin.getDefault().getCreateXtextProcessService();
                    ProcessType processType = n.convertDesignerEditorInput(
                            ((IFile) getEditor(2).getEditorInput().getAdapter(IResource.class)).getLocation().toOSString(),
                            oldProcess.getProperty());

                    Item item = oldProcess.getProperty().getItem();

                    if (item instanceof ProcessItem) {

                        ((Process) oldProcess).updateProcess(processType);
                    } else if (item instanceof JobletProcessItem) {
                        ((Process) oldProcess).updateProcess(processType);
                    }
                    oldProcess.getUpdateManager().updateAll();
                    designerEditor.setDirty(isDirty);

                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }

        } else if (newPageIndex == 2) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICreateXtextProcessService.class)) {
                ICreateXtextProcessService convertJobtoScriptService = CorePlugin.getDefault().getCreateXtextProcessService();

                String scriptValue;
                try {
                    scriptValue = convertJobtoScriptService.convertJobtoScript(getProcess().saveXmlFile());
                    IFile file = (IFile) getEditor(2).getEditorInput().getAdapter(IResource.class);
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(scriptValue.getBytes());
                    if (file.exists()) {
                        ((AbstractDecoratedTextEditor) getEditor(2)).getDocumentProvider()
                                .getDocument(getEditor(2).getEditorInput()).set(scriptValue);

                        IAction action = ((AbstractDecoratedTextEditor) getEditor(2)).getAction("FoldingRestore"); //$NON-NLS-1$
                        action.run();
                        getEditor(2).doSave(null);
                    } else {
                        file.create(byteArrayInputStream, true, null);
                    }
                } catch (PartInitException e) {
                    ExceptionHandler.process(e);
                } catch (CoreException e) {
                    ExceptionHandler.process(e);
                } catch (IOException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        oldPageIndex = getActivePage();

    }

    /**
     * Move Cursor to Selected Node.
     * 
     * @param processor
     */
    private void moveCursorToSelectedComponent() {
        String nodeName = getSelectedNodeName();
        if (nodeName != null) {
            if (codeEditor instanceof TalendJavaEditor) {
                ((TalendJavaEditor) codeEditor).placeCursorTo(nodeName);
            } else {
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerPerlService.class)) {
                    IDesignerPerlService service = (IDesignerPerlService) GlobalServiceRegister.getDefault().getService(
                            IDesignerPerlService.class);
                    service.placeCursorTo(codeEditor, nodeName);
                }
            }
        }
    }

    public void setName() {
        if (getEditorInput() == null) {
            return;
        }
        String label = getEditorInput().getName();
        oldJobName = label;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.MultiPageEditorPart#createPages()
     */
    @Override
    protected void createPages() {
        createPage0();
        createPage1();
        createPage2();

        if (getPageCount() == 1) {
            Composite container = getContainer();
            if (container instanceof CTabFolder) {
                ((CTabFolder) container).setTabHeight(0);
            }
        }

    }

    protected void createPage0() {
        try {
            int index = addPage(designerEditor, getEditorInput());
            setPageText(index, "Designer"); //$NON-NLS-1$
            designerEditor.setParent(this);
        } catch (PartInitException e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
    }

    /**
     * Creates page 1 of the multi-page editor, which allows you to change the font used in page 2.
     */
    protected void createPage1() {
        IProcess2 process = getProcess();
        codeEditor = CodeEditorFactory.getInstance().getCodeEditor(getCurrentLang(), process);
        ((Process) process).setEditor(this);
        processor = ProcessorUtilities.getProcessor(process, process.getProperty(), process.getContextManager()
                .getDefaultContext());
        if (processor instanceof IJavaBreakpointListener) {
            JDIDebugModel.addJavaBreakpointListener((IJavaBreakpointListener) processor);
        }

        processor.setProcessorStates(IProcessor.STATES_EDIT);
        if (codeEditor instanceof ISyntaxCheckableEditor) {
            processor.setSyntaxCheckableEditor((ISyntaxCheckableEditor) codeEditor);
        }

        if (useCodeView) {
            try {
                int index = addPage(codeEditor, createFileEditorInput());
                // init Syntax Validation.
                if (getCurrentLang() == ECodeLanguage.PERL) {
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerPerlService.class)) {
                        IDesignerPerlService service = (IDesignerPerlService) GlobalServiceRegister.getDefault().getService(
                                IDesignerPerlService.class);
                        service.setSyntaxValidationPreference(true);
                    }
                }
                setPageText(index, "Code"); //$NON-NLS-1$

            } catch (PartInitException pie) {
                // pie.printStackTrace();
                ExceptionHandler.process(pie);
            }
        }

        if (DesignerPlugin.getDefault().getPreferenceStore().getBoolean(TalendDesignerPrefConstants.GENERATE_CODE_WHEN_OPEN_JOB)) {
            generateCode();
        }
    }

    // create jobscript editor
    protected void createPage2() {
        if (!GlobalServiceRegister.getDefault().isServiceRegistered(ICreateXtextProcessService.class)) {
            return;
        }
        String scriptValue = "";
        try {
            IProject currentProject = ResourceModelUtils.getProject(ProjectManager.getInstance().getCurrentProject());
            String jobScriptVersion = "";
            if (getEditorInput() != null && getEditorInput() instanceof RepositoryEditorInput) {
                Item item = ((RepositoryEditorInput) getEditorInput()).getItem();
                if (item != null) {
                    Property property = item.getProperty();
                    if (property != null) {
                        jobScriptVersion = "_" + property.getVersion();
                    }
                }
            }
            IFile file = currentProject.getFolder("temp").getFile(getEditorInput().getName() + jobScriptVersion + ".jobscript");

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(scriptValue.getBytes());
            if (file.exists()) {
                file.setContents(byteArrayInputStream, 0, null);
            } else
                file.create(byteArrayInputStream, true, null);

            // the way to get the xtextEditor programmly
            IEditorInput editorInput = new FileEditorInput(file);
            IEditorDescriptor desc = WorkbenchPlugin.getDefault().getEditorRegistry()
                    .findEditor("org.talend.metalanguage.jobscript.JobScript");

            WorkbenchPage page = (WorkbenchPage) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            EditorReference ref = new EditorReference(page.getEditorManager(), new FileEditorInput(file), (EditorDescriptor) desc);

            IEditorPart editorPart = ref.getEditor(true);

            if (editorPart != null) {
                int index = addPage(editorPart, editorInput);
                setPageText(index, "Jobscript");
            }
        } catch (PartInitException e) {
            ExceptionHandler.process(e);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * DOC bqian Comment method "generateCode".
     */
    protected void generateCode() {
        final IProcess2 process = getProcess();
        if (!(process.getProperty().getItem() instanceof ProcessItem)) { // shouldn't work for joblet
            return;
        }
        if (process.getGeneratingNodes().size() != 0) {
            Job job = new AccessingEmfJob("Generating code") { //$NON-NLS-1$

                @Override
                protected IStatus doRun(IProgressMonitor monitor) {
                    try {
                        ProcessorUtilities.generateCode(process, process.getContextManager().getDefaultContext(), false, false,
                                true, ProcessorUtilities.GENERATE_WITH_FIRST_CHILD);
                    } catch (ProcessorException e) {
                        ExceptionHandler.process(e);
                    }

                    return Status.OK_STATUS;
                }
            };
            job.setUser(true);
            job.setPriority(Job.BUILD);
            job.schedule(); // start as soon as possible
        }
    }

    /**
     * Creates page 1 of the multi-page editor, which allows you to change the font used in page 2.
     */

    /**
     * get the current project generating code language.
     * 
     * @return the current generating code language
     */
    private ECodeLanguage getCurrentLang() {
        return ((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getProject()
                .getLanguage();
    }

    /**
     * Saves the multi-page editor's document.
     */
    @Override
    public void doSave(final IProgressMonitor monitor) {
        if (!isDirty()) {
            return;
        }
        updateRunJobContext();
        designerEditor.getProcess().getProperty().eAdapters().remove(dirtyListener);
        IRepositoryService service = CorePlugin.getDefault().getRepositoryService();
        IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();
        display = getSite().getShell().getDisplay();
        repFactory.addRepositoryWorkUnitListener(repositoryWorkListener);

        if (getActivePage() == 0) {
            getEditor(0).doSave(monitor);

            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICreateXtextProcessService.class)) {
                ICreateXtextProcessService convertJobtoScriptService = CorePlugin.getDefault().getCreateXtextProcessService();

                Item item = getDesignerEditor().getProcess().getProperty().getItem();
                ProcessType processType = null;

                if (item instanceof ProcessItem) {
                    processType = ((ProcessItem) item).getProcess();
                } else if (item instanceof JobletProcessItem) {
                    processType = ((JobletProcessItem) item).getJobletProcess();
                }

                if (item instanceof ProcessItem) { // disable for joblet for now
                    String scriptValue = convertJobtoScriptService.convertJobtoScript(processType);
                    try {
                        IFile file = (IFile) getEditor(2).getEditorInput().getAdapter(IResource.class);
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(scriptValue.getBytes());
                        if (file.exists()) {
                            ((AbstractDecoratedTextEditor) getEditor(2)).getDocumentProvider()
                                    .getDocument(getEditor(2).getEditorInput()).set(scriptValue);

                            IAction action = ((AbstractDecoratedTextEditor) getEditor(2)).getAction("FoldingRestore"); //$NON-NLS-1$
                            action.run();
                            getEditor(2).doSave(monitor);
                        } else {
                            file.create(byteArrayInputStream, true, null);
                        }
                    } catch (PartInitException e) {
                        ExceptionHandler.process(e);
                    } catch (CoreException e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        } else if (getActivePage() == 2) {
            getEditor(2).doSave(monitor);
            try {
                // ICreateXtextProcessService n = CorePlugin.getDefault().getCreateXtextProcessService();
                //
                // ProcessType processType = n.convertDesignerEditorInput(
                // ((IFile) getEditor(2).getEditorInput().getAdapter(IResource.class)).getLocation().toOSString(),
                // designerEditor.getProcess().getProperty());
                // ProcessItem processItem = (ProcessItem) getProcess().getProperty().getItem();
                // processItem.setProcess(processType);
                // getProcess().loadXmlFile();

                IProcess2 oldProcess = getProcess();

                ICreateXtextProcessService n = CorePlugin.getDefault().getCreateXtextProcessService();
                ProcessType processType = n.convertDesignerEditorInput(
                        ((IFile) getEditor(2).getEditorInput().getAdapter(IResource.class)).getLocation().toOSString(),
                        oldProcess.getProperty());

                // designerEditor.getProcess().dispose();
                // ProcessItem processItem = (ProcessItem) getProcess().getProperty().getItem();
                // processItem.setProcess(processType);
                IProcess2 newProcess = null;
                Item item = getProcess().getProperty().getItem();

                if (item instanceof ProcessItem) {
                    // ((ProcessItem) item).setProcess(processType);
                    // newProcess = new Process(item.getProperty());

                    ((Process) designerEditor.getProcess()).updateProcess(processType);
                } else if (item instanceof JobletProcessItem) {
                    AbstractProcessProvider processProvider = AbstractProcessProvider
                            .findProcessProviderFromPID(IComponent.JOBLET_PID);
                    if (processProvider != null) {
                        newProcess = processProvider.buildNewGraphicProcess(item);
                    }
                    designerEditor.setProcess(newProcess);

                    Boolean lastVersion = null;
                    if (oldProcess instanceof ILastVersionChecker) {
                        lastVersion = ((ILastVersionChecker) oldProcess).isLastVersion(item);
                    }

                    if (designerEditor.getEditorInput() instanceof JobEditorInput) {
                        ((JobEditorInput) designerEditor.getEditorInput()).checkInit(lastVersion, null, true);
                    }
                }

            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        /*
         * refresh should be executed before add the listener,or it will has eProxy on the property,it will cause a
         * editor dirty problem. hywang commet bug 17357
         */
        if (processEditorInput != null) {
            propertyInformation = new ArrayList(processEditorInput.getItem().getProperty().getInformations());
            propertyIsDirty = false;
            firePropertyChange(IEditorPart.PROP_DIRTY);

            if (processEditorInput.getItem() instanceof ProcessItem) {
                RepositoryManager.refresh(ERepositoryObjectType.PROCESS);

            } else {
                RepositoryManager.refresh(ERepositoryObjectType.JOBLET);
            }
        }
        if (designerEditor != null && dirtyListener != null)
            designerEditor.getProcess().getProperty().eAdapters().add(dirtyListener);
    }

    protected void updateRunJobContext() {
        final JobContextManager manager = (JobContextManager) getProcess().getContextManager();
        if (manager.isModified()) {
            final Map<String, String> nameMap = manager.getNameMap();

            // gcui:add a progressDialog.
            Shell shell = null;
            Display display = PlatformUI.getWorkbench().getDisplay();
            if (display != null) {
                shell = display.getActiveShell();
            }
            if (shell == null) {
                display = Display.getCurrent();
                if (display == null) {
                    display = Display.getDefault();
                }
                if (display != null) {
                    shell = display.getActiveShell();
                }
            }
            ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(shell);
            IRunnableWithProgress runnable = new IRunnableWithProgress() {

                public void run(final IProgressMonitor monitor) {
                    monitor.beginTask(Messages.getString("AbstractMultiPageTalendEditor_pleaseWait"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                    Display.getDefault().syncExec(new Runnable() {

                        public void run() {
                            IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();
                            factory.executeRepositoryWorkUnit(new RepositoryWorkUnit<Object>("..", this) { //$NON-NLS-1$

                                @Override
                                protected void run() throws LoginException, PersistenceException {
                                    try {
                                        IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();

                                        Set<String> curContextVars = getCurrentContextVariables(manager);
                                        IProcess2 process2 = getProcess();
                                        String jobId = process2.getProperty().getId();
                                        IEditorReference[] reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                                .getActivePage().getEditorReferences();
                                        List<IProcess2> processes = CorePlugin.getDefault().getDesignerCoreService()
                                                .getOpenedProcess(reference);

                                        // gcui:if nameMap is empty it do nothing.
                                        if (!nameMap.isEmpty()) {
                                            UpdateRunJobComponentContextHelper.updateItemRunJobComponentReference(factory,
                                                    nameMap, jobId, curContextVars);
                                            UpdateRunJobComponentContextHelper.updateOpenedJobRunJobComponentReference(processes,
                                                    nameMap, jobId, curContextVars);
                                        }
                                        // add for bug 9564
                                        List<IRepositoryViewObject> all = factory.getAll(ERepositoryObjectType.PROCESS, true);
                                        List<ProcessItem> allProcess = new ArrayList<ProcessItem>();
                                        for (IRepositoryViewObject repositoryObject : all) {
                                            Item item = repositoryObject.getProperty().getItem();
                                            if (item instanceof ProcessItem) {
                                                ProcessItem processItem = (ProcessItem) item;
                                                allProcess.add(processItem);
                                            }
                                        }
                                        UpdateRunJobComponentContextHelper.updateRefJobRunJobComponentContext(factory,
                                                allProcess, process2);

                                    } catch (PersistenceException e) {
                                        // e.printStackTrace();
                                        ExceptionHandler.process(e);
                                    }
                                    nameMap.clear();
                                    manager.setModified(false);
                                }
                            });

                        }

                    });
                    monitor.done();
                    if (monitor.isCanceled()) {
                        try {
                            throw new InterruptedException("Save Fail"); //$NON-NLS-1$
                        } catch (InterruptedException e) {
                            ExceptionHandler.process(e);
                        }
                    }
                }
            };
            try {
                progressDialog.run(true, true, runnable);
            } catch (InvocationTargetException e1) {
                ExceptionHandler.process(e1);
            } catch (InterruptedException e1) {
                ExceptionHandler.process(e1);
            }
        }
    }

    private Set<String> getCurrentContextVariables(IContextManager manager) {
        Set<String> varNameSet = new HashSet<String>();
        if (manager != null) {
            for (IContextParameter param : manager.getDefaultContext().getContextParameterList()) {
                varNameSet.add(param.getName());
            }
        }
        return varNameSet;
    }

    public void codeSync() {
        IProcess2 process = getProcess();
        if (!(process.getProperty().getItem() instanceof ProcessItem)) { // shouldn't work for joblet
            return;
        }
        // added for routines code generated switch editor 0 to 3.
        ProcessItem processItem = (ProcessItem) process.getProperty().getItem();

        if (oldPageIndex == 2) {
            ParametersType parameters = processItem.getProcess().getParameters();
            if (parameters != null && parameters.getRoutinesParameter() != null && parameters.getRoutinesParameter().size() == 0) {
                try {
                    List<RoutinesParameterType> dependenciesInPreference = RoutinesUtil.createDependenciesInPreference();
                    parameters.getRoutinesParameter().addAll(dependenciesInPreference);
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        // if some code has been generated already, for the editor we should need only the main job, not the childs.
        try {
            boolean lastGeneratedWithStats = ProcessorUtilities.getLastGeneratedWithStats(process.getId());
            boolean lastGeneratedWithTrace = ProcessorUtilities.getLastGeneratedWithTrace(process.getId());

            if (processor.isCodeGenerated()) {
                ProcessorUtilities.generateCode(process, process.getContextManager().getDefaultContext(), lastGeneratedWithStats,
                        lastGeneratedWithTrace, true, ProcessorUtilities.GENERATE_MAIN_ONLY);
            } else {
                ProcessorUtilities.generateCode(process, process.getContextManager().getDefaultContext(), lastGeneratedWithStats,
                        lastGeneratedWithTrace, true, ProcessorUtilities.GENERATE_WITH_FIRST_CHILD);
            }
        } catch (ProcessorException e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * Saves the multi-page editor's document as another file. Also updates the text for page 0's tab, and updates this
     * multi-page editor's input to correspond to the nested editor's.
     */
    @Override
    public void doSaveAs() {
        IEditorPart editor = getEditor(0);
        editor.doSaveAs();
        // setPageText(0, editor.getTitle());
        // setInput(editor.getEditorInput());
    }

    /*
     * (non-Javadoc) Method declared on IEditorPart
     */
    public void gotoMarker(final IMarker marker) {
        setActivePage(0);
    }

    /*
     * (non-Javadoc) Method declared on IEditorPart.
     */
    @Override
    public boolean isSaveAsAllowed() {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            return false;
        }
        return true;
    }

    public void showDesignerPage() {
        setActivePage(0);
    }

    public void showCodePage() {
        if (useCodeView) {
            setActivePage(1);
        }
    }

    /**
     * Move Cursor to Selected Node.
     * 
     * @param processor
     */

    /**
     * Get the selected Node if any.
     * 
     * @return the component selected name or null if component is not found or is not activated
     */
    public String getSelectedNodeName() {
        String nodeName = null;
        Node node = getSelectedGraphicNode();
        if (node != null) {
            if (node.isActivate() || node.isDummy()) {
                nodeName = node.getUniqueName();
            } else {
                nodeName = null;
            }

            if (isVirtualNode(node)) {
                // add for feature 13701
                for (IMultipleComponentManager mcm : node.getComponent().getMultipleComponentManagers()) {
                    if (!mcm.isLookupMode()) {
                        nodeName += "_" + mcm.getInput().getName(); //$NON-NLS-1$
                    }
                }
                // for feature 13701
                //nodeName += "_" + node.getComponent().getMultipleComponentManagers().get(0).getInput().getName(); //$NON-NLS-1$
            }
            if (node.isFileScaleComponent()) {
                nodeName += "_fsNode"; //$NON-NLS-1$
            }
        }
        return nodeName;
    }

    /**
     * 
     * DOC YeXiaowei Comment method "isVirtualNode".
     * 
     * @param node
     * @return
     */
    private boolean isVirtualNode(final INode node) {
        boolean isVirtualNode = false;

        IElementParameter param = node.getElementParameter("IS_VIRTUAL_COMPONENT"); //$NON-NLS-1$
        if (param != null) { // now only available for tUniqRow.
            return (Boolean) param.getValue();
        }

        if (node.getUniqueName().startsWith("tMap")) { //$NON-NLS-1$
            isVirtualNode = CorePlugin.getDefault().getMapperService().isVirtualComponent(node);
        } else {
            List<IMultipleComponentManager> multipleComponentManagers = node.getComponent().getMultipleComponentManagers();
            for (IMultipleComponentManager mcm : multipleComponentManagers) {
                if (!mcm.isLookupMode()) {
                    return true;
                }
            }
        }

        return isVirtualNode;
    }

    /**
     * DOC amaumont Comment method "getSelectedNode".
     * 
     * @return
     */
    public Node getSelectedGraphicNode() {
        Node node = null;
        List selections = designerEditor.getViewer().getSelectedEditParts();
        if (selections.size() == 1) {
            Object selection = selections.get(0);

            if (selection instanceof NodeTreeEditPart) {
                NodeTreeEditPart nTreePart = (NodeTreeEditPart) selection;
                node = (Node) nTreePart.getModel();
            } else {
                if (selection instanceof NodePart) {
                    NodePart editPart = (NodePart) selection;
                    node = (Node) editPart.getModel();
                } else if (selection instanceof NodeLabelEditPart) {
                    NodeLabelEditPart editPart = (NodeLabelEditPart) selection;
                    node = ((NodeLabel) editPart.getModel()).getNode();
                }
            }
        }
        return node;
    }

    public abstract void updateTitleImage();

    /**
     * Closes all project files on project close.
     */

    public void resourceChanged(final IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
            Display.getDefault().asyncExec(new Runnable() {

                public void run() {
                    IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
                    for (int i = 0; i < pages.length; i++) {
                        if (((FileEditorInput) designerEditor.getEditorInput()).getFile().getProject()
                                .equals(event.getResource())) {
                            IEditorPart editorPart = pages[i].findEditor(designerEditor.getEditorInput());
                            pages[i].closeEditor(editorPart, true);
                        }
                    }
                }
            });
        } else if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            updateTitleImage();
        }
    }

    @Override
    public Object getAdapter(final Class adapter) {
        if (designerEditor.equals(getActiveEditor())) {
            return this.getActiveEditor().getAdapter(adapter);
        }
        /*
         * if (textEditor.equals(getActiveEditor())) { if (adapter == IPropertySheetPage.class) { return null; } return
         * this.getActiveEditor().getAdapter(adapter); }
         */
        return super.getAdapter(adapter);
    }

    /**
     * Will allow to propagate the Delete evenement in the designer.
     */
    public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
        if (this.equals(getSite().getPage().getActiveEditor())) {
            if (selection instanceof StructuredSelection) {
                StructuredSelection structSel = (StructuredSelection) selection;
                if (structSel.getFirstElement() instanceof EditPart) {
                    if (designerEditor.equals(getActiveEditor())) {
                        designerEditor.selectionChanged(getActiveEditor(), selection);

                    }
                }
            }
        }
    }

    protected FileEditorInput createFileEditorInput() {

        IPath codePath = processor.getCodePath();

        if (codePath.isEmpty()) {
            // reinitialize the processor if there was any problem during the initialization.
            // (should not happen)
            try {
                processor.initPath();
            } catch (ProcessorException e) {
                MessageBoxExceptionHandler.process(e);
            }
            codePath = processor.getCodePath();
        }

        IFile codeFile = ResourcesPlugin.getWorkspace().getRoot()
                .getFile(processor.getCodeProject().getFullPath().append(codePath));
        if (!codeFile.exists()) {
            // Create empty one
            try {
                codeFile.create(new ByteArrayInputStream("".getBytes()), true, null); //$NON-NLS-1$
            } catch (CoreException e) {
                // Do nothing.
            }
        }
        return new FileEditorInput(codeFile);
    }

    /**
     * Getter for process.
     * 
     * @return the process
     */
    public IProcess2 getProcess() {
        if (designerEditor == null) {
            return null;
        }
        return designerEditor.getProcess();
    }

    public void updateChildrens() {
        // just call the method add protection will update new childrens and
        // keep old ones (keep to delete automatically
        // when closing job)
        JobResourceManager.getInstance().addProtection(designerEditor);
    }

    /**
     * DOC bqian Comment method "selectNode".
     * 
     * @param node
     */
    public void selectNode(INode node) {
        GraphicalViewer viewer = designerEditor.getViewer();
        Object object = viewer.getRootEditPart().getChildren().get(0);
        if (object instanceof ProcessPart) {
            for (EditPart editPart : (List<EditPart>) ((ProcessPart) object).getChildren()) {
                if (editPart instanceof NodePart) {
                    if (((INodeEditPart) editPart).getModel().equals(node)) {
                        viewer.select(editPart);
                    }
                }
            }
        }
    }

    public boolean isJobAlreadyOpened() {
        return foundExistEditor(this.getEditorInput());
    }

    private boolean foundExistEditor(final IEditorInput editorInput) {
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow != null) {

            WorkbenchPage page = (WorkbenchPage) activeWorkbenchWindow.getActivePage();
            if (page != null) {
                if (editorInput instanceof RepositoryEditorInput) {
                    RepositoryEditorInput curEditorInput = (RepositoryEditorInput) editorInput;

                    IEditorReference[] ref = page.findEditors(curEditorInput, getEditorId(), IWorkbenchPage.MATCH_INPUT);
                    return ref.length > 1;
                }
            }

        }
        return false;
    }

    /**
     * DOC qzhang Comment method "getEditorId".
     * 
     * @return
     */
    public abstract String getEditorId();

    /**
     * Getter for keepPropertyLocked.
     * 
     * @return the keepPropertyLocked
     */
    public boolean isKeepPropertyLocked() {
        return this.keepPropertyLocked;
    }

    /**
     * Sets the keepPropertyLocked.
     * 
     * @param keepPropertyLocked the keepPropertyLocked to set
     */
    public void setKeepPropertyLocked(boolean keepPropertyLocked) {
        this.keepPropertyLocked = keepPropertyLocked;
    }

    /**
     * Getter for codeEditor.
     * 
     * @return the codeEditor
     */
    public TalendJavaEditor getCodeEditor() {
        return (TalendJavaEditor) this.codeEditor;
    }

    public AbstractTalendEditor getTalendEditor() {
        return designerEditor;
    }

    public void beforeDispose() {
        if (!GlobalServiceRegister.getDefault().isServiceRegistered(ICreateXtextProcessService.class)) {
            return;
        }
        if (this.getPageCount() > 2) {
            IColumnSupport cs = (IColumnSupport) ((AbstractDecoratedTextEditor) getEditor(2)).getAdapter(IColumnSupport.class);
            cs.dispose();
        }
    }

    /**
     * The <code>MultiPageEditorPart</code> implementation of this <code>IWorkbenchPart</code> method disposes all
     * nested editors. Subclasses may extend.
     */
    @Override
    public void dispose() {
        getSite().setSelectionProvider(null);
        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
        /* after the release of eclipse3.6,this parameter can't be null */
        // setInput(null);
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        getSite().getWorkbenchWindow().getPartService().removePartListener(partListener);
        super.dispose();

        if (isKeepPropertyLocked()) {
            return;
        }

        // Unlock the process :
        IRepositoryService service = CorePlugin.getDefault().getRepositoryService();
        IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();
        try {
            getProcess().getProperty().eAdapters().remove(dirtyListener);
            Property property = getProcess().getProperty();
            if (property.eResource() == null || property.getItem().eResource() == null) {
                property = repFactory.getUptodateProperty(property);
            }
            // fix for bug 12524 for db repository
            // property = repFactory.reload(property);

            JobletUtil jUtil = new JobletUtil();
            jUtil.makeSureUnlockJoblet(getProcess());
            Item item = getProcess().getProperty().getItem();
            boolean keep = jUtil.keepLockJoblet(item);
            if (keep) {
                repFactory.unlock(property.getItem());
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        } catch (LoginException e) {
            ExceptionHandler.process(e);
        }

        if (AbstractProcessProvider.isExtensionProcessForJoblet(getProcess())) {
            RepositoryManager.refresh(ERepositoryObjectType.JOBLET);
        } else {
            RepositoryManager.refresh(ERepositoryObjectType.PROCESS);
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault()
                        .getService(ICamelDesignerCoreService.class);
                RepositoryManager.refresh(camelService.getRoutes());
            }
        }
        processEditorInput.setLoadedProcess(null);
        processEditorInput = null;
        designerEditor = null;
        codeEditor = null;
        if (processor instanceof IJavaBreakpointListener) {
            JDIDebugModel.removeJavaBreakpointListener((IJavaBreakpointListener) processor);
        }
        processor = null;
        dirtyListener = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.MultiPageEditorPart#initializePageSwitching()
     */
    protected void initializePageSwitching() {

    }

    public abstract AbstractTalendEditor getDesignerEditor();

}
