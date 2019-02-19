package uk.ac.ebi.pride.ws.pride.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@Controller
public class IndexController {

    @RequestMapping(method = RequestMethod.GET, path = "/")
    public String getSwaggerUI(){
        return "redirect:swagger-ui.html";
    }

}
