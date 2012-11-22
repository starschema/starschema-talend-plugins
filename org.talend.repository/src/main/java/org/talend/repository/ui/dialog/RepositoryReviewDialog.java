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
package org.talend.repository.ui.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.utils.time.TimeMeasure;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IESBService;
import org.talend.core.PluginChecker;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.MetadataColumnRepositoryObject;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.Query;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.HeaderFooterConnection;
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
import org.talend.core.model.param.ERepositoryCategoryType;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.HeaderFooterConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.DragAndDropManager;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.IDragAndDropServiceHandler;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.core.ui.ICDCProviderService;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.ProjectRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.SAPFunctionRepositoryObject;
import org.talend.repository.model.nodes.IProjectRepositoryNode;
import org.talend.repository.ui.utils.RecombineRepositoryNodeUtil;
import org.talend.repository.ui.views.IRepositoryView;
import org.talend.repository.viewer.ui.provider.RepositoryViewerProvider;
import org.talend.repository.viewer.ui.viewer.RepositoryTreeViewer;

/**
 * bqian check the content of the repository view. <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
public class RepositoryReviewDialog extends Dialog {

    ERepositoryObjectType type;

    String repositoryType;

    private String[] repositoryTypes;

    private RepositoryNode result;

    ITypeProcessor typeProcessor;

    /*
     * selectedNodeName,isSelectionId,selectionType for selection
     */
    private String selectedNodeName;

    private boolean isSelectionId;

    private ERepositoryObjectType selectionType;

    private ViewerFilter[] additionalFilters;

    private DatabaseTypeFilter dbSupportFilter;

    ViewerTextFilter textFilter = new ViewerTextFilter();

    private RepositoryTreeViewer repositoryTreeViewer;

    private IRepositoryView repView;

    protected RepositoryReviewDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(SWT.SHELL_TRIM | SWT.APPLICATION_MODAL | getDefaultOrientation());

        boolean debugMode = CommonsPlugin.isDebugMode();
        // debugMode = true;
        TimeMeasure.display = debugMode;
        TimeMeasure.displaySteps = debugMode;
        TimeMeasure.measureActive = debugMode;

        TimeMeasure.begin(RepositoryReviewDialog.class.getSimpleName());
    }

    /**
     * DOC bqian RepositoryReviewDialog constructor comment.
     * 
     * @param parentShell
     * @param type support ERepositoryObjectType.PROCESS -> process <br>
     * ERepositoryObjectType.METADATA --> Repository <br>
     * ERepositoryObjectType.METADATA_CON_TABLE --> Schema <br>
     * ERepositoryObjectType.METADATA_CON_QUERY --> Query <br>
     * 
     * @param repositoryType String repositoryType = elem.getElementParameter(paramName).getRepositoryValue();<br>
     * see DynamicComposite.updateRepositoryListExtra().<br>
     * 
     * 
     */
    public RepositoryReviewDialog(Shell parentShell, ERepositoryObjectType type, String repositoryType) {
        this(parentShell);
        this.type = type;
        /*
         * avoid select self repository node for Process Type.
         * 
         * borrow the repositoryType to set the current process id here.
         */
        this.repositoryType = repositoryType;
        typeProcessor = createTypeProcessor();
    }

    public RepositoryReviewDialog(Shell parentShell, String[] repositoryTypes) {
        this(parentShell);
        this.repositoryTypes = repositoryTypes;
        typeProcessor = createTypeProcessor();
    }

    public RepositoryReviewDialog(Shell parentShell, ERepositoryObjectType type, String repositoryType, String[] itemFilter) {
        this(parentShell, type, repositoryType);
        this.dbSupportFilter = new DatabaseTypeFilter(itemFilter);
    }

    /**
     * DOC ycbai RepositoryReviewDialog constructor comment.
     * 
     * @param parentShell
     * @param type
     * @param repositoryType
     * @param additionalFilter
     */
    public RepositoryReviewDialog(Shell parentShell, ERepositoryObjectType type, String repositoryType,
            ViewerFilter[] additionalFilters) {
        this(parentShell, type, repositoryType);
        this.additionalFilters = additionalFilters;
    }

    public RepositoryReviewDialog(Shell parentShell, ERepositoryObjectType type, Boolean isHeaderButton, String repositoryType) {
        this(parentShell, type, repositoryType);

        if (typeProcessor instanceof RepositoryTypeProcessor) {
            ((RepositoryTypeProcessor) typeProcessor).setHeaderButton(isHeaderButton);
        }
    }

    public RepositoryReviewDialog(Shell parentShell, ERepositoryObjectType type, String repositoryType,
            boolean hidenTypeSelection, boolean needInitialize) {
        this(parentShell, type, repositoryType);

        if (hidenTypeSelection && (typeProcessor instanceof RepositoryTypeProcessor)) {
            ((RepositoryTypeProcessor) typeProcessor).setHidenTypeSelection(hidenTypeSelection);
        }

    }

    public RepositoryReviewDialog(Shell parentShell, ERepositoryObjectType type) {
        this(parentShell, type, null);
    }

    public RepositoryReviewDialog(Shell parentShell, ITypeProcessor typeProcessor, ERepositoryObjectType type) {
        this(parentShell, type);
        this.typeProcessor = typeProcessor;
    }

    protected RepositoryTreeViewer getRepositoryTreeViewer() {
        return repositoryTreeViewer;
    }

    protected IRepositoryView getRepView() {
        if (repView == null) {
            repView = RepositoryManagerHelper.findRepositoryView();
        }
        return repView;
    }

    /**
     * bqian create the correct TypeProcessor according to the type.
     * 
     * @return
     */
    private ITypeProcessor createTypeProcessor() {
        if (type == ERepositoryObjectType.PROCESS) {
            return new JobTypeProcessor(repositoryType);
        }

        if (type == ERepositoryObjectType.METADATA) {
            return new RepositoryTypeProcessor(repositoryType);
        }

        if (type == ERepositoryObjectType.METADATA_CON_TABLE) {
            return new SchemaTypeProcessor(repositoryType);
        }

        if (type == ERepositoryObjectType.METADATA_CON_QUERY) {
            return new QueryTypeProcessor(repositoryType);
        }

        if (type == ERepositoryObjectType.METADATA_SAP_FUNCTION) {
            return new SAPFunctionProcessor(repositoryType);
        }

        if (type == ERepositoryObjectType.CONTEXT) {
            return new ContextTypeProcessor(repositoryType);
        }
        if (type == ERepositoryObjectType.METADATA_HEADER_FOOTER) {
            return new HeaderFooterTypeProcessor(repositoryType);
        }

        if (type == ERepositoryObjectType.METADATA_VALIDATION_RULES) {
            return new ValidationRuleTypeProcessor(repositoryType);
        }

        if (repositoryTypes != null) {
            return new MetadataMultiTypeProcessor(repositoryTypes);
        }

        throw new IllegalArgumentException(Messages.getString("RepositoryReviewDialog.0", type)); //$NON-NLS-1$
    }

    /**
     * Configures the shell
     * 
     * @param shell the shell
     */
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        // Set the title bar text and the size
        if (typeProcessor.getDialogTitle() == null) {
            shell.setText(Messages.getString("RepositoryReviewDialog.repositoryContent")); //$NON-NLS-1$
        } else {
            shell.setText(typeProcessor.getDialogTitle());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        TimeMeasure.step(RepositoryReviewDialog.class.getSimpleName(), "before createDialogArea..."); //$NON-NLS-1$
        GridData data = (GridData) container.getLayoutData();
        data.minimumHeight = 400;
        data.heightHint = 400;
        data.minimumWidth = 500;
        data.widthHint = 500;
        container.setLayoutData(data);

        createFilterField(container);

        Composite viewContainer = new Composite(container, SWT.BORDER);
        viewContainer.setLayout(new GridLayout());
        viewContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

        RepositoryViewerProvider provider = new RepositoryViewerProvider() {

            @Override
            protected IRepositoryNode getInputRoot(IProjectRepositoryNode projectRepoNode) {
                return typeProcessor.getInputRoot(projectRepoNode);
            }

            @Override
            protected TreeViewer createTreeViewer(Composite parent, int style) {
                return new RepositoryTreeViewer(parent, style);
            }

        };

        repositoryTreeViewer = (RepositoryTreeViewer) provider.createViewer(viewContainer);

        TimeMeasure.step(RepositoryReviewDialog.class.getSimpleName(), "finshed createViewer"); //$NON-NLS-1$

        repositoryTreeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

        addFilter(textFilter);
        if (dbSupportFilter != null) {
            addFilter(dbSupportFilter);
        }
        if (additionalFilters != null) {
            addFilter(additionalFilters);
        }
        ViewerFilter filter = typeProcessor.makeFilter();
        addFilter(filter);
        TimeMeasure.step(RepositoryReviewDialog.class.getSimpleName(), "finshed add Filters"); //$NON-NLS-1$

        TimeMeasure.step(RepositoryReviewDialog.class.getSimpleName(), "set input"); //$NON-NLS-1$ 
        repositoryTreeViewer.expandAll();
        TimeMeasure.step(RepositoryReviewDialog.class.getSimpleName(), "expandAll"); //$NON-NLS-1$

        // see feature 0003664: tRunJob: When opening the tree dialog to select the job target, it could be useful to
        // open it on previous selected job if exists
        selectNode();
        TimeMeasure.step(RepositoryReviewDialog.class.getSimpleName(), "selectNode"); //$NON-NLS-1$  

        repositoryTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                boolean highlightOKButton = isSelectionValid(event);
                getButton(IDialogConstants.OK_ID).setEnabled(highlightOKButton);
            }

        });
        repositoryTreeViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                if (getButton(IDialogConstants.OK_ID).isEnabled()) {
                    okPressed();
                }
            }
        });

        TimeMeasure.step(RepositoryReviewDialog.class.getSimpleName(), "finished createDialogArea..."); //$NON-NLS-1$ 
        TimeMeasure.end(RepositoryReviewDialog.class.getSimpleName());
        TimeMeasure.display = false;
        TimeMeasure.displaySteps = false;
        TimeMeasure.measureActive = false;

        return container;
    }

    private IRepositoryNode getInput() {
        return typeProcessor.getInputRoot(ProjectRepositoryNode.getInstance());

    }

    public void addFilter(ViewerFilter filter) {
        if (filter != null) {
            getRepositoryTreeViewer().addFilter(filter);
        }
    }

    public void addFilter(ViewerFilter[] filters) {
        if (filters != null) {
            for (ViewerFilter filter : filters) {
                addFilter(filter);
            }
        }
    }

    protected boolean isSelectionValid(SelectionChangedEvent event) {
        boolean highlightOKButton = true;
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        if (selection == null || selection.size() != 1) {
            highlightOKButton = false;
        } else {
            RepositoryNode node = (RepositoryNode) selection.getFirstElement();

            if (node.getType() != ENodeType.REPOSITORY_ELEMENT) {
                highlightOKButton = false;
            }
            // else if (t == ERepositoryObjectType.SERVICESOPERATION) {
            // return highlightOKButton;
            // }
            else if (!typeProcessor.isSelectionValid(node)) {
                highlightOKButton = false;
            }
        }
        return highlightOKButton;
    }

    /**
     * DOC bqian Comment method "createFilterField".
     * 
     * @param container
     */
    private void createFilterField(Composite container) {

        if (type != ERepositoryObjectType.PROCESS) {
            return;
        }

        // create text filter
        Label label = new Label(container, SWT.NONE);
        label.setText(Messages.getString("RepositoryReviewDialog.jobNameFormat")); //$NON-NLS-1$
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final Text text = new Text(container, SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                String pattern = text.getText();
                pattern = pattern.replace("*", ".*"); //$NON-NLS-1$ //$NON-NLS-2$
                pattern = pattern.replace("?", "."); //$NON-NLS-1$ //$NON-NLS-2$
                pattern = "(?i)" + pattern + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
                textFilter.setText(pattern);
                getRepositoryTreeViewer().refresh();
                getRepositoryTreeViewer().expandAll();
                // repositoryView.selectFirstOne();
            }
        });
    }

    public void setSelectedNodeName(String selectionNode) {
        this.selectedNodeName = selectionNode;
    }

    public void setSelectedNodeName(ERepositoryObjectType selectionType, String selectionNode, boolean isSelectionId) {
        setSelectedNodeName(selectionNode);
        this.selectionType = selectionType;
        this.isSelectionId = isSelectionId;
    }

    private void selectNode() {
        /*
         * Make sure expand all. Just notice it here, because have been expand before.
         */
        // getRepositoryTreeViewer().expandAll();
        RepositoryNode root = (RepositoryNode) getRepositoryTreeViewer().getInput();
        selectNode(root, this.selectionType, this.selectedNodeName, this.isSelectionId);
    }

    private void selectNode(RepositoryNode root, ERepositoryObjectType selectionType, String idOrLabel, boolean isSelectionId) {
        if (idOrLabel == null) {
            return;
        }
        if (selectionType != null) {
            if (root.getContentType() != selectionType || root.getObjectType() != selectionType) {
                return;
            }
        }
        boolean valid = false;
        if (isSelectionId) {
            IRepositoryViewObject object = root.getObject();
            if (object != null && idOrLabel.equals(object.getId())) {
                valid = true;
            }
        } else if (idOrLabel.equals(root.getProperties(EProperties.LABEL))) {
            valid = true;
        }
        if (valid) {
            getRepositoryTreeViewer().setSelection(new StructuredSelection(root), true);
        } else if (root.hasChildren()) {
            for (IRepositoryNode child : root.getChildren()) {
                selectNode((RepositoryNode) child, selectionType, idOrLabel, isSelectionId);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        IStructuredSelection selection = (IStructuredSelection) getRepositoryTreeViewer().getSelection();
        result = (RepositoryNode) selection.getFirstElement();
        super.okPressed();
    }

    public RepositoryNode getResult() {
        return result;
    }

    public void setJobIDList(List<String> jobIDList) {
        if (this.typeProcessor instanceof JobTypeProcessor) {
            ((JobTypeProcessor) this.typeProcessor).setJobIDList(jobIDList);
        }
    }

}

/**
 * bqian decouple the process logic of JOB, REPOSITORY, SCHEMA, QUERY <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
interface ITypeProcessor {

    boolean isSelectionValid(RepositoryNode node);

    IRepositoryNode getInputRoot(IProjectRepositoryNode projectRepoNode);

    ViewerFilter makeFilter();

    String getDialogTitle();
}

/**
 * 
 * ggu class global comment. Detailled comment
 */
abstract class MultiTypesProcessor implements ITypeProcessor {

    private String[] repositoryTypes;

    public MultiTypesProcessor(String[] repositoryTypes) {
        super();
        this.repositoryTypes = repositoryTypes;
    }

    protected String[] getRepositoryTypes() {
        return repositoryTypes;
    }

    protected abstract List<ERepositoryObjectType> getTypes();

    public IRepositoryNode getInputRoot(IProjectRepositoryNode projectRepoNode) {
        return RecombineRepositoryNodeUtil.getFixingTypesInputRoot(projectRepoNode, getTypes());
    }

    public boolean isSelectionValid(RepositoryNode node) {
        Object nodeType = node.getProperties(EProperties.CONTENT_TYPE);
        List<ERepositoryObjectType> types = getTypes();
        if (types != null) {
            for (ERepositoryObjectType type : types) {
                if (nodeType == type) {
                    return true;
                }
            }
        }
        return false;
    }

    public ViewerFilter makeFilter() {
        return new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                return selectRepositoryNode(viewer, (RepositoryNode) parentElement, (RepositoryNode) element);
            }
        };
    }

    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode parentNode, RepositoryNode node) {
        if (node == null)
            return false;
        IRepositoryViewObject object = node.getObject();
        if (object != null) {
            // column
            if (object instanceof MetadataColumnRepositoryObject) {
                return false;
            }
        }
        // hide the column folder
        if (object == null && node.getParent() != null && node.getParent().getObject() != null
                && node.getParent().getObjectType() == ERepositoryObjectType.METADATA_CON_TABLE) {
            return false;
        }
        // cdc
        ICDCProviderService cdcService = null;
        if (node.getObjectType() == ERepositoryObjectType.METADATA_CON_CDC) {
            return false;
        }
        if (isCDCConnection(node)) {
            return false;
        }
        if (PluginChecker.isCDCPluginLoaded()) {
            cdcService = (ICDCProviderService) GlobalServiceRegister.getDefault().getService(ICDCProviderService.class);
            if (cdcService != null && cdcService.isSubscriberTableNode(node)) {
                return false;
            }
        }
        return true;
    }

    protected final boolean isCDCConnection(RepositoryNode node) {
        ICDCProviderService service = null;
        if (PluginChecker.isCDCPluginLoaded()) {
            service = (ICDCProviderService) GlobalServiceRegister.getDefault().getService(ICDCProviderService.class);
        }
        if (node != null && node.getType() == ENodeType.STABLE_SYSTEM_FOLDER) {
            List<IRepositoryNode> children = node.getChildren();
            if (children != null) {
                for (IRepositoryNode child : children) {
                    if (service != null && service.isSystemSubscriberTable((RepositoryNode) child)) {
                        return true;
                    }
                }
            }
        }
        return false;

    }

    public String getDialogTitle() {
        return null;
    }

}

