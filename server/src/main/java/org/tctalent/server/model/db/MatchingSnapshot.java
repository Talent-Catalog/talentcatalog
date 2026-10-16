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

package org.tctalent.server.model.db;

import java.time.OffsetDateTime;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * A {@link Matching}'s description and updatedDate, read together at one point in time.
 * <p>
 * Anything derived from the description (eg a match explanation) records the snapshot's
 * updatedDate, so that it can later tell whether it was derived from the current description.
 *
 * @param id Matching id
 * @param matchingDescription Description at the time of the snapshot - null if none
 * @param updatedDate When the description last changed, as at the time of the snapshot
 */
public record MatchingSnapshot(
    long id, @Nullable String matchingDescription, @NonNull OffsetDateTime updatedDate) {

    @NonNull
    public static MatchingSnapshot of(@NonNull Matching matching) {
        return new MatchingSnapshot(
            matching.getId(), matching.getMatchingDescription(), matching.getUpdatedDate());
    }
}
