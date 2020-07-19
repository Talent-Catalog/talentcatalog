/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.db.CandidateEducation;
import org.tbbtalent.server.model.db.EducationType;

public interface EducationRepository extends JpaRepository<CandidateEducation, Long> {

    @Query(" select e from CandidateEducation e "
            + " left join e.candidate c "
            + " where e.id = :id")
    Optional<CandidateEducation> findByIdLoadCandidate(@Param("id") Long id);

    @Query(" select e from CandidateEducation e "
            + " where e.educationType = :educationType")
    CandidateEducation findByIdLoadEducationType(@Param("educationType") EducationType educationType);
}
