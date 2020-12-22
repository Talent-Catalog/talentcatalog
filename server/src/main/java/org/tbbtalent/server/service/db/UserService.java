/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.request.user.*;
import org.tbbtalent.server.response.JwtAuthenticationResponse;

import javax.security.auth.login.AccountLockedException;

public interface UserService {

    JwtAuthenticationResponse login(LoginRequest request) throws AccountLockedException;

    void logout();
    User getMyUser();
    void resetPassword(ResetPasswordRequest request);
    void checkResetToken(CheckPasswordResetTokenRequest request);
    void generateResetPasswordToken(SendResetPasswordEmailRequest request);
    void updatePassword(UpdateUserPasswordRequest request);
    void updateUserPassword(long id, UpdateUserPasswordRequest request);

    Page<User> searchUsers(SearchUserRequest request);

    User getUser(long id);

    User createUser(CreateUserRequest request) throws UsernameTakenException;

    User updateUser(long id, UpdateUserRequest request);

    User updateUsername(long id, UpdateUsernameRequest request);

    void deleteUser(long id);
    
    User addToSharedWithUser(long id, UpdateSharingRequest request);

    User removeFromSharedWithUser(long id, UpdateSharingRequest request);
}
