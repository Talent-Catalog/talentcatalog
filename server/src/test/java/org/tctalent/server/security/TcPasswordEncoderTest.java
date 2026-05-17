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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

  @Test
  void matches_withNullEncodedPassword_returnsFalse() {
    String rawPassword = "anything";

    boolean result = tcPasswordEncoder.matches(rawPassword, null);

    assertFalse(result);
  }

  @Test
  void matches_withNullEncodedPasswordAndPreviousDefaultPassword_returnsFalse() {
    String rawPassword = "password";

    boolean result = tcPasswordEncoder.matches(rawPassword, null);

    assertFalse(result);
  }
}
