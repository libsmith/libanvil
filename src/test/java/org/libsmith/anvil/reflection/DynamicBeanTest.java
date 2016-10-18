package org.libsmith.anvil.reflection;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 05.10.16 3:11
 */
public class DynamicBeanTest {

    @Test
    public void getAndSetTest() {
        HashMap<String, Object> beanProperties = new HashMap<>();
        DynamicBean dynamicBean = DynamicBean.of(beanProperties);

        GenericInterface gi = dynamicBean.as(GenericInterface.class);
        Assert.assertTrue(beanProperties.isEmpty());
        gi.setSomeProperty("Test test test");
        Assert.assertEquals("Test test test", gi.getSomeProperty());
        Assert.assertEquals("Test test test", beanProperties.get("someProperty"));

        SplitGetterInterface getterInterface = dynamicBean.as(SplitGetterInterface.class);
        SplitSetterInterface setterInterface = dynamicBean.as(SplitSetterInterface.class);

        setterInterface.setSomeProperty("Another value");
        Assert.assertEquals("Another value", getterInterface.getSomeProperty());
    }

    @Test
    public void splitGetAndSetTest() {
        DynamicBean dynamicBean = DynamicBean.of(new HashMap<>());
        SplitGetterInterface getterInterface = dynamicBean.as(SplitGetterInterface.class);
        SplitSetterInterface setterInterface = dynamicBean.as(SplitSetterInterface.class);

        setterInterface.setSomeProperty("qwerty");
        Assert.assertEquals("qwerty", getterInterface.getSomeProperty());
    }

    @Test
    public void optionalGetterTest() {
        DynamicBean dynamicBean = DynamicBean.of(new HashMap<>());
        OptionalGetterInterface getter = dynamicBean.as(OptionalGetterInterface.class);
        Assert.assertFalse(getter.getSomeProperty().isPresent());

        SplitSetterInterface setter = dynamicBean.as(SplitSetterInterface.class);
        setter.setSomeProperty("asdf");
        Assert.assertEquals("asdf", getter.getSomeProperty().orElse(null));
    }

    @Test
    public void defaultMethodInvoke() {
        DefaultMethodInterface dmi = DynamicBean.of(new HashMap<>()).as(DefaultMethodInterface.class);
        dmi.setSomeProperty("asdf");
        Assert.assertEquals("Hello asdf!", dmi.makeSomeHello());
    }

    @Test
    public void contextMapAndDetachTest() {
        HashMap<String, Object> propetiesA = new HashMap<>();
        HashMap<String, Object> propetiesB = new HashMap<>();
        AtomicInteger a = new AtomicInteger();
        DynamicBean dynamicBean = DynamicBean.of(() -> a.incrementAndGet() % 2 == 0 ? propetiesA : propetiesB);
        GenericInterface genericInterface = dynamicBean.as(GenericInterface.class);
        genericInterface.setSomeProperty("qwerty");
        genericInterface.setSomeProperty("asdfgh");

        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("qwerty", genericInterface.getSomeProperty());
            Assert.assertEquals("asdfgh", genericInterface.getSomeProperty());
        }

