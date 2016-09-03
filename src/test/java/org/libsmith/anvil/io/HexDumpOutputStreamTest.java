package org.libsmith.anvil.io;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 04.09.16 0:52
 */
public class HexDumpOutputStreamTest {

    private ByteArrayOutputStream res;
    private HexDumpOutputStream hd;

    @Before
    public void before() {
        res = Mockito.spy(new ByteArrayOutputStream());
        hd = HexDumpOutputStream.to(res);
    }

    @Test
    public void genericTest() throws IOException {
        for (int i = 0; i < 256; i++) {
            hd.write(i);
        }
        hd.flush();
        assertThat(res.toString()).isEqualTo(
                "000000 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f ................\n" +
                "000010 10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e 1f ................\n" +
                "000020 20 21 22 23 24 25 26 27 28 29 2a 2b 2c 2d 2e 2f  !\"#$%&'()*+,-./\n" +
                "000030 30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e 3f 0123456789:;<=>?\n" +
                "000040 40 41 42 43 44 45 46 47 48 49 4a 4b 4c 4d 4e 4f @ABCDEFGHIJKLMNO\n" +
                "000050 50 51 52 53 54 55 56 57 58 59 5a 5b 5c 5d 5e 5f PQRSTUVWXYZ[\\]^_\n" +
                "000060 60 61 62 63 64 65 66 67 68 69 6a 6b 6c 6d 6e 6f `abcdefghijklmno\n" +
                "000070 70 71 72 73 74 75 76 77 78 79 7a 7b 7c 7d 7e 7f pqrstuvwxyz{|}~.\n" +
                "000080 80 81 82 83 84 85 86 87 88 89 8a 8b 8c 8d 8e 8f ................\n" +
                "000090 90 91 92 93 94 95 96 97 98 99 9a 9b 9c 9d 9e 9f ................\n" +
                "0000a0 a0 a1 a2 a3 a4 a5 a6 a7 a8 a9 aa ab ac ad ae af  ¡¢£¤¥¦§¨©ª«¬\u00AD®¯\n" +
                "0000b0 b0 b1 b2 b3 b4 b5 b6 b7 b8 b9 ba bb bc bd be bf °±²³´µ¶·¸¹º»¼½¾¿\n" +
                "0000c0 c0 c1 c2 c3 c4 c5 c6 c7 c8 c9 ca cb cc cd ce cf ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏ\n" +
                "0000d0 d0 d1 d2 d3 d4 d5 d6 d7 d8 d9 da db dc dd de df ÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞß\n" +
                "0000e0 e0 e1 e2 e3 e4 e5 e6 e7 e8 e9 ea eb ec ed ee ef àáâãäåæçèéêëìíîï\n" +
                "0000f0 f0 f1 f2 f3 f4 f5 f6 f7 f8 f9 fa fb fc fd fe ff ðñòóôõö÷øùúûüýþÿ\n");
    }

    @Test
    public void genericStressTest() throws IOException {
        hd.setSourceCharset(Charset.forName("UTF-8"));
        Random random = new Random(42);
        for (int i = 0; i < 50_000; i++) {
            hd.write(random.nextInt(255));
        }
        hd.setLookBackSize(2);
        for (int i = 0; i < 50_000; i++) {
            hd.write(random.nextInt(255));
        }
    }

