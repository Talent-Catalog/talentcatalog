package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.Education;
import org.tbbtalent.server.model.EducationType;

import java.util.Optional;

public interface EducationRepository extends JpaRepository<Education, Long> {

    @Query(" select e from Education e "
            + " left join e.candidate c "
            + " where e.id = :id")
    Optional<Education> findByIdLoadCandidate(@Param("id") Long id);

    @Query(" select e from Education e "
            + " where e.educationType = :educationType")
    Education findByIdLoadEducationType(@Param("educationType") EducationType educationType);
}
