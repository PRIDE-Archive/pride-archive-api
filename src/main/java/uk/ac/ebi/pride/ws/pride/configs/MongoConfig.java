package uk.ac.ebi.pride.ws.pride.configs;

import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    private final PrideMongoClientFactory prideMongoClientFactory;

//    ProjectMongoClient projectMongoClient = prideMongoClientFactory.getProjectMongoClient();
//    FileMongoClient fileRepoClient = prideMongoClientFactory.getFileRepoClient();
}
