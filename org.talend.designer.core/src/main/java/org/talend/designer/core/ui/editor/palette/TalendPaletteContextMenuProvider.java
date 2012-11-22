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
package org.talend.designer.core.ui.editor.palette;

import java.util.List;
import java.util.Set;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.palette.PaletteContextMenuProvider;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.Project;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.action.ComponentSearcher;
import org.talend.designer.core.ui.editor.PaletteComponentFactory;
import org.talend.repository.model.ComponentsFactoryProvider;
import org.talend.repository.ui.actions.ShowFavoriteAction;

/**
 * DOC hcw class global comment. Detailled comment
 */
public class TalendPaletteContextMenuProvider extends PaletteContextMenuProvider {

    /**
     * DOC hcw TalendPaletteContextMenuProvider constructor comment.
     * 
     * @param palette
     */

    public TalendPaletteContextMenuProvider(PaletteViewer palette) {
        super(palette);
    }

    // public TalendPaletteContextMenuProvider() {
    //
    // }

    @Override
    public void buildContextMenu(IMenuManager menu) {
        super.buildContextMenu(menu);
        menu.appendToGroup(GEFActionConstants.MB_ADDITIONS, new SearchComponentAction(getPaletteViewer()));
        PaletteEntry element = (PaletteEntry) ((EditPart) getPaletteViewer().getSelectedEditParts().get(0)).getModel();
        boolean note = element.getLabel().equals(Messages.getString("TalendEditorPaletteFactory.Note"));//$NON-NLS-1$
        if (note) {

        } else {
            if (ShowFavoriteAction.state == true) {
                menu.appendToGroup(GEFActionConstants.GROUP_COPY, new FavoriteComponentAction(getPaletteViewer()));
            } else {
                menu.appendToGroup(GEFActionConstants.GROUP_COPY, new RemoveFavoriteComponentAction(getPaletteViewer()));
            }
        }
        menu.appendToGroup(GEFActionConstants.GROUP_COPY, new HiddenFloderAction(getPaletteViewer()));
        menu.appendToGroup(GEFActionConstants.GROUP_COPY, new DisplayFloderAction(getPaletteViewer()));
    }

    class SearchComponentAction extends Action {

        private Shell shell;

        private PaletteViewer paletteViewer;

        /**
         * DOC hcw SearchComponentAction constructor comment.
         * 
         * @param paletteViewer
         */
        public SearchComponentAction(PaletteViewer paletteViewer) {
            this.paletteViewer = paletteViewer;
            shell = paletteViewer.getControl().getShell();
            setText(Messages.getString("SearchComponentInJobs.Title")); //$NON-NLS-1$
            setToolTipText(Messages.getString("SearchComponentInJobs.Tooltip")); //$NON-NLS-1$
            setDescription(Messages.getString("SearchComponentInJobs.Tooltip")); //$NON-NLS-1$
        }

        @Override
        public void run() {
            List list = paletteViewer.getSelectedEditParts();
            if (!list.isEmpty()) {
                PaletteEntry selection = (PaletteEntry) ((EditPart) list.get(0)).getModel();
                if (!(selection instanceof PaletteRoot)) {
                    ComponentSearcher searcher = new ComponentSearcher(selection.getLabel(), paletteViewer.getControl()
                            .getShell());
                    searcher.run();
                }
            }
        }

    }

    class FavoriteComponentAction extends Action {

        private Shell shell;

        private PaletteViewer paletteViewer;

        /**
         * DOC hcw SearchComponentAction constructor comment.
         * 
         * @param paletteViewer
         */
        public FavoriteComponentAction(PaletteViewer paletteViewer) {
            this.paletteViewer = paletteViewer;
            shell = paletteViewer.getControl().getShell();
            setText(Messages.getString("TalendPaletteContextMenuProvider.textAdd")); //$NON-NLS-1$
            setToolTipText(Messages.getString("TalendPaletteContextMenuProvider.tipAdd")); //$NON-NLS-1$
            setDescription(Messages.getString("TalendPaletteContextMenuProvider.descriptionAdd")); //$NON-NLS-1$
        }

        @Override
        public void run() {

            List list = paletteViewer.getSelectedEditParts();
            PaletteEntry element = (PaletteEntry) ((EditPart) list.get(0)).getModel();
            Project project = null;
            if (element instanceof TalendPaletteDrawer) {

                List eleList = ((TalendPaletteDrawer) element).getChildren();
                addListNotes(eleList, project);
            } else if (element instanceof CombinedTemplateCreationEntry) {
                addNotes((CombinedTemplateCreationEntry) element, project);

            }

        }

    }

