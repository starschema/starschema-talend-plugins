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
package org.talend.repository.ui.actions.toolbar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WWinPluginPulldown;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.ui.actions.AContextualAction;
import org.talend.repository.ui.actions.metadata.CreateConnectionAction;
import org.talend.repository.ui.actions.metadata.CreateFileDelimitedAction;
import org.talend.repository.ui.actions.metadata.CreateFileExcelAction;
import org.talend.repository.ui.actions.metadata.CreateFileLdifAction;
import org.talend.repository.ui.actions.metadata.CreateFilePositionalAction;
import org.talend.repository.ui.actions.metadata.CreateFileRegexpAction;
import org.talend.repository.ui.actions.metadata.CreateFileXmlAction;
import org.talend.repository.ui.actions.metadata.CreateGenericSchemaAction;
import org.talend.repository.ui.actions.metadata.CreateLDAPSchemaAction;
import org.talend.repository.ui.actions.metadata.CreateSalesforceSchemaAction;
import org.talend.repository.ui.actions.metadata.CreateWSDLSchemaAction;
import org.talend.repository.ui.actions.routines.CreateRoutineAction;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC qwei class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
public abstract class AbstractCreatToolbarAction implements IWorkbenchWindowPulldownDelegate2, IActionDelegate2 {

    /**
     * The menu created by this action
     */
    private Menu fMenu;

    protected boolean fRecreateMenu = false;

