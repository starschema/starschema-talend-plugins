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
package org.talend.repository.ui.wizards.metadata.connection.database;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledCombo;
import org.talend.commons.ui.swt.formtools.LabelledDirectoryField;
import org.talend.commons.ui.swt.formtools.LabelledFileField;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.commons.ui.swt.formtools.UtilsButton;
import org.talend.commons.ui.utils.PathUtils;
import org.talend.core.CorePlugin;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.DatabaseConnStrUtil;
import org.talend.core.database.conn.EDatabaseConnVar;
import org.talend.core.database.conn.template.EDatabaseConnTemplate;
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.PasswordEncryptUtil;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.swt.utils.AbstractForm;
import org.talend.repository.ui.utils.ConnectionContextHelper;
import org.talend.repository.ui.utils.DBConnectionContextUtils;
import org.talend.repository.ui.utils.ManagerConnection;
import org.talend.repository.ui.utils.DBConnectionContextUtils.EDBParamName;

/**
 * @author ocarbone
 * 
 */
public class DatabaseForm extends AbstractForm {

    /**
     * The number of items that can be visible in database type combo.
     */
    private static final int VISIBLE_DATABASE_COUNT = 20;

    /**
     * Composite.
     */
    private Composite compositeDbSettings;

    /**
     * Main Vars.
     */
    private final ConnectionItem connectionItem;

    /**
     * Flags.
     */
    private boolean databaseSettingIsValide = false;

    private boolean readOnly;

    /**
     * Main Fields.
     */
    private LabelledCombo dbTypeCombo;

    private LabelledCombo dbVersionCombo;

    private LabelledCombo sqlSyntaxCombo;

    private LabelledText serverText;

    private LabelledText portText;

    private LabelledText usernameText;

    private LabelledText passwordText;

    private LabelledText sidOrDatabaseText;

    private LabelledText schemaText;

    private LabelledText datasourceText;

    private LabelledText stringQuoteText;

    private LabelledText nullCharText;

    private Label sqlModeLabel;

    private Button button1;

    private Button button2;

    private LabelledText urlConnectionStringText;

    private LabelledFileField fileField;

    private LabelledDirectoryField directoryField;

    private LabelledText additionParamText;

    private Button standardButton;

    private Button systemButton;

    /**
     * Fields for general jdbc
     */

    private LabelledText generalJdbcUrlText = null;

    private LabelledText generalJdbcUserText = null;

    private LabelledText generalJdbcPasswordText = null;

    private LabelledText generalJdbcClassNameText = null;

    private LabelledText generalJdbcDriverjarText = null;

    private LabelledText jDBCschemaText;

    private Button browseJarFilesButton = null;

    /**
     * Anothers Fields.
     */
    private UtilsButton checkButton;

    private Group databaseSettingGroup;

    private Composite typeDbCompositeParent;

    private Composite generalDbCompositeParent;

    private Composite compositeGroupDbSettings;

    private LabelledText generalMappingFileText;

    private Button mappingSelectButton;

    private boolean isCreation;

    private boolean first = true;

    private boolean isModify = false;

    private Composite newParent;

    private ScrolledComposite scrolledComposite;

    /**
     * Constructor to use by a Wizard to create a new database connection.
     * 
     * @param existingNames
     * 
     * @param Composite
     * @param Wizard
     * @param ISelection
     */
    public DatabaseForm(Composite parent, ConnectionItem connectionItem, String[] existingNames, boolean isCreation) {
        super(parent, SWT.NONE, existingNames);
        this.connectionItem = connectionItem;
        this.isCreation = isCreation;
        setConnectionItem(connectionItem); // must be first.
        setupForm(true);
        addStringConnectionControls();
        GridLayout layout2 = (GridLayout) getLayout();
        layout2.marginHeight = 0;
        setLayout(layout2);
    }

    /**
     * initialize UI (button save & default settings or saved settings).
     */
    @Override
    public void initialize() {
        EDatabaseConnTemplate template = EDatabaseConnTemplate.indexOfTemplate(getConnection().getDatabaseType());
        if (template != null) {
            dbTypeCombo.setText(template.getDbType().getDisplayName());

            if (isGeneralJDBC()) {
                switchBetweenTypeandGeneralDB(false);
                initializeGeneralJDBC();
            }
        }
        if (isContextMode()) {
            adaptFormToEditable();
            urlConnectionStringText.setText(getStringConnection());
        } else {
            urlConnectionStringText.setText(getConnection().getURL());

        }
        usernameText.setText(getConnection().getUsername());
        passwordText.setText(getConnection().getPassword());
        serverText.setText(getConnection().getServerName());
        portText.setText(getConnection().getPort());
        datasourceText.setText(getConnection().getDatasourceName());
        additionParamText.setText(getConnection().getAdditionalParams());
        sidOrDatabaseText.setText(getConnection().getSID());
        schemaText.setText(getConnection().getUiSchema());

        if (getConnection().getDbVersionString() != null) {
            dbVersionCombo.setText(getConnection().getDbVersionString());
        }

        fileField.setText(getConnection().getFileFieldName());
        stringQuoteText.setText(getConnection().getStringQuote());
        nullCharText.setText(getConnection().getNullChar());
        directoryField.setText(getConnection().getDBRootPath());
        setSqlModelFields();
        checkAS400SpecificCase();
        // PTODO !StandBy! (use width SQL Editor): to define the values of SQL
        // Syntax (need by SQL Editor)
        getConnection().setSqlSynthax(Messages.getString("DatabaseForm.sqlSyntax")); //$NON-NLS-1$
        sqlSyntaxCombo.select(getSqlSyntaxIndex(getConnection().getSqlSynthax()));
        updateStatus(IStatus.OK, ""); //$NON-NLS-1$
    }

    private void setSqlModelFields() {
        button1.setSelection(getConnection().isSQLMode());
        button2.setSelection(!getConnection().isSQLMode());
    }

    /**
     * DOC YeXiaowei Comment method "initializeGeneralJDBC".
     */
    private void initializeGeneralJDBC() {
        generalJdbcUrlText.setText(getConnection().getURL());
        generalJdbcClassNameText.setText(getConnection().getDriverClass());
        generalJdbcUserText.setText(getConnection().getUsername());
        generalJdbcPasswordText.setText(getConnection().getPassword());

        generalJdbcDriverjarText.setText(getConnection().getDriverJarPath());
        generalMappingFileText.setText(getConnection().getDbmsId());

        String jdbcUrlString = "";
        if (isContextMode()) {
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(getShell(), getConnection(), true);
            if (contextType != null) {
                jdbcUrlString = ConnectionContextHelper.getOriginalValue(contextType, getConnection().getURL());
            }
        } else {
            jdbcUrlString = generalJdbcUrlText.getText();
        }
        if (jdbcUrlString.contains("sqlserver")) {//$NON-NLS-1$ 
            jDBCschemaText.setText(getConnection().getUiSchema());
        } else {
            jDBCschemaText.setHideWidgets(true);
        }
    }

    private void checkAS400SpecificCase() {
        if (getConnection().isStandardSQL() == getConnection().isSystemSQL()) { // create
            // connection
            boolean b = CorePlugin.getDefault().getPreferenceStore().getBoolean(ITalendCorePrefConstants.AS400_SQL_SEG);
            standardButton.setSelection(b);
            systemButton.setSelection(!b);
            getConnection().setStandardSQL(b);
            getConnection().setSystemSQL(!b);
        } else {
            standardButton.setSelection(getConnection().isStandardSQL());
            systemButton.setSelection(getConnection().isSystemSQL());
        }
    }

    /**
     * DOC ocarbone Comment method "adaptFormToReadOnly".
     */
    @Override
    protected void adaptFormToReadOnly() {
        readOnly = isReadOnly();

        dbTypeCombo.setReadOnly(isReadOnly());
        urlConnectionStringText.setReadOnly(isReadOnly());
        usernameText.setReadOnly(isReadOnly());
        passwordText.setReadOnly(isReadOnly());
        serverText.setReadOnly(isReadOnly());
        portText.setReadOnly(isReadOnly());
        sidOrDatabaseText.setReadOnly(isReadOnly());
        schemaText.setReadOnly(isReadOnly());
        dbVersionCombo.setReadOnly(isReadOnly());
        datasourceText.setReadOnly(isReadOnly());
        additionParamText.setReadOnly(isReadOnly());
        fileField.setReadOnly(isReadOnly());
        sqlSyntaxCombo.setReadOnly(isReadOnly());
        stringQuoteText.setReadOnly(isReadOnly());
        nullCharText.setReadOnly(isReadOnly());

    }

