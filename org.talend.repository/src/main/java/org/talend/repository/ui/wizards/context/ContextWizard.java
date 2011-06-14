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
package org.talend.repository.ui.wizards.context;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.ui.images.ECoreImage;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.wizards.CheckLastVersionRepositoryWizard;
import org.talend.repository.ui.wizards.PropertiesWizardPage;
import org.talend.repository.ui.wizards.metadata.connection.Step0WizardPage;

/**
 * FileWizard present the FileForm. Use to create a new connection to a DB.
 */

public class ContextWizard extends CheckLastVersionRepositoryWizard implements INewWizard {

    private static Logger log = Logger.getLogger(ContextWizard.class);

    private PropertiesWizardPage contextWizardPage0;

    private Property contextProperty;

    private ContextItem contextItem;

    private IContextManager contextManager;

    private String originaleObjectLabel;

    private String originalVersion;

    private String originalPurpose;

    private String originalDescription;

    private String originalStatus;

    ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();;

    /**
     * 
     * this constructor only for context mode. (feature 2449)
     */
    public ContextWizard(final String contextName, boolean creation, ISelection selection, final List<IContextParameter> paramList) {
        this(PlatformUI.getWorkbench(), creation, (RepositoryNode) ((IStructuredSelection) selection).getFirstElement(), false);
        initContextMode(contextName, paramList);
    }

    private void initContextMode(final String contextName, final List<IContextParameter> paramList) {
        if (creation && contextName != null && contextProperty != null) {
            contextProperty.setLabel(contextName);
        }
        if (paramList != null && contextManager != null) {
            for (IContext context : contextManager.getListContext()) {
                for (IContextParameter param : paramList) {
                    boolean existed = false;
                    for (IContextParameter existedParam : context.getContextParameterList()) {
                        if (existedParam.getName().equals(param.getName())) {
                            existed = true;
                            break;
                        }
                    }
                    if (!existed) {
                        IContextParameter toAdd = param.clone();
                        toAdd.setContext(context);
                        context.getContextParameterList().add(toAdd);
                    }
                }

            }
        }
    }

