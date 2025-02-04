/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.service.db;

import java.util.List;
import java.util.Set;
import javax.security.auth.login.AccountLockedException;
import org.springframework.data.domain.Page;
import org.springframework.lang.Nullable;
import org.tctalent.server.exception.InvalidCredentialsException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.UsernameTakenException;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.request.LoginRequest;
import org.tctalent.server.request.user.CheckPasswordResetTokenRequest;
import org.tctalent.server.request.user.ResetPasswordRequest;
import org.tctalent.server.request.user.SearchUserRequest;
import org.tctalent.server.request.user.SendResetPasswordEmailRequest;
import org.tctalent.server.request.user.UpdateUserPasswordRequest;
import org.tctalent.server.request.user.UpdateUserRequest;
import org.tctalent.server.response.JwtAuthenticationResponse;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.util.qr.EncodedQrImage;

import org.tctalent.server.request.user.emailverify.VerifyEmailRequest;
import org.tctalent.server.request.user.emailverify.SendVerifyEmailRequest;
import org.tctalent.server.request.user.emailverify.CheckEmailVerificationTokenRequest;

public interface UserService {

    /**
     * True if given user is a candidate.
     * @param user User
     * @return True if user is a candidate. Returns false if user is null or not a candidate.
     */
    boolean isCandidate(@Nullable User user);

    JwtAuthenticationResponse login(LoginRequest request) throws AccountLockedException;

    void logout();

    /**
     * Returns a live JPA entity representing the logged in user.
     * <p/>
     * Use this instead of {@link AuthService#getLoggedInUser()} when you want to access entity
     * properties of the user.
     * @return Logged in user or null if there is none.
     */
    @Nullable
    User getLoggedInUser();

    /**
     * Returns a live JPA entity representing the logged in partner.
     * @return Partner associated with logged in partner or null if no user.
     */
    Partner getLoggedInPartner();

    User getSystemAdminUser();

    void resetPassword(ResetPasswordRequest request);
    void checkResetToken(CheckPasswordResetTokenRequest request);
    void generateResetPasswordToken(SendResetPasswordEmailRequest request);
    void updatePassword(UpdateUserPasswordRequest request);
    void updateUserPassword(long id, UpdateUserPasswordRequest request);


    // Generates an email verification token and sends a verification email.
    void sendVerifyEmailRequest(SendVerifyEmailRequest request);
    // Checks the validity of the email verification token.
    void checkEmailVerificationToken(CheckEmailVerificationTokenRequest request);
    // Verifies the user's email using the verification token.
    void verifyEmail(VerifyEmailRequest request);

    User findByUsernameAndRole(String username, Role role);

    List<User> search(SearchUserRequest request);

    Page<User> searchPaged(SearchUserRequest request);

    User getUser(long id);

    /**
     * Get a user’s source countries, defaulting to all countries if empty
     */
    Set<Country> getDefaultSourceCountries(User user);

    User createUser(
        UpdateUserRequest request, @Nullable User creatingUser) throws UsernameTakenException;
    User createUser(UpdateUserRequest request) throws UsernameTakenException;

    User updateUser(long id, UpdateUserRequest request);

    void deleteUser(long id);

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
