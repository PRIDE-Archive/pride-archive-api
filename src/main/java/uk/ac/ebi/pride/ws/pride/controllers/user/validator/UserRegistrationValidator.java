package uk.ac.ebi.pride.ws.pride.controllers.user.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import uk.ac.ebi.pride.archive.repo.services.user.UserService;
import uk.ac.ebi.pride.archive.repo.services.user.validation.UserSummaryValidator;

@Component
public class UserRegistrationValidator extends UserSummaryValidator {
    @Autowired
    public UserRegistrationValidator(UserService userServiceImpl) {
        super(userServiceImpl);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateContactDetails(target, errors);
        validateEmail(target, errors);
        validateAcceptedTerms(target,errors);
    }

}