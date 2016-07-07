package org.libsmith.anvil.log;

import javax.annotation.Nonnull;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 21.06.16
 */
public class LogRecordBuilder extends LogRecord {

    private static final long serialVersionUID = 7469618647126785527L;

    private LogRecordBuilder(@Nonnull Level level, String messagePattern) {
        super(level, messagePattern);
    }

    private LogRecordBuilder(@Nonnull Level level, String messagePattern, Object ... patternParameters) {
        super(level, messagePattern);
        setParameters(patternParameters);
    }

    public static LogRecordBuilder finest() {
        return new LogRecordBuilder(Level.FINEST, null);
    }

    public static LogRecordBuilder finest(@Nonnull String messagePattern, @Nonnull Object ... patternParameters) {
        return new LogRecordBuilder(Level.FINEST, messagePattern, patternParameters);
    }

    public static LogRecordBuilder finestIfLoggableWith(@Nonnull Logger logger) {
        return logger.isLoggable(Level.FINEST) ? finest() : NULL_LOG_RECORD;
    }

    public static LogRecordBuilder finer() {
        return new LogRecordBuilder(Level.FINER, null);
    }

    public static LogRecordBuilder finer(@Nonnull String messagePattern, @Nonnull Object ... patternParameters) {
        return new LogRecordBuilder(Level.FINER, messagePattern, patternParameters);
    }

    public static LogRecordBuilder finerIfLoggableWith(@Nonnull Logger logger) {
        return logger.isLoggable(Level.FINER) ? finer() : NULL_LOG_RECORD;
    }

    public static LogRecordBuilder fine() {
        return new LogRecordBuilder(Level.FINE, null);
    }

    public static LogRecordBuilder fine(@Nonnull String messagePattern, @Nonnull Object ... patternParameters) {
        return new LogRecordBuilder(Level.FINE, messagePattern, patternParameters);
    }

    public static LogRecordBuilder fineIfLoggableWith(Logger logger) {
        return logger.isLoggable(Level.FINE) ? fine() : NULL_LOG_RECORD;
    }

    public static LogRecordBuilder config() {
        return new LogRecordBuilder(Level.CONFIG, null);
    }

    public static LogRecordBuilder config(@Nonnull String messagePattern, @Nonnull Object... patternParameters) {
        return new LogRecordBuilder(Level.CONFIG, messagePattern, patternParameters);
    }

    public static LogRecordBuilder configIfLoggableWith(@Nonnull Logger logger) {
        return logger.isLoggable(Level.CONFIG) ? config() : NULL_LOG_RECORD;
    }

    public static LogRecordBuilder info() {
        return new LogRecordBuilder(Level.INFO, null);
    }

    public static LogRecordBuilder info(@Nonnull String messagePattern, @Nonnull Object ... patternParameters) {
        return new LogRecordBuilder(Level.INFO, messagePattern, patternParameters);
    }

    public static LogRecordBuilder infoIfLoggableWith(@Nonnull Logger logger) {
        return logger.isLoggable(Level.INFO) ? info() : NULL_LOG_RECORD;
    }

    public static LogRecordBuilder warning() {
        return new LogRecordBuilder(Level.WARNING, null);
    }

    public static LogRecordBuilder warning(@Nonnull String messagePattern, @Nonnull Object ... patternParameters) {
        return new LogRecordBuilder(Level.WARNING, messagePattern, patternParameters);
    }

    public static LogRecordBuilder warningIfLoggableWith(@Nonnull Logger logger) {
        return logger.isLoggable(Level.WARNING) ? warning() : NULL_LOG_RECORD;
    }

    public static LogRecordBuilder severe() {
        return new LogRecordBuilder(Level.SEVERE, null);
    }

    public static LogRecordBuilder severe(@Nonnull String messagePattern, @Nonnull Object ... patternParameters) {
        return new LogRecordBuilder(Level.SEVERE, messagePattern, patternParameters);
    }

    public static LogRecordBuilder severeIfLoggableWith(@Nonnull Logger logger) {
        return logger.isLoggable(Level.SEVERE) ? new LogRecordBuilder(Level.SEVERE, null) : NULL_LOG_RECORD;
    }

    public LogRecordBuilder withMessage(@Nonnull String message) {
        setMessage(message);
        return this;
    }

    public LogRecordBuilder withParameters(@Nonnull Object ... parameters) {
        setParameters(parameters);
        return this;
    }

    public LogRecordBuilder withThrown(@Nonnull Throwable throwable) {
        setThrown(throwable);
        return this;
    }

    public LogRecordBuilder withMillis(long millis) {
        setMillis(millis);
        return this;
    }

    @SuppressWarnings("serial")
    private static final LogRecordBuilder NULL_LOG_RECORD = new LogRecordBuilder(Level.ALL, null) {
        { super.setMillis(0); }

        //<editor-fold desc="Passthru immutable builder methods override">
        @Override
        public LogRecordBuilder withThrown(@Nonnull Throwable throwable) {
            return this;
        }

        @Override
        public LogRecordBuilder withMessage(@Nonnull String message) {
            return this;
        }

        @Override
        public LogRecordBuilder withParameters(@Nonnull Object... parameters) {
            return this;
        }

        @Override
        public LogRecordBuilder withMillis(long millis) {
            return this;
        }
        //</editor-fold>

        //<editor-fold desc="Immutable setters override">
        @Override
        public void setLevel(Level level) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLoggerName(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMessage(String message) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSourceClassName(String sourceClassName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSourceMethodName(String sourceMethodName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setThreadID(int threadID) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMillis(long millis) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setParameters(Object[] parameters) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setResourceBundle(ResourceBundle bundle) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setResourceBundleName(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSequenceNumber(long seq) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setThrown(Throwable thrown) {
            throw new UnsupportedOperationException();
        }
        //</editor-fold>
    };
}
