package org.libsmith.anvil.time;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.Serializable;
import java.time.Duration;
import java.time.temporal.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 28.10.2015 16:26
 */
@SuppressWarnings("WeakerAccess")
public class TimePeriod implements Serializable, Comparable<TimePeriod>, TemporalAmount {

    private static final long serialVersionUID = -4819273455430547917L;

    public static final TimePeriod ZERO = new TimePeriod(0, TimeUnit.NANOSECONDS);
    public static final TimePeriod MIN_VALUE = new TimePeriod(Long.MIN_VALUE, TimeUnit.DAYS);
    public static final TimePeriod MAX_VALUE = new TimePeriod(Long.MAX_VALUE, TimeUnit.DAYS);

    private static Pattern PARSE_PATTERN = Pattern.compile("\\s*(\\d+)\\s*(\\p{Alpha}*)\\s*\\p{Punct}*");

    private final long duration;
    private final TimeUnit timeUnit;

    protected TimePeriod(long duration, @Nonnull TimeUnit timeUnit) {
        this.duration = duration;
        this.timeUnit = timeUnit;
    }

    public static TimePeriod of(long duration, @Nonnull TimeUnit timeUnit) {
        return new TimePeriod(duration, timeUnit);
    }

    public static TimePeriod ofInexact(long duration, @Nonnull TimeUnit timeUnit) {
        if (duration == 0) {
            return ZERO;
        }
        return new TimePeriod(duration, timeUnit);
    }

    public static TimePeriod ofNanos(long nanoseconds) {
        return ofInexact(nanoseconds, TimeUnit.NANOSECONDS);
    }

    public static TimePeriod ofMicros(long microseconds) {
        return ofInexact(microseconds, TimeUnit.MICROSECONDS);
    }

    public static TimePeriod ofMillis(long milliseconds) {
        return ofInexact(milliseconds, TimeUnit.MILLISECONDS);
    }

    public static TimePeriod ofSeconds(long seconds) {
        return ofInexact(seconds, TimeUnit.SECONDS);
    }

    public static TimePeriod ofMinutes(long minutes) {
        return ofInexact(minutes, TimeUnit.MINUTES);
    }

    public static TimePeriod ofHours(long hours) {
        return ofInexact(hours, TimeUnit.HOURS);
    }

    public static TimePeriod ofDays(long days) {
        return ofInexact(days, TimeUnit.DAYS);
    }

    public @Nonnull TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public long getDuration() {
        return duration;
    }

    public long getDuration(TimeUnit timeUnit) {
        return convertExact(getDuration(), getTimeUnit(), timeUnit);
    }

    public long getDurationInexact(TimeUnit timeUnit) {
        return timeUnit.convert(getDuration(), getTimeUnit());
    }

    public long getDurationMillis() {
        return getDuration(TimeUnit.MILLISECONDS);
    }

    public void sleep() throws InterruptedException {
        getTimeUnit().sleep(getDuration());
    }

    public TimePeriod add(TimePeriod timePeriod) {
        return add(timePeriod.getDuration(), timePeriod.getTimeUnit());
    }

    public TimePeriod add(long duration, TimeUnit timeUnit) {
        if (duration == 0) {
            return this;
        }
        TimeUnit min = min(this.getTimeUnit(), timeUnit);
        try {
            return new TimePeriod(Math.addExact(this.getDuration(min), convertExact(duration, timeUnit, min)), min);
        }
        catch (ArithmeticException ex) {
            throw new ArithmeticException("Overflow at addition to '" + this + "' value '" +
                                          new TimePeriod(duration, timeUnit) + "'");
        }
    }

    public TimePeriod addRandom(long origin, long bound, TimeUnit timeUnit) {
        return add(ThreadLocalRandom.current().nextLong(origin, bound), timeUnit);
    }

    public TimePeriod addRandom(TimePeriod origin, TimePeriod bound) {
        TimeUnit min = min(origin.getTimeUnit(), bound.getTimeUnit());
        return addRandom(origin.getDuration(min), bound.getDuration(min), min);
    }

