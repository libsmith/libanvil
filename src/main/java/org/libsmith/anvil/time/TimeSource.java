package org.libsmith.anvil.time;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 02.07.16
 */
@SuppressWarnings("unused")
public interface TimeSource {

    long getTime();
    @Nonnull TimeUnit getResolution();


    TimeSource NANO_TIME_SOURCE = new TimeSource() {

        @Override
        public long getTime() {
            return System.nanoTime();
        }

        @Override
        public @Nonnull TimeUnit getResolution() {
            return TimeUnit.NANOSECONDS;
        }
    };

    TimeSource MILLIS_TIME_SOURCE = new TimeSource() {

        @Override
        public long getTime() {
            return System.currentTimeMillis();
        }

        @Override
        public @Nonnull TimeUnit getResolution() {
            return TimeUnit.MILLISECONDS;
        }
    };
}
