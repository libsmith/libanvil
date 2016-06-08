package org.libsmith.anvil.text;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 21.03.16 2:54
 */
public class Strings {
    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isNotEmpty(String string) {
        return string != null && !string.isEmpty();
    }

    public static boolean isBlank(String string) {
        if (string != null) {
            for (int p = 0, l = string.length(); p < l; p++) {
                if (!Character.isWhitespace(string.charAt(p))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isNotBlank(String string) {
        return !isBlank(string);
    }
}
