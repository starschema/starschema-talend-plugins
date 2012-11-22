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
package org.talend.designer.core.ui.views.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.core.PluginChecker;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.properties.tab.HorizontalTabFactory;
import org.talend.core.properties.tab.IDynamicProperty;
import org.talend.core.properties.tab.TalendPropertyTabDescriptor;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.notes.Note;
import org.talend.designer.core.ui.editor.properties.connections.MainConnectionComposite;
import org.talend.designer.core.ui.editor.properties.notes.AbstractNotePropertyComposite;
import org.talend.designer.core.ui.editor.properties.notes.BasicNotePropertyComposite;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.designer.core.ui.views.subjob.SubjobBasicComposite;

/**
 * nrousseau class global comment. Detailled comment <br/>
 * 
 */
public class ComponentSettingsView extends ViewPart implements IComponentSettingsView {

    private static final String PARENT = "parent"; //$NON-NLS-1$

    private static final String CATEGORY = "category"; //$NON-NLS-1$

    private static final String DEFAULT = "default"; //$NON-NLS-1$

    private static final String TABLEVIEW = "table view"; //$NON-NLS-1$

    public static final String ID = "org.talend.designer.core.ui.views.properties.ComponentSettingsView"; //$NON-NLS-1$

    private HorizontalTabFactory tabFactory = null;

    private TalendPropertyTabDescriptor currentSelectedTab;

    private Element element;

    private IDynamicProperty dc = null;

    private boolean cleaned;

    private boolean selectedPrimary;

    private Map<String, Composite> parentMap = null;

    private Map<String, EComponentCategory> categoryMap = null;

    private Composite parent;

    /**
     * Getter for parentMap.
     * 
     * @return the parentMap
     */
    public Map<String, Composite> getParentMap() {
        return this.parentMap;
    }

    /**
     * Getter for categoryMap.
     * 
     * @return the categoryMap
     */
    public Map<String, EComponentCategory> getCategoryMap() {
        return this.categoryMap;
    }

    /**
     * nrousseau ComponentSettings constructor comment.
     */
    public ComponentSettingsView() {
        tabFactory = new HorizontalTabFactory();
        parentMap = new HashMap<String, Composite>();
        categoryMap = new HashMap<String, EComponentCategory>();
    }

    /**
     * DOC zwang Comment method "getPreference".
     * 
     * @return
     */
    private IPreferenceStore getPreference() {
        // TODO Auto-generated method stub
        return DesignerPlugin.getDefault().getPreferenceStore();
    }

