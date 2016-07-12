package org.libsmith.anvil.time;

import org.junit.Test;
import org.libsmith.anvil.exception.ImmutabilityProhibitedException;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 13.07.16
 */
public abstract class AbstractImmutableDateTest<T extends Date & DateExtensions> {

    protected abstract T val(long value);

    @Test
    public void compareTo() throws Exception {
        assertThat(val(-1).compareTo(val(1))).isLessThan(0);
        assertThat(val(1).compareTo(val(-1))).isGreaterThan(0);
        assertThat(val(0).compareTo(val(0))).isZero();
    }

    @Test
    public void afterTest() throws Exception {
        assertThat(val(1).after(val(0))).isTrue();
        assertThat(val(0).after(val(1))).isFalse();
    }

    @Test
    public void beforeTest() throws Exception {
        assertThat(val(-1).before(val(0))).isTrue();
        assertThat(val(0).before(val(-1))).isFalse();
    }

    @Test
    public void addTest() throws Exception {
        assertThat(val(100).add(2, TimeUnit.HOURS).getTime())
                .isEqualTo(TimeUnit.HOURS.toMillis(2) + 100);

        assertThat(val(100).add(TimePeriod.parse("3h 4m")).getTime())
                .isEqualTo(TimeUnit.HOURS.toMillis(3) + TimeUnit.MINUTES.toMillis(4) + 100);
    }

    @Test
    public void subTest() throws Exception {
        assertThat(val(100).sub(val(40))).isEqualByComparingTo(TimePeriod.parse("60ms"));
        assertThat(val(50000).sub(TimePeriod.parse("3s"))).isEqualTo(val(47000));
        assertThat(val(5000).sub(4, TimeUnit.SECONDS)).isEqualTo(val(1000));
    }

    @Test
    public void quantizeTest() throws Exception {
        assertThat(val(123456).quantize(TimeUnit.SECONDS)).isEqualTo(val(123000));
        assertThat(val(123456).quantize(TimeUnit.MINUTES)).isEqualTo(val(120000));
    }


    @Test
    public void maxTest() throws Exception {
        assertThat(DateExtensions.max(val(142), val(-1234567), val(356), val(3))).isEqualTo(val(356));
    }

    @Test
    public void minTest() throws Exception {
        assertThat(DateExtensions.min(val(142), val(1234567), val(-356), val(3))).isEqualTo(val(-356));
    }

    @Test
    public void cloneTest() throws Exception {
        Date original = val(4242424242L);
        Date clone = (Date) original.clone();
        assertThat(clone).isNotSameAs(original);
        assertThat(clone).isEqualTo(original);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void immutabilityTest() throws Exception {
        Date val = val(123);
        assertThatThrownBy(() -> val.setYear(1)).isInstanceOf(ImmutabilityProhibitedException.class);
        assertThatThrownBy(() -> val.setMonth(1)).isInstanceOf(ImmutabilityProhibitedException.class);
        assertThatThrownBy(() -> val.setDate(1)).isInstanceOf(ImmutabilityProhibitedException.class);
        assertThatThrownBy(() -> val.setHours(1)).isInstanceOf(ImmutabilityProhibitedException.class);
        assertThatThrownBy(() -> val.setMinutes(1)).isInstanceOf(ImmutabilityProhibitedException.class);
        assertThatThrownBy(() -> val.setSeconds(1)).isInstanceOf(ImmutabilityProhibitedException.class);
        assertThatThrownBy(() -> val.setTime(1)).isInstanceOf(ImmutabilityProhibitedException.class);
        assertThat(val).isEqualTo(val(123));
    }
}
