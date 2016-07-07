package org.libsmith.anvil.text;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 21.03.16 2:54
 */
public class Strings {

    public static boolean isEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.toString().isEmpty();
    }

    public static boolean isEmpty(Supplier<? extends CharSequence> supplier) {
        CharSequence val = supplier == null ? null : supplier.get();
        return val == null || val.toString().isEmpty();
    }

    public static <T extends CharSequence> boolean isEmpty(LazyCharSequence<T> supplier) {
        return isEmpty((Supplier<T>) supplier);
    }

    public static boolean isNotEmpty(CharSequence string) {
        return string != null && !string.toString().isEmpty();
    }

    public static boolean isNotEmpty(Supplier<? extends CharSequence> supplier) {
        CharSequence val = supplier == null ? null : supplier.get();
        return val != null && !val.toString().isEmpty();
    }

    public static <T extends CharSequence> boolean isNotEmpty(LazyCharSequence<T> supplier) {
        return isNotEmpty((Supplier<T>) supplier);
    }

    public static boolean isBlank(CharSequence charSequence) {
        if (charSequence != null) {
            for (int p = 0, l = charSequence.length(); p < l; p++) {
                if (!Character.isWhitespace(charSequence.charAt(p))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isBlank(Supplier<? extends CharSequence> supplier) {
        CharSequence val = supplier == null ? null : supplier.get();
        return isBlank(val);
    }

    public static <T extends CharSequence> boolean isBlank(LazyCharSequence<T> supplier) {
        return isBlank((Supplier<T>) supplier);
    }

    public static boolean isNotBlank(CharSequence charSequence) {
        return !isBlank(charSequence);
    }

    public static boolean isNotBlank(Supplier<? extends CharSequence> supplier) {
        CharSequence val = supplier == null ? null : supplier.get();
        return !isBlank(val);
    }

    public static <T extends CharSequence> boolean isNotBlank(LazyCharSequence<T> supplier) {
        return !isBlank((Supplier<T>) supplier);
    }

    public static <T extends CharSequence> LazyCharSequence<T> lazy(Supplier<T> supplier) {
        return new LazyCharSequence<>(supplier);
    }

    public static class LazyCharSequence<T extends CharSequence> implements Supplier<T>, CharSequence {

        private final Supplier<T> supplier;

        private T value;

        protected LazyCharSequence(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public int length() {
            return get().length();
        }

        @Override
        public char charAt(int index) {
            return get().charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return get().subSequence(start, end);
        }

        @Override
        public T get() {
            return value == null ? value = supplier.get() : value;
        }

        @Override
        public @Nonnull String toString() {
            return get().toString();
        }
    }
}
