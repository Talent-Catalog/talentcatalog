/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.security;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;

class TcAuthenticationProviderTest {

  @Mock
  private UserDetailsService userDetailsService;

  @InjectMocks
  private TcAuthenticationProvider tcAuthenticationProvider;

  private TcAuthorizationToken tcAuthorizationToken;

  private AutoCloseable mocks;

  @BeforeEach
  void setUp() {
    mocks = MockitoAnnotations.openMocks(this);
    tcAuthorizationToken = new TcAuthorizationToken("testuser", "password");
  }

  @AfterEach
  void tearDown() throws Exception {
    mocks.close();
  }

  @Test
  void authenticate_throwsAuthenticationException_onFailure() {
    when(userDetailsService.loadUserByUsername("testuser")).thenThrow(new BadCredentialsException("Invalid credentials"));
    assertThrows(AuthenticationException.class, () -> tcAuthenticationProvider.authenticate(tcAuthorizationToken),
        "authenticate should throw AuthenticationException on failure");
    verify(userDetailsService).loadUserByUsername("testuser");
  }

  @Test
  void supports_returnsTrue_forTcAuthorizationToken() {
    boolean supports = tcAuthenticationProvider.supports(TcAuthorizationToken.class);
    assertTrue(supports, "supports should return true for TcAuthorizationToken");
  }
}
