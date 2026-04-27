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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tctalent.server.exception.InvalidPasswordFormatException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PasswordHelperTest {

  @InjectMocks
  private PasswordHelper passwordHelper;

  @Mock
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void validateAndEncodePassword_validPassword_returnsEncodedPassword() {
    String password = "password123";
    String encodedPassword = "encodedPass123";

    when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

    String result = passwordHelper.validateAndEncodePassword(password);

    assertEquals(encodedPassword, result);
  }

  @Test
  void validateAndEncodePassword_blankPassword_throwsInvalidPasswordFormatException() {
    String password = "";

    InvalidPasswordFormatException ex = assertThrows(InvalidPasswordFormatException.class,
        () -> {
          passwordHelper.validateAndEncodePassword(password);
        });

    assertEquals("Password must not be blank", ex.getMessage());
  }

  @Test
  void validateAndEncodePassword_shortPassword_throwsInvalidPasswordFormatException() {
    String password = "pass";

    InvalidPasswordFormatException ex = assertThrows(InvalidPasswordFormatException.class,
        () -> {
          passwordHelper.validateAndEncodePassword(password);
        });

    assertEquals("Password must be at least 8 characters long", ex.getMessage());
  }

  @Test
  void encodePassword_encodesPassword() {
    String password = "password123";
    String encodedPassword = "encodedPass123";

    when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

    String result = passwordHelper.encodePassword(password);

    assertEquals(encodedPassword, result);
  }

  @Test
  void validatePasswordRules_validPassword_passes() {
    String password = "password123";

    assertDoesNotThrow(() -> passwordHelper.validatePasswordRules(password));
  }

  @Test
  void validatePasswordRules_nullPassword_throwsInvalidPasswordFormatException() {
    String password = null;

    InvalidPasswordFormatException ex = assertThrows(InvalidPasswordFormatException.class,
        () -> {
          passwordHelper.validatePasswordRules(password);
        });

    assertEquals("Password must not be blank", ex.getMessage());
  }

  @Test
  void validatePasswordRules_blankPassword_throwsInvalidPasswordFormatException() {
    String password = " ";

    InvalidPasswordFormatException ex = assertThrows(InvalidPasswordFormatException.class,
        () -> {
          passwordHelper.validatePasswordRules(password);
        });

    assertEquals("Password must not be blank", ex.getMessage());
  }

  @Test
  void validatePasswordRules_shortPassword_throwsInvalidPasswordFormatException() {
    String password = "pass123";

    InvalidPasswordFormatException ex = assertThrows(InvalidPasswordFormatException.class,
        () -> {
          passwordHelper.validatePasswordRules(password);
        });

    assertEquals("Password must be at least 8 characters long", ex.getMessage());
  }

  @Test
  void isValidPassword_matchingPassword_returnsTrue() {
    String password = "password123";
    String encodedPassword = "encodedPass123";

    when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

    boolean result = passwordHelper.isValidPassword(password, encodedPassword);

    assertTrue(result);
  }

  @Test
  void isValidPassword_nonMatchingPassword_returnsFalse() {
    String password = "password123";
    String encodedPassword = "encodedPass123";

    when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

    boolean result = passwordHelper.isValidPassword(password, encodedPassword);

    assertFalse(result);
  }
}