    public TimePeriod sub(TimePeriod timePeriod) {
        return sub(timePeriod.getDuration(), timePeriod.getTimeUnit());
    }

    public TimePeriod sub(long duration, TimeUnit timeUnit) {
        if (duration == 0) {
            return this;
        }
        TimeUnit min = min(timeUnit, this.getTimeUnit());
        try {
            return new TimePeriod(Math.subtractExact(this.getDuration(min), convertExact(duration, timeUnit, min)), min);
        }
        catch (ArithmeticException ex) {
            throw new ArithmeticException("Overflow at subtraction from '" + this + "' value '" +
                                          new TimePeriod(duration, timeUnit) + "'");
        }
    }

    public TimePeriod mul(long val) {
        if (val == 0) {
            return ZERO;
        }
        else if (val == 1 || getDuration() == 0) {
            return this;
        }
        try {
            return new TimePeriod(Math.multiplyExact(getDuration(), val), getTimeUnit());
        }
        catch (ArithmeticException ex) {
            throw new ArithmeticException("Overflow at multiply value '" + this + "' by '" + val + "'");
        }
    }

    public TimePeriod div(long val) {
        if (val == 1 || getDuration() == 0 && val != 0) {
            return this;
        }
        return new TimePeriod(getDuration() / val, getTimeUnit());
    }

    public ImmutableDate fromNow() {
        return new ImmutableDate(System.currentTimeMillis() + getDurationMillis());
    }

    public ImmutableDate beforeNow() {
        return new ImmutableDate(System.currentTimeMillis() - getDurationMillis());
    }

    public ImmutableDate from(Date date) {
        return new ImmutableDate(date.getTime() + getDurationMillis());
    }

    public ImmutableDate before(Date date) {
        return new ImmutableDate(date.getTime() - getDurationMillis());
    }

    //<editor-fold desc="Parse">
    public static TimePeriod parse(String stringValue) {
        return parse(stringValue, null);
    }

    public static TimePeriod parse(String stringValue, TimeUnit defaultTimeUnit) {
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

        Matcher matcher = PARSE_PATTERN.matcher(stringValue.trim());
        MutableTimePeriod value = new MutableTimePeriod();
        boolean negate = stringValue.charAt(0) == '-';
        int lastPos = negate ? 1 : 0;

        while (matcher.find()) {
            if (lastPos != matcher.start()) {
                throw new IllegalArgumentException(
                        "Unparseable time period: " + stringValue +
                        "; can't parse token '" + stringValue.substring(lastPos, matcher.start()) + "'");
            }
            lastPos = matcher.end();
            long duration = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            if (defaultTimeUnit != null && unit.isEmpty()) {
                value.add(duration, defaultTimeUnit);
            }
            else if (unit.startsWith("d")) {
                value.add(duration, TimeUnit.DAYS);
            }
            else if (unit.startsWith("h")) {
                value.add(duration, TimeUnit.HOURS);
            }
            else if (unit.equals("m") || unit.startsWith("min")) {
                value.add(duration, TimeUnit.MINUTES);
            }
            else if (unit.startsWith("s")) {
                value.add(duration, TimeUnit.SECONDS);
            }
            else if (unit.equals("ms") || unit.startsWith("mil")) {
                value.add(duration, TimeUnit.MILLISECONDS);
            }
            else if (unit.equals("μs") || unit.equals("us") || unit.startsWith("mic")) {
                value.add(duration, TimeUnit.MICROSECONDS);
            }
            else if (unit.startsWith("n")) {
                value.add(duration, TimeUnit.NANOSECONDS);
            }
            else {
                throw new IllegalArgumentException("Unparseable time period: " + stringValue +
                                                   "; can't parse token '" + matcher.group(0) + "'");
            }
        }
        if (negate) {
            value.negateExact();
        }
        return value.toTimePeriod();
    }
    //</editor-fold>

    //<editor-fold desc="ToString">
    @Override
    public String toString() {
        return toString(TimeUnit.NANOSECONDS, false);
    }

    public String toString(TimeUnit minUnit) {
        return toString(timeUnit, false);
    }

