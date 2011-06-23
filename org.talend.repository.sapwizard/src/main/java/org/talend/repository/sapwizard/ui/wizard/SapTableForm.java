/** 
 *    Copyright (C) 2011, Starschema Ltd. <info at starschema.net>
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 2 of the License, or
 *    any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package org.talend.repository.sapwizard.ui.wizard;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.commons.ui.swt.formtools.UtilsButton;
import org.talend.commons.utils.data.text.IndiceHelper;
import org.talend.core.model.metadata.MetadataTool;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.InputSAPFunctionParameterTable;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.OutputSAPFunctionParameterTable;
import org.talend.core.model.metadata.builder.connection.SAPConnection;
import org.talend.core.model.metadata.builder.connection.SAPFunctionParameterColumn;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.metadata.builder.connection.SAPTestInputParameterTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.sap.i18n.Messages;
import org.talend.repository.sapwizard.service.SapParameterTypeEnum;
import org.talend.repository.sapwizard.service.SapClientManager;
import org.talend.repository.sapwizard.service.SapTableDescription;
import org.talend.repository.sapwizard.service.SapUtil;

import com.sap.mw.jco.JCO;

/**
 * @author Ammu
 * 
 */
/**
 * @author Ammu
 * 
 */
public class SapTableForm extends BaseSAPForm {

	/**
	 * tableNameText
	 */
	private LabelledText tableNameText;
	/**
	 * directoryText
	 */
	private LabelledText directoryText;
	/**
	 * findButton
	 */
	private UtilsButton findButton;
	/**
	 * columnTableViewer
	 */
	private TableViewer columnTableViewer;

	/**
	 * readOnly
	 */
	private boolean readOnly;
	/**
	 * newTableList
	 */
	private List<MetadataTable> newTableList;

	/**
	 * functionUnit
	 */
	private SAPFunctionUnit functionUnit;
	/**
	 * manager
	 */
	private SapClientManager manager = null;
	/**
	 * selectedFunction
	 */
	private JCO.Function selectedFunction = null;
	/**
	 * inputParameterTable
	 */
	private OutputSAPFunctionParameterTable outputParameterTable;
	/**
	 * inputParameterTable
	 */
	private InputSAPFunctionParameterTable inputParameterTable;
	/**
	 * metadataTable
	 */
	private MetadataTable metadataTable;
	/**
	 * testInputParameterTable
	 */
	private SAPTestInputParameterTable testInputParameterTable;
	/**
	 * createOne
	 */
	private boolean createOne;
	/**
	 * initFunctionName
	 */
	private String initTableName = null;

