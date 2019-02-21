package uk.ac.ebi.pride.ws.pride.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;

@ApiIgnore
@Controller
public class IndexController {

    @RequestMapping(method = RequestMethod.GET, path = "/")
    public String getSwaggerUI(HttpServletRequest request){
        System.out.println("redirect:"+request.getRequestURL().toString()+"swagger-ui.html");
        return "redirect:"+request.getRequestURL().toString()+"swagger-ui.html";
    }

}
