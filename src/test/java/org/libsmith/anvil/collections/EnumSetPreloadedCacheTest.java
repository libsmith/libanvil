package org.libsmith.anvil.collections;


import org.junit.Test;
import org.libsmith.anvil.collections.MockEnums.LongLongEnum;
import org.mockito.Mockito;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.libsmith.anvil.collections.MockEnums.LongLongEnum.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

/**
 * @author Dmitriy Balakin <balakin@0x0000.ru>
 * @created 05.08.2016 22:07
 */
public class EnumSetPreloadedCacheTest {

    @Test
    public void staticValuesTest() {
        EnumSetPreloadedCache<LongLongEnum, String> cache =
                EnumSetPreloadedCache.of(LongLongEnum.class, Object::toString);

        assertThat(cache.get(EnumSet.of(_1, _3, _4))).isEqualTo("[_1, _3, _4]");
        assertThat(cache.get(EnumSet.of(_0, _2, _5))).isEqualTo("[_0, _2, _5]");
        assertThat(cache.get(EnumSet.of(_0, _22, _31))).isEqualTo("[_0, _22, _31]");
        assertThat(cache.get(EnumSet.of(_4, _65, _127))).isEqualTo("[_4, _65, _127]");
    }

    @Test
    public void preloadTest() {

        class ToStringFunction implements Function<Set<LongLongEnum>, String> {
            @Override
            public String apply(Set<LongLongEnum> set) {
                return set.toString();
            }
        }

        Function<Set<LongLongEnum>, String> loader = Mockito.spy(new ToStringFunction());

        EnumSetPreloadedCache<LongLongEnum, String> cache =
                EnumSetPreloadedCache.of(40, LongLongEnum.class, loader);

        Mockito.verify(loader, times(40)).apply(any());

        EnumSetPacker<LongLongEnum> packer = EnumSetPacker.of(LongLongEnum.class);
        for (int i = 0; i < 40; i++) {
            Set<LongLongEnum> unpacked = packer.unpack(i);
            assertThat(cache.get(unpacked)).isEqualTo(unpacked.toString());
        }

        Mockito.verify(loader, times(40)).apply(any());

        for (int i = 0; i < 100_000; i++) {
            Set<LongLongEnum> unpacked = packer.unpack(i);
            assertThat(cache.get(unpacked)).isEqualTo(unpacked.toString());
        }
        Mockito.verify(loader, times(100_000)).apply(any());
    }
}
