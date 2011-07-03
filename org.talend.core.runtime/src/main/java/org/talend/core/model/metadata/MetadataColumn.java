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
package org.talend.core.model.metadata;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;

/**
 * Definition of a column in the Meta Data. <br/>
 * 
 * $Id: MetadataColumn.java 38013 2010-03-05 14:21:59Z mhirt $
 * 
 */
public class MetadataColumn implements IMetadataColumn, Cloneable {

    private static int nextId = 0;

    private String id;

    private String label = ""; //$NON-NLS-1$

    private boolean key = false;

    private String sourceType = ""; //$NON-NLS-1$

    private String talendType = ""; //$NON-NLS-1$

    private boolean nullable = false;

    private Integer length;

    private Integer precision;

    private String defaut = ""; //$NON-NLS-1$

    private String comment = ""; //$NON-NLS-1$

    private String pattern = ""; //$NON-NLS-1$

    private boolean custom = false;

    private boolean readOnly = false;

    private int customId = 0;

    private String originalDbColumnName;

    private String relatedEntity = "";

    private String relationshipType = "";

    private String expression = "";

    public MetadataColumn() {
        super();
        this.id = getNewId();
    }

    /**
     * copy all fields.
     * 
     * @param metadataColumn
     */
    public MetadataColumn(IMetadataColumn metadataColumn) {
        this();

        this.setLabel(metadataColumn.getLabel());
        this.setOriginalDbColumnName(metadataColumn.getOriginalDbColumnName());
        this.key = metadataColumn.isKey();
        this.pattern = metadataColumn.getPattern();
        try {
            this.setTalendType(metadataColumn.getTalendType());
        } catch (NoClassDefFoundError e) {
            // should never happend when product run
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }

        this.setType(metadataColumn.getType());
        // setDbms(metadataColumn.getDbms());

        this.nullable = metadataColumn.isNullable();
        this.length = metadataColumn.getLength();
        this.precision = metadataColumn.getPrecision();

        setDefault(metadataColumn.getDefault());
        setComment(metadataColumn.getComment());

        // Datacert: custom metadataColumn to set relatedEntity
        // and relationShipType info.
        setRelatedEntity(metadataColumn.getRelatedEntity());
        setRelationshipType(metadataColumn.getRelationshipType());
    }

    private static synchronized String getNewId() {
        return String.valueOf(nextId++);
    }

    @Override
    public String toString() {
        return getLabel();
    }

    /**
     * Getter for id.
     * 
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the id.
     * 
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataColumn#getLabel()
     */
    public String getLabel() {
        return this.label;
    }

