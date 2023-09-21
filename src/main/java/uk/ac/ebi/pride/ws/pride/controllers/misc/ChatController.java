package uk.ac.ebi.pride.ws.pride.controllers.misc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.pride.ws.pride.configs.ChatApiConfig;

import javax.validation.constraints.NotNull;

@Controller
@Slf4j
public class ChatController {
    private final ChatApiConfig chatApiConfig;
    private final RestTemplate proxyRestTemplate;
    private final ObjectMapper objectMapper;


    @Autowired
    public ChatController(ChatApiConfig chatApiConfig,
                          @Qualifier("proxyRestTemplate") RestTemplate proxyRestTemplate) {
        this.chatApiConfig = chatApiConfig;
        this.proxyRestTemplate = proxyRestTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping(path = "/chat", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String chat(@RequestBody @NotNull String query) throws HttpClientErrorException {

        String chatApiBaseUrl = chatApiConfig.getChatApiBaseUrl();
        if (!chatApiBaseUrl.endsWith("/")) {
            chatApiBaseUrl += "/";
        }

        String url = chatApiBaseUrl + "post-file";

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
//        System.out.println(body);
        return body;
    }

}
