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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.core.CorePlugin;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.ProxyRepositoryFactory;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: RepositoryWizard.java 49777 2010-10-12 06:41:27Z zli $
 * 
 */
public abstract class RepositoryWizard extends Wizard {

    protected static Logger log = Logger.getLogger(RepositoryWizard.class);

    protected static final String PID = RepositoryPlugin.PLUGIN_ID;

    private IWorkbench workbench;

    protected ISelection selection;

    protected IRepositoryViewObject repositoryObject = null;

    protected String[] existingNames;

    protected IPath pathToSave = null;

    protected boolean creation;

    private boolean repositoryObjectEditable = true;

    private boolean forceReadOnly;

    public RepositoryWizard(IWorkbench workbench, boolean creation) {
        this(workbench, creation, false);
    }

    public RepositoryWizard(IWorkbench workbench, boolean creation, boolean forceReadOnly) {
        super();
        this.workbench = workbench;
        this.creation = creation;
        this.forceReadOnly = forceReadOnly;
    }

    /**
     * Getter for workbench.
     * 
     * @return the workbench
     */
    public IWorkbench getWorkbench() {
        return this.workbench;
    }

    /**
     * Sets the workbench.
     * 
     * @param workbench the workbench to set
     */
    public void setWorkbench(IWorkbench workbench) {
        this.workbench = workbench;
    }

    /**
     * DOC ocarbone Comment method "performCancel". Unlock the IRepositoryObject before the close of the wizard.
     * 
     * @param IRepositoryObject
     */
    public boolean performCancel() {
        closeLockStrategy();
        return true;
    }

    /**
     * DOC mhelleboid Comment method "reload".
     * 
     * @throws PersistenceException
     */
    private void reload() throws PersistenceException {
        if (repositoryObject != null) {
            ItemState state = repositoryObject.getProperty().getItem().getState();
            IProxyRepositoryFactory repositoryFactory = CorePlugin.getDefault().getRepositoryService()
                    .getProxyRepositoryFactory();
            if (state != null && state.getPath() != null) {
                IRepositoryViewObject lastVersion = repositoryFactory
                        .getLastVersion(ProjectManager.getInstance().getCurrentProject(), repositoryObject.getProperty().getId(),
                                state.getPath(), repositoryObject.getType());
                lastVersion.setRepositoryNode(repositoryObject.getRepositoryNode());
                repositoryObject = lastVersion;
            }
        }
    }

    /**
     * Sets the repositoryObject.
     * 
     * @param repositoryObject the repositoryObject to set
     */
    public void setRepositoryObject(IRepositoryViewObject repositoryViewObject) {
        // RepositoryViewObject is dynamic, here we prefer have a RepositoryObject with a fixed property.
        this.repositoryObject = new RepositoryObject(repositoryViewObject.getProperty());
        calculateRepositoryObjectEditable(repositoryViewObject);
    }

    /**
     * DOC ocarbone Comment method "isRepositoryObjectEditable". Check the RepositoryObject (is locked or on recycle
     * bin) and return a boolean represent if the RepositoryObject is editable (true) or readOnly (false).
     * 
     * @return boolean
     */
    public boolean isRepositoryObjectEditable() {
        return repositoryObjectEditable && !forceReadOnly;
    }

    /**
     * DOC matthieu Comment method "calculateObjectEditable".
     * 
     * @return
     */
    private void calculateRepositoryObjectEditable(IRepositoryViewObject repositoryViewObject) {
        if (repositoryObject != null && !forceReadOnly) {
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            repositoryObjectEditable = factory.isEditableAndLockIfPossible(repositoryObject);

            final IRepositoryNode repositoryNode = repositoryViewObject.getRepositoryNode();
            RepositoryManager.getRepositoryView().expand(repositoryNode);
            RepositoryManager.getRepositoryView().getViewer().refresh(repositoryObject.getType());
        }
    }

    /**
     * DOC ocarbone Comment method "initLockStrategy". If needed, lock the repositoryObject.
     * 
     */
    public void initLockStrategy() {
        if (isRepositoryObjectEditable()) {
            // The lock strategy is useless when the repositoryObject isn't yet created
            if (repositoryObject != null) {
                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                try {
                    factory.lock(repositoryObject);
                } catch (PersistenceException e1) {
                    String detailError = e1.toString();
                    new ErrorDialogWidthDetailArea(getShell(), PID, Messages.getString("CommonWizard.persistenceException"), //$NON-NLS-1$
                            detailError);
                    log.error(Messages.getString("CommonWizard.persistenceException") + "\n" + detailError); //$NON-NLS-1$ //$NON-NLS-2$
                } catch (BusinessException e) {
                    // Nothing to do
                }
            }
        }
    }

    /**
     * DOC ocarbone Comment method "performCancel". Unlock the IRepositoryObject before the close of the wizard.
     * 
     * @param IRepositoryObject
     */
    public void closeLockStrategy() {
        // The lock strategy is useless when the repositoryObject isn't created
        if (repositoryObject != null) {
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            try {
                factory.unlock(repositoryObject);
            } catch (PersistenceException e) {
                String detailError = e.toString();
                new ErrorDialogWidthDetailArea(getShell(), PID, Messages.getString("CommonWizard.persistenceException"), //$NON-NLS-1$
                        detailError);
                log.error(Messages.getString("CommonWizard.persistenceException") + "\n" + detailError); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    /**
     * 
     * DOC wzhang Comment method "getConnectionItem".
     * 
     * @return
     */
    public ConnectionItem getConnectionItem() {
        return null;
    }

    public boolean isCreation() {
        return this.creation;
    }

}
