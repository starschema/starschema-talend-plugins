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
package org.talend.repository.ui.login.connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.formtools.LabelText;
import org.talend.commons.ui.swt.formtools.LabelledCombo;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.ConnectionBean;
import org.talend.core.repository.model.DynamicButtonBean;
import org.talend.core.repository.model.DynamicChoiceBean;
import org.talend.core.repository.model.DynamicFieldBean;
import org.talend.core.repository.model.IRepositoryFactory;
import org.talend.core.repository.model.RepositoryFactoryProvider;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.ui.login.LoginComposite;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 * 
 */
public class ConnectionFormComposite extends Composite {

    private ConnectionsDialog dialog;

    private FormToolkit toolkit;

    private ComboViewer repositoryCombo;

    private Text nameText;

    private Text descriptionText;

    private Text userText;

    private Text passwordText;

    private Text workSpaceText;

    private Button workSpaceButton;

    private ConnectionBean connection;

    private ConnectionsListComposite connectionsListComposite;

    private Map<IRepositoryFactory, Map<String, LabelText>> dynamicControls = new HashMap<IRepositoryFactory, Map<String, LabelText>>();

    private Map<IRepositoryFactory, Map<String, LabelText>> dynamicRequiredControls = new HashMap<IRepositoryFactory, Map<String, LabelText>>();

    private Map<IRepositoryFactory, Map<String, Button>> dynamicButtons = new HashMap<IRepositoryFactory, Map<String, Button>>();

    private Map<IRepositoryFactory, Map<String, LabelledCombo>> dynamicChoices = new HashMap<IRepositoryFactory, Map<String, LabelledCombo>>();

    /**
     * DOC smallet ConnectionsComposite constructor comment.
     * 
     * @param parent
     * @param style
     */
    public ConnectionFormComposite(Composite parent, int style, ConnectionsListComposite connectionsListComposite,
            ConnectionsDialog dialog) {
        super(parent, style);
        this.dialog = dialog;
        this.connectionsListComposite = connectionsListComposite;

        toolkit = new FormToolkit(this.getDisplay());
        ScrolledForm form = toolkit.createScrolledForm(this);
        Composite formBody = form.getBody();

        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);
        form.setLayoutData(new GridData(GridData.FILL_BOTH));

        final int hSpan = 10;
        final int labelHSpan = 3;
        final int btnHSpan = 2;

        GridLayout lay = new GridLayout();
        lay.numColumns = hSpan;
        formBody.setLayout(lay);

        // Use by all widgets:
        GridData data1;

        // Repository
        Label repositoryLabel = toolkit.createLabel(formBody, Messages.getString("connections.form.field.repository")); //$NON-NLS-1$
        data1 = new GridData();
        data1.horizontalSpan = labelHSpan;
        repositoryLabel.setLayoutData(data1);

        repositoryCombo = new ComboViewer(formBody, SWT.BORDER | SWT.READ_ONLY);
        repositoryCombo.setContentProvider(new ArrayContentProvider());
        repositoryCombo.setLabelProvider(new RepositoryFactoryLabelProvider());
        repositoryCombo.getControl().setLayoutData(createGridLayoutData(hSpan - labelHSpan));

        // Name
        Label nameLabel = toolkit.createLabel(formBody, Messages.getString("connections.form.field.name")); //$NON-NLS-1$
        data1 = new GridData();
        data1.horizontalSpan = labelHSpan;
        nameLabel.setLayoutData(data1);

        nameText = toolkit.createText(formBody, "", SWT.BORDER); //$NON-NLS-1$
        nameText.setLayoutData(createGridLayoutData(hSpan - labelHSpan));

        // Comment
        Label descriptionLabel = toolkit.createLabel(formBody, Messages.getString("connections.form.field.description")); //$NON-NLS-1$
        data1 = new GridData();
        data1.horizontalSpan = labelHSpan;
        descriptionLabel.setLayoutData(data1);

        descriptionText = toolkit.createText(formBody, "", SWT.BORDER); //$NON-NLS-1$
        descriptionText.setLayoutData(createGridLayoutData(hSpan - labelHSpan));

