package uk.ac.ebi.pride.ws.pride.controllers;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

@RestController
public class LoginController {

    @Value("${aap.auth.url}")
    private String auth_url;

    @RequestMapping(path = "getAAPToken",method = RequestMethod.POST)
    public String getAAPToken(String username, String password) throws Exception {
        ResponseEntity<String> response = null;
        try{
            HttpEntity<String> entity = new HttpEntity<>(createHttpHeaders(username,password));
            RestTemplate restTemplate = new RestTemplate();
            response = restTemplate.exchange
                    (auth_url, HttpMethod.GET, entity, String.class);
        }
        catch (HttpClientErrorException e){
            throw new Exception(String.format("username/password wrong. Please check username or password to get token"),e);
        }
        catch (Exception e){
            throw new RuntimeException("Error while getting AAP token",e);
        }
        return response.getBody();
    }

    @RequestMapping(method = RequestMethod.POST,path="/tokentest")
    public String getSecureMsg(){
        return "Token Valid";
    }

    private static HttpHeaders createHttpHeaders(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(Charset.forName("US-ASCII")) );
        String authHeader = "Basic " + new String( encodedAuth );

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authHeader);
        return headers;
    }

}
