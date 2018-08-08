package uk.ac.ebi.pride.ws.pride.configs;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 14/06/2018.
 */

@Configuration
public class SwaggerConfig {

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
