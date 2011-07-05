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
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC xye class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
public class ContextRepositoryReviewDialog extends RepositoryReviewDialog {

    // added by hyWang
    private IContextManager contextManager;

    private final List<IContextParameter> params;

    private Text contextNameText;

    private Button createNewButton;

    private String msg = org.talend.core.i18n.Messages.getString("PropertiesWizardPage.NameFormatError"); //$NON-NLS-1$

    private String title = Messages.getString("ContextRepositoryReviewDialog.conflictError"); //$NON-NLS-1$

    private String errorMsg = Messages.getString("ContextRepositoryReviewDialog.contextSameNameError"); //$NON-NLS-1$

    /**
     * DOC xye ContextRepositoryReviewDialog constructor comment.
     * 
     * @param parentShell
     * @param type
     */
    public ContextRepositoryReviewDialog(Shell parentShell, ERepositoryObjectType type, final List<IContextParameter> params,
            IContextManager contextManager) {
        super(parentShell, type);
        this.params = params;
        this.contextManager = contextManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.dialog.RepositoryReviewDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);

        Composite bg = new Composite(container, SWT.NULL);
        bg.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        bg.setLayout(layout);

        Label label = new Label(bg, SWT.NONE);
        label.setText(Messages.getString("BuiltinToContext.newContextName")); //$NON-NLS-1$
        contextNameText = new Text(bg, SWT.BORDER);
        contextNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        contextNameText.setEnabled(false);

