package org.libsmith.anvil.reflection;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 06.08.16 19:28
 */
@FunctionalInterface
public interface ReflectiveFunction<T, R> extends Function<T, R> {

    R applyReflection(T argument) throws ReflectiveOperationException;

    @Override
    default R apply(T argument) {
        try {
            return applyReflection(argument);
        }
        catch (ReflectiveOperationException ex) {
            throw ReflectiveOperationRuntimeException.translate(ex);
        }
    }

    default Optional<R> applyOp(T argument) {
        try {
            return Optional.of(applyReflection(argument));
        }
        catch (ReflectiveOperationException ex) {
            return Optional.empty();
        }
    }
}
