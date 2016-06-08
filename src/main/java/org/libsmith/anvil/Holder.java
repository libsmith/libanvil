package org.libsmith.anvil;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 21.03.16 3:10
 */
public class Holder<T> {
    private T value;

    public Holder()
    { }

    public Holder(T value) {
        setValue(value);
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
}
