package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tbbtalent.server.model.CandidateEducation;

public interface CandidateEducationRepository extends JpaRepository<CandidateEducation, Long> {

    // TO DO ADD EDUCATION LEVEL TO CANDIDATE EDUCATION TABLE
//    @Query(" select e from CandidateEducation e "
//            + " where e.educationLevel.id = :id ")
//    List<CandidateEducation> findByEducationLevelId(@Param("id") Long id);
}
