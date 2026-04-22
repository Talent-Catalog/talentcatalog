/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.security;

import java.util.Collection;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Authentication token representing a Talent Catalog user authenticated via an external IdP
 * and resolved to a local application user.
 *
 * <p>The principal is the local authenticated user abstraction, while the credentials are the
 * validated JWT that proved the caller's identity.</p>
 */
@Getter
public class TcAuthenticationToken extends AbstractAuthenticationToken {

    private final CurrentUserInfo principal;
    private final Jwt jwt;

    public TcAuthenticationToken(
        CurrentUserInfo principal,
        Jwt jwt,
        Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.principal = principal;
        this.jwt = jwt;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return jwt;
    }

    /**
     * Pick up the name from the {@link CurrentUserInfo} principal.
     */
    @Override
    public String getName() {
        if (principal.getName() != null && !principal.getName().isBlank()) {
            return principal.getName();
        }
        return principal.getIdpSubject();
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        if (authenticated) {
            throw new IllegalArgumentException(
                "Cannot mark this token authenticated via setAuthenticated(true)"
            );
        }
        super.setAuthenticated(false);
    }
}
