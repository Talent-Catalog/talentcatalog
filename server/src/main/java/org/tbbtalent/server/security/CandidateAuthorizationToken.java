package org.tbbtalent.server.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class CandidateAuthorizationToken extends UsernamePasswordAuthenticationToken {

    public CandidateAuthorizationToken(Object principal,
                                       Object credentials) {
        super(principal, credentials);
    }

}
