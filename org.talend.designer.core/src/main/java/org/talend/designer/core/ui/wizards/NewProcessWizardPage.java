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
package org.talend.designer.core.ui.wizards;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.ui.wizards.PropertiesWizardPage;

/**
 * Page for new project details. <br/>
 * 
 * $Id: NewProcessWizardPage.java 54939 2011-02-11 01:34:57Z mhirt $
 * 
 */
public class NewProcessWizardPage extends PropertiesWizardPage {

    private static final String DESC = Messages.getString("NewProcessWizard.description"); //$NON-NLS-1$

    /**
     * Constructs a new NewProjectWizardPage.
     * 
     */
    public NewProcessWizardPage(Property property, IPath destinationPath) {
        super("WizardPage", property, destinationPath); //$NON-NLS-1$

        setTitle(Messages.getString("NewProcessWizard.title")); //$NON-NLS-1$
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
        return ERepositoryObjectType.PROCESS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.wizards.PropertiesWizardPage#evaluateTextField()
     */
    protected void evaluateTextField() {
        super.evaluateTextField();
        if (nameStatus.getSeverity() == IStatus.OK) {
            evaluateNameInRoutine();
        }
    }

    /**
     * ftang Comment method "evaluateNameInRoutine".
     */
    private void evaluateNameInRoutine() {
        String jobName = nameText.getText().trim();
        boolean isValid = isNameValidInRountine(jobName);

        if (!isValid) {
            nameStatus = createStatus(IStatus.ERROR, Messages.getString("PropertiesWizardPage.ItemExistsInRoutineError")); //$NON-NLS-1$
            updatePageStatus();
        }
    }

    /**
     * ftang Comment method "isNameExistingInRountine".
     * 
     * @param jobName
     */
    private boolean isNameValidInRountine(String jobName) {
        Property property = PropertiesFactory.eINSTANCE.createProperty();

        IProxyRepositoryFactory repositoryFactory = DesignerPlugin.getDefault().getRepositoryService()
                .getProxyRepositoryFactory();
        property.setId(repositoryFactory.getNextId());
        RoutineItem routineItem = PropertiesFactory.eINSTANCE.createRoutineItem();
        routineItem.setProperty(property);
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
