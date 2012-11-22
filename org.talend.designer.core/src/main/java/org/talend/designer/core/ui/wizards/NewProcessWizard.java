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
package org.talend.designer.core.ui.wizards;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * Wizard for the creation of a new project. <br/>
 * 
 * $Id: NewProcessWizard.java 82047 2012-04-19 08:15:10Z hcyi $
 * 
 */
public class NewProcessWizard extends Wizard {

    /** Main page. */
    private NewProcessWizardPage mainPage;

    /** Created project. */
    private ProcessItem processItem;

    private Property property;

    private IPath path;

    private IProxyRepositoryFactory repositoryFactory;

    /**
     * Constructs a new NewProjectWizard.
     * 
     * @param author Project author.
     * @param server
     * @param password
     */
    public NewProcessWizard(IPath path) {
        super();
        this.path = path;

        this.property = PropertiesFactory.eINSTANCE.createProperty();
        this.property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                .getUser());
        this.property.setVersion(VersionUtils.DEFAULT_VERSION);
        this.property.setStatusCode(""); //$NON-NLS-1$

        processItem = PropertiesFactory.eINSTANCE.createProcessItem();

        processItem.setProperty(property);

        repositoryFactory = DesignerPlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();

        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.PROCESS_WIZ));
    }

    /**
     * Constructs a new NewProjectWizard.
     * 
     * @param author Project author.
     * @param server
     * @param password
     * @param label
     */
    public NewProcessWizard(IPath path, String label) {
        super();
        this.path = path;

        this.property = PropertiesFactory.eINSTANCE.createProperty();
        this.property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                .getUser());
        if (label != null) {
            property.setLabel(label);
        }
        this.property.setVersion(VersionUtils.DEFAULT_VERSION);
        this.property.setStatusCode(""); //$NON-NLS-1$

        processItem = PropertiesFactory.eINSTANCE.createProcessItem();

        processItem.setProperty(property);

        repositoryFactory = DesignerPlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();

        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.PROCESS_WIZ));
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        mainPage = new NewProcessWizardPage(property, path);
        // TDI-20399
        if (property.getLabel() != null && !"".equals(property.getLabel())) {
            mainPage.setNameModifiedByUser(true);
        }
        addPage(mainPage);
        setWindowTitle(Messages.getString("NewProcessWizard.windowTitle")); //$NON-NLS-1$
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean performFinish() {
        try {

            property.setId(repositoryFactory.getNextId());
            // changed by hqzhang for TDI-19527, label=displayName
            property.setLabel(property.getDisplayName());

            ProcessType process = TalendFileFactory.eINSTANCE.createProcessType();
            ParametersType parameterType = TalendFileFactory.eINSTANCE.createParametersType();
            // add depended routines.
            List<RoutinesParameterType> dependenciesInPreference = RoutinesUtil.createDependenciesInPreference();
            parameterType.getRoutinesParameter().addAll(dependenciesInPreference);
            process.setParameters(parameterType);
            processItem.setProcess(process);
            RepositoryWorkUnit<Object> workUnit = new RepositoryWorkUnit<Object>(this.getWindowTitle(), this) {

                @Override
                protected void run() throws LoginException, PersistenceException {
                    repositoryFactory.create(processItem, mainPage.getDestinationPath());
                    RelationshipItemBuilder.getInstance().addOrUpdateItem(processItem);
                }
            };
            workUnit.setAvoidUnloadResources(true);
            repositoryFactory.executeRepositoryWorkUnit(workUnit);
        } catch (PersistenceException e) {
            MessageDialog.openError(getShell(), Messages.getString("NewProcessWizard.failureTitle"), Messages //$NON-NLS-1$
                    .getString("NewProcessWizard.failureText") + " : " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            ExceptionHandler.process(e);
        }
        return processItem != null;
    }

    /**
     * Getter for project.
     * 
     * @return the project
     */
    public ProcessItem getProcess() {
        return this.processItem;
    }
}