        GenericInterface detachedA = genericInterface.detach().as(GenericInterface.class);
        GenericInterface detachedB = genericInterface.detach().as(GenericInterface.class);

        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("qwerty", detachedA.getSomeProperty());
        }

        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("asdfgh", detachedB.getSomeProperty());
        }
    }

    @Test
    public void equalsAndHashCodeTest() {
        DynamicBean dynamicBean = DynamicBean.of(new HashMap<>());
        GenericInterface genericInterface1 = dynamicBean.as(GenericInterface.class);
        GenericInterface genericInterface2 = dynamicBean.as(GenericInterface.class);

        Assert.assertEquals(genericInterface1, genericInterface1);
        Assert.assertEquals(genericInterface2, genericInterface2);
        Assert.assertEquals(genericInterface1, genericInterface2);
        Assert.assertEquals(genericInterface2, genericInterface1);

        Assert.assertEquals(genericInterface1.hashCode(), genericInterface1.hashCode());
        Assert.assertEquals(genericInterface2.hashCode(), genericInterface2.hashCode());

        Assert.assertFalse(genericInterface1.toString().isEmpty());
        Assert.assertEquals(genericInterface1.toString(), genericInterface1.toString());
        Assert.assertEquals(genericInterface1.toString(), genericInterface2.toString());
    }

    @Test
    public void booleanTest() {
        DynamicBean dynamicBean = DynamicBean.of(new HashMap<>());
        BooleanInterface booleanInterface = dynamicBean.as(BooleanInterface.class);
        Assert.assertFalse(booleanInterface.isValue());
        Assert.assertNull(booleanInterface.getValue());

        booleanInterface.setValue(true);
        Assert.assertTrue(booleanInterface.isValue());
        Assert.assertTrue(booleanInterface.getValue());

        booleanInterface.setValue(false);
        Assert.assertFalse(booleanInterface.isValue());
        Assert.assertFalse(booleanInterface.getValue());
    }

    @Test
    public void dynamicBeanInterfaceTest() {
        DynamicBean dynamicBean = DynamicBean.of(new HashMap<>());
        BooleanInterface gi = dynamicBean.as(BooleanInterface.class);
        gi.as(SplitSetterInterface.class).setSomeProperty("Abc...");
        Assert.assertEquals("Abc...", dynamicBean.as(SplitGetterInterface.class).getSomeProperty());
    }

    @Test
    public void propertyDescriptorTest() {
        DynamicBean dynamicBean = DynamicBean.of(new HashMap<>());
        dynamicBean.as(SplitSetterInterface.class).setSomeProperty("qwerty");
        Assert.assertEquals("qwerty", dynamicBean.as(PropertyDescribed.class).getAJustGetter());
    }

    @Test
    public void namespaceTest() {
        HashMap<String, Object> map = new HashMap<>();
        DynamicBean dynamicBean = DynamicBean.of(map);

        {
            NameSpacingIface nsi = dynamicBean.as(NameSpacingIface.class);
            nsi.setValue("qwe");
            nsi.setAnotherValue("asd");
            nsi.setSomeProperty("some");

            Assert.assertEquals("qwe", nsi.getValue());
            Assert.assertEquals("asd", nsi.getAnotherValue());
            Assert.assertEquals("some", nsi.getSomeProperty());

            Assert.assertEquals("qwe", map.get("com.test.value"));
            Assert.assertEquals("asd", map.get("com.test.val"));
            Assert.assertEquals("some", map.get("someProperty"));

            Assert.assertNull(map.get("value"));
            Assert.assertNull(map.get("val"));
            Assert.assertNull(map.get("com.test.someProperty"));
        }

        {
            AsClassNameSpacingIface acnsi = dynamicBean.as(AsClassNameSpacingIface.class);
            Assert.assertEquals("qwe", acnsi.getValue());
            Assert.assertEquals("asd", acnsi.getAnotherValue());
            Assert.assertEquals("some", acnsi.getSomeProperty());

            acnsi.setValue("123");
            acnsi.setAnotherValue("456");
            acnsi.setSomeProperty("789");

            Assert.assertEquals("123", map.get("com.test.value"));
            Assert.assertEquals("456", map.get("com.test.val"));
            Assert.assertEquals("789", map.get("someProperty"));

            acnsi.setAsClValue("fffggg");
            Assert.assertEquals("fffggg", acnsi.getAsClValue());
            Assert.assertEquals("fffggg", map.get(AsClassNameSpacingIface.class.getCanonicalName() + ".asClValue"));
        }

        {
            AsClassSfxNameSpacingIface acnsi = dynamicBean.as(AsClassSfxNameSpacingIface.class);
            Assert.assertEquals("123", acnsi.getValue());
            Assert.assertEquals("456", acnsi.getAnotherValue());
            Assert.assertEquals("789", acnsi.getSomeProperty());
            Assert.assertEquals("fffggg", acnsi.getAsClValue());

            acnsi.setAsClSfxValue("cvbn");
            Assert.assertEquals("cvbn", acnsi.getAsClSfxValue());
            Assert.assertEquals("cvbn", map.get(AsClassSfxNameSpacingIface.class.getCanonicalName() + ".suffix.asClSfxValue"));
        }

        {
            AsClassPnsNameSpacingIface iface = dynamicBean.as(AsClassPnsNameSpacingIface.class);
            Assert.assertEquals("fffggg", iface.getAsClValue());
            Assert.assertEquals(null, iface.getAsClSfxValue());
        }
    }

    @Test
    public void asMapTest() {
        GenericInterface gi = DynamicBean.of(new HashMap<>()).as(GenericInterface.class);
        gi.setSomeProperty("some value");
        Assert.assertEquals("some value", gi.as(Map.class).get("someProperty"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unsupportedMethod() {
        DynamicBean.of(Collections.emptyMap()).as(GenericInterface.class).unsupportedMethod();
    }

    @Test
    public void defaultNamespaceTesting() {
        DynamicBean.Namespace namespace = DynamicBean.makeNamespace("suf", DynamicBean.Namespace.Self.class);
        HashMap<String, Object> map = new HashMap<>();
        DynamicBean dynamicBean = DynamicBean.of(map);
        DynamicBean dynamicNSBean = dynamicBean.withDefaultNamespace(namespace);
        dynamicNSBean.as(GenericInterface.class).setSomeProperty("sadad");
        Assert.assertEquals("sadad", map.get(GenericInterface.class.getCanonicalName() + ".suf.someProperty"));
        Assert.assertEquals("sadad", dynamicNSBean.as(GenericInterface.class).getSomeProperty());
        Assert.assertNull(dynamicBean.as(GenericInterface.class).getSomeProperty());

        GenericInterface giQqq = dynamicBean.withDefaultNamespace(DynamicBean.makeNamespace("qqq"))
                                            .as(GenericInterface.class);
        Assert.assertNull(giQqq.getSomeProperty());
        giQqq.setSomeProperty("ddffgg");
        Assert.assertEquals("ddffgg", map.get("qqq.someProperty"));

        GenericInterface giObj = dynamicBean.withDefaultNamespace(DynamicBean.makeNamespace(Object.class))
                                            .as(GenericInterface.class);
        Assert.assertNull(giObj.getSomeProperty());
        giObj.setSomeProperty("werty");
        Assert.assertEquals("werty", map.get("java.lang.Object.someProperty"));
    }

    @Test
    public void wrapperCacheTest() {
        DynamicBean dynamicBean = DynamicBean.of(new HashMap<>());
        Assert.assertSame(dynamicBean.as(GenericInterface.class), dynamicBean.as(GenericInterface.class));
        Assert.assertEquals(dynamicBean.as(GenericInterface.class), dynamicBean.as(GenericInterface.class));
        Assert.assertNotSame(dynamicBean.as(GenericInterface.class), dynamicBean.detach().as(GenericInterface.class));
        Assert.assertNotEquals(dynamicBean.as(GenericInterface.class), dynamicBean.detach().as(GenericInterface.class));
        Assert.assertNotSame(dynamicBean.as(DefaultMethodInterface.class), dynamicBean.as(GenericInterface.class));
        Assert.assertNotEquals(dynamicBean.as(DefaultMethodInterface.class), dynamicBean.as(GenericInterface.class));
    }

    interface PropertyDescribed {

        @DynamicBean.Property(name = "someProperty")
        String getAJustGetter();
    }

    interface GenericInterface extends DynamicBean {

        String getSomeProperty();
        void setSomeProperty(String someProperty);
        String unsupportedMethod();
    }

    public interface DefaultMethodInterface extends GenericInterface {

        default String makeSomeHello() {
            return "Hello " + getSomeProperty() + "!";
        }
    }

    interface SplitGetterInterface {
        String getSomeProperty();
    }

    interface SplitSetterInterface {
        void setSomeProperty(String propertyValue);
    }

    interface BooleanInterface extends DynamicBean {
        void setValue(boolean value);
        boolean isValue();
        Boolean getValue();
    }

    interface OptionalGetterInterface {
        Optional<String> getSomeProperty();
    }

    @DynamicBean.Namespace("com.test")
    interface NameSpacingIface extends GenericInterface {
        String getValue();
        void setValue(String value);

        @DynamicBean.Property(name = "val")
        String getAnotherValue();

        @DynamicBean.Property(name = "val")
        void setAnotherValue(String value);
    }

    @DynamicBean.Namespace(as = AsClassNameSpacingIface.class)
    interface AsClassNameSpacingIface extends NameSpacingIface {

        String getAsClValue();
        void setAsClValue(String value);
    }

    @DynamicBean.Namespace(value = "suffix", as = AsClassSfxNameSpacingIface.class)
    interface AsClassSfxNameSpacingIface extends AsClassNameSpacingIface {

        String getAsClSfxValue();
        void setAsClSfxValue(String value);
    }

    @DynamicBean.Namespace(as = AsClassPnsNameSpacingIface.class)
    interface AsClassPnsNameSpacingIface extends AsClassSfxNameSpacingIface {

        @Override
        @Namespace(as = AsClassNameSpacingIface.class)
        String getAsClValue();

        @Override
        String getAsClSfxValue();
    }
}
