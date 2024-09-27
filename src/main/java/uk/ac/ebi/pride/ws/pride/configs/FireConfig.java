package uk.ac.ebi.pride.ws.pride.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
public class FireConfig {

    @Value("${fire.url}")
    private String fireUrl;

    @Value("${fire.user}")
    private String fireUser;

    @Value("${fire.password}")
    private String firePasswd;

    @Value("${proxy-host}")
    private String proxyHost;

    @Value("${proxy-port}")
    private Integer proxyPort;

    public FireConfig() {
    }

    public String getFireUrl() {
        return fireUrl;
    }

    public String getFireUser() {
        return fireUser;
    }

    public String getFirePasswd() {
        return firePasswd;
    }

    @Bean(name = "fireRestTemplate")
    public RestTemplate restTemplate() {
        final RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        if (proxyHost != null && proxyPort != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            requestFactory.setProxy(proxy);
        }
        restTemplate.setRequestFactory(requestFactory);
        return restTemplate;
    }
}
