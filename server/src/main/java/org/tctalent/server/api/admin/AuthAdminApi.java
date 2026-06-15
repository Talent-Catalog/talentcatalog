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

package org.tctalent.server.api.admin;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.model.db.PartnerDtoHelper;
import org.tctalent.server.model.db.User;
import org.tctalent.server.response.AuthenticationResponse;
import org.tctalent.server.security.AuthProfile;
import org.tctalent.server.security.OAuth2UserService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.dto.DtoBuilder;
import org.tctalent.server.util.qr.EncodedQrImage;

/**
 * Rest controller that handles endpoints for user login, logout and Multi-Factor Authentication setup. Login
 * authentication requires a valid username and password; in addition to which Multi-Factor Authentication is
 * required.
 * <p/>
 * The controller delegates to the UserService for login authentication, logout processing, MFA setup and MFA
 * verification.
 */
@RestController
@RequestMapping("/api/admin/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthAdminApi {

    private final UserService userService;
    private final OAuth2UserService oAuth2UserService;


    /**
     * This is the server side of an OAuth2 login flow. The actual login is handled by
     * an external Identity Provider (IDP), e.g. Cognito or Keycloak, called from the user's browser
     * (our Angular code). The IDP will ask the user for their log-in details (email and password).
     * If they are valid, the user will be authenticated and redirected here through this API call
     * passing in the profile of the user as recorded by the IDP.
     * <p>
     * This method looks up the user on the database using information in the profile and then
     * returns an authentication response containing user details and permissions.
     * <p>
     * A user is not considered logged in by the Angular code until this method returns successfully.
     * @param profile The authentication profile containing the user's credentials.
     * @return A map containing the authentication response with user details and permissions.
     */
    @PostMapping("login")
    public Map<String, Object> login(@RequestBody AuthProfile profile) {
        //Look up the user in the database using the profile.
        //Only throws an exception if there is a system error. See UserService.login() for details.
        User user = userService.login(profile);

        //This checks that the type of user is as expected. It is intended to check for unusual
        //cases such as an admin user logging in with a candidate's details, or vice versa.
        //Normally this should be a no-op.
        oAuth2UserService.checkUserClientId(user, OAuth2UserService.OAUTH_TC_ADMIN_CLIENT_ID);

        //The normal response contains some basic user details and permissions.
        AuthenticationResponse response = userService.createAuthenticationResponse(user);
        return authenticationDto().build(response);
    }

    @PostMapping("logout")
    public ResponseEntity<Void> logout() {
        userService.logout();
        return ResponseEntity.ok().build();
    }

    /**
     * Sets up Multi Factor Authentication (MFA) in the form of a
     * Time based One Time Password (TOTP).
     * <p/>
     * Generates a new secret key (hence POST rather than GET) and returns a Base64 encoded
     * QRCode image which can be displayed as described
     * <a href="https://www.w3docs.com/snippets/html/how-to-display-base64-images-in-html.html"> here.</a>
     * @return EncodedQrImage
     */
    @PostMapping("mfa-setup")
    public Map<String, Object> mfaSetup() {
        EncodedQrImage qr = userService.mfaSetup();
        return qrDto().build(qr);
    }

    DtoBuilder qrDto() {
        return new DtoBuilder()
                .add("base64Encoding")
                ;
    }

    DtoBuilder authenticationDto() {
        return new DtoBuilder()
            .add("canViewChats")
            .add("tcInstanceType")
            .add("user", userBriefDto())
            ;
    }

    private DtoBuilder userBriefDto() {
        return new DtoBuilder()
                .add("id")
                .add("username")
                .add("email")
                .add("role")
                .add("jobCreator")
                .add("readOnly")
                .add("firstName")
                .add("lastName")
                .add("usingMfa")
                .add("mfaConfigured")
                .add("partner", PartnerDtoHelper.getPartnerDto())
                ;
    }
}
