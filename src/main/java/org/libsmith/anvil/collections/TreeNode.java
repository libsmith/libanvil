package org.libsmith.anvil.collections;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 01.06.2015 21:14
 */
public class TreeNode<T> implements Iterable<TreeNode<T>>, Supplier<T>, Consumer<T> {
    private T value;
    private TreeNode<T> parent;
    private List<TreeNode<T>> childrens = new LinkedList<>();

    public TreeNode(T value) {
        this.value = value;
        this.parent = null;
    }

    public TreeNode(T value, TreeNode<T> parent) {
        this.value = value;
        this.parent = parent;
    }

    public TreeNode<T> addChild(T value) {
        TreeNode<T> treeNode = new TreeNode<>(value, this);
        childrens.add(treeNode);
        return treeNode;
    }

    public TreeNode<T> addChild(TreeNode<T> node) {
        if (node.parent != null) {
            throw new IllegalStateException();
        }
        node.parent = this;
        childrens.add(node);
        return node;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void accept(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public TreeNode<T> getParent() {
        return parent;
    }

    public int size() {
        return childrens.size();
    }

    @Override
    public Iterator<TreeNode<T>> iterator() {
        return new Iterator<TreeNode<T>>() {
            private Iterator<TreeNode<T>> delegate = childrens.iterator();
            private TreeNode<T> next;

            @Override
            public boolean hasNext() {
                next = null;
                return delegate.hasNext();
            }

            @Override
            public TreeNode<T> next() {
                return next = delegate.next();
            }

            @Override
            public void remove() {
                delegate.remove();
                next.parent = null;
            }
        };
    }
}

