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

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.repository.db.UserSpecification;
import org.tctalent.server.request.user.SearchUserRequest;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private Sort sort;
    private PageRequest pageRequest;
    private Page<User> userPage;
    private long userId;

    @Mock private UserRepository userRepository;
    @Mock private User mockUser;
    @Mock private User mockUser2;
    @Mock private SearchUserRequest searchUserRequest;
    @Mock private Specification<User> mockedUserSpec;
    @Mock private CountryRepository countryRepository;
    @Mock private Country mockCountry;
    @Mock private Country mockCountry2;

    @InjectMocks private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        sort = Sort.unsorted();
        pageRequest = PageRequest.of(0, 10, sort);
        userPage = new PageImpl<>(List.of(mockUser, mockUser2));
        userId = 1L;
    }

    @Test
    @DisplayName("should return user")
    void findByUsernameAndRole_shouldReturnUser() {
        given(userRepository.findByUsernameAndRole("alice", Role.user))
            .willReturn(mockUser);

        User result = userService.findByUsernameAndRole("alice", Role.user);

        assertEquals(mockUser, result);
        verify(userRepository).findByUsernameAndRole("alice", Role.user);
    }

    @Test
    @DisplayName("should return null when not found")
    void findByUsernameAndRole_shouldReturnNullWhenNotFound() {
        given(userRepository.findByUsernameAndRole("bob", Role.user))
            .willReturn(null);

        User result = userService.findByUsernameAndRole("bob", Role.user);

        assertNull(result);
    }

    @Test
    @DisplayName("should return user list")
    void search_shouldReturnUserList() {
        // MODEL: mocking static methods - must be inside try block along with any code that depends
        // on the mock return. Otherwise, Mockito won't match the stub due to instance mismatch.
        try (MockedStatic<UserSpecification> mocked = Mockito.mockStatic(UserSpecification.class)) {

            mocked.when(() -> UserSpecification.buildSearchQuery(searchUserRequest))
                .thenReturn(mockedUserSpec);
            given(searchUserRequest.getSort()).willReturn(sort);
            given(userRepository.findAll(mockedUserSpec, sort))
                .willReturn(List.of(mockUser, mockUser2));

            List<User> results = userService.search(searchUserRequest);

            assertEquals(List.of(mockUser, mockUser2), results);
            verify(userRepository).findAll(mockedUserSpec, sort);
        }
    }

    @Test
    @DisplayName("should return empty user list when nothing found")
    void search_shouldReturnEmptyUserListWhenNothingFound() {
        try (MockedStatic<UserSpecification> mocked = Mockito.mockStatic(UserSpecification.class)) {

            mocked.when(() -> UserSpecification.buildSearchQuery(searchUserRequest))
                .thenReturn(mockedUserSpec);
            given(searchUserRequest.getSort()).willReturn(sort);
            given(userRepository.findAll(mockedUserSpec, sort))
                .willReturn(Collections.emptyList());

            List<User> results = userService.search(searchUserRequest);

            assertTrue(results.isEmpty());
            verify(userRepository).findAll(mockedUserSpec, sort);
        }
    }

    @Test
    @DisplayName("should return user page")
    void searchPaged_shouldReturnUserPage() {
        try (MockedStatic<UserSpecification> mocked = Mockito.mockStatic(UserSpecification.class)) {

            mocked.when(() -> UserSpecification.buildSearchQuery(searchUserRequest))
                .thenReturn(mockedUserSpec);
            given(searchUserRequest.getPageRequest()).willReturn(pageRequest);
            given(userRepository.findAll(mockedUserSpec, pageRequest))
                .willReturn(userPage);

            Page<User> results = userService.searchPaged(searchUserRequest);

            assertEquals(userPage, results);
            verify(userRepository).findAll(mockedUserSpec, pageRequest);
        }

    }

    @Test
    @DisplayName("should return empty user page when nothing found")
    void searchPaged_shouldReturnEmptyUserPageWhenNothingFound() {
        Page<User> emptyUserPage = new PageImpl<>(Collections.emptyList());

        try (MockedStatic<UserSpecification> mocked = Mockito.mockStatic(UserSpecification.class)) {

            mocked.when(() -> UserSpecification.buildSearchQuery(searchUserRequest))
                .thenReturn(mockedUserSpec);
            given(searchUserRequest.getPageRequest()).willReturn(pageRequest);
            given(userRepository.findAll(mockedUserSpec, pageRequest))
                .willReturn(emptyUserPage);

            Page<User> results = userService.searchPaged(searchUserRequest);

            assertEquals(emptyUserPage, results);
            verify(userRepository).findAll(mockedUserSpec, pageRequest);
        }

    }

    @Test
    @DisplayName("should return user when found")
    void findById_shouldReturnUser() {
        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));

        User result = userService.getUser(userId);

        assertEquals(mockUser, result);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("should throw NoSuchObjectException when user not found")
    void getUser_shouldThrowWhenUserNotFound() {
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        NoSuchObjectException exception = assertThrows(
            NoSuchObjectException.class,
            () -> userService.getUser(userId) // When
        );

        assertTrue(exception.getMessage().contains(String.valueOf(userId)));
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("should return all countries when user has no source countries")
    public void getDefaultSourceCountries_shouldReturnAllCountries_whenUserHasNoSourceCountries() {
        List<Country> allCountriesList = Arrays.asList(mockCountry, mockCountry2);
        given(mockUser.getSourceCountries()).willReturn(Collections.emptySet());
        given(countryRepository.findAll()).willReturn(allCountriesList);

        Set<Country> result = userService.getDefaultSourceCountries(mockUser); // When

        assertEquals(2, result.size());
        assertTrue(result.contains(mockCountry));
        assertTrue(result.contains(mockCountry2));
    }

    @Test
    @DisplayName("should return user source countries when they have some")
    public void getDefaultSourceCountries_shouldReturnUserSourceCountries() {
        Set<Country> userSourceCountries = Set.of(mockCountry, mockCountry2);
        given(mockUser.getSourceCountries()).willReturn(userSourceCountries);

        Set<Country> result = userService.getDefaultSourceCountries(mockUser); // When

        assertEquals(userSourceCountries, result);
    }

}
