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

package org.tctalent.server.idp.application.providers.keycloak;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the Keycloak Admin Client.
 */
@Configuration
public class KeycloakConfig {
    @Value("${KEYCLOAK_ADMIN_PASSWORD:}")
    private String keycloakAdminPassword;


    @Bean
    public Keycloak keycloak(KeycloakAuthProperties properties) {
        KeycloakBuilder builder = KeycloakBuilder.builder()
            .serverUrl(properties.getServerUrl())
            //We log in with master realm, but users are added to the realm specified in
            //properties.getRealm() (e.g. talentcatalog).
            //See KeycloakIdpAdminService.registerUser() for details.
            .realm("master")
            .clientId("admin-cli")
            .username("admin")
            .password(keycloakAdminPassword)
            .grantType(OAuth2Constants.PASSWORD);

        return builder.build();
    }
}
