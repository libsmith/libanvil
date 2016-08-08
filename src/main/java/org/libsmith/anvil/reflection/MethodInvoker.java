package org.libsmith.anvil.reflection;

import org.libsmith.anvil.reflection.ReflectiveOperationRuntimeException.NoSuchMemberRuntimeException;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.libsmith.anvil.reflection.Modifier.STATIC;
import static org.libsmith.anvil.reflection.ReflectionCommons.EMPTY_CLASS_ARRAY;
import static org.libsmith.anvil.reflection.ReflectionCommons.EMPTY_OBJECT_ARRAY;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 06.08.16 20:58
 */
public class MethodInvoker<T, R, S extends MethodInvoker<T, R, S>>
        implements ReflectionCommons.MemberCommons<Method, S>,
                   ReflectionCommons.AccessibleCommons<Method, S> {

    private final Method method;

    MethodInvoker(@Nonnull Method method) {
        this.method = method;
    }

    @Override
    public @Nonnull Method getReflectionSubject() {
        return method;
    }

    @SuppressWarnings("unchecked")
    protected R invokeAt(T object, Object[] args) {
        try {
            return (R) method.invoke(object, args);
        }
        catch (IllegalAccessException | InvocationTargetException ex) {
            throw new ReflectiveOperationRuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    @Nonnull <N> MethodInvoker<T, N, ?> returns(@Nonnull Class<N> returnType) {
        if (returnType.isAssignableFrom(method.getReturnType())) {
            throw new ClassCastException(method.getAnnotatedReturnType() + " cannot cast to " + returnType);
        }
        return (MethodInvoker<T, N, ?>) this;
    }

    @SuppressWarnings("unchecked")
    @Nonnull <N> Optional<? extends MethodInvoker<T, N, ?>> returnsOp(@Nonnull Class<N> returnType) {
        if (!returnType.isAssignableFrom(method.getReturnType())) {
            return Optional.empty();
        }
        return Optional.of((MethodInvoker<T, N, ?>) this);
    }

    @Override
    public String toString() {
        return "Method invoker '" + method + "'";
    }

    public abstract static class MethodInvokerWithObject<T, R, S extends MethodInvokerWithObject<T, R, S>>
            extends MethodInvoker<T, R, S> {

        protected final T object;

        MethodInvokerWithObject(@Nonnull Method method, T object) {
            super(method);
            this.object = object;
            if (object == null && STATIC.notPresentIn(method)) {
                throw new NoSuchMemberRuntimeException("Method must be static: " + method);
            }
        }

        @Override
        public String toString() {
            return super.toString() + (object == null ? " of static method" : " of object '" + object + "'");
        }
    }

    public static class UncheckedArgument<T, R> extends MethodInvoker<T, R, UncheckedArgument<T, R>> {

        UncheckedArgument(@Nonnull Method method) {
            super(method);
        }

        @Override
        public R invokeAt(@Nonnull T object, Object ... args) {
            return super.invokeAt(object, args);
        }

        public UncheckedArgumentWithObject<T, R> asStatic() {
            return new UncheckedArgumentWithObject<>(getReflectionSubject(), null);
        }

        public Optional<UncheckedArgumentWithObject<T, R>> asStaticOp() {
            Method method = getReflectionSubject();
            return STATIC.presentIn(method) ? Optional.of(new UncheckedArgumentWithObject<>(method, null))
                                            : Optional.empty();
        }

        public UncheckedArgumentWithObject<T, R> withObject(@Nonnull T object) {
            return new UncheckedArgumentWithObject<>(getReflectionSubject(), object);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @Nonnull <N> UncheckedArgument<T, N> returns(@Nonnull Class<N> returnType) {
            return (UncheckedArgument<T, N>) super.returns(returnType);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @Nonnull <N> Optional<UncheckedArgument<T, N>> returnsOp(@Nonnull Class<N> returnType) {
            return (Optional<UncheckedArgument<T, N>>) super.returnsOp(returnType);
        }
    }

    public static class UncheckedArgumentWithObject<T, R>
            extends MethodInvokerWithObject<T, R, UncheckedArgumentWithObject<T, R>> {

        UncheckedArgumentWithObject(@Nonnull Method method, T object) {
            super(method, object);
        }

        public R invoke(Object ... args) {
            return invokeAt(object, args);
        }

        public Function<Object[], R> asFunction() {
            return this::invoke;
        }
    }

    public static class NoArgument<T, R> extends MethodInvoker<T, R, NoArgument<T, R>> {

        NoArgument(@Nonnull Method method) {
            super(method);
        }

        public R invokeAt(@Nonnull T object) {
            return invokeAt(object, EMPTY_OBJECT_ARRAY);
        }

        public NoArgumentWithObject<T, R> asStatic() {
            return new NoArgumentWithObject<>(getReflectionSubject(), null);
        }

        public Optional<NoArgumentWithObject<T, R>> asStaticOp() {
            Method method = getReflectionSubject();
            return STATIC.presentIn(method) ? Optional.of(new NoArgumentWithObject<>(method, null))
                                            : Optional.empty();
        }

        public NoArgumentWithObject<T, R> withObject(@Nonnull T object) {
            return new NoArgumentWithObject<>(getReflectionSubject(), object);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @Nonnull <N> NoArgument<T, N> returns(@Nonnull Class<N> returnType) {
            return (NoArgument<T, N>) super.returns(returnType);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @Nonnull <N> Optional<NoArgument<T, N>> returnsOp(@Nonnull Class<N> returnType) {
            return (Optional<NoArgument<T, N>>) super.returnsOp(returnType);
        }
    }

    public static class NoArgumentWithObject<T, R>
            extends MethodInvokerWithObject<T, R, NoArgumentWithObject<T, R>> {

        NoArgumentWithObject(@Nonnull Method method, T object) {
            super(method, object);
        }

        public R invoke() {
            return invokeAt(object, EMPTY_OBJECT_ARRAY);
        }

        public Supplier<R> asSupplier() {
            return this::invoke;
        }
    }

    public static class SingleArgument<T, A0, R> extends MethodInvoker<T, R, SingleArgument<T, A0, R>> {

        SingleArgument(@Nonnull Method method) {
            super(method);
        }

        public R invokeAt(@Nonnull T object, A0 arg0) {
            return super.invokeAt(object, new Object[] { arg0 });
        }

        public SingleArgumentWithObject<T, A0, R> asStatic() {
            return new SingleArgumentWithObject<>(getReflectionSubject(), null);
        }

        public Optional<SingleArgumentWithObject<T, A0, R>> asStaticOp() {
            Method method = getReflectionSubject();
            return STATIC.presentIn(method) ? Optional.of(new SingleArgumentWithObject<>(method, null))
                                            : Optional.empty();
        }

        public SingleArgumentWithObject<T, A0, R> withObject(@Nonnull T object) {
            return new SingleArgumentWithObject<>(getReflectionSubject(), object);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @Nonnull <N> SingleArgument<T, A0, N> returns(@Nonnull Class<N> returnType) {
            return (SingleArgument<T, A0, N>) super.returns(returnType);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @Nonnull <N> Optional<SingleArgument<T, A0, N>> returnsOp(@Nonnull Class<N> returnType) {
            return (Optional<SingleArgument<T, A0, N>>) super.returnsOp(returnType);
        }
    }

    public static class SingleArgumentWithObject<T, A0, R>
            extends MethodInvokerWithObject<T, R, SingleArgumentWithObject<T, A0, R>> {

        SingleArgumentWithObject(@Nonnull Method method, T object) {
            super(method, object);
        }

        public R invoke(A0 arg0) {
            return invokeAt(object, new Object[] { arg0 });
        }

        public Function<A0, R> asFunction() {
            return this::invoke;
        }
    }

    public static class BiArgument<T, A0, A1, R> extends MethodInvoker<T, R, BiArgument<T, A0, A1, R>> {

        BiArgument(@Nonnull Method method) {
            super(method);
        }

        public R invokeAt(T object, A0 arg0, A1 arg1) {
            return super.invokeAt(object, new Object[] { arg0, arg1 });
        }

        public BiArgumentWithObject<T, A0, A1, R> asStatic() {
            return new BiArgumentWithObject<>(getReflectionSubject(), null);
        }

        public Optional<BiArgumentWithObject<T, A0, A1, R>> asStaticOp() {
            Method method = getReflectionSubject();
            return STATIC.presentIn(method) ? Optional.of(new BiArgumentWithObject<>(method, null))
                                            : Optional.empty();
        }

        public BiArgumentWithObject<T, A0, A1, R> withObject(@Nonnull T object) {
            return new BiArgumentWithObject<>(getReflectionSubject(), object);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @Nonnull <N> BiArgument<T, A0, A1, N> returns(@Nonnull Class<N> returnType) {
            return (BiArgument<T, A0, A1, N>) super.returns(returnType);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @Nonnull <N> Optional<BiArgument<T, A0, A1, N>> returnsOp(@Nonnull Class<N> returnType) {
            return (Optional<BiArgument<T, A0, A1, N>>) super.returnsOp(returnType);
        }
    }

    public static class BiArgumentWithObject<T, A0, A1, R>
            extends MethodInvokerWithObject<T, R, BiArgumentWithObject<T, A0, A1, R>>  {

        BiArgumentWithObject(@Nonnull Method method, T object) {
            super(method, object);
        }

        public R invoke(A0 arg0, A1 arg1) {
            return invokeAt(object, new Object[] { arg0, arg1 });
        }

        public BiFunction<A0, A1, R> asBiFunction() {
            return this::invoke;
        }
    }

    public static class BiAndRestArgument<T, A0, A1, R> extends MethodInvoker<T, R, BiAndRestArgument<T, A0, A1, R>> {

        BiAndRestArgument(@Nonnull Method method) {
            super(method);
        }

        public R invokeAt(@Nonnull T object, A0 arg0, A1 arg1, Object ... restArguments) {
            Object[] args = new Object[restArguments.length + 2];
            args[0] = arg0;
            args[1] = arg1;
            System.arraycopy(restArguments, 0, args, 2, restArguments.length);
            return super.invokeAt(object, args);
        }

        public BiAndRestArgumentWithObject<T, A0, A1, R> asStatic() {
            return new BiAndRestArgumentWithObject<>(getReflectionSubject(), null);
        }

        public Optional<BiAndRestArgumentWithObject<T, A0, A1, R>> asStaticOp() {
            Method method = getReflectionSubject();
            return STATIC.presentIn(method) ? Optional.of(new BiAndRestArgumentWithObject<>(method, null))
                                            : Optional.empty();
        }

        public BiAndRestArgumentWithObject<T, A0, A1, R> withObject(@Nonnull T object) {
            return new BiAndRestArgumentWithObject<>(getReflectionSubject(), object);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @Nonnull <N> BiAndRestArgument<T, A0, A1, N> returns(@Nonnull Class<N> returnType) {
            return (BiAndRestArgument<T, A0, A1, N>) super.returns(returnType);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @Nonnull <N> Optional<BiAndRestArgument<T, A0, A1, N>> returnsOp(@Nonnull Class<N> returnType) {
            return (Optional<BiAndRestArgument<T, A0, A1, N>>) super.returnsOp(returnType);
        }
    }

    public static class BiAndRestArgumentWithObject<T, A0, A1, R>
            extends MethodInvokerWithObject<T, R, BiArgumentWithObject<T, A0, A1, R>>  {

        BiAndRestArgumentWithObject(@Nonnull Method method, T object) {
            super(method, object);
        }

        public R invoke(A0 arg0, A1 arg1, Object ... restArguments) {
            Object[] args = new Object[restArguments.length + 2];
            args[0] = arg0;
            args[1] = arg1;
            System.arraycopy(restArguments, 0, args, 2, restArguments.length);
            return super.invokeAt(object, args);
        }
    }

    public static class Resolver<T> {

        private final String name;
        private final ReflectiveBiFunction<String, Class<?>[], Method> methodResolver;

        Resolver(String name, ReflectiveBiFunction<String, Class<?>[], Method> methodResolver) {
            this.name = name;
            this.methodResolver = methodResolver;
        }

        public NoArgument<T, ?> noParams() {
            return new NoArgument<>(methodResolver.apply(name, EMPTY_CLASS_ARRAY));
        }

        public UncheckedArgument<T, ?> paramsUnchecked(@Nonnull Class<?> ... paramTypes) {
            return new UncheckedArgument<>(methodResolver.apply(name, paramTypes));
        }

        public <P0> SingleArgument<T, P0, ?> params(@Nonnull Class<P0> param0) {
            return new SingleArgument<>(methodResolver.apply(name, new Class[] { param0 }));
        }

        public <P0, P1> BiArgument<T, P0, P1, ?> params(@Nonnull Class<P0> param0, @Nonnull Class<P1> param1) {
            return new BiArgument<>(methodResolver.apply(name, new Class[] { param0, param1 }));
        }

        public <P0, P1> BiAndRestArgument<T, P0, P1, ?> params(
                @Nonnull Class<P0> param0, @Nonnull Class<P1> param1, @Nonnull Class<?> ... restParams) {

            Class<?>[] params = new Class[restParams.length + 2];
            params[0] = param0;
            params[1] = param1;
            System.arraycopy(restParams, 0, params, 2, restParams.length);
            return new BiAndRestArgument<>(methodResolver.apply(name, params));
        }

        public Optional<? extends NoArgument<T, ?>> noParamsOp() {
            return methodResolver.applyOp(name, EMPTY_CLASS_ARRAY).map(NoArgument::new);
        }

        public Optional<? extends UncheckedArgument<T, ?>> paramsUncheckedOp(@Nonnull Class<?> ... paramTypes) {
            return methodResolver.applyOp(name, paramTypes).map(UncheckedArgument::new);
        }

        public <P0> Optional<? extends SingleArgument<T, P0, ?>> paramsOp(@Nonnull Class<P0> param0) {
            return methodResolver.applyOp(name, new Class[] { param0 }).map(SingleArgument::new);
        }

        public <P0, P1> Optional<? extends BiArgument<T, P0, P1, ?>> paramsOp(
                @Nonnull Class<P0> param0, @Nonnull Class<P1> param1) {
            return methodResolver.applyOp(name, new Class[] { param0, param1 }).map(BiArgument::new);
        }

        public <P0, P1> Optional<? extends BiAndRestArgument<T, P0, P1, ?>> paramsOp(
                @Nonnull Class<P0> param0, @Nonnull Class<P1> param1, @Nonnull Class<?> ... restParams) {
            Class<?>[] params = new Class[restParams.length + 2];
            params[0] = param0;
            params[1] = param1;
            System.arraycopy(restParams, 0, params, 2, restParams.length);
            return methodResolver.applyOp(name, params).map(BiAndRestArgument::new);
        }
    }
}
