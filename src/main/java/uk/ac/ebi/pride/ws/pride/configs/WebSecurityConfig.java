package uk.ac.ebi.pride.ws.pride.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

//@EnableWebFluxSecurity
//@EnableReactiveMethodSecurity
//@Configuration
//@EnableWebFlux //https://stackoverflow.com/a/51901854/2259926 //https://docs.spring.io/spring-boot/reference/web/reactive.html#web.reactive.webflux.auto-configuration
public class WebSecurityConfig {
//
//    @Value("${security.header-name}")
//    private String headerName;
//
//    @Value("${security.api-key}")
//    private String configuredApiKey;

//    @Bean
//    public SecurityWebFilterChain securitygWebFilterChain(ServerHttpSecurity httpSecurity) {
//        httpSecurity.cors(corsSpec -> corsSpec.configurationSource(request -> {
//            CorsConfiguration configuration = new CorsConfiguration();
//            configuration.setAllowedOrigins(List.of("*"));
//            configuration.setAllowedMethods(List.of("*"));
//            configuration.setAllowedHeaders(List.of("*"));
//            return configuration;
//        }));
//        httpSecurity.csrf(ServerHttpSecurity.CsrfSpec::disable);
///*
//TODO decide if this is really needed? as https://github.com/spring-projects/spring-security/issues/6552#issuecomment-493511251 says
// "WebFlux works differently that the imperative equivalent. This means that JWT does not create a session on authentication by default."
//Creating a stateless session:
//https://github.com/spring-projects/spring-security/issues/6552#issuecomment-519398510
// The security context in a WebFlux application is stored in a ServerSecurityContextRepository.
// Its WebSessionServerSecurityContextRepository implementation, which is used by default, stores the context in session.
// Configuring a NoOpServerSecurityContextRepository instead would make our application stateless*/
////        httpSecurity.securityContextRepository(NoOpServerSecurityContextRepository.getInstance());
//
//        httpSecurity.formLogin(ServerHttpSecurity.FormLoginSpec::disable);
//        httpSecurity.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable);
//        httpSecurity.authorizeExchange(authorizeExchangeSpec -> {
//            authorizeExchangeSpec.pathMatchers(HttpMethod.OPTIONS).permitAll();
//            authorizeExchangeSpec.pathMatchers("/actuator/health").permitAll();
//            authorizeExchangeSpec.pathMatchers("/webjars/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll();
//            authorizeExchangeSpec.anyExchange().permitAll();
////            authorizeExchangeSpec.anyExchange().authenticated();
//        });
//
////        httpSecurity.addFilterAt(apiKeyAuthFilter(), SecurityWebFiltersOrder.AUTHENTICATION);
//
//        return httpSecurity.build();
//    }

//    public ApiKeyAuthFilter apiKeyAuthFilter() {
//        return new ApiKeyAuthFilter(new ApiKeyAuthenticationManager(configuredApiKey));
//    }
//
//    private class ApiKeyAuthenticationManager implements ReactiveAuthenticationManager {
//
//        private final String validApiKey;
//
//        public ApiKeyAuthenticationManager(String validApiKey) {
//            this.validApiKey = validApiKey;
//        }
//
//        @Override
//        public Mono<Authentication> authenticate(Authentication authentication) throws AuthenticationException {
//            String apiKey = (String) authentication.getCredentials();
//            if (validApiKey.equals(apiKey)) {
//                authentication.setAuthenticated(true);
//                return Mono.just(authentication);
//            } else {
//                return Mono.error(new BadCredentialsException("Invalid API Key"));
//            }
//        }
//    }
//
//    private static class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {
//        private final String apiKey;
//
//        public ApiKeyAuthenticationToken(String apiKey) {
//            super(null);
//            this.apiKey = apiKey;
//            setAuthenticated(false);
//        }
//
//        @Override
//        public Object getCredentials() {
//            return apiKey;
//        }
//
//        @Override
//        public Object getPrincipal() {
//            return null;
//        }
//    }
//
//    private class ApiKeyAuthFilter extends AuthenticationWebFilter {
//
//        public ApiKeyAuthFilter(ReactiveAuthenticationManager authenticationManager) {
//            super(authenticationManager);
//            setServerAuthenticationConverter(exchange -> {
//                String apiKey = exchange.getRequest().getHeaders().getFirst(headerName);
//                if (apiKey != null) {
//                    return Mono.just(new ApiKeyAuthenticationToken(apiKey));
//                }
//                return Mono.empty();
//            });
//            setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.anyExchange());
//        }
//    }

}