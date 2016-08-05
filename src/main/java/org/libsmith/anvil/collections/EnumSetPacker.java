package org.libsmith.anvil.collections;


import java.math.BigInteger;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author Dmitriy Balakin <balakin@0x0000.ru>
 * @created 05.08.2016 22:50
 */
public class EnumSetPacker<T extends Enum<T>> {

    private final Class<T> enumType;
    private final T[] universe;

    protected EnumSetPacker(Class<T> enumType) {
        this.enumType = enumType;
        this.universe = enumType.getEnumConstants();
    }

    public static <T extends Enum<T>> EnumSetPacker<T> of(Class<T> enumType) {
        return new EnumSetPacker<>(enumType);
    }

    public byte packToByte(Set<T> enumSet) {
        return (byte) packToPrimitive(enumSet, 7);
    }

    public short packToShort(Set<T> enumSet) {
        return (short) packToPrimitive(enumSet, 15);
    }

    public int packToInt(Set<T> enumSet) {
        return (int) packToPrimitive(enumSet, 31);
    }

    public long packToLong(Set<T> enumSet) {
        return packToPrimitive(enumSet, 63);
    }

    protected long packToPrimitive(Set<T> enumSet, int maxOrdinal) {
        long packed = 0;
        for (T val : enumSet) {
            long ordinal = val.ordinal();
            if (ordinal > maxOrdinal) {
                throw new ArithmeticException("Ordinal of " + val + " > " + maxOrdinal);
            }
            packed |= 1L << ordinal;
        }
        return packed;
    }

    public BigInteger packToBigInteger(Set<T> enumSet) {
        BigInteger packed = BigInteger.ZERO;
        for (T val : enumSet) {
            packed = packed.or(BigInteger.ONE.shiftLeft(val.ordinal()));
        }
        return packed;
    }

    public EnumSet<T> unpack(long bitSet) {
        return unpack(bitSet, false);
    }

    public EnumSet<T> unpackInexact(long bitSet) {
        return unpack(bitSet, true);
    }

    private EnumSet<T> unpack(long bitSet, boolean inexact) {
        EnumSet<T> set = EnumSet.noneOf(enumType);
        for (int ordinal = 0; bitSet != 0; bitSet = bitSet >>> 1, ordinal++) {
            if ((bitSet & 1) != 0) {
                add(set, ordinal, inexact);
            }
        }
        return set;
    }

    public EnumSet<T> unpack(BigInteger bitSet) {
        return unpack(bitSet, false);
    }

    public EnumSet<T> unpackInexact(BigInteger bitSet) {
        return unpack(bitSet, true);
    }

    private EnumSet<T> unpack(BigInteger bitSet, boolean inexact) {
        EnumSet<T> set = EnumSet.noneOf(enumType);
        for (int ordinal = 0, count = bitSet.bitLength(); ordinal < count; ordinal++) {
            if (bitSet.testBit(ordinal)) {
                add(set, ordinal, inexact);
            }
        }
        return set;
    }

    private void add(EnumSet<T> enumSet, int ordinal, boolean inexact) {
        if (ordinal >= universe.length) {
            if (!inexact) {
                throw new IllegalArgumentException("No value at ordinal " + ordinal + " for enum " +
                                                   enumType.getCanonicalName());
            }
        }
        else {
            enumSet.add(universe[ordinal]);
        }
    }
}
