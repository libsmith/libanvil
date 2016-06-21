package org.libsmith.anvil.reflection;

import org.junit.Test;
import org.libsmith.anvil.AbstractTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 09.06.16 3:05
 */
public class TypeLiteralTest extends AbstractTest {

    private TypeLiteral literal = new TypeLiteral<Map<List<Void>, String>>() { };

    @Test
    public void getActualTypeArgumentsTest() {
        assertEquals("[java.util.List<java.lang.Void>, class java.lang.String]",
                     Arrays.toString(literal.getActualTypeArguments()));
    }

    @Test
    public void toClassTest() {
        assertEquals(Map.class, literal.toClass());
    }

    @Test
    public void getRawType() {
        assertEquals(Map.class, literal.getRawType());
    }

    @Test
    public void getOwnerType() {
        assertNull(literal.getOwnerType());
    }

    @Test
    public void getTypeName() {
        assertEquals("java.util.Map<java.util.List<java.lang.Void>, java.lang.String>", literal.getTypeName());
    }
}
