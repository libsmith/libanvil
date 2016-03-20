package org.libsmith.anvil.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeLiteral<T> implements Type, ParameterizedType {
    private final ParameterizedType delegate;

    protected TypeLiteral() {
        delegate = (ParameterizedType) GenericReflection.extractGenericType(TypeLiteral.class).from(this.getClass());
    }

    @SuppressWarnings("unchecked")
    public Class<T> toClass() {
        return (Class<T>) delegate.getRawType();
    }

    //<editor-fold desc="Delegates">
    @Override
    public Type[] getActualTypeArguments() {
        return delegate.getActualTypeArguments();
    }

    @Override
    public Type getRawType() {
        return delegate.getRawType();
    }

    @Override
    public Type getOwnerType() {
        return delegate.getOwnerType();
    }

    @Override
    public String getTypeName() {
        return delegate.getTypeName();
    }
    //</editor-fold>
}
