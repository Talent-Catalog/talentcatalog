package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.WorkExperience;

import java.util.Optional;

public interface WorkExperienceRepository extends JpaRepository<WorkExperience, Long> {

    @Query(" select w from WorkExperience w "
            + " left join w.candidate c "
            + " where w.id = :id")
    Optional<WorkExperience> findByIdLoadCandidate(@Param("id") Long id);
}
