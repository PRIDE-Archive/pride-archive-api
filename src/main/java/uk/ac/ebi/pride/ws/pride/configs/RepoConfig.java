package uk.ac.ebi.pride.ws.pride.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.pride.archive.repo.client.PrideRepoClientFactory;
import uk.ac.ebi.pride.archive.repo.client.ProjectRepoClient;

@Configuration
public class RepoConfig {

    @Value("${pride-repo.api.baseUrl}")
    private String apiBaseUrl;

    @Value("${pride-repo.api.keyName}")
    private String apiKeyName;

    @Value("${pride-repo.api.keyValue}")
    private String apiKeyValue;

    private PrideRepoClientFactory prideRepoClientFactory = null;

    @Bean
    public ProjectRepoClient getProjectRepoClient() {
        return getPrideRepoClientFactory().getProjectRepoClient();
    }

    private PrideRepoClientFactory getPrideRepoClientFactory() {
        if (prideRepoClientFactory == null)
            prideRepoClientFactory = new PrideRepoClientFactory(apiBaseUrl, apiKeyName, apiKeyValue);

        return prideRepoClientFactory;
    }
}
