/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.db;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.db.CandidateJobExperience;

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
