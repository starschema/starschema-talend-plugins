package org.talend.repository.sapwizard.ui.wizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.commons.ui.swt.formtools.UtilsButton;
import org.talend.commons.utils.data.text.IndiceHelper;
import org.talend.core.model.metadata.MetadataTool;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.OutputSAPFunctionParameterTable;
import org.talend.core.model.metadata.builder.connection.SAPFunctionParameterColumn;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.cwm.helper.TableHelper;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.sap.i18n.Messages;

public class SapTableForm extends AbstractSAPForm {
	private LabelledText tableNameText;
	private LabelledText directoryText;
	boolean readOnly;
	private List<MetadataTable> newTableList = new ArrayList<MetadataTable>();
	private UtilsButton checkButton;
	private UtilsButton findButton;
	private TableViewer columnTableViewer;

	private SAPFunctionUnit functionUnit;
	private MetadataTable metadataTable;
	private String initFunctionName = null;

	public SapTableForm(Composite paramComposite, ConnectionItem connectionItem, SAPFunctionUnit functionUnit,
			MetadataTable paramMetadataTable) {
		super(paramComposite, 0, null);
		this.connectionItem = connectionItem;
		this.functionUnit = functionUnit;
		this.metadataTable = paramMetadataTable;

		if (functionUnit != null) {
			this.initFunctionName = functionUnit.getName();
		}
		setConnectionItem(connectionItem);
		setupForm(false);
	}

	protected void adaptFormToReadOnly() {
		this.readOnly = isReadOnly();
		this.tableNameText.setReadOnly(isReadOnly());
		this.directoryText.setReadOnly(isReadOnly());
	}



	private void initMetadataForm() {
		String str = MetadataTool.validateValue(this.metadataTable.getLabel());
		this.tableNameText.setText(str);
		this.directoryText.setText(this.metadataTable.getComment());
		this.tableNameText.forceFocus();
	}

	protected void addMetadataTable() {
		ProxyRepositoryFactory localProxyRepositoryFactory = ProxyRepositoryFactory.getInstance();
		this.metadataTable = ConnectionFactory.eINSTANCE.createMetadataTable();
		getFunctionUnit().getTables().add(this.metadataTable);
		this.newTableList.add(this.metadataTable);
		this.metadataTable.setId(localProxyRepositoryFactory.getNextId());
		this.metadataTable.setLabel(IndiceHelper.getIndexedLabel(this.metadataTable.getLabel(), this.existingNames));
		initMetadataForm();
	}

