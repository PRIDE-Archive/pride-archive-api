package uk.ac.ebi.pride.ws.pride.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.ac.ebi.pride.archive.repo.repos.user.PasswordUtilities;
import uk.ac.ebi.pride.archive.repo.repos.user.User;
import uk.ac.ebi.pride.archive.repo.repos.user.UserRepository;
import uk.ac.ebi.pride.archive.repo.services.user.UserAccessException;
import uk.ac.ebi.pride.archive.repo.services.user.UserServiceWebServiceImpl;
import uk.ac.ebi.pride.archive.repo.services.user.UserSummary;
import uk.ac.ebi.pride.archive.repo.services.user.url.UserWebServiceUrl;
import uk.ac.ebi.pride.archive.repo.util.AAPConstants;
import uk.ac.ebi.pride.archive.repo.util.ObjectMapper;
import uk.ac.ebi.pride.ws.pride.models.user.ChangePassword;
import uk.ac.ebi.pride.ws.pride.models.user.UserProfile;
import uk.ac.ebi.pride.ws.pride.utils.PrideSupportEmailSender;

import java.util.Calendar;

@Service
@Slf4j
public class UserProfileService {

    @Autowired
    UserServiceWebServiceImpl userServiceWebServiceImpl;

    @Autowired
    private UserWebServiceUrl userWebServiceUrl;

    @Value("${aap.auth.url}")
    private String aapRegisterURL;

    @Autowired
    private AAPService aapService;

    @Autowired
    private PrideSupportEmailSender prideSupportEmailSender;

    @Autowired
    private String registrationEmailTemplate;

    @Autowired
    private String passwordChangeEmailTemplate;

    @Autowired
    private UserRepository userRepository;

    public String registerNewUser(UserSummary userSummary) {
        log.info("Entered registerNewUser");
        String password = PasswordUtilities.generatePassword();
        userSummary.setPassword(password);

        //Sign up user in both AAP and PRIDE
        log.info("Begin user signup");
        userWebServiceUrl.setAapRegisterUrl(aapRegisterURL);
        User user = userServiceWebServiceImpl.signUp(userSummary);


        //Add user to submitter domain in AAP
        log.info("Begin user domain registeration");
        if(user.getUserRef()!=null) {
            boolean isDomainRegSuccessful = aapService.addUserToAAPDomain(user.getUserRef(), AAPConstants.PRIDE_SUBMITTER_DOMAIN);
            if(!isDomainRegSuccessful){
                log.error("Error adding user to submitter domain in AAP:"+user.getEmail());
            }
        }else{
            log.error("Error creating user and getting user ref for email:"+user.getEmail());
        }

        // send registration success email
        log.info("Begin user email trigger");
        prideSupportEmailSender.sendRegistrationEmail(ObjectMapper.mapUserSummaryToUser(userSummary), password, registrationEmailTemplate);

        log.info("Exiting registerNewUser");
        return user.getUserRef();
    }

    public void changePassword(String userReference, ChangePassword changePassword) throws Exception {
        User user = userRepository.findByUserRef(userReference);
        user.setPassword(changePassword.getNewPassword());
        //update in aap
        boolean isChangeSuccessful = aapService.changeAAPPassword(userReference,changePassword);
        if(isChangeSuccessful) {
            //update in pride
            user = userRepository.save(user);
            UserSummary userSummary = new UserSummary();
            userSummary.setEmail(user.getEmail());
            userSummary.setFirstName(user.getFirstName());
            userSummary.setLastName(user.getLastName());
            userSummary.setPassword(user.getPassword());
            prideSupportEmailSender.sendPasswordChangeEmail(userSummary, passwordChangeEmailTemplate);
        }else{
            throw new Exception("Failed to update pwd in AAP");
        }
    }

    public boolean updateProfile(String token, String userReference, String currentUserEmail, UserProfile updateUser) {
        User currentUser = userRepository.findByEmail(currentUserEmail);
        UserSummary oldUserSumary = ObjectMapper.mapUserToUserSummary(currentUser);
        //check if fields have been modified
        boolean isModified=false;
        boolean isAAPUpdateRequired=false;

        if(!oldUserSumary.getTitle().getTitle().equalsIgnoreCase(updateUser.getTitle().getTitle())){
            oldUserSumary.setTitle(updateUser.getTitle());
            isModified=true;
        }

        if(!oldUserSumary.getFirstName().equals(updateUser.getFirstName())){
            oldUserSumary.setFirstName(updateUser.getFirstName());
            isAAPUpdateRequired=true;
            isModified=true;
        }

        if(!oldUserSumary.getLastName().equals(updateUser.getLastName())){
            oldUserSumary.setLastName(updateUser.getLastName());
            isAAPUpdateRequired=true;
            isModified=true;
        }

        if(!oldUserSumary.getAffiliation().equals(updateUser.getAffiliation())){
            oldUserSumary.setAffiliation(updateUser.getAffiliation());
            isAAPUpdateRequired=true;
            isModified=true;
        }

        if(!oldUserSumary.getCountry().equals(updateUser.getCountry())){
            oldUserSumary.setCountry(updateUser.getCountry());
            isModified=true;
        }

        if(updateUser.getOrcid()!= null && !updateUser.getOrcid().equals(oldUserSumary.getOrcid())){
            oldUserSumary.setOrcid(updateUser.getOrcid());
            isModified=true;
        }

        if(isModified){
            oldUserSumary.setUpdateAt(Calendar.getInstance().getTime());
            if(isAAPUpdateRequired){
                //update in AAP
                boolean isUpdateSuccessful = aapService.updateUserData(token, userReference,oldUserSumary);
                if(!isUpdateSuccessful){
                    String msg = "Failed to update user detail in AAP: " + oldUserSumary.getEmail();
                    log.error(msg);
                    throw new UserAccessException(msg,oldUserSumary.getEmail());
                }
            }
            //update pride
            try {
                User user = ObjectMapper.mapUserSummaryToUser(oldUserSumary);
                userRepository.save(user);
            } catch (Exception ex) {
                String msg = "Failed to update user detail, user email: " + oldUserSumary.getEmail();
                log.error(ex.getMessage()+";"+msg);
                throw new UserAccessException(msg, ex, oldUserSumary.getEmail());
            }
        }

        return isModified;


    }

    public UserSummary getProfile(String email) {
        User currentUser = userRepository.findByEmail(email);
        if(currentUser != null) {
            UserSummary userSumary = ObjectMapper.mapUserToUserSummary(currentUser);
            return userSumary;
        }else{
            throw new NullPointerException("Email id doesn't exist");
        }
    }
}
