package org.libsmith.anvil.reflection;

import org.libsmith.anvil.reflection.ReflectiveOperationRuntimeException.NoSuchMemberRuntimeException;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
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

    public Class<T> getReflectionSubjectClass() {
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

    public Method getMethodExact(String declaration, Class<?> ... parameters) {
        return getMember(declaration, (name) -> type.getMethod(name, parameters));
    }

    public Optional<Method> getMethod(String declaration, Class<?> ... parameters) {
        try {
            return Optional.of(getMember(declaration, name -> type.getMethod(name, parameters)));
        }
        catch (NoSuchMemberRuntimeException ex) {
            return Optional.empty();
        }
    }

    public Method getLocalMethodExact(String declaration, Class<?> ... parameters) {
        return getMember(declaration, name -> type.getDeclaredMethod(name, parameters));
    }

    public Optional<Method> getLocalMethod(String declaration, Class<?> ... parameters) {
        try {
            return Optional.of(getMember(declaration, name -> type.getDeclaredMethod(name, parameters)));
        }
        catch (NoSuchMemberRuntimeException ex) {
            return Optional.empty();
        }
    }

    public Field getFieldExact(String declaration) {
        return getMember(declaration, type::getField);
    }

    public Optional<Field> getField(String declaration) {
        try {
            return Optional.of(getMember(declaration, type::getField));
        }
        catch (NoSuchMemberRuntimeException ex) {
            return Optional.empty();
        }
    }

    public Field getLocalFieldExact(String declaration) {
        return getMember(declaration, type::getDeclaredField);
    }

    public Optional<Field> getLocalField(String declaration) {
        try {
            return Optional.of(getMember(declaration, type::getDeclaredField));
        }
        catch (NoSuchMemberRuntimeException ex) {
            return Optional.empty();
        }
    }

    public Set<Method> getAllMethods() {
        Set<Method> allMethods = this.allMethods;
        if (allMethods == null) {
            Set<Method> collection = new HashSet<>();
            getFullHierarchy().forEach(t -> Stream.of(t.getDeclaredMethods())
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
            getFullHierarchy().forEach(t -> Stream.of(t.getDeclaredFields())
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

    private static <T extends Member> T getMember(String declaration, ReflectiveFunction<String, T> getter) {
        declaration = declaration.trim();
        Set<Modifier> modifierSet = Collections.emptySet();
        int lastDelimiter = declaration.lastIndexOf(" ");
        if (lastDelimiter != -1) {
            modifierSet = Modifier.parse(declaration.substring(0, lastDelimiter));
            declaration = declaration.substring(lastDelimiter + 1);
        }
        T member = getter.apply(declaration);
        EnumSet<Modifier> memberModifiers = Modifier.unpack(member.getModifiers());
        if (!memberModifiers.containsAll(modifierSet)) {
            modifierSet.removeAll(memberModifiers);
            throw new NoSuchMemberRuntimeException("Found member does not contain modifier(s): " + modifierSet +
                                                   ", member is " + member);
        }
        return member;
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

    public static final Predicate<Method> PUBLIC_METHODS = method -> Modifier.PUBLIC.presentIn(method.getModifiers());

    public static final Predicate<Field> COPYABLE_FIELDS = field -> !Modifier.STATIC.presentIn(field.getModifiers())
                                                                 && !Modifier.FINAL.presentIn(field.getModifiers());
}
