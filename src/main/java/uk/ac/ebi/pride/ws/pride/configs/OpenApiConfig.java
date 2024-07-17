package uk.ac.ebi.pride.ws.pride.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.URI;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class OpenApiConfig {

    @Value("${spring.webflux.base-path}")
    private String basePath;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "PrideBearerAuth";
        final String apiTitle = "PRIDE Archive Restful WS";
        final String desc = "The PRIDE PRoteomics IDEntifications (PRIDE) database is a centralized, standards compliant, public data repository for proteomics data, including protein and peptide identifications, post-translational modifications and supporting molecules evidence.";
        final String apiVersion = "3.0";
        final String license = "Apache License Version 2.0";
        return new OpenAPI()
                .info(new Info().title(apiTitle).version(apiVersion).description(desc));
//                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName)) //allows to add global security schema and to get rid of writing security to @Operation of each controller method.
//                .components(
//                        new Components()
//                                .addSecuritySchemes(securitySchemeName,
//                                        new SecurityScheme()
//                                                .name(securitySchemeName)
//                                                .type(SecurityScheme.Type.HTTP)
//                                                .scheme("bearer")
//                                                .bearerFormat("JWT")
//                                )
//                )
    }

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return route(GET("/"), req ->
                ServerResponse.permanentRedirect(URI.create(basePath + "/swagger-ui.html")).build()
        );
    }
}
