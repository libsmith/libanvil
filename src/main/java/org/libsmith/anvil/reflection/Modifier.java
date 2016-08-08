package org.libsmith.anvil.reflection;

import com.sun.istack.internal.NotNull;
import org.libsmith.anvil.collections.EnumSetPacker;

import java.lang.reflect.Member;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 06.08.16 17:25
 */
@SuppressWarnings("unused")
public enum Modifier {

    INTERFACE(java.lang.reflect.Modifier.INTERFACE),

    PRIVATE(java.lang.reflect.Modifier.PRIVATE),
    PROTECTED(java.lang.reflect.Modifier.PROTECTED),
    PUBLIC(java.lang.reflect.Modifier.PUBLIC),

    STATIC(java.lang.reflect.Modifier.STATIC),
    FINAL(java.lang.reflect.Modifier.FINAL),
    ABSTRACT(java.lang.reflect.Modifier.ABSTRACT),

    VOLATILE(java.lang.reflect.Modifier.VOLATILE),
    SYNCHRONIZED(java.lang.reflect.Modifier.SYNCHRONIZED),

    TRANSIENT(java.lang.reflect.Modifier.TRANSIENT),
    NATIVE(java.lang.reflect.Modifier.NATIVE),
    STRICT(java.lang.reflect.Modifier.STRICT);


    static final EnumSetPacker<Modifier> PACKER =
            EnumSetPacker.of(Modifier.class)
                         .mapOrdinal(v -> Integer.numberOfTrailingZeros(v.getMask()));

    public static final Set<Modifier> INTERFACE_MODIFIERS = Collections.unmodifiableSet(
            PACKER.unpackInexact(java.lang.reflect.Modifier.interfaceModifiers()));

    public static final Set<Modifier> CLASS_MODIFIERS = Collections.unmodifiableSet(
            PACKER.unpackInexact(java.lang.reflect.Modifier.classModifiers()));

    public static final Set<Modifier> FIELD_MODIFIERS = Collections.unmodifiableSet(
            PACKER.unpackInexact(java.lang.reflect.Modifier.fieldModifiers()));

    public static final Set<Modifier> CONSTRUCTOR_MODIFIERS = Collections.unmodifiableSet(
            PACKER.unpackInexact(java.lang.reflect.Modifier.constructorModifiers()));

    public static final Set<Modifier> METHOD_MODIFIERS = Collections.unmodifiableSet(
            PACKER.unpackInexact(java.lang.reflect.Modifier.methodModifiers()));

    public static final Set<Modifier> PARAMETER_MODIFIERS = Collections.unmodifiableSet(
            PACKER.unpackInexact(java.lang.reflect.Modifier.parameterModifiers()));

    private final int mask;

    Modifier(int mask) {
        this.mask = mask;
    }

    public int getMask() {
        return mask;
    }

    public boolean presentIn(int packed) {
        return (packed & getMask()) != 0;
    }

    public boolean presentIn(Member member) {
        return presentIn(member.getModifiers());
    }

    public boolean notPresentIn(int packed) {
        return (packed & getMask()) == 0;
    }

    public boolean notPresentIn(Member member) {
        return notPresentIn(member.getModifiers());
    }

    public static @NotNull EnumSet<Modifier> unpack(int packed) {
        return PACKER.unpack(packed);
    }

    public static int pack(@NotNull Modifier ... set) {
        return PACKER.packToInt(set);
    }

    public static int pack(@NotNull Set<Modifier> set) {
        return PACKER.packToInt(set);
    }

    public static EnumSet<Modifier> parse(String modifiers) {
        EnumSet<Modifier> enumSet = EnumSet.noneOf(Modifier.class);
        String[] split = modifiers.split(" ");
        for (String modifier : split) {
            modifier = modifier.trim();
            if (!modifier.isEmpty()) {
                enumSet.add(valueOf(modifier.toUpperCase()));
            }
        }
        return enumSet;
    }
}
