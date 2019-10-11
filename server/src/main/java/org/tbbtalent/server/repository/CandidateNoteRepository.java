package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.CandidateNote;

import java.util.List;
import java.util.Optional;

public interface CandidateNoteRepository extends JpaRepository<CandidateNote, Long> {

    @Query(" select n from CandidateNote n "
            + " left join n.candidate c "
            + " where n.id = :id")
    Optional<CandidateNote> findByIdLoadCandidate(@Param("id") Long id);

    @Query(" select n from CandidateNote n "
            + " where n.candidate.id = :candidateId ")
    List<CandidateNote> findByCandidateId(@Param("candidateId") Long candidateId);
}
