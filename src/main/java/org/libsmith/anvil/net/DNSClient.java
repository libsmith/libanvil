package org.libsmith.anvil.net;

import org.libsmith.anvil.log.LogRecordBuilder;
import org.libsmith.anvil.time.TimePeriod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.naming.*;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.libsmith.anvil.text.Strings.ifNotBlank;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 22.02.2016 17:12
 */
public class DNSClient {

    private static final Logger LOG = Logger.getLogger(DNSClient.class.getName());

    private final InitialDirContext initialDirContext;
    private final String serverHostName;

    public static DNSClient of(@Nonnull InetSocketAddress inetSocketAddress) {
        return new DNSClient(inetSocketAddress.getHostName() + ":" + inetSocketAddress.getPort());
    }

    public static DNSClient of(@Nonnull InetAddress inetAddress) {
        return new DNSClient(inetAddress.getHostName());
    }

    public static DNSClient of(@Nonnull String serverHostName) {
        return new DNSClient(serverHostName);
    }

    public static DNSClient defaultInstance() {
        return DefaultInstanceHolder.INSTANCE;
    }

    protected DNSClient(@Nullable String serverHostName) {
        this.serverHostName = serverHostName;
        try {
            java.util.Hashtable<String, String> env = new java.util.Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
            if (serverHostName != null && !serverHostName.isEmpty()) {
                env.put("java.naming.provider.url", "dns://" + serverHostName);
            }
            initialDirContext = new InitialDirContext(env);
        }
        catch (NamingException ex) {
            throw new ResolvingRuntimeException(null, ex);
        }
    }

    public @Nonnull List<InetAddress> resolveA(@Nonnull String host) throws NameNotFoundException {
        List<String> resolved = resolve(host, A);
        if (resolved.isEmpty()) {
            return Collections.emptyList();
        }
        List<InetAddress> ret = new ArrayList<>(resolved.size());
        for (String record : resolved) {
            String[] splits = record.split("\\.");
            byte[] address = new byte[4];
            for (int i = 0; i < 4; i++) {
                short tokenValue = Short.parseShort(splits[i]);
                if (tokenValue > 255 || tokenValue < 0) {
                    throw new ResolvingRuntimeException(null, null);
                }
                address[i] = (byte) tokenValue;
            }
            try {
                ret.add(InetAddress.getByAddress(host, address));
            }
            catch (UnknownHostException ex) {
                throw new ResolvingRuntimeException(null, ex);
            }
        }
        return Collections.unmodifiableList(ret);
    }

    public @Nonnull InetAddress resolveARecord(@Nonnull String host) throws NameNotFoundException {
        List<InetAddress> inetAddresses = resolveA(host);
        if (inetAddresses.isEmpty()) {
            throw new NameNotFoundException(host);
        }
        return inetAddresses.get(0);
    }

    public @Nonnull List<InetAddress> resolveAAAA(@Nonnull String host) throws NameNotFoundException {
        List<String> resolved = resolve(host, AAAA);
        if (resolved.isEmpty()) {
            return Collections.emptyList();
        }
        List<InetAddress> ret = new ArrayList<>(resolved.size());
        for (String record : resolved) {
            String[] splits = record.split(":");
            byte[] address = new byte[16];
            int i = 0;
            for (String token : splits) {
                if (token.isEmpty()) {
                    i = 16 - (splits.length - 1 - i / 2) * 2;
                }
                else {
                    int tokenValue = Integer.parseInt(token, 16);
                    if (tokenValue > 0xFFFF || tokenValue < 0) {
                        throw new ResolvingRuntimeException(null, null);
                    }
                    address[i] = (byte) ((tokenValue & 0xFF00) >> 8);
                    address[i + 1] = (byte) (tokenValue & 0x00FF);
                    i += 2;
                }
            }
            try {
                ret.add(InetAddress.getByAddress(host, address));
            }
            catch (UnknownHostException ex) {
                throw new ResolvingRuntimeException(null, ex);
            }
        }
        return Collections.unmodifiableList(ret);
    }

    public @Nonnull InetAddress resolveAAAARecord(@Nonnull String host) throws NameNotFoundException {
        List<InetAddress> inetAddresses = resolveAAAA(host);
        if (inetAddresses.isEmpty()) {
            throw new NameNotFoundException(host);
        }
        return inetAddresses.get(0);
    }

    public @Nonnull List<InetAddress> resolveCNAME(@Nonnull String host) throws NameNotFoundException {
        List<InetAddress> resolved = new ArrayList<>();
        try {
            for (String record : resolve(host, CNAME)) {
                resolved.add(InetAddress.getByName(record));
            }
        }
        catch (UnknownHostException ex) {
            throw new ResolvingRuntimeException(null, ex);
        }
        return resolved;
    }

    public @Nonnull InetAddress resolveCNAMERecord(@Nonnull String host) throws NameNotFoundException {
        List<InetAddress> inetAddresses = resolveCNAME(host);
        if (inetAddresses.isEmpty()) {
            throw new NameNotFoundException(host);
        }
        return inetAddresses.get(0);
    }

    public @Nonnull List<InetAddress> resolveMX(@Nonnull String host) throws NameNotFoundException {
        List<InetAddress> resolved = new ArrayList<>();
        for (String record : resolve(host, MX)) {
            String[] split = record.split(" ", 2);
            try {
                resolved.add(InetAddress.getByName(split.length == 1 ? split[0] : split[1]));
            }
            catch (UnknownHostException ex) {
                throw new ResolvingRuntimeException(null, ex);
            }
        }
        return resolved;
    }

    public @Nonnull InetAddress resolveMXRecord(@Nonnull String host) throws NameNotFoundException {
        List<InetAddress> inetAddresses = resolveMX(host);
        if (inetAddresses.isEmpty()) {
            throw new NameNotFoundException(host);
        }
        return inetAddresses.get(0);
    }

