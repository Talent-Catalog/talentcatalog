package org.tbbtalent.server.service;

import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.response.JwtAuthenticationResponse;

import javax.security.auth.login.AccountLockedException;

public interface UserService {

    JwtAuthenticationResponse login(LoginRequest request) throws AccountLockedException;
    void logout();


}
