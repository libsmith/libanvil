package org.libsmith.anvil.net.mail;

import org.assertj.core.data.Percentage;
import org.junit.Before;
import org.junit.Test;
import org.libsmith.anvil.AbstractTest;
import org.libsmith.anvil.UncheckedException;
import org.libsmith.anvil.io.CountingOutputStream;
import org.libsmith.anvil.net.mail.MailDelivery.DeliveryResult;
import org.libsmith.anvil.net.mail.MailSessionBuilder.SMTPTransportConfig;
import org.libsmith.anvil.time.ImmutableDate;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.libsmith.anvil.net.mail.JavaMailDelivery.Header.*;
import static org.libsmith.anvil.net.mail.MailDelivery.DeliveryResult.Status.SENT;
import static org.libsmith.anvil.net.mail.MailSessionBuilder.SESSION_AUTH;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 11.07.16
 */
public class JavaMailDeliveryTest extends AbstractTest {

    private static final Logger LOG = Logger.getLogger(JavaMailDeliveryTest.class.getName());

    private static final ThreadLocal<MockedTransport> MOCK_TRANSPORT_TL = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> MOCK_TRANSPORT_AUTH_FAIL = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> MOCK_TRANSPORT_SEND_FAIL = new ThreadLocal<>();

    private JavaMailDelivery.Builder deliveryBuilder;
    private Supplier<JavaMailDelivery> deliveryFactory;
    private MailSessionBuilder sessionBuilder;

    @Before
    public void before() throws Exception {
        MOCK_TRANSPORT_AUTH_FAIL.remove();
        MOCK_TRANSPORT_SEND_FAIL.remove();

        sessionBuilder = new MailSessionBuilder().name("Test session")
                                                 .transportConfig(p -> p.put(SESSION_AUTH,
                                                                             new PasswordAuthentication("test", "test")));

        Supplier<Session> mockedFactory = () -> {
            Session session = sessionBuilder.build();
            Provider provider = new Provider(Provider.Type.TRANSPORT, "smtp", MockedTransport.class.getName(),
                                             "test", "1.0.0");
            UncheckedException.wrap(() -> session.setProvider(provider));
            return session;
        };

        this.deliveryBuilder = JavaMailDelivery.builder()
                                               .name("Testing mail delivery")
                                               .executor(Executors.newCachedThreadPool())
                                               .sessionFactory(mockedFactory);
        
        this.deliveryFactory = deliveryBuilder::build;
    }

    @Test
    public void genericTestMessage() throws Exception {

        sessionBuilder.transportConfig(new SMTPTransportConfig().host("mx.0x0000.ru")
                                                                .port(33)
                                                                .username("ellet")
                                                                .password("pwd"));
        MailMessage message = MailMessage.builder()
                                         .from("Me <me@0x0000.ru>")
                                         .to("Serious Tester <test@0x0000.ru>")
                                         .cc("Other <other@0x0000.ru>")
                                         .bcc("Shadow Tester <shadow@0x0000.ru>")
                                         .subject("Important test")
                                         .build();
        List<Address> allRecipients = Arrays.asList(message.getAllRecipients());
        Consumer<DeliveryResult> assertions = (result) -> {
            assertThat(result.getMessage()).isSameAs(message);
            assertThat(result.getStatus()).isEqualTo(SENT);
            assertThat(result.getQueuedDate()).isCloseTo(ImmutableDate.now(), 1000);
            assertThat(result.getProcessedDate()).isCloseTo(ImmutableDate.now(), 1000);
            assertThat(result.getDeliveredDate()).isNull();
            assertThat(result.getTransportTime().getDurationMillis()).isCloseTo(1000, Percentage.withPercentage(200));

            MockedTransport mockedTransport = MOCK_TRANSPORT_TL.get();
            assertThat(mockedTransport.recipients).hasSameElementsAs(allRecipients);
            assertThat(mockedTransport.sentMessage).isEqualTo(message);
            assertThat(mockedTransport.host).isEqualTo("mx.0x0000.ru");
            assertThat(mockedTransport.port).isEqualTo(33);
            assertThat(mockedTransport.user).isEqualTo("ellet");
            assertThat(mockedTransport.password).isEqualTo("pwd");
        };

        assertions.accept(message.sendVia(deliveryFactory).throwExceptionOnError());

        message.sendAsyncVia(deliveryFactory)
               .thenApply(DeliveryResult::throwExceptionOnError)
               .thenAccept(assertions);

        deliveryBuilder.executor(null);

        message.sendAsyncVia(deliveryFactory)
               .thenApply(DeliveryResult::throwExceptionOnError)
               .thenAccept(assertions);
    }

