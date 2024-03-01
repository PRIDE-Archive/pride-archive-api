package uk.ac.ebi.pride.ws.pride.controllers.misc;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.pride.ws.pride.configs.ChatApiConfig;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Controller
@Slf4j
public class ChatController {
    private final ChatApiConfig chatApiConfig;
    private final RestTemplate proxyRestTemplate;


    @Autowired
    public ChatController(ChatApiConfig chatApiConfig,
                          @Qualifier("proxyRestTemplate") RestTemplate proxyRestTemplate
                          //@Qualifier("proxyWebClient") WebClient webClient
    ) {
        this.chatApiConfig = chatApiConfig;
        this.proxyRestTemplate = proxyRestTemplate;
        // this.objectMapper = new ObjectMapper();
        // this.webClient = webClient;
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

        return getResponse(query, url);
    }

    @PostMapping(path = "/chat_px", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CrossOrigin(origins = "*")
    public String pride_search(@RequestBody @NotNull Chat query) throws HttpClientErrorException {

        String chatApiBaseUrl = chatApiConfig.getChatApiBaseUrl();
        if (!chatApiBaseUrl.endsWith("/")) {
            chatApiBaseUrl += "/";
        }

        String url = chatApiBaseUrl + "pride";

        return getResponse(query, url);
    }

    private String getResponse(@RequestBody @NotNull Chat query, String url) {
        ResponseEntity<String> response;
        try {
            //  create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // build the request
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity(query, headers);

            log.info("Post Request to chat-api: " + query);
            response = getPostStringResponseEntity(url, requestEntity);
        } catch (RestClientException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        String body = response.getBody();
        return body;
    }


    @PostMapping(path = "/saveBenchmark", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CrossOrigin(origins = "*")
    public String saveBenchmark(@RequestBody @NotNull Benchmark benchmark) throws HttpClientErrorException {

        String chatApiBaseUrl = chatApiConfig.getChatApiBaseUrl();
        if (!chatApiBaseUrl.endsWith("/")) {
            chatApiBaseUrl += "/";
        }

        String url = chatApiBaseUrl + "saveBenchmark";

        ResponseEntity<String> response;
        try {
            //  create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // build the request
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity(benchmark, headers);

            log.info("Post Request to benchmark-api: " + benchmark);
            response = getPostStringResponseEntity(url, requestEntity);
        } catch (RestClientException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        String body = response.getBody();
        return body;
    }

    @GetMapping(path = "/getBenchmark")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public String getBenchmark(@RequestParam(defaultValue = "0", name = "page_num") int page_num, @RequestParam(defaultValue = "100", name = "items_per_page") @NotNull int items_per_page,
                               @RequestParam(defaultValue = "5", name = "iteration") @NotNull int iteration) throws HttpClientErrorException {

        String chatApiBaseUrl = chatApiConfig.getChatApiBaseUrl();
        if (!chatApiBaseUrl.endsWith("/")) {
            chatApiBaseUrl += "/";
        }

        if(iteration > 5){
            throw new RestClientException("Please provide iterations less than or equal to 5");
        }

        String url = chatApiBaseUrl + "getBenchmark?page_num=" + page_num + "&items_per_page=" + items_per_page + "&iteration=" + iteration ;

        ResponseEntity<String> response;
        try {
            //  create headers
            HttpHeaders headers = new HttpHeaders();
            // build the request
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity(headers);

            log.info("GET Request to get benchmark-api ");
            response = getStringResponseEntity(url, requestEntity);
        } catch (RestClientException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        String body = response.getBody();
        return body;
    }


    @GetMapping(path = "/similarProjects")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public String similarProjects(String accessions) throws HttpClientErrorException {

        String chatApiBaseUrl = chatApiConfig.getChatApiBaseUrl();
        if (!chatApiBaseUrl.endsWith("/")) {
            chatApiBaseUrl += "/";
        }

        String url = chatApiBaseUrl + "similar_projects?accessions=" + accessions;

        ResponseEntity<String> response;
        try {
            //  create headers
            HttpHeaders headers = new HttpHeaders();

            // build the request
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity(headers);

            log.info("Get Request to similarProjects: " + accessions);
            response = getStringResponseEntity(url, requestEntity);
        } catch (RestClientException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        String body = response.getBody();
        return body;
    }


    @PostMapping(path = "/saveQueryFeedback", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CrossOrigin(origins = "*")
    public String saveQueryFeedback(@RequestBody @NotNull Feedback feedback) throws HttpClientErrorException {

        String chatApiBaseUrl = chatApiConfig.getChatApiBaseUrl();
        if (!chatApiBaseUrl.endsWith("/")) {
            chatApiBaseUrl += "/";
        }

        String url = chatApiBaseUrl + "saveQueryFeedback";

        ResponseEntity<String> response;
        try {
            //  create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // build the request
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity(feedback, headers);

            log.info("Post Request to saveProjectsQueryFeedback: " + feedback);
            response = getPostStringResponseEntity(url, requestEntity);
        } catch (RestClientException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        String body = response.getBody();
        return body;
    }

    @GetMapping(path = "/getQueryFeedback")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public String getQueryFeedback(@RequestParam(defaultValue = "0", name = "page_num") int page_num, @RequestParam(defaultValue = "100", name = "items_per_page") @NotNull int items_per_page) throws HttpClientErrorException {

        String chatApiBaseUrl = chatApiConfig.getChatApiBaseUrl();
        if (!chatApiBaseUrl.endsWith("/")) {
            chatApiBaseUrl += "/";
        }

        String url = chatApiBaseUrl + "getQueryFeedback?page_num=" + page_num + "&items_per_page=" + items_per_page;

        ResponseEntity<String> response;
        try {
            //  create headers
            HttpHeaders headers = new HttpHeaders();
            // build the request
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity(headers);

            log.info("GET Request to getQueryFeedback-api ");
            response = getStringResponseEntity(url, requestEntity);
        } catch (RestClientException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        String body = response.getBody();
        return body;
    }


    @GetMapping(path = "/load")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public String load() {

        String chatApiBaseUrl = chatApiConfig.getChatApiBaseUrl();
        if (!chatApiBaseUrl.endsWith("/")) {
            chatApiBaseUrl += "/";
        }

        String url = chatApiBaseUrl + "load";

        ResponseEntity<String> response;
        try {
            //  create headers
            HttpHeaders headers = new HttpHeaders();
            // build the request
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity(headers);

            log.info("GET Request to load api ");
            response = getStringResponseEntity(url, requestEntity);
        } catch (RestClientException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        String body = response.getBody();
        return body;
    }

    @GetMapping(path = "/delete_all")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public String deleteAll() {

        String chatApiBaseUrl = chatApiConfig.getChatApiBaseUrl();
        if (!chatApiBaseUrl.endsWith("/")) {
            chatApiBaseUrl += "/";
        }

        String url = chatApiBaseUrl + "delete_all";

        ResponseEntity<String> response;
        try {
            //  create headers
            HttpHeaders headers = new HttpHeaders();
            // build the request
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity(headers);

            log.info("GET Request to delete_all api ");
            response = getStringResponseEntity(url, requestEntity);
        } catch (RestClientException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        String body = response.getBody();
        return body;
    }

    @GetMapping(path = "/get_tree")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public String get_tree() {

        String chatApiBaseUrl = chatApiConfig.getChatApiBaseUrl();
        if (!chatApiBaseUrl.endsWith("/")) {
            chatApiBaseUrl += "/";
        }

        String url = chatApiBaseUrl + "get_tree";

        ResponseEntity<String> response;
        try {
            //  create headers
            HttpHeaders headers = new HttpHeaders();
            // build the request
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity(headers);

            log.info("GET Request to get_tree api ");
            response = getStringResponseEntity(url, requestEntity);
        } catch (RestClientException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        String body = response.getBody();
        return body;
    }

    @PostMapping(path = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CrossOrigin(origins = "*")
    public String delete(@RequestBody @NotNull FileN fileN) throws HttpClientErrorException {

        String chatApiBaseUrl = chatApiConfig.getChatApiBaseUrl();
        if (!chatApiBaseUrl.endsWith("/")) {
            chatApiBaseUrl += "/";
        }

        String url = chatApiBaseUrl + "delete";

        ResponseEntity<String> response;
        try {
            //  create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // build the request
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity(fileN, headers);

            log.info("Post Request to delete-api: " + fileN);
            response = getPostStringResponseEntity(url, requestEntity);
        } catch (RestClientException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        String body = response.getBody();
        return body;
    }

    @PostMapping(path = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseBody
    @CrossOrigin(origins = "*")
    public String upload(@RequestPart("files") List<MultipartFile> files) throws HttpClientErrorException {

        String chatApiBaseUrl = chatApiConfig.getChatApiBaseUrl();
        if (!chatApiBaseUrl.endsWith("/")) {
            chatApiBaseUrl += "/";
        }

        String url = chatApiBaseUrl + "upload";

        ResponseEntity<String> response;
        try {
            //  create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);


            // Prepare the request body (multipart form data)
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            files.forEach(file -> {
                body.add("files", new FileSystemResource(convert(file)));
            });
            // build the request
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity(body, headers);

            log.info("Post Request to upload-api: ");
            response = getPostStringResponseEntity(url, requestEntity);
        } catch (RestClientException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        String body = response.getBody();
        return body;
    }


    public static File convert(MultipartFile file) {
        File convFile = new File(file.getOriginalFilename());
        try {
            convFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(file.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return convFile;
    }

    private ResponseEntity<String> getPostStringResponseEntity(String url, HttpEntity<MultiValueMap<String, String>> requestEntity) {
        ResponseEntity<String> response;
        System.out.println(url);
        System.out.println(requestEntity);
        response = proxyRestTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        HttpStatus statusCode = response.getStatusCode();
        if (statusCode != HttpStatus.OK && statusCode != HttpStatus.CREATED && statusCode != HttpStatus.ACCEPTED) {
            String errorMessage = "[POST] Received invalid response for : " + url + " : " + response;
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        return response;
    }

    private ResponseEntity<String> getStringResponseEntity(String url, HttpEntity<MultiValueMap<String, String>> requestEntity) {
        ResponseEntity<String> response;
        response = proxyRestTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        HttpStatus statusCode = response.getStatusCode();
        if (statusCode != HttpStatus.OK) {
            String errorMessage = "[GET] Received invalid response for : " + url + " : " + response;
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        return response;
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

    @Data
    public static class FileN {

        String filename;

        @Override
        public String toString() {
            return "{" +
                    "filename='" + filename + '\'' +
                    '}';
        }
    }

    @Data
    public static class Benchmark {

        String query;
        String model_a;
        String answer_a;
        String model_b;
        String answer_b;
        Integer time_a;
        Integer time_b;
        String winner;
        String judge;

        @Override
        public String toString() {
            return "{" +
                    "query='" + query + '\'' +
                    ", model_a='" + model_a + '\'' +
                    ", answer_a='" + answer_a + '\'' +
                    ", model_b='" + model_b + '\'' +
                    ", answer_b='" + answer_b + '\'' +
                    ", time_a=" + time_a +
                    ", time_b=" + time_b +
                    ", winner='" + winner + '\'' +
                    ", judge='" + judge + '\'' +
                    '}';
        }
    }

    @Data
    public static class Feedback {

        String query;
        String answer;
        String feedback;

        String model;

        String source;

        Integer time_ms;

        @Override
        public String toString() {
            return "{" +
                    "query='" + query + '\'' +
                    ", answer='" + answer + '\'' +
                    ", feedback='" + feedback + '\'' +
                    ", model='" + model + '\'' +
                    ", source='" + source + '\'' +
                    ", time_ms=" + time_ms +
                    '}';
        }
    }


//    @PostMapping(path = "/asyncchat", consumes = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
//    @CrossOrigin(origins = "*")
//    public String chatAsync(@RequestBody @NotNull Chat query) throws HttpClientErrorException {
//        return webClient.post().uri("/chat").body(BodyInserters.fromObject(query))
//                .retrieve()
//                .onStatus(
//                        httpStatus -> httpStatus.is4xxClientError(),
//                        clientResponse -> {
//                            if (clientResponse.statusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
//                                return clientResponse.bodyToMono(String.class)
//                                        .flatMap(errorResponse -> {
//                                            // Handle the error response here, log it, or return it to the client
//                                            return Mono.error(new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY, errorResponse));
//                                        });
//                            }
//                            if (clientResponse.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
//                                return clientResponse.bodyToMono(String.class)
//                                        .flatMap(errorResponse -> {
//                                            // Handle the error response here, log it, or return it to the client
//                                            return Mono.error(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, errorResponse));
//                                        });
//                            }
//                            return Mono.error(new WebClientResponseException(
//                                    clientResponse.statusCode().getReasonPhrase(),
//                                    clientResponse.statusCode().value(),
//                                    clientResponse.statusCode().toString(),
//                                    clientResponse.headers().asHttpHeaders(),
//                                    null,
//                                    StandardCharsets.UTF_8
//                            ));
//
//                        }
//                ).bodyToMono(String.class).timeout(Duration.ofMinutes(2)).block(Duration.ofMinutes(2));
//    }


}
