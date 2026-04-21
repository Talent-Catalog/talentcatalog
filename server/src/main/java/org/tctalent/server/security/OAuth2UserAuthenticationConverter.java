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

/**
 * Converts a JWT into a TcAuthenticationToken
 */
@Component
@RequiredArgsConstructor
public class OAuth2UserAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final OAuth2UserService oAuth2UserService;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String issuer = jwt.getIssuer().toString();
        String subject = jwt.getSubject();

        OAuth2AuthenticatedUser user =
            oAuth2UserService.loadUser(issuer, subject);

        return new TcAuthenticationToken(
            user,
            jwt,
            user.getAuthorities()
        );
    }
}
