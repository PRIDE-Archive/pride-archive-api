package uk.ac.ebi.pride.ws.pride;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.HateoasSortHandlerMethodArgumentResolver;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.core.EvoInflectorRelProvider;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import uk.ac.ebi.pride.solr.indexes.pride.config.HttpSolrConfiguration;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideSolrDataset;
import uk.ac.ebi.pride.ws.pride.controllers.DatasetController;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.hateoas.FacetsResourceAssembler;
import uk.ac.ebi.pride.ws.pride.utils.SimpleCORSFilter;

/**
 * Retrieve the datasets {@link uk.ac.ebi.pride.archive.dataprovider.dataset.DatasetProvider} from PRIDE Archive and the corresponding information.
 *
 * @author ypriverol
 *
 */

@EnableSwagger2
@SpringBootApplication(scanBasePackageClasses = {DatasetController.class, SimpleCORSFilter.class, HttpSolrConfiguration.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public Docket swaggerSpringMvcPlugin() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(paths())
                .build().apiInfo(apiInfo());
    }

    /**
     * This function exclude all the paths we don't want to show in the swagger documentation.
     * @return List of paths
     */
    private Predicate<String> paths() {
            return Predicates.not(PathSelectors.regex("/error"));
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

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("PRIDE Archive Restful WS")
                .description("The PRIDE PRoteomics IDEntifications (PRIDE) database is a centralized, standards compliant, public data repository for proteomics data, including protein and peptide identifications, post-translational modifications and supporting spectral evidence. ")
                .contact(new Contact("PRIDE Support Team", "www.ebi.ac.uk/pride", "pride-support@ebi.ac.uk"))
                .license("Apache License Version 2.0")
                .version("2.0")
                .build();
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
    public CustomPagedResourcesAssembler<PrideSolrDataset> customPagedResourcesAssembler(){
        return new CustomPagedResourcesAssembler<PrideSolrDataset>(pageableResolver(), facetResourceAssembler());
    }
}
