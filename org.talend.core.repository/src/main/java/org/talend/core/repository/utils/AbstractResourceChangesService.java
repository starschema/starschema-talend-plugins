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
package org.talend.core.repository.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.resource.Resource;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.IService;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.TDQItem;

import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * DOC mzhao Abstract unload resource service which can be extended by client.
 */
public class AbstractResourceChangesService implements IService {

    public void handleUnload(Resource toBeUnloadedResource) {
    }

    // Add new elements to resource, remove elements from resource, delete resource
    public boolean handleResourceChange(ModelElement modelElement) {
        return true;
    }

    public void handleLogicalDelete(Property prop) {
    }

    public void handlePhysicalDelete(Property prop) {
    }

    public void handleRestore(Property prop) {
    }

    public Resource create(IProject project, Item item, int classID, IPath path) {
        return null;
    }

    public boolean isAnalysisOrReportItem(Item item) {
        return false;
    };

    // ADD qiongli 2011-5-10.should remove dependecy after showing the question of physical delete and confirm.
    public void removeAllDependecies(Item item) {

    }

    /**
     * update the connection item's dependencies when the version changed.
     * 
     * @param connItem
     * @param oldVersion
     * @param newVersion
     */
    public void updateDependeciesWhenVersionChange(ConnectionItem connItem, String oldVersion, String newVersion) {
    }

    /**
     * 
     * DOC gdbu Comment method "saveResourceByEMFShared".
     * 
     * @param item
     * @throws PersistenceException
     */
    public void saveResourceByEMFShared(Resource toSave) {

    }

    /**
     * Support to save the source file which is not an EMF model DOC yyin Comment method "saveSourceFile". added
     * 20120614 TDQ-5468
     * 
     * @param item
     * @return
     */
    public boolean saveSourceFile(TDQItem item) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * TDQ-5468 yyin 20120625 Handle the non-EMF file move action, such as sql source file & JRXML file etc.
     */
    public void postMove(Item dqItem, String targetPath) {

    }
}
