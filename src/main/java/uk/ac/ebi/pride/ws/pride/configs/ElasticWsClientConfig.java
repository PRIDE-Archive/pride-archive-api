package uk.ac.ebi.pride.ws.pride.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.pride.archive.elastic.client.ElasticAPProjectClient;
import uk.ac.ebi.pride.archive.elastic.client.ElasticClientFactory;
import uk.ac.ebi.pride.archive.elastic.client.ElasticProjectClient;

@Configuration
public class ElasticWsClientConfig {

    private final ElasticClientFactory elasticClientFactory;

    private final ElasticProjectClient elasticProjectClient;

    private final ElasticAPProjectClient elasticAPProjectClient;

    public ElasticWsClientConfig(@Value("${elastic-ws.baseUrl}") String apiBaseUrl,
                                 @Value("${elastic-ws.keyName}") String apiKeyName,
                                 @Value("${elastic-ws.keyValue}") String apiKeyValue) {

        this.elasticClientFactory = new ElasticClientFactory(apiBaseUrl, apiKeyName, apiKeyValue, "pride-api");
        this.elasticProjectClient = elasticClientFactory.getProjectElasticClient();
        this.elasticAPProjectClient = elasticClientFactory.getElasticAPProjectClient();

    }

    @Bean
    public ElasticProjectClient getElasticProjectClient() {
        return elasticProjectClient;
    }

    @Bean
    public ElasticAPProjectClient getElasticAPProjectClient() {
        return elasticAPProjectClient;
    }
}
