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

package org.tctalent.server.api.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.security.AuthenticationErrorEntryPoint;
import org.tctalent.server.security.CurrentUserInfo;
import org.tctalent.server.security.OAuth2UserService;
import org.tctalent.server.service.db.email.EmailHelper;

/**
 * Common beans and configuration required by api unit tests.
 *
 * @author smalik
 */
@WithMockUser()
public class ApiTestBase {
    private static final String USER_NAME = "test_user";
    private static final String FIRST_NAME = "test";
    private static final String LAST_NAME = "user";
    private static final String EMAIL = "test.user@tbb.org";
    private static final Role ROLE = Role.admin;

    protected User user;

    @MockitoBean
    UserRepository userRepository;
    @MockitoBean
    AuthenticationErrorEntryPoint AuthenticationErrorEntryPoint;
    @MockitoBean
    EmailHelper emailHelper;
    @MockitoBean
    OAuth2UserService oAuth2UserService;

    public void configureAuthentication() {
        user = new User(USER_NAME, FIRST_NAME, LAST_NAME, EMAIL, ROLE);
        user.setId(1L);

        CurrentUserInfo currentUserInfo = CurrentUserInfo.builder()
            .id(user.getId())
            .name(user.getEmail())
            .idpIssuer("Keycloak")
            .idpSubject("1234567890")
            .build();

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        currentUserInfo.setAuthorities(authorities);

        currentUserInfo.setUser(user);

        when(oAuth2UserService.loadUser(any(), any())).thenReturn(currentUserInfo);

        when(userRepository.findByUsernameIgnoreCase(anyString())).thenReturn(user);
    }


}
