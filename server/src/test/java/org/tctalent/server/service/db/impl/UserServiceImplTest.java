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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.tctalent.server.exception.ExpiredTokenException;
import org.tctalent.server.exception.InvalidCredentialsException;
import org.tctalent.server.exception.InvalidPasswordTokenException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.PasswordMatchException;
import org.tctalent.server.exception.ServiceException;
import org.tctalent.server.exception.UsernameTakenException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.repository.db.UserSpecification;
import org.tctalent.server.request.LoginRequest;
import org.tctalent.server.request.user.CheckPasswordResetTokenRequest;
import org.tctalent.server.request.user.ResetPasswordRequest;
import org.tctalent.server.request.user.SearchUserRequest;
import org.tctalent.server.request.user.SendResetPasswordEmailRequest;
import org.tctalent.server.request.user.UpdateUserPasswordRequest;
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
    private List<User> userList;
    private Page<User> userPage;
    private long userId;
    private User expectedUser;
    private UpdateUserRequest updateUserRequest;
    private User adminUser;
    private User systemAdminUser;
    private User limitedUser;
    private User candidateUser;
    private Partner adminUserPartner;
    private Long approverId;
    private Long partnerId;
    private List<Country> sourceCountryList;
    private LoginRequest loginRequest;
    private SendVerifyEmailRequest sendVerifyEmailRequest;
    private VerifyEmailRequest verifyEmailRequest;
    private UpdateUserPasswordRequest updateUserPasswordRequest;
    private String encryptedPassword;
    private SendResetPasswordEmailRequest sendResetPasswordEmailRequest;
    private ResetPasswordRequest resetPasswordRequest;
    private OffsetDateTime offsetDateTime;
    private CheckPasswordResetTokenRequest checkPasswordResetTokenRequest;

    @Mock private UserRepository userRepository;
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
    @Mock private CandidateRepository candidateRepository;

    @Captor ArgumentCaptor<User> userCaptor;

    @InjectMocks
    @Spy
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        sort = Sort.unsorted();
        userId = 11L;
        sourceCountryList = List.of(mockCountry, mockCountry2);
        CreateUpdateUserTestData testData = AdminApiTestUtil.createUpdateUserRequestAndExpectedUser();
        expectedUser = testData.expectedUser();
        updateUserRequest = testData.request();
        adminUser = AdminApiTestUtil.getAdminUser();
        systemAdminUser = AdminApiTestUtil.getSystemAdminUser();
        candidateUser = AdminApiTestUtil.getCandidateUser();
        limitedUser = AdminApiTestUtil.getLimitedUser();
        adminUserPartner = adminUser.getPartner();
        pageRequest = PageRequest.of(0, 10, sort);
        userList = List.of(limitedUser, adminUser);
        userPage = new PageImpl<>(userList);
        approverId = 1L;
        partnerId = 1L;
        loginRequest = new LoginRequest();
        loginRequest.setUsername(adminUser.getEmail()); // Simulate email used for login
        loginRequest.setPassword("password");
        sendVerifyEmailRequest = new SendVerifyEmailRequest();
        sendVerifyEmailRequest.setEmail(updateUserRequest.getEmail());
        verifyEmailRequest = new VerifyEmailRequest();
        verifyEmailRequest.setToken("fakeToken");
        updateUserPasswordRequest = new UpdateUserPasswordRequest();
        updateUserPasswordRequest.setPassword("password");
        updateUserPasswordRequest.setPasswordConfirmation("password");
        encryptedPassword = "lkjhdsfaioy87";
        sendResetPasswordEmailRequest = new SendResetPasswordEmailRequest();
        sendResetPasswordEmailRequest.setEmail("person@email.com");
        resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setPassword("password");
        resetPasswordRequest.setPasswordConfirmation("password");
        resetPasswordRequest.setToken("token");
        offsetDateTime = OffsetDateTime.parse("2023-01-01T10:00:00+00:00");
        checkPasswordResetTokenRequest = new CheckPasswordResetTokenRequest();
        checkPasswordResetTokenRequest.setToken("token");
    }

    @Test
    @DisplayName("should return user")
    void findByUsernameAndRole_shouldReturnUser() {
        given(userRepository.findByUsernameAndRole("alice", Role.user))
            .willReturn(adminUser);

        User result = userService.findByUsernameAndRole("alice", Role.user);

        assertEquals(adminUser, result);
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
                .willReturn(List.of(limitedUser, adminUser));

            List<User> results = userService.search(searchUserRequest);

            assertEquals(List.of(limitedUser, adminUser), results);
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
        given(userRepository.findById(userId)).willReturn(Optional.of(adminUser));

        User result = userService.getUser(userId);

        assertEquals(adminUser, result);
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
        updateUserRequest.setSourceCountries(sourceCountryList);

        List<Country> allCountriesList = Arrays.asList(mockCountry, mockCountry2);
        adminUser.setSourceCountries(Collections.emptySet());
        given(countryRepository.findAll()).willReturn(allCountriesList);

        Set<Country> result = userService.getDefaultSourceCountries(adminUser); // When

        assertEquals(2, result.size());
        assertTrue(result.contains(mockCountry));
        assertTrue(result.contains(mockCountry2));
    }

    @Test
    @DisplayName("should return user source countries when they have some")
    public void getDefaultSourceCountries_shouldReturnUserSourceCountries() {
        Set<Country> userSourceCountries = Set.of(mockCountry, mockCountry2);
        adminUser.setSourceCountries(userSourceCountries);

        Set<Country> result = userService.getDefaultSourceCountries(adminUser); // When

        assertEquals(userSourceCountries, result);
    }

    @Test
    @DisplayName("should create and return new admin user matching valid request")
    void createUser_withValidRequest_shouldCreateAndReturnExpectedUser() {
        // Testing Approver path
        updateUserRequest.setApproverId(approverId);
        expectedUser.setApprover(systemAdminUser);

        // Testing Partner path
        updateUserRequest.setPartnerId(partnerId);
        expectedUser.setPartner(mockPartnerImpl);

        // Source Country path
        updateUserRequest.setSourceCountries(sourceCountryList);
        expectedUser.setSourceCountries(new HashSet<>(sourceCountryList));

        // Audit fields
        expectedUser.setCreatedBy(systemAdminUser);
        expectedUser.setUpdatedBy(systemAdminUser);

        given(userRepository.findByUsernameIgnoreCase(updateUserRequest.getUsername()))
            .willReturn(null);
        given(userRepository.findByEmailIgnoreCase(updateUserRequest.getEmail()))
            .willReturn(null);
        given(partnerService.getPartner(updateUserRequest.getPartnerId()))
            .willReturn(mockPartnerImpl);
        doReturn(systemAdminUser).when(userService).getUser(updateUserRequest.getApproverId());
        given(passwordHelper.validateAndEncodePassword(updateUserRequest.getPassword()))
            .willReturn(updateUserRequest.getPassword());

        userService.createUser(updateUserRequest, systemAdminUser); // When

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
        given(userRepository.findByUsernameIgnoreCase(updateUserRequest.getUsername()))
            .willReturn(adminUser); // Returns a user with same username

        assertThrows(UsernameTakenException.class, () -> userService.createUser(updateUserRequest,
            limitedUser));
    }

    @Test
    @DisplayName("should throw UsernameTakenException when email taken")
    void populateUserFields_shouldThrowUsernameTakenException_whenEmailTaken() {
        given(userRepository.findByUsernameIgnoreCase(updateUserRequest.getUsername()))
            .willReturn(null);
        given(userRepository.findByEmailIgnoreCase(updateUserRequest.getEmail()))
            .willReturn(adminUser); // Returns a user with same email

        assertThrows(UsernameTakenException.class, () -> userService.createUser(updateUserRequest,
            limitedUser));
    }

    @Test
    @DisplayName("should reassign partner when requested and logged in user is System Admin")
    void reassignPartnerIfNeeded_shouldReassignPartner_whenUserIsSystemAdmin() {
        // Following update() path to reach reassignPartnerIfNeeded()
        given(authService.getLoggedInUser()).willReturn(Optional.of(systemAdminUser));
        // Return test user who has a different partner than mockPartnerImpl:
        given(userRepository.findById(userId)).willReturn(Optional.of(adminUser));
        given(authService.hasAdminPrivileges(systemAdminUser.getRole())).willReturn(true);
        // Mock update request has partner ID that will return mockPartnerImpl:
        given(partnerService.getPartner(updateUserRequest.getPartnerId())).willReturn(mockPartnerImpl);
        updateUserRequest.setApproverId(null); // We're not testing the approver path

        User result = userService.updateUser(userId, updateUserRequest); // When

        verify(userRepository).save(adminUser);
        assertEquals(result.getPartner(), mockPartnerImpl); // Request partner assigned
    }

    @Test
    @DisplayName("should throw InvalidRequestException when logged in user is regular admin and "
        + "tries to reassign partner")
    void reassignPartnerIfNeeded_shouldThrowInvalidRequestException_whenUserIsAdmin() {
        // Following update() path to reach reassignPartnerIfNeeded()
        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser));
        given(userRepository.findById(userId)).willReturn(Optional.of(limitedUser));
        given(authService.hasAdminPrivileges(adminUser.getRole())).willReturn(true);

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.updateUser(userId, updateUserRequest)
        );

        assertEquals("You don't have permission to change a partner.", ex.getMessage());
    }

    @Test
    @DisplayName("should throw InvalidRequestException when no partnerId in request")
    void reassignPartnerIfNeeded_shouldThrowInvalidRequestException_whenNoPartnerId() {
        updateUserRequest.setPartnerId(null);
        // The below path would reassign successfully if not for the null partnerId
        given(authService.getLoggedInUser()).willReturn(Optional.of(systemAdminUser));
        given(userRepository.findById(userId)).willReturn(Optional.of(adminUser));
        given(authService.hasAdminPrivileges(systemAdminUser.getRole())).willReturn(true);

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.updateUser(userId, updateUserRequest)
        );

        assertEquals("A partner must be specified.", ex.getMessage());
    }

    @Test
    @DisplayName("should set new approver if different and logged in user is System Admin")
    void updateApproverIfNeeded_shouldSetNewApproverIfDifferentAndLoggedInUserIsSystemAdmin() {
        setupPathToUpdateApproverIfNeeded(systemAdminUser);
        given(authService.hasAdminPrivileges(systemAdminUser.getRole())).willReturn(true);
        given(userRepository.findById(updateUserRequest.getApproverId()))
            .willReturn(Optional.of(systemAdminUser));

        User result = userService.updateUser(userId, updateUserRequest);

        verify(userRepository).save(limitedUser);
        assertEquals(result.getApprover(), systemAdminUser);
    }

    @Test
    @DisplayName("should throw InvalidRequestException when approver update attempted and logged in "
        + "user is regular admin")
    void updateApproverIfNeeded_shouldThrowInvalidRequestException_whenUpdatingUserIsAdmin() {
        setupPathToUpdateApproverIfNeeded(adminUser);
        limitedUser.setPartner(null); // Handles partner assign path for regular admin (no update)
        given(authService.hasAdminPrivileges(adminUser.getRole())).willReturn(true);

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.updateUser(userId, updateUserRequest)
        );

        assertEquals("You don't have permission to assign an approver.", ex.getMessage());
    }

    private void setupPathToUpdateApproverIfNeeded(User updatingUser) {
        updateUserRequest.setApproverId(approverId);
        expectedUser.setApprover(systemAdminUser);
        given(authService.getLoggedInUser()).willReturn(Optional.of(updatingUser));
        given(userRepository.findById(userId)).willReturn(Optional.of(limitedUser)); // User being updated
    }

    @Test
    @DisplayName("should throw InvalidRequestException when Admin tries to create System Admin")
    void addRoleIfValid_shouldThrowInvalidRequestException_whenAdminTriesToCreateSystemAdmin() {
        updateUserRequest.setRole(Role.systemadmin);

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.createUser(updateUserRequest, adminUser)
        );

        assertEquals("You don't have permission to save this role type.", ex.getMessage());
    }

    @Test
    @DisplayName("should allow System Admin to create System Admin")
    void addRoleIfValid_shouldAllowSystemAdminToCreateSystemAdmin() {
        updateUserRequest.setRole(Role.systemadmin);

        userService.createUser(updateUserRequest, systemAdminUser);

        verify(userRepository).save(userCaptor.capture());
        assertEquals(userCaptor.getValue().getRole(), Role.systemadmin);
    }

    @Test
    @DisplayName("should allow creating user with no source country restrictions (empty) to add "
        + "any source country to new user")
    void addSourceCountriesIfValid_shouldAllowUnrestrictedAdminToCreateUserWithAnySourceCountry() {
        setupPathToAddSourceCountriesIfValid();
        adminUser.setSourceCountries(Collections.emptySet()); // No restrictions on creating user

        userService.createUser(updateUserRequest, adminUser); // When

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getSourceCountries(),
            containsInAnyOrder(sourceCountryList.toArray()));
    }

    @Test
    @DisplayName("should not allow restricted creating user to assign any source country that they "
        + "can't themselves access")
    void addSourceCountriesIfValid_shouldNotAllowRestrictedAdminToCreateUserWithAnySourceCountry() {
        setupPathToAddSourceCountriesIfValid();
        adminUser.setSourceCountries(Set.of(mockCountry)); // Restricted to one source country

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.createUser(updateUserRequest, adminUser)
        );

        assertEquals("You don't have permission to add this country.", ex.getMessage());
    }

    @Test
    @DisplayName("should set new user source countries the same as restricted partner admin if "
        + "none specified in request")
    void addSourceCountriesIfValid_shouldSetNewUserSourceCountriesSameAsCreatingUser_whenEmptyRequest() {
        setupPathToAddSourceCountriesIfValid();
        updateUserRequest.setSourceCountries(null);
        adminUser.setSourceCountries(new HashSet<>(sourceCountryList));

        userService.createUser(updateUserRequest, adminUser);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getSourceCountries(),
            containsInAnyOrder(sourceCountryList.toArray()));
    }

    private void setupPathToAddSourceCountriesIfValid() {
        updateUserRequest.setSourceCountries(sourceCountryList);
        updateUserRequest.setRole(Role.partneradmin);
        adminUser.setRole(Role.partneradmin);
    }

    @Test
    @DisplayName("should throw InvalidRequestException when read-only user tries to create a user")
    void createUser_shouldThrowInvalidRequestException_whenReadonlyUserTriesToCreateUser() {
        adminUser.setReadOnly(true);
        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser));

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.createUser(updateUserRequest)
        );

        assertEquals("You don't have permission to create a user.", ex.getMessage());
    }

    @Test
    @DisplayName("should throw InvalidRequestException when creating user has no admin privileges")
    void createUser_shouldThrowInvalidRequestException_whenCreatingUserHasNoPrivileges() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser));
        given(authService.hasAdminPrivileges(adminUser.getRole())).willReturn(false);

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.createUser(updateUserRequest)
        );

        assertEquals("You don't have permission to create a user.", ex.getMessage());
    }

    @Test
    @DisplayName("should reset email verification fields when new email supplied")
    void checkAndResetEmailVerification_shouldResetEmailVerification_whenNewEmailIsSupplied() {
        setupPathToCheckAndResetEmailVerification();

        userService.updateUser(userId, updateUserRequest);

        assertFalse(adminUser.getEmailVerified());
        assertNull(adminUser.getEmailVerificationToken());
        assertNull(adminUser.getEmailVerificationTokenIssuedDate());
    }

    @Test
    @DisplayName("should do nothing when request email same as old")
    void checkAndResetEmailVerification_shouldDoNothing_whenRequestEmailIsSameAsOld() {
        setupPathToCheckAndResetEmailVerification();
        adminUser.setEmail(updateUserRequest.getEmail()); // Same email

        userService.updateUser(userId, updateUserRequest);

        assertFalse(adminUser.getEmailVerified());
        assertNull(adminUser.getEmailVerificationToken());
        assertNull(adminUser.getEmailVerificationTokenIssuedDate());
    }

    @Test
    @DisplayName("should do nothing when no email in request")
    void checkAndResetEmailVerification_shouldDoNothing_whenNoEmailInRequest() {
        setupPathToCheckAndResetEmailVerification();
        updateUserRequest.setEmail(null);

        userService.updateUser(userId, updateUserRequest);

        assertFalse(adminUser.getEmailVerified());
        assertNull(adminUser.getEmailVerificationToken());
        assertNull(adminUser.getEmailVerificationTokenIssuedDate());
    }

    private void setupPathToCheckAndResetEmailVerification() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(systemAdminUser)); // logged-in user
        given(userRepository.findById(userId)).willReturn(Optional.of(adminUser)); // user being updated
        // Logged-in user has required privileges:
        given(authService.hasAdminPrivileges(systemAdminUser.getRole())).willReturn(true);
    }

    @Test
    @DisplayName("should return false and cause caller updateUser() to throw InvalidRequestException"
        + " when logged in user is read-only")
    void authoriseAdminUser_shouldReturnFalse_whenLoggedInUserIsReadonly() {
        adminUser.setReadOnly(true);
        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser)); // logged-in user
        given(userRepository.findById(userId)).willReturn(Optional.of(systemAdminUser)); // user being updated

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.updateUser(userId, updateUserRequest)
        );

        assertEquals("You don't have permission to edit this user.", ex.getMessage());
    }

    @Test
    @DisplayName("should return false and cause caller updateUser() to throw InvalidRequestException"
        + " when hasAdminPrivileges returns false")
    void authoriseAdminUser_shouldReturnFalse_whenHasAdminPrivilegesFalse() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser)); // logged-in user
        given(userRepository.findById(userId)).willReturn(Optional.of(systemAdminUser)); // user being updated
        given(authService.hasAdminPrivileges(adminUser.getRole())).willReturn(false);

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.updateUser(userId, updateUserRequest)
        );

        assertEquals("You don't have permission to edit this user.", ex.getMessage());
    }

    @Test
    @DisplayName("should return true when hasAdminPrivileges returns true")
    void authoriseAdminUser_shouldReturnFalse_whenHasAdminPrivilegesTrue() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(systemAdminUser)); // logged-in user
        given(userRepository.findById(userId)).willReturn(Optional.of(adminUser)); // user being updated
        given(authService.hasAdminPrivileges(systemAdminUser.getRole())).willReturn(true);

        // Path should conclude successfully
        assertDoesNotThrow(() -> userService.updateUser(userId, updateUserRequest));
    }

    @Test
    @DisplayName("should set user status to deleted, update audit fields and save user when logged-in"
        + " user is authorised")
    void deleteUser_shouldPerformDeleteActions_whenLoggedInUserAuthorised() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(systemAdminUser)); // logged-in user
        given(userRepository.findById(userId)).willReturn(Optional.of(adminUser)); // user being deleted
        given(authService.hasAdminPrivileges(systemAdminUser.getRole())).willReturn(true);

        userService.deleteUser(userId);

        assertEquals(adminUser.getStatus(), Status.deleted);
        assertEquals(adminUser.getUpdatedBy(), systemAdminUser);
        verify(userRepository).save(adminUser);
    }

    @Test
    @DisplayName("should throw InvalidRequestException when logged-in user not authorised")
    void deleteUser_shouldThrowException_whenLoggedInUserNotAuthorised() {
        adminUser.setReadOnly(true); // authoriseAdminUser() will return false
        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser)); // logged-in user
        given(userRepository.findById(userId)).willReturn(Optional.of(systemAdminUser)); // user being deleted

        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> userService.deleteUser(userId)
        );

        assertEquals("You don't have permission to delete this user.", ex.getMessage());
    }

    @Test
    @DisplayName("should return false when given user's role is not user")
    void isCandidate_shouldReturnFalse_whenUserRoleIsNotUser() {
        adminUser.setRole(Role.admin);

        assertFalse(userService.isCandidate(adminUser));
    }

    @Test
    @DisplayName("should return true when given user's role is user")
    void isCandidate_shouldReturnTrue_whenUserRoleIsUser() {
        adminUser.setRole(Role.user);

        assertTrue(userService.isCandidate(adminUser));
    }

    @Test
    @DisplayName("should throw InvalidCredentialsException when user attempting login with "
        + "non-unique email")
    void login_shouldThrowInvalidCredentialsException_whenLoginAttemptedWithNonUniqueEmail() {
        given(userRepository.findByEmailIgnoreCase(loginRequest.getUsername()))
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
        adminUser.setStatus(Status.inactive);
        given(userRepository.findByEmailIgnoreCase(loginRequest.getUsername())).willReturn(adminUser);
        given(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .willReturn(mockAuth);
        given(userRepository.findByUsernameIgnoreCase(adminUser.getUsername())).willReturn(adminUser);

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
        adminUser.setLastLogin(offsetDateTime);
        given(userRepository.findByEmailIgnoreCase(loginRequest.getUsername())).willReturn(adminUser);
        given(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .willReturn(mockAuth);
        given(userRepository.findByUsernameIgnoreCase(adminUser.getUsername())).willReturn(adminUser);
        given(tokenProvider.generateToken(any())).willReturn("mock-jwt");
        given(userRepository.save(adminUser)).willReturn(adminUser);

        JwtAuthenticationResponse response =
            assertDoesNotThrow(() -> userService.login(loginRequest));

        assertEquals(mockAuth, SecurityContextHolder.getContext().getAuthentication());
        assertNotEquals(adminUser.getLastLogin(), offsetDateTime);
        verify(userRepository).save(adminUser);
        verify(tokenProvider).generateToken(mockAuth);
        assertEquals("mock-jwt", response.getAccessToken());
        assertEquals(adminUser, response.getUser());
    }

    @Test
    @DisplayName("should throw AccountLockedException with correct message when account is locked")
    void login_shouldThrowAccountLockedException_whenAccountLocked() {
        given(userRepository.findByEmailIgnoreCase(loginRequest.getUsername())).willReturn(adminUser);
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
        given(userRepository.findByEmailIgnoreCase(loginRequest.getUsername())).willReturn(adminUser);
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
        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser));
        given(userRepository.findById(adminUser.getId())).willReturn(Optional.of(adminUser));

        User result = userService.getLoggedInUser();
        assertEquals(adminUser, result);
    }

    @Test
    @DisplayName("should return partner when user retrieved")
    void getLoggedInPartner_shouldReturnPartner_whenUserRetrieved() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser));
        given(userRepository.findById(adminUser.getId())).willReturn(Optional.of(adminUser));

        Partner result = userService.getLoggedInPartner();
        assertEquals(adminUserPartner, result);
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
        given(userRepository.findByUsernameAndRole(any(), any())).willReturn(systemAdminUser);

        User result = userService.getSystemAdminUser();

        Assertions.assertSame(systemAdminUser, result);
    }

    @Test
    @DisplayName("should set token, issued date and send verification email when user retrieved")
    void sendVerifyEmailRequest_shouldSendVerificationEmail_ifUserRetrieved() {
        given(userRepository.findByEmailIgnoreCase(sendVerifyEmailRequest.getEmail()))
            .willReturn(adminUser);

        userService.sendVerifyEmailRequest(sendVerifyEmailRequest);

        assertNotNull(adminUser.getEmailVerificationToken());
        assertNotNull(adminUser.getEmailVerificationToken());
        verify(userRepository).save(adminUser);
        verify(emailHelper).sendVerificationEmail(adminUser);
    }

    @Test
    @DisplayName("should do nothing when user not retrieved")
    void sendVerifyEmailRequest_shouldDoNothingWhenUserNotRetrieved() {
        given(userRepository.findByEmailIgnoreCase(sendVerifyEmailRequest.getEmail()))
            .willReturn(null);

        userService.sendVerifyEmailRequest(sendVerifyEmailRequest);

        assertNull(adminUser.getEmailVerificationToken());
        assertNull(adminUser.getEmailVerificationTokenIssuedDate());
        verify(userRepository, never()).save(any(User.class));
        verify(emailHelper, never()).sendVerificationEmail(any(User.class));
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
        adminUser.setEmailVerificationTokenIssuedDate(OffsetDateTime.now());
        adminUser.setEmailVerified(false);
        given(userRepository.findByEmailVerificationToken(verifyEmailRequest.getToken()))
            .willReturn(adminUser);

        userService.verifyEmail(verifyEmailRequest);

        assertTrue(adminUser.getEmailVerified());
        verify(userRepository).save(adminUser);
        verify(emailHelper).sendCompleteVerificationEmail(adminUser, true);
    }

    @Test
    @DisplayName("should throw exception when token expired")
    void verifyEmail_shouldThrowException_whenTokenExpired() {
        adminUser.setEmailVerificationTokenIssuedDate(OffsetDateTime.now().minus(Period.ofDays(7)));
        given(userRepository.findByEmailVerificationToken(verifyEmailRequest.getToken()))
            .willReturn(adminUser);

        assertThrows(ServiceException.class,
            () -> userService.verifyEmail(verifyEmailRequest));
    }

    @Test
    @DisplayName("should throw exception when confirmation doesn't match")
    void updatePassword_shouldThrowException_whenConfirmationDoesNotMatch() {
        updateUserPasswordRequest.setPasswordConfirmation("passwordz");

        assertThrows(PasswordMatchException.class,
            () -> userService.updatePassword(updateUserPasswordRequest));
    }

    @Test
    @DisplayName("should throw exception user not logged in")
    void updatePassword_shouldThrowException_whenUserNotLoggedIn() {
        given(authService.getLoggedInUser()).willReturn(Optional.empty());

        assertThrows(InvalidSessionException.class,
            () -> userService.updatePassword(updateUserPasswordRequest));
    }

    @Test
    @DisplayName("should set password to value returned by encryption helper method")
    void updatePassword_shouldSetPasswordToValueReturnedByEncryptionHelperMethod() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(candidateUser));
        given(passwordHelper.validateAndEncodePassword(anyString())).willReturn(encryptedPassword);

        userService.updatePassword(updateUserPasswordRequest);

        assertEquals(candidateUser.getPasswordEnc(), encryptedPassword);
        verify(userRepository).save(candidateUser);
    }

    @Test
    @DisplayName("should set change password on candidate to false if previously true and password "
        + "successfully changed")
    void updatePassword_shouldSetChangePasswordOnCandidateToFalseIfTrueAndPasswordChanged() {
        Candidate candidate = candidateUser.getCandidate();
        candidate.setChangePassword(true);
        given(authService.getLoggedInUser()).willReturn(Optional.of(candidateUser));
        given(passwordHelper.validateAndEncodePassword(anyString())).willReturn(encryptedPassword);
        given(candidateRepository.findById(candidate.getId()))
            .willReturn(Optional.of(candidate));

        userService.updatePassword(updateUserPasswordRequest);

        assertFalse(candidate.isChangePassword());
        verify(candidateRepository).save(candidate);
    }

    @Test
    @DisplayName("should throw exception if user not found")
    void updateUserPassword_shouldThrowException_whenUserNotFound() {
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class,
            () -> userService.updateUserPassword(userId, updateUserPasswordRequest));
    }

    @Test
    @DisplayName("should throw exception when confirmation doesn't match")
    void updateUserPassword_shouldThrowException_whenConfirmationDoesNotMatch() {
        updateUserPasswordRequest.setPasswordConfirmation("passwordz");
        given(userRepository.findById(anyLong())).willReturn(Optional.of(adminUser));
        given(authService.getLoggedInUser()).willReturn(Optional.of(systemAdminUser));
        given(authService.hasAdminPrivileges(systemAdminUser.getRole())).willReturn(true);

        assertThrows(PasswordMatchException.class,
            () -> userService.updateUserPassword(userId, updateUserPasswordRequest));
    }

    @Test
    @DisplayName("should use encrypted password and save user when passwords match and updating "
        + "user has admin privileges")
    void updateUserPassword_shouldUseEncryptedPasswordAndSaveUser() {
        given(userRepository.findById(anyLong())).willReturn(Optional.of(adminUser));
        given(authService.getLoggedInUser()).willReturn(Optional.of(systemAdminUser));
        given(authService.hasAdminPrivileges(systemAdminUser.getRole())).willReturn(true);
        given(passwordHelper.validateAndEncodePassword(anyString())).willReturn(encryptedPassword);

        userService.updateUserPassword(userId, updateUserPasswordRequest);

        assertEquals(adminUser.getPasswordEnc(), encryptedPassword);
        verify(userRepository).save(adminUser);
    }

    @Test
    @DisplayName("should throw exception when updating user does not have admin privileges")
    void updateUserPassword_shouldThrowException_whenUpdatingUserDoesNotHaveAdminPrivileges() {
        given(userRepository.findById(anyLong())).willReturn(Optional.of(adminUser));
        given(authService.getLoggedInUser()).willReturn(Optional.of(limitedUser));

        assertThrows(InvalidRequestException.class,
            () -> userService.updateUserPassword(userId, updateUserPasswordRequest));
    }

    @Test
    @DisplayName("should set reset token and issue date, save user and send email")
    void generateResetPasswordToken_shouldSetResetTokenIssuedDateAndSaveUser() {
        given(userRepository.findByEmailIgnoreCase(sendResetPasswordEmailRequest.getEmail()))
            .willReturn(adminUser);

        userService.generateResetPasswordToken(sendResetPasswordEmailRequest);

        assertNotNull(adminUser.getResetToken());
        assertNotNull(adminUser.getResetTokenIssuedDate());
        verify(userRepository).save(adminUser);
        verify(emailHelper).sendResetPasswordEmail(adminUser);
    }

    @Test
    @DisplayName("should throw exception when user not found")
    void checkResetToken_shouldThrowException_whenUserNotFound() {
        given(userRepository.findByResetToken(checkPasswordResetTokenRequest.getToken()))
            .willReturn(null);

        assertThrows(InvalidPasswordTokenException.class,
            () -> userService.checkResetToken(checkPasswordResetTokenRequest));
    }

    @Test
    @DisplayName("should throw exception when token expired")
    void checkResetToken_shouldThrowException_whenTokenExpired() {
        adminUser.setResetTokenIssuedDate(offsetDateTime);
        given(userRepository.findByResetToken(checkPasswordResetTokenRequest.getToken()))
            .willReturn(adminUser);

        assertThrows(ExpiredTokenException.class,
            () -> userService.checkResetToken(checkPasswordResetTokenRequest));
    }

    @Test
    @DisplayName("should throw exception when confirmation doesn't match")
    void resetPassword_shouldThrowException_whenConfirmationDoesNotMatch() {
        resetPasswordRequest.setPasswordConfirmation("different");

        assertThrows(PasswordMatchException.class,
            () -> userService.resetPassword(resetPasswordRequest));
    }

    @Test
    @DisplayName("should set password to encrypted version and updated date to current time, set "
        + "token fields to null and save user")
    void resetPassword_shouldSetEncryptedPasswordAndTokenFieldsAndSaveUser() {
        adminUser.setResetToken("token");
        adminUser.setResetTokenIssuedDate(offsetDateTime);
        adminUser.setPasswordUpdatedDate(offsetDateTime);
        given(userRepository.findByResetToken(resetPasswordRequest.getToken()))
            .willReturn(adminUser);
        given(passwordHelper.validateAndEncodePassword(anyString())).willReturn(encryptedPassword);

        userService.resetPassword(resetPasswordRequest);

        assertEquals(adminUser.getPasswordEnc(), encryptedPassword);
        assertNotEquals(adminUser.getPasswordUpdatedDate(), offsetDateTime);
        assertNull(adminUser.getResetTokenIssuedDate());
        assertNull(adminUser.getResetToken());
        verify(userRepository).save(adminUser);
    }

    @Test
    @DisplayName("should throw exception when user not found")
    void mfaReset_shouldThrowException_whenUserNotFound() {
        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser));
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class, () -> userService.mfaReset(userId));
    }

    @Test
    @DisplayName("should reset MFA and audit fields and save user")
    void mfaReset_shouldResetMfaAndAuditFieldsAndSaveUser() {
        systemAdminUser.setMfaSecret("secret");
        given(authService.getLoggedInUser()).willReturn(Optional.of(adminUser)); // Updating admin
        given(userRepository.findById(userId)).willReturn(Optional.of(systemAdminUser));
        given(authService.hasAdminPrivileges(adminUser.getRole())).willReturn(true);

        userService.mfaReset(userId);

        assertNull(systemAdminUser.getMfaSecret());
        assertEquals(systemAdminUser.getUpdatedBy(), adminUser);
        verify(userRepository).save(systemAdminUser);
    }

    @Test
    @DisplayName("should return results of repo method")
    void searchStaffNotUsingMfa_shouldReturnResultsOfRepoMethod() {
        given(userRepository.searchStaffNotUsingMfa()).willReturn(userList);

        List<User> result = userService.searchStaffNotUsingMfa();

        assertEquals(result, userList);
    }

    @Test
    @DisplayName("should send email alert when there are staff not using mfa")
    void checkMfaUsers_shouldSendEmailAlert_whenThereAreStaffNotUsingMfa() {
        given(userRepository.searchStaffNotUsingMfa()).willReturn(userList);

        userService.checkMfaUsers();

        verify(emailHelper).sendAlert(anyString());
    }

}
