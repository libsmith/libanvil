package org.libsmith.anvil.time;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 12.10.2015 15:47
 */
public class ImmutableDate extends Date {

    private static final long serialVersionUID = -8681298118653084478L;

    public ImmutableDate(long date) {
        super(date);
    }

    public ImmutableDate(@Nonnull Date date) {
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

    public ImmutableDate add(long duration, @Nonnull TimeUnit unit) {
        return new ImmutableDate(getTime() + unit.toMillis(duration));
    }

    public ImmutableDate add(@Nonnull TimePeriod timePeriod) {
        return new ImmutableDate(getTime() + timePeriod.getPeriod());
    }

    public ImmutableDate sub(long duration, @Nonnull TimeUnit unit) {
        return new ImmutableDate(getTime() - unit.toMillis(duration));
    }

    public ImmutableDate sub(@Nonnull TimePeriod timePeriod) {
        return new ImmutableDate(getTime() - timePeriod.getPeriod());
    }

    public TimePeriod sub(@Nonnull Date date) {
        return new TimePeriod(getTime() - date.getTime(), TimeUnit.MILLISECONDS);
    }

    public ImmutableDate quantize(@Nonnull TimeUnit quantumUnit) {
        return quantize(this, quantumUnit);
    }

    //@Contract("null, _ -> null")
    public static ImmutableDate quantize(@Nullable Date date, @Nonnull TimeUnit quantumUnit) {
        return date == null ? null
                : new ImmutableDate(quantumUnit.toMillis(quantumUnit.convert(date.getTime(), TimeUnit.MILLISECONDS)));
    }

    //@Contract("null -> null")
    public static ImmutableDate of(@Nullable Date date) {
        return date == null ? null : date instanceof ImmutableDate ? (ImmutableDate) date : new ImmutableDate(date);
    }

    public static ImmutableDate now() {
        return new ImmutableDate(System.currentTimeMillis());
    }

    @SafeVarargs
    public static <T extends Date> T max(T ... values) {
        T ret = null;
        for (T value : values) {
            if (value != null && (ret == null || ret.getTime() < value.getTime())) {
                ret = value;
            }
        }
        return ret;
    }

    @SafeVarargs
    public static <T extends Date> T min(T ... values) {
        T ret = null;
        for (T value : values) {
            if (value != null && (ret == null || ret.getTime() > value.getTime())) {
                ret = value;
            }
        }
        return ret;
    }

    //<editor-fold desc="Delegate methods">
    @Override
    public ImmutableDate clone() {
        return (ImmutableDate) super.clone();
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

    @Override
    public void setTime(long time) {
        throw new UnsupportedOperationException();
    }
    //</editor-fold>
}
