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
package org.talend.repository.ui.login;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
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
import org.epic.core.preferences.PerlMainPreferencePage;
import org.epic.perleditor.PerlEditorPlugin;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.MessageBoxExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.general.ConnectionBean;
import org.talend.core.model.general.Project;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.prefs.PreferenceManipulator;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ui.login.connections.ConnectionUserPerReader;

/**
 * Login dialog. <br/>
 * 
 * $Id: LoginDialog.java 47989 2010-09-08 11:32:50Z gldu $
 * 
 */
public class LoginDialog extends TrayDialog {

    /** The login composite. */
    private LoginComposite loginComposite;

    private ConnectionUserPerReader perReader;

    private boolean inuse = false;

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
        container.setBackground(new Color(null, 215, 215, 215));
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        new ImageCanvas(container, brandingService.getLoginVImage()); //$NON-NLS-1$

        if (!perReader.isHaveUserPer()) {
            perReader.createPropertyFile();
        }
        loginComposite = new LoginComposite(container, SWT.NONE, this, inuse);
        GridData data = new GridData(GridData.FILL_BOTH);
        // data.widthHint = INNER_LOGIN_COMPOSITE_WIDTH;
        // data.heightHint = DIALOG_HEIGHT;
        loginComposite.setLayoutData(data);

        return container;
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
        if (LoginComposite.isRestart == true) {
            super.okPressed();
        } else {
            logIn(loginComposite.getProject());
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
    protected void logIn(final Project project) {

        ConnectionBean connBean = loginComposite.getConnection();
        if (connBean == null || project == null || project.getLabel() == null) {
            return;
        }

        // Save last used parameters
        PreferenceManipulator prefManipulator = new PreferenceManipulator(CorePlugin.getDefault().getPreferenceStore());
        prefManipulator.setLastConnection(connBean.getName());
        prefManipulator.setLastProject(project.getLabel());
        saveLastConnBean(connBean);
        if (project.getLanguage().equals(ECodeLanguage.PERL)) {
            IPreferenceStore store = CorePlugin.getDefault().getPreferenceStore();
            String prelExecutableValue = store.getString(ITalendCorePrefConstants.PERL_INTERPRETER);
            PerlEditorPlugin.getDefault().setExecutablePreference("\"" + prelExecutableValue + "\"");
            PerlMainPreferencePage.refreshExecutableTextValue("\"" + prelExecutableValue + "\"");
        }
        final Shell shell = this.getShell();
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);

        IRunnableWithProgress runnable = new IRunnableWithProgress() {

            private IProgressMonitor monitorWrap;

            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                // monitorWrap = new EventLoopProgressMonitor(monitor);
                try {
                    ProxyRepositoryFactory.getInstance().logOnProject(project, monitor);
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
            loginComposite.populateProjectList();
            MessageBoxExceptionHandler.process(e.getTargetException(), getShell());
            return;
        } catch (InterruptedException e) {
            loginComposite.populateProjectList();
            return;
        }

        super.okPressed();
    }

    public void updateButtons() {
        // loginComposite.fillProjectsBtn.setEnabled(loginComposite.canFinish());
        // getButton(IDialogConstants.OK_ID).setEnabled(loginComposite.canFinish());
    }

    /**
     * Canvas displaying an image.<br/>
     * 
     * $Id: LoginDialog.java 47989 2010-09-08 11:32:50Z gldu $
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
