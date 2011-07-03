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
package org.talend.core.model.repository;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.talend.core.runtime.i18n.Messages;

public class DynaEnum<E extends DynaEnum<E>> {

    private static Map<Class<? extends DynaEnum<?>>, Map<String, DynaEnum<?>>> elements = new LinkedHashMap<Class<? extends DynaEnum<?>>, Map<String, DynaEnum<?>>>();

    private String key;

    protected boolean isStaticNode;

    protected String type;

    public String getKey() {
        return this.key;
    }

    private final int ordinal;

    public final int ordinal() {
        return ordinal;
    }

    public String name() {
        return type;
    }

    protected DynaEnum(String key, String type, boolean isStaticNode, int ordinal) {
        this.key = key;
        this.isStaticNode = isStaticNode;
        this.ordinal = ordinal;
        Map<String, DynaEnum<?>> typeElements = elements.get(getClass());
        if (typeElements == null) {
            typeElements = new LinkedHashMap<String, DynaEnum<?>>();
            elements.put(getDynaEnumClass(), typeElements);
        }
        typeElements.put(type, this);
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends DynaEnum<?>> getDynaEnumClass() {
        return (Class<? extends DynaEnum<?>>) getClass();
    }

    @Override
    public String toString() {
        if (isStaticNode()) {
            return Messages.getString(key);
        }
        return key;
    }

    @Override
    public final boolean equals(Object other) {
        return this == other;
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public final int compareTo(E other) {
        DynaEnum<?> self = this;
        if (self.getClass() != other.getClass() && // optimization
                self.getDeclaringClass() != other.getDeclaringClass())
            throw new ClassCastException();
        return self.ordinal - other.ordinal;
    }

    @SuppressWarnings("unchecked")
    public final Class<E> getDeclaringClass() {
        Class clazz = getClass();
        Class zuper = clazz.getSuperclass();
        return (zuper == DynaEnum.class) ? clazz : zuper;
    }

    @SuppressWarnings("unchecked")
    public static <T extends DynaEnum<T>> T valueOf(Class<T> enumType, String name) {
        return (T) elements.get(enumType).get(name);
    }

    @SuppressWarnings("unused")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException("can't deserialize enum");
    }

    @SuppressWarnings("unused")
    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("can't deserialize enum");
    }

    @Override
    protected final void finalize() {
    }

    public static <E> DynaEnum<? extends DynaEnum<?>>[] values() {
        throw new IllegalStateException("Sub class of DynaEnum must implement method valus()");
    }

    @SuppressWarnings("unchecked")
    public static <E> E[] values(Class<E> enumType) {
        Collection<DynaEnum<?>> values = elements.get(enumType).values();
        int n = values.size();
        E[] typedValues = (E[]) Array.newInstance(enumType, n);
        int i = 0;
        for (DynaEnum<?> value : values) {
            Array.set(typedValues, i, value);
            i++;
        }

        return typedValues;
    }

    public boolean isStaticNode() {
        return this.isStaticNode;
    }

    public String getType() {
        return type;
    }
}
