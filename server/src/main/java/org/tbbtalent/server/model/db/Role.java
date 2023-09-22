/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.model.db;

/**
 * Different user roles which define what each user is authorized to do.
 * <p/>
 * There are two broad categories of user: Candidates and Staff.
 * <p/>
 * Candidates interact with the "front end" of the software: the "candidate portal".
 * Staff interact with the "back end": the "admin portal".
 * <p/>
 * All candidates have Role.user.
 * <p/>
 * Staff have different roles (not Role.user) based on their level of access to the data and system
 * configuration.
 */
public enum Role {

    systemadmin,
    admin,

    /**
     * All candidates have Role.user.
     */
    user,

    sourcepartneradmin,
    semilimited,
    limited
}
