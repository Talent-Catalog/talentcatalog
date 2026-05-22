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

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;

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
            if (request.getTcUserId() != null) {
                user.setAttributes(java.util.Map.of("tc_user_id", java.util.List.of(request.getTcUserId())));
            }

            if (request.getTemporaryPassword() != null) {
                CredentialRepresentation cred = new CredentialRepresentation();
                cred.setType("password");
                cred.setValue(request.getTemporaryPassword());
                cred.setTemporary(Boolean.TRUE);
                user.setCredentials(java.util.List.of(cred));
            }

            RealmResource realm = keycloak.realm(properties.getRealm());
            Response resp = realm.users().create(user);
            int status = resp.getStatus();
            if (status != 201) {
                throw new IdpAdminException("Failed to create user in Keycloak: status=" + status);
            }

            String location = resp.getHeaderString("Location");
            if (location == null || location.isBlank()) {
                throw new IdpAdminException("Keycloak did not return Location header after user creation");
            }

            String createdId = extractIdFromLocation(location);

            return new IdpUserRef(properties.getIssuer(), createdId, request.getEmail());
        } catch (IdpAdminException e) {
            throw e;
        } catch (Exception e) {
            throw new IdpAdminException("Error creating user in Keycloak", e);
        }
    }

    @Override
    public IdpUserProfile getIdpUserProfile(IdpUserRef userRef) {
        RealmResource realm = keycloak.realm(properties.getRealm());
        var userResource = realm.users().get(userRef.getSubject());
        UserRepresentation u = userResource.toRepresentation();

        String tcUserId = null;
        if (u.getAttributes() != null && u.getAttributes().get("tc_user_id") != null
            && !u.getAttributes().get("tc_user_id").isEmpty()) {
            tcUserId = u.getAttributes().get("tc_user_id").get(0);
        }

        return IdpUserProfile.builder()
            .issuer(properties.getIssuer())
            .subject(u.getId())
            .username(u.getUsername())
            .email(u.getEmail())
            .firstName(u.getFirstName())
            .lastName(u.getLastName())
            .status("ENABLED")
            .tcUserId(tcUserId)
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
}




