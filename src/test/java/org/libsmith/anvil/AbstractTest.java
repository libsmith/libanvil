package org.libsmith.anvil;

import org.junit.BeforeClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 21.06.16
 */
public abstract class AbstractTest {

    @BeforeClass
    public static void initLog() throws IOException {
        try (InputStream config = AbstractTest.class.getResourceAsStream("/META-INF/test-logging.properties")) {
            LogManager.getLogManager().readConfiguration(config);
        }
    }
}
