package org.libsmith.anvil.net.mail;

import org.libsmith.anvil.text.Strings;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 11.07.16
 */
public class MailSessionBuilder {

    @SuppressWarnings("RedundantStringConstructorCall")
    public static final String SESSION_AUTH = new String("session.auth");

    @FunctionalInterface
    interface TransportConfig extends Consumer<Properties>
    { }

    private volatile Session staticInstanceHolder;

    private volatile TransportConfig transportConfig;
    private volatile String name;
    private volatile String defaultFrom;
    private volatile long maxMessageSize;
    private volatile boolean staticInstance;

    public MailSessionBuilder transportConfig(TransportConfig transportConfig) {
        this.transportConfig = transportConfig;
        return this;
    }

    public MailSessionBuilder name(String name) {
        this.name = name;
        this.staticInstanceHolder = null;
        return this;
    }

    public MailSessionBuilder defaultFrom(String defaultFrom) {
        this.defaultFrom = defaultFrom;
        this.staticInstanceHolder = null;
        return this;
    }

    public MailSessionBuilder maxMessageSize(long maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
        this.staticInstanceHolder = null;
        return this;
    }

    public MailSessionBuilder staticInstance(boolean staticInstance) {
        this.staticInstance = staticInstance;
        this.staticInstanceHolder = null;
        return this;
    }

    public Session build() {
        Session session = this.staticInstanceHolder;
        if (session != null) {
            return session;
        }
        Properties properties = new Properties();
        if (name != null) {
            properties.put("session.name", name);
        }
        if (defaultFrom != null) {
            properties.put("session.from", defaultFrom);
        }
        if (maxMessageSize > 0) {
            properties.put("session.limit", maxMessageSize);
        }
        if (transportConfig != null) {
            transportConfig.accept(properties);
        }
        Object sessionAuth = properties.get(SESSION_AUTH);
        Authenticator authenticator;
        if (sessionAuth instanceof PasswordAuthentication) {
            authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return (PasswordAuthentication) sessionAuth;
                }
            };
        }
        else if (sessionAuth instanceof Authenticator) {
            authenticator = (Authenticator) sessionAuth;
        }
        else {
            throw new IllegalStateException("Unsupported auth " + sessionAuth.getClass());
        }
        session = Session.getInstance(properties, authenticator);
        if (staticInstance) {
            this.staticInstanceHolder = session;
        }
        return session;
    }

    public static class SMTPTransportConfig implements TransportConfig {

        private String host;
        private int port;
        private boolean startTLS;
        private String username;
        private String password;

        @Override
        public void accept(Properties properties) {
            int port = this.port;
            if (port <= 0) {
                port = startTLS ? 587 : 25;
            }
            boolean auth = Strings.isNotEmpty(username) || Strings.isNotEmpty(password);

            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", String.valueOf(port));
            properties.put("mail.smtp.auth", String.valueOf(auth));
            properties.put("mail.smtp.starttls.enable", startTLS);
            if (auth) {
                properties.put(SESSION_AUTH, new PasswordAuthentication(username, password));
            }
        }

        public SMTPTransportConfig host(String host) {
            this.host = host;
            return this;
        }

        public SMTPTransportConfig port(int port) {
            this.port = port;
            return this;
        }

        public SMTPTransportConfig startTLS(boolean startTLS) {
            this.startTLS = startTLS;
            return this;
        }

        public SMTPTransportConfig username(String username) {
            this.username = username;
            return this;
        }

        public SMTPTransportConfig password(String password) {
            this.password = password;
            return this;
        }
    }
}
