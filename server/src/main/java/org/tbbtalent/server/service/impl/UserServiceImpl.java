package org.tbbtalent.server.service.impl;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.*;
import org.tbbtalent.server.model.*;
import org.tbbtalent.server.repository.*;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.request.user.*;
import org.tbbtalent.server.response.JwtAuthenticationResponse;
import org.tbbtalent.server.security.JwtTokenProvider;
import org.tbbtalent.server.security.PasswordHelper;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.UserService;
import org.tbbtalent.server.service.UserService;

import javax.security.auth.login.AccountLockedException;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordHelper passwordHelper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserContext userContext;

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
            User user = this.userRepository.findByUsernameIgnoreCase(request.getUsername());

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
}
