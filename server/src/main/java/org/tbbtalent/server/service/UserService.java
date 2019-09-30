package org.tbbtalent.server.service;

import javax.security.auth.login.AccountLockedException;

import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.request.user.CheckPasswordResetTokenRequest;
import org.tbbtalent.server.request.user.ResetPasswordRequest;
import org.tbbtalent.server.request.user.SendResetPasswordEmailRequest;
import org.tbbtalent.server.request.user.UpdateUserPasswordRequest;
import org.tbbtalent.server.response.JwtAuthenticationResponse;

public interface UserService {

    JwtAuthenticationResponse login(LoginRequest request) throws AccountLockedException;
    void logout();
    User getMyUser();
    void resetPassword(ResetPasswordRequest request);
    void checkResetToken(CheckPasswordResetTokenRequest request);
    void generateResetPasswordToken(SendResetPasswordEmailRequest request);
    void updatePassword(UpdateUserPasswordRequest request);


}
