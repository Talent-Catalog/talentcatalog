
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TcUserDetailsServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private TcUserDetailsService tcUserDetailsService;

  private AutoCloseable mocks;
  private ListAppender<ILoggingEvent> logAppender;
  private Logger logger;
  private Level originalLevel;

  @BeforeEach
  void setUp() {
    mocks = MockitoAnnotations.openMocks(this);

    logger = (Logger) LoggerFactory.getLogger(TcUserDetailsService.class);
    originalLevel = logger.getLevel();
    logger.setLevel(Level.DEBUG);
    logAppender = new ListAppender<>();
    logAppender.start();
    logger.addAppender(logAppender);
  }

  @AfterEach
  void tearDown() throws Exception {
    logger.detachAppender(logAppender);
    logAppender.stop();
    logger.setLevel(originalLevel);

    mocks.close();
  }

  @Test
  void constructor_setsUserRepository() {
    verifyNoInteractions(userRepository);
  }

  @Test
  void loadUserByUsername_returnsTcUserDetails_forValidUsername() {
    String username = "testuser";
    User user = new User();
    user.setId(1L);
    user.setUsername(username);
    user.setRole(Role.user);
    when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(user);

    TcUserDetails result = tcUserDetailsService.loadUserByUsername(username);

    assertNotNull(result, "Returned UserDetails should not be null");
    assertEquals(user, result.getUser(), "User in TcUserDetails should match the found user");
    verify(userRepository).findByUsernameIgnoreCase(username);

    // Verify logging
    List<ILoggingEvent> logs = logAppender.list;
    assertEquals(1, logs.size(), "Expected one log message");
    assertEquals(Level.DEBUG, logs.get(0).getLevel());
    assertTrue(logs.get(0).getFormattedMessage().contains("Found user with ID 1 for username 'testuser'"));
  }

  @Test
  void loadUserByUsername_throwsUsernameNotFoundException_forInvalidUsername() {
    String username = "nonexistentuser";
    when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(null);

    UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
        () -> tcUserDetailsService.loadUserByUsername(username),
        "Expected UsernameNotFoundException for invalid username");

    assertEquals("No user found for: " + username, exception.getMessage());
    verify(userRepository).findByUsernameIgnoreCase(username);

    List<ILoggingEvent> logs = logAppender.list;
    assertTrue(logs.isEmpty(), "No log messages expected when user is not found");
  }

  @Test
  void loadUserByUsername_handlesCaseInsensitiveUsername() {
    String username = "TestUser";
    String usernameLower = "testuser";
    User user = new User();
    user.setId(2L);
    user.setUsername(usernameLower);
    user.setRole(Role.user);
    when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(user);

    TcUserDetails result = tcUserDetailsService.loadUserByUsername(username);

    assertNotNull(result, "Returned UserDetails should not be null");
    assertEquals(user, result.getUser(), "User in TcUserDetails should match the found user");
    verify(userRepository).findByUsernameIgnoreCase(username);

    List<ILoggingEvent> logs = logAppender.list;
    assertEquals(1, logs.size(), "Expected one log message");
    assertEquals(Level.DEBUG, logs.get(0).getLevel());
    assertTrue(logs.get(0).getFormattedMessage().contains("Found user with ID 2 for username 'TestUser'"));
  }
}
