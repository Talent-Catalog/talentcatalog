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

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.security.auth.login.AccountLockedException;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.configuration.SystemAdminConfiguration;
import org.tctalent.server.exception.EmailSendFailedException;
import org.tctalent.server.exception.ExpiredTokenException;
import org.tctalent.server.exception.InvalidCredentialsException;
import org.tctalent.server.exception.InvalidPasswordTokenException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.PasswordExpiredException;
import org.tctalent.server.exception.PasswordMatchException;
import org.tctalent.server.exception.ServiceException;
import org.tctalent.server.exception.UserDeactivatedException;
import org.tctalent.server.exception.UsernameTakenException;
import org.tctalent.server.logging.LogBuilder;
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
import org.tctalent.server.request.user.CheckPasswordResetTokenRequest;
import org.tctalent.server.request.user.ResetPasswordRequest;
import org.tctalent.server.request.user.SearchUserRequest;
import org.tctalent.server.request.user.SendResetPasswordEmailRequest;
import org.tctalent.server.request.user.UpdateUserPasswordRequest;
import org.tctalent.server.request.user.UpdateUserRequest;
import org.tctalent.server.response.JwtAuthenticationResponse;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.security.JwtTokenProvider;
import org.tctalent.server.security.PasswordHelper;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.service.db.email.EmailHelper;
import org.tctalent.server.util.qr.EncodedQrImage;


import org.tctalent.server.request.user.emailverify.SendVerifyEmailRequest;
import org.tctalent.server.request.user.emailverify.VerifyEmailRequest;
import org.tctalent.server.exception.InvalidEmailVerificationTokenException;
import org.tctalent.server.exception.ExpiredEmailTokenException;