    @Test
    public void genericCharsetTest() throws IOException {
        hd.setSourceCharset(Charset.forName("UTF-8"));
        assertThat(hd.getSourceCharset()).isEqualTo(Charset.forName("UTF-8"));
        for (int i = 0; i < 256; i++) {
            hd.write(i);
        }
        hd.flush();
        assertThat(res.toString()).isEqualTo(
                "000000 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f ................\n" +
                "000010 10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e 1f ................\n" +
                "000020 20 21 22 23 24 25 26 27 28 29 2a 2b 2c 2d 2e 2f  !\"#$%&'()*+,-./\n" +
                "000030 30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e 3f 0123456789:;<=>?\n" +
                "000040 40 41 42 43 44 45 46 47 48 49 4a 4b 4c 4d 4e 4f @ABCDEFGHIJKLMNO\n" +
                "000050 50 51 52 53 54 55 56 57 58 59 5a 5b 5c 5d 5e 5f PQRSTUVWXYZ[\\]^_\n" +
                "000060 60 61 62 63 64 65 66 67 68 69 6a 6b 6c 6d 6e 6f `abcdefghijklmno\n" +
                "000070 70 71 72 73 74 75 76 77 78 79 7a 7b 7c 7d 7e 7f pqrstuvwxyz{|}~.\n" +
                "000080 80 81 82 83 84 85 86 87 88 89 8a 8b 8c 8d 8e 8f ................\n" +
                "000090 90 91 92 93 94 95 96 97 98 99 9a 9b 9c 9d 9e 9f ................\n" +
                "0000a0 a0 a1 a2 a3 a4 a5 a6 a7 a8 a9 aa ab ac ad ae af ................\n" +
                "0000b0 b0 b1 b2 b3 b4 b5 b6 b7 b8 b9 ba bb bc bd be bf ................\n" +
                "0000c0 c0 c1 c2 c3 c4 c5 c6 c7 c8 c9 ca cb cc cd ce cf ................\n" +
                "0000d0 d0 d1 d2 d3 d4 d5 d6 d7 d8 d9 da db dc dd de df ................\n" +
                "0000e0 e0 e1 e2 e3 e4 e5 e6 e7 e8 e9 ea eb ec ed ee ef ................\n" +
                "0000f0 f0 f1 f2 f3 f4 f5 f6 f7 f8 f9 fa fb fc fd fe ff ................\n");
    }

    @Test
    public void cyrillicUTF8Test() throws IOException {
        hd.setSourceCharset(Charset.forName("UTF-8"));
        for (char c = '\u0410'; c < '\u0450'; c++) {
            hd.write(Character.toString(c).getBytes("UTF-8"));
        }
        hd.flush();
        assertThat(res.toString()).isEqualTo(
                "000000 d0 90 d0 91 d0 92 d0 93 d0 94 d0 95 d0 96 d0 97 .А.Б.В.Г.Д.Е.Ж.З\n" +
                "000010 d0 98 d0 99 d0 9a d0 9b d0 9c d0 9d d0 9e d0 9f .И.Й.К.Л.М.Н.О.П\n" +
                "000020 d0 a0 d0 a1 d0 a2 d0 a3 d0 a4 d0 a5 d0 a6 d0 a7 .Р.С.Т.У.Ф.Х.Ц.Ч\n" +
                "000030 d0 a8 d0 a9 d0 aa d0 ab d0 ac d0 ad d0 ae d0 af .Ш.Щ.Ъ.Ы.Ь.Э.Ю.Я\n" +
                "000040 d0 b0 d0 b1 d0 b2 d0 b3 d0 b4 d0 b5 d0 b6 d0 b7 .а.б.в.г.д.е.ж.з\n" +
                "000050 d0 b8 d0 b9 d0 ba d0 bb d0 bc d0 bd d0 be d0 bf .и.й.к.л.м.н.о.п\n" +
                "000060 d1 80 d1 81 d1 82 d1 83 d1 84 d1 85 d1 86 d1 87 .р.с.т.у.ф.х.ц.ч\n" +
                "000070 d1 88 d1 89 d1 8a d1 8b d1 8c d1 8d d1 8e d1 8f .ш.щ.ъ.ы.ь.э.ю.я\n");

        res.reset();
        hd.setLineSize(11);
        hd.write("123Т1е2с3т123".getBytes("UTF-8"));
        hd.flush();
        assertThat(res.toString()).isEqualTo(
                "000080 31 32 33 d0 a2 31 d0 b5 32 d1 81 123.Т1.е2.с\n" +
                "00008b 33 d1 82 31 32 33                3.т123     \n");
    }

    @Test
    public void cyrillicCP866Test() throws IOException {
        hd.setSourceCharset(Charset.forName("CP866"));
        for (char c = '\u0410'; c < '\u0450'; c++) {
            hd.write(Character.toString(c).getBytes("CP866"));
        }
        hd.flush();
        assertThat(res.toString()).isEqualTo(
                "000000 80 81 82 83 84 85 86 87 88 89 8a 8b 8c 8d 8e 8f АБВГДЕЖЗИЙКЛМНОП\n" +
                "000010 90 91 92 93 94 95 96 97 98 99 9a 9b 9c 9d 9e 9f РСТУФХЦЧШЩЪЫЬЭЮЯ\n" +
                "000020 a0 a1 a2 a3 a4 a5 a6 a7 a8 a9 aa ab ac ad ae af абвгдежзийклмноп\n" +
                "000030 e0 e1 e2 e3 e4 e5 e6 e7 e8 e9 ea eb ec ed ee ef рстуфхцчшщъыьэюя\n");
    }

