package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.CandidateEducation;
import org.tbbtalent.server.model.EducationType;

import java.util.List;
import java.util.Optional;

public interface CandidateEducationRepository extends JpaRepository<CandidateEducation, Long> {

    // TO DO ADD EDUCATION LEVEL TO CANDIDATE EDUCATION TABLE
//    @Query(" select e from CandidateEducation e "
//            + " where e.educationLevel.id = :id ")
//    List<CandidateEducation> findByEducationLevelId(@Param("id") Long id);

    @Query(" select e from CandidateEducation e "
            + " left join e.candidate c "
            + " where e.id = :id")
    Optional<CandidateEducation> findByIdLoadCandidate(@Param("id") Long id);

    @Query(" select e from CandidateEducation e "
            + " join e.country "
            + " join e.educationMajor "
            + " where e.candidate.id = :candidateId ")
    List<CandidateEducation> findByCandidateId(@Param("candidateId") Long candidateId);
}
