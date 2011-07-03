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
package org.talend.designer.core.model.metadata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.talend.core.PluginChecker;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFilePackage;
import org.talend.designer.core.model.utils.emf.talendfile.util.TalendFileResourceImpl;

/**
 * Class that will take either a metadata or an emf object for the metadata, and that can return after the emf object or
 * the metadata object.
 * 
 * $Id: MetadataEmfFactory.java 51271 2010-11-15 08:40:42Z nrousseau $
 * 
 */
public class MetadataEmfFactory {

    IMetadataTable metadataTable;

    MetadataType metadataType;

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public void setMetadataTable(final IMetadataTable metadataTable) {
        this.metadataTable = metadataTable;
        TalendFileFactory fileFact = TalendFileFactory.eINSTANCE;
        metadataType = fileFact.createMetadataType();
        IMetadataColumn metaCol;
        ColumnType colType;
        EList listColType;

        metadataType.setComment(metadataTable.getComment());
        metadataType.setName(metadataTable.getTableName());
        metadataType.setLabel(metadataTable.getLabel());
        metadataType.setConnector(metadataTable.getAttachedConnector());
        listColType = metadataType.getColumn();

        if (metadataTable.getListColumns() != null) {
            for (int i = 0; i < metadataTable.getListColumns().size(); i++) {
                metaCol = metadataTable.getListColumns().get(i);
                colType = fileFact.createColumnType();
                colType.setComment(metaCol.getComment());
                colType.setKey(metaCol.isKey());
                colType.setNullable(metaCol.isNullable());
                if (metaCol.getLength() == null) {
                    // colType.setLength(-1);
                    colType.unsetLength();
                } else {
                    colType.setLength(metaCol.getLength());
                }
                colType.setName(metaCol.getLabel());
                if (metaCol.getPrecision() == null) {
                    // colType.setPrecision(-1);
                    colType.unsetPrecision();
                } else {
                    colType.setPrecision(metaCol.getPrecision());
                }
                if (!metaCol.getLabel().equals(metaCol.getOriginalDbColumnName())) {
                    colType.setOriginalDbColumnName(metaCol.getOriginalDbColumnName());
                }
                colType.setType(metaCol.getTalendType());
                colType.setSourceType(metaCol.getType());
                colType.setPattern(metaCol.getPattern());
                colType.setDefaultValue(metaCol.getDefault());
                if (PluginChecker.isDatacertPluginLoaded()) {
                    colType.setRelatedEntity(metaCol.getRelatedEntity());
                    colType.setRelationshipType(metaCol.getRelationshipType());
                }
                listColType.add(colType);
            }
        }
    }

    public void setMetadataType(final MetadataType metadataType) {
        this.metadataType = metadataType;

        metadataTable = new MetadataTable();

        IMetadataColumn metaCol;
        ColumnType colType;
        EList listColType;
        List<IMetadataColumn> listMetadataColumn;

        metadataTable.setComment(metadataType.getComment());
        metadataTable.setTableName(metadataType.getName());
        metadataTable.setLabel(metadataType.getLabel());
        if (metadataType.getConnector() != null) {
            metadataTable.setAttachedConnector(metadataType.getConnector());
        } else {
            metadataTable.setAttachedConnector(EConnectionType.FLOW_MAIN.getName());
        }
        listColType = metadataType.getColumn();

        listMetadataColumn = new ArrayList<IMetadataColumn>();
        for (int i = 0; i < listColType.size(); i++) {
            colType = (ColumnType) listColType.get(i);
            metaCol = new MetadataColumn();
            metaCol.setComment(colType.getComment());
            metaCol.setKey(colType.isKey());
            metaCol.setNullable(colType.isNullable());
            if (colType.isSetLength()) {
                if (colType.getLength() >= 0) {
                    metaCol.setLength(new Integer(colType.getLength()));
                } else {
                    metaCol.setLength(null);
                }
            } else {
                metaCol.setLength(null);
            }
            metaCol.setLabel(colType.getName());
            if (colType.isSetPrecision()) {
                if (colType.getPrecision() >= 0) {
                    metaCol.setPrecision(new Integer(colType.getPrecision()));
                } else {
                    metaCol.setPrecision(null);
                }
            } else {
                metaCol.setPrecision(null);
            }
            metaCol.setTalendType(colType.getType());
            metaCol.setType(colType.getSourceType());
            metaCol.setPattern(colType.getPattern());
            metaCol.setDefault(colType.getDefaultValue());

            metaCol.setRelatedEntity(colType.getRelatedEntity());
            metaCol.setRelationshipType(colType.getRelationshipType());
            if (colType.getOriginalDbColumnName() == null) {
                metaCol.setOriginalDbColumnName(colType.getName());
            } else {
                metaCol.setOriginalDbColumnName(colType.getOriginalDbColumnName());
            }
            listMetadataColumn.add(metaCol);
        }
        metadataTable.setListColumns(listMetadataColumn);
    }

    public MetadataType getMetadataType() {
        return metadataType;
    }

    public IMetadataTable getMetadataTable() {
        return metadataTable;
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public OutputStream getOutputStream() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        URI uri = URI.createURI(""); //$NON-NLS-1$
        Resource res = new TalendFileResourceImpl(uri);
        res.getContents().add(metadataType);

        res.save(out, null);
        return out;
    }

    public void setInputStream(final InputStream istream) throws IOException {
        URI uri = URI.createURI(""); //$NON-NLS-1$

        TalendFilePackage.eINSTANCE.getNsURI();
        Resource res = new TalendFileResourceImpl(uri);

        res.load(istream, null);
        setMetadataType((MetadataType) res.getContents().get(0));
    }
}
