/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Matching;

/**
 * Service for creating and updating {@link Matching} contexts.
 */
public interface MatchingService {

    /**
     * Creates and persists a new matching context.
     *
     * @param matchingDescription Initial description - null or blank if none
     * @return The persisted Matching
     */
    @NonNull
    Matching createMatching(@Nullable String matchingDescription);

    /**
     * Gets the Matching with the given id.
     *
     * @param id Matching id
     * @return The Matching
     * @throws NoSuchObjectException if there is no such Matching
     */
    @NonNull
    Matching getMatching(long id) throws NoSuchObjectException;

    /**
     * Changes the description of the given Matching - see
     * {@link Matching#updateMatchingDescription} for exactly when this counts as a change.
     * Nothing is saved if the description is unchanged.
     *
     * @param id Matching id
     * @param matchingDescription New description - null or blank if none
     * @return The (possibly updated) Matching
     * @throws NoSuchObjectException if there is no such Matching
     */
    @NonNull
    Matching updateMatchingDescription(long id, @Nullable String matchingDescription)
        throws NoSuchObjectException;
}
