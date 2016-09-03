package org.libsmith.anvil.io;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 17.06.16
 */
public class HexDumpOutputStream extends OutputStream {

    private static final char UNKNOWN = '\ufffd';

    private final Writer writer;

    private boolean isClosed;

    private int offset = 0;
    private int lineSize = 16;
    private int groupSize = 1;
    private int lookBackSize = 6;

    private Charset sourceCharset;
    private byte[] charBuffer;
    private int charBufferLength;

    private char[] line;
    private int offsetStringLength;
    private int charDumpOffset;
    private int lineOffset;

    protected HexDumpOutputStream(Writer writer) {
        this.writer = writer;
    }

    public static HexDumpOutputStream to(Writer writer) {
        return new HexDumpOutputStream(writer);
    }

    public static HexDumpOutputStream to(OutputStream outputStream) {
        return new HexDumpOutputStream(new OutputStreamWriter(outputStream));
    }

    public static HexDumpOutputStream to(PrintStream printStream) {
        return new HexDumpOutputStream(new PrintWriter(printStream));
    }

    public int getGroupSize() {
        return groupSize;
    }

    public void setGroupSize(int groupSize) throws IOException {
        if (groupSize < 0) {
            throw new IllegalArgumentException("Group size must be positive, got " + groupSize);
        }
        if (this.groupSize != groupSize) {
            flush();
            this.groupSize = groupSize;
        }
    }

    public int getLineSize() {
        return lineSize;
    }

    public void setLineSize(int lineSize) throws IOException {
        if (lineSize <= 0) {
            throw new IllegalArgumentException("Line size must greater than zero, got " + lineSize);
        }
        if (this.lineSize != lineSize) {
            flush();
            this.lineSize = lineSize;
        }
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) throws IOException {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must be positive, got " + offset);
        }
        if (this.offset != offset) {
            flush();
            this.offset = offset;
        }
    }

    public Charset getSourceCharset() {
        return sourceCharset;
    }

    public void setSourceCharset(Charset sourceCharset) throws IOException {
        if (!Objects.equals(this.sourceCharset, sourceCharset)) {
            flush();
            charBuffer = null;
            charBufferLength = 0;
            this.sourceCharset = sourceCharset;
        }
    }

    @Override
    public void write(int b) throws IOException {
        if (isClosed) {
            throw new IOException("Stream closed");
        }
        b = b & 0xFF;
        if (lineOffset >= lineSize) {
            flush();
        }
        if (line == null) {
            String offsetString = String.format("%06x ", offset);
            offsetStringLength = offsetString.length();
            charDumpOffset = offsetStringLength + lineSize * 2 +
                             (groupSize == 0 ? 1 : (lineSize + groupSize - 1) / groupSize);
            line = new char[charDumpOffset + lineSize];
            Arrays.fill(line, ' ');
            System.arraycopy(offsetString.toCharArray(), 0, line, 0, offsetString.length());
        }
        String hexValue = Integer.toHexString(b);
        int linePosition = offsetStringLength + lineOffset * 2 + (groupSize == 0 ? 0 : lineOffset / groupSize);
        line[linePosition] = hexValue.length() > 1 ? hexValue.charAt(0) : '0';
        line[linePosition + 1] = hexValue.length() > 1 ? hexValue.charAt(1) : hexValue.charAt(0);

        String charAsString = null;
        if (sourceCharset != null) {
            if (charBuffer == null) {
                charBuffer = new byte[lookBackSize];
            }
            if (charBufferLength == 0) {
                charBuffer[0] = (byte) b;
                charAsString = new String(charBuffer, 0, 1, sourceCharset);
                if (charAsString.charAt(0) == UNKNOWN) {
                    charBufferLength = 1;
                }
            }
            else {
                charBuffer[charBufferLength++] = (byte) b;
                charAsString = new String(charBuffer, 0, charBufferLength, sourceCharset);
                if (charAsString.charAt(0) == UNKNOWN) {
                    if (charAsString.length() > 1) {
                        charAsString = charAsString.substring(1);
                        charBufferLength = 0;
                    }
                    else {
                        if (charBuffer.length == charBufferLength) {
                            charBufferLength = 0;
                        }
                        charAsString = String.valueOf(UNKNOWN);
                    }
                }
                else {
                    charBufferLength = 0;
                }
            }
        }

        if (charAsString != null && charAsString.length() == 2 && Character.isSurrogate(charAsString.charAt(0))) {
            line = Arrays.copyOf(line, line.length + 1);
            line[charDumpOffset++] = charAsString.charAt(0);
            line[charDumpOffset++] = charAsString.charAt(1);
        }
        else {
            char ch = charAsString == null ? (char) b : charAsString.charAt(0);
            line[charDumpOffset++] = ch == UNKNOWN || Character.isISOControl(ch) ? '.' : ch;
        }
        lineOffset += 1;
        offset += 1;
    }

    @Override
    public void flush() throws IOException {
        if (line != null) {
            writer.write(line);
            writer.write(System.lineSeparator());
            writer.flush();
            line = null;
            offsetStringLength = 0;
            lineOffset = 0;
        }
    }

    @Override
    public void close() throws IOException {
        flush();
        writer.close();
        isClosed = true;
    }

    void setLookBackSize(int lookBackSize) {
        this.lookBackSize = lookBackSize;
        if (charBuffer != null) {
            charBuffer = Arrays.copyOf(charBuffer, lookBackSize);
        }
    }
}

