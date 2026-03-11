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
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.UserRepository;

/**
 * Implementation of Spring's {@link UserDetailsService}, returning {@link TcUserDetails}
 * objects.
 */
@Slf4j
@Component
public class TcUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public TcUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public TcUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsernameIgnoreCase(username);
        if (user == null) {
            throw new UsernameNotFoundException("No user found for: " + username);
        }

        LogBuilder.builder(log)
            .action("loadUserByUsername")
            .message("Found user with ID " + user.getId() + " for username '" + username + "'")
            .logDebug();

        return createTcUserDetails(user);
    }

    /**
     * Creates a TcUserDetails object from a User object.
     * <p>
     *     This is the TC's Security Principal. It contains the user's role and authorities.
     * </p>
     * @param user Associated user.
     */
    private TcUserDetails createTcUserDetails(@NonNull User user) {
        TcUserDetails tcUserDetails = new TcUserDetails(user);
        tcUserDetails.setAuthorities(createAuthorities(user));
        return tcUserDetails;
    }

    /**
     * In the future, we may store user permissions in a separate db table and retrieve them here.
     * @param user User to create authorities for
     * @return List of authorities
     */
    private List<GrantedAuthority> createAuthorities(@NonNull User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // If read-only is checked, assign the read-only role
        if(user.getReadOnly()){
            authorities.add(new SimpleGrantedAuthority("ROLE_READONLY"));
        } else if (user.getRole().equals(Role.systemadmin)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_SYSTEMADMIN"));
        } else if (user.getRole().equals(Role.admin)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else if (user.getRole().equals(Role.partneradmin)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_PARTNERADMIN"));
        } else if (user.getRole().equals(Role.semilimited)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_SEMILIMITED"));
        } else if (user.getRole().equals(Role.limited)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_LIMITED"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        //For now we hard code some particular test users as our chat test users
        if("TestChattingCandidate".equals(user.getUsername())){
            authorities.add(new SimpleGrantedAuthority("CHAT_SUBSCRIBE"));
        }
        if("TestChattingJobCreator".equals(user.getUsername())){
            authorities.add(new SimpleGrantedAuthority("CHAT_SUBSCRIBE"));
        }
        if("TestChattingSourcePartner".equals(user.getUsername())){
            authorities.add(new SimpleGrantedAuthority("CHAT_SUBSCRIBE"));
        }

        return authorities;
    }


}
