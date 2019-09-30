package org.tbbtalent.server.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.security.auth.login.AccountLockedException;

import org.apache.commons.lang3.StringUtils;
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
import org.tbbtalent.server.exception.*;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.model.Role;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.repository.UserRepository;
import org.tbbtalent.server.repository.UserSpecification;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.request.user.*;
import org.tbbtalent.server.request.user.CheckPasswordResetTokenRequest;
import org.tbbtalent.server.request.user.ResetPasswordRequest;
import org.tbbtalent.server.request.user.SendResetPasswordEmailRequest;
import org.tbbtalent.server.request.user.UpdateUserPasswordRequest;
import org.tbbtalent.server.response.JwtAuthenticationResponse;
import org.tbbtalent.server.security.JwtTokenProvider;
import org.tbbtalent.server.security.PasswordHelper;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordHelper passwordHelper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserContext userContext;

    @Value("${web.portal}")
    private String portalUrl;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           PasswordHelper passwordHelper,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider tokenProvider,
                           UserContext userContext) {
        this.userRepository = userRepository;
        this.passwordHelper = passwordHelper;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userContext = userContext;
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
                StringUtils.isBlank(request.getUsername()) ? request.getEmail() : null,
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                Role.user);

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
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

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
            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getUsername(), request.getPassword()
            ));
            User user = userRepository.findByUsernameIgnoreCase(request.getUsername());

            if (user.getStatus().equals(Status.inactive)) {
                throw new InvalidCredentialsException("Sorry, it looks like that account is no longer active.");
            }

            SecurityContextHolder.getContext().setAuthentication(auth);
            String jwt = tokenProvider.generateToken(auth);
            return new JwtAuthenticationResponse(jwt, user);

        } catch (BadCredentialsException e) {
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
    public void generateResetPasswordToken(SendResetPasswordEmailRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail());
        if (user != null) {
            user.setResetToken(UUID.randomUUID().toString());
            user.setResetTokenIssuedDate(LocalDateTime.now());
            this.userRepository.save(user);

            /* TODO
            try {
                emailHelper.sendResetPasswordEmail(user);
            } catch (EmailSendFailedException e) {
                log.error("unable to send reset password email for " + user.getEmail());
            }
            */

            // temporary for testing till emails are working
            log.info("RESET URL: " + portalUrl + "/reset-password/" + user.getResetToken());
        }
    }

    @Override
    public void checkResetToken(CheckPasswordResetTokenRequest request) {
        User user = userRepository.findByResetToken(request.getToken());

        if (user == null) {
            throw new InvalidPasswordTokenException();
        } else if (LocalDateTime.now().isAfter(user.getResetTokenIssuedDate().plusHours(2))) {
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
            user.setPasswordUpdatedDate(LocalDateTime.now());
            user.setResetTokenIssuedDate(null);
            user.setResetToken(null);
            userRepository.save(user);
        }
    }
}
