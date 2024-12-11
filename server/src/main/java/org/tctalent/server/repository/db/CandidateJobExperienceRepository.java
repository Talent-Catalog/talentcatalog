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

package org.tctalent.server.repository.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.CandidateJobExperience;

import java.util.Optional;

public interface CandidateJobExperienceRepository extends JpaRepository<CandidateJobExperience, Long> {

    @Query(" select w from CandidateJobExperience w "
            + " left join w.candidate c "
            + " where w.id = :id")
    Optional<CandidateJobExperience> findByIdLoadCandidate(@Param("id") Long id);

    @Query(" select w from CandidateJobExperience w "
            + " left join w.candidateOccupation o "
            + " where o.id = :candidateOccupationId")
    Page<CandidateJobExperience> findByCandidateOccupationId(@Param("candidateOccupationId") Long candidateOccupationId, Pageable request);

    int countByCandidateOccupationId(Long candidateOccupationId);

    @Query(" select e from CandidateJobExperience e "
            + " left join e.candidateOccupation o "
            + " where e.id = :id")
    Optional<CandidateJobExperience> findByIdLoadCandidateOccupation(@Param("id") Long id);

    @Query(" select w from CandidateJobExperience w "
            + " left join w.candidate c "
            + " where c.id = :candidateId")
    Page<CandidateJobExperience> findByCandidateId(@Param("candidateId") Long candidateId, Pageable request);

}
