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

import org.springframework.boot.test.mock.mockito.MockBean;
import org.tbbtalent.server.security.JwtAuthenticationEntryPoint;
import org.tbbtalent.server.security.JwtTokenProvider;
import org.tbbtalent.server.security.TbbUserDetailsService;
import org.tbbtalent.server.service.db.email.EmailHelper;

/**
 * Common beans and configuration required by api unit tests.
 *
 * @author smalik
 */
public class ApiTestBase {
    @MockBean TbbUserDetailsService tbbUserDetailsService;
    @MockBean JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @MockBean JwtTokenProvider jwtTokenProvider;
    @MockBean EmailHelper emailHelper;
}