        // User
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        boolean usesMailCheck = brandingService.getBrandingConfiguration().isUseMailLoginCheck();
        Label userLabel;
        if (usesMailCheck) {
            userLabel = toolkit.createLabel(formBody, Messages.getString("connections.form.field.username")); //$NON-NLS-1$
        } else {
            userLabel = toolkit.createLabel(formBody, Messages.getString("connections.form.field.usernameNoMail")); //$NON-NLS-1$
        }

        data1 = new GridData(GridData.FILL_HORIZONTAL);
        data1.horizontalSpan = labelHSpan;
        userLabel.setLayoutData(data1);

        userText = toolkit.createText(formBody, "", SWT.BORDER); //$NON-NLS-1$
        userText.setLayoutData(createGridLayoutData(hSpan - labelHSpan));

        // Password
        Label passwordLabel = toolkit.createLabel(formBody, Messages.getString("connections.form.field.password")); //$NON-NLS-1$
        data1 = new GridData();
        data1.horizontalSpan = labelHSpan;
        passwordLabel.setLayoutData(data1);

        passwordText = toolkit.createText(formBody, "", SWT.PASSWORD | SWT.BORDER); //$NON-NLS-1$
        passwordText.setLayoutData(createGridLayoutData(hSpan - labelHSpan));

        Label workSpaceLabel = toolkit.createLabel(formBody, Messages.getString("ConnectionFormComposite.WORKSPACE")); //$NON-NLS-1$
        data1 = new GridData();
        data1.horizontalSpan = labelHSpan;
        workSpaceLabel.setLayoutData(data1);

        workSpaceText = toolkit.createText(formBody, "", SWT.BORDER); //$NON-NLS-1$
        data1 = createGridLayoutData(hSpan - labelHSpan - btnHSpan);
        // data1.widthHint = ConnectionsDialog.STANDARD_TEXT_WIDTH - 50;
        workSpaceText.setLayoutData(data1);

        workSpaceButton = toolkit.createButton(formBody, null, SWT.PUSH);
        data1 = new GridData();
        data1.horizontalSpan = btnHSpan;
        workSpaceButton.setToolTipText(Messages.getString("ConnectionFormComposite.SELECT_WORKSPACE")); //$NON-NLS-1$
        workSpaceButton.setImage(ImageProvider.getImage(EImage.THREE_DOTS_ICON));
        workSpaceButton.setLayoutData(data1);

        GridData data;
        List<IRepositoryFactory> availableRepositories = getUsableRepositoryProvider();
        for (IRepositoryFactory current : availableRepositories) {
            Map<String, LabelText> list = new HashMap<String, LabelText>();
            Map<String, LabelText> listRequired = new HashMap<String, LabelText>();
            Map<String, Button> listButtons = new HashMap<String, Button>();
            Map<String, LabelledCombo> listChoices = new HashMap<String, LabelledCombo>();
            dynamicControls.put(current, list);
            dynamicRequiredControls.put(current, listRequired);
            dynamicButtons.put(current, listButtons);
            dynamicChoices.put(current, listChoices);
            Control baseControl = passwordLabel;

            for (final DynamicChoiceBean currentChoiceBean : current.getChoices()) {
                Label label = toolkit.createLabel(formBody, currentChoiceBean.getName());
                data = new GridData();
                data.horizontalSpan = labelHSpan;
                label.setLayoutData(data);

                Combo combo = new Combo(formBody, SWT.BORDER | SWT.READ_ONLY);
                for (String label1 : currentChoiceBean.getChoices().values()) {
                    combo.add(label1);
                }

                data = new GridData(GridData.FILL_HORIZONTAL);
                data.horizontalSpan = hSpan - labelHSpan;
                combo.setLayoutData(createGridLayoutData(hSpan - labelHSpan));

                baseControl = combo;

                listChoices.put(currentChoiceBean.getId(), new LabelledCombo(label, combo));
            }

            for (DynamicFieldBean currentField : current.getFields()) {
                int textStyle = SWT.BORDER;
                if (currentField.isPassword()) {
                    textStyle = textStyle | SWT.PASSWORD;
                }
                Label label = toolkit.createLabel(formBody, currentField.getName());
                data = new GridData();
                data.horizontalSpan = labelHSpan;
                label.setLayoutData(data);

                Text text = toolkit.createText(formBody, "", textStyle); //$NON-NLS-1$

                text.setLayoutData(createGridLayoutData(hSpan - labelHSpan));

                baseControl = text;

                LabelText labelText = new LabelText(label, text);
                if (currentField.isRequired()) {
                    listRequired.put(currentField.getId(), labelText);
                }
                list.put(currentField.getId(), labelText);
            }

            for (final DynamicButtonBean currentButtonBean : current.getButtons()) {
                Label label = toolkit.createLabel(formBody, ""); //$NON-NLS-1$
                label.setVisible(false);
                data = new GridData();
                data.horizontalSpan = labelHSpan;
                label.setLayoutData(data);

                Button button = new Button(formBody, SWT.PUSH);
                button.setText(currentButtonBean.getName());
                button.addSelectionListener(new DelegateSelectionListener(currentButtonBean));

                button.setLayoutData(createGridLayoutData(hSpan - labelHSpan));

                baseControl = button;

                listButtons.put(currentButtonBean.getId(), button);
            }
        }

