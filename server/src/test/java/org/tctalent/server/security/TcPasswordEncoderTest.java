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
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TcPasswordEncoderTest {

  @Spy
  private TcPasswordEncoder tcPasswordEncoder;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    doCallRealMethod().when(tcPasswordEncoder).matches(any(), any());
  }

  @Test
  void matches_withValidEncodedPassword_delegatesToSuper() {
    String rawPassword = "password123";
    String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);

    doReturn(true).when((BCryptPasswordEncoder) tcPasswordEncoder).matches(rawPassword, encodedPassword);

    boolean result = tcPasswordEncoder.matches(rawPassword, encodedPassword);

    assertTrue(result);
    verify((BCryptPasswordEncoder) tcPasswordEncoder).matches(rawPassword, encodedPassword);
  }

  @Test
  void matches_withInvalidEncodedPassword_delegatesToSuper() {
    String rawPassword = "password123";
    String encodedPassword = new BCryptPasswordEncoder().encode("differentPassword");

    doReturn(false).when((BCryptPasswordEncoder) tcPasswordEncoder).matches(rawPassword, encodedPassword);

    boolean result = tcPasswordEncoder.matches(rawPassword, encodedPassword);

    assertFalse(result);
    verify((BCryptPasswordEncoder) tcPasswordEncoder).matches(rawPassword, encodedPassword);
  }

  @Test
  void matches_withNullRawPassword_throwsIllegalArgumentException() {
    CharSequence rawPassword = null;
    String encodedPassword = new BCryptPasswordEncoder().encode("password123");

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
      tcPasswordEncoder.matches(rawPassword, encodedPassword);
    });

    assertEquals("rawPassword cannot be null", ex.getMessage());
    verify((BCryptPasswordEncoder) tcPasswordEncoder).matches(rawPassword, encodedPassword);
  }
}