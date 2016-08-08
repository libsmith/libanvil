package org.libsmith.anvil.reflection;

import org.libsmith.anvil.reflection.ReflectiveOperationRuntimeException.NoSuchMemberRuntimeException;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.EnumSet;
import java.util.Optional;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 08.08.16 1:48
 */
public final class ReflectionCommons {

    private ReflectionCommons()
    { }

    public interface MemberCommons<T extends Member, S extends MemberCommons<T, S>>
            extends ReflectionSubjectAware<T>, Member {

        default S modifier(int modifiers) {
            return modifierOp(modifiers).orElseThrow(
                    () -> new NoSuchMemberRuntimeException("Found member does not contain one of this modifier(s): " +
                                                           modifiers + ", member is " + getReflectionSubject()));
        }

        @SuppressWarnings("unchecked")
        default Optional<S> modifierOp(int modifiers) {
            if ((getReflectionSubject().getModifiers() & modifiers) == modifiers) {
                return Optional.of((S) this);
            }
            return Optional.empty();
        }

        default S modifier(String string) {
            return modifier(Modifier.pack(Modifier.parse(string)));
        }

        default Optional<S> modifierOp(String string) {
            return modifierOp(Modifier.pack(Modifier.parse(string)));
        }

        default S modifier(Modifier... modifiers) {
            return modifier(Modifier.pack(modifiers));
        }

        default Optional<S> modifierOp(Modifier... modifiers) {
            return modifierOp(Modifier.pack(modifiers));
        }

        default EnumSet<Modifier> getModifierSet() {
            return Modifier.unpack(getReflectionSubject().getModifiers());
        }

        //<editor-fold desc="Delegates">
        @Override
        default String getName() {
            return getReflectionSubject().getName();
        }

        @Override
        default int getModifiers() {
            return getReflectionSubject().getModifiers();
        }

        @Override
        default Class<?> getDeclaringClass() {
            return getReflectionSubject().getDeclaringClass();
        }

        @Override
        default boolean isSynthetic() {
            return getReflectionSubject().isSynthetic();
        }
        //</editor-fold>
    }

    public interface AccessibleCommons<T extends AccessibleObject, S extends AccessibleCommons<T, S>>
            extends ReflectionSubjectAware<T> {

        @SuppressWarnings("unchecked")
        default S accessible(boolean value) {
            getReflectionSubject().setAccessible(value);
            return (S) this;
        }
    }

    static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
}
