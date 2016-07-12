package org.libsmith.anvil.io;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 22.06.16
 */
public class NullOutputStream extends OutputStream {

    private static final NullOutputStream INSTANCE = new NullOutputStream();

    protected NullOutputStream()
    { }

    @Override
    public void write(int b) throws IOException
    { }

    @Override
    public void write(@Nonnull byte[] b) throws IOException
    { }

    @Override
    public void write(@Nonnull byte[] b, int off, int len) throws IOException
    { }

    public static NullOutputStream getInstance() {
        return INSTANCE;
    }
}