    @Test
    public void sessionDefaultFromAddressTest() throws IOException, MessagingException {

        sessionBuilder.defaultFrom("Ya <me@0x0000.ru>");

        Supplier<String> sentFormSupplier = () -> UncheckedException.rethrow(() ->
                                InternetAddress.toString(MOCK_TRANSPORT_TL.get().sentMessage.getFrom()));

        MailMessage.builder()
                   .from("Other <other@0x0000.ru>")
                   .to("You <you@0x0000.ru>")
                   .build().sendVia(deliveryFactory).throwExceptionOnError();
        assertThat(sentFormSupplier.get()).isEqualTo("Other <other@0x0000.ru>");

        MailMessage.builder()
                   .to("You <you@0x0000.ru>")
                   .build().sendVia(deliveryFactory).throwExceptionOnError();
        assertThat(sentFormSupplier.get()).isEqualTo("Ya <me@0x0000.ru>");

    }

    @Test
    public void overrideTest() throws IOException, MessagingException {

        MailMessage.Builder messageBuilder =
                MailMessage.builder().from("Ya <me@0x0000.ru>")
                                     .to("Dyadya Vanya <ivan@0x0000.ru>")
                                     .cc("Mukha CC <cc@0x0000.ru>")
                                     .bcc("Mukha BCC <bcc@0x0000.ru>")
                                     .replyTo("Feedback <feedback@0x0000.ru>")
                                     .subject("Te st st st");
        MailMessage referenceMessage = messageBuilder.build();

        {
            assertThat(messageBuilder.build().sendVia(deliveryFactory).throwExceptionOnError()
                                     .getStatus()).isEqualTo(SENT);

            Message sentMessage = MOCK_TRANSPORT_TL.get().sentMessage;
            assertThat(sentMessage).extracting("from", "allRecipients", "replyTo", "subject")
                                   .containsExactly(referenceMessage.getFrom(),
                                                    referenceMessage.getAllRecipients(),
                                                    referenceMessage.getReplyTo(),
                                                    referenceMessage.getSubject());
        }

        {
            deliveryBuilder.override(MailMessage.builder()
                                                .from("Morzh Khrenovij <morzsh@0x0000.ru>")
                                                .to("Test Inbox <test@0x0000.ru>")
                                                .replyTo("Hemul <h.h.h@0x0000.ru>")
                                                .subject("Over subj!")
                                                .build());

            assertThat(messageBuilder.build().sendVia(deliveryFactory).throwExceptionOnError()
                                     .getStatus()).isEqualTo(SENT);

            Message sentMessage = MOCK_TRANSPORT_TL.get().sentMessage;
            assertThat(InternetAddress.toString(sentMessage.getFrom())).isEqualTo("Morzh Khrenovij <morzsh@0x0000.ru>");
            assertThat(InternetAddress.toString(sentMessage.getAllRecipients())).isEqualTo("Test Inbox <test@0x0000.ru>");
            assertThat(InternetAddress.toString(sentMessage.getReplyTo())).isEqualTo("Hemul <h.h.h@0x0000.ru>");
            assertThat(sentMessage.getSubject()).isEqualTo("Over subj!");

            assertThat(sentMessage.getHeader(X_ORIGINAL_FROM.NAME)).containsExactly("Ya <me@0x0000.ru>");
            assertThat(sentMessage.getHeader(X_ORIGINAL_TO.NAME)).containsExactly("Dyadya Vanya <ivan@0x0000.ru>");
            assertThat(sentMessage.getHeader(X_ORIGINAL_CC.NAME)).containsExactly("Mukha CC <cc@0x0000.ru>");
            assertThat(sentMessage.getHeader(X_ORIGINAL_BCC.NAME)).containsExactly("Mukha BCC <bcc@0x0000.ru>");
            assertThat(sentMessage.getHeader(X_ORIGINAL_REPLY_TO.NAME)).containsExactly("Feedback <feedback@0x0000.ru>");
            assertThat(sentMessage.getHeader(X_ORIGINAL_SUBJECT.NAME)).containsExactly("Te st st st");
        }

        {
            deliveryBuilder.override(MailMessage.builder()
                                                .to("Test Inbox <test@0x0000.ru>")
                                                .cc("O CC <occ@0x0000.ru>")
                                                .bcc("O BCC <obcc@0x0000.ru>")
                                                .build());

            assertThat(messageBuilder.build().sendVia(deliveryFactory).throwExceptionOnError()
                                     .getStatus()).isEqualTo(SENT);

            Message sentMessage = MOCK_TRANSPORT_TL.get().sentMessage;
            assertThat(InternetAddress.toString(sentMessage.getFrom())).isEqualTo("Ya <me@0x0000.ru>");
            assertThat(InternetAddress.toString(sentMessage.getAllRecipients()))
                    .isEqualTo("Test Inbox <test@0x0000.ru>, O CC <occ@0x0000.ru>, O BCC <obcc@0x0000.ru>");
            assertThat(InternetAddress.toString(sentMessage.getReplyTo())).isEqualTo("Feedback <feedback@0x0000.ru>");
            assertThat(sentMessage.getSubject()).isEqualTo("Te st st st");

            assertThat(sentMessage.getHeader(X_ORIGINAL_FROM.NAME)).isNull();
            assertThat(sentMessage.getHeader(X_ORIGINAL_TO.NAME)).containsExactly("Dyadya Vanya <ivan@0x0000.ru>");
            assertThat(sentMessage.getHeader(X_ORIGINAL_CC.NAME)).containsExactly("Mukha CC <cc@0x0000.ru>");
            assertThat(sentMessage.getHeader(X_ORIGINAL_BCC.NAME)).containsExactly("Mukha BCC <bcc@0x0000.ru>");
            assertThat(sentMessage.getHeader(X_ORIGINAL_REPLY_TO.NAME)).isNull();
            assertThat(sentMessage.getHeader(X_ORIGINAL_SUBJECT.NAME)).isNull();
        }

        {
            deliveryBuilder.override(MailMessage.builder()
                                                .subject("Only Subj")
                                                .build());

            assertThat(messageBuilder.build().sendVia(deliveryFactory).throwExceptionOnError()
                                     .getStatus()).isEqualTo(SENT);

            Message sentMessage = MOCK_TRANSPORT_TL.get().sentMessage;
            assertThat(sentMessage).extracting("from", "allRecipients", "replyTo")
                                   .containsExactly(referenceMessage.getFrom(),
                                                    referenceMessage.getAllRecipients(),
                                                    referenceMessage.getReplyTo());

            assertThat(sentMessage.getSubject()).isEqualTo("Only Subj");
        }
    }

