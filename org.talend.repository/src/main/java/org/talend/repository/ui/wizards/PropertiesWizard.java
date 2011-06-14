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
package org.talend.repository.ui.wizards;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.MessageBoxExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.image.EImage;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.i18n.Messages;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryObject;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.expressionbuilder.ExpressionPersistance;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.ProxyRepositoryFactory;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 * 
 */
public class PropertiesWizard extends Wizard {

    private PropertiesWizardPage mainPage;

    private IRepositoryObject object;

    private final IPath path;

    private boolean alreadyEditedByUser = false;

    private final String originaleObjectLabel;

    private String originalVersion;

    private String originalPurpose;

    private String originalDescription;

    private String originalStatus;

    private String lastVersionFound;

    public PropertiesWizard(IRepositoryViewObject repositoryViewObject, IPath path, boolean useLastVersion) {
        super();

        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(EImage.PROPERTIES_WIZ));
        // properties wizard editable status not working for ref items ,because econtainer is null;
        if (repositoryViewObject != null && repositoryViewObject.getProperty() != null) {
            this.object = new RepositoryObject(repositoryViewObject.getProperty());
            Property property = object.getProperty();
            Item item = property.getItem();
            if (property.eResource() == null || item != null && item.eContainer() == null) {
                IProxyRepositoryFactory proxyRepositoryFactory = CorePlugin.getDefault().getRepositoryService()
                        .getProxyRepositoryFactory();
                try {
                    ItemState state = item.getState();
                    if (useLastVersion) {
                        if (state != null && state.getPath() != null) {
                            this.object = (IRepositoryObject) proxyRepositoryFactory.getLastVersion(new Project(ProjectManager
                                    .getInstance().getProject(item)), property.getId(), state.getPath(), object.getType());
                            lastVersionFound = this.object.getVersion();
                        } else {
                            this.object = (IRepositoryObject) proxyRepositoryFactory.getLastVersion(new Project(ProjectManager
                                    .getInstance().getProject(item)), property.getId());
                            lastVersionFound = this.object.getVersion();
                        }
                    } else {
                        if (state != null && state.getPath() != null) {
                            this.lastVersionFound = proxyRepositoryFactory.getLastVersion(
                                    new Project(ProjectManager.getInstance().getProject(item)), property.getId(),
                                    state.getPath(), object.getType()).getVersion();
                        } else {
                            this.lastVersionFound = proxyRepositoryFactory.getLastVersion(
                                    new Project(ProjectManager.getInstance().getProject(item)), property.getId()).getVersion();
                        }
                        this.object.setProperty(proxyRepositoryFactory.getUptodateProperty(property));
                    }
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        if (this.object == null) {
            this.object = new RepositoryObject(repositoryViewObject.getProperty());
        }
        this.originaleObjectLabel = this.object.getLabel();
        this.originalVersion = this.object.getVersion();
        this.originalDescription = this.object.getDescription();
        this.originalPurpose = this.object.getPurpose();
        this.originalStatus = this.object.getStatusCode();
        this.path = path;
        lockObject();
    }

    private void lockObject() {
        IProxyRepositoryFactory repositoryFactory = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
        try {
            if (repositoryFactory.getStatus(object).equals(ERepositoryStatus.LOCK_BY_USER)
                    && RepositoryManager.isOpenedItemInEditor(object)) {
                alreadyEditedByUser = true;
            } else {
                repositoryFactory.lock(object);
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        } catch (BusinessException e) {
            // Nothing to do
        }
    }

    private void unlockObject() {
        if (!alreadyEditedByUser) {
            IProxyRepositoryFactory repositoryFactory = CorePlugin.getDefault().getRepositoryService()
                    .getProxyRepositoryFactory();
            try {
                repositoryFactory.unlock(object);

                // if we have updated the version, the object will change, so we still need to unlock the original
                // version of the object.
                if (!object.getProperty().getVersion().equals(originalVersion)) {
                    List<IRepositoryViewObject> list = repositoryFactory.getAllVersion(object.getProperty().getId());
                    for (IRepositoryViewObject obj : list) {
                        if (obj.getProperty().getVersion().equals(originalVersion)) {
                            repositoryFactory.unlock(obj);
                            break;
                        }
                    }
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    private boolean isReadOnly() {
        IProxyRepositoryFactory repositoryFactory = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
        return !repositoryFactory.getStatus(object).isEditable() || alreadyEditedByUser;
    }

    @Override
    public void addPages() {
        mainPage = new PropertiesWizardPage("WizardPage", object.getProperty(), path, isReadOnly(), false, lastVersionFound) { //$NON-NLS-1$

            @Override
            public void createControl(Composite parent) {
                Composite container = new Composite(parent, SWT.NONE);
                GridLayout layout = new GridLayout(2, false);
                container.setLayout(layout);

                if (alreadyEditedByUser) {
                    Label label = new Label(container, SWT.NONE);
                    label.setForeground(ColorConstants.red);
                    label.setText(Messages.getString("PropertiesWizard.alreadyLockedByUser")); //$NON-NLS-1$
                    GridData gridData = new GridData();
                    gridData.horizontalSpan = 2;
                    label.setLayoutData(gridData);
                }

                super.createControl(container);

                setControl(container);
                updateContent();
                addListeners();
                setPageComplete(false);
            }

            @Override
            public ERepositoryObjectType getRepositoryObjectType() {
                return object.getType();
            }
        };
        addPage(mainPage);
        setWindowTitle(Messages.getString("PropertiesWizard.EditPropertiesPageTitle")); //$NON-NLS-1$
    }

    @Override
    public boolean performFinish() {
        if (alreadyEditedByUser) {
            return false;
        }
        try {
            ProxyRepositoryFactory proxyRepositoryFactory = ProxyRepositoryFactory.getInstance();
            proxyRepositoryFactory.save(object.getProperty(), this.originaleObjectLabel, this.originalVersion);
            ExpressionPersistance.getInstance().jobNameChanged(originaleObjectLabel, object.getLabel());
            proxyRepositoryFactory.saveProject(ProjectManager.getInstance().getCurrentProject());
            return true;
        } catch (PersistenceException e) {
            MessageBoxExceptionHandler.process(e);
            return false;
        }
    }

    /**
     * Use to replace in all tRunJob, the old job name by the new one.
     */
    // private void manageRunJobRenaming(String newName, String oldName) {
    // System.out.println("Rename " + oldName + "->" + newName); //$NON-NLS-1$ //$NON-NLS-2$
    //
    // IComponentFilter filter1 = new PropertyComponentFilter("tRunJob", "PROCESS_TYPE_PROCESS", oldName); //$NON-NLS-1$
    // //$NON-NLS-2$
    //
    // IComponentConversion updateCompProperty = new UpdatePropertyComponentConversion("PROCESS_TYPE_PROCESS", newName);
    // //$NON-NLS-1$
    // IComponentConversion updateRequiredProperty = new UpdateRequiredProperty(oldName, newName);
    //
    // try {
    // ModifyComponentsAction.searchAndModify(filter1, Arrays.<IComponentConversion> asList(updateCompProperty,
    // updateRequiredProperty));
    // } catch (Exception e) {
    // ExceptionHandler.process(e, Level.ERROR);
    // }
    // }
    /**
     * Update the "required" property.
     * 
     * <required> <job context="Default" name="newJobName"/> </required>
     */
    // private class UpdateRequiredProperty implements IComponentConversion {
    //
    // private final String oldJobName;
    //
    // private final String newJobName;
    //
    // public UpdateRequiredProperty(String oldJobName, String newJobName) {
    // super();
    // this.oldJobName = oldJobName;
    // this.newJobName = newJobName;
    // }
    //
    // public void transform(NodeType node) {
    // ProcessType item = (ProcessType) node.eContainer();
    // renameJobInRequiredProperty(item, newJobName);
    // }
    //
    // private void renameJobInRequiredProperty(ProcessType item, String newJobName) {
    // RequiredType required = item.getRequired();
    // for (Object o : required.getJob()) {
    // JobType job = (JobType) o;
    // if (job.getName().equals(oldJobName)) {
    // job.setName(newJobName);
    // }
    // }
    // }
    // }
    @Override
    public boolean performCancel() {
        if (!alreadyEditedByUser) {
            object.getProperty().setVersion(this.originalVersion);
            object.getProperty().setLabel(this.originaleObjectLabel);
            object.getProperty().setDescription(this.originalDescription);
            object.getProperty().setPurpose(this.originalPurpose);
            object.getProperty().setStatusCode(this.originalStatus);
        }
        return true;
    }

    @Override
    public void dispose() {
        unlockObject();
        super.dispose();
    }
}
