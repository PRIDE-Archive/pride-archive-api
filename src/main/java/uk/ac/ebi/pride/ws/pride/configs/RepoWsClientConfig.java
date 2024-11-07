package uk.ac.ebi.pride.ws.pride.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.pride.archive.repo.client.PrideRepoClientFactory;
import uk.ac.ebi.pride.archive.repo.client.ProjectRepoClient;
import uk.ac.ebi.pride.archive.repo.client.StatRepoClient;

@Configuration
public class RepoWsClientConfig {

    private final PrideRepoClientFactory prideRepoClientFactory;

    public RepoWsClientConfig(@Value("${pride-repo.api.baseUrl}") String apiBaseUrl,
                              @Value("${pride-repo.api.keyName}") String apiKeyName,
                              @Value("${pride-repo.api.keyValue}") String apiKeyValue) {
        this.prideRepoClientFactory = new PrideRepoClientFactory(apiBaseUrl, apiKeyName, apiKeyValue, "pride-api");
    }

    @Bean
    public ProjectRepoClient getProjectRepoClient() {
        return prideRepoClientFactory.getProjectRepoClient();
    }

    @Bean
    public StatRepoClient getStatsRepoClient() {
        return prideRepoClientFactory.getStatRepoClient();
    }
}
