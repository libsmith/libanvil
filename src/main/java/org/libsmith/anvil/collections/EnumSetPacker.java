package org.libsmith.anvil.collections;


import javax.annotation.Nonnull;
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

    protected EnumSetPacker(@Nonnull Class<T> enumType) {
        this.enumType = enumType;
        this.universe = enumType.getEnumConstants();
    }

    public static <T extends Enum<T>> EnumSetPacker<T> of(@Nonnull Class<T> enumType) {
        return new EnumSetPacker<>(enumType);
    }

    public int getMaxWidth() {
        return universe.length;
    }

    public byte packToByte(@Nonnull Set<T> enumSet) {
        return (byte) packToPrimitive(enumSet, 8);
    }

    public short packToShort(@Nonnull Set<T> enumSet) {
        return (short) packToPrimitive(enumSet, 16);
    }

    public int packToInt(@Nonnull Set<T> enumSet) {
        return (int) packToPrimitive(enumSet, 32);
    }

    public long packToLong(@Nonnull Set<T> enumSet) {
        return packToPrimitive(enumSet, 64);
    }

    public long packToLongInexact(@Nonnull Set<T> enumSet) {
        return packToPrimitive(enumSet, Integer.MAX_VALUE);
    }

    protected long packToPrimitive(@Nonnull Set<T> enumSet, int maxOrdinal) {
        long packed = 0;
        for (T val : enumSet) {
            long ordinal = val.ordinal();
            if (ordinal >= maxOrdinal) {
                throw new ArithmeticException("Ordinal of " + val + " > " + maxOrdinal);
            }
            if (ordinal < 64) {
                packed |= 1L << ordinal;
            }
        }
        return packed;
    }

    public @Nonnull BigInteger packToBigInteger(@Nonnull Set<T> enumSet) {
        BigInteger packed = BigInteger.ZERO;
        for (T val : enumSet) {
            packed = packed.or(BigInteger.ONE.shiftLeft(val.ordinal()));
        }
        return packed;
    }

    public @Nonnull EnumSet<T> unpack(long bitSet) {
        return unpack(bitSet, false);
    }

    public @Nonnull EnumSet<T> unpackInexact(long bitSet) {
        return unpack(bitSet, true);
    }

    private @Nonnull EnumSet<T> unpack(long bitSet, boolean inexact) {
        EnumSet<T> set = EnumSet.noneOf(enumType);
        for (int ordinal = 0; bitSet != 0; bitSet = bitSet >>> 1, ordinal++) {
            if ((bitSet & 1) != 0) {
                add(set, ordinal, inexact);
            }
        }
        return set;
    }

    public @Nonnull EnumSet<T> unpack(@Nonnull BigInteger bitSet) {
        return unpack(bitSet, false);
    }

    public @Nonnull EnumSet<T> unpackInexact(@Nonnull BigInteger bitSet) {
        return unpack(bitSet, true);
    }

    private @Nonnull EnumSet<T> unpack(@Nonnull BigInteger bitSet, boolean inexact) {
        EnumSet<T> set = EnumSet.noneOf(enumType);
        for (int ordinal = 0, count = bitSet.bitLength(); ordinal < count; ordinal++) {
            if (bitSet.testBit(ordinal)) {
                add(set, ordinal, inexact);
            }
        }
        return set;
    }

    private void add(@Nonnull EnumSet<T> enumSet, int ordinal, boolean inexact) {
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
