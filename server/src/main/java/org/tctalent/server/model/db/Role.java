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

package org.tctalent.server.model.db;

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

    /**
     * Can do anything - intended for tech staff.
     * Only level which can create partners
     * Only level which can modify configuration for whole system
     * Only level which can create new system admin
     * Only level which can create new users assigning any partner to them - not just the default
     * Only level which can see all data, no matter which partner they are associated with.
     */
    systemadmin,

    /**
     * Can create new users, including administrators, for their own partner
     */
    admin,

    /**
     * Can create new users, including administrators, for their own partner
     * Can’t assign regions to admins
     * Can’t add or modify anything to do with the appearance of the app - ie can’t modify
     * Posts and Pages, User Registration Options, Localization or General Settings
     */
    partneradmin,

    /**
     * Can't create new users
     * Can’t see candidate’s name or contact info,
     * Can’t see file attachments
     * Can’t update candidate comments
     */
    semilimited,

    /**
     * Can’t see a refugee’s location info - country, province, nationality
     */
    limited,

    /**
     * All candidates have Role.user.
     * They can only see information relevant to themselves.
     */
    user
}
