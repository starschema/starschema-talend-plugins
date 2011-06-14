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
package org.talend.repository.ui.wizards.metadata.connection.files.excel;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import jxl.read.biff.BiffException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledCombo;
import org.talend.commons.ui.swt.formtools.LabelledFileField;
import org.talend.commons.ui.swt.formtools.UtilsButton;
import org.talend.commons.ui.utils.PathUtils;
import org.talend.core.model.metadata.IMetadataContextModeManager;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.swt.utils.AbstractExcelFileStepForm;

/**
 * DOC yexiaowei class global comment. Detailled comment
 */
public class ExcelFileStep1Form extends AbstractExcelFileStepForm {

    private static final int GROUP_WIDTH = 120;

    private static final int WIDTH = 60;

    private LabelledCombo serverCombo = null;

    private LabelledFileField fileField = null;

    private LabelledCombo sheetsCombo = null;

    private TableViewer viewer = null;

    private boolean readOnly;

    private UtilsButton cancelButton;

    private ExcelReader excelReader = null;

    private CheckboxTreeViewer sheetViewer = null;

    private static final int WIDTH_GRIDDATA_PIXEL = 300;

    private List<String> allsheets = new ArrayList<String>();

    /**
     * DOC yexiaowei ExcelFileStep1Form constructor comment.
     * 
     * @param parent
     * @param connectionItem
     * @param existingNames
     */
    public ExcelFileStep1Form(Composite parent, ConnectionItem connectionItem, String[] existingNames,
            IMetadataContextModeManager contextModeManager) {
        super(parent, connectionItem, existingNames);
        setContextModeManager(contextModeManager);
        setupForm();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.swt.utils.AbstractForm#adaptFormToReadOnly()
     */
    @Override
    protected void adaptFormToReadOnly() {
        readOnly = isReadOnly();
        fileField.setReadOnly(isReadOnly());
        updateStatus(IStatus.OK, ""); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.swt.utils.AbstractForm#addFields()
     */
    @Override
    protected void addFields() {
        Group group = Form.createGroup(this, 1, Messages.getString("FileStep2.groupDelimitedFileSettings"), GROUP_WIDTH); //$NON-NLS-1$
        Composite compositeFileLocation = Form.startNewDimensionnedGridLayout(group, 3, WIDTH_GRIDDATA_PIXEL, 120);

        String[] serverLocation = { "Localhost 127.0.0.1" }; //$NON-NLS-1$
        serverCombo = new LabelledCombo(compositeFileLocation, Messages.getString("FileStep1.server"), Messages //$NON-NLS-1$
                .getString("FileStep1.serverTip"), serverLocation, 2, true, SWT.NONE); //$NON-NLS-1$

        String[] extensions = { "*.xls;*.xlsx" }; //$NON-NLS-1$ //$NON-NLS-2$  hywang add "*.xlsx"
        fileField = new LabelledFileField(compositeFileLocation, Messages.getString("FileStep1.filepath"), extensions); //$NON-NLS-1$

        int numColumnForViewer = 4;

        viewerGroup = Form.createGroup(this, numColumnForViewer, Messages.getString("ExcelFileStep1Form.fileSetting"), 150); //$NON-NLS-1$

        createSheetsSelectViewer(viewerGroup);

        Composite compositeExcelViewer = Form.startNewDimensionnedGridLayout(viewerGroup, 2, WIDTH_GRIDDATA_PIXEL, 150);

        sheetsCombo = new LabelledCombo(compositeExcelViewer, Messages.getString("ExcelFileStep1Form.sheet.choice"), Messages //$NON-NLS-1$
                .getString("ExcelFileStep1Form.sheet.tip"), new String[] { "Sheet1" }, 1, false, SWT.NONE); //$NON-NLS-1$ //$NON-NLS-2$

        createTableViewer(compositeExcelViewer);

        if (!isInWizard()) {
            Composite compositeBottomButton = Form.startNewGridLayout(this, 2, false, SWT.CENTER, SWT.CENTER);
            cancelButton = new UtilsButton(compositeBottomButton, Messages.getString("CommonWizard.cancel"), WIDTH_BUTTON_PIXEL, //$NON-NLS-1$
                    HEIGHT_BUTTON_PIXEL);
        }

        makeViewerGroupAvailable(false);
        addUtilsButtonListeners();
    }

    /**
     * DOC YeXiaowei Comment method "createSheetsSelectViewer".
     * 
     * @param group
     */
    private void createSheetsSelectViewer(Group group) {
        sheetsViewerComposite = Form.startNewDimensionnedGridLayout(group, 2, 80, 150);
        Label label = new Label(sheetsViewerComposite, SWT.NONE);
        label.setText(Messages.getString("ExcelFileStep1Form.sheetPara")); //$NON-NLS-1$

        Combo place = new Combo(sheetsViewerComposite, SWT.NONE);
        place.setVisible(false);

        createPerlSheetsViewer(sheetsViewerComposite);
    }

    private void makeViewerGroupAvailable(boolean available) {
        if (viewerGroup != null) {
            viewerGroup.setEnabled(available);
        }
    }

    private void createPerlSheetsViewer(Composite parent) {
        sheetViewer = new ContainerCheckedTreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);

        sheetViewer.setContentProvider(new ITreeContentProvider() {

            public Object[] getChildren(Object parentElement) {
                if (parentElement instanceof SheetNode) {
                    return ((SheetNode) parentElement).getChildren().toArray();
                }
                return null;
            }

            public Object getParent(Object element) {

                return null;
            }

            public boolean hasChildren(Object element) {
                if (element instanceof SheetNode) {
                    return ((SheetNode) element).getChildren() != null && ((SheetNode) element).getChildren().size() > 0;
                }
                return false;
            }

            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof List) {
                    return ((List) inputElement).toArray();
                }
                return null;
            }

            public void dispose() {

            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

            }
        });

