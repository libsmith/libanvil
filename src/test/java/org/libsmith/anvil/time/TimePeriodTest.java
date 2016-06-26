package org.libsmith.anvil.time;

import org.junit.Test;
import org.libsmith.anvil.AbstractTest;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @author Dmitriy Balakin <balakin@loyaltyplant.com>
 * @created 18.02.2016 3:00
 */
public class TimePeriodTest extends AbstractTest {

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
        assertEquals(TimeUnit.DAYS.toMillis(1) + 1234, new TimePeriod(1, TimeUnit.DAYS).from(new Date(1234)).getTime());
    }

    @Test
    public void beforeTest() {
        assertEquals(1234 - TimeUnit.DAYS.toMillis(1), new TimePeriod(1, TimeUnit.DAYS).before(new Date(1234))
                                                                                       .getTime());
    }

    @Test
    public void fromNow() {
        assertTrue(System.currentTimeMillis() - new TimePeriod(1, TimeUnit.DAYS).fromNow()
                                                                                .getTime() + TimeUnit.DAYS.toMillis(1) < 100);
    }

    @Test
    public void beforeNow() {
        assertTrue(System.currentTimeMillis() - new TimePeriod(1, TimeUnit.DAYS).beforeNow()
                                                                                .getTime() - TimeUnit.DAYS.toMillis(1) < 100);
    }


    @Test
    public void parseTest() {
        assertEquals(TimeUnit.DAYS.toMillis(1), TimePeriod.parse("1d").getDuration());
        assertEquals(TimeUnit.DAYS.toMillis(7), TimePeriod.parse("1w").getDuration());
        assertEquals(TimeUnit.DAYS.toMillis(7) +
                     TimeUnit.DAYS.toMillis(4) +
                     TimeUnit.HOURS.toMillis(3) +
                     TimeUnit.MINUTES.toMillis(2) +
                     TimeUnit.SECONDS.toMillis(1) +
                     TimeUnit.MILLISECONDS.toMillis(432)
                , TimePeriod.parse("1w 4d 3h 2m 1s 432ms").getDuration());

        assertEquals(-(TimeUnit.DAYS.toMillis(7) +
                       TimeUnit.MINUTES.toMillis(6) +
                       TimeUnit.SECONDS.toMillis(5))
                , TimePeriod.parse("-7d 6m 5s").getDuration());

        assertEquals(1234, TimePeriod.parse("1234").getDuration());

        assertEquals(0, TimePeriod.parse("0").getDuration());

        assertNull(TimePeriod.parse(""));
        assertNull(TimePeriod.parse(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseErrorTimePeriodTest() {
        TimePeriod.parse("2w 1d 2m 3x 4s");
    }

    @Test
    public void toStringTest() {
        assertEquals("1w 2d 3h 4m 5s 6ms", new TimePeriod(
                             TimeUnit.DAYS.toMillis(7) +
                             TimeUnit.DAYS.toMillis(2) +
                             TimeUnit.HOURS.toMillis(3) +
                             TimeUnit.MINUTES.toMillis(4) +
                             TimeUnit.SECONDS.toMillis(5) +
                             TimeUnit.MILLISECONDS.toMillis(6), TimeUnit.MILLISECONDS).toString()
                    );
        assertEquals("-1w 2d 3h 4m 5s 6ms", new TimePeriod(
                             -(TimeUnit.DAYS.toMillis(7) +
                               TimeUnit.DAYS.toMillis(2) +
                               TimeUnit.HOURS.toMillis(3) +
                               TimeUnit.MINUTES.toMillis(4) +
                               TimeUnit.SECONDS.toMillis(5) +
                               TimeUnit.MILLISECONDS.toMillis(6)), TimeUnit.MILLISECONDS).toString()
                    );

        assertEquals("0", TimePeriod.ZERO.toString());
    }

    @Test
    public void stressToStringParseTest() {
        Random random = new Random(42);
        for (int i = 0; i < 100_000; i++) {
            TimePeriod timePeriod = new TimePeriod(random.nextLong(), TimeUnit.MILLISECONDS);
            assertEquals(timePeriod.toString(), TimePeriod.parse(timePeriod.toString()).toString());
        }
    }

    @Test
    public void sinceNowToTest() {
        Date date = new Date(System.currentTimeMillis() + 10000);
        TimePeriod timePeriod = TimePeriod.sinceNowTo(date);
        assertTrue("Actual " + timePeriod.getDuration(), timePeriod.getDuration() <= 10000);
        assertTrue("Actual " + timePeriod.getDuration(), timePeriod.getDuration() > 9950);

        TimePeriod millis = TimePeriod.sinceNowTo(System.currentTimeMillis() + 10000);
        assertTrue("Actual " + millis.getDuration(), millis.getDuration() <= 10000);
        assertTrue("Actual " + millis.getDuration(), millis.getDuration() > 9950);
    }

    @Test
    public void tillNowFromTest() {
        Date date = new Date(System.currentTimeMillis() - 10000);
        TimePeriod timePeriod = TimePeriod.tillNowFrom(date);
        assertTrue("Actual " + timePeriod.getDuration(), timePeriod.getDuration() >= 10000);
        assertTrue("Actual " + timePeriod.getDuration(), timePeriod.getDuration() < 10050);

        TimePeriod millis = TimePeriod.tillNowFrom(System.currentTimeMillis() - 10000);
        assertTrue("Actual " + millis.getDuration(), millis.getDuration() >= 10000);
        assertTrue("Actual " + millis.getDuration(), millis.getDuration() < 10050);
    }

    @Test
    public void between() {
        assertEquals(2, TimePeriod.between(new Date(1000), new Date(3000)).getDuration(TimeUnit.SECONDS));
        assertEquals(4, TimePeriod.between(6000, 10000).getDuration(TimeUnit.SECONDS));
    }

    @Test
    public void addTest() {
        assertEquals("1h 25m 143ms",
                     TimePeriod.parse("3m 143ms").add(TimePeriod.parse("1h 22m")).toString());
        assertEquals("4h 2s",
                     new TimePeriod(4, TimeUnit.HOURS).add(new TimePeriod(2, TimeUnit.SECONDS)).toString());
        assertEquals("2h 4s",
                     new TimePeriod(4, TimeUnit.SECONDS).add(new TimePeriod(2, TimeUnit.HOURS)).toString());
    }

    @Test(expected = ArithmeticException.class)
    public void addOverflowTestAtConversion() {
        new TimePeriod(TimeUnit.MILLISECONDS.toDays(Long.MIN_VALUE) - 1, TimeUnit.DAYS).add(TimePeriod.parse("1d 1ms"));
    }

    @Test(expected = ArithmeticException.class)
    public void addOverflowTestAtAddition() {
        new TimePeriod(TimeUnit.MILLISECONDS.toDays(Long.MAX_VALUE), TimeUnit.DAYS).add(TimePeriod.parse("1d 1ms"));
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
    public void subTest() {
        assertEquals("41m 20ms",
                     TimePeriod.parse("1h 3m 24ms").sub(TimePeriod.parse("22m 4ms")).toString());
        assertEquals("3h 59m 58s",
                     new TimePeriod(4, TimeUnit.HOURS).sub(new TimePeriod(2, TimeUnit.SECONDS)).toString());
        assertEquals("-1h 59m 56s",
                     new TimePeriod(4, TimeUnit.SECONDS).sub(new TimePeriod(2, TimeUnit.HOURS)).toString());

    }

    @Test(expected = ArithmeticException.class)
    public void subOverflowTest() {
        new TimePeriod(TimeUnit.MILLISECONDS.toDays(Long.MIN_VALUE), TimeUnit.DAYS).sub(TimePeriod.parse("1d 1ms"));
    }
}
