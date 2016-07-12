package org.libsmith.anvil.io;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 13.07.16
 */
public class CountingOutputStreamTest {

    @Test
    public void countTest() throws IOException {

        CountingOutputStream cos = new CountingOutputStream();

        cos.write(142);
        assertThat(cos.getCount()).isEqualTo(1);

        cos.write("Test test test!".getBytes());
        assertThat(cos.getCount()).isEqualTo("Test test test!".length() + 1);

        cos.write("Test test test!".getBytes(), 3, 4);
        assertThat(cos.getCount()).isEqualTo("Test test test!".length() + 1 + 4);
    }

    @Test
    public void passthruTest() throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CountingOutputStream cos = new CountingOutputStream(baos);

        cos.write('A');
        cos.write(" test ".getBytes());
        cos.write("bad case ever ".getBytes(), 4, 4);

        cos.flush();

        assertThat(baos.toString()).isEqualTo("A test case");
        assertThat(cos.getCount()).isEqualTo(baos.size());
    }
}
