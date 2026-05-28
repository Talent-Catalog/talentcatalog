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

package org.tctalent.server.idp.domain.model;

import org.tctalent.server.idp.api.request.RegisterUserRequest;

/**
 * Our common interface for interacting with IDP providers such as Cognito and Keycloak.
 */
public interface IdpAdminService {

    /**
     * Get the user profile from the IDP.
     * @param userRef Identifies the user in the IDP.
     * @return User profile.
     */
    IdpUserProfile getIdpUserProfile(IdpUserRef userRef);


    /**
     * Register a user on the IDP.
     * @param request Data required to register the user.
     * @return User profile.
     */
    IdpUserRef registerUser(RegisterUserRequest request);
}