        createNewButton = new Button(bg, SWT.CHECK);
        createNewButton.setText(Messages.getString("BuiltinToContext.createNewContext")); //$NON-NLS-1$
        createNewButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                contextNameText.setEnabled(createNewButton.getSelection());
                getButton(IDialogConstants.OK_ID).setEnabled(createNewButton.getSelection());
            }
        });

        return container;
    }

    private boolean nameInvalid(Text nameText) {
        return nameText.getText().length() == 0
                || !Pattern.matches(RepositoryConstants.getPattern(ERepositoryObjectType.CONTEXT), nameText.getText())
                || nameText.getText().trim().contains(" "); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.dialog.RepositoryReviewDialog#okPressed()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void okPressed() {
        if (params == null || params.isEmpty()) {
            super.okPressed();
        }

        ContextItem item = null;

        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (createNewButton.getSelection()) {
            if (nameInvalid(contextNameText)) {
                MessageDialog.openError(getShell(), "Context", msg); //$NON-NLS-1$
                return;
            } else {
                item = PropertiesFactory.eINSTANCE.createContextItem();
                if (item == null) {
                    return;
                }
                Property createProperty = PropertiesFactory.eINSTANCE.createProperty();
                createProperty
                        .setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                                .getUser());
                createProperty.setVersion(VersionUtils.DEFAULT_VERSION);
                createProperty.setStatusCode(""); //$NON-NLS-1$
                try {
                    String nextId = factory.getNextId();
                    createProperty.setId(nextId);
                    item.setProperty(createProperty);
                    for (IContext context : contextManager.getListContext()) {
                        ContextType contextType = TalendFileFactory.eINSTANCE.createContextType();
                        contextType.setName(context.getName());
                        item.getContext().add(contextType);
                    }
                    item.setDefaultContext(contextManager.getDefaultContext().getName());
                    item.getProperty().setLabel(contextNameText.getText().trim());
                    IRepositoryService service = (IRepositoryService) GlobalServiceRegister.getDefault().getService(
                            IRepositoryService.class);
                    IProxyRepositoryFactory repositoryFactory = service.getProxyRepositoryFactory();
                    try {
                        boolean nameAvaliabe = repositoryFactory.isNameAvailable(createProperty.getItem(),
                                contextNameText.getText());
                        if (!nameAvaliabe) {
                            MessageDialog.openError(getShell(), "Context", org.talend.core.i18n.Messages //$NON-NLS-1$
                                    .getString("PropertiesWizardPage.ItemExistsError")); //$NON-NLS-1$
                            return;
                        }
                    } catch (PersistenceException e) {
                        ExceptionHandler.process(e);
                        super.okPressed();
                    }
                    factory.create(item, new Path("")); //$NON-NLS-1$
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                    super.okPressed();
                }
            }
        } else {
            IStructuredSelection selection = (IStructuredSelection) repositoryView.getViewer().getSelection();
            RepositoryNode context = (RepositoryNode) selection.getFirstElement();
            try { // get the item from file
                IRepositoryViewObject contextObj = factory.getLastVersion(context.getObject().getProperty().getId());
                item = (ContextItem) contextObj.getProperty().getItem();
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                super.okPressed();
            }
        }

        IContext jobDefaultContext = contextManager.getDefaultContext(); // get default context from job
        List<IContext> jobContextlist = contextManager.getListContext(); // get all context from job
        List<String> jobContextNames = getJobContextGroupNames(jobContextlist);
        if (item == null) {
            return;
        }
        EList contextList = item.getContext();

        // if (contextList.isEmpty()) {
        // ContextType contextType = TalendFileFactory.eINSTANCE.createContextType();
        // contextList.add(contextType);
        // }
        boolean conflictflag = false;

        JobContextManager itemManager = new JobContextManager();
        itemManager.loadFromEmf(contextList, item.getDefaultContext());
        // this loop is used to get all context groups
        for (int i = 0, n = contextList.size(); i < n; i++) {
            ContextType type = (ContextType) contextList.get(i);
            if (type == null) {
                super.okPressed();
            }
            EList contextParameters = type.getContextParameter(); // parameters from current contextType
            List<IContextParameter> allParameters = getAllExistContextParameters(contextParameters, params);
            // find one same context group,this group will get the
            // values from the same group of designer
            if (jobContextNames.contains(type.getName())) {
                int index = jobContextNames.indexOf(type.getName());
                IContext currentContext = jobContextlist.get(index);
                for (IContextParameter param : params) {
                    if (!allParameters.contains(param)) {
                        IContextParameter selectedParam = currentContext.getContextParameter(param.getName());
                        ContextParameterType parameter = TalendFileFactory.eINSTANCE.createContextParameterType();
                        copyContextParameter(item, contextParameters, parameter, selectedParam);
                    } else {
                        for (int k = 0; k < contextParameters.size(); k++) {
                            ContextParameterType parameter = (ContextParameterType) contextParameters.get(k);
                            if (parameter.getName().equals(param.getName())) {
                                conflictflag = true;
                                break;
                            }
                        }

                    }
                }
                // this group will get the vaules from default group of designer
            } else if (!jobContextNames.contains(type.getName()) && type.getName() != null) {
                for (IContextParameter param : params) {
                    if (!allParameters.contains(param)) {
                        IContextParameter selectedParam = jobDefaultContext.getContextParameter(param.getName());
                        ContextParameterType parameter = TalendFileFactory.eINSTANCE.createContextParameterType();
                        copyContextParameter(item, contextParameters, parameter, selectedParam);
                    } else {
                        for (int k = 0; k < contextParameters.size(); k++) {
                            ContextParameterType parameter = (ContextParameterType) contextParameters.get(k);
                            if (parameter.getName().equals(param.getName())) {
                                conflictflag = true;
                                break;
                            }
                        }
                    }
                }
            }

            // Update some parameters
            // EList contextParameters = type.getContextParameter();
            // List<IContextParameter> updateParams = new ArrayList<IContextParameter>();
            // for (int j = 0, m = contextParameters.size(); j < m; j++) {
            // ContextParameterType parameter = (ContextParameterType) contextParameters.get(j);
            // for (IContextParameter param : params) {
            // if (parameter.getName().equals(param.getName())) {
            // copyContextParameter(item, contextParameters, parameter, param);
            // updateParams.add(param);
            // }
            // }
            // }

            // Add parameter to group
            // for (IContextParameter param : params) {
            // if (!updateParams.contains(param)) {
            // ContextParameterType parameter = TalendFileFactory.eINSTANCE.createContextParameterType();
            // copyContextParameter(item, contextParameters, parameter, param);
            // }
            // }

        }
        if (conflictflag) {
            MessageDialog.openError(getShell(), title, errorMsg);
        }

        // Save
        try {
            ProxyRepositoryFactory.getInstance().save(item, false);
            updateRelatedView();
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        super.okPressed();
    }

    private List<IContextParameter> getAllExistContextParameters(EList contextParameters, List<IContextParameter> params) {
        List<IContextParameter> allParameters = new ArrayList<IContextParameter>();
        for (int i = 0; i < contextParameters.size(); i++) {
            ContextParameterType parameter = (ContextParameterType) contextParameters.get(i);
            for (IContextParameter param : params) {
                if (parameter.getName().equals(param.getName())) {
                    allParameters.add(param);
                }
            }
        }
        return allParameters;
    }

    /**
     * DOC xye Comment method "copyContextParameter".
     * 
     * @param context
     * @param item
     * @param contextParameters
     * @param parameter
     * @param param
     */
    @SuppressWarnings("unchecked")
    private void copyContextParameter(ContextItem item, EList contextParameters, ContextParameterType parameter,
            IContextParameter selectedParam) {
        parameter.setName(selectedParam.getName());
        parameter.setComment(selectedParam.getComment());
        parameter.setPrompt(selectedParam.getPrompt());
        parameter.setPromptNeeded(selectedParam.isPromptNeeded());
        // parameter.setRepositoryContextId(item.getProperty().getId());
        parameter.setType(selectedParam.getType());
        parameter.setValue(selectedParam.getValue());
        contextParameters.add(parameter);
        selectedParam.setSource(item.getProperty().getId());
    }

    /**
     * DOC xye Comment method "updateProcessContextManager".
     */
    private void updateRelatedView() {
        // refresh context view
        IDesignerCoreService designerCoreService = RepositoryPlugin.getDefault().getDesignerCoreService();
        designerCoreService.switchToCurContextsView();
        designerCoreService.switchToCurComponentSettingsView();
        designerCoreService.switchToCurJobSettingsView();
        // refresh repository view
        IRepositoryView view = (IRepositoryView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .findView(IRepositoryView.VIEW_ID);
        if (view != null) {
            view.refresh(ERepositoryObjectType.CONTEXT);
        }
        // refresh fake repository view
        if (getRepositoryView() != null) {
            getRepositoryView().refresh();
        }
    }

    /**
     * DOC hyWang Comment method "getJobContextGroupNames".
     */
    private List<String> getJobContextGroupNames(List<IContext> jobContextlist) {
        List<String> jobContextNames = new ArrayList<String>();
        // parameters from designer of default context
        for (int k = 0; k < jobContextlist.size(); k++) {
            jobContextNames.add(k, jobContextlist.get(k).getName());
        }
        return jobContextNames;
    }

}
