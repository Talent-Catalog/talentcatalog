package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.CandidateOccupation;

import java.util.List;
import java.util.Optional;

public interface CandidateOccupationRepository extends JpaRepository<CandidateOccupation, Long> {

    @Query(" select p from CandidateOccupation p "
            + " left join p.candidate c "
            + " where p.id = :id")
    Optional<CandidateOccupation> findByIdLoadCandidate(@Param("id") Long id);

    @Query(value = " select co from CandidateOccupation co "
            + " left join co.candidate c "
            + " left join co.occupation o "
            + " where c.id = :candidateId")
    List<CandidateOccupation> findByCandidateIdLoadOccupation(@Param("candidateId") Long candidateId);
}
