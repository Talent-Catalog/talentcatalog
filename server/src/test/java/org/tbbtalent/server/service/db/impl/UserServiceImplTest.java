/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.service.db.impl;

import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.model.db.Role;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.UserRepository;
import org.tbbtalent.server.request.user.CreateUserRequest;
import org.tbbtalent.server.security.PasswordHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class UserServiceImplTest {

    @Mock
    private User user;

    @Mock
    private PasswordHelper passwordHelper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

//    @Test
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

//    @Test
    void testCreateUserSourceCountries(){
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("first");
        request.setLastName("last");
        request.setUsername("username2");
        request.setEmail("email2@test.com");
        request.setRole(Role.admin);
        request.setPassword("xxxxxxxxxx");

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

}
