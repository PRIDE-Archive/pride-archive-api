package uk.ac.ebi.pride.ws.pride;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Predicates;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import uk.ac.ebi.pride.ws.pride.controllers.DatasetController;
import uk.ac.ebi.pride.ws.pride.utils.SimpleCORSFilter;

/**
 * Retrieve the datasets {@link uk.ac.ebi.pride.archive.dataprovider.dataset.DatasetProvider} from PRIDE Archive and the corresponding information.
 *
 * @author ypriverol
 *
 */


@SpringBootApplication
@EnableSwagger2
@ComponentScan(basePackageClasses = {DatasetController.class, SimpleCORSFilter.class
})
public class Application {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
    }

    @Bean
    public Docket swaggerSpringMvcPlugin() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build().apiInfo(apiInfo());
    }

    @Component
    @Primary
    public class CustomObjectMapper extends ObjectMapper {
        public CustomObjectMapper() {
            setSerializationInclusion(JsonInclude.Include.NON_NULL);
            configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            enable(SerializationFeature.INDENT_OUTPUT);
        }
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
}
