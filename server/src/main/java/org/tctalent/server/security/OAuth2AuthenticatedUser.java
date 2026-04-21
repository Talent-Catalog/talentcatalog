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

/**
 * Lightweight representation of an authenticated user.
 * <p/>
 * Also has some convenience methods for checking authorities.
 */
@Getter
@Builder
public class OAuth2AuthenticatedUser implements QueryAuthorities {
    private static final String ROLE_PREFIX = "ROLE_"; // Spring default

    private final Long id;
    private final String username;
    private final String email;
    private final String idpIssuer;
    private final String idpSubject;

    /**
     * The language the user has selected.
     * Set in {@link LanguageFilter}
     */
    @Setter
    private String selectedLanguage;

    private Collection<? extends GrantedAuthority> authorities;

    private Set<String> authorityStrings;

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