	protected void addFields() {

		Composite parent = new Composite(this, SWT.NONE);
		parent.setLayout(new GridLayout(2, false));
		GridData localGridData = new GridData(GridData.FILL_HORIZONTAL);
		parent.setLayoutData(localGridData);

		this.tableNameText = new LabelledText(parent, Messages.getString("SapTableForm.Name"), true);
		this.directoryText = new LabelledText(parent, Messages.getString("SapTableForm.Directory"), true);

		addFindhButton(parent);

		Group localGroup = Form.createGroup(parent, 1, Messages.getString("SapTableForm.GroupColumnsToDownload"), 1);
		localGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		localGridData.horizontalSpan = 2;
		localGridData.verticalIndent = 20;
		localGroup.setLayoutData(localGridData);

		Composite localComposite7 = Form.startNewDimensionnedGridLayout(localGroup, 1, 400, 200);
		localComposite7.setLayout(new FillLayout());
		columnTableViewer = new TableViewer(localComposite7);
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(50, 75, true));
		columnTableViewer.getTable().setLayout(layout);
		columnTableViewer.setColumnProperties(new String[] { "Column" });
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
		column.setText("ColumnName");
		column.setWidth(200);
		addCheckButton(localGroup);
	}

 	private void addFindhButton(Composite paramComposite) {
		Composite localComposite = Form.startNewGridLayout(paramComposite, 1, false, 16777216, 128);
		GridData localGridData = new GridData(768);
		localGridData.horizontalSpan = 2;
		localGridData.horizontalAlignment = 16777216;
		localComposite.setLayoutData(localGridData);
		GridLayout localGridLayout = (GridLayout) localComposite.getLayout();
		localGridLayout.marginHeight = 0;
		localGridLayout.marginTop = 0;
		localGridLayout.marginBottom = 0;
		this.findButton = new UtilsButton(localComposite, "Find...", 100, 30);
		this.findButton.setEnabled(true);
	}

	private void addCheckButton(Composite paramComposite) {
		Composite localComposite = Form.startNewGridLayout(paramComposite, 1, false, 16777216, 128);
		GridData localGridData = new GridData(768);
		localGridData.horizontalSpan = 2;
		localGridData.horizontalAlignment = 16777216;
		localComposite.setLayoutData(localGridData);
		GridLayout localGridLayout = (GridLayout) localComposite.getLayout();
		localGridLayout.marginHeight = 0;
		localGridLayout.marginTop = 0;
		localGridLayout.marginBottom = 0;
		this.checkButton = new UtilsButton(localComposite, Messages.getString("SapForm.Check"), 100, 30);
		this.checkButton.setEnabled(true);
	}

 
	protected boolean checkFieldsValue() {
		if (!checkAllTablesIsCorrect())
			return false;
		updateStatus(0, null);
		return true;
	}

 
	private boolean checkAllTablesIsCorrect() {
		if (getFunctionUnit() == null)
			return false;
		EList localEList = getFunctionUnit().getTables();
		if (localEList == null)
			return false;
		for (int i = 0; i < localEList.size(); i++) {
			if (localEList.get(i) == null)
				continue;
			MetadataTable localMetadataTable = (MetadataTable) localEList.get(i);
			String[] arrayOfString = TableHelper.getTableNames(getFunctionUnit(), localMetadataTable.getLabel());
			List localList = this.existingNames == null ? Collections.EMPTY_LIST : Arrays.asList(arrayOfString);
			if (localMetadataTable.getLabel().equals("")) {
				updateStatus(4, Messages.getString("DatabaseTableForm.nameAlert"));
				return false;
			}
			if (localList.contains(localMetadataTable.getLabel())) {
				updateStatus(4, Messages.getString("CommonWizard.nameAlreadyExist") + " \""
						+ localMetadataTable.getLabel() + "\"");
				return false;
			}
			if (!Pattern.matches("^[a-zA-Z0-9\\_]+$", localMetadataTable.getLabel())) {
				updateStatus(4, Messages.getString("DatabaseTableForm.illegalChar", new Object[] { localMetadataTable
						.getLabel() }));
				return false;
			}
			if (!Pattern.matches("^[a-zA-Z_][a-zA-Z_0-9]*$", localMetadataTable.getLabel())) {
				updateStatus(4, Messages.getString("DatabaseTableForm.nameInvalid", new Object[] { localMetadataTable
						.getLabel() }));
				return false;
			}
			if (localMetadataTable.getColumns().size() != 0)
				continue;
			updateStatus(4, Messages.getString("FileStep3.itemAlert") + " \"" + localMetadataTable.getLabel() + "\"");
			return false;
		}
		return true;
	}

 	public void performCancel() {
		processWhenDispose();
		cleanModel();
	}

	private void cleanModel() {
		getFunctionUnit().getTables().removeAll(this.newTableList);
	}

	protected void processWhenDispose() {
	}

	protected void initialize() {
		if (this.functionUnit != null) {
 			if(metadataTable==null){
				metadataTable=functionUnit.getMetadataTable();
			}
			initMetadataForm();
			setColumnTableInput();
		}
		checkFieldsValue();
	}

	protected SAPFunctionUnit getFunctionUnit() {
		return this.functionUnit;
	}

 	private void setColumnTableInput() {
		OutputSAPFunctionParameterTable outputParameterTable2 = functionUnit.getOutputParameterTable();
		List<SAPFunctionParameterColumn> list = new ArrayList<SAPFunctionParameterColumn>();
		for (SAPFunctionParameterColumn column : outputParameterTable2.getColumns()) {
			list.add(column);
		}
		columnTableViewer.setInput(list);
	}

	@Override
	protected void addFieldsListeners() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void addUtilsButtonListeners() {
		// TODO Auto-generated method stub
		
	}

}