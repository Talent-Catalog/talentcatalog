package org.tbbtalent.server.service.db.impl;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.security.auth.login.AccountLockedException;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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
import org.tbbtalent.server.exception.EmailSendFailedException;
import org.tbbtalent.server.exception.ExpiredTokenException;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.InvalidPasswordTokenException;
import org.tbbtalent.server.exception.InvalidSessionException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.PasswordExpiredException;
import org.tbbtalent.server.exception.PasswordMatchException;
import org.tbbtalent.server.exception.UserDeactivatedException;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.Country;
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
import org.tbbtalent.server.security.JwtTokenProvider;
import org.tbbtalent.server.security.PasswordHelper;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.db.UserService;
import org.tbbtalent.server.service.db.email.EmailHelper;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final CountryRepository countryRepository;
    private final PasswordHelper passwordHelper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserContext userContext;
    private final EmailHelper emailHelper;
    private final SavedSearchRepository savedSearchRepository;

    @Value("${web.portal}")
    private String portalUrl;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           CandidateRepository candidateRepository,
                           CountryRepository countryRepository,
                           SavedSearchRepository savedSearchRepository,
                           PasswordHelper passwordHelper,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider tokenProvider,
                           UserContext userContext,
                           EmailHelper emailHelper) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.countryRepository = countryRepository;
        this.savedSearchRepository = savedSearchRepository;
        this.passwordHelper = passwordHelper;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userContext = userContext;
        this.emailHelper = emailHelper;
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
    @Transactional
    public User createUser(CreateUserRequest request) throws UsernameTakenException {
        User user = new User(
                request.getUsername(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getRole());

        user.setReadOnly(request.getReadOnly());

        if(CollectionUtils.isNotEmpty(request.getSourceCountries())) {
            for (Country sourceCountry : request.getSourceCountries()) {
                user.getSourceCountries().add(sourceCountry);
            }
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

        return this.userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(long id, UpdateUserRequest request) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(User.class, id));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())){
            User existing = userRepository.findByEmailIgnoreCase(request.getEmail());
            if (existing != null){
                throw new UsernameTakenException("email");
            }
        }

        // Clear old source country joins before adding again
        user.getSourceCountries().clear();
        for (Country sourceCountry : request.getSourceCountries()) {
            user.getSourceCountries().add(sourceCountry);
        }


        user.setReadOnly(request.getReadOnly());

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus());
        user.setRole(request.getRole());

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUsername(long id, UpdateUsernameRequest request) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(User.class, id));

        if (!user.getUsername().equalsIgnoreCase(request.getUsername())){
            User existing = userRepository.findByUsernameIgnoreCase(request.getUsername());
            if (existing != null){
                throw new UsernameTakenException("username");
            }
        }

        user.setUsername(request.getUsername());

        return userRepository.save(user);
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
    public void deleteUser(long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setStatus(Status.deleted);
            userRepository.save(user);
        }
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

    public User getLoggedInUser() {
        User user = userContext.getLoggedInUser();
        if (user == null) {
            throw new InvalidSessionException("Can not find an active session for a user with this token");
        }
        return user;
    }

    @Override
    public User getMyUser() {
        return userContext.getLoggedInUser();
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void updatePassword(UpdateUserPasswordRequest request) {
        /* Check that the new passwords match */
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new PasswordMatchException();
        }

        /* Check that the old passwords match */
        User user = userContext.getLoggedInUser();
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

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void updateUserPassword(long id, UpdateUserPasswordRequest request) {
        /* Get user */
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(User.class, id));

        /* Check that the new passwords match */
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new PasswordMatchException();
        }

        /* Change the password */
        String passwordEnc = passwordHelper.validateAndEncodePassword(request.getPassword());
        user.setPasswordEnc(passwordEnc);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
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
            throw new NoSuchObjectException(User.class, request.getEmail());
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
    @Transactional(readOnly = false, rollbackFor = Exception.class)
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
}
