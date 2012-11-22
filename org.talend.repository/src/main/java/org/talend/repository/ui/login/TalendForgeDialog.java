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

import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.general.IExchangeService;
import org.talend.core.model.general.Project;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.ui.TalendBrowserLaunchHelper;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.registeruser.RegisterManagement;
import org.talend.repository.ui.ERepositoryImages;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class TalendForgeDialog extends TrayDialog {

    private StackLayout stackLayout;

    private Hyperlink link;

    private Text userNameText;

    private Text emailText;

    private String oldCountry;

    private int countryToSelect = 0;

    private Combo countryCombo;

    private Text passwordText;

    private Text passwordAgainText;

    private Button agreeButton;

    private Button improveButton;

    private Button createAccountButton;

    private Button proxySettingButton;

    private Button skipButton;

    private String proxyHost = null;

    private String proxyPort = null;

    private String proxyUser = null;

    private String proxyPassword = null;

    private boolean isProxyEnable = false;

    private Label password2ValidateLabel;

    private String notValidateStr = Messages.getString("TalendForgeDialog.notValid");

    private Composite downComposite;

    private Composite createAccount;

    private Composite connectAccount;

    private Hyperlink linkToCreate;

    private Text usernameTextForConnect;

    private Text passwordTextForconnect;

    private Button improveButtonInConnect;

    private Button connectButton;

    private Button skipButtonForConnect;

    private Button proxySettingButtonForConnect;

    private Project project;

    public static final String LOGINCOUNT = "LOGINCOUNT";

    private static final Image DOT_IMAGE = ImageProvider.getImage(ERepositoryImages.DOT_ICON);

    private Font font = new Font(null, LoginComposite.FONT_ARIAL, 9, SWT.NORMAL);

    private Font fontMac = new Font(null, LoginComposite.FONT_ARIAL, 12, SWT.NORMAL);

    private final String osName = System.getProperties().getProperty("os.name");

    /**
     * DOC Administrator TalendForgeDialog constructor comment.
     * 
     * @param shell
     */
    public TalendForgeDialog(Shell shell, Project project) {
        super(shell);
        this.project = project;
        if (osName.contains("Mac")) {
            font = fontMac;
        }
    }

    protected void initializeBounds() {
        super.initializeBounds();
        Point location = getInitialLocation(getShell().getSize());
        getShell().setLocation(location.x, location.y);
    }

    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("TalendForgeDialog.newProjectTitle")); //$NON-NLS-1$
    }

    @Override
    protected Control createContents(Composite parent) {
        parent.setLayout(new FormLayout());
        Composite composite = new Composite(parent, 0);
        FormData data = new FormData();
        data.height = 500;
        data.width = 670;
        composite.setLayoutData(data);
        composite.setLayout(new FormLayout());
        composite.setBackground(new Color(null, 255, 255, 255));
        // create the dialog area and button bar
        dialogArea = createDialogArea(composite);
        addListener();
        return composite;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        Composite upComposite = new Composite(parent, SWT.NONE);
        FormData data = null;

        data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.left = new FormAttachment(0, 0);
        data.height = 140;
        data.width = 670;
        upComposite.setLayoutData(data);
        upComposite.setLayout(new FormLayout());
        upComposite.setBackground(parent.getBackground());
        createUpComposite(upComposite);

        downComposite = new Composite(parent, SWT.NONE);
        data = new FormData();
        data.top = new FormAttachment(0, 140);
        data.left = new FormAttachment(0, 0);
        data.height = 360;
        data.width = 670;
        downComposite.setLayoutData(data);
        stackLayout = new StackLayout();
        downComposite.setLayout(stackLayout);
        downComposite.setBackground(parent.getBackground());
        createDownComposite(downComposite);

        return parent;
    }

    private void createUpComposite(Composite parent) {
        FormData data = null;

        Label labelTitle = new Label(parent, SWT.WRAP);
        data = new FormData();
        data.top = new FormAttachment(0, 10);
        data.left = new FormAttachment(0, 10);
        data.right = new FormAttachment(0, 450);
        data.bottom = new FormAttachment(0, 30);
        labelTitle.setText(Messages.getString("TalendForgeDialog.labelTitle"));
        labelTitle.setFont(font);
        labelTitle.setLayoutData(data);
        labelTitle.setBackground(parent.getBackground());

        Label imageLabel = new Label(parent, SWT.NONE);
        data = new FormData();
        data.top = new FormAttachment(labelTitle, 10, SWT.BOTTOM);
        data.left = new FormAttachment(labelTitle, 10, SWT.LEFT);
        imageLabel.setLayoutData(data);
        imageLabel.setImage(DOT_IMAGE);

        Label labelMessageOne = new Label(parent, SWT.WRAP);
        data = new FormData();
        data.top = new FormAttachment(imageLabel, 1, SWT.TOP);
        data.left = new FormAttachment(imageLabel, 7, SWT.RIGHT);
        data.right = new FormAttachment(0, 450);
        labelMessageOne.setText(Messages.getString("TalendForgeDialog.labelMessageOne"));
        labelMessageOne.setFont(font);
        labelMessageOne.setLayoutData(data);
        labelMessageOne.setBackground(parent.getBackground());

        Label imageLabelTow = new Label(parent, SWT.NONE);
        data = new FormData();
        data.top = new FormAttachment(labelMessageOne, 5, SWT.BOTTOM);
        data.left = new FormAttachment(labelTitle, 10, SWT.LEFT);
        imageLabelTow.setLayoutData(data);
        imageLabelTow.setImage(DOT_IMAGE);

        Label labelMessageTwo = new Label(parent, SWT.WRAP);
        data = new FormData();
        data.top = new FormAttachment(imageLabelTow, 1, SWT.TOP);
        data.left = new FormAttachment(imageLabelTow, 7, SWT.RIGHT);
        data.right = new FormAttachment(labelMessageOne, 0, SWT.RIGHT);
        labelMessageTwo.setText(Messages.getString("TalendForgeDialog.labelMessageTwo"));
        labelMessageTwo.setFont(font);
        labelMessageTwo.setLayoutData(data);
        labelMessageTwo.setBackground(parent.getBackground());

        Label imageLabelTree = new Label(parent, SWT.NONE);
        data = new FormData();
        data.top = new FormAttachment(labelMessageTwo, 5, SWT.BOTTOM);
        data.left = new FormAttachment(labelTitle, 10, SWT.LEFT);
        imageLabelTree.setLayoutData(data);
        imageLabelTree.setImage(DOT_IMAGE);

        Label labelMessageThree = new Label(parent, SWT.WRAP);
        data = new FormData();
        data.top = new FormAttachment(imageLabelTree, 2, SWT.TOP);
        data.left = new FormAttachment(imageLabelTree, 7, SWT.RIGHT);
        data.right = new FormAttachment(labelMessageTwo, 0, SWT.RIGHT);
        data.bottom = new FormAttachment(labelMessageTwo, 20, SWT.BOTTOM);
        labelMessageThree.setText(Messages.getString("TalendForgeDialog.labelMessageThree"));
        labelMessageThree.setFont(font);
        labelMessageThree.setLayoutData(data);
        labelMessageThree.setBackground(parent.getBackground());

        ImageCanvas cc = new ImageCanvas(parent, ImageProvider.getImageDesc(ERepositoryImages.TALENDFORGE_ICON));
        data = new FormData();
        data.top = new FormAttachment(0, 20);
        data.right = new FormAttachment(100, -25);
        cc.setLayoutData(data);
    }

    private void createDownComposite(Composite parent) {
        FormData data = null;

        createAccount = new Composite(parent, SWT.NONE);
        createAccount.setBackground(parent.getBackground());
        createAccount.setLayout(new FormLayout());

        Label createLabel = new Label(createAccount, SWT.NONE);
        data = new FormData();
        data.top = new FormAttachment(0, 10);
        data.left = new FormAttachment(0, 10);
        data.bottom = new FormAttachment(0, 30);
        createLabel.setText(Messages.getString("TalendForgeDialog.createLabel"));
        createLabel.setLayoutData(data);
        createLabel.setFont(font);
        createLabel.setBackground(createAccount.getBackground());
        createLabel.forceFocus();

        link = new Hyperlink(createAccount, SWT.NONE);
        data = new FormData();
        data.top = new FormAttachment(createLabel, 0, SWT.TOP);
        data.left = new FormAttachment(createLabel, 0, SWT.RIGHT);
        data.bottom = new FormAttachment(createLabel, 0, SWT.BOTTOM);
        link.setText(Messages.getString("TalendForgeDialog.link"));
        link.setForeground(LoginComposite.YELLOW_GREEN_COLOR);
        link.setBackground(createAccount.getBackground());
        link.setFont(font);
        link.setLayoutData(data);

        Label userNameLabel = new Label(createAccount, SWT.RIGHT);
        userNameLabel.setFont(font);
        GC gc = new GC(userNameLabel);
        Point tmpSize;
        Point labelSize = gc.stringExtent(Messages.getString("TalendForgeDialog.userNameLabel"));
        tmpSize = gc.stringExtent(Messages.getString("TalendForgeDialog.passwordAgainLabel"));
        if (tmpSize.x > labelSize.x) {
            labelSize = tmpSize;
        }
        tmpSize = gc.stringExtent(Messages.getString("TalendForgeDialog.emailLabel"));
        if (tmpSize.x > labelSize.x) {
            labelSize = tmpSize;
        }
        tmpSize = gc.stringExtent(Messages.getString("TalendForgeDialog.passwordLabel"));
        if (tmpSize.x > labelSize.x) {
            labelSize = tmpSize;
        }
        gc.dispose();
        data = new FormData();
        data.top = new FormAttachment(createLabel, 20, SWT.BOTTOM);
        data.left = new FormAttachment(0, 170 - labelSize.x);
        data.right = new FormAttachment(0, 170);
        data.bottom = new FormAttachment(createLabel, 40, SWT.BOTTOM);
        userNameLabel.setText(Messages.getString("TalendForgeDialog.userNameLabel"));
        userNameLabel.setLayoutData(data);
        userNameLabel.setBackground(createAccount.getBackground());

        userNameText = new Text(createAccount, SWT.BORDER);
        data = new FormData();
        data.top = new FormAttachment(userNameLabel, 0, SWT.TOP);
        data.left = new FormAttachment(userNameLabel, 10, SWT.RIGHT);
        data.right = new FormAttachment(userNameLabel, 200, SWT.RIGHT);
        data.bottom = new FormAttachment(userNameLabel, 0, SWT.BOTTOM);
        userNameText.setLayoutData(data);
        userNameText.setFont(font);
        userNameText.forceFocus();

        Label needLabelforUserName = new Label(createAccount, SWT.NONE);
        data = new FormData();
        data.top = new FormAttachment(userNameText, 0, SWT.TOP);
        data.left = new FormAttachment(userNameText, 5, SWT.RIGHT);
        data.right = new FormAttachment(userNameText, 10, SWT.RIGHT);
        data.bottom = new FormAttachment(userNameText, 0, SWT.BOTTOM);
        needLabelforUserName.setText("*");
        needLabelforUserName.setFont(font);
        needLabelforUserName.setLayoutData(data);
        Color red = new Color(null, 255, 0, 0);
        needLabelforUserName.setForeground(red);
        needLabelforUserName.setBackground(createAccount.getBackground());
        red.dispose();

        Label emailLabel = new Label(createAccount, SWT.RIGHT);
        data = new FormData();
        data.top = new FormAttachment(userNameLabel, 5, SWT.BOTTOM);
        data.left = new FormAttachment(userNameLabel, -labelSize.x, SWT.RIGHT);
        data.right = new FormAttachment(userNameLabel, 0, SWT.RIGHT);
        data.bottom = new FormAttachment(userNameLabel, 25, SWT.BOTTOM);
        emailLabel.setText(Messages.getString("TalendForgeDialog.emailLabel"));
        emailLabel.setBackground(createAccount.getBackground());
        emailLabel.setLayoutData(data);
        emailLabel.setFont(font);

        emailText = new Text(createAccount, SWT.BORDER);
        data = new FormData();
        data.top = new FormAttachment(emailLabel, 0, SWT.TOP);
        data.left = new FormAttachment(userNameText, 0, SWT.LEFT);
        data.right = new FormAttachment(userNameText, 0, SWT.RIGHT);
        data.bottom = new FormAttachment(emailLabel, 0, SWT.BOTTOM);
        emailText.setLayoutData(data);
        emailText.setFont(font);

        Label needLabel = new Label(createAccount, SWT.NONE);
        data = new FormData();
        data.top = new FormAttachment(emailLabel, 0, SWT.TOP);
        data.left = new FormAttachment(emailText, 5, SWT.RIGHT);
        data.right = new FormAttachment(emailText, 10, SWT.RIGHT);
        data.bottom = new FormAttachment(emailLabel, 0, SWT.BOTTOM);
        needLabel.setText("*");
        needLabel.setFont(font);
        needLabel.setLayoutData(data);
        red = new Color(null, 255, 0, 0);
        needLabel.setForeground(red);
        needLabel.setBackground(createAccount.getBackground());
        red.dispose();

        Label passwordLabel = new Label(createAccount, SWT.RIGHT);
        data = new FormData();
        data.top = new FormAttachment(emailLabel, 5, SWT.BOTTOM);
        data.left = new FormAttachment(emailLabel, -labelSize.x, SWT.RIGHT);
        data.right = new FormAttachment(emailLabel, 0, SWT.RIGHT);
        data.bottom = new FormAttachment(emailLabel, 25, SWT.BOTTOM);
        passwordLabel.setLayoutData(data);
        passwordLabel.setBackground(createAccount.getBackground());
        passwordLabel.setText(Messages.getString("TalendForgeDialog.passwordLabel"));
        passwordLabel.setFont(font);

        passwordText = new Text(createAccount, SWT.BORDER);
        data = new FormData();
        data.top = new FormAttachment(passwordLabel, 0, SWT.TOP);
        data.left = new FormAttachment(emailText, 0, SWT.LEFT);
        data.right = new FormAttachment(emailText, 0, SWT.RIGHT);
        data.bottom = new FormAttachment(passwordLabel, 0, SWT.BOTTOM);
        passwordText.setLayoutData(data);
        passwordText.setEchoChar('*');
        passwordText.setFont(font);

        Label needLabelforPassword = new Label(createAccount, SWT.NONE);
        data = new FormData();
        data.top = new FormAttachment(passwordText, 0, SWT.TOP);
        data.left = new FormAttachment(passwordText, 5, SWT.RIGHT);
        data.right = new FormAttachment(passwordText, 10, SWT.RIGHT);
        data.bottom = new FormAttachment(passwordText, 0, SWT.BOTTOM);
        needLabelforPassword.setText("*");
        needLabelforPassword.setFont(font);
        needLabelforPassword.setLayoutData(data);
        red = new Color(null, 255, 0, 0);
        needLabelforPassword.setForeground(red);
        needLabelforPassword.setBackground(createAccount.getBackground());
        red.dispose();

        Label passwordAgainLabel = new Label(createAccount, SWT.RIGHT);
        data = new FormData();
        data.top = new FormAttachment(passwordLabel, 5, SWT.BOTTOM);
        data.left = new FormAttachment(passwordLabel, -labelSize.x, SWT.RIGHT);
        data.right = new FormAttachment(passwordLabel, 0, SWT.RIGHT);
        data.bottom = new FormAttachment(passwordLabel, 25, SWT.BOTTOM);
        passwordAgainLabel.setLayoutData(data);
        passwordAgainLabel.setBackground(createAccount.getBackground());
        passwordAgainLabel.setText(Messages.getString("TalendForgeDialog.passwordAgainLabel"));
        passwordAgainLabel.setFont(font);

        passwordAgainText = new Text(createAccount, SWT.BORDER);
        data = new FormData();
        data.top = new FormAttachment(passwordAgainLabel, 0, SWT.TOP);
        data.left = new FormAttachment(passwordText, 0, SWT.LEFT);
        data.right = new FormAttachment(passwordText, 0, SWT.RIGHT);
        data.bottom = new FormAttachment(passwordAgainLabel, 0, SWT.BOTTOM);
        passwordAgainText.setLayoutData(data);
        passwordAgainText.setEchoChar('*');
        passwordAgainText.setFont(font);

        Label needLabelforPasswordAgain = new Label(createAccount, SWT.NONE);
        data = new FormData();
        data.top = new FormAttachment(passwordAgainText, 0, SWT.TOP);
        data.left = new FormAttachment(passwordAgainText, 5, SWT.RIGHT);
        data.right = new FormAttachment(passwordAgainText, 10, SWT.RIGHT);
        data.bottom = new FormAttachment(passwordAgainText, 0, SWT.BOTTOM);
        needLabelforPasswordAgain.setText("*");
        needLabelforPasswordAgain.setFont(font);
        needLabelforPasswordAgain.setLayoutData(data);
        red = new Color(null, 255, 0, 0);
        needLabelforPasswordAgain.setForeground(red);
        needLabelforPasswordAgain.setBackground(createAccount.getBackground());
        red.dispose();

        password2ValidateLabel = new Label(createAccount, SWT.NONE);
        data = new FormData();
        data.top = new FormAttachment(needLabelforPasswordAgain, 0, SWT.TOP);
        data.left = new FormAttachment(needLabelforPasswordAgain, 10, SWT.RIGHT);
        data.right = new FormAttachment(needLabelforPasswordAgain, 110, SWT.RIGHT);
        data.bottom = new FormAttachment(needLabelforPasswordAgain, 0, SWT.BOTTOM);
        password2ValidateLabel.setBackground(createAccount.getBackground());
        password2ValidateLabel.setLayoutData(data);

        Label countryLabel = new Label(createAccount, SWT.RIGHT);
        countryLabel.setFont(font);
        gc = new GC(countryLabel);
        labelSize = gc.stringExtent(Messages.getString("TalendForgeDialog.countryLabel"));
        gc.dispose();
        data = new FormData();
        data.top = new FormAttachment(passwordAgainLabel, 5, SWT.BOTTOM);
        data.left = new FormAttachment(passwordAgainLabel, -labelSize.x, SWT.RIGHT);
        data.right = new FormAttachment(passwordAgainLabel, 0, SWT.RIGHT);
        data.bottom = new FormAttachment(passwordAgainLabel, 25, SWT.BOTTOM);
        countryLabel.setText(Messages.getString("TalendForgeDialog.countryLabel"));
        countryLabel.setLayoutData(data);
        countryLabel.setBackground(createAccount.getBackground());

        countryCombo = new Combo(createAccount, SWT.BORDER | SWT.READ_ONLY);
        data = new FormData();
        data.top = new FormAttachment(countryLabel, 0, SWT.TOP);
        data.left = new FormAttachment(emailText, 0, SWT.LEFT);
        data.right = new FormAttachment(emailText, 0, SWT.RIGHT);
        data.bottom = new FormAttachment(countryLabel, 0, SWT.BOTTOM);
        countryCombo.setLayoutData(data);
        countryCombo.setItems(initiateCountryList());
        countryCombo.select(countryToSelect);
        countryCombo.setFont(font);

        agreeButton = new Button(createAccount, SWT.CHECK);
        gc = new GC(agreeButton);
        labelSize = gc.stringExtent(Messages.getString("TalendForgeDialog.agreeButton"));
        gc.dispose();
        data = new FormData();
        data.top = new FormAttachment(countryCombo, 20, SWT.BOTTOM);
        data.left = new FormAttachment(countryCombo, 0, SWT.LEFT);
        // if (Platform.getOS().equals(Platform.OS_WIN32)) {
        // data.right = new FormAttachment(passwordAgainText, labelSize.x + 50, SWT.LEFT);
        // } else if (Platform.getOS().equals(Platform.OS_LINUX)) {
        // data.right = new FormAttachment(passwordAgainText, labelSize.x + 80, SWT.LEFT);
        // } else {
        // data.right = new FormAttachment(passwordAgainText, labelSize.x + 80, SWT.LEFT);
        // }
        // data.bottom = new FormAttachment(passwordAgainText, 40, SWT.BOTTOM);
        agreeButton.setText(Messages.getString("TalendForgeDialog.agreeButton"));
        agreeButton.setLayoutData(data);
        agreeButton.setFont(font);
        agreeButton.setBackground(createAccount.getBackground());
        agreeButton.setSelection(false);

        improveButton = new Button(createAccount, SWT.CHECK);
        data = new FormData();
        data.top = new FormAttachment(agreeButton, 0, SWT.BOTTOM);
        data.left = new FormAttachment(agreeButton, 0, SWT.LEFT);
        data.right = new FormAttachment(agreeButton, 500, SWT.LEFT);
        data.bottom = new FormAttachment(agreeButton, 30, SWT.BOTTOM);
        improveButton.setText(Messages.getString("TalendForgeDialog.improveButton"));
        improveButton.setLayoutData(data);
        improveButton.setBackground(createAccount.getBackground());
        improveButton.setFont(font);
        improveButton.setSelection(true);
        final IPreferenceStore preferenceStore = CoreRuntimePlugin.getInstance().getPreferenceStore();
        if (preferenceStore.getBoolean(ITalendCorePrefConstants.DATA_COLLECTOR_ENABLED)) {
            improveButton.setSelection(true);
        } else {
            improveButton.setSelection(false);
        }
        improveButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                preferenceStore.setValue(ITalendCorePrefConstants.DATA_COLLECTOR_ENABLED, improveButton.getSelection());
            }
        });

        Hyperlink readMore = new Hyperlink(createAccount, SWT.NONE);
        data = new FormData();
        data.top = new FormAttachment(improveButton, 0, SWT.BOTTOM);
        data.left = new FormAttachment(improveButton, 5, SWT.LEFT);
        data.right = new FormAttachment(improveButton, 100, SWT.LEFT);
        data.bottom = new FormAttachment(improveButton, 20, SWT.BOTTOM);
        readMore.setText(Messages.getString("TalendForgeDialog.readMore"));
        readMore.setBackground(createAccount.getBackground());
        readMore.setLayoutData(data);
        readMore.setFont(font);
        Color blue = new Color(null, 0, 0, 255);
        readMore.setForeground(blue);
        blue.dispose();
        readMore.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent e) {
                String url = "http://www.talendforge.org/communitybenefits.php";
                TalendBrowserLaunchHelper.openURL(url);
            }
        });

        createAccountButton = new Button(createAccount, SWT.PUSH);
        data = new FormData();
        data.top = new FormAttachment(readMore, 10, SWT.BOTTOM);
        data.left = new FormAttachment(improveButton, 0, SWT.LEFT);
        data.right = new FormAttachment(improveButton, 180, SWT.LEFT);
        data.bottom = new FormAttachment(readMore, 35, SWT.BOTTOM);
        createAccountButton.setLayoutData(data);
        createAccountButton.setText(Messages.getString("TalendForgeDialog.createAccountButton"));
        createAccountButton.setEnabled(false);
        createAccountButton.setFont(font);

        proxySettingButton = new Button(createAccount, SWT.PUSH);
        data = new FormData();
        data.left = new FormAttachment(0, 20);
        data.bottom = new FormAttachment(100, -20);
        data.height = 25;
        proxySettingButton.setText(Messages.getString("TalendForgeDialog.proxySettingButton"));
        proxySettingButton.setLayoutData(data);
        proxySettingButton.setFont(font);

        skipButton = new Button(createAccount, SWT.PUSH);
        data = new FormData();
        data.right = new FormAttachment(100, -20);
        data.bottom = new FormAttachment(100, -20);
        data.height = 25;
        data.width = 60;
        skipButton.setText(Messages.getString("TalendForgeDialog.skipButton"));
        skipButton.setLayoutData(data);
        skipButton.setFont(font);

        connectAccount = new Composite(parent, SWT.NONE);
        connectAccount.setBackground(parent.getBackground());
        connectAccount.setLayout(new FormLayout());

        Label loginLabel = new Label(connectAccount, SWT.NONE);
        loginLabel.setFont(font);
        gc = new GC(loginLabel);
        labelSize = gc.stringExtent(Messages.getString("TalendForgeDialog.loginLabel"));
        gc.dispose();
        data = new FormData();
        data.top = new FormAttachment(0, 10);
        data.left = new FormAttachment(0, 10);
        data.right = new FormAttachment(0, 10 + labelSize.x);
        data.bottom = new FormAttachment(0, 30);
        loginLabel.setText(Messages.getString("TalendForgeDialog.loginLabel"));
        loginLabel.setLayoutData(data);
        loginLabel.setBackground(connectAccount.getBackground());
        // loginLabel.forceFocus();

        linkToCreate = new Hyperlink(connectAccount, SWT.NONE);
        data = new FormData();
        data.top = new FormAttachment(loginLabel, 0, SWT.TOP);
        data.left = new FormAttachment(loginLabel, 5, SWT.RIGHT);
        data.bottom = new FormAttachment(loginLabel, 0, SWT.BOTTOM);
        linkToCreate.setText(Messages.getString("TalendForgeDialog.linkToCreate"));
        linkToCreate.setLayoutData(data);
        linkToCreate.setBackground(connectAccount.getBackground());
        linkToCreate.setForeground(LoginComposite.YELLOW_GREEN_COLOR);
        linkToCreate.setFont(font);

        Label emailLabelInConnect = new Label(connectAccount, SWT.RIGHT);
        emailLabelInConnect.setFont(font);
        gc = new GC(emailLabelInConnect);
        labelSize = gc.stringExtent(Messages.getString("TalendForgeDialog.userNameLabel"));
        int newSize = gc.stringExtent(Messages.getString("TalendForgeDialog.passwordLabel")).x;
        if (newSize > labelSize.x) {
            labelSize.x = newSize;
        }
        gc.dispose();
        data = new FormData();
        data.top = new FormAttachment(loginLabel, 20, SWT.BOTTOM);
        data.left = new FormAttachment(0, 170 - labelSize.x);
        data.right = new FormAttachment(0, 170);
        data.bottom = new FormAttachment(loginLabel, 40, SWT.BOTTOM);
        emailLabelInConnect.setText(Messages.getString("TalendForgeDialog.userNameLabel"));
        emailLabelInConnect.setLayoutData(data);
        emailLabelInConnect.setBackground(connectAccount.getBackground());

        usernameTextForConnect = new Text(connectAccount, SWT.BORDER);
        data = new FormData();
        data.top = new FormAttachment(emailLabelInConnect, 0, SWT.TOP);
        data.left = new FormAttachment(emailLabelInConnect, 10, SWT.RIGHT);
        data.right = new FormAttachment(emailLabelInConnect, 200, SWT.RIGHT);
        data.bottom = new FormAttachment(emailLabelInConnect, 0, SWT.BOTTOM);
        usernameTextForConnect.setLayoutData(data);
        usernameTextForConnect.setFont(font);

        Label needLabel1 = new Label(connectAccount, SWT.NONE);
        data = new FormData();
        data.top = new FormAttachment(emailLabelInConnect, 0, SWT.TOP);
        data.left = new FormAttachment(usernameTextForConnect, 5, SWT.RIGHT);
        data.right = new FormAttachment(usernameTextForConnect, 10, SWT.RIGHT);
        data.bottom = new FormAttachment(emailLabelInConnect, 0, SWT.BOTTOM);
        needLabel1.setText("*");
        needLabel1.setFont(font);
        needLabel1.setLayoutData(data);
        red = new Color(null, 255, 0, 0);
        needLabel1.setForeground(red);
        needLabel1.setBackground(connectAccount.getBackground());
        red.dispose();

        Label passwordLabelInConnect = new Label(connectAccount, SWT.RIGHT);
        data = new FormData();
        data.top = new FormAttachment(emailLabelInConnect, 5, SWT.BOTTOM);
        data.left = new FormAttachment(emailLabelInConnect, -labelSize.x, SWT.RIGHT);
        data.right = new FormAttachment(emailLabelInConnect, 0, SWT.RIGHT);
        data.bottom = new FormAttachment(emailLabelInConnect, 25, SWT.BOTTOM);
        passwordLabelInConnect.setText(Messages.getString("TalendForgeDialog.passwordLabel"));
        passwordLabelInConnect.setLayoutData(data);
        passwordLabelInConnect.setBackground(connectAccount.getBackground());
        passwordLabelInConnect.setFont(font);

        passwordTextForconnect = new Text(connectAccount, SWT.BORDER);
        data = new FormData();
        data.top = new FormAttachment(passwordLabelInConnect, 0, SWT.TOP);
        data.left = new FormAttachment(usernameTextForConnect, 0, SWT.LEFT);
        data.right = new FormAttachment(usernameTextForConnect, 0, SWT.RIGHT);
        data.bottom = new FormAttachment(passwordLabelInConnect, 0, SWT.BOTTOM);
        passwordTextForconnect.setLayoutData(data);
        passwordTextForconnect.setEchoChar('*');
        passwordTextForconnect.setFont(font);

        Label needLabel2 = new Label(connectAccount, SWT.NONE);
        data = new FormData();
        data.top = new FormAttachment(passwordLabelInConnect, 0, SWT.TOP);
        data.left = new FormAttachment(passwordTextForconnect, 5, SWT.RIGHT);
        data.right = new FormAttachment(passwordTextForconnect, 10, SWT.RIGHT);
        data.bottom = new FormAttachment(passwordLabelInConnect, 0, SWT.BOTTOM);
        needLabel2.setText("*");
        needLabel2.setFont(font);
        needLabel2.setLayoutData(data);
        red = new Color(null, 255, 0, 0);
        needLabel2.setForeground(red);
        needLabel2.setBackground(connectAccount.getBackground());
        red.dispose();

        improveButtonInConnect = new Button(connectAccount, SWT.CHECK);
        data = new FormData();
        data.top = new FormAttachment(passwordTextForconnect, 0, SWT.BOTTOM);
        data.left = new FormAttachment(passwordTextForconnect, 0, SWT.LEFT);
        data.right = new FormAttachment(passwordTextForconnect, 500, SWT.LEFT);
        data.bottom = new FormAttachment(passwordTextForconnect, 30, SWT.BOTTOM);
        improveButtonInConnect.setText(Messages.getString("TalendForgeDialog.improveButton"));
        improveButtonInConnect.setBackground(connectAccount.getBackground());
        improveButtonInConnect.setLayoutData(data);
        improveButtonInConnect.setSelection(true);
        improveButtonInConnect.setFont(font);

        Hyperlink readMoreInConnect = new Hyperlink(connectAccount, SWT.NONE);
        data = new FormData();
        data.top = new FormAttachment(improveButtonInConnect, 0, SWT.BOTTOM);
        data.left = new FormAttachment(improveButtonInConnect, 5, SWT.LEFT);
        data.right = new FormAttachment(improveButtonInConnect, 100, SWT.LEFT);
        data.bottom = new FormAttachment(improveButtonInConnect, 20, SWT.BOTTOM);
        readMoreInConnect.setText(Messages.getString("TalendForgeDialog.readMore"));
        readMoreInConnect.setFont(font);
        readMoreInConnect.setLayoutData(data);
        blue = new Color(null, 0, 0, 255);
        readMoreInConnect.setForeground(blue);
        readMoreInConnect.setBackground(connectAccount.getBackground());
        blue.dispose();
        readMoreInConnect.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent e) {
                String url = "http://www.talendforge.org/communitybenefits.php";
                TalendBrowserLaunchHelper.openURL(url);
            }
        });

        connectButton = new Button(connectAccount, SWT.PUSH);
        data = new FormData();
        data.top = new FormAttachment(readMoreInConnect, 20, SWT.BOTTOM);
        data.left = new FormAttachment(improveButtonInConnect, 0, SWT.LEFT);
        data.right = new FormAttachment(improveButtonInConnect, 80, SWT.LEFT);
        data.bottom = new FormAttachment(readMoreInConnect, 45, SWT.BOTTOM);
        connectButton.setText(Messages.getString("TalendForgeDialog.connectButton"));
        connectButton.setLayoutData(data);
        connectButton.setEnabled(false);
        connectButton.setFont(font);

        skipButtonForConnect = new Button(connectAccount, SWT.PUSH);
        data = new FormData();
        data.right = new FormAttachment(100, -20);
        data.bottom = new FormAttachment(100, -20);
        data.height = 25;
        data.width = 60;
        skipButtonForConnect.setText(Messages.getString("TalendForgeDialog.skipButton"));
        skipButtonForConnect.setLayoutData(data);
        skipButtonForConnect.setFont(font);

        proxySettingButtonForConnect = new Button(connectAccount, SWT.PUSH);
        data = new FormData();
        data.left = new FormAttachment(0, 20);
        data.bottom = new FormAttachment(100, -20);
        data.height = 25;
        proxySettingButtonForConnect.setText(Messages.getString("TalendForgeDialog.proxySettingButton"));
        proxySettingButtonForConnect.setLayoutData(data);
        proxySettingButtonForConnect.setFont(font);

        stackLayout.topControl = createAccount;
        parent.layout();

    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
    }

    private void addListener() {
        createAccountButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                boolean success = false;
                String email = emailText.getText().trim();
                String password = passwordText.getText().trim();
                String pseudonym = userNameText.getText().trim();
                try {
                    success = RegisterManagement.getInstance().createUser(email, pseudonym, password, "", "",
                            countryCombo.getText(), isProxyEnable, proxyHost, proxyPort, proxyUser, proxyPassword);
                    if (success) {
                        IPreferenceStore prefStore = PlatformUI.getPreferenceStore();
                        String connectionEmail = project.getAuthor().getLogin();
                        prefStore.setValue(connectionEmail, email + ":" + pseudonym + ":" + password);
                    }
                } catch (BusinessException e1) {
                    ExceptionHandler.process(e1);
                } finally {
                    if (success) {
                        MessageDialog.openInformation(getShell(), Messages.getString("TalendForgeDialog.MessageTitle"),
                                Messages.getString("TalendForgeDialog.Message"));
                        okPressed();
                    }
                }
            }
        });

        proxySettingButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                NetworkSettingDialog netSettingDialog = new NetworkSettingDialog(getParentShell());
                if (netSettingDialog.open() == Dialog.OK) {
                    String[] proxyString = netSettingDialog.getProxyString();
                    proxyHost = proxyString[0];
                    proxyPort = proxyString[1];
                    proxyUser = proxyString[2];
                    proxyPassword = proxyString[3];
                    if (proxyHost != null && !"".equals(proxyHost)) {//$NON-NLS-N$
                        isProxyEnable = true;
                    }
                } else {
                    proxyHost = null;
                    proxyPort = null;
                    proxyUser = null;
                    proxyPassword = null;
                    isProxyEnable = false;
                }
            }
        });

        proxySettingButtonForConnect.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                NetworkSettingDialog netSettingDialog = new NetworkSettingDialog(getParentShell());
                if (netSettingDialog.open() == Dialog.OK) {
                    String[] proxyString = netSettingDialog.getProxyString();
                    proxyHost = proxyString[0];
                    proxyPort = proxyString[1];
                    proxyUser = proxyString[2];
                    proxyPassword = proxyString[3];
                    if (proxyHost != null && !"".equals(proxyHost)) {//$NON-NLS-N$
                        isProxyEnable = true;
                    }
                } else {
                    proxyHost = null;
                    proxyPort = null;
                    proxyUser = null;
                    proxyPassword = null;
                    isProxyEnable = false;
                }
            }
        });

        skipButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                IPreferenceStore prefStore = PlatformUI.getPreferenceStore();
                int count = prefStore.getInt(LOGINCOUNT);
                count++;
                prefStore.setValue(LOGINCOUNT, count);
                okPressed();
            }
        });

        skipButtonForConnect.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                IPreferenceStore prefStore = PlatformUI.getPreferenceStore();
                int count = prefStore.getInt(LOGINCOUNT);
                count++;
                prefStore.setValue(LOGINCOUNT, count);
                okPressed();
            }
        });

        userNameText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                String password = passwordText.getText();
                String password2 = passwordAgainText.getText();
                String pseudonym = userNameText.getText();
                if (password != null && !"".equals(password) && password2 != null && !"".equals(password2)
                        && password.equals(password2) && pseudonym != null && !"".equals(pseudonym) && isEmailValid()
                        && agreeButton.getSelection()) {
                    createAccountButton.setEnabled(true);
                } else {
                    createAccountButton.setEnabled(false);
                }

            }
        });

        passwordText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {

                String password = passwordText.getText();
                String password2 = passwordAgainText.getText();
                String pseudonym = userNameText.getText();
                if (password != null && !"".equals(password) && password2 != null && !"".equals(password2)
                        && password.equals(password2)) {
                    password2ValidateLabel.setText("");
                    if (pseudonym != null && !"".equals(pseudonym)) {
                        if (agreeButton.getSelection() && isEmailValid()) {
                            createAccountButton.setEnabled(true);
                        }
                    } else {
                        createAccountButton.setEnabled(false);
                    }
                } else {
                    createAccountButton.setEnabled(false);
                    password2ValidateLabel.setText(notValidateStr);
                }
            }
        });

        passwordAgainText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                String password = passwordText.getText();
                String password2 = passwordAgainText.getText();
                String pseudonym = userNameText.getText();

                if (password != null && !"".equals(password) && password2 != null && !"".equals(password2)
                        && password.equals(password2)) {
                    password2ValidateLabel.setText("");
                    if (pseudonym != null && !"".equals(pseudonym)) {
                        if (agreeButton.getSelection() && isEmailValid()) {
                            createAccountButton.setEnabled(true);
                        }
                    } else {
                        createAccountButton.setEnabled(false);
                    }
                } else {
                    createAccountButton.setEnabled(false);
                    password2ValidateLabel.setText(notValidateStr);
                }
            }
        });

        emailText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                String password = passwordText.getText();
                String password2 = passwordAgainText.getText();
                String pseudonym = userNameText.getText();
                if (password != null && !"".equals(password) && password2 != null && !"".equals(password2)
                        && password.equals(password2) && pseudonym != null && !"".equals(pseudonym) && isEmailValid()
                        && agreeButton.getSelection()) {
                    createAccountButton.setEnabled(true);
                } else {
                    createAccountButton.setEnabled(false);
                }
            }
        });

        agreeButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                if (agreeButton.getSelection()) {
                    String password = passwordText.getText();
                    String password2 = passwordAgainText.getText();
                    String pseudonym = userNameText.getText();
                    if (password != null && !"".equals(password) && password2 != null && !"".equals(password2)
                            && password.equals(password2) && pseudonym != null && !"".equals(pseudonym) && isEmailValid()) {
                        createAccountButton.setEnabled(true);
                    } else {
                        createAccountButton.setEnabled(false);
                    }
                } else {
                    createAccountButton.setEnabled(false);
                }
            }
        });

        link.addHyperlinkListener(new HyperlinkAdapter() {

            public void linkActivated(HyperlinkEvent e) {
                stackLayout.topControl = connectAccount;
                downComposite.layout();
            }
        });

        linkToCreate.addHyperlinkListener(new HyperlinkAdapter() {

            public void linkActivated(HyperlinkEvent e) {
                stackLayout.topControl = createAccount;
                downComposite.layout();
            }
        });

        //
        usernameTextForConnect.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                String email = usernameTextForConnect.getText();
                String password = passwordTextForconnect.getText();
                if (email != null && !email.trim().equals("") && password != null && !password.trim().equals("")) {
                    connectButton.setEnabled(true);
                } else {
                    connectButton.setEnabled(false);
                }
            }
        });

        passwordTextForconnect.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                String email = usernameTextForConnect.getText();
                String password = passwordTextForconnect.getText();
                if (email != null && !email.trim().equals("") && password != null && !password.trim().equals("")) {
                    connectButton.setEnabled(true);
                } else {
                    connectButton.setEnabled(false);
                }
            }
        });

        connectButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                String username = usernameTextForConnect.getText().trim();
                String password = passwordTextForconnect.getText().trim();

                boolean success = false;
                try {
                    success = RegisterManagement.getInstance().createUser(username, password, "", "", "", isProxyEnable,
                            proxyHost, proxyPort, proxyUser, proxyPassword);
                } catch (BusinessException e1) {
                    ExceptionHandler.process(e1);
                }

                if (success) {
                    // check the password
                    boolean isUserPassRight = true;
                    if (PluginChecker.isExchangeSystemLoaded()) {
                        IExchangeService service = (IExchangeService) GlobalServiceRegister.getDefault().getService(
                                IExchangeService.class);
                        String checkUserAndPass = service.checkUserAndPass(username, password);
                        if (checkUserAndPass != null) {
                            isUserPassRight = false;
                            MessageDialog.openInformation(getShell(), Messages.getString("TalendForgeDialog.MessageTitle"),
                                    checkUserAndPass);
                        }
                    }
                    if (isUserPassRight) {
                        IPreferenceStore prefStore = PlatformUI.getPreferenceStore();
                        String connectionEmail = project.getAuthor().getLogin();
                        prefStore.setValue(connectionEmail, "notused" + ":" + username + ":" + password);
                        // bug TDI-19619,when connect correct,no need openInformation.
                        // MessageDialog.openInformation(getShell(),
                        // Messages.getString("TalendForgeDialog.MessageTitle"),
                        // Messages.getString("TalendForgeDialog.ConnectSuccessMessage"));
                        okPressed();
                    }
                }
            }
        });
    }

    private boolean isEmailValid() {
        String email = emailText.getText();
        if (email != null && !"".equals(email)) {
            if (!Pattern.matches(RepositoryConstants.MAIL_PATTERN, email)) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void okPressed() {
        super.okPressed();
        font.dispose();
    }

    private String[] initiateCountryList() {
        SortedSet<String> countryList = new TreeSet<String>();
        for (Locale locale : Locale.getAvailableLocales()) {
            if (locale.getDisplayCountry().compareTo("") != 0) { //$NON-NLS-1$
                countryList.add(locale.getDisplayCountry());
            }
        }

        String defaultCountry = Locale.getDefault().getDisplayCountry();
        int i = 0;

        if (oldCountry != null) {
            for (String country : countryList) {

                if (country.equals(oldCountry)) {
                    countryToSelect = i;
                    break;
                }
                i++;
            }
        } else {
            for (String country : countryList) {
                if (country.equals(defaultCountry)) {
                    countryToSelect = i;
                    break;
                }
                i++;
            }
        }
        return countryList.toArray(new String[] {});
    }

    /**
     * Canvas displaying an image.<br/>
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

    /**
     * DOC zwzhao TalendForgeDialog class global comment. Detailled comment
     */
    class NetworkSettingDialog extends Dialog {

        private Text proxyHostText;

        private Text proxyPortText;

        private Text proxyUserText;

        private Text proxyPassworkText;

        private String[] proxyString;

        /**
         * DOC zli NetworSettingDialog constructor comment.
         * 
         * @param parentShell
         */
        protected NetworkSettingDialog(Shell parentShell) {
            super(parentShell);
        }

        @Override
        protected void configureShell(Shell newShell) {

            super.configureShell(newShell);
            newShell.setModified(true);
            newShell.setSize(350, 300);
            newShell.setText(Messages.getString("TalendForgeDialog.netWorkSetting")); //$NON-NLS-1$
        }

        @Override
        protected Control createDialogArea(Composite parent) {

            GridData layoutData;

            Composite center = new Composite(parent, SWT.NONE);
            center.setLayout(new GridLayout(2, false));
            layoutData = new GridData(GridData.FILL_BOTH);
            layoutData.horizontalIndent = 20;
            layoutData.verticalIndent = 40;

            center.setLayoutData(layoutData);

            Label proxyHostLabel = new Label(center, SWT.NONE);
            proxyHostLabel.setText(Messages.getString("TalendForgeDialog.proxyHost"));
            proxyHostText = new Text(center, SWT.BORDER);
            layoutData = new GridData(GridData.FILL_HORIZONTAL);
            proxyHostText.setLayoutData(layoutData);
            if (proxyHost != null) {
                proxyHostText.setText(proxyHost);
            }

            Label proxyPortLabel = new Label(center, SWT.NONE);
            proxyPortLabel.setText(Messages.getString("TalendForgeDialog.proxyPort"));
            proxyPortText = new Text(center, SWT.BORDER);
            layoutData = new GridData(GridData.FILL_HORIZONTAL);
            proxyPortText.setLayoutData(layoutData);
            if (proxyPort != null) {
                proxyPortText.setText(proxyPort);
            }

            Label proxyUserLabel = new Label(center, SWT.NONE);
            proxyUserLabel.setText(Messages.getString("TalendForgeDialog.proxyUser"));
            proxyUserText = new Text(center, SWT.BORDER);
            layoutData = new GridData(GridData.FILL_HORIZONTAL);
            proxyUserText.setLayoutData(layoutData);
            if (proxyUser != null) {
                proxyUserText.setText(proxyUser);
            }

            Label proxyPasswordLabel = new Label(center, SWT.NONE);
            proxyPasswordLabel.setText(Messages.getString("TalendForgeDialog.proxyPassword"));
            proxyPassworkText = new Text(center, SWT.BORDER);
            layoutData = new GridData(GridData.FILL_HORIZONTAL);
            proxyPassworkText.setLayoutData(layoutData);
            if (proxyPassword != null) {
                proxyPassworkText.setText(proxyPassword);
            }

            return center;

        }

        @Override
        protected void okPressed() {

            String[] proxyString = new String[4];
            proxyString[0] = proxyHostText.getText();
            proxyString[1] = proxyPortText.getText();
            proxyString[2] = proxyUserText.getText();
            proxyString[3] = proxyPassworkText.getText();
            setProxyString(proxyString);
            super.okPressed();
        }

        private String[] getProxyString() {
            return this.proxyString;
        }

        private void setProxyString(String[] proxyString) {
            this.proxyString = proxyString;
        }

        @Override
        protected void cancelPressed() {
            super.cancelPressed();
            // font.dispose();
        }
    }
}
