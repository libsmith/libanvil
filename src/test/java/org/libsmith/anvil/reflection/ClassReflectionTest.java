package org.libsmith.anvil.reflection;

import org.junit.Test;
import org.libsmith.anvil.AbstractTest;
import org.libsmith.anvil.reflection.ReflectiveOperationRuntimeException.NoSuchMemberRuntimeException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 09.06.16 0:50
 */
public class ClassReflectionTest extends AbstractTest {

    @Test
    public void instantiationTest() {
        assertEquals(
                Subject.class,
                ClassReflection.of(Subject.class).getReflectionSubjectClass());

        assertEquals(
                Subject.class,
                ClassReflection.ofInstance(new Subject()).getReflectionSubjectClass());

        assertEquals(
                ParentSubject.class,
                ClassReflection.ofDeclaringClass(ClassReflection.of(Subject.class).getFieldExact("PARENT_PUBLIC_FIELD"))
                               .getReflectionSubjectClass());
        assertEquals(
                ParentSubject.class,
                ClassReflection.ofDeclaringClass(ClassReflection.of(Subject.class).getMethodExact("parentPublicMethod"))
                               .getReflectionSubjectClass());
    }

    @Test
    public void allFieldsTest() {
        assertEquals(
                "INTERFACE_FIELD, PARENT_PRIVATE_FIELD, PARENT_PRIVATE_STATIC_FIELD, PARENT_PUBLIC_FIELD, " +
                "PARENT_PUBLIC_STATIC_FIELD, PRIVATE_FIELD, PRIVATE_STATIC_FIELD, PUBLIC_FIELD, PUBLIC_STATIC_FIELD",
                ClassReflection.of(Subject.class).allFields().map(Field::getName).sorted().collect(joining(", ")));

        assertEquals(
                "INTERFACE_FIELD, PARENT_PRIVATE_FIELD, PARENT_PRIVATE_STATIC_FIELD, PARENT_PUBLIC_FIELD, " +
                "PARENT_PUBLIC_STATIC_FIELD",
                ClassReflection.of(ParentSubject.class).allFields()
                               .map(Field::getName).sorted().collect(joining(", ")));

        assertEquals(
                "INTERFACE_FIELD",
                ClassReflection.of(Interface.class).allFields()
                               .map(Field::getName).sorted().collect(joining(", ")));
    }

    @Test
    public void localFieldsTest() {
        assertEquals(
                "PRIVATE_FIELD, PRIVATE_STATIC_FIELD, PUBLIC_FIELD, PUBLIC_STATIC_FIELD",
                ClassReflection.of(Subject.class).localFields()
                               .map(Field::getName).sorted().collect(joining(", ")));

        assertEquals(
                "PARENT_PRIVATE_FIELD, PARENT_PRIVATE_STATIC_FIELD, PARENT_PUBLIC_FIELD, PARENT_PUBLIC_STATIC_FIELD",
                ClassReflection.of(ParentSubject.class).localFields()
                               .map(Field::getName).sorted().collect(joining(", ")));
    }

    @Test
    public void allMethodsTest() {
        assertEquals(
                "abstractMethod, abstractMethod, parentPrivateMethod, parentPrivateStaticMethod, " +
                "parentPublicMethod, parentPublicStaticMethod, privateMethod, privateStaticMethod, " +
                "publicMethod, publicMethod, publicStaticMethod",
                ClassReflection.of(Subject.class).allMethods()
                               .map(Method::getName).sorted().collect(joining(", ")));

        assertEquals(
                "abstractMethod, parentPrivateMethod, parentPrivateStaticMethod, parentPublicMethod, " +
                "parentPublicStaticMethod, publicMethod",
                ClassReflection.of(ParentSubject.class).allMethods()
                               .map(Method::getName).sorted().collect(joining(", ")));
    }

    @Test
    public void localMethodsTest() {
        assertEquals(
                "abstractMethod, privateMethod, privateStaticMethod, publicMethod, publicStaticMethod",
                ClassReflection.of(Subject.class).localMethods()
                               .map(Method::getName).sorted().collect(joining(", ")));

        assertEquals(
                "abstractMethod, parentPrivateMethod, parentPrivateStaticMethod, " +
                "parentPublicMethod, parentPublicStaticMethod",
                ClassReflection.of(ParentSubject.class).localMethods()
                               .map(Method::getName).sorted().collect(joining(", ")));
    }