    /**
     * Constructor for FileWizard.
     * 
     * @param workbench
     * @param selection
     * @param strings
     */
    public ContextWizard(IWorkbench workbench, boolean creation, RepositoryNode repositoryNode, boolean forceReadOnly) {
        super(workbench, creation, forceReadOnly);
        if (repositoryNode != null) {
            IRepositoryService service = CorePlugin.getDefault().getRepositoryService();
            pathToSave = service.getRepositoryPath(repositoryNode);
        } else { // set root
            pathToSave = new Path(""); //$NON-NLS-1$
        }
        setWindowTitle(""); //$NON-NLS-1$
        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.CONTEXT_WIZ));
        setNeedsProgressMonitor(true);

        if (creation) {
            contextItem = PropertiesFactory.eINSTANCE.createContextItem();
            contextProperty = PropertiesFactory.eINSTANCE.createProperty();
            contextProperty.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                    .getUser());
            contextProperty.setVersion(VersionUtils.DEFAULT_VERSION);
            contextProperty.setStatusCode(""); //$NON-NLS-1$

            contextItem.setProperty(contextProperty);

            contextManager = new JobContextManager();
        } else {

            RepositoryNode node = repositoryNode;
            if (node == null && selection != null) {
                node = (RepositoryNode) ((IStructuredSelection) selection).getFirstElement();
            }
            if (node != null) {
                IRepositoryViewObject object;
                if (node.getObject() instanceof RepositoryObject)
                    object = (RepositoryObject) node.getObject();
                else {
                    object = (RepositoryViewObject) node.getObject();
                }
                setRepositoryObject(object);
                isRepositoryObjectEditable();
                initLockStrategy();

                contextItem = (ContextItem) object.getProperty().getItem();
                originaleObjectLabel = contextItem.getProperty().getLabel();
                this.originalVersion = contextItem.getProperty().getVersion();
                this.originalDescription = contextItem.getProperty().getDescription();
                this.originalPurpose = contextItem.getProperty().getPurpose();
                this.originalStatus = contextItem.getProperty().getStatusCode();
                contextProperty = contextItem.getProperty();
                contextManager = new JobContextManager(contextItem.getContext(), contextItem.getDefaultContext());
            }
        }
        initLockStrategy();
    }

    /**
     * Adding the page to the wizard.
     */
    public void addPages() {
        setWindowTitle(Messages.getString("ContextWizard.Title")); //$NON-NLS-1$
        contextWizardPage0 = new Step0WizardPage(contextProperty, pathToSave, ERepositoryObjectType.CONTEXT,
                !isRepositoryObjectEditable(), creation);
        contextWizardPage0.setTitle(Messages.getString("ContextWizard.step0Title")); //$NON-NLS-1$
        contextWizardPage0.setDescription(Messages.getString("ContextWizard.step0Description")); //$NON-NLS-1$
        addPage(contextWizardPage0);
        if (creation || !isRepositoryObjectEditable()) {
            contextWizardPage0.setPageComplete(false);
        }

        ContextPage contextPage = new ContextPage("test", contextManager, !isRepositoryObjectEditable()); //$NON-NLS-1$
        contextPage.setTitle(Messages.getString("ContextWizard.contextPageTitle")); //$NON-NLS-1$
        contextPage.setDescription(Messages.getString("ContextWizard.contextPageDescription")); //$NON-NLS-1$
        addPage(contextPage);
        if (!isRepositoryObjectEditable()) {
            contextPage.setPageComplete(false);
        }
    }

    /**
     * This method determine if the 'Finish' button is enable This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it using wizard as execution context.
     */
    public boolean performFinish() {
        // TimeMeasure.display = true;
        // TimeMeasure.measureActive = true;
        // TimeMeasure.begin("performFinish");
        boolean formIsPerformed = contextManager.getListContext().size() != 0;
        // if (delimitedFileWizardPage3 == null) {
        // formIsPerformed = delimitedFileWizardPage2.isPageComplete();
        // } else {
        // formIsPerformed = delimitedFileWizardPage3.isPageComplete();
        // }

        if (formIsPerformed) {
            // IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            try {
                if (creation) {
                    String nextId = factory.getNextId();
                    contextProperty.setId(nextId);
                    contextManager.saveToEmf(contextItem.getContext());
                    contextItem.setDefaultContext(contextManager.getDefaultContext().getName());
                    factory.create(contextItem, contextWizardPage0.getDestinationPath());

                } else {
                    contextItem.getContext().clear();
                    contextManager.saveToEmf(contextItem.getContext());
                    contextItem.setDefaultContext(contextManager.getDefaultContext().getName());
                    if (contextManager instanceof JobContextManager) {
                        JobContextManager manager = (JobContextManager) contextManager;
                        if (manager.isModified()) {
                            // the function has moved to UpdateContextParameterCommand for update manager(bug 3993).
                            // update the tRunJob reference
                            // UpdateContextReferenceHelper.updateJobContextReference((JobContextManager)
                            // contextManager,
                            // contextItem);

                            // update
                            RepositoryUpdateManager.updateContext((JobContextManager) contextManager, contextItem);
                        }
                    }
                    // contextItem.setProperty(ProxyRepositoryFactory.getInstance().getUptodateProperty(contextItem.getProperty()));
                    factory.save(contextItem);
                    updateRelatedView();

                }
                closeLockStrategy();
                ProxyRepositoryFactory.getInstance().saveProject(ProjectManager.getInstance().getCurrentProject());
                // TimeMeasure.end("performFinish");
            } catch (PersistenceException e) {
                String detailError = e.toString();
                new ErrorDialogWidthDetailArea(getShell(), PID, Messages.getString("CommonWizard.persistenceException"), //$NON-NLS-1$
                        detailError);
                log.error(Messages.getString("CommonWizard.persistenceException") + "\n" + detailError); //$NON-NLS-1$ //$NON-NLS-2$
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean performCancel() {
        contextItem.getProperty().setVersion(this.originalVersion);
        contextItem.getProperty().setLabel(this.originaleObjectLabel);
        contextItem.getProperty().setDescription(this.originalDescription);
        contextItem.getProperty().setPurpose(this.originalPurpose);
        contextItem.getProperty().setStatusCode(this.originalStatus);
        return super.performCancel();
    }

    /**
     * DOC bqian Comment method "updateProcessContextManager".
     */
    private void updateRelatedView() {
        IDesignerCoreService designerCoreService = RepositoryPlugin.getDefault().getDesignerCoreService();

        designerCoreService.switchToCurContextsView();
        // for tRunJob component
        designerCoreService.switchToCurComponentSettingsView();
        // for 2608
        designerCoreService.switchToCurJobSettingsView();
    }

    /**
     * We will accept the selection in the workbench to see if we can initialize from it.
     * 
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(final IWorkbench workbench, final IStructuredSelection selection2) {
        this.selection = selection2;
    }

    public ContextItem getContextItem() {
        return this.contextItem;
    }

}
