package org.libsmith.anvil.net;

import com.sun.istack.internal.NotNull;
import org.libsmith.anvil.io.CountingOutputStream;
import org.libsmith.anvil.log.LogRecordBuilder;
import org.libsmith.anvil.text.Strings;
import org.libsmith.anvil.time.Stopwatch;

import javax.annotation.Nonnull;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 27.02.2015 15:58
 */
public class JavaMailDelivery implements MailDelivery {

    private static final Logger LOG = Logger.getLogger(JavaMailDelivery.class.getName());

    enum Header {
        X_ORIGINAL_TO("X-Original-TO"),
        X_ORIGINAL_CC("X-Original-CC"),
        X_ORIGINAL_BCC("X-Original-BCC");

        public final String NAME;

        Header(String name) {
            this.NAME = name;
        }
    }

    enum SessionLog {
        NONE, ON_ERROR, ALWAYS
    }

    private final String name;
    private final Supplier<Session> sessionFactory;
    private final Message override;
    private final Executor executor;
    private final SessionLog sessionLog;

    protected JavaMailDelivery(Builder builder) {
        name = builder.name;
        sessionFactory = builder.sessionFactory;
        override = builder.override;
        executor = builder.executor;
        sessionLog = builder.sessionLog;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    @Override
    public @Nonnull DeliveryResult send(final @Nonnull Message message) {
        return makeTask(message).get();
    }

    @Override
    public @Nonnull CompletableFuture<DeliveryResult> sendAsync(final @Nonnull Message message) {
        try {
            Supplier<DeliveryResult> task = makeTask(message);
            if (executor == null) {
                return CompletableFuture.supplyAsync(task);
            }
            else {
                return CompletableFuture.supplyAsync(task, executor);
            }
        }
        catch (Exception ex) {
            return CompletableFuture.completedFuture(DeliveryResult.Builder.queued(message).error(ex));
        }
    }

    private @NotNull Supplier<DeliveryResult> makeTask(final @Nonnull Message message) {
        DeliveryResult.Builder builder = DeliveryResult.Builder.queued(message);
        SessionLog sessionLog = this.sessionLog == null ? SessionLog.NONE : this.sessionLog;
        try {
            final Session session = this.sessionFactory == null ? null : this.sessionFactory.get();

            final Consumer<Level> debugLogFlusher;
            if (session != null && sessionLog != SessionLog.NONE) {
                DataPartLessPrintStreamBuffer debugLogBuffer = new DataPartLessPrintStreamBuffer();
                session.setDebugOut(debugLogBuffer);
                session.setDebug(true);
                debugLogFlusher = (level) -> {
                    debugLogBuffer.flush();
                    LOG.log(level, debugLogBuffer.toString());
                    if (level == Level.SEVERE) {
                        try {
                            File mailFile = File.createTempFile("failed-email-", ".eml");
                            try (FileOutputStream fileOutputStream = new FileOutputStream(mailFile)) {
                                message.writeTo(fileOutputStream);
                                LOG.severe("Email which screwed up was written to file " + mailFile);
                            }
                        }
                        catch (Exception ex) {
                            LOG.log(Level.SEVERE, "Error while write email to file", ex);
                        }
                    }
                };
            }
            else {
                debugLogFlusher = null;
            }

            /* Apply limits */ {
                Long limit = session == null ? null : (Long) session.getProperties().get("session.limit");
                if (limit != null && limit > 0) {
                    long size = message.getSize();
                    if (size < 0) {
                        CountingOutputStream counter = new CountingOutputStream();
                        message.writeTo(counter);
                        size = counter.getCount();
                    }
                    if (size > limit) {
                        throw new IllegalArgumentException("Message size " + size + " exceeds the maximum of " + limit);
                    }
                }
            }

            /* Apply default from address */ {
                Address[] originalFrom = message.getFrom();
                if (originalFrom == null || originalFrom.length == 0) {
                    Object from = session == null ? null : session.getProperties().get("session.from");
                    if (from instanceof String) {
                        message.setFrom(MailUtils.parseAddress(from.toString()));
                    }
                    else if (from instanceof Address) {
                        message.setFrom((Address) from);
                    }
                }
            }

            final Strings.LazyStringBuilder messageDescriptionBuilder = Strings.lazyStringBuilder();

            /* Overriding data */
            final Address[] targetRecipients;
            if (override != null) {
                Address[] overriddenRecipients = override.getAllRecipients();
                if (overriddenRecipients != null && overriddenRecipients.length > 0) {
                    messageDescriptionBuilder.append(
                            Strings.lazy(", original-recipients: {0}",
                                         Arrays.toString(message.getAllRecipients())));
                    targetRecipients = overriddenRecipients;
                }
                else {
                    targetRecipients = message.getAllRecipients();
                }
                applyOverridesToMessage(message, override);
            }
            else {
                targetRecipients = message.getAllRecipients();
            }

            message.saveChanges();

            messageDescriptionBuilder.append(Strings.lazy("id: {0}, recipients: {1}, subject: \"{2}\"",
                                                          message instanceof MimeMessage
                                                            ? ((MimeMessage) message).getMessageID()
                                                            : System.identityHashCode(message),
                                                          Arrays.toString(targetRecipients),
                                                          message.getSubject()));

            if (session == null) {
                LOG.fine(Strings.lazy("Email message suppressed because transport session is not defined {0}",
                                      messageDescriptionBuilder));
                return builder::ignored;
            }

            String sessionName = session.getProperty("session.name");
            if (sessionName != null) {
                messageDescriptionBuilder.append(", session: ").append(sessionName);
            }

            if (targetRecipients == null || targetRecipients.length == 0) {
                LOG.fine(Strings.lazy("Email message suppressed because empty message destinations {0}",
                                      messageDescriptionBuilder));
                return builder::ignored;
            }

            return () -> {
                builder.process();
                Stopwatch.Group stopwatch = Stopwatch.group("Process message ''{0}''", messageDescriptionBuilder);
                Map<String, TransportTarget> transportMap = new HashMap<>();
                try {
                    for (Address targetRecipient : targetRecipients) {
                        String transportType = targetRecipient.getType();
                        TransportTarget transportTarget = transportMap.get(transportType);
                        if (transportTarget == null) {
                            Transport transport = session.getTransport(targetRecipient);
                            stopwatch.start("open ''{0}''", Strings.lazy(() -> {
                                URLName urlName = transport.getURLName();
                                return MessageFormat.format("{0}://{1}{2}",
                                                            urlName.getProtocol(),
                                                            urlName.getHost() == null ? "localhost" : urlName.getHost(),
                                                            urlName.getPort() == -1 ? "" : ":" + urlName.getPort());
                            }));
                            transport.connect();
                            stopwatch.stop();
                            transportTarget = new TransportTarget(transport);
                            transportMap.put(transportType, transportTarget);
                        }
                        transportTarget.recipients.add(targetRecipient);
                    }
                    for (TransportTarget transportTarget : transportMap.values()) {
                        stopwatch.start("send");
                        transportTarget.transport.sendMessage(message, transportTarget.getRecipientsArray());
                        stopwatch.stop();
                    }
                    LOG.info(Strings.lazy("[{0}] Message was sent successfully, {1}", getName(), stopwatch));
                    if (sessionLog == SessionLog.ALWAYS) {
                        debugLogFlusher.accept(Level.INFO);
                    }
                    return builder.sent();
                }
                catch (AuthenticationFailedException ex) {
                    if (sessionLog != SessionLog.NONE) {
                        debugLogFlusher.accept(Level.SEVERE);
                    }
                    LOG.severe(Strings.lazy("[{0}] Message send error, authentication failed. {1}", getName(), stopwatch));
                    return builder.error(ex);
                }
                catch (Exception ex) {
                    if (sessionLog != SessionLog.NONE) {
                        debugLogFlusher.accept(Level.SEVERE);
                    }
                    LOG.log(LogRecordBuilder.severe("[{0}] Message send error, {1}", getName(), stopwatch)
                                            .withThrown(ex));
                    return builder.error(ex);
                }
                finally {
                    if (stopwatch.isRunning()) {
                        stopwatch.stop();
                    }
                    for (Map.Entry<String, TransportTarget> entry : transportMap.entrySet()) {
                        try {
                            entry.getValue().transport.close();
                        }
                        catch (Exception ex) {
                            LOG.log(LogRecordBuilder.severe("[{0}] Error closing transport {1}", getName(), entry.getKey())
                                                    .withThrown(ex));
                        }
                    }
                }
            };
        }
        catch (Exception ex) {
            return () -> builder.error(ex);
        }
    }

    private static void applyOverridesToMessage(Message message, Message override) throws MessagingException {
        Address[] originalTO = message.getRecipients(Message.RecipientType.TO);
        Address[] originalCC = message.getRecipients(Message.RecipientType.CC);
        Address[] originalBCC = message.getRecipients(Message.RecipientType.BCC);
        message.setRecipients(Message.RecipientType.TO, override.getRecipients(Message.RecipientType.TO));
        message.setRecipients(Message.RecipientType.CC, override.getRecipients(Message.RecipientType.CC));
        message.setRecipients(Message.RecipientType.BCC, override.getRecipients(Message.RecipientType.BCC));
        message.setHeader(Header.X_ORIGINAL_TO.NAME, InternetAddress.toString(originalTO));
        message.setHeader(Header.X_ORIGINAL_CC.NAME, InternetAddress.toString(originalCC));
        message.setHeader(Header.X_ORIGINAL_BCC.NAME, InternetAddress.toString(originalBCC));
        Address[] fromOverride = message.getFrom();
        if (fromOverride != null && fromOverride.length > 0) {
            message.setFrom(fromOverride[0]);
        }
    }

    //<editor-fold desc="class TransportTarget">
    private static class TransportTarget {
        private final Transport transport;
        private final List<Address> recipients;

        private TransportTarget(Transport transport) {
            this.transport = transport;
            this.recipients = new ArrayList<>();
        }

        public Address[] getRecipientsArray() {
            return recipients.toArray(new Address[recipients.size()]);
        }
    }
    //</editor-fold>

    //<editor-fold desc="class DataPartLessPrintStreamBuffer">
    private static class DataPartLessPrintStreamBuffer extends PrintStream {

        private final ByteArrayOutputStream baos;
        private boolean data;
        private boolean dataAccepted;
        private int skippedLines;
        private int skippedCharacters;

        public DataPartLessPrintStreamBuffer() {
            this(new ByteArrayOutputStream());
        }

        private DataPartLessPrintStreamBuffer(ByteArrayOutputStream baos) {
            super(baos);
            this.baos = baos;
        }

        @Override
        public void println(String line) {
            if (!dataAccepted) {
                super.println(line);
            }
            if (data) {
                if (line.startsWith("354")) {
                    dataAccepted = true;
                }
                else {
                    data = false;
                }
            }
            else if (dataAccepted) {
                if (line.equals(".")) {
                    super.println("... skipped " + skippedLines + " data line(s) and " +
                                  skippedCharacters + " character(s) ...");
                    data = false;
                    dataAccepted = false;
                    skippedLines = 0;
                    skippedCharacters = 0;
                }
                else {
                    skippedLines += 1;
                    skippedCharacters += line.length();
                }
            }
            else if (line.equals("DATA")) {
                data = true;
            }
        }

        @Override
        public String toString() {
            return baos.toString();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Builder">
    public static final class Builder {

        private String name;
        private Supplier<Session> sessionFactory;
        private Message override;
        private Executor executor;
        private SessionLog sessionLog;

        private Builder()
        { }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder sessionFactory(Supplier<Session> val) {
            sessionFactory = val;
            return this;
        }

        public Builder override(Message val) {
            override = val;
            return this;
        }

        public Builder executor(Executor val) {
            executor = val;
            return this;
        }

        public Builder sessionLog(SessionLog val) {
            sessionLog = val;
            return this;
        }

        public JavaMailDelivery build() {
            return new JavaMailDelivery(this);
        }
    }
    //</editor-fold>
}

