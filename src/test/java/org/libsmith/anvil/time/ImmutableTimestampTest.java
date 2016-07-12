package org.libsmith.anvil.time;

import org.junit.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 13.07.16
 */
public class ImmutableTimestampTest extends AbstractImmutableDateTest<ImmutableTimestamp> {

    @Override
    public ImmutableTimestamp val(long value) {
        return new ImmutableTimestamp(value);
    }

    @Test
    public void nowTest() throws Exception {
        assertThat(ImmutableTimestamp.now()).isCloseTo(new Timestamp(System.currentTimeMillis()), 1000);
    }
}
