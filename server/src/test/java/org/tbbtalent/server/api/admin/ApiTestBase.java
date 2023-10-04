/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tbbtalent.server.api.admin;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.tbbtalent.server.model.db.Role;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.security.JwtAuthenticationEntryPoint;
import org.tbbtalent.server.security.JwtTokenProvider;
import org.tbbtalent.server.security.TcUserDetails;
import org.tbbtalent.server.security.TcUserDetailsService;
import org.tbbtalent.server.service.db.email.EmailHelper;

/**
 * Common beans and configuration required by api unit tests.
 *
 * @author smalik
 */
public class ApiTestBase {
    private static final String USER_NAME = "test_user";
    private static final String FIRST_NAME = "test";
    private static final String LAST_NAME = "user";
    private static final String EMAIL = "test.user@tbb.org";
    private static final Role ROLE = Role.admin;

    protected User user;

    @MockBean
    TcUserDetailsService tcUserDetailsService;
    @MockBean JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @MockBean JwtTokenProvider jwtTokenProvider;
    @MockBean EmailHelper emailHelper;

    void configureAuthentication() {
        user = new User(USER_NAME, FIRST_NAME, LAST_NAME, EMAIL, ROLE);

        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromJwt(anyString())).thenReturn(USER_NAME);

        when(tcUserDetailsService.loadUserByUsername(anyString()))
                .thenReturn(new TcUserDetails(user));
    }


}
