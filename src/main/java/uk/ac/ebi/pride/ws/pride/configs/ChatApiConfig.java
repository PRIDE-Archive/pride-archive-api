package uk.ac.ebi.pride.ws.pride.configs;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Component
public class ChatApiConfig {

    @Value("${chat-api.base-url}")
    @Getter
    private String chatApiBaseUrl;

    @Value("${proxy-host}")
    private String proxyHost;

    @Value("${proxy-port}")
    private Integer proxyPort;

    @Bean("proxyRestTemplate")
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        if (proxyHost != null && proxyPort != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            requestFactory.setProxy(proxy);
        }
        return new RestTemplate(requestFactory);
    }

    @Bean("proxyWebClient")
    public WebClient getProxyWebClient() {
        ReactorClientHttpConnector conn = new ReactorClientHttpConnector(httpClient -> httpClient.build());
        if (proxyHost != null && proxyPort != null) {
            conn = new ReactorClientHttpConnector(httpClient -> httpClient.httpProxy(proxyOptions -> proxyOptions.address(new InetSocketAddress(proxyHost, proxyPort))));
        }
        WebClient webClient = WebClient.builder().baseUrl(chatApiBaseUrl).clientConnector(conn).build();
        return webClient;
    }


}
