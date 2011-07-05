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
package org.talend.repository;

import java.beans.PropertyChangeEvent;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.utils.PasswordHelper;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.general.ConnectionBean;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.SAPConnection;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.metadata.designerproperties.ComponentToRepositoryProperty;
import org.talend.core.model.migration.IMigrationToolService;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.SAPConnectionItem;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.properties.User;
import org.talend.core.model.properties.impl.PropertiesFactoryImpl;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.SVNConstant;
import org.talend.core.prefs.PreferenceManipulator;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.RepositoryFactoryProvider;
import org.talend.core.repository.model.ResourceModelUtils;
import org.talend.core.repository.utils.ProjectHelper;
import org.talend.core.repository.utils.RepositoryPathProvider;
import org.talend.core.ui.DisableLanguageActions;
import org.talend.core.ui.IBRMSProviderService;
import org.talend.core.ui.IEBCDICProviderService;
import org.talend.core.ui.IFTPProviderService;
import org.talend.core.ui.IHL7ProviderService;
import org.talend.core.ui.IHeaderFooterProviderService;
import org.talend.core.ui.IMDMProviderService;
import org.talend.core.ui.ISAPProviderService;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.cwm.helper.ModelElementHelper;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.model.ComponentsFactoryProvider;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.ProjectNodeHelper;
import org.talend.repository.model.ProjectRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.plugin.integration.BindingActions;
import org.talend.repository.plugin.integration.SwitchProjectAction;
import org.talend.repository.ui.actions.metadata.AbstractCreateTableAction;
import org.talend.repository.ui.actions.metadata.CreateTableAction;
import org.talend.repository.ui.actions.sqlpattern.CreateSqlpatternAction;
import org.talend.repository.ui.actions.sqlpattern.EditSqlpatternAction;
import org.talend.repository.ui.dialog.ContextRepositoryReviewDialog;
import org.talend.repository.ui.login.LoginDialog;
import org.talend.repository.ui.login.connections.ConnectionUserPerReader;
import org.talend.repository.ui.utils.ColumnNameValidator;
import org.talend.repository.ui.utils.DBConnectionContextUtils;
import org.talend.repository.ui.views.IRepositoryView;
import org.talend.repository.ui.views.RepositoryView;
import org.talend.repository.ui.wizards.RepositoryWizard;
import org.talend.repository.ui.wizards.metadata.connection.database.DatabaseWizard;
import org.talend.repository.ui.wizards.metadata.connection.files.delimited.DelimitedFileWizard;
import org.talend.repository.ui.wizards.metadata.connection.files.excel.ExcelFileWizard;
import org.talend.repository.ui.wizards.metadata.connection.files.ldif.LdifFileWizard;
import org.talend.repository.ui.wizards.metadata.connection.files.positional.FilePositionalWizard;
import org.talend.repository.ui.wizards.metadata.connection.files.regexp.RegexpFileWizard;
import org.talend.repository.ui.wizards.metadata.connection.files.salesforce.SalesforceSchemaWizard;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.XmlFileWizard;
import org.talend.repository.ui.wizards.metadata.connection.genericshema.GenericSchemaWizard;
import org.talend.repository.ui.wizards.metadata.connection.ldap.LDAPSchemaWizard;
import org.talend.repository.ui.wizards.metadata.connection.wsdl.WSDLSchemaWizard;

;

/**
 * DOC qian class global comment. Detailled comment <br/>
 * 
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (星期五, 29 九月 2006) nrousseau $
 * 
 */

public class RepositoryService implements IRepositoryService {

    private GenericSchemaWizard genericSchemaWizard = null;