    public String toString(TimeUnit minUnit, boolean abbreviateZero) {
        minUnit = getTimeUnit().ordinal() > minUnit.ordinal() ? getTimeUnit() : minUnit;
        long duration = minUnit.convert(getDuration(), getTimeUnit());
        StringBuilder sb = new StringBuilder();
        if (duration == 0) {
            return abbreviateZero ? "0" + abbreviate(minUnit) : "0";
        }
        else if (duration < 0) {
            sb.append("-");
            duration = Math.negateExact(duration);
        }
        for (TimeUnit unit : new TimeUnit[] { TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS,
                                              TimeUnit.MILLISECONDS, TimeUnit.MICROSECONDS, TimeUnit.NANOSECONDS}) {
            duration -= buildUnitAndGetRemainder(unit, duration, minUnit, sb);
        }
        return sb.toString();
    }

    private static long buildUnitAndGetRemainder(TimeUnit targetUnit, long duration, TimeUnit sourceUnit,
                                                 StringBuilder sb) {
        long value = targetUnit.convert(duration, sourceUnit);
        if (value > 0) {
            if (sb.length() > 1) {
                sb.append(" ");
            }
            sb.append(value);
            sb.append(abbreviate(targetUnit));
        }
        return sourceUnit.convert(value, targetUnit);
    }

