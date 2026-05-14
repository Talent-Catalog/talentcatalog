/*
 * Copyright (c) 2026 Talent Catalog.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.tctalent.server.model.db.User;
import org.tctalent.server.service.db.UserService;

class SpringSecurityAuditorAwareTest {

  @InjectMocks
  private SpringSecurityAuditorAware auditorAware;

  @Mock
  private UserService userService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void getCurrentAuditor_returnsLoggedInUser_whenAvailable() {
    User loggedInUser = new User();
    loggedInUser.setId(101L);
    when(userService.getLoggedInUser()).thenReturn(loggedInUser);

    Optional<User> result = auditorAware.getCurrentAuditor();

    assertTrue(result.isPresent());
    assertSame(loggedInUser, result.get());
  }

  @Test
  void getCurrentAuditor_returnsSystemAdminShell_whenNoLoggedInUser() {
    when(userService.getLoggedInUser()).thenReturn(null);
    auditorAware.setSystemAdminId(999L);

    Optional<User> result = auditorAware.getCurrentAuditor();

    assertTrue(result.isPresent());
    User fallbackUser = result.get();
    assertEquals(999L, fallbackUser.getId());
  }

  @Test
  void getCurrentAuditor_returnsEmpty_whenNoLoggedInUserAndNoSystemAdmin() {
    when(userService.getLoggedInUser()).thenReturn(null);

    Optional<User> result = auditorAware.getCurrentAuditor();

    assertFalse(result.isPresent());
  }
}
