package org.libsmith.anvil.time;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 12.10.2015 15:57
 */
public class TzDate extends ImmutableDate {
    private static final long serialVersionUID = 6071078983631105670L;

    private final @Nonnull TimeZone timeZone;

    public TzDate() {
        this(System.currentTimeMillis(), TimeZone.getDefault());
    }

    public TzDate(@Nonnull Date date) {
        this(date.getTime(), TimeZone.getDefault());
    }

    public TzDate(long unixTimeMillis) {
        this(unixTimeMillis, TimeZone.getDefault());
    }

    public TzDate(@Nonnull TimeZone timeZone) {
        this(System.currentTimeMillis(), timeZone);
    }

    public TzDate(@Nonnull Date date, @Nonnull TimeZone timeZone) {
        this(date.getTime(), timeZone);
    }

    public TzDate(long unixTimeMillis, @Nonnull TimeZone timeZone) {
        super(unixTimeMillis);
        //noinspection ConstantConditions
        if (timeZone == null) {
            throw new NullPointerException("TimeZone is null");
        }
        this.timeZone = timeZone;
    }

    public @Nonnull TimeZone getTimeZone() {
        return timeZone;
    }

    //@Contract("null, _ -> null")
    public static TzDate of(@Nullable Date date, @Nullable TimeZone timeZone) {
        return date == null ? null : new TzDate(date, timeZone == null ? TimeZone.getDefault() : timeZone);
    }

    //@Contract("null, _ -> null")
    public static TzDate ofDateTime(@Nullable Date date, @Nullable TimeZone timeZone) {
        if (date == null) {
            return null;
        }
        Calendar localDate = Calendar.getInstance();
        localDate.setTime(date);

        Calendar utcDate = Calendar.getInstance();
        utcDate.setTimeZone(timeZone);
        //noinspection MagicConstant
        utcDate.set(localDate.get(Calendar.YEAR), localDate.get(Calendar.MONTH), localDate.get(Calendar.DAY_OF_MONTH),
                    localDate.get(Calendar.HOUR_OF_DAY), localDate.get(Calendar.MINUTE), localDate.get(Calendar.SECOND));

        return new TzDate(utcDate.getTime(), timeZone == null ? TimeZone.getDefault() : timeZone);
    }

    @Override
    public TzDate clone() {
        return (TzDate) super.clone();
    }

    @Override
    public String toString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        format.setTimeZone(timeZone);
        return format.format(this);
    }
}