@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CountryRepository countryRepository;
    private final PasswordHelper passwordHelper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuthService authService;
    private final EmailHelper emailHelper;
    private final PartnerService partnerService;

    @Value("${web.portal}")
    private String portalUrl;
    @Value("${web.admin}")
    private String adminUrl;

    //Multi factor authentication (MFA) is implemented using TOTP (Time based One Time Password)
    //tools
    @Autowired
    private SecretGenerator totpSecretGenerator;
    @Autowired
    private QrDataFactory totpQrDataFactory;
    @Autowired
    private QrGenerator totpQrGenerator;
    @Autowired
    private CodeVerifier totpVerifier;


    @Autowired
    public UserServiceImpl(UserRepository userRepository,
        CountryRepository countryRepository,
        PasswordHelper passwordHelper,
        AuthenticationManager authenticationManager,
        JwtTokenProvider tokenProvider,
        AuthService authService,
        EmailHelper emailHelper, PartnerService partnerService) {
        this.userRepository = userRepository;
        this.countryRepository = countryRepository;
        this.passwordHelper = passwordHelper;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.authService = authService;
        this.emailHelper = emailHelper;
        this.partnerService = partnerService;
    }

    @Override
    public User findByUsernameAndRole(String username, Role role) {
        return userRepository.findByUsernameAndRole(username, role);
    }

    @Override
    public List<User> search(SearchUserRequest request) {
        List<User> users = userRepository.findAll(
            UserSpecification.buildSearchQuery(request), request.getSort());
        return users;
    }

    @Override
    public Page<User> searchPaged(SearchUserRequest request) {
        Page<User> users = userRepository.findAll(
                UserSpecification.buildSearchQuery(request), request.getPageRequest());
        return users;
    }

    @Override
    public User getUser(long id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(User.class, id));
    }

    @Override
    public Set<Country> getDefaultSourceCountries(User user) {
        Set<Country> countries;
        if(CollectionUtils.isEmpty(user.getSourceCountries())){
            countries = new HashSet<>(countryRepository.findAll());
        } else {
            countries = user.getSourceCountries();
        }
        return countries;
    }

    @Override
    public User createUser(UpdateUserRequest request, @Nullable User creatingUser)
        throws UsernameTakenException {

        User user = new User();

        populateUserFields(user, request, creatingUser);

        /* Validate the password before account creation */
        String passwordEncrypted = passwordHelper.validateAndEncodePassword(request.getPassword());
        user.setPasswordEnc(passwordEncrypted);
        return this.userRepository.save(user);
    }

    private void populateUserFields(User user, UpdateUserRequest request, @Nullable User creatingUser) {
        //Check for changes to something existing which must be unique

        final String requestedUsername = request.getUsername();
        final String requestedEmail = request.getEmail();

        String currentUsername = user.getUsername();
        if (currentUsername == null || !currentUsername.equals(requestedUsername)) {
            User existing = userRepository.findByUsernameIgnoreCase(requestedUsername);
            if (existing != null) {
                throw new UsernameTakenException("username");
            }
        }
        user.setUsername(requestedUsername);

        String currentEmail = user.getEmail();
        if (currentEmail == null || !currentEmail.equals(requestedEmail)) {
            User existing = userRepository.findByEmailIgnoreCase(requestedEmail);
            if (existing != null) {
                throw new UsernameTakenException("email");
            }
        }
        user.setEmail(requestedEmail);

        //Possibly update the user's source partner
        Partner currentPartner = user.getPartner();
        Long currentPartnerId = currentPartner == null ? null : currentPartner.getId();
        Partner newSourcePartner = null;
        Long partnerId = request.getPartnerId();
        if (partnerId != null) {
            //Partner specified - is it changing the existing partner?
            if (!partnerId.equals(currentPartnerId)) {
                if (creatingUser != null && currentPartnerId != null
                    && creatingUser.getRole() != Role.systemadmin) {
                    //Only system admins can change partners
                    throw new InvalidRequestException("You don't have permission to change a partner.");
                } else {
                    //Changing partner -
                    //or setting partner for the first time (currentPartnerId = null)
                    newSourcePartner = partnerService.getPartner(partnerId);
                }
            }
        } else {
            //Throw an exception if no partner is specified
            throw new InvalidRequestException("A partner must be specified.");
        }
        //If we have a new source partner, update it.
        if (newSourcePartner != null) {
            user.setPartner((PartnerImpl) newSourcePartner);
        }

        //Possibly update the user's approver
        User currentApprover = user.getApprover();
        Long currentApproverId = currentApprover == null ? null : currentApprover.getId();
        User newApprover = null;
        Long approverId = request.getApproverId();
        if (approverId != null) {
            //Approver specified - is it a new one?
            if (!approverId.equals(currentApproverId)) {
                if (creatingUser != null && creatingUser.getRole() != Role.systemadmin) {
                    //Only system admins can change approver
                    throw new InvalidRequestException("You don't have permission to assign an approver.");
                } else {
                    //Changing approver
                    newApprover = getUser(approverId);
                }
            }
        } else {
            //If user does not already have an approver, assign one
            if (currentApprover == null && creatingUser != null) {
                newApprover = creatingUser.getApprover();
            }
        }
        //If we have a new approver, update it.
        if (newApprover != null) {
            user.setApprover(newApprover);
        }

        user.setReadOnly(request.getReadOnly());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setStatus(request.getStatus());
        user.setUsingMfa(request.getUsingMfa());
        user.setPurpose(request.getPurpose());
        user.setJobCreator(request.getJobCreator());

        if (creatingUser == null) {
            user.setRole(request.getRole());
        } else {
            addRoleIfValid(user, request.getRole(), creatingUser);

            //Validate source countries aren't restricted, and add to user.
            addSourceCountriesIfValid(user, request.getSourceCountries(), creatingUser);
        }
        user.setAuditFields(creatingUser == null ? user : creatingUser);
    }

    @Override
    @Transactional
    public User createUser(UpdateUserRequest request) throws UsernameTakenException, InvalidRequestException {
        User loggedInUser = fetchLoggedInUser();
        boolean authSuccess;

        if (loggedInUser.getReadOnly()) {
            authSuccess = false;
        } else {
            authSuccess = authService.hasAdminPrivileges(loggedInUser.getRole());
        }

        if (authSuccess) {
            return createUser(request, loggedInUser);
        } else {
            throw new InvalidRequestException("You don't have permission to create a user.");
        }

    }

    private void checkAndResetEmailVerification(User user, String newEmail) {
        if (user.getEmail() == null ? newEmail != null : !user.getEmail().equals(newEmail)) {
            user.setEmailVerified(false);
            user.setEmailVerificationToken(null);
            user.setEmailVerificationTokenIssuedDate(null);
        } else {
            // Email is not updated, no need to reset email verification
        }
    }

    @Override
    @Transactional
    public User updateUser(long id, UpdateUserRequest request) throws UsernameTakenException, InvalidRequestException {
        User loggedInUser = fetchLoggedInUser();
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(User.class, id));

        // Only update if logged in user role is admin OR source partner admin AND they created the user.
        if (authoriseAdminUser()) {
            checkAndResetEmailVerification(user, request.getEmail());
            populateUserFields(user, request, loggedInUser);
            userRepository.save(user);
        } else {
            throw new InvalidRequestException("You don't have permission to edit this user.");
        }
        return user;
    }


    /**
     * Check that the logged in user is authorized to create or update a user.
     * Admin users can create or update users (unless they are read only).
     * @return True if authorised
     */
    private boolean authoriseAdminUser() {
        boolean authSuccess;
        User loggedInUser = fetchLoggedInUser();
        if (loggedInUser.getReadOnly()) {
            authSuccess = false;
        } else {
            authSuccess = authService.hasAdminPrivileges(loggedInUser.getRole());
        }

        return authSuccess;
    }

    /**
     * Validates that if restricted source countries are present,
     * and if valid it adds those source countries are added to new or updated users.
     * If creating users source countries is empty, it means no restrictions.
     * @param user User to add source countries to.
     * @param requestCountries The list of countries from the request. Can be empty.
     * @param creatingUser User which is assigning countries to the given user
     */
    private void addSourceCountriesIfValid(User user, List<Country> requestCountries,
        User creatingUser) throws InvalidRequestException {
        // Clear old source country joins before adding again
        user.getSourceCountries().clear();
        if (CollectionUtils.isNotEmpty(requestCountries)) {
            for (Country sourceCountry : requestCountries) {
                if (creatingUser.getSourceCountries().isEmpty() || creatingUser.getSourceCountries().contains(sourceCountry)) {
                    user.getSourceCountries().add(sourceCountry);
                } else {
                    throw new InvalidRequestException("You don't have permission to add this country.");
                }
                user.getSourceCountries().add(sourceCountry);
            }
        } else {
            if (creatingUser.getRole().equals(Role.partneradmin) && !creatingUser.getSourceCountries().isEmpty()) {
                for (Country sourceCountry : creatingUser.getSourceCountries()) {
                    user.getSourceCountries().add(sourceCountry);
                }
            }
        }
    }

    /**
     * Validates whether given creating user can assign a given role to the user they are creating
     * or updating.
     * <p/>
     * See doc on {@link Role} for who can create users of what types.
     * @param user User - the user to add or update role type to.
     * @param requestedRole - The role to change to in the request.
     * @param creatingUser User that is assigning role to given user
     */
    private void addRoleIfValid(User user, Role requestedRole,
        User creatingUser) throws InvalidRequestException {
        Role creatingUserRole = creatingUser.getRole();
        if (creatingUserRole == Role.systemadmin) {
            user.setRole(requestedRole);
        } else if (creatingUserRole == Role.admin) {
            if (requestedRole != Role.systemadmin) {
                user.setRole(requestedRole);
            } else {
                throw new InvalidRequestException("You don't have permission to save this role type.");
            }
        } else if (creatingUserRole == Role.partneradmin) {
            // Check that source partner admin is only saving roles that are allowed
            // (not admin or system admin)
            if (requestedRole != Role.systemadmin && requestedRole != Role.admin) {
                user.setRole(requestedRole);
            } else {
                throw new InvalidRequestException("You don't have permission to save this role type.");
            }
        } else {
            throw new InvalidRequestException("You don't have permission to save this role type.");
        }
    }

    @Override
    @Transactional
    public void deleteUser(long id) throws NoSuchObjectException, InvalidRequestException {
        User loggedInUser = fetchLoggedInUser();
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(User.class, id));
        if (authoriseAdminUser()) {
            user.setStatus(Status.deleted);
            user.setAuditFields(loggedInUser);
            userRepository.save(user);
        } else {
            throw new InvalidRequestException("You don't have permission to delete this user.");
        }
    }

    @Override
    public boolean isCandidate(@Nullable User user) {
        return user != null && user.getRole().equals(Role.user);
    }

    @Override
    public JwtAuthenticationResponse login(LoginRequest request) throws AccountLockedException {
        try {
            String loggedInName = request.getUsername();
            if (loggedInName.contains("@")) {
                //User has supplied email as username.
                //See if we have a user with this email.
                try {
                    User exists = userRepository.findByEmailIgnoreCase(loggedInName);
                    if (exists != null) {
                        loggedInName = exists.getUsername();
                    }
                } catch (Exception ex) {
                    //Log details to check for nature of brute force attacks.
                    LogBuilder.builder(log)
                        .action("Login")
                        .message("Invalid credentials for user with given username: " +
                            request.getUsername())
                        .logError(ex);

                    //Exception if there is more than one user associated with email.
                    throw new InvalidCredentialsException("Sorry, that email is not unique. Log in with your username.");
                }
                //Just continue if we couldn't find user for email
            }


            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loggedInName, request.getPassword()
            ));
            User user = userRepository.findByUsernameIgnoreCase(loggedInName);

            if (user.getStatus().equals(Status.inactive)) {
                throw new InvalidCredentialsException("Sorry, it looks like that account is no longer active.");
            }

            user.setLastLogin(OffsetDateTime.now());
            user = userRepository.save(user);

            SecurityContextHolder.getContext().setAuthentication(auth);
            String jwt = tokenProvider.generateToken(auth);
            return new JwtAuthenticationResponse(jwt, user);

        } catch (BadCredentialsException e) {
            //Log details to check for nature of brute force attacks.
            LogBuilder.builder(log)
                .action("Login")
                .message("Invalid credentials for user with given username: " +
                    request.getUsername())
                .logError(e);

            // map spring exception to a service exception for better handling
            throw new InvalidCredentialsException("Invalid credentials for user");
        } catch (LockedException e) {
            throw new AccountLockedException("Account locked");
        } catch (DisabledException e) {
            throw new UserDeactivatedException();
        } catch (CredentialsExpiredException e) {
            throw new PasswordExpiredException();
        }
    }

    @Override
    public void logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    private User fetchLoggedInUser() {
        User user = authService.getLoggedInUser().orElse(null);
        if (user == null) {
            throw new InvalidSessionException("Can not find an active session for a user with this token");
        }
        return user;
    }

    @Override
    public User getLoggedInUser() {
        User user = authService.getLoggedInUser().orElse(null);
        if (user != null) {
            //Fetch user from database
            user = getUser(user.getId());
        }
        return user;
    }

    @Override
    public Partner getLoggedInPartner() {
        Partner partner = null;
        User user = authService.getLoggedInUser().orElse(null);
        if (user != null) {
            //Fetch user from database
            user = getUser(user.getId());
            partner = user.getPartner();
        }
        return partner;
    }

    @Override
    public User getSystemAdminUser() {
        return findByUsernameAndRole(SystemAdminConfiguration.SYSTEM_ADMIN_NAME, Role.systemadmin);
    }

    @Override
    @Transactional
    public void sendVerifyEmailRequest(SendVerifyEmailRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail());
        if (user != null) {
            user.setEmailVerificationToken(UUID.randomUUID().toString());
            user.setEmailVerificationTokenIssuedDate(OffsetDateTime.now());
            userRepository.save(user);

            try {
                emailHelper.sendVerificationEmail(user);
            } catch (EmailSendFailedException e) {
                LogBuilder.builder(log)
                        .action("SendVerificationEmail")
                        .message("Unable to send verification email for " + user.getEmail())
                        .logError(e);
            }
        } else {
            LogBuilder.builder(log)
                    .action("GenerateVerificationToken")
                    .message("Unable to send verification email for " + request.getEmail())
                    .logError();
        }
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        try {
            User user = userRepository.findByEmailVerificationToken(request.getToken());

            if (user == null) {
                throw new InvalidEmailVerificationTokenException();
            } else if (OffsetDateTime.now().isAfter(user.getEmailVerificationTokenIssuedDate().plusHours(24))) {
                emailHelper.sendCompleteVerificationEmail(user, false);
                throw new ExpiredEmailTokenException();
            }

            log.info("Email verification token is valid.");
            log.info("Received verify email request: {}", request);
            log.info("Email verification token: {}", user.getEmailVerificationToken());
            log.info("Email verification token issued date: {}", user.getEmailVerificationTokenIssuedDate());

            user.setEmailVerified(true);
            user.setEmailVerificationTokenIssuedDate(user.getEmailVerificationTokenIssuedDate());
            user.setEmailVerificationToken(user.getEmailVerificationToken());
            userRepository.save(user);
            emailHelper.sendCompleteVerificationEmail(user, true);
        } catch (Exception e) {
            log.error("An unexpected error occurred during email verification", e);
            throw new ServiceException("email_verification_error", "An unexpected error occurred during email verification", e);
        }
    }

    /**
     * CANDIDATE PORTAL: Update a users password
     * @param request Request containing password
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UpdateUserPasswordRequest request) {
        /* Check that the new passwords match */
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new PasswordMatchException();
        }

        /* Check that the old passwords match */
        User user = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        // TODO extend PasswordEncoder to expose BCrypts `checkpw` method (to compare plaintext and hashed passwords)
