package org.libsmith.anvil.time;

import org.junit.Test;
import org.libsmith.anvil.AbstractTest;
import org.libsmith.anvil.jaxb.JAXBContextHelper;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.*;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 18.02.2016 3:00
 */
public class TimePeriodTest extends AbstractTest {

    @Test
    public void instantiationTest() {
        assertEquals(0, TimePeriod.of(0, TimeUnit.DAYS).compareTo(TimePeriod.ZERO));
        assertNotSame(TimePeriod.ZERO, TimePeriod.of(0, TimeUnit.DAYS));
        assertSame(TimePeriod.ZERO, TimePeriod.ofInexact(0, TimeUnit.DAYS));
        assertSame(TimePeriod.ZERO, TimePeriod.ofNanos(0));
        assertSame(TimePeriod.ZERO, TimePeriod.ofMicros(0));
        assertSame(TimePeriod.ZERO, TimePeriod.ofMillis(0));
        assertSame(TimePeriod.ZERO, TimePeriod.ofSeconds(0));
        assertSame(TimePeriod.ZERO, TimePeriod.ofMinutes(0));
        assertSame(TimePeriod.ZERO, TimePeriod.ofHours(0));
        assertSame(TimePeriod.ZERO, TimePeriod.ofDays(0));

        assertEquals("42ns", TimePeriod.ofNanos(42).toString());
        assertEquals("42us", TimePeriod.ofMicros(42).toString());
        assertEquals("42ms", TimePeriod.ofMillis(42).toString());
        assertEquals("42s", TimePeriod.ofSeconds(42).toString());
        assertEquals("42m", TimePeriod.ofMinutes(42).toString());
        assertEquals("1d 18h", TimePeriod.ofHours(42).toString());
        assertEquals("42d", TimePeriod.ofDays(42).toString());
    }

    @Test
    public void getDurationTest() {
        assertEquals(TimeUnit.HOURS.toMillis(1234), new TimePeriod(1234, TimeUnit.HOURS).getDurationMillis());
        assertEquals(TimeUnit.MILLISECONDS.toMillis(1234), new TimePeriod(1234, TimeUnit.MILLISECONDS).getDurationMillis());
    }

    @Test(expected = ArithmeticException.class)
    public void getDurationOverflowTest() {
        new TimePeriod(Long.MAX_VALUE - 100, TimeUnit.DAYS).getDuration(TimeUnit.MILLISECONDS);
    }

    @Test
    public void getTimeUnitTest() {
        assertEquals(TimeUnit.DAYS, new TimePeriod(1, TimeUnit.DAYS).getTimeUnit());
        assertEquals(TimeUnit.HOURS, new TimePeriod(1, TimeUnit.HOURS).getTimeUnit());
    }

