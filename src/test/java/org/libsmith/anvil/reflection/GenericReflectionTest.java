package org.libsmith.anvil.reflection;

import org.junit.Test;
import org.libsmith.anvil.AbstractTest;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.libsmith.anvil.reflection.GenericReflection.extractGenericParameterOf;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 17.06.2014 23:13
 */
public class GenericReflectionTest extends AbstractTest {

    private G x = new G();

    @Test
    @SuppressWarnings("null")
    public void getGenericParameterClassTest() {
        assertNotSame(String.class, extractGenericParameterOf(A.class, 1).from(x));
        assertEquals(String.class, extractGenericParameterOf(A.class).from(x));
        assertEquals(Integer.class, extractGenericParameterOf(A.class, 1).from(x));

        assertEquals(Integer.class, extractGenericParameterOf(B.class).from(x));
        assertEquals(String.class, extractGenericParameterOf(B.class, 1).from(x));
        assertEquals(Set.class, extractGenericParameterOf(B.class, 2).from(x));

        assertEquals(String.class, extractGenericParameterOf(C.class).from(x));
        assertEquals(Double.class, extractGenericParameterOf(C.class, 1).from(x));
        assertEquals(Integer.class, extractGenericParameterOf(C.class, 2).from(x));

        assertEquals(Integer.class, extractGenericParameterOf(D.class).from(x));
        assertEquals(Double.class, extractGenericParameterOf(D.class, 1).from(x));

        assertEquals(Byte.class, extractGenericParameterOf(F.class).from(x));
        assertEquals(Long.class, extractGenericParameterOf(F.class, 1).from(x));

        assertEquals(Double.class, extractGenericParameterOf(H.class, 0).from(x));
        assertEquals(Integer.class, extractGenericParameterOf(H.class, 1).from(x));

        assertEquals(Integer.class, extractGenericParameterOf(L.class, 0).from(x));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSelfClass() {
        extractGenericParameterOf(A.class).from(A.class);
    }

    @Test
    public void getGenericParameterClassTypeTest() {
        TypeLiteral<ArrayList<Long>> t1 = new TypeLiteral<ArrayList<Long>>() { };
        assertEquals(Long.class, extractGenericParameterOf(Collection.class).from(t1));
        assertEquals(ArrayList.class, t1.toClass());

        Type t2 = new TypeLiteral<Collection<String>>() { };
        assertEquals(String.class, extractGenericParameterOf(Collection.class).from(t2));

        Type t3 = new TypeLiteral<Function<? super BigInteger, ? extends BigDecimal>>() { };
        assertEquals(BigInteger.class, extractGenericParameterOf(Function.class).from(t3));
        assertEquals(BigDecimal.class, extractGenericParameterOf(Function.class, 1).from(t3));

        Type t4 = new TypeLiteral<Function<? super Function<? super Number, ? extends CharSequence>, ? extends BigDecimal>>() { };
        assertEquals(Number.class,
                     extractGenericParameterOf(Function.class).from(extractGenericParameterOf(Function.class).asType().from(t4)));
        assertEquals(CharSequence.class,
                     extractGenericParameterOf(Function.class, 1).from(extractGenericParameterOf(Function.class).asType().from(t4)));
    }

    @SuppressWarnings({ "hiding", "unused" })
    static class A<K, L> { } // String, Integer

    @SuppressWarnings({ "hiding", "unused" })
    static class B<P, Q, R extends Collection<?>> extends A<Q, P> { } // Integer, String, Set

    @SuppressWarnings({ "hiding", "unused" })
    static class C<X extends Comparable<String>, Y, Z> extends B<Z, X, Set<Long>> { } // String, Double, Integer

    @SuppressWarnings({ "hiding", "unused" })
    static class D<M, N extends Comparable<Double>> extends C<String, N, M> implements H<N, M> { } // Integer, Double

    @SuppressWarnings({ "hiding", "unused" })
    static class E extends D<Integer, Double> { }

    @SuppressWarnings({ "hiding", "unused" })
    static class F<T, S> extends E { } // Byte, Long

    @SuppressWarnings({ "hiding", "unused" })
    static class G extends F<Byte, Long> { }

    @SuppressWarnings({ "hiding", "unused" })
    interface H<H1, H2> extends L<H2> { } // Double, Integer

    @SuppressWarnings({ "hiding", "unused" })
    interface L<L1> { } // Integer
}
