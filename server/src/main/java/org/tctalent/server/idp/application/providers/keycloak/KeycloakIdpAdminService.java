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

import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.tctalent.server.idp.api.request.RegisterUserRequest;
import org.tctalent.server.idp.domain.model.IdpAdminException;
import org.tctalent.server.idp.domain.model.IdpAdminService;
import org.tctalent.server.idp.domain.model.IdpUserProfile;
import org.tctalent.server.idp.domain.model.IdpUserRef;

/**
 * Adapter for Keycloak Admin Client implementing the provider-neutral {@link IdpAdminService}.
 *
 * <p>All Keycloak-specific interactions are kept inside this class so callers can use the
 * provider-neutral {@link IdpAdminService} API.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakIdpAdminService implements IdpAdminService {

    private final Keycloak keycloak;
    private final KeycloakAuthProperties properties;

    @Override
    public IdpUserRef registerUser(RegisterUserRequest request) {
        try {
            UserRepresentation user = new UserRepresentation();
            user.setUsername(request.getEmail());
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEnabled(true);
            if (request.getPublicId() != null) {
                user.setAttributes(java.util.Map.of("public_id", List.of(request.getPublicId())));
            }

            CredentialRepresentation cred = new CredentialRepresentation();
            cred.setType("password");
            cred.setValue(request.getPassword());
            user.setCredentials(java.util.List.of(cred));

            RealmResource realm = keycloak.realm(properties.getRealm());

            // Response holds HTTP resources, so close it after reading status/headers.
            try (Response resp = realm.users().create(user)) {
                int status = resp.getStatus();
                if (status != 201) {
                    throw new IdpAdminException(
                        "Failed to create user " + user.getEmail() + " in Keycloak: status=" + status);
                }

                String location = resp.getHeaderString("Location");
                if (location == null || location.isBlank()) {
                    throw new IdpAdminException(
                        "Keycloak did not return Location header after user creation");
                }

                String createdId = extractIdFromLocation(location);

                return new IdpUserRef(properties.getIssuer(), createdId, request.getEmail());
            }
        } catch (IdpAdminException e) {
            throw e;
        } catch (Exception e) {
            throw new IdpAdminException("Error creating user in Keycloak", e);
        }
    }

    @Override
    public IdpUserProfile getIdpUserProfile(IdpUserRef userRef) {
        RealmResource realm = keycloak.realm(properties.getRealm());
        UserResource userResource = realm.users().get(userRef.getSubject());
        UserRepresentation u = userResource.toRepresentation();

        String publicId = null;
        if (u.getAttributes() != null && u.getAttributes().get("public_id") != null
            && !u.getAttributes().get("public_id").isEmpty()) {
            publicId = u.getAttributes().get("public_id").get(0);
        }

        return IdpUserProfile.builder()
            .issuer(properties.getIssuer())
            .subject(u.getId())
            .username(u.getUsername())
            .email(u.getEmail())
            .firstName(u.getFirstName())
            .lastName(u.getLastName())
            .status("ENABLED")
            .publicId(publicId)
            .build();
    }

    /**
     * Extracts the Keycloak-created user id from the Location header. Location typically ends with
     * /users/{id}
     */
    private String extractIdFromLocation(String location) {
        int idx = location.lastIndexOf('/');
        if (idx == -1 || idx == location.length() - 1) {
            throw new IdpAdminException("Cannot extract user id from Location header: " + location);
        }
        return location.substring(idx + 1);
    }

    @Override
    public void updateEmail(IdpUserRef userRef, String newEmail) {
        RealmResource realm = keycloak.realm(properties.getRealm());
        UserResource userResource = realm.users().get(userRef.getSubject());
        UserRepresentation u = userResource.toRepresentation();
        u.setEmail(newEmail);
        u.setUsername(newEmail); // if username is email

        userResource.update(u);

        // We don't support email verification of on Keycloak (which is just used for local
        // testing). But if we did, this is how we would force Keycloak to send verification email.
        // If you execute the following line without setting Keycloak up for email verification,
        // it will fail with an HTTP 500 Internal Server Error.
//        userResource.executeActionsEmail(List.of("VERIFY_EMAIL"));
    }

}




