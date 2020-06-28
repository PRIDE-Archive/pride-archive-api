package uk.ac.ebi.pride.ws.pride.security.authorization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import java.util.Collection;

@Slf4j
public class CustomAuthorizationVoter implements AccessDecisionVoter {
    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    @Override
    public int vote(Authentication authentication, Object object, Collection collection) {
        return ACCESS_GRANTED;
    }

    @Override
    public boolean supports(Class clazz) {
        return true;
    }
}