    protected static String abbreviate(TimeUnit timeUnit) {
        switch (timeUnit) {
            case DAYS:         return "d";
            case HOURS:        return "h";
            case MINUTES:      return "m";
            case SECONDS:      return "s";
            case MILLISECONDS: return "ms";
            case MICROSECONDS: return "us";
            case NANOSECONDS:  return "ns";
            default:           throw new RuntimeException();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Equals / HashCode">
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
    //</editor-fold>

    //<editor-fold desc="CompareTo">
    @Override
    public int compareTo(@Nonnull TimePeriod other) {
        if (getTimeUnit() == other.getTimeUnit()) {
            return Long.compare(getDuration(), other.getDuration());
        }
        if (getTimeUnit().compareTo(other.getTimeUnit()) > 0) {
            return Long.compare(getDurationInexact(other.getTimeUnit()), other.getDuration());
        }
        else {
            return Long.compare(getDuration(), other.getDurationInexact(getTimeUnit()));
        }
    }
    //</editor-fold>

    public static TimePeriod betweenMillis(long since, long till) {
        return new TimePeriod(till - since, TimeUnit.MILLISECONDS);
    }

    public static TimePeriod between(long since, long till, TimeUnit timeUnit) {
        return new TimePeriod(till - since, timeUnit);
    }

    public static TimePeriod between(@Nonnull Date since, @Nonnull Date till) {
        return new TimePeriod(till.getTime() - since.getTime(), TimeUnit.MILLISECONDS);
    }

    public static TimePeriod tillNowFrom(@Nonnull Date date) {
        return between(date.getTime(), System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    public static TimePeriod tillNowFromMillis(long utcTimeInMillis) {
        return between(utcTimeInMillis, System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    public static TimePeriod tillNowFromNanos(long jvmNanoTime) {
        return between(jvmNanoTime, System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    public static TimePeriod sinceNowTo(@Nonnull Date date) {
        return between(System.currentTimeMillis(), date.getTime(), TimeUnit.MILLISECONDS);
    }

    public static TimePeriod sinceNowToMillis(long utcTimeInMillis) {
        return between(System.currentTimeMillis(), utcTimeInMillis, TimeUnit.MILLISECONDS);
    }

    public static TimePeriod sinceNowToNanos(long jvmNanoTime) {
        return between(System.nanoTime(), jvmNanoTime, TimeUnit.NANOSECONDS);
    }

    public static TimePeriod sum(TimePeriod ... timePeriods) {
        return sum(Arrays.asList(timePeriods));
    }

    public static TimePeriod sum(Iterable<? extends TimePeriod> timePeriods) {
        long value = 0;
        TimeUnit resolution = TimeUnit.DAYS;
        for (TimePeriod timePeriod : timePeriods) {
            TimeUnit min = min(resolution, timePeriod.getTimeUnit());
            value = Math.addExact(timePeriod.getDuration(min), convertExact(value, resolution, min));
            resolution = min;
        }
        return value == 0 ? TimePeriod.ZERO : new TimePeriod(value, resolution);
    }

    public TimePeriod normalize() {
        long duration = getDuration();
        TimeUnit unit = getTimeUnit();
        if (duration == 0) {
            return TimePeriod.ZERO;
        }
        TimeUnit[] units = TimeUnit.values();
        while (unit.ordinal() < units.length - 1) {
            TimeUnit subjectUnit = units[unit.ordinal() + 1];
            long subjectDuration = subjectUnit.convert(duration, unit);
            if (unit.convert(subjectDuration, subjectUnit) == duration) {
                duration = subjectDuration;
                unit = subjectUnit;
            }
            else {
                break;
            }
        }
        if (unit == getTimeUnit()) {
            return this;
        }
        return new TimePeriod(duration, unit);
    }

    private static long convertExact(long sourceDuration, TimeUnit sourceTimeUnit, TimeUnit destTimeUnit) {
        if (sourceTimeUnit == destTimeUnit || sourceDuration == 0) {
            return sourceDuration;
        }
        long value = destTimeUnit.convert(sourceDuration, sourceTimeUnit);
        if (value == Long.MIN_VALUE || value == Long.MAX_VALUE) {
            throw new ArithmeticException("Overflow conversion of period " + sourceDuration + " " + sourceTimeUnit +
                                          " to " + destTimeUnit);
        }
        return value;
    }

    private static TimeUnit min(TimeUnit timeUnitA, TimeUnit timeUnitB) {
        return timeUnitA.compareTo(timeUnitB) < 0 ? timeUnitA : timeUnitB;
    }

    private static class MutableTimePeriod {

        private long duration;
        private TimeUnit timeUnit = TimeUnit.DAYS;

        public void add(long duration, TimeUnit timeUnit) {
            TimeUnit min = min(this.timeUnit, timeUnit);
            this.duration = min.convert(this.duration, this.timeUnit) + min.convert(duration, timeUnit);
            this.timeUnit = min;
        }

        public void negateExact() {
            duration = Math.negateExact(duration);
        }

        public TimePeriod toTimePeriod() {
            return new TimePeriod(duration, timeUnit);
        }
    }

    //<editor-fold desc="JSR310 Glue">
    @Override
    public long get(TemporalUnit unit) {
        if (convertToTemporalUnit(timeUnit).equals(unit)) {
            return duration;
        }
        throw new UnsupportedTemporalTypeException("Unsupported type " + unit);
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return Collections.singletonList(convertToTemporalUnit(timeUnit));
    }

    private static TemporalUnit convertToTemporalUnit(TimeUnit timeUnit) {
        switch (timeUnit) {
            case NANOSECONDS:   return ChronoUnit.NANOS;
            case MICROSECONDS:  return ChronoUnit.MICROS;
            case MILLISECONDS:  return ChronoUnit.MILLIS;
            case SECONDS:       return ChronoUnit.SECONDS;
            case MINUTES:       return ChronoUnit.MINUTES;
            case HOURS:         return ChronoUnit.HOURS;
            case DAYS:          return ChronoUnit.DAYS;
            default:            throw new RuntimeException(timeUnit.toString());
        }
    }

    public static TimePeriod of(TemporalAmount temporalAmount) {
        if (temporalAmount instanceof TimePeriod) {
            return (TimePeriod) temporalAmount;
        }
        Duration asDuration = Duration.from(temporalAmount);
        return TimePeriod.ofSeconds(asDuration.getSeconds())
                         .add(TimePeriod.ofNanos(asDuration.getNano()).normalize())
                         .normalize();
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        return temporal.plus(duration, convertToTemporalUnit(timeUnit));
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        return temporal.minus(duration, convertToTemporalUnit(timeUnit));
    }
    //</editor-fold>

    //<editor-fold desc="JAXB Glue">
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
    //</editor-fold>
}