/**
 * 
 * ggu class global comment. Detailled comment
 */
abstract class SingleTypeProcessor extends MultiTypesProcessor {

    public SingleTypeProcessor(String repositoryType) {
        super(new String[] { repositoryType });
    }

    protected String getRepositoryType() {
        return getRepositoryTypes()[0];
    }

    protected List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> types = new ArrayList<ERepositoryObjectType>();
        ERepositoryObjectType type = getType();
        if (type != null) {
            types.add(type);
        }
        return types;
    }

    protected abstract ERepositoryObjectType getType();

    public boolean isSelectionValid(RepositoryNode node) {
        if (node.getObjectType() == getType()) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode parentNode, RepositoryNode node) {
        if (node == null)
            return false;
        if (node.getContentType() == getType()) {
            return false;
        }
        if (isCDCConnection(node)) {
            return false;
        }
        return true;
    }

}

/**
 * bqian TypeProcessor for Job. <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
class JobTypeProcessor extends SingleTypeProcessor {

    private List<String> jobIDList;

    /**
     * ggu JobTypeProcessor constructor comment.
     */
    public JobTypeProcessor(String curJobId) {
        super(curJobId);
    }

    @Override
    protected ERepositoryObjectType getType() {
        return ERepositoryObjectType.PROCESS;
    }

    public List<String> getJobIDList() {
        return jobIDList;
    }

    public void setJobIDList(List<String> jobIDList) {
        this.jobIDList = jobIDList;
    }

    public boolean isSelectionValid(RepositoryNode node) {

        ERepositoryObjectType t = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
        List<String> idList = getJobIDList();
        if (idList != null && t == ERepositoryObjectType.PROCESS) {
            if (idList.contains(node.getObject().getId())) {
                MessageDialog.openWarning(Display.getCurrent().getActiveShell(),
                        Messages.getString("RepositoryReviewDialog.waringTitle"), //$NON-NLS-1$
                        Messages.getString("RepositoryReviewDialog.waringMessages")); //$NON-NLS-1$
                return false;
            }

        }
        if (node.getProperties(EProperties.CONTENT_TYPE) == getType()) {
            return true;
        }
        return false;
        // else {
        // if (node.getProperties(EProperties.CONTENT_TYPE) != ERepositoryObjectType.METADATA_CON_TABLE) {
        // highlightOKButton = false;
        // }
        // }

    }

    @Override
    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode parentNode, RepositoryNode node) {
        if (getRepositoryType() != null && node.getObject() != null) {
            if (node.getObject().getId() == null || node.getObject().getId().equals(getRepositoryType())) {
                return false;
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.dialog.ITypeProcessor#getDialogTitle()
     */
    public String getDialogTitle() {
        return Messages.getString("OpenJobSelectionDialog.findJob"); //$NON-NLS-1$
    }
}

/**
 * bqian TypeProcessor for Repository. <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
class RepositoryTypeProcessor extends SingleTypeProcessor {

    boolean hidenTypeSelection;

    boolean isHeaderButton;

    /**
     * DOC bqian RepositoryTypeProcessor constructor comment.
     * 
     * @param repositoryType
     */
    public RepositoryTypeProcessor(String repositoryType) {
        super(repositoryType);
    }

    @Override
    protected ERepositoryObjectType getType() {
        final String repositoryType = getRepositoryType();

        if (repositoryType == null) { // all
            return ERepositoryObjectType.METADATA;
        }
        if (repositoryType.equals(ERepositoryCategoryType.DELIMITED.getName())) {
            return ERepositoryObjectType.METADATA_FILE_DELIMITED;
        }
        if (repositoryType.equals(ERepositoryCategoryType.POSITIONAL.getName())) {
            return ERepositoryObjectType.METADATA_FILE_POSITIONAL;
        }
        if (repositoryType.equals(ERepositoryCategoryType.REGEX.getName())) {
            return ERepositoryObjectType.METADATA_FILE_REGEXP;
        }
        if (repositoryType.equals(ERepositoryCategoryType.XML.getName())
                || repositoryType.equals(ERepositoryCategoryType.XMLOUTPUT.getName())) {
            return ERepositoryObjectType.METADATA_FILE_XML;
        }
        if (repositoryType.equals(ERepositoryCategoryType.LDIF.getName())) {
            return ERepositoryObjectType.METADATA_FILE_LDIF;
        }
        if (repositoryType.equals(ERepositoryCategoryType.EXCEL.getName())) {
            return ERepositoryObjectType.METADATA_FILE_EXCEL;
        }
        if (repositoryType.equals(ERepositoryCategoryType.GENERIC.getName())) {
            return ERepositoryObjectType.METADATA_GENERIC_SCHEMA;
        }
        if (repositoryType.equals(ERepositoryCategoryType.LDAP.getName())) {
            return ERepositoryObjectType.METADATA_LDAP_SCHEMA;
        }
        if (repositoryType.equals(ERepositoryCategoryType.WSDL.getName())) {
            return ERepositoryObjectType.METADATA_WSDL_SCHEMA;
        }
        if (repositoryType.equals(ERepositoryCategoryType.SALESFORCE.getName())) {
            return ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA;
        }

        if (repositoryType.startsWith(ERepositoryCategoryType.DATABASE.getName())) {
            return ERepositoryObjectType.METADATA_CONNECTIONS;
        }
        if (repositoryType.startsWith(ERepositoryCategoryType.SAP.getName())) {
            return ERepositoryObjectType.METADATA_SAPCONNECTIONS;
        }
        if (repositoryType.startsWith(ERepositoryCategoryType.HEADERFOOTER.getName())) {
            return ERepositoryObjectType.METADATA_HEADER_FOOTER;
        }
        if (repositoryType.equals(ERepositoryCategoryType.EBCDIC.getName())) {
            return ERepositoryObjectType.METADATA_FILE_EBCDIC;
        }
        if (repositoryType.equals(ERepositoryCategoryType.MDM.getName())) {
            return ERepositoryObjectType.METADATA_MDMCONNECTION;
        }
        if (repositoryType.equals(ERepositoryCategoryType.FTP.getName())) {
            return ERepositoryObjectType.METADATA_FILE_FTP;
        }
        if (repositoryType.equals(ERepositoryCategoryType.BRMS.getName())) {
            return ERepositoryObjectType.METADATA_FILE_BRMS;
        }
        if (repositoryType.equals(ERepositoryCategoryType.HL7.getName())) {
            return ERepositoryObjectType.METADATA_FILE_HL7;
        }
        // added by hyWang
        if (repositoryType.equals(ERepositoryCategoryType.RULE.getName())) {
            return ERepositoryObjectType.METADATA_FILE_RULES;
        }
        if (repositoryType.equals(ERepositoryCategoryType.VALIDATIONRULES.getName())) {
            return ERepositoryObjectType.METADATA_VALIDATION_RULES;
        }
        if (repositoryType.equals(ERepositoryCategoryType.EDIFACT.getName())) {
            return ERepositoryObjectType.METADATA_EDIFACT;
        }
        // http://jira.talendforge.org/browse/TESB-5218 LiXiaopeng
        if (repositoryType.equals("SERVICES:OPERATION") || repositoryType.equals("WEBSERVICE")) { //$NON-NLS-1$
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class)) {
                IESBService service = (IESBService) GlobalServiceRegister.getDefault().getService(IESBService.class);
                return service.getServicesType();
            }
        }
        for (IDragAndDropServiceHandler handler : DragAndDropManager.getHandlers()) {
            if (handler.getType(repositoryType) != null) {
                return handler.getType(repositoryType);
            }
        }
        return null;

    }

    public boolean isSelectionValid(RepositoryNode node) {
        // only for item
        IRepositoryViewObject object = node.getObject();
        if (object != null && object.getProperty().getItem() != null) {
            return true;
        }
        return false;
    }

    public boolean isHidenTypeSelection() {
        return this.hidenTypeSelection;
    }

    public void setHidenTypeSelection(boolean hidenTypeSelection) {
        this.hidenTypeSelection = hidenTypeSelection;
    }

    public boolean isHeaderButton() {
        return this.isHeaderButton;
    }

    public void setHeaderButton(boolean isHeaderButton) {
        this.isHeaderButton = isHeaderButton;
    }

    @Override
    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode parentNode, RepositoryNode node) {
        final String repositoryType = getRepositoryType();
        if (node == null) {
            return false;
        }
        if (node.getContentType() == ERepositoryObjectType.REFERENCED_PROJECTS) {
            return true;
        }
        // ProjectManager pManager = ProjectManager.getInstance();
        // if (!pManager.isInCurrentMainProject(node)) {
        // for sub folders
        if (node.getType() == ENodeType.STABLE_SYSTEM_FOLDER) {
            return false;
        }
        // for Db Connections
        if (node.getType() == ENodeType.SYSTEM_FOLDER) {
            return true;
        }
        // }
        IRepositoryViewObject object = node.getObject();
        if (object == null || object.getProperty().getItem() == null) {
            return false;
        }
        if (object instanceof MetadataTable) {
            return false;
        }
        Item item = object.getProperty().getItem();
        if (item instanceof FolderItem) {
            return true;
        }

        if (item instanceof ConnectionItem) {
            ConnectionItem connectionItem = (ConnectionItem) item;
            Connection connection = connectionItem.getConnection();
            // tAdvancedFileOutputXML
            if (repositoryType != null && repositoryType.equals(ERepositoryCategoryType.XMLOUTPUT.getName())) {
                if (connection instanceof XmlFileConnection && ((XmlFileConnection) connection).isInputModel()) {
                    return false;
                }
            }

            if (repositoryType.startsWith(ERepositoryCategoryType.DATABASE.getName())) {
                String currentDbType = (String) RepositoryToComponentProperty.getValue(connection, "TYPE", null); //$NON-NLS-1$
                if (repositoryType.contains(":")) { // database //$NON-NLS-1$
                    // is
                    // specified
                    // //$NON-NLS-1$
                    String neededDbType = repositoryType.substring(repositoryType.indexOf(":") + 1); //$NON-NLS-1$
                    if (hidenTypeSelection) {
                        return true;
                    }
                    if (!MetadataTalendType.sameDBProductType(neededDbType, currentDbType)) {
                        return false;
                    }
                }
            }
        }
        if (repositoryType.startsWith(ERepositoryCategoryType.HEADERFOOTER.getName())) {
            if (item instanceof HeaderFooterConnectionItem) {
                HeaderFooterConnectionItem connectionItem = (HeaderFooterConnectionItem) item;
                HeaderFooterConnection connection = (HeaderFooterConnection) connectionItem.getConnection();
                boolean isHeader = connection.isIsHeader();

                if ((isHeader && isHeaderButton) || (!isHeader && !isHeaderButton)) {
                    return true;
                } else {
                    return false;
                }

            }
        }
        return true;
    }

}