	/**
	 * @param parent
	 * @param connectionItem
	 * @param functionUnit
	 * @param metadataTable
	 */
	public SapTableForm(Composite parent, ConnectionItem connectionItem, SAPFunctionUnit functionUnit,
			MetadataTable metadataTable) {
		super(parent, 0, null);
		this.connectionItem = connectionItem;
		this.functionUnit = functionUnit;
		this.metadataTable = metadataTable;
		this.newTableList = new ArrayList<MetadataTable>();
		if (functionUnit != null) {
			this.initTableName = functionUnit.getName();
		}
		setConnectionItem(connectionItem);
		setupForm(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.talend.repository.ui.swt.utils.AbstractForm#adaptFormToReadOnly()
	 */
	protected void adaptFormToReadOnly() {
		this.readOnly = isReadOnly();
		this.tableNameText.setReadOnly(isReadOnly());
		this.directoryText.setReadOnly(isReadOnly());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Control#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		updateFindButton();
		if (isContextMode()) {
			adaptFormToEditable();
		}
		if (isReadOnly() != this.readOnly) {
			adaptFormToReadOnly();
		}
	}

	/**
	 * 
	 */
	private void initJCOManager() {
		SAPConnection connection = getConnection();
		try {
			this.manager = new SapClientManager(connection.getClient(), connection.getHost(), connection.getUsername(),
					connection.getPassword(), connection.getLanguage(), connection.getSystemNumber());
		} catch (Throwable throwable) {
			openErrorDialogWithDetail(throwable);
			this.manager = null;
		}
	}

	/**
	 * 
	 */
	protected void addMetadataTable() {
		ProxyRepositoryFactory proxyRepositoryFactory = ProxyRepositoryFactory.getInstance();
		this.metadataTable = ConnectionFactory.eINSTANCE.createMetadataTable();
		getFunctionUnit().getTables().add(this.metadataTable);
		this.newTableList.add(this.metadataTable);
		this.metadataTable.setId(proxyRepositoryFactory.getNextId());
		this.metadataTable.setLabel(IndiceHelper.getIndexedLabel(this.metadataTable.getLabel(), this.existingNames));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.talend.repository.ui.swt.utils.AbstractForm#addFields()
	 */
	protected void addFields() {

		Composite parent = new Composite(this, SWT.NONE);
		parent.setLayout(new GridLayout(2, false));
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		parent.setLayoutData(gridData);

		this.tableNameText = new LabelledText(parent, Messages.getString("SapTableForm.Name"), true);
		this.directoryText = new LabelledText(parent, Messages.getString("SapTableForm.Directory"), true);

		addFindhButton(parent);

		Group group = Form.createGroup(parent, 1, Messages.getString("SapTableForm.GroupColumnsToDownload"), 1);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 2;
		gridData.verticalIndent = 20;
		group.setLayoutData(gridData);

		Composite columnTableComposite = Form.startNewDimensionnedGridLayout(group, 1, 400, 200);
		columnTableComposite.setLayout(new FillLayout());
		columnTableViewer = new TableViewer(columnTableComposite);
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(50, 75, true));
		columnTableViewer.getTable().setLayout(layout);
		columnTableViewer.setColumnProperties(new String[] { Messages.getString("SapTableForm.ColumnTable.Column1") });
		columnTableViewer.setContentProvider(new ArrayContentProvider());
		final Table table = columnTableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		final TableViewerColumn viewerColumn = new TableViewerColumn(columnTableViewer, SWT.NONE);
		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((SAPFunctionParameterColumn) element).getName();
			}
		});
		final TableColumn column = viewerColumn.getColumn();
		column.setText(Messages.getString("SapTableForm.ColumnTable.Column1"));
		column.setWidth(200);
		column.setResizable(true);
		column.setMoveable(true);
	}

	/**
	 * @param composite
	 */
	private void addFindhButton(Composite composite) {
		Composite localComposite = Form.startNewGridLayout(composite, 1, false, 16777216, 128);
		GridData localGridData = new GridData(768);
		localGridData.horizontalSpan = 2;
		localGridData.horizontalAlignment = 16777216;
		localComposite.setLayoutData(localGridData);
		GridLayout localGridLayout = (GridLayout) localComposite.getLayout();
		localGridLayout.marginHeight = 0;
		localGridLayout.marginTop = 0;
		localGridLayout.marginBottom = 0;
		this.findButton = new UtilsButton(localComposite, Messages.getString("SapTableForm.FindButton"), 100, 30);
		this.findButton.setEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.talend.repository.ui.swt.utils.AbstractForm#addUtilsButtonListeners()
	 */
	protected void addUtilsButtonListeners() {
		this.findButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				searchFunction();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.talend.repository.ui.swt.utils.AbstractForm#addFieldsListeners()
	 */
	@Override
	protected void addFieldsListeners() {
		this.tableNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent modifyEvent) {
				checkFieldsValue();
			}
		});
		this.directoryText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent modifyEvent) {
				checkFieldsValue();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.talend.repository.ui.swt.utils.AbstractForm#checkFieldsValue()
	 */
	protected boolean checkFieldsValue() {
		if (isContextMode()) {
			return true;
		}
		if (this.tableNameText.getCharCount() == 0) {
			updateStatus(4, Messages.getString("SapForm.Alert", new Object[] { this.tableNameText.getLabelText() }));
			return false;
		}
		updateFindButton();
		updateStatus(0, null);
		return true;
	}

	/**
	 * 
	 */
	private void updateFindButton() {
		if (isContextMode()) {
			this.findButton.setEnabled(true);
		} else {
			boolean bool = (this.tableNameText.getCharCount() != 0);
			this.findButton.setEnabled(bool);
		}
	}

	/**
	 * @param jcoFunctionDescription
	 */
	private void validatingFunction(SapTableDescription jcoFunctionDescription) {
		if (this.manager == null) {
			initJCOManager();
		}
		if (this.manager != null) {
			String tableName = jcoFunctionDescription.getName();
			JCO.Function function = null;
			try {
				function = this.manager.getTableByName(tableName);
				if (function == null) {
					MessageDialog.openWarning(getShell(), "SAP", Messages.getString("SapTableForm.NoTableName")
							+ tableName);
				} else if (this.selectedFunction == null) {
					this.selectedFunction = function;
					setFunctionUnitParametersAndMetadata(false);
				} else if (!this.selectedFunction.equals(function)) {
					this.selectedFunction = function;
					setFunctionUnitParametersAndMetadata(true);
				}
				this.selectedFunction = function;
				if (this.selectedFunction != null) {
					getConnection().setCurrentFucntion(this.functionUnit.getName());
				}
			} catch (Exception exception) {
				MessageDialog.openWarning(getShell(), "SAP", exception.getMessage());
			}
		}
		checkFieldsValue();
	}

	/**
	 * @param isFunctionValid
	 */
	private void setFunctionUnitParametersAndMetadata(boolean isFunctionValid) {
		SAPConnection localSAPConnection = getConnection();
		ProxyRepositoryFactory localProxyRepositoryFactory = ProxyRepositoryFactory.getInstance();
		createFunctionParametersAndMetadata(localSAPConnection, localProxyRepositoryFactory, isFunctionValid);
	}

	private void createFunctionParametersAndMetadata(SAPConnection sapConnection,
			IProxyRepositoryFactory proxyRepositoryFactory, boolean isFunctionValid) {
		SAPFunctionUnit functionUnit = null;
		JCO.Function function = this.selectedFunction;
		if (functionUnit != null) {
			this.functionUnit = functionUnit;
			this.inputParameterTable = functionUnit.getInputParameterTable();
			this.outputParameterTable = functionUnit.getOutputParameterTable();
			this.metadataTable = functionUnit.getMetadataTable();
			this.testInputParameterTable = functionUnit.getTestInputParameterTable();
		} else if (this.functionUnit == null) {
			createFunctionAndAdd(sapConnection, proxyRepositoryFactory, function);
		} else if ((this.initTableName != null) && (!this.initTableName.equals(function.getName()))
				&& (!this.createOne)) {
			createFunctionAndAdd(sapConnection, proxyRepositoryFactory, function);
		} else if (isFunctionValid) {
			this.functionUnit.setName(this.selectedFunction.getName());
			CharArrayWriter localCharArrayWriter = new CharArrayWriter();
			try {
				this.selectedFunction.writeHTML(localCharArrayWriter);
			} catch (IOException ioException) {
				ExceptionHandler.process(ioException);
			}

			this.inputParameterTable = this.functionUnit.getInputParameterTable();
			this.inputParameterTable.setLabel(this.functionUnit.getName());
			this.inputParameterTable.getColumns().clear();
			SapUtil.createParamsForFunction(this.inputParameterTable, function, 0);

			this.outputParameterTable = this.functionUnit.getOutputParameterTable();
			this.outputParameterTable.setLabel(this.functionUnit.getName());
			this.outputParameterTable.getColumns().clear();
			SapUtil.createParamsForFunction(this.outputParameterTable, function, 1);

			this.metadataTable = this.functionUnit.getMetadataTable();
			this.metadataTable.setLabel(this.functionUnit.getName());

			this.testInputParameterTable = ConnectionFactory.eINSTANCE.createSAPTestInputParameterTable();
			this.testInputParameterTable.setFunctionUnit(this.functionUnit);
			this.testInputParameterTable.setLabel(function.getName());
			SapUtil.createParamsForFunction(this.testInputParameterTable, function, 0);
		} else {
			if (this.functionUnit.getTestInputParameterTable() == null) {
				this.testInputParameterTable = ConnectionFactory.eINSTANCE.createSAPTestInputParameterTable();
				this.testInputParameterTable.setFunctionUnit(this.functionUnit);
				this.testInputParameterTable.setId(proxyRepositoryFactory.getNextId());
				this.testInputParameterTable.setLabel(function.getName());
				SapUtil.createParamsForFunction(this.testInputParameterTable, function, 0);
				this.functionUnit.setTestInputParameterTable(this.testInputParameterTable);
			}
		}
	}

	/**
	 * @param sapConnection
	 * @param proxyRepositoryFactory
	 * @param function
	 * @return
	 */
	private SAPFunctionUnit createFunctionAndAdd(SAPConnection sapConnection,
			IProxyRepositoryFactory proxyRepositoryFactory, JCO.Function function) {
		this.functionUnit = ConnectionFactory.eINSTANCE.createSAPFunctionUnit();
		this.functionUnit.setName(function.getName());
		this.functionUnit.setOutputType(SapParameterTypeEnum.OUTPUT_SINGLE.getDisplayLabel());
		this.functionUnit.setConnection(sapConnection);
		this.functionUnit.setId(proxyRepositoryFactory.getNextId());
		CharArrayWriter charArrayWriter = new CharArrayWriter();
		try {
			function.writeHTML(charArrayWriter);
		} catch (IOException ioException) {
			ExceptionHandler.process(ioException);
		}
		// New Input parameter table
		this.inputParameterTable = ConnectionFactory.eINSTANCE.createInputSAPFunctionParameterTable();
		this.inputParameterTable.setFunctionUnit(this.functionUnit);
		this.inputParameterTable.setId(proxyRepositoryFactory.getNextId());
		this.inputParameterTable.setLabel(this.functionUnit.getName());
		SapUtil.createParamsForFunction(this.inputParameterTable, function, 0);

		// New out parameter table
		this.outputParameterTable = ConnectionFactory.eINSTANCE.createOutputSAPFunctionParameterTable();
		this.outputParameterTable.setFunctionUnit(this.functionUnit);
		this.outputParameterTable.setId(proxyRepositoryFactory.getNextId());
		this.outputParameterTable.setLabel(this.functionUnit.getName());
		SapUtil.createParamsForFunction(this.outputParameterTable, function, 1);

		// New Test parameter table
		this.testInputParameterTable = ConnectionFactory.eINSTANCE.createSAPTestInputParameterTable();
		this.testInputParameterTable.setFunctionUnit(this.functionUnit);
		this.testInputParameterTable.setId(proxyRepositoryFactory.getNextId());
		this.testInputParameterTable.setLabel(function.getName());
		SapUtil.createParamsForFunction(this.testInputParameterTable, function, 0);

		// New Metadata table
		this.metadataTable = ConnectionFactory.eINSTANCE.createMetadataTable();
		this.metadataTable.setId(proxyRepositoryFactory.getNextId());
		this.metadataTable.setLabel(function.getName());
		// FIXME
		// this.metadataTable.setConnection(paramSAPConnection);
		this.functionUnit.setInputParameterTable(this.inputParameterTable);
		this.functionUnit.setOutputParameterTable(this.outputParameterTable);
		this.functionUnit.setMetadataTable(this.metadataTable);
		this.functionUnit.setTestInputParameterTable(this.testInputParameterTable);
		sapConnection.getFuntions().add(this.functionUnit);
		this.createOne = true;
		return this.functionUnit;
	}

	/**
	 * 
	 */
	public void performCancel() {
		processWhenDispose();
		cleanModel();
	}

	/**
	 * 
	 */
	private void cleanModel() {
		getFunctionUnit().getTables().removeAll(this.newTableList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.talend.repository.ui.swt.utils.AbstractForm#initialize()
	 */
	protected void initialize() {
		if (this.functionUnit != null) {
			if (metadataTable == null) {
				metadataTable = functionUnit.getMetadataTable();
			}
			String str = MetadataTool.validateValue(this.metadataTable.getLabel());
			this.tableNameText.setText(str);
			this.directoryText.setText(this.metadataTable.getComment());
			this.tableNameText.forceFocus();
			setColumnTableInput();
		}
		updateStatus(2, "");
	}

	/**
	 * @return
	 */
	protected SAPFunctionUnit getFunctionUnit() {
		return this.functionUnit;
	}

	/**
	 * 
	 */
	private void searchFunction() {
		if (this.manager == null) {
			initJCOManager();
		}
		if (this.manager != null && getStatusLevel() == Status.OK) {
			String tableName = this.tableNameText.getText();
			try {
				List<SapTableDescription> tableList = this.manager.listTables(tableName, "");
				if ((tableList == null) || (tableList.isEmpty())) {
					MessageDialog.openWarning(getShell(), "SAP", Messages.getString("SapTableForm.NoTable"));
				} else {
					validatingFunction(tableList.get(0));
					setColumnTableInput();
				}
			} catch (Exception exception) {
				String message = exception.getMessage();
				if (message.equalsIgnoreCase("NO_FUNCTION_FOUND")) {
					message = Messages.getString("SapTableForm.NoTable");
				}
				MessageDialog.openWarning(getShell(), "SAP", message);
			}
		}
	}

	/**
	 * 
	 */
	private void setColumnTableInput() {
		OutputSAPFunctionParameterTable outputParameterTable = functionUnit.getOutputParameterTable();
		columnTableViewer.setInput(outputParameterTable.getColumns());
	}

}