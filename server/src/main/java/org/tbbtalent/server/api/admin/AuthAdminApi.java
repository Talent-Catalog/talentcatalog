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

package org.tbbtalent.server.api.admin;

import java.util.Map;
import javax.security.auth.login.AccountLockedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.InvalidPasswordFormatException;
import org.tbbtalent.server.exception.PasswordExpiredException;
import org.tbbtalent.server.model.db.PartnerDtoHelper;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.response.JwtAuthenticationResponse;
import org.tbbtalent.server.service.db.CaptchaService;
import org.tbbtalent.server.service.db.UserService;
import org.tbbtalent.server.util.dto.DtoBuilder;
import org.tbbtalent.server.util.qr.EncodedQrImage;

/**
 * Rest controller that handles endpoints for user login, logout and Multi-Factor Authentication setup. Login
 * authentication requires a valid username and password; in addition to which Multi-Factor Authentication or a valid
 * Recaptcha tokens is required.
 * <p/>
 * The controller delegates to the UserService for login authentication, logout processing, MFA setup and MFA
 * verification. And delegates to the CaptchaService for processing and verifying Recaptcha tokens.
 */
@RestController()
@RequestMapping("/api/admin/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthAdminApi {

    private final UserService userService;
    private final CaptchaService captchaService;

    @PostMapping("login")
    public Map<String, Object> login(@RequestBody LoginRequest request)
            throws AccountLockedException, PasswordExpiredException, InvalidCredentialsException,
            InvalidPasswordFormatException {

        JwtAuthenticationResponse response = userService.login(request);

        //If we are using mfa don't worry about Captcha stuff
        //It is sometimes not reliable (especially in test from localhost) - and unnecessary with MFA
        User user = response.getUser();
        if (user.getUsingMfa()) {
            //If they are not yet configured we skip verification but they will be required
            //to set up mfa as soon as they log in.
            if (user.getMfaConfigured()) {
                userService.mfaVerify(request.getTotpToken());
            }
        } else {
            final String reCaptchaV3Token = request.getReCaptchaV3Token();
            if (reCaptchaV3Token != null) {
                //Do check for automated logins. Throws exception if it looks
                //automated.
                captchaService.processCaptchaV3Token(reCaptchaV3Token, "login");
            }
        }

        return jwtDto().build(response);
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

    DtoBuilder jwtDto() {
        return new DtoBuilder()
                .add("accessToken")
                .add("tokenType")
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
