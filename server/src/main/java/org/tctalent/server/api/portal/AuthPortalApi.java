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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import javax.security.auth.login.AccountLockedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.configuration.TranslationConfig;
import org.tctalent.server.exception.InvalidCredentialsException;
import org.tctalent.server.exception.InvalidPasswordFormatException;
import org.tctalent.server.exception.PasswordExpiredException;
import org.tctalent.server.exception.ReCaptchaInvalidException;
import org.tctalent.server.exception.UserDeactivatedException;
import org.tctalent.server.request.AuthenticateInContextTranslationRequest;
import org.tctalent.server.request.LoginRequest;
import org.tctalent.server.request.candidate.RegisterCandidateRequest;
import org.tctalent.server.response.JwtAuthenticationResponse;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController
@RequestMapping("/api/portal/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthPortalApi {

    private final UserService userService;
    private final CandidateService candidateService;
    private final TranslationConfig translationConfig;

    @PostMapping("xlate")
    public void authorizeInContextTranslation(@RequestBody AuthenticateInContextTranslationRequest request)
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

        JwtAuthenticationResponse response = userService.login(request);
        return jwtDto().build(response);
    }

    @PostMapping("logout")
    public ResponseEntity<Void> logout() {
        this.userService.logout();
        return ResponseEntity.ok().build();
    }

    @PostMapping("register")
    public Map<String, Object> register(
        HttpServletRequest httpRequest, @Valid @RequestBody RegisterCandidateRequest request)
            throws AccountLockedException, ReCaptchaInvalidException {

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