/**
 * bqian TypeProcessor for Schema. <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
class SchemaTypeProcessor extends MultiTypesProcessor {

    /**
     * DOC bqian RepositoryTypeProcessor constructor comment.
     * 
     * @param repositoryType
     */
    public SchemaTypeProcessor(String repositoryType) {
        super(new String[] { repositoryType });
    }

    protected String getRepositoryType() {
        return getRepositoryTypes()[0];
    }

    @Override
    protected List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> list = new ArrayList<ERepositoryObjectType>(50);
        String repositoryType = getRepositoryType();
        if (repositoryType != null && repositoryType.startsWith(ERepositoryCategoryType.DATABASE.getName())) {
            list.add(ERepositoryObjectType.METADATA_CONNECTIONS);
        } else {
            list.add(ERepositoryObjectType.METADATA_CONNECTIONS);
            list.add(ERepositoryObjectType.METADATA_FILE_DELIMITED);
            list.add(ERepositoryObjectType.METADATA_FILE_POSITIONAL);
            list.add(ERepositoryObjectType.METADATA_FILE_REGEXP);
            list.add(ERepositoryObjectType.METADATA_FILE_XML);
            list.add(ERepositoryObjectType.METADATA_FILE_LDIF);
            list.add(ERepositoryObjectType.METADATA_FILE_EXCEL);
            list.add(ERepositoryObjectType.METADATA_FILE_HL7);
            list.add(ERepositoryObjectType.METADATA_FILE_EBCDIC);
            list.add(ERepositoryObjectType.METADATA_FILE_FTP);
            list.add(ERepositoryObjectType.METADATA_FILE_BRMS);
            list.add(ERepositoryObjectType.METADATA_GENERIC_SCHEMA);
            list.add(ERepositoryObjectType.METADATA_LDAP_SCHEMA);
            list.add(ERepositoryObjectType.METADATA_WSDL_SCHEMA);
            list.add(ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA);
            list.add(ERepositoryObjectType.METADATA_SAPCONNECTIONS);
            list.add(ERepositoryObjectType.METADATA_RULES_MANAGEMENT);
            list.add(ERepositoryObjectType.METADATA_MDMCONNECTION);
            list.add(ERepositoryObjectType.METADATA_EDIFACT);

        }
        return list;
    }

    public boolean isSelectionValid(RepositoryNode node) {
        if (node.getObject() instanceof MetadataTable || node.getObject() instanceof SAPFunctionRepositoryObject) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode parentNode, RepositoryNode node) {
        if (super.selectRepositoryNode(viewer, parentNode, node)) {
            IRepositoryViewObject object = node.getObject();
            if (object != null) {
                // query
                if (object instanceof Query) {
                    return false;
                }
            }
            // cdc
            ICDCProviderService cdcService = null;
            if (PluginChecker.isCDCPluginLoaded()) {
                cdcService = (ICDCProviderService) GlobalServiceRegister.getDefault().getService(ICDCProviderService.class);
            }
            String repositoryType = getRepositoryType();
            if (ERepositoryCategoryType.CDC.getName().equals(repositoryType) && (object != null)) {
                if (object.getRepositoryObjectType() == ERepositoryObjectType.METADATA_CONNECTIONS) {
                    DatabaseConnectionItem item = (DatabaseConnectionItem) object.getProperty().getItem();
                    DatabaseConnection connection = (DatabaseConnection) item.getConnection();

                    if (cdcService != null && cdcService.canCreateCDCConnection(connection)) {
                        return true;
                    }
                    return false;
                }
                if (object instanceof MetadataTable) {
                    return ((MetadataTableRepositoryObject) object).getTable().isActivatedCDC();
                }
            }
            return true;
        }
        return false;
    }

}

