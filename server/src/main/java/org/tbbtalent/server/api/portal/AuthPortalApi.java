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

package org.tbbtalent.server.api.portal;

import java.util.Map;
import javax.security.auth.login.AccountLockedException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.configuration.TranslationConfig;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.InvalidPasswordFormatException;
import org.tbbtalent.server.exception.PasswordExpiredException;
import org.tbbtalent.server.exception.ReCaptchaInvalidException;
import org.tbbtalent.server.exception.UserDeactivatedException;
import org.tbbtalent.server.request.AuthorizeInContextTranslationRequest;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.request.candidate.RegisterCandidateRequest;
import org.tbbtalent.server.response.JwtAuthenticationResponse;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.CaptchaService;
import org.tbbtalent.server.service.db.UserService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/auth")
public class AuthPortalApi {
    private static final Logger log = LoggerFactory.getLogger(AuthPortalApi.class);

    private final UserService userService;
    private final CandidateService candidateService;
    private final CaptchaService captchaService;
    private final TranslationConfig translationConfig;

    @Autowired
    public AuthPortalApi(UserService userService,
        CaptchaService captchaService,
        CandidateService candidateService,
        TranslationConfig translationConfig) {
        this.userService = userService;
        this.candidateService = candidateService;
        this.captchaService = captchaService;
        this.translationConfig = translationConfig;
    }

    @PostMapping("xlate")
    public void authorizeInContextTranslation(@RequestBody AuthorizeInContextTranslationRequest request)
            throws InvalidCredentialsException {
        final String password = translationConfig.getPassword();
        if (password == null || !password.equals(request.getPassword())) {
            throw new InvalidCredentialsException("Not authorized");
        }
    }

    @PostMapping("login")
    public Map<String, Object> login(@RequestBody LoginRequest request)
            throws AccountLockedException, PasswordExpiredException, InvalidCredentialsException,
            InvalidPasswordFormatException, UserDeactivatedException,
            ReCaptchaInvalidException {

        //Do check for automated logins. Throws exception if it looks
        //automated.
        captchaService.processCaptchaV3Token(request.getReCaptchaV3Token(), "login");

        JwtAuthenticationResponse response = userService.login(request);
        return jwtDto().build(response);
    }

    @PostMapping("logout")
    public ResponseEntity logout() {
        this.userService.logout();
        return ResponseEntity.ok().build();
    }

    @PostMapping("register")
    public Map<String, Object> register(
        HttpServletRequest httpRequest, @Valid @RequestBody RegisterCandidateRequest request)
            throws AccountLockedException, ReCaptchaInvalidException {

        //Do check for automated registrations. Throws exception if it looks
        //automated.
        captchaService.processCaptchaV3Token(request.getReCaptchaV3Token(), "registration");

        LoginRequest loginRequest = candidateService.register(request, httpRequest);

        JwtAuthenticationResponse jwt = userService.login(loginRequest);
        return jwtDto().build(jwt);
    }

    DtoBuilder jwtDto() {
        return new DtoBuilder()
                .add("accessToken")
                .add("tokenType")
                .add("user", candidateBriefDto())
                ;
    }

    private DtoBuilder candidateBriefDto() {
        return new DtoBuilder()
                .add("id")
                .add("username")
                .add("email")
                ;
    }
}