    public Composite getParent() {
        return parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        this.parent = parent;
        tabFactory.initComposite(parent, true);
        tabFactory.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                TalendPropertyTabDescriptor descriptor = (TalendPropertyTabDescriptor) selection.getFirstElement();

                if (descriptor == null) {
                    return;
                }

                if (currentSelectedTab != null
                        && (!currentSelectedTab.getData().equals(descriptor.getData()) || currentSelectedTab.getCategory() != descriptor
                                .getCategory())) {
                    for (Control curControl : tabFactory.getTabComposite().getChildren()) {
                        curControl.dispose();
                    }
                }

                if (element == null || !element.equals(descriptor.getData()) || currentSelectedTab == null
                        || currentSelectedTab.getCategory() != descriptor.getCategory() || selectedPrimary) {
                    element = (Element) descriptor.getData();
                    currentSelectedTab = descriptor;

                    createDynamicComposite(tabFactory.getTabComposite(), (Element) descriptor.getData(), descriptor.getCategory());

                    selectedPrimary = false;
                }
            }
        });
    }

    /**
     * yzhang Comment method "createDynamicComposite".
     * 
     * @param parent
     * @param element
     * @param category
     */
    private void createDynamicComposite(Composite parent, Element element, EComponentCategory category) {
        // DynamicComposite dc = null;

        if (element instanceof Node) {
            tabFactory.getTabbedPropertyComposite().getCompactButton().setVisible(false);
            tabFactory.getTabbedPropertyComposite().getTableButton().setVisible(false);
            // dc = new DynamicComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, category, element);
            if (category == EComponentCategory.BASIC) {
                getParentMap().put(ComponentSettingsView.PARENT, parent);
                getCategoryMap().put(ComponentSettingsView.CATEGORY, category);
                // getElementMap().put(ComponentSettingsView.ELEMENT, element);
                createButtonListener();
                // tabFactory.getTabbedPropertyComposite().setVisible(true);
                if (ComponentSettingsView.DEFAULT.equals(getPreference().getString(TalendDesignerPrefConstants.VIEW_OPTIONS))) {
                    tabFactory.getTabbedPropertyComposite().setCompactView(true);
                    tabFactory.getTabbedPropertyComposite().getCompactButton()
                            .setImage(ImageProvider.getImage(EImage.COMPACT_VIEW));
                    tabFactory.getTabbedPropertyComposite().getTableButton()
                            .setImage(ImageProvider.getImage(EImage.NO_TABLE_VIEW));
                    dc = new MultipleThreadDynamicComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, category,
                            element, true);
                } else if (ComponentSettingsView.TABLEVIEW.equals(getPreference().getString(
                        TalendDesignerPrefConstants.VIEW_OPTIONS))) {
                    tabFactory.getTabbedPropertyComposite().setCompactView(false);
                    tabFactory.getTabbedPropertyComposite().getCompactButton()
                            .setImage(ImageProvider.getImage(EImage.NO_COMPACT_VIEW));
                    tabFactory.getTabbedPropertyComposite().getTableButton().setImage(ImageProvider.getImage(EImage.TABLE_VIEW));
                    dc = new MultipleThreadDynamicComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, category,
                            element, false);
                }
            } else if (category == EComponentCategory.DYNAMICS_SETTINGS) {
                dc = new AdvancedContextComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, element);
            } else if (category == EComponentCategory.SQL_PATTERN) {
                dc = new SQLPatternComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, element);
            } else {
                dc = new MultipleThreadDynamicComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, category, element,
                        true);
            }
        } else if (element instanceof Connection) {
            dc = new MainConnectionComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, category, element);
        } else if (element instanceof Note) {

            if (category == EComponentCategory.BASIC) {

                if (parent.getLayout() instanceof FillLayout) {
                    FillLayout layout = (FillLayout) parent.getLayout();
                    layout.type = SWT.VERTICAL;
                    layout.marginHeight = 0;
                    layout.marginWidth = 0;
                    layout.spacing = 0;
                }
                ScrolledComposite scrolled = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
                scrolled.setExpandHorizontal(true);
                scrolled.setExpandVertical(true);

                scrolled.setMinWidth(600);
                scrolled.setMinHeight(400);

                Composite composite = tabFactory.getWidgetFactory().createComposite(scrolled);
                scrolled.setContent(composite);
                composite.setLayout(new FormLayout());
                FormData d = new FormData();
                d.left = new FormAttachment(0, 0);
                d.right = new FormAttachment(100, 0);
                d.top = new FormAttachment(0, 0);
                d.bottom = new FormAttachment(100, 0);
                composite.setLayoutData(d);

                AbstractNotePropertyComposite c1 = new BasicNotePropertyComposite(composite, (Note) element, tabFactory);
                // AbstractNotePropertyComposite c2 = new TextNotePropertyComposite(composite, (Note) element,
                // tabFactory);
                // FormData data = new FormData();
                // data.top = new FormAttachment(c1.getComposite(), 20, SWT.DOWN);
                // data.left = new FormAttachment(0, 0);
                // data.right = new FormAttachment(100, 0);
                // c2.getComposite().setLayoutData(data);
                parent.layout();
            }
        } else if (element instanceof SubjobContainer) {
            if (category == EComponentCategory.BASIC) {
                dc = new SubjobBasicComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, element);
            }
        } else {
            tabFactory.getTabbedPropertyComposite().getCompactButton().setVisible(false);
            tabFactory.getTabbedPropertyComposite().getTableButton().setVisible(false);
            dc = new MultipleThreadDynamicComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, category, element, true);
        }

        if (parent.getChildren().length == 0) {
            if (parent.getLayout() instanceof FillLayout) {
                FillLayout layout = (FillLayout) parent.getLayout();
                layout.type = SWT.VERTICAL;
                layout.marginHeight = 0;
                layout.marginWidth = 0;
                layout.spacing = 0;
            }

            Composite composite = tabFactory.getWidgetFactory().createComposite(parent);

            composite.setLayout(new FormLayout());
            FormData d = new FormData();
            d.left = new FormAttachment(2, 0);
            d.right = new FormAttachment(100, 0);
            d.top = new FormAttachment(5, 0);
            d.bottom = new FormAttachment(100, 0);
            composite.setLayoutData(d);

            Label alertText = new Label(composite, SWT.NONE);
            alertText.setText(Messages.getString("ComponentSettingsView.noAdvancedSetting")); //$NON-NLS-1$
            alertText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            parent.layout();
        }
        if (dc != null) {
            dc.refresh();
        }
    }

    /**
     * DOC zwang Comment method "createButtons".
     */
    private void createButtonListener() {
        // TODO Auto-generated method stub
        // tabFactory.getTabbedPropertyComposite().getComposite().setBackground(
        // ImageProvider.getImage(EImage.COMPOSITE_BACKGROUND).getBackground());
        tabFactory.getTabbedPropertyComposite().getCompactButton().setVisible(true);
        tabFactory.getTabbedPropertyComposite().getTableButton().setVisible(true);

        tabFactory.getTabbedPropertyComposite().getCompactButton().addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }

            public void widgetSelected(SelectionEvent e) {
                // TODO Auto-generated method stub
                tabFactory.getTabbedPropertyComposite().setCompactView(true);
                getPreference().setValue(TalendDesignerPrefConstants.VIEW_OPTIONS, ComponentSettingsView.DEFAULT);
                tabFactory.getTabbedPropertyComposite().getCompactButton().setImage(ImageProvider.getImage(EImage.COMPACT_VIEW));
                tabFactory.getTabbedPropertyComposite().getTableButton().setImage(ImageProvider.getImage(EImage.NO_TABLE_VIEW));

                if (getDc() != null) {
                    // getDc().setCompactView(false);
                    getDc().dispose();
                    if (getParentMap().get(ComponentSettingsView.PARENT) != null
                            && getCategoryMap().get(ComponentSettingsView.CATEGORY) != null) {
                        dc = new MultipleThreadDynamicComposite(getParentMap().get(ComponentSettingsView.PARENT), SWT.H_SCROLL
                                | SWT.V_SCROLL | SWT.NO_FOCUS, getCategoryMap().get(ComponentSettingsView.CATEGORY), element,
                                true);
                        dc.refresh();
                    }
                }
            }
        });

        tabFactory.getTabbedPropertyComposite().getTableButton().addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

            }

            public void widgetSelected(SelectionEvent e) {
                // TODO Auto-generated method stub
                tabFactory.getTabbedPropertyComposite().setCompactView(false);
                getPreference().setValue(TalendDesignerPrefConstants.VIEW_OPTIONS, ComponentSettingsView.TABLEVIEW);
                tabFactory.getTabbedPropertyComposite().getCompactButton()
                        .setImage(ImageProvider.getImage(EImage.NO_COMPACT_VIEW));
                tabFactory.getTabbedPropertyComposite().getTableButton().setImage(ImageProvider.getImage(EImage.TABLE_VIEW));

                if (getDc() != null) {
                    // getDc().setCompactView(false);
                    getDc().dispose();
                    if (getParentMap().get(ComponentSettingsView.PARENT) != null
                            && getCategoryMap().get(ComponentSettingsView.CATEGORY) != null) {
                        dc = new MultipleThreadDynamicComposite(getParentMap().get(ComponentSettingsView.PARENT), SWT.H_SCROLL
                                | SWT.V_SCROLL | SWT.NO_FOCUS, getCategoryMap().get(ComponentSettingsView.CATEGORY), element,
                                false);
                        dc.refresh();
                    }
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        this.parent.setFocus();
    }

    public boolean isCleaned() {
        return this.cleaned;
    }

    public void cleanDisplay() {
        tabFactory.setInput(null);
        tabFactory.setTitle(null, null);
        tabFactory.getTabbedPropertyComposite().getComposite()
                .setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        tabFactory.getTabbedPropertyComposite().getCompactButton().setVisible(false);
        tabFactory.getTabbedPropertyComposite().getTableButton().setVisible(false);
        if (tabFactory.getTabComposite() != null) {
            for (Control curControl : tabFactory.getTabComposite().getChildren()) {
                curControl.dispose();
            }
        }
        this.element = null;
        cleaned = true;
        selectedPrimary = true;
    }

    public void setElement(Element elem) {
        if (currentSelectedTab != null && currentSelectedTab.getData().equals(elem) && !cleaned) {
            return;
        }

        EComponentCategory[] categories = getCategories(elem);
        final List<TalendPropertyTabDescriptor> descriptors = new ArrayList<TalendPropertyTabDescriptor>();
        for (EComponentCategory category : categories) {
            TalendPropertyTabDescriptor d = new TalendPropertyTabDescriptor(category);
            d.setData(elem);
            descriptors.add(d);
            // if (category.hadSubCategories()) {
            // for (EComponentCategory subCategory : category.getSubCategories()) {
            // TalendPropertyTabDescriptor subc = new TalendPropertyTabDescriptor(subCategory);
            // subc.setElement(elem);
            // d.addSubItem(subc);
            // }
            // }
        }

        tabFactory.setInput(descriptors);
        setPropertiesViewerTitle(elem);
        cleaned = false;
        tabFactory.setSelection(new IStructuredSelection() {

            public Object getFirstElement() {
                return null;
            }

            public Iterator iterator() {
                return null;
            }

            public int size() {
                return 0;
            }

            public Object[] toArray() {
                return null;
            }

            public List toList() {
                List<TalendPropertyTabDescriptor> d = new ArrayList<TalendPropertyTabDescriptor>();

                if (descriptors.size() > 0) {
                    if (currentSelectedTab != null) {
                        for (TalendPropertyTabDescriptor ds : descriptors) {
                            if (ds.getCategory() == currentSelectedTab.getCategory()) {
                                d.add(ds);
                                return d;
                            }
                        }
                    }
                    d.add(descriptors.get(0));
                }
                return d;
            }

            public boolean isEmpty() {
                return false;
            }

        });
    }

    public void selectTab(final EComponentCategory category) {
        if (tabFactory.getSelection().getCategory().equals(category)) {
            return;
        }

        List<TalendPropertyTabDescriptor> allTabs = tabFactory.getInput();
        final List<TalendPropertyTabDescriptor> selection = new ArrayList<TalendPropertyTabDescriptor>();
        for (TalendPropertyTabDescriptor talendPropertyTabDescriptor : allTabs) {
            if (talendPropertyTabDescriptor.getCategory().equals(category)) {
                selection.add(talendPropertyTabDescriptor);
            }
        }
        tabFactory.setSelection(new StructuredSelection() {

            public List toList() {
                return selection;
            }
        });
    }

    public void updatePropertiesViewerTitle() {
        if (this.element != null) {
            setPropertiesViewerTitle(this.element);
        }
    }

    /**
     * yzhang Comment method "setPropertiesViewerTitle".
     * 
     * @param elem
     */
    private void setPropertiesViewerTitle(Element elem) {
        String label = null;
        Image image = null;
        if (elem instanceof Node) {
            label = ((Node) elem).getLabel();

            String uniqueName = ((Node) elem).getUniqueName();
            if (!label.equals(uniqueName)) {
                label = label + "(" + uniqueName + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            image = CoreImageProvider.getComponentIcon(((Node) elem).getComponent(), ICON_SIZE.ICON_24);
        } else if (elem instanceof Connection) {
            label = ((Connection) elem).getElementName();
            image = ImageProvider.getImage(EImage.RIGHT_ICON);
        } else if (elem instanceof Note) {
            label = Messages.getString("ComponentSettingsView.note"); //$NON-NLS-1$
            image = ImageProvider.getImage(EImage.PASTE_ICON);
        } else if (elem instanceof SubjobContainer) {
            label = Messages.getString("ComponentSettingsView.subjob"); //$NON-NLS-1$
            image = ImageProvider.getImage(EImage.PASTE_ICON);
        }
        tabFactory.setTitle(label, image);
        super.setTitleToolTip(label);
    }

    /**
     * yzhang Comment method "getCategories".
     * 
     * @param elem
     * @return
     */
    private EComponentCategory[] getCategories(Element elem) {
        if (elem instanceof Connection) {

            EComponentCategory[] categories = EElementType.CONNECTION.getCategories();
            if (PluginChecker.isTeamEdition()) {

                Object propertyValue = elem.getPropertyValue(Connection.LINESTYLE_PROP);
                if (propertyValue instanceof EConnectionType
                        && ((EConnectionType) propertyValue).hasConnectionCategory(IConnectionCategory.FLOW)) {
                    // if (((Connection) elem).checkTraceShowEnable()) {
                    final List<EComponentCategory> list = new ArrayList<EComponentCategory>(Arrays.asList(categories));
                    list.add(EComponentCategory.BREAKPOINT);
                    return list.toArray(new EComponentCategory[0]);
                    // }
                } else if (propertyValue.equals(EConnectionType.ON_COMPONENT_OK)
                        || propertyValue.equals(EConnectionType.ON_COMPONENT_ERROR)
                        || propertyValue.equals(EConnectionType.RUN_IF) || propertyValue.equals(EConnectionType.ON_SUBJOB_OK)
                        || propertyValue.equals(EConnectionType.ON_SUBJOB_ERROR)
                        || propertyValue.equals(EConnectionType.ROUTE_WHEN) || propertyValue.equals(EConnectionType.ROUTE_CATCH)) {

                    int length = categories.length;
                    EComponentCategory[] newCategories;
                    // rusuming checkBox only for ON_SUBJOB_OK , modified by nma, order 8663
                    if (propertyValue.equals(EConnectionType.ON_SUBJOB_OK)) {
                        newCategories = new EComponentCategory[length + 1];
                        for (int i = 0; i < length; i++) {
                            newCategories[i] = categories[i];
                        }
                        EComponentCategory resuming = EComponentCategory.RESUMING;
                        newCategories[length] = resuming;
                    } else {
                        newCategories = new EComponentCategory[length];
                        for (int i = 0; i < length; i++) {
                            newCategories[i] = categories[i];
                        }
                    }
                    return newCategories;
                }

            }
            return categories;
        } else if (elem instanceof Node) {
            // if (isAdvancedType(elem)) {
            if (((Node) elem).isELTComponent()) {
                if (!((Node) elem).getComponent().getName().endsWith("Output") //$NON-NLS-1$
                        && !((Node) elem).getComponent().getName().endsWith("Input") //$NON-NLS-1$
                        && !((Node) elem).getComponent().getName().endsWith("Map") //$NON-NLS-1$
                        && !((Node) elem).getComponent().getName().endsWith("TableList") //$NON-NLS-1$
                        && !((Node) elem).getComponent().getName().endsWith("ColumnList")) { //$NON-NLS-1$
                    return EElementType.ELT_NODE.getCategories();
                }
            }
            EComponentCategory[] categories = EElementType.ADVANCED_NODE.getCategories();
            if (PluginChecker.isValidationrulesPluginLoaded() && isSupportValidationRuleNode((Node) elem)) { // show
                EComponentCategory[] newCategories = new EComponentCategory[categories.length + 1];
                System.arraycopy(categories, 0, newCategories, 0, categories.length);
                newCategories[categories.length] = EComponentCategory.VALIDATION_RULES;
                return newCategories;
            }
            return categories;
        } else if (elem instanceof Note) {
            return EElementType.NOTE.getCategories();
        } else if (elem instanceof SubjobContainer) {
            return EElementType.SUBJOB.getCategories();
        }
        return null;
    }

    /**
     * DOC ycbai Comment method "isSupportValidationRuleNode".
     * 
     * @param node
     * @return
     */
    private boolean isSupportValidationRuleNode(Node node) {
        boolean hasFlow = false;
        if (node.getComponent() != null && node.getComponent() instanceof EmfComponent) {
            EmfComponent component = (EmfComponent) node.getComponent();
            if (component.useLookup() || component.useMerge() || !component.useSchema()) {
                return false;
            }
            if (component.useFlow()) {
                hasFlow = true;
            }
        }

        return hasFlow;
    }

    public Element getElement() {
        return element;
    }

    /**
     * yzhang Comment method "isAdvancedType".
     * 
     * @param elem
     * @return
     */
    private boolean isAdvancedType(Element elem) {
        for (IElementParameter param : elem.getElementParameters()) {
            if (param.getCategory().equals(EComponentCategory.ADVANCED)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Getter for dc.
     * 
     * @return the dc
     */
    public Composite getDc() {
        return (Composite) this.dc;
    }

    // /**
    // * Getter for elementMap.
    // *
    // * @return the elementMap
    // */
    // public Map<String, Element> getElementMap() {
    // return this.elementMap;
    // }
}
