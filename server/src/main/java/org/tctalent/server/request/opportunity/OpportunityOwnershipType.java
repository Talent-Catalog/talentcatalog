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

package org.tctalent.server.request.opportunity;

/**
 * There are a number of ways that an opportunity can be owned:
 * <ul>
 *     <li>
 *        Source partners own the opportunities of candidates that they are responsible for.
 *     </li>
 *     <li>
 *        Job creators own the opportunities associated with the jobs that they created
 *     </li>
 * </ul>
 *
 * @author John Cameron
 */
public enum OpportunityOwnershipType {
    AS_SOURCE_PARTNER,
    AS_JOB_CREATOR
}
