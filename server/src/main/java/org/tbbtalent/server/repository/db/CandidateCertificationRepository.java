/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.db;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.db.CandidateCertification;

public interface CandidateCertificationRepository extends JpaRepository<CandidateCertification, Long> {

    @Query(" select f from CandidateCertification f "
            + " left join f.candidate c "
            + " where f.id = :id")
    Optional<CandidateCertification> findByIdLoadCandidate(@Param("id") Long id);

    @Query(" select f from CandidateCertification f "
            + " where f.candidate.id = :candidateId ")
    List<CandidateCertification> findByCandidateId(@Param("candidateId") Long candidateId);
}
