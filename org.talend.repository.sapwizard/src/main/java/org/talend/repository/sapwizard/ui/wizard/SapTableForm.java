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
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.commons.ui.swt.formtools.UtilsButton;
import org.talend.commons.utils.data.text.IndiceHelper;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.OutputSAPFunctionParameterTable;
import org.talend.core.model.metadata.builder.connection.SAPFunctionParameterColumn;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.cwm.helper.ModelElementHelper;
import org.talend.repository.sap.i18n.Messages;
import org.talend.repository.sapwizard.service.SapDataTypeEnum;
import org.talend.repository.sapwizard.service.SapUtil;

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
	 * directoryText
	 */
	private LabelledText inputDirectoryText;
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
	 * metadataTable
	 */
	private MetadataTable metadataTable;
	private ToolItem addToolItem;
	private ToolItem removeToolItem;
	private ToolItem upToolItem;
	private ToolItem downToolItemDown;

	/**
	 * @param parent
	 * @param connectionItem
	 * @param functionUnit
	 * @param metadataTable
	 */
	public SapTableForm(Composite parent, ConnectionItem connectionItem, SAPFunctionUnit functionUnit, MetadataTable metadataTable) {
		super(parent, 0, null);
		this.connectionItem = connectionItem;
		this.functionUnit = functionUnit;
		this.metadataTable = metadataTable;
		this.newTableList = new ArrayList<MetadataTable>();

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
		this.inputDirectoryText.setReadOnly(isReadOnly());
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
		this.directoryText = new LabelledText(parent, Messages.getString("SapTableForm.Output.Directory"), true);
		this.inputDirectoryText = new LabelledText(parent, Messages.getString("SapTableForm.Input.Directory"), true);

		addFindhButton(parent);

		Group group = Form.createGroup(parent, 1, Messages.getString("SapTableForm.GroupColumnsToDownload"), 1);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 2;
		gridData.verticalIndent = 20;
		group.setLayoutData(gridData);

		Composite columnTableComposite = new Composite(group, SWT.NULL);
		columnTableComposite.setLayout(new GridLayout());
		GridData columnCompGridData = new GridData(GridData.FILL_BOTH);
		columnCompGridData.widthHint = 400;
		columnCompGridData.heightHint = 200;
		columnTableComposite.setLayoutData(columnCompGridData);

		columnTableViewer = new TableViewer(columnTableComposite, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(10, 50, true));
		layout.addColumnData(new ColumnWeightData(10, 20, true));
		layout.addColumnData(new ColumnWeightData(10, 50, false));
		columnTableViewer.getTable().setLayout(layout);
		GridDataFactory.fillDefaults().span(1, 2).grab(true, true).applyTo(columnTableViewer.getTable());

		String[] columnNames = new String[] { Messages.getString("SapTableForm.ColumnTable.Column1"),
				Messages.getString("SapTableForm.ColumnTable.Column2"), Messages.getString("SapTableForm.ColumnTable.Column3") };

		columnTableViewer.setColumnProperties(columnNames);
		columnTableViewer.setContentProvider(new ArrayContentProvider());
		columnTableViewer.setLabelProvider(new TableColumnLabelProvider());
		final Table table = columnTableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		createColumn(0, 50);
		createColumn(1, 20);
		createColumn(2, 50);

		ComboBoxCellEditor dataTypeComboCellEdditor = new ComboBoxCellEditor(table, SapDataTypeEnum.getLabels(), SWT.READ_ONLY);
		TextCellEditor columNameTextCellEditor = new TextCellEditor(this.columnTableViewer.getTable());
		TextCellEditor descTextCellEditor = new TextCellEditor(this.columnTableViewer.getTable());

		this.columnTableViewer.setCellEditors(new CellEditor[] { columNameTextCellEditor, dataTypeComboCellEdditor, descTextCellEditor });
		addCellModifier();
		createTabbingForTableCell();

		addTableToolBar(columnTableComposite);
	}

	private void createColumn(final int columnIndex, int width) {
		TableColumn column = new TableColumn(columnTableViewer.getTable(), SWT.LEFT_TO_RIGHT);
		column.setText(columnTableViewer.getColumnProperties()[columnIndex].toString());
		column.setWidth(width);
		column.setResizable(true);
		column.setMoveable(true);
	}

	private void addTableToolBar(Composite columnTableComposite) {
		ToolBar toolBar = new ToolBar(columnTableComposite, SWT.HORIZONTAL | SWT.WRAP | SWT.RIGHT);

		addToolItem = new ToolItem(toolBar, SWT.PUSH);
		addToolItem.setImage(ImageProvider.getImage(EImage.ADD_ICON));

		removeToolItem = new ToolItem(toolBar, SWT.PUSH);
		removeToolItem.setImage(ImageProvider.getImage(EImage.DELETE_ICON));
		removeToolItem.setEnabled(false);

		upToolItem = new ToolItem(toolBar, SWT.PUSH);
		upToolItem.setImage(ImageProvider.getImage(EImage.UP_ICON));
		upToolItem.setEnabled(false);

		downToolItemDown = new ToolItem(toolBar, SWT.PUSH);
		downToolItemDown.setImage(ImageProvider.getImage(EImage.DOWN_ICON));
		downToolItemDown.setEnabled(false);
	}

	private void setMoveAndUp(int currentPos, int newPos) {
		@SuppressWarnings("unchecked")
		List<SAPFunctionParameterColumn> input = (List<SAPFunctionParameterColumn>) columnTableViewer.getInput();
		IStructuredSelection selection = (IStructuredSelection) columnTableViewer.getSelection();
		SAPFunctionParameterColumn moveColumn = (SAPFunctionParameterColumn) selection.getFirstElement();
		input.remove(currentPos);
		input.add(newPos, moveColumn);
		columnTableViewer.setSelection(selection, true);
		columnTableViewer.refresh();
	}

	private void addCellModifier() {
		this.columnTableViewer.setCellModifier(new ICellModifier() {

			public void modify(Object element, String property, Object value) {
				TableItem localTableItem = (TableItem) element;
				Object localObject = localTableItem.getData();
				int columnIndex = getColumnIndex(property);
				if ((localObject instanceof SAPFunctionParameterColumn)) {
					SAPFunctionParameterColumn column = (SAPFunctionParameterColumn) localObject;
					switch (columnIndex) {
					case 0:
						column.setName(String.valueOf(value));
						break;
					case 1:
						Integer index = Integer.valueOf(value.toString());
						String dataTpe;
						if (index < 0 || index > SapDataTypeEnum.getLabels().length) {
							dataTpe = SapDataTypeEnum.INVALID.name();
						} else {
							dataTpe = SapDataTypeEnum.getLabels()[index];
						}
						column.setDataType(dataTpe);
						break;
					case 2:
						ModelElementHelper.getFirstDescription(column).setBody(String.valueOf(value));
						break;
					}
				}
				columnTableViewer.refresh();

			}

			public Object getValue(Object element, String property) {
				if (element == null || property == null) {
					return "";
				}
				if ((element instanceof SAPFunctionParameterColumn)) {
					SAPFunctionParameterColumn column = (SAPFunctionParameterColumn) element;
					switch (getColumnIndex(property)) {
					case 0:
						return column.getName() == null ? "" : column.getName();
					case 1:
						List<String> dataTypes = Arrays.asList(SapDataTypeEnum.getLabels());
						return column.getDataType() == null ? dataTypes.indexOf(SapDataTypeEnum.INVALID) : dataTypes.indexOf(column.getDataType());
					case 2:
						return ModelElementHelper.getFirstDescription(column).getBody();
					}
				}
				return null;
			}

			public boolean canModify(Object element, String property) {
				return true;
			}
		});
	}

	private int getColumnIndex(String property) {
		return Arrays.asList(columnTableViewer.getColumnProperties()).indexOf(property);
	}

	private void createTabbingForTableCell() {
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(columnTableViewer) {
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		TableViewerEditor.create(columnTableViewer, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);
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
		this.findButton = new UtilsButton(localComposite, Messages.getString("SapTableForm.FindButton"), 150, 30);
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
		this.inputDirectoryText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent modifyEvent) {
				checkFieldsValue();
			}
		});

		this.columnTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection localIStructuredSelection = (IStructuredSelection) columnTableViewer.getSelection();
				updateToolBarItems(localIStructuredSelection.getFirstElement() != null);
			}
		});
		this.columnTableViewer.getTable().addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					removeColumn();
				}
			}

			public void keyPressed(KeyEvent e) {
			}
		});
		this.addToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (functionUnit == null) {
					return;
				}
				SAPFunctionParameterColumn sapFunctionParameterColumn = ConnectionFactory.eINSTANCE.createSAPFunctionParameterColumn();
				sapFunctionParameterColumn.setName(Messages.getString("SapTableForm.DefaultNewLabel"));
				sapFunctionParameterColumn.setDataType("DataType");
				sapFunctionParameterColumn.setParameterType("");
				sapFunctionParameterColumn.setLength("Length");
				ModelElementHelper.getFirstDescription(sapFunctionParameterColumn).setBody("Description");

				sapFunctionParameterColumn.setStructureOrTableName(metadataTable.getLabel());
				functionUnit.getOutputParameterTable().getColumns().add(sapFunctionParameterColumn);
				columnTableViewer.refresh();
				columnTableViewer.setSelection(new StructuredSelection(sapFunctionParameterColumn), true);
				updateToolBarItems(true);
			}
		});

		this.removeToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeColumn();
			}
		});

		this.upToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Table table = columnTableViewer.getTable();
				int selectedIndex = table.getSelectionIndex();
				setMoveAndUp(selectedIndex, selectedIndex - 1);
				table.select(selectedIndex - 1);
				upToolItem.setEnabled(selectedIndex - 1 > 0);
				downToolItemDown.setEnabled(true);

			}
		});

		this.downToolItemDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				Table table = columnTableViewer.getTable();
				int selectedIndex = table.getSelectionIndex();
				setMoveAndUp(selectedIndex, selectedIndex + 1);
				table.select(selectedIndex + 1);
				upToolItem.setEnabled(true);
				downToolItemDown.setEnabled(selectedIndex < table.getItemCount() - 2);
			}

		});

	}

	private void removeColumn() {
		IStructuredSelection selections = (IStructuredSelection) columnTableViewer.getSelection();
		if (selections.isEmpty() || functionUnit == null) {
			return;
		}
		for (Object selection : selections.toArray()) {

			SAPFunctionParameterColumn selectedCol = (SAPFunctionParameterColumn) selection;
			Table table = columnTableViewer.getTable();
			int selectionIndex = table.getSelectionIndex();
			functionUnit.getOutputParameterTable().getColumns().remove(selectedCol);
			columnTableViewer.refresh();

			if (table.getItemCount() == 0) {
				updateToolBarItems(false);
				return;
			}
			int nextIndex = selectionIndex == table.getItemCount() ? 0 : selectionIndex;
			SAPFunctionParameterColumn elementAt = (SAPFunctionParameterColumn) columnTableViewer.getElementAt(nextIndex);
			columnTableViewer.setSelection(new StructuredSelection(elementAt), true);
		}
		updateToolBarItems(true);
	}

	private void updateToolBarItems(boolean enabled) {
		removeToolItem.setEnabled(enabled);
		Table table = columnTableViewer.getTable();
		upToolItem.setEnabled(!(table.getSelectionIndex() <= 0));
		downToolItemDown.setEnabled((table.getSelectionIndex() < table.getItemCount() - 1 && table.getSelectionIndex() >= 0));
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
		if (getStatusLevel() == Status.OK) {
			String tableName = this.tableNameText.getText();
			try {
				functionUnit = SapUtil.createFunctionForGivenTable(tableName, getConnection());
				if (functionUnit == null) {
					MessageDialog.openWarning(getShell(), "SAP", Messages.getString("SapTableForm.NoTable"));
				} else {
					this.metadataTable = functionUnit.getMetadataTable();
					setColumnTableInput();
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

	/**
	 * 
	 */
	private void setColumnTableInput() {
		OutputSAPFunctionParameterTable outputParameterTable = functionUnit.getOutputParameterTable();
		columnTableViewer.setInput(outputParameterTable.getColumns());
	}

}