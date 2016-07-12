package org.libsmith.anvil.net;

import javax.annotation.Nullable;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 12.07.16
 */
public class MailUtils {

    private static final Map<Message.RecipientType, JavaMailDelivery.Header> TYPE_HEADER_MAP;

    static {
        Map<Message.RecipientType, JavaMailDelivery.Header> map = new HashMap<>();
        map.put(Message.RecipientType.TO, JavaMailDelivery.Header.X_ORIGINAL_TO);
        map.put(Message.RecipientType.CC, JavaMailDelivery.Header.X_ORIGINAL_CC);
        map.put(Message.RecipientType.BCC, JavaMailDelivery.Header.X_ORIGINAL_BCC);
        TYPE_HEADER_MAP = Collections.unmodifiableMap(map);
    }

    private MailUtils()
    { }

    public static @Nullable String getOriginalRecipients(Message message, Message.RecipientType recipientType) {
        try {
            JavaMailDelivery.Header originalHeader = TYPE_HEADER_MAP.get(recipientType);
            String[] originalTo = originalHeader == null ? null : message.getHeader(originalHeader.NAME);
            if (originalTo != null && originalTo.length > 0) {
                return String.join(", ", (CharSequence[]) originalTo);
            }
            Address[] recipients = message.getRecipients(recipientType);
            return InternetAddress.toString(recipients);
        }
        catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static InternetAddress parseAddress(String address) throws UnsupportedEncodingException, AddressException {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }
        InternetAddress internetAddress = new InternetAddress(address);
        String personal = internetAddress.getPersonal();
        if (personal != null) {
            internetAddress.setPersonal(personal, "UTF-8");
        }
        return internetAddress;
    }
}
