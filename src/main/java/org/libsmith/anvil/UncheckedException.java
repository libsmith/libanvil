package org.libsmith.anvil;

import java.util.concurrent.Callable;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 12.07.16
 */
public class UncheckedException extends RuntimeException {

    private static final long serialVersionUID = -3877272876662615234L;

    protected UncheckedException(Throwable ex) {
        super(ex);
    }

    public static RuntimeException wrap(Throwable throwable) {
        return throwable instanceof RuntimeException
                    ? (RuntimeException) throwable
                    : new UncheckedException(throwable);
    }

    public static <T> T wrap(Callable<T> callback) {
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

    public static void wrap(Runnable callback) {
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

    public static void wrap(ThrowableRunnable callback) {
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

    public static <T> T rethrow(Callable<T> callback) {
        try {
            return callback.call();
        }
        catch (Exception ex) {
            throw rethrow(ex);
        }
    }

    public static void rethrow(ThrowableRunnable callback) {
        try {
            callback.run();
        }
        catch (Exception ex) {
            throw rethrow(ex);
        }
    }

    public static void rethrow(Runnable callback) {
        try {
            callback.run();
        }
        catch (Exception ex) {
            throw rethrow(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> T rethrow(Throwable throwable) throws T {
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
                throw rethrow(th);
            }
        }

        void runEx() throws Throwable;
    }
}
