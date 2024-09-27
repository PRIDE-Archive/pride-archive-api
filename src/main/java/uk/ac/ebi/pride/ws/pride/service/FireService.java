package uk.ac.ebi.pride.ws.pride.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.pride.ws.pride.configs.FireConfig;
import uk.ac.ebi.pride.ws.pride.models.file.FireObject;

import java.io.InvalidObjectException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
public class FireService {
    private final RestTemplate fireRestTemplate;
    private final FireConfig fireConfig;

    private String fireObjUrl;
    private ObjectMapper objectMapper;


    @Autowired
    public FireService(@Qualifier("fireRestTemplate") RestTemplate fireRestTemplate, FireConfig fireConfig) {
        this.fireRestTemplate = fireRestTemplate;
        this.fireConfig = fireConfig;

        String fireBaseUrl = fireConfig.getFireUrl();
        Assert.notNull(fireBaseUrl, "fireBaseUrl is null");
        log.info("fireBaseUrl: {}", fireBaseUrl);

        if (!fireBaseUrl.endsWith("/")) {
            fireBaseUrl += "/";
        }
        fireObjUrl = fireBaseUrl + "fire/objects";

        objectMapper = new ObjectMapper();
    }


    public String getcheckSumOfFiles(String projectpath) {
        HttpHeaders headers = getCommomHeaders();
        HttpEntity requestEntity = new HttpEntity(headers);
        try {
            ResponseEntity<String> response = fireRestTemplate.exchange(fireObjUrl + "/entries/path/" + projectpath, HttpMethod.GET, requestEntity, String.class);
            if (response.getBody() == null) {
                throw new InvalidObjectException("Empty response body from FIRE API");
            }
            List<FireObject> fireObjs = objectMapper.readValue(response.getBody(), new TypeReference<List<FireObject>>() {
            });
            return toCsv(fireObjs);

        } catch (Exception ex) {
            if (ex instanceof HttpClientErrorException) {
                HttpClientErrorException e = (HttpClientErrorException) ex;
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    throw e;
                }
            }
        }
        return null;
    }

    public static String toCsv(List<FireObject> fireObjects) {
        StringBuilder tsvBuilder = new StringBuilder();
        tsvBuilder.append("File-Name\tFile-MD5Checksum\tFile-Size\n");

        for (FireObject fireObject : fireObjects) {
            tsvBuilder.append(fireObject.getFilesystemEntry().getPath().split("/")[4])
                    .append("\t")
                    .append(fireObject.getObjectMd5())
                    .append("\t")
                    .append(fireObject.getObjectSize())
                    .append("\n");
        }

        return tsvBuilder.toString();
    }

    private HttpHeaders getCommomHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = fireConfig.getFireUser() + ":" + fireConfig.getFirePasswd();
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);
        return headers;
    }
}
