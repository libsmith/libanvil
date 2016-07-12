package org.libsmith.anvil.time;

import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 13.07.16
 */
public class ImmutableDateTest extends AbstractImmutableDateTest<ImmutableDate> {

    @Override
    public ImmutableDate val(long value) {
        return new ImmutableDate(value);
    }

    @Test
    public void nowTest() throws Exception {
        assertThat(ImmutableDate.now()).isCloseTo(new Date(), 1000);
    }
}
