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
package org.talend.repository.ui.wizards.newproject.copyfromeclipse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.dialogs.ResourceTreeAndListGroup;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.internal.wizards.datatransfer.IDataTransferHelpContextIds;
import org.eclipse.ui.internal.wizards.datatransfer.WizardArchiveFileResourceExportPage1;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.talend.core.prefs.GeneralParametersProvider;
import org.talend.core.prefs.GeneralParametersProvider.GeneralParameters;

/**
 * 
 */
public class TalendWizardArchiveFileResourceExportPage1 extends WizardArchiveFileResourceExportPage1 {

    private IStructuredSelection initialResourceSelection;

    private ResourceTreeAndListGroup resourceGroup;

    private final static String SELECT_TYPES_TITLE = IDEWorkbenchMessages.WizardTransferPage_selectTypes;

    private final static String SELECT_ALL_TITLE = IDEWorkbenchMessages.WizardTransferPage_selectAll;

    private final static String DESELECT_ALL_TITLE = IDEWorkbenchMessages.WizardTransferPage_deselectAll;

    public TalendWizardArchiveFileResourceExportPage1(IStructuredSelection selection) {
        super(selection);
        this.initialResourceSelection = selection;
        setTitle(DataTransferMessages.ArchiveExport_exportTitle);
        setDescription(DataTransferMessages.ArchiveExport_description);
    }

    public void createControl(Composite parent) {

        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setFont(parent.getFont());

        myCreateResourcesGroup(composite);
        myCreateButtonsGroup(composite);

        createDestinationGroup(composite);

        createOptionsGroup(composite);

        restoreResourceSpecificationWidgetValues(); // ie.- local
        restoreWidgetValues(); // ie.- subclass hook
        if (initialResourceSelection != null) {
            setupBasedOnInitialSelections();
        }

        updateWidgetEnablements();
        setPageComplete(determinePageCompletion());
        setErrorMessage(null); // should not initially have error message

        setControl(composite);

        giveFocusToDestination();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IDataTransferHelpContextIds.ZIP_FILE_EXPORT_WIZARD_PAGE);
    }

    protected void myCreateResourcesGroup(Composite parent) {

        // create the input element, which has the root resource
        // as its only child
        List<String> notExportProjects = Arrays.asList(GeneralParametersProvider
                .getStrings(GeneralParameters.PROJECTS_EXCLUDED_FROM_EXPORT));

        List input = new ArrayList();
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (int i = 0; i < projects.length; i++) {
            if (projects[i].isOpen() && !notExportProjects.contains(projects[i].getName())) {
                input.add(projects[i]);
            }
        }

        this.resourceGroup = new ResourceTreeAndListGroup(parent, input,
                getResourceProvider(IResource.FOLDER | IResource.PROJECT), WorkbenchLabelProvider
                        .getDecoratingWorkbenchLabelProvider(), getResourceProvider(IResource.FILE), WorkbenchLabelProvider
                        .getDecoratingWorkbenchLabelProvider(), SWT.NONE, DialogUtil.inRegularFontMode(parent));

        ICheckStateListener listener = new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                updateWidgetEnablements();
            }
        };

        this.resourceGroup.addCheckStateListener(listener);

    }

    protected Iterator getSelectedResourcesIterator() {
        return this.resourceGroup.getAllCheckedListItems().iterator();
    }

    protected List getWhiteCheckedResources() {
        return this.resourceGroup.getAllWhiteCheckedItems();
    }

    protected void setupBasedOnInitialSelections() {
        Iterator it = this.initialResourceSelection.iterator();
        while (it.hasNext()) {
            IResource currentResource = (IResource) it.next();
            if (currentResource.getType() == IResource.FILE) {
                this.resourceGroup.initialCheckListItem(currentResource);
            } else {
                this.resourceGroup.initialCheckTreeItem(currentResource);
            }
        }
    }

    private ITreeContentProvider getResourceProvider(final int resourceType) {
        return new WorkbenchContentProvider() {

            public Object[] getChildren(Object o) {
                if (o instanceof IContainer) {
                    IResource[] members = null;
                    try {
                        members = ((IContainer) o).members();
                    } catch (CoreException e) {
                        // just return an empty set of children
                        return new Object[0];
                    }

                    // filter out the desired resource types
                    ArrayList results = new ArrayList();
                    for (int i = 0; i < members.length; i++) {
                        // And the test bits with the resource types to see if they are what we want
                        if ((members[i].getType() & resourceType) > 0) {
                            results.add(members[i]);
                        }
                    }
                    return results.toArray();
                }
                // input element case
                if (o instanceof ArrayList) {
                    return ((ArrayList) o).toArray();
                }
                return new Object[0];
            }
        };
    }

    protected final void myCreateButtonsGroup(Composite parent) {

        Font font = parent.getFont();

        // top level group
        Composite buttonComposite = new Composite(parent, SWT.NONE);
        buttonComposite.setFont(parent.getFont());

        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.makeColumnsEqualWidth = true;
        buttonComposite.setLayout(layout);
        buttonComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

        // types edit button
        Button selectTypesButton = createButton(buttonComposite, IDialogConstants.SELECT_TYPES_ID, SELECT_TYPES_TITLE, false);

        SelectionListener listener = new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                handleTypesEditButtonPressed();
            }
        };
        selectTypesButton.addSelectionListener(listener);
        selectTypesButton.setFont(font);
        setButtonLayoutData(selectTypesButton);

        Button selectButton = createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID, SELECT_ALL_TITLE, false);

        listener = new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                resourceGroup.setAllSelections(true);
                updateWidgetEnablements();
            }
        };
        selectButton.addSelectionListener(listener);
        selectButton.setFont(font);
        setButtonLayoutData(selectButton);

        Button deselectButton = createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID, DESELECT_ALL_TITLE, false);

        listener = new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                resourceGroup.setAllSelections(false);
                updateWidgetEnablements();
            }
        };
        deselectButton.addSelectionListener(listener);
        deselectButton.setFont(font);
        setButtonLayoutData(deselectButton);

    }
}
