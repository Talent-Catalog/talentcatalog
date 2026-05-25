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

package org.tctalent.server.idp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.tctalent.server.idp.api.request.RegisterUserRequest;
import org.tctalent.server.idp.application.providers.keycloak.KeycloakAuthProperties;
import org.tctalent.server.idp.application.providers.keycloak.KeycloakIdpAdminService;
import org.tctalent.server.idp.domain.model.IdpAdminException;
import org.tctalent.server.idp.domain.model.IdpUserProfile;
import org.tctalent.server.idp.domain.model.IdpUserRef;

class KeycloakIdpAdminServiceTest {

    private Keycloak keycloak;
    private RealmResource realm;
    private UsersResource users;
    private KeycloakAuthProperties props;
    private KeycloakIdpAdminService svc;

    @BeforeEach
    void setUp() {
        keycloak = mock(Keycloak.class);
        realm = mock(RealmResource.class);
        users = mock(UsersResource.class);
        when(keycloak.realm(any())).thenReturn(realm);
        when(realm.users()).thenReturn(users);

        props = new KeycloakAuthProperties();
        props.setRealm("test-realm");
        props.setIssuer("https://auth.example.com");

        svc = new KeycloakIdpAdminService(keycloak, props);
    }

    @Test
    void registerUser_success() {
        Response resp = mock(Response.class);
        when(resp.getStatus()).thenReturn(201);
        when(resp.getHeaderString("Location")).thenReturn("http://kc/auth/admin/realms/test-realm/users/created-id");
        when(users.create(any())).thenReturn(resp);

        RegisterUserRequest req = RegisterUserRequest.builder()
            .email("bob@example.com")
            .firstName("Bob")
            .lastName("Builder")
            .tcUserId("tc-123")
            .temporaryPassword("tempPass")
            .build();

        IdpUserRef ref = svc.registerUser(req);

        assertEquals(props.getIssuer(), ref.getIssuer());
        assertEquals("created-id", ref.getSubject());
        assertEquals("bob@example.com", ref.getUsername());
    }

    @Test
    void registerUser_non201_throws() {
        Response resp = mock(Response.class);
        when(resp.getStatus()).thenReturn(400);
        when(users.create(any())).thenReturn(resp);

        RegisterUserRequest req = RegisterUserRequest.builder().email("x@x").build();

        assertThrows(IdpAdminException.class, () -> svc.registerUser(req));
    }

    @Test
    void registerUser_missingLocation_throws() {
        Response resp = mock(Response.class);
        when(resp.getStatus()).thenReturn(201);
        when(resp.getHeaderString("Location")).thenReturn(null);
        when(users.create(any())).thenReturn(resp);

        RegisterUserRequest req = RegisterUserRequest.builder().email("x@x").build();

        assertThrows(IdpAdminException.class, () -> svc.registerUser(req));
    }

    @Test
    void getIdpUserProfile_success_and_tcUserId_present() {
        UserResource ur = mock(UserResource.class);
        UserRepresentation u = new UserRepresentation();
        u.setId("uid-1");
        u.setUsername("alice");
        u.setEmail("alice@example.com");
        u.setEmailVerified(Boolean.TRUE);
        u.setFirstName("Alice");
        u.setLastName("Doe");
        u.setEnabled(Boolean.TRUE);
        u.setAttributes(Map.of("tc_user_id", List.of("tc-555")));

        when(users.get("uid-1")).thenReturn(ur);
        when(ur.toRepresentation()).thenReturn(u);

        IdpUserRef ref = new IdpUserRef(props.getIssuer(), "uid-1", "alice");
        IdpUserProfile p = svc.getIdpUserProfile(ref);

        assertEquals("uid-1", p.getSubject());
        assertEquals("alice", p.getUsername());
        assertEquals("tc-555", p.getTcUserId());
        assertEquals("ENABLED", p.getStatus());
    }

    @Test
    void getIdpUserProfile_missingTcUserId_mapsNull() {
        UserResource ur = mock(UserResource.class);
        UserRepresentation u = new UserRepresentation();
        u.setId("uid-2");
        u.setUsername("joe");
        u.setEnabled(Boolean.FALSE);
        u.setAttributes(null);

        when(users.get("uid-2")).thenReturn(ur);
        when(ur.toRepresentation()).thenReturn(u);

        IdpUserRef ref = new IdpUserRef(props.getIssuer(), "uid-2", "joe");
        IdpUserProfile p = svc.getIdpUserProfile(ref);

        assertNull(p.getTcUserId());
    }
}



