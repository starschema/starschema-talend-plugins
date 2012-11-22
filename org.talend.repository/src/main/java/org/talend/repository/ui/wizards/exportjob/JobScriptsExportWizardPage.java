// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.wizards.exportjob;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.IRepositoryPrefConstants;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ProcessTypeImpl;
import org.talend.repository.ProjectManager;
import org.talend.repository.documentation.ArchiveFileExportOperationFullPath;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.documentation.FileSystemExporterFullPath;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.utils.ZipToFile;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.action.JobExportAction;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.petals.PetalsJobJavaScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.petals.PetalsTemporaryOptionsKeeper;
import org.talend.repository.utils.JobVersionUtils;

/**
 * Page of the Job Scripts Export Wizard. <br/>
 * 
 * @referto WizardArchiveFileResourceExportPage1 $Id: JobScriptsExportWizardPage.java 1 2006-12-13 下午03:09:07 bqian
 * 
 */
public abstract class JobScriptsExportWizardPage extends WizardFileSystemResourceExportPage1 {

    protected static final String DESTINATION_FILE = "destinationFile";//$NON-NLS-1$

    protected static final String ESB_EXPORT_TYPE = "esbExportType";//$NON-NLS-1$

    protected static final String ESB_SERVICE_NAME = "serviceName";//$NON-NLS-1$

    protected static final String ESB_CATEGORY = "category";//$NON-NLS-1$

    protected static final String QUERY_MESSAGE_NAME = "queryMessageName";//$NON-NLS-1$

    public static final String ALL_VERSIONS = "Latest"; //$NON-NLS-1$

    private static final String OUTPUT_FILE_SUFFIX = ".zip"; //$NON-NLS-1$

    // widgets
    protected Button shellLauncherButton;

    protected Button systemRoutineButton;

    protected Button userRoutineButton;

    protected Button modelButton;

    protected Button jobItemButton;

    protected Button contextButton;

    protected Button jobScriptButton;

    // protected ExportFileResource[] process;

    protected ProcessItem processItem = null;

    protected Combo contextCombo;

    protected Combo launcherCombo;

    protected JobScriptsManager manager;

    protected Button applyToChildrenButton;

    protected Button setParametersValueButton;

    protected Button setParametersValueButton2;

    protected RepositoryNode[] nodes;

    protected String zipOption;

    protected Button chkButton;

    String selectedJobVersion = "0.1";

    private String originalRootFolderName;

    protected Button exportDependencies;

    protected IStructuredSelection selection;

    private ExportTreeViewer treeViewer;

    Collection<RepositoryNode> repositoryNodes = new ArrayList<RepositoryNode>();

    Set<RepositoryNode> checkedNodes = new HashSet<RepositoryNode>();

    Set<RepositoryNode> allNode = new HashSet<RepositoryNode>();

    /**
     * 
     * Gets the set of current job's context.
     * 
     * @return a List of context names.
     * 
     */
    public static List<String> getJobContexts(ProcessItem processItem) {
        List<String> contextNameList = new ArrayList<String>();
        for (Object o : ((ProcessTypeImpl) processItem.getProcess()).getContext()) {
            if (o instanceof ContextType) {
                ContextType context = (ContextType) o;
                if (contextNameList.contains(context.getName())) {
                    continue;
                }
                contextNameList.add(context.getName());
            }
        }
        return contextNameList;
    }

    /**
     * Create an instance of this class.
     * 
     * @param name java.lang.String
     */
    @SuppressWarnings("unchecked")
    public JobScriptsExportWizardPage(String name, IStructuredSelection selection) {
        super(name, null);
        this.selection = selection;
        manager = null;
        nodes = (RepositoryNode[]) selection.toList().toArray(new RepositoryNode[selection.size()]);
    }

    protected RepositoryNode[] getCheckNodes() {
        return treeViewer.getCheckNodes();
    }

    protected ProcessItem getProcessItem() {
        if ((processItem == null) && (nodes != null) && (nodes.length >= 1)) {
            IRepositoryViewObject repositoryObject = nodes[0].getObject();
            // add for bug TDI-20132
            List<IRepositoryNode> nodesChildren = nodes[0].getChildren();
            IRepositoryViewObject childObject = null;
            if ((nodesChildren != null) && (nodesChildren.size() >= 1)) {
                childObject = nodesChildren.get(0).getObject();
            }
            if (repositoryObject == null && childObject != null && childObject.getProperty().getItem() instanceof ProcessItem) {
                processItem = (ProcessItem) childObject.getProperty().getItem();
            }
            if (repositoryObject != null && repositoryObject.getProperty().getItem() instanceof ProcessItem) {
                processItem = (ProcessItem) repositoryObject.getProperty().getItem();
            }
        }
        return processItem;
    }

    protected void setProcessItem(ProcessItem value) {
        processItem = value;
    }

    public abstract JobScriptsManager createJobScriptsManager();

    /**
     * Create an instance of this class.
     * 
     * @param selection the selection
     */
    public JobScriptsExportWizardPage(IStructuredSelection selection) {
        this("jobscriptsExportPage1", selection); //$NON-NLS-1$
        setDescription(Messages.getString("JobScriptsExportWizardPage.ExportJob")); //$NON-NLS-1$
        setTitle(DataTransferMessages.ArchiveExport_exportTitle);
    }

