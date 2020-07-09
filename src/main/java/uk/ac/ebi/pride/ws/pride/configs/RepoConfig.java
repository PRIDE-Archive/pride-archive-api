package uk.ac.ebi.pride.ws.pride.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.pride.archive.repo.client.PrideRepoClientFactory;
import uk.ac.ebi.pride.archive.repo.client.ProjectRepoClient;

@Configuration
public class RepoConfig {

    private final PrideRepoClientFactory prideRepoClientFactory;

    public RepoConfig(@Value("${pride-repo.api.baseUrl}") String apiBaseUrl,
                      @Value("${pride-repo.api.keyName}") String apiKeyName,
                      @Value("${pride-repo.api.keyValue}") String apiKeyValue) {
        this.prideRepoClientFactory = new PrideRepoClientFactory(apiBaseUrl, apiKeyName, apiKeyValue, "pride-api");
    }

    @Bean
    public ProjectRepoClient getProjectRepoClient() {
        return prideRepoClientFactory.getProjectRepoClient();
    }
}
