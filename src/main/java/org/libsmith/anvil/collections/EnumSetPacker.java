package org.libsmith.anvil.collections;


import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

/**
 * @author Dmitriy Balakin <balakin@0x0000.ru>
 * @created 05.08.2016 22:50
 */
public class EnumSetPacker<T extends Enum<T>> {

    private final Class<T> enumType;
    private final T[] universe;

    protected EnumSetPacker(@Nonnull Class<T> enumType, @Nonnull T[] universe) {
        this.enumType = enumType;
        this.universe = universe;
    }

    public static <T extends Enum<T>> EnumSetPacker<T> of(@Nonnull Class<T> enumType) {
        return new EnumSetPacker<>(enumType, enumType.getEnumConstants());
    }

    public EnumSetPacker<T> mapOrdinal(Function<T, Integer> mapFunction) {
        T[] mappedUniverse = Arrays.copyOf(universe, universe.length);
        Map<T, Integer> map = new HashMap<>();
        for (T value : universe) {
            Integer ordinal = mapFunction.apply(value);
            map.put(value, ordinal);
            mappedUniverse[ordinal] = value;
        }
        return new EnumSetPacker<T>(enumType, mappedUniverse) {
            @Override
            protected int getOrdinal(T val) {
                return map.get(val);
            }
        };
    }

    public int getMaxWidth() {
        return universe.length;
    }

    @SuppressWarnings("unchecked")
    void ensureThatWidthSatisfySizeOf(Class<?> ... types) {
        for (Class<?> type : types) {
            ensureThatWidthSatisfySizeOf((Class) type);
        }
    }

    public void ensureThatWidthSatisfySizeOf(Class<? extends Number> type) {
        if (getMaxWidth() > sizeOf(type)) {
            throw new IllegalStateException("Size of enum " + enumType.getCanonicalName() +
                                            " (" + universe.length + ")" +
                                            " does not fit to pack into " + type.getSimpleName());
        }
    }

    public byte packToByte(@Nonnull Set<T> enumSet) {
        return (byte) packToPrimitive(enumSet, Byte.SIZE);
    }

    public short packToShort(@Nonnull Set<T> enumSet) {
        return (short) packToPrimitive(enumSet, Short.SIZE);
    }

    public int packToInt(@Nonnull Set<T> enumSet) {
        return (int) packToPrimitive(enumSet, Integer.SIZE);
    }

    public long packToLong(@Nonnull Set<T> enumSet) {
        return packToPrimitive(enumSet, Long.SIZE);
    }

    public long packToLongInexact(@Nonnull Set<T> enumSet) {
        return packToPrimitive(enumSet, Integer.MAX_VALUE);
    }

    @SafeVarargs
    public final byte packToByte(@Nonnull T ... enumSet) {
        return (byte) packToPrimitive(enumSet, Byte.SIZE);
    }

    @SafeVarargs
    public final short packToShort(@Nonnull T ... enumSet) {
        return (short) packToPrimitive(enumSet, Short.SIZE);
    }

    @SafeVarargs
    public final int packToInt(@Nonnull T ... enumSet) {
        return (int) packToPrimitive(enumSet, Integer.SIZE);
    }

    @SafeVarargs
    public final long packToLong(@Nonnull T ... enumSet) {
        return packToPrimitive(enumSet, Long.SIZE);
    }

    @SafeVarargs
    public final long packToLongInexact(@Nonnull T... enumSet) {
        return packToPrimitive(enumSet, Integer.MAX_VALUE);
    }

    protected long packToPrimitive(@Nonnull Set<T> enumSet, int maxOrdinal) {
        long packed = 0;
        for (T val : enumSet) {
            long ordinal = getOrdinal(val);
            if (ordinal >= maxOrdinal) {
                throw new ArithmeticException("Ordinal of " + val + " > " + maxOrdinal);
            }
            if (ordinal < Long.SIZE) {
                packed |= 1L << ordinal;
            }
        }
        return packed;
    }

    protected long packToPrimitive(@Nonnull T[] enumSet, int maxOrdinal) {
        long packed = 0;
        for (T val : enumSet) {
            long ordinal = getOrdinal(val);
            if (ordinal >= maxOrdinal) {
                throw new ArithmeticException("Ordinal of " + val + " > " + maxOrdinal);
            }
            if (ordinal < Long.SIZE) {
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
        T[] universe = this.universe;
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

    int getOrdinal(T val) {
        return val.ordinal();
    }

    private static int sizeOf(Class<? extends Number> type) {
        if (type == Byte.class    || type == byte.class)  { return Byte.SIZE; }
        if (type == Short.class   || type == short.class) { return Short.SIZE; }
        if (type == Integer.class || type == int.class)   { return Integer.SIZE; }
        if (type == Long.class    || type == long.class)  { return Long.SIZE; }
        if (BigInteger.class.isAssignableFrom(type))      { return Integer.MAX_VALUE; }
        return -1;
    }
}
