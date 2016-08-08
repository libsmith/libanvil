package org.libsmith.anvil.reflection;

import javax.annotation.Nonnull;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 07.08.16 23:53
 */
public interface ReflectionSubjectAware<T> {

    @Nonnull T getReflectionSubject();

}
