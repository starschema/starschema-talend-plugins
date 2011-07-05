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
package org.talend.repository.ui.wizards.ConfigExternalLib;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.RoutineItem;
import org.talend.repository.model.RepositoryNode;

/**
 * Page of the Job Scripts Export Wizard. <br/>
 * 
 * @referto WizardArchiveFileResourceExportPage1 $Id: JobScriptsExportWizardPage.java 1 2006-12-13 下午03:09:07 bqian
 * 
 */
public abstract class ConfigExternalLibPage extends WizardPage {

    private IStructuredSelection selection;

    /**
     * Getter for selection.
     * 
     * @return the selection
     */
    public IStructuredSelection getSelection() {
        return this.selection;
    }

    /**
     * An abstract class for import external Lib.
     * 
     * @param pageName
     * @param title
     * @param titleImage
     */
    public ConfigExternalLibPage(String name, IStructuredSelection selection) {
        super(name);
        this.selection = selection;
    }

    /**
     * Subclasses should implement this for its own business.
     * 
     * @return
     */
    public boolean finish() {
        return true;
    }

    public void cancel() {

    }

    public RepositoryNode getSelectedRepositoryNode() {
        return (RepositoryNode) this.getSelection().getFirstElement();
    }

    public RoutineItem getSelectedRoutine() {
        RepositoryNode node = getSelectedRepositoryNode();
        Item item = node.getObject().getProperty().getItem();
        return (RoutineItem) item;
    }

}
