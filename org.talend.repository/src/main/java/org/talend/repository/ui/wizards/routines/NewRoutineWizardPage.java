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
package org.talend.repository.ui.wizards.routines;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.ui.wizards.PropertiesWizardPage;

/**
 * Page for new project details. <br/>
 * 
 * $Id: NewProcessWizardPage.java 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 * 
 */
public class NewRoutineWizardPage extends PropertiesWizardPage {

    private static final String DESC = Messages.getString("NewRoutineWizardPage.description"); //$NON-NLS-1$

    /**
     * Constructs a new NewProjectWizardPage.
     * 
     */
    public NewRoutineWizardPage(Property property, IPath destinationPath) {
        super("WizardPage", property, destinationPath); //$NON-NLS-1$

        setTitle(Messages.getString("NewRoutineWizardPage.title")); //$NON-NLS-1$
        setDescription(DESC);
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);

        super.createControl(container);

        setControl(container);
        updateContent();
        addListeners();
        setPageComplete(false);
    }

    public ERepositoryObjectType getRepositoryObjectType() {
        return ERepositoryObjectType.ROUTINES;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.wizards.PropertiesWizardPage#evaluateTextField()
     */
    protected void evaluateTextField() {
        super.evaluateTextField();
        if (nameStatus.getSeverity() == IStatus.OK) {
            evaluateNameInJob();
        }
    }

    /**
     * ftang Comment method "evaluateNameInJob".
     */
    private void evaluateNameInJob() {
        String jobName = nameText.getText().trim();
        boolean isValid = isNameValidInJob(jobName);

        if (!isValid) {
            nameStatus = createStatus(IStatus.ERROR, Messages.getString("PropertiesWizardPage.ItemExistsInJobError")); //$NON-NLS-1$
            updatePageStatus();
        }
    }

    /**
     * ftang Comment method "isNameExistingInJob".
     * 
     * @param jobName
     */
    private boolean isNameValidInJob(String jobName) {
        Property property = PropertiesFactory.eINSTANCE.createProperty();

        IRepositoryService repositoryService = (IRepositoryService) GlobalServiceRegister.getDefault().getService(
                IRepositoryService.class);
        IProxyRepositoryFactory repositoryFactory = repositoryService.getProxyRepositoryFactory();
        property.setId(repositoryFactory.getNextId());
        ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        processItem.setProperty(property);
        boolean isValid = false;
        try {
            isValid = repositoryFactory.isNameAvailable(property.getItem(), jobName);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return false;
        }
        return isValid;
    }
}
