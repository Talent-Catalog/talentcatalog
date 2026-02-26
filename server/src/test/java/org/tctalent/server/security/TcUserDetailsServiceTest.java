
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.UserRepository;

class TcUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private User user;

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
    void create_setsUserRepository() {
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
        assertTrue(logs.get(0).getFormattedMessage()
            .contains("Found user with ID 1 for username 'testuser'"));
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
        assertTrue(logs.get(0).getFormattedMessage()
            .contains("Found user with ID 2 for username 'TestUser'"));
    }


    @Test
    void create_withReadOnly_setsReadOnlyAuthority() {
        when(userRepository.findByUsernameIgnoreCase(anyString())).thenReturn(user);
        when(user.getReadOnly()).thenReturn(true);
        when(user.getRole()).thenReturn(Role.admin); // Role should be ignored if read-only

        TcUserDetails tcUserDetails = tcUserDetailsService.loadUserByUsername("xxx");

        Collection<? extends GrantedAuthority> authorities = tcUserDetails.getAuthorities();
        assertEquals(1, authorities.size(), "Should have exactly one authority");
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_READONLY")),
            "Authority should be ROLE_READONLY");
        assertTrue(tcUserDetails.hasRole("READONLY"), "Authority should be ROLE_READONLY");
        assertTrue(tcUserDetails.hasAnyRole("X", "READONLY", "Y"),
            "Authority should be ROLE_READONLY");

    }

    @Test
    void create_withSystemAdminRole_setsSystemAdminAuthority() {
        when(userRepository.findByUsernameIgnoreCase(anyString())).thenReturn(user);
        when(user.getReadOnly()).thenReturn(false);
        when(user.getRole()).thenReturn(Role.systemadmin);

        TcUserDetails tcUserDetails = tcUserDetailsService.loadUserByUsername("xxx");

        Collection<? extends GrantedAuthority> authorities = tcUserDetails.getAuthorities();
        assertEquals(1, authorities.size(), "Should have exactly one authority");
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SYSTEMADMIN")), "Authority should be ROLE_SYSTEMADMIN");
    }

    @Test
    void create_withAdminRole_setsAdminAuthority() {
        when(userRepository.findByUsernameIgnoreCase(anyString())).thenReturn(user);
        when(user.getReadOnly()).thenReturn(false);
        when(user.getRole()).thenReturn(Role.admin);

        TcUserDetails tcUserDetails = tcUserDetailsService.loadUserByUsername("xxx");

        Collection<? extends GrantedAuthority> authorities = tcUserDetails.getAuthorities();
        assertEquals(1, authorities.size(), "Should have exactly one authority");
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")), "Authority should be ROLE_ADMIN");
    }

    @Test
    void create_withPartnerAdminRole_setsPartnerAdminAuthority() {
        when(userRepository.findByUsernameIgnoreCase(anyString())).thenReturn(user);
        when(user.getReadOnly()).thenReturn(false);
        when(user.getRole()).thenReturn(Role.partneradmin);

        TcUserDetails tcUserDetails = tcUserDetailsService.loadUserByUsername("xxx");

        Collection<? extends GrantedAuthority> authorities = tcUserDetails.getAuthorities();
        assertEquals(1, authorities.size(), "Should have exactly one authority");
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_PARTNERADMIN")), "Authority should be ROLE_PARTNERADMIN");
    }

    @Test
    void create_withSemiLimitedRole_setsSemiLimitedAuthority() {
        when(userRepository.findByUsernameIgnoreCase(anyString())).thenReturn(user);
        when(user.getReadOnly()).thenReturn(false);
        when(user.getRole()).thenReturn(Role.semilimited);

        TcUserDetails tcUserDetails = tcUserDetailsService.loadUserByUsername("xxx");

        Collection<? extends GrantedAuthority> authorities = tcUserDetails.getAuthorities();
        assertEquals(1, authorities.size(), "Should have exactly one authority");
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SEMILIMITED")), "Authority should be ROLE_SEMILIMITED");
    }

    @Test
    void create_withLimitedRole_setsLimitedAuthority() {
        when(userRepository.findByUsernameIgnoreCase(anyString())).thenReturn(user);
        when(user.getReadOnly()).thenReturn(false);
        when(user.getRole()).thenReturn(Role.limited);

        TcUserDetails tcUserDetails = tcUserDetailsService.loadUserByUsername("xxx");

        Collection<? extends GrantedAuthority> authorities = tcUserDetails.getAuthorities();
        assertEquals(1, authorities.size(), "Should have exactly one authority");
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_LIMITED")), "Authority should be ROLE_LIMITED");
    }

    @Test
    void create_withUserRole_setsUserAuthority() {
        when(userRepository.findByUsernameIgnoreCase(anyString())).thenReturn(user);
        when(user.getReadOnly()).thenReturn(false);
        when(user.getRole()).thenReturn(Role.user);

        TcUserDetails tcUserDetails = tcUserDetailsService.loadUserByUsername("xxx");

        Collection<? extends GrantedAuthority> authorities = tcUserDetails.getAuthorities();
        assertEquals(1, authorities.size(), "Should have exactly one authority");
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")), "Authority should be ROLE_USER");
    }

}
