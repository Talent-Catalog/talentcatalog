/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.service.db.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.request.user.UpdateUserRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.security.PasswordHelper;

@Tag("skip-test-in-gradle-build")
@SpringBootTest
class UserServiceImplTest {

    @Mock
    private User user;

    @Mock
    private PasswordHelper passwordHelper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void authenticate() {
        User loggedInUser = new User("username", "first", "last",
                "email@test.com", Role.admin);
        when(authService.getLoggedInUser()).thenReturn(Optional.of(loggedInUser));
    }

    @Test
    void createUserAndCountries(){
        assertNotNull(userService);
        assertNotNull(userRepository);
        User user = new User(
                "username", "first", "last",
                "email@test.com", Role.admin);

        when(userRepository.save(user)).thenReturn(user);
        userRepository.save(user);
        assertNotNull(user);
    }

    @Test
    void testCreateUserSourceCountries(){
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("first");
        request.setLastName("last");
        request.setUsername("username2");
        request.setEmail("email2@test.com");
        request.setRole(Role.admin);
        request.setPassword("xxxxxxxxxx");
        request.setReadOnly(false);
        request.setUsingMfa(false);

        when(userRepository.save(any(User.class))).then(returnsFirstArg());

        User testUser = userService.createUser(request);
        assertNotNull(testUser);
        assertThat(testUser.getSourceCountries()).isEmpty();

        Country country1 = new Country("Iraq", Status.active);
        Country country2 = new Country("Jordan", Status.active);
        List<Country> countries = new ArrayList<>();
        countries.add(country1);
        countries.add(country2);
        request.setSourceCountries(countries);

        User testUser2 = userService.createUser(request);
        assertNotNull(testUser2);
        assertThat(testUser2.getSourceCountries()).isNotEmpty();

    }

    @Test
    void updateUser() {
        User user = new User("username2", "first", "last", "email2@test.com", Role.admin);
        user.setId(1L);

        UpdateUserRequest update = new UpdateUserRequest();
        update.setFirstName("new name");
        update.setLastName("last");
        update.setEmail("email2@test.com");
        update.setRole(Role.admin);
        update.setStatus(Status.active);
        update.setReadOnly(false);
        update.setUsingMfa(false);

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).then(returnsFirstArg());
        User testUserNew = userService.updateUser(user.getId(), update);

        assertNotNull(testUserNew);
        assertThat(testUserNew.getFirstName()).isEqualTo("new name");
    }

}
