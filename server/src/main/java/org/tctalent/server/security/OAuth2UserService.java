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
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.UserRepository;

/**
 * Loads the user from the database based on the idpIssuer and idpSubject received in the JWT token
 * provided by the idp. Then uses that data to create an {@link CurrentUserInfo},
 * complete with the user's authorities.
 */
@Service
public class OAuth2UserService {

    public static final String OAUTH_TC_ADMIN_CLIENT_ID = "admin";
    public static final String OAUTH_TC_CANDIDATE_CLIENT_ID = "candidate";
    private final UserRepository userRepository;

    public OAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Checks that the clientId from the JWT token matches the user's role. If not, throws an exception
     * which will cause the authentication to fail.
     * <p>
     * We use one clientId, "admin", for admin-portal users, and "candidate" for candidate-portal users.
     */
    public void checkUserClientId
        (@NonNull User user, @Nullable String clientId) throws UsernameNotFoundException {
        final String idpIssuer = user.getIdpIssuer();
        final String idpSubject = user.getIdpSubject();
        
        if (!StringUtils.hasText(clientId)) {
            throw new UsernameNotFoundException("No client id provided");
        }

        boolean shouldBeCandidate = switch (clientId) {
            case OAUTH_TC_ADMIN_CLIENT_ID, OAUTH_TC_CANDIDATE_CLIENT_ID ->
                clientId.equals(OAUTH_TC_CANDIDATE_CLIENT_ID);
            default -> throw new UsernameNotFoundException("Unrecognized client id: " + clientId);
        };


        //We have matched the user by issuer and subject - but the clientId also has to match the
        //user role.
        //We use one clientId, "admin", for admin-portal users, and "candidate" for
        //candidate-portal users.
        if (shouldBeCandidate && !user.getRole().equals(Role.user)) {
            throw new UsernameNotFoundException(
                "Candidate user mismatch for issuer=" + idpIssuer + ", sub=" + idpSubject);
        }
        if (!shouldBeCandidate && user.getRole().equals(Role.user)) {
            throw new UsernameNotFoundException(
                "Admin user mismatch for issuer=" + idpIssuer + ", sub=" + idpSubject);
        }
    }

    public CurrentUserInfo constructUserInfo(User user, String idpIssuer, String idpSubject) {
        CurrentUserInfo currentUserInfo = CurrentUserInfo.builder()
            .id(user.getId())

            //Use email as a human-readable identifier.
            //This is used to populate the name of TcAuthenticationToken for which CurrentUserInfo
            //is the Principal.
            .name(user.getEmail())

            .idpIssuer(idpIssuer)
            .idpSubject(idpSubject)
            .build();

        currentUserInfo.setAuthorities(mapAuthorities(user));

        //TODO JC Temporarily expose the User
        currentUserInfo.setUser(user);
        return currentUserInfo;
    }

    public CurrentUserInfo loadUser(String idpIssuer, String idpSubject, String clientId) {
        User user = userRepository.findByIdpIssuerAndIdpSubject(idpIssuer, idpSubject)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found for issuer=" + idpIssuer + ", sub=" + idpSubject
            ));

        checkUserClientId(user, clientId);

        CurrentUserInfo currentUserInfo = constructUserInfo(user, idpIssuer, idpSubject);
        return currentUserInfo;
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