    @Test
    public void equalsAndHashCodeTest() {
        //noinspection ObjectEqualsNull
        assertFalse(TimePeriod.ZERO.equals(null));
        assertEquals(TimePeriod.ZERO, TimePeriod.ZERO);
        assertNotEquals(new TimePeriod(0, TimeUnit.DAYS), new TimePeriod(0, TimeUnit.SECONDS));
        assertEquals(new TimePeriod(1, TimeUnit.DAYS), new TimePeriod(1, TimeUnit.DAYS));
        assertEquals(new TimePeriod(2, TimeUnit.HOURS), new TimePeriod(2, TimeUnit.HOURS));
        assertNotEquals(new TimePeriod(1, TimeUnit.SECONDS), new TimePeriod(1000, TimeUnit.MILLISECONDS));
        assertNotEquals(new TimePeriod(2, TimeUnit.DAYS), new TimePeriod(1, TimeUnit.DAYS));
        assertNotEquals(new TimePeriod(1, TimeUnit.HOURS), new TimePeriod(1, TimeUnit.DAYS));

        assertEquals(new TimePeriod(1, TimeUnit.DAYS).hashCode(), new TimePeriod(1, TimeUnit.DAYS).hashCode());
        assertNotEquals(new TimePeriod(2, TimeUnit.DAYS).hashCode(), new TimePeriod(1, TimeUnit.DAYS).hashCode());
        assertNotEquals(new TimePeriod(1, TimeUnit.MILLISECONDS).hashCode(), new TimePeriod(1, TimeUnit.DAYS).hashCode());

        assertEquals(TimeUnit.NANOSECONDS, TimePeriod.ZERO.getTimeUnit());
        assertEquals(TimeUnit.DAYS, TimePeriod.MIN_VALUE.getTimeUnit());
        assertEquals(TimeUnit.DAYS, TimePeriod.MAX_VALUE.getTimeUnit());

        assertNotEquals(TimePeriod.MAX_VALUE, TimePeriod.ZERO);
        assertNotEquals(TimePeriod.ZERO, TimePeriod.MAX_VALUE);
        assertNotEquals(TimePeriod.MIN_VALUE, TimePeriod.ZERO);
        assertNotEquals(TimePeriod.ZERO, TimePeriod.MIN_VALUE);
        assertNotEquals(TimePeriod.MAX_VALUE, TimePeriod.MIN_VALUE);
        assertEquals(TimePeriod.MAX_VALUE, TimePeriod.MAX_VALUE);
        assertEquals(TimePeriod.MIN_VALUE, TimePeriod.MIN_VALUE);
    }

    @Test
    public void compareToTest() {
        assertTrue(new TimePeriod(1, TimeUnit.DAYS).compareTo(new TimePeriod(1, TimeUnit.DAYS)) == 0);
        assertTrue(new TimePeriod(2, TimeUnit.DAYS).compareTo(new TimePeriod(1, TimeUnit.DAYS)) > 0);
        assertTrue(new TimePeriod(1, TimeUnit.DAYS).compareTo(new TimePeriod(2, TimeUnit.DAYS)) < 0);

        assertTrue(new TimePeriod(Long.MAX_VALUE, TimeUnit.DAYS).compareTo(new TimePeriod(Long.MIN_VALUE, TimeUnit.DAYS)) > 0);
        assertTrue(new TimePeriod(Long.MIN_VALUE, TimeUnit.DAYS).compareTo(new TimePeriod(Long.MAX_VALUE, TimeUnit.DAYS)) < 0);

        assertTrue(new TimePeriod(0, TimeUnit.DAYS).compareTo(new TimePeriod(0, TimeUnit.HOURS)) == 0);
        assertTrue(new TimePeriod(1, TimeUnit.DAYS).compareTo(new TimePeriod(24, TimeUnit.HOURS)) == 0);
        assertTrue(new TimePeriod(1, TimeUnit.DAYS).compareTo(new TimePeriod(23, TimeUnit.HOURS)) > 0);
        assertTrue(new TimePeriod(1, TimeUnit.DAYS).compareTo(new TimePeriod(25, TimeUnit.HOURS)) < 0);

        assertTrue(new TimePeriod(24, TimeUnit.HOURS).compareTo(new TimePeriod(1, TimeUnit.DAYS)) == 0);
        assertTrue(new TimePeriod(23, TimeUnit.HOURS).compareTo(new TimePeriod(1, TimeUnit.DAYS)) < 0);
        assertTrue(new TimePeriod(25, TimeUnit.HOURS).compareTo(new TimePeriod(1, TimeUnit.DAYS)) > 0);

        assertEquals(TimeUnit.NANOSECONDS, TimePeriod.ZERO.getTimeUnit());
        assertEquals(TimeUnit.DAYS, TimePeriod.MIN_VALUE.getTimeUnit());
        assertEquals(TimeUnit.DAYS, TimePeriod.MAX_VALUE.getTimeUnit());

        assertTrue(TimePeriod.MAX_VALUE.compareTo(TimePeriod.ZERO) > 0);
        assertTrue(TimePeriod.MIN_VALUE.compareTo(TimePeriod.ZERO) < 0);
        assertTrue(TimePeriod.ZERO.compareTo(TimePeriod.ZERO) == 0);
        assertTrue(TimePeriod.MAX_VALUE.compareTo(TimePeriod.MAX_VALUE) == 0);
        assertTrue(TimePeriod.MIN_VALUE.compareTo(TimePeriod.MIN_VALUE) == 0);
    }

