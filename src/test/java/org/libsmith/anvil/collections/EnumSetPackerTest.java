package org.libsmith.anvil.collections;


import org.junit.Test;
import org.libsmith.anvil.collections.MockEnums.*;

import java.math.BigInteger;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.libsmith.anvil.collections.MockEnums.LongLongEnum.*;

/**
 * @author Dmitriy Balakin <balakin@loyaltyplant.com>
 * @created 05.08.2016 23:04
 */
public class EnumSetPackerTest {

    @SuppressWarnings("serial")
    private static final Map<Number, Set<LongLongEnum>> STATIC_TEST_DATA =
            Collections.unmodifiableMap(new HashMap<Number, Set<LongLongEnum>>() {{
                put(0x00_00_00_00_00_00_00_00L, EnumSet.noneOf(LongLongEnum.class));
                put(0xFF_FF_FF_FF_FF_FF_FF_FFL, EnumSet.range(_0, _63));
                put(0x7F_FF_FF_FF_FF_FF_FF_FFL, EnumSet.range(_0, _62));
                put(0xFF_FF_FF_FF_FF_FF_FF_FEL, EnumSet.range(_1, _63));
                put(0x80_00_00_00_00_00_00_01L, EnumSet.of(_0, _63));
                put(0x00_80_04_22_81_00_00_81L, EnumSet.of(_0, _7, _55, _42, _24, _31, _33, _37));
                put(0x80_00_04_00_01_00_00_04L, EnumSet.of(_2, _24, _42, _63));

                put(new BigInteger("FF FF FF FF  FF FF FF FF  FF FF FF FF  FF FF FF FF".replaceAll(" ", ""), 16),
                    EnumSet.allOf(LongLongEnum.class));
                put(new BigInteger("80 00 00 00  00 04 04 00  00 00 04 00  00 00 00 14".replaceAll(" ", ""), 16),
                    EnumSet.of(_2, _4, _42, _74, _82, _127));
            }});

    @Test
    public void packToByteTest() throws Exception {
        assertThat(
                EnumSetPacker.of(ByteEnum.class).packToByte(EnumSet.allOf(ByteEnum.class))
        ).isEqualTo((byte) -1);

        assertThatThrownBy(
                () -> EnumSetPacker.of(LongLongEnum.class).packToByte(EnumSet.of(_2, _4, _8))
        ).isInstanceOf(ArithmeticException.class)
         .hasMessageContaining("8");
    }

    @Test
    public void packToShortTest() throws Exception {
        assertThat(
                EnumSetPacker.of(ShortEnum.class).packToShort(EnumSet.allOf(ShortEnum.class))
        ).isEqualTo((short) -1);

        assertThatThrownBy(
                () -> EnumSetPacker.of(LongLongEnum.class).packToShort(EnumSet.of(_2, _4, _8, _16))
        ).isInstanceOf(ArithmeticException.class)
         .hasMessageContaining("16");
    }

    @Test
    public void packToIntTest() throws Exception {
        assertThat(
                EnumSetPacker.of(IntEnum.class).packToInt(EnumSet.allOf(IntEnum.class))
        ).isEqualTo(-1);

        assertThatThrownBy(
                () -> EnumSetPacker.of(LongLongEnum.class).packToInt(EnumSet.of(_2, _4, _8, _16, _32))
        ).isInstanceOf(ArithmeticException.class)
         .hasMessageContaining("32");
    }

    @Test
    public void packToLongTest() throws Exception {
        assertThat(
                EnumSetPacker.of(LongEnum.class).packToLong(EnumSet.allOf(LongEnum.class))
        ).isEqualTo(-1);

        STATIC_TEST_DATA.entrySet().stream().filter(e -> e.getKey() instanceof Long).forEach(
                e -> assertThat(EnumSetPacker.of(LongLongEnum.class).packToLong(e.getValue()))
                        .as("0x%016x must be pack value from %s", e.getKey(), e.getValue())
                        .isEqualTo(e.getKey()));

        assertThatThrownBy(
                () -> EnumSetPacker.of(LongLongEnum.class).packToLong(EnumSet.of(_2, _4, _8, _16, _32, _64))
        ).isInstanceOf(ArithmeticException.class)
         .hasMessageContaining("64");
    }

    @Test
    public void packToBigIntegerTest() throws Exception {

        STATIC_TEST_DATA.forEach(
                (k, v) -> assertThat(EnumSetPacker.of(LongLongEnum.class).packToBigInteger(v))
                          .as("0x%016x must be pack value from %s", k, v)
                          .isEqualTo(k instanceof BigInteger ? k : new BigInteger(Long.toHexString(k.longValue()), 16)));
    }

    @Test
    public void unpackTest() throws Exception {

        STATIC_TEST_DATA.entrySet().stream().filter(e -> e.getKey() instanceof Long).forEach(
                e -> assertThat(EnumSetPacker.of(LongLongEnum.class).unpack(e.getKey().longValue()))
                        .as("0x%016x must be expands to %s", e.getKey(), e.getValue())
                        .isEqualTo(e.getValue()));

        assertThatThrownBy(() -> EnumSetPacker.of(IntEnum.class).unpack(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32");
    }

    @Test
    public void unpackInexactTest() throws Exception {
        assertThatThrownBy(() -> EnumSetPacker.of(IntEnum.class).unpack(-1L))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(EnumSetPacker.of(IntEnum.class).unpackInexact(-1L))
                .isEqualTo(EnumSet.allOf(IntEnum.class));
    }

    @Test
    public void unpackFromBigIntegerTest() throws Exception {
        STATIC_TEST_DATA.forEach(
                (k, v) -> assertThat(EnumSetPacker.of(LongLongEnum.class).unpack(
                            k instanceof BigInteger ? (BigInteger) k
                                                    : new BigInteger(Long.toHexString(k.longValue()), 16)))
                        .as("0x%016x must be expands to %s", k, v)
                        .isEqualTo(v));

        assertThatThrownBy(() -> EnumSetPacker.of(IntEnum.class).unpack(new BigInteger(Long.toHexString(-1L), 16)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32");
    }

    @Test
    public void unpackInexactFromBigIntegerTest() throws Exception {
        assertThatThrownBy(() -> EnumSetPacker.of(IntEnum.class).unpack(new BigInteger(Long.toHexString(-1L), 16)))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(EnumSetPacker.of(IntEnum.class).unpackInexact(new BigInteger(Long.toHexString(-1L), 16)))
                .isEqualTo(EnumSet.allOf(IntEnum.class));
    }
}
