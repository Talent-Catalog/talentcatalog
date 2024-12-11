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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.security.JwtAuthenticationEntryPoint;
import org.tctalent.server.security.JwtTokenProvider;
import org.tctalent.server.security.TcUserDetails;
import org.tctalent.server.security.TcUserDetailsService;
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

    @MockBean
    TcUserDetailsService tcUserDetailsService;
    @MockBean
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @MockBean
    JwtTokenProvider jwtTokenProvider;
    @MockBean
    EmailHelper emailHelper;

    public void configureAuthentication() {
        user = new User(USER_NAME, FIRST_NAME, LAST_NAME, EMAIL, ROLE);

        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromJwt(anyString())).thenReturn(USER_NAME);

        when(tcUserDetailsService.loadUserByUsername(anyString()))
                .thenReturn(new TcUserDetails(user));
    }


}
