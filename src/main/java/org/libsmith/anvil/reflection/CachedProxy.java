package org.libsmith.anvil.reflection;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 01.02.2016 0:47
 */
public interface CachedProxy {

    void dropCaches();

    @SuppressWarnings("unchecked")
    static <T> T wrap(final T object, Class<T> proxyInterface) {
        final Map<Key, Object> cache = Collections.synchronizedMap(new HashMap<Key, Object>());
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                                          new Class[] { proxyInterface, CachedProxy.class },
                (proxy, method, args) -> {
                    if (DROP_CACHES_METHOD.equals(method)) {
                        cache.clear();
                        return null;
                    }
                    Key key = new Key(method, args);
                    synchronized (cache) {
                        Object result = cache.get(key);
                        if (result == null && !cache.containsKey(key)) {
                            result = method.invoke(object, args);
                            cache.put(key, result);
                        }
                        return result;
                    }
                });
    }

    final class Key {
        private final Method method;
        private final Object[] args;
        private final int argsHash;

        private Key(Method method, Object[] args) {
            this.method = method;
            this.args = args;
            this.argsHash = Arrays.hashCode(args);
        }

        @Override
        public int hashCode() {
            return method.hashCode() + argsHash;
        }

        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object o) {
            Key key = (Key) o;
            return this == key || Objects.equals(method, key.method) && Arrays.equals(args, key.args);
        }
    }

    Method DROP_CACHES_METHOD = ReflectionUtils.getMethod(CachedProxy.class, "dropCaches");
}
