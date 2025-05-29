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
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TcUserDetailsTest {

  @Mock
  private User user;

  private TcUserDetails tcUserDetails;

  private AutoCloseable mocks;

  @BeforeEach
  void setUp() {
    mocks = MockitoAnnotations.openMocks(this);
    // Default stubbing for tests where role is irrelevant
    when(user.getReadOnly()).thenReturn(false);
    when(user.getRole()).thenReturn(Role.user);
  }

  @AfterEach
  void tearDown() throws Exception {
    mocks.close();
  }

  @Test
  void constructor_withReadOnly_setsReadOnlyAuthority() {
    when(user.getReadOnly()).thenReturn(true);
    when(user.getRole()).thenReturn(Role.admin); // Role should be ignored if read-only

    tcUserDetails = new TcUserDetails(user);

    Collection<? extends GrantedAuthority> authorities = tcUserDetails.getAuthorities();
    assertEquals(1, authorities.size(), "Should have exactly one authority");
    assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_READONLY")), "Authority should be ROLE_READONLY");
  }

  @Test
  void constructor_withSystemAdminRole_setsSystemAdminAuthority() {
    when(user.getReadOnly()).thenReturn(false);
    when(user.getRole()).thenReturn(Role.systemadmin);

    tcUserDetails = new TcUserDetails(user);

    Collection<? extends GrantedAuthority> authorities = tcUserDetails.getAuthorities();
    assertEquals(1, authorities.size(), "Should have exactly one authority");
    assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SYSTEMADMIN")), "Authority should be ROLE_SYSTEMADMIN");
  }

  @Test
  void constructor_withAdminRole_setsAdminAuthority() {
    when(user.getReadOnly()).thenReturn(false);
    when(user.getRole()).thenReturn(Role.admin);

    tcUserDetails = new TcUserDetails(user);

    Collection<? extends GrantedAuthority> authorities = tcUserDetails.getAuthorities();
    assertEquals(1, authorities.size(), "Should have exactly one authority");
    assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")), "Authority should be ROLE_ADMIN");
  }

  @Test
  void constructor_withPartnerAdminRole_setsPartnerAdminAuthority() {
    when(user.getReadOnly()).thenReturn(false);
    when(user.getRole()).thenReturn(Role.partneradmin);

    tcUserDetails = new TcUserDetails(user);

    Collection<? extends GrantedAuthority> authorities = tcUserDetails.getAuthorities();
    assertEquals(1, authorities.size(), "Should have exactly one authority");
    assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_PARTNERADMIN")), "Authority should be ROLE_PARTNERADMIN");
  }

  @Test
  void constructor_withSemiLimitedRole_setsSemiLimitedAuthority() {
    when(user.getReadOnly()).thenReturn(false);
    when(user.getRole()).thenReturn(Role.semilimited);

    tcUserDetails = new TcUserDetails(user);

    Collection<? extends GrantedAuthority> authorities = tcUserDetails.getAuthorities();
    assertEquals(1, authorities.size(), "Should have exactly one authority");
    assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SEMILIMITED")), "Authority should be ROLE_SEMILIMITED");
  }

  @Test
  void constructor_withLimitedRole_setsLimitedAuthority() {
    when(user.getReadOnly()).thenReturn(false);
    when(user.getRole()).thenReturn(Role.limited);

    tcUserDetails = new TcUserDetails(user);

    Collection<? extends GrantedAuthority> authorities = tcUserDetails.getAuthorities();
    assertEquals(1, authorities.size(), "Should have exactly one authority");
    assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_LIMITED")), "Authority should be ROLE_LIMITED");
  }

  @Test
  void constructor_withUserRole_setsUserAuthority() {
    when(user.getReadOnly()).thenReturn(false);
    when(user.getRole()).thenReturn(Role.user);

    tcUserDetails = new TcUserDetails(user);

    Collection<? extends GrantedAuthority> authorities = tcUserDetails.getAuthorities();
    assertEquals(1, authorities.size(), "Should have exactly one authority");
    assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")), "Authority should be ROLE_USER");
  }

  @Test
  void getUser_returnsUser() {
    tcUserDetails = new TcUserDetails(user);

    assertSame(user, tcUserDetails.getUser(), "getUser should return the same User instance");
  }

  @Test
  void setUser_updatesUser() {
    tcUserDetails = new TcUserDetails(user);
    User newUser = mock(User.class);
    when(newUser.getReadOnly()).thenReturn(false);
    when(newUser.getRole()).thenReturn(Role.user);

    tcUserDetails.setUser(newUser);

    assertSame(newUser, tcUserDetails.getUser(), "setUser should update the User instance");
  }

  @Test
  void getPassword_returnsUserPassword() {
    String password = "encryptedPassword";
    when(user.getPasswordEnc()).thenReturn(password);

    tcUserDetails = new TcUserDetails(user);

    assertEquals(password, tcUserDetails.getPassword(), "getPassword should return the user's encrypted password");
  }

  @Test
  void getUsername_returnsUserUsername() {
    String username = "testuser";
    when(user.getUsername()).thenReturn(username);

    tcUserDetails = new TcUserDetails(user);

    assertEquals(username, tcUserDetails.getUsername(), "getUsername should return the user's username");
  }

  @Test
  void isAccountNonExpired_returnsTrue() {
    tcUserDetails = new TcUserDetails(user);

    assertTrue(tcUserDetails.isAccountNonExpired(), "isAccountNonExpired should always return true");
  }

  @Test
  void isAccountNonLocked_returnsTrue() {
    tcUserDetails = new TcUserDetails(user);

    assertTrue(tcUserDetails.isAccountNonLocked(), "isAccountNonLocked should always return true");
  }

  @Test
  void isCredentialsNonExpired_returnsTrue() {
    tcUserDetails = new TcUserDetails(user);

    assertTrue(tcUserDetails.isCredentialsNonExpired(), "isCredentialsNonExpired should always return true");
  }

  @Test
  void isEnabled_returnsTrue() {
    tcUserDetails = new TcUserDetails(user);

    assertTrue(tcUserDetails.isEnabled(), "isEnabled should always return true");
  }

  @Test
  void constructor_withNullUser_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new TcUserDetails(null),
        "Constructor should throw NullPointerException for null User due to @NotNull");
  }
}
