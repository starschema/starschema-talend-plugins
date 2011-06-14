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
package org.talend.repository.ui.wizards.metadata.connection.files.salesforce;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.commons.ui.swt.formtools.UtilsButton;
import org.talend.commons.ui.swt.thread.SWTUIThreadProcessor;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataContextModeManager;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection;
import org.talend.core.model.metadata.types.JavaType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.utils.CsvArray;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.repository.i18n.Messages;
import org.talend.repository.preview.ProcessDescription;
import org.talend.repository.preview.SalesforceSchemaBean;
import org.talend.repository.ui.swt.preview.ShadowProcessPreview;
import org.talend.repository.ui.swt.utils.AbstractSalesforceStepForm;
import org.talend.repository.ui.utils.ConnectionContextHelper;
import org.talend.repository.ui.utils.OtherConnectionContextUtils;
import org.talend.repository.ui.utils.ShadowProcessHelper;

/**
 * DOC YeXiaowei class global comment. Detailled comment <br/>
 * 
 */
public class SalesforceStep2Form extends AbstractSalesforceStepForm {

    private static Logger log = Logger.getLogger(SalesforceStep2Form.class);

    private LabelledText queryConditionText = null;

    private String defaultQueryString = ""; // 'name == talend' //$NON-NLS-1$

    private Button previewButton = null;

    private Label previewInformationLabel = null;

    private ShadowProcessPreview salesforcePreviewProcess = null;

    private boolean readOnly;

    private Button alphabet;

    private UtilsButton cancelButton;

    private TableViewer moduleViewer = null;

    private static final int COLUMN_WIDTH = 60;

    private SWTUIThreadProcessor processor = new PreviewProcessor();

    /**
     * Output tab.
     */
    private CTabFolder tabFolder;

    private CTabItem previewTabItem;

    private CTabItem outputTabItem;

    private Composite outputComposite;

    private SalesforceModuleParseAPI salesforceAPI = null;

    /**
     * DOC YeXiaowei SalesforceStep2Form constructor comment.
     * 
     * @param parent
     * @param connectionItem
     */
    public SalesforceStep2Form(Composite parent, ConnectionItem connectionItem, SalesforceModuleParseAPI salesforceAPI,
            IMetadataContextModeManager contextModeManager) {
        super(parent, connectionItem, salesforceAPI);
        setConnectionItem(connectionItem);
        setContextModeManager(contextModeManager);
        setupForm(true);
        this.salesforceAPI = salesforceAPI;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.swt.utils.AbstractForm#adaptFormToReadOnly()
     */
    @Override
    protected void adaptFormToReadOnly() {
        readOnly = isReadOnly();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.swt.utils.AbstractForm#addFields()
     */
    @Override
    protected void addFields() {
        addQueryConditionGroup();
        addSalesforcePreviewGroup();
        if (!isInWizard()) {
            Composite compositeBottomButton = Form.startNewGridLayout(this, 2, false, SWT.CENTER, SWT.CENTER);
            cancelButton = new UtilsButton(compositeBottomButton, Messages.getString("CommonWizard.cancel"), WIDTH_BUTTON_PIXEL, //$NON-NLS-1$
                    HEIGHT_BUTTON_PIXEL);
        }
    }

    /**
     * DOC YeXiaowei Comment method "addSalesforcePreviewGroup".
     */
    private void addSalesforcePreviewGroup() {
        // Group previewGroup = Form.createGroup(this, 2, "Salesforce Preview");

        tabFolder = new CTabFolder(this, SWT.BORDER);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        previewTabItem = new CTabItem(tabFolder, SWT.BORDER);
        previewTabItem.setText(Messages.getString("SalesforceStep2Form.preview")); //$NON-NLS-1$
        outputTabItem = new CTabItem(tabFolder, SWT.BORDER);
        outputTabItem.setText(Messages.getString("SalesforceStep2Form.output")); //$NON-NLS-1$

        Composite previewComposite = Form.startNewGridLayout(tabFolder, 1);
        outputComposite = Form.startNewGridLayout(tabFolder, 1);

        previewButton = new Button(previewComposite, SWT.NONE);
        previewButton.setText(Messages.getString("FileStep2.refreshPreview")); //$NON-NLS-1$
        previewButton.setSize(WIDTH_BUTTON_PIXEL, HEIGHT_BUTTON_PIXEL);

        previewInformationLabel = new Label(previewComposite, SWT.NONE);
        previewInformationLabel
                .setText("                                                                                                                        "); //$NON-NLS-1$
        previewInformationLabel.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));

        salesforcePreviewProcess = new ShadowProcessPreview(previewComposite, null, 600, 200);
        salesforcePreviewProcess.newTablePreview();

        previewTabItem.setControl(previewComposite);
        outputTabItem.setControl(outputComposite);
        tabFolder.setSelection(previewTabItem);
        tabFolder.pack();

    }

