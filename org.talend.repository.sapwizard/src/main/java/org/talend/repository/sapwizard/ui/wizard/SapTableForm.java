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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.commons.ui.swt.formtools.UtilsButton;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.behavior.CellEditorValueAdapter;
import org.talend.commons.ui.swt.tableviewer.behavior.ComboEditorValueAdapter;
import org.talend.commons.ui.swt.tableviewer.behavior.IColumnImageProvider;
import org.talend.commons.ui.utils.ControlUtils;
import org.talend.commons.utils.data.text.IndiceHelper;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.SAPConnection;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.metadata.builder.connection.impl.MetadataColumnImpl;
import org.talend.core.model.metadata.editor.MetadataEmfTableEditor;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.metadata.editor.MetadataEmfTableEditorView;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.repository.sap.i18n.Messages;
import org.talend.repository.sapwizard.service.SapUtil;
import orgomg.cwm.objectmodel.core.Expression;

/**
 * @author Ammu
 * 
 */
public class SapTableForm extends BaseSAPForm {

	private enum SearchMode {
		OVERRIDE, MANUAL
	}

	private SearchMode searchMode;

	/**
	 * tableNameText
	 */
	private LabelledText tableNameText;

	/**
	 * findButton
	 */
	private UtilsButton findButton;

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
	 * metadataTable
	 */
	private MetadataTable metadataTable;
	private MetadataEmfTableEditor metadataEditor;
	private MetadataEmfTableEditorView tableEditorView;

	ComboBoxCellEditor nameColumnCellEditor;
	Map<String, MetadataColumn> columnNameMetadataColumnMap;
	Map<String, MetadataColumn> retrievedColumnsMap = new HashMap<String, MetadataColumn>();
	List<String> customizedColumnsList = new ArrayList<String>();

	Button btnRadioAddSchema;
	Button btnRadioOverrideSchema;
	String lastValidValue = "";

	SelectionListener comboSelectionListener = new SelectionAdapter() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			int selectedIdx = ((CCombo) e.getSource()).getSelectionIndex();
			String selectedItem = ((CCombo) e.getSource()).getItem(selectedIdx);
			MetadataColumn column = columnNameMetadataColumnMap.get(selectedItem);
			TableItem item = (TableItem) ((CCombo) e.getSource()).getData();

