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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.User;

import java.io.IOException;

import static org.mockito.Mockito.*;

class LanguageFilterTest {

  @InjectMocks
  private LanguageFilter languageFilter;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @Mock
  private TcUserDetails tcUserDetails;

  @Mock
  private User user;


  @Mock
  private LogBuilder logBuilder;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    SecurityContextHolder.setContext(securityContext);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilter_withValidLanguageHeader_setsUserLanguage() throws IOException, ServletException {
    String language = "fr";
    when(request.getHeader("X-Language")).thenReturn(language);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(tcUserDetails);
    when(tcUserDetails.getUser()).thenReturn(user);

    languageFilter.doFilterInternal(request, response, filterChain);

    verify(user).setSelectedLanguage(language);
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(logBuilder);
  }

  @Test
  void doFilter_withBlankLanguageHeader_setsDefaultLanguage() throws IOException, ServletException {
    when(request.getHeader("X-Language")).thenReturn("");
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(tcUserDetails);
    when(tcUserDetails.getUser()).thenReturn(user);

    languageFilter.doFilterInternal(request, response, filterChain);

    verify(user).setSelectedLanguage("en");
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(logBuilder);
  }

  @Test
  void doFilter_withNullLanguageHeader_setsDefaultLanguage() throws IOException, ServletException {
    when(request.getHeader("X-Language")).thenReturn(null);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(tcUserDetails);
    when(tcUserDetails.getUser()).thenReturn(user);

    languageFilter.doFilterInternal(request, response, filterChain);

    verify(user).setSelectedLanguage("en");
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(logBuilder);
  }

  @Test
  void doFilter_withNoAuthentication_proceedsWithoutSettingLanguage() throws IOException, ServletException {
    when(securityContext.getAuthentication()).thenReturn(null);

    languageFilter.doFilterInternal(request, response, filterChain);

    verifyNoInteractions(tcUserDetails, user);
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(logBuilder);
  }

  @Test
  void doFilter_withNonTcUserDetailsPrincipal_proceedsWithoutSettingLanguage() throws IOException, ServletException {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(new Object());

    languageFilter.doFilterInternal(request, response, filterChain);

    verifyNoInteractions(tcUserDetails, user);
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(logBuilder);
  }
}