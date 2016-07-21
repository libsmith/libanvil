package org.libsmith.anvil.collections;

import org.junit.Test;
import org.libsmith.anvil.collections.DependentNode.CircularDependencyException;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 20.07.16 22:54
 */
public class DependentNodeTest {

    private final DependentString abc = new DependentString("abc");
    private final DependentString def = new DependentString("def");
    private final DependentString ghi = new DependentString("ghi");
    private final DependentString jkl = new DependentString("jkl");
    private final DependentString mno = new DependentString("mno");
    private final DependentString pqrs = new DependentString("pqrs");
    private final DependentString tuv = new DependentString("tuv");
    private final DependentString wxyz = new DependentString("wxyz");

    private final List<DependentString> graph = Arrays.asList(
            abc, def, ghi, jkl, mno, pqrs, tuv, wxyz
    );

    @Test
    public void passthruSort() {
        List<DependentString> toBeSorted = new ArrayList<>(graph);
        DependentNode.dependentSort(toBeSorted);
        assertThat(toBeSorted).isEqualTo(graph);
    }

    @Test
    public void plainSort() {
        List<DependentString> toBeSorted = new ArrayList<>(graph);
        for (int i = 1; i < toBeSorted.size(); i++) {
            toBeSorted.get(i).addDependency(toBeSorted.get(i - 1));
        }
        do {
            Collections.shuffle(toBeSorted);
        }
        while (toBeSorted.equals(graph));
        assertThat(toBeSorted).isNotEqualTo(graph);
        DependentNode.dependentSort(toBeSorted);
        assertThat(toBeSorted).isEqualTo(graph);
    }

    @Test
    public void constantSort() {
        def.addDependency(wxyz);
        wxyz.addDependency(jkl).addDependency(pqrs);
        pqrs.addDependency(jkl);
        DependentNode.dependentSort(graph);
        assertThat(graph).containsExactly(abc, jkl, pqrs, wxyz, def, ghi, mno, tuv);
    }

    @Test
    public void circularDependencyPassTest() {
        abc.addDependency(def);
        def.addDependency(ghi).addDependency(jkl);
        jkl.addDependency(ghi);
        assertThat(abc.detectCircularDependency()).isNull();
    }

    @Test
    public void circularDependencyLoopPassTest() {
        abc.addDependency(def);
        def.addDependency(jkl);
        jkl.addDependency(def);
        assertThat(abc.detectCircularDependency()).isNull();
    }

    @Test
    public void circularDependencyTest() {
        abc.addDependency(def);
        def.addDependency(jkl).addDependency(mno);
        jkl.addDependency(def);
        mno.addDependency(abc);
        assertThat(abc.detectCircularDependency()).containsExactly(abc, def, mno, abc);
    }

    @Test
    public void circularDependencySortTest() {
        List<DependentString> circularChain = Arrays.asList(abc, def, abc);

        abc.addDependency(def);
        def.addDependency(abc);

        assertThatThrownBy(
                () -> DependentNode.dependentSort(graph))
                        .isInstanceOf(CircularDependencyException.class)
                        .hasMessageContaining("abc")
                        .hasMessageContaining("def")
                        .matches(t -> ((CircularDependencyException) t).getChain()
                                                                       .equals(circularChain),
                                 "Dependency circular chain assertion failure, must be " + circularChain);
    }

    @Test
    public void getAllDependenciesTest() throws Exception {
        abc.addDependency(def);
        def.addDependency(ghi).addDependency(jkl);
        assertThat(abc.getAllDependencies()).containsExactly(def, ghi, jkl);
    }

    @Test
    public void getAllDependenciesWithLoopingTest() throws Exception {
        abc.addDependency(def).addDependency(mno);
        def.addDependency(pqrs).addDependency(jkl);
        jkl.addDependency(abc);
        mno.addDependency(def);
        assertThat(abc.getAllDependencies()).containsExactly(def, mno, pqrs, jkl);
    }

    private static class DependentString implements DependentNode<DependentString> {

        private final String string;
        private List<DependentString> dependencies;

        public DependentString(String string) {
            this.string = string;
        }

        public DependentString addDependency(DependentString dependentString) {
            if (dependencies == null) {
                dependencies = new ArrayList<>();
            }
            dependencies.add(dependentString);
            return this;
        }

        @Override
        public Collection<DependentString> getDependencies() {
            return dependencies;
        }

        @Override
        public String toString() {
            return string;
        }
    }
}
