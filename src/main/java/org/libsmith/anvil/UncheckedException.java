package org.libsmith.anvil;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 12.07.16
 */
public class UncheckedException extends RuntimeException {

    private static final long serialVersionUID = -3877272876662615234L;

    protected UncheckedException(@Nonnull Throwable ex) {
        super(ex);
        setStackTrace(ex.getStackTrace());
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    //<editor-fold desc="Description">
    public static RuntimeException wrap(@Nonnull Throwable throwable) {
        return throwable instanceof RuntimeException
                    ? (RuntimeException) throwable
                    : new UncheckedException(throwable);
    }
    //</editor-fold>

    public static <T> T wrap(@Nonnull Callable<T> callback) {
        try {
            return callback.call();
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new UncheckedException(ex);
        }
    }

    public static void wrap(@Nonnull ThrowableRunnable callback) {
        try {
            callback.run();
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new UncheckedException(ex);
        }
    }

    public static <T> T rethrow(@Nonnull Callable<T> callback) {
        try {
            return callback.call();
        }
        catch (Exception ex) {
            throw UncheckedException.<Error>__rethrow(ex);
        }
    }

    public static void rethrow(@Nonnull ThrowableRunnable callback) {
        try {
            callback.run();
        }
        catch (Exception ex) {
            throw UncheckedException.<Error>__rethrow(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> T __rethrow(@Nonnull Throwable throwable) throws T {
        throw (T) throwable;
    }

    @FunctionalInterface
    public interface ThrowableRunnable extends Runnable {

        @Override
        default void run() {
            try {
                runEx();
            }
            catch (Throwable th) {
                throw UncheckedException.<Error>__rethrow(th);
            }
        }

        void runEx() throws Throwable;
    }
}
