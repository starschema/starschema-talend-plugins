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
package org.talend.repository.ui.actions.importexport;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.MessageBoxExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.swt.advanced.composite.FilteredCheckboxTree;
import org.talend.core.CorePlugin;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.repository.i18n.Messages;
import org.talend.repository.local.ExportItemUtil;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.ProjectRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.ui.views.CheckboxRepositoryTreeViewer;
import org.talend.repository.ui.views.IRepositoryView;
import org.talend.repository.ui.views.RepositoryContentProvider;
import org.talend.repository.ui.views.RepositoryView;

/**
 * Initialy copied from org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage.
 */
class ExportItemWizardPage extends WizardPage {

    private Button itemFromDirectoryRadio;

    private Text directoryPathField;

    private Button browseDirectoriesButton;

    private Button itemFromArchiveRadio;

    private Text archivePathField;

    private Button browseArchivesButton;

    private String previouslyBrowsedDirectory = ""; //$NON-NLS-1$

    private String previouslyBrowsedArchive = ""; //$NON-NLS-1$

    private static final String[] FILE_EXPORT_MASK = { "*.zip;*.tar;*.tar.gz", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$

    private static final String DESTINATION_FILE = "destinationFile";

    private static final String DIRECTORY_PATH = "directoryPath";

    private static final String ARCHIVE_PATH = "archivePath";

    private String lastPath;

    private CheckboxRepositoryView exportItemsTreeViewer;

    private IRepositoryView repositoryView;

    private IStructuredSelection selection;

    private FilteredCheckboxTree filteredCheckboxTree;

    private Button exportDependencies;

    private TreeViewer viewer;

    Collection<RepositoryNode> repositoryNodes = new ArrayList<RepositoryNode>();

    Set<RepositoryNode> checkedNodes = new HashSet<RepositoryNode>();

    Set<RepositoryNode> allNode = new HashSet<RepositoryNode>();

    protected ExportItemWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName);
        repositoryView = RepositoryView.show();
        this.selection = selection;
        setDescription(Messages.getString("ExportItemWizardPage.description")); //$NON-NLS-1$
        setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_EXPORT_WIZ));
    }

    public void createControl(Composite parent) {
        Composite workArea = new Composite(parent, SWT.NONE);
        setControl(workArea);

        workArea.setLayout(new GridLayout());
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

        createItemRoot(workArea);
        createItemList(workArea);
    }

    private String reloadExportPath(String pathType) {
        if (getDialogSettings() != null) {
            IDialogSettings section = getDialogSettings().getSection(DESTINATION_FILE);
            if (section == null) {
                section = getDialogSettings().addNewSection(DESTINATION_FILE);
            }
            return section.get(pathType);
        }
        return null;
    }

    /**
     * DOC hcw Comment method "createItemList".
     * 
     * @param workArea
     */
    private void createItemList(Composite workArea) {
        Composite itemComposite = new Composite(workArea, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(itemComposite);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(500, 300).applyTo(itemComposite);

        Label label = new Label(itemComposite, SWT.NONE);
        label.setText(Messages.getString("ExportItemWizardPage.labelText")); //$NON-NLS-1$
        GridDataFactory.swtDefaults().span(2, 1).applyTo(label);

        createTreeViewer(itemComposite);

        createSelectionButton(itemComposite);

        exportItemsTreeViewer.refresh();
        // force loading all nodes
        viewer = exportItemsTreeViewer.getViewer();
        viewer.expandAll();
        viewer.collapseAll();
        // expand to level of metadata connection
        viewer.expandToLevel(2);

        addTreeCheckedSelection();
        // if user has select some items in repository view, mark them as checked
        if (!selection.isEmpty()) {
            // for bug 10969
            Set<RepositoryNode> newSelection = new HashSet<RepositoryNode>();
            for (RepositoryNode currentNode : (List<RepositoryNode>) selection.toList()) {
                List<IRepositoryViewObject> objects = null;
                if (currentNode.getContentType() != null && currentNode.getObject() != null
                        && currentNode.getObjectType() != ERepositoryObjectType.FOLDER) {
                    try {
                        objects = exportItemsTreeViewer.getAll(currentNode.getObjectType());
                    } catch (IllegalArgumentException e) {
                        // do nothing
                        objects = new ArrayList<IRepositoryViewObject>();
                    }

                    for (IRepositoryViewObject nodeToSelect : objects) {
                        if (currentNode.getId().equals(((RepositoryNode) nodeToSelect.getRepositoryNode()).getId())) {
                            newSelection.add((RepositoryNode) nodeToSelect.getRepositoryNode());
                        }
                    }
                } else {
                    newSelection.add(currentNode);
                }
            }

            repositoryNodes.addAll(newSelection);
            repositoryNodes.addAll(checkedNodes);

            Set<RepositoryNode> nodes = new HashSet<RepositoryNode>();

            for (RepositoryNode node : repositoryNodes) {
                expandRoot(node);
                expandParent(viewer, node);
                checkElement(node, nodes);
            }

            ((CheckboxTreeViewer) viewer).setCheckedElements(nodes.toArray());
        }
    }

    private void refreshExportDependNodes() {
        checkedNodes.clear();
        Object[] checkedObj = ((CheckboxTreeViewer) viewer).getCheckedElements();
        for (int i = 0; i < checkedObj.length; i++) {
            if (checkedObj[i] instanceof RepositoryNode) {
                RepositoryNode checkedNode = (RepositoryNode) checkedObj[i];
                if (checkedNode != null && !RepositoryNode.NO_ID.equals(checkedNode.getId())) {
                    if (checkedNode.getChildren().isEmpty()) {
                        checkedNodes.add(checkedNode);
                    }
                }
            }
        }
        allNode.clear();
        allNode.addAll(repositoryNodes);
        allNode.addAll(checkedNodes);
    }

    private void checkElement(RepositoryNode node, Set<RepositoryNode> nodes) {
        if (node == null) {
            return;
        }
        ERepositoryObjectType objectType = node.getObjectType();
        Property property = null;
        if (objectType != null) {
            switch (objectType) {
            case METADATA_CON_TABLE:
            case METADATA_CON_VIEW:
            case METADATA_CON_SYNONYM:
            case METADATA_CON_QUERY:
                if (node.getObject() != null) {
                    property = node.getObject().getProperty();
                }
                break;
            }

        }
        if (property != null) {
            RepositoryNode repositoryNode = RepositoryNodeUtilities.getRepositoryNode(property.getId(), false);
            if (repositoryNode != null) {
                nodes.add(repositoryNode);
            }
        } else {
            nodes.add(node);
        }

    }

    private void expandParent(TreeViewer viewer, RepositoryNode node) {
        if (node.getContentType() == ERepositoryObjectType.SVN_ROOT) {
            viewer.setExpandedState(node, true);
        }
        RepositoryNode parent = node.getParent();
        if (parent != null) {
            expandParent(viewer, parent);
            if (ERepositoryObjectType.METADATA_CONNECTIONS.equals(node.getObjectType())) {
                viewer.expandToLevel(node, TreeViewer.ALL_LEVELS);
            } else {
                viewer.setExpandedState(node, true);
            }

        }
    }

    // expand root node for node in metadata , routines , documentation
    private void expandRoot(RepositoryNode node) {
        ERepositoryObjectType objectType = node.getObjectType();

        // for user folders in metadata , routines , documentation
        if (ERepositoryObjectType.FOLDER.equals(objectType)) {
            RepositoryNode rootNode = getParentNodeNotFolder(node);
            objectType = rootNode.getContentType();
        }
        if (objectType == null) {
            objectType = node.getContentType();
        }

        if (objectType != null) {
            switch (objectType) {
            case METADATA_CON_TABLE:
            case METADATA_CON_VIEW:
            case METADATA_CON_SYNONYM:
            case METADATA_CON_QUERY:
            case METADATA_CONNECTIONS:
            case METADATA_FILE_DELIMITED:
            case METADATA_FILE_POSITIONAL:
            case METADATA_FILE_REGEXP:
            case METADATA_FILE_XML:
            case METADATA_FILE_LDIF:
            case METADATA_FILE_EXCEL:
            case METADATA_GENERIC_SCHEMA:
            case METADATA_LDAP_SCHEMA:
            case METADATA_SALESFORCE_SCHEMA:
            case METADATA_WSDL_SCHEMA:
            case METADATA_FILE_EBCDIC:
            case METADATA_FILE_HL7:
            case METADATA_MDMCONNECTION:
            case METADATA_FILE_RULES:
            case METADATA_SAPCONNECTIONS:
            case METADATA_SAP_FUNCTION:
            case METADATA_SAP_IDOC:
            case METADATA_HEADER_FOOTER:
                objectType = ERepositoryObjectType.METADATA;
                break;
            case ROUTINES:
            case SNIPPETS:
                objectType = ERepositoryObjectType.ROUTINES;
                break;
            case DOCUMENTATION:
            case JOB_DOC:
            case JOBLET_DOC:
                objectType = ERepositoryObjectType.DOCUMENTATION;
                break;
            default:
            }
        }
        if (objectType != null) {
            switch (objectType) {
            case METADATA:
                viewer.expandToLevel(((ProjectRepositoryNode) exportItemsTreeViewer.getRoot()).getMetadataNode(), 2);
                break;
            case ROUTINES:
                viewer.expandToLevel(((ProjectRepositoryNode) exportItemsTreeViewer.getRoot()).getCodeNode(), 2);
                break;
            case DOCUMENTATION:
                viewer.expandToLevel(((ProjectRepositoryNode) exportItemsTreeViewer.getRoot()).getDocNode(), 2);
                break;
            }

        }

    }

    private RepositoryNode getParentNodeNotFolder(RepositoryNode node) {
        if (ERepositoryObjectType.FOLDER.equals(node.getObjectType())) {
            return getParentNodeNotFolder(node.getParent());
        } else {
            return node;
        }
    }

    private void addTreeCheckedSelection() {
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                refreshExportDependNodes();
            }

        });
    }

    private void createTreeViewer(Composite itemComposite) {
        filteredCheckboxTree = new FilteredCheckboxTree(itemComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI) {

            @Override
            protected CheckboxTreeViewer doCreateTreeViewer(Composite parent, int style) {
                exportItemsTreeViewer = new CheckboxRepositoryView();
                try {
                    exportItemsTreeViewer.init(repositoryView.getViewSite());
                } catch (PartInitException e) {
                    // e.printStackTrace();
                    ExceptionHandler.process(e);
                }
                exportItemsTreeViewer.createPartControl(parent);
                return (CheckboxTreeViewer) exportItemsTreeViewer.getViewer();
            }

            @Override
            protected void refreshCompleted() {
                getViewer().expandToLevel(3);
                restoreCheckedElements();
            }

            @Override
            protected boolean isNodeCollectable(TreeItem item) {
                Object obj = item.getData();
                if (obj instanceof RepositoryNode) {
                    RepositoryNode node = (RepositoryNode) obj;
                    if (node.getObjectType() == ERepositoryObjectType.METADATA_CONNECTIONS) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * DOC hcw Comment method "createSelectionButton".
     * 
     * @param itemComposite
     */
    private void createSelectionButton(Composite itemComposite) {
        Composite buttonComposite = new Composite(itemComposite, SWT.NONE);
        GridLayoutFactory.swtDefaults().margins(0, 25).applyTo(buttonComposite);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(buttonComposite);

        Button selectAll = new Button(buttonComposite, SWT.PUSH);
        selectAll.setText(DataTransferMessages.DataTransfer_selectAll);
        selectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ((CheckboxTreeViewer) exportItemsTreeViewer.getViewer()).setAllChecked(true);
            }
        });

        setButtonLayoutData(selectAll);

        Button deselectAll = new Button(buttonComposite, SWT.PUSH);
        deselectAll.setText(DataTransferMessages.DataTransfer_deselectAll);
        deselectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ((CheckboxTreeViewer) exportItemsTreeViewer.getViewer()).setAllChecked(false);
            }
        });

        setButtonLayoutData(deselectAll);

        Button expandBtn = new Button(buttonComposite, SWT.PUSH);
        expandBtn.setText(Messages.getString("ExportItemWizardPage.expandBtnText")); //$NON-NLS-1$
        expandBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                exportItemsTreeViewer.getViewer().expandAll();
            }
        });
        setButtonLayoutData(expandBtn);

        Button collapseBtn = new Button(buttonComposite, SWT.PUSH);
        collapseBtn.setText(Messages.getString("ExportItemWizardPage.collapseBtnText")); //$NON-NLS-1$
        collapseBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                exportItemsTreeViewer.getViewer().collapseAll();
            }
        });
        setButtonLayoutData(collapseBtn);
    }

    @SuppressWarnings("restriction")
    private void createItemRoot(Composite workArea) {
        Composite projectGroup = new Composite(workArea, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 0;
        projectGroup.setLayout(layout);
        projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        itemFromDirectoryRadio = new Button(projectGroup, SWT.RADIO);
        itemFromDirectoryRadio.setText(DataTransferMessages.WizardProjectsImportPage_RootSelectTitle);

        this.directoryPathField = new Text(projectGroup, SWT.BORDER);

        this.directoryPathField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        String reloaded = reloadExportPath(DIRECTORY_PATH);
        if (reloaded != null) {
            this.directoryPathField.setText(reloaded);
            this.lastPath = reloaded;
        }

        browseDirectoriesButton = new Button(projectGroup, SWT.PUSH);
        browseDirectoriesButton.setText(DataTransferMessages.DataTransfer_browse);
        setButtonLayoutData(browseDirectoriesButton);

        // new project from archive radio button
        itemFromArchiveRadio = new Button(projectGroup, SWT.RADIO);
        itemFromArchiveRadio.setText(DataTransferMessages.WizardProjectsImportPage_ArchiveSelectTitle);

        // project location entry field
        archivePathField = new Text(projectGroup, SWT.BORDER);

        archivePathField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        if (reloadExportPath(ARCHIVE_PATH) != null) {
            this.archivePathField.setText(reloadExportPath(ARCHIVE_PATH));
        }

        // browse button
        browseArchivesButton = new Button(projectGroup, SWT.PUSH);
        browseArchivesButton.setText(DataTransferMessages.DataTransfer_browse);
        setButtonLayoutData(browseArchivesButton);

        itemFromDirectoryRadio.setSelection(true);
        archivePathField.setEnabled(false);
        browseArchivesButton.setEnabled(false);

        browseDirectoriesButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleLocationDirectoryButtonPressed();
            }
        });

        browseArchivesButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleLocationArchiveButtonPressed();
            }
        });

        directoryPathField.addTraverseListener(new TraverseListener() {

            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                    lastPath = directoryPathField.getText().trim();
                }
            }

        });

        directoryPathField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(org.eclipse.swt.events.FocusEvent e) {
                lastPath = directoryPathField.getText().trim();
            }

        });

        directoryPathField.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                if (getDialogSettings() != null) {
                    IDialogSettings section = getDialogSettings().getSection(DESTINATION_FILE);
                    if (section == null) {
                        section = getDialogSettings().addNewSection(DESTINATION_FILE);
                    }
                    section.put(DIRECTORY_PATH, directoryPathField.getText());
                }
            }

        });

        archivePathField.addTraverseListener(new TraverseListener() {

            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                    lastPath = archivePathField.getText().trim();
                }
            }
        });

        archivePathField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(org.eclipse.swt.events.FocusEvent e) {
                lastPath = archivePathField.getText().trim();
            }
        });

        archivePathField.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                if (getDialogSettings() != null) {
                    IDialogSettings section = getDialogSettings().getSection(DESTINATION_FILE);
                    if (section == null) {
                        section = getDialogSettings().addNewSection(DESTINATION_FILE);
                    }
                    section.put(ARCHIVE_PATH, archivePathField.getText());
                }

            }

        });
        itemFromDirectoryRadio.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                directoryRadioSelected();
            }
        });

        itemFromArchiveRadio.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                archiveRadioSelected();
            }
        });

        exportDependencies = new Button(workArea, SWT.CHECK);
        exportDependencies.setText(Messages.getString("ExportItemWizardPage.exportDependenciesText")); //$NON-NLS-1$
        exportDependencies.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshExportDependNodes();
                viewer.expandAll();
                viewer.collapseAll();
                viewer.expandToLevel(3);
                exportDependenciesSelected();
                allNode.clear();
                if (exportDependencies.getSelection()) {
                    allNode.addAll(checkedNodes);
                } else {
                    repositoryNodes.clear();
                    repositoryNodes.addAll(selection.toList());
                    allNode.addAll(repositoryNodes);
                }
                Set<RepositoryNode> nodes = new HashSet<RepositoryNode>();
                for (RepositoryNode node : allNode) {
                    expandRoot(node);
                    expandParent(viewer, node);
                    checkElement(node, nodes);
                }
                ((CheckboxTreeViewer) viewer).setCheckedElements(nodes.toArray());
            }
        });
    }

    private void checkItemDependencies(Item item, List<IRepositoryViewObject> repositoryObjects) {
        if (item == null) {
            return;
        }
        IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();
        RelationshipItemBuilder builder = RelationshipItemBuilder.getInstance();

        List<RelationshipItemBuilder.Relation> relations = builder.getItemsRelatedTo(item.getProperty().getId(), item
                .getProperty().getVersion(), RelationshipItemBuilder.JOB_RELATION);
        for (RelationshipItemBuilder.Relation relation : relations) {
            IRepositoryViewObject obj = null;
            try {
                if (RelationshipItemBuilder.ROUTINE_RELATION.equals(relation.getType())) {
                    obj = RoutinesUtil.getRoutineFromName(relation.getId());
                } else {
                    obj = factory.getLastVersion(relation.getId());
                }
                if (obj != null) {
                    RepositoryNode repositoryNode = RepositoryNodeUtilities.getRepositoryNode(obj, false);
                    if (repositoryNode != null) {
                        if (!repositoryObjects.contains(obj)) {
                            repositoryObjects.add(obj);
                            checkAllVerSionLatest(repositoryObjects, obj);
                            checkItemDependencies(obj.getProperty().getItem(), repositoryObjects);
                        }
                    }
                }
            } catch (PersistenceException et) {
                ExceptionHandler.process(et);
            }
        }
    }

    /**
     * DOC qwei Comment method "exportDependenciesSelected".
     */
    private void exportDependenciesSelected() {
        final Collection<Item> selectedItems = getSelectedItems();

        // addTreeCheckedSelection();

        IRunnableWithProgress runnable = new IRunnableWithProgress() {

            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitor.beginTask("Dependencies", 100);//$NON-NLS-1$
                monitor.setCanceled(false);
                //
                final List<IRepositoryViewObject> repositoryObjects = new ArrayList<IRepositoryViewObject>();

                ProcessUtils.clearFakeProcesses();

                // dependencies All
                Display.getDefault().syncExec(new Runnable() {

                    public void run() {
                        for (Item item : selectedItems) {
                            checkItemDependencies(item, repositoryObjects);
                        }
                    }
                });
                monitor.worked(60);

                //
                Display.getDefault().syncExec(new Runnable() {

                    public void run() {
                        if (exportDependencies.getSelection()) {
                            for (IRepositoryViewObject repositoryObject : repositoryObjects) {
                                RepositoryNode repositoryNode = RepositoryNodeUtilities
                                        .getRepositoryNode(repositoryObject, false);
                                if (repositoryNode != null && !repositoryNodes.contains(repositoryNode)) {
                                    repositoryNodes.add(repositoryNode);
                                    checkedNodes.add(repositoryNode);
                                }

                            }
                        } else {
                            for (IRepositoryViewObject repositoryObject : repositoryObjects) {
                                RepositoryNode repositoryNode = RepositoryNodeUtilities
                                        .getRepositoryNode(repositoryObject, false);
                                if (repositoryNode != null && repositoryNodes.contains(repositoryNode)) {
                                    repositoryNodes.remove(repositoryNode);
                                    checkedNodes.remove(repositoryNode);
                                }
                            }
                        }
                    }
                });
                monitor.worked(90);
                // selection
                Display.getDefault().syncExec(new Runnable() {

                    public void run() {
                        CheckboxTreeViewer viewer = (CheckboxTreeViewer) exportItemsTreeViewer.getViewer();
                        Set<RepositoryNode> nodes = new HashSet<RepositoryNode>();
                        nodes.addAll(repositoryNodes);
                        nodes.addAll(checkedNodes);
                        viewer.setCheckedElements(nodes.toArray());

                    }
                });
                ProcessUtils.clearFakeProcesses();
                monitor.done();
            }

        };
        final ProgressMonitorJobsDialog dialog = new ProgressMonitorJobsDialog(getShell());
        try {
            dialog.run(true, false, runnable);
        } catch (InvocationTargetException e) {
            //
        } catch (InterruptedException e) {
            //
        }

    }

    private void checkAllVerSionLatest(List<IRepositoryViewObject> repositoryObjects, IRepositoryViewObject object) {
        IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();
        RelationshipItemBuilder builder = RelationshipItemBuilder.getInstance();
        if (object.getRepositoryNode() != null) {
            List<RelationshipItemBuilder.Relation> relations = builder.getItemsJobRelatedTo(object.getId(), object.getVersion(),
                    RelationshipItemBuilder.JOB_RELATION);
            for (RelationshipItemBuilder.Relation relation : relations) {
                try {
                    IRepositoryViewObject obj = factory.getLastVersion(relation.getId());
                    if (obj != null) {
                        if (!repositoryObjects.contains(obj)) {
                            repositoryObjects.add(obj);
                            checkAllVerSionLatest(repositoryObjects, obj);
                        }
                    }
                } catch (PersistenceException et) {
                    ExceptionHandler.process(et);
                }
            }
        }
    }

    private void archiveRadioSelected() {
        if (itemFromArchiveRadio.getSelection()) {
            directoryPathField.setEnabled(false);
            browseDirectoriesButton.setEnabled(false);
            archivePathField.setEnabled(true);
            browseArchivesButton.setEnabled(true);
            lastPath = archivePathField.getText().trim();
            archivePathField.setFocus();
        }
    }

    @SuppressWarnings("restriction")
    protected void handleLocationDirectoryButtonPressed() {

        DirectoryDialog dialog = new DirectoryDialog(directoryPathField.getShell());
        dialog.setMessage(DataTransferMessages.FileExport_selectDestinationTitle);

        String dirName = directoryPathField.getText().trim();
        if (dirName.length() == 0) {
            dirName = previouslyBrowsedDirectory;
        }

        if (dirName.length() == 0) {
            dialog.setFilterPath(IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getLocation().toOSString());
        } else {
            File path = new File(dirName);
            if (path.exists()) {
                dialog.setFilterPath(new Path(dirName).toOSString());
            }
        }

        String selectedDirectory = dialog.open();
        if (selectedDirectory != null) {
            previouslyBrowsedDirectory = selectedDirectory;
            directoryPathField.setText(previouslyBrowsedDirectory);
            lastPath = directoryPathField.getText().trim();
            saveExportPath(DIRECTORY_PATH);

        }

    }

    /**
     * The browse button has been selected. Select the location.
     */
    @SuppressWarnings("restriction")
    protected void handleLocationArchiveButtonPressed() {

        FileDialog dialog = new FileDialog(archivePathField.getShell(), SWT.SAVE);
        dialog.setFilterExtensions(FILE_EXPORT_MASK);
        dialog.setText(DataTransferMessages.ArchiveExport_selectDestinationTitle);

        String fileName = archivePathField.getText().trim();
        if (fileName.length() == 0) {
            fileName = previouslyBrowsedArchive;
        }

        if (fileName.length() == 0) {
            dialog.setFilterPath(IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getLocation().toOSString());
        } else {
            File path = new File(fileName);
            if (path.exists()) {
                dialog.setFilterPath(new Path(fileName).toOSString());
            }
        }

        String selectedArchive = dialog.open();
        if (selectedArchive != null) {
            previouslyBrowsedArchive = selectedArchive;
            archivePathField.setText(previouslyBrowsedArchive);
            lastPath = archivePathField.getText().trim();
            saveExportPath(ARCHIVE_PATH);

        }

    }

    private void saveExportPath(String pathType) {
        if (getDialogSettings() != null) {
            IDialogSettings section = getDialogSettings().getSection(DESTINATION_FILE);
            if (section == null) {
                section = getDialogSettings().addNewSection(DESTINATION_FILE);
            }
            section.put(pathType, lastPath);
        }
    }

    private void directoryRadioSelected() {
        if (itemFromDirectoryRadio.getSelection()) {
            directoryPathField.setEnabled(true);
            browseDirectoriesButton.setEnabled(true);
            archivePathField.setEnabled(false);
            browseArchivesButton.setEnabled(false);
            lastPath = directoryPathField.getText().trim();
            directoryPathField.setFocus();
        }
    }

    public boolean performFinish() {
        if (!checkExportFile()) {
            return false;
        }

        Collection<Item> selectedItems = getSelectedItems();
        try {
            ExportItemUtil exportItemUtil = new ExportItemUtil();
            // MOD sgandon 31/03/2010 bug 12229: moved getAllVersion into ExportItemUtil.exportitems() method.
            exportItemUtil.exportItems(new File(lastPath), selectedItems, true, new NullProgressMonitor());

        } catch (Exception e) {
            MessageBoxExceptionHandler.process(e);
        } finally {
            if (exportItemsTreeViewer != null) {
                exportItemsTreeViewer.dispose();
            }
        }
        return true;
    }

    private boolean checkExportFile() {
        if (lastPath == null || "".equals(lastPath.trim())) {//$NON-NLS-1$
            MessageDialog.openError(getShell(), "Error", "Must input the export path or archive file.");//$NON-NLS-1$//$NON-NLS-2$
            return false;
        }
        return true;
    }

    private static boolean isRepositoryFolder(RepositoryNode node) {
        final ENodeType type = node.getType();
        if (type == ENodeType.SIMPLE_FOLDER || type == ENodeType.STABLE_SYSTEM_FOLDER || type == ENodeType.SYSTEM_FOLDER) {
            return true;
        }
        return false;
    }

    /**
     * Get all selected items to export.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private Collection<Item> getSelectedItems() {
        // add this if user use filter
        Set checkedElements = new HashSet();
        for (Object obj : filteredCheckboxTree.getCheckedLeafNodes()) {
            checkedElements.add(obj);
        }

        // add this if user does not use filter
        for (Object obj : filteredCheckboxTree.getViewer().getCheckedElements()) {
            RepositoryNode repositoryNode = (RepositoryNode) obj;
            if (!isRepositoryFolder(repositoryNode) && !(repositoryNode instanceof ProjectRepositoryNode)) {
                checkedElements.add(obj);
            }
        }

        Object[] elements = checkedElements.toArray();

        Map<String, Item> items = new HashMap<String, Item>();
        collectNodes(items, elements);
        return new ArrayList<Item>(items.values());
    }

    @SuppressWarnings("unchecked")
    private void collectNodes(Map<String, Item> items, Object[] objects) {
        for (int i = 0; i < objects.length; i++) {
            RepositoryNode repositoryNode = (RepositoryNode) objects[i];
            collectNodes(items, repositoryNode);
        }
    }

    private void collectNodes(Map<String, Item> items, RepositoryNode repositoryNode) {
        IRepositoryViewObject repositoryObject = repositoryNode.getObject();
        if (repositoryObject != null) {
            if (repositoryObject.getType().isResourceItem()) {
                Item item = repositoryObject.getProperty().getItem();
                items.put(item.getProperty().getId(), item);
            }
        } else {
            if (repositoryNode.getParent() != null && repositoryNode.getParent().getObject() != null) {
                Item item = repositoryNode.getParent().getObject().getProperty().getItem();
                items.put(item.getProperty().getId(), item);
            }
        }
        RepositoryContentProvider repositoryContentProvider = (RepositoryContentProvider) RepositoryView.show().getViewer()
                .getContentProvider();
        collectNodes(items, repositoryContentProvider.getChildren(repositoryNode));
    }

    public boolean performCancel() {
        if (exportItemsTreeViewer != null) {
            exportItemsTreeViewer.dispose();
        }
        return true;
    }

    /**
     * DOC hywang ExportViewProvider constructor comment. This provider only using for export wizard page It will hide
     * the tables nodes of file connection and database connection when export item wizard is open
     * 
     * @param view
     */
    class ExportViewProvider extends RepositoryContentProvider {

        public ExportViewProvider(IRepositoryView view) {
            super(view);
        }

        @Override
        public Object[] getChildren(Object parent) {
            RepositoryNode repositoryNode = ((RepositoryNode) parent);
            if (repositoryNode.getObjectType() != null) {
                switch (repositoryNode.getObjectType()) {
                case METADATA_CONNECTIONS:
                case METADATA_FILE_BRMS:
                case METADATA_FILE_DELIMITED:
                case METADATA_FILE_EBCDIC:
                case METADATA_FILE_EXCEL:
                case METADATA_FILE_FTP:
                case METADATA_FILE_HL7:
                case METADATA_FILE_LDIF:
                case METADATA_FILE_POSITIONAL:
                case METADATA_FILE_REGEXP:
                case METADATA_FILE_XML:
                    return new Object[0];
                }
            }

            if (!repositoryNode.isInitialized()) {
                if (repositoryNode.getParent() instanceof ProjectRepositoryNode) {
                    // initialize repository from main project
                    ((ProjectRepositoryNode) repositoryNode.getParent()).initializeChildren(parent);
                }
                repositoryNode.setInitialized(true);
            }

            return repositoryNode.getChildren().toArray();
        }

    }

    /**
     * 
     * A repository view with checkbox on the left.
     */
    class CheckboxRepositoryView extends RepositoryView {

        @Override
        protected TreeViewer createTreeViewer(Composite parent) {
            return new CheckboxRepositoryTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.talend.repository.ui.views.RepositoryView#createPartControl(org.eclipse.swt.widgets.Composite)
         */
        @Override
        public void createPartControl(Composite parent) {
            super.createPartControl(parent);
            CorePlugin.getDefault().getRepositoryService().removeRepositoryChangedListener(this);
        }

        @Override
        protected void setContentProviderForView() {
            ExportViewProvider contentProvider = new ExportViewProvider(repositoryView);
            viewer.setContentProvider(contentProvider);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.talend.repository.ui.views.RepositoryView#refresh(java.lang.Object)
         */
        @Override
        public void refresh(Object object) {
            refresh();
            if (object != null) {
                getViewer().expandToLevel(object, AbstractTreeViewer.ALL_LEVELS);
            }
        }

        @Override
        protected void makeActions() {
        }

        @Override
        protected void hookContextMenu() {
        }

        @Override
        protected void contributeToActionBars() {
        }

        @Override
        protected void initDragAndDrop() {
        }

        @Override
        protected void hookDoubleClickAction() {
        }

        @Override
        public void addFilters() {
        }

        @Override
        public void createActionComposite(Composite parent) {
        }

    }
}
