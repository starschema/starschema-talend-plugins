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
package org.talend.repository.ui.actions;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.image.EImage;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.utils.KeywordsValidator;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.actions.CopyObjectAction;
import org.talend.repository.ui.dialog.PastSelectorDialog;

/**
 * DOC zwang class global comment. Detailled comment
 */
public class DuplicateAction extends AContextualAction {

    private RepositoryNode sourceNode = null;

    private IStructuredSelection selection = null;

    IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    public DuplicateAction() {
        super();
        this.setText(Messages.getString("DuplicateAction.thisText.duplicate")); //$NON-NLS-1$
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.DUPLICATE_ICON));
    }

    public void init(TreeViewer viewer, IStructuredSelection selection) {

        boolean canWork = true;

        RepositoryNode node = (RepositoryNode) selection.getFirstElement();

        if (selection.isEmpty()) {
            setEnabled(false);
            return;
        }

        this.sourceNode = node;
        this.selection = selection;
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject() || !ProjectManager.getInstance().isInCurrentMainProject(node)) {
            canWork = false;
        }

        if (selection != null) {
            if (((StructuredSelection) selection).toArray().length > 1) {
                canWork = false;
            } else if (((StructuredSelection) selection).toArray().length == 1) {
                Object obj = ((StructuredSelection) selection).toList().get(0);
                if (canWork) {
                    RepositoryNode sourceNode = (RepositoryNode) obj;
                    if (!CopyObjectAction.getInstance().validateAction(sourceNode, null)) {
                        canWork = false;
                    } else if (node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.JOB_DOC
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.JOBLET_DOC) {
                        canWork = false;
                    } else if (node.getContentType() == ERepositoryObjectType.JOBS
                            || node.getContentType() == ERepositoryObjectType.JOBLETS
                            || node.getContentType() == ERepositoryObjectType.GENERATED
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.JOB_DOC
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.JOBLET_DOC) {
                        canWork = false;
                    } else if (node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_CDC
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_QUERY
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_TABLE
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_VIEW
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_SYNONYM
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_SAP_FUNCTION
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_SAP_IDOC
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.MDM_CONCEPT) {
                        canWork = false;
                    }
                }
            }
        } else {
            canWork = false;
        }

        setEnabled(canWork);
    }

    @Override
    protected void doRun() {

        if (sourceNode == null) {
            return;
        }

        Property property = (Property) sourceNode.getObject().getProperty();

        Property updatedProperty = null;
        try {
            updatedProperty = ProxyRepositoryFactory.getInstance().getLastVersion(
                    new Project(ProjectManager.getInstance().getProject(property.getItem())), property.getId()).getProperty();
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        // update the property of the node repository object
        // sourceNode.getObject().setProperty(updatedProperty);

        String initNameValue = "Copy_of_" + sourceNode.getObject().getProperty().getItem().getProperty().getLabel(); //$NON-NLS-1$

        CopyObjectAction copyObjectAction = CopyObjectAction.getInstance();

        final TreeSelection selectionInClipboard = (TreeSelection) selection;

        // see feature 0001563: Display "Save job" prompt when "copy" action for a job is requested.
        promptForSavingIfNecessary((RepositoryNode) selection.getFirstElement());

        String jobNameValue = null;

        try {
            jobNameValue = getDuplicateName(initNameValue, selectionInClipboard);
        } catch (BusinessException e) {
            jobNameValue = ""; //$NON-NLS-1$
        }

        InputDialog jobNewNameDialog = new InputDialog(null, Messages.getString("DuplicateAction.input.title"), //$NON-NLS-1$
                Messages.getString("DuplicateAction.input.message"), jobNameValue, new IInputValidator() { //$NON-NLS-1$

                    public String isValid(String newText) {
                        return validJobName(newText, selectionInClipboard);
                    }

                });

        if (jobNewNameDialog.open() != Dialog.OK) {
            return;
        }

        String jobNewName = jobNewNameDialog.getValue();

        createOperation(jobNewName, sourceNode, copyObjectAction, selectionInClipboard);

        RepositoryManager.refreshCreatedNode(sourceNode.getObjectType());

    }

    public String getDuplicateName(String value, final TreeSelection selectionInClipboard) throws BusinessException {

        if (validJobName(value, selectionInClipboard) == null) {
            return value;
        } else {
            char j = 'a';
            String temp = value;
            while (validJobName(temp, selectionInClipboard) != null) {
                if (j > 'z') {
                    throw new BusinessException(Messages.getString("DuplicateAction.cannotGenerateItem")); //$NON-NLS-1$
                }
                temp = value + "_" + (j++) + ""; //$NON-NLS-1$ //$NON-NLS-2$
            }
            return temp;
        }
    }

    /**
     * 
     * DOC YeXiaowei Comment method "isValid".
     * 
     * @param itemName
     * @param selectionInClipboard
     * @return null means valid, other means some error exist
     */
    private String validJobName(String itemName, TreeSelection selectionInClipboard) {

        IRepositoryService service = (IRepositoryService) GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
        IProxyRepositoryFactory repositoryFactory = service.getProxyRepositoryFactory();
        if (itemName.length() == 0) {
            return org.talend.core.i18n.Messages.getString("PropertiesWizardPage.NameEmptyError"); //$NON-NLS-1$
        } else if (!Pattern.matches(RepositoryConstants.getPattern(((RepositoryNode) selectionInClipboard.toArray()[0])
                .getObject().getType()), itemName)) {
            /*
             * maybe Messages.getString("PropertiesWizardPage.KeywordsError")
             */
            return org.talend.core.i18n.Messages.getString("PropertiesWizardPage.NameFormatError"); //$NON-NLS-1$
        } else {
            try {
                Item testNewItem = createNewItem();
                if (testNewItem != null) {
                    if (!repositoryFactory.isNameAvailable(testNewItem, itemName)) {
                        return org.talend.core.i18n.Messages.getString("PropertiesWizardPage.ItemExistsError"); //$NON-NLS-1$
                    }
                }
            } catch (PersistenceException e) {
                return org.talend.core.i18n.Messages.getString("PropertiesWizardPage.ItemExistsError"); //$NON-NLS-1$
            }
            // see bug 0004157: Using specific name for (main) tream
            if (isKeyword(itemName)) {
                return org.talend.core.i18n.Messages.getString("PropertiesWizardPage.KeywordsError"); //$NON-NLS-1$
            }
        }

        return null;
    }

    /**
     * DOC hcw Comment method "isKeyword".
     * 
     * @param itemName
     * @return
     */
    private boolean isKeyword(String itemName) {
        ERepositoryObjectType itemType = sourceNode.getObjectType();
        ERepositoryObjectType[] types = { ERepositoryObjectType.PROCESS, ERepositoryObjectType.ROUTINES,
                ERepositoryObjectType.JOBS, ERepositoryObjectType.JOBLET, ERepositoryObjectType.JOBLETS,
                ERepositoryObjectType.JOB_SCRIPT };
        if (Arrays.asList(types).contains(itemType)) {
            return KeywordsValidator.isKeyword(itemName);
        }
        return false;
    }

    private Item createNewItem() {

        ERepositoryObjectType repositoryType = sourceNode.getObjectType();

        Item item = null;
        if (repositoryType != null) {
            switch (repositoryType) {
            case BUSINESS_PROCESS:
                item = PropertiesFactory.eINSTANCE.createBusinessProcessItem();
                break;
            case CONTEXT:
                item = PropertiesFactory.eINSTANCE.createContextItem();
                break;
            case DOCUMENTATION:
                item = PropertiesFactory.eINSTANCE.createDocumentationItem();
                break;
            case JOBLET:
                item = PropertiesFactory.eINSTANCE.createJobletProcessItem();
                break;
            case METADATA_CONNECTIONS:
                item = PropertiesFactory.eINSTANCE.createDatabaseConnectionItem();
                break;
            case METADATA_FILE_DELIMITED:
                item = PropertiesFactory.eINSTANCE.createDelimitedFileConnectionItem();
                break;
            case METADATA_FILE_EBCDIC:
                item = PropertiesFactory.eINSTANCE.createEbcdicConnectionItem();
                break;
            case METADATA_FILE_EXCEL:
                item = PropertiesFactory.eINSTANCE.createExcelFileConnectionItem();
                break;
            case METADATA_FILE_HL7:
                item = PropertiesFactory.eINSTANCE.createHL7ConnectionItem();
                break;
            case METADATA_FILE_LDIF:
                item = PropertiesFactory.eINSTANCE.createLdifFileConnectionItem();
                break;
            case METADATA_FILE_POSITIONAL:
                item = PropertiesFactory.eINSTANCE.createPositionalFileConnectionItem();
                break;
            case METADATA_FILE_LINKRULES:
                item = PropertiesFactory.eINSTANCE.createLinkRulesItem();
                break;
            case METADATA_FILE_REGEXP:
                item = PropertiesFactory.eINSTANCE.createRegExFileConnectionItem();
                break;
            case METADATA_FILE_RULES:
                item = PropertiesFactory.eINSTANCE.createRulesItem();
                break;
            case METADATA_FILE_XML:
                item = PropertiesFactory.eINSTANCE.createXmlFileConnectionItem();
                break;
            case METADATA_GENERIC_SCHEMA:
                item = PropertiesFactory.eINSTANCE.createGenericSchemaConnectionItem();
                break;
            case METADATA_LDAP_SCHEMA:
                item = PropertiesFactory.eINSTANCE.createLDAPSchemaConnectionItem();
                break;
            case METADATA_MDMCONNECTION:
                item = PropertiesFactory.eINSTANCE.createMDMConnectionItem();
                break;
            case METADATA_SALESFORCE_SCHEMA:
                item = PropertiesFactory.eINSTANCE.createSalesforceSchemaConnectionItem();
                break;
            case METADATA_SAPCONNECTIONS:
                item = PropertiesFactory.eINSTANCE.createSAPConnectionItem();
                break;
            case METADATA_WSDL_SCHEMA:
                item = PropertiesFactory.eINSTANCE.createWSDLSchemaConnectionItem();
                break;
            case PROCESS:
                item = PropertiesFactory.eINSTANCE.createProcessItem();
                break;
            case ROUTINES:
                item = PropertiesFactory.eINSTANCE.createRoutineItem();
                break;
            case JOB_SCRIPT:
                item = PropertiesFactory.eINSTANCE.createJobScriptItem();
                break;
            case SNIPPETS:
                item = PropertiesFactory.eINSTANCE.createSnippetItem();
                break;
            case SQLPATTERNS:
                item = PropertiesFactory.eINSTANCE.createSQLPatternItem();
                break;
            case SVG_BUSINESS_PROCESS:
                item = PropertiesFactory.eINSTANCE.createSVGBusinessProcessItem();
                break;
            default:
            }
        }
        if (item != null) {
            Property property = PropertiesFactory.eINSTANCE.createProperty();
            item.setProperty(property);
        }
        return item;
    }

    private void createOperation(String newJobName, RepositoryNode target, CopyObjectAction copyObjectAction,
            TreeSelection selectionInClipboard) {

        Object currentSource = selectionInClipboard.toArray()[0];
        try {
            IPath path = RepositoryNodeUtilities.getPath(target);

            if (((RepositoryNode) currentSource).getType().equals(ENodeType.REPOSITORY_ELEMENT)) {
                Item originalItem = ((RepositoryNode) currentSource).getObject().getProperty().getItem();
                List<IRepositoryViewObject> allVersion = factory.getAllVersion(originalItem.getProperty().getId());
                for (IRepositoryViewObject obj : allVersion) {
                    if (obj.getVersion().equals(originalItem.getProperty().getVersion())) {
                        originalItem = obj.getProperty().getItem();
                        break;
                    }
                }

                if (allVersion.size() == 1) {
                    duplicateSingleVersionItem(originalItem, path, newJobName);

                } else if (allVersion.size() > 1) {
                    PastSelectorDialog dialog = new PastSelectorDialog(Display.getCurrent().getActiveShell(), allVersion,
                            sourceNode);
                    if (dialog.open() == Window.OK) {
                        Set<IRepositoryViewObject> selectedVersionItems = dialog.getSelectedVersionItems();
                        String id = null;
                        String label = null;
                        boolean isfirst = true;
                        boolean needSys = true;
                        for (IRepositoryViewObject object : selectedVersionItems) {
                            Item selectedItem = object.getProperty().getItem();
                            final Item copy = factory.copy(selectedItem, path);
                            if (isfirst) {
                                id = copy.getProperty().getId();
                                label = copy.getProperty().getLabel();
                                isfirst = false;
                            }
                            copy.getProperty().setId(id);
                            copy.getProperty().setLabel(label);
                            if (needSys && originalItem instanceof RoutineItem) {
                                String lastestVersion = getLastestVersion(selectedVersionItems);
                                if (lastestVersion.equals(copy.getProperty().getVersion())) {
                                    synDuplicatedRoutine((RoutineItem) copy);
                                    needSys = false;
                                }
                            }

                            final RepositoryWorkUnit<Object> workUnit = new RepositoryWorkUnit<Object>(this.getText(), this) {

                                @Override
                                protected void run() throws LoginException, PersistenceException {
                                    if (copy instanceof ProcessItem) {
                                        RelationshipItemBuilder.getInstance().addOrUpdateItem((ProcessItem) copy);
                                    }
                                    factory.save(copy);
                                }
                            };
                            workUnit.setAvoidUnloadResources(true);
                            factory.executeRepositoryWorkUnit(workUnit);
                        }
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    private void duplicateSingleVersionItem(final Item item, final IPath path, final String newName) {

        RepositoryWorkUnit<Object> rwu = new RepositoryWorkUnit<Object>(this.getText(), this) {

            @Override
            protected void run() throws LoginException, PersistenceException {
                try {
                    final Item newItem = factory.copy(item, path, true);
                    newItem.getProperty().setLabel(newName);
                    // qli modified to fix the bug 5400 and 6185.
                    if (newItem instanceof RoutineItem) {
                        synDuplicatedRoutine((RoutineItem) newItem);
                    }// end

                    if (newItem instanceof ProcessItem) {
                        RelationshipItemBuilder.getInstance().addOrUpdateItem((ProcessItem) newItem);
                    }

                    if (newItem instanceof ConnectionItem) {
                        ConnectionItem connectionItem = (ConnectionItem) newItem;
                        connectionItem.getConnection().getSupplierDependency().clear();
                    }
                    factory.save(newItem);
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        };
        rwu.setAvoidUnloadResources(true);
        factory.executeRepositoryWorkUnit(rwu);

    }

    private String getLastestVersion(Set<IRepositoryViewObject> set) {
        if (set.isEmpty()) {
            return null;
        }
        String version = null;
        for (IRepositoryViewObject obj : set) {
            String curVersion = obj.getProperty().getVersion();
            if (version == null) {
                version = curVersion;
            } else {
                Double dVersion = Double.valueOf(version);
                Double dCurVersion = Double.valueOf(curVersion);
                if (dCurVersion > dVersion) {
                    version = curVersion;
                }
            }
        }
        return version;
    }

    private void synDuplicatedRoutine(RoutineItem item) {
        ICodeGeneratorService codeGenService = (ICodeGeneratorService) GlobalServiceRegister.getDefault().getService(
                ICodeGeneratorService.class);
        if (codeGenService != null) {
            codeGenService.createRoutineSynchronizer().renameRoutineClass((RoutineItem) item);
            try {
                codeGenService.createRoutineSynchronizer().syncRoutine((RoutineItem) item, true);
            } catch (SystemException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    /**
     * 
     * DOC YeXiaowei Comment method "resetProcessVersion".
     * 
     * @return
     */
    protected boolean resetProcessVersion() {
        return false;
    }
}
