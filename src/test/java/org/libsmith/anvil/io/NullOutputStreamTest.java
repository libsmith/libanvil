package org.libsmith.anvil.io;

import org.junit.Test;

import java.io.IOException;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 13.07.16
 */
public class NullOutputStreamTest {

    @Test
    public void writeTest() throws IOException {

        NullOutputStream nullOutputStream = NullOutputStream.getInstance();
        nullOutputStream.write(134);
        nullOutputStream.write(new byte[1234]);
        nullOutputStream.write(new byte[3214], 1, 44);
    }
}
