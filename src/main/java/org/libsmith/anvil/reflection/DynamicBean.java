package org.libsmith.anvil.reflection;

import org.libsmith.anvil.text.Strings;

import java.lang.annotation.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 05.10.16 1:38
 */
public interface DynamicBean {

    static DynamicBean of(Supplier<Map<? super String, Object>> propertiesSupplier) {
        return new Impl(propertiesSupplier);
    }

    static DynamicBean of(Map<? super String, Object> properties) {
        return new Impl(() -> properties);
    }

    @SuppressWarnings("unchecked")
    <T> T as(Class<T> iface);

    @Inherited
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Property {
        String name() default "";
    }

    @Inherited
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Namespace {
        String value() default "";
        boolean asClass() default false;
    }

    class Impl implements DynamicBean {

        private final Supplier<Map<? super String, Object>> propertiesSupplier;

        protected Impl(Supplier<Map<? super String, Object>> propertiesSupplier) {
            this.propertiesSupplier = propertiesSupplier;
        }

        @Override
        @SuppressWarnings("unchecked")
        public final <T> T as(Class<T> iface) {

            Map<Method, MethodInvoker> accessors = new ConcurrentHashMap<>();
            InvocationHandler invocationHandler = (proxy, method, args) -> {
                MethodInvoker accessor = accessors.computeIfAbsent(method, this::makeAccessor);
                return accessor.invoke(proxy, args);
            };
            return (T) Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(),
                    new Class[] { iface }, invocationHandler);
        }

        private MethodInvoker makeAccessor(Method method) {

            String name = method.getName();
            if (method.getDeclaringClass() == Object.class) {
                if ("equals".equals(name)) {
                    return (self, args) -> self == args[0];
                }
                if ("hashCode".equals(name)) {
                    int hash = ThreadLocalRandom.current().nextInt();
                    return (self, args) -> hash;
                }
                if ("toString".equals(name)) {
                    return (self, args) -> self.getClass().getName() + "@" + self.hashCode();
                }
            }
            if (method.getDeclaringClass() == DynamicBean.class) {
                return (self, args) -> method.invoke(this, args);
            }
            if (method.isDefault()) {
                try {
                    Class<?> declaringClass = method.getDeclaringClass();
                    MethodHandles.Lookup lookup = null;
                    if (LOOKUP_CONSTRUCTOR != null) {
                        try {
                            lookup = LOOKUP_CONSTRUCTOR.newInstance(declaringClass, MethodHandles.Lookup.PRIVATE);
                        }
                        catch (InstantiationException | InvocationTargetException ignored)
                        { }
                    }
                    if (lookup == null) {
                        lookup = MethodHandles.lookup().in(declaringClass);
                    }
                    MethodHandle methodHandle = lookup.unreflectSpecial(method, declaringClass);
                    return (proxy, args) -> methodHandle.bindTo(proxy).invokeWithArguments(args);
                }
                catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
            Namespace namespaceDescriptor = method.getDeclaringClass().getAnnotation(Namespace.class);
            Function<String, String> namespace = val -> {
                if (namespaceDescriptor == null) {
                    return val;
                }
                StringBuilder sb = new StringBuilder();
                if (namespaceDescriptor.asClass()) {
                    sb.append(method.getDeclaringClass().getName());
                }
                if (Strings.isNotBlank(namespaceDescriptor.value())) {
                    if (sb.length() != 0) {
                        sb.append(".");
                    }
                    sb.append(namespaceDescriptor.value());
                }
                if (sb.length() != 0) {
                    sb.append(".");
                    sb.append(val);
                    return sb.toString();
                }
                else {
                    return val;
                }
            };

            Optional<Property> methodDescriptor = Optional.ofNullable(method.getAnnotation(Property.class));
            Optional<String> nameFromDescriptor =
                    methodDescriptor.flatMap(p -> p.name().isEmpty() ? Optional.empty() : Optional.of(p.name()));

            if (name.length() > 2 && name.startsWith("is") && method.getReturnType() == boolean.class) {
                String propertyName = namespace.apply(nameFromDescriptor.orElseGet(() -> substringPropertyNameAt(2, name)));
                return (self, args) -> Boolean.TRUE == propertiesSupplier.get().get(propertyName);
            }
            if (name.length() > 3) {
                String propertyName = namespace.apply(nameFromDescriptor.orElseGet(() -> substringPropertyNameAt(3, name)));
                if (name.startsWith("get")) {
                    if (method.getReturnType() == Optional.class) {
                        return (self, args) -> Optional.ofNullable(propertiesSupplier.get().get(propertyName));
                    }
                    return (self, args) -> propertiesSupplier.get().get(propertyName);
                }
                else if (name.startsWith("set") && method.getParameterCount() == 1) {
                    return (self, args) -> propertiesSupplier.get().put(propertyName, args[0]);
                }
            }
            throw new UnsupportedOperationException(method.toString());
        }

        private static String substringPropertyNameAt(int position, String methodName) {

            return Character.toLowerCase(methodName.charAt(position)) + methodName.substring(position + 1);
        }

        @FunctionalInterface
        private interface MethodInvoker {

            Object invoke(Object self, Object ... parameters) throws Throwable;
        }

        private static final Constructor<MethodHandles.Lookup> LOOKUP_CONSTRUCTOR;
        static {
            Constructor<MethodHandles.Lookup> lookupConstructor = null;
            try {
                lookupConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
                lookupConstructor.setAccessible(true);
            }
            catch (NoSuchMethodException ignored)
            { }
            LOOKUP_CONSTRUCTOR = lookupConstructor;
        }
    }
}
