package org.libsmith.anvil.reflection;

import org.junit.Assert;
import org.junit.Test;
import org.libsmith.anvil.AbstractTest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static java.util.stream.Collectors.joining;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 09.06.16 0:50
 */
public class ClassReflectionTest extends AbstractTest {

    @Test
    public void instantiationTest() {
        Assert.assertEquals(
                Subject.class,
                ClassReflection.of(Subject.class).getReflectionSubject());

        Assert.assertEquals(
                Subject.class,
                ClassReflection.ofInstance(new Subject()).getReflectionSubject());

        Assert.assertEquals(
                ParentSubject.class,
                ClassReflection.ofDeclaringClass(ClassReflection.of(Subject.class).getField("PARENT_PUBLIC_FIELD"))
                               .getReflectionSubject());
        Assert.assertEquals(
                ParentSubject.class,
                ClassReflection.ofDeclaringClass(ClassReflection.of(Subject.class).getMethod("parentPublicMethod"))
                               .getReflectionSubject());
    }

    @Test
    public void allFieldsTest() {
        Assert.assertEquals(
                "INTERFACE_FIELD, PARENT_PRIVATE_FIELD, PARENT_PRIVATE_STATIC_FIELD, PARENT_PUBLIC_FIELD, " +
                "PARENT_PUBLIC_STATIC_FIELD, PRIVATE_FIELD, PRIVATE_STATIC_FIELD, PUBLIC_FIELD, PUBLIC_STATIC_FIELD",
                ClassReflection.of(Subject.class).allFields().map(Field::getName).sorted().collect(joining(", ")));

        Assert.assertEquals(
                "INTERFACE_FIELD, PARENT_PRIVATE_FIELD, PARENT_PRIVATE_STATIC_FIELD, PARENT_PUBLIC_FIELD, " +
                "PARENT_PUBLIC_STATIC_FIELD",
                ClassReflection.of(ParentSubject.class).allFields().map(Field::getName).sorted().collect(joining(", ")));

        Assert.assertEquals(
                "INTERFACE_FIELD",
                ClassReflection.of(Interface.class).allFields().map(Field::getName).sorted().collect(joining(", ")));
    }

    @Test
    public void localFieldsTest() {
        Assert.assertEquals(
                "PRIVATE_FIELD, PRIVATE_STATIC_FIELD, PUBLIC_FIELD, PUBLIC_STATIC_FIELD",
                ClassReflection.of(Subject.class).localFields().map(Field::getName).sorted().collect(joining(", ")));

        Assert.assertEquals(
                "PARENT_PRIVATE_FIELD, PARENT_PRIVATE_STATIC_FIELD, PARENT_PUBLIC_FIELD, PARENT_PUBLIC_STATIC_FIELD",
                ClassReflection.of(ParentSubject.class).localFields().map(Field::getName).sorted().collect(joining(", ")));
    }

    @Test
    public void allMethodsTest() {
        Assert.assertEquals(
                "abstractMethod, abstractMethod, parentPrivateMethod, parentPrivateStaticMethod, " +
                "parentPublicMethod, parentPublicStaticMethod, privateMethod, privateStaticMethod, " +
                "publicMethod, publicMethod, publicStaticMethod",
                ClassReflection.of(Subject.class).allMethods().map(Method::getName).sorted().collect(joining(", ")));

        Assert.assertEquals(
                "abstractMethod, parentPrivateMethod, parentPrivateStaticMethod, parentPublicMethod, " +
                "parentPublicStaticMethod, publicMethod",
                ClassReflection.of(ParentSubject.class).allMethods().map(Method::getName).sorted().collect(joining(", ")));
    }

    @Test
    public void localMethodsTest() {
        Assert.assertEquals(
                "abstractMethod, privateMethod, privateStaticMethod, publicMethod, publicStaticMethod",
                ClassReflection.of(Subject.class).localMethods().map(Method::getName).sorted().collect(joining(", ")));

        Assert.assertEquals(
                "abstractMethod, parentPrivateMethod, parentPrivateStaticMethod, parentPublicMethod, parentPublicStaticMethod",
                ClassReflection.of(ParentSubject.class).localMethods().map(Method::getName).sorted().collect(joining(", ")));
    }

