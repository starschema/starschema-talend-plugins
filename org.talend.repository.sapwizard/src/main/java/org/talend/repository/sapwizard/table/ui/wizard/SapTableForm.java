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

package org.talend.repository.sapwizard.table.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.commons.ui.swt.formtools.UtilsButton;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.utils.data.text.IndiceHelper;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
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
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		parent.setLayoutData(gridData);

		this.tableNameText = new LabelledText(parent, Messages.getString("SapTableForm.Name"), true);
		this.directoryText = new LabelledText(parent, Messages.getString("SapTableForm.Output.Directory"), true);
		this.inputDirectoryText = new LabelledText(parent, Messages.getString("SapTableForm.Input.Directory"), true);

		addFindhButton(parent);

		Group group = Form.createGroup(parent, 1, Messages.getString("SapTableForm.GroupColumnsToDownload"), 1);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 2;
		group.setLayoutData(gridData);
		Composite compositeMetaData = Form.startNewGridLayout(group, 1);

		int rightCompositeWidth = 100;
		int tableCompositeHeight = 220;

		Composite compositeTable = Form.startNewDimensionnedGridLayout(compositeMetaData, 1, rightCompositeWidth, tableCompositeHeight);
		compositeTable.setLayout(new FillLayout());
		metadataEditor = new MetadataEmfTableEditor(""); //$NON-NLS-1$
		tableEditorView = new SapMetadataEmfTableEditorView(compositeTable);
		this.tableEditorView.setShowDbTypeColumn(false, false, false);
		this.tableEditorView.setShowDbColumnName(false, false);
		tableEditorView.setCurrentDbms("sap");
		tableEditorView.initGraphicComponents();

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
			metadataEditor.setMetadataTable(metadataTable);
			tableEditorView.setMetadataEditor(metadataEditor);
			tableEditorView.getTableViewerCreator().layout();

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
			String tableName = this.tableNameText.getText();
			try {
				functionUnit = SapUtil.createFunctionForGivenTable(tableName, getConnection());
				if (functionUnit == null) {
					MessageDialog.openWarning(getShell(), "SAP", Messages.getString("SapTableForm.NoTable"));
				} else {
					this.metadataTable = functionUnit.getMetadataTable();
					metadataEditor.setMetadataTable(metadataTable);
					tableEditorView.setMetadataEditor(metadataEditor);
					tableEditorView.getTableViewerCreator().layout();
					handleDefaultValue(metadataEditor.getMetadataColumnList());
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
				String returnValue = initialValue==null?null:initialValue.getBody();
				if (metadataColumn.getTalendType().equals(JavaTypesManager.STRING.getId())
						|| metadataColumn.getTalendType().equals(JavaTypesManager.DATE.getId())) {
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
			TableViewerCreatorColumn<MetadataColumn, Object> localTableViewerCreatorColumn = new TableViewerCreatorColumn<MetadataColumn, Object>(
					tableViewerCreator);
			localTableViewerCreatorColumn.setTitle("");
			localTableViewerCreatorColumn.setDefaultInternalValue("");
			localTableViewerCreatorColumn.setWidth(15);
			configureNameColumn(tableViewerCreator);
			configureTypeColumns(tableViewerCreator);
			configureLengthColumn(tableViewerCreator);
			configureCommentColumn(tableViewerCreator);
		}
	}
}