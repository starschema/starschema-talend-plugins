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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gmf.runtime.common.ui.preferences.CheckBoxFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.commons.ui.utils.workbench.preferences.ComboFieldEditor;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.EDatabaseConnVar;
import org.talend.core.database.conn.template.EDatabaseConnTemplate;
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
import org.talend.core.model.param.ERepositoryCategoryType;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.proposal.TalendProposalUtils;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.properties.ContextParameterExtractor;
import org.talend.designer.core.ui.views.statsandlogs.StatsAndLogsViewHelper;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;
import org.talend.repository.ui.views.RepositoryContentProvider;
import org.talend.repository.ui.views.RepositoryView;

/**
 * DOC Administrator class global comment. Detailled comment <br/>
 * 
 * @deprecated see StatLogsProjectSettingPage
 * 
 */
public abstract class StatsAndLogsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public static final String CONNECTION_ITEM_LABEL = "_CONNECTION_ITEM_LABEL"; //$NON-NLS-1$

    private static final String REPOSITORY = "Repository"; //$NON-NLS-1$

    private static final String BUILT_IN = "Built-In"; //$NON-NLS-1$

    private ECodeLanguage language;

    private String languagePrefix;

    /** controls. * */
    private CheckBoxFieldEditor onStatCatcherField;

    private CheckBoxFieldEditor onLogCatcherField;

    private CheckBoxFieldEditor onMetterCatcherField;

    private CheckBoxFieldEditor onFilesField;

    private DirectoryFieldEditorWithQuotes filePathField;

    private StringFieldEditor statsFileNameField;

    private StringFieldEditor logsFileNameField;

    private StringFieldEditor metterFileNameField;

    private CheckBoxFieldEditor onDatabaseField;

    private ComboFieldEditor dbTypeField;

    private StringFieldEditor hostField;

    private StringFieldEditor additionParamField;

    private FileFieldEditorWithQuotes dabasePathField;

    private StringFieldEditor portField;

    private StringFieldEditor dbNameField;

    private StringFieldEditor schemaField;

    private StringFieldEditor userField;

    private StringFieldEditor passwordField;

    private StringFieldEditor logsTableField;

    private StringFieldEditor statsTableField;

    private StringFieldEditor metterTableField;

    private CheckBoxFieldEditor catchRuntimeErrorsField;

    private CheckBoxFieldEditor catchUserErrorsField;

    private CheckBoxFieldEditor catchUserWarningField;

    private CheckBoxFieldEditor catchRealtimeStatsField;

    private Composite parent;

    private Composite dbTypeComposite;

    private Composite finalPart;

    private Combo comboRepositoryType;

    private ConnectionItem connectionItem;

    /**
     * Default constructor.
     */
    public StatsAndLogsPreferencePage(ECodeLanguage language) {
        super(GRID);
        this.language = language;
        setPreferenceStore(DesignerPlugin.getDefault().getPreferenceStore());
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to manipulate various
     * types of preferences. Each field editor knows how to save and restore itself.
     */
    @Override
    public void createFieldEditors() {
        languagePrefix = language.toString() + "_"; //$NON-NLS-1$
        createFields();
        updateEnableStateFromPreferences();
        addListeners();
    }

    private void updateDbFields(ConnectionItem connectionItem) {
        DatabaseConnection conn = (DatabaseConnection) connectionItem.getConnection();
        String databaseType = conn.getDatabaseType();
        EDatabaseConnTemplate template = EDatabaseConnTemplate.indexOfTemplate(databaseType);
        EDatabaseVersion4Drivers version = EDatabaseVersion4Drivers.indexOfByVersion(conn.getDbVersionString());
        String stringConnection = ""; //$NON-NLS-1$
        if (template != null) {
            stringConnection = template.getUrlTemplate(version);
        }

        setDbFieldsEnable(false);

        userField.setEnabled(true, parent);
        passwordField.setEnabled(true, parent);

        boolean visible = true;

        if (stringConnection.contains(EDatabaseConnVar.HOST.getVariable())) {
            hostField.setEnabled(visible, parent);
        }
        if (stringConnection.contains(EDatabaseConnVar.PORT.getVariable())) {
            portField.setEnabled(visible, parent);
        }

        if (stringConnection.contains(EDatabaseConnVar.SID.getVariable())
                || stringConnection.contains(EDatabaseConnVar.SERVICE_NAME.getVariable())) {
            // sidOrDatabaseText.setEditable(visible);
        }
        if (stringConnection.contains(EDatabaseConnVar.FILENAME.getVariable())) {
            if (EDatabaseTypeName.getTypeFromDisplayName(conn.getDatabaseType()).equals(EDatabaseTypeName.SQLITE)) {
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

        if (hostField.getTextControl(parent).isEnabled()) {
            hostField.getTextControl(parent).setText(processQuotes(conn.getServerName()));
            hostField.getTextControl(parent).setEnabled(false);
        } else {
            hostField.getTextControl(parent).setText(""); //$NON-NLS-1$
        }

        if (portField.getTextControl(parent).isEnabled()) {
            portField.getTextControl(parent).setText(processQuotes(conn.getPort()));
            portField.getTextControl(parent).setEnabled(false);
        } else {
            portField.getTextControl(parent).setText(""); //$NON-NLS-1$
        }

        if (dbNameField.getTextControl(parent).isEnabled()) {
            dbNameField.getTextControl(parent).setText(processQuotes(conn.getSID()));
            dbNameField.getTextControl(parent).setEditable(false);
        } else {
            dbNameField.getTextControl(parent).setText(""); //$NON-NLS-1$
        }

        if (schemaField.getTextControl(parent).isEnabled()) {
            schemaField.getTextControl(parent).setText(processQuotes(conn.getUiSchema()));
            schemaField.getTextControl(parent).setEditable(false);
        } else {
            schemaField.getTextControl(parent).setText(""); //$NON-NLS-1$
        }

        if (userField.getTextControl(parent).isEnabled()) {
            userField.getTextControl(parent).setText(processQuotes(conn.getUsername()));
            userField.getTextControl(parent).setEditable(false);
        } else {
            userField.getTextControl(parent).setText(""); //$NON-NLS-1$
        }

        if (passwordField.getTextControl(parent).isEnabled()) {
            passwordField.getTextControl(parent).setText(processQuotes(conn.getPassword()));
            passwordField.getTextControl(parent).setEditable(false);
        } else {
            passwordField.getTextControl(parent).setText(""); //$NON-NLS-1$
        }

        if (dabasePathField.getTextControl(parent).isEnabled()) {
            dabasePathField.getTextControl(parent).setText(
                    conn.getDBRootPath() == null ? "" : processQuotes(conn.getFileFieldName())); //$NON-NLS-1$
            dabasePathField.getTextControl(parent).setEditable(false);
        } else {
            dabasePathField.getTextControl(parent).setText(""); //$NON-NLS-1$
        }
        if (additionParamField != null) {
            if (additionParamField.getTextControl(parent).isEnabled()) {
                additionParamField.getTextControl(parent).setText(
                        conn.getAdditionalParams() == null ? "" : processQuotes(conn.getAdditionalParams())); //$NON-NLS-1$
                additionParamField.getTextControl(parent).setEditable(false);
            } else {
                additionParamField.getTextControl(parent).setText(""); //$NON-NLS-1$
            }
        }
        if (template == EDatabaseConnTemplate.MSSQL) {
            schemaField.getTextControl(parent).setEditable(true);
            if (schemaField.getTextControl(parent).getText().equals("")) { //$NON-NLS-1$
                schemaField.getTextControl(parent).setText(TalendTextUtils.addQuotes("dbo")); //$NON-NLS-1$
            }
        }

    }

    /**
     * yzhang Comment method "processQuotes".
     * 
     * @param text
     * @return
     */
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

    private void resetAllDbFieldsContent() {
        List<Text> texts = new ArrayList<Text>();
        texts.add(hostField.getTextControl(parent));
        texts.add(portField.getTextControl(parent));
        texts.add(dbNameField.getTextControl(parent));
        texts.add(additionParamField.getTextControl(parent));
        texts.add(schemaField.getTextControl(parent));
        texts.add(userField.getTextControl(parent));
        texts.add(passwordField.getTextControl(parent));
        texts.add(dabasePathField.getTextControl(parent));

        texts.add(statsTableField.getTextControl(parent));
        texts.add(logsTableField.getTextControl(parent));
        texts.add(metterTableField.getTextControl(parent));

        for (Text text : texts) {
            if (text.isEnabled()) {
                text.setText(TalendTextUtils.addQuotes("")); //$NON-NLS-1$
            } else {
                text.setText(""); //$NON-NLS-1$
            }
        }
    }

    private void setDbFieldsEnable(boolean f) {
        hostField.setEnabled(f, parent);
        hostField.getTextControl(parent).setEditable(f);
        portField.setEnabled(f, parent);
        portField.getTextControl(parent).setEditable(f);
        dbNameField.setEnabled(f, parent);
        dbNameField.getTextControl(parent).setEditable(f);
        if (additionParamField != null) {
            additionParamField.setEnabled(f, parent);
            additionParamField.getTextControl(parent).setEditable(f);
        }
        schemaField.setEnabled(f, parent);
        schemaField.getTextControl(parent).setEditable(f);
        userField.setEnabled(f, parent);
        userField.getTextControl(parent).setEditable(f);
        passwordField.setEnabled(f, parent);
        passwordField.getTextControl(parent).setEditable(f);
        dabasePathField.setEnabled(f, parent);
        dabasePathField.getTextControl(parent).setEditable(f);
    }

    /**
     * yzhang Comment method "formRepositoryTypeText".
     * 
     * @param dialog
     * @return
     */
    private String formRepositoryTypeText(ConnectionItem connectionItem) {
        if (connectionItem == null) {
            return ""; //$NON-NLS-1$
        }
        ERepositoryObjectType repositoryObjectType = ERepositoryObjectType.getItemType(connectionItem);
        String aliasName = repositoryObjectType.getAlias();
        org.talend.core.model.metadata.builder.connection.Connection connection = connectionItem.getConnection();
        if (connection instanceof DatabaseConnection) {
            String currentDbType = (String) RepositoryToComponentProperty.getValue(connection, "TYPE", null); //$NON-NLS-1$
            aliasName += " (" + currentDbType + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return aliasName + ":" + connectionItem.getProperty().getLabel(); //$NON-NLS-1$
    }

    private void createFields() {
        parent = getFieldEditorParent();
        int languageType = language == ECodeLanguage.JAVA ? 1 : 0;

        Composite titlePart = new Composite(parent, SWT.None);
        titlePart.setLayout(new GridLayout());

        onStatCatcherField = new CheckBoxFieldEditor(languagePrefix + EParameterName.ON_STATCATCHER_FLAG.getName(),
                EParameterName.ON_STATCATCHER_FLAG.getDisplayName(), titlePart);
        onLogCatcherField = new CheckBoxFieldEditor(languagePrefix + EParameterName.ON_LOGCATCHER_FLAG.getName(),
                EParameterName.ON_LOGCATCHER_FLAG.getDisplayName(), titlePart);
        onMetterCatcherField = new CheckBoxFieldEditor(languagePrefix + EParameterName.ON_METERCATCHER_FLAG.getName(),
                EParameterName.ON_METERCATCHER_FLAG.getDisplayName(), titlePart);

        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 4;
        titlePart.setLayoutData(gridData);

        GridLayout gridLayout = (GridLayout) titlePart.getLayout();
        gridLayout.numColumns = 12;
        titlePart.setLayout(gridLayout);

        onFilesField = new CheckBoxFieldEditor(languagePrefix + EParameterName.ON_FILES_FLAG.getName(),
                EParameterName.ON_FILES_FLAG.getDisplayName(), parent);
        filePathField = new DirectoryFieldEditorWithQuotes(languagePrefix + EParameterName.FILE_PATH.getName(),
                EParameterName.FILE_PATH.getDisplayName(), parent);
        TalendProposalUtils.installOn(filePathField.getTextControl(parent), null);

        statsFileNameField = new StringFieldEditor(languagePrefix + EParameterName.FILENAME_STATS.getName(),
                EParameterName.FILENAME_STATS.getDisplayName(), parent);
        TalendProposalUtils.installOn(statsFileNameField.getTextControl(parent), null);
        statsFileNameField.getTextControl(parent).setText(
                TalendTextUtils.addQuotes(PreferenceInitializer.DEFAULT_STATS_FILE_NAME));

        logsFileNameField = new StringFieldEditor(languagePrefix + EParameterName.FILENAME_LOGS.getName(),
                EParameterName.FILENAME_LOGS.getDisplayName(), parent);
        logsFileNameField.getTextControl(parent).setText(TalendTextUtils.addQuotes(PreferenceInitializer.DEFAULT_LOGS_FILE_NAME));
        TalendProposalUtils.installOn(logsFileNameField.getTextControl(parent), null);

        metterFileNameField = new StringFieldEditor(languagePrefix + EParameterName.FILENAME_METTER.getName(),
                EParameterName.FILENAME_METTER.getDisplayName(), parent);
        metterFileNameField.getTextControl(parent).setText(
                TalendTextUtils.addQuotes(PreferenceInitializer.DEFAULT_METER_FILE_NAME));
        TalendProposalUtils.installOn(metterFileNameField.getTextControl(parent), null);

        onDatabaseField = new CheckBoxFieldEditor(languagePrefix + EParameterName.ON_DATABASE_FLAG.getName(),
                EParameterName.ON_DATABASE_FLAG.getDisplayName(), parent);

        Composite comboTypePanel = new Composite(parent, SWT.NONE);
        GridData layout = new GridData(GridData.FILL_HORIZONTAL);
        layout.horizontalSpan = 3;

        comboTypePanel.setLayoutData(layout);
        RowLayout rowLayout = new RowLayout();
        rowLayout.spacing = 5;
        comboTypePanel.setLayout(rowLayout);
        Label labelRepositoryType = new Label(comboTypePanel, SWT.NONE);
        labelRepositoryType.setText(Messages.getString("StatsAndLogsPreferencePage.repositoryType")); //$NON-NLS-1$

        comboRepositoryType = new Combo(comboTypePanel, SWT.READ_ONLY);

        String[] repositoryTypes = new String[] { BUILT_IN, REPOSITORY };
        comboRepositoryType.setItems(repositoryTypes);

        String currentType = getPreferenceStore().getString(languagePrefix + EParameterName.PROPERTY_TYPE.getName());
        currentType = currentType.equals(EmfComponent.REPOSITORY) ? REPOSITORY : BUILT_IN;
        int currentTypeIndex = 0;
        for (; currentTypeIndex < repositoryTypes.length; currentTypeIndex++) {
            if (repositoryTypes[currentTypeIndex].equals(currentType)) {
                break;
            }

        }
        comboRepositoryType.select(currentTypeIndex);

        String dbTypeLabel = getPreferenceStore().getString(
                languagePrefix + EParameterName.REPOSITORY_PROPERTY_TYPE.getName()
                        + StatsAndLogsPreferencePage.CONNECTION_ITEM_LABEL);

        RepositoryContentProvider contentProvider = (RepositoryContentProvider) RepositoryView.show().getViewer()
                .getContentProvider();
        RepositoryNode repositoryNode = contentProvider.getMetadataConNode();
        connectionItem = StatsAndLogsViewHelper.findConnectionItem(contentProvider, repositoryNode, dbTypeLabel);

        textRepositoryType = new Text(comboTypePanel, SWT.SINGLE | SWT.BORDER);
        textRepositoryType.setVisible(currentType.equals(REPOSITORY));
        textRepositoryType.setEditable(false);
        textRepositoryType.setLayoutData(new RowData(260, SWT.DEFAULT));
        textRepositoryType.setText(formRepositoryTypeText(connectionItem));

        buttonShowRepository = new Button(comboTypePanel, SWT.NONE);
        buttonShowRepository.setText("..."); //$NON-NLS-1$
        buttonShowRepository.setVisible(currentType.equals(REPOSITORY));

        dbTypeComposite = new Composite(parent, SWT.NONE);

        String[] strDisplay, strValue;
        strDisplay = StatsAndLogsConstants.DISPLAY_DBNAMES[languageType];
        strValue = StatsAndLogsConstants.DB_COMPONENTS[languageType];
        String[][] strsForDBType = new String[strDisplay.length][2];

        for (int i = 0; i < strDisplay.length; i++) {
            strsForDBType[i][0] = strDisplay[i];
            strsForDBType[i][1] = strValue[i];
        }

        dbTypeField = new ComboFieldEditor(languagePrefix + EParameterName.DB_TYPE.getName(), EParameterName.DB_TYPE
                .getDisplayName(), strsForDBType, dbTypeComposite);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 4;
        dbTypeComposite.setLayoutData(gridData);

        gridLayout = (GridLayout) dbTypeComposite.getLayout();
        gridLayout.numColumns = 6;
        dbTypeComposite.setLayout(gridLayout);

        hostField = new StringFieldEditor(languagePrefix + EParameterName.HOST.getName(), EParameterName.HOST.getDisplayName(),
                parent);
        TalendProposalUtils.installOn(hostField.getTextControl(parent), null);
        ContextParameterExtractor.installOn(hostField.getTextControl(parent), null, languagePrefix
                + EParameterName.HOST.getName(), null);

        portField = new StringFieldEditor(languagePrefix + EParameterName.PORT.getName(), EParameterName.PORT.getDisplayName(),
                parent);
        TalendProposalUtils.installOn(portField.getTextControl(parent), null);

        dbNameField = new StringFieldEditor(languagePrefix + EParameterName.DBNAME.getName(), EParameterName.DBNAME
                .getDisplayName(), parent);
        TalendProposalUtils.installOn(dbNameField.getTextControl(parent), null);

        if (language == ECodeLanguage.JAVA) {
            additionParamField = new StringFieldEditor(languagePrefix + EParameterName.PROPERTIES.getName(),
                    EParameterName.PROPERTIES.getDisplayName(), parent);
            TalendProposalUtils.installOn(additionParamField.getTextControl(parent), null);
        }
        schemaField = new StringFieldEditor(languagePrefix + EParameterName.SCHEMA_DB.getName(), EParameterName.SCHEMA_DB
                .getDisplayName(), parent);
        TalendProposalUtils.installOn(schemaField.getTextControl(parent), null);

        userField = new StringFieldEditor(languagePrefix + EParameterName.USER.getName(), EParameterName.USER.getDisplayName(),
                parent);
        TalendProposalUtils.installOn(userField.getTextControl(parent), null);

        passwordField = new StringFieldEditor(languagePrefix + EParameterName.PASS.getName(), EParameterName.PASS
                .getDisplayName(), parent);
        TalendProposalUtils.installOn(passwordField.getTextControl(parent), null);

        dabasePathField = new FileFieldEditorWithQuotes(languagePrefix + EParameterName.DBFILE.getName(), EParameterName.DBFILE
                .getDisplayName(), parent);
        TalendProposalUtils.installOn(dabasePathField.getTextControl(parent), null);

        statsTableField = new StringFieldEditor(languagePrefix + EParameterName.TABLE_STATS.getName(), EParameterName.TABLE_STATS
                .getDisplayName(), parent);
        TalendProposalUtils.installOn(statsTableField.getTextControl(parent), null);

        logsTableField = new StringFieldEditor(languagePrefix + EParameterName.TABLE_LOGS.getName(), EParameterName.TABLE_LOGS
                .getDisplayName(), parent);
        TalendProposalUtils.installOn(logsTableField.getTextControl(parent), null);

        metterTableField = new StringFieldEditor(languagePrefix + EParameterName.TABLE_METER.getName(),
                EParameterName.TABLE_METER.getDisplayName(), parent);
        TalendProposalUtils.installOn(metterTableField.getTextControl(parent), null);

        finalPart = new Composite(parent, SWT.None);
        finalPart.setLayout(new GridLayout());

        catchRuntimeErrorsField = new CheckBoxFieldEditor(languagePrefix + EParameterName.CATCH_RUNTIME_ERRORS.getName(),
                EParameterName.CATCH_RUNTIME_ERRORS.getDisplayName(), finalPart);
        catchUserErrorsField = new CheckBoxFieldEditor(languagePrefix + EParameterName.CATCH_USER_ERRORS.getName(),
                EParameterName.CATCH_USER_ERRORS.getDisplayName(), finalPart);
        catchUserWarningField = new CheckBoxFieldEditor(languagePrefix + EParameterName.CATCH_USER_WARNING.getName(),
                EParameterName.CATCH_USER_WARNING.getDisplayName(), finalPart);
        catchRealtimeStatsField = new CheckBoxFieldEditor(languagePrefix + EParameterName.CATCH_REALTIME_STATS.getName(),
                EParameterName.CATCH_REALTIME_STATS.getDisplayName(), finalPart);

        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 4;
        finalPart.setLayoutData(gridData);

        gridLayout = (GridLayout) finalPart.getLayout();
        gridLayout.numColumns = 12;
        finalPart.setLayout(gridLayout);

        addField(onStatCatcherField);
        addField(onLogCatcherField);
        addField(onMetterCatcherField);

        addField(onFilesField);
        addField(filePathField);
        addField(statsFileNameField);
        addField(logsFileNameField);
        addField(metterFileNameField);
        addField(onDatabaseField);

        addField(dbTypeField);
        addField(hostField);

        addField(portField);
        addField(dbNameField);
        if (language == ECodeLanguage.JAVA) {
            addField(additionParamField);
        }

        addField(schemaField);
        addField(userField);
        addField(passwordField);
        addField(dabasePathField);
        addField(statsTableField);
        addField(logsTableField);
        addField(metterTableField);

        addField(catchRuntimeErrorsField);
        addField(catchUserErrorsField);
        addField(catchUserWarningField);
        addField(catchRealtimeStatsField);
    }

    private void updateEnableStateFromPreferences() {
        IPreferenceStore preferenceStore = getPreferenceStore();

        boolean onStatCatcher = preferenceStore.getBoolean(languagePrefix + EParameterName.ON_STATCATCHER_FLAG.getName());
        boolean onLogCatcher = preferenceStore.getBoolean(languagePrefix + EParameterName.ON_LOGCATCHER_FLAG.getName());
        boolean onMetterCatcher = preferenceStore.getBoolean(languagePrefix + EParameterName.ON_METERCATCHER_FLAG.getName());
        boolean onFiles = preferenceStore.getBoolean(languagePrefix + EParameterName.ON_FILES_FLAG.getName());
        boolean onDatabase = preferenceStore.getBoolean(languagePrefix + EParameterName.ON_DATABASE_FLAG.getName());
        String dbValue = preferenceStore.getString(languagePrefix + EParameterName.DB_TYPE.getName());
        boolean isBuiltIn = comboRepositoryType.getText().equals(BUILT_IN);
        updateEnableState(onStatCatcher, onLogCatcher, onMetterCatcher, onFiles, onDatabase, dbValue, isBuiltIn);
        if (!isBuiltIn && connectionItem != null) {
            updateDbFields(connectionItem);
        }
    }

    private void updateEnableStateFromDisplay() {
        boolean onStatCatcher = onStatCatcherField.getBooleanValue();
        boolean onLogCatcher = onLogCatcherField.getBooleanValue();
        boolean onMetterCatcher = onMetterCatcherField.getBooleanValue();
        boolean onFiles = onFilesField.getBooleanValue();
        boolean onDatabase = onDatabaseField.getBooleanValue();
        String dbValue = dbTypeField.getFieldValue();
        boolean isBuildin = comboRepositoryType.getText().equals(BUILT_IN);

        updateEnableState(onStatCatcher, onLogCatcher, onMetterCatcher, onFiles, onDatabase, dbValue, isBuildin);
        if (!isBuildin && connectionItem != null) {
            updateDbFields(connectionItem);
        }
    }

    private void updateEnableState(boolean onStatCatcher, boolean onLogCatcher, boolean onMetterCatcher, boolean onFiles,
            boolean onDatabase, String dbValue, boolean isBuildin) {
        onFilesField.setEnabled(onStatCatcher || onLogCatcher || onMetterCatcher, parent);
        filePathField.setEnabled(onFiles && (onStatCatcher || onLogCatcher || onMetterCatcher), parent);
        statsFileNameField.setEnabled(onFiles && onStatCatcher, parent);
        logsFileNameField.setEnabled(onFiles && onLogCatcher, parent);
        metterFileNameField.setEnabled(onFiles && onMetterCatcher, parent);

        comboRepositoryType.setEnabled(onLogCatcher && onDatabase);
        textRepositoryType.setEnabled(onLogCatcher && onDatabase);
        buttonShowRepository.setEnabled(onLogCatcher && onDatabase);

        onDatabaseField.setEnabled(onStatCatcher || onLogCatcher || onMetterCatcher, parent);
        dbTypeField.getComboBoxControl(dbTypeComposite).setEnabled(
                isBuildin && onDatabase && (onStatCatcher || onLogCatcher || onMetterCatcher));
        dbTypeField.setEnabled(isBuildin && onDatabase && (onStatCatcher || onLogCatcher || onMetterCatcher), dbTypeComposite);
        hostField.setEnabled((!dbValue.equals("tAccessOutput") && !dbValue.equals("tSQLiteOutput")) && onDatabase //$NON-NLS-1$ //$NON-NLS-2$
                && (onStatCatcher || onLogCatcher || onMetterCatcher), parent);
        portField.setEnabled((!dbValue.equals("tAccessOutput") && !dbValue.equals("tSQLiteOutput") && !dbValue //$NON-NLS-1$ //$NON-NLS-2$
                .equals("tFirebirdOutput")) //$NON-NLS-1$
                && onDatabase && (onStatCatcher || onLogCatcher || onMetterCatcher), parent);

        dbNameField.setEnabled((!dbValue.equals("tAccessOutput") && !dbValue.equals("tSQLiteOutput") && !dbValue //$NON-NLS-1$ //$NON-NLS-2$
                .equals("tFirebirdOutput")) //$NON-NLS-1$
                && onDatabase && (onStatCatcher || onLogCatcher || onMetterCatcher), parent);
        if (language == ECodeLanguage.JAVA) {
            additionParamField.setEnabled((dbValue.equals("tMSSqlOutput") || dbValue.equals("tInformixOutput") || dbValue //$NON-NLS-1$ //$NON-NLS-2$
                    .equals("tMysqlOutput")) //$NON-NLS-1$
                    && onDatabase && (onStatCatcher || onLogCatcher || onMetterCatcher), parent);
        }
        schemaField.setEnabled((dbValue.equals("tOracleOutput") || dbValue.equals("tPostgresqlOutput")) && onDatabase //$NON-NLS-1$ //$NON-NLS-2$
                && (onStatCatcher || onLogCatcher || onMetterCatcher), parent);
        userField.setEnabled((!dbValue.equals("tSQLiteOutput")) && onDatabase //$NON-NLS-1$
                && (onStatCatcher || onLogCatcher || onMetterCatcher), parent);
        passwordField.setEnabled((!dbValue.equals("tSQLiteOutput")) && onDatabase //$NON-NLS-1$
                && (onStatCatcher || onLogCatcher || onMetterCatcher), parent);
        dabasePathField.setEnabled((dbValue.equals("tAccessOutput") || dbValue.equals("tSQLiteOutput") || dbValue //$NON-NLS-1$ //$NON-NLS-2$
                .equals("tFirebirdOutput")) //$NON-NLS-1$
                && onDatabase && (onStatCatcher || onLogCatcher || onMetterCatcher), parent);
        statsTableField.setEnabled(onDatabase && onStatCatcher, parent);
        logsTableField.setEnabled(onDatabase && onLogCatcher, parent);
        metterTableField.setEnabled(onDatabase && onMetterCatcher, parent);

        catchRuntimeErrorsField.setEnabled(onLogCatcher, finalPart);
        catchUserErrorsField.setEnabled(onLogCatcher, finalPart);
        catchUserWarningField.setEnabled(onLogCatcher, finalPart);
        catchRealtimeStatsField.setEnabled(onStatCatcher, finalPart);
    }

    private void addListeners() {
        SelectionListener listener = new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                setDbFieldsEnable(true);
                updateEnableStateFromDisplay();
            }
        };
        onStatCatcherField.getCheckbox().addSelectionListener(listener);
        onLogCatcherField.getCheckbox().addSelectionListener(listener);
        onMetterCatcherField.getCheckbox().addSelectionListener(listener);
        onFilesField.getCheckbox().addSelectionListener(listener);
        onDatabaseField.getCheckbox().addSelectionListener(listener);
        dbTypeField.getComboBoxControl(dbTypeComposite).addSelectionListener(listener);

        buttonShowRepository.addSelectionListener(new SelectionListener() {

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetDefaultSelected(SelectionEvent e) {

            }

            public void widgetSelected(SelectionEvent e) {
                RepositoryReviewDialog dialog = new RepositoryReviewDialog(Display.getCurrent().getActiveShell(),
                        ERepositoryObjectType.METADATA, ERepositoryCategoryType.DATABASE.getName());

                if (dialog.open() == RepositoryReviewDialog.OK) {
                    setDbId(dialog.getResult().getObject().getId());
                    connectionItem = (ConnectionItem) dialog.getResult().getObject().getProperty().getItem();
                    String repositoryType = formRepositoryTypeText(connectionItem);
                    textRepositoryType.setText(repositoryType);

                    setDbFieldsEnable(true);
                    updateEnableStateFromDisplay();
                    // updateDbFields(connectionItem);
                }
            }

        });

        comboRepositoryType.addSelectionListener(new SelectionListener() {

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetDefaultSelected(SelectionEvent e) {

            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                updateRepositoryType();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        super.performDefaults();
        updateEnableStateFromDisplay();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {

    }

    private String id;

    private Text textRepositoryType;

    private Button buttonShowRepository;

    private void setDbId(String id) {
        this.id = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        boolean isRepositoryType = comboRepositoryType.getText().equals(REPOSITORY);

        String key = languagePrefix + EParameterName.PROPERTY_TYPE.getName();
        if (isRepositoryType) {
            getPreferenceStore().setValue(key, EmfComponent.REPOSITORY);
        } else {
            getPreferenceStore().setValue(key, EmfComponent.BUILTIN);
        }

        getPreferenceStore().setValue(languagePrefix + EParameterName.REPOSITORY_PROPERTY_TYPE.getName(),
                this.id == null ? "" : this.id); //$NON-NLS-1$
        String itemLabel = ""; //$NON-NLS-1$
        if (connectionItem != null) {
            itemLabel = connectionItem.getProperty().getLabel();
        }
        getPreferenceStore().setValue(languagePrefix + EParameterName.REPOSITORY_PROPERTY_TYPE.getName() + CONNECTION_ITEM_LABEL,
                itemLabel);
        return super.performOk();
    }

    /**
     * yzhang Comment method "updateRepositoryType".
     */
    private void updateRepositoryType() {
        if (comboRepositoryType.getText().equals(BUILT_IN)) {
            textRepositoryType.setVisible(false);
            buttonShowRepository.setVisible(false);
            dbTypeField.getControl().setEnabled(true);
            setDbFieldsEnable(true);
            updateEnableStateFromDisplay();
            resetAllDbFieldsContent();
        } else {
            textRepositoryType.setVisible(true);
            buttonShowRepository.setVisible(true);
            dbTypeField.setEnabled(false, dbTypeComposite);
            dbTypeField.getComboBoxControl(dbTypeComposite).setEnabled(false);
            if (connectionItem != null) {
                updateDbFields(connectionItem);
            }
        }
    }

    /**
     * yzhang StatsAndLogsPreferencePage class global comment. Detailled comment
     */
    static class DirectoryFieldEditorWithQuotes extends DirectoryFieldEditor {

        /**
         * yzhang StatsAndLogsPreferencePage.DirectoryFieldEditorWithQuotes constructor comment.
         */
        public DirectoryFieldEditorWithQuotes(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.preference.DirectoryFieldEditor#changePressed()
         */
        @Override
        protected String changePressed() {

            File f = new File(getTextControl().getText());
            if (!f.exists()) {
                f = null;
            }
            File d = getDirectory(f);
            if (d == null) {
                return null;
            }

            return TalendTextUtils.addQuotes(d.getAbsolutePath());
        }

        /**
         * yzhang Comment method "getDirectory".
         * 
         * @param startingDirectory
         * @return
         */
        private File getDirectory(File startingDirectory) {

            DirectoryDialog fileDialog = new DirectoryDialog(getShell(), SWT.OPEN);
            if (startingDirectory != null) {
                fileDialog.setFilterPath(startingDirectory.getPath());
            }
            String dir = fileDialog.open();
            if (dir != null) {
                dir = dir.trim();
                if (dir.length() > 0) {
                    return new File(dir);
                }
            }

            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.preference.DirectoryFieldEditor#doCheckState()
         */
        @Override
        protected boolean doCheckState() {
            return true;
        }

    }

    /**
     * yzhang StatsAndLogsPreferencePage class global comment. Detailled comment
     */
    static class FileFieldEditorWithQuotes extends FileFieldEditor {

        boolean enforceAbsolute = false;

        /**
         * yzhang StatsAndLogsPreferencePage.FileFieldEditorWithQuotes constructor comment.
         */
        public FileFieldEditorWithQuotes(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.preference.FileFieldEditor#changePressed()
         */
        @Override
        protected String changePressed() {
            File f = new File(getTextControl().getText());
            if (!f.exists()) {
                f = null;
            }
            File d = getFile(f);
            if (d == null) {
                return null;
            }

            return TalendTextUtils.addQuotes(d.getAbsolutePath());
        }

        /**
         * Helper to open the file chooser dialog.
         * 
         * @param startingDirectory the directory to open the dialog on.
         * @return File The File the user selected or <code>null</code> if they do not.
         */
        private File getFile(File startingDirectory) {

            FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
            if (startingDirectory != null) {
                dialog.setFileName(startingDirectory.getPath());
            }

            String file = dialog.open();
            if (file != null) {
                file = file.trim();
                if (file.length() > 0) {
                    return new File(file);
                }
            }

            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.preference.FileFieldEditor#checkState()
         */
        @Override
        protected boolean checkState() {

            return true;

        }
    }
}
