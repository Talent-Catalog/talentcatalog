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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.InvalidPasswordFormatException;
import org.tbbtalent.server.exception.PasswordExpiredException;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.response.JwtAuthenticationResponse;
import org.tbbtalent.server.service.db.CaptchaService;
import org.tbbtalent.server.service.db.UserService;
import org.tbbtalent.server.util.dto.DtoBuilder;
import org.tbbtalent.server.util.qr.EncodedQrImage;

@RestController()
@RequestMapping("/api/admin/auth")
public class AuthAdminApi {

    private final UserService userService;
    private final CaptchaService captchaService;

    @Autowired
    public AuthAdminApi(UserService userService, CaptchaService captchaService) {
        this.userService = userService;
        this.captchaService = captchaService;
    }

    @PostMapping("login")
    public Map<String, Object> login(@RequestBody LoginRequest request)
            throws AccountLockedException, PasswordExpiredException, InvalidCredentialsException,
            InvalidPasswordFormatException {

        final String reCaptchaV3Token = request.getReCaptchaV3Token();
        if (reCaptchaV3Token != null) {
            //Do check for automated logins. Throws exception if it looks
            //automated.
            captchaService.processCaptchaV3Token(reCaptchaV3Token, "login");
        }

        JwtAuthenticationResponse response = this.userService.login(request);

        userService.mfaVerify(request.getTotpToken());

        return jwtDto().build(response);
    }

    @PostMapping("logout")
    public ResponseEntity logout() {
        this.userService.logout();
        return ResponseEntity.ok().build();
    }

    /**
     * Sets up Multi Factor Authentication (MFA) in the form of a 
     * Time based One Time Password (TOTP).
     * <p/>
     * Generates a new secret key (hence POST rather than GET) and returns a Base64 encoded 
     * QRCode image which can be displayed as described here:
     * https://www.w3docs.com/snippets/html/how-to-display-base64-images-in-html.html
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
                .add("readOnly")
                .add("firstName")
                .add("lastName")
                .add("usingMfa")
                ;
    }
}