    @Test
    public void getMethodTest() {
        ClassReflection<Subject> subjectReflection = ClassReflection.of(Subject.class);
        assertEquals("parentPublicMethod", subjectReflection.getMethodExact("parentPublicMethod").getName());
        assertEquals("privateMethod", subjectReflection.getLocalMethodExact("privateMethod").getName());
    }

    @Test
    public void getMethodNoSuchMethodTst() {

        ClassReflection<Subject> subjectReflection = ClassReflection.of(Subject.class);

        assertThat(subjectReflection.getMethod("privateMethod").orElse(null)).isNull();
        assertThat(subjectReflection.getLocalMethod("parentPublicMethod").orElse(null)).isNull();

        assertThatThrownBy(() -> subjectReflection.getMethodExact("privateMethod"))
                .isInstanceOf(NoSuchMemberRuntimeException.class)
                .hasCauseInstanceOf(NoSuchMethodException.class);

        assertThatThrownBy(() -> subjectReflection.getLocalMethodExact("parentPublicMethod"))
                .isInstanceOf(NoSuchMemberRuntimeException.class)
                .hasCauseInstanceOf(NoSuchMethodException.class);
    }

    @Test
    public void getFieldTest() {

        ClassReflection<Subject> subjectReflection = ClassReflection.of(Subject.class);

        assertEquals("PARENT_PUBLIC_FIELD", subjectReflection.getFieldExact("PARENT_PUBLIC_FIELD").getName());
        assertEquals("PRIVATE_FIELD", subjectReflection.getLocalFieldExact("PRIVATE_FIELD").getName());
    }

    @Test
    public void getFieldNoSuchFieldTest() {

        ClassReflection<Subject> subjectReflection = ClassReflection.of(Subject.class);

        assertThat(subjectReflection.getField("PRIVATE_FIELD").orElse(null)).isNull();
        assertThat(subjectReflection.getLocalField("PARENT_PUBLIC_FIELD").orElse(null)).isNull();

        assertThatThrownBy(() -> subjectReflection.getFieldExact("PRIVATE_FIELD"))
                .isInstanceOf(NoSuchMemberRuntimeException.class)
                .hasCauseInstanceOf(NoSuchFieldException.class);

        assertThatThrownBy(() -> subjectReflection.getLocalFieldExact("PARENT_PUBLIC_FIELD"))
                .isInstanceOf(NoSuchMemberRuntimeException.class)
                .hasCauseInstanceOf(NoSuchFieldException.class);
    }

    @Test
    public void getMembersWithModifiers() {
        ClassReflection<Subject> reflection = ClassReflection.of(Subject.class);

        assertThat(reflection.getMethod("publicStaticMethod")).isPresent();
        assertThat(reflection.getMethod("public static publicStaticMethod")).isPresent();

        assertThat(reflection.getMethod("public static final publicStaticMethod")).isNotPresent();
        assertThat(reflection.getMethod("private publicStaticMethod")).isNotPresent();

        assertThat(reflection.getField("PUBLIC_FIELD")).isPresent();
        assertThat(reflection.getField("public static PUBLIC_STATIC_FIELD")).isPresent();

        assertThat(reflection.getField("private PUBLIC_FIELD")).isNotPresent();
        assertThat(reflection.getField("volatile PUBLIC_STATIC_FIELD")).isNotPresent();

        assertThatThrownBy(() -> reflection.getMethodExact("private publicStaticMethod"))
                .isInstanceOf(NoSuchMemberRuntimeException.class);

        assertThatThrownBy(() -> reflection.getFieldExact("private PUBLIC_FIELD"))
                .isInstanceOf(NoSuchMemberRuntimeException.class);
    }

    @Test
    public void toStringTest() {
        assertEquals(
                ClassReflection.class.getSimpleName() + " of " + Subject.class.getName(),
                ClassReflection.of(Subject.class).toString());
    }

    @Test
    public void copyableFieldsFilterTest() {
        assertEquals(
                "PUBLIC_FIELD, PARENT_PUBLIC_FIELD, PRIVATE_FIELD, PARENT_PRIVATE_FIELD",
                ClassReflection.of(Subject.class).allFields().filter(ClassReflection.COPYABLE_FIELDS)
                               .map(Field::getName).collect(joining(", ")));
    }

    @Test
    public void publicMethodsFilterTest() {
        assertEquals(
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
