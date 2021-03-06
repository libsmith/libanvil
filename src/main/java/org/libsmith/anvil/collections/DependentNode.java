package org.libsmith.anvil.collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 20.07.16 22:52
 */
public interface DependentNode<T extends DependentNode<T>> {

    default @Nullable Collection<T> getDependencies() {
        return Collections.emptySet();
    }

    default @Nonnull Collection<T> getAllDependencies() {
        Set<T> dependencies = new LinkedHashSet<>();
        Collection<T> row = this.getDependencies();
        while (row != null && !row.isEmpty()) {
            Collection<T> newRow = new ArrayList<>();
            for (T dep : row) {
                if (!dep.equals(this) && dependencies.add(dep)) {
                    Collection<T> depOfDep = dep.getDependencies();
                    if (depOfDep != null) {
                        newRow.addAll(depOfDep);
                    }
                }
            }
            row = newRow;
        }
        return dependencies;
    }

    static <T extends DependentNode<T>> void dependentSort(List<T> dependentGraph) {
        dependentSort(dependentGraph, true, true);
    }

    static <T extends DependentNode<T>> void dependentSort(List<T> dependentGraph,
                                                           boolean circularProhibited,
                                                           boolean missingProhibited) {

        Set<T> visited = new HashSet<>();
        List<T> result = new ArrayList<>();
        Consumer<T> dfs = new Consumer<T>() {
            @Override
            public void accept(T element) {
                if (circularProhibited) {
                    List<T> chain = element.detectCircularDependency();
                    if (chain != null) {
                        throw new CircularDependencyException(chain);
                    }
                }
                visited.add(element);
                Collection<T> dependencies = element.getDependencies();
                if (dependencies != null) {
                    dependencies.stream()
                                .filter(v -> !visited.contains(v))
                                .forEach(this);
                }
                result.add(element);
            }
        };
        dependentGraph.stream()
                      .filter(element -> !visited.contains(element))
                      .forEach(dfs);
        if (result.retainAll(dependentGraph)) {
            if (missingProhibited) {
                throw new NoSuchElementException("Dependency graph has dependencies that " +
                                                 "not contains in the original list");
            }
        }
        Collections.copy(dependentGraph, result);
    }

    default @Nullable List<T> detectCircularDependency() {

        Set<DependentNode<T>> visited = new HashSet<>();
        visited.add(this);
        Collection<LinkedNode<T>> row = LinkedNode.linkNodes(getDependencies(), null);
        while (row != null && !row.isEmpty()) {
            List<LinkedNode<T>> newRow = new ArrayList<>();
            for (LinkedNode<T> node : row) {
                T value = node.getValue();
                if (!visited.contains(value)) {
                    visited.add(value);
                    LinkedNode.linkNodes(value.getDependencies(), node, newRow);
                }
                else if (value.equals(this)) {
                    List<T> list = new ArrayList<>();
                    node.parentStream().forEach(list::add);
                    list.add(value);
                    Collections.reverse(list);
                    return list;
                }
            }
            row = newRow;
        }
        return null;
    }

    class CircularDependencyException extends IllegalArgumentException {

        private static final long serialVersionUID = 7654252441233629712L;

        private final List<?> chain;

        public <T extends DependentNode<T>> CircularDependencyException(String message, List<? extends T> chain) {
            super(message);
            this.chain = Collections.unmodifiableList(chain);
        }

        public <T extends DependentNode<T>> CircularDependencyException(List<? extends T> chain) {
            this("Detected circular dependency through chain '" + chain.stream().map(Objects::toString)
                                                                       .collect(Collectors.joining("' -> '")) + "'",
                 chain);
        }

        @SuppressWarnings("unchecked")
        public <T extends DependentNode<T>> List<T> getChain() {
            return (List<T>) chain;
        }
    }
}