        addListeners();
        addWorkSpaceListener();
        fillLists();
        showHideDynamicsControls();
        showHideTexts();
        // validateFields();
    }

    private GridData createGridLayoutData(int horizontalSpan) {
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = horizontalSpan;
        gridData.widthHint = ConnectionsDialog.STANDARD_TEXT_WIDTH;
        return gridData;

    }

    public boolean canFinish() {
        return validateFields();
    }

    private boolean validateFields() {
        String errorMsg = null;
        boolean valid = true;
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        boolean usesMailCheck = brandingService.getBrandingConfiguration().isUseMailLoginCheck();
        if (valid && getRepository() == null) {
            valid = false;
            errorMsg = Messages.getString("connections.form.emptyField.repository"); //$NON-NLS-1$
        } else if (valid && getName().length() == 0) {
            valid = false;
            errorMsg = Messages.getString("connections.form.emptyField.connname"); //$NON-NLS-1$
        } else if (valid && getUser().length() == 0) {
            valid = false;
            errorMsg = Messages.getString("connections.form.emptyField.username"); //$NON-NLS-1$
        } else if (valid && usesMailCheck && !Pattern.matches(RepositoryConstants.MAIL_PATTERN, getUser())) {
            valid = false;
            errorMsg = Messages.getString("connections.form.malformedField.username"); //$NON-NLS-1$
        } else {
            for (LabelText current : dynamicRequiredControls.get(getRepository()).values()) {
                if (valid && current.getText().length() == 0) {
                    valid = false;
                    errorMsg = Messages.getString("connections.form.dynamicFieldEmpty", current.getLabel()); //$NON-NLS-1$
                }
            }
        }

        if (!valid) {
            dialog.setErrorMessage(errorMsg);
        } else {
            dialog.setErrorMessage(null);
        }

        if (connection != null) {
            connection.setComplete(valid);
        }
        return valid;
    }

    private void showHideDynamicsControls() {
        // PTODO SML Optimize
        // 1. Hide all controls:
        for (IRepositoryFactory f : dynamicControls.keySet()) {
            for (LabelText control : dynamicControls.get(f).values()) {
                control.setVisible(false);
            }

            for (Button control : dynamicButtons.get(f).values()) {
                control.setVisible(false);
            }

            for (LabelledCombo control : dynamicChoices.get(f).values()) {
                control.setVisible(false);
            }
        }

        // 2. Show active repository controls:
        if (getRepository() != null) {
            for (LabelText control : dynamicControls.get(getRepository()).values()) {
                control.setVisible(true);
            }

            for (Button control : dynamicButtons.get(getRepository()).values()) {
                control.setVisible(true);
            }

            for (LabelledCombo control : dynamicChoices.get(getRepository()).values()) {
                control.setVisible(true);
            }

        }
    }

    private void showHideTexts() {
        if (connection != null) {
            IRepositoryFactory factory = RepositoryFactoryProvider.getRepositoriyById(connection.getRepositoryId());
            if (factory != null) {
                boolean authenticationNeeded = factory.isAuthenticationNeeded();
                if (authenticationNeeded) {
                    passwordText.setEnabled(true);
                    passwordText.setEditable(true);
                    passwordText.setBackground(LoginComposite.WHITE_COLOR);
                } else {
                    passwordText.setText(""); //$NON-NLS-1$
                    passwordText.setEnabled(false);
                    passwordText.setEditable(false);
                    passwordText.setBackground(LoginComposite.GREY_COLOR);
                }
            }
        }
    }

    public IRepositoryFactory getRepository() {
        IRepositoryFactory repositoryFactory = null;
        IStructuredSelection sel = (IStructuredSelection) repositoryCombo.getSelection();
        repositoryFactory = (IRepositoryFactory) sel.getFirstElement();
        return repositoryFactory;
    }

    private String getName() {
        return nameText.getText();
    }

    private String getUser() {
        return userText.getText();
    }

    ModifyListener standardTextListener = new ModifyListener() {

        public void modifyText(ModifyEvent e) {
            validateFields();
            fillBean();
        }
    };

    ISelectionChangedListener repositoryListener = new ISelectionChangedListener() {

        public void selectionChanged(SelectionChangedEvent e) {
            showHideDynamicsControls();
            validateFields();
            fillBean();
            showHideTexts();

        }

    };

    private void addListeners() {
        repositoryCombo.addPostSelectionChangedListener(repositoryListener);
        nameText.addModifyListener(standardTextListener);
        descriptionText.addModifyListener(standardTextListener);
        userText.addModifyListener(standardTextListener);
        passwordText.addModifyListener(standardTextListener);
        workSpaceText.addModifyListener(standardTextListener);

        for (IRepositoryFactory f : dynamicControls.keySet()) {
            for (LabelText control : dynamicControls.get(f).values()) {
                control.addModifyListener(standardTextListener);
            }
        }

        for (IRepositoryFactory f : dynamicChoices.keySet()) {
            for (LabelledCombo control : dynamicChoices.get(f).values()) {
                control.getCombo().addModifyListener(standardTextListener);
            }
        }
    }

    private void addWorkSpaceListener() {
        workSpaceButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dirDialog = new DirectoryDialog(dialog.getShell());
                String path = dirDialog.open();
                if (path == null || "".equals(path)) { //$NON-NLS-1$
                    workSpaceText.setText(getRecentWorkSpace());
                } else {
                    //                    String cPath = path.replaceAll("\\\\", File.separator); //$NON-NLS-1$ //$NON-NLS-2$                   
                    workSpaceText.setText(path);

                }
            }
        });
    }

    private void removeListeners() {
        repositoryCombo.removePostSelectionChangedListener(repositoryListener);
        nameText.removeModifyListener(standardTextListener);
        descriptionText.removeModifyListener(standardTextListener);
        userText.removeModifyListener(standardTextListener);
        passwordText.removeModifyListener(standardTextListener);
        workSpaceText.removeModifyListener(standardTextListener);

        for (IRepositoryFactory f : dynamicControls.keySet()) {
            for (LabelText control : dynamicControls.get(f).values()) {
                control.removeModifyListener(standardTextListener);
            }
        }

        for (IRepositoryFactory f : dynamicChoices.keySet()) {
            for (LabelledCombo control : dynamicChoices.get(f).values()) {
                control.getCombo().removeModifyListener(standardTextListener);
            }
        }
    }

    private void fillBean() {
        if (connection != null) {
            if (getRepository() != null) {
                connection.setRepositoryId(getRepository().getId());

                Map<String, String> connFields = new HashMap<String, String>();

                Map<String, LabelText> map = dynamicControls.get(getRepository());
                for (String fieldKey : map.keySet()) {
                    connFields.put(fieldKey, map.get(fieldKey).getText());
                }

                Map<String, LabelledCombo> map2 = dynamicChoices.get(getRepository());
                for (String fieldKey : map2.keySet()) {
                    for (DynamicChoiceBean dynamicChoiceBean : getRepository().getChoices()) {
                        if (dynamicChoiceBean.getId().equals(fieldKey)) {
                            int selectionIndex = map2.get(fieldKey).getCombo().getSelectionIndex();
                            connFields.put(fieldKey, dynamicChoiceBean.getChoiceValue(selectionIndex));
                        }
                    }
                }

                connection.setDynamicFields(connFields);
            }
            connection.setName(nameText.getText());
            connection.setDescription(descriptionText.getText());
            connection.setUser(userText.getText());
            connection.setPassword(passwordText.getText());
            connection.setWorkSpace(workSpaceText.getText());

            connectionsListComposite.refresh(connection);
        }
    }

    private void fillLists() {
        List<IRepositoryFactory> availableRepositories = getUsableRepositoryProvider();
        repositoryCombo.setInput(availableRepositories);
        fillFields();
    }

    private List<IRepositoryFactory> getUsableRepositoryProvider() {
        List<IRepositoryFactory> availableRepositories = RepositoryFactoryProvider.getAvailableRepositories();

        List<IRepositoryFactory> result = new ArrayList<IRepositoryFactory>();
        for (IRepositoryFactory repositoryFactory : availableRepositories) {
            if (repositoryFactory.isDisplayToUser()) {
                result.add(repositoryFactory);
            }
        }
        return result;
    }

    private void fillFields() {
        if (connection != null) {
            removeListeners();
            String repositoryId = connection.getRepositoryId();
            IRepositoryFactory repositoriyById = RepositoryFactoryProvider.getRepositoriyById(repositoryId);
            repositoryCombo.setSelection(new StructuredSelection(new Object[] { repositoriyById }));

            if (getRepository() != null) {
                Map<String, LabelText> map = dynamicControls.get(getRepository());

                for (String fieldKey : map.keySet()) {
                    LabelText current = map.get(fieldKey);
                    String string = connection.getDynamicFields().get(fieldKey);
                    current.setText(string == null ? "" : string); //$NON-NLS-1$
                }

                Map<String, LabelledCombo> map2 = dynamicChoices.get(getRepository());
                for (String fieldKey : map2.keySet()) {
                    Combo combo = map2.get(fieldKey).getCombo();
                    String value = connection.getDynamicFields().get(fieldKey);
                    combo.deselectAll();
                    for (DynamicChoiceBean dynamicChoiceBean : getRepository().getChoices()) {
                        if (dynamicChoiceBean.getId().equals(fieldKey)) {
                            combo.select(dynamicChoiceBean.getChoiceIndex(value));
                        }
                    }
                }

            }
            nameText.setText((connection.getName() == null ? "" : connection.getName())); //$NON-NLS-1$
            descriptionText.setText((connection.getDescription() == null ? "" : connection.getDescription())); //$NON-NLS-1$
            userText.setText((connection.getUser() == null ? "" : connection.getUser())); //$NON-NLS-1$
            passwordText.setText((connection.getPassword() == null ? "" : connection.getPassword())); //$NON-NLS-1$
            workSpaceText
                    .setText(("".equals(connection.getWorkSpace()) || connection.getWorkSpace() == null) ? getRecentWorkSpace() : connection.getWorkSpace());//$NON-NLS-1$
            addListeners();
        }
    }

    public String getRecentWorkSpace() {
        String filePath = new Path(Platform.getInstanceLocation().getURL().getPath()).toFile().getPath();
        // String defaultPath = Platform.getInstanceLocation().getURL().toString();
        //        String filePath = defaultPath.substring(defaultPath.indexOf("/"), defaultPath.length() - 1); //$NON-NLS-1$
        return filePath;
    }

    /**
     * DOC smallet Comment method "setConnection".
     * 
     * @param selected
     */
    public void setConnection(ConnectionBean selected) {
        this.connection = selected;
        fillFields();
        showHideDynamicsControls();
        validateFields();
        showHideTexts();
    }

    /**
     */
    private final class DelegateSelectionListener implements SelectionListener {

        private DynamicButtonBean bean;

        private DelegateSelectionListener(DynamicButtonBean bean) {
            this.bean = bean;
        }

        public void widgetDefaultSelected(SelectionEvent e) {
        }

        public void widgetSelected(SelectionEvent e) {
            e.data = connection;
            bean.getListener().widgetSelected(e);
        }
    }

    /**
     * DOC smallet LoginComposite class global comment. Detailled comment <br/>
     * 
     * $Id: LoginComposite.java 1380 2007-01-10 11:18:55Z smallet $
     * 
     */
    private class RepositoryFactoryLabelProvider extends LabelProvider {

        /**
         * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
         */
        @Override
        public String getText(Object element) {
            IRepositoryFactory prj = (IRepositoryFactory) element;
            return prj.getName();
        }
    }
}
