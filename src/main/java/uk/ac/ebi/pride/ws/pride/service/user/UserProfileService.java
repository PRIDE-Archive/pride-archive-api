package uk.ac.ebi.pride.ws.pride.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.ac.ebi.pride.archive.repo.repos.user.PasswordUtilities;
import uk.ac.ebi.pride.archive.repo.repos.user.User;
import uk.ac.ebi.pride.archive.repo.repos.user.UserRepository;
import uk.ac.ebi.pride.archive.repo.services.user.UserServiceWebServiceImpl;
import uk.ac.ebi.pride.archive.repo.services.user.UserSummary;
import uk.ac.ebi.pride.archive.repo.services.user.url.UserWebServiceUrl;
import uk.ac.ebi.pride.archive.repo.util.AAPConstants;
import uk.ac.ebi.pride.archive.repo.util.ObjectMapper;
import uk.ac.ebi.pride.ws.pride.models.uer.ChangePassword;
import uk.ac.ebi.pride.ws.pride.utils.PrideSupportEmailSender;

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
}
