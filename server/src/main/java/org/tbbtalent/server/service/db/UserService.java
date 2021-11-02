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

import java.util.List;
import javax.security.auth.login.AccountLockedException;
import org.springframework.data.domain.Page;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.db.Role;
import org.tbbtalent.server.model.db.User;
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
import org.tbbtalent.server.util.qr.EncodedQrImage;

public interface UserService {

    JwtAuthenticationResponse login(LoginRequest request) throws AccountLockedException;

    void logout();
    User getMyUser();
    void resetPassword(ResetPasswordRequest request);
    void checkResetToken(CheckPasswordResetTokenRequest request);
    void generateResetPasswordToken(SendResetPasswordEmailRequest request);
    void updatePassword(UpdateUserPasswordRequest request);
    void updateUserPassword(long id, UpdateUserPasswordRequest request);

    User findByUsernameAndRole(String username, Role role);

    Page<User> searchUsers(SearchUserRequest request);

    User getUser(long id);

    User createUser(CreateUserRequest request, @Nullable User creatingUser) throws UsernameTakenException;
    User createUser(CreateUserRequest request) throws UsernameTakenException;

    User updateUser(long id, UpdateUserRequest request);

    User updateUsername(long id, UpdateUsernameRequest request);

    void deleteUser(long id);

    User addToSharedWithUser(long id, UpdateSharingRequest request);

    User removeFromSharedWithUser(long id, UpdateSharingRequest request);

    /**
     * Clears the mfaSecret for the given user.
     * <p/>
     * The next time they login they will be prompted to setup again
     * @param id id of user.
     * @throws NoSuchObjectException if no such user exists
     */
    void mfaReset(long id) throws NoSuchObjectException;

    /**
     * Sets up a Multi Factor Authorization (MFA) using Time based One Time Password (TOTP),
     * creating a new secret and its corresponding QR Code to be displayed to a user.
     * @return QR code image
     */
    EncodedQrImage mfaSetup();

    /**
     * Verifies that the given code matches our Multi Factor Authorization (MFA).
     * @param mfaCode Code received from user which should match what is expected, otherwise
     *                authorization fails.
     * @throws InvalidCredentialsException if verification fails
     */
    void mfaVerify(String mfaCode) throws InvalidCredentialsException;

    /**
     * Returns all staff users (ie not candidates) who are not using multi factor authentication.
     * @return Users not using mfa.
     */
    List<User> searchStaffNotUsingMfa();
}
