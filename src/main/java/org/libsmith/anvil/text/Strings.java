package org.libsmith.anvil.text;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
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

    public static LazyCharSequence<String> lazy(CharSequence charSequence) {
        return new LazyCharSequence<>(() -> charSequence == null ? null : charSequence.toString());
    }

    public static LazyCharSequence<String> lazy(CharSequence pattern, Object ... arguments) {
        return new LazyCharSequence<>(() -> pattern == null ? null
                                                            : MessageFormat.format(pattern.toString(), arguments));
    }

    public static LazyStringBuilder lazyStringBuilder() {
        return new LazyStringBuilder();
    }

    public static class LazyStringBuilder implements Appendable, CharSequence {

        private StringBuilder stringBuilder = new StringBuilder();
        private List<Supplier<?>> suppliers = new ArrayList<>();

        protected LazyStringBuilder()
        { }

        public LazyStringBuilder append(LazyCharSequence<?> supplier) {
            return append((Supplier<?>) supplier);
        }

        public LazyStringBuilder append(Supplier<?> supplier) {
            suppliers.add(supplier == null ? () -> "null" : supplier);
            return this;
        }

        public LazyStringBuilder append(Object object) {
            return append(() -> String.valueOf(object));
        }

        @Override
        public LazyStringBuilder append(CharSequence csq) throws IOException {
            suppliers.add(() -> csq);
            return this;
        }

        @Override
        public LazyStringBuilder append(CharSequence csq, int start, int end) throws IOException {
            suppliers.add(() -> (csq == null ? "null" : csq).subSequence(start, end));
            return this;
        }

        @Override
        public LazyStringBuilder append(char c) throws IOException {
            suppliers.add(() -> c);
            return this;
        }

        @Override
        public int length() {
            return build().length();
        }

        @Override
        public char charAt(int index) {
            return build().charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return build().subSequence(start, end);
        }

        @Override
        public @Nonnull String toString() {
            return build().toString();
        }

        private StringBuilder build() {
            suppliers.stream().map(Supplier::get).forEach(stringBuilder::append);
            return stringBuilder;
        }
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