    /**
     * Get the index of a sqlSyntax label or 0 if the label don't exist.
     * 
     * @param string label
     */
    public int getSqlSyntaxIndex(final String label) {
        for (int i = 0; i < sqlSyntaxCombo.getItemCount(); i++) {
            if (sqlSyntaxCombo.getItem(i).equals(label)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    protected void addFields() {
        int width = getSize().x;
        GridLayout layout2;
        databaseSettingGroup = Form.createGroup(this, 1, Messages.getString("DatabaseForm.groupDatabaseSettings"), 270); //$NON-NLS-1$

        scrolledComposite = new ScrolledComposite(databaseSettingGroup, SWT.V_SCROLL | SWT.H_SCROLL);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        newParent = new Composite(scrolledComposite, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        newParent.setLayout(gridLayout);
        scrolledComposite.setContent(newParent);

        compositeGroupDbSettings = Form.startNewGridLayout(newParent, 1);
        // compositeGroupDbSettings = new Composite(newParent, SWT.NONE);
        // compositeGroupDbSettings.setLayout(new GridLayout(1, false));
        // compositeGroupDbSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        layout2 = (GridLayout) compositeGroupDbSettings.getLayout();
        layout2.marginHeight = 0;
        layout2.marginTop = 0;
        layout2.marginBottom = 0;
        layout2.marginLeft = 0;
        layout2.marginRight = 0;
        layout2.marginWidth = 0;

        compositeDbSettings = new Composite(compositeGroupDbSettings, SWT.NULL);
        compositeDbSettings.setLayout(new GridLayout(3, false));
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.minimumWidth = width;
        // gridData.minimumHeight = 50;
        gridData.widthHint = width;
        // gridData.heightHint = 50;
        compositeDbSettings.setLayoutData(gridData);

        layout2 = (GridLayout) compositeDbSettings.getLayout();
        layout2.marginHeight = 0;
        layout2.marginTop = 0;
        layout2.marginBottom = 0;

        // Main Fields

        // Database Type Combo

        addDBSelectCombo();

        Label label = new Label(compositeDbSettings, SWT.SEPARATOR | SWT.H_SCROLL);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        label.setLayoutData(data);

        addFieldsForTypeDB(compositeGroupDbSettings);
        addFieldsForGeneralDB(compositeGroupDbSettings);

        switchBetweenTypeandGeneralDB(true);

        addCheckAndStandardButtons(width, compositeGroupDbSettings);

        checkDBTypeAS400();

        // scrolledComposite.setSize(compositeGroupDbSettings.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        //

        scrolledComposite.addControlListener(new ControlAdapter() {

            public void controlResized(ControlEvent e) {
                Rectangle r = scrolledComposite.getClientArea();
                scrolledComposite.setMinSize(newParent.computeSize(r.width, 300));
            }
        });
    }

    /**
     * DOC YeXiaowei Comment method "addFieldForTypeDB".
     * 
     * @param width
     * @param compositeGroupDbSettings
     */
    private void addFieldsForTypeDB(Composite compositeGroupDbSettings) {

        typeDbCompositeParent = new Composite(compositeGroupDbSettings, SWT.NULL);
        typeDbCompositeParent.setLayout(new GridLayout(3, false));
        GridLayout layout2 = (GridLayout) typeDbCompositeParent.getLayout();
        layout2.marginHeight = 0;
        layout2.marginTop = 0;
        layout2.marginBottom = 0;

        dbVersionCombo = new LabelledCombo(typeDbCompositeParent, Messages.getString("DatabaseForm.dbversion"), Messages //$NON-NLS-1$
                .getString("DatabaseForm.dbversion.tip"), new String[0], 2, true); //$NON-NLS-1$

        // Field connectionString
        urlConnectionStringText = new LabelledText(typeDbCompositeParent, Messages.getString("DatabaseForm.stringConnection"), 2); //$NON-NLS-1$
        urlConnectionStringText.setEditable(false);

        // Field login & password
        usernameText = new LabelledText(typeDbCompositeParent, Messages.getString("DatabaseForm.login"), 2); //$NON-NLS-1$
        passwordText = new LabelledText(typeDbCompositeParent, Messages.getString("DatabaseForm.password"), 2, SWT.BORDER //$NON-NLS-1$
                | SWT.SINGLE | SWT.PASSWORD);

        // Another fields
        serverText = new LabelledText(typeDbCompositeParent, Messages.getString("DatabaseForm.server"), 2); //$NON-NLS-1$
        portText = new LabelledText(typeDbCompositeParent, Messages.getString("DatabaseForm.port"), 2); //$NON-NLS-1$
        // portText.setTextLimit(5);
        sidOrDatabaseText = new LabelledText(typeDbCompositeParent, Messages.getString("DatabaseForm.DataBase"), 2); //$NON-NLS-1$
        schemaText = new LabelledText(typeDbCompositeParent, Messages.getString("DatabaseForm.schema"), 2); //$NON-NLS-1$
        if (getConnection() != null
                && EDatabaseConnTemplate.INFORMIX.getDBDisplayName().equals(getConnection().getDatabaseType())) {
            datasourceText = new LabelledText(typeDbCompositeParent, Messages.getString("DatabaseForm.informixInstance"), 2); //$NON-NLS-1$ 
        } else {
            datasourceText = new LabelledText(typeDbCompositeParent, Messages.getString("DatabaseForm.dataSource"), 2); //$NON-NLS-1$
        }
        additionParamText = new LabelledText(typeDbCompositeParent, Messages.getString("DatabaseForm.AddParams"), 2); //$NON-NLS-1$

        String[] extensions = { "*.*" }; //$NON-NLS-1$
        fileField = new LabelledFileField(typeDbCompositeParent, Messages.getString("DatabaseForm.mdbFile"), extensions); //$NON-NLS-1$
        directoryField = new LabelledDirectoryField(typeDbCompositeParent, "DB Root Path"); //$NON-NLS-1$
    }

    /**
     * 
     * DOC YeXiaowei Comment method "getVersionDrivers".
     * 
     * DOC qli modify method "getVersionDrivers",just add a parameter "dbType".
     * 
     * @return
     */
    private List<String> getVersionDrivers(String dbType) {
        List<String> result = new ArrayList<String>();
        List<EDatabaseVersion4Drivers> v4dList = EDatabaseVersion4Drivers.indexOfByDbType(dbType);
        for (EDatabaseVersion4Drivers v4d : v4dList) {
            result.add(v4d.getVersionDisplay());
        }
        return result;
    }

    private void addFieldsForGeneralDB(Composite parent) {

        generalDbCompositeParent = new Composite(parent, SWT.NULL);

        generalDbCompositeParent.setLayout(new GridLayout(3, false));

        GridLayout layout2 = (GridLayout) generalDbCompositeParent.getLayout();
        layout2.marginHeight = 0;
        layout2.marginTop = 0;
        layout2.marginBottom = 0;

        generalJdbcUrlText = new LabelledText(generalDbCompositeParent, Messages.getString("DatabaseForm.general.url"), 2); //$NON-NLS-1$

        generalJdbcDriverjarText = new LabelledText(generalDbCompositeParent, Messages.getString("DatabaseForm.general.jarfile"), //$NON-NLS-1$
                1);

        browseJarFilesButton = new Button(generalDbCompositeParent, SWT.NONE);
        browseJarFilesButton.setText("..."); //$NON-NLS-1$
        browseJarFilesButton.setToolTipText(Messages.getString("DatabaseForm.selectJar")); //$NON-NLS-1$

        generalJdbcClassNameText = new LabelledText(generalDbCompositeParent, Messages
                .getString("DatabaseForm.general.classname"), 2); //$NON-NLS-1$

        generalJdbcUserText = new LabelledText(generalDbCompositeParent, Messages.getString("DatabaseForm.general.user"), 2); //$NON-NLS-1$

        generalJdbcPasswordText = new LabelledText(generalDbCompositeParent,
                Messages.getString("DatabaseForm.general.password"), 2); //$NON-NLS-1$
        generalJdbcPasswordText.getTextControl().setEchoChar('*'); // see
        // feature
        // 3629 hide
        // password
        jDBCschemaText = new LabelledText(generalDbCompositeParent, Messages.getString("DatabaseForm.schema"), 2); //$NON-NLS-1$
        generalMappingFileText = new LabelledText(generalDbCompositeParent, Messages.getString("DatabaseForm.general.mapping"), 1); //$NON-NLS-1$

        mappingSelectButton = new Button(generalDbCompositeParent, SWT.NONE);
        mappingSelectButton.setText("..."); //$NON-NLS-1$
        mappingSelectButton.setToolTipText(Messages.getString("DatabaseForm.selectRule")); //$NON-NLS-1$

    }

    /**
     * Hidden one composite and display another one
     * <p>
     * DOC YeXiaowei Comment method "switchBetweenTypeandGeneralDB".
     * 
     * @param hiddenGeneral
     */
    private void switchBetweenTypeandGeneralDB(boolean hiddenGeneral) {

        GridData dataGeneralDb = null;
        GridData dataTypeDb = null;

        if (hiddenGeneral) {
            dataGeneralDb = new GridData(GridData.FILL_HORIZONTAL);
            dataGeneralDb.heightHint = 0;

            dataTypeDb = new GridData(GridData.FILL_BOTH);
        } else {
            dataGeneralDb = new GridData(GridData.FILL_BOTH);

            dataTypeDb = new GridData(GridData.FILL_HORIZONTAL);
            dataTypeDb.heightHint = 0;
        }

        generalDbCompositeParent.setLayoutData(dataGeneralDb);
        typeDbCompositeParent.setLayoutData(dataTypeDb);

        compositeGroupDbSettings.layout();
    }

    /**
     * DOC YeXiaowei Comment method "addCheckAndStandardButtons".
     * 
     * @param width
     * @param compositeGroupDbSettings
     */
    private void addCheckAndStandardButtons(int width, Composite compositeGroupDbSettings) {

        GridLayout layout2 = null;

        fileField.hide();
        directoryField.hide();

        // Button Check
        // Group checkGroup = Form.createGroup(this, 1, "", 5);
        Composite checkGroup = new Composite(this, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        checkGroup.setLayout(gridLayout);
        GridData gridData23 = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData23.minimumHeight = 2;
        gridData23.heightHint = 2;
        checkGroup.setLayoutData(gridData23);
        Composite compositeCheckButton = Form.startNewGridLayout(checkGroup, 1, false, SWT.CENTER, SWT.BOTTOM);
        layout2 = (GridLayout) compositeCheckButton.getLayout();
        layout2.marginHeight = 0;
        layout2.marginTop = 0;
        layout2.marginBottom = 0;
        layout2.marginLeft = 0;
        layout2.marginRight = 0;
        layout2.marginWidth = 0;

        GridData checkGridData = new GridData(GridData.FILL_HORIZONTAL);
        checkGridData.minimumHeight = 5;
        checkGroup.setLayoutData(checkGridData);
        checkButton = new UtilsButton(compositeCheckButton, Messages.getString("DatabaseForm.check"), WIDTH_BUTTON_PIXEL, //$NON-NLS-1$
                HEIGHT_BUTTON_PIXEL);
        checkButton.setEnabled(false);

        // Group Database Properties
        Group group1 = Form.createGroup(this, 1, Messages.getString("DatabaseForm.groupDatabaseProperties")); //$NON-NLS-1$
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.minimumHeight = 80;
        // gridData.heightHint = 80;
        group1.setLayoutData(gridData);
        // Composite compositeGroupDbProperties =
        // Form.startNewGridLayout(group1, 4, false, SWT.LEFT, SWT.CENTER);
        Composite compositeGroupDbProperties = Form.startNewDimensionnedGridLayout(group1, 4, width, 100);

        // PTODO !StandBy! (use width SQL Editor): to define the values of SQL
        // Syntax (need by SQL Editor)
        String[] item = { "SQL 92" }; //$NON-NLS-1$
        sqlSyntaxCombo = new LabelledCombo(compositeGroupDbProperties, Messages.getString("DatabaseForm.sqlSyntax"), null, item, //$NON-NLS-1$
                3);

        stringQuoteText = new LabelledText(compositeGroupDbProperties, Messages.getString("DatabaseForm.stringQuote"), false); //$NON-NLS-1$
        nullCharText = new LabelledText(compositeGroupDbProperties, Messages.getString("DatabaseForm.nullChar"), false); //$NON-NLS-1$

        gridData = new GridData();
        gridData.horizontalSpan = 2;
        standardButton = new Button(compositeGroupDbProperties, SWT.RADIO);
        standardButton.setText(Messages.getString("DatabaseForm.StandardSQL")); //$NON-NLS-1$
        standardButton.setLayoutData(gridData);
        systemButton = new Button(compositeGroupDbProperties, SWT.RADIO);
        systemButton.setText(Messages.getString("DatabaseForm.SystemSQL")); //$NON-NLS-1$
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        systemButton.setLayoutData(gridData);

        Composite c = new Composite(compositeGroupDbProperties, SWT.NONE);
        GridLayout layout = new GridLayout(4, false);
        layout.horizontalSpacing = 15;
        layout.verticalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.horizontalSpan = 4;
        c.setLayoutData(layoutData);
        c.setLayout(layout);
        sqlModeLabel = new Label(c, SWT.NONE);
        sqlModeLabel.setText(Messages.getString("DatabaseForm.sqlMode")); //$NON-NLS-1$
        button1 = new Button(c, SWT.RADIO);
        button1.setText(Messages.getString("DatabaseForm.yes")); //$NON-NLS-1$
        button2 = new Button(c, SWT.RADIO);
        button2.setText(Messages.getString("DatabaseForm.no")); //$NON-NLS-1$
        sqlModeLabel.setVisible(false);
        button1.setVisible(false);
        button2.setVisible(false);

    }

    /**
     * DOC YeXiaowei Comment method "addDBSelectCombo". Extract method form addFields()
     */
    private void addDBSelectCombo() {
        // PTODO cantoine : HIDDEN some Database connection in function of
        // project MODE (Perl/Java).

        dbTypeCombo = new LabelledCombo(compositeDbSettings, Messages.getString("DatabaseForm.dbType"), Messages //$NON-NLS-1$
                .getString("DatabaseForm.dbTypeTip"), EDatabaseConnTemplate.getDBTypeDisplay().toArray(new String[0]), 2, true); //$NON-NLS-1$

        // configure the visible item of database combo
        int visibleItemCount = dbTypeCombo.getCombo().getItemCount();
        if (visibleItemCount > VISIBLE_DATABASE_COUNT) {
            visibleItemCount = VISIBLE_DATABASE_COUNT;
        }
        dbTypeCombo.getCombo().setVisibleItemCount(visibleItemCount);
    }

    /**
     * Check DBType is AS400,set systemButton and stardardButton visible.a
     */
    private void checkDBTypeAS400() {
        if (dbTypeCombo.getSelectionIndex() == 16 || dbTypeCombo.getSelectionIndex() == 25
                || dbTypeCombo.getSelectionIndex() == 26) {
            standardButton.setVisible(true);
            systemButton.setVisible(true);
        } else {
            standardButton.setVisible(false);
            systemButton.setVisible(false);
        }

    }

    /**
     * Check data connection.
     */
    private void checkConnection() {

        checkButton.setEnabled(false);
        if (connectionItem.getConnection() instanceof DatabaseConnection) {
            DatabaseConnection c = (DatabaseConnection) connectionItem.getConnection();
            if (c.getProductId().equals(EDatabaseTypeName.ORACLEFORSID.getProduct())) {
                if (!isContextMode()) {
                    schemaText.setText(schemaText.getText().toUpperCase());
                }
            }
        }
        ManagerConnection managerConnection = new ManagerConnection();

        if (isContextMode()) { // context mode
            String urlStr = DBConnectionContextUtils.setManagerConnectionValues(managerConnection, connectionItem, dbTypeCombo
                    .getItem(dbTypeCombo.getSelectionIndex()), dbTypeCombo.getSelectionIndex());
            if (urlStr == null) {
                if (dbTypeCombo.getText().equals(EDatabaseConnTemplate.GENERAL_JDBC.getDBDisplayName())) {
                    DatabaseConnection dbConn = (DatabaseConnection) connectionItem.getConnection();

                    ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(dbConn);
                    urlStr = ConnectionContextHelper.getOriginalValue(contextType, dbConn.getURL());
                } else {
                    urlStr = getStringConnection();
                }
            }
            urlConnectionStringText.setText(urlStr);
        } else {
            String versionStr = dbVersionCombo.getText();
            EDatabaseVersion4Drivers version = EDatabaseVersion4Drivers.indexOfByVersionDisplay(versionStr);
            if (version != null) {
                versionStr = version.getVersionValue();
            }
            // set the value
            managerConnection.setValue(0, dbTypeCombo.getItem(dbTypeCombo.getSelectionIndex()),
                    isGeneralJDBC() ? generalJdbcUrlText.getText() : urlConnectionStringText.getText(), serverText.getText(),
                    isGeneralJDBC() ? generalJdbcUserText.getText() : usernameText.getText(),
                    isGeneralJDBC() ? generalJdbcPasswordText.getText() : passwordText.getText(), sidOrDatabaseText.getText(),
                    portText.getText(), fileField.getText(), datasourceText.getText(), isGeneralJDBC() ? "" : schemaText //$NON-NLS-1$
                            .getText(), additionParamText.getText(), generalJdbcClassNameText.getText(), generalJdbcDriverjarText
                            .getText(), enableDbVersion() ? versionStr : null);

            managerConnection.setDbRootPath(directoryField.getText());

        }
        managerConnection.setValueProperties(sqlSyntaxCombo.getItem(sqlSyntaxCombo.getSelectionIndex()), stringQuoteText
                .getText(), nullCharText.getText());
        // check the connection
        databaseSettingIsValide = managerConnection.check();

        // update the button
        checkButton.setEnabled(true);

        // show the result of check connection
        if (databaseSettingIsValide) {
            if (!isReadOnly()) {
                updateStatus(IStatus.OK, null);
            }
            MessageDialog.openInformation(getShell(), Messages.getString("DatabaseForm.checkConnectionTitle"), "\"" //$NON-NLS-1$ //$NON-NLS-2$
                    + connectionItem.getProperty().getLabel() + "\" " + Messages.getString("DatabaseForm.checkIsDone")); //$NON-NLS-1$ //$NON-NLS-2$
            if (!isReadOnly()) {
                if (isContextMode()) {
                    adaptFormToEditable();
                } else {
                    setPropertiesFormEditable(true);
                }
            }

        } else {
            String mainMsg = Messages.getString("DatabaseForm.checkFailure") + " " //$NON-NLS-1$ //$NON-NLS-2$
                    + Messages.getString("DatabaseForm.checkFailureTip"); //$NON-NLS-1$
            if (!isReadOnly()) {
                updateStatus(IStatus.WARNING, mainMsg);
            }
            new ErrorDialogWidthDetailArea(getShell(), PID, mainMsg, managerConnection.getMessageException());
        }
    }

    private boolean enableDbVersion() {
        return oracleVersionEnable() || as400VersionEnable()
                || EDatabaseConnTemplate.ACCESS.getDBDisplayName().equals(dbTypeCombo.getText());
    }

    /**
     * add StringConnection Controls.
     */
    private void addStringConnectionControls() {
        // event FocusIn
        urlConnectionStringText.addListener(SWT.FocusIn, new Listener() {

            public void handleEvent(final Event e) {
                if (!isContextMode()) {
                    if (dbTypeCombo.getSelectionIndex() >= 0) {
                        setPropertiesFormEditable(false);
                        urlConnectionStringText.setEditable(true);
                    } else {
                        updateStatus(IStatus.ERROR, Messages.getString("DatabaseForm.alert", dbTypeCombo.getLabel())); //$NON-NLS-1$
                    }
                }
            }
        });

        urlConnectionStringText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    if (urlConnectionStringText.getEditable()) {
                        if (!databaseSettingIsValide) {
                            updateStatus(IStatus.INFO, Messages.getString("DatabaseForm.checkInformation")); //$NON-NLS-1$
                        }
                        databaseSettingIsValide = false;
                        checkButton.setEnabled(true);
                        String[] s = DatabaseConnStrUtil.analyseURL(getConnection().getDatabaseType(), getConnection()
                                .getDbVersionString(), urlConnectionStringText.getText());
                        // if the ConnectionString write manually don't
                        // correspond width selectedIndex of combo DbType
                        // we search if another regex corresponding at this
                        // string
                        String selection = s[0];
                        if (!dbTypeCombo.getText().equals(selection)) {
                            dbTypeCombo.setText(selection);
                            dbTypeCombo.forceFocus();
                        }

                        int index = 1;
                        if (s[index] != "") {//$NON-NLS-1$
                            if (selection.equals(EDatabaseConnTemplate.GODBC.getDBDisplayName())
                                    || selection.equals(EDatabaseConnTemplate.MSODBC.getDBDisplayName())) {
                                datasourceText.setText(s[index]);
                                getConnection().setDatasourceName(s[index]);
                            } else if (selection.equals(EDatabaseConnTemplate.SQLITE.getDBDisplayName())
                                    || selection.equals(EDatabaseConnTemplate.ACCESS.getDBDisplayName())) {
                                fileField.setText(s[index]);
                                getConnection().setFileFieldName(s[index]);
                            } else if (selection.equals(EDatabaseConnTemplate.JAVADB_EMBEDED.getDBDisplayName())) {
                                sidOrDatabaseText.setText(s[index]);
                                getConnection().setSID(s[index]);
                                getConnection().setURL(getStringConnection());
                            } else if (selection.equals(EDatabaseConnTemplate.HSQLDB_IN_PROGRESS.getDBDisplayName())) {
                                directoryField.setText(s[index]);
                                getConnection().setDBRootPath(s[index]);
                            } else {
                                serverText.setText(s[index]);
                                getConnection().setServerName(s[index]);
                            }
                        }

                        index = 2;
                        if (s[index] != "") { //$NON-NLS-1$
                            if (selection.equals(EDatabaseConnTemplate.INTERBASE.getDBDisplayName())
                                    || selection.equals(EDatabaseConnTemplate.TERADATA.getDBDisplayName())
                                    || selection.equals(EDatabaseConnTemplate.AS400.getDBDisplayName())
                                    || selection.equals(EDatabaseConnTemplate.HSQLDB_IN_PROGRESS.getDBDisplayName())) {
                                sidOrDatabaseText.setText(s[index]);
                                getConnection().setSID(s[index]);
                            } else if (selection.equals(EDatabaseConnTemplate.FIREBIRD.getDBDisplayName())) {
                                fileField.setText(s[index]);
                                getConnection().setFileFieldName(s[index]);
                            } else {
                                portText.setText(s[index]);
                                getConnection().setPort(s[index]);
                            }
                        }

                        index = 3;
                        if (s[index] != "") { //$NON-NLS-1$
                            if (selection.equals(EDatabaseConnTemplate.AS400.getDBDisplayName())) {
                                sidOrDatabaseText.setText(s[index]);
                                getConnection().setSID(s[index]);
                            }
                        }

                        index = 4;
                        if (s[index] != "") { //$NON-NLS-1$
                            if (selection.equals(EDatabaseConnTemplate.INFORMIX.getDBDisplayName())) {
                                datasourceText.setText(s[index]);
                                getConnection().setDatasourceName(s[index]);
                            } else {
                                additionParamText.setText(s[index]);
                                getConnection().setAdditionalParams(s[index]);
                            }
                        }

                        index = 5;
                        if (s[index] != "") { //$NON-NLS-1$
                            additionParamText.setText(s[index]);
                            getConnection().setAdditionalParams(s[index]);
                        }
                    }
                    checkDBTypeAS400();
                }
            }
        });

    }

    /**
     * addButtonControls.
     * 
     * @param cancelButton
     */
    @Override
    protected void addUtilsButtonListeners() {

        // Event CheckButton
        checkButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                checkConnection();
            }
        });
    }

    /**
     * Main Fields addControls.
     */
    @Override
    protected void addFieldsListeners() {
        // common Listener to force the choice of dbCombo
        Listener listener = new Listener() {

            public void handleEvent(final Event e) {
                if (isContextMode()) {
                    //
                } else {
                    if (dbTypeCombo.getSelectionIndex() == -1) {
                        dbTypeCombo.forceFocus();
                    }
                    setPropertiesFormEditable(dbTypeCombo.getSelectionIndex() > -1);
                    urlConnectionStringText.setEditable(false);
                }
            }
        };

        usernameText.addListener(SWT.FocusIn, listener);
        passwordText.addListener(SWT.FocusIn, listener);
        serverText.addListener(SWT.FocusIn, listener);
        portText.addListener(SWT.FocusIn, listener);
        sidOrDatabaseText.addListener(SWT.FocusIn, listener);
        datasourceText.addListener(SWT.FocusIn, listener);
        schemaText.addListener(SWT.FocusIn, listener);
        additionParamText.addListener(SWT.FocusIn, listener);
        urlConnectionStringText.addListener(SWT.FocusIn, listener);

        // serverText : Event modifyText
        serverText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    if (!urlConnectionStringText.getEditable()) {
                        getConnection().setServerName(serverText.getText());
                        modifyFieldValue();
                    }
                }
            }
        });

        // portText : Event modifyText
        portText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    if (!urlConnectionStringText.getEditable()) {
                        getConnection().setPort(portText.getText());
                        modifyFieldValue();
                    }
                    // Check port
                    boolean b = true;
                    String databaseType = getConnection().getDatabaseType();
                    if (databaseType != null) {
                        if (databaseType.equals("Ingres")) { //$NON-NLS-1$
                            b = Pattern.matches(Messages.getString("DatabaseForm.ingresDBRegex"), portText.getText()); //$NON-NLS-1$
                        } else {
                            b = Pattern.matches(Messages.getString("DatabaseForm.otherDBRegex"), portText.getText()); //$NON-NLS-1$
                        }
                    }
                    if (b) {
                        b = portText.getText().length() <= 5;
                    }
                    evaluateTextField(b);
                }
            }
        });

        // portText : Event Key
        portText.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (!Character.isLetterOrDigit(e.character) && !Character.isIdentifierIgnorable(e.character)) {
                    e.doit = false;
                }
            }
        });

        // usernameText : Event modifyText
        usernameText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    if (!urlConnectionStringText.getEditable()) {
                        getConnection().setUsername(usernameText.getText());
                    }
                }
            }
        });

        // passwordText : Event modifyText
        passwordText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    if (!urlConnectionStringText.getEditable()) {
                        try {
                            String password = PasswordEncryptUtil.encryptPassword(passwordText.getText());
                            getConnection().setPassword(password);
                        } catch (Exception ex) {
                            ExceptionHandler.process(ex);
                        }
                    }
                }
            }
        });

        // sidOrDatabaseText : Event modifyText
        sidOrDatabaseText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    if (!urlConnectionStringText.getEditable()) {
                        getConnection().setSID(sidOrDatabaseText.getText());
                        modifyFieldValue();
                    }
                }
            }
        });

        // datasourceText : Event modifyText
        datasourceText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    if (!urlConnectionStringText.getEditable()) {
                        getConnection().setDatasourceName(datasourceText.getText());
                        modifyFieldValue();
                    }
                }
            }
        });

        // schemaText : Event modifyText
        schemaText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    if (!urlConnectionStringText.getEditable()) {
                        getConnection().setUiSchema(schemaText.getText());
                        modifyFieldValue();
                    }
                }
            }
        });

        // Db version
        dbVersionCombo.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    EDatabaseVersion4Drivers version = EDatabaseVersion4Drivers.indexOfByVersionDisplay(dbVersionCombo.getText());
                    if (version != null) {
                        getConnection().setDbVersionString(version.getVersionValue());
                    }
                    urlConnectionStringText.setText(getStringConnection());
                    checkFieldsValue();
                }
            }
        });
        hideDbVersion();

        // additional parameters: Event modifyText
        additionParamText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    if (!urlConnectionStringText.getEditable()) {
                        getConnection().setAdditionalParams(additionParamText.getText());
                        modifyFieldValue();
                    }
                }
            }
        });
        // standardButton parameters: Event modifyText
        standardButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!isContextMode()) {
                    getConnection().setStandardSQL(standardButton.getSelection());
                    getConnection().setSystemSQL(systemButton.getSelection());
                }
            }

        });
        // systemButton parameters: Event modifyText
        systemButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!isContextMode()) {
                    getConnection().setStandardSQL(standardButton.getSelection());
                    getConnection().setSystemSQL(systemButton.getSelection());
                }
            }

        });
        // button1 parameter:Event modifyText
        button1.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!isContextMode()) {
                    getConnection().setSQLMode(button1.getSelection());
                }
            }
        });
        // button2 parameter:Event modifyText
        button2.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!isContextMode()) {
                    getConnection().setSQLMode(!button2.getSelection());
                }
            }
        });
        // Event dbTypeCombo
        dbTypeCombo.addModifyListener(new ModifyListener() {

            // Event Modify
            public void modifyText(final ModifyEvent e) {

                // add for bug 12649
                boolean isGeneralJDBC = isGeneralJDBC();
                if (isGeneralJDBC) {
                    if (generalJdbcUrlText != null) {
                        generalJdbcUrlText.setText("");
                    }
                    if (generalJdbcUserText != null) {
                        generalJdbcUserText.setText("");
                    }
                    if (generalJdbcPasswordText != null) {
                        generalJdbcPasswordText.setText("");
                    }

                } else {
                    if (urlConnectionStringText != null) {
                        urlConnectionStringText.setText("");
                    }
                    if (usernameText != null) {

                        usernameText.setText("");
                    }
                    if (passwordText != null) {
                        passwordText.setText("");
                    }
                }
                if (schemaText != null) {
                    schemaText.setText("");
                }
                if (serverText != null) {
                    serverText.setText("");
                }
                if (sidOrDatabaseText != null) {
                    sidOrDatabaseText.setText("");
                }
                if (portText != null && portText.getText() != null) {
                    portText.setText("");
                }
                if (fileField != null) {
                    fileField.setText("");
                }
                if (datasourceText != null) {
                    datasourceText.setText("");
                }
                if (additionParamText != null) {
                    additionParamText.setText("");
                }
                if (generalJdbcClassNameText != null) {
                    generalJdbcClassNameText.setText("");
                }
                if (generalJdbcDriverjarText != null) {
                    generalJdbcDriverjarText.setText("");
                }
                if (additionParamText != null) {
                    additionParamText.setText("");
                }

                boolean hiddenGeneral = !isGeneralJDBC();
                // change controls
                switchBetweenTypeandGeneralDB(hiddenGeneral);

                if (!isContextMode()) {
                    setPropertiesFormEditable(true);
                    getConnection().setDatabaseType(dbTypeCombo.getText());
                    EDatabaseConnTemplate template = EDatabaseConnTemplate.indexOfTemplate(getConnection().getDatabaseType());
                    if (template != null) {
                        portText.setText(template.getDefaultPort());
                    }
                    final String product = EDatabaseTypeName.getTypeFromDisplayName(getConnection().getDatabaseType())
                            .getProduct();
                    getConnection().setProductId(product);

                    String mapping = null;

                    if (product == null || product.equals(EDatabaseConnTemplate.GENERAL_JDBC.getDBDisplayName())) {
                        mapping = generalMappingFileText.getText();
                    } else {
                        if (MetadataTalendType.getDefaultDbmsFromProduct(product) != null) {
                            mapping = MetadataTalendType.getDefaultDbmsFromProduct(product).getId();
                        }
                    }
                    if (mapping == null) {
                        mapping = "mysql_id"; // default value //$NON-NLS-1$
                    }

                    getConnection().setDbmsId(mapping);
                    additionParamText.setText(EDatabaseConnTemplate.getAdditionProperty(dbTypeCombo.getText()));
                    if (dbTypeCombo.getText().equals(EDatabaseConnTemplate.INFORMIX.getDBDisplayName())) {
                        datasourceText.setLabelText(Messages.getString("DatabaseForm.informixInstance"));
                    }
                    checkAndSetIniSQLModel();
                    checkAS400SpecificCase();
                    checkFieldsValue();
                    hideDbVersion();
                    // see bug 0005237: Create DB Connection issue.
                    if (!schemaText.getEditable()) {
                        schemaText.setText(""); //$NON-NLS-1$
                    }
                }
                checkScrolledCompositeSize();
            }

        });

        // When the DbType is selected, disabled the action of keyboard's letter
        // to modify the combo
        // utils when the user use the keyboard to write the connection string
        dbTypeCombo.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (dbTypeCombo.getSelectionIndex() > -1) {
                    if (Character.isLetterOrDigit(e.character)) {
                        e.doit = false;
                    }
                }
            }
        });

        // Event fileField
        fileField.addListener(SWT.FocusIn, new Listener() {

            // Event FocusIn
            public void handleEvent(final Event e) {
                if (!isContextMode()) {
                    if (dbTypeCombo.getSelectionIndex() == -1) {
                        dbTypeCombo.forceFocus();
                    } else {
                        EDatabaseConnTemplate template = EDatabaseConnTemplate.indexOfTemplate(getConnection().getDatabaseType());
                        EDatabaseVersion4Drivers version = EDatabaseVersion4Drivers.indexOfByVersionDisplay(getConnection()
                                .getDbVersionString());
                        if (template != null
                                && template.getUrlTemplate(version).contains(EDatabaseConnVar.FILENAME.getVariable())) {
                            setPropertiesFormEditable(true);
                            urlConnectionStringText.setEditable(false);
                        }
                        getConnection().setFileFieldName(PathUtils.getPortablePath(fileField.getText()));
                    }
                }
            }
        });
        // Event Modify
        fileField.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    if (!urlConnectionStringText.getEditable()) {
                        getConnection().setFileFieldName(fileField.getText());
                        modifyFieldValue();
                    }
                }
            }
        });

        directoryField.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    if (!urlConnectionStringText.getEditable()) {
                        String text = directoryField.getText();
                        getConnection().setDBRootPath(text);
                        checkFieldsValue();
                    }
                }
            }
        });
        // sqlSyntaxText : Event modifyText
        sqlSyntaxCombo.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    getConnection().setSqlSynthax(sqlSyntaxCombo.getText());
                }
            }
        });

        // nullCharText : Event modifyText
        nullCharText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    getConnection().setNullChar(nullCharText.getText());
                }
            }
        });

        // stringQuoteText : Event modifyText
        stringQuoteText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    getConnection().setStringQuote(stringQuoteText.getText());
                }
            }
        });

        addGeneralDbFieldsListeners();

        // urlConnectionStringText : Event modifyText
        // urlConnectionStringText.addModifyListener(new ModifyListener() {
        //
        // public void modifyText(final ModifyEvent e) {
        // if (!isContextMode()) {
        // getConnection().setURL(urlConnectionStringText.getText());
        // getConnection().setServerName(serverText.getText());
        // getConnection().setPort(portText.getText());
        // getConnection().setUsername(usernameText.getText());
        // getConnection().setPassword(passwordText.getText());
        // getConnection().setSID(sidOrDatabaseText.getText());
        // getConnection().setDatasourceName(datasourceText.getText());
        // getConnection().setSchema(schemaText.getText());
        // getConnection().setDatabaseType(dbTypeCombo.getText());
        // getConnection().setFileFieldName(fileField.getText());
        // getConnection().setDBRootPath(directoryField.getText());
        // getConnection().setAdditionalParams(additionParamText.getText());
        // }
        // }
        // });

    }

    private void checkScrolledCompositeSize() {
        // TODO Auto-generated method stub
        int i = 0;
        if (urlConnectionStringText.isVisiable()) {
            i = i + 1;
        }
        if (usernameText.isVisiable()) {
            i = i + 1;
        }
        if (passwordText.isVisiable()) {
            i = i + 1;
        }
        if (serverText.isVisiable()) {
            i = i + 1;
        }
        if (portText.isVisiable()) {
            i = i + 1;
        }
        if (sidOrDatabaseText.isVisiable()) {
            i = i + 1;
        }
        if (schemaText.isVisiable()) {
            i = i + 1;
        }
        if (datasourceText.isVisiable()) {
            i = i + 1;
        }
        if (additionParamText.isVisiable()) {
            i = i + 1;
        }
        if (i > 6) {
            scrolledComposite.setMinSize(SWT.DEFAULT, 310);
        } else {
            scrolledComposite.setMinSize(SWT.DEFAULT, 240);
        }
    }

    /**
     * 
     * ggu Comment method "checkAndSetIniSQLModel".
     * 
     * bug 12811
     */
    private void checkAndSetIniSQLModel() {
        if (isCreation && first) {
            getConnection().setSQLMode(false);
            setSqlModelFields();
            first = false;
        }
    }

    private void modifyFieldValue() {
        isModify = true;
        checkFieldsValue();
        isModify = false;
    }

    /**
     * DOC YeXiaowei Comment method "hideDbVersion".
     */
    private void hideDbVersion() {
        // qli comment
        // Just layout version combo when choose db type
        String dbType = dbTypeCombo.getText();
        List<String> items = getVersionDrivers(dbType);
        String[] versions = new String[items.size()];
        items.toArray(versions);

        boolean isOracle = oracleVersionEnable();
        boolean isAS400 = as400VersionEnable();
        boolean isMySQL = asMySQLVersionEnable();
        boolean isVertica = asVerticaVersionEnable();

        String selectedVersion = getConnection().getDbVersionString();
        dbVersionCombo.removeAll();
        dbVersionCombo.setHideWidgets(true);
        if (dbType.equals(EDatabaseConnTemplate.ORACLEFORSID.getDBDisplayName())
                || dbType.equals(EDatabaseConnTemplate.ORACLESN.getDBDisplayName())
                || dbType.equals(EDatabaseConnTemplate.ORACLE_OCI.getDBDisplayName())) {
            dbVersionCombo.getCombo().setItems(versions);
            dbVersionCombo.setHideWidgets(!isOracle);
        } else if (dbType.equals(EDatabaseConnTemplate.AS400.getDBDisplayName())) {
            dbVersionCombo.getCombo().setItems(versions);
            dbVersionCombo.setHideWidgets(!isAS400);
        } else if (dbType.equals(EDatabaseConnTemplate.ACCESS.getDBDisplayName())) {
            dbVersionCombo.getCombo().setItems(versions);
            dbVersionCombo.setHideWidgets(false);
        } else if (dbType.equals(EDatabaseConnTemplate.MYSQL.getDBDisplayName())) {
            dbVersionCombo.getCombo().setItems(versions);
            dbVersionCombo.setHideWidgets(!isMySQL);
        } else if (dbType.equals(EDatabaseConnTemplate.VERTICA.getDBDisplayName())) {
            dbVersionCombo.getCombo().setItems(versions);
            dbVersionCombo.setHideWidgets(!isVertica);
        }
        if (selectedVersion != null && !"".equals(selectedVersion)) { //$NON-NLS-1$
            EDatabaseVersion4Drivers version = EDatabaseVersion4Drivers.indexOfByVersion(selectedVersion);
            if (version != null) {
                dbVersionCombo.setText(version.getVersionDisplay());
            }
        } else {
            dbVersionCombo.select(0);
            EDatabaseVersion4Drivers version = EDatabaseVersion4Drivers.indexOfByVersionDisplay(dbVersionCombo.getText());
            if (version != null) {
                getConnection().setDbVersionString(version.getVersionValue());
            }
        }
        urlConnectionStringText.setText(getStringConnection());
    }

    /**
     * DOC YeXiaowei Comment method "addGeneralDbFieldsListeners".
     */
    private void addGeneralDbFieldsListeners() {

        generalJdbcClassNameText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    if (validText(generalJdbcClassNameText.getText())) {
                        getConnection().setDriverClass(generalJdbcClassNameText.getText());
                        checkFieldsValue();
                    }
                }
            }
        });

        generalJdbcDriverjarText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    if (validText(generalJdbcDriverjarText.getText())) {
                        getConnection().setDriverJarPath(generalJdbcDriverjarText.getText());
                        checkFieldsValue();
                    }
                }
            }
        });

        generalJdbcUrlText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    if (validText(generalJdbcUrlText.getText())) {
                        getConnection().setURL(generalJdbcUrlText.getText());
                        checkFieldsValue();
                    }
                }
            }
        });

        generalJdbcPasswordText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    if (validText(generalJdbcPasswordText.getText())) {
                        try {
                            String password = PasswordEncryptUtil.encryptPassword(generalJdbcPasswordText.getText());
                            getConnection().setPassword(password);
                        } catch (Exception ex) {
                            ExceptionHandler.process(ex);
                        }
                        checkFieldsValue();
                    }
                }
            }
        });

        generalJdbcUserText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    if (validText(generalJdbcUserText.getText())) {
                        getConnection().setUsername(generalJdbcUserText.getText());
                        checkFieldsValue();
                    }
                }
            }
        });

        jDBCschemaText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    String text = jDBCschemaText.getText();
                    schemaText.setText(text);
                    if (validText(schemaText.getText())) {
                        getConnection().setUiSchema(schemaText.getText());
                        checkFieldsValue();
                    }
                }
            }
        });

        generalMappingFileText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    if (validText(generalMappingFileText.getText())) {
                        getConnection().setDbmsId(generalMappingFileText.getText());
                        checkFieldsValue();
                    }
                }
            }
        });

        browseJarFilesButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                SelectDatabaseJarDialog dialog = new SelectDatabaseJarDialog(getShell(), generalJdbcDriverjarText.getText());
                if (dialog.open() == Window.OK) {
                    generalJdbcDriverjarText.setText(dialog.getJarsString());
                }
            }

        });

        mappingSelectButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                MappingFileSelectDialog dialog = new MappingFileSelectDialog(getShell());
                dialog.open();
                generalMappingFileText.setText(dialog.getSelectId());
            }
        });
    }

    /**
     * 
     * DOC YeXiaowei Comment method "isGeneralJDBC".
     * 
     * @return
     */
    private boolean isGeneralJDBC() {
        return dbTypeCombo.getText().equals(EDatabaseConnTemplate.GENERAL_JDBC.getDBDisplayName());
    }

    /**
     * Ensures that fields are set. Update checkEnable / use to checkConnection().
     */
    @Override
    public boolean checkFieldsValue() {

        boolean checkGeneralDB = isGeneralJDBC();

        // See bug 004800
        if (!checkGeneralDB || dbTypeCombo.getText().equals(EDatabaseConnTemplate.ACCESS.getDBDisplayName())) {
            getConnection().setURL(getStringConnection());
        }
        boolean isTeradata = EDatabaseTypeName.TERADATA.getDisplayName().equals(dbTypeCombo.getText());
        sqlModeLabel.setVisible(isTeradata);
        button1.setVisible(isTeradata);
        button2.setVisible(isTeradata);
        /*
         * commet by bug 12811
         */
        // if (isTeradata) {
        // button1.setSelection(!isTeradata);
        // button2.setSelection(isTeradata);
        // } else {
        // button1.setSelection(getConnection().isSQLMode());
        // button2.setSelection(!getConnection().isSQLMode());
        // }

        if (isContextMode()) {
            return true;
        }
        databaseSettingIsValide = false;
        urlConnectionStringText.setText(getStringConnection());
        EDatabaseConnTemplate template = EDatabaseConnTemplate.indexOfTemplate(getConnection().getDatabaseType());
        if (template != null) {
            EDatabaseVersion4Drivers version = EDatabaseVersion4Drivers.indexOfByVersion(getConnection().getDbVersionString());
            urlConnectionStringText.setToolTipText(template.getUrlTemplate(version));
        }
        updateCheckButton();

        if (!isModify) {
            setPropertiesFormEditable(false);
        }

        if (dbTypeCombo.getSelectionIndex() < 0) {
            updateStatus(IStatus.ERROR, Messages.getString("DatabaseForm.alert", dbTypeCombo.getLabel())); //$NON-NLS-1$
            return false;
        }

        // Show Database Properties
        if (!isModify) {
            setPropertiesFormEditable(true);
        }

        if (!checkGeneralDB) {
            if (!checkTypeDBFieldValues()) {
                return false;
            }
        } else {
            if (!checkGeneralDBFieldValues()) {
                return false;
            }
        }

        if (!databaseSettingIsValide) {
            updateStatus(IStatus.INFO, Messages.getString("DatabaseForm.checkInformation")); //$NON-NLS-1$
            return false;
        }
        if (sqlSyntaxCombo.getSelectionIndex() == -1) {
            updateStatus(IStatus.WARNING, Messages.getString("DatabaseForm.alert", sqlSyntaxCombo.getLabel())); //$NON-NLS-1$
            return false;
        }
        if (nullCharText.getCharCount() == 0) {
            updateStatus(IStatus.WARNING, Messages.getString("DatabaseForm.alert", nullCharText.getLabelText())); //$NON-NLS-1$
            return false;
        }
        if (stringQuoteText.getCharCount() == 0) {
            updateStatus(IStatus.WARNING, Messages.getString("DatabaseForm.alert", stringQuoteText.getLabelText())); //$NON-NLS-1$
            return false;
        }

        updateStatus(IStatus.OK, null);
        return true;
    }

    private boolean checkGeneralDBFieldValues() {
        String value = generalJdbcUrlText.getText();
        if (!validText(value)) {
            updateStatus(IStatus.WARNING, Messages.getString("DatabaseForm.alert", generalJdbcUrlText.getLabelText())); //$NON-NLS-1$
            return false;
        }

        value = generalJdbcDriverjarText.getText();
        if (!validText(value)) {
            updateStatus(IStatus.WARNING, Messages.getString("DatabaseForm.alert", generalJdbcDriverjarText.getLabelText())); //$NON-NLS-1$
            return false;
        }

        value = generalJdbcClassNameText.getText();
        if (!validText(value)) {
            updateStatus(IStatus.WARNING, Messages.getString("DatabaseForm.alert", generalJdbcClassNameText.getLabelText())); //$NON-NLS-1$
            return false;
        }

        value = generalJdbcUserText.getText();
        if (!validText(value)) {
            updateStatus(IStatus.WARNING, Messages.getString("DatabaseForm.alert", generalJdbcUserText.getLabelText())); //$NON-NLS-1$
            return false;
        }

        value = generalJdbcPasswordText.getText();
        if (!validText(value)) {
            updateStatus(IStatus.WARNING, Messages.getString("DatabaseForm.alert", generalJdbcPasswordText.getLabelText())); //$NON-NLS-1$
            return false;
        }

        value = jDBCschemaText.getText();
        if (!validText(value)) {
            updateStatus(IStatus.WARNING, Messages.getString("DatabaseForm.alert", jDBCschemaText.getLabelText())); //$NON-NLS-1$
            return false;
        }

        updateStatus(IStatus.OK, null);
        return true;
    }

    private boolean validText(final String value) {
        return value != null && !value.equals(""); //$NON-NLS-1$
    }

    private boolean checkTypeDBFieldValues() {

        // Another fields depend on DbType
        EDatabaseConnTemplate template = EDatabaseConnTemplate.indexOfTemplate(getConnection().getDatabaseType());
        EDatabaseVersion4Drivers version = EDatabaseVersion4Drivers.indexOfByVersion(getConnection().getDbVersionString());
        String s = ""; //$NON-NLS-1$
        if (template != null) {
            s = template.getUrlTemplate(version);
        }

        if (s.contains(EDatabaseConnVar.HOST.getVariable()) && serverText.getCharCount() == 0) {
            updateStatus(IStatus.WARNING, Messages.getString("DatabaseForm.alert", serverText.getLabelText())); //$NON-NLS-1$
            return false;
        }
        if (s.contains(EDatabaseConnVar.PORT.getVariable()) && portText.getCharCount() == 0) {
            updateStatus(IStatus.WARNING, Messages.getString("DatabaseForm.alert", portText.getLabelText())); //$NON-NLS-1$
            return false;
        }
        if ((s.contains(EDatabaseConnVar.SID.getVariable()) || s.contains(EDatabaseConnVar.SERVICE_NAME.getVariable()))
                && sidOrDatabaseText.getCharCount() == 0) {
            updateStatus(IStatus.WARNING, Messages.getString("DatabaseForm.alert", sidOrDatabaseText.getLabelText())); //$NON-NLS-1$
            return false;
        }
        if (s.contains(EDatabaseConnVar.FILENAME.getVariable()) && fileField.getText() == "") { //$NON-NLS-1$ 
            updateStatus(IStatus.WARNING, Messages.getString("DatabaseForm.alert", fileField.getLabelText())); //$NON-NLS-1$
            return false;
        }
        if (s.contains(EDatabaseConnVar.DATASOURCE.getVariable()) && datasourceText.getText() == "") { //$NON-NLS-1$
            updateStatus(IStatus.WARNING, Messages.getString("DatabaseForm.alert", datasourceText.getLabelText())); //$NON-NLS-1$
            return false;
        }
        if (s.contains(EDatabaseConnVar.DBROOTPATH.getVariable()) && directoryField.getText() == "") { //$NON-NLS-1$ 
            updateStatus(IStatus.WARNING, Messages.getString("DatabaseForm.alert", directoryField.getLabelText())); //$NON-NLS-1$
            return false;
        }

        if (EDatabaseConnTemplate.isSchemaNeeded(getConnection().getDatabaseType()) && schemaText.getCharCount() == 0) {
            updateStatus(IStatus.WARNING, Messages.getString("DatabaseForm.alert", schemaText.getLabelText())); //$NON-NLS-1$
            return false;
        }
        updateStatus(IStatus.OK, null);
        return true;

    }

    private String getStringConnection() {
        String s = null;
        if (isContextMode()) {
            s = DBConnectionContextUtils.getUrlConnectionString(connectionItem, true);
        } else {
            String versionStr = dbVersionCombo.getText();
            EDatabaseVersion4Drivers version = EDatabaseVersion4Drivers.indexOfByVersionDisplay(versionStr);
            if (version != null) {
                versionStr = version.getVersionValue();
            }
            s = DatabaseConnStrUtil.getURLString(dbTypeCombo.getText(), versionStr, serverText.getText(), usernameText.getText(),
                    passwordText.getText(), portText.getText(), sidOrDatabaseText.getText(), fileField.getText().toLowerCase(),
                    datasourceText.getText(), directoryField.getText(), additionParamText.getText());
        }

        return s;
    }

    private void updateCheckButton() {
        // update checkEnable
        if (isContextMode()) {
            checkButton.setEnabled(true);
        } else {
            EDatabaseConnTemplate template = EDatabaseConnTemplate.indexOfTemplate(getConnection().getDatabaseType());
            EDatabaseVersion4Drivers version = EDatabaseVersion4Drivers.indexOfByVersionDisplay(getConnection()
                    .getDbVersionString());
            checkButton.setEnabled((dbTypeCombo.getSelectionIndex() >= 0) && template != null
                    && (getStringConnection() != template.getUrlTemplate(version)));
            if (isGeneralJDBC()) {
                checkButton.setEnabled(true);
            }
        }
    }

    /**
     * SetEditable fields.
     * 
     * @param boolean
     */
    @SuppressWarnings("static-access")
    private void setPropertiesFormEditable(boolean visible) {
        clearContextParams();
        EDBParamName sidOrDatabase = null;
        // update the UI label
        if (EDatabaseTypeName.ORACLEFORSID.getProduct().equals(getConnection().getProductId())) {
            if (EDatabaseConnTemplate.ORACLESN.getDBDisplayName().equals(getConnection().getDatabaseType())) {
                sidOrDatabaseText.setLabelText(Messages.getString("DatabaseForm.service_name")); //$NON-NLS-1$
                sidOrDatabaseText.setLabelWidth(65);
                sidOrDatabase = EDBParamName.ServiceName;
            } else if (EDatabaseConnTemplate.ORACLEFORSID.getDBDisplayName().equals(getConnection().getDatabaseType())) {
                sidOrDatabaseText.setLabelText(Messages.getString("DatabaseForm.sid")); //$NON-NLS-1$
                sidOrDatabase = EDBParamName.Sid;
            } else if (EDatabaseConnTemplate.ORACLE_OCI.getDBDisplayName().equals(getConnection().getDatabaseType())) {
                sidOrDatabaseText.setLabelText(Messages.getString("DatabaseForm.local_service_name")); //$NON-NLS-1$
                sidOrDatabase = EDBParamName.Sid;
            }
        } else {
            sidOrDatabaseText.setLabelText(Messages.getString("DatabaseForm.DataBase")); //$NON-NLS-1$
            sidOrDatabase = EDBParamName.Database;
            if (EDatabaseConnTemplate.INFORMIX.getDBDisplayName().equals(getConnection().getDatabaseType())) {
                sidOrDatabaseText.setLabelText(Messages.getString("DatabaseForm.DataBase")); //$NON-NLS-1$
            }

        }

        if (EDatabaseTypeName.MSODBC.getDisplayName().equals(dbTypeCombo.getText())) {
            sidOrDatabaseText.setLabelText(Messages.getString("DatabaseForm.DataBase")); //$NON-NLS-1$
        }

        if (EDatabaseTypeName.GODBC.getDisplayName().equals(dbTypeCombo.getText())) {
            sidOrDatabaseText.setLabelText(Messages.getString("DatabaseForm.DataBase")); //$NON-NLS-1$
        }

        // hshen
        if (EDatabaseConnTemplate.GENERAL_JDBC.getDBTypeName().equals(dbTypeCombo.getText())) {
            addContextParams(EDBParamName.JdbcUrl, visible);
            addContextParams(EDBParamName.DriverJar, visible);
            addContextParams(EDBParamName.MappingFile, visible);
            addContextParams(EDBParamName.ClassName, visible);
        }

        addContextParams(EDBParamName.Login, visible);
        addContextParams(EDBParamName.Password, visible);
        // update the UI Fields

        boolean isOracle = visible && oracleVersionEnable();
        boolean isAS400 = visible && as400VersionEnable();
        boolean isMySQL = visible && asMySQLVersionEnable();
        boolean isVertica = visible && asVerticaVersionEnable();

        dbVersionCombo.setEnabled(!isReadOnly()
                && (isOracle || isAS400 || isMySQL || isVertica || EDatabaseConnTemplate.ACCESS.getDBTypeName().equals(
                        dbTypeCombo.getText())));
        usernameText.setEditable(visible);
        passwordText.setEditable(visible);
        serverText.setEditable(false);
        portText.setEditable(false);
        sidOrDatabaseText.setEditable(false);
        datasourceText.setEditable(false);
        additionParamText.setEditable(false);
        schemaText.setEditable(false);
        fileField.setEditable(false);
        directoryField.setEditable(false);
        if (dbTypeCombo.getSelectionIndex() < 0) {
            urlConnectionStringText.setEditable(false);
        } else {
            EDatabaseConnTemplate template = EDatabaseConnTemplate.indexOfTemplate(dbTypeCombo.getText());
            String s = ""; //$NON-NLS-1$
            if (template != null) {
                EDatabaseVersion4Drivers version = EDatabaseVersion4Drivers.indexOfByVersionDisplay(dbVersionCombo.getText());
                s = template.getUrlTemplate(version);
            }
            urlConnectionStringText.setEditable(!visible);
            // schemaText.hide();
            boolean schemaTextIsShow = true;
            if (template == EDatabaseConnTemplate.MSSQL) {
                schemaText.show();
                schemaText.setEditable(true);
                addContextParams(EDBParamName.Schema, true);

                // for bug 13033
                //                if (schemaText.getText().equals("")) { //$NON-NLS-1$
                //                    schemaText.setText("dbo"); //$NON-NLS-1$
                // }
            } else if (template == EDatabaseConnTemplate.VERTICA || template == EDatabaseConnTemplate.INFORMIX) {
                // add for bug 0009553 10531
                schemaText.show();
                schemaText.setEditable(true);
                addContextParams(EDBParamName.Schema, true);
            } else if (template == EDatabaseConnTemplate.GENERAL_JDBC) {
                String jdbcUrlString = "";
                if (isContextMode()) {
                    ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(getShell(), getConnection(),
                            true);
                    if (contextType != null) {
                        jdbcUrlString = ConnectionContextHelper.getOriginalValue(contextType, getConnection().getURL());
                    }
                } else {
                    jdbcUrlString = generalJdbcUrlText.getText();
                }
                if (jdbcUrlString.contains("sqlserver")) {//$NON-NLS-1$
                    jDBCschemaText.setHideWidgets(false);
                    addContextParams(EDBParamName.Schema, true);
                } else {
                    jDBCschemaText.setHideWidgets(true);
                    addContextParams(EDBParamName.Schema, false);
                }

            } else {
                // schemaText.hide();
                schemaTextIsShow = false;
                // addContextParams(EDBParamName.Schema, false);
            }

            if (s.contains(EDatabaseConnVar.HOST.getVariable())) {
                if (!EDatabaseConnTemplate.GENERAL_JDBC.getDBTypeName().equals(dbTypeCombo.getText())) {
                    serverText.show();
                    serverText.setEditable(visible);
                    addContextParams(EDBParamName.Server, visible);
                }
            } else {
                serverText.hide();
                addContextParams(EDBParamName.Server, false);
            }
            if (s.contains(EDatabaseConnVar.PORT.getVariable())) {
                portText.show();
                portText.setEditable(visible);
                addContextParams(EDBParamName.Port, visible);
            } else {
                portText.hide();
                addContextParams(EDBParamName.Port, false);
            }
            if (s.contains(EDatabaseConnVar.SID.getVariable()) || s.contains(EDatabaseConnVar.SERVICE_NAME.getVariable())) {
                if (!EDatabaseConnTemplate.GENERAL_JDBC.getDBTypeName().equals(dbTypeCombo.getText())) {
                    sidOrDatabaseText.show();
                    sidOrDatabaseText.setEditable(visible);
                    addContextParams(sidOrDatabase, visible);
                } else {
                    sidOrDatabaseText.hide();
                    addContextParams(sidOrDatabase, false);
                }
            } else {
                if (template.getDbType() != EDatabaseTypeName.JAVADB_EMBEDED) {
                    sidOrDatabaseText.hide();
                    addContextParams(sidOrDatabase, false);
                }
            }
            if (s.contains(EDatabaseConnVar.FILENAME.getVariable())) {
                fileField.show();
                fileField.setEditable(!isReadOnly() && visible);
                addContextParams(EDBParamName.File, visible);
                boolean isSqlLite = false;
                if (template.getDbType() == EDatabaseTypeName.SQLITE) {
                    isSqlLite = true;
                    usernameText.hide();
                    passwordText.hide();
                } else {
                    isSqlLite = false;
                    usernameText.show();
                    passwordText.show();
                }
                usernameText.setEditable(!isSqlLite);
                passwordText.setEditable(!isSqlLite);
                addContextParams(EDBParamName.Login, !isSqlLite);
                addContextParams(EDBParamName.Password, !isSqlLite);
            } else {
                fileField.hide();
                addContextParams(EDBParamName.File, false);
                // if (template.getDbType() != EDatabaseTypeName.SYBASEASE && template.getDbType() !=
                // EDatabaseTypeName.TERADATA) {
                // usernameText.hide();
                // passwordText.hide();
                // addContextParams(EDBParamName.Login, false);
                // addContextParams(EDBParamName.Password, false);
                // } else {
                usernameText.show();
                passwordText.show();
                addContextParams(EDBParamName.Login, true);
                addContextParams(EDBParamName.Password, true);
                // }

            }
            if (s.contains(EDatabaseConnVar.DATASOURCE.getVariable())) {
                datasourceText.show();
                datasourceText.setEditable(visible);
                addContextParams(EDBParamName.Datasource, visible);
            } else {
                datasourceText.hide();
                addContextParams(EDBParamName.Datasource, false);
            }

            if (s.contains(EDatabaseConnVar.DBROOTPATH.getVariable())) {
                directoryField.show();
                directoryField.setEditable(visible);
                sidOrDatabaseText.setEditable(visible);
                addContextParams(EDBParamName.DBRootPath, visible);
                addContextParams(sidOrDatabase, visible);
            } else {
                directoryField.hide();
                addContextParams(EDBParamName.DBRootPath, false);
            }

            if (EDatabaseConnTemplate.isSchemaNeeded(getConnection().getDatabaseType())) {
                schemaText.show();
                schemaText.setEditable(visible);
                addContextParams(EDBParamName.Schema, visible);
            } else {
                if (!schemaTextIsShow) {
                    schemaText.hide();
                    addContextParams(EDBParamName.Schema, false);
                }
            }
            if (EDatabaseConnTemplate.isAddtionParamsNeeded(getConnection().getDatabaseType())
                    && !EDatabaseConnTemplate.GENERAL_JDBC.getDBTypeName().equals(dbTypeCombo.getText()) && visible) {
                additionParamText.show();
                additionParamText.setEditable(true);
                addContextParams(EDBParamName.AdditionalParams, true);
            } else {
                additionParamText.hide();
                addContextParams(EDBParamName.AdditionalParams, false);
            }
            if (EDatabaseConnTemplate.FIREBIRD.equals(template)) {
                portText.show();
                portText.setEditable(true);
                addContextParams(EDBParamName.Port, true);
            }
        }

        compositeDbSettings.layout();
        typeDbCompositeParent.layout();
        newParent.layout();
        databaseSettingGroup.layout();
        compositeGroupDbSettings.layout();
    }

    /**
     * 
     * DOC YeXiaowei Comment method "oracleVersionEnable".
     * 
     * @return
     */
    private boolean oracleVersionEnable() {

        if (dbTypeCombo == null) {
            return false;
        }
        EDatabaseConnTemplate template = EDatabaseConnTemplate.indexOfTemplate(dbTypeCombo.getText());
        return template != null
                && (template == EDatabaseConnTemplate.ORACLEFORSID || template == EDatabaseConnTemplate.ORACLESN || template == EDatabaseConnTemplate.ORACLE_OCI);
    }

    /**
     * 
     * DOC qli Comment method "as400VersionEnable".
     * 
     * @return
     */
    private boolean as400VersionEnable() {

        if (dbTypeCombo == null) {
            return false;
        }
        EDatabaseConnTemplate template = EDatabaseConnTemplate.indexOfTemplate(dbTypeCombo.getText());
        return template != null && template == EDatabaseConnTemplate.AS400
                && LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA);
    }

    /**
     * 
     * DOC zli Comment method "as400VersionEnable".
     * 
     * @return
     */
    private boolean asMySQLVersionEnable() {
        // for bug 11487
        if (dbTypeCombo == null) {
            return false;
        }
        EDatabaseConnTemplate template = EDatabaseConnTemplate.indexOfTemplate(dbTypeCombo.getText());
        return template != null && template == EDatabaseConnTemplate.MYSQL
                && LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA);
    }

    /**
     * 
     * DOC hwang Comment method "VerticaVersionEnable".
     * 
     * @return
     */
    private boolean asVerticaVersionEnable() {
        if (dbTypeCombo == null) {
            return false;
        }
        EDatabaseConnTemplate template = EDatabaseConnTemplate.indexOfTemplate(dbTypeCombo.getText());
        return template != null && template == EDatabaseConnTemplate.VERTICA
                && LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.widgets.Control#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        updateCheckButton();
        if (urlConnectionStringText.getText().equals("")) { //$NON-NLS-1$
            urlConnectionStringText.setText(getStringConnection());
        }
        if (isContextMode()) {
            adaptFormToEditable();
        } else {
            EDatabaseConnTemplate template = EDatabaseConnTemplate.indexOfTemplate(getConnection().getDatabaseType());
            setPropertiesFormEditable(template != null);
        }
        if (isReadOnly() != readOnly) {
            adaptFormToReadOnly();
        }
        /**
         * add by wzhang. Estimate the status
         */
        if (visible) {
            updateStatus(getStatusLevel(), getStatus());
        }
    }

    protected DatabaseConnection getConnection() {
        return (DatabaseConnection) connectionItem.getConnection();
    }

    protected void evaluateTextField(boolean b) {

        if (b) {
            updateStatus(IStatus.OK, null);
        } else {
            updateStatus(IStatus.ERROR, Messages.getString("DatabaseForm.portError")); //$NON-NLS-1$

        }
    }

    @Override
    protected void adaptFormToEditable() {
        super.adaptFormToEditable();
        dbTypeCombo.setReadOnly(isContextMode());

        urlConnectionStringText.setEditable(!isContextMode());
        usernameText.setEditable(!isContextMode());
        passwordText.setEditable(!isContextMode());
        serverText.setEditable(!isContextMode());
        portText.setEditable(!isContextMode());
        sidOrDatabaseText.setEditable(!isContextMode());
        schemaText.setEditable(!isContextMode());
        dbVersionCombo.setReadOnly(isContextMode());
        datasourceText.setEditable(!isContextMode());
        additionParamText.setEditable(!isContextMode());
        fileField.setEditable(!isContextMode());
        directoryField.setEditable(!isContextMode());

        sqlSyntaxCombo.setReadOnly(isContextMode());
        stringQuoteText.setEditable(!isContextMode());
        nullCharText.setEditable(!isContextMode());
        button1.setEnabled(!isContextMode());
        button2.setEnabled(!isContextMode());
        // hshen
        generalJdbcUrlText.setEditable(!isContextMode());

        generalJdbcUserText.setEditable(!isContextMode());

        generalJdbcPasswordText.setEditable(!isContextMode());

        generalJdbcClassNameText.setEditable(!isContextMode());

        generalJdbcDriverjarText.setEditable(!isContextMode());

        jDBCschemaText.setEditable(!isContextMode());

        generalMappingFileText.setEditable(!isContextMode());
        if (isContextMode()) {
            passwordText.getTextControl().setEchoChar('\0');
            generalJdbcPasswordText.getTextControl().setEchoChar('\0');
        } else {
            passwordText.getTextControl().setEchoChar('*');
            generalJdbcPasswordText.getTextControl().setEchoChar('*');
        }
    }

    public boolean isDbTypenull() {
        return dbTypeCombo.getText().trim().length() == 0;
    }
}
