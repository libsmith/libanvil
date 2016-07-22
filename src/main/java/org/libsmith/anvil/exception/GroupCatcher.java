package org.libsmith.anvil.exception;

import java.util.function.Consumer;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 23.06.2016 21:30
 */
public class GroupCatcher<T extends Exception> implements AutoCloseable {

    private final Class<T> exceptionType;
    private T exception;

    public GroupCatcher(Class<T> exceptionType) {
        this.exceptionType = exceptionType;
    }

    public Unchecked<T> asUnchecked() {
        return new Unchecked<>(exceptionType);
    }

    public void trying(ThrowingRunnable runnable) {
        trying((Runnable) runnable);
    }

    public void trying(Runnable runnable) {
        try {
            runnable.run();
        }
        catch (Exception ex) {
            if (exceptionType.isInstance(ex)) {
                if (exception != null) {
                    exception.addSuppressed(ex);
                }
                else {
                    exception = exceptionType.cast(ex);
                }
            }
            else {
                throw GroupCatcher.<Error>__sneaky(ex);
            }
        }
    }

    public <E> void forEach(Iterable<E> iterable, Consumer<E> consumer) {
        iterable.forEach(e -> trying(() -> consumer.accept(e)));
    }

    @FunctionalInterface
    public interface ThrowingRunnable extends Runnable {

        @Override
        default void run() {
            try {
                runThrowing();
            }
            catch (Throwable throwable) {
                throw GroupCatcher.<Error>__sneaky(throwable);
            }
        }

        void runThrowing() throws Throwable;
    }

    @Override
    public void close() throws T {
        if (exception != null) {
            throw exception;
        }
    }

    public static class Unchecked<T extends Exception> extends GroupCatcher<T> {

        public Unchecked(Class<T> exceptionType) {
            super(exceptionType);
        }

        @Override
        public void close() {
            try {
                super.close();
            }
            catch (RuntimeException ex) {
                throw ex;
            }
            catch (Exception ex) {
                throw GroupCatcher.<Error>__sneaky(ex);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> E __sneaky(Throwable ex) throws E {
        throw (E) ex;
    }
}
