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
