package uk.ac.ebi.pride.ws.pride.configs;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
//@EnableSolrRepositories(basePackages = "uk.ac.ebi.pride.solr.indexes.repository")
@ComponentScan(basePackages = "uk.ac.ebi.pride.solr.indexes.services")
public class SolrCloudConfig {

    @Value("${spring.data.solr.host}")
    private String solrURls;

//    @Bean
//    public SolrClient solrClient() {
//        List<String> urls = Arrays.stream(solrURls.split(",")).map(String::trim).collect(Collectors.toList());
//        return new CloudSolrClient.Builder(urls).build();
//    }

//    @Bean
//    public SolrTemplate solrTemplate(SolrClient solrClient) {
//        return new SolrTemplate(solrClient);
//    }

}