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
package org.talend.repository.ui.wizards.exportjob;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.EventLoopProgressMonitor;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1;
import org.eclipse.ui.progress.IProgressService;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryPrefConstants;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ProcessTypeImpl;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.documentation.ArchiveFileExportOperationFullPath;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.documentation.FileSystemExporterFullPath;
import org.talend.repository.i18n.Messages;
import org.talend.repository.job.deletion.JobResource;
import org.talend.repository.job.deletion.JobResourceManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.ProjectRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.utils.ZipToFile;
import org.talend.repository.ui.views.RepositoryContentProvider;
import org.talend.repository.ui.views.RepositoryView;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.JobJavaScriptOSGIForESBManager;
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

    // widgets
    protected Button shellLauncherButton;

    protected Button systemRoutineButton;

    protected Button userRoutineButton;

    protected Button modelButton;

    protected Button jobItemButton;

    protected Button contextButton;

    protected Button jobScriptButton;

    protected ExportFileResource[] process;

    protected Combo contextCombo;

    protected Combo launcherCombo;

    protected JobScriptsManager manager;

    private IWorkspace workspace;

    protected Button applyToChildrenButton;

    protected Button setParametersValueButton;

    protected Button setParametersValueButton2;

    private RepositoryNode[] nodes;

    protected String zipOption;

    protected Button chkButton;

    private String allVersions = "all"; //$NON-NLS-1$

    private String outputFileSuffix = ".zip"; //$NON-NLS-1$

    private String selectedJobVersion;

    private String originalRootFolderName;

    protected Button exportDependencies;

    boolean ok;

    private IStructuredSelection selection;

    private ExportTreeViewer treeViewer;

    private String suDestinationFilePath;

    private String initDestinationFilePath;

    private static final int DIALOG_WIDTH = 600;

    private static final int DIALOG_HEIGHT = 480;

    Collection<RepositoryNode> repositoryNodes = new ArrayList<RepositoryNode>();

    Set<RepositoryNode> checkedNodes = new HashSet<RepositoryNode>();

    Set<RepositoryNode> allNode = new HashSet<RepositoryNode>();

    private String getInitDestinationFilePath() {
        return this.initDestinationFilePath;
    }

    private void setInitDestinationFilePath(String initDestinationFilePath) {
        this.initDestinationFilePath = initDestinationFilePath;
    }

    public String getSuDestinationFilePath() {
        return this.suDestinationFilePath;
    }

    public void setSuDestinationFilePath(String suDestinationFilePath) {
        this.suDestinationFilePath = suDestinationFilePath;
    }

    /**
     * Create an instance of this class.
     * 
     * @param name java.lang.String
     */
    public JobScriptsExportWizardPage(String name, IStructuredSelection selection) {
        super(name, null);
        this.selection = selection;
        manager = createJobScriptsManager();
        nodes = (RepositoryNode[]) selection.toList().toArray(new RepositoryNode[selection.size()]);
        setNodes(nodes);
    }

    protected RepositoryNode[] getCheckNodes() {
        return treeViewer.getCheckNodes();
    }

    private void setNodes(RepositoryNode[] nodes) {

        List<ExportFileResource> list = new ArrayList<ExportFileResource>();
        int nodeSize = nodes.length;
        if (nodeSize > 1) {
            manager.setMultiNodes(true);
        }
        for (int i = 0; i < nodeSize; i++) {
            RepositoryNode node = nodes[i];
            if (node.getType() == ENodeType.SYSTEM_FOLDER || node.getType() == ENodeType.SIMPLE_FOLDER) {
                addTreeNode(node, node.getProperties(EProperties.LABEL).toString(), list);
            }
            if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
                IRepositoryViewObject repositoryObject = node.getObject();
                if (repositoryObject.getProperty().getItem() instanceof ProcessItem) {
                    ProcessItem processItem = (ProcessItem) repositoryObject.getProperty().getItem();
                    ExportFileResource resource = new ExportFileResource(processItem, processItem.getProperty().getLabel());
                    processItem.getProcess().getNode();
                    resource.setNode(node);
                    list.add(resource);
                }
            }
        }
        process = list.toArray(new ExportFileResource[list.size()]);
    }

    private void addTreeNode(RepositoryNode node, String path, List<ExportFileResource> list) {
        if (node != null && node.getType() == ENodeType.REPOSITORY_ELEMENT) {
            IRepositoryViewObject repositoryObject = node.getObject();
            if (repositoryObject.getProperty().getItem() instanceof ProcessItem) {
                ProcessItem processItem = (ProcessItem) repositoryObject.getProperty().getItem();
                ExportFileResource resource = new ExportFileResource(processItem, path);
                resource.setNode(node);
                list.add(resource);
            }
        }
        Object[] nodes = node.getChildren().toArray();
        if (nodes.length <= 0) {
            return;
        }
        for (int i = 0; i < nodes.length; i++) {
            addTreeNode((RepositoryNode) nodes[i], path + "/" //$NON-NLS-1$
                    + ((RepositoryNode) nodes[i]).getProperties(EProperties.LABEL).toString(), list);
        }
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
                path = path.append(this.getDefaultFileName().get(0) + "_" + this.getDefaultFileName().get(1) + getOutputSuffix()); //$NON-NLS-1$
            } else if (length > 1) {
                // i changed here ..
                path = path.append(this.getDefaultFileName().get(0) + "_" + this.getDefaultFileName().get(1) + getOutputSuffix()); //$NON-NLS-1$
            }
        } else {
            // path = new Path(destinationFile);
            IPreferenceStore store = RepositoryManager.getPreferenceStore();
            if (store.getBoolean(IRepositoryPrefConstants.USE_EXPORT_SAVE)) {
                path = new Path(destinationFile);
            } else {
                path = path.append(this.getDefaultFileName().get(0) + "_" + this.getDefaultFileName().get(1) + getOutputSuffix()); //$NON-NLS-1$
            }
        }
        setInitDestinationFilePath(path.toOSString());
        setDestinationValue(path.toOSString());
    }

    /**
     * yzhang Comment method "getDefaultFileName".
     */
    // protected String getDefaultFileVersion() {
    // if (nodes.length >= 1) {
    // String label = null;
    // String version = null;
    // RepositoryNode node = nodes[0];
    // if (node.getType() == ENodeType.SYSTEM_FOLDER || node.getType() == ENodeType.SIMPLE_FOLDER) {
    // label = node.getProperties(EProperties.LABEL).toString();
    // } else if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
    // IRepositoryObject repositoryObject = node.getObject();
    // if (repositoryObject.getProperty().getItem() instanceof ProcessItem) {
    // ProcessItem processItem = (ProcessItem) repositoryObject.getProperty().getItem();
    // label = processItem.getProperty().getLabel();
    // System.out.println(label);
    // version = processItem.getProperty().getVersion();
    // }
    // }
    //
    // return label;
    // }
    // return "";
    //
    // }

    protected List getDefaultFileName() {
        List list = null;
        if (nodes.length >= 1) {
            String label = null;
            String version = null;
            RepositoryNode node = nodes[0];
            if (node.getType() == ENodeType.SYSTEM_FOLDER || node.getType() == ENodeType.SIMPLE_FOLDER) {
                label = node.getProperties(EProperties.LABEL).toString();
            } else if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
                IRepositoryViewObject repositoryObject = node.getObject();
                if (repositoryObject.getProperty().getItem() instanceof ProcessItem) {
                    ProcessItem processItem = (ProcessItem) repositoryObject.getProperty().getItem();
                    label = processItem.getProperty().getLabel();
                    version = processItem.getProperty().getVersion();
                    list = new ArrayList();
                    list.add(label);
                    list.add(version);
                }
            }

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
        return true;
    }

    protected SashForm createExportTree(Composite parent) {
        treeViewer = new ExportTreeViewer(selection, this);
        SashForm sashForm = treeViewer.createContents(parent);
        treeViewer.addCheckStateListener(checkStateListener);
        return sashForm;
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
        versionGroup.setText(Messages.getString("JobScriptsExportWSWizardPage.JobVersion")); //$NON-NLS-1$
        versionGroup.setFont(parent.getFont());

        versionGroup.setLayout(new GridLayout(1, true));

        Composite left = new Composite(versionGroup, SWT.NONE);
        left.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
        left.setLayout(new GridLayout(3, false));

        Label label = new Label(left, SWT.NONE);
        label.setText(Messages.getString("JobScriptsExportWSWizardPage.JobVersion.Label")); //$NON-NLS-1$

        final Combo versionCombo = new Combo(left, SWT.PUSH);
        GridData gd = new GridData();
        gd.horizontalSpan = 1;
        versionCombo.setLayoutData(gd);

        String[] allVersions = JobVersionUtils.getAllVersions(nodes[0]);
        String currentVersion = JobVersionUtils.getCurrentVersion(nodes[0]);
        versionCombo.setItems(allVersions);
        if (allVersions.length > 1) {
            versionCombo.add(this.allVersions);
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
    public boolean validateSourceGroup() {
        return true;
    }

    /**
     * Create the export options specification widgets.
     * 
     */
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

            public void widgetSelected(SelectionEvent e) {

                exportDependencies.setEnabled(jobItemButton.getSelection());
                if (!jobItemButton.getSelection()) {
                    exportDependencies.setSelection(false);
                }
            }
        });
        exportDependencies.setLayoutData(gd);
        // feature 19312
        exportDependencies.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshExportDependNodes();
                exportDependenciesSelected();
            }
        });

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

            public void widgetSelected(SelectionEvent e) {
                List<ContextParameterType> contextEditableResultValuesList = manager.getContextEditableResultValuesList();
                List<ContextParameterType> contextValueList = new ArrayList<ContextParameterType>();
                if (contextEditableResultValuesList == null) {
                    contextValueList = getJobContextValues((ProcessItem) process[0].getItem(), contextCombo.getText());
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

    /**
     * 
     * DOC yhch Comment method "exportDependenciesSelected".
     */
    private void exportDependenciesSelected() {
        final Collection<Item> selectedItems = getSelectedItems();

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
                        IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();
                        RelationshipItemBuilder builder = RelationshipItemBuilder.getInstance();
                        for (Item item : selectedItems) {
                            if (item == null) {
                                continue;
                            }
                            List<RelationshipItemBuilder.Relation> relations = builder.getItemsRelatedTo(item.getProperty()
                                    .getId(), item.getProperty().getVersion(), RelationshipItemBuilder.JOB_RELATION);
                            for (RelationshipItemBuilder.Relation relation : relations) {
                                try {
                                    IRepositoryViewObject obj = factory.getLastVersion(relation.getId());
                                    if (obj != null) {
                                        RepositoryNode repositoryNode = RepositoryNodeUtilities.getRepositoryNode(obj, false);
                                        if (repositoryNode != null) {
                                            if (!repositoryObjects.contains(obj)) {
                                                repositoryObjects.add(obj);
                                            }
                                        }
                                    }
                                } catch (PersistenceException et) {
                                    ExceptionHandler.process(et);
                                }
                            }

                        }

                    }
                });
                monitor.worked(60);
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
                        CheckboxTreeViewer viewer = (CheckboxTreeViewer) treeViewer.getExportItemsTreeViewer().getViewer();
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

    /**
     * Get all selected items to export.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private Collection<Item> getSelectedItems() {
        // add this if user use filter
        Set checkedElements = new HashSet();
        for (Object obj : treeViewer.getFilteredCheckboxTree().getCheckedLeafNodes()) {
            checkedElements.add(obj);
        }

        // add this if user does not use filter
        for (Object obj : treeViewer.getFilteredCheckboxTree().getViewer().getCheckedElements()) {
            RepositoryNode repositoryNode = (RepositoryNode) obj;
            if (!isRepositoryFolder(repositoryNode) && !(repositoryNode instanceof ProjectRepositoryNode)) {
                checkedElements.add(obj);
            }
        }

        Object[] elements = checkedElements.toArray();

        Map<String, Item> items = new HashMap<String, Item>();
        collectNodes(items, elements);
        return items.values();
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
        RepositoryContentProvider repositoryContentProvider = (RepositoryContentProvider) RepositoryView.show().getViewer()
                .getContentProvider();
        collectNodes(items, repositoryContentProvider.getChildren(repositoryNode));
    }

    private static boolean isRepositoryFolder(RepositoryNode node) {
        final ENodeType type = node.getType();
        if (type == ENodeType.SIMPLE_FOLDER || type == ENodeType.STABLE_SYSTEM_FOLDER || type == ENodeType.SYSTEM_FOLDER) {
            return true;
        }
        return false;
    }

    private void refreshExportDependNodes() {
        checkedNodes.clear();
        if (nodes.length <= 0) {
            return;
        }
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] instanceof RepositoryNode) {
                RepositoryNode checkedNode = (RepositoryNode) nodes[i];
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

    /**
     * DOC zli JobScriptsExportWizardPage class global comment. Detailled comment
     */
    // for feature 11976
    class ParametersValuesDialog extends Dialog {

        private String contextParameterName = Messages.getString("ParametersValuesDialog_Name"); //$NON-NLS-1$

        private String contextParameterValue = Messages.getString("ParametersValuesDialog_Value"); //$NON-NLS-1$

        private TableViewer tableViewer;

        private Table table;

        private List<ContextParameterType> contextValueList;

        private List<ContextParameterType> contextEditableValuesList;

        private List<ContextParameterType> contextResultValuesList;

        private Button setContextButton;

        private Button addButton;

        private Button removeButton;

        private String addParameterName = "new";

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
            targetPath = getSuDestinationFilePath();
        } else {
            targetPath = getDestinationValue();
        }
        if (this.selectedJobVersion != null && this.selectedJobVersion.equals(this.allVersions)) {

            if (this.originalRootFolderName == null) {
                this.originalRootFolderName = getRootFolderName();
            }
            String newFileName = this.originalRootFolderName + manager.getSelectedJobVersion() + outputFileSuffix;
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
    public boolean finish() {
        if (treeViewer != null) {
            treeViewer.removeCheckStateListener(checkStateListener);
            // achen added
            if (getCheckNodes() != null) {
                setNodes(getCheckNodes());
            }
        }

        List<ContextParameterType> contextEditableResultValuesList = manager.getContextEditableResultValuesList();

        manager = createJobScriptsManager();
        // for feature:11976, recover back the old default manager value with ContextParameters
        if (contextEditableResultValuesList == null) {
            manager.setContextEditableResultValuesList(new ArrayList());
        } else {
            manager.setContextEditableResultValuesList(contextEditableResultValuesList);
        }

        if (manager instanceof PetalsJobJavaScriptsManager) {
            PetalsTemporaryOptionsKeeper.INSTANCE.setSelection(selection);
        }
        manager.setMultiNodes(isMultiNodes());
        // achen modify to fix bug 0006222
        IRunnableWithProgress worker = new IRunnableWithProgress() {

            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                final EventLoopProgressMonitor progressMonitor = new EventLoopProgressMonitor(monitor);

                progressMonitor.beginTask(
                        Messages.getString("JobScriptsExportWizardPage.exportJobScript"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                if (selectedJobVersion != null && selectedJobVersion.equals(allVersions)) {
                    String[] allVersions = JobVersionUtils.getAllVersions(nodes[0]);
                    for (String version : allVersions) {
                        monitor.subTask(Messages.getString("JobScriptsExportWizardPage.exportJob0", nodes[0].getLabel(), version)); //$NON-NLS-1$
                        ok = exportJobScript(version, progressMonitor);
                        if (!ok) {
                            return;
                        }
                    }
                } else {
                    monitor.subTask(Messages.getString(
                            "JobScriptsExportWizardPage.exportJob1", nodes[0].getLabel(), selectedJobVersion)); //$NON-NLS-1$
                    ok = exportJobScript(selectedJobVersion, progressMonitor);
                    if (!ok) {
                        return;
                    }
                }
                monitor.subTask(Messages.getString(
                        "JobScriptsExportWizardPage.exportJobSucessful", nodes[0].getLabel(), selectedJobVersion)); //$NON-NLS-1$
                progressMonitor.done();
            }
        };
        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
        try {
            progressService.run(false, true, worker);
        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
        } catch (InterruptedException e) {
            ExceptionHandler.process(e);
        }

        if (treeViewer != null) {
            treeViewer.dispose();
        }
        nodes = null;
        process = null;
        selection = null;

        // end
        RepositoryManager.refreshCreatedNode(ERepositoryObjectType.PROCESS);
        return ok;
    }

    /**
     * ftang Comment method "exportJobScript".
     * 
     * @return
     */
    private boolean exportJobScript(String version, IProgressMonitor monitor) {
        manager.setJobVersion(version);
        // monitor.subTask("Init export choices...");
        Map<ExportChoice, Object> exportChoiceMap = getExportChoiceMap();
        boolean canExport = false;
        for (ExportChoice choice : ExportChoice.values()) {
            // if (choice.equals(ExportChoice.needGenerateCode)) {
            // continue;
            // }
            if (exportChoiceMap.get(choice) != null && exportChoiceMap.get(choice) instanceof Boolean
                    && (Boolean) exportChoiceMap.get(choice)) {
                canExport = true;
                break;
            }
        }
        if (!canExport) {
            MessageDialog.openInformation(getContainer().getShell(),
                    Messages.getString("JobScriptsExportWizardPage.exportResourceError"), //$NON-NLS-1$
                    Messages.getString("JobScriptsExportWizardPage.chooseResource")); //$NON-NLS-1$
            return false;
        }

        if (!ensureTargetIsValid()) {
            return false;
        }
        // String topFolder = getRootFolderName();

        boolean isNotFirstTime = this.originalRootFolderName != null;
        if (isNotFirstTime && process[0] != null) {
            process[0].setDirectoryName(this.originalRootFolderName);

        }
        try {
            ProxyRepositoryFactory.getInstance().initialize();
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        ItemCacheManager.clearCache();

        if (!isMultiNodes()) {
            for (int i = 0; i <= process.length - 1; i++) {
                process[i].removeAllMap();
                ProcessItem processItem = (ProcessItem) process[i].getItem();
                if (!processItem.getProperty().getVersion().equals(version)) {
                    processItem = ItemCacheManager.getProcessItem(processItem.getProperty().getId(), version);
                    // update with the correct version.
                    process[i].setProcess(processItem);
                }
            }

        }

        manager.setProgressMonitor(monitor);
        List<ExportFileResource> resourcesToExport = null;
        try {
            resourcesToExport = getExportResources();
            // if job has compile error, will not export to avoid problem if run jobscript
            boolean hasErrors = CorePlugin.getDefault().getRunProcessService().checkExportProcess(selection, true);
            if (hasErrors) {
                manager.deleteTempFiles();
                return false;
            }
        } catch (ProcessorException e) {
            MessageBoxExceptionHandler.process(e);
            return false;
        }
        if (manager instanceof PetalsJobJavaScriptsManager) {
            setTopFolderForPetals();
        } else if (manager instanceof JobJavaScriptOSGIForESBManager) {
            // do nothing.
        } else {
            if (isNotFirstTime) {
                setTopFolder(resourcesToExport, this.originalRootFolderName);
            } else {
                setTopFolder(resourcesToExport, this.getOriginalRootFolderName());// this.getOriginalRootFolderName()
                // getRootFolderName()
            }
        }

        // Save dirty editors if possible but do not stop if not all are saved
        saveDirtyEditors();
        // about to invoke the operation so save our state
        saveWidgetValues();
        // boolean ok =executeExportOperation(new ArchiveFileExportOperationFullPath(process));
        ArchiveFileExportOperationFullPath exporterOperation = getExporterOperation(resourcesToExport);

        ok = executeExportOperation(exporterOperation);

        // path can like name/name
        manager.deleteTempFiles();
        ProcessorUtilities.resetExportConfig();

        String projectName = ((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                .getProject().getLabel();

        List<JobResource> jobResources = new ArrayList<JobResource>();

        for (int i = 0; i < process.length; i++) {
            // don't update anymore, it should be done automatically in function getItem when needed.
            // try {
            // process[i].setProcess((ProcessItem) ProxyRepositoryFactory.getInstance().getUptodateProperty(
            // process[i].getItem().getProperty()).getItem());
            // } catch (PersistenceException e) {
            // MessageBoxExceptionHandler.process(e);
            // return false;
            // }
            ProcessItem processItem = (ProcessItem) process[i].getItem();
            JobInfo jobInfo = new JobInfo(processItem, processItem.getProcess().getDefaultContext(), version);
            jobResources.add(new JobResource(projectName, jobInfo));

            Set<JobInfo> jobInfos = ProcessorUtilities.getChildrenJobInfo(processItem);
            for (JobInfo subjobInfo : jobInfos) {
                jobResources.add(new JobResource(projectName, subjobInfo));
            }
        }

        JobResourceManager reManager = JobResourceManager.getInstance();
        for (JobResource r : jobResources) {
            if (reManager.isProtected(r)) {
                try {
                    ProcessorUtilities.generateCode(r.getJobInfo().getJobId(), r.getJobInfo().getContextName(), r.getJobInfo()
                            .getJobVersion(), false, false, monitor);
                } catch (ProcessorException e) {
                    MessageBoxExceptionHandler.process(e);
                    return false;
                }
            }
            // else {
            // try {
            // reManager.deleteResource(r);
            // } catch (Exception e) {
            // ExceptionHandler.process(e);
            // }
            // }
        }
        monitor.subTask(Messages.getString("JobScriptsExportWizardPage.exportSuccess")); //$NON-NLS-1$
        // achen modify to fix bug 0006108
        // rearchieve the jobscript zip file
        // if (curLanguage == ECodeLanguage.JAVA) {
        reBuildJobZipFile();
        // }
        // see bug 7181
        if (zipOption != null && zipOption.equals("true")) { //$NON-NLS-1$
            // unzip
            try {
                String zipFile = getDestinationValue();
                ZipToFile.unZipFile(getDestinationValue(), new File(zipFile).getParentFile().getAbsolutePath());
            } catch (Exception e) {
                MessageBoxExceptionHandler.process(e);
                return false;
            }
        }
        return ok;
    }

    /**
     * 
     * DOC aiming Comment method "reBuildJobZipFile".
     */
    private void reBuildJobZipFile() {
        JavaJobExportReArchieveCreator creator = null;
        String zipFile = getTempDestinationValue();
        String destinationZipFile = null;
        if (manager instanceof PetalsJobJavaScriptsManager) {
            destinationZipFile = getSuDestinationFilePath();
        } else {
            destinationZipFile = getDestinationValue();
        }

        String tmpFolder = JavaJobExportReArchieveCreator.getTmpFolder();
        try {
            // unzip to tmpFolder
            ZipToFile.unZipFile(zipFile, tmpFolder);
            // build new jar
            for (int i = 0; i < process.length; i++) {
                if (process[i] != null) {
                    String jobFolderName = process[i].getDirectoryName();
                    int pos = jobFolderName.indexOf("/"); //$NON-NLS-1$
                    if (pos != -1) {
                        jobFolderName = jobFolderName.substring(pos + 1);
                    }
                    if (creator == null) {
                        creator = new JavaJobExportReArchieveCreator(zipFile, jobFolderName);
                    } else {
                        creator.setJobFolerName(jobFolderName);
                    }
                    creator.buildNewJar();
                }
            }
            // rezip the tmpFolder to zipFile
            ZipToFile.zipFile(tmpFolder, destinationZipFile);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally {
            JavaJobExportReArchieveCreator.deleteTempFiles();
            JavaJobExportReArchieveCreator.deleteTempDestinationFiles();
        }
    }

    /**
     * Get the export operation.
     * 
     * @param resourcesToExport
     * @return
     */
    public ArchiveFileExportOperationFullPath getExporterOperation(List<ExportFileResource> resourcesToExport) {
        ArchiveFileExportOperationFullPath exporterOperation = new ArchiveFileExportOperationFullPath(resourcesToExport,
                getTempDestinationValue());
        return exporterOperation;
    }

    /**
     * DOC zli Comment method "getTempDestinationValue".
     * 
     * @return
     */
    protected String getTempDestinationValue() {
        String idealSuffix = getOutputSuffix();
        String destinationText = this.getInitDestinationFilePath();// getDestinationValue();//
        String tempdestination = JavaJobExportReArchieveCreator.getTmpDestinationFolder();
        if (destinationText.indexOf("\\") != -1) {
            int lastIndexOf = destinationText.lastIndexOf("\\");
            String substring = destinationText.substring(lastIndexOf + 1, destinationText.length());
            tempdestination = tempdestination + "/" + substring;
        }
        if (tempdestination.length() != 0 && !tempdestination.endsWith(File.separator)) {
            int dotIndex = tempdestination.lastIndexOf('.');
            if (dotIndex != -1) {
                // the last path seperator index
                int pathSepIndex = tempdestination.lastIndexOf(File.separator);
                if (pathSepIndex != -1 && dotIndex < pathSepIndex) {
                    tempdestination += idealSuffix;
                }
            } else {
                tempdestination += idealSuffix;
            }
        }
        if (tempdestination.endsWith(this.getSelectedJobVersion() + this.getOutputSuffix())) {
            return tempdestination;
        }
        return tempdestination;

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
     * Returns the root folder name.
     * 
     * @return
     */
    private String getRootFolderName() {
        IPath path = null;
        if (manager instanceof PetalsJobJavaScriptsManager) {
            path = new Path(getSuDestinationFilePath());
        } else {
            path = new Path(this.getDestinationValue());// y
        }

        String subjectString = path.lastSegment();
        Pattern regex = Pattern.compile("(.*)(?=(\\.(tar|zip))\\b)", Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE //$NON-NLS-1$
                | Pattern.UNICODE_CASE);
        Matcher regexMatcher = regex.matcher(subjectString);
        if (regexMatcher.find()) {
            subjectString = regexMatcher.group(0);
        }
        return subjectString.trim();
    }

    private String getOriginalRootFolderName() {
        IPath path = null;
        if (manager instanceof PetalsJobJavaScriptsManager) {
            path = new Path(getSuDestinationFilePath());
        } else {
            path = new Path(this.getInitDestinationFilePath());// y
        }

        String subjectString = path.lastSegment();
        Pattern regex = Pattern.compile("(.*)(?=(\\.(tar|zip))\\b)", Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE //$NON-NLS-1$
                | Pattern.UNICODE_CASE);
        Matcher regexMatcher = regex.matcher(subjectString);
        if (regexMatcher.find()) {
            subjectString = regexMatcher.group(0);
        }
        return subjectString.trim();
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

    public void setTopFolderForPetals() {
    }

    /**
     * Answer the string to display in self as the destination type.
     * 
     * @return java.lang.String
     */
    protected String getDestinationLabel() {
        return DataTransferMessages.ArchiveExport_destinationLabel;
    }

    /**
     * Returns resources to be exported. This returns file - for just the files use getSelectedResources.
     * 
     * @return a collection of resources currently selected for export (element type: <code>IResource</code>)
     * @throws ProcessorException
     */
    public List<ExportFileResource> getExportResources() throws ProcessorException {
        Map<ExportChoice, Object> exportChoiceMap = getExportChoiceMap();
        return manager.getExportResources(process, exportChoiceMap, contextCombo.getText(), launcherCombo.getText(),
                IProcessor.NO_STATISTICS, IProcessor.NO_TRACES);
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
        exportChoiceMap.put(ExportChoice.needDependencies, exportDependencies.getSelection());
        exportChoiceMap.put(ExportChoice.setParameterValues, setParametersValueButton2.getSelection());
        // exportChoiceMap.put(ExportChoice.needGenerateCode, genCodeButton.getSelection());
        return exportChoiceMap;
    }

    /**
     * Answer the contents of self's destination specification widget. If this value does not have a suffix then add it
     * first.
     */
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
        return outputFileSuffix; //$NON-NLS-1$
    }

    /**
     * Open an appropriate destination browser so that the user can specify a source to import from.
     */
    protected void handleDestinationBrowseButtonPressed() {
        FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.zip", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.setText(""); //$NON-NLS-1$
        dialog.setFileName((String) this.getDefaultFileName().get(0));
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

            String s = (String) this.getDefaultFileName().get(0);

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
    protected void internalSaveWidgetValues() {
    }

    /**
     * Hook method for restoring widget values to the values that they held last time this wizard was used to
     * completion.
     */
    protected void restoreWidgetValues() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.wizards.datatransfer.WizardFileSystemResourceExportPage1#destinationEmptyMessage()
     */
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
        return this.selectedJobVersion;
    }

    @Override
    protected boolean validateDestinationGroup() {
        return super.validateDestinationGroup() && this.checkExport();
    }
}
