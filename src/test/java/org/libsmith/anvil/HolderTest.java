package org.libsmith.anvil;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 10.07.16
 */
public class HolderTest extends AbstractTest {

    @Test
    public void getSetTest() throws Exception {
        Holder<String> holder = Holder.empty();
        holder.setValue("abc");
        assertThat(holder.getValue()).isEqualTo("abc");

        holder.accept("qwe");
        assertThat(holder.get()).isEqualTo("qwe");

        Holder.ThreadSafe<String> threadSafe = holder.asThreadSafeHolder();
        assertThat(threadSafe.getValue()).isEqualTo("qwe");

        threadSafe.accept("zxc");
        assertThat(threadSafe.getValue()).isEqualTo("zxc");

        threadSafe.setValue("qaz");
        assertThat(threadSafe.get()).isEqualTo("qaz");
    }

    @Test
    public void applyTest() throws Exception {
        String wsx = Holder.of("wsx").apply((v) -> v.substring(1));
        assertThat(wsx).isEqualTo("sx");
    }
}
