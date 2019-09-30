package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.CandidateEducation;
import org.tbbtalent.server.model.EducationType;

import java.util.Optional;

public interface EducationRepository extends JpaRepository<CandidateEducation, Long> {

    @Query(" select e from CandidateEducation e "
            + " left join e.candidate c "
            + " where e.id = :id")
    Optional<CandidateEducation> findByIdLoadCandidate(@Param("id") Long id);

    @Query(" select e from CandidateEducation e "
            + " where e.educationType = :educationType")
    CandidateEducation findByIdLoadEducationType(@Param("educationType") EducationType educationType);
}
