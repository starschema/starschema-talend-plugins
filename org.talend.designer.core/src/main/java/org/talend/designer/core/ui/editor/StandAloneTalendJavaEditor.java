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
package org.talend.designer.core.ui.editor;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.corext.refactoring.rename.JavaRenameProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameCompilationUnitProcessor;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.progress.WorkbenchJob;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.FileItem;
import org.talend.core.model.properties.Information;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.model.utils.DesignerColorUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.ILastVersionChecker;
import org.talend.core.ui.IUIRefresher;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ui.action.SaveAsRoutineAction;
import org.talend.designer.core.ui.action.SaveAsSQLPatternAction;
import org.talend.designer.core.ui.views.problems.Problems;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.editor.RepositoryEditorInput;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.views.IJobSettingsView;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * Stand alone Perl editor.<br/>
 * 
 */
public class StandAloneTalendJavaEditor extends CompilationUnitEditor implements IUIRefresher, ILastVersionChecker {

    public static final String ID = "org.talend.designer.core.ui.editor.StandAloneTalendJavaEditor"; //$NON-NLS-1$

    private RepositoryEditorInput rEditorInput;

    private Boolean isEditable = true;

    private Color bgColorForReadOnlyItem;

    private Color bgColorForEditabeItem;

    /**
     * DOC smallet Comment method "getRepositoryFactory".
     */
    private IProxyRepositoryFactory getRepositoryFactory() {
        return DesignerPlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
    }

