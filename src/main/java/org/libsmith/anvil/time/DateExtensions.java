package org.libsmith.anvil.time;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 13.07.16
 */
public interface DateExtensions<T extends Date & DateExtensions> {

    @Nonnull T add(long duration, @Nonnull TimeUnit unit);

    @Nonnull T add(@Nonnull TimePeriod timePeriod);

    @Nonnull T sub(long duration, @Nonnull TimeUnit unit);

    @Nonnull T sub(@Nonnull TimePeriod timePeriod);

    @Nonnull TimePeriod sub(@Nonnull Date date);

    @Nonnull T quantize(@Nonnull TimeUnit quantumUnit);

    @SafeVarargs
    static <T extends Date> T max(T ... values) {
        return max(Arrays.asList(values));
    }

    static <T extends Date> T max(Iterable<T> values) {
        T ret = null;
        for (T value : values) {
            if (value != null && (ret == null || ret.getTime() < value.getTime())) {
                ret = value;
            }
        }
        return ret;
    }

    @SafeVarargs
    static <T extends Date> T min(T ... values) {
        return min(Arrays.asList(values));
    }

    static <T extends Date> T min(Iterable<T> values) {
        T ret = null;
        for (T value : values) {
            if (value != null && (ret == null || ret.getTime() > value.getTime())) {
                ret = value;
            }
        }
        return ret;
    }
}