			Table table = item.getParent();
			int columnIdx = table.getSelectionIndex();
			if (!metadataTable.getColumns().get(columnIdx).equals(column)) {
				metadataTable.getColumns().remove(columnIdx);
				metadataEditor.setMetadataTable(metadataTable);
			}
			metadataTable.getColumns().add(columnIdx, column);
			metadataEditor.setMetadataTable(metadataTable);
			retrievedColumnsMap.remove(column.getLabel());
			tableEditorView.setMetadataEditor(metadataEditor);
			table.select(columnIdx);
		}
	};


	/**
	 * @param parent
	 * @param connectionItem
	 * @param functionUnit
	 * @param metadataTable
	 */
	public SapTableForm(Composite parent, ConnectionItem connectionItem, SAPFunctionUnit functionUnit, MetadataTable metadataTable, String[] existingNames) {
		super(parent, 0, existingNames);
		this.connectionItem = connectionItem;
		this.functionUnit = functionUnit;
		this.metadataTable = metadataTable;
		this.newTableList = new ArrayList<MetadataTable>();
		this.columnNameMetadataColumnMap = new HashMap<String, MetadataColumn>();

		setConnectionItem(connectionItem);
		setupForm(false);

		TableItem[] items = tableEditorView.getTableViewerCreator().getTableViewer().getTable().getItems();
		if (items.length == 0) {
			btnRadioOverrideSchema.setSelection(true);
			btnRadioAddSchema.setSelection(false);
			searchMode = SearchMode.OVERRIDE;
		} else {
			btnRadioOverrideSchema.setSelection(false);
			btnRadioAddSchema.setSelection(true);
			searchMode = SearchMode.MANUAL;
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.talend.repository.ui.swt.utils.AbstractForm#adaptFormToReadOnly()
	 */
	protected void adaptFormToReadOnly() {
		this.readOnly = isReadOnly();
		this.tableNameText.setReadOnly(isReadOnly());
		this.tableEditorView.setReadOnly(isReadOnly());

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
		tableNameText.forceFocus();
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
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		parent.setLayoutData(gridData);

		this.tableNameText = new LabelledText(parent, Messages.getString("SapTableForm.Name"), true);

		addFindButton(parent);

		Group group = Form.createGroup(parent, 1, Messages.getString("SapTableForm.GroupColumnsToDownload"), 1);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 2;
		group.setLayoutData(gridData);
		Composite compositeMetaData = Form.startNewGridLayout(group, 1);

		int rightCompositeWidth = 100;
		int tableCompositeHeight = 220;

		Composite compositeTable = Form.startNewDimensionnedGridLayout(compositeMetaData, 1, rightCompositeWidth, tableCompositeHeight);
		compositeTable.setLayout(new FillLayout());
		this.metadataEditor = new MetadataEmfTableEditor(""); //$NON-NLS-1$
		this.tableEditorView = new SapMetadataEmfTableEditorView(compositeTable);
		this.tableEditorView.setShowDbTypeColumn(false, false, false);
		this.tableEditorView.setShowDbColumnName(false, false);
		this.tableEditorView.setCurrentDbms("sap");
		this.tableEditorView.initGraphicComponents();
	}


	/**
	 * @param composite
	 */
	private void addFindButton(Composite composite) {
		Group group = new Group(composite, SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 2;
		group.setLayoutData(gridData);
		SelectionListener radioListener = new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (((Button) e.getSource()).getSelection()) {
					searchMode = SearchMode.OVERRIDE;
				} else {
					searchMode = SearchMode.MANUAL;
				}
			}


			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		};
		btnRadioOverrideSchema = new Button(group, SWT.RADIO);
		btnRadioOverrideSchema.setText("Override Existing Schema");
		btnRadioOverrideSchema.addSelectionListener(radioListener);

		Composite btnComposite = new Composite(group, SWT.NONE);
		btnComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 2));
		this.findButton = new UtilsButton(btnComposite, Messages.getString("SapTableForm.FindButton"), 180, 30);
		this.findButton.setEnabled(false);

		btnRadioAddSchema = new Button(group, SWT.RADIO);
		btnRadioAddSchema.setText("Add New Columns Manually");
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.talend.repository.ui.swt.utils.AbstractForm#addUtilsButtonListeners()
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
				boolean status = checkFieldsValue();
				findButton.setEnabled(status);
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
			this.tableNameText.setText(functionUnit.getName());
			this.metadataEditor.setMetadataTable(metadataTable);
			this.tableEditorView.setMetadataEditor(metadataEditor);
			this.tableEditorView.getTableViewerCreator().layout();

		}
		this.tableNameText.forceFocus();
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
		if (getStatusLevel() == Status.OK) {
			columnNameMetadataColumnMap = new HashMap<String, MetadataColumn>();
			String tableName = this.tableNameText.getText();
			try {
				if (existingNames.contains(tableName)) {
					SAPConnection sapConnection = (SAPConnection) getConnection();
					final EList<SAPFunctionUnit> funtions = sapConnection.getFuntions();
					for (SAPFunctionUnit unit : new ArrayList<SAPFunctionUnit>(funtions)) {
						if (unit.getMetadataTable().getLabel().equals(tableName)) {
							SapUtil.updateFunctionForGivenTable(unit, tableName, sapConnection);
							functionUnit = unit;
							break;
						}
					}
				} else {
					functionUnit = SapUtil.createFunctionForGivenTable(tableName, getConnection());
				}
				if (functionUnit == null) {
					MessageDialog.openWarning(getShell(), "SAP", Messages.getString("SapTableForm.NoTable"));
				} else {
					metadataTable = functionUnit.getMetadataTable();
					if (searchMode == SearchMode.OVERRIDE) {
						metadataEditor.setMetadataTable(metadataTable);
						tableEditorView.setMetadataEditor(metadataEditor);
					}
					tableEditorView.getTableViewerCreator().layout();
					handleDefaultValue(metadataEditor.getMetadataColumnList());

					TableItem[] items = tableEditorView.getTableViewerCreator().getTableViewer().getTable().getItems();
					List<String> existingTableColumnNames = new ArrayList<String>();
					for (TableItem item : items) {
						MetadataColumnImpl column = (MetadataColumnImpl) item.getData();
						existingTableColumnNames.add(column.getLabel());
					}
					for (MetadataColumn metadataColumn : metadataTable.getColumns()) {
						if (!existingTableColumnNames.contains(metadataColumn.getLabel())) {
							retrievedColumnsMap.put(metadataColumn.getLabel(), metadataColumn);
						}
						columnNameMetadataColumnMap.put(metadataColumn.getLabel(), metadataColumn);
					}
					for (MetadataColumn metadataColumn : metadataEditor.getMetadataTable().getColumns()) {
						if (!columnNameMetadataColumnMap.keySet().contains(metadataColumn.getLabel())) {
							customizedColumnsList.add(metadataColumn.getLabel());
						}
					}
					for (String columnName : retrievedColumnsMap.keySet()) {
						metadataTable.getColumns().remove(retrievedColumnsMap.get(columnName));
					}
					if (searchMode == SearchMode.MANUAL) {
						metadataTable = metadataEditor.getMetadataTable();
						functionUnit.setMetadataTable(metadataTable);
					}
					String[] retrievedColumns = retrievedColumnsMap.keySet().toArray(new String[retrievedColumnsMap.size()]);

					nameColumnCellEditor.setItems(retrievedColumns);

					updateStatus(0, "");
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


	private void handleDefaultValue(List<MetadataColumn> metadataColumnList) {
		for (MetadataColumn metadataColumn : metadataColumnList) {
			if (metadataColumn == null) {
				continue;
			}
			switch (LanguageManager.getCurrentLanguage()) {
			case JAVA:
				Expression initialValue = metadataColumn.getInitialValue();
				String returnValue = initialValue == null ? null : initialValue.getBody();
				if (metadataColumn.getTalendType().equals(JavaTypesManager.STRING.getId()) || metadataColumn.getTalendType().equals(JavaTypesManager.DATE.getId())) {
					if ((returnValue == null) || (returnValue.length() == 0)) {
						returnValue = null;
					} else if (returnValue.equalsIgnoreCase("null")) {
						returnValue = "null";
					} else {
						returnValue = returnValue.replaceAll("\'", "");
					}
				} else {
					String returnBoolean = TalendQuoteUtils.removeQuotes(returnValue);
					if (returnBoolean != null && returnBoolean.length() > 0 && returnBoolean.getBytes()[0] == 1) {
						returnValue = TalendQuoteUtils.addQuotes("1"); //$NON-NLS-1$
					}
				}
				metadataColumn.setDefaultValue(returnValue);
			default:
				break;
			}
		}
	}

	class SapMetadataEmfTableEditorView extends MetadataEmfTableEditorView {
		public static final String ID_COLUMN_UNTRANSCODE = "ID_COLUMN_UNTRANSCODE";


		public SapMetadataEmfTableEditorView(Composite parent) {
			super(parent, SWT.NONE, false);
		}


		@Override
		protected void createColumns(TableViewerCreator<MetadataColumn> tableViewerCreator, Table table) {
			tableViewerCreator.setReadOnly(this.readOnly);
			TableViewerCreatorColumn<MetadataColumn, Object> localTableViewerCreatorColumn = new TableViewerCreatorColumn<MetadataColumn, Object>(tableViewerCreator);
			localTableViewerCreatorColumn.setTitle("");
			localTableViewerCreatorColumn.setDefaultInternalValue("");
			localTableViewerCreatorColumn.setWidth(15);
			configureNameColumn(tableViewerCreator);
			configureTypeColumns(tableViewerCreator);
			configureLengthColumn(tableViewerCreator);
			configureCommentColumn(tableViewerCreator);
		}


		@Override
		protected void configureNameColumn(final TableViewerCreator<MetadataColumn> tableViewerCreator) {
			TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
			column.setId(ID_COLUMN_NAME);
			column.setTitle(Messages.getString("MetadataTableEditorView.ColumnTitle")); //$NON-NLS-1$
			column.setToolTipHeader(Messages.getString("MetadataTableEditorView.ColumnTitle")); //$NON-NLS-1$

			column.setBeanPropertyAccessors(getLabelAccessor());
			final Image imageKey = ImageProvider.getImage(EImage.KEY_ICON);
			final Image imageEmpty = ImageProvider.getImage(EImage.EMPTY);
			column.setImageProvider(new IColumnImageProvider() {

				public Image getImage(Object element) {
					if (getKeyAccesor().get((MetadataColumn) element)) {
						return imageKey;
					} else {
						return imageEmpty;
					}
				}

			});
			column.setWeight(25);
			column.setModifiable(!isReadOnly());
			column.setMinimumWidth(45);

			List<String> columnsList = new ArrayList<String>();
			if (metadataTable != null) {
				for (MetadataColumn metadataColumn : metadataTable.getColumns()) {
					columnsList.add(metadataColumn.getLabel());
				}
			}
			String[] columns = columnsList.toArray(new String[columnsList.size()]);

			nameColumnCellEditor = new ComboBoxCellEditor(tableViewerCreator.getTable(), columns);
			nameColumnCellEditor.addListener(new ICellEditorListener() {

				@Override
				public void editorValueChanged(boolean oldValidState, boolean newValidState) {

				}


				@Override
				public void cancelEditor() {

				}


				@Override
				public void applyEditorValue() {
					lastValidValue = ControlUtils.getText(nameColumnCellEditor.getControl());
					if (columnNameMetadataColumnMap.get(lastValidValue) == null) {
						final TableItem[] items = ((Table) nameColumnCellEditor.getControl().getParent()).getSelection();
						MetadataColumn metadataColumn = (MetadataColumn) items[0].getData();
						metadataColumn.setLabel(lastValidValue);
						metadataColumn.setName(lastValidValue);
						customizedColumnsList.add(lastValidValue);
						metadataEditor.setMetadataTable(metadataTable);
						tableEditorView.setMetadataEditor(metadataEditor);
					}
				}
			});
			CellEditorValueAdapter comboValueAdapter = new ComboEditorValueAdapter(null) {

				@Override
				public String[] getItems(CellEditor cellEditor) {
					if (columnNameMetadataColumnMap.isEmpty()) {
						for (MetadataColumn metadataColumn : metadataTable.getColumns()) {
							columnNameMetadataColumnMap.put(metadataColumn.getLabel(), metadataColumn);
						}
					}
					String[] columns = ((ComboBoxCellEditor) cellEditor).getItems();
					final TableItem[] items = ((Table) cellEditor.getControl().getParent()).getSelection();
					if (items != null && items.length > 0) {
						final String selectedColumnName = ((MetadataColumn) items[0].getData()).getLabel();
						boolean isSAPColumn = columnNameMetadataColumnMap.keySet().contains(selectedColumnName);
						if (isSAPColumn) {
							columns = new String[] { ((MetadataColumn) items[0].getData()).getLabel() };
						} else {
							if (customizedColumnsList.contains(selectedColumnName)) {
								columns = new String[] { selectedColumnName };
							} else if (retrievedColumnsMap == null || retrievedColumnsMap.isEmpty()) {
								columns = new String[] { "" };
							} else {
								columns = retrievedColumnsMap.keySet().toArray(new String[retrievedColumnsMap.size()]);
							}
						}
						nameColumnCellEditor.setItems(columns);
						((CCombo) cellEditor.getControl()).setText(columns[0]);
						((CCombo) cellEditor.getControl()).setEditable(!isSAPColumn);
						((CCombo) cellEditor.getControl()).setData(items[0]);
						((CCombo) cellEditor.getControl()).removeSelectionListener(comboSelectionListener);
						((CCombo) cellEditor.getControl()).addSelectionListener(comboSelectionListener);
					}
					return columns;
				}
			};

			column.setCellEditor(nameColumnCellEditor, comboValueAdapter);
		}
	}
}
