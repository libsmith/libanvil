package org.libsmith.anvil.net;

import org.assertj.core.data.Percentage;
import org.junit.Before;
import org.junit.Test;
import org.libsmith.anvil.AbstractTest;
import org.libsmith.anvil.UncheckedException;
import org.libsmith.anvil.net.MailDelivery.DeliveryResult;
import org.libsmith.anvil.net.MailSessionFactory.SMTPTransportConfig;
import org.libsmith.anvil.time.ImmutableDate;

import javax.mail.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 11.07.16
 */
public class JavaMailDeliveryTest extends AbstractTest {

    private static final Logger LOG = Logger.getLogger(JavaMailDeliveryTest.class.getName());

    private volatile static MockedTransport mockedTransport;

    private JavaMailDelivery.Builder builder;
    private MailSessionFactory sessionFactory;

    @Before
    public void before() throws Exception {
        sessionFactory = new MailSessionFactory().staticInstance(true);

        Supplier<Session> mockedFactory = () -> {
            Session session = sessionFactory.get();
            Provider provider = new Provider(Provider.Type.TRANSPORT, "smtp", MockedTransport.class.getName(),
                                             "test", "1.0.0");
            UncheckedException.wrap(() -> session.setProvider(provider));
            return session;
        };

        this.builder = JavaMailDelivery.builder()
                                       .name("Testing mail delivery")
                                       .sessionFactory(mockedFactory);
    }

    @Test
    public void genericTestMessage() throws Exception {
        sessionFactory.transportConfig(new SMTPTransportConfig().host("mx.0x0000.ru")
                                                                .port(33)
                                                                .username("ellet")
                                                                .password("pwd"));
        JavaMailDelivery delivery = builder.build();
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
            assertThat(result.getStatus()).isEqualTo(DeliveryResult.Status.SENT);
            assertThat(result.getQueuedDate()).isCloseTo(ImmutableDate.now(), 1000);
            assertThat(result.getProcessedDate()).isCloseTo(ImmutableDate.now(), 1000);
            assertThat(result.getDeliveredDate()).isNull();
            assertThat(result.getTransportTime().getDurationMillis()).isCloseTo(1000, Percentage.withPercentage(200));

            assertThat(mockedTransport.recipients).hasSameElementsAs(allRecipients);
            assertThat(mockedTransport.sentMessages).hasSameElementsAs(Collections.singletonList(message));
            assertThat(mockedTransport.host).isEqualTo("mx.0x0000.ru");
            assertThat(mockedTransport.port).isEqualTo(33);
            assertThat(mockedTransport.user).isEqualTo("ellet");
            assertThat(mockedTransport.password).isEqualTo("pwd");
        };

        assertions.accept(message.sendVia(delivery).throwExceptionOnError());
        for (int i = 0; i < 10000; i++ ){
            message.sendAsyncVia(delivery).thenApply(DeliveryResult::throwExceptionOnError).thenAccept(assertions);
        }
    }

    @Test
    public void testBlankMessage() throws IOException, MessagingException {
        JavaMailDelivery delivery = builder.build();
        MailMessage message = MailMessage.builder().build();
        DeliveryResult result = message.sendVia(delivery).throwExceptionOnError();
        assertThat(result.getMessage()).isSameAs(message);
        assertThat(result.getStatus()).isEqualTo(DeliveryResult.Status.IGNORED);
        assertThat(result.getQueuedDate()).isCloseTo(ImmutableDate.now(), 1000);
        assertThat(result.getProcessedDate()).isNull();
        assertThat(result.getDeliveredDate()).isNull();
        assertThat(result.getTransportTime()).isNull();
    }

    @Test
    public void noSessionDelivery() throws IOException, MessagingException {
        JavaMailDelivery delivery = JavaMailDelivery.builder().build();
        MailMessage message = MailMessage.builder().build();
        DeliveryResult result = message.sendVia(delivery).throwExceptionOnError();
        assertThat(result.getMessage()).isSameAs(message);
        assertThat(result.getStatus()).isEqualTo(DeliveryResult.Status.IGNORED);
        assertThat(result.getQueuedDate()).isCloseTo(ImmutableDate.now(), 1000);
        assertThat(result.getProcessedDate()).isNull();
        assertThat(result.getDeliveredDate()).isNull();
        assertThat(result.getTransportTime()).isNull();
    }

    public static class MockedTransport extends Transport {

        private final List<Message> sentMessages = Collections.synchronizedList(new ArrayList<>());
        private final List<Address> recipients = Collections.synchronizedList(new ArrayList<>());
        private volatile String host;
        private volatile int port;
        private volatile String user;
        private volatile String password;

        public MockedTransport(Session session, URLName urlname) {
            super(session, urlname);
            JavaMailDeliveryTest.mockedTransport = this; // so ugly
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
            sentMessages.add(msg);
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
        protected boolean protocolConnect(String host, int port, String user, String password)
                throws MessagingException {

            if (password == null) {
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
