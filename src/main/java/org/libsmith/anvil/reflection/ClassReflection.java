package org.libsmith.anvil.reflection;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.libsmith.anvil.reflection.ReflectionUtils.uncheckedClassCast;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 20.03.16 23:14
 */
public class ClassReflection<T> {
    private final Class<T> type;
    private volatile Set<Class<? super T>> hierarchy;
    private volatile Set<Method> allMethods;
    private volatile Set<Field> allFields;
    private volatile Set<Method> localMethods;
    private volatile Set<Field> localFields;

    protected ClassReflection(@Nonnull Class<T> type) {
        this.type = type;
    }

    public Class<T> getReflectionSubject() {
        return type;
    }

    public static <T> ClassReflection<T> of(@Nonnull Class<T> type) {
        return new ClassReflection<>(type);
    }

    public static <T> ClassReflection<T> ofDeclaringClass(@Nonnull Method method) {
        return new ClassReflection<>(uncheckedClassCast(method.getDeclaringClass()));
    }

    public static <T> ClassReflection<T> ofDeclaringClass(@Nonnull Field field) {
        return new ClassReflection<>(uncheckedClassCast(field.getDeclaringClass()));
    }

    @SuppressWarnings("unchecked")
    public static <T> ClassReflection<T> ofInstance(@Nonnull T instance) {
        return new ClassReflection<>((Class) instance.getClass());
    }

    public Method getMethod(String name, Class<?> ... parameters) {
        try {
            return type.getMethod(name, parameters);
        }
        catch (NoSuchMethodException ex) {
            throw new ReflectiveOperationRuntimeException(ex);
        }
    }

    public Method getLocalMethod(String name, Class<?> ... parameters) {
        try {
            return type.getDeclaredMethod(name, parameters);
        }
        catch (NoSuchMethodException ex) {
            throw new ReflectiveOperationRuntimeException(ex);
        }
    }

    public Field getField(String name) {
        try {
            return type.getField(name);
        }
        catch (NoSuchFieldException ex) {
            throw new ReflectiveOperationRuntimeException(ex);
        }
    }

    public Field getLocalField(String name) {
        try {
            return type.getDeclaredField(name);
        }
        catch (NoSuchFieldException ex) {
            throw new ReflectiveOperationRuntimeException(ex);
        }
    }

    public Set<Method> getAllMethods() {
        Set<Method> allMethods = this.allMethods;
        if (allMethods == null) {
            Set<Method> collection = new HashSet<>();
            getFullHierarchy().stream()
                              .forEach(t -> Stream.of(t.getDeclaredMethods())
                                                  .filter(USER_METHODS_FILTER)
                                                  .collect(Collectors.toCollection(() -> collection)));
            this.allMethods = allMethods = Collections.unmodifiableSet(collection);
        }
        return allMethods;
    }

    public Stream<Method> allMethods() {
        return getAllMethods().stream();
    }

    public Set<Method> getLocalMethods() {
        Set<Method> localMethods = this.localMethods;
        if (localMethods == null) {
            localMethods = Stream.of(type.getDeclaredMethods())
                                 .filter(USER_METHODS_FILTER)
                                 .collect(Collectors.toSet());
            this.localMethods = localMethods = Collections.unmodifiableSet(localMethods);
        }
        return localMethods;
    }

    public Stream<Method> localMethods() {
        return getLocalMethods().stream();
    }

    public Set<Field> getAllFields() {
        Set<Field> allFields = this.allFields;
        if (allFields == null) {
            Set<Field> collection = new HashSet<>();
            getFullHierarchy().stream()
                              .forEach(t -> Stream.of(t.getDeclaredFields())
                                                  .filter(USER_FIELDS_FILTER)
                                                  .collect(Collectors.toCollection(() -> collection)));
            this.allFields = allFields = Collections.unmodifiableSet(collection);
        }
        return allFields;
    }

    public Stream<Field> allFields() {
        return getAllFields().stream();
    }

    public Set<Field> getLocalFields() {
        Set<Field> localFields = this.localFields;
        if (localFields == null) {
            localFields = Stream.of(type.getDeclaredFields())
                                .filter(USER_FIELDS_FILTER)
                                .collect(Collectors.toSet());
            this.localFields = localFields = Collections.unmodifiableSet(localFields);
        }
        return localFields;
    }

    public Stream<Field> localFields() {
        return getLocalFields().stream();
    }

    public Set<Class<? super T>> getFullHierarchy() {
        Set<Class<? super T>> hierarchy = this.hierarchy;
        if (hierarchy == null) {
            hierarchy = new HashSet<>();
            List<Class<?>> row = new ArrayList<>();
            row.add(type);
            row.addAll(Arrays.asList(type.getInterfaces()));
            while (!row.isEmpty()) {
                //noinspection unchecked
                ((Set) hierarchy).addAll(row);
                List<Class<?>> newRow = new ArrayList<>();
                for (Class<?> aClass : row) {
                    Class<?> superclass = aClass.getSuperclass();
                    if (superclass != null) {
                        newRow.add(superclass);
                    }
                    newRow.addAll(Arrays.asList(aClass.getInterfaces()));
                }
                row = newRow;
            }
            this.hierarchy = Collections.unmodifiableSet(hierarchy);
        }
        return hierarchy;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " of " + type.getName();
    }

    private static final Predicate<Field> USER_FIELDS_FILTER = field -> !field.isSynthetic()
                                                                     &&  field.getDeclaringClass() != Object.class;

    private static final Predicate<Method> USER_METHODS_FILTER = method -> !method.isSynthetic()
                                                                        && !method.isBridge()
                                                                        &&  method.getDeclaringClass() != Object.class;

    public static final Predicate<Method> PUBLIC_METHODS = method -> Modifier.isPublic(method.getModifiers());

    public static final Predicate<Field> COPYABLE_FIELDS = field -> !Modifier.isStatic(field.getModifiers())
                                                                 && !Modifier.isFinal(field.getModifiers());
}
