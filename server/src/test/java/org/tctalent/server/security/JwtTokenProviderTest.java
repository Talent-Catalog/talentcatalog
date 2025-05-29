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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.Key;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;

import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenProviderTest {

  @InjectMocks
  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private LogBuilder logBuilder;

  @Mock
  private Authentication authentication;

  @Mock
  private TcUserDetails tcUserDetails;

  @Mock
  private User user;

  private Key jwtSecret;
  private final int jwtExpirationInMs = 3_600_000;
  private AutoCloseable mocks;

  @BeforeEach
  void setUp() {
    mocks = MockitoAnnotations.openMocks(this);

    byte[] keyBytes = new byte[64];
    new java.security.SecureRandom().nextBytes(keyBytes);
    String jwtSecretBase64 = Base64.getEncoder().encodeToString(keyBytes);
    jwtSecret = io.jsonwebtoken.security.Keys.hmacShaKeyFor(Base64.getDecoder().decode(
        jwtSecretBase64));

    setField(jwtTokenProvider, "jwtSecretBase64", jwtSecretBase64);
    setField(jwtTokenProvider, "jwtExpirationInMs", jwtExpirationInMs);

    jwtTokenProvider.afterPropertiesSet();
  }

  @AfterEach
  void tearDown() throws Exception {
    mocks.close();
  }

  @Test
  void afterPropertiesSet_initializesJwtSecret() {
    jwtTokenProvider.afterPropertiesSet();
    java.security.Key initializedKey = getField(jwtTokenProvider, "jwtSecret");
    assertNotNull(initializedKey, "JWT secret key should be initialized");
  }

  @Test
  void generateToken_withUserDetails_generatesValidToken() {
    String username = "testuser";
    when(authentication.getPrincipal()).thenReturn(tcUserDetails);
    when(tcUserDetails.getUsername()).thenReturn(username);
    when(tcUserDetails.getUser()).thenReturn(user);
    when(user.getRole()).thenReturn(Role.admin);

    String token = jwtTokenProvider.generateToken(authentication);

    assertNotNull(token, "Generated token should not be null");
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(jwtSecret)
        .build()
        .parseClaimsJws(token)
        .getBody();

    assertEquals(username, claims.getSubject(), "Token subject should match username");
    assertNotNull(claims.getIssuedAt(), "Token should have issued-at date");
    assertNotNull(claims.getExpiration(), "Token should have expiration date");
  }

  @Test
  void generateToken_withCandidateRole_setsNoExpiration() {
    String username = "candidate";
    when(authentication.getPrincipal()).thenReturn(tcUserDetails);
    when(tcUserDetails.getUsername()).thenReturn(username);
    when(tcUserDetails.getUser()).thenReturn(user);
    when(user.getRole()).thenReturn(Role.user);

    String token = jwtTokenProvider.generateToken(authentication);

    assertNotNull(token, "Generated token should not be null");
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(jwtSecret)
        .build()
        .parseClaimsJws(token)
        .getBody();

    assertEquals(username, claims.getSubject(), "Token subject should match username");
    assertNull(claims.getExpiration(), "Candidate token should have no expiration");
  }

  @Test
  void generateToken_withNonUserDetailsPrincipal_generatesTokenWithNullSubject() {
    when(authentication.getPrincipal()).thenReturn(new Object());

    String token = jwtTokenProvider.generateToken(authentication);

    assertNotNull(token, "Generated token should not be null");
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(jwtSecret)
        .build()
        .parseClaimsJws(token)
        .getBody();

    assertNull(claims.getSubject(), "Token subject should be null for non-TcUserDetails principal");
    assertNotNull(claims.getExpiration(), "Token should have expiration date");
  }

  @Test
  void getUsernameFromJwt_extractsUsername() {
    String username = "testuser";
    String token = Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
        .signWith(jwtSecret)
        .compact();

    String extractedUsername = jwtTokenProvider.getUsernameFromJwt(token);

    assertEquals(username, extractedUsername, "Extracted username should match token subject");
  }

  @Test
  void validateToken_withValidToken_returnsTrue() {
    String token = Jwts.builder()
        .setSubject("testuser")
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
        .signWith(jwtSecret)
        .compact();

    boolean isValid = jwtTokenProvider.validateToken(token);

    assertTrue(isValid, "Valid token should return true");
    verifyNoInteractions(logBuilder);
  }

  // Helper methods for reflection-based field access
  private void setField(Object target, String fieldName, Object value) {
    try {
      java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Failed to set field: " + fieldName, e);
    }
  }

  private <T> T getField(Object target, String fieldName) {
    try {
      java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      return (T) field.get(target);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Failed to get field: " + fieldName, e);
    }
  }
}