//        String oldPasswordEnc = passwordHelper.encodePassword(request.getOldPassword());
//        if (!passwordHelper.isValidPassword(user.getPasswordEnc(), oldPasswordEnc)) {
//            throw new InvalidCredentialsException("Invalid credentials for this user");
//        }

        /* Change the password */
        String passwordEnc = passwordHelper.validateAndEncodePassword(request.getPassword());
        user.setPasswordEnc(passwordEnc);
        userRepository.save(user);
    }

    /**
     * ADMIN PORTAL: Update an administrators user password
     * @param id of user
     * @param request Request containing password
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserPassword(long id, UpdateUserPasswordRequest request) throws InvalidRequestException {
        /* Get user */
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(User.class, id));
        if (authoriseAdminUser()) {
            /* Check that the new passwords match */
            if (!request.getPassword().equals(request.getPasswordConfirmation())) {
                throw new PasswordMatchException();
            }

            /* Change the password */
            String passwordEnc = passwordHelper.validateAndEncodePassword(request.getPassword());
            user.setPasswordEnc(passwordEnc);
            userRepository.save(user);
        } else {
            throw new InvalidRequestException("You don't have permission to update this user's password.");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateResetPasswordToken(SendResetPasswordEmailRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail());
        if (user != null) {
            user.setResetToken(UUID.randomUUID().toString());
            user.setResetTokenIssuedDate(OffsetDateTime.now());
            this.userRepository.save(user);

            try {
                emailHelper.sendResetPasswordEmail(user);
            } catch (EmailSendFailedException e) {
                LogBuilder.builder(log)
                    .action("ResetPassword")
                    .message("Unable to send reset password email for " + user.getEmail())
                    .logError(e);
            }

            // temporary for testing till emails are working
            String resetUrl = user.getRole() == Role.user ? portalUrl : adminUrl;

            LogBuilder.builder(log)
                .action("GenerateResetPasswordToken")
                .message("RESET URL: " + resetUrl + "/reset-password/" + user.getResetToken())
                .logInfo();
        } else {
            LogBuilder.builder(log)
                .action("GenerateResetPasswordToken")
                .message("Unable to send reset password email for " + request.getEmail())
                .logError();
        }
    }

    @Override
    public void checkResetToken(CheckPasswordResetTokenRequest request) {
        User user = userRepository.findByResetToken(request.getToken());

        if (user == null) {
            throw new InvalidPasswordTokenException();
        } else if (OffsetDateTime.now().isAfter(user.getResetTokenIssuedDate().plusHours(2))) {
            throw new ExpiredTokenException();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new PasswordMatchException();
        }

        User user = userRepository.findByResetToken(request.getToken());

        if (user != null) {
            String passwordEnc = passwordHelper.validateAndEncodePassword(request.getPassword());

            LogBuilder.builder(log)
                .action("ResetPassword")
                .message("Saving new password for user with id " + user.getId())
                .logInfo();

            user.setPasswordEnc(passwordEnc);
            user.setPasswordUpdatedDate(OffsetDateTime.now());
            user.setResetTokenIssuedDate(null);
            user.setResetToken(null);
            userRepository.save(user);
        }
    }

    @Override
    public void mfaReset(long id) throws NoSuchObjectException, InvalidRequestException {
        User loggedInUser = fetchLoggedInUser();
        User user = this.userRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(User.class, id));
        if (authoriseAdminUser()) {
            user.setMfaSecret(null);
            user.setAuditFields(loggedInUser);
            userRepository.save(user);
        } else {
            throw new InvalidRequestException("You don't have permission to reset this user's MFA.");
        }
    }

    @Override
    public EncodedQrImage mfaSetup() {

        User user = fetchLoggedInUser();

        // Generate and store the secret
        String secret = totpSecretGenerator.generate();
        //Store with user
        user.setMfaSecret(secret);
        userRepository.save(user);

        QrData data = totpQrDataFactory.newBuilder()
            .label(user.getEmail())
            .secret(secret)
            .issuer("TalentCatalog")
            .build();

        // Generate the QR code image data as a base64 string which
        // can be used in an <img> tag
        // See https://www.w3docs.com/snippets/html/how-to-display-base64-images-in-html.html
        try {
            String qrCodeImage = getDataUriForImage(
                totpQrGenerator.generate(data),
                totpQrGenerator.getImageMimeType()
            );

            return new EncodedQrImage(qrCodeImage);
        } catch (QrGenerationException ex) {
            throw new ServiceException("qr_error", "Error generating QR code", ex);
        }
    }

    @Override
    public void mfaVerify(String mfaCode) throws InvalidCredentialsException {
        User user = fetchLoggedInUser();
        if (user.getUsingMfa()) {
            if (mfaCode == null || mfaCode.isEmpty()) {
                throw new InvalidCredentialsException("You need to enter an authentication code for this user");
            }
            if (!totpVerifier.isValidCode(user.getMfaSecret(), mfaCode)) {
                throw new InvalidCredentialsException("Incorrect authentication code - try again. Or contact a Talent Catalog administrator.");
            }
        }
    }

    @Override
    public List<User> searchStaffNotUsingMfa() {
        return userRepository.searchStaffNotUsingMfa();
    }

    //10pm Sunday night GMT
    @Scheduled(cron = "0 0 22 * * SUN", zone = "GMT")
    @SchedulerLock(name = "UserService_searchStaffNotUsingMfa", lockAtLeastFor = "PT23H", lockAtMostFor = "PT23H")
    public void checkMfaUsers() {
        List<User> users = searchStaffNotUsingMfa();
        if (!users.isEmpty()) {
            String s = users.stream()
                .map(User::getUsername)
                .collect(Collectors.joining(","));
            final String mess = "The following staff members have MFA disabled: " + s;

            LogBuilder.builder(log)
                .action("CheckMfaUsers")
                .message(mess)
                .logWarn();

            emailHelper.sendAlert(mess);
        }
    }

}
