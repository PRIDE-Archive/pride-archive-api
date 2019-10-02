package uk.ac.ebi.pride.ws.pride.controllers.user;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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
import uk.ac.ebi.pride.ws.pride.controllers.user.validator.UpdateProfileValidator;
import uk.ac.ebi.pride.ws.pride.controllers.user.validator.UserRegistrationValidator;
import uk.ac.ebi.pride.ws.pride.models.user.ChangePassword;
import uk.ac.ebi.pride.ws.pride.models.user.UserProfile;
import uk.ac.ebi.pride.ws.pride.service.user.UserProfileService;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.tsc.aap.client.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

//`@ApiIgnore
@RestController
@RequestMapping(path = "/user")
@Slf4j
public class UserProfileController {

    @Autowired
    private UserRegistrationValidator userRegistrationValidator;

    @Autowired
    private ChangePasswordValidator changePasswordValidator;

    @Autowired
    private UpdateProfileValidator updateProfileValidator;

    @InitBinder("userProfile")
    protected void initBinderUpdateProfile(WebDataBinder binder){ binder.setValidator(updateProfileValidator);}

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

    @ApiOperation(notes = "Register a new user", value = "registration", nickname = "registerNewUser", tags = {"User"} )
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
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }

    @ApiOperation(notes = "Change password for a user. User needs to be authenticated to access", value = "change-pwd", nickname = "changePwd", tags = {"User"} )
    @PreAuthorize("isAuthenticated()")
    @PostMapping(path="/change-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> changePassword(@RequestBody @Valid ChangePassword changePassword,
                                                 BindingResult errors,
                                                 Authentication authentication){
        if (errors.hasErrors()) {
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
                log.error(ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
            }
        }
    }

    @ApiOperation(notes = "View user's profile. User needs to be authenticated to access", value = "viewProfile", nickname = "viewProfile", tags = {"User"} )
    @PreAuthorize("isAuthenticated()")
    @GetMapping(path="/view-profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getProfile(Authentication authentication){

        User currentUser = (User) (authentication).getDetails();
        try{
            UserSummary userSummary = userProfileService.getProfile(currentUser.getEmail());
            return ResponseEntity.ok(userSummary);
        }catch(Exception e){
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error fetching user profile:"+e.getMessage());
        }
    }

    @ApiOperation(notes = "Update user's profile. User needs to be authenticated to access", value = "updateProfile", nickname = "updateProfile", tags = {"User"} )
    @PreAuthorize("isAuthenticated()")
    @PostMapping(path="/update-profile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateProfile(@RequestBody @Valid UserProfile userProfile,
                                                BindingResult errors,
                                                HttpServletRequest request,
                                                Authentication authentication){
        String token = request.getHeader("Authorization");
        if(token!=null && token.startsWith("Bearer ")){
            token = token.replaceFirst("Bearer ","");
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(WsContastants.AAP_TOKEN_MISMATCH_ERROR);
        }

        if (errors.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors.getAllErrors());
        } else {
            try {
                User currentUser = (User) (authentication).getDetails();
                if(!currentUser.getEmail().equalsIgnoreCase(userProfile.getEmail())){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email mismatch occurred");
                }
                userProfileService.updateProfile(token,currentUser.getUserReference(),currentUser.getEmail(),userProfile);
                return ResponseEntity.ok().build();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
            }
        }
    }


}
