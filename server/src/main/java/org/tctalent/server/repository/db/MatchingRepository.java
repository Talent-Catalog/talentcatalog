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

package org.tctalent.server.repository.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.Matching;
import org.tctalent.server.model.db.MatchingSnapshot;

/**
 * See doc for {@link Matching}
 */
public interface MatchingRepository extends JpaRepository<Matching, Long> {

    /**
     * Reads the Matching's current description and updatedDate from the database - unlike
     * findById, which returns any copy of the Matching already loaded by the current
     * persistence context, which may be out of date.
     */
    @Query("select new org.tctalent.server.model.db.MatchingSnapshot("
        + " m.id, m.matchingDescription, m.updatedDate) from Matching m where m.id = :id")
    Optional<MatchingSnapshot> findSnapshot(@Param("id") long id);
}
