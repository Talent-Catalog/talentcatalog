package org.tbbtalent.server.security;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;

public class CandidateAuthenticationProvider extends DaoAuthenticationProvider {

    public CandidateAuthenticationProvider(UserDetailsService userDetailsService) {
        this.setUserDetailsService(userDetailsService);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return super.authenticate(authentication);
    }

    @Override
    public boolean supports( Class<?> authentication ) {
        if (authentication.isAssignableFrom(CandidateAuthorizationToken.class)) {
            return true;
        }
        return false;
    }

}
