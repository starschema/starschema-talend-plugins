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
package org.talend.designer.core.ui.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.gmf.runtime.common.ui.preferences.CheckBoxFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.EDatabaseConnVar;
import org.talend.core.database.conn.template.EDatabaseConnTemplate;
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
import org.talend.core.model.param.ERepositoryCategoryType;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.proposal.TalendProposalUtils;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.process.jobsettings.JobSettingsConstants;
import org.talend.designer.core.model.process.jobsettings.JobSettingsConstants.ContextLoadInfo;
import org.talend.designer.core.ui.editor.properties.ContextParameterExtractor;
import org.talend.designer.core.ui.preferences.StatsAndLogsPreferencePage.FileFieldEditorWithQuotes;
import org.talend.designer.core.ui.views.jobsettings.ImplicitContextLoadHelper;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;

/**
 * DOC hcw class global comment. Detailled comment
 * 
 * @deprecated see ImplicitContextLoadProjectSettingPage
 */
public class ImplicitContextLoadPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public static final String CONNECTION_ITEM_LABEL = "_CONNECTION_ITEM_LABEL"; //$NON-NLS-1$

    private ECodeLanguage language;

    private String languagePrefix;

    private Composite parent;

    private static String[] repositoryTypes = new String[] { EmfComponent.TEXT_BUILTIN, EmfComponent.TEXT_REPOSITORY };

    private List<FieldEditor> fileEditors;

    private List<FieldEditor> dbEditors;

    // control
    private CheckBoxFieldEditor implicitContextLoadField;

    // file
    private RadioFieldEditor fromFileField;

    private FileFieldEditor filePathField;

    private StringFieldEditor separatorField;

    // db
    private RadioFieldEditor fromDbField;

    private Combo repositoryTypeCombo;

    private Button buttonShowRepository;

    private Text textRepositoryType;

    private ConnectionItem connectionItem;

    private Composite dbTypeComposite;

    private Label repositoryTypeLabel;

    private ComboFieldEditor dbTypeField;

    private StringFieldEditor hostField;

    private StringFieldEditor portField;

    private StringFieldEditor dbNameField;

    private StringFieldEditor additionParamField;

    private StringFieldEditor schemaField;

    private StringFieldEditor userField;

    private StringFieldEditor passwordField;

    private FileFieldEditorWithQuotes dabasePathField;

    private StringFieldEditor dbTableField;

    private StringFieldEditor queryConditionField;

    // context load
    private ComboFieldEditor newVariableField;

    private ComboFieldEditor oldVariableField;

    private CheckBoxFieldEditor printOperationField;

    private CheckBoxFieldEditor disableErrorField;

    private CheckBoxFieldEditor disableWarningField;

    private CheckBoxFieldEditor disableInfoField;

    protected String dbId;

    private Composite variableComposite;

    public ImplicitContextLoadPreferencePage() {
        super(GRID);
        language = LanguageManager.getCurrentLanguage();
        languagePrefix = language.toString() + "_"; //$NON-NLS-1$
        this.setTitle(getTitle() + " (" + language.getCaseName() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        setPreferenceStore(DesignerPlugin.getDefault().getPreferenceStore());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        fileEditors = new ArrayList<FieldEditor>();
        dbEditors = new ArrayList<FieldEditor>();
        parent = getFieldEditorParent();

        implicitContextLoadField = new CheckBoxFieldEditor(getPreferenceName(EParameterName.IMPLICIT_TCONTEXTLOAD),
                EParameterName.IMPLICIT_TCONTEXTLOAD.getDisplayName(), parent);

        addField(implicitContextLoadField);

        createFileFields();
        createDbFields();
        createContextLoadFields();

        addListeners();
    }

    /**
     * DOC hcw Comment method "updateDbType".
     */
    private void updateDbType() {
        // update db type
        Object value = RepositoryToComponentProperty.getValue(connectionItem.getConnection(), "TYPE", null); //$NON-NLS-1$
        int languageType = (language.equals(ECodeLanguage.JAVA)) ? 1 : 0;
        String[] list = StatsAndLogsConstants.REPOSITORY_ITEMS[languageType];
        for (int i = 0; i < list.length; i++) {
            if (value.equals(list[i])) {
                dbTypeField.select(i);
                break;
            }
        }
    }

    private void addListeners() {

        repositoryTypeCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                onRepositoryTypeChange();
            }
        });

        buttonShowRepository.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                RepositoryReviewDialog dialog = new RepositoryReviewDialog(Display.getCurrent().getActiveShell(),
                        ERepositoryObjectType.METADATA, ERepositoryCategoryType.DATABASE.getName());

                if (dialog.open() == RepositoryReviewDialog.OK) {
                    dbId = dialog.getResult().getObject().getId();
                    connectionItem = (ConnectionItem) dialog.getResult().getObject().getProperty().getItem();

                    updateDbType();

                    String repositoryType = ImplicitContextLoadHelper.getRepositoryTypeLabel(connectionItem);
                    textRepositoryType.setText(repositoryType);

                    refreshEnabledControls();
                }
            }

        });

        SelectionListener listener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshEnabledControls();
            }
        };

        implicitContextLoadField.getCheckbox().addSelectionListener(listener);
        fromFileField.getRadio().addSelectionListener(listener);
        fromDbField.getRadio().addSelectionListener(listener);
        dbTypeField.getComboBoxControl(dbTypeComposite).addSelectionListener(listener);

    }

    /**
     * DOC hcw Comment method "refreshEnableControls".
     */
    private void refreshEnabledControls() {
        boolean fromFile = fromFileField.getBooleanValue();
        boolean fromDb = fromDbField.getBooleanValue();
        boolean contextLoad = implicitContextLoadField.getBooleanValue();

        // context load
        FieldEditor[] editors = { fromFileField, fromDbField, printOperationField, disableErrorField, disableWarningField,
                disableInfoField };
        for (FieldEditor e : editors) {
            if (e != null) {
                e.setEnabled(contextLoad, parent);
            }
        }
        if (language == ECodeLanguage.JAVA) {
            newVariableField.setEnabled(contextLoad, variableComposite);
            oldVariableField.setEnabled(contextLoad, variableComposite);
        }

        if (!contextLoad) {
            fromFile = fromDb = false;
        }

        // file fields
        for (FieldEditor e : fileEditors) {
            e.setEnabled(fromFile, parent);
        }

        // repository type
        for (Control c : new Control[] { repositoryTypeLabel, repositoryTypeCombo, textRepositoryType, buttonShowRepository }) {
            c.setEnabled(fromDb);
        }
        dbTypeField.setEnabled(fromDb, dbTypeComposite);

        setDbFieldsEnable(fromDb);

        // check db type
        if (fromDb) {
            refreshEnabledDbControls();
        }

    }

    private void setDbFieldsEnable(boolean f) {
        for (FieldEditor e : dbEditors) {
            e.setEnabled(f, parent);
        }
    }

    /**
     * DOC hcw Comment method "refreshEnabledDbControls".
     */
    private void refreshEnabledDbControls() {
        boolean isBuiltIn = repositoryTypeCombo.getText().equals(EmfComponent.TEXT_BUILTIN);
        String dbValue = dbTypeField.getFieldValue();

        dbTypeField.setEnabled(isBuiltIn, dbTypeComposite);

        if (isBuiltIn) {
            hostField.setEnabled(!isMatch(dbValue, "tAccessOutput", "tSQLiteOutput"), parent); //$NON-NLS-1$ //$NON-NLS-2$
            portField.setEnabled(!isMatch(dbValue, "tAccessOutput", "tSQLiteOutput", "tFirebirdOutput"), parent); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            dbNameField.setEnabled(!isMatch(dbValue, "tAccessOutput", "tSQLiteOutput", "tFirebirdOutput"), parent); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (language == ECodeLanguage.JAVA) {
                additionParamField.setEnabled(isMatch(dbValue, "tMSSqlOutput", "tInformixOutput", "tMysqlOutput"), parent); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            schemaField.setEnabled(isMatch(dbValue, "tOracleOutput", "tPostgresqlOutput"), parent); //$NON-NLS-1$ //$NON-NLS-2$
            userField.setEnabled(!isMatch(dbValue, "tSQLiteOutput"), parent); //$NON-NLS-1$
            passwordField.setEnabled(!isMatch(dbValue, "tSQLiteOutput"), parent); //$NON-NLS-1$
            dabasePathField.setEnabled(isMatch(dbValue, "tAccessOutput", "tSQLiteOutput", "tFirebirdOutput"), parent); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else if (!isBuiltIn && connectionItem != null) {
            // check connection url
            DatabaseConnection conn = (DatabaseConnection) connectionItem.getConnection();

            String databaseType = conn.getDatabaseType();
            EDatabaseConnTemplate template = EDatabaseConnTemplate.indexOfTemplate(databaseType);
            EDatabaseVersion4Drivers version = EDatabaseVersion4Drivers.indexOfByVersion(conn.getDbVersionString());
            String stringConnection = ""; //$NON-NLS-1$
            if (template != null) {
                stringConnection = template.getUrlTemplate(version);
            }

            setDbFieldsEnable(false);

            dbTableField.setEnabled(true, parent);
            queryConditionField.setEnabled(true, parent);
            userField.setEnabled(true, parent);
            passwordField.setEnabled(true, parent);

            boolean visible = true;

            if (stringConnection.contains(EDatabaseConnVar.HOST.getVariable())) {
                hostField.setEnabled(visible, parent);
            }
            if (stringConnection.contains(EDatabaseConnVar.PORT.getVariable())) {
                portField.setEnabled(visible, parent);
            }

            if (stringConnection.contains(EDatabaseConnVar.FILENAME.getVariable())) {
                if (EDatabaseTypeName.getTypeFromDisplayName(databaseType).equals(EDatabaseTypeName.SQLITE)) {
                    userField.setEnabled(false, parent);
                    passwordField.setEnabled(false, parent);
                } else {
                    userField.setEnabled(true, parent);
                    passwordField.setEnabled(true, parent);
                }
                dabasePathField.setEnabled(visible, parent);
            }

            if (EDatabaseConnTemplate.isSchemaNeeded(databaseType)) {
                schemaField.setEnabled(visible, parent);
            }

            if (EDatabaseConnTemplate.isAddtionParamsNeeded(databaseType) && additionParamField != null) {
                additionParamField.setEnabled(visible, parent);
            }

            if (EDatabaseConnTemplate.isDatabaseNeeded(databaseType)) {
                dbNameField.setEnabled(visible, parent);
            }

            processDbField(hostField, processQuotes(conn.getServerName()));
            processDbField(portField, processQuotes(conn.getPort()));
            processDbField(dbNameField, processQuotes(conn.getSID()));
            processDbField(schemaField, processQuotes(conn.getUiSchema()));
            processDbField(userField, processQuotes(conn.getUsername()));
            processDbField(passwordField, processQuotes(conn.getPassword()));

            processDbField(dabasePathField, conn.getDBRootPath() == null ? "" : processQuotes(conn.getFileFieldName())); //$NON-NLS-1$

            if (additionParamField != null) {
                processDbField(additionParamField, conn.getAdditionalParams() == null ? "" : processQuotes(conn //$NON-NLS-1$
                        .getAdditionalParams()));
            }
            if (template == EDatabaseConnTemplate.MSSQL) {
                schemaField.getTextControl(parent).setEditable(true);
                if (schemaField.getTextControl(parent).getText().equals("")) { //$NON-NLS-1$
                    schemaField.getTextControl(parent).setText(TalendTextUtils.addQuotes("dbo")); //$NON-NLS-1$
                }
            }

        }
    }

    private void processDbField(StringFieldEditor field, String value) {
        if (field.getTextControl(parent).isEnabled()) {
            field.getTextControl(parent).setText(value);
            field.getTextControl(parent).setEnabled(false);
        } else {
            field.getTextControl(parent).setText(""); //$NON-NLS-1$
        }
    }

    private boolean isMatch(String target, String... source) {
        return Arrays.asList(source).contains(target);
    }

    private void onRepositoryTypeChange() {
        if (repositoryTypeCombo.getText().equals(EmfComponent.TEXT_BUILTIN)) {
            textRepositoryType.setVisible(false);
            buttonShowRepository.setVisible(false);

            setDbFieldsEnable(true);
            refreshEnabledDbControls();
            clearAllDbFields();
        } else {
            textRepositoryType.setVisible(true);
            buttonShowRepository.setVisible(true);
            refreshEnabledDbControls();
        }
    }

    private void clearAllDbFields() {
        for (FieldEditor e : dbEditors) {
            if (e instanceof StringFieldEditor) {
                Text text = ((StringFieldEditor) e).getTextControl(parent);
                if (text.isEnabled()) {
                    text.setText(TalendTextUtils.addQuotes("")); //$NON-NLS-1$
                } else {
                    text.setText(""); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * DOC hcw Comment method "createContextLoadFields".
     */
    private void createContextLoadFields() {
        if (language == ECodeLanguage.JAVA) {

            variableComposite = createComposite(parent);

            String[] itemValues = new String[] { ContextLoadInfo.ERROR.getDisplayName(),
                    ContextLoadInfo.WARNING.getDisplayName(), ContextLoadInfo.INFO.getDisplayName() };

            newVariableField = new ComboFieldEditor(getPreferenceName(EParameterName.LOAD_NEW_VARIABLE),
                    EParameterName.LOAD_NEW_VARIABLE.getDisplayName(), createArray(itemValues, itemValues), variableComposite);

            oldVariableField = new ComboFieldEditor(getPreferenceName(EParameterName.NOT_LOAD_OLD_VARIABLE),
                    EParameterName.NOT_LOAD_OLD_VARIABLE.getDisplayName(), createArray(itemValues, itemValues), variableComposite);
            // addFields(newVariableField, oldVariableField);
        }

        printOperationField = new CheckBoxFieldEditor(getPreferenceName(EParameterName.PRINT_OPERATIONS),
                EParameterName.PRINT_OPERATIONS.getDisplayName(), parent);
        addField(printOperationField);

        if (language == ECodeLanguage.JAVA) {
            disableErrorField = new CheckBoxFieldEditor(getPreferenceName(EParameterName.DISABLE_ERROR),
                    EParameterName.DISABLE_ERROR.getDisplayName(), parent);
            addField(disableErrorField);
        }

        disableWarningField = new CheckBoxFieldEditor(getPreferenceName(EParameterName.DISABLE_WARNINGS),
                EParameterName.DISABLE_WARNINGS.getDisplayName(), parent);
        addField(disableWarningField);

        if (language == ECodeLanguage.JAVA) {
            disableInfoField = new CheckBoxFieldEditor(getPreferenceName(EParameterName.DISABLE_INFO),
                    EParameterName.DISABLE_INFO.getDisplayName(), parent);
            addField(disableInfoField);
        }

    }

    private Composite createComposite(Composite parent) {
        Composite display = new Composite(parent, SWT.NONE);
        updateLayout(display);
        GridData data = new GridData();
        data.horizontalSpan = 2;
        data.horizontalAlignment = GridData.FILL;
        display.setLayoutData(data);
        return display;
    }

    private void updateLayout(Composite composite) {
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 5;
        layout.marginHeight = 5;
        layout.horizontalSpacing = 5;
        layout.verticalSpacing = 5;
        composite.setLayout(layout);
    }

    /**
     * DOC hcw Comment method "createDbFields".
     */
    private void createDbFields() {
        // database
        fromDbField = new RadioFieldEditor(getPreferenceName(EParameterName.FROM_DATABASE_FLAG),
                EParameterName.FROM_DATABASE_FLAG.getDisplayName(), parent);

        addField(fromDbField);

        createRepositoryType();
        createDbType();

        hostField = createStringFieldEditor(EParameterName.HOST, parent);
        ContextParameterExtractor.installOn(hostField.getTextControl(parent), null, getPreferenceName(EParameterName.HOST), null);

        portField = createStringFieldEditor(EParameterName.PORT, parent);
        dbNameField = createStringFieldEditor(EParameterName.DBNAME, parent);

        addFieldEditors(dbEditors, hostField, portField, dbNameField);
        if (language == ECodeLanguage.JAVA) {
            additionParamField = createStringFieldEditor(EParameterName.PROPERTIES, parent);

            addFieldEditors(dbEditors, additionParamField);
        }

        schemaField = createStringFieldEditor(EParameterName.SCHEMA_DB, parent);
        userField = createStringFieldEditor(EParameterName.USER, parent);
        passwordField = createStringFieldEditor(EParameterName.PASS, parent);

        dabasePathField = new FileFieldEditorWithQuotes(getPreferenceName(EParameterName.DBFILE), EParameterName.DBFILE
                .getDisplayName(), parent);
        dbTableField = createStringFieldEditor(EParameterName.DBTABLE, parent);
        TalendProposalUtils.installOn(dabasePathField.getTextControl(parent), null);

        queryConditionField = createStringFieldEditor(EParameterName.QUERY_CONDITION, parent);

        addFieldEditors(dbEditors, schemaField, userField, passwordField, dabasePathField, dbTableField, queryConditionField);

        adjustLabelWidth();

    }

    /**
     * DOC hcw Comment method "adjustLabelWidth".
     */
    private void adjustLabelWidth() {
        // find longest label
        Label label = (language == ECodeLanguage.JAVA) ? additionParamField.getLabelControl(parent) : queryConditionField
                .getLabelControl(parent);
        int width = label.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        label = dbTypeField.getLabelControl(dbTypeComposite);
        ((GridData) label.getLayoutData()).widthHint = width;

        RowData rd = new RowData();
        rd.width = width + 3;
        repositoryTypeLabel.setLayoutData(rd);

    }

    private StringFieldEditor createStringFieldEditor(EParameterName param, Composite parent) {
        StringFieldEditor editor = new StringFieldEditor(getPreferenceName(param), param.getDisplayName(), parent);
        TalendProposalUtils.installOn(editor.getTextControl(parent), null);
        return editor;
    }

    /**
     * DOC hcw Comment method "createDbType".
     */
    private void createDbType() {
        dbTypeComposite = createComposite(parent);
        ((GridData) dbTypeComposite.getLayoutData()).horizontalSpan = 3;

        dbTypeField = new ComboFieldEditor(getPreferenceName(EParameterName.DB_TYPE), EParameterName.DB_TYPE.getDisplayName(),
                getDbComboItems(), dbTypeComposite);
    }

    /**
     * DOC hcw Comment method "getDbComboItems".
     * 
     * @return
     */
    private String[][] getDbComboItems() {
        int languageType = language == ECodeLanguage.JAVA ? 1 : 0;
        String[] strDisplay, strValue;
        strDisplay = StatsAndLogsConstants.DISPLAY_DBNAMES[languageType];
        strValue = JobSettingsConstants.DB_OUTPUT_COMPONENTS[languageType];
        return createArray(strDisplay, strValue);
    }

    /**
     * DOC hcw Comment method "createArray".
     * 
     * @param strDisplay
     * @param strValue
     * @return
     */
    private String[][] createArray(String[] strDisplay, String[] strValue) {
        String[][] namesAndValues = new String[strDisplay.length][2];

        for (int i = 0; i < strDisplay.length; i++) {
            namesAndValues[i][0] = strDisplay[i];
            namesAndValues[i][1] = strValue[i];
        }
        return namesAndValues;
    }

    /**
     * DOC hcw Comment method "createRepositoryType".
     */
    private void createRepositoryType() {
        // repositoryTypes
        Composite comboTypePanel = new Composite(parent, SWT.NONE);
        GridData layout = new GridData(GridData.FILL_HORIZONTAL);
        layout.horizontalSpan = 3;

        comboTypePanel.setLayoutData(layout);
        RowLayout rowLayout = new RowLayout();
        rowLayout.spacing = 5;
        rowLayout.marginLeft = 0;
        comboTypePanel.setLayout(rowLayout);

        repositoryTypeLabel = new Label(comboTypePanel, SWT.NONE);
        repositoryTypeLabel.setText("Repository Type"); //$NON-NLS-1$

        repositoryTypeCombo = new Combo(comboTypePanel, SWT.READ_ONLY);
        repositoryTypeCombo.setItems(repositoryTypes);

        // select current repository type
        String currentType = getPreferenceStore().getString(getPreferenceName(EParameterName.PROPERTY_TYPE));

        int currentTypeIndex = currentType.equals(EmfComponent.REPOSITORY) ? 1 : 0;
        repositoryTypeCombo.select(currentTypeIndex);

        String dbTypeLabel = getPreferenceStore().getString(
                getPreferenceName(EParameterName.REPOSITORY_PROPERTY_TYPE) + CONNECTION_ITEM_LABEL);

        connectionItem = ImplicitContextLoadHelper.findConnectionItemByLabel(dbTypeLabel);

        textRepositoryType = new Text(comboTypePanel, SWT.SINGLE | SWT.BORDER);
        textRepositoryType.setVisible(currentType.equals(EmfComponent.REPOSITORY));
        textRepositoryType.setEditable(false);
        textRepositoryType.setLayoutData(new RowData(260, SWT.DEFAULT));
        textRepositoryType.setText(ImplicitContextLoadHelper.getRepositoryTypeLabel(connectionItem));

        buttonShowRepository = new Button(comboTypePanel, SWT.NONE);
        buttonShowRepository.setText("..."); //$NON-NLS-1$
        buttonShowRepository.setVisible(currentType.equals(EmfComponent.REPOSITORY));
    }

    /**
     * DOC hcw Comment method "createFileFields".
     */
    private void createFileFields() {
        // file
        fromFileField = new RadioFieldEditor(getPreferenceName(EParameterName.FROM_FILE_FLAG), EParameterName.FROM_FILE_FLAG
                .getDisplayName(), parent);

        filePathField = new FileFieldEditorWithQuotes(languagePrefix + EParameterName.IMPLICIT_TCONTEXTLOAD_FILE.getName(),
                EParameterName.IMPLICIT_TCONTEXTLOAD_FILE.getDisplayName(), parent);
        TalendProposalUtils.installOn(filePathField.getTextControl(parent), null);

        separatorField = new StringFieldEditor(getPreferenceName(EParameterName.FIELDSEPARATOR), EParameterName.FIELDSEPARATOR
                .getDisplayName(), parent);
        TalendProposalUtils.installOn(separatorField.getTextControl(parent), null);

        addField(fromFileField);
        addFieldEditors(fileEditors, filePathField, separatorField);
    }

    private String processQuotes(String text) {
        if (text == null) {
            return ""; //$NON-NLS-1$
        }
        if (ContextParameterUtils.isContainContextParam(text)) {
            return text;
        } else {
            return TalendTextUtils.addQuotes(text);
        }
    }

    /**
     * Initializes all field editors.
     */
    @Override
    protected void initialize() {
        super.initialize();
        FieldEditor[] editors = { dbTypeField, newVariableField, oldVariableField };
        for (FieldEditor pe : editors) {
            if (pe != null) {
                pe.setPage(this);
                pe.setPropertyChangeListener(this);
                pe.setPreferenceStore(getPreferenceStore());
                pe.load();
            }
        }
        refreshEnabledControls();
    }

    @Override
    public void dispose() {
        super.dispose();
        FieldEditor[] editors = { dbTypeField, newVariableField, oldVariableField };
        for (FieldEditor pe : editors) {
            if (pe != null) {
                pe.setPage(null);
                pe.setPropertyChangeListener(null);
                pe.setPreferenceStore(null);
            }

        }
    }

    @Override
    protected void performDefaults() {
        FieldEditor[] editors = { dbTypeField, newVariableField, oldVariableField };
        for (FieldEditor pe : editors) {
            pe.loadDefault();
        }
        super.performDefaults();
        refreshEnabledControls();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        FieldEditor[] editors = { dbTypeField, newVariableField, oldVariableField };
        for (FieldEditor e : editors) {
            if (e != null) {
                e.store();
            }
        }

        String value = repositoryTypeCombo.getText().equals(EmfComponent.TEXT_BUILTIN) ? EmfComponent.BUILTIN
                : EmfComponent.REPOSITORY;
        getPreferenceStore().setValue(getPreferenceName(EParameterName.PROPERTY_TYPE), value);

        getPreferenceStore().setValue(getPreferenceName(EParameterName.REPOSITORY_PROPERTY_TYPE),
                this.dbId == null ? "" : this.dbId); //$NON-NLS-1$
        String itemLabel = (connectionItem == null) ? "" : connectionItem.getProperty().getLabel(); //$NON-NLS-1$

        getPreferenceStore().setValue(getPreferenceName(EParameterName.REPOSITORY_PROPERTY_TYPE) + CONNECTION_ITEM_LABEL,
                itemLabel);
        return super.performOk();
    }

    private void addFieldEditors(List<FieldEditor> list, FieldEditor... editor) {
        for (FieldEditor e : editor) {
            if (list != null) {
                list.add(e);
            }
            addField(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
    }

    public String getPreferenceName(EParameterName param) {
        return languagePrefix + ImplicitContextLoadHelper.getExtraParameterName(param);
    }

    /**
     * 
     * DOC hcw ImplicitContextLoadPreferencePage class global comment. Detailled comment
     */
    static class ComboFieldEditor extends FieldEditor {

        /**
         * The <code>Combo</code> widget.
         */
        private Combo fCombo;

        /**
         * The value (not the name) of the currently selected item in the Combo widget.
         */
        private String fValue;

        /**
         * The names (labels) and underlying values to populate the combo widget. These should be arranged as: { {name1,
         * value1}, {name2, value2}, ...}
         */
        private String[][] fEntryNamesAndValues;

        /**
         * Create the combo box field editor.
         * 
         * @param name the name of the preference this field editor works on
         * @param labelText the label text of the field editor
         * @param entryNamesAndValues the names (labels) and underlying values to populate the combo widget. These
         * should be arranged as: { {name1, value1}, {name2, value2}, ...}
         * @param parent the parent composite
         */
        public ComboFieldEditor(String name, String labelText, String[][] entryNamesAndValues, Composite parent) {
            init(name, labelText);
            Assert.isTrue(checkArray(entryNamesAndValues));
            fEntryNamesAndValues = entryNamesAndValues;
            createControl(parent);
        }

        /**
         * Checks whether given <code>String[][]</code> is of "type" <code>String[][2]</code>.
         * 
         * @return <code>true</code> if it is ok, and <code>false</code> otherwise
         */
        private boolean checkArray(String[][] table) {
            if (table == null) {
                return false;
            }
            for (int i = 0; i < table.length; i++) {
                String[] array = table[i];
                if (array == null || array.length != 2) {
                    return false;
                }
            }
            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
         */
        @Override
        protected void adjustForNumColumns(int numColumns) {
            if (numColumns > 1) {
                Control control = getLabelControl();
                int left = numColumns;
                if (control != null) {
                    ((GridData) control.getLayoutData()).horizontalSpan = 1;
                    left = left - 1;
                }
                ((GridData) fCombo.getLayoutData()).horizontalSpan = left;
            } else {
                Control control = getLabelControl();
                if (control != null) {
                    ((GridData) control.getLayoutData()).horizontalSpan = 1;
                }
                ((GridData) fCombo.getLayoutData()).horizontalSpan = 1;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
         */
        @Override
        protected void doFillIntoGrid(Composite parent, int numColumns) {
            int comboC = 1;
            if (numColumns > 1) {
                comboC = numColumns - 1;
            }
            Control control = getLabelControl(parent);
            GridData gd = new GridData();
            gd.horizontalSpan = 1;
            control.setLayoutData(gd);
            control = getComboBoxControl(parent);
            gd = new GridData();
            gd.horizontalSpan = comboC;
            gd.horizontalAlignment = GridData.FILL;
            control.setLayoutData(gd);
            control.setFont(parent.getFont());
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.preference.FieldEditor#doLoad()
         */
        @Override
        protected void doLoad() {
            updateComboForValue(getPreferenceStore().getString(getPreferenceName()));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
         */
        @Override
        protected void doLoadDefault() {
            updateComboForValue(getPreferenceStore().getDefaultString(getPreferenceName()));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.preference.FieldEditor#doStore()
         */
        @Override
        protected void doStore() {
            if (fValue == null) {
                getPreferenceStore().setToDefault(getPreferenceName());
                return;
            }
            getPreferenceStore().setValue(getPreferenceName(), fValue);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
         */
        @Override
        public int getNumberOfControls() {
            return 2;
        }

        /*
         * Lazily create and return the Combo control.
         */
        protected Combo getComboBoxControl(Composite parent) {
            if (fCombo == null) {
                fCombo = new Combo(parent, SWT.READ_ONLY);
                fCombo.setFont(parent.getFont());
                for (int i = 0; i < fEntryNamesAndValues.length; i++) {
                    fCombo.add(fEntryNamesAndValues[i][0], i);
                }

                fCombo.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent evt) {
                        onComboChange();
                    }
                });
            }
            return fCombo;
        }

        /**
         * DOC hcw Comment method "onComboChange".
         */
        protected void onComboChange() {
            String oldValue = fValue;
            String name = fCombo.getText();
            fValue = getValueForName(name);
            setPresentsDefaultValue(false);
            fireValueChanged(VALUE, oldValue, fValue);

        }

        public void select(int index) {
            fCombo.select(index);
            onComboChange();
        }

        /*
         * Given the name (label) of an entry, return the corresponding value.
         */
        private String getValueForName(String name) {
            for (int i = 0; i < fEntryNamesAndValues.length; i++) {
                String[] entry = fEntryNamesAndValues[i];
                if (name.equals(entry[0])) {
                    return entry[1];
                }
            }
            return fEntryNamesAndValues[0][0];
        }

        /*
         * Set the name in the combo widget to match the specified value.
         */
        private void updateComboForValue(String value) {
            fValue = value;
            for (int i = 0; i < fEntryNamesAndValues.length; i++) {
                if (value.equals(fEntryNamesAndValues[i][1])) {
                    fCombo.setText(fEntryNamesAndValues[i][0]);
                    return;
                }
            }
            if (fEntryNamesAndValues.length > 0) {
                fValue = fEntryNamesAndValues[0][1];
                fCombo.setText(fEntryNamesAndValues[0][0]);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.preference.FieldEditor#setEnabled(boolean, org.eclipse.swt.widgets.Composite)
         */
        @Override
        public void setEnabled(boolean enabled, Composite parent) {
            super.setEnabled(enabled, parent);
            getComboBoxControl(parent).setEnabled(enabled);
        }

        public String getFieldValue() {
            return fValue;
        }

        @Override
        public void setPresentsDefaultValue(boolean booleanValue) {
            super.setPresentsDefaultValue(booleanValue);
        }
    }
}
