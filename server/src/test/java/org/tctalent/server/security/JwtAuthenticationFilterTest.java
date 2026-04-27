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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

  @Mock
  private JwtTokenProvider tokenProvider;

  @Mock
  private TcUserDetailsService userDetailsService;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @InjectMocks
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  private AutoCloseable mocks;
  private ListAppender<ILoggingEvent> logAppender;
  private Logger logger;
  private Level originalLevel;

  @BeforeEach
  void setUp() {
    mocks = MockitoAnnotations.openMocks(this);

    logger = (Logger) LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    originalLevel = logger.getLevel();
    logger.setLevel(Level.ERROR);
    logAppender = new ListAppender<>();
    logAppender.start();
    logger.addAppender(logAppender);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() throws Exception {
    logger.detachAppender(logAppender);
    logAppender.stop();
    logger.setLevel(originalLevel);

    SecurityContextHolder.clearContext();

    mocks.close();
  }

  @Test
  void doFilterInternal_skipsAuthenticationForInvalidJwt() throws ServletException, IOException {
    String jwt = "invalid.jwt.token";
    when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
    when(tokenProvider.validateToken(jwt)).thenReturn(false);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(tokenProvider).validateToken(jwt);
    verifyNoMoreInteractions(tokenProvider, userDetailsService);
    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should not be set");

    assertTrue(logAppender.list.isEmpty(), "No error logs expected for invalid JWT");
  }

  @Test
  void doFilterInternal_skipsAuthenticationForMissingJwt() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn(null);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verifyNoInteractions(tokenProvider, userDetailsService);
    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should not be set");

    assertTrue(logAppender.list.isEmpty(), "No error logs expected for missing JWT");
  }

  @Test
  void getJwtFromRequest_extractsTokenFromValidBearerHeader() {
    String jwt = "valid.jwt.token";
    when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);

    String result = invokePrivateMethod("getJwtFromRequest", request);
    assertEquals(jwt, result, "Extracted JWT should match the token");
    verify(request).getHeader("Authorization");
  }

  @Test
  void getJwtFromRequest_returnsNullForInvalidBearerHeader() {
    when(request.getHeader("Authorization")).thenReturn("InvalidToken");
    String result = invokePrivateMethod("getJwtFromRequest", request);

    assertNull(result, "Should return null for invalid Authorization header");
    verify(request).getHeader("Authorization");
  }

  @Test
  void getJwtFromRequest_returnsNullForMissingBearerHeader() {
    when(request.getHeader("Authorization")).thenReturn(null);
    String result = invokePrivateMethod("getJwtFromRequest", request);
    assertNull(result, "Should return null for missing Authorization header");
    verify(request).getHeader("Authorization");
  }

  @SuppressWarnings("unchecked")
  private <T> T invokePrivateMethod(String methodName, Object... args) {
    try {
      Method method = JwtAuthenticationFilter.class.getDeclaredMethod(methodName, HttpServletRequest.class);
      method.setAccessible(true);
      return (T) method.invoke(jwtAuthenticationFilter, args);
    } catch (Exception e) {
      throw new RuntimeException("Failed to invoke private method: " + methodName, e);
    }
  }
}