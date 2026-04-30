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
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.tctalent.server.model.db.User;

/**
 * Lightweight representation of an authenticated user.
 * <p/>
 * Also has some convenience methods for checking authorities.
 */
@Getter
@Builder
public class CurrentUserInfo implements QueryAuthorities {
    private static final String ROLE_PREFIX = "ROLE_"; // Spring default

    /**
     * Id of corresponding User on the database.
     */
    private final Long id;

    /**
     * ID of the IDP (Identity Provider) where the user was authenticated.
     */
    private final String idpIssuer;

    /**
     * Subject (IDP unique identifier) of the user on the IDP (Identity Provider).
     */
    private final String idpSubject;

    /**
     * Human-readable identifier for the user
     * (currently the email address - see {@link OAuth2UserService}).
     * This name value is used to populate the name of TcAuthenticationToken for which
     * CurrentUserInfo is the Principal.
     */
    private final String name;

    /**
     * The language the user has selected.
     * Set in {@link LanguageFilter}
     */
    @Setter
    private String selectedLanguage;

    /**
     * Collection of authorities granted to the user.
     */
    private Collection<? extends GrantedAuthority> authorities;

    //TODO JC Temporarily expose User object.
    @Setter
    private User user;

    //Set of Strings computed from authorities to efficiently support hasAuthority
    //and hasRole methods.
    private Set<String> authorityStrings;

    /**
     * Hook into setting authorities so that the authorityStrings can be computed.
     */
    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;

        //Construct Set of Strings to efficiently support hasAuthority(String) and hasRole(String)
        authorityStrings = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toUnmodifiableSet());
    }


    public boolean hasAuthority(String authority) {
        return authority != null && authorityStrings.contains(authority);
    }

    public boolean hasRole(String role) {
        return role != null && authorityStrings.contains(ROLE_PREFIX + role);
    }

    public boolean hasAnyAuthority(String... authorities) {
        if (authorities == null) return false;
        for (String a : authorities) {
            if (a != null && authorityStrings.contains(a)) return true;
        }
        return false;
    }

    public boolean hasAnyRole(String... roles) {
        if (roles == null) return false;
        for (String r : roles) {
            if (r != null && authorityStrings.contains(ROLE_PREFIX + r)) return true;
        }
        return false;
    }

}