    /**
     * yzhang Comment method "setDefaultDestination".
     */
    protected void setDefaultDestination() {

        String userDir = System.getProperty("user.dir"); //$NON-NLS-1$
        IPath path = new Path(userDir);
        int length = nodes.length;
        String destinationFile = ""; //$NON-NLS-1$
        if (getDialogSettings() != null) {
            IDialogSettings section = getDialogSettings().getSection(DESTINATION_FILE);
            if (section != null) {
                destinationFile = section.get(DESTINATION_FILE);
            }
        }
        if (destinationFile == null || "".equals(destinationFile)) { //$NON-NLS-1$
            if (length == 1) {
                // TODOthis is changed by shenhaize first open ,it show contains in the combo
                path = path.append(getDefaultFileNameWithType()); //$NON-NLS-1$
            } else if (length > 1) {
                // i changed here ..
                path = path.append(getDefaultFileNameWithType()); //$NON-NLS-1$
            }
        } else {
            // path = new Path(destinationFile);
            IPreferenceStore store = RepositoryManager.getPreferenceStore();
            if (store.getBoolean(IRepositoryPrefConstants.USE_EXPORT_SAVE)) {
                path = new Path(destinationFile);
            } else {
                path = path.append(getDefaultFileNameWithType()); //$NON-NLS-1$
            }
        }
        setDestinationValue(path.toOSString());
    }

    protected void setDefaultDestinationForOSGI() {
        String bundleName = getDefaultFileNameWithType();
        String userDir = System.getProperty("user.dir"); //$NON-NLS-1$
        IPath path = new Path(userDir).append(bundleName);
        setDestinationValue(path.toOSString());
    }

