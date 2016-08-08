package org.libsmith.anvil.reflection;

import org.libsmith.anvil.reflection.ReflectiveOperationRuntimeException.NoSuchMemberRuntimeException;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Optional;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 06.08.16 20:57
 */
public abstract class FieldAccessor<T, V, S extends FieldAccessor<T, V, S>>
        implements ReflectionCommons.AccessibleCommons<Field, S>,
                   ReflectionCommons.MemberCommons<Field, S> {

    protected final Field field;

    FieldAccessor(@Nonnull Field field) {
        this.field = field;
    }

    @Override
    public @Nonnull Field getReflectionSubject() {
        return field;
    }

    @SuppressWarnings("unchecked")
    <N> FieldAccessor<T, N, ?> type(@Nonnull Class<N> type) {
        if (type.isAssignableFrom(field.getType())) {
            throw new ClassCastException(field.getType() + " cannot cast to " + type);
        }
        return (FieldAccessor<T, N, ?>) this;
    }

    @SuppressWarnings("unchecked")
    <N> Optional<? extends FieldAccessor<T, N, ?>> typeOp(@Nonnull Class<N> type) {
        if (type.isAssignableFrom(field.getType())) {
            return Optional.empty();
        }
        return Optional.of((FieldAccessor<T, N, ?>) this);
    }

    public static class Regular<T, V> extends FieldAccessor<T, V, Regular<T, V>> {

        Regular(@Nonnull Field field) {
            super(field);
        }

        @SuppressWarnings("unchecked")
        public V getValueAt(T object) {
            try {
                return (V) field.get(object);
            }
            catch (IllegalAccessException ex) {
                throw new ReflectiveOperationRuntimeException(ex);
            }
        }

        public void setValueAt(T object, V value) {
            try {
                field.set(object, value);
            }
            catch (IllegalAccessException ex) {
                throw new ReflectiveOperationRuntimeException(ex);
            }
        }

        public WithObject<T, V> asStatic() {
            return new WithObject<>(field, null);
        }

        public Optional<WithObject<T, V>> asStaticOp() {
            return Modifier.STATIC.presentIn(field) ? Optional.of(new WithObject<>(field, null)) : Optional.empty();
        }

        public WithObject<T, V> withObject(T object) {
            return new WithObject<>(field, object);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <N> Optional<Regular<T, N>> typeOp(@Nonnull Class<N> type) {
            return (Optional<Regular<T, N>>) super.typeOp(type);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <N> Regular<T, N> type(@Nonnull Class<N> type) {
            return (Regular<T, N>) super.type(type);
        }
    }

    public static class WithObject<T, V> extends FieldAccessor<T, V, WithObject<T, V>> {

        protected final T object;

        WithObject(@Nonnull Field field, T object) {
            super(field);
            if (object == null && Modifier.STATIC.notPresentIn(field)) {
                throw new NoSuchMemberRuntimeException("Field must be static: " + field);
            }
            this.object = object;
        }

        @SuppressWarnings("unchecked")
        public V getValue() {
            try {
                return (V) field.get(object);
            }
            catch (IllegalAccessException ex) {
                throw new ReflectiveOperationRuntimeException(ex);
            }
        }

        public void setValue(V value) {
            try {
                field.set(object, value);
            }
            catch (IllegalAccessException ex) {
                throw new ReflectiveOperationRuntimeException(ex);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public <N> WithObject<T, N> type(@Nonnull Class<N> type) {
            return (WithObject<T, N>) super.type(type);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <N> Optional<WithObject<T, N>> typeOp(@Nonnull Class<N> type) {
            return (Optional<WithObject<T, N>>) super.typeOp(type);
        }

        @Override
        public String toString() {
            return "Field accessor '" + field + "'" +
                   (object == null ? " of static object" : " of object '" + object + "'");
        }
    }

    @Override
    public String toString() {
        return "Field accessor '" + field + "'";
    }
}
