package org.talend.repository.sapwizard.ui.wizard;

import org.eclipse.core.runtime.IPath;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.ui.wizards.metadata.connection.Step0WizardPage;


/**
 * @author Ammu
 *
 */
public class SapStep0WizardPage extends Step0WizardPage {

    /**
     * @param property
     * @param destinationPath
     * @param type
     * @param readOnly
     * @param editPath
     */
    public SapStep0WizardPage(Property property, IPath destinationPath, ERepositoryObjectType type, boolean readOnly,
            boolean editPath) {
        super(property, destinationPath, type, readOnly, editPath);
    }
}
