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

import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.model.db.explanation.CandidateMatchingExplanation;
import org.tctalent.server.model.db.explanation.CandidateMatchingExplanationKey;

/**
 * See doc for {@link CandidateMatchingExplanation}
 */
public interface CandidateMatchingExplanationRepository extends
    JpaRepository<CandidateMatchingExplanation, CandidateMatchingExplanationKey> {

    /**
     * Saves the given explanation for the candidate and Matching, replacing any existing one -
     * but only if the Matching's updatedDate still equals the given updatedDate, captured
     * before the explanation was generated. Otherwise, the Matching's description has changed
     * since then, so the explanation is out of date and nothing is saved.
     * <p/>
     * This means that an explanation generated from an older description can never overwrite one
     * generated from the current description, even when requests run concurrently: the
     * Matching row is share locked while saving, so its description can't change between the
     * check and the save.
     * <p/>
     * The persistence context is cleared afterwards, so that explanations read later in the same
     * request are read from the database.
     *
     * @param candidateId Candidate id
     * @param matchingId Matching id
     * @param matchingUpdatedDate The Matching's updatedDate when generation started
     * @param explanationJson The explanation as jsonb - see {@link CandidateMatchingExplanation}
     * @param userId Id of the user saving the explanation, for the audit columns - null if none
     * @return 1 if the explanation was saved, 0 if the Matching has changed (or no longer exists)
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = """
        with unchanged_matching as (
            select id, updated_date from matching
            where id = :matchingId and updated_date = :matchingUpdatedDate
            for share
        )
        insert into candidate_matching_explanation (candidate_id, matching_id,
            matching_updated_date, explanation, created_date, created_by, updated_date, updated_by)
        select :candidateId, id, updated_date, cast(:explanationJson as jsonb),
            now(), cast(:userId as bigint), now(), cast(:userId as bigint)
        from unchanged_matching
        on conflict (candidate_id, matching_id) do update
            set matching_updated_date = excluded.matching_updated_date,
                explanation = excluded.explanation,
                updated_date = excluded.updated_date,
                updated_by = excluded.updated_by
        """, nativeQuery = true)
    int saveIfMatchingUnchanged(@Param("candidateId") long candidateId,
        @Param("matchingId") long matchingId,
        @Param("matchingUpdatedDate") OffsetDateTime matchingUpdatedDate,
        @Param("explanationJson") String explanationJson,
        @Param("userId") @Nullable Long userId);
}
