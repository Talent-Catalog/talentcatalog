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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.tctalent.server.model.db.User;

class TcAuthenticationProviderTest {

  private static final TcPasswordEncoder ENCODER = new TcPasswordEncoder();

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

  @Test
  void afterPropertiesSet_succeeds_whenUserDetailsServiceSuppliedToConstructor() {
    assertDoesNotThrow(() -> configuredProvider().afterPropertiesSet());
  }

  @Test
  void authenticate_returnsAuthenticatedToken_forCorrectPassword() {
    TcUserDetails userDetails = userDetails("testuser", ENCODER.encode("password"));
    when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

    Authentication result = configuredProvider().authenticate(tcAuthorizationToken);

    UsernamePasswordAuthenticationToken token =
        assertInstanceOf(UsernamePasswordAuthenticationToken.class, result);
    assertTrue(token.isAuthenticated());
    assertSame(userDetails, token.getPrincipal());
    verify(userDetailsService).loadUserByUsername("testuser");
  }

  @Test
  void authenticate_throwsBadCredentials_forWrongPassword() {
    when(userDetailsService.loadUserByUsername("testuser"))
        .thenReturn(userDetails("testuser", ENCODER.encode("other-password")));

    assertThrows(BadCredentialsException.class,
        () -> configuredProvider().authenticate(tcAuthorizationToken));
  }

  @Test
  void authenticate_throwsBadCredentials_whenUserHasNoPassword() {
    //TcPasswordEncoder never matches a null encoded password
    when(userDetailsService.loadUserByUsername("testuser"))
        .thenReturn(userDetails("testuser", null));

    assertThrows(BadCredentialsException.class,
        () -> configuredProvider().authenticate(tcAuthorizationToken));
  }

  @Test
  void authenticate_throwsBadCredentials_notUsernameNotFound_forUnknownUser() {
    //Unknown users are reported as bad credentials so as not to reveal which usernames exist
    when(userDetailsService.loadUserByUsername("testuser"))
        .thenThrow(new UsernameNotFoundException("testuser"));

    assertThrows(BadCredentialsException.class,
        () -> configuredProvider().authenticate(tcAuthorizationToken));
  }

  @Test
  void supports_returnsFalse_forUnrelatedAuthenticationType() {
    assertFalse(tcAuthenticationProvider.supports(AnonymousAuthenticationToken.class));
  }

  /**
   * Provider configured in the same way as in SecurityConfiguration.
   */
  private TcAuthenticationProvider configuredProvider() {
    TcAuthenticationProvider provider = new TcAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(ENCODER);
    return provider;
  }

  private static TcUserDetails userDetails(String username, String passwordEnc) {
    User user = new User();
    user.setUsername(username);
    user.setPasswordEnc(passwordEnc);
    TcUserDetails userDetails = new TcUserDetails(user);
    userDetails.setAuthorities(AuthorityUtils.NO_AUTHORITIES);
    return userDetails;
  }
}
