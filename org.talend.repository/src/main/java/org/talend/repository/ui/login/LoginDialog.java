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
package org.talend.repository.ui.login;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.general.ConnectionBean;
import org.talend.core.model.general.IExchangeService;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ExchangeUser;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.prefs.PreferenceManipulator;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.service.ICorePerlService;
import org.talend.core.tis.ICoreTisService;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.login.connections.ConnectionUserPerReader;

/**
 * Login dialog. <br/>
 * 
 * $Id: LoginDialog.java 77219 2012-01-24 01:14:15Z mhirt $
 * 
 */
public class LoginDialog extends TrayDialog {

    /** The login composite. */
    private LoginComposite loginComposite;

    private ConnectionUserPerReader perReader;

    private boolean inuse = false;

    private Composite base;

    private StackLayout stackLayout;

    private TOSLoginComposite tosLoginComposite;

    /**
     * Construct a new LoginDialog.
     * 
     * @param parentShell Parent shell.
     */
    public LoginDialog(Shell parentShell) {
        super(parentShell);
        perReader = ConnectionUserPerReader.getInstance();
        setHelpAvailable(false);
    }

    /**
     * Construct a new LoginDialog.
     * 
     * @param parentShell Parent shell.
     */
    public LoginDialog(Shell parentShell, boolean inuse) {
        super(parentShell);
        this.inuse = inuse;
        perReader = ConnectionUserPerReader.getInstance();
        setHelpAvailable(false);
    }

    protected void initializeBounds() {
        super.initializeBounds();
        Point location = getInitialLocation(getShell().getSize());
        getShell().setLocation(location.x, location.y);
    }