    /**
     * The action used to render this delegate.
     */
    private IAction fAction;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate2#getMenu(org.eclipse.swt.widgets.Menu)
     */
    public Menu getMenu(Menu parent) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
     */
    public void init(IAction action) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction,
     * org.eclipse.swt.widgets.Event)
     */
    public void runWithEvent(IAction action, Event event) {
        if (fMenu == null && action instanceof WWinPluginPulldown) {
            IMenuCreator menuProxy = ((WWinPluginPulldown) action).getMenuCreator();
            ToolItem item = (ToolItem) event.widget;
            menuProxy.getMenu(item.getParent());
        }
        fMenu.setVisible(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     * org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {

    }

    protected void fillMenu(Menu menu) {

        IRepositoryView repositoryView = getRepositoryView();

        if (repositoryView.containsRepositoryType(ERepositoryObjectType.PROCESS)) {
            addToMenu(menu, CorePlugin.getDefault().getDesignerCoreService().getCreateProcessAction(true), -1);
            addSeparator(menu);
        }
        if (repositoryView.containsRepositoryType(ERepositoryObjectType.BUSINESS_PROCESS)) {
            addToMenu(menu, CorePlugin.getDefault().getDiagramModelService().getCreateDiagramAction(true), -1);
            addSeparator(menu);
        }

        if (repositoryView.containsRepositoryType(ERepositoryObjectType.METADATA_CONNECTIONS)) {
            final CreateConnectionAction createConnectionAction = new CreateConnectionAction(true);
            createConnectionAction.setWorkbenchPart(repositoryView);
            addToMenu(menu, createConnectionAction, -1);
        }

        if (repositoryView.containsRepositoryType(ERepositoryObjectType.METADATA_FILE_DELIMITED)) {
            final CreateFileDelimitedAction createFileDelimitedAction = new CreateFileDelimitedAction(true);
            createFileDelimitedAction.setWorkbenchPart(repositoryView);
            addToMenu(menu, createFileDelimitedAction, -1);
        }

        if (repositoryView.containsRepositoryType(ERepositoryObjectType.METADATA_FILE_POSITIONAL)) {
            final CreateFilePositionalAction createFilePositionalAction = new CreateFilePositionalAction(true);
            createFilePositionalAction.setWorkbenchPart(repositoryView);
            addToMenu(menu, createFilePositionalAction, -1);
        }

        if (repositoryView.containsRepositoryType(ERepositoryObjectType.METADATA_FILE_REGEXP)) {
            final CreateFileRegexpAction createFileRegexpAction = new CreateFileRegexpAction(true);
            createFileRegexpAction.setWorkbenchPart(repositoryView);
            addToMenu(menu, createFileRegexpAction, -1);
        }

        if (repositoryView.containsRepositoryType(ERepositoryObjectType.METADATA_FILE_XML)) {
            final CreateFileXmlAction createFileXmlAction = new CreateFileXmlAction(true);
            createFileXmlAction.setWorkbenchPart(repositoryView);
            addToMenu(menu, createFileXmlAction, -1);
        }

        if (repositoryView.containsRepositoryType(ERepositoryObjectType.METADATA_FILE_EXCEL)) {
            final CreateFileExcelAction createFileExcelAction = new CreateFileExcelAction(true);
            createFileExcelAction.setWorkbenchPart(repositoryView);
            addToMenu(menu, createFileExcelAction, -1);
        }

        if (repositoryView.containsRepositoryType(ERepositoryObjectType.METADATA_FILE_LDIF)) {
            final CreateFileLdifAction createFileLdifAction = new CreateFileLdifAction(true);
            createFileLdifAction.setWorkbenchPart(repositoryView);
            addToMenu(menu, createFileLdifAction, -1);
        }

        if (repositoryView.containsRepositoryType(ERepositoryObjectType.METADATA_LDAP_SCHEMA)) {
            final CreateLDAPSchemaAction createLDAPSchemaAction = new CreateLDAPSchemaAction(true);
            createLDAPSchemaAction.setWorkbenchPart(repositoryView);
            addToMenu(menu, createLDAPSchemaAction, -1);
        }

        if (repositoryView.containsRepositoryType(ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA)) {
            final CreateSalesforceSchemaAction createSalesforceSchemaAction = new CreateSalesforceSchemaAction(true);
            createSalesforceSchemaAction.setWorkbenchPart(repositoryView);
            addToMenu(menu, createSalesforceSchemaAction, -1);
        }

        if (repositoryView.containsRepositoryType(ERepositoryObjectType.METADATA_GENERIC_SCHEMA)) {
            final CreateGenericSchemaAction createGenericSchemaAction = new CreateGenericSchemaAction(true);
            createGenericSchemaAction.setWorkbenchPart(repositoryView);
            addToMenu(menu, createGenericSchemaAction, -1);
        }

        if (repositoryView.containsRepositoryType(ERepositoryObjectType.METADATA_WSDL_SCHEMA)) {
            final CreateWSDLSchemaAction createWSDLSchemaAction = new CreateWSDLSchemaAction(true);
            createWSDLSchemaAction.setWorkbenchPart(repositoryView);
            addToMenu(menu, createWSDLSchemaAction, -1);
        }

        addSeparator(menu);

        if (repositoryView.containsRepositoryType(ERepositoryObjectType.ROUTINES)) {
            final CreateRoutineAction createRoutineAction = new CreateRoutineAction(true);
            createRoutineAction.setWorkbenchPart(repositoryView);
            addToMenu(menu, createRoutineAction, -1);
        }

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IConfigurationElement[] configurationElements = registry
                .getConfigurationElementsFor("org.talend.repository.toolbar_creation"); //$NON-NLS-1$
        for (int i = 0; i < configurationElements.length; i++) {
            IConfigurationElement element = configurationElements[i];
            try {
                AContextualAction action = (AContextualAction) element.createExecutableExtension("class"); //$NON-NLS-1$
                action.setToolbar(true);
                action.setWorkbenchPart(repositoryView);
                addToMenu(menu, action, -1);
            } catch (CoreException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    /**
     * Adds a separator to the given menu.
     * 
     * @param menu
     */
    protected void addSeparator(Menu menu) {
        new MenuItem(menu, SWT.SEPARATOR);
    }

    protected void addToMenu(Menu menu, IAction action, int accelerator) {
        StringBuffer label = new StringBuffer();
        if (accelerator >= 0 && accelerator < 10) {
            // add the numerical accelerator
            label.append('&');
            label.append(accelerator);
            label.append(' ');
        }
        label.append(action.getText());
        action.setText(label.toString());
        ActionContributionItem item = new ActionContributionItem(action);
        item.fill(menu, -1);
    }

    public Menu getMenu(Control parent) {
        setMenu(new Menu(parent));
        fillMenu(fMenu);
        initMenu();
        return fMenu;
    }

    private void setMenu(Menu menu) {
        if (fMenu != null) {
            fMenu.dispose();
        }
        fMenu = menu;
    }

    /**
     * Creates the menu for the action.
     */
    private void initMenu() {
        // Add listener to re-populate the menu each time
        // it is shown because of dynamic history list
        fMenu.addMenuListener(new MenuAdapter() {

            public void menuShown(MenuEvent e) {
                if (fRecreateMenu) {
                    Menu m = (Menu) e.widget;
                    MenuItem[] items = m.getItems();
                    for (int i = 0; i < items.length; i++) {
                        items[i].dispose();
                    }
                    fillMenu(m);
                    fRecreateMenu = false;
                }
            }
        });

    }

    private IRepositoryView getRepositoryView() {
        IViewPart findView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(
                IRepositoryView.VIEW_ID);
        return (IRepositoryView) findView;
    }

}
