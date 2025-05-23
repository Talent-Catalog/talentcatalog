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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.security.auth.login.AccountLockedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.tctalent.server.api.admin.AdminApiTestUtil;
import org.tctalent.server.api.admin.AdminApiTestUtil.CreateUpdateUserTestData;
import org.tctalent.server.configuration.SystemAdminConfiguration;
import org.tctalent.server.exception.InvalidCredentialsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.ServiceException;
import org.tctalent.server.exception.UsernameTakenException;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.repository.db.UserSpecification;
import org.tctalent.server.request.LoginRequest;
import org.tctalent.server.request.user.SearchUserRequest;
import org.tctalent.server.request.user.UpdateUserRequest;
import org.tctalent.server.request.user.emailverify.SendVerifyEmailRequest;
import org.tctalent.server.request.user.emailverify.VerifyEmailRequest;
import org.tctalent.server.response.JwtAuthenticationResponse;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.security.JwtTokenProvider;
import org.tctalent.server.security.PasswordHelper;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.email.EmailHelper;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private Sort sort;
    private PageRequest pageRequest;
    private Page<User> userPage;
    private long userId;
    private User expectedUser;
    private UpdateUserRequest request;
    private User testUser;
    private Long approverId;
    private Long partnerId;
    private List<Country> sourceCountryList;
    private String loginUsername;
    private LoginRequest loginRequest;
    private SendVerifyEmailRequest sendVerifyEmailRequest;
    private VerifyEmailRequest verifyEmailRequest;

    @Mock private UserRepository userRepository;
    @Mock private User mockUser;
    @Mock private User mockUser2;
    @Mock private SearchUserRequest searchUserRequest;
    @Mock private Specification<User> mockedUserSpec;
    @Mock private CountryRepository countryRepository;
    @Mock private Country mockCountry;
    @Mock private Country mockCountry2;
    @Mock private PartnerService partnerService;
    @Mock private PartnerImpl mockPartnerImpl;
    @Mock private PasswordHelper passwordHelper;
    @Mock private AuthService authService;
    @Mock private AuthenticationManager authManager;
    @Mock private Authentication mockAuth;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private EmailHelper emailHelper;

    @Captor ArgumentCaptor<User> userCaptor;

    @InjectMocks
    @Spy
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        sort = Sort.unsorted();
        pageRequest = PageRequest.of(0, 10, sort);
        userPage = new PageImpl<>(List.of(mockUser, mockUser2));
        userId = 11L;
        sourceCountryList = List.of(mockCountry, mockCountry2);
        CreateUpdateUserTestData testData = AdminApiTestUtil.createUpdateUserRequestAndExpectedUser();
        expectedUser = testData.expectedUser();
        request = testData.request();
        testUser = AdminApiTestUtil.getFullUser();
        approverId = 1L;
        partnerId = 1L;
        loginUsername = "person@email.com";
        loginRequest = new LoginRequest();
        loginRequest.setUsername(loginUsername);
        loginRequest.setPassword("password");
        sendVerifyEmailRequest = new SendVerifyEmailRequest();
        sendVerifyEmailRequest.setEmail(request.getEmail());
        verifyEmailRequest = new VerifyEmailRequest();
        verifyEmailRequest.setToken("fakeToken");
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
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

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
        request.setSourceCountries(sourceCountryList);

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

    @Test
    @DisplayName("should create and return new admin user matching valid request")
    void createUser_withValidRequest_shouldCreateAndReturnExpectedUser() {
        // Testing Approver path
        request.setApproverId(approverId);
        expectedUser.setApprover(mockUser2);

        // Testing Partner path
        request.setPartnerId(partnerId);
        expectedUser.setPartner(mockPartnerImpl);

        // Source Country path
        request.setSourceCountries(sourceCountryList);
        expectedUser.setSourceCountries(new HashSet<>(sourceCountryList));

        // Audit fields
        expectedUser.setCreatedBy(mockUser);
        expectedUser.setUpdatedBy(mockUser);

        given(userRepository.findByUsernameIgnoreCase(request.getUsername())).willReturn(null);
        given(userRepository.findByEmailIgnoreCase(request.getEmail())).willReturn(null);
        given(partnerService.getPartner(request.getPartnerId())).willReturn(mockPartnerImpl);
        doReturn(mockUser2).when(userService).getUser(request.getApproverId());
        given(mockUser.getRole()).willReturn(Role.systemadmin);
        given(passwordHelper.validateAndEncodePassword(request.getPassword()))
            .willReturn(request.getPassword());

        userService.createUser(request, mockUser); // When

        verify(userRepository).save(userCaptor.capture());
        // MODEL: compare field values rather than object instances:
        assertThat(userCaptor.getValue())
            .usingRecursiveComparison()
            .ignoringFields("createdDate", "updatedDate")
            .isEqualTo(expectedUser);
    }

    @Test
    @DisplayName("should throw UsernameTakenException when username taken")
    void populateUserFields_shouldThrowUsernameTakenException_whenUsernameTaken() {
        given(userRepository.findByUsernameIgnoreCase(request.getUsername()))
            .willReturn(mockUser);

        assertThrows(UsernameTakenException.class, () -> userService.createUser(request, mockUser));
    }

    @Test
    @DisplayName("should throw UsernameTakenException when email taken")
    void populateUserFields_shouldThrowUsernameTakenException_whenEmailTaken() {
        given(userRepository.findByUsernameIgnoreCase(request.getUsername())).willReturn(null);
        given(userRepository.findByEmailIgnoreCase(request.getEmail())).willReturn(mockUser);

        assertThrows(UsernameTakenException.class, () -> userService.createUser(request, mockUser));
    }

    @Test
    @DisplayName("should reassign partner when requested and logged in user is System Admin")
    void reassignPartnerIfNeeded_shouldReassignPartner_whenUserIsSystemAdmin() {
        // Following update() path to reach reassignPartnerIfNeeded()
        given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser));
        // Return test user who has a different partner than mockPartnerImpl:
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(mockUser.getReadOnly()).willReturn(false);
        given(mockUser.getRole()).willReturn(Role.systemadmin);
        given(authService.hasAdminPrivileges(Role.systemadmin)).willReturn(true);
        // Mock update request has partner ID that will return mockPartnerImpl:
        given(partnerService.getPartner(request.getPartnerId())).willReturn(mockPartnerImpl);
        request.setApproverId(null); // We're not testing the approver path

        User result = userService.updateUser(userId, request); // When

        verify(userRepository).save(testUser);
        assertEquals(result.getPartner(), mockPartnerImpl); // Request partner assigned
    }

    @Test
    @DisplayName("should throw InvalidRequestException when logged in user is regular admin and "
        + "tries to reassign partner")
    void reassignPartnerIfNeeded_shouldThrowInvalidRequestException_whenUserIsAdmin() {
        // Following update() path to reach reassignPartnerIfNeeded()
        given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser));
        // Return test user who has a different partner than mockPartnerImpl:
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(mockUser.getReadOnly()).willReturn(false);
        given(mockUser.getRole()).willReturn(Role.admin);
        given(authService.hasAdminPrivileges(Role.admin)).willReturn(true);

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.updateUser(userId, request)
        );

        assertEquals("You don't have permission to change a partner.", ex.getMessage());
    }

    @Test
    @DisplayName("should throw InvalidRequestException when no partnerId in request")
    void reassignPartnerIfNeeded_shouldThrowInvalidRequestException_whenNoPartnerId() {
        request.setPartnerId(null);
        // The below path would reassign successfully if not for the null partnerId
        given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser));
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(mockUser.getReadOnly()).willReturn(false);
        given(mockUser.getRole()).willReturn(Role.systemadmin);
        given(authService.hasAdminPrivileges(Role.systemadmin)).willReturn(true);

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.updateUser(userId, request)
        );

        assertEquals("A partner must be specified.", ex.getMessage());
    }

    @Test
    @DisplayName("should set new approver if different and logged in user is System Admin")
    void updateApproverIfNeeded_shouldSetNewApproverIfDifferentAndLoggedInUserIsSystemAdmin() {
        setupPathToUpdateApproverIfNeeded();
        given(mockUser.getRole()).willReturn(Role.systemadmin);
        given(authService.hasAdminPrivileges(Role.systemadmin)).willReturn(true);
        given(userRepository.findById(request.getApproverId())).willReturn(Optional.of(mockUser2));

        User result = userService.updateUser(userId, request);

        verify(userRepository).save(testUser);
        assertEquals(result.getApprover(), mockUser2);
    }

    @Test
    @DisplayName("should throw InvalidRequestException when approver update attempted and logged in "
        + "user is regular admin")
    void updateApproverIfNeeded_shouldThrowInvalidRequestException_whenUpdatingUserIsAdmin() {
        setupPathToUpdateApproverIfNeeded();
        testUser.setPartner(null); // Handles partner assign path for regular admin (no update)
        given(mockUser.getRole()).willReturn(Role.admin);
        given(authService.hasAdminPrivileges(Role.admin)).willReturn(true);

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.updateUser(userId, request)
        );

        assertEquals("You don't have permission to assign an approver.", ex.getMessage());
    }

    private void setupPathToUpdateApproverIfNeeded() {
        request.setApproverId(approverId);
        expectedUser.setApprover(mockUser2);
        given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser)); // Updating user
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser)); // User being updated
    }

    @Test
    @DisplayName("should throw InvalidRequestException when Admin tries to create System Admin")
    void addRoleIfValid_shouldThrowInvalidRequestException_whenAdminTriesToCreateSystemAdmin() {
        request.setRole(Role.systemadmin);
        given(mockUser.getRole()).willReturn(Role.admin);

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.createUser(request, mockUser)
        );

        assertEquals("You don't have permission to save this role type.", ex.getMessage());
    }

    @Test
    @DisplayName("should allow System Admin to create System Admin")
    void addRoleIfValid_shouldAllowSystemAdminToCreateSystemAdmin() {
        request.setRole(Role.systemadmin);
        given(mockUser.getRole()).willReturn(Role.systemadmin);

        userService.createUser(request, mockUser);

        verify(userRepository).save(userCaptor.capture());
        assertEquals(userCaptor.getValue().getRole(), Role.systemadmin);
    }

    @Test
    @DisplayName("should allow creating user with no source country restrictions (empty) to add "
        + "any source country to new user")
    void addSourceCountriesIfValid_shouldAllowUnrestrictedAdminToCreateUserWithAnySourceCountry() {
        setupPathToAddSourceCountriesIfValid();
        // No restrictions on creating user:
        given(mockUser.getSourceCountries()).willReturn(Collections.emptySet());

        userService.createUser(request, mockUser); // When

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getSourceCountries(),
            containsInAnyOrder(sourceCountryList.toArray()));
    }

    @Test
    @DisplayName("should not allow restricted creating user to assign any source country that they "
        + "can't themselves access")
    void addSourceCountriesIfValid_shouldNotAllowRestrictedAdminToCreateUserWithAnySourceCountry() {
        setupPathToAddSourceCountriesIfValid();
        // Creating user restricted to one source country:
        given(mockUser.getSourceCountries()).willReturn(Set.of(mockCountry));

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.createUser(request, mockUser)
        );

        assertEquals("You don't have permission to add this country.", ex.getMessage());
    }

    @Test
    @DisplayName("should set new user source countries the same as restricted partner admin if "
        + "none specified in request")
    void addSourceCountriesIfValid_shouldSetNewUserSourceCountriesSameAsCreatingUser_whenEmptyRequest() {
        setupPathToAddSourceCountriesIfValid();
        request.setSourceCountries(null);
        given(mockUser.getSourceCountries()).willReturn(new HashSet<>(sourceCountryList));

        userService.createUser(request, mockUser);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getSourceCountries(),
            containsInAnyOrder(sourceCountryList.toArray()));
    }

    private void setupPathToAddSourceCountriesIfValid() {
        request.setSourceCountries(sourceCountryList);
        request.setRole(Role.partneradmin);
        given(mockUser.getRole()).willReturn(Role.partneradmin);
    }

    @Test
    @DisplayName("should throw InvalidRequestException when read-only user tries to create a user")
    void createUser_shouldThrowInvalidRequestException_whenReadonlyUserTriesToCreateUser() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser));
        given(mockUser.getReadOnly()).willReturn(true);

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.createUser(request)
        );

        assertEquals("You don't have permission to create a user.", ex.getMessage());
    }

    @Test
    @DisplayName("should throw InvalidRequestException when creating user has no admin privileges")
    void createUser_shouldThrowInvalidRequestException_whenCreatingUserHasNoPrivileges() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser));
        given(mockUser.getReadOnly()).willReturn(false);
        given(authService.hasAdminPrivileges(mockUser.getRole())).willReturn(false);

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.createUser(request)
        );

        assertEquals("You don't have permission to create a user.", ex.getMessage());
    }

    @Test
    @DisplayName("should reset email verification fields when new email supplied")
    void checkAndResetEmailVerification_shouldResetEmailVerification_whenNewEmailIsSupplied() {
        setupPathToCheckAndResetEmailVerification();

        userService.updateUser(userId, request);

        verify(mockUser2).setEmailVerified(eq(false));
        verify(mockUser2).setEmailVerificationToken(null);
        verify(mockUser2).setEmailVerificationTokenIssuedDate(null);
    }

    @Test
    @DisplayName("should do nothing when request email same as old")
    void checkAndResetEmailVerification_shouldDoNothing_whenRequestEmailIsSameAsOld() {
        setupPathToCheckAndResetEmailVerification();
        given(mockUser2.getEmail()).willReturn(request.getEmail()); // Same email

        userService.updateUser(userId, request);

        verify(mockUser2, never()).setEmailVerified(eq(false));
        verify(mockUser2, never()).setEmailVerificationToken(null);
        verify(mockUser2, never()).setEmailVerificationTokenIssuedDate(null);
    }

    @Test
    @DisplayName("should do nothing when no email in request")
    void checkAndResetEmailVerification_shouldDoNothing_whenNoEmailInRequest() {
        setupPathToCheckAndResetEmailVerification();
        request.setEmail(null);

        userService.updateUser(userId, request);

        verify(mockUser2, never()).setEmailVerified(eq(false));
        verify(mockUser2, never()).setEmailVerificationToken(null);
        verify(mockUser2, never()).setEmailVerificationTokenIssuedDate(null);
    }

    private void setupPathToCheckAndResetEmailVerification() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser)); // logged-in user
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser2)); // user being updated
        // Logged-in user has required privileges:
        given(mockUser.getReadOnly()).willReturn(false);
        given(mockUser.getRole()).willReturn(Role.admin);
        given(authService.hasAdminPrivileges(mockUser.getRole())).willReturn(true);
    }

    @Test
    @DisplayName("should return false and cause caller updateUser() to throw InvalidRequestException"
        + " when logged in user is read-only")
    void authoriseAdminUser_shouldReturnFalse_whenLoggedInUserIsReadonly() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser)); // logged-in user
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser2)); // user being updated
        given(mockUser.getReadOnly()).willReturn(true);

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.updateUser(userId, request)
        );

        assertEquals("You don't have permission to edit this user.", ex.getMessage());
    }

    @Test
    @DisplayName("should return false and cause caller updateUser() to throw InvalidRequestException"
        + " when hasAdminPrivileges returns false")
    void authoriseAdminUser_shouldReturnFalse_whenHasAdminPrivilegesFalse() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser)); // logged-in user
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser2)); // user being updated
        given(mockUser.getReadOnly()).willReturn(false);
        given(authService.hasAdminPrivileges(mockUser.getRole())).willReturn(false);

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.updateUser(userId, request)
        );

        assertEquals("You don't have permission to edit this user.", ex.getMessage());
    }

    @Test
    @DisplayName("should return true when hasAdminPrivileges returns true")
    void authoriseAdminUser_shouldReturnFalse_whenHasAdminPrivilegesTrue() {
        given(mockUser.getRole()).willReturn(Role.systemadmin);
        given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser)); // logged-in user
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser2)); // user being updated
        given(mockUser.getReadOnly()).willReturn(false);
        given(authService.hasAdminPrivileges(Role.systemadmin)).willReturn(true);

        // Path should conclude successfully
        assertDoesNotThrow(() -> userService.updateUser(userId, request));
    }

    @Test
    @DisplayName("should set user status to deleted, update audit fields and save user when logged-in"
        + " user is authorised")
    void deleteUser_shouldPerformDeleteActions_whenLoggedInUserAuthorised() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser)); // logged-in user
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser)); // user being deleted
        given(mockUser.getReadOnly()).willReturn(false);
        given(authService.hasAdminPrivileges(mockUser.getRole())).willReturn(true);

        userService.deleteUser(userId);

        assertEquals(testUser.getStatus(), Status.deleted);
        assertEquals(testUser.getUpdatedBy(), mockUser);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("should throw InvalidRequestException when logged-in user not authorised")
    void deleteUser_shouldThrowException_whenLoggedInUserNotAuthorised() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser)); // logged-in user
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser2)); // user being deleted
        given(mockUser.getReadOnly()).willReturn(true); // authoriseAdminUser() returns false

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.deleteUser(userId)
        );

        assertEquals("You don't have permission to delete this user.", ex.getMessage());
    }

    @Test
    @DisplayName("should return false when given user's role is not user")
    void isCandidate_shouldReturnFalse_whenUserRoleIsNotUser() {
        testUser.setRole(Role.admin);

        assertFalse(userService.isCandidate(testUser));
    }

    @Test
    @DisplayName("should return true when given user's role is user")
    void isCandidate_shouldReturnTrue_whenUserRoleIsUser() {
        testUser.setRole(Role.user);

        assertTrue(userService.isCandidate(testUser));
    }

    @Test
    @DisplayName("should throw InvalidCredentialsException when user attempting login with "
        + "non-unique email")
    void login_shouldThrowInvalidCredentialsException_whenLoginAttemptedWithNonUniqueEmail() {
        given(userRepository.findByEmailIgnoreCase(loginUsername))
            .willThrow(new IncorrectResultSizeDataAccessException(1));

        InvalidCredentialsException ex = assertThrows(
            InvalidCredentialsException.class,
            () -> userService.login(loginRequest)
        );

        assertEquals(
            "Sorry, that email is not unique. Log in with your username.",
            ex.getMessage()
        );
    }

    @Test
    @DisplayName("should throw InvalidCredentialsException if user is no longer active")
    void login_shouldThrowInvalidCredentialsException_ifUserIsNotActive() {
        given(userRepository.findByEmailIgnoreCase(loginUsername)).willReturn(mockUser);
        given(mockUser.getUsername()).willReturn(loginUsername);
        given(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .willReturn(mockAuth);
        given(userRepository.findByUsernameIgnoreCase(loginUsername)).willReturn(mockUser);
        given(mockUser.getStatus()).willReturn(Status.inactive);

        InvalidCredentialsException ex = assertThrows(
            InvalidCredentialsException.class,
            () -> userService.login(loginRequest)
        );

        assertEquals(
            "Sorry, it looks like that account is no longer active.",
            ex.getMessage()
        );
    }

    @Test
    @DisplayName("should set last login, save user, set auth, generate and return a token when "
        + "credentials valid")
    void login_shouldPerformActions_whenLoginCredentialsValid() {
        given(userRepository.findByEmailIgnoreCase(loginUsername)).willReturn(mockUser);
        given(mockUser.getUsername()).willReturn(loginUsername);
        given(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .willReturn(mockAuth);
        given(userRepository.findByUsernameIgnoreCase(loginUsername)).willReturn(mockUser);
        given(mockUser.getStatus()).willReturn(Status.active);
        given(tokenProvider.generateToken(any())).willReturn("mock-jwt");
        given(userRepository.save(mockUser)).willReturn(mockUser);

        JwtAuthenticationResponse response =
            assertDoesNotThrow(() -> userService.login(loginRequest));

        assertEquals(mockAuth, SecurityContextHolder.getContext().getAuthentication());
        verify(mockUser).setLastLogin(any(OffsetDateTime.class));
        verify(userRepository).save(mockUser);
        verify(tokenProvider).generateToken(mockAuth);
        assertEquals("mock-jwt", response.getAccessToken());
        assertEquals(mockUser, response.getUser());
    }

    @Test
    @DisplayName("should throw AccountLockedException with correct message when account is locked")
    void login_shouldThrowAccountLockedException_whenAccountLocked() {
        given(userRepository.findByEmailIgnoreCase(loginUsername)).willReturn(mockUser);
        given(mockUser.getUsername()).willReturn(loginUsername);
        given(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .willThrow(new LockedException("Generic message"));

        AccountLockedException ex = assertThrows(
            AccountLockedException.class,
            () -> userService.login(loginRequest)
        );

        assertEquals("Account locked", ex.getMessage());
    }

    @Test
    @DisplayName("should throw InvalidCredentialsException with correct message when credentials bad")
    void login_shouldThrowInvalidCredentialsException_whenBadCredentials() {
        given(userRepository.findByEmailIgnoreCase(loginUsername)).willReturn(mockUser);
        given(mockUser.getUsername()).willReturn(loginUsername);
        given(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .willThrow(new BadCredentialsException("Generic message"));

        InvalidCredentialsException ex = assertThrows(
            InvalidCredentialsException.class,
            () -> userService.login(loginRequest)
        );

        assertEquals("Invalid credentials for user", ex.getMessage());
    }

    @Test
    @DisplayName("should set authentication context to null")
    void logout_shouldSetAuthenticationContextToNull() {
        SecurityContextHolder.getContext().setAuthentication(mockAuth);

        userService.logout();

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("should return null when none found")
    void getLoggedInUser_shouldReturnNull_whenNoneRetrieved() {
        given(authService.getLoggedInUser()).willReturn(Optional.empty());

        assertNull(userService.getLoggedInUser());
    }

    @Test
    @DisplayName("should return user when retrieved")
    void getLoggedInUser_shouldReturnUser_whenRetrieved() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser));
        given(mockUser.getId()).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        User result = userService.getLoggedInUser();
        assertEquals(mockUser, result);
    }

    @Test
    @DisplayName("should return partner when user retrieved")
    void getLoggedInPartner_shouldReturnPartner_whenUserRetrieved() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(mockUser));
        given(mockUser.getId()).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(mockUser.getPartner()).willReturn(mockPartnerImpl);

        Partner result = userService.getLoggedInPartner();
        assertEquals(mockPartnerImpl, result);
    }

    @Test
    @DisplayName("should return null when no logged-in user retrieved")
    void getLoggedInUser_shouldReturnNull_whenNoLoggedInUserRetrieved() {
        given(authService.getLoggedInUser()).willReturn(Optional.empty());

        assertNull(userService.getLoggedInPartner());
    }

    @Test
    @DisplayName("should call findByUsernameAndRole() with correct arguments")
    void getSystemAdminUser_shouldCallFindByUsernameAndRole_withCorrectArguments() {
        userService.getSystemAdminUser();

        verify(userRepository).findByUsernameAndRole(
            SystemAdminConfiguration.SYSTEM_ADMIN_NAME,
            Role.systemadmin
        );
    }

    @Test
    @DisplayName("should return result from find by username and role")
    void getSystemAdminUser_shouldReturnResultFromFindByUsernameAndRole() {
        given(userRepository.findByUsernameAndRole(any(), any())).willReturn(mockUser);

        User result = userService.getSystemAdminUser();

        Assertions.assertSame(mockUser, result);
    }

    @Test
    @DisplayName("should set token, issued date and send verification email when user retrieved")
    void sendVerifyEmailRequest_shouldSendVerificationEmail_ifUserRetrieved() {
        given(userRepository.findByEmailIgnoreCase(sendVerifyEmailRequest.getEmail()))
            .willReturn(mockUser);

        userService.sendVerifyEmailRequest(sendVerifyEmailRequest);

        verify(mockUser).setEmailVerificationToken(anyString());
        verify(mockUser).setEmailVerificationTokenIssuedDate(any(OffsetDateTime.class));
        verify(userRepository).save(mockUser);
        verify(emailHelper).sendVerificationEmail(mockUser);
    }

    @Test
    @DisplayName("should do nothing when user not retrieved")
    void sendVerifyEmailRequest_shouldDoNothingWhenUserNotRetrieved() {
        given(userRepository.findByEmailIgnoreCase(sendVerifyEmailRequest.getEmail()))
            .willReturn(null);

        userService.sendVerifyEmailRequest(sendVerifyEmailRequest);

        verify(mockUser, never()).setEmailVerificationToken(anyString());
        verify(mockUser, never()).setEmailVerificationTokenIssuedDate(any(OffsetDateTime.class));
        verify(userRepository, never()).save(mockUser);
        verify(emailHelper, never()).sendVerificationEmail(mockUser);
    }

    @Test
    @DisplayName("should throw ServiceException when token invalid")
    void verifyEmail_shouldThrowServiceException_whenTokenInvalid() {
        given(userRepository.findByEmailVerificationToken(verifyEmailRequest.getToken()))
            .willReturn(null);

        assertThrows(ServiceException.class,
            () -> userService.verifyEmail(verifyEmailRequest));
    }

    @Test
    @DisplayName("should set email verified to true, save user and send complete email when token valid")
    void verifyEmail_shouldSucceed_whenTokenValid() {
        given(userRepository.findByEmailVerificationToken(verifyEmailRequest.getToken()))
            .willReturn(mockUser);
        given(mockUser.getEmailVerificationTokenIssuedDate()).willReturn(OffsetDateTime.now());

        userService.verifyEmail(verifyEmailRequest);

        verify(mockUser).setEmailVerified(eq(true));
        verify(userRepository).save(mockUser);
        verify(emailHelper).sendCompleteVerificationEmail(mockUser, true);
    }

    @Test
    @DisplayName("should throw exception when token expired")
    void verifyEmail_shouldThrowException_whenTokenExpired() {
        given(userRepository.findByEmailVerificationToken(verifyEmailRequest.getToken()))
            .willReturn(mockUser);
        given(mockUser.getEmailVerificationTokenIssuedDate())
            .willReturn(OffsetDateTime.now().minus(Period.ofDays(7)));

        assertThrows(ServiceException.class,
            () -> userService.verifyEmail(verifyEmailRequest));
    }

}
