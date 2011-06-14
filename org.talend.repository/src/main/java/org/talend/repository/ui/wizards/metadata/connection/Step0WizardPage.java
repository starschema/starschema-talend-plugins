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
package org.talend.repository.ui.wizards.metadata.connection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.wizards.PropertiesWizardPage;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: Step0WizardPage.java 38013 2010-03-05 14:21:59Z mhirt $
 * 
 */
public class Step0WizardPage extends PropertiesWizardPage {

    private ERepositoryObjectType type;

    public Step0WizardPage(Property property, IPath destinationPath, ERepositoryObjectType type, boolean readOnly,
            boolean editPath) {
        super("WizardPage", property, destinationPath, readOnly, editPath); //$NON-NLS-1$
        this.type = type;

        setTitle(Messages.getString("Step0WizardPage.title")); //$NON-NLS-1$
        setDescription(Messages.getString("Step0WizardPage.description")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.wizards.PropertiesWizardPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);

        super.createControl(container);

        setControl(container);
        updateContent();
        addListeners();
        // setPageComplete(false);

        updatePageComplete();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.wizards.PropertiesWizardPage#getRepositoryObjectType()
     */
    @Override
    public ERepositoryObjectType getRepositoryObjectType() {
        return type;
    }

    /*
     * @see WizardPage#becomesVisible
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            this.nameText.setFocus();
        }
    }
}
