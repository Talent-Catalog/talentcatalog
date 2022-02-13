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

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.security.auth.login.AccountLockedException;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.tbbtalent.server.configuration.SystemAdminConfiguration;
import org.tbbtalent.server.exception.EmailSendFailedException;
import org.tbbtalent.server.exception.ExpiredTokenException;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.InvalidPasswordTokenException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.InvalidSessionException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.PasswordExpiredException;
import org.tbbtalent.server.exception.PasswordMatchException;
import org.tbbtalent.server.exception.ServiceException;
import org.tbbtalent.server.exception.UserDeactivatedException;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.model.db.Role;
import org.tbbtalent.server.model.db.SavedSearch;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.CountryRepository;
import org.tbbtalent.server.repository.db.SavedSearchRepository;
import org.tbbtalent.server.repository.db.UserRepository;
import org.tbbtalent.server.repository.db.UserSpecification;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.request.user.CheckPasswordResetTokenRequest;
import org.tbbtalent.server.request.user.CreateUserRequest;
import org.tbbtalent.server.request.user.ResetPasswordRequest;
import org.tbbtalent.server.request.user.SearchUserRequest;
import org.tbbtalent.server.request.user.SendResetPasswordEmailRequest;
import org.tbbtalent.server.request.user.UpdateSharingRequest;
import org.tbbtalent.server.request.user.UpdateUserPasswordRequest;
import org.tbbtalent.server.request.user.UpdateUserRequest;
import org.tbbtalent.server.request.user.UpdateUsernameRequest;
import org.tbbtalent.server.response.JwtAuthenticationResponse;
import org.tbbtalent.server.security.AuthService;
import org.tbbtalent.server.security.JwtTokenProvider;
import org.tbbtalent.server.security.PasswordHelper;
import org.tbbtalent.server.service.db.UserService;
import org.tbbtalent.server.service.db.email.EmailHelper;
import org.tbbtalent.server.util.qr.EncodedQrImage;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final CountryRepository countryRepository;
    private final PasswordHelper passwordHelper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuthService authService;
    private final EmailHelper emailHelper;
    private final SavedSearchRepository savedSearchRepository;

    @Value("${web.portal}")
    private String portalUrl;


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
                           CandidateRepository candidateRepository,
                           CountryRepository countryRepository,
                           SavedSearchRepository savedSearchRepository,
                           PasswordHelper passwordHelper,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider tokenProvider,
                           AuthService authService,
                           EmailHelper emailHelper) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.countryRepository = countryRepository;
        this.savedSearchRepository = savedSearchRepository;
        this.passwordHelper = passwordHelper;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.authService = authService;
        this.emailHelper = emailHelper;
    }

    @Override
    public User findByUsernameAndRole(String username, Role role) {
        return userRepository.findByUsernameAndRole(username, role);
    }

    @Override
    public Page<User> searchUsers(SearchUserRequest request) {
        Page<User> users = userRepository.findAll(
                UserSpecification.buildSearchQuery(request), request.getPageRequest());
        log.info("Found " + users.getTotalElements() + " users in search");
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
    public User createUser(CreateUserRequest request, @Nullable User creatingUser)
        throws UsernameTakenException {
        User user = new User(
            request.getUsername(),
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            request.getRole());
        user.setReadOnly(request.getReadOnly());
        user.setUsingMfa(request.getUsingMfa());

        //Avoid checks if there is no creating user (ie the currently logged in user)
        if (creatingUser != null) {
            //Validate source countries aren't restricted, and add to user.
            addSourceCountriesIfValid(user, request.getSourceCountries());
            // Validate the role requested, and add to user.
            addRoleIfValid(user, request.getRole());
        }

        /* Validate the password before account creation */
        String passwordEncrypted = passwordHelper.validateAndEncodePassword(request.getPassword());
        user.setPasswordEnc(passwordEncrypted);
        User existing = userRepository.findByUsernameIgnoreCase(user.getUsername());
        if (existing != null){
            throw new UsernameTakenException("username");
        }

        existing = userRepository.findByEmailIgnoreCase(user.getEmail());
        if (existing != null){
            throw new UsernameTakenException("email");
        }
        user.setAuditFields(creatingUser == null ? user : creatingUser);
        return this.userRepository.save(user);
    }

    @Override
    @Transactional
    public User createUser(CreateUserRequest request) throws UsernameTakenException, InvalidRequestException {
        User loggedInUser = getLoggedInUser();
        boolean authSuccess;

        if (loggedInUser.getReadOnly()) {
            authSuccess = false;
        } else if (loggedInUser.getRole() == Role.admin || loggedInUser.getRole() == Role.sourcepartneradmin) {
            authSuccess = true;
        } else {
            authSuccess = false;
        }

        if (authSuccess) {
            return createUser(request, loggedInUser);
        } else {
            throw new InvalidRequestException("You don't have permission to create a user.");
        }

    }

    @Override
    @Transactional
    public User updateUser(long id, UpdateUserRequest request) throws UsernameTakenException, InvalidRequestException {
        User loggedInUser = getLoggedInUser();
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(User.class, id));

        // Only update if logged in user role is admin OR source partner admin AND they created the user.
        if (authoriseAdminUser(user)) {
            if (!user.getEmail().equalsIgnoreCase(request.getEmail())){
                User existing = userRepository.findByEmailIgnoreCase(request.getEmail());
                if (existing != null){
                    throw new UsernameTakenException("email");
                }
            }

            //Check source countries aren't restricted, and add to user.
            addSourceCountriesIfValid(user, request.getSourceCountries());

            //Check role type isn't restricted, and add to user.
            addRoleIfValid(user, request.getRole());

            user.setReadOnly(request.getReadOnly());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setStatus(request.getStatus());
            user.setUsingMfa(request.getUsingMfa());
            user.setAuditFields(loggedInUser);
            userRepository.save(user);
        } else {
            throw new InvalidRequestException("You don't have permission to edit this user.");
        }
        return user;
    }


    /**
     * Check that the logged in user is authorized to create or update a user.
     * Admin users can create or update users (unless they are read only).
     * Source partner admins can create users, but only for their source countries.
     * Source partner admins can also only create users that arent admins or source partner admins.
     * Source partner admins can only update users who they created.
     * @param user It is the user to be updated.
     * @return True if authorised
     */
    private boolean authoriseAdminUser(User user) {
        boolean authSuccess;
        User loggedInUser =getLoggedInUser();
        if (loggedInUser.getReadOnly()) {
            authSuccess = false;
        } else if (loggedInUser.getRole() == Role.admin) {
            authSuccess = true;
        } else if (loggedInUser.getRole() == Role.sourcepartneradmin) {
            authSuccess = true;
//            // Only allowed to update/delete if user belongs to logged in user.
//            authSuccess = user.getCreatedBy().getId().equals(loggedInUser.getId());
        } else {
            authSuccess = false;
        }
        return authSuccess;
    }

    /**
     * Validates that if restricted source countries are present,
     * and if valid it adds those source countries are added to new or updated users.
     * If logged in users source countries is empty, it means no restrictions.
     * @param user User to add source countries to.
     * @param requestCountries The list of countries from the request. Can be empty.
     */
    private void addSourceCountriesIfValid(User user, List<Country> requestCountries) throws InvalidRequestException {
        User loggedInUser = getLoggedInUser();
        // Only update source countries if they are different from existing.
        List<Country> currentUserCountries = new ArrayList<>(user.getSourceCountries());
        //todo This comparison will always be unequal - doesn't compare contents of lists
        if (!currentUserCountries.equals(requestCountries)) {
            // Clear old source country joins before adding again
            user.getSourceCountries().clear();
            if (CollectionUtils.isNotEmpty(requestCountries)) {
                for (Country sourceCountry : requestCountries) {
                    if (loggedInUser.getSourceCountries().isEmpty() || loggedInUser.getSourceCountries().contains(sourceCountry)) {
                        user.getSourceCountries().add(sourceCountry);
                    } else {
                        throw new InvalidRequestException("You don't have permission to add this country.");
                    }
                    user.getSourceCountries().add(sourceCountry);
                }
            } else {
                if (loggedInUser.getRole().equals(Role.sourcepartneradmin) && !loggedInUser.getSourceCountries().isEmpty()) {
                    for (Country sourceCountry : loggedInUser.getSourceCountries()) {
                        user.getSourceCountries().add(sourceCountry);
                    }
                }
            }
        }
    }

    /**
     * Validates that source partner admins can only set roles that aren't admin or source partner admin.
     * @param user User - the user to add or update role type to.
     * @param requestedRole - The role to change to in the request.
     */
    private void addRoleIfValid(User user, Role requestedRole) throws InvalidRequestException {
        User loggedInUser = getLoggedInUser();
        Role loggedInRole = loggedInUser.getRole();
        if (loggedInRole == Role.admin) {
            user.setRole(requestedRole);
        } else {
            // Check that source partner admin is only saving roles that are allowed (not admin or source partner admin)
            if (requestedRole != Role.admin && requestedRole != Role.sourcepartneradmin) {
                user.setRole(requestedRole);
            } else {
                throw new InvalidRequestException("You don't have permission to save this role type.");
            }
        }
    }

    @Override
    @Transactional
    public User updateUsername(long id, UpdateUsernameRequest request) throws NoSuchObjectException, InvalidRequestException {
        User loggedInUser = getLoggedInUser();
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(User.class, id));
        if (authoriseAdminUser(user)) {
            if (!user.getUsername().equalsIgnoreCase(request.getUsername())){
                User existing = userRepository.findByUsernameIgnoreCase(request.getUsername());
                if (existing != null){
                    throw new UsernameTakenException("username");
                }
            }
            user.setUsername(request.getUsername());
            user.setAuditFields(loggedInUser);
            userRepository.save(user);
        } else {
            throw new InvalidRequestException("You don't have permission to update this user's username.");
        }
        return user;
    }

    @Override
    @Transactional
    public User addToSharedWithUser(long id, UpdateSharingRequest request) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(User.class, id));

        final Long sharedSearchID = request.getSavedSearchId();
        SavedSearch savedSearch = savedSearchRepository.findById(sharedSearchID)
                .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, sharedSearchID));

        user.addSharedSearch(savedSearch);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User removeFromSharedWithUser(long id, UpdateSharingRequest request) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(User.class, id));

        final Long sharedSearchID = request.getSavedSearchId();
        SavedSearch savedSearch = savedSearchRepository.findById(sharedSearchID)
                .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, sharedSearchID));

        user.removeSharedSearch(savedSearch);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(long id) throws NoSuchObjectException, InvalidRequestException {
        User loggedInUser = getLoggedInUser();
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(User.class, id));
        if (authoriseAdminUser(user)) {
            user.setStatus(Status.deleted);
            user.setAuditFields(loggedInUser);
            userRepository.save(user);
        } else {
            throw new InvalidRequestException("You don't have permission to delete this user.");
        }
    }


    @Override
    public JwtAuthenticationResponse login(LoginRequest request, String hostDomain) throws AccountLockedException {
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
                    log.info("Invalid credentials for user: " + request + ". Exception " + ex);
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
            user.setHostDomain(hostDomain);
            user = userRepository.save(user);

            SecurityContextHolder.getContext().setAuthentication(auth);
            String jwt = tokenProvider.generateToken(auth);
            return new JwtAuthenticationResponse(jwt, user);

        } catch (BadCredentialsException e) {
            //Log details to check for nature of brute force attacks.
            log.info("Invalid credentials for user: " + request);
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

    private User getLoggedInUser() {
        User user = authService.getLoggedInUser().orElse(null);
        if (user == null) {
            throw new InvalidSessionException("Can not find an active session for a user with this token");
        }
        return user;
    }

    @Override
    public User getMyUser() {
        return authService.getLoggedInUser().orElse(null);
    }

    @Override
    public User getSystemAdminUser() {
        return findByUsernameAndRole(SystemAdminConfiguration.SYSTEM_ADMIN_NAME, Role.admin);
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
        if (authoriseAdminUser(user)) {
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
                Candidate candidate = candidateRepository.findByUserId(user.getId());
                emailHelper.sendResetPasswordEmail(user);
            } catch (EmailSendFailedException e) {
                log.error("unable to send reset password email for " + user.getEmail());
            }

            // temporary for testing till emails are working
            log.info("RESET URL: " + portalUrl + "/reset-password/" + user.getResetToken());
        } else {
            log.error("unable to send reset email for " + request.getEmail());
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

            log.info("Saving new password for user with id {}", user.getId());
            user.setPasswordEnc(passwordEnc);
            user.setPasswordUpdatedDate(OffsetDateTime.now());
            user.setResetTokenIssuedDate(null);
            user.setResetToken(null);
            userRepository.save(user);
        }
    }

    @Override
    public void mfaReset(long id) throws NoSuchObjectException, InvalidRequestException {
        User loggedInUser = getLoggedInUser();
        User user = this.userRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(User.class, id));
        if (authoriseAdminUser(user)) {
            user.setMfaSecret(null);
            user.setAuditFields(loggedInUser);
            userRepository.save(user);
        } else {
            throw new InvalidRequestException("You don't have permission to reset this user's MFA.");
        }
    }

    @Override
    public EncodedQrImage mfaSetup() {

        User user = getLoggedInUser();

        // Generate and store the secret
        String secret = totpSecretGenerator.generate();
        //Store with user
        user.setMfaSecret(secret);
        userRepository.save(user);

        QrData data = totpQrDataFactory.newBuilder()
            .label(user.getEmail())
            .secret(secret)
            .issuer("TBB")
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
        User user = getLoggedInUser();
        if (user.getUsingMfa()) {
            if (mfaCode == null || mfaCode.length() == 0) {
                throw new InvalidCredentialsException("You need to enter an authentication code for this user");
            }
            if (!totpVerifier.isValidCode(user.getMfaSecret(), mfaCode)) {
                throw new InvalidCredentialsException("Incorrect authentication code - try again. Or contact a TBB administrator.");
            }
        }
    }

    @Override
    public List<User> searchStaffNotUsingMfa() {
        return userRepository.searchStaffNotUsingMfa();
    }

    //10pm Sunday night GMT
    @Scheduled(cron = "0 0 22 * * SUN", zone = "GMT")
    public void checkMfaUsers() {
        List<User> users = searchStaffNotUsingMfa();
        if (users.size() > 0) {
            String s = users.stream()
                .map(User::getUsername)
                .collect(Collectors.joining(","));
            final String mess = "The following staff members have MFA disabled: " + s;
            log.warn(mess);
            emailHelper.sendAlert(mess);
        }
    }

}
