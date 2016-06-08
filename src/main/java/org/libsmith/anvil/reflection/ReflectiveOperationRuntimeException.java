package org.libsmith.anvil.reflection;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 09.06.16 1:14
 */
public class ReflectiveOperationRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -1351211962042586932L;

    public ReflectiveOperationRuntimeException(Throwable throwable) {
        super(throwable);
    }
}