    @Test
    public void testBlankMessage() throws IOException, MessagingException {

        MailMessage message = MailMessage.builder().build();
        DeliveryResult result = message.sendVia(deliveryFactory).throwExceptionOnError();
        assertThat(result.getMessage()).isSameAs(message);
        assertIgnored(result);
    }

    @Test
    public void noSessionDelivery() {

        JavaMailDelivery delivery = deliveryBuilder.sessionFactory(() -> null)
                                                   .build();
        MailMessage message = MailMessage.builder().build();
        DeliveryResult result = message.sendVia(delivery).throwExceptionOnError();
        assertThat(result.getMessage()).isSameAs(message);
        assertIgnored(result);
    }

    @Test
    public void authFailedTest() {
        MOCK_TRANSPORT_AUTH_FAIL.set(true);
        deliveryBuilder.sessionLog(JavaMailDelivery.SessionLog.ON_ERROR);
        DeliveryResult deliveryResult = MailMessage.builder().to("test@0x0000.ru").build().sendVia(deliveryFactory);
        assertThatThrownBy(deliveryResult::throwExceptionOnError)
                .hasCauseInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    public void sendFailedTest() {
        MOCK_TRANSPORT_SEND_FAIL.set(true);
        deliveryBuilder.sessionLog(JavaMailDelivery.SessionLog.ON_ERROR);
        DeliveryResult deliveryResult = MailMessage.builder().to("test@0x0000.ru").build().sendVia(deliveryFactory);
        assertThatThrownBy(deliveryResult::throwExceptionOnError)
                .hasCause(new MessagingException("Boom!"));
    }

    @Test
    public void limitsTest() throws MessagingException, IOException {
        int maxSize = 1000;
        sessionBuilder.maxMessageSize(1000);
        for (int i = 0; i < maxSize + maxSize / 5; i += maxSize / 10) {
            MailMessage message = MailMessage.builder()
                                             .to("test@0x0000.ru")
                                             .attachment(new ByteArrayDataSource(new byte[i],
                                                                                 "application/octet-stream"))
                                             .build();
            CountingOutputStream countingOutputStream = new CountingOutputStream();
            message.writeTo(countingOutputStream);
            DeliveryResult deliveryResult = message.sendVia(deliveryFactory);
            if (countingOutputStream.getCount() > maxSize) {
                assertThatThrownBy(deliveryResult::throwExceptionOnError).hasCauseInstanceOf(MessagingException.class);
            }
            else {
                deliveryResult.throwExceptionOnError();
            }
        }
    }

    private void assertIgnored(DeliveryResult result) {

        assertThat(result.getStatus()).isEqualTo(DeliveryResult.Status.IGNORED);
        assertThat(result.getQueuedDate()).isCloseTo(ImmutableDate.now(), 1000);
        assertThat(result.getProcessedDate()).isNull();
        assertThat(result.getDeliveredDate()).isNull();
        assertThat(result.getTransportTime()).isNull();
    }

    public static class MockedTransport extends Transport {

        private final List<Address> recipients = new ArrayList<>();
        private Message sentMessage;
        private String host;
        private int port;
        private String user;
        private String password;

        public MockedTransport(Session session, URLName urlname) {
            super(session, urlname);
            JavaMailDeliveryTest.MOCK_TRANSPORT_TL.set(this);
            String protocol = getURLName().getProtocol();
            setURLName(new URLName(protocol,
                                   getURLName().getHost(),
                                   Integer.parseInt(session.getProperties()
                                                           .getProperty("mail." + protocol + ".port", "-1")),
                                   getURLName().getFile(),
                                   getURLName().getUsername(),
                                   getURLName().getPassword()));

        }

        @Override
        public void sendMessage(Message msg, Address[] addresses) throws MessagingException {
            if (MOCK_TRANSPORT_SEND_FAIL.get() == Boolean.TRUE) {
                throw new MessagingException("Boom!");
            }
            if (sentMessage != null) {
                throw new RuntimeException();
            }
            sentMessage = msg;
            recipients.addAll(Arrays.asList(addresses));
            if (LOG.isLoggable(Level.FINEST)) {
                UncheckedException.rethrow(()-> {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    msg.writeTo(baos);
                    LOG.finest(baos.toString());
                });
            }
        }

        @Override
        public synchronized void close() throws MessagingException {
            super.close();
            if (MOCK_TRANSPORT_SEND_FAIL.get() == Boolean.TRUE) {
                throw new MessagingException("Boom on close!");
            }
        }

        @Override
        protected boolean protocolConnect(String host, int port, String user, String password)
                throws MessagingException {

            if (MOCK_TRANSPORT_AUTH_FAIL.get() == Boolean.TRUE || password == null) {
                return false;
            }
            this.host = host;
            this.port = port;
            this.user = user;
            this.password = password;
            return true;
        }
    }

}
