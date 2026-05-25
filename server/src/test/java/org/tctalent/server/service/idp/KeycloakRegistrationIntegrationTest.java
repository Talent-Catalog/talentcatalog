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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.tctalent.server.idp.IdpAdminService;
import org.tctalent.server.idp.IdpUserProfile;
import org.tctalent.server.idp.IdpUserRef;
import org.tctalent.server.idp.KeycloakAuthProperties;
import org.tctalent.server.idp.RegisterUserRequest;

/**
 * Integration test for Keycloak user registration.
 *
 * <p>This test requires a running Keycloak instance as configured in application.yml.
 * It is tagged with 'skip-test-in-gradle-build' to avoid failing in CI environments
 * where Keycloak is not available.</p>
 */
@SpringBootTest
@Tag("skip-test-in-gradle-build")
@Slf4j
class KeycloakRegistrationIntegrationTest {

    @Autowired
    private IdpAdminService idpAdminService;

    @Autowired
    private KeycloakAuthProperties properties;

    @Test
    void registerSingleUserOnKeycloak() {
        log.info("Testing Keycloak registration with server: {}, realm: {}, client: {}, user: {}",
            properties.getServerUrl(), properties.getRealm(), properties.getClientId(), properties.getUsername());

        String uniqueEmail = "test-user-" + UUID.randomUUID() + "@example.com";
        RegisterUserRequest request = RegisterUserRequest.builder()
            .email(uniqueEmail)
            .firstName("Integration")
            .lastName("Test")
            .tcUserId("tc-" + UUID.randomUUID())
            .temporaryPassword("Temp123!")
            .build();

        try {
            IdpUserRef userRef = idpAdminService.registerUser(request);

            assertNotNull(userRef);
            assertNotNull(userRef.getSubject());
            assertEquals(uniqueEmail, userRef.getUsername());

            // Verify we can fetch the profile back
            IdpUserProfile profile = idpAdminService.getIdpUserProfile(userRef);
            assertNotNull(profile);
            assertEquals(uniqueEmail, profile.getEmail());
            assertEquals("Integration", profile.getFirstName());
            assertEquals("Test", profile.getLastName());
        } catch (Exception e) {
            log.error("Failed to register user. If you get 400 Bad Request, check if realm '{}' exists " +
                "and client '{}' has 'Direct Access Grants Enabled' or is properly configured.",
                properties.getRealm(), properties.getClientId());
            throw e;
        }
    }
}
