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
package org.talend.repository.ui.wizards.metadata.connection.files.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.commons.ui.swt.drawing.link.IExtremityLink;
import org.talend.commons.ui.swt.drawing.link.LinkDescriptor;
import org.talend.commons.ui.swt.drawing.link.LinksManager;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.XMLFileNode;
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.repository.ui.swt.utils.AbstractXmlFileStepForm;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.action.CreateAttributeAction;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.action.CreateElementAction;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.action.CreateNameSpaceAction;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.action.DeleteNodeAction;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.action.DisconnectAction;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.action.FixValueAction;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.action.ImportTreeFromXMLAction;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.action.RemoveGroupAction;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.action.SetForLoopAction;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.action.SetGroupAction;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.buttons.AddTreeNodeButton;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.buttons.MoveDownTreeNodeButton;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.buttons.MoveUpTreeNodeButton;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.buttons.RemoveTreeNodeButton;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.Attribute;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.Element;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.FOXTreeNode;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.NameSpaceNode;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.util.StringUtil;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.util.TreeUtil;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.view.XmlFileSchema2TreeLinker;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.view.XmlFileSchemaDialog;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.view.XmlFileTableViewerProvider;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.view.XmlFileTreeViewerProvider;

/**
 * wzhang class global comment. Detailled comment
 */
public class XmlFileOutputStep2Form extends AbstractXmlFileStepForm {

    private SashForm mainSashFormComposite;

    private Button schemaButton;

    private TableViewer schemaViewer;

    private TreeViewer xmlViewer;

    private IAction createAction;

    private IAction deleteAction;

    private IAction disconnectAction;

    private IAction fixValueAction;

    private IAction createAttributeAction;

    private IAction createNamespaceAction;

    private IAction importFromXMLAction;

    private IAction setLoopAction;

    private IAction setGroupAction;

    private IAction removeGroupAction;

    private XmlFileSchema2TreeLinker linker;

    private String selectedText;

    private List<FOXTreeNode> treeData = new ArrayList<FOXTreeNode>();

    private boolean creation;

    private int order = 1;

    private Map<String, Integer> orderMap = new HashMap<String, Integer>();

    public XmlFileOutputStep2Form(boolean creation, Composite parent, ConnectionItem connectionItem) {
        super(parent, connectionItem);
        this.creation = creation;
        setupForm();
    }

    @Override
    protected void adaptFormToReadOnly() {

    }

    @Override
    protected void addFields() {
        mainSashFormComposite = new SashForm(this, SWT.HORIZONTAL | SWT.SMOOTH);
        mainSashFormComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        mainSashFormComposite.setBackgroundMode(SWT.INHERIT_FORCE);
        addSchemaViewer(mainSashFormComposite, 300, 100);
        addXmlFileViewer(mainSashFormComposite, 400, 100);
        mainSashFormComposite.setWeights(new int[] { 40, 60 });

        linker = new XmlFileSchema2TreeLinker(mainSashFormComposite);
        linker.setForm(this);
        linker.init(schemaViewer.getTable(), xmlViewer);
    }

    private void initSchemaTable() {
        EList columns = ConnectionHelper.getTables(getConnection()).toArray(new MetadataTable[0])[0].getColumns();
        List<MetadataColumn> columnList = new ArrayList<MetadataColumn>();
        for (int i = 0; i < columns.size(); i++) {
            columnList.add((MetadataColumn) columns.get(i));
        }
        schemaViewer.setInput(columnList);
        schemaViewer.refresh();
    }

    private void initLinker(TreeItem node, TableItem[] tableItems) {
        FOXTreeNode treeNode = (FOXTreeNode) node.getData();
        IMetadataColumn column = treeNode.getColumn();
        if (column != null) {
            for (int i = 0; i < tableItems.length; i++) {
                MetadataColumn metadataColumn = (MetadataColumn) tableItems[i].getData();
                if (metadataColumn.getLabel().equals(column.getLabel())) {
                    linker.addLoopLink(tableItems[i], tableItems[i].getData(), xmlViewer.getTree(), treeNode, true);
                    break;
                }
            }
        }
        TreeItem[] children = node.getItems();
        for (int i = 0; i < children.length; i++) {
            initLinker(children[i], tableItems);
        }
    }

