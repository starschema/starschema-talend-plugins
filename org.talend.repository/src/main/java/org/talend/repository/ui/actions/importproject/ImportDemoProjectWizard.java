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
package org.talend.repository.ui.actions.importproject;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * DOC Administrator class global comment. Detailled comment <br/>
 * 
 */
public class ImportDemoProjectWizard extends Wizard implements IImportWizard {

    private ImportDemoProjectPage demoProjectPage;

    private List<DemoProjectBean> demoProjectList;

    @Override
    public boolean performFinish() {
        return true;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {

        setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/exportzip_wiz.png"));//$NON-NLS-1$
        setNeedsProgressMonitor(true);
    }

    public void addPages() {
        super.addPages();
        demoProjectPage = new ImportDemoProjectPage(null);
        demoProjectPage.setImportDemoProjectList(this.demoProjectList);
        super.addPage(demoProjectPage);
    }

    public ImportDemoProjectWizard(List<DemoProjectBean> demoProjectList) {
        this.demoProjectList = demoProjectList;
    }

    public int getSelectedDemoProjectIndex() {
        return demoProjectPage.getSelectedDemoProjectIndex();
    }
}