/**
 * xye TypeProcessor for Query. <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
class SAPFunctionProcessor extends SingleTypeProcessor {

    /**
     * bqian RepositoryTypeProcessor constructor comment.
     * 
     * @param repositoryType
     */
    public SAPFunctionProcessor(String repositoryType) {
        super(repositoryType);
    }

    @Override
    protected ERepositoryObjectType getType() {
        return ERepositoryObjectType.METADATA_SAPCONNECTIONS;
    }

    public boolean isSelectionValid(RepositoryNode node) {
        if (node.getObject().getRepositoryObjectType() == ERepositoryObjectType.METADATA_SAP_FUNCTION) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode parentNode, RepositoryNode node) {
        if (node.getObject() != null && (node.getObject() instanceof MetadataTable)) {
            return false;
        }
        return true;
    }

}

// //

/**
 * DOC zli class global comment. Detailled comment
 */
class HeaderFooterTypeProcessor extends SingleTypeProcessor {

    /**
     * DOC zli HeaderFooterTypeProcessor constructor comment.
     * 
     * @param repositoryType
     */
    public HeaderFooterTypeProcessor(String repositoryType) {
        super(repositoryType);
    }

    @Override
    protected ERepositoryObjectType getType() {
        return ERepositoryObjectType.METADATA_HEADER_FOOTER;
    }

}