    @Override
    public boolean isSaveAsAllowed() {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            return false;
        }
        return getRepositoryFactory().getStatus(item).isEditable();
    }

    @Override
    public boolean isEditable() {
        if (!getRepositoryFactory().getStatus(item).isEditable()) {
            getSourceViewer().getTextWidget().setDragDetect(false);
            isEditable = false;
        }
        return !rEditorInput.isReadOnly() && getRepositoryFactory().getStatus(item).isEditable() && isLastVersion(item);
    }

    @Override
    public void doSetInput(IEditorInput input) throws CoreException {
        // Lock the process :
        IRepositoryService service = DesignerPlugin.getDefault().getRepositoryService();
        IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();

        if (input instanceof RepositoryEditorInput) {
            rEditorInput = (RepositoryEditorInput) input;
        } else {
            FileEditorInput fileInput = (FileEditorInput) input;
            rEditorInput = new RepositoryEditorInput(fileInput.getFile(), rEditorInput.getItem());
        }
        if (rEditorInput.getRepositoryNode() == null) {
            rEditorInput.setRepositoryNode(null); // retrieve node
        }

        try {
            RepositoryManager.getRepositoryView().refresh(rEditorInput.getRepositoryNode());
            // see bug 1321
            item = (FileItem) rEditorInput.getItem();
            if (!rEditorInput.isReadOnly()) {
                if (getRepositoryFactory().getStatus(item).isPotentiallyEditable()) {
                    item.getProperty().eAdapters().add(dirtyListener);
                    repFactory.lock(item);
                }
            } else {
                rEditorInput.getFile().setReadOnly(true);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        rEditorInput.getFile().refreshLocal(IResource.DEPTH_ONE, null);
        super.doSetInput(rEditorInput);
        setName();
    }

    private void setName() {
        IRepositoryView viewPart = (IRepositoryView) getSite().getPage().findView(IRepositoryView.VIEW_ID);
        ILabelProvider labelProvider = (ILabelProvider) viewPart.getViewer().getLabelProvider();
        RepositoryNode repositoryNode = rEditorInput.getRepositoryNode();
        if (repositoryNode != null) {
            setTitleImage(labelProvider.getImage(repositoryNode.getObject()));
            setPartName(labelProvider.getText(repositoryNode.getObject()));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#getTitleImage()
     */
    @Override
    public Image getTitleImage() {
        if (item != null) {
            IRepositoryView viewPart = (IRepositoryView) getSite().getPage().findView(IRepositoryView.VIEW_ID);
            ILabelProvider labelProvider = (ILabelProvider) viewPart.getViewer().getLabelProvider();
            RepositoryNode repositoryNode = rEditorInput.getRepositoryNode();
            if (repositoryNode != null) {
                return labelProvider.getImage(repositoryNode.getObject());
            }
        }
        return super.getTitleImage();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#getPartName()
     */
    @Override
    public String getPartName() {
        if (item != null) {
            IRepositoryView viewPart = (IRepositoryView) getSite().getPage().findView(IRepositoryView.VIEW_ID);
            ILabelProvider labelProvider = (ILabelProvider) viewPart.getViewer().getLabelProvider();
            return labelProvider.getText(item.getProperty());
        }
        return super.getPartName();
    }

    @Override
    public void dispose() {
        // remove the Runtines .java file in the .Java Project.
        // try {
        // rEditorInput.getFile().delete(true, null);
        // } catch (CoreException e1) {
        // RuntimeExceptionHandler.process(e1);
        // }
        super.dispose();
        // Unlock the process :
        IRepositoryService service = DesignerPlugin.getDefault().getRepositoryService();
        IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();
        try {
            item.getProperty().eAdapters().remove(dirtyListener);
            repFactory.unlock(item);
        } catch (PersistenceException e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        } catch (LoginException e) {
            ExceptionHandler.process(e);
        }
        RepositoryNode repositoryNode = rEditorInput.getRepositoryNode();
        if (repositoryNode != null) {
            if (repFactory.getStatus(item) == ERepositoryStatus.DELETED) {
                RepositoryManager.refreshDeletedNode(null);
            } else {
                RepositoryManager.refresh(repositoryNode.getObjectType());
            }
        }
        if (!isEditable)
            rEditorInput.getFile().setReadOnly(false);
        // force clean jobsettings
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow != null) {
            IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
            if (activePage != null) {
                IViewPart findView = activePage.findView(IJobSettingsView.ID);
                if (findView != null) {
                    IJobSettingsView jobsetting = (IJobSettingsView) findView;
                    if (!jobsetting.isCleaned()) {
                        jobsetting.cleanDisplay();
                    }
                }
            }
        }

        // dispose custom color
        if (bgColorForReadOnlyItem != null) {
            bgColorForReadOnlyItem.dispose();
        }
        if (bgColorForEditabeItem != null) {
            bgColorForEditabeItem.dispose();
        }
    }

    @Override
    public boolean isDirty() {
        return propertyIsDirty || super.isDirty();
    }

    @Override
    protected void editorSaved() {

    }

    @Override
    public void doSave(final IProgressMonitor monitor) {
        EList adapters = item.getProperty().eAdapters();
        adapters.remove(dirtyListener);
        super.doSave(monitor);

        try {
            ByteArray byteArray = item.getContent();
            byteArray.setInnerContentFromFile(((FileEditorInput) getEditorInput()).getFile());
            IRepositoryService service = DesignerPlugin.getDefault().getRepositoryService();
            final IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();

            try {
                CorePlugin.getDefault().getRunProcessService().getJavaProject().getProject()
                        .build(IncrementalProjectBuilder.AUTO_BUILD, null);
            } catch (CoreException e1) {
                ExceptionHandler.process(e1);
            }
            // check syntax error
            addProblems();
            String name = "Save Routine";
            RepositoryWorkUnit<Object> repositoryWorkUnit = new RepositoryWorkUnit<Object>(name, this) {

                @Override
                protected void run() throws LoginException, PersistenceException {
                    refreshJobAndSave(repFactory);
                }
            };
            repFactory.executeRepositoryWorkUnit(repositoryWorkUnit);

            // for bug 11930: Unable to save Routines.* in db project

            // repFactory.save(item);
            // startRefreshJob(repFactory);

        } catch (Exception e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }

    }

    private void refreshJobAndSave(final IProxyRepositoryFactory repFactory) {
        try {
            // cause it to update MaxInformationLevel
            repFactory.save(item);
        } catch (Exception e) {
        }
        // update image in repository
        RepositoryManager.refreshSavedNode(rEditorInput.getRepositoryNode());
        // update editor image
        setTitleImage(getTitleImage());

    }

    private void startRefreshJob(final IProxyRepositoryFactory repFactory) {
        Job refreshJob = new WorkbenchJob("") {//$NON-NLS-1$

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
             */
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                try {
                    CorePlugin.getDefault().getRunProcessService().getJavaProject().getProject()
                            .build(IncrementalProjectBuilder.AUTO_BUILD, null);
                } catch (CoreException e1) {
                    ExceptionHandler.process(e1);
                }
                // check syntax error
                addProblems();

                try {
                    // cause it to update MaxInformationLevel
                    repFactory.save(item.getProperty());
                } catch (Exception e) {

                }

                // add dirtyListener
                propertyIsDirty = false;
                EList adapters = item.getProperty().eAdapters();
                // if (!(item instanceof RoutineItem)) {
                adapters.add(dirtyListener);
                firePropertyChange(IEditorPart.PROP_DIRTY);
                // }

                // update image in repository
                RepositoryManager.refreshSavedNode(rEditorInput.getRepositoryNode());
                // update editor image
                setTitleImage(getTitleImage());
                return Status.OK_STATUS;
            }
        };
        refreshJob.setSystem(true);

        refreshJob.schedule(300);

    }

    /**
     * add routine compilation errors into problems view.
     */
    private void addProblems() {
        List<Information> informations = Problems.addRoutineFile(rEditorInput.getFile(), item.getProperty());
        item.getProperty().getInformations().clear();
        item.getProperty().getInformations().addAll(informations);
        Problems.refreshProblemTreeView();
    }

    private FileItem item;

    private boolean propertyIsDirty;

    private final AdapterImpl dirtyListener = new AdapterImpl() {

        @Override
        public void notifyChanged(Notification notification) {
            if (notification.getEventType() != Notification.REMOVING_ADAPTER) {
                propertyIsDirty = true;
                Display.getDefault().syncExec(new Runnable() {

                    public void run() {
                        firePropertyChange(IEditorPart.PROP_DIRTY);
                    }
                });
            }
        }
    };

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.ui.javaeditor.JavaEditor#getCorrespondingElement(org.eclipse.jdt.core.IJavaElement)
     */
    @Override
    protected IJavaElement getCorrespondingElement(IJavaElement element) {
        return element;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.ui.javaeditor.JavaEditor#getElementAt(int)
     */
    @Override
    protected IJavaElement getElementAt(int offset) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.IUIRefresher#refreshName()
     */
    public void refreshName() {

        ICompilationUnit unit = (ICompilationUnit) this.getInputJavaElement();
        String newName = item.getProperty().getLabel();
        propertyIsDirty = false;
        try {
            boolean noError = true;
            String newName2 = newName + SuffixConstants.SUFFIX_STRING_java;
            if (item instanceof RoutineItem && !unit.getElementName().equals(newName2)) {

                JavaRenameProcessor processor = new RenameCompilationUnitProcessor(unit);
                processor.setNewElementName(newName2);
                RenameRefactoring ref = new RenameRefactoring(processor);
                final PerformRefactoringOperation operation = new PerformRefactoringOperation(ref,
                        CheckConditionsOperation.ALL_CONDITIONS);

                IRunnableWithProgress r = new IRunnableWithProgress() {

                    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        Display.getDefault().asyncExec(new Runnable() {

                            public void run() {
                                try {
                                    operation.run(monitor);
                                } catch (CoreException e) {
                                    ExceptionHandler.process(e);
                                }
                            }
                        });

                    }
                };

                PlatformUI.getWorkbench().getProgressService().run(true, true, r);
                RefactoringStatus conditionStatus = operation.getConditionStatus();
                if (conditionStatus != null && conditionStatus.hasError()) {
                    String errorMessage = "Rename " + unit.getElementName() + " to " + newName + " has errors!"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    RefactoringStatusEntry[] entries = conditionStatus.getEntries();
                    for (int i = 0; i < entries.length; i++) {
                        RefactoringStatusEntry entry = entries[i];
                        errorMessage += "\n>>>" + entry.getMessage(); //$NON-NLS-1$
                    }
                    MessageDialog.openError(this.getSite().getShell(), "Warning", errorMessage); //$NON-NLS-1$
                    noError = false;
                }
            }
            if (noError) {
                doSave(null);
            }
            setName();
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    // reference:Process.isLastVersion(Item item)
    public boolean isLastVersion(Item item) {
        if (item.getProperty() != null) {
            try {
                List<IRepositoryViewObject> allVersion = ProxyRepositoryFactory.getInstance().getAllVersion(
                        item.getProperty().getId());

                if (allVersion == null || allVersion.isEmpty()) {
                    return false;
                }
                String lastVersion = VersionUtils.DEFAULT_VERSION;

                for (IRepositoryViewObject object : allVersion) {
                    if (VersionUtils.compareTo(object.getVersion(), lastVersion) > 0) {
                        lastVersion = object.getVersion();
                    }
                }
                if (VersionUtils.compareTo(item.getProperty().getVersion(), lastVersion) == 0) {
                    return true;
                }

            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        return false;
    }

    public boolean isEditorInputModifiable() {
        return isEditable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.ILastVersionChecker#setLastVersion(java.lang.Boolean)
     */
    public void setLastVersion(Boolean lastVersion) {
        // not used yet
    }

    protected void createActions() {
        super.createActions();
        getAction(IJavaEditorActionDefinitionIds.SHOW_IN_BREADCRUMB).setEnabled(false);
    }

    @Override
    protected void initializeViewerColors(ISourceViewer viewer) {
        super.initializeViewerColors(viewer);

        StyledText styledText = viewer.getTextWidget();

        if (!isEditable()) {
            bgColorForReadOnlyItem = new Color(styledText.getDisplay(), DesignerColorUtils.getPreferenceReadonlyRGB(
                    DesignerColorUtils.READONLY_BACKGROUND_COLOR_NAME, DesignerColorUtils.DEFAULT_READONLY_COLOR));
            styledText.setBackground(bgColorForReadOnlyItem);
        } else {
            bgColorForEditabeItem = new Color(styledText.getDisplay(), DesignerColorUtils.getPreferenceDesignerEditorRGB(
                    DesignerColorUtils.JOBDESIGNER_EGITOR_BACKGROUND_COLOR_NAME, DesignerColorUtils.DEFAULT_EDITOR_COLOR));
            styledText.setBackground(bgColorForEditabeItem);
        }
    }

    @Override
    public void doSaveAs() {
        ERepositoryObjectType type = this.rEditorInput.getRepositoryNode().getObject().getRepositoryObjectType();
        if (type == ERepositoryObjectType.ROUTINES) {
            SaveAsRoutineAction saveAsAction = new SaveAsRoutineAction(this);
            saveAsAction.run();
        } else if (type == ERepositoryObjectType.SQLPATTERNS) {
            SaveAsSQLPatternAction saveAsAction = new SaveAsSQLPatternAction(this);
            saveAsAction.run();
        }

    }
}
