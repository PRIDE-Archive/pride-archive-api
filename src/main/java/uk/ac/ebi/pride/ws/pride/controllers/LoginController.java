package uk.ac.ebi.pride.ws.pride.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.pride.ws.pride.models.user.Credentials;
import uk.ac.ebi.pride.ws.pride.utils.APIError;

import javax.validation.Valid;
import java.nio.charset.Charset;

@RestController
public class LoginController {

    @Value("${aap.auth.url}")
    private String auth_url;

    @ApiOperation(notes = "Get a valid access token", value = "access token", nickname = "getAAPToken", tags = {"authorization"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class),
            @ApiResponse(code = 400, message = "Bad Request", response = APIError.class)
    })
    @RequestMapping(path = "getAAPToken",method = RequestMethod.POST)
    public String getAAPToken(@RequestBody @Valid Credentials credentials) throws Exception {
        ResponseEntity<String> response = null;
        try{
            HttpEntity<String> entity = new HttpEntity<>(createHttpHeaders(credentials.getUsername(),credentials.getPassword()));
            RestTemplate restTemplate = new RestTemplate();
            response = restTemplate.exchange
                    (auth_url, HttpMethod.GET, entity, String.class);
        }
        catch (HttpClientErrorException e){
            throw new Exception("username/password wrong. Please check username or password to get token",e);
        }
        catch (Exception e){
            throw new RuntimeException("Error while getting AAP token",e);
        }
        return response.getBody();
    }

    @ApiOperation(notes = "Get a valid access token", value = "access token", nickname = "test-token-validity", tags = {"authorization"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class),
            @ApiResponse(code = 400, message = "Bad Request", response = APIError.class)
    })
    @RequestMapping(method = RequestMethod.POST,path="/token-validation")
    public String getTokenValidity(){
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
