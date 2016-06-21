package org.libsmith.anvil.reflection;

import org.junit.Test;
import org.libsmith.anvil.AbstractTest;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 03.02.2016 0:54
 */
public class CachedProxyTest extends AbstractTest {

    private interface SomethingInterface {

        int methodA(int a, boolean b);

        int methodB(int a, boolean b);
    }

    private static class SomethingClass implements SomethingInterface {

        private int somethingCounter;

        @Override
        public int methodA(int a, boolean b) {
            return somethingCounter++;
        }

        @Override
        public int methodB(int a, boolean b) {
            return somethingCounter++;
        }
    }

    @Test
    public void genericTest() throws Exception {

        SomethingInterface somethingProxy = CachedProxy.wrap(new SomethingClass(), SomethingInterface.class);

        assertEquals(0, somethingProxy.methodA(0, true));
        assertEquals(1, somethingProxy.methodA(1, true));
        assertEquals(0, somethingProxy.methodA(0, true));
        assertEquals(2, somethingProxy.methodA(0, false));
        assertEquals(3, somethingProxy.methodA(1, false));

        assertEquals(4, somethingProxy.methodB(0, true));
        assertEquals(5, somethingProxy.methodB(1, true));
        assertEquals(4, somethingProxy.methodB(0, true));
        assertEquals(6, somethingProxy.methodB(0, false));
        assertEquals(7, somethingProxy.methodB(1, false));

        ((CachedProxy) somethingProxy).dropCaches();

        assertEquals(8, somethingProxy.methodA(0, true));
        assertEquals(9, somethingProxy.methodB(0, true));
        assertEquals(8, somethingProxy.methodA(0, true));
        assertEquals(9, somethingProxy.methodB(0, true));
    }
}
