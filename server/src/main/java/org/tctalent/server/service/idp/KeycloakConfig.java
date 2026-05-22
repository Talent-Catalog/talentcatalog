/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.idp;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the Keycloak Admin Client.
 */
@Configuration
public class KeycloakConfig {

    @Bean
    public Keycloak keycloak(KeycloakAuthProperties properties) {
        KeycloakBuilder builder = KeycloakBuilder.builder()
            .serverUrl(properties.getServerUrl())
            //todo Need to make users go into talentcatalog realm instead of master, but for now we can just use master to avoid having to set up a separate realm and client for the admin operations
            .realm("master")
            .clientId("admin-cli")
            .username("admin")
            .password("admin")
            .grantType(OAuth2Constants.PASSWORD);
//            .realm(properties.getRealm())
//            .clientId(properties.getClientId());
//
//        if (properties.getClientSecret() != null && !properties.getClientSecret().isBlank()) {
//            builder.clientSecret(properties.getClientSecret());
//        }
//
//        if (properties.getUsername() != null && !properties.getUsername().isBlank()) {
//            builder.username(properties.getUsername())
//                   .password(properties.getPassword())
//                   .grantType("password");
//        } else {
//            builder.grantType("client_credentials");
//        }

        return builder.build();
    }
}