    @Test
    public void surrogateTest() throws IOException {
        hd.setSourceCharset(Charset.forName("UTF-8"));
        hd.write("Пук\uD83D\uDCA9кyeah".getBytes("UTF-8"));
        hd.flush();
        assertThat(res.toString()).isEqualTo(
                "000000 d0 9f d1 83 d0 ba f0 9f 92 a9 d0 ba 79 65 61 68 .П.у.к...\uD83D\uDCA9.кyeah\n");
    }

    @Test
    public void lineSizeTest() throws IOException {
        hd.setLineSize(4);
        assertThat(hd.getLineSize()).isEqualTo(4);
        hd.write("abcdefghijk".getBytes("UTF-8"));
        hd.setLineSize(5);
        assertThat(hd.getLineSize()).isEqualTo(5);
        hd.write("kjihgfedcba".getBytes("UTF-8"));
        hd.flush();
        assertThat(res.toString()).isEqualTo(
                "000000 61 62 63 64 abcd\n" +
                "000004 65 66 67 68 efgh\n" +
                "000008 69 6a 6b    ijk \n" +

                "00000b 6b 6a 69 68 67 kjihg\n" +
                "000010 66 65 64 63 62 fedcb\n" +
                "000015 61             a    \n");
    }

    @Test
    public void groupSizeTest() throws IOException {
        hd.setGroupSize(2);
        assertThat(hd.getGroupSize()).isEqualTo(2);
        hd.write("abcdefghijklmnopqrst".getBytes("UTF-8"));
        hd.setLineSize(7);
        hd.write("abcdefghijklmnopqrst".getBytes("UTF-8"));
        hd.setGroupSize(0);
        assertThat(hd.getGroupSize()).isEqualTo(0);
        hd.write("abcdefghijklmnopqrst".getBytes("UTF-8"));
        hd.flush();
        assertThat(res.toString()).isEqualTo(
                "000000 6162 6364 6566 6768 696a 6b6c 6d6e 6f70 abcdefghijklmnop\n" +
                "000010 7172 7374                               qrst            \n" +
                "000014 6162 6364 6566 67 abcdefg\n" +
                "00001b 6869 6a6b 6c6d 6e hijklmn\n" +
                "000022 6f70 7172 7374    opqrst \n" +
                "000028 61626364656667 abcdefg\n" +
                "00002f 68696a6b6c6d6e hijklmn\n" +
                "000036 6f7071727374   opqrst \n");
    }

    @Test
    public void offsetTest() throws IOException {
        assertThat(hd.getOffset()).isEqualTo(0);
        hd.write("abcdef".getBytes("UTF-8"));
        hd.setOffset(0xFF);
        assertThat(hd.getOffset()).isEqualTo(0xFF);
        hd.write("abcdef".getBytes("UTF-8"));
        assertThat(hd.getOffset()).isEqualTo(0x105);
        hd.flush();
        assertThat(hd.getOffset()).isEqualTo(0x105);
        assertThat(res.toString()).isEqualTo(
                "000000 61 62 63 64 65 66                               abcdef          \n" +
                "0000ff 61 62 63 64 65 66                               abcdef          \n");

    }

    @Test
    public void illegalArgumentsTest() {

        assertThatThrownBy(() -> hd.setOffset(-1))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("-1");

        assertThatThrownBy(() -> hd.setGroupSize(-2))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("-2");

        assertThatThrownBy(() -> hd.setLineSize(0))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("0");
    }

    @Test
    public void closeTest() throws IOException {
        hd.write("abcd".getBytes("UTF-8"));
        assertThat(res.toString()).isEmpty();
        verify(res, never()).close();
        hd.close();
        verify(res, times(1)).close();
        assertThat(res.toString()).isEqualTo(
                "000000 61 62 63 64                                     abcd            \n");
        assertThatThrownBy(() -> hd.write(42)).isInstanceOf(IOException.class).hasMessage("Stream closed");
    }

}
