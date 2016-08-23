package org.libsmith.anvil.net;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.libsmith.anvil.AbstractTest;

import javax.naming.NameNotFoundException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 22.02.2016 17:20
 */
public class DNSClientIT extends AbstractTest {
    private static final String UNKNOWN_HOST = "hernya.kakayato.hesushestvuyashaya.ru";

    private DNSClient dnsClient = DNSClient.defaultInstance();

    @Test
    public void resolveA() throws Exception {
        List<InetAddress> addresses = dnsClient.resolveA("google.com");
        assertFalse(addresses.isEmpty());
    }

    @Test(expected = NameNotFoundException.class)
    public void unknownHostResolveA() throws Exception {
        dnsClient.resolveA(UNKNOWN_HOST);
    }

    @Test
    public void resolveARecord() throws Exception {
        assertNotNull(dnsClient.resolveARecord("google.com"));
    }

    @Test(expected = NameNotFoundException.class)
    public void unknownHostResolveARecord() throws Exception {
        dnsClient.resolveA(UNKNOWN_HOST);
    }

    @Test
    public void resolveAAAA() throws Exception {
        List<InetAddress> addresses = dnsClient.resolveAAAA("google.com");
        assertFalse(addresses.isEmpty());
    }

    @Test
    public void unknownResolveAAAA() throws Exception {
        assertTrue(dnsClient.resolveAAAA("narod.ru").isEmpty());
    }

    @Test(expected = NameNotFoundException.class)
    public void unknownHostResolveAAAA() throws Exception {
        dnsClient.resolveAAAA(UNKNOWN_HOST);
    }

    @Test
    public void resolveAAAARecord() throws Exception {
        assertNotNull(dnsClient.resolveAAAARecord("google.com"));
    }

    @Test(expected = NameNotFoundException.class)
    public void unknownHostResolveAAAARecord() throws Exception {
        dnsClient.resolveAAAARecord(UNKNOWN_HOST);
    }

    @Test(expected = NameNotFoundException.class)
    public void unknownResolveAAAARecord() throws Exception {
        dnsClient.resolveAAAARecord("narod.ru");
    }

    @Test
    public void resolveCNAME() throws Exception {
        List<InetAddress> hosts = dnsClient.resolveCNAME("www.ya.ru");
        assertFalse(hosts.isEmpty());
    }

    @Test(expected = NameNotFoundException.class)
    public void unknownHostResolveCNAME() throws Exception {
        dnsClient.resolveCNAME(UNKNOWN_HOST);
    }

    @Test
    public void resolveCNAMERecord() throws Exception {
        assertNotNull(dnsClient.resolveCNAMERecord("www.ya.ru"));
    }

    @Test(expected = NameNotFoundException.class)
    public void unknownHostResolveCNAMERecord() throws Exception {
        dnsClient.resolveCNAMERecord(UNKNOWN_HOST);
    }

    @Test(expected = NameNotFoundException.class)
    public void unknownResolveCNAMERecord() throws Exception {
        dnsClient.resolveCNAMERecord("google.com");
    }

    @Test
    public void resolveMX() throws Exception {
        List<InetAddress> hosts = dnsClient.resolveMX("gmail.com");
        assertFalse(hosts.isEmpty());
    }

    @Test(expected = NameNotFoundException.class)
    public void unknownHostResolveMX() throws Exception {
        dnsClient.resolveMX(UNKNOWN_HOST);
    }

    @Test
    public void resolveMXRecord() throws Exception {
        assertNotNull(dnsClient.resolveMXRecord("gmail.com"));
    }

    @Test(expected = NameNotFoundException.class)
    public void unknownHostResolveMXRecord() throws Exception {
        dnsClient.resolveMXRecord(UNKNOWN_HOST);
    }

    @Test(expected = NameNotFoundException.class)
    public void unknownResolveMXRecord() throws Exception {
        dnsClient.resolveMXRecord("www.google.com");
    }

    @Test
    public void resolveTXT() throws Exception {
        List<String> strings = dnsClient.resolveTXT("google.com");
        assertFalse(strings.isEmpty());
    }

    @Test(expected = NameNotFoundException.class)
    public void resolveUnknownTXT() throws Exception {
        dnsClient.resolveTXT(UNKNOWN_HOST);
    }

    @Test
    public void resolveAPTR() throws Exception {
        List<InetAddress> strings = dnsClient.resolvePTR("8.8.8.8.in-addr.arpa");
        assertFalse(strings.isEmpty());
    }

    @Test
    public void resolveAAAAPTR() throws Exception {
        List<InetAddress> strings = dnsClient.resolvePTR(
                "5.6.0.0.0.0.0.0.0.0.0.0.0.0.0.0.b.0.c.0.0.1.0.4.0.5.4.1.0.0.a.2.ip6.arpa");
        assertFalse(strings.isEmpty());
    }

    @Test
    public void resolveAPTRByAddress() throws Exception {
        List<InetAddress> strings = dnsClient.resolvePTR(InetAddress.getByName("8.8.8.8"));
        assertFalse(strings.isEmpty());
    }

    @Test
    public void resolveAAAAPTRByAddress() throws Exception {
        List<InetAddress> strings = dnsClient.resolvePTR(InetAddress.getByName("2a00:1450:4010:c0b:0:0:0:65"));
        assertFalse(strings.isEmpty());
    }

    @Test
    public void resolvePTRRecordByName() throws Exception {
        InetAddress inetAddress = dnsClient.resolvePTRRecord("8.8.8.8.in-addr.arpa");
        assertNotNull(inetAddress.getHostName());
    }

    @Test
    public void resolvePTRRecordByAddress() throws Exception {
        InetAddress inetAddress = dnsClient.resolvePTRRecord(InetAddress.getByName("8.8.8.8"));
        assertNotNull(inetAddress.getHostName());
    }

    @Test
    public void resolve() throws Exception {
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
    public void nonDefaultServers() throws Exception {
        List<InetAddress> yandexAResolve = DNSClient.of(new InetSocketAddress("8.8.8.8", 53)).resolveA("ya.ru");
        assertFalse(yandexAResolve.isEmpty());

        List<InetAddress> yandexAResolveMore = DNSClient.of(InetAddress.getByName("8.8.4.4")).resolveA("ya.ru");
        assertFalse(yandexAResolveMore.isEmpty());

        Assertions.assertThatThrownBy(() ->
            DNSClient.of("notexisting.dns.server.ru").resolveA("ya.ru")
        )       .isInstanceOf(DNSClient.ResolvingCommunicationException.class)
                .hasMessageContaining("notexisting.dns.server.ru");
    }
}
