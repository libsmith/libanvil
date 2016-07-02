package org.libsmith.anvil.time;

import org.junit.Before;
import org.junit.Test;
import org.libsmith.anvil.time.Stopwatch.Group;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 02.07.16
 */
public class StopwatchTest {

    private static final TimeUnit DEFAULT_RESOLUTION = TimeUnit.MICROSECONDS;
    private static final long DEFAULT_ITERATION = 100_000;

    private TimeSource mockTimeSource;

    @Before
    public void before() {
        AtomicLong counter = new AtomicLong();
        mockTimeSource = Mockito.mock(TimeSource.class);
        Mockito.when(mockTimeSource.getTime()).then(i -> counter.addAndGet(DEFAULT_ITERATION));
        Mockito.when(mockTimeSource.getResolution()).then(i -> StopwatchTest.DEFAULT_RESOLUTION);
    }

    @Test
    public void constructTest() {
        assertThat(Stopwatch.start().getTaskName()).isNull();
        assertThat(Stopwatch.start().getTimeSource()).isEqualTo(TimeSource.MILLIS_TIME_SOURCE);
        assertThat(Stopwatch.start().getOriginTime()).isGreaterThan(0);
        assertThat(Stopwatch.start().getOriginTime()).isLessThanOrEqualTo(System.currentTimeMillis());
        assertThat(Stopwatch.start("Test").getTaskName()).isEqualTo("Test");
        assertThat(Stopwatch.start("Test", TimeSource.NANO_TIME_SOURCE).getTimeSource())
                .isEqualTo(TimeSource.NANO_TIME_SOURCE);

        assertThat(Stopwatch.start("Task name", mockTimeSource).toString()).isEqualTo("Task name 100ms");
    }

    @Test
    public void getSampleTest() {
        Stopwatch test = Stopwatch.start("Test", mockTimeSource);
        Mockito.verify(mockTimeSource, Mockito.times(1)).getTime();

        Stopwatch.Sample sampleA = test.sample();
        Mockito.verify(mockTimeSource, Mockito.times(2)).getTime();
        assertThat(sampleA.getOrigin()).isSameAs(test);
        assertThat(sampleA.getTimeUnit()).isEqualTo(DEFAULT_RESOLUTION);
        assertThat(sampleA.getDuration()).isEqualTo(DEFAULT_ITERATION);

        Stopwatch.Sample sampleB = test.sample();
        Mockito.verify(mockTimeSource, Mockito.times(3)).getTime();
        assertThat(sampleB.getOrigin()).isSameAs(test);
        assertThat(sampleB.getTimeUnit()).isEqualTo(DEFAULT_RESOLUTION);
        assertThat(sampleB.getDuration()).isEqualTo(DEFAULT_ITERATION * 2);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void sampleToStringTest() {
        assertThat(Stopwatch.start(null, mockTimeSource).sample().toString()).isEqualTo("100ms");
        assertThat(Stopwatch.start("Test name", mockTimeSource).sample().toString()).isEqualTo("Test name 100ms");
        assertThat(Stopwatch.start("Test name", mockTimeSource).sample().toString()).isEqualTo("Test name 100ms");
    }

    @Test
    public void groupTest() {
        assertThat(Stopwatch.group("Simple group").getTimeSource()).isEqualTo(TimeSource.MILLIS_TIME_SOURCE);
        assertThat(Stopwatch.group("Simple group").getGroupName()).isEqualTo("Simple group");

        Group group = Stopwatch.group("Group name", mockTimeSource);
        assertThat(group.isRunning()).isFalse();
        group.start("Task A");
        assertThat(group.isRunning()).isTrue();
        assertThat(group.getSamples()).isEmpty();
        assertThat(group.stream().mapToLong(TimePeriod::getDuration).sum()).isZero();
        group.start("Task B");
        assertThat(group.isRunning()).isTrue();
        assertThat(group.getSamples()).hasSize(1);
        assertThat(group.stream().mapToLong(TimePeriod::getDuration).sum()).isEqualTo(DEFAULT_ITERATION);
        group.stop();
        assertThat(group.isRunning()).isFalse();
        assertThat(group.getSamples()).hasSize(2);
        assertThat(group.stream().mapToLong(TimePeriod::getDuration).sum()).isEqualTo(DEFAULT_ITERATION * 2);
        group.start("Task C");
        assertThat(group.isRunning()).isTrue();
        assertThat(group.getSamples()).hasSize(2);
        assertThat(group.stream().mapToLong(TimePeriod::getDuration).sum()).isEqualTo(DEFAULT_ITERATION * 2);
        group.stop();
        assertThat(group.isRunning()).isFalse();
        assertThat(group.getSamples()).hasSize(3);
        assertThat(group.stream().mapToLong(TimePeriod::getDuration).sum()).isEqualTo(DEFAULT_ITERATION * 3);
        assertThat(group.toString()).isEqualTo("Group name 300ms [Task A 100ms; Task B 100ms; Task C 100ms]");

        assertThatThrownBy(() -> Stopwatch.group("").stop()).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Stopwatch.group("").start("").stop().stop()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void messageFormatConstructors() {
        assertThat(Stopwatch.start("Task {0} ptn {1}", "zero", 42).toString()).startsWith("Task zero ptn 42");
        assertThat(Stopwatch.group("Group {0} ptn {1}", "zero", 42).getGroupName()).startsWith("Group zero ptn 42");
        assertThat(Stopwatch.group("").start("Task {0} ptn {1}", "zero", 42)
                            .stop().getSamples().get(0).getOrigin().getTaskName()).startsWith("Task zero ptn 42");
    }


}
