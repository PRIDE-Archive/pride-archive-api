package uk.ac.ebi.pride.ws.pride.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@Slf4j
public class IndexController {

    @Operation(hidden = true)
    @RequestMapping(method = RequestMethod.GET, path = {"/"})
    public String getSwaggerUI(HttpServletRequest request){
        String url = request.getRequestURL().toString();

        log.debug("redirect for "+url+" => swagger-ui.html");
        //for "/" URL, we redirect client to actual swagger page
        return "redirect:swagger-ui.html";
    }

}