    class RemoveFavoriteComponentAction extends Action {

        private Shell shell;

        private PaletteViewer paletteViewer;

        /**
         * DOC hcw SearchComponentAction constructor comment.
         * 
         * @param paletteViewer
         */
        public RemoveFavoriteComponentAction(PaletteViewer paletteViewer) {
            this.paletteViewer = paletteViewer;
            shell = paletteViewer.getControl().getShell();
            setText(Messages.getString("TalendPaletteContextMenuProvider.textRemove")); //$NON-NLS-1$
            setToolTipText(Messages.getString("TalendPaletteContextMenuProvider.tipRemove")); //$NON-NLS-1$
            setDescription(Messages.getString("TalendPaletteContextMenuProvider.descriptionRemove")); //$NON-NLS-1$
        }

        @Override
        public void run() {

            List list = paletteViewer.getSelectedEditParts();
            PaletteEntry element = (PaletteEntry) ((EditPart) list.get(0)).getModel();
            Project project = null;
            if (element instanceof TalendPaletteDrawer) {

                List eleList = ((TalendPaletteDrawer) element).getChildren();
                removeListNotes(eleList, project);
            } else if (element instanceof CombinedTemplateCreationEntry) {
                removeNotes((CombinedTemplateCreationEntry) element, project);

            }
            ComponentUtilities.updatePalette(true);
        }

    }

    class HiddenFloderAction extends Action {

        private Shell shell;

        private PaletteViewer paletteViewer;

        /**
         * DOC hcw SearchComponentAction constructor comment.
         * 
         * @param paletteViewer
         */
        public HiddenFloderAction(PaletteViewer paletteViewer) {
            this.paletteViewer = paletteViewer;
            shell = paletteViewer.getControl().getShell();
            setText(Messages.getString("TalendPaletteContextMenuProvider.textHidden")); //$NON-NLS-1$
            setToolTipText(Messages.getString("TalendPaletteContextMenuProvider.tipHidden")); //$NON-NLS-1$
            setDescription(Messages.getString("TalendPaletteContextMenuProvider.descriptionHidden")); //$NON-NLS-1$
        }

        @Override
        public void run() {
            ComponentUtilities.histate = 1;
            DesignerPlugin.getDefault().getPreferenceStore().setValue("HiddenState", 1); //$NON-NLS-1$
            ComponentUtilities.updatePalette(!ShowFavoriteAction.state);

        }
    }

    class DisplayFloderAction extends Action {

        private Shell shell;

        private PaletteViewer paletteViewer;

        /**
         * DOC hcw SearchComponentAction constructor comment.
         * 
         * @param paletteViewer
         */
        public DisplayFloderAction(PaletteViewer paletteViewer) {
            this.paletteViewer = paletteViewer;
            shell = paletteViewer.getControl().getShell();
            setText(Messages.getString("TalendPaletteContextMenuProvider.textDisplay")); //$NON-NLS-1$
            setToolTipText(Messages.getString("TalendPaletteContextMenuProvider.tipDisplay")); //$NON-NLS-1$
            setDescription(Messages.getString("TalendPaletteContextMenuProvider.descriptionDisplay")); //$NON-NLS-1$
        }

        @Override
        public void run() {
            ComponentUtilities.histate = 0;
            DesignerPlugin.getDefault().getPreferenceStore().setValue("HiddenState", 0); //$NON-NLS-1$
            ComponentUtilities.updatePalette(!ShowFavoriteAction.state);

        }
    }

