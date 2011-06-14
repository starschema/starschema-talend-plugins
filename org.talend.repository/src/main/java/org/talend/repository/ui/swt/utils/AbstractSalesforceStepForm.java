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
package org.talend.repository.ui.swt.utils;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.core.CorePlugin;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataContextModeManager;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.SalesforceSchemaConnection;
import org.talend.core.model.process.AbstractNode;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.wizards.metadata.connection.files.salesforce.ISalesforceModuleParser;
import org.talend.repository.ui.wizards.metadata.connection.files.salesforce.SalesforceModuleParseAPI;
import org.talend.repository.ui.wizards.metadata.connection.files.salesforce.SalesforceModuleParserPartner;

import com.salesforce.soap.partner.DescribeSObjectsResponse;
import com.salesforce.soap.partner.InvalidSObjectFault;
import com.salesforce.soap.partner.SforceServiceStub;
import com.sforce.soap.enterprise.DescribeGlobalResult;
import com.sforce.soap.enterprise.SoapBindingStub;
import com.sforce.soap.enterprise.fault.UnexpectedErrorFault;

/**
 * DOC YeXiaowei class global comment. Detailled comment <br/>
 * 
 */
public abstract class AbstractSalesforceStepForm extends AbstractForm {

    protected int maximumRowsToPreview = CorePlugin.getDefault().getPreferenceStore().getInt(
            ITalendCorePrefConstants.PREVIEW_LIMIT);

    protected SalesforceSchemaConnection connection;

    protected AbstractNode fakeSalesforceNode = null;

    private final String tSalesforceUniqueName = "tSalesforceInput"; //$NON-NLS-1$

    private SalesforceModuleParseAPI salesforceAPI = null;

    private IMetadataContextModeManager contextModeManager;

    private SoapBindingStub binding = null;

    // private com.salesforce.soap.partner.SoapBindingStub bindingPartner = null;
    private SforceServiceStub bindingPartner = null;

    public static final String TSALESFORCE_INPUT_URL = "https://www.salesforce.com/services/Soap/u/19.0"; //$NON-NLS-1$

    public static final String TSALESFORCE_PARTNER_INPUT_URL = "https://test.salesforce.com/services/Soap/u/10.0"; //$NON-NLS-1$

    // note that tSalesforceInput use a different url, if the web service is called by wizard we should use
    // DEFAULT_WEB_SERVICE_URL, if the web service is called by tSalesforceInput we should use TSALESFORCE_INPUT_URL
    public static final String DEFAULT_WEB_SERVICE_URL = "https://www.salesforce.com/services/Soap/u/8.0"; //$NON-NLS-1$

    public static final String DEFAULT_WEB_SERVICE_FOR_SOQL_URL = "https://www.salesforce.com/services/Soap/c/8.0"; //$NON-NLS-1$

    public static final String TSALESFORCE_CUSTOM_MODULE = "org.talend.salesforce.custom.module"; //$NON-NLS-1$

    public static final String TSALESFORCE_CUSTOM_MODULE_SPILT = ","; //$NON-NLS-1$

    public boolean useAlphbet;

    public IMetadataTable metadataTableOrder;

    public IMetadataTable metadataTableClone;

    public AbstractSalesforceStepForm(Composite parent, ConnectionItem connectionItem, String[] existingNames,
            SalesforceModuleParseAPI salesforceAPI) {
        super(parent, SWT.NONE, existingNames);
        setConnectionItem(connectionItem);
        this.salesforceAPI = salesforceAPI;
    }

    public AbstractSalesforceStepForm(Composite parent, ConnectionItem connectionItem, SalesforceModuleParseAPI salesforceAPI) {
        this(parent, connectionItem, null, salesforceAPI);
    }

    public AbstractSalesforceStepForm(Composite parent, ConnectionItem connectionItem, MetadataTable metadataTable,
            String[] existingNames, SalesforceModuleParseAPI salesforceAPI) {
        super(parent, SWT.NONE, existingNames);
        setConnectionItem(connectionItem);
        this.salesforceAPI = salesforceAPI;
    }

    protected SalesforceSchemaConnection getConnection() {
        return (SalesforceSchemaConnection) connectionItem.getConnection();
    }

    public boolean isPerlProject() {
        ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();
        return (codeLanguage == ECodeLanguage.PERL);
    }

    /**
     * 
     * DOC YeXiaowei Comment method "getSalesforceComponent".
     * 
     * @return Always not null
     */
    public INode getSalesforceNode() {
        return RepositoryPlugin.getDefault().getDesignerCoreService().getRefrenceNode(tSalesforceUniqueName);
    }

