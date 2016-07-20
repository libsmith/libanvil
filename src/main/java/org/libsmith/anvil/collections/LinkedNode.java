package org.libsmith.anvil.collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 20.07.16 22:59
 */
public class LinkedNode<T> {

    private final T value;
    private final LinkedNode<T> parent;

    public LinkedNode(T value) {
        this(value, null);
    }

    public LinkedNode(T value, LinkedNode<T> parent) {
        this.value = value;
        this.parent = parent;
    }

    public T getValue() {
        return value;
    }

    public LinkedNode<T> getParent() {
        return parent;
    }

    public Iterator<T> parentIterator() {

        return new Iterator<T>() {

            private LinkedNode<T> next = LinkedNode.this;

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public T next() {
                if (next == null) {
                    throw new NoSuchElementException();
                }
                T value = next.getValue();
                next = next.getParent();
                return value;
            }
        };
    }

    public Stream<T> parentStream() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(parentIterator(), Spliterator.ORDERED |
                                                                      Spliterator.IMMUTABLE), false);
    }

    static <T> List<LinkedNode<T>> linkNodes(@Nullable Collection<T> nodes, @Nullable LinkedNode<T> parent) {
        if (nodes == null) {
            return null;
        }
        ArrayList<LinkedNode<T>> list = new ArrayList<>(nodes.size());
        linkNodes(nodes, parent, list);
        return list;
    }

    static <T> void linkNodes(@Nullable Collection<T> nodes, @Nullable LinkedNode<T> parent,
                              @Nonnull Collection<LinkedNode<T>> dest) {
        if (nodes == null) {
            return;
        }
        for (T node : nodes) {
            dest.add(new LinkedNode<>(node, parent));
        }
    }
}