    /**
     * DOC YeXiaowei Comment method "addQueryConditionGroup".
     */
    private void addQueryConditionGroup() {
        Group queryConditionGroup = Form.createGroup(this, 2, Messages.getString("SalesforceStep2Form.queryCondition")); //$NON-NLS-1$

        queryConditionText = new LabelledText(queryConditionGroup, "Query Condition", true); //$NON-NLS-1$
        queryConditionText.setText(defaultQueryString);

        Composite moduleViewerComposite = new Composite(queryConditionGroup, SWT.NONE);

        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        moduleViewerComposite.setLayoutData(data);

        moduleViewerComposite.setLayout(new GridLayout(2, true));

        Label label = new Label(moduleViewerComposite, SWT.NONE);
        label.setText(Messages.getString("SalesforceStep2Form.saleforceDetail")); //$NON-NLS-1$
        label.setLayoutData(new GridData(GridData.FILL | GridData.BEGINNING));

        alphabet = new Button(moduleViewerComposite, SWT.CHECK);
        alphabet.setText(Messages.getString("SalesforceStep2Form.orderTheFields")); //$NON-NLS-1$
        alphabet.setLayoutData(new GridData(GridData.CENTER));

        createModuleDetailViewer(moduleViewerComposite);
    }

    /**
     * DOC YeXiaowei Comment method "readAndSetModuleDetailContent".
     */
    private void readAndSetModuleDetailContent() {

        metadataTableOrder = readMetadataDetail();
        if (metadataTableOrder != null) {
            metadataTableClone = metadataTableOrder.clone();

            metadataTableOrder = modifyMetadataTable();
            if (useAlphbet) {
                List<IMetadataColumn> listColumns = metadataTableOrder.getListColumns();
                if (listColumns != null) {
                    moduleViewer.setInput(listColumns.toArray());
                }
            } else {
                List<IMetadataColumn> listColumns = metadataTableClone.getListColumns();
                if (listColumns != null) {

                    moduleViewer.setInput(listColumns.toArray());
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.swt.utils.AbstractForm#addFieldsListeners()
     */
    @Override
    protected void addFieldsListeners() {
        queryConditionText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                if (!isContextMode()) {
                    if (checkFieldsValue()) {
                        getConnection().setQueryCondition(queryConditionText.getText());
                    }
                }
            }

        });

        alphabet.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                useAlphbet = alphabet.getSelection();
                getConnection().setUseAlphbet(useAlphbet);

                Object input = moduleViewer.getInput();
                if (input instanceof Object[]) {
                    if (useAlphbet) {
                        List<IMetadataColumn> listColumns = metadataTableOrder.getListColumns();
                        if (listColumns != null) {
                            moduleViewer.setInput(listColumns.toArray());
                        }
                    } else {
                        List<IMetadataColumn> listColumns = metadataTableClone.getListColumns();
                        if (listColumns != null) {
                            moduleViewer.setInput(listColumns.toArray());
                        }
                    }

                }
                moduleViewer.refresh();
            }

        });

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.swt.utils.AbstractForm#addUtilsButtonListeners()
     */
    @Override
    protected void addUtilsButtonListeners() {
        // Event PreviewButton
        previewButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                processor.execute();
            }
        });

        if (cancelButton != null) {
            // Event CancelButton
            cancelButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(final SelectionEvent e) {
                    getShell().close();
                }
            });
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.swt.utils.AbstractForm#checkFieldsValue()
     */
    @Override
    protected boolean checkFieldsValue() {
        previewInformationLabel.setText("   " + Messages.getString("FileStep2.settingsIncomplete")); //$NON-NLS-1$ //$NON-NLS-2$
        updateStatus(IStatus.OK, null);
        previewButton.setEnabled(false);
        previewInformationLabel.setText(""); //$NON-NLS-1$
        previewButton.setEnabled(true);

        updateStatus(IStatus.OK, null);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.swt.utils.AbstractForm#initialize()
     */
    @Override
    protected void initialize() {

        String queryCondition = getConnection().getQueryCondition();
        if (queryCondition != null && !queryCondition.equals("")) { //$NON-NLS-1$
            queryConditionText.setText(queryCondition);
        } else {
            queryConditionText.setText(""); //$NON-NLS-1$
        }
        useAlphbet = getConnection().isUseAlphbet();
        alphabet.setSelection(useAlphbet);

        checkFieldsValue();
    }

    @Override
    public void setVisible(boolean visible) {

        super.setVisible(visible);

        if (super.isVisible()) {
            if (!isContextMode()) {
                if ((!"".equals(getConnection().getWebServiceUrl())) && (getConnection().getModuleName() != null)) { //$NON-NLS-1$
                    readAndSetModuleDetailContent();
                    refreshPreview();
                }
            }

            if (isReadOnly() != readOnly) {
                adaptFormToReadOnly();
            }
            if (isContextMode()) {
                adaptFormToEditable();
            }
        }
    }

    /**
     * DOC YeXiaowei Comment method "refreshPreview".
     */
    private void refreshPreview() {
        processor.execute();
    }

    /**
     * 
     * DOC YeXiaowei SalesforceStep2Form class global comment. Detailled comment <br/>
     * 
     */
    class PreviewProcessor extends SWTUIThreadProcessor {

        String previewInformationLabelMsg = null;

        CsvArray csvArray = null;

        ProcessDescription processDescription = null;

        boolean firstRowIsCatption = false;

        @Override
        public boolean preProcessStart() {
            previewButton.setText(Messages.getString("FileStep2.stop")); //$NON-NLS-1$

            clearPreview();
            String webServiceUrl = getConnection().getWebServiceUrl();
            SalesforceSchemaConnection originalValueConnection = null;
            if (isContextMode()) {
                boolean found = false;
                ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(getShell(), getConnection());
                if (contextType != null) {
                    if (getContextModeManager() != null) {
                        getContextModeManager().setSelectedContextType(contextType);
                        webServiceUrl = getContextModeManager().getOriginalValue(getConnection().getWebServiceUrl());
                        found = true;
                    }
                    originalValueConnection = OtherConnectionContextUtils.cloneOriginalValueSalesforceConnection(getConnection(),
                            contextType);
                }
                if (!found) {
                    webServiceUrl = null;
                }
            }

            if (webServiceUrl == null || webServiceUrl.equals("")) { //$NON-NLS-1$
                previewInformationLabel.setText(" Please reset Salesforce URL"); //$NON-NLS-1$ 
                return false;
            }

            if (!checkFieldsValue()) {
                previewInformationLabel.setText("   " + Messages.getString("FileStep2.settingsIncomplete")); //$NON-NLS-1$ //$NON-NLS-2$
                return false;
            }

            previewInformationLabel.setText("   " + Messages.getString("FileStep2.previewProgress")); //$NON-NLS-1$ //$NON-NLS-2$
            firstRowIsCatption = false;
            if (originalValueConnection == null) {
                originalValueConnection = getConnection();
            }
            processDescription = getProcessDescription(originalValueConnection);
            return true;
        }

        @Override
        public void nonUIProcessInThread() {
            // get the XmlArray width an adapt ProcessDescription
            try {
                List<IMetadataTable> schema = processDescription.getSchema();
                if (schema != null && schema.size() > 0) {
                    if (useAlphbet) {
                        if (metadataTableOrder == null) {
                            metadataTableOrder = schema.get(0);
                            metadataTableOrder = modifyMetadataTable();
                        }
                        if (metadataTableOrder != null) {
                            schema.get(0).setListColumns(metadataTableOrder.getListColumns());
                        }
                    } else {
                        if (metadataTableClone == null) {
                            metadataTableClone = schema.get(0);
                        }
                        if (metadataTableClone != null) {
                            schema.get(0).setListColumns(metadataTableClone.getListColumns());
                        }
                    }
                }

                csvArray = ShadowProcessHelper.getCsvArray(processDescription, "SALESFORCE_SCHEMA", true); //$NON-NLS-1$
                if (csvArray == null) {
                    previewInformationLabelMsg = "   " + Messages.getString("FileStep2.previewFailure"); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    previewInformationLabelMsg = "   " + Messages.getString("FileStep2.previewIsDone"); //$NON-NLS-1$ //$NON-NLS-2$

                    // refresh TablePreview on this step
                    previewInformationLabelMsg = ""; //$NON-NLS-1$
                }
            } catch (Exception ex) {
                setException(ex);
                previewInformationLabelMsg = "   " + Messages.getString("FileStep2.previewFailure"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        @Override
        public void updateUIInThreadIfThreadIsCanceled() {
            if (!previewInformationLabel.isDisposed()) {
                previewInformationLabel.setText(""); //$NON-NLS-1$
            }
        }

        @Override
        public void updateUIInThreadIfThreadIsNotCanceled() {
            if (previewInformationLabel.isDisposed()) {
                return;
            }
            previewInformationLabel.setText(previewInformationLabelMsg);
            if (getException() != null) {

                // new ErrorDialogWidthDetailArea(getShell(), PID,
                // Messages.getString("FileStep2.previewFailure"), getException().getMessage()); //$NON-NLS-1$

                previewInformationLabel.setText("   " + Messages.getString("FileStep2.previewFailure")); //$NON-NLS-1$ //$NON-NLS-2$
                Display.getDefault().asyncExec(new Runnable() {

                    public void run() {
                        handleErrorOutput(outputComposite, tabFolder, outputTabItem);
                    }
                });
                return;

            }
            if (csvArray != null) {
                salesforcePreviewProcess.refreshTablePreview(csvArray, firstRowIsCatption, processDescription);
            }
        }

        @Override
        public void updateUIInThreadIfThreadFinally() {
            if (!previewButton.isDisposed()) {
                previewButton.setText(Messages.getString("FileStep2.refreshPreview")); //$NON-NLS-1$
                previewButton.setEnabled(true);

            }
        }

        @Override
        public void postProcessCancle() {
            previewButton.setEnabled(false);
        }
    }

    void clearPreview() {
        salesforcePreviewProcess.clearTablePreview();
    }

    /**
     * DOC YeXiaowei Comment method "getProcessDescription".
     * 
     * @return
     */
    private ProcessDescription getProcessDescription(SalesforceSchemaConnection originalValueConnection) {

        ProcessDescription processDescription = ShadowProcessHelper.getProcessDescription(originalValueConnection);

        SalesforceSchemaBean bean = new SalesforceSchemaBean();
        bean.setWebServerUrl(originalValueConnection.getWebServiceUrl());
        bean.setUserName(originalValueConnection.getUserName());
        bean.setPassword(originalValueConnection.getPassword());
        bean.setModuleName(originalValueConnection.getModuleName());
        bean.setQueryCondition(originalValueConnection.getQueryCondition());
        bean.setUseCustomModule(originalValueConnection.isUseCustomModuleName());
        bean.setBatchSize(originalValueConnection.getBatchSize());
        bean.setUseProxy(originalValueConnection.isUseProxy());
        bean.setUesHttp(originalValueConnection.isUseHttpProxy());
        bean.setProxyHost(originalValueConnection.getProxyHost());
        bean.setProxyPort(originalValueConnection.getProxyPort());
        bean.setProxyUsername(originalValueConnection.getProxyUsername());
        bean.setProxyPassword(originalValueConnection.getProxyPassword());
        try {
            bean.setTimeOut(Integer.parseInt(originalValueConnection.getTimeOut()));
        } catch (NumberFormatException e) {
            // use default
        }
        processDescription.setSalesforceSchemaBean(bean);

        IMetadataTable tableGet = getMetadatasForSalesforce(bean.getWebServerUrl(), bean.getUserName(), bean.getPassword(),
                String.valueOf(bean.getTimeOut()), bean.getModuleName(), bean.getBatchSize(), bean.isUseProxy(),
                bean.isUesHttp(), bean.getProxyHost(), bean.getProxyPort(), bean.getProxyUsername(), bean.getProxyPassword(),
                false);

        List<IMetadataTable> tableSchema = new ArrayList<IMetadataTable>();
        IMetadataTable table = new MetadataTable();
        List<IMetadataColumn> schema = new ArrayList<IMetadataColumn>();

        for (IMetadataColumn column : tableGet.getListColumns()) {
            schema.add(column.clone());
        }

        table.setTableName("tSalesforceInput"); //$NON-NLS-1$
        table.setListColumns(schema);
        tableSchema.add(table);

        processDescription.setSchema(tableSchema);

        processDescription.setEncoding(TalendTextUtils.addQuotes("ISO-8859-15")); //$NON-NLS-1$  
        if (tableGet != null) {
            moduleViewer.getTable().clearAll();
            if (useAlphbet) {
                if (metadataTableOrder == null) {
                    List<IMetadataTable> schema2 = processDescription.getSchema();
                    if (schema2 != null && schema2.size() > 0) {
                        metadataTableOrder = schema2.get(0);
                        metadataTableOrder = modifyMetadataTable();
                    }
                }
                if (metadataTableOrder != null) {
                    tableGet.setListColumns(metadataTableOrder.getListColumns());
                }
            } else {
                if (metadataTableClone == null) {
                    List<IMetadataTable> schema2 = processDescription.getSchema();
                    if (schema2 != null && schema2.size() > 0) {
                        metadataTableClone = schema2.get(0);
                    }
                }
                if (metadataTableClone != null) {
                    tableGet.setListColumns(metadataTableClone.getListColumns());
                }
            }
            moduleViewer.setInput(tableGet.getListColumns().toArray());
            moduleViewer.refresh();
        }

        return processDescription;
    }

    /**
     * DOC YeXiaowei Comment method "createModuleDetailViewer".
     * 
     * @param moduleGroup
     */
    private void createModuleDetailViewer(Composite moduleGroup) {
        moduleViewer = new TableViewer(moduleGroup, SWT.FILL | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);

        moduleViewer.getTable().setHeaderVisible(true);
        moduleViewer.getTable().setLinesVisible(true);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = 2;
        moduleViewer.getTable().setLayoutData(gridData);

        moduleViewer.setContentProvider(new IStructuredContentProvider() {

            public void dispose() {

            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

            }

            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof Object[]) {
                    return (Object[]) inputElement;
                }
                return null;
            }

        });

        moduleViewer.setLabelProvider(new ITableLabelProvider() {

            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }

            public String getColumnText(Object element, int columnIndex) {
                if (element instanceof IMetadataColumn) {
                    IMetadataColumn metadataColumn = (IMetadataColumn) element;
                    String title = null;
                    switch (columnIndex) {
                    case 0:
                        title = metadataColumn.getLabel();
                        break;
                    case 1:
                        title = metadataColumn.isKey() ? "true" : "false"; //$NON-NLS-1$ //$NON-NLS-2$
                        break;
                    case 2:
                        String talendType = metadataColumn.getTalendType();
                        JavaType javaTypeFromId = JavaTypesManager.getJavaTypeFromId(talendType);
                        if (javaTypeFromId != null) {
                            title = javaTypeFromId.getLabel();
                        }
                        break;
                    case 3:
                        title = metadataColumn.isNullable() ? "true" : "false"; //$NON-NLS-1$ //$NON-NLS-2$
                        break;
                    case 4:
                        title = metadataColumn.getPattern();
                        break;
                    case 5:
                        title = getStringFromInt(metadataColumn.getLength());
                        break;
                    case 6:
                        title = getStringFromInt(metadataColumn.getPrecision());
                        break;
                    case 7:
                        title = metadataColumn.getDefault();
                        break;
                    case 8:
                        title = metadataColumn.getComment();
                        break;
                    default:
                        title = Messages.getString("SalesforceStep2Form.otherTitle"); //$NON-NLS-1$
                    }

                    return title;
                }
                return null;
            }

            private String getStringFromInt(int x) {
                try {
                    return Integer.toString(x);
                } catch (Error e) {
                    return ""; //$NON-NLS-1$
                }
            }

            public void addListener(ILabelProviderListener listener) {

            }

            public void dispose() {

            }

            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            public void removeListener(ILabelProviderListener listener) {

            }

        });

        String[] titles = new String[] { "Column", "Key", "Type", "Nullable", "Data Pattern", "Length", "Precision", "Default", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
                "Comment" }; //$NON-NLS-1$

        for (String title : titles) {
            int width = COLUMN_WIDTH;
            if (title.equals("Column") || title.equals("Data Pattern") || title.equals("Comment")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                width = COLUMN_WIDTH * 2;
            }
            createTableColumn(title, width);
        }

        // readAndSetModuleDetailContent();
    }

    private void createTableColumn(String title, int width) {
        TableColumn column = new TableColumn(moduleViewer.getTable(), SWT.NONE);
        column.setText(title);
        if (width < COLUMN_WIDTH || width > 400) {
            column.setWidth(COLUMN_WIDTH);
        } else {
            column.setWidth(width);
        }
    }

    @Override
    protected void adaptFormToEditable() {
        super.adaptFormToEditable();
        queryConditionText.setEditable(!isContextMode());
    }

    @Override
    protected void exportAsContext() {
        super.exportAsContext();
        if (getContextModeManager() != null) {
            getContextModeManager().setDefaultContextType(getConnection());
        }
    }

    @Override
    protected void processWhenDispose() {
        if (processor != null) {
            processor.forceStop();
        }
    }
}
