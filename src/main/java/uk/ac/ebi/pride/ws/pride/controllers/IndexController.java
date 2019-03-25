package uk.ac.ebi.pride.ws.pride.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;

@ApiIgnore
@Controller
public class IndexController {

    // "/v2" or "/v2/" is the alternate path to api v2.0
    @RequestMapping(method = RequestMethod.GET, path = {"/","/v2"})
    public String getSwaggerUI(HttpServletRequest request){
        String url = request.getRequestURL().toString();

        //URL with /v2/** will be changed by Traffic manager and sent to our app as /**
        if(url.endsWith("/v2/") || url.endsWith("/v2")){
            System.out.println("V2 redirect for "+url+" => v2/swagger-ui.html");
            //this redirection happens from client side
            // and traffic manager will then remove v2/ from the URL before app gets the request from client
            return "redirect:v2/swagger-ui.html";
        }

        System.out.println("redirect for "+url+" => swagger-ui.html");
        //for "/" URL, we redirect client to actual swagger page
        return "redirect:swagger-ui.html";
    }

}
