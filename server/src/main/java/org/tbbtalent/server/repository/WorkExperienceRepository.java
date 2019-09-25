package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.CandidateJobExperience;

import java.util.Optional;

public interface WorkExperienceRepository extends JpaRepository<CandidateJobExperience, Long> {

    @Query(" select w from CandidateJobExperience w "
            + " left join w.candidate c "
            + " where w.id = :id")
    Optional<CandidateJobExperience> findByIdLoadCandidate(@Param("id") Long id);
}