    protected List<String> getDefaultFileName() {
        List<String> list = new ArrayList<String>();
        if (nodes.length >= 1) {
            String label = "";
            String version = "";
            if (nodes.length > 1) {
                label = ProjectManager.getInstance().getCurrentProject().getLabel();
            } else {
                RepositoryNode node = nodes[0];
                if (node.getType() == ENodeType.SYSTEM_FOLDER || node.getType() == ENodeType.SIMPLE_FOLDER) {
                    label = node.getProperties(EProperties.LABEL).toString();
                } else if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
                    IRepositoryViewObject repositoryObject = node.getObject();
                    if (repositoryObject.getProperty().getItem() instanceof ProcessItem) {
                        ProcessItem processItem = (ProcessItem) repositoryObject.getProperty().getItem();
                        label = processItem.getProperty().getLabel();
                        version = processItem.getProperty().getVersion();
                    }
                }
            }
            list.add(label);
            list.add(version);
            // return label;
            return list;
        }
        return null;

    }

    /**
     * (non-Javadoc) Method declared on IDialogPage.
     */
    /**
     * (non-Javadoc) Method declared on IDialogPage.
     */
    @Override
    public void createControl(Composite parent) {

        initializeDialogUnits(parent);
        SashForm sash = createExportTree(parent);

        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 0;
        layout.marginHeight = 5;
        layout.marginBottom = 0;
        Composite composite = new Composite(sash, SWT.BORDER);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setFont(parent.getFont());

        createDestinationGroup(composite);
        if (!isMultiNodes()) {
            createJobVersionGroup(composite);
        }

        createUnzipOptionGroup(composite);
        createOptionsGroup(composite);

        restoreResourceSpecificationWidgetValues(); // ie.- local
        restoreWidgetValues(); // ie.- subclass hook

        updateWidgetEnablements();
        setPageComplete(determinePageCompletion());
        setErrorMessage(null); // should not initially have error message

        setControl(sash);
        sash.setWeights(new int[] { 0, 2, 23 });
        giveFocusToDestination();

    }

    ICheckStateListener checkStateListener = new ICheckStateListener() {

        public void checkStateChanged(CheckStateChangedEvent event) {
            checkExport();
        }

    };

    public boolean checkExport() {
        Map<ExportChoice, Object> exportChoiceMap = getExportChoiceMap();
        boolean canExport = false;
        for (ExportChoice choice : ExportChoice.values()) {
            if (exportChoiceMap.get(choice) != null && exportChoiceMap.get(choice) instanceof Boolean
                    && (Boolean) exportChoiceMap.get(choice)) {
                canExport = true;
                break;
            }
        }
        if (!canExport) {
            this.setErrorMessage(Messages.getString("JobScriptsExportWizardPage.chooseResource"));
        }
        return canExport;
    }

    protected SashForm createExportTree(Composite parent) {
        // Using a protected method to provide the tree. LiXiaopeng 2011-9-21
        treeViewer = getExportTree();
        SashForm sashForm = treeViewer.createContents(parent);
        treeViewer.addCheckStateListener(checkStateListener);
        return sashForm;
    }

    /**
     * get ExportTreeViewer, subclass may override.
     */
    protected ExportTreeViewer getExportTree() {
        return new ExportTreeViewer(selection, this);
    }

    /**
     * ftang Comment method "createJobVersionGroup".
     * 
     * @param composite
     */
    protected void createJobVersionGroup(Composite parent) {
        Group versionGroup = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        versionGroup.setLayout(layout);
        versionGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        versionGroup.setText(Messages.getString("JobScriptsExportWSWizardPage.newJobVersion", getProcessType())); //$NON-NLS-1$
        versionGroup.setFont(parent.getFont());

        versionGroup.setLayout(new GridLayout(1, true));

        Composite left = new Composite(versionGroup, SWT.NONE);
        left.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
        left.setLayout(new GridLayout(3, false));

        Label label = new Label(left, SWT.NONE);
        label.setText(Messages.getString("JobScriptsExportWSWizardPage.newJobVersion.Label", getProcessType())); //$NON-NLS-1$

        final Combo versionCombo = new Combo(left, SWT.PUSH);
        GridData gd = new GridData();
        gd.horizontalSpan = 1;
        versionCombo.setLayoutData(gd);

        String[] allVersions = JobVersionUtils.getAllVersions(nodes[0]);
        String currentVersion = JobVersionUtils.getCurrentVersion(nodes[0]);
        versionCombo.setItems(allVersions);
        if (allVersions.length > 1) {
            versionCombo.add(JobScriptsExportWizardPage.ALL_VERSIONS);
        }
        versionCombo.setText(currentVersion);
        selectedJobVersion = currentVersion;
        versionCombo.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                selectedJobVersion = versionCombo.getText();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

        });
    }

    protected void createUnzipOptionGroup(Composite parent) {
        // options group
        Group optionsGroup = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        optionsGroup.setLayout(layout);
        optionsGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        optionsGroup.setText("Extract zip file"); //$NON-NLS-1$
        optionsGroup.setFont(parent.getFont());
        optionsGroup.setLayout(new GridLayout(1, true));
        Composite left = new Composite(optionsGroup, SWT.NONE);
        left.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
        left.setLayout(new GridLayout(3, false));
        chkButton = new Button(left, SWT.CHECK);
        chkButton.setText(Messages.getString("JobScriptsExportWizardPage.extractZipFile")); //$NON-NLS-1$
        chkButton.setSelection(false);
        chkButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                chkButton.setSelection(chkButton.getSelection());
                zipOption = String.valueOf(chkButton.getSelection());
            }
        });
    }

    /*
     * It's not a good method to resovle the problem of null pointer, which is led by commenting the //
     * createResourcesGroup(composite); and createButtonsGroup(composite); (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1#validateSourceGroup()
     */
    @Override
    public boolean validateSourceGroup() {
        return true;
    }

    /**
     * Create the export options specification widgets.
     * 
     */
    @Override
    public void createOptionsGroupButtons(Group optionsGroup) {
        Font font = optionsGroup.getFont();
        optionsGroup.setLayout(new GridLayout(1, true));

        Composite left = new Composite(optionsGroup, SWT.NONE);
        left.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
        left.setLayout(new GridLayout(3, true));

        createOptions(left, font);

        // Composite right = new Composite(optionsGroup, SWT.NONE);
        // right.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
        // right.setLayout(new GridLayout(1, true));
    }

    /**
     * Create the buttons for the group that determine if the entire or selected directory structure should be created.
     * 
     * @param optionsGroup
     * @param font
     */
    public void createOptions(final Composite optionsGroup, Font font) {
        // create directory structure radios
        shellLauncherButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        shellLauncherButton.setText(Messages.getString("JobScriptsExportWizardPage.shellLauncher")); //$NON-NLS-1$
        shellLauncherButton.setSelection(true);
        shellLauncherButton.setFont(font);

        launcherCombo = new Combo(optionsGroup, SWT.PUSH);
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        launcherCombo.setLayoutData(gd);
        // laucherText = new Text(optionsGroup, SWT.BORDER);
        // laucherText.setEditable(false);

        // create directory structure radios
        systemRoutineButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        systemRoutineButton.setText(Messages.getString("JobScriptsExportWizardPage.systemRoutines")); //$NON-NLS-1$
        systemRoutineButton.setSelection(true);
        systemRoutineButton.setFont(font);

        userRoutineButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        userRoutineButton.setText(Messages.getString("JobScriptsExportWizardPage.userRoutines")); //$NON-NLS-1$
        userRoutineButton.setSelection(true);
        userRoutineButton.setFont(font);
        gd = new GridData();
        gd.horizontalSpan = 2;
        userRoutineButton.setLayoutData(gd);

        modelButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        modelButton.setText(Messages.getString("JobScriptsExportWizardPage.requiredTalendPerlModules")); //$NON-NLS-1$
        modelButton.setSelection(true);
        modelButton.setFont(font);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        modelButton.setLayoutData(gd);

        jobScriptButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        if (LanguageManager.getCurrentLanguage() == ECodeLanguage.PERL) {
            jobScriptButton.setText(Messages.getString("JobScriptsExportWizardPage.jobPerlScripts")); //$NON-NLS-1$
        } else {
            jobScriptButton.setText(Messages.getString("JobScriptsExportWizardPage.jobJavaScripts")); //$NON-NLS-1$
        }
        jobScriptButton.setSelection(true);
        jobScriptButton.setFont(font);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        jobScriptButton.setLayoutData(gd);

        jobItemButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        jobItemButton.setText(Messages.getString("JobScriptsExportWizardPage.sourceFiles")); //$NON-NLS-1$
        jobItemButton.setSelection(true);
        jobItemButton.setFont(font);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        jobItemButton.setLayoutData(gd);

        exportDependencies = new Button(optionsGroup, SWT.CHECK);
        exportDependencies.setText("Export Dependencies"); //$NON-NLS-1$
        exportDependencies.setFont(font);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        jobItemButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                exportDependencies.setEnabled(jobItemButton.getSelection());
                if (!jobItemButton.getSelection()) {
                    exportDependencies.setSelection(false);
                }
            }
        });
        exportDependencies.setLayoutData(gd);
        // // feature 19312
        // exportDependencies.addSelectionListener(new SelectionAdapter() {
        //
        // @Override
        // public void widgetSelected(SelectionEvent e) {
        // refreshExportDependNodes();
        // exportDependenciesSelected();
        // }
        // });

        contextButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        contextButton.setText(Messages.getString("JobScriptsExportWizardPage.contextPerlScripts")); //$NON-NLS-1$
        contextButton.setSelection(true);
        contextButton.setFont(font);

        contextCombo = new Combo(optionsGroup, SWT.PUSH);

        applyToChildrenButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        applyToChildrenButton.setText(Messages.getString("JobScriptsExportWizardPage.ApplyToChildren")); //$NON-NLS-1$

        // genCodeButton = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
        // genCodeButton.setText(Messages.getString("JobScriptsExportWizardPage.generatePerlFiles")); //$NON-NLS-1$
        // genCodeButton.setSelection(true);
        // genCodeButton.setFont(font);

        setParametersValueButton = new Button(optionsGroup, SWT.NONE);
        setParametersValueButton.setText(Messages.getString("JobScriptsExportWizardPage.OverrideParameterValues"));
        setParametersValueButton.setSelection(false);

        setParametersValueButton2 = new Button(optionsGroup, SWT.CHECK);
        setParametersValueButton2.setVisible(false);

        setParametersValueButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (manager == null) {
                    manager = createJobScriptsManager();
                }
                List<ContextParameterType> contextEditableResultValuesList = manager.getContextEditableResultValuesList();
                List<ContextParameterType> contextValueList = new ArrayList<ContextParameterType>();
                if (contextEditableResultValuesList == null) {
                    contextValueList = getJobContextValues(getProcessItem(), contextCombo.getText());
                }
                ParametersValuesDialog dialog = new ParametersValuesDialog(getShell(), contextValueList,
                        contextEditableResultValuesList);
                int open = dialog.open();
                if (open == Dialog.OK) {
                    List<ContextParameterType> contextResultValuesList = dialog.getContextResultValuesList();
                    manager.setContextEditableResultValuesList(contextResultValuesList);
                    setParametersValueButton2.setSelection(true);
                } else {
                    setParametersValueButton2.setSelection(false);
                }
            }
        });
    }

    /**
     * DOC zli Comment method "getJobContextValues".
     * 
     * @param processItem
     * @param contextName
     * @return
     */
    @SuppressWarnings("rawtypes")
    public List<ContextParameterType> getJobContextValues(ProcessItem processItem, String contextName) {
        if (contextName == null) {
            return null;
        }// else do next line
        List<ContextParameterType> list = new ArrayList<ContextParameterType>();
        EList contexts = ((ProcessTypeImpl) processItem.getProcess()).getContext();
        for (int i = 0; i < contexts.size(); i++) {
            Object object = contexts.get(i);
            if (object instanceof ContextType) {
                ContextType contextType = (ContextType) object;
                if (contextName.equals(contextType.getName())) {
                    EList contextParameter = contextType.getContextParameter();
                    for (int j = 0; j < contextParameter.size(); j++) {
                        Object object2 = contextParameter.get(j);
                        if (object2 instanceof ContextParameterType) {
                            ContextParameterType contextParameterType = (ContextParameterType) object2;
                            list.add(contextParameterType);
                        }
                    }
                    return list;
                }
            }
        }
        return null;
    }

    private void collectNodes(Map<String, Item> items, Object[] objects) {
        for (int i = 0; i < objects.length; i++) {
            RepositoryNode repositoryNode = (RepositoryNode) objects[i];
            collectNodes(items, repositoryNode);
        }
    }

    private void collectNodes(Map<String, Item> items, RepositoryNode repositoryNode) {
        IRepositoryViewObject repositoryObject = repositoryNode.getObject();
        if (repositoryObject != null) {
            if (repositoryObject.getRepositoryObjectType().isResourceItem()) {
                Item item = repositoryObject.getProperty().getItem();
                items.put(item.getProperty().getId(), item);
            }
        } else {
            if (repositoryNode.getParent() != null && repositoryNode.getParent().getObject() != null) {
                Item item = repositoryNode.getParent().getObject().getProperty().getItem();
                items.put(item.getProperty().getId(), item);
            }
        }
        if (this.treeViewer != null) {
            IContentProvider contentProvider = this.treeViewer.getFilteredCheckboxTree().getViewer().getContentProvider();
            if (contentProvider instanceof ITreeContentProvider) {
                Object[] children = ((ITreeContentProvider) contentProvider).getChildren(repositoryNode);
                collectNodes(items, children);
            }
        }
    }

    private static boolean isRepositoryFolder(RepositoryNode node) {
        final ENodeType type = node.getType();
        if (type == ENodeType.SIMPLE_FOLDER || type == ENodeType.STABLE_SYSTEM_FOLDER || type == ENodeType.SYSTEM_FOLDER) {
            return true;
        }
        return false;
    }

    /**
     * DOC zli JobScriptsExportWizardPage class global comment. Detailled comment
     */
    // for feature 11976
    class ParametersValuesDialog extends Dialog {

        private final String contextParameterName = Messages.getString("ParametersValuesDialog_Name"); //$NON-NLS-1$

        private final String contextParameterValue = Messages.getString("ParametersValuesDialog_Value"); //$NON-NLS-1$

        private TableViewer tableViewer;

        private Table table;

        private List<ContextParameterType> contextValueList;

        private List<ContextParameterType> contextEditableValuesList;

        private List<ContextParameterType> contextResultValuesList;

        private Button setContextButton;

        private Button addButton;

        private Button removeButton;

        private final String addParameterName = "new";

        /**
         * DOC zli ParametersValuesDialog constructor comment.
         * 
         * @param parentShell
         */
        protected ParametersValuesDialog(Shell parentShell) {
            super(parentShell);
            setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX | SWT.MIN);
        }

        protected ParametersValuesDialog(Shell parentShell, List<ContextParameterType> contextValueList,
                List<ContextParameterType> contextEditableResultValuesList) {
            super(parentShell);
            setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX | SWT.MIN);
            this.contextValueList = contextValueList;
            // if set before, will use the set values. if not, only display the default parameters names.
            if (contextEditableResultValuesList == null) {
                contextEditableValuesList = initContextValues(contextValueList);
            } else {
                contextEditableValuesList = reviewContextValues(contextEditableResultValuesList);
            }
        }

        @Override
        protected Point getInitialSize() {
            Point p = super.getInitialSize();
            p.x = 600;
            p.y = 450;
            return p;
        }

        protected List<ContextParameterType> initContextValues(List<ContextParameterType> valueList) {

            List<ContextParameterType> list = new ArrayList<ContextParameterType>();

            for (int i = 0; i < valueList.size(); i++) {
                Object object = valueList.get(i);
                ContextParameterType contextType = (ContextParameterType) object;
                ContextParameterType createContextParameterType = TalendFileFactory.eINSTANCE.createContextParameterType();
                createContextParameterType.setName(contextType.getName());
                createContextParameterType.setType(contextType.getType());
                createContextParameterType.setValue("");
                list.add(createContextParameterType);
            }
            return list;
        }

        protected List<ContextParameterType> reviewContextValues(List<ContextParameterType> valueList) {

            List<ContextParameterType> list = new ArrayList<ContextParameterType>();

            for (int i = 0; i < valueList.size(); i++) {
                Object object = valueList.get(i);
                ContextParameterType contextType = (ContextParameterType) object;
                ContextParameterType createContextParameterType = TalendFileFactory.eINSTANCE.createContextParameterType();
                createContextParameterType.setName(contextType.getName());
                createContextParameterType.setType(contextType.getType());
                createContextParameterType.setValue(contextType.getValue());
                list.add(createContextParameterType);
            }
            return list;
        }

        @Override
        protected Control createDialogArea(Composite parent) {

            Composite composite = (Composite) super.createDialogArea(parent);
            getShell().setText(Messages.getString("ParametersValuesDialog_Title")); //$NON-NLS-1$
            setTitle(Messages.getString("ParametersValuesDialog_Title")); //$NON-NLS-1$
            setMessage(Messages.getString("ParametersValuesDialog_Desc")); //$NON-NLS-1$

            tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
            tableViewer.setContentProvider(new ContentProvider());
            tableViewer.setLabelProvider(new TableLabelProvider());
            tableViewer.setInput(contextEditableValuesList);
            table = tableViewer.getTable();
            TableLayout layout = new TableLayout();
            table.setLayout(layout);
            table.setLayoutData(new GridData(GridData.FILL_BOTH));
            table.setHeaderVisible(true);
            table.setLinesVisible(true);

            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText(contextParameterName);
            column.setWidth(150);

            column = new TableColumn(table, SWT.NONE);
            column.setText(contextParameterValue);
            column.setWidth(300);
            tableViewer.refresh();
            tableViewer.setColumnProperties(new String[] { contextParameterName, contextParameterValue });
            // set modifier
            tableViewer.setCellModifier(new ICellModifier() {

                public void modify(Object element, String property, Object value) {
                    List<String> nameList = new ArrayList<String>();
                    for (int i = 0; i < contextEditableValuesList.size(); i++) {
                        String name = contextEditableValuesList.get(i).getName();
                        nameList.add(name);
                    }
                    TableItem tableItem = (TableItem) element;
                    ContextParameterType node = (ContextParameterType) tableItem.getData();
                    if (contextEditableValuesList.contains(node)) {
                        nameList.remove(node.getName());
                    }
                    if (property.equals(contextParameterName)) {
                        if (value == null || "".equals(value) || nameList.contains(value)) {
                            MessageDialog.openError(
                                    new Shell(),
                                    Messages.getString("ContextProcessSection.errorTitle"), Messages.getString("ContextProcessSection.ParameterNameIsNotValid")); //$NON-NLS-1$ //$NON-NLS-2$
                        } else {
                            node.setName((String) value);
                        }
                    }
                    if (property.equals(contextParameterValue)) {
                        node.setValue((String) value);
                    }
                    tableViewer.refresh(node);
                }

                public Object getValue(Object element, String property) {
                    ContextParameterType node = (ContextParameterType) element;
                    if (property.equals(contextParameterName)) { //$NON-NLS-1$
                        return node.getName();
                    }
                    if (property.equals(contextParameterValue)) { //$NON-NLS-1$
                        return node.getValue();
                    }

                    return null;
                }

                public boolean canModify(Object element, String property) {
                    return true;
                }
            });
            // set editor
            int columnCount = table.getColumnCount();
            CellEditor[] editors = new CellEditor[columnCount];
            for (int i = 0; i < columnCount; i++) {
                editors[i] = new TextCellEditor(table);
            }
            tableViewer.setCellEditors(editors);

            final Composite buttonsComposite = new Composite(composite, SWT.NONE);

            buttonsComposite.setLayout(new GridLayout(6, false));
            GridData gData = new GridData(GridData.FILL_HORIZONTAL);
            buttonsComposite.setLayoutData(gData);

            setContextButton = new Button(buttonsComposite, SWT.NONE);
            GridData gD = new GridData();
            gD.horizontalSpan = 2;
            setContextButton.setLayoutData(gD);
            setContextButton.setText("Values from selected context");//$NON-NLS-N$
            setContextButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    for (ContextParameterType contextType : contextEditableValuesList) {
                        for (ContextParameterType context : contextValueList) {
                            if (contextType.getName().equals(context.getName())) {
                                contextType.setValue(context.getValue());
                            }
                        }
                    }
                    tableViewer.refresh(true);
                }
            });

            addButton = new Button(buttonsComposite, SWT.PUSH);
            addButton.setLayoutData(new GridData());
            addButton.setText("Add");//$NON-NLS-N$
            addButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    ContextParameterType addContextParameterType = TalendFileFactory.eINSTANCE.createContextParameterType();
                    Integer numParam = new Integer(1);
                    boolean paramNameFound;
                    String paramName = null;
                    do { // look for a new name
                        paramNameFound = true;
                        paramName = addParameterName + numParam;
                        for (int i = 0; i < contextEditableValuesList.size(); i++) {
                            if (paramName.equals(contextEditableValuesList.get(i).getName())) {
                                paramNameFound = false;
                            }
                        }
                        if (!paramNameFound) {
                            numParam++;
                        }
                    } while (!paramNameFound);
                    addContextParameterType.setName(paramName);
                    addContextParameterType.setType("id_String");//$NON-NLS-N$
                    addContextParameterType.setValue("");//$NON-NLS-N$
                    contextEditableValuesList.add(addContextParameterType);
                    tableViewer.refresh(true);
                }

            });

            removeButton = new Button(buttonsComposite, SWT.PUSH);
            removeButton.setLayoutData(new GridData());
            removeButton.setText("Remove");//$NON-NLS-N$
            removeButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {

                    TableItem[] items = tableViewer.getTable().getSelection();
                    if (items != null && items.length == 1) {
                        TableItem removeItem = items[0];
                        Object data = removeItem.getData();
                        if (data instanceof ContextParameterType) {
                            ContextParameterType removeContextType = (ContextParameterType) data;
                            contextEditableValuesList.remove(removeContextType);
                        }
                        tableViewer.refresh(true);
                    }
                }
            });

            return composite;
        }

        private List<ContextParameterType> getContextResultValuesList() {
            return this.contextResultValuesList;
        }

        private void setContextResultValuesList(List<ContextParameterType> contextResultValuesList) {
            this.contextResultValuesList = contextResultValuesList;
        }

        @Override
        protected void okPressed() {
            super.okPressed();
            setContextResultValuesList(contextEditableValuesList);
        }

        @Override
        protected void cancelPressed() {
            super.cancelPressed();
        }

    }

    /**
     * DOC zli JobScriptsExportWizardPage class global comment. Detailled comment
     */

    class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof ContextParameterType) {
                ContextParameterType contextParameter = (ContextParameterType) element;
                if (columnIndex == 0) {
                    return contextParameter.getName();
                }
                if (columnIndex == 1) {
                    return contextParameter.getValue();
                }
            }
            return ""; //$NON-NLS-1$
        }

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

    }

    /**
     * DOC zli JobScriptsExportWizardPage class global comment. Detailled comment
     */
    class ContentProvider implements IStructuredContentProvider {

        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof List) {
                return ((List) inputElement).toArray();
            }
            return new Object[0];
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
         */
        public Object[] getChildren(Object parentElement) {
            return getElements(parentElement);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
         */
        public Object getParent(Object element) {
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
         */
        public boolean hasChildren(Object element) {
            return false;
        }
    }

    /**
     * Returns a boolean indicating whether the directory portion of the passed pathname is valid and available for use.
     */
    protected boolean ensureTargetDirectoryIsValid(String fullPathname) {
        int separatorIndex = fullPathname.lastIndexOf(File.separator);

        if (separatorIndex == -1) {
            return true;
        }

        return ensureTargetIsValid(new File(fullPathname.substring(0, separatorIndex)));
    }

    /**
     * Returns a boolean indicating whether the passed File handle is is valid and available for use.
     */
    protected boolean ensureTargetFileIsValid(File targetFile) {
        if (targetFile.exists() && targetFile.isDirectory()) {
            displayErrorDialog(DataTransferMessages.ZipExport_mustBeFile);
            giveFocusToDestination();
            return false;
        }

        if (targetFile.exists()) {
            if (targetFile.canWrite()) {
                if (!queryYesNoQuestion(DataTransferMessages.ZipExport_alreadyExists)) {
                    displayErrorDialog("Please enter another destination zip file.");
                    giveFocusToDestination();
                    return false;
                }
            } else {
                displayErrorDialog(DataTransferMessages.ZipExport_alreadyExistsError);
                giveFocusToDestination();
                return false;
            }
        }

        return true;
    }

    /**
     * Ensures that the target output file and its containing directory are both valid and able to be used. Answer a
     * boolean indicating validity.
     */
    protected boolean ensureTargetIsValid() {
        String targetPath = null;
        if (manager instanceof PetalsJobJavaScriptsManager) {
            targetPath = manager.getDestinationPath();
        } else {
            targetPath = getDestinationValue();
        }
        if (this.selectedJobVersion != null && this.selectedJobVersion.equals(JobScriptsExportWizardPage.ALL_VERSIONS)) {

            if (this.originalRootFolderName == null) {
                this.originalRootFolderName = manager.getRootFolderName(getDestinationValue());
            }
            String newFileName = this.originalRootFolderName + manager.getSelectedJobVersion() + getOutputSuffix();
            targetPath = targetPath.substring(0, targetPath.lastIndexOf(File.separator) + 1) + newFileName;
            setDestinationValue(targetPath);
        }

        if (!ensureTargetDirectoryIsValid(targetPath)) {
            return false;
        }

        if (!ensureTargetFileIsValid(new File(targetPath))) {
            return false;
        }

        return true;
    }

    /**
     * Export the passed resource and recursively export all of its child resources (iff it's a container). Answer a
     * boolean indicating success.
     */
    protected boolean executeExportOperation(ArchiveFileExportOperationFullPath op) {
        op.setCreateLeadupStructure(true);
        op.setUseCompression(true);

        try {
            getContainer().run(true, true, op);
        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
        } catch (InterruptedException e) {
            ExceptionHandler.process(e);

        }

        IStatus status = op.getStatus();
        if (!status.isOK()) {
            ErrorDialog.openError(getContainer().getShell(), "", null, // no //$NON-NLS-1$
                    // special
                    // message
                    status);
            return false;
        }

        return true;
    }

    // protected String getDestinationValueSU() {
    //        return this.suDestinationFilePath != null ? this.suDestinationFilePath : ""; //$NON-NLS-1$
    //
    // }

    /**
     * The Finish button was pressed. Try to do the required work now and answer a boolean indicating success. If false
     * is returned then the wizard will not close.
     * 
     * @returns boolean
     */
    @Override
    public boolean finish() {
        // TODO
        if (treeViewer != null) {
            treeViewer.removeCheckStateListener(checkStateListener);
        }

        List<ContextParameterType> contextEditableResultValuesList = null;
        if (manager != null) {
            contextEditableResultValuesList = manager.getContextEditableResultValuesList();
        }
        manager = createJobScriptsManager();

        // Save dirty editors if possible but do not stop if not all are saved
        saveDirtyEditors();
        // about to invoke the operation so save our state
        saveWidgetValues();

        if (!ensureTargetIsValid()) {
            return false;
        }

        // for feature:11976, recover back the old default manager value with ContextParameters
        if (contextEditableResultValuesList == null) {
            manager.setContextEditableResultValuesList(new ArrayList<ContextParameterType>());
        } else {
            manager.setContextEditableResultValuesList(contextEditableResultValuesList);
        }

        if (manager instanceof PetalsJobJavaScriptsManager) {
            PetalsTemporaryOptionsKeeper.INSTANCE.setSelection(selection);
        }
        manager.setMultiNodes(isMultiNodes());
        // achen modify to fix bug 0006222

        IRunnableWithProgress worker = new JobExportAction(Arrays.asList(getCheckNodes()), getSelectedJobVersion(), manager,
                originalRootFolderName, getProcessType());

        try {
            getContainer().run(false, true, worker);
        } catch (InvocationTargetException e) {
            MessageBoxExceptionHandler.process(e.getCause(), getShell());
            return false;
        } catch (InterruptedException e) {
            return false;
        }

        // see bug 7181
        if (zipOption != null && zipOption.equals("true")) { //$NON-NLS-1$
            // unzip
            try {
                String zipFile = manager.getDestinationPath();
                // Added by Marvin Wang on Feb.1, 2012 for bug TDI-18824
                File file = new File(zipFile);
                if (file.exists())
                    ZipToFile.unZipFile(zipFile, file.getParentFile().getAbsolutePath());
            } catch (Exception e) {
                MessageBoxExceptionHandler.process(e, getShell());
                return false;
            }
        }

        if (treeViewer != null) {
            treeViewer.dispose();
        }

        // end
        return true;
    }

    /**
     * Get the export operation.
     * 
     * @param resourcesToExport
     * @return
     */
    public FileSystemExporterFullPath getUnzipExporterOperation(List<ExportFileResource> resourcesToExport) {
        String currentUnzipFile = getDestinationValue().replace("/", File.separator); //$NON-NLS-1$ //$NON-NLS-2$
        currentUnzipFile = currentUnzipFile.substring(0, currentUnzipFile.lastIndexOf(File.separator)); //$NON-NLS-1$
        FileSystemExporterFullPath exporterOperation = null;
        try {
            exporterOperation = new FileSystemExporterFullPath(resourcesToExport, currentUnzipFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
        return exporterOperation;
    }

    /**
     * Comment method "setTopFolder".
     * 
     * @param resourcesToExport
     * @param topFolder
     */
    public void setTopFolder(List<ExportFileResource> resourcesToExport, String topFolder) {
        for (ExportFileResource fileResource : resourcesToExport) {
            String directory = fileResource.getDirectoryName();
            fileResource.setDirectoryName(topFolder + "/" + directory); //$NON-NLS-1$
        }
    }

    /**
     * Answer the string to display in self as the destination type.
     * 
     * @return java.lang.String
     */
    @Override
    protected String getDestinationLabel() {
        return DataTransferMessages.ArchiveExport_destinationLabel;
    }

    protected Map<ExportChoice, Object> getExportChoiceMap() {
        Map<ExportChoice, Object> exportChoiceMap = new EnumMap<ExportChoice, Object>(ExportChoice.class);
        exportChoiceMap.put(ExportChoice.needLauncher, shellLauncherButton.getSelection());
        exportChoiceMap.put(ExportChoice.needSystemRoutine, systemRoutineButton.getSelection());
        exportChoiceMap.put(ExportChoice.needUserRoutine, userRoutineButton.getSelection());
        exportChoiceMap.put(ExportChoice.needTalendLibraries, modelButton.getSelection());
        exportChoiceMap.put(ExportChoice.needJobItem, jobItemButton.getSelection());
        exportChoiceMap.put(ExportChoice.needSourceCode, jobItemButton.getSelection());
        exportChoiceMap.put(ExportChoice.needDependencies, exportDependencies.getSelection());
        exportChoiceMap.put(ExportChoice.needJobScript, jobScriptButton.getSelection());
        exportChoiceMap.put(ExportChoice.needContext, contextButton.getSelection());
        exportChoiceMap.put(ExportChoice.applyToChildren, applyToChildrenButton.getSelection());
        // exportChoice.put(ExportChoice.needDependencies, exportDependencies.getSelection());
        exportChoiceMap.put(ExportChoice.setParameterValues, setParametersValueButton2.getSelection());
        // exportChoice.put(ExportChoice.needGenerateCode, genCodeButton.getSelection());
        return exportChoiceMap;
    }

    /**
     * Answer the contents of self's destination specification widget. If this value does not have a suffix then add it
     * first.
     */
    @Override
    protected String getDestinationValue() {
        String idealSuffix = getOutputSuffix();
        String destinationText = super.getDestinationValue();
        // only append a suffix if the destination doesn't already have a . in
        // its last path segment.
        // Also prevent the user from selecting a directory. Allowing this will
        // create a ".zip" file in the directory
        if (destinationText.length() != 0 && !destinationText.endsWith(File.separator)) {
            int dotIndex = destinationText.lastIndexOf('.');
            if (dotIndex != -1) {
                // the last path seperator index
                int pathSepIndex = destinationText.lastIndexOf(File.separator);
                if (pathSepIndex != -1 && dotIndex < pathSepIndex) {
                    destinationText += idealSuffix;
                }
            } else {
                destinationText += idealSuffix;
            }
        }
        // this is the entrance to the answer .. shenhaize.
        // System.out.println(destinationText);
        // String b = destinationText.substring(0, (destinationText.length() - 4));
        // return (b + destinationText.subSequence((destinationText.length() - 4), destinationText.length()));
        // System.out.println(destinationText + "  " + idealSuffix);
        if (destinationText.endsWith(this.getSelectedJobVersion() + this.getOutputSuffix())) {
            return destinationText;
        }
        return destinationText;

    }

    /**
     * Answer the suffix that files exported from this wizard should have. If this suffix is a file extension (which is
     * typically the case) then it must include the leading period character.
     * 
     */
    protected String getOutputSuffix() {
        return OUTPUT_FILE_SUFFIX; //$NON-NLS-1$
    }

    /**
     * Open an appropriate destination browser so that the user can specify a source to import from.
     */
    @Override
    protected void handleDestinationBrowseButtonPressed() {
        FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.zip", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.setText(""); //$NON-NLS-1$
        dialog.setFileName(this.getDefaultFileName().get(0));
        String currentSourceString = getDestinationValue();
        int lastSeparatorIndex = currentSourceString.lastIndexOf(File.separator);
        if (lastSeparatorIndex != -1) {
            dialog.setFilterPath(currentSourceString.substring(0, lastSeparatorIndex));
        }
        String selectedFileName = dialog.open();
        if (selectedFileName != null && !selectedFileName.endsWith(this.getOutputSuffix()))
            selectedFileName += this.getOutputSuffix();

        // when user change the name of job,will add the version auto
        if (selectedFileName != null && !selectedFileName.endsWith(this.getSelectedJobVersion() + this.getOutputSuffix())) {
            String b = selectedFileName.substring(0, (selectedFileName.length() - 4));
            File file = new File(b);

            String str = file.getName();

            String s = this.getDefaultFileName().get(0);

            if (str.equals(s)) {
                selectedFileName = b + "_" + this.getDefaultFileName().get(1) + this.getOutputSuffix(); //$NON-NLS-1$
            } else {
                selectedFileName = b + this.getOutputSuffix();
            }

        }
        if (selectedFileName != null) {
            setErrorMessage(null);
            setDestinationValue(selectedFileName);

        }
    }

    /**
     * Hook method for saving widget values for restoration by the next instance of this class.
     */
    @Override
    protected void internalSaveWidgetValues() {
    }

    /**
     * Hook method for restoring widget values to the values that they held last time this wizard was used to
     * completion.
     */
    @Override
    protected void restoreWidgetValues() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.wizards.datatransfer.WizardFileSystemResourceExportPage1#destinationEmptyMessage()
     */
    @Override
    protected String destinationEmptyMessage() {
        return ""; //$NON-NLS-1$
    }

    /**
     * ftang Comment method "isMultiNodes".
     * 
     * @return
     */
    public boolean isMultiNodes() {
        if (treeViewer == null) {
            return false;
        }
        return this.getCheckNodes().length > 1;
    }

    /**
     * ftang Comment method "getSelectedJobVersion".
     * 
     * @return
     */
    public String getSelectedJobVersion() {
        return selectedJobVersion;
    }

    @Override
    protected boolean validateDestinationGroup() {
        return super.validateDestinationGroup() && this.checkExport();
    }

    protected String getDefaultFileNameWithType() {
        String version = "";
        List<String> defaultFileName = getDefaultFileName();
        if (defaultFileName.get(1) != null && !"".equals(defaultFileName.get(1))) {
            version = ((JobExportType.OSGI.equals(getCurrentExportType1())) ? "-" : "_") + defaultFileName.get(1);
        }
        String fileName = defaultFileName.get(0) + version + getOutputSuffix();
        return fileName;
    }

    public JobExportType getCurrentExportType1() {
        return JobExportType.POJO;
    }

    /**
     * GLIU add for fixing TESB-4975 default is "Job"
     * 
     * @return
     */
    protected String getProcessType() {
        return "Job";
    }

}
