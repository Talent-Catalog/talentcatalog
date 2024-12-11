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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;

/**
 * Talent Catalog's implementation of Spring's {@link UserDetails}, retrieving user data from database, setting
 * authorities based on the user's {@link Role} and exposing the corresponding underlying
 * {@link User} object through {@link #getUser()}.
 * <p/>
 * Note that this is what is returned by {@link Authentication#getPrincipal()}
 */
public class TcUserDetails implements UserDetails {

    private User user;
    private List<GrantedAuthority> authorities;

    public TcUserDetails(@NotNull User user) {
        this.user = user;
        this.authorities = new ArrayList<>();

        // If read only is checked assign the read only role
        if(user.getReadOnly()){
            this.authorities.add(new SimpleGrantedAuthority("ROLE_READONLY"));
        } else if (user.getRole().equals(Role.systemadmin)) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_SYSTEMADMIN"));
        } else if (user.getRole().equals(Role.admin)) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else if (user.getRole().equals(Role.partneradmin)) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_PARTNERADMIN"));
        } else if (user.getRole().equals(Role.semilimited)) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_SEMILIMITED"));
        } else if (user.getRole().equals(Role.limited)) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_LIMITED"));
        } else {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
    }

    public @NotNull User getUser() {
        return user;
    }

    public void setUser(@NotNull User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordEnc();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
