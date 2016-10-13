package org.libsmith.anvil.reflection;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.libsmith.anvil.reflection.ReflectionUtils.uncheckedClassCast;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 20.03.16 23:14
 */
public class ClassReflection<T> implements ReflectionSubjectAware<Class<T>>  {

    private final Class<T> type;

    private volatile Set<Class<? super T>> hierarchyCached;
    private volatile Set<Method> allMethodsCached;
    private volatile Set<Field> allFieldsCached;
    private volatile Set<Method> localMethodsCached;
    private volatile Set<Field> localFieldsCached;

    protected ClassReflection(@Nonnull Class<T> type) {
        this.type = type;
    }

    @Override
    public @Nonnull Class<T> getReflectionSubject() {
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

    public MethodInvoker.Resolver<T> method(String name) {
        return new MethodInvoker.Resolver<>(name, type::getMethod);
    }

    public @Nonnull Optional<Method> getMethodOp(@Nonnull String name, Class<?> ... parameters) {
        return method(name).paramsUncheckedOp(parameters).map(MethodInvoker::getReflectionSubject);
    }

    public @Nonnull Method getMethod(@Nonnull String name, Class<?> ... parameters) {
        return method(name).paramsUnchecked(parameters).getReflectionSubject();
    }

    public MethodInvoker.Resolver<T> localMethod(String name) {
        return new MethodInvoker.Resolver<>(name, type::getDeclaredMethod);
    }

    public @Nonnull Method getLocalMethod(@Nonnull String name, Class<?> ... parameters) {
        return localMethod(name).paramsUnchecked(parameters).getReflectionSubject();
    }

    public @Nonnull Optional<Method> getLocalMethodOp(@Nonnull String name, Class<?> ... parameters) {
        return localMethod(name).paramsUncheckedOp(parameters).map(MethodInvoker::getReflectionSubject);
    }

    public Optional<? extends FieldAccessor.Regular<T, ?>> fieldOp(String name) {
        return getFieldOp(name).map(FieldAccessor.Regular::new);
    }

    public FieldAccessor.Regular<T, ?> field(String name) {
        return new FieldAccessor.Regular<>(getField(name));
    }

    public Optional<? extends FieldAccessor.Regular<T, ?>> localFieldOp(String name) {
        return getLocalFieldOp(name).map(FieldAccessor.Regular::new);
    }

    public FieldAccessor.Regular<T, ?> localField(String name) {
        return new FieldAccessor.Regular<>(getLocalField(name));
    }

    public @Nonnull Field getField(@Nonnull String name) {
        return ((ReflectiveFunction<String, Field>) type::getField).apply(name);
    }

    public @Nonnull Optional<Field> getFieldOp(@Nonnull String name) {
        return ((ReflectiveFunction<String, Field>) type::getField).applyOp(name);
    }

    public @Nonnull Field getLocalField(@Nonnull String name) {
        return ((ReflectiveFunction<String, Field>) type::getDeclaredField).apply(name);
    }

    public @Nonnull Optional<Field> getLocalFieldOp(@Nonnull String name) {
        return ((ReflectiveFunction<String, Field>) type::getDeclaredField).applyOp(name);
    }

    public @Nonnull ConstructorInvoker.Resolver<T> constructor() {
        return new ConstructorInvoker.Resolver<>(type::getDeclaredConstructor);
    }

    public @Nonnull Constructor<T> getConstructor(Class<?> ... params) {
        return constructor().paramsUnchecked(params).getConstructor();
    }

    public @Nonnull Optional<Constructor<T>> getConstructorOp(Class<?> ... params) {
        return constructor().paramsUncheckedOp(params).map(ConstructorInvoker::getConstructor);
    }

    public @Nonnull Set<Method> getAllMethods() {
        Set<Method> allMethods = this.allMethodsCached;
        if (allMethods == null) {
            Set<Method> collection = new HashSet<>();
            getFullHierarchy().forEach(t -> Stream.of(t.getDeclaredMethods())
                                                  .filter(USER_METHODS_FILTER)
                                                  .collect(Collectors.toCollection(() -> collection)));
            this.allMethodsCached = allMethods = Collections.unmodifiableSet(collection);
        }
        return allMethods;
    }

    public @Nonnull Stream<Method> allMethods() {
        return getAllMethods().stream();
    }

    public @Nonnull Set<Method> getLocalMethods() {
        Set<Method> localMethods = this.localMethodsCached;
        if (localMethods == null) {
            localMethods = Stream.of(type.getDeclaredMethods())
                                 .filter(USER_METHODS_FILTER)
                                 .collect(Collectors.toSet());
            this.localMethodsCached = localMethods = Collections.unmodifiableSet(localMethods);
        }
        return localMethods;
    }

    public @Nonnull Stream<Method> localMethods() {
        return getLocalMethods().stream();
    }

    public @Nonnull Set<Field> getAllFields() {
        Set<Field> allFields = this.allFieldsCached;
        if (allFields == null) {
            Set<Field> collection = new HashSet<>();
            getFullHierarchy().forEach(t -> Stream.of(t.getDeclaredFields())
                                                  .filter(USER_FIELDS_FILTER)
                                                  .collect(Collectors.toCollection(() -> collection)));
            this.allFieldsCached = allFields = Collections.unmodifiableSet(collection);
        }
        return allFields;
    }

    public @Nonnull Stream<Field> allFields() {
        return getAllFields().stream();
    }

    public @Nonnull Set<Field> getLocalFields() {
        Set<Field> localFields = this.localFieldsCached;
        if (localFields == null) {
            localFields = Stream.of(type.getDeclaredFields())
                                .filter(USER_FIELDS_FILTER)
                                .collect(Collectors.toSet());
            this.localFieldsCached = localFields = Collections.unmodifiableSet(localFields);
        }
        return localFields;
    }

    public @Nonnull Stream<Field> localFields() {
        return getLocalFields().stream();
    }

    public @Nonnull Set<Class<? super T>> getFullHierarchy() {
        Set<Class<? super T>> hierarchy = this.hierarchyCached;
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
            this.hierarchyCached = Collections.unmodifiableSet(hierarchy);
        }
        return hierarchy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClassReflection<?> that = (ClassReflection<?>) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public @Nonnull String toString() {
        return getClass().getSimpleName() + " of " + type.getName();
    }

    private static final Predicate<Field> USER_FIELDS_FILTER = field -> !field.isSynthetic()
                                                                     &&  field.getDeclaringClass() != Object.class;

    private static final Predicate<Method> USER_METHODS_FILTER = method -> !method.isSynthetic()
                                                                        && !method.isBridge()
                                                                        &&  method.getDeclaringClass() != Object.class;

    public static final Predicate<Method> PUBLIC_METHODS = Modifier.PUBLIC::presentIn;

    public static final Predicate<Field> COPYABLE_FIELDS = field -> !Modifier.STATIC.presentIn(field)
                                                                 && !Modifier.FINAL.presentIn(field);
}
