package uk.ac.ebi.pride.ws.pride.controllers.aap;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
@RequestMapping(path = "/profile")
public class UserProfileController {

    @RequestMapping(method = RequestMethod.GET,path="/myprofile")

    public String getMyProfileData(Authentication authentication){
        String response = "";
        RestTemplate restTemplate = new RestTemplate();

        return response;
    }


}