    @Test
    public void getMethodTest() {
        Assert.assertEquals("parentPublicMethod", ClassReflection.of(Subject.class).getMethod("parentPublicMethod").getName());
        Assert.assertEquals("privateMethod", ClassReflection.of(Subject.class).getLocalMethod("privateMethod").getName());
    }

    @Test(expected = ReflectiveOperationRuntimeException.class)
    public void getMethodExceptionTest() {
        ClassReflection.of(Subject.class).getMethod("privateMethod");
    }

    @Test(expected = ReflectiveOperationRuntimeException.class)
    public void getLocalMethodExceptionTest() {
        ClassReflection.of(Subject.class).getLocalMethod("parentPublicMethod");
    }

    @Test
    public void getFieldTest() {
        Assert.assertEquals("PARENT_PUBLIC_FIELD", ClassReflection.of(Subject.class).getField("PARENT_PUBLIC_FIELD").getName());
        Assert.assertEquals("PRIVATE_FIELD", ClassReflection.of(Subject.class).getLocalField("PRIVATE_FIELD").getName());
    }

    @Test(expected = ReflectiveOperationRuntimeException.class)
    public void getFieldExceptionTest() {
        ClassReflection.of(Subject.class).getField("PRIVATE_FIELD");
    }

    @Test(expected = ReflectiveOperationRuntimeException.class)
    public void getLocalFieldExceptionTest() {
        ClassReflection.of(Subject.class).getLocalField("PARENT_PUBLIC_FIELD");
    }

    @Test
    public void toStringTest() {
        Assert.assertEquals(
                ClassReflection.class.getSimpleName() + " of " + Subject.class.getName(),
                ClassReflection.of(Subject.class).toString());
    }

    @Test
    public void copyableFieldsFilterTest() {
        Assert.assertEquals(
                "PUBLIC_FIELD, PARENT_PUBLIC_FIELD, PRIVATE_FIELD, PARENT_PRIVATE_FIELD",
                ClassReflection.of(Subject.class).allFields().filter(ClassReflection.COPYABLE_FIELDS)
                               .map(Field::getName).collect(joining(", ")));
    }

    @Test
    public void publicMethodsFilterTest() {
        Assert.assertEquals(
                "publicMethod, publicMethod, publicStaticMethod, parentPublicMethod, parentPublicStaticMethod",
                ClassReflection.of(Subject.class).allMethods().filter(ClassReflection.PUBLIC_METHODS)
                               .map(Method::getName).collect(joining(", ")));
    }

    @SuppressWarnings("unused")
    private interface Interface {
        String INTERFACE_FIELD = "if";
        void publicMethod();
    }

    @SuppressWarnings("unused")
    private abstract static class ParentSubject implements Interface {
        private static String PARENT_PRIVATE_STATIC_FIELD = "pprsf";
        private        String PARENT_PRIVATE_FIELD        = "pprsf";
        public static  String PARENT_PUBLIC_STATIC_FIELD  = "ppusf";
        public         String PARENT_PUBLIC_FIELD         = "ppusf";

        private static void parentPrivateStaticMethod() { }
        public static void parentPublicStaticMethod() { }

        private void parentPrivateMethod() { }
        public void parentPublicMethod() { }

        abstract void abstractMethod();
    }

    @SuppressWarnings("unused")
    private static class Subject extends ParentSubject {
        private static String PRIVATE_STATIC_FIELD = "prsf";
        private        String PRIVATE_FIELD        = "prsf";
        public static  String PUBLIC_STATIC_FIELD  = "pusf";
        public         String PUBLIC_FIELD         = "pusf";

        static {
            int ignored = ParentSubject.PARENT_PRIVATE_STATIC_FIELD.hashCode(); // bridge method create
        }

        private static void privateStaticMethod() {
        }

        public static void publicStaticMethod() {
        }

        private void privateMethod() {
        }

        @Override public void publicMethod() {
        }

        @Override void abstractMethod() {
        }
    }
}
