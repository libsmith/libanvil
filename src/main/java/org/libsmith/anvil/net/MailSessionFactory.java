package org.libsmith.anvil.net;

import org.libsmith.anvil.text.Strings;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 11.07.16
 */
public class MailSessionFactory implements Supplier<Session> {

    public static final String SESSION_AUTHENTICATOR = "session.authenticator";

    interface TransportConfig extends Consumer<Properties>
    { }

    private volatile Session staticInstanceHolder;

    private volatile TransportConfig transportConfig;
    private volatile String sessionName;
    private volatile String defaultFrom;
    private volatile long maxMessageSize;
    private volatile boolean staticInstance;

    public MailSessionFactory transportConfig(TransportConfig transportConfig) {
        this.transportConfig = transportConfig;
        return this;
    }

    public MailSessionFactory sessionName(String sessionName) {
        this.sessionName = sessionName;
        this.staticInstanceHolder = null;
        return this;
    }

    public MailSessionFactory defaultFrom(String defaultFrom) {
        this.defaultFrom = defaultFrom;
        this.staticInstanceHolder = null;
        return this;
    }

    public MailSessionFactory maxMessageSize(long maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
        this.staticInstanceHolder = null;
        return this;
    }

    public MailSessionFactory staticInstance(boolean staticInstance) {
        this.staticInstance = staticInstance;
        this.staticInstanceHolder = null;
        return this;
    }

    @Override
    public Session get() {
        Session session = this.staticInstanceHolder;
        if (session != null) {
            return session;
        }
        Properties properties = new Properties();
        if (sessionName != null) {
            properties.put("session.name", sessionName);
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
        session = Session.getInstance(properties, (Authenticator) properties.get(SESSION_AUTHENTICATOR));
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
                properties.put(SESSION_AUTHENTICATOR, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return (new PasswordAuthentication(username, password));
                    }
                });
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
