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

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.part.EditorPart;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.editor.JobEditorInput;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryNode;

/**
 * Wizard for the creation of a new project. <br/>
 * 
 * $Id: NewProcessWizard.java 46332 2010-08-05 06:48:56Z cli $
 * 
 */
public class SaveAsProcessWizard extends Wizard {

    /** Main page. */
    private NewProcessWizardPage mainPage;

    /** Created project. */
    private ProcessItem processItem;

    private Property property;

    private IPath path;

    private IProxyRepositoryFactory repositoryFactory;

    private JobEditorInput jobEditorInput;

    private ProcessItem oldProcessItem;

    private Property oldProperty;

    private boolean isUpdate;

    public SaveAsProcessWizard(EditorPart editorPart) {

        this.jobEditorInput = (JobEditorInput) editorPart.getEditorInput();

        RepositoryNode repositoryNode = jobEditorInput.getRepositoryNode();
        // see: RepositoryEditorInput.setRepositoryNode(IRepositoryNode repositoryNode)
        if (repositoryNode == null) {
            repositoryNode = (RepositoryNode) CorePlugin.getDefault().getRepositoryService().getRepositoryNode(
                    jobEditorInput.getItem().getProperty().getId(), false);
        }

        IRepositoryService service = DesignerPlugin.getDefault().getRepositoryService();
        this.path = service.getRepositoryPath((RepositoryNode) repositoryNode);

        this.oldProcessItem = (ProcessItem) jobEditorInput.getItem();
        oldProperty = this.oldProcessItem.getProperty();

        this.property = PropertiesFactory.eINSTANCE.createProperty();

        assginVlaues(this.property, oldProperty);

        processItem = PropertiesFactory.eINSTANCE.createProcessItem();

        processItem.setProperty(property);

        repositoryFactory = service.getProxyRepositoryFactory();

        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.PROCESS_WIZ));
    }

    public void addPages() {
        mainPage = new NewProcessWizardPage(property, path);
        mainPage.initializeSaveAs(oldProperty.getLabel(), oldProperty.getVersion(), true);

        // overwrite it.
        mainPage.setTitle("Save As");
        mainPage.setDescription("Save as another new job.");

        addPage(mainPage);
        setWindowTitle("Save As");
    }

    public boolean performFinish() {

        boolean ok = false;
        try {

            IProcess2 loadedProcess = jobEditorInput.getLoadedProcess();
            ProcessType processType = loadedProcess.saveXmlFile();

            isUpdate = isUpdate();

            if (isUpdate) {
                oldProcessItem.setProcess(processType);

                assginVlaues(oldProperty, property);

                repositoryFactory.save(oldProcessItem);

                // assign value
                processItem = oldProcessItem;
            } else {
                processItem.setProcess(processType);

                property.setId(repositoryFactory.getNextId());
                // don't need to add depended routines.

                repositoryFactory.create(processItem, mainPage.getDestinationPath());
            }
            ok = true;

        } catch (Exception e) {
            MessageDialog.openError(getShell(), "Error", "Job could not be saved" + " : " + e.getMessage());
            ExceptionHandler.process(e);
        }

        return ok;
    }

    public ProcessItem getProcess() {
        return this.processItem;
    }

    public boolean isUpdateOperation() {
        return this.isUpdate;
    }

    // if name is different, it will create a new job, if name is the same, means to update the job(version or
    // description...)
    private boolean isUpdate() {
        if (oldProperty.getLabel().trim().equalsIgnoreCase(property.getLabel().trim())) {
            return true;
        } else {
            return false;
        }
    }

    // left = right
    private void assginVlaues(Property leftProperty, Property rightProperty) {
        // 6 fields, don't contains the "locker" and "path". and author , they are the same.
        leftProperty.setLabel(rightProperty.getLabel());
        leftProperty.setPurpose(rightProperty.getPurpose());
        leftProperty.setDescription(rightProperty.getDescription());
        // same author as old one.
        leftProperty.setAuthor(rightProperty.getAuthor());
        leftProperty.setVersion(rightProperty.getVersion());
        leftProperty.setStatusCode(rightProperty.getStatusCode());
    }
}