    // /**
    // * Check the input String is empty or not.
    // *
    // * @param input
    // * @return
    // */
    // private boolean isNull(String input) {
    // return input == null;
    // }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataColumn#setLabel(java.lang.String)
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataColumn#isKey()
     */
    public boolean isKey() {
        return this.key;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataColumn#setKey(boolean)
     */
    public void setKey(boolean key) {
        this.key = key;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataColumn#getType()
     */
    public String getType() {
        return this.sourceType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataColumn#setType(java.lang.String)
     */
    public void setType(String sourceType) {
        this.sourceType = sourceType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.metadata.IMetadataColumn#getTalendType()
     */
    public String getTalendType() {
        // if ((talendType == null) || (talendType.compareTo("") == 0)) { //$NON-NLS-1$
        // this.talendType = MetadataTalendType.loadTalendType(this.type, this.dbms, false);
        // }
        return talendType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.metadata.IMetadataColumn#setTalendType(java.lang.String)
     */
    public void setTalendType(String talendType) {
        this.talendType = talendType;
    }

    // /*
    // * (non-Javadoc)
    // *
    // * @see org.talend.core.model.metadata.IMetadataColumn#getDbms()
    // */
    // public String getDbms() {
    // return this.dbms;
    // }

    // /*
    // * (non-Javadoc)
    // *
    // * @see org.talend.core.model.metadata.IMetadataColumn#setDbms(java.lang.String)
    // */
    // public void setDbms(String dbms) {
    // this.dbms = dbms;
    // }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataColumn#getLength()
     */
    public Integer getLength() {
        return this.length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataColumn#setLength(Integer)
     */
    public void setLength(Integer length) {
        this.length = length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataColumn#isNullable()
     */
    public boolean isNullable() {
        return this.nullable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataColumn#setNullable(boolean)
     */
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataColumn#getPrecision()
     */
    public Integer getPrecision() {
        return this.precision;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataColumn#setPrecision(Integer)
     */
    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataColumn#getDefault()
     */
    public String getDefault() {
        return this.defaut;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataColumn#setDefault(java.lang.String)
     */
    public void setDefault(String defaut) {
        this.defaut = defaut;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataColumn#getComment()
     */
    public String getComment() {
        return this.comment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.metadata.IMetadataColumn#setComment(java.lang.String)
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.metadata.IMetadataColumn#getPattern()
     */
    public String getPattern() {
        return this.pattern;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.metadata.IMetadataColumn#setPattern(java.lang.String)
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public IMetadataColumn clone() {
        return clone(false);
    }

    public IMetadataColumn clone(boolean withCustoms) {
        IMetadataColumn clonedMetacolumn = null;
        try {
            clonedMetacolumn = (IMetadataColumn) super.clone();
            if (!withCustoms) {
                clonedMetacolumn.setCustom(false);
                clonedMetacolumn.setReadOnly(false);
            }
        } catch (CloneNotSupportedException e) {
            // nothing
        }
        return clonedMetacolumn;
    }

    public boolean sameMetacolumnAs(IMetadataColumn other, int options) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof IMetadataColumn)) {
            return false;
        }
        if ((options & OPTIONS_IGNORE_COMMENT) == 0) {
            if (!sameStringValue(this.comment, other.getComment())) {
                return false;
            }
        }
        if ((options & OPTIONS_IGNORE_KEY) == 0) {
            if (this.key != other.isKey()) {
                return false;
            }
        }
        if ((options & OPTIONS_IGNORE_LABEL) == 0) {
            if (!sameStringValue(this.label, other.getLabel())) {
                return false;
            }
        }
        if ((options & OPTIONS_IGNORE_PATTERN) == 0) {
            if (!sameStringValue(this.pattern, other.getPattern())) {
                return false;
            }
        }
        if ((options & OPTIONS_IGNORE_LENGTH) == 0) {
            if (!sameIntegerValue(this.length, other.getLength())) {
                if (((options & OPTIONS_IGNORE_BIGGER_SIZE) == 0)) {
                    return false;

                }
                if (!largeValue(this.length, other.getLength())) {
                    return false;
                }
            }
        }
        if ((options & OPTIONS_IGNORE_NULLABLE) == 0) {
            if (this.nullable != other.isNullable()) {
                return false;
            }
        }
        if ((options & OPTIONS_IGNORE_PRECISION) == 0) {
            if (!sameIntegerValue(this.precision, other.getPrecision())) {
                if (((options & OPTIONS_IGNORE_BIGGER_SIZE) == 0)) {
                    return false;

                }
                if (!largeValue(this.precision, other.getPrecision())) {
                    return false;
                }

            }
        }
        if ((options & OPTIONS_IGNORE_DEFAULT) == 0) {
            if (!sameStringValue(this.defaut, other.getDefault())) {
                return false;
            }
        }
        if ((options & OPTIONS_IGNORE_TALENDTYPE) == 0) {
            if (!sameStringValue(this.talendType, other.getTalendType())) {
                return false;
            }
        }
        if ((options & OPTIONS_IGNORE_DBTYPE) == 0) {
            if (!sameStringValue(this.sourceType, other.getType())) {
                return false;
            }
        }
        if ((options & OPTIONS_IGNORE_DBCOLUMNNAME) == 0) {
            if (!sameStringValue(this.originalDbColumnName, other.getOriginalDbColumnName())) {
                return false;
            }
        }
        if ((options & OPTIONS_IGNORE_RELATEDENTITY) == 0) {
            if (!sameStringValue(this.relatedEntity, other.getRelatedEntity())) {
                return false;
            }
        }
        if ((options & OPTIONS_IGNORE_RELATIONSHIPTYPE) == 0) {
            if (!sameStringValue(this.relationshipType, other.getRelationshipType())) {
                return false;
            }
        }
        return true;
    }

    public boolean sameMetacolumnAs(IMetadataColumn other) {
        return sameMetacolumnAs(other, OPTIONS_NONE);
    }

    private boolean sameStringValue(String value1, String value2) {
        if (value1 == null) {
            if (value2 == null) {
                return true;
            } else {
                return value2.equals(""); //$NON-NLS-1$
            }
        } else {
            if (value1.equals("") && value2 == null) { //$NON-NLS-1$
                return true;
            } else {
                return value1.equals(value2);
            }
        }
    }

    private boolean sameIntegerValue(Integer value1, Integer value2) {
        if (value1 == null) {
            if (value2 == null) {
                return true;
            } else {
                return value2 <= 0;
            }
        } else {
            if (value1 <= 0 && value2 == null) {
                return true;
            } else {
                return (value1 <= 0 && value2 <= 0) || value1.equals(value2);
            }
        }
    }

    private boolean largeValue(Integer value1, Integer value2) {
        if (value1 == null) {
            if (value2 == null) {
                return false;
            } else {
                return value2 > 0;
            }
        } else {
            if (value2 == null) {
                return value1 < 0;
            } else {
                return value1 < value2;
            }
        }
    }

    /**
     * Getter for custom.
     * 
     * @return the custom
     */
    public boolean isCustom() {
        return custom;
    }

    /**
     * Sets the custom.
     * 
     * @param custom the custom to set
     */
    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    /**
     * Getter for readOnly.
     * 
     * @return the readOnly
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets the readOnly.
     * 
     * @param readOnly the readOnly to set
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public int getCustomId() {
        return customId;
    }

    public void setCustomId(int customId) {
        this.customId = customId;
    }

    public String getOriginalDbColumnName() {
        return originalDbColumnName;
    }

    public void setOriginalDbColumnName(String originalDbColumnName) {
        this.originalDbColumnName = originalDbColumnName;
    }

    /**
     * @return the relatedEntity
     */
    public String getRelatedEntity() {
        return relatedEntity;
    }

    /**
     * @param relatedEntity the relatedEntity to set
     */
    public void setRelatedEntity(String relatedEntity) {
        this.relatedEntity = relatedEntity;
    }

    /**
     * @return the relationshipType
     */
    public String getRelationshipType() {
        return relationshipType;
    }

    /**
     * @param relationshipType the relationshipType to set
     */
    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public String getExpression() {
        return this.expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

}
