package org.libsmith.anvil.net;

import org.junit.Before;
import org.junit.Test;

import javax.naming.NameNotFoundException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 22.02.2016 17:20
 */
public class DNSClientIT {
    private static final String UNKNOWN_HOST = "hernya.kakayato.hesushestvuyashaya.ru";

    private DNSClient dnsClient = DNSClient.defaultInstance();

    @Before
    public void before() {
        Logger.getLogger(DNSClient.class.getName()).setLevel(Level.FINEST);
        for (Handler handler : Logger.getLogger("").getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                handler.setLevel(Level.FINEST);
            }
        }
    }

    @Test
    public void testResolveA() throws Exception {
        List<InetAddress> addresses = dnsClient.resolveA("google.com");
        assertFalse(addresses.isEmpty());
    }

    @Test(expected = NameNotFoundException.class)
    public void testUnknownHostResolveA() throws Exception {
        dnsClient.resolveA(UNKNOWN_HOST);
    }

    @Test
    public void testResolveARecord() throws Exception {
        assertNotNull(dnsClient.resolveARecord("google.com"));
    }

    @Test(expected = NameNotFoundException.class)
    public void testUnknownHostResolveARecord() throws Exception {
        dnsClient.resolveA(UNKNOWN_HOST);
    }

    @Test
    public void testResolveAAAA() throws Exception {
        List<InetAddress> addresses = dnsClient.resolveAAAA("google.com");
        assertFalse(addresses.isEmpty());
    }

    @Test
    public void testUnknownResolveAAAA() throws Exception {
        assertTrue(dnsClient.resolveAAAA("narod.ru").isEmpty());
    }

    @Test(expected = NameNotFoundException.class)
    public void testUnknownHostResolveAAAA() throws Exception {
        dnsClient.resolveAAAA(UNKNOWN_HOST);
    }

    @Test
    public void testResolveAAAARecord() throws Exception {
        assertNotNull(dnsClient.resolveAAAARecord("google.com"));
    }

    @Test(expected = NameNotFoundException.class)
    public void testUnknownHostResolveAAAARecord() throws Exception {
        dnsClient.resolveAAAARecord(UNKNOWN_HOST);
    }

    @Test(expected = NameNotFoundException.class)
    public void testUnknownResolveAAAARecord() throws Exception {
        dnsClient.resolveAAAARecord("narod.ru");
    }

    @Test
    public void testResolveCNAME() throws Exception {
        List<InetAddress> hosts = dnsClient.resolveCNAME("www.ya.ru");
        assertFalse(hosts.isEmpty());
    }

    @Test(expected = NameNotFoundException.class)
    public void testUnknownHostResolveCNAME() throws Exception {
        dnsClient.resolveCNAME(UNKNOWN_HOST);
    }

    @Test
    public void testResolveCNAMERecord() throws Exception {
        assertNotNull(dnsClient.resolveCNAMERecord("www.ya.ru"));
    }

    @Test(expected = NameNotFoundException.class)
    public void testUnknownHostResolveCNAMERecord() throws Exception {
        dnsClient.resolveCNAMERecord(UNKNOWN_HOST);
    }

    @Test(expected = NameNotFoundException.class)
    public void testUnknownResolveCNAMERecord() throws Exception {
        dnsClient.resolveCNAMERecord("google.com");
    }

    @Test
    public void testResolveMX() throws Exception {
        List<InetAddress> hosts = dnsClient.resolveMX("gmail.com");
        assertFalse(hosts.isEmpty());
    }

    @Test(expected = NameNotFoundException.class)
    public void testUnknownHostResolveMX() throws Exception {
        dnsClient.resolveMX(UNKNOWN_HOST);
    }

    @Test
    public void testResolveMXRecord() throws Exception {
        assertNotNull(dnsClient.resolveMXRecord("gmail.com"));
    }

    @Test(expected = NameNotFoundException.class)
    public void testUnknownHostResolveMXRecord() throws Exception {
        dnsClient.resolveMXRecord(UNKNOWN_HOST);
    }

    @Test(expected = NameNotFoundException.class)
    public void testUnknownResolveMXRecord() throws Exception {
        dnsClient.resolveMXRecord("www.google.com");
    }

    @Test
    public void testResolveTXT() throws Exception {
        List<String> strings = dnsClient.resolveTXT("google.com");
        assertFalse(strings.isEmpty());
    }

    @Test(expected = NameNotFoundException.class)
    public void testResolveUnknownTXT() throws Exception {
        dnsClient.resolveTXT(UNKNOWN_HOST);
    }

    @Test
    public void testResolveAPTR() throws Exception {
        List<InetAddress> strings = dnsClient.resolvePTR("8.8.8.8.in-addr.arpa");
        assertFalse(strings.isEmpty());
    }

    @Test
    public void testResolveAAAAPTR() throws Exception {
        List<InetAddress> strings = dnsClient.resolvePTR(
                "5.6.0.0.0.0.0.0.0.0.0.0.0.0.0.0.b.0.c.0.0.1.0.4.0.5.4.1.0.0.a.2.ip6.arpa");
        assertFalse(strings.isEmpty());
    }


    @Test
    public void testResolveAPTRByAddress() throws Exception {
        List<InetAddress> strings = dnsClient.resolvePTR(InetAddress.getByName("8.8.8.8"));
        assertFalse(strings.isEmpty());
    }

    @Test
    public void testResolveAAAAPTRByAddress() throws Exception {
        List<InetAddress> strings = dnsClient.resolvePTR(InetAddress.getByName("2a00:1450:4010:c0b:0:0:0:65"));
        assertFalse(strings.isEmpty());
    }

    @Test
    public void testResolvePTRRecordByName() throws Exception {
        InetAddress inetAddress = dnsClient.resolvePTRRecord("8.8.8.8.in-addr.arpa");
        assertNotNull(inetAddress.getHostName());
    }

    @Test
    public void testResolvePTRRecordByAddress() throws Exception {
        InetAddress inetAddress = dnsClient.resolvePTRRecord(InetAddress.getByName("8.8.8.8"));
        assertNotNull(inetAddress.getHostName());
    }

    @Test
    public void testResolve() throws Exception {
        List<String> googleAResolve = dnsClient.resolve("google.com", "A");
        assertFalse(googleAResolve.isEmpty());

        List<String> googleAAAAResolve = dnsClient.resolve("google.com", "AAAA");
        assertFalse(googleAAAAResolve.isEmpty());

        List<String> yandexCNAMEResolve = dnsClient.resolve("www.ya.ru", "CNAME");
        assertFalse(yandexCNAMEResolve.isEmpty());

        List<String> resolvedMXGmail = dnsClient.resolve("gmail.com", "MX");
        assertFalse(resolvedMXGmail.isEmpty());

        List<String> gdnsPTRResolve = dnsClient.resolve("4.4.8.8.in-addr.arpa", "PTR");
        assertFalse(gdnsPTRResolve.isEmpty());
    }

    @Test
    public void testNonDefaultServers() throws Exception {
        List<InetAddress> yandexAResolve = new DNSClient(new InetSocketAddress("8.8.8.8", 53)).resolveA("ya.ru");
        assertFalse(yandexAResolve.isEmpty());

        List<InetAddress> yandexAResolveMore = new DNSClient(InetAddress.getByName("8.8.4.4")).resolveA("ya.ru");
        assertFalse(yandexAResolveMore.isEmpty());
    }
}
