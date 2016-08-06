package org.libsmith.anvil.reflection;

import org.junit.Test;

import java.util.EnumSet;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.libsmith.anvil.reflection.Modifier.*;


/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 06.08.16 17:49
 */
public class ModifierTest {

    @Test
    public void staticSetsTest() {

        assertThat(Modifier.pack(INTERFACE_MODIFIERS))
                .isEqualTo(java.lang.reflect.Modifier.interfaceModifiers());

        assertThat(Modifier.pack(CLASS_MODIFIERS))
                .isEqualTo(java.lang.reflect.Modifier.classModifiers());

        assertThat(Modifier.pack(FIELD_MODIFIERS))
                .isEqualTo(java.lang.reflect.Modifier.fieldModifiers());

        assertThat(Modifier.pack(CONSTRUCTOR_MODIFIERS))
                .isEqualTo(java.lang.reflect.Modifier.constructorModifiers());

        assertThat(Modifier.pack(METHOD_MODIFIERS))
                .isEqualTo(java.lang.reflect.Modifier.methodModifiers());

        assertThat(Modifier.pack(PARAMETER_MODIFIERS))
                .isEqualTo(java.lang.reflect.Modifier.parameterModifiers());
    }

    @Test
    public void presentInTest() {
        assertThat(STATIC.presentIn(java.lang.reflect.Modifier.STATIC)).isTrue();
        assertThat(PUBLIC.presentIn(java.lang.reflect.Modifier.PUBLIC)).isTrue();
        assertThat(FINAL.presentIn(java.lang.reflect.Modifier.FINAL)).isTrue();
        assertThat(FINAL.presentIn(java.lang.reflect.Modifier.STATIC)).isFalse();
    }

    @Test
    public void parseTest() {
        assertThat(Modifier.parse("public static final")).contains(PUBLIC, STATIC, FINAL);
        assertThat(Modifier.parse("static volatile")).contains(STATIC, VOLATILE);
        assertThat(Modifier.parse("")).isEmpty();
        assertThat(Modifier.parse("  static    final ")).contains(STATIC, FINAL);

        for (int i = 0, c = 1 << Modifier.PACKER.getMaxWidth(); i < c; i++) {
            EnumSet<Modifier> set = Modifier.unpack(i);
            String string = set.stream().map(Enum::toString).collect(joining(" ")).toLowerCase();
            assertThat(Modifier.parse(string)).isEqualTo(set);
        }

        assertThatThrownBy(() -> Modifier.parse("static fainal"))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
