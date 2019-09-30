package org.tbbtalent.server.service;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.request.user.CreateUserRequest;
import org.tbbtalent.server.request.user.SearchUserRequest;
import org.tbbtalent.server.request.user.UpdateUserRequest;
import org.tbbtalent.server.response.JwtAuthenticationResponse;

import javax.security.auth.login.AccountLockedException;

public interface UserService {

    JwtAuthenticationResponse login(LoginRequest request) throws AccountLockedException;
    
    void logout();

    Page<User> searchUsers(SearchUserRequest request);

    User getUser(long id);

    User createUser(CreateUserRequest request) throws UsernameTakenException;

    User updateUser(long id, UpdateUserRequest request);

    void deleteUser(long id);



}
