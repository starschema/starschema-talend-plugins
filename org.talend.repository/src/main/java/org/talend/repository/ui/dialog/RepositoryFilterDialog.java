// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PartInitException;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.PropertiesPackage;
import org.talend.core.model.properties.Status;
import org.talend.core.model.properties.User;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryPrefConstants;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.ProjectRepositoryNode;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.ui.views.CheckboxRepositoryTreeViewer;
import org.talend.repository.ui.views.IRepositoryView;
import org.talend.repository.ui.views.RepositoryCheckBoxView;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class RepositoryFilterDialog extends Dialog {

    private static final int WIDTH = 550;

    private static final int HEIGHT = 500;

    public static final String SEPARATOR = ":";

    private IRepositoryView repositoryView;

    private CheckboxRepositoryTreeViewer treeViewer;

    private Table statusTable;

    private Table userTable;

    private Text userFilterPattern;

    private Label patternInfo;

    private Button enableUserPatternBtn;

    private Button allUsersBtn;

    private Set<String> uncheckedNode = new HashSet<String>();

    private Set<String> uncheckedStatus = new HashSet<String>();

    private Set<String> uncheckedUser = new HashSet<String>();

    /**
     * DOC wchen RepositoryFilterDialog constructor comment.
     * 
     * @param parentShell
     */
    public RepositoryFilterDialog(IRepositoryView view) {
        super(view.getViewSite().getShell());
        setShellStyle(getShellStyle() | SWT.MAX | SWT.MIN | SWT.RESIZE | SWT.APPLICATION_MODAL);
        this.repositoryView = view;

    }

    @Override
    protected Control createDialogArea(Composite parent) {
        SashForm sash = new SashForm(parent, SWT.HORIZONTAL | SWT.SMOOTH);
        sash.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        // layout.numColumns = 2;
        sash.setLayout(layout);
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.widthHint = WIDTH;
        layoutData.heightHint = HEIGHT;
        sash.setLayoutData(layoutData);
        applyDialogFont(sash);

        Composite leftPart = new Composite(sash, SWT.NONE);
        leftPart.setLayout(new GridLayout());
        leftPart.setLayoutData(new GridData(GridData.FILL_BOTH));
        createLeftContent(leftPart);

        Composite rightPart = new Composite(sash, SWT.NONE);
        rightPart.setLayout(new GridLayout());
        rightPart.setLayoutData(new GridData(GridData.FILL_BOTH));
        createRightContent(rightPart);
        addListeners();
        sash.setWeights(new int[] { 180, 250 });
        return sash;
    }

    private void createLeftContent(Composite parent) {
        RepositoryCheckBoxView checkboxTreeViewer = new RepositoryCheckBoxView();
        try {
            checkboxTreeViewer.init(repositoryView.getViewSite());
        } catch (PartInitException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        checkboxTreeViewer.createPartControl(parent);

        treeViewer = (CheckboxRepositoryTreeViewer) checkboxTreeViewer.getViewer();
        treeViewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                return filterRepositoryNode(element);
            }

        });
        treeViewer.expandAll();
        // treeViewer.collapseAll();
        RepositoryNode referenceProjectNode = ((ProjectRepositoryNode) checkboxTreeViewer.getRoot()).getReferenceProjectNode();
        if (referenceProjectNode != null) {
            treeViewer.collapseToLevel(referenceProjectNode, treeViewer.ALL_LEVELS);
        }
        String[] uncheckedNodesFromFilter = RepositoryManager.getFiltersByPreferenceKey(IRepositoryPrefConstants.FILTER_BY_NODE);
        if (uncheckedNodesFromFilter != null && uncheckedNodesFromFilter.length > 0) {
            uncheckedNode.addAll(Arrays.asList(uncheckedNodesFromFilter));
        }

        restoreTreeStatus(checkboxTreeViewer);
    }

    private void createRightContent(Composite parent) {
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        org.talend.core.model.properties.Project emfProject = currentProject.getEmfProject();
        Resource resource = emfProject.eResource();
        Collection<User> users = new ArrayList<User>();
        if (resource != null) {
            users = EcoreUtil.getObjectsByType(resource.getContents(), PropertiesPackage.eINSTANCE.getUser());
        }
        // if resource is null, add current user
        if (users.size() == 0) {
            users.add(emfProject.getAuthor());
        }
        // filter by Name
        Composite userPatternComp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        userPatternComp.setLayout(layout);
        enableUserPatternBtn = new Button(userPatternComp, SWT.CHECK);
        boolean enabled = RepositoryManager.getPreferenceStore().getBoolean(
                IRepositoryPrefConstants.TAG_USER_DEFINED_PATTERNS_ENABLED);
        enableUserPatternBtn.setSelection(enabled);
        Label filterByName = new Label(userPatternComp, SWT.LEFT | SWT.WRAP);
        filterByName.setText("Filter By Name : ");
        filterByName.setSize(20, 150);

        String patterns = RepositoryManager.getPreferenceStore().getString(IRepositoryPrefConstants.FILTER_BY_NAME);
        userFilterPattern = new Text(parent, SWT.BORDER);
        userFilterPattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        userFilterPattern.setEnabled(enabled);
        if (patterns != null) {
            userFilterPattern.setText(patterns);
        }

        patternInfo = new Label(parent, SWT.NONE);
        patternInfo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        patternInfo.setText("The patterns are separated by comma, where\n* = any string, ? = any character, ,, = ,");
        patternInfo.setEnabled(enabled);

        // filter by user
        Composite userTop = new Composite(parent, SWT.NONE);
        userTop.setLayout(new GridLayout(2, false));
        userTop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label filterByUser = new Label(userTop, SWT.NONE);
        filterByUser.setText("Filter By User :");

        allUsersBtn = new Button(userTop, SWT.CHECK | SWT.LEFT);
        allUsersBtn.setText("All Users");
        boolean userTableEnable = RepositoryManager.getPreferenceStore().getBoolean(
                IRepositoryPrefConstants.USER_FILTER_TABLE_ENABLED);
        allUsersBtn.setSelection(!userTableEnable);

        userTable = new Table(parent, SWT.BORDER | SWT.CHECK);
        userTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        userTable.setHeaderVisible(true);
        userTable.setLinesVisible(true);
        userTable.setEnabled(!allUsersBtn.getSelection());

        TableColumn login = new TableColumn(userTable, SWT.NONE);
        login.setWidth(100);
        login.setText("Login");

        TableColumn firstName = new TableColumn(userTable, SWT.NONE);
        firstName.setWidth(100);
        firstName.setText("FirstName");

        TableColumn lastName = new TableColumn(userTable, SWT.NONE);
        lastName.setWidth(100);
        lastName.setText("LastName");

        String[] filtersByPreferenceKey = RepositoryManager.getFiltersByPreferenceKey(IRepositoryPrefConstants.FILTER_BY_USER);
        if (filtersByPreferenceKey != null && filtersByPreferenceKey.length > 0) {
            uncheckedUser.addAll(Arrays.asList(filtersByPreferenceKey));
        }
        for (User user : users) {
            TableItem item = new TableItem(userTable, SWT.NONE);
            item.setText(0, user.getLogin());
            item.setText(1, user.getFirstName() == null ? "" : user.getFirstName());
            item.setText(2, user.getLastName() == null ? "" : user.getLastName());
            if (!uncheckedUser.contains(user.getLogin())) {
                item.setChecked(true);
            }
        }

        // filter by status
        Label filterByStatus = new Label(parent, SWT.NONE);
        filterByStatus.setText("Filter By Status :");
        EList technicalStatus = emfProject.getTechnicalStatus();
        statusTable = new Table(parent, SWT.BORDER | SWT.CHECK);
        statusTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        statusTable.setHeaderVisible(true);
        statusTable.setLinesVisible(true);

        TableColumn code = new TableColumn(statusTable, SWT.NONE);
        code.setWidth(100);
        code.setText("Code");

        TableColumn lable = new TableColumn(statusTable, SWT.NONE);
        lable.setWidth(100);
        lable.setText("Label");
        String[] filters = RepositoryManager.getFiltersByPreferenceKey(IRepositoryPrefConstants.FILTER_BY_STATUS);
        if (filters != null && filters.length > 0) {
            uncheckedStatus.addAll(Arrays.asList(filters));
        }
        for (Object o : technicalStatus) {
            Status status = (Status) o;
            TableItem item = new TableItem(statusTable, SWT.NONE);
            item.setText(0, status.getCode());
            item.setText(1, status.getLabel());
            if (!uncheckedStatus.contains(status.getCode())) {
                item.setChecked(true);
            }

        }
        TableItem item = new TableItem(statusTable, SWT.NONE);
        item.setText(1, "not set status");
        item.setData(RepositoryConstants.NOT_SET_STATUS, RepositoryConstants.NOT_SET_STATUS);
        if (!uncheckedStatus.contains(RepositoryConstants.NOT_SET_STATUS)) {
            item.setChecked(true);
        }
    }

    private boolean filterRepositoryNode(Object element) {
        if (!(element instanceof RepositoryNode)) {
            return false;
        }

        RepositoryNode node = (RepositoryNode) element;
        if (node.isBin()) {
            return false;
        }
        ENodeType nodeType = node.getType();
        boolean visible = false;
        if (nodeType != null) {
            switch (nodeType) {
            case SYSTEM_FOLDER:
                if (!ERepositoryObjectType.ROUTINES.equals(node.getContentType())) {
                    visible = true;
                }
                break;
            case STABLE_SYSTEM_FOLDER:
                // Generic/system unvisible
                if (node.getParent() != null && node.getParent().getObjectType() == null
                        && !ERepositoryObjectType.DOCUMENTATION.equals(node.getParent().getContentType())) {
                    visible = true;
                }
                break;
            case SIMPLE_FOLDER:
                // Generic visible ,user defined unvisible
                if (ERepositoryObjectType.SQLPATTERNS.equals(node.getParent().getContentType())
                        && node.getParent().getObjectType() == null) {
                    visible = true;
                }
                break;
            case REFERENCED_PROJECT:
                visible = true;
                break;
            default:
                visible = false;
                break;
            }
        }

        return visible;

    }

    private void restoreTreeStatus(RepositoryCheckBoxView viewer) {
        CheckboxRepositoryTreeViewer checkboxTreeViewer = (CheckboxRepositoryTreeViewer) viewer.getViewer();
        checkboxTreeViewer.setAllChecked(true);

        String[] array = RepositoryManager.getFiltersByPreferenceKey(IRepositoryPrefConstants.FILTER_BY_NODE);
        if (array == null) {
            return;
        }
        for (String uniqueSymble : array) {
            String[] split = uniqueSymble.split(SEPARATOR);
            if (split.length < 2) {
                continue;
            }
            ProjectRepositoryNode root = getRootNode((ProjectRepositoryNode) viewer.getRoot(), split[0]);
            if (root == null) {
                continue;
            }
            if (split.length == 2) {
                if (ERepositoryObjectType.BUSINESS_PROCESS.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getBusinessProcessNode(), false);
                } else if (ERepositoryObjectType.PROCESS.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getProcessNode(), false);
                } else if (ERepositoryObjectType.JOBLET.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getJobletNode(), false);
                } else if (ERepositoryObjectType.CONTEXT.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getContextNode(), false);
                } else if (ERepositoryObjectType.ROUTINES.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getCodeNode(), false);
                } else if (ERepositoryObjectType.SQLPATTERNS.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getSQLPatternNode(), false);
                } else if (ERepositoryObjectType.METADATA.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getMetadataNode(), false);
                } else if (ERepositoryObjectType.METADATA_CONNECTIONS.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getMetadataConNode(), false);
                } else if (ERepositoryObjectType.METADATA_SAPCONNECTIONS.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getMetadataSAPConnectionNode(), false);
                } else if (ERepositoryObjectType.METADATA_FILE_DELIMITED.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getMetadataFileNode(), false);
                } else if (ERepositoryObjectType.METADATA_FILE_POSITIONAL.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getMetadataFilePositionalNode(), false);
                } else if (ERepositoryObjectType.METADATA_FILE_REGEXP.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getMetadataFileRegexpNode(), false);
                } else if (ERepositoryObjectType.METADATA_FILE_XML.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getMetadataFileXmlNode(), false);
                } else if (ERepositoryObjectType.METADATA_FILE_EXCEL.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getMetadataFileExcelNode(), false);
                } else if (ERepositoryObjectType.METADATA_FILE_LDIF.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getMetadataFileLdifNode(), false);
                } else if (ERepositoryObjectType.METADATA_LDAP_SCHEMA.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getMetadataLDAPSchemaNode(), false);
                } else if (ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getMetadataSalesforceSchemaNode(), false);
                } else if (ERepositoryObjectType.METADATA_GENERIC_SCHEMA.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getMetadataGenericSchemaNode(), false);
                } else if (ERepositoryObjectType.METADATA_FILE_EBCDIC.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getMetadataEbcdicConnectionNode(), false);
                } else if (ERepositoryObjectType.METADATA_FILE_RULES.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getMetadataRulesNode(), false);
                } else if (ERepositoryObjectType.METADATA_FILE_BRMS.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getMetadataBRMSConnectionNode(), false);
                } else if (ERepositoryObjectType.METADATA_WSDL_SCHEMA.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getMetadataWSDLSchemaNode(), false);
                } else if (ERepositoryObjectType.DOCUMENTATION.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getDocNode(), false);
                } else if (ERepositoryObjectType.REFERENCED_PROJECTS.name().equals(split[1])) {
                    checkboxTreeViewer.setChecked(root.getReferenceProjectNode(), false);
                }
            } else if (split.length == 3) {
                // child in sql pattern
                if (ERepositoryObjectType.SQLPATTERNS.name().equals(split[1])) {
                    for (IRepositoryNode node : root.getSQLPatternNode().getChildren()) {
                        if (split[2] != null && split[2].equals(node.getProperties(EProperties.LABEL))) {
                            checkboxTreeViewer.setChecked(node, false);
                        }
                    }
                    // referenced ProjectRepositoryNode :root
                } else if (ERepositoryObjectType.REFERENCED_PROJECTS.name().equals(split[1]) && "ROOT".equals(split[2])) {
                    checkboxTreeViewer.setChecked(root, false);
                }
            }
        }

    }

    private ProjectRepositoryNode getRootNode(ProjectRepositoryNode mainRoot, String projectName) {
        if (mainRoot.getProject().getEmfProject().getTechnicalLabel().equals(projectName)) {
            return mainRoot;
        } else {
            RepositoryNode referenceProjectNode = mainRoot.getReferenceProjectNode();
            if (referenceProjectNode != null) {
                for (IRepositoryNode node : referenceProjectNode.getChildren()) {
                    ProjectRepositoryNode rootNode = getRootNode((ProjectRepositoryNode) node, projectName);
                    if (rootNode == null) {
                        continue;
                    }
                    return rootNode;
                }
            }
        }
        return null;
    }

    private void addListeners() {
        treeViewer.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                Set<String> all = new HashSet<String>();
                TreeItem lastClickedItem = treeViewer.getLastClickedItem();
                if (lastClickedItem != null) {
                    updateChildrenItems(lastClickedItem, all);
                    if (event.getChecked()) {
                        updateParentItems(lastClickedItem, all, true);
                        uncheckedNode.removeAll(all);
                    } else {
                        updateParentItems(lastClickedItem, all, false);
                        uncheckedNode.addAll(all);
                    }
                }
            }
        });
        statusTable.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.detail == SWT.CHECK) {
                    TableItem item = (TableItem) e.item;
                    String text = item.getText(0);
                    if (text == null || "".equals(text)) {
                        Object data = item.getData(RepositoryConstants.NOT_SET_STATUS);
                        if (data != null && RepositoryConstants.NOT_SET_STATUS.equals(data)) {
                            text = data.toString();
                        }
                    }
                    if (!item.getChecked()) {
                        uncheckedStatus.add(text);
                    } else {
                        uncheckedStatus.remove(text);
                    }
                }
            }

        });

        userTable.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.detail == SWT.CHECK) {
                    TableItem item = (TableItem) e.item;
                    if (!item.getChecked()) {
                        uncheckedUser.add(item.getText(0));
                    } else {
                        uncheckedUser.remove(item.getText(0));
                    }
                }
            }
        });
        enableUserPatternBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                boolean selection = enableUserPatternBtn.getSelection();
                patternInfo.setEnabled(selection);
                userFilterPattern.setEnabled(selection);
                if (selection) {
                    userFilterPattern.setFocus();
                }
            }

        });

        allUsersBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                userTable.setEnabled(!allUsersBtn.getSelection());
                if (allUsersBtn.getSelection()) {
                    uncheckedUser.clear();
                }
            }

        });
    }

    private void updateChildrenItems(TreeItem parent, Set<String> set) {
        RepositoryNode data = (RepositoryNode) parent.getData();
        if (filterRepositoryNode(data)) {
            String uniqueSymbol = getUniqueSymbol(data);
            if (uniqueSymbol != null) {
                set.add(uniqueSymbol);
                Item[] children = parent.getItems();
                boolean state = parent.getChecked();
                for (int i = 0; i < children.length; i++) {
                    TreeItem curr = (TreeItem) children[i];
                    data = (RepositoryNode) curr.getData();
                    if (filterRepositoryNode(data)) {
                        uniqueSymbol = getUniqueSymbol(data);
                        if (uniqueSymbol != null) {
                            set.add(uniqueSymbol);
                        }
                    }
                    updateChildrenItems(curr, set);
                }
            }

        }
    }

    private void updateParentItems(TreeItem item, Set<String> set, boolean check) {
        if (item != null) {
            RepositoryNode data = (RepositoryNode) item.getData();
            Item[] children = item.getItems();

            if (check && filterRepositoryNode(data)) {
                String uniqueSymbol = getUniqueSymbol(data);
                if (uniqueSymbol != null) {
                    set.add(uniqueSymbol);
                }
            } else if (!check && filterRepositoryNode(data)) {
                boolean containsChecked = false;
                for (int i = 0; i < children.length; i++) {
                    TreeItem curr = (TreeItem) children[i];
                    containsChecked |= curr.getChecked();
                }
                if (!containsChecked) {
                    String uniqueSymbol = getUniqueSymbol(data);
                    if (uniqueSymbol != null) {
                        set.add(uniqueSymbol);
                    }
                }

            }
            updateParentItems(item.getParentItem(), set, check);
        }
    }

    private String getUniqueSymbol(RepositoryNode node) {
        String technicalLabel = node.getRoot().getProject().getEmfProject().getTechnicalLabel();
        String uniqueSymbol = technicalLabel + SEPARATOR;
        // sql patterns like Generic ,Mysql
        if (node.getContentType() != null && ERepositoryObjectType.SQLPATTERNS.equals(node.getContentType())
                && node.getId() != "-1") {
            uniqueSymbol = uniqueSymbol + node.getContentType().name() + SEPARATOR + node.getProperties(EProperties.LABEL);
        } else {
            uniqueSymbol = uniqueSymbol + node.getContentType().name();
            if (node instanceof ProjectRepositoryNode) {
                uniqueSymbol = uniqueSymbol + SEPARATOR + "ROOT";//$NON-NLS-1$
            }
        }
        return uniqueSymbol;
    }

    @Override
    protected void okPressed() {
        IPreferenceStore preferenceStore = RepositoryManager.getPreferenceStore();

        String relust = convertToString(uncheckedNode, RepositoryManager.ITEM_SEPARATOR);
        preferenceStore.setValue(IRepositoryPrefConstants.FILTER_BY_NODE, relust.length() > 2 ? relust.substring(0, relust
                .length() - 2) : relust);

        String status = convertToString(uncheckedStatus, RepositoryManager.ITEM_SEPARATOR);
        preferenceStore.setValue(IRepositoryPrefConstants.FILTER_BY_STATUS, status.length() > 2 ? status.substring(0, status
                .length() - 2) : status);

        String users = convertToString(uncheckedUser, RepositoryManager.ITEM_SEPARATOR);
        preferenceStore.setValue(IRepositoryPrefConstants.FILTER_BY_USER, users.length() > 2 ? users.substring(0,
                users.length() - 2) : users);

        boolean canUserFilterEnable = this.userFilterPattern.getText() != null && !"".equals(this.userFilterPattern.getText());
        preferenceStore.setValue(IRepositoryPrefConstants.FILTER_BY_NAME, this.userFilterPattern.getText());
        preferenceStore.setValue(IRepositoryPrefConstants.TAG_USER_DEFINED_PATTERNS_ENABLED, this.enableUserPatternBtn
                .getSelection()
                && canUserFilterEnable);

        preferenceStore.setValue(IRepositoryPrefConstants.USER_FILTER_TABLE_ENABLED, !allUsersBtn.getSelection());
        super.okPressed();
    }

    private String convertToString(Collection<String> connection, String separator) {
        StringBuffer buffer = new StringBuffer();
        for (String item : connection) {
            buffer.append(item);
            buffer.append(separator);
        }
        return buffer.toString();
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Repository Filter");
    }

}
