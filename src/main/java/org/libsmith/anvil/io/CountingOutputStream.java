package org.libsmith.anvil.io;

import javax.annotation.Nonnull;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 22.06.16
 */
public class CountingOutputStream extends FilterOutputStream {

    private long count;

    public CountingOutputStream() {
        super(NullOutputStream.getInstance());
    }

    public CountingOutputStream(@Nonnull OutputStream out) {
        super(out);
    }

    public long getCount() {
        return count;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        count += 1;
    }

    @Override
    public void write(@Nonnull byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        count += len;
    }
}
