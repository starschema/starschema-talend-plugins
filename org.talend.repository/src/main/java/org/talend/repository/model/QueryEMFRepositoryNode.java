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
package org.talend.repository.model;

import org.talend.commons.ui.image.IImage;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.ui.images.ECoreImage;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 * 
 */
public class QueryEMFRepositoryNode extends RepositoryNode {

    private Query query;

    /**
     * DOC smallet EMFObjectRepositoryNode constructor comment.
     * 
     * @param object
     * @param parent
     * @param type
     */
    public QueryEMFRepositoryNode(Query query, RepositoryNode parent) {
        super(null, parent, ENodeType.REPOSITORY_ELEMENT);
        this.query = query;
    }

    public IImage getIcon() {
        return ECoreImage.METADATA_QUERY_ICON;
    }

    @Override
    public String getLabel() {
        return query.getLabel();
    }

    @Override
    public ENodeType getType() {
        return type;
    }

    public ERepositoryObjectType getObjectType() {
        return ERepositoryObjectType.METADATA_CON_QUERY;
    }

    /**
     * Getter for query.
     * 
     * @return the query
     */
    public Query getQuery() {
        return this.query;
    }
}
