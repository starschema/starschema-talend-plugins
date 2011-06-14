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
package org.talend.repository.ui.actions.importproject;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1;
import org.osgi.framework.Bundle;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.language.ECodeLanguage;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;

/**
 * This class is used for creating a page for importing demo project.<br/>
 * 
 * @author ftang
 * 
 */
public class ImportDemoProjectPage extends WizardFileSystemResourceExportPage1 implements ISelectionChangedListener {

    private TableViewer wizardSelectionViewer;

    private Browser descriptionBrowser;

    private List<DemoProjectBean> demoProjectList;

    private int selectedDemoProjectIndex = Integer.MAX_VALUE;

    /**
     * ImportDemoProjectPage constructor.
     * 
     * @param selection
     */
    public ImportDemoProjectPage(IStructuredSelection selection) {
        super(selection);
        this.setMessage(Messages.getString("ImportDemoProjectPage.message")); //$NON-NLS-1$
        this.setTitle(Messages.getString("ImportDemoProjectPage.title")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1#createControl(org.eclipse.swt
     * .widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 10;
        container.setLayout(layout);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label label = new Label(container, SWT.NONE);
        label.setText(Messages.getString("ImportDemoProjectPage.availableProjectsPrompt")); //$NON-NLS-1$
        GridData gd = new GridData();
        label.setLayoutData(gd);

        SashForm sashForm = new SashForm(container, SWT.HORIZONTAL);
        gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 300;
        sashForm.setLayoutData(gd);

        wizardSelectionViewer = new TableViewer(sashForm, SWT.BORDER);
        createDescriptionIn(sashForm);
        initializeViewer();
        wizardSelectionViewer.addSelectionChangedListener(this);
        Dialog.applyDialogFont(container);
        setControl(container);
    }

    /**
     * DOC Administrator Comment method "createDescriptionIn".
     * 
     * @param composite
     */
    public void createDescriptionIn(Composite composite) {
        descriptionBrowser = new Browser(composite, SWT.BORDER);
        descriptionBrowser.setText(""); //$NON-NLS-1$
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 200;
        descriptionBrowser.setLayoutData(gd);
    }

    /**
     * initializeViewer.
     */
    private void initializeViewer() {
        for (int i = 0; i < this.demoProjectList.size(); i++) {
            DemoProjectBean demoProject = this.demoProjectList.get(i);

            TableItem tableItem = new TableItem(wizardSelectionViewer.getTable(), i);
            tableItem.setText(demoProject.getProjectName());
            tableItem.setImage(getImageForDemoProject(demoProject.getLanguage()));
        }
    }

    /**
     * Gets images.
     * 
     * @param language
     * @return
     */
    private Image[] getImageForDemoProject(ECodeLanguage language) {

        String languageName = language.getName();
        Image[] image = getFullImagePath(languageName);
        return image;
    }

    /**
     * getFullImagePath.
     * 
     * @param languageName
     * @return
     */
    private Image[] getFullImagePath(String languageName) {
        String relatedImagePath = null;
        if (languageName.equalsIgnoreCase(ECodeLanguage.getCodeLanguage("java").getName())) { //$NON-NLS-1$
            relatedImagePath = "icons/java.png"; //$NON-NLS-1$
        } else if (languageName.equalsIgnoreCase(ECodeLanguage.getCodeLanguage("perl").getName())) { //$NON-NLS-1$
            relatedImagePath = "icons/perl.gif"; //$NON-NLS-1$
        } else {
            relatedImagePath = "icons/perl.gif"; //$NON-NLS-1$
        }
        Bundle bundle = Platform.getBundle(RepositoryPlugin.PLUGIN_ID);
        URL url = null;
        String pluginPath = null;
        try {
            //url = FileLocator.resolve(bundle.getEntry(relatedImagePath));
            url = FileLocator.toFileURL(FileLocator.find(bundle, new Path(relatedImagePath), null));
            pluginPath = new Path(url.getFile()).toOSString();
        } catch (IOException e1) {
            ExceptionHandler.process(e1);
        }

        return new Image[] { new Image(null, pluginPath) };
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent
     * )
     */
    public void selectionChanged(SelectionChangedEvent event) {

        selectedDemoProjectIndex = ((TableViewer) event.getSource()).getTable().getSelectionIndex();

        // try {
        // Bundle bundle = Platform.getBundle(ResourcesPlugin.PLUGIN_ID);
        // URL url;
        // url = FileLocator.toFileURL(FileLocator.find(bundle, new
        // Path(this.demoProjectList.get(selectedDemoProjectIndex)
        // .getDescriptionFilePath()), null));
        //
        // String descriptionFilePath = new Path(url.getFile()).toOSString();
        // descriptionBrowser.setUrl(descriptionFilePath);

        ECodeLanguage demoLanguage = this.demoProjectList.get(selectedDemoProjectIndex).getLanguage();
        String demoDescription = CorePlugin.getDefault().getResourceService().getDemoDescription(demoLanguage);
        descriptionBrowser.setText(demoDescription);
        // } catch (IOException e) {
        // ExceptionHandler.process(e);
        // }
    }

    /**
     * Sets import demo project list.
     * 
     * @param demoProjectList
     */
    public void setImportDemoProjectList(List<DemoProjectBean> demoProjectList) {
        this.demoProjectList = demoProjectList;
    }

    /**
     * Gets the index of selected demo project.
     * 
     * @return
     */
    public int getSelectedDemoProjectIndex() {
        return selectedDemoProjectIndex;
    }
}
