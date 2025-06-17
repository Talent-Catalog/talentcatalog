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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

  @InjectMocks
  private AuthService authService;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @Mock
  private TcUserDetails tcUserDetails;

  @Mock
  private User user;

  @Mock
  private Candidate candidate;

  @Mock
  private Country country;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
  }

  @Test
  void getLoggedInUser_returnsUser_whenAuthenticated() {
    when(authentication.getPrincipal()).thenReturn(tcUserDetails);
    when(tcUserDetails.getUser()).thenReturn(user);

    Optional<User> result = authService.getLoggedInUser();

    assertTrue(result.isPresent());
    assertEquals(user, result.get());
  }

  @Test
  void getLoggedInUser_returnsEmpty_whenNotAuthenticated() {
    when(authentication.getPrincipal()).thenReturn(null);

    Optional<User> result = authService.getLoggedInUser();

    assertFalse(result.isPresent());
  }

  @Test
  void getLoggedInUser_returnsEmpty_whenAuthenticationIsNull() {
    when(securityContext.getAuthentication()).thenReturn(null);

    Optional<User> result = authService.getLoggedInUser();

    assertFalse(result.isPresent());
  }

  @Test
  void getLoggedInCandidateId_returnsId_whenUserHasCandidate() {
    Long candidateId = 123L;
    when(authentication.getPrincipal()).thenReturn(tcUserDetails);
    when(tcUserDetails.getUser()).thenReturn(user);
    when(user.getCandidate()).thenReturn(candidate);
    when(candidate.getId()).thenReturn(candidateId);

    Long result = authService.getLoggedInCandidateId();

    assertEquals(candidateId, result);
  }

  @Test
  void getLoggedInCandidateId_returnsNull_whenNoUser() {
    when(authentication.getPrincipal()).thenReturn(null);

    Long result = authService.getLoggedInCandidateId();

    assertNull(result);
  }

  @Test
  void getLoggedInCandidateId_returnsNull_whenUserHasNoCandidate() {
    when(authentication.getPrincipal()).thenReturn(tcUserDetails);
    when(tcUserDetails.getUser()).thenReturn(user);
    when(user.getCandidate()).thenReturn(null);

    Long result = authService.getLoggedInCandidateId();

    assertNull(result);
  }

  @Test
  void getLoggedInCandidate_returnsCandidate_whenUserHasCandidate() {
    when(authentication.getPrincipal()).thenReturn(tcUserDetails);
    when(tcUserDetails.getUser()).thenReturn(user);
    when(user.getCandidate()).thenReturn(candidate);

    Candidate result = authService.getLoggedInCandidate();

    assertEquals(candidate, result);
  }

  @Test
  void getLoggedInCandidate_returnsNull_whenNoUser() {
    when(authentication.getPrincipal()).thenReturn(null);

    Candidate result = authService.getLoggedInCandidate();

    assertNull(result);
  }

  @Test
  void getLoggedInCandidate_returnsNull_whenUserHasNoCandidate() {
    when(authentication.getPrincipal()).thenReturn(tcUserDetails);
    when(tcUserDetails.getUser()).thenReturn(user);
    when(user.getCandidate()).thenReturn(null);

    Candidate result = authService.getLoggedInCandidate();

    assertNull(result);
  }

  @Test
  void getUserLanguage_returnsLanguage_whenUserLoggedIn() {
    String language = "en";
    when(authentication.getPrincipal()).thenReturn(tcUserDetails);
    when(tcUserDetails.getUser()).thenReturn(user);
    when(user.getSelectedLanguage()).thenReturn(language);

    String result = authService.getUserLanguage();

    assertEquals(language, result);
  }

  @Test
  void getUserLanguage_returnsNull_whenNoUser() {
    when(authentication.getPrincipal()).thenReturn(null);

    String result = authService.getUserLanguage();

    assertNull(result);
  }

  @Test
  void authoriseLoggedInUser_returnsFalse_whenNoUser() {
    when(authentication.getPrincipal()).thenReturn(null);

    boolean result = authService.authoriseLoggedInUser(candidate);

    assertFalse(result);
  }

  @Test
  void authoriseLoggedInUser_returnsFalse_whenUserIsReadOnly() {
    when(authentication.getPrincipal()).thenReturn(tcUserDetails);
    when(tcUserDetails.getUser()).thenReturn(user);
    when(user.getReadOnly()).thenReturn(true);

    boolean result = authService.authoriseLoggedInUser(candidate);

    assertFalse(result);
  }

  @ParameterizedTest
  @EnumSource(value = Role.class, names = {"admin", "systemadmin"})
  void authoriseLoggedInUser_returnsTrue_forAdminRoles(Role role) {
    when(authentication.getPrincipal()).thenReturn(tcUserDetails);
    when(tcUserDetails.getUser()).thenReturn(user);
    when(user.getReadOnly()).thenReturn(false);
    when(user.getRole()).thenReturn(role);

    boolean result = authService.authoriseLoggedInUser(candidate);

    assertTrue(result);
  }

  @Test
  void authoriseLoggedInUser_returnsTrue_forPartnerAdminNoCountryRestrictions() {
    when(authentication.getPrincipal()).thenReturn(tcUserDetails);
    when(tcUserDetails.getUser()).thenReturn(user);
    when(user.getReadOnly()).thenReturn(false);
    when(user.getRole()).thenReturn(Role.partneradmin);
    when(user.getSourceCountries()).thenReturn(Collections.emptySet());

    boolean result = authService.authoriseLoggedInUser(candidate);

    assertTrue(result);
  }

  @Test
  void authoriseLoggedInUser_returnsTrue_forPartnerAdminWithMatchingCountry() {
    when(authentication.getPrincipal()).thenReturn(tcUserDetails);
    when(tcUserDetails.getUser()).thenReturn(user);
    when(user.getReadOnly()).thenReturn(false);
    when(user.getRole()).thenReturn(Role.partneradmin);
    when(user.getSourceCountries()).thenReturn(Set.of(country));
    when(candidate.getCountry()).thenReturn(country);

    boolean result = authService.authoriseLoggedInUser(candidate);

    assertTrue(result);
  }

  @Test
  void authoriseLoggedInUser_returnsFalse_forPartnerAdminWithNonMatchingCountry() {
    Country otherCountry = mock(Country.class);
    when(authentication.getPrincipal()).thenReturn(tcUserDetails);
    when(tcUserDetails.getUser()).thenReturn(user);
    when(user.getReadOnly()).thenReturn(false);
    when(user.getRole()).thenReturn(Role.partneradmin);
    when(user.getSourceCountries()).thenReturn(Set.of(country));
    when(candidate.getCountry()).thenReturn(otherCountry);

    boolean result = authService.authoriseLoggedInUser(candidate);

    assertFalse(result);
  }

  @Test
  void authoriseLoggedInUser_returnsTrue_forUserOwningCandidate() {
    Long candidateId = 123L;
    when(authentication.getPrincipal()).thenReturn(tcUserDetails);
    when(tcUserDetails.getUser()).thenReturn(user);
    when(user.getReadOnly()).thenReturn(false);
    when(user.getRole()).thenReturn(Role.user);
    when(user.getCandidate()).thenReturn(candidate);
    when(candidate.getId()).thenReturn(candidateId);
    when(candidate.getId()).thenReturn(candidateId);

    boolean result = authService.authoriseLoggedInUser(candidate);

    assertTrue(result);
  }

  @Test
  void authoriseLoggedInUser_returnsFalse_forUserNotOwningCandidate() {
    Candidate otherCandidate = mock(Candidate.class);
    when(authentication.getPrincipal()).thenReturn(tcUserDetails);
    when(tcUserDetails.getUser()).thenReturn(user);
    when(user.getReadOnly()).thenReturn(false);
    when(user.getRole()).thenReturn(Role.user);
    when(user.getCandidate()).thenReturn(otherCandidate);
    when(otherCandidate.getId()).thenReturn(456L);
    when(candidate.getId()).thenReturn(123L);

    boolean result = authService.authoriseLoggedInUser(candidate);

    assertFalse(result);
  }

  @ParameterizedTest
  @EnumSource(value = Role.class, names = {"partneradmin", "admin", "systemadmin"})
  void hasAdminPrivileges_returnsTrue_forAdminRoles(Role role) {
    boolean result = authService.hasAdminPrivileges(role);

    assertTrue(result);
  }
}