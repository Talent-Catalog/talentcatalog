/*
 * Copyright (c) 2024 Talent Catalog.
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.tctalent.server.model.db.User;

/**
 * Talent Catalog's implementation of Spring's {@link UserDetails}, exposing the corresponding
 * underlying {@link User} object through {@link #getUser()}.
 * <p/>
 * Note that this is what is returned by {@link Authentication#getPrincipal()}
 */
public class TcUserDetails implements UserDetails {
    private static final String ROLE_PREFIX = "ROLE_"; // Spring default

    private User user;
    private List<GrantedAuthority> authorities;
    private Set<String> authorityStrings;

    public TcUserDetails(@NonNull User user) {
        this.user = user;
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

    public @NonNull User getUser() {
        return user;
    }

    public void setUser(@NonNull User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<GrantedAuthority> authorities) {
        this.authorities = authorities;

        //Construct Set of Strings to efficiently support hasAuthority(String) and hasRole(String)
        this.authorityStrings = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getPassword() {
        return user.getPasswordEnc();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }
}