    /**
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        newShell.setText(Messages.getString("LoginDialog.title", brandingService.getFullProductName())); //$NON-NLS-1$
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, 0);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        applyDialogFont(composite);
        // initialize the dialog units
        initializeDialogUnits(composite);
        // create the dialog area and button bar
        dialogArea = createDialogArea(composite);
        return composite;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(final Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        container.setLayout(layout);
        // container.setBackground(new Color(null, 215, 215, 215));
        container.setBackground(new Color(null, 255, 255, 255));
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        new ImageCanvas(container, brandingService.getLoginVImage()); //$NON-NLS-1$

        if (!perReader.isHaveUserPer()) {
            perReader.createPropertyFile();
        }
        base = new Composite(container, SWT.NONE);
        base.setLayoutData(new GridData(GridData.FILL_BOTH));
        stackLayout = new StackLayout();
        base.setLayout(stackLayout);
        if (!PluginChecker.isSVNProviderPluginLoaded()) {// tos
            loginComposite = new LoginComposite(base, SWT.NONE, this, inuse, tosLoginComposite, stackLayout);
            loginComposite.populateProjectList();
            tosLoginComposite = new TOSLoginComposite(base, SWT.NONE, loginComposite, this, inuse);
        } else {
            loginComposite = new LoginComposite(base, SWT.NONE, this, inuse, tosLoginComposite, stackLayout);
        }
        GridData data = new GridData(GridData.FILL_BOTH);
        // data.widthHint = INNER_LOGIN_COMPOSITE_WIDTH;
        // data.heightHint = DIALOG_HEIGHT;
        loginComposite.setLayoutData(data);
        stackLayout.topControl = loginComposite;
        base.layout();
        if (!PluginChecker.isSVNProviderPluginLoaded()) {
            Project[] projectList = readProject();
            if (projectList.length > 0) {
                advanced();
            }
        }
        return container;
    }

    private Project[] readProject() {
        ProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        Project[] projects = null;
        try {
            projects = repositoryFactory.readProject();
        } catch (PersistenceException e1) {
            e1.printStackTrace();
        } catch (BusinessException e1) {
            e1.printStackTrace();
        }
        return projects;
    }

    public void advanced() {
        stackLayout.topControl = tosLoginComposite;
        base.layout();
        Project[] projectCollection = tosLoginComposite.readProject();

        Map<String, String> convertorMapper = tosLoginComposite.getConvertorMapper();

        for (int i = 0; i < projectCollection.length; i++) {

            tosLoginComposite.getProjectMap().put(projectCollection[i].getLabel().toUpperCase(), projectCollection[i]);
            convertorMapper.put(projectCollection[i].getLabel().toUpperCase(), projectCollection[i].getLabel());

        }

        ListViewer projectListViewer = tosLoginComposite.getProjectListViewer();
        projectListViewer.setInput(new ArrayList(convertorMapper.values()));

        try {
            tosLoginComposite.setStatusArea();
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }

        if (projectListViewer.getList().getItemCount() > 0) {

            projectListViewer.getList().select(0);
            tosLoginComposite.enableOpenAndDelete(true);

        }
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // super.createButtonsForButtonBar(parent);
        updateButtons();
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        if (LoginComposite.isRestart) {
            super.okPressed();
        } else {
            if (PluginChecker.isSVNProviderPluginLoaded()) {
                boolean isLogInOk = logIn(loginComposite.getProject());
                if (isLogInOk) {
                    super.okPressed();
                }// else login failed so ignor the ok button.
            } else {
                super.okPressed();
            }
        }
    }

    public void saveLastConnBean(ConnectionBean connBean) {
        perReader.saveLastConnectionBean(connBean);
    }

    /**
     * DOC smallet Comment method "logIn".
     * 
     * @param project
     * @throws Exception
     */
    protected boolean logIn(final Project project) {
        final ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        ConnectionBean connBean = loginComposite.getConnection();
        final boolean needRestartForLocal = loginComposite.needRestartForLocal();
        if (connBean == null || project == null || project.getLabel() == null) {
            return false;
        }

        // Save last used parameters
        PreferenceManipulator prefManipulator = new PreferenceManipulator(CorePlugin.getDefault().getPreferenceStore());
        prefManipulator.setLastConnection(connBean.getName());
        prefManipulator.setLastProject(project.getTechnicalLabel());
        saveLastConnBean(connBean);
        if (project.getLanguage().equals(ECodeLanguage.PERL)) {
            IPreferenceStore store = CorePlugin.getDefault().getPreferenceStore();
            String prelExecutableValue = store.getString(ITalendCorePrefConstants.PERL_INTERPRETER);
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICorePerlService.class)) {
                ICorePerlService service = (ICorePerlService) GlobalServiceRegister.getDefault().getService(
                        ICorePerlService.class);
                service.setExecutablePreference(prelExecutableValue);
            }
        }

        try {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ICoreTisService.class)) {
                final ICoreTisService service = (ICoreTisService) GlobalServiceRegister.getDefault().getService(
                        ICoreTisService.class);
                if (service != null) {// if in TIS then update the bundle status according to the project type
                    if (!service.validProject(project, needRestartForLocal)) {
                        LoginComposite.isRestart = true;
                        return true;
                    }
                }// else not in TIS so ignor caus we may not have a licence so we do not know which bundles belong to
                 // DI, DQ or MDM
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
            loginComposite.populateProjectList();
            MessageDialog.openError(getShell(), getShell().getText(), e.getMessage());
            return false;
        }

        // if (!PluginChecker.isSVNProviderPluginLoaded()) {// tos
        // if (project.getExchangeUser().getLogin() == null || project.getExchangeUser().getLogin().equals("")) {
        // TalendForgeDialog tfDialog = new TalendForgeDialog(this.getShell(), project);
        // tfDialog.open();
        // }
        // } else {// tis
        IPreferenceStore prefStore = PlatformUI.getPreferenceStore();
        boolean checkTisVersion = prefStore.getBoolean(ITalendCorePrefConstants.EXCHANGE_CHECK_TIS_VERSION);
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        if (!checkTisVersion && brandingService.isPoweredbyTalend()) {
            int count = prefStore.getInt(TalendForgeDialog.LOGINCOUNT);
            ExchangeUser exchangeUser = project.getExchangeUser();
            boolean isExchangeLogon = exchangeUser.getLogin() != null && !exchangeUser.getLogin().equals("");
            boolean isUserPassRight = true;
            if (isExchangeLogon) {
                if (PluginChecker.isExchangeSystemLoaded()) {
                    IExchangeService service = (IExchangeService) GlobalServiceRegister.getDefault().getService(
                            IExchangeService.class);
                    if (service.checkUserAndPass(exchangeUser.getUsername(), exchangeUser.getPassword()) != null) {
                        isUserPassRight = false;
                    }
                }
            }

            if (!isExchangeLogon || !isUserPassRight) {
                if (count < 10) {
                    TalendForgeDialog tfDialog = new TalendForgeDialog(this.getShell(), project);
                    tfDialog.open();
                }
            }
        }
        // }

        final Shell shell = this.getShell();
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);

        IRunnableWithProgress runnable = new IRunnableWithProgress() {

            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                // monitorWrap = new EventLoopProgressMonitor(monitor);
                try {

                    factory.logOnProject(project, monitor);
                } catch (LoginException e) {
                    throw new InvocationTargetException(e);
                } catch (PersistenceException e) {
                    throw new InvocationTargetException(e);
                } catch (OperationCanceledException e) {
                    throw new InterruptedException(e.getLocalizedMessage());
                }

                monitor.done();
            }
        };

        try {

            dialog.run(true, true, runnable);

        } catch (InvocationTargetException e) {
            if (PluginChecker.isSVNProviderPluginLoaded()) {
                loginComposite.populateProjectList();
                MessageBoxExceptionHandler.process(e.getTargetException(), getShell());
            } else {
                loginComposite.populateTOSProjectList();
                MessageBoxExceptionHandler.process(e.getTargetException(), getShell());
            }
            return false;
        } catch (InterruptedException e) {
            if (PluginChecker.isSVNProviderPluginLoaded()) {
                loginComposite.populateProjectList();
            } else {
                loginComposite.populateTOSProjectList();
            }
            return false;
        }

        return true;
    }

    public void updateButtons() {
        // loginComposite.fillProjectsBtn.setEnabled(loginComposite.canFinish());
        // getButton(IDialogConstants.OK_ID).setEnabled(loginComposite.canFinish());
    }

    /**
     * Canvas displaying an image.<br/>
     * 
     * $Id: LoginDialog.java 77219 2012-01-24 01:14:15Z mhirt $
     * 
     */
    private class ImageCanvas extends Canvas {

        private Image img;

        public ImageCanvas(Composite parent, ImageDescriptor imgDesc) {
            super(parent, SWT.NONE);

            if (imgDesc != null) {
                img = imgDesc.createImage();
                addPaintListener(new PaintListener() {

                    public void paintControl(PaintEvent e) {
                        e.gc.drawImage(img, 0, 0);
                    }
                });
            }
        }

        /*
         * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
         */
        @Override
        public Point computeSize(int wHint, int hHint, boolean changed) {
            Point size;
            if (img != null) {
                size = new Point(img.getBounds().width, img.getBounds().height);
            } else {
                size = super.computeSize(wHint, hHint, changed);
            }
            return size;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.widgets.Widget#dispose()
         */
        @Override
        public void dispose() {
            if (img != null) {
                img.dispose();
                img = null;
            }

            super.dispose();
        }

    }

}
