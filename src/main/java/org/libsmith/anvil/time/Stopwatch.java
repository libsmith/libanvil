package org.libsmith.anvil.time;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.libsmith.anvil.time.TimeSource.MILLIS_TIME_SOURCE;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 22.06.16
 */
@Immutable
@ThreadSafe
public class Stopwatch implements Serializable {

    private static final long serialVersionUID = 6703242737042402435L;
    private static final TimeSource DEFAULT_TIME_SOURCE = MILLIS_TIME_SOURCE;

    private final @Nullable String taskName;
    private final long originTime;
    private final TimeSource timeSource;

    protected Stopwatch(@Nullable String taskName, long originTime, TimeSource timeSource) {
        this.originTime = originTime;
        this.timeSource = timeSource;
        this.taskName = taskName;
    }

    public long getOriginTime() {
        return originTime;
    }

    public TimeSource getTimeSource() {
        return timeSource;
    }

    public @Nullable String getTaskName() {
        return taskName;
    }

    public @Nonnull Sample sample() {
        TimeSource timeSource = getTimeSource();
        return new Sample(timeSource.getTime() - getOriginTime(), timeSource.getResolution());
    }

    @Override
    public String toString() {
        return sample().toString();
    }

    @Immutable
    @ThreadSafe
    public class Sample extends TimePeriod {

        private static final long serialVersionUID = 3810523489112745052L;

        protected Sample(long duration, TimeUnit timeUnit) {
            super(duration, timeUnit);
        }

        public Stopwatch getOrigin() {
            return Stopwatch.this;
        }

        @Override
        public String toString(TimeUnit timeUnit) {
            return taskName == null ? super.toString(timeUnit) : taskName + " " + super.toString(timeUnit);
        }
    }

    public static Stopwatch start() {
        return new Stopwatch(null, DEFAULT_TIME_SOURCE.getTime(), DEFAULT_TIME_SOURCE);
    }

    public static Stopwatch start(@Nonnull String taskName) {
        return new Stopwatch(taskName, DEFAULT_TIME_SOURCE.getTime(), DEFAULT_TIME_SOURCE);
    }

    public static Stopwatch start(@Nonnull String taskNameFormat, Object ... patternArguments) {
        return start(MessageFormat.format(taskNameFormat, patternArguments));
    }

    public static Stopwatch start(@Nonnull String taskName, @Nonnull TimeSource timeSource) {
        return new Stopwatch(taskName, timeSource.getTime(), timeSource);
    }

    public static Group group(@Nonnull String groupName) {
        return new Group(groupName, DEFAULT_TIME_SOURCE);
    }

    public static Group group(@Nonnull String groupNameFormat, Object ... patternArguments) {
        return group(MessageFormat.format(groupNameFormat, patternArguments));
    }

    public static Group group(@Nonnull String groupName, @Nonnull TimeSource timeSource) {
        return new Group(groupName, timeSource);
    }

    @ThreadSafe
    public static class Group implements Iterable<Sample> {

        private final String groupName;
        private final TimeSource timeSource;
        private final List<Sample> samples;

        private Stopwatch current;

        private Group(@Nonnull String groupName, @Nonnull TimeSource timeSource) {
            this.groupName = groupName;
            this.timeSource = timeSource;
            this.samples = Collections.synchronizedList(new ArrayList<>());
        }

        public String getGroupName() {
            return groupName;
        }

        public TimeSource getTimeSource() {
            return timeSource;
        }

        public List<Sample> getSamples() {
            return Collections.unmodifiableList(samples);
        }

        public Group start(@Nonnull String taskName) {
            synchronized (samples) {
                if (current != null) {
                    stop();
                }
                current = Stopwatch.start(taskName, timeSource);
            }
            return this;
        }

        public Group start(@Nonnull String taskNameFormat, Object ... formatArguments) {
            return start(MessageFormat.format(taskNameFormat, formatArguments));
        }

        public boolean isRunning() {
            synchronized (samples) {
                return current != null;
            }
        }

        public Group stop() {
            synchronized (samples) {
                if (current == null) {
                    throw new IllegalStateException();
                }
                samples.add(current.sample());
                current = null;
            }
            return this;
        }

        public Stream<Sample> stream() {
            return StreamSupport.stream(spliterator(), false);
        }

        @Override
        public @Nonnull Iterator<Sample> iterator() {
            return getSamples().iterator();
        }

        @Override
        public String toString() {
            return toString(getTimeSource().getResolution());
        }

        public String toString(TimeUnit timeUnit) {
            return getGroupName() + " " + TimePeriod.sum(getSamples()) + " [" +
                   stream().map(v -> v.toString(timeUnit)).collect(Collectors.joining("; ")) +
                   (current != null ? "; " + current.sample().toString(timeUnit) : "") + "]";
        }
    }
}
