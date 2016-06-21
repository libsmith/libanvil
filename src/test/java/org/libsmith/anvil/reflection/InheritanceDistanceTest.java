package org.libsmith.anvil.reflection;

import org.junit.Test;
import org.libsmith.anvil.AbstractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.libsmith.anvil.reflection.InheritanceDistance.fromType;
import static org.libsmith.anvil.reflection.InheritanceDistance.toType;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 20.03.16 6:19
 */
public class InheritanceDistanceTest extends AbstractTest {

    @Test
    public void forwardDistanceTest() {
        assertEquals(A.class, fromType(A.class).to(A.class).getType());
        assertEquals(0, fromType(A.class).to(A.class).getDistance().intValue());

        assertEquals(A.class, fromType(Object.class).to(A.class).to(B.class).getType());
        assertEquals(1, fromType(Object.class).to(A.class).to(B.class).getDistance().intValue());

        assertEquals(B.class, fromType(A.class).to(B.class).getType());
        assertEquals(1, fromType(A.class).to(B.class).getDistance().intValue());

        assertNull(fromType(A.class).to(Object.class).getType());
        assertNull(fromType(A.class).to(Object.class).getDistance());

        assertNull(fromType(A.class).to(I.class).getType());
        assertNull(fromType(A.class).to(I.class).getDistance());
    }

    @Test
    public void backwardDistanceTest() {
        assertEquals(I.class, toType(A.class).from(I.class).from(J.class).getType());
        assertEquals(1, toType(A.class).from(I.class).from(J.class).getDistance().intValue());

        assertEquals(A.class, toType(B.class).from(Object.class).from(A.class).getType());
        assertEquals(1, toType(B.class).from(Object.class).from(A.class).getDistance().intValue());
    }

    private static class A implements I { }
    private static class B extends A { }

    interface I extends J { }
    interface J { }
}