    public void redrawLinkers() {
        linker.removeAllLinks();
        TreeItem root = xmlViewer.getTree().getItem(0);

        TableItem[] tableItems = schemaViewer.getTable().getItems();
        initLinker(root, tableItems);
        if (linker.linkSize() == 0) {
            linker.updateLinksStyleAndControlsSelection(xmlViewer.getTree(), true);
        }
    }

    private void addXmlFileViewer(final Composite mainComposite, final int width, final int height) {
        final Group group = Form.createGroup(mainComposite, 1, "Linker Target", height);
        GridData data = new GridData(GridData.FILL_BOTH);
        Composite composite = new Composite(group, SWT.BORDER);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(data);
        xmlViewer = new TreeViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        data = new GridData(GridData.FILL_BOTH);
        xmlViewer.getControl().setLayoutData(data);
        xmlViewer.setUseHashlookup(true);
        Tree tree = xmlViewer.getTree();
        if (isReadOnly()) {
            tree.setEnabled(false);
        }
        tree.setLinesVisible(true);
        tree.setBackground(tree.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
        column1.setText("XML Tree");
        column1.setWidth(120);

        TreeColumn column2 = new TreeColumn(tree, SWT.CENTER);
        column2.setText("Related Column");
        column2.setWidth(100);

        TreeColumn column3 = new TreeColumn(tree, SWT.CENTER);
        column3.setText("Node Status");
        column3.setWidth(100);

        TreeColumn column4 = new TreeColumn(tree, SWT.CENTER);
        column4.setText("Default Value");
        column4.setWidth(100);

        tree.setHeaderVisible(true);
        XmlFileTreeViewerProvider provider = new XmlFileTreeViewerProvider();
        xmlViewer.setLabelProvider(provider);

        xmlViewer.setCellModifier(new ICellModifier() {

            public void modify(Object element, String property, Object value) {
                TreeItem treeItem = (TreeItem) element;
                FOXTreeNode node = (FOXTreeNode) treeItem.getData();
                if (property.equals("C1")) {
                    node.setLabel((String) value);
                }
                if (property.equals("C4")) {
                    node.setDefaultValue((String) value);
                }
                xmlViewer.refresh(node);
            }

            public Object getValue(Object element, String property) {
                FOXTreeNode node = (FOXTreeNode) element;
                if (property.equals("C1")) { //$NON-NLS-1$
                    return node.getLabel();
                }
                if (property.equals("C4")) { //$NON-NLS-1$
                    return node.getDefaultValue();
                }

                return null;
            }

            public boolean canModify(Object element, String property) {
                FOXTreeNode node = (FOXTreeNode) element;
                if (property.equals("C1")) {
                    if (node.getLabel() != null && node.getLabel().length() > 0) {
                        return true;
                    }
                }
                if (property.equals("C4")) {
                    if (node.getDefaultValue() != null && node.getDefaultValue().length() > 0) {
                        return true;
                    }
                }
                return false;
            }
        });

        xmlViewer.setColumnProperties(new String[] { "C1", "C2", "C3", "C4" });
        CellEditor editor = new TextCellEditor(xmlViewer.getTree());
        editor.addListener(new DialogErrorXMLLabelCellEditor(editor, "C1"));
        CellEditor editorDefault = new TextCellEditor(xmlViewer.getTree());
        editorDefault.addListener(new DialogErrorXMLLabelCellEditor(editorDefault, "C4"));

        xmlViewer.setCellEditors(new CellEditor[] { editor, null, null, editorDefault });

        xmlViewer.setContentProvider(provider);
        // xmlViewer.setInput(treeData);
        xmlViewer.expandAll();

        createAction();

        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(xmlViewer.getControl());
        xmlViewer.getControl().setMenu(menu);
        xmlViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {

            }

        });
        initToolBar(composite);

    }

    private void initToolBar(Composite parent) {
        // tool buttons
        Composite toolBarComp = new Composite(parent, SWT.BORDER);
        GridLayout layout = new GridLayout();
        layout.numColumns = 4;
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        toolBarComp.setLayout(layout);
        toolBarComp.setLayoutData(data);
        AddTreeNodeButton addTreeNodeBtn = new AddTreeNodeButton(toolBarComp, this);
        RemoveTreeNodeButton removeNodeBtn = new RemoveTreeNodeButton(toolBarComp, this);
        MoveUpTreeNodeButton moveUpBtn = new MoveUpTreeNodeButton(toolBarComp, this);
        MoveDownTreeNodeButton moveDown = new MoveDownTreeNodeButton(toolBarComp, this);
    }

    private void createAction() {
        createAction = new CreateElementAction(xmlViewer, this, "Add Sub-element");
        createAttributeAction = new CreateAttributeAction(xmlViewer, this, "Add Attribute");
        createNamespaceAction = new CreateNameSpaceAction(xmlViewer, this, "Add Name Space");
        deleteAction = new DeleteNodeAction(xmlViewer, this, "Delete");
        disconnectAction = new DisconnectAction(xmlViewer, this, "Disconnect Linker");
        fixValueAction = new FixValueAction(xmlViewer, this, "Set A Fix Value");
        importFromXMLAction = new ImportTreeFromXMLAction(xmlViewer, this, "Import XML Tree");
        setLoopAction = new SetForLoopAction(xmlViewer, this, "Set As Loop Element");
        setGroupAction = new SetGroupAction(xmlViewer, this, "Set As Group Element");
        removeGroupAction = new RemoveGroupAction(xmlViewer, "Remove Group Element", this);

    }

    private void addSchemaViewer(final Composite mainComposite, final int width, final int height) {
        final Group group = Form.createGroup(mainComposite, 1, "Linker Source", height);
        // group.setBackgroundMode(SWT.INHERIT_FORCE);
        schemaButton = new Button(group, SWT.PUSH);
        schemaButton.setText("Schema Management");
        schemaButton.setToolTipText("You can add or edit schema and save in 'Schema List' viewer");

        schemaViewer = new TableViewer(group);
        XmlFileTableViewerProvider provider = new XmlFileTableViewerProvider();
        schemaViewer.setContentProvider(provider);
        schemaViewer.setLabelProvider(provider);

        GridData gridData = new GridData(GridData.FILL_BOTH);
        Table table = schemaViewer.getTable();
        if (isReadOnly()) {
            table.setEnabled(false);
        }
        table.setHeaderVisible(true);
        org.eclipse.swt.widgets.TableColumn column = new org.eclipse.swt.widgets.TableColumn(table, SWT.LEFT);
        column.setText("Schema List");
        column.setWidth(100);
        table.setLayoutData(gridData);
    }

    private void fillContextMenu(IMenuManager manager) {
        if (!xmlViewer.getSelection().isEmpty()) {
            manager.add(createAction);
            manager.add(createAttributeAction);
            manager.add(createNamespaceAction);
            manager.add(new Separator());
            manager.add(deleteAction);
            manager.add(disconnectAction);
            manager.add(fixValueAction);
            manager.add(new Separator());
            manager.add(setLoopAction);
            manager.add(new Separator());
            manager.add(setGroupAction);
            manager.add(removeGroupAction);
            manager.add(new Separator());
            manager.add(importFromXMLAction);
        }
    }

    @Override
    protected void addFieldsListeners() {

    }

    @Override
    protected void addUtilsButtonListeners() {
        schemaButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                XmlFileSchemaDialog dialog = new XmlFileSchemaDialog(mainSashFormComposite.getShell(),
                        XmlFileOutputStep2Form.this);
                if (dialog != null && dialog.open() == XmlFileSchemaDialog.OK) {
                    MetadataTable metadataTable = dialog.getMetadataTable();
                    EList columns = metadataTable.getColumns();
                    List<MetadataColumn> inputList = new ArrayList<MetadataColumn>();
                    for (int i = 0; i < columns.size(); i++) {
                        MetadataColumn column = (MetadataColumn) columns.get(i);
                        inputList.add(column);
                    }
                    schemaViewer.setInput(inputList);
                    schemaViewer.refresh();

                    EList columnList = ConnectionHelper.getTables(getConnection()).toArray(new MetadataTable[0])[0].getColumns();
                    columnList.clear();
                    columnList.addAll(inputList);

                    updateXmlTreeViewer();
                    redrawLinkers();
                    checkFieldsValue();
                }
            }
        });
    }

    private void updateXmlTreeViewer() {
        LinksManager<Item, Object, Tree, Object> linkManager = linker.getLinkManager();
        List<LinkDescriptor<Item, Object, Tree, Object>> links = linkManager.getLinks();
        for (int i = 0; i < links.size(); i++) {
            LinkDescriptor<Item, Object, Tree, Object> linkDescriptor = links.get(i);
            IExtremityLink<Item, Object> ex1 = linkDescriptor.getExtremity1();
            IExtremityLink<Tree, Object> ex2 = linkDescriptor.getExtremity2();
            MetadataColumn metaColumn = (MetadataColumn) ex1.getDataItem();
            FOXTreeNode node = (FOXTreeNode) ex2.getDataItem();
            node.setColumn(ConvertionHelper.convertToIMetaDataColumn(metaColumn));
            node.setDataType(metaColumn.getTalendType());
        }
        xmlViewer.refresh();
        updateConnection();
    }

    @Override
    protected boolean checkFieldsValue() {
        int num = 0, rootNum = 0;
        StringBuffer msgError = new StringBuffer();
        List<FOXTreeNode> onLoopNodes = new ArrayList<FOXTreeNode>();
        for (FOXTreeNode node : treeData) {
            FOXTreeNode rootFOXTreeNode = TreeUtil.getRootFOXTreeNode(node);
            if (rootFOXTreeNode != null) {
                if (existedLoopNode(rootFOXTreeNode)) {
                    num++;
                } else {
                    onLoopNodes.add(rootFOXTreeNode);
                }
                rootNum++;
            }
        }
        if (num != rootNum) {
            msgError.append("Require set as loop\n");
        }
        if (linker.isNoLinker()) {
            msgError.append("No source and target linked");
        }
        if ("".equals(msgError.toString())) {
            updateStatus(IStatus.OK, null);
            return true;
        }
        updateStatus(IStatus.ERROR, msgError.toString());
        return false;
    }

    public void updateStatus() {
        checkFieldsValue();
    }

    @Override
    protected void initialize() {
    }

    public IMetadataTable getIMetadataTable() {
        IMetadataTable metadataTable = null;
        if (ConnectionHelper.getTables(getConnection()) != null && !ConnectionHelper.getTables(getConnection()).isEmpty()) {
            metadataTable = ConvertionHelper
                    .convert(ConnectionHelper.getTables(getConnection()).toArray(new MetadataTable[0])[0]);
        } else {
            metadataTable = new org.talend.core.model.metadata.MetadataTable();
        }
        return metadataTable;
    }

    protected FOXTreeNode addElement(FOXTreeNode current, String currentPath, String newPath, String defaultValue) {
        String name = newPath.substring(newPath.lastIndexOf("/") + 1); //$NON-NLS-1$
        String parentPath = newPath.substring(0, newPath.lastIndexOf("/")); //$NON-NLS-1$
        FOXTreeNode temp = new Element(name, defaultValue);

        if (current == null) {// root node
            return temp;
        }

        if (currentPath.equals(parentPath)) {
            current.addChild(temp);
        } else {
            String[] nods = currentPath.split("/"); //$NON-NLS-1$
            String[] newNods = parentPath.split("/"); //$NON-NLS-1$
            int parentLevel = 0;
            int checkLength = nods.length < newNods.length ? nods.length : newNods.length;
            for (int i = 1; i < checkLength; i++) {
                if (nods[i].equals(newNods[i])) {
                    parentLevel = i;
                }
            }
            FOXTreeNode parent = current;
            for (int i = 0; i < nods.length - (parentLevel + 1); i++) {
                FOXTreeNode tmpParent = parent.getParent();
                if (tmpParent == null) {
                    break;
                }
                parent = tmpParent;
            }

            if (parent != null)
                parent.addChild(temp);
        }

        return temp;
    }

    private IMetadataColumn getColumn(String columnName) {
        EList columns = ConnectionHelper.getTables(getConnection()).toArray(new MetadataTable[0])[0].getColumns();
        for (int i = 0; i < columns.size(); i++) {
            MetadataColumn column = (MetadataColumn) columns.get(i);
            if (column.getLabel().equals(columnName)) {
                return ConvertionHelper.convertToIMetaDataColumn(column);
            }
        }
        return null;
    }

    private void initXmlTreeData() {
        treeData.clear();
        // metadataColumnList.clear();
        EList root = getConnection().getRoot();
        EList loop = getConnection().getLoop();
        EList group = getConnection().getGroup();
        // initialMetadataColumn();

        FOXTreeNode rootNode = null;
        FOXTreeNode current = null;
        FOXTreeNode temp = null;
        FOXTreeNode mainNode = null;
        String mainPath = null;
        String currentPath = null;
        String defaultValue = null;
        int nodeOrder = 0;
        boolean haveOrder = true;

        // build root tree
        for (int i = 0; i < root.size(); i++) {
            XMLFileNode node = (XMLFileNode) root.get(i);
            String newPath = node.getXMLPath();
            defaultValue = node.getDefaultValue();
            String type = node.getType();
            String orderValue = String.valueOf(node.getOrder());
            if (orderValue == null || "".equals(orderValue)) {
                haveOrder = false;
            }
            if (haveOrder) {
                nodeOrder = node.getOrder();
            }
            if (node.getAttribute().equals("attri")) {
                temp = new Attribute(newPath);
                temp.setDefaultValue(defaultValue);
                temp.setAttribute(true);
                temp.setDataType(type);
                current.addChild(temp);
            } else if (node.getAttribute().equals("ns")) {
                temp = new NameSpaceNode(newPath);
                temp.setDefaultValue(defaultValue);
                temp.setNameSpace(true);
                temp.setDataType(type);
                current.addChild(temp);
            } else {
                temp = this.addElement(current, currentPath, newPath, defaultValue);
                temp.setDataType(type);
                if (rootNode == null) {
                    rootNode = temp;
                }
                if (node.getAttribute().equals("main")) {
                    temp.setMain(true);
                    mainNode = temp;
                    mainPath = newPath;
                }
                current = temp;
                currentPath = newPath;
            }
            if (haveOrder) {
                temp.setOrder(nodeOrder);
            }
            String columnName = node.getRelatedColumn();
            if (columnName != null && columnName.length() > 0) {
                temp.setColumn(getColumn(columnName));
            }
        }

        // build group tree
        current = mainNode;
        currentPath = mainPath;
        boolean isFirst = true;
        for (int i = 0; i < group.size(); i++) {
            XMLFileNode node = (XMLFileNode) group.get(i);
            String newPath = node.getXMLPath();
            defaultValue = node.getDefaultValue();
            String type = node.getType();
            String orderValue = String.valueOf(node.getOrder());
            if (orderValue == null || "".equals(orderValue)) {
                haveOrder = false;
            }
            if (haveOrder) {
                nodeOrder = node.getOrder();
            }
            if (node.getAttribute().equals("attri")) {
                temp = new Attribute(newPath);
                temp.setDefaultValue(defaultValue);
                temp.setAttribute(true);
                temp.setDataType(type);
                current.addChild(temp);
            } else if (node.getAttribute().equals("ns")) {
                temp = new NameSpaceNode(newPath);
                temp.setDefaultValue(defaultValue);
                temp.setNameSpace(true);
                temp.setDataType(type);
                current.addChild(temp);
            } else {
                temp = this.addElement(current, currentPath, newPath, defaultValue);
                temp.setDataType(type);
                if (node.getAttribute().equals("main")) {
                    temp.setMain(true);
                    mainNode = temp;
                    mainPath = newPath;
                }
                if (isFirst) {
                    temp.setGroup(true);
                    isFirst = false;
                }
                current = temp;
                currentPath = newPath;
            }
            if (haveOrder) {
                temp.setOrder(nodeOrder);
            }
            String columnName = node.getRelatedColumn();
            if (columnName != null && columnName.length() > 0) {
                temp.setColumn(getColumn(columnName));
            }
        }

        // build loop tree
        current = mainNode;
        currentPath = mainPath;
        isFirst = true;
        for (int i = 0; i < loop.size(); i++) {
            XMLFileNode node = (XMLFileNode) loop.get(i);
            String newPath = node.getXMLPath();
            defaultValue = node.getDefaultValue();
            String type = node.getType();
            String orderValue = String.valueOf(node.getOrder());
            if (orderValue == null || "".equals(orderValue)) {
                haveOrder = false;
            }
            if (haveOrder) {
                nodeOrder = node.getOrder();
            }
            if (node.getAttribute().equals("attri")) {
                temp = new Attribute(newPath);
                temp.setDefaultValue(defaultValue);
                temp.setAttribute(true);
                temp.setDataType(type);
                temp.setDataType(type);
                current.addChild(temp);
            } else if (node.getAttribute().equals("ns")) {
                temp = new NameSpaceNode(newPath);
                temp.setDefaultValue(defaultValue);
                temp.setNameSpace(true);
                temp.setDataType(type);
                current.addChild(temp);
            } else {
                temp = this.addElement(current, currentPath, newPath, defaultValue);
                temp.setDataType(type);
                if (node.getAttribute().equals("main")) {
                    temp.setMain(true);
                    mainNode = temp;
                    mainPath = newPath;
                }
                if (isFirst) {
                    temp.setLoop(true);
                    isFirst = false;
                }
                current = temp;
                currentPath = newPath;
            }
            if (haveOrder) {
                temp.setOrder(nodeOrder);
            }
            String columnName = node.getRelatedColumn();
            if (columnName != null && columnName.length() > 0) {
                temp.setColumn(getColumn(columnName));
            }
        }

        if (rootNode == null) {
            rootNode = new Element("rootTag");
        }
        rootNode.setParent(null);
        if (haveOrder) {
            orderNode(rootNode);
        }
        treeData.add(rootNode);
    }

    public MetadataTable getMetadataTable() {
        return ConnectionHelper.getTables(getConnection()).toArray(new MetadataTable[0])[0];
    }

    public TableViewer getSchemaViewer() {
        return this.schemaViewer;
    }

    public void updateConnection() {
        ConnectionHelper.getTables(getConnection());
        EList root = getConnection().getRoot();
        EList loop = getConnection().getLoop();
        EList group = getConnection().getGroup();
        root.clear();
        loop.clear();
        group.clear();
        List<FOXTreeNode> node = (List<FOXTreeNode>) xmlViewer.getInput();
        FOXTreeNode foxTreeNode = node.get(0);
        if (foxTreeNode != null) {
            initNodeOrder(foxTreeNode);
            tableLoader((Element) foxTreeNode, "", root, foxTreeNode.getDefaultValue());
            Element loopNode = (Element) TreeUtil.getLoopNode(foxTreeNode);
            if (loopNode != null) {
                String path = TreeUtil.getPath(loopNode);
                tableLoader(loopNode, path.substring(0, path.lastIndexOf("/")), loop, loopNode.getDefaultValue());
            }
            Element groupNode = (Element) TreeUtil.getGroupNode(foxTreeNode);
            if (groupNode != null) {
                String path = TreeUtil.getPath(groupNode);
                tableLoader(groupNode, path.substring(0, path.lastIndexOf("/")), group, groupNode.getDefaultValue());
            }
        }

    }

    private void orderNode(FOXTreeNode node) {
        // reset the order.
        if (node != null) {
            List<FOXTreeNode> firstSubChildren = node.getChildren();
            FOXTreeNode foundNode = null;
            for (FOXTreeNode childen : firstSubChildren) {
                if (childen.isLoop()) {
                    foundNode = childen;
                    sortOrder(foundNode, node);
                    break;
                } else if (childen.isGroup()) {
                    foundNode = childen;
                    sortOrder(foundNode, node);
                    orderNode(childen);
                } else {
                    orderNode(childen);
                }
            }
        }
    }

    private void sortOrder(FOXTreeNode treeNode, FOXTreeNode node) {
        if (node != null) {
            List<FOXTreeNode> children = node.getChildren();
            if (treeNode != null) {
                int tmpOrder = 0;
                int attrNsCount = 0;
                for (FOXTreeNode child : children) {
                    if (child.getOrder() < treeNode.getOrder()) {
                        tmpOrder++;
                    }
                    if (child.isAttribute() || child.isNameSpace()) {
                        attrNsCount++;
                    }
                }
                if (tmpOrder > -1) {
                    int oldOrder = children.indexOf(treeNode);
                    if (oldOrder != -1 && oldOrder != tmpOrder) {
                        node.removeChild(treeNode);
                        if (children.size() > tmpOrder) {
                            node.addChild(tmpOrder - attrNsCount, treeNode);
                        } else {
                            node.addChild(treeNode);
                        }
                    }
                }
            }
        }

    }

    private void initNodeOrder(FOXTreeNode node) {
        if (node == null) {
            return;
        }
        FOXTreeNode parent = node.getParent();
        if (parent == null) {
            node.setOrder(1);
            String path = TreeUtil.getPath(node);
            orderMap.put(path, order);
            order++;
        }
        List<FOXTreeNode> childNode = node.getChildren();
        for (FOXTreeNode child : childNode) {
            child.setOrder(order);
            String path = TreeUtil.getPath(child);
            orderMap.put(path, order);
            order++;
            if (child.getChildren().size() > 0) {
                initNodeOrder(child);
            }
        }

    }

    private int getNodeOrder(FOXTreeNode node) {
        if (node != null) {
            String path = TreeUtil.getPath(node);
            return orderMap.get(path);
        }
        return 0;
    }

    public void tableLoader(Element element, String parentPath, List<XMLFileNode> table, String defaultValue) {
        XMLFileNode xmlFileNode = ConnectionFactory.eINSTANCE.createXMLFileNode();
        String currentPath = parentPath + "/" + element.getLabel();
        xmlFileNode.setXMLPath(currentPath);
        xmlFileNode.setRelatedColumn(element.getColumnLabel());
        xmlFileNode.setAttribute(element.isMain() ? "main" : "branch");
        xmlFileNode.setDefaultValue(defaultValue);
        xmlFileNode.setType(element.getDataType());
        xmlFileNode.setOrder(getNodeOrder(element));
        table.add(xmlFileNode);
        for (FOXTreeNode att : element.getAttributeChildren()) {
            xmlFileNode = ConnectionFactory.eINSTANCE.createXMLFileNode();
            xmlFileNode.setXMLPath(att.getLabel());
            xmlFileNode.setRelatedColumn(att.getColumnLabel());
            xmlFileNode.setAttribute("attri");
            xmlFileNode.setDefaultValue(att.getDefaultValue());
            xmlFileNode.setType(att.getDataType());
            xmlFileNode.setOrder(getNodeOrder(att));
            table.add(xmlFileNode);
        }
        for (FOXTreeNode att : element.getNameSpaceChildren()) {
            xmlFileNode = ConnectionFactory.eINSTANCE.createXMLFileNode();
            xmlFileNode.setXMLPath(att.getLabel());
            xmlFileNode.setRelatedColumn(att.getColumnLabel());
            xmlFileNode.setAttribute("ns");
            xmlFileNode.setDefaultValue(att.getDefaultValue());
            xmlFileNode.setType(att.getDataType());
            xmlFileNode.setOrder(getNodeOrder(att));
            table.add(xmlFileNode);
        }
        List<FOXTreeNode> children = element.getElementChildren();
        for (FOXTreeNode child : children) {
            if (!child.isGroup() && !child.isLoop()) {
                tableLoader((Element) child, currentPath, table, child.getDefaultValue());
            }
        }

    }

    public void setSelectedText(String label) {
        selectedText = label;
    }

    public List<FOXTreeNode> getTreeData() {
        return treeData;
    }

    public XmlFileConnection getConn() {
        return getConnection();
    }

    private boolean existedLoopNode(FOXTreeNode node) {
        if (node != null) {
            if (node.isLoop()) {
                return true;
            }
            for (FOXTreeNode child : node.getChildren()) {
                if (existedLoopNode(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean existedGroupNode(FOXTreeNode node) {
        if (node != null) {
            if (node.isGroup()) {
                return true;
            }
            for (FOXTreeNode child : node.getChildren()) {
                if (existedGroupNode(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (super.isVisible()) {
            initXmlTreeData();
            initSchemaTable();
            xmlViewer.setInput(treeData);
            xmlViewer.expandAll();
            redrawLinkers();
            if (!creation) {
                checkFieldsValue();
            }
        }
    }

    /**
     * class global comment. Detailled comment
     */
    class DialogErrorXMLLabelCellEditor implements ICellEditorListener {

        CellEditor editor;

        String property;

        Boolean validateLabel;

        public void applyEditorValue() {
            String text = getControl().getText();
            onValueChanged(text, true, property);
        }

        public void cancelEditor() {
        }

        public void editorValueChanged(boolean oldValidState, boolean newValidState) {
            onValueChanged(getControl().getText(), false, property);
        }

        private void onValueChanged(final String newValue, boolean showAlertIfError, String property) {
            final Text text = getControl();
            FOXTreeNode selectNode = null;
            ISelection selection = xmlViewer.getSelection();
            if (selection instanceof TreeSelection) {
                Object obj = ((TreeSelection) selection).getFirstElement();
                if (obj instanceof FOXTreeNode) {
                    selectNode = (FOXTreeNode) obj;
                }
            }
            String errorMessage = null;

            if ("C4".equals(property)) { //$NON-NLS-1$
                validateLabel = StringUtil.validateLabelForFixedValue(text.getText());
            }
            if ("C1".equals(property) && selectNode != null && selectNode instanceof NameSpaceNode) {
                validateLabel = StringUtil.validateLabelForNameSpace(text.getText());
            } else {
                validateLabel = StringUtil.validateLabelForXML(text.getText());
            }
            if (!validateLabel) {
                errorMessage = "Invalid string for XML Label. Label was not changed.";
            }

            if (errorMessage == null) {
                text.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_WHITE));
            } else {
                text.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_RED));
                if (showAlertIfError) {
                    text.setText(selectedText);
                    MessageDialog.openError(text.getShell(), "Invalid XML label.", errorMessage);
                }
            }
        }

        public DialogErrorXMLLabelCellEditor(CellEditor editor, String property) {
            super();
            this.property = property;
            this.editor = editor;
        }

        private Text getControl() {
            return (Text) editor.getControl();
        }

    }

    @Override
    public TreeViewer getTreeViewer() {
        return this.xmlViewer;
    }

}
