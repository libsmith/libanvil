package org.libsmith.anvil.collections;

import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 21.07.16 1:56
 */
public class LinkedNodeTest {

    @Test
    public void linkNodesTest() {
        List<LinkedNode<String>> noParentNodes = LinkedNode.linkNodes(Arrays.asList("a", "b", "c"), null);
        assertThat(
                noParentNodes.stream().map(LinkedNode::getValue).collect(Collectors.toList()))
                    .containsExactly("a", "b", "c");
        assertThat(
                noParentNodes.stream().map(LinkedNode::getParent).collect(Collectors.toList()))
                    .containsExactly(null, null, null);

        List<LinkedNode<String>> withParentNodes =
                LinkedNode.linkNodes(Arrays.asList("a", "b", "c"), new LinkedNode<>("parent"));

        assertThat(
                withParentNodes.stream().map(n -> n.getParent().getValue()).collect(Collectors.toList()))
                    .containsOnly("parent");

        assertThat(LinkedNode.linkNodes(null, null)).isNull();

        LinkedNode.linkNodes(null, null, Collections.emptyList());
    }

    @Test
    public void parentIteratorTest() {
        LinkedNode<String> subj = new LinkedNode<>("a", new LinkedNode<>("b", new LinkedNode<>("c")));
        Iterator<String> i = subj.parentIterator();
        assertThat(i.next()).isEqualTo("a");
        assertThat(i.next()).isEqualTo("b");
        assertThat(i.next()).isEqualTo("c");
        assertThatThrownBy(i::next).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void parentStreamTest() {
        LinkedNode<String> subj = new LinkedNode<>("a", new LinkedNode<>("b", new LinkedNode<>("c")));
        assertThat(subj.parentStream().collect(Collectors.joining(", ")))
                .isEqualTo("a, b, c");
    }

}
