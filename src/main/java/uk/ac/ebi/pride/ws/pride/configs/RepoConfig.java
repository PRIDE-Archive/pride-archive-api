package uk.ac.ebi.pride.ws.pride.configs;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private PrideRepoClientFactory prideRepoClientFactory;

    @Bean
    public ProjectRepoClient getProjectRepoClient() {
        return prideRepoClientFactory.getProjectRepoClient();
    }

    @Bean
    public PrideRepoClientFactory getPrideRepoClientFactory() {
        return new PrideRepoClientFactory(apiBaseUrl, apiKeyName, apiKeyValue);
    }
}
