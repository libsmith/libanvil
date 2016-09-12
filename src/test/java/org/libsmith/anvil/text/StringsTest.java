package org.libsmith.anvil.text;


import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.Test;
import org.libsmith.anvil.EqualityAssertions;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.libsmith.anvil.text.Strings.*;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 11.09.16 23:35
 */
public class StringsTest {

    private static final String WHITESPACES = " \t\r\f\n\u000B\u001c\u001D\u001E\u001F";

    @Test
    public void isEmptyTest() {
        Function<String, List<AbstractBooleanAssert<?>>> assertions = str ->
            Arrays.asList(
                    assertThat(isEmpty(str)),
                    assertThat(isEmpty(() -> str)),
                    assertThat(isEmpty(lazy(str)))
            );

        assertions.apply("").forEach(AbstractBooleanAssert::isTrue);
        assertions.apply(" ").forEach(AbstractBooleanAssert::isFalse);
        assertions.apply("a").forEach(AbstractBooleanAssert::isFalse);
    }

    @Test
    public void isNotEmptyTest() {
        Function<String, List<AbstractBooleanAssert<?>>> assertions = str ->
                Arrays.asList(
                        assertThat(isNotEmpty(str)),
                        assertThat(isNotEmpty(() -> str)),
                        assertThat(isNotEmpty(lazy(str)))
                );

        assertions.apply("").forEach(AbstractBooleanAssert::isFalse);
        assertions.apply(" ").forEach(AbstractBooleanAssert::isTrue);
        assertions.apply("a").forEach(AbstractBooleanAssert::isTrue);
    }

    @Test
    public void ifNotEmptyTest() {
        assertThat(ifNotEmpty("")).isNotPresent();
        assertThat(ifNotEmpty(" ")).isPresent().hasValue(" ");
        assertThat(ifNotEmpty("a")).isPresent().hasValue("a");
        assertThat(ifNotEmpty(lazy(""))).isNotPresent();
        assertThat(ifNotEmpty(lazy("a"))).isPresent().hasValueSatisfying(v -> assertThat(v).isEqualTo("a"));
    }

    @Test
    public void isBlankTest() {
        Function<String, List<AbstractBooleanAssert<?>>> assertions = str ->
                Arrays.asList(
                        assertThat(isBlank(str)),
                        assertThat(isBlank(() -> str)),
                        assertThat(isBlank(lazy(str)))
                );

        assertions.apply("").forEach(AbstractBooleanAssert::isTrue);
        assertions.apply(" ").forEach(AbstractBooleanAssert::isTrue);
        assertions.apply(WHITESPACES).forEach(AbstractBooleanAssert::isTrue);
        assertions.apply("a").forEach(AbstractBooleanAssert::isFalse);
    }

    @Test
    public void isNotBlankTest() {
        Function<String, List<AbstractBooleanAssert<?>>> assertions = str ->
                Arrays.asList(
                        assertThat(isNotBlank(str)),
                        assertThat(isNotBlank(() -> str)),
                        assertThat(isNotBlank(lazy(str)))
                );
        assertions.apply("").forEach(AbstractBooleanAssert::isFalse);
        assertions.apply(" ").forEach(AbstractBooleanAssert::isFalse);
        assertions.apply(WHITESPACES).forEach(AbstractBooleanAssert::isFalse);
        assertions.apply("a").forEach(AbstractBooleanAssert::isTrue);
    }

    @Test
    public void ifNotBlankTest() {
        assertThat(ifNotBlank("")).isNotPresent();
        assertThat(ifNotBlank(" ")).isNotPresent();
        assertThat(ifNotBlank(WHITESPACES)).isNotPresent();
        assertThat(ifNotBlank("a")).isPresent().hasValue("a");
        assertThat(ifNotBlank(lazy(""))).isNotPresent();
        assertThat(ifNotBlank(lazy(WHITESPACES))).isNotPresent();
        assertThat(ifNotBlank(lazy(" "))).isNotPresent();
        assertThat(ifNotBlank(lazy("a"))).isPresent().hasValueSatisfying(v -> assertThat(v).isEqualTo("a"));
    }

    @Test
    public void lazyStringEqualityTest() {
        EqualityAssertions.assertThat(lazy("abc")).and(lazy(() -> "abc"))
                          .equalsTo(lazy("abc")).and("abc")
                          .notEqualsTo(lazy("def")).and("def");
    }

    @Test
    public void lazyCharSequenceTest() {
        LazyCharSequence<String> abc = lazy("abc");
        assertThat(abc.length()).isEqualTo(3);
        assertThat(abc.charAt(1)).isEqualTo('b');
        assertThat(abc.subSequence(1, 3)).isEqualTo("bc");
        assertThat(abc.toString()).isEqualTo("abc");
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void lazyStringTest() {
        assertThat(lazy("abc").get()).isEqualTo("abc");
        assertThatThrownBy(() -> lazy((String) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void lazyPatternTest() {
        assertThat(lazy("Hello {1} {0}", "world!", "perfect"))
                .isEqualTo("Hello perfect world!");
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void lazyStringSupplierTest() {
        assertThat(lazy(() -> "abc").get()).isEqualTo("abc");
        assertThatThrownBy(() -> lazy((String) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void lazyStringBuilderTest() {
        LazyStringBuilder sb = lazyStringBuilder();
        sb.append('L');
        sb.append("Lorem ipsum dolor", 1, 6);
        sb.append(lazy("ipsum "));
        sb.append(() -> "dolor ");
        sb.append((Object) null);
        sb.append(", ");
        sb.append((Supplier<?>) null);
        sb.append(", ");
        sb.append((CharSequence) null);

        assertThat(sb.toString()).isEqualTo("Lorem ipsum dolor null, null, null");
        assertThat(sb.length()).isEqualTo(34);
        assertThat(sb.charAt(7)).isEqualTo('p');
        assertThat(sb.subSequence(1, 5)).isEqualTo("orem");
    }
}
