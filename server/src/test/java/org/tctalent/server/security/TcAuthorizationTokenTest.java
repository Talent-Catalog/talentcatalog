/*
 * Copyright (c) 2025 Talent Catalog.
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class TcAuthorizationTokenTest {

  @Mock
  private UserDetails principal;

  @Mock
  private Object credentials;

  private TcAuthorizationToken token;

  private AutoCloseable mocks;

  @BeforeEach
  void setUp() {
    mocks = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void tearDown() throws Exception {
    mocks.close();
  }

  @Test
  void constructor_setsPrincipalAndCredentials() {
    token = new TcAuthorizationToken(principal, credentials);

    assertSame(principal, token.getPrincipal(), "Principal should match the constructor argument");
    assertSame(credentials, token.getCredentials(), "Credentials should match the constructor argument");
  }

  @Test
  void constructor_withNullPrincipalAndCredentials_setsCorrectly() {
    token = new TcAuthorizationToken(null, null);

    assertNull(token.getPrincipal(), "Principal should be null");
    assertNull(token.getCredentials(), "Credentials should be null");
  }

  @Test
  void constructor_setsEmptyAuthoritiesByDefault() {
    token = new TcAuthorizationToken(principal, credentials);

    Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
    assertNotNull(authorities, "Authorities should not be null");
    assertTrue(authorities.isEmpty(), "Authorities should be empty by default");
  }

  @Test
  void constructor_setsNotAuthenticatedByDefault() {
    token = new TcAuthorizationToken(principal, credentials);

    assertFalse(token.isAuthenticated(), "Token should not be authenticated by default");
  }

  @Test
  void getName_returnsPrincipalName_whenPrincipalIsUserDetails() {
    String username = "testuser";
    when(principal.getUsername()).thenReturn(username);

    token = new TcAuthorizationToken(principal, credentials);

    assertEquals(username, token.getName(), "getName should return the principal's username");
  }

  @Test
  void getName_returnsToString_whenPrincipalIsNotUserDetails() {
    Object nonUserDetailsPrincipal = new Object();
    token = new TcAuthorizationToken(nonUserDetailsPrincipal, credentials);

    assertEquals(nonUserDetailsPrincipal.toString(), token.getName(),
        "getName should return the principal's toString for non-UserDetails principal");
  }

  @Test
  void getName_returnsEmptyString_whenPrincipalIsNull() {
    token = new TcAuthorizationToken(null, credentials);

    assertEquals("", token.getName(), "getName should return empty string when principal is null");
  }
}