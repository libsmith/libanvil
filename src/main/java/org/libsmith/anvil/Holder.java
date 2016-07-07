package org.libsmith.anvil;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 21.03.16 3:10
 */
public class Holder<T> implements Supplier<T>, Consumer<T>{
    private T value;

    protected Holder()
    { }

    public static <T> Holder<T> of(T value) {
        Holder<T> holder = new Holder<>();
        holder.setValue(value);
        return holder;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean hasValue() {
        return getValue() != null;
    }

    public <R> Optional<R> apply(Function<? super T, ? extends R> function) {
        return hasValue() ? Optional.of(function.apply(getValue())) : Optional.empty();
    }

    @Override
    public void accept(T value) {
        setValue(value);
    }

    @Override
    public T get() {
        return getValue();
    }
}
