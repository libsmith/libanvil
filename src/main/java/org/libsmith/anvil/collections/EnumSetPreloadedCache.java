package org.libsmith.anvil.collections;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Dmitriy Balakin <balakin@0x0000.ru>
 * @created 05.08.2016 21:47
 */
public class EnumSetPreloadedCache<K extends Enum<K>, V> {

    private static final int DEFAULT_MAX_SIZE = 128;

    private final V[] cache;
    private final Function<Set<K>, V> loader;
    private final EnumSetPacker<K> packer;

    public static <K extends Enum<K>, V> EnumSetPreloadedCache<K, V> of(
            @Nonnull Class<K> enumClass, @Nonnull Function<Set<K>, V> loader) {
        return new EnumSetPreloadedCache<>(DEFAULT_MAX_SIZE, enumClass, loader);
    }

    public static <K extends Enum<K>, V> EnumSetPreloadedCache<K, V> of(
            int maxSize, @Nonnull Class<K> enumClass, @Nonnull Function<Set<K>, V> loader) {
        return new EnumSetPreloadedCache<>(maxSize, enumClass, loader);
    }

    @SuppressWarnings("unchecked")
    protected EnumSetPreloadedCache(int maxSize, @Nonnull Class<K> enumClass, @Nonnull Function<Set<K>, V> loader) {

        if (maxSize < 0) {
            throw new IllegalArgumentException("Max size must be greater than zero, got: " + maxSize);
        }

        this.packer = EnumSetPacker.of(enumClass);
        this.loader = loader;

        int length = packer.getMaxWidth();
        int size = length == 0 ? 0 : length > 32 ? Integer.MAX_VALUE : 1 << length - 1;
        if (size < length || size > maxSize) {
            size = maxSize;
        }

        V[] cache = (V[]) new Object[size];
        for (int i = 0; i < size; i++) {
            cache[i] = loader.apply(packer.unpack(i));
        }
        this.cache = cache;
    }

    public V get(Set<K> key) {
        int index = (int) packer.packToLongInexact(key);
        if (index < 0 || index >= cache.length || Integer.bitCount(index) < key.size()) {
            return loader.apply(key);
        }
        return cache[index];
    }
}
