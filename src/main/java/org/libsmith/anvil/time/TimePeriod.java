package org.libsmith.anvil.time;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 28.10.2015 16:26
 */
public class TimePeriod implements Serializable, Comparable<TimePeriod> {
    private static final long serialVersionUID = -4819273455430547917L;

    public static final TimePeriod ZERO = new TimePeriod(0, TimeUnit.NANOSECONDS);
    public static final TimePeriod MIN_VALUE = new TimePeriod(Long.MIN_VALUE, TimeUnit.DAYS);
    public static final TimePeriod MAX_VALUE = new TimePeriod(Long.MAX_VALUE, TimeUnit.DAYS);

    private static Pattern PARSE_PATTERN = Pattern.compile("(\\d+)\\s*(\\p{Alpha}*)");

    private final long duration;
    private final TimeUnit timeUnit;

    public TimePeriod(long duration, @Nonnull TimeUnit timeUnit) {
        //noinspection ConstantConditions
        if (timeUnit == null) {
            throw new IllegalArgumentException();
        }
        this.duration = duration;
        this.timeUnit = timeUnit;
    }

    public long getPeriod() { //TODO: refactor to getDurationMillis
        return getPeriod(TimeUnit.MILLISECONDS);
    }

    public long getPeriod(TimeUnit timeUnit) { //TODO: refactor to getDuration
        long value = timeUnit.convert(getDuration(), getTimeUnit());
        if (value == Long.MIN_VALUE || value == Long.MAX_VALUE) {
            throw new ArithmeticException("Overflow conversion of period " + getDuration() + " " + getTimeUnit() + " to " + timeUnit);
        }
        return value;
    }

    public long getPeriodInexact(TimeUnit timeUnit) { //TODO: refactor to getDuration
        return timeUnit.convert(getDuration(), getTimeUnit());
    }

    public long getDuration() {
        return duration;
    }

    public @Nonnull TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public ImmutableDate fromNow() {
        return new ImmutableDate(System.currentTimeMillis() + getPeriod());
    }

    public ImmutableDate beforeNow() {
        return new ImmutableDate(System.currentTimeMillis() - getPeriod());
    }

    public ImmutableDate from(Date date) {
        return new ImmutableDate(date.getTime() + getPeriod());
    }

    public ImmutableDate before(Date date) {
        return new ImmutableDate(date.getTime() - getPeriod());
    }

    public static TimePeriod parse(String stringValue) {
        if (stringValue == null) {
            return null;
        }
        stringValue = stringValue.toLowerCase().trim();
        if (stringValue.isEmpty()) {
            return null;
        }
        else if (stringValue.equals("0")) {
            return TimePeriod.ZERO;
        }
        Matcher matcher = PARSE_PATTERN.matcher(stringValue);
        long value = 0;
        while (matcher.find()) { // TODO: сделать проверку на непрерывность нахождения вхождений, иначе парсится будет всякая чушь
            long val = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            if (unit.startsWith("w")) {
                value += TimeUnit.DAYS.toMillis(val * 7);
            }
            else if (unit.startsWith("d")) {
                value += TimeUnit.DAYS.toMillis(val);
            }
            else if (unit.startsWith("h")) {
                value += TimeUnit.HOURS.toMillis(val);
            }
            else if (unit.startsWith("min") || unit.equals("m")) {
                value += TimeUnit.MINUTES.toMillis(val);
            }
            else if (unit.startsWith("s")) {
                value += TimeUnit.SECONDS.toMillis(val);
            }
            else if (unit.startsWith("ms") || unit.startsWith("mil") || unit.isEmpty()) {
                value += val;
            }
            else {
                throw new IllegalArgumentException("Unparseable time period: " + stringValue);
            }
        }
        return new TimePeriod(stringValue.charAt(0) == '-' ? -value : value, TimeUnit.MILLISECONDS);
    }

    @Override
    public String toString() {
        return toString(getPeriod());
    }

    protected static String toString(long period) {
        if (period == 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        if (period < 0) {
            sb.append("-");
            period = -period;
        }
        long weeks = period / TimeUnit.DAYS.toMillis(7);
        period %= TimeUnit.DAYS.toMillis(7);
        long days = period / TimeUnit.DAYS.toMillis(1);
        period %= TimeUnit.DAYS.toMillis(1);
        long hours = period / TimeUnit.HOURS.toMillis(1);
        period %= TimeUnit.HOURS.toMillis(1);
        long minutes = period / TimeUnit.MINUTES.toMillis(1);
        period %= TimeUnit.MINUTES.toMillis(1);
        long seconds = period / TimeUnit.SECONDS.toMillis(1);
        period %= TimeUnit.SECONDS.toMillis(1);
        long milliSeconds = period;

        if (weeks > 0) {
            sb.append(weeks).append("w ");
        }
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        if (seconds > 0) {
            sb.append(seconds).append("s ");
        }
        if (milliSeconds > 0) {
            sb.append(milliSeconds).append("ms ");
        }
        return sb.toString().trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TimePeriod)) {
            return false;
        }

        TimePeriod that = (TimePeriod) o;
        return getDuration() == that.getDuration() && getTimeUnit() == that.getTimeUnit();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDuration(), getTimeUnit());
    }

    @Override
    public int compareTo(@Nonnull TimePeriod other) {
        if (getTimeUnit() == other.getTimeUnit()) {
            return Long.compare(getDuration(), other.getDuration());
        }
        if (getTimeUnit().compareTo(other.getTimeUnit()) > 0) {
            return Long.compare(getPeriodInexact(other.getTimeUnit()), other.getDuration());
        }
        else {
            return Long.compare(getDuration(), other.getPeriodInexact(getTimeUnit()));
        }
    }

    public static TimePeriod between(long sinceMillis, long tillMillis) {
        return new TimePeriod(tillMillis - sinceMillis, TimeUnit.MILLISECONDS);
    }

    public static TimePeriod between(@Nonnull Date since, @Nonnull Date till) {
        return new TimePeriod(till.getTime() - since.getTime(), TimeUnit.MILLISECONDS);
    }

    public static TimePeriod tillNowFrom(@Nonnull Date date) {
        return between(date.getTime(), System.currentTimeMillis());
    }

    public static TimePeriod tillNowFrom(long utcTimeInMillis) {
        return between(utcTimeInMillis, System.currentTimeMillis());
    }

    public static TimePeriod sinceNowTo(@Nonnull Date date) {
        return between(System.currentTimeMillis(), date.getTime());
    }

    public static TimePeriod sinceNowTo(long utcTimeInMillis) {
        return between(System.currentTimeMillis(), utcTimeInMillis);
    }

    public static class Adapter extends XmlAdapter<String, TimePeriod> {
        @Override
        public TimePeriod unmarshal(String string) throws Exception {
            return TimePeriod.parse(string);
        }

        @Override
        public String marshal(TimePeriod timePeriod) throws Exception {
            return timePeriod == null ? null : timePeriod.toString();
        }
    }
}
