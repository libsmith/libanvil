package org.libsmith.anvil.reflection;

import org.libsmith.anvil.reflection.GenericReflection.GenericTypeReflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeLiteral<T> implements Type, ParameterizedType {
    private static final GenericTypeReflection<TypeLiteral> TYPE_EXTRACTOR =
            GenericReflection.extractGenericParameterOf(TypeLiteral.class).asType();

    private final ParameterizedType delegate;

    protected TypeLiteral() {
        delegate = (ParameterizedType) TYPE_EXTRACTOR.from(this.getClass());
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