        sheetViewer.setLabelProvider(new ILabelProvider() {

            public Image getImage(Object element) {
                return null;
            }

            public String getText(Object element) {
                if (element instanceof SheetNode) {
                    return ((SheetNode) element).getLabel();
                }
                return null;
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
        initSheetViewer();
        sheetViewer.expandAll();

        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        sheetViewer.getTree().setLayoutData(data);

        addTreeListener();
    }

    /**
     * DOC YeXiaowei Comment method "resetSheetViewer".
     */
    private void initSheetViewer() {
        if (sheetViewer == null) {
            return;
        }
        List<SheetNode> sheetChildren = new ArrayList<SheetNode>();
        for (String s : allsheets) {
            SheetNode current = new SheetNode(rootNode, s);
            sheetChildren.add(current);
        }
        rootNode.setChildren(sheetChildren);

        List<SheetNode> rootList = new ArrayList<SheetNode>();
        rootList.add(rootNode);
        sheetViewer.setInput(rootList);
        sheetViewer.expandAll();
    }

    private void addTreeListener() {

        sheetViewer.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                fillSheetList();
                checkFieldsValue();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void fillSheetList() {
        Object[] x = sheetViewer.getCheckedElements();
        ArrayList sl = getConnection().getSheetList();
        if (sl == null) {
            sl = new ArrayList();
            getConnection().setSheetList(sl);
        }
        sl.clear();
        boolean allSheets = false;
        for (Object o : x) {
            if (o instanceof SheetNode) {
                if (o.equals(rootNode)) {
                    if (!sheetViewer.getGrayed(o)) {
                        allSheets = true;
                    }
                } else {
                    sl.add(((SheetNode) o).getLabel());
                }
            }
        }
        getConnection().setSelectAllSheets(allSheets);
    }

    /**
     * DOC yexiaowei Comment method "createTableViewer".
     * 
     * @param compositeExcelViewer
     */
    private void createTableViewer(Composite compositeExcelViewer) {

        viewer = new TableViewer(compositeExcelViewer, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

        Table table = viewer.getTable();

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        GridData layData = new GridData(GridData.FILL_BOTH);
        layData.horizontalSpan = 2;

        table.setLayoutData(layData);

        viewer.setContentProvider(new IStructuredContentProvider() {

            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof List) {
                    return ((List) inputElement).toArray();
                }
                return null;
            }

            public void dispose() {

            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

            }

        });

        viewer.setLabelProvider(new ITableLabelProvider() {

            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }

            public String getColumnText(Object element, int columnIndex) {
                if (element instanceof String[]) {
                    try {
                        return ((String[]) element)[columnIndex];
                    } catch (Exception e) {
                        return ""; //$NON-NLS-1$
                    }
                }
                return ""; //$NON-NLS-1$
            }

            public void addListener(ILabelProviderListener listener) {

            }

            public void dispose() {

            }

            public boolean isLabelProperty(Object element, String property) {
                return true;
            }

            public void removeListener(ILabelProviderListener listener) {

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

        serverCombo.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                getConnection().setServer(serverCombo.getText());
                checkFieldsValue();
            }
        });

        fileField.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!isContextMode()) {
                    if (isVisible()) {
                        try {
                            viewExcelFile();
                            restoreSelectedSheets();
                            checkFieldsValue();
                        } catch (BiffException e1) {
                            getConnection().setFilePath(null);
                            updateErrorStatus(e1.getMessage());
                            makeViewerGroupAvailable(false);
                        } catch (IOException e1) {
                            getConnection().setFilePath(null);
                            updateErrorStatus(e1.getMessage());
                            makeViewerGroupAvailable(false);
                        }
                    }
                }
            }

        });

        sheetsCombo.getCombo().addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (isVisible()) {
                    String sheet = sheetsCombo.getText().trim();
                    viewSheet(sheet);
                    checkFieldsValue();
                }
            }
        });
    }

    /**
     * DOC YeXiaowei Comment method "reserverSelectedSheets".
     */
    private void restoreSelectedSheets() {
        if (!getConnection().isSelectAllSheets()) {
            ArrayList<?> sheetList = getConnection().getSheetList();
            List<String> removed = new ArrayList<String>();
            if (sheetList != null && !sheetList.isEmpty()) {
                for (int i = 0; i < sheetList.size(); i++) {
                    String curSheet = (String) sheetList.get(i);
                    boolean needRemove = true;
                    for (String sheet : allsheets) {
                        if (curSheet.equals(sheet)) {
                            needRemove = false;
                            break;
                        }
                    }
                    if (needRemove) {
                        removed.add(curSheet);
                    }
                }
                if (!removed.isEmpty()) {
                    sheetList.removeAll(removed);
                }
            }
        }
    }

    /**
     * DOC yexiaowei Comment method "readAndViewExcelFile".
     */
    private void viewExcelFile() throws IOException, BiffException {

        String fileStr = fileField.getText();
        String filePath = null;

        if (isContextMode() && getContextModeManager() != null) {
            fileStr = getContextModeManager().getOriginalValue(getConnection().getFilePath());
            fileStr = TalendTextUtils.removeQuotes(fileStr);
            filePath = PathUtils.getPortablePath(fileStr);
        } else {
            filePath = PathUtils.getPortablePath(fileStr);
        }

        excelReader = new ExcelReader(filePath);

        String[] sheets = excelReader.getSheetNames();

        allsheets.clear();

        for (String s : sheets) {
            allsheets.add(s);
        }

        initSheetViewer();
        initSheetsCombo(sheets);
        viewSheet(sheetsCombo.getText());

        if (!isContextMode()) {
            getConnection().setFilePath(filePath);
        }
        makeViewerGroupAvailable(true);
    }

    private void viewSheet(final String sheetName) {

        ProgressMonitorDialog dialog = new ProgressMonitorDialog(viewer.getTable().getShell());

        try {
            dialog.run(true, false, new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

                    monitor.beginTask("Excel Preview", IProgressMonitor.UNKNOWN); //$NON-NLS-1$

                    getConnection().setSheetName(sheetName);

                    Display.getDefault().syncExec(new Runnable() {

                        @SuppressWarnings("unchecked")
                        public void run() {

                            disposeExistColumns();

                            final List<String[]> input = excelReader.readSheet(sheetName);
                            if (input == null) {
                                viewer.setInput(getEmptyInput());
                                return;
                            }

                            int maxLength = 0;
                            for (int i = 0, z = input.size(); i < z; i++) {
                                int x = input.get(i).length;
                                if (x > maxLength) {
                                    maxLength = x;
                                }
                            }

                            String[] names = ExcelReader.getColumnsTitle(maxLength);
                            List columns = getConnection().getSheetColumns();
                            columns.clear();

                            for (String name : names) {
                                columns.add(name);
                                TableColumn tableColumn = new TableColumn(viewer.getTable(), SWT.NONE);
                                tableColumn.setText(name);
                                tableColumn.setWidth(WIDTH);
                            }
                            viewer.setInput(input);
                        }
                    });

                    monitor.done();

                }

            });
        } catch (InvocationTargetException e) {
            updateErrorStatus(e.getMessage());
        } catch (InterruptedException e) {
            updateErrorStatus(e.getMessage());
        }

    }

    private List<String[]> getEmptyInput() {
        List<String[]> res = new ArrayList<String[]>();
        res.add(new String[0]);
        return res;
    }

    /**
     * DOC yexiaowei Comment method "disposeExistColumns".
     */
    private void disposeExistColumns() {
        Table table = viewer.getTable();
        TableColumn[] cols = table.getColumns();
        if (cols != null && cols.length > 0) {
            for (TableColumn col : cols) {
                col.dispose();
            }
        }
    }

    private void updateErrorStatus(String msg) {
        updateStatus(IStatus.ERROR, msg);
    }

    /**
     * DOC yexiaowei Comment method "initSheetsCombo".
     * 
     * @param sheets
     */
    private void initSheetsCombo(String[] sheets) {

        String sheetOrigin = getConnection().getSheetName();

        sheetsCombo.removeAll();

        for (String sheet : sheets) {
            sheetsCombo.add(sheet);
        }

        if (sheetOrigin == null || sheetOrigin.equals("") || !containsSheet(sheets, sheetOrigin)) { //$NON-NLS-1$
            sheetsCombo.select(0);
        } else {
            sheetsCombo.setText(sheetOrigin);
        }
    }

    private boolean containsSheet(String[] sheets, String sheet) {
        for (String s : sheets) {
            if (s.equals(sheet)) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.swt.utils.AbstractForm#addUtilsButtonListeners()
     */
    @Override
    protected void addUtilsButtonListeners() {
        if (!isInWizard()) {
            cancelButton.addSelectionListener(new SelectionAdapter() {

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
        if (!isContextMode()) {
            fileField.setEditable(true);
        }
        if (fileField.getText() == "") { //$NON-NLS-1$
            updateStatus(IStatus.ERROR, Messages.getString("FileStep1.filepathAlert")); //$NON-NLS-1$
            return false;
        }

        if (!getConnection().isSelectAllSheets() && !isContextMode()) {
            if (getConnection().getSheetList() == null || getConnection().getSheetList().size() <= 0) {
                updateStatus(IStatus.ERROR, "At lease one sheet should be selected"); //$NON-NLS-1$
                return false;
            }
        }

        updateStatus(IStatus.OK, null);
        return true;
    }

    /**
     * DOC YeXiaowei Comment method "initAllParameters".
     */
    private void initAllParameters() {
        if (getConnection().getServer() == null) {
            serverCombo.select(0);
            getConnection().setServer(serverCombo.getText());
        } else {
            serverCombo.setText(getConnection().getServer());
        }
        serverCombo.clearSelection();

        // Just mask it.
        serverCombo.setReadOnly(true);

        if (getConnection().getFilePath() != null) {
            if (isContextMode()) {
                fileField.setText(getConnection().getFilePath());
            } else {
                fileField.setText(getConnection().getFilePath().replace("\\\\", "\\")); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        String sheetName = getConnection().getSheetName();
        if (sheetName != null && !sheetName.equals("")) { //$NON-NLS-1$
            sheetName = sheetName.replace("\\\\", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
            for (int i = 0; i < sheetsCombo.getCombo().getItems().length; i++) {
                if (sheetName.equals(sheetsCombo.getCombo().getItems()[i])) {
                    sheetsCombo.select(i);
                    break;
                }
            }
        }
    }

    /**
     * DOC YeXiaowei Comment method "initTreeSelectStates".
     */
    private void initTreeSelectStates() {
        if (rootNode == null || getConnection().getSheetList() == null) {
            return;
        }
        List<SheetNode> ss = rootNode.getChildren();
        if (ss == null || ss.size() <= 0) {
            return;
        }
        for (SheetNode node : ss) {
            if (getConnection().getSheetList().contains(node.getLabel())) {
                sheetViewer.setChecked(node, true);
            }
        }
        sheetViewer.expandAll();
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (isReadOnly() != readOnly) {
            adaptFormToReadOnly();
        }
        if (visible) {
            initAllParameters();
            initTreeSelectStates();
            checkFieldsValue();
        }
    }

    @Override
    protected void adaptFormToEditable() {
        super.adaptFormToEditable();
        fileField.setEditable(!isContextMode());
    }

    private final SheetNode rootNode = new SheetNode(null, "All sheets/DSelect sheet"); //$NON-NLS-1$

    private Composite sheetsViewerComposite;

    private Group viewerGroup;

    /**
     * 
     * DOC YeXiaowei ExcelFileStep1Form class global comment. Detailled comment <br/>
     * 
     */
    public static class SheetNode {

        private final String label;

        private final SheetNode parent;

        private List<SheetNode> children = null;

        public SheetNode(SheetNode parent, String label) {
            this.label = label;
            this.parent = parent;
        }

        /**
         * Getter for label.
         * 
         * @return the label
         */
        public String getLabel() {
            return this.label;
        }

        /**
         * Getter for parent.
         * 
         * @return the parent
         */
        public SheetNode getParent() {
            return this.parent;
        }

        /**
         * Getter for children.
         * 
         * @return the children
         */
        public List<SheetNode> getChildren() {
            return this.children;
        }

        /**
         * Sets the children.
         * 
         * @param children the children to set
         */
        public void setChildren(List<SheetNode> children) {
            this.children = children;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.swt.utils.AbstractForm#initialize()
     */
    @Override
    protected void initialize() {

    }
}
