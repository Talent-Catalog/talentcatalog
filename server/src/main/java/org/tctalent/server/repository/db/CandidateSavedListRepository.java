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

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.CandidateSavedList;
import org.tctalent.server.model.db.CandidateSavedListKey;

/**
 * See doc for {@link CandidateSavedList}
 */
public interface CandidateSavedListRepository extends
        JpaRepository<CandidateSavedList, CandidateSavedListKey> {

    List<CandidateSavedList> findBySavedList_Id(Long savedListId);
    List<CandidateSavedList> findByCandidate_Id(Long candidateId);

    long countBySavedList_Id(Long savedListId);

    @Query("select csl.candidate.id from CandidateSavedList csl"
            + " where csl.savedList.id = :savedListId and csl.candidate.id > :afterCandidateId"
            + " order by csl.candidate.id")
    List<Long> findCandidateIdsBySavedListId(@Param("savedListId") Long savedListId,
            @Param("afterCandidateId") Long afterCandidateId, Pageable pageable);
}
