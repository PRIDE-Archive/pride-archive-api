package uk.ac.ebi.pride.ws.pride.controllers.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.archive.repo.services.user.UserSummary;
import uk.ac.ebi.pride.ws.pride.controllers.user.validator.ChangePasswordValidator;
import uk.ac.ebi.pride.ws.pride.controllers.user.validator.UserRegistrationValidator;
import uk.ac.ebi.pride.ws.pride.models.uer.ChangePassword;
import uk.ac.ebi.pride.ws.pride.service.user.UserProfileService;
import uk.ac.ebi.tsc.aap.client.model.User;

import javax.validation.Valid;

//`@ApiIgnore
@RestController
@RequestMapping(path = "/user")
public class UserProfileController {

    @Autowired
    private UserRegistrationValidator userRegistrationValidator;

    @Autowired
    private ChangePasswordValidator changePasswordValidator;

    @InitBinder("changePassword")
    protected void initBinderChangePwd(WebDataBinder binder) {
        binder.setValidator(changePasswordValidator);
    }

    @InitBinder("userSummary")
    protected void initBinderRegister(WebDataBinder binder) {
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

    @PreAuthorize("isAuthenticated()")
    @PostMapping(path="/change-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> changePassword(@RequestBody @Valid ChangePassword changePassword,
                                                 BindingResult errors,
                                                 Authentication authentication){
        if (errors.hasErrors()) {
            // return to the initial edit user profile page
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors.getAllErrors());
        } else {
            try {
                User currentUser = (User) (authentication).getDetails();
                if(!currentUser.getEmail().equalsIgnoreCase(changePassword.getEmail())){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email mismatch occurred");
                }
                userProfileService.changePassword(currentUser.getUserReference(),changePassword);
                return ResponseEntity.ok().build();
            } catch (Exception ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
            }
        }
    }


}
