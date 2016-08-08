package org.libsmith.anvil.reflection;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 09.06.16 1:14
 */
public class ReflectiveOperationRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -1351211962042586932L;

    public ReflectiveOperationRuntimeException(String message) {
        super(message);
    }

    public ReflectiveOperationRuntimeException(Throwable throwable) {
        super(throwable);
    }

    public static ReflectiveOperationRuntimeException translate(ReflectiveOperationException ex) {
        if (ex instanceof NoSuchFieldException || ex instanceof NoSuchMethodException) {
            throw new NoSuchMemberRuntimeException(ex);
        }
        throw new ReflectiveOperationRuntimeException(ex);
    }

    public static NoSuchMemberRuntimeException translate(NoSuchFieldException ex) {
        throw new NoSuchMemberRuntimeException(ex);
    }

    public static NoSuchMemberRuntimeException translate(NoSuchMethodException ex) {
        throw new NoSuchMemberRuntimeException(ex);
    }

    public static class NoSuchMemberRuntimeException extends ReflectiveOperationRuntimeException {

        private static final long serialVersionUID = 2282014946581299709L;

        public NoSuchMemberRuntimeException(String message) {
            super(message);
        }

        public NoSuchMemberRuntimeException(Throwable throwable) {
            super(throwable);
        }
    }
}