    public @Nonnull List<String> resolveTXT(@Nonnull String host) throws NameNotFoundException {
        return resolve(host, TXT);
    }

    public @Nonnull List<InetAddress> resolvePTR(@Nonnull String host) throws NameNotFoundException {
        List<InetAddress> resolved = new ArrayList<>();
        boolean aaaa = host.endsWith("ip6.arpa") || host.endsWith("ip6-arpa.");
        for (String record : resolve(host, PTR)) {
            List<InetAddress> inetAddresses = aaaa ? resolveAAAA(record) : resolveA(record);
            resolved.add(inetAddresses.get(0));
        }
        return resolved;
    }

    public @Nonnull InetAddress resolvePTRRecord(@Nonnull String host) throws NameNotFoundException {
        List<InetAddress> inetAddresses = resolvePTR(host);
        if (inetAddresses.isEmpty()) {
            throw new NameNotFoundException(host);
        }
        return inetAddresses.get(0);
    }

    public @Nonnull InetAddress resolvePTRRecord(@Nonnull InetAddress inetAddress) throws NameNotFoundException {
        List<InetAddress> inetAddresses = resolvePTR(inetAddress);
        if (inetAddresses.isEmpty()) {
            throw new NameNotFoundException(inetAddress.toString());
        }
        return inetAddresses.get(0);
    }

    public @Nonnull List<InetAddress> resolvePTR(@Nonnull InetAddress inetAddress) throws NameNotFoundException {
        StringBuilder sb = new StringBuilder();
        byte[] address = inetAddress.getAddress();
        if (address.length == 16) {
            for (byte b : address) {
                sb.insert(0, '.');
                sb.insert(0, Integer.toHexString((b & 0xF0) >> 4));
                sb.insert(0, '.');
                sb.insert(0, Integer.toHexString(b & 0x0F));
            }
            sb.append("ip6.arpa");
        }
        else if (address.length == 4) {
            for (byte b : address) {
                sb.insert(0, '.');
                sb.insert(0, b & 0xFF);
            }
            sb.append("in-addr.arpa");
        }

        List<InetAddress> resolved = new ArrayList<>();
        try {
            for (String record : resolve(sb.toString(), PTR)) {
                resolved.add(InetAddress.getByAddress(record, inetAddress.getAddress()));
            }
        }
        catch (UnknownHostException ex) {
            throw new ResolvingRuntimeException(null, ex);
        }
        return resolved;
    }

    public @Nonnull List<String> resolve(@Nonnull String name, @Nonnull String type) throws NameNotFoundException {
        return resolve(name, new String[] { type });
    }

    private @Nonnull List<String> resolve(@Nonnull String name, @Nonnull String[] types) throws NameNotFoundException {
        assert types.length == 1;
        String type = types[0];
        long start = System.nanoTime();
        try {
            LOG.finest(() -> MessageFormat.format("Resolve {0} via {1}", name, getServerNameDescription()));
            Attributes attributes = initialDirContext.getAttributes(name, types);
            List<String> list = Collections.emptyList();
            if (attributes != null) {
                Attribute attribute = attributes.get(type);
                if (attribute != null && attribute.size() > 0) {
                    list = new ArrayList<>(attribute.size());
                    for (int i = 0; i < attribute.size(); i++) {
                        list.add((String) attribute.get(i));
                    }
                }
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(LogRecordBuilder.fine()
                                        .withMessage("Resolved {0} as {1} via {2}; {3}")
                                        .withParameters(type, list.isEmpty() ? "empty list" : list,
                                                        getServerNameDescription(),
                                                        TimePeriod.tillNowFromMillis(start)));
            }

            return list;
        }
        catch (NameNotFoundException ex) {
            LOG.finer(() -> formatException(name, start, ex));
            throw ex;
        }
        catch (CommunicationException ex) {
            LOG.finer(() -> formatException(name, start, ex));
            if (ex.getCause() instanceof IOException) {
                throw new ResolvingCommunicationException(ex.getMessage(), ex.getCause());
            }
            throw new ResolvingCommunicationException(null, ex);
        }
        catch (ConfigurationException ex) {
            LOG.finer(() -> formatException(name, start, ex));
            if (ex.getCause() instanceof UnknownHostException) {
                throw new ResolvingCommunicationException(ex.getMessage(), ex.getCause());
            }
            throw new ResolvingRuntimeException(null, ex);
        }
        catch (NamingException ex) {
            LOG.finer(() -> formatException(name, start, ex));
            throw new ResolvingRuntimeException(null, ex);
        }
    }

    private String formatException(String name, long start, Throwable th) {
        return MessageFormat.format("Resolve {0} via {1} failed; {2}; {3}",
                                    name, getServerNameDescription(),
                                    TimePeriod.tillNowFromNanos(start), th);
    }

    private String getServerNameDescription() {
        return ifNotBlank(serverHostName).orElse("default server");
    }

    public static class ResolvingRuntimeException extends RuntimeException {

        private static final long serialVersionUID = -7058305943001458889L;

        private ResolvingRuntimeException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ResolvingCommunicationException extends ResolvingRuntimeException {

        private static final long serialVersionUID = -2787192644452509106L;

        private ResolvingCommunicationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final String[] A     = { "A"     };
    private static final String[] AAAA  = { "AAAA"  };
    private static final String[] CNAME = { "CNAME" };
    private static final String[] TXT   = { "TXT"   };
    private static final String[] MX    = { "MX"    };
    private static final String[] PTR   = { "PTR"   };

    private static final class DefaultInstanceHolder {
        private static final DNSClient INSTANCE = new DNSClient(null);
    }
}
