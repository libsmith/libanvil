package org.libsmith.anvil.time;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 21.12.2015 22:09
 */
public class ImmutableTimestamp extends Timestamp {

    private static final long serialVersionUID = -7579071256449554807L;

    public ImmutableTimestamp(long time) {
        super(time);
    }

    public ImmutableTimestamp(@Nonnull Date date) {
        super(date.getTime());
    }

    @Override
    public int compareTo(@Nonnull Date anotherDate) {
        return Long.compare(getTime(), anotherDate.getTime());
    }

    @Override
    public boolean after(@Nonnull Date when) {
        return getTime() > when.getTime();
    }

    @Override
    public boolean before(@Nonnull Date when) {
        return getTime() < when.getTime();
    }

    public ImmutableTimestamp add(long duration, @Nonnull TimeUnit unit) {
        return new ImmutableTimestamp(getTime() + unit.toMillis(duration));
    }

    public ImmutableTimestamp add(TimePeriod timePeriod) {
        return new ImmutableTimestamp(getTime() + timePeriod.getPeriod());
    }

    public ImmutableTimestamp sub(long duration, @Nonnull TimeUnit unit) {
        return new ImmutableTimestamp(getTime() - unit.toMillis(duration));
    }

    public ImmutableTimestamp sub(TimePeriod timePeriod) {
        return new ImmutableTimestamp(getTime() - timePeriod.getPeriod());
    }

    public TimePeriod sub(@Nonnull Date date) {
        return new TimePeriod(getTime() - date.getTime(), TimeUnit.MILLISECONDS);
    }

    public ImmutableTimestamp quantize(@Nonnull TimeUnit quantumUnit) {
        return quantize(this, quantumUnit);
    }

    //@Contract("null, _ -> null")
    public static ImmutableTimestamp quantize(@Nullable Timestamp timestamp, @Nonnull TimeUnit quantumUnit) {
        return timestamp == null ? null
                : new ImmutableTimestamp(quantumUnit.toMillis(quantumUnit.convert(timestamp.getTime(), TimeUnit.MILLISECONDS)));
    }

    @SafeVarargs
    public static <T extends Timestamp> T max(T ... values) {
        T ret = null;
        for (T value : values) {
            if (value != null && (ret == null || ret.getTime() < value.getTime())) {
                ret = value;
            }
        }
        return ret;
    }

    @SafeVarargs
    public static <T extends Timestamp> T min(T ... values) {
        T ret = null;
        for (T value : values) {
            if (value != null && (ret == null || ret.getTime() > value.getTime())) {
                ret = value;
            }
        }
        return ret;
    }

    //@Contract("null -> null")
    public static ImmutableTimestamp valueOf(@Nullable Date date) {
        return date == null ? null : date instanceof ImmutableTimestamp ? (ImmutableTimestamp) date : new ImmutableTimestamp(date);
    }

    public static ImmutableTimestamp now() {
        return new ImmutableTimestamp(System.currentTimeMillis());
    }

    //<editor-fold desc="Delegate methods">
    @Override
    public void setTime(long time) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNanos(int n) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setYear(int year) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setMonth(int month) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setDate(int date) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setHours(int hours) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setMinutes(int minutes) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setSeconds(int seconds) {
        throw new UnsupportedOperationException();
    }
    //</editor-fold>
}
