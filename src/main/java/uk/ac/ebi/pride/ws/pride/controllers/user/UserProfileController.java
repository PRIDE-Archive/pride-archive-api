package uk.ac.ebi.pride.ws.pride.controllers.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import uk.ac.ebi.pride.archive.repo.repos.user.PasswordUtilities;
import uk.ac.ebi.pride.archive.repo.services.user.UserService;
import uk.ac.ebi.pride.archive.repo.services.user.UserSummary;
import uk.ac.ebi.pride.ws.pride.controllers.user.validator.UserRegistrationValidator;
import uk.ac.ebi.pride.ws.pride.service.user.UserProfileService;

import javax.validation.Valid;

//`@ApiIgnore
@RestController
@RequestMapping(path = "/user")
public class UserProfileController {

    @Autowired
    private UserRegistrationValidator userRegistrationValidator;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(userRegistrationValidator);
    }

    @Autowired
    private UserProfileService userProfileService;

    @RequestMapping(method = RequestMethod.POST,path="/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> registerNewUser(@RequestBody @Valid UserSummary userSummary,BindingResult errors){

        if(errors.hasErrors())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors.getAllErrors());
        }

        try{
            String userRef = userProfileService.registerNewUser(userSummary);
            return ResponseEntity.ok(String.valueOf(userRef));
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }


}