// ////////////
// //

/**
 * xye class global comment. Detailled comment
 */
class ContextTypeProcessor extends SingleTypeProcessor {

    /**
     * xye RepositoryTypeProcessor constructor comment.
     * 
     * @param repositoryType
     */
    public ContextTypeProcessor(String repositoryType) {
        super(repositoryType);
    }

    @Override
    protected ERepositoryObjectType getType() {
        return ERepositoryObjectType.CONTEXT;
    }

}

/**
 * bqian TypeProcessor for Query. <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
class QueryTypeProcessor extends SingleTypeProcessor {

    /**
     * bqian RepositoryTypeProcessor constructor comment.
     * 
     * @param repositoryType
     */
    public QueryTypeProcessor(String repositoryType) {
        super(repositoryType);
    }

    @Override
    protected ERepositoryObjectType getType() {
        return ERepositoryObjectType.METADATA_CONNECTIONS;
    }

    public boolean isSelectionValid(RepositoryNode node) {
        if (node.getObject() instanceof Query) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode parentNode, RepositoryNode node) {
        if (node.getObject() != null && (node.getObject() instanceof MetadataTable)) {
            return false;
        }
        if (isCDCConnection(node)) {
            return false;
        }
        return true;
    }

}

/**
 * DOC ycbai class global comment. Detailled comment
 */
