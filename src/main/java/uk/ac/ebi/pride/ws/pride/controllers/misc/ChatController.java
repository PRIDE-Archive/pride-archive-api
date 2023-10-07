package uk.ac.ebi.pride.ws.pride.controllers.misc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import uk.ac.ebi.pride.ws.pride.configs.ChatApiConfig;

import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Controller
@Slf4j
public class ChatController {
    private final ChatApiConfig chatApiConfig;
    private final RestTemplate proxyRestTemplate;
    private final ObjectMapper objectMapper;

    private final WebClient webClient;


    @Autowired
    public ChatController(ChatApiConfig chatApiConfig,
                          @Qualifier("proxyRestTemplate") RestTemplate proxyRestTemplate,
                          @Qualifier("proxyWebClient") WebClient webClient) {
        this.chatApiConfig = chatApiConfig;
        this.proxyRestTemplate = proxyRestTemplate;
        this.objectMapper = new ObjectMapper();
        this.webClient = webClient;
    }

    @PostMapping(path = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CrossOrigin(origins = "*")
    public String chat(@RequestBody @NotNull Chat query) throws HttpClientErrorException {

        String chatApiBaseUrl = chatApiConfig.getChatApiBaseUrl();
        if (!chatApiBaseUrl.endsWith("/")) {
            chatApiBaseUrl += "/";
        }

        String url = chatApiBaseUrl + "chat";

        ResponseEntity<String> response;
        try {
            //  create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // build the request
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity(query, headers);

            log.info("Post Request to chat-api: " + query);
            response = proxyRestTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            HttpStatus statusCode = response.getStatusCode();
            if (statusCode != HttpStatus.OK && statusCode != HttpStatus.CREATED && statusCode != HttpStatus.ACCEPTED) {
                String errorMessage = "[POST] Received invalid response for : " + url + " : " + response;
                log.error(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
        } catch (RestClientException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        String body = response.getBody();
        return body;
    }


    @PostMapping(path = "/asyncchat", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CrossOrigin(origins = "*")
    public String chatAsync(@RequestBody @NotNull Chat query) throws HttpClientErrorException {
        return webClient.post().uri("/chat").body(BodyInserters.fromObject(query))
                .retrieve()
                .onStatus(
                        httpStatus -> httpStatus.is4xxClientError(),
                        clientResponse -> {
                            if (clientResponse.statusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorResponse -> {
                                            // Handle the error response here, log it, or return it to the client
                                            return Mono.error(new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY, errorResponse));
                                        });
                            }
                            if (clientResponse.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorResponse -> {
                                            // Handle the error response here, log it, or return it to the client
                                            return Mono.error(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, errorResponse));
                                        });
                            }
                            return Mono.error(new WebClientResponseException(
                                    clientResponse.statusCode().getReasonPhrase(),
                                    clientResponse.statusCode().value(),
                                    clientResponse.statusCode().toString(),
                                    clientResponse.headers().asHttpHeaders(),
                                    null,
                                    StandardCharsets.UTF_8
                            ));

                        }
                ).bodyToMono(String.class).timeout(Duration.ofMinutes(2)).block(Duration.ofMinutes(2));
    }


    @Data
    public static class Chat {

        String prompt;

        String model_name;

        @Override
        public String toString() {
            return "{" +
                    "prompt='" + prompt + '\'' +
                    ", model_name='" + model_name + '\'' +
                    '}';
        }
    }
}
