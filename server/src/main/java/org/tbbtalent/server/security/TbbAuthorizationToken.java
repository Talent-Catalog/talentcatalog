package org.tbbtalent.server.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class TbbAuthorizationToken extends UsernamePasswordAuthenticationToken {

    public TbbAuthorizationToken(Object principal,
                                 Object credentials) {
        super(principal, credentials);
    }

}