    public IMetadataTable getMetadatasForSalesforce(String endPoint, String user, String pass, String timeOut, String moduleName,
            String betchSize, boolean useProxy, boolean useHttp, String proxyHost, String proxyPort, String proxyUsername,
            String proxyPassword, boolean update) {

        IMetadataTable result = null;
        String proxy = null;
        if (useProxy) {
            proxy = SalesforceModuleParseAPI.USE_SOCKS_PROXY;//$NON-NLS-1$
        } else if (useHttp) {
            proxy = SalesforceModuleParseAPI.USE_HTTP_PROXY;//$NON-NLS-1$
        }
        if (!moduleName.equals(salesforceAPI.getCurrentModuleName())) {
            result = getMetadataTableBySalesforceServerAPI(endPoint, user, pass, timeOut, moduleName, proxy, proxyHost,
                    proxyPort, proxyUsername, proxyPassword);
            if (result == null) {
                result = getMetadataTableFromConfigFile(moduleName);
            }
            return result;
        } else {
            if (update) {
                result = getMetadataTableBySalesforceServerAPI(endPoint, user, pass, timeOut, moduleName, proxy, proxyHost,
                        proxyPort, proxyUsername, proxyPassword);
                if (result == null) {
                    result = getMetadataTableFromConfigFile(moduleName);
                }
                return result;
            } else {
                IMetadataTable metadataTable = new org.talend.core.model.metadata.MetadataTable();
                metadataTable.setListColumns(salesforceAPI.getCurrentMetadataColumns());
                return metadataTable;
            }
        }
    }

