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

package org.tctalent.server.api.portal;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.configuration.TranslationConfig;
import org.tctalent.server.exception.InvalidCredentialsException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.AuthenticateInContextTranslationRequest;
import org.tctalent.server.request.candidate.CompleteOauthAuthenticationRequest;
import org.tctalent.server.response.AuthenticationResponse;
import org.tctalent.server.security.OAuth2UserService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController
@RequestMapping("/api/portal/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthPortalApi {

    private final CandidateService candidateService;
    private final OAuth2UserService oAuth2UserService;
    private final TranslationConfig translationConfig;
    private final UserService userService;

    @PostMapping("xlate")
    public void authorizeInContextTranslation(@RequestBody AuthenticateInContextTranslationRequest request)
            throws InvalidCredentialsException {
        final String password = translationConfig.getPassword();
        if (password == null || !password.equals(request.getPassword())) {
            throw new InvalidCredentialsException("Not authorized");
        }
    }

    @PostMapping("logout")
    public ResponseEntity<Void> logout() {
        this.userService.logout();
        return ResponseEntity.ok().build();
    }

    @PostMapping("complete-auth")
    public Map<String, Object> completeAuthentication(
        @RequestBody CompleteOauthAuthenticationRequest request, @NonNull HttpServletRequest httpRequest) {

        //We need to handle both logins and registrations.
        //Is this a new user or one that we already have on our database?
        final boolean newUser = userService.isNewUser(request.getProfile());
        boolean consentedPartners = false;
        boolean consentedRegistration = false;
        User user;
        if (newUser) {
            Candidate candidate = candidateService.register(request, httpRequest);
            consentedPartners = candidate.getContactConsentPartners();
            consentedRegistration = candidate.getContactConsentRegistration();
            user = candidate.getUser();
        } else {
            user = userService.login(request.getProfile());
            oAuth2UserService.checkUserClientId(user, OAuth2UserService.OAUTH_TC_CANDIDATE_CLIENT_ID);
        }

        AuthenticationResponse response = userService.createAuthenticationResponse(user);
        response.setUserIsNew(newUser);
        response.setContactConsentPartners(consentedPartners);
        response.setContactConsentRegistration(consentedRegistration);

        return authenticationDto().build(response);
    }

    DtoBuilder authenticationDto() {
        return new DtoBuilder()
            .add("canViewChats")
            .add("contactConsentRegistration")
            .add("tcInstanceType")
            .add("user", candidateBriefDto())
            .add("userIsNew")
            ;
    }

    private DtoBuilder candidateBriefDto() {
        return new DtoBuilder()
            .add("id")
            .add("username")
            .add("firstName")
            .add("lastName")
            .add("email")
            ;
    }
}