    public void addNotes(CombinedTemplateCreationEntry element, Project project) {
        String label = element.getLabel();
        String[] fam = null;
        String family = ComponentsFactoryProvider.getPaletteEntryFamily(element.getParent()); //$NON-NLS-1$ //$NON-NLS-2$
        if ("".equals(family) || family == null) {//$NON-NLS-1$
            PaletteComponentFactory paCom = (PaletteComponentFactory) element
                    .getToolProperty(CreationTool.PROPERTY_CREATION_FACTORY);
            fam = paCom.getCombinedFamilyName().split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
        }

        RepositoryContext repositoryContext = (RepositoryContext) CorePlugin.getContext().getProperty(
                Context.REPOSITORY_CONTEXT_KEY);
        project = repositoryContext.getProject();

        Set<IComponent> components = ComponentsFactoryProvider.getInstance().getComponents();
        for (IComponent component : components) {

            if (fam != null) {
                for (int i = 0; i < fam.length; i++) {
                    if (!component.isVisible(fam[i])) {
                        continue;
                    }
                    String famName = null;
                    String familyName[] = null;
                    famName = fam[i];

                    familyName = component.getOriginalFamilyName().split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                    for (int j = 0; j < familyName.length; j++) {

                        if (component.getName().equals(label) && (familyName[j]).equals(famName)) {
                            String key = component.getName() + "#" + famName; //$NON-NLS-1$
                            DesignerPlugin.getDefault().getPreferenceStore().setValue(key, true);
                        }
                    }

                }
            } else {
                if (!component.isVisible(family)) {
                    continue;
                }
                String famName = null;
                String[] familyName = null;

                famName = family;
                familyName = component.getOriginalFamilyName().split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                for (int i = 0; i < familyName.length; i++) {
                    if (component.getName().equals(label) && (familyName[i]).equals(famName)) {
                        String key = component.getName() + "#" + famName; //$NON-NLS-1$
                        DesignerPlugin.getDefault().getPreferenceStore().setValue(key, true);
                    }
                }

            }

        }
    }

    public void addListNotes(List eleList, Project project) {
        for (int i = 0; i < eleList.size(); i++) {
            PaletteEntry elementLi = (PaletteEntry) eleList.get(i);
            if (elementLi instanceof TalendPaletteDrawer) {
                List list = ((TalendPaletteDrawer) elementLi).getChildren();
                addListNotes(list, project);
            } else if (elementLi instanceof CombinedTemplateCreationEntry) {
                addNotes((CombinedTemplateCreationEntry) elementLi, project);
            }

        }
    }

    public void removeNotes(CombinedTemplateCreationEntry element, Project project) {
        String label = element.getLabel();
        String[] fam = null;
        String family = ComponentsFactoryProvider.getPaletteEntryFamily(element.getParent()); //$NON-NLS-1$ //$NON-NLS-2$

        if ("".equals(family) || family == null) {//$NON-NLS-1$
            PaletteComponentFactory paCom = (PaletteComponentFactory) element
                    .getToolProperty(CreationTool.PROPERTY_CREATION_FACTORY);
            fam = paCom.getCombinedFamilyName().split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
        }
        RepositoryContext repositoryContext = (RepositoryContext) CorePlugin.getContext().getProperty(
                Context.REPOSITORY_CONTEXT_KEY);
        project = repositoryContext.getProject();

        Set<IComponent> components = ComponentsFactoryProvider.getInstance().getComponents();

        for (IComponent component : components) {

            if (fam != null) {
                for (int i = 0; i < fam.length; i++) {
                    if (!component.isVisible(fam[i])) {
                        continue;
                    }
                    String famName = null;
                    String[] familyName = null;
                    famName = fam[i];
                    familyName = component.getOriginalFamilyName().split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                    for (int j = 0; j < familyName.length; j++) {
                        if (component.getName().equals(label) && (familyName[j]).equals(famName)) {
                            String key = component.getName() + "#" + famName; //$NON-NLS-1$
                            DesignerPlugin.getDefault().getPreferenceStore().setValue(key, false);
                        }
                    }
                }
            } else {
                if (!component.isVisible(family)) {
                    continue;
                }
                String famName = null;
                String[] familyName = null;

                famName = family;
                familyName = component.getOriginalFamilyName().split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                for (int j = 0; j < familyName.length; j++) {
                    if (component.getName().equals(label) && (familyName[j]).equals(famName)) {
                        String key = component.getName() + "#" + famName; //$NON-NLS-1$
                        DesignerPlugin.getDefault().getPreferenceStore().setValue(key, false);
                    }
                }

            }

        }
    }

    public void removeListNotes(List eleList, Project project) {
        for (int i = 0; i < eleList.size(); i++) {
            PaletteEntry elementLi = (PaletteEntry) eleList.get(i);
            if (elementLi instanceof TalendPaletteDrawer) {
                List list = ((TalendPaletteDrawer) elementLi).getChildren();
                removeListNotes(list, project);
            } else if (elementLi instanceof CombinedTemplateCreationEntry) {
                removeNotes((CombinedTemplateCreationEntry) elementLi, project);
            }

        }
    }
}
