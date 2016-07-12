package org.libsmith.anvil;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 21.03.16 3:10
 */
public class Holder<T> implements Supplier<T>, Consumer<T> {

    private T value;

    protected Holder()
    { }

    public static <T> Holder<T> of(T value) {
        Holder<T> holder = new Holder<>();
        holder.setValue(value);
        return holder;
    }

    public static <T> Holder<T> empty() {
        return new Holder<>();
    }

    public ThreadSafe<T> asThreadSafeHolder() {
        ThreadSafe<T> holder = new ThreadSafe<>();
        holder.setValue(value);
        return holder;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public <R> R apply(Function<? super T, R> function) {
        return function.apply(getValue());
    }

    @Override
    public void accept(T value) {
        setValue(value);
    }

    @Override
    public T get() {
        return getValue();
    }

    public static class ThreadSafe<T> extends Holder<T> {

        private volatile T value;

        protected ThreadSafe()
        { }

        @Override
        public void setValue(T value) {
            this.value = value;
        }

        @Override
        public T getValue() {
            return value;
        }
    }
}
