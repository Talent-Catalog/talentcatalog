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
package org.tctalent.server.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.tctalent.server.configuration.SecurityConfiguration;

/**
 * Converts a JWT into a TcAuthenticationToken by looking up the user in the database from
 * the Oauth issuer and subject data contained in the JWT. If the user is not found,
 * an exception is thrown and the authentication fails.
 * <p>
 * This is the normal authentication flow.
 * <p>
 * It is configured into our application's security configuration in {@link SecurityConfiguration}.
 */
@Component
@RequiredArgsConstructor
public class OAuth2UserAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final OAuth2UserService oAuth2UserService;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String issuer = jwt.getIssuer().toString();
        String subject = jwt.getSubject();

        CurrentUserInfo currentUserInfo =
            oAuth2UserService.loadUser(issuer, subject);

        return new TcAuthenticationToken(
            currentUserInfo,
            jwt,
            currentUserInfo.getAuthorities()
        );
    }
}
