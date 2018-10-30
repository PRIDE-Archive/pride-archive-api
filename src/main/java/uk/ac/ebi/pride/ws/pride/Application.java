package uk.ac.ebi.pride.ws.pride;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.HateoasSortHandlerMethodArgumentResolver;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.core.EvoInflectorRelProvider;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideSolrProject;
import uk.ac.ebi.pride.ws.pride.configs.MongoProjectConfig;
import uk.ac.ebi.pride.ws.pride.configs.SolrCloudConfig;
import uk.ac.ebi.pride.ws.pride.configs.SwaggerConfig;
import uk.ac.ebi.pride.ws.pride.controllers.annotator.AnnotatorController;
import uk.ac.ebi.pride.ws.pride.controllers.file.FileController;
import uk.ac.ebi.pride.ws.pride.controllers.file.MSRunController;
import uk.ac.ebi.pride.ws.pride.controllers.project.ProjectController;
import uk.ac.ebi.pride.ws.pride.controllers.stats.StatsController;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.hateoas.FacetsResourceAssembler;
import uk.ac.ebi.pride.ws.pride.utils.SimpleCORSFilter;

/**
 * Retrieve the projects {@link uk.ac.ebi.pride.archive.dataprovider.project.ProjectProvider} from PRIDE Archive and the corresponding information.
 *
 * @author ypriverol
 *
 */

@EnableSwagger2
@SpringBootApplication/*(scanBasePackageClasses = {ProjectController.class, FileController.class,
        SimpleCORSFilter.class, SolrCloudConfig.class, MongoProjectConfig.class, SwaggerConfig.class})*/
@ComponentScan({"uk.ac.ebi.pride.ws.pride","uk.ac.ebi.tsc.aap.client.security"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Component
    @Primary
    private class CustomObjectMapper extends ObjectMapper {
        public CustomObjectMapper() {
            setSerializationInclusion(JsonInclude.Include.NON_NULL);
            configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
            enable(SerializationFeature.INDENT_OUTPUT);

        }
    }

    @Bean
    public RelProvider relProvider() {
        return new EvoInflectorRelProvider();
    }

    @Bean
    public ResourceAssembler facetResourceAssembler(){
        return new FacetsResourceAssembler();
    }

    @Bean
    public HateoasPageableHandlerMethodArgumentResolver pageableResolver() {
        return new HateoasPageableHandlerMethodArgumentResolver(sortResolver());
    }

    @Bean
    public HateoasSortHandlerMethodArgumentResolver sortResolver() {
        return new HateoasSortHandlerMethodArgumentResolver();
    }

    @SuppressWarnings("unchecked")
    @Bean
    public CustomPagedResourcesAssembler<PrideSolrProject> customPagedResourcesAssembler(){
        return new CustomPagedResourcesAssembler<PrideSolrProject>(pageableResolver(), facetResourceAssembler());
    }
}
