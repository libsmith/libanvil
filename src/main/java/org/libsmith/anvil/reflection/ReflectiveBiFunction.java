package org.libsmith.anvil.reflection;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 06.08.16 22:48
 */
@FunctionalInterface
public interface ReflectiveBiFunction<T, U, R> extends BiFunction<T, U, R> {

    R applyReflection(T arg0, U arg1) throws ReflectiveOperationException;

    @Override
    default R apply(T arg0, U arg1) {
        try {
            return applyReflection(arg0, arg1);
        }
        catch (ReflectiveOperationException ex) {
            throw ReflectiveOperationRuntimeException.translate(ex);
        }
    }

    default Optional<R> applyOp(T arg0, U arg1) {
        try {
            return Optional.of(applyReflection(arg0, arg1));
        }
        catch (ReflectiveOperationException ex) {
            return Optional.empty();
        }
    }
}
