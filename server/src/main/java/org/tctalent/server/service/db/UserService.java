/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db;

import java.util.List;
import java.util.Set;
import javax.security.auth.login.AccountLockedException;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
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
import org.tctalent.server.request.user.emailverify.SendVerifyEmailRequest;
import org.tctalent.server.request.user.emailverify.VerifyEmailRequest;
import org.tctalent.server.response.AuthenticationResponse;
import org.tctalent.server.response.JwtAuthenticationResponse;
import org.tctalent.server.security.AuthProfile;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.util.qr.EncodedQrImage;

public interface UserService {

    /**
     * True if given user is a candidate.
     * @param user User
     * @return True if user is a candidate. Returns false if user is null or not a candidate.
     */
    boolean isCandidate(@Nullable User user);

    /**
     * Log in a user based on the given OAuth2 profile.
     * @param profile Profile data managed by the IDP.
     * @return User logged in.
     */
    User login(AuthProfile profile);

    /**
     * Old, pre OAuth2, login
     */
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
     * Retrieves a user by their email verification token.
     * @param token The token associated with the user
     * @return The User associated with the token
     * @throws NoSuchObjectException if no user found with that token
     */
    User getUserByEmailVerificationToken(String token) throws NoSuchObjectException;

    /**
     * Returns a live JPA entity representing the logged in partner.
     * @return Partner associated with logged in partner or null if no user.
     */
    Partner getLoggedInPartner();

    User getSystemAdminUser();

    /**
     * Creates an AuthenticationResponse object for the given user.
     * This is the response returned on successful login or registration
     * @param user User associated with the authentication response
     * @return AuthenticationResponse returned on successful login or registration
     */
    AuthenticationResponse createAuthenticationResponse(User user);

    void resetPassword(ResetPasswordRequest request);
    void checkResetToken(CheckPasswordResetTokenRequest request);
    void generateResetPasswordToken(SendResetPasswordEmailRequest request);

    void updatePassword(UpdateUserPasswordRequest request);
    void updateUserPassword(long id, UpdateUserPasswordRequest request);

    /**
     * Generates an email verification token and sends a verification email.
     * Throws an error if the email verification token generation or sending fails.
     */
    void sendVerifyEmailRequest(SendVerifyEmailRequest request);
    /**
     * Checks the validity of the email verification token and verifies the user's email.
     * Throws an error if the email verification token is invalid or expired, or if sending the completion verification email fails.
     */
    void verifyEmail(VerifyEmailRequest request);

    User findByUsernameAndRole(String username, Role role);

    List<User> search(SearchUserRequest request);

    Page<User> searchPaged(SearchUserRequest request);

    User getUser(long id);

    /**
     * Get a user’s source countries, defaulting to all countries if empty
     */
    Set<Country> getDefaultSourceCountries(User user);

    /**
     * Should be called when an Oauth managed user logs in or registers.
     * <p>
     * It creates or updates a user based on the given OAuth profile for the user.
     * <p>
     * This is used to auto-create a minimal user record from the data held by the OAuth IDP,
     * or update an existing user record if it already exists with the data that is managed by the
     * IDP.
     * <p>
     * It looks up the user by issuer and subject if it can. If that is not successful, it will
     * fall back to looking up the user by email. If that also is not successful, it will create
     * a new user record with the provided profile data. If it does find a user, it will update
     * the user record with the new profile data.
     * <p>
     * This method should do all it can to create or update a user.
     * It should only throw an exception if there is a system error - such as not being able to
     * connect to the database.
     * <p>
     * <strong>Use cases:</strong>
     * <p>
     * Registration happens on IDP but completion does not succeed (eg because the server is down),
     * so there is no record on DB.
     * An exception will have been processed in Angular, so they will be recorded as logged out.
     * So the next interaction will be a log in request on the idp. The login completion request
     * to the server will use a token that matches nothing on the database, but the user can
     * be created using the passed in AuthProfile.
     * <p>
     * User migration. Cognito login hook is in place which checks whether a user email matches
     * a user on the database and, if so, whether the idp entered password matches the hash on the
     * db. If so, the login succeeds but the generated token will have an idp subject which does not
     * match any idp subject in the db. When the complete login is called, we will have to look up
     * the user by email and update the idp subject in the db.
     * @param profile User profile data managed by the IDP. It overwrites any data on the user
     *                record on our database.
     * @param partner Partner assigned to the user
     * @return User created or updated.
     */
    User syncOauthUserAtLoginOrRegister(AuthProfile profile, @NonNull Partner partner);

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
