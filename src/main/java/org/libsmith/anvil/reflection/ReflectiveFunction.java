package org.libsmith.anvil.reflection;

import java.util.function.Function;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 06.08.16 19:28
 */
@FunctionalInterface
public interface ReflectiveFunction<T, R> extends Function<T, R> {

    @Override
    default R apply(T t) {
        try {
            return applyReflection(t);
        }
        catch (ReflectiveOperationException ex) {
            throw ReflectiveOperationRuntimeException.translate(ex);
        }
    }

    R applyReflection(T t) throws ReflectiveOperationException;
}
