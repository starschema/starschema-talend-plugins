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
package org.talend.repository.preference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.image.EImage;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.commons.ui.swt.advanced.composite.ThreeCompositesSashForm;
import org.talend.core.CorePlugin;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ComponentSetting;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.designer.components.preference.labelformat.TalendPaletteLabelProvider;
import org.talend.designer.components.preference.labelformat.TalendPaletteTreeProvider;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.ComponentsFactoryProvider;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ui.actions.ShowStandardAction;

/**
 * DOC aimingchen class global comment. Detailled comment
 */
public class PaletteSettingPage extends ProjectSettingPage {

    private static final String FAMILY_SPEARATOR = "--FAMILY--"; //$NON-NLS-1$

    // private static final String DIALOG_TITLE = "Palette Settings";

    private static final boolean RESTORE = true;

    private TreeViewer hiddenViewer, displayViewer;

    private Project project = pro;

    private Button hideCompnentsButton;

    private Button displayComponentsButton;

    private ThreeCompositesSashForm compositesSachForm;

    private boolean needCodeGen;

    // <name:visiblility>
    private Map<String, Boolean> statusBackup = new HashMap<String, Boolean>();

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        init();

        Composite composite = new Composite(parent, 0);
        compositesSachForm = new ThreeCompositesSashForm(composite, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginBottom = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginWidth = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(gridData);
        addTreeViewer(compositesSachForm);
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            composite.setEnabled(false);
        }
        return composite;
    }

    private void init() {
        // this.project = pro;
        List<ComponentSetting> c = getComponentsFromProject(project);
        for (ComponentSetting componentSetting : c) {
            statusBackup.put(componentSetting.getFamily() + FAMILY_SPEARATOR + componentSetting.getName(), !componentSetting
                    .isHidden());
        }
    }

    private PaletteRoot getViewerInput() {
        return ComponentUtilities.createPaletteRootWithAllComponents();
        // PaletteRoot paletteRoot = CorePlugin.getDefault().getDesignerCoreService().getAllNodeStructure(components);
        // return paletteRoot;
    }

    /**
     * qwei Comment method "addViewer".
     * 
     * @param parent
     */
    private void addTreeViewer(ThreeCompositesSashForm parent) {

        Composite leftComposite = parent.getLeftComposite();
        Label label = new Label(leftComposite, SWT.NONE);
        label.setText("Hide");
        hiddenViewer = new TreeViewer(leftComposite);
        hiddenViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        hiddenViewer.setContentProvider(new TalendPaletteTreeProvider());
        hiddenViewer.setLabelProvider(new TalendPaletteLabelProvider());
        hiddenViewer.addFilter(getFilterForComponent(false));

        hiddenViewer.expandToLevel(2);
        hiddenViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                displayComponentsButton.setEnabled(!event.getSelection().isEmpty());
            }
        });
        createButtons(parent.getMidComposite());
        Composite rightComposite = parent.getRightComposite();
        label = new Label(rightComposite, SWT.NONE);
        label.setText("Show");
        displayViewer = new TreeViewer(rightComposite);
        displayViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        displayViewer.setContentProvider(new TalendPaletteTreeProvider());
        displayViewer.setLabelProvider(new TalendPaletteLabelProvider());
        displayViewer.addFilter(getFilterForComponent(true));

        displayViewer.expandToLevel(2);
        displayViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                hideCompnentsButton.setEnabled(!event.getSelection().isEmpty());
            }
        });

        PaletteRoot input = getViewerInput();
        getComponentsFromProject(project);
        hiddenViewer.setInput(input);
        displayViewer.setInput(input);
    }

    /**
     * DOC qwei Comment method "getFilterForHiddenComponent". isVisible false for left viewer; true for right viewer
     * 
     * @return
     */
    private ViewerFilter getFilterForComponent(final boolean isVisible) {
        ViewerFilter filter = new ViewerFilter() {

            public boolean select(Viewer viewer, Object parentElement, Object element) {
                PaletteEntry entry = (PaletteEntry) element;

                if (entry instanceof PaletteContainer) {
                    return isFolderVisible((PaletteContainer) entry, isVisible);
                }

                if (isVisible) {
                    return isComponentVisible(entry);
                } else {
                    return !isComponentVisible(entry);
                }
            }
        };
        return filter;
    }

    /**
     * Check if this folder needs to be showed. *
     * 
     * @param entry
     * @param isVisible
     * @return
     */
    protected boolean isFolderVisible(PaletteContainer container, boolean isVisible) {
        for (Iterator iterator = container.getChildren().iterator(); iterator.hasNext();) {
            PaletteEntry entry = (PaletteEntry) iterator.next();
            if (entry instanceof PaletteContainer) {
                boolean display = isFolderVisible((PaletteContainer) entry, isVisible);
                if (display) {
                    return true;
                } else {
                    continue;
                }
            } else {
                if (isVisible) {
                    if (isComponentVisible(entry)) {
                        return true;
                    }
                } else {
                    if (!isComponentVisible(entry)) {
                        return true;
                    }
                }
                continue;
            }
        }

        return false;
    }

    private void refreshViewer() {
        hiddenViewer.refresh();
        displayViewer.refresh();
    }

    private void createButtons(Composite parent) {
        Label label1 = new Label(compositesSachForm.getMidComposite(), SWT.NONE);
        GridDataFactory.swtDefaults().hint(42, 18).applyTo(label1);
        Composite buttonComposite = new Composite(compositesSachForm.getMidComposite(), SWT.BORDER);
        Label label2 = new Label(compositesSachForm.getMidComposite(), SWT.NONE);
        GridDataFactory.swtDefaults().hint(42, 0).applyTo(label2);

        GridLayout gridLayout = new GridLayout(1, true);
        buttonComposite.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        // gridData.verticalAlignment = GridData.CENTER;
        buttonComposite.setLayoutData(gridData);

        Composite buttonComposite2 = new Composite(buttonComposite, SWT.NONE);

        gridLayout = new GridLayout(1, true);
        gridLayout.marginBottom = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        gridLayout.marginTop = 0;
        gridLayout.marginWidth = 0;
        buttonComposite2.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.verticalAlignment = GridData.CENTER;
        buttonComposite2.setLayoutData(gridData);
        displayComponentsButton = new Button(buttonComposite2, SWT.NONE);
        displayComponentsButton.setImage(ImageProvider.getImage(EImage.RIGHT_ICON));
        displayComponentsButton.setToolTipText(""); //$NON-NLS-1$
        GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(displayComponentsButton);
        displayComponentsButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                setComponentVisible(hiddenViewer.getSelection(), true);
                // getButton(IDialogConstants.OK_ID).setEnabled(true);
                setValid(true);
                needCodeGen = true;
            }
        });
        hideCompnentsButton = new Button(buttonComposite2, SWT.NONE);
        hideCompnentsButton.setImage(ImageProvider.getImage(EImage.LEFT_ICON));
        hideCompnentsButton.setToolTipText(""); //$NON-NLS-1$
        gridData = new GridData();
        gridData.verticalAlignment = GridData.CENTER;
        hideCompnentsButton.setLayoutData(gridData);
        hideCompnentsButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                setComponentVisible(displayViewer.getSelection(), false);
                // getButton(IDialogConstants.OK_ID).setEnabled(true);
                setValid(true);
            }
        });

        displayComponentsButton.setEnabled(false);
        hideCompnentsButton.setEnabled(false);

    }

    /**
     * set the selected component as visible or not
     * 
     * @param selection
     * @param b
     */
    protected void setComponentVisible(ISelection selection, boolean visible) {
        IStructuredSelection sel = (IStructuredSelection) selection;
        Set<String> names = new HashSet<String>();

        for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
            PaletteEntry entry = (PaletteEntry) iterator.next();
            retreiveAllEntry(names, entry);
        }

        Set<String> usedComponents = ComponentUtilities.getComponentsUsedInProject(ProjectManager.getInstance()
                .getCurrentProject());

        boolean isUsed = false;
        for (String string : names) {
            if (!visible) {
                if (usedComponents.contains(string)) {
                    isUsed = true;
                    continue;
                }

            }
            setComponentVisible(string, visible, !RESTORE);

        }
        if (isUsed) {
            MessageDialog messageDialog = new MessageDialog(getShell(),
                    Messages.getString("PaletteSettingPage.paletteSettings"), null, //$NON-NLS-1$
                    Messages.getString("PaletteSettingPage.selection1") + "\n" //$NON-NLS-1$ //$NON-NLS-2$
                            + Messages.getString("PaletteSettingPage.selection2"), MessageDialog.INFORMATION, //$NON-NLS-1$
                    new String[] { "OK" }, 0); //$NON-NLS-1$
            messageDialog.open();
        }

        refreshViewer();
    }

    private void retreiveAllEntry(Set<String> list, PaletteEntry entry) {
        if (entry instanceof PaletteContainer) {
            PaletteContainer container = (PaletteContainer) entry;
            for (Iterator iterator = container.getChildren().iterator(); iterator.hasNext();) {
                PaletteEntry en = (PaletteEntry) iterator.next();
                retreiveAllEntry(list, en);
            }
        } else {
            String family = ComponentsFactoryProvider.getPaletteEntryFamily(entry.getParent()); //$NON-NLS-1$ //$NON-NLS-2$
            list.add(family + FAMILY_SPEARATOR + entry.getLabel());
        }

    }

    @SuppressWarnings("unchecked")
    public List<ComponentSetting> getComponentsFromProject(Project project) {
        List<ComponentSetting> components = (List<ComponentSetting>) project.getEmfProject().getComponentsSettings();
        return components;
    }

    public boolean isComponentVisible(PaletteEntry entry) {
        String label = entry.getLabel();

        String family = ComponentsFactoryProvider.getPaletteEntryFamily(entry.getParent());
        List<ComponentSetting> components = getComponentsFromProject(project);

        for (ComponentSetting componentSetting : components) {
            if (componentSetting.getName().equals(label) && componentSetting.getFamily() != null
                    && componentSetting.getFamily().equals(family)) {
                return !componentSetting.isHidden();
            }
        }

        return true;
    }

    private void setComponentVisibleForRestore(String name, boolean visible) {
        setComponentVisible(name, visible, RESTORE);
    }

    private void setComponentVisible(String name, boolean visible, boolean restore) {
        try {
            String[] names = name.split(FAMILY_SPEARATOR);
            if (names.length != 2) {
                return;
            }
            String family = names[0];
            String label = names[1];
            List<ComponentSetting> components = getComponentsFromProject(project);
            for (ComponentSetting componentSetting : components) {
                if (componentSetting.getFamily() != null && componentSetting.getFamily().equals(family)
                        && componentSetting.getName().equals(label)) {
                    componentSetting.setHidden(!visible);
                    return;
                }
            }
            if (!restore) {
                ComponentSetting cs = PropertiesFactory.eINSTANCE.createComponentSetting();
                cs.setName(label);
                cs.setHidden(!visible);
                cs.setFamily(family);
                components.add(cs);
                statusBackup.put(label, !visible);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * qwei Comment method "getViewer".
     * 
     * @return
     */
    public TreeViewer getViewer() {
        return this.hiddenViewer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        // TODO Auto-generated method stub
        // super.performApply();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        // TODO Auto-generated method stub
        boolean performOk = super.performOk();
        okPressed();
        return performOk;
    }

    protected void okPressed() {
        IProxyRepositoryFactory prf = CorePlugin.getDefault().getProxyRepositoryFactory();
        try {
            prf.saveProject(project);
            ShowStandardAction.getInstance().doRun();
            if (needCodeGen) {
                IComponentsFactory components = ComponentsFactoryProvider.getInstance();
                CorePlugin.getDefault().getCodeGeneratorService().refreshTemplates();
                CorePlugin.getDefault().getLibrariesService().resetModulesNeeded();
            }

            // ComponentUtilities.updatePalette();
        } catch (Exception ex) {
            ExceptionHandler.process(ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        // TODO Auto-generated method stub
        super.performDefaults();
        retoreDefaultSettings();
    }

    /**
     * bqian Comment method "retoreDefaultSettings".
     */
    private void retoreDefaultSettings() {
        ComponentsFactoryProvider.restoreComponentVisibilityStatus();
        refreshViewer();
        // getButton(IDialogConstants.OK_ID).setEnabled(true);
        setValid(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performCancel()
     */
    @Override
    public boolean performCancel() {
        // TODO Auto-generated method stub
        cancelPressed();
        return super.performCancel();
    }

    protected void cancelPressed() {
        for (Iterator iterator = statusBackup.keySet().iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            setComponentVisibleForRestore(name, statusBackup.get(name));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.preference.ProjectSettingPage#refresh()
     */
    @Override
    public void refresh() {

    }
}
