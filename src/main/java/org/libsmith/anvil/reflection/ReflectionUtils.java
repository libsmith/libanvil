package org.libsmith.anvil.reflection;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 17.06.2014 23:13
 */
public final class ReflectionUtils {

    @SuppressWarnings("unchecked")
    public static @Nonnull <T> Class<T> extractClass(@Nonnull Type type) {
        if (type instanceof ParameterizedType) {
            return (Class<T>) ((ParameterizedType) type).getRawType();
        }
        else if (type instanceof Class) {
            return (Class<T>) type;
        }
        throw new IllegalArgumentException(String.valueOf(type));
    }

    public static @Nonnull Type extractWildcardType(@Nonnull Type type) {
        if (type instanceof WildcardType) {
            WildcardType wt = (WildcardType) type;
            if (wt.getLowerBounds().length == 1) {
                return wt.getLowerBounds()[0];
            }
            else if (wt.getUpperBounds().length == 1) {
                return wt.getUpperBounds()[0];
            }
        }
        return type;
    }

    public static <T extends Annotation, E extends Enum<?>> T getEnumAnnotation(E value, Class<T> annotationType) {
        try {
            return value.getClass().getField(value.name()).getAnnotation(annotationType);
        }
        catch (NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> uncheckedClassCast(Class<?> type) {
        return (Class<T>) type;
    }

    public static String getLessSimpleName(Class<?> type) {
        StringBuilder stringBuilder = new StringBuilder();
        do {
            if (stringBuilder.length() != 0) {
                stringBuilder.insert(0, "$");
            }
            stringBuilder.insert(0, type.getSimpleName());
            type = type.getEnclosingClass();
        }
        while (type != null);
        return stringBuilder.toString();
    }
}