class ValidationRuleTypeProcessor extends SingleTypeProcessor {

    /**
     * DOC ycbai ValidationRuleTypeProcessor constructor comment.
     * 
     * @param repositoryType
     */
    public ValidationRuleTypeProcessor(String repositoryType) {
        super(repositoryType);
    }

    @Override
    protected ERepositoryObjectType getType() {
        return ERepositoryObjectType.METADATA_VALIDATION_RULES;
    }

    @Override
    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode parentNode, RepositoryNode node) {
        if (node.getContentType() == ERepositoryObjectType.REFERENCED_PROJECTS) {
            return true;
        }
        ProjectManager pManager = ProjectManager.getInstance();
        if (!pManager.isInCurrentMainProject(node)) {
            if (node.getType() == ENodeType.STABLE_SYSTEM_FOLDER) {
                return false;
            }
            if (node.getType() == ENodeType.SYSTEM_FOLDER) {
                return true;
            }
        }

        if (node.getType() == ENodeType.SYSTEM_FOLDER) {
            return true;
        }

        if (node.getObject() == null || node.getObject().getProperty().getItem() == null) {
            return false;
        }
        if (node.getObject() instanceof MetadataTable) {
            return false;
        }
        Item item = node.getObject().getProperty().getItem();
        if (item instanceof FolderItem) {
            return true;
        }
        if (node.getObjectType() == getType()) {
            return true;
        }
        return false;

    }

}

