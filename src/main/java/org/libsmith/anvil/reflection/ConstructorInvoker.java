package org.libsmith.anvil.reflection;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static org.libsmith.anvil.reflection.ReflectionCommons.EMPTY_CLASS_ARRAY;
import static org.libsmith.anvil.reflection.ReflectionCommons.EMPTY_OBJECT_ARRAY;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 07.08.16 18:09
 */
public class ConstructorInvoker<T, S extends ConstructorInvoker<T, S>>
        implements ReflectionCommons.MemberCommons<Constructor<T>, S>,
                   ReflectionCommons.AccessibleCommons<Constructor<T>, S> {

    private final Constructor<T> constructor;

    ConstructorInvoker(Constructor<T> constructor) {
        this.constructor = constructor;
    }

    public Constructor<T> getConstructor() {
        return constructor;
    }

    @Override
    public @Nonnull Constructor<T> getReflectionSubject() {
        return constructor;
    }

    protected T construct(Object[] args) {
        try {
            return constructor.newInstance(args);
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new ReflectiveOperationRuntimeException(ex);
        }
    }

    public static class UncheckedArgument<T> extends ConstructorInvoker<T, UncheckedArgument<T>> {

        UncheckedArgument(@Nonnull Constructor<T> constructor) {
            super(constructor);
        }

        @Override
        public T construct(Object... args) {
            return super.construct(args);
        }
    }

    public static class NoArgument<T> extends ConstructorInvoker<T, NoArgument<T>> {

        NoArgument(@Nonnull Constructor<T> constructor) {
            super(constructor);
        }

        public T construct() {
            return super.construct(EMPTY_OBJECT_ARRAY);
        }
    }

    public static class SingleArgument<T, A0> extends ConstructorInvoker<T, SingleArgument<T, A0>> {

        SingleArgument(@Nonnull Constructor<T> constructor) {
            super(constructor);
        }

        public T construct(A0 arg0) {
            return super.construct(new Object[] { arg0 });
        }
    }

    public static class BiArgument<T, A0, A1> extends ConstructorInvoker<T, BiArgument<T, A0, A1>> {

        BiArgument(@Nonnull Constructor<T> constructor) {
            super(constructor);
        }

        public T construct(A0 arg0, A1 arg1) {
            return super.construct(new Object[] { arg0, arg1 });
        }
    }

    public static class BiAndRestArgument<T, A0, A1> extends ConstructorInvoker<T, BiAndRestArgument<T, A0, A1>> {

        BiAndRestArgument(@Nonnull Constructor<T> constructor) {
            super(constructor);
        }

        public T construct(A0 arg0, A1 arg1, Object... restArguments) {
            Object[] args = new Class[restArguments.length + 2];
            args[0] = arg0;
            args[1] = arg1;
            System.arraycopy(restArguments, 0, args, 2, restArguments.length);
            return super.construct(args);
        }
    }

    @SuppressWarnings("Convert2MethodRef")
    public static class Resolver<T> {

        private final ReflectiveFunction<Class<?>[], Constructor<T>> resolver;

        Resolver(ReflectiveFunction<Class<?>[], Constructor<T>> resolver) {
            this.resolver = resolver;
        }

        public NoArgument<T> noParams() {
            return new NoArgument<>(resolver.apply(EMPTY_CLASS_ARRAY));
        }

        public UncheckedArgument<T> paramsUnchecked(Class<?>... paramTypes) {
            return new UncheckedArgument<>(resolver.apply(paramTypes));
        }

        public <P0> SingleArgument<T, P0> params(Class<P0> param0) {
            return new SingleArgument<>(resolver.apply(new Class[] { param0 }));
        }

        public <P0, P1> BiArgument<T, P0, P1> params(Class<P0> param0, Class<P1> param1) {
            return new BiArgument<>(resolver.apply(new Class[] { param0, param1 }));
        }

        public <P0, P1> BiAndRestArgument<T, P0, P1> params(
                Class<P0> param0, Class<P1> param1, Class<?>... restParams) {

            Class<?>[] params = new Class[restParams.length + 2];
            params[0] = param0;
            params[1] = param1;
            System.arraycopy(restParams, 0, params, 2, restParams.length);
            return new BiAndRestArgument<>(resolver.apply(params));
        }

        public Optional<? extends NoArgument<T>> noParamsOp() {
            return resolver.applyOp(EMPTY_CLASS_ARRAY).map(NoArgument::new);
        }

        public Optional<? extends UncheckedArgument<T>> paramsUncheckedOp(Class<?>... paramTypes) {
            return resolver.applyOp(paramTypes).map(UncheckedArgument::new);
        }

        public <P0> Optional<SingleArgument<T, P0>> paramsOp(Class<P0> param0) {
            return resolver.applyOp(new Class[] { param0 }).map(c -> new SingleArgument<>(c));
        }

        public <P0, P1> Optional<BiArgument<T, P0, P1>> paramsOp(
                Class<P0> param0, Class<P1> param1) {
            return resolver.applyOp(new Class[] { param0, param1 }).map(c -> new BiArgument<>(c));
        }

        public <P0, P1> Optional<? extends BiAndRestArgument<T, P0, P1>> paramsOp(
                Class<P0> param0, Class<P1> param1, Class<?>... restParams) {
            Class<?>[] params = new Class[restParams.length + 2];
            params[0] = param0;
            params[1] = param1;
            System.arraycopy(restParams, 0, params, 2, restParams.length);
            return resolver.applyOp(params).map(c -> new BiAndRestArgument<>(c));
        }
    }
}
