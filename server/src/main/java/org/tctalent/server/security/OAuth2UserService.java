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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.UserRepository;

/**
 * Loads the user from the database based on the idpIssuer and idpSubject received in the JWT token
 * provided by the idp. Then uses that data to create an {@link OAuth2AuthenticatedUser},
 * complete with the user's authorities.
 */
@Service
public class OAuth2UserService {

    private final UserRepository userRepository;

    public OAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public OAuth2AuthenticatedUser loadUser(String idpIssuer, String idpSubject) {
        User user = userRepository.findByIdpIssuerAndIdpSubject(idpIssuer, idpSubject)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found for issuer=" + idpIssuer + ", sub=" + idpSubject
            ));

        OAuth2AuthenticatedUser authUser = OAuth2AuthenticatedUser.builder()
            .id(user.getId())
            .email(user.getEmail())
            .idpIssuer(idpIssuer)
            .idpSubject(idpSubject)
            .build();

        authUser.setAuthorities(mapAuthorities(user));
        return authUser;
    }

    /**
     * In the future, we may store user permissions in a separate db table and retrieve them here.
     * @param user User to create authorities for
     * @return Collection of authorities
     */
    private Collection<SimpleGrantedAuthority> mapAuthorities(User user) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

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
