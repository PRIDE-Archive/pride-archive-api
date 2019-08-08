package uk.ac.ebi.pride.ws.pride;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.HateoasSortHandlerMethodArgumentResolver;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.core.EvoInflectorRelProvider;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideSolrProject;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.hateoas.FacetsResourceAssembler;

/**
 * Retrieve the projects {@link uk.ac.ebi.pride.archive.dataprovider.project.ProjectProvider} from PRIDE Archive and the corresponding information.
 *
 * @author ypriverol
 *
 */

@EnableSwagger2

@SpringBootApplication
@ComponentScan({"uk.ac.ebi.pride.ws.pride","uk.ac.ebi.tsc.aap.client.security",
        "uk.ac.ebi.pride.utilities.ols.web.service.cache" , "uk.ac.ebi.pride.mongodb.configs",
        "uk.ac.ebi.pride.archive.spectra.configs"})
@Slf4j
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