    private static final String PERSPECTIVE_DI_ID = "org.talend.rcp.perspective"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IRepositoryService#getComponentsFactory()
     */
    public IComponentsFactory getComponentsFactory() {
        return ComponentsFactoryProvider.getInstance();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IRepositoryService#getPathFileName(java.lang.String, java.lang.String)
     */
    public IPath getPathFileName(String folderName, String fileName) {
        return RepositoryPathProvider.getPathFileName(folderName, fileName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IRepositoryService#getProxyRepositoryFactory()
     */
    public IProxyRepositoryFactory getProxyRepositoryFactory() {
        return ProxyRepositoryFactory.getInstance();
    }

    public IPath getRepositoryPath(IRepositoryNode node) {
        return RepositoryNodeUtilities.getPath((RepositoryNode) node);
    }

    ChangeProcessor changeProcessor = new ChangeProcessor();

    /*
     * (non-Javadoc)
     * 
     * @seeorg.talend.repository.model.IRepositoryService#registerRepositoryChangedListener(org.talend.repository.
     * IRepositoryChangedListener)
     */
    public void registerRepositoryChangedListener(IRepositoryChangedListener listener) {
        changeProcessor.addRepositoryChangedListener(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.repository.model.IRepositoryService#registerRepositoryChangedListenerAsFirst(org.talend.repository
     * .IRepositoryChangedListener)
     */
    public void registerRepositoryChangedListenerAsFirst(IRepositoryChangedListener listener) {
        changeProcessor.registerRepositoryChangedListenerAsFirst(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.talend.repository.model.IRepositoryService#removeRepositoryChangedListener(org.talend.repository.
     * IRepositoryChangedListener)
     */
    public void removeRepositoryChangedListener(IRepositoryChangedListener listener) {
        changeProcessor.removeRepositoryChangedListener(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.repository.model.IRepositoryService#repositoryChanged(org.talend.repository.RepositoryElementDelta)
     */
    public void repositoryChanged(IRepositoryElementDelta delta) {
        changeProcessor.repositoryChanged(delta);
    }

    // This method is used for the Action in RepositoryView to synchronize the sqlBuilder.
    // see DataBaseWizard, DatabaseTableWizard, AContextualAction
    public void notifySQLBuilder(List<IRepositoryViewObject> list) {
        IRepositoryChangedListener listener = (IRepositoryChangedListener) RepositoryView.show();
        removeRepositoryChangedListener(listener);
        for (Iterator<IRepositoryViewObject> iter = list.iterator(); iter.hasNext();) {
            IRepositoryViewObject element = iter.next();
            repositoryChanged(new RepositoryElementDelta(element));
        }
        registerRepositoryChangedListenerAsFirst(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IRepositoryService#validateColumnName(java.lang.String, int)
     */
    public String validateColumnName(String columnName, int index) {
        return ColumnNameValidator.validateColumnNameFormat(columnName, index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IRepositoryService#getGenericSchemaWizardDialog(org.eclipse.swt.widgets.Shell,
     * org.eclipse.ui.IWorkbench, boolean, org.eclipse.jface.viewers.ISelection, java.lang.String[], boolean)
     */
    public WizardDialog getGenericSchemaWizardDialog(Shell shell, IWorkbench workbench, boolean creation, ISelection selection,
            String[] existingNames, boolean isSinglePageOnly) {

        genericSchemaWizard = new GenericSchemaWizard(workbench, creation, selection, existingNames, isSinglePageOnly);
        return new WizardDialog(shell, genericSchemaWizard);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IRepositoryService#getPropertyFromWizardDialog()
     */
    public Property getPropertyFromWizardDialog() {
        if (this.genericSchemaWizard != null) {
            return this.genericSchemaWizard.getConnectionProperty();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IRepositoryService#getPathForSaveAsGenericSchema()
     */
    public IPath getPathForSaveAsGenericSchema() {
        if (this.genericSchemaWizard != null) {
            return this.genericSchemaWizard.getPathForSaveAsGenericSchema();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IRepositoryService#openLoginDialog()
     */
    public void openLoginDialog() {
        if (isloginDialogDisabled()) {
            return;
        }

        if (CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY) != null) {
            return;
        }

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        boolean logged = false;
        LoginDialog loginDialog = new LoginDialog(shell);
        // PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(true);
        logged = loginDialog.open() == LoginDialog.OK;
        if (logged) {

            // addCommand();
            new DisableLanguageActions().earlyStartup();

            new BindingActions().bind();

            IMigrationToolService toolService = CorePlugin.getDefault().getMigrationToolService();
            toolService.executeMigration(SwitchProjectAction.PLUGIN_MODEL);

            IRunProcessService runService = CorePlugin.getDefault().getRunProcessService();
            runService.deleteAllJobs(SwitchProjectAction.PLUGIN_MODEL);

            CorePlugin.getDefault().getCodeGeneratorService().initializeTemplates();
            CorePlugin.getDefault().getDesignerCoreService()
                    .synchronizeDesignerUI(new PropertyChangeEvent(this, ComponentUtilities.NORMAL, null, null));

        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IRepositoryService#openLoginDialog(org.eclipse.swt.widgets.Shell, boolean)
     */
    public boolean openLoginDialog(Shell shell, boolean inuse) {
        if (isloginDialogDisabled()) {
            return true;
        }
        LoginDialog loginDialog = new LoginDialog(shell, inuse);
        boolean logged = loginDialog.open() == LoginDialog.OK;
        return logged;

    }

    private boolean isloginDialogDisabled() {
        boolean startable = Boolean.parseBoolean(System.getProperty("talend.project.Startable")); //$NON-NLS-1$
        PreferenceManipulator preferenceManipulator = new PreferenceManipulator();
        ConnectionBean lastBean = null;
        if (startable) {
            final ConnectionUserPerReader instance = ConnectionUserPerReader.getInstance();
            instance.forceReadConnections();
            final String lastConncetion = ConnectionUserPerReader.getInstance().readLastConncetion();
            for (ConnectionBean bean : instance.readConnections()) {
                if (bean.getName().equals(lastConncetion)) {
                    lastBean = bean;
                    break;
                }
            }
        }

        if (ArrayUtils.contains(Platform.getApplicationArgs(), "--disableLoginDialog") || startable) {
            boolean deleteProjectIfExist = ArrayUtils.contains(Platform.getApplicationArgs(), "--deleteProjectIfExist");
            IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                    IBrandingService.class);
            brandingService.getBrandingConfiguration().setUseProductRegistration(false);
            ProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();

            String projectName = "AUTO_LOGIN_PROJECT";
            int index = ArrayUtils.indexOf(Platform.getApplicationArgs(), "-project");
            if (index > 0) {
                if (index + 1 < Platform.getApplicationArgs().length) {
                    projectName = Platform.getApplicationArgs()[index + 1];
                }
            }

            String language = "java";
            index = ArrayUtils.indexOf(Platform.getApplicationArgs(), "-language");
            if (index > 0) {
                if (index + 1 < Platform.getApplicationArgs().length) {
                    language = Platform.getApplicationArgs()[index + 1];
                }
            }

            String login = "auto@login.com";
            index = ArrayUtils.indexOf(Platform.getApplicationArgs(), "-login");
            if (index > 0) {
                if (index + 1 < Platform.getApplicationArgs().length) {
                    login = Platform.getApplicationArgs()[index + 1];
                }
            }

            String password = "";
            index = ArrayUtils.indexOf(Platform.getApplicationArgs(), "-password");
            if (index > 0) {
                if (index + 1 < Platform.getApplicationArgs().length) {
                    password = Platform.getApplicationArgs()[index + 1];
                }
            }

            String branch = null;

            if (startable && lastBean != null) {
                final String lastProject = preferenceManipulator.getLastProject();
                if (lastProject != null) {
                    projectName = lastProject;
                }
                final String lastSVNBranch = preferenceManipulator.getLastSVNBranch();
                if (lastSVNBranch != null) {
                    branch = lastSVNBranch;
                }
                final String lastUser = lastBean.getUser();
                if (lastUser != null) {
                    login = lastUser;
                }
                final String lastPass = lastBean.getPassword();
                if (lastPass != null) {
                    password = lastPass;
                }

            }
            User userInfo = PropertiesFactoryImpl.eINSTANCE.createUser();
            userInfo.setLogin(login);
            try {
                userInfo.setPassword(PasswordHelper.encryptPasswd(password));
            } catch (NoSuchAlgorithmException e) {
                ExceptionHandler.process(e);
            }

            try {
                ConnectionBean bean = ConnectionBean.getDefaultConnectionBean();
                if (startable && lastBean != null) {
                    bean = lastBean;
                }
                Context ctx = CorePlugin.getContext();
                RepositoryContext repositoryContext = new RepositoryContext();
                repositoryContext.setUser(userInfo);
                repositoryContext.setClearPassword(password);
                repositoryContext.setFields(bean.getDynamicFields());
                String branchKey = IProxyRepositoryFactory.BRANCH_SELECTION + SVNConstant.UNDER_LINE_CHAR + projectName;
                if (branch != null) {
                    repositoryContext.getFields().put(branchKey, branch);
                } else {
                    repositoryContext.getFields().put(branchKey, SVNConstant.EMPTY);
                }

                ctx.putProperty(Context.REPOSITORY_CONTEXT_KEY, repositoryContext);

                repositoryFactory.setRepositoryFactoryFromProvider(RepositoryFactoryProvider.getRepositoriyById(bean
                        .getRepositoryId()));
                Project project = null;
                for (Project p : repositoryFactory.readProject()) {
                    if (p.getLabel().equals(projectName) || p.getTechnicalLabel().equals(projectName)) {
                        project = p;
                        break;
                    }
                }
                if (!startable) {
                    if (deleteProjectIfExist && project != null) {
                        ResourceModelUtils.getProject(project).delete(true, new NullProgressMonitor());
                    }
                    if (project == null || deleteProjectIfExist) {
                        Project projectInfor = ProjectHelper.createProject(projectName, "", //$NON-NLS-1$
                                language, userInfo);
                        project = repositoryFactory.createProject(projectInfor);
                    }
                }
                repositoryContext.setProject(project);

                repositoryFactory.logOnProject(project, new NullProgressMonitor());
            } catch (PersistenceException e) {
                MessageBoxExceptionHandler.process(e, new Shell());
                repositoryFactory.logOffProject();
                return false;
            } catch (LoginException e) {
                MessageBoxExceptionHandler.process(e, new Shell());
                repositoryFactory.logOffProject();
                return false;
            } catch (BusinessException e) {
                MessageBoxExceptionHandler.process(e, new Shell());
                repositoryFactory.logOffProject();
                return false;
            } catch (CoreException e) {
                MessageBoxExceptionHandler.process(e, new Shell());
                repositoryFactory.logOffProject();
                return false;
            }

            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IRepositoryService#initializeForTalendStartupJob()
     */
    public void initializeForTalendStartupJob() {
        // do nothing now.

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IRepositoryService#initializeTalend()
     */
    public void initializePluginMode() {

        if (CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY) != null) {
            return;
        }
        openLoginDialog();
    }

    boolean rcpMode = false;

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IRepositoryService#isRCPMode()
     */
    public boolean isRCPMode() {
        return rcpMode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IRepositoryService#setRCPMode()
     */
    public void setRCPMode() {
        rcpMode = true;
    }

    public void openMetadataConnection(IRepositoryViewObject o, INode node) {
        final RepositoryNode realNode = RepositoryNodeUtilities.getRepositoryNode(o);
        openMetadataConnection(false, realNode, node);
    }

    public ConnectionItem openMetadataConnection(boolean creation, IRepositoryNode repoNode, INode node) {

        RepositoryNode realNode;
        if (repoNode instanceof RepositoryNode) {
            realNode = (RepositoryNode) repoNode;
            IWizard relatedWizard = null;
            ERepositoryObjectType objectType = null;
            if (creation) {
                objectType = realNode.getContentType();
            } else {
                objectType = realNode.getObjectType();
            }
            if (objectType.equals(ERepositoryObjectType.METADATA_CONNECTIONS)) {
                relatedWizard = new DatabaseWizard(PlatformUI.getWorkbench(), creation, realNode, null);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_DELIMITED)) {
                relatedWizard = new DelimitedFileWizard(PlatformUI.getWorkbench(), creation, realNode, null);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_LDIF)) {
                relatedWizard = new LdifFileWizard(PlatformUI.getWorkbench(), creation, realNode, null);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_POSITIONAL)) {
                relatedWizard = new FilePositionalWizard(PlatformUI.getWorkbench(), creation, realNode, null);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_REGEXP)) {
                relatedWizard = new RegexpFileWizard(PlatformUI.getWorkbench(), creation, realNode, null);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_XML)) {
                relatedWizard = new XmlFileWizard(PlatformUI.getWorkbench(), creation, realNode, null);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_GENERIC_SCHEMA)) {
                relatedWizard = new GenericSchemaWizard(PlatformUI.getWorkbench(), creation, realNode, null, true);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_WSDL_SCHEMA)) {
                relatedWizard = new WSDLSchemaWizard(PlatformUI.getWorkbench(), creation, realNode, null, false);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_LDAP_SCHEMA)) {
                relatedWizard = new LDAPSchemaWizard(PlatformUI.getWorkbench(), creation, realNode, null, false);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_EXCEL)) {
                relatedWizard = new ExcelFileWizard(PlatformUI.getWorkbench(), creation, realNode, null);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA)) {
                relatedWizard = new SalesforceSchemaWizard(PlatformUI.getWorkbench(), creation, realNode, null, false);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_EBCDIC)) {
                if (PluginChecker.isEBCDICPluginLoaded()) {
                    IEBCDICProviderService service = (IEBCDICProviderService) GlobalServiceRegister.getDefault().getService(
                            IEBCDICProviderService.class);
                    if (service != null) {
                        relatedWizard = service.newEbcdicWizard(PlatformUI.getWorkbench(), creation, realNode, null);
                    }
                }
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_HL7)) {
                if (PluginChecker.isHL7PluginLoaded()) {
                    IHL7ProviderService service = (IHL7ProviderService) GlobalServiceRegister.getDefault().getService(
                            IHL7ProviderService.class);
                    if (service != null) {
                        relatedWizard = service.newHL7Wizard(PlatformUI.getWorkbench(), creation, realNode, null);
                    }
                }
            } else if (objectType.equals(ERepositoryObjectType.METADATA_MDMCONNECTION)) {
                if (PluginChecker.isMDMPluginLoaded()) {
                    IMDMProviderService service = (IMDMProviderService) GlobalServiceRegister.getDefault().getService(
                            IMDMProviderService.class);
                    if (service != null) {
                        relatedWizard = service.newMDMWizard(PlatformUI.getWorkbench(), creation, realNode, null);
                    }
                }
            } else if (objectType.equals(ERepositoryObjectType.METADATA_SAPCONNECTIONS)) {
                if (PluginChecker.isSAPWizardPluginLoaded()) {
                    ISAPProviderService service = (ISAPProviderService) GlobalServiceRegister.getDefault().getService(
                            ISAPProviderService.class);
                    if (service != null) {
                        relatedWizard = service.newSAPWizard(PlatformUI.getWorkbench(), creation, realNode, null);
                    }
                }
            } else if (objectType.equals(ERepositoryObjectType.METADATA_HEADER_FOOTER)) {
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IHeaderFooterProviderService.class)) {
                    IHeaderFooterProviderService service = (IHeaderFooterProviderService) GlobalServiceRegister.getDefault()
                            .getService(IHeaderFooterProviderService.class);
                    if (service != null) {
                        relatedWizard = service.newHeaderFooterWizard(PlatformUI.getWorkbench(), creation, realNode, null);
                    }
                }
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_FTP)) {
                if (PluginChecker.isFTPPluginLoaded()) {
                    IFTPProviderService service = (IFTPProviderService) GlobalServiceRegister.getDefault().getService(
                            IFTPProviderService.class);
                    if (service != null) {
                        relatedWizard = service.newFTPWizard(PlatformUI.getWorkbench(), creation, realNode, null);
                    }
                }
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_BRMS)) {
                if (PluginChecker.isBRMSPluginLoaded()) {
                    IBRMSProviderService service = (IBRMSProviderService) GlobalServiceRegister.getDefault().getService(
                            IBRMSProviderService.class);
                    if (service != null) {
                        relatedWizard = service.newBRMSWizard(PlatformUI.getWorkbench(), creation, realNode);
                    }
                }
            }
            boolean changed = false;
            if (relatedWizard != null) {
                ConnectionItem connItem = null;
                if (node != null && relatedWizard instanceof RepositoryWizard) {// creation && node != null
                    connItem = ((RepositoryWizard) relatedWizard).getConnectionItem();
                    if (connItem != null) {
                        changed = ComponentToRepositoryProperty.setValue(connItem.getConnection(), node);
                    }
                }
                if (connItem != null && changed) {
                    // Open the Wizard
                    WizardDialog wizardDialog = new WizardDialog(Display.getCurrent().getActiveShell(), relatedWizard);
                    wizardDialog.setPageSize(600, 500);
                    wizardDialog.create();
                    if (wizardDialog.open() == wizardDialog.OK) {
                        return connItem;
                    }
                }
            }
        }
        return null;
    }

    public void openEditSchemaWizard(String value) {
        final RepositoryNode realNode = RepositoryNodeUtilities.getMetadataTableFromConnection(value);
        if (realNode != null) {
            AbstractCreateTableAction action = new CreateTableAction() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see org.talend.repository.ui.actions.AContextualAction#getSelection()
                 */
                @Override
                public ISelection getSelection() {
                    return new StructuredSelection(realNode);
                }
            };
            action.run();
        }
    }

    public DatabaseConnection cloneOriginalValueConnection(DatabaseConnection dbConn) {
        return DBConnectionContextUtils.cloneOriginalValueConnection(dbConn);
    }

    public DatabaseConnection cloneOriginalValueConnection(DatabaseConnection dbConn, boolean defaultContext) {
        return DBConnectionContextUtils.cloneOriginalValueConnection(dbConn, defaultContext, null);
    }

    public void setMetadataConnectionParameter(DatabaseConnection dbConn, IMetadataConnection metaConn) {
        DBConnectionContextUtils.setMetadataConnectionParameter(dbConn, metaConn);
    }

    public DatabaseConnection cloneOriginalValueConnection(DatabaseConnection dbConn, boolean defaultContext,
            String selectedContext) {
        return DBConnectionContextUtils.cloneOriginalValueConnection(dbConn, defaultContext, selectedContext);
    }

    public IEditorPart openSQLPatternEditor(SQLPatternItem item, boolean readOnly) {
        IEditorPart openSQLPatternEditor = null;
        try {
            openSQLPatternEditor = new EditSqlpatternAction().openSQLPatternEditor(item, readOnly);
        } catch (PartInitException e) {
            ExceptionHandler.process(e);
        } catch (SystemException e) {
            ExceptionHandler.process(e);
        }
        return openSQLPatternEditor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IRepositoryService#createSqlpattern()
     */
    public void createSqlpattern(String path, boolean isFromSqlPatternComposite) {
        new CreateSqlpatternAction(path, isFromSqlPatternComposite).run();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IRepositoryService#addRepositoryViewListener(org.eclipse.ui.ISelectionListener)
     */
    public void addRepositoryTreeViewListener(ISelectionChangedListener listener) {
        TreeViewer treeViewer = getRepositoryTreeView();
        if (treeViewer != null) {
            treeViewer.addSelectionChangedListener(listener);
        } else {
            RepositoryView.addPreparedListeners(listener);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.talend.repository.model.IRepositoryService#removeRepositoryTreeViewListener(org.eclipse.jface.viewers.
     * ISelectionChangedListener)
     */
    public void removeRepositoryTreeViewListener(ISelectionChangedListener listener) {
        TreeViewer treeViewer = getRepositoryTreeView();
        if (treeViewer != null) {
            treeViewer.removeSelectionChangedListener(listener);
        }
    }

    /**
     * yzhang Comment method "getRepositoryView".
     * 
     * @return
     */
    public TreeViewer getRepositoryTreeView() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page != null) {
            // bug 16594
            String perId = page.getPerspective().getId();
            if ((!"".equals(perId) || null != perId) && perId.equalsIgnoreCase(PERSPECTIVE_DI_ID)) {
                IViewPart view = page.findView(RepositoryView.ID);
                if (view == null) {
                    try {
                        view = page.showView(RepositoryView.ID);
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }
                if (view instanceof RepositoryView) {
                    return ((RepositoryView) view).getViewer();
                }
            }
            return null;
        } else {
            return null;
        }
    }

    public IPreferenceStore getRepositoryPreferenceStore() {
        return RepositoryPlugin.getDefault().getPreferenceStore();
    }

    public RepositoryNode getRepositoryNode(String id, boolean expanded) {
        return RepositoryNodeUtilities.getRepositoryNode(id, expanded);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.talend.repository.model.IRepositoryService#openRepositoryReviewDialog(org.talend.core.model.repository.
     * ERepositoryObjectType, java.lang.String)
     */
    public void openRepositoryReviewDialog(ERepositoryObjectType type, String repositoryType, List<IContextParameter> params,
            IContextManager contextManager) {
        ContextRepositoryReviewDialog dialog = new ContextRepositoryReviewDialog(new Shell(), type, params, contextManager);
        dialog.open();
    }

    /**
     * wzhang Comment method "getRootRepositoryNode".
     * 
     * @param type
     * @return
     */
    public RepositoryNode getRootRepositoryNode(ERepositoryObjectType type) {
        IRepositoryView view = RepositoryView.show();
        if (view != null) {
            ProjectRepositoryNode root = (ProjectRepositoryNode) view.getRoot();
            return root.getRootRepositoryNode(type);
        }
        return null;
    }

    public Action getRepositoryViewDoubleClickAction() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page != null) {
            // bug 16594
            String perId = page.getPerspective().getId();
            if ((!"".equals(perId) || null != perId) && perId.equalsIgnoreCase(PERSPECTIVE_DI_ID)) {
                IViewPart view = page.findView(RepositoryView.ID);
                if (view == null) {
                    try {
                        view = page.showView(RepositoryView.ID);

                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }
                RepositoryView repositoryView = (RepositoryView) view;

                return repositoryView.getDoubleClickAction();
            }
        }
        return null;
    }

    public void setInternalNodeHTMLMap(INode node, Map<String, Object> internalNodeHTMLMap) {
        IElementParameter propertyParam = null;
        IElementParameter functionParam = null;
        for (IElementParameter param : node.getElementParameters()) {

            if ("PROPERTY".equals(param.getName())) { //$NON-NLS-1$
                propertyParam = param.getChildParameters().get("REPOSITORY_PROPERTY_TYPE"); //$NON-NLS-1$
            }
            if ("SAP_FUNCTION".equals(param.getName())) { //$NON-NLS-1$
                functionParam = param;
            }
        }
        if (propertyParam != null && functionParam != null) {
            try {
                IRepositoryViewObject lastVersion = ProxyRepositoryFactory.getInstance().getLastVersion(
                        (String) propertyParam.getValue());
                if (lastVersion != null) {

                    Item item = lastVersion.getProperty().getItem();
                    if (item instanceof SAPConnectionItem) {
                        SAPConnectionItem sapItem = (SAPConnectionItem) item;
                        SAPConnection connection = (SAPConnection) sapItem.getConnection();
                        connection.getFuntions();
                        for (Object obj : connection.getFuntions()) {
                            if (obj instanceof SAPFunctionUnit) {
                                SAPFunctionUnit function = (SAPFunctionUnit) obj;
                                String functionName = (String) functionParam.getValue();
                                if (function.getName().equals(functionName.substring(1, functionName.length() - 1))) {
                                    String document = ModelElementHelper.getFirstDocument(function).getReference();
                                    if (document != null && !"".equals(document)) { //$NON-NLS-1$

                                        internalNodeHTMLMap.put(node.getUniqueName(),
                                                document.substring(document.indexOf("<font"), document.indexOf("</body>"))); //$NON-NLS-1$ //$NON-NLS-2$
                                    }
                                }

                            }
                        }

                    }
                }

            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }

    }

    public IDialogSettings getDialogSettings() {
        return RepositoryPlugin.getDefault().getDialogSettings();
    }

    public Set<MetadataTable> getTablesFromSpecifiedDataPackage(DatabaseConnection dbconn) {
        return ProjectNodeHelper.getTablesFromSpecifiedDataPackage(dbconn);
    }

}