    private IMetadataTable getMetadataTableBySalesforceServerAPI(final String endPoint, final String user, final String pass,
            final String timeOut, final String moduleName, final String proxy, final String proxyHost, final String proxyPort,
            final String proxyUsername, final String proxyPassword) {
        IMetadataTable metadataTable = new org.talend.core.model.metadata.MetadataTable();

        if (user == null || pass == null || user.equals("") || pass.equals("") || moduleName == null || moduleName.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return null;
        }

        ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
        try {
            dialog.run(true, false, new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

                    monitor.beginTask(Messages.getString("AbstractSalesforceStepForm.fetchModule", moduleName), //$NON-NLS-1$
                            IProgressMonitor.UNKNOWN);
                    boolean socksProxy = false;
                    boolean httpProxy = false;
                    boolean httpsProxy = false;
                    if (SalesforceModuleParseAPI.USE_SOCKS_PROXY.equals(proxy)) {
                        socksProxy = true;
                    }
                    if (SalesforceModuleParseAPI.USE_HTTP_PROXY.equals(proxy)) {
                        if (endPoint.startsWith("https")) {
                            httpsProxy = true;
                        } else {
                            httpProxy = true;
                        }

                    }
                    salesforceAPI.resetProxy(httpProxy, socksProxy, httpsProxy);
                    salesforceAPI.setProxy(proxyHost, proxyPort, proxyUsername, proxyPassword, httpProxy, socksProxy, httpsProxy);
                    if (!salesforceAPI.isLogin()) {
                        try {

                            ArrayList loginList = salesforceAPI.login(endPoint, user, pass, timeOut);
                            for (int i = 0; i < loginList.size(); i++) {
                                if (loginList.get(i) instanceof SoapBindingStub) {
                                    binding = (SoapBindingStub) loginList.get(i);
                                } else {
                                    bindingPartner = new SforceServiceStub(endPoint);
                                }
                            }
                        } catch (Throwable e) {
                            ExceptionHandler.process(e);
                        }
                    }
                    salesforceAPI.fetchMetaDataColumns(moduleName);
                    salesforceAPI.resetProxy(httpProxy, socksProxy, httpsProxy);
                    monitor.done();
                }
            });
        } catch (InvocationTargetException e1) {
            ExceptionHandler.process(e1);
        } catch (InterruptedException e2) {
            ExceptionHandler.process(e2);
        }

        if (salesforceAPI.getCurrentMetadataColumns() == null) {
            return null;
        }

        metadataTable.setListColumns(salesforceAPI.getCurrentMetadataColumns());
        return metadataTable;
    }

    protected SalesforceModuleParseAPI checkSalesfoceLogin(final String proxy, final String endPoint, final String username,
            final String password, final String timeOut, final String proxyHost, final String proxyPort,
            final String proxyUsername, final String proxyPassword) {
        final List<String> errors = new ArrayList<String>();

        salesforceAPI.setLogin(false);

        ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
        try {
            dialog.run(true, false, new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

                    monitor.beginTask(Messages.getString("AbstractSalesforceStepForm.tryToLogin"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$

                    if (salesforceAPI == null) {
                        try {
                            salesforceAPI = new SalesforceModuleParseAPI();
                        } catch (Throwable e) {
                            ExceptionHandler.process(e);
                        }
                    }
                    boolean socksProxy = false;
                    boolean httpProxy = false;
                    boolean httpsProxy = false;
                    if (SalesforceModuleParseAPI.USE_SOCKS_PROXY.equals(proxy)) {
                        socksProxy = true;
                    }
                    if (SalesforceModuleParseAPI.USE_HTTP_PROXY.equals(proxy)) {
                        if (endPoint.startsWith("https")) {
                            httpsProxy = true;
                        } else {
                            httpProxy = true;
                        }
                    }
                    salesforceAPI.resetProxy(httpProxy, socksProxy, httpsProxy);

                    salesforceAPI.setProxy(proxyHost, proxyPort, proxyUsername, proxyPassword, httpProxy, socksProxy, httpsProxy);
                    try {
                        // binding ;
                        ArrayList loginList = salesforceAPI.login(endPoint, username, password, timeOut);
                        if (loginList != null) {
                            for (int i = 0; i < loginList.size(); i++) {
                                if (loginList.get(i) instanceof SoapBindingStub) {
                                    binding = (SoapBindingStub) loginList.get(i);
                                }
                                if (loginList.get(i) instanceof SforceServiceStub) {
                                    bindingPartner = (SforceServiceStub) loginList.get(i);
                                }

                            }

                        }

                        salesforceAPI.setLogin(true);
                    } catch (Throwable e) {
                        errors.add(e.getMessage());
                        ExceptionHandler.process(e);
                    } finally {
                        salesforceAPI.resetProxy(httpProxy, socksProxy, httpsProxy);
                    }
                    monitor.done();
                }
            });
        } catch (InvocationTargetException e1) {
            ExceptionHandler.process(e1);
        } catch (InterruptedException e2) {
            ExceptionHandler.process(e2);
        }

        if (salesforceAPI.isLogin()) {
            MessageDialog.openInformation(getShell(), Messages.getString("SalesforceForm.checkConnectionTitle"), //$NON-NLS-1$ 
                    Messages.getString("SalesforceForm.checkIsDone")); //$NON-NLS-1$ 
        } else {
            String mainMsg = Messages.getString("SalesforceForm.checkFailure") + " " //$NON-NLS-1$ //$NON-NLS-2$
                    + Messages.getString("SalesforceForm.checkFailureTip"); //$NON-NLS-1$
            String error = errors.size() > 0 ? errors.get(0) : ""; //$NON-NLS-1$
            new ErrorDialogWidthDetailArea(getShell(), PID, mainMsg, error);
        }

        return salesforceAPI;
    }

    protected DescribeGlobalResult describeGlobal() throws UnexpectedErrorFault, RemoteException {
        if (salesforceAPI.isLogin()) {
            if (binding != null) {
                return binding.describeGlobal();
            }
        }
        return null;
    }

    protected com.salesforce.soap.partner.DescribeGlobalResult describeGlobalPartner() throws RemoteException {
        if (salesforceAPI.isLogin()) {
            ISalesforceModuleParser currentAPI = salesforceAPI.getCurrentAPI();
            if (currentAPI instanceof SalesforceModuleParserPartner) {
                SalesforceModuleParserPartner partner = (SalesforceModuleParserPartner) currentAPI;
                try {
                    DescribeSObjectsResponse describeSObjects = bindingPartner.describeSObjects(null, null, null, null, null);
                    // return describeSObjects.getResult();
                } catch (InvalidSObjectFault e) {
                    e.printStackTrace();
                } catch (com.salesforce.soap.partner.UnexpectedErrorFault e) {
                    e.printStackTrace();
                }

            }
        }
        return null;
    }

    private IMetadataTable getMetadataTableFromConfigFile(String moduleName) {

        INode node = getSalesforceNode();

        IElementParameter currentModuleNameParam = node.getElementParameter("MODULENAME"); //$NON-NLS-1$
        currentModuleNameParam.setValue(moduleName);

        node.getComponent().createElementParameters(node);

        IElementParameter schemaParam = node.getElementParameter("SCHEMA"); //$NON-NLS-1$

        if (schemaParam == null) {
            return null;
        }

        schemaParam.setValueToDefault(node.getElementParameters()); // Call this method to recompute some parameters
        // value.

        IMetadataTable metadataTable = (IMetadataTable) schemaParam.getValue();

        return metadataTable;
    }

    /**
     * DOC zli Comment method "readMetadataDetail".
     */
    public IMetadataTable readMetadataDetail() {
        String moduleName = getConnection().getModuleName();

        if (moduleName == null || moduleName.equals("")) { //$NON-NLS-1$
            return null;
        }

        String webServiceUrl = getConnection().getWebServiceUrl();
        String userName = getConnection().getUserName();
        String password = getConnection().getPassword();
        String timeOut = getConnection().getTimeOut();
        // add for feature 7507
        String betchSize = getConnection().getBatchSize();
        boolean useProxy = getConnection().isUseProxy();
        boolean useHttp = getConnection().isUseHttpProxy();
        String proxyHost = getConnection().getProxyHost();
        String proxyPort = getConnection().getProxyPort();
        String proxyUsername = getConnection().getProxyUsername();
        String proxyPassword = getConnection().getProxyPassword();

        if (isContextMode() && getContextModeManager() != null) {
            webServiceUrl = getContextModeManager().getOriginalValue(webServiceUrl);
            userName = getContextModeManager().getOriginalValue(userName);
            password = getContextModeManager().getOriginalValue(password);
            timeOut = getContextModeManager().getOriginalValue(timeOut);
            betchSize = getContextModeManager().getOriginalValue(betchSize);
            useProxy = Boolean.valueOf(getContextModeManager().getOriginalValue(String.valueOf(useProxy)));
            useHttp = Boolean.valueOf(getContextModeManager().getOriginalValue(String.valueOf(useHttp)));
            proxyHost = getContextModeManager().getOriginalValue(proxyHost);
            proxyPort = getContextModeManager().getOriginalValue(proxyPort);
            proxyUsername = getContextModeManager().getOriginalValue(proxyUsername);
            proxyPassword = getContextModeManager().getOriginalValue(proxyPassword);
        }

        metadataTableOrder = getMetadatasForSalesforce(webServiceUrl, userName, password, timeOut, moduleName, betchSize,
                useProxy, useHttp, proxyHost, proxyPort, proxyUsername, proxyPassword, true);

        return metadataTableOrder;
    }

    /**
     * DOC zli Comment method "modifyMetadataTable".
     */
    public IMetadataTable modifyMetadataTable() {
        if (metadataTableOrder != null) {
            List<IMetadataColumn> listColumns = metadataTableOrder.getListColumns();
            if (listColumns != null) {

                Object[] array = listColumns.toArray();
                for (int i = 0; i < array.length; i++) {
                    for (int j = i + 1; j < array.length; j++) {

                        String labela = ((MetadataColumn) array[i]).getLabel();
                        String labelb = ((MetadataColumn) array[j]).getLabel();
                        if (labela.compareTo(labelb) > 0) {
                            MetadataColumn metadataColumn = (MetadataColumn) array[i];
                            array[i] = array[j];
                            array[j] = metadataColumn;
                        }
                    }
                }
                List<Object> asList = Arrays.asList(array);
                List<IMetadataColumn> aa = new ArrayList();
                if (asList != null && asList.size() > 0) {
                    Object object = asList.get(0);
                    if (object instanceof MetadataColumn) {
                        for (int i = 0; i < asList.size(); i++) {
                            aa.add(i, (MetadataColumn) asList.get(i));
                        }
                        metadataTableOrder.setListColumns(aa);
                    }
                }
            }
        }
        return metadataTableOrder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.swt.utils.AbstractForm#adaptFormToReadOnly()
     */
    @Override
    protected void adaptFormToReadOnly() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.swt.utils.AbstractForm#addFields()
     */
    @Override
    protected void addFields() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.swt.utils.AbstractForm#addFieldsListeners()
     */
    @Override
    protected void addFieldsListeners() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.swt.utils.AbstractForm#addUtilsButtonListeners()
     */
    @Override
    protected void addUtilsButtonListeners() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.swt.utils.AbstractForm#checkFieldsValue()
     */
    @Override
    protected boolean checkFieldsValue() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.swt.utils.AbstractForm#initialize()
     */
    @Override
    protected void initialize() {
        // TODO Auto-generated method stub
    }

    /**
     * Getter for salesforceAPI.
     * 
     * @return the salesforceAPI
     */
    public SalesforceModuleParseAPI getSalesforceAPI() {
        return this.salesforceAPI;
    }

    /**
     * Sets the salesforceAPI.
     * 
     * @param salesforceAPI the salesforceAPI to set
     */
    public void setSalesforceAPI(SalesforceModuleParseAPI salesforceAPI) {
        this.salesforceAPI = salesforceAPI;
    }

    public IMetadataContextModeManager getContextModeManager() {
        return this.contextModeManager;
    }

    public void setContextModeManager(IMetadataContextModeManager contextModeManager) {
        this.contextModeManager = contextModeManager;
    }
}
