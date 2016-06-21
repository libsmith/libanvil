package org.libsmith.anvil.log;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.libsmith.anvil.AbstractTest;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 21.06.16
 */
public class LogRecordBuilderTest extends AbstractTest {

    @Test
    public void finest() throws Exception {

        String messagePattern = "Finest message {0}";

        Assertions.assertThat(LogRecordBuilder.finest())
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.FINEST, null, null);

        Assertions.assertThat(LogRecordBuilder.finest(messagePattern, 42))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.FINEST, messagePattern, new Object[] { 42 });


        Assertions.assertThat(LogRecordBuilder.finestIfLoggableWith(TestLogger.FINEST))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.FINEST, null, null);

        Assertions.assertThat(LogRecordBuilder.finestIfLoggableWith(TestLogger.FINER))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.ALL, null, null);
    }

    @Test
    public void finer() throws Exception {

        String messagePattern = "Finer message {0}";

        Assertions.assertThat(LogRecordBuilder.finer())
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.FINER, null, null);

        Assertions.assertThat(LogRecordBuilder.finer(messagePattern, 42))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.FINER, messagePattern, new Object[] { 42 });

        Assertions.assertThat(LogRecordBuilder.finerIfLoggableWith(TestLogger.FINER))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.FINER, null, null);

        Assertions.assertThat(LogRecordBuilder.finerIfLoggableWith(TestLogger.FINE))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.ALL, null, null);
    }

    @Test
    public void fine() throws Exception {

        String messagePattern = "Fine message {0}";

        Assertions.assertThat(LogRecordBuilder.fine())
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.FINE, null, null);

        Assertions.assertThat(LogRecordBuilder.fine(messagePattern, 42))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.FINE, messagePattern, new Object[] { 42 });

        Assertions.assertThat(LogRecordBuilder.fineIfLoggableWith(TestLogger.FINE))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.FINE, null, null);

        Assertions.assertThat(LogRecordBuilder.fineIfLoggableWith(TestLogger.CONFIG))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.ALL, null, null);
    }

    @Test
    public void config() throws Exception {

        String messagePattern = "Config message {0}";

        Assertions.assertThat(LogRecordBuilder.config())
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.CONFIG, null, null);

        Assertions.assertThat(LogRecordBuilder.config(messagePattern, 42))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.CONFIG, messagePattern, new Object[] { 42 });

        Assertions.assertThat(LogRecordBuilder.configIfLoggableWith(TestLogger.CONFIG))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.CONFIG, null, null);

        Assertions.assertThat(LogRecordBuilder.configIfLoggableWith(TestLogger.INFO))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.ALL, null, null);
    }

    @Test
    public void info() throws Exception {

        String messagePattern = "Info message {0}";

        Assertions.assertThat(LogRecordBuilder.info())
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.INFO, null, null);

        Assertions.assertThat(LogRecordBuilder.info(messagePattern, 42))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.INFO, messagePattern, new Object[] { 42 });

        Assertions.assertThat(LogRecordBuilder.infoIfLoggableWith(TestLogger.INFO))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.INFO, null, null);

        Assertions.assertThat(LogRecordBuilder.infoIfLoggableWith(TestLogger.WARNING))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.ALL, null, null);
    }

    @Test
    public void warning() throws Exception {

        String messagePattern = "Warning message {0}";

        Assertions.assertThat(LogRecordBuilder.warning())
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.WARNING, null, null);

        Assertions.assertThat(LogRecordBuilder.warning(messagePattern, 42))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.WARNING, messagePattern, new Object[] { 42 });

        Assertions.assertThat(LogRecordBuilder.warningIfLoggableWith(TestLogger.WARNING))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.WARNING, null, null);

        Assertions.assertThat(LogRecordBuilder.warningIfLoggableWith(TestLogger.SEVERE))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.ALL, null, null);
    }

    @Test
    public void severe() throws Exception {

        String messagePattern = "Severe message {0}";

        Assertions.assertThat(LogRecordBuilder.severe())
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.SEVERE, null, null);

        Assertions.assertThat(LogRecordBuilder.severe(messagePattern, 42))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.SEVERE, messagePattern, new Object[] { 42 });

        Assertions.assertThat(LogRecordBuilder.severeIfLoggableWith(TestLogger.SEVERE))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.SEVERE, null, null);

        Assertions.assertThat(LogRecordBuilder.severeIfLoggableWith(TestLogger.OFF))
                  .extracting("level", "message", "parameters")
                  .containsExactly(Level.ALL, null, null);
    }

    @Test
    public void with() throws Exception {

        LogRecordBuilder record = LogRecordBuilder.info();
        String message = "Info message {0}";
        Object[] parameters = { 42 };
        Throwable throwable = new RuntimeException();
        long millis = 10042;

        Assertions.assertThat(record.withMessage(message)
                                    .withParameters(parameters)
                                    .withThrown(throwable)
                                    .withMillis(millis))
                  .isSameAs(record)
                  .extracting("level", "message", "parameters", "thrown", "millis")
                  .containsExactly(Level.INFO, message, parameters, throwable, millis);
    }

    @Test
    public void preparedNullConfigMustHavePassthruBuilderMethods() {

        LogRecordBuilder passthruBuilder = LogRecordBuilder.severeIfLoggableWith(TestLogger.OFF);
        Assertions.assertThat(passthruBuilder.withMessage("a")
                                             .withParameters(1, 2, 3)
                                             .withThrown(new Throwable())
                                             .withMillis(1))
                  .isSameAs(passthruBuilder)
                  .extracting("level", "message", "thrown", "millis")
                  .containsExactly(Level.ALL, null, null, 0L);
    }

    @Test
    public void preparedNullConfigMustBeImmutable() {

        LogRecordBuilder passthruBuilder = LogRecordBuilder.severeIfLoggableWith(TestLogger.OFF);

        Assertions.assertThatThrownBy(() -> passthruBuilder.setLevel(Level.FINEST))
                  .isInstanceOf(UnsupportedOperationException.class);

        Assertions.assertThatThrownBy(() -> passthruBuilder.setMillis(0))
                  .isInstanceOf(UnsupportedOperationException.class);

        Assertions.assertThatThrownBy(() -> passthruBuilder.setParameters(null))
                  .isInstanceOf(UnsupportedOperationException.class);

        Assertions.assertThatThrownBy(() -> passthruBuilder.setMessage(null))
                  .isInstanceOf(UnsupportedOperationException.class);

        Assertions.assertThatThrownBy(() -> passthruBuilder.setLoggerName(null))
                  .isInstanceOf(UnsupportedOperationException.class);

        Assertions.assertThatThrownBy(() -> passthruBuilder.setResourceBundle(null))
                  .isInstanceOf(UnsupportedOperationException.class);

        Assertions.assertThatThrownBy(() -> passthruBuilder.setResourceBundleName(null))
                  .isInstanceOf(UnsupportedOperationException.class);

        Assertions.assertThatThrownBy(() -> passthruBuilder.setSequenceNumber(0))
                  .isInstanceOf(UnsupportedOperationException.class);

        Assertions.assertThatThrownBy(() -> passthruBuilder.setSourceClassName(null))
                  .isInstanceOf(UnsupportedOperationException.class);

        Assertions.assertThatThrownBy(() -> passthruBuilder.setSourceMethodName(null))
                  .isInstanceOf(UnsupportedOperationException.class);

        Assertions.assertThatThrownBy(() -> passthruBuilder.setThreadID(0))
                  .isInstanceOf(UnsupportedOperationException.class);

        Assertions.assertThatThrownBy(() -> passthruBuilder.setThrown(null))
                  .isInstanceOf(UnsupportedOperationException.class);

    }

    private static class TestLogger extends Logger {

        private static final TestLogger OFF     = new TestLogger(Level.OFF);
        private static final TestLogger SEVERE  = new TestLogger(Level.SEVERE);
        private static final TestLogger WARNING = new TestLogger(Level.WARNING);
        private static final TestLogger INFO    = new TestLogger(Level.INFO);
        private static final TestLogger CONFIG  = new TestLogger(Level.CONFIG);
        private static final TestLogger FINE    = new TestLogger(Level.FINE);
        private static final TestLogger FINER   = new TestLogger(Level.FINER);
        private static final TestLogger FINEST  = new TestLogger(Level.FINEST);

        private TestLogger(Level level) {

            super(null, null);
            setLevel(level);
        }
    }
}
