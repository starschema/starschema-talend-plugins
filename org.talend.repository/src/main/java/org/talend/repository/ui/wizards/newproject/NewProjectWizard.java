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
package org.talend.repository.ui.wizards.newproject;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.talend.commons.exception.MessageBoxExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.general.Project;
import org.talend.core.ui.images.ECoreImage;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.utils.ProjectHelper;

/**
 * Wizard for the creation of a new project. <br/>
 * 
 * $Id: NewProjectWizard.java 48656 2010-09-21 05:50:26Z cli $
 * 
 */
public class NewProjectWizard extends Wizard {

    /** Main page. */
    private NewProjectWizardPage mainPage;

    private Project project;

    private Project[] projects;

    /**
     * Constructs a new NewProjectWizard.
     * 
     * @param author Project author.
     * @param server
     * @param password
     * @param port2
     */
    public NewProjectWizard(Project[] projects) {
        super();
        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.PROJECT_WIZ));
        this.projects = projects;
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        mainPage = new NewProjectWizardPage();
        mainPage.setProjects(projects);
        addPage(mainPage);
        setWindowTitle(Messages.getString("NewProjectWizard.windowTitle")); //$NON-NLS-1$
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        Context ctx = CorePlugin.getContext();
        RepositoryContext repositoryContext = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
        try {
            Project projectInfor = ProjectHelper.createProject(mainPage.getName(), mainPage.getDescription(), mainPage
                    .getLanguage(), repositoryContext.getUser());
            project = repositoryFactory.createProject(projectInfor);
            return true;
        } catch (PersistenceException e) {
            MessageDialog.openError(getShell(), Messages.getString("NewProjectWizard.failureTitle"), Messages //$NON-NLS-1$
                    .getString("NewProjectWizard.failureText")); //$NON-NLS-1$ //$NON-NLS-2$
            MessageBoxExceptionHandler.process(e);
            return false;
        }
    }

    /**
     * Getter for project.
     * 
     * @return the project
     */
    public Project getProject() {
        return this.project;
    }

}
