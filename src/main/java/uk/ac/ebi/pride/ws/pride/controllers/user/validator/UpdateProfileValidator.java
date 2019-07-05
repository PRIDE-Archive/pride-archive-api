package uk.ac.ebi.pride.ws.pride.controllers.user.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import uk.ac.ebi.pride.archive.repo.services.user.UserService;
import uk.ac.ebi.pride.archive.repo.services.user.validation.UserSummaryValidator;
import uk.ac.ebi.pride.ws.pride.models.user.UserProfile;

@Component
public class UpdateProfileValidator extends UserSummaryValidator {

    @Autowired
    public UpdateProfileValidator(UserService userServiceImpl) {
        super(userServiceImpl);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return UserProfile.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateEmailExists(target,errors);
        validateContactDetails(target, errors);
        validateAcceptedTerms(target,errors);
    }
}
