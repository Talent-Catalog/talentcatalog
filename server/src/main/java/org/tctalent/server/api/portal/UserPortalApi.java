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

package org.tctalent.server.api.portal;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.tctalent.server.exception.ExpiredTokenException;
import org.tctalent.server.exception.InvalidPasswordFormatException;
import org.tctalent.server.exception.InvalidPasswordTokenException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.ReCaptchaInvalidException;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.user.CheckPasswordResetTokenRequest;
import org.tctalent.server.request.user.ResetPasswordRequest;
import org.tctalent.server.request.user.SendResetPasswordEmailRequest;
import org.tctalent.server.request.user.UpdateUserPasswordRequest;
import org.tctalent.server.service.db.CaptchaService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.dto.DtoBuilder;


import org.tctalent.server.request.user.emailverify.SendVerifyEmailRequest;
import org.tctalent.server.request.user.emailverify.VerifyEmailRequest;
import org.tctalent.server.exception.InvalidEmailVerificationTokenException;
import org.tctalent.server.exception.ExpiredEmailTokenException;

@RestController
@RequestMapping("/api/portal/user")
public class UserPortalApi {

    private final CaptchaService captchaService;
    private final UserService userService;

    @Autowired
    public UserPortalApi(CaptchaService captchaService, UserService userService) {
        this.captchaService = captchaService;
        this.userService = userService;
    }

    @GetMapping()
    public Map<String, Object> getMyUser() {
        User user = userService.getLoggedInUser();
        return userBriefDto().build(user);
    }

    @PostMapping("password")
    public void updatePassword(@Valid @RequestBody UpdateUserPasswordRequest request)  {
        userService.updatePassword(request);
    }

    @PostMapping(value="reset-password-email")
    public void sendResetPasswordEmail(
            @RequestBody SendResetPasswordEmailRequest request)
            throws NoSuchObjectException, ReCaptchaInvalidException {

        //Do check for automated reset requests. Throws exception if it looks
        //automated.
        captchaService.processCaptchaV3Token(request.getReCaptchaV3Token(), "resetPassword");

        userService.generateResetPasswordToken(request);
    }

    @PostMapping(value="check-token")
    public void checkResetTokenValidity(@RequestBody CheckPasswordResetTokenRequest request) throws ExpiredTokenException, InvalidPasswordTokenException {
        userService.checkResetToken(request);
    }

    @PostMapping(value="reset-password")
    public void resetPassword(@RequestBody ResetPasswordRequest request) throws InvalidPasswordFormatException {
        userService.resetPassword(request);
    }

    @PostMapping("/verify-email")
    public void sendVerifyEmailRequest(@RequestBody SendVerifyEmailRequest request)
            throws NoSuchObjectException, ReCaptchaInvalidException {
        userService.sendVerifyEmailRequest(request);
    }

    @GetMapping("/verify-email/{token}")
    public void verifyEmail(@PathVariable("token") String token) throws ExpiredEmailTokenException, InvalidEmailVerificationTokenException {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setToken(token);
        userService.verifyEmail(request);
        System.out.println(token);
    }

    private DtoBuilder userBriefDto() {
        return new DtoBuilder()
                .add("id")
                .add("username")
                .add("email")
                .add("firstName")
                .add("lastName")
                .add("emailVerified")
                ;
    }




}