/**
 * 
 * DOC talend class global comment. Detailled comment
 */
class MetadataMultiTypeProcessor extends MultiTypesProcessor {

    public MetadataMultiTypeProcessor(String[] repositoryTypes) {
        super(repositoryTypes);
    }

    @Override
    protected List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> types = new ArrayList<ERepositoryObjectType>();

        String[] repositoryTypes = getRepositoryTypes();
        if (repositoryTypes != null) {
            for (int i = 0; i < repositoryTypes.length; i++) {
                if (ERepositoryCategoryType.XML.getName().equals(repositoryTypes[i])) {
                    types.add(ERepositoryObjectType.METADATA_FILE_XML);
                } else if (ERepositoryCategoryType.MDM.getName().equals(repositoryTypes[i])) {
                    types.add(ERepositoryObjectType.METADATA_MDMCONNECTION);
                }
            }
        }
        return types;
    }

    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode parentNode, RepositoryNode node) {
        if (super.selectRepositoryNode(viewer, parentNode, node)) {
            IRepositoryViewObject object = node.getObject();
            if (object != null) {
                // query
                if (object instanceof Query) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public String getDialogTitle() {
        return Messages.getString("RepositoryReviewDialog.metadataTitle"); //$NON-NLS-1$
    }

}

/**
 * bqian class global comment. Detailled comment
 */
class ViewerTextFilter extends ViewerFilter {

    private String text = null;

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (text == null || text.equals("")) { //$NON-NLS-1$
            return true;
        }
        RepositoryNode node = (RepositoryNode) element;
        ENodeType nodeType = node.getType();
        if (nodeType != ENodeType.REPOSITORY_ELEMENT) {
            List<IRepositoryNode> children = node.getChildren();
            if (children.isEmpty()) {
                return false;
            }
            for (IRepositoryNode child : children) {
                if (select(viewer, null, child)) {
                    return true;
                }
            }

            return false;
        }

        final IRepositoryViewObject object = node.getObject();
        if (object != null) {
            String name = object.getProperty().getLabel();
            if (name != null) {
                return name.matches(text);
            }

        }
        return true; // always
    }
}

/**
 * wchen class global comment. Detailled comment
 */
class DatabaseTypeFilter extends ViewerFilter {

    private String[] filterItems;

    public DatabaseTypeFilter(String[] filterItems) {
        this.filterItems = filterItems;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        return isSupportNode((RepositoryNode) element, filterItems);
    }

    private boolean isSupportNode(IRepositoryNode node, String[] items) {
        if (filterItems == null) {
            return true;
        }
        List<String> asList = Arrays.asList(items);
        if (node.getObject() == null) {
            if (node.getType() == ENodeType.REFERENCED_PROJECT || node.getType() == ENodeType.SYSTEM_FOLDER) {
                return true;
            }
        } else {
            Item item = node.getObject().getProperty().getItem();
            if (item instanceof DatabaseConnectionItem) {
                DatabaseConnectionItem connItem = (DatabaseConnectionItem) item;
                DatabaseConnection connection = (DatabaseConnection) connItem.getConnection();
                if (connection != null) {
                    String databaseType = connection.getDatabaseType();
                    if (databaseType.equals(EDatabaseTypeName.ORACLEFORSID.getDisplayName())) {
                        databaseType = EDatabaseTypeName.ORACLEFORSID.getXmlName();
                    } else if (databaseType.equals(EDatabaseTypeName.ORACLESN.getDisplayName())) {
                        databaseType = EDatabaseTypeName.ORACLESN.getXmlName();
                    } else if (databaseType.equals(EDatabaseTypeName.ORACLE_OCI.getDisplayName())) {
                        databaseType = EDatabaseTypeName.ORACLE_OCI.getXmlName();
                    } else if (databaseType.equals(EDatabaseTypeName.MSSQL.getDisplayName())) {
                        databaseType = EDatabaseTypeName.MSSQL.getXmlName(); // for component
                    } else {
                        databaseType = EDatabaseTypeName.getTypeFromDbType(databaseType).getProduct();
                    }

                    if (asList.contains(databaseType)) {
                        return true;
                    }
                }
            } else if (item instanceof FolderItem) {
                return true;
            }
        }
        return false;
    }

}
