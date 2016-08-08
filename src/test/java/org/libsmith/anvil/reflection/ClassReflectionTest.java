package org.libsmith.anvil.reflection;

import org.junit.Test;
import org.libsmith.anvil.AbstractTest;
import org.libsmith.anvil.EqualityAssertions;
import org.libsmith.anvil.reflection.ReflectiveOperationRuntimeException.NoSuchMemberRuntimeException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;

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
                ClassReflection.of(Subject.class).getReflectionSubject());

        assertEquals(
                Subject.class,
                ClassReflection.ofInstance(new Subject()).getReflectionSubject());

        assertEquals(
                ParentSubject.class,
                ClassReflection.ofDeclaringClass(ClassReflection.of(Subject.class).getField("PARENT_PUBLIC_FIELD"))
                               .getReflectionSubject());
        assertEquals(
                ParentSubject.class,
                ClassReflection.ofDeclaringClass(ClassReflection.of(Subject.class).getMethod("parentPublicMethod"))
                               .getReflectionSubject());
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

        ClassReflection<Subject> reflection = ClassReflection.of(Subject.class);

        assertThat(reflection.getMethodOp("parentPublicMethod")).isPresent();
        assertThat(reflection.getMethod("parentPublicMethod").getName()).isEqualTo("parentPublicMethod");

        assertThat(reflection.getLocalMethodOp("privateMethod")).isPresent();
        assertThat(reflection.getLocalMethod("privateMethod").getName()).isEqualTo("privateMethod");
    }

    @Test
    public void getMethodNoSuchMethodTest() {

        ClassReflection<Subject> reflection = ClassReflection.of(Subject.class);

        assertThat(reflection.method("privateMethod").noParamsOp()).isNotPresent();
        assertThat(reflection.getMethodOp("privateMethod")).isNotPresent();
        assertThat(reflection.getLocalMethodOp("parentPublicMethod")).isNotPresent();

        assertThatThrownBy(() -> reflection.method("privateMethod").noParams())
                .isInstanceOf(NoSuchMemberRuntimeException.class)
                .hasCauseInstanceOf(NoSuchMethodException.class);

        assertThatThrownBy(() -> reflection.getMethod("privateMethod"))
                .isInstanceOf(NoSuchMemberRuntimeException.class)
                .hasCauseInstanceOf(NoSuchMethodException.class);

        assertThatThrownBy(() -> reflection.getLocalMethod("parentPublicMethod"))
                .isInstanceOf(NoSuchMemberRuntimeException.class)
                .hasCauseInstanceOf(NoSuchMethodException.class);
    }

    @Test
    public void getFieldTest() {

        ClassReflection<Subject> reflection = ClassReflection.of(Subject.class);

        assertThat(reflection.fieldOp("PARENT_PUBLIC_FIELD")).isPresent();
        assertThat(reflection.field("PARENT_PUBLIC_FIELD").getName()).isEqualTo("PARENT_PUBLIC_FIELD");
        assertThat(reflection.getField("PARENT_PUBLIC_FIELD").getName()).isEqualTo("PARENT_PUBLIC_FIELD");
        assertThat(reflection.getFieldOp("PARENT_PUBLIC_FIELD")).isPresent();

        assertThat(reflection.localFieldOp("PRIVATE_FIELD")).isPresent();
        assertThat(reflection.localField("PRIVATE_FIELD").getName()).isEqualTo("PRIVATE_FIELD");
        assertThat(reflection.getLocalField("PRIVATE_FIELD").getName()).isEqualTo("PRIVATE_FIELD");
        assertThat(reflection.getLocalFieldOp("PRIVATE_FIELD")).isPresent();
    }

    @Test
    public void getFieldNoSuchFieldTest() {

        ClassReflection<Subject> reflection = ClassReflection.of(Subject.class);

        assertThat(reflection.fieldOp("PRIVATE_FIELD")).isNotPresent();
        assertThat(reflection.getFieldOp("PRIVATE_FIELD")).isNotPresent();
        assertThat(reflection.getLocalFieldOp("PARENT_PUBLIC_FIELD")).isNotPresent();

        assertThatThrownBy(() -> reflection.field("PRIVATE_FIELD"))
                .isInstanceOf(NoSuchMemberRuntimeException.class)
                .hasCauseInstanceOf(NoSuchFieldException.class);

        assertThatThrownBy(() -> reflection.getField("PRIVATE_FIELD"))
                .isInstanceOf(NoSuchMemberRuntimeException.class)
                .hasCauseInstanceOf(NoSuchFieldException.class);

        assertThatThrownBy(() -> reflection.getLocalField("PARENT_PUBLIC_FIELD"))
                .isInstanceOf(NoSuchMemberRuntimeException.class)
                .hasCauseInstanceOf(NoSuchFieldException.class);
    }

    @Test
    public void getConstructorTest() {

        ClassReflection<Subject> reflection = ClassReflection.of(Subject.class);

        assertThat(reflection.constructor().noParams().getName()).isEqualTo(Subject.class.getName());
        assertThat(reflection.getConstructor().getName()).isEqualTo(Subject.class.getName());
        assertThat(reflection.getConstructorOp()).isPresent();
    }

    @Test
    public void getConstructorNoSuchConstructorTest() {

        ClassReflection<Subject> reflection = ClassReflection.of(Subject.class);

        assertThat(reflection.constructor().paramsOp(Void.class)).isNotPresent();
        assertThat(reflection.getConstructorOp(Void.class)).isNotPresent();

        assertThatThrownBy(() -> reflection.constructor().params(Void.class))
                .isInstanceOf(NoSuchMemberRuntimeException.class)
                .hasCauseInstanceOf(NoSuchMethodException.class);

        assertThatThrownBy(() -> reflection.getConstructor(Void.class))
                .isInstanceOf(NoSuchMemberRuntimeException.class)
                .hasCauseInstanceOf(NoSuchMethodException.class);
    }


    @Test
    public void methodInvocationTest() {

        Subject subject = new Subject();
        ClassReflection<Subject> reflection = ClassReflection.of(Subject.class);

        assertThat(reflection.method("parentPublicStaticMethod").noParams().asStatic().invoke())
                .isEqualTo("ppusm");

        assertThat(reflection.localMethod("privateStaticMethod").noParams().accessible(true).asStatic().invoke())
                .isEqualTo("prsm");

        assertThat(reflection.method("privateStaticMethod").noParamsOp()).isNotPresent();

        assertThat(reflection.method("publicMethod").noParams().withObject(subject).accessible(true).invoke())
                .isEqualTo("pum");

        assertThat(reflection.localMethod("privateMethod").noParams().withObject(subject).accessible(true).invoke())
                .isEqualTo("prm");

        assertThat(reflection.method("privateMethod").noParamsOp()).isNotPresent();
    }

    @Test
    public void fieldAccessTest() {

        Subject subject = new Subject();
        ClassReflection<Subject> reflection = ClassReflection.of(Subject.class);

        assertThat(reflection.field("PARENT_PUBLIC_STATIC_FIELD").asStatic().getValue())
                .isEqualTo("ppusf");

        assertThat(reflection.localField("PRIVATE_STATIC_FIELD").asStatic().accessible(true).getValue())
                .isEqualTo("prsf");

        assertThat(reflection.fieldOp("PRIVATE_STATIC_FIELD")).isNotPresent();

        assertThat(reflection.field("PARENT_PUBLIC_FIELD").withObject(subject).getValue())
                .isEqualTo("ppuf");

        assertThat(reflection.localField("PRIVATE_FIELD").withObject(subject).accessible(true).getValue())
                .isEqualTo("prf");

        assertThat(reflection.fieldOp("PRIVATE_FIELD")).isNotPresent();
    }

    @Test
    public void constructorInvocationTest() {

        assertThat(ClassReflection.of(Subject.class).constructor().noParams().accessible(true).construct().publicMethod())
                .isEqualTo("pum");

        assertThat(ClassReflection.of(BigInteger.class).constructor().params(String.class).construct("42").intValue())
                .isEqualTo(42);

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

    @Test
    public void equalsHashCodeTest() {
        EqualityAssertions.assertThat(ClassReflection.of(String.class))
                          .equalsTo(ClassReflection.of(String.class))
                          .notEqualsTo(ClassReflection.of(Void.class));
    }

    @SuppressWarnings("unused")
    private interface Interface {
        String INTERFACE_FIELD = "if";
        String publicMethod();
    }

    @SuppressWarnings("unused")
    private abstract static class ParentSubject implements Interface {

        private static String PARENT_PRIVATE_STATIC_FIELD = "pprsf";
        private        String PARENT_PRIVATE_FIELD        = "pprf";
        public static  String PARENT_PUBLIC_STATIC_FIELD  = "ppusf";
        public         String PARENT_PUBLIC_FIELD         = "ppuf";

        private static String parentPrivateStaticMethod() { return "pprsm"; }
        public static  String parentPublicStaticMethod()  { return "ppusm"; }

        private String parentPrivateMethod() { return "pprm"; }
        public  String parentPublicMethod()  { return "ppum"; }

        abstract void abstractMethod();
    }

    @SuppressWarnings("unused")
    private static class Subject extends ParentSubject {

        private static String PRIVATE_STATIC_FIELD = "prsf";
        private        String PRIVATE_FIELD        = "prf";
        public static  String PUBLIC_STATIC_FIELD  = "pusf";
        public         String PUBLIC_FIELD         = "puf";

        static {
            int ignored = ParentSubject.PARENT_PRIVATE_STATIC_FIELD.hashCode(); // bridge method create
        }

        private static String privateStaticMethod() { return "prsm"; }
        public static  String publicStaticMethod()  { return "pusm"; }

        private String privateMethod() { return "prm"; }

        @Override
        public String  publicMethod()  { return "pum"; }

        @Override
        void abstractMethod() { }
    }
}
