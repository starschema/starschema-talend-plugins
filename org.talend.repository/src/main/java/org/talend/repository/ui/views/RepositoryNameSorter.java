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
package org.talend.repository.ui.views;

import java.util.Comparator;

import org.eclipse.jface.viewers.ViewerSorter;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;

/**
 * Name sorter for the repository view.<br/>
 * 
 * $Id: RepositoryNameSorter.java 46952 2010-08-18 08:41:09Z nrousseau $
 * 
 */
public class RepositoryNameSorter extends ViewerSorter {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
     */
    @Override
    public int category(Object element) {
        RepositoryNode node = (RepositoryNode) element;

        if (node.isBin()) {
            return 150;
        }

        if (node.getType() == ENodeType.STABLE_SYSTEM_FOLDER || node.getType() == ENodeType.SYSTEM_FOLDER) {
            ERepositoryObjectType contentType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
            if (contentType == null) {
                return 199;
            }

            switch (contentType) {
            case BUSINESS_PROCESS:
                return 0;
            case PROCESS:
                return 5;
            case JOBLET:
                return 6;
            case CONTEXT:
                return 10;
            case ROUTINES:
                return 15;
            case METADATA:
                return 20;
            case METADATA_CONNECTIONS:
                return 25;
            case METADATA_SAPCONNECTIONS:
                return 28;
            case SQLPATTERNS:
                return 19;
            case METADATA_FILE_DELIMITED:
                return 30;
            case METADATA_FILE_POSITIONAL:
                return 35;
            case METADATA_FILE_REGEXP:
                return 40;
            case METADATA_FILE_XML:
                return 45;
            case METADATA_FILE_EXCEL:
                return 46;
            case METADATA_FILE_LDIF:
                return 50;
            case METADATA_LDAP_SCHEMA:
                return 55;
            case METADATA_SALESFORCE_SCHEMA:
                return 56;
            case METADATA_GENERIC_SCHEMA:
                return 60;
            case METADATA_HEADER_FOOTER:
                return 61;
            case DOCUMENTATION:
                return 65;
            case JOBS:
                return 70;
            case JOBLETS:
                return 75;
            default:
                return 199;
            }
        } else if (node.getType() == ENodeType.SIMPLE_FOLDER) {
            return 120;
        } else if (node.getType() == ENodeType.REFERENCED_PROJECT) {
            return 200;
        } else {
            if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
                ERepositoryObjectType contentType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
                if (contentType != null) {
                    switch (contentType) {
                    case METADATA_CON_CDC:
                        return 120;
                    case METADATA_CON_TABLE:
                        return 125;
                    default:
                        return 130;
                    }
                }
            }
            return 130;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ViewerSorter#getComparator()
     */
    @Override
    protected Comparator<String> getComparator() {

        return new Comparator<String>() {

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */

            public int compare(String o1, String o2) {
                // Replace all "_" to " " due to avoid this situation: job name
                // "a_b_c" before "a_b" in the job list.
                return o1.replaceAll("_", " ").compareToIgnoreCase( //$NON-NLS-1$ //$NON-NLS-2$
                        o2.replaceAll("_", " ")); //$NON-NLS-1$ //$NON-NLS-2$
            }
        };
    }

}