    @Test
    public void fromTest() {
        assertEquals(TimeUnit.DAYS.toMillis(1) + 1234, TimePeriod.ofDays(1).from(new Date(1234)).getTime());
    }

    @Test
    public void beforeTest() {
        assertEquals(1234 - TimeUnit.DAYS.toMillis(1), TimePeriod.ofDays(1).before(new Date(1234)).getTime());
    }

    @Test
    public void fromNow() {
        assertTrue(System.currentTimeMillis() - TimePeriod.ofDays(1).fromNow().getTime() + TimeUnit.DAYS.toMillis(1) < 100);
    }

    @Test
    public void beforeNow() {
        assertTrue(System.currentTimeMillis() - TimePeriod.ofDays(1).beforeNow().getTime() - TimeUnit.DAYS.toMillis(1) < 100);
    }

    @Test
    public void parseTest() {
        assertEquals(TimeUnit.DAYS.toMillis(1), TimePeriod.parse("1d").getDurationMillis());
        assertEquals(TimeUnit.DAYS.toNanos(4) +
                     TimeUnit.HOURS.toNanos(3) +
                     TimeUnit.MINUTES.toNanos(2) +
                     TimeUnit.SECONDS.toNanos(1) +
                     TimeUnit.MILLISECONDS.toNanos(432) +
                     TimeUnit.MICROSECONDS.toNanos(123) +
                     TimeUnit.NANOSECONDS.toNanos(445)
                , TimePeriod.parse("4d 3h 2m 1s 432ms 123us 445ns").getDuration(TimeUnit.NANOSECONDS));

        assertEquals(TimeUnit.DAYS.toNanos(4) +
                     TimeUnit.HOURS.toNanos(3) +
                     TimeUnit.MINUTES.toNanos(2) +
                     TimeUnit.SECONDS.toNanos(1) +
                     TimeUnit.MILLISECONDS.toNanos(432) +
                     TimeUnit.MICROSECONDS.toNanos(123) +
                     TimeUnit.NANOSECONDS.toNanos(445)
                , TimePeriod.parse("4 days, 3 hour; 2 minute + 1 second & 432 millisecond / 123 microsecond | 445 nanosecond")
                            .getDuration(TimeUnit.NANOSECONDS));

        assertEquals(-(TimeUnit.DAYS.toMillis(7) +
                       TimeUnit.MINUTES.toMillis(6) +
                       TimeUnit.SECONDS.toMillis(5))
                , TimePeriod.parse("-7d 6m 5s").getDurationMillis());

        assertEquals(1234, TimePeriod.parse("1234", TimeUnit.MINUTES).getDuration(TimeUnit.MINUTES));

        assertEquals(0, TimePeriod.parse("0").getDuration());

        assertEquals(TimeUnit.MINUTES, TimePeriod.parse("3d 3m").getTimeUnit());
        assertEquals(42, TimePeriod.parse("42s").getDuration());

        assertNull(TimePeriod.parse(""));
        assertNull(TimePeriod.parse(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseErrorTimePeriodTest() {
        TimePeriod.parse("1d 2m 3x 4s");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseErrorTimePeriodGarbageTest() {
        TimePeriod.parse("1d hrenoten vsakaya 2m");
    }

    @Test
    public void toStringTest() {
        assertEquals("9d 3h 4m 5s 6ms", new TimePeriod(
                             TimeUnit.DAYS.toMillis(7) +
                             TimeUnit.DAYS.toMillis(2) +
                             TimeUnit.HOURS.toMillis(3) +
                             TimeUnit.MINUTES.toMillis(4) +
                             TimeUnit.SECONDS.toMillis(5) +
                             TimeUnit.MILLISECONDS.toMillis(6), TimeUnit.MILLISECONDS).toString()
                    );
        assertEquals("-9d 3h 4m 5s 6ms", new TimePeriod(
                             -(TimeUnit.DAYS.toMillis(7) +
                               TimeUnit.DAYS.toMillis(2) +
                               TimeUnit.HOURS.toMillis(3) +
                               TimeUnit.MINUTES.toMillis(4) +
                               TimeUnit.SECONDS.toMillis(5) +
                               TimeUnit.MILLISECONDS.toMillis(6)), TimeUnit.MILLISECONDS).toString()
                    );

        assertEquals("0", TimePeriod.ZERO.toString());
        assertEquals("0h", TimePeriod.ZERO.toString(TimeUnit.HOURS, true));
        assertEquals("42ms", TimePeriod.ofNanos(42424242).toString(TimeUnit.MILLISECONDS));
    }

    @Test
    public void stressToStringParseTest() {
        Random random = new Random(42);
        for (int i = 0; i < 100_000; i++) {
            TimePeriod timePeriod = TimePeriod.ofNanos(random.nextLong());
            assertEquals(timePeriod.toString(), TimePeriod.parse(timePeriod.toString()).toString());
        }
    }

    @Test
    public void sinceNowToTest() {
        Date date = new Date(System.currentTimeMillis() + 10000);
        TimePeriod timePeriod = TimePeriod.sinceNowTo(date);
        assertTrue("Actual " + timePeriod.getDuration(), timePeriod.getDuration() <= 10000);
        assertTrue("Actual " + timePeriod.getDuration(), timePeriod.getDuration() > 9950);

        TimePeriod millis = TimePeriod.sinceNowToMillis(System.currentTimeMillis() + 10000);
        assertTrue("Actual " + millis.getDuration(), millis.getDuration() <= 10000);
        assertTrue("Actual " + millis.getDuration(), millis.getDuration() > 9950);

        TimePeriod nanos = TimePeriod.sinceNowToNanos(System.nanoTime() + 10000000);
        assertTrue("Actual " + nanos.getDuration(), nanos.getDuration() <= 10000000);
        assertTrue("Actual " + nanos.getDuration(), nanos.getDuration() > 9950000);
    }

    @Test
    public void tillNowFromTest() {
        Date date = new Date(System.currentTimeMillis() - 10000);
        TimePeriod timePeriod = TimePeriod.tillNowFrom(date);
        assertTrue("Actual " + timePeriod.getDuration(), timePeriod.getDuration() >= 10000);
        assertTrue("Actual " + timePeriod.getDuration(), timePeriod.getDuration() < 10050);

        TimePeriod millis = TimePeriod.tillNowFromMillis(System.currentTimeMillis() - 10000);
        assertTrue("Actual " + millis.getDuration(), millis.getDuration() >= 10000);
        assertTrue("Actual " + millis.getDuration(), millis.getDuration() < 10050);

        TimePeriod nanos = TimePeriod.tillNowFromNanos(System.nanoTime() - 10000000);
        assertTrue("Actual " + nanos.getDuration(), nanos.getDuration() >= 10000000);
        assertTrue("Actual " + nanos.getDuration(), nanos.getDuration() < 10050000);
    }

    @Test
    public void between() {
        assertEquals(2, TimePeriod.between(new Date(1000), new Date(3000)).getDuration(TimeUnit.SECONDS));
        assertEquals(4, TimePeriod.between(6000, 10000, TimeUnit.MILLISECONDS).getDuration(TimeUnit.SECONDS));
    }

    @Test
    public void betweenMillis() {
        assertEquals(4, TimePeriod.betweenMillis(6000, 10000).getDuration(TimeUnit.SECONDS));
    }

    @Test(timeout = 1000)
    public void sleepTest() throws InterruptedException {
        long now = System.nanoTime();
        TimePeriod.ofMillis(750).sleep();
        assertTrue(System.nanoTime() >= now + TimeUnit.MILLISECONDS.toNanos(750));
    }

    @Test
    public void addTest() {
        assertEquals("1h 25m 143ms",
                     TimePeriod.parse("3m 143ms").add(TimePeriod.parse("1h 22m")).toString());
        assertEquals("4h 2s",
                     new TimePeriod(4, TimeUnit.HOURS).add(new TimePeriod(2, TimeUnit.SECONDS)).toString());
        assertEquals("2h 4s",
                     new TimePeriod(4, TimeUnit.SECONDS).add(new TimePeriod(2, TimeUnit.HOURS)).toString());
        assertSame(TimePeriod.ZERO, TimePeriod.ZERO.add(0, TimeUnit.SECONDS));
    }

    @Test
    public void addRandomTest() {
        TimePeriod timePeriod = TimePeriod.parse("0");
        for (int i = 200; i < 10000; i++) {
            long val = timePeriod.addRandom(100, i, TimeUnit.HOURS).getDuration(TimeUnit.HOURS);
            assertTrue(val >= 100);
            assertTrue(val < 10000);
        }

        TimePeriod tenMinutes = new TimePeriod(10, TimeUnit.MINUTES);
        TimePeriod tenHours = new TimePeriod(10, TimeUnit.HOURS);
        for (int i = 0; i < 10000; i++) {
            TimePeriod val = timePeriod.addRandom(tenMinutes, tenHours);
            long ms = val.getDuration(TimeUnit.MILLISECONDS);
            assertTrue("Must be >= 10m, actual: " + val, ms >= TimeUnit.MINUTES.toMillis(10));
            assertTrue("Must be < 10h, actual: " + val, ms < TimeUnit.HOURS.toMillis(10));
        }
    }

    @Test(expected = ArithmeticException.class)
    public void addOverflowTestAtConversion() {
        TimePeriod.ofDays(TimeUnit.MILLISECONDS.toDays(Long.MIN_VALUE) - 1).add(TimePeriod.parse("1d 1ms"));
    }

    @Test(expected = ArithmeticException.class)
    public void addOverflowTestAtAddition() {
        TimePeriod.ofDays(TimeUnit.MILLISECONDS.toDays(Long.MAX_VALUE)).add(TimePeriod.parse("1d 1ms"));
    }

    @Test
    public void sumTest() {
        assertEquals("1d 2h 3m", TimePeriod.sum(TimePeriod.parse("1d"),
                                                TimePeriod.parse("2h"),
                                                TimePeriod.parse("3m")).toString());
        assertSame(TimePeriod.ZERO, TimePeriod.sum());
    }

    @Test
    public void subTest() {
        assertEquals("41m 20ms",
                     TimePeriod.parse("1h 3m 24ms").sub(TimePeriod.parse("22m 4ms")).toString());
        assertEquals("3h 59m 58s",
                     new TimePeriod(4, TimeUnit.HOURS).sub(new TimePeriod(2, TimeUnit.SECONDS)).toString());
        assertEquals("-1h 59m 56s",
                     new TimePeriod(4, TimeUnit.SECONDS).sub(new TimePeriod(2, TimeUnit.HOURS)).toString());
        assertSame(TimePeriod.ZERO, TimePeriod.ZERO.sub(0, TimeUnit.SECONDS));
    }

    @Test(expected = ArithmeticException.class)
    public void subOverflowTestAtConversion() {
        new TimePeriod(TimeUnit.MILLISECONDS.toDays(Long.MAX_VALUE) + 1, TimeUnit.DAYS).sub(TimePeriod.parse("1d 1ms"));
    }

    @Test(expected = ArithmeticException.class)
    public void subOverflowTestAtAddition() {
        new TimePeriod(TimeUnit.MILLISECONDS.toDays(Long.MIN_VALUE), TimeUnit.DAYS).sub(TimePeriod.parse("1d 1ms"));
    }

    @Test
    public void mulTest() {
        assertEquals("4h 2m", TimePeriod.parse("2h 1m").mul(2).toString());
        assertSame(TimePeriod.ZERO, TimePeriod.ZERO.mul(3));
        assertSame(TimePeriod.MAX_VALUE, TimePeriod.MAX_VALUE.mul(1));
        assertSame(TimePeriod.ZERO, TimePeriod.ZERO.mul(0));
    }

    @Test(expected = ArithmeticException.class)
    public void mulOverflowTest() {
        TimePeriod.parse("2h 21m").mul(Long.MAX_VALUE);
    }

    @Test
    public void divTest() {
        assertEquals("2h 1m", TimePeriod.parse("4h 2m").div(2).toString());
        assertSame(TimePeriod.MAX_VALUE, TimePeriod.MAX_VALUE.div(1));
        assertSame(TimePeriod.ZERO, TimePeriod.ZERO.div(100));
    }

    @Test(expected = ArithmeticException.class)
    public void divZeroByZeroTest() {
        TimePeriod.ZERO.div(0);
    }

    @Test
    public void normalizeTest() {

        assertSame(TimePeriod.ZERO, new TimePeriod(0, TimeUnit.HOURS).normalize());

        {
            TimePeriod oneSecond = new TimePeriod(TimeUnit.SECONDS.toMillis(1), TimeUnit.MILLISECONDS).normalize();
            assertEquals(TimeUnit.SECONDS, oneSecond.getTimeUnit());
            assertEquals(1, oneSecond.getDuration());
        }

        {
            TimePeriod elevenHours = new TimePeriod(TimeUnit.HOURS.toSeconds(11), TimeUnit.SECONDS).normalize();
            assertEquals(TimeUnit.HOURS, elevenHours.getTimeUnit());
            assertEquals(11, elevenHours.getDuration());
        }

        {
            TimePeriod twentyFourDays = new TimePeriod(TimeUnit.DAYS.toSeconds(24), TimeUnit.SECONDS).normalize();
            assertEquals(TimeUnit.DAYS, twentyFourDays.getTimeUnit());
            assertEquals(24, twentyFourDays.getDuration());
        }

        {
            TimePeriod threeMinutes = new TimePeriod(3, TimeUnit.MINUTES).normalize();
            assertEquals(TimeUnit.MINUTES, threeMinutes.getTimeUnit());
            assertEquals(3, threeMinutes.getDuration());
        }
    }

    @Test
    @SuppressWarnings("RedundantCast")
    public void constructFromJSR310Test() {

        {
            TimePeriod subj = TimePeriod.parse("1d 42m 34s 4ms 25ns");
            assertEquals(subj, TimePeriod.of(Duration.ofNanos(subj.getDuration(TimeUnit.NANOSECONDS))));
            assertSame(subj, TimePeriod.of((TemporalAmount) subj));
        }

        {
            TemporalAmount amount = new TemporalAmountImpl(2, new TemporalUnitImpl(Duration.ofMillis(2002)));
            assertEquals("4s 4ms", TimePeriod.of(amount).toString());
        }
    }

    @Test
    public void temporalOperationsOnJSR310() {
        LocalTime lt = LocalTime.of(3, 14, 42);
        TimePeriod tp = TimePeriod.parse("5m 5s");
        assertEquals("03:19:47", tp.addTo(lt).toString());
        assertEquals("03:09:37", tp.subtractFrom(lt).toString());
    }

    @Test
    public void unitsForJSR310Test() {
        assertEquals(Collections.singletonList(ChronoUnit.NANOS), TimePeriod.parse("1ns").getUnits());
        assertEquals(Collections.singletonList(ChronoUnit.MICROS), TimePeriod.parse("1us").getUnits());
        assertEquals(Collections.singletonList(ChronoUnit.MILLIS), TimePeriod.parse("1ms").getUnits());
        assertEquals(Collections.singletonList(ChronoUnit.SECONDS), TimePeriod.parse("1s").getUnits());
        assertEquals(Collections.singletonList(ChronoUnit.MINUTES), TimePeriod.parse("1m").getUnits());
        assertEquals(Collections.singletonList(ChronoUnit.HOURS), TimePeriod.parse("1h").getUnits());
        assertEquals(Collections.singletonList(ChronoUnit.DAYS), TimePeriod.parse("1d").getUnits());

        assertEquals(1, TimePeriod.parse("1ns").get(ChronoUnit.NANOS));
        assertEquals(1, TimePeriod.parse("1us").get(ChronoUnit.MICROS));
        assertEquals(1, TimePeriod.parse("1ms").get(ChronoUnit.MILLIS));
        assertEquals(1, TimePeriod.parse("1s").get(ChronoUnit.SECONDS));
        assertEquals(1, TimePeriod.parse("1m").get(ChronoUnit.MINUTES));
        assertEquals(1, TimePeriod.parse("1h").get(ChronoUnit.HOURS));
        assertEquals(1, TimePeriod.parse("1d").get(ChronoUnit.DAYS));
    }

    @Test(expected = UnsupportedTemporalTypeException.class)
    public void unsupportedGetTypeException() {
        TimePeriod.parse("1m").get(ChronoUnit.HOURS);
    }

    @Test
    public void marshallTest() throws JAXBException {

        JAXBContextHelper context = JAXBContextHelper.builder().with(SomeObject.class).build();
        SomeObject someObject = new SomeObject();
        someObject.setTimePeriod(new TimePeriod(12423523536714L, TimeUnit.NANOSECONDS));
        String marshall = context.marshall(someObject);
        SomeObject unmarshalledObject = context.unmarshal(marshall);
        assertEquals(someObject.getTimePeriod(), unmarshalledObject.getTimePeriod());
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class SomeObject {

        @XmlJavaTypeAdapter(TimePeriod.Adapter.class)
        private TimePeriod timePeriod;

        public TimePeriod getTimePeriod() {
            return timePeriod;
        }

        public void setTimePeriod(TimePeriod timePeriod) {
            this.timePeriod = timePeriod;
        }
    }

    private class TemporalUnitImpl implements TemporalUnit {

        private final Duration duration;

        private TemporalUnitImpl(Duration duration) {
            this.duration = duration;
        }

        @Override
        public Duration getDuration() {
            return duration;
        }

        @Override
        public boolean isDurationEstimated() {
            return false;
        }

        @Override
        public boolean isDateBased() {
            return false;
        }

        @Override
        public boolean isTimeBased() {
            return true;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <R extends Temporal> R addTo(R temporal, long amount) {
            return (R) temporal.plus(amount, this);
        }

        @Override
        public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
            return temporal1Inclusive.until(temporal2Exclusive, this);
        }
    }

    private class TemporalAmountImpl implements TemporalAmount {

        private final long amount;
        private final TemporalUnit temporalUnit;

        private TemporalAmountImpl(long amount, TemporalUnit temporalUnit) {
            this.amount = amount;
            this.temporalUnit = temporalUnit;
        }

        @Override
        public long get(TemporalUnit unit) {
            if (unit != temporalUnit) {
                throw new RuntimeException();
            }
            return amount;
        }

        @Override
        public List<TemporalUnit> getUnits() {
            return Collections.singletonList(temporalUnit);
        }

        @Override
        public Temporal addTo(Temporal temporal) {
            return temporal.plus(amount, temporalUnit);
        }

        @Override
        public Temporal subtractFrom(Temporal temporal) {
            return temporal.minus(amount, temporalUnit);
        }
    }
}
