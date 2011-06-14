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
package org.talend.repository.imports;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.talend.repository.model.RepositoryNode;

/**
 */
public class ImportItemWizard extends Wizard implements IImportWizard {

    private ImportItemWizardPage mainPage;

    private IWorkbench workbench;

    private IStructuredSelection selection;

    private RepositoryNode rNode;

    public ImportItemWizard(RepositoryNode rNode) {
        super();
        this.rNode = rNode;
    }

    public void addPages() {
        super.addPages();
        mainPage = new ImportItemWizardPage(rNode, getWindowTitle()); //$NON-NLS-1$
        addPage(mainPage);
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
    }

    public boolean performCancel() {
        return mainPage.performCancel();
    }

    public boolean performFinish() {
        return mainPage.performFinish();
    }

    public boolean isNeedToRefreshPalette() {
        return mainPage.isNeedToRefreshPalette();
    }

}
