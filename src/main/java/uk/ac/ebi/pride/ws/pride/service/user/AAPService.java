package uk.ac.ebi.pride.ws.pride.service.user;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.pride.archive.repo.services.user.UserSummary;
import uk.ac.ebi.pride.archive.repo.util.AAPConstants;
import uk.ac.ebi.pride.ws.pride.models.user.ChangePassword;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AAPService {

    private RestTemplate restTemplate;
    private String aapToken;
    private Map<String,String> prideAAPDomainsMap;

    @Value("${aap.auth.url}")
    private String aapAuthURL;

    @Value("${aap.domain.management.url}")
    private String aapDomainMngmtURL;

    @Value("${aap.pride.service.uname}")
    private String aapUname;

    @Value("${aap.pride.service.pwd}")
    private String aapPwd;

    @Value("${aap.domain.url}")
    private String aapDomainsURL;

    AAPService(){
        restTemplate = new RestTemplate();
        /*getAAPToken();
        getAAPDomains();*/
    }


    /*To create authorization header with a valid user token
    * Used while updating user specific data in aap*/
    private HttpHeaders frameUserAuthToken(String token){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer "+token);
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON_UTF8));
        return headers;
    }

    /*Used to send AAP token in the headers of requests*/
    private HttpHeaders createAAPTokenAuthHeaders(){
        //refresh token
        getAAPToken();

        //create headers
        HttpHeaders headers = new HttpHeaders();
        headers.add( "Authorization", "Bearer "+aapToken );
        /*headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON_UTF8));*/
        return headers;
    }

    /*To get AAP token initially*/
    private HttpHeaders createBasicAuthHeaders(String username, String password){
        HttpHeaders headers = new HttpHeaders();
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(Charset.forName("UTF-8")) );
        String authHeader = "Basic " + new String( encodedAuth );
        headers.add( "Authorization", authHeader );
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        return headers;
    }

    private HttpHeaders createChangePwdHeaders(String username, String password){
        HttpHeaders headers = new HttpHeaders();
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(Charset.forName("UTF-8")) );
        String authHeader = "Basic " + new String( encodedAuth );
        headers.add( "Authorization", authHeader );
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        return headers;
    }

    public String getAAPToken(){
        ResponseEntity<String> responseEntity = restTemplate.exchange(aapAuthURL+"?ttl=180", HttpMethod.GET,new HttpEntity(createBasicAuthHeaders(aapUname,aapPwd)),String.class);
        log.info("getAAPToken() Status:"+responseEntity.getStatusCode());
        if(responseEntity.getStatusCode().is2xxSuccessful()){
            aapToken=responseEntity.getBody();
            return aapToken;
        }else{
            log.error("Unable to get AAP token. Status:"+responseEntity.getStatusCode()+" Body:"+responseEntity.getBody());
            return null;
        }
    }

    protected Map<String,String> getAAPDomains(){
        ResponseEntity<String> responseEntity = restTemplate.exchange(aapDomainMngmtURL, HttpMethod.GET,new HttpEntity(createAAPTokenAuthHeaders()),String.class);
        if(!responseEntity.getStatusCode().is2xxSuccessful()){
            log.error("Cannot retrieve PRIDE domains. Error code:"+responseEntity.getStatusCode()+"  and response body:"+responseEntity.getBody());
            return null;
        }
        JSONArray domainsJsonArray = new JSONArray(responseEntity.getBody());
        prideAAPDomainsMap = new HashMap<>();
        for(int i=0;i<domainsJsonArray.length();i++){
            JSONObject domainJsonObj = domainsJsonArray.getJSONObject(i);
            prideAAPDomainsMap.put(domainJsonObj.getString(AAPConstants.DOMAIN_NAME),domainJsonObj.getString(AAPConstants.DOMAIN_REF));
        }
        return prideAAPDomainsMap;
    }

    protected boolean addUserToAAPDomain(String userRef, String domainName) {
        if(prideAAPDomainsMap==null){
            getAAPDomains();
        }
        String domainRef = prideAAPDomainsMap.get(domainName);

        ResponseEntity<String> responseEntity = restTemplate.exchange(aapDomainsURL+"/"+domainRef+"/"+userRef+"/user", HttpMethod.PUT,new HttpEntity(createAAPTokenAuthHeaders()),String.class);
        if(!responseEntity.getStatusCode().is2xxSuccessful()){
            log.error("User:"+userRef+" not added to domain:"+domainRef+" Error code:"+responseEntity.getStatusCode()+" and error body:"+responseEntity.getBody());
            return false;
        }else{
            return true;
        }
    }

    protected boolean changeAAPPassword(String userReference, ChangePassword changePassword){
        String changePwdJson = "{\"password\" : \""+changePassword.getNewPassword()+"\"}";
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(aapAuthURL + "?_method=patch", HttpMethod.POST, new HttpEntity(changePwdJson, createChangePwdHeaders(changePassword.getEmail(), changePassword.getOldPassword())), String.class);
            return responseEntity.getStatusCode().is2xxSuccessful();
        }catch(Exception e){
            log.error(e.getMessage(),e);
            return false;
        }
    }

    public boolean updateUserData(String token, String userReference, UserSummary userSummary) {
        String org = userSummary.getAffiliation().substring(0, userSummary.getAffiliation().length()<=255?userSummary.getAffiliation().length():255);//AAP limits org size to 255bytes
        String updateUserRegJSON = "{\"username\" : \""+userSummary.getEmail()+"\",     \"email\" : \""+userSummary.getEmail()+"\",     \"name\" : \""+userSummary.getFirstName()+" "+userSummary.getLastName()+"\",     \"organisation\" : \""+org+"\"}";
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(aapAuthURL + "/" + userReference , HttpMethod.PUT, new HttpEntity(updateUserRegJSON, frameUserAuthToken(token)), String.class);
            return responseEntity.getStatusCode().is2xxSuccessful();
        }catch(Exception e){
            log.error(e.getMessage(),e);
            return false;
        }
    }
}
