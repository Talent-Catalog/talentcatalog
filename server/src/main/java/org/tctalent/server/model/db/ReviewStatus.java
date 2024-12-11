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
 * Records whether a candidate's inclusion in a saved search has been reviewed or not.
 * @see CandidateReviewStatusItem
 */
public enum ReviewStatus {

    /**
     * The status of the candidate in a saved search has not been reviewed
     */
    unverified,

    /**
     * It has been verified that the candidate does belong in the saved search
     */
    verified,

    /**
     * The candidate does not really belong in the saved search even though the search will find
     * them.
     */
    rejected

}
