package org.libsmith.anvil;

import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 04.09.16 21:41
 */
public class UncheckedExceptionTest {

    @Test
    public void wrappedStackTraceTest() {
        Exception ex = new Exception();
        UncheckedException uex = new UncheckedException(ex);
        assertThat(uex.getStackTrace()).isEqualTo(ex.getStackTrace());
    }

    @Test
    public void wrapThrowableTest() {
        assertThat(UncheckedException.wrap(new IOException()))
                .isInstanceOf(UncheckedException.class)
                .hasCauseInstanceOf(IOException.class);

        assertThat(UncheckedException.wrap(new UnsupportedOperationException()))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasNoCause();
    }

    @Test
    public void wrapFunctionalTest() {

        assertThatThrownBy(() -> UncheckedException.wrap(this::voidCheckedException))
                .isInstanceOf(UncheckedException.class)
                .hasCauseInstanceOf(IOException.class);

        assertThatThrownBy(() -> UncheckedException.wrap(this::voidUncheckedException))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasNoCause();

        assertThatThrownBy(() -> UncheckedException.wrap(this::objectCheckedException))
                .isInstanceOf(UncheckedException.class)
                .hasCauseInstanceOf(IOException.class);

        assertThatThrownBy(() -> UncheckedException.wrap(this::objectUncheckedException))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasNoCause();

        UncheckedException.wrap(this::noop);
        assertThat(UncheckedException.wrap(this::some)).isEqualTo(42);
    }

    @Test
    public void rethrowFunctionalTest() {
        assertThatThrownBy(() -> UncheckedException.rethrow(this::voidCheckedException))
                .isInstanceOf(IOException.class)
                .hasNoCause();

        assertThatThrownBy(() -> UncheckedException.rethrow(this::voidUncheckedException))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasNoCause();

        assertThatThrownBy(() -> UncheckedException.rethrow(this::objectCheckedException))
                .isInstanceOf(IOException.class)
                .hasNoCause();

        assertThatThrownBy(() -> UncheckedException.rethrow(this::objectUncheckedException))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasNoCause();

        UncheckedException.rethrow(this::noop);
        assertThat(UncheckedException.rethrow(this::some)).isEqualTo(42);
    }

    private void noop()
    { }

    private int some() {
        return 42;
    }

    private void voidCheckedException() throws IOException {
        throw new IOException();
    }

    private void voidUncheckedException() {
        throw new UnsupportedOperationException();
    }

    private Object objectCheckedException() throws IOException {
        throw new IOException();
    }

    private Object objectUncheckedException() {
        throw new UnsupportedOperationException();
    }
}
