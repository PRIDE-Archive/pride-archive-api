package uk.ac.ebi.pride.ws.pride.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.pride.archive.mongo.client.*;

@Configuration
public class MongoWsClientConfig {

    private final PrideMongoClientFactory prideMongoClientFactory;
    private final ProjectMongoClient projectMongoClient;
    private final ImportedProjectMongoClient importedProjectMongoClient;
    private final FileMongoClient fileMongoClient;
    private final StatsMongoClient statsMongoClient;
    private final ReanalysisMongoClient reanalysisMongoClient;

    public MongoWsClientConfig(@Value("${mongo-ws.baseUrl}") String apiBaseUrl,
                               @Value("${mongo-ws.keyName}") String apiKeyName,
                               @Value("${mongo-ws.keyValue}") String apiKeyValue) {

        this.prideMongoClientFactory = new PrideMongoClientFactory(apiBaseUrl, apiKeyName, apiKeyValue, "pride-api");

        projectMongoClient = prideMongoClientFactory.getProjectMongoClient();
        importedProjectMongoClient = prideMongoClientFactory.getImportedProjectMongoClient();
        fileMongoClient = prideMongoClientFactory.getFileRepoClient();
        statsMongoClient = prideMongoClientFactory.getStatsMongoClient();
        reanalysisMongoClient = prideMongoClientFactory.getReanalysisMongoClient();
    }

    @Bean
    public ProjectMongoClient getProjectMongoClient() {
        return projectMongoClient;
    }

    @Bean
    public ImportedProjectMongoClient getImportedProjectMongoClient() {
        return importedProjectMongoClient;
    }

    @Bean
    public FileMongoClient getFileMongoClient() {
        return fileMongoClient;
    }

    @Bean
    public StatsMongoClient getStatsMongoClient() {
        return statsMongoClient;
    }

    @Bean
    public ReanalysisMongoClient getReanalysisMongoClient() {
        return reanalysisMongoClient;
    }
}